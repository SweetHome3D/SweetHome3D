/*
 * TexturesCatalog.java 5 oct. 2006
 * 
 * Copyright (c) 2007 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Textures catalog.
 * @author Emmanuel Puybaret
 */
public abstract class TexturesCatalog {
  private List<TexturesCategory>  categories = new ArrayList<TexturesCategory>();
  private boolean                 sorted;
  private final CollectionChangeSupport<CatalogTexture> texturesChangeSupport = 
                                      new CollectionChangeSupport<CatalogTexture>(this);

  /**
   * Returns the categories list sorted by name.
   * @return an unmodifiable list of categories.
   */
  public List<TexturesCategory> getCategories() {
    checkCategoriesSorted();
    return Collections.unmodifiableList(this.categories);
  }

  /**
   * Checks categories are sorted.
   */
  private void checkCategoriesSorted() {
    if (!this.sorted) {
      Collections.sort(this.categories);
      this.sorted = true;
    }
  }

  /**
   * Returns the count of categories in this catalog.
   */
  public int getCategoriesCount() {
    return this.categories.size();
  }

  /**
   * Returns the category at a given <code>index</code>.
   */
  public TexturesCategory getCategory(int index) {
    checkCategoriesSorted();
    return this.categories.get(index);
  }

  /**
   * Adds the texture <code>listener</code> in parameter to this catalog.
   */
  public void addTexturesListener(CollectionListener<CatalogTexture> listener) {
    this.texturesChangeSupport.addCollectionListener(listener);
  }

  /**
   * Removes the texture <code>listener</code> in parameter from this catalog.
   */
  public void removeTexturesListener(CollectionListener<CatalogTexture> listener) {
    this.texturesChangeSupport.removeCollectionListener(listener);
  }

  /**
   * Adds a category to this catalog.
   * @param category the textures category to add.
   * @throws IllegalHomonymException if a category with same name as the one in
   *           parameter already exists in this catalog.
   */
  private void add(TexturesCategory category) {
    if (this.categories.contains(category)) {
      throw new IllegalHomonymException(
          category.getName() + " already exists in catalog");
    }
    this.categories.add(category);
    this.sorted = false;
  }

  /**
   * Adds <code>texture</code> of a given <code>category</code> to this catalog.
   * Once the <code>texture</code> is added, texture listeners added to this catalog will receive a
   * {@link CollectionListener#collectionChanged(CollectionEvent) collectionChanged} notification.
   * @param category the category of the texture.
   * @param texture  a texture.
   */
  public void add(TexturesCategory category, CatalogTexture texture) {
    int index = this.categories.indexOf(category);
    // If category doesn't exist yet, add it to categories
    if (index == -1) {
      category = new TexturesCategory(category.getName());
      add(category);
    } else {
      category = this.categories.get(index);
    }    
    // Add current texture to category list
    category.add(texture);
    
    this.texturesChangeSupport.fireCollectionChanged(texture, 
        Collections.binarySearch(category.getTextures(), texture), CollectionEvent.Type.ADD);
  }

  /**
   * Deletes the <code>texture</code> from this catalog.
   * If then texture category is empty, it will be removed from the categories of this catalog. 
   * Once the <code>texture</code> is deleted, texture listeners added to this catalog will receive a
   * {@link CollectionListener#collectionChanged(CollectionEvent) collectionChanged} notification.
   * @param texture a texture.
   */
  public void delete(CatalogTexture texture) {
    TexturesCategory category = texture.getCategory();
    // Remove texture from its category
    if (category != null) {
      int pieceIndex = Collections.binarySearch(category.getTextures(), texture);
      if (pieceIndex >= 0) {
        category.delete(texture);
        
        if (category.getTexturesCount() == 0) {
          //  Make a copy of the list to avoid conflicts in the list returned by getCategories
          this.categories = new ArrayList<TexturesCategory>(this.categories);
          this.categories.remove(category);
        }
        
        this.texturesChangeSupport.fireCollectionChanged(texture, pieceIndex, CollectionEvent.Type.DELETE);
        return;
      }
    }

    throw new IllegalArgumentException("catalog doesn't contain texture " + texture.getName());
  }
}
