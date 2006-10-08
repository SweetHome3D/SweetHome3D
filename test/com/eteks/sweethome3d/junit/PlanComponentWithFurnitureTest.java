/*
 * PlanComponentWithFurnitureTest.java 26 juin 2006
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
import java.awt.Container;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JTree;

import junit.extensions.abbot.ComponentTestFixture;
import abbot.tester.ComponentLocation;
import abbot.tester.JButtonTester;
import abbot.tester.JTreeTester;

import com.eteks.sweethome3d.io.DefaultUserPreferences;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.swing.CatalogTree;
import com.eteks.sweethome3d.swing.HomeController;
import com.eteks.sweethome3d.swing.HomePane;
import com.eteks.sweethome3d.swing.PlanComponent;

/**
 * Tests wall and furniture management in 
 * {@link com.eteks.sweethome3d.swing.PlanComponent plan} component and 
 * its {@link com.eteks.sweethome3d.swing.PlanController controller}.
 * @author Emmanuel Puybaret
 */
public class PlanComponentWithFurnitureTest extends ComponentTestFixture {
  public void testPlanComponentWithFurniture() {
    // 1. Create a frame that displays a home view and a tool bar
    // with Mode, Add furniture, Undo and Redo buttons
    TestFrame frame = new TestFrame();    
    // Show home plan frame
    showWindow(frame);
    
    // 2. Use WALL_CREATION mode
    JButtonTester tester = new JButtonTester();
    tester.actionClick(frame.modeButton);
    PlanComponent planComponent = (PlanComponent)
      frame.homeController.getPlanController().getView();
    // Click at (20, 20), (220, 20), (270, 70), (270, 170), (20, 170) 
    // then double click at (20, 20)
    tester.actionClick(planComponent, 20, 20);
    tester.actionClick(planComponent, 220, 20);
    tester.actionClick(planComponent, 270, 70);
    tester.actionClick(planComponent, 270, 170);
    tester.actionClick(planComponent, 20, 170);
    tester.actionClick(planComponent, 20, 20, InputEvent.BUTTON1_MASK, 2);
    // Check 5 walls were added to home plan
    assertEquals("Wrong walls count", 5, 
        frame.home.getWalls().size());

    // 3. Use SELECTION mode
    tester.actionClick(frame.modeButton);
    // Select the first piece in catalog tree
    JTree catalogTree = (JTree)getComponent(
          frame.homeController.getView(), CatalogTree.class);
    catalogTree.expandRow(0); 
    catalogTree.addSelectionInterval(1, 1);
    // Click on Add furniture button
    tester.actionClick(frame.addButton);
    // Check home contains one selected piece
    assertEquals("Wrong piece count", 
        1, frame.home.getFurniture().size());
    assertEquals("Wrong selected items count", 
        1, frame.home.getSelectedItems().size());
    
    HomePieceOfFurniture piece = frame.home.getFurniture().get(0);
    float pieceX = piece.getWidth() / 2;
    float pieceY = piece.getDepth() / 2;
    assertLocationAndOrientationEqualPiece(pieceX, pieceY, 0, piece);
    
    // 4. Press mouse button at piece center
    int widthPixel = 
      Math.round(piece.getWidth() * planComponent.getScale());
    int depthPixel = 
      Math.round(piece.getDepth() * planComponent.getScale());
    tester.actionMousePress(planComponent, new ComponentLocation( 
        new Point(20 + widthPixel / 2, 20 + depthPixel / 2))); 
    // Drag mouse to (100, 100) from piece center
    tester.actionMousePress(planComponent, new ComponentLocation( 
        new Point(20 + widthPixel / 2 + 100, 20 + depthPixel / 2 + 100))); 
    tester.actionMouseRelease(); 
    // Check piece moved 
    pieceX += 200;
    pieceY += 200;
    assertLocationAndOrientationEqualPiece(pieceX, pieceY, 0, piece);
    
    // 5. Press mouse button at top left vertex of selected piece 
    tester.actionMousePress(planComponent, 
        new ComponentLocation(new Point(120, 120)));
    // Drag mouse to (-depthPixel / 2 - 2, widthPixel / 2) pixels from piece center
    tester.actionMouseMove(planComponent, new ComponentLocation( 
        new Point(120 + widthPixel / 2 - depthPixel / 2 - 2, 
                  120 + depthPixel / 2 + widthPixel / 2))); 
    tester.actionMouseRelease(); 
    // Check piece angle is 3 * PI / 2 (=-90°)
    assertLocationAndOrientationEqualPiece(
        pieceX, pieceY, (float)Math.PI * 3 / 2, piece);

    // 6. Press mouse button at top left vertex of selected piece
    tester.actionMousePress(planComponent, new ComponentLocation(
        new Point(120 + widthPixel / 2 - depthPixel / 2, 
                  120 + depthPixel / 2 + widthPixel / 2)));
    // Drag mouse to the previous position plus 2 pixels along x axis
    tester.actionMouseMove(planComponent, new ComponentLocation(
        new Point(122, 120))); 
    // Check piece angle is 0°
    assertLocationAndOrientationEqualPiece(pieceX, pieceY, 0, piece);
    // Press Shift key
    tester.actionKeyPress(KeyEvent.VK_SHIFT);
    // Check piece angle is different from 0°
    assertFalse("Piece orientation shouldn't be magnetized", 
        Math.abs(piece.getAngle()) < 1E-10);
    tester.actionKeyRelease(KeyEvent.VK_SHIFT);    
    tester.actionKeyStroke(planComponent, KeyEvent.VK_ESCAPE);
    tester.actionMouseRelease(); 
    // Check piece angle is 3 * PI / 2 (=-90°)
    assertLocationAndOrientationEqualPiece(
        pieceX, pieceY, (float)Math.PI * 3 / 2, piece);
    
    // 7. Click at point (20, 160) with Shift key depressed 
    tester.actionKeyPress(KeyEvent.VK_SHIFT);
    tester.actionClick(planComponent, 20, 160); 
    tester.actionKeyRelease(KeyEvent.VK_SHIFT);
    // Check selected items contains the piece of furniture and the fifth wall
    List<Object> selectedItems = 
      new ArrayList<Object>(frame.home.getSelectedItems());
    assertEquals("Wrong selected items count", 
        2, selectedItems.size());
    assertTrue("Piece of furniture not selected", 
        selectedItems.contains(piece));
    // Remove piece form list to get the selected wall
    selectedItems.remove(piece);
    Wall fifthWall = (Wall)selectedItems.get(0);
    // Check piece and wall coordinates
    assertLocationAndOrientationEqualPiece(
        pieceX, pieceY, (float)Math.PI * 3 / 2, piece);
    assertCoordinatesEqualWallPoints(0, 300, 0, 0, fifthWall);
    
    // 8. Drag and drop mouse to (30, 160), 
    tester.actionMousePress(planComponent, 
        new ComponentLocation(new Point(20, 160)));
    tester.actionMouseMove(planComponent,  
        new ComponentLocation(new Point(30, 160))); 
    tester.actionMouseRelease(); 
    // Check the piece of furniture moved 20 cm along x axis
    assertLocationAndOrientationEqualPiece(
        pieceX + 20, pieceY, (float)Math.PI * 3 / 2, piece);
    assertCoordinatesEqualWallPoints(20, 300, 20, 0, fifthWall);
    
    // 9. Click twice on undo button
    tester.actionClick(frame.undoButton);
    tester.actionClick(frame.undoButton);
    // Check piece orientation and location is canceled
    assertLocationAndOrientationEqualPiece(
        pieceX, pieceY, 0f, piece);
    assertCoordinatesEqualWallPoints(0, 300, 0, 0, fifthWall);
    
    // 10. Click twice on redo button
    tester.actionClick(frame.redoButton);
    tester.actionClick(frame.redoButton);
    // Check piece and wall location was redone
    assertLocationAndOrientationEqualPiece(
        pieceX + 20, pieceY, (float)Math.PI * 3 / 2, piece);
    assertCoordinatesEqualWallPoints(20, 300, 20, 0, fifthWall);
    // Check selected items contains the piece of furniture and the fifth wall
    selectedItems = frame.home.getSelectedItems();
    assertEquals("Wrong selected items count", 
        2, selectedItems.size());
    assertTrue("Piece of furniture not selected", 
        selectedItems.contains(piece));
    assertTrue("Fifth wall not selected", 
        selectedItems.contains(fifthWall));
  }
  
  /**
  * Returns the first component of a given class in <code>container</code> hierarchy.
  */
  private Component getComponent(Container container, Class componentClass) {
   for (int i = 0, n = container.getComponentCount(); i < n; i++) {
     Component component = container.getComponent(i);
     if (componentClass.isInstance(component)) {
       return component;
     } else if (component instanceof Container) {
       component = getComponent((Container)component, componentClass);
       if (component != null) {
         return component;
       }
     }
   }
   return null;
  }

  /**
   * Asserts the start point and the end point of 
   * <code>wall</code> are at (<code>xStart</code>, <code>yStart</code>), (<code>xEnd</code>, <code>yEnd</code>). 
   */
  private void assertCoordinatesEqualWallPoints(float xStart, float yStart, 
                                                float xEnd, float yEnd, 
                                                Wall wall) {
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
   * Asserts location and orientation of <code>piece</code>  
   * is at (<code>x</code>, <code>y</code>), and <code>angle</code>. 
   */
  private void assertLocationAndOrientationEqualPiece(float x, float y, 
                              float angle, HomePieceOfFurniture piece) {
    assertTrue("Incorrect X", Math.abs(x - piece.getX()) < 1E-10);
    assertTrue("Incorrect Y", Math.abs(y - piece.getY()) < 1E-10);
    assertTrue("Incorrect angle", Math.abs(angle - piece.getAngle()) < 1E-10);
  }
  
  private static class TestFrame extends JFrame {
    private final Home           home;
    private final HomeController homeController; 
    private final JToggleButton  modeButton;
    private final JButton        addButton;
    private final JButton        undoButton;
    private final JButton        redoButton;

    public TestFrame() {
      super("Home Plan Component Test");
      this.home = new Home();
      UserPreferences preferences = new DefaultUserPreferences();
      this.homeController = new HomeController(home, preferences);
      ActionMap actions = this.homeController.getView().getActionMap();
      // Create buttons from HomePane actions map
      this.modeButton = new JToggleButton(actions.get(HomePane.ActionType.WALL_CREATION));
      this.addButton = new JButton(actions.get(HomePane.ActionType.ADD_HOME_FURNITURE));
      this.undoButton = new JButton(actions.get(HomePane.ActionType.UNDO));
      this.redoButton = new JButton(actions.get(HomePane.ActionType.REDO));
      // Put them it a tool bar
      JToolBar toolBar = new JToolBar();
      toolBar.add(this.modeButton);
      toolBar.add(this.addButton);
      toolBar.add(this.undoButton);
      toolBar.add(this.redoButton);
      // Display the tool bar and main view in this pane
      add(toolBar, BorderLayout.NORTH);
      add(homeController.getView(), BorderLayout.CENTER);
      pack();
    }
  }
}
