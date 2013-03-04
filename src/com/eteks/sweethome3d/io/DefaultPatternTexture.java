/*
 * DefaultPatternTexture.java 04 mars 2013
 *
 * Sweet Home 3D, Copyright (c) 2006-2013 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
package com.eteks.sweethome3d.io;

import java.io.IOException;
import java.io.ObjectInputStream;

import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.TextureImage;
import com.eteks.sweethome3d.tools.ResourceURLContent;

/**
 * A pattern built from resources.
 * @since 3.3
 */
class DefaultPatternTexture implements TextureImage {
  private static final long serialVersionUID = 1L;

  private final String      name;
  private transient Content image;
  
  public DefaultPatternTexture(String name) {
    this.name = name;
    this.image = new ResourceURLContent(DefaultPatternTexture.class, "resources/patterns/" + this.name + ".png");
  }
  
  /**
   * Initializes transient fields and reads pattern from <code>in</code> stream with default reading method.
   */
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    try {
      this.image = new ResourceURLContent(DefaultPatternTexture.class, "resources/patterns/" + this.name + ".png");
    } catch (IllegalArgumentException ex) {
      this.image = new ResourceURLContent(DefaultPatternTexture.class, "resources/patterns/foreground.png");
    }
  }

  public String getName() {
    return this.name;
  }

  public Content getImage() {
    return this.image;
  }
  
  public float getWidth() {
    return 10;
  }
  
  public float getHeight() {
    return 10;
  }

  /**
   * Returns <code>true</code> if the object in parameter is equal to this pattern.
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (obj instanceof DefaultPatternTexture) {
      DefaultPatternTexture pattern = (DefaultPatternTexture)obj;
      return pattern.name.equals(this.name);
    } else {
      return false;
    }
  }
  
  /**
   * Returns a hash code for this pattern.
   */
  @Override
  public int hashCode() {
    return this.name.hashCode();
  }
}