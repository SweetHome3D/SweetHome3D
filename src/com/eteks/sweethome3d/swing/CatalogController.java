/*
 * CatalogController.java 15 mai 2006
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

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import com.eteks.sweethome3d.model.Catalog;
import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.URLContent;

/**
 * A MVC controller for the furniture catalog.
 * @author Emmanuel Puybaret
 */
public class CatalogController {
  private Catalog         catalog;
  private UserPreferences preferences;
  private JComponent      catalogView;

  /**
   * Creates a controller of the furniture catalog view.
   * @param catalog the furniture catalog of the application
   */
  public CatalogController(Catalog catalog) {
    this(catalog, null);
  }

  /**
   * Creates a controller of the furniture catalog view.
   * @param catalog the furniture catalog of the application
   * @param preferences application user preferences
   */
  public CatalogController(Catalog catalog, UserPreferences preferences) {
    this.catalog = catalog;
    this.preferences = preferences;
    this.catalogView = new CatalogTree(catalog, this);
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
    ((CatalogTree)getView()).setFurnitureSelectionSynchronized(furnitureSelectionSynchronized);
  }

  /**
   * Displays the wizard that helps to change the selected piece of furniture. 
   */
  public void modifySelectedFurniture() {
    if (this.preferences != null) {
      CatalogPieceOfFurniture piece = this.catalog.getSelectedFurniture().get(0);
      if (piece.isModifiable()) {
        new ImportedFurnitureWizardController(piece, this.preferences);
      }
    }
  }

  /**
   * Displays the wizard that helps to import furniture to catalog. 
   */
  public void importFurniture() {
    if (this.preferences != null) {
      new ImportedFurnitureWizardController(this.preferences);
    }
  }

  /**
   * Displays the wizard that helps to import furniture to catalog. 
   */
  private void importFurniture(URLContent model) {
    if (this.preferences != null) {
      new ImportedFurnitureWizardController(model, this.preferences);
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
  public boolean dropFiles(List<File> files) {
    // Search importables files
    List<URLContent> importableModels = new ArrayList<URLContent>();        
    for (File file : files) {
      if (file.getName().toLowerCase().endsWith(".obj")
          || file.getName().toLowerCase().endsWith(".lws")
          || file.getName().toLowerCase().endsWith(".3ds")) {
        try {
          importableModels.add(new URLContent(file.toURL()));
        } catch (MalformedURLException ex) {
          // Ignore files that can be transformed as a URL
        }
      }        
    }
    if (importableModels.size() == 0) {
      return false;
    } else {
      // Import furniture
      for (URLContent model : importableModels) {
        importFurniture(model);
      }
      return true;
    }
  }
}
