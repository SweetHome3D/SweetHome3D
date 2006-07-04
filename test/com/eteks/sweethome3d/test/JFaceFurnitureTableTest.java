/*
 * JFaceFurnitureTableTest.java 1 mai 2006
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
package com.eteks.sweethome3d.test;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.eteks.sweethome3d.io.DefaultUserPreferences;
import com.eteks.sweethome3d.jface.FurnitureTable;
import com.eteks.sweethome3d.model.Catalog;
import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.Category;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * Tests furniture furniture table JFace implementation.
 * @author Emmanuel Puybaret
 */
public class JFaceFurnitureTableTest {
  public static void main(String [] args) {
    final UserPreferences preferences = new DefaultUserPreferences();
    Catalog catalog = preferences.getCatalog();
    List<HomePieceOfFurniture> homeFurniture = new ArrayList<HomePieceOfFurniture>();
    for (Category category : catalog.getCategories()) {
      for (CatalogPieceOfFurniture piece : category.getFurniture()) {
        homeFurniture.add(new HomePieceOfFurniture(piece));
      }
    }
    final Home home = new Home(homeFurniture);
    // Create an application window that displays an instance of CatalogTree
    ApplicationWindow window = new ApplicationWindow(null) {
       protected void configureShell(Shell shell) {
        shell.setText("Category Tree Test");
        shell.setLayout(new FillLayout(SWT.VERTICAL));
      }

      protected Control createContents(Composite parent) {
        new FurnitureTable(parent, home, preferences);
        return parent;
      }
    };
    window.setBlockOnOpen(true);
    window.open();
    Display.getCurrent().dispose(); 
  } 
}
