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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
          } else if (leadSelectedPieceOfFurniture == null ||
                     selectedFurniture.size() == 1) {
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
    final boolean oldBasePlanLocked = this.home.isBasePlanLocked();
    final List<Selectable> oldSelection = this.home.getSelectedItems(); 
    final HomePieceOfFurniture [] newFurniture = furniture.
        toArray(new HomePieceOfFurniture [furniture.size()]);
    // Get indices of furniture added to home
    final int [] furnitureIndex = new int [furniture.size()];
    int endIndex = this.home.getFurniture().size();
    boolean basePlanLocked = oldBasePlanLocked;
    for (int i = 0; i < furnitureIndex.length; i++) {
      furnitureIndex [i] = endIndex++; 
      // Unlock base plan if the piece is a part of it
      basePlanLocked &= !isPieceOfFurniturePartOfBasePlan(newFurniture [i]);
    }  
    final boolean newBasePlanLocked = basePlanLocked;
    
    doAddFurniture(newFurniture, furnitureIndex, newBasePlanLocked); 
    if (this.undoSupport != null) {
      UndoableEdit undoableEdit = new AbstractUndoableEdit() {
        @Override
        public void undo() throws CannotUndoException {
          super.undo();
          doDeleteFurniture(newFurniture, oldBasePlanLocked); 
          home.setSelectedItems(oldSelection); 
        }
        
        @Override
        public void redo() throws CannotRedoException {
          super.redo();
          doAddFurniture(newFurniture, furnitureIndex, newBasePlanLocked); 
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
                              int [] furnitureIndex,
                              boolean basePlanLocked) {
    for (int i = 0; i < furnitureIndex.length; i++) {
      this.home.addPieceOfFurniture (furniture [i], furnitureIndex [i]);
    }
    this.home.setBasePlanLocked(basePlanLocked);
    this.home.setSelectedItems(Arrays.asList(furniture)); 
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
    List<HomePieceOfFurniture> homeFurniture = this.home.getFurniture(); 
    // Sort the deletable furniture in the ascending order of their index in home
    Map<Integer, HomePieceOfFurniture> sortedMap = 
        new TreeMap<Integer, HomePieceOfFurniture>(); 
    for (HomePieceOfFurniture piece : deletedFurniture) {
      if (isPieceOfFurnitureDeletable(piece)) {
        sortedMap.put(homeFurniture.indexOf(piece), piece);
      }
    }
    final HomePieceOfFurniture [] furniture = sortedMap.values().
        toArray(new HomePieceOfFurniture [sortedMap.size()]); 
    final int [] furnitureIndex = new int [furniture.length];
    int i = 0;
    for (int index : sortedMap.keySet()) {
      furnitureIndex [i++] = index; 
    }
    doDeleteFurniture(furniture, basePlanLocked); 
    if (this.undoSupport != null) {
      UndoableEdit undoableEdit = new AbstractUndoableEdit() {
        @Override
        public void undo() throws CannotUndoException {
          super.undo();
          doAddFurniture(furniture, furnitureIndex, basePlanLocked); 
        }
        
        @Override
        public void redo() throws CannotRedoException {
          super.redo();
          home.setSelectedItems(Arrays.asList(furniture));
          doDeleteFurniture(furniture, basePlanLocked); 
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
                                 boolean basePlanLocked) { 
    for (HomePieceOfFurniture piece : furniture) {
      this.home.deletePieceOfFurniture(piece);
    }
    this.home.setBasePlanLocked(basePlanLocked);
  }

  /**
   * Updates the selected furniture in home.
   */
  public void setSelectedFurniture(List<HomePieceOfFurniture> selectedFurniture) {
    if (this.home.isBasePlanLocked()) {
      selectedFurniture = getFurnitureNotPartOfBasePlan(selectedFurniture);
    }
    this.home.setSelectedItems(selectedFurniture);
  }

  /**
   * Selects all furniture in home.
   */
  public void selectAll() {
    setSelectedFurniture(this.home.getFurniture());
  }
  
  /**
   * Returns <code>true</code> if the given <code>piece</code> is movable.
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
   * sorted on the given <code>furnitureProperty<code>, it will be sorted in descending 
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
              HomePieceOfFurniture.SortableProperty.WIDTH,
              HomePieceOfFurniture.SortableProperty.DEPTH,
              HomePieceOfFurniture.SortableProperty.HEIGHT,
              HomePieceOfFurniture.SortableProperty.X,
              HomePieceOfFurniture.SortableProperty.Y,
              HomePieceOfFurniture.SortableProperty.ELEVATION,
              HomePieceOfFurniture.SortableProperty.ANGLE,
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
   * Groups the selected furniture as one piece of furniture.
   */
  public void groupSelectedFurniture() {
    List<HomePieceOfFurniture> selectedFurniture = getMovableSelectedFurniture();
    if (!selectedFurniture.isEmpty()) {
      final boolean basePlanLocked = this.home.isBasePlanLocked();
      final List<Selectable> oldSelection = this.home.getSelectedItems();
      List<HomePieceOfFurniture> homeFurniture = this.home.getFurniture();
      // Sort the grouped furniture in the ascending order of their index in home
      Map<Integer, HomePieceOfFurniture> sortedMap = 
          new TreeMap<Integer, HomePieceOfFurniture>(); 
      for (HomePieceOfFurniture piece : selectedFurniture) {
          sortedMap.put(homeFurniture.indexOf(piece), piece);
      }
      final HomePieceOfFurniture [] groupPieces = sortedMap.values().
          toArray(new HomePieceOfFurniture [sortedMap.size()]); 
      final int [] groupPiecesIndex = new int [groupPieces.length];
      final boolean [] groupPiecesMovable = new boolean [groupPieces.length];
      final boolean [] groupPiecesVisible = new boolean [groupPieces.length];
      int i = 0;
      for (Entry<Integer, HomePieceOfFurniture> pieceEntry : sortedMap.entrySet()) {
        groupPiecesIndex [i] = pieceEntry.getKey();
        groupPiecesMovable [i] = pieceEntry.getValue().isMovable();
        groupPiecesVisible [i++] = pieceEntry.getValue().isVisible();
      }

      final HomeFurnitureGroup furnitureGroup = createHomeFurnitureGroup(selectedFurniture);
      final int furnitureGroupIndex = homeFurniture.size() - groupPieces.length;
      final boolean movable = furnitureGroup.isMovable();
      
      doGroupFurniture(groupPieces, new HomeFurnitureGroup [] {furnitureGroup}, 
          new int [] {furnitureGroupIndex}, basePlanLocked);
      if (this.undoSupport != null) {
        UndoableEdit undoableEdit = new AbstractUndoableEdit() {
            @Override
            public void undo() throws CannotUndoException {
              super.undo();
              doUngroupFurniture(groupPieces, groupPiecesIndex, 
                  new HomeFurnitureGroup [] {furnitureGroup}, basePlanLocked);
              for (int i = 0; i < groupPieces.length; i++) {
                groupPieces [i].setMovable(groupPiecesMovable [i]);
                groupPieces [i].setVisible(groupPiecesVisible [i]);
              }
              home.setSelectedItems(oldSelection);
            }
            
            @Override
            public void redo() throws CannotRedoException {
              super.redo();
              doGroupFurniture(groupPieces, new HomeFurnitureGroup [] {furnitureGroup}, 
                  new int [] {furnitureGroupIndex}, basePlanLocked);
              furnitureGroup.setMovable(movable);
              furnitureGroup.setVisible(true);
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
    String furnitureGroupName = this.preferences.getLocalizedString(
        FurnitureController.class, "groupName", getFurnitureGroupCount(this.home.getFurniture()) + 1);
    final HomeFurnitureGroup furnitureGroup = new HomeFurnitureGroup(furniture, furnitureGroupName);
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

  private void doGroupFurniture(HomePieceOfFurniture [] groupPieces,
                                HomeFurnitureGroup [] furnitureGroups,
                                int [] furnitureGroupsIndex,
                                boolean basePlanLocked) {
    doDeleteFurniture(groupPieces, basePlanLocked);
    doAddFurniture(furnitureGroups, furnitureGroupsIndex, basePlanLocked);
  }

  private void doUngroupFurniture(HomePieceOfFurniture [] groupPieces,
                                  int [] groupPiecesIndex,
                                  HomeFurnitureGroup [] furnitureGroups,
                                  boolean basePlanLocked) {
    doDeleteFurniture(furnitureGroups, basePlanLocked);
    doAddFurniture(groupPieces, groupPiecesIndex, basePlanLocked);
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
      final boolean oldBasePlanLocked = this.home.isBasePlanLocked();
      final List<Selectable> oldSelection = this.home.getSelectedItems();
      List<HomePieceOfFurniture> homeFurniture = this.home.getFurniture();
      // Sort the groups in the ascending order of their index in home
      Map<Integer, HomeFurnitureGroup> sortedMap = 
          new TreeMap<Integer, HomeFurnitureGroup>(); 
      for (HomeFurnitureGroup group : movableSelectedFurnitureGroups) {
          sortedMap.put(homeFurniture.indexOf(group), group);
      }
      final HomeFurnitureGroup [] furnitureGroups = sortedMap.values().
          toArray(new HomeFurnitureGroup [sortedMap.size()]); 
      final int [] furnitureGroupsIndex = new int [furnitureGroups.length];
      int i = 0;
      for (int index : sortedMap.keySet()) {
        furnitureGroupsIndex [i++] = index; 
      }

      List<HomePieceOfFurniture> groupPiecesList = new ArrayList<HomePieceOfFurniture>();
      for (HomeFurnitureGroup furnitureGroup : furnitureGroups) {
        groupPiecesList.addAll(furnitureGroup.getFurniture());
      }
      final HomePieceOfFurniture [] groupPieces = 
          groupPiecesList.toArray(new HomePieceOfFurniture [groupPiecesList.size()]);      
      final int [] groupPiecesIndex = new int [groupPieces.length];
      int endIndex = homeFurniture.size() - furnitureGroups.length;
      boolean basePlanLocked = oldBasePlanLocked;
      for (i = 0; i < groupPieces.length; i++) {
        groupPiecesIndex [i] = endIndex++; 
        // Unlock base plan if the piece is a part of it
        basePlanLocked &= !isPieceOfFurniturePartOfBasePlan(groupPieces [i]);
      }  
      final boolean newBasePlanLocked = basePlanLocked;

      doUngroupFurniture(groupPieces, groupPiecesIndex, furnitureGroups, newBasePlanLocked);
      if (this.undoSupport != null) {
        UndoableEdit undoableEdit = new AbstractUndoableEdit() {
            @Override
            public void undo() throws CannotUndoException {
              super.undo();
              doGroupFurniture(groupPieces, furnitureGroups, furnitureGroupsIndex, oldBasePlanLocked);
              home.setSelectedItems(oldSelection);
            }
            
            @Override
            public void redo() throws CannotRedoException {
              super.redo();
              doUngroupFurniture(groupPieces, groupPiecesIndex, furnitureGroups, newBasePlanLocked);
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
    final List<HomePieceOfFurniture> selectedFurniture = getMovableSelectedFurniture();
    if (selectedFurniture.size() >= 2) {
      final HomePieceOfFurniture leadPiece = this.leadSelectedPieceOfFurniture;
      final AlignedPieceOfFurniture [] alignedFurniture = 
          AlignedPieceOfFurniture.getAlignedFurniture(selectedFurniture, leadPiece, false);
      doAlignFurnitureOnTop(alignedFurniture, leadPiece);
      if (this.undoSupport != null) {
        UndoableEdit undoableEdit = new AbstractUndoableEdit() {
          @Override
          public void undo() throws CannotUndoException {
            super.undo();
            undoAlignFurniture(alignedFurniture, false); 
          }
          
          @Override
          public void redo() throws CannotRedoException {
            super.redo();
            home.setSelectedItems(selectedFurniture);
            doAlignFurnitureOnTop(alignedFurniture, leadPiece);
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

  private void doAlignFurnitureOnTop(AlignedPieceOfFurniture [] alignedFurniture, 
                                     HomePieceOfFurniture leadPiece) {
    float minYLeadPiece = getMinY(leadPiece);
    for (AlignedPieceOfFurniture alignedPiece : alignedFurniture) {
      HomePieceOfFurniture piece = alignedPiece.getPieceOfFurniture();
      float minY = getMinY(piece);
      piece.setY(piece.getY() + minYLeadPiece - minY);
    }
  }

  private void undoAlignFurniture(AlignedPieceOfFurniture [] alignedFurniture,
                                  boolean alignX) {
    for (AlignedPieceOfFurniture alignedPiece : alignedFurniture) {
      HomePieceOfFurniture piece = alignedPiece.getPieceOfFurniture();
      if (alignX) {
        piece.setX(alignedPiece.getXOrY());
      } else {
        piece.setY(alignedPiece.getXOrY());
      }
    }
  }
  
  /**
   * Controls the alignment of selected furniture on bottom of the first selected piece.
   */
  public void alignSelectedFurnitureOnBottom() {
    final List<HomePieceOfFurniture> selectedFurniture = getMovableSelectedFurniture();
    if (selectedFurniture.size() >= 2) {
      final HomePieceOfFurniture leadPiece = this.leadSelectedPieceOfFurniture;
      final AlignedPieceOfFurniture [] alignedFurniture = 
          AlignedPieceOfFurniture.getAlignedFurniture(selectedFurniture, leadPiece, false);
      doAlignFurnitureOnBottom(alignedFurniture, leadPiece);
      if (this.undoSupport != null) {
        UndoableEdit undoableEdit = new AbstractUndoableEdit() {
          @Override
          public void undo() throws CannotUndoException {
            super.undo();
            undoAlignFurniture(alignedFurniture, false); 
          }
          
          @Override
          public void redo() throws CannotRedoException {
            super.redo();
            home.setSelectedItems(selectedFurniture);
            doAlignFurnitureOnBottom(alignedFurniture, leadPiece);
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

  private void doAlignFurnitureOnBottom(AlignedPieceOfFurniture [] alignedFurniture, 
                                        HomePieceOfFurniture leadPiece) {
    float maxYLeadPiece = getMaxY(leadPiece);
    for (AlignedPieceOfFurniture alignedPiece : alignedFurniture) {
      HomePieceOfFurniture piece = alignedPiece.getPieceOfFurniture();
      float maxY = getMaxY(piece);
      piece.setY(piece.getY() + maxYLeadPiece - maxY);
    }
  }

  /**
   * Controls the alignment of selected furniture on left of the first selected piece.
   */
  public void alignSelectedFurnitureOnLeft() {
    final List<HomePieceOfFurniture> selectedFurniture = getMovableSelectedFurniture();
    if (selectedFurniture.size() >= 2) {
      final HomePieceOfFurniture leadPiece = this.leadSelectedPieceOfFurniture;
      final AlignedPieceOfFurniture [] alignedFurniture = 
          AlignedPieceOfFurniture.getAlignedFurniture(selectedFurniture, leadPiece, true);
      doAlignFurnitureOnLeft(alignedFurniture, leadPiece);
      if (this.undoSupport != null) {
        UndoableEdit undoableEdit = new AbstractUndoableEdit() {
          @Override
          public void undo() throws CannotUndoException {
            super.undo();
            undoAlignFurniture(alignedFurniture, true); 
          }
          
          @Override
          public void redo() throws CannotRedoException {
            super.redo();
            home.setSelectedItems(selectedFurniture);
            doAlignFurnitureOnLeft(alignedFurniture, leadPiece);
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

  private void doAlignFurnitureOnLeft(AlignedPieceOfFurniture [] alignedFurniture, 
                                      HomePieceOfFurniture leadPiece) {
    float minXLeadPiece = getMinX(leadPiece);
    for (AlignedPieceOfFurniture alignedPiece : alignedFurniture) {
      HomePieceOfFurniture piece = alignedPiece.getPieceOfFurniture();
      float minX = getMinX(piece);
      piece.setX(piece.getX() + minXLeadPiece - minX);
    }
  }

  /**
   * Controls the alignment of selected furniture on right of the first selected piece.
   */
  public void alignSelectedFurnitureOnRight() {
    final List<HomePieceOfFurniture> selectedFurniture = getMovableSelectedFurniture();
    if (selectedFurniture.size() >= 2) {
      final HomePieceOfFurniture leadPiece = this.leadSelectedPieceOfFurniture;
      final AlignedPieceOfFurniture [] alignedFurniture = 
          AlignedPieceOfFurniture.getAlignedFurniture(selectedFurniture, leadPiece, true);
      doAlignFurnitureOnRight(alignedFurniture, leadPiece);
      if (this.undoSupport != null) {
        UndoableEdit undoableEdit = new AbstractUndoableEdit() {
          @Override
          public void undo() throws CannotUndoException {
            super.undo();
            undoAlignFurniture(alignedFurniture, true); 
          }
          
          @Override
          public void redo() throws CannotRedoException {
            super.redo();
            home.setSelectedItems(selectedFurniture);
            doAlignFurnitureOnRight(alignedFurniture, leadPiece);
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
  
  private void doAlignFurnitureOnRight(AlignedPieceOfFurniture [] alignedFurniture, 
                                       HomePieceOfFurniture leadPiece) {
    float maxXLeadPiece = getMaxX(leadPiece);
    for (AlignedPieceOfFurniture alignedPiece : alignedFurniture) {
      HomePieceOfFurniture piece = alignedPiece.getPieceOfFurniture();
      float maxX = getMaxX(piece);
      piece.setX(piece.getX() + maxXLeadPiece - maxX);
    }
  }

  /**
   * Returns the minimum abcissa of the vertices of <code>piece</code>.  
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
   * Returns the maximum abcissa of the vertices of <code>piece</code>.  
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
   * Stores the current x or y value of an aligned piece of furniture.
   */
  private static class AlignedPieceOfFurniture {
    private HomePieceOfFurniture piece;
    private float                xOrY;
    
    public AlignedPieceOfFurniture(HomePieceOfFurniture piece, 
                                   boolean alignX) {
      this.piece = piece;
      if (alignX) {
        this.xOrY = piece.getX();
      } else {
        this.xOrY = piece.getY();
      }
    }

    public HomePieceOfFurniture getPieceOfFurniture() {
      return this.piece;
    }

    public float getXOrY() {
      return this.xOrY;
    }

    /**
     * A helper method that returns an array of <code>AlignedPieceOfFurniture</code>
     * built from <code>furniture</code> pieces excepted for <code>leadPiece</code>.
     */
    public static AlignedPieceOfFurniture [] getAlignedFurniture(List<HomePieceOfFurniture> furniture, 
                                                                 HomePieceOfFurniture leadPiece,
                                                                 boolean alignX) {
      final AlignedPieceOfFurniture[] alignedFurniture =
          new AlignedPieceOfFurniture[furniture.size() - 1];
      int i = 0;
      for (HomePieceOfFurniture piece : furniture) {
        if (piece != leadPiece) {
          alignedFurniture[i++] = new AlignedPieceOfFurniture(piece, alignX);
        }
      }
      return alignedFurniture;
    }
  }
}
