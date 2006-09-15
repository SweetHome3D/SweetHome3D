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

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.UIManager;

import com.eteks.sweethome3d.io.DefaultUserPreferences;
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
    this.userPreferences = new DefaultUserPreferences();
    // Add a listener that opens a frame when a home is added to application
    addHomeListener(new HomeListener() {
        public void homeChanged(HomeEvent ev) {
          if (ev.getType() == HomeEvent.Type.ADD) {
            Home home = ev.getHome();
            HomeController controller = 
                new HomeController(home, SweetHome3D.this);
            new HomeFrame(home, SweetHome3D.this, controller);  
          }
        };
      });
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
   * Sweet Home 3D entry point.
   * @param args may contain one .sh3d file to open.  
   */
  public static void main(String [] args) {
    HomeApplication application = new SweetHome3D();
    Home firstHome; 
    if (args.length == 2 && args [0].equals("-open")) {
      try {
        // Read home file in args [1] if args [0] == "-file"
        firstHome = application.getHomeRecorder().readHome(args [1]);
       } catch (RecorderException ex) {
        return;
       }
    } else {
      // Create a default home
      firstHome = new Home(application.getUserPreferences().getDefaultWallHeight());
    }
    
    // TODO
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
    // Opening a frame at the end of a main is ok as this method work is over
    application.addHome(firstHome);
  }

  /**
   * A frame that displays a home.
   */
  public static class HomeFrame extends JFrame {
    private static int         newHomeCount;
    private int                newHomeNumber;
    private ResourceBundle     resource;
    
    private HomeFrame(Home home,
                      HomeApplication application,
                      HomeController  controller) {
      if (home.getName() == null) {
        newHomeNumber = ++newHomeCount;
      }
      this.resource = ResourceBundle.getBundle(SweetHome3D.class.getName());
      // Enable windows to update their content while window resizing
      addListeners(home, application, controller);
      // Let windows layout their content while resizing
      Toolkit.getDefaultToolkit().setDynamicLayout(true); 
      // Update frame image ans title 
      setIconImage(new ImageIcon(
          HomeFrame.class.getResource("resources/frameIcon.png")).getImage());
      updateTitle(home);
      // Replace frame rootPane by home controller view
      // The best solution should be to program the following statement : 
      //   add(controller.getView());
      // but Mac OS X accepts to display the menu bar of a frame in the screen 
      // menu bar only if this menu bar depends directly on its root pane  
      setRootPane((JRootPane)controller.getView());
      // Compute frame size and location
      pack();
      fitInScreen();
      setLocationByPlatform(true);
      // Show frame
      setVisible(true);
    }
    
    /**
     * Add listeners to frame.
     */
    private void addListeners(final Home home, 
                              final HomeApplication application, 
                              final HomeController controller) {
      // Control window closing 
      setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
      addWindowListener(new WindowAdapter () {
          @Override
          public void windowClosing(WindowEvent ev) {
            controller.close();
          }
        });
      // Dispose window when a home is deleted 
      application.addHomeListener(new HomeListener() {
          public void homeChanged(HomeEvent ev) {
            if (ev.getHome() == home
                && ev.getType() == HomeEvent.Type.DELETE) {
              application.removeHomeListener(this);
              dispose();
              // Exit if application has no more home
              if (application.getHomes().isEmpty()) {
                System.exit(0);
              }
            }
          };
        });
      // Update title when the name or the modified state of home changes
      home.addPropertyChangeListener("name", new PropertyChangeListener () {
          public void propertyChange(PropertyChangeEvent ev) {
            updateTitle(home);
          }
        });
      home.addPropertyChangeListener("modified", new PropertyChangeListener () {
          public void propertyChange(PropertyChangeEvent ev) {
            updateTitle(home);
          }
        });
      if (System.getProperty("os.name").startsWith("Mac OS X")) {
        MacOSXConfiguration.bindControllerToApplicationMenu(this, controller);
      }
    }
    
    /**
     * Computes frame size to fit into screen.
     */
    private void fitInScreen() {
      Dimension screenSize = getToolkit().getScreenSize();
      Insets screenInsets = getToolkit().getScreenInsets(getGraphicsConfiguration());
      screenSize.width -= screenInsets.left + screenInsets.right;
      screenSize.height -= screenInsets.top + screenInsets.bottom;
      setSize(Math.min(screenSize.width * 4 / 5, getWidth()), 
              Math.min(screenSize.height * 4 / 5, getHeight()));
    }
    
    /**
     * Updates frame title from <code>home</code> name.
     */
    private void updateTitle(final Home home) {
      String name = home.getName();
      if (name == null) {
        name = this.resource.getString("untitled"); 
        if (newHomeNumber > 1) {
          name += " " + newHomeNumber;
        }
      } else {
        name = new File(name).getName();
      }
      
      String title = name;
      if (System.getProperty("os.name").startsWith("Mac OS X")) {
        // Use black indicator in close icon to show home is modified
        getRootPane().putClientProperty("windowModified", 
            Boolean.valueOf(home.isModified())); 
      } else {
        title += " - Sweet Home 3D"; 
        if (home.isModified()) {
          title = "* " + title;
        }
      }
      setTitle(title);
    }
  }
}
