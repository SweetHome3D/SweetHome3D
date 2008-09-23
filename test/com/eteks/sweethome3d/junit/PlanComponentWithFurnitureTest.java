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
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JTree;

import junit.extensions.abbot.ComponentTestFixture;
import abbot.tester.ComponentLocation;
import abbot.tester.JComponentTester;

import com.eteks.sweethome3d.io.DefaultUserPreferences;
import com.eteks.sweethome3d.model.DimensionLine;
import com.eteks.sweethome3d.model.DimensionLineEvent;
import com.eteks.sweethome3d.model.DimensionLineListener;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;
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
  public void testPlanComponentWithFurniture() throws InterruptedException {
    // 1. Create a frame that displays a home view and a tool bar
    // with Mode, Add furniture, Undo and Redo buttons
    TestFrame frame = new TestFrame();    
    // Show home plan frame
    showWindow(frame);
    
    // 2. Use CREATE_WALLS mode
    frame.createWallsButton.doClick();
    PlanComponent planComponent = (PlanComponent)
        frame.homeController.getPlanController().getView();
    // Click at (30, 30), (220, 30), (270, 80), (270, 170), (30, 170) 
    // then double click at (30, 30) with no magnetism
    JComponentTester tester = new JComponentTester();
    tester.actionKeyPress(KeyEvent.VK_SHIFT);
    tester.actionClick(planComponent, 30, 30);
    tester.actionClick(planComponent, 220, 30);
    tester.actionClick(planComponent, 270, 80);
    tester.actionClick(planComponent, 270, 170);
    tester.actionClick(planComponent, 30, 170);
    tester.actionClick(planComponent, 30, 30, InputEvent.BUTTON1_MASK, 2);
    tester.actionKeyRelease(KeyEvent.VK_SHIFT);
    // Check 5 walls were added to home plan
    assertEquals("Wrong walls count", 5, frame.home.getWalls().size());

    // 3. Use SELECTION mode
    frame.selectButton.doClick();
    // Select the first piece in catalog tree
    JTree catalogTree = (JTree)
        frame.homeController.getCatalogController().getView();
    catalogTree.expandRow(0); 
    catalogTree.addSelectionInterval(1, 1);
    // Click on Add furniture button
    frame.addButton.doClick();
    // Check home contains one selected piece
    assertEquals("Wrong piece count", 
        1, frame.home.getFurniture().size());
    assertEquals("Wrong selected items count", 
        1, frame.home.getSelectedItems().size());
    
    HomePieceOfFurniture piece = frame.home.getFurniture().get(0);
    float pieceWidth = piece.getWidth();
    float pieceDepth = piece.getDepth();
    float pieceHeight = piece.getHeight();
    float pieceX = pieceWidth / 2;
    float pieceY = pieceDepth / 2;
    assertLocationAndOrientationEqualPiece(pieceX, pieceY, 0, piece);
    
    // 4. Press mouse button at piece center
    int widthPixel = Math.round(pieceWidth * planComponent.getScale());
    int depthPixel = Math.round(pieceDepth * planComponent.getScale());
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
    // Drag mouse to (-depthPixel / 2 - 1, widthPixel / 2) pixels from piece center
    tester.actionMouseMove(planComponent, new ComponentLocation( 
        new Point(120 + widthPixel / 2 - depthPixel / 2 - 1, 
                  120 + depthPixel / 2 + widthPixel / 2))); 
    tester.actionMouseRelease(); 
    // Check piece angle is 3 * PI / 2 (=-90°)
    assertLocationAndOrientationEqualPiece(
        pieceX, pieceY, (float)Math.PI * 3 / 2, piece);

    // 6. Press mouse button at top left vertex of selected piece
    tester.actionMousePress(planComponent, new ComponentLocation(
        new Point(120 + widthPixel / 2 - depthPixel / 2, 
                  120 + depthPixel / 2 + widthPixel / 2)));
    // Drag mouse to the previous position plus 1 pixel along x axis
    tester.actionMouseMove(planComponent, new ComponentLocation(
        new Point(121, 120))); 
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
    
    // 7. Click at point (30, 160) with Shift key depressed 
    tester.actionKeyPress(KeyEvent.VK_SHIFT);
    tester.actionClick(planComponent, 30, 160); 
    tester.actionKeyRelease(KeyEvent.VK_SHIFT);
    // Check selected items contains the piece of furniture and the fifth wall
    List<Object> selectedItems = 
      new ArrayList<Object>(frame.home.getSelectedItems());
    assertEquals("Wrong selected items count", 2, selectedItems.size());
    assertTrue("Piece of furniture not selected", selectedItems.contains(piece));
    // Remove piece form list to get the selected wall
    selectedItems.remove(piece);
    Wall fifthWall = (Wall)selectedItems.get(0);
    // Check piece and wall coordinates
    assertLocationAndOrientationEqualPiece(
        pieceX, pieceY, (float)Math.PI * 3 / 2, piece);
    assertCoordinatesEqualWallPoints(20, 300, 20, 20, fifthWall);
    
    // 8. Drag and drop mouse to (40, 160)
    Thread.sleep(1000); // Wait 1s to avoid double click
    tester.actionMousePress(planComponent, 
        new ComponentLocation(new Point(30, 160)));
    tester.actionMouseMove(planComponent,  
        new ComponentLocation(new Point(40, 160))); 
    tester.actionMouseRelease(); 
    // Check the piece of furniture moved 20 cm along x axis
    assertLocationAndOrientationEqualPiece(
        pieceX + 20, pieceY, (float)Math.PI * 3 / 2, piece);
     assertCoordinatesEqualWallPoints(40, 300, 40, 20, fifthWall);
    
    // 9. Click twice on undo button
    frame.undoButton.doClick();
    frame.undoButton.doClick();
    // Check piece orientation and location are canceled
    assertLocationAndOrientationEqualPiece(
        pieceX, pieceY, 0f, piece);
    assertCoordinatesEqualWallPoints(20, 300, 20, 20, fifthWall);
    
    // 10. Click twice on redo button
    frame.redoButton.doClick();
    frame.redoButton.doClick();
    // Check piece and wall location was redone
    assertLocationAndOrientationEqualPiece(
        pieceX + 20, pieceY, (float)Math.PI * 3 / 2, piece);
    assertCoordinatesEqualWallPoints(40, 300, 40, 20, fifthWall);
    // Check selected items contains the piece of furniture and the fifth wall
    selectedItems = frame.home.getSelectedItems();
    assertEquals("Wrong selected items count", 
        2, selectedItems.size());
    assertTrue("Piece of furniture not selected", 
        selectedItems.contains(piece));
    assertTrue("Fifth wall not selected", 
        selectedItems.contains(fifthWall));
    
    // 11. Click at point (pieceXPixel + depthPixel / 2, pieceYPixel - widthPixel / 2) 
    //     at width and depth resize vertex of the piece
    int pieceXPixel = Math.round((piece.getX() + 40) * planComponent.getScale());
    int pieceYPixel = Math.round((piece.getY() + 40) * planComponent.getScale());
    tester.actionClick(planComponent, pieceXPixel + depthPixel / 2, pieceYPixel - widthPixel / 2);
    
    // Check selected items contains only the piece of furniture 
    selectedItems = frame.home.getSelectedItems();
    assertEquals("Wrong selected items count", 1, selectedItems.size());
    assertTrue("Piece of furniture not selected", selectedItems.contains(piece));
    // Drag mouse (4,4) pixels out of piece box with magnetism disabled
    Thread.sleep(1000); // Wait 1s to avoid double click
    tester.actionMousePress(planComponent, new ComponentLocation(new Point(
        pieceXPixel + depthPixel / 2, pieceYPixel - widthPixel / 2)));
    tester.actionKeyPress(KeyEvent.VK_SHIFT);
    tester.actionMouseMove(planComponent, new ComponentLocation(new Point(
        pieceXPixel + depthPixel / 2 + 4, pieceYPixel - widthPixel / 2 + 4))); 
    tester.actionMouseRelease();
    tester.actionKeyRelease(KeyEvent.VK_SHIFT);
    // Check piece width and depth were resized (caution : piece angle is oriented at 90°)
    assertDimensionEqualPiece(pieceWidth - 4 / planComponent.getScale(), 
        pieceDepth + 4 / planComponent.getScale(), pieceHeight, piece);

    // 12. Click at point (pieceXPixel + depthPixel / 2, pieceYPixel + widthPixel / 2) 
    //     at height resize vertex of the piece
    pieceXPixel = Math.round((piece.getX() + 40) * planComponent.getScale());
    pieceYPixel = Math.round((piece.getY() + 40) * planComponent.getScale());
    tester.actionMouseMove(planComponent, new ComponentLocation(new Point(
        pieceXPixel + depthPixel / 2, pieceYPixel + widthPixel / 2)));
    Thread.sleep(1000);
    
    tester.actionMousePress(planComponent, new ComponentLocation(new Point(
        pieceXPixel + depthPixel / 2, pieceYPixel + widthPixel / 2)));
    // Drag mouse (2,4) pixels 
    tester.actionMouseMove(planComponent, new ComponentLocation(new Point(
        pieceXPixel + depthPixel / 2 + 2, pieceYPixel + widthPixel / 2 + 4))); 
    tester.actionMouseRelease();
    // Check piece height was resized 
    assertDimensionEqualPiece(pieceWidth - 4 / planComponent.getScale(), 
        pieceDepth + 4 / planComponent.getScale(), 
        Math.round((pieceHeight - 4 / planComponent.getScale()) * 2) / 2, piece);

    // 13. Click at point (pieceXPixel - depthPixel / 2, pieceYPixel - widthPixel / 2) 
    //     at elevation vertex of the piece
    float pieceElevation = piece.getElevation();
    tester.actionMousePress(planComponent, new ComponentLocation(new Point(
        pieceXPixel - depthPixel / 2, pieceYPixel - widthPixel / 2)));
    // Drag mouse (2,-4) pixels 
    tester.actionMouseMove(planComponent, new ComponentLocation(new Point(
        pieceXPixel - depthPixel / 2 + 2, pieceYPixel - widthPixel / 2 - 4))); 
    tester.actionMouseRelease();
    // Check piece elevation was updated
    assertElevationEqualPiece(pieceElevation + 4 / planComponent.getScale(), piece);

    // 14. Click three times on undo button
    frame.undoButton.doClick();
    frame.undoButton.doClick();
    frame.undoButton.doClick();
    // Check piece dimension and elevation are canceled
    assertDimensionEqualPiece(pieceWidth, pieceDepth, pieceHeight, piece);
    assertElevationEqualPiece(pieceElevation, piece);
    
    // Build an ordered list of dimensions added to home
    final ArrayList<DimensionLine> orderedDimensionLines = new ArrayList<DimensionLine>();
    frame.home.addDimensionLineListener(new DimensionLineListener () {
      public void dimensionLineChanged(DimensionLineEvent ev) {
        if (ev.getType() == DimensionLineEvent.Type.ADD) {
          orderedDimensionLines.add(ev.getDimensionLine());
        }
      }
    });
    
    // 15. Use CREATE_DIMENSION_LINES mode
    frame.createDimensionsButton.doClick();
    // Draw a dimension in plan
    tester.actionClick(planComponent, 280, 81);
    tester.actionClick(planComponent, 281, 169, InputEvent.BUTTON1_MASK, 2);
    // Draw a dimension with extension lines
    tester.actionClick(planComponent, 41, 175);
    tester.actionClick(planComponent, 269, 175);
    tester.actionClick(planComponent, 280, 185);
    // Check 2 dimensions were added to home plan
    assertEquals("Wrong dimensions count", 2, frame.home.getDimensionLines().size());
    // Check one dimension is selected
    assertEquals("Wrong selection", 1, frame.home.getSelectedItems().size());
    assertEquals("Selection doesn't contain the second dimension", 
        frame.home.getSelectedItems().get(0), orderedDimensionLines.get(1));
    // Check the size of the created dimension lines
    DimensionLine firstDimensionLine = orderedDimensionLines.get(0);
    assertEqualsDimensionLine(520, 122, 520, 298, 0, firstDimensionLine);
    assertEqualsDimensionLine(42, 310, 498, 310, 20, orderedDimensionLines.get(1));
    
    // 16. Select the first dimension line
    frame.selectButton.doClick();
    tester.actionClick(planComponent, 280, 90);
    assertEquals("Wrong selection", 1, frame.home.getSelectedItems().size());
    assertEquals("Selection doesn't contain the first dimension", 
        frame.home.getSelectedItems().get(0), firstDimensionLine);
    // Move its end point to (330, 167)
    tester.actionMousePress(planComponent, new ComponentLocation(new Point(280, 167)));
    tester.actionMouseMove(planComponent, new ComponentLocation(new Point(320, 167)));
    // Check its coordinates while Shift key isn't pressed (with magnetism)
    assertEqualsDimensionLine(520, 122, 567.105f, 297.7985f, 0, firstDimensionLine);
    // Check its length with magnetism 
    float firstDimensionLineLength = (float)Point2D.distance(
        firstDimensionLine.getXStart(), firstDimensionLine.getYStart(), 
        firstDimensionLine.getXEnd(), firstDimensionLine.getYEnd());
    assertTrue("Incorrect length 182 " + firstDimensionLineLength, 
        Math.abs(182 - firstDimensionLineLength) < 1E-4);
    // Press Shift key
    tester.actionKeyPress(KeyEvent.VK_SHIFT);
    // Check its coordinates while Shift key is pressed (with no magnetism)
    assertEqualsDimensionLine(520, 122, 600, 298, 0, firstDimensionLine);
    // Release Shift key and mouse button
    tester.actionKeyRelease(KeyEvent.VK_SHIFT);    
    tester.actionMouseRelease();
    assertEqualsDimensionLine(520, 122, 567.105f, 297.7985f, 0, firstDimensionLine);
    
    // 17. Click three times on undo button
    frame.undoButton.doClick();
    frame.undoButton.doClick();
    frame.undoButton.doClick();
    // Check home doesn't contain any dimension
    assertEquals("Home dimensions set isn't empty", 0, frame.home.getDimensionLines().size());
    
    // 18. Click twice on redo button
    frame.redoButton.doClick();
    frame.redoButton.doClick();
    // Check the size of the created dimension lines
    assertEqualsDimensionLine(520, 122, 520, 298f, 0, firstDimensionLine);
    assertEqualsDimensionLine(42, 310, 498, 310, 20, orderedDimensionLines.get(1));
    // Click again on redo button
    frame.redoButton.doClick();
    // Check the first dimension is selected
    assertEquals("Wrong selection", 1, frame.home.getSelectedItems().size());
    assertEquals("Selection doesn't contain the first dimension", 
        frame.home.getSelectedItems().get(0), firstDimensionLine);
    // Check the coordinates of the first dimension 
    assertEqualsDimensionLine(520, 122, 567.105f, 297.7985f, 0, firstDimensionLine);
    
    // 19. Select two walls, the piece and a dimension line
    tester.actionMousePress(planComponent, new ComponentLocation(new Point(20, 100)));
    tester.actionMouseMove(planComponent, new ComponentLocation(new Point(200, 200)));
    tester.actionMouseRelease();
    // Check selection
    selectedItems = frame.home.getSelectedItems();
    assertEquals("Selection doesn't contain 4 items", 4, selectedItems.size());    
    int wallsCount = frame.home.getWalls().size();
    int furnitureCount = frame.home.getFurniture().size();
    int dimensionLinesCount = frame.home.getDimensionLines().size();
    HomePieceOfFurniture selectedPiece = Home.getFurnitureSubList(selectedItems).get(0);
    pieceX = selectedPiece.getX();
    pieceY = selectedPiece.getY();
    // Start items duplication 
    tester.actionKeyPress(KeyEvent.VK_ALT);
    tester.actionMousePress(planComponent, new ComponentLocation(new Point(50, 170)));
    // Check selection changed
    assertFalse("Selection didn't change", selectedItems.equals(frame.home.getSelectedItems()));
    assertEquals("Selection doesn't contain 4 items", 4, frame.home.getSelectedItems().size());    
    assertEquals("No new wall", wallsCount + 2, frame.home.getWalls().size());    
    assertEquals("No new piece", furnitureCount + 1, frame.home.getFurniture().size());    
    assertEquals("No new dimension lines", dimensionLinesCount + 1, frame.home.getDimensionLines().size());
    
    // 20. Duplicate and move items
    tester.actionMouseMove(planComponent, new ComponentLocation(new Point(70, 200)));
    // Check the piece moved and the original piece didn't move 
    HomePieceOfFurniture movedPiece = 
        Home.getFurnitureSubList(frame.home.getSelectedItems()).get(0);
    assertLocationAndOrientationEqualPiece(pieceX + 20 / planComponent.getScale(), 
        pieceY + 30 / planComponent.getScale(), selectedPiece.getAngle(), movedPiece);
    assertLocationAndOrientationEqualPiece(pieceX, pieceY, selectedPiece.getAngle(), selectedPiece);
    
    // 21. Release Alt key
    tester.actionKeyRelease(KeyEvent.VK_ALT);
    // Check original items replaced duplicated items
    assertTrue("Original items not selected", selectedItems.equals(frame.home.getSelectedItems()));
    assertLocationAndOrientationEqualPiece(pieceX + 20 / planComponent.getScale(), 
        pieceY + 30 / planComponent.getScale(), selectedPiece.getAngle(), selectedPiece);
    assertFalse("Duplicated piece still in home", frame.home.getFurniture().contains(movedPiece));
    // Press Alt key again 
    tester.actionKeyPress(KeyEvent.VK_ALT);
    // Check the duplicated piece moved and the original piece moved back to its original location 
    movedPiece = Home.getFurnitureSubList(frame.home.getSelectedItems()).get(0);
    assertLocationAndOrientationEqualPiece(pieceX + 20 / planComponent.getScale(), 
        pieceY + 30 / planComponent.getScale(), selectedPiece.getAngle(), movedPiece);
    assertLocationAndOrientationEqualPiece(pieceX, pieceY, selectedPiece.getAngle(), selectedPiece);
    // Press Escape key
    tester.actionKeyStroke(KeyEvent.VK_ESCAPE);
    // Check no items where duplicated
    assertTrue("Original items not selected", selectedItems.equals(frame.home.getSelectedItems()));
    assertLocationAndOrientationEqualPiece(pieceX, pieceY, selectedPiece.getAngle(), selectedPiece);
    assertEquals("New walls created", wallsCount, frame.home.getWalls().size());    
    assertEquals("New pieces created", furnitureCount, frame.home.getFurniture().size());    
    assertEquals("New dimension lines created", dimensionLinesCount, frame.home.getDimensionLines().size());
    tester.actionMouseRelease();
    
    // 22. Duplicate items
    tester.actionMousePress(planComponent, new ComponentLocation(new Point(50, 170)));
    tester.actionMouseMove(planComponent, new ComponentLocation(new Point(50, 190)));
    tester.actionMouseRelease();
    tester.actionKeyRelease(KeyEvent.VK_ALT);
    // Check the duplicated piece moved and the original piece didn't move
    List<Object> movedItems = frame.home.getSelectedItems();
    assertEquals("Selection doesn't contain 4 items", 4, movedItems.size());    
    movedPiece = Home.getFurnitureSubList(movedItems).get(0);
    assertLocationAndOrientationEqualPiece(pieceX, 
        pieceY + 20 / planComponent.getScale(), selectedPiece.getAngle(), movedPiece);
    assertLocationAndOrientationEqualPiece(pieceX, pieceY, selectedPiece.getAngle(), selectedPiece);
    // Check the duplicated walls are joined to each other
    List<Wall> movedWalls = Home.getWallsSubList(movedItems);
    Wall movedWall1 = movedWalls.get(0);
    Wall movedWall2 = movedWalls.get(1);
    assertFalse("First moved wall not new", selectedItems.contains(movedWall1));
    assertFalse("Second moved wall not new", selectedItems.contains(movedWall2));
    assertSame("First moved wall not joined to second one", movedWall2, movedWall1.getWallAtEnd());
    assertSame("Second moved wall not joined to first one", movedWall1, movedWall2.getWallAtStart());
    
    // 23. Undo duplication
    frame.undoButton.doClick();
    // Check piece and walls don't belong to home
    assertFalse("Piece still in home", frame.home.getFurniture().contains(movedPiece));
    assertFalse("First wall still in home", frame.home.getWalls().contains(movedWall1));
    assertFalse("Second wall still in home", frame.home.getWalls().contains(movedWall2));
    // Check original items are selected
    assertTrue("Original items not selected", selectedItems.equals(frame.home.getSelectedItems()));
    // Redo
    frame.redoButton.doClick();
    // Check piece and walls belong to home
    assertTrue("Piece not in home", frame.home.getFurniture().contains(movedPiece));
    assertTrue("First wall not in home", frame.home.getWalls().contains(movedWall1));
    assertTrue("Second wall not in home", frame.home.getWalls().contains(movedWall2));
    // Check moved items are selected
    assertTrue("Original items not selected", movedItems.equals(frame.home.getSelectedItems()));
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
   * Asserts the start point, the end point and the offset of 
   * <code>dimensionLine</code> are at (<code>xStart</code>, <code>yStart</code>), 
   * (<code>xEnd</code>, <code>yEnd</code>) and <code>offset</code>. 
   */
  private void assertEqualsDimensionLine(float xStart, float yStart, 
                                         float xEnd, float yEnd,
                                         float offset,
                                         DimensionLine dimensionLine) {
    assertTrue("Incorrect X start " + xStart + " " + dimensionLine.getXStart(), 
        Math.abs(xStart - dimensionLine.getXStart()) < 1E-4);
    assertTrue("Incorrect Y start " + yStart + " " + dimensionLine.getYStart(), 
        Math.abs(yStart - dimensionLine.getYStart()) < 1E-4);
    assertTrue("Incorrect X end " + xEnd + " " + dimensionLine.getXEnd(), 
        Math.abs(xEnd - dimensionLine.getXEnd()) < 1E-4);
    assertTrue("Incorrect Y end " + yEnd + " " + dimensionLine.getYEnd(), 
        Math.abs(yEnd - dimensionLine.getYEnd()) < 1E-4);
    assertTrue("Incorrect offset " + offset + " " + dimensionLine.getOffset(), 
        Math.abs(offset - dimensionLine.getOffset()) < 1E-4);
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
  
  /**
   * Asserts <code>piece</code> elevation is equal to the given  
   * <code>elevation</code>. 
   */
  private void assertElevationEqualPiece(float elevation, HomePieceOfFurniture piece) {
    assertTrue("Incorrect elevation", Math.abs(elevation - piece.getElevation()) < 1E-10);
  }
  
  /**
   * Asserts width and depth of <code>piece</code>  
   * are at <code>width</code> and <code>depth</code>). 
   */
  private void assertDimensionEqualPiece(float width, float depth, float height,
                              HomePieceOfFurniture piece) {
    assertTrue("Incorrect width " + width + " " + piece.getWidth(), 
        Math.abs(width - piece.getWidth()) < 1E-5);
    assertTrue("Incorrect depth " + depth + " " + piece.getDepth(), 
        Math.abs(depth - piece.getDepth()) < 1E-5);
    assertTrue("Incorrect height " + height + " " + piece.getHeight(), 
        Math.abs(height - piece.getHeight()) < 1E-5);
  }
  
  private static class TestFrame extends JFrame {
    private final Home           home;
    private final HomeController homeController; 
    private final JToggleButton  selectButton;
    private final JToggleButton  createWallsButton;
    private final JToggleButton  createDimensionsButton;
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
      this.selectButton = new JToggleButton(actions.get(HomePane.ActionType.SELECT));
      this.createWallsButton = new JToggleButton(actions.get(HomePane.ActionType.CREATE_WALLS));
      this.createDimensionsButton = new JToggleButton(actions.get(HomePane.ActionType.CREATE_DIMENSION_LINES));
      ButtonGroup group = new ButtonGroup();
      group.add(this.selectButton);
      group.add(this.createWallsButton);
      group.add(this.createDimensionsButton);
      this.addButton = new JButton(actions.get(HomePane.ActionType.ADD_HOME_FURNITURE));
      this.undoButton = new JButton(actions.get(HomePane.ActionType.UNDO));
      this.redoButton = new JButton(actions.get(HomePane.ActionType.REDO));
      // Put them it a tool bar
      JToolBar toolBar = new JToolBar();
      toolBar.add(this.selectButton);
      toolBar.add(this.createWallsButton);
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
