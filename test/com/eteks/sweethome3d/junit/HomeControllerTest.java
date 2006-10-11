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
import java.awt.Container;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JRootPane;

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
import com.eteks.sweethome3d.swing.HomePane;

/**
 * Tests home controller.
 * @author Emmanuel Puybaret
 */
public class HomeControllerTest extends TestCase {
  private Home                home;
  private HomeController      homeController;
  private CatalogTree         catalogTree;
  private FurnitureController furnitureController;
  private FurnitureTable      furnitureTable;

  @Override
  protected void setUp() {
    // 1. Create model objects
    UserPreferences preferences = new DefaultUserPreferences();
    this.home = new Home();
    this.homeController = new HomeController(this.home, preferences);
    CatalogController catalogController = 
        homeController.getCatalogController();
    this.catalogTree = (CatalogTree)catalogController.getView();
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
    List<HomePieceOfFurniture> furniture = this.home.getFurniture();
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
        firstPiece, this.home.getFurniture().get(0));
    assertEquals("Deleted piece isn't selected", 
        firstPiece, this.home.getSelectedItems().get(0));
    //  Check all actions are enabled
    assertActionsEnabled(true, true, true, true);

    // 6. Undo first operation
    runAction(HomePane.ActionType.UNDO);
    // Check home is empty
    assertTrue("Home furniture isn't empty", 
        this.home.getFurniture().isEmpty());
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
    JComponent homeView = this.homeController.getView();
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

  public static void main(String [] args) {
    UserPreferences preferences = new DefaultUserPreferences();
    Home home = new Home();
    new ControllerTest(home, preferences);
  }

  private static class ControllerTest extends HomeController {
    public ControllerTest(Home home, UserPreferences preferences) {
      super(home, preferences);
      new ViewTest(this).displayView();
    }
  }

  private static class ViewTest extends JRootPane {
    public ViewTest(final HomeController controller) {
      // Display main view in this pane
      getContentPane().add(controller.getView());
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
