/*
 * HomeController.java 15 mai 2006
 * 
 * Copyright (c) 2006 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights
 * Reserved.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.eteks.sweethome3d.viewcontroller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.PieceOfFurniture;
import com.eteks.sweethome3d.model.SelectionEvent;
import com.eteks.sweethome3d.model.SelectionListener;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * A MVC controller for the home view.
 * @author Emmanuel Puybaret
 */
public class HomeController {
  private Home                home;
  private UserPreferences     preferences;
  private View                homeView;
  private UndoableEditSupport undoSupport;
  private UndoManager         undoManager;
  private ResourceBundle      resource;

  /**
   * Creates the controller of home view.
   * @param viewFactory factory able to create views
   * @param home the home edited by this controller and its view.
   * @param preferences the preferences of the application.
   */
  public HomeController(ViewFactory viewFactory, Home home,
                        UserPreferences preferences) {
    this.home = home;
    this.preferences = preferences;
    this.undoSupport = new UndoableEditSupport();
    this.undoManager = new UndoManager();
    this.undoSupport.addUndoableEditListener(this.undoManager);
    this.resource = ResourceBundle.getBundle(
        HomeController.class.getName());
    this.homeView = viewFactory.createHomeView(home, preferences, this);
    addListeners();
  }

  /**
   * Returns the view associated with this controller.
   */
  public View getView() {
    return this.homeView;
  }

  /**
   * Adds listeners that updates the enabled / disabled state of actions.
   */
  private void addListeners() {
    addCatalogSelectionListener();
    addHomeSelectionListener();
    addUndoSupportListener();
  }

  /**
   * Adds a selection listener on catalog that enables / disables Add Furniture action.
   */
  private void addCatalogSelectionListener() {
    this.preferences.getCatalog().addSelectionListener(
      new SelectionListener() {
        public void selectionChanged(SelectionEvent ev) {
          ((HomeView)getView()).setEnabled(
              HomeView.ActionType.ADD_HOME_FURNITURE,
              !ev.getSelectedItems().isEmpty());
        }
      });
  }

  /**
   *  Adds a selection listener on home that enables / disables Delete Furniture action.
   */
  private void addHomeSelectionListener() {
    this.home.addSelectionListener(new SelectionListener() {
      public void selectionChanged(SelectionEvent ev) {
        // Search if selection contains at least one piece
        boolean selectionContainsFurniture = false;
        for (Object item : ev.getSelectedItems()) {
          if (item instanceof HomePieceOfFurniture) {
            selectionContainsFurniture = true;
            break;
          }
        }
        ((HomeView)getView()).setEnabled(
            HomeView.ActionType.DELETE_HOME_FURNITURE,
            selectionContainsFurniture);
      }
    });
  }

  /**
   * Adds undoable edit listener on undo support that enables Undo action.
   */
  private void addUndoSupportListener() {
    this.undoSupport.addUndoableEditListener(
      new UndoableEditListener () {
        public void undoableEditHappened(UndoableEditEvent ev) {
          HomeView view = ((HomeView)getView());
          view.setEnabled(HomeView.ActionType.UNDO, true);
          view.setEnabled(HomeView.ActionType.REDO, false);
          view.setUndoRedoName(ev.getEdit().getUndoPresentationName(), null);
        }
      });
  }

  /**
   * Adds the selected furniture in catalog to home and selects it.  
   */
  public void addHomeFurniture() {
    addHomeFurniture(this.preferences.getCatalog().getSelectedFurniture());
  }

  /**
   * Controls new furniture added to home. Once added the furniture will be selected in view 
   * and undo support will receive a new undoable edit.
   * @param furniture the furniture to add.
   */
  private void addHomeFurniture(List<? extends PieceOfFurniture> furniture) {
    final List<Object> oldSelection = this.home.getSelectedItems();
    // Create HomePieceOfFurniture instances that will be added to home
    final HomePieceOfFurniture [] newFurniture = 
      new HomePieceOfFurniture [furniture.size()];
    final int [] furnitureIndex = new int [furniture.size()];
    int endIndex = home.getFurniture().size();
    for (int i = 0; i < furnitureIndex.length; i++) {
      newFurniture [i] = new HomePieceOfFurniture(furniture.get(i));
      furnitureIndex [i] = endIndex++;
    }

    doAddFurniture(newFurniture, furnitureIndex);
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
        return resource.getString("undoAddHomeFurnitureName");
      }      
    };
    this.undoSupport.postEdit(undoableEdit);
  }
  
  private void doAddFurniture(HomePieceOfFurniture [] furniture,
                              int [] furnitureIndex) {
    for (int i = 0; i < furnitureIndex.length; i++) {
      this.home.addPieceOfFurniture(furniture [i], furnitureIndex [i]);
    }
    home.setSelectedItems(Arrays.asList(furniture));
  }
 
  /**
   * Controls the deletion of the current selected furniture in home.
   * Once the selected furniture is deleted, undo support will receive a new undoable edit.
   */
  public void deleteHomeFurniture() {
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
    final HomePieceOfFurniture [] furniture = 
      sortedMap.values().toArray(new HomePieceOfFurniture [sortedMap.size()]);
    final int [] furnitureIndex = new int [furniture.length];
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
        home.setSelectedItems(Arrays.asList(furniture));
        doDeleteFurniture(furniture);
      }

      @Override
      public String getPresentationName() {
        return resource.getString("undoDeleteName");
      }      
    };
    this.undoSupport.postEdit(undoableEdit);
  }
  
  private void doDeleteFurniture(HomePieceOfFurniture [] furniture) {
    for (HomePieceOfFurniture piece : furniture) {
      this.home.deletePieceOfFurniture(piece);
    }
  }

  /**
   * Undoes last operation.
   */
  public void undo() {
    this.undoManager.undo();
    HomeView view = ((HomeView)getView());
    boolean moreUndo = this.undoManager.canUndo();
    view.setEnabled(HomeView.ActionType.UNDO, moreUndo);
    view.setEnabled(HomeView.ActionType.REDO, true);
    if (moreUndo) {
      view.setUndoRedoName(this.undoManager.getUndoPresentationName(),
          this.undoManager.getRedoPresentationName());
    } else {
      view.setUndoRedoName(null, this.undoManager.getRedoPresentationName());
    }
  }
  
  /**
   * Redoes last undone operation.
   */
  public void redo() {
    this.undoManager.redo();
    HomeView view = ((HomeView)getView());
    boolean moreRedo = this.undoManager.canRedo();
    view.setEnabled(HomeView.ActionType.UNDO, true);
    view.setEnabled(HomeView.ActionType.REDO, moreRedo);
    if (moreRedo) {
      view.setUndoRedoName(this.undoManager.getUndoPresentationName(),
          this.undoManager.getRedoPresentationName());
    } else {
      view.setUndoRedoName(this.undoManager.getUndoPresentationName(), null);
    }
  }
}
