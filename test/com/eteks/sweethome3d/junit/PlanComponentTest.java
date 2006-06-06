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
import java.awt.Dimension;
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
import javax.swing.JComponent;
import javax.swing.JFrame;
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
        frame.planController.getMode(), PlanController.Mode.WALL_CREATION);
    // Click at (20, 20), (270, 21), (269, 170), then double click at (20, 171)
    PlanComponent plan = (PlanComponent)frame.planController.getView();
    tester.actionClick(plan, 20, 20);
    tester.actionClick(plan, 270, 21);
    tester.actionClick(plan, 269, 170);
    tester.actionClick(plan, 20, 171, InputEvent.BUTTON1_MASK, 2);
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
    assertSelectionContains(plan, wall1, wall2, wall3);
    
    // 3. Use WALL_CREATION mode
    tester.actionClick(frame.modeButton);
    // Click at (20, 170), then double click at (30, 30) with Alt key depressed
    tester.actionClick(plan, 20, 170);
    tester.actionKeyPress(KeyEvent.VK_ALT);
    tester.actionClick(plan, 30, 30, InputEvent.BUTTON1_MASK, 2);
    tester.actionKeyRelease(KeyEvent.VK_ALT);
    // Check a forth wall was created at (0, 300), (20, 20) coordinates
    Wall wall4 = orderedWalls.get(3);
    assertCoordinatesEqualWallPoints(0, 300, 20, 20, wall4);
    assertWallsAreJoined(wall3, wall4, null);

    // 4. Check current mode is SELECTION
    assertEquals("Current mode isn't " + PlanController.Mode.SELECTION, 
        frame.planController.getMode(), PlanController.Mode.SELECTION);
    // Click at (29, 32), and check the end point of the forth wall is selected 
    tester.actionClick(plan, 29, 32);
    assertSame("End point of 4th wall not selected", 
        wall4, plan.getSelectedWallEndPoint());

    // 5. Drag and drop cursor to (19, 19)
    tester.actionMousePress(plan, 
        new ComponentLocation(new Point(29, 32)));
    tester.actionMouseMove(plan, 
        new ComponentLocation(new Point(19, 19)));
    tester.actionMouseRelease();
    // Check the forth wall coordinates are (0, 300), (0, 0)
    assertCoordinatesEqualWallPoints(0, 300, 0, 0, wall4);
    // Check its end point is joined to the third wall
    assertWallsAreJoined(wall3, wall4, wall1);

    // 6. drag and drop cursor from (10, 100) to (20, 180)
    tester.actionMousePress(plan, 
        new ComponentLocation(new Point(10, 100)));
    tester.actionMouseMove(plan, 
        new ComponentLocation(new Point(20, 180)));
    tester.actionMouseRelease();
    // Check the selected walls are the third and the forth one
    assertSelectionContains(plan, wall3, wall4);

    // 7. Press twice right arrow key     
    tester.actionKeyStroke(KeyEvent.VK_RIGHT);
    tester.actionKeyStroke(KeyEvent.VK_RIGHT);
    // Check the 4 walls coordinates are (4, 0), (500, 0), (500, 300), (4, 300) 
    assertCoordinatesEqualWallPoints(4, 0, 500, 0, wall1);
    assertCoordinatesEqualWallPoints(500, 0, 500, 300, wall2);
    assertCoordinatesEqualWallPoints(500, 300, 4, 300, wall3);
    assertCoordinatesEqualWallPoints(4, 300, 4, 0, wall4);

    // 8. Click at (269, 40) with Shift key depressed
    tester.actionKeyPress(KeyEvent.VK_SHIFT);
    tester.actionClick(plan, 269, 40);
    tester.actionKeyRelease(KeyEvent.VK_SHIFT);
    // Check the second wall was added to selection
    assertSelectionContains(plan, wall2, wall3, wall4);

    // 9. Press the delete key 
    tester.actionKeyStroke(KeyEvent.VK_DELETE);
    // Check plan contains only the first wall
    assertHomeContains(frame.home, wall1);

    // 10. Drag cursor from (50, 20) to (50, 40) 
    tester.actionMousePress(plan, 
        new ComponentLocation(new Point(50, 20)));
    tester.actionMouseMove(plan, 
        new ComponentLocation(new Point(50, 40)));
    assertSelectionContains(plan, wall1);
    // Transfer focus to undo button
    tester.actionFocus(frame.undoButton);
    // Check the wall didn't move
    assertCoordinatesEqualWallPoints(4, 0, 500, 0, wall1);

    // 11. Click 6 times on undo button
    for (int i = 0; i < 6; i++) {
      tester.actionClick(frame.undoButton);
    }
    // Check home doesn't contain any wall
    assertHomeContains(frame.home);
    
    // 12. Click 6 times on redo button
    for (int i = 0; i < 6; i++) {
      tester.actionClick(frame.redoButton);
    }
    // Check plan contains only the first wall
    assertHomeContains(frame.home, wall1);
    // Check the first wall is selected
    assertSelectionContains(plan, wall1);
  }

  /**
   * Asserts the start point and the end point of 
   * <code>wall</code> are at (startX, startY), (endX, endY). 
   */
  private void assertCoordinatesEqualWallPoints(int startX, int startY, int endX, int endY, Wall wall) {
    assertEquals("Incorrect start X", startX, wall.getStartX());
    assertEquals("Incorrect start Y", startY, wall.getStartY());
    assertEquals("Incorrect end X", endY, wall.getEndX());
    assertEquals("Incorrect end Y", endY, wall.getEndY());
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
    Collection<Wall> homeWalls = home.getWalls();
    assertEquals("Home walls incorrect count", 
        walls.length, homeWalls.size());
    for (Wall wall : walls) {
      assertTrue("Wall doesn't belong to home", homeWalls.contains(wall));
    }
  }

  /**
   * Asserts <code>walls</code> are the current selected ones in <code>home</code>.
   */
  private void assertSelectionContains(PlanComponent plan, 
                                       Wall ... walls) {
    List<Wall> selectedWalls = plan.getSelectedWalls();
    assertEquals(walls.length, selectedWalls.size());
    for (Wall wall : walls) {
      assertTrue("Wall not selected", selectedWalls.contains(wall));
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
      // Add plan component to frame with its preferred size set to 540 pixels by 400
      JComponent plan = this.planController.getView();
      plan.setPreferredSize(new Dimension(540, 400));
      add(plan);
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
      this.home.addWallListener(new WallListener () {
        public void wallChanged(WallEvent ev) {
          if (ev.getType() == WallEvent.Type.ADD) {
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
