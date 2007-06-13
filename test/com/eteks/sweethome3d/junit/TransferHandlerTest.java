/*
 * TransferHandlerTest.java 11 sept 06
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
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.eteks.sweethome3d.junit;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JFrame;

import junit.extensions.abbot.ComponentTestFixture;
import abbot.finder.ComponentSearchException;
import abbot.tester.ComponentLocation;
import abbot.tester.JComponentTester;

import com.eteks.sweethome3d.io.DefaultUserPreferences;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.swing.CatalogTree;
import com.eteks.sweethome3d.swing.FurnitureTable;
import com.eteks.sweethome3d.swing.HomeController;
import com.eteks.sweethome3d.swing.HomePane;
import com.eteks.sweethome3d.swing.PlanComponent;

/**
 * Tests drag and drop, and cut / copy / paste.
 * @author Emmanuel Puybaret
 */
public class TransferHandlerTest extends ComponentTestFixture {
  public void testTransferHandler() throws ComponentSearchException {
    UserPreferences preferences = new DefaultUserPreferences();
    Home home = new Home();
    HomeController controller = new HomeController(home, preferences);
    CatalogTree catalogTree = (CatalogTree)TestUtilities.findComponent(
         controller.getView(), CatalogTree.class);
    FurnitureTable furnitureTable = (FurnitureTable)TestUtilities.findComponent(
        controller.getView(), FurnitureTable.class);
    PlanComponent planComponent = (PlanComponent)TestUtilities.findComponent(
         controller.getView(), PlanComponent.class);

    // 1. Create a frame that displays a home view 
    JFrame frame = new JFrame("Home TransferHandler Test");    
    frame.add(controller.getView());
    frame.pack();

    // Show home plan frame
    showWindow(frame);
    JComponentTester tester = new JComponentTester();
    tester.waitForIdle();
    // Check catalog tree has default focus
    assertTrue("Tree doesn't have the focus", catalogTree.isFocusOwner());
    // Check Cut, Copy, Paste and Delete actions are disable
    assertActionsEnabled(controller, false, false, false, false);
    
    // 2. Select the first piece of furniture in catalog
    catalogTree.expandRow(0); 
    catalogTree.addSelectionInterval(1, 1);
    // Check only Copy action is enabled
    assertActionsEnabled(controller, false, true, false, false);
    
    // 3. Drag and drop selected piece in tree to point (120, 120) in plan component
    Rectangle selectedRowBounds = catalogTree.getRowBounds(1);
    tester.actionDrag(catalogTree, new ComponentLocation( 
        new Point(selectedRowBounds.x, selectedRowBounds.y)));
    tester.actionDrop(planComponent, new ComponentLocation( 
        new Point(120, 120))); 
    tester.waitForIdle();
    // Check a piece was added to home
    assertEquals("Wrong piece count in home", 1, home.getFurniture().size());
    // Check top left corner of the piece is at (200, 200) 
    HomePieceOfFurniture piece = home.getFurniture().get(0);
    assertTrue("Incorrect X " + piece.getX(), 
        Math.abs(200 - piece.getX() + piece.getWidth() / 2) < 1E-10);
    assertTrue("Incorrect Y " + piece.getY(), 
        Math.abs(200 - piece.getY() + piece.getDepth() / 2) < 1E-10);

    // 4.  Transfer focus to plan view with TAB keys
    tester.actionKeyStroke(KeyEvent.VK_TAB);
    tester.actionKeyStroke(KeyEvent.VK_TAB);
    // Check plan component has focus
    assertTrue("Plan doesn't have the focus", planComponent.isFocusOwner());
    // Check Cut, Copy and Delete actions are enabled in plan view
    assertActionsEnabled(controller, true, true, false, true);

    // 5. Use Wall creation mode
    controller.setWallCreationMode();
    // Check Cut, Copy, Paste actions are disabled
    assertActionsEnabled(controller, false, false, false, false);
    
    // 6. Create a wall between points (20, 20) and (100, 20)
    tester.actionClick(planComponent, 20, 20);
    tester.actionClick(planComponent, 100, 20, InputEvent.BUTTON1_MASK, 2);
    // Use Selection mode 
    controller.setSelectionMode();
    // Check Cut, Copy and Delete actions are enabled
    assertActionsEnabled(controller, true, true, false, true);
    
    // 7. Select the wall and the piece 
    tester.actionKeyPress(KeyEvent.VK_SHIFT);
    tester.actionClick(planComponent, 120, 120); 
    tester.actionKeyRelease(KeyEvent.VK_SHIFT);
    // Cut selected items in plan component
    runAction(controller, HomePane.ActionType.CUT);
    // Check home is empty
    assertEquals("Wrong piece count in home", 0, home.getFurniture().size());
    assertEquals("Wrong wall count in home", 0, home.getWalls().size());
    // Check only Paste action is enabled
    assertActionsEnabled(controller, false, false, true, false);

    // 8. Paste selected items in plan component
    runAction(controller, HomePane.ActionType.PASTE);
    tester.waitForIdle();
    // Check home contains one wall and one piece
    assertEquals("Wrong piece count in home", 1, home.getFurniture().size());
    assertEquals("Wrong wall count in home", 1, home.getWalls().size());

    // 9. Transfer focus to furniture table
    tester.actionKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_MASK);
    // Check furniture table has focus
    assertTrue("Table doesn't have the focus", furnitureTable.isFocusOwner());
    // Delete selection 
    runAction(controller, HomePane.ActionType.DELETE);
    // Check home contains one wall and no piece
    assertEquals("Wrong piece count in home", 0, home.getFurniture().size());
    assertEquals("Wrong wall count in home", 1, home.getWalls().size());
    // Check only Paste action is enabled
    assertActionsEnabled(controller, false, false, true, false);

    // 10. Paste selected items in furniture table
    runAction(controller, HomePane.ActionType.PASTE);
    // Check home contains one wall and one piece
    assertEquals("Wrong piece count in home", 1, home.getFurniture().size());
    assertEquals("Wrong wall count in home", 1, home.getWalls().size());
    // Check Cut, Copy and Paste actions are enabled
    assertActionsEnabled(controller, true, true, true, true);
  }
  
  /**
   * Runs <code>actionPerformed</code> method matching <code>actionType</code> 
   * in <code>HomePane</code>. 
   */
  private void runAction(HomeController controller,
                         HomePane.ActionType actionType) {
    getAction(controller, actionType).actionPerformed(null);
  }

  /**
   * Returns the action matching <code>actionType</code> in <code>HomePane</code>. 
   */
  private Action getAction(HomeController controller,
                           HomePane.ActionType actionType) {
    return controller.getView().getActionMap().get(actionType);
  }
  
  /**
   * Asserts CUT, COPY, PASTE and DELETE actions in <code>HomePane</code> 
   * are enabled or disabled. 
   */
  private void assertActionsEnabled(HomeController controller,
                                    boolean cutActionEnabled, 
                                    boolean copyActionEnabled, 
                                    boolean pasteActionEnabled, 
                                    boolean deleteActionEnabled) {
    assertTrue("Cut action invalid state", 
        cutActionEnabled == getAction(controller, HomePane.ActionType.CUT).isEnabled());
    assertTrue("Copy action invalid state", 
        copyActionEnabled == getAction(controller, HomePane.ActionType.COPY).isEnabled());
    assertTrue("Paste action invalid state", 
        pasteActionEnabled == getAction(controller, HomePane.ActionType.PASTE).isEnabled());
    assertTrue("Delete action invalid state", 
        deleteActionEnabled == getAction(controller, HomePane.ActionType.DELETE).isEnabled());
  }
}
