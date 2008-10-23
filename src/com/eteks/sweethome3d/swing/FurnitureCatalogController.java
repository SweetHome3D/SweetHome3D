/*
 * FurnitureCatalogController.java 15 mai 2006
 *
 * Copyright (c) 2006 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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
package com.eteks.sweethome3d.swing;

import java.util.List;

import javax.swing.JComponent;

import com.eteks.sweethome3d.model.FurnitureCatalog;
import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * A MVC controller for the furniture catalog.
 * @author Emmanuel Puybaret
 */
public class FurnitureCatalogController {
  private FurnitureCatalog         catalog;
  private UserPreferences preferences;
  private ContentManager  contentManager;
  private JComponent      catalogView;

  /**
   * Creates a controller of the furniture catalog view.
   * @param catalog the furniture catalog of the application
   */
  public FurnitureCatalogController(FurnitureCatalog catalog) {
    this(catalog, null, null);
  }

  /**
   * Creates a controller of the furniture catalog view.
   * @param catalog the furniture catalog of the application
   * @param preferences application user preferences
   * @param contentManager contentManager for furniture import
   */
  public FurnitureCatalogController(FurnitureCatalog catalog, 
                           UserPreferences preferences, 
                           ContentManager  contentManager) {
    this.catalog = catalog;
    this.preferences = preferences;
    this.contentManager = contentManager;
    this.catalogView = new FurnitureCatalogTree(catalog, this);
  }

  /**
   * Returns the view associated with this controller.
   */
  public JComponent getView() {
    return this.catalogView;
  }

  /**
   * Updates the selected furniture in catalog.
   */
  public void setSelectedFurniture(List<CatalogPieceOfFurniture> selectedFurniture) {
    this.catalog.setSelectedFurniture(selectedFurniture);
  }
  
  /**
   * If <code>furnitureSelectionSynchronized</code> is <code>true</code>, the selected 
   * furniture in the catalog model will be synchronized with be the selection displayed 
   * by the catalog view managed by this controller.
   * By default, selection is synchronized. 
   */
  public void setFurnitureSelectionSynchronized(boolean furnitureSelectionSynchronized) {
    ((FurnitureCatalogTree)getView()).setFurnitureSelectionSynchronized(furnitureSelectionSynchronized);
  }

  /**
   * Displays the wizard that helps to change the selected piece of furniture. 
   */
  public void modifySelectedFurniture() {
    if (this.preferences != null) {
      List<CatalogPieceOfFurniture> selectedFurniture = this.catalog.getSelectedFurniture();
      if (selectedFurniture.size() > 0) {
        CatalogPieceOfFurniture piece = selectedFurniture.get(0);
        if (piece.isModifiable()) {
          new ImportedFurnitureWizardController(piece, this.preferences, this.contentManager);
        }
      }
    }
  }

  /**
   * Displays the wizard that helps to import furniture to catalog. 
   */
  public void importFurniture() {
    if (this.preferences != null) {
      new ImportedFurnitureWizardController(this.preferences, this.contentManager);
    }
  }

  /**
   * Displays the wizard that helps to import furniture to catalog. 
   */
  private void importFurniture(String modelName) {
    if (this.preferences != null) {
      new ImportedFurnitureWizardController(modelName, this.preferences, this.contentManager);
    }
  }

  /**
   * Deletes selected catalog furniture. 
   */
  public void deleteSelection() {
    for (CatalogPieceOfFurniture piece : this.catalog.getSelectedFurniture()) {
      if (piece.isModifiable()) {
        this.catalog.delete(piece);
      }
    }
  }

  /**
   * Adds dropped files to catalog.
   */
  public void dropFiles(List<String> importableModels) {
    // Import furniture
    for (String model : importableModels) {
      importFurniture(model);
    }
  }
}
