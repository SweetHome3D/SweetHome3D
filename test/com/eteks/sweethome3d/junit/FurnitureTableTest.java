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
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import junit.framework.TestCase;

import com.eteks.sweethome3d.io.DefaultUserPreferences;
import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.FurnitureCatalog;
import com.eteks.sweethome3d.model.FurnitureCategory;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.LengthUnit;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.swing.FurnitureTable;
import com.eteks.sweethome3d.swing.FurnitureTable.FurnitureFilter;
import com.eteks.sweethome3d.swing.SwingViewFactory;
import com.eteks.sweethome3d.viewcontroller.FurnitureController;

/**
 * Tests furniture table component.
 * @author Emmanuel Puybaret
 */
public class FurnitureTableTest extends TestCase {
  /**
   * Tests furniture table creation and localization.
   */
  public void testFurnitureTableCreation()  {
    // 1. Choose a locale that displays furniture dimensions in inches
    Locale.setDefault(Locale.US);
    // Read default user preferences
    UserPreferences preferences = new DefaultUserPreferences();
    // Check the current unit isn't centimeter
    LengthUnit currentUnit = preferences.getLengthUnit();
    assertFalse("Unit is in centimeter", currentUnit == LengthUnit.CENTIMETER);
    // Get furniture catalog
    FurnitureCatalog catalog = preferences.getFurnitureCatalog();
    
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
    for (int row = 0, n = table.getRowCount(); row < n; row++) {
      preferences.setUnit(LengthUnit.INCH);
      String widthInInch = getRenderedDepth(table, row);
      preferences.setUnit(LengthUnit.CENTIMETER);
      String widthInMeter = getRenderedDepth(table, row);
      assertFalse("Same depth in different units", 
          widthInInch.equals(widthInMeter));
    }
  }
  
  private static List<HomePieceOfFurniture> createHomeFurnitureFromCatalog(
      FurnitureCatalog catalog) {
    List<HomePieceOfFurniture> homeFurniture = new ArrayList<HomePieceOfFurniture>();
    for (FurnitureCategory category : catalog.getCategories()) {
      for (CatalogPieceOfFurniture piece : category.getFurniture()) {
        homeFurniture.add(new HomePieceOfFurniture(piece));
      }
    }
    return homeFurniture;
  }
  
  private String getRenderedDepth(JTable table, int row) {
    // Get index of detph column in model
    TableColumn depthColumn = table.getColumn(HomePieceOfFurniture.SortableProperty.DEPTH);

    // Get depth value at row
    TableModel model = table.getModel();
    Object cellValue = model.getValueAt(row, depthColumn.getModelIndex());
    
    // Get component used to render the depth cell at row
    int tableColumnIndex = table.getColumnModel().getColumnIndex(HomePieceOfFurniture.SortableProperty.DEPTH);
    TableCellRenderer renderer = table.getCellRenderer(row, tableColumnIndex);
    Component cellLabel = renderer.getTableCellRendererComponent(
        table, cellValue, false, false, row, tableColumnIndex);
    
    // Return rendered depth
    return ((JLabel)cellLabel).getText();
  }

  /**
   * Tests sort in furniture table. 
   */
  public void testFurnitureTableSort() {
    // 1.  Create a home that contains furniture matching catalog furniture
    UserPreferences preferences = new DefaultUserPreferences();
    List<HomePieceOfFurniture> homeFurniture = 
      createHomeFurnitureFromCatalog(preferences.getFurnitureCatalog());
    Home home = new Home(homeFurniture);
    // Check home furniture isn't empty
    assertTrue("Home furniture is empty", homeFurniture.size() > 0);

    // 2. Create a table that displays home furniture with its controller  
    FurnitureController furnitureController = 
        new FurnitureController(home, preferences, new SwingViewFactory());
    FurnitureTable table = (FurnitureTable)furnitureController.getView();
    
    // 3. Sort furniture table in alphabetical order of furniture name
    furnitureController.sortFurniture(HomePieceOfFurniture.SortableProperty.NAME);
    // Check the alphabetical order of table data
    assertFurnitureIsSortedByName(table, true);
    
    // 4. Sort in descending order and check order
    furnitureController.sortFurniture(HomePieceOfFurniture.SortableProperty.NAME);
    // Check table data is sorted in alphabetical descending order
    assertFurnitureIsSortedByName(table, false);
    
    // 5. Change first furniture name
    HomePieceOfFurniture firstPiece = home.getFurniture().get(0);
    firstPiece.setName("Aaaa");
    // Check table data is sorted in alphabetical descending order
    assertFurnitureIsSortedByName(table, false);
  }

  private void assertFurnitureIsSortedByName(JTable table,
                                             boolean ascendingOrder) {
    int modelColumnIndex = table.getColumn(HomePieceOfFurniture.SortableProperty.NAME).getModelIndex();
    TableModel model = table.getModel();
    Comparator<Object> comparator = Collator.getInstance();
    if (!ascendingOrder)
      comparator = Collections.reverseOrder(comparator);
    // For each row
    for (int row = 0, n = model.getRowCount() - 1; row < n; row++) {
      Object value = model.getValueAt(row, modelColumnIndex);
      Object nextValue = model.getValueAt(row + 1, modelColumnIndex);
      // Check alphabetical order of values at a row and next row
      assertTrue("Column not sorted", 
          comparator.compare(((HomePieceOfFurniture)value).getName(), 
                             ((HomePieceOfFurniture)nextValue).getName()) <= 0);
    }
  }
  
  /**
   * Tests filter in furniture table.
   */
  public void testFurnitureTableFilter() {
    // 1.  Create a home that contains furniture matching catalog furniture
    UserPreferences preferences = new DefaultUserPreferences();
    List<HomePieceOfFurniture> homeFurniture = 
      createHomeFurnitureFromCatalog(preferences.getFurnitureCatalog());
    Home home = new Home(homeFurniture);
    // Check home furniture isn't empty
    assertTrue("Home furniture is empty", homeFurniture.size() > 0);

    // 2. Create a table that displays home furniture with its controller  
    FurnitureController furnitureController = 
        new FurnitureController(home, preferences, new SwingViewFactory());
    FurnitureTable table = (FurnitureTable)furnitureController.getView();
    assertEquals("Home furniture count and row count different", homeFurniture.size(), table.getRowCount());
    // Apply a filter on furniture that refuses pieces that are windows
    table.setFurnitureFilter(new FurnitureFilter() {
        public boolean include(Home home, HomePieceOfFurniture piece) {          
          return !piece.isDoorOrWindow();
        }
      });
    // Count how many doors and windows are in home
    int doorsAndWindowsCount = 0;
    HomePieceOfFurniture doorOrWindowPiece = null;
    HomePieceOfFurniture otherPiece = null;
    for (HomePieceOfFurniture piece : home.getFurniture()) {
      if (piece.isDoorOrWindow()) {
        doorsAndWindowsCount++;
        doorOrWindowPiece = piece;
      } else {
        otherPiece = piece;
      }
    }
    // Check there's no door or window in table
    int homeFurnitureCount = homeFurniture.size();
    int tableFilterRowCount = table.getRowCount();
    assertEquals("Home furniture count and row count same", 
        homeFurnitureCount - doorsAndWindowsCount, tableFilterRowCount);
    
    // 3. Add a door or window to home
    home.addPieceOfFurniture(new HomePieceOfFurniture(doorOrWindowPiece));
    // Check no row were added in table
    assertEquals("Wrong furniture count in home", homeFurnitureCount + 1, home.getFurniture().size());
    assertEquals("Wrong row count in table", tableFilterRowCount, table.getRowCount());
    
    // 4. Add an other kind of piece to home
    home.addPieceOfFurniture(new HomePieceOfFurniture(otherPiece));
    // Check one row was added in table
    assertEquals("Wrong furniture count in home", homeFurnitureCount + 2, home.getFurniture().size());
    assertEquals("Wrong row count in table", tableFilterRowCount + 1, table.getRowCount());
    
    // 5. Test sort and filter internal buffer of the table
    furnitureController.sortFurniture(HomePieceOfFurniture.SortableProperty.NAME);
    // Check the alphabetical order of table data
    assertFurnitureIsSortedByName(table, true);
    // Add a door or window and an other kind of piece to home
    home.addPieceOfFurniture(new HomePieceOfFurniture(doorOrWindowPiece));
    home.addPieceOfFurniture(new HomePieceOfFurniture(otherPiece));
    // Check one row was added in sorted table
    assertEquals("Wrong furniture count in home", homeFurnitureCount + 4, home.getFurniture().size());
    assertEquals("Wrong row count in table", tableFilterRowCount + 2, table.getRowCount());
    assertFurnitureIsSortedByName(table, true);
    
    // 6. Remove filter
    table.setFurnitureFilter(null);
    // Check missing rows are back
    assertEquals("Home furniture count and row count different", 
        home.getFurniture().size(), table.getRowCount());
    assertFurnitureIsSortedByName(table, true);
  }

  public static void main(String [] args) {
    UserPreferences preferences = new DefaultUserPreferences();
    List<HomePieceOfFurniture> homeFurniture = 
      createHomeFurnitureFromCatalog(preferences.getFurnitureCatalog());
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
