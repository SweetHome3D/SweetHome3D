/*
 * Camera.java 16 juin 07
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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * Camera characteristics in home.
 * @author Emmanuel Puybaret
 */
public class Camera implements Serializable {
  /**
   * The properties of a camera that may change. <code>PropertyChangeListener</code>s added 
   * to a camera will be notified under a property name equal to the string value of one these properties.
   */
  public enum Property {X, Y, Z, YAW, PITCH, FIELD_OF_VIEW}
  
  private static final long serialVersionUID = 1L;
  
  private float       x;
  private float       y;
  private float       z;
  private float       yaw;
  private float       pitch;
  private float       fieldOfView;
  
  private transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

  /**
   * Creates a camera at given location and angles.
   */
  public Camera(float x, float y, float z, float yaw, float pitch, float fieldOfView) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.yaw = yaw;
    this.pitch = pitch;
    this.fieldOfView = fieldOfView;
  }

  /**
   * Initializes new camera transient fields  
   * and reads its properties from <code>in</code> stream with default reading method.
   */
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    this.propertyChangeSupport = new PropertyChangeSupport(this);
    in.defaultReadObject();
  }

  /**
   * Adds the property change <code>listener</code> in parameter to this camera.
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    this.propertyChangeSupport.addPropertyChangeListener(listener);
  }

  /**
   * Removes the property change <code>listener</code> in parameter from this camera.
   */
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    this.propertyChangeSupport.removePropertyChangeListener(listener);
  }

  /**
   * Returns the yaw angle in radians of this camera.
   */
  public float getYaw() {
    return this.yaw;
  }

  /**
   * Sets the yaw angle in radians of this camera.
   */
  public void setYaw(float yaw) {
    if (yaw != this.yaw) {
      float oldYaw = this.yaw;
      this.yaw = yaw;
      this.propertyChangeSupport.firePropertyChange(Property.YAW.name(), oldYaw, yaw);
    }
  }
  
  /**
   * Returns the pitch angle in radians of this camera.
   */
  public float getPitch() {
    return this.pitch;
  }

  /**
   * Sets the pitch angle in radians of this camera.
   */
  public void setPitch(float pitch) {
    if (pitch != this.pitch) {
      float oldPitch = this.pitch;
      this.pitch = pitch;
      this.propertyChangeSupport.firePropertyChange(Property.PITCH.name(), oldPitch, pitch);
    }
  }

  /**
   * Returns the field of view. in radians of this camera.
   */
  public float getFieldOfView() {
    return this.fieldOfView;
  }

  /**
   * Sets the field of view in radians of this camera.
   */
  public void setFieldOfView(float fieldOfView) {
    if (fieldOfView != this.fieldOfView) {
      float oldFieldOfView = this.fieldOfView;
      this.fieldOfView = fieldOfView;
      this.propertyChangeSupport.firePropertyChange(Property.FIELD_OF_VIEW.name(), oldFieldOfView, fieldOfView);
    }
  }

  /**
   * Returns the abcissa of this camera.
   */
  public float getX() {
    return this.x;
  }

  /**
   * Sets the abcissa of this camera.
   */
  public void setX(float x) {
    if (x != this.x) {
      float oldX = this.x;
      this.x = x;
      this.propertyChangeSupport.firePropertyChange(Property.X.name(), oldX, x);
    }
  }
  
  /**
   * Returns the ordinate of this camera.
   */
  public float getY() {
    return this.y;
  }

  /**
   * Sets the ordinate of this camera.
   * This method should be called only from {@link Home}, which
   * controls notifications when a camera changed.
   */
  public void setY(float y) {
    if (y != this.y) {
      float oldY = this.y;
      this.y = y;
      this.propertyChangeSupport.firePropertyChange(Property.Y.name(), oldY, y);
    }
  }
  
  /**
   * Returns the elevation of this camera.
   */
  public float getZ() {
    return this.z;
  }
  
  /**
   * Sets the elevation of this camera.
   * This method should be called only from {@link Home}, which
   * controls notifications when a camera changed.
   */
  public void setZ(float z) {
    if (z != this.z) {
      float oldZ = this.z;
      this.z = z;
      this.propertyChangeSupport.firePropertyChange(Property.Z.name(), oldZ, z);
    }
  }
}
