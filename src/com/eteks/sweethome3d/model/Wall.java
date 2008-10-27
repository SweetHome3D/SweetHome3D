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

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A wall of a home plan.
 * @author Emmanuel Puybaret
 */
public class Wall implements Serializable, Selectable {
  private static final long serialVersionUID = 1L;
  
  private float   xStart;
  private float   yStart;
  private float   xEnd;
  private float   yEnd; 
  private Wall    wallAtStart;
  private Wall    wallAtEnd;
  private float   thickness;
  private Float   height;
  private Float   heightAtEnd;
  private Integer     leftSideColor;
  private HomeTexture leftSideTexture;
  private Integer     rightSideColor;
  private HomeTexture rightSideTexture;
  
  private transient float [][] pointsCache;

  /**
   * Creates a wall from (<code>xStart</code>,<code>yStart</code>)
   * to (<code>xEnd</code>, <code>yEnd</code>), 
   * with given thickness. Left and right colors are <code>null</code>.
   */
  public Wall(float xStart, float yStart, float xEnd, float yEnd, float thickness) {
    this.xStart = xStart;
    this.yStart = yStart;
    this.xEnd = xEnd;
    this.yEnd = yEnd;
    this.thickness = thickness;
  }
  
  /**
   * Creates a wall from a given <code>wall</code>.
   * The walls at start and at end are not copied.  
   */
  public Wall(Wall wall) {
    this(wall.getXStart(), wall.getYStart(), 
         wall.getXEnd(), wall.getYEnd(), wall.getThickness());
    this.height = wall.getHeight();
    this.heightAtEnd = wall.getHeightAtEnd();
    this.leftSideColor = wall.getLeftSideColor();
    this.leftSideTexture = wall.getLeftSideTexture();
    this.rightSideColor = wall.getRightSideColor();
    this.rightSideTexture = wall.getRightSideTexture();
  }
  
  /**
   * Returns the start point abscissa of this wall.
   */
  public float getXStart() {
    return this.xStart;
  }

  /**
   * Sets the start point abscissa of this wall.
   * This method should be called from {@link Home}, which
   * controls notifications when a wall changed.
   */
  void setXStart(float xStart) {
    this.xStart = xStart;
    clearPointsCache();
  }

  /**
   * Returns the start point ordinate of this wall.
   */
  public float getYStart() {
    return this.yStart;
  }

  /**
   * Sets the start point ordinate of this wall.
   * This method should be called from {@link Home}, which
   * controls notifications when a wall changed.
   */
  void setYStart(float yStart) {
    this.yStart = yStart;
    clearPointsCache();
  }

  /**
   * Returns the end point abscissa of this wall.
   */
  public float getXEnd() {
    return this.xEnd;
  }

  /**
   * Sets the end point abscissa of this wall.
   * This method should be called from {@link Home}, which
   * controls notifications when a wall changed.
   */
  void setXEnd(float xEnd) {
    this.xEnd = xEnd;
    clearPointsCache();
  }

  /**
   * Returns the end point ordinate of this wall.
   */
  public float getYEnd() {
    return this.yEnd;
  }

  /**
   * Sets the end point ordinate of this wall.
   * This method should be called from {@link Home}, which
   * controls notifications when a wall changed.
   */
  void setYEnd(float yEnd) {
    this.yEnd = yEnd;
    clearPointsCache();
  }

  /**
   * Returns the wall joined to this wall at start point.
   */
  public Wall getWallAtStart() {
    return this.wallAtStart;
  }

  /**
   * Sets the wall joined to this wall at start point.
   * This method should be called from {@link Home}, which
   * controls notifications when a wall changed.
   */
  void setWallAtStart(Wall wallAtStart) {
    this.wallAtStart = wallAtStart;
    clearPointsCache();
  }

  /**
   * Returns the wall joined to this wall at end point.
   */
  public Wall getWallAtEnd() {
    return this.wallAtEnd;
  }
 
 
  /**
   * Sets the wall joined to this wall at end point.
   * This method should be called from {@link Home}, which
   * controls notifications when a wall changed.
   */
  void setWallAtEnd(Wall wallAtEnd) {
    this.wallAtEnd = wallAtEnd;
    clearPointsCache();
  }

  /**
   * Returns the thickness of this wall.
   */
  public float getThickness() {
    return this.thickness;
  }

  /**
   * Sets wall thickness.
   * This method should be called from {@link Home}, which
   * controls notifications when a wall changed.
   */
  void setThickness(float thickness) {
    this.thickness = thickness;
    clearPointsCache();
  }

  /**
   * Returns the height of this wall. If {@link #getHeightAtEnd() getHeightAtEnd}
   * returns a value not <code>null</code>, the returned height should be
   * considered as the height of this wall at its start point.
   */
  public Float getHeight() {
    return this.height;
  }

  /**
   * Sets the height of this wall.
   * This method should be called from {@link Home}, which
   * controls notifications when a wall changed.
   */
  void setHeight(Float height) {
    this.height = height;
  }

  /**
   * Returns the height of this wall at its end point.
   */
  public Float getHeightAtEnd() {
    return this.heightAtEnd;
  }

  /**
   * Sets the height of this wall at its end point.
   * This method should be called from {@link Home}, which
   * controls notifications when a wall changed.
   */
  void setHeightAtEnd(Float heightAtEnd) {
    this.heightAtEnd = heightAtEnd;
  }

  /**
   * Returns <code>true</code> if the height of this wall is different
   * at its start and end points. 
   */
  public boolean isTrapezoidal() {
    return this.height != null
        && this.heightAtEnd != null
        && !this.height.equals(this.heightAtEnd);  
  }
  
  /**
   * Returns left side color of this wall. This is the color of the left side 
   * of this wall when you go through wall from start point to end point.
   */
  public Integer getLeftSideColor() {
    return this.leftSideColor;
  }

  /**
   * Sets left side color of this wall. 
   * This method should be called from {@link Home}, which
   * controls notifications when a wall changed.
   */
  void setLeftSideColor(Integer leftColor) {
    this.leftSideColor = leftColor;
  }

  /**
   * Returns right side color of this wall. This is the color of the right side 
   * of this wall when you go through wall from start point to end point.
   */
  public Integer getRightSideColor() {
    return this.rightSideColor;
  }

  /**
   * Sets right side color of this wall. 
   */
  void setRightSideColor(Integer rightColor) {
    this.rightSideColor = rightColor;
  }

  
  /**
   * Returns the left side texture of this wall.
   */
  public HomeTexture getLeftSideTexture() {
    return this.leftSideTexture;
  }

  /**
   * Sets the left side texture of this wall.
   */
  void setLeftSideTexture(HomeTexture leftSideTexture) {
    this.leftSideTexture = leftSideTexture;
  }

  /**
   * Returns the right side texture of this wall.
   */
  public HomeTexture getRightSideTexture() {
    return this.rightSideTexture;
  }

  /**
   * Sets the right side texture of this wall.
   */
  void setRightSideTexture(HomeTexture rightSideTexture) {
    this.rightSideTexture = rightSideTexture;
  }

  /**
   * Clears the points cache of this wall and of the walls attached to it.
   */
  private void clearPointsCache() {
    this.pointsCache = null;
    if (this.wallAtStart != null ) {
      this.wallAtStart.pointsCache = null;
    }
    if (this.wallAtEnd != null) {
      this.wallAtEnd.pointsCache = null;
    }
  }
  
  /**
   * Returns the points of each corner of a wall. 
   * @return an array of the 4 (x,y) coordinates of the wall corners.
   *    The points at index 0 and 3 indicates the start of the wall, while
   *    the points at index 1 and 2 indicates the end of the wall. 
   */
  public float [][] getPoints() {
    if (this.pointsCache == null) {
      float [][] wallPoints = getRectanglePoints();
      float limit = 2 * this.thickness;
      // If wall is joined to a wall at its start, 
      // compute the intersection between their outlines 
      if (this.wallAtStart != null) {
        float [][] wallAtStartPoints = this.wallAtStart.getRectanglePoints();
        if (this.wallAtStart.getWallAtEnd() == this) {
          computeIntersection(wallPoints [0], wallPoints [1], 
              wallAtStartPoints [1], wallAtStartPoints [0], limit);
          computeIntersection(wallPoints [3], wallPoints [2],  
              wallAtStartPoints [2], wallAtStartPoints [3], limit);
        } else if (this.wallAtStart.getWallAtStart() == this) {
          computeIntersection(wallPoints [0], wallPoints [1], 
              wallAtStartPoints [2], wallAtStartPoints [3], limit);
          computeIntersection(wallPoints [3], wallPoints [2],  
              wallAtStartPoints [0], wallAtStartPoints [1], limit);
        }
      }
    
      // If wall is joined to a wall at its end, 
      // compute the intersection between their outlines 
      if (this.wallAtEnd != null) {
        float [][] wallAtEndPoints = this.wallAtEnd.getRectanglePoints();
        if (this.wallAtEnd.getWallAtStart() == this) {
          computeIntersection(wallPoints [1], wallPoints [0], 
              wallAtEndPoints [0], wallAtEndPoints [1], limit);
          computeIntersection(wallPoints [2], wallPoints [3], 
              wallAtEndPoints [3], wallAtEndPoints [2], limit);
        
        } else if (this.wallAtEnd.getWallAtEnd() == this) {
          computeIntersection(wallPoints [1], wallPoints [0],  
              wallAtEndPoints [3], wallAtEndPoints [2], limit);
          computeIntersection(wallPoints [2], wallPoints [3], 
              wallAtEndPoints [0], wallAtEndPoints [1], limit);
        }
      }
      // Cache shape
      this.pointsCache = wallPoints;
    }
    return new float [][] {
        {this.pointsCache [0][0], this.pointsCache [0][1]},
        {this.pointsCache [1][0], this.pointsCache [1][1]},
        {this.pointsCache [2][0], this.pointsCache [2][1]},
        {this.pointsCache [3][0], this.pointsCache [3][1]}};
  }

  /**
   * Compute the rectangle of a wall with its thickness.
   */  
  private float [][] getRectanglePoints() {
    double angle = Math.atan2(this.yEnd - this.yStart, 
                              this.xEnd - this.xStart);
    float dx = (float)Math.sin(angle) * this.thickness / 2;
    float dy = (float)Math.cos(angle) * this.thickness / 2;
    return new float [][] {
        {this.xStart + dx, this.yStart - dy},
        {this.xEnd   + dx, this.yEnd   - dy},
        {this.xEnd   - dx, this.yEnd   + dy},
        {this.xStart - dx, this.yStart + dy}};
  }
  
  /**
   * Compute the intersection between the line that joins <code>point1</code> to <code>point2</code>
   * and the line that joins <code>point3</code> and <code>point4</code>, and stores the result 
   * in <code>point1</code>.
   */
  private void computeIntersection(float [] point1, float [] point2, 
                                   float [] point3, float [] point4, float limit) {
    float x = point1 [0];
    float y = point1 [1];
    float alpha1 = (point2 [1] - point1 [1]) / (point2 [0] - point1 [0]);
    float beta1  = point2 [1] - alpha1 * point2 [0];
    float alpha2 = (point4 [1] - point3 [1]) / (point4 [0] - point3 [0]);
    float beta2  = point4 [1] - alpha2 * point4 [0];
    // If the two lines are not parallel
    if (alpha1 != alpha2) {
      // If first line is vertical
      if (point1 [0] == point2 [0]) {
        x = point1 [0];
        y = alpha2 * x + beta2;
      // If second line is vertical
      } else if (point3 [0] == point4 [0]) {
        x = point3 [0];
        y = alpha1 * x + beta1;
      } else  {
        x = (beta2 - beta1) / (alpha1 - alpha2);
        y = alpha1 * x + beta1;
      } 
    }
    if (Point2D.distanceSq(x, y, point1 [0], point1 [1]) < limit * limit) {
      point1 [0] = x;
      point1 [1] = y;
    }
  }
  
  /**
   * Returns <code>true</code> if this wall intersects
   * with the horizontal rectangle which opposite corners are at points
   * (<code>x0</code>, <code>y0</code>) and (<code>x1</code>, <code>y1</code>).
   */
  public boolean intersectsRectangle(float x0, float y0, float x1, float y1) {
    Rectangle2D rectangle = new Rectangle2D.Float(x0, y0, 0, 0);
    rectangle.add(x1, y1);
    return getShape().intersects(rectangle);
  }
  
  /**
   * Returns <code>true</code> if this wall contains 
   * the point at (<code>x</code>, <code>y</code>)
   * with a given <code>margin</code>.
   */
  public boolean containsPoint(float x, float y, float margin) {
    return containsShapeAtWithMargin(getShape(), x, y, margin);
  }
  
  /**
   * Returns <code>true</code> if this wall start line contains 
   * the point at (<code>x</code>, <code>y</code>)
   * with a given <code>margin</code> around the wall start line.
   */
  public boolean containsWallStartAt(float x, float y, float margin) {
    float [][] wallPoints = getPoints();
    Line2D startLine = new Line2D.Float(wallPoints [0][0], wallPoints [0][1], wallPoints [3][0], wallPoints [3][1]);
    return containsShapeAtWithMargin(startLine, x, y, margin);
  }
  
  /**
   * Returns <code>true</code> if this wall end line contains 
   * the point at (<code>x</code>, <code>y</code>)
   * with a given <code>margin</code> around the wall end line.
   */
  public boolean containsWallEndAt(float x, float y, float margin) {
    float [][] wallPoints = getPoints();
    Line2D endLine = new Line2D.Float(wallPoints [1][0], wallPoints [1][1], wallPoints [2][0], wallPoints [2][1]); 
    return containsShapeAtWithMargin(endLine, x, y, margin);
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
   * Returns the shape matching this wall.
   */
  private Shape getShape() {
    float [][] wallPoints = getPoints();
    GeneralPath wallPath = new GeneralPath();
    wallPath.moveTo(wallPoints [0][0], wallPoints [0][1]);
    for (int i = 1; i < wallPoints.length; i++) {
      wallPath.lineTo(wallPoints [i][0], wallPoints [i][1]);
    }
    wallPath.closePath();
    return wallPath;
  }
  
  /**
   * Returns a deep copy of the <code>walls</code>. All existing walls 
   * are copied and their wall at start and end point are set with copied
   * walls only if they belong to the returned list.
   */
  public static List<Wall> deepCopy(List<Wall> walls) {
    ArrayList<Wall> wallsCopy = new ArrayList<Wall>(walls.size());
    // Deep copy walls
    for (Wall wall : walls) {
      wallsCopy.add(new Wall(wall));      
    }
    // Update walls at start and end point
    for (int i = 0; i < walls.size(); i++) {
      Wall wall = walls.get(i);
      int wallAtStartIndex = walls.indexOf(wall.getWallAtStart());
      if (wallAtStartIndex != -1) {
        wallsCopy.get(i).setWallAtStart(wallsCopy.get(wallAtStartIndex));
      }
      int wallAtEndIndex = walls.indexOf(wall.getWallAtEnd());
      if (wallAtEndIndex != -1) {
        wallsCopy.get(i).setWallAtEnd(wallsCopy.get(wallAtEndIndex));
      }
    }
    return wallsCopy;
  }
}
