/*
 * HomeLight.java 12 mars 2009
 *
 * Sweet Home 3D, Copyright (c) 2009 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
 * A light in {@linkplain Home home}.
 * @author Emmanuel Puybaret
 * @since  1.7
 */
public class HomeLight extends HomePieceOfFurniture implements Light {
  private static final long serialVersionUID = 1L;

  /**
   * The properties of a light that may change. <code>PropertyChangeListener</code>s added
   * to a light will be notified under a property name equal to the string value of one these properties.
   */
  public enum Property {POWER};

  private final LightSource [] lightSources;
  private float power;

  /**
   * Creates a home light from an existing one.
   * @param light the light from which data are copied
   */
  public HomeLight(Light light) {
    this(createId("light"), light);
  }

  /**
   * Creates a home light from an existing one.
   * @param id    the ID of the light
   * @param light the light from which data are copied
   * @since 6.4
   */
  public HomeLight(String id, Light light) {
    super(id, light);
    this.lightSources = light.getLightSources();
    this.power = 0.5f;
  }

  /**
   * Returns the sources managed by this light. Each light source point
   * is a percentage of the width, the depth and the height of this light.
   * with the abscissa origin at the left side of the piece,
   * the ordinate origin at the front side of the piece
   * and the elevation origin at the bottom side of the piece.
   * @return a copy of light sources array.
   */
  public LightSource [] getLightSources() {
    if (this.lightSources.length == 0) {
      return this.lightSources;
    } else {
      return this.lightSources.clone();
    }
  }

  /**
   * Returns the power of this light.
   * @since 3.0
   */
  public float getPower() {
    return this.power;
  }

  /**
   * Sets the power of this light. Once this light is updated,
   * listeners added to this piece will receive a change notification.
   * @since 3.0
   */
  public void setPower(float power) {
    if (power != this.power) {
      float oldPower = this.power;
      this.power = power;
      firePropertyChange(Property.POWER.name(), oldPower, power);
    }
  }

  /**
   * Returns a clone of this light.
   */
  @Override
  public HomeLight clone() {
    return (HomeLight)super.clone();
  }
}
