/*
 * HomeFileRecorder.java 30 aout 2006
 *
 * Copyright (c) 2006 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeRecorder;
import com.eteks.sweethome3d.model.InterruptedRecorderException;
import com.eteks.sweethome3d.model.RecorderException;

/**
 * Recorder that stores homes in files with {@link DefaultHomeOutputStream} and
 * {@link DefaultHomeInputStream}.
 * @author Emmanuel Puybaret
 */
public class HomeFileRecorder implements HomeRecorder {
  private final int compressionLevel;

  /**
   * Creates a home recorder able to write and read homes in uncompressed files. 
   */
  public HomeFileRecorder() {
    this(0);
  }

  /**
   * Creates a home recorder able to write and read homes in files compressed 
   * at a level from 0 to 9. 
   * @param compressionLevel 0 (uncompressed) to 9 (compressed).
   */
  public HomeFileRecorder(int compressionLevel) {
    this.compressionLevel = compressionLevel;
  }

  /**
   * Writes home data.
   * @throws RecorderException if a problem occurred while writing home.
   */
  public void writeHome(Home home, String name) throws RecorderException {
    File homeFile = new File(name);
    File backupFile = null;    

    // Backup existing home file to a temporary file
    if (homeFile.exists()) {
      try {
        backupFile = File.createTempFile("backup", ".sh3d");
        copyFile(homeFile, backupFile);
      } catch (IOException ex) {
        if (backupFile != null) {
          backupFile.delete();
        }
        throw new RecorderException("Can't create temporary backup of " + homeFile, ex);
      }
    }
    
    DefaultHomeOutputStream homeOut = null;
    try {
      homeOut = new DefaultHomeOutputStream(new FileOutputStream(homeFile), 
          this.compressionLevel, false);
      // Write home with HomeOuputStream 
      // Overwriting it will ensure that home file rights are kept
      homeOut.writeHome(home); 
    } catch (InterruptedIOException ex) {
      try {
        if (homeOut != null) {
          homeOut.close();
        }
      } catch (IOException ex2) {          
      }
      
      if (backupFile != null) {
        try {
          // Prefer copy than rename to keep file rights
          copyFile(backupFile, homeFile);
        } catch (RecorderException ex2) {
          throw new InterruptedRecorderException("Can't restore backup of " + homeFile + " after interruption");
        }
      } else {
        homeFile.delete();
      }
      throw new InterruptedRecorderException("Save " + name + " interrupted");
    } catch (FileNotFoundException ex) {
      throw new RecorderException("Can't save file " + name, ex);
    } catch (IOException ex) {
      try {
        if (homeOut != null) {
          homeOut.close();
        }
      } catch (IOException ex2) {          
      }
      
      if (backupFile != null) {
        try {
          // Prefer copy than rename to keep file rights
          copyFile(backupFile, homeFile);
        } catch (RecorderException ex2) {
          // Last chance : delete home file and rename backup file to home file
          if (!homeFile.delete()
              || !backupFile.renameTo(homeFile)) {
            throw new RecorderException("Can't save file " + name + " and restore backup", ex);
          }
        }
      } else {
        homeFile.delete();
      }
      throw new RecorderException("Can't save file " + name, ex);
    } finally {
      try {
        if (homeOut != null) {
          homeOut.close();
        }
      } catch (IOException ex) {
        if (backupFile != null) {
          try {
            // Prefer copy than rename to keep file rights
            copyFile(backupFile, homeFile);
          } catch (RecorderException ex2) {
            // Last chance : delete home file and rename backup file to home file
            if (!homeFile.delete()
                || !backupFile.renameTo(homeFile)) {
              throw new RecorderException("Can't close file " + name + " and restore backup", ex);
            }
          }
        }
        throw new RecorderException("Can't close home " + name, ex);
      } finally {
        if (backupFile != null) {
          backupFile.delete();
        }
      }
      
      if (backupFile != null) {
        backupFile.delete();
      }
    }
  }

  /**
   * Copy <code>file1</code> to <code>file2</code>.
   */
  private void copyFile(File file1, File file2) throws RecorderException {
    byte [] buffer = new byte [8192];
    InputStream in = null;
    OutputStream out = null;
    try {
      in = new FileInputStream(file1);          
      out = new FileOutputStream(file2);
      int size; 
      while ((size = in.read(buffer)) != -1) {
        out.write(buffer, 0, size);
      }
    } catch (IOException ex) { 
      throw new RecorderException("Can't copy file " + file1 + " to " + file2);
    } finally {
      try {
        if (out != null) {          
          out.close();
        }
      } catch (IOException ex) {
        throw new RecorderException("Can't close file " + file2, ex);
      }
      try {
        if (in != null) {          
          in.close();
        }
      } catch (IOException ex) {
        // Forget exception
      }
    }
  }

  /**
   * Returns a home instance read from its file <code>name</code>.
   * @throws RecorderException if a problem occurred while reading home, 
   *   or if file <code>name</code> doesn't exist.
   */
  public Home readHome(String name) throws RecorderException {
    DefaultHomeInputStream in = null;
    try {
      // Open a stream on file
      in = new DefaultHomeInputStream(new FileInputStream(name));
      // Read home with HomeInputStream
      Home home = in.readHome();
      return home;
    } catch (InterruptedIOException ex) {
      throw new InterruptedRecorderException("Read " + name + " interrupted");
    } catch (IOException ex) {
      throw new RecorderException("Can't read home from " + name, ex);
    } catch (ClassNotFoundException ex) {
      throw new RecorderException("Missing classes to read home from " + name, ex);
    } finally {
      try {
        if (in != null) {
          in.close();
        }
      } catch (IOException ex) {
        throw new RecorderException("Can't close file " + name, ex);
      }
    }
  }

  /**
   * Returns <code>true</code> if the file <code>name</code> exists.
   */
  public boolean exists(String name) throws RecorderException {
    return new File(name).exists();
  }
}
