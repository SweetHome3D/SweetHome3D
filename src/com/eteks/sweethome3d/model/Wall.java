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
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A wall of a home plan.
 * @author Emmanuel Puybaret
 */
public class Wall implements Serializable, Selectable {
  /**
   * The properties of a wall that may change. <code>PropertyChangeListener</code>s added 
   * to a wall will be notified under a property name equal to the string value of one these properties.
   */
  public enum Property {X_START, Y_START, X_END, Y_END, WALL_AT_START, WALL_AT_END, 
                        THICKNESS, HEIGHT, HEIGHT_AT_END, 
                        LEFT_SIDE_COLOR, LEFT_SIDE_TEXTURE, LEFT_SIDE_SHININESS, 
                        RIGHT_SIDE_COLOR, RIGHT_SIDE_TEXTURE, RIGHT_SIDE_SHININESS}
  
  private static final long serialVersionUID = 1L;
  
  private float       xStart;
  private float       yStart;
  private float       xEnd;
  private float       yEnd; 
  private Wall        wallAtStart;
  private Wall        wallAtEnd;
  private float       thickness;
  private Float       height;
  private Float       heightAtEnd;
  private Integer     leftSideColor;
  private HomeTexture leftSideTexture;
  private float       leftSideShininess;
  private Integer     rightSideColor;
  private HomeTexture rightSideTexture;
  private float       rightSideShininess;
  
  private transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
  private transient float [][] pointsCache;

  /**
   * Creates a wall from (<code>xStart</code>,<code>yStart</code>)
   * to (<code>xEnd</code>, <code>yEnd</code>), 
   * with given thickness. Height, left and right colors are <code>null</code>.
   */
  public Wall(float xStart, float yStart, float xEnd, float yEnd, float thickness) {
    this.xStart = xStart;
    this.yStart = yStart;
    this.xEnd = xEnd;
    this.yEnd = yEnd;
    this.thickness = thickness;
  }
  
  /**
   * Creates a wall from (<code>xStart</code>,<code>yStart</code>)
   * to (<code>xEnd</code>, <code>yEnd</code>), 
   * with given thickness and height. Left and right colors are <code>null</code>.
   */
  public Wall(float xStart, float yStart, float xEnd, float yEnd, float thickness, float height) {
    this(xStart, yStart, xEnd, yEnd, thickness);
    this.height = height;
  }
  
  /**
   * Initializes new wall transient fields  
   * and reads wall from <code>in</code> stream with default reading method.
   */
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    this.propertyChangeSupport = new PropertyChangeSupport(this);
    in.defaultReadObject();
  }

  /**
   * Adds the property change <code>listener</code> in parameter to this wall.
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    this.propertyChangeSupport.addPropertyChangeListener(listener);
  }

  /**
   * Removes the property change <code>listener</code> in parameter from this wall.
   */
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    this.propertyChangeSupport.removePropertyChangeListener(listener);
  }

  /**
   * Returns the start point abscissa of this wall.
   */
  public float getXStart() {
    return this.xStart;
  }

  /**
   * Sets the start point abscissa of this wall. Once this wall is updated, 
   * listeners added to this wall will receive a change notification.
   */
  public void setXStart(float xStart) {
    if (xStart != this.xStart) {
      float oldXStart = this.xStart;
      this.xStart = xStart;
      clearPointsCache();
      this.propertyChangeSupport.firePropertyChange(Property.X_START.name(), oldXStart, xStart);
    }
  }

  /**
   * Returns the start point ordinate of this wall.
   */
  public float getYStart() {
    return this.yStart;
  }

  /**
   * Sets the start point ordinate of this wall. Once this wall is updated, 
   * listeners added to this wall will receive a change notification.
   */
  public void setYStart(float yStart) {
    if (yStart != this.yStart) {
      float oldYStart = this.yStart;
      this.yStart = yStart;
      clearPointsCache();
      this.propertyChangeSupport.firePropertyChange(Property.Y_START.name(), oldYStart, yStart);
    }
  }

  /**
   * Returns the end point abscissa of this wall.
   */
  public float getXEnd() {
    return this.xEnd;
  }

  /**
   * Sets the end point abscissa of this wall. Once this wall is updated, 
   * listeners added to this wall will receive a change notification.
   */
  public void setXEnd(float xEnd) {
    if (xEnd != this.xEnd) {
      float oldXEnd = this.xEnd;
      this.xEnd = xEnd;
      clearPointsCache();
      this.propertyChangeSupport.firePropertyChange(Property.X_END.name(), oldXEnd, xEnd);
    }
  }

  /**
   * Returns the end point ordinate of this wall.
   */
  public float getYEnd() {
    return this.yEnd;
  }

  /**
   * Sets the end point ordinate of this wall. Once this wall is updated, 
   * listeners added to this wall will receive a change notification.
   */
  public void setYEnd(float yEnd) {
    if (yEnd != this.yEnd) {
      float oldYEnd = this.yEnd;
      this.yEnd = yEnd;
      clearPointsCache();
      this.propertyChangeSupport.firePropertyChange(Property.Y_END.name(), oldYEnd, yEnd);
    }
  }

  /**
   * Returns the length from the start point of this wall to its end point.
   * @since 2.0
   */
  public float getLength() {
    return (float)Point2D.distance(getXStart(), getYStart(), getXEnd(), getYEnd());
  }
  
  /**
   * Returns the wall joined to this wall at start point.
   */
  public Wall getWallAtStart() {
    return this.wallAtStart;
  }

  /**
   * Sets the wall joined to this wall at start point. Once this wall is updated, 
   * listeners added to this wall will receive a change notification.
   * If the start point of this wall is attached to an other wall, it will be detached 
   * from this wall, and wall listeners will receive a change notification.
   * @param wallAtStart a wall or <code>null</code> to detach this wall
   *          from any wall it was attached to before.
   */
  public void setWallAtStart(Wall wallAtStart) {
    setWallAtStart(wallAtStart, true);
  }

  /**
   * Sets the wall joined to this wall at start point and detachs the wall at start
   * from this wall if <code>detachJoinedWallAtStart</code> is true. 
   */
  private void setWallAtStart(Wall wallAtStart, boolean detachJoinedWallAtStart) {
    if (wallAtStart != this.wallAtStart) {
      Wall oldWallAtStart = this.wallAtStart;
      this.wallAtStart = wallAtStart;
      clearPointsCache();
      this.propertyChangeSupport.firePropertyChange(Property.WALL_AT_START.name(), 
          oldWallAtStart, wallAtStart);
      
      if (detachJoinedWallAtStart) {
        detachJoinedWall(oldWallAtStart);
      }
    }
  }

  /**
   * Returns the wall joined to this wall at end point.
   */
  public Wall getWallAtEnd() {
    return this.wallAtEnd;
  }
 
 
  /**
   * Sets the wall joined to this wall at end point. Once this wall is updated, 
   * listeners added to this wall will receive a change notification. 
   * If the end point of this wall is attached to an other wall, it will be detached 
   * from this wall, and wall listeners will receive a change notification.
   * @param wallAtEnd a wall or <code>null</code> to detach this wall
   *          from any wall it was attached to before.
   */
  public void setWallAtEnd(Wall wallAtEnd) {
    setWallAtEnd(wallAtEnd, true);
  }

  /**
   * Sets the wall joined to this wall at end point and detachs the wall at end
   * from this wall if <code>detachJoinedWallAtEnd</code> is true. 
   */
  private void setWallAtEnd(Wall wallAtEnd, boolean detachJoinedWallAtEnd) {
    if (wallAtEnd != this.wallAtEnd) {
      Wall oldWallAtEnd = this.wallAtEnd;
      this.wallAtEnd = wallAtEnd;
      clearPointsCache();
      this.propertyChangeSupport.firePropertyChange(Property.WALL_AT_END.name(), 
          oldWallAtEnd, wallAtEnd);
      
      if (detachJoinedWallAtEnd) {
        detachJoinedWall(oldWallAtEnd);
      }
    }
  }

  /**
   * Detaches <code>joinedWall</code> from this wall.
   */
  private void detachJoinedWall(Wall joinedWall) {
    // Detach the previously attached wall 
    if (joinedWall != null) {
      if (joinedWall.getWallAtStart() == this) {
        joinedWall.setWallAtStart(null, false);
      } else if (joinedWall.getWallAtEnd() == this) {
        joinedWall.setWallAtEnd(null, false);
      } 
    }
  }

  /**
   * Returns the thickness of this wall.
   */
  public float getThickness() {
    return this.thickness;
  }

  /**
   * Sets wall thickness. Once this wall is updated, 
   * listeners added to this wall will receive a change notification.
   */
  public void setThickness(float thickness) {
    if (thickness != this.thickness) {
      float oldThickness = this.thickness;
      this.thickness = thickness;
      clearPointsCache();
      this.propertyChangeSupport.firePropertyChange(Property.THICKNESS.name(), 
          oldThickness, thickness);
    }
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
   * Sets the height of this wall. Once this wall is updated, 
   * listeners added to this wall will receive a change notification.
   */
  public void setHeight(Float height) {
    if (height != this.height
        || (height != null && !height.equals(this.height))) {
      Float oldHeight = this.height;
      this.height = height;
      this.propertyChangeSupport.firePropertyChange(Property.HEIGHT.name(), 
          oldHeight, height);
    }
  }

  /**
   * Returns the height of this wall at its end point.
   */
  public Float getHeightAtEnd() {
    return this.heightAtEnd;
  }

  /**
   * Sets the height of this wall at its end point. Once this wall is updated, 
   * listeners added to this wall will receive a change notification.
   */
  public void setHeightAtEnd(Float heightAtEnd) {
    if (heightAtEnd != this.heightAtEnd
        || (heightAtEnd != null && !heightAtEnd.equals(this.heightAtEnd))) {
      Float oldHeightAtEnd = this.heightAtEnd;
      this.heightAtEnd = heightAtEnd;
      this.propertyChangeSupport.firePropertyChange(Property.HEIGHT_AT_END.name(), 
          oldHeightAtEnd, heightAtEnd);
    }
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
   * Sets left side color of this wall. Once this wall is updated, 
   * listeners added to this wall will receive a change notification.
   */
  public void setLeftSideColor(Integer leftSideColor) {
    if (leftSideColor != this.leftSideColor
        || (leftSideColor != null && !leftSideColor.equals(this.leftSideColor))) {
      Integer oldLeftSideColor = this.leftSideColor;
      this.leftSideColor = leftSideColor;
      this.propertyChangeSupport.firePropertyChange(Property.LEFT_SIDE_COLOR.name(), 
          oldLeftSideColor, leftSideColor);
    }
  }

  /**
   * Returns right side color of this wall. This is the color of the right side 
   * of this wall when you go through wall from start point to end point.
   */
  public Integer getRightSideColor() {
    return this.rightSideColor;
  }

  /**
   * Sets right side color of this wall. Once this wall is updated, 
   * listeners added to this wall will receive a change notification.
   */
  public void setRightSideColor(Integer rightSideColor) {
    if (rightSideColor != this.rightSideColor
        || (rightSideColor != null && !rightSideColor.equals(this.rightSideColor))) {
      Integer oldLeftSideColor = this.rightSideColor;
      this.rightSideColor = rightSideColor;
      this.propertyChangeSupport.firePropertyChange(Property.RIGHT_SIDE_COLOR.name(), 
          oldLeftSideColor, rightSideColor);
    }
  }

  
  /**
   * Returns the left side texture of this wall.
   */
  public HomeTexture getLeftSideTexture() {
    return this.leftSideTexture;
  }

  /**
   * Sets the left side texture of this wall. Once this wall is updated, 
   * listeners added to this wall will receive a change notification.
   */
  public void setLeftSideTexture(HomeTexture leftSideTexture) {
    if (leftSideTexture != this.leftSideTexture
        || (leftSideTexture != null && !leftSideTexture.equals(this.leftSideTexture))) {
      HomeTexture oldLeftSideTexture = this.leftSideTexture;
      this.leftSideTexture = leftSideTexture;
      this.propertyChangeSupport.firePropertyChange(Property.LEFT_SIDE_TEXTURE.name(), 
          oldLeftSideTexture, leftSideTexture);
    }
  }

  /**
   * Returns the right side texture of this wall.
   */
  public HomeTexture getRightSideTexture() {
    return this.rightSideTexture;
  }

  /**
   * Sets the right side texture of this wall. Once this wall is updated, 
   * listeners added to this wall will receive a change notification.
   */
  public void setRightSideTexture(HomeTexture rightSideTexture) {
    if (rightSideTexture != this.rightSideTexture
        || (rightSideTexture != null && !rightSideTexture.equals(this.rightSideTexture))) {
      HomeTexture oldLeftSideTexture = this.rightSideTexture;
      this.rightSideTexture = rightSideTexture;
      this.propertyChangeSupport.firePropertyChange(Property.RIGHT_SIDE_TEXTURE.name(), 
          oldLeftSideTexture, rightSideTexture);
    }
  }

  /**
   * Returns the left side shininess of this wall.
   * @return a value between 0 (matte) and 1 (very shiny)  
   * @since 3.0
   */
  public float getLeftSideShininess() {
    return this.leftSideShininess;
  }

  /**
   * Sets the left side shininess of this wall. Once this wall is updated, 
   * listeners added to this wall will receive a change notification.
   * @since 3.0
   */
  public void setLeftSideShininess(float leftSideShininess) {
    if (leftSideShininess != this.leftSideShininess) {
      float oldLeftSideShininess = this.leftSideShininess;
      this.leftSideShininess = leftSideShininess;
      this.propertyChangeSupport.firePropertyChange(Property.LEFT_SIDE_SHININESS.name(), oldLeftSideShininess, leftSideShininess);
    }
  }

  /**
   * Returns the right side shininess of this wall.
   * @return a value between 0 (matte) and 1 (very shiny)  
   * @since 3.0
   */
  public float getRightSideShininess() {
    return this.rightSideShininess;
  }

  /**
   * Sets the right side shininess of this wall. Once this wall is updated, 
   * listeners added to this wall will receive a change notification.
   * @since 3.0
   */
  public void setRightSideShininess(float rightSideShininess) {
    if (rightSideShininess != this.rightSideShininess) {
      float oldRightSideShininess = this.rightSideShininess;
      this.rightSideShininess = rightSideShininess;
      this.propertyChangeSupport.firePropertyChange(Property.RIGHT_SIDE_SHININESS.name(), oldRightSideShininess, rightSideShininess);
    }
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
      final float epsilon = 0.01f;
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

          // If the computed start point of this wall and the computed end point of the wall at start 
          // are equal to within epsilon, share the exact same point to avoid computing errors on areas 
          if (this.wallAtStart.pointsCache != null) {
            if (Math.abs(wallPoints [0][0] - this.wallAtStart.pointsCache [1][0]) < epsilon
                && Math.abs(wallPoints [0][1] - this.wallAtStart.pointsCache [1][1]) < epsilon) {
              wallPoints [0] = this.wallAtStart.pointsCache [1];
            }                        
            if (Math.abs(wallPoints [3][0] - this.wallAtStart.pointsCache [2][0]) < epsilon
                && Math.abs(wallPoints [3][1] - this.wallAtStart.pointsCache [2][1]) < epsilon) {
              wallPoints [3] = this.wallAtStart.pointsCache [2];
            }
          }
        } else if (this.wallAtStart.getWallAtStart() == this) {
          computeIntersection(wallPoints [0], wallPoints [1], 
              wallAtStartPoints [2], wallAtStartPoints [3], limit);
          computeIntersection(wallPoints [3], wallPoints [2],  
              wallAtStartPoints [0], wallAtStartPoints [1], limit);
          
          // If the computed start point of this wall and the computed start point of the wall at start 
          // are equal to within epsilon, share the exact same point to avoid computing errors on areas 
          if (this.wallAtStart.pointsCache != null) {
            if (Math.abs(wallPoints [0][0] - this.wallAtStart.pointsCache [3][0]) < epsilon
                && Math.abs(wallPoints [0][1] - this.wallAtStart.pointsCache [3][1]) < epsilon) {
              wallPoints [0] = this.wallAtStart.pointsCache [3];
            }                            
            if (this.wallAtStart.pointsCache != null
                && Math.abs(wallPoints [3][0] - this.wallAtStart.pointsCache [0][0]) < epsilon
                && Math.abs(wallPoints [3][1] - this.wallAtStart.pointsCache [0][1]) < epsilon) {
              wallPoints [3] = this.wallAtStart.pointsCache [0];
            }
          }
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

          // If the computed end point of this wall and the computed start point of the wall at end 
          // are equal to within epsilon, share the exact same point to avoid computing errors on areas 
          if (this.wallAtEnd.pointsCache != null) {
            if (Math.abs(wallPoints [1][0] - this.wallAtEnd.pointsCache [0][0]) < epsilon
                && Math.abs(wallPoints [1][1] - this.wallAtEnd.pointsCache [0][1]) < epsilon) {
              wallPoints [1] = this.wallAtEnd.pointsCache [0];
            }                        
            if (Math.abs(wallPoints [2][0] - this.wallAtEnd.pointsCache [3][0]) < epsilon
                && Math.abs(wallPoints [2][1] - this.wallAtEnd.pointsCache [3][1]) < epsilon) {
              wallPoints [2] = this.wallAtEnd.pointsCache [3];
            }
          }
        } else if (this.wallAtEnd.getWallAtEnd() == this) {
          computeIntersection(wallPoints [1], wallPoints [0],  
              wallAtEndPoints [3], wallAtEndPoints [2], limit);
          computeIntersection(wallPoints [2], wallPoints [3], 
              wallAtEndPoints [0], wallAtEndPoints [1], limit);

          // If the computed end point of this wall and the computed start point of the wall at end 
          // are equal to within epsilon, share the exact same point to avoid computing errors on areas 
          if (this.wallAtEnd.pointsCache != null) {
            if (Math.abs(wallPoints [1][0] - this.wallAtEnd.pointsCache [2][0]) < epsilon
                && Math.abs(wallPoints [1][1] - this.wallAtEnd.pointsCache [2][1]) < epsilon) {
              wallPoints [1] = this.wallAtEnd.pointsCache [2];
            }                        
            if (Math.abs(wallPoints [2][0] - this.wallAtEnd.pointsCache [1][0]) < epsilon
                && Math.abs(wallPoints [2][1] - this.wallAtEnd.pointsCache [1][1]) < epsilon) {
              wallPoints [2] = this.wallAtEnd.pointsCache [1];
            }
          }
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
      if (Math.abs(alpha1) > 1E5)  {
        if (Math.abs(alpha2) < 1E5) {
          x = point1 [0];
          y = alpha2 * x + beta2;
        }
      // If second line is vertical
      } else if (Math.abs(alpha2) > 1E5) {
        if (Math.abs(alpha1) < 1E5) {
          x = point3 [0];
          y = alpha1 * x + beta1;
        }
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
    if (margin == 0) {
      return shape.contains(x, y);
    } else {
      return shape.intersects(x - margin, y - margin, 2 * margin, 2 * margin);
    }
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
   * Returns a clone of the <code>walls</code> list. All existing walls 
   * are copied and their wall at start and end point are set with copied
   * walls only if they belong to the returned list.
   */
  public static List<Wall> clone(List<Wall> walls) {
    ArrayList<Wall> wallsCopy = new ArrayList<Wall>(walls.size());
    // Clone walls
    for (Wall wall : walls) {
      wallsCopy.add(wall.clone());      
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

  /**
   * Moves this wall of (<code>dx</code>, <code>dy</code>) units.
   */
  public void move(float dx, float dy) {
    setXStart(getXStart() + dx);
    setYStart(getYStart() + dy);
    setXEnd(getXEnd() + dx);
    setYEnd(getYEnd() + dy);
  }
  
  /**
   * Returns a clone of this wall expected 
   * its wall at start and wall at end aren't copied.
   */
  @Override
  public Wall clone() {
    try {
      Wall clone = (Wall)super.clone();
      clone.propertyChangeSupport = new PropertyChangeSupport(clone);
      clone.wallAtStart = null;
      clone.wallAtEnd = null;
      return clone;
    } catch (CloneNotSupportedException ex) {
      throw new IllegalStateException("Super class isn't cloneable"); 
    }
  }
}
