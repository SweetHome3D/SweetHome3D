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

import java.io.Serializable;

/**
 * Camera characteritics in home.
 * @author Emmanuel Puybaret
 */
public class Camera implements Serializable {
  private static final long serialVersionUID = 1L;
  
  private float       x;
  private float       y;
  private float       z;
  private float       yaw;
  private float       pitch;
  
  /**
   * Creates a camera at given location and angles.
   */
  public Camera(float x, float y, float z, float yaw, float pitch) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.yaw = yaw;
    this.pitch = pitch;
  }

  /**
   * Returns the yaw angle in radians of this camera.
   */
  public float getYaw() {
    return this.yaw;
  }

  /**
   * Sets the yaw angle in radians of this camera.
   * This method should be called only from {@link Home}, which
   * controls notifications when a camera changed.
   */
  void setYaw(float yaw) {
    this.yaw = yaw;
  }
  
  /**
   * Returns the pitch angle in radians of this camera.
   */
  public float getPitch() {
    return this.pitch;
  }

  /**
   * Sets the pitch angle in radians of this camera.
   * This method should be called only from {@link Home}, which
   * controls notifications when a camera changed.
   */
  void setPitch(float pitch) {
    this.pitch = pitch;
  }

  /**
   * Returns the abcissa of this camera.
   */
  public float getX() {
    return this.x;
  }

  /**
   * Sets the abcissa of this camera.
   * This method should be called only from {@link Home}, which
   * controls notifications when a camera changed.
   */
  void setX(float x) {
    this.x = x;
  }
  
  /**
   * Returns the ordinate of this camera.
   */
  public float getY() {
    return this.y;
  }

  /**
   * Sets the abcissa of this camera.
   * This method should be called only from {@link Home}, which
   * controls notifications when a camera changed.
   */
  void setY(float y) {
    this.y = y;
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
  void setZ(float z) {
    this.z = z;
  }
}
