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
package com.eteks.sweethome3d.viewcontroller;

import java.util.List;

import com.eteks.sweethome3d.model.Catalog;
import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;

/**
 * A MVC controller for the furniture catalog.
 * @author Emmanuel Puybaret
 */
public class CatalogController {
  private Catalog catalog;
  private View    catalogView;
  
  /**
   * Creates a controller of the furniture catalog view.
   * @param viewFactory factory able to create views
   * @param catalog the furniture catalog of the application
   */
  public CatalogController(ViewFactory viewFactory, Catalog catalog) {
    this.catalog = catalog;
    this.catalogView = viewFactory.createCatalogView(catalog, this);
  }

  /**
   * Returns the view associated with this controller.
   */
  public View getView() {
    return this.catalogView;
  }

  /**
   * Updates the selected furniture in catalog.
   */
  public void setSelectedFurniture(List<CatalogPieceOfFurniture> selectedFurniture) {
    this.catalog.setSelectedFurniture(selectedFurniture);
  }
}
