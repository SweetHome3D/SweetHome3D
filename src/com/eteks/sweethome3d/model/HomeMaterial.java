/*
 * HomeMaterial.java 19 oct. 2012
 *
 * Sweet Home 3D, Copyright (c) 2012 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
 * The color and other properties of a material.
 * @since 3.8
 * @author Emmanuel Puybaret
 */
public class HomeMaterial implements Serializable {
  private static final long serialVersionUID = 1L;
  
  private final String      name;
  private final Integer     color;
  private final HomeTexture texture;
  private final Float       shininess;
  
  /**
   * Creates a material instance from parameters.
   * @since 3.8
   */
  public HomeMaterial(String name, Integer color, HomeTexture texture, Float shininess) {
    this.name = name;
    this.color = color;
    this.texture = texture;
    this.shininess = shininess;
  }

  /**
   * Returns the name of this material.
   * @return the name of the material or <code>null</code> if material has no name.
   * @since 3.8
   */
  public String getName() {
    return this.name;
  }
  
  /**
   * Returns the color of this material.
   * @return the color of the material as RGB code or <code>null</code> if material color is unchanged.
   * @since 3.8
   */
  public Integer getColor() {
    return this.color;
  }
  
  /**
   * Returns the texture of this material.
   * @return the texture of the material or <code>null</code> if material texture is unchanged.
   * @since 3.8
   */
  public HomeTexture getTexture() {
    return this.texture;
  }

  /**
   * Returns the shininess of this material.
   * @return a value between 0 (matt) and 1 (very shiny) or <code>null</code> if material shininess is unchanged.
   * @since 3.8
   */
  public Float getShininess() {
    return this.shininess;
  }
}
