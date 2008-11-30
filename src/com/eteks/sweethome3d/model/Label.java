/*
 * Label.java 28 nov. 2008
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

import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * A free label.
 * @author Emmanuel Puybaret
 */
public class Label implements Selectable, Serializable {
  private static final long serialVersionUID = 1L;
  
  /**
   * The properties of a label that may change. <code>PropertyChangeListener</code>s added 
   * to a label will be notified under a property name equal to the string value of one these properties.
   */
  public enum Property {TEXT, X, Y, STYLE};
  
  private String    text;
  private float     x;
  private float     y;
  private TextStyle style;
  
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
        || (text != null && !text.equals(this.text))) {
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
    try {
      Label clone = (Label)super.clone();
      clone.propertyChangeSupport = new PropertyChangeSupport(clone);
      return clone;
    } catch (CloneNotSupportedException ex) {
      throw new IllegalStateException("Super class isn't cloneable"); 
    }
  }
}
