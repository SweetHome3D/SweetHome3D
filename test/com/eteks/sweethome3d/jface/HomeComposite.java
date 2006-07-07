/*
 * HomeComposite.java 29 mai 2006
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
package com.eteks.sweethome3d.jface;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.viewcontroller.HomeView;

/**
 * The MVC view that edits home furniture. 
 * @author Emmanuel Puybaret
 */
public class HomeComposite implements HomeView {
  private Composite composite;

  public HomeComposite(Composite parent, Home home, UserPreferences preferences) {
    this.composite = new Composite(parent, SWT.NONE);
    SashForm catalogFurnitureSashForm = new SashForm(this.composite, SWT.VERTICAL);
    new CatalogTree(catalogFurnitureSashForm, preferences.getCatalog());
    new FurnitureTable(catalogFurnitureSashForm, home, preferences);
    this.composite.setLayout(new FillLayout());
  }

  public Composite getHomeComposite() {
    return this.composite;
  }
}
