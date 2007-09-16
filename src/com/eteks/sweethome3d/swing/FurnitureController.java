/*
 * FurnitureController.java 15 mai 2006
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
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

import javax.swing.JComponent;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.ContentManager;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.SelectionEvent;
import com.eteks.sweethome3d.model.SelectionListener;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.HomePieceOfFurniture.SortableProperty;

/**
 * A MVC controller for the furniture table.
 * @author Emmanuel Puybaret
 */
public class FurnitureController {
  private Home                 home;
  private JComponent           furnitureView;
  private ResourceBundle       resource;
  private UndoableEditSupport  undoSupport;
  private UserPreferences      preferences;
  private ContentManager       contentManager;
  private HomePieceOfFurniture leadSelectedPieceOfFurniture;

  /**
   * Creates the controller of home furniture view.
   * @param home the home edited by this controller and its view
   * @param preferences the preferences of the application
   */
  public FurnitureController(Home home, 
                             UserPreferences preferences) {
    this(home, preferences, null, null); 
  }

  /**
   * Creates the controller of home furniture view with undo support.
   */
  public FurnitureController(Home home, 
                             UserPreferences preferences, 
                             ContentManager contentManager,
                             UndoableEditSupport undoSupport) {
    this.home = home;
    this.undoSupport = undoSupport;
    this.preferences = preferences;
    this.contentManager = contentManager;
    this.resource    = ResourceBundle.getBundle(
        FurnitureController.class.getName());
    this.furnitureView = new FurnitureTable(home, preferences, this);
    
    // Add a selection listener that gets the lead selected piece in home
    this.home.addSelectionListener(new SelectionListener() {
        public void selectionChanged(SelectionEvent ev) {
          List<HomePieceOfFurniture> selectedFurniture = 
              Home.getFurnitureSubList(ev.getSelectedItems());
          if (selectedFurniture.isEmpty()) {
            leadSelectedPieceOfFurniture = null;
          } else if (leadSelectedPieceOfFurniture == null ||
                     selectedFurniture.size() == 1) {
            leadSelectedPieceOfFurniture = selectedFurniture.get(0);
          }
        }
      });
  }

  /**
   * Returns the view associated with this controller.
   */
  public JComponent getView() {
    return this.furnitureView;
  }

  /**
   * Controls new furniture added to home. 
   * Once added the furniture will be selected in view 
   * and undo support will receive a new undoable edit.
   * @param furniture the furniture to add.
   */
  public void addFurniture(List<HomePieceOfFurniture> furniture) {
    final List<Object> oldSelection = this.home.getSelectedItems(); 
    final HomePieceOfFurniture [] newFurniture = furniture.
        toArray(new HomePieceOfFurniture [furniture.size()]);
    // Get indices of furniture add to home
    final int [] furnitureIndex = new int [furniture.size()];
    int endIndex = home.getFurniture().size();
    for (int i = 0; i < furnitureIndex.length; i++) {
      furnitureIndex [i] = endIndex++; 
    }
  
    doAddFurniture(newFurniture, furnitureIndex); 
    if (this.undoSupport != null) {
      UndoableEdit undoableEdit = new AbstractUndoableEdit() {
        @Override
        public void undo() throws CannotUndoException {
          super.undo();
          doDeleteFurniture(newFurniture); 
          home.setSelectedItems(oldSelection); 
        }
        
        @Override
        public void redo() throws CannotRedoException {
          super.redo();
          doAddFurniture(newFurniture, furnitureIndex); 
        }
        
        @Override
        public String getPresentationName() {
          return resource.getString("undoAddFurnitureName");
        }
      };
      this.undoSupport.postEdit(undoableEdit);
    }
  }
  
  private void doAddFurniture(HomePieceOfFurniture [] furniture,
                              int [] furnitureIndex) { 
    for (int i = 0; i < furnitureIndex.length; i++) {
      this.home.addPieceOfFurniture (furniture [i], 
                                     furnitureIndex [i]);
    }
    this.home.setSelectedItems(Arrays.asList(furniture)); 
  }
  
  /**
   * Controls the deletion of the current selected furniture in home.
   * Once the selected furniture is deleted, undo support will receive a new undoable edit.
   */
  public void deleteSelection() {
    List<HomePieceOfFurniture> homeFurniture = this.home.getFurniture(); 
    // Sort the selected furniture in the ascending order of their index in home
    Map<Integer, HomePieceOfFurniture> sortedMap = 
        new TreeMap<Integer, HomePieceOfFurniture>(); 
    for (Object item : this.home.getSelectedItems()) {
      if (item instanceof HomePieceOfFurniture) {
        HomePieceOfFurniture piece = (HomePieceOfFurniture)item;
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
    doDeleteFurniture(furniture); 
    if (this.undoSupport != null) {
      UndoableEdit undoableEdit = new AbstractUndoableEdit() {
        @Override
        public void undo() throws CannotUndoException {
          super.undo();
          doAddFurniture(furniture, furnitureIndex); 
        }
        
        @Override
        public void redo() throws CannotRedoException {
          super.redo();
          home.setSelectedItems(Arrays.asList(furniture));
          doDeleteFurniture(furniture); 
        }
        
        @Override
        public String getPresentationName() {
          return resource.getString("undoDeleteSelectionName");
        }
      };
      this.undoSupport.postEdit(undoableEdit);
    }
  }
  
  private void doDeleteFurniture(
                      HomePieceOfFurniture [] furniture) { 
    for (HomePieceOfFurniture piece : furniture) {
      this.home.deletePieceOfFurniture(piece);
    }
  }

  /**
   * Updates the selected furniture in home.
   */
  public void setSelectedFurniture(
           List<HomePieceOfFurniture> selectedFurniture) {
    this.home.setSelectedItems(selectedFurniture);
  }

  /**
   * Selects all furniture in home.
   */
  public void selectAll() {
    setSelectedFurniture(this.home.getFurniture());
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
              HomePieceOfFurniture.SortableProperty.NAME, 
              HomePieceOfFurniture.SortableProperty.WIDTH,
              HomePieceOfFurniture.SortableProperty.DEPTH,
              HomePieceOfFurniture.SortableProperty.HEIGHT,
              HomePieceOfFurniture.SortableProperty.X,
              HomePieceOfFurniture.SortableProperty.Y,
              HomePieceOfFurniture.SortableProperty.ELEVATION,
              HomePieceOfFurniture.SortableProperty.ANGLE,
              HomePieceOfFurniture.SortableProperty.COLOR,
              HomePieceOfFurniture.SortableProperty.MOVABLE,
              HomePieceOfFurniture.SortableProperty.DOOR_OR_WINDOW,
              HomePieceOfFurniture.SortableProperty.VISIBLE}); 
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
      new HomeFurnitureController(this.home, this.preferences, this.undoSupport);
    }
  }
  
  /**
   * Displays the wizard that helps to import furniture to home. 
   */
  public void importFurniture() {
    new ImportedFurnitureWizardController(this.home, this.preferences, this.contentManager, this.undoSupport);
  }
  
  /**
   * Displays the wizard that helps to import furniture to home with a
   * given model name. 
   */
  public void importFurniture(String modelName) {
    new ImportedFurnitureWizardController(
        this.home, modelName, this.preferences, this.contentManager, this.undoSupport);
  }
  
  /**
   * Controls the alignment of selected furniture on top of the first selected piece.
   */
  public void alignSelectedFurnitureOnTop() {
    final List<HomePieceOfFurniture> selectedFurniture = 
        Home.getFurnitureSubList(this.home.getSelectedItems());
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
            return resource.getString("undoAlignName");
          }
        };
        this.undoSupport.postEdit(undoableEdit);
      }
    }
  }

  private void doAlignFurnitureOnTop(AlignedPieceOfFurniture [] alignedFurniture, 
                                     HomePieceOfFurniture leadPiece) {
    float minYLeadPiece = getMinY(leadPiece);
    for (AlignedPieceOfFurniture alignedPiece : alignedFurniture) {
      HomePieceOfFurniture piece = alignedPiece.getPieceOfFurniture();
      float minY = getMinY(piece);
      this.home.setPieceOfFurnitureLocation(piece, piece.getX(), 
          piece.getY() + minYLeadPiece - minY);
    }
  }

  private void undoAlignFurniture(AlignedPieceOfFurniture [] alignedFurniture,
                                  boolean alignX) {
    for (AlignedPieceOfFurniture alignedPiece : alignedFurniture) {
      HomePieceOfFurniture piece = alignedPiece.getPieceOfFurniture();
      if (alignX) {
        this.home.setPieceOfFurnitureLocation(piece, alignedPiece.getXOrY(), 
            piece.getY());
      } else {
        this.home.setPieceOfFurnitureLocation(piece, piece.getX(),
            alignedPiece.getXOrY());
      }
    }
  }
  
  /**
   * Controls the alignment of selected furniture on bottom of the first selected piece.
   */
  public void alignSelectedFurnitureOnBottom() {
    final List<HomePieceOfFurniture> selectedFurniture = 
        Home.getFurnitureSubList(this.home.getSelectedItems());
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
            return resource.getString("undoAlignName");
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
      this.home.setPieceOfFurnitureLocation(piece, piece.getX(), 
          piece.getY() + maxYLeadPiece - maxY);
    }
  }

  /**
   * Controls the alignment of selected furniture on left of the first selected piece.
   */
  public void alignSelectedFurnitureOnLeft() {
    final List<HomePieceOfFurniture> selectedFurniture = 
        Home.getFurnitureSubList(this.home.getSelectedItems());
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
            return resource.getString("undoAlignName");
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
      this.home.setPieceOfFurnitureLocation(piece, 
          piece.getX() + minXLeadPiece - minX, piece.getY());
    }
  }

  /**
   * Controls the alignment of selected furniture on right of the first selected piece.
   */
  public void alignSelectedFurnitureOnRight() {
    final List<HomePieceOfFurniture> selectedFurniture = 
        Home.getFurnitureSubList(this.home.getSelectedItems());
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
            return resource.getString("undoAlignName");
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
      this.home.setPieceOfFurnitureLocation(piece, 
          piece.getX() + maxXLeadPiece - maxX, piece.getY());
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
