/*
 * HomeDoorOrWindow.java 8 mars 2009
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

/**
 * A door or a window in {@linkplain Home home}.
 * @author Emmanuel Puybaret
 * @since  1.6
 */
public class HomeDoorOrWindow extends HomePieceOfFurniture implements DoorOrWindow {
  private static final long serialVersionUID = 1L;

  private final float   wallThickness;
  private final float   wallDistance;
  private final Sash [] sashes;

  /**
   * Creates a home door or window from an existing one.
   * @param doorOrWindow the door or window from which data are copied
   */
  public HomeDoorOrWindow(DoorOrWindow doorOrWindow) {
    super(doorOrWindow);
    this.wallThickness = doorOrWindow.getWallThickness();
    this.wallDistance = doorOrWindow.getWallDistance();
    this.sashes = doorOrWindow.getSashes();
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

  /**
   * Returns a clone of this door or window.
   */
  @Override
  public HomeDoorOrWindow clone() {
    return (HomeDoorOrWindow)super.clone();
  }
}
