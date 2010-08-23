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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The home managed by the application with its furniture and walls.
 * @author Emmanuel Puybaret
 */
public class Home implements Serializable, Cloneable {
  private static final long serialVersionUID = 1L;

  /**
   * The current version of this home. Each time the field list is changed
   * in <code>Home</code> class or in one of the classes that it uses,
   * this number is increased.
   */
  public static final long CURRENT_VERSION = 2600;
  
  private static final boolean KEEP_BACKWARD_COMPATIBLITY = true;
  
  /**
   * The properties of a home that may change. <code>PropertyChangeListener</code>s added 
   * to a home will be notified under a property name equal to the name value of one these properties.
   */
  public enum Property {NAME, MODIFIED,
    FURNITURE_SORTED_PROPERTY, FURNITURE_DESCENDING_SORTED, FURNITURE_VISIBLE_PROPERTIES,    
    BACKGROUND_IMAGE, CAMERA, PRINT, BASE_PLAN_LOCKED};
  
  private List<HomePieceOfFurniture>                  furniture;
  private transient CollectionChangeSupport<HomePieceOfFurniture> furnitureChangeSupport;
  private transient List<Selectable>                  selectedItems;
  private transient List<SelectionListener>           selectionListeners;
  private List<Wall>                                  walls;
  private transient CollectionChangeSupport<Wall>     wallsChangeSupport;
  private List<Room>                                  rooms;
  private transient CollectionChangeSupport<Room>     roomsChangeSupport;
  private List<DimensionLine>                         dimensionLines;
  private transient CollectionChangeSupport<DimensionLine> dimensionLinesChangeSupport;
  private List<Label>                                 labels;
  private transient CollectionChangeSupport<Label>    labelsChangeSupport;
  private Camera                                      camera;
  private String                                      name;
  private float                                       wallHeight;
  private transient boolean                           modified;
  private BackgroundImage                             backgroundImage;
  private ObserverCamera                              observerCamera;
  private Camera                                      topCamera;
  private HomeEnvironment                             environment;
  private HomePrint                                   print;
  private String                                      furnitureSortedPropertyName;
  private List<String>                                furnitureVisiblePropertyNames;
  private boolean                                     furnitureDescendingSorted;
  private Map<String, Object>                         visualProperties;
  private transient PropertyChangeSupport             propertyChangeSupport;
  private long                                        version;
  private boolean                                     basePlanLocked; 
  // The 5 following environment fields are still declared for compatibility reasons
  private int                                         skyColor;
  private int                                         groundColor;
  private HomeTexture                                 groundTexture;
  private int                                         lightColor;
  private float                                       wallsAlpha;
  // The two following fields aren't transient for backward compatibility reasons 
  private HomePieceOfFurniture.SortableProperty       furnitureSortedProperty;
  private List<HomePieceOfFurniture.SortableProperty> furnitureVisibleProperties;
  // The following field is a temporary copy of furniture containing HomeDoorOrWindow instances
  // created at serialization time for backward compatibility reasons
  private List<HomePieceOfFurniture>                  furnitureWithDoorsAndWindows;
  // The following field is a temporary copy of furniture containing HomeFurnitureGroup instances
  // created at serialization time for backward compatibility reasons
  private List<HomePieceOfFurniture>                  furnitureWithGroups;

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
    
    if (KEEP_BACKWARD_COMPATIBLITY) {
      // Restore furnitureSortedProperty from furnitureSortedPropertyName
      if (this.furnitureSortedPropertyName != null) {
        try {
          this.furnitureSortedProperty = 
              HomePieceOfFurniture.SortableProperty.valueOf(this.furnitureSortedPropertyName);
        } catch (IllegalArgumentException ex) {
          // Ignore malformed enum constant 
        }
        this.furnitureSortedPropertyName = null;
      }
      // Restore furnitureVisibleProperties from furnitureVisiblePropertyNames
      if (this.furnitureVisiblePropertyNames != null) {
        this.furnitureVisibleProperties = new ArrayList<HomePieceOfFurniture.SortableProperty>();
        for (String furnitureVisiblePropertyName : this.furnitureVisiblePropertyNames) {
          try {
            this.furnitureVisibleProperties.add(
                HomePieceOfFurniture.SortableProperty.valueOf(furnitureVisiblePropertyName));
          } catch (IllegalArgumentException ex) {
            // Ignore malformed enum constants 
          }
        }
        this.furnitureVisiblePropertyNames = null;
      }
  
      // Restore referenced HomeDoorOrWindow instances stored in a separate field 
      // for backward compatibility reasons
      if (this.furnitureWithDoorsAndWindows != null) {
        this.furniture = this.furnitureWithDoorsAndWindows;
        this.furnitureWithDoorsAndWindows = null;
      }

      // Restore referenced HomeFurnitureGroup instances stored in a separate field 
      // for backward compatibility reasons
      if (this.furnitureWithGroups != null) {
        this.furniture = this.furnitureWithGroups;
        this.furnitureWithGroups = null;
      }

      // Restore environment fields from home fields for compatibility reasons
      this.environment.setGroundColor(this.groundColor);
      this.environment.setGroundTexture(this.groundTexture);
      this.environment.setSkyColor(this.skyColor);
      this.environment.setLightColor(this.lightColor);
      this.environment.setWallsAlpha(this.wallsAlpha);
    }
  }

  private void init() {
    // Initialize transient lists
    this.selectedItems = new ArrayList<Selectable>();
    this.furnitureChangeSupport = new CollectionChangeSupport<HomePieceOfFurniture>(this);
    this.selectionListeners = new ArrayList<SelectionListener>();
    this.wallsChangeSupport = new CollectionChangeSupport<Wall>(this);
    this.roomsChangeSupport = new CollectionChangeSupport<Room>(this);
    this.dimensionLinesChangeSupport = new CollectionChangeSupport<DimensionLine>(this);
    this.labelsChangeSupport = new CollectionChangeSupport<Label>(this);
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
    this.observerCamera = new ObserverCamera(50, 50, 170, 
        7 * (float)Math.PI / 4, (float)Math.PI / 16, (float)Math.PI * 63 / 180);
    // Initialize new fields 
    this.environment = new HomeEnvironment();
    this.rooms = new ArrayList<Room>();
    this.dimensionLines = new ArrayList<DimensionLine>();
    this.labels = new ArrayList<Label>();
    this.visualProperties = new HashMap<String, Object>();
    
    this.version = CURRENT_VERSION;
  }

  /**
   * Sets the version of this home and writes it to <code>out</code> stream
   * with default writing method. 
   */
  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    this.version = CURRENT_VERSION;
        
    if (KEEP_BACKWARD_COMPATIBLITY) {
      HomePieceOfFurniture.SortableProperty currentFurnitureSortedProperty = this.furnitureSortedProperty;
      if (this.furnitureSortedProperty != null) {
        this.furnitureSortedPropertyName = this.furnitureSortedProperty.name();
        // Store in furnitureSortedProperty only backward compatible property
        if (!isFurnitureSortedPropertyBackwardCompatible(this.furnitureSortedProperty)) {
          this.furnitureSortedProperty = null;
        }
      }
      
      this.furnitureVisiblePropertyNames = new ArrayList<String>();
      // Store in furnitureVisibleProperties only backward compatible properties
      List<HomePieceOfFurniture.SortableProperty> currentFurnitureVisibleProperties = this.furnitureVisibleProperties;
      this.furnitureVisibleProperties = new ArrayList<HomePieceOfFurniture.SortableProperty>();
      for (HomePieceOfFurniture.SortableProperty visibleProperty : currentFurnitureVisibleProperties) {
        this.furnitureVisiblePropertyNames.add(visibleProperty.name());
        if (isFurnitureSortedPropertyBackwardCompatible(visibleProperty)) {
          this.furnitureVisibleProperties.add(visibleProperty);
        }
      }
    
      // Store referenced HomeFurnitureGroup instances in a separate field 
      // for backward compatibility reasons (version < 2.3)
      this.furnitureWithGroups = this.furniture;
      // Serialize a furnitureWithDoorsAndWindows field that contains only 
      // HomePieceOfFurniture, HomeDoorOrWindow and HomeLight instances
      // for backward compatibility reasons (version < 1.7)
      this.furnitureWithDoorsAndWindows = new ArrayList<HomePieceOfFurniture>(this.furniture.size());
      // Serialize a furniture field that contains only HomePieceOfFurniture instances
      this.furniture = new ArrayList<HomePieceOfFurniture>(this.furniture.size());
      for (HomePieceOfFurniture piece : this.furnitureWithGroups) {
        if (piece.getClass() == HomePieceOfFurniture.class) {
          this.furnitureWithDoorsAndWindows.add(piece);
          this.furniture.add(piece);
        } else {
          if (piece.getClass() == HomeFurnitureGroup.class) {
            // Add the ungrouped pieces to furniture and furnitureWithDoorsAndWindows list 
            for (HomePieceOfFurniture groupPiece : getGroupFurniture((HomeFurnitureGroup)piece)) {
              this.furnitureWithDoorsAndWindows.add(groupPiece);
              if (groupPiece.getClass() == HomePieceOfFurniture.class) {
                this.furniture.add(groupPiece);
              } else {
                // Create backward compatible instances
                this.furniture.add(new HomePieceOfFurniture(groupPiece));
              }
            }            
          } else {
            this.furnitureWithDoorsAndWindows.add(piece);
            // Create backward compatible instances
            this.furniture.add(new HomePieceOfFurniture(piece));
          }
        }
      }
      
      // Store environment fields in home fields for compatibility reasons
      this.groundColor = this.environment.getGroundColor();
      this.groundTexture = this.environment.getGroundTexture();
      this.skyColor = this.environment.getSkyColor();
      this.lightColor = this.environment.getLightColor();
      this.wallsAlpha = this.environment.getWallsAlpha();

      out.defaultWriteObject();
    
      // Restore current values
      this.furniture = this.furnitureWithGroups;
      this.furnitureWithDoorsAndWindows = null;
      this.furnitureWithGroups = null;
    
      this.furnitureSortedProperty = currentFurnitureSortedProperty;
      this.furnitureVisibleProperties = currentFurnitureVisibleProperties;
      // Set furnitureSortedPropertyName and furnitureVisiblePropertyNames to null
      // (they are used only for serialization)
      this.furnitureSortedPropertyName = null;
      this.furnitureVisiblePropertyNames = null;
    } else {
      out.defaultWriteObject();
    }
  }
  
  /**
   * Returns <code>true</code> if the given <code>property</code> is compatible 
   * with the first set of sortable properties that existed in <code>HomePieceOfFurniture</code> class.
   */
  private boolean isFurnitureSortedPropertyBackwardCompatible(HomePieceOfFurniture.SortableProperty property) {
    switch (property) {
      case NAME : 
      case WIDTH : 
      case DEPTH :
      case HEIGHT :
      case MOVABLE :
      case DOOR_OR_WINDOW :
      case COLOR :
      case VISIBLE :
      case X :
      case Y :
      case ELEVATION :
      case ANGLE :
        return true;
      default :
        return false;
    }
  }

  /**
   * Returns all the pieces of the given <code>furnitureGroup</code>.  
   */
  private List<HomePieceOfFurniture> getGroupFurniture(HomeFurnitureGroup furnitureGroup) {
    List<HomePieceOfFurniture> groupFurniture = new ArrayList<HomePieceOfFurniture>();
    for (HomePieceOfFurniture piece : furnitureGroup.getFurniture()) {
      if (piece instanceof HomeFurnitureGroup) {
        groupFurniture.addAll(getGroupFurniture((HomeFurnitureGroup)piece));
      } else {
        groupFurniture.add(piece);
      }
    }
    return groupFurniture;
  }
  
  /**
   * Adds the furniture <code>listener</code> in parameter to this home.
   */
  public void addFurnitureListener(CollectionListener<HomePieceOfFurniture> listener) {
    this.furnitureChangeSupport.addCollectionListener(listener);
  }

  /**
   * Removes the furniture <code>listener</code> in parameter from this home.
   */
  public void removeFurnitureListener(CollectionListener<HomePieceOfFurniture> listener) {
    this.furnitureChangeSupport.removeCollectionListener(listener);
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
   * {@link CollectionListener#collectionChanged(CollectionEvent) collectionChanged}
   * notification.
   */
  public void addPieceOfFurniture(HomePieceOfFurniture piece) {
    addPieceOfFurniture(piece, this.furniture.size());
  }

  /**
   * Adds the <code>piece</code> in parameter at a given <code>index</code>.
   * Once the <code>piece</code> is added, furniture listeners added to this home will receive a
   * {@link CollectionListener#collectionChanged(CollectionEvent) collectionChanged}
   * notification.
   */
  public void addPieceOfFurniture(HomePieceOfFurniture piece, int index) {
    // Make a copy of the list to avoid conflicts in the list returned by getFurniture
    this.furniture = new ArrayList<HomePieceOfFurniture>(this.furniture);
    this.furniture.add(index, piece);
    this.furnitureChangeSupport.fireCollectionChanged(piece, index, CollectionEvent.Type.ADD);
  }

  /**
   * Deletes the <code>piece</code> in parameter from this home.
   * Once the <code>piece</code> is deleted, furniture listeners added to this home will receive a
   * {@link CollectionListener#collectionChanged(CollectionEvent) collectionChanged}
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
      this.furnitureChangeSupport.fireCollectionChanged(piece, index, CollectionEvent.Type.DELETE);
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
  public List<Selectable> getSelectedItems() {
    return Collections.unmodifiableList(this.selectedItems);
  }
  
  /**
   * Sets the selected items in home and notifies listeners selection change.
   */
  public void setSelectedItems(List<? extends Selectable> selectedItems) {
    // Make a copy of the list to avoid conflicts in the list returned by getSelectedItems
    this.selectedItems = new ArrayList<Selectable>(selectedItems);
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
   * Deselects <code>item</code> if it's selected and notifies listeners selection change.
   * @since 2.2
   */
  public void deselectItem(Selectable item) {
    int pieceSelectionIndex = this.selectedItems.indexOf(item);
    if (pieceSelectionIndex != -1) {
      List<Selectable> selectedItems = new ArrayList<Selectable>(getSelectedItems());
      selectedItems.remove(pieceSelectionIndex);
      setSelectedItems(selectedItems);
    }
  }

  /**
   * Adds the wall <code>listener</code> in parameter to this home.
   */
  public void addWallsListener(CollectionListener<Wall> listener) {
    this.wallsChangeSupport.addCollectionListener(listener);
  }
  
  /**
   * Removes the wall <code>listener</code> in parameter from this home.
   */
  public void removeWallsListener(CollectionListener<Wall> listener) {
    this.wallsChangeSupport.removeCollectionListener(listener);
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
   * {@link CollectionListener#collectionChanged(CollectionEvent) collectionChanged}
   * notification, with an {@link CollectionEvent#getType() event type} 
   * equal to {@link CollectionEvent.Type#ADD ADD}. 
   */
  public void addWall(Wall wall) {
    // Make a copy of the list to avoid conflicts in the list returned by getWalls
    this.walls = new ArrayList<Wall>(this.walls);
    this.walls.add(wall);
    this.wallsChangeSupport.fireCollectionChanged(wall, CollectionEvent.Type.ADD);
  }

  /**
   * Removes a given <code>wall</code> from the set of walls of this home.
   * Once the <code>wall</code> is removed, wall listeners added to this home will receive a
   * {@link CollectionListener#collectionChanged(CollectionEvent) collectionChanged}
   * notification, with an {@link CollectionEvent#getType() event type} 
   * equal to {@link CollectionEvent.Type#DELETE DELETE}.
   * If any wall is attached to <code>wall</code> they will be detached from it.
   */
  public void deleteWall(Wall wall) {
    //  Ensure selectedItems don't keep a reference to wall
    deselectItem(wall);
    // Detach any other wall attached to wall
    for (Wall otherWall : getWalls()) {
      if (wall.equals(otherWall.getWallAtStart())) {
        otherWall.setWallAtStart(null);
      } else if (wall.equals(otherWall.getWallAtEnd())) {
        otherWall.setWallAtEnd(null);
      }
    }
    // Make a copy of the list to avoid conflicts in the list returned by getWalls
    this.walls = new ArrayList<Wall>(this.walls);
    this.walls.remove(wall);
    this.wallsChangeSupport.fireCollectionChanged(wall, CollectionEvent.Type.DELETE);
  }

  /**
   * Adds the room <code>listener</code> in parameter to this home.
   */
  public void addRoomsListener(CollectionListener<Room> listener) {
    this.roomsChangeSupport.addCollectionListener(listener);
  }
  
  /**
   * Removes the room <code>listener</code> in parameter from this home.
   */
  public void removeRoomsListener(CollectionListener<Room> listener) {
    this.roomsChangeSupport.removeCollectionListener(listener);
  } 

  /**
   * Returns an unmodifiable collection of the rooms of this home.
   */
  public List<Room> getRooms() {
    return Collections.unmodifiableList(this.rooms);
  }

  /**
   * Adds a given <code>room</code> to the list of rooms of this home.
   * Once the <code>room</code> is added, room listeners added to this home will receive a
   * {@link CollectionListener#collectionChanged(CollectionEvent) collectionChanged}
   * notification, with an {@link CollectionEvent#getType() event type} 
   * equal to {@link CollectionEvent.Type#ADD ADD}. 
   */
  public void addRoom(Room room) {
    addRoom(room, this.rooms.size());
  }

  /**
   * Adds the <code>room</code> in parameter at a given <code>index</code>.
   * Once the <code>room</code> is added, room listeners added to this home will receive a
   * {@link CollectionListener#collectionChanged(CollectionEvent) collectionChanged}
   * notification, with an {@link CollectionEvent#getType() event type} 
   * equal to {@link CollectionEvent.Type#ADD ADD}.
   */
  public void addRoom(Room room, int index) {
    // Make a copy of the list to avoid conflicts in the list returned by getRooms
    this.rooms = new ArrayList<Room>(this.rooms);
    this.rooms.add(index, room);
    this.roomsChangeSupport.fireCollectionChanged(room, index, CollectionEvent.Type.ADD);
  }

  /**
   * Removes a given <code>room</code> from the set of rooms of this home.
   * Once the <code>room</code> is removed, room listeners added to this home will receive a
   * {@link CollectionListener#collectionChanged(CollectionEvent) collectionChanged}
   * notification, with an {@link CollectionEvent#getType() event type} 
   * equal to {@link CollectionEvent.Type#DELETE DELETE}.
   */
  public void deleteRoom(Room room) {
    //  Ensure selectedItems don't keep a reference to room
    deselectItem(room);
    // Make a copy of the list to avoid conflicts in the list returned by getRooms
    this.rooms = new ArrayList<Room>(this.rooms);
    this.rooms.remove(room);
    this.roomsChangeSupport.fireCollectionChanged(room, CollectionEvent.Type.DELETE);
  }

  /**
   * Adds the dimension line <code>listener</code> in parameter to this home.
   */
  public void addDimensionLinesListener(CollectionListener<DimensionLine> listener) {
    this.dimensionLinesChangeSupport.addCollectionListener(listener);
  }
  
  /**
   * Removes the dimension line <code>listener</code> in parameter from this home.
   */
  public void removeDimensionLinesListener(CollectionListener<DimensionLine> listener) {
    this.dimensionLinesChangeSupport.removeCollectionListener(listener);
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
   * {@link CollectionListener#collectionChanged(CollectionEvent) collectionChanged}
   * notification, with an {@link CollectionEvent#getType() event type} 
   * equal to {@link CollectionEvent.Type#ADD ADD}. 
   */
  public void addDimensionLine(DimensionLine dimensionLine) {
    // Make a copy of the list to avoid conflicts in the list returned by getDimensionLines
    this.dimensionLines = new ArrayList<DimensionLine>(this.dimensionLines);
    this.dimensionLines.add(dimensionLine);
    this.dimensionLinesChangeSupport.fireCollectionChanged(dimensionLine, CollectionEvent.Type.ADD);
  }

  /**
   * Removes a given dimension line from the set of dimension lines of this home.
   * Once <code>dimensionLine</code> is removed, dimension line listeners added 
   * to this home will receive a
   * {@link CollectionListener#collectionChanged(CollectionEvent) collectionChanged}
   * notification, with an {@link CollectionEvent#getType() event type} 
   * equal to {@link CollectionEvent.Type#DELETE DELETE}.
   */
  public void deleteDimensionLine(DimensionLine dimensionLine) {
    //  Ensure selectedItems don't keep a reference to dimension line
    deselectItem(dimensionLine);
    // Make a copy of the list to avoid conflicts in the list returned by getDimensionLines
    this.dimensionLines = new ArrayList<DimensionLine>(this.dimensionLines);
    this.dimensionLines.remove(dimensionLine);
    this.dimensionLinesChangeSupport.fireCollectionChanged(dimensionLine, CollectionEvent.Type.DELETE);
  }

  /**
   * Adds the label <code>listener</code> in parameter to this home.
   */
  public void addLabelsListener(CollectionListener<Label> listener) {
    this.labelsChangeSupport.addCollectionListener(listener);
  }
  
  /**
   * Removes the label <code>listener</code> in parameter from this home.
   */
  public void removeLabelsListener(CollectionListener<Label> listener) {
    this.labelsChangeSupport.removeCollectionListener(listener);
  } 

  /**
   * Returns an unmodifiable collection of the labels of this home.
   */
  public Collection<Label> getLabels() {
    return Collections.unmodifiableCollection(this.labels);
  }

  /**
   * Adds a given label to the set of labels of this home.
   * Once <code>label</code> is added, label listeners added 
   * to this home will receive a
   * {@link CollectionListener#collectionChanged(CollectionEvent) collectionChanged}
   * notification, with an {@link CollectionEvent#getType() event type} 
   * equal to {@link CollectionEvent.Type#ADD ADD}. 
   */
  public void addLabel(Label label) {
    // Make a copy of the list to avoid conflicts in the list returned by getLabels
    this.labels = new ArrayList<Label>(this.labels);
    this.labels.add(label);
    this.labelsChangeSupport.fireCollectionChanged(label, CollectionEvent.Type.ADD);
  }

  /**
   * Removes a given label from the set of labels of this home.
   * Once <code>label</code> is removed, label listeners added to this home will receive a
   * {@link CollectionListener#collectionChanged(CollectionEvent) collectionChanged}
   * notification, with an {@link CollectionEvent#getType() event type} 
   * equal to {@link CollectionEvent.Type#DELETE DELETE}.
   */
  public void deleteLabel(Label label) {
    //  Ensure selectedItems don't keep a reference to label
    deselectItem(label);
    // Make a copy of the list to avoid conflicts in the list returned by getLabels
    this.labels = new ArrayList<Label>(this.labels);
    this.labels.remove(label);
    this.labelsChangeSupport.fireCollectionChanged(label, CollectionEvent.Type.DELETE);
  }
  
  /**
   * Returns <code>true</code> if this home doesn't contain any item i.e.  
   * no piece of furniture, no wall, no room, no dimension line and no label.
   * @since 2.2
   */
  public boolean isEmpty() {
    return this.furniture.isEmpty()
        && this.walls.isEmpty()
        && this.rooms.isEmpty()
        && this.dimensionLines.isEmpty()
        && this.labels.isEmpty();
  }

  /**
   * Adds the property change <code>listener</code> in parameter to this home.
   */
  public void addPropertyChangeListener(Property property, PropertyChangeListener listener) {
    this.propertyChangeSupport.addPropertyChangeListener(property.name(), listener);
  }

  /**
   * Removes the property change <code>listener</code> in parameter from this home.
   */
  public void removePropertyChangeListener(Property property, PropertyChangeListener listener) {
    this.propertyChangeSupport.removePropertyChangeListener(property.name(), listener);
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
      this.propertyChangeSupport.firePropertyChange(Property.NAME.name(), oldName, name);
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
          Property.MODIFIED.name(), !modified, modified);
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
          Property.FURNITURE_SORTED_PROPERTY.name(), 
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
          Property.FURNITURE_DESCENDING_SORTED.name(), 
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
          Property.FURNITURE_VISIBLE_PROPERTIES.name(), 
          Collections.unmodifiableList(oldFurnitureVisibleProperties), 
          Collections.unmodifiableList(furnitureVisibleProperties));
    }
  }

  /**
   * Returns the plan background image of this home.
   */
  public BackgroundImage getBackgroundImage() {
    return this.backgroundImage;
  }

  /**
   * Sets the plan background image of this home and fires a <code>PropertyChangeEvent</code>.
   */
  public void setBackgroundImage(BackgroundImage backgroundImage) {
    if (backgroundImage != this.backgroundImage) {
      BackgroundImage oldBackgroundImage = this.backgroundImage;
      this.backgroundImage = backgroundImage;
      this.propertyChangeSupport.firePropertyChange(
          Property.BACKGROUND_IMAGE.name(), oldBackgroundImage, backgroundImage);
    }
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
          Property.CAMERA.name(), oldCamera, camera);
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
   * Returns the environment attributes of this home.
   */
  public HomeEnvironment getEnvironment() {
    return this.environment;
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
          Property.PRINT.name(), oldPrint, print);
    }
    this.print = print;
  }
  
  /**
   * Returns the value of the visual property <code>propertyName</code> associated with this home.
   */
  public Object getVisualProperty(String propertyName) {
    return this.visualProperties.get(propertyName);
  }
  
  /**
   * Sets a visual property associated with this home.
   */
  public void setVisualProperty(String propertyName, Object propertyValue) {
    this.visualProperties.put(propertyName, propertyValue);
  }

  /**
   * Returns <code>true</code> if the home objects belonging to the base plan 
   * (generally walls, rooms, dimension lines and texts) are locked.
   * @since 1.8
   */
  public boolean isBasePlanLocked() {
    return this.basePlanLocked;
  }

  /**
   * Sets whether home objects belonging to the base plan (generally walls, rooms, 
   * dimension lines and texts) are locked and fires a <code>PropertyChangeEvent</code>.
   * @since 1.8
   */
  public void setBasePlanLocked(boolean basePlanLocked) {
    if (basePlanLocked != this.basePlanLocked) {
      this.basePlanLocked = basePlanLocked;
      this.propertyChangeSupport.firePropertyChange(
          Property.BASE_PLAN_LOCKED.name(), !basePlanLocked, basePlanLocked);
    }
  }
  
  /**
   * Returns the version of this home, the last time it was serialized or 
   * or {@link #CURRENT_VERSION} if it is not serialized yet or 
   * was serialized with Sweet Home 3D 0.x.  
   * Version is useful to know with which Sweet Home 3D version this home was saved
   * and warn user that he may lose information if he saves with 
   * current application a home created by a more recent version.
   */
  public long getVersion() {
    return this.version;
  }
  
  /**
   * Returns a clone of this home and the objects it contains. 
   * Listeners bound to this home aren't added to the returned home.
   * @since 2.3
   */
  @Override
  public Home clone() {
    try {
      Home clone = (Home)super.clone();
      // Deep clone selectable items
      clone.selectedItems = new ArrayList<Selectable>(this.selectedItems.size());
      clone.furniture = cloneSelectableItems(
          this.furniture, this.selectedItems, clone.selectedItems);
      clone.rooms = cloneSelectableItems(this.rooms, this.selectedItems, clone.selectedItems);
      clone.dimensionLines = cloneSelectableItems(
          this.dimensionLines, this.selectedItems, clone.selectedItems);
      clone.labels = cloneSelectableItems(this.labels, this.selectedItems, clone.selectedItems);
      // Deep clone walls
      clone.walls = Wall.clone(this.walls);
      for (int i = 0; i < this.walls.size(); i++) {
        Wall wall = this.walls.get(i);
        if (this.selectedItems.contains(wall)) {
          clone.selectedItems.add(clone.walls.get(i));
        }
      }
      // Clone cameras
      clone.observerCamera = this.observerCamera.clone();
      clone.topCamera = this.topCamera.clone();
      if (this.camera == this.observerCamera) {
        clone.camera = clone.observerCamera;
        if (this.selectedItems.contains(this.observerCamera)) {
          clone.selectedItems.add(clone.observerCamera);
        }
      } else {
        clone.camera = clone.topCamera;
      }
      // Clone other mutable objects
      clone.environment = this.environment.clone();
      clone.furnitureVisibleProperties = new ArrayList<HomePieceOfFurniture.SortableProperty>(
          this.furnitureVisibleProperties);
      clone.visualProperties = new HashMap<String, Object>(this.visualProperties);
     // Create new listeners support
      clone.furnitureChangeSupport = new CollectionChangeSupport<HomePieceOfFurniture>(clone);
      clone.selectionListeners = new ArrayList<SelectionListener>();
      clone.wallsChangeSupport = new CollectionChangeSupport<Wall>(clone);
      clone.roomsChangeSupport = new CollectionChangeSupport<Room>(clone);
      clone.dimensionLinesChangeSupport = new CollectionChangeSupport<DimensionLine>(clone);
      clone.labelsChangeSupport = new CollectionChangeSupport<Label>(clone);
      clone.propertyChangeSupport = new PropertyChangeSupport(clone);
      return clone;
    } catch (CloneNotSupportedException ex) {
      throw new IllegalStateException("Super class isn't cloneable"); 
    }
  }
  
  /**
   * Returns the list of cloned items in <code>source</code>.
   * If a cloned item is selected its clone will be selected too (ie added to 
   * <code>destinationSelectedItems</code>).
   */
  @SuppressWarnings("unchecked")
  private <T extends Selectable> List<T> cloneSelectableItems(List<T> source,
                                                              List<Selectable> sourceSelectedItems,
                                                              List<Selectable> destinationSelectedItems) {
    List<T> destination = new ArrayList<T>(source.size());
    for (T item : source) {
      T clone = (T)item.clone();
      destination.add(clone);
      if (sourceSelectedItems.contains(item)) {
        destinationSelectedItems.add(clone);
      }
    }
    return destination;
  }
  
  /**
   * Returns a deep copy of home selectable <code>items</code>.
   */
  public static List<Selectable> duplicate(List<? extends Selectable> items) {
    List<Selectable> list = new ArrayList<Selectable>();
    for (Selectable item : items) {
      if (!(item instanceof Wall)         // Walls are copied further
          && !(item instanceof Camera)) { // Cameras can't be duplicated
        list.add(item.clone());
      }
    }
    // Add to list a clone of walls list with their walls at start and end point set
    list.addAll(Wall.clone(getWallsSubList(items)));
    return list;
  }

  /**
   * Returns a sub list of <code>items</code> that contains only home furniture.
   */
  public static List<HomePieceOfFurniture> getFurnitureSubList(List<? extends Selectable> items) {
    return getSubList(items, HomePieceOfFurniture.class);
  }

  /**
   * Returns a sub list of <code>items</code> that contains only walls.
   */
  public static List<Wall> getWallsSubList(List<? extends Selectable> items) {
    return getSubList(items, Wall.class);
  }

  /**
   * Returns a sub list of <code>items</code> that contains only rooms.
   */
  public static List<Room> getRoomsSubList(List<? extends Selectable> items) {
    return getSubList(items, Room.class);
  }
  
  /**
   * Returns a sub list of <code>items</code> that contains only dimension lines.
   */
  public static List<DimensionLine> getDimensionLinesSubList(List<? extends Selectable> items) {
    return getSubList(items, DimensionLine.class);
  }
  
  /**
   * Returns a sub list of <code>items</code> that contains only labels.
   */
  public static List<Label> getLabelsSubList(List<? extends Selectable> items) {
    return getSubList(items, Label.class);
  }
  
  /**
   * Returns a sub list of <code>items</code> that contains only instances of <code>subListClass</code>.
   * @since 2.2
   */
  @SuppressWarnings("unchecked")
  public static <T> List<T> getSubList(List<? extends Selectable> items, 
                                       Class<T> subListClass) {
    List<T> subList = new ArrayList<T>();
    for (Selectable item : items) {
      if (subListClass.isInstance(item)) {
        subList.add((T)item);
      }
    }
    return subList;
  }
}