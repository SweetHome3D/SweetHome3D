/*
 * FurnitureTableTest.java 11 mai 2006
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
package com.eteks.sweethome3d.junit;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import junit.framework.TestCase;

import com.eteks.sweethome3d.io.DefaultUserPreferences;
import com.eteks.sweethome3d.model.Catalog;
import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.Category;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.swing.FurnitureTable;

/**
 * Tests furniture table component.
 * @author Emmanuel Puybaret
 */
public class FurnitureTableTest extends TestCase {
  public void testFurnitureTableCreation()  {
    // 1. Choose a locale that displays furniture dimensions in inches
    Locale.setDefault(Locale.US);
    // Read default user preferences
    UserPreferences preferences = new DefaultUserPreferences();
    // Check the current unit isn't centimeter
    UserPreferences.Unit currentUnit = preferences.getUnit();
    assertFalse("Unit is in centimeter", currentUnit == UserPreferences.Unit.CENTIMETER);
    // Get furniture catalog
    Catalog catalog = preferences.getCatalog();
    
    // 2. Create a home that contains furniture matching catalog furniture
    List<HomePieceOfFurniture> homeFurniture = createHomeFurnitureFromCatalog(catalog);
    Home home = new Home(homeFurniture);
    // Check catalog furniture count equals home furniture count
    assertEquals("Different furniture count in list and home", 
        homeFurniture.size(), home.getFurniture().size());

    // 3. Create a table that displays home furniture 
    JTable table = new FurnitureTable(home, preferences);
    // Check home furniture count equals table row count
    assertEquals("Different furniture count in home and table", 
        home.getFurniture().size(), table.getRowCount());
    
    // 4. Check the displayed depth in table are different in French and US version
    String widthInInch = getRenderedDepth(table, 0);
    preferences.setUnit(UserPreferences.Unit.CENTIMETER);
    String widthInMeter = getRenderedDepth(table, 0);
    assertFalse("Same depth in different units", 
        widthInInch.equals(widthInMeter));
  }

  private static List<HomePieceOfFurniture> createHomeFurnitureFromCatalog(
      Catalog catalog) {
    List<HomePieceOfFurniture> homeFurniture = new ArrayList<HomePieceOfFurniture>();
    for (Category category : catalog.getCategories()) {
      for (CatalogPieceOfFurniture piece : category.getFurniture()) {
        homeFurniture.add(new HomePieceOfFurniture(piece));
      }
    }
    return homeFurniture;
  }
  
  private String getRenderedDepth(JTable table, int row) {
    // Get index of detph column in model
    ResourceBundle resource = 
      ResourceBundle.getBundle(table.getClass().getName());
    String columnName = resource.getString("depthColumn");
    int modelColumnIndex = table.getColumn(columnName).getModelIndex();

    // Get depth value at row
    TableModel model = table.getModel();
    Object cellValue = model.getValueAt(row, modelColumnIndex);
    
    // Get component used to render the depth cell at row
    TableCellRenderer renderer = table.getCellRenderer(row, modelColumnIndex);
    int tableColumnIndex = table.convertColumnIndexToView(modelColumnIndex);
    Component cellLabel = renderer.getTableCellRendererComponent(
        table, cellValue, false, false, row, tableColumnIndex);
    
    // Return rendered depth
    return ((JLabel)cellLabel).getText();
  }

  public static void main(String [] args) {
    UserPreferences preferences = new DefaultUserPreferences();
    List<HomePieceOfFurniture> homeFurniture = 
      createHomeFurnitureFromCatalog(preferences.getCatalog());
    Home home = new Home(homeFurniture);
    
    // Create a furniture table
    JTable table = new FurnitureTable(home, preferences);
    JFrame frame = new JFrame("Furniture table Test");
    frame.add(new JScrollPane(table));
    frame.pack();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);
  }
}
