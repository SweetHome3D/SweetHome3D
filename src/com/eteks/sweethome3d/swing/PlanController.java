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

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.Camera;
import com.eteks.sweethome3d.model.CameraEvent;
import com.eteks.sweethome3d.model.CameraListener;
import com.eteks.sweethome3d.model.DimensionLine;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.ObserverCamera;
import com.eteks.sweethome3d.model.SelectionEvent;
import com.eteks.sweethome3d.model.SelectionListener;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;

/**
 * A MVC controller for the plan view.
 * @author Emmanuel Puybaret
 */
public class PlanController extends FurnitureController {
  public enum Property {MODE}
  
  public enum Mode {WALL_CREATION, SELECTION, DIMENSION_LINE_CREATION}
  
  private static final String SCALE_VISUAL_PROPERTY = "com.eteks.sweethome3d.SweetHome3D.PlanScale";

  private JComponent            planView;
  private Home                  home;
  private UserPreferences       preferences;
  private ContentManager        contentManager;
  private UndoableEditSupport   undoSupport;
  private ResourceBundle        resource;
  private SelectionListener     selectionListener;
  private PropertyChangeSupport propertyChangeSupport;
  // Current state
  private ControllerState       state;
  // Possibles states
  private ControllerState       selectionState;
  private ControllerState       rectangleSelectionState;
  private ControllerState       selectionMoveState;
  private ControllerState       wallCreationState;
  private ControllerState       newWallState;
  private ControllerState       wallResizeState;
  private ControllerState       pieceOfFurnitureRotationState;
  private ControllerState       pieceOfFurnitureElevationState;
  private ControllerState       pieceOfFurnitureHeightState;
  private ControllerState       pieceOfFurnitureResizeState;
  private ControllerState       cameraYawRotationState;
  private ControllerState       cameraPitchRotationState;
  private ControllerState       dimensionLineCreationState;
  private ControllerState       newDimensionLineState;
  private ControllerState       dimensionLineResizeState;
  private ControllerState       dimensionLineOffsetState;
  // Mouse cursor position at last mouse press
  private float                 xLastMousePress;
  private float                 yLastMousePress;
  private boolean               shiftDownLastMousePress;
  private boolean               duplicationActivatedLastMousePress;
  private float                 xLastMouseMove;
  private float                 yLastMouseMove;

  /**
   * Creates the controller of plan view. 
   * @param home        the home plan edited by this controller and its view
   * @param preferences the preferences of the application
   * @param undoSupport undo support to post changes on plan by this controller
   */
  public PlanController(Home home, UserPreferences preferences, 
                        ContentManager contentManager,
                        UndoableEditSupport undoSupport) {
    super(home, preferences, contentManager, undoSupport);
    this.home = home;
    this.preferences = preferences;
    this.contentManager = contentManager;
    this.undoSupport = undoSupport;
    this.resource  = ResourceBundle.getBundle(PlanController.class.getName());
    this.propertyChangeSupport = new PropertyChangeSupport(this);
    // Create view
    this.planView = new PlanComponent(home, preferences, this);
    // Initialize states
    this.selectionState = new SelectionState();
    this.selectionMoveState = new SelectionMoveState();
    this.rectangleSelectionState = new RectangleSelectionState();
    this.wallCreationState = new WallCreationState();
    this.newWallState = new NewWallState();
    this.wallResizeState = new WallResizeState();
    this.pieceOfFurnitureRotationState = new PieceOfFurnitureRotationState();
    this.pieceOfFurnitureElevationState = new PieceOfFurnitureElevationState();
    this.pieceOfFurnitureHeightState = new PieceOfFurnitureHeightState();
    this.pieceOfFurnitureResizeState = new PieceOfFurnitureResizeState();
    this.cameraYawRotationState = new CameraYawRotationState();
    this.cameraPitchRotationState = new CameraPitchRotationState();
    this.dimensionLineCreationState = new DimensionLineCreationState();
    this.newDimensionLineState = new NewDimensionLineState();
    this.dimensionLineResizeState = new DimensionLineResizeState();
    this.dimensionLineOffsetState = new DimensionLineOffsetState();
    // Set default state to selectionState
    setState(this.selectionState);
    
    addHomeListeners();
    addLanguageListener(preferences);
    
    // Restore previous scale if it exists
    Float scale = (Float)home.getVisualProperty(SCALE_VISUAL_PROPERTY);
    if (scale != null) {
      setScale(scale);
    }
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
   * Adds the property change <code>listener</code> in parameter to this controller.
   */
  public void addPropertyChangeListener(Property property, PropertyChangeListener listener) {
    this.propertyChangeSupport.addPropertyChangeListener(property.toString(), listener);
  }

  /**
   * Removes the property change <code>listener</code> in parameter from this controller.
   */
  public void removePropertyChangeListener(Property property, PropertyChangeListener listener) {
    this.propertyChangeSupport.removePropertyChangeListener(property.toString(), listener);
  }

  /**
   * Returns the active mode of this controller.
   */
  public Mode getMode() {
    return this.state.getMode();
  }

  /**
   * Sets the active mode of this controller and fires a <code>PropertyChangeEvent</code>. 
   */
  public void setMode(Mode mode) {
    Mode oldMode = this.state.getMode();
    if (mode != oldMode) {
      this.state.setMode(mode);
      this.propertyChangeSupport.firePropertyChange(Property.MODE.toString(), oldMode, mode);
    }
  }

  /**
   * Deletes the selection in home.
   */
  @Override
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
   * @param magnetismToggled if <code>true</code> then magnetism feature is toggled.
   */
  public void toggleMagnetism(boolean magnetismToggled) {
    this.state.toggleMagnetism(magnetismToggled);
  }

  /**
   * Activates duplication feature. 
   * @param duplicationActivated if <code>true</code> then duplication is active.
   */
  public void activateDuplication(boolean duplicationActivated) {
    this.state.activateDuplication(duplicationActivated);
  }

  /**
   * Processes a mouse button pressed event.
   */
  public void pressMouse(float x, float y, int clickCount, 
                         boolean shiftDown, boolean duplicationActivated) {
    // Store the last coordinates of a mouse press
    this.xLastMousePress = x;
    this.yLastMousePress = y;
    this.xLastMouseMove = x;
    this.yLastMouseMove = y;
    this.shiftDownLastMousePress = shiftDown; 
    this.duplicationActivatedLastMousePress = duplicationActivated; 
    this.state.pressMouse(x, y, clickCount, shiftDown, duplicationActivated);
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
    // Store the last coordinates of a mouse move
    this.xLastMouseMove = x;
    this.yLastMouseMove = y;
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
   * Returns the wall resize state.
   */
  protected ControllerState getWallResizeState() {
    return this.wallResizeState;
  }
  
  /**
   * Returns the piece rotation state.
   */
  protected ControllerState getPieceOfFurnitureRotationState() {
    return this.pieceOfFurnitureRotationState;
  }

  /**
   * Returns the piece elevation state.
   */
  protected ControllerState getPieceOfFurnitureElevationState() {
    return this.pieceOfFurnitureElevationState;
  }

  /**
   * Returns the piece height state.
   */
  protected ControllerState getPieceOfFurnitureHeightState() {
    return this.pieceOfFurnitureHeightState;
  }

  /**
   * Returns the piece resize state.
   */
  protected ControllerState getPieceOfFurnitureResizeState() {
    return this.pieceOfFurnitureResizeState;
  }

  /**
   * Returns the camera yaw rotation state.
   */
  public ControllerState getCameraYawRotationState() {
    return this.cameraYawRotationState;
  }

  /**
   * Returns the camera pitch rotation state.
   */
  public ControllerState getCameraPitchRotationState() {
    return this.cameraPitchRotationState;
  }

  /**
   * Returns the dimension line creation state.
   */
  public ControllerState getDimensionLineCreationState() {
    return this.dimensionLineCreationState;
  }

  /**
   * Returns the new dimension line state.
   */
  public ControllerState getNewDimensionLineState() {
    return this.newDimensionLineState;
  }

  /**
   * Returns the dimension line resize state.
   */
  private ControllerState getDimensionLineResizeState() {
    return this.dimensionLineResizeState;
  }

  /**
   * Returns the dimension line offset state.
   */
  private ControllerState getDimensionLineOffsetState() {
    return this.dimensionLineOffsetState;
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
   * Returns <code>true</code> if duplication was activated at last mouse press.
   */
  protected boolean wasDuplicationActivatedLastMousePress() {
    return this.duplicationActivatedLastMousePress;
  }

  /**
   * Returns the abscissa of mouse position at last mouse move.
   */
  protected float getXLastMouseMove() {
    return this.xLastMouseMove;
  }

  /**
   * Returns the ordinate of mouse position at last mouse move.
   */
  protected float getYLastMouseMove() {
    return this.yLastMouseMove;
  }
  
  /**
   * Controls the modification of selected walls.
   */
  public void modifySelectedWalls() {
    if (!Home.getWallsSubList(this.home.getSelectedItems()).isEmpty()) {
      new WallController(this.home, this.preferences, this.contentManager, this.undoSupport);
    }
  }
  
  /**
   * Controls the direction reverse of selected walls.
   */
  public void reverseSelectedWallsDirection() {
    List<Wall> selectedWalls = Home.getWallsSubList(this.home.getSelectedItems());
    if (!selectedWalls.isEmpty()) {
      Wall [] reversedWalls = selectedWalls.toArray(new Wall [selectedWalls.size()]);
      doReverseWallsDirection(reversedWalls);
      postReverseSelectedWallsDirection(reversedWalls, this.home.getSelectedItems());
    }
  }
  
  /**
   * Posts an undoable reverse wall operation, about <code>walls</code>.
   */
  private void postReverseSelectedWallsDirection(final Wall [] walls, 
                                                 List<Object> oldSelection) {
    final Object [] oldSelectedItems = 
        oldSelection.toArray(new Object [oldSelection.size()]);
    UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
      @Override
      public void undo() throws CannotUndoException {
        super.undo();
        doReverseWallsDirection(walls);
        selectAndShowItems(Arrays.asList(oldSelectedItems));
      }
      
      @Override
      public void redo() throws CannotRedoException {
        super.redo();
        doReverseWallsDirection(walls);
        selectAndShowItems(Arrays.asList(oldSelectedItems));
      }      

      @Override
      public String getPresentationName() {
        return resource.getString("undoReverseWallsDirectionName");
      }      
    };
    this.undoSupport.postEdit(undoableEdit);
  }

  /**
   * Reverses the <code>walls</code> direction.
   */
  private void doReverseWallsDirection(Wall [] walls) {
    for (Wall wall : walls) {
      float xStart = wall.getXStart();
      float yStart = wall.getYStart();
      float xEnd = wall.getXEnd();
      float yEnd = wall.getYEnd();
      this.home.moveWallStartPointTo(wall, xEnd, yEnd);
      this.home.moveWallEndPointTo(wall, xStart, yStart);

      Wall wallAtStart = wall.getWallAtStart();            
      boolean joinedAtEndOfWallAtStart =
        wallAtStart != null
        && wallAtStart.getWallAtEnd() == wall;
      boolean joinedAtStartOfWallAtStart =
        wallAtStart != null
        && wallAtStart.getWallAtStart() == wall;
      Wall wallAtEnd = wall.getWallAtEnd();      
      boolean joinedAtEndOfWallAtEnd =
        wallAtEnd != null
        && wallAtEnd.getWallAtEnd() == wall;
      boolean joinedAtStartOfWallAtEnd =
        wallAtEnd != null
        && wallAtEnd.getWallAtStart() == wall;
      
      this.home.setWallAtStart(wall, wallAtEnd);
      this.home.setWallAtEnd(wall, wallAtStart);
      
      if (joinedAtEndOfWallAtStart) {
        this.home.setWallAtEnd(wallAtStart, wall);
      } else if (joinedAtStartOfWallAtStart) {
        this.home.setWallAtStart(wallAtStart, wall);
      }
      
      if (joinedAtEndOfWallAtEnd) {
        this.home.setWallAtEnd(wallAtEnd, wall);
      } else if (joinedAtStartOfWallAtEnd) {
        this.home.setWallAtStart(wallAtEnd, wall);
      }
      
      Integer rightSideColor = wall.getRightSideColor();
      HomeTexture rightSideTexture = wall.getRightSideTexture();
      Integer leftSideColor = wall.getLeftSideColor();
      HomeTexture leftSideTexture = wall.getLeftSideTexture();      
      this.home.setWallLeftSideColor(wall, rightSideColor);
      this.home.setWallLeftSideTexture(wall, rightSideTexture);
      this.home.setWallRightSideColor(wall, leftSideColor);
      this.home.setWallRightSideTexture(wall, leftSideTexture);
      
      Float heightAtEnd = wall.getHeightAtEnd();
      if (heightAtEnd != null) {
        Float height = wall.getHeight();
        this.home.setWallHeight(wall, heightAtEnd);
        this.home.setWallHeightAtEnd(wall, height);
      }
    }
  }
  
  /**
   * Controls the split of the selected wall in two joined walls of equal length.
   */
  public void splitSelectedWall() {
    List<Object> selectedItems = this.home.getSelectedItems();
    List<Wall> selectedWalls = Home.getWallsSubList(selectedItems);
    if (selectedWalls.size() == 1) {
      Wall splitWall = selectedWalls.get(0);
      JoinedWall splitJoinedWall = new JoinedWall(splitWall);
      float xStart = splitWall.getXStart();
      float yStart = splitWall.getYStart();
      float xEnd = splitWall.getXEnd();
      float yEnd = splitWall.getYEnd();
      float xMiddle = (xStart + xEnd) / 2;
      float yMiddle = (yStart + yEnd) / 2;

      Wall wallAtStart = splitWall.getWallAtStart();            
      boolean joinedAtEndOfWallAtStart =
        wallAtStart != null
        && wallAtStart.getWallAtEnd() == splitWall;
      boolean joinedAtStartOfWallAtStart =
        wallAtStart != null
        && wallAtStart.getWallAtStart() == splitWall;
      Wall wallAtEnd = splitWall.getWallAtEnd();      
      boolean joinedAtEndOfWallAtEnd =
        wallAtEnd != null
        && wallAtEnd.getWallAtEnd() == splitWall;
      boolean joinedAtStartOfWallAtEnd =
        wallAtEnd != null
        && wallAtEnd.getWallAtStart() == splitWall;

      // Create new walls with copy constructor to copy their characteristics 
      Wall firstWall = new Wall(splitWall);
      this.home.addWall(firstWall);
      Wall secondWall = new Wall(splitWall);
      this.home.addWall(secondWall);
      
      // Change split walls end and start point
      this.home.moveWallEndPointTo(firstWall, xMiddle, yMiddle);
      this.home.moveWallStartPointTo(secondWall, xMiddle, yMiddle);
      if (splitWall.getHeightAtEnd() != null) {
        Float heightAtMiddle = (splitWall.getHeight() + splitWall.getHeightAtEnd()) / 2;
        this.home.setWallHeightAtEnd(firstWall, heightAtMiddle);
        this.home.setWallHeight(secondWall, heightAtMiddle);
      } 
            
      this.home.setWallAtEnd(firstWall, secondWall);
      this.home.setWallAtStart(secondWall, firstWall);
      
      this.home.setWallAtStart(firstWall, wallAtStart);
      if (joinedAtEndOfWallAtStart) {
        this.home.setWallAtEnd(wallAtStart, firstWall);
      } else if (joinedAtStartOfWallAtStart) {
        this.home.setWallAtStart(wallAtStart, firstWall);
      }
      
      this.home.setWallAtEnd(secondWall, wallAtEnd);
      if (joinedAtEndOfWallAtEnd) {
        this.home.setWallAtEnd(wallAtEnd, secondWall);
      } else if (joinedAtStartOfWallAtEnd) {
        this.home.setWallAtStart(wallAtEnd, secondWall);
      }
      
      // Delete split wall
      this.home.deleteWall(splitWall);
      selectAndShowItems(Arrays.asList(new Wall [] {firstWall}));
      
      postSplitSelectedWalls(splitJoinedWall, 
          new JoinedWall(firstWall), new JoinedWall(secondWall), selectedItems);
    }
  }
  
  /**
   * Posts an undoable split wall operation.
   */
  private void postSplitSelectedWalls(final JoinedWall splitJoinedWall, 
                                      final JoinedWall firstJoinedWall, 
                                      final JoinedWall secondJoinedWall,
                                      List<Object> oldSelection) {
    final Object [] oldSelectedItems = 
        oldSelection.toArray(new Object [oldSelection.size()]);
    UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
      @Override
      public void undo() throws CannotUndoException {
        super.undo();
        doDeleteWalls(new JoinedWall [] {firstJoinedWall, secondJoinedWall});
        doAddWalls(new JoinedWall [] {splitJoinedWall});
        selectAndShowItems(Arrays.asList(oldSelectedItems));
      }
      
      @Override
      public void redo() throws CannotRedoException {
        super.redo();
        doDeleteWalls(new JoinedWall [] {splitJoinedWall});
        doAddWalls(new JoinedWall [] {firstJoinedWall, secondJoinedWall});
        selectAndShowItems(Arrays.asList(new Wall [] {firstJoinedWall.getWall()}));
      }      

      @Override
      public String getPresentationName() {
        return resource.getString("undoSplitWallsName");
      }      
    };
    this.undoSupport.postEdit(undoableEdit);
  }

  /**
   * Returns the scale in plan view. 
   */
  public float getScale() {
    return ((PlanComponent)getView()).getScale();
  }

  /**
   * Controls the scale in plan view. 
   */
  public void setScale(float scale) {
    ((PlanComponent)getView()).setScale(scale);
    this.home.setVisualProperty(SCALE_VISUAL_PROPERTY, scale);
  } 
  
  /**
   * Selects all visible objects in home.
   */
  @Override
  public void selectAll() {
    List<Object> all = new ArrayList<Object>(this.home.getWalls());
    all.addAll(this.home.getDimensionLines());
    for (HomePieceOfFurniture piece : this.home.getFurniture()) {
      if (piece.isVisible()) {
        all.add(piece);
      }
    }
    this.home.setSelectedItems(all);
  }
  
  /**
   * Returns the horizontal ruler of the plan view. 
   */
  public JComponent getHorizontalRulerView() {
    return ((PlanComponent)getView()).getHorizontalRuler();
  }
  
  /**
   * Returns the vertical ruler of the plan view. 
   */
  public JComponent getVerticalRulerView() {
    return ((PlanComponent)getView()).getVerticalRuler();
  }
  
  private void addHomeListeners() {
    this.selectionListener = new SelectionListener() {
        public void selectionChanged(SelectionEvent selectionEvent) {
          ((PlanComponent)getView()).makeSelectionVisible();
        }
      };
    this.home.addSelectionListener(this.selectionListener);
    // Ensure observer camera is visible when its location or angles change
    this.home.addCameraListener(new CameraListener() {
        public void cameraChanged(CameraEvent ev) {
          if (ev.getCamera() == home.getObserverCamera()
              && home.getSelectedItems().contains(ev.getCamera())) {
            ((PlanComponent)getView()).makeSelectionVisible();
          }
        }
      });
  }

  /**
   * Adds a property change listener to <code>preferences</code> to update
   * resource bundle when preferred language changes.
   */
  private void addLanguageListener(UserPreferences preferences) {
    preferences.addPropertyChangeListener(UserPreferences.Property.LANGUAGE, 
        new LanguageChangeListener(this));
  }

  /**
   * Preferences property listener bound to this component with a weak reference to avoid
   * strong link between preferences and this component.  
   */
  private static class LanguageChangeListener implements PropertyChangeListener {
    private WeakReference<PlanController> planController;

    public LanguageChangeListener(PlanController helpPane) {
      this.planController = new WeakReference<PlanController>(helpPane);
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
      // If plan controller was garbage collected, remove this listener from preferences
      PlanController planController = this.planController.get();
      if (planController == null) {
        ((UserPreferences)ev.getSource()).removePropertyChangeListener(
            UserPreferences.Property.LANGUAGE, this);
      } else {
        // Update resource from current default locale
        planController.resource = ResourceBundle.getBundle(PlanController.class.getName());
      }
    }
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
    // Create a new wall
    Wall newWall = new Wall(xStart, yStart, xEnd, yEnd, 
        this.preferences.getNewWallThickness());
    this.home.addWall(newWall);
    // Ignore home default wall height, and use preferences new wall height
    this.home.setWallHeight(newWall, this.preferences.getNewWallHeight());
    if (wallStartAtStart != null) {
      this.home.setWallAtStart(newWall, wallStartAtStart);
      this.home.setWallAtStart(wallStartAtStart, newWall);
    } else if (wallEndAtStart != null) {
      this.home.setWallAtStart(newWall, wallEndAtStart);
      this.home.setWallAtEnd(wallEndAtStart, newWall);
    }        
    return newWall;
  }
  
  /**
   * Joins the end point of <code>wall</code> to the start of
   * <code>wallStartAtEnd</code> or the end of <code>wallEndAtEnd</code>.
   */
  private void joinNewWallEndToWall(Wall wall, 
                                    Wall wallStartAtEnd, Wall wallEndAtEnd) {
    if (wallStartAtEnd != null) {
      this.home.setWallAtEnd(wall, wallStartAtEnd);
      this.home.setWallAtStart(wallStartAtEnd, wall);
      // Make wall end at the exact same position as wallAtEnd start point
      this.home.moveWallEndPointTo(wall, wallStartAtEnd.getXStart(),
          wallStartAtEnd.getYStart());
    } else if (wallEndAtEnd != null) {
      this.home.setWallAtEnd(wall, wallEndAtEnd);
      this.home.setWallAtEnd(wallEndAtEnd, wall);
      // Make wall end at the exact same position as wallAtEnd end point
      this.home.moveWallEndPointTo(wall, wallEndAtEnd.getXEnd(),
          wallEndAtEnd.getYEnd());
    }
  }
  
  /**
   * Returns the wall at (<code>x</code>, <code>y</code>) point,  
   * which has a start point not joined to any wall. 
   */
  private Wall getWallStartAt(float x, float y, Wall ignoredWall) {
    float margin = 2 / ((PlanComponent)getView()).getScale();
    for (Wall wall : this.home.getWalls()) {
      if (wall != ignoredWall
          && wall.getWallAtStart() == null
          && wall.containsWallStartAt(x, y, margin)) 
        return wall;
    }
    return null;
  }

  /**
   * Returns the wall at (<code>x</code>, <code>y</code>) point,  
   * which has a end point not joined to any wall. 
   */
  private Wall getWallEndAt(float x, float y, Wall ignoredWall) {
    float margin = 2 / ((PlanComponent)getView()).getScale();
    for (Wall wall : this.home.getWalls()) {
      if (wall != ignoredWall
          && wall.getWallAtEnd() == null
          && wall.containsWallEndAt(x, y, margin)) 
        return wall;
    }
    return null;
  }

  /**
   * Returns the selected wall with a start point 
   * at (<code>x</code>, <code>y</code>).
   */
  private Wall getResizedWallStartAt(float x, float y) {
    List<Object> selectedItems = this.home.getSelectedItems();
    if (selectedItems.size() == 1
        && selectedItems.get(0) instanceof Wall) {
      Wall wall = (Wall)selectedItems.get(0);
      float margin = 3 / ((PlanComponent)getView()).getScale();
      if (wall.containsWallStartAt(x, y, margin)) {
        return wall;
      }
    } 
    return null;
  }
  
  /**
   * Returns the selected wall with an end point 
   * at (<code>x</code>, <code>y</code>).
   */
  private Wall getResizedWallEndAt(float x, float y) {
    List<Object> selectedItems = this.home.getSelectedItems();
    if (selectedItems.size() == 1
        && selectedItems.get(0) instanceof Wall) {
      Wall wall = (Wall)selectedItems.get(0);
      float margin = 3 / ((PlanComponent)getView()).getScale();
      if (wall.containsWallEndAt(x, y, margin)) {
        return wall;
      }
    } 
    return null;
  }
  
  /**
   * Returns the selected dimension line with an end extension line
   * at (<code>x</code>, <code>y</code>).
   */
  private DimensionLine getResizedDimensionLineStartAt(float x, float y) {
    List<Object> selectedItems = this.home.getSelectedItems();
    if (selectedItems.size() == 1
        && selectedItems.get(0) instanceof DimensionLine) {
      DimensionLine dimensionLine = (DimensionLine)selectedItems.get(0);
      float margin = 3 / ((PlanComponent)getView()).getScale();
      if (dimensionLine.containsStartExtensionLinetAt(x, y, margin)) {
        return dimensionLine;
      }
    } 
    return null;
  }
  
  /**
   * Returns the selected dimension line with an end extension line
   * at (<code>x</code>, <code>y</code>).
   */
  private DimensionLine getResizedDimensionLineEndAt(float x, float y) {
    List<Object> selectedItems = this.home.getSelectedItems();
    if (selectedItems.size() == 1
        && selectedItems.get(0) instanceof DimensionLine) {
      DimensionLine dimensionLine = (DimensionLine)selectedItems.get(0);
      float margin = 3 / ((PlanComponent)getView()).getScale();
      if (dimensionLine.containsEndExtensionLineAt(x, y, margin)) {
        return dimensionLine;
      }
    } 
    return null;
  }
  
  /**
   * Returns the selected dimension line with a point
   * at (<code>x</code>, <code>y</code>) at its middle.
   */
  private DimensionLine getOffsetDimensionLineAt(float x, float y) {
    List<Object> selectedItems = this.home.getSelectedItems();
    if (selectedItems.size() == 1
        && selectedItems.get(0) instanceof DimensionLine) {
      DimensionLine dimensionLine = (DimensionLine)selectedItems.get(0);
      float margin = 3 / ((PlanComponent)getView()).getScale();
      if (dimensionLine.isMiddlePointAt(x, y, margin)) {
        return dimensionLine;
      }
    } 
    return null;
  }
  
  /**
   * Returns the item at (<code>x</code>, <code>y</code>) point.
   */
  private Object getItemAt(float x, float y) {
    float margin = 3 / ((PlanComponent)getView()).getScale();
    ObserverCamera camera = this.home.getObserverCamera();
    if (camera != null
        && camera == this.home.getCamera()
        && camera.containsPoint(x, y, margin)) {
      return camera;
    }
    
    for (DimensionLine dimensionLine : this.home.getDimensionLines()) {
      if (dimensionLine.containsPoint(x, y, margin)) {
        return dimensionLine;
      }
    }    
    
    List<HomePieceOfFurniture> furniture = this.home.getFurniture();
    // Search in home furniture in reverse order to give priority to last drawn piece
    // at highest elevation in case it covers an other piece
    HomePieceOfFurniture foundPiece = null;
    for (int i = furniture.size() - 1; i >= 0; i--) {
      HomePieceOfFurniture piece = furniture.get(i);
      if (piece.isVisible() 
          && piece.containsPoint(x, y, margin)
          && (foundPiece == null
              || piece.getElevation() > foundPiece.getElevation())) {
        foundPiece = piece;
      }
    }
    if (foundPiece != null) {
      return foundPiece;
    } else {
      for (Wall wall : this.home.getWalls()) {
        if (wall.containsPoint(x, y, margin)) {
          return wall;
        }
      }    
      return null;
    }
  }

  /**
   * Returns the items that intersects with the rectangle of (<code>x0</code>,
   * <code>y0</code>), (<code>x1</code>, <code>y1</code>) opposite
   * corners.
   */
  private List<Object> getRectangleItems(float x0, float y0, float x1, float y1) {
    List<Object> items = new ArrayList<Object>();
    ObserverCamera camera = this.home.getObserverCamera();
    if (camera != null && camera.intersectsRectangle(x0, y0, x1, y1)) {
      items.add(camera);
    }
    for (DimensionLine dimensionLine : home.getDimensionLines()) {
      if (dimensionLine.intersectsRectangle(x0, y0, x1, y1)) {
        items.add(dimensionLine);
      }
    }
    for (HomePieceOfFurniture piece : this.home.getFurniture()) {
      if (piece.isVisible() && piece.intersectsRectangle(x0, y0, x1, y1)) {
        items.add(piece);
      }
    }
    for (Wall wall : home.getWalls()) {
      if (wall.intersectsRectangle(x0, y0, x1, y1)) {
        items.add(wall);
      }
    }
    return items;
  }
  
  /**
   * Returns the selected piece of furniture with a vertex 
   * at (<code>x</code>, <code>y</code>) that can be used to rotate the piece.
   */
  private HomePieceOfFurniture getRotatedPieceOfFurnitureAt(float x, float y) {
    List<Object> selectedItems = this.home.getSelectedItems();
    if (selectedItems.size() == 1
        && selectedItems.get(0) instanceof HomePieceOfFurniture) {
      HomePieceOfFurniture piece = (HomePieceOfFurniture)selectedItems.get(0);
      float margin = 3 / ((PlanComponent)getView()).getScale();
      if (piece.isTopLeftVertexAt(x, y, margin)) {
        return piece;
      }
    } 
    return null;
  }
  
  /**
   * Returns the selected piece of furniture with a vertex 
   * at (<code>x</code>, <code>y</code>) that can be used to elevate the piece.
   */
  private HomePieceOfFurniture getElevatedPieceOfFurnitureAt(float x, float y) {
    List<Object> selectedItems = this.home.getSelectedItems();
    if (selectedItems.size() == 1
        && selectedItems.get(0) instanceof HomePieceOfFurniture) {
      HomePieceOfFurniture piece = (HomePieceOfFurniture)selectedItems.get(0);
      float margin = 3 / ((PlanComponent)getView()).getScale();
      if (piece.isTopRightVertexAt(x, y, margin)) {
        return piece;
      }
    } 
    return null;
  }
  
  /**
   * Returns the selected piece of furniture with a vertex 
   * at (<code>x</code>, <code>y</code>) that can be used to resize the height 
   * of the piece.
   */
  private HomePieceOfFurniture getHeightResizedPieceOfFurnitureAt(float x, float y) {
    List<Object> selectedItems = this.home.getSelectedItems();
    if (selectedItems.size() == 1
        && selectedItems.get(0) instanceof HomePieceOfFurniture) {
      HomePieceOfFurniture piece = (HomePieceOfFurniture)selectedItems.get(0);
      float margin = 3 / ((PlanComponent)getView()).getScale();
      if (piece.isResizable() 
          && piece.isBottomLeftVertexAt(x, y, margin)) {
        return piece;
      }
    } 
    return null;
  }
  
  /**
   * Returns the selected piece of furniture with a vertex 
   * at (<code>x</code>, <code>y</code>) that can be used to resize 
   * the width and the depth of the piece.
   */
  private HomePieceOfFurniture getWidthAndDepthResizedPieceOfFurnitureAt(float x, float y) {
    List<Object> selectedItems = this.home.getSelectedItems();
    if (selectedItems.size() == 1
        && selectedItems.get(0) instanceof HomePieceOfFurniture) {
      HomePieceOfFurniture piece = (HomePieceOfFurniture)selectedItems.get(0);
      float margin = 3 / ((PlanComponent)getView()).getScale();
      if (piece.isResizable() 
          && piece.isBottomRightVertexAt(x, y, margin)) {
        return piece;
      }
    } 
    return null;
  }
  
  /**
   * Returns the selected camera with a point at (<code>x</code>, <code>y</code>) 
   * that can be used to change the camera yaw angle.
   */
  private Camera getYawRotatedCameraAt(float x, float y) {
    List<Object> selectedItems = this.home.getSelectedItems();
    if (selectedItems.size() == 1
        && selectedItems.get(0) instanceof Camera) {
      ObserverCamera camera = (ObserverCamera)selectedItems.get(0);
      float margin = 3 / ((PlanComponent)getView()).getScale();
      float [][] cameraPoints = camera.getPoints();
      // Check if (x,y) matches the point between the first and the last point 
      // of the rectangle surrounding camera
      float xMiddleFirstAndLastPoint = (cameraPoints [0][0] + cameraPoints [3][0]) / 2; 
      float yMiddleFirstAndLastPoint = (cameraPoints [0][1] + cameraPoints [3][1]) / 2;      
      if (Math.abs(x - xMiddleFirstAndLastPoint) <= margin 
          && Math.abs(y - yMiddleFirstAndLastPoint) <= margin) {
        return camera;
      }
    } 
    return null;
  }

  /**
   * Returns the selected camera with a point at (<code>x</code>, <code>y</code>) 
   * that can be used to change the camera pitch angle.
   */
  private Camera getPitchRotatedCameraAt(float x, float y) {
    List<Object> selectedItems = this.home.getSelectedItems();
    if (selectedItems.size() == 1
        && selectedItems.get(0) instanceof Camera) {
      ObserverCamera camera = (ObserverCamera)selectedItems.get(0);
      float margin = 3 / ((PlanComponent)getView()).getScale();
      float [][] cameraPoints = camera.getPoints();
      // Check if (x,y) matches the point between the second and the third point 
      // of the rectangle surrounding camera
      float xMiddleFirstAndLastPoint = (cameraPoints [1][0] + cameraPoints [2][0]) / 2; 
      float yMiddleFirstAndLastPoint = (cameraPoints [1][1] + cameraPoints [2][1]) / 2;      
      if (Math.abs(x - xMiddleFirstAndLastPoint) <= margin 
          && Math.abs(y - yMiddleFirstAndLastPoint) <= margin) {
        return camera;
      }
    } 
    return null;
  }

  /**
   * Deletes <code>items</code> in plan and record it as an undoable operation.
   */
  public void deleteItems(List<? extends Object> items) {
    if (!items.isEmpty()) {
      // Start a compound edit that deletes walls, furniture and dimension lines from home
      this.undoSupport.beginUpdate();
      
      final List<Object> deletedItems = new ArrayList<Object>(items);      
      // Add a undoable edit that will select the undeleted items at undo
      this.undoSupport.postEdit(new AbstractUndoableEdit() {      
          @Override
          public void undo() throws CannotRedoException {
            super.undo();
            selectAndShowItems(deletedItems);
          }
        });

      deleteFurniture(Home.getFurnitureSubList(items));      

      List<Object> deletedWallsAndDimensionLines = 
          new ArrayList<Object>(Home.getWallsSubList(items));
      deletedWallsAndDimensionLines.addAll(Home.getDimensionLinesSubList(items));
      // First post to undo support that walls and dimension lines are deleted, 
      // otherwise data about joined walls can't be stored       
      postDeleteWallsAndDimensionLines(deletedWallsAndDimensionLines);
      // Then delete walls and dimension lines from plan
      doDeleteItems(deletedWallsAndDimensionLines);

      // End compound edit
      this.undoSupport.endUpdate();
    }          
  }

  /**
   * Posts an undoable delete items operation about <code>deletedItems</code>.
   */
  private void postDeleteWallsAndDimensionLines(final List<? extends Object> deletedItems) {
    // Manage walls
    List<Wall> deletedWalls = new ArrayList<Wall>();
    for (Object item : deletedItems) {
      if (item instanceof Wall) {
        deletedWalls.add((Wall)item);
      }
    }
    // Get joined walls data for undo operation
    final JoinedWall [] joinedDeletedWalls = 
      JoinedWall.getJoinedWalls(deletedWalls);

    // Manage dimension lines
    List<DimensionLine> deletedDimensionLines = new ArrayList<DimensionLine>();
    for (Object item : deletedItems) {
      if (item instanceof DimensionLine) {
        deletedDimensionLines.add((DimensionLine)item);
      }
    }
    final DimensionLine [] dimensionLines = deletedDimensionLines.toArray(
        new DimensionLine [deletedDimensionLines.size()]);
    
    UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
      @Override
      public void undo() throws CannotUndoException {
        super.undo();
        doAddWalls(joinedDeletedWalls);       
        doAddDimensionLines(dimensionLines);
        selectAndShowItems(deletedItems);
      }
      
      @Override
      public void redo() throws CannotRedoException {
        super.redo();
        selectItems(deletedItems);
        doDeleteWalls(joinedDeletedWalls);       
        doDeleteDimensionLines(dimensionLines);
      }      
      
      @Override
      public String getPresentationName() {
        return resource.getString("undoDeleteSelectionName");
      }      
    };
    this.undoSupport.postEdit(undoableEdit);
  }

  /**
   * Deletes <code>items</code> from home.
   */
  private void doDeleteItems(List<Object> items) {
    for (Object item : items) {
      if (item instanceof Wall) {
        home.deleteWall((Wall)item);
      } else if (item instanceof DimensionLine) {
        home.deleteDimensionLine((DimensionLine)item);
      } else if (item instanceof HomePieceOfFurniture) {
        home.deletePieceOfFurniture((HomePieceOfFurniture)item);
      }
    }
  }
  
  /**
   * Moves and shows selected items in plan component of (<code>dx</code>,
   * <code>dy</code>) units and record it as undoable operation.
   */
  private void moveAndShowSelectedItems(float dx, float dy) {
    List<Object> selectedItems = this.home.getSelectedItems();
    if (!selectedItems.isEmpty()) {
      moveItems(selectedItems, dx, dy);
      ((PlanComponent)getView()).makeSelectionVisible();
      postItemsMove(selectedItems, dx, dy);
    }
  }

  /**
   * Moves <code>items</code> of (<code>dx</code>, <code>dy</code>) units.
   */
  public void moveItems(List<? extends Object> items, float dx, float dy) {
    for (Object item : items) {
      if (item instanceof Wall) {
        Wall wall = (Wall)item;
        moveWallStartPoint(wall, 
            wall.getXStart() + dx, wall.getYStart() + dy,
            !items.contains(wall.getWallAtStart()));
        moveWallEndPoint(wall, 
            wall.getXEnd() + dx, wall.getYEnd() + dy,
            !items.contains(wall.getWallAtEnd()));
      } else if (item instanceof HomePieceOfFurniture) {
        HomePieceOfFurniture piece = (HomePieceOfFurniture)item;
        this.home.setPieceOfFurnitureLocation(
            piece, piece.getX() + dx, piece.getY() + dy);
      } else if (item instanceof Camera) {
        Camera camera = (Camera)item;
        this.home.setCameraLocation(camera, camera.getX() + dx, 
            camera.getY() + dy, camera.getZ());
      } else if (item instanceof DimensionLine) {
        DimensionLine dimensionLine = (DimensionLine)item;
        this.home.moveDimensionLineStartPointTo(dimensionLine, 
            dimensionLine.getXStart() + dx, dimensionLine.getYStart() + dy);
        this.home.moveDimensionLineEndPointTo(dimensionLine, 
            dimensionLine.getXEnd() + dx, dimensionLine.getYEnd() + dy);
      } 
    }
  }
  
  /**
   * Moves <code>wall</code> start point to (<code>xStart</code>, <code>yStart</code>)
   * and the wall point joined to its start point if <code>moveWallAtStart</code> is true.
   */
  private void moveWallStartPoint(Wall wall, float xStart, float yStart,
                                  boolean moveWallAtStart) {
    this.home.moveWallStartPointTo(wall, xStart, yStart);
    Wall wallAtStart = wall.getWallAtStart();
    // If wall is joined to a wall at its start 
    // and this wall doesn't belong to the list of moved walls
    if (wallAtStart != null && moveWallAtStart) {
      // Move the wall start point or end point
      if (wallAtStart.getWallAtStart() == wall) {
        this.home.moveWallStartPointTo(wallAtStart, 
            xStart, yStart);
      } else if (wallAtStart.getWallAtEnd() == wall) {
        this.home.moveWallEndPointTo(wallAtStart, 
            xStart, yStart);
      }
    }
  }
  
  /**
   * Moves <code>wall</code> end point to (<code>xEnd</code>, <code>yEnd</code>)
   * and the wall point joined to its end if <code>moveWallAtEnd</code> is true.
   */
  private void moveWallEndPoint(Wall wall, float xEnd, float yEnd,
                                boolean moveWallAtEnd) {
    this.home.moveWallEndPointTo(wall, xEnd, yEnd);
    Wall wallAtEnd = wall.getWallAtEnd();
    // If wall is joined to a wall at its end  
    // and this wall doesn't belong to the list of moved walls
    if (wallAtEnd != null && moveWallAtEnd) {
      // Move the wall start point or end point
      if (wallAtEnd.getWallAtStart() == wall) {
        this.home.moveWallStartPointTo(wallAtEnd, xEnd, yEnd);
      } else if (wallAtEnd.getWallAtEnd() == wall) {
        this.home.moveWallEndPointTo(wallAtEnd, xEnd, yEnd);
      }
    }
  }
  
  /**
   * Moves <code>wall</code> start point to (<code>x</code>, <code>y</code>)
   * if <code>startPoint</code> is true or <code>wall</code> end point 
   * to (<code>x</code>, <code>y</code>) if <code>startPoint</code> is false.
   */
  private void moveWallPoint(Wall wall, float x, float y, boolean startPoint) {
    if (startPoint) {
      moveWallStartPoint(wall, x, y, true);
    } else {
      moveWallEndPoint(wall, x, y, true);
    }    
  }
  
  /**
   * Moves <code>dimensionLine</code> start point to (<code>x</code>, <code>y</code>)
   * if <code>startPoint</code> is true or <code>dimensionLine</code> end point 
   * to (<code>x</code>, <code>y</code>) if <code>startPoint</code> is false.
   */
  private void moveDimensionLinePoint(DimensionLine dimensionLine, float x, float y, boolean startPoint) {
    if (startPoint) {
      this.home.moveDimensionLineStartPointTo(dimensionLine, x, y);
    } else {
      this.home.moveDimensionLineEndPointTo(dimensionLine, x, y);
    }    
  }
  
  /**
   * Selects <code>items</code> and make them visible at screen.
   */
  private void selectAndShowItems(List<? extends Object> items) {
    selectItems(items);
    ((PlanComponent)getView()).makeSelectionVisible();
  }
  
  /**
   * Selects <code>items</code>.
   */
  private void selectItems(List<? extends Object> items) {
    // Remove selectionListener when selection is done from this controller
    // to control when selection should be made visible
    this.home.removeSelectionListener(this.selectionListener);
    this.home.setSelectedItems(items);
    this.home.addSelectionListener(this.selectionListener);
  }
  
  /**
   * Selects only a given <code>item</code>.
   */
  private void selectItem(Object item) {
    selectItems(Arrays.asList(new Object [] {item}));
  }

  /**
   * Deselects all walls in plan. 
   */
  private void deselectAll() {
    selectItems(Collections.emptyList());
  }

  /**
   * Add <code>newWalls</code> to home and post an undoable new wall operation.
   */
  public void addWalls(List<Wall> newWalls) {
    for (Wall wall : newWalls) {
      this.home.addWall(wall);
    }
    postAddWalls(newWalls, this.home.getSelectedItems());
  }
  
  /**
   * Posts an undoable new wall operation, about <code>newWalls</code>.
   */
  private void postAddWalls(List<Wall> newWalls, List<Object> oldSelection) {
    if (newWalls.size() > 0) {
      // Retrieve data about joined walls to newWalls
      final JoinedWall [] joinedNewWalls = new JoinedWall [newWalls.size()];
      for (int i = 0; i < joinedNewWalls.length; i++) {
         joinedNewWalls [i] = new JoinedWall(newWalls.get(i));
      }
      final Object [] oldSelectedItems = 
        oldSelection.toArray(new Object [oldSelection.size()]);
      UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
        @Override
        public void undo() throws CannotUndoException {
          super.undo();
          doDeleteWalls(joinedNewWalls);
          selectAndShowItems(Arrays.asList(oldSelectedItems));
        }
        
        @Override
        public void redo() throws CannotRedoException {
          super.redo();
          doAddWalls(joinedNewWalls);       
          selectAndShowItems(JoinedWall.getWalls(joinedNewWalls));
        }      
  
        @Override
        public String getPresentationName() {
          return resource.getString("undoAddWallsName");
        }      
      };
      this.undoSupport.postEdit(undoableEdit);
    }
  }

  /**
   * Adds the walls in <code>joinedNewWalls</code> to plan component, joins
   * them to other walls if necessary.
   */
  private void doAddWalls(JoinedWall [] joinedNewWalls) {
    // First add all walls to home
    for (JoinedWall joinedNewWall : joinedNewWalls) {
      this.home.addWall(joinedNewWall.getWall());
    }
    // Then join them to each other if necessary
    for (JoinedWall joinedNewWall : joinedNewWalls) {
      Wall wall = joinedNewWall.getWall();
      Wall wallAtStart = joinedNewWall.getWallAtStart();
      if (wallAtStart != null) {
        this.home.setWallAtStart(wall, wallAtStart);
        if (joinedNewWall.isJoinedAtEndOfWallAtStart()) {
          this.home.setWallAtEnd(wallAtStart, wall);
        } else if (joinedNewWall.isJoinedAtStartOfWallAtStart()) {
          this.home.setWallAtStart(wallAtStart, wall);
        }
      }
      Wall wallAtEnd = joinedNewWall.getWallAtEnd();
      if (wallAtEnd != null) {
        this.home.setWallAtEnd(wall, wallAtEnd);
        if (joinedNewWall.isJoinedAtStartOfWallAtEnd()) {
          this.home.setWallAtStart(wallAtEnd, wall);
        } else if (joinedNewWall.isJoinedAtEndOfWallAtEnd()) {
          this.home.setWallAtEnd(wallAtEnd, wall);
        }
      }
    }      
  }
  
  /**
   * Deletes walls referenced in <code>joinedDeletedWalls</code>.
   */
  private void doDeleteWalls(JoinedWall [] joinedDeletedWalls) {
    for (JoinedWall joinedWall : joinedDeletedWalls) {
      this.home.deleteWall(joinedWall.getWall());
    }
  }

  /**
   * Add <code>newDimensionLines</code> to home and post an undoable new dimension line operation.
   */
  public void addDimensionLines(List<DimensionLine> newDimensionLines) {
    for (DimensionLine dimensionLine : newDimensionLines) {
      this.home.addDimensionLine(dimensionLine);
    }
    postAddDimensionLines(newDimensionLines, this.home.getSelectedItems());
  }
  
  /**
   * Posts an undoable new dimension line operation, about <code>newDimensionLines</code>.
   */
  private void postAddDimensionLines(List<DimensionLine> newDimensionLines, List<Object> oldSelection) {
    if (newDimensionLines.size() > 0) {
      final DimensionLine [] dimensionLines = newDimensionLines.toArray(
          new DimensionLine [newDimensionLines.size()]);
      final Object [] oldSelectedItems = 
          oldSelection.toArray(new Object [oldSelection.size()]);
      UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
        @Override
        public void undo() throws CannotUndoException {
          super.undo();
          doDeleteDimensionLines(dimensionLines);
          selectAndShowItems(Arrays.asList(oldSelectedItems));
        }
        
        @Override
        public void redo() throws CannotRedoException {
          super.redo();
          doAddDimensionLines(dimensionLines);       
          selectAndShowItems(Arrays.asList(dimensionLines));
        }      
  
        @Override
        public String getPresentationName() {
          return resource.getString("undoAddDimensionLinesName");
        }      
      };
      this.undoSupport.postEdit(undoableEdit);
    }
  }

  /**
   * Adds the dimension lines in <code>dimensionLines</code> to plan component.
   */
  private void doAddDimensionLines(DimensionLine [] dimensionLines) {
    for (DimensionLine dimensionLine : dimensionLines) {
      this.home.addDimensionLine(dimensionLine);
    }
  }
  
  /**
   * Deletes dimension lines in <code>dimensionLines</code>.
   */
  private void doDeleteDimensionLines(DimensionLine [] dimensionLines) {
    for (DimensionLine dimensionLine : dimensionLines) {
      this.home.deleteDimensionLine(dimensionLine);
    }
  }

  /**
   * Posts an undoable operation of a (<code>dx</code>, <code>dy</code>) move 
   * of <code>movedItems</code>.
   */
  private void postItemsMove(List<? extends Object> movedItems, 
                             final float dx, final float dy) {
    if (dx != 0 || dy != 0) {
      // Store the moved walls in an array
      final Object [] itemsArray = 
        movedItems.toArray(new Object [movedItems.size()]);
      UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
        @Override
        public void undo() throws CannotUndoException {
          super.undo();
          doMoveAndShowItems(itemsArray, -dx, -dy);       
        }
        
        @Override
        public void redo() throws CannotRedoException {
          super.redo();
          doMoveAndShowItems(itemsArray, dx, dy);   
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
   * Moves <code>movedItems</code> of (<code>dx</code>, <code>dy</code>) pixels, 
   * selects them and make them visible.
   */
  private void doMoveAndShowItems(Object [] movedItems, 
                                  float dx, float dy) {
    List<Object> itemsList = Arrays.asList(movedItems);
    moveItems(itemsList, dx, dy);   
    selectAndShowItems(itemsList);
  }

  /**
   * Posts an undoable operation about duplication <code>items</code>.
   */
  public void postItemsDuplication(final List<Object> items,
                                   final List<Object> oldSelectedItems) {
    // Delete furniture and add it again in a compound edit
    List<HomePieceOfFurniture> furniture = Home.getFurnitureSubList(items);
    deleteFurniture(furniture);
    
    // Post duplicated items in a compound edit  
    this.undoSupport.beginUpdate();
    // Add a undoable edit that will select previous items at undo
    this.undoSupport.postEdit(new AbstractUndoableEdit() {      
        @Override
        public void undo() throws CannotRedoException {
          super.undo();
          selectAndShowItems(oldSelectedItems);
        }
      });

    addFurniture(furniture);
    postAddWalls(Home.getWallsSubList(items), Collections.emptyList());
    postAddDimensionLines(Home.getDimensionLinesSubList(items), Collections.emptyList());

    // Add a undoable edit that will select all the items at redo
    this.undoSupport.postEdit(new AbstractUndoableEdit() {      
        @Override
        public void redo() throws CannotRedoException {
          super.redo();
          selectAndShowItems(items);
        }

        @Override
        public String getPresentationName() {
          return resource.getString("undoDuplicateSelectionName");
        }      
      });
   
    // End compound edit
    this.undoSupport.endUpdate();
    
    selectItems(items);
  }

  /**
   * Posts an undoable operation about <code>wall</code> resizing.
   */
  private void postWallResize(final Wall wall, final float oldX, final float oldY, 
                              final boolean startPoint) {
    final float newX;
    final float newY;
    if (startPoint) {
      newX = wall.getXStart();
      newY = wall.getYStart();
    } else {
      newX = wall.getXEnd();
      newY = wall.getYEnd();
    }
    if (newX != oldX || newY != oldY) {
      UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
        @Override
        public void undo() throws CannotUndoException {
          super.undo();
          moveWallPoint(wall, oldX, oldY, startPoint);
          selectAndShowItems(Arrays.asList(new Wall [] {wall}));
        }
        
        @Override
        public void redo() throws CannotRedoException {
          super.redo();
          moveWallPoint(wall, newX, newY, startPoint);
          selectAndShowItems(Arrays.asList(new Wall [] {wall}));
        }      
  
        @Override
        public String getPresentationName() {
          return resource.getString("undoWallResizeName");
        }      
      };
      this.undoSupport.postEdit(undoableEdit);
    }
  }
  
  /**
   * Post to undo support an angle change on <code>piece</code>. 
   */
  private void postPieceOfFurnitureRotation(final HomePieceOfFurniture piece, final float oldAngle) {
    final float newAngle = piece.getAngle();
    if (newAngle != oldAngle) {
      UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
        @Override
        public void undo() throws CannotUndoException {
          super.undo();
          home.setPieceOfFurnitureAngle(piece, oldAngle);
          selectAndShowItems(Arrays.asList(new HomePieceOfFurniture [] {piece}));
        }
        
        @Override
        public void redo() throws CannotRedoException {
          super.redo();
          home.setPieceOfFurnitureAngle(piece, newAngle);
          selectAndShowItems(Arrays.asList(new HomePieceOfFurniture [] {piece}));
        }      
  
        @Override
        public String getPresentationName() {
          return resource.getString("undoPieceOfFurnitureRotationName");
        }      
      };
      this.undoSupport.postEdit(undoableEdit);
    }
  }

  /**
   * Post to undo support an elevation change on <code>piece</code>. 
   */
  private void postPieceOfFurnitureElevation(final HomePieceOfFurniture piece, final float oldElevation) {
    final float newElevation = piece.getElevation();
    if (newElevation != oldElevation) {
      UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
        @Override
        public void undo() throws CannotUndoException {
          super.undo();
          home.setPieceOfFurnitureElevation(piece, oldElevation);
          selectAndShowItems(Arrays.asList(new HomePieceOfFurniture [] {piece}));
        }
        
        @Override
        public void redo() throws CannotRedoException {
          super.redo();
          home.setPieceOfFurnitureElevation(piece, newElevation);
          selectAndShowItems(Arrays.asList(new HomePieceOfFurniture [] {piece}));
        }      
  
        @Override
        public String getPresentationName() {          
          return resource.getString(oldElevation < newElevation 
              ? "undoPieceOfFurnitureRaiseName"
              : "undoPieceOfFurnitureLowerName");
        }      
      };
      this.undoSupport.postEdit(undoableEdit);
    }
  }

  /**
   * Post to undo support a height change on <code>piece</code>. 
   */
  private void postPieceOfFurnitureHeightResize(final HomePieceOfFurniture piece, final float oldHeight) {
    final float newHeight = piece.getHeight();
    if (newHeight != oldHeight) {
      UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
        @Override
        public void undo() throws CannotUndoException {
          super.undo();
          home.setPieceOfFurnitureSize(piece, piece.getWidth(), piece.getDepth(), oldHeight);
          selectAndShowItems(Arrays.asList(new HomePieceOfFurniture [] {piece}));
        }
        
        @Override
        public void redo() throws CannotRedoException {
          super.redo();
          home.setPieceOfFurnitureSize(piece, piece.getWidth(), piece.getDepth(), newHeight);
          selectAndShowItems(Arrays.asList(new HomePieceOfFurniture [] {piece}));
        }      
  
        @Override
        public String getPresentationName() {
          return resource.getString("undoPieceOfFurnitureHeightResizeName");
        }      
      };
      this.undoSupport.postEdit(undoableEdit);
    }
  }

  /**
   * Post to undo support a width and depth change on <code>piece</code>. 
   */
  private void postPieceOfFurnitureWidthAndDepthResize(final HomePieceOfFurniture piece, 
                                          final float oldX, final float oldY,
                                          final float oldWidth, final float oldDepth) {
    final float newX = piece.getX();
    final float newY = piece.getY();
    final float newWidth = piece.getWidth();
    final float newDepth = piece.getDepth();
    if (newWidth != oldWidth
        || newDepth != oldDepth) {
      UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
        @Override
        public void undo() throws CannotUndoException {
          super.undo();
          home.setPieceOfFurnitureLocation(piece, oldX, oldY);
          home.setPieceOfFurnitureSize(piece, oldWidth, oldDepth, piece.getHeight());
          selectAndShowItems(Arrays.asList(new HomePieceOfFurniture [] {piece}));
        }
        
        @Override
        public void redo() throws CannotRedoException {
          super.redo();
          home.setPieceOfFurnitureLocation(piece, newX, newY);
          home.setPieceOfFurnitureSize(piece, newWidth, newDepth, piece.getHeight());
          selectAndShowItems(Arrays.asList(new HomePieceOfFurniture [] {piece}));
        }      
  
        @Override
        public String getPresentationName() {
          return resource.getString("undoPieceOfFurnitureWidthAndDepthResizeName");
        }      
      };
      this.undoSupport.postEdit(undoableEdit);
    }
  }

  /**
   * Posts an undoable operation about <code>dimensionLine</code> resizing.
   */
  private void postDimensionLineResize(final DimensionLine dimensionLine, final float oldX, final float oldY, 
                                       final boolean startPoint) {
    final float newX;
    final float newY;
    if (startPoint) {
      newX = dimensionLine.getXStart();
      newY = dimensionLine.getYStart();
    } else {
      newX = dimensionLine.getXEnd();
      newY = dimensionLine.getYEnd();
    }
    if (newX != oldX || newY != oldY) {
      UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
        @Override
        public void undo() throws CannotUndoException {
          super.undo();
          moveDimensionLinePoint(dimensionLine, oldX, oldY, startPoint);
          selectAndShowItems(Arrays.asList(new DimensionLine [] {dimensionLine}));
        }
        
        @Override
        public void redo() throws CannotRedoException {
          super.redo();
          moveDimensionLinePoint(dimensionLine, newX, newY, startPoint);
          selectAndShowItems(Arrays.asList(new DimensionLine [] {dimensionLine}));
        }      
  
        @Override
        public String getPresentationName() {
          return resource.getString("undoDimensionLineResizeName");
        }      
      };
      this.undoSupport.postEdit(undoableEdit);
    }
  }
  
  /**
   * Posts an undoable operation about <code>dimensionLine</code> offset change.
   */
  public void postDimensionLineOffset(final DimensionLine dimensionLine, final float oldOffset) {
    final float newOffset = dimensionLine.getOffset();
    if (newOffset != oldOffset) {
      UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
        @Override
        public void undo() throws CannotUndoException {
          super.undo();
          home.setDimensionLineOffset(dimensionLine, oldOffset);
          selectAndShowItems(Arrays.asList(new DimensionLine [] {dimensionLine}));
        }
        
        @Override
        public void redo() throws CannotRedoException {
          super.redo();
          home.setDimensionLineOffset(dimensionLine, newOffset);
          selectAndShowItems(Arrays.asList(new DimensionLine [] {dimensionLine}));
        }      
  
        @Override
        public String getPresentationName() {
          return resource.getString("undoDimensionLineOffsetName");
        }      
      };
      this.undoSupport.postEdit(undoableEdit);
    }
  }

  /**
   * Stores the walls at start and at end of a given wall. This data are usefull
   * to add a collection of walls after an undo/redo delete operation.
   */
  private static final class JoinedWall {
    private final Wall wall;
    private final Wall wallAtStart;
    private final Wall wallAtEnd;
    private final boolean joinedAtStartOfWallAtStart;
    private final boolean joinedAtEndOfWallAtStart; 
    private final boolean joinedAtStartOfWallAtEnd;
    private final boolean joinedAtEndOfWallAtEnd;
    
    public JoinedWall(Wall wall) {
      this.wall = wall;
      this.wallAtStart = wall.getWallAtStart();
      this.joinedAtEndOfWallAtStart =
          this.wallAtStart != null
          && this.wallAtStart.getWallAtEnd() == wall;
      this.joinedAtStartOfWallAtStart =
          this.wallAtStart != null
          && this.wallAtStart.getWallAtStart() == wall;
      this.wallAtEnd = wall.getWallAtEnd();
      this.joinedAtEndOfWallAtEnd =
          this.wallAtEnd != null
          && wallAtEnd.getWallAtEnd() == wall;
      this.joinedAtStartOfWallAtEnd =
          this.wallAtEnd != null
          && wallAtEnd.getWallAtStart() == wall;
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

    public boolean isJoinedAtEndOfWallAtStart() {
      return this.joinedAtEndOfWallAtStart;
    }

    public boolean isJoinedAtStartOfWallAtStart() {
      return this.joinedAtStartOfWallAtStart;
    }

    public boolean isJoinedAtEndOfWallAtEnd() {
      return this.joinedAtEndOfWallAtEnd;
    }

    public boolean isJoinedAtStartOfWallAtEnd() {
      return this.joinedAtStartOfWallAtEnd;
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
  private static class PointWithMagnetism {
    private static final int STEP_COUNT = 24; // 15 degrees step 
    private float xMagnetizedPoint;
    private float yMagnetizedPoint;
    
    /**
     * Create a point that applies magnetism to point (<code>x</code>,
     * <code>y</code>). Point end coordinates may be different from
     * x or y, to match the closest point belonging to one of the radius of a
     * circle centered at (<code>xStart</code>, <code>yStart</code>), each
     * radius being a multiple of 15 degrees. The length of the line joining
     * (<code>xStart</code>, <code>yStart</code>) to the computed point is 
     * approximated depending on the current <code>unit</code> and scale.
     */
    public PointWithMagnetism(float xStart, float yStart, float x, float y, 
                              UserPreferences.Unit unit, float maxLengthDelta) {
      this.xMagnetizedPoint = x;
      this.yMagnetizedPoint = y;
      if (xStart == x) {
        // Apply magnetism to the length of the line joining start point to magnetized point
        float magnetizedLength = unit.getMagnetizedLength(Math.abs(yStart - y), maxLengthDelta);
        this.yMagnetizedPoint = yStart + (float)(magnetizedLength * Math.signum(y - yStart));
      } else if (yStart == y) {
        // Apply magnetism to the length of the line joining start point to magnetized point
        float magnetizedLength = unit.getMagnetizedLength(Math.abs(xStart - x), maxLengthDelta);
        this.xMagnetizedPoint = xStart + (float)(magnetizedLength * Math.signum(x - xStart));
      } else { // xStart != x && yStart != y
        double angleStep = 2 * Math.PI / STEP_COUNT; 
        // Caution : pixel coordinate space is indirect !
        double angle = Math.atan2(yStart - y, x - xStart);
        // Compute previous angle closest to a step angle (multiple of angleStep) 
        double previousStepAngle = Math.floor(angle / angleStep) * angleStep;
        double angle1;
        double tanAngle1;
        double angle2;
        double tanAngle2;
        // Compute the tan of previousStepAngle and the next step angle
        if (Math.tan(angle) > 0) {
          angle1 = previousStepAngle;
          tanAngle1 = Math.tan(previousStepAngle);
          angle2 = previousStepAngle + angleStep;
          tanAngle2 = Math.tan(previousStepAngle + angleStep);
        } else {
          // If slope is negative inverse the order of the two angles
          angle1 = previousStepAngle + angleStep;
          tanAngle1 = Math.tan(previousStepAngle + angleStep);
          angle2 = previousStepAngle;
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
        double magnetismAngle;
        if (Math.abs(xEnd2 - xEnd1) < Math.abs(yEnd1 - yEnd2)) {
          magnetismAngle = angle2; 
          this.xMagnetizedPoint = xStart + (float)((yStart - y) / tanAngle2);            
        } else {
          magnetismAngle = angle1; 
          this.yMagnetizedPoint = yStart - (float)((x - xStart) * tanAngle1);
        }
        
        // Apply magnetism to the length of the line joining start point 
        // to magnetized point
        float magnetizedLength = unit.getMagnetizedLength((float)Point2D.distance(xStart, yStart, 
            this.xMagnetizedPoint, this.yMagnetizedPoint), maxLengthDelta);
        this.xMagnetizedPoint = xStart + (float)(magnetizedLength * Math.cos(magnetismAngle));            
        this.yMagnetizedPoint = yStart - (float)(magnetizedLength * Math.sin(magnetismAngle));
      }       
    }

    /**
     * Returns the abscissa of end point computed with magnetism.
     */
    float getXMagnetizedPoint() {
      return this.xMagnetizedPoint;
    }

    /**
     * Returns the ordinate of end point computed with magnetism.
     */
    float getYMagnetizedPoint() {
      return this.yMagnetizedPoint;
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

    public void activateDuplication(boolean duplicationActivated) {
    }

    public void pressMouse(float x, float y, int clickCount, 
                           boolean shiftDown, boolean duplicationActivated) {
    }

    public void releaseMouse(float x, float y) {
    }

    public void moveMouse(float x, float y) {
    }
  }

  // ControllerState subclasses
  
  /**
   * Default selection state. This state manages transition to
   * <code>WALL_CREATION</code> and <code>DIMENSION_LINES_CREATION</code> mode, 
   * the deletion of selected objects, and the move of selected objects with arrow keys.
   */
  private class SelectionState extends ControllerState {
    @Override
    public Mode getMode() {
      return Mode.SELECTION;
    }

    @Override
    public void enter() {
      moveMouse(getXLastMouseMove(), getYLastMouseMove());
      ((PlanComponent)getView()).setResizeIndicatorVisible(true);
    }
    
    @Override
    public void setMode(Mode mode) {
      switch (mode) {
        case WALL_CREATION :
          setState(getWallCreationState());
          break;
        case DIMENSION_LINE_CREATION :
          setState(getDimensionLineCreationState());
          break;
      } 
    }

    @Override
    public void deleteSelection() {
      deleteItems(home.getSelectedItems());
      updateCursor(getXLastMouseMove(), getYLastMouseMove());
    }

    @Override
    public void moveSelection(float dx, float dy) {
      moveAndShowSelectedItems(dx, dy);
      updateCursor(getXLastMouseMove(), getYLastMouseMove());
    }

    @Override
    public void moveMouse(float x, float y) {
      updateCursor(x, y);
    }

    private void updateCursor(float x, float y) {
      if (getYawRotatedCameraAt(x, y) != null
          || getPitchRotatedCameraAt(x, y) != null) {
        ((PlanComponent)getView()).setRotationCursor();
      } else if (getResizedDimensionLineStartAt(x, y) != null
          || getResizedDimensionLineEndAt(x, y) != null
          || getWidthAndDepthResizedPieceOfFurnitureAt(x, y) != null
          || getResizedWallStartAt(x, y) != null
          || getResizedWallEndAt(x, y) != null) {
        ((PlanComponent)getView()).setResizeCursor();
      } else if (getOffsetDimensionLineAt(x, y) != null
          || getHeightResizedPieceOfFurnitureAt(x, y) != null) {
        ((PlanComponent)getView()).setHeightCursor();
      } else if (getRotatedPieceOfFurnitureAt(x, y) != null) {
        ((PlanComponent)getView()).setRotationCursor();
      } else if (getElevatedPieceOfFurnitureAt(x, y) != null) {
        ((PlanComponent)getView()).setElevationCursor();
      } else {
        ((PlanComponent)getView()).setCursor(Mode.SELECTION);
      }
    }

    @Override
    public void pressMouse(float x, float y, int clickCount,
                           boolean shiftDown, boolean duplicationActivated) {
      if (clickCount == 1) {
        if (getYawRotatedCameraAt(x, y) != null) {
          setState(getCameraYawRotationState());
        } else if (getPitchRotatedCameraAt(x, y) != null) {
          setState(getCameraPitchRotationState());
        } else if (getResizedDimensionLineStartAt(x, y) != null
            || getResizedDimensionLineEndAt(x, y) != null) {
          setState(getDimensionLineResizeState());
        } else if (getWidthAndDepthResizedPieceOfFurnitureAt(x, y) != null) {
          setState(getPieceOfFurnitureResizeState());
        } else if (getResizedWallStartAt(x, y) != null
            || getResizedWallEndAt(x, y) != null) {
          setState(getWallResizeState());
        } else if (getOffsetDimensionLineAt(x, y) != null) {
          setState(getDimensionLineOffsetState());
        } else if (getHeightResizedPieceOfFurnitureAt(x, y) != null) {
          setState(getPieceOfFurnitureHeightState());
        } else if (getRotatedPieceOfFurnitureAt(x, y) != null) {
          setState(getPieceOfFurnitureRotationState());
        } else if (getElevatedPieceOfFurnitureAt(x, y) != null) {
          setState(getPieceOfFurnitureElevationState());
        } else {
          Object item = getItemAt(x, y);
          // If shift isn't pressed, and an item is under cursor position
          if (!shiftDown && item != null) {
            // Change state to SelectionMoveState
            setState(getSelectionMoveState());
          } else {
            // Otherwise change state to RectangleSelectionState
            setState(getRectangleSelectionState());
          }
          
        }
      } else if (clickCount == 2) {
        Object item = getItemAt(x, y);
        // If shift isn't pressed, and an item is under cursor position
        if (!shiftDown && item != null) {
          // Modify selected item on a double click
          if (item instanceof Wall) {
            modifySelectedWalls();
          } else if (item instanceof HomePieceOfFurniture) {
            modifySelectedFurniture();
          } 
        }
      }
    }
    
    @Override
    public void exit() {
      ((PlanComponent)getView()).setResizeIndicatorVisible(false);
    }
  }

  /**
   * Move selection state. This state manages the move of current selected walls
   * with mouse and the selection of one wall, if mouse isn't moved while button
   * is depressed.
   */
  private class SelectionMoveState extends ControllerState {
    private float        xLastMouseMove;
    private float        yLastMouseMove;
    private boolean      mouseMoved;
    private List<Object> movedItems;
    private List<Object> duplicatedItems;
  
    @Override
    public Mode getMode() {
      return Mode.SELECTION;
    }
    
    @Override
    public void enter() {
      this.xLastMouseMove = getXLastMousePress();
      this.yLastMouseMove = getYLastMousePress();
      this.mouseMoved = false;
      Object itemUnderCursor = getItemAt(getXLastMousePress(),
          getYLastMousePress());
      this.movedItems = home.getSelectedItems();
      // If the item under the cursor doesn't belong to selection
      if (itemUnderCursor != null && !this.movedItems.contains(itemUnderCursor)) {
        // Select only the item under cursor position
        selectItem(itemUnderCursor);
        this.movedItems = home.getSelectedItems();
      }
      this.duplicatedItems = null;
      activateDuplication(wasDuplicationActivatedLastMousePress());
    }
    
    @Override
    public void moveMouse(float x, float y) {      
      moveItems(this.movedItems, 
          x - this.xLastMouseMove, y - this.yLastMouseMove);
      ((PlanComponent)getView()).makePointVisible(x, y);
      this.xLastMouseMove = x;
      this.yLastMouseMove = y;
      this.mouseMoved = true;
    }
  
    @Override
    public void releaseMouse(float x, float y) {
      if (this.mouseMoved) {
        // Post in undo support a move or duplicate operation if selection isn't a camera 
        if (!(this.movedItems.get(0) instanceof Camera)) {
          if (this.duplicatedItems != null) {
            postItemsDuplication(this.movedItems, this.duplicatedItems);
          } else {
            postItemsMove(this.movedItems,
                this.xLastMouseMove - getXLastMousePress(), 
                this.yLastMouseMove - getYLastMousePress());
          }
        }
      } else {
        // If mouse didn't move, select only the item at (x,y)
        Object itemUnderCursor = getItemAt(x, y);
        if (itemUnderCursor != null) {
          // Select only the item under cursor position
          selectItem(itemUnderCursor);
        }
      }
      // Change the state to SelectionState
      setState(getSelectionState());
    }
  
    @Override
    public void escape() {
      if (this.duplicatedItems != null) {
        // Delete moved items and select original items
        doDeleteItems(this.movedItems);
        selectItems(this.duplicatedItems);
      } else if (this.mouseMoved) {
        // Put items back to their initial location
        moveItems(this.movedItems, 
            getXLastMousePress() - this.xLastMouseMove, 
            getYLastMousePress() - this.yLastMouseMove);
      }
      // Change the state to SelectionState
      setState(getSelectionState());
    }
    
    @Override
    public void activateDuplication(boolean duplicationActivated) {
      if (!(this.movedItems.get(0) instanceof Camera)) {
        if (duplicationActivated) {
          // Duplicate original items and add them to home
          this.duplicatedItems = this.movedItems;          
          this.movedItems = HomeTransferableList.deepCopy(this.movedItems);          
          for (Object item : this.movedItems) {
            if (item instanceof Wall) {
              home.addWall((Wall)item);
            } else if (item instanceof DimensionLine) {
              home.addDimensionLine((DimensionLine)item);
            } else if (item instanceof HomePieceOfFurniture) {
              home.addPieceOfFurniture((HomePieceOfFurniture)item);
            }
          }
          
          // Put original items back to their initial location
          moveItems(this.duplicatedItems, 
              getXLastMousePress() - this.xLastMouseMove, 
              getYLastMousePress() - this.yLastMouseMove);
          ((PlanComponent)getView()).setDuplicationCursor();
        } else if (this.duplicatedItems != null) {
          // Delete moved items 
          doDeleteItems(this.movedItems);
          
          // Move original items to the current location
          moveItems(this.duplicatedItems, 
              this.xLastMouseMove - getXLastMousePress(), 
              this.yLastMouseMove - getYLastMousePress());
          this.movedItems = this.duplicatedItems;
          this.duplicatedItems = null;
          ((PlanComponent)getView()).setCursor(Mode.SELECTION);
        }
        
        selectItems(this.movedItems);
      }
    }
  }

  /**
   * Selection with rectangle state. This state manages selection when mouse
   * press is done outside of an item or when mouse press is done with shift key
   * down.
   */
  private class RectangleSelectionState extends ControllerState {
    private List<Object> selectedItemsMousePressed;  
    private boolean      mouseMoved;
  
    @Override
    public Mode getMode() {
      return Mode.SELECTION;
    }

    @Override
    public void enter() {
      Object itemUnderCursor = 
          getItemAt(getXLastMousePress(), getYLastMousePress());
      // If no item under cursor and shift wasn't down, deselect all
      if (itemUnderCursor == null && !wasShiftDownLastMousePress()) {
        deselectAll();
      } 
      // Store current selection
      this.selectedItemsMousePressed = 
        new ArrayList<Object>(home.getSelectedItems());
      this.mouseMoved = false;
    }

    @Override
    public void moveMouse(float x, float y) {
      this.mouseMoved = true;
      updateSelectedItems(getXLastMousePress(), getYLastMousePress(), 
          x, y, this.selectedItemsMousePressed);
      // Update rectangle feedback
      PlanComponent planView = (PlanComponent)getView();
      planView.setRectangleFeedback(
          getXLastMousePress(), getYLastMousePress(), x, y);
      planView.makePointVisible(x, y);
    }

    @Override
    public void releaseMouse(float x, float y) {
      // If cursor didn't move
      if (!this.mouseMoved) {
        Object itemUnderCursor = getItemAt(x, y);
        // Toggle selection of the item under cursor 
        if (itemUnderCursor != null) {
          if (this.selectedItemsMousePressed.contains(itemUnderCursor)) {
            this.selectedItemsMousePressed.remove(itemUnderCursor);
          } else {
            // Remove any camera from current selection 
            for (Iterator<Object> iter = this.selectedItemsMousePressed.iterator(); iter.hasNext();) {
              if (iter.next() instanceof Camera) {
                iter.remove();
              }
            }
            // Let the camera belong to selection only if no item are selected
            if (!(itemUnderCursor instanceof Camera)
                || this.selectedItemsMousePressed.size() == 0) {
              this.selectedItemsMousePressed.add(itemUnderCursor);
            }
          }
          selectItems(this.selectedItemsMousePressed);
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
      ((PlanComponent)getView()).deleteRectangleFeedback();
      this.selectedItemsMousePressed = null;
    }

    /**
     * Updates selection from <code>selectedItemsMousePressed</code> and the
     * items that intersects the rectangle at coordinates (<code>x0</code>,
     * <code>y0</code>) and (<code>x1</code>, <code>y1</code>).
     */
    private void updateSelectedItems(float x0, float y0, 
                                     float x1, float y1,
                                     List<Object> selectedItemsMousePressed) {
      List<Object> selectedItems;
      boolean shiftDown = wasShiftDownLastMousePress();
      if (shiftDown) {
        selectedItems = new ArrayList<Object>(selectedItemsMousePressed);
      } else {
        selectedItems = new ArrayList<Object>();
      }
      
      // For all the items that intersects with rectangle
      for (Object item : getRectangleItems(x0, y0, x1, y1)) {
        // Don't let the camera be able to be selected with a rectangle
        if (!(item instanceof Camera)) {
          // If shift was down at mouse press
          if (shiftDown) {
            // Toogle selection of item
            if (selectedItemsMousePressed.contains(item)) {
              selectedItems.remove(item);
            } else {
              selectedItems.add(item);
            }
          } else if (!selectedItemsMousePressed.contains(item)) {
            // Else select the wall
            selectedItems.add(item);
          }
        }
      }    
      // Update selection
      selectItems(selectedItems);
    }
  }

  /**
   * Wall creation state. This state manages transition to
   * <code>SELECTION</code> and <code>DIMENSION_LINES_CREATION</code> modes, 
   * and initial wall creation.
   */
  private class WallCreationState extends ControllerState {
    @Override
    public Mode getMode() {
      return Mode.WALL_CREATION;
    }

    @Override
    public void enter() {
      ((PlanComponent)getView()).setCursor(Mode.WALL_CREATION);
    }

    @Override
    public void setMode(Mode mode) {
      switch (mode) {
        case SELECTION :
          // Change state to SelectionState
          setState(getSelectionState());
          break;
        case DIMENSION_LINE_CREATION :
          setState(getDimensionLineCreationState());
          break;
      } 
    }

    @Override
    public void moveMouse(float x, float y) {
      ((PlanComponent)getView()).setWallAlignmentFeeback(null, x, y);
    }

    @Override
    public void pressMouse(float x, float y, int clickCount,
                           boolean shiftDown, boolean duplicationActivated) {
      // Change state to NewWallState
      setState(getNewWallState());
    }

    @Override
    public void exit() {
      ((PlanComponent)getView()).deleteWallAlignmentFeeback();
    }  
  }

  /**
   * Wall modification state.  
   */
  private abstract class AbstractWallState extends ControllerState {
    private String wallLengthToolTipFeedback;
    
    @Override
    public void enter() {
      this.wallLengthToolTipFeedback = resource.getString("wallLengthToolTipFeedback");
    }
    
    protected String getToolTipFeedbackText(Wall wall) {
      float length = (float)Point2D.distance(wall.getXStart(), wall.getYStart(), 
          wall.getXEnd(), wall.getYEnd());
      return String.format(this.wallLengthToolTipFeedback, 
          preferences.getUnit().getLengthFormatWithUnit().format(length));
    }
  }

  /**
   * New wall state. This state manages wall creation at each mouse press. 
   */
  private class NewWallState extends AbstractWallState {
    private float        xStart;
    private float        yStart;
    private float        xLastEnd;
    private float        yLastEnd;
    private Wall         wallStartAtStart;
    private Wall         wallEndAtStart;
    private Wall         newWall;
    private Wall         wallStartAtEnd;
    private Wall         wallEndAtEnd;
    private Wall         lastWall;
    private List<Object> oldSelection;
    private List<Wall>   newWalls;
    private boolean      magnetismEnabled;
    
    @Override
    public Mode getMode() {
      return Mode.WALL_CREATION;
    }
    
    @Override
    public void setMode(Mode mode) {
      switch (mode) {
        case SELECTION :
          // Escape current creation and change state to SelectionState
          escape();
          setState(getSelectionState());
          break;
        case DIMENSION_LINE_CREATION :
          // Escape current creation and change state to DimensionLineCreationState
          escape();
          setState(getDimensionLineCreationState());
          break;
      } 
    }

    @Override
    public void enter() {
      super.enter();
      this.oldSelection = home.getSelectedItems();
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
      this.newWalls = new ArrayList<Wall>();
      deselectAll();
      toggleMagnetism(wasShiftDownLastMousePress());
      PlanComponent planView = (PlanComponent)getView();
      planView.setWallAlignmentFeeback(null, 
          getXLastMousePress(), getYLastMousePress());
    }

    @Override
    public void moveMouse(float x, float y) {
      PlanComponent planView = (PlanComponent)getView();
      // Compute the coordinates where wall end point should be moved
      float xEnd;
      float yEnd;
      if (this.magnetismEnabled) {
        PointWithMagnetism point = new PointWithMagnetism(
            this.xStart, this.yStart, x, y, preferences.getUnit(), planView.getPixelLength());
        xEnd = point.getXMagnetizedPoint();
        yEnd = point.getYMagnetizedPoint();
      } else {
        xEnd = x;
        yEnd = y;
      }

      // If current wall doesn't exist
      if (this.newWall == null) {
        // Create a new one
        this.newWall = createNewWall(this.xStart, this.yStart, 
            xEnd, yEnd, this.wallStartAtStart, this.wallEndAtStart);
        this.newWalls.add(this.newWall);
      } else {
        // Otherwise update its end point
        home.moveWallEndPointTo(this.newWall, xEnd, yEnd); 
      }         
      planView.setToolTipFeedback(getToolTipFeedbackText(this.newWall), x, y);
      planView.setWallAlignmentFeeback(this.newWall, xEnd, yEnd);
      
      // If the start or end line of a wall close to (xEnd, yEnd) is
      // free, it will the wall at end of the new wall.
      this.wallStartAtEnd = getWallStartAt(xEnd, yEnd, this.newWall);
      if (this.wallStartAtEnd != null) {
        this.wallEndAtEnd = null;
        // Select the wall with a free start to display a feedback to user  
        selectItem(this.wallStartAtEnd);          
      } else {
        this.wallEndAtEnd = getWallEndAt(xEnd, yEnd, this.newWall);
        if (this.wallEndAtEnd != null) {
          // Select the wall with a free end to display a feedback to user  
          selectItem(this.wallEndAtEnd);          
        } else {
          deselectAll();
        }
      }

      // Ensure point at (x,y) is visible
      planView.makePointVisible(x, y);
      // Update move coordinates
      this.xLastEnd = xEnd;
      this.yLastEnd = yEnd;
    }

    @Override
    public void pressMouse(float x, float y, int clickCount, 
                           boolean shiftDown, boolean duplicationActivated) {
      if (clickCount == 2) {
        if (this.lastWall != null) {
          // Join last wall to the selected wall at its end
          joinNewWallEndToWall(this.lastWall, 
              this.wallStartAtEnd, this.wallEndAtEnd);
        }
        // Post walls creation to undo support
        postAddWalls(this.newWalls, this.oldSelection);
        selectItems(this.newWalls);
        // Change state to WallCreationState 
        setState(getWallCreationState());
      } else {
        // Create a new wall only when it will have a length > 0
        // meaning after the first mouse move
        if (this.newWall != null) {
          ((PlanComponent)getView()).deleteToolTipFeedback();
          selectItem(this.newWall);
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
        moveMouse(getXLastMouseMove(), getYLastMouseMove());
      }
    }

    @Override
    public void escape() {
      if (this.newWall != null) {
        // Delete current created wall
        home.deleteWall(this.newWall);
        this.newWalls.remove(this.newWall);
      }
      // Post other walls creation to undo support
      postAddWalls(this.newWalls, this.oldSelection);
      selectItems(this.newWalls);
      // Change state to WallCreationState 
      setState(getWallCreationState());
    }

    @Override
    public void exit() {
      PlanComponent planView = (PlanComponent)getView();
      planView.deleteToolTipFeedback();
      planView.deleteWallAlignmentFeeback();
      this.wallStartAtStart = null;
      this.wallEndAtStart = null;
      this.newWall = null;
      this.wallStartAtEnd = null;
      this.wallEndAtEnd = null;
      this.lastWall = null;
      this.oldSelection = null;
      this.newWalls = null;
    }  
  }

  /**
   * Wall resize state. This state manages wall resizing. 
   */
  private class WallResizeState extends AbstractWallState {
    private Wall         selectedWall;
    private boolean      startPoint;
    private float        oldX;
    private float        oldY;
    private float        deltaXToResizePoint;
    private float        deltaYToResizePoint;
    private boolean      magnetismEnabled;
    
    @Override
    public Mode getMode() {
      return Mode.SELECTION;
    }
    
    @Override
    public void enter() {
      super.enter();
      this.selectedWall = (Wall)home.getSelectedItems().get(0);
      this.startPoint = this.selectedWall 
          == getResizedWallStartAt(getXLastMousePress(), getYLastMousePress());
      if (this.startPoint) {
        this.oldX = this.selectedWall.getXStart();
        this.oldY = this.selectedWall.getYStart();
      } else {
        this.oldX = this.selectedWall.getXEnd();
        this.oldY = this.selectedWall.getYEnd();
      }
      this.deltaXToResizePoint = getXLastMousePress() - this.oldX;
      this.deltaYToResizePoint = getYLastMousePress() - this.oldY;
      toggleMagnetism(wasShiftDownLastMousePress());
      PlanComponent planView = (PlanComponent)getView();
      planView.setResizeIndicatorVisible(true);
      planView.setToolTipFeedback(getToolTipFeedbackText(this.selectedWall), 
          getXLastMousePress(), getYLastMousePress());
      planView.setWallAlignmentFeeback(this.selectedWall, this.oldX, this.oldY);
    }
    
    @Override
    public void moveMouse(float x, float y) {
      PlanComponent planView = (PlanComponent)getView();
      float newX = x - this.deltaXToResizePoint;
      float newY = y - this.deltaYToResizePoint;
      if (this.magnetismEnabled) {
        PointWithMagnetism point = new PointWithMagnetism(
            this.startPoint 
                ? this.selectedWall.getXEnd()
                : this.selectedWall.getXStart(), 
            this.startPoint 
                ? this.selectedWall.getYEnd()
                : this.selectedWall.getYStart(), newX, newY, 
            preferences.getUnit(), planView.getPixelLength());
        newX = point.getXMagnetizedPoint();
        newY = point.getYMagnetizedPoint();
      } 
      moveWallPoint(this.selectedWall, newX, newY, this.startPoint);

      planView.setToolTipFeedback(getToolTipFeedbackText(this.selectedWall), x, y);
      planView.setWallAlignmentFeeback(this.selectedWall, newX, newY);
      // Ensure point at (x,y) is visible
      planView.makePointVisible(x, y);
    }

    @Override
    public void releaseMouse(float x, float y) {
      postWallResize(this.selectedWall, this.oldX, this.oldY, this.startPoint);
      setState(getSelectionState());
    }

    @Override
    public void toggleMagnetism(boolean magnetismToggled) {
      // Compute active magnetism
      this.magnetismEnabled = preferences.isMagnetismEnabled()
                              ^ magnetismToggled;
      moveMouse(getXLastMouseMove(), getYLastMouseMove());
    }

    @Override
    public void escape() {
      moveWallPoint(this.selectedWall, this.oldX, this.oldY, this.startPoint);
      setState(getSelectionState());
    }

    @Override
    public void exit() {
      PlanComponent planView = (PlanComponent)getView();
      planView.setResizeIndicatorVisible(false);
      planView.deleteToolTipFeedback();
      planView.deleteWallAlignmentFeeback();
      this.selectedWall = null;
    }  
  }

  /**
   * Furniture rotation state. This states manages the rotation of a piece of furniture.
   */
  private class PieceOfFurnitureRotationState extends ControllerState {
    private static final int     STEP_COUNT = 24;
    private boolean              magnetismEnabled;
    private HomePieceOfFurniture selectedPiece;
    private float                angleMousePress;
    private float                oldAngle;
    private String               rotationToolTipFeedback;

    @Override
    public Mode getMode() {
      return Mode.SELECTION;
    }
    
    @Override
    public void enter() {
      this.rotationToolTipFeedback = resource.getString("rotationToolTipFeedback");
      this.selectedPiece = (HomePieceOfFurniture)home.getSelectedItems().get(0);
      this.angleMousePress = (float)Math.atan2(this.selectedPiece.getY() - getYLastMousePress(), 
          getXLastMousePress() - this.selectedPiece.getX()); 
      this.oldAngle = this.selectedPiece.getAngle();
      this.magnetismEnabled = preferences.isMagnetismEnabled()
                              ^ wasShiftDownLastMousePress();
      PlanComponent planView = (PlanComponent)getView();
      planView.setResizeIndicatorVisible(true);
      planView.setToolTipFeedback(getToolTipFeedbackText(this.oldAngle), 
          getXLastMousePress(), getYLastMousePress());
    }
    
    @Override
    public void moveMouse(float x, float y) {      
      // Compute the new angle of the piece
      float angleMouseMove = (float)Math.atan2(this.selectedPiece.getY() - y, 
          x - this.selectedPiece.getX()); 
      float newAngle = this.oldAngle - angleMouseMove + this.angleMousePress;
      
      if (this.magnetismEnabled) {
        float angleStep = 2 * (float)Math.PI / STEP_COUNT; 
        // Compute angles closest to a step angle (multiple of angleStep) 
        newAngle = Math.round(newAngle / angleStep) * angleStep;
      }

      // Update piece new angle
      home.setPieceOfFurnitureAngle(this.selectedPiece, newAngle); 

      // Ensure point at (x,y) is visible
      PlanComponent planView = (PlanComponent)getView();
      planView.makePointVisible(x, y);
      planView.setToolTipFeedback(getToolTipFeedbackText(newAngle), x, y);
    }

    @Override
    public void releaseMouse(float x, float y) {
      postPieceOfFurnitureRotation(this.selectedPiece, this.oldAngle);
      setState(getSelectionState());
    }

    @Override
    public void toggleMagnetism(boolean magnetismToggled) {
      // Compute active magnetism
      this.magnetismEnabled = preferences.isMagnetismEnabled()
                              ^ magnetismToggled;
      // Compute again angle as if mouse moved
      moveMouse(getXLastMouseMove(), getYLastMouseMove());
    }

    @Override
    public void escape() {
      home.setPieceOfFurnitureAngle(this.selectedPiece, oldAngle);
      setState(getSelectionState());
    }
    
    @Override
    public void exit() {
      PlanComponent planView = (PlanComponent)getView();
      planView.setResizeIndicatorVisible(false);
      planView.deleteToolTipFeedback();
      this.selectedPiece = null;
    }  

    private String getToolTipFeedbackText(float angle) {
      return String.format(this.rotationToolTipFeedback, 
          (Math.round(Math.toDegrees(angle)) + 360) % 360);
    }
  }

  /**
   * Furniture elevation state. This states manages the elevation change of a piece of furniture.
   */
  private class PieceOfFurnitureElevationState extends ControllerState {
    private boolean              magnetismEnabled;
    private float                deltaYToElevationVertex;
    private HomePieceOfFurniture selectedPiece;
    private float                oldElevation;
    private String               elevationToolTipFeedback;

    @Override
    public Mode getMode() {
      return Mode.SELECTION;
    }
    
    @Override
    public void enter() {
      this.elevationToolTipFeedback = resource.getString("elevationToolTipFeedback");
      this.selectedPiece = (HomePieceOfFurniture)home.getSelectedItems().get(0);
      float [] elevationPoint = this.selectedPiece.getPoints() [1];
      this.deltaYToElevationVertex = getYLastMousePress() - elevationPoint [1];
      this.oldElevation = this.selectedPiece.getElevation();
      this.magnetismEnabled = preferences.isMagnetismEnabled()
                              ^ wasShiftDownLastMousePress();
      PlanComponent planView = (PlanComponent)getView();
      planView.setResizeIndicatorVisible(true);
      planView.setToolTipFeedback(getToolTipFeedbackText(this.oldElevation), 
          getXLastMousePress(), getYLastMousePress());
    }
    
    @Override
    public void moveMouse(float x, float y) {
      // Compute the new height of the piece 
      PlanComponent planView = (PlanComponent)getView();
      float [] topRightPoint = this.selectedPiece.getPoints() [1];
      float deltaY = y - this.deltaYToElevationVertex - topRightPoint[1];
      float newElevation = this.oldElevation - deltaY;
      newElevation = Math.max(newElevation, 0f);
      if (this.magnetismEnabled) {
        newElevation = preferences.getUnit().getMagnetizedLength(newElevation, planView.getPixelLength());
      }

      // Update piece new dimension
      home.setPieceOfFurnitureElevation(this.selectedPiece, newElevation);

      // Ensure point at (x,y) is visible
      planView.makePointVisible(x, y);
      planView.setToolTipFeedback(getToolTipFeedbackText(newElevation), x, y);
    }

    @Override
    public void releaseMouse(float x, float y) {
      postPieceOfFurnitureElevation(this.selectedPiece, this.oldElevation);
      setState(getSelectionState());
    }

    @Override
    public void toggleMagnetism(boolean magnetismToggled) {
      // Compute active magnetism
      this.magnetismEnabled = preferences.isMagnetismEnabled()
                              ^ magnetismToggled;
      // Compute again angle as if mouse moved
      moveMouse(getXLastMouseMove(), getYLastMouseMove());
    }

    @Override
    public void escape() {
      home.setPieceOfFurnitureElevation(this.selectedPiece, this.oldElevation);
      setState(getSelectionState());
    }

    @Override
    public void exit() {
      PlanComponent planView = (PlanComponent)getView();
      planView.setResizeIndicatorVisible(false);
      planView.deleteToolTipFeedback();
      this.selectedPiece = null;
    }  
    
    private String getToolTipFeedbackText(float height) {
      return String.format(this.elevationToolTipFeedback,  
          preferences.getUnit().getLengthFormatWithUnit().format(height));
    }
  }

  /**
   * Furniture height state. This states manages the height resizing of a piece of furniture.
   */
  private class PieceOfFurnitureHeightState extends ControllerState {
    private boolean              magnetismEnabled;
    private float                deltaYToResizeVertex;
    private HomePieceOfFurniture selectedPiece;
    private float                oldHeight;
    private String               resizeToolTipFeedback;

    @Override
    public Mode getMode() {
      return Mode.SELECTION;
    }
    
    @Override
    public void enter() {
      this.resizeToolTipFeedback = resource.getString("heightResizeToolTipFeedback");
      this.selectedPiece = (HomePieceOfFurniture)home.getSelectedItems().get(0);
      float [] resizePoint = this.selectedPiece.getPoints() [3];
      this.deltaYToResizeVertex = getYLastMousePress() - resizePoint [1];
      this.oldHeight = this.selectedPiece.getHeight();
      this.magnetismEnabled = preferences.isMagnetismEnabled()
                              ^ wasShiftDownLastMousePress();
      PlanComponent planView = (PlanComponent)getView();
      planView.setResizeIndicatorVisible(true);
      planView.setToolTipFeedback(getToolTipFeedbackText(this.oldHeight), 
          getXLastMousePress(), getYLastMousePress());
    }
    
    @Override
    public void moveMouse(float x, float y) {
      // Compute the new height of the piece 
      PlanComponent planView = (PlanComponent)getView();
      float [] bottomLeftPoint = this.selectedPiece.getPoints() [3];
      float deltaY = y - this.deltaYToResizeVertex - bottomLeftPoint[1];
      float newHeight = this.oldHeight - deltaY;
      newHeight = Math.max(newHeight, 0f);
      if (this.magnetismEnabled) {
        newHeight = preferences.getUnit().getMagnetizedLength(newHeight, planView.getPixelLength());
      }
      newHeight = Math.max(newHeight, preferences.getUnit().getMinimumLength());

      // Update piece new dimension
      home.setPieceOfFurnitureSize(this.selectedPiece, 
          this.selectedPiece.getWidth(), this.selectedPiece.getDepth(), 
          newHeight);

      // Ensure point at (x,y) is visible
      planView.makePointVisible(x, y);
      planView.setToolTipFeedback(getToolTipFeedbackText(newHeight), x, y);
    }

    @Override
    public void releaseMouse(float x, float y) {
      postPieceOfFurnitureHeightResize(this.selectedPiece, this.oldHeight);
      setState(getSelectionState());
    }

    @Override
    public void toggleMagnetism(boolean magnetismToggled) {
      // Compute active magnetism
      this.magnetismEnabled = preferences.isMagnetismEnabled()
                              ^ magnetismToggled;
      // Compute again angle as if mouse moved
      moveMouse(getXLastMouseMove(), getYLastMouseMove());
    }

    @Override
    public void escape() {
      home.setPieceOfFurnitureSize(this.selectedPiece, 
          this.selectedPiece.getWidth(), this.selectedPiece.getDepth(), 
          this.oldHeight);
      setState(getSelectionState());
    }

    @Override
    public void exit() {
      PlanComponent planView = (PlanComponent)getView();
      planView.setResizeIndicatorVisible(false);
      planView.deleteToolTipFeedback();
      this.selectedPiece = null;
    }  
    
    private String getToolTipFeedbackText(float height) {
      return String.format(this.resizeToolTipFeedback,  
          preferences.getUnit().getLengthFormatWithUnit().format(height));
    }
  }

  /**
   * Furniture resize state. This states manages the resizing of a piece of furniture.
   */
  private class PieceOfFurnitureResizeState extends ControllerState {
    private boolean              magnetismEnabled;
    private float                deltaXToResizeVertex;
    private float                deltaYToResizeVertex;
    private HomePieceOfFurniture selectedPiece;
    private float                oldX;
    private float                oldY;
    private float                oldWidth;
    private float                oldDepth;
    private String               widthResizeToolTipFeedback;
    private String               depthResizeToolTipFeedback;

    @Override
    public Mode getMode() {
      return Mode.SELECTION;
    }
    
    @Override
    public void enter() {
      this.widthResizeToolTipFeedback = resource.getString("widthResizeToolTipFeedback");
      this.depthResizeToolTipFeedback = resource.getString("depthResizeToolTipFeedback");
      this.selectedPiece = (HomePieceOfFurniture)home.getSelectedItems().get(0);
      float [] resizePoint = this.selectedPiece.getPoints() [2];
      this.deltaXToResizeVertex = getXLastMousePress() - resizePoint [0];
      this.deltaYToResizeVertex = getYLastMousePress() - resizePoint [1];
      this.oldX = this.selectedPiece.getX();
      this.oldY = this.selectedPiece.getY();
      this.oldWidth = this.selectedPiece.getWidth();
      this.oldDepth = this.selectedPiece.getDepth();
      this.magnetismEnabled = preferences.isMagnetismEnabled()
                              ^ wasShiftDownLastMousePress();
      PlanComponent planView = (PlanComponent)getView();
      planView.setResizeIndicatorVisible(true);
      planView.setToolTipFeedback(getToolTipFeedbackText(this.oldWidth, this.oldDepth), 
          getXLastMousePress(), getYLastMousePress());
    }
    
    @Override
    public void moveMouse(float x, float y) {
      // Compute the new location and dimension of the piece to let 
      // its bottom right point be at mouse location
      PlanComponent planView = (PlanComponent)getView();
      float angle = this.selectedPiece.getAngle();
      double cos = Math.cos(angle); 
      double sin = Math.sin(angle); 
      float [] topLeftPoint = this.selectedPiece.getPoints() [0];
      float deltaX = x - this.deltaXToResizeVertex - topLeftPoint[0];
      float deltaY = y - this.deltaYToResizeVertex - topLeftPoint[1];
      float newWidth =  (float)(deltaY * sin + deltaX * cos);
      float newDepth =  (float)(deltaY * cos - deltaX * sin);

      if (this.magnetismEnabled) {
        newWidth = preferences.getUnit().getMagnetizedLength(newWidth, planView.getPixelLength());
        newDepth = preferences.getUnit().getMagnetizedLength(newDepth, planView.getPixelLength());
      }
      newWidth = Math.max(newWidth, preferences.getUnit().getMinimumLength());
      newDepth = Math.max(newDepth, preferences.getUnit().getMinimumLength());

      // Update piece new location
      float newX = (float)(topLeftPoint [0] + (newWidth * cos - newDepth * sin) / 2f);
      float newY = (float)(topLeftPoint [1] + (newWidth * sin + newDepth * cos) / 2f);
      home.setPieceOfFurnitureLocation(this.selectedPiece, newX, newY);
      // Update piece new dimension
      home.setPieceOfFurnitureSize(this.selectedPiece, newWidth, newDepth, 
          this.selectedPiece.getHeight());

      // Ensure point at (x,y) is visible
      planView.makePointVisible(x, y);
      planView.setToolTipFeedback(getToolTipFeedbackText(newWidth, newDepth), x, y);
    }

    @Override
    public void releaseMouse(float x, float y) {
      postPieceOfFurnitureWidthAndDepthResize(this.selectedPiece, this.oldX, this.oldY, 
          this.oldWidth, this.oldDepth);
      setState(getSelectionState());
    }

    @Override
    public void toggleMagnetism(boolean magnetismToggled) {
      // Compute active magnetism
      this.magnetismEnabled = preferences.isMagnetismEnabled()
                              ^ magnetismToggled;
      // Compute again angle as if mouse moved
      moveMouse(getXLastMouseMove(), getYLastMouseMove());
    }

    @Override
    public void escape() {
      home.setPieceOfFurnitureLocation(this.selectedPiece, this.oldX, this.oldY);
      home.setPieceOfFurnitureSize(this.selectedPiece, 
          this.oldWidth, this.oldDepth, this.selectedPiece.getHeight());
      setState(getSelectionState());
    }

    @Override
    public void exit() {
      PlanComponent planView = (PlanComponent)getView();
      planView.setResizeIndicatorVisible(false);
      planView.deleteToolTipFeedback();
      this.selectedPiece = null;
    }  
    
    private String getToolTipFeedbackText(float width, float depth) {
      return "<html>" + String.format(this.widthResizeToolTipFeedback,  
              preferences.getUnit().getLengthFormatWithUnit().format(width))
          + "<br>" + String.format(this.depthResizeToolTipFeedback,
              preferences.getUnit().getLengthFormatWithUnit().format(depth));
    }
  }

  /**
   * Camera yaw change state. This states manages the change of the observer camera yaw angle.
   */
  private class CameraYawRotationState extends ControllerState {
    private ObserverCamera selectedCamera;
    private float          angleMousePress;
    private float          oldYaw;
    private String         rotationToolTipFeedback;

    @Override
    public Mode getMode() {
      return Mode.SELECTION;
    }
    
    @Override
    public void enter() {
      this.rotationToolTipFeedback = resource.getString("cameraYawRotationToolTipFeedback");
      this.selectedCamera = (ObserverCamera)home.getSelectedItems().get(0);
      this.angleMousePress = (float)Math.atan2(this.selectedCamera.getY() - getYLastMousePress(), 
          getXLastMousePress() - this.selectedCamera.getX()); 
      this.oldYaw = this.selectedCamera.getYaw();
      PlanComponent planView = (PlanComponent)getView();
      planView.setResizeIndicatorVisible(true);
      planView.setToolTipFeedback(getToolTipFeedbackText(this.oldYaw), 
          getXLastMousePress(), getYLastMousePress());
    }
    
    @Override
    public void moveMouse(float x, float y) {      
      // Compute the new angle of the camera
      float angleMouseMove = (float)Math.atan2(this.selectedCamera.getY() - y, 
          x - this.selectedCamera.getX()); 
      float newYaw = this.oldYaw - angleMouseMove + this.angleMousePress;
      
      // Update camera new yaw angle
      home.setCameraAngles(this.selectedCamera, newYaw, this.selectedCamera.getPitch()); 

      ((PlanComponent)getView()).setToolTipFeedback(getToolTipFeedbackText(newYaw), x, y);
    }

    @Override
    public void releaseMouse(float x, float y) {
      setState(getSelectionState());
    }

    @Override
    public void escape() {
      home.setCameraAngles(this.selectedCamera, this.oldYaw, this.selectedCamera.getPitch());
      setState(getSelectionState());
    }
    
    @Override
    public void exit() {
      PlanComponent planView = (PlanComponent)getView();
      planView.setResizeIndicatorVisible(false);
      planView.deleteToolTipFeedback();
      this.selectedCamera = null;
    }  

    private String getToolTipFeedbackText(float angle) {
      return String.format(this.rotationToolTipFeedback, 
          (Math.round(Math.toDegrees(angle)) + 360) % 360);
    }
  }

  /**
   * Camera pitch rotation state. This states manages the change of the observer camera pitch angle.
   */
  private class CameraPitchRotationState extends ControllerState {
    private ObserverCamera selectedCamera;
    private float          oldPitch;
    private String         rotationToolTipFeedback;

    @Override
    public Mode getMode() {
      return Mode.SELECTION;
    }
    
    @Override
    public void enter() {
      this.rotationToolTipFeedback = resource.getString("cameraPitchRotationToolTipFeedback");
      this.selectedCamera = (ObserverCamera)home.getSelectedItems().get(0);
      this.oldPitch = this.selectedCamera.getPitch();
      PlanComponent planView = (PlanComponent)getView();
      planView.setResizeIndicatorVisible(true);
      planView.setToolTipFeedback(getToolTipFeedbackText(this.oldPitch), 
          getXLastMousePress(), getYLastMousePress());
    }
    
    @Override
    public void moveMouse(float x, float y) {      
      // Compute the new angle of the camera
      float newPitch = (float)(this.oldPitch 
          + (y - getYLastMousePress()) * Math.cos(this.selectedCamera.getYaw()) * Math.PI / 360
          - (x - getXLastMousePress()) * Math.sin(this.selectedCamera.getYaw()) * Math.PI / 360);
      // Check new angle is between -60° and 75°  
      newPitch = Math.max(newPitch, -(float)Math.PI / 3);
      newPitch = Math.min(newPitch, (float)Math.PI / 36 * 15);
      
      // Update camera pitch angle
      home.setCameraAngles(this.selectedCamera, this.selectedCamera.getYaw(), newPitch);
      
      ((PlanComponent)getView()).setToolTipFeedback(getToolTipFeedbackText(newPitch), x, y);
    }

    @Override
    public void releaseMouse(float x, float y) {
      setState(getSelectionState());
    }

    @Override
    public void escape() {
      home.setCameraAngles(this.selectedCamera, this.selectedCamera.getYaw(), this.oldPitch);
      setState(getSelectionState());
    }
    
    @Override
    public void exit() {
      PlanComponent planView = (PlanComponent)getView();
      planView.setResizeIndicatorVisible(false);
      planView.deleteToolTipFeedback();
      this.selectedCamera = null;
    }  

    private String getToolTipFeedbackText(float angle) {
      return String.format(this.rotationToolTipFeedback, 
          Math.round(Math.toDegrees(angle)) % 360);
    }
  }

  /**
   * Dimension line creation state. This state manages transition to
   * <code>SELECTION</code> mode, and initial dimension line creation.
   */
  private class DimensionLineCreationState extends ControllerState {
    @Override
    public Mode getMode() {
      return Mode.DIMENSION_LINE_CREATION;
    }

    @Override
    public void enter() {
      ((PlanComponent)getView()).setCursor(Mode.WALL_CREATION);
    }

    @Override
    public void setMode(Mode mode) {
      switch (mode) {
        case SELECTION :
          // Change state to SelectionState
          setState(getSelectionState());
          break;
        case WALL_CREATION :
          setState(getWallCreationState());
          break;
      } 
    }

    @Override
    public void moveMouse(float x, float y) {
      ((PlanComponent)getView()).setDimensionLineAlignmentFeeback(null, x, y);
    }

    @Override
    public void pressMouse(float x, float y, int clickCount,
                           boolean shiftDown, boolean duplicationActivated) {
      // Change state to NewWallState
      setState(getNewDimensionLineState());
    }

    @Override
    public void exit() {
      ((PlanComponent)getView()).deleteDimensionLineAlignmentFeeback();
    }  
  }

  /**
   * New dimension line state. This state manages dimension line creation at mouse press. 
   */
  private class NewDimensionLineState extends ControllerState {
    private float         xStart;
    private float         yStart;
    private DimensionLine newDimensionLine;
    private List<Object>  oldSelection;
    private boolean       magnetismEnabled;
    private boolean       offsetChoice;
    
    @Override
    public Mode getMode() {
      return Mode.DIMENSION_LINE_CREATION;
    }
    
    @Override
    public void setMode(Mode mode) {
      switch (mode) {
        case SELECTION :
          // Escape current creation and change state to SelectionState
          escape();
          setState(getSelectionState());
          break;
        case WALL_CREATION :
          // Escape current creation and change state to WallCreationState
          escape();
          setState(getWallCreationState());
          break;
      } 
    }

    @Override
    public void enter() {
      this.oldSelection = home.getSelectedItems();
      this.xStart = getXLastMousePress();
      this.yStart = getYLastMousePress();
      this.offsetChoice = false;
      this.newDimensionLine = null;
      deselectAll();
      toggleMagnetism(wasShiftDownLastMousePress());
      ((PlanComponent)getView()).setDimensionLineAlignmentFeeback(null, getXLastMousePress(), getYLastMousePress());
    }

    @Override
    public void moveMouse(float x, float y) {
      PlanComponent planView = (PlanComponent)getView();
      if (offsetChoice) {
        float distanceToDimensionLine = (float)Line2D.ptLineDist(this.xStart, this.yStart, 
            this.newDimensionLine.getXEnd(), this.newDimensionLine.getYEnd(), x, y);
        int relativeCCW = Line2D.relativeCCW(this.xStart, this.yStart, 
            this.newDimensionLine.getXEnd(), this.newDimensionLine.getYEnd(), x, y);
        home.setDimensionLineOffset(this.newDimensionLine, 
             -Math.signum(relativeCCW) * distanceToDimensionLine);
      } else {
        // Compute the coordinates where dimension line end point should be moved
        float xEnd;
        float yEnd;
        if (this.magnetismEnabled) {
          PointWithMagnetism point = new PointWithMagnetism(
              this.xStart, this.yStart, x, y,
              preferences.getUnit(), planView.getPixelLength());
          xEnd = point.getXMagnetizedPoint();
          yEnd = point.getYMagnetizedPoint();
        } else {
          xEnd = x;
          yEnd = y;
        }
  
        // If current dimension line doesn't exist
        if (this.newDimensionLine == null) {
          // Create a new one
          this.newDimensionLine = new DimensionLine(this.xStart, this.yStart, xEnd, yEnd, 0);
          home.addDimensionLine(newDimensionLine);
        } else {
          // Otherwise update its end point
          home.moveDimensionLineEndPointTo(this.newDimensionLine, xEnd, yEnd); 
        }         
        planView.setDimensionLineAlignmentFeeback(this.newDimensionLine, xEnd, yEnd);
      }
      // Ensure point at (x,y) is visible
      planView.makePointVisible(x, y);
    }

    @Override
    public void pressMouse(float x, float y, int clickCount, 
                           boolean shiftDown, boolean duplicationActivated) {
      // Create a new dimension line only when it will have a length > 0
      // meaning after the first mouse move
      if (this.newDimensionLine != null) {
        if (offsetChoice) {
          selectItem(this.newDimensionLine);
          // Post dimension line creation to undo support
          postAddDimensionLines(Arrays.asList(new DimensionLine [] {this.newDimensionLine}), 
              this.oldSelection);
          this.newDimensionLine = null;
          // Change state to WallCreationState 
          setState(getDimensionLineCreationState());
        } else {
          // Switch to offset choice
          offsetChoice = true;
          PlanComponent planView = (PlanComponent)getView();
          planView.setHeightCursor();
          planView.deleteDimensionLineAlignmentFeeback();
        }
      }
    }

    @Override
    public void toggleMagnetism(boolean magnetismToggled) {
      // Compute active magnetism
      this.magnetismEnabled = preferences.isMagnetismEnabled()
                              ^ magnetismToggled;
      // If the new dimension line already exists, 
      // compute again its end as if mouse moved
      if (this.newDimensionLine != null && !this.offsetChoice) {
        moveMouse(getXLastMouseMove(), getYLastMouseMove());
      }
    }

    @Override
    public void escape() {
      if (this.newDimensionLine != null) {
        // Delete current created dimension line
        home.deleteDimensionLine(this.newDimensionLine);
      }
      // Change state to DimensionLineCreationState 
      setState(getDimensionLineCreationState());
    }

    @Override
    public void exit() {
      ((PlanComponent)getView()).deleteDimensionLineAlignmentFeeback();
      this.newDimensionLine = null;
      this.oldSelection = null;
    }  
  }

  /**
   * Dimension line resize state. This state manages dimension line resizing. 
   */
  private class DimensionLineResizeState extends ControllerState {
    private DimensionLine selectedDimensionLine;
    private boolean       startPoint;
    private float         oldX;
    private float         oldY;
    private float         deltaXToResizePoint;
    private float         deltaYToResizePoint;
    private float         distanceFromResizePointToDimensionBaseLine;
    private boolean       magnetismEnabled;
    
    @Override
    public Mode getMode() {
      return Mode.SELECTION;
    }
    
    @Override
    public void enter() {
      PlanComponent planView = (PlanComponent)getView();
      planView.setResizeIndicatorVisible(true);
      
      this.selectedDimensionLine = (DimensionLine)home.getSelectedItems().get(0);
      this.startPoint = this.selectedDimensionLine 
          == getResizedDimensionLineStartAt(getXLastMousePress(), getYLastMousePress());
      if (this.startPoint) {
        this.oldX = this.selectedDimensionLine.getXStart();
        this.oldY = this.selectedDimensionLine.getYStart();
      } else {
        this.oldX = this.selectedDimensionLine.getXEnd();
        this.oldY = this.selectedDimensionLine.getYEnd();
      }

      float xResizePoint;
      float yResizePoint;
      // Compute the closest resize point placed on the extension line and the distance 
      // between that point and the dimension line base
      if (this.selectedDimensionLine.getXStart() == this.selectedDimensionLine.getXEnd()) {
        xResizePoint = getXLastMousePress();
        if (this.startPoint) {
          yResizePoint = this.selectedDimensionLine.getYStart();
        } else {
          yResizePoint = this.selectedDimensionLine.getYEnd();
        }
      } else if (this.selectedDimensionLine.getYStart() == this.selectedDimensionLine.getYEnd()) {
        if (this.startPoint) {
          xResizePoint = this.selectedDimensionLine.getXStart();
        } else {
          xResizePoint = this.selectedDimensionLine.getXEnd();
        }
        yResizePoint = getYLastMousePress();
      } else {
        float alpha1 = (float)(this.selectedDimensionLine.getYEnd() - this.selectedDimensionLine.getYStart()) 
            / (this.selectedDimensionLine.getXEnd() - this.selectedDimensionLine.getXStart());
        float beta1 = getYLastMousePress() - alpha1 * getXLastMousePress();
        float alpha2 = -1 / alpha1;
        float beta2;
        if (this.startPoint) {
          beta2 = this.selectedDimensionLine.getYStart() - alpha2 * this.selectedDimensionLine.getXStart();
        } else {
          beta2 = this.selectedDimensionLine.getYEnd() - alpha2 * this.selectedDimensionLine.getXEnd();
        }
        xResizePoint = (beta2 - beta1) / (alpha1 - alpha2);
        yResizePoint = alpha1 * xResizePoint + beta1;
      }

      this.deltaXToResizePoint = getXLastMousePress() - xResizePoint;
      this.deltaYToResizePoint = getYLastMousePress() - yResizePoint;
      if (this.startPoint) {
        this.distanceFromResizePointToDimensionBaseLine = (float)Point2D.distance(xResizePoint, yResizePoint, 
            this.selectedDimensionLine.getXStart(), this.selectedDimensionLine.getYStart());
        planView.setDimensionLineAlignmentFeeback(this.selectedDimensionLine, 
            this.selectedDimensionLine.getXStart(), this.selectedDimensionLine.getYStart());
      } else {
        this.distanceFromResizePointToDimensionBaseLine = (float)Point2D.distance(xResizePoint, yResizePoint, 
            this.selectedDimensionLine.getXEnd(), this.selectedDimensionLine.getYEnd());
        planView.setDimensionLineAlignmentFeeback(this.selectedDimensionLine, 
            this.selectedDimensionLine.getXEnd(), this.selectedDimensionLine.getYEnd());
      }
      toggleMagnetism(wasShiftDownLastMousePress());
    }
    
    @Override
    public void moveMouse(float x, float y) {
      PlanComponent planView = (PlanComponent)getView();
      float xResizePoint = x - this.deltaXToResizePoint;
      float yResizePoint = y - this.deltaYToResizePoint;
      if (this.startPoint) {
        // Compute the new start point of the dimension line knowing that the distance 
        // from resize point to dimension line base is constant, 
        // and that the end point of the dimension line doesn't move
        double distanceFromResizePointToDimensionLineEnd = Point2D.distance(xResizePoint, yResizePoint, 
            this.selectedDimensionLine.getXEnd(), this.selectedDimensionLine.getYEnd());
        double distanceFromDimensionLineStartToDimensionLineEnd = Math.sqrt(
            distanceFromResizePointToDimensionLineEnd * distanceFromResizePointToDimensionLineEnd
            - this.distanceFromResizePointToDimensionBaseLine * this.distanceFromResizePointToDimensionBaseLine);
        if (distanceFromDimensionLineStartToDimensionLineEnd > 0) {
          double dimensionLineRelativeAngle = -Math.atan2(this.distanceFromResizePointToDimensionBaseLine, 
              distanceFromDimensionLineStartToDimensionLineEnd);
          if (this.selectedDimensionLine.getOffset() >= 0) {
            dimensionLineRelativeAngle = -dimensionLineRelativeAngle;
          }
          double resizePointToDimensionLineEndAngle = Math.atan2(yResizePoint - this.selectedDimensionLine.getYEnd(), 
              xResizePoint - this.selectedDimensionLine.getXEnd());
          double dimensionLineStartToDimensionLineEndAngle = dimensionLineRelativeAngle + resizePointToDimensionLineEndAngle;
          float xNewStartPoint = this.selectedDimensionLine.getXEnd() + (float)(distanceFromDimensionLineStartToDimensionLineEnd 
              * Math.cos(dimensionLineStartToDimensionLineEndAngle));
          float yNewStartPoint = this.selectedDimensionLine.getYEnd() + (float)(distanceFromDimensionLineStartToDimensionLineEnd 
              * Math.sin(dimensionLineStartToDimensionLineEndAngle));

          if (this.magnetismEnabled) {
            PointWithMagnetism point = new PointWithMagnetism(
                this.selectedDimensionLine.getXEnd(), 
                this.selectedDimensionLine.getYEnd(), xNewStartPoint, yNewStartPoint,
                preferences.getUnit(), planView.getPixelLength());
            xNewStartPoint = point.getXMagnetizedPoint();
            yNewStartPoint = point.getYMagnetizedPoint();
          } 

          moveDimensionLinePoint(this.selectedDimensionLine, xNewStartPoint, yNewStartPoint, this.startPoint);        
          planView.setDimensionLineAlignmentFeeback(this.selectedDimensionLine, 
              xNewStartPoint, yNewStartPoint);
        } else {
          planView.deleteDimensionLineAlignmentFeeback();
        }        
      } else {
        // Compute the new end point of the dimension line knowing that the distance 
        // from resize point to dimension line base is constant, 
        // and that the start point of the dimension line doesn't move
        double distanceFromResizePointToDimensionLineStart = Point2D.distance(xResizePoint, yResizePoint, 
            this.selectedDimensionLine.getXStart(), this.selectedDimensionLine.getYStart());
        double distanceFromDimensionLineStartToDimensionLineEnd = Math.sqrt(
            distanceFromResizePointToDimensionLineStart * distanceFromResizePointToDimensionLineStart
            - this.distanceFromResizePointToDimensionBaseLine * this.distanceFromResizePointToDimensionBaseLine);
        if (distanceFromDimensionLineStartToDimensionLineEnd > 0) {
          double dimensionLineRelativeAngle = Math.atan2(this.distanceFromResizePointToDimensionBaseLine, 
              distanceFromDimensionLineStartToDimensionLineEnd);
          if (this.selectedDimensionLine.getOffset() >= 0) {
            dimensionLineRelativeAngle = -dimensionLineRelativeAngle;
          }
          double resizePointToDimensionLineStartAngle = Math.atan2(yResizePoint - this.selectedDimensionLine.getYStart(), 
              xResizePoint - this.selectedDimensionLine.getXStart());
          double dimensionLineStartToDimensionLineEndAngle = dimensionLineRelativeAngle + resizePointToDimensionLineStartAngle;
          float xNewEndPoint = this.selectedDimensionLine.getXStart() + (float)(distanceFromDimensionLineStartToDimensionLineEnd 
              * Math.cos(dimensionLineStartToDimensionLineEndAngle));
          float yNewEndPoint = this.selectedDimensionLine.getYStart() + (float)(distanceFromDimensionLineStartToDimensionLineEnd 
              * Math.sin(dimensionLineStartToDimensionLineEndAngle));

          if (this.magnetismEnabled) {
            PointWithMagnetism point = new PointWithMagnetism(
                this.selectedDimensionLine.getXStart(), 
                this.selectedDimensionLine.getYStart(), xNewEndPoint, yNewEndPoint,
                preferences.getUnit(), planView.getPixelLength());
            xNewEndPoint = point.getXMagnetizedPoint();
            yNewEndPoint = point.getYMagnetizedPoint();
          } 

          moveDimensionLinePoint(this.selectedDimensionLine, xNewEndPoint, yNewEndPoint, this.startPoint);
          planView.setDimensionLineAlignmentFeeback(this.selectedDimensionLine, 
              xNewEndPoint, yNewEndPoint);
        } else {
          planView.deleteDimensionLineAlignmentFeeback();
        }
      }     

      // Ensure point at (x,y) is visible
      ((PlanComponent)getView()).makePointVisible(x, y);
    }

    @Override
    public void releaseMouse(float x, float y) {
      postDimensionLineResize(this.selectedDimensionLine, this.oldX, this.oldY, this.startPoint);
      setState(getSelectionState());
    }

    @Override
    public void toggleMagnetism(boolean magnetismToggled) {
      // Compute active magnetism
      this.magnetismEnabled = preferences.isMagnetismEnabled()
                              ^ magnetismToggled;
      moveMouse(getXLastMouseMove(), getYLastMouseMove());
    }

    @Override
    public void escape() {
      moveDimensionLinePoint(this.selectedDimensionLine, this.oldX, this.oldY, this.startPoint);
      setState(getSelectionState());
    }

    @Override
    public void exit() {
      PlanComponent planView = (PlanComponent)getView();
      planView.deleteDimensionLineAlignmentFeeback();
      planView.setResizeIndicatorVisible(false);
      this.selectedDimensionLine = null;
    }  
  }

  /**
   * Dimension line offset state. This state manages dimension line offset. 
   */
  private class DimensionLineOffsetState extends ControllerState {
    private DimensionLine selectedDimensionLine;
    private float         oldOffset;
    private float         deltaXToOffsetPoint;
    private float         deltaYToOffsetPoint;
    
    @Override
    public Mode getMode() {
      return Mode.SELECTION;
    }
    
    @Override
    public void enter() {
      this.selectedDimensionLine = (DimensionLine)home.getSelectedItems().get(0);
      this.oldOffset = this.selectedDimensionLine.getOffset();
      double angle = Math.atan2(this.selectedDimensionLine.getYEnd() - this.selectedDimensionLine.getYStart(), 
          this.selectedDimensionLine.getXEnd() - this.selectedDimensionLine.getXStart());
      float dx = (float)-Math.sin(angle) * this.oldOffset;
      float dy = (float)Math.cos(angle) * this.oldOffset;
      float xMiddle = (this.selectedDimensionLine.getXStart() + this.selectedDimensionLine.getXEnd()) / 2 + dx; 
      float yMiddle = (this.selectedDimensionLine.getYStart() + this.selectedDimensionLine.getYEnd()) / 2 + dy;
      this.deltaXToOffsetPoint = getXLastMousePress() - xMiddle;
      this.deltaYToOffsetPoint = getYLastMousePress() - yMiddle;
      PlanComponent planView = (PlanComponent)getView();
      planView.setResizeIndicatorVisible(true);
    }
    
    @Override
    public void moveMouse(float x, float y) {
      float newX = x - this.deltaXToOffsetPoint;
      float newY = y - this.deltaYToOffsetPoint;

      float distanceToDimensionLine = 
          (float)Line2D.ptLineDist(this.selectedDimensionLine.getXStart(), this.selectedDimensionLine.getYStart(), 
              this.selectedDimensionLine.getXEnd(), this.selectedDimensionLine.getYEnd(), newX, newY);
      int relativeCCW = Line2D.relativeCCW(this.selectedDimensionLine.getXStart(), this.selectedDimensionLine.getYStart(), 
          this.selectedDimensionLine.getXEnd(), this.selectedDimensionLine.getYEnd(), newX, newY);
      home.setDimensionLineOffset(this.selectedDimensionLine, 
           -Math.signum(relativeCCW) * distanceToDimensionLine);

      // Ensure point at (x,y) is visible
      ((PlanComponent)getView()).makePointVisible(x, y);
    }

    @Override
    public void releaseMouse(float x, float y) {
      postDimensionLineOffset(this.selectedDimensionLine, this.oldOffset);
      setState(getSelectionState());
    }

    @Override
    public void escape() {
      home.setDimensionLineOffset(this.selectedDimensionLine, this.oldOffset);
      setState(getSelectionState());
    }

    @Override
    public void exit() {
      ((PlanComponent)getView()).setResizeIndicatorVisible(false);
      this.selectedDimensionLine = null;
    }  
  }
}
