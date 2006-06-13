/*
 * PlanController.java 2 juin 2006
 *
 * Copyright (c) 2006 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.eteks.sweethome3d.swing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;

/**
 * A MVC controller for the plan view.
 * @author Emmanuel Puybaret
 */
public class PlanController implements Controller {
  public enum Mode {WALL_CREATION, SELECTION }
  
  private PlanComponent         planComponent;
  private Home                  home;
  private UserPreferences       userPreferences;
  private UndoableEditSupport   undoSupport;
  private ResourceBundle        resource;
  // Current state
  private ControllerState       state;
  // Possibles states
  private final ControllerState selectionState;
  private final ControllerState rectangleSelectionState;
  private final ControllerState selectionMoveState;
  private final ControllerState wallCreationState;
  private final ControllerState newWallState;
  // Mouse cursor position at last mouse press  
  private int xLastMousePress;
  private int yLastMousePress;
  private boolean shiftDownLastMousePress;

  public PlanController(Home home, UserPreferences userPreferences, 
                        UndoableEditSupport undoSupport) {
    this.home = home;
    this.userPreferences = userPreferences;
    this.undoSupport = undoSupport;
    this.resource  = ResourceBundle.getBundle(getClass().getName());
    // Initialize states
    this.selectionState = new SelectionState();
    this.selectionMoveState = new SelectionMoveState();
    this.rectangleSelectionState = new RectangleSelectionState();
    this.wallCreationState = new WallCreationState();
    this.newWallState = new NewWallState();
    // Set defaut state to selectionState
    this.state = this.selectionState;
    // Create view
    this.planComponent = new PlanComponent(this, home, userPreferences);
  }

  /**
   * Returns the view associated with this controller.
   */
  public JComponent getView() {
    return this.planComponent;
  }

  /**
   * Changes current state of controller.
   */
  private void setState(ControllerState state) {
    this.state.exit();
    this.state = state;
    this.state.enter();
  }
  
  /**
   * Returns the active mode of this controller.
   */
  public Mode getMode() {
    return this.state.getMode();
  }

  /**
   * Sets the active mode of this controller. 
   */
  public void setMode(Mode mode) {
    this.state.setMode(mode);
  }

  /**
   * Deletes the selection in home.
   */
  public void deleteSelection() {
    this.state.deleteSelection();
  }

  /**
   * Escapes of current action.
   */
  public void escape() {
    this.state.escape();
  }

  /**
   * Moves the selection of (<code>dx</code>,<code>dy</code>) pixels in home.
   */
  public void moveSelection(int dx, int dy) {
    this.state.moveSelection(dx, dy);
  }
  
  /**
   * Disables temporary magnetism feature of user preferences. 
   * @param magnetismDisabled if <code>true</code> then magnetism feature isn't active.
   */
  public void setMagnetismDisabled(boolean magnetismDisabled) {
    this.state.setMagnetismDisabled(magnetismDisabled);
  }

  /**
   * Processes a mouse button pressed event.
   */
  public void pressMouse(int x, int y, int clickCount, boolean shiftDown) {
    // Store the last coodinates of a mouse press
    this.xLastMousePress = x;
    this.yLastMousePress = y;
    this.shiftDownLastMousePress = shiftDown; 
    this.state.pressMouse(x, y, clickCount, shiftDown);
  }

  /**
   * Processes a mouse button released event.
   */
  public void releaseMouse(int x, int y) {
    this.state.releaseMouse(x, y);
  }

  /**
   * Processes a mouse button moved event.
   */
  public void moveMouse(int x, int y) {
    this.state.moveMouse(x, y);
  }

  /**
   * Processes a component resized event.
   * @param width the new width of the component 
   * @param height the new height of the component
   */
  public void resizeComponent(int width, int height) {
    this.state.resizeComponent(width, height);
  }

  /**
   * Returns the abscissa of mouse position at last mouse press.
   */
  private int getXLastMousePress() {
    return this.xLastMousePress;
  }

  /**
   * Returns the ordinate of mouse position at last mouse press.
   */
  private int getYLastMousePress() {
    return this.yLastMousePress;
  }
  
  /**
   * Returns <code>true</code> if shift key was down at last mouse press.
   */
  private boolean isShiftDownLastMousePress() {
    return this.shiftDownLastMousePress;
  }

  /**
   * Returns a wall instance with end points matching (<code>x0</code>, <code>y0</code>)
   * and (<code>x1</code>, <code>y1</code>). The new wall start point is joined to the 
   * <code>wallAtStart</code> closest point if it exits.
   */
  private Wall createNewWall(int x0, int y0, int x1, int y1, Wall wallAtStart) {
    float xStart = planComponent.convertPixelXToModel(x0);
    float yStart = planComponent.convertPixelYToModel(y0);
    float xEnd = planComponent.convertPixelXToModel(x1);
    float yEnd = planComponent.convertPixelYToModel(y1);
    final int defaultColor = 0xFFFFFF; // White
    // Create a new wall
    Wall newWall = new Wall(xStart, yStart, xEnd, yEnd, defaultColor, defaultColor, 
        userPreferences.getDefaultThickness());
    home.addWall(newWall);
    if (wallAtStart != null) {
      home.setWallAtStart(newWall, wallAtStart);
      float distanceStartToEnd = getSquareDistance(xStart, yStart, wallAtStart.getXEnd(), wallAtStart.getYEnd());
      float distanceStartToStart = getSquareDistance(xStart, yStart, wallAtStart.getXStart(), wallAtStart.getYStart());
      if (distanceStartToEnd < distanceStartToStart) {
        home.setWallAtEnd(wallAtStart, newWall);
      } else {
        home.setWallAtStart(wallAtStart, newWall);
      }
    }        
    return newWall;
  }
  
  /**
   * Returns the square distance between points (<code>x0</code>, <code>y0</code>) 
   * and (<code>x1</code>, <code>y1</code>).
   */
  private float getSquareDistance(float x0, float y0, float x1, float y1) {
    return (x0 - x1) * (x0 - x1) + (y0 - y1) * (y0 - y1);
  }
  
  /**
   * Joins the end point of <code>wall</code> to <code>wallAtEnd</code> start or end point.
   */
  private void joinWallEndToWall(Wall wall, Wall wallAtEnd) {
    home.setWallAtEnd(wall, wallAtEnd);
    float distanceEndToEnd = getSquareDistance(wall.getXEnd(), wall.getYEnd(), 
        wallAtEnd.getXEnd(), wallAtEnd.getYEnd());
    float distanceEndToStart = getSquareDistance(wall.getXEnd(), wall.getYEnd(), 
        wallAtEnd.getXStart(), wallAtEnd.getYStart());
    if (distanceEndToEnd < distanceEndToStart) {
      home.setWallAtEnd(wallAtEnd, wall);
      // Make wall end at the exact same position as wallAtEnd start point
      home.moveWallEndPoint(wall, wallAtEnd.getXEnd() - wall.getXEnd(), 
                                  wallAtEnd.getYEnd() - wall.getYEnd());
    } else {
      home.setWallAtStart(wallAtEnd, wall);
      // Make wall end at the exact same position as wallAtEnd start point
      home.moveWallEndPoint(wall, wallAtEnd.getXStart() - wall.getXEnd(), 
                                  wallAtEnd.getYStart() - wall.getYEnd());
    }
  }
  
  /**
   * Adds the walls in <code>joinedWalls</code> to plan component and
   * record the new walls as an udoable operation.
   */
  private void postNewWall(Wall wall) {
    final Wall [] newWalls = {wall}; 
    // Retrieve data about joined walls to wall
    final JoinedWall [] joinedWalls = JoinedWall.getJoinedWalls(newWalls);
    UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
      @Override
      public void undo() throws CannotUndoException {
        super.undo();
        doDeleteWalls(newWalls);       
      }
      
      @Override
      public void redo() throws CannotRedoException {
        super.redo();
        doAddWalls(newWalls, joinedWalls);       
      }      

      @Override
      public String getPresentationName() {
        return resource.getString("undoAddWallsName");
      }      
    };
    undoSupport.postEdit(undoableEdit);
  }

  /**
   * Adds the walls in <code>walls</code> to plan component, joins
   * them to other walls if necessary and select the added walls.
   * @param newWalls 
   */
  private void doAddWalls(Wall [] newWalls, JoinedWall [] joinedWalls) {
    // First add all walls to home
    for (JoinedWall joinedWall : joinedWalls) {
      home.addWall(joinedWall.getWall());
    }
    // Then join them to each other if necessary
    for (JoinedWall joinedWall : joinedWalls) {
      Wall wall = joinedWall.getWall();
      Wall wallAtStart = joinedWall.getWallAtStart();
      if (wallAtStart != null) {
        home.setWallAtStart(wall, wallAtStart);
        if (joinedWall.isJoinedAtEndOfWallAtStart()) {
          home.setWallAtEnd(wallAtStart, wall);
        } else if (joinedWall.isJoinedAtStartOfWallAtStart()) {
          home.setWallAtStart(wallAtStart, wall);
        }
      }
      Wall wallAtEnd = joinedWall.getWallAtEnd();
      if (wallAtEnd != null) {
        home.setWallAtEnd(wall, wallAtEnd);
        if (joinedWall.isJoinedAtStartOfWallAtEnd()) {
          home.setWallAtStart(wallAtEnd, wall);
        } else if (joinedWall.isJoinedAtEndOfWallAtEnd()) {
          home.setWallAtEnd(wallAtEnd, wall);
        }
      }
    }      
    // Select added walls
    doSelectWalls(Arrays.asList(newWalls));
  }

  /**
   * Deletes selected walls in plan and record it as an undoable operation.
   */
  private void deleteSelectedWalls() {
    List<Wall> selectedWalls = planComponent.getSelectedWalls();
    if (!selectedWalls.isEmpty()) {
      final Wall [] deletedWalls = selectedWalls.toArray(new Wall [selectedWalls.size()]);
      // Get joined walls data for undo operation
      final JoinedWall [] joinedWalls = JoinedWall.getJoinedWalls(deletedWalls);
      doDeleteWalls(deletedWalls);
      
      UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
        @Override
        public void undo() throws CannotUndoException {
          super.undo();
          doAddWalls(deletedWalls, joinedWalls);       
        }
        
        @Override
        public void redo() throws CannotRedoException {
          super.redo();
          doDeleteWalls(deletedWalls);       
        }      

        @Override
        public String getPresentationName() {
          return resource.getString("undoDeleteSelectionName");
        }      
      };
      undoSupport.postEdit(undoableEdit);
    }    
  }

  /**
   * Deletes walls in plan component and unselect all.
   */
  private void doDeleteWalls(Wall [] walls) {
    for (Wall wall : walls) {
      home.deleteWall(wall);
    }
    doDeselectAll();
  }

  /**
   * Post an undoable operation of a (<code>dx</code>, <code>dy</code>) pixels move 
   * of selection in plan component.
   */
  private void postWallsMove(final int dx, final int dy) {
    if (dx != 0 || dy != 0) {
      List<Wall> selection = planComponent.getSelectedWalls();
      final Wall [] walls = selection.toArray(new Wall [selection.size()]);
      UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
        @Override
        public void undo() throws CannotUndoException {
          super.undo();
          List<Wall> wallList = Arrays.asList(walls);
          doSelectWalls(wallList);
          doMoveWalls(wallList, -dx, -dy);       
        }
        
        @Override
        public void redo() throws CannotRedoException {
          super.redo();
          List<Wall> wallList = Arrays.asList(walls);
          doSelectWalls(wallList);
          doMoveWalls(wallList, dx, dy);      
        }      
  
        @Override
        public String getPresentationName() {
          return resource.getString("undoMoveSelectionName");
        }      
      };
      undoSupport.postEdit(undoableEdit);
    }
  }
  
  /**
   * Moves selected walls in plan component of (<code>dx</code>, <code>dy</code>) pixels
   * and record it as undoable operation.
   */
  private void moveSelectedWalls(int dx, int dy) {
    List<Wall> selection = planComponent.getSelectedWalls();
    if (!selection.isEmpty()) {
      doMoveWalls(selection, dx, dy);
      postWallsMove(dx, dy);
    }
  }
  
  /**
   * Moves <code>walls</code> in plan component of (<code>dx</code>, <code>dy</code>) pixels.
   */
  private void doMoveWalls(List<Wall> walls, int pixelDx, int pixelDy) {
    float dx = planComponent.convertPixelDistanceToModel(pixelDx);
    float dy = planComponent.convertPixelDistanceToModel(pixelDy);
    for (Wall wall : walls) {        
      home.moveWallStartPoint(wall, dx, dy);
      home.moveWallEndPoint(wall, dx, dy);
      Wall wallAtStart = wall.getWallAtStart();
      // If wall is joined to a wall at its start 
      // and that this wall doesn't belong to the list of moved walls
      if (wallAtStart != null && !walls.contains(wallAtStart)) {
        // Move the wall start point or end point
        if (wallAtStart.getWallAtStart() == wall) {
          home.moveWallStartPoint(wallAtStart, dx, dy);
        } else if (wallAtStart.getWallAtEnd() == wall) {
          home.moveWallEndPoint(wallAtStart, dx, dy);
        }
      }
      Wall wallAtEnd = wall.getWallAtEnd();
      // If wall is joined to a wall at its  
      // and that this wall doesn't belong to the list of moved walls
      if (wallAtEnd != null && !walls.contains(wallAtEnd)) {
        // Move the wall start point or end point
        if (wallAtEnd.getWallAtStart() == wall) {
          home.moveWallStartPoint(wallAtEnd, dx, dy);
        } else if (wallAtEnd.getWallAtEnd() == wall) {
          home.moveWallEndPoint(wallAtEnd, dx, dy);
        }
      }
    }
  }

  /**
   * Selects <code>selection</code> objects.
   */
  private void doSelectWalls(List<Wall> selection) {
    planComponent.setSelectedWalls(selection);
  }
  
  /**
   * Selects only a given <code>wall</code>.
   */
  private void doSelectWall(Wall wall) {
    doSelectWalls(Arrays.asList(new Wall [] {wall}));
  }

  /**
   * Deselect all walls in plan. 
   */
  private void doDeselectAll() {
    List<Wall> emptyList = Collections.emptyList();
    doSelectWalls(emptyList);
  }


  /**
   * Returns the wall at (<code>x</code>, <code>y</code>) 
   * point, with a given <code>margin</code>, different from 
   * <code>ignoredWall</code>.
   */
  private Wall getWallAt(int x, int y, int margin, Wall ignoredWall) {
    for (Wall wall : home.getWalls()) {
      if (planComponent.containsWallAt(wall, x, y, margin)
          && wall != ignoredWall) 
        return wall;
    }
    return null;
  }

  /**
   * Returns the wall at (<code>x</code>, <code>y</code>) point,  
   * which has a start or end point not joined to any wall. 
   */
  private Wall getFreeWallAt(int x, int y, Wall ignoredWall) {
    Wall wallUnderCursor = getWallAt(x, y, 1, ignoredWall);
    if (wallUnderCursor != null) {
      // Check if wallUnderCursor is already joined to an other wall
      // and if (x,y) is close to wallUnderCursor end line 
      if (wallUnderCursor != null
          && wallUnderCursor.getWallAtEnd() == null
          && planComponent.wallEndLineContains(wallUnderCursor, x, y, 5)) {
        return wallUnderCursor;
      }
      // Check if wallUnderCursor is already joined to an other wall
      // and if (x,y) is close to wallUnderCursor start line  
      if (wallUnderCursor.getWallAtStart() == null
          && planComponent.wallStartLineContains(wallUnderCursor, x, y, 5)) {
        return wallUnderCursor;
      }
    }
    return null;
  }

  /**
   * Stores the walls at start and at end of a given wall. This data 
   * are usefull to add a collection of walls after an undo/redo delete operation.
   */
  private final static class JoinedWall {
    private Wall wall;
    private Wall wallAtStart;
    private Wall wallAtEnd;
    private boolean joinedAtStartOfWallAtStart;
    private boolean joinedAtEndOfWallAtStart; 
    private boolean joinedAtStartOfWallAtEnd;
    private boolean joinedAtEndOfWallAtEnd;
    
    public JoinedWall(Wall wall) {
      this.wall = wall;
      this.wallAtStart = wall.getWallAtStart();
      if (this.wallAtStart != null) {
        this.joinedAtEndOfWallAtStart = this.wallAtStart.getWallAtEnd() == wall;
        this.joinedAtStartOfWallAtStart = this.wallAtStart.getWallAtStart() == wall;
      }
      this.wallAtEnd = wall.getWallAtEnd();
      if (this.wallAtEnd != null) {
        joinedAtEndOfWallAtEnd = wallAtEnd.getWallAtEnd() == wall;
        joinedAtStartOfWallAtEnd = wallAtEnd.getWallAtStart() == wall;
      }
    }

    public boolean isJoinedAtEndOfWallAtEnd() {
      return this.joinedAtEndOfWallAtEnd;
    }

    public boolean isJoinedAtEndOfWallAtStart() {
      return this.joinedAtEndOfWallAtStart;
    }

    public boolean isJoinedAtStartOfWallAtEnd() {
      return this.joinedAtStartOfWallAtEnd;
    }

    public boolean isJoinedAtStartOfWallAtStart() {
      return this.joinedAtStartOfWallAtStart;
    }

    public Wall getWall() {
      return this.wall;
    }

    public Wall getWallAtEnd() {
      return this.wallAtEnd;
    }

    public Wall getWallAtStart() {
      return this.wallAtStart;
    }
    
    /**
     * A helper method that builds an array of <code>JoinedWall</code> objects 
     * for a given list of walls.
     */
    public static JoinedWall [] getJoinedWalls(Wall [] walls) {
      JoinedWall [] joinedWalls = new JoinedWall [walls.length];
      for (int i = 0; i < joinedWalls.length; i++) {
        joinedWalls [i] = new JoinedWall(walls [i]);
      }
      return joinedWalls;
    }
  }
  
  /**
   * A point which coordinates are computed with a magnetism algorithm.
   */
  public static class PointWithMagnetism {
    private static final int STEP_COUNT = 24; // 15 degres step 
    private int x;
    private int y;
    
    /**
     * Create a point that applies magnetism to pixel point (<code>x</code>, <code>y</code>).
     * Point x or y coordinates may be changed, to match the closest point belonging to
     * one of the radius of a circle centered at (<code>xCenter</code>, <code>yCenter</code>,
     * each radius is a multiple of 15 degres.
     */
    public PointWithMagnetism(int xCenter, int yCenter, int x, int y) {
      this.x = x;
      this.y = y;
      if (xCenter != x && yCenter != y) {
        double angleStep = 2 * Math.PI / STEP_COUNT; 
        // Caution : pixel coordinate space is indirect !
        double angle = Math.atan2(y - yCenter, xCenter - x);
        // Compute previous angle closest to a step angle (multiple of angleStep) 
        double previousStepAngle = Math.floor((angle + Math.PI) / angleStep) * angleStep;
        double tanAngle1;
        double tanAngle2;
        // Compute the tan of previousStepAngle and the next step angle
        if (Math.tan(angle) > 0) {
          tanAngle1 = Math.tan(previousStepAngle);
          tanAngle2 = Math.tan(previousStepAngle + angleStep);
        } else {
          // If slope is negative inverse the order of the two angles
          tanAngle1 = Math.tan(previousStepAngle + angleStep);
          tanAngle2 = Math.tan(previousStepAngle);
        }
        // Search in the quarter of trigonometric circle, 
        // the point (x1,y1) or (x2,y2) closest to point (x,y) 
        // that belongs to angle 1 or angle 2 radius  
        double firstQuarterTanAngle1 = Math.abs(tanAngle1);   
        double firstQuarterTanAngle2 = Math.abs(tanAngle2);   
        int x1 = Math.abs(xCenter - x);
        int y2 = Math.abs(yCenter - y);
        int x2 = 0;
        // If angle 2 is greater than 0 rad
        if (firstQuarterTanAngle2 > 1E-10) { 
          // Compute the abscissa of point 2 that belongs to angle 1 radius at y2 ordinate 
          x2 = (int)(y2 / firstQuarterTanAngle2);
        }
        int y1 = 0;
        // If angle 1 is smaller than PI / 2 rad
        if (firstQuarterTanAngle1 < 1E10) {
          // Compute the ordinate of point 1 that belongs to angle 1 radius at x1 abscissa 
          y1 = (int)(x1 * firstQuarterTanAngle1);
        }
        
        // Apply magnetism to the smallest distance
        if (Math.abs(x2 - x1) < Math.abs(y1 - y2)) {
          this.x = xCenter + (int)((yCenter - y) / tanAngle2);            
        } else {
          this.y = yCenter - (int)((x - xCenter) * tanAngle1);
        }
      }
    }

    /**
     * Returns the abscissa of this point computed with magnetism.
     */
    int getX() {
      return this.x;
    }

    /**
     * Returns the ordinate of this point computed with magnetism.
     */
    int getY() {
      return this.y;
    }
  }

  
  /**
   * Controller state classes super class.
   */
  private abstract class ControllerState {
    public void enter() {
    }

    public void exit() {
    }

    public abstract Mode getMode();

    public void setMode(Mode mode) {
    }

    public void deleteSelection() {
    }

    public void escape() {
    }

    public void moveSelection(int dx, int dy) {
    }

    public void setMagnetismDisabled(boolean magnetismDisabled) {
    }

    public void pressMouse(int x, int y, int clickCount, boolean shiftDown) {
    }

    public void releaseMouse(int x, int y) {
    }

    public void moveMouse(int x, int y) {
    }

    public void resizeComponent(int width, int height) {
      // TODO
    }
  }

  // ControllerState subclasses
  
  /**
   * Default selection state. This state manages transition to <code>WALL_CREATION</code> mode,
   * the deleting of selected walls, and the move of selected walls with arrow keys.
   */
  private class SelectionState extends ControllerState {
    @Override
    public Mode getMode() {
      return Mode.SELECTION;
    }

    @Override
    public void enter() {
      planComponent.setCursor(getMode());
    }

    @Override
    public void setMode(Mode mode) {
      if (mode == Mode.WALL_CREATION) {
        setState(wallCreationState);
      }
    }

    @Override
    public void deleteSelection() {
      deleteSelectedWalls();
    }

    @Override
    public void moveSelection(int dx, int dy) {
      moveSelectedWalls(dx, dy);
    }

    @Override
    public void pressMouse(int x, int y, int clickCount, boolean shiftDown) {
      // If shift isn't pressed, and a wall is under cursor position
      if (!shiftDown && getWallAt(x, y, 2, null) != null) {
        // Change state to SelectionMoveState
        setState(selectionMoveState);
      } else {
        // Otherwise change state to RectangleSelectionState
        setState(rectangleSelectionState);
      }
    }
  }

  /**
   * Move selection state. This state manages the move of current selected walls with mouse 
   * and the selection of one wall, if mouse isn't moved while button is depressed. 
   */
  private class SelectionMoveState extends ControllerState {
    private int     xLastMouseMove;
    private int     yLastMouseMove;
    private boolean mouseMoved;

    @Override
    public Mode getMode() {
      return Mode.SELECTION;
    }
    
    @Override
    public void enter() {
      this.xLastMouseMove = getXLastMousePress();
      this.yLastMouseMove = getYLastMousePress();
      this.mouseMoved = false;
      Wall wallUnderCursor = getWallAt(getXLastMousePress(), getYLastMousePress(), 2, null);
      List<Wall> selection = planComponent.getSelectedWalls();
      // If the wall under the cursor doesn't belong to selection
      if (!selection.contains(wallUnderCursor)) {
        // Select only the wall under cursor position
        doSelectWall(wallUnderCursor);
      }
    }

    @Override
    public void moveMouse(int x, int y) {      
      doMoveWalls(planComponent.getSelectedWalls(), 
          x - this.xLastMouseMove, y - this.yLastMouseMove);
      this.xLastMouseMove = x;
      this.yLastMouseMove = y;
      this.mouseMoved = true;
    }

    @Override
    public void releaseMouse(int x, int y) {
      if (this.mouseMoved) {
        // Post in undo support a move operation
        postWallsMove(this.xLastMouseMove - getXLastMousePress(), 
                      this.yLastMouseMove - getYLastMousePress());
      } else {
        // If mouse didn't move, select only the wall at (x,y)
        Wall wallUnderCursor = getWallAt(x, y, 2, null);
        // Select only the wall under cursor position
        doSelectWall(wallUnderCursor);
      }
      // Change the state to SelectionState
      setState(selectionState);
    }

    @Override
    public void escape() {
      if (this.mouseMoved) {
        // Put walls back to their initial position
        doMoveWalls(planComponent.getSelectedWalls(), 
            getXLastMousePress() - this.xLastMouseMove, 
            getYLastMousePress() - this.yLastMouseMove);
      }
      // Change the state to SelectionState
      setState(selectionState);
    }
  }

  /**
   * Selection with rectangle state. This state manages selection when mouse press
   * is done outside of a wall or when mouse is press done with shift key down. 
   */
  private class RectangleSelectionState extends ControllerState {
    private List<Wall> mousePressSelection;  
    private boolean    mouseMoved;
  
    @Override
    public Mode getMode() {
      return Mode.SELECTION;
    }

    @Override
    public void enter() {
      Wall wallUnderCursor = getWallAt(getXLastMousePress(), getYLastMousePress(), 2, null);
      // If no wall under cursor and shift wasn't down, deselect all
      if (wallUnderCursor == null && !isShiftDownLastMousePress()) {
        doDeselectAll();
      } 
      // 
      this.mousePressSelection = new ArrayList<Wall>(planComponent.getSelectedWalls());
      this.mouseMoved = false;
    }

    @Override
    public void moveMouse(int x, int y) {
      this.mouseMoved = true;
      updateSelectedWalls(getXLastMousePress(), getYLastMousePress(), x, y);
      // Update rectangle feedback
      planComponent.setRectangleFeedback(getXLastMousePress(), getYLastMousePress(),
          x - getXLastMousePress(), y - getYLastMousePress());
    }

    @Override
    public void releaseMouse(int x, int y) {
      // If cursor didn't move
      if (!this.mouseMoved) {
        Wall wallUnderCursor = getWallAt(x, y, 2, null);
        // Toggle selection of the wall under cursor 
        if (wallUnderCursor != null) {
          if (this.mousePressSelection.contains(wallUnderCursor)) {
            this.mousePressSelection.remove(wallUnderCursor);
          } else {
            this.mousePressSelection.add(wallUnderCursor);
          }
          doSelectWalls(this.mousePressSelection);
        }
      }      
      // Change state to SelectionState
      setState(selectionState);
    }
    
    @Override
    public void escape() {
      setState(selectionState);
    }

    @Override
    public void exit() {
      this.mousePressSelection = null;
      planComponent.deleteFeedback();
    }

    /**
     * Updates the selection in rectangle which opposite corners are at 
     * (<code>x0</code>, <code>y0</code>) and (<code>x1</code>, <code>y1</code>)
     * coordinates.
     */
    private void updateSelectedWalls(int x0, int y0, int x1, int y1) {
      List<Wall> selectedWalls;
      boolean shiftDown = isShiftDownLastMousePress();
      if (shiftDown) {
        selectedWalls = new ArrayList<Wall>(this.mousePressSelection);
      } else {
        selectedWalls = new ArrayList<Wall>();
      }
      // For all the walls that intersects with surrounding rectangle
      for (Wall wall : home.getWalls()) {
        if (planComponent.wallIntersects(wall, x0, y0, x1, y1)) {
          // If shift was down at mouse press
          if (shiftDown) {
            // Toogle selection of the wall
            if (this.mousePressSelection.contains(wall)) {
              selectedWalls.remove(wall);
            } else {
              selectedWalls.add(wall);
            }
          } else if (!this.mousePressSelection.contains(wall)) {
            // Else select the wall
            selectedWalls.add(wall);
          }
        }    
      }
      // Update selection
      doSelectWalls(selectedWalls);
    }
  }

  /**
   * Wall creation state. This state manages transition to <code>SELECTION</code> mode,
   * and initial wall creation.
   */
  private class WallCreationState extends ControllerState {
    @Override
    public Mode getMode() {
      return Mode.WALL_CREATION;
    }

    @Override
    public void setMode(Mode mode) {
      if (mode == Mode.SELECTION) {
        // Change state to SelectionState
        setState(selectionState);
      } 
    }

    @Override
    public void enter() {
      planComponent.setCursor(getMode());
    }

    @Override
    public void pressMouse(int x, int y, int clickCount, boolean shiftDown) {
      doDeselectAll();
      // Change state to NewWallState
      setState(newWallState);
    }
  }

  /**
   * New wall state. This state manages wall creation at each mouse press. 
   */
  private class NewWallState extends ControllerState {
    private int     xLastMousePress;
    private int     yLastMousePress;
    private int     xLastMouseMove;
    private int     yLastMouseMove;
    private int     xLastMove;
    private int     yLastMove;
    private Wall    wallAtStart;
    private Wall    currentWall;
    private Wall    wallAtEnd;
    private boolean magnetismEnabled;
    
    @Override
    public Mode getMode() {
      return Mode.WALL_CREATION;
    }
    
    @Override
    public void enter() {
      this.xLastMousePress = getXLastMousePress();
      this.yLastMousePress = getYLastMousePress();
      // TODO
      // If a wall close to (xLastMousePress, y LastMousePress) is free,
      // it will the wall at start of the future created wall
      this.wallAtStart = getFreeWallAt(this.xLastMousePress, this.yLastMousePress, null);
      if (this.wallAtStart != null) {
        this.xLastMousePress = planComponent.convertModelXToPixel(this.wallAtStart.getXEnd());
        this.yLastMousePress = planComponent.convertModelYToPixel(this.wallAtStart.getYEnd());  
      }
      this.currentWall = null;
      this.wallAtEnd = null;
    }

    @Override
    public void moveMouse(int x, int y) {
      // Compute the coordinates where wall end point should be move
      int xMove = x;
      int yMove = y;
      if (this.magnetismEnabled) {
        PointWithMagnetism point = new PointWithMagnetism(
            this.xLastMousePress, this.yLastMousePress, x, y);
        xMove = point.getX();
        yMove = point.getY();
      }

      // If current wall doesn't exist
      if (this.currentWall == null) {
        // Create a new one
        this.currentWall = createNewWall(this.xLastMousePress, this.yLastMousePress, 
            xMove, yMove, this.wallAtStart);
      } else {
        // Otherwise update its end point
        home.moveWallEndPoint(this.currentWall, 
            planComponent.convertPixelDistanceToModel(xMove - this.xLastMove), 
            planComponent.convertPixelDistanceToModel(yMove - this.yLastMove));
      }         
      // Select any wall close to (xMove, yMove) to display a feedback 
      // to user about the wall the current wall will be joined to at its end  
      this.wallAtEnd = getFreeWallAt(xMove, yMove, this.currentWall);
      if (this.wallAtEnd == null) {
        doDeselectAll();
      } else {
        doSelectWall(this.wallAtEnd);          
      }
      
      // Update move coordinates
      this.xLastMouseMove = x;
      this.yLastMouseMove = y;
      this.xLastMove = xMove;
      this.yLastMove = yMove;
    }

    @Override
    public void pressMouse(int x, int y, int clickCount, boolean shiftDown) {
      if (clickCount == 2) {
        if (this.wallAtStart != null) {
          // Join to last wall if possible
          if (this.wallAtEnd != null) {
            joinWallEndToWall(this.wallAtStart, this.wallAtEnd);
          }
        }
        // Change state to WallCreationState 
        setState(wallCreationState);
      } else {
        // Create a new wall only when it may have a length > 0
        // meaning after the first mouse move
        if (this.currentWall != null) {
          doSelectWall(this.currentWall);
          // Post wall creation to undo support
          postNewWall(this.currentWall);
          this.wallAtStart = this.currentWall;
          this.currentWall = null;
          this.xLastMousePress = this.xLastMove; 
          this.yLastMousePress = this.yLastMove;
        }
      }
    }

    @Override
    public void setMagnetismDisabled(boolean magnetismDisabled) {
      this.magnetismEnabled = userPreferences.isMagnetismEnabled()
                              && !magnetismDisabled;
      if (this.currentWall != null) {
        moveMouse(this.xLastMouseMove, this.yLastMouseMove);
      }
    }

    @Override
    public void escape() {
      if (this.currentWall != null) {
        doDeleteWalls(new Wall [] {this.currentWall});
      }
      setState(wallCreationState);
    }
  }
}
