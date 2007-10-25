/*
 * SweetHome3D.java 1 sept. 2006
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
package com.eteks.sweethome3d;

import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.jnlp.ServiceManagerStub;
import javax.jnlp.SingleInstanceListener;
import javax.jnlp.SingleInstanceService;
import javax.jnlp.UnavailableServiceException;
import javax.media.j3d.IllegalRenderingStateException;
import javax.media.j3d.RenderingError;
import javax.media.j3d.RenderingErrorListener;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.eteks.sweethome3d.io.FileUserPreferences;
import com.eteks.sweethome3d.io.HomeFileRecorder;
import com.eteks.sweethome3d.model.ContentManager;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeApplication;
import com.eteks.sweethome3d.model.HomeEvent;
import com.eteks.sweethome3d.model.HomeListener;
import com.eteks.sweethome3d.model.HomeRecorder;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.swing.Component3DManager;
import com.eteks.sweethome3d.swing.HomeController;

/**
 * Sweet Home 3D main class.
 * @author Emmanuel Puybaret
 */
public class SweetHome3D extends HomeApplication {
  private HomeRecorder       homeRecorder;
  private UserPreferences    userPreferences;
  private FileContentManager contentManager;
  private Map<Home, JFrame>  homeFrames;

  private SweetHome3D() {
    this.homeRecorder = new HomeFileRecorder();
    this.userPreferences = new FileUserPreferences();
    this.contentManager = new FileContentManager();
    this.homeFrames = new HashMap<Home, JFrame>();
  }

  /**
   * Returns a recorder able to write and read homes in files.
   */
  @Override
  public HomeRecorder getHomeRecorder() {
    return this.homeRecorder;
  }
  
  /**
   * Returns user preferences stored in resources and local file system.
   */
  @Override
  public UserPreferences getUserPreferences() {
    return this.userPreferences;
  }
  
  /**
   * Returns a content manager able to manage content locale files. 
   */
  @Override
  public ContentManager getContentManager() {
    return this.contentManager;
  }
  
  /**
   * Adds a given <code>home</code> to this application, 
   * in the Event Dispatch Thread.
   */
  @Override
  public void addHome(final Home home) {
    EventQueue.invokeLater(new Runnable() {
        public void run() {
          SweetHome3D.super.addHome(home);
        }
      });
  }
  
  /**
   * Returns the frame that displays a given <code>home</code>.
   */
  public JFrame getHomeFrame(Home home) {
    return this.homeFrames.get(home);
  }
  
  /**
   * Shows and brings to front <code>home</code> frame. 
   */
  private void showHomeFrame(Home home) {
    final JFrame homeFrame = getHomeFrame(home);
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        homeFrame.setVisible(true);
        homeFrame.setState(JFrame.NORMAL);
        homeFrame.toFront();
      }
    });
  }

  // Only one application may be created with main method or SingleInstanceService
  private static SweetHome3D application;

  /**
   * Sweet Home 3D entry point.
   * @param args may contain one .sh3d file to open, following a <code>-open</code> option.  
   */
  public static void main(String [] args) {
    // At first main call
    if (application == null) {
      initSystemProperties();
      
      // If Sweet Home 3D is launched from outside of Java Web Start
      if (ServiceManager.getServiceNames() == null) {
        // Try to call single instance server 
        if (StandaloneSingleInstanceService.callSingleInstanceServer(args)) {
          // If single instance server was successfully called, exit application 
          System.exit(0);
        } else {
          // Display splash screen
          new SplashScreenWindow();
          // Create JNLP services required by Sweet Home 3D 
          ServiceManager.setServiceManagerStub(new StandaloneServiceManager());
        }
      }      
      
      initLookAndFeel();
      application = createApplication();
    }

    if (args.length == 2 && args [0].equals("-open")) {
      // If requested home is already opened, show it
      for (Home home : application.getHomes()) {
        if (args [1].equals(home.getName())) {
          application.showHomeFrame(home);
          return;
        }
      }
      
      try {
        // Read home file in args [1] if args [0] == "-open"
        Home home = application.getHomeRecorder().readHome(args [1]);
        home.setName(args [1]); 
        application.addHome(home);
      } catch (RecorderException ex) {
        // Show an error message dialog if home couldn't be read
        ResourceBundle resource = ResourceBundle.getBundle(
            HomeController.class.getName());
        String message = String.format(resource.getString("openError"), args [1]);
        JOptionPane.showMessageDialog(null, message, "Sweet Home 3D", 
            JOptionPane.ERROR_MESSAGE);
      }
    } else if (application.getHomes().isEmpty()) {
      // Create a default home 
      Home home = new Home(application.getUserPreferences().getNewHomeWallHeight());
      application.addHome(home);
    } else {
      // If no Sweet Home 3D frame has focus, bring last created viewed frame to front 
      final List<Home> homes = application.getHomes();
      Home home = null;
      for (int i = homes.size() - 1; i >= 0; i--) {
        JFrame homeFrame = application.getHomeFrame(homes.get(i));
        if (homeFrame.isActive()
            || homeFrame.getState() != JFrame.ICONIFIED) {
          home = homes.get(i);
          break;
        }
      }
      // If no frame is visible and not iconified, take any displayable frame
      if (home == null) {
        for (int i = homes.size() - 1; i >= 0; i--) {
          JFrame homeFrame = application.getHomeFrame(homes.get(i));
          if (homeFrame.isDisplayable()) {
            home = homes.get(i);
            break;
          }
        }
      }
      
      application.showHomeFrame(home);
    }
  }

  /**
   * Sets various <code>System</code> properties.
   */
  private static void initSystemProperties() {
    // Enables Java 5 bug correction about dragging directly
    // a tree element without selecting it before :
    // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4521075
    System.setProperty("sun.swing.enableImprovedDragGesture", "true");
    // Change Mac OS X application menu name
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Sweet Home 3D");
    // Use Mac OS X screen menu bar for frames menu bar
    System.setProperty("apple.laf.useScreenMenuBar", "true");
  }

  /**
   * Sets application look anf feel.
   */
  private static void initLookAndFeel() {
    try {
      // Apply current system look and feel
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
      // Too bad keep current look and feel
    }
  }

  /**
   * Returns main application object. 
   */
  private static SweetHome3D createApplication() {
    SingleInstanceService service = null;
    final SingleInstanceListener singleInstanceListener = 
      new SingleInstanceListener() {
        public void newActivation(String [] args) {
          // Just call main with the arguments it should have received
          main(args);
        }
      };
    try {
      // Retrieve Java Web Start SingleInstanceService
      service = (SingleInstanceService)
          ServiceManager.lookup("javax.jnlp.SingleInstanceService");
      service.addSingleInstanceListener(singleInstanceListener);
    } catch (UnavailableServiceException ex) {
      // Just ignore SingleInstanceService if it's not available 
      // to let application work outside of Java Web Start
    } 
    
    // Make a final copy of service
    final SingleInstanceService singleInstanceService = service;
          
    // Create the application that manages homes 
    final SweetHome3D application = new SweetHome3D();
    // Add a listener that opens a frame when a home is added to application
    application.addHomeListener(new HomeListener() {
        private boolean firstApplicationHomeAdded;
        
        public void homeChanged(HomeEvent ev) {
          switch (ev.getType()) {
            case ADD :
              Home home = ev.getHome();
              try {
                HomeController controller = new HomeFrameController(home, application);
                if (!this.firstApplicationHomeAdded) {
                  application.addNewHomeCloseListener(home, controller);
                  this.firstApplicationHomeAdded = true;
                }          
                
                JFrame homeFrame = (JFrame)SwingUtilities.getRoot(controller.getView());
                application.homeFrames.put(home, homeFrame); 
              } catch (IllegalRenderingStateException ex) {
                ex.printStackTrace();
                // In case of a problem in Java 3D, simply exit with a message.
                application.exitAfter3DError();
              }
              break;
            case DELETE :
              application.homeFrames.remove(ev.getHome());
              
              // If application has no more home 
              if (application.getHomes().isEmpty()
                  && !System.getProperty("os.name").startsWith("Mac OS X")) {
                // If SingleInstanceService is available, remove the listener that was added on it
                if (singleInstanceService != null) {
                  singleInstanceService.removeSingleInstanceListener(singleInstanceListener);
                }
                // Exit (under Mac OS X, exit is managed by MacOSXConfiguration)
                System.exit(0);
              }
              break;
          }
        };
      });
    
    application.addComponent3DRenderingErrorListener();
    
    if (System.getProperty("os.name").startsWith("Mac OS X")) {
      // Bind to application menu  
      MacOSXConfiguration.bindToApplicationMenu(application);
    }

    return application;
  }
  
  /**
   * Adds a listener to new home to close it if an other one is opened.
   */ 
  private void addNewHomeCloseListener(final Home home, 
                                       final HomeController controller) {
    if (home.getName() == null) {
      final HomeListener newHomeListener = new HomeListener() {
          public void homeChanged(HomeEvent ev) {
            // Close new home for any named home added to application
            if (ev.getType() == HomeEvent.Type.ADD) { 
              if (ev.getHome().getName() != null
                  && home.getName() == null) {
                controller.close();
              }
              removeHomeListener(this);
            } else if (ev.getHome() == home
                       && ev.getType() == HomeEvent.Type.DELETE) {
              removeHomeListener(this);
            }
          }
        };
      addHomeListener(newHomeListener);
      // Disable this listener at first home change
      home.addPropertyChangeListener(Home.Property.MODIFIED, new PropertyChangeListener () {
          public void propertyChange(PropertyChangeEvent ev) {
            removeHomeListener(newHomeListener);
            home.removePropertyChangeListener(Home.Property.MODIFIED, this);
          }
        });
    }
  }
  
  /**
   * Sets the rendering error listener bound to Java 3D 
   * to avoid default System exit in case of error during 3D rendering. 
   */
  private void addComponent3DRenderingErrorListener() {
    // Instead of adding a RenderingErrorListener directly to VirtualUniverse, 
    // we add it through Canvas3DManager, because offscreen rendering needs to check 
    // rendering errors with its own RenderingErrorListener
    Component3DManager.getInstance().setRenderingErrorListener(new RenderingErrorListener() {
        public void errorOccurred(RenderingError error) {
          error.printVerbose();
          EventQueue.invokeLater(new Runnable() {
              public void run() {
                exitAfter3DError();
              }
            });
        }
      });
  }

  /**
   * Displays a message to user about a 3D error, saves modified homes 
   * and forces exit. 
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
            getHomeFrame(home).toFront();
            homeName = getContentManager().showSaveDialog(null, 
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
    ResourceBundle resource = ResourceBundle.getBundle(SweetHome3D.class.getName());
    String message = resource.getString("3DError.message");
    String title = resource.getString("3DError.title");
    JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Displays a dialog that let user choose whether he wants to save
   * modified homes after an error in 3D rendering system.
   * @return <code>true</code> if user confirmed to save.
   */
  private boolean confirmSaveAfter3DError() {
    ResourceBundle resource = ResourceBundle.getBundle(SweetHome3D.class.getName());
    String message = resource.getString("confirmSaveAfter3DError.message");
    String title = resource.getString("confirmSaveAfter3DError.title");
    String save = resource.getString("confirmSaveAfter3DError.save");
    String doNotSave = resource.getString("confirmSaveAfter3DError.doNotSave");
    
    return JOptionPane.showOptionDialog(null, message, title, 
        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
        null, new Object [] {save, doNotSave}, save) == JOptionPane.YES_OPTION;
  }

  /**
   * An AWT window displaying a splash screen image.
   * The window is disposed when an other AWT frame is created.
   */
  private static class SplashScreenWindow extends Window {
    private BufferedImage image;
    
    public SplashScreenWindow() {
      super(new Frame());
      try {
        this.image = ImageIO.read(SplashScreenWindow.class.getResource(
            "resources/splashScreen.jpg"));
        setSize(this.image.getWidth(), this.image.getHeight());
        setLocationRelativeTo(null);
        setVisible(true);
        
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
              try {
                while (isVisible()) {
                  Thread.sleep(500);
                  // If an other frame is created, dispose splash window
                  EventQueue.invokeLater(new Runnable() {
                    public void run() {
                      if (Frame.getFrames().length > 1) {
                        dispose();
                      }
                    }
                  });
                }
              } catch (InterruptedException ex) {
                EventQueue.invokeLater(new Runnable() {
                  public void run() {
                    dispose();
                  }
                });
              };
            }
          });
      } catch (IOException ex) {
        // Ignore splash screen
      }
    }
    
    @Override
    public void paint(Graphics g) {
      g.drawImage(this.image, 0, 0, this);
    }
  }
  
  /**
   * JNLP <code>ServiceManagerStub</code> implementation for standalone applications 
   * run out of Java Web Start. This service manager supports <code>BasicService</code> 
   * and <code>javax.jnlp.SingleInstanceService</code>.
   */
  private static class StandaloneServiceManager implements ServiceManagerStub {
    public Object lookup(final String name) throws UnavailableServiceException {
      if (name.equals("javax.jnlp.BasicService")) {
        // Create a basic service that uses Java SE 6 java.awt.Desktop class
        return new StandaloneBasicService();
      } else if (name.equals("javax.jnlp.SingleInstanceService")) {
        // Create a server that waits for further Sweet Home 3D launches
        return new StandaloneSingleInstanceService();
      } else {
        throw new UnavailableServiceException(name);
      }
    }
    
    public String[] getServiceNames() {
      return new String[]  {
          "javax.jnlp.BasicService",
          "javax.jnlp.SingleInstanceService"};
    }
  }    

  /**
   * <code>BasicService</code> that launches web browser either with Java SE 6
   * <code>java.awt.Desktop</code> class, or with the <code>open</code> command under Mac OS X.
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
          // For any exception, let's consider simply the showDocument method failed
        }
      } else if (System.getProperty("os.name").startsWith("Mac OS X")) {
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
          // Call Java SE 6 java.awt.Desktop isSupported(Desktop.Action.BROWSE) method by reflection to 
          // ensure Java SE 5 compatibility
          Class desktopClass = Class.forName("java.awt.Desktop");
          Object desktopInstance = desktopClass.getMethod("getDesktop").invoke(null);
          Class desktopActionClass = Class.forName("java.awt.Desktop$Action");
          Object desktopBrowseAction = desktopActionClass.getMethod("valueOf", String.class).
              invoke(null, "BROWSE");
          return (Boolean)desktopClass.getMethod("isSupported", desktopActionClass).
              invoke(desktopInstance, desktopBrowseAction);
        } catch (Exception ex) {
          // For any exception, let's consider simply the isSupported method failed
        }
      }
      // For other Java versions, let's support only Mac OS X
      return System.getProperty("os.name").startsWith("Mac OS X");
    }

    private boolean isJava6() {
      String javaVersion = System.getProperty("java.version");
      String [] javaVersionParts = javaVersion.split("\\.|_");
      if (javaVersionParts.length >= 1) {
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
   * A single instance service server that waits for further Sweet Home 3D launches.
   */
  private static class StandaloneSingleInstanceService implements SingleInstanceService {
    private static final String SINGLE_INSTANCE_PORT = "singleInstancePort";
    
    private List<SingleInstanceListener> singleInstanceListeners = new ArrayList<SingleInstanceListener>();
    
    public void addSingleInstanceListener(SingleInstanceListener l) {
      if (this.singleInstanceListeners.isEmpty()) {
        launchSingleInstanceServer();     
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
        Preferences preferences = Preferences.userNodeForPackage(SweetHome3D.class);
        preferences.putInt(SINGLE_INSTANCE_PORT, serverSocket.getLocalPort());
        preferences.sync();
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
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), "UTF-8"));
                String [] params = reader.readLine().split("\t");
                reader.close();
                socket.close();
                
                // Work on a copy of singleInstanceListeners to ensure a listener 
                // can modify safely listeners list
                SingleInstanceListener [] listeners = singleInstanceListeners.
                    toArray(new SingleInstanceListener [singleInstanceListeners.size()]);
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
        Preferences preferences = Preferences.userNodeForPackage(SweetHome3D.class);
        preferences.remove(SINGLE_INSTANCE_PORT);
        try {
          preferences.sync();
        } catch (BackingStoreException ex) {
          throw new RuntimeException(ex);
        }
      }
    }
    
    /**
     * Returns <code>true</code> if single instance server was successfully called.
     */
    public static boolean callSingleInstanceServer(String [] mainArgs) {
      Preferences preferences = Preferences.userNodeForPackage(SweetHome3D.class);
      int singleInstancePort = preferences.getInt(SINGLE_INSTANCE_PORT, -1);
      if (singleInstancePort != -1) {
        try {
          // Try to connect to single instance server
          Socket socket = new Socket("127.0.0.1", singleInstancePort);
          // Write main args
          BufferedWriter writer = new BufferedWriter(
              new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
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
      return false;
    }
  }
}
