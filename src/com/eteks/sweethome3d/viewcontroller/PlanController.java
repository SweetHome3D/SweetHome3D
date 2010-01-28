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
package com.eteks.sweethome3d.viewcontroller;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.Camera;
import com.eteks.sweethome3d.model.CollectionEvent;
import com.eteks.sweethome3d.model.CollectionListener;
import com.eteks.sweethome3d.model.DimensionLine;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeDoorOrWindow;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.Label;
import com.eteks.sweethome3d.model.LengthUnit;
import com.eteks.sweethome3d.model.ObserverCamera;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.SelectionEvent;
import com.eteks.sweethome3d.model.SelectionListener;
import com.eteks.sweethome3d.model.TextStyle;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;

/**
 * A MVC controller for the plan view.
 * @author Emmanuel Puybaret
 */
public class PlanController extends FurnitureController implements Controller {
  public enum Property {MODE, MODIFICATION_STATE, SCALE}

  /**
   * Selectable modes in controller.
   */
  public static class Mode {
    // Don't qualify Mode as an enumeration to be able to extend Mode class
    public static final Mode SELECTION = new Mode("SELECTION"); 
    public static final Mode WALL_CREATION = new Mode("WALL_CREATION");
    public static final Mode ROOM_CREATION = new Mode("ROOM_CREATION");
    public static final Mode DIMENSION_LINE_CREATION = new Mode("DIMENSION_LINE_CREATION"); 
    public static final Mode LABEL_CREATION = new Mode("LABEL_CREATION");
    
    private final String name;
    
    protected Mode(String name) {
      this.name = name;      
    }
    
    public final String name() {
      return this.name;
    }
    
    @Override
    public String toString() {
      return this.name;
    }
  };

  /**
   * Fields that can be edited in plan view.
   */
  public static enum EditableProperty {X, Y, LENGTH, ANGLE, THICKNESS, OFFSET}

  private static final String SCALE_VISUAL_PROPERTY = "com.eteks.sweethome3d.SweetHome3D.PlanScale";
  
  private static final int PIXEL_MARGIN = 3;
  private static final int PIXEL_WALL_MARGIN = 2;

  private final Home                  home;
  private final UserPreferences       preferences;
  private final ViewFactory           viewFactory;
  private final ContentManager        contentManager;
  private final UndoableEditSupport   undoSupport;
  private final PropertyChangeSupport propertyChangeSupport;
  private PlanView                    planView;
  private SelectionListener           selectionListener;
  // Possibles states
  private final ControllerState       selectionState;
  private final ControllerState       rectangleSelectionState;
  private final ControllerState       selectionMoveState;
  private final ControllerState       dragAndDropState;
  private final ControllerState       wallCreationState;
  private final ControllerState       wallDrawingState;
  private final ControllerState       wallResizeState;
  private final ControllerState       pieceOfFurnitureRotationState;
  private final ControllerState       pieceOfFurnitureElevationState;
  private final ControllerState       pieceOfFurnitureHeightState;
  private final ControllerState       pieceOfFurnitureResizeState;
  private final ControllerState       pieceOfFurnitureNameOffsetState;
  private final ControllerState       cameraYawRotationState;
  private final ControllerState       cameraPitchRotationState;
  private final ControllerState       cameraElevationState;
  private final ControllerState       dimensionLineCreationState;
  private final ControllerState       dimensionLineDrawingState;
  private final ControllerState       dimensionLineResizeState;
  private final ControllerState       dimensionLineOffsetState;
  private final ControllerState       roomCreationState;
  private final ControllerState       roomDrawingState;
  private final ControllerState       roomResizeState;
  private final ControllerState       roomAreaOffsetState;
  private final ControllerState       roomNameOffsetState;
  private final ControllerState       labelCreationState;
  // Current state
  private ControllerState             state;
  private ControllerState             previousState;
  // Mouse cursor position at last mouse press
  private float                       xLastMousePress;
  private float                       yLastMousePress;
  private boolean                     shiftDownLastMousePress;
  private boolean                     duplicationActivatedLastMousePress;
  private float                       xLastMouseMove;
  private float                       yLastMouseMove;
  private Area                        wallsAreaCache;
  private Area                        insideWallsAreaCache;
  private List<GeneralPath>           roomPathsCache;
  private List<Selectable>            draggedItems;

  /**
   * Creates the controller of plan view. 
   * @param home        the home plan edited by this controller and its view
   * @param preferences the preferences of the application
   * @param viewFactory a factory able to create the plan view managed by this controller
   * @param contentManager a content manager used to import furniture
   * @param undoSupport undo support to post changes on plan by this controller
   */
  public PlanController(Home home, 
                        UserPreferences preferences, 
                        ViewFactory viewFactory,
                        ContentManager contentManager,
                        UndoableEditSupport undoSupport) {
    super(home, preferences, viewFactory, contentManager, undoSupport);
    this.home = home;
    this.preferences = preferences;
    this.viewFactory = viewFactory;
    this.contentManager = contentManager;
    this.undoSupport = undoSupport;
    this.propertyChangeSupport = new PropertyChangeSupport(this);
    // Initialize states
    this.selectionState = new SelectionState();
    this.selectionMoveState = new SelectionMoveState();
    this.rectangleSelectionState = new RectangleSelectionState();
    this.dragAndDropState = new DragAndDropState();
    this.wallCreationState = new WallCreationState();
    this.wallDrawingState = new WallDrawingState();
    this.wallResizeState = new WallResizeState();
    this.pieceOfFurnitureRotationState = new PieceOfFurnitureRotationState();
    this.pieceOfFurnitureElevationState = new PieceOfFurnitureElevationState();
    this.pieceOfFurnitureHeightState = new PieceOfFurnitureHeightState();
    this.pieceOfFurnitureResizeState = new PieceOfFurnitureResizeState();
    this.pieceOfFurnitureNameOffsetState = new PieceOfFurnitureNameOffsetState();
    this.cameraYawRotationState = new CameraYawRotationState();
    this.cameraPitchRotationState = new CameraPitchRotationState();
    this.cameraElevationState = new CameraElevationState();
    this.dimensionLineCreationState = new DimensionLineCreationState();
    this.dimensionLineDrawingState = new DimensionLineDrawingState();
    this.dimensionLineResizeState = new DimensionLineResizeState();
    this.dimensionLineOffsetState = new DimensionLineOffsetState();
    this.roomCreationState = new RoomCreationState();
    this.roomDrawingState = new RoomDrawingState();
    this.roomResizeState = new RoomResizeState();
    this.roomAreaOffsetState = new RoomAreaOffsetState();
    this.roomNameOffsetState = new RoomNameOffsetState();
    this.labelCreationState = new LabelCreationState();
    // Set default state to selectionState
    setState(this.selectionState);
    
    addModelListeners();
    
    // Restore previous scale if it exists
    Float scale = (Float)home.getVisualProperty(SCALE_VISUAL_PROPERTY);
    if (scale != null) {
      setScale(scale);
    }
  }

  /**
   * Returns the view associated with this controller.
   */
  public PlanView getView() {
    // Create view lazily only once it's needed
    if (this.planView == null) {
      this.planView = this.viewFactory.createPlanView(this.home, this.preferences, this);
    }
    return this.planView;
  }

  /**
   * Changes current state of controller.
   */
  protected void setState(ControllerState state) {
    boolean oldModificationState = false;
    if (this.state != null) {
      this.state.exit();
      oldModificationState = this.state.isModificationState();
    }
    
    this.previousState = this.state;
    this.state = state;
    if (oldModificationState != state.isModificationState()) {
      this.propertyChangeSupport.firePropertyChange(Property.MODIFICATION_STATE.name(), 
          oldModificationState, !oldModificationState);
    }
    
    this.state.enter();
  }
  
  /**
   * Adds the property change <code>listener</code> in parameter to this controller.
   */
  public void addPropertyChangeListener(Property property, PropertyChangeListener listener) {
    this.propertyChangeSupport.addPropertyChangeListener(property.name(), listener);
  }

  /**
   * Removes the property change <code>listener</code> in parameter from this controller.
   */
  public void removePropertyChangeListener(Property property, PropertyChangeListener listener) {
    this.propertyChangeSupport.removePropertyChangeListener(property.name(), listener);
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
      this.propertyChangeSupport.firePropertyChange(Property.MODE.name(), oldMode, mode);
    }
  }

  /**
   * Returns <code>true</code> if the interactions in the current mode may modify 
   * the state of a home. 
   */
  public boolean isModificationState() {
    return this.state.isModificationState();
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
   * Activates or deactivates duplication feature. 
   * @param duplicationActivated if <code>true</code> then duplication is active.
   */
  public void setDuplicationActivated(boolean duplicationActivated) {
    this.state.setDuplicationActivated(duplicationActivated);
  }

  /**
   * Activates or deactivates edition.
   * @param editionActivated if <code>true</code> then edition is active 
   */
  public void setEditionActivated(boolean editionActivated) {    
    this.state.setEditionActivated(editionActivated);
  }  

  /**
   * Updates an editable property with the entered <code>value</code>.
   */
  public void updateEditableProperty(EditableProperty editableProperty, Object value) {
    this.state.updateEditableProperty(editableProperty, value);
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
   * Returns the drag and drop state.
   */
  protected ControllerState getDragAndDropState() {
    return this.dragAndDropState;
  }

  /**
   * Returns the wall creation state.
   */
  protected ControllerState getWallCreationState() {
    return this.wallCreationState;
  }

  /**
   * Returns the wall drawing state.
   */
  protected ControllerState getWallDrawingState() {
    return this.wallDrawingState;
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
   * Returns the piece name offset state.
   */
  protected ControllerState getPieceOfFurnitureNameOffsetState() {
    return this.pieceOfFurnitureNameOffsetState;
  }
  
  /**
   * Returns the camera yaw rotation state.
   */
  protected ControllerState getCameraYawRotationState() {
    return this.cameraYawRotationState;
  }

  /**
   * Returns the camera pitch rotation state.
   */
  protected ControllerState getCameraPitchRotationState() {
    return this.cameraPitchRotationState;
  }

  /**
   * Returns the camera elevation state.
   */
  protected ControllerState getCameraElevationState() {
    return this.cameraElevationState;
  }

  /**
   * Returns the dimension line creation state.
   */
  protected ControllerState getDimensionLineCreationState() {
    return this.dimensionLineCreationState;
  }

  /**
   * Returns the dimension line drawing state.
   */
  protected ControllerState getDimensionLineDrawingState() {
    return this.dimensionLineDrawingState;
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
   * Returns the room creation state.
   */
  protected ControllerState getRoomCreationState() {
    return this.roomCreationState;
  }

  /**
   * Returns the room drawing state.
   */
  protected ControllerState getRoomDrawingState() {
    return this.roomDrawingState;
  }
  
  /**
   * Returns the room resize state.
   */
  protected ControllerState getRoomResizeState() {
    return this.roomResizeState;
  }
  
  /**
   * Returns the room area offset state.
   */
  protected ControllerState getRoomAreaOffsetState() {
    return this.roomAreaOffsetState;
  }
  
  /**
   * Returns the room name offset state.
   */
  protected ControllerState getRoomNameOffsetState() {
    return this.roomNameOffsetState;
  }
  
  /**
   * Returns the label creation state.
   */
  protected ControllerState getLabelCreationState() {
    return this.labelCreationState;
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
      new WallController(this.home, this.preferences, this.viewFactory,
          this.contentManager, this.undoSupport).displayView(getView());
    }
  }
  
  /**
   * Locks home base plan.
   */
  public void lockBasePlan() {
    if (!this.home.isBasePlanLocked()) {
      List<Selectable> selection = this.home.getSelectedItems();
      final Selectable [] oldSelectedItems = 
          selection.toArray(new Selectable [selection.size()]);
      
      List<Selectable> newSelection = getItemsNotPartOfBasePlan(selection);
      final Selectable [] newSelectedItems = 
        newSelection.toArray(new Selectable [newSelection.size()]);
      
      this.home.setBasePlanLocked(true);
      selectItems(newSelection);
      UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
        @Override
        public void undo() throws CannotUndoException {
          super.undo();
          home.setBasePlanLocked(false);
          selectAndShowItems(Arrays.asList(oldSelectedItems));
        }
        
        @Override
        public void redo() throws CannotRedoException {
          super.redo();
          home.setBasePlanLocked(true);
          selectAndShowItems(Arrays.asList(newSelectedItems));
        }      
    
        @Override
        public String getPresentationName() {
          return preferences.getLocalizedString(
              PlanController.class, "undoLockBasePlan");
        }      
      };
      this.undoSupport.postEdit(undoableEdit);
    }
  }

  /**
   * Returns <code>true</code> it the given <code>item</code> belongs
   * to the base plan.
   */
  protected boolean isItemPartOfBasePlan(Selectable item) {
    if (item instanceof HomePieceOfFurniture) {
      return isPieceOfFurniturePartOfBasePlan((HomePieceOfFurniture)item);
    } else {
      return !(item instanceof ObserverCamera);
    }
  }

  /**
   * Returns the items among the given list that are not part of the base plan.
   */
  private List<Selectable> getItemsNotPartOfBasePlan(List<? extends Selectable> items) {
    List<Selectable> itemsNotPartOfBasePlan = new ArrayList<Selectable>();
    for (Selectable item : items) {
      if (!isItemPartOfBasePlan(item)) {
        itemsNotPartOfBasePlan.add(item);
      }
    }
    return itemsNotPartOfBasePlan;
  }

  /**
   * Unlocks home base plan.
   */
  public void unlockBasePlan() {
    if (this.home.isBasePlanLocked()) {
      List<Selectable> selection = this.home.getSelectedItems();
      final Selectable [] selectedItems = 
          selection.toArray(new Selectable [selection.size()]);
      
      this.home.setBasePlanLocked(false);
      UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
        @Override
        public void undo() throws CannotUndoException {
          super.undo();
          home.setBasePlanLocked(true);
          selectAndShowItems(Arrays.asList(selectedItems));
        }
        
        @Override
        public void redo() throws CannotRedoException {
          super.redo();
          home.setBasePlanLocked(false);
          selectAndShowItems(Arrays.asList(selectedItems));
        }      
    
        @Override
        public String getPresentationName() {
          return preferences.getLocalizedString(
              PlanController.class, "undoUnlockBasePlan");
        }      
      };
      this.undoSupport.postEdit(undoableEdit);
    }
  }

  /**
   * Returns <code>true</code> if the given <code>item</code> may be moved
   * in the plan. Default implementation returns <code>true</code>. 
   */
  protected boolean isItemMovable(Selectable item) {
    if (item instanceof HomePieceOfFurniture) {
      return isPieceOfFurnitureMovable((HomePieceOfFurniture)item);
    } else {
      return true;
    }
  }
  
  /**
   * Returns <code>true</code> if the given <code>item</code> may be resized.
   * Default implementation returns <code>false</code> if the given <code>item</code>
   * is a non resizable piece of furniture.
   */
  protected boolean isItemResizable(Selectable item) {
    if (item instanceof HomePieceOfFurniture) {
      return ((HomePieceOfFurniture)item).isResizable();
    } else {
      return true;
    }
  }
  
  /**
   * Returns <code>true</code> if the given <code>item</code> may be deleted.
   * Default implementation returns <code>true</code>. 
   */
  protected boolean isItemDeletable(Selectable item) {
    if (item instanceof HomePieceOfFurniture) {
      return isPieceOfFurnitureDeletable((HomePieceOfFurniture)item);
    } else {
      return true;
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
                                                 List<Selectable> oldSelection) {
    final Selectable [] oldSelectedItems = 
        oldSelection.toArray(new Selectable [oldSelection.size()]);
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
        return preferences.getLocalizedString(
            PlanController.class, "undoReverseWallsDirectionName");
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
      wall.setXStart(xEnd);
      wall.setYStart(yEnd);
      wall.setXEnd(xStart);
      wall.setYEnd(yStart);

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
      
      wall.setWallAtStart(wallAtEnd);
      wall.setWallAtEnd(wallAtStart);
      
      if (joinedAtEndOfWallAtStart) {
        wallAtStart.setWallAtEnd(wall);
      } else if (joinedAtStartOfWallAtStart) {
        wallAtStart.setWallAtStart(wall);
      }
      
      if (joinedAtEndOfWallAtEnd) {
        wallAtEnd.setWallAtEnd(wall);
      } else if (joinedAtStartOfWallAtEnd) {
        wallAtEnd.setWallAtStart(wall);
      }
      
      Integer rightSideColor = wall.getRightSideColor();
      HomeTexture rightSideTexture = wall.getRightSideTexture();
      Integer leftSideColor = wall.getLeftSideColor();
      HomeTexture leftSideTexture = wall.getLeftSideTexture();      
      wall.setLeftSideColor(rightSideColor);
      wall.setLeftSideTexture(rightSideTexture);
      wall.setRightSideColor(leftSideColor);
      wall.setRightSideTexture(leftSideTexture);
      
      Float heightAtEnd = wall.getHeightAtEnd();
      if (heightAtEnd != null) {
        Float height = wall.getHeight();
        wall.setHeight(heightAtEnd);
        wall.setHeightAtEnd(height);
      }
    }
  }
  
  /**
   * Controls the split of the selected wall in two joined walls of equal length.
   */
  public void splitSelectedWall() {
    List<Selectable> selectedItems = this.home.getSelectedItems();
    List<Wall> selectedWalls = Home.getWallsSubList(selectedItems);
    if (selectedWalls.size() == 1) {
      boolean basePlanLocked = this.home.isBasePlanLocked();
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

      // Clone new walls to copy their characteristics 
      Wall firstWall = splitWall.clone();
      this.home.addWall(firstWall);
      Wall secondWall = splitWall.clone();
      this.home.addWall(secondWall);
      
      // Change split walls end and start point
      firstWall.setXEnd(xMiddle);
      firstWall.setYEnd(yMiddle);
      secondWall.setXStart(xMiddle);
      secondWall.setYStart(yMiddle);
      if (splitWall.getHeightAtEnd() != null) {
        Float heightAtMiddle = (splitWall.getHeight() + splitWall.getHeightAtEnd()) / 2;
        firstWall.setHeightAtEnd(heightAtMiddle);
        secondWall.setHeight(heightAtMiddle);
      } 
            
      firstWall.setWallAtEnd(secondWall);
      secondWall.setWallAtStart(firstWall);
      
      firstWall.setWallAtStart(wallAtStart);
      if (joinedAtEndOfWallAtStart) {
        wallAtStart.setWallAtEnd(firstWall);
      } else if (joinedAtStartOfWallAtStart) {
        wallAtStart.setWallAtStart(firstWall);
      }
      
      secondWall.setWallAtEnd(wallAtEnd);
      if (joinedAtEndOfWallAtEnd) {
        wallAtEnd.setWallAtEnd(secondWall);
      } else if (joinedAtStartOfWallAtEnd) {
        wallAtEnd.setWallAtStart(secondWall);
      }
      
      // Delete split wall
      this.home.deleteWall(splitWall);
      selectAndShowItems(Arrays.asList(new Wall [] {firstWall}));
      
      postSplitSelectedWall(splitJoinedWall, 
          new JoinedWall(firstWall), new JoinedWall(secondWall), selectedItems, basePlanLocked);
    }
  }
  
  /**
   * Posts an undoable split wall operation.
   */
  private void postSplitSelectedWall(final JoinedWall splitJoinedWall, 
                                     final JoinedWall firstJoinedWall, 
                                     final JoinedWall secondJoinedWall,
                                     List<Selectable> oldSelection,
                                     final boolean oldBasePlanLocked) {
    final Selectable [] oldSelectedItems = 
        oldSelection.toArray(new Selectable [oldSelection.size()]);
    final boolean newBasePlanLocked = this.home.isBasePlanLocked();
    UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
      @Override
      public void undo() throws CannotUndoException {
        super.undo();
        doDeleteWalls(new JoinedWall [] {firstJoinedWall, secondJoinedWall}, oldBasePlanLocked);
        doAddWalls(new JoinedWall [] {splitJoinedWall}, oldBasePlanLocked);
        selectAndShowItems(Arrays.asList(oldSelectedItems));
      }
      
      @Override
      public void redo() throws CannotRedoException {
        super.redo();
        doDeleteWalls(new JoinedWall [] {splitJoinedWall}, newBasePlanLocked);
        doAddWalls(new JoinedWall [] {firstJoinedWall, secondJoinedWall}, newBasePlanLocked);
        selectAndShowItems(Arrays.asList(new Wall [] {firstJoinedWall.getWall()}));
      }      

      @Override
      public String getPresentationName() {
        return preferences.getLocalizedString(
            PlanController.class, "undoSplitWallName");
      }      
    };
    this.undoSupport.postEdit(undoableEdit);
  }

  /**
   * Controls the modification of the selected rooms.
   */
  public void modifySelectedRooms() {
    if (!Home.getRoomsSubList(this.home.getSelectedItems()).isEmpty()) {
      new RoomController(this.home, this.preferences, this.viewFactory,
          this.contentManager, this.undoSupport).displayView(getView());
    }
  }
  
  /**
   * Controls the creation of new labels.
   */
  private void createLabel(float x, float y) {
    new LabelController(this.home, x, y, this.preferences, this.viewFactory,
        this.undoSupport).displayView(getView());
  }
  
  /**
   * Controls the modification of the selected labels.
   */
  public void modifySelectedLabels() {
    if (!Home.getLabelsSubList(this.home.getSelectedItems()).isEmpty()) {
      new LabelController(this.home, this.preferences, this.viewFactory,
          this.undoSupport).displayView(getView());
    }
  }
  
  /**
   * Toggles bold style of texts in selected items.
   */
  public void toggleBoldStyle() {
    // Find if selected items are all bold or not
    Boolean selectionBoldStyle = null;
    for (Selectable item : this.home.getSelectedItems()) {
      Boolean bold;
      if (item instanceof Label) {
        bold = getItemTextStyle(item, ((Label)item).getStyle()).isBold();
      } else if (item instanceof HomePieceOfFurniture
          && ((HomePieceOfFurniture)item).isVisible()) {
        bold = getItemTextStyle(item, ((HomePieceOfFurniture)item).getNameStyle()).isBold();
      } else if (item instanceof Room) {
        Room room = (Room)item;
        bold = getItemTextStyle(room, room.getNameStyle()).isBold();
        if (bold != getItemTextStyle(room, room.getAreaStyle()).isBold()) {
          bold = null;
        }
      } else if (item instanceof DimensionLine) {
        bold = getItemTextStyle(item, ((DimensionLine)item).getLengthStyle()).isBold();
      } else {
        continue;
      }
      if (selectionBoldStyle == null) {
        selectionBoldStyle = bold;
      } else if (bold == null || !selectionBoldStyle.equals(bold)) {
        selectionBoldStyle = null;
        break;
      }
    }
    
    // Apply new bold style to all selected items
    if (selectionBoldStyle == null) {
      selectionBoldStyle = Boolean.TRUE;
    } else {
      selectionBoldStyle = !selectionBoldStyle;
    }

    List<Selectable> itemsWithText = new ArrayList<Selectable>(); 
    List<TextStyle> oldTextStyles = new ArrayList<TextStyle>(); 
    List<TextStyle> textStyles = new ArrayList<TextStyle>(); 
    for (Selectable item : this.home.getSelectedItems()) {
      if (item instanceof Label) {
        Label label = (Label)item;
        itemsWithText.add(label);
        TextStyle oldTextStyle = getItemTextStyle(label, label.getStyle());
        oldTextStyles.add(oldTextStyle);
        textStyles.add(oldTextStyle.deriveBoldStyle(selectionBoldStyle));
      } else if (item instanceof HomePieceOfFurniture) {
        HomePieceOfFurniture piece = (HomePieceOfFurniture)item;
        if (piece.isVisible()) {
          itemsWithText.add(piece);
          TextStyle oldNameStyle = getItemTextStyle(piece, piece.getNameStyle());
          oldTextStyles.add(oldNameStyle);
          textStyles.add(oldNameStyle.deriveBoldStyle(selectionBoldStyle));
        }
      } else if (item instanceof Room) {
        final Room room = (Room)item;
        itemsWithText.add(room);
        TextStyle oldNameStyle = getItemTextStyle(room, room.getNameStyle());
        oldTextStyles.add(oldNameStyle);
        textStyles.add(oldNameStyle.deriveBoldStyle(selectionBoldStyle));
        TextStyle oldAreaStyle = getItemTextStyle(room, room.getAreaStyle());
        oldTextStyles.add(oldAreaStyle);
        textStyles.add(oldAreaStyle.deriveBoldStyle(selectionBoldStyle));
      } else if (item instanceof DimensionLine) {
        DimensionLine dimensionLine = (DimensionLine)item;
        itemsWithText.add(dimensionLine);
        TextStyle oldLengthStyle = getItemTextStyle(dimensionLine, dimensionLine.getLengthStyle());
        oldTextStyles.add(oldLengthStyle);
        textStyles.add(oldLengthStyle.deriveBoldStyle(selectionBoldStyle));
      } 
    }
    modifyTextStyle(itemsWithText.toArray(new Selectable [itemsWithText.size()]),
        oldTextStyles.toArray(new TextStyle [oldTextStyles.size()]),
        textStyles.toArray(new TextStyle [textStyles.size()]));
  }
  
  /**
   * Returns <code>textStyle</code> if not null or the default text style.
   */
  private TextStyle getItemTextStyle(Selectable item, TextStyle textStyle) {
    if (textStyle == null) {
      textStyle = this.preferences.getDefaultTextStyle(item.getClass());              
    }          
    return textStyle;
  }
  
  /**
   * Toggles italic style of texts in selected items.
   */
  public void toggleItalicStyle() {
    // Find if selected items are all italic or not
    Boolean selectionItalicStyle = null;
    for (Selectable item : this.home.getSelectedItems()) {
      Boolean italic;
      if (item instanceof Label) {
        italic = getItemTextStyle(item, ((Label)item).getStyle()).isItalic();
      } else if (item instanceof HomePieceOfFurniture
          && ((HomePieceOfFurniture)item).isVisible()) {
        italic = getItemTextStyle(item, ((HomePieceOfFurniture)item).getNameStyle()).isItalic();
      } else if (item instanceof Room) {
        Room room = (Room)item;
        italic = getItemTextStyle(room, room.getNameStyle()).isItalic();
        if (italic != getItemTextStyle(room, room.getAreaStyle()).isItalic()) {
          italic = null;
        }
      } else if (item instanceof DimensionLine) {
        italic = getItemTextStyle(item, ((DimensionLine)item).getLengthStyle()).isItalic();
      } else {
        continue;
      }
      if (selectionItalicStyle == null) {
        selectionItalicStyle = italic;
      } else if (italic == null || !selectionItalicStyle.equals(italic)) {
        selectionItalicStyle = null;
        break;
      }
    }
    
    // Apply new italic style to all selected items
    if (selectionItalicStyle == null) {
      selectionItalicStyle = Boolean.TRUE;
    } else {
      selectionItalicStyle = !selectionItalicStyle; 
    }
    
    List<Selectable> itemsWithText = new ArrayList<Selectable>(); 
    List<TextStyle> oldTextStyles = new ArrayList<TextStyle>(); 
    List<TextStyle> textStyles = new ArrayList<TextStyle>(); 
    for (Selectable item : this.home.getSelectedItems()) {
      if (item instanceof Label) {
        Label label = (Label)item;
        itemsWithText.add(label);
        TextStyle oldTextStyle = getItemTextStyle(label, label.getStyle());
        oldTextStyles.add(oldTextStyle);
        textStyles.add(oldTextStyle.deriveItalicStyle(selectionItalicStyle));
      } else if (item instanceof HomePieceOfFurniture) {
        HomePieceOfFurniture piece = (HomePieceOfFurniture)item;
        if (piece.isVisible()) {
          itemsWithText.add(piece);
          TextStyle oldNameStyle = getItemTextStyle(piece, piece.getNameStyle());
          oldTextStyles.add(oldNameStyle);
          textStyles.add(oldNameStyle.deriveItalicStyle(selectionItalicStyle));
        }
      } else if (item instanceof Room) {
        final Room room = (Room)item;
        itemsWithText.add(room);
        TextStyle oldNameStyle = getItemTextStyle(room, room.getNameStyle());
        oldTextStyles.add(oldNameStyle);
        textStyles.add(oldNameStyle.deriveItalicStyle(selectionItalicStyle));
        TextStyle oldAreaStyle = getItemTextStyle(room, room.getAreaStyle());
        oldTextStyles.add(oldAreaStyle);
        textStyles.add(oldAreaStyle.deriveItalicStyle(selectionItalicStyle));
      } else if (item instanceof DimensionLine) {
        DimensionLine dimensionLine = (DimensionLine)item;
        itemsWithText.add(dimensionLine);
        TextStyle oldLengthStyle = getItemTextStyle(dimensionLine, dimensionLine.getLengthStyle());
        oldTextStyles.add(oldLengthStyle);
        textStyles.add(oldLengthStyle.deriveItalicStyle(selectionItalicStyle));
      } 
    }
    modifyTextStyle(itemsWithText.toArray(new Selectable [itemsWithText.size()]),
        oldTextStyles.toArray(new TextStyle [oldTextStyles.size()]),
        textStyles.toArray(new TextStyle [textStyles.size()]));
  }
  
  /**
   * Increase the size of texts in selected items.
   */
  public void increaseTextSize() {
    applyFactorToTextSize(1.1f);
  }
  
  /**
   * Decrease the size of texts in selected items.
   */
  public void decreaseTextSize() {
    applyFactorToTextSize(1 / 1.1f);
  }

  /**
   * Applies a factor to the font size of the texts of the selected items in home.
   */
  private void applyFactorToTextSize(float factor) {
    List<Selectable> itemsWithText = new ArrayList<Selectable>(); 
    List<TextStyle> oldTextStyles = new ArrayList<TextStyle>(); 
    List<TextStyle> textStyles = new ArrayList<TextStyle>(); 
    for (Selectable item : this.home.getSelectedItems()) {
      if (item instanceof Label) {
        Label label = (Label)item;
        itemsWithText.add(label);
        TextStyle oldLabelStyle = getItemTextStyle(item, label.getStyle());
        oldTextStyles.add(oldLabelStyle);
        textStyles.add(oldLabelStyle.deriveStyle(Math.round(oldLabelStyle.getFontSize() * factor)));
      } else if (item instanceof HomePieceOfFurniture) {
        HomePieceOfFurniture piece = (HomePieceOfFurniture)item;
        if (piece.isVisible()) {
          itemsWithText.add(piece);
          TextStyle oldNameStyle = getItemTextStyle(piece, piece.getNameStyle());
          oldTextStyles.add(oldNameStyle);
          textStyles.add(oldNameStyle.deriveStyle(Math.round(oldNameStyle.getFontSize() * factor)));
        }
      } else if (item instanceof Room) {
        final Room room = (Room)item;
        itemsWithText.add(room);
        TextStyle oldNameStyle = getItemTextStyle(room, room.getNameStyle());
        oldTextStyles.add(oldNameStyle);
        textStyles.add(oldNameStyle.deriveStyle(Math.round(oldNameStyle.getFontSize() * factor)));
        TextStyle oldAreaStyle = getItemTextStyle(room, room.getAreaStyle());
        oldTextStyles.add(oldAreaStyle);
        textStyles.add(oldAreaStyle.deriveStyle(Math.round(oldAreaStyle.getFontSize() * factor)));
      } else if (item instanceof DimensionLine) {
        DimensionLine dimensionLine = (DimensionLine)item;
        itemsWithText.add(dimensionLine);
        TextStyle oldLengthStyle = getItemTextStyle(dimensionLine, dimensionLine.getLengthStyle());
        oldTextStyles.add(oldLengthStyle);
        textStyles.add(oldLengthStyle.deriveStyle(Math.round(oldLengthStyle.getFontSize() * factor)));
      } 
    }
    modifyTextStyle(itemsWithText.toArray(new Selectable [itemsWithText.size()]), 
        oldTextStyles.toArray(new TextStyle [oldTextStyles.size()]),
        textStyles.toArray(new TextStyle [textStyles.size()]));
  }
  
  /**
   * Changes the style of items and posts an undoable change style operation.
   */
  private void modifyTextStyle(final Selectable [] items, 
                               final TextStyle [] oldStyles,
                               final TextStyle [] styles) {
    List<Selectable> oldSelection = this.home.getSelectedItems();
    final Selectable [] oldSelectedItems = 
        oldSelection.toArray(new Selectable [oldSelection.size()]);
    
    doModifyTextStyle(items, styles);
    UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
      @Override
      public void undo() throws CannotUndoException {
        super.undo();
        doModifyTextStyle(items, oldStyles);
        selectAndShowItems(Arrays.asList(oldSelectedItems));
      }
      
      @Override
      public void redo() throws CannotRedoException {
        super.redo();
        doModifyTextStyle(items, styles);
        selectAndShowItems(Arrays.asList(oldSelectedItems));
      }      

      @Override
      public String getPresentationName() {
        return preferences.getLocalizedString(
            PlanController.class, "undoModifyTextStyleName");
      }      
    };
    this.undoSupport.postEdit(undoableEdit);
  }
  
  /**
   * Changes the style of items.
   */
  private void doModifyTextStyle(Selectable [] items, TextStyle [] styles) {
    int styleIndex = 0;
    for (Selectable item : items) {
      if (item instanceof Label) {
        ((Label)item).setStyle(styles [styleIndex++]);
      } else if (item instanceof HomePieceOfFurniture) {
        HomePieceOfFurniture piece = (HomePieceOfFurniture)item;
        if (piece.isVisible()) {
          piece.setNameStyle(styles [styleIndex++]);
        }
      } else if (item instanceof Room) {
        final Room room = (Room)item;
        room.setNameStyle(styles [styleIndex++]);
        room.setAreaStyle(styles [styleIndex++]);
      } else if (item instanceof DimensionLine) {
        ((DimensionLine)item).setLengthStyle(styles [styleIndex++]);
      } 
    }
  }

  /**
   * Returns the scale in plan view. 
   */
  public float getScale() {
    return getView().getScale();
  }

  /**
   * Controls the scale in plan view and and fires a <code>PropertyChangeEvent</code>. 
   */
  public void setScale(float scale) {
    if (scale != getView().getScale()) {
      float oldScale = getView().getScale();
      getView().setScale(scale);
      this.propertyChangeSupport.firePropertyChange(Property.SCALE.name(), oldScale, scale);
      this.home.setVisualProperty(SCALE_VISUAL_PROPERTY, scale);
    }
  } 
  
  /**
   * Selects all visible items in home.
   */
  @Override
  public void selectAll() {
    List<Selectable> all = new ArrayList<Selectable>(this.home.getWalls());
    all.addAll(this.home.getRooms());
    all.addAll(this.home.getDimensionLines());
    all.addAll(this.home.getLabels());
    for (HomePieceOfFurniture piece : this.home.getFurniture()) {
      if (piece.isVisible()) {
        all.add(piece);
      }
    }
    if (this.home.isBasePlanLocked()) {
      this.home.setSelectedItems(getItemsNotPartOfBasePlan(all));
    } else {
      this.home.setSelectedItems(all);
    }
  }
  
  /**
   * Returns the horizontal ruler of the plan view. 
   */
  public View getHorizontalRulerView() {
    return getView().getHorizontalRuler();
  }
  
  /**
   * Returns the vertical ruler of the plan view. 
   */
  public View getVerticalRulerView() {
    return getView().getVerticalRuler();
  }
  
  private void addModelListeners() {
    this.selectionListener = new SelectionListener() {
        public void selectionChanged(SelectionEvent ev) {
          getView().makeSelectionVisible();
        }
      };
    this.home.addSelectionListener(this.selectionListener);
    // Ensure observer camera is visible when its location or angles change
    this.home.getObserverCamera().addPropertyChangeListener(new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          if (home.getSelectedItems().contains(ev.getSource())) {
            getView().makeSelectionVisible();
          }
        }
      });
    // Add listener to update roomPathsCache when walls change
    final PropertyChangeListener wallChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          String propertyName = ev.getPropertyName();
          if (Wall.Property.X_START.name().equals(propertyName)
              || Wall.Property.X_END.name().equals(propertyName) 
              || Wall.Property.Y_START.name().equals(propertyName) 
              || Wall.Property.Y_END.name().equals(propertyName)
              || Wall.Property.WALL_AT_START.name().equals(propertyName)
              || Wall.Property.WALL_AT_END.name().equals(propertyName)
              || Wall.Property.THICKNESS.name().equals(propertyName)) {
            wallsAreaCache = null;
            insideWallsAreaCache = null;
            roomPathsCache = null;
          }
        }
      };
    for (Wall wall : home.getWalls()) {
      wall.addPropertyChangeListener(wallChangeListener);
    }
    this.home.addWallsListener(new CollectionListener<Wall> () {
        public void collectionChanged(CollectionEvent<Wall> ev) {
          if (ev.getType() == CollectionEvent.Type.ADD) {
            ev.getItem().addPropertyChangeListener(wallChangeListener);
          } else if (ev.getType() == CollectionEvent.Type.DELETE) {
            ev.getItem().removePropertyChangeListener(wallChangeListener);
          }
          wallsAreaCache = null;
          insideWallsAreaCache = null;
          roomPathsCache = null;
        }
      });
  }

  /**
   * Displays in plan view the feedback of <code>draggedItems</code>,
   * during a drag and drop operation initiated from outside of plan view. 
   */
  public void startDraggedItems(List<Selectable> draggedItems, float x, float y) {
    this.draggedItems = draggedItems;
    // If magnetism is enabled, adjust furniture size and elevation
    if (this.preferences.isMagnetismEnabled()) {
      for (HomePieceOfFurniture piece : Home.getFurnitureSubList(draggedItems)) {
        if (piece.isResizable()) {
          piece.setWidth(this.preferences.getLengthUnit().getMagnetizedLength(piece.getWidth(), 0.1f));
          piece.setDepth(this.preferences.getLengthUnit().getMagnetizedLength(piece.getDepth(), 0.1f));
          piece.setHeight(this.preferences.getLengthUnit().getMagnetizedLength(piece.getHeight(), 0.1f));
        }
        piece.setElevation(this.preferences.getLengthUnit().getMagnetizedLength(piece.getElevation(), 0.1f));
      }
    }
    setState(getDragAndDropState());
    moveMouse(x, y);
  }

  /**
   * Deletes in plan view the feedback of the dragged items. 
   */
  public void stopDraggedItems() {
    if (this.state != getDragAndDropState()) {
      throw new IllegalStateException("Controller isn't in a drag and drop state");
    }
    this.draggedItems = null;    
    setState(this.previousState);
  }
  
  /**
   * Attempts to modify <code>piece</code> location depending of its context.
   * If the <code>piece</code> is a door or a window and the point (<code>x</code>, <code>y</code>)
   * belongs to a wall, the piece will be resized, rotated and moved so 
   * its opening depth is equal to wall thickness and its angle matches wall direction.
   * If the <code>piece</code> isn't a door or a window and the point (<code>x</code>, <code>y</code>)
   * belongs to a wall, the piece will be rotated and moved so 
   * its back face lies along the closest wall side and its angle matches wall direction.
   * If the <code>piece</code> isn't a door or a window, its bounding box is included in 
   * the one of an other object and its elevation is equal to zero, it will be elevated
   * to appear on the top of the latter. 
   */
  protected void adjustMagnetizedPieceOfFurniture(HomePieceOfFurniture piece, float x, float y) {
    adjustPieceOfFurnitureOnWallAt(piece, x, y);
    adjustPieceOfFurnitureElevationAt(piece);
  }
  
  /**
   * Attempts to move and resize <code>piece</code> depending on the wall under the 
   * point (<code>x</code>, <code>y</code>) and returns that wall it it exists.
   * @see #adjustMagnetizedPieceOfFurniture(HomePieceOfFurniture, float, float)
   */
  private Wall adjustPieceOfFurnitureOnWallAt(HomePieceOfFurniture piece, float x, float y) {
    Wall wallAtPoint = null;
    // Search if point (x, y) is contained in home walls with no margin
    for (Wall wall : this.home.getWalls()) {
      if (wall.containsPoint(x, y, 0)
          && wall.getLength() > 0) {
        wallAtPoint = wall;
        break;
      }
    }
    if (wallAtPoint == null) {
      float margin = PIXEL_MARGIN / getScale();
      // If not found search if point (x, y) is contained in home walls with a margin
      for (Wall wall : this.home.getWalls()) {
        if (wall.containsPoint(x, y, margin)
            && wall.getLength() > 0) {
          wallAtPoint = wall;
          break;
        }
      }
    }

    if (wallAtPoint != null) {      
      double wallAngle = Math.atan2(wallAtPoint.getYEnd() - wallAtPoint.getYStart(), 
          wallAtPoint.getXEnd() - wallAtPoint.getXStart());
      boolean magnetizedAtRight = wallAngle > -Math.PI / 2 && wallAngle <= Math.PI / 2; 
      double cosAngle = Math.cos(wallAngle);
      double sinAngle = Math.sin(wallAngle);
      float [][] wallPoints = wallAtPoint.getPoints();
      double distanceToLeftSide = Line2D.ptLineDist(
          wallPoints [0][0], wallPoints [0][1], wallPoints [1][0], wallPoints [1][1], x, y);
      double distanceToRightSide = Line2D.ptLineDist(
          wallPoints [2][0], wallPoints [2][1], wallPoints [3][0], wallPoints [3][1], x, y);
      
      float [][] piecePoints = piece.getPoints();
      float pieceAngle = piece.getAngle();
      double distanceToPieceLeftSide = Line2D.ptLineDist(
          piecePoints [0][0], piecePoints [0][1], piecePoints [3][0], piecePoints [3][1], x, y);
      double distanceToPieceRightSide = Line2D.ptLineDist(
          piecePoints [1][0], piecePoints [1][1], piecePoints [2][0], piecePoints [2][1], x, y);
      double distanceToPieceSide = pieceAngle > (3 * Math.PI / 2 + 1E-6) || pieceAngle < (Math.PI / 2 + 1E-6)
          ? distanceToPieceLeftSide
          : distanceToPieceRightSide;
      
      double angle;
      double xPiece;
      double yPiece;
      float halfWidth = piece.getWidth() / 2;
      final float thicknessEpsilon = 0.0002f;
      if (piece.isDoorOrWindow()) {
        float wallDistance = thicknessEpsilon / 2;
        if (piece instanceof HomeDoorOrWindow) {
          HomeDoorOrWindow doorOrWindow = (HomeDoorOrWindow) piece;
          if (piece.isResizable()
              && isItemResizable(piece)) {
            piece.setDepth(thicknessEpsilon 
                + wallAtPoint.getThickness() / doorOrWindow.getWallThickness());
          }
          wallDistance += piece.getDepth() * doorOrWindow.getWallDistance();           
        } 
        float halfDepth = piece.getDepth() / 2;
        if (distanceToRightSide < distanceToLeftSide) {
          angle = wallAngle;
          xPiece = x + sinAngle * (distanceToLeftSide + wallDistance);
          yPiece = y - cosAngle * (distanceToLeftSide + wallDistance);
          if (magnetizedAtRight) {
            xPiece += cosAngle * (halfWidth - distanceToPieceSide) - sinAngle * halfDepth;
            yPiece += sinAngle * (halfWidth - distanceToPieceSide) + cosAngle * halfDepth;
          } else {
            // Ensure adjusted window is at the right of the cursor 
            xPiece += -cosAngle * (halfWidth - distanceToPieceSide) - sinAngle * halfDepth;
            yPiece += -sinAngle * (halfWidth - distanceToPieceSide) + cosAngle * halfDepth;
          }
        } else {
          angle = wallAngle + Math.PI;
          xPiece = x - sinAngle * (distanceToRightSide + wallDistance);
          yPiece = y + cosAngle * (distanceToRightSide + wallDistance);
          if (magnetizedAtRight) {
            xPiece += cosAngle * (halfWidth - distanceToPieceSide) + sinAngle * halfDepth;
            yPiece += sinAngle * (halfWidth - distanceToPieceSide) - cosAngle * halfDepth;
          } else {
            // Ensure adjusted window is at the right of the cursor 
            xPiece += -cosAngle * (halfWidth - distanceToPieceSide) + sinAngle * halfDepth;
            yPiece += -sinAngle * (halfWidth - distanceToPieceSide) - cosAngle * halfDepth;
          }
        }
      } else {
        float halfDepth = piece.getDepth() / 2;
        if (distanceToRightSide < distanceToLeftSide) {
          angle = wallAngle;
          int pointIndicator = Line2D.relativeCCW(
              wallPoints [2][0], wallPoints [2][1], wallPoints [3][0], wallPoints [3][1], x, y);
          xPiece = x + pointIndicator * sinAngle * distanceToRightSide;
          yPiece = y - pointIndicator * cosAngle * distanceToRightSide;
          if (magnetizedAtRight) {
            xPiece += cosAngle * (halfWidth - distanceToPieceSide) - sinAngle * halfDepth;
            yPiece += sinAngle * (halfWidth - distanceToPieceSide) + cosAngle * halfDepth;
          } else {
            // Ensure adjusted piece is at the right of the cursor 
            xPiece += -cosAngle * (halfWidth - distanceToPieceSide) - sinAngle * halfDepth;
            yPiece += -sinAngle * (halfWidth - distanceToPieceSide) + cosAngle * halfDepth;
          }
        } else {
          angle = wallAngle + Math.PI;
          int pointIndicator = Line2D.relativeCCW(
              wallPoints [0][0], wallPoints [0][1], wallPoints [1][0], wallPoints [1][1], x, y);
          xPiece = x - pointIndicator * sinAngle * distanceToLeftSide;
          yPiece = y + pointIndicator * cosAngle * distanceToLeftSide;
          if (magnetizedAtRight) {
            xPiece += cosAngle * (halfWidth - distanceToPieceSide) + sinAngle * halfDepth;
            yPiece += sinAngle * (halfWidth - distanceToPieceSide) - cosAngle * halfDepth;
          } else {
            // Ensure adjusted piece is at the right of the cursor 
            xPiece += -cosAngle * (halfWidth - distanceToPieceSide) + sinAngle * halfDepth;
            yPiece += -sinAngle * (halfWidth - distanceToPieceSide) - cosAngle * halfDepth;
          }
        }
      }
      piece.setAngle((float)angle);
      piece.setX((float)xPiece);
      piece.setY((float)yPiece);
      if (piece instanceof HomeDoorOrWindow) {
        ((HomeDoorOrWindow)piece).setBoundToWall(true);
      }
    }    
    return wallAtPoint;
  }

  /**
   * Returns the dimension lines that indicates how is placed a given <code>piece</code>
   * along a <code>wall</code>. 
   */
  private List<DimensionLine> getDimensionLinesAlongWall(HomePieceOfFurniture piece, Wall wall) {
    // Search the points on the wall side closest to piece
    float [][] piecePoints = piece.getPoints();
    float [] piecePoint = piece.isDoorOrWindow()
        ? piecePoints [3] // Front side point
        : piecePoints [0]; // Back side point
    float [][] wallPoints = wall.getPoints();
    float [] pieceLeftPoint;
    float [] pieceRightPoint;
    if (Line2D.ptLineDistSq(wallPoints [0][0], wallPoints [0][1], 
            wallPoints [1][0], wallPoints [1][1], 
            piecePoint [0], piecePoint [1]) 
        <= Line2D.ptLineDistSq(wallPoints [2][0], wallPoints [2][1],
            wallPoints [3][0], wallPoints [3][1], 
            piecePoint [0], piecePoint [1])) {
      pieceLeftPoint = computeIntersection(wallPoints [0], wallPoints [1], piecePoints [0], piecePoints [3]);
      pieceRightPoint = computeIntersection(wallPoints [0], wallPoints [1], piecePoints [1], piecePoints [2]);
    } else {
      pieceLeftPoint = computeIntersection(wallPoints [2], wallPoints [3], piecePoints [0], piecePoints [3]);
      pieceRightPoint = computeIntersection(wallPoints [2], wallPoints [3], piecePoints [1], piecePoints [2]);
    }
    
    List<DimensionLine> dimensionLines = new ArrayList<DimensionLine>();
    float [] wallEndPointJoinedToPieceLeftPoint = null;
    float [] wallEndPointJoinedToPieceRightPoint = null;
    // Search among room paths which segment includes pieceLeftPoint and pieceRightPoint
    List<GeneralPath> roomPaths = getRoomPathsFromWalls();
    for (int i = 0; 
         i < roomPaths.size()
         && wallEndPointJoinedToPieceLeftPoint == null 
         && wallEndPointJoinedToPieceRightPoint == null; i++) {
      float [][] roomPoints = getPathPoints(roomPaths.get(i), true);
      for (int j = 0; j < roomPoints.length; j++) {
        float [] startPoint = roomPoints [j];
        float [] endPoint = roomPoints [(j + 1) % roomPoints.length];
        boolean segmentContainsLeftPoint = Line2D.ptSegDistSq(startPoint [0], startPoint [1], 
            endPoint [0], endPoint [1], pieceLeftPoint [0], pieceLeftPoint [1]) < 0.0001;
        boolean segmentContainsRightPoint = Line2D.ptSegDistSq(startPoint [0], startPoint [1], 
            endPoint [0], endPoint [1], pieceRightPoint [0], pieceRightPoint [1]) < 0.0001;
        if (segmentContainsLeftPoint || segmentContainsRightPoint) {
          if (segmentContainsLeftPoint) {
            // Compute distances to segment start point
            double startPointToLeftPointDistance = Point2D.distanceSq(startPoint [0], startPoint [1], 
                pieceLeftPoint [0], pieceLeftPoint [1]);
            double startPointToRightPointDistance = Point2D.distanceSq(startPoint [0], startPoint [1], 
                pieceRightPoint [0], pieceRightPoint [1]);
            if (startPointToLeftPointDistance < startPointToRightPointDistance
                || !segmentContainsRightPoint) {
              wallEndPointJoinedToPieceLeftPoint = startPoint.clone();
            } else {
              wallEndPointJoinedToPieceLeftPoint = endPoint.clone();
            }
          }
          if (segmentContainsRightPoint) {
            // Compute distances to segment start point
            double endPointToLeftPointDistance = Point2D.distanceSq(endPoint [0], endPoint [1], 
                pieceLeftPoint [0], pieceLeftPoint [1]);
            double endPointToRightPointDistance = Point2D.distanceSq(endPoint [0], endPoint [1], 
                pieceRightPoint [0], pieceRightPoint [1]);
            if (endPointToLeftPointDistance < endPointToRightPointDistance
                && segmentContainsLeftPoint) {
              wallEndPointJoinedToPieceRightPoint = startPoint.clone();
            } else {
              wallEndPointJoinedToPieceRightPoint = endPoint.clone();
            }
          }
          break;
        }
      }
    }

    float angle = piece.getAngle();
    boolean reverse = angle > Math.PI / 2 && angle <= 3 * Math.PI / 2;
    if (wallEndPointJoinedToPieceLeftPoint != null) {
      float offset = (float)Point2D.distance(pieceLeftPoint [0], pieceLeftPoint [1], 
          piecePoints [3][0], piecePoints [3][1]) + 10 / getView().getScale();
      if (reverse) {
        dimensionLines.add(new DimensionLine(pieceLeftPoint [0], pieceLeftPoint [1],
            wallEndPointJoinedToPieceLeftPoint [0],
            wallEndPointJoinedToPieceLeftPoint [1], -offset));
      } else {
        dimensionLines.add(new DimensionLine(wallEndPointJoinedToPieceLeftPoint [0],
            wallEndPointJoinedToPieceLeftPoint [1], 
            pieceLeftPoint [0], pieceLeftPoint [1], offset));
      }
    }
    if (wallEndPointJoinedToPieceRightPoint != null) {
      float offset = (float)Point2D.distance(pieceRightPoint [0], pieceRightPoint [1], 
          piecePoints [2][0], piecePoints [2][1]) + 10 / getView().getScale();
      if (reverse) {
        dimensionLines.add(new DimensionLine(wallEndPointJoinedToPieceRightPoint [0],
            wallEndPointJoinedToPieceRightPoint [1], 
            pieceRightPoint [0], pieceRightPoint [1], -offset));
      } else {
        dimensionLines.add(new DimensionLine(pieceRightPoint [0], pieceRightPoint [1],
            wallEndPointJoinedToPieceRightPoint [0],
            wallEndPointJoinedToPieceRightPoint [1], offset));
      }
    }
    return dimensionLines;
  }

  /** 
   * Returns the intersection point between the lines defined by the points
   * (<code>point1</code>, <code>point2</code>) and (<code>point3</code>, <code>pont4</code>).
   */
  private float [] computeIntersection(float [] point1, float [] point2, float [] point3, float [] point4) {
    float x = point2 [0];
    float y = point2 [1];
    float alpha1 = (point2 [1] - point1 [1]) / (point2 [0] - point1 [0]);
    float beta1 = point2 [1] - alpha1 * point2 [0];
    float alpha2 = (point4 [1] - point3 [1]) / (point4 [0] - point3 [0]);
    float beta2 = point4 [1] - alpha2 * point4 [0];

    if (alpha1 != alpha2) {
      // If first line is vertical
      if (Math.abs(alpha1) > 1E5)  {
        if (Math.abs(alpha2) < 1E5) {
          x = point1 [0];
          y = alpha2 * x + beta2;
        }
        // If second line is vertical
      } else if (Math.abs(alpha2) > 1E5) {
        if (Math.abs(alpha1) < 1E5) {
          x = point3 [0];
          y = alpha1 * x + beta1;
        }
      } else {
        x = (beta2 - beta1) / (alpha1 - alpha2);
        y = alpha1 * x + beta1;
      }
    }
    return new float [] {x, y};
  }
  
  /**
   * Attempts to elevate <code>piece</code> depending on the highest piece that includes
   * its bounding box and returns that piece.
   * @see #adjustMagnetizedPieceOfFurniture(HomePieceOfFurniture, float, float)
   */
  private HomePieceOfFurniture adjustPieceOfFurnitureElevationAt(HomePieceOfFurniture piece) {
    // Search if another piece at floor level contains the given piece to elevate it at its height
    if (!piece.isDoorOrWindow()
        && piece.getElevation() == 0) {
      float [][] piecePoints = piece.getPoints();
      HomePieceOfFurniture highestSurroundingPiece = null;
      float highestElevation = Float.MIN_VALUE;
      for (HomePieceOfFurniture homePiece : this.home.getFurniture()) {
        if (homePiece != piece 
            && !homePiece.isDoorOrWindow()
            && homePiece.isVisible()) {
          Shape shape = getPath(homePiece.getPoints());
          boolean surroundingPieceContainsPiece = true;
          for (float [] point : piecePoints) {
            if (!shape.contains(point [0], point [1])) {
              surroundingPieceContainsPiece = false;
              break;
            }
          }
          if (surroundingPieceContainsPiece) {
            float elevation = homePiece.getElevation() + homePiece.getHeight();
            if (elevation > highestElevation) {
              highestElevation = elevation;
              highestSurroundingPiece = homePiece;
            }
          }
        }
      }
      if (highestSurroundingPiece != null) {
        piece.setElevation(highestElevation);
        return highestSurroundingPiece;
      }
    }
    return null;
  }

  /**
   * Returns a new wall instance between (<code>xStart</code>,
   * <code>yStart</code>) and (<code>xEnd</code>, <code>yEnd</code>)
   * end points. The new wall is added to home and its start point is joined 
   * to the start of <code>wallStartAtStart</code> or 
   * the end of <code>wallEndAtStart</code>.
   */
  protected Wall createWall(float xStart, float yStart,
                            float xEnd, float yEnd,
                            Wall wallStartAtStart,
                            Wall wallEndAtStart) {
    // Create a new wall
    Wall newWall = new Wall(xStart, yStart, xEnd, yEnd, 
        this.preferences.getNewWallThickness(),
        this.preferences.getNewWallHeight());
    this.home.addWall(newWall);
    if (wallStartAtStart != null) {
      newWall.setWallAtStart(wallStartAtStart);
      wallStartAtStart.setWallAtStart(newWall);
    } else if (wallEndAtStart != null) {
      newWall.setWallAtStart(wallEndAtStart);
      wallEndAtStart.setWallAtEnd(newWall);
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
      wall.setWallAtEnd(wallStartAtEnd);
      wallStartAtEnd.setWallAtStart(wall);
      // Make wall end at the exact same position as wallAtEnd start point
      wall.setXEnd(wallStartAtEnd.getXStart());
      wall.setYEnd(wallStartAtEnd.getYStart());
    } else if (wallEndAtEnd != null) {
      wall.setWallAtEnd(wallEndAtEnd);
      wallEndAtEnd.setWallAtEnd(wall);
      // Make wall end at the exact same position as wallAtEnd end point
      wall.setXEnd(wallEndAtEnd.getXEnd());
      wall.setYEnd(wallEndAtEnd.getYEnd());
    }
  }
  
  /**
   * Returns the wall at (<code>x</code>, <code>y</code>) point,  
   * which has a start point not joined to any wall. 
   */
  private Wall getWallStartAt(float x, float y, Wall ignoredWall) {
    float margin = PIXEL_WALL_MARGIN / getScale();
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
    float margin = PIXEL_WALL_MARGIN / getScale();
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
    List<Selectable> selectedItems = this.home.getSelectedItems();
    if (selectedItems.size() == 1
        && selectedItems.get(0) instanceof Wall
        && isItemResizable(selectedItems.get(0))) {
      Wall wall = (Wall)selectedItems.get(0);
      float margin = PIXEL_MARGIN / getScale();
      if (wall.containsWallStartAt(x, y, margin)) {
        return wall;
      }
    } 
    return null;
  }
  
  /**
   * Returns the selected wall with an end point at (<code>x</code>, <code>y</code>).
   */
  private Wall getResizedWallEndAt(float x, float y) {
    List<Selectable> selectedItems = this.home.getSelectedItems();
    if (selectedItems.size() == 1
        && selectedItems.get(0) instanceof Wall
        && isItemResizable(selectedItems.get(0))) {
      Wall wall = (Wall)selectedItems.get(0);
      float margin = PIXEL_MARGIN / getScale();
      if (wall.containsWallEndAt(x, y, margin)) {
        return wall;
      }
    } 
    return null;
  }
  
  /**
   * Returns a new room instance with the given points. 
   * The new room is added to home.
   */
  protected Room createRoom(float [][] roomPoints) {
    Room newRoom = new Room(roomPoints);
    this.home.addRoom(newRoom);
    return newRoom;
  }
  
  /**
   * Returns the selected room with a point at (<code>x</code>, <code>y</code>).
   */
  private Room getResizedRoomAt(float x, float y) {
    List<Selectable> selectedItems = this.home.getSelectedItems();
    if (selectedItems.size() == 1
        && selectedItems.get(0) instanceof Room) {
      Room room = (Room)selectedItems.get(0);
      float margin = PIXEL_MARGIN / getScale();
      if (room.getPointIndexAt(x, y, margin) != -1) {
        return room;
      }
    } 
    return null;
  }
  
  /**
   * Returns the selected room with its name center point at (<code>x</code>, <code>y</code>).
   */
  private Room getRoomNameAt(float x, float y) {
    List<Selectable> selectedItems = this.home.getSelectedItems();
    if (selectedItems.size() == 1
        && selectedItems.get(0) instanceof Room
        && isItemMovable(selectedItems.get(0))) {
      Room room = (Room)selectedItems.get(0);
      float margin = PIXEL_MARGIN / getScale();
      if (room.getName() != null
          && room.getName().trim().length() > 0
          && room.isNameCenterPointAt(x, y, margin)) {
        return room;
      }
    } 
    return null;
  }
  
  /**
   * Returns the selected room with its area center point at (<code>x</code>, <code>y</code>).
   */
  private Room getRoomAreaAt(float x, float y) {
    List<Selectable> selectedItems = this.home.getSelectedItems();
    if (selectedItems.size() == 1
        && selectedItems.get(0) instanceof Room
        && isItemMovable(selectedItems.get(0))) {
      Room room = (Room)selectedItems.get(0);
      float margin = PIXEL_MARGIN / getScale();
      if (room.isAreaVisible() 
          && room.isAreaCenterPointAt(x, y, margin)) {
        return room;
      }
    } 
    return null;
  }
  
  /**
   * Returns a new dimension instance joining (<code>xStart</code>,
   * <code>yStart</code>) and (<code>xEnd</code>, <code>yEnd</code>) points. 
   * The new dimension line is added to home.
   */
  protected DimensionLine createDimensionLine(float xStart, float yStart, 
                                              float xEnd, float yEnd, 
                                              float offset) {
    DimensionLine newDimensionLine = new DimensionLine(xStart, yStart, xEnd, yEnd, offset);
    this.home.addDimensionLine(newDimensionLine);
    return newDimensionLine;
  }

  /**
   * Returns the selected dimension line with an end extension line
   * at (<code>x</code>, <code>y</code>).
   */
  private DimensionLine getResizedDimensionLineStartAt(float x, float y) {
    List<Selectable> selectedItems = this.home.getSelectedItems();
    if (selectedItems.size() == 1
        && selectedItems.get(0) instanceof DimensionLine
        && isItemResizable(selectedItems.get(0))) {
      DimensionLine dimensionLine = (DimensionLine)selectedItems.get(0);
      float margin = PIXEL_MARGIN / getScale();
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
    List<Selectable> selectedItems = this.home.getSelectedItems();
    if (selectedItems.size() == 1
        && selectedItems.get(0) instanceof DimensionLine
        && isItemResizable(selectedItems.get(0))) {
      DimensionLine dimensionLine = (DimensionLine)selectedItems.get(0);
      float margin = PIXEL_MARGIN / getScale();
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
    List<Selectable> selectedItems = this.home.getSelectedItems();
    if (selectedItems.size() == 1
        && selectedItems.get(0) instanceof DimensionLine
        && isItemResizable(selectedItems.get(0))) {
      DimensionLine dimensionLine = (DimensionLine)selectedItems.get(0);
      float margin = PIXEL_MARGIN / getScale();
      if (dimensionLine.isMiddlePointAt(x, y, margin)) {
        return dimensionLine;
      }
    } 
    return null;
  }
  
  /**
   * Returns the item that will be selected by a click at (<code>x</code>, <code>y</code>) point.
   */
  protected Selectable getSelectableItemAt(float x, float y) {
    List<Selectable> selectableItems = getSelectableItemsAt(x, y, true);
    if (selectableItems.size() != 0) {
      return selectableItems.get(0);
    } else {
      return null;
    }
  }
  
  /**
   * Returns the selectable items at (<code>x</code>, <code>y</code>) point.
   */
  protected List<Selectable> getSelectableItemsAt(float x, float y) {
    return getSelectableItemsAt(x, y, false);
  }
  
  /**
   * Returns the selectable items at (<code>x</code>, <code>y</code>) point.
   */
  private List<Selectable> getSelectableItemsAt(float x, float y, 
                                                boolean stopAtFirstItem) {
    List<Selectable> items = new ArrayList<Selectable>();
    float margin = PIXEL_MARGIN / getScale();
    float textMargin = PIXEL_MARGIN / 2 / getScale();
    ObserverCamera camera = this.home.getObserverCamera();
    if (camera != null
        && camera == this.home.getCamera()
        && camera.containsPoint(x, y, margin)) {
      items.add(camera);
      if (stopAtFirstItem) {
        return items;
      }
    }
    
    boolean basePlanLocked = this.home.isBasePlanLocked();
    for (Label label : this.home.getLabels()) {
      if (!basePlanLocked 
          || !isItemPartOfBasePlan(label)) {
        if (label.containsPoint(x, y, margin)) {
          items.add(label);
          if (stopAtFirstItem) {
            return items;
          }
        } else if (isItemTextAt(label, label.getText(), label.getStyle(), 
            label.getX(), label.getY(), x, y, textMargin)) {
          items.add(label);
          if (stopAtFirstItem) {
            return items;
          }
        }
      }
    }    
    
    for (DimensionLine dimensionLine : this.home.getDimensionLines()) {
      if ((!basePlanLocked 
            || !isItemPartOfBasePlan(dimensionLine))
          && dimensionLine.containsPoint(x, y, margin)) {
        items.add(dimensionLine);
        if (stopAtFirstItem) {
          return items;
        }
      }
    }    
    
    List<HomePieceOfFurniture> furniture = this.home.getFurniture();
    // Search in home furniture in reverse order to give priority to last drawn piece
    // at highest elevation in case it covers an other piece
    HomePieceOfFurniture foundPiece = null;
    for (int i = furniture.size() - 1; i >= 0; i--) {
      HomePieceOfFurniture piece = furniture.get(i);
      if ((!basePlanLocked 
            || !isItemPartOfBasePlan(piece))
          && piece.isVisible()) {
        if (piece.containsPoint(x, y, margin)) {
          items.add(piece);
          if (foundPiece == null
              || piece.getElevation() > foundPiece.getElevation()) {
            foundPiece = piece;
          }
        } else if (foundPiece == null) { 
          // Search if piece name contains point in case it is drawn outside of the piece
          String pieceName = piece.getName();
          if (pieceName != null
              && piece.isNameVisible() 
              && isItemTextAt(piece, pieceName, piece.getNameStyle(), 
                  piece.getX() + piece.getNameXOffset(), 
                  piece.getY() + piece.getNameYOffset(), x, y, textMargin)) {
            items.add(piece);
            foundPiece = piece;
          }
        }
      }
    }
    if (foundPiece != null
        && stopAtFirstItem) {
      return Arrays.asList(new Selectable [] {foundPiece});
    } else {
      for (Wall wall : this.home.getWalls()) {
        if ((!basePlanLocked 
              || !isItemPartOfBasePlan(wall))
            && wall.containsPoint(x, y, margin)) {
          items.add(wall);
          if (stopAtFirstItem) {
            return items;
          }
        }
      }    

      List<Room> rooms = this.home.getRooms();
      // Search in home rooms in reverse order to give priority to last drawn room
      // at highest elevation in case it covers an other piece
      Room foundRoom = null;
      for (int i = rooms.size() - 1; i >= 0; i--) {
        Room room = rooms.get(i);
        if (!basePlanLocked 
            || !isItemPartOfBasePlan(room)) {
          if (room.containsPoint(x, y, margin)) {
            items.add(room);
             if (foundRoom == null
                 || room.isCeilingVisible() && !foundRoom.isCeilingVisible()) {
               foundRoom = room;
             }
          } else { 
            // Search if room name contains point in case it is drawn outside of the room
            String roomName = room.getName();
            if (roomName != null 
                && isItemTextAt(room, roomName, room.getNameStyle(), 
                  room.getXCenter() + room.getNameXOffset(), 
                  room.getYCenter() + room.getNameYOffset(), x, y, textMargin)) {
              items.add(room);
              foundRoom = room;
            }
            // Search if room area contains point in case its text is drawn outside of the room 
            if (room.isAreaVisible()) {
              String areaText = this.preferences.getLengthUnit().getAreaFormatWithUnit().format(room.getArea());
              if (isItemTextAt(room, areaText, room.getAreaStyle(), 
                  room.getXCenter() + room.getAreaXOffset(), 
                  room.getYCenter() + room.getAreaYOffset(), x, y, textMargin)) {
                items.add(room);
                foundRoom = room;
              }
            }
          }
        }
      }
      if (foundRoom != null
          && stopAtFirstItem) {
        return Arrays.asList(new Selectable [] {foundRoom});
      } else {
        return items;
      }
    }
  }

  /**
   * Returns <code>true</code> if the <code>text</code> of an <code>item</code> displayed
   * at the point (<code>xText</code>, <code>yText</code>) contains the point (<code>x</code>, <code>y</code>).
   */
  private boolean isItemTextAt(Selectable item, String text, TextStyle textStyle, float xText, float yText, 
                               float x, float y, float textMargin) {
    if (textStyle == null) {
      textStyle = this.preferences.getDefaultTextStyle(item.getClass());              
    }          
    float [][] textBounds = getView().getTextBounds(text, textStyle, xText, yText, 0);
    return getPath(textBounds).intersects(x - textMargin, y - textMargin, 2 * textMargin, 2 * textMargin);
  }
  
  /**
   * Returns the items that intersects with the rectangle of (<code>x0</code>,
   * <code>y0</code>), (<code>x1</code>, <code>y1</code>) opposite corners.
   */
  protected List<Selectable> getSelectableItemsIntersectingRectangle(float x0, float y0, float x1, float y1) {
    List<Selectable> items = new ArrayList<Selectable>();
    updateRectangleItems(items, this.home.getDimensionLines(), x0, y0, x1, y1);
    updateRectangleItems(items, this.home.getRooms(), x0, y0, x1, y1);
    updateRectangleItems(items, this.home.getWalls(), x0, y0, x1, y1);
    updateRectangleItems(items, this.home.getLabels(), x0, y0, x1, y1);
    ObserverCamera camera = this.home.getObserverCamera();
    if (camera != null && camera.intersectsRectangle(x0, y0, x1, y1)) {
      items.add(camera);
    }
    boolean basePlanLocked = this.home.isBasePlanLocked();
    for (HomePieceOfFurniture piece : this.home.getFurniture()) {
      if ((!basePlanLocked 
            || !isItemPartOfBasePlan(piece))
          && piece.isVisible() 
          && piece.intersectsRectangle(x0, y0, x1, y1)) {
        items.add(piece);
      }
    }
    return items;
  }

  /**
   * Adds to <code>rectangleItems</code> every item of <code>selectableItems</code>
   * that intersects with the rectangle of (<code>x0</code>, <code>y0</code>), 
   * (<code>x1</code>, <code>y1</code>) opposite corners.
   */
  private void updateRectangleItems(List<Selectable> rectangleItems,
                                    Collection<? extends Selectable> selectableItems,
                                    float x0, float y0, float x1, float y1) {
    boolean basePlanLocked = this.home.isBasePlanLocked();
    for (Selectable item : selectableItems) {
      if ((!basePlanLocked 
            || !isItemPartOfBasePlan(item))
          && item.intersectsRectangle(x0, y0, x1, y1)) {
        rectangleItems.add(item);
      }
    }
  }
  
  /**
   * Returns the selected piece of furniture with a point 
   * at (<code>x</code>, <code>y</code>) that can be used to rotate the piece.
   */
  private HomePieceOfFurniture getRotatedPieceOfFurnitureAt(float x, float y) {
    List<Selectable> selectedItems = this.home.getSelectedItems();
    if (selectedItems.size() == 1
        && selectedItems.get(0) instanceof HomePieceOfFurniture
        && isItemMovable(selectedItems.get(0))) {
      HomePieceOfFurniture piece = (HomePieceOfFurniture)selectedItems.get(0);
      float margin = PIXEL_MARGIN / getScale();
      if (piece.isTopLeftPointAt(x, y, margin)) {
        return piece;
      }
    } 
    return null;
  }
  
  /**
   * Returns the selected piece of furniture with a point 
   * at (<code>x</code>, <code>y</code>) that can be used to elevate the piece.
   */
  private HomePieceOfFurniture getElevatedPieceOfFurnitureAt(float x, float y) {
    List<Selectable> selectedItems = this.home.getSelectedItems();
    if (selectedItems.size() == 1
        && selectedItems.get(0) instanceof HomePieceOfFurniture
        && isItemMovable(selectedItems.get(0))) {
      HomePieceOfFurniture piece = (HomePieceOfFurniture)selectedItems.get(0);
      float margin = PIXEL_MARGIN / getScale();
      if (piece.isTopRightPointAt(x, y, margin)) {
        return piece;
      }
    } 
    return null;
  }
  
  /**
   * Returns the selected piece of furniture with a point 
   * at (<code>x</code>, <code>y</code>) that can be used to resize the height 
   * of the piece.
   */
  private HomePieceOfFurniture getHeightResizedPieceOfFurnitureAt(float x, float y) {
    List<Selectable> selectedItems = this.home.getSelectedItems();
    if (selectedItems.size() == 1
        && selectedItems.get(0) instanceof HomePieceOfFurniture) {
      HomePieceOfFurniture piece = (HomePieceOfFurniture)selectedItems.get(0);
      float margin = PIXEL_MARGIN / getScale();
      if (piece.isResizable()
          && isItemResizable(piece) 
          && piece.isBottomLeftPointAt(x, y, margin)) {
        return piece;
      }
    } 
    return null;
  }
  
  /**
   * Returns the selected piece of furniture with a point 
   * at (<code>x</code>, <code>y</code>) that can be used to resize 
   * the width and the depth of the piece.
   */
  private HomePieceOfFurniture getWidthAndDepthResizedPieceOfFurnitureAt(float x, float y) {
    List<Selectable> selectedItems = this.home.getSelectedItems();
    if (selectedItems.size() == 1
        && selectedItems.get(0) instanceof HomePieceOfFurniture
        && isItemResizable(selectedItems.get(0))) {
      HomePieceOfFurniture piece = (HomePieceOfFurniture)selectedItems.get(0);
      float margin = PIXEL_MARGIN / getScale();
      if (piece.isResizable()
          && isItemResizable(piece) 
          && piece.isBottomRightPointAt(x, y, margin)) {
        return piece;
      }
    } 
    return null;
  }
  
  /**
   * Returns the selected piece of furniture with its 
   * name center point at (<code>x</code>, <code>y</code>).
   */
  private HomePieceOfFurniture getPieceOfFurnitureNameAt(float x, float y) {
    List<Selectable> selectedItems = this.home.getSelectedItems();
    if (selectedItems.size() == 1
        && selectedItems.get(0) instanceof HomePieceOfFurniture
        && isItemMovable(selectedItems.get(0))) {
      HomePieceOfFurniture piece = (HomePieceOfFurniture)selectedItems.get(0);
      float margin = PIXEL_MARGIN / getScale();
      if (piece.isNameVisible()
          && piece.getName().trim().length() > 0
          && piece.isNameCenterPointAt(x, y, margin)) {
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
    List<Selectable> selectedItems = this.home.getSelectedItems();
    if (selectedItems.size() == 1
        && selectedItems.get(0) instanceof Camera
        && isItemResizable(selectedItems.get(0))) {
      ObserverCamera camera = (ObserverCamera)selectedItems.get(0);
      float margin = PIXEL_MARGIN / getScale();
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
    List<Selectable> selectedItems = this.home.getSelectedItems();
    if (selectedItems.size() == 1
        && selectedItems.get(0) instanceof Camera
        && isItemResizable(selectedItems.get(0))) {
      ObserverCamera camera = (ObserverCamera)selectedItems.get(0);
      float margin = PIXEL_MARGIN / getScale();
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
   * Returns the selected camera with a point at (<code>x</code>, <code>y</code>) 
   * that can be used to change the camera elevation.
   */
  private Camera getElevatedCameraAt(float x, float y) {
    List<Selectable> selectedItems = this.home.getSelectedItems();
    if (selectedItems.size() == 1
        && selectedItems.get(0) instanceof Camera
        && isItemResizable(selectedItems.get(0))) {
      ObserverCamera camera = (ObserverCamera)selectedItems.get(0);
      float margin = PIXEL_MARGIN / getScale();
      float [][] cameraPoints = camera.getPoints();
      // Check if (x,y) matches the point between the first and the second point 
      // of the rectangle surrounding camera
      float xMiddleFirstAndSecondPoint = (cameraPoints [0][0] + cameraPoints [1][0]) / 2; 
      float yMiddleFirstAndSecondPoint = (cameraPoints [0][1] + cameraPoints [1][1]) / 2;      
      if (Math.abs(x - xMiddleFirstAndSecondPoint) <= margin 
          && Math.abs(y - yMiddleFirstAndSecondPoint) <= margin) {
        return camera;
      }
    } 
    return null;
  }

  /**
   * Deletes <code>items</code> in plan and record it as an undoable operation.
   */
  public void deleteItems(List<? extends Selectable> items) {
    List<Selectable> deletedItems = new ArrayList<Selectable>(items.size());
    for (Selectable item : items) {
      if (isItemDeletable(item)) {
        deletedItems.add(item);
      }
    }
    
    if (!deletedItems.isEmpty()) {
      // Start a compound edit that deletes walls, furniture and dimension lines from home
      this.undoSupport.beginUpdate();
      
      final List<Selectable> selectedItems = new ArrayList<Selectable>(items);      
      // Add a undoable edit that will select the undeleted items at undo
      this.undoSupport.postEdit(new AbstractUndoableEdit() {      
          @Override
          public void undo() throws CannotRedoException {
            super.undo();
            selectAndShowItems(selectedItems);
          }
        });

      deleteFurniture(Home.getFurnitureSubList(deletedItems));      

      List<Selectable> deletedOtherItems = 
          new ArrayList<Selectable>(Home.getWallsSubList(deletedItems));
      deletedOtherItems.addAll(Home.getRoomsSubList(deletedItems));
      deletedOtherItems.addAll(Home.getDimensionLinesSubList(deletedItems));
      deletedOtherItems.addAll(Home.getLabelsSubList(deletedItems));
      // First post to undo support that walls, rooms and dimension lines are deleted, 
      // otherwise data about joined walls and rooms index can't be stored       
      postDeleteItems(deletedOtherItems, this.home.isBasePlanLocked());
      // Then delete items from plan
      doDeleteItems(deletedOtherItems);

      // End compound edit
      this.undoSupport.endUpdate();
    }          
  }

  /**
   * Posts an undoable delete items operation about <code>deletedItems</code>.
   */
  private void postDeleteItems(final List<? extends Selectable> deletedItems,
                               final boolean basePlanLocked) {
    // Manage walls
    List<Wall> deletedWalls = Home.getWallsSubList(deletedItems);
    // Get joined walls data for undo operation
    final JoinedWall [] joinedDeletedWalls = 
      JoinedWall.getJoinedWalls(deletedWalls);

    // Manage rooms and their index
    List<Room> deletedRooms = Home.getRoomsSubList(deletedItems);
    List<Room> homeRooms = this.home.getRooms(); 
    // Sort the deleted rooms in the ascending order of their index in home
    Map<Integer, Room> sortedMap = new TreeMap<Integer, Room>(); 
    for (Room room : deletedRooms) {
      sortedMap.put(homeRooms.indexOf(room), room); 
    }
    final Room [] rooms = sortedMap.values().toArray(new Room [sortedMap.size()]); 
    final int [] roomsIndex = new int [rooms.length];
    int i = 0;
    for (int index : sortedMap.keySet()) {
      roomsIndex [i++] = index; 
    }
    
    // Manage dimension lines
    List<DimensionLine> deletedDimensionLines = Home.getDimensionLinesSubList(deletedItems);
    final DimensionLine [] dimensionLines = deletedDimensionLines.toArray(
        new DimensionLine [deletedDimensionLines.size()]);
    
    // Manage labels
    List<Label> deletedLabels = Home.getLabelsSubList(deletedItems);
    final Label [] labels = deletedLabels.toArray(new Label [deletedLabels.size()]);
    
    UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
      @Override
      public void undo() throws CannotUndoException {
        super.undo();
        doAddWalls(joinedDeletedWalls, basePlanLocked);       
        doAddRooms(rooms, roomsIndex, basePlanLocked);
        doAddDimensionLines(dimensionLines, basePlanLocked);
        doAddLabels(labels, basePlanLocked);
        selectAndShowItems(deletedItems);
      }
      
      @Override
      public void redo() throws CannotRedoException {
        super.redo();
        selectItems(deletedItems);
        doDeleteWalls(joinedDeletedWalls, basePlanLocked);       
        doDeleteRooms(rooms, basePlanLocked);
        doDeleteDimensionLines(dimensionLines, basePlanLocked);
        doDeleteLabels(labels, basePlanLocked);
      }      
      
      @Override
      public String getPresentationName() {
        return preferences.getLocalizedString(
            PlanController.class, "undoDeleteSelectionName");
      }      
    };
    this.undoSupport.postEdit(undoableEdit);
  }

  /**
   * Deletes <code>items</code> from home.
   */
  private void doDeleteItems(List<Selectable> items) {
    boolean basePlanLocked = this.home.isBasePlanLocked();
    for (Selectable item : items) {
      if (item instanceof Wall) {
        home.deleteWall((Wall)item);
      } else if (item instanceof DimensionLine) {
        home.deleteDimensionLine((DimensionLine)item);
      } else if (item instanceof Room) {
        home.deleteRoom((Room)item);
      } else if (item instanceof Label) {
        home.deleteLabel((Label)item);
      } else if (item instanceof HomePieceOfFurniture) {
        home.deletePieceOfFurniture((HomePieceOfFurniture)item);
      }
      // Unlock base plan if item is a part of it
      basePlanLocked &= !isItemPartOfBasePlan(item);
    }
    this.home.setBasePlanLocked(basePlanLocked);
  }
  
  /**
   * Moves and shows selected items in plan component of (<code>dx</code>,
   * <code>dy</code>) units and record it as undoable operation.
   */
  private void moveAndShowSelectedItems(float dx, float dy) {
    List<Selectable> selectedItems = this.home.getSelectedItems();
    List<Selectable> movedItems = new ArrayList<Selectable>(selectedItems.size());      
    for (Selectable item : selectedItems) {
      if (isItemMovable(item)) {
        movedItems.add(item);
      }
    }
    
    if (!movedItems.isEmpty()) {
      moveItems(movedItems, dx, dy);
      selectAndShowItems(movedItems);
      if (movedItems.size() != 1
          || !(movedItems.get(0) instanceof Camera)) {
        // Post move undo only for items different from the camera 
        postItemsMove(movedItems, selectedItems, dx, dy);
      }
    }
  }

  /**
   * Moves <code>items</code> of (<code>dx</code>, <code>dy</code>) units.
   */
  public void moveItems(List<? extends Selectable> items, float dx, float dy) {
    for (Selectable item : items) {
      if (item instanceof Wall) {
        Wall wall = (Wall)item;
        moveWallStartPoint(wall, 
            wall.getXStart() + dx, wall.getYStart() + dy,
            !items.contains(wall.getWallAtStart()));
        moveWallEndPoint(wall, 
            wall.getXEnd() + dx, wall.getYEnd() + dy,
            !items.contains(wall.getWallAtEnd()));
      } else {
        item.move(dx, dy);
      } 
    }
  }
  
  /**
   * Moves <code>wall</code> start point to (<code>xStart</code>, <code>yStart</code>)
   * and the wall point joined to its start point if <code>moveWallAtStart</code> is true.
   */
  private void moveWallStartPoint(Wall wall, float xStart, float yStart,
                                  boolean moveWallAtStart) {
    wall.setXStart(xStart);
    wall.setYStart(yStart);
    Wall wallAtStart = wall.getWallAtStart();
    // If wall is joined to a wall at its start 
    // and this wall doesn't belong to the list of moved walls
    if (wallAtStart != null && moveWallAtStart) {
      // Move the wall start point or end point
      if (wallAtStart.getWallAtStart() == wall) {
        wallAtStart.setXStart(xStart);
        wallAtStart.setYStart(yStart);
      } else if (wallAtStart.getWallAtEnd() == wall) {
        wallAtStart.setXEnd(xStart);
        wallAtStart.setYEnd(yStart);
      }
    }
  }
  
  /**
   * Moves <code>wall</code> end point to (<code>xEnd</code>, <code>yEnd</code>)
   * and the wall point joined to its end if <code>moveWallAtEnd</code> is true.
   */
  private void moveWallEndPoint(Wall wall, float xEnd, float yEnd,
                                boolean moveWallAtEnd) {
    wall.setXEnd(xEnd);
    wall.setYEnd(yEnd);
    Wall wallAtEnd = wall.getWallAtEnd();
    // If wall is joined to a wall at its end  
    // and this wall doesn't belong to the list of moved walls
    if (wallAtEnd != null && moveWallAtEnd) {
      // Move the wall start point or end point
      if (wallAtEnd.getWallAtStart() == wall) {
        wallAtEnd.setXStart(xEnd);
        wallAtEnd.setYStart(yEnd);
      } else if (wallAtEnd.getWallAtEnd() == wall) {
        wallAtEnd.setXEnd(xEnd);
        wallAtEnd.setYEnd(yEnd);
      }
    }
  }
  
  /**
   * Moves <code>wall</code> start point to (<code>x</code>, <code>y</code>)
   * if <code>editingStartPoint</code> is true or <code>wall</code> end point 
   * to (<code>x</code>, <code>y</code>) if <code>editingStartPoint</code> is false.
   */
  private void moveWallPoint(Wall wall, float x, float y, boolean startPoint) {
    if (startPoint) {
      moveWallStartPoint(wall, x, y, true);
    } else {
      moveWallEndPoint(wall, x, y, true);
    }    
  }
  
  /**
   * Moves <code>room</code> point at the given index to (<code>x</code>, <code>y</code>).
   */
  private void moveRoomPoint(Room room, float x, float y, int pointIndex) {
    room.setPoint(x, y, pointIndex);
  }
  
  /**
   * Moves <code>dimensionLine</code> start point to (<code>x</code>, <code>y</code>)
   * if <code>editingStartPoint</code> is true or <code>dimensionLine</code> end point 
   * to (<code>x</code>, <code>y</code>) if <code>editingStartPoint</code> is false.
   */
  private void moveDimensionLinePoint(DimensionLine dimensionLine, float x, float y, boolean startPoint) {
    if (startPoint) {
      dimensionLine.setXStart(x);
      dimensionLine.setYStart(y);
    } else {
      dimensionLine.setXEnd(x);
      dimensionLine.setYEnd(y);
    }    
  }
  
  /**
   * Swaps start and end points of the given dimension line.
   */
  private void reverseDimensionLine(DimensionLine dimensionLine) {
    float swappedX = dimensionLine.getXStart();
    float swappedY = dimensionLine.getYStart();
    dimensionLine.setXStart(dimensionLine.getXEnd());
    dimensionLine.setYStart(dimensionLine.getYEnd());
    dimensionLine.setXEnd(swappedX);
    dimensionLine.setYEnd(swappedY);
    dimensionLine.setOffset(-dimensionLine.getOffset());
  }
  
  /**
   * Selects <code>items</code> and make them visible at screen.
   */
  protected void selectAndShowItems(List<? extends Selectable> items) {
    selectItems(items);
    getView().makeSelectionVisible();
  }
  
  /**
   * Selects <code>items</code>.
   */
  protected void selectItems(List<? extends Selectable> items) {
    // Remove selectionListener when selection is done from this controller
    // to control when selection should be made visible
    this.home.removeSelectionListener(this.selectionListener);
    this.home.setSelectedItems(items);
    this.home.addSelectionListener(this.selectionListener);
  }
  
  /**
   * Selects only a given <code>item</code>.
   */
  private void selectItem(Selectable item) {
    selectItems(Arrays.asList(new Selectable [] {item}));
  }

  /**
   * Deselects all walls in plan. 
   */
  private void deselectAll() {
    List<Selectable> emptyList = Collections.emptyList(); 
    selectItems(emptyList);
  }

  /**
   * Add <code>walls</code> to home and post an undoable new wall operation.
   */
  public void addWalls(List<Wall> walls) {
    for (Wall wall : walls) {
      this.home.addWall(wall);
    }    
    postCreateWalls(walls, this.home.getSelectedItems(), home.isBasePlanLocked());
  }
  
  /**
   * Posts an undoable new wall operation, about <code>newWalls</code>.
   */
  private void postCreateWalls(List<Wall> newWalls, 
                               List<Selectable> oldSelection, 
                               final boolean oldBasePlanLocked) {
    if (newWalls.size() > 0) {
      boolean basePlanLocked = this.home.isBasePlanLocked();
      if (basePlanLocked) {
        for (Wall wall : newWalls) {
          // Unlock base plan if wall is a part of it
          basePlanLocked &= !isItemPartOfBasePlan(wall);
        }
        this.home.setBasePlanLocked(basePlanLocked);
      }
      final boolean newBasePlanLocked = basePlanLocked;
      
      // Retrieve data about joined walls to newWalls
      final JoinedWall [] joinedNewWalls = new JoinedWall [newWalls.size()];
      for (int i = 0; i < joinedNewWalls.length; i++) {
         joinedNewWalls [i] = new JoinedWall(newWalls.get(i));
      }
      final Selectable [] oldSelectedItems = 
        oldSelection.toArray(new Selectable [oldSelection.size()]);
      UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
        @Override
        public void undo() throws CannotUndoException {
          super.undo();
          doDeleteWalls(joinedNewWalls, oldBasePlanLocked);
          selectAndShowItems(Arrays.asList(oldSelectedItems));
        }
        
        @Override
        public void redo() throws CannotRedoException {
          super.redo();
          doAddWalls(joinedNewWalls, newBasePlanLocked);       
          selectAndShowItems(JoinedWall.getWalls(joinedNewWalls));
        }      
  
        @Override
        public String getPresentationName() {
          return preferences.getLocalizedString(
              PlanController.class, "undoCreateWallsName");
        }      
      };
      this.undoSupport.postEdit(undoableEdit);
    }
  }

  /**
   * Adds the walls in <code>joinedWalls</code> to plan component, joins
   * them to other walls if necessary.
   */
  private void doAddWalls(JoinedWall [] joinedWalls, boolean basePlanLocked) {
    // First add all walls to home
    for (JoinedWall joinedNewWall : joinedWalls) {
      Wall wall = joinedNewWall.getWall();
      this.home.addWall(wall);
    }
    this.home.setBasePlanLocked(basePlanLocked);
    
    // Then join them to each other if necessary
    for (JoinedWall joinedNewWall : joinedWalls) {
      Wall wall = joinedNewWall.getWall();
      Wall wallAtStart = joinedNewWall.getWallAtStart();
      if (wallAtStart != null) {
        wall.setWallAtStart(wallAtStart);
        if (joinedNewWall.isJoinedAtEndOfWallAtStart()) {
          wallAtStart.setWallAtEnd(wall);
        } else if (joinedNewWall.isJoinedAtStartOfWallAtStart()) {
          wallAtStart.setWallAtStart(wall);
        }
      }
      Wall wallAtEnd = joinedNewWall.getWallAtEnd();
      if (wallAtEnd != null) {
        wall.setWallAtEnd(wallAtEnd);
        if (joinedNewWall.isJoinedAtStartOfWallAtEnd()) {
          wallAtEnd.setWallAtStart(wall);
        } else if (joinedNewWall.isJoinedAtEndOfWallAtEnd()) {
          wallAtEnd.setWallAtEnd(wall);
        }
      }
    }      
  }
  
  /**
   * Deletes walls referenced in <code>joinedDeletedWalls</code>.
   */
  private void doDeleteWalls(JoinedWall [] joinedDeletedWalls, 
                             boolean basePlanLocked) {
    for (JoinedWall joinedWall : joinedDeletedWalls) {
      this.home.deleteWall(joinedWall.getWall());
    }
    this.home.setBasePlanLocked(basePlanLocked);
  }

  /**
   * Add <code>newRooms</code> to home and post an undoable new room line operation.
   */
  public void addRooms(List<Room> rooms) {
    final Room [] newRooms = rooms.toArray(new Room [rooms.size()]);
    // Get indices of rooms added to home
    final int [] roomsIndex = new int [rooms.size()];
    int endIndex = home.getRooms().size();
    for (int i = 0; i < roomsIndex.length; i++) {
      roomsIndex [i] = endIndex++; 
      this.home.addRoom(newRooms [i], roomsIndex [i]);
    }
    postCreateRooms(newRooms, roomsIndex, this.home.getSelectedItems(), this.home.isBasePlanLocked());
  }
  
  /**
   * Posts an undoable new room operation, about <code>newRooms</code>.
   */
  private void postCreateRooms(final Room [] newRooms,
                               final int [] roomsIndex, 
                               List<Selectable> oldSelection, 
                               final boolean oldBasePlanLocked) {
    if (newRooms.length > 0) {
      boolean basePlanLocked = this.home.isBasePlanLocked();
      if (basePlanLocked) {
        for (Room room : newRooms) {
          // Unlock base plan if room is a part of it
          basePlanLocked &= !isItemPartOfBasePlan(room);
        }
        this.home.setBasePlanLocked(basePlanLocked);
      }
      final boolean newBasePlanLocked = basePlanLocked;

      final Selectable [] oldSelectedItems = 
          oldSelection.toArray(new Selectable [oldSelection.size()]);
      UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
        @Override
        public void undo() throws CannotUndoException {
          super.undo();
          doDeleteRooms(newRooms, oldBasePlanLocked);
          selectAndShowItems(Arrays.asList(oldSelectedItems));
        }
        
        @Override
        public void redo() throws CannotRedoException {
          super.redo();
          doAddRooms(newRooms, roomsIndex, newBasePlanLocked);       
          selectAndShowItems(Arrays.asList(newRooms));
        }      
  
        @Override
        public String getPresentationName() {
          return preferences.getLocalizedString(
              PlanController.class, "undoCreateRoomsName");
        }      
      };
      this.undoSupport.postEdit(undoableEdit);
    }
  }

  /**
   * Posts an undoable new room operation, about <code>newRooms</code>.
   */
  private void postCreateRooms(List<Room> rooms, 
                               List<Selectable> oldSelection,
                               boolean basePlanLocked) {
    // Search the index of rooms in home list of rooms
    Room [] newRooms = rooms.toArray(new Room [rooms.size()]);
    int [] roomsIndex = new int [rooms.size()];
    List<Room> homeRooms = this.home.getRooms(); 
    for (int i = 0; i < roomsIndex.length; i++) {
      roomsIndex [i] = homeRooms.lastIndexOf(newRooms [i]); 
    }
    postCreateRooms(newRooms, roomsIndex, oldSelection, basePlanLocked);
  }

  /**
   * Adds the <code>rooms</code> to plan component.
   * @param oldBasePlanLocked 
   */
  private void doAddRooms(Room [] rooms,
                          int [] roomsIndex, 
                          boolean basePlanLocked) {
    for (int i = 0; i < roomsIndex.length; i++) {
      this.home.addRoom (rooms [i], roomsIndex [i]);
    }
    this.home.setBasePlanLocked(basePlanLocked);
  }
  
  /**
   * Deletes <code>rooms</code>.
   */
  private void doDeleteRooms(Room [] rooms, 
                             boolean basePlanLocked) {
    for (Room room : rooms) {
      this.home.deleteRoom(room);
    }
    this.home.setBasePlanLocked(basePlanLocked);
  }

  /**
   * Add <code>dimensionLines</code> to home and post an undoable new dimension line operation.
   */
  public void addDimensionLines(List<DimensionLine> dimensionLines) {
    for (DimensionLine dimensionLine : dimensionLines) {
      this.home.addDimensionLine(dimensionLine);
    }
    postCreateDimensionLines(dimensionLines, 
        this.home.getSelectedItems(), this.home.isBasePlanLocked());
  }
  
  /**
   * Posts an undoable new dimension line operation, about <code>newDimensionLines</code>.
   */
  private void postCreateDimensionLines(List<DimensionLine> newDimensionLines, 
                                        List<Selectable> oldSelection,
                                        final boolean oldBasePlanLocked) {
    if (newDimensionLines.size() > 0) {
      boolean basePlanLocked = this.home.isBasePlanLocked();
      if (basePlanLocked) {
        for (DimensionLine dimensionLine : newDimensionLines) {
          // Unlock base plan if dimension line is a part of it
          basePlanLocked &= !isItemPartOfBasePlan(dimensionLine);
        }
        this.home.setBasePlanLocked(basePlanLocked);
      }
      final boolean newBasePlanLocked = basePlanLocked;
      
      final DimensionLine [] dimensionLines = newDimensionLines.toArray(
          new DimensionLine [newDimensionLines.size()]);
      final Selectable [] oldSelectedItems = 
          oldSelection.toArray(new Selectable [oldSelection.size()]);
      UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
        @Override
        public void undo() throws CannotUndoException {
          super.undo();
          doDeleteDimensionLines(dimensionLines, oldBasePlanLocked);
          selectAndShowItems(Arrays.asList(oldSelectedItems));
        }
        
        @Override
        public void redo() throws CannotRedoException {
          super.redo();
          doAddDimensionLines(dimensionLines, newBasePlanLocked);       
          selectAndShowItems(Arrays.asList(dimensionLines));
        }      
  
        @Override
        public String getPresentationName() {
          return preferences.getLocalizedString(
              PlanController.class, "undoCreateDimensionLinesName");
        }      
      };
      this.undoSupport.postEdit(undoableEdit);
    }
  }

  /**
   * Adds the dimension lines in <code>dimensionLines</code> to plan component.
   */
  private void doAddDimensionLines(DimensionLine [] dimensionLines, 
                                   boolean basePlanLocked) {
    for (DimensionLine dimensionLine : dimensionLines) {
      this.home.addDimensionLine(dimensionLine);
    }
    this.home.setBasePlanLocked(basePlanLocked);
  }
  
  /**
   * Deletes dimension lines in <code>dimensionLines</code>.
   */
  private void doDeleteDimensionLines(DimensionLine [] dimensionLines,
                                      boolean basePlanLocked) {
    for (DimensionLine dimensionLine : dimensionLines) {
      this.home.deleteDimensionLine(dimensionLine);
    }
    this.home.setBasePlanLocked(basePlanLocked);
  }

  /**
   * Add <code>labels</code> to home and post an undoable new label operation.
   */
  public void addLabels(List<Label> labels) {
    for (Label label : labels) {
      this.home.addLabel(label);
    }
    postCreateLabels(labels, this.home.getSelectedItems(), this.home.isBasePlanLocked());
  }
  
  /**
   * Posts an undoable new label operation, about <code>newLabels</code>.
   */
  private void postCreateLabels(List<Label> newLabels, 
                                List<Selectable> oldSelection, 
                                final boolean oldBasePlanLocked) {
    if (newLabels.size() > 0) {
      boolean basePlanLocked = this.home.isBasePlanLocked();
      if (basePlanLocked) {
        for (Label label : newLabels) {
          // Unlock base plan if label is a part of it
          basePlanLocked &= !isItemPartOfBasePlan(label);
        }
        this.home.setBasePlanLocked(basePlanLocked);
      }
      final boolean newBasePlanLocked = basePlanLocked;
      
      final Label [] labels = newLabels.toArray(new Label [newLabels.size()]);
      final Selectable [] oldSelectedItems = 
          oldSelection.toArray(new Selectable [oldSelection.size()]);
      UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
        @Override
        public void undo() throws CannotUndoException {
          super.undo();
          doDeleteLabels(labels, oldBasePlanLocked);
          selectAndShowItems(Arrays.asList(oldSelectedItems));
        }
        
        @Override
        public void redo() throws CannotRedoException {
          super.redo();
          doAddLabels(labels, newBasePlanLocked);       
          selectAndShowItems(Arrays.asList(labels));
        }      
  
        @Override
        public String getPresentationName() {
          return preferences.getLocalizedString(
              PlanController.class, "undoCreateLabelsName");
        }      
      };
      this.undoSupport.postEdit(undoableEdit);
    }
  }

  /**
   * Adds the labels in <code>labels</code> to plan component.
   */
  private void doAddLabels(Label [] labels, boolean basePlanLocked) {
    for (Label label : labels) {
      this.home.addLabel(label);
    }
    this.home.setBasePlanLocked(basePlanLocked);
  }
  
  /**
   * Deletes labels in <code>labels</code>.
   */
  private void doDeleteLabels(Label [] labels, boolean basePlanLocked) {
    for (Label label : labels) {
      this.home.deleteLabel(label);
    }
    this.home.setBasePlanLocked(basePlanLocked);
  }

  /**
   * Posts an undoable operation of a (<code>dx</code>, <code>dy</code>) move 
   * of <code>movedItems</code>.
   */
  private void postItemsMove(List<? extends Selectable> movedItems, 
                             List<? extends Selectable> oldSelection, 
                             final float dx, final float dy) {
    if (dx != 0 || dy != 0) {
      // Store the moved items in an array
      final Selectable [] itemsArray = 
          movedItems.toArray(new Selectable [movedItems.size()]);
      final Selectable [] oldSelectedItems = 
        oldSelection.toArray(new Selectable [oldSelection.size()]);
      UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
        @Override
        public void undo() throws CannotUndoException {
          super.undo();
          doMoveAndShowItems(itemsArray, oldSelectedItems, -dx, -dy);       
        }
        
        @Override
        public void redo() throws CannotRedoException {
          super.redo();
          doMoveAndShowItems(itemsArray, itemsArray, dx, dy);   
        }      
  
        @Override
        public String getPresentationName() {
          return preferences.getLocalizedString(
              PlanController.class, "undoMoveSelectionName");
        }      
      };
      this.undoSupport.postEdit(undoableEdit);
    }
  }

  /** 
   * Moves <code>movedItems</code> of (<code>dx</code>, <code>dy</code>) pixels, 
   * selects them and make them visible.
   */
  private void doMoveAndShowItems(Selectable [] movedItems,
                                  Selectable [] selectedItems,
                                  float dx, float dy) {
    moveItems(Arrays.asList(movedItems), dx, dy);   
    selectAndShowItems(Arrays.asList(selectedItems));
  }

  /**
   * Posts an undoable operation of a (<code>dx</code>, <code>dy</code>) move 
   * of <code>movedPieceOfFurniture</code>.
   */
  private void postPieceOfFurnitureMove(final HomePieceOfFurniture piece, 
                                        final float dx, final float dy,
                                        final float oldAngle, 
                                        final float oldDepth,
                                        final float oldElevation,
                                        final boolean oldDoorOrWindowBoundToWall) {
    final float newAngle = piece.getAngle();
    final float newDepth = piece.getDepth();
    final float newElevation = piece.getElevation();
    if (dx != 0 || dy != 0 
        || newAngle != oldAngle 
        || newDepth != oldDepth
        || newElevation != oldElevation) {
      UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
        @Override
        public void undo() throws CannotUndoException {
          super.undo();
          piece.move(-dx, -dy);
          piece.setAngle(oldAngle);
          if (piece.isResizable()
              && isItemResizable(piece)) {
            piece.setDepth(oldDepth);
          }
          piece.setElevation(oldElevation);
          if (piece instanceof HomeDoorOrWindow) {
            ((HomeDoorOrWindow)piece).setBoundToWall(oldDoorOrWindowBoundToWall);
          }
          selectAndShowItems(Arrays.asList(new HomePieceOfFurniture [] {piece}));
        }
        
        @Override
        public void redo() throws CannotRedoException {
          super.redo();
          piece.move(dx, dy);
          piece.setAngle(newAngle);
          if (piece.isResizable()
              && isItemResizable(piece)) {
            piece.setDepth(newDepth);
          }
          piece.setElevation(newElevation);
          selectAndShowItems(Arrays.asList(new HomePieceOfFurniture [] {piece}));
        }      
  
        @Override
        public String getPresentationName() {
          return preferences.getLocalizedString(
              PlanController.class, "undoMoveSelectionName");
        }      
      };
      this.undoSupport.postEdit(undoableEdit);
    }
  }

  /**
   * Posts an undoable operation about duplication <code>items</code>.
   */
  private void postItemsDuplication(final List<Selectable> items,
                                    final List<Selectable> oldSelectedItems) {
    boolean basePlanLocked = this.home.isBasePlanLocked();
    // Delete furniture and add it again in a compound edit
    List<HomePieceOfFurniture> furniture = Home.getFurnitureSubList(items);
    for (HomePieceOfFurniture piece : furniture) {
      this.home.deletePieceOfFurniture(piece);
    }
    
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
    List<Selectable> emptyList = Collections.emptyList();
    postCreateWalls(Home.getWallsSubList(items), emptyList, basePlanLocked);
    postCreateRooms(Home.getRoomsSubList(items), emptyList, basePlanLocked);
    postCreateDimensionLines(Home.getDimensionLinesSubList(items), emptyList, basePlanLocked);
    postCreateLabels(Home.getLabelsSubList(items), emptyList, basePlanLocked);

    // Add a undoable edit that will select all the items at redo
    this.undoSupport.postEdit(new AbstractUndoableEdit() {      
        @Override
        public void redo() throws CannotRedoException {
          super.redo();
          selectAndShowItems(items);
        }

        @Override
        public String getPresentationName() {
          return preferences.getLocalizedString(
              PlanController.class, "undoDuplicateSelectionName");
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
          return preferences.getLocalizedString(
              PlanController.class, "undoWallResizeName");
        }      
      };
      this.undoSupport.postEdit(undoableEdit);
    }
  }
  
  /**
   * Posts an undoable operation about <code>room</code> resizing.
   */
  private void postRoomResize(final Room room, final float oldX, final float oldY, 
                              final int pointIndex) {
    float [] roomPoint = room.getPoints() [pointIndex];
    final float newX = roomPoint [0];
    final float newY = roomPoint [1];
    if (newX != oldX || newY != oldY) {
      UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
        @Override
        public void undo() throws CannotUndoException {
          super.undo();
          moveRoomPoint(room, oldX, oldY, pointIndex);
          selectAndShowItems(Arrays.asList(new Room [] {room}));
        }
        
        @Override
        public void redo() throws CannotRedoException {
          super.redo();
          moveRoomPoint(room, newX, newY, pointIndex);
          selectAndShowItems(Arrays.asList(new Room [] {room}));
        }      
  
        @Override
        public String getPresentationName() {
          return preferences.getLocalizedString(
              PlanController.class, "undoRoomResizeName");
        }      
      };
      this.undoSupport.postEdit(undoableEdit);
    }
  }
  
  /**
   * Posts an undoable operation about <code>room</code> name offset change.
   */
  private void postRoomNameOffset(final Room room, final float oldNameXOffset, 
                                  final float oldNameYOffset) {
    final float newNameXOffset = room.getNameXOffset();
    final float newNameYOffset = room.getNameYOffset();
    if (newNameXOffset != oldNameXOffset
        || newNameYOffset != oldNameYOffset) {
      UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
        @Override
        public void undo() throws CannotUndoException {
          super.undo();
          room.setNameXOffset(oldNameXOffset);
          room.setNameYOffset(oldNameYOffset);
          selectAndShowItems(Arrays.asList(new Room [] {room}));
        }
        
        @Override
        public void redo() throws CannotRedoException {
          super.redo();
          room.setNameXOffset(newNameXOffset);
          room.setNameYOffset(newNameYOffset);
          selectAndShowItems(Arrays.asList(new Room [] {room}));
        }      
  
        @Override
        public String getPresentationName() {
          return preferences.getLocalizedString(
              PlanController.class, "undoRoomNameOffsetName");
        }      
      };
      this.undoSupport.postEdit(undoableEdit);
    }
  }

  /**
   * Posts an undoable operation about <code>room</code> area offset change.
   */
  private void postRoomAreaOffset(final Room room, final float oldAreaXOffset, 
                                  final float oldAreaYOffset) {
    final float newAreaXOffset = room.getAreaXOffset();
    final float newAreaYOffset = room.getAreaYOffset();
    if (newAreaXOffset != oldAreaXOffset
        || newAreaYOffset != oldAreaYOffset) {
      UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
        @Override
        public void undo() throws CannotUndoException {
          super.undo();
          room.setAreaXOffset(oldAreaXOffset);
          room.setAreaYOffset(oldAreaYOffset);
          selectAndShowItems(Arrays.asList(new Room [] {room}));
        }
        
        @Override
        public void redo() throws CannotRedoException {
          super.redo();
          room.setAreaXOffset(newAreaXOffset);
          room.setAreaYOffset(newAreaYOffset);
          selectAndShowItems(Arrays.asList(new Room [] {room}));
        }      
  
        @Override
        public String getPresentationName() {
          return preferences.getLocalizedString(
              PlanController.class, "undoRoomAreaOffsetName");
        }      
      };
      this.undoSupport.postEdit(undoableEdit);
    }
  }

  /**
   * Post to undo support an angle change on <code>piece</code>. 
   */
  private void postPieceOfFurnitureRotation(final HomePieceOfFurniture piece, 
                                            final float oldAngle, 
                                            final boolean oldDoorOrWindowBoundToWall) {
    final float newAngle = piece.getAngle();
    if (newAngle != oldAngle) {
      UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
        @Override
        public void undo() throws CannotUndoException {
          super.undo();
          piece.setAngle(oldAngle);
          if (piece instanceof HomeDoorOrWindow) {
            ((HomeDoorOrWindow)piece).setBoundToWall(oldDoorOrWindowBoundToWall);
          }
          selectAndShowItems(Arrays.asList(new HomePieceOfFurniture [] {piece}));
        }
        
        @Override
        public void redo() throws CannotRedoException {
          super.redo();
          piece.setAngle(newAngle);
          selectAndShowItems(Arrays.asList(new HomePieceOfFurniture [] {piece}));
        }      
  
        @Override
        public String getPresentationName() {
          return preferences.getLocalizedString(
              PlanController.class, "undoPieceOfFurnitureRotationName");
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
          piece.setElevation(oldElevation);
          selectAndShowItems(Arrays.asList(new HomePieceOfFurniture [] {piece}));
        }
        
        @Override
        public void redo() throws CannotRedoException {
          super.redo();
          piece.setElevation(newElevation);
          selectAndShowItems(Arrays.asList(new HomePieceOfFurniture [] {piece}));
        }      
  
        @Override
        public String getPresentationName() {          
          return preferences.getLocalizedString(PlanController.class, 
              oldElevation < newElevation 
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
          piece.setHeight(oldHeight);
          selectAndShowItems(Arrays.asList(new HomePieceOfFurniture [] {piece}));
        }
        
        @Override
        public void redo() throws CannotRedoException {
          super.redo();
          piece.setHeight(newHeight);
          selectAndShowItems(Arrays.asList(new HomePieceOfFurniture [] {piece}));
        }      
  
        @Override
        public String getPresentationName() {
          return preferences.getLocalizedString(
              PlanController.class, "undoPieceOfFurnitureHeightResizeName");
        }      
      };
      this.undoSupport.postEdit(undoableEdit);
    }
  }

  /**
   * Post to undo support a width and depth change on <code>piece</code>. 
   * @param b 
   */
  private void postPieceOfFurnitureWidthAndDepthResize(final HomePieceOfFurniture piece, 
                                          final float oldX, final float oldY,
                                          final float oldWidth, final float oldDepth, 
                                          final boolean oldDoorOrWindowBoundToWall) {
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
          piece.setX(oldX);
          piece.setY(oldY);
          piece.setWidth(oldWidth);
          piece.setDepth(oldDepth);
          if (piece instanceof HomeDoorOrWindow) {
            ((HomeDoorOrWindow)piece).setBoundToWall(oldDoorOrWindowBoundToWall);
          }
          selectAndShowItems(Arrays.asList(new HomePieceOfFurniture [] {piece}));
        }
        
        @Override
        public void redo() throws CannotRedoException {
          super.redo();
          piece.setX(newX);
          piece.setY(newY);
          piece.setWidth(newWidth);
          piece.setDepth(newDepth);
          selectAndShowItems(Arrays.asList(new HomePieceOfFurniture [] {piece}));
        }      
  
        @Override
        public String getPresentationName() {
          return preferences.getLocalizedString(
              PlanController.class, "undoPieceOfFurnitureWidthAndDepthResizeName");
        }      
      };
      this.undoSupport.postEdit(undoableEdit);
    }
  }

  /**
   * Posts an undoable operation about <code>piece</code> name offset change.
   */
  private void postPieceOfFurnitureNameOffset(final HomePieceOfFurniture piece, 
                                              final float oldNameXOffset, 
                                              final float oldNameYOffset) {
    final float newNameXOffset = piece.getNameXOffset();
    final float newNameYOffset = piece.getNameYOffset();
    if (newNameXOffset != oldNameXOffset
        || newNameYOffset != oldNameYOffset) {
      UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
        @Override
        public void undo() throws CannotUndoException {
          super.undo();
          piece.setNameXOffset(oldNameXOffset);
          piece.setNameYOffset(oldNameYOffset);
          selectAndShowItems(Arrays.asList(new HomePieceOfFurniture [] {piece}));
        }
        
        @Override
        public void redo() throws CannotRedoException {
          super.redo();
          piece.setNameXOffset(newNameXOffset);
          piece.setNameYOffset(newNameYOffset);
          selectAndShowItems(Arrays.asList(new HomePieceOfFurniture [] {piece}));
        }      
  
        @Override
        public String getPresentationName() {
          return preferences.getLocalizedString(
              PlanController.class, "undoPieceOfFurnitureNameOffsetName");
        }      
      };
      this.undoSupport.postEdit(undoableEdit);
    }
  }

  /**
   * Posts an undoable operation about <code>dimensionLine</code> resizing.
   */
  private void postDimensionLineResize(final DimensionLine dimensionLine, final float oldX, final float oldY, 
                                       final boolean startPoint, final boolean reversed) {
    final float newX;
    final float newY;
    if (startPoint) {
      newX = dimensionLine.getXStart();
      newY = dimensionLine.getYStart();
    } else {
      newX = dimensionLine.getXEnd();
      newY = dimensionLine.getYEnd();
    }
    if (newX != oldX || newY != oldY || reversed) {
      UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
        @Override
        public void undo() throws CannotUndoException {
          super.undo();
          if (reversed) {
            reverseDimensionLine(dimensionLine);
            moveDimensionLinePoint(dimensionLine, oldX, oldY, !startPoint);
          } else {
            moveDimensionLinePoint(dimensionLine, oldX, oldY, startPoint);
          }
          selectAndShowItems(Arrays.asList(new DimensionLine [] {dimensionLine}));
        }
        
        @Override
        public void redo() throws CannotRedoException {
          super.redo();
          moveDimensionLinePoint(dimensionLine, newX, newY, startPoint);
          if (reversed) {
            reverseDimensionLine(dimensionLine);
          }
          selectAndShowItems(Arrays.asList(new DimensionLine [] {dimensionLine}));
        }      
  
        @Override
        public String getPresentationName() {
          return preferences.getLocalizedString(
              PlanController.class, "undoDimensionLineResizeName");
        }      
      };
      this.undoSupport.postEdit(undoableEdit);
    }
  }
  
  /**
   * Posts an undoable operation about <code>dimensionLine</code> offset change.
   */
  private void postDimensionLineOffset(final DimensionLine dimensionLine, final float oldOffset) {
    final float newOffset = dimensionLine.getOffset();
    if (newOffset != oldOffset) {
      UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
        @Override
        public void undo() throws CannotUndoException {
          super.undo();
          dimensionLine.setOffset(oldOffset);
          selectAndShowItems(Arrays.asList(new DimensionLine [] {dimensionLine}));
        }
        
        @Override
        public void redo() throws CannotRedoException {
          super.redo();
          dimensionLine.setOffset(newOffset);
          selectAndShowItems(Arrays.asList(new DimensionLine [] {dimensionLine}));
        }      
  
        @Override
        public String getPresentationName() {
          return preferences.getLocalizedString(
              PlanController.class, "undoDimensionLineOffsetName");
        }      
      };
      this.undoSupport.postEdit(undoableEdit);
    }
  }

  /**
   * Returns the points of a general path which contains only one path.
   */
  private float [][] getPathPoints(GeneralPath roomPath, 
                                   boolean removeAlignedPoints) {
    List<float []> pathPoints = new ArrayList<float[]>();
    float [] previousPathPoint = null;
    for (PathIterator it = roomPath.getPathIterator(null); !it.isDone(); ) {
      float [] pathPoint = new float[2];
      if (it.currentSegment(pathPoint) != PathIterator.SEG_CLOSE
          && (previousPathPoint == null
              || !Arrays.equals(pathPoint, previousPathPoint))) {
        boolean replacePoint = false;
        if (removeAlignedPoints
            && pathPoints.size() > 1) {
          // Check if pathPoint is aligned with the last line added to pathPoints
          float [] lastLineStartPoint = pathPoints.get(pathPoints.size() - 2);
          float [] lastLineEndPoint = previousPathPoint;
          replacePoint = Line2D.ptLineDistSq(lastLineStartPoint [0], lastLineStartPoint [1], 
              lastLineEndPoint [0], lastLineEndPoint [1], 
              pathPoint [0], pathPoint [1]) < 0.0001;
        } 
        if (replacePoint) {
          pathPoints.set(pathPoints.size() - 1, pathPoint);
        } else {
          pathPoints.add(pathPoint);
        }
        previousPathPoint = pathPoint;
      }
      it.next();
    }      
    
    // Remove last point if it's equal to first point
    if (pathPoints.size() > 1
        && Arrays.equals(pathPoints.get(0), pathPoints.get(pathPoints.size() - 1))) {
      pathPoints.remove(pathPoints.size() - 1);
    }
    
    return pathPoints.toArray(new float [pathPoints.size()][]);
  }

  /**
   * Returns the list of closed paths that may define rooms from 
   * the current set of home walls.
   */
  private List<GeneralPath> getRoomPathsFromWalls() {
    if (this.roomPathsCache == null) {
      // Iterate over all the paths the walls area contains
      List<GeneralPath> roomPaths = new ArrayList<GeneralPath>();
      Area wallsArea = getWallsArea();
      Area insideWallsArea = new Area(wallsArea);
      GeneralPath roomPath = new GeneralPath();
      for (PathIterator it = wallsArea.getPathIterator(null, 0.5f); !it.isDone(); ) {
        float [] roomPoint = new float[2];
        switch (it.currentSegment(roomPoint)) {
          case PathIterator.SEG_MOVETO : 
            roomPath.moveTo(roomPoint [0], roomPoint [1]);
            break;
          case PathIterator.SEG_LINETO : 
            roomPath.lineTo(roomPoint [0], roomPoint [1]);
            break;
          case PathIterator.SEG_CLOSE :
            roomPath.closePath();
            insideWallsArea.add(new Area(roomPath));              
            roomPaths.add(roomPath);
            roomPath = new GeneralPath();
            break;
        }
        it.next();        
      }
      
      this.roomPathsCache = roomPaths;
      this.insideWallsAreaCache = insideWallsArea;
    }
    return this.roomPathsCache;
  }
  
  /**
   * Returns the area that includes walls and inside walls area.
   */
  private Area getInsideWallsArea() {
    if (this.insideWallsAreaCache == null) {
      getRoomPathsFromWalls();
    }
    return this.insideWallsAreaCache;
  }
  
  /**
   * Returns the area covered by walls.
   */
  private Area getWallsArea() {
    if (this.wallsAreaCache == null) {
      // Compute walls area
      Area wallsArea = new Area();
      for (Wall wall : home.getWalls()) {
        wallsArea.add(new Area(getPath(wall.getPoints())));
      }
      this.wallsAreaCache = wallsArea;
    }
    return this.wallsAreaCache;
  }
  
  /**
   * Returns the shape matching the coordinates in <code>points</code> array.
   */
  private GeneralPath getPath(float [][] points) {
    GeneralPath path = new GeneralPath();
    path.moveTo(points [0][0], points [0][1]);
    for (int i = 1; i < points.length; i++) {
      path.lineTo(points [i][0], points [i][1]);
    }
    path.closePath();
    return path;
  }

  /**
   * Returns the path matching a given area.
   */
  private GeneralPath getPath(Area area) {
    GeneralPath path = new GeneralPath();
    float [] point = new float [2];
    for (PathIterator it = area.getPathIterator(null, 0.5f); !it.isDone(); ) {
      switch (it.currentSegment(point)) {
        case PathIterator.SEG_MOVETO : 
          path.moveTo(point [0], point [1]);
          break;
        case PathIterator.SEG_LINETO : 
          path.lineTo(point [0], point [1]);
          break;
      }
      it.next();
    }
    return path;
  }

  /**
   * Stores the walls at start and at end of a given wall. This data are useful
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
   * A point which coordinates are computed with an angle magnetism algorithm.
   */
  private static class PointWithAngleMagnetism {
    private static final int STEP_COUNT = 24; // 15 degrees step 
    private float x;
    private float y;
    
    /**
     * Create a point that applies angle magnetism to point (<code>x</code>,
     * <code>y</code>). Point end coordinates may be different from
     * x or y, to match the closest point belonging to one of the radius of a
     * circle centered at (<code>xStart</code>, <code>yStart</code>), each
     * radius being a multiple of 15 degrees. The length of the line joining
     * (<code>xStart</code>, <code>yStart</code>) to the computed point is 
     * approximated depending on the current <code>unit</code> and scale.
     */
    public PointWithAngleMagnetism(float xStart, float yStart, float x, float y, 
                                   LengthUnit unit, float maxLengthDelta) {
      this.x = x;
      this.y = y;
      if (xStart == x) {
        // Apply magnetism to the length of the line joining start point to magnetized point
        float magnetizedLength = unit.getMagnetizedLength(Math.abs(yStart - y), maxLengthDelta);
        this.y = yStart + (float)(magnetizedLength * Math.signum(y - yStart));
      } else if (yStart == y) {
        // Apply magnetism to the length of the line joining start point to magnetized point
        float magnetizedLength = unit.getMagnetizedLength(Math.abs(xStart - x), maxLengthDelta);
        this.x = xStart + (float)(magnetizedLength * Math.signum(x - xStart));
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
          this.x = xStart + (float)((yStart - y) / tanAngle2);            
        } else {
          magnetismAngle = angle1; 
          this.y = yStart - (float)((x - xStart) * tanAngle1);
        }
        
        // Apply magnetism to the length of the line joining start point 
        // to magnetized point
        float magnetizedLength = unit.getMagnetizedLength((float)Point2D.distance(xStart, yStart, 
            this.x, this.y), maxLengthDelta);
        this.x = xStart + (float)(magnetizedLength * Math.cos(magnetismAngle));            
        this.y = yStart - (float)(magnetizedLength * Math.sin(magnetismAngle));
      }       
    }

    /**
     * Returns the abscissa of end point computed with magnetism.
     */
    float getX() {
      return this.x;
    }

    /**
     * Returns the ordinate of end point computed with magnetism.
     */
    float getY() {
      return this.y;
    }
  }
 
  /**
   * A point which coordinates are equal to the closest point of a wall or a room.
   */
  private class PointMagnetizedToClosestWallOrRoomPoint {
    private float   x;
    private float   y;
    private boolean magnetized;
    
    /**
     * Create a point that applies magnetism to point (<code>x</code>, <code>y</code>).
     * If this point point is close to a point of a wall corner or of a room 
     * within the given <code>margin</code>, the magnetized point will be the closest one.
     */
    public PointMagnetizedToClosestWallOrRoomPoint(Collection<Room> rooms, float x, float y, float margin) {
      // Find the closest wall point to (x,y)
      double smallestDistance = Double.MAX_VALUE;
      for (GeneralPath roomPath : getRoomPathsFromWalls()) {
        smallestDistance = updateMagnetizedPoint(x, y,
            smallestDistance, getPathPoints(roomPath, false));
      }      
      for (Room room : rooms) {
        smallestDistance = updateMagnetizedPoint(x, y,
            smallestDistance, room.getPoints());
      }      
      this.magnetized = smallestDistance <= margin * margin;
      if (!this.magnetized) {
        // Don't magnetism if closest wall point is too far
        this.x = x;
        this.y = y;
      }
    }

    private double updateMagnetizedPoint(float x, float y,
                                         double smallestDistance,
                                         float [][] roomPoints) {
      for (int i = 0; i < roomPoints.length; i++) {
        double distance = Point2D.distanceSq(roomPoints [i][0], roomPoints [i][1], x, y);
        if (distance < smallestDistance) {
          this.x = roomPoints [i][0];
          this.y = roomPoints [i][1];
          smallestDistance = distance;
        }
      }
      return smallestDistance;
    }

    /**
     * Returns the abscissa of end point computed with magnetism.
     */
    public float getX() {
      return this.x;
    }

    /**
     * Returns the ordinate of end point computed with magnetism.
     */
    public float getY() {
      return this.y;
    }
    
    public boolean isMagnetized() {
      return this.magnetized;
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

    public boolean isModificationState() {
      return false;
    }

    public void deleteSelection() {
    }

    public void escape() {
    }

    public void moveSelection(float dx, float dy) {
    }

    public void toggleMagnetism(boolean magnetismToggled) {
    }

    public void setDuplicationActivated(boolean duplicationActivated) {
    }

    public void setEditionActivated(boolean editionActivated) {
    }

    public void updateEditableProperty(EditableProperty editableField, Object value) {
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
   * Abstract state able to manage the transition to other modes.
   */
  private abstract class AbstractModeChangeState extends ControllerState {
    @Override
    public void setMode(Mode mode) {
      if (mode == Mode.SELECTION) {
        setState(getSelectionState());
      } else if (mode == Mode.WALL_CREATION) {
        setState(getWallCreationState());
      } else if (mode == Mode.ROOM_CREATION) {
        setState(getRoomCreationState());
      } else if (mode == Mode.DIMENSION_LINE_CREATION) {
        setState(getDimensionLineCreationState());
      } else if (mode == Mode.LABEL_CREATION) {
        setState(getLabelCreationState());
      } 
    }

    @Override
    public void deleteSelection() {
      deleteItems(home.getSelectedItems());
      // Compute again feedback 
      moveMouse(getXLastMouseMove(), getYLastMouseMove());
    }

    @Override
    public void moveSelection(float dx, float dy) {
      moveAndShowSelectedItems(dx, dy);
      // Compute again feedback 
      moveMouse(getXLastMouseMove(), getYLastMouseMove());
    }
  }
  
  /**
   * Default selection state. This state manages transition to other modes, 
   * the deletion of selected items, and the move of selected items with arrow keys.
   */
  private class SelectionState extends AbstractModeChangeState {
    private final SelectionListener selectionListener = new SelectionListener() {
        public void selectionChanged(SelectionEvent selectionEvent) {
          List<Selectable> selectedItems = home.getSelectedItems();
          getView().setResizeIndicatorVisible(selectedItems.size() == 1
              && (isItemResizable(selectedItems.get(0))
                  || isItemMovable(selectedItems.get(0))));
        }
      };
    
    @Override
    public Mode getMode() {
      return Mode.SELECTION;
    }

    @Override
    public void enter() {
      moveMouse(getXLastMouseMove(), getYLastMouseMove());
      home.addSelectionListener(this.selectionListener);
      this.selectionListener.selectionChanged(null);
    }
    
    @Override
    public void moveMouse(float x, float y) {
      if (getYawRotatedCameraAt(x, y) != null
          || getPitchRotatedCameraAt(x, y) != null) {
        getView().setCursor(PlanView.CursorType.ROTATION);
      } else if (getElevatedCameraAt(x, y) != null) {
        getView().setCursor(PlanView.CursorType.ELEVATION);
      } else if (getRoomNameAt(x, y) != null
          || getRoomAreaAt(x, y) != null
          || getResizedDimensionLineStartAt(x, y) != null
          || getResizedDimensionLineEndAt(x, y) != null
          || getWidthAndDepthResizedPieceOfFurnitureAt(x, y) != null
          || getResizedWallStartAt(x, y) != null
          || getResizedWallEndAt(x, y) != null
          || getResizedRoomAt(x, y) != null) {
        getView().setCursor(PlanView.CursorType.RESIZE);
      } else if (getOffsetDimensionLineAt(x, y) != null
          || getHeightResizedPieceOfFurnitureAt(x, y) != null) {
        getView().setCursor(PlanView.CursorType.HEIGHT);
      } else if (getRotatedPieceOfFurnitureAt(x, y) != null) {
        getView().setCursor(PlanView.CursorType.ROTATION);
      } else if (getElevatedPieceOfFurnitureAt(x, y) != null) {
        getView().setCursor(PlanView.CursorType.ELEVATION);
      } else if (getPieceOfFurnitureNameAt(x, y) != null) {
        getView().setCursor(PlanView.CursorType.RESIZE);
      } else {
        getView().setCursor(PlanView.CursorType.SELECTION);
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
        } else if (getElevatedCameraAt(x, y) != null) {
          setState(getCameraElevationState());
        } else if (getRoomNameAt(x, y) != null) {
          setState(getRoomNameOffsetState());
        } else if (getRoomAreaAt(x, y) != null) {
          setState(getRoomAreaOffsetState());
        } else if (getResizedDimensionLineStartAt(x, y) != null
            || getResizedDimensionLineEndAt(x, y) != null) {
          setState(getDimensionLineResizeState());
        } else if (getWidthAndDepthResizedPieceOfFurnitureAt(x, y) != null) {
          setState(getPieceOfFurnitureResizeState());
        } else if (getResizedWallStartAt(x, y) != null
            || getResizedWallEndAt(x, y) != null) {
          setState(getWallResizeState());
        } else if (getResizedRoomAt(x, y) != null) {
          setState(getRoomResizeState());
        } else if (getOffsetDimensionLineAt(x, y) != null) {
          setState(getDimensionLineOffsetState());
        } else if (getHeightResizedPieceOfFurnitureAt(x, y) != null) {
          setState(getPieceOfFurnitureHeightState());
        } else if (getRotatedPieceOfFurnitureAt(x, y) != null) {
          setState(getPieceOfFurnitureRotationState());
        } else if (getElevatedPieceOfFurnitureAt(x, y) != null) {
          setState(getPieceOfFurnitureElevationState());
        } else if (getPieceOfFurnitureNameAt(x, y) != null) {
          setState(getPieceOfFurnitureNameOffsetState());
        } else {
          Selectable item = getSelectableItemAt(x, y);
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
        Selectable item = getSelectableItemAt(x, y);
        // If shift isn't pressed, and an item is under cursor position
        if (!shiftDown && item != null) {
          // Modify selected item on a double click
          if (item instanceof Wall) {
            modifySelectedWalls();
          } else if (item instanceof HomePieceOfFurniture) {
            modifySelectedFurniture();
          } else if (item instanceof Room) {
            modifySelectedRooms();
          } else if (item instanceof Label) {
            modifySelectedLabels();
          } 
        }
      }
    }
    
    @Override
    public void exit() {
      home.removeSelectionListener(this.selectionListener);
      getView().setResizeIndicatorVisible(false);
    }
  }

  /**
   * Move selection state. This state manages the move of current selected items
   * with mouse and the selection of one item, if mouse isn't moved while button
   * is depressed. If duplication is activated during the move of the mouse,
   * moved items are duplicated first.  
   */
  private class SelectionMoveState extends ControllerState {
    private float                xLastMouseMove;
    private float                yLastMouseMove;
    private boolean              mouseMoved;
    private List<Selectable>     oldSelection;
    private List<Selectable>     movedItems;
    private List<Selectable>     duplicatedItems;
    private HomePieceOfFurniture movedPieceOfFurniture;
    private float                angleMovedPieceOfFurniture;
    private float                depthMovedPieceOfFurniture;
    private float                elevationMovedPieceOfFurniture;
    private float                xMovedPieceOfFurniture;
    private float                yMovedPieceOfFurniture;
    private boolean              movedDoorOrWindowBoundToWall;
    private boolean              magnetismEnabled;

    @Override
    public Mode getMode() {
      return Mode.SELECTION;
    }
    
    @Override
    public boolean isModificationState() {
      return true;
    }
    
    @Override
    public void enter() {
      this.xLastMouseMove = getXLastMousePress();
      this.yLastMouseMove = getYLastMousePress();
      this.mouseMoved = false;
      List<Selectable> selectableItemsUnderCursor = 
          getSelectableItemsAt(getXLastMousePress(), getYLastMousePress());
      this.oldSelection = home.getSelectedItems();
      toggleMagnetism(wasShiftDownLastMousePress());
      // If no selectable item under the cursor belongs to selection
      if (Collections.disjoint(selectableItemsUnderCursor, this.oldSelection)) {
        // Select only the item with highest priority under cursor position
        selectItem(getSelectableItemAt(getXLastMousePress(), getYLastMousePress()));
      }       
      List<Selectable> selectedItems = home.getSelectedItems();
      this.movedItems = new ArrayList<Selectable>(selectedItems.size());      
      for (Selectable item : selectedItems) {
        if (isItemMovable(item)) {
          this.movedItems.add(item);
        }
      }
      if (this.movedItems.size() == 1
          && this.movedItems.get(0) instanceof HomePieceOfFurniture) {
        this.movedPieceOfFurniture = (HomePieceOfFurniture)this.movedItems.get(0);
        this.xMovedPieceOfFurniture = this.movedPieceOfFurniture.getX(); 
        this.yMovedPieceOfFurniture = this.movedPieceOfFurniture.getY(); 
        this.angleMovedPieceOfFurniture = this.movedPieceOfFurniture.getAngle(); 
        this.depthMovedPieceOfFurniture = this.movedPieceOfFurniture.getDepth(); 
        this.elevationMovedPieceOfFurniture = this.movedPieceOfFurniture.getElevation();
        this.movedDoorOrWindowBoundToWall = this.movedPieceOfFurniture instanceof HomeDoorOrWindow 
            && ((HomeDoorOrWindow)this.movedPieceOfFurniture).isBoundToWall();
      }
      this.duplicatedItems = null;
      setDuplicationActivated(wasDuplicationActivatedLastMousePress());
    }
    
    @Override
    public void moveMouse(float x, float y) {      
      if (this.movedPieceOfFurniture != null) {
        // Reset to default piece values and adjust piece of furniture location, angle and depth
        this.movedPieceOfFurniture.setX(this.xMovedPieceOfFurniture);
        this.movedPieceOfFurniture.setY(this.yMovedPieceOfFurniture);
        this.movedPieceOfFurniture.setAngle(this.angleMovedPieceOfFurniture);
        if (this.movedPieceOfFurniture.isResizable()
            && isItemResizable(this.movedPieceOfFurniture)) {
          this.movedPieceOfFurniture.setDepth(this.depthMovedPieceOfFurniture);
        }
        this.movedPieceOfFurniture.setElevation(this.elevationMovedPieceOfFurniture);
        this.movedPieceOfFurniture.move(x - getXLastMousePress(), y - getYLastMousePress());
        if (this.magnetismEnabled) {
          Wall magnetWall = adjustPieceOfFurnitureOnWallAt(this.movedPieceOfFurniture, x, y);
          if (magnetWall != null) {
            getView().setDimensionLinesFeedback(
                getDimensionLinesAlongWall(this.movedPieceOfFurniture, magnetWall));
          } else {
            getView().setDimensionLinesFeedback(null);
          }
          adjustPieceOfFurnitureElevationAt(this.movedPieceOfFurniture);
        } 
      } else { 
        moveItems(this.movedItems, x - this.xLastMouseMove, y - this.yLastMouseMove);
      }
      
      if (!this.mouseMoved) {
        selectItems(this.movedItems);
      }
      getView().makePointVisible(x, y);
      this.xLastMouseMove = x;
      this.yLastMouseMove = y;
      this.mouseMoved = true;
    }
  
    @Override
    public void releaseMouse(float x, float y) {
      if (this.mouseMoved) {
        // Post in undo support a move or duplicate operation if selection isn't a camera 
        if (this.movedItems.size() > 0
            && !(this.movedItems.get(0) instanceof Camera)) {
          if (this.duplicatedItems != null) {
            postItemsDuplication(this.movedItems, this.duplicatedItems);
          } else if (this.movedPieceOfFurniture != null) {
            postPieceOfFurnitureMove(this.movedPieceOfFurniture,
                this.movedPieceOfFurniture.getX() - this.xMovedPieceOfFurniture, 
                this.movedPieceOfFurniture.getY() - this.yMovedPieceOfFurniture,
                this.angleMovedPieceOfFurniture,
                this.depthMovedPieceOfFurniture,
                this.elevationMovedPieceOfFurniture,
                this.movedDoorOrWindowBoundToWall);
          } else {
            postItemsMove(this.movedItems, this.oldSelection,
                this.xLastMouseMove - getXLastMousePress(), 
                this.yLastMouseMove - getYLastMousePress());
          }
        }
      } else {
        // If mouse didn't move, select only the item at (x,y)
        Selectable itemUnderCursor = getSelectableItemAt(x, y);
        if (itemUnderCursor != null) {
          // Select only the item under cursor position
          selectItem(itemUnderCursor);
        }
      }
      // Change the state to SelectionState
      setState(getSelectionState());
    }
  
    @Override
    public void toggleMagnetism(boolean magnetismToggled) {
      // Compute active magnetism
      this.magnetismEnabled = preferences.isMagnetismEnabled()
                              ^ magnetismToggled;
      // Compute again piece move as if mouse moved
      if (this.movedPieceOfFurniture != null) {
        moveMouse(getXLastMouseMove(), getYLastMouseMove());   
        if (!this.magnetismEnabled) {
          getView().deleteFeedback();
        }
      }
    }

    @Override
    public void escape() {
      if (this.duplicatedItems != null) {
        // Delete moved items and select original items
        doDeleteItems(this.movedItems);
        selectItems(this.duplicatedItems);
      } else if (this.mouseMoved) {
        // Put items back to their initial location
        if (this.movedPieceOfFurniture != null) {
          this.movedPieceOfFurniture.setX(this.xMovedPieceOfFurniture);
          this.movedPieceOfFurniture.setY(this.yMovedPieceOfFurniture);
          this.movedPieceOfFurniture.setAngle(this.angleMovedPieceOfFurniture);
          if (this.movedPieceOfFurniture.isResizable()
              && isItemResizable(this.movedPieceOfFurniture)) {
            this.movedPieceOfFurniture.setDepth(this.depthMovedPieceOfFurniture);
          }
          this.movedPieceOfFurniture.setElevation(this.elevationMovedPieceOfFurniture);
          if (this.movedPieceOfFurniture instanceof HomeDoorOrWindow) {
            ((HomeDoorOrWindow)this.movedPieceOfFurniture).setBoundToWall(
                this.movedDoorOrWindowBoundToWall);
          }          
        } else {
          moveItems(this.movedItems, 
              getXLastMousePress() - this.xLastMouseMove, 
              getYLastMousePress() - this.yLastMouseMove);
        }
      }
      // Change the state to SelectionState
      setState(getSelectionState());
    }
    
    @Override
    public void setDuplicationActivated(boolean duplicationActivated) {
      if (this.movedItems.size() > 0
          && !(this.movedItems.get(0) instanceof Camera)) {
        if (duplicationActivated
            && this.duplicatedItems == null) {
          // Duplicate original items and add them to home
          this.duplicatedItems = this.movedItems;          
          this.movedItems = Home.duplicate(this.movedItems);          
          for (Selectable item : this.movedItems) {
            if (item instanceof Wall) {
              home.addWall((Wall)item);
            } else if (item instanceof Room) {
              home.addRoom((Room)item);
            } else if (item instanceof DimensionLine) {
              home.addDimensionLine((DimensionLine)item);
            } else if (item instanceof HomePieceOfFurniture) {
              home.addPieceOfFurniture((HomePieceOfFurniture)item);
            } else if (item instanceof Label) {
              home.addLabel((Label)item);
            }
          }
          
          // Put original items back to their initial location
          if (this.movedPieceOfFurniture != null) {
            this.movedPieceOfFurniture.setX(this.xMovedPieceOfFurniture);
            this.movedPieceOfFurniture.setY(this.yMovedPieceOfFurniture);
            this.movedPieceOfFurniture.setAngle(this.angleMovedPieceOfFurniture);
            if (this.movedPieceOfFurniture.isResizable()
                && isItemResizable(this.movedPieceOfFurniture)) {
              this.movedPieceOfFurniture.setDepth(this.depthMovedPieceOfFurniture);
            }
            this.movedPieceOfFurniture.setElevation(this.elevationMovedPieceOfFurniture);
            this.movedPieceOfFurniture = (HomePieceOfFurniture)this.movedItems.get(0);
          } else {
            moveItems(this.duplicatedItems, 
                getXLastMousePress() - this.xLastMouseMove, 
                getYLastMousePress() - this.yLastMouseMove);
          }

          getView().setCursor(PlanView.CursorType.DUPLICATION);
        } else if (!duplicationActivated
                   && this.duplicatedItems != null) {
          // Delete moved items 
          doDeleteItems(this.movedItems);
          
          // Move original items to the current location
          moveItems(this.duplicatedItems, 
              this.xLastMouseMove - getXLastMousePress(), 
              this.yLastMouseMove - getYLastMousePress());
          this.movedItems = this.duplicatedItems;
          this.duplicatedItems = null;
          if (this.movedPieceOfFurniture != null) {
            this.movedPieceOfFurniture = (HomePieceOfFurniture)this.movedItems.get(0);
          }
          getView().setCursor(PlanView.CursorType.SELECTION);
        }
        
        if (this.mouseMoved) {
          selectItems(this.movedItems);
        }
      }
    }
    
    @Override
    public void exit() {
      getView().deleteFeedback();
      this.movedItems = null;
      this.duplicatedItems = null;
      this.movedPieceOfFurniture = null;
    }
  }

  /**
   * Selection with rectangle state. This state manages selection when mouse
   * press is done outside of an item or when mouse press is done with shift key
   * down.
   */
  private class RectangleSelectionState extends ControllerState {
    private List<Selectable> selectedItemsMousePressed;  
    private boolean          mouseMoved;
  
    @Override
    public Mode getMode() {
      return Mode.SELECTION;
    }

    @Override
    public boolean isModificationState() {
      return true;
    }
    
    @Override
    public void enter() {
      Selectable itemUnderCursor = 
          getSelectableItemAt(getXLastMousePress(), getYLastMousePress());
      // If no item under cursor and shift wasn't down, deselect all
      if (itemUnderCursor == null && !wasShiftDownLastMousePress()) {
        deselectAll();
      } 
      // Store current selection
      this.selectedItemsMousePressed = 
        new ArrayList<Selectable>(home.getSelectedItems());
      this.mouseMoved = false;
    }

    @Override
    public void moveMouse(float x, float y) {
      this.mouseMoved = true;
      updateSelectedItems(getXLastMousePress(), getYLastMousePress(), 
          x, y, this.selectedItemsMousePressed);
      // Update rectangle feedback
      PlanView planView = getView();
      planView.setRectangleFeedback(
          getXLastMousePress(), getYLastMousePress(), x, y);
      planView.makePointVisible(x, y);
    }

    @Override
    public void releaseMouse(float x, float y) {
      // If cursor didn't move
      if (!this.mouseMoved) {
        Selectable itemUnderCursor = getSelectableItemAt(x, y);
        // Toggle selection of the item under cursor 
        if (itemUnderCursor != null) {
          if (this.selectedItemsMousePressed.contains(itemUnderCursor)) {
            this.selectedItemsMousePressed.remove(itemUnderCursor);
          } else {
            // Remove any camera from current selection 
            for (Iterator<Selectable> iter = this.selectedItemsMousePressed.iterator(); iter.hasNext();) {
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
      getView().deleteFeedback();
      this.selectedItemsMousePressed = null;
    }

    /**
     * Updates selection from <code>selectedItemsMousePressed</code> and the
     * items that intersects the rectangle at coordinates (<code>x0</code>,
     * <code>y0</code>) and (<code>x1</code>, <code>y1</code>).
     */
    private void updateSelectedItems(float x0, float y0, 
                                     float x1, float y1,
                                     List<Selectable> selectedItemsMousePressed) {
      List<Selectable> selectedItems;
      boolean shiftDown = wasShiftDownLastMousePress();
      if (shiftDown) {
        selectedItems = new ArrayList<Selectable>(selectedItemsMousePressed);
      } else {
        selectedItems = new ArrayList<Selectable>();
      }
      
      // For all the items that intersect with rectangle
      for (Selectable item : getSelectableItemsIntersectingRectangle(x0, y0, x1, y1)) {
        // Don't let the camera be able to be selected with a rectangle
        if (!(item instanceof Camera)) {
          // If shift was down at mouse press
          if (shiftDown) {
            // Toggle selection of item
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
   * Drag and drop state. This state manages the dragging of items
   * transfered from outside of plan view with the mouse.
   */
  private class DragAndDropState extends ControllerState {
    private float                xLastMouseMove;
    private float                yLastMouseMove;
    private HomePieceOfFurniture draggedPieceOfFurniture;
    private float                xDraggedPieceOfFurniture;
    private float                yDraggedPieceOfFurniture;
    private float                angleDraggedPieceOfFurniture;
    private float                depthDraggedPieceOfFurniture;
    private float                elevationDraggedPieceOfFurniture;
    
    @Override
    public Mode getMode() {
      return Mode.SELECTION;
    }

    @Override
    public boolean isModificationState() {
      // This state is used before a modification is performed
      return false;
    }
    
    @Override
    public void enter() {
      this.xLastMouseMove = 0;
      this.yLastMouseMove = 0;
      getView().setDraggedItemsFeedback(draggedItems);
      if (draggedItems.size() == 1
          && draggedItems.get(0) instanceof HomePieceOfFurniture) {
        this.draggedPieceOfFurniture = (HomePieceOfFurniture)draggedItems.get(0);
        this.xDraggedPieceOfFurniture = this.draggedPieceOfFurniture.getX(); 
        this.yDraggedPieceOfFurniture = this.draggedPieceOfFurniture.getY(); 
        this.angleDraggedPieceOfFurniture = this.draggedPieceOfFurniture.getAngle(); 
        this.depthDraggedPieceOfFurniture = this.draggedPieceOfFurniture.getDepth(); 
        this.elevationDraggedPieceOfFurniture = this.draggedPieceOfFurniture.getElevation(); 
      }
    }

    @Override
    public void moveMouse(float x, float y) {
      List<Selectable> draggedItemsFeedback = new ArrayList<Selectable>(draggedItems);
      // Update in plan view the location of the feedback of the dragged items
      moveItems(draggedItems, x - this.xLastMouseMove, y - this.yLastMouseMove);
      if (this.draggedPieceOfFurniture != null
          && preferences.isMagnetismEnabled()) {
        // Reset to default piece values and adjust piece of furniture location, angle and depth
        this.draggedPieceOfFurniture.setX(this.xDraggedPieceOfFurniture);
        this.draggedPieceOfFurniture.setY(this.yDraggedPieceOfFurniture);
        this.draggedPieceOfFurniture.setAngle(this.angleDraggedPieceOfFurniture);
        if (this.draggedPieceOfFurniture.isResizable()) {
          this.draggedPieceOfFurniture.setDepth(this.depthDraggedPieceOfFurniture);
        }
        this.draggedPieceOfFurniture.setElevation(this.elevationDraggedPieceOfFurniture);
        this.draggedPieceOfFurniture.move(x, y);

        Wall magnetWall = adjustPieceOfFurnitureOnWallAt(this.draggedPieceOfFurniture, x, y);
        if (magnetWall != null) {
          getView().setDimensionLinesFeedback(
              getDimensionLinesAlongWall(this.draggedPieceOfFurniture, magnetWall));
        } else {
          getView().setDimensionLinesFeedback(null);
        }
        adjustPieceOfFurnitureElevationAt(this.draggedPieceOfFurniture);
      } 
      getView().setDraggedItemsFeedback(draggedItemsFeedback);
      this.xLastMouseMove = x;
      this.yLastMouseMove = y;
    }

    @Override
    public void exit() {
      this.draggedPieceOfFurniture = null;
      getView().deleteFeedback();
    }
  }

  /**
   * Wall creation state. This state manages transition to other modes, 
   * and initial wall creation.
   */
  private class WallCreationState extends AbstractModeChangeState {
    @Override
    public Mode getMode() {
      return Mode.WALL_CREATION;
    }

    @Override
    public void enter() {
      getView().setCursor(PlanView.CursorType.DRAW);
    }
    
    @Override
    public void moveMouse(float x, float y) {
      getView().setAlignmentFeedback(Wall.class, null, x, y, false);
    }

    @Override
    public void pressMouse(float x, float y, int clickCount,
                           boolean shiftDown, boolean duplicationActivated) {
      // Change state to WallDrawingState
      setState(getWallDrawingState());
    }

    @Override
    public void setEditionActivated(boolean editionActivated) {
      if (editionActivated) {
        setState(getWallDrawingState());
        PlanController.this.setEditionActivated(editionActivated);
      }
    }
    
    @Override
    public void exit() {
      getView().deleteFeedback();
    }  
  }

  /**
   * Wall modification state.  
   */
  private abstract class AbstractWallState extends ControllerState {
    private String wallLengthToolTipFeedback;
    private String wallAngleToolTipFeedback;
    private String wallThicknessToolTipFeedback;
    
    @Override
    public void enter() {
      this.wallLengthToolTipFeedback = preferences.getLocalizedString(
          PlanController.class, "wallLengthToolTipFeedback");
      this.wallAngleToolTipFeedback = preferences.getLocalizedString(
          PlanController.class, "wallAngleToolTipFeedback");
      this.wallThicknessToolTipFeedback = preferences.getLocalizedString(
          PlanController.class, "wallThicknessToolTipFeedback");
    }
    
    protected String getToolTipFeedbackText(Wall wall) {
      float length = wall.getLength();
      int wallAngle = getWallAngle(wall, length);
      return "<html>" + String.format(this.wallLengthToolTipFeedback, 
          preferences.getLengthUnit().getFormatWithUnit().format(length))
          + "<br>" + String.format(this.wallAngleToolTipFeedback, wallAngle)
          + "<br>" + String.format(this.wallThicknessToolTipFeedback, 
              preferences.getLengthUnit().getFormatWithUnit().format(wall.getThickness()));
    }
    
    /**
     * Returns wall angle in degrees.
     */
    protected Integer getWallAngle(Wall wall) {
      return getWallAngle(wall, wall.getLength());
    }

    private Integer getWallAngle(Wall wall, float wallLength) {
      Wall wallAtStart = wall.getWallAtStart();
      if (wallAtStart != null) {
        float wallAtStartLength = (float)Point2D.distance(
            wallAtStart.getXStart(), wallAtStart.getYStart(), 
            wallAtStart.getXEnd(), wallAtStart.getYEnd());
        if (wallLength != 0 && wallAtStartLength != 0) {
          // Compute the angle between the wall and its wall at start
          float xWallVector = (wall.getXEnd() - wall.getXStart()) / wallLength;
          float yWallVector = (wall.getYEnd() - wall.getYStart()) / wallLength;
          float xWallAtStartVector = (wallAtStart.getXEnd() - wallAtStart.getXStart()) / wallAtStartLength;
          float yWallAtStartVector = (wallAtStart.getYEnd() - wallAtStart.getYStart()) / wallAtStartLength;
          if (wallAtStart.getWallAtStart() == wall) {
            // Reverse wall at start direction
            xWallAtStartVector = -xWallAtStartVector;
            yWallAtStartVector = -yWallAtStartVector;
          }
          int wallAngle = (int)Math.round(180 - Math.toDegrees(Math.atan2(
              yWallVector * xWallAtStartVector - xWallVector * yWallAtStartVector,
              xWallVector * xWallAtStartVector + yWallVector * yWallAtStartVector)));
          if (wallAngle > 180) {
            wallAngle -= 360;
          }
          return wallAngle;
        }
      } 
      if (wallLength == 0) {
        return 0;
      } else {
        return (int)Math.round(Math.toDegrees(Math.atan2(
            wall.getYStart() - wall.getYEnd(), wall.getXEnd() - wall.getXStart())));
      }
    }

    protected void showWallAngleFeedback(Wall wall) {
      Wall wallAtStart = wall.getWallAtStart();
      if (wallAtStart != null) {
        if (wallAtStart.getWallAtStart() == wall) {
          getView().setAngleFeedback(wall.getXStart(), wall.getYStart(), 
              wallAtStart.getXEnd(), wallAtStart.getYEnd(), wall.getXEnd(), wall.getYEnd());
        } else {
          getView().setAngleFeedback(wall.getXStart(), wall.getYStart(), 
              wallAtStart.getXStart(), wallAtStart.getYStart(), 
              wall.getXEnd(), wall.getYEnd());
        }
      }
    }
  }

  /**
   * Wall drawing state. This state manages wall creation at each mouse press. 
   */
  private class WallDrawingState extends AbstractWallState {
    private float            xStart;
    private float            yStart;
    private float            xLastEnd;
    private float            yLastEnd;
    private Wall             wallStartAtStart;
    private Wall             wallEndAtStart;
    private Wall             newWall;
    private Wall             wallStartAtEnd;
    private Wall             wallEndAtEnd;
    private Wall             lastWall;
    private List<Selectable> oldSelection;
    private boolean          oldBasePlanLocked;
    private List<Wall>       newWalls;
    private boolean          magnetismEnabled;
    private long             lastWallCreationTime;
    
    @Override
    public Mode getMode() {
      return Mode.WALL_CREATION;
    }
 
    @Override
    public boolean isModificationState() {
      return true;
    }
    
    @Override
    public void setMode(Mode mode) {
      // Escape current creation and change state to matching mode
      escape();
      if (mode == Mode.SELECTION) {
        setState(getSelectionState());
      } else if (mode == Mode.ROOM_CREATION) {
        setState(getRoomCreationState());
      } else if (mode == Mode.DIMENSION_LINE_CREATION) {
        setState(getDimensionLineCreationState());
      } else if (mode == Mode.LABEL_CREATION) {
        setState(getLabelCreationState());
      } 
    }

    @Override
    public void enter() {
      super.enter();
      this.oldSelection = home.getSelectedItems();
      this.oldBasePlanLocked = home.isBasePlanLocked();
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
      this.lastWallCreationTime = -1;
      deselectAll();
      toggleMagnetism(wasShiftDownLastMousePress());
      PlanView planView = getView();
      planView.setAlignmentFeedback(Wall.class, null, this.xStart, this.yStart, false);
    }

    @Override
    public void moveMouse(float x, float y) {
      PlanView planView = getView();
      // Compute the coordinates where wall end point should be moved
      float xEnd;
      float yEnd;
      if (this.magnetismEnabled) {
        PointWithAngleMagnetism point = new PointWithAngleMagnetism(
            this.xStart, this.yStart, x, y, preferences.getLengthUnit(), planView.getPixelLength());
        xEnd = point.getX();
        yEnd = point.getY();
      } else {
        xEnd = x;
        yEnd = y;
      }

      // If current wall doesn't exist
      if (this.newWall == null) {
        // Create a new one
        this.newWall = createWall(this.xStart, this.yStart, 
            xEnd, yEnd, this.wallStartAtStart, this.wallEndAtStart);
        this.newWalls.add(this.newWall);
      } else {
        // Otherwise update its end point
        this.newWall.setXEnd(xEnd);
        this.newWall.setYEnd(yEnd);
      }         
      planView.setToolTipFeedback(getToolTipFeedbackText(this.newWall), x, y);
      planView.setAlignmentFeedback(Wall.class, this.newWall, xEnd, yEnd, false);
      showWallAngleFeedback(this.newWall);
      
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
        Selectable selectableItem = getSelectableItemAt(x, y);
        if (this.newWalls.size() == 0
            && selectableItem instanceof Room) {
          createWallsAroundRoom((Room)selectableItem);
        } else if (this.lastWall != null) {
          // Join last wall to the selected wall at its end
          joinNewWallEndToWall(this.lastWall, 
              this.wallStartAtEnd, this.wallEndAtEnd);
        }
        validateDrawnWalls();
      } else {
        // Create a new wall only when it will have a length > 0
        if (this.newWall != null
            && this.newWall.getLength() > 0) {
          getView().deleteToolTipFeedback();
          selectItem(this.newWall);
          endWallCreation();
        }
      }
    }

    /**
     * Creates walls around the given <code>room</code>.
     */
    private void createWallsAroundRoom(Room room) {
      if (room.isSingular()) {
        float [][] roomPoints = room.getPoints();
        List<float []> pointsList = new ArrayList<float[]>(Arrays.asList(roomPoints));
        // It points are not clockwise reverse their order
        if (!room.isClockwise()) {
          Collections.reverse(pointsList);
        }
        // Remove equal points 
        for (int i = 0; i < pointsList.size(); ) {
          float [] point = pointsList.get(i);
          float [] nextPoint = pointsList.get((i + 1) % pointsList.size());
          if (point [0] == nextPoint [0]
              && point [1] == nextPoint [1]) {
            pointsList.remove(i);
          } else {
            i++;
          }
        }
        roomPoints = pointsList.toArray(new float [pointsList.size()][]);
        
        float halfWallThickness = preferences.getNewWallThickness() / 2;
        float [][] largerRoomPoints = new float [roomPoints.length][];
        for (int i = 0; i < roomPoints.length; i++) {
          float [] point = roomPoints [i];
          float [] previousPoint = roomPoints [(i + roomPoints.length - 1) % roomPoints.length];
          float [] nextPoint     = roomPoints [(i + 1) % roomPoints.length];
          
          // Compute the angle of the line with a direction orthogonal to line (previousPoint, point)
          double previousAngle = Math.atan2(point [0] - previousPoint [0], previousPoint [1] - point [1]);      
          // Compute the points of the line joining previous and current point
          // at a distance equal to the half wall thickness 
          float deltaX = (float)(Math.cos(previousAngle) * halfWallThickness);
          float deltaY = (float)(Math.sin(previousAngle) * halfWallThickness);
          float [] point1 = {previousPoint [0] - deltaX, previousPoint [1] - deltaY}; 
          float [] point2 = {point [0] - deltaX, point [1] - deltaY};
          
          // Compute the angle of the line with a direction orthogonal to line (point, nextPoint)
          double nextAngle = Math.atan2(nextPoint [0] - point [0], point [1] - nextPoint [1]);      
          // Compute the points of the line joining current and next point
          // at a distance equal to the half wall thickness 
          deltaX = (float)(Math.cos(nextAngle) * halfWallThickness);
          deltaY = (float)(Math.sin(nextAngle) * halfWallThickness);
          float [] point3 = {point [0] - deltaX, point [1] - deltaY}; 
          float [] point4 = {nextPoint [0] - deltaX, nextPoint [1] - deltaY}; 
          
          largerRoomPoints [i] = computeIntersection(point1, point2, point3, point4);
        }

        // Create walls joining points of largerRoomPoints
        Wall lastWall = null;
        for (int i = 0; i < largerRoomPoints.length; i++) {
          float [] point     = largerRoomPoints [i];
          float [] nextPoint = largerRoomPoints [(i + 1) % roomPoints.length];
          Wall wall = createWall(point [0], point [1], nextPoint [0], nextPoint [1], null, lastWall);
          this.newWalls.add(wall);
          lastWall = wall;
        }
        joinNewWallEndToWall(lastWall, this.newWalls.get(0), null);
      }      
    }

    private void validateDrawnWalls() {
      if (this.newWalls.size() > 0) {
        // Post walls creation to undo support
        postCreateWalls(this.newWalls, this.oldSelection, this.oldBasePlanLocked);
        selectItems(this.newWalls);
      }
      // Change state to WallCreationState 
      setState(getWallCreationState());
    }

    private void endWallCreation() {
      this.lastWall = 
      this.wallEndAtStart = this.newWall;
      this.wallStartAtStart = null;
      this.newWall = null;
      this.xStart = this.xLastEnd; 
      this.yStart = this.yLastEnd;
    }

    @Override
    public void setEditionActivated(boolean editionActivated) {
      PlanView planView = getView();
      if (editionActivated) {
        planView.deleteFeedback();
        if (this.newWalls.size() == 0
            && this.wallEndAtStart == null
            && this.wallStartAtStart == null) {
          // Edit xStart and yStart
          planView.setToolTipEditedProperties(new EditableProperty [] {EditableProperty.X,
                                                                       EditableProperty.Y},
              new Object [] {this.xStart, this.yStart},
              this.xStart, this.yStart);
        } else {
          if (this.newWall == null) {
            // May happen if edition is activated after the user clicked to finish one wall 
            createNextWall();            
          }
          // Edit length, angle and thickness        
          planView.setToolTipEditedProperties(new EditableProperty [] {EditableProperty.LENGTH,
                                                                       EditableProperty.ANGLE,
                                                                       EditableProperty.THICKNESS},
              new Object [] {this.newWall.getLength(), 
                             getWallAngle(this.newWall), 
                             this.newWall.getThickness()},
              this.newWall.getXEnd(), this.newWall.getYEnd());
        }
      } else { 
        if (this.newWall == null) {
          // Create a new wall once user entered the start point of the first wall 
          float defaultLength = preferences.getLengthUnit() == LengthUnit.INCH 
              ? LengthUnit.footToCentimeter(10) : 300;
          this.xLastEnd = this.xStart + defaultLength;
          this.yLastEnd = this.yStart;
          this.newWall = createWall(this.xStart, this.yStart, 
              this.xLastEnd, this.yLastEnd, this.wallStartAtStart, this.wallEndAtStart);
          this.newWalls.add(this.newWall);
          // Activate automatically second step to let user enter the 
          // length, angle and thickness of the new wall
          planView.deleteFeedback();
          setEditionActivated(true);
        } else if (System.currentTimeMillis() - this.lastWallCreationTime < 300) {
          // If the user deactivated edition less than 300 ms after activation, 
          // validate drawn walls after removing the last added wall 
          if (this.newWalls.size() > 1) {
            this.newWalls.remove(this.newWall);
            home.deleteWall(this.newWall);
          }
          validateDrawnWalls();
        } else {
          endWallCreation();
          if (this.newWalls.size() > 2 && this.wallStartAtEnd != null) {
            // Join last wall to the first wall at its end and validate drawn walls
            joinNewWallEndToWall(this.lastWall, this.wallStartAtEnd, null);
            validateDrawnWalls();
            return;
          }
          createNextWall();
          // Reactivate automatically second step
          planView.deleteToolTipFeedback();
          setEditionActivated(true);
        }
      }
    }

    private void createNextWall() {
      Wall previousWall = this.wallEndAtStart != null
          ? this.wallEndAtStart
          : this.wallStartAtStart;
      // Create a new wall with an angle equal to previous wall angle - 90°
      double previousWallAngle = Math.PI - Math.atan2(previousWall.getYStart() - previousWall.getYEnd(), 
          previousWall.getXStart() - previousWall.getXEnd());
      previousWallAngle -=  Math.PI / 2;
      float previousWallLength = previousWall.getLength(); 
      this.xLastEnd = (float)(this.xStart + previousWallLength * Math.cos(previousWallAngle));
      this.yLastEnd = (float)(this.yStart - previousWallLength * Math.sin(previousWallAngle));
      this.newWall = createWall(this.xStart, this.yStart, 
          this.xLastEnd, this.yLastEnd, this.wallStartAtStart, previousWall);
      this.newWall.setThickness(previousWall.getThickness());          
      this.newWalls.add(this.newWall);
      this.lastWallCreationTime = System.currentTimeMillis();
      deselectAll();
    }
    
    @Override
    public void updateEditableProperty(EditableProperty editableProperty, Object value) {
      PlanView planView = getView();
      if (this.newWall == null) {
        // Update start point of the first wall
        switch (editableProperty) {
          case X : 
            this.xStart = value != null ? ((Number)value).floatValue() : 0;
            this.xStart = Math.max(-100000f, Math.min(this.xStart, 100000f));
            break;      
          case Y : 
            this.yStart = value != null ? ((Number)value).floatValue() : 0;
            this.yStart = Math.max(-100000f, Math.min(this.yStart, 100000f));
            break;      
        }
        planView.setAlignmentFeedback(Wall.class, null, this.xStart, this.yStart, true);
        planView.makePointVisible(this.xStart, this.yStart);
      } else {
        if (editableProperty == EditableProperty.THICKNESS) {
          float thickness = value != null ? Math.abs(((Number)value).floatValue()) : 0;
          thickness = Math.max(0.01f, Math.min(thickness, 1000));
          this.newWall.setThickness(thickness);
        } else {
          // Update end point of the current wall
          switch (editableProperty) {
            case LENGTH : 
              float length = value != null ? ((Number)value).floatValue() : 0;
              length = Math.max(0.001f, Math.min(length, 100000f));
              double wallAngle = Math.PI - Math.atan2(this.yStart - this.yLastEnd, this.xStart - this.xLastEnd);
              this.xLastEnd = (float)(this.xStart + length * Math.cos(wallAngle));
              this.yLastEnd = (float)(this.yStart - length * Math.sin(wallAngle));
              break;      
            case ANGLE : 
              wallAngle = Math.toRadians(value != null ? ((Number)value).floatValue() : 0);
              if (this.lastWall != null
                  && this.lastWall.getLength() > 0) {
                wallAngle -= Math.atan2(this.lastWall.getYStart() - this.lastWall.getYEnd(), 
                    this.lastWall.getXStart() - this.lastWall.getXEnd());
              }
              float wallLength = this.newWall.getLength();              
              this.xLastEnd = (float)(this.xStart + wallLength * Math.cos(wallAngle));
              this.yLastEnd = (float)(this.yStart - wallLength * Math.sin(wallAngle));
              break;
            default :
              return;
          }

          // Update new wall
          this.newWall.setXEnd(this.xLastEnd);
          this.newWall.setYEnd(this.yLastEnd);
          planView.setAlignmentFeedback(Wall.class, this.newWall, this.xLastEnd, this.yLastEnd, false);
          showWallAngleFeedback(this.newWall);
          // Ensure wall points are visible
          planView.makePointVisible(this.xStart, this.yStart);
          planView.makePointVisible(this.xLastEnd, this.yLastEnd);
          // Search if the free start point of the first wall matches the end point of the current wall
          if (this.newWalls.size() > 2
              && this.newWalls.get(0).getWallAtStart() == null
              && this.newWalls.get(0).containsWallStartAt(this.xLastEnd, this.yLastEnd, 1E-3f)) {
            this.wallStartAtEnd = this.newWalls.get(0);
            selectItem(this.wallStartAtEnd);          
          } else {
            this.wallStartAtEnd = null;
            deselectAll();
          }
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
      validateDrawnWalls();
    }

    @Override
    public void exit() {
      PlanView planView = getView();
      planView.deleteFeedback();
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
    public boolean isModificationState() {
      return true;
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
      PlanView planView = getView();
      planView.setResizeIndicatorVisible(true);
      planView.setToolTipFeedback(getToolTipFeedbackText(this.selectedWall), 
          getXLastMousePress(), getYLastMousePress());
      planView.setAlignmentFeedback(Wall.class, this.selectedWall, this.oldX, this.oldY, false);
      showWallAngleFeedback(this.selectedWall);
    }
    
    @Override
    public void moveMouse(float x, float y) {
      PlanView planView = getView();
      float newX = x - this.deltaXToResizePoint;
      float newY = y - this.deltaYToResizePoint;
      if (this.magnetismEnabled) {
        PointWithAngleMagnetism point = new PointWithAngleMagnetism(
            this.startPoint 
                ? this.selectedWall.getXEnd()
                : this.selectedWall.getXStart(), 
            this.startPoint 
                ? this.selectedWall.getYEnd()
                : this.selectedWall.getYStart(), newX, newY, 
            preferences.getLengthUnit(), planView.getPixelLength());
        newX = point.getX();
        newY = point.getY();
      } 
      moveWallPoint(this.selectedWall, newX, newY, this.startPoint);

      planView.setToolTipFeedback(getToolTipFeedbackText(this.selectedWall), x, y);
      planView.setAlignmentFeedback(Wall.class, this.selectedWall, newX, newY, false);
      showWallAngleFeedback(this.selectedWall);
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
      PlanView planView = getView();
      planView.setResizeIndicatorVisible(false);
      planView.deleteFeedback();
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
    private boolean              doorOrWindowBoundToWall;
    private String               rotationToolTipFeedback;

    @Override
    public Mode getMode() {
      return Mode.SELECTION;
    }
    
    @Override
    public boolean isModificationState() {
      return true;
    }
    
    @Override
    public void enter() {
      this.rotationToolTipFeedback = preferences.getLocalizedString(
          PlanController.class, "cameraElevationToolTipFeedback");
      this.selectedPiece = (HomePieceOfFurniture)home.getSelectedItems().get(0);
      this.angleMousePress = (float)Math.atan2(this.selectedPiece.getY() - getYLastMousePress(), 
          getXLastMousePress() - this.selectedPiece.getX()); 
      this.oldAngle = this.selectedPiece.getAngle();
      this.doorOrWindowBoundToWall = this.selectedPiece instanceof HomeDoorOrWindow 
          && ((HomeDoorOrWindow)this.selectedPiece).isBoundToWall();
      this.magnetismEnabled = preferences.isMagnetismEnabled()
                              ^ wasShiftDownLastMousePress();
      PlanView planView = getView();
      planView.setResizeIndicatorVisible(true);
      planView.setToolTipFeedback(getToolTipFeedbackText(this.oldAngle), 
          getXLastMousePress(), getYLastMousePress());
    }
    
    @Override
    public void moveMouse(float x, float y) {
      if (x != this.selectedPiece.getX() || y != this.selectedPiece.getY()) {
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
        this.selectedPiece.setAngle(newAngle); 
  
        // Ensure point at (x,y) is visible
        PlanView planView = getView();
        planView.makePointVisible(x, y);
        planView.setToolTipFeedback(getToolTipFeedbackText(newAngle), x, y);
      }
    }

    @Override
    public void releaseMouse(float x, float y) {
      postPieceOfFurnitureRotation(this.selectedPiece, this.oldAngle, this.doorOrWindowBoundToWall);
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
      this.selectedPiece.setAngle(this.oldAngle);
      if (this.selectedPiece instanceof HomeDoorOrWindow) {
        ((HomeDoorOrWindow)this.selectedPiece).setBoundToWall(this.doorOrWindowBoundToWall);
      }
      setState(getSelectionState());
    }
    
    @Override
    public void exit() {
      PlanView planView = getView();
      planView.setResizeIndicatorVisible(false);
      planView.deleteFeedback();
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
    private float                deltaYToElevationPoint;
    private HomePieceOfFurniture selectedPiece;
    private float                oldElevation;
    private String               elevationToolTipFeedback;

    @Override
    public Mode getMode() {
      return Mode.SELECTION;
    }
    
    @Override
    public boolean isModificationState() {
      return true;
    }
    
    @Override
    public void enter() {
      this.elevationToolTipFeedback = preferences.getLocalizedString(
          PlanController.class, "cameraElevationToolTipFeedback");
      this.selectedPiece = (HomePieceOfFurniture)home.getSelectedItems().get(0);
      float [] elevationPoint = this.selectedPiece.getPoints() [1];
      this.deltaYToElevationPoint = getYLastMousePress() - elevationPoint [1];
      this.oldElevation = this.selectedPiece.getElevation();
      this.magnetismEnabled = preferences.isMagnetismEnabled()
                              ^ wasShiftDownLastMousePress();
      PlanView planView = getView();
      planView.setResizeIndicatorVisible(true);
      planView.setToolTipFeedback(getToolTipFeedbackText(this.oldElevation), 
          getXLastMousePress(), getYLastMousePress());
    }
    
    @Override
    public void moveMouse(float x, float y) {
      // Compute the new height of the piece 
      PlanView planView = getView();
      float [] topRightPoint = this.selectedPiece.getPoints() [1];
      float deltaY = y - this.deltaYToElevationPoint - topRightPoint[1];
      float newElevation = this.oldElevation - deltaY;
      newElevation = Math.max(newElevation, 0f);
      if (this.magnetismEnabled) {
        newElevation = preferences.getLengthUnit().getMagnetizedLength(newElevation, planView.getPixelLength());
      }

      // Update piece new dimension
      this.selectedPiece.setElevation(newElevation);

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
      this.selectedPiece.setElevation(this.oldElevation);
      setState(getSelectionState());
    }

    @Override
    public void exit() {
      PlanView planView = getView();
      planView.setResizeIndicatorVisible(false);
      planView.deleteFeedback();
      this.selectedPiece = null;
    }  
    
    private String getToolTipFeedbackText(float height) {
      return String.format(this.elevationToolTipFeedback,  
          preferences.getLengthUnit().getFormatWithUnit().format(height));
    }
  }

  /**
   * Furniture height state. This states manages the height resizing of a piece of furniture.
   */
  private class PieceOfFurnitureHeightState extends ControllerState {
    private boolean              magnetismEnabled;
    private float                deltaYToResizePoint;
    private HomePieceOfFurniture selectedPiece;
    private float                oldHeight;
    private String               resizeToolTipFeedback;

    @Override
    public Mode getMode() {
      return Mode.SELECTION;
    }
    
    @Override
    public boolean isModificationState() {
      return true;
    }
    
    @Override
    public void enter() {
      this.resizeToolTipFeedback = preferences.getLocalizedString(
          PlanController.class, "heightResizeToolTipFeedback");
      this.selectedPiece = (HomePieceOfFurniture)home.getSelectedItems().get(0);
      float [] resizePoint = this.selectedPiece.getPoints() [3];
      this.deltaYToResizePoint = getYLastMousePress() - resizePoint [1];
      this.oldHeight = this.selectedPiece.getHeight();
      this.magnetismEnabled = preferences.isMagnetismEnabled()
                              ^ wasShiftDownLastMousePress();
      PlanView planView = getView();
      planView.setResizeIndicatorVisible(true);
      planView.setToolTipFeedback(getToolTipFeedbackText(this.oldHeight), 
          getXLastMousePress(), getYLastMousePress());
    }
    
    @Override
    public void moveMouse(float x, float y) {
      // Compute the new height of the piece 
      PlanView planView = getView();
      float [] bottomLeftPoint = this.selectedPiece.getPoints() [3];
      float deltaY = y - this.deltaYToResizePoint - bottomLeftPoint[1];
      float newHeight = this.oldHeight - deltaY;
      newHeight = Math.max(newHeight, 0f);
      if (this.magnetismEnabled) {
        newHeight = preferences.getLengthUnit().getMagnetizedLength(newHeight, planView.getPixelLength());
      }
      newHeight = Math.max(newHeight, preferences.getLengthUnit().getMinimumLength());

      // Update piece new dimension
      this.selectedPiece.setHeight(newHeight);

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
      this.selectedPiece.setHeight(this.oldHeight);
      setState(getSelectionState());
    }

    @Override
    public void exit() {
      PlanView planView = getView();
      planView.setResizeIndicatorVisible(false);
      planView.deleteFeedback();
      this.selectedPiece = null;
    }  
    
    private String getToolTipFeedbackText(float height) {
      return String.format(this.resizeToolTipFeedback,  
          preferences.getLengthUnit().getFormatWithUnit().format(height));
    }
  }

  /**
   * Furniture resize state. This states manages the resizing of a piece of furniture.
   */
  private class PieceOfFurnitureResizeState extends ControllerState {
    private boolean              magnetismEnabled;
    private float                deltaXToResizePoint;
    private float                deltaYToResizePoint;
    private HomePieceOfFurniture selectedPiece;
    private float []             topLeftPoint;
    private float                oldX;
    private float                oldY;
    private float                oldWidth;
    private float                oldDepth;
    private boolean              doorOrWindowBoundToWall;
    private String               widthResizeToolTipFeedback;
    private String               depthResizeToolTipFeedback;

    @Override
    public Mode getMode() {
      return Mode.SELECTION;
    }
    
    @Override
    public boolean isModificationState() {
      return true;
    }
    
    @Override
    public void enter() {
      this.widthResizeToolTipFeedback = preferences.getLocalizedString(
          PlanController.class, "widthResizeToolTipFeedback");
      this.depthResizeToolTipFeedback = preferences.getLocalizedString(
          PlanController.class, "depthResizeToolTipFeedback");
      this.selectedPiece = (HomePieceOfFurniture)home.getSelectedItems().get(0);
      float [] resizePoint = this.selectedPiece.getPoints() [2];
      this.deltaXToResizePoint = getXLastMousePress() - resizePoint [0];
      this.deltaYToResizePoint = getYLastMousePress() - resizePoint [1];
      this.oldX = this.selectedPiece.getX();
      this.oldY = this.selectedPiece.getY();
      this.oldWidth = this.selectedPiece.getWidth();
      this.oldDepth = this.selectedPiece.getDepth();
      this.topLeftPoint = this.selectedPiece.getPoints() [0];
      this.magnetismEnabled = preferences.isMagnetismEnabled()
                              ^ wasShiftDownLastMousePress();
      this.doorOrWindowBoundToWall = this.selectedPiece instanceof HomeDoorOrWindow 
          && ((HomeDoorOrWindow)this.selectedPiece).isBoundToWall();
      PlanView planView = getView();
      planView.setResizeIndicatorVisible(true);
      planView.setToolTipFeedback(getToolTipFeedbackText(this.oldWidth, this.oldDepth), 
          getXLastMousePress(), getYLastMousePress());
    }
    
    @Override
    public void moveMouse(float x, float y) {
      // Compute the new location and dimension of the piece to let 
      // its bottom right point be at mouse location
      PlanView planView = getView();
      float angle = this.selectedPiece.getAngle();
      double cos = Math.cos(angle); 
      double sin = Math.sin(angle); 
      float deltaX = x - this.deltaXToResizePoint - this.topLeftPoint [0];
      float deltaY = y - this.deltaYToResizePoint - this.topLeftPoint [1];
      float newWidth =  (float)(deltaY * sin + deltaX * cos);
      if (this.magnetismEnabled) {
        newWidth = preferences.getLengthUnit().getMagnetizedLength(newWidth, planView.getPixelLength());
      }
      newWidth = Math.max(newWidth, preferences.getLengthUnit().getMinimumLength());
      
      float newDepth;
      if (!this.doorOrWindowBoundToWall
          || !this.magnetismEnabled) {
        // Update piece depth if it's not a door a window 
        // or if it's a a door a window unbound to a wall when magnetism is enabled
        newDepth = (float)(deltaY * cos - deltaX * sin);
        if (this.magnetismEnabled) {
          newDepth = preferences.getLengthUnit().getMagnetizedLength(newDepth, planView.getPixelLength());
        }
        newDepth = Math.max(newDepth, preferences.getLengthUnit().getMinimumLength());
      } else {
        newDepth = this.oldDepth;
      }

      // Update piece new location
      float newX = (float)(this.topLeftPoint [0] + (newWidth * cos - newDepth * sin) / 2f);
      float newY = (float)(this.topLeftPoint [1] + (newWidth * sin + newDepth * cos) / 2f);
      this.selectedPiece.setX(newX);
      this.selectedPiece.setY(newY);
      // Update piece size
      this.selectedPiece.setDepth(newDepth);
      this.selectedPiece.setWidth(newWidth);
      if (this.doorOrWindowBoundToWall) {
        // Maintain boundToWall flag
        ((HomeDoorOrWindow)this.selectedPiece).setBoundToWall(this.magnetismEnabled);
      }

      // Ensure point at (x,y) is visible
      planView.makePointVisible(x, y);
      planView.setToolTipFeedback(getToolTipFeedbackText(newWidth, newDepth), x, y);
    }

    @Override
    public void releaseMouse(float x, float y) {
      postPieceOfFurnitureWidthAndDepthResize(this.selectedPiece, this.oldX, this.oldY, 
          this.oldWidth, this.oldDepth, this.doorOrWindowBoundToWall);
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
      this.selectedPiece.setX(this.oldX);
      this.selectedPiece.setY(this.oldY);
      this.selectedPiece.setWidth(this.oldWidth);
      this.selectedPiece.setDepth(this.oldDepth);
      if (this.selectedPiece instanceof HomeDoorOrWindow) {
        ((HomeDoorOrWindow)this.selectedPiece).setBoundToWall(this.doorOrWindowBoundToWall);
      }
      setState(getSelectionState());
    }

    @Override
    public void exit() {
      PlanView planView = getView();
      planView.setResizeIndicatorVisible(false);
      planView.deleteFeedback();
      this.selectedPiece = null;
    }  
    
    private String getToolTipFeedbackText(float width, float depth) {
      String toolTipFeedbackText = "<html>" + String.format(this.widthResizeToolTipFeedback,  
          preferences.getLengthUnit().getFormatWithUnit().format(width));
      if (!(this.selectedPiece instanceof HomeDoorOrWindow) 
          || !((HomeDoorOrWindow)this.selectedPiece).isBoundToWall()) {
        toolTipFeedbackText += "<br>" + String.format(this.depthResizeToolTipFeedback,
            preferences.getLengthUnit().getFormatWithUnit().format(depth));
      }
      return toolTipFeedbackText;
    }
  }

  /**
   * Furniture name offset state. This state manages the name offset of a piece of furniture. 
   */
  private class PieceOfFurnitureNameOffsetState extends ControllerState {
    private HomePieceOfFurniture selectedPiece;
    private float                oldNameXOffset;
    private float                oldNameYOffset;
    private float                xLastMouseMove;
    private float                yLastMouseMove;
    
    @Override
    public Mode getMode() {
      return Mode.SELECTION;
    }
    
    @Override
    public boolean isModificationState() {
      return true;
    }
    
    @Override
    public void enter() {
      this.selectedPiece = (HomePieceOfFurniture)home.getSelectedItems().get(0);
      this.oldNameXOffset = this.selectedPiece.getNameXOffset();
      this.oldNameYOffset = this.selectedPiece.getNameYOffset();
      this.xLastMouseMove = getXLastMousePress();
      this.yLastMouseMove = getYLastMousePress();
      PlanView planView = getView();
      planView.setResizeIndicatorVisible(true);
    }
    
    @Override
    public void moveMouse(float x, float y) {
      this.selectedPiece.setNameXOffset(this.selectedPiece.getNameXOffset() + x - this.xLastMouseMove);
      this.selectedPiece.setNameYOffset(this.selectedPiece.getNameYOffset() + y - this.yLastMouseMove);
      this.xLastMouseMove = x;
      this.yLastMouseMove = y;

      // Ensure point at (x,y) is visible
      getView().makePointVisible(x, y);
    }

    @Override
    public void releaseMouse(float x, float y) {
      postPieceOfFurnitureNameOffset(this.selectedPiece, this.oldNameXOffset, this.oldNameYOffset);
      setState(getSelectionState());
    }

    @Override
    public void escape() {
      this.selectedPiece.setNameXOffset(this.oldNameXOffset);
      this.selectedPiece.setNameYOffset(this.oldNameYOffset);
      setState(getSelectionState());
    }

    @Override
    public void exit() {
      getView().setResizeIndicatorVisible(false);
      this.selectedPiece = null;
    }  
  }
  
  /**
   * Camera yaw change state. This states manages the change of the observer camera yaw angle.
   */
  private class CameraYawRotationState extends ControllerState {
    private ObserverCamera selectedCamera;
    private float          oldYaw;
    private float          xLastMouseMove;
    private float          yLastMouseMove;
    private float          angleLastMouseMove;
    private String         rotationToolTipFeedback;

    @Override
    public Mode getMode() {
      return Mode.SELECTION;
    }
    
    @Override
    public boolean isModificationState() {
      return true;
    }
    
    @Override
    public void enter() {
      this.rotationToolTipFeedback = preferences.getLocalizedString(
          PlanController.class, "cameraYawRotationToolTipFeedback");
      this.selectedCamera = (ObserverCamera)home.getSelectedItems().get(0);
      this.oldYaw = this.selectedCamera.getYaw();
      this.xLastMouseMove = getXLastMousePress();
      this.yLastMouseMove = getYLastMousePress();
      this.angleLastMouseMove = (float)Math.atan2(this.selectedCamera.getY() - this.yLastMouseMove, 
          this.xLastMouseMove - this.selectedCamera.getX());
      PlanView planView = getView();
      planView.setResizeIndicatorVisible(true);
      planView.setToolTipFeedback(getToolTipFeedbackText(this.oldYaw), 
          getXLastMousePress(), getYLastMousePress());
    }
    
    @Override
    public void moveMouse(float x, float y) {      
      if (x != this.selectedCamera.getX() || y != this.selectedCamera.getY()) {
        // Compute the new angle of the camera
        float angleMouseMove = (float)Math.atan2(this.selectedCamera.getY() - y, 
            x - this.selectedCamera.getX());
  
        // Compute yaw angle with a delta that takes into account the direction
        // of the rotation (clock wise or counter clock wise) 
        float deltaYaw = angleLastMouseMove - angleMouseMove;
        float orientation = Math.signum((y - this.selectedCamera.getY()) * (this.xLastMouseMove - this.selectedCamera.getX()) 
            - (this.yLastMouseMove - this.selectedCamera.getY()) * (x- this.selectedCamera.getX()));
        if (orientation < 0 && deltaYaw > 0) {
          deltaYaw -= (float)(Math.PI * 2f);
        } else if (orientation > 0 && deltaYaw < 0) {
          deltaYaw += (float)(Math.PI * 2f);
        }  
        
        // Update camera new yaw angle
        float newYaw = this.selectedCamera.getYaw() + deltaYaw;
        this.selectedCamera.setYaw(newYaw); 
  
        getView().setToolTipFeedback(getToolTipFeedbackText(newYaw), x, y);
  
        this.xLastMouseMove = x;
        this.yLastMouseMove = y;      
        this.angleLastMouseMove = angleMouseMove;
      }
    }

    @Override
    public void releaseMouse(float x, float y) {
      setState(getSelectionState());
    }

    @Override
    public void escape() {
      this.selectedCamera.setYaw(this.oldYaw);
      setState(getSelectionState());
    }
    
    @Override
    public void exit() {
      PlanView planView = getView();
      planView.setResizeIndicatorVisible(false);
      planView.deleteFeedback();
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
    public boolean isModificationState() {
      return true;
    }
    
    @Override
    public void enter() {
      this.rotationToolTipFeedback = preferences.getLocalizedString(
          PlanController.class, "cameraPitchRotationToolTipFeedback");
      this.selectedCamera = (ObserverCamera)home.getSelectedItems().get(0);
      this.oldPitch = this.selectedCamera.getPitch();
      PlanView planView = getView();
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
      // Check new angle is between -60° and 90°  
      newPitch = Math.max(newPitch, -(float)Math.PI / 3);
      newPitch = Math.min(newPitch, (float)Math.PI / 36 * 15);
      
      // Update camera pitch angle
      this.selectedCamera.setPitch(newPitch);
      
      getView().setToolTipFeedback(getToolTipFeedbackText(newPitch), x, y);
    }

    @Override
    public void releaseMouse(float x, float y) {
      setState(getSelectionState());
    }

    @Override
    public void escape() {
      this.selectedCamera.setPitch(this.oldPitch);
      setState(getSelectionState());
    }
    
    @Override
    public void exit() {
      PlanView planView = getView();
      planView.setResizeIndicatorVisible(false);
      planView.deleteFeedback();
      this.selectedCamera = null;
    }  

    private String getToolTipFeedbackText(float angle) {
      return String.format(this.rotationToolTipFeedback, 
          Math.round(Math.toDegrees(angle)) % 360);
    }
  }

  /**
   * Camera elevation state. This states manages the change of the observer camera elevation.
   */
  private class CameraElevationState extends ControllerState {
    private ObserverCamera selectedCamera;
    private float          oldElevation;
    private String         cameraElevationToolTipFeedback;
    private String         observerHeightToolTipFeedback;

    @Override
    public Mode getMode() {
      return Mode.SELECTION;
    }
    
    @Override
    public boolean isModificationState() {
      return true;
    }
    
    @Override
    public void enter() {
      this.cameraElevationToolTipFeedback = preferences.getLocalizedString(
          PlanController.class, "cameraElevationToolTipFeedback");
      this.observerHeightToolTipFeedback = preferences.getLocalizedString(
          PlanController.class, "observerHeightToolTipFeedback");
      this.selectedCamera = (ObserverCamera)home.getSelectedItems().get(0);
      this.oldElevation = this.selectedCamera.getZ();
      PlanView planView = getView();
      planView.setResizeIndicatorVisible(true);
      planView.setToolTipFeedback(getToolTipFeedbackText(this.oldElevation), 
          getXLastMousePress(), getYLastMousePress());
    }
    
    @Override
    public void moveMouse(float x, float y) {      
      // Compute the new angle of the camera
      float newElevation = (float)(this.oldElevation - (y - getYLastMousePress()));
      // Check new angle is between -60° and 90°  
      newElevation = Math.max(newElevation, 10 * 14 / 15);
      newElevation = Math.min(newElevation, 1000 * 14 / 15);
      
      // Update camera elevation
      this.selectedCamera.setZ(newElevation);
      
      getView().setToolTipFeedback(getToolTipFeedbackText(newElevation), x, y);
    }

    @Override
    public void releaseMouse(float x, float y) {
      setState(getSelectionState());
    }

    @Override
    public void escape() {
      this.selectedCamera.setZ(this.oldElevation);
      setState(getSelectionState());
    }
    
    @Override
    public void exit() {
      PlanView planView = getView();
      planView.setResizeIndicatorVisible(false);
      planView.deleteFeedback();
      this.selectedCamera = null;
    }  

    private String getToolTipFeedbackText(float elevation) {
      String toolTipFeedbackText = "<html>" + String.format(this.cameraElevationToolTipFeedback,  
          preferences.getLengthUnit().getFormatWithUnit().format(elevation));
      if (elevation >= 70 && elevation <= 218.75f) {
        toolTipFeedbackText += "<br>" + String.format(this.observerHeightToolTipFeedback,
            preferences.getLengthUnit().getFormatWithUnit().format(elevation * 15 / 14));
      }
      return toolTipFeedbackText;
    }
  }

  /**
   * Dimension line creation state. This state manages transition to
   * other modes, and initial dimension line creation.
   */
  private class DimensionLineCreationState extends AbstractModeChangeState {
    @Override
    public Mode getMode() {
      return Mode.DIMENSION_LINE_CREATION;
    }

    @Override
    public void enter() {
      getView().setCursor(PlanView.CursorType.DRAW);
    }

    @Override
    public void moveMouse(float x, float y) {
      getView().setAlignmentFeedback(DimensionLine.class, null, x, y, false);
    }

    @Override
    public void pressMouse(float x, float y, int clickCount,
                           boolean shiftDown, boolean duplicationActivated) {
      // Ignore double clicks (may happen when state is activated returning from DimensionLineDrawingState) 
      if (clickCount == 1) {
        // Change state to DimensionLineDrawingState
        setState(getDimensionLineDrawingState());
      }
    }

    @Override
    public void setEditionActivated(boolean editionActivated) {
      if (editionActivated) {
        setState(getDimensionLineDrawingState());
        PlanController.this.setEditionActivated(editionActivated);
      }
    }

    @Override
    public void exit() {
      getView().deleteFeedback();
    }  
  }

  /**
   * Dimension line drawing state. This state manages dimension line creation at mouse press. 
   */
  private class DimensionLineDrawingState extends ControllerState {
    private float            xStart;
    private float            yStart;
    private boolean          editingStartPoint;
    private DimensionLine    newDimensionLine;
    private List<Selectable> oldSelection;
    private boolean          oldBasePlanLocked;
    private boolean          magnetismEnabled;
    private boolean          offsetChoice;
    
    @Override
    public Mode getMode() {
      return Mode.DIMENSION_LINE_CREATION;
    }
    
    @Override
    public boolean isModificationState() {
      return true;
    }
    
    @Override
    public void setMode(Mode mode) {
      // Escape current creation and change state to matching mode
      escape();
      if (mode == Mode.SELECTION) {
        setState(getSelectionState());
      } else if (mode == Mode.WALL_CREATION) {
        setState(getWallCreationState());
      } else if (mode == Mode.ROOM_CREATION) {
        setState(getRoomCreationState());
      } else if (mode == Mode.LABEL_CREATION) {
        setState(getLabelCreationState());
      } 
    }

    @Override
    public void enter() {
      this.oldSelection = home.getSelectedItems();
      this.oldBasePlanLocked = home.isBasePlanLocked();
      this.xStart = getXLastMousePress();
      this.yStart = getYLastMousePress();
      this.editingStartPoint = false;
      this.offsetChoice = false;
      this.newDimensionLine = null;
      deselectAll();
      toggleMagnetism(wasShiftDownLastMousePress());
      getView().setAlignmentFeedback(DimensionLine.class, 
          null, getXLastMousePress(), getYLastMousePress(), false);
    }

    @Override
    public void moveMouse(float x, float y) {
      PlanView planView = getView();
      planView.deleteFeedback();
      if (this.offsetChoice) {
        float distanceToDimensionLine = (float)Line2D.ptLineDist(
            this.newDimensionLine.getXStart(), this.newDimensionLine.getYStart(), 
            this.newDimensionLine.getXEnd(), this.newDimensionLine.getYEnd(), x, y);
        int relativeCCW = Line2D.relativeCCW(
            this.newDimensionLine.getXStart(), this.newDimensionLine.getYStart(), 
            this.newDimensionLine.getXEnd(), this.newDimensionLine.getYEnd(), x, y);
        this.newDimensionLine.setOffset(
            -Math.signum(relativeCCW) * distanceToDimensionLine);
      } else {
        // Compute the coordinates where dimension line end point should be moved
        float newX;
        float newY;
        if (this.magnetismEnabled) {
          PointWithAngleMagnetism point = new PointWithAngleMagnetism(
              this.xStart, this.yStart, x, y,
              preferences.getLengthUnit(), planView.getPixelLength());
          newX = point.getX();
          newY = point.getY();
        } else {
          newX = x;
          newY = y;
        }
  
        // If current dimension line doesn't exist
        if (this.newDimensionLine == null) {
          // Create a new one
          this.newDimensionLine = createDimensionLine(this.xStart, this.yStart, newX, newY, 0);
        } else {
          // Otherwise update its end points
          if (this.editingStartPoint) {
            this.newDimensionLine.setXStart(newX); 
            this.newDimensionLine.setYStart(newY);
          } else {
            this.newDimensionLine.setXEnd(newX); 
            this.newDimensionLine.setYEnd(newY);
          }
        }         
        updateReversedDimensionLine();
        
        planView.setAlignmentFeedback(DimensionLine.class, 
            this.newDimensionLine, newX, newY, false);
      }
      // Ensure point at (x,y) is visible
      planView.makePointVisible(x, y);
    }

    /**
     * Swaps start and end point of the created dimension line if needed
     * to ensure its text is never upside down.  
     */
    private void updateReversedDimensionLine() {
      double angle = getDimensionLineAngle();
      boolean reverse = angle < -Math.PI / 2 || angle > Math.PI / 2;
      if (reverse ^ this.editingStartPoint) {
        reverseDimensionLine(this.newDimensionLine);
        this.editingStartPoint = !this.editingStartPoint;
      }
    }

    private double getDimensionLineAngle() {
      if (this.newDimensionLine.getLength() == 0) {
        return 0;
      } else {
        if (this.editingStartPoint) {
          return Math.atan2(this.yStart - this.newDimensionLine.getYStart(), 
              this.newDimensionLine.getXStart() - this.xStart);
        } else {
          return Math.atan2(this.yStart - this.newDimensionLine.getYEnd(), 
              this.newDimensionLine.getXEnd() - this.xStart);
        }
      }
    }

    @Override
    public void pressMouse(float x, float y, int clickCount, 
                           boolean shiftDown, boolean duplicationActivated) {
      // Create a new dimension line only when it will have a length > 0
      // meaning after the first mouse move
      if (this.newDimensionLine != null) {
        if (this.offsetChoice) {
          validateDrawnDimensionLine();
        } else {
          // Switch to offset choice
          this.offsetChoice = true;
          PlanView planView = getView();
          planView.setCursor(PlanView.CursorType.HEIGHT);
          planView.deleteFeedback();
        }
      }
    }

    private void validateDrawnDimensionLine() {
      selectItem(this.newDimensionLine);
      // Post dimension line creation to undo support
      postCreateDimensionLines(Arrays.asList(new DimensionLine [] {this.newDimensionLine}), 
          this.oldSelection, this.oldBasePlanLocked);
      this.newDimensionLine = null;
      // Change state to DimensionLineCreationState 
      setState(getDimensionLineCreationState());
    }

    @Override
    public void setEditionActivated(boolean editionActivated) {
      PlanView planView = getView();
      if (editionActivated) {
        planView.deleteFeedback();
        if (this.newDimensionLine == null) {
          // Edit xStart and yStart
          planView.setToolTipEditedProperties(new EditableProperty [] {EditableProperty.X,
                                                                       EditableProperty.Y},
              new Object [] {this.xStart, this.yStart},
              this.xStart, this.yStart);
        } else if (this.offsetChoice) {
          // Edit offset
          planView.setToolTipEditedProperties(new EditableProperty [] {EditableProperty.OFFSET},
              new Object [] {this.newDimensionLine.getOffset()},
              this.newDimensionLine.getXEnd(), this.newDimensionLine.getYEnd());
        } else {
          // Edit length and angle
          planView.setToolTipEditedProperties(new EditableProperty [] {EditableProperty.LENGTH,
                                                                       EditableProperty.ANGLE},
              new Object [] {this.newDimensionLine.getLength(), 
                             (int)Math.round(Math.toDegrees(getDimensionLineAngle()))},
              this.newDimensionLine.getXEnd(), this.newDimensionLine.getYEnd());
        }
      } else { 
        if (this.newDimensionLine == null) {
          // Create a new dimension line once user entered its start point 
          float defaultLength = preferences.getLengthUnit() == LengthUnit.INCH 
              ? LengthUnit.footToCentimeter(3) : 100;
          this.newDimensionLine = createDimensionLine(this.xStart, this.yStart, 
              this.xStart + defaultLength, this.yStart, 0);
          // Activate automatically second step to let user enter the 
          // length and angle of the new dimension line
          planView.deleteFeedback();
          setEditionActivated(true);
        } else if (this.offsetChoice) {
          validateDrawnDimensionLine();
        } else {
          this.offsetChoice = true;
          setEditionActivated(true);
        }
      }
    }

    @Override
    public void updateEditableProperty(EditableProperty editableProperty, Object value) {
      PlanView planView = getView();
      if (this.newDimensionLine == null) {
        // Update start point of the dimension line
        switch (editableProperty) {
          case X : 
            this.xStart = value != null ? ((Number)value).floatValue() : 0;
            this.xStart = Math.max(-100000f, Math.min(this.xStart, 100000f));
            break;      
          case Y : 
            this.yStart = value != null ? ((Number)value).floatValue() : 0;
            this.yStart = Math.max(-100000f, Math.min(this.yStart, 100000f));
            break;      
        }
        planView.setAlignmentFeedback(DimensionLine.class, null, this.xStart, this.yStart, true);
        planView.makePointVisible(this.xStart, this.yStart);
      } else if (this.offsetChoice) {
        if (editableProperty == EditableProperty.OFFSET) {
          // Update new dimension line offset 
          float offset = value != null ? ((Number)value).floatValue() : 0;
          offset = Math.max(-100000f, Math.min(offset, 100000f));
          this.newDimensionLine.setOffset(offset);
        }
      } else {
        float newX;
        float newY;
        // Update end point of the dimension line
        switch (editableProperty) {
          case LENGTH : 
            float length = value != null ? ((Number)value).floatValue() : 0;
            length = Math.max(0.001f, Math.min(length, 100000f));
            double dimensionLineAngle = getDimensionLineAngle();
            newX = (float)(this.xStart + length * Math.cos(dimensionLineAngle));
            newY = (float)(this.yStart - length * Math.sin(dimensionLineAngle));
            break;      
          case ANGLE : 
            dimensionLineAngle = Math.toRadians(value != null ? ((Number)value).floatValue() : 0);
            float dimensionLineLength = this.newDimensionLine.getLength();              
            newX = (float)(this.xStart + dimensionLineLength * Math.cos(dimensionLineAngle));
            newY = (float)(this.yStart - dimensionLineLength * Math.sin(dimensionLineAngle));
            break;
          default :
            return;
        }

        // Update new dimension line
        if (this.editingStartPoint) {
          this.newDimensionLine.setXStart(newX); 
          this.newDimensionLine.setYStart(newY);
        } else {
          this.newDimensionLine.setXEnd(newX); 
          this.newDimensionLine.setYEnd(newY);
        }
        updateReversedDimensionLine();
        planView.setAlignmentFeedback(DimensionLine.class, this.newDimensionLine, newX, newY, false);
        // Ensure dimension line end points are visible
        planView.makePointVisible(this.xStart, this.yStart);
        planView.makePointVisible(newX, newY);
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
      getView().deleteFeedback();
      this.newDimensionLine = null;
      this.oldSelection = null;
    }  
  }

  /**
   * Dimension line resize state. This state manages dimension line resizing. 
   */
  private class DimensionLineResizeState extends ControllerState {
    private DimensionLine selectedDimensionLine;
    private boolean       editingStartPoint;
    private float         oldX;
    private float         oldY;
    private boolean       reversedDimensionLine;
    private float         deltaXToResizePoint;
    private float         deltaYToResizePoint;
    private float         distanceFromResizePointToDimensionBaseLine;
    private boolean       magnetismEnabled;
    
    @Override
    public Mode getMode() {
      return Mode.SELECTION;
    }
    
    @Override
    public boolean isModificationState() {
      return true;
    }
    
    @Override
    public void enter() {
      PlanView planView = getView();
      planView.setResizeIndicatorVisible(true);
      
      this.selectedDimensionLine = (DimensionLine)home.getSelectedItems().get(0);
      this.editingStartPoint = this.selectedDimensionLine 
          == getResizedDimensionLineStartAt(getXLastMousePress(), getYLastMousePress());
      if (this.editingStartPoint) {
        this.oldX = this.selectedDimensionLine.getXStart();
        this.oldY = this.selectedDimensionLine.getYStart();
      } else {
        this.oldX = this.selectedDimensionLine.getXEnd();
        this.oldY = this.selectedDimensionLine.getYEnd();
      }
      this.reversedDimensionLine = false;

      float xResizePoint;
      float yResizePoint;
      // Compute the closest resize point placed on the extension line and the distance 
      // between that point and the dimension line base
      float alpha1 = (float)(this.selectedDimensionLine.getYEnd() - this.selectedDimensionLine.getYStart()) 
          / (this.selectedDimensionLine.getXEnd() - this.selectedDimensionLine.getXStart());
      // If line is vertical
      if (Math.abs(alpha1) > 1E5) {
        xResizePoint = getXLastMousePress();
        if (this.editingStartPoint) {
          yResizePoint = this.selectedDimensionLine.getYStart();
        } else {
          yResizePoint = this.selectedDimensionLine.getYEnd();
        }
      } else if (this.selectedDimensionLine.getYStart() == this.selectedDimensionLine.getYEnd()) {
        if (this.editingStartPoint) {
          xResizePoint = this.selectedDimensionLine.getXStart();
        } else {
          xResizePoint = this.selectedDimensionLine.getXEnd();
        }
        yResizePoint = getYLastMousePress();
      } else {
        float beta1 = getYLastMousePress() - alpha1 * getXLastMousePress();
        float alpha2 = -1 / alpha1;
        float beta2;
        
        if (this.editingStartPoint) {
          beta2 = this.selectedDimensionLine.getYStart() - alpha2 * this.selectedDimensionLine.getXStart();
        } else {
          beta2 = this.selectedDimensionLine.getYEnd() - alpha2 * this.selectedDimensionLine.getXEnd();
        }
        xResizePoint = (beta2 - beta1) / (alpha1 - alpha2);
        yResizePoint = alpha1 * xResizePoint + beta1;
      }

      this.deltaXToResizePoint = getXLastMousePress() - xResizePoint;
      this.deltaYToResizePoint = getYLastMousePress() - yResizePoint;
      if (this.editingStartPoint) {
        this.distanceFromResizePointToDimensionBaseLine = (float)Point2D.distance(xResizePoint, yResizePoint, 
            this.selectedDimensionLine.getXStart(), this.selectedDimensionLine.getYStart());
        planView.setAlignmentFeedback(DimensionLine.class, this.selectedDimensionLine, 
            this.selectedDimensionLine.getXStart(), this.selectedDimensionLine.getYStart(), false);
      } else {
        this.distanceFromResizePointToDimensionBaseLine = (float)Point2D.distance(xResizePoint, yResizePoint, 
            this.selectedDimensionLine.getXEnd(), this.selectedDimensionLine.getYEnd());
        planView.setAlignmentFeedback(DimensionLine.class, this.selectedDimensionLine, 
            this.selectedDimensionLine.getXEnd(), this.selectedDimensionLine.getYEnd(), false);
      }
      toggleMagnetism(wasShiftDownLastMousePress());
    }
    
    @Override
    public void moveMouse(float x, float y) {
      PlanView planView = getView();
      float xResizePoint = x - this.deltaXToResizePoint;
      float yResizePoint = y - this.deltaYToResizePoint;
      if (this.editingStartPoint) {
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
            PointWithAngleMagnetism point = new PointWithAngleMagnetism(
                this.selectedDimensionLine.getXEnd(), 
                this.selectedDimensionLine.getYEnd(), xNewStartPoint, yNewStartPoint,
                preferences.getLengthUnit(), planView.getPixelLength());
            xNewStartPoint = point.getX();
            yNewStartPoint = point.getY();
          } 

          moveDimensionLinePoint(this.selectedDimensionLine, xNewStartPoint, yNewStartPoint, this.editingStartPoint);
          updateReversedDimensionLine();
          planView.setAlignmentFeedback(DimensionLine.class, this.selectedDimensionLine, 
              xNewStartPoint, yNewStartPoint, false);
        } else {
          planView.deleteFeedback();
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
            PointWithAngleMagnetism point = new PointWithAngleMagnetism(
                this.selectedDimensionLine.getXStart(), 
                this.selectedDimensionLine.getYStart(), xNewEndPoint, yNewEndPoint,
                preferences.getLengthUnit(), planView.getPixelLength());
            xNewEndPoint = point.getX();
            yNewEndPoint = point.getY();
          } 

          moveDimensionLinePoint(this.selectedDimensionLine, xNewEndPoint, yNewEndPoint, this.editingStartPoint);
          updateReversedDimensionLine();
          planView.setAlignmentFeedback(DimensionLine.class, this.selectedDimensionLine, 
              xNewEndPoint, yNewEndPoint, false);
        } else {
          planView.deleteFeedback();
        }
      }     

      // Ensure point at (x,y) is visible
      getView().makePointVisible(x, y);
    }

    /**
     * Swaps start and end point of the dimension line if needed
     * to ensure its text is never upside down.  
     */
    private void updateReversedDimensionLine() {
      double angle = getDimensionLineAngle();
      if (angle < -Math.PI / 2 || angle > Math.PI / 2) {
        reverseDimensionLine(this.selectedDimensionLine);
        this.editingStartPoint = !this.editingStartPoint;
        this.reversedDimensionLine = !this.reversedDimensionLine;
      }
    }

    private double getDimensionLineAngle() {
      if (this.selectedDimensionLine.getLength() == 0) {
        return 0;
      } else {
        return Math.atan2(this.selectedDimensionLine.getYStart() - this.selectedDimensionLine.getYEnd(), 
            this.selectedDimensionLine.getXEnd() - this.selectedDimensionLine.getXStart());
      }
    }

    @Override
    public void releaseMouse(float x, float y) {
      postDimensionLineResize(this.selectedDimensionLine, this.oldX, this.oldY, 
          this.editingStartPoint, this.reversedDimensionLine);
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
      if (this.reversedDimensionLine) {
        reverseDimensionLine(this.selectedDimensionLine);
        this.editingStartPoint = !this.editingStartPoint;
      }
      moveDimensionLinePoint(this.selectedDimensionLine, this.oldX, this.oldY, this.editingStartPoint);
      setState(getSelectionState());
    }

    @Override
    public void exit() {
      PlanView planView = getView();
      planView.deleteFeedback();
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
    public boolean isModificationState() {
      return true;
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
      PlanView planView = getView();
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
      this.selectedDimensionLine.setOffset( 
           -Math.signum(relativeCCW) * distanceToDimensionLine);

      // Ensure point at (x,y) is visible
      getView().makePointVisible(x, y);
    }

    @Override
    public void releaseMouse(float x, float y) {
      postDimensionLineOffset(this.selectedDimensionLine, this.oldOffset);
      setState(getSelectionState());
    }

    @Override
    public void escape() {
      this.selectedDimensionLine.setOffset(this.oldOffset);
      setState(getSelectionState());
    }

    @Override
    public void exit() {
      getView().setResizeIndicatorVisible(false);
      this.selectedDimensionLine = null;
    }  
  }

  /**
   * Room creation state. This state manages transition to
   * other modes, and initial room creation.
   */
  private class RoomCreationState extends AbstractModeChangeState {
    private boolean magnetismEnabled;
    
    @Override
    public Mode getMode() {
      return Mode.ROOM_CREATION;
    }

    @Override
    public void enter() {
      getView().setCursor(PlanView.CursorType.DRAW);
      toggleMagnetism(wasShiftDownLastMousePress());
      moveMouse(getXLastMouseMove(), getYLastMouseMove());
    }

    @Override
    public void moveMouse(float x, float y) {
      if (this.magnetismEnabled) {
        // Find the closest wall or room point to current mouse location
        PointMagnetizedToClosestWallOrRoomPoint point = new PointMagnetizedToClosestWallOrRoomPoint(
            home.getRooms(), x, y, PIXEL_WALL_MARGIN / getScale());
        getView().setAlignmentFeedback(Room.class, null, point.getX(), 
            point.getY(), point.isMagnetized());
      } else {
        getView().setAlignmentFeedback(Room.class, null, x, y, false);
      } 
    }

    @Override
    public void pressMouse(float x, float y, int clickCount,
                           boolean shiftDown, boolean duplicationActivated) {
      // Change state to RoomDrawingState
      setState(getRoomDrawingState());
    }

    @Override
    public void setEditionActivated(boolean editionActivated) {
      if (editionActivated) {
        setState(getRoomDrawingState());
        PlanController.this.setEditionActivated(editionActivated);
      }
    }
    
    @Override
    public void toggleMagnetism(boolean magnetismToggled) {
      // Compute active magnetism
      this.magnetismEnabled = preferences.isMagnetismEnabled()
                              ^ magnetismToggled;
      // Compute again feedback point as if mouse moved
      moveMouse(getXLastMouseMove(), getYLastMouseMove());
    }

    @Override
    public void exit() {
      getView().deleteFeedback();
    }  
  }

  /**
   * Room modification state.  
   */
  private abstract class AbstractRoomState extends ControllerState {
    private String roomSideLengthToolTipFeedback;
    private String roomSideAngleToolTipFeedback;
    
    @Override
    public void enter() {
      this.roomSideLengthToolTipFeedback = preferences.getLocalizedString(
          PlanController.class, "roomSideLengthToolTipFeedback");
      this.roomSideAngleToolTipFeedback = preferences.getLocalizedString(
          PlanController.class, "roomSideAngleToolTipFeedback");
    }
    
    protected String getToolTipFeedbackText(Room room, int pointIndex) {
      float length = getRoomSideLength(room, pointIndex);
      int angle = getRoomSideAngle(room, pointIndex);
      return "<html>" + String.format(this.roomSideLengthToolTipFeedback, 
          preferences.getLengthUnit().getFormatWithUnit().format(length))
          + "<br>" + String.format(this.roomSideAngleToolTipFeedback, angle);
    }
    
    protected float getRoomSideLength(Room room, int pointIndex) {
      float [][] points = room.getPoints();
      int previousPointIndex = pointIndex == 0 
          ? points.length - 1
          : pointIndex - 1;
      return (float)Point2D.distance(points [previousPointIndex][0], points [previousPointIndex][1], 
          points [pointIndex][0], points [pointIndex][1]);
    }

    /**
     * Returns room side angle at the given point index in degrees.
     */
    protected Integer getRoomSideAngle(Room room, int pointIndex) {
      float [][] points = room.getPoints();
      int previousPointIndex = pointIndex == 0 
          ? points.length - 1
          : pointIndex - 1;
      int previousPreviousPointIndex = previousPointIndex == 0 
          ? points.length - 1
          : previousPointIndex - 1;
      float sideLength = (float)Point2D.distance(
          points [previousPointIndex][0], points [previousPointIndex][1], 
          points [pointIndex][0], points [pointIndex][1]);
      float previousSideLength = (float)Point2D.distance(
          points [previousPreviousPointIndex][0], points [previousPreviousPointIndex][1],
          points [previousPointIndex][0], points [previousPointIndex][1]);
      if (previousPreviousPointIndex != pointIndex 
          && sideLength != 0 && previousSideLength != 0) {
        // Compute the angle between the side finishing at pointIndex 
        // and the previous side
        float xSideVector = (points [pointIndex][0] - points [previousPointIndex][0]) / sideLength;
        float ySideVector = (points [pointIndex][1] - points [previousPointIndex][1]) / sideLength;
        float xPreviousSideVector = (points [previousPointIndex][0] - points [previousPreviousPointIndex][0]) / previousSideLength;
        float yPreviousSideVector = (points [previousPointIndex][1] - points [previousPreviousPointIndex][1]) / previousSideLength;
        int sideAngle = (int)Math.round(180 - Math.toDegrees(Math.atan2(
            ySideVector * xPreviousSideVector - xSideVector * yPreviousSideVector,
            xSideVector * xPreviousSideVector + ySideVector * yPreviousSideVector)));
        if (sideAngle > 180) {
          sideAngle -= 360;
        }
        return sideAngle;
      }
      if (sideLength == 0) {
        return 0;
      } else {
        return (int)Math.round(Math.toDegrees(Math.atan2(
            points [previousPointIndex][1] - points [pointIndex][1], 
            points [pointIndex][0] - points [previousPointIndex][0])));
      }
    }

    protected void showRoomAngleFeedback(Room room, int pointIndex) {
      float [][] points = room.getPoints();
      if (points.length > 2) {
        int previousPointIndex = pointIndex == 0 
            ? points.length - 1
            : pointIndex - 1;
        int previousPreviousPointIndex = previousPointIndex == 0 
            ? points.length - 1
            : previousPointIndex - 1;
        getView().setAngleFeedback(points [previousPointIndex][0], points [previousPointIndex][1], 
            points [previousPreviousPointIndex][0], points [previousPreviousPointIndex][1], 
            points [pointIndex][0], points [pointIndex][1]);
      }
    }
  }

  /**
   * Room drawing state. This state manages room creation at mouse press. 
   */
  private class RoomDrawingState extends AbstractRoomState {
    private Collection<Room>       rooms;
    private float                  xPreviousPoint;
    private float                  yPreviousPoint;
    private Room                   newRoom;
    private float []               newPoint;
    private List<Selectable>       oldSelection;
    private boolean                oldBasePlanLocked;
    private boolean                magnetismEnabled;
    private long                   lastPointCreationTime;
    
    @Override
    public Mode getMode() {
      return Mode.ROOM_CREATION;
    }
    
    @Override
    public boolean isModificationState() {
      return true;
    }
    
    @Override
    public void setMode(Mode mode) {
      // Escape current creation and change state to matching mode
      escape();
      if (mode == Mode.SELECTION) {
        setState(getSelectionState());
      } else if (mode == Mode.WALL_CREATION) {
        setState(getWallCreationState());
      } else if (mode == Mode.DIMENSION_LINE_CREATION) {
        setState(getDimensionLineCreationState());
      } else if (mode == Mode.LABEL_CREATION) {
        setState(getLabelCreationState());
      } 
    }

    @Override
    public void enter() {
      super.enter();
      this.oldSelection = home.getSelectedItems();
      this.oldBasePlanLocked = home.isBasePlanLocked();
      this.rooms = home.getRooms();
      this.newRoom = null;
      toggleMagnetism(wasShiftDownLastMousePress());
      if (this.magnetismEnabled) {
        // Find the closest wall or room point to current mouse location
        PointMagnetizedToClosestWallOrRoomPoint point = new PointMagnetizedToClosestWallOrRoomPoint(
            this.rooms, getXLastMouseMove(), getYLastMouseMove(), 
            PIXEL_WALL_MARGIN / getScale());
        this.xPreviousPoint = point.getX();
        this.yPreviousPoint = point.getY();
        getView().setAlignmentFeedback(Room.class, null, 
            point.getX(), point.getY(), point.isMagnetized());
        
      } else {
        this.xPreviousPoint = getXLastMousePress();
        this.yPreviousPoint = getYLastMousePress();
        getView().setAlignmentFeedback(Room.class, null, 
            this.xPreviousPoint, this.yPreviousPoint, false);
      }
      deselectAll();
    }

    @Override
    public void moveMouse(float x, float y) {
      PlanView planView = getView();
      // Compute the coordinates where current edit room point should be moved
      float xEnd = x;
      float yEnd = y;
      boolean magnetizedPoint = false;
      if (this.magnetismEnabled) {
        // Find the closest wall or room point to current mouse location
        PointMagnetizedToClosestWallOrRoomPoint point = new PointMagnetizedToClosestWallOrRoomPoint(
            this.rooms, x, y, PIXEL_WALL_MARGIN / getScale());
        magnetizedPoint = point.isMagnetized();
        if (magnetizedPoint) {
          xEnd = point.getX();
          yEnd = point.getY();
        } else {
          // Use magnetism if closest wall point is too far
          PointWithAngleMagnetism pointWithAngleMagnetism = new PointWithAngleMagnetism(
              this.xPreviousPoint, this.yPreviousPoint, x, y, preferences.getLengthUnit(), planView.getPixelLength());
          xEnd = pointWithAngleMagnetism.getX();
          yEnd = pointWithAngleMagnetism.getY();
        }
      } 

      // If current room doesn't exist
      if (this.newRoom == null) {
        // Create a new one
        this.newRoom = createAndSelectRoom(this.xPreviousPoint, this.yPreviousPoint, xEnd, yEnd);
      } else if (this.newPoint != null) {
        // Add a point to current room
        float [][] points = this.newRoom.getPoints();
        this.xPreviousPoint = points [points.length - 1][0];
        this.yPreviousPoint = points [points.length - 1][1]; 
        this.newRoom.addPoint(xEnd, yEnd);
        this.newPoint [0] = xEnd; 
        this.newPoint [1] = yEnd; 
        this.newPoint = null;
      } else {
        // Otherwise update its last point
        this.newRoom.setPoint(xEnd, yEnd, this.newRoom.getPointCount() - 1);
      }         
      planView.setToolTipFeedback(
          getToolTipFeedbackText(this.newRoom, this.newRoom.getPointCount() - 1), x, y);
      planView.setAlignmentFeedback(Room.class, this.newRoom, 
          xEnd, yEnd, magnetizedPoint);
      showRoomAngleFeedback(this.newRoom, this.newRoom.getPointCount() - 1);
      
      // Ensure point at (x,y) is visible
      planView.makePointVisible(x, y);
    }
    
    /**
     * Returns a new room instance with one side between (<code>xStart</code>,
     * <code>yStart</code>) and (<code>xEnd</code>, <code>yEnd</code>) points. 
     * The new room is added to home and selected
     */
    private Room createAndSelectRoom(float xStart, float yStart,
                                     float xEnd, float yEnd) {
      Room newRoom = createRoom(new float [][] {{xStart, yStart}, {xEnd, yEnd}});
      // Let's consider that points outside of home will create  by default a room with no ceiling
      Area insideWallsArea = getInsideWallsArea();
      newRoom.setCeilingVisible(insideWallsArea.contains(xStart, yStart));
      selectItem(newRoom);
      return newRoom;
    }

    @Override
    public void pressMouse(float x, float y, int clickCount, 
                           boolean shiftDown, boolean duplicationActivated) {
      if (clickCount == 2) {
        if (this.newRoom == null) {
          // Try to guess the room that contains the point (x,y)
          this.newRoom = createRoomAt(x, y);
          if (this.newRoom != null) {
            selectItem(this.newRoom);           
          }
        }
        validateDrawnRoom();
      } else {
        endRoomSide();
      }
    }

    private void validateDrawnRoom() {
      if (this.newRoom != null) {
        float [][] points = this.newRoom.getPoints();
        if (points.length < 3) {
          // Delete current created room if it doesn't have more than 2 clicked points
          home.deleteRoom(this.newRoom);
        } else {
          // Post room creation to undo support
          postCreateRooms(Arrays.asList(new Room [] {this.newRoom}), 
              this.oldSelection, this.oldBasePlanLocked);
        }
      }
      // Change state to RoomCreationState 
      setState(getRoomCreationState());
    }

    private void endRoomSide() {
      // Create a new room side only when its length is greater than zero
      if (this.newRoom != null
          && getRoomSideLength(this.newRoom, this.newRoom.getPointCount() - 1) > 0) {        
        this.newPoint = new float [2];
        // Let's consider that any point outside of home will create 
        // by default a room with no ceiling
        if (this.newRoom.isCeilingVisible()) {
          float [][] roomPoints = this.newRoom.getPoints();
          float [] lastPoint = roomPoints [roomPoints.length - 1];
          if (!getInsideWallsArea().contains(lastPoint [0], lastPoint [1])) {
            this.newRoom.setCeilingVisible(false);
          }
        }
      }
    }

    /**
     * Returns the room matching the closed path that contains the point at the given
     * coordinates or <code>null</code> if there's no closed path at this point. 
     */
    private Room createRoomAt(float x, float y) {
      for (GeneralPath roomPath : getRoomPathsFromWalls()) {
        if (roomPath.contains(x, y)) {
          // Add to roomPath a half of the footprint on floor of all the doors and windows 
          // with a elevation equal to zero that intersects with roomPath
          for (HomePieceOfFurniture piece : home.getFurniture()) {
            if (piece.isDoorOrWindow()
                && piece.getElevation() == 0) {
              float [][] doorPoints = piece.getPoints();
              int intersectionCount = 0;
              for (int i = 0; i < doorPoints.length; i++) {
                if (roomPath.contains(doorPoints [i][0], doorPoints [i][1])) {
                  intersectionCount++;
                }                
              }
              if (intersectionCount == 2
                  && doorPoints.length == 4) {
                // Find the intersection of the door with home walls
                Area wallsDoorIntersection = new Area(getWallsArea());
                wallsDoorIntersection.intersect(new Area(getPath(doorPoints)));
                // Reduce the size of intersection to its half
                float [][] intersectionPoints = getPathPoints(getPath(wallsDoorIntersection), false);
                Shape halfDoorPath = null;
                if (intersectionPoints.length == 4) {
                  float epsilon = 0.05f;
                  for (int i = 0; i < intersectionPoints.length; i++) {
                    // Check point in room with rectangle intersection test otherwise we miss some points
                    if (roomPath.intersects(intersectionPoints [i][0] - epsilon / 2, 
                        intersectionPoints [i][1] - epsilon / 2, epsilon, epsilon)) {
                      int inPoint1 = i;
                      int inPoint2;
                      int outPoint1;
                      int outPoint2;
                      if (roomPath.intersects(intersectionPoints [i + 1][0] - epsilon / 2, 
                               intersectionPoints [i + 1][1] - epsilon / 2, epsilon, epsilon)) {
                        inPoint2 = i + 1;
                        outPoint2 = (i + 2) % 4;
                        outPoint1 = (i + 3) % 4;
                      } else {
                        outPoint1 = (i + 1) % 4;
                        outPoint2 = (i + 2) % 4;
                        inPoint2 = (i + 3) % 4;
                      }
                      intersectionPoints [outPoint1][0] = (intersectionPoints [outPoint1][0] 
                          + intersectionPoints [inPoint1][0]) / 2; 
                      intersectionPoints [outPoint1][1] = (intersectionPoints [outPoint1][1] 
                          + intersectionPoints [inPoint1][1]) / 2; 
                      intersectionPoints [outPoint2][0] = (intersectionPoints [outPoint2][0] 
                          + intersectionPoints [inPoint2][0]) / 2; 
                      intersectionPoints [outPoint2][1] = (intersectionPoints [outPoint2][1] 
                          + intersectionPoints [inPoint2][1]) / 2;
                      
                      GeneralPath path = getPath(intersectionPoints);
                      // Enlarge the intersection path to ensure its union with room builds only one path
                      AffineTransform transform = new AffineTransform();
                      Rectangle2D bounds2D = path.getBounds2D();                    
                      transform.translate(bounds2D.getCenterX(), bounds2D.getCenterY());
                      double min = Math.min(bounds2D.getWidth(), bounds2D.getHeight());
                      double scale = (min + epsilon) / min;
                      transform.scale(scale, scale);
                      transform.translate(-bounds2D.getCenterX(), -bounds2D.getCenterY());
                      halfDoorPath = path.createTransformedShape(transform);
                      break;
                    }
                  }
                }                
                
                if (halfDoorPath != null) {
                  Area halfDoorRoomUnion = new Area(halfDoorPath);
                  halfDoorRoomUnion.add(new Area(roomPath));
                  roomPath = getPath(halfDoorRoomUnion);
                }
              }
            }
          }
          
          return createRoom(getPathPoints(roomPath, false));
        }
      }
      return null;
    }

    @Override
    public void setEditionActivated(boolean editionActivated) {
      PlanView planView = getView();
      if (editionActivated) {
        planView.deleteFeedback();
        if (this.newRoom == null) {
          // Edit xStart and yStart
          planView.setToolTipEditedProperties(new EditableProperty [] {EditableProperty.X,
                                                                       EditableProperty.Y},
              new Object [] {this.xPreviousPoint, this.yPreviousPoint},
              this.xPreviousPoint, this.yPreviousPoint);
        } else {
          if (this.newPoint != null) {
            // May happen if edition is activated after the user clicked to add a new point 
            createNextSide();            
          }
          // Edit length and angle
          float [][] points = this.newRoom.getPoints();
          planView.setToolTipEditedProperties(new EditableProperty [] {EditableProperty.LENGTH,
                                                                       EditableProperty.ANGLE},
              new Object [] {getRoomSideLength(this.newRoom, points.length - 1), 
                             getRoomSideAngle(this.newRoom, points.length - 1)},
              points [points.length - 1][0], points [points.length - 1][1]);
        }
      } else { 
        if (this.newRoom == null) {
          // Create a new side once user entered the start point of the room 
          float defaultLength = preferences.getLengthUnit() == LengthUnit.INCH 
              ? LengthUnit.footToCentimeter(10) : 300;
          this.newRoom = createAndSelectRoom(this.xPreviousPoint, this.yPreviousPoint, 
                                             this.xPreviousPoint + defaultLength, this.yPreviousPoint);
          // Activate automatically second step to let user enter the 
          // length and angle of the new side
          planView.deleteFeedback();
          setEditionActivated(true);
        } else if (System.currentTimeMillis() - this.lastPointCreationTime < 300) {
          // If the user deactivated edition less than 300 ms after activation, 
          // escape current side creation
          escape();
        } else {
          endRoomSide();
          float [][] points = this.newRoom.getPoints();
          // If last edited point matches first point validate drawn room 
          if (points.length > 2 
              && this.newRoom.getPointIndexAt(points [points.length - 1][0], points [points.length - 1][1], 0.001f) == 0) {
            // Remove last currently edited point.
            this.newRoom.removePoint(this.newRoom.getPointCount() - 1);
            validateDrawnRoom();
            return;
          }
          createNextSide();
          // Reactivate automatically second step
          planView.deleteToolTipFeedback();
          setEditionActivated(true);
        }
      }
    }

    private void createNextSide() {
      // Add a point to current room
      float [][] points = this.newRoom.getPoints();
      this.xPreviousPoint = points [points.length - 1][0];
      this.yPreviousPoint = points [points.length - 1][1]; 
      // Create a new side with an angle equal to previous side angle - 90°
      double previousSideAngle = Math.PI - Math.atan2(points [points.length - 2][1] - points [points.length - 1][1], 
          points [points.length - 2][0] - points [points.length - 1][0]);
      previousSideAngle -=  Math.PI / 2;
      float previousSideLength = getRoomSideLength(this.newRoom, points.length - 1); 
      this.newRoom.addPoint(
          (float)(this.xPreviousPoint + previousSideLength * Math.cos(previousSideAngle)),
          (float)(this.yPreviousPoint - previousSideLength * Math.sin(previousSideAngle)));
      this.newPoint = null;
      this.lastPointCreationTime = System.currentTimeMillis();
    }
        
    @Override
    public void updateEditableProperty(EditableProperty editableProperty, Object value) {
      PlanView planView = getView();
      if (this.newRoom == null) {
        // Update start point of the first wall
        switch (editableProperty) {
          case X : 
            this.xPreviousPoint = value != null ? ((Number)value).floatValue() : 0;
            this.xPreviousPoint = Math.max(-100000f, Math.min(this.xPreviousPoint, 100000f));
            break;      
          case Y : 
            this.yPreviousPoint = value != null ? ((Number)value).floatValue() : 0;
            this.yPreviousPoint = Math.max(-100000f, Math.min(this.yPreviousPoint, 100000f));
            break;      
        }
        planView.setAlignmentFeedback(Room.class, null, this.xPreviousPoint, this.yPreviousPoint, true);
        planView.makePointVisible(this.xPreviousPoint, this.yPreviousPoint);
      } else {
        float [][] points = this.newRoom.getPoints();
        // Update end point of the current room
        switch (editableProperty) {
          case LENGTH : 
            float length = value != null ? ((Number)value).floatValue() : 0;
            length = Math.max(0.001f, Math.min(length, 100000f));
            double wallAngle = Math.PI - Math.atan2(points [points.length - 2][1] - points [points.length - 1][1], 
                points [points.length - 2][0] - points [points.length - 1][0]);
            this.newRoom.setPoint(
                (float)(points [points.length - 2][0] + length * Math.cos(wallAngle)),
                (float)(points [points.length - 2][1] - length * Math.sin(wallAngle)),
                points.length - 1);
            break;      
          case ANGLE : 
            wallAngle = Math.toRadians(value != null ? ((Number)value).floatValue() : 0);
            if (points.length > 2) {
              wallAngle -= Math.atan2(points [points.length - 3][1] - points [points.length - 2][1], 
                  points [points.length - 3][0] - points [points.length - 2][0]);
            }
            float wallLength = getRoomSideLength(this.newRoom, points.length - 1);              
            this.newRoom.setPoint(
                (float)(points [points.length - 2][0] + wallLength * Math.cos(wallAngle)),
                (float)(points [points.length - 2][1] - wallLength * Math.sin(wallAngle)),
                points.length - 1);
            break;
          default :
            return;
        }

        // Update new room
        planView.setAlignmentFeedback(Room.class, this.newRoom, 
            points [points.length - 1][0], points [points.length - 1][1], false);
        showRoomAngleFeedback(this.newRoom, points.length - 1);
        // Ensure room side points are visible
        planView.makePointVisible(points [points.length - 2][0], points [points.length - 2][1]);
        planView.makePointVisible(points [points.length - 1][0], points [points.length - 1][1]);
      }
    }

    @Override
    public void toggleMagnetism(boolean magnetismToggled) {
      // Compute active magnetism
      this.magnetismEnabled = preferences.isMagnetismEnabled()
                              ^ magnetismToggled;
      // If the new room already exists, 
      // compute again its last point as if mouse moved
      if (this.newRoom != null) {
        moveMouse(getXLastMouseMove(), getYLastMouseMove());
      }
    }

    @Override
    public void escape() {
      if (this.newRoom != null
          && this.newPoint == null) {
        // Remove last currently edited point.
        this.newRoom.removePoint(this.newRoom.getPointCount() - 1);
      }
      validateDrawnRoom();
    }

    @Override
    public void exit() {
      getView().deleteFeedback();
      this.newRoom = null;
      this.newPoint = null;
      this.oldSelection = null;
    }  
  }

  /**
   * Room resize state. This state manages room resizing. 
   */
  private class RoomResizeState extends AbstractRoomState {
    private Collection<Room> rooms;
    private Room             selectedRoom;
    private int              roomPointIndex;
    private float            oldX;
    private float            oldY;
    private float            deltaXToResizePoint;
    private float            deltaYToResizePoint;
    private boolean          magnetismEnabled;
    
    @Override
    public Mode getMode() {
      return Mode.SELECTION;
    }
    
    @Override
    public boolean isModificationState() {
      return true;
    }
    
    @Override
    public void enter() {
      super.enter();
      this.selectedRoom = (Room)home.getSelectedItems().get(0);
      this.rooms = new ArrayList<Room>(home.getRooms());
      this.rooms.remove(this.selectedRoom);
      float margin = PIXEL_MARGIN / getScale();
      this.roomPointIndex = this.selectedRoom.getPointIndexAt( 
          getXLastMousePress(), getYLastMousePress(), margin);
      float [][] roomPoints = this.selectedRoom.getPoints();
      this.oldX = roomPoints [this.roomPointIndex][0];
      this.oldY = roomPoints [this.roomPointIndex][1];
      this.deltaXToResizePoint = getXLastMousePress() - this.oldX;
      this.deltaYToResizePoint = getYLastMousePress() - this.oldY;
      toggleMagnetism(wasShiftDownLastMousePress());
      PlanView planView = getView();
      planView.setResizeIndicatorVisible(true);
      planView.setToolTipFeedback(getToolTipFeedbackText(this.selectedRoom, this.roomPointIndex), 
          getXLastMousePress(), getYLastMousePress());
      showRoomAngleFeedback(this.selectedRoom, this.roomPointIndex);
    }
    
    @Override
    public void moveMouse(float x, float y) {
      PlanView planView = getView();
      float newX = x - this.deltaXToResizePoint;
      float newY = y - this.deltaYToResizePoint;
      boolean magnetizedPoint = false;
      if (this.magnetismEnabled) {
        // Find the closest wall or room point to current mouse location
        PointMagnetizedToClosestWallOrRoomPoint point = new PointMagnetizedToClosestWallOrRoomPoint(
            this.rooms, newX, newY, PIXEL_WALL_MARGIN / getScale());
        magnetizedPoint = point.isMagnetized();
        if (magnetizedPoint) {
          newX = point.getX();
          newY = point.getY();
        } else {
          // Use magnetism if closest wall point is too far
          float [][] roomPoints = this.selectedRoom.getPoints();
          int previousPointIndex = this.roomPointIndex == 0 
              ? roomPoints.length - 1 
              : this.roomPointIndex - 1;
          float xPreviousPoint = roomPoints [previousPointIndex][0];
          float yPreviousPoint = roomPoints [previousPointIndex][1];
          PointWithAngleMagnetism pointWithAngleMagnetism = new PointWithAngleMagnetism(
              xPreviousPoint, yPreviousPoint, newX, newY, preferences.getLengthUnit(), planView.getPixelLength());
          newX = pointWithAngleMagnetism.getX();
          newY = pointWithAngleMagnetism.getY();
        }
      } 
      moveRoomPoint(this.selectedRoom, newX, newY, this.roomPointIndex);

      planView.setToolTipFeedback(getToolTipFeedbackText(this.selectedRoom, this.roomPointIndex), x, y);
      planView.setAlignmentFeedback(Room.class, this.selectedRoom, newX, newY, magnetizedPoint);
      showRoomAngleFeedback(this.selectedRoom, this.roomPointIndex);
      // Ensure point at (x,y) is visible
      planView.makePointVisible(x, y);
    }

    @Override
    public void releaseMouse(float x, float y) {
      postRoomResize(this.selectedRoom, this.oldX, this.oldY, this.roomPointIndex);
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
      moveRoomPoint(this.selectedRoom, this.oldX, this.oldY, this.roomPointIndex);
      setState(getSelectionState());
    }

    @Override
    public void exit() {
      PlanView planView = getView();
      planView.setResizeIndicatorVisible(false);
      planView.deleteFeedback();
      this.selectedRoom = null;
    }  
  }

  /**
   * Room name offset state. This state manages room name offset. 
   */
  private class RoomNameOffsetState extends ControllerState {
    private Room  selectedRoom;
    private float oldNameXOffset;
    private float oldNameYOffset;
    private float xLastMouseMove;
    private float yLastMouseMove;
    
    @Override
    public Mode getMode() {
      return Mode.SELECTION;
    }
    
    @Override
    public boolean isModificationState() {
      return true;
    }
    
    @Override
    public void enter() {
      this.selectedRoom = (Room)home.getSelectedItems().get(0);
      this.oldNameXOffset = this.selectedRoom.getNameXOffset();
      this.oldNameYOffset = this.selectedRoom.getNameYOffset();
      this.xLastMouseMove = getXLastMousePress();
      this.yLastMouseMove = getYLastMousePress();
      PlanView planView = getView();
      planView.setResizeIndicatorVisible(true);
    }
    
    @Override
    public void moveMouse(float x, float y) {
      this.selectedRoom.setNameXOffset(this.selectedRoom.getNameXOffset() + x - this.xLastMouseMove);
      this.selectedRoom.setNameYOffset(this.selectedRoom.getNameYOffset() + y - this.yLastMouseMove);
      this.xLastMouseMove = x;
      this.yLastMouseMove = y;

      // Ensure point at (x,y) is visible
      getView().makePointVisible(x, y);
    }

    @Override
    public void releaseMouse(float x, float y) {
      postRoomNameOffset(this.selectedRoom, this.oldNameXOffset, this.oldNameYOffset);
      setState(getSelectionState());
    }

    @Override
    public void escape() {
      this.selectedRoom.setNameXOffset(this.oldNameXOffset);
      this.selectedRoom.setNameYOffset(this.oldNameYOffset);
      setState(getSelectionState());
    }

    @Override
    public void exit() {
      getView().setResizeIndicatorVisible(false);
      this.selectedRoom = null;
    }  
  }
  
  /**
   * Room area offset state. This state manages room area offset. 
   */
  private class RoomAreaOffsetState extends ControllerState {
    private Room  selectedRoom;
    private float oldAreaXOffset;
    private float oldAreaYOffset;
    private float xLastMouseMove;
    private float yLastMouseMove;
    
    @Override
    public Mode getMode() {
      return Mode.SELECTION;
    }
    
    @Override
    public boolean isModificationState() {
      return true;
    }
    
    @Override
    public void enter() {
      this.selectedRoom = (Room)home.getSelectedItems().get(0);
      this.oldAreaXOffset = this.selectedRoom.getAreaXOffset();
      this.oldAreaYOffset = this.selectedRoom.getAreaYOffset();
      this.xLastMouseMove = getXLastMousePress();
      this.yLastMouseMove = getYLastMousePress();
      PlanView planView = getView();
      planView.setResizeIndicatorVisible(true);
    }
    
    @Override
    public void moveMouse(float x, float y) {
      this.selectedRoom.setAreaXOffset(this.selectedRoom.getAreaXOffset() + x - this.xLastMouseMove);
      this.selectedRoom.setAreaYOffset(this.selectedRoom.getAreaYOffset() + y - this.yLastMouseMove);
      this.xLastMouseMove = x;
      this.yLastMouseMove = y;

      // Ensure point at (x,y) is visible
      getView().makePointVisible(x, y);
    }

    @Override
    public void releaseMouse(float x, float y) {
      postRoomAreaOffset(this.selectedRoom, this.oldAreaXOffset, this.oldAreaYOffset);
      setState(getSelectionState());
    }

    @Override
    public void escape() {
      this.selectedRoom.setAreaXOffset(this.oldAreaXOffset);
      this.selectedRoom.setAreaYOffset(this.oldAreaYOffset);
      setState(getSelectionState());
    }

    @Override
    public void exit() {
      getView().setResizeIndicatorVisible(false);
      this.selectedRoom = null;
    }  
  }

  /**
   * Label creation state. This state manages transition to
   * other modes, and initial label creation.
   */
  private class LabelCreationState extends AbstractModeChangeState {
    @Override
    public Mode getMode() {
      return Mode.LABEL_CREATION;
    }

    @Override
    public void enter() {
      getView().setCursor(PlanView.CursorType.DRAW);
    }

    @Override
    public void pressMouse(float x, float y, int clickCount,
                           boolean shiftDown, boolean duplicationActivated) {
      createLabel(x, y);
    }
  }
}
