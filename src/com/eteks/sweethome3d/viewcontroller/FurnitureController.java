/*
 * FurnitureController.java 15 mai 2006
 *
 * Sweet Home 3D, Copyright (c) 2006 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import java.awt.geom.Line2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.CollectionEvent;
import com.eteks.sweethome3d.model.CollectionListener;
import com.eteks.sweethome3d.model.DoorOrWindow;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeDoorOrWindow;
import com.eteks.sweethome3d.model.HomeFurnitureGroup;
import com.eteks.sweethome3d.model.HomeLight;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.HomePieceOfFurniture.SortableProperty;
import com.eteks.sweethome3d.model.Level;
import com.eteks.sweethome3d.model.Light;
import com.eteks.sweethome3d.model.PieceOfFurniture;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.SelectionEvent;
import com.eteks.sweethome3d.model.SelectionListener;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * A MVC controller for the home furniture table.
 * @author Emmanuel Puybaret
 */
public class FurnitureController implements Controller {
  private final Home                home;
  private final UserPreferences     preferences;
  private final ViewFactory         viewFactory;
  private final ContentManager      contentManager;
  private final UndoableEditSupport undoSupport;
  private View                      furnitureView;
  private HomePieceOfFurniture      leadSelectedPieceOfFurniture;

  /**
   * Creates the controller of home furniture view.
   * @param home the home edited by this controller and its view
   * @param preferences the preferences of the application
   * @param viewFactory a factory able to create the furniture view managed by this controller
   */
  public FurnitureController(Home home, 
                             UserPreferences preferences,
                             ViewFactory viewFactory) {
    this(home, preferences, viewFactory, null, null); 
  }

  /**
   * Creates the controller of home furniture view with undo support.
   */
  public FurnitureController(final Home home, 
                             UserPreferences preferences,
                             ViewFactory viewFactory,
                             ContentManager contentManager,
                             UndoableEditSupport undoSupport) {
    this.home = home;
    this.preferences = preferences;
    this.viewFactory = viewFactory;
    this.undoSupport = undoSupport;
    this.contentManager = contentManager;    
    
    addModelListeners();
  }

  /**
   * Returns the view associated with this controller.
   */
  public View getView() {
    // Create view lazily only once it's needed
    if (this.furnitureView == null) {
      this.furnitureView = this.viewFactory.createFurnitureView(this.home, this.preferences, this);
    }
    return this.furnitureView;
  }
  
  private void addModelListeners() {
    // Add a selection listener that gets the lead selected piece in home
    this.home.addSelectionListener(new SelectionListener() {
        public void selectionChanged(SelectionEvent ev) {
          List<HomePieceOfFurniture> selectedFurniture = 
              Home.getFurnitureSubList(home.getSelectedItems());
          if (selectedFurniture.isEmpty()) {
            leadSelectedPieceOfFurniture = null;
          } else if (leadSelectedPieceOfFurniture == null 
                     || selectedFurniture.size() == 1
                     || selectedFurniture.indexOf(leadSelectedPieceOfFurniture) == -1) {
            leadSelectedPieceOfFurniture = selectedFurniture.get(0);
          }
        }
      });
    
    // Add listener to update base plan lock when furniture movability changes
    final PropertyChangeListener furnitureChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          if (HomePieceOfFurniture.Property.MOVABLE.name().equals(ev.getPropertyName())) {
            // Remove non movable pieces from selection when base plan is locked 
            HomePieceOfFurniture piece = (HomePieceOfFurniture)ev.getSource();
            if (home.isBasePlanLocked()
                && isPieceOfFurniturePartOfBasePlan(piece)) {
              List<Selectable> selectedItems = home.getSelectedItems();
              if (selectedItems.contains(piece)) {
                selectedItems = new ArrayList<Selectable>(selectedItems);
                selectedItems.remove(piece);
                home.setSelectedItems(selectedItems);
              }
            }
          }
        }
      };
    for (HomePieceOfFurniture piece : home.getFurniture()) {
      piece.addPropertyChangeListener(furnitureChangeListener);
    }
    this.home.addFurnitureListener(new CollectionListener<HomePieceOfFurniture> () {
        public void collectionChanged(CollectionEvent<HomePieceOfFurniture> ev) {
          if (ev.getType() == CollectionEvent.Type.ADD) {
            ev.getItem().addPropertyChangeListener(furnitureChangeListener);
          } else if (ev.getType() == CollectionEvent.Type.DELETE) {
            ev.getItem().removePropertyChangeListener(furnitureChangeListener);
          }
        }
      });
  }
  
  /**
   * Controls new furniture added to home. 
   * Once added the furniture will be selected in view 
   * and undo support will receive a new undoable edit.
   * @param furniture the furniture to add.
   */
  public void addFurniture(List<HomePieceOfFurniture> furniture) {
    addFurniture(furniture, null);
  }
  
  /**
   * Controls new furniture added to the given group. 
   * Once added the furniture will be selected in view 
   * and undo support will receive a new undoable edit.
   * @param furniture the furniture to add.
   * @param group     the group to which furniture will be added.
   */
  public void addFurnitureToGroup(List<HomePieceOfFurniture> furniture, HomeFurnitureGroup group) {
    if (group == null) {
      throw new IllegalArgumentException("Group shouldn't be null");
    }
    addFurniture(furniture, group);
  }
    
  private void addFurniture(List<HomePieceOfFurniture> furniture, HomeFurnitureGroup group) {
    final boolean oldBasePlanLocked = this.home.isBasePlanLocked();
    final boolean allLevelsSelection = this.home.isAllLevelsSelection();
    final List<Selectable> oldSelection = this.home.getSelectedItems(); 
    final HomePieceOfFurniture [] newFurniture = 
        furniture.toArray(new HomePieceOfFurniture [furniture.size()]);
    // Get indices of added furniture 
    final int [] furnitureIndex = new int [furniture.size()];
    final HomeFurnitureGroup [] furnitureGroups = group != null
        ? new HomeFurnitureGroup [furniture.size()]
        : null;
    int endIndex = group == null 
        ? this.home.getFurniture().size()
        : group.getFurniture().size();
    boolean basePlanLocked = oldBasePlanLocked;
    for (int i = 0; i < furnitureIndex.length; i++) {
      furnitureIndex [i] = endIndex++; 
      // Unlock base plan if the piece is a part of it
      basePlanLocked &= !isPieceOfFurniturePartOfBasePlan(newFurniture [i]);
      if (furnitureGroups != null) {
        furnitureGroups [i] = group;
      }
    }  
    final boolean newBasePlanLocked = basePlanLocked;
    final Level furnitureLevel = this.home.getSelectedLevel();
    
    doAddFurniture(newFurniture, furnitureGroups, furnitureIndex, furnitureLevel, null, newBasePlanLocked, false); 
    if (this.undoSupport != null) {
      UndoableEdit undoableEdit = new AbstractUndoableEdit() {
        @Override
        public void undo() throws CannotUndoException {
          super.undo();
          doDeleteFurniture(newFurniture, oldBasePlanLocked, allLevelsSelection); 
          home.setSelectedItems(oldSelection); 
        }
        
        @Override
        public void redo() throws CannotRedoException {
          super.redo();
          doAddFurniture(newFurniture, furnitureGroups, furnitureIndex, furnitureLevel, null, newBasePlanLocked, false); 
        }
        
        @Override
        public String getPresentationName() {
          return preferences.getLocalizedString(FurnitureController.class, "undoAddFurnitureName");
        }
      };
      this.undoSupport.postEdit(undoableEdit);
    }
  }
  
  private void doAddFurniture(HomePieceOfFurniture [] furniture,
                              HomeFurnitureGroup [] furnitureGroups,
                              int [] furnitureIndex, 
                              Level furnitureLevel, 
                              Level [] furnitureLevels,
                              boolean basePlanLocked,
                              boolean allLevelsSelection) {
    for (int i = 0; i < furnitureIndex.length; i++) {
      if (furnitureGroups != null && furnitureGroups [i] != null) {
        this.home.addPieceOfFurnitureToGroup(furniture [i], furnitureGroups [i], furnitureIndex [i]);
        furniture [i].setVisible(furnitureGroups [i].isVisible());
      } else {
        this.home.addPieceOfFurniture(furniture [i], furnitureIndex [i]);
      }
      furniture [i].setLevel(furnitureLevels != null ? furnitureLevels [i] : furnitureLevel);
    }
    this.home.setBasePlanLocked(basePlanLocked);
    this.home.setSelectedItems(Arrays.asList(furniture)); 
    this.home.setAllLevelsSelection(allLevelsSelection);
  }
  
  /**
   * Controls the deletion of the current selected furniture in home.
   * Once the selected furniture is deleted, undo support will receive a new undoable edit.
   */
  public void deleteSelection() {
    deleteFurniture(Home.getFurnitureSubList(this.home.getSelectedItems()));    
  }
  
  /**
   * Deletes the furniture of <code>deletedFurniture</code> from home.
   * Once the selected furniture is deleted, undo support will receive a new undoable edit.
   */
  public void deleteFurniture(List<HomePieceOfFurniture> deletedFurniture) {
    final boolean basePlanLocked = this.home.isBasePlanLocked();
    final boolean allLevelsSelection = this.home.isAllLevelsSelection();
    final List<Selectable> oldSelection = this.home.getSelectedItems(); 
    List<HomePieceOfFurniture> homeFurniture = this.home.getFurniture(); 

    // Replace pieces by their group when they have to be all deleted
    deletedFurniture = new ArrayList<HomePieceOfFurniture>(deletedFurniture);
    List<HomeFurnitureGroup> homeGroups = new ArrayList<HomeFurnitureGroup>();  
    searchGroups(homeFurniture, homeGroups);
    boolean updated;
    do {
      updated = false;
      for (HomeFurnitureGroup group : homeGroups) {
        List<HomePieceOfFurniture> groupFurniture = group.getFurniture();
        if (deletedFurniture.containsAll(groupFurniture)) {
          deletedFurniture.removeAll(groupFurniture);
          deletedFurniture.add(group);
          updated = true;
        }
      }
    } while (updated);
      
    // Sort the deletable furniture in the ascending order of their index in home or their group
    Map<HomeFurnitureGroup, Map<Integer, HomePieceOfFurniture>> deletedFurnitureMap =
        new HashMap<HomeFurnitureGroup, Map<Integer, HomePieceOfFurniture>>();
    int deletedFurnitureCount = 0;
    for (HomePieceOfFurniture piece : deletedFurniture) {
      // Check piece is deletable and doesn't belong to a group
      if (isPieceOfFurnitureDeletable(piece)) {
        HomeFurnitureGroup group = getPieceOfFurnitureGroup(piece, null, homeFurniture);
        Map<Integer, HomePieceOfFurniture> sortedMap = deletedFurnitureMap.get(group);
        if (sortedMap == null) {
          sortedMap = new TreeMap<Integer, HomePieceOfFurniture>();
          deletedFurnitureMap.put(group, sortedMap);
        }
        if (group == null) {
          sortedMap.put(homeFurniture.indexOf(piece), piece);
        } else {
          sortedMap.put(group.getFurniture().indexOf(piece), piece);
        }
        deletedFurnitureCount++;
      }
    }
    final HomePieceOfFurniture [] furniture = new HomePieceOfFurniture [deletedFurnitureCount]; 
    final int [] furnitureIndex = new int [furniture.length];
    final Level [] furnitureLevels = new Level [furniture.length];
    final HomeFurnitureGroup [] furnitureGroups = new HomeFurnitureGroup [furniture.length];
    int i = 0;
    for (Map.Entry<HomeFurnitureGroup, Map<Integer, HomePieceOfFurniture>> sortedMapEntry : deletedFurnitureMap.entrySet()) {
      for (Map.Entry<Integer, HomePieceOfFurniture> pieceEntry : sortedMapEntry.getValue().entrySet()) {
        furniture [i] = pieceEntry.getValue();
        furnitureIndex [i] = pieceEntry.getKey(); 
        furnitureLevels [i] = furniture [i].getLevel();
        furnitureGroups [i++] = sortedMapEntry.getKey();
      }
    } 
    doDeleteFurniture(furniture, basePlanLocked, false); 
    if (this.undoSupport != null) {
      UndoableEdit undoableEdit = new AbstractUndoableEdit() {
        @Override
        public void undo() throws CannotUndoException {
          super.undo();
          doAddFurniture(furniture, furnitureGroups, furnitureIndex, null, furnitureLevels, basePlanLocked, allLevelsSelection); 
          home.setSelectedItems(oldSelection);
        }
        
        @Override
        public void redo() throws CannotRedoException {
          super.redo();
          home.setSelectedItems(Arrays.asList(furniture));
          doDeleteFurniture(furniture, basePlanLocked, false); 
        }
        
        @Override
        public String getPresentationName() {
          return preferences.getLocalizedString(FurnitureController.class, "undoDeleteSelectionName");
        }
      };
      this.undoSupport.postEdit(undoableEdit);
    }
  }
  
  private void doDeleteFurniture(HomePieceOfFurniture [] furniture, 
                                 boolean basePlanLocked,
                                 boolean allLevelsSelection) { 
    for (HomePieceOfFurniture piece : furniture) {
      this.home.deletePieceOfFurniture(piece);
    }
    this.home.setBasePlanLocked(basePlanLocked);
    this.home.setAllLevelsSelection(allLevelsSelection);
  }

  /**
   * Searches all the groups among furniture and its children.
   */
  private void searchGroups(List<HomePieceOfFurniture> furniture,
                            List<HomeFurnitureGroup> groups) {
    for (HomePieceOfFurniture piece : furniture) {
      if (piece instanceof HomeFurnitureGroup) {
        groups.add((HomeFurnitureGroup)piece);
        searchGroups(((HomeFurnitureGroup)piece).getFurniture(), groups);
      }
    }
  }

  /**
   * Returns the furniture group that contains the given <code>piece</code> or <code>null</code> if it can't be found.
   */
  private HomeFurnitureGroup getPieceOfFurnitureGroup(HomePieceOfFurniture piece, 
                                                      HomeFurnitureGroup furnitureGroup, 
                                                      List<HomePieceOfFurniture> furniture) {
    for (HomePieceOfFurniture homePiece : furniture) {
      if (homePiece.equals(piece)) {
        return furnitureGroup;
      } else if (homePiece instanceof HomeFurnitureGroup) {
        HomeFurnitureGroup group = getPieceOfFurnitureGroup(piece, 
            (HomeFurnitureGroup)homePiece, ((HomeFurnitureGroup)homePiece).getFurniture());
        if (group != null) {
          return group;
        }
      }
    }
    return null;
  }
  
  /**
   * Updates the selected furniture in home.
   */
  public void setSelectedFurniture(List<HomePieceOfFurniture> selectedFurniture) {
    if (this.home.isBasePlanLocked()) {
      selectedFurniture = getFurnitureNotPartOfBasePlan(selectedFurniture);
    }
    this.home.setSelectedItems(selectedFurniture);
    this.home.setAllLevelsSelection(false);
  }

  /**
   * Selects all furniture in home.
   */
  public void selectAll() {
    setSelectedFurniture(this.home.getFurniture());
  }
  
  /**
   * Returns <code>true</code> if the given <code>piece</code> isn't movable.
   */
  protected boolean isPieceOfFurniturePartOfBasePlan(HomePieceOfFurniture piece) {
    return !piece.isMovable() || piece.isDoorOrWindow();
  }

  /**
   * Returns <code>true</code> if the given <code>piece</code> may be moved.
   * Default implementation always returns <code>true</code>. 
   */
  protected boolean isPieceOfFurnitureMovable(HomePieceOfFurniture piece) {
    return true;
  }

  /**
   * Returns <code>true</code> if the given <code>piece</code> may be deleted.
   * Default implementation always returns <code>true</code>. 
   */
  protected boolean isPieceOfFurnitureDeletable(HomePieceOfFurniture piece) {
    return true;
  }

  /**
   * Returns a new home piece of furniture created from an other given <code>piece</code> of furniture.
   */
  public HomePieceOfFurniture createHomePieceOfFurniture(PieceOfFurniture piece) {
    if (piece instanceof DoorOrWindow) {
      return new HomeDoorOrWindow((DoorOrWindow)piece);
    } else if (piece instanceof Light) {
      return new HomeLight((Light)piece);
    } else {
      return new HomePieceOfFurniture(piece);
    }
  }

  /**
   * Returns the furniture among the given list that are not part of the base plan.
   */
  private List<HomePieceOfFurniture> getFurnitureNotPartOfBasePlan(List<HomePieceOfFurniture> furniture) {
    List<HomePieceOfFurniture> furnitureNotPartOfBasePlan = new ArrayList<HomePieceOfFurniture>();
    for (HomePieceOfFurniture piece : furniture) {
      if (!isPieceOfFurniturePartOfBasePlan(piece)) {        
        furnitureNotPartOfBasePlan.add(piece);
      }
    }
    return furnitureNotPartOfBasePlan;
  }

  /**
   * Uses <code>furnitureProperty</code> to sort home furniture 
   * or cancels home furniture sort if home is already sorted on <code>furnitureProperty</code>
   * @param furnitureProperty a property of {@link HomePieceOfFurniture HomePieceOfFurniture} class.
   */
  public void toggleFurnitureSort(HomePieceOfFurniture.SortableProperty furnitureProperty) {
    if (furnitureProperty.equals(this.home.getFurnitureSortedProperty())) {
      this.home.setFurnitureSortedProperty(null);
    } else {
      this.home.setFurnitureSortedProperty(furnitureProperty);      
    }
  }

  /**
   * Toggles home furniture sort order.
   */
  public void toggleFurnitureSortOrder() {
    this.home.setFurnitureDescendingSorted(!this.home.isFurnitureDescendingSorted());
  }

  /**
   * Controls the sort of the furniture in home. If home furniture isn't sorted
   * or is sorted on an other property, it will be sorted on the given
   * <code>furnitureProperty</code> in ascending order. If home furniture is already
   * sorted on the given <code>furnitureProperty</code>, it will be sorted in descending 
   * order, if the sort is in ascending order, otherwise it won't be sorted at all 
   * and home furniture will be listed in insertion order. 
    * @param furnitureProperty  the furniture property on which the view wants
   *          to sort the furniture it displays.
   */
  public void sortFurniture(HomePieceOfFurniture.SortableProperty furnitureProperty) {
    // Compute sort algorithm described in javadoc
    final HomePieceOfFurniture.SortableProperty  oldProperty = 
        this.home.getFurnitureSortedProperty();
    final boolean oldDescending = this.home.isFurnitureDescendingSorted(); 
    boolean descending = false;
    if (furnitureProperty.equals(oldProperty)) {
      if (oldDescending) {
        furnitureProperty = null;
      } else {
        descending = true;
      }
    }
    this.home.setFurnitureSortedProperty(furnitureProperty);
    this.home.setFurnitureDescendingSorted(descending);
  }

  /**
   * Updates the furniture visible properties in home.  
   */
  public void setFurnitureVisibleProperties(List<HomePieceOfFurniture.SortableProperty> furnitureVisibleProperties) {
    this.home.setFurnitureVisibleProperties(furnitureVisibleProperties);
  }
  
  /**
   * Toggles furniture property visibility in home. 
   */
  public void toggleFurnitureVisibleProperty(HomePieceOfFurniture.SortableProperty furnitureProperty) {
    List<SortableProperty> furnitureVisibleProperties = 
        new ArrayList<SortableProperty>(this.home.getFurnitureVisibleProperties());
    if (furnitureVisibleProperties.contains(furnitureProperty)) {
      furnitureVisibleProperties.remove(furnitureProperty);
      // Ensure at least one column is visible
      if (furnitureVisibleProperties.isEmpty()) {
        furnitureVisibleProperties.add(HomePieceOfFurniture.SortableProperty.NAME);
      }
    } else {
      // Add furniture property after the visible property that has the previous index in 
      // the following list
      List<HomePieceOfFurniture.SortableProperty> propertiesOrder = 
          Arrays.asList(new HomePieceOfFurniture.SortableProperty [] {
              HomePieceOfFurniture.SortableProperty.CATALOG_ID, 
              HomePieceOfFurniture.SortableProperty.NAME, 
              HomePieceOfFurniture.SortableProperty.CREATOR, 
              HomePieceOfFurniture.SortableProperty.WIDTH,
              HomePieceOfFurniture.SortableProperty.DEPTH,
              HomePieceOfFurniture.SortableProperty.HEIGHT,
              HomePieceOfFurniture.SortableProperty.X,
              HomePieceOfFurniture.SortableProperty.Y,
              HomePieceOfFurniture.SortableProperty.ELEVATION,
              HomePieceOfFurniture.SortableProperty.ANGLE,
              HomePieceOfFurniture.SortableProperty.LEVEL,
              HomePieceOfFurniture.SortableProperty.MODEL_SIZE,
              HomePieceOfFurniture.SortableProperty.COLOR,
              HomePieceOfFurniture.SortableProperty.TEXTURE,
              HomePieceOfFurniture.SortableProperty.MOVABLE,
              HomePieceOfFurniture.SortableProperty.DOOR_OR_WINDOW,
              HomePieceOfFurniture.SortableProperty.VISIBLE,
              HomePieceOfFurniture.SortableProperty.PRICE,
              HomePieceOfFurniture.SortableProperty.VALUE_ADDED_TAX_PERCENTAGE,
              HomePieceOfFurniture.SortableProperty.VALUE_ADDED_TAX,
              HomePieceOfFurniture.SortableProperty.PRICE_VALUE_ADDED_TAX_INCLUDED}); 
      int propertyIndex = propertiesOrder.indexOf(furnitureProperty) - 1;
      if (propertyIndex > 0) {      
        while (propertyIndex > 0) {
          int visiblePropertyIndex = furnitureVisibleProperties.indexOf(propertiesOrder.get(propertyIndex));
          if (visiblePropertyIndex >= 0) {
            propertyIndex = visiblePropertyIndex + 1;
            break;
          } else {
            propertyIndex--;
          }
        }
      }
      if (propertyIndex < 0) {
        propertyIndex = 0;
      }
      furnitureVisibleProperties.add(propertyIndex, furnitureProperty);
    }
    this.home.setFurnitureVisibleProperties(furnitureVisibleProperties);
  }
  
  /**
   * Controls the modification of selected furniture.
   */
  public void modifySelectedFurniture() {
    if (!Home.getFurnitureSubList(this.home.getSelectedItems()).isEmpty()) {
      new HomeFurnitureController(this.home, this.preferences,  
          this.viewFactory, this.contentManager, this.undoSupport).displayView(getView());
    }
  }

  /**
   * Controls the modification of the visibility of the selected piece of furniture.
   */
  public void toggleSelectedFurnitureVisibility() {
    if (Home.getFurnitureSubList(this.home.getSelectedItems()).size() == 1) {
      HomeFurnitureController controller = new HomeFurnitureController(this.home, this.preferences,  
          this.viewFactory, this.contentManager, this.undoSupport);
      controller.setVisible(!controller.getVisible());
      controller.modifyFurniture();
    }
  }

  /**
   * Groups the selected furniture as one piece of furniture.
   */
  public void groupSelectedFurniture() {
    List<HomePieceOfFurniture> selectedFurniture = getMovableSelectedFurniture();
    if (!selectedFurniture.isEmpty()) {
      final boolean basePlanLocked = this.home.isBasePlanLocked();
      final boolean allLevelsSelection = this.home.isAllLevelsSelection();
      final List<Selectable> oldSelection = this.home.getSelectedItems();
      List<HomePieceOfFurniture> homeFurniture = this.home.getFurniture();
      // Sort the grouped furniture in the ascending order of their index in home or their group
      Map<HomeFurnitureGroup, TreeMap<Integer, HomePieceOfFurniture>> groupedFurnitureMap =
          new HashMap<HomeFurnitureGroup, TreeMap<Integer, HomePieceOfFurniture>>();
      int groupedFurnitureCount = 0;
      for (HomePieceOfFurniture piece : selectedFurniture) {
        HomeFurnitureGroup group = getPieceOfFurnitureGroup(piece, null, homeFurniture);
        TreeMap<Integer, HomePieceOfFurniture> sortedMap = groupedFurnitureMap.get(group);
        if (sortedMap == null) {
          sortedMap = new TreeMap<Integer, HomePieceOfFurniture>();
          groupedFurnitureMap.put(group, sortedMap);
        }
        if (group == null) {
          sortedMap.put(homeFurniture.indexOf(piece), piece);
        } else {
          sortedMap.put(group.getFurniture().indexOf(piece), piece);
        }
        groupedFurnitureCount++;
      }
      final HomePieceOfFurniture [] groupedPieces = new HomePieceOfFurniture [groupedFurnitureCount]; 
      final int [] groupedPiecesIndex = new int [groupedPieces.length];
      final Level [] groupedPiecesLevel = new Level [groupedPieces.length];
      final float [] groupPiecesElevation = new float [groupedPieces.length];
      final boolean [] groupPiecesVisible = new boolean [groupedPieces.length];
      final HomeFurnitureGroup [] groupedPiecesGroups = new HomeFurnitureGroup [groupedPieces.length];
      Level minLevel = this.home.getSelectedLevel();
      int i = 0;
      for (Map.Entry<HomeFurnitureGroup, TreeMap<Integer, HomePieceOfFurniture>> sortedMapEntry : groupedFurnitureMap.entrySet()) {
        for (Map.Entry<Integer, HomePieceOfFurniture> pieceEntry : sortedMapEntry.getValue().entrySet()) {
          HomePieceOfFurniture piece = pieceEntry.getValue();
          groupedPieces [i] = piece;
          groupedPiecesIndex [i] = pieceEntry.getKey();
          groupedPiecesLevel [i] = piece.getLevel();
          groupPiecesElevation [i] = piece.getElevation();
          groupPiecesVisible [i] = piece.isVisible();
          groupedPiecesGroups [i] = sortedMapEntry.getKey();
          if (groupedPiecesLevel [i] != null) {
            if (minLevel == null
                || groupedPiecesLevel [i].getElevation() < minLevel.getElevation()) {
              minLevel = groupedPiecesLevel [i];
            }
          }
          i++;
        }
      } 
      final HomeFurnitureGroup group;
      if (selectedFurniture.indexOf(this.leadSelectedPieceOfFurniture) > 0) {
        group = createHomeFurnitureGroup(Arrays.asList(groupedPieces), this.leadSelectedPieceOfFurniture);
      } else {
        group = createHomeFurnitureGroup(Arrays.asList(groupedPieces));
      }
      // Store piece elevation that could have been updated during grouping
      final float [] groupPiecesNewElevation = new float [groupedPieces.length];
      i = 0;
      for (HomePieceOfFurniture piece : groupedPieces) {
        groupPiecesNewElevation [i++] = piece.getElevation();
      }
      TreeMap<Integer, HomePieceOfFurniture> homeSortedMap = groupedFurnitureMap.get(null);
      final int groupIndex = homeSortedMap != null 
          ? homeSortedMap.lastKey() + 1 - groupedPieces.length
          : homeFurniture.size();
      final boolean movable = group.isMovable();
      final Level groupLevel = minLevel;
      
      doGroupFurniture(groupedPieces, new HomeFurnitureGroup [] {group}, 
          null, new int [] {groupIndex}, new Level [] {groupLevel}, basePlanLocked, false);
      if (this.undoSupport != null) {
        UndoableEdit undoableEdit = new AbstractUndoableEdit() {
            @Override
            public void undo() throws CannotUndoException {
              super.undo();
              doUngroupFurniture(new HomeFurnitureGroup [] {group}, groupedPieces, 
                  groupedPiecesGroups, groupedPiecesIndex, groupedPiecesLevel, basePlanLocked, allLevelsSelection);
              for (int i = 0; i < groupedPieces.length; i++) {
                groupedPieces [i].setElevation(groupPiecesElevation [i]);
                groupedPieces [i].setVisible(groupPiecesVisible [i]);
              }
              home.setSelectedItems(oldSelection);
            }
            
            @Override
            public void redo() throws CannotRedoException {
              super.redo();
              for (int i = 0; i < groupedPieces.length; i++) {
                groupedPieces [i].setElevation(groupPiecesNewElevation [i]);
                groupedPieces [i].setLevel(null);
              }
              group.setMovable(movable);
              group.setVisible(true);
              doGroupFurniture(groupedPieces, new HomeFurnitureGroup [] {group}, 
                  null, new int [] {groupIndex}, new Level [] {groupLevel}, basePlanLocked, false);
            }
            
            @Override
            public String getPresentationName() {
              return preferences.getLocalizedString(FurnitureController.class, "undoGroupName");
            }
          };
        this.undoSupport.postEdit(undoableEdit);
      }
    }
  }

  /**
   * Returns a new furniture group for the given furniture list.
   */
  protected HomeFurnitureGroup createHomeFurnitureGroup(List<HomePieceOfFurniture> furniture) {
    return createHomeFurnitureGroup(furniture, furniture.get(0));
  }

  /**
   * Returns a new furniture group for the given furniture list.
   * @since 4.5
   */
  protected HomeFurnitureGroup createHomeFurnitureGroup(List<HomePieceOfFurniture> furniture, HomePieceOfFurniture leadingPiece) {
    String furnitureGroupName = this.preferences.getLocalizedString(
        FurnitureController.class, "groupName", getFurnitureGroupCount(this.home.getFurniture()) + 1);
    final HomeFurnitureGroup furnitureGroup = new HomeFurnitureGroup(furniture, leadingPiece, furnitureGroupName);
    return furnitureGroup;
  }

  /**
   * Returns the count of furniture groups among the given list.
   */
  private int getFurnitureGroupCount(List<HomePieceOfFurniture> furniture) {
    int i = 0;
    for (HomePieceOfFurniture piece : furniture) {
      if (piece instanceof HomeFurnitureGroup) {
        i += 1 + getFurnitureGroupCount(((HomeFurnitureGroup)piece).getFurniture());
      }
    }
    return i;
  }

  private void doGroupFurniture(HomePieceOfFurniture [] groupedPieces,
                                HomeFurnitureGroup [] groups,
                                HomeFurnitureGroup [] groupsGroups,
                                int [] groupsIndex, 
                                Level [] groupsLevels,
                                boolean basePlanLocked,
                                boolean allLevelsSelection) {
    doDeleteFurniture(groupedPieces, basePlanLocked, allLevelsSelection);
    doAddFurniture(groups, groupsGroups, groupsIndex, null, groupsLevels, basePlanLocked, allLevelsSelection);
  }

  private void doUngroupFurniture(HomeFurnitureGroup [] groups,
                                  HomePieceOfFurniture [] ungroupedPieces,
                                  HomeFurnitureGroup [] ungroupedPiecesGroups,
                                  int [] ungroupedPiecesIndex, 
                                  Level [] ungroupedPiecesLevels,
                                  boolean basePlanLocked,
                                  boolean allLevelsSelection) {
    doDeleteFurniture(groups, basePlanLocked, allLevelsSelection);
    doAddFurniture(ungroupedPieces, ungroupedPiecesGroups, ungroupedPiecesIndex, null, ungroupedPiecesLevels, basePlanLocked, allLevelsSelection);
  }

  /**
   * Ungroups the selected groups of furniture.
   */
  public void ungroupSelectedFurniture() {
    List<HomeFurnitureGroup> movableSelectedFurnitureGroups = new ArrayList<HomeFurnitureGroup>(); 
    for (Selectable item : this.home.getSelectedItems()) {
      if (item instanceof HomeFurnitureGroup) {
        HomeFurnitureGroup group = (HomeFurnitureGroup)item;
        if (isPieceOfFurnitureMovable(group)) {
          movableSelectedFurnitureGroups.add(group);
        }
      }
    }  
    if (!movableSelectedFurnitureGroups.isEmpty()) {
      List<HomePieceOfFurniture> homeFurniture = this.home.getFurniture();
      final boolean oldBasePlanLocked = this.home.isBasePlanLocked();
      final boolean allLevelsSelection = this.home.isAllLevelsSelection();
      final List<Selectable> oldSelection = this.home.getSelectedItems();
      // Sort the groups in the ascending order of their index in home or their group
      Map<HomeFurnitureGroup, TreeMap<Integer, HomeFurnitureGroup>> groupsMap =
          new HashMap<HomeFurnitureGroup, TreeMap<Integer, HomeFurnitureGroup>>();
      int groupsCount = 0;
      for (HomeFurnitureGroup piece : movableSelectedFurnitureGroups) {
        HomeFurnitureGroup groupGroup = getPieceOfFurnitureGroup(piece, null, homeFurniture);
        TreeMap<Integer, HomeFurnitureGroup> sortedMap = groupsMap.get(groupGroup);
        if (sortedMap == null) {
          sortedMap = new TreeMap<Integer, HomeFurnitureGroup>();
          groupsMap.put(groupGroup, sortedMap);
        }
        if (groupGroup == null) {
          sortedMap.put(homeFurniture.indexOf(piece), piece);
        } else {
          sortedMap.put(groupGroup.getFurniture().indexOf(piece), piece);
        }
        groupsCount++;
      }
      final HomeFurnitureGroup [] groups = new HomeFurnitureGroup [groupsCount]; 
      final HomeFurnitureGroup [] groupsGroups = new HomeFurnitureGroup [groups.length];
      final int [] groupsIndex = new int [groups.length];
      final Level [] groupsLevels = new Level [groups.length];
      int i = 0;
      List<HomePieceOfFurniture> ungroupedPiecesList = new ArrayList<HomePieceOfFurniture>();
      List<Integer> ungroupedPiecesIndexList = new ArrayList<Integer>();
      List<HomeFurnitureGroup> ungroupedPiecesGroupsList = new ArrayList<HomeFurnitureGroup>();
      for (Map.Entry<HomeFurnitureGroup, TreeMap<Integer, HomeFurnitureGroup>> sortedMapEntry : groupsMap.entrySet()) {
        TreeMap<Integer, HomeFurnitureGroup> sortedMap = sortedMapEntry.getValue();
        int endIndex = sortedMap.lastKey() + 1 - sortedMap.size();
        for (Map.Entry<Integer, HomeFurnitureGroup> groupEntry : sortedMap.entrySet()) {
          HomeFurnitureGroup group = groupEntry.getValue();
          groups [i] = group;
          groupsGroups [i] = sortedMapEntry.getKey();
          groupsIndex [i] = groupEntry.getKey(); 
          groupsLevels [i++] = group.getLevel();
          for (HomePieceOfFurniture groupPiece : group.getFurniture()) {
            ungroupedPiecesList.add(groupPiece);
            ungroupedPiecesGroupsList.add(sortedMapEntry.getKey());
            ungroupedPiecesIndexList.add(endIndex++);
          }
        }
      } 
      final HomePieceOfFurniture [] ungroupedPieces = 
          ungroupedPiecesList.toArray(new HomePieceOfFurniture [ungroupedPiecesList.size()]);      
      final HomeFurnitureGroup [] ungroupedPiecesGroups = 
          ungroupedPiecesGroupsList.toArray(new HomeFurnitureGroup [ungroupedPiecesGroupsList.size()]);      
      final int [] ungroupedPiecesIndex = new int [ungroupedPieces.length];
      final Level [] ungroupedPiecesLevels = new Level [ungroupedPieces.length];
      boolean basePlanLocked = oldBasePlanLocked;
      for (i = 0; i < ungroupedPieces.length; i++) {
        ungroupedPiecesIndex [i] = ungroupedPiecesIndexList.get(i); 
        ungroupedPiecesLevels [i] = ungroupedPieces [i].getLevel();
        // Unlock base plan if the piece is a part of it
        basePlanLocked &= !isPieceOfFurniturePartOfBasePlan(ungroupedPieces [i]);
      }  
      final boolean newBasePlanLocked = basePlanLocked;

      doUngroupFurniture(groups, ungroupedPieces, ungroupedPiecesGroups, ungroupedPiecesIndex, ungroupedPiecesLevels, newBasePlanLocked, false);
      if (this.undoSupport != null) {
        UndoableEdit undoableEdit = new AbstractUndoableEdit() {
            @Override
            public void undo() throws CannotUndoException {
              super.undo();
              doGroupFurniture(ungroupedPieces, groups, groupsGroups, groupsIndex, groupsLevels, oldBasePlanLocked, allLevelsSelection);
              home.setSelectedItems(oldSelection);
            }
            
            @Override
            public void redo() throws CannotRedoException {
              super.redo();
              doUngroupFurniture(groups, ungroupedPieces, ungroupedPiecesGroups, ungroupedPiecesIndex, ungroupedPiecesLevels, newBasePlanLocked, false);
            }
            
            @Override
            public String getPresentationName() {
              return preferences.getLocalizedString(FurnitureController.class, "undoUngroupName");
            }
          };
        this.undoSupport.postEdit(undoableEdit);
      }
    }
  }

  /**
   * Displays the wizard that helps to import furniture to home. 
   */
  public void importFurniture() {
    new ImportedFurnitureWizardController(this.home, this.preferences, this, this.viewFactory, 
        this.contentManager, this.undoSupport).displayView(getView());
  }
  
  /**
   * Displays the wizard that helps to import furniture to home with a
   * given model name. 
   */
  public void importFurniture(String modelName) {
    new ImportedFurnitureWizardController(this.home, modelName, this.preferences, this, 
        this.viewFactory, this.contentManager, this.undoSupport).displayView(getView());
  }
  
  /**
   * Controls the alignment of selected furniture on top of the first selected piece.
   */
  public void alignSelectedFurnitureOnTop() {
    alignSelectedFurniture(new AlignmentAction() {
        public void alignFurniture(AlignedPieceOfFurniture [] alignedFurniture, 
                                   HomePieceOfFurniture leadPiece) {
          float minYLeadPiece = getMinY(leadPiece);
          for (AlignedPieceOfFurniture alignedPiece : alignedFurniture) {
            HomePieceOfFurniture piece = alignedPiece.getPieceOfFurniture();
            float minY = getMinY(piece);
            piece.setY(piece.getY() + minYLeadPiece - minY);
          }
        }
      });
  }
  
  /**
   * Controls the alignment of selected furniture on bottom of the first selected piece.
   */
  public void alignSelectedFurnitureOnBottom() {
    alignSelectedFurniture(new AlignmentAction() {
        public void alignFurniture(AlignedPieceOfFurniture [] alignedFurniture, 
                                   HomePieceOfFurniture leadPiece) {
          float maxYLeadPiece = getMaxY(leadPiece);
          for (AlignedPieceOfFurniture alignedPiece : alignedFurniture) {
            HomePieceOfFurniture piece = alignedPiece.getPieceOfFurniture();
            float maxY = getMaxY(piece);
            piece.setY(piece.getY() + maxYLeadPiece - maxY);
          }
        }
      });
  }

  /**
   * Controls the alignment of selected furniture on left of the first selected piece.
   */
  public void alignSelectedFurnitureOnLeft() {
    alignSelectedFurniture(new AlignmentAction() {
        public void alignFurniture(AlignedPieceOfFurniture [] alignedFurniture, 
                                   HomePieceOfFurniture leadPiece) {
          float minXLeadPiece = getMinX(leadPiece);
          for (AlignedPieceOfFurniture alignedPiece : alignedFurniture) {
            HomePieceOfFurniture piece = alignedPiece.getPieceOfFurniture();
            float minX = getMinX(piece);
            piece.setX(piece.getX() + minXLeadPiece - minX);
          }
        }
      });
  }

  /**
   * Controls the alignment of selected furniture on right of the first selected piece.
   */
  public void alignSelectedFurnitureOnRight() {
    alignSelectedFurniture(new AlignmentAction() {
        public void alignFurniture(AlignedPieceOfFurniture [] alignedFurniture, 
                                   HomePieceOfFurniture leadPiece) {
          float maxXLeadPiece = getMaxX(leadPiece);
          for (AlignedPieceOfFurniture alignedPiece : alignedFurniture) {
            HomePieceOfFurniture piece = alignedPiece.getPieceOfFurniture();
            float maxX = getMaxX(piece);
            piece.setX(piece.getX() + maxXLeadPiece - maxX);
          }
        }
      });
  }

  /**
   * Controls the alignment of selected furniture on the front side of the first selected piece.
   */
  public void alignSelectedFurnitureOnFrontSide() {
    alignSelectedFurniture(new AlignmentAction() {
        public void alignFurniture(AlignedPieceOfFurniture [] alignedFurniture, 
                                   HomePieceOfFurniture leadPiece) {
          float [][] points = leadPiece.getPoints();
          Line2D frontLine = new Line2D.Float(points [2][0], points [2][1], points [3][0], points [3][1]);    
          for (AlignedPieceOfFurniture alignedPiece : alignedFurniture) {
            alignPieceOfFurnitureAlongSides(alignedPiece.getPieceOfFurniture(), leadPiece, frontLine, true, null, 0);
          }
        }
      });
  }

  /**
   * Controls the alignment of selected furniture on the back side of the first selected piece.
   */
  public void alignSelectedFurnitureOnBackSide() {
    alignSelectedFurniture(new AlignmentAction() {
        public void alignFurniture(AlignedPieceOfFurniture [] alignedFurniture, 
                                   HomePieceOfFurniture leadPiece) {
          float [][] points = leadPiece.getPoints();
          Line2D backLine = new Line2D.Float(points [0][0], points [0][1], points [1][0], points [1][1]);    
          for (AlignedPieceOfFurniture alignedPiece : alignedFurniture) {
            alignPieceOfFurnitureAlongSides(alignedPiece.getPieceOfFurniture(), leadPiece, backLine, false, null, 0);
          }
        }
      });
  }

  /**
   * Controls the alignment of selected furniture on the left side of the first selected piece.
   */
  public void alignSelectedFurnitureOnLeftSide() {
    alignSelectedFurniture(new AlignmentAction() {
        public void alignFurniture(AlignedPieceOfFurniture [] alignedFurniture, 
                                   HomePieceOfFurniture leadPiece) {
          float [][] points = leadPiece.getPoints();
          Line2D leftLine = new Line2D.Float(points [3][0], points [3][1], points [0][0], points [0][1]);  
          for (AlignedPieceOfFurniture alignedPiece : alignedFurniture) {
            alignPieceOfFurnitureAlongLeftOrRightSides(alignedPiece.getPieceOfFurniture(), leadPiece, leftLine, false);
          }
        }
      });
  }

  /**
   * Controls the alignment of selected furniture on the right side of the first selected piece.
   */
  public void alignSelectedFurnitureOnRightSide() {
    alignSelectedFurniture(new AlignmentAction() {
        public void alignFurniture(AlignedPieceOfFurniture [] alignedFurniture, 
                                   HomePieceOfFurniture leadPiece) {
          float [][] points = leadPiece.getPoints();
          Line2D rightLine = new Line2D.Float(points [1][0], points [1][1], points [2][0], points [2][1]);     
          for (AlignedPieceOfFurniture alignedPiece : alignedFurniture) {
            alignPieceOfFurnitureAlongLeftOrRightSides(alignedPiece.getPieceOfFurniture(), leadPiece, rightLine, true);
          }
        }
      });
  }

  /**
   * Controls the alignment of selected furniture on the sides of the first selected piece.
   */
  public void alignSelectedFurnitureSideBySide() {
    alignSelectedFurniture(new AlignmentAction() {
        public void alignFurniture(AlignedPieceOfFurniture [] alignedFurniture, 
                                   HomePieceOfFurniture leadPiece) {
          alignFurnitureSideBySide(alignedFurniture, leadPiece);
        }
      });
  }

  private void alignFurnitureSideBySide(AlignedPieceOfFurniture [] alignedFurniture, 
                                        HomePieceOfFurniture leadPiece) {
    float [][] points = leadPiece.getPoints();
    final Line2D centerLine = new Line2D.Float(leadPiece.getX(), leadPiece.getY(), 
        (points [0][0] + points [1][0]) / 2, (points [0][1] + points [1][1]) / 2);
    List<HomePieceOfFurniture> furnitureSortedAlongBackLine = sortFurniture(alignedFurniture, leadPiece, centerLine);
    
    int leadPieceIndex = furnitureSortedAlongBackLine.indexOf(leadPiece);    
    Line2D backLine = new Line2D.Float(points [0][0], points [0][1], points [1][0], points [1][1]);    
    float sideDistance = leadPiece.getWidthInPlan() / 2;
    for (int i = leadPieceIndex + 1; i < furnitureSortedAlongBackLine.size(); i++) {
      sideDistance += alignPieceOfFurnitureAlongSides(furnitureSortedAlongBackLine.get(i), 
          leadPiece, backLine, false, centerLine, sideDistance);
    }
    sideDistance = -leadPiece.getWidthInPlan() / 2;
    for (int i = leadPieceIndex - 1; i >= 0; i--) {
      sideDistance -= alignPieceOfFurnitureAlongSides(furnitureSortedAlongBackLine.get(i), 
          leadPiece, backLine, false, centerLine, sideDistance);
    }
  }

  /**
   * Returns a list containing aligned furniture and lead piece sorted in the order of their distribution along
   * a line orthogonal to the given axis.
   */
  public List<HomePieceOfFurniture> sortFurniture(AlignedPieceOfFurniture [] furniture,
                                                  HomePieceOfFurniture leadPiece, 
                                                  final Line2D orthogonalAxis) {
    List<HomePieceOfFurniture> sortedFurniture = new ArrayList<HomePieceOfFurniture>(furniture.length + 1);
    if (leadPiece != null) {
      sortedFurniture.add(leadPiece);
    }
    for (AlignedPieceOfFurniture piece : furniture) {
      sortedFurniture.add(piece.getPieceOfFurniture());
    }
    Collections.sort(sortedFurniture, new Comparator<HomePieceOfFurniture>() {
        public int compare(HomePieceOfFurniture p1, HomePieceOfFurniture p2) {
          return Double.compare(orthogonalAxis.ptLineDistSq(p2.getX(), p2.getY()) * orthogonalAxis.relativeCCW(p2.getX(), p2.getY()),
              orthogonalAxis.ptLineDistSq(p1.getX(), p1.getY()) * orthogonalAxis.relativeCCW(p1.getX(), p1.getY()));
        }
      });
    return sortedFurniture;
  }

  /**
   * Aligns the given <code>piece</code> along the front or back side of the lead piece and its left or right side 
   * at a distance equal to <code>sideDistance</code>, and returns the width of the bounding box of 
   * the <code>piece</code> along the back side axis. 
   */
  private double alignPieceOfFurnitureAlongSides(HomePieceOfFurniture piece, HomePieceOfFurniture leadPiece,
                                                 Line2D frontOrBackLine, boolean frontLine, 
                                                 Line2D centerLine, float sideDistance) {
    // Search the distance required to align piece on the front or back side 
    double distance = frontOrBackLine.relativeCCW(piece.getX(), piece.getY()) * frontOrBackLine.ptLineDist(piece.getX(), piece.getY()) 
        + getPieceBoundingRectangleHeight(piece, -leadPiece.getAngle()) / 2;
    if (frontLine) {
      distance = -distance;
    }
    double sinLeadPieceAngle = Math.sin(leadPiece.getAngle());
    double cosLeadPieceAngle = Math.cos(leadPiece.getAngle());
    float deltaX = (float)(-distance * sinLeadPieceAngle);
    float deltaY = (float)(distance * cosLeadPieceAngle);

    double rotatedBoundingBoxWidth = getPieceBoundingRectangleWidth(piece, -leadPiece.getAngle());
    if (centerLine != null) {
      // Search the distance required to align piece on the side of the previous piece
      int location = centerLine.relativeCCW(piece.getX(), piece.getY());
      if (location == 0) {
        location = frontLine ? 1 : -1;
      }
      distance = sideDistance + location 
          * (centerLine.ptLineDist(piece.getX(), piece.getY()) - rotatedBoundingBoxWidth / 2);      
      deltaX += (float)(distance * cosLeadPieceAngle);
      deltaY += (float)(distance * sinLeadPieceAngle);
    }
    
    piece.move(deltaX, deltaY);
    return rotatedBoundingBoxWidth;
  }

  /**
   * Aligns the given <code>piece</code> along the left or right side of the lead piece. 
   */
  private void alignPieceOfFurnitureAlongLeftOrRightSides(HomePieceOfFurniture piece, HomePieceOfFurniture leadPiece,
                                                          Line2D leftOrRightLine, boolean rightLine) {
    // Search the distance required to align piece on the side of the lead piece
    double distance = leftOrRightLine.relativeCCW(piece.getX(), piece.getY()) * leftOrRightLine.ptLineDist(piece.getX(), piece.getY())
        + getPieceBoundingRectangleWidth(piece, -leadPiece.getAngle()) / 2;
    if (rightLine) {
      distance = -distance;
    }
    piece.move((float)(distance * Math.cos(leadPiece.getAngle())), (float)(distance * Math.sin(leadPiece.getAngle())));
  }

  /**
   * Returns the bounding box width of the given piece when it's rotated of an additional angle.  
   */
  private double getPieceBoundingRectangleWidth(HomePieceOfFurniture piece, float additionalAngle) {
    return Math.abs(piece.getWidthInPlan() * Math.cos(additionalAngle + piece.getAngle())) 
        + Math.abs(piece.getDepthInPlan() * Math.sin(additionalAngle + piece.getAngle()));
  }

  /**
   * Returns the bounding box height of the given piece when it's rotated of an additional angle.  
   */
  private double getPieceBoundingRectangleHeight(HomePieceOfFurniture piece, float additionalAngle) {
    return Math.abs(piece.getWidthInPlan() * Math.sin(additionalAngle + piece.getAngle())) 
        + Math.abs(piece.getDepthInPlan() * Math.cos(additionalAngle + piece.getAngle()));
  }

  /**
   * Controls the alignment of selected furniture.
   */
  private void alignSelectedFurniture(final AlignmentAction alignmentAction) {
    final List<HomePieceOfFurniture> selectedFurniture = getMovableSelectedFurniture();
    if (selectedFurniture.size() >= 2) {
      final List<Selectable> oldSelection = this.home.getSelectedItems();
      final HomePieceOfFurniture leadPiece = this.leadSelectedPieceOfFurniture;
      final AlignedPieceOfFurniture [] alignedFurniture = 
          AlignedPieceOfFurniture.getAlignedFurniture(selectedFurniture, leadPiece);
      this.home.setSelectedItems(selectedFurniture);
      alignmentAction.alignFurniture(alignedFurniture, leadPiece);
      if (this.undoSupport != null) {
        UndoableEdit undoableEdit = new AbstractUndoableEdit() {
          @Override
          public void undo() throws CannotUndoException {
            super.undo();
            undoAlignFurniture(alignedFurniture); 
            home.setSelectedItems(oldSelection);
          }
          
          @Override
          public void redo() throws CannotRedoException {
            super.redo();
            home.setSelectedItems(selectedFurniture);
            alignmentAction.alignFurniture(alignedFurniture, leadPiece);
          }
          
          @Override
          public String getPresentationName() {
            return preferences.getLocalizedString(FurnitureController.class, "undoAlignName");
          }
        };
        this.undoSupport.postEdit(undoableEdit);
      }
    }
  }
  
  private List<HomePieceOfFurniture> getMovableSelectedFurniture() {
    List<HomePieceOfFurniture> movableSelectedFurniture = new ArrayList<HomePieceOfFurniture>();
    for (Selectable item : this.home.getSelectedItems()) {
      if (item instanceof HomePieceOfFurniture) {
        HomePieceOfFurniture piece = (HomePieceOfFurniture)item;
        if (isPieceOfFurnitureMovable(piece)) {
          movableSelectedFurniture.add(piece);
        }
      }
    }  
    return movableSelectedFurniture;
  }

  private void undoAlignFurniture(AlignedPieceOfFurniture [] alignedFurniture) {
    for (AlignedPieceOfFurniture alignedPiece : alignedFurniture) {
      HomePieceOfFurniture piece = alignedPiece.getPieceOfFurniture();
      piece.setX(alignedPiece.getX());
      piece.setY(alignedPiece.getY());
    }
  }
  
  /**
   * Returns the minimum abscissa of the vertices of <code>piece</code>.  
   */
  private float getMinX(HomePieceOfFurniture piece) {
    float [][] points = piece.getPoints();
    float minX = Float.POSITIVE_INFINITY;
    for (float [] point : points) {
      minX = Math.min(minX, point [0]);
    } 
    return minX;
  }

  /**
   * Returns the maximum abscissa of the vertices of <code>piece</code>.  
   */
  private float getMaxX(HomePieceOfFurniture piece) {
    float [][] points = piece.getPoints();
    float maxX = Float.NEGATIVE_INFINITY;
    for (float [] point : points) {
      maxX = Math.max(maxX, point [0]);
    } 
    return maxX;
  }

  /**
   * Returns the minimum ordinate of the vertices of <code>piece</code>.  
   */
  private float getMinY(HomePieceOfFurniture piece) {
    float [][] points = piece.getPoints();
    float minY = Float.POSITIVE_INFINITY;
    for (float [] point : points) {
      minY = Math.min(minY, point [1]);
    } 
    return minY;
  }

  /**
   * Returns the maximum ordinate of the vertices of <code>piece</code>.  
   */
  private float getMaxY(HomePieceOfFurniture piece) {
    float [][] points = piece.getPoints();
    float maxY = Float.NEGATIVE_INFINITY;
    for (float [] point : points) {
      maxY = Math.max(maxY, point [1]);
    } 
    return maxY;
  }

  /**
   * Controls the distribution of the selected furniture along horizontal axis.
   */
  public void distributeSelectedFurnitureHorizontally() {
    distributeSelectedFurniture(true);
  }
  
  /**
   * Controls the distribution of the selected furniture along vertical axis.
   */
  public void distributeSelectedFurnitureVertically() {
    distributeSelectedFurniture(false);
  }
  
  /**
   * Controls the distribution of the selected furniture along the axis orthogonal to the given one.
   */
  public void distributeSelectedFurniture(final boolean horizontal) {
    final List<HomePieceOfFurniture> selectedFurniture = getMovableSelectedFurniture();
    if (selectedFurniture.size() >= 3) {
      final List<Selectable> oldSelection = this.home.getSelectedItems();
      final AlignedPieceOfFurniture [] alignedFurniture = 
          AlignedPieceOfFurniture.getAlignedFurniture(selectedFurniture, null);
      this.home.setSelectedItems(selectedFurniture);
      doDistributeFurnitureAlongAxis(alignedFurniture, horizontal);
      if (this.undoSupport != null) {
        UndoableEdit undoableEdit = new AbstractUndoableEdit() {
          @Override
          public void undo() throws CannotUndoException {
            super.undo();
            undoAlignFurniture(alignedFurniture); 
            home.setSelectedItems(oldSelection);
          }
          
          @Override
          public void redo() throws CannotRedoException {
            super.redo();
            home.setSelectedItems(selectedFurniture);
            doDistributeFurnitureAlongAxis(alignedFurniture, horizontal);
          }
          
          @Override
          public String getPresentationName() {
            return preferences.getLocalizedString(FurnitureController.class, "undoDistributeName");
          }
        };
        this.undoSupport.postEdit(undoableEdit);
      }
    }
  }

  private void doDistributeFurnitureAlongAxis(AlignedPieceOfFurniture [] alignedFurniture,
                                              boolean horizontal) {
    Line2D orthogonalAxis = horizontal ? new Line2D.Float(0, 0, 0, -1) : new Line2D.Float(0, 0, 1, 0);
    List<HomePieceOfFurniture> furnitureHorizontallySorted = sortFurniture(alignedFurniture, null, orthogonalAxis);
    float axisAngle = (float)(horizontal ? 0 : Math.PI / 2);
    HomePieceOfFurniture firstPiece = furnitureHorizontallySorted.get(0);
    double firstPieceBoundingRectangleHalfWidth = getPieceBoundingRectangleWidth(firstPiece, axisAngle) / 2;
    HomePieceOfFurniture lastPiece = furnitureHorizontallySorted.get(furnitureHorizontallySorted.size() - 1);
    double lastPieceBoundingRectangleHalfWidth = getPieceBoundingRectangleWidth(lastPiece, axisAngle) / 2;
    double gap = Math.abs(orthogonalAxis.ptLineDist(lastPiece.getX(), lastPiece.getY()) * orthogonalAxis.relativeCCW(lastPiece.getX(), lastPiece.getY()) 
          - orthogonalAxis.ptLineDist(firstPiece.getX(), firstPiece.getY()) * orthogonalAxis.relativeCCW(firstPiece.getX(), firstPiece.getY()))
        - lastPieceBoundingRectangleHalfWidth
        - firstPieceBoundingRectangleHalfWidth;
    double [] furnitureWidthsAlongAxis = new double [furnitureHorizontallySorted.size() - 2];
    for (int i = 1; i < furnitureHorizontallySorted.size() - 1; i++) {
      HomePieceOfFurniture piece = furnitureHorizontallySorted.get(i);
      furnitureWidthsAlongAxis [i - 1] = getPieceBoundingRectangleWidth(piece, axisAngle);
      gap -= furnitureWidthsAlongAxis [i - 1];
    }
    gap /= furnitureHorizontallySorted.size() - 1;
    float xOrY = (horizontal ? firstPiece.getX() : firstPiece.getY()) 
        + (float)(firstPieceBoundingRectangleHalfWidth + gap); 
    for (int i = 1; i < furnitureHorizontallySorted.size() - 1; i++) {
      HomePieceOfFurniture piece = furnitureHorizontallySorted.get(i);
      if (horizontal) {
        piece.setX((float)(xOrY + furnitureWidthsAlongAxis [i - 1] / 2));
      } else {
        piece.setY((float)(xOrY + furnitureWidthsAlongAxis [i - 1] / 2));
      }
      xOrY += gap + furnitureWidthsAlongAxis [i - 1];
    }
  }

  /**
   * Resets the elevation of the selected furniture to its default elevation.
   * @since 4.4
   */
  public void resetFurnitureElevation() {
    final List<HomePieceOfFurniture> selectedFurniture = getMovableSelectedFurniture();
    if (selectedFurniture.size() >= 1) {
      final List<Selectable> oldSelection = this.home.getSelectedItems();
      final float [] furnitureOldElevation = new float [selectedFurniture.size()];
      for (int i = 0; i < selectedFurniture.size(); i++) {
        furnitureOldElevation [i] = selectedFurniture.get(i).getElevation();
      }
      this.home.setSelectedItems(selectedFurniture);
      doResetFurnitureElevation(selectedFurniture);
      if (this.undoSupport != null) {
        UndoableEdit undoableEdit = new AbstractUndoableEdit() {
          @Override
          public void undo() throws CannotUndoException {
            super.undo();
            for (int i = 0; i < selectedFurniture.size(); i++) {
              selectedFurniture.get(i).setElevation(furnitureOldElevation [i]);
            }
            home.setSelectedItems(oldSelection);
          }
          
          @Override
          public void redo() throws CannotRedoException {
            super.redo();
            home.setSelectedItems(selectedFurniture);
            doResetFurnitureElevation(selectedFurniture);
          }
          
          @Override
          public String getPresentationName() {
            return preferences.getLocalizedString(FurnitureController.class, "undoResetElevation");
          }
        };
        this.undoSupport.postEdit(undoableEdit);
      }
    }
  }
  
  private void doResetFurnitureElevation(List<HomePieceOfFurniture> selectedFurniture) {
    for (HomePieceOfFurniture piece : selectedFurniture) {
      HomePieceOfFurniture highestSurroundingPiece = getHighestSurroundingPieceOfFurniture(piece, selectedFurniture);
      if (highestSurroundingPiece != null) {
        float elevation = highestSurroundingPiece.getElevation() 
            + highestSurroundingPiece.getHeightInPlan() * highestSurroundingPiece.getDropOnTopElevation();
        if (highestSurroundingPiece.getLevel() != null) {
          elevation += highestSurroundingPiece.getLevel().getElevation() - piece.getLevel().getElevation();
        }
        piece.setElevation(Math.max(0, elevation));
      } else {
        piece.setElevation(0);
      }
    }
  }

  /**
   * Returns the highest piece of furniture that includes the given <code>piece</code> 
   * with a margin error of 5% of the smallest side length.
   * @since 4.4
   */
  protected HomePieceOfFurniture getHighestSurroundingPieceOfFurniture(HomePieceOfFurniture piece) {
    List<HomePieceOfFurniture> ignoredFurniture = Collections.emptyList();
    return getHighestSurroundingPieceOfFurniture(piece, ignoredFurniture);
  }
  
  private HomePieceOfFurniture getHighestSurroundingPieceOfFurniture(HomePieceOfFurniture piece,
                                                                     List<HomePieceOfFurniture> ignoredFurniture) {
    float [][] piecePoints = piece.getPoints();
    float margin = Math.min(piece.getWidthInPlan(), piece.getDepthInPlan()) * 0.05f;
    HomePieceOfFurniture highestSurroundingPiece = null;
    float highestElevation = Float.MIN_VALUE;
    for (HomePieceOfFurniture homePiece : getFurnitureInSameGroup(piece)) {
      if (homePiece != piece 
          && !ignoredFurniture.contains(homePiece)
          && isPieceOfFurnitureVisibleAtSelectedLevel(homePiece)
          && homePiece.getDropOnTopElevation() >= 0) {
        boolean surroundingPieceContainsPiece = true;
        for (float [] point : piecePoints) {
          if (!homePiece.containsPoint(point [0], point [1], margin)) {
            surroundingPieceContainsPiece = false;
            break;
          }
        }
        if (surroundingPieceContainsPiece) {
          float elevation = homePiece.getElevation() 
              + homePiece.getHeightInPlan() * homePiece.getDropOnTopElevation();
          if (elevation > highestElevation) {
            highestElevation = elevation;
            highestSurroundingPiece = homePiece;
          }
        }
      }
    }
    return highestSurroundingPiece;
  }
  
  /**
   * Returns the furniture list of the given <code>piece</code> which belongs to same group
   * or home furniture if it doesn't belong to home furniture.
   * @since 5.0
   */
  protected List<HomePieceOfFurniture> getFurnitureInSameGroup(HomePieceOfFurniture piece) {
    List<HomePieceOfFurniture> homeFurniture = this.home.getFurniture();
    List<HomePieceOfFurniture> furnitureInSameGroup = getFurnitureInSameGroup(piece, homeFurniture);
    if (furnitureInSameGroup != null) {
      return furnitureInSameGroup;
    } else {
      return homeFurniture;
    }
  }
  
  private List<HomePieceOfFurniture> getFurnitureInSameGroup(HomePieceOfFurniture piece, List<HomePieceOfFurniture> furniture) {
    for (HomePieceOfFurniture piece2 : furniture) {
      if (piece2 == piece) {
        return furniture;
      } else if (piece2 instanceof HomeFurnitureGroup) {
        List<HomePieceOfFurniture> siblingFurniture = getFurnitureInSameGroup(piece, ((HomeFurnitureGroup)piece2).getFurniture());
        if (siblingFurniture != null) {
          return siblingFurniture;
        }
      }
    }
    return null;
  }

  /**
   * Returns <code>true</code> if the given piece is viewable and 
   * its height and elevation make it viewable at the selected level in home.
   * @since 4.4
   */
  protected boolean isPieceOfFurnitureVisibleAtSelectedLevel(HomePieceOfFurniture piece) {
    Level selectedLevel = this.home.getSelectedLevel();
    return piece.isVisible()
        && (piece.getLevel() == null
            || piece.getLevel().isViewable())
        && (piece.getLevel() == selectedLevel
            || piece.isAtLevel(selectedLevel));
  }
  
  /**
   * Controls the change of value of a visual property in home.
   * @deprecated {@link #setVisualProperty(String, Object) setVisualProperty} should be replaced by a call to 
   * {@link #setHomeProperty(String, String)} to ensure the property can be easily saved and read.
   * @since 5.0
   */
  public void setVisualProperty(String propertyName,
                                Object propertyValue) {
    this.home.setVisualProperty(propertyName, propertyValue);
  }

  /**
   * Controls the change of value of a property in home.
   * @since 5.2
   */
  public void setHomeProperty(String propertyName,
                                String propertyValue) {
    this.home.setProperty(propertyName, propertyValue);
  }

  /**
   * Stores the current x or y value of an aligned piece of furniture.
   */
  private static class AlignedPieceOfFurniture {
    private HomePieceOfFurniture piece;
    private float                x;
    private float                y;
    
    public AlignedPieceOfFurniture(HomePieceOfFurniture piece) {
      this.piece = piece;
      this.x = piece.getX();
      this.y = piece.getY();
    }

    public HomePieceOfFurniture getPieceOfFurniture() {
      return this.piece;
    }

    public float getX() {
      return this.x;
    }

    public float getY() {
      return this.y;
    }

    /**
     * A helper method that returns an array of <code>AlignedPieceOfFurniture</code>
     * built from <code>furniture</code> pieces excepted for <code>leadPiece</code>.
     */
    public static AlignedPieceOfFurniture [] getAlignedFurniture(List<HomePieceOfFurniture> furniture, 
                                                                 HomePieceOfFurniture leadPiece) {
      final AlignedPieceOfFurniture[] alignedFurniture =
          new AlignedPieceOfFurniture[leadPiece == null  ? furniture.size()  : furniture.size() - 1];
      int i = 0;
      for (HomePieceOfFurniture piece : furniture) {
        if (piece != leadPiece) {
          alignedFurniture[i++] = new AlignedPieceOfFurniture(piece);
        }
      }
      return alignedFurniture;
    }
  }

  /**
   * Describes how to align furniture on a lead piece.
   */
  private static interface AlignmentAction {
    public void alignFurniture(AlignedPieceOfFurniture [] alignedFurniture, 
                               HomePieceOfFurniture leadPiece);
  }
}
