/*
 * HomeControllerTest.java 15 mai 2006
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
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;

import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRootPane;
import javax.swing.JTable;

import junit.framework.TestCase;

import com.eteks.sweethome3d.io.DefaultUserPreferences;
import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.FurnitureCategory;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeFurnitureGroup;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.LengthUnit;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.swing.FurnitureCatalogTree;
import com.eteks.sweethome3d.swing.FurnitureTable;
import com.eteks.sweethome3d.swing.HomePane;
import com.eteks.sweethome3d.swing.SwingViewFactory;
import com.eteks.sweethome3d.viewcontroller.FurnitureCatalogController;
import com.eteks.sweethome3d.viewcontroller.FurnitureController;
import com.eteks.sweethome3d.viewcontroller.HomeController;
import com.eteks.sweethome3d.viewcontroller.PlanController;
import com.eteks.sweethome3d.viewcontroller.ViewFactory;

/**
 * Tests home controller.
 * @author Emmanuel Puybaret
 */
public class HomeControllerTest extends TestCase {
  private ViewFactory          viewFactory;
  private UserPreferences      preferences;
  private Home                 home;
  private HomeController       homeController;
  private FurnitureCatalogTree catalogTree;
  private FurnitureController  furnitureController;
  private FurnitureTable       furnitureTable;

  @Override
  protected void setUp() {
    this.viewFactory = new SwingViewFactory();
    this.preferences = new DefaultUserPreferences();
    this.preferences.setFurnitureCatalogViewedInTree(true);
    this.home = new Home();
    this.homeController = 
        new HomeController(this.home, this.preferences, viewFactory);
    FurnitureCatalogController catalogController = 
        homeController.getFurnitureCatalogController();
    this.catalogTree = (FurnitureCatalogTree)catalogController.getView();
    this.furnitureController = 
        homeController.getFurnitureController();
    this.furnitureTable = 
        (FurnitureTable)furnitureController.getView();
  }

  /**
   * Tests add and delete furniture in home.
   */
  public void testHomeFurniture() {
    // 2. Select the two first pieces of furniture in catalog and add them to the table
    catalogTree.expandRow(0); 
    catalogTree.addSelectionInterval(1, 2);
    homeController.addHomeFurniture();

    // Check the table contains two pieces
    assertEquals("Table doesn't contain 2 pieces", 
        2, furnitureTable.getRowCount());
    //  Check the two pieces in table are selected
    assertEquals("Table doesn't display 2 selected pieces", 
        2, furnitureTable.getSelectedRowCount());

    // 3. Select the first piece in table, delete it
    furnitureTable.setRowSelectionInterval(0, 0);
    homeController.getFurnitureController().deleteSelection();
    // Check the table contains only one piece
    assertEquals("Table doesn't contain 1 piece", 
        1, furnitureTable.getRowCount());
    // Check the table doesn't display any selection
    assertEquals("Table selection isn't empty", 
        0, furnitureTable.getSelectedRowCount());
  }

  /**
   * Tests undo add and delete furniture in home.
   */
  public void testHomeFurnitureUndoableActions() {
    // Check all actions are disabled
    assertActionsEnabled(false, false, false, false);
    
    // 2. Select the two first pieces of furniture in catalog
    catalogTree.expandRow(0); 
    catalogTree.addSelectionInterval(1, 2);
    // Check only Add action is enabled
    assertActionsEnabled(true, false, false, false);
    
    // 3. Add the selected furniture to the table
    runAction(HomePane.ActionType.ADD_HOME_FURNITURE);
    List<HomePieceOfFurniture> furniture = home.getFurniture();
    //  Check Add, Delete and Undo actions are enabled
    assertActionsEnabled(true, true, true, false);
    
    // 4. Select the first piece in table and delete it
    furnitureTable.setRowSelectionInterval(0, 0);
    runAction(HomePane.ActionType.DELETE_HOME_FURNITURE);
    //  Check Add and Undo actions are enabled
    assertActionsEnabled(true, false, true, false);
    
    // 5. Undo last operation
    runAction(HomePane.ActionType.UNDO);
    // Check home contains the deleted piece
    HomePieceOfFurniture firstPiece = furniture.get(0);
    assertEquals("Deleted piece isn't undeleted", 
        firstPiece, home.getFurniture().get(0));
    assertEquals("Deleted piece isn't selected", 
        firstPiece, home.getSelectedItems().get(0));
    //  Check all actions are enabled
    assertActionsEnabled(true, true, true, true);

    // 6. Undo first operation
    runAction(HomePane.ActionType.UNDO);
    // Check home is empty
    assertTrue("Home furniture isn't empty", 
        home.getFurniture().isEmpty());
    //  Check Add and Redo actions are enabled
    assertActionsEnabled(true, false, false, true);
    
    // 7. Redo first operation
    runAction(HomePane.ActionType.REDO);
    // Check home contains the two previously added pieces
    assertEquals("Home doesn't contain the two previously added pieces",
        furniture, home.getFurniture());
    // Check they are selected
    assertEquals("Added pieces are selected",
        furniture, home.getSelectedItems());
    //  Check all actions are enabled
    assertActionsEnabled(true, true, true, true);

    // 8. Redo second operation
    runAction(HomePane.ActionType.REDO);
    // Check home contains only the second piece
    assertEquals("Home doesn't contain the second piece",
        furniture.get(1), home.getFurniture().get(0));
    // Check selection is empty
    assertTrue("Selection isn't empty",
        home.getSelectedItems().isEmpty());
    //  Check Add and Undo actions are enabled
    assertActionsEnabled(true, false, true, false);
  }
  
  /**
   * Tests zoom and alignment tools. 
   */
  public void testZoomAndAligment() {
    // Add the first two pieces of catalog first category to home
    FurnitureCategory firstCategory = this.preferences.getFurnitureCatalog().getCategories().get(0);
    HomePieceOfFurniture firstPiece = new HomePieceOfFurniture(firstCategory.getFurniture().get(0));
    this.home.addPieceOfFurniture(firstPiece);
    HomePieceOfFurniture secondPiece = new HomePieceOfFurniture(firstCategory.getFurniture().get(1));
    secondPiece.setAngle(15);
    this.home.addPieceOfFurniture(secondPiece);
    // Add a wall to home
    this.home.addWall(new Wall(0, 0, 100, 0, 7, this.home.getWallHeight()));
    
    PlanController planController = this.homeController.getPlanController();
    float scale = planController.getScale();
    
    // 1. Zoom in 
    runAction(HomePane.ActionType.ZOOM_IN);    
    // Check scale changed
    assertEquals("Scale is incorrect", scale * 1.5f, planController.getScale()); 
    
    // 2. Zoom out 
    runAction(HomePane.ActionType.ZOOM_OUT);    
    // Check scale is back to its previous value
    assertEquals("Scale is incorrect", scale, planController.getScale());
    
    // 3. Select all while table has focus
    this.homeController.focusedViewChanged(this.furnitureTable);
    runAction(HomePane.ActionType.SELECT_ALL);
    // Check selection contains the two pieces
    assertEquals("Selection doesn't contain home furniture", 
        this.home.getFurniture(), this.home.getSelectedItems());

    // 4. Select all while plan has focus
    this.homeController.focusedViewChanged(planController.getView());
    runAction(HomePane.ActionType.SELECT_ALL);
    // Check selection contains the two pieces, the wall and the compass
    assertEquals("Selection doesn't contain home objects", 
        4, this.home.getSelectedItems().size());
    
    // 5. Select the first two pieces 
    this.home.setSelectedItems(Arrays.asList(new Selectable [] {firstPiece, secondPiece}));
    float secondPieceX = secondPiece.getX();
    float secondPieceY = secondPiece.getY();
    // Align on bottom
    runAction(HomePane.ActionType.ALIGN_FURNITURE_ON_BOTTOM);
    // Check bottom of second piece equals bottom of first piece
    TestUtilities.assertEqualsWithinEpsilon("Second piece isn't aligned on bottom of first piece",
        getMaxY(firstPiece), getMaxY(secondPiece), 10E-4f);

    // 6. Align on top
    runAction(HomePane.ActionType.ALIGN_FURNITURE_ON_TOP);
    // Check bottom of second piece equals bottom of first piece
    TestUtilities.assertEqualsWithinEpsilon("Second piece isn't aligned on top of first piece",
        getMinY(firstPiece), getMinY(secondPiece), 10E-4f);
    
    // 7. Align on left
    runAction(HomePane.ActionType.ALIGN_FURNITURE_ON_LEFT);
    // Check bottom of second piece equals bottom of first piece
    TestUtilities.assertEqualsWithinEpsilon("Second piece isn't aligned on left of first piece",
        getMinX(firstPiece), getMinX(secondPiece), 10E-4f);
    
    // 8. Align on right
    runAction(HomePane.ActionType.ALIGN_FURNITURE_ON_RIGHT);
    // Check bottom of second piece equals bottom of first piece
    TestUtilities.assertEqualsWithinEpsilon("Second piece isn't aligned on right of first piece",
        getMaxX(firstPiece), getMaxX(secondPiece), 10E-4f);
    float alignedPieceX = secondPiece.getX();
    float alignedPieceY = secondPiece.getY();

    // 9. Undo alignments
    runAction(HomePane.ActionType.UNDO);
    runAction(HomePane.ActionType.UNDO);
    runAction(HomePane.ActionType.UNDO);
    runAction(HomePane.ActionType.UNDO);
    // Check second piece is back to its place
    TestUtilities.assertEqualsWithinEpsilon("Second piece abcissa is incorrect",
        secondPieceX, secondPiece.getX(), 10E-4f);
    TestUtilities.assertEqualsWithinEpsilon("Second piece ordinate is incorrect",
        secondPieceY, secondPiece.getY(), 10E-4f);

    // 10. Redo alignments
    runAction(HomePane.ActionType.REDO);
    runAction(HomePane.ActionType.REDO);
    runAction(HomePane.ActionType.REDO);
    runAction(HomePane.ActionType.REDO);
    // Check second piece is back to its place
    TestUtilities.assertEqualsWithinEpsilon("Second piece abcissa is incorrect",
        alignedPieceX, secondPiece.getX(), 10E-4f);
    TestUtilities.assertEqualsWithinEpsilon("Second piece ordinate is incorrect",
        alignedPieceY, secondPiece.getY(), 10E-4f);
  }
  
  /**
   * Tests furniture visible properties changes.
   */
  public void testFurnitureVisibleProperties() {
    // 1. Add the first piece of catalog first category to home
    FurnitureCategory firstCategory = this.preferences.getFurnitureCatalog().getCategories().get(0);
    HomePieceOfFurniture piece = new HomePieceOfFurniture(firstCategory.getFurniture().get(0));
    this.home.addPieceOfFurniture(piece);
    // Use centimeter as unit
    this.preferences.setUnit(LengthUnit.CENTIMETER);
    // Check displayed values in table
    assertFurnitureFirstRowEquals(this.furnitureTable, piece.getName(),
        piece.getWidth(), piece.getDepth(), piece.getHeight(), piece.isVisible());
    
    // 2. Make name property invisible
    runAction(HomePane.ActionType.DISPLAY_HOME_FURNITURE_NAME);
    // Check displayed values in table doesn't contain piece name
    assertFurnitureFirstRowEquals(this.furnitureTable, 
        piece.getWidth(), piece.getDepth(), piece.getHeight(), piece.isVisible());

    // 3. Make y property visible
    runAction(HomePane.ActionType.DISPLAY_HOME_FURNITURE_Y);
    // Check displayed values in table contains piece ordinate after piece depth
    assertFurnitureFirstRowEquals(this.furnitureTable, 
        piece.getWidth(), piece.getDepth(), piece.getHeight(), piece.getY(), piece.isVisible());

    // 4. Make name property visible again
    runAction(HomePane.ActionType.DISPLAY_HOME_FURNITURE_NAME);
    // Check displayed values in table contains piece name in first position
    assertFurnitureFirstRowEquals(this.furnitureTable, piece.getName(),
        piece.getWidth(), piece.getDepth(), piece.getHeight(), piece.getY(), piece.isVisible());
    
    // 5. Change visible properties order and list
    this.furnitureController.setFurnitureVisibleProperties(
        Arrays.asList(new HomePieceOfFurniture.SortableProperty [] {
            HomePieceOfFurniture.SortableProperty.MOVABLE,
            HomePieceOfFurniture.SortableProperty.NAME}));
    // Check displayed values in table contains piece name and visible properties
    assertFurnitureFirstRowEquals(this.furnitureTable, piece.isMovable(), piece.getName());

    // 6. Make visible property visible
    runAction(HomePane.ActionType.DISPLAY_HOME_FURNITURE_VISIBLE);
    // Check displayed values in table contains piece visible property after movable property
    assertFurnitureFirstRowEquals(this.furnitureTable, 
        piece.isMovable(), piece.isVisible(), piece.getName());
  }
  
  /**
   * Tests furniture group / ungroup.
   */
  public void testFurnitureGroup() {
    assertGroupActionsEnabled(false, false);
    
    // 1. Add the first two pieces of catalog first category to home
    FurnitureCategory firstCategory = this.preferences.getFurnitureCatalog().getCategories().get(0);
    List<CatalogPieceOfFurniture> catalogPieces = Arrays.asList(new CatalogPieceOfFurniture [] {
        firstCategory.getFurniture().get(0), firstCategory.getFurniture().get(1)});
    this.homeController.getFurnitureCatalogController().setSelectedFurniture(catalogPieces);
    runAction(HomePane.ActionType.ADD_HOME_FURNITURE);
    List<HomePieceOfFurniture> pieces = this.home.getFurniture();
    // Check home contains two selected pieces
    assertEquals("Home doesn't contain 2 pieces", 2, pieces.size());
    assertEquals("Home doesn't contain 2 selected items", 2, this.home.getSelectedItems().size());
    assertGroupActionsEnabled(true, false);
    HomePieceOfFurniture piece1 = pieces.get(0);
    HomePieceOfFurniture piece2 = pieces.get(1);
    piece2.move(100, 100);
    piece2.setElevation(100);
    
    // 2. Group selected furniture
    runAction(HomePane.ActionType.GROUP_FURNITURE);
    // Check home contains one selected piece that replaced added pieces
    pieces = this.home.getFurniture();
    assertEquals("Home doesn't contain 1 piece", 1, pieces.size());
    assertEquals("Home doesn't contain 1 selected item", 1, this.home.getSelectedItems().size());
    assertFalse("Home still contains first added piece", pieces.contains(piece1));
    assertFalse("Home still contains scond added piece", pieces.contains(piece2));
    assertGroupActionsEnabled(false, true);
    HomeFurnitureGroup group = (HomeFurnitureGroup)pieces.get(0);
    // Compare surrounding rectangles
    Rectangle2D piecesRectangle = getSurroundingRectangle(piece1);
    piecesRectangle.add(getSurroundingRectangle(piece2));
    Rectangle2D groupRectangle = getSurroundingRectangle(group);
    assertEquals("Surrounding rectangle is incorrect", piecesRectangle, groupRectangle);
    // Compare elevation and height
    assertEquals("Wrong elevation", Math.min(piece1.getElevation(), piece2.getElevation()), group.getElevation());
    assertEquals("Wrong height", 
        Math.max(piece1.getElevation() + piece1.getHeight(), piece2.getElevation() + piece2.getHeight()) 
        - Math.min(piece1.getElevation(), piece2.getElevation()), group.getHeight());
    
    // 3. Rotate group
    float angle = (float)(Math.PI / 2);
    group.setAngle(angle);
    // Check pieces rotation
    assertEquals("Piece angle is wrong", angle, piece1.getAngle());
    assertEquals("Piece angle is wrong", angle, piece2.getAngle());
    assertEquals("Group angle is wrong", angle, group.getAngle());
    // Check surrounding rectangles
    Rectangle2D rotatedGroupRectangle = new GeneralPath(groupRectangle).createTransformedShape(
        AffineTransform.getRotateInstance(angle, groupRectangle.getCenterX(), groupRectangle.getCenterY())).getBounds2D();
    groupRectangle = getSurroundingRectangle(group);
    assertEquals("Surrounding rectangle is incorrect", rotatedGroupRectangle, groupRectangle);
    piecesRectangle = getSurroundingRectangle(piece1);
    piecesRectangle.add(getSurroundingRectangle(piece2));
    assertEquals("Surrounding rectangle is incorrect", piecesRectangle.getWidth(), groupRectangle.getWidth());
    assertEquals("Surrounding rectangle is incorrect", piecesRectangle.getHeight(), groupRectangle.getHeight());
    
    // 4. Undo / Redo
    assertActionsEnabled(true, true, true, false);
    runAction(HomePane.ActionType.UNDO);
    pieces = this.home.getFurniture();
    assertEquals("Home doesn't contain 2 pieces", 2, pieces.size());
    assertEquals("Home doesn't contain 2 selected items", 2, this.home.getSelectedItems().size());
    assertFalse("Home contains group", pieces.contains(group));
    assertGroupActionsEnabled(true, false);

    assertActionsEnabled(true, true, true, true);
    runAction(HomePane.ActionType.REDO);
    pieces = this.home.getFurniture();
    assertEquals("Home doesn't contain 1 piece", 1, pieces.size());
    assertEquals("Home doesn't contain 1 selected item", 1, this.home.getSelectedItems().size());
    assertTrue("Home doesn't contain group", pieces.contains(group));
    assertGroupActionsEnabled(false, true);
    
    // 5. Add one more piece to home
    catalogPieces = Arrays.asList(new CatalogPieceOfFurniture [] {firstCategory.getFurniture().get(0)});
    this.homeController.getFurnitureCatalogController().setSelectedFurniture(catalogPieces);
    runAction(HomePane.ActionType.ADD_HOME_FURNITURE);
    pieces = this.home.getFurniture();
    // Check home contains two pieces with one selected
    assertEquals("Home doesn't contain 2 pieces", 2, pieces.size());
    assertEquals("Home doesn't contain 1 selected item", 1, this.home.getSelectedItems().size());
    assertGroupActionsEnabled(false, false);
    
    // 6. Group it with the other group
    HomePieceOfFurniture piece3 = pieces.get(1);
    this.home.setSelectedItems(Arrays.asList(new Selectable [] {group, piece3}));
    assertEquals("Home doesn't contain 2 selected items", 2, this.home.getSelectedItems().size());
    assertGroupActionsEnabled(true, true);
    runAction(HomePane.ActionType.GROUP_FURNITURE);
    pieces = this.home.getFurniture();
    assertEquals("Home doesn't contain 1 piece", 1, pieces.size());
    assertEquals("Home doesn't contain 1 selected item", 1, this.home.getSelectedItems().size());
    HomeFurnitureGroup group2 = (HomeFurnitureGroup)pieces.get(0);
    assertFalse("Home doesn't contain a different group", group == group2);
    assertGroupActionsEnabled(false, true);
    
    // 7. Ungroup furniture
    runAction(HomePane.ActionType.UNGROUP_FURNITURE);
    assertGroupActionsEnabled(true, true);
    pieces = this.home.getFurniture();
    assertEquals("Home doesn't contain 2 piece", 2, pieces.size());
    assertEquals("Home doesn't contain 2 selected item", 2, this.home.getSelectedItems().size());
    assertFalse("Home contains second group", pieces.contains(group2));
    assertTrue("Home doesn't contain group", pieces.contains(group));
    runAction(HomePane.ActionType.UNGROUP_FURNITURE);
    assertGroupActionsEnabled(true, false);
    pieces = this.home.getFurniture();
    assertEquals("Home doesn't contain 3 piece", 3, pieces.size());
    assertEquals("Home doesn't contain 2 selected item", 2, this.home.getSelectedItems().size());
    assertFalse("Home contains group", pieces.contains(group));
  }
    
  /**
   * Runs <code>actionPerformed</code> method matching <code>actionType</code> 
   * in <code>HomePane</code>. 
   */
  private void runAction(HomePane.ActionType actionType) {
    getAction(actionType).actionPerformed(null);
  }

  /**
   * Returns the action matching <code>actionType</code> in <code>HomePane</code>. 
   */
  private Action getAction(HomePane.ActionType actionType) {
    JComponent homeView = (JComponent)this.homeController.getView();
    return homeView.getActionMap().get(actionType);
  }
  
  /**
   * Asserts ADD_HOME_FURNITURE, DELETE_HOME_FURNITURE, 
   * UNDO and REDO actions in <code>HomePane</code> 
   * are enabled or disabled. 
   */
  private void assertActionsEnabled(boolean addActionEnabled, 
                                    boolean deleteActionEnabled, 
                                    boolean undoActionEnabled, 
                                    boolean redoActionEnabled) {
    assertTrue("Add action invalid state", 
        getAction(HomePane.ActionType.ADD_HOME_FURNITURE).isEnabled() == addActionEnabled);
    assertTrue("Delete action invalid state", 
        getAction(HomePane.ActionType.DELETE_HOME_FURNITURE).isEnabled() == deleteActionEnabled);
    assertTrue("Undo action invalid state", 
        getAction(HomePane.ActionType.UNDO).isEnabled() == undoActionEnabled);
    assertTrue("Redo action invalid state", 
        getAction(HomePane.ActionType.REDO).isEnabled() == redoActionEnabled);
  }
  
  /**
   * Asserts GROUP_FURNITURE, UNGROUP_FURNITURE are enabled or disabled. 
   */
  private void assertGroupActionsEnabled(boolean groupActionEnabled, 
                                         boolean ungroupActionEnabled) {
    assertTrue("Group action invalid state", 
        getAction(HomePane.ActionType.GROUP_FURNITURE).isEnabled() == groupActionEnabled);
    assertTrue("Ungroup action invalid state", 
        getAction(HomePane.ActionType.UNGROUP_FURNITURE).isEnabled() == ungroupActionEnabled);
  }
  
  /**
   * Returns the minimum abcissa of the vertices of <code>piece</code>.  
   */
  private float getMinX(HomePieceOfFurniture piece) {
    float [][] points = piece.getPoints();
    float minX = Float.POSITIVE_INFINITY;
    for (float [] point : points) {
      minX = Math.min(minX, point [0]);
    } 
    return minX;
  }

  /**
   * Returns the maximum abcissa of the vertices of <code>piece</code>.  
   */
  private float getMaxX(HomePieceOfFurniture piece) {
    float [][] points = piece.getPoints();
    float maxX = Float.NEGATIVE_INFINITY;
    for (float [] point : points) {
      maxX = Math.max(maxX, point [0]);
    } 
    return maxX;
  }

  /**
   * Returns the minimum ordinate of the vertices of <code>piece</code>.  
   */
  private float getMinY(HomePieceOfFurniture piece) {
    float [][] points = piece.getPoints();
    float minY = Float.POSITIVE_INFINITY;
    for (float [] point : points) {
      minY = Math.min(minY, point [1]);
    } 
    return minY;
  }

  /**
   * Returns the maximum ordinate of the vertices of <code>piece</code>.  
   */
  private float getMaxY(HomePieceOfFurniture piece) {
    float [][] points = piece.getPoints();
    float maxY = Float.NEGATIVE_INFINITY;
    for (float [] point : points) {
      maxY = Math.max(maxY, point [1]);
    } 
    return maxY;
  }

  /**
   * Asserts that <code>table</code> first row values are correct.
   */
  private void assertFurnitureFirstRowEquals(JTable table, Object ... values) {
    assertEquals("Wrong column count", values.length, table.getColumnCount());

    for (int column = 0, n = table.getColumnCount(); column < n; column++) {
      Component cellRendererComponent = table.getCellRenderer(0, column).
          getTableCellRendererComponent(table, table.getValueAt(0, column), false, false, 0, column);
      if (values [column] instanceof Number) {
        assertEquals("Wrong value at column " + column,  
            this.preferences.getLengthUnit().getFormat().format(values [column]), 
            ((JLabel)cellRendererComponent).getText());
      } else if (values [column] instanceof Boolean) {
        assertEquals("Wrong value at column " + column, values [column], 
            ((JCheckBox)cellRendererComponent).isSelected());
      } else {
        assertEquals("Wrong value at column " + column, values [column], 
            ((JLabel)cellRendererComponent).getText());
      }
    }    
  }

  /**
   * Returns the rectangle surrounding the given <code>piece</code>.
   */
  private Rectangle2D getSurroundingRectangle(HomePieceOfFurniture piece) {
    float [][] points = piece.getPoints();
    Rectangle2D rectangle = new Rectangle2D.Float(points [0][0], points [0][1], 0, 0);
    for (int i = 1; i < points.length; i++) {
      rectangle.add(points [i][0], points [i][1]);
    }
    return rectangle;
  }
  
  public static void main(String [] args) {
    ViewFactory viewFactory = new SwingViewFactory();
    UserPreferences preferences = new DefaultUserPreferences();
    Home home = new Home();
    new ControllerTest(home, preferences, viewFactory);
  }

  private static class ControllerTest extends HomeController {
    public ControllerTest(Home home, 
                          UserPreferences preferences,
                          ViewFactory viewFactory) {
      super(home, preferences, viewFactory);
      new ViewTest(this).displayView();
    }
  }

  private static class ViewTest extends JRootPane {
    public ViewTest(final HomeController controller) {
      // Display main view in this pane
      getContentPane().add((JComponent)controller.getView());
    }

    public void displayView() {
      JFrame frame = new JFrame("Home Controller Test") {
        {
          setRootPane(ViewTest.this);
        }
      };
      frame.pack();
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setVisible(true);
    } 
  }
}
