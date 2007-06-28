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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;

import javax.swing.ActionMap;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;
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
  private static Application    application = new Application();
  private static JFrame         defaultFrame;
  private static HomeController currentController;

  static {
    // Add a listener on Mac OS X application that will call
    // controller methods of the active frame
    application.addApplicationListener(new ApplicationAdapter() {
      @Override
      public void handleQuit(ApplicationEvent ev) {
        currentController.exit();
        EventQueue.invokeLater(new Runnable() {
            public void run() {
              // If default frame is active, it means there's no more open window
              if (defaultFrame != null && defaultFrame.isActive()) {
                System.exit(0);
              }
            }
          });
      }
      
      @Override
      public void handleAbout(ApplicationEvent ev) {
        currentController.about();
        ev.setHandled(true);
      }

      @Override
      public void handlePreferences(ApplicationEvent ev) {
        currentController.editPreferences();
      }

      @Override
      public void handleOpenFile(ApplicationEvent ev) {
        // handleOpenFile is called when user opens a document
        // associated with a Java Web Start application
        // Just call main with -open file arguments as JNLP specifies 
        SweetHome3D.main(new String [] {"-open", ev.getFilename()});
      }
    });
    application.setEnabledAboutMenu(true);
    application.setEnabledPreferencesMenu(true);

    // Create a default undecorated frame out of sight 
    // to attach the default application menu bar to it
    defaultFrame = new JFrame();
    defaultFrame.setLocation(-10, 0);
    defaultFrame.setUndecorated(true);
    defaultFrame.setVisible(true);
  }

  /**
   * Binds the home <code>controller</code> methods of a <code>frame</code>
   * to Mac OS X application menu.
   */
  public static void bindControllerToApplicationMenu(final JFrame frame, 
                                                     final HomeController controller) {
    frame.addWindowListener(new WindowAdapter () {
        @Override
        public void windowActivated(WindowEvent ev) {
          currentController = controller;
        }
        
        @Override
        public void windowClosed(WindowEvent ev) {
          if (defaultFrame.getJMenuBar() == null) {
            // Create the default application menu bar 
            JMenuBar menuBar = new JMenuBar();
            JMenu fileMenu = new JMenu(new ResourceAction(
                ResourceBundle.getBundle(HomePane.class.getName()), "FILE_MENU", true));
            menuBar.add(fileMenu);
            ActionMap homePaneActionMap = controller.getView().getActionMap();
            fileMenu.add(new ResourceAction.MenuAction(homePaneActionMap.get(HomePane.ActionType.NEW_HOME)));
            fileMenu.add(new ResourceAction.MenuAction(homePaneActionMap.get(HomePane.ActionType.OPEN)));
            
            defaultFrame.setJMenuBar(menuBar);
          }
        }
      });
  }  
}
