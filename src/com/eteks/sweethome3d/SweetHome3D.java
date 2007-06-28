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

import java.util.ResourceBundle;

import javax.jnlp.ServiceManager;
import javax.jnlp.SingleInstanceListener;
import javax.jnlp.SingleInstanceService;
import javax.jnlp.UnavailableServiceException;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.eteks.sweethome3d.io.FileUserPreferences;
import com.eteks.sweethome3d.io.HomeFileRecorder;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeApplication;
import com.eteks.sweethome3d.model.HomeEvent;
import com.eteks.sweethome3d.model.HomeListener;
import com.eteks.sweethome3d.model.HomeRecorder;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.swing.HomeController;

/**
 * Sweet Home 3D main class.
 * @author Emmanuel Puybaret
 */
public class SweetHome3D extends HomeApplication {
  private HomeRecorder    homeRecorder;
  private UserPreferences userPreferences;

  private SweetHome3D() {
    this.homeRecorder = new HomeFileRecorder();
    this.userPreferences = new FileUserPreferences();
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
  
  // Only one application may be created with main method or SingleInstanceService
  private static HomeApplication application;

  /**
   * Sweet Home 3D entry point.
   * @param args may contain one .sh3d file to open, following a <code>-open</code> option.  
   */
  public static void main(String [] args) {
    // At first main call
    if (application == null) {
      initLookAndFeel();
      application = createApplication();
    }

    if (args.length == 2 && args [0].equals("-open")) {
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
    } else  {
      // Create a default home 
      Home home = new Home(application.getUserPreferences().getNewHomeWallHeight());
      application.addHome(home);
    }
  }

  /**
   * Sets application look anf feel and various <code>System</code> properties.
   */
  private static void initLookAndFeel() {
    // Enables Java 5 bug correction about dragging directly
    // a tree element without selecting it before :
    // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4521075
    System.setProperty("sun.swing.enableImprovedDragGesture", "true");
    // Change Mac OS X application menu name
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Sweet Home 3D");
    // Use Mac OS X screen menu bar for frames menu bar
    System.setProperty("apple.laf.useScreenMenuBar", "true");
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
  private static HomeApplication createApplication() {
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
    final HomeApplication application = new SweetHome3D();
    // Add a listener that opens a frame when a home is added to application
    application.addHomeListener(new HomeListener() {
        public void homeChanged(HomeEvent ev) {
          switch (ev.getType()) {
            case ADD :
              Home home = ev.getHome();
              new HomeFrameController(home, application);
              break;
            case DELETE :
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
    return application;
  }
}
