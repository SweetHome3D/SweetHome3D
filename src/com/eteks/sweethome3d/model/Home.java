/*
 * Home.java 15 mai 2006
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
package com.eteks.sweethome3d.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * The home managed by the application with its furniture and walls.
 * @author Emmanuel Puybaret
 */
public class Home implements Serializable {
  private static final long serialVersionUID = 1L;

  /**
   * The current version of this home. Each time the field list is changed
   * in <code>Home</code> class or in one of the classes that it uses,
   * this number is increased.
   */
  public static final long CURRENT_VERSION = 1200;
  
  public enum Property {NAME, MODIFIED,
    FURNITURE_SORTED_PROPERTY, FURNITURE_DESCENDING_SORTED, FURNITURE_VISIBLE_PROPERTIES,
    BACKGROUND_IMAGE, CAMERA, SKY_COLOR, GROUND_COLOR, LIGHT_COLOR, WALLS_ALPHA, PRINT};
  
  private List<HomePieceOfFurniture>                  furniture;
  private transient List<Object>                      selectedItems;
  private transient List<FurnitureListener>           furnitureListeners;
  private transient List<SelectionListener>           selectionListeners;
  private Collection<Wall>                            walls;
  private transient List<WallListener>                wallListeners;
  private Collection<DimensionLine>                   dimensionLines;
  private transient List<DimensionLineListener>       dimensionLineListeners;
  private Camera                                      camera;
  private transient List<CameraListener>              cameraListeners;
  private float                                       wallHeight;
  private String                                      name;
  private HomePieceOfFurniture.SortableProperty       furnitureSortedProperty;
  private boolean                                     furnitureDescendingSorted;
  private List<HomePieceOfFurniture.SortableProperty> furnitureVisibleProperties;
  private transient boolean                           modified;
  private BackgroundImage                             backgroundImage;
  private ObserverCamera                              observerCamera;
  private Camera                                      topCamera;
  private int                                         skyColor;
  private int                                         groundColor;
  private int                                         lightColor;
  private float                                       wallsAlpha;
  private HomePrint                                   print;
  private long                                        version;
  private transient PropertyChangeSupport             propertyChangeSupport;


  /**
   * Creates a home with no furniture, no walls, 
   * and a height equal to 250 cm.
   */
  public Home() {
    this(250);
  }

  /**
   * Creates a home with no furniture and no walls.
   */
  public Home(float wallHeight) {
    this(new ArrayList<HomePieceOfFurniture>(), wallHeight);
  }

  /**
   * Creates a home with the given <code>furniture</code>, 
   * no walls and a height equal to 250 cm.
   */
  public Home(List<HomePieceOfFurniture> furniture) {
    this(furniture, 250);
  }

  private Home(List<HomePieceOfFurniture> furniture, float wallHeight) {
    this.furniture = new ArrayList<HomePieceOfFurniture>(furniture);
    this.walls = new ArrayList<Wall>();
    this.wallHeight = wallHeight;
    this.furnitureVisibleProperties = Arrays.asList(new HomePieceOfFurniture.SortableProperty [] {
        HomePieceOfFurniture.SortableProperty.NAME,
        HomePieceOfFurniture.SortableProperty.WIDTH,
        HomePieceOfFurniture.SortableProperty.DEPTH,
        HomePieceOfFurniture.SortableProperty.HEIGHT,
        HomePieceOfFurniture.SortableProperty.VISIBLE});
    // Init transient lists and other fields
    init();
  }

  /**
   * Initializes new and transient home fields to their default values 
   * and reads home from <code>in</code> stream with default reading method.
   */
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    init();
    in.defaultReadObject();
  }

  private void init() {
    // Init transient lists
    this.selectedItems = new ArrayList<Object>();
    this.furnitureListeners = new ArrayList<FurnitureListener>();
    this.selectionListeners = new ArrayList<SelectionListener>();
    this.wallListeners = new ArrayList<WallListener>();
    this.dimensionLineListeners = new ArrayList<DimensionLineListener>();
    this.cameraListeners = new ArrayList<CameraListener>();
    this.propertyChangeSupport = new PropertyChangeSupport(this);

    if (this.furnitureVisibleProperties == null) {
      // Set the furniture properties that were visible before version 0.19 
      this.furnitureVisibleProperties = Arrays.asList(new HomePieceOfFurniture.SortableProperty [] {
          HomePieceOfFurniture.SortableProperty.NAME,
          HomePieceOfFurniture.SortableProperty.WIDTH,
          HomePieceOfFurniture.SortableProperty.DEPTH,
          HomePieceOfFurniture.SortableProperty.HEIGHT,
          HomePieceOfFurniture.SortableProperty.COLOR,
          HomePieceOfFurniture.SortableProperty.MOVABLE,
          HomePieceOfFurniture.SortableProperty.DOOR_OR_WINDOW,
          HomePieceOfFurniture.SortableProperty.VISIBLE});
    }
    // Create a default top camera that matches default point of view in previous versions 
    this.topCamera = new Camera(500, 1500, 1000, 
        (float)Math.PI, (float)Math.PI / 4, (float)Math.PI * 63 / 180);
    // Create a default observer camera (use a 63° field of view equivalent to a 35mm lens for a 24x36 film)
    this.observerCamera = new ObserverCamera(100, 100, 170, 
        3 * (float)Math.PI / 4, (float)Math.PI / 16, (float)Math.PI * 63 / 180);
    // Initialize new fields 
    this.skyColor = (204 << 16) + (228 << 8) + 252;
    this.groundColor = 0xE0E0E0;
    this.lightColor = 0xF0F0F0;
    this.dimensionLines = new ArrayList<DimensionLine>();
    
    this.version = CURRENT_VERSION;
  }

  /**
   * Sets the version of this home and writes it to <code>out</code> stream
   * with default writing method. 
   */
  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    this.version = CURRENT_VERSION;
    out.defaultWriteObject();
  }
  
  /**
   * Adds the furniture <code>listener</code> in parameter to this home.
   */
  public void addFurnitureListener(FurnitureListener listener) {
    this.furnitureListeners.add(listener);
  }

  /**
   * Removes the furniture <code>listener</code> in parameter from this home.
   */
  public void removeFurnitureListener(FurnitureListener listener) {
    this.furnitureListeners.remove(listener);
  }

  /**
   * Returns an unmodifiable list of the furniture managed by this home. 
   * This furniture in this list is always sorted in the index order they were added to home. 
   */
  public List<HomePieceOfFurniture> getFurniture() {
    return Collections.unmodifiableList(this.furniture);
  }

  /**
   * Adds a <code>piece</code> in parameter.
   * Once the <code>piece</code> is added, furniture listeners added to this home will receive a
   * {@link FurnitureListener#pieceOfFurnitureChanged(FurnitureEvent) pieceOfFurnitureChanged}
   * notification.
   */
  public void addPieceOfFurniture(HomePieceOfFurniture piece) {
    addPieceOfFurniture(piece, this.furniture.size());
  }

  /**
   * Adds the <code>piece</code> in parameter at a given <code>index</code>.
   * Once the <code>piece</code> is added, furniture listeners added to this home will receive a
   * {@link FurnitureListener#pieceOfFurnitureChanged(FurnitureEvent) pieceOfFurnitureChanged}
   * notification.
   */
  public void addPieceOfFurniture(HomePieceOfFurniture piece, int index) {
    // Make a copy of the list to avoid conflicts in the list returned by getFurniture
    this.furniture = new ArrayList<HomePieceOfFurniture>(this.furniture);
    this.furniture.add(index, piece);
    firePieceOfFurnitureChanged(piece, index, FurnitureEvent.Type.ADD);
  }

  /**
   * Deletes the <code>piece</code> in parameter from this home.
   * Once the <code>piece</code> is deleted, furniture listeners added to this home will receive a
   * {@link FurnitureListener#pieceOfFurnitureChanged(FurnitureEvent) pieceOfFurnitureChanged}
   * notification.
   */
  public void deletePieceOfFurniture(HomePieceOfFurniture piece) {
    // Ensure selectedItems don't keep a reference to piece
    deselectItem(piece);
    int index = this.furniture.indexOf(piece);
    if (index != -1) {
      // Make a copy of the list to avoid conflicts in the list returned by getFurniture
      this.furniture = new ArrayList<HomePieceOfFurniture>(this.furniture);
      this.furniture.remove(index);
      firePieceOfFurnitureChanged(piece, index, FurnitureEvent.Type.DELETE);
    }
  }

  /**
   * Updates the name of <code>piece</code>. 
   * Once the <code>piece</code> is updated, furniture listeners added to this home will receive a
   * {@link FurnitureListener#pieceOfFurnitureChanged(FurnitureEvent) pieceOfFurnitureChanged}
   * notification.
   */
  public void setPieceOfFurnitureName(HomePieceOfFurniture piece, 
                                      String name) {
    if ((piece.getName() == null && name != null)
        || (piece.getName() != null && !piece.getName().equals(name))) {
      piece.setName(name);
      firePieceOfFurnitureChanged(piece, this.furniture.indexOf(piece), FurnitureEvent.Type.UPDATE);
    }
  }

  /**
   * Updates the location of <code>piece</code>. 
   * Once the <code>piece</code> is updated, furniture listeners added to this home will receive a
   * {@link FurnitureListener#pieceOfFurnitureChanged(FurnitureEvent) pieceOfFurnitureChanged}
   * notification.
   */
  public void setPieceOfFurnitureLocation(HomePieceOfFurniture piece, 
                                          float x, float y) {
    if (piece.getX() != x
        || piece.getY() != y) {
      piece.setX(x);
      piece.setY(y);
      firePieceOfFurnitureChanged(piece, this.furniture.indexOf(piece), FurnitureEvent.Type.UPDATE);
    }
  }
  
  /**
   * Updates the elevation of <code>piece</code>. 
   * Once the <code>piece</code> is updated, furniture listeners added to this home will receive a
   * {@link FurnitureListener#pieceOfFurnitureChanged(FurnitureEvent) pieceOfFurnitureChanged}
   * notification.
   */
  public void setPieceOfFurnitureElevation(HomePieceOfFurniture piece, 
                                           float elevation) {
    if (piece.getElevation() != elevation) {
      piece.setElevation(elevation);
      firePieceOfFurnitureChanged(piece, this.furniture.indexOf(piece), FurnitureEvent.Type.UPDATE);
    }
  }

  /**
   * Updates the angle of <code>piece</code>. 
   * Once the <code>piece</code> is updated, furniture listeners added to this home will receive a
   * {@link FurnitureListener#pieceOfFurnitureChanged(FurnitureEvent) pieceOfFurnitureChanged}
   * notification.
   */
  public void setPieceOfFurnitureAngle(HomePieceOfFurniture piece, 
                                       float angle) {
    if (piece.getAngle() != angle) {
      piece.setAngle(angle);
      firePieceOfFurnitureChanged(piece, this.furniture.indexOf(piece), FurnitureEvent.Type.UPDATE);
    }
  }

  /**
   * Updates the size of <code>piece</code>. 
   * Once the <code>piece</code> is updated, furniture listeners added to this home will receive a
   * {@link FurnitureListener#pieceOfFurnitureChanged(FurnitureEvent) pieceOfFurnitureChanged}
   * notification.
   */
  public void setPieceOfFurnitureSize(HomePieceOfFurniture piece, 
                                      float width, float depth, float height) {
    if (piece.getWidth() != width
        || piece.getDepth() != depth
        || piece.getHeight() != height) {
      piece.setWidth(width);
      piece.setDepth(depth);
      piece.setHeight(height);
      firePieceOfFurnitureChanged(piece, this.furniture.indexOf(piece), FurnitureEvent.Type.UPDATE);
    }
  }
  
  /**
   * Updates the color of <code>piece</code>. 
   * Once the <code>piece</code> is updated, furniture listeners added to this home will receive a
   * {@link FurnitureListener#pieceOfFurnitureChanged(FurnitureEvent) pieceOfFurnitureChanged}
   * notification.
   */
  public void setPieceOfFurnitureColor(HomePieceOfFurniture piece, 
                                       Integer color) {
    Integer pieceColor = piece.getColor(); 
    if ((pieceColor == null && color != null)
        || (pieceColor != null && !pieceColor.equals(color))) {
      piece.setColor(color);
      firePieceOfFurnitureChanged(piece, this.furniture.indexOf(piece), FurnitureEvent.Type.UPDATE);
    }
  }

  /**
   * Updates the visibility of <code>piece</code>. 
   * Once the <code>piece</code> is updated, furniture listeners added to this home will receive a
   * {@link FurnitureListener#pieceOfFurnitureChanged(FurnitureEvent) pieceOfFurnitureChanged}
   * notification.
   */
  public void setPieceOfFurnitureVisible(HomePieceOfFurniture piece, 
                                         boolean visible) {
    if (piece.isVisible() != visible) {
      piece.setVisible(visible);
      firePieceOfFurnitureChanged(piece, this.furniture.indexOf(piece), FurnitureEvent.Type.UPDATE);
    }
  }

  /**
   * Updates the mirrored model state of <code>piece</code>. 
   * Once the <code>piece</code> is updated, furniture listeners added to this home will receive a
   * {@link FurnitureListener#pieceOfFurnitureChanged(FurnitureEvent) pieceOfFurnitureChanged}
   * notification.
   */
  public void setPieceOfFurnitureModelMirrored(HomePieceOfFurniture piece, 
                                               boolean modelMirrored) {
    if (piece.isModelMirrored() != modelMirrored) {
      piece.setModelMirrored(modelMirrored);
      firePieceOfFurnitureChanged(piece, this.furniture.indexOf(piece), FurnitureEvent.Type.UPDATE);
    }
  }

  private void firePieceOfFurnitureChanged(HomePieceOfFurniture piece, int index, 
                                           FurnitureEvent.Type eventType) {
    if (!this.furnitureListeners.isEmpty()) {
      FurnitureEvent furnitureEvent = 
          new FurnitureEvent(this, piece, index, eventType);
      // Work on a copy of furnitureListeners to ensure a listener 
      // can modify safely listeners list
      FurnitureListener [] listeners = this.furnitureListeners.
        toArray(new FurnitureListener [this.furnitureListeners.size()]);
      for (FurnitureListener listener : listeners) {
        listener.pieceOfFurnitureChanged(furnitureEvent);
      }
    }
  }

  /**
   * Adds the selection <code>listener</code> in parameter to this home.
   */
  public void addSelectionListener(SelectionListener listener) {
    this.selectionListeners.add(listener);
  }

  /**
   * Removes the selection <code>listener</code> in parameter from this home.
   */
  public void removeSelectionListener(SelectionListener listener) {
    this.selectionListeners.remove(listener);
  }

  /**
   * Returns an unmodifiable list of the selected items in home.
   */
  public List<Object> getSelectedItems() {
    return Collections.unmodifiableList(this.selectedItems);
  }
  
  /**
   * Sets the selected items in home and notifies listeners selection change.
   */
  public void setSelectedItems(List<? extends Object> selectedItems) {
    // Make a copy of the list to avoid conflicts in the list returned by getSelectedItems
    this.selectedItems = new ArrayList<Object>(selectedItems);
    if (!this.selectionListeners.isEmpty()) {
      SelectionEvent selectionEvent = new SelectionEvent(this, getSelectedItems());
      // Work on a copy of selectionListeners to ensure a listener 
      // can modify safely listeners list
      SelectionListener [] listeners = this.selectionListeners.
        toArray(new SelectionListener [this.selectionListeners.size()]);
      for (SelectionListener listener : listeners) {
        listener.selectionChanged(selectionEvent);
      }
    }
  }

  /**
   * Deselects <code>item</code> if it's selected.
   */
  private void deselectItem(Object item) {
    int pieceSelectionIndex = this.selectedItems.indexOf(item);
    if (pieceSelectionIndex != -1) {
      List<Object> selectedItems = new ArrayList<Object>(getSelectedItems());
      selectedItems.remove(pieceSelectionIndex);
      setSelectedItems(selectedItems);
    }
  }

  /**
   * Adds the wall <code>listener</code> in parameter to this home.
   */
  public void addWallListener(WallListener listener) {
    this.wallListeners.add(listener);
  }
  
  /**
   * Removes the wall <code>listener</code> in parameter from this home.
   */
  public void removeWallListener(WallListener listener) {
    this.wallListeners.remove(listener);
  } 

  /**
   * Returns an unmodifiable collection of the walls of this home.
   */
  public Collection<Wall> getWalls() {
    return Collections.unmodifiableCollection(this.walls);
  }

  /**
   * Adds a given <code>wall</code> to the set of walls of this home.
   * Once the <code>wall</code> is added, wall listeners added to this home will receive a
   * {@link WallListener#wallChanged(WallEvent) wallChanged}
   * notification, with an {@link WallEvent#getType() event type} 
   * equal to {@link WallEvent.Type#ADD ADD}. 
   */
  public void addWall(Wall wall) {
    // Make a copy of the list to avoid conflicts in the list returned by getWalls
    this.walls = new ArrayList<Wall>(this.walls);
    this.walls.add(wall);
    fireWallEvent(wall, WallEvent.Type.ADD);
  }

  /**
   * Removes a given <code>wall</code> from the set of walls of this home.
   * Once the <code>wall</code> is removed, wall listeners added to this home will receive a
   * {@link WallListener#wallChanged(WallEvent) wallChanged}
   * notification, with an {@link WallEvent#getType() event type} 
   * equal to {@link WallEvent.Type#DELETE DELETE}.
   * If any wall is attached to <code>wall</code> they will be detached from it ;
   * therefore wall listeners will receive a 
   * {@link WallListener#wallChanged(WallEvent) wallChanged}
   * notification, with an {@link WallEvent#getType() event type} 
   * equal to {@link WallEvent.Type#UPDATE UPDATE}. 
   */
  public void deleteWall(Wall wall) {
    //  Ensure selectedItems don't keep a reference to wall
    deselectItem(wall);
    // Detach any other wall attached to wall
    for (Wall otherWall : getWalls()) {
      if (wall.equals(otherWall.getWallAtStart())) {
        setWallAtStart(otherWall, null);
      } else if (wall.equals(otherWall.getWallAtEnd())) {
        setWallAtEnd(otherWall, null);
      }
    }
    // Make a copy of the list to avoid conflicts in the list returned by getWalls
    this.walls = new ArrayList<Wall>(this.walls);
    this.walls.remove(wall);
    fireWallEvent(wall, WallEvent.Type.DELETE);
  }

  /**
   * Moves <code>wall</code> start point to (<code>x</code>, <code>y</code>).
   * Once the <code>wall</code> is updated, wall listeners added to this home will receive a
   * {@link WallListener#wallChanged(WallEvent) wallChanged}
   * notification, with an {@link WallEvent#getType() event type} 
   * equal to {@link WallEvent.Type#UPDATE UPDATE}. 
   * No change is made on walls attached to <code>wall</code>.
   */
  public void moveWallStartPointTo(Wall wall, float x, float y) {
    if (x != wall.getXStart() || y != wall.getYStart()) {
      wall.setXStart(x);
      wall.setYStart(y);
      fireWallEvent(wall, WallEvent.Type.UPDATE);
    }
  }

  /**
   * Moves <code>wall</code> end point to (<code>x</code>, <code>y</code>) pixels.
   * Once the <code>wall</code> is updated, wall listeners added to this home will receive a
   * {@link WallListener#wallChanged(WallEvent) wallChanged}
   * notification, with an {@link WallEvent#getType() event type} 
   * equal to {@link WallEvent.Type#UPDATE UPDATE}. 
   * No change is made on walls attached to <code>wall</code>.
   */
  public void moveWallEndPointTo(Wall wall, float x, float y) {
    if (x != wall.getXEnd() || y != wall.getYEnd()) {
      wall.setXEnd(x);
      wall.setYEnd(y);
      fireWallEvent(wall, WallEvent.Type.UPDATE);
    }
  }

  /**
   * Sets the <code>thickness</code> of <code>wall</code>.
   * Once the <code>wall</code> is updated, wall listeners added to this home will receive a
   * {@link WallListener#wallChanged(WallEvent) wallChanged}
   * notification, with an {@link WallEvent#getType() event type} 
   * equal to {@link WallEvent.Type#UPDATE UPDATE}. 
   */
  public void setWallThickness(Wall wall, float thickness) {
    if (thickness != wall.getThickness()) {
      wall.setThickness(thickness);
      fireWallEvent(wall, WallEvent.Type.UPDATE);
    }    
  }

  /**
   * Sets the left side <code>color</code> of <code>wall</code>.
   * Once the <code>wall</code> is updated, wall listeners added to this home will receive a
   * {@link WallListener#wallChanged(WallEvent) wallChanged}
   * notification, with an {@link WallEvent#getType() event type} 
   * equal to {@link WallEvent.Type#UPDATE UPDATE}. 
   */
  public void setWallLeftSideColor(Wall wall, Integer color) {
    Integer wallColor = wall.getLeftSideColor(); 
    if ((wallColor == null && color != null)
        || (wallColor != null && !wallColor.equals(color))) {
      wall.setLeftSideColor(color);
      fireWallEvent(wall, WallEvent.Type.UPDATE);
    }
  }

  /**
   * Sets the left side <code>texture</code> of <code>wall</code>.
   * Once the <code>wall</code> is updated, wall listeners added to this home will receive a
   * {@link WallListener#wallChanged(WallEvent) wallChanged}
   * notification, with an {@link WallEvent#getType() event type} 
   * equal to {@link WallEvent.Type#UPDATE UPDATE}. 
   */
  public void setWallLeftSideTexture(Wall wall, HomeTexture texture) {
    HomeTexture wallTexture = wall.getLeftSideTexture(); 
    if ((wallTexture == null && texture != null)
        || (wallTexture != null && !wallTexture.equals(texture))) {
      wall.setLeftSideTexture(texture);
      fireWallEvent(wall, WallEvent.Type.UPDATE);
    }
  }

  /**
   * Sets the right side <code>color</code> of <code>wall</code>.
   * Once the <code>wall</code> is updated, wall listeners added to this home will receive a
   * {@link WallListener#wallChanged(WallEvent) wallChanged}
   * notification, with an {@link WallEvent#getType() event type} 
   * equal to {@link WallEvent.Type#UPDATE UPDATE}. 
   */
  public void setWallRightSideColor(Wall wall, Integer color) {
    Integer wallColor = wall.getRightSideColor(); 
    if ((wallColor == null && color != null)
        || (wallColor != null && !wallColor.equals(color))) {
      wall.setRightSideColor(color);
      fireWallEvent(wall, WallEvent.Type.UPDATE);
    }
  }

  /**
   * Sets the right side <code>texture</code> of <code>wall</code>.
   * Once the <code>wall</code> is updated, wall listeners added to this home will receive a
   * {@link WallListener#wallChanged(WallEvent) wallChanged}
   * notification, with an {@link WallEvent#getType() event type} 
   * equal to {@link WallEvent.Type#UPDATE UPDATE}. 
   */
  public void setWallRightSideTexture(Wall wall, HomeTexture texture) {
    HomeTexture wallTexture = wall.getRightSideTexture(); 
    if ((wallTexture == null && texture != null)
        || (wallTexture != null && !wallTexture.equals(texture))) {
      wall.setRightSideTexture(texture);
      fireWallEvent(wall, WallEvent.Type.UPDATE);
    }
  }

  /**
   * Sets the wall at start of <code>wall</code> as <code>wallAtEnd</code>. 
   * Once the <code>wall</code> is updated, wall listeners added to this home will receive a
   * {@link WallListener#wallChanged(WallEvent) wallChanged} notification, with
   * an {@link WallEvent#getType() event type} equal to
   * {@link WallEvent.Type#UPDATE UPDATE}. 
   * If the wall attached to <code>wall</code> start point is attached itself
   * to <code>wall</code>, this wall will be detached from <code>wall</code>, 
   * and wall listeners will receive
   * {@link WallListener#wallChanged(WallEvent) wallChanged}
   * notification about this wall, with an {@link WallEvent#getType() event type} 
   * equal to {@link WallEvent.Type#UPDATE UPDATE}. 
   * @param wallAtStart a wall or <code>null</code> to detach <code>wall</code>
   *          from any wall it was attached to before.
   */
  public void setWallAtStart(Wall wall, Wall wallAtStart) {
    detachJoinedWall(wall, wall.getWallAtStart());    
    wall.setWallAtStart(wallAtStart);
    fireWallEvent(wall, WallEvent.Type.UPDATE);
  }

  /**
   * Sets the wall at end of <code>wall</code> as <code>wallAtEnd</code>. 
   * Once the <code>wall</code> is updated, wall listeners added to this home will receive a
   * {@link WallListener#wallChanged(WallEvent) wallChanged} notification, with
   * an {@link WallEvent#getType() event type} equal to
   * {@link WallEvent.Type#UPDATE UPDATE}. 
   * If the wall attached to <code>wall</code> end point is attached itself
   * to <code>wall</code>, this wall will be detached from <code>wall</code>, 
   * and wall listeners will receive
   * {@link WallListener#wallChanged(WallEvent) wallChanged}
   * notification about this wall, with an {@link WallEvent#getType() event type} 
   * equal to {@link WallEvent.Type#UPDATE UPDATE}. 
   * @param wallAtEnd a wall or <code>null</code> to detach <code>wall</code>
   *          from any wall it was attached to before.
   */
  public void setWallAtEnd(Wall wall, Wall wallAtEnd) {
    detachJoinedWall(wall, wall.getWallAtEnd());    
    wall.setWallAtEnd(wallAtEnd);
    fireWallEvent(wall, WallEvent.Type.UPDATE);
  }

  /**
   * Detaches <code>joinedWall</code> from <code>wall</code>.
   */
  private void detachJoinedWall(Wall wall, Wall joinedWall) {
    // Detach the previously attached wall to wall in parameter
    if (joinedWall != null) {
      if (wall.equals(joinedWall.getWallAtStart())) {
        joinedWall.setWallAtStart(null);
        fireWallEvent(joinedWall, WallEvent.Type.UPDATE);
      } else if (wall.equals(joinedWall.getWallAtEnd())) {
        joinedWall.setWallAtEnd(null);
        fireWallEvent(joinedWall, WallEvent.Type.UPDATE);
      } 
    }
  }

  /**
   * Sets the <code>height</code> of the given <code>wall</code>. If <code>height</code> is
   * <code>null</code>, {@link #getWallHeight() home wall height} should be used.  
   * Once the <code>wall</code> is updated, wall listeners added to this home will receive a
   * {@link WallListener#wallChanged(WallEvent) wallChanged} notification, with
   * an {@link WallEvent#getType() event type} equal to
   * {@link WallEvent.Type#UPDATE UPDATE}. 
   */
  public void setWallHeight(Wall wall, Float height) {
    Float wallHeight = wall.getHeight(); 
    if ((wallHeight == null && height != null)
        || (wallHeight != null && !wallHeight.equals(height))) {
      wall.setHeight(height);
      fireWallEvent(wall, WallEvent.Type.UPDATE);
    }
  }

  /**
   * Notifies all wall listeners added to this home an event of 
   * a given type.
   */
  private void fireWallEvent(Wall wall, WallEvent.Type eventType) {
    if (!this.wallListeners.isEmpty()) {
      WallEvent wallEvent = new WallEvent(this, wall, eventType);
      // Work on a copy of wallListeners to ensure a listener 
      // can modify safely listeners list
      WallListener [] listeners = this.wallListeners.
        toArray(new WallListener [this.wallListeners.size()]);
      for (WallListener listener : listeners) {
        listener.wallChanged(wallEvent);
      }
    }
  }

  /**
   * Adds the dimension line <code>listener</code> in parameter to this home.
   */
  public void addDimensionLineListener(DimensionLineListener listener) {
    this.dimensionLineListeners.add(listener);
  }
  
  /**
   * Removes the dimension line <code>listener</code> in parameter from this home.
   */
  public void removeDimensionLineListener(DimensionLineListener listener) {
    this.dimensionLineListeners.remove(listener);
  } 

  /**
   * Returns an unmodifiable collection of the dimension lines of this home.
   */
  public Collection<DimensionLine> getDimensionLines() {
    return Collections.unmodifiableCollection(this.dimensionLines);
  }

  /**
   * Adds a given dimension line to the set of dimension lines of this home.
   * Once <code>dimensionLine</code> is added, dimension line listeners added 
   * to this home will receive a
   * {@link DimensionLineListener#dimensionLineChanged(DimensionLineEvent) dimensionLineChanged}
   * notification, with an {@link DimensionLineEvent#getType() event type} 
   * equal to {@link DimensionLineEvent.Type#ADD ADD}. 
   */
  public void addDimensionLine(DimensionLine dimensionLine) {
    // Make a copy of the list to avoid conflicts in the list returned by getDimensionLines
    this.dimensionLines = new ArrayList<DimensionLine>(this.dimensionLines);
    this.dimensionLines.add(dimensionLine);
    fireDimensionLineEvent(dimensionLine, DimensionLineEvent.Type.ADD);
  }

  /**
   * Removes a given dimension line from the set of dimension lines of this home.
   * Once <code>dimensionLine</code> is removed, dimension line listeners added 
   * to this home will receive a
   * {@link DimensionLineListener#dimensionLineChanged(DimensionLineEvent) dimensionLineChanged}
   * notification, with an {@link DimensionLineEvent#getType() event type} 
   * equal to {@link DimensionLineEvent.Type#DELETE DELETE}.
   */
  public void deleteDimensionLine(DimensionLine dimensionLine) {
    //  Ensure selectedItems don't keep a reference to dimension line
    deselectItem(dimensionLine);
    // Make a copy of the list to avoid conflicts in the list returned by getDimensionLines
    this.dimensionLines = new ArrayList<DimensionLine>(this.dimensionLines);
    this.dimensionLines.remove(dimensionLine);
    fireDimensionLineEvent(dimensionLine, DimensionLineEvent.Type.DELETE);
  }

  /**
   * Moves <code>dimensionLine</code> start point to (<code>x</code>, <code>y</code>).
   * Once <code>dimensionLine</code> is updated, dimension line listeners added 
   * to this home will receive a
   * {@link DimensionLineListener#dimensionLineChanged(DimensionLineEvent) dimensionLineChanged}
   * notification, with an {@link DimensionLineEvent#getType() event type} 
   * equal to {@link DimensionLineEvent.Type#UPDATE UPDATE}. 
   */
  public void moveDimensionLineStartPointTo(DimensionLine dimensionLine, float x, float y) {
    if (x != dimensionLine.getXStart() || y != dimensionLine.getYStart()) {
      dimensionLine.setXStart(x);
      dimensionLine.setYStart(y);
      fireDimensionLineEvent(dimensionLine, DimensionLineEvent.Type.UPDATE);
    }
  }

  /**
   * Moves <code>dimensionLine</code> end point to (<code>x</code>, <code>y</code>) pixels.
   * Once the <code>dimensionLine</code> is updated, dimension line listeners added 
   * to this home will receive a
   * {@link DimensionLineListener#dimensionLineChanged(DimensionLineEvent) dimensionLineChanged}
   * notification, with an {@link DimensionLineEvent#getType() event type} 
   * equal to {@link DimensionLineEvent.Type#UPDATE UPDATE}. 
   */
  public void moveDimensionLineEndPointTo(DimensionLine dimensionLine, float x, float y) {
    if (x != dimensionLine.getXEnd() || y != dimensionLine.getYEnd()) {
      dimensionLine.setXEnd(x);
      dimensionLine.setYEnd(y);
      fireDimensionLineEvent(dimensionLine, DimensionLineEvent.Type.UPDATE);
    }
  }

  /**
   * Sets the <code>offset</code> of a given dimension line.
   * Once the <code>dimensionLine</code> is updated, dimension line listeners added 
   * to this home will receive a
   * {@link DimensionLineListener#dimensionLineChanged(DimensionLineEvent) dimensionLineChanged}
   * notification, with an {@link DimensionLineEvent#getType() event type} 
   * equal to {@link DimensionLineEvent.Type#UPDATE UPDATE}. 
   */
  public void setDimensionLineOffset(DimensionLine dimensionLine, float offset) {
    if (offset != dimensionLine.getOffset()) {
      dimensionLine.setOffset(offset);
      fireDimensionLineEvent(dimensionLine, DimensionLineEvent.Type.UPDATE);
    }    
  }

  /**
   * Notifies all dimension line listeners added to this home an event of 
   * a given type.
   */
  private void fireDimensionLineEvent(DimensionLine dimensionLine, DimensionLineEvent.Type eventType) {
    if (!this.dimensionLineListeners.isEmpty()) {
      DimensionLineEvent dimensionLineEvent = new DimensionLineEvent(this, dimensionLine, eventType);
      // Work on a copy of dimensionLineListeners to ensure a listener 
      // can modify safely listeners list
      DimensionLineListener [] listeners = this.dimensionLineListeners.
        toArray(new DimensionLineListener [this.dimensionLineListeners.size()]);
      for (DimensionLineListener listener : listeners) {
        listener.dimensionLineChanged(dimensionLineEvent);
      }
    }
  }

  /**
   * Adds the property change <code>listener</code> in parameter to this home.
   */
  public void addPropertyChangeListener(Property property, PropertyChangeListener listener) {
    this.propertyChangeSupport.addPropertyChangeListener(property.toString(), listener);
  }

  /**
   * Removes the property change <code>listener</code> in parameter from this home.
   */
  public void removePropertyChangeListener(Property property, PropertyChangeListener listener) {
    this.propertyChangeSupport.removePropertyChangeListener(property.toString(), listener);
  }

  /**
   * Returns the wall height of this home.
   */
  public float getWallHeight() {
    return this.wallHeight;
  }

  /**
   * Returns the name of this home.
   */
  public String getName() {
    return this.name;
  }

  /**
   * Sets the name of this home and fires a <code>PropertyChangeEvent</code>.
   */
  public void setName(String name) {
    if (name != this.name
        || (name != null && !name.equals(this.name))) {
      String oldName = this.name;
      this.name = name;
      this.propertyChangeSupport.firePropertyChange(Property.NAME.toString(), oldName, name);
    }
  }

  /**
   * Returns whether the state of this home is modified or not.
   */
  public boolean isModified() {
    return this.modified;
  }

  /**
   * Sets the modified state of this home and fires a <code>PropertyChangeEvent</code>.
   */
  public void setModified(boolean modified) {
    if (modified != this.modified) {
      this.modified = modified;
      this.propertyChangeSupport.firePropertyChange(
          Property.MODIFIED.toString(), !modified, modified);
    }
  }
  
  /**
   * Returns the furniture property on which home is sorted or <code>null</code> if
   * home furniture isn't sorted.
   */
  public HomePieceOfFurniture.SortableProperty getFurnitureSortedProperty() {
    return this.furnitureSortedProperty;
  }

  /**
   * Sets the furniture property on which this home should be sorted 
   * and fires a <code>PropertyChangeEvent</code>.
   */
  public void setFurnitureSortedProperty(HomePieceOfFurniture.SortableProperty furnitureSortedProperty) {
    if (furnitureSortedProperty != this.furnitureSortedProperty
        || (furnitureSortedProperty != null && !furnitureSortedProperty.equals(this.furnitureSortedProperty))) {
      HomePieceOfFurniture.SortableProperty oldFurnitureSortedProperty = this.furnitureSortedProperty;
      this.furnitureSortedProperty = furnitureSortedProperty;
      this.propertyChangeSupport.firePropertyChange(
          Property.FURNITURE_SORTED_PROPERTY.toString(), 
          oldFurnitureSortedProperty, furnitureSortedProperty);
    }
  }

  /**
   * Returns whether furniture is sorted in ascending or descending order.
   */
  public boolean isFurnitureDescendingSorted() {
    return this.furnitureDescendingSorted;
  }
  
  /**
   * Sets the furniture sort order on which home should be sorted 
   * and fires a <code>PropertyChangeEvent</code>.
   */
  public void setFurnitureDescendingSorted(boolean furnitureDescendingSorted) {
    if (furnitureDescendingSorted != this.furnitureDescendingSorted) {
      this.furnitureDescendingSorted = furnitureDescendingSorted;
      this.propertyChangeSupport.firePropertyChange(
          Property.FURNITURE_DESCENDING_SORTED.toString(), 
          !furnitureDescendingSorted, furnitureDescendingSorted);
    }
  }

  /**
   * Returns an unmodifiable list of the furniture properties that are visible.
   */
  public List<HomePieceOfFurniture.SortableProperty> getFurnitureVisibleProperties() {
    return Collections.unmodifiableList(this.furnitureVisibleProperties);
  }
  
  /**
   * Sets the furniture properties that are visible and the order in which they are visible,
   * then fires a <code>PropertyChangeEvent</code>.
   */
  public void setFurnitureVisibleProperties(List<HomePieceOfFurniture.SortableProperty> furnitureVisibleProperties) {
    if (furnitureVisibleProperties != this.furnitureVisibleProperties
        || (furnitureVisibleProperties != null && !furnitureVisibleProperties.equals(this.furnitureVisibleProperties))) {
      List<HomePieceOfFurniture.SortableProperty> oldFurnitureVisibleProperties = this.furnitureVisibleProperties;
      this.furnitureVisibleProperties = new ArrayList<HomePieceOfFurniture.SortableProperty>(furnitureVisibleProperties);
      this.propertyChangeSupport.firePropertyChange(
          Property.FURNITURE_VISIBLE_PROPERTIES.toString(), 
          Collections.unmodifiableList(oldFurnitureVisibleProperties), 
          Collections.unmodifiableList(furnitureVisibleProperties));
    }
  }

  /**
   * Returns the background image of this home.
   */
  public BackgroundImage getBackgroundImage() {
    return this.backgroundImage;
  }

  /**
   * Sets the background image of this home and fires a <code>PropertyChangeEvent</code>.
   */
  public void setBackgroundImage(BackgroundImage backgroundImage) {
    if (backgroundImage != this.backgroundImage) {
      BackgroundImage oldBackgroundImage = this.backgroundImage;
      this.backgroundImage = backgroundImage;
      this.propertyChangeSupport.firePropertyChange(
          Property.BACKGROUND_IMAGE.toString(), oldBackgroundImage, backgroundImage);
    }
  }

  /**
   * Adds the camera <code>listener</code> in parameter to this home.
   */
  public void addCameraListener(CameraListener listener) {
    this.cameraListeners.add(listener);
  }
  
  /**
   * Removes the camera <code>listener</code> in parameter from this home.
   */
  public void removeCameraListener(CameraListener listener) {
    this.cameraListeners.remove(listener);
  } 

  /**
   * Returns the camera used to display this home from a top point of view.
   */
  public Camera getTopCamera() {
    return this.topCamera;
  }
  
  /**
   * Returns the camera used to display this home from an observer point of view.
   */
  public ObserverCamera getObserverCamera() {
    return this.observerCamera;
  }
  
  /**
   * Sets the camera used to display this home and fires a <code>PropertyChangeEvent</code>.
   */
  public void setCamera(Camera camera) {
    if (camera != this.camera) {
      Camera oldCamera = this.camera;
      this.camera = camera;
      this.propertyChangeSupport.firePropertyChange(
          Property.CAMERA.toString(), oldCamera, camera);
    }
  }

  /**
   * Returns the camera used to display this home.
   */
  public Camera getCamera() {
    if (this.camera == null) {
      // Use by default top camera
      this.camera = getTopCamera();
    }
    return this.camera;
  }

  /**
   * Updates the location of <code>camera</code>. 
   * Once the <code>camera</code> is updated, camera listeners added to this home will receive a
   * {@link CameraListener#cameraChanged(CameraEvent) cameraChanged}
   * notification.
   */
  public void setCameraLocation(Camera camera, 
                                float x, float y, float z) {
    if (camera.getX() != x
        || camera.getY() != y
        || camera.getZ() != z) {
      camera.setX(x);
      camera.setY(y);
      camera.setZ(z);
      fireCameraChanged(camera);
    }
  }
  
  /**
   * Updates the yaw and pitch angles of <code>camera</code>. 
   * Once the <code>camera</code> is updated, camera listeners added to this home will receive a
   * {@link CameraListener#cameraChanged(CameraEvent) cameraChanged}
   * notification.
   */
  public void setCameraAngles(Camera camera, float yaw, float pitch) {
    if (camera.getYaw() != yaw
        || camera.getPitch() != pitch) {
      camera.setYaw(yaw);
      camera.setPitch(pitch);
      fireCameraChanged(camera);
    }
  }

  /**
   * Updates the field of view angle of <code>camera</code>. 
   * Once the <code>camera</code> is updated, camera listeners added to this home will receive a
   * {@link CameraListener#cameraChanged(CameraEvent) cameraChanged}
   * notification.
   */
  public void setCameraFieldOfView(Camera camera, float fieldOfView) {
    if (camera.getFieldOfView() != fieldOfView) {
      camera.setFieldOfView(fieldOfView);
      fireCameraChanged(camera);
    }
  }

  private void fireCameraChanged(Camera piece) {
    if (!this.cameraListeners.isEmpty()) {
      CameraEvent cameraEvent = new CameraEvent(this, piece);
      // Work on a copy of furnitureListeners to ensure a listener 
      // can modify safely listeners list
      CameraListener [] listeners = this.cameraListeners.
        toArray(new CameraListener [this.cameraListeners.size()]);
      for (CameraListener listener : listeners) {
        listener.cameraChanged(cameraEvent);
      }
    }
  }

  /**
   * Returns the ground color of this home.
   */
  public int getGroundColor() {
    return this.groundColor;
  }

  /**
   * Sets the ground color of this home and fires a <code>PropertyChangeEvent</code>.
   */
  public void setGroundColor(int groundColor) {
    if (groundColor != this.groundColor) {
      int oldGroundColor = this.groundColor;
      this.groundColor = groundColor;
      this.propertyChangeSupport.firePropertyChange(
          Property.GROUND_COLOR.toString(), oldGroundColor, groundColor);
    }
  }

  /**
   * Returns the sky color of this home.
   */
  public int getSkyColor() {
    return this.skyColor;
  }
  
  /**
   * Sets the sky color of this home and fires a <code>PropertyChangeEvent</code>.
   */
  public void setSkyColor(int skyColor) {
    if (skyColor != this.skyColor) {
      int oldSkyColor = this.skyColor;
      this.skyColor = skyColor;
      this.propertyChangeSupport.firePropertyChange(
          Property.SKY_COLOR.toString(), oldSkyColor, skyColor);
    }
  }
  
  /**
   * Returns the light color of this home.
   */
  public int getLightColor() {
    return this.lightColor;
  }

  /**
   * Sets the color that lights this home and fires a <code>PropertyChangeEvent</code>.
   */
  public void setLightColor(int lightColor) {
    if (lightColor != this.lightColor) {
      int oldLightColor = this.lightColor;
      this.lightColor = lightColor;
      this.propertyChangeSupport.firePropertyChange(
          Property.LIGHT_COLOR.toString(), oldLightColor, lightColor);
    }
  }

  /**
   * Returns the walls transparency alpha factor of this home.
   */
  public float getWallsAlpha() {
    return this.wallsAlpha;
  }

  /**
   * Sets the walls transparency alpha of this home and fires a <code>PropertyChangeEvent</code>.
   * @param wallsAlpha a value between 0 and 1, 0 meaning opaque and 1 invisible.
   */
  public void setWallsAlpha(float wallsAlpha) {
    if (wallsAlpha != this.wallsAlpha) {
      float oldWallsAlpha = this.wallsAlpha;
      this.wallsAlpha = wallsAlpha;
      this.propertyChangeSupport.firePropertyChange(
          Property.WALLS_ALPHA.toString(), oldWallsAlpha, wallsAlpha);
    }
  }

  /**
   * Returns the print attributes of this home.
   */
  public HomePrint getPrint() {
    return this.print;
  }

  /**
   * Sets the print attributes of this home and fires a <code>PropertyChangeEvent</code>.
   */
  public void setPrint(HomePrint print) {
    if (print != this.print) {
      HomePrint oldPrint = this.print;
      this.print = print;
      this.propertyChangeSupport.firePropertyChange(
          Property.PRINT.toString(), oldPrint, print);
    }
    this.print = print;
  }

  /**
   * Returns the version of this home, the last time it was serialized or 
   * or {@link #CURRENT_VERSION} if it is not serialized yet or 
   * was serialized with Sweet Home 3D 0.x.  
   * Version is usefull to know with which Sweet Home 3D version this home was saved
   * and warn user that he may lose information if he saves with 
   * current application a home created by a more recent version.
   */
  public long getVersion() {
    return this.version;
  }
  
  /**
   * Returns a sub list of <code>items</code> that contains only home furniture.
   */
  public static List<HomePieceOfFurniture> getFurnitureSubList(List<? extends Object> items) {
    return getSubList(items, HomePieceOfFurniture.class);
  }

  /**
   * Returns a sub list of <code>items</code> that contains only walls.
   */
  public static List<Wall> getWallsSubList(List<? extends Object> items) {
    return getSubList(items, Wall.class);
  }

  /**
   * Returns a sub list of <code>items</code> that contains only dimension lines.
   */
  public static List<DimensionLine> getDimensionLinesSubList(List<? extends Object> items) {
    return getSubList(items, DimensionLine.class);
  }
  
  @SuppressWarnings("unchecked")
  private static <T> List<T> getSubList(List<? extends Object> items, 
                                        Class<T> subListClass) {
    List<T> subList = new ArrayList<T>();
    for (Object item : items) {
      if (subListClass.isInstance(item)) {
        subList.add((T)item);
      }
    }
    return subList;
  }
}

