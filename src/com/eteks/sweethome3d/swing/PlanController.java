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

import com.eteks.sweethome3d.model.Plan;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;

/**
 * A MVC controller for the plan view.
 * @author Emmanuel Puybaret
 */
public class PlanController implements Controller {
  public enum Mode {WALL_CREATION, SELECTION}
  
  private JComponent          planView;
  private Plan                plan;
  private UserPreferences     preferences;
  private UndoableEditSupport undoSupport;
  private ResourceBundle      resource;
  // Current state
  private ControllerState     state;
  // Possibles states
  private ControllerState     selectionState;
  private ControllerState     rectangleSelectionState;
  private ControllerState     selectionMoveState;
  private ControllerState     wallCreationState;
  private ControllerState     newWallState;
  // Mouse cursor position at last mouse press
  private float               xLastMousePress;
  private float               yLastMousePress;
  private boolean             shiftDownLastMousePress;

  /**
   * Creates the controller of plan view. 
   * @param plan        the plan edited by this controller and its view
   * @param preferences the preferences of the application
   * @param undoSupport undo support to post changes on plan by this controller
   */
  public PlanController(Plan plan, UserPreferences preferences, 
                        UndoableEditSupport undoSupport) {
    this.plan = plan;
    this.preferences = preferences;
    this.undoSupport = undoSupport;
    this.resource  = ResourceBundle.getBundle(PlanController.class.getName());
    // Create view
    this.planView = new PlanComponent(plan, preferences, this);
    // Initialize states
    this.selectionState = new SelectionState();
    this.selectionMoveState = new SelectionMoveState();
    this.rectangleSelectionState = new RectangleSelectionState();
    this.wallCreationState = new WallCreationState();
    this.newWallState = new NewWallState();
    // Set defaut state to selectionState
    setState(this.selectionState);
  }

  /**
   * Returns the view associated with this controller.
   */
  public JComponent getView() {
    return this.planView;
  }

  /**
   * Changes current state of controller.
   */
  protected void setState(ControllerState state) {
    if (this.state != null) {
      this.state.exit();
    }
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
   * Moves the selection of (<code>dx</code>,<code>dy</code>) in home.
   */
  public void moveSelection(float dx, float dy) {
    this.state.moveSelection(dx, dy);
  }
  
  /**
   * Toggles temporary magnetism feature of user preferences. 
   * @param magnetismToggled if <code>true</code> then magnetism feature isn't active.
   */
  public void toggleMagnetism(boolean magnetismToggled) {
    this.state.toggleMagnetism(magnetismToggled);
  }

  /**
   * Processes a mouse button pressed event.
   */
  public void pressMouse(float x, float y, int clickCount, boolean shiftDown) {
    // Store the last coodinates of a mouse press
    this.xLastMousePress = x;
    this.yLastMousePress = y;
    this.shiftDownLastMousePress = shiftDown; 
    this.state.pressMouse(x, y, clickCount, shiftDown);
  }

  /**
   * Processes a mouse button released event.
   */
  public void releaseMouse(float x, float y) {
    this.state.releaseMouse(x, y);
  }

  /**
   * Processes a mouse button moved event.
   */
  public void moveMouse(float x, float y) {
    this.state.moveMouse(x, y);
  }

  /**
   * Returns the selection state.
   */
  protected ControllerState getSelectionState() {
    return this.selectionState;
  }

  /**
   * Returns the selection move state.
   */
  protected ControllerState getSelectionMoveState() {
    return this.selectionMoveState;
  }

  /**
   * Returns the rectangle selection state.
   */
  protected ControllerState getRectangleSelectionState() {
    return this.rectangleSelectionState;
  }

  /**
   * Returns the wall creation state.
   */
  protected ControllerState getWallCreationState() {
    return this.wallCreationState;
  }

  /**
   * Returns the new wall state.
   */
  protected ControllerState getNewWallState() {
    return this.newWallState;
  }

  /**
   * Returns the abscissa of mouse position at last mouse press.
   */
  protected float getXLastMousePress() {
    return this.xLastMousePress;
  }

  /**
   * Returns the ordinate of mouse position at last mouse press.
   */
  protected float getYLastMousePress() {
    return this.yLastMousePress;
  }
  
  /**
   * Returns <code>true</code> if shift key was down at last mouse press.
   */
  protected boolean wasShiftDownLastMousePress() {
    return this.shiftDownLastMousePress;
  }

  /**
   * Returns a new wall instance between (<code>xStart</code>,
   * <code>yStart</code>) and (<code>xEnd</code>, <code>yEnd</code>)
   * end points. The new wall start point is joined to the start of
   * <code>wallStartAtStart</code> or the end of <code>wallEndAtStart</code>.
   */
  private Wall createNewWall(float xStart, float yStart,
                             float xEnd, float yEnd,
                             Wall wallStartAtStart,
                             Wall wallEndAtStart) {
    final int defaultColor = 0xFFFFFF; // White
    // Create a new wall
    Wall newWall = new Wall(xStart, yStart, xEnd, yEnd, 
        defaultColor, defaultColor, 
        this.preferences.getDefaultThickness());
    this.plan.addWall(newWall);
    if (wallStartAtStart != null) {
      this.plan.setWallAtStart(newWall, wallStartAtStart);
      this.plan.setWallAtStart(wallStartAtStart, newWall);
    } else if (wallEndAtStart != null) {
      this.plan.setWallAtStart(newWall, wallEndAtStart);
      this.plan.setWallAtEnd(wallEndAtStart, newWall);
    }        
    return newWall;
  }
  
  /**
   * Joins the end point of <code>wall</code> to the start of
   * <code>wallStartAtEnd</code> or the end of <code>wallEndAtEnd</code>.
   */
  private void joinNewWallEndToWall(Wall wall, JoinedWall joinedWall, 
                                 Wall wallStartAtEnd, Wall wallEndAtEnd) {
    if (wallStartAtEnd != null) {
      this.plan.setWallAtEnd(wall, wallStartAtEnd);
      this.plan.setWallAtStart(wallStartAtEnd, wall);
      // Update joinedWall data created by postAddWall
      joinedWall.setWallAtEnd(wallStartAtEnd);
      joinedWall.setJoinedAtStartOfWallAtEnd(true);
      // Make wall end at the exact same position as wallAtEnd start point
      this.plan.moveWallEndPointTo(wall, wallStartAtEnd.getXStart(),
          wallStartAtEnd.getYStart());
    } else if (wallEndAtEnd != null) {
      this.plan.setWallAtEnd(wall, wallEndAtEnd);
      this.plan.setWallAtEnd(wallEndAtEnd, wall);
      // Update joinedWall data created by postAddWall
      joinedWall.setWallAtEnd(wallEndAtEnd);
      joinedWall.setJoinedAtEndOfWallAtEnd(true);
      // Make wall end at the exact same position as wallAtEnd end point
      this.plan.moveWallEndPointTo(wall, wallEndAtEnd.getXEnd(),
          wallEndAtEnd.getYEnd());
    }
  }
  
  /**
   * Returns the wall at (<code>x</code>, <code>y</code>) 
   * point different from <code>ignoredWall</code>.
   */
  private Wall getWallAt(float x, float y, Wall ignoredWall) {
    for (Wall wall : this.plan.getWalls()) {
      if (wall != ignoredWall
          && ((PlanComponent)getView()).containsWallAt(wall, x, y)) 
        return wall;
    }
    return null;
  }

  /**
   * Returns the wall at (<code>x</code>, <code>y</code>) point,  
   * which has a start point not joined to any wall. 
   */
  private Wall getWallStartAt(float x, float y, Wall ignoredWall) {
    for (Wall wall : this.plan.getWalls()) {
      if (wall != ignoredWall
          && wall.getWallAtStart() == null
          && ((PlanComponent)getView()).containsWallStartAt(wall, x, y)) 
        return wall;
    }
    return null;
  }

  /**
   * Returns the wall at (<code>x</code>, <code>y</code>) point,  
   * which has a end point not joined to any wall. 
   */
  private Wall getWallEndAt(float x, float y, Wall ignoredWall) {
    for (Wall wall : this.plan.getWalls()) {
      if (wall != ignoredWall
          && wall.getWallAtEnd() == null
          && ((PlanComponent)getView()).containsWallEndAt(wall, x, y)) 
        return wall;
    }
    return null;
  }

  /**
   * Deletes selected walls in plan and record it as an undoable operation.
   */
  private void deleteSelectedWalls() {
    List<Wall> selectedWalls = ((PlanComponent)getView()).getSelectedWalls();
    if (!selectedWalls.isEmpty()) {
      // First post to undo support that walls are deleted, 
      // otherwise data about joined walls can't be stored 
      postDeleteWalls(selectedWalls);
      // Then delete walls from plan
      for (Wall wall : selectedWalls) {
        this.plan.deleteWall(wall);
      }
      deselectAll();
    }      
  }

  /**
   * Moves and shows selected walls in plan component of (<code>dx</code>,
   * <code>dy</code>) units and record it as undoable operation.
   */
  private void moveAndShowSelectedWalls(float dx, float dy) {
    List<Wall> selectedWalls = ((PlanComponent)getView()).getSelectedWalls();
    if (!selectedWalls.isEmpty()) {
      moveWalls(selectedWalls, dx, dy);
      ((PlanComponent)getView()).ensureSelectionIsVisible();
      postWallsMove(selectedWalls, dx, dy);
    }
  }

  /**
   * Moves <code>walls</code> in plan component of (<code>dx</code>,
   * <code>dy</code>) units.
   */
  private void moveWalls(List<Wall> walls, float dx, float dy) {
    for (Wall wall : walls) {        
      this.plan.moveWallStartPointTo(wall, 
          wall.getXStart() + dx, wall.getYStart() + dy);
      this.plan.moveWallEndPointTo(wall, 
          wall.getXEnd() + dx, wall.getYEnd() + dy);
      Wall wallAtStart = wall.getWallAtStart();
      // If wall is joined to a wall at its start 
      // and this wall doesn't belong to the list of moved walls
      if (wallAtStart != null && !walls.contains(wallAtStart)) {
        // Move the wall start point or end point
        if (wallAtStart.getWallAtStart() == wall) {
          this.plan.moveWallStartPointTo(wallAtStart, 
              wallAtStart.getXStart() + dx, 
              wallAtStart.getYStart() + dy);
        } else if (wallAtStart.getWallAtEnd() == wall) {
          this.plan.moveWallEndPointTo(wallAtStart, 
              wallAtStart.getXEnd() + dx, 
              wallAtStart.getYEnd() + dy);
        }
      }
      Wall wallAtEnd = wall.getWallAtEnd();
      // If wall is joined to a wall at its end  
      // and this wall doesn't belong to the list of moved walls
      if (wallAtEnd != null && !walls.contains(wallAtEnd)) {
        // Move the wall start point or end point
        if (wallAtEnd.getWallAtStart() == wall) {
          this.plan.moveWallStartPointTo(wallAtEnd, 
              wallAtEnd.getXStart() + dx, 
              wallAtEnd.getYStart() + dy);
        } else if (wallAtEnd.getWallAtEnd() == wall) {
          this.plan.moveWallEndPointTo(wallAtEnd, 
              wallAtEnd.getXEnd() + dx, 
              wallAtEnd.getYEnd() + dy);
        }
      }
    }
  }
  
  /**
   * Selects <code>walls</code> walls and make them visible at screen.
   */
  private void selectAndShowWalls(List<Wall> walls) {
    selectWalls(walls);
    ((PlanComponent)getView()).ensureSelectionIsVisible();
  }
  
  /**
   * Selects <code>walls</code> walls.
   */
  private void selectWalls(List<Wall> walls) {
    ((PlanComponent)getView()).setSelectedWalls(walls);
  }
  
  /**
   * Selects only a given <code>wall</code>.
   */
  private void selectWall(Wall wall) {
    selectWalls(Arrays.asList(new Wall [] {wall}));
  }

  /**
   * Deselect all walls in plan. 
   */
  private void deselectAll() {
    List<Wall> emptyList = Collections.emptyList();
    selectWalls(emptyList);
  }

  /**
   * Posts an undoable new wall operation, about <code>newWall</code>.
   */
  private JoinedWall postAddWall(Wall newWall, List<Wall> oldSelection) {
    // Retrieve data about joined walls to newWall
    final JoinedWall [] joinedNewWall = {new JoinedWall(newWall)};
    final Wall [] oldSelectedWalls = 
      oldSelection.toArray(new Wall [oldSelection.size()]);
    UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
      @Override
      public void undo() throws CannotUndoException {
        super.undo();
        doDeleteWalls(joinedNewWall);
        selectAndShowWalls(Arrays.asList(oldSelectedWalls));
      }
      
      @Override
      public void redo() throws CannotRedoException {
        super.redo();
        doAddAndShowWalls(joinedNewWall);       
      }      

      @Override
      public String getPresentationName() {
        return resource.getString("undoAddWallsName");
      }      
    };
    this.undoSupport.postEdit(undoableEdit);
    return joinedNewWall [0];
  }

  /**
   * Adds the walls in <code>joinedNewWalls</code> to plan component, joins
   * them to other walls if necessary and select the added walls.
   */
  private void doAddAndShowWalls(JoinedWall [] joinedNewWalls) {
    // First add all walls to home
    for (JoinedWall joinedNewWall : joinedNewWalls) {
      this.plan.addWall(joinedNewWall.getWall());
    }
    // Then join them to each other if necessary
    for (JoinedWall joinedNewWall : joinedNewWalls) {
      Wall wall = joinedNewWall.getWall();
      Wall wallAtStart = joinedNewWall.getWallAtStart();
      if (wallAtStart != null) {
        this.plan.setWallAtStart(wall, wallAtStart);
        if (joinedNewWall.isJoinedAtEndOfWallAtStart()) {
          this.plan.setWallAtEnd(wallAtStart, wall);
        } else if (joinedNewWall.isJoinedAtStartOfWallAtStart()) {
          this.plan.setWallAtStart(wallAtStart, wall);
        }
      }
      Wall wallAtEnd = joinedNewWall.getWallAtEnd();
      if (wallAtEnd != null) {
        this.plan.setWallAtEnd(wall, wallAtEnd);
        if (joinedNewWall.isJoinedAtStartOfWallAtEnd()) {
          this.plan.setWallAtStart(wallAtEnd, wall);
        } else if (joinedNewWall.isJoinedAtEndOfWallAtEnd()) {
          this.plan.setWallAtEnd(wallAtEnd, wall);
        }
      }
    }      
    // Select added walls
    selectAndShowWalls(JoinedWall.getWalls(joinedNewWalls));
  }

  /**
   * Posts an undoable delete wall operation, about <code>deletedWalls</code>.
   */
  private void postDeleteWalls(List<Wall> deletedWalls) {
    // Get joined walls data for undo operation
    final JoinedWall [] joinedDeletedWalls = 
      JoinedWall.getJoinedWalls(deletedWalls);
    UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
      @Override
      public void undo() throws CannotUndoException {
        super.undo();
        doAddAndShowWalls(joinedDeletedWalls);       
      }
      
      @Override
      public void redo() throws CannotRedoException {
        super.redo();
        doDeleteWalls(joinedDeletedWalls);       
      }      

      @Override
      public String getPresentationName() {
        return resource.getString("undoDeleteSelectionName");
      }      
    };
    this.undoSupport.postEdit(undoableEdit);
  }

  /**
   * Deletes walls referenced in <code>joinedDeletedWalls</code> and unselect all.
   */
  private void doDeleteWalls(JoinedWall [] joinedDeletedWalls) {
    for (JoinedWall joinedWall : joinedDeletedWalls) {
      this.plan.deleteWall(joinedWall.getWall());
    }
    deselectAll();
  }

  /**
   * Posts an undoable operation of a (<code>dx</code>, <code>dy</code>) move 
   * of <code>movedWalls</code>.
   */
  private void postWallsMove(List<Wall> movedWalls, 
                             final float dx, final float dy) {
    if (dx != 0 || dy != 0) {
      // Store the moved walls in an array
      final Wall [] wallsArray = 
        movedWalls.toArray(new Wall [movedWalls.size()]);
      UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
        @Override
        public void undo() throws CannotUndoException {
          super.undo();
          doMoveAndShowWalls(wallsArray, -dx, -dy);       
        }
        
        @Override
        public void redo() throws CannotRedoException {
          super.redo();
          doMoveAndShowWalls(wallsArray, dx, dy);   
        }      
  
        @Override
        public String getPresentationName() {
          return resource.getString("undoMoveSelectionName");
        }      
      };
      this.undoSupport.postEdit(undoableEdit);
    }
  }

  /** 
   * Moves <code>movedWalls</code> of (<code>dx</code>, <code>dy</code>) pixels, 
   * selects them and make them visible.
   */
  private void doMoveAndShowWalls(Wall [] movedWalls, 
                                  float dx, float dy) {
    List<Wall> wallsList = Arrays.asList(movedWalls);
    moveWalls(wallsList, dx, dy);   
    selectAndShowWalls(wallsList);
  }

  /**
   * Stores the walls at start and at end of a given wall. This data are usefull
   * to add a collection of walls after an undo/redo delete operation.
   */
  private static class JoinedWall {
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
        this.joinedAtEndOfWallAtStart = 
          this.wallAtStart.getWallAtEnd() == wall;
        this.joinedAtStartOfWallAtStart = 
          this.wallAtStart.getWallAtStart() == wall;
      }
      this.wallAtEnd = wall.getWallAtEnd();
      if (this.wallAtEnd != null) {
        this.joinedAtEndOfWallAtEnd = 
          wallAtEnd.getWallAtEnd() == wall;
        this.joinedAtStartOfWallAtEnd = 
          wallAtEnd.getWallAtStart() == wall;
      }
    }

    public Wall getWall() {
      return this.wall;
    }

    public Wall getWallAtEnd() {
      return this.wallAtEnd;
    }

    public void setWallAtEnd(Wall wallAtEnd) {
      this.wallAtEnd = wallAtEnd;
    }

    public Wall getWallAtStart() {
      return this.wallAtStart;
    }

    public boolean isJoinedAtEndOfWallAtStart() {
      return this.joinedAtEndOfWallAtStart;
    }

    public boolean isJoinedAtStartOfWallAtStart() {
      return this.joinedAtStartOfWallAtStart;
    }

    public void setJoinedAtEndOfWallAtEnd(
                     boolean joinedAtEndOfWallAtEnd) {
      this.joinedAtEndOfWallAtEnd = joinedAtEndOfWallAtEnd;
    }

    public boolean isJoinedAtEndOfWallAtEnd() {
      return this.joinedAtEndOfWallAtEnd;
    }

    public boolean isJoinedAtStartOfWallAtEnd() {
      return this.joinedAtStartOfWallAtEnd;
    }

    public void setJoinedAtStartOfWallAtEnd(
                     boolean joinedAtStartOfWallAtEnd) {
      this.joinedAtStartOfWallAtEnd = joinedAtStartOfWallAtEnd;
    }

    /**
     * A helper method that builds an array of <code>JoinedWall</code> objects 
     * for a given list of walls.
     */
    public static JoinedWall [] getJoinedWalls(List<Wall> walls) {
      JoinedWall [] joinedWalls = new JoinedWall [walls.size()];
      for (int i = 0; i < joinedWalls.length; i++) {
        joinedWalls [i] = new JoinedWall(walls.get(i));
      }
      return joinedWalls;
    }
    
    /**
     * A helper method that builds a list of <code>Wall</code> objects 
     * for a given array of <code>JoinedWall</code> objects.
     */
    public static List<Wall> getWalls(JoinedWall [] joinedWalls) {
      Wall [] walls = new Wall [joinedWalls.length];
      for (int i = 0; i < joinedWalls.length; i++) {
        walls [i] = joinedWalls [i].getWall();
      }
      return Arrays.asList(walls);
    }
  }

  /**
   * A point which coordinates are computed with a magnetism algorithm.
   */
  public static class PointWithMagnetism {
    private static final int STEP_COUNT = 24; // 15 degres step 
    private float xEnd;
    private float yEnd;
    
    /**
     * Create a point that applies magnetism to point (<code>x</code>,
     * <code>y</code>). Point xEnd or yEnd coordinates may be different from
     * x or y, to match the closest point belonging to one of the radius of a
     * circle centered at (<code>xStart</code>, <code>yStart</code>, each
     * radius being a multiple of 15 degres.
     */
    public PointWithMagnetism(float xStart, float yStart, float x, float y) {
      this.xEnd = x;
      this.yEnd = y;
      if (xStart != x && yStart != y) {
        double angleStep = 2 * Math.PI / STEP_COUNT; 
        // Caution : pixel coordinate space is indirect !
        double angle = Math.atan2(yStart - y, x - xStart);
        // Compute previous angle closest to a step angle (multiple of angleStep) 
        double previousStepAngle = Math.floor(angle / angleStep) * angleStep;
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
        // Search in the first quarter of the trigonometric circle, 
        // the point (xEnd1,yEnd1) or (xEnd2,yEnd2) closest to point 
        // (xEnd,yEnd) that belongs to angle 1 or angle 2 radius  
        double firstQuarterTanAngle1 = Math.abs(tanAngle1);   
        double firstQuarterTanAngle2 = Math.abs(tanAngle2);   
        float xEnd1 = Math.abs(xStart - x);
        float yEnd2 = Math.abs(yStart - y);
        float xEnd2 = 0;
        // If angle 2 is greater than 0 rad
        if (firstQuarterTanAngle2 > 1E-10) { 
          // Compute the abscissa of point 2 that belongs to angle 1 radius at
          // y2 ordinate
          xEnd2 = (float)(yEnd2 / firstQuarterTanAngle2);
        }
        float yEnd1 = 0;
        // If angle 1 is smaller than PI / 2 rad
        if (firstQuarterTanAngle1 < 1E10) {
          // Compute the ordinate of point 1 that belongs to angle 1 radius at
          // x1 abscissa
          yEnd1 = (float)(xEnd1 * firstQuarterTanAngle1);
        }
        
        // Apply magnetism to the smallest distance
        if (Math.abs(xEnd2 - xEnd1) < Math.abs(yEnd1 - yEnd2)) {
          this.xEnd = xStart + (float)((yStart - y) / tanAngle2);            
        } else {
          this.yEnd = yStart - (float)((x - xStart) * tanAngle1);
        }
      }
    }

    /**
     * Returns the abscissa of end point computed with magnetism.
     */
    float getXEnd() {
      return this.xEnd;
    }

    /**
     * Returns the ordinate of end point computed with magnetism.
     */
    float getYEnd() {
      return this.yEnd;
    }
  }
 
  /**
   * Controller state classes super class.
   */
  protected static abstract class ControllerState {
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

    public void moveSelection(float dx, float dy) {
    }

    public void toggleMagnetism(boolean magnetismToggled) {
    }

    public void pressMouse(float x, float y, int clickCount, boolean shiftDown) {
    }

    public void releaseMouse(float x, float y) {
    }

    public void moveMouse(float x, float y) {
    }
  }

  // ControllerState subclasses
  
  /**
   * Default selection state. This state manages transition to
   * <code>WALL_CREATION</code> mode, the deleting of selected walls, 
   * and the move of selected walls with arrow keys.
   */
  private class SelectionState extends ControllerState {
    @Override
    public Mode getMode() {
      return Mode.SELECTION;
    }

    @Override
    public void enter() {
      ((PlanComponent)getView()).setCursor(getMode());
    }

    @Override
    public void setMode(Mode mode) {
      if (mode == Mode.WALL_CREATION) {
        setState(getWallCreationState());
      }
    }

    @Override
    public void deleteSelection() {
      deleteSelectedWalls();
    }

    @Override
    public void moveSelection(float dx, float dy) {
      moveAndShowSelectedWalls(dx, dy);
    }

    @Override
    public void pressMouse(float x, float y, int clickCount,
                           boolean shiftDown) {
      // If shift isn't pressed, and a wall is under cursor position
      if (!shiftDown && getWallAt(x, y, null) != null) {
        // Change state to SelectionMoveState
        setState(getSelectionMoveState());
      } else {
        // Otherwise change state to RectangleSelectionState
        setState(getRectangleSelectionState());
      }
    }
  }

  /**
   * Move selection state. This state manages the move of current selected walls
   * with mouse and the selection of one wall, if mouse isn't moved while button
   * is depressed.
   */
  private class SelectionMoveState extends ControllerState {
    private float   xLastMouseMove;
    private float   yLastMouseMove;
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
      Wall wallUnderCursor = getWallAt(getXLastMousePress(),
          getYLastMousePress(), null);
      List<Wall> selection = ((PlanComponent)getView()).getSelectedWalls();
      // If the wall under the cursor doesn't belong to selection
      if (!selection.contains(wallUnderCursor)) {
        // Select only the wall under cursor position
        selectWall(wallUnderCursor);
      }
    }

    @Override
    public void moveMouse(float x, float y) {      
      moveWalls(((PlanComponent)getView()).getSelectedWalls(), 
          x - this.xLastMouseMove, y - this.yLastMouseMove);
      ((PlanComponent)getView()).ensurePointIsVisible(x, y);
      this.xLastMouseMove = x;
      this.yLastMouseMove = y;
      this.mouseMoved = true;
    }

    @Override
    public void releaseMouse(float x, float y) {
      if (this.mouseMoved) {
        // Post in undo support a move operation
        postWallsMove(((PlanComponent)getView()).getSelectedWalls(),
            this.xLastMouseMove - getXLastMousePress(), 
            this.yLastMouseMove - getYLastMousePress());
      } else {
        // If mouse didn't move, select only the wall at (x,y)
        Wall wallUnderCursor = getWallAt(x, y, null);
        // Select only the wall under cursor position
        selectWall(wallUnderCursor);
      }
      // Change the state to SelectionState
      setState(getSelectionState());
    }

    @Override
    public void escape() {
      if (this.mouseMoved) {
        // Put walls back to their initial position
        moveWalls(((PlanComponent)getView()).getSelectedWalls(), 
            getXLastMousePress() - this.xLastMouseMove, 
            getYLastMousePress() - this.yLastMouseMove);
      }
      // Change the state to SelectionState
      setState(getSelectionState());
    }
  }

  /**
   * Selection with rectangle state. This state manages selection when mouse
   * press is done outside of a wall or when mouse press is done with shift key
   * down.
   */
  private class RectangleSelectionState extends ControllerState {
    private List<Wall> selectedWallsMousePressed;  
    private boolean    mouseMoved;
  
    @Override
    public Mode getMode() {
      return Mode.SELECTION;
    }

    @Override
    public void enter() {
      Wall wallUnderCursor = 
        getWallAt(getXLastMousePress(), getYLastMousePress(), null);
      // If no wall under cursor and shift wasn't down, deselect all
      if (wallUnderCursor == null && !wasShiftDownLastMousePress()) {
        deselectAll();
      } 
      // Store current selection
      this.selectedWallsMousePressed = 
        new ArrayList<Wall>(((PlanComponent)getView()).getSelectedWalls());
      this.mouseMoved = false;
    }

    @Override
    public void moveMouse(float x, float y) {
      this.mouseMoved = true;
      updateSelectedWalls(getXLastMousePress(), getYLastMousePress(), 
          x, y, this.selectedWallsMousePressed);
      // Update rectangle feedback
      ((PlanComponent)getView()).setRectangleFeedback(
          getXLastMousePress(), getYLastMousePress(), x, y);
      ((PlanComponent)getView()).ensurePointIsVisible(x, y);
    }

    @Override
    public void releaseMouse(float x, float y) {
      // If cursor didn't move
      if (!this.mouseMoved) {
        Wall wallUnderCursor = getWallAt(x, y, null);
        // Toggle selection of the wall under cursor 
        if (wallUnderCursor != null) {
          if (this.selectedWallsMousePressed.contains(wallUnderCursor)) {
            this.selectedWallsMousePressed.remove(wallUnderCursor);
          } else {
            this.selectedWallsMousePressed.add(wallUnderCursor);
          }
          selectWalls(this.selectedWallsMousePressed);
        }
      }      
      // Change state to SelectionState
      setState(getSelectionState());
    }
    
    @Override
    public void escape() {
      setState(getSelectionState());
    }

    @Override
    public void exit() {
      this.selectedWallsMousePressed = null;
      ((PlanComponent)getView()).deleteRectangleFeedback();
    }

    /**
     * Updates selection from <code>selectedWallsMousePressed</code> and the
     * walls that intersects the rectangle at coordinates (<code>x0</code>,
     * <code>y0</code>) and (<code>x1</code>, <code>y1</code>).
     */
    private void updateSelectedWalls(float x0, float y0, 
                                     float x1, float y1,
                                     List<Wall> selectedWallsMousePressed) {
      List<Wall> selectedWalls;
      boolean shiftDown = wasShiftDownLastMousePress();
      if (shiftDown) {
        selectedWalls = new ArrayList<Wall>(selectedWallsMousePressed);
      } else {
        selectedWalls = new ArrayList<Wall>();
      }
      
      // For all the walls that intersects with surrounding rectangle
      for (Wall wall : plan.getWalls()) {
        if (((PlanComponent)getView()).doesWallIntersectRectangle(wall, x0, y0, x1, y1)) {
          // If shift was down at mouse press
          if (shiftDown) {
            // Toogle selection of the wall
            if (selectedWallsMousePressed.contains(wall)) {
              selectedWalls.remove(wall);
            } else {
              selectedWalls.add(wall);
            }
          } else if (!selectedWallsMousePressed.contains(wall)) {
            // Else select the wall
            selectedWalls.add(wall);
          }
        }    
      }
      // Update selection
      selectWalls(selectedWalls);
    }
  }

  /**
   * Wall creation state. This state manages transition to
   * <code>SELECTION</code> mode, and initial wall creation.
   */
  private class WallCreationState extends ControllerState {
    @Override
    public Mode getMode() {
      return Mode.WALL_CREATION;
    }

    @Override
    public void enter() {
      ((PlanComponent)getView()).setCursor(getMode());
    }

    @Override
    public void setMode(Mode mode) {
      if (mode == Mode.SELECTION) {
        // Change state to SelectionState
        setState(getSelectionState());
      } 
    }

    @Override
    public void pressMouse(float x, float y, int clickCount,
                           boolean shiftDown) {
      // Change state to NewWallState
      setState(getNewWallState());
    }
  }

  /**
   * New wall state. This state manages wall creation at each mouse press. 
   */
  private class NewWallState extends ControllerState {
    private float      xStart;
    private float      yStart;
    private float      xLastEnd;
    private float      yLastEnd;
    private float      xLastMouseMove;
    private float      yLastMouseMove;
    private Wall       wallStartAtStart;
    private Wall       wallEndAtStart;
    private Wall       newWall;
    private Wall       wallStartAtEnd;
    private Wall       wallEndAtEnd;
    private Wall       lastWall;
    private JoinedWall joinedLastWall;
    private List<Wall> oldSelection;
    private boolean    magnetismEnabled;
    
    @Override
    public Mode getMode() {
      return Mode.WALL_CREATION;
    }
    
    @Override
    public void enter() {
      this.oldSelection = ((PlanComponent)getView()).getSelectedWalls();
      deselectAll();
      toggleMagnetism(wasShiftDownLastMousePress());
      this.xStart = getXLastMousePress();
      this.yStart = getYLastMousePress();
      // If the start or end line of a wall close to (xStart, yStart) is
      // free, it will the wall at start of the new wall
      this.wallEndAtStart = getWallEndAt(this.xStart, this.yStart, null);
      if (this.wallEndAtStart != null) {
        this.wallStartAtStart = null;
        this.xStart = this.wallEndAtStart.getXEnd();
        this.yStart = this.wallEndAtStart.getYEnd();  
      } else {
        this.wallStartAtStart = getWallStartAt(
            this.xStart, this.yStart, null);
        if (this.wallStartAtStart != null) {
          this.xStart = this.wallStartAtStart.getXStart();
          this.yStart = this.wallStartAtStart.getYStart();        
        }
      }
      this.newWall = null;
      this.wallStartAtEnd = null;
      this.wallEndAtEnd = null;
      this.lastWall = null;
    }

    @Override
    public void moveMouse(float x, float y) {
      // Compute the coordinates where wall end point should be moved
      float xEnd;
      float yEnd;
      if (this.magnetismEnabled) {
        PointWithMagnetism point = new PointWithMagnetism(
            this.xStart, this.yStart, x, y);
        xEnd = point.getXEnd();
        yEnd = point.getYEnd();
      } else {
        xEnd = x;
        yEnd = y;
      }

      // If current wall doesn't exist
      if (this.newWall == null) {
        // Create a new one
        this.newWall = createNewWall(this.xStart, this.yStart, 
            xEnd, yEnd, this.wallStartAtStart, this.wallEndAtStart);
      } else {
        // Otherwise update its end point
        plan.moveWallEndPointTo(this.newWall, xEnd, yEnd); 
      }         
      
      // If the start or end line of a wall close to (xEnd, yEnd) is
      // free, it will the wall at end of the new wall.
      this.wallStartAtEnd = getWallStartAt(xEnd, yEnd, this.newWall);
      if (this.wallStartAtEnd != null) {
        this.wallEndAtEnd = null;
        // Select the wall with a free start to display a feedback to user  
        selectWall(this.wallStartAtEnd);          
      } else {
        this.wallEndAtEnd = getWallEndAt(xEnd, yEnd, this.newWall);
        if (this.wallEndAtEnd != null) {
          // Select the wall with a free end to display a feedback to user  
          selectWall(this.wallEndAtEnd);          
        } else {
          deselectAll();
        }
      }

      // Ensure point at (x,y) is visible
      ((PlanComponent)getView()).ensurePointIsVisible(x, y);
      // Update move coordinates
      this.xLastEnd = xEnd;
      this.yLastEnd = yEnd;
      this.xLastMouseMove = x;
      this.yLastMouseMove = y;
    }

    @Override
    public void pressMouse(float x, float y, int clickCount, 
                           boolean shiftDown) {
      if (clickCount == 2) {
        if (this.lastWall != null) {
          // Join last wall to the selected wall at its end
          joinNewWallEndToWall(this.lastWall, this.joinedLastWall,
              this.wallStartAtEnd, this.wallEndAtEnd);
        }
        // Change state to WallCreationState 
        setState(getWallCreationState());
      } else {
        // Create a new wall only when it will have a length > 0
        // meaning after the first mouse move
        if (this.newWall != null) {
          selectWall(this.newWall);
          // Post wall creation to undo support
          this.joinedLastWall = postAddWall(this.newWall, this.oldSelection);
          this.oldSelection = Arrays.asList(new Wall [] {this.newWall});
          this.lastWall = 
          this.wallEndAtStart = this.newWall;
          this.wallStartAtStart = null;
          this.newWall = null;
          this.xStart = this.xLastEnd; 
          this.yStart = this.yLastEnd;
        }
      }
    }

    @Override
    public void toggleMagnetism(boolean magnetismToggled) {
      // Compute active magnetism
      this.magnetismEnabled = preferences.isMagnetismEnabled()
                              ^ magnetismToggled;
      // If the new wall already exists, 
      // compute again its end as if mouse moved
      if (this.newWall != null) {
        moveMouse(this.xLastMouseMove, this.yLastMouseMove);
      }
    }

    @Override
    public void escape() {
      if (this.newWall != null) {
        plan.deleteWall(this.newWall);
      }
      // Change state to WallCreationState 
      setState(getWallCreationState());
    }
  }
}
