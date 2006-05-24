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
import com.eteks.sweethome3d.model.PieceOfFurniture;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * A MVC controller for the furniture table.
 * @author Emmanuel Puybaret
 */
public class FurnitureController {
  private FurnitureTable             furnitureView;
  private Home                       home;
  private UndoableEditSupport        undoSupport;
  private ResourceBundle             viewResource;

  /**
   * Creates the controller of home furniture view. 
   * @param home        the home edited by this controller and its view
   * @param preferences the preferences of the application
   * @param undoSupport undo support to post changes on home by this controller
   */
  public FurnitureController(Home home, UserPreferences preferences, UndoableEditSupport undoSupport) {
    this.home = home;
    this.undoSupport = undoSupport;
    this.viewResource  = ResourceBundle.getBundle(FurnitureTable.class.getName()); 
    this.furnitureView = new FurnitureTable(this, home, preferences);
  }

  /**
   * Returns the view associated with this controller.
   */
  public JComponent getView() {
    return this.furnitureView;
  }

  /**
   * Controls new furniture added to home. Once added the furniture will be selected in view 
   * and undo support will receive a new undoable edit.
   * @param furniture the furniture to add.
   */
  public void addFurniture(List<? extends PieceOfFurniture> furniture) {
    final List<HomePieceOfFurniture> oldSelection = this.furnitureView.getSelectedFurniture();
    // Create the list of HomePieceOfFurniture instances that will be added to home
    final List<HomePieceOfFurniture> newFurniture = new ArrayList<HomePieceOfFurniture> (furniture.size());
    final int [] furnitureIndex = new int [furniture.size()];
    int endIndex = home.getFurniture().size();
    for (int i = 0; i < furnitureIndex.length; i++) {
      newFurniture.add(new HomePieceOfFurniture(furniture.get(i)));
      furnitureIndex [i] = endIndex++;
    }

    doAddFurniture(newFurniture, furnitureIndex);
    UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
      @Override
      public void undo() throws CannotUndoException {
        super.undo();
        doDeleteFurniture(newFurniture);
        selectAndShowFurniture(oldSelection);        
      }
      
      @Override
      public void redo() throws CannotRedoException {
        super.redo();
        doAddFurniture(newFurniture, furnitureIndex);
      }      

      @Override
      public String getPresentationName() {
        return viewResource.getString("undoAddFurnitureName");
      }      
    };
    this.undoSupport.postEdit(undoableEdit);
  }
  
  private void doAddFurniture(List<HomePieceOfFurniture> furniture,
                              int [] furnitureIndex) {
    for (int i = 0; i < furnitureIndex.length; i++) {
      this.home.add(furniture.get(i), furnitureIndex [i]);
    }
    selectAndShowFurniture(furniture);
  }
 
  private void selectAndShowFurniture(List<HomePieceOfFurniture> furniture) {
    this.furnitureView.setSelectedFurniture(furniture);
    this.furnitureView.ensureFurnitureIsVisible(furniture);
  }

  /**
   * Controls the deletion of the current selected furniture in home.
   * Once the selected furniture is deleted, undo support will receive a new undoable edit.
   */
  public void deleteFurniture() {
    final List<HomePieceOfFurniture> selectedFurniture = this.furnitureView.getSelectedFurniture();
    List<HomePieceOfFurniture> homeFurniture = this.home.getFurniture();
    // Sort the selected furniture in the ascending order of their index in home
    Map<Integer, HomePieceOfFurniture> sortedMap = new TreeMap<Integer, HomePieceOfFurniture>();
    for (HomePieceOfFurniture piece : selectedFurniture) {
      sortedMap.put(homeFurniture.indexOf(piece), piece);
    }
    final List<HomePieceOfFurniture> furniture = 
      new ArrayList<HomePieceOfFurniture>(sortedMap.values());
    final int [] furnitureIndex = new int [furniture.size()];
    int i = 0;
    for (int index : sortedMap.keySet()) {
      furnitureIndex [i++] = index;
    }

    doDeleteFurniture(furniture);
    UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
      @Override
      public void undo() throws CannotUndoException {
        super.undo();
        doAddFurniture(furniture, furnitureIndex);
      }
      
      @Override
      public void redo() throws CannotRedoException {
        super.redo();
        doDeleteFurniture(furniture);
      }

      @Override
      public String getPresentationName() {
        return viewResource.getString("undoDeleteFurnitureName");
      }      
    };
    this.undoSupport.postEdit(undoableEdit);
  }
  
  private void doDeleteFurniture(List<HomePieceOfFurniture> furniture) {
    for (HomePieceOfFurniture piece : furniture) {
      home.delete(piece);
    }
  }

  /**
   * Controls the sort of the furniture in home. If home furniture isn't sorted
   * or is sorted on an other column, it will be sorted on the given
   * <code>property</code> in ascending order. If home furniture is already
   * sorted on the given <code>property<code>, it will be sorted in descending 
   * order, if the sort is in ascending order, otherwise it won't be sorted at all 
   * and home furniture will be listed in insertion order. 
    * @param property  the furniture property on which the view wants
   *          to sort the furniture it displays.
   */
//  public void sortFurniture(String property) {
//    // Compute sort algorithm described in javadoc
//    final String  oldProperty    = this.furnitureView.getSortedProperty();
//    final boolean oldAscending = this.furnitureView.isAscendingSort(); 
//    boolean ascending = true;
//    if (property.equals(oldProperty)) {
//      if (oldAscending) {
//        ascending = false;
//      } else {
//        property = null;
//      }
//    }
//    this.furnitureView.setSortedProperty(property);
//    this.furnitureView.setAscendingSort(ascending);
//  }
}
