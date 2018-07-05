/*
 * HomeDoorOrWindow.java 8 mars 2009
 *
 * Sweet Home 3D, Copyright (c) 2009 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * A door or a window in {@linkplain Home home}.
 * @author Emmanuel Puybaret
 * @since  1.7
 */
public class HomeDoorOrWindow extends HomePieceOfFurniture implements DoorOrWindow {
  private static final long serialVersionUID = 1L;

  private float         wallThickness;
  private float         wallDistance;
  private float         wallWidth;
  private float         wallLeft;
  private float         wallHeight;
  private float         wallTop;
  private boolean       wallCutOutOnBothSides; // false for version < 5.5
  private boolean       widthDepthDeformable;
  private Sash []       sashes;
  private String        cutOutShape;
  private boolean       boundToWall;

  /**
   * Creates a home door or window from an existing one.
   * @param doorOrWindow the door or window from which data are copied
   */
  public HomeDoorOrWindow(DoorOrWindow doorOrWindow) {
    super(doorOrWindow);
    this.wallThickness = doorOrWindow.getWallThickness();
    this.wallDistance = doorOrWindow.getWallDistance();
    this.wallWidth = 1;
    this.wallLeft = 0;
    this.wallHeight = 1;
    this.wallTop = 0;
    this.wallCutOutOnBothSides = doorOrWindow.isWallCutOutOnBothSides();
    this.widthDepthDeformable = doorOrWindow.isWidthDepthDeformable();
    this.sashes = doorOrWindow.getSashes();
    this.cutOutShape = doorOrWindow.getCutOutShape();
  }

  /**
   * Initializes new fields to their default values
   * and reads object from <code>in</code> stream with default reading method.
   */
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    this.cutOutShape = PieceOfFurniture.DEFAULT_CUT_OUT_SHAPE;
    this.widthDepthDeformable = true;
    this.wallWidth = 1;
    this.wallLeft = 0;
    this.wallHeight = 1;
    this.wallTop = 0;
    in.defaultReadObject();
  }

  /**
   * Returns the thickness of the wall in which this door or window should be placed.
   * @return a value in percentage of the depth of the door or the window.
   */
  public float getWallThickness() {
    return this.wallThickness;
  }

  /**
   * Sets the thickness of the wall in which this door or window should be placed.
   * @param wallThickness a value in percentage of the depth of the door or the window.
   * @since 6.0
   */
  public void setWallThickness(float wallThickness) {
    this.wallThickness = wallThickness;
  }

  /**
   * Returns the distance between the back side of this door or window and the wall where it's located.
   * @return a distance in percentage of the depth of the door or the window.
   */
  public float getWallDistance() {
    return this.wallDistance;
  }

  /**
   * Sets the distance between the back side of this door or window and the wall where it's located.
   * @param wallDistance a distance in percentage of the depth of the door or the window.
   * @since 6.0
   */
  public void setWallDistance(float wallDistance) {
    this.wallDistance = wallDistance;
  }

  /**
   * Returns the width of the wall part in which this door or window should be placed.
   * @return a value in percentage of the width of the door or the window.
   * @since 6.0
   */
  public float getWallWidth() {
    return this.wallWidth;
  }

  /**
   * Sets the width of the wall part in which this door or window should be placed.
   * @param wallWidth a value in percentage of the width of the door or the window.
   * @since 6.0
   */
  public void setWallWidth(float wallWidth) {
    this.wallWidth = wallWidth;
  }

  /**
   * Returns the distance between the left side of this door or window and the wall part where it should be placed.
   * @return a distance in percentage of the width of the door or the window.
   * @since 6.0
   */
  public float getWallLeft() {
    return this.wallLeft;
  }

  /**
   * Sets the distance between the left side of this door or window and the wall part where it should be placed.
   * @param wallLeft a distance in percentage of the width of the door or the window.
   * @since 6.0
   */
  public void setWallLeft(float wallLeft) {
    this.wallLeft = wallLeft;
  }

  /**
   * Returns the height of the wall part in which this door or window should be placed.
   * @return a value in percentage of the height of the door or the window.
   * @since 6.0
   */
  public float getWallHeight() {
    return this.wallHeight;
  }

  /**
   * Sets the height of the wall part in which this door or window should be placed.
   * @param wallHeight a value in percentage of the height of the door or the window.
   * @since 6.0
   */
  public void setWallHeight(float wallHeight) {
    this.wallHeight = wallHeight;
  }

  /**
   * Returns the distance between the left side of this door or window and the wall part where it should be placed.
   * @return a distance in percentage of the height of the door or the window.
   * @since 6.0
   */
  public float getWallTop() {
    return this.wallTop;
  }

  /**
   * Sets the distance between the top side of this door or window and the wall part where it should be placed.
   * @param wallTop a distance in percentage of the height of the door or the window.
   * @since 6.0
   */
  public void setWallTop(float wallTop) {
    this.wallTop = wallTop;
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
   * Sets the sashes attached to this door or window.
   * @param sashes sashes of this window.
   * @since 6.0
   */
  public void setSashes(Sash [] sashes) {
    this.sashes = sashes.length == 0
        ? sashes
        : sashes.clone();
  }

  /**
   * Returns the shape used to cut out walls that intersect this new door or window.
   * @since 4.2
   */
  public String getCutOutShape() {
    return this.cutOutShape;
  }

  /**
   * Returns <code>true</code> if this door or window should cut out the both sides
   * of the walls it intersects, even if its front or back side are within the wall thickness.
   * @since 5.5
   */
  public boolean isWallCutOutOnBothSides() {
    return this.wallCutOutOnBothSides;
  }

  /**
   * Returns <code>false</code> if the width and depth of the new door or window may
   * not be changed independently from each other. When <code>false</code>, this door or window
   * will also make a hole in the wall when it's placed whatever its depth if its
   * {@link #isBoundToWall() bouldToWall} flag is <code>true</code>.
   * @since 5.5
   */
  @Override
  public boolean isWidthDepthDeformable() {
    return this.widthDepthDeformable;
  }

  /**
   * Returns <code>true</code> if the location and the size of this door or window
   * were bound to a wall, last time they were updated.
   */
  public boolean isBoundToWall() {
    return this.boundToWall;
  }

  /**
   * Sets whether the location and the size of this door or window
   * were bound to a wall, last time they were updated.
   */
  public void setBoundToWall(boolean boundToWall) {
    this.boundToWall = boundToWall;
  }

  /**
   * Sets the abscissa of this door or window and
   * resets its {@link #isBoundToWall() boundToWall} flag if the abscissa changed.
   */
  @Override
  public void setX(float x) {
    if (getX() != x) {
      this.boundToWall = false;
    }
    super.setX(x);
  }

  /**
   * Sets the ordinate of this door or window and
   * resets its {@link #isBoundToWall() boundToWall} flag if the ordinate changed.
   */
  @Override
  public void setY(float y) {
    if (getY() != y) {
      this.boundToWall = false;
    }
    super.setY(y);
  }

  /**
   * Sets the angle of this door or window and
   * resets its {@link #isBoundToWall() boundToWall} flag if the angle changed.
   */
  @Override
  public void setAngle(float angle) {
    if (getAngle() != angle) {
      this.boundToWall = false;
    }
    super.setAngle(angle);
  }

  /**
   * Sets the depth of this door or window and
   * resets its {@link #isBoundToWall() boundToWall} flag if the depth changed.
   */
  @Override
  public void setDepth(float depth) {
    if (getDepth() != depth) {
      this.boundToWall = false;
    }
    super.setDepth(depth);
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
    HomeDoorOrWindow clone = (HomeDoorOrWindow)super.clone();
    clone.boundToWall = false;
    return clone;
  }
}
