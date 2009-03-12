/*
 * HomeLight.java 12 mars 2009
 *
 * Copyright (c) 2009 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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

  private final LightSource [] lightSources;

  /**
   * Creates a home light from an existing one.
   * @param light the light from which data are copied
   */
  public HomeLight(Light light) {
    super(light);
    this.lightSources = light.getLightSources();
  }

  /**
   * Returns the sources managed by this light. Each light source point
   * is a percentage of the width, the depth and the height of this light.  
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
   * Returns a clone of this light.
   */
  @Override
  public HomeLight clone() {
    return (HomeLight)super.clone();
  }
}
