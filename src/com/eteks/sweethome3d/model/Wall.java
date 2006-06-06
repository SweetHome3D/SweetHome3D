/*
 * Wall.java 3 juin 2006
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
 * A wall of a home.
 * @author Emmanuel Puybaret
 */
public class Wall {
  private float startX;
  private float startY;
  private float endX;
  private float endY; 
  private Wall  wallAtStart;
  private Wall  wallAtEnd;
  private int   innerColor;
  private int   outerColor;
  private float thickness;

  /**
   * Creates a wall from (<code>startX</code>,<code>startY</code>)
   * to (<code>endX</code>, <code>endY</code>), 
   * with given inner color, outer color and thickness.
   */
  public Wall(float startX, float startY, float endX, float endY, int innerColor, int outerColor, float thickness) {
    this.startX = startX;
    this.startY = startY;
    this.endX = endX;
    this.endY = endY;
    this.innerColor = innerColor;
    this.outerColor = outerColor;
    this.thickness = thickness;
  }

  /**
   * Returns the start point abscissa of this wall.
   */
  public float getStartX() {
    return this.startX;
  }

  /**
   * Sets the start point abscissa of this wall.
   */
  void setStartX(float startX) {
    this.startX = startX;
  }

  /**
   * Returns the start point ordinate of this wall.
   */
  public float getStartY() {
    return this.startY;
  }

  /**
   * Sets the start point ordinate of this wall.
   */
  void setStartY(float startY) {
    this.startY = startY;
  }

  /**
   * Returns the end point abscissa of this wall.
   */
  public float getEndX() {
    return this.endX;
  }

  /**
   * Sets the end point abscissa of this wall.
   */
  void setEndX(float endX) {
    this.endX = endX;
  }

  /**
   * Returns the end point ordinate of this wall.
   */
  public float getEndY() {
    return this.endY;
  }

  /**
   * Sets the end point ordinate of this wall.
   */
  void setEndY(float endY) {
    this.endY = endY;
  }

  /**
   * Returns the wall joined to this wall at start point.
   */
  public Wall getWallAtStart() {
    return this.wallAtStart;
  }

  /**
   * Sets the wall joined to this wall at start point.
   * This method should be called only from {@link Home}, which
   * controls notifications when a wall changed.
   */
  void setWallAtStart(Wall wallAtStart) {
    this.wallAtStart = wallAtStart;
  }

  /**
   * Returns the wall joined to this wall at end point.
   */
  public Wall getWallAtEnd() {
    return this.wallAtEnd;
  }
 
 
  /**
   * Sets the wall joined to this wall at end point.
   * This method should be called only from {@link Home}, which
   * controls notifications when a wall changed.
   */
  void setWallAtEnd(Wall wallAtEnd) {
    this.wallAtEnd = wallAtEnd;
  }

  /**
   * Returns the inner color of this wall.
   * @return the RGB code of the inner color of this wall
   */
  public int getInnerColor() {
    return this.innerColor;
  }

  /**
   * Returns the outer color of this wall.
   * @return the RGB code of the outer color of this wall
   */
  public int getOuterColor() {
    return this.outerColor;
  }

  /**
   * Returns the thickness of this wall.
   */
  public float getThickness() {
    return this.thickness;
  }
}
