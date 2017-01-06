/*
 * Baseboard.java 13 mai 2015
 *
 * Sweet Home 3D, Copyright (c) 2015 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * A baseboard associated to wall.
 * @author Emmanuel Puybaret
 */
public class Baseboard implements Serializable {
  private static final long serialVersionUID = 1L;
  
  private final float       thickness;
  private final float       height;
  private final Integer     color;
  private final HomeTexture texture;
  
  private static final List<WeakReference<Baseboard>> baseboardsCache = new ArrayList<WeakReference<Baseboard>>(); 

  /**
   * Creates a baseboard.
   */
  public Baseboard(float thickness, float height, Integer color, HomeTexture texture) {
    this(height, thickness, color, texture, true);
  }

  private Baseboard(float thickness, float height, Integer color, HomeTexture texture, boolean cached) {
    this.height = height;
    this.thickness = thickness;
    this.color = color;
    this.texture = texture;
    
    if (cached) {
      baseboardsCache.add(new WeakReference<Baseboard>(this));
    }
  }

  /**
   * Reads properties and updates cache.
   */
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    
    baseboardsCache.add(new WeakReference<Baseboard>(this));
  }

  /**
   * Returns an instance of this class matching the given parameters.
   */
  public static Baseboard getInstance(float thickness, float height, 
                                      Integer color, HomeTexture texture) {
    Baseboard baseboard = new Baseboard(thickness, height, color, texture, false);
    for (int i = baseboardsCache.size() - 1; i >= 0; i--) {
      Baseboard cachedBaseboard = baseboardsCache.get(i).get();
      if (cachedBaseboard == null) {
        baseboardsCache.remove(i);
      } else if (cachedBaseboard.equals(baseboard)) {
        return baseboard;
      }
    }
    baseboardsCache.add(new WeakReference<Baseboard>(baseboard));
    return baseboard;
  }

  /**
   * Returns the thickness of this baseboard.
   */
  public float getThickness() {
    return this.thickness;
  }

  /**
   * Returns the height of this baseboard. 
   */
  public float getHeight() {
    return this.height;
  }

  /**
   * Returns the color of this baseboard.
   */
  public Integer getColor() {
    return this.color;
  }

  /**
   * Returns the texture of this baseboard.
   */
  public HomeTexture getTexture() {
    return this.texture;
  }

  /**
   * Returns <code>true</code> if this baseboard is equal to <code>object</code>.
   */
  @Override
  public boolean equals(Object object) {
    if (object instanceof Baseboard) {
      Baseboard baseboard = (Baseboard)object;
      return baseboard.thickness == this.thickness
          && baseboard.height == this.height
          && (baseboard.color == this.color
              || baseboard.color != null && baseboard.color.equals(this.color))
          && (baseboard.texture == this.texture
              || baseboard.texture != null && baseboard.texture.equals(this.texture));
    }
    return false;
  }
  
  /**
   * Returns a hash code for this baseboard.
   */
  @Override
  public int hashCode() {
    int hashCode = Float.floatToIntBits(this.thickness)
        + Float.floatToIntBits(this.height);
    if (this.color != null) {
      hashCode += this.color.hashCode();
    }
    if (this.texture != null) {
      hashCode += this.texture.hashCode();
    }
    return hashCode;
  }
}
