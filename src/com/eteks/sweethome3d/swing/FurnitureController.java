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

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * A MVC controller for the furniture table.
 * @author Emmanuel Puybaret
 */
public class FurnitureController {
  private Home                home;
  private JComponent          furnitureView;
  private ResourceBundle      resource;
  private UndoableEditSupport undoSupport;
  private UserPreferences     preferences;

  /**
   * Creates the controller of home furniture view.
   * @param home the home edited by this controller and its view
   * @param preferences the preferences of the application
   */
  public FurnitureController(Home home, 
                             UserPreferences preferences) {
    this(home, preferences, null); 
  }

  /**
   * Creates the controller of home furniture view with undo support.
   */
  public FurnitureController(Home home, 
                             UserPreferences preferences, 
                             UndoableEditSupport undoSupport) {
    this.home = home;
    this.undoSupport = undoSupport;
    this.preferences = preferences;
    this.resource    = ResourceBundle.getBundle(
        FurnitureController.class.getName());
    this.furnitureView = 
        new FurnitureTable(home, preferences, this); 
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
    List<HomePieceOfFurniture> homeFurniture = 
        this.home.getFurniture(); 
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
   * Controls the modification of selected furniture.
   */
  public void modifySelection() {
    final List<Object> oldSelection = this.home.getSelectedItems(); 
    List<HomePieceOfFurniture> selectedFurniture = 
        Home.getFurnitureSubList(oldSelection);
    if (!selectedFurniture.isEmpty()) {
      // Search the common properties among selected furniture
      String editedName = selectedFurniture.get(0).getName();
      if (editedName != null) {
        for (int i = 1; i < selectedFurniture.size(); i++) {
          if (!editedName.equals(selectedFurniture.get(i).getName())) {
            editedName = null;
            break;
          }
        }
      }
      Float editedX = selectedFurniture.get(0).getX();
      for (int i = 1; i < selectedFurniture.size(); i++) {
        if (editedX != selectedFurniture.get(i).getX()) {
          editedX = null;
          break;
        }
      }
      Float editedY = selectedFurniture.get(0).getY();
      for (int i = 1; i < selectedFurniture.size(); i++) {
        if (editedY != selectedFurniture.get(i).getY()) {
          editedY = null;
          break;
        }
      }
      Float editedAngle = selectedFurniture.get(0).getAngle();
      for (int i = 1; i < selectedFurniture.size(); i++) {
        if (editedAngle != selectedFurniture.get(i).getAngle()) {
          editedAngle = null;
          break;
        }
      }
      Float editedWidth = selectedFurniture.get(0).getWidth();
      for (int i = 1; i < selectedFurniture.size(); i++) {
        if (editedWidth != selectedFurniture.get(i).getWidth()) {
          editedWidth = null;
          break;
        }
      }
      Float editedDepth = selectedFurniture.get(0).getDepth();
      for (int i = 1; i < selectedFurniture.size(); i++) {
        if (editedDepth != selectedFurniture.get(i).getDepth()) {
          editedDepth = null;
          break;
        }
      }
      Float editedHeight = selectedFurniture.get(0).getHeight();
      for (int i = 1; i < selectedFurniture.size(); i++) {
        if (editedHeight != selectedFurniture.get(i).getHeight()) {
          editedHeight = null;
          break;
        }
      }
      Integer editedColor = selectedFurniture.get(0).getColor();
      if (editedColor != null) {
        for (int i = 1; i < selectedFurniture.size(); i++) {
          if (!editedColor.equals(selectedFurniture.get(i).getColor())) {
            editedColor = null;
            break;
          }
        }
      }
      Boolean editedVisible = selectedFurniture.get(0).isVisible();
      for (int i = 1; i < selectedFurniture.size(); i++) {
        if (editedVisible != selectedFurniture.get(i).isVisible()) {
          editedVisible = null;
          break;
        }
      }
      
      // Create a furniture panel to edit furniture properties
      HomeFurniturePanel furniturePanel = new HomeFurniturePanel(this.preferences);
      furniturePanel.setFurnitureName(editedName);
      furniturePanel.setFurnitureLocation(editedX, editedY);
      furniturePanel.setFurnitureAngle(editedAngle);
      furniturePanel.setFurnitureDimension(editedWidth, editedDepth, editedHeight);
      furniturePanel.setFurnitureColor(editedColor);
      furniturePanel.setFurnitureVisible(editedVisible);
      // Display furniture panel in a dialog box
      if (furniturePanel.showDialog(getView())) {
        final String name = furniturePanel.getFurnitureName();
        final Float x = furniturePanel.getFurnitureX();
        final Float y = furniturePanel.getFurnitureY();
        final Float angle = furniturePanel.getFurnitureAngle();
        final Float width = furniturePanel.getFurnitureWidth();
        final Float depth = furniturePanel.getFurnitureDepth();
        final Float height = furniturePanel.getFurnitureHeight();
        final Integer color = furniturePanel.getFurnitureColor();
        final Boolean visible = furniturePanel.isFurnitureVisible();
        
        // Create an array of modified furniture with their current properties values
        final ModifiedPieceOfFurniture [] modifiedFurniture = 
            new ModifiedPieceOfFurniture [selectedFurniture.size()]; 
        for (int i = 0; i < modifiedFurniture.length; i++) {
          modifiedFurniture [i] = new ModifiedPieceOfFurniture(selectedFurniture.get(i));
        }
        // Apply modification
        doModifyFurniture(modifiedFurniture, 
            name, width, depth, height, x, y, angle, color, visible); 
        if (this.undoSupport != null) {
          UndoableEdit undoableEdit = new AbstractUndoableEdit() {
            @Override
            public void undo() throws CannotUndoException {
              super.undo();
              doRestoreFurniture(modifiedFurniture); 
              home.setSelectedItems(oldSelection); 
            }
            
            @Override
            public void redo() throws CannotRedoException {
              super.redo();
              doModifyFurniture(modifiedFurniture, 
                  name, width, depth, height, x, y, angle, color, visible); 
              home.setSelectedItems(oldSelection); 
            }
            
            @Override
            public String getPresentationName() {
              return resource.getString("undoModifyFurnitureName");
            }
          };
          this.undoSupport.postEdit(undoableEdit);
        }
      }
    }
  }
  
  /**
   * Modifies furniture properties with the values in parameter.
   */
  private void doModifyFurniture(ModifiedPieceOfFurniture [] modifiedFurniture, 
                                 String name, Float width, Float depth, Float height, 
                                 Float x, Float y, Float angle, Integer color, Boolean visible) {
    for (ModifiedPieceOfFurniture modifiedPiece : modifiedFurniture) {
      HomePieceOfFurniture piece = modifiedPiece.getPieceOfFurniture();
      this.home.setPieceOfFurnitureName(piece, 
          name != null ? name : piece.getName());
      this.home.setPieceOfFurnitureLocation(piece, 
          x != null ? x.floatValue() : piece.getX(), 
          y != null ? y.floatValue() : piece.getY());
      this.home.setPieceOfFurnitureAngle(piece, 
          angle != null ? angle.floatValue() : piece.getAngle());
      this.home.setPieceOfFurnitureDimension(piece, 
          width != null ? width.floatValue() : piece.getWidth(), 
          depth != null ? depth.floatValue() : piece.getDepth(), 
          height != null ? height.floatValue() : piece.getHeight());
      this.home.setPieceOfFurnitureColor(piece, 
          color != null ? color : piece.getColor());
      this.home.setPieceOfFurnitureVisible(piece, 
          visible != null ? visible.booleanValue() : piece.isVisible());
    }
  }

  /**
   * Restores furniture properties from the values stored in <code>modifiedFurnitures</code>.
   */
  private void doRestoreFurniture(ModifiedPieceOfFurniture [] modifiedFurniture) {
    for (ModifiedPieceOfFurniture modifiedPiece : modifiedFurniture) {
      HomePieceOfFurniture piece = modifiedPiece.getPieceOfFurniture();
      this.home.setPieceOfFurnitureName(piece, modifiedPiece.getName());
      this.home.setPieceOfFurnitureLocation(piece, modifiedPiece.getX(), modifiedPiece.getY());
      this.home.setPieceOfFurnitureAngle(piece, modifiedPiece.getAngle());
      this.home.setPieceOfFurnitureDimension(piece, 
          modifiedPiece.getWidth(), modifiedPiece.getDepth(), modifiedPiece.getHeight());
      this.home.setPieceOfFurnitureColor(piece, modifiedPiece.getColor());
      this.home.setPieceOfFurnitureVisible(piece, modifiedPiece.isVisible());
    }
  }

  /**
   * Stores the current properties values of a modified piece of furniture.
   */
  private static class ModifiedPieceOfFurniture {
    private final HomePieceOfFurniture piece;
    private final String  name;
    private final float   x;
    private final float   y;
    private final float   angle;
    private final float   width;
    private final float   depth;
    private final float   height;
    private final Integer color;
    private final boolean visible;

    public ModifiedPieceOfFurniture(HomePieceOfFurniture piece) {
      this.piece = piece;
      this.name = piece.getName();
      this.x = piece.getX();
      this.y = piece.getY();
      this.angle = piece.getAngle();
      this.width = piece.getWidth();
      this.depth = piece.getDepth();
      this.height = piece.getHeight();
      this.color = piece.getColor();
      this.visible = piece.isVisible();
    }

    public HomePieceOfFurniture getPieceOfFurniture() {
      return this.piece;
    }
    
    public String getName() {
      return this.name;
    }

    public float getDepth() {
      return this.depth;
    }

    public float getHeight() {
      return this.height;
    }

    public float getWidth() {
      return this.width;
    }

    public Integer getColor() {
      return this.color;
    }

    public boolean isVisible() {
      return this.visible;
    }
    
    public float getX() {
      return this.x;
    }

    public float getY() {
      return this.y;
    }

    public float getAngle() {
      return this.angle;
    }
  }
}
