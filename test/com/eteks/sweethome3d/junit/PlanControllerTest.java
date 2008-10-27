/*
 * PlanControllerTest.java 31 mai 2006
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

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEditSupport;

import junit.framework.TestCase;

import com.eteks.sweethome3d.io.DefaultUserPreferences;
import com.eteks.sweethome3d.model.CollectionEvent;
import com.eteks.sweethome3d.model.CollectionListener;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.swing.PlanController;

/**
 * Tests {@link com.eteks.sweethome3d.swing.PlanController plan controller}.
 * @author Emmanuel Puybaret
 */
public class PlanControllerTest extends TestCase {
  /**
   * Performs the same tests as {@link PlanComponentTest#testPlanComponent()} 
   * but with direct calls to controller in memory.
   */
  public void testPlanContoller() throws InterruptedException, InvocationTargetException {
    // Run test in Event Dispatch Thread because the default view associated 
    // to plan controller instance performs some actions in EDT even if it's not displayed
    EventQueue.invokeAndWait(new Runnable() {
        public void run() {
          runPlanContollerTest();          
        }
      });
  }
  
  private void runPlanContollerTest() { 
    // 1. Create a frame that displays a PlanComponent at its preferred size, 
    Home home = new Home();
    Locale.setDefault(Locale.FRANCE);
    UserPreferences preferences = new DefaultUserPreferences();
    UndoableEditSupport undoSupport = new UndoableEditSupport();
    UndoManager undoManager = new UndoManager();
    undoSupport.addUndoableEditListener(undoManager);
    PlanController planController = new PlanController(home, preferences, null, undoSupport);
    
    // Build an ordered list of walls added to home
    final ArrayList<Wall> orderedWalls = new ArrayList<Wall>();
    home.addWallsListener(new CollectionListener<Wall> () {
      public void collectionChanged(CollectionEvent<Wall> ev) {
        if (ev.getType() == CollectionEvent.Type.ADD) {
          orderedWalls.add(ev.getItem());
        }
      }
    });
    
    // 2. Use WALL_CREATION mode
    planController.setMode(PlanController.Mode.WALL_CREATION);
    // Click at (20, 20), (500, 22), (498, 300), then double click at (20, 302) in home coordinates space 
    planController.moveMouse(20, 20);
    planController.pressMouse(20, 20, 1, false, false);
    planController.toggleMagnetism(false);
    planController.releaseMouse(20, 20);
    planController.moveMouse(500, 22);
    planController.pressMouse(500, 22, 1, false, false);
    planController.releaseMouse(500, 22);
    planController.moveMouse(498, 300);
    planController.pressMouse(498, 300, 1, false, false);
    planController.releaseMouse(498, 300);
    planController.moveMouse(20, 302);
    planController.pressMouse(20, 302, 1, false, false);
    planController.releaseMouse(20, 302);
    planController.pressMouse(20, 302, 2, false, false);
    planController.releaseMouse(20, 302);
    // Check 3 walls were created at (20, 20), (500, 20), (500, 300) and (20, 300) coordinates
    Wall wall1 = orderedWalls.get(0);
    assertCoordinatesEqualWallPoints(20, 20, 500, 20, wall1);
    Wall wall2 = orderedWalls.get(1);
    assertCoordinatesEqualWallPoints(500, 20, 500, 300, wall2);
    Wall wall3 = orderedWalls.get(2);
    assertCoordinatesEqualWallPoints(500, 300, 20, 300, wall3);
    // Check they are joined to each other end point
    assertWallsAreJoined(null, wall1, wall2); 
    assertWallsAreJoined(wall1, wall2, wall3); 
    assertWallsAreJoined(wall2, wall3, null); 
    // Check they are selected
    assertSelectionContains(home, wall1, wall2, wall3);

    // 3. Click at (20, 300), then double click at (60, 60) with Alt key depressed
    planController.moveMouse(20, 300);
    planController.pressMouse(20, 300, 1, false, false);
    planController.releaseMouse(20, 300);
    planController.toggleMagnetism(true);
    planController.moveMouse(60, 60);
    planController.pressMouse(60, 60, 1, false, false);
    planController.releaseMouse(60, 60);
    planController.pressMouse(60, 60, 2, false, false);
    planController.releaseMouse(60, 60);
    planController.toggleMagnetism(false);
    // Check a forth wall was created at (20, 300), (60, 60) coordinates
    Wall wall4 = orderedWalls.get(orderedWalls.size() - 1);
    assertCoordinatesEqualWallPoints(20, 300, 60, 60, wall4);
    assertSelectionContains(home, wall4);
    assertWallsAreJoined(wall3, wall4, null);

    // 4. Use SELECTION mode
    planController.setMode(PlanController.Mode.SELECTION);
    // Check current mode is SELECTION
    assertEquals("Current mode isn't " + PlanController.Mode.SELECTION, 
        PlanController.Mode.SELECTION, planController.getMode());
    // Press the delete key
    planController.deleteSelection();
    // Check plan contains only the first three walls
    assertHomeContains(home, wall1, wall2, wall3);
    
    // 5. Use WALL_CREATION mode
    planController.setMode(PlanController.Mode.WALL_CREATION);
    //  Click at (22, 18), then double click at (20, 300)
    planController.moveMouse(22, 18);
    planController.pressMouse(22, 18, 1, false, false);
    planController.releaseMouse(22, 18);
    planController.moveMouse(20, 300);
    planController.pressMouse(20, 300, 1, false, false);
    planController.releaseMouse(20, 300);
    planController.pressMouse(20, 300, 2, false, false);
    planController.releaseMouse(20, 300);
    // Check a new forth wall was created at (20, 20), (20, 300) coordinates
    wall4 = orderedWalls.get(orderedWalls.size() - 1);
    assertCoordinatesEqualWallPoints(20, 20, 20, 300, wall4);
    // Check its end points are joined to the first and third wall
    assertWallsAreJoined(wall1, wall4, wall3);
    
    // 6. Use SELECTION mode
    planController.setMode(PlanController.Mode.SELECTION);
    // Drag and drop cursor from (360, 160) to (560, 320)
    planController.moveMouse(360, 160);
    planController.pressMouse(360, 160, 1, false, false);
    planController.moveMouse(560, 320);
    planController.releaseMouse(560, 320);
    // Check the selected walls are the second and third ones
    assertSelectionContains(home, wall2, wall3);

    // 7. Press twice right arrow key     
    planController.moveSelection(2, 0);
    planController.moveSelection(2, 0);
    // Check the 4 walls coordinates are (20, 20), (504, 20), (504, 300), (24, 300) 
    assertCoordinatesEqualWallPoints(20, 20, 504, 20, wall1);
    assertCoordinatesEqualWallPoints(504, 20, 504, 300, wall2);
    assertCoordinatesEqualWallPoints(504, 300, 24, 300, wall3);
    assertCoordinatesEqualWallPoints(20, 20, 24, 300, wall4);

    // 8. Click at (504, 40) with Shift key depressed
    planController.moveMouse(504, 40);
    planController.pressMouse(504, 40, 1, true, false);
    planController.releaseMouse(504, 40);
    // Check the second wall was removed from selection
    assertSelectionContains(home, wall3);

     // 9. Drag cursor from (60, 20) to (60, 60) 
    planController.moveMouse(60, 20);
    planController.pressMouse(60, 20, 1, false, false);
    planController.moveMouse(60, 60);
    // Check first wall is selected and that it moved
    assertSelectionContains(home, wall1);
    assertCoordinatesEqualWallPoints(20, 60, 504, 60, wall1);
    // Lose focus
    planController.escape();
    // Check the wall didn't move at end
    assertCoordinatesEqualWallPoints(20, 20, 504, 20, wall1);

    // 10. Undo 8 times 
    for (int i = 0; i < 6; i++) {
      undoManager.undo();
    }
    // Check home doesn't contain any wall
    assertHomeContains(home);
    
    // 11. Redo 8 times 
    for (int i = 0; i < 6; i++) {
      undoManager.redo();
    }
    // Check plan contains the four wall
    assertHomeContains(home, wall1, wall2, wall3, wall4);
    // Check the second and the third wall are selected
    assertSelectionContains(home, wall2, wall3);
  }

  /**
   * Asserts the start point and the end point of 
   * <code>wall</code> are at (<code>xStart</code>, <code>yStart</code>), (<code>xEnd</code>, <code>yEnd</code>). 
   */
  private void assertCoordinatesEqualWallPoints(float xStart, float yStart, float xEnd, float yEnd, Wall wall) {
    assertTrue("Incorrect X start " + xStart + " " + wall.getXStart(), 
        Math.abs(xStart - wall.getXStart()) < 1E-10);
    assertTrue("Incorrect Y start " + yStart + " " + wall.getYStart(), 
        Math.abs(yStart - wall.getYStart()) < 1E-10);
    assertTrue("Incorrect X end " + xEnd + " " + wall.getXEnd(), 
        Math.abs(xEnd - wall.getXEnd()) < 1E-10);
    assertTrue("Incorrect Y end " + yEnd + " " + wall.getYEnd(), 
        Math.abs(yEnd - wall.getYEnd()) < 1E-10);
  }

  /**
   * Asserts <code>wall</code> is joined to <code>wallAtStart</code> 
   * and <code>wallAtEnd</code>.
   */
  private void assertWallsAreJoined(Wall wallAtStart, Wall wall, Wall wallAtEnd) {
    assertSame("Incorrect wall at start", wallAtStart, wall.getWallAtStart());
    assertSame("Incorrect wall at end", wallAtEnd, wall.getWallAtEnd());
  }

  /**
   * Asserts <code>home</code> contains <code>walls</code>.
   */
  private void assertHomeContains(Home home, Wall ... walls) {
    Collection<Wall> planWalls = home.getWalls();
    assertEquals("Home walls incorrect count", 
        walls.length, planWalls.size());
    for (Wall wall : walls) {
      assertTrue("Wall doesn't belong to plan", planWalls.contains(wall));
    }
  }

  /**
   * Asserts <code>walls</code> are the current selected ones in <code>home</code>.
   */
  private void assertSelectionContains(Home home, 
                                       Wall ... walls) {
    List<Selectable> selectedItems = home.getSelectedItems();
    assertEquals(walls.length, selectedItems.size());
    for (Wall wall : walls) {
      assertTrue("Wall not selected", selectedItems.contains(wall));
    }
  }
}
