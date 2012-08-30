/*
 * TexturesCategory.java 5 oct. 2007
 * 
 * Sweet Home 3D, Copyright (c) 2007 Emmanuel PUYBARET / eTeks <info@eteks.com>
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.eteks.sweethome3d.model;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Category of textures.
 * @author Emmanuel Puybaret
 */
public class TexturesCategory implements Comparable<TexturesCategory> {
  private final String         name;
  private List<CatalogTexture> textures;
  private boolean              sorted;
  
  private static final Collator  COMPARATOR = Collator.getInstance();

  /**
   * Create a category.
   * @param name the name of the category.
   */
  public TexturesCategory(String name) {
    this.name = name;
    this.textures = new ArrayList<CatalogTexture>();
  }

  /**
   * Returns the name of this category.
   */
  public String getName() {
    return this.name;
  }

  /**
   * Returns the textures list of this category sorted by name.
   * @return an unmodifiable list of furniture.
   */
  public List<CatalogTexture> getTextures() {
    checkTexturesSorted();
    return Collections.unmodifiableList(this.textures);
  }

  /**
   * Checks textures are sorted.
   */
  private void checkTexturesSorted() {
    if (!this.sorted) {
      Collections.sort(this.textures);
      this.sorted = true;
    }
  }

  /**
   * Returns the count of textures in this category.
   */
  public int getTexturesCount() {
    return this.textures.size();
  }

  /**
   * Returns the texture at a given <code>index</code>.
   */
  public CatalogTexture getTexture(int index) {
    checkTexturesSorted();
    return this.textures.get(index);
  }

  /**
   * Returns the index of the given <code>texture</code>.
   * @since 3.6
   */
  public int getIndexOfTexture(CatalogTexture texture) {
    checkTexturesSorted();
    return this.textures.indexOf(texture);
  }

  /**
   * Adds a texture to this category.
   * @param texture the texture to add.
   */
  void add(CatalogTexture texture) {
    texture.setCategory(this);
    this.textures.add(texture);    
    this.sorted = false;
  }

  /**
   * Deletes a texture from this category.
   * @param texture the texture to remove.
   * @throws IllegalArgumentException if the texture doesn't exist in this category.
   */
  void delete(CatalogTexture texture) {
    int textureIndex = this.textures.indexOf(texture);
    if (textureIndex == -1) {
      throw new IllegalArgumentException(
          this.name + " doesn't contain texture " + texture.getName());
    }
    //  Make a copy of the list to avoid conflicts in the list returned by getTextures
    this.textures = new ArrayList<CatalogTexture>(this.textures);
    this.textures.remove(textureIndex);
  }
  
  /**
   * Returns true if this category and the one in parameter have the same name.
   */
  @Override
  public boolean equals(Object obj) {
    return obj instanceof TexturesCategory
           && COMPARATOR.equals(this.name, ((TexturesCategory)obj).name);
  }

  @Override
  public int hashCode() {
    return this.name.hashCode();
  }

  /**
   * Compares the names of this category and the one in parameter.
   */
  public int compareTo(TexturesCategory category) {
    return COMPARATOR.compare(this.name, category.name);
  }
}
