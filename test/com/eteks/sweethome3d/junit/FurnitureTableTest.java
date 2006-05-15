/*
 * CatalogTreeTest.java 11 mai 2006
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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import junit.framework.TestCase;

import com.eteks.sweethome3d.io.DefaultCatalog;
import com.eteks.sweethome3d.io.DefaultUserPreferences;
import com.eteks.sweethome3d.model.Catalog;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.Unit;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.swing.CatalogController;
import com.eteks.sweethome3d.swing.CatalogTree;
import com.eteks.sweethome3d.swing.FurnitureController;
import com.eteks.sweethome3d.swing.FurnitureTable;
import com.eteks.sweethome3d.swing.HomeController;
import com.eteks.sweethome3d.swing.HomePane;
import com.sun.tools.javac.util.List;

public class FurnitureTableTest extends TestCase {
  public FurnitureTableTest(String name) {
    super(name);
  }
  
  public void testFurnitureTable()  {
    // Choose a language that displays furniture dimensions in inches
    Locale.setDefault(Locale.US);    
    // Create model objects
    UserPreferences preferences = new DefaultUserPreferences();
    Home home = new Home();
    // Create home controller and its view
    HomeController homeController = new HomeController(home, preferences);
    // Retrieve tree and table objects created by home controller
    JComponent homeView = homeController.getView();
    CatalogController catalogController = homeController.getCatalogController();
    CatalogTree    tree  = (CatalogTree)catalogController.getView();
    FurnitureController tableController = homeController.getFurnitureController();
    FurnitureTable table = (FurnitureTable)tableController.getView();
    
    // Select two pieces of furniture in tree and add them to the table
    tree.addSelectionInterval(1, 2);
    homeController.addHomeFurniture();
    
    // Check the model and the table contains two pieces 
    List<HomePieceOfFurniture> homeFurniture = home.getFurniture();
    assertEquals("Home doesn't contain 2 pieces", homeFurniture.size(), 2);
    assertEquals("Table doesn't contain 2 pieces", table.getSelectedFurniture().size(), 2);
    
    // Select the first piece in table, delete it
    table.setRowSelectionInterval(0, 0);
    tableController.deleteHomeFurniture();
    // Check the model and the table contains only one piece 
    assertEquals("Home doesn't contain 1 piece", home.getFurniture().size(), 1);
    assertEquals("Table doesn't contain 1 piece", table.getSelectedFurniture().size(), 1);
    
    // Undo previous operation
    homeController.undo();
    // Check the model and the table contains two pieces 
    assertEquals("Home doesn't contain 2 pieces after undo", home.getFurniture().size(), 2);
    assertEquals("Table doesn't contain 2 pieces after undo", table.getSelectedFurniture().size(), 2);
    
    // Check the previously deleted piece is selected
    assertEquals("Deleted piece isn't selected", table.getSelectedRow(), 0);
    
    // Undo first operation on table
    homeController.undo();
    // Check the model and the table doesn't contain any piece 
    assertEquals("Home isn't empty after 2 undo operations", home.getFurniture().size(), 0);
    assertEquals("Table isn't empty after 2 undo operations", table.getSelectedFurniture().size(), 0);
    
    // Redo the 2 operations on table
    homeController.redo();
    homeController.redo();
    // Check the model contains the two pieces that where added at beginning
    assertEquals("Home doesn't contain the same furniture", homeFurniture, home.getFurniture());
    
    // Sort furniture table in alphabetical order of furniture name
    ResourceBundle resource = ResourceBundle.getBundle(table.getClass().getName());
    String nameColumn = resource.getString("nameColumn");
    table.setSortedColumn(nameColumn);
    tableController.sortHomeFurniture();
    
    // Check the alphabetical order of table data
    assertTableIsSorted(table, nameColumn, false);
    // Sort in descending order and check order
    table.setAscendingSort(false);
    tableController.sortHomeFurniture();
    assertTableIsSorted(table, nameColumn, true);

    // Check the displayed widths in table are different in French and US version
    String widthColumn = resource.getString("widthColumn");
    String widthInInch = getTableRenderedValue(table, widthColumn, 0);
    preferences.setUnit(Unit.METER);
    String widthInMeter = getTableRenderedValue(table, widthColumn, 0);
    assertFalse("Same width in different units", widthInInch.equals(widthInMeter));
  }
  
  private void assertTableIsSorted(JTable table, String column, boolean ascendingOrder) {
    // TODO Check if column name column is sorted
    TableModel model = table.getModel();
    TableColumnModel columnModel = table.getColumnModel(); 
    int columnIndex = columnModel.getColumnIndex(column);
    int modelColumnIndex = table.convertColumnIndexToModel(columnIndex);
    Comparator<Object> comparator = Collator.getInstance();
    if (!ascendingOrder)
      comparator = Collections.reverseOrder(comparator);
    // For each row 
    for (int row = 0, n = model.getRowCount()-1; row < n; row++) {
      Object value = model.getValueAt(row, modelColumnIndex);
      Object nextValue = model.getValueAt(row + 1, modelColumnIndex);
      // Check alphatical order of values at a row and next row
      assertTrue("Column not sorted", comparator.compare(value, nextValue) <= 0);
    }
  }
  
  private String getTableRenderedValue(FurnitureTable table, String column, int row) {
    // TODO Get the value displayed in table cell at column, row 
    return null;
  }

  public static void main(String [] args) {
    UserPreferences preferences = new DefaultUserPreferences();
    Home home = new Home();
    HomeControllerTest controler = new HomeControllerTest(home, preferences);
  }

  private static class HomeControllerTest extends HomeController {
    public HomeControllerTest(Home home, UserPreferences preferences) {
      super(home, preferences);
      // TODO Display a home view in a frame with buttons linked to controller
      new HomeCatalogViewTest(this, home, preferences).displayView();
    }
  }

  private static class HomeCatalogViewTest extends JRootPane {
    public HomeCatalogViewTest(final HomeController controler, Home home, UserPreferences preferences) {
      JToolBar toolBar = new JToolBar();
      JButton addButton = new JButton(new ImageIcon(
          FurnitureTableTest.class.getResource("resources/Add16.gif")));
      addButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          controler.addHomeFurniture();
        }
      });
      JButton deleteButton = new JButton(new ImageIcon(
          FurnitureTableTest.class.getResource("resources/Delete16.gif")));
      addButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          controler.getFurnitureController().deleteHomeFurniture();
        }
      });
      JButton undoButton = new JButton(new ImageIcon(
          FurnitureTableTest.class.getResource("resources/Undo16.gif")));
      addButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          controler.undo();
        }
      });
      JButton redoButton = new JButton(new ImageIcon(
          FurnitureTableTest.class.getResource("resources/Redo16.gif")));
      addButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          controler.redo();
        }
      });
      toolBar.add(addButton);
      toolBar.add(deleteButton);
      toolBar.add(undoButton);
      toolBar.add(redoButton);
      getContentPane().add(toolBar, BorderLayout.NORTH);
      getContentPane().add(controler.getView(), BorderLayout.CENTER);    
    }

    public void displayView() {
      JFrame frame = new JFrame("Furniture Table Test") {
        {
          setRootPane(HomeCatalogViewTest.this);
        }
      };
      frame.pack();
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setVisible(true);
    } 
  }
}
