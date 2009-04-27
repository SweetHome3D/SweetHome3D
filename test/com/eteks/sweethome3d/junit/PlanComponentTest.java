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
import java.util.Locale;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEditSupport;

import junit.extensions.abbot.ComponentTestFixture;
import abbot.tester.ComponentLocation;
import abbot.tester.JComponentTester;

import com.eteks.sweethome3d.io.DefaultUserPreferences;
import com.eteks.sweethome3d.model.CollectionEvent;
import com.eteks.sweethome3d.model.CollectionListener;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.swing.PlanComponent;
import com.eteks.sweethome3d.swing.SwingViewFactory;
import com.eteks.sweethome3d.viewcontroller.PlanController;
import com.eteks.sweethome3d.viewcontroller.ViewFactory;

/**
 * Tests {@link com.eteks.sweethome3d.swing.PlanComponent plan} component and 
 * its {@link com.eteks.sweethome3d.viewcontroller.PlanController controller}.
 * @author Emmanuel Puybaret
 */
public class PlanComponentTest extends ComponentTestFixture {
  public void testPlanComponentWithMouse() {
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
    frame.home.addWallsListener(new CollectionListener<Wall>() {
      public void collectionChanged(CollectionEvent<Wall> ev) {
        if (ev.getType() == CollectionEvent.Type.ADD) {
          orderedWalls.add(ev.getItem());
        }
      }
    });
    
    // 2. Use WALL_CREATION mode
    frame.modeButton.doClick();
    assertEquals("Current mode isn't " + PlanController.Mode.WALL_CREATION, 
        PlanController.Mode.WALL_CREATION, frame.planController.getMode());
    // Click at (30, 30), (270, 31), (269, 170), then double click at (30, 171)
    JComponentTester tester = new JComponentTester();
    tester.actionClick(planComponent, 30, 30);
    tester.actionClick(planComponent, 270, 31);
    tester.actionClick(planComponent, 269, 170);
    tester.actionClick(planComponent, 30, 171, InputEvent.BUTTON1_MASK, 2);
    // Check 3 walls were created at (20, 20), (500, 20), (500, 300) and (20, 300) coordinates
    Wall wall1 = orderedWalls.get(0);
    assertCoordinatesEqualWallPoints(20, 20, 500, 20, wall1);
    Wall wall2 = orderedWalls.get(1);
    assertCoordinatesEqualWallPoints(500, 20, 500, 300, wall2);
    Wall wall3 = orderedWalls.get(2);
    assertCoordinatesEqualWallPoints(500, 300, 20, 300, wall3);
    // Check the thickness and the height of first wall
    assertEquals("Wrong wall tickness", frame.preferences.getNewWallThickness(), wall1.getThickness());
    assertEquals("Wrong wall height", frame.preferences.getNewWallHeight(), wall1.getHeight());
    assertEquals("Wrong wall height at end", null, wall1.getHeightAtEnd());
    // Check they are joined to each other end point
    assertWallsAreJoined(null, wall1, wall2); 
    assertWallsAreJoined(wall1, wall2, wall3); 
    assertWallsAreJoined(wall2, wall3, null); 
    // Check they are selected
    assertSelectionContains(frame.home, wall1, wall2, wall3);

    // 3. Click at (30, 170), then double click at (50, 50) with Shift key depressed
    tester.actionClick(planComponent, 30, 170);
    tester.actionKeyPress(KeyEvent.VK_SHIFT);
    tester.actionClick(planComponent, 50, 50, InputEvent.BUTTON1_MASK, 2);
    tester.actionKeyRelease(KeyEvent.VK_SHIFT);
    // Check a forth wall was created at (20, 300), (60, 60) coordinates
    Wall wall4 = orderedWalls.get(orderedWalls.size() - 1);
    assertCoordinatesEqualWallPoints(20, 300, 60, 60, wall4);
    assertSelectionContains(frame.home, wall4);
    assertWallsAreJoined(wall3, wall4, null);
    
    // 4. Use SELECTION mode
    frame.modeButton.doClick(); 
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
    frame.modeButton.doClick();   
    //  Click at (31, 29), then double click at (30, 170)
    tester.actionClick(planComponent, 31, 29); 
    tester.actionClick(planComponent, 30, 170, InputEvent.BUTTON1_MASK, 2);
    // Check a new forth wall was created at (20, 20), (20, 300) coordinates
    wall4 = orderedWalls.get(orderedWalls.size() - 1);
    assertCoordinatesEqualWallPoints(20, 20, 20, 300, wall4);
    // Check its end points are joined to the first and third wall
    assertWallsAreJoined(wall1, wall4, wall3);
    
    // 6. Use SELECTION mode
    frame.modeButton.doClick(); 
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
    // Check the 4 walls coordinates are (20, 20), (504, 20), (504, 300), (24, 300) 
    assertCoordinatesEqualWallPoints(20, 20, 504, 20, wall1);
    assertCoordinatesEqualWallPoints(504, 20, 504, 300, wall2);
    assertCoordinatesEqualWallPoints(504, 300, 24, 300, wall3);
    assertCoordinatesEqualWallPoints(20, 20, 24, 300, wall4);

    // 8. Click at (272, 40) with Shift key depressed
    tester.actionKeyPress(KeyEvent.VK_SHIFT);
    tester.actionClick(planComponent, 272, 40);
    tester.actionKeyRelease(KeyEvent.VK_SHIFT);
    // Check the second wall was removed from selection
    assertSelectionContains(frame.home, wall3);

     // 9. Drag cursor from (50, 30) to (50, 50) 
    tester.actionMousePress(planComponent, 
        new ComponentLocation(new Point(50, 30)));
    tester.actionMouseMove(planComponent, 
        new ComponentLocation(new Point(50, 50)));
    // Check first wall is selected and that it moved
    assertSelectionContains(frame.home, wall1);
    assertCoordinatesEqualWallPoints(20, 60, 504, 60, wall1);
    // Lose focus 
    tester.actionKeyStroke(KeyEvent.VK_TAB);
    // Check the wall didn't move at end
    assertCoordinatesEqualWallPoints(20, 20, 504, 20, wall1);

    // 10. Click 6 times on undo button
    for (int i = 0; i < 6; i++) {
      frame.undoButton.doClick();
    }
    // Check home doesn't contain any wall
    assertHomeContains(frame.home);
    
    // 11. Click 6 times on redo button
    for (int i = 0; i < 6; i++) {
      frame.redoButton.doClick();
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
    
    // 12. Reverse directions of selected walls
    float xStartWall2 = wall2.getXStart();
    float yStartWall2 = wall2.getYStart();
    float xStartWall3 = wall3.getXStart();
    float yStartWall3 = wall3.getYStart();
    float xEndWall3 = wall3.getXEnd();
    float yEndWall3 = wall3.getYEnd();
    frame.reverseDirectionButton.doClick();
    // Check the second and the third wall are still selected
    assertSelectionContains(frame.home, wall2, wall3);
    // Check wall2 and wall3 were reserved
    assertCoordinatesEqualWallPoints(xStartWall3, yStartWall3, xStartWall2, yStartWall2, wall2);
    assertCoordinatesEqualWallPoints(xEndWall3, yEndWall3, xStartWall3, yStartWall3, wall3);
    assertWallsAreJoined(wall3, wall2, wall1); 
    assertWallsAreJoined(wall4, wall3, wall2);
    
    // 13. Select first wall
    tester.actionClick(planComponent, 100, 100); // Give focus first
    tester.actionClick(planComponent, 40, 30);
    // Drag cursor from (30, 30) to (50, 50) with shift key pressed
    tester.actionMousePress(planComponent, 
        new ComponentLocation(new Point(30, 30))); 
    tester.actionMouseMove(planComponent, 
        new ComponentLocation(new Point(50, 50))); 
    tester.actionKeyPress(KeyEvent.VK_SHIFT);
    tester.waitForIdle();
    tester.actionMouseRelease(); 
    tester.actionKeyRelease(KeyEvent.VK_SHIFT);
    // Check wall start point moved to (60, 60)
    assertCoordinatesEqualWallPoints(60, 60, 504, 20, wall1);
    assertCoordinatesEqualWallPoints(60, 60, 24, 300, wall4);
    
    // 14. Select first wall 
    tester.actionClick(planComponent, 60, 50);
    assertSelectionContains(frame.home, wall1);
    // Split first wall in two walls
    frame.splitButton.doClick();
    Wall wall5 = orderedWalls.get(orderedWalls.size() - 2);
    Wall wall6 = orderedWalls.get(orderedWalls.size() - 1);
    assertSelectionContains(frame.home, wall5);
    assertCoordinatesEqualWallPoints(60, 60, 282, 40, wall5);
    assertCoordinatesEqualWallPoints(282, 40, 504, 20, wall6);
    assertWallsAreJoined(wall4, wall5, wall6); 
    assertWallsAreJoined(wall5, wall6, wall2); 
    assertFalse("Split wall still present in home", frame.home.getWalls().contains(wall1));
    // Undo operation and check undone state
    frame.undoButton.doClick();
    assertSelectionContains(frame.home, wall1);
    assertCoordinatesEqualWallPoints(60, 60, 504, 20, wall1);
    assertWallsAreJoined(wall4, wall1, wall2); 
    assertTrue("Split wall not present in home", frame.home.getWalls().contains(wall1));
    assertFalse("Wall still present in home", frame.home.getWalls().contains(wall5));
    assertFalse("Wall still present in home", frame.home.getWalls().contains(wall6));
  }

  public void testPlanComponentWithKeyboard() throws InterruptedException {
    // 1. Create a frame that displays a PlanComponent instance 
    PlanTestFrame frame = new PlanTestFrame();    
    // Show home plan frame
    showWindow(frame);
    PlanComponent planComponent = (PlanComponent)frame.planController.getView();
  
    // Build an ordered list of walls added to home
    final ArrayList<Wall> orderedWalls = new ArrayList<Wall>();
    frame.home.addWallsListener(new CollectionListener<Wall>() {
      public void collectionChanged(CollectionEvent<Wall> ev) {
        if (ev.getType() == CollectionEvent.Type.ADD) {
          orderedWalls.add(ev.getItem());
        }
      }
    });
    
    // 2. Create walls with keyboard
    frame.planController.setMode(PlanController.Mode.WALL_CREATION);
    assertEquals("Current mode isn't " + PlanController.Mode.WALL_CREATION, 
        PlanController.Mode.WALL_CREATION, frame.planController.getMode());
    planComponent.requestFocus();
    JComponentTester tester = new JComponentTester();
    tester.waitForIdle();
    assertTrue("Plan component doesn't have focus", planComponent.hasFocus());
    tester.actionKeyStroke(KeyEvent.VK_ENTER);
    // Enter the coordinates of the start point
    tester.actionKeyString("10");
    tester.actionKeyStroke(KeyEvent.VK_TAB);
    tester.actionKeyString("21");
    tester.actionKeyStroke(KeyEvent.VK_ENTER);
    // Enter the length of the wall
    tester.actionKeyString("200");
    tester.actionKeyStroke(KeyEvent.VK_ENTER);
    // Create a wall with same length
    Thread.sleep(500);
    tester.actionKeyStroke(KeyEvent.VK_ENTER);
    // Create a wall with same length, an angle at 270° and a thickness of 7,55 cm
    tester.actionKeyStroke(KeyEvent.VK_DOWN);
    tester.actionKeyStroke(KeyEvent.VK_HOME);
    tester.actionKeyString("27");
    tester.actionKeyStroke(KeyEvent.VK_DELETE); // Remove the 9 digit
    tester.actionKeyStroke(KeyEvent.VK_UP);
    tester.actionKeyStroke(KeyEvent.VK_UP);
    tester.actionKeyStroke(KeyEvent.VK_END);
    tester.actionKeyString("5 ");
    tester.actionKeyStroke(KeyEvent.VK_ENTER);
    tester.actionKeyStroke(KeyEvent.VK_ESCAPE);
    // Check created walls
    assertEquals("Wrong walls count", 3, frame.home.getWalls().size());
    Wall wall1 = orderedWalls.get(0);
    assertCoordinatesEqualWallPoints(10, 21, 210, 21, wall1);
    Wall wall2 = orderedWalls.get(1);
    assertCoordinatesEqualWallPoints(210, 21, 210, 221, wall2);
    assertEquals("Wrong wall thickness", wall1.getThickness(), wall2.getThickness());
    Wall wall3 = orderedWalls.get(2);
    assertCoordinatesEqualWallPoints(210, 221, 410, 221, wall3);
    assertEquals("Wrong wall thickness", 
        Float.parseFloat(String.valueOf(wall1.getThickness()) + "5"), wall3.getThickness());
    assertWallsAreJoined(wall1, wall2, wall3);
    assertSelectionContains(frame.home, wall1, wall2, wall3);
    
    // 3. Mix mouse and keyboard to create other walls
    tester.actionClick(planComponent, 300, 200);
    tester.actionMouseMove(planComponent, 
        new ComponentLocation(new Point(310, 200)));
    tester.actionKeyStroke(KeyEvent.VK_ENTER);
    // Enter the length and the angle of the wall
    tester.actionKeyString("100");
    tester.actionKeyStroke(KeyEvent.VK_TAB);
    tester.actionKeyString("315");
    tester.actionKeyStroke(KeyEvent.VK_ENTER);
    // Create 3 walls with same length
    Thread.sleep(500);
    tester.actionKeyStroke(KeyEvent.VK_ENTER);
    Thread.sleep(500);
    tester.actionKeyStroke(KeyEvent.VK_ENTER);
    // Take control with mouse
    tester.actionMouseMove(planComponent, 
        new ComponentLocation(new Point(200, 200)));
    tester.actionKeyPress(KeyEvent.VK_SHIFT);
    Wall wall7 = orderedWalls.get(7);
    assertCoordinatesEqualWallPoints(wall7.getXStart(), wall7.getYStart(), 
        planComponent.convertXPixelToModel(200), 
        planComponent.convertYPixelToModel(200), wall7);
    tester.waitForIdle();
    tester.actionKeyRelease(KeyEvent.VK_SHIFT);
    // Take control again with keyboard and close the walls square
    tester.actionKeyStroke(KeyEvent.VK_ENTER);
    tester.actionKeyString("100");
    tester.actionKeyStroke(KeyEvent.VK_TAB);
    tester.actionKeyString("90");
    tester.actionKeyStroke(KeyEvent.VK_ENTER);
    // Check created walls
    assertEquals("Wrong walls count", 7, frame.home.getWalls().size());
    Wall wall4 = orderedWalls.get(4);
    Wall wall5 = orderedWalls.get(5);
    Wall wall6 = orderedWalls.get(6);
    assertSelectionContains(frame.home, wall4, wall5, wall6, wall7);
    assertWallsAreJoined(wall6, wall7, wall4);
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

  public static void main(String [] args) {
    JFrame frame = new PlanTestFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);
  }
  
  private static class PlanTestFrame extends JFrame {
    private final UserPreferences preferences;
    private final Home            home;
    private final PlanController  planController;
    private final JToggleButton   modeButton;
    private final JButton         undoButton;
    private final JButton         redoButton;
    private final JButton         reverseDirectionButton;
    private final JButton         splitButton;

    public PlanTestFrame() {
      super("Plan Component Test");
      // Create model objects
      this.home = new Home();
      Locale.setDefault(Locale.FRANCE);
      this.preferences = new DefaultUserPreferences();
      ViewFactory viewFactory = new SwingViewFactory();
      UndoableEditSupport undoSupport = new UndoableEditSupport();
      final UndoManager undoManager = new UndoManager();
      undoSupport.addUndoableEditListener(undoManager);
      this.planController = new PlanController(this.home, this.preferences, viewFactory, null, undoSupport);
      // Add plan component to frame at its preferred size 
      add(new JScrollPane((JComponent)this.planController.getView()));
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
      this.reverseDirectionButton = new JButton("Reverse");
      this.reverseDirectionButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ev) {
            planController.reverseSelectedWallsDirection();
          }
        });
      this.splitButton = new JButton("Split");
      this.splitButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ev) {
            planController.splitSelectedWall();
          }
        });
      // Add a tool bar to the frame with mode toggle button, 
      // undo and redo buttons
      JToolBar toolBar = new JToolBar();
      toolBar.add(this.modeButton);
      toolBar.add(this.undoButton);
      toolBar.add(this.redoButton);
      toolBar.add(this.reverseDirectionButton);
      toolBar.add(this.splitButton);
      // Add the tool bar at top of the window
      add(toolBar, BorderLayout.NORTH);
      // Pack frame to ensure home plan component is at its preferred size 
      pack();
    }
  }
}
