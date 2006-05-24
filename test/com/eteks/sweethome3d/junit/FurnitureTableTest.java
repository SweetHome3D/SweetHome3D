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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.Collator;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRootPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import junit.framework.TestCase;

import com.eteks.sweethome3d.io.DefaultUserPreferences;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.swing.CatalogController;
import com.eteks.sweethome3d.swing.CatalogTree;
import com.eteks.sweethome3d.swing.FurnitureController;
import com.eteks.sweethome3d.swing.FurnitureTable;
import com.eteks.sweethome3d.swing.HomeController;

public class FurnitureTableTest extends TestCase {
  public void testFurnitureTable()  {
    // 1. Choose a locale that displays furniture dimensions in inches
    Locale.setDefault(Locale.US);
    // Create model objects
    UserPreferences preferences = new DefaultUserPreferences();
    Home home = new Home();

    // Check the current unit isn't centimeter
    UserPreferences.Unit currentUnit = preferences.getUnit();
    assertFalse("Unit is in centimeter", currentUnit == UserPreferences.Unit.CENTIMETER);
   
    // Create home controller
    HomeController homeController = 
        new HomeController(home, preferences);
    // Retrieve tree and table objects created by home controller
    CatalogController catalogController = 
        homeController.getCatalogController();
    CatalogTree tree = (CatalogTree)catalogController.getView();
    FurnitureController furnitureController = 
        homeController.getFurnitureController();
    FurnitureTable table = 
        (FurnitureTable)furnitureController.getView();

    // 2. Select two pieces of furniture in tree and add them to the table
    tree.expandRow(0); 
    tree.addSelectionInterval(1, 2);
    homeController.addHomeFurniture();

    // Check the model contains two pieces
    List<HomePieceOfFurniture> homeFurniture = home.getFurniture(); 
    assertEquals("Home doesn't contain 2 pieces", 
        2, homeFurniture.size());
    //  Check the two pieces in table are selected
    assertEquals("Table doesn't display 2 selected pieces", 
        2, table.getSelectedFurniture().size());

    // 3. Select the first piece in table, delete it
    table.setSelectedFurniture(Arrays.asList(
        new HomePieceOfFurniture [] {homeFurniture.get(0)}));
    furnitureController.deleteFurniture();
    // Check the model contains only one piece
    assertEquals("Home doesn't contain 1 piece", 
        1, home.getFurniture().size());
    // Check the table doesn't display any selection
    assertEquals("Table selection isn't empty", 
        0, table.getSelectedFurniture().size());

    // 4. Undo previous operation
    homeController.undo();
    // Check the model contains two pieces
    assertEquals("Home doesn't contain 2 pieces after undo", 
        2, home.getFurniture().size());
    //  Check the deleted piece in table is selected
    assertEquals("Table selection doesn't contain the previously deleted piece",
        homeFurniture.get(0), table.getSelectedFurniture().get(0));


    // 5. Undo first operation on table
    homeController.undo();
    // Check the model and the table doesn't contain any piece
    assertEquals("Home isn't empty after 2 undo operations", 
        0, home.getFurniture().size());

    // 6. Redo the last undone operation on table
    homeController.redo();
    // Check the model contains the two pieces that where added at beginning
    assertEquals("Home doesn't contain the same furniture",
        homeFurniture, home.getFurniture());
    assertEquals("Table doesn't display 2 selected pieces",
        2, table.getSelectedFurniture().size());

    // 7. Redo the delete operation
    homeController.redo();
    // Check the model contains only one piece
    assertEquals("Home doesn't contain 1 piece", 
        1, home.getFurniture().size());
    // Check the table doesn't display any selection
    assertEquals("Table selection isn't empty", 
        0, table.getSelectedFurniture().size());

//    // 8. Sort furniture table in alphabetical order of furniture name
//    furnitureController.sortFurniture("name");
//
//    // Check the alphabetical order of table data
//    assertTableIsSorted(table, "nameColumn", true);
//    // Sort in descending order and check order
//    furnitureController.sortFurniture("name");
//    assertTableIsSorted(table, "nameColumn", false);

    // 9. Check the displayed widths in table are different in French and US version
    String widthInInch = getRenderedValue(table, "widthColumn", 0);
    preferences.setUnit(UserPreferences.Unit.CENTIMETER);
    String widthInMeter = getRenderedValue(table, "widthColumn", 0);
    assertFalse("Same width in different units", 
        widthInInch.equals(widthInMeter));
  }
  
//  private void assertTableIsSorted(JTable table, String columnNameKey, boolean ascendingOrder) {
//    // TODO Check if column in table is sorted
//    int columnIndex = getColumnIndex(table, columnNameKey);
//    int modelColumnIndex = table.convertColumnIndexToModel(columnIndex);
//    TableModel model = table.getModel();
//    Comparator<Object> comparator = Collator.getInstance();
//    if (!ascendingOrder)
//      comparator = Collections.reverseOrder(comparator);
//    // For each row 
//    for (int row = 0, n = model.getRowCount()-1; row < n; row++) {
//      Object value = model.getValueAt(row, modelColumnIndex);
//      Object nextValue = model.getValueAt(row + 1, modelColumnIndex);
//      // Check alphatical order of values at a row and next row
//      assertTrue("Column not sorted", comparator.compare(value, nextValue) <= 0);
//    }
//  }

  private String getRenderedValue(JTable table, String columnNameKey, int row) {
    //  TODO Return the value displayed in a cell of table
    int columnIndex = getColumnIndex(table, columnNameKey);
    int modelColumnIndex = table.convertColumnIndexToModel(columnIndex);
    TableModel model = table.getModel();
    Object cellValue = model.getValueAt(row, modelColumnIndex);
    TableCellRenderer renderer = table.getCellRenderer(row, modelColumnIndex);
    Component cellLabel = renderer.getTableCellRendererComponent(table, cellValue, false, false, row, columnIndex);
    return ((JLabel)cellLabel).getText();
  }

  private int getColumnIndex(JTable table, String columnNameKey) {
    ResourceBundle resource = 
      ResourceBundle.getBundle(table.getClass().getName());
    String columnHeader = resource.getString(columnNameKey);

    TableColumnModel columnModel = table.getColumnModel();
    TableColumn column = null;
    for (int i = 0, n = columnModel.getColumnCount(); i < n; i++) {
      if (columnModel.getColumn(i).getHeaderValue().equals(columnHeader)) {
        column = columnModel.getColumn(i);
        break;
      }
    }
    if (column == null)
      fail("Unkonwn column " + columnHeader);
    return columnModel.getColumnIndex(column.getIdentifier());
  }

  public static void main(String [] args) {
    UserPreferences preferences = new DefaultUserPreferences();
    Home home = new Home();
    new HomeControllerTest(home, preferences);
  }

  private static class HomeControllerTest extends HomeController {
    public HomeControllerTest(Home home, UserPreferences preferences) {
      super(home, preferences);
      new HomeCatalogViewTest(this).displayView();
    }
  }

  private static class HomeCatalogViewTest extends JRootPane {
    public HomeCatalogViewTest(final HomeController controller) {
      // Create buttons that will launch controler methods
      JButton addButton = new JButton(new ImageIcon(
          getClass().getResource("resources/Add16.gif")));
      addButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          controller.addHomeFurniture();
        }
      });
      JButton deleteButton = new JButton(new ImageIcon(
          getClass().getResource("resources/Delete16.gif")));
      deleteButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          controller.getFurnitureController().deleteFurniture();
        }
      });
      JButton undoButton = new JButton(new ImageIcon(
          getClass().getResource("resources/Undo16.gif")));
      undoButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          controller.undo();
        }
      });
      JButton redoButton = new JButton(new ImageIcon(
          getClass().getResource("resources/Redo16.gif")));
      redoButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          controller.redo();
        }
      });
      // Put them it a tool bar
      JToolBar toolBar = new JToolBar();
      toolBar.add(addButton);
      toolBar.add(deleteButton);
      toolBar.add(undoButton);
      toolBar.add(redoButton);
      // Display the tool bar and main view in this pane
      getContentPane().add(toolBar, BorderLayout.NORTH);
      getContentPane().add(controller.getHomeView(), BorderLayout.CENTER);    
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
