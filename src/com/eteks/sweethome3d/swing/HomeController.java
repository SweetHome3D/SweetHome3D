/*
 * HomeController.java 15 mai 2006
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

import javax.swing.JComponent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.SelectionEvent;
import com.eteks.sweethome3d.model.SelectionListener;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * A MVC controller for the home view.
 * @author Emmanuel Puybaret
 */
public class HomeController  {
  private Home                home;
  private UserPreferences     preferences;
  private JComponent          homeView; 
  private CatalogController   catalogController;
  private FurnitureController furnitureController;
  private PlanController      planController;
  private UndoableEditSupport undoSupport;
  private UndoManager         undoManager;

  /**
   * Creates the controller of home view. 
   * @param home        the home edited by this controller and its view.
   * @param preferences the preferences of the application.
   */
  public HomeController(Home home, UserPreferences preferences) {
    this.home = home;
    this.preferences = preferences;
    this.undoSupport = new UndoableEditSupport();
    this.undoManager = new UndoManager();
    this.undoSupport.addUndoableEditListener(this.undoManager);
    
    this.catalogController   = new CatalogController(
        preferences.getCatalog());
    this.furnitureController = new FurnitureController(
        home, preferences, this.undoSupport);
    this.planController = new PlanController(
        home, preferences, undoSupport);
    
    this.homeView = new HomePane(home, preferences, this);
    addListeners();
    enableDefaultActions();
  }

  /**
   * Returns the view associated with this controller.
   */
  public JComponent getView() {
    return this.homeView;
  }

  /**
   * Returns the catalog controller managed by this controller.
   */
  public CatalogController getCatalogController() {
    return this.catalogController;
  }

  /**
   * Returns the furniture controller managed by this controller.
   */
  public FurnitureController getFurnitureController() {
    return this.furnitureController;
  }

  /**
   * Returns the controller of home plan.
   */
  public PlanController getPlanController() {
    return this.planController;
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
          enableActionsOnSelection();
        }
      });
  }

  /**
   *  Adds a selection listener on home that enables / disables Delete Furniture action.
   */
  private void addHomeSelectionListener() {
    this.home.addSelectionListener(new SelectionListener() {
      public void selectionChanged(SelectionEvent ev) {
        enableActionsOnSelection();
      }
    });
  }

  /**
   * Enables action bound to selection. 
   */
  private void enableActionsOnSelection() {
    boolean wallCreationMode =  
        getPlanController().getMode() == PlanController.Mode.WALL_CREATION;
    
    // Search if selection contains at least one piece
    List selectedItems = this.home.getSelectedItems();
    boolean selectionContainsFurniture = false;
    if (!wallCreationMode)
      for (Object item : selectedItems) {
        if (item instanceof HomePieceOfFurniture) {
          selectionContainsFurniture = true;
          break;
        }
      }
    // In creation mode al actions bound to selection are disabled
    HomePane view = ((HomePane)getView());
    view.setEnabled(HomePane.ActionType.DELETE_HOME_FURNITURE,
        !wallCreationMode && selectionContainsFurniture);
    view.setEnabled(HomePane.ActionType.DELETE_SELECTION,
        !wallCreationMode && !selectedItems.isEmpty());
    view.setEnabled(HomePane.ActionType.ADD_HOME_FURNITURE,
        !wallCreationMode && !this.preferences.getCatalog().getSelectedFurniture().isEmpty());
  }

  /**
   * Adds undoable edit listener on undo support that enables Undo action.
   */
  private void addUndoSupportListener() {
    this.undoSupport.addUndoableEditListener(
      new UndoableEditListener () {
        public void undoableEditHappened(UndoableEditEvent ev) {
          HomePane view = ((HomePane)getView());
          view.setEnabled(HomePane.ActionType.UNDO, 
              planController.getMode() != PlanController.Mode.WALL_CREATION);
          view.setEnabled(HomePane.ActionType.REDO, false);
          view.setUndoRedoName(ev.getEdit().getUndoPresentationName(), null);
        }
      });
  }

  /**
   * Enables actions at controller instantiation. 
   */
  private void enableDefaultActions() {
    ((HomePane)getView()).setEnabled(HomePane.ActionType.WALL_CREATION, true);
  }

  /**
   * Adds the selected furniture in catalog to home and selects it.  
   */
  public void addHomeFurniture() {
    List<CatalogPieceOfFurniture> selectedFurniture = 
      this.preferences.getCatalog().getSelectedFurniture();
    if (!selectedFurniture.isEmpty()) {
      List<HomePieceOfFurniture> newFurniture = 
          new ArrayList<HomePieceOfFurniture>();
      for (CatalogPieceOfFurniture piece : selectedFurniture) {
        newFurniture.add(new HomePieceOfFurniture(piece));
      }
      // Add newFurniture to home with furnitureController
      getFurnitureController().addFurniture(newFurniture);
    }
  }

  /**
   * Undoes last operation.
   */
  public void undo() {
    this.undoManager.undo();
    HomePane view = ((HomePane)getView());
    boolean moreUndo = this.undoManager.canUndo();
    view.setEnabled(HomePane.ActionType.UNDO, moreUndo);
    view.setEnabled(HomePane.ActionType.REDO, true);
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
    HomePane view = ((HomePane)getView());
    boolean moreRedo = this.undoManager.canRedo();
    view.setEnabled(HomePane.ActionType.UNDO, true);
    view.setEnabled(HomePane.ActionType.REDO, moreRedo);
    if (moreRedo) {
      view.setUndoRedoName(this.undoManager.getUndoPresentationName(),
          this.undoManager.getRedoPresentationName());
    } else {
      view.setUndoRedoName(this.undoManager.getUndoPresentationName(), null);
    }
  }

  /**
   * Sets wall creation mode in plan controller, 
   * and disables forbidden actions in this mode.  
   */
  public void setWallCreationMode() {
    getPlanController().setMode(PlanController.Mode.WALL_CREATION);
    enableActionsOnSelection();
    HomePane view = ((HomePane)getView());
    view.setEnabled(HomePane.ActionType.UNDO, false);
    view.setEnabled(HomePane.ActionType.REDO, false);
  }

  /**
   * Sets wall creation mode in plan controller, 
   * and enables authorized actions in this mode.  
   */
  public void setSelectionMode() {
    getPlanController().setMode(PlanController.Mode.SELECTION);
    enableActionsOnSelection();
    HomePane view = ((HomePane)getView());
    view.setEnabled(HomePane.ActionType.UNDO, this.undoManager.canUndo());
    view.setEnabled(HomePane.ActionType.REDO, this.undoManager.canRedo());
  }
}
