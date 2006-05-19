/*
 * Catalog.java 7 avr. 2006
 * 
 * Copyright (c) 2006 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights
 * Reserved.
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
 * Furniture catalog.
 * @author Emmanuel Puybaret
 */
public abstract class Catalog {
  private List<Category> categories = new ArrayList<Category>();
  private boolean        sorted;

  /**
   * Returns the catagories list sorted by name.
   * @return an unmodifiable list of catagories.
   */
  public List<Category> getCategories() {
    if (!this.sorted) {
      Collections.sort(this.categories);
      this.sorted = true;
    }
    return Collections.unmodifiableList(this.categories);
  }

  /**
   * Adds a catagory.
   * @param category the category to add.
   * @throws IllegalArgumentException if a category with same name as the one in
   *           parameter already exists in this catalog.
   */
  private void add(Category category) {
    if (this.categories.contains(category)) {
      throw new IllegalArgumentException(
          category.getName() + " already exists in catalog");
    }
    this.categories.add(category);
    this.sorted = false;
  }

  /**
   * Adds <code>piece</code> of a given <code>category</code> to this catalog.
   * @param category the category of the piece.
   * @param piece    a piece of furniture.
   */
  protected void add(Category category, CatalogPieceOfFurniture piece) {
    int index = this.categories.indexOf(category);
    // If category doesn't exist yet, add it to catagories
    if (index == -1) {
      add(category);
    } else {
      category = this.categories.get(index);
    }    
    // Add current piece of furniture to category list
    category.add(piece);
  }
}
