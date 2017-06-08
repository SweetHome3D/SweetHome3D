/*
 * Label.java 28 nov. 2008
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

import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * A free label.
 * @author Emmanuel Puybaret
 */
public class Label extends HomeObject implements Selectable, Elevatable {
  private static final long serialVersionUID = 1L;
  
  private static final double TWICE_PI = 2 * Math.PI;

  /**
   * The properties of a label that may change. <code>PropertyChangeListener</code>s added 
   * to a label will be notified under a property name equal to the string value of one these properties.
   */
  public enum Property {TEXT, X, Y, ELEVATION, STYLE, COLOR, OUTLINE_COLOR, ANGLE, PITCH, LEVEL};
  
  private String              text;
  private float               x;
  private float               y;
  private TextStyle           style;
  private Integer             color;
  private Integer             outlineColor;
  private float               angle;
  private Float               pitch;
  private float               elevation;
  private Level               level;
  
  private transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

  public Label(String text, float x, float y) {
    this.text = text;
    this.x = x;
    this.y = y;
  }
  
  /**
   * Initializes transient fields to their default values 
   * and reads label from <code>in</code> stream with default reading method.
   */
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    this.propertyChangeSupport = new PropertyChangeSupport(this);
    in.defaultReadObject();
  }

  /**
   * Adds the property change <code>listener</code> in parameter to this label.
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    this.propertyChangeSupport.addPropertyChangeListener(listener);
  }

  /**
   * Removes the property change <code>listener</code> in parameter from this label.
   */
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    this.propertyChangeSupport.removePropertyChangeListener(listener);
  }

  /**
   * Returns the text of this label.
   */
  public String getText() {
    return this.text;
  }

  /**
   * Sets the text of this label. Once this label is updated, 
   * listeners added to this label will receive a change notification.
   */
  public void setText(String text) {
    if (text != this.text
        && (text == null || !text.equals(this.text))) {
      String oldText = this.text;
      this.text = text;
      this.propertyChangeSupport.firePropertyChange(Property.TEXT.name(), oldText, text);
    }
  }
   
  /**
   * Returns the abscissa of the text of this label.
   */
  public float getX() {
    return this.x;
  }

  /**
   * Sets the abscissa of the text of this label. Once this label is updated, 
   * listeners added to this label will receive a change notification.
   */
  public void setX(float x) {
    if (x != this.x) {
      float oldX = this.x;
      this.x = x;
      this.propertyChangeSupport.firePropertyChange(Property.X.name(), oldX, x);
    }
  }
  
  /**
   * Returns the ordinate of the text of this label.
   */
  public float getY() {
    return this.y;
  }

  /**
   * Sets the ordinate of the text of this label. Once this label is updated, 
   * listeners added to this label will receive a change notification.
   */
  public void setY(float y) {
    if (y != this.y) {
      float oldY = this.y;
      this.y = y;
      this.propertyChangeSupport.firePropertyChange(Property.Y.name(), oldY, y);
    }
  }

  /**
   * Returns the elevation of this label 
   * from the ground according to the elevation of its level.
   * @since 5.0
   */
  public float getGroundElevation() {
    if (this.level != null) {
      return this.elevation + this.level.getElevation();
    } else {
      return this.elevation;
    }
  }

  /**
   * Returns the elevation of this label on its level.
   * @see #getPitch() 
   * @since 5.0 
   */
  public float getElevation() {
    return this.elevation;
  }

  /**
   * Sets the elevation of this label on its level. Once this label is updated, 
   * listeners added to this label will receive a change notification.
   * @since 5.0 
   */
  public void setElevation(float elevation) {
    if (elevation != this.elevation) {
      float oldElevation = this.elevation;
      this.elevation = elevation;
      this.propertyChangeSupport.firePropertyChange(Property.ELEVATION.name(), oldElevation, elevation);
    }
  }

  /**
   * Returns the style used to display the text of this label.
   */
  public TextStyle getStyle() {
    return this.style;  
  }

  /**
   * Sets the style used to display the text of this label.
   * Once this label is updated, listeners added to this label will receive a change notification.
   */
  public void setStyle(TextStyle style) {
    if (style != this.style) {
      TextStyle oldStyle = this.style;
      this.style = style;
      this.propertyChangeSupport.firePropertyChange(Property.STYLE.name(), oldStyle, style);
    }
  }

  /**
   * Returns the color used to display the text of this label.
   * @since 5.0
   */
  public Integer getColor() {
    return this.color;  
  }

  /**
   * Sets the color used to display the text of this label.
   * Once this label is updated, listeners added to this label will receive a change notification.
   * @since 5.0
   */
  public void setColor(Integer color) {
    if (color != this.color) {
      Integer oldColor = this.color;
      this.color = color;
      this.propertyChangeSupport.firePropertyChange(Property.COLOR.name(), oldColor, color);
    }
  }

  /**
   * Returns the color used to outline the text of this label.
   * @since 5.3
   */
  public Integer getOutlineColor() {
    return this.outlineColor;  
  }

  /**
   * Sets the color used to outline the text of this label.
   * Once this label is updated, listeners added to this label will receive a change notification.
   * @since 5.3
   */
  public void setOutlineColor(Integer outlineColor) {
    if (outlineColor != this.outlineColor) {
      Integer oldOutlineColor = this.outlineColor;
      this.outlineColor = outlineColor;
      this.propertyChangeSupport.firePropertyChange(Property.OUTLINE_COLOR.name(), oldOutlineColor, outlineColor);
    }
  }

  /**
   * Returns the angle in radians around vertical axis used to display this label.
   * @since 3.6 
   */
  public float getAngle() {
    return this.angle;
  }

  /**
   * Sets the angle in radians around vertical axis used to display this label. Once this label is updated, 
   * listeners added to this label will receive a change notification.
   * @since 3.6 
   */
  public void setAngle(float angle) {
    // Ensure angle is always positive and between 0 and 2 PI
    angle = (float)((angle % TWICE_PI + TWICE_PI) % TWICE_PI);
    if (angle != this.angle) {
      float oldAngle = this.angle;
      this.angle = angle;
      this.propertyChangeSupport.firePropertyChange(Property.ANGLE.name(), oldAngle, angle);
    }
  }
  
  /**
   * Returns the pitch angle in radians used to rotate this label around horizontal axis in 3D.
   * @return an angle in radians or <code>null</code> if the label shouldn't be displayed in 3D.
   *         A pitch angle equal to 0 should make this label fully visible when seen from top. 
   * @since 5.0 
   */
  public Float getPitch() {
    return this.pitch;
  }

  /**
   * Sets the angle in radians used to rotate this label around horizontal axis in 3D. Once this label is updated, 
   * listeners added to this label will receive a change notification.
   * Pitch axis is horizontal transverse axis. 
   * @since 5.0 
   */
  public void setPitch(Float pitch) {
    if (pitch != null) {
      // Ensure pitch is always positive and between 0 and 2 PI
      pitch = (float)((pitch % TWICE_PI + TWICE_PI) % TWICE_PI);
    }
    if (pitch != this.pitch
        && (pitch == null || !pitch.equals(this.pitch))) {
      Float oldPitch = this.pitch;
      this.pitch = pitch;
      this.propertyChangeSupport.firePropertyChange(Property.PITCH.name(), oldPitch, pitch);
    }
  }
  
  /**
   * Returns the level which this label belongs to. 
   * @since 3.4
   */
  public Level getLevel() {
    return this.level;
  }

  /**
   * Sets the level of this label. Once this label is updated, 
   * listeners added to this label will receive a change notification.
   * @since 3.4
   */
  public void setLevel(Level level) {
    if (level != this.level) {
      Level oldLevel = this.level;
      this.level = level;
      this.propertyChangeSupport.firePropertyChange(Property.LEVEL.name(), oldLevel, level);
    }
  }

  /**
   * Returns <code>true</code> if this label is at the given <code>level</code> 
   * or at a level with the same elevation and a smaller elevation index.
   * @since 3.4
   */
  public boolean isAtLevel(Level level) {
    return this.level == level
        || this.level != null && level != null
           && this.level.getElevation() == level.getElevation()
           && this.level.getElevationIndex() < level.getElevationIndex();
  }
  
  /**
   * Returns the point of this label.
   * @return an array of the (x,y) coordinates of this label.
   */
  public float [][] getPoints() {
    return new float [][] {{this.x, this.y}};
  }
  
  /**
   * Returns <code>true</code> if the point of this label is contained
   * in the horizontal rectangle which opposite corners are at points
   * (<code>x0</code>, <code>y0</code>) and (<code>x1</code>, <code>y1</code>).
   */
  public boolean intersectsRectangle(float x0, float y0, 
                                     float x1, float y1) {
    Rectangle2D rectangle = new Rectangle2D.Float(x0, y0, 0, 0);
    rectangle.add(x1, y1);
    return rectangle.contains(this.x, this.y);
  }
  
  /**
   * Returns <code>true</code> if this text is at the point at (<code>x</code>, <code>y</code>)
   * with a given <code>margin</code>.
   */
  public boolean containsPoint(float x, float y, float margin) {
    return Math.abs(x - this.x) <= margin && Math.abs(y - this.y) <= margin;
  }
  
  /**
   * Moves this label of (<code>dx</code>, <code>dy</code>) units.
   */
  public void move(float dx, float dy) {
    setX(getX() + dx);
    setY(getY() + dy);
  }
  
  /**
   * Returns a clone of this label.
   */
  @Override
  public Label clone() {
    Label clone = (Label)super.clone();
    clone.propertyChangeSupport = new PropertyChangeSupport(clone);
    clone.level = null;
    return clone;
  }
}
