/*
 * SweetHome3D.java 1 sept. 2006
 * 
 * Copyright (c) 2006 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights
 * Reserved.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.eteks.sweethome3d;

import java.awt.EventQueue;
import java.awt.KeyboardFocusManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.jnlp.ServiceManagerStub;
import javax.jnlp.SingleInstanceListener;
import javax.jnlp.SingleInstanceService;
import javax.jnlp.UnavailableServiceException;
import javax.media.j3d.IllegalRenderingStateException;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.eteks.sweethome3d.io.FileUserPreferences;
import com.eteks.sweethome3d.io.HomeFileRecorder;
import com.eteks.sweethome3d.j3d.Component3DManager;
import com.eteks.sweethome3d.model.CollectionEvent;
import com.eteks.sweethome3d.model.CollectionListener;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeApplication;
import com.eteks.sweethome3d.model.HomeRecorder;
import com.eteks.sweethome3d.model.InterruptedRecorderException;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.UserPreferences.Property;
import com.eteks.sweethome3d.plugin.PluginManager;
import com.eteks.sweethome3d.swing.FileContentManager;
import com.eteks.sweethome3d.swing.SwingTools;
import com.eteks.sweethome3d.swing.SwingViewFactory;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.ContentManager;
import com.eteks.sweethome3d.viewcontroller.HomeController;
import com.eteks.sweethome3d.viewcontroller.View;
import com.eteks.sweethome3d.viewcontroller.ViewFactory;

/**
 * Sweet Home 3D main class. Sweet Home 3D accepts the parameter
 * <code>-open</code> followed by the path of a Sweet Home 3D file.<br> 
 * Users may also define the optional following System properties to alter program behavior:
 * 
 * <ul><li><code>com.eteks.sweethome3d.applicationFolders</code> defines the folder(s) where private files 
 * of Sweet Home 3D are stored. Private files include SH3F furniture library files stored in a subfolder named
 * <code>furniture</code>, SH3T textures library files stored in a subfolder named <code>textures</code>, 
 * SH3L language library files stored in a subfolder named <code>languages</code>, SH3P application plug-in 
 * files stored in a subfolder named <code>plugins</code> and SH3D files automatically created for recovery 
 * purpose stored in a subfolder named <code>recovery</code>.<br>
 * If this property describes more than one folder, they should be separated by a semicolon (;) under Windows
 * or by a colon (:) under Mac OS X and Unix systems. The first folder listed in this property is used
 * as the folder where will be stored recovered SH3D files and SH3F, SH3T, SH3L, SH3P files imported by the user.
 * Thus the user should have write access rights on this first folder otherwise he won't be able to import 
 * SH3F, SH3T, SH3L or SH3P files, and auto recovered SH3D files won't be managed. If this folder or
 * one of its <code>furniture</code>, <code>textures</code>, <code>languages</code>, <code>plugins</code>,
 * <code>recovery</code> subfolders don't exist, Sweet Home 3D will create it when needed.<br>
 * The other folders are used as resources where SH3F, SH3T, SH3L, SH3P files will be searched 
 * in their respective code>furniture</code>, <code>textures</code>, <code>languages</code>, <code>plugins</code> 
 * subfolders. Any of the cited folders may be an absolute path or a relative path to the folder
 * from which the program was launched.</li>
 *       
 * <li><code>com.eteks.sweethome3d.preferencesFolder</code> defines the folder where preferences
 * files (<code>preferences.xml</code> and the files depending on it) are stored. The user should have
 * write access rights on this folder otherwise the program won't be able to save his preferences
 * and the files he imported in furniture and textures catalogs. This folder may be the same as the
 * folder cited in <code>com.eteks.sweethome3d.applicationFolders</code> property.</li>
 * 
 * <li><code>com.eteks.sweethome3d.j3d.checkOffScreenSupport</code> should be set to <code>false</code> 
 * when editing preferences, printing, creating a photo or creating a video always lead to a crash of Sweet Home 3D. 
 * This means offscreen 3D images isn't supported by your video driver and Sweet Home 3D doesn't even succeed
 * to test this support. Setting this System property to <code>false</code> disables this test.</li>
 * 
 * <li><code>com.eteks.sweethome3d.j3d.additionalLoaderClasses</code> defines additional Java 3D 
 * {@linkplain com.sun.j3d.loaders.Loader loader} classes that Sweet Home 3D will use to read 3D models content
 * at formats not supported by default in Sweet Home 3D.<br>
 * The classes cited in this property must be available in the classpath and if more than one class is
 * cited, they should be separated by a colon or a space.</li></ul>
 * 
 * <p>The value of a System property can be set with the -D 
 * <a href="http://download.oracle.com/javase/6/docs/technotes/tools/windows/java.html">java</a> option.
 * @author Emmanuel Puybaret
 */
public class SweetHome3D extends HomeApplication {
  private static final String     PREFERENCES_FOLDER             = "com.eteks.sweethome3d.preferencesFolder";
  private static final String     APPLICATION_FOLDERS            = "com.eteks.sweethome3d.applicationFolders";
  private static final String     APPLICATION_PLUGINS_SUB_FOLDER = "plugins";

  private HomeRecorder            homeRecorder;
  private HomeRecorder            compressedHomeRecorder;
  private UserPreferences         userPreferences;
  private ContentManager          contentManager;
  private ViewFactory             viewFactory;
  private PluginManager           pluginManager;
  private boolean                 pluginManagerInitialized;
  private AutoSaveManager         autoSaveManager;
  private final Map<Home, JFrame> homeFrames;

  /**
   * Creates a home application instance. Recorders, user preferences, content
   * manager, view factory and plug-in manager handled by this application are
   * lazily instantiated to let subclasses override their creation.
   */
  protected SweetHome3D() {
    this.homeFrames = new HashMap<Home, JFrame>();
  }

  /**
   * Returns a recorder able to write and read homes in files.
   */
  @Override
  public HomeRecorder getHomeRecorder() {
    // Initialize homeRecorder lazily
    if (this.homeRecorder == null) {
      this.homeRecorder = new HomeFileRecorder();
    }
    return this.homeRecorder;
  }

  @Override
  public HomeRecorder getHomeRecorder(HomeRecorder.Type type) {
    if (type == HomeRecorder.Type.COMPRESSED) {
      // Initialize compressedHomeRecorder lazily
      if (this.compressedHomeRecorder == null) {
        this.compressedHomeRecorder = new HomeFileRecorder(9);
      }
      return this.compressedHomeRecorder;
    } else {
      return super.getHomeRecorder(type);
    }
  }

  /**
   * Returns user preferences stored in resources and local file system.
   */
  @Override
  public UserPreferences getUserPreferences() {
    // Initialize userPreferences lazily
    if (this.userPreferences == null) {
      // Retrieve preferences and application folders
      String preferencesFolderProperty = System.getProperty(PREFERENCES_FOLDER, null);
      String applicationFoldersProperty = System.getProperty(APPLICATION_FOLDERS, null);
      File preferencesFolder = preferencesFolderProperty != null
          ? new File(preferencesFolderProperty)
          : null;
      File [] applicationFolders;
      if (applicationFoldersProperty != null) {
        String [] applicationFoldersProperties = applicationFoldersProperty.split(File.pathSeparator);
        applicationFolders = new File [applicationFoldersProperties.length];
        for (int i = 0; i < applicationFolders.length; i++) {
          applicationFolders [i] = new File(applicationFoldersProperties [i]);
        }
      } else {
        applicationFolders = null;
      }
      this.userPreferences = new FileUserPreferences(preferencesFolder, applicationFolders);
    }
    return this.userPreferences;
  }

  /**
   * Returns a content manager able to handle files.
   */
  protected ContentManager getContentManager() {
    if (this.contentManager == null) {
      this.contentManager = new FileContentManager(getUserPreferences());
    }
    return this.contentManager;
  }

  /**
   * Returns a Swing view factory.
   */
  protected ViewFactory getViewFactory() {
    if (this.viewFactory == null) {
      this.viewFactory = new SwingViewFactory();
    }
    return this.viewFactory;
  }

  /**
   * Returns the plugin manager of this application.
   */
  protected PluginManager getPluginManager() {
    if (!this.pluginManagerInitialized) {
      try {
        UserPreferences userPreferences = getUserPreferences();
        if (userPreferences instanceof FileUserPreferences) {
          File [] applicationPluginsFolders = ((FileUserPreferences) userPreferences)
              .getApplicationSubfolders(APPLICATION_PLUGINS_SUB_FOLDER);
          // Create the plug-in manager that will search plug-in files in
          // plugins folders
          this.pluginManager = new PluginManager(applicationPluginsFolders);
        }
      } catch (IOException ex) {
      }
      this.pluginManagerInitialized = true;
    }
    return this.pluginManager;
  }

  /**
   * Returns the name of this application read from resources.
   */
  @Override
  public String getName() {
    return getUserPreferences().getLocalizedString(SweetHome3D.class, "applicationName");
  }

  /**
   * Returns information about the version of this application.
   */
  public String getVersion() {
    String applicationVersion = getUserPreferences().getLocalizedString(SweetHome3D.class, "applicationVersion");
    String versionInformation = System.getProperty("com.eteks.sweethome3d.deploymentInformation");
    if (versionInformation != null) {
      applicationVersion += " " + versionInformation;
    }
    return applicationVersion;
  }

  /**
   * Returns the frame that displays a given <code>home</code>.
   */
  JFrame getHomeFrame(Home home) {
    return this.homeFrames.get(home);
  }

  /**
   * Shows and brings to front <code>home</code> frame.
   */
  private void showHomeFrame(Home home) {
    final JFrame homeFrame = getHomeFrame(home);
    homeFrame.setVisible(true);
    homeFrame.setState(JFrame.NORMAL);
    homeFrame.toFront();
  }

  /**
   * Sweet Home 3D entry point.
   * @param args may contain one .sh3d, .sh3f or .sh3p file to open or install,
   *          following a <code>-open</code> option.
   */
  public static void main(final String [] args) {
    new SweetHome3D().init(args);
  }

  /**
   * Inits application instance.
   */
  protected void init(final String [] args) {
    initSystemProperties();

    // If Sweet Home 3D is launched from outside of Java Web Start
    if (ServiceManager.getServiceNames() == null) {
      // Try to call single instance server
      if (StandaloneSingleInstanceService.callSingleInstanceServer(args, getClass())) {
        // If single instance server was successfully called, exit application
        System.exit(0);
      } else {
        // Display splash screen
        SwingTools.showSplashScreenWindow(SweetHome3D.class.getResource("resources/splashScreen.jpg"));
        // Create JNLP services required by Sweet Home 3D
        ServiceManager.setServiceManagerStub(new StandaloneServiceManager(getClass()));
      }
    }

    SingleInstanceService service = null;
    final SingleInstanceListener singleInstanceListener = new SingleInstanceListener() {
      public void newActivation(final String [] args) {
        // Call run with the arguments it should have received
        EventQueue.invokeLater(new Runnable() {
          public void run() {
            SweetHome3D.this.run(args);
          }
        });
      }
    };
    try {
      // Retrieve Java Web Start SingleInstanceService
      service = (SingleInstanceService) ServiceManager.lookup("javax.jnlp.SingleInstanceService");
      service.addSingleInstanceListener(singleInstanceListener);
    } catch (UnavailableServiceException ex) {
      // Just ignore SingleInstanceService if it's not available
      // to let application work outside of Java Web Start
    }

    // Make a final copy of service
    final SingleInstanceService singleInstanceService = service;

    // Add a listener that opens a frame when a home is added to application
    addHomesListener(new CollectionListener<Home>() {
      private boolean firstApplicationHomeAdded;

      public void collectionChanged(CollectionEvent<Home> ev) {
        switch (ev.getType()) {
          case ADD:
            Home home = ev.getItem();
            try {
              HomeFrameController controller = createHomeFrameController(home);
              controller.displayView();
              if (!this.firstApplicationHomeAdded) {
                addNewHomeCloseListener(home, controller.getHomeController());
                this.firstApplicationHomeAdded = true;
              }

              JFrame homeFrame = (JFrame) SwingUtilities.getRoot((JComponent) controller.getView());
              homeFrames.put(home, homeFrame);
            } catch (IllegalRenderingStateException ex) {
              ex.printStackTrace();
              // In case of a problem in Java 3D, simply exit with a message.
              exitAfter3DError();
            }
            break;
          case DELETE:
            homeFrames.remove(ev.getItem());

            // If application has no more home
            if (getHomes().isEmpty() && !OperatingSystem.isMacOSX()) {
              // If SingleInstanceService is available, remove the listener that
              // was added on it
              if (singleInstanceService != null) {
                singleInstanceService.removeSingleInstanceListener(singleInstanceListener);
              }
              // Exit once current events are managed (under Mac OS X, exit is managed by MacOSXConfiguration)
              EventQueue.invokeLater(new Runnable() {
                  public void run() {
                    System.exit(0);
                  }
                });
            }
            break;
        }
      };
    });

    addComponent3DRenderingErrorObserver();

    if (OperatingSystem.isMacOSX()) {
      // Bind to application menu
      MacOSXConfiguration.bindToApplicationMenu(this);
    }

    // Init look and feel afterwards to ensure that Swing takes into account
    // default locale change
    getUserPreferences();
    initLookAndFeel();
    try {
      this.autoSaveManager = new AutoSaveManager(this, getContentManager());
    } catch (RecorderException ex) {
      // Too bad we can't retrieve homes to recover
      ex.printStackTrace();
    }

    // Run everything else in Event Dispatch Thread
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        SweetHome3D.this.run(args);
      }
    });
  }

  /**
   * Returns a new instance of a home frame controller after <code>home</code>
   * was created.
   */
  protected HomeFrameController createHomeFrameController(Home home) {
    return new HomeFrameController(home, this, getViewFactory(), getContentManager(), getPluginManager());
  }

  /**
   * Sets various <code>System</code> properties.
   */
  private void initSystemProperties() {
    if (OperatingSystem.isMacOSX()) {
      // Change Mac OS X application menu name
      String classPackage = SweetHome3D.class.getName();
      classPackage = classPackage.substring(0, classPackage.lastIndexOf("."));
      ResourceBundle resource = ResourceBundle.getBundle(classPackage + "." + "package");
      String applicationName = resource.getString("SweetHome3D.applicationName");
      System.setProperty("com.apple.mrj.application.apple.menu.about.name", applicationName);
      // Use Mac OS X screen menu bar for frames menu bar
      System.setProperty("apple.laf.useScreenMenuBar", "true");
      // Force the use of Quartz under Mac OS X for better Java 2D rendering
      // performance
      System.setProperty("apple.awt.graphics.UseQuartz", "true");
    }
  }

  /**
   * Sets application look and feel.
   */
  private void initLookAndFeel() {
    try {
      // Apply current system look and feel
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      // Change default titled borders under Mac OS X 10.5
      if (OperatingSystem.isMacOSXLeopardOrSuperior()) {
        UIManager.put("TitledBorder.border", UIManager.getBorder("TitledBorder.aquaVariant"));
      }
      SwingTools.updateSwingResourceLanguage();
    } catch (Exception ex) {
      // Too bad keep current look and feel
    }
  }

  /**
   * Adds a listener to new home to close it if an other one is opened.
   */
  private void addNewHomeCloseListener(final Home home, final HomeController controller) {
    if (home.getName() == null) {
      final CollectionListener<Home> newHomeListener = new CollectionListener<Home>() {
        public void collectionChanged(CollectionEvent<Home> ev) {
          // Close new home for any named home added to application
          if (ev.getType() == CollectionEvent.Type.ADD) {
            if (ev.getItem().getName() != null 
                && home.getName() == null
                && !home.isRecovered()) {
              controller.close();
            }
            removeHomesListener(this);
          } else if (ev.getItem() == home && ev.getType() == CollectionEvent.Type.DELETE) {
            removeHomesListener(this);
          }
        }
      };
      addHomesListener(newHomeListener);
      // Disable this listener at first home change
      home.addPropertyChangeListener(Home.Property.MODIFIED, new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          removeHomesListener(newHomeListener);
          home.removePropertyChangeListener(Home.Property.MODIFIED, this);
        }
      });
    }
  }

  /**
   * Sets the rendering error listener bound to Java 3D to avoid default System
   * exit in case of error during 3D rendering.
   */
  private void addComponent3DRenderingErrorObserver() {
    // Add a RenderingErrorObserver to Canvas3DManager, because offscreen
    // rendering needs to check rendering errors with its own
    // RenderingErrorListener
    Component3DManager.getInstance().setRenderingErrorObserver(new Component3DManager.RenderingErrorObserver() {
      public void errorOccured(int errorCode, String errorMessage) {
        System.err.print("Error in Java 3D : " + errorCode + " " + errorMessage);
        EventQueue.invokeLater(new Runnable() {
          public void run() {
            exitAfter3DError();
          }
        });
      }
    });
  }

  /**
   * Displays a message to user about a 3D error, saves modified homes and
   * forces exit.
   */
  private void exitAfter3DError() {
    // Check if there are modified homes
    boolean modifiedHomes = false;
    for (Home home : getHomes()) {
      if (home.isModified()) {
        modifiedHomes = true;
        break;
      }
    }

    if (!modifiedHomes) {
      // Show 3D error message
      show3DError();
    } else if (confirmSaveAfter3DError()) {
      // Delete all homes after saving modified ones
      for (Home home : getHomes()) {
        if (home.isModified()) {
          String homeName = home.getName();
          if (homeName == null) {
            JFrame homeFrame = getHomeFrame(home);
            homeFrame.toFront();
            homeName = contentManager.showSaveDialog((View) homeFrame.getRootPane(), null,
                ContentManager.ContentType.SWEET_HOME_3D, null);
          }
          if (homeName != null) {
            try {
              // Write home with application recorder
              getHomeRecorder().writeHome(home, homeName);
            } catch (RecorderException ex) {
              // As this is an emergency exit, don't report error
              ex.printStackTrace();
            }
          }
          deleteHome(home);
        }
      }
    }
    // Close homes
    for (Home home : getHomes()) {
      deleteHome(home);
    }
    // Force exit if program didn't exit by itself
    System.exit(0);
  }

  /**
   * Displays in a 3D error message.
   */
  private void show3DError() {
    UserPreferences userPreferences = getUserPreferences();
    String message = userPreferences.getLocalizedString(SweetHome3D.class, "3DError.message");
    String title = userPreferences.getLocalizedString(SweetHome3D.class, "3DError.title");
    JOptionPane.showMessageDialog(KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow(), message,
        title, JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Displays a dialog that let user choose whether he wants to save modified
   * homes after an error in 3D rendering system.
   * @return <code>true</code> if user confirmed to save.
   */
  private boolean confirmSaveAfter3DError() {
    UserPreferences userPreferences = getUserPreferences();
    String message = userPreferences.getLocalizedString(SweetHome3D.class, "confirmSaveAfter3DError.message");
    String title = userPreferences.getLocalizedString(SweetHome3D.class, "confirmSaveAfter3DError.title");
    String save = userPreferences.getLocalizedString(SweetHome3D.class, "confirmSaveAfter3DError.save");
    String doNotSave = userPreferences.getLocalizedString(SweetHome3D.class, "confirmSaveAfter3DError.doNotSave");

    return JOptionPane.showOptionDialog(KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow(),
        message, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object [] {save, doNotSave},
        save) == JOptionPane.YES_OPTION;
  }

  /**
   * Runs application once initialized.
   */
  void run(String [] args) {
    if (args.length == 2 && args [0].equals("-open")) {
      // If requested home is already opened, show it
      for (Home home : getHomes()) {
        if (args [1].equals(home.getName())) {
          showHomeFrame(home);
          return;
        }
      }
      
      if (getContentManager().isAcceptable(args [1], ContentManager.ContentType.SWEET_HOME_3D)) {
        // Add a listener to application to recover homes once the one in parameter is open
        addHomesListener(new CollectionListener<Home>() {
            public void collectionChanged(CollectionEvent<Home> ev) {
              if (ev.getType() == CollectionEvent.Type.ADD) {
                removeHomesListener(this);
                autoSaveManager.openRecoveredHomes();                
              }
            }
          });
        // Read home file in args [1] if args [0] == "-open" with a dummy
        // controller
        createHomeFrameController(createHome()).getHomeController().open(args [1]);
      } else if (getContentManager().isAcceptable(args [1], ContentManager.ContentType.LANGUAGE_LIBRARY)) {
        run(new String [0]);
        final String languageLibraryName = args [1];
        EventQueue.invokeLater(new Runnable() {
          public void run() {
            // Import language library with a dummy controller
            createHomeFrameController(createHome()).getHomeController().importLanguageLibrary(languageLibraryName);
          }
        });
      } else if (getContentManager().isAcceptable(args [1], ContentManager.ContentType.FURNITURE_LIBRARY)) {
        run(new String [0]);
        final String furnitureLibraryName = args [1];
        EventQueue.invokeLater(new Runnable() {
          public void run() {
            // Import furniture library with a dummy controller
            createHomeFrameController(createHome()).getHomeController().importFurnitureLibrary(furnitureLibraryName);
          }
        });
      } else if (getContentManager().isAcceptable(args [1], ContentManager.ContentType.TEXTURES_LIBRARY)) {
        run(new String [0]);
        final String texturesLibraryName = args [1];
        EventQueue.invokeLater(new Runnable() {
          public void run() {
            // Import textures library with a dummy controller
            createHomeFrameController(createHome()).getHomeController().importTexturesLibrary(texturesLibraryName);
          }
        });
      } else if (getContentManager().isAcceptable(args [1], ContentManager.ContentType.PLUGIN)) {
        run(new String [0]);
        final String pluginName = args [1];
        EventQueue.invokeLater(new Runnable() {
          public void run() {
            // Import plug-in with a dummy controller
            createHomeFrameController(createHome()).getHomeController().importPlugin(pluginName);
          }
        });
      }
    } else if (getHomes().isEmpty()) {
      this.autoSaveManager.openRecoveredHomes();
      if (getHomes().isEmpty()) {
        // Add a new home to application
        addHome(createHome());
      }
    } else {
      // If no Sweet Home 3D frame has focus, bring last created viewed frame to
      // front
      final List<Home> homes = getHomes();
      Home home = null;
      for (int i = homes.size() - 1; i >= 0; i--) {
        JFrame homeFrame = getHomeFrame(homes.get(i));
        if (homeFrame.isActive() || homeFrame.getState() != JFrame.ICONIFIED) {
          home = homes.get(i);
          break;
        }
      }
      // If no frame is visible and not iconified, take any displayable frame
      if (home == null) {
        for (int i = homes.size() - 1; i >= 0; i--) {
          JFrame homeFrame = getHomeFrame(homes.get(i));
          if (homeFrame.isDisplayable()) {
            home = homes.get(i);
            break;
          }
        }
      }

      showHomeFrame(home);
    }
  }

  /**
   * Manager able to automatically save open homes in recovered files folder with a timer.
   */
  private static class AutoSaveManager {
    private static final String RECOVERED_FILES_SUB_FOLDER        = "recovery";
    private static final String RECOVERED_FILES_DEFAULT_EXTENSION = ".recovered";

    private final HomeApplication application;
    private final ContentManager  contentManager;
    private final List<Home>      recoveredHomes = new ArrayList<Home>();
    private final Map<Home, File> autoSavedFiles = Collections.synchronizedMap(new HashMap<Home, File>());
    private final ExecutorService autoSaveForRecoveryExecutor;
    private Timer                 timer;
    private long                  lastAutoSaveTime;

    private AutoSaveManager(HomeApplication application, 
                           ContentManager contentManager) throws RecorderException {
      this.application = application;
      this.contentManager = contentManager;
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
              File homeFile = autoSavedFiles.get(ev.getItem());
              if (homeFile != null) {
                homeFile.delete();
                autoSavedFiles.remove(ev.getItem());
              }
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
      File [] recoveredFiles = getRecoveredFilesFolder().listFiles(new FileFilter() {
          public boolean accept(File file) {
            return file.isFile()
                && file.getName().endsWith(contentManager instanceof FileContentManager
                      ? ((FileContentManager)contentManager)
                          .getDefaultFileExtension(ContentManager.ContentType.SWEET_HOME_3D)
                      : RECOVERED_FILES_DEFAULT_EXTENSION);
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

    /**
     * Opens recovered homes.
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
                  cloneAndAutoSaveHome(home);
                }
              }
            }
          }
        };
        this.timer.scheduleAtFixedRate(task, autoSaveDelayForRecovery, autoSaveDelayForRecovery);
      }
    }

    private void cloneAndAutoSaveHome(final Home home) {
      try {
        EventQueue.invokeAndWait(new Runnable() {
          public void run() {
            // Clone home in Event Dispatch Thread
            final Home autoSavedHome = home.clone();
            autoSaveForRecoveryExecutor.submit(new Runnable() {
              public void run() {
                try {
                  // Save home clone in an other thread
                  autoSaveHome(home, autoSavedHome);
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

    private void autoSaveHome(Home home, Home autoSavedHome) throws RecorderException {
      if (this.application.getHomes().contains(home)) {
        File autoSavedHomeFile = this.autoSavedFiles.get(home);
        if (autoSavedHomeFile == null) {
          File recoveredFilesFolder = getRecoveredFilesFolder();
          if (!recoveredFilesFolder.exists()) {
            recoveredFilesFolder.mkdirs();
          }
          // Find a unique file for home in recovered files sub folder
          if (autoSavedHome.getName() != null) {
            String homeFile = new File(autoSavedHome.getName()).getName();
            autoSavedHomeFile = new File(recoveredFilesFolder, homeFile);
            if (autoSavedHomeFile.exists()) {
              autoSavedHomeFile = new File(recoveredFilesFolder, UUID.randomUUID() + "-" + homeFile);
            }
          } else {
            autoSavedHomeFile = new File(recoveredFilesFolder,
                UUID.randomUUID()
                    + (this.contentManager instanceof FileContentManager
                        ? ((FileContentManager)this.contentManager)
                            .getDefaultFileExtension(ContentManager.ContentType.SWEET_HOME_3D)
                        : RECOVERED_FILES_DEFAULT_EXTENSION));
          }
          this.autoSavedFiles.put(home, autoSavedHomeFile);
        }
        if (autoSavedHome.isModified()) {
          try {
            this.application.getHomeRecorder().writeHome(autoSavedHome, autoSavedHomeFile.getPath());
          } catch (InterruptedRecorderException ex) {
            // Forget exception that probably happen because of shutdown hook
            // management
          }
        } else {
          autoSavedHomeFile.delete();
        }
      }
      this.lastAutoSaveTime = Math.max(this.lastAutoSaveTime, System.currentTimeMillis());
    }

    private File getRecoveredFilesFolder() throws RecorderException {
      try {
        UserPreferences userPreferences = this.application.getUserPreferences();
        return new File(userPreferences instanceof FileUserPreferences
            ? ((FileUserPreferences) userPreferences).getApplicationFolder()
            : OperatingSystem.getDefaultApplicationFolder(), RECOVERED_FILES_SUB_FOLDER);
      } catch (IOException ex) {
        throw new RecorderException("Can't retrieve recovered files folder", ex);
      }
    }
  }
  
  /**
   * JNLP <code>ServiceManagerStub</code> implementation for standalone
   * applications run out of Java Web Start. This service manager supports
   * <code>BasicService</code> and <code>javax.jnlp.SingleInstanceService</code>.
   * .
   */
  private static class StandaloneServiceManager implements ServiceManagerStub {
    private final Class<?> mainClass;

    public StandaloneServiceManager(Class<?> mainClass) {
      this.mainClass = mainClass;
    }

    public Object lookup(final String name) throws UnavailableServiceException {
      if (name.equals("javax.jnlp.BasicService")) {
        // Create a basic service that uses Java SE 6 java.awt.Desktop class
        return new StandaloneBasicService();
      } else if (name.equals("javax.jnlp.SingleInstanceService")) {
        // Create a server that waits for further Sweet Home 3D launches
        return new StandaloneSingleInstanceService(this.mainClass);
      } else {
        throw new UnavailableServiceException(name);
      }
    }

    public String [] getServiceNames() {
      return new String [] {"javax.jnlp.BasicService", "javax.jnlp.SingleInstanceService"};
    }
  }

  /**
   * <code>BasicService</code> that launches web browser either with Java SE 6
   * <code>java.awt.Desktop</code> class, or with the <code>open</code> command
   * under Mac OS X.
   */
  private static class StandaloneBasicService implements BasicService {
    public boolean showDocument(URL url) {
      if (isJava6()) {
        try {
          // Call Java SE 6 java.awt.Desktop browse method by reflection to
          // ensure Java SE 5 compatibility
          Class desktopClass = Class.forName("java.awt.Desktop");
          Object desktopInstance = desktopClass.getMethod("getDesktop").invoke(null);
          desktopClass.getMethod("browse", URI.class).invoke(desktopInstance, url.toURI());
          return true;
        } catch (Exception ex) {
          // For any exception, let's consider simply the showDocument method
          // failed
        }
      } else if (OperatingSystem.isMacOSX()) {
        try {
          Runtime.getRuntime().exec(new String [] {"open", url.toString()});
          return true;
        } catch (IOException ex) {
        }
      }
      return false;
    }

    public URL getCodeBase() {
      // Return a default URL matching the <code>resources</code> sub directory.
      return StandaloneServiceManager.class.getResource("resources");
    }

    public boolean isOffline() {
      return false;
    }

    public boolean isWebBrowserSupported() {
      if (isJava6()) {
        try {
          // Call Java SE 6 java.awt.Desktop isSupported(Desktop.Action.BROWSE)
          // method by reflection to
          // ensure Java SE 5 compatibility
          Class desktopClass = Class.forName("java.awt.Desktop");
          Object desktopInstance = desktopClass.getMethod("getDesktop").invoke(null);
          Class desktopActionClass = Class.forName("java.awt.Desktop$Action");
          Object desktopBrowseAction = desktopActionClass.getMethod("valueOf", String.class).invoke(null, "BROWSE");
          return (Boolean) desktopClass.getMethod("isSupported", desktopActionClass).invoke(desktopInstance,
              desktopBrowseAction);
        } catch (Exception ex) {
          // For any exception, let's consider simply the isSupported method
          // failed
        }
      }
      // For other Java versions, let's support only Mac OS X
      return OperatingSystem.isMacOSX();
    }

    private boolean isJava6() {
      String javaVersion = System.getProperty("java.version");
      String [] javaVersionParts = javaVersion.split("\\.|_");
      if (javaVersionParts.length >= 2) {
        try {
          // Return true for Java SE 6 and superior
          if (Integer.parseInt(javaVersionParts [1]) >= 6) {
            return true;
          }
        } catch (NumberFormatException ex) {
        }
      }
      return false;
    }
  }

  /**
   * A single instance service server that waits for further Sweet Home 3D
   * launches.
   */
  private static class StandaloneSingleInstanceService implements SingleInstanceService {
    private static final String                SINGLE_INSTANCE_PORT    = "singleInstancePort";

    private final Class<?>                     mainClass;
    private final List<SingleInstanceListener> singleInstanceListeners = new ArrayList<SingleInstanceListener>();

    public StandaloneSingleInstanceService(Class<?> mainClass) {
      this.mainClass = mainClass;
    }

    public void addSingleInstanceListener(SingleInstanceListener l) {
      if (this.singleInstanceListeners.isEmpty()) {
        if (!OperatingSystem.isMacOSX()) {
          // Launching a server is useless under Mac OS X because further
          // launches will
          // be notified by com.apple.eawt.ApplicationListener added to
          // application
          // in MacOSXConfiguration class
          launchSingleInstanceServer();
        }
      }
      this.singleInstanceListeners.add(l);
    }

    /**
     * Launches single instance server.
     */
    private void launchSingleInstanceServer() {
      final ServerSocket serverSocket;
      try {
        // Launch a server that waits for other Sweet Home 3D launches
        serverSocket = new ServerSocket(0, 0, InetAddress.getByName("127.0.0.1"));
        // Share server port in preferences
        Preferences preferences = Preferences.userNodeForPackage(this.mainClass);
        preferences.putInt(SINGLE_INSTANCE_PORT, serverSocket.getLocalPort());
        preferences.flush();
      } catch (IOException ex) {
        // Ignore exception, Sweet Home 3D will work with multiple instances
        return;
      } catch (BackingStoreException ex) {
        // Ignore exception, Sweet Home 3D will work with multiple instances
        return;
      }

      Executors.newSingleThreadExecutor().execute(new Runnable() {
        public void run() {
          try {
            while (true) {
              // Wait client calls
              Socket socket = serverSocket.accept();
              // Read client params
              BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
              String [] params = reader.readLine().split("\t");
              reader.close();
              socket.close();

              // Work on a copy of singleInstanceListeners to ensure a listener
              // can modify safely listeners list
              SingleInstanceListener [] listeners = singleInstanceListeners
                  .toArray(new SingleInstanceListener [singleInstanceListeners.size()]);
              // Call listeners with received params
              for (SingleInstanceListener listener : listeners) {
                listener.newActivation(params);
              }
            }
          } catch (IOException ex) {
            // In case of problem, relaunch server
            launchSingleInstanceServer();
          }
        }
      });
    }

    public void removeSingleInstanceListener(SingleInstanceListener l) {
      this.singleInstanceListeners.remove(l);
      if (this.singleInstanceListeners.isEmpty()) {
        Preferences preferences = Preferences.userNodeForPackage(this.mainClass);
        preferences.remove(SINGLE_INSTANCE_PORT);
        try {
          preferences.flush();
        } catch (BackingStoreException ex) {
          throw new RuntimeException(ex);
        }
      }
    }

    /**
     * Returns <code>true</code> if single instance server was successfully
     * called.
     */
    public static boolean callSingleInstanceServer(String [] mainArgs, Class<?> mainClass) {
      if (!OperatingSystem.isMacOSX()) {
        // No server under Mac OS X, multiple application launches are managed
        // by com.apple.eawt.ApplicationListener in MacOSXConfiguration class
        Preferences preferences = Preferences.userNodeForPackage(mainClass);
        int singleInstancePort = preferences.getInt(SINGLE_INSTANCE_PORT, -1);
        if (singleInstancePort != -1) {
          try {
            // Try to connect to single instance server
            Socket socket = new Socket("127.0.0.1", singleInstancePort);
            // Write main args
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            for (String arg : mainArgs) {
              writer.write(arg);
              writer.write("\t");
            }
            writer.write("\n");
            writer.close();
            socket.close();
            return true;
          } catch (IOException ex) {
            // Return false
          }
        }
      }
      return false;
    }
  }
}
