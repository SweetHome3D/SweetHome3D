/*
 * HomePluginController.java 05 janv 2012
 *
 * Sweet Home 3D, Copyright (c) 2006-2012 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
package com.eteks.sweethome3d.plugin;

import java.util.Collections;
import java.util.List;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeApplication;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.viewcontroller.ContentManager;
import com.eteks.sweethome3d.viewcontroller.HomeController;
import com.eteks.sweethome3d.viewcontroller.ViewFactory;

/**
 * A MVC controller for the home view able to manage plug-ins.
 * @author Emmanuel Puybaret
 */
public class HomePluginController extends HomeController {
  private final Home                  home;
  private final HomeApplication       application;
  private final PluginManager         pluginManager;

  /**
   * Creates the controller of home view.
   * @param home the home edited by this controller and its view.
   * @param application the instance of current application.
   * @param viewFactory a factory able to create views.
   * @param contentManager the content manager of the application.
   * @param pluginManager  the plug-in manager of the application.
   */
  public HomePluginController(Home home, 
                              HomeApplication application,
                              ViewFactory    viewFactory, 
                              ContentManager contentManager, 
                              PluginManager pluginManager) {
    super(home, application, viewFactory, contentManager);
    this.home = home;
    this.application = application;
    this.pluginManager = pluginManager;
  }

  /**
   * Returns the plug-ins available with this controller.
   */
  public List<Plugin> getPlugins() {
    if (this.application != null && this.pluginManager != null) {
      // Retrieve home plug-ins
      return this.pluginManager.getPlugins(
          this.application, this.home, this.application.getUserPreferences(), this, getUndoableEditSupport());
    } else {
      List<Plugin> plugins = Collections.emptyList();
      return plugins;
    }
  }

  /**
   * Imports a given plugin.
   */
  public void importPlugin(String pluginName) {
    if (this.pluginManager != null) {
      try {
        if (!this.pluginManager.pluginExists(pluginName) 
            || getView().confirmReplacePlugin(pluginName)) {
          this.pluginManager.addPlugin(pluginName);
          getView().showMessage(this.application.getUserPreferences().getLocalizedString(HomeController.class, 
              "importedPluginMessage"));
        }
      } catch (RecorderException ex) {
        String message = this.application.getUserPreferences().getLocalizedString(HomeController.class, 
            "importPluginError", pluginName);
        getView().showError(message);
      }
    }
  }
}
