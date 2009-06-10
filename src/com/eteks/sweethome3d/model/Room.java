/*
 * Room.java 18 nov. 2008
 *
 * Copyright (c) 2008 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A room or a polygon in a home plan. 
 * @author Emmanuel Puybaret
 */
public class Room implements Serializable, Selectable {
  /**
   * The properties of a room that may change. <code>PropertyChangeListener</code>s added 
   * to a room will be notified under a property name equal to the string value of one these properties.
   */
  public enum Property {NAME, NAME_X_OFFSET, NAME_Y_OFFSET, NAME_STYLE,
      POINTS, AREA_VISIBLE, AREA_X_OFFSET, AREA_Y_OFFSET, AREA_STYLE,
      FLOOR_COLOR, FLOOR_TEXTURE, FLOOR_VISIBLE, 
      CEILING_COLOR, CEILING_TEXTURE, CEILING_VISIBLE}
  
  private static final long serialVersionUID = 1L;
  
  private String      name;
  private float       nameXOffset;
  private float       nameYOffset;
  private TextStyle   nameStyle;
  private float [][]  points;
  private boolean     areaVisible;
  private float       areaXOffset;
  private float       areaYOffset;
  private TextStyle   areaStyle;
  private boolean     floorVisible;
  private Integer     floorColor;
  private HomeTexture floorTexture;
  private boolean     ceilingVisible;
  private Integer     ceilingColor;
  private HomeTexture ceilingTexture;
  
  private transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
  private transient Shape shapeCache;
  private transient Float areaCache;

  /**
   * Creates a room from its name and the given coordinates.
   */
  public Room(float [][] points) {
    this.points = deepCopy(points);
    this.areaVisible = true;
    this.nameYOffset = -40f;
    this.floorVisible = true;
    this.ceilingVisible = true;
  }

  /**
   * Initializes new room transient fields  
   * and reads room from <code>in</code> stream with default reading method.
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
   * Returns the name of this room.
   */
  public String getName() {
    return this.name;
  }

  /**
   * Sets the name of this room. Once this room is updated, 
   * listeners added to this room will receive a change notification.
   */
  public void setName(String name) {
    if (name != this.name
        || (name != null && !name.equals(this.name))) {
      String oldName = this.name;
      this.name = name;
      this.propertyChangeSupport.firePropertyChange(Property.NAME.name(), oldName, name);
    }
  }
   
  /**
   * Returns the distance along x axis applied to room center abscissa 
   * to display room name. 
   */
  public float getNameXOffset() {
    return this.nameXOffset;  
  }
  
  /**
   * Sets the distance along x axis applied to room center abscissa to display room name. 
   * Once this room  is updated, listeners added to this room will receive a change notification.
   */
  public void setNameXOffset(float nameXOffset) {
    if (nameXOffset != this.nameXOffset) {
      float oldNameXOffset = this.nameXOffset;
      this.nameXOffset = nameXOffset;
      this.propertyChangeSupport.firePropertyChange(Property.NAME_X_OFFSET.name(), oldNameXOffset, nameXOffset);
    }
  }
  
  /**
   * Returns the distance along y axis applied to room center ordinate 
   * to display room name.
   */
  public float getNameYOffset() {
    return this.nameYOffset;  
  }

  /**
   * Sets the distance along y axis applied to room center ordinate to display room name. 
   * Once this room is updated, listeners added to this room will receive a change notification.
   */
  public void setNameYOffset(float nameYOffset) {
    if (nameYOffset != this.nameYOffset) {
      float oldNameYOffset = this.nameYOffset;
      this.nameYOffset = nameYOffset;
      this.propertyChangeSupport.firePropertyChange(Property.NAME_Y_OFFSET.name(), oldNameYOffset, nameYOffset);
    }
  }
  
  /**
   * Returns the text style used to display room name.
   */
  public TextStyle getNameStyle() {
    return this.nameStyle;  
  }

  /**
   * Sets the text style used to display room name.
   * Once this room is updated, listeners added to this room will receive a change notification.
   */
  public void setNameStyle(TextStyle nameStyle) {
    if (nameStyle != this.nameStyle) {
      TextStyle oldNameStyle = this.nameStyle;
      this.nameStyle = nameStyle;
      this.propertyChangeSupport.firePropertyChange(Property.NAME_STYLE.name(), oldNameStyle, nameStyle);
    }
  }
  
  /**
   * Returns the points of the polygon matching this room. 
   * @return an array of the (x,y) coordinates of the room points.
   */
  public float [][] getPoints() {
    return deepCopy(this.points);  
  }

  /**
   * Returns the number of points of the polygon matching this room.
   * @since 2.0 
   */
  public int getPointCount() {
    return this.points.length;  
  }

  private float [][] deepCopy(float [][] points) {
    float [][] pointsCopy = new float [points.length][];
    for (int i = 0; i < points.length; i++) {
      pointsCopy [i] = points [i].clone();
    }
    return pointsCopy;
  }

  /**
   * Sets the points of the polygon matching this room. Once this room 
   * is updated, listeners added to this room will receive a change notification.
   */
  public void setPoints(float [][] points) {
    if (!Arrays.deepEquals(this.points, points)) {
      updatePoints(points);
    }
  }

  /**
   * Update the points of the polygon matching this room.
   */
  private void updatePoints(float [][] points) {
    float [][] oldPoints = this.points;
    this.points = deepCopy(points);
    this.shapeCache = null;
    this.areaCache  = null;
    this.propertyChangeSupport.firePropertyChange(Property.POINTS.name(), oldPoints, points);
  }

  /**
   * Adds a point at the end of room points.
   * @since 2.0
   */
  public void addPoint(float x, float y) {
    addPoint(x, y, this.points.length);
  }
  
  /**
   * Adds a point at the given <code>index</code>.
   * @throws IndexOutOfBoundsException if <code>index</code> is negative or > <code>getPointCount()</code> 
   * @since 2.0
   */
  public void addPoint(float x, float y, int index) {
    if (index < 0 || index > this.points.length) {
      throw new IndexOutOfBoundsException("Invalid index " + index);
    }
    
    float [][] newPoints = new float [this.points.length + 1][];
    System.arraycopy(this.points, 0, newPoints, 0, index);
    newPoints [index] = new float [] {x, y};
    System.arraycopy(this.points, index, newPoints, index + 1, this.points.length - index);
    
    float [][] oldPoints = this.points;
    this.points = newPoints;
    this.shapeCache = null;
    this.areaCache  = null;
    this.propertyChangeSupport.firePropertyChange(Property.POINTS.name(), oldPoints, deepCopy(this.points));
  }
  
  /**
   * Sets the point at the given <code>index</code>.
   * @throws IndexOutOfBoundsException if <code>index</code> is negative or >= <code>getPointCount()</code> 
   * @since 2.0
   */
  public void setPoint(float x, float y, int index) {
    if (index < 0 || index >= this.points.length) {
      throw new IndexOutOfBoundsException("Invalid index " + index);
    }
    if (this.points [index][0] != x 
        || this.points [index][1] != y) {
      float [][] oldPoints = this.points;
      this.points = deepCopy(this.points);
      this.points [index][0] = x;
      this.points [index][1] = y;
      this.shapeCache = null;
      this.areaCache  = null;
      this.propertyChangeSupport.firePropertyChange(Property.POINTS.name(), oldPoints, deepCopy(this.points));
    }
  }
  
  /**
   * Removes the point at the given <code>index</code>.
   * @throws IndexOutOfBoundsException if <code>index</code> is negative or >= <code>getPointCount()</code> 
   * @since 2.0
   */
  public void removePoint(int index) {
    if (index < 0 || index >= this.points.length) {
      throw new IndexOutOfBoundsException("Invalid index " + index);
    }
    
    float [][] newPoints = new float [this.points.length - 1][];
    System.arraycopy(this.points, 0, newPoints, 0, index);
    System.arraycopy(this.points, index + 1, newPoints, index, this.points.length - index - 1);
    
    float [][] oldPoints = this.points;
    this.points = newPoints;
    this.shapeCache = null;
    this.areaCache  = null;
    this.propertyChangeSupport.firePropertyChange(Property.POINTS.name(), oldPoints, deepCopy(this.points));
  }
  
  /**
   * Returns whether the area of this room is visible or not. 
   */
  public boolean isAreaVisible() {
    return this.areaVisible;  
  }
  
  /**
   * Sets whether the area of this room is visible or not. Once this room 
   * is updated, listeners added to this room will receive a change notification.
   */
  public void setAreaVisible(boolean areaVisible) {
    if (areaVisible != this.areaVisible) {
      this.areaVisible = areaVisible;
      this.propertyChangeSupport.firePropertyChange(Property.AREA_VISIBLE.name(), !areaVisible, areaVisible);
    }
  }
  
  /**
   * Returns the distance along x axis applied to room center abscissa 
   * to display room area. 
   */
  public float getAreaXOffset() {
    return this.areaXOffset;  
  }
  
  /**
   * Sets the distance along x axis applied to room center abscissa to display room area. 
   * Once this room  is updated, listeners added to this room will receive a change notification.
   */
  public void setAreaXOffset(float areaXOffset) {
    if (areaXOffset != this.areaXOffset) {
      float oldAreaXOffset = this.areaXOffset;
      this.areaXOffset = areaXOffset;
      this.propertyChangeSupport.firePropertyChange(Property.AREA_X_OFFSET.name(), oldAreaXOffset, areaXOffset);
    }
  }
  
  /**
   * Returns the distance along y axis applied to room center ordinate 
   * to display room area.
   */
  public float getAreaYOffset() {
    return this.areaYOffset;  
  }

  /**
   * Sets the distance along y axis applied to room center ordinate to display room area. 
   * Once this room is updated, listeners added to this room will receive a change notification.
   */
  public void setAreaYOffset(float areaYOffset) {
    if (areaYOffset != this.areaYOffset) {
      float oldAreaYOffset = this.areaYOffset;
      this.areaYOffset = areaYOffset;
      this.propertyChangeSupport.firePropertyChange(Property.AREA_Y_OFFSET.name(), oldAreaYOffset, areaYOffset);
    }
  }
  
  /**
   * Returns the text style used to display room area.
   */
  public TextStyle getAreaStyle() {
    return this.areaStyle;  
  }

  /**
   * Sets the text style used to display room area.
   * Once this room is updated, listeners added to this room will receive a change notification.
   */
  public void setAreaStyle(TextStyle areaStyle) {
    if (areaStyle != this.areaStyle) {
      TextStyle oldAreaStyle = this.areaStyle;
      this.areaStyle = areaStyle;
      this.propertyChangeSupport.firePropertyChange(Property.AREA_STYLE.name(), oldAreaStyle, areaStyle);
    }
  }
  
  /**
   * Returns the abscissa of the center point of this room.
   */
  public float getXCenter() {
    float xMin = this.points [0][0]; 
    float xMax = this.points [0][0]; 
    for (int i = 1; i < this.points.length; i++) {
      xMin = Math.min(xMin, this.points [i][0]);
      xMax = Math.max(xMax, this.points [i][0]);
    }
    return (xMin + xMax) / 2;
  }
  
  /**
   * Returns the ordinate of the center point of this room.
   */
  public float getYCenter() {
    float yMin = this.points [0][1]; 
    float yMax = this.points [0][1]; 
    for (int i = 1; i < this.points.length; i++) {
      yMin = Math.min(yMin, this.points [i][1]);
      yMax = Math.max(yMax, this.points [i][1]);
    }
    return (yMin + yMax) / 2;
  }
  
  /**
   * Returns the floor color color of this room. 
   */
  public Integer getFloorColor() {
    return this.floorColor;
  }

  /**
   * Sets the floor color of this room. Once this room is updated, 
   * listeners added to this room will receive a change notification.
   */
  public void setFloorColor(Integer floorColor) {
    if (floorColor != this.floorColor
        || (floorColor != null && !floorColor.equals(this.floorColor))) {
      Integer oldFloorColor = this.floorColor;
      this.floorColor = floorColor;
      this.propertyChangeSupport.firePropertyChange(Property.FLOOR_COLOR.name(), 
          oldFloorColor, floorColor);
    }
  }

  /**
   * Returns the floor texture of this room.
   */
  public HomeTexture getFloorTexture() {
    return this.floorTexture;
  }

  /**
   * Sets the floor texture of this room. Once this room is updated, 
   * listeners added to this room will receive a change notification.
   */
  public void setFloorTexture(HomeTexture floorTexture) {
    if (floorTexture != this.floorTexture
        || (floorTexture != null && !floorTexture.equals(this.floorTexture))) {
      HomeTexture oldFloorTexture = this.floorTexture;
      this.floorTexture = floorTexture;
      this.propertyChangeSupport.firePropertyChange(Property.FLOOR_TEXTURE.name(), 
          oldFloorTexture, floorTexture);
    }
  }

  /**
   * Returns whether the floor of this room is visible or not. 
   */
  public boolean isFloorVisible() {
    return this.floorVisible;  
  }
  
  /**
   * Sets whether the floor of this room is visible or not. Once this room 
   * is updated, listeners added to this room will receive a change notification.
   */
  public void setFloorVisible(boolean floorVisible) {
    if (floorVisible != this.floorVisible) {
      this.floorVisible = floorVisible;
      this.propertyChangeSupport.firePropertyChange(Property.FLOOR_VISIBLE.name(), !floorVisible, floorVisible);
    }
  }
  
  /**
   * Returns the ceiling color color of this room. 
   */
  public Integer getCeilingColor() {
    return this.ceilingColor;
  }

  /**
   * Sets the ceiling color of this room. Once this room is updated, 
   * listeners added to this room will receive a change notification.
   */
  public void setCeilingColor(Integer ceilingColor) {
    if (ceilingColor != this.ceilingColor
        || (ceilingColor != null && !ceilingColor.equals(this.ceilingColor))) {
      Integer oldCeilingColor = this.ceilingColor;
      this.ceilingColor = ceilingColor;
      this.propertyChangeSupport.firePropertyChange(Property.CEILING_COLOR.name(), 
          oldCeilingColor, ceilingColor);
    }
  }

  /**
   * Returns the ceiling texture of this room.
   */
  public HomeTexture getCeilingTexture() {
    return this.ceilingTexture;
  }

  /**
   * Sets the ceiling texture of this room. Once this room is updated, 
   * listeners added to this room will receive a change notification.
   */
  public void setCeilingTexture(HomeTexture ceilingTexture) {
    if (ceilingTexture != this.ceilingTexture
        || (ceilingTexture != null && !ceilingTexture.equals(this.ceilingTexture))) {
      HomeTexture oldCeilingTexture = this.ceilingTexture;
      this.ceilingTexture = ceilingTexture;
      this.propertyChangeSupport.firePropertyChange(Property.CEILING_TEXTURE.name(), 
          oldCeilingTexture, ceilingTexture);
    }
  }

  /**
   * Returns whether the ceiling of this room is visible or not. 
   */
  public boolean isCeilingVisible() {
    return this.ceilingVisible;  
  }
  
  /**
   * Sets whether the ceiling of this room is visible or not. Once this room 
   * is updated, listeners added to this room will receive a change notification.
   */
  public void setCeilingVisible(boolean ceilingVisible) {
    if (ceilingVisible != this.ceilingVisible) {
      this.ceilingVisible = ceilingVisible;
      this.propertyChangeSupport.firePropertyChange(Property.CEILING_VISIBLE.name(), !ceilingVisible, ceilingVisible);
    }
  }
  
  /**
   * Returns the area of this room.
   */
  public float getArea() {
    if (this.areaCache == null) {
      Area roomArea = new Area(getShape());
      if (roomArea.isSingular()) {
        this.areaCache = Math.abs(getSignedArea(getPoints()));
      } else {
        // Add the surface of the different polygons of this room
        float area = 0;
        List<float []> currentPathPoints = new ArrayList<float[]>();
        for (PathIterator it = roomArea.getPathIterator(null); !it.isDone(); ) {
          float [] roomPoint = new float[2];
          switch (it.currentSegment(roomPoint)) {
            case PathIterator.SEG_MOVETO : 
              currentPathPoints.add(roomPoint);
              break;
            case PathIterator.SEG_LINETO : 
              currentPathPoints.add(roomPoint);
              break;
            case PathIterator.SEG_CLOSE :
              float [][] pathPoints = 
                  currentPathPoints.toArray(new float [currentPathPoints.size()][]);
              area += Math.abs(getSignedArea(pathPoints));
              currentPathPoints.clear();
              break;
          }
          it.next();        
        }
        this.areaCache = area;
      }
    }
    return this.areaCache;
  }
  
  private float getSignedArea(float areaPoints [][]) {
    // From "Area of a General Polygon" algorithm described in  
    // http://www.davidchandler.com/AreaOfAGeneralPolygon.pdf
    float area = 0;
    for (int i = 1; i < areaPoints.length; i++) {
      area += areaPoints [i][0] * areaPoints [i - 1][1];
      area -= areaPoints [i][1] * areaPoints [i - 1][0];
    }
    area += areaPoints [0][0] * areaPoints [areaPoints.length - 1][1];
    area -= areaPoints [0][1] * areaPoints [areaPoints.length - 1][0];
    return area / 2;
  }
  
  /**
   * Returns <code>true</code> if the points of this room are in clockwise order.
   */
  public boolean isClockwise() {
    return getSignedArea(getPoints()) < 0;
  }
  
  /**
   * Returns <code>true</code> if this room is comprised of only one polygon.
   */
  public boolean isSingular() {
    return new Area(getShape()).isSingular();
  }
  
  /**
   * Returns <code>true</code> if this room intersects
   * with the horizontal rectangle which opposite corners are at points
   * (<code>x0</code>, <code>y0</code>) and (<code>x1</code>, <code>y1</code>).
   */
  public boolean intersectsRectangle(float x0, float y0, float x1, float y1) {
    Rectangle2D rectangle = new Rectangle2D.Float(x0, y0, 0, 0);
    rectangle.add(x1, y1);
    return getShape().intersects(rectangle);
  }
  
  /**
   * Returns <code>true</code> if this room contains 
   * the point at (<code>x</code>, <code>y</code>) with a given <code>margin</code>.
   */
  public boolean containsPoint(float x, float y, float margin) {
    return containsShapeAtWithMargin(getShape(), x, y, margin);
  }
  
  /**
   * Returns the index of the point of this room equal to 
   * the point at (<code>x</code>, <code>y</code>) with a given <code>margin</code>.
   * @return the index of the first found point or -1.
   */
  public int getPointIndexAt(float x, float y, float margin) {
    for (int i = 0; i < this.points.length; i++) {
      if (Math.abs(x - this.points [i][0]) <= margin && Math.abs(y - this.points [i][1]) <= margin) {
        return i;
      }
    }
    return -1;
  }
  
  /**
   * Returns <code>true</code> if the center point at which is displayed the name 
   * of this room is equal to the point at (<code>x</code>, <code>y</code>) 
   * with a given <code>margin</code>. 
   */
  public boolean isNameCenterPointAt(float x, float y, float margin) {
    return Math.abs(x - getXCenter() - getNameXOffset()) <= margin 
        && Math.abs(y - getYCenter() - getNameYOffset()) <= margin;
  }
  
  /**
   * Returns <code>true</code> if the center point at which is displayed the area 
   * of this room is equal to the point at (<code>x</code>, <code>y</code>) 
   * with a given <code>margin</code>. 
   */
  public boolean isAreaCenterPointAt(float x, float y, float margin) {
    return Math.abs(x - getXCenter() - getAreaXOffset()) <= margin 
        && Math.abs(y - getYCenter() - getAreaYOffset()) <= margin;
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
   * Returns the shape matching this room.
   */
  private Shape getShape() {
    if (this.shapeCache == null) {
      GeneralPath roomShape = new GeneralPath();
      roomShape.moveTo(this.points [0][0], this.points [0][1]);
      for (int i = 1; i < this.points.length; i++) {
        roomShape.lineTo(this.points [i][0], this.points [i][1]);
      }
      roomShape.closePath();
      // Cache roomShape
      this.shapeCache = roomShape;
    }
    return this.shapeCache;
  }

  /**
   * Moves this room of (<code>dx</code>, <code>dy</code>) units.
   */
  public void move(float dx, float dy) {
    if (dx != 0 || dy != 0) {
      float [][] points = getPoints();
      for (int i = 0; i < points.length; i++) {
        points [i][0] += dx;
        points [i][1] += dy;
      }
      updatePoints(points);
    }
  }
  
  /**
   * Returns a clone of this room.
   */
  @Override
  public Room clone() {
    try {
      Room clone = (Room)super.clone();
      clone.propertyChangeSupport = new PropertyChangeSupport(clone);
      return clone;
    } catch (CloneNotSupportedException ex) {
      throw new IllegalStateException("Super class isn't cloneable"); 
    }
  }
}
