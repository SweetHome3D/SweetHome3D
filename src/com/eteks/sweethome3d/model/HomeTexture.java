/*
 * HomeTexture.java 5 oct. 07
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

/**
 * An image used as texture on home 3D objects.
 * @author Emmanuel Puybaret
 */
public class HomeTexture implements TextureImage {
  private static final long serialVersionUID = 1L;
  
  private final String name;
  private final Content image;
  private final float width;
  private final float height;
  
  /**
   * Creates a home texture from an existing one.
   * @param texture the texture from which data are copied
   */
  public HomeTexture(TextureImage texture) {
    this.name = texture.getName();
    this.image = texture.getImage();
    this.width = texture.getWidth();
    this.height = texture.getHeight();
  }
  
  /**
   * Returns the name of this texture.
   */
  public String getName() {
    return this.name;
  }
  
  /**
   * Returns the content of the image used for this texture. 
   */
  public Content getImage() {
    return this.image;
  }
  
  /**
   * Returns the width of the image in centimeters.
   */
  public float getWidth() {
    return this.width;
  }

  /**
   * Returns the height of the image in centimeters.
   */
  public float getHeight() {
    return this.height;
  }
}
