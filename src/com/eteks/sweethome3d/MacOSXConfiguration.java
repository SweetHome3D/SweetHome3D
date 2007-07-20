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

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

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
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeEvent;
import com.eteks.sweethome3d.model.HomeListener;
import com.eteks.sweethome3d.swing.HomeController;
import com.eteks.sweethome3d.swing.HomePane;
import com.eteks.sweethome3d.swing.ResourceAction;

/**
 * Configuration class that accesses to Mac OS X specifics.
 * Do not invoke methods of this class without checking first if 
 * <code>os.name</code> System property is <code>Mac OS X</code>.
 * This class requires some classes of <code>com.apple.eawt</code> package  
 * to compile.
 * @author Emmanuel Puybaret
 */
class MacOSXConfiguration {
  private static final ResourceBundle RESOURCE = 
      ResourceBundle.getBundle(MacOSXConfiguration.class.getName());
  
  /**
   * Binds <code>homeApplication</code> to Mac OS X application menu.
   */
  public static void bindToApplicationMenu(final SweetHome3D homeApplication) {
    // Create a default controller for an empty home and disable unrelated actions
    final HomeController defaultController = new HomeController(new Home(), homeApplication);
    HomePane defaultHomeView = (HomePane)defaultController.getView();
    defaultHomeView.setEnabled(HomePane.ActionType.CLOSE, false);
    defaultHomeView.setEnabled(HomePane.ActionType.SAVE, false);
    defaultHomeView.setEnabled(HomePane.ActionType.SAVE_AS, false);
    defaultHomeView.setEnabled(HomePane.ActionType.IMPORT_FURNITURE, false);
    defaultHomeView.setEnabled(HomePane.ActionType.SORT_HOME_FURNITURE_BY_NAME, false);
    defaultHomeView.setEnabled(HomePane.ActionType.SORT_HOME_FURNITURE_BY_WIDTH, false);
    defaultHomeView.setEnabled(HomePane.ActionType.SORT_HOME_FURNITURE_BY_HEIGHT, false);
    defaultHomeView.setEnabled(HomePane.ActionType.SORT_HOME_FURNITURE_BY_DEPTH, false);
    defaultHomeView.setEnabled(HomePane.ActionType.SORT_HOME_FURNITURE_BY_COLOR, false);
    defaultHomeView.setEnabled(HomePane.ActionType.SORT_HOME_FURNITURE_BY_MOVABILITY, false);
    defaultHomeView.setEnabled(HomePane.ActionType.SORT_HOME_FURNITURE_BY_TYPE, false);
    defaultHomeView.setEnabled(HomePane.ActionType.SORT_HOME_FURNITURE_BY_VISIBILITY, false);
    defaultHomeView.setEnabled(HomePane.ActionType.SELECT, false);
    defaultHomeView.setEnabled(HomePane.ActionType.CREATE_WALLS, false);
    defaultHomeView.setEnabled(HomePane.ActionType.IMPORT_BACKGROUND_IMAGE, false);
    defaultHomeView.setEnabled(HomePane.ActionType.ZOOM_IN, false);
    defaultHomeView.setEnabled(HomePane.ActionType.ZOOM_OUT, false);
    defaultHomeView.setEnabled(HomePane.ActionType.VIEW_FROM_TOP, false);
    defaultHomeView.setEnabled(HomePane.ActionType.VIEW_FROM_OBSERVER, false);
    defaultHomeView.setEnabled(HomePane.ActionType.MODIFY_3D_ATTRIBUTES, false);
    
    // Create a default undecorated frame out of sight 
    // and attach the application menu bar of empty view to it
    JFrame defaultFrame = new JFrame();
    defaultFrame.setLocation(-10, 0);
    defaultFrame.setUndecorated(true);
    defaultFrame.setVisible(true);
    defaultFrame.setJMenuBar(defaultHomeView.getJMenuBar());
    addWindowMenuToFrame(defaultFrame, homeApplication, true);

    Application application = new Application();
    // Add a listener on Mac OS X application that will call
    // controller methods of the active frame
    application.addApplicationListener(new ApplicationAdapter() {
      @Override
      public void handleQuit(ApplicationEvent ev) {
        defaultController.exit();
        if (homeApplication.getHomes().isEmpty()) {
          System.exit(0);
        }
      }
      
      @Override
      public void handleAbout(ApplicationEvent ev) {
        defaultController.about();
        ev.setHandled(true);
      }

      @Override
      public void handlePreferences(ApplicationEvent ev) {
        defaultController.editPreferences();
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
    
    homeApplication.addHomeListener(new HomeListener() {
      public void homeChanged(HomeEvent ev) {
        if (ev.getType() == HomeEvent.Type.ADD) {
          // Add Mac OS X Window menu on new homes
          MacOSXConfiguration.addWindowMenuToFrame(
              homeApplication.getHomeFrame(ev.getHome()), homeApplication, false);
        }
      };
    });
  }
  
  /**
   * Adds Mac OS X standard Window menu to frame. 
   * @param application 
   */
  private static void addWindowMenuToFrame(final JFrame frame, 
                                           final SweetHome3D application,
                                           boolean defaultFrame) {
    final JMenu windowMenu = new JMenu(new ResourceAction(RESOURCE, "WINDOW", true));
    // Add Window menu before Help menu
    JMenuBar menuBar = frame.getJMenuBar();
    menuBar.add(windowMenu, menuBar.getComponentCount() - 1);
    windowMenu.add(new JMenuItem(new ResourceAction(RESOURCE, "MINIMIZE", !defaultFrame) {
        public void actionPerformed(ActionEvent ev) {
          frame.setState(JFrame.ICONIFIED);
        }
      }));
    windowMenu.add(new JMenuItem(new ResourceAction(RESOURCE, "ZOOM", !defaultFrame) {
        public void actionPerformed(ActionEvent ev) {
          if ((frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) != 0) {
            frame.setExtendedState(frame.getExtendedState() & ~JFrame.MAXIMIZED_BOTH);
          } else {
            frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
          }
        }
      }));
    windowMenu.addSeparator();
    windowMenu.add(new JMenuItem(new ResourceAction(RESOURCE, "BRING_ALL_TO_FRONT", !defaultFrame) {
        public void actionPerformed(ActionEvent ev) {
          // Avoid blincking while bringing other windows to front
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
          // Remove dimaically filled part of menu
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
