/*
 * AutoRecoveryManager.java 21 nov. 2006
 *
 * Copyright (c) 2010 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.eteks.sweethome3d.io;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.channels.OverlappingFileLockException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import com.eteks.sweethome3d.model.CollectionEvent;
import com.eteks.sweethome3d.model.CollectionListener;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeApplication;
import com.eteks.sweethome3d.model.InterruptedRecorderException;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.UserPreferences.Property;
import com.eteks.sweethome3d.tools.OperatingSystem;

/**
 * Manager able to automatically save open homes in recovery folder with a timer.
 * The delay between two automatic save operations is specified by 
 * {@link UserPreferences#getAutoSaveDelayForRecovery() auto save delay for recovery}
 * property.
 * @author Emmanuel Puybaret
 */
public class AutoRecoveryManager {
  private static final String RECOVERY_SUB_FOLDER        = "recovery";
  private static final String RECOVERED_FILE_EXTENSION = ".recovered";

  private final HomeApplication             application;
  private final List<Home>                  recoveredHomes      = new ArrayList<Home>();
  private final Map<Home, File>             autoSavedFiles      = new HashMap<Home, File>();
  private final Map<File, FileOutputStream> lockedOutputStreams = new HashMap<File, FileOutputStream>();
  private final ExecutorService             autoSaveForRecoveryExecutor;
  private Timer                             timer;
  private long                              lastAutoSaveTime;

  /**
   * Creates a manager able to automatically recover <code>application</code> homes.
   */
  public AutoRecoveryManager(HomeApplication application) throws RecorderException {
    this.application = application;
    this.autoSaveForRecoveryExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
        public Thread newThread(Runnable runnable) {
          Thread thread = new Thread(runnable);
          thread.setPriority(Thread.MIN_PRIORITY);
          return thread;
        }
      });
    
    readRecoveredHomes();
    
    // Interrupt auto saving when program stops
    Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          autoSaveForRecoveryExecutor.shutdownNow();
        }
      });
    
    // Remove auto saved files when a home is closed
    application.addHomesListener(new CollectionListener<Home>() {
        public void collectionChanged(CollectionEvent<Home> ev) {
          if (ev.getType() == CollectionEvent.Type.DELETE) {
            final Home home = ev.getItem();
            autoSaveForRecoveryExecutor.submit(new Runnable() {
                public void run() {
                  try {
                    final File homeFile = autoSavedFiles.get(home);
                    if (homeFile != null) {
                      freeLockedFile(homeFile);
                      homeFile.delete();
                      autoSavedFiles.remove(home);
                    }
                  } catch (RecorderException ex) {
                  }
                }
              });
          }
        }
      });
    
    // Add a listener on auto save delay that will run auto save timer
    application.getUserPreferences().addPropertyChangeListener(Property.AUTO_SAVE_DELAY_FOR_RECOVERY, new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          restartTimer();
        }
      });
    restartTimer();
  }

  /**
   * Reads the homes to recover.
   */
  private void readRecoveredHomes() throws RecorderException {
    File [] recoveredFiles = getRecoveryFolder().listFiles(new FileFilter() {
        public boolean accept(File file) {
          return file.isFile()
              && file.getName().endsWith(RECOVERED_FILE_EXTENSION);
        }
      });
    if (recoveredFiles != null) {
      Arrays.sort(recoveredFiles, new Comparator<File>() {
          public int compare(File f1, File f2) {
            if (f1.lastModified() < f2.lastModified()) {
              return 1;
            } else {
              return -1;
            }
          }
        });
      for (final File file : recoveredFiles) {
        if (!isFileLocked(file)) {
          try {
            final Home home = this.application.getHomeRecorder().readHome(file.getPath());
            // Recovered homes are the ones with a name different from the file path 
            if (home.getName() == null 
                || !file.equals(new File(home.getName()))) {
              home.setRecovered(true);
              // Delete recovered file once home isn't recovered anymore
              home.addPropertyChangeListener(Home.Property.RECOVERED, new PropertyChangeListener() {
                  public void propertyChange(PropertyChangeEvent evt) {
                    if (!home.isRecovered()) {
                      file.delete();
                    }
                  }
                });
              this.recoveredHomes.add(home);
            }
          } catch (RecorderException ex) {
            if (recoveredFiles.length > 1) {
              // Let's give a chance to other files
              ex.printStackTrace();
            } else {
              throw ex; 
            }
          }
        }
      }
    }
  }

  /**
   * Returns <code>true</code> if the given file is locked or can't accessed.
   */
  private boolean isFileLocked(final File file) {
    FileOutputStream out = null;
    try {
      // Check file lock is free
      out = new FileOutputStream(file, true); 
      return out.getChannel().tryLock() == null;
    } catch (IOException ex) {
      // Forget this file
      return true;
    } finally {
      if (out != null) {
        try {
          out.close();
        } catch (IOException ex) {
          return true;
        }
      }
    }
  }

  /**
   * Opens recovered homes and adds them to application. 
   */
  public void openRecoveredHomes() {
    for (Home recoveredHome : this.recoveredHomes) {
      boolean recoveredHomeOpen = false;
      for (Home home : this.application.getHomes()) {
        // If recovered home matches an opened home, open it as a new home
        if (home.getName() != null
            && home.getName().equals(recoveredHome.getName())) {
          recoveredHome.setName(null);
          this.application.addHome(recoveredHome);
          recoveredHomeOpen = true;
          break;
        }
      }
      if (!recoveredHomeOpen) {
        this.application.addHome(recoveredHome);
      }
    }
    // Clear the list to avoid open twice the recovered homes
    this.recoveredHomes.clear();
  }
  
  /**
   * Restarts the timer that regularly saves application homes. 
   */
  private void restartTimer() {
    if (this.timer != null) {
      this.timer.cancel();
      this.timer = null;
    }
    int autoSaveDelayForRecovery = this.application.getUserPreferences().getAutoSaveDelayForRecovery();
    if (autoSaveDelayForRecovery > 0) {
      this.timer = new Timer("autoSaveTimer", true);
      TimerTask task = new TimerTask() {
        @Override
        public void run() {
          if (System.currentTimeMillis() - lastAutoSaveTime > 30000) {
            for (Home home : application.getHomes()) {
              if (application.getHomes().contains(home)) {
                cloneAndSaveHome(home);
              }
            }
          }
        }
      };
      this.timer.scheduleAtFixedRate(task, autoSaveDelayForRecovery, autoSaveDelayForRecovery);
    }
  }

  /**
   * Clones the given <code>home</code> and saves it in automatic save executor.
   */
  private void cloneAndSaveHome(final Home home) {
    try {
      EventQueue.invokeAndWait(new Runnable() {
        public void run() {
          // Clone home in Event Dispatch Thread
          final Home autoSavedHome = home.clone();
          autoSaveForRecoveryExecutor.submit(new Runnable() {
            public void run() {
              try {
                // Save home clone in an other thread
                saveHome(home, autoSavedHome);
              } catch (RecorderException ex) {
                ex.printStackTrace();
              }
            }
          });
        }
      });
    } catch (InvocationTargetException ex) {
      throw new RuntimeException(ex);
    } catch (InterruptedException ex) {
      // Ignore saving in case of interruption
    }
  }

  /**
   * Saves the given <code>home</code> in recovery folder.
   */
  private void saveHome(Home home, Home autoSavedHome) throws RecorderException {
    if (this.application.getHomes().contains(home)) {
      File autoSavedHomeFile = this.autoSavedFiles.get(home);
      if (autoSavedHomeFile == null) {
        File recoveredFilesFolder = getRecoveryFolder();
        if (!recoveredFilesFolder.exists()) {
          recoveredFilesFolder.mkdirs();
        }
        // Find a unique file for home in recovered files sub folder
        if (autoSavedHome.getName() != null) {
          String homeFile = new File(autoSavedHome.getName()).getName();
          autoSavedHomeFile = new File(recoveredFilesFolder, homeFile + RECOVERED_FILE_EXTENSION);
          if (autoSavedHomeFile.exists()) {
            autoSavedHomeFile = new File(recoveredFilesFolder, 
                UUID.randomUUID() + "-" + homeFile + RECOVERED_FILE_EXTENSION);
          }
        } else {
          autoSavedHomeFile = new File(recoveredFilesFolder,
              UUID.randomUUID() + RECOVERED_FILE_EXTENSION);
        }
      }
      freeLockedFile(autoSavedHomeFile);        
      if (autoSavedHome.isModified()) {
        this.autoSavedFiles.put(home, autoSavedHomeFile);
        try {
          // Save home and lock the saved file to avoid possible auto recovery processes to read it 
          this.application.getHomeRecorder().writeHome(autoSavedHome, autoSavedHomeFile.getPath());
          
          FileOutputStream lockedOutputStream = null;
          try {
            lockedOutputStream = new FileOutputStream(autoSavedHomeFile, true);
            lockedOutputStream.getChannel().lock();
            lockedOutputStreams.put(autoSavedHomeFile, lockedOutputStream);
          } catch (OverlappingFileLockException ex) {
            // Don't try to race with other processes that acquired a lock on the file 
          } catch (IOException ex) {
            if (lockedOutputStream != null) {
              try {
                lockedOutputStream.close();
              } catch (IOException ex1) {
                // Forget it
              }
            }
            throw new RecorderException("Can't lock saved home", ex);            
          }
        } catch (InterruptedRecorderException ex) {
          // Forget exception that probably happen because of shutdown hook management
        } 
      } else {
        autoSavedHomeFile.delete();
        this.autoSavedFiles.remove(home);
      }
    }
    this.lastAutoSaveTime = Math.max(this.lastAutoSaveTime, System.currentTimeMillis());
  }

  /**
   * Frees the given <code>file</code> if it's locked.
   */
  private void freeLockedFile(File file) throws RecorderException {
    FileOutputStream lockedOutputStream = this.lockedOutputStreams.get(file);
    if (lockedOutputStream != null) {
      // Close stream and free its associated lock
      try {
        lockedOutputStream.close();
        this.lockedOutputStreams.remove(file);
      } catch (IOException ex) {
        throw new RecorderException("Can't close locked stream", ex);
      }
    }
  }

  /**
   * Returns the folder where recovered files are stored.
   */
  private File getRecoveryFolder() throws RecorderException {
    try {
      UserPreferences userPreferences = this.application.getUserPreferences();
      return new File(userPreferences instanceof FileUserPreferences
          ? ((FileUserPreferences)userPreferences).getApplicationFolder()
          : OperatingSystem.getDefaultApplicationFolder(), RECOVERY_SUB_FOLDER);
    } catch (IOException ex) {
      throw new RecorderException("Can't retrieve recovered files folder", ex);
    }
  }
}