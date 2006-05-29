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

import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * A MVC controller for the furniture catalog.
 * @author Emmanuel Puybaret
 */
public class CatalogController implements Controller {
  private CatalogView catalogView;
  /**
   * Creates a controller of the furniture catalog view.
   * @param viewFactory factory able to create views
   * @param preferences the preferences of the application
   */
  public CatalogController(ViewFactory viewFactory, UserPreferences preferences) {
    this.catalogView = viewFactory.createCatalogView(preferences.getCatalog());
  }

  /**
   * Returns the view associated with this controller.
   */
  public View getView() {
    return this.catalogView;
  }

  /**
   * Returns the selected furniture un catalog view.
   */
  public List<CatalogPieceOfFurniture> getSelectedFurniture() {
    return this.catalogView.getSelectedFurniture();
  }
}
