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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.jnlp.ServiceManager;
import javax.jnlp.SingleInstanceListener;
import javax.jnlp.SingleInstanceService;
import javax.jnlp.UnavailableServiceException;
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
  
  // Only one application may be created with main method or SingleInstanceService
  private static SweetHome3D application;

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
    } else if (application.getHomes().isEmpty()) {
      // Create a default home 
      Home home = new Home(application.getUserPreferences().getNewHomeWallHeight());
      application.addHome(home);
    } else {
      // If no Sweet Home 3D frame has focus, bring last created viewed frame to front 
      final List<Home> homes = application.getHomes();
      JFrame frame = null;
      for (int i = homes.size() - 1; i >= 0; i--) {
        JFrame homeFrame = application.getHomeFrame(homes.get(i));
        if (homeFrame.isActive()
            || homeFrame.getState() != JFrame.ICONIFIED) {
          frame = homeFrame;
          break;
        }
      }
      // If no frame is visible and not iconified, take any displayable frame
      if (frame == null) {
        for (int i = homes.size() - 1; i >= 0; i--) {
          JFrame homeFrame = application.getHomeFrame(homes.get(i));
          if (homeFrame.isDisplayable()) {
            frame = homeFrame;
            break;
          }
        }
      }
      
      final JFrame shownFrame = frame;
      EventQueue.invokeLater(new Runnable() {
          public void run() {
            shownFrame.setVisible(true);
            shownFrame.setState(JFrame.NORMAL);
            shownFrame.toFront();
          }
        });      
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
              HomeController controller = new HomeFrameController(home, application);
              if (!this.firstApplicationHomeAdded) {
                application.addNewHomeCloseListener(home, controller);
                this.firstApplicationHomeAdded = true;
              }          
              
              JFrame homeFrame = (JFrame)SwingUtilities.getRoot(controller.getView());
              application.homeFrames.put(home, homeFrame);
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
}
