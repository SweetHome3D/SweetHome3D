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

import javax.swing.UIManager;

import com.eteks.sweethome3d.io.HomeFileRecorder;
import com.eteks.sweethome3d.io.FileUserPreferences;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeApplication;
import com.eteks.sweethome3d.model.HomeEvent;
import com.eteks.sweethome3d.model.HomeListener;
import com.eteks.sweethome3d.model.HomeRecorder;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.UserPreferences;

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
    // Add a listener that opens a frame when a home is added to application
    addHomeListener(new HomeListener() {
        public void homeChanged(HomeEvent ev) {
          switch (ev.getType()) {
            case ADD :
              Home home = ev.getHome();
              new HomeFrameController(home, SweetHome3D.this);
              break;
            case DELETE :
              // Exit if application has no more home
              if (getHomes().isEmpty()) {
                System.exit(0);
              }
              break;
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
   * @param args may contain one .sh3d file to open, following a <code>-open</code> option.  
   */
  public static void main(String [] args) {
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

    HomeApplication application = new SweetHome3D();
    Home firstHome; 
    if (args.length == 2 && args [0].equals("-open")) {
      try {
        // Read home file in args [1] if args [0] == "-open"
        firstHome = application.getHomeRecorder().readHome(args [1]);
       } catch (RecorderException ex) {
        return;
       }
    } else {
      // Create a default home
      firstHome = new Home(application.getUserPreferences().getNewHomeWallHeight());
    }
    // Opening a frame at the end of a main is ok as this method work is over
    application.addHome(firstHome);
  }
}
