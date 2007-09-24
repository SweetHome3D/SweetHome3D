/*
 * UserPreferences.java 15 mai 2006
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
package com.eteks.sweethome3d.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * User preferences.
 * @author Emmanuel Puybaret
 */
public abstract class UserPreferences {
  public enum Property {LANGUAGE, UNIT, MAGNETISM_ENABLED, RULERS_VISIBLE, GRID_VISIBLE, 
                        NEW_HOME_WALL_HEIGHT, NEW_WALL_THICKNESS, RECENT_HOMES}
  
  private PropertyChangeSupport propertyChangeSupport;

  /**
   * Unit used for sizes.
   */
  public enum Unit {
    CENTIMETER, INCH;

    public static float centimeterToInch(float length) {
      return length / 2.54f;
    }

    public static float centimeterToFoot(float length) {
      return length / 2.54f / 12;
    }
    
    public static float inchToCentimeter(float length) {
      return length * 2.54f;
    }
  }

  private Catalog      catalog;
  private String       language;
  private Unit         unit;
  private boolean      magnetismEnabled = true;
  private boolean      rulersVisible    = true;
  private boolean      gridVisible      = true;
  private float        newWallThickness;
  private float        newHomeWallHeight;
  private List<String> recentHomes;

  public UserPreferences() {
    this.propertyChangeSupport = new PropertyChangeSupport(this); 
  }
  
  /**
   * Writes user preferences.
   * @throws RecorderException if user preferences couldn'y be saved.
   */
  public abstract void write() throws RecorderException;
  
  /**
   * Adds the <code>listener</code> in parameter to these preferences. 
   */
  public void addPropertyChangeListener(Property property, 
                                        PropertyChangeListener listener) {
    this.propertyChangeSupport.addPropertyChangeListener(property.toString(), listener);
  }

  /**
   * Removes the <code>listener</code> in parameter from these preferences.
   */
  public void removePropertyChangeListener(Property property, 
                                           PropertyChangeListener listener) {
    this.propertyChangeSupport.removePropertyChangeListener(property.toString(), listener);
  }

  /**
   * Returns the catalog.
   */
  public Catalog getCatalog() {
    return this.catalog;
  }

  protected void setCatalog(Catalog catalog) {
    this.catalog = catalog;
  }

  /**
   * Returns the unit currently in use.
   */
  public Unit getUnit() {
    return this.unit;
  }
  
  /**
   * Returns the preferred language to display information, noted with ISO 639 code. 
   */
  public String getLanguage() {
    return this.language;
  }

  /**
   * Sets the preferred language to display information, changes current default locale accordingly 
   * and notifies listeners of this change.
   */
  public void setLanguage(String language) {
    if (!language.equals(this.language)) {
      String oldLanguage = this.language;
      this.language = language;      
      Locale.setDefault(new Locale(language, Locale.getDefault().getCountry()));
      this.propertyChangeSupport.firePropertyChange(Property.LANGUAGE.toString(), 
          oldLanguage, language);
    }
  }

  /**
   * Changes the unit currently in use, and notifies listeners of this change. 
   * @param unit one of the values of Unit.
   */
  public void setUnit(Unit unit) {
    if (this.unit != unit) {
      Unit oldUnit = this.unit;
      this.unit = unit;
      this.propertyChangeSupport.firePropertyChange(Property.UNIT.toString(), oldUnit, unit);
    }
  }

  /**
   * Returns <code>true</code> if magnetism is enabled.
   * @return <code>true</code> by default.
   */
  public boolean isMagnetismEnabled() {
    return this.magnetismEnabled;
  }

  /**
   * Sets whether magnetism is enabled or not, and notifies
   * listeners of this change. 
   * @param magnetismEnabled <code>true</code> if magnetism is enabled,
   *          <code>false</code> otherwise.
   */
  public void setMagnetismEnabled(boolean magnetismEnabled) {
    if (this.magnetismEnabled != magnetismEnabled) {
      this.magnetismEnabled = magnetismEnabled;
      this.propertyChangeSupport.firePropertyChange(Property.MAGNETISM_ENABLED.toString(), 
          !magnetismEnabled, magnetismEnabled);
    }
  }

  /**
   * Returns <code>true</code> if rulers are visible.
   * @return <code>true</code> by default.
   */
  public boolean isRulersVisible() {
    return this.rulersVisible;
  }

  /**
   * Sets whether rulers are visible or not, and notifies
   * listeners of this change. 
   * @param rulersVisible <code>true</code> if rulers are visible,
   *          <code>false</code> otherwise.
   */
  public void setRulersVisible(boolean rulersVisible) {
    if (this.rulersVisible != rulersVisible) {
      this.rulersVisible = rulersVisible;
      this.propertyChangeSupport.firePropertyChange(Property.RULERS_VISIBLE.toString(), 
          !rulersVisible, rulersVisible);
    }
  }
  
  /**
   * Returns <code>true</code> if plan grid visible.
   * @return <code>true</code> by default.
   */
  public boolean isGridVisible() {
    return this.gridVisible;
  }
  
  /**
   * Sets whether plan grid is visible or not, and notifies
   * listeners of this change. 
   * @param gridVisible <code>true</code> if grid is visible,
   *          <code>false</code> otherwise.
   */
  public void setGridVisible(boolean gridVisible) {
    if (this.gridVisible != gridVisible) {
      this.gridVisible = gridVisible;
      this.propertyChangeSupport.firePropertyChange(Property.GRID_VISIBLE.toString(), 
          !gridVisible, gridVisible);
    }
  }

  /**
   * Returns default thickness of new walls in home. 
   */
  public float getNewWallThickness() {
    return this.newWallThickness;
  }

  /**
   * Sets default thickness of new walls in home, and notifies
   * listeners of this change.  
   */
  public void setNewWallThickness(float newWallThickness) {
    if (this.newWallThickness != newWallThickness) {
      float oldDefaultThickness = this.newWallThickness;
      this.newWallThickness = newWallThickness;
      this.propertyChangeSupport.firePropertyChange(Property.NEW_WALL_THICKNESS.toString(), 
          oldDefaultThickness, newWallThickness);
    }
  }

  /**
   * Returns default wall height of new home walls. 
   */
  public float getNewHomeWallHeight() {
    return this.newHomeWallHeight;
  }

  /**
   * Sets default wall height of new home walls, and notifies
   * listeners of this change. 
   */
  public void setNewHomeWallHeight(float newHomeWallHeight) {
    if (this.newHomeWallHeight != newHomeWallHeight) {
      float oldHomeWallHeight = this.newHomeWallHeight;
      this.newHomeWallHeight = newHomeWallHeight;
      this.propertyChangeSupport.firePropertyChange(Property.NEW_HOME_WALL_HEIGHT.toString(), 
          oldHomeWallHeight, newHomeWallHeight);
    }
  }
  
  
  
  /**
   * Returns an unmodifiable list of the recent homes.
   */
  public List<String> getRecentHomes() {
    return Collections.unmodifiableList(this.recentHomes);
  }

  /**
   * Sets the recent homes list and notifies listeners of this change.
   */
  public void setRecentHomes(List<String> recentHomes) {
    if (!recentHomes.equals(this.recentHomes)) {
      List<String> oldRecentHomes = this.recentHomes;
      this.recentHomes = new ArrayList<String>(recentHomes);
      this.propertyChangeSupport.firePropertyChange(Property.RECENT_HOMES.toString(), 
          oldRecentHomes, getRecentHomes());
    }
  }
}
