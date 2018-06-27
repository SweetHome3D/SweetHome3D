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
 * @since 4.0
 * @author Emmanuel Puybaret
 */
public class HomeMaterial implements Serializable {
  private static final long serialVersionUID = 1L;

  private final String      name;
  private final String      key;
  private final Integer     color;
  private final HomeTexture texture;
  private final Float       shininess;

  /**
   * Creates a material instance from parameters.
   * @since 4.0
   */
  public HomeMaterial(String name, Integer color, HomeTexture texture, Float shininess) {
    this(name, null, color, texture, shininess);
  }

  /**
   * Creates a material instance from parameters.
   * @since 5.3
   */
  public HomeMaterial(String name, String key, Integer color, HomeTexture texture, Float shininess) {
    this.name = name;
    this.key = key;
    this.color = color;
    this.texture = texture;
    this.shininess = shininess;
  }

  /**
   * Returns the name of this material.
   * @return the name of the material or <code>null</code> if material has no name.
   * @since 4.0
   */
  public String getName() {
    return this.name;
  }

  /**
   * Returns the key of this material. If not <code>null</code>, this key should be used
   * as the unique identifier to find this material among the ones available on a model,
   * rather than the name of this material.
   * @return the key of the material or <code>null</code> if material has no key.
   * @since 5.3
   */
  public String getKey() {
    return this.key;
  }

  /**
   * Returns the color of this material.
   * @return the color of the material as RGB code or <code>null</code> if material color is unchanged.
   * @since 4.0
   */
  public Integer getColor() {
    return this.color;
  }

  /**
   * Returns the texture of this material.
   * @return the texture of the material or <code>null</code> if material texture is unchanged.
   * @since 4.0
   */
  public HomeTexture getTexture() {
    return this.texture;
  }

  /**
   * Returns the shininess of this material.
   * @return a value between 0 (matt) and 1 (very shiny) or <code>null</code> if material shininess is unchanged.
   * @since 4.0
   */
  public Float getShininess() {
    return this.shininess;
  }

  /**
   * Returns <code>true</code> if this material is equal to <code>object</code>.
   * @since 6.0
   */
  @Override
  public boolean equals(Object object) {
    if (object instanceof HomeMaterial) {
      HomeMaterial material = (HomeMaterial)object;
      return (material.name == this.name
              || (material.name != null && material.name.equals(this.name)))
          && (material.key == this.key
              || (material.key != null && material.key.equals(this.name)))
          && (material.color == this.color
              || (material.color != null && material.color.equals(this.color)))
          && (material.texture == this.texture
              || (material.texture != null && material.texture.equals(this.texture)))
          && (material.shininess == this.shininess
              || (material.shininess != null && material.shininess.equals(this.shininess)));
    }
    return false;
  }

  /**
   * Returns a hash code for this material.
   * @since 6.0
   */
  @Override
  public int hashCode() {
    int hashCode = 0;
    if (this.name != null) {
      hashCode += this.name.hashCode();
    }
    if (this.key != null) {
      hashCode += this.key.hashCode();
    }
    if (this.color != null) {
      hashCode += this.color.hashCode();
    }
    if (this.texture != null) {
      hashCode += this.texture.hashCode();
    }
    if (this.shininess != null) {
      hashCode += this.shininess.hashCode();
    }
    return hashCode;
  }
}
