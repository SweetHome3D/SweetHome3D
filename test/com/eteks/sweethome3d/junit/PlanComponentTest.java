/*
 * PlanComponentTest.java 31 mai 2006
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
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEditSupport;

import junit.extensions.abbot.ComponentTestFixture;
import abbot.tester.ComponentLocation;
import abbot.tester.JButtonTester;

import com.eteks.sweethome3d.io.DefaultUserPreferences;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.model.WallEvent;
import com.eteks.sweethome3d.model.WallListener;
import com.eteks.sweethome3d.swing.PlanComponent;
import com.eteks.sweethome3d.swing.PlanController;

/**
 * Tests {@link com.eteks.sweethome3d.swing.PlanComponent plan} component and 
 * its {@link com.eteks.sweethome3d.swing.PlanController controller}.
 * @author Emmanuel Puybaret
 */
public class PlanComponentTest extends ComponentTestFixture {
  public void testPlanComponent() {
    // 1. Create a frame that displays a PlanComponent instance of 
    // a new home at 540 pixels by 400 preferred size, and a tool bar
    // with a mode toggle button, an undo button and a redo button
    PlanTestFrame frame = new PlanTestFrame();    
    // Show home plan frame
    showWindow(frame);
    PlanComponent planComponent = (PlanComponent)frame.planController.getView();
    assertEquals("Wrong preferred width", 540, planComponent.getWidth());
    
    // Build an ordered list of walls added to home
    final ArrayList<Wall> orderedWalls = new ArrayList<Wall>();
    frame.home.addWallListener(new WallListener () {
      public void wallChanged(WallEvent ev) {
        if (ev.getType() == WallEvent.Type.ADD) {
          orderedWalls.add(ev.getWall());
        }
      }
    });
    
    // 2. Use WALL_CREATION mode
    JButtonTester tester = new JButtonTester();
    tester.actionClick(frame.modeButton);
    assertEquals("Current mode isn't " + PlanController.Mode.WALL_CREATION, 
        PlanController.Mode.WALL_CREATION, frame.planController.getMode());
    // Click at (20, 20), (270, 21), (269, 170), then double click at (20, 171)
    tester.actionClick(planComponent, 20, 20);
    tester.actionClick(planComponent, 270, 21);
    tester.actionClick(planComponent, 269, 170);
    tester.actionClick(planComponent, 20, 171, InputEvent.BUTTON1_MASK, 2);
    // Check 3 walls were created at (0, 0), (500, 0), (500, 300) and (0, 300) coordinates
    Wall wall1 = orderedWalls.get(0);
    assertCoordinatesEqualWallPoints(0, 0, 500, 0, wall1);
    Wall wall2 = orderedWalls.get(1);
    assertCoordinatesEqualWallPoints(500, 0, 500, 300, wall2);
    Wall wall3 = orderedWalls.get(2);
    assertCoordinatesEqualWallPoints(500, 300, 0, 300, wall3);
    // Check they are joined to each other end point
    assertWallsAreJoined(null, wall1, wall2); 
    assertWallsAreJoined(wall1, wall2, wall3); 
    assertWallsAreJoined(wall2, wall3, null); 
    // Check they are selected
    assertSelectionContains(frame.home, wall1, wall2, wall3);

    // 3. Click at (20, 170), then double click at (30, 30) with Shift key depressed
    tester.actionClick(planComponent, 20, 170);
    tester.actionKeyPress(KeyEvent.VK_SHIFT);
    tester.actionClick(planComponent, 30, 30, InputEvent.BUTTON1_MASK, 2);
    tester.actionKeyRelease(KeyEvent.VK_SHIFT);
    // Check a forth wall was created at (0, 300), (20, 20) coordinates
    Wall wall4 = orderedWalls.get(orderedWalls.size() - 1);
    assertCoordinatesEqualWallPoints(0, 300, 20, 20, wall4);
    assertSelectionContains(frame.home, wall4);
    assertWallsAreJoined(wall3, wall4, null);

    // 4. Use SELECTION mode
    tester.actionClick(frame.modeButton); 
    // Check current mode is SELECTION
    assertEquals("Current mode isn't " + PlanController.Mode.SELECTION, 
        PlanController.Mode.SELECTION, frame.planController.getMode());
    // Give focus to plan component
    tester.actionFocus(planComponent);
    // Press the delete key 
    tester.actionKeyStroke(KeyEvent.VK_DELETE);
    // Check plan contains only the first three walls
    assertHomeContains(frame.home, wall1, wall2, wall3);
    
    // 5. Use WALL_CREATION mode
    tester.actionClick(frame.modeButton);   
    //  Click at (21, 19), then double click at (20, 170)
    tester.actionClick(planComponent, 21, 19); 
    tester.actionClick(planComponent, 20, 170, InputEvent.BUTTON1_MASK, 2);
    // Check a new forth wall was created at (0, 0), (0, 300) coordinates
    wall4 = orderedWalls.get(orderedWalls.size() - 1);
    assertCoordinatesEqualWallPoints(0, 0, 0, 300, wall4);
    // Check its end points are joined to the first and third wall
    assertWallsAreJoined(wall1, wall4, wall3);
    
    // 6. Use SELECTION mode
    tester.actionClick(frame.modeButton); 
    // Drag and drop cursor from (200, 100) to (300, 180)
    tester.actionMousePress(planComponent, 
        new ComponentLocation(new Point(200, 100))); 
    tester.actionMouseMove(planComponent, 
        new ComponentLocation(new Point(300, 180))); 
    tester.actionMouseRelease(); 
    // Check the selected walls are the second and third ones
    assertSelectionContains(frame.home, wall2, wall3);

    // 7. Press twice right arrow key     
    tester.actionKeyStroke(KeyEvent.VK_RIGHT);
    tester.actionKeyStroke(KeyEvent.VK_RIGHT);
    // Check the 4 walls coordinates are (0, 0), (504, 0), (504, 300), (4, 300) 
    assertCoordinatesEqualWallPoints(0, 0, 504, 0, wall1);
    assertCoordinatesEqualWallPoints(504, 0, 504, 300, wall2);
    assertCoordinatesEqualWallPoints(504, 300, 4, 300, wall3);
    assertCoordinatesEqualWallPoints(0, 0, 4, 300, wall4);

    // 8. Click at (272, 40) with Shift key depressed
    tester.actionKeyPress(KeyEvent.VK_SHIFT);
    tester.actionClick(planComponent, 272, 40);
    tester.actionKeyRelease(KeyEvent.VK_SHIFT);
    // Check the second wall was removed from selection
    assertSelectionContains(frame.home, wall3);

     // 9. Drag cursor from (50, 20) to (50, 40) 
    tester.actionMousePress(planComponent, 
        new ComponentLocation(new Point(50, 20)));
    tester.actionMouseMove(planComponent, 
        new ComponentLocation(new Point(50, 40)));
    // Check first wall is selected and that it moved
    assertSelectionContains(frame.home, wall1);
    assertCoordinatesEqualWallPoints(0, 40, 504, 40, wall1);
    // Lose focus 
    tester.actionKeyStroke(KeyEvent.VK_TAB);
    // Check the wall didn't move at end
    assertCoordinatesEqualWallPoints(0, 0, 504, 0, wall1);

    // 10. Click 6 times on undo button
    for (int i = 0; i < 6; i++) {
      tester.actionClick(frame.undoButton);
    }
    // Check home doesn't contain any wall
    assertHomeContains(frame.home);
    
    // 11. Click 6 times on redo button
    for (int i = 0; i < 6; i++) {
      tester.actionClick(frame.redoButton);
    }
    // Check plan contains the four wall
    assertHomeContains(frame.home, wall1, wall2, wall3, wall4);
    //  Check they are joined to each other end point
    assertWallsAreJoined(wall4, wall1, wall2); 
    assertWallsAreJoined(wall1, wall2, wall3); 
    assertWallsAreJoined(wall2, wall3, wall4); 
    assertWallsAreJoined(wall1, wall4, wall3); 
    // Check the second and the third wall are selected
    assertSelectionContains(frame.home, wall2, wall3);
  }

  /**
   * Asserts the start point and the end point of 
   * <code>wall</code> are at (<code>xStart</code>, <code>yStart</code>), (<code>xEnd</code>, <code>yEnd</code>). 
   */
  private void assertCoordinatesEqualWallPoints(float xStart, float yStart, float xEnd, float yEnd, Wall wall) {
    assertTrue("Incorrect X start", Math.abs(xStart - wall.getXStart()) < 1E-10);
    assertTrue("Incorrect Y start", Math.abs(yStart - wall.getYStart()) < 1E-10);
    assertTrue("Incorrect X end", Math.abs(xEnd - wall.getXEnd()) < 1E-10);
    assertTrue("Incorrect Y end", Math.abs(yEnd - wall.getYEnd()) < 1E-10);
  }

  /**
   * Asserts <code>wall</code> is joined to <code>wallAtStart</code> 
   * and <code>wallAtEnd</code>.
   */
  private void assertWallsAreJoined(Wall wallAtStart, Wall wall, Wall wallAtEnd) {
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
    List<Object> selectedItems = home.getSelectedItems();
    assertEquals(walls.length, selectedItems.size());
    for (Wall wall : walls) {
      assertTrue("Wall not selected", selectedItems.contains(wall));
    }
  }

  public static void main(String [] args) {
    JFrame frame = new PlanTestFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);
  }
  
  private static class PlanTestFrame extends JFrame {
    private final Home           home;
    private final PlanController planController;
    private final JToggleButton  modeButton;
    private final JButton        undoButton;
    private final JButton        redoButton;

    public PlanTestFrame() {
      super("Plan Component Test");
      // Create model objects
      this.home = new Home();
      UserPreferences preferences = new DefaultUserPreferences();
      UndoableEditSupport undoSupport = new UndoableEditSupport();
      final UndoManager undoManager = new UndoManager();
      undoSupport.addUndoableEditListener(undoManager);
      this.planController = new PlanController(this.home, preferences, undoSupport);
      // Add plan component to frame at its preferred size 
      add(new JScrollPane(this.planController.getView()));
      // Create a toggle button for plan component mode 
      this.modeButton = new JToggleButton(new ImageIcon(
          getClass().getResource("resources/Add16.gif")));
      // Add listeners to modeButton that manages plan planController mode
      this.modeButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          if (modeButton.isSelected()) {
            planController.setMode(PlanController.Mode.WALL_CREATION);
          } else {
            planController.setMode(PlanController.Mode.SELECTION);
          }
        }
      });
      // Create an undo button 
      this.undoButton = new JButton(new ImageIcon(
          getClass().getResource("resources/Undo16.gif")));
      this.undoButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          undoManager.undo();
        }
      });
      // Create a redo button
      this.redoButton = new JButton(new ImageIcon(
          getClass().getResource("resources/Redo16.gif")));
      this.redoButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          undoManager.redo();
        }
      });
      // Add a tool bar to the frame with mode toggle button, 
      // undo and redo buttons
      JToolBar toolBar = new JToolBar();
      toolBar.add(this.modeButton);
      toolBar.add(this.undoButton);
      toolBar.add(this.redoButton);
      // Add the tool bar at top of the window
      add(toolBar, BorderLayout.NORTH);
      // Pack frame to ensure home plan component is at its preferred size 
      pack();
    }
  }
}
