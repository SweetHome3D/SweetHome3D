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
  private List<Category>                categories        = new ArrayList<Category>();
  private boolean                       sorted;
  private List<CatalogPieceOfFurniture> selectedFurniture = Collections.emptyList();
  private List<SelectionListener>       selectionListeners = new ArrayList<SelectionListener>();
  private List<FurnitureListener>       furnitureListeners = new ArrayList<FurnitureListener>();

  /**
   * Returns the catagories list sorted by name.
   * @return an unmodifiable list of catagories.
   */
  public List<Category> getCategories() {
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
   * Returns the count of catagories in this catalog.
   */
  public int getCategoriesCount() {
    return this.categories.size();
  }

  /**
   * Returns the category at a given <code>index</code>.
   */
  public Category getCategory(int index) {
    checkCategoriesSorted();
    return this.categories.get(index);
  }

  /**
   * Adds the furniture <code>listener</code> in parameter to this home.
   */
  public void addFurnitureListener(FurnitureListener listener) {
    this.furnitureListeners.add(listener);
  }

  /**
   * Removes the furniture <code>listener</code> in parameter from this home.
   */
  public void removeFurnitureListener(FurnitureListener listener) {
    this.furnitureListeners.remove(listener);
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
   * Once the <code>piece</code> is added, furniture listeners added to this catalog will receive a
   * {@link FurnitureListener#pieceOfFurnitureChanged(FurnitureEvent) pieceOfFurnitureChanged}
   * notification.
   * @param category the category of the piece.
   * @param piece    a piece of furniture.
   */
  public void add(Category category, CatalogPieceOfFurniture piece) {
    int index = this.categories.indexOf(category);
    // If category doesn't exist yet, add it to catagories
    if (index == -1) {
      category = new Category(category.getName());
      add(category);
    } else {
      category = this.categories.get(index);
    }    
    // Add current piece of furniture to category list
    category.add(piece);
    
    firePieceOfFurnitureChanged(piece, 
        Collections.binarySearch(category.getFurniture(), piece), FurnitureEvent.Type.ADD);
  }

  /**
   * Deletes the <code>piece</code> from this catalog.
   * If then piece category is empty, it will be removed from the categories of this catalog. 
   * Once the <code>piece</code> is deleted, furniture listeners added to this home will receive a
   * {@link FurnitureListener#pieceOfFurnitureChanged(FurnitureEvent) pieceOfFurnitureChanged}
   * notification.
   * @param piece a piece of furniture in that category.
   */
  public void delete(CatalogPieceOfFurniture piece) {
    Category category = piece.getCategory();
    // Remove piece from its category
    if (category != null) {
      int pieceIndex = Collections.binarySearch(category.getFurniture(), piece);
      if (pieceIndex >= 0) {
        // Ensure selectedFurniture don't keep a reference to piece
        deselectPieceOfFurniture(piece);
        category.delete(piece);
        
        if (category.getFurniture().size() == 0) {
          //  Make a copy of the list to avoid conflicts in the list returned by getCategories
          this.categories = new ArrayList<Category>(this.categories);
          this.categories.remove(category);
        }
        
        firePieceOfFurnitureChanged(piece, pieceIndex, FurnitureEvent.Type.DELETE);
        return;
      }
    }

    throw new IllegalArgumentException(
        "catalog doesn't contain piece " + piece.getName());
  }

  private void firePieceOfFurnitureChanged(CatalogPieceOfFurniture piece, int index,
                                           FurnitureEvent.Type eventType) {
    if (!this.furnitureListeners.isEmpty()) {
      FurnitureEvent furnitureEvent = 
          new FurnitureEvent(this, piece, index, eventType);
      // Work on a copy of furnitureListeners to ensure a listener 
      // can modify safely listeners list
      FurnitureListener [] listeners = this.furnitureListeners.
        toArray(new FurnitureListener [this.furnitureListeners.size()]);
      for (FurnitureListener listener : listeners) {
        listener.pieceOfFurnitureChanged(furnitureEvent);
      }
    }
  }

  /**
   * Adds the selection <code>listener</code> in parameter to this home.
   */
  public void addSelectionListener(SelectionListener listener) {
    this.selectionListeners.add(listener);
  }

  /**
   * Removes the selection <code>listener</code> in parameter from this home.
   */
  public void removeSelectionListener(SelectionListener listener) {
    this.selectionListeners.remove(listener);
  }
  
  /**
   * Returns an unmodifiable list of the selected furniture in catalog.
   */
  public List<CatalogPieceOfFurniture> getSelectedFurniture() {
    return Collections.unmodifiableList(this.selectedFurniture);
  }
  
  /**
   * Sets the selected items in home and notifies listeners selection change.
   */
  public void setSelectedFurniture(List<CatalogPieceOfFurniture> selectedFurniture) {
    this.selectedFurniture = new ArrayList<CatalogPieceOfFurniture>(selectedFurniture);
    if (!this.selectionListeners.isEmpty()) {
      SelectionEvent selectionEvent = new SelectionEvent(this, getSelectedFurniture());
      // Work on a copy of selectionListeners to ensure a listener 
      // can modify safely listeners list
      SelectionListener [] listeners = this.selectionListeners.
        toArray(new SelectionListener [this.selectionListeners.size()]);
      for (SelectionListener listener : listeners) {
        listener.selectionChanged(selectionEvent);
      }
    }
  }

  /**
   * Removes <code>piece</code> from selected furniture.
   */
  private void deselectPieceOfFurniture(CatalogPieceOfFurniture piece) {
    int pieceSelectionIndex = this.selectedFurniture.indexOf(piece);
    if (pieceSelectionIndex != -1) {
      List<CatalogPieceOfFurniture> selectedItems = 
          new ArrayList<CatalogPieceOfFurniture>(getSelectedFurniture());
      selectedItems.remove(pieceSelectionIndex);
      setSelectedFurniture(selectedItems);
    }
  }
}
