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
import java.util.ResourceBundle;

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
  private Home                       home;
  private UndoableEditSupport        undoSupport;
  private FurnitureTable             furnitureView;
  private ResourceBundle             viewResource;
  private List<HomePieceOfFurniture> selectedFurniture;

  /**
   * Creates the controller of home furniture view. 
   * @param home        the home edited by this controller and its view
   * @param preferences the preferences of the application
   * @param undoSupport undo support to post changes on home by this controller
   */
  public FurnitureController(Home home, UserPreferences preferences, UndoableEditSupport undoSupport) {
    this.home = home;
    this.undoSupport = undoSupport;
    this.furnitureView = new FurnitureTable(this, home, preferences);
    this.viewResource  = ResourceBundle.getBundle(this.furnitureView.getClass().getName()); 
    this.selectedFurniture = new ArrayList<HomePieceOfFurniture>();
  }

  /**
   * Returns the view associated with this controller.
   */
  public JComponent getView() {
    return this.furnitureView;
  }

  /**
   * Controls the deletion of the current selected furniture in home.
   * Once the selected furniture is deleted, undo support will receive a new undoable edit.
   */
  public void deleteFurniture() {
    final List<HomePieceOfFurniture> furniture = furnitureView.getSelectedFurniture();
    doDeleteFurniture();
    UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
      @Override
      public void undo() throws CannotUndoException {
        super.undo();
        doAddFurniture(furniture);
      }
      
      @Override
      public void redo() throws CannotRedoException {
        super.redo();
        doDeleteFurniture();
      }

      @Override
      public String getPresentationName() {
        return viewResource.getString("undoRedoDeleteFurniture");
      }      
    };
    this.undoSupport.postEdit(undoableEdit);
  }
  
  private void doDeleteFurniture() {
    for (HomePieceOfFurniture piece : furnitureView.getSelectedFurniture()) {
      home.delete(piece);
    }
  }

  /**
   * Controls new furniture added to home. Once added the furniture will be selected in view 
   * and undo support will receive a new undoable edit.
   * @param furniture the furniture to add.
   */
  public void addFurniture(List<? extends PieceOfFurniture> furniture) {
    // Create the list of HomePieceOfFurniture instances that will be added
    final List<HomePieceOfFurniture> newFurniture = new ArrayList<HomePieceOfFurniture> (furniture.size());
    for (PieceOfFurniture piece : furniture) {
      newFurniture.add(new HomePieceOfFurniture(piece));
    }
    doAddFurniture(newFurniture);
    UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
      @Override
      public void undo() throws CannotUndoException {
        super.undo();
        doDeleteFurniture();
      }
      
      @Override
      public void redo() throws CannotRedoException {
        super.redo();
        doAddFurniture(newFurniture);
      }      

      @Override
      public String getPresentationName() {
        return viewResource.getString("undoRedoAddFurniture");
      }      
    };
    this.undoSupport.postEdit(undoableEdit);
  }
  
  private void doAddFurniture(List<HomePieceOfFurniture> furniture) {
    int endIndex = home.getFurniture().size();
    for (HomePieceOfFurniture piece : furniture) {
      this.home.add(piece, endIndex++);
    }
    this.furnitureView.setSelectedFurniture(furniture);
  }
    
  /**
   * Controls the sort of the furniture in home. If home furniture isn't sorted
   * or is sorted on an other property, it will be sorted on the given
   * <code>property</code> in ascending order. If home furniture is already
   * sorted on the given <code>property<code>, it will be sorted in descending 
   * order, if the sort is in ascending order, otherwise it won't be sorted at all 
   * and home furniture will be listed in insertion order. 
   * Once sorted undo support will receive a new undoable insignificant edit.
   * @param property the property of <code>Home</code> on which the view wants
   *          to sort the furniture it displays.
   */
  public void sortFurniture(String property) {
    // Refuse sort on icon property
    if (property.equals("icon"))
      return;
    // Compute sort algorithm described in javadoc
    final String  oldProperty   = home.getSortedProperty();
    final boolean oldAscending  = home.isAscendingSort(); 
    boolean ascending = true;
    if (property.equals(oldProperty)) {
      if (oldAscending) {
        ascending = false;
      } else {
        property = null;
      }
    }
    final String newProperty = property;
    final boolean newAscending = ascending;
    doSortFurniture(property, ascending);
    UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
      @Override
      public void undo() throws CannotUndoException {
        super.undo();
        doSortFurniture(oldProperty, oldAscending);
      }
      
      @Override
      public void redo() throws CannotRedoException {
        super.redo();
        doSortFurniture(newProperty, newAscending);
      }

      // Let's tag sort as insignificant edit
      @Override
      public boolean isSignificant() {
        return false;
      }      
    };
    this.undoSupport.postEdit(undoableEdit);
  }

  private void doSortFurniture(String property, boolean ascending) {
    home.setSortedProperty(property);
    home.setAscendingSort(ascending);
  }

  /**
   * Records in undo support the current selected furniture in view.
   */
  public void recordSelectedFurniture() {
    // Record new and old selection
    final List<HomePieceOfFurniture> newSelectedFurniture = furnitureView.getSelectedFurniture();
    final List<HomePieceOfFurniture> oldSelectedFurniture = this.selectedFurniture;
    this.selectedFurniture = newSelectedFurniture;
    UndoableEdit undoableEdit = new AbstractUndoableEdit() {      
      @Override
      public void undo() throws CannotUndoException {
        super.undo();
        doSelectFurniture(oldSelectedFurniture);
      }
      
      @Override
      public void redo() throws CannotRedoException {
        super.redo();
        doSelectFurniture(newSelectedFurniture);
      }

      // Let's tag selection as insignificant edit
      @Override
      public boolean isSignificant() {
        return false;
      }      
    };
    this.undoSupport.postEdit(undoableEdit);
  }

  private void doSelectFurniture(List<HomePieceOfFurniture> furniture) {
    this.furnitureView.setSelectedFurniture(furniture);
  }
}
