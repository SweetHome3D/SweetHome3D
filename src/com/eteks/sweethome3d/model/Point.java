/*
 * Point.java 3 juin 2006
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

/**
 * The start point or the end point of a wall.
 * @author Emmanuel Puybaret
 */
public class Point {
  private float x;
  private float y;

  /**
   * Creates a point of coordinates (<code>x</code>,<code>y</code>).
   */
  public Point(float x, float y) {
    this.x = x;
    this.y = y;
  }

  /**
   * Returns the abscissa of this point.
   */
  public float getX() {
    return this.x;
  }

  /**
   * Sets the abscissa of this point.
   * This method should be called only from {@link Home}, which
   * controls notifications when a point changed.
   */
  void setX(float x) {
    this.x = x;
  }

  /**
   * Returns the ordinate of this point.
   */
  public float getY() {
    return this.y;
  }

  /**
   * Sets the ordinate of this point.
   * This method should be called only from {@link Home}, which
   * controls notifications when a point changed.
   */
  void setY(float y) {
    this.y = y;
  }
}
