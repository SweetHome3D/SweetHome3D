/*
 * UserPreferencesController.java 28 oct 2008
 *
 * Copyright (c) 2007 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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
package com.eteks.sweethome3d.viewcontroller;

import com.eteks.sweethome3d.model.UserPreferences;

/**
 * A MVC controller for user preferences view.
 * @author Emmanuel Puybaret
 */
public class UserPreferencesController implements Controller {
  private final UserPreferences     preferences;
  private final ViewFactory         viewFactory;
  private UserPreferencesView       userPreferencesView;

  /**
   * Creates the controller of user preferences view.
   */
  public UserPreferencesController(UserPreferences preferences,
                        ViewFactory viewFactory, 
                        ContentManager contentManager) {
    this.preferences = preferences;
    this.viewFactory = viewFactory;
  }

  /**
   * Returns the view associated with this controller.
   */
  public UserPreferencesView getView() {
    // Create view lazily only once it's needed
    if (this.userPreferencesView == null) {
      this.userPreferencesView = this.viewFactory.createUserPreferencesView(this.preferences, this); 
    }
    return this.userPreferencesView;
  }

  /**
   * Displays the view controlled by this controller.
   */
  public void displayView(View parentView) {
    getView().displayView(parentView);
  }

  /**
   * Controls the modification of user preferences.
   */
  public void modifyUserPreferences() {
    UserPreferencesView preferencesView = getView();
    this.preferences.setLanguage(preferencesView.getLanguage());
    this.preferences.setUnit(preferencesView.getUnit());
    this.preferences.setMagnetismEnabled(preferencesView.isMagnetismEnabled());
    this.preferences.setRulersVisible(preferencesView.isRulersVisible());
    this.preferences.setGridVisible(preferencesView.isGridVisible());
    this.preferences.setNewWallThickness(preferencesView.getNewWallThickness());
    this.preferences.setNewWallHeight(preferencesView.getNewWallHeight());
  }
}
