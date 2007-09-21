/*
 * DimensionLine.java 17 sept 2007
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
package com.eteks.sweethome3d.model;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

/**
 * A dimension line in plan.
 * @author Emmanuel Puybaret
 */
public class DimensionLine implements Serializable {
  private static final long serialVersionUID = 1L;
  
  private float xStart;
  private float yStart;
  private float xEnd;
  private float yEnd;
  private float offset;

  private transient Shape shapeCache;

  /**
   * Creates a dimension line from (<code>xStart</code>,<code>yStart</code>)
   * to (<code>xEnd</code>, <code>yEnd</code>), with a given offset.
   */
  public DimensionLine(float xStart, float yStart, float xEnd, float yEnd, float offset) {
    this.xStart = xStart;
    this.yStart = yStart;
    this.xEnd = xEnd;
    this.yEnd = yEnd;
    this.offset = offset;
  }
  
  /**
   * Creates a dimension line from an existing one.
   * @param dimensionLine the dimension line from which data are copied
   */
  public DimensionLine(DimensionLine dimensionLine) {
    this(dimensionLine.xStart, dimensionLine.yStart, dimensionLine.xEnd, dimensionLine.yEnd, dimensionLine.offset);
  }

  /**
   * Returns the start point abscissa of this dimension line.
   */
  public float getXStart() {
    return this.xStart;
  }

  /**
   * Sets the start point abscissa of this dimension line.
   * This method should be called from {@link Home}, which
   * controls notifications when a dimension line changed.
   */
  void setXStart(float xStart) {
    this.xStart = xStart;
    this.shapeCache = null;
  }

  /**
   * Returns the start point ordinate of this dimension line.
   */
  public float getYStart() {
    return this.yStart;
  }

  /**
   * Sets the start point ordinate of this dimension line.
   * This method should be called from {@link Home}, which
   * controls notifications when a dimension line changed.
   */
  void setYStart(float yStart) {
    this.yStart = yStart;
    this.shapeCache = null;
  }

  /**
   * Returns the end point abscissa of this dimension line.
   */
  public float getXEnd() {
    return this.xEnd;
  }

  /**
   * Sets the end point abscissa of this dimension line.
   * This method should be called from {@link Home}, which
   * controls notifications when a dimension line changed.
   */
  void setXEnd(float xEnd) {
    this.xEnd = xEnd;
    this.shapeCache = null;
  }

  /**
   * Returns the end point ordinate of this dimension line.
   */
  public float getYEnd() {
    return this.yEnd;
  }

  /**
   * Sets the end point ordinate of this dimension line.
   * This method should be called from {@link Home}, which
   * controls notifications when a dimension line changed.
   */
  void setYEnd(float yEnd) {
    this.yEnd = yEnd;
    this.shapeCache = null;
  }

  /**
   * Returns the offset of this dimension line.
   */
  public float getOffset() {
    return this.offset;
  }

  /**
   * Sets the offset of this dimension line.
   * This method should be called from {@link Home}, which
   * controls notifications when a dimension line changed.
   */
  void setOffset(float offset) {
    this.offset = offset;
    this.shapeCache = null;
  }

  /**
   * Returns the points of the rectangle surrounding 
   * this dimension line and its extension lines.
   * @return an array of the 4 (x,y) coordinates of the rectangle.
   */
  public float [][] getPoints() {
    double angle = Math.atan2(this.yEnd - this.yStart, this.xEnd - this.xStart);
    float dx = (float)-Math.sin(angle) * this.offset;
    float dy = (float)Math.cos(angle) * this.offset;
    
    return new float [] [] {{this.xStart, this.yStart},
                            {this.xStart + dx, this.yStart + dy},
                            {this.xEnd + dx, this.yEnd + dy},
                            {this.xEnd, this.yEnd}};
  }
  
  /**
   * Returns <code>true</code> if this dimension line intersects
   * with the horizontal rectangle which opposite corners are at points
   * (<code>x0</code>, <code>y0</code>) and (<code>x1</code>, <code>y1</code>).
   */
  public boolean intersectsRectangle(float x0, float y0, float x1, float y1) {
    Rectangle2D rectangle = new Rectangle2D.Float(x0, y0, 0, 0);
    rectangle.add(x1, y1);
    return getShape().intersects(rectangle);
  }
  
  /**
   * Returns <code>true</code> if this dimension line contains 
   * the point at (<code>x</code>, <code>y</code>)
   * with a given <code>margin</code>.
   */
  public boolean containsPoint(float x, float y, float margin) {
    return containsShapeAtWithMargin(getShape(), x, y, margin);
  }
  
  /**
   * Returns <code>true</code> if the middle point of this dimension line 
   * is the point at (<code>x</code>, <code>y</code>)
   * with a given <code>margin</code>.
   */
  public boolean isMiddlePointAt(float x, float y, float margin) {
    double angle = Math.atan2(this.yEnd - this.yStart, this.xEnd - this.xStart);
    float dx = (float)-Math.sin(angle) * this.offset;
    float dy = (float)Math.cos(angle) * this.offset;
    float xMiddle = (xStart + xEnd) / 2 + dx;
    float yMiddle = (yStart + yEnd) / 2 + dy;
    return Math.abs(x - xMiddle) <= margin && Math.abs(y - yMiddle) <= margin;
  }

  /**
   * Returns <code>true</code> if the extension line at the start of this dimension line 
   * contains the point at (<code>x</code>, <code>y</code>)
   * with a given <code>margin</code> around the extension line.
   */
  public boolean containsStartExtensionLinetAt(float x, float y, float margin) {
    double angle = Math.atan2(this.yEnd - this.yStart, this.xEnd - this.xStart);
    Line2D startExtensionLine = new Line2D.Float(this.xStart, this.yStart, 
        this.xStart + (float)-Math.sin(angle) * this.offset, 
        this.yStart + (float)Math.cos(angle) * this.offset);
    return containsShapeAtWithMargin(startExtensionLine, x, y, margin);
  }
  
  /**
   * Returns <code>true</code> if the extension line at the end of this dimension line 
   * contains the point at (<code>x</code>, <code>y</code>)
   * with a given <code>margin</code> around the extension line.
   */
  public boolean containsEndExtensionLineAt(float x, float y, float margin) {
    double angle = Math.atan2(this.yEnd - this.yStart, this.xEnd - this.xStart);
    Line2D endExtensionLine = new Line2D.Float(this.xEnd, this.yEnd, 
        this.xEnd + (float)-Math.sin(angle) * this.offset, 
        this.yEnd + (float)Math.cos(angle) * this.offset); 
    return containsShapeAtWithMargin(endExtensionLine, x, y, margin);
  }

  /**
   * Returns <code>true</code> if <code>shape</code> contains 
   * the point at (<code>x</code>, <code>y</code>)
   * with a given <code>margin</code>.
   */
  private boolean containsShapeAtWithMargin(Shape shape, float x, float y, float margin) {
    return shape.intersects(x - margin, y - margin, 2 * margin, 2 * margin);
  }

  /**
   * Returns the shape matching this dimension line.
   */
  private Shape getShape() {    
    if (this.shapeCache == null) {
      // Create the rectangle that matches piece bounds
      double angle = Math.atan2(this.yEnd - this.yStart, this.xEnd - this.xStart);
      float dx = (float)-Math.sin(angle) * this.offset;
      float dy = (float)Math.cos(angle) * this.offset;
      
      GeneralPath dimensionLineShape = new GeneralPath();
      // Append dimension line
      dimensionLineShape.append(new Line2D.Float(this.xStart + dx, this.yStart + dy, this.xEnd + dx, this.yEnd + dy), false);
      // Append extension lines
      dimensionLineShape.append(new Line2D.Float(this.xStart, this.yStart, this.xStart + dx, this.yStart + dy), false);
      dimensionLineShape.append(new Line2D.Float(this.xEnd, this.yEnd, this.xEnd + dx, this.yEnd + dy), false);
      // Cache shape
      this.shapeCache = dimensionLineShape;
    }
    return this.shapeCache;
  }
}
