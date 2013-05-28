/*
 * HomeController3D.java 21 juin 07
 *
 * Sweet Home 3D, Copyright (c) 2007 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.Camera;
import com.eteks.sweethome3d.model.CollectionEvent;
import com.eteks.sweethome3d.model.CollectionListener;
import com.eteks.sweethome3d.model.Elevatable;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeEnvironment;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.Level;
import com.eteks.sweethome3d.model.ObserverCamera;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.SelectionEvent;
import com.eteks.sweethome3d.model.SelectionListener;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;

/**
 * A MVC controller for the home 3D view.
 * @author Emmanuel Puybaret
 */
public class HomeController3D implements Controller {
  private final Home                  home;
  private final UserPreferences       preferences;
  private final ViewFactory           viewFactory;
  private final ContentManager        contentManager;
  private final UndoableEditSupport   undoSupport;
  private View                        home3DView;
  // Possibles states
  private final CameraControllerState topCameraState;
  private final CameraControllerState observerCameraState;
  // Current state
  private CameraControllerState       cameraState;

  /**
   * Creates the controller of home 3D view.
   * @param home the home edited by this controller and its view
   */
  public HomeController3D(final Home home, 
                          UserPreferences preferences,
                          ViewFactory viewFactory, 
                          ContentManager contentManager, 
                          UndoableEditSupport undoSupport) {
    this.home = home;
    this.preferences = preferences;
    this.viewFactory = viewFactory;
    this.contentManager = contentManager;
    this.undoSupport = undoSupport;
    // Initialize states
    this.topCameraState = new TopCameraState(preferences);
    this.observerCameraState = new ObserverCameraState();
    // Set default state 
    setCameraState(home.getCamera() == home.getTopCamera() 
        ? this.topCameraState
        : this.observerCameraState);
    addModelListeners(home);
  }

  /**
   * Add listeners to model to update camera position accordingly.
   */
  private void addModelListeners(final Home home) {
    home.addPropertyChangeListener(Home.Property.CAMERA, new PropertyChangeListener() {      
        public void propertyChange(PropertyChangeEvent ev) {
          setCameraState(home.getCamera() == home.getTopCamera() 
              ? topCameraState
              : observerCameraState);
        }
      });
    // Add listeners to adjust observer camera elevation when the elevation of the selected level  
    // or the level selection change
    final PropertyChangeListener levelElevationChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          if (Level.Property.ELEVATION.name().equals(ev.getPropertyName()) 
              && home.getEnvironment().isObserverCameraElevationAdjusted()) {
            home.getObserverCamera().setZ(Math.max(getObserverCameraMinimumElevation(home), 
                home.getObserverCamera().getZ() + (Float)ev.getNewValue() - (Float)ev.getOldValue()));
          }
        }
      };
    Level selectedLevel = home.getSelectedLevel();
    if (selectedLevel != null) {
      selectedLevel.addPropertyChangeListener(levelElevationChangeListener);
    }
    this.home.addPropertyChangeListener(Home.Property.SELECTED_LEVEL, new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          Level oldSelectedLevel = (Level)ev.getOldValue();
          Level selectedLevel = home.getSelectedLevel();
          if (home.getEnvironment().isObserverCameraElevationAdjusted()) {
            home.getObserverCamera().setZ(Math.max(getObserverCameraMinimumElevation(home), 
                home.getObserverCamera().getZ() 
                + (selectedLevel == null ? 0 : selectedLevel.getElevation()) 
                - (oldSelectedLevel == null ? 0 : oldSelectedLevel.getElevation())));
          }
          if (oldSelectedLevel != null) {
            oldSelectedLevel.removePropertyChangeListener(levelElevationChangeListener);
          }
          if (selectedLevel != null) {
            selectedLevel.addPropertyChangeListener(levelElevationChangeListener);
          }
        }
      });     
    // Add a listener to home to update visible levels according to selected level
    PropertyChangeListener selectedLevelListener = new PropertyChangeListener() {
         public void propertyChange(PropertyChangeEvent ev) {
           List<Level> levels = home.getLevels();
           Level selectedLevel = home.getSelectedLevel();
           boolean visible = true;
           for (int i = 0; i < levels.size(); i++) {
             levels.get(i).setVisible(visible);
             if (levels.get(i) == selectedLevel
                 && !home.getEnvironment().isAllLevelsVisible()) {
               visible = false;
             }
           }
         }
       };
     this.home.addPropertyChangeListener(Home.Property.SELECTED_LEVEL, selectedLevelListener);     
     this.home.getEnvironment().addPropertyChangeListener(HomeEnvironment.Property.ALL_LEVELS_VISIBLE, selectedLevelListener);
  }

  private float getObserverCameraMinimumElevation(final Home home) {
    List<Level> levels = home.getLevels();
    float minimumElevation = levels.size() == 0  ? 10  : 10 + levels.get(0).getElevation();
    return minimumElevation;
  }

  /**
   * Returns the view associated with this controller.
   */
  public View getView() {
    // Create view lazily only once it's needed
    if (this.home3DView == null) {
      this.home3DView = this.viewFactory.createView3D(this.home, this.preferences, this);
    }
    return this.home3DView;
  }

  /**
   * Changes home camera for {@link Home#getTopCamera() top camera}.
   */
  public void viewFromTop() {
    this.home.setCamera(this.home.getTopCamera());
  }
  
  /**
   * Changes home camera for {@link Home#getObserverCamera() observer camera}.
   */
  public void viewFromObserver() {
    this.home.setCamera(this.home.getObserverCamera());
  }
  
  /**
   * Stores a clone of the current camera in home under the given <code>name</code>.
   */
  public void storeCamera(String name) {
    Camera camera = this.home.getCamera().clone();
    camera.setName(name);
    List<Camera> homeStoredCameras = this.home.getStoredCameras();
    ArrayList<Camera> storedCameras = new ArrayList<Camera>(homeStoredCameras.size() + 1);
    storedCameras.addAll(homeStoredCameras);
    // Don't keep two cameras with the same name or the same location
    for (Iterator<Camera> it = storedCameras.iterator(); it.hasNext(); ) {
      Camera storedCamera = it.next();
      if (name.equals(storedCamera.getName())
          || (camera.getX() == storedCamera.getX()
              && camera.getY() == storedCamera.getY()
              && camera.getZ() == storedCamera.getZ()
              && camera.getPitch() == storedCamera.getPitch()
              && camera.getYaw() == storedCamera.getYaw()
              && camera.getFieldOfView() == storedCamera.getFieldOfView()
              && camera.getTime() == storedCamera.getTime()
              && camera.getLens() == storedCamera.getLens())) {
        it.remove();
      }
    }
    storedCameras.add(0, camera);
    // Ensure home stored cameras don't contain more than 20 cameras
    while (storedCameras.size() > 20) {
      storedCameras.remove(storedCameras.size() - 1);
    }
    this.home.setStoredCameras(storedCameras);
  }
  
  /**
   * Switches to observer or top camera and move camera to the values as the current camera.
   */
  public void goToCamera(Camera camera) {
    if (camera instanceof ObserverCamera) {
      viewFromObserver();
    } else {
      viewFromTop();
    }
    this.cameraState.goToCamera(camera);
    // Reorder cameras
    ArrayList<Camera> storedCameras = new ArrayList<Camera>(this.home.getStoredCameras());
    storedCameras.remove(camera);
    storedCameras.add(0, camera);
    this.home.setStoredCameras(storedCameras);
  }

  /**
   * Deletes the given list of cameras from the ones stored in home.
   */
  public void deleteCameras(List<Camera> cameras) {
    List<Camera> homeStoredCameras = this.home.getStoredCameras();
    // Build a list of cameras that will contain only the cameras not in the camera list in parameter
    ArrayList<Camera> storedCameras = new ArrayList<Camera>(homeStoredCameras.size() - cameras.size());
    for (Camera camera : homeStoredCameras) {
      if (!cameras.contains(camera)) {
        storedCameras.add(camera);
      }
    }
    this.home.setStoredCameras(storedCameras);
  }

  /**
   * Makes all levels visible.
   */
  public void displayAllLevels() {
    this.home.getEnvironment().setAllLevelsVisible(true);
  }
  
  /**
   * Makes the selected level and below visible.
   */
  public void displaySelectedLevel() {
    this.home.getEnvironment().setAllLevelsVisible(false);
  }
  
  /**
   * Controls the edition of 3D attributes. 
   */
  public void modifyAttributes() {
    new Home3DAttributesController(this.home, this.preferences, 
        this.viewFactory, this.contentManager, this.undoSupport).displayView(getView());    
  }

  /**
   * Changes current state of controller.
   */
  protected void setCameraState(CameraControllerState state) {
    if (this.cameraState != null) {
      this.cameraState.exit();
    }
    this.cameraState = state;
    this.cameraState.enter();
  }
  
  /**
   * Moves home camera of <code>delta</code>.
   */
  public void moveCamera(float delta) {
    this.cameraState.moveCamera(delta);
  }

  /**
   * Elevates home camera of <code>delta</code>.
   */
  public void elevateCamera(float delta) {
    this.cameraState.elevateCamera(delta);
  }

  /**
   * Rotates home camera yaw angle of <code>delta</code> radians.
   */
  public void rotateCameraYaw(float delta) {
    this.cameraState.rotateCameraYaw(delta);
  }

  /**
   * Rotates home camera pitch angle of <code>delta</code> radians.
   */
  public void rotateCameraPitch(float delta) {
    this.cameraState.rotateCameraPitch(delta);
  }

  /**
   * Returns the observer camera state.
   */
  protected CameraControllerState getObserverCameraState() {
    return this.observerCameraState;
  }

  /**
   * Returns the top camera state.
   */
  protected CameraControllerState getTopCameraState() {
    return this.topCameraState;
  }

  /**
   * Controller state classes super class.
   */
  protected static abstract class CameraControllerState {
    public void enter() {
    }

    public void exit() {
    }

    public void moveCamera(float delta) {
    }

    public void elevateCamera(float delta) {     
    }
    
    public void rotateCameraYaw(float delta) {
    }

    public void rotateCameraPitch(float delta) {
    }

    public void goToCamera(Camera camera) {
    }
  }
  
  // CameraControllerState subclasses

  /**
   * Top camera controller state. 
   */
  private class TopCameraState extends CameraControllerState {
    private final float MIN_WIDTH  = 1000;
    private final float MIN_DEPTH  = 1000;
    private final float MIN_HEIGHT = 20;
    
    private Camera      topCamera;
    private float []    aerialViewBoundsLowerPoint;
    private float []    aerialViewBoundsUpperPoint;
    private float       minDistanceToAerialViewCenter;
    private float       maxDistanceToAerialViewCenter;
    private boolean     aerialViewCenteredOnSelectionEnabled;
    private PropertyChangeListener objectChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          updateCameraFromHomeBounds(false);
        }
      };
    private CollectionListener<Level> levelsListener = new CollectionListener<Level>() {
        public void collectionChanged(CollectionEvent<Level> ev) {
          if (ev.getType() == CollectionEvent.Type.ADD) {
            ev.getItem().addPropertyChangeListener(objectChangeListener);
          } else if (ev.getType() == CollectionEvent.Type.DELETE) {
            ev.getItem().removePropertyChangeListener(objectChangeListener);
          } 
          updateCameraFromHomeBounds(false);
        }
      };
    private CollectionListener<Wall> wallsListener = new CollectionListener<Wall>() {
        public void collectionChanged(CollectionEvent<Wall> ev) {
          if (ev.getType() == CollectionEvent.Type.ADD) {
            ev.getItem().addPropertyChangeListener(objectChangeListener);
          } else if (ev.getType() == CollectionEvent.Type.DELETE) {
            ev.getItem().removePropertyChangeListener(objectChangeListener);
          } 
          updateCameraFromHomeBounds(false);
        }
      };
    private CollectionListener<HomePieceOfFurniture> furnitureListener = new CollectionListener<HomePieceOfFurniture>() {
        public void collectionChanged(CollectionEvent<HomePieceOfFurniture> ev) {
          if (ev.getType() == CollectionEvent.Type.ADD) {
            ev.getItem().addPropertyChangeListener(objectChangeListener);
            updateCameraFromHomeBounds(home.getFurniture().size() == 1
                && home.getWalls().isEmpty()
                && home.getRooms().isEmpty());
          } else if (ev.getType() == CollectionEvent.Type.DELETE) {
            ev.getItem().removePropertyChangeListener(objectChangeListener);
            updateCameraFromHomeBounds(false);
          } 
        }
      };
    private CollectionListener<Room> roomsListener = new CollectionListener<Room>() {
        public void collectionChanged(CollectionEvent<Room> ev) {
          if (ev.getType() == CollectionEvent.Type.ADD) {
            ev.getItem().addPropertyChangeListener(objectChangeListener);
          } else if (ev.getType() == CollectionEvent.Type.DELETE) {
            ev.getItem().removePropertyChangeListener(objectChangeListener);
          } 
          updateCameraFromHomeBounds(false);
        }
      };
    private SelectionListener selectionListener = new SelectionListener() {
        public void selectionChanged(SelectionEvent ev) {
          updateCameraFromHomeBounds(false);
        }
      };

    public TopCameraState(UserPreferences preferences) {
      this.aerialViewCenteredOnSelectionEnabled = preferences.isAerialViewCenteredOnSelectionEnabled();
      preferences.addPropertyChangeListener(UserPreferences.Property.AERIAL_VIEW_CENTERED_ON_SELECTION_ENABLED, 
          new UserPreferencesChangeListener(this));
    }

    @Override
    public void enter() {
      this.topCamera = home.getCamera();
      updateCameraFromHomeBounds(false);
      for (Level level : home.getLevels()) {
        level.addPropertyChangeListener(this.objectChangeListener);
      }
      home.addLevelsListener(this.levelsListener);
      for (Wall wall : home.getWalls()) {
        wall.addPropertyChangeListener(this.objectChangeListener);
      }
      home.addWallsListener(this.wallsListener);
      for (HomePieceOfFurniture piece : home.getFurniture()) {
        piece.addPropertyChangeListener(this.objectChangeListener);
      }
      home.addFurnitureListener(this.furnitureListener);
      for (Room room : home.getRooms()) {
        room.addPropertyChangeListener(this.objectChangeListener);
      }
      home.addRoomsListener(this.roomsListener);
      home.addSelectionListener(this.selectionListener);
    }
    
    /**
     * Sets whether aerial view should be centered on selection or not.
     */
    public void setAerialViewCenteredOnSelectionEnabled(boolean aerialViewCenteredOnSelectionEnabled) {
      this.aerialViewCenteredOnSelectionEnabled = aerialViewCenteredOnSelectionEnabled;
      updateCameraFromHomeBounds(false);
    }
    
    /**
     * Updates camera location from home bounds.
     */
    private void updateCameraFromHomeBounds(boolean firstPieceOfFurnitureAddedToEmptyHome) {
      if (this.aerialViewBoundsLowerPoint == null) {
        updateAerialViewBounds(this.aerialViewCenteredOnSelectionEnabled);
      }
      float distanceToCenter = getCameraToAerialViewCenterDistance();
      updateAerialViewBounds(this.aerialViewCenteredOnSelectionEnabled);
      updateCameraIntervalToAerialViewCenter();
      placeCameraAt(distanceToCenter, firstPieceOfFurnitureAddedToEmptyHome);
    }

    /**
     * Returns the distance between the current camera location and home bounds center.
     */
    private float getCameraToAerialViewCenterDistance() {
      return (float)Math.sqrt(Math.pow((this.aerialViewBoundsLowerPoint [0] + this.aerialViewBoundsUpperPoint [0]) / 2 - this.topCamera.getX(), 2) 
          + Math.pow((this.aerialViewBoundsLowerPoint [1] + this.aerialViewBoundsUpperPoint [1]) / 2 - this.topCamera.getY(), 2) 
          + Math.pow((this.aerialViewBoundsLowerPoint [2] + this.aerialViewBoundsUpperPoint [2]) / 2 - this.topCamera.getZ(), 2));
    }

    /**
     * Sets the bounds that includes walls, furniture and rooms, or only selected items 
     * if <code>centerOnSelection</code> is <code>true</code>.
     */
    private void updateAerialViewBounds(boolean centerOnSelection) {
      this.aerialViewBoundsLowerPoint = 
      this.aerialViewBoundsUpperPoint = null;
      List<Selectable> selectedItems = Collections.emptyList();
      if (centerOnSelection) { 
        selectedItems = new ArrayList<Selectable>();
        for (Selectable item : home.getSelectedItems()) {
          if (item instanceof Elevatable 
              && isItemAtVisibleLevel((Elevatable)item)
              && (!(item instanceof HomePieceOfFurniture)
                  || ((HomePieceOfFurniture)item).isVisible())) {
            selectedItems.add(item);
          }
        }
      }
      boolean selectionEmpty = selectedItems.size() == 0 || !centerOnSelection;

      // Compute plan bounds to include rooms, walls and furniture
      boolean containsVisibleWalls = false;
      for (Wall wall : selectionEmpty
                           ? home.getWalls()
                           : Home.getWallsSubList(selectedItems)) {
        if (isItemAtVisibleLevel(wall)) {
          containsVisibleWalls = true;
          
          float wallElevation = wall.getLevel() != null 
              ? wall.getLevel().getElevation() 
              : 0;
          float minZ = selectionEmpty
              ? 0
              : wallElevation;
          
          Float height = wall.getHeight();
          float maxZ;
          if (height != null) {
            maxZ = wallElevation + height;
          } else {
            maxZ = wallElevation + home.getWallHeight();
          }
          Float heightAtEnd = wall.getHeightAtEnd();
          if (heightAtEnd != null) {
            maxZ = Math.max(maxZ, wallElevation + heightAtEnd);
          }
          for (float [] point : wall.getPoints()) {
            updateAerialViewBounds(point [0], point [1], minZ, maxZ);
          }
        }
      }

      for (HomePieceOfFurniture piece : selectionEmpty 
                                            ? home.getFurniture()
                                            : Home.getFurnitureSubList(selectedItems)) {
        if (piece.isVisible() && isItemAtVisibleLevel(piece)) {
          float minZ;
          float maxZ;
          if (selectionEmpty) {
            minZ = Math.max(0, piece.getGroundElevation());
            maxZ = Math.max(0, piece.getGroundElevation() + piece.getHeight());
          } else {
            minZ = piece.getGroundElevation();
            maxZ = piece.getGroundElevation() + piece.getHeight();
          }
          for (float [] point : piece.getPoints()) {
            updateAerialViewBounds(point [0], point [1], minZ, maxZ);
          }
        }
      }
      
      for (Room room : selectionEmpty 
                           ? home.getRooms()
                           : Home.getRoomsSubList(selectedItems)) {
        if (isItemAtVisibleLevel(room)) {
          float minZ = 0;
          float maxZ = MIN_HEIGHT;
          Level roomLevel = room.getLevel();
          if (roomLevel != null) {
            minZ = roomLevel.getElevation() - roomLevel.getFloorThickness();
            maxZ = roomLevel.getElevation();
            if (selectionEmpty) {
              minZ = Math.max(0, minZ);
              maxZ = Math.max(MIN_HEIGHT, roomLevel.getElevation());
            }
          }
          for (float [] point : room.getPoints()) {
            updateAerialViewBounds(point [0], point [1], minZ, maxZ);
          }
        }
      }
      
      if (this.aerialViewBoundsLowerPoint == null) {
        this.aerialViewBoundsLowerPoint = new float [] {0, 0, 0};
        this.aerialViewBoundsUpperPoint = new float [] {MIN_WIDTH, MIN_DEPTH, MIN_HEIGHT};
      } else if (containsVisibleWalls && selectionEmpty) {
        // If home contains walls, ensure bounds are always minimum 10 meters wide centered in middle of 3D view
        if (MIN_WIDTH > this.aerialViewBoundsUpperPoint [0] - this.aerialViewBoundsLowerPoint [0]) {
          this.aerialViewBoundsLowerPoint [0] = (this.aerialViewBoundsLowerPoint [0] + this.aerialViewBoundsUpperPoint [0]) / 2 - MIN_WIDTH / 2;
          this.aerialViewBoundsUpperPoint [0] = this.aerialViewBoundsLowerPoint [0] + MIN_WIDTH;
        }
        if (MIN_DEPTH > this.aerialViewBoundsUpperPoint [1] - this.aerialViewBoundsLowerPoint [1]) {
          this.aerialViewBoundsLowerPoint [1] = (this.aerialViewBoundsLowerPoint [1] + this.aerialViewBoundsUpperPoint [1]) / 2 - MIN_DEPTH / 2;
          this.aerialViewBoundsUpperPoint [1] = this.aerialViewBoundsLowerPoint [1] + MIN_DEPTH;
        }
        if (MIN_HEIGHT > this.aerialViewBoundsUpperPoint [2] - this.aerialViewBoundsLowerPoint [2]) {
          this.aerialViewBoundsLowerPoint [2] = (this.aerialViewBoundsLowerPoint [2] + this.aerialViewBoundsUpperPoint [2]) / 2 - MIN_HEIGHT / 2;
          this.aerialViewBoundsUpperPoint [2] = this.aerialViewBoundsLowerPoint [2] + MIN_HEIGHT;
        }
      }
    }

    /**
     * Adds the point at the given coordinates to aerial view bounds.
     */
    private void updateAerialViewBounds(float x, float y, float minZ, float maxZ) {
      if (this.aerialViewBoundsLowerPoint == null) {
        this.aerialViewBoundsLowerPoint = new float [] {x, y, minZ};
        this.aerialViewBoundsUpperPoint = new float [] {x, y, maxZ};
      } else {
        this.aerialViewBoundsLowerPoint [0] = Math.min(this.aerialViewBoundsLowerPoint [0], x); 
        this.aerialViewBoundsUpperPoint [0] = Math.max(this.aerialViewBoundsUpperPoint [0], x);
        this.aerialViewBoundsLowerPoint [1] = Math.min(this.aerialViewBoundsLowerPoint [1], y); 
        this.aerialViewBoundsUpperPoint [1] = Math.max(this.aerialViewBoundsUpperPoint [1], y);
        this.aerialViewBoundsLowerPoint [2] = Math.min(this.aerialViewBoundsLowerPoint [2], minZ); 
        this.aerialViewBoundsUpperPoint [2] = Math.max(this.aerialViewBoundsUpperPoint [2], maxZ);
      }
    }

    /**
     * Returns <code>true</code> if the given <code>item</code> is at a visible level.
     */
    private boolean isItemAtVisibleLevel(Elevatable item) {
      return item.getLevel() == null || item.getLevel().isVisible();
    }
    
    /**
     * Updates the minimum and maximum distances of the camera to the center of the aerial view.
     */
    private void updateCameraIntervalToAerialViewCenter() {  
      float homeBoundsWidth = this.aerialViewBoundsUpperPoint [0] - this.aerialViewBoundsLowerPoint [0];
      float homeBoundsDepth = this.aerialViewBoundsUpperPoint [1] - this.aerialViewBoundsLowerPoint [1];
      float homeBoundsHeight = this.aerialViewBoundsUpperPoint [2] - this.aerialViewBoundsLowerPoint [2];
      float halfDiagonal = (float)Math.sqrt(homeBoundsWidth * homeBoundsWidth 
          + homeBoundsDepth * homeBoundsDepth 
          + homeBoundsHeight * homeBoundsHeight) / 2;
      this.minDistanceToAerialViewCenter = halfDiagonal * 1.05f;
      this.maxDistanceToAerialViewCenter = Math.max(5 * this.minDistanceToAerialViewCenter, 1000);
    }
       
    @Override
    public void moveCamera(float delta) {
      // Use a 5 times bigger delta for top camera move
      delta *= 5;
      float newDistanceToCenter = getCameraToAerialViewCenterDistance() - delta;
      placeCameraAt(newDistanceToCenter, false);
    }

    public void placeCameraAt(float distanceToCenter, boolean firstPieceOfFurnitureAddedToEmptyHome) {
      // Check camera is always outside the sphere centered in home center and with a radius equal to minimum distance   
      distanceToCenter = Math.max(distanceToCenter, this.minDistanceToAerialViewCenter);
      // Check camera isn't too far
      distanceToCenter = Math.min(distanceToCenter, this.maxDistanceToAerialViewCenter);
      if (firstPieceOfFurnitureAddedToEmptyHome) {
        // Get closer to the first piece of furniture added to an empty home when that is small
        distanceToCenter = Math.min(distanceToCenter, 3 * this.minDistanceToAerialViewCenter);
      }
      double distanceToCenterAtGroundLevel = distanceToCenter * Math.cos(this.topCamera.getPitch());
      this.topCamera.setX((this.aerialViewBoundsLowerPoint [0] + this.aerialViewBoundsUpperPoint [0]) / 2 
          + (float)(Math.sin(this.topCamera.getYaw()) * distanceToCenterAtGroundLevel));
      this.topCamera.setY((this.aerialViewBoundsLowerPoint [1] + this.aerialViewBoundsUpperPoint [1]) / 2 
          - (float)(Math.cos(this.topCamera.getYaw()) * distanceToCenterAtGroundLevel));
      this.topCamera.setZ((this.aerialViewBoundsLowerPoint [2] + this.aerialViewBoundsUpperPoint [2]) / 2 
          + (float)Math.sin(this.topCamera.getPitch()) * distanceToCenter);
    }

    @Override
    public void rotateCameraYaw(float delta) {
      float newYaw = this.topCamera.getYaw() + delta;
      double distanceToCenterAtGroundLevel = getCameraToAerialViewCenterDistance() * Math.cos(this.topCamera.getPitch());
      // Change camera yaw and location so user turns around home
      this.topCamera.setYaw(newYaw); 
      this.topCamera.setX((this.aerialViewBoundsLowerPoint [0] + this.aerialViewBoundsUpperPoint [0]) / 2 
          + (float)(Math.sin(newYaw) * distanceToCenterAtGroundLevel));
      this.topCamera.setY((this.aerialViewBoundsLowerPoint [1] + this.aerialViewBoundsUpperPoint [1]) / 2 
          - (float)(Math.cos(newYaw) * distanceToCenterAtGroundLevel));
    }
    
    @Override
    public void rotateCameraPitch(float delta) {
      float newPitch = this.topCamera.getPitch() + delta;
      // Check new pitch is between 0 and PI / 2  
      newPitch = Math.max(newPitch, (float)0);
      newPitch = Math.min(newPitch, (float)Math.PI / 2);
      // Compute new z to keep the same distance to view center
      double distanceToCenter = getCameraToAerialViewCenterDistance();
      double distanceToCenterAtGroundLevel = distanceToCenter * Math.cos(newPitch);
      // Change camera pitch 
      this.topCamera.setPitch(newPitch); 
      this.topCamera.setX((this.aerialViewBoundsLowerPoint [0] + this.aerialViewBoundsUpperPoint [0]) / 2 
          + (float)(Math.sin(this.topCamera.getYaw()) * distanceToCenterAtGroundLevel));
      this.topCamera.setY((this.aerialViewBoundsLowerPoint [1] + this.aerialViewBoundsUpperPoint [1]) / 2 
          - (float)(Math.cos(this.topCamera.getYaw()) * distanceToCenterAtGroundLevel));
      this.topCamera.setZ((this.aerialViewBoundsLowerPoint [2] + this.aerialViewBoundsUpperPoint [2]) / 2 
          + (float)(distanceToCenter * Math.sin(newPitch)));
    }
    
    @Override
    public void goToCamera(Camera camera) {
      this.topCamera.setCamera(camera);
      this.topCamera.setTime(camera.getTime());
      this.topCamera.setLens(camera.getLens());
      updateCameraFromHomeBounds(false);
    }
    
    @Override
    public void exit() {
      this.topCamera = null;
      for (Wall wall : home.getWalls()) {
        wall.removePropertyChangeListener(this.objectChangeListener);
      }
      home.removeWallsListener(wallsListener);
      for (HomePieceOfFurniture piece : home.getFurniture()) {
        piece.removePropertyChangeListener(this.objectChangeListener);
      }
      home.removeFurnitureListener(this.furnitureListener);
      for (Room room : home.getRooms()) {
        room.removePropertyChangeListener(this.objectChangeListener);
      }
      home.removeRoomsListener(this.roomsListener);
      for (Level level : home.getLevels()) {
        level.removePropertyChangeListener(this.objectChangeListener);
      }
      home.removeLevelsListener(this.levelsListener);
      home.removeSelectionListener(this.selectionListener);
    }
  }
  
  /**
   * Preferences property listener bound to top camera state with a weak reference to avoid
   * strong link between user preferences and top camera state.  
   */
  private static class UserPreferencesChangeListener implements PropertyChangeListener {
    private WeakReference<TopCameraState>  topCameraState;

    public UserPreferencesChangeListener(TopCameraState topCameraState) {
      this.topCameraState = new WeakReference<TopCameraState>(topCameraState);
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
      // If top camera state was garbage collected, remove this listener from preferences
      TopCameraState topCameraState = this.topCameraState.get();
      UserPreferences preferences = (UserPreferences)ev.getSource();
      if (topCameraState == null) {
        preferences.removePropertyChangeListener(UserPreferences.Property.valueOf(ev.getPropertyName()), this);
      } else {
        topCameraState.setAerialViewCenteredOnSelectionEnabled(preferences.isAerialViewCenteredOnSelectionEnabled());
      }
    }
  }

  /**
   * Observer camera controller state. 
   */
  private class ObserverCameraState extends CameraControllerState {
    private ObserverCamera observerCamera;
    private PropertyChangeListener levelElevationChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          if (Level.Property.ELEVATION.name().equals(ev.getPropertyName())) {
            updateCameraMinimumElevation();
          }
        }
      };
    private CollectionListener<Level> levelsListener = new CollectionListener<Level>() {
        public void collectionChanged(CollectionEvent<Level> ev) {
          if (ev.getType() == CollectionEvent.Type.ADD) {
            ev.getItem().addPropertyChangeListener(levelElevationChangeListener);
          } else if (ev.getType() == CollectionEvent.Type.DELETE) {
            ev.getItem().removePropertyChangeListener(levelElevationChangeListener);
          } 
          updateCameraMinimumElevation();
        }
      };

    @Override
    public void enter() {
      this.observerCamera = (ObserverCamera)home.getCamera();
      for (Level level : home.getLevels()) {
        level.addPropertyChangeListener(this.levelElevationChangeListener);
      }
      home.addLevelsListener(this.levelsListener);
      // Select observer camera for user feedback
      home.setSelectedItems(Arrays.asList(new Selectable [] {this.observerCamera}));
    }
    
    @Override
    public void moveCamera(float delta) {
      this.observerCamera.setX(this.observerCamera.getX() - (float)Math.sin(this.observerCamera.getYaw()) * delta);
      this.observerCamera.setY(this.observerCamera.getY() + (float)Math.cos(this.observerCamera.getYaw()) * delta);
      // Select observer camera for user feedback
      home.setSelectedItems(Arrays.asList(new Selectable [] {this.observerCamera}));
    }
    
    @Override
    public void elevateCamera(float delta) {
      float newElevation = this.observerCamera.getZ() + delta; 
      newElevation = Math.min(Math.max(newElevation, getMinimumElevation()), preferences.getLengthUnit().getMaximumElevation());
      this.observerCamera.setZ(newElevation);
      // Select observer camera for user feedback
      home.setSelectedItems(Arrays.asList(new Selectable [] {this.observerCamera}));
    }

    private void updateCameraMinimumElevation() {
      observerCamera.setZ(Math.max(observerCamera.getZ(), getMinimumElevation()));
    }

    public float getMinimumElevation() {
      List<Level> levels = home.getLevels();
      if (levels.size() > 0) {
        return 10 + levels.get(0).getElevation();
      } else {
        return 10;
      }
    }
    
    @Override
    public void rotateCameraYaw(float delta) {
      this.observerCamera.setYaw(this.observerCamera.getYaw() + delta); 
      // Select observer camera for user feedback
      home.setSelectedItems(Arrays.asList(new Selectable [] {this.observerCamera}));
    }
    
    @Override
    public void rotateCameraPitch(float delta) {
      float newPitch = this.observerCamera.getPitch() + delta; 
      // Check new angle is between -60° and 75°  
      newPitch = Math.max(newPitch, -(float)Math.PI / 3);
      newPitch = Math.min(newPitch, (float)Math.PI / 36 * 15);
      this.observerCamera.setPitch(newPitch); 
      // Select observer camera for user feedback
      home.setSelectedItems(Arrays.asList(new Selectable [] {this.observerCamera}));
    }
    
    @Override
    public void goToCamera(Camera camera) {
      this.observerCamera.setCamera(camera);
      this.observerCamera.setTime(camera.getTime());
      this.observerCamera.setLens(camera.getLens());
    }
    
    @Override
    public void exit() {
      // Remove observer camera from selection
      List<Selectable> selectedItems = home.getSelectedItems();
      if (selectedItems.contains(this.observerCamera)) {
        selectedItems = new ArrayList<Selectable>(selectedItems);
        selectedItems.remove(this.observerCamera);
        home.setSelectedItems(selectedItems);
      }
      for (Level room : home.getLevels()) {
        room.removePropertyChangeListener(this.levelElevationChangeListener);
      }
      home.removeLevelsListener(this.levelsListener);
      this.observerCamera = null;
    }
  }
}
