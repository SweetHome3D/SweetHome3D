/*
 * Selectable.java 27 oct 2008
 *
 * Sweet Home 3D, Copyright (c) 2008 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
 * An object that is selectable in home.
 * @author Emmanuel Puybaret
 */
public interface Selectable extends Cloneable {
  /**
   * Returns the points of the shape surrounding this object.
   * @return an array of the (x,y) coordinates of the rectangle.
   */
  public abstract float [][] getPoints();

  /**
   * Returns <code>true</code> if this object intersects
   * with the horizontal rectangle which opposite corners are at points
   * (<code>x0</code>, <code>y0</code>) and (<code>x1</code>, <code>y1</code>).
   */
  public abstract boolean intersectsRectangle(float x0, float y0,
                                              float x1, float y1);

  /**
   * Returns <code>true</code> if this object contains the point at 
   * (<code>x</code>, <code>y</code>) with a given <code>margin</code>.
   */
  public abstract boolean containsPoint(float x, float y,
                                        float margin);

  /**
   * Moves this object of (<code>dx</code>, <code>dy</code>) units.
   */
  public abstract void move(float dx, float dy);
  
  /**
   * Returns a clone of this object.
   */
  public abstract Selectable clone();
}