/*
 * HomeFrameController.java 1 sept. 2006
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

import javax.swing.JComponent;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeApplication;
import com.eteks.sweethome3d.plugin.PluginManager;
import com.eteks.sweethome3d.swing.ContentManager;
import com.eteks.sweethome3d.swing.HomeController;

/**
 * Home frame pane controller.
 * @author Emmanuel Puybaret
 */
public class HomeFrameController {
  private Home            home;
  private ContentManager  contentManager;
  private HomeApplication application;
  private JComponent      homeFrameView;
  private PluginManager   pluginManager;
  
  private HomeController  homeController;
  
  public HomeFrameController(Home home, HomeApplication application, 
                             ContentManager contentManager, PluginManager pluginManager) {
    this.home = home;
    this.application = application;
    this.contentManager = contentManager;
    this.pluginManager = pluginManager;
  }

  /**
   * Returns the view associated with this controller.
   */
  public JComponent getView() {
    // Create view lazily only once it's needed
    if (this.homeFrameView == null) {
      this.homeFrameView = new HomeFramePane(this.home, this.application, this.contentManager, this);
    }
    return this.homeFrameView;
  }
  
  /**
   * Returns the home controller managed by this controller.
   */
  public HomeController getHomeController() {
    // Create sub controller lazily only once it's needed
    if (this.homeController == null) {
      this.homeController = new HomeController(
          this.home, this.application, this.contentManager, this.pluginManager);
    }
    return this.homeController;
  }
  
  /**
   * Displays the view controlled by this controller.
   */
  public void displayView() {
    ((HomeFramePane)getView()).displayView();
  }
}
