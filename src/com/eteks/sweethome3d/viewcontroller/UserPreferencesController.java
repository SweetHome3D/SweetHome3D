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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.eteks.sweethome3d.model.UserPreferences;

/**
 * A MVC controller for user preferences view.
 * @author Emmanuel Puybaret
 */
public class UserPreferencesController implements Controller {
  /**
   * The properties that may be edited by the view associated to this controller. 
   */
  public enum Property {LANGUAGE, UNIT, MAGNETISM_ENABLED, RULERS_VISIBLE, 
      GRID_VISIBLE, NEW_WALL_THICKNESS, NEW_WALL_HEIGHT}
  
  private final UserPreferences       preferences;
  private final ViewFactory           viewFactory;
  private final PropertyChangeSupport propertyChangeSupport;
  private DialogView                  userPreferencesView;

  private String                language;
  private UserPreferences.Unit  unit;
  private boolean               magnetismEnabled;
  private boolean               rulersVisible;
  private boolean               gridVisible;
  private float                 newWallThickness;
  private float                 newWallHeight;

  /**
   * Creates the controller of user preferences view.
   */
  public UserPreferencesController(UserPreferences preferences,
                        ViewFactory viewFactory, 
                        ContentManager contentManager) {
    this.preferences = preferences;
    this.viewFactory = viewFactory;
    this.propertyChangeSupport = new PropertyChangeSupport(this);
    
    updateProperties(preferences);
  }

  /**
   * Returns the view associated with this controller.
   */
  public DialogView getView() {
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
   * Adds the property change <code>listener</code> in parameter to this controller.
   */
  public void addPropertyChangeListener(Property property, PropertyChangeListener listener) {
    this.propertyChangeSupport.addPropertyChangeListener(property.toString(), listener);
  }

  /**
   * Removes the property change <code>listener</code> in parameter from this controller.
   */
  public void removePropertyChangeListener(Property property, PropertyChangeListener listener) {
    this.propertyChangeSupport.removePropertyChangeListener(property.toString(), listener);
  }

  /**
   * Updates controller edited properties from <code>preferences</code>.
   */
  private void updateProperties(UserPreferences preferences) {
    setLanguage(preferences.getLanguage());
    setUnit(preferences.getUnit());
    setMagnetismEnabled(preferences.isMagnetismEnabled());
    setGridVisible(preferences.isGridVisible());
    setRulersVisible(preferences.isRulersVisible());
    setNewWallThickness(preferences.getNewWallThickness());
    setNewWallHeight(preferences.getNewWallHeight());
  }  

  /**
   * Sets the edited language.
   */
  public void setLanguage(String language) {
    if (language != this.language) {
      String oldLanguage = this.language;
      this.language = language;
      this.propertyChangeSupport.firePropertyChange(Property.LANGUAGE.toString(), oldLanguage, language);
    }
  }

  /**
   * Returns the edited language.
   */
  public String getLanguage() {
    return this.language;
  }

  /**
   * Sets the edited unit.
   */
  public void setUnit(UserPreferences.Unit unit) {
    if (unit != this.unit) {
      UserPreferences.Unit oldUnit = this.unit;
      this.unit = unit;
      this.propertyChangeSupport.firePropertyChange(Property.UNIT.toString(), oldUnit, unit);
    }
  }

  /**
   * Returns the edited unit.
   */
  public UserPreferences.Unit getUnit() {
    return this.unit;
  }

  /**
   * Sets whether magnetism is enabled or not.
   */
  public void setMagnetismEnabled(boolean magnetismEnabled) {
    if (magnetismEnabled != this.magnetismEnabled) {
      this.magnetismEnabled = magnetismEnabled;
      this.propertyChangeSupport.firePropertyChange(Property.MAGNETISM_ENABLED.toString(), !magnetismEnabled, magnetismEnabled);
    }
  }

  /**
   * Returns whether magnetism is enabled or not.
   */
  public boolean isMagnetismEnabled() {
    return this.magnetismEnabled;
  }

  /**
   * Sets whether rulers are visible or not.
   */
  public void setRulersVisible(boolean rulersVisible) {
    if (rulersVisible != this.rulersVisible) {
      this.rulersVisible = rulersVisible;
      this.propertyChangeSupport.firePropertyChange(Property.MAGNETISM_ENABLED.toString(), !rulersVisible, rulersVisible);
    }
  }

  /**
   * Returns whether rulers are visible or not.
   */
  public boolean isRulersVisible() {
    return this.rulersVisible;
  }

  /**
   * Sets whether grid is visible or not.
   */
  public void setGridVisible(boolean gridVisible) {
    if (gridVisible != this.gridVisible) {
      this.gridVisible = gridVisible;
      this.propertyChangeSupport.firePropertyChange(Property.MAGNETISM_ENABLED.toString(), !gridVisible, gridVisible);
    }
  }

  /**
   * Returns whether grid is visible or not.
   */
  public boolean isGridVisible() {
    return this.gridVisible;
  }

  /**
   * Sets the edited new wall thickness.
   */
  public void setNewWallThickness(float newWallThickness) {
    if (newWallThickness != this.newWallThickness) {
      float oldNewWallThickness = this.newWallThickness;
      this.newWallThickness = newWallThickness;
      this.propertyChangeSupport.firePropertyChange(Property.NEW_WALL_THICKNESS.toString(), oldNewWallThickness, newWallThickness);
    }
  }

  /**
   * Returns the edited new wall thickness.
   */
  public float getNewWallThickness() {
    return this.newWallThickness;
  }

  /**
   * Sets the edited new wall height.
   */
  public void setNewWallHeight(float newWallHeight) {
    if (newWallHeight != this.newWallHeight) {
      float oldNewWallHeight = this.newWallHeight;
      this.newWallHeight = newWallHeight;
      this.propertyChangeSupport.firePropertyChange(Property.NEW_WALL_HEIGHT.toString(), oldNewWallHeight, newWallHeight);
    }
  }

  /**
   * Returns the edited new wall height.
   */
  public float getNewWallHeight() {
    return this.newWallHeight;
  }

  /**
   * Controls the modification of user preferences.
   */
  public void modify() {
    this.preferences.setLanguage(getLanguage());
    this.preferences.setUnit(getUnit());
    this.preferences.setMagnetismEnabled(isMagnetismEnabled());
    this.preferences.setRulersVisible(isRulersVisible());
    this.preferences.setGridVisible(isGridVisible());
    this.preferences.setNewWallThickness(getNewWallThickness());
    this.preferences.setNewWallHeight(getNewWallHeight());
  }
}
