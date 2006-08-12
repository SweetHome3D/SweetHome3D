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

import com.eteks.sweethome3d.io.DefaultCatalog;

/**
 * User preferences.
 * @author Emmanuel Puybaret
 */
public abstract class UserPreferences {
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
  private float   defaultThickness;
  
  /**
   * Returns the catalog.
   */
  public Catalog getCatalog() {
    return this.catalog;
  }

  protected void setCatalog(DefaultCatalog catalog) {
    this.catalog = catalog;
  }

  /**
   * Returns the unit currently in use.
   */
  public Unit getUnit() {
    return this.unit;
  }
  
  /**
   * Changes the unit currently in use.
   * @param unit one of the values of Unit.
   */
  public void setUnit(Unit unit) {
    this.unit = unit;
  }

  /**
   * Returns <code>true</code> if magnetism is enabled.
   * @return <code>true</code> by default.
   */
  public boolean isMagnetismEnabled() {
    return this.magnetismEnabled;
  }

  /**
   * Sets whether magnetism is enabled or not.
   * @param magnetismEnabled <code>true</code> if magnetism is enabled,
   *          <code>false</code> otherwise.
   */
  public void setMagnetismEnabled(boolean magnetismEnabled) {
    this.magnetismEnabled = magnetismEnabled;
  }

  /**
   * Returns default thickness of new walls in home. 
   */
  public float getDefaultThickness() {
    return this.defaultThickness;
  }

  /**
   * Sets default thickness of new walls in home. 
   */
  public void setDefaultThickness(float defaultThickness) {
    this.defaultThickness = defaultThickness;
  }
}
