/*
 * MacOSXConfiguraton.java 6 sept. 2006
 *
 * Sweet Home 3D, Copyright (c) 2006 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;
import javax.media.j3d.Canvas3D;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.UIManager;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;
import com.eteks.sweethome3d.model.CollectionEvent;
import com.eteks.sweethome3d.model.CollectionListener;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.swing.HomePane;
import com.eteks.sweethome3d.swing.ResourceAction;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.HomeController;
import com.sun.j3d.exp.swing.JCanvas3D;

/**
 * Configuration class that accesses to Mac OS X specifics.
 * Do not invoke methods of this class without checking first if 
 * <code>os.name</code> System property is <code>Mac OS X</code>.
 * This class requires some classes of <code>com.apple.eawt</code> package  
 * to compile.
 * @author Emmanuel Puybaret
 */
class MacOSXConfiguration {
  /**
   * Binds <code>homeApplication</code> to Mac OS X application menu.
   */
  public static void bindToApplicationMenu(final SweetHome3D homeApplication) {
    final Application macosxApplication = Application.getApplication();
    // Create a default controller for an empty home and disable unrelated actions
    final HomeController defaultController = 
        homeApplication.createHomeFrameController(homeApplication.createHome()).getHomeController();
    final HomePane defaultHomeView = (HomePane)defaultController.getView();
    for (HomePane.ActionType action : HomePane.ActionType.values()) {
      switch (action) {
        case ABOUT :
        case NEW_HOME :
        case OPEN :
        case DELETE_RECENT_HOMES :
        case HELP :
          break;
        default :
          defaultHomeView.setEnabled(action, false);
      }
    }

    final JMenuBar defaultMenuBar = defaultHomeView.getJMenuBar();
    JFrame frame = null;
    try {
      if (OperatingSystem.isJavaVersionGreaterOrEqual("1.7")) {
        // Application#setDefaultMenuBar does nothing under Java 7
        frame = createDummyFrameWithDefaultMenuBar(homeApplication, defaultHomeView, defaultMenuBar);
      } else if (UIManager.getLookAndFeel().getClass().getName().equals(UIManager.getSystemLookAndFeelClassName())) {
        macosxApplication.setDefaultMenuBar(defaultMenuBar);
        addWindowMenu(null, defaultMenuBar, homeApplication, true);
      }
    } catch (NoSuchMethodError ex) {
      // Create default frame if setDefaultMenuBar isn't available
      frame = createDummyFrameWithDefaultMenuBar(homeApplication, defaultHomeView, defaultMenuBar);
    } 

    final JFrame defaultFrame = frame;
    // Add a listener to Mac OS X application that will call
    // controller methods of the active frame
    macosxApplication.addApplicationListener(new ApplicationAdapter() {      
      @Override
      public void handleQuit(ApplicationEvent ev) { 
        handleAction(new Runnable() {
            public void run() {
              defaultController.exit();
            }
          });
        if (homeApplication.getHomes().isEmpty()) {
          System.exit(0);
        }
      }
      
      @Override
      public void handleAbout(ApplicationEvent ev) {
        handleAction(new Runnable() {
            public void run() {
              defaultController.about();
            }
          });
        ev.setHandled(true);
      }

      @Override
      public void handlePreferences(ApplicationEvent ev) {
        handleAction(new Runnable() {
            public void run() {
              defaultController.editPreferences();
            }
          });
      }
      
      private void handleAction(Runnable runnable) {
        Frame activeFrame = null;
        for (Frame frame : Frame.getFrames()) {
          if (frame != defaultFrame && frame.isActive()) {
            activeFrame = frame;
            break;
          }
        }
        if (defaultFrame != null) {
          // Move default frame to center to display dialogs at center
          defaultFrame.setLocationRelativeTo(null);
          defaultFrame.toFront();
          defaultFrame.setAlwaysOnTop(true);
          // Disable About and Preferences menu items 
          macosxApplication.setEnabledAboutMenu(false);
          macosxApplication.setEnabledPreferencesMenu(false);
        }
        
        runnable.run();
        
        // Activate previous frame again
        if (activeFrame != null) {
          activeFrame.toFront();
        }
        if (defaultFrame != null) {
          defaultFrame.setAlwaysOnTop(false);
          // Move default frame out of user view
          defaultFrame.toBack();
          defaultFrame.setLocation(-10, 0);
          // Enable About and Preferences menu items again
          macosxApplication.setEnabledAboutMenu(true);
          macosxApplication.setEnabledPreferencesMenu(true);
        }
      }

      @Override
      public void handleOpenFile(ApplicationEvent ev) {
        // handleOpenFile is called when user opens a document
        // associated with a Java Web Start application
        // Just call main with -open file arguments as JNLP specifies 
        homeApplication.start(new String [] {"-open", ev.getFilename()});
      }
      
      @Override
      public void handleReOpenApplication(ApplicationEvent ev) {
        // handleReOpenApplication is called when user launches 
        // the application when it's already open
        homeApplication.start(new String [0]);
      }
    });
    macosxApplication.setEnabledAboutMenu(true);
    macosxApplication.setEnabledPreferencesMenu(true);
    
    homeApplication.addHomesListener(new CollectionListener<Home>() {
      public void collectionChanged(CollectionEvent<Home> ev) {
        if (ev.getType() == CollectionEvent.Type.ADD) {
          final JFrame homeFrame = homeApplication.getHomeFrame(ev.getItem());
          if (!Boolean.getBoolean("com.eteks.sweethome3d.no3D")) {
            // To avoid a possible freeze of the program when the user requests a window enlargement 
            // while the frame canvas 3D is instantiated, forbid window to be resized
            homeFrame.setResizable(false);
            Executors.newSingleThreadExecutor().submit(new Runnable() {                
                public void run() {
                  try {
                    final AtomicBoolean canvas3D = new AtomicBoolean();
                    do {
                      Thread.sleep(50);
                      EventQueue.invokeAndWait(new Runnable() {
                          public void run() {
                            canvas3D.set(homeFrame.isShowing()
                                && isParentOfCanvas3D(homeFrame, Canvas3D.class, JCanvas3D.class));
                          }
                        });
                    } while (!canvas3D.get());                  
                  } catch (InterruptedException ex) {
                  } catch (InvocationTargetException ex) {
                    ex.printStackTrace();
                  } finally {
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                          homeFrame.setResizable(true);
                        }
                      });
                  }
                }
                
                private boolean isParentOfCanvas3D(Container parent, Class<?> ... canvas3DClasses) {
                  // Search 3D canvas among children and child windows in case the 3D view was detached
                  for (int i = 0; i < parent.getComponentCount(); i++) {
                    Component child = parent.getComponent(i);
                    for (Class<?> canvas3DClass : canvas3DClasses) {
                      if (canvas3DClass.isInstance(child)
                          || child instanceof Container
                            && isParentOfCanvas3D((Container)child, canvas3DClasses)) {
                        return true;
                      }
                    }
                  }
                  if (parent instanceof Window) {
                    for (Window window : ((Window)parent).getOwnedWindows()) {
                      if (isParentOfCanvas3D(window, canvas3DClasses)) {
                        return true;
                      }
                    }
                  } 
                  return false;
                }
              });
          }
          // Add Mac OS X Window menu on new homes
          MacOSXConfiguration.addWindowMenu(
              homeFrame, homeFrame.getJMenuBar(), homeApplication, false);
          
          if (OperatingSystem.isJavaVersionGreaterOrEqual("1.7")) {
            // Help system to understand it should display the main menu of one of the remaining windows when a window is closed
            homeFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent ev) {
                  List<Home> homes = homeApplication.getHomes();
                  defaultFrame.setVisible(false);
                  defaultFrame.setVisible(true);
                  if (homes.size() > 0) {
                    homeApplication.getHomeFrame(homes.get(0)).toFront();
                    defaultFrame.setVisible(false);
                  }
                }
              });
          }
        }
      };
    });
    
    // Set application icon if program wasn't launch from bundle
    if (!Boolean.getBoolean("sweethome3d.bundle")) {
      try {
        String iconPath = homeApplication.getUserPreferences().getLocalizedString(HomePane.class, "about.icon");
        Image icon = ImageIO.read(HomePane.class.getResource(iconPath));
        macosxApplication.setDockIconImage(icon);
      } catch (NoSuchMethodError ex) {
        // Ignore icon change if setDockIconImage isn't available
      } catch (IOException ex) {
      }
    }
  }

  /**
   * Returns a dummy frame used to display the default menu bar.
   */
  private static JFrame createDummyFrameWithDefaultMenuBar(final SweetHome3D homeApplication,
                                                           final HomePane defaultHomeView, 
                                                           final JMenuBar defaultMenuBar) {
    final JFrame frame = new JFrame();
    EventQueue.invokeLater(new Runnable() {
        public void run() {
          // Create a default undecorated frame out of sight 
          // and attach the application menu bar of empty view to it
          frame.setLocation(-10, 0);
          frame.setUndecorated(true);
          frame.setBackground(new Color(0, 0, 0, 0));
          frame.setVisible(true);
          frame.setJMenuBar(defaultMenuBar);
          frame.setContentPane(defaultHomeView);
          addWindowMenu(frame, defaultMenuBar, homeApplication, true);
        }
      });
    return frame;
  }
  
  /**
   * Adds Mac OS X standard Window menu to frame. 
   */
  private static void addWindowMenu(final JFrame frame, 
                                    final JMenuBar menuBar, 
                                    final SweetHome3D homeApplication,
                                    boolean defaultFrame) {
    UserPreferences preferences = homeApplication.getUserPreferences();
    final JMenu windowMenu = new JMenu(
        new ResourceAction(preferences, MacOSXConfiguration.class, "WINDOW_MENU", true));
    // Add Window menu before Help menu
    menuBar.add(windowMenu, menuBar.getComponentCount() - 1);
    windowMenu.add(new JMenuItem(
        new ResourceAction(preferences, MacOSXConfiguration.class, "MINIMIZE", !defaultFrame) {
            @Override
            public void actionPerformed(ActionEvent ev) {
              frame.setState(JFrame.ICONIFIED);
            }
          }));
    windowMenu.add(new JMenuItem(
        new ResourceAction(preferences, MacOSXConfiguration.class, "ZOOM", !defaultFrame) {
            @Override
            public void actionPerformed(ActionEvent ev) {
              if ((frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) != 0) {
                frame.setExtendedState(frame.getExtendedState() & ~JFrame.MAXIMIZED_BOTH);
              } else {
                frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
              }
            }
          }));
    windowMenu.addSeparator();
    windowMenu.add(new JMenuItem(
        new ResourceAction(preferences, MacOSXConfiguration.class, "BRING_ALL_TO_FRONT", !defaultFrame) {
            @Override
            public void actionPerformed(ActionEvent ev) {
              // Avoid blinking while bringing other windows to front
              frame.setAlwaysOnTop(true);
              for (Home home : homeApplication.getHomes()) {
                JFrame applicationFrame = homeApplication.getHomeFrame(home);
                if (applicationFrame != frame
                    && applicationFrame.getState() != JFrame.ICONIFIED) {
                  applicationFrame.setFocusableWindowState(false);
                  applicationFrame.toFront();
                  applicationFrame.setFocusableWindowState(true);
                }
              }
              frame.setAlwaysOnTop(false);
            }
          }));
    
    windowMenu.addMenuListener(new MenuListener() {
        public void menuSelected(MenuEvent ev) {
          boolean firstMenuItem = true;
          // Fill menu dynamically with a menu item for the frame of each application home
          for (Home home : homeApplication.getHomes()) {
            final JFrame applicationFrame = homeApplication.getHomeFrame(home);
            JCheckBoxMenuItem windowMenuItem = new JCheckBoxMenuItem(
                new AbstractAction(applicationFrame.getTitle()) {
                    public void actionPerformed(ActionEvent ev) {
                      applicationFrame.toFront();
                    }
                  });
              
            if (frame == applicationFrame) {
              windowMenuItem.setSelected(true);
            }
            if (firstMenuItem) {
              windowMenu.addSeparator();
              firstMenuItem = false;
            }
            windowMenu.add(windowMenuItem);
          }
        }

        public void menuDeselected(MenuEvent ev) {
          // Remove dynamically filled part of menu
          for (int i = windowMenu.getMenuComponentCount() - 1; i >= 4; i--) {
            windowMenu.remove(i);
          }
        }

        public void menuCanceled(MenuEvent ev) {
          menuDeselected(ev);
        }
      });
  }
}
