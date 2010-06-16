/*
 * CatalogDoorOrWindow.java 8 mars 2009
 *
 * Copyright (c) 2009 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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

import java.math.BigDecimal;

/**
 * A door or a window of the catalog.
 * @author Emmanuel Puybaret
 * @since  1.7
 */
public class CatalogDoorOrWindow extends CatalogPieceOfFurniture implements DoorOrWindow {
  private static final long serialVersionUID = 1L;

  private final float   wallThickness;
  private final float   wallDistance;
  private final Sash [] sashes;

  /**
   * Creates an unmodifiable catalog door or window of the default catalog.
   * @param id    the id of the new door or window, or <code>null</code>
   * @param name  the name of the new door or window
   * @param description the description of the new door or window 
   * @param icon content of the icon of the new door or window
   * @param model content of the 3D model of the new door or window
   * @param width  the width in centimeters of the new door or window
   * @param depth  the depth in centimeters of the new door or window
   * @param height  the height in centimeters of the new door or window
   * @param elevation  the elevation in centimeters of the new door or window
   * @param movable if <code>true</code>, the new door or window is movable
   * @param wallThickness a value in percentage of the depth of the new door or window
   * @param wallDistance a distance in percentage of the depth of the new door or window
   * @param sashes the sashes attached to the new door or window
   * @param modelRotation the rotation 3 by 3 matrix applied to the door or window model
   * @param creator the creator of the model
   * @param resizable if <code>true</code>, the size of the new door or window may be edited
   * @param price the price of the new door or window, or <code>null</code> 
   * @param valueAddedTaxPercentage the Value Added Tax percentage applied to the 
   *             price of the new door or window or <code>null</code> 
   */
  public CatalogDoorOrWindow(String id, String name, String description, Content icon, Content model, 
                             float width, float depth, float height, float elevation, boolean movable, 
                             float wallThickness, float wallDistance, Sash [] sashes,
                             float [][] modelRotation, String creator,
                             boolean resizable, BigDecimal price, BigDecimal valueAddedTaxPercentage) {
    this(id, name, description, icon, null, model, width, depth, height, elevation, movable,   
        wallThickness, wallDistance, sashes, modelRotation, creator, resizable, price, valueAddedTaxPercentage);
  }
         
  /**
   * Creates an unmodifiable catalog door or window of the default catalog.
   * @param id    the id of the new door or window, or <code>null</code>
   * @param name  the name of the new door or window
   * @param description the description of the new door or window 
   * @param icon content of the icon of the new door or window
   * @param planIcon content of the icon of the new piece displayed in plan
   * @param model content of the 3D model of the new door or window
   * @param width  the width in centimeters of the new door or window
   * @param depth  the depth in centimeters of the new door or window
   * @param height  the height in centimeters of the new door or window
   * @param elevation  the elevation in centimeters of the new door or window
   * @param movable if <code>true</code>, the new door or window is movable
   * @param wallThickness a value in percentage of the depth of the new door or window
   * @param wallDistance a distance in percentage of the depth of the new door or window
   * @param sashes the sashes attached to the new door or window
   * @param modelRotation the rotation 3 by 3 matrix applied to the door or window model
   * @param creator the creator of the model
   * @param resizable if <code>true</code>, the size of the new door or window may be edited
   * @param price the price of the new door or window, or <code>null</code> 
   * @param valueAddedTaxPercentage the Value Added Tax percentage applied to the 
   *             price of the new door or window or <code>null</code>
   * @since 2.2 
   */
  public CatalogDoorOrWindow(String id, String name, String description, 
                             Content icon, Content planIcon, Content model, 
                             float width, float depth, float height, float elevation, boolean movable, 
                             float wallThickness, float wallDistance, Sash [] sashes,
                             float [][] modelRotation, String creator,
                             boolean resizable, BigDecimal price, BigDecimal valueAddedTaxPercentage) {
    super(id, name, description, icon, model, width, depth, height, elevation, movable,   
        modelRotation, creator, resizable, price, valueAddedTaxPercentage);
    this.wallThickness = wallThickness;
    this.wallDistance = wallDistance;
    this.sashes = sashes;
  }
         
  /**
   * Creates a modifiable catalog door or window with all its values.
   * @param name  the name of the new door or window
   * @param icon content of the icon of the new door or window
   * @param model content of the 3D model of the new door or window
   * @param width  the width in centimeters of the new door or window
   * @param depth  the depth in centimeters of the new door or window
   * @param height  the height in centimeters of the new door or window
   * @param elevation  the elevation in centimeters of the new door or window
   * @param movable if <code>true</code>, the new door or window is movable
   * @param wallThickness a value in percentage of the depth of the new door or window
   * @param wallDistance a distance in percentage of the depth of the new door or window
   * @param sashes the sashes attached to the new door or window
   * @param color the color of the door or window as RGB code or <code>null</code> 
   *        if door or window color is unchanged
   * @param modelRotation the rotation 3 by 3 matrix applied to the door or window model
   * @param backFaceShown <code>true</code> if back face should be shown
   * @param iconYaw the yaw angle used to create the door or window icon
   * @param proportional if <code>true</code>, size proportions will be kept
   */
  public CatalogDoorOrWindow(String name, Content icon, Content model, 
                             float width, float depth, float height,
                             float elevation, boolean movable, 
                             float wallThickness, float wallDistance, Sash [] sashes, 
                             Integer color, float [][] modelRotation, boolean backFaceShown, 
                             float iconYaw, boolean proportional) {
    super(name, icon, model, width, depth, height, elevation, movable,   
        color, modelRotation, backFaceShown, iconYaw, proportional);
    this.wallThickness = wallThickness;
    this.wallDistance = wallDistance;
    this.sashes = sashes;
  }

  /**
   * Returns the default thickness of the wall in which this door or window should be placed.
   * @return a value in percentage of the depth of the door or the window.
   */
  public float getWallThickness() {
    return this.wallThickness;
  }
  
  /**
   * Returns the default distance that should lie at the back side of this door or window.
   * @return a distance in percentage of the depth of the door or the window.
   */
  public float getWallDistance() {
    return this.wallDistance;
  }
  
  /**
   * Returns a copy of the sashes attached to this door or window.
   * If no sash is defined an empty array is returned. 
   */
  public Sash [] getSashes() {
    if (this.sashes.length == 0) {
      return this.sashes;
    } else {
      return this.sashes.clone();
    }
  }

  /**
   * Returns always <code>true</code>.
   */
  @Override
  public boolean isDoorOrWindow() {
    return true;
  }
}
