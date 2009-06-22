/*
 * MacOSXConfiguraton.java 6 sept. 2006
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
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
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
import com.eteks.sweethome3d.viewcontroller.ContentManager;
import com.eteks.sweethome3d.viewcontroller.HomeController;
import com.eteks.sweethome3d.viewcontroller.ViewFactory;

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
  public static void bindToApplicationMenu(final SweetHome3D homeApplication,
                                           ContentManager contentManager,
                                           ViewFactory viewFactory) {
    // Create a default controller for an empty home and disable unrelated actions
    final HomeController defaultController = new HomeController(
        new Home(), homeApplication, viewFactory, contentManager, null);
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
    
    final JFrame defaultFrame = new JFrame();
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        // Create a default undecorated frame out of sight 
        // and attach the application menu bar of empty view to it
        defaultFrame.setLocation(-10, 0);
        defaultFrame.setUndecorated(true);
        defaultFrame.setVisible(true);
        defaultFrame.setJMenuBar(defaultHomeView.getJMenuBar());
        defaultFrame.setContentPane(defaultHomeView);
        addWindowMenuToFrame(defaultFrame, homeApplication, true);
      }
    });

    Application application = Application.getApplication();
    // Add a listener on Mac OS X application that will call
    // controller methods of the active frame
    application.addApplicationListener(new ApplicationAdapter() {      
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
          if (frame.isActive()) {
            activeFrame = frame;
            break;
          }
        }
        // Move default frame to center to display dialogs at center
        defaultFrame.setLocationRelativeTo(null);
        
        runnable.run();
        
        // Activate previous frame again
        if (activeFrame != null) {
          activeFrame.toFront();
        }
        // Move default frame out of user view
        defaultFrame.setLocation(-10, 0);
      }

      @Override
      public void handleOpenFile(ApplicationEvent ev) {
        // handleOpenFile is called when user opens a document
        // associated with a Java Web Start application
        // Just call main with -open file arguments as JNLP specifies 
        SweetHome3D.main(new String [] {"-open", ev.getFilename()});
      }
      
      @Override
      public void handleReOpenApplication(ApplicationEvent ev) {
        // handleReOpenApplication is called when user launches 
        // the application when it's already open
        SweetHome3D.main(new String [0]);
      }
    });
    application.setEnabledAboutMenu(true);
    application.setEnabledPreferencesMenu(true);
    
    homeApplication.addHomesListener(new CollectionListener<Home>() {
      public void collectionChanged(CollectionEvent<Home> ev) {
        if (ev.getType() == CollectionEvent.Type.ADD) {
          // Add Mac OS X Window menu on new homes
          MacOSXConfiguration.addWindowMenuToFrame(
              homeApplication.getHomeFrame(ev.getItem()), homeApplication, false);
        }
      };
    });
    
    // Set application icon if program wasn't launch from bundle
    if (!"true".equalsIgnoreCase(System.getProperty("sweethome3d.bundle", "false"))) {
      try {
        // Call setDockIconImage by reflection
        Method setDockIconImageMethod = Application.class.getMethod("setDockIconImage", Image.class);
        String iconPath = homeApplication.getUserPreferences().getLocalizedString(HomePane.class, "about.icon");
        Image icon = ImageIO.read(HomePane.class.getResource(iconPath));
        setDockIconImageMethod.invoke(application, icon);
      } catch (NoSuchMethodException ex) {
        // Ignore icon change if setDockIconImage isn't available
      } catch (InvocationTargetException ex) {
      } catch (IllegalAccessException ex) {
      } catch (IOException ex) {
      }
    }
  }
  
  /**
   * Adds Mac OS X standard Window menu to frame. 
   * @param application 
   */
  private static void addWindowMenuToFrame(final JFrame frame, 
                                           final SweetHome3D application,
                                           boolean defaultFrame) {
    UserPreferences preferences = application.getUserPreferences();
    JMenuBar menuBar = frame.getJMenuBar();
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
              for (Home home : application.getHomes()) {
                JFrame applicationFrame = application.getHomeFrame(home);
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
          for (Home home : application.getHomes()) {
            final JFrame applicationFrame = application.getHomeFrame(home);
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
