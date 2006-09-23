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

/**
 * User preferences.
 * @author Emmanuel Puybaret
 */
public abstract class UserPreferences {
  private PropertyChangeSupport propertyChangeSupport;

  /**
   * Unit used for dimensions.
   */
  public enum Unit {
    CENTIMETER, INCH;

    public static float centimerToInch(float length) {
      return length / 2.54f;
    }

    public static float inchToCentimer(float length) {
      return length * 2.54f;
    }
  }

  private Catalog catalog;
  private Unit    unit;
  private boolean magnetismEnabled = true;
  private float   newWallThickness;
  private float   newHomeWallHeight;

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
  public void addPropertyChangeListener(String property, 
                                        PropertyChangeListener listener) {
    this.propertyChangeSupport.addPropertyChangeListener(
        property, listener);
  }

  /**
   * Removes the <code>listener</code> in parameter from these preferences.
   */
  public void removeProrertyChangeistener(String property, 
                                          PropertyChangeListener listener) {
    this.propertyChangeSupport.removePropertyChangeListener(
        property, listener);
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
   * Changes the unit currently in use, and notifies
   * listeners of this change. 
   * @param unit one of the values of Unit.
   */
  public void setUnit(Unit unit) {
    if (this.unit != unit) {
      Unit oldUnit = this.unit;
      this.unit = unit;
      this.propertyChangeSupport.firePropertyChange("unit", oldUnit, unit);
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
      boolean oldMagnetismEnabled = this.magnetismEnabled;
      this.magnetismEnabled = magnetismEnabled;
      this.propertyChangeSupport.firePropertyChange("magnetismEnabled", 
          oldMagnetismEnabled, magnetismEnabled);
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
      this.propertyChangeSupport.firePropertyChange("newWallThickness", 
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
      this.propertyChangeSupport.firePropertyChange("newHomeWallHeight", 
          oldHomeWallHeight, newHomeWallHeight);
    }
  }
}
