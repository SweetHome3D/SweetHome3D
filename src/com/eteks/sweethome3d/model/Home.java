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
public class Home implements Serializable {
  private static final long serialVersionUID = 1L;

  /**
   * The current version of this home. Each time the field list is changed
   * in <code>Home</code> class or in one of the classes that it uses,
   * this number is increased.
   */
  public static final long CURRENT_VERSION = 1500;
  
  /**
   * The properties of a home that may change. <code>PropertyChangeListener</code>s added 
   * to a home will be notified under a property name equal to the string value of one these properties.
   */
  public enum Property {NAME, MODIFIED,
    FURNITURE_SORTED_PROPERTY, FURNITURE_DESCENDING_SORTED, FURNITURE_VISIBLE_PROPERTIES,    
    BACKGROUND_IMAGE, CAMERA, SKY_COLOR, GROUND_COLOR, GROUND_TEXTURE, LIGHT_COLOR, WALLS_ALPHA, PRINT};
  
  private List<HomePieceOfFurniture>                  furniture;
  private transient List<CollectionListener<HomePieceOfFurniture>> furnitureListeners;
  private transient List<Selectable>                  selectedItems;
  private transient List<SelectionListener>           selectionListeners;
  private List<Wall>                                  walls;
  private transient List<CollectionListener<Wall>>    wallListeners;
  private List<DimensionLine>                         dimensionLines;
  private transient List<CollectionListener<DimensionLine>> dimensionLineListeners;
  private Camera                                      camera;
  private String                                      name;
  private float                                       wallHeight;
  private transient boolean                           modified;
  private BackgroundImage                             backgroundImage;
  private ObserverCamera                              observerCamera;
  private Camera                                      topCamera;
  private int                                         skyColor;
  private int                                         groundColor;
  private HomeTexture                                 groundTexture;
  private int                                         lightColor;
  private float                                       wallsAlpha;
  private HomePrint                                   print;
  private String                                      furnitureSortedPropertyName;
  private List<String>                                furnitureVisiblePropertyNames;
  private boolean                                     furnitureDescendingSorted;
  // The two following fields aren't transient for backward compatibility reasons 
  private HomePieceOfFurniture.SortableProperty       furnitureSortedProperty;
  private List<HomePieceOfFurniture.SortableProperty> furnitureVisibleProperties;
  private Map<String, Object>                         visualProperties;
  private transient PropertyChangeSupport             propertyChangeSupport;
  private long                                        version;

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
  }

  private void init() {
    // Initialize transient lists
    this.selectedItems = new ArrayList<Selectable>();
    this.furnitureListeners = new ArrayList<CollectionListener<HomePieceOfFurniture>>();
    this.selectionListeners = new ArrayList<SelectionListener>();
    this.wallListeners = new ArrayList<CollectionListener<Wall>>();
    this.dimensionLineListeners = new ArrayList<CollectionListener<DimensionLine>>();
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
    this.visualProperties = new HashMap<String, Object>();
    
    this.version = CURRENT_VERSION;
  }

  /**
   * Sets the version of this home and writes it to <code>out</code> stream
   * with default writing method. 
   */
  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    this.version = CURRENT_VERSION;
        
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
    
    out.defaultWriteObject();
    
    // Restore current values
    this.furnitureSortedProperty = currentFurnitureSortedProperty;
    this.furnitureVisibleProperties = currentFurnitureVisibleProperties;
    // Set furnitureSortedPropertyName and furnitureVisiblePropertyNames to null
    // (they are used only for serialization)
    this.furnitureSortedPropertyName = null;
    this.furnitureVisiblePropertyNames = null;
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
   * Adds the furniture <code>listener</code> in parameter to this home.
   */
  public void addFurnitureListener(CollectionListener<HomePieceOfFurniture> listener) {
    this.furnitureListeners.add(listener);
  }

  /**
   * Removes the furniture <code>listener</code> in parameter from this home.
   */
  public void removeFurnitureListener(CollectionListener<HomePieceOfFurniture> listener) {
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
    firePieceOfFurnitureChanged(piece, index, CollectionEvent.Type.ADD);
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
      firePieceOfFurnitureChanged(piece, index, CollectionEvent.Type.DELETE);
    }
  }

  @SuppressWarnings("unchecked")
  private void firePieceOfFurnitureChanged(HomePieceOfFurniture piece, int index, 
                                           CollectionEvent.Type eventType) {
    if (!this.furnitureListeners.isEmpty()) {
      CollectionEvent<HomePieceOfFurniture> furnitureEvent = 
          new CollectionEvent<HomePieceOfFurniture>(this, piece, index, eventType);
      // Work on a copy of furnitureListeners to ensure a listener 
      // can modify safely listeners list
      CollectionListener<HomePieceOfFurniture> [] listeners = this.furnitureListeners.
        toArray(new CollectionListener [this.furnitureListeners.size()]);
      for (CollectionListener<HomePieceOfFurniture> listener : listeners) {
        listener.collectionChanged(furnitureEvent);
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
   * Deselects <code>item</code> if it's selected.
   */
  private void deselectItem(Selectable item) {
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
    this.wallListeners.add(listener);
  }
  
  /**
   * Removes the wall <code>listener</code> in parameter from this home.
   */
  public void removeWallsListener(CollectionListener<Wall> listener) {
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
    fireWallEvent(wall, CollectionEvent.Type.ADD);
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
        otherWall.setWallAtStart(null);
      } else if (wall.equals(otherWall.getWallAtEnd())) {
        otherWall.setWallAtEnd(null);
      }
    }
    // Make a copy of the list to avoid conflicts in the list returned by getWalls
    this.walls = new ArrayList<Wall>(this.walls);
    this.walls.remove(wall);
    fireWallEvent(wall, CollectionEvent.Type.DELETE);
  }

  /**
   * Notifies all wall listeners added to this home an event of 
   * a given type.
   */
  @SuppressWarnings("unchecked")
  private void fireWallEvent(Wall wall, CollectionEvent.Type eventType) {
    if (!this.wallListeners.isEmpty()) {
      CollectionEvent<Wall> wallEvent = new CollectionEvent<Wall>(this, wall, eventType);
      // Work on a copy of wallListeners to ensure a listener 
      // can modify safely listeners list
      CollectionListener<Wall> [] listeners = this.wallListeners.
        toArray(new CollectionListener [this.wallListeners.size()]);
      for (CollectionListener<Wall> listener : listeners) {
        listener.collectionChanged(wallEvent);
      }
    }
  }

  /**
   * Adds the dimension line <code>listener</code> in parameter to this home.
   */
  public void addDimensionLinesListener(CollectionListener<DimensionLine> listener) {
    this.dimensionLineListeners.add(listener);
  }
  
  /**
   * Removes the dimension line <code>listener</code> in parameter from this home.
   */
  public void removeDimensionLinesListener(CollectionListener<DimensionLine> listener) {
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
    fireDimensionLineEvent(dimensionLine, CollectionEvent.Type.ADD);
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
    fireDimensionLineEvent(dimensionLine, CollectionEvent.Type.DELETE);
  }

  /**
   * Notifies all dimension line listeners added to this home an event of 
   * a given type.
   */
  @SuppressWarnings("unchecked")
  private void fireDimensionLineEvent(DimensionLine dimensionLine, CollectionEvent.Type eventType) {
    if (!this.dimensionLineListeners.isEmpty()) {
      CollectionEvent<DimensionLine> dimensionLineEvent = 
          new CollectionEvent<DimensionLine>(this, dimensionLine, eventType);
      // Work on a copy of dimensionLineListeners to ensure a listener 
      // can modify safely listeners list
      CollectionListener<DimensionLine> [] listeners = this.dimensionLineListeners.
        toArray(new CollectionListener [this.dimensionLineListeners.size()]);
      for (CollectionListener<DimensionLine> listener : listeners) {
        listener.collectionChanged(dimensionLineEvent);
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
   * Returns the ground texture of this home.
   */
  public HomeTexture getGroundTexture() {
    return this.groundTexture;
  }

  /**
   * Sets the ground texture of this home and fires a <code>PropertyChangeEvent</code>.
   */
  public void setGroundTexture(HomeTexture groundTexture) {
    if (groundTexture != this.groundTexture) {
      HomeTexture oldGroundTexture = this.groundTexture;
      this.groundTexture = groundTexture;
      this.propertyChangeSupport.firePropertyChange(
          Property.GROUND_TEXTURE.toString(), oldGroundTexture, groundTexture);
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
   * Returns a sub list of <code>items</code> that contains only dimension lines.
   */
  public static List<DimensionLine> getDimensionLinesSubList(List<? extends Selectable> items) {
    return getSubList(items, DimensionLine.class);
  }
  
  @SuppressWarnings("unchecked")
  private static <T> List<T> getSubList(List<? extends Selectable> items, 
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

