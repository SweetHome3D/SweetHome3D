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
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

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
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.ObserverCamera;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.SelectionEvent;
import com.eteks.sweethome3d.model.SelectionListener;
import com.eteks.sweethome3d.model.LengthUnit;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;

/**
 * A MVC controller for the plan view.
 * @author Emmanuel Puybaret
 */
public class PlanController extends FurnitureController implements Controller {
  public enum Property {MODE}
  
  public enum Mode {SELECTION, WALL_CREATION, ROOM_CREATION, DIMENSION_LINE_CREATION}
  
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
  private ResourceBundle              resource;
  private SelectionListener           selectionListener;
  // Possibles states
  private final ControllerState       selectionState;
  private final ControllerState       rectangleSelectionState;
  private final ControllerState       selectionMoveState;
  private final ControllerState       wallCreationState;
  private final ControllerState       newWallState;
  private final ControllerState       wallResizeState;
  private final ControllerState       pieceOfFurnitureRotationState;
  private final ControllerState       pieceOfFurnitureElevationState;
  private final ControllerState       pieceOfFurnitureHeightState;
  private final ControllerState       pieceOfFurnitureResizeState;
  private final ControllerState       cameraYawRotationState;
  private final ControllerState       cameraPitchRotationState;
  private final ControllerState       dimensionLineCreationState;
  private final ControllerState       newDimensionLineState;
  private final ControllerState       dimensionLineResizeState;
  private final ControllerState       dimensionLineOffsetState;
  private final ControllerState       roomCreationState;
  private final ControllerState       newRoomState;
  private final ControllerState       roomResizeState;
  private final ControllerState       roomAreaOffsetState;
  private final ControllerState       roomNameOffsetState;
  // Current state
  private ControllerState             state;
  // Mouse cursor position at last mouse press
  private float                       xLastMousePress;
  private float                       yLastMousePress;
  private boolean                     shiftDownLastMousePress;
  private boolean                     duplicationActivatedLastMousePress;
  private float                       xLastMouseMove;
  private float                       yLastMouseMove;
  private Area                        wallsAreaCache;
  private List<GeneralPath>           roomPathsCache;


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
    this.resource  = ResourceBundle.getBundle(PlanController.class.getName());
    this.propertyChangeSupport = new PropertyChangeSupport(this);
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
    this.roomCreationState = new RoomCreationState();
    this.newRoomState = new NewRoomState();
    this.roomResizeState = new RoomResizeState();
    this.roomAreaOffsetState = new RoomAreaOffsetState();
    this.roomNameOffsetState = new RoomNameOffsetState();
    // Set default state to selectionState
    setState(this.selectionState);
    
    addModelListeners();
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
   * Returns the room creation state.
   */
  protected ControllerState getRoomCreationState() {
    return this.roomCreationState;
  }

  /**
   * Returns the new room state.
   */
  protected ControllerState getNewRoomState() {
    return this.newRoomState;
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
                                      List<Selectable> oldSelection) {
    final Selectable [] oldSelectedItems = 
        oldSelection.toArray(new Selectable [oldSelection.size()]);
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
   * Controls the modification of selected rooms.
   */
  public void modifySelectedRooms() {
    if (!Home.getRoomsSubList(this.home.getSelectedItems()).isEmpty()) {
      new RoomController(this.home, this.preferences, this.viewFactory,
          this.contentManager, this.undoSupport).displayView(getView());
    }
  }
  
  /**
   * Returns the scale in plan view. 
   */
  public float getScale() {
    return getView().getScale();
  }

  /**
   * Controls the scale in plan view. 
   */
  public void setScale(float scale) {
    getView().setScale(scale);
    this.home.setVisualProperty(SCALE_VISUAL_PROPERTY, scale);
  } 
  
  /**
   * Selects all visible objects in home.
   */
  @Override
  public void selectAll() {
    List<Selectable> all = new ArrayList<Selectable>(this.home.getWalls());
    all.addAll(this.home.getRooms());
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
        public void selectionChanged(SelectionEvent selectionEvent) {
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
          roomPathsCache = null;
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
    newWall.setHeight(this.preferences.getNewWallHeight());
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
    float margin = PIXEL_WALL_MARGIN / getView().getScale();
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
    float margin = PIXEL_WALL_MARGIN / getView().getScale();
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
        && selectedItems.get(0) instanceof Wall) {
      Wall wall = (Wall)selectedItems.get(0);
      float margin = PIXEL_MARGIN / getView().getScale();
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
        && selectedItems.get(0) instanceof Wall) {
      Wall wall = (Wall)selectedItems.get(0);
      float margin = PIXEL_MARGIN / getView().getScale();
      if (wall.containsWallEndAt(x, y, margin)) {
        return wall;
      }
    } 
    return null;
  }
  
  /**
   * Returns the selected room with a point at (<code>x</code>, <code>y</code>).
   */
  private Room getResizedRoomAt(float x, float y) {
    List<Selectable> selectedItems = this.home.getSelectedItems();
    if (selectedItems.size() == 1
        && selectedItems.get(0) instanceof Room) {
      Room room = (Room)selectedItems.get(0);
      float margin = PIXEL_MARGIN / getView().getScale();
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
        && selectedItems.get(0) instanceof Room) {
      Room room = (Room)selectedItems.get(0);
      float margin = PIXEL_MARGIN / getView().getScale();
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
        && selectedItems.get(0) instanceof Room) {
      Room room = (Room)selectedItems.get(0);
      float margin = PIXEL_MARGIN / getView().getScale();
      if (room.isAreaVisible() 
          && room.isAreaCenterPointAt(x, y, margin)) {
        return room;
      }
    } 
    return null;
  }
  
  /**
   * Returns the selected dimension line with an end extension line
   * at (<code>x</code>, <code>y</code>).
   */
  private DimensionLine getResizedDimensionLineStartAt(float x, float y) {
    List<Selectable> selectedItems = this.home.getSelectedItems();
    if (selectedItems.size() == 1
        && selectedItems.get(0) instanceof DimensionLine) {
      DimensionLine dimensionLine = (DimensionLine)selectedItems.get(0);
      float margin = PIXEL_MARGIN / getView().getScale();
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
        && selectedItems.get(0) instanceof DimensionLine) {
      DimensionLine dimensionLine = (DimensionLine)selectedItems.get(0);
      float margin = PIXEL_MARGIN / getView().getScale();
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
        && selectedItems.get(0) instanceof DimensionLine) {
      DimensionLine dimensionLine = (DimensionLine)selectedItems.get(0);
      float margin = PIXEL_MARGIN / getView().getScale();
      if (dimensionLine.isMiddlePointAt(x, y, margin)) {
        return dimensionLine;
      }
    } 
    return null;
  }
  
  /**
   * Returns the item at (<code>x</code>, <code>y</code>) point.
   */
  private Selectable getItemAt(float x, float y) {
    float margin = PIXEL_MARGIN / getView().getScale();
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

      for (Room room : this.home.getRooms()) {
        if (room.containsPoint(x, y, margin)) {
          return room;
        }
      }    
      return null;
    }
  }

  /**
   * Returns the items that intersects with the rectangle of (<code>x0</code>,
   * <code>y0</code>), (<code>x1</code>, <code>y1</code>) opposite corners.
   */
  private List<Selectable> getRectangleItems(float x0, float y0, float x1, float y1) {
    List<Selectable> items = new ArrayList<Selectable>();
    updateRectangleItems(items, this.home.getDimensionLines(), x0, y0, x1, y1);
    updateRectangleItems(items, this.home.getRooms(), x0, y0, x1, y1);
    updateRectangleItems(items, this.home.getWalls(), x0, y0, x1, y1);
    ObserverCamera camera = this.home.getObserverCamera();
    if (camera != null && camera.intersectsRectangle(x0, y0, x1, y1)) {
      items.add(camera);
    }
    for (HomePieceOfFurniture piece : this.home.getFurniture()) {
      if (piece.isVisible() && piece.intersectsRectangle(x0, y0, x1, y1)) {
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
    for (Selectable item : selectableItems) {
      if (item.intersectsRectangle(x0, y0, x1, y1)) {
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
        && selectedItems.get(0) instanceof HomePieceOfFurniture) {
      HomePieceOfFurniture piece = (HomePieceOfFurniture)selectedItems.get(0);
      float margin = PIXEL_MARGIN / getView().getScale();
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
        && selectedItems.get(0) instanceof HomePieceOfFurniture) {
      HomePieceOfFurniture piece = (HomePieceOfFurniture)selectedItems.get(0);
      float margin = PIXEL_MARGIN / getView().getScale();
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
      float margin = PIXEL_MARGIN / getView().getScale();
      if (piece.isResizable() 
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
        && selectedItems.get(0) instanceof HomePieceOfFurniture) {
      HomePieceOfFurniture piece = (HomePieceOfFurniture)selectedItems.get(0);
      float margin = PIXEL_MARGIN / getView().getScale();
      if (piece.isResizable() 
          && piece.isBottomRightPointAt(x, y, margin)) {
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
        && selectedItems.get(0) instanceof Camera) {
      ObserverCamera camera = (ObserverCamera)selectedItems.get(0);
      float margin = PIXEL_MARGIN / getView().getScale();
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
        && selectedItems.get(0) instanceof Camera) {
      ObserverCamera camera = (ObserverCamera)selectedItems.get(0);
      float margin = PIXEL_MARGIN / getView().getScale();
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
  public void deleteItems(List<? extends Selectable> items) {
    if (!items.isEmpty()) {
      // Start a compound edit that deletes walls, furniture and dimension lines from home
      this.undoSupport.beginUpdate();
      
      final List<Selectable> deletedItems = new ArrayList<Selectable>(items);      
      // Add a undoable edit that will select the undeleted items at undo
      this.undoSupport.postEdit(new AbstractUndoableEdit() {      
          @Override
          public void undo() throws CannotRedoException {
            super.undo();
            selectAndShowItems(deletedItems);
          }
        });

      deleteFurniture(Home.getFurnitureSubList(items));      

      List<Selectable> deletedOtherObjects = 
          new ArrayList<Selectable>(Home.getWallsSubList(items));
      deletedOtherObjects.addAll(Home.getRoomsSubList(items));
      deletedOtherObjects.addAll(Home.getDimensionLinesSubList(items));
      // First post to undo support that walls, rooms and dimension lines are deleted, 
      // otherwise data about joined walls can't be stored       
      postDeleteItems(deletedOtherObjects);
      // Then delete objects from plan
      doDeleteItems(deletedOtherObjects);

      // End compound edit
      this.undoSupport.endUpdate();
    }          
  }

  /**
   * Posts an undoable delete items operation about <code>deletedItems</code>.
   */
  private void postDeleteItems(final List<? extends Selectable> deletedItems) {
    // Manage walls
    List<Wall> deletedWalls = Home.getWallsSubList(deletedItems);
    // Get joined walls data for undo operation
    final JoinedWall [] joinedDeletedWalls = 
      JoinedWall.getJoinedWalls(deletedWalls);

    // Manage rooms
    List<Room> deletedRooms = Home.getRoomsSubList(deletedItems);
    final Room [] rooms = deletedRooms.toArray(new Room [deletedRooms.size()]);
    
    // Manage dimension lines
    List<DimensionLine> deletedDimensionLines = Home.getDimensionLinesSubList(deletedItems);
    final DimensionLine [] dimensionLines = deletedDimensionLines.toArray(
        new DimensionLine [deletedDimensionLines.size()]);
    
    UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
      @Override
      public void undo() throws CannotUndoException {
        super.undo();
        doAddWalls(joinedDeletedWalls);       
        doAddRooms(rooms);
        doAddDimensionLines(dimensionLines);
        selectAndShowItems(deletedItems);
      }
      
      @Override
      public void redo() throws CannotRedoException {
        super.redo();
        selectItems(deletedItems);
        doDeleteWalls(joinedDeletedWalls);       
        doDeleteRooms(rooms);
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
  private void doDeleteItems(List<Selectable> items) {
    for (Selectable item : items) {
      if (item instanceof Wall) {
        home.deleteWall((Wall)item);
      } else if (item instanceof DimensionLine) {
        home.deleteDimensionLine((DimensionLine)item);
      } else if (item instanceof Room) {
        home.deleteRoom((Room)item);
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
    List<Selectable> selectedItems = this.home.getSelectedItems();
    if (!selectedItems.isEmpty()) {
      moveItems(selectedItems, dx, dy);
      getView().makeSelectionVisible();
      postItemsMove(selectedItems, dx, dy);
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
      } else if (item instanceof HomePieceOfFurniture) {
        HomePieceOfFurniture piece = (HomePieceOfFurniture)item;
        piece.setX(piece.getX() + dx);
        piece.setY(piece.getY() + dy);
      } else if (item instanceof Camera) {
        Camera camera = (Camera)item;
        camera.setX(camera.getX() + dx);
        camera.setY(camera.getY() + dy);
      } else if (item instanceof Room) {
        Room room = (Room)item;
        float [][] points = room.getPoints();
        for (int i = 0; i < points.length; i++) {
          points [i][0] += dx;
          points [i][1] += dy;
        }
        room.setPoints(points);
      } else if (item instanceof DimensionLine) {
        DimensionLine dimensionLine = (DimensionLine)item;
        dimensionLine.setXStart(dimensionLine.getXStart() + dx);
        dimensionLine.setYStart(dimensionLine.getYStart() + dy);
        dimensionLine.setXEnd(dimensionLine.getXEnd() + dx);
        dimensionLine.setYEnd(dimensionLine.getYEnd() + dy);
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
   * Moves <code>room</code> point at the given index to (<code>x</code>, <code>y</code>).
   */
  private void moveRoomPoint(Room room, float x, float y, int pointIndex) {
    float [][] points = room.getPoints();
    points [pointIndex][0] = x;
    points [pointIndex][1] = y;
    room.setPoints(points);
  }
  
  /**
   * Moves <code>dimensionLine</code> start point to (<code>x</code>, <code>y</code>)
   * if <code>startPoint</code> is true or <code>dimensionLine</code> end point 
   * to (<code>x</code>, <code>y</code>) if <code>startPoint</code> is false.
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
   * Selects <code>items</code> and make them visible at screen.
   */
  private void selectAndShowItems(List<? extends Selectable> items) {
    selectItems(items);
    getView().makeSelectionVisible();
  }
  
  /**
   * Selects <code>items</code>.
   */
  private void selectItems(List<? extends Selectable> items) {
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
  private void postAddWalls(List<Wall> newWalls, List<Selectable> oldSelection) {
    if (newWalls.size() > 0) {
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
  private void doDeleteWalls(JoinedWall [] joinedDeletedWalls) {
    for (JoinedWall joinedWall : joinedDeletedWalls) {
      this.home.deleteWall(joinedWall.getWall());
    }
  }

  /**
   * Add <code>newRooms</code> to home and post an undoable new room line operation.
   */
  public void addRooms(List<Room> newRooms) {
    for (Room room : newRooms) {
      this.home.addRoom(room);
    }
    postAddRooms(newRooms, this.home.getSelectedItems());
  }
  
  /**
   * Posts an undoable new room operation, about <code>newRooms</code>.
   */
  private void postAddRooms(List<Room> newRooms, 
                            List<Selectable> oldSelection) {
    if (newRooms.size() > 0) {
      final Room [] rooms = newRooms.toArray(
          new Room [newRooms.size()]);
      final Selectable [] oldSelectedItems = 
          oldSelection.toArray(new Selectable [oldSelection.size()]);
      UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
        @Override
        public void undo() throws CannotUndoException {
          super.undo();
          doDeleteRooms(rooms);
          selectAndShowItems(Arrays.asList(oldSelectedItems));
        }
        
        @Override
        public void redo() throws CannotRedoException {
          super.redo();
          doAddRooms(rooms);       
          selectAndShowItems(Arrays.asList(rooms));
        }      
  
        @Override
        public String getPresentationName() {
          return resource.getString("undoAddRoomsName");
        }      
      };
      this.undoSupport.postEdit(undoableEdit);
    }
  }

  /**
   * Adds the <code>rooms</code> to plan component.
   */
  private void doAddRooms(Room [] rooms) {
    for (Room room : rooms) {
      this.home.addRoom(room);
    }
  }
  
  /**
   * Deletes <code>rooms</code>.
   */
  private void doDeleteRooms(Room [] rooms) {
    for (Room room : rooms) {
      this.home.deleteRoom(room);
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
  private void postAddDimensionLines(List<DimensionLine> newDimensionLines, 
                                     List<Selectable> oldSelection) {
    if (newDimensionLines.size() > 0) {
      final DimensionLine [] dimensionLines = newDimensionLines.toArray(
          new DimensionLine [newDimensionLines.size()]);
      final Selectable [] oldSelectedItems = 
          oldSelection.toArray(new Selectable [oldSelection.size()]);
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
  private void postItemsMove(List<? extends Selectable> movedItems, 
                             final float dx, final float dy) {
    if (dx != 0 || dy != 0) {
      // Store the moved walls in an array
      final Selectable [] itemsArray = 
        movedItems.toArray(new Selectable [movedItems.size()]);
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
  private void doMoveAndShowItems(Selectable [] movedItems, 
                                  float dx, float dy) {
    List<Selectable> itemsList = Arrays.asList(movedItems);
    moveItems(itemsList, dx, dy);   
    selectAndShowItems(itemsList);
  }

  /**
   * Posts an undoable operation about duplication <code>items</code>.
   */
  public void postItemsDuplication(final List<Selectable> items,
                                   final List<Selectable> oldSelectedItems) {
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
    List<Selectable> emptyWallList = Collections.emptyList();
    postAddWalls(Home.getWallsSubList(items), emptyWallList);
    List<Selectable> emptyRoomList = Collections.emptyList();
    postAddRooms(Home.getRoomsSubList(items), emptyRoomList);
    List<Selectable> emptyDimensionLineList = Collections.emptyList();
    postAddDimensionLines(Home.getDimensionLinesSubList(items), emptyDimensionLineList);

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
          return resource.getString("undoRoomResizeName");
        }      
      };
      this.undoSupport.postEdit(undoableEdit);
    }
  }
  
  /**
   * Posts an undoable operation about <code>room</code> name offset change.
   */
  public void postRoomNameOffset(final Room room, final float oldNameXOffset, 
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
          return resource.getString("undoRoomNameOffsetName");
        }      
      };
      this.undoSupport.postEdit(undoableEdit);
    }
  }

  /**
   * Posts an undoable operation about <code>room</code> area offset change.
   */
  public void postRoomAreaOffset(final Room room, final float oldAreaXOffset, 
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
          return resource.getString("undoRoomAreaOffsetName");
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
          piece.setAngle(oldAngle);
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
          piece.setX(oldX);
          piece.setY(oldY);
          piece.setWidth(oldWidth);
          piece.setDepth(oldDepth);
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
          return resource.getString("undoDimensionLineOffsetName");
        }      
      };
      this.undoSupport.postEdit(undoableEdit);
    }
  }

  /**
   * Returns the points of a general path which contains only one path.
   */
  private float [][] getPathPoints(GeneralPath roomPath) {
    List<float []> pathPoints = new ArrayList<float[]>();
    for (PathIterator it = roomPath.getPathIterator(null); !it.isDone(); ) {
      float [] pathPoint = new float[2];
      if (it.currentSegment(pathPoint) != PathIterator.SEG_CLOSE) {
        pathPoints.add(pathPoint);
      }
      it.next();
    }      
    return pathPoints.toArray(new float [pathPoints.size()][]);
  }

  /**
   * Returns the list of closed paths that may define rooms from 
   * the current set of home walls.
   */
  private List<GeneralPath> getRoomPaths() {
    if (this.roomPathsCache == null) {
      // Make an exclusive or with a rectangle bigger than its bounding rectangle
      // (this allows us to recognize any part outside of the walls when the home isn't rectangular)
      Area roomsArea = new Area(getWallsArea());
      Rectangle2D areaBounds = roomsArea.getBounds2D();
      areaBounds.add(areaBounds.getMinX() - 100000, areaBounds.getMinY() - 100000);
      areaBounds.add(areaBounds.getMaxX() + 100000, areaBounds.getMaxY() + 100000);
      roomsArea.exclusiveOr(new Area(areaBounds));
      
      // Iterate over all the paths the area contains
      List<GeneralPath> roomPaths = new ArrayList<GeneralPath>();
      GeneralPath roomPath = new GeneralPath();
      for (PathIterator it = roomsArea.getPathIterator(null, 0.5f); !it.isDone(); ) {
        float [] roomPoint = new float[2];
        switch (it.currentSegment(roomPoint)) {
          case PathIterator.SEG_MOVETO : 
            roomPath.moveTo(roomPoint [0], roomPoint [1]);
            break;
          case PathIterator.SEG_LINETO : 
            roomPath.lineTo(roomPoint [0], roomPoint [1]);
            break;
          case PathIterator.SEG_CLOSE :
            if (!roomPath.contains(areaBounds.getMinX() + 0.5f, areaBounds.getMinY() + 0.5f)) {
              roomPaths.add(roomPath);
            }
            roomPath = new GeneralPath();
            break;
        }
        it.next();
      }
      this.roomPathsCache = roomPaths;
    }
    return this.roomPathsCache;
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
    for (PathIterator it = area.getPathIterator(null, 0.5f); !it.isDone(); ) {
      float [] point = new float[2];
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
      for (GeneralPath roomPath : getRoomPaths()) {
        smallestDistance = updateMagnetizedPoint(x, y,
            smallestDistance, getPathPoints(roomPath));
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
   * Abstract state able to manage the transition to other modes.
   */
  private abstract class AbstractModeChangeState extends ControllerState {
    @Override
    public void setMode(Mode mode) {
      switch (mode) {
        case SELECTION :
          setState(getSelectionState());
          break;
        case WALL_CREATION :
          setState(getWallCreationState());
          break;
        case ROOM_CREATION :
          setState(getRoomCreationState());
          break;
        case DIMENSION_LINE_CREATION :
          setState(getDimensionLineCreationState());
          break;
      } 
    }
  }
  
  /**
   * Default selection state. This state manages transition to other modes, 
   * the deletion of selected objects, and the move of selected objects with arrow keys.
   */
  private class SelectionState extends AbstractModeChangeState {
    @Override
    public Mode getMode() {
      return Mode.SELECTION;
    }

    @Override
    public void enter() {
      moveMouse(getXLastMouseMove(), getYLastMouseMove());
      getView().setResizeIndicatorVisible(true);
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
        getView().setCursor(PlanView.CursorType.ROTATION);
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
        } else {
          Selectable item = getItemAt(x, y);
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
        Selectable item = getItemAt(x, y);
        // If shift isn't pressed, and an item is under cursor position
        if (!shiftDown && item != null) {
          // Modify selected item on a double click
          if (item instanceof Wall) {
            modifySelectedWalls();
          } else if (item instanceof HomePieceOfFurniture) {
            modifySelectedFurniture();
          } else if (item instanceof Room) {
            modifySelectedRooms();
          } 
        }
      }
    }
    
    @Override
    public void exit() {
      getView().setResizeIndicatorVisible(false);
    }
  }

  /**
   * Move selection state. This state manages the move of current selected walls
   * with mouse and the selection of one wall, if mouse isn't moved while button
   * is depressed.
   */
  private class SelectionMoveState extends ControllerState {
    private float            xLastMouseMove;
    private float            yLastMouseMove;
    private boolean          mouseMoved;
    private List<Selectable> movedItems;
    private List<Selectable> duplicatedItems;
  
    @Override
    public Mode getMode() {
      return Mode.SELECTION;
    }
    
    @Override
    public void enter() {
      this.xLastMouseMove = getXLastMousePress();
      this.yLastMouseMove = getYLastMousePress();
      this.mouseMoved = false;
      Selectable itemUnderCursor = getItemAt(getXLastMousePress(),
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
      getView().makePointVisible(x, y);
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
        Selectable itemUnderCursor = getItemAt(x, y);
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
          this.movedItems = Home.deepCopy(this.movedItems);          
          for (Selectable item : this.movedItems) {
            if (item instanceof Wall) {
              home.addWall((Wall)item);
            } else if (item instanceof Room) {
              home.addRoom((Room)item);
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
          getView().setCursor(PlanView.CursorType.DUPLICATION);
        } else if (this.duplicatedItems != null) {
          // Delete moved items 
          doDeleteItems(this.movedItems);
          
          // Move original items to the current location
          moveItems(this.duplicatedItems, 
              this.xLastMouseMove - getXLastMousePress(), 
              this.yLastMouseMove - getYLastMousePress());
          this.movedItems = this.duplicatedItems;
          this.duplicatedItems = null;
          getView().setCursor(PlanView.CursorType.SELECTION);
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
    private List<Selectable> selectedItemsMousePressed;  
    private boolean          mouseMoved;
  
    @Override
    public Mode getMode() {
      return Mode.SELECTION;
    }

    @Override
    public void enter() {
      Selectable itemUnderCursor = 
          getItemAt(getXLastMousePress(), getYLastMousePress());
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
        Selectable itemUnderCursor = getItemAt(x, y);
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
      getView().deleteRectangleFeedback();
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
      
      // For all the items that intersects with rectangle
      for (Selectable item : getRectangleItems(x0, y0, x1, y1)) {
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
      getView().setWallAlignmentFeedback(null, x, y);
    }

    @Override
    public void pressMouse(float x, float y, int clickCount,
                           boolean shiftDown, boolean duplicationActivated) {
      // Change state to NewWallState
      setState(getNewWallState());
    }

    @Override
    public void exit() {
      getView().deleteWallAlignmentFeedback();
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
          preferences.getLengthUnit().getFormatWithUnit().format(length));
    }
  }

  /**
   * New wall state. This state manages wall creation at each mouse press. 
   */
  private class NewWallState extends AbstractWallState {
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
    private List<Wall>       newWalls;
    private boolean          magnetismEnabled;
    
    @Override
    public Mode getMode() {
      return Mode.WALL_CREATION;
    }
    
    @Override
    public void setMode(Mode mode) {
      // Escape current creation and change state to matching mode
      escape();
      switch (mode) {
        case SELECTION :
          setState(getSelectionState());
          break;
        case ROOM_CREATION :
          setState(getRoomCreationState());
          break;
        case DIMENSION_LINE_CREATION :
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
      PlanView planView = getView();
      planView.setWallAlignmentFeedback(null, 
          getXLastMousePress(), getYLastMousePress());
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
        this.newWall = createNewWall(this.xStart, this.yStart, 
            xEnd, yEnd, this.wallStartAtStart, this.wallEndAtStart);
        this.newWalls.add(this.newWall);
      } else {
        // Otherwise update its end point
        this.newWall.setXEnd(xEnd);
        this.newWall.setYEnd(yEnd);
      }         
      planView.setToolTipFeedback(getToolTipFeedbackText(this.newWall), x, y);
      planView.setWallAlignmentFeedback(this.newWall, xEnd, yEnd);
      
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
          getView().deleteToolTipFeedback();
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
      PlanView planView = getView();
      planView.deleteToolTipFeedback();
      planView.deleteWallAlignmentFeedback();
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
      PlanView planView = getView();
      planView.setResizeIndicatorVisible(true);
      planView.setToolTipFeedback(getToolTipFeedbackText(this.selectedWall), 
          getXLastMousePress(), getYLastMousePress());
      planView.setWallAlignmentFeedback(this.selectedWall, this.oldX, this.oldY);
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
      planView.setWallAlignmentFeedback(this.selectedWall, newX, newY);
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
      planView.deleteToolTipFeedback();
      planView.deleteWallAlignmentFeedback();
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
      PlanView planView = getView();
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
      this.selectedPiece.setAngle(newAngle); 

      // Ensure point at (x,y) is visible
      PlanView planView = getView();
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
      this.selectedPiece.setAngle(this.oldAngle);
      setState(getSelectionState());
    }
    
    @Override
    public void exit() {
      PlanView planView = getView();
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
    private float                deltaYToElevationPoint;
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
      planView.deleteToolTipFeedback();
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
    public void enter() {
      this.resizeToolTipFeedback = resource.getString("heightResizeToolTipFeedback");
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
      planView.deleteToolTipFeedback();
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
      this.deltaXToResizePoint = getXLastMousePress() - resizePoint [0];
      this.deltaYToResizePoint = getYLastMousePress() - resizePoint [1];
      this.oldX = this.selectedPiece.getX();
      this.oldY = this.selectedPiece.getY();
      this.oldWidth = this.selectedPiece.getWidth();
      this.oldDepth = this.selectedPiece.getDepth();
      this.magnetismEnabled = preferences.isMagnetismEnabled()
                              ^ wasShiftDownLastMousePress();
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
      float [] topLeftPoint = this.selectedPiece.getPoints() [0];
      float deltaX = x - this.deltaXToResizePoint - topLeftPoint[0];
      float deltaY = y - this.deltaYToResizePoint - topLeftPoint[1];
      float newWidth =  (float)(deltaY * sin + deltaX * cos);
      float newDepth =  (float)(deltaY * cos - deltaX * sin);

      if (this.magnetismEnabled) {
        newWidth = preferences.getLengthUnit().getMagnetizedLength(newWidth, planView.getPixelLength());
        newDepth = preferences.getLengthUnit().getMagnetizedLength(newDepth, planView.getPixelLength());
      }
      newWidth = Math.max(newWidth, preferences.getLengthUnit().getMinimumLength());
      newDepth = Math.max(newDepth, preferences.getLengthUnit().getMinimumLength());

      // Update piece new location
      float newX = (float)(topLeftPoint [0] + (newWidth * cos - newDepth * sin) / 2f);
      float newY = (float)(topLeftPoint [1] + (newWidth * sin + newDepth * cos) / 2f);
      this.selectedPiece.setX(newX);
      this.selectedPiece.setY(newY);
      // Update piece new dimension
      this.selectedPiece.setWidth(newWidth);
      this.selectedPiece.setDepth(newDepth);

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
      this.selectedPiece.setX(this.oldX);
      this.selectedPiece.setY(this.oldY);
      this.selectedPiece.setWidth(this.oldWidth);
      this.selectedPiece.setDepth(this.oldDepth);
      setState(getSelectionState());
    }

    @Override
    public void exit() {
      PlanView planView = getView();
      planView.setResizeIndicatorVisible(false);
      planView.deleteToolTipFeedback();
      this.selectedPiece = null;
    }  
    
    private String getToolTipFeedbackText(float width, float depth) {
      return "<html>" + String.format(this.widthResizeToolTipFeedback,  
              preferences.getLengthUnit().getFormatWithUnit().format(width))
          + "<br>" + String.format(this.depthResizeToolTipFeedback,
              preferences.getLengthUnit().getFormatWithUnit().format(depth));
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
      PlanView planView = getView();
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
      this.selectedCamera.setYaw(newYaw); 

      getView().setToolTipFeedback(getToolTipFeedbackText(newYaw), x, y);
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
      // Check new angle is between -60° and 75°  
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
      getView().setDimensionLineAlignmentFeedback(null, x, y);
    }

    @Override
    public void pressMouse(float x, float y, int clickCount,
                           boolean shiftDown, boolean duplicationActivated) {
      // Change state to NewDimensionLineState
      setState(getNewDimensionLineState());
    }

    @Override
    public void exit() {
      getView().deleteDimensionLineAlignmentFeedback();
    }  
  }

  /**
   * New dimension line state. This state manages dimension line creation at mouse press. 
   */
  private class NewDimensionLineState extends ControllerState {
    private float            xStart;
    private float            yStart;
    private DimensionLine    newDimensionLine;
    private List<Selectable> oldSelection;
    private boolean          magnetismEnabled;
    private boolean          offsetChoice;
    
    @Override
    public Mode getMode() {
      return Mode.DIMENSION_LINE_CREATION;
    }
    
    @Override
    public void setMode(Mode mode) {
      // Escape current creation and change state to matching mode
      escape();
      switch (mode) {
        case SELECTION :
          setState(getSelectionState());
          break;
        case WALL_CREATION :
          setState(getWallCreationState());
          break;
        case ROOM_CREATION :
          setState(getRoomCreationState());
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
      getView().setDimensionLineAlignmentFeedback(null, getXLastMousePress(), getYLastMousePress());
    }

    @Override
    public void moveMouse(float x, float y) {
      PlanView planView = getView();
      if (offsetChoice) {
        float distanceToDimensionLine = (float)Line2D.ptLineDist(this.xStart, this.yStart, 
            this.newDimensionLine.getXEnd(), this.newDimensionLine.getYEnd(), x, y);
        int relativeCCW = Line2D.relativeCCW(this.xStart, this.yStart, 
            this.newDimensionLine.getXEnd(), this.newDimensionLine.getYEnd(), x, y);
        this.newDimensionLine.setOffset(
            -Math.signum(relativeCCW) * distanceToDimensionLine);
      } else {
        // Compute the coordinates where dimension line end point should be moved
        float xEnd;
        float yEnd;
        if (this.magnetismEnabled) {
          PointWithAngleMagnetism point = new PointWithAngleMagnetism(
              this.xStart, this.yStart, x, y,
              preferences.getLengthUnit(), planView.getPixelLength());
          xEnd = point.getX();
          yEnd = point.getY();
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
          this.newDimensionLine.setXEnd(xEnd); 
          this.newDimensionLine.setYEnd(yEnd); 
        }         
        planView.setDimensionLineAlignmentFeedback(this.newDimensionLine, xEnd, yEnd);
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
          PlanView planView = getView();
          planView.setCursor(PlanView.CursorType.HEIGHT);
          planView.deleteDimensionLineAlignmentFeedback();
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
      getView().deleteDimensionLineAlignmentFeedback();
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
      PlanView planView = getView();
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
        planView.setDimensionLineAlignmentFeedback(this.selectedDimensionLine, 
            this.selectedDimensionLine.getXStart(), this.selectedDimensionLine.getYStart());
      } else {
        this.distanceFromResizePointToDimensionBaseLine = (float)Point2D.distance(xResizePoint, yResizePoint, 
            this.selectedDimensionLine.getXEnd(), this.selectedDimensionLine.getYEnd());
        planView.setDimensionLineAlignmentFeedback(this.selectedDimensionLine, 
            this.selectedDimensionLine.getXEnd(), this.selectedDimensionLine.getYEnd());
      }
      toggleMagnetism(wasShiftDownLastMousePress());
    }
    
    @Override
    public void moveMouse(float x, float y) {
      PlanView planView = getView();
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
            PointWithAngleMagnetism point = new PointWithAngleMagnetism(
                this.selectedDimensionLine.getXEnd(), 
                this.selectedDimensionLine.getYEnd(), xNewStartPoint, yNewStartPoint,
                preferences.getLengthUnit(), planView.getPixelLength());
            xNewStartPoint = point.getX();
            yNewStartPoint = point.getY();
          } 

          moveDimensionLinePoint(this.selectedDimensionLine, xNewStartPoint, yNewStartPoint, this.startPoint);        
          planView.setDimensionLineAlignmentFeedback(this.selectedDimensionLine, 
              xNewStartPoint, yNewStartPoint);
        } else {
          planView.deleteDimensionLineAlignmentFeedback();
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

          moveDimensionLinePoint(this.selectedDimensionLine, xNewEndPoint, yNewEndPoint, this.startPoint);
          planView.setDimensionLineAlignmentFeedback(this.selectedDimensionLine, 
              xNewEndPoint, yNewEndPoint);
        } else {
          planView.deleteDimensionLineAlignmentFeedback();
        }
      }     

      // Ensure point at (x,y) is visible
      getView().makePointVisible(x, y);
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
      PlanView planView = getView();
      planView.deleteDimensionLineAlignmentFeedback();
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
    private boolean                magnetismEnabled;
    
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
            home.getRooms(), x, y, PIXEL_WALL_MARGIN / getView().getScale());
        getView().setRoomAlignmentFeedback(null, point.getX(), 
            point.getY(), point.isMagnetized());
      } else {
        getView().setRoomAlignmentFeedback(null, x, y, false);
      } 
    }

    @Override
    public void pressMouse(float x, float y, int clickCount,
                           boolean shiftDown, boolean duplicationActivated) {
      // Change state to NewRoomState
      setState(getNewRoomState());
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
      getView().deleteRoomAlignmentFeedback();
    }  
  }

  /**
   * New room state. This state manages room creation at mouse press. 
   */
  private class NewRoomState extends ControllerState {
    private Collection<Room>       rooms;
    private float                  xPreviousPoint;
    private float                  yPreviousPoint;
    private Room                   newRoom;
    private float []               newPoint;
    private List<Selectable>       oldSelection;
    private boolean                magnetismEnabled;
    
    @Override
    public Mode getMode() {
      return Mode.ROOM_CREATION;
    }
    
    @Override
    public void setMode(Mode mode) {
      // Escape current creation and change state to matching mode
      escape();
      switch (mode) {
        case SELECTION :
          setState(getSelectionState());
          break;
        case WALL_CREATION :
          setState(getWallCreationState());
          break;
        case DIMENSION_LINE_CREATION :
          setState(getDimensionLineCreationState());
          break;
      } 
    }

    @Override
    public void enter() {
      this.oldSelection = home.getSelectedItems();
      this.rooms = home.getRooms();
      this.newRoom = null;
      toggleMagnetism(wasShiftDownLastMousePress());
      if (this.magnetismEnabled) {
        // Find the closest wall or room point to current mouse location
        PointMagnetizedToClosestWallOrRoomPoint point = new PointMagnetizedToClosestWallOrRoomPoint(
            this.rooms, getXLastMouseMove(), getYLastMouseMove(), 
            PIXEL_WALL_MARGIN / getView().getScale());
        this.xPreviousPoint = point.getX();
        this.yPreviousPoint = point.getY();
        getView().setRoomAlignmentFeedback(null, point.getX(), 
            point.getY(), point.isMagnetized());
        
      } else {
         this.xPreviousPoint = getXLastMousePress();
         this.yPreviousPoint = getYLastMousePress();
         getView().setRoomAlignmentFeedback(null, this.xPreviousPoint, this.yPreviousPoint, false);
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
            this.rooms, x, y, PIXEL_WALL_MARGIN / getView().getScale());
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
        this.newRoom = new Room(new float [][] {{this.xPreviousPoint, this.yPreviousPoint}, {xEnd, yEnd}});
        home.addRoom(this.newRoom);
        selectItem(this.newRoom);
      } else if (this.newPoint != null) {
        // Add a point to current room
        float [][] points = this.newRoom.getPoints();
        this.xPreviousPoint = points [points.length - 1][0];
        this.yPreviousPoint = points [points.length - 1][1]; 
        float [][] newPoints = new float [points.length + 1][];
        System.arraycopy(points, 0, newPoints, 0, points.length);
        newPoints [newPoints.length - 1] = this.newPoint;
        this.newPoint [0] = xEnd; 
        this.newPoint [1] = yEnd; 
        this.newRoom.setPoints(newPoints);
        this.newPoint = null;
      } else {
        // Otherwise update its last point
        float [][] points = this.newRoom.getPoints();
        points [points.length - 1][0] = xEnd;
        points [points.length - 1][1] = yEnd;
        this.newRoom.setPoints(points);
      }         
      planView.setRoomAlignmentFeedback(this.newRoom, xEnd, yEnd, magnetizedPoint);
      
      // Ensure point at (x,y) is visible
      planView.makePointVisible(x, y);
    }
    
    @Override
    public void pressMouse(float x, float y, int clickCount, 
                           boolean shiftDown, boolean duplicationActivated) {
      if (clickCount == 2) {
        if (this.newRoom == null) {
          // Try to guess the room that contains the point (x,y)
          this.newRoom = createRoomAt(x, y);
          if (this.newRoom != null) {
            home.addRoom(this.newRoom);
            selectItem(this.newRoom);           
          }
        }
        if (this.newRoom != null) {
          if (this.newRoom.getPoints().length > 2) {
            // Post walls creation to undo support
            postAddRooms(Arrays.asList(new Room [] {this.newRoom}), this.oldSelection);
          } else {
            // Delete rooms with only two points
            home.deleteRoom(this.newRoom);
          }
        }
          // Change state to WallCreationState 
        setState(getRoomCreationState());
      } else {
        // Create a new room only when it will have one point
        // meaning after the first mouse move
        if (this.newRoom != null) {
          this.newPoint = new float [2];
        }
      }
    }

    /**
     * Returns the room matching the closed path that contains the point at the given
     * coordinates or <code>null</code> if there's no closed path at this point. 
     */
    private Room createRoomAt(float x, float y) {
      for (GeneralPath roomPath : getRoomPaths()) {
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
                float [][] intersectionPoints = getPathPoints(getPath(wallsDoorIntersection));
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
          
          return new Room(getPathPoints(roomPath));
        }
      }
      return null;
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
      if (this.newRoom != null) {
        float [][] points = this.newRoom.getPoints();
        if (points.length <= 3) {
          // Delete current created room if it doesn't have more than 2 clicked points
          home.deleteRoom(this.newRoom);
        } else {
          // Remove last currently edited point
          float [][] newPoints = new float [points.length -1][];
          System.arraycopy(points, 0, newPoints, 0, newPoints.length);
          this.newRoom.setPoints(newPoints);
          // Post walls creation to undo support
          postAddRooms(Arrays.asList(new Room [] {this.newRoom}), this.oldSelection);
        }
      }
      // Change state to RoomCreationState 
      setState(getRoomCreationState());
    }

    @Override
    public void exit() {
      getView().deleteRoomAlignmentFeedback();
      this.newRoom = null;
      this.newPoint = null;
      this.oldSelection = null;
    }  
  }

  /**
   * Room resize state. This state manages room resizing. 
   */
  private class RoomResizeState extends AbstractWallState {
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
    public void enter() {
      super.enter();
      this.selectedRoom = (Room)home.getSelectedItems().get(0);
      this.rooms = new ArrayList<Room>(home.getRooms());
      this.rooms.remove(this.selectedRoom);
      float margin = PIXEL_MARGIN / getView().getScale();
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
            this.rooms, newX, newY, PIXEL_WALL_MARGIN / getView().getScale());
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

      planView.setRoomAlignmentFeedback(this.selectedRoom, newX, newY, magnetizedPoint);
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
      planView.deleteRoomAlignmentFeedback();
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
}
