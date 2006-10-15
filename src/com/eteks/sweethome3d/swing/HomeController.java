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
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeApplication;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.RecorderException;
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
  private HomeApplication     application;
  private JComponent          homeView; 
  private CatalogController   catalogController;
  private FurnitureController furnitureController;
  private PlanController      planController;
  private UndoableEditSupport undoSupport;
  private UndoManager         undoManager;
  private ResourceBundle      resource;
  private int                 saveUndoLevel; 

  /**
   * Creates the controller of home view. 
   * @param home        the home edited by this controller and its view.
   * @param application the instance of current application.
   */
  public HomeController(Home home, HomeApplication application) {
    this(home, application.getUserPreferences(), application);
  }

  /**
   * Creates the controller of home view. 
   * @param home        the home edited by this controller and its view.
   * @param preferences the preferences of the application.
   */
  public HomeController(Home home, UserPreferences preferences) {
    this(home, preferences, null);
  }

  private HomeController(Home home, UserPreferences preferences, 
                         HomeApplication application) {
    this.home = home;
    this.preferences = preferences;
    this.application = application;
    this.undoSupport = new UndoableEditSupport();
    this.undoManager = new UndoManager();
    this.undoSupport.addUndoableEditListener(this.undoManager);
    this.resource = ResourceBundle.getBundle(
        HomeController.class.getName());
    
    this.catalogController   = new CatalogController(
        preferences.getCatalog());
    this.furnitureController = new FurnitureController(
        home, preferences, this.undoSupport);
    this.planController = new PlanController(
        home, preferences, undoSupport);
    
    this.homeView = new HomePane(home, preferences, this);
    addListeners();
    enableDefaultActions((HomePane)this.homeView);
  }

  /**
   * Enables actions at controller instantiation. 
   */
  private void enableDefaultActions(HomePane homeView) {
    homeView.setEnabled(HomePane.ActionType.NEW_HOME, true);
    homeView.setEnabled(HomePane.ActionType.OPEN, true);
    homeView.setEnabled(HomePane.ActionType.CLOSE, true);
    homeView.setEnabled(HomePane.ActionType.SAVE, true);
    homeView.setEnabled(HomePane.ActionType.SAVE_AS, true);
    homeView.setEnabled(HomePane.ActionType.EXIT, true);
    homeView.setEnabled(HomePane.ActionType.WALL_CREATION, true);
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
          saveUndoLevel++;
          home.setModified(true);
        }
      });
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
    this.saveUndoLevel--;
    this.home.setModified(this.saveUndoLevel != 0);
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
    this.saveUndoLevel++;
    this.home.setModified(this.saveUndoLevel != 0);
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


  /**
   * Creates a new home and adds it to application home list.
   */
  public void newHome() {
    this.application.addHome(
        new Home(this.preferences.getNewHomeWallHeight()));
  }

  /**
   * Opens a home. This method displays an {@link HomePane#showOpenDialog() open dialog} 
   * in view, reads the home from the choosen name and adds it to application home list.
   */
  public void open() {
    final String homeName = ((HomePane)getView()).showOpenDialog();
    if (homeName != null) {
      try {
        Home openedHome = this.application.getHomeRecorder().readHome(homeName); 
        this.application.addHome(openedHome);
      } catch (RecorderException ex) {
        String message = String.format(this.resource.getString("openError"), homeName);
        ((HomePane)getView()).showError(message);
      }
    }
  }

  /**
   * Manages home close operation. If the home managed by this controller is modified,
   * this method will {@link HomePane#confirmSave(String) confirm} 
   * in view whether home should be saved. Once home is actually saved,
   * home is removed from application homes list.
   */
  public void close() {
    boolean willClose = true;
    if (this.home.isModified()) {
      switch (((HomePane)getView()).confirmSave(this.home.getName())) {
        case SAVE   : willClose = save();
                      break;
        case CANCEL : willClose = false;
                      break;
      }  
    }
    if (willClose) {
      this.application.deleteHome(home);
    }
  }

  /**
   * Saves the home managed by this controller. If home name doesn't exist, 
   * this method will act as {@link #saveAs() saveAs} method.
   * @return <code>true</code> if home was saved.
   */
  public boolean save() {
    if (this.home.getName() == null) {
      return saveAs();
    } else {
      return save(this.home.getName());
    }
  }

  /**
   * Saves the home managed by this controller with a different name. 
   * This method displays a {@link HomePane#showSaveDialog(String) save dialog} in   view, 
   * and saves home with the choosen name if any. 
   * If this name already exists, the user will be 
   * {@link HomePane#confirmOverwrite(String) prompted} in view whether 
   * he wants to overwrite this existing name. 
   * @return <code>true</code> if home was saved.
   */
  public boolean saveAs() {
    String newName = ((HomePane)getView()).showSaveDialog(this.home.getName());
    if (newName != null) {
      try {
        if (!this.application.getHomeRecorder().exists(newName)
            || ((HomePane)getView()).confirmOverwrite(newName)) {
          return save(newName);
        } else {
          return saveAs();
        }
      } catch (RecorderException ex) {
        String message = String.format(this.resource.getString("saveError"), newName);
        ((HomePane)getView()).showError(message);
      }
    }
    return false;
  }

  /**
   * Actually saves the home managed by this controller.
   * @return <code>true</code> if home was saved.
   */
  private boolean save(String homeName) {
    try {
      this.application.getHomeRecorder().writeHome(this.home, homeName);
      this.home.setName(homeName);
      this.saveUndoLevel = 0;
      this.home.setModified(false);
      return true;
    } catch (RecorderException ex) {
      String message = String.format(this.resource.getString("saveError"), homeName);
      ((HomePane)getView()).showError(message);
      return false;
    }
  }

  /**
   * Manages application exit. If any home in application homes list is modified,
   * the user will {@link HomePane#confirmExit() prompted} in view whether he wants
   * to discard his modifications.  
   */
  public void exit() {
    for (Home home : this.application.getHomes()) {
      if (home.isModified()) {
        if (((HomePane)getView()).confirmExit()) {
          break;
        } else {
          return;
        }
      }
    }
    // Remove all homes from application
    for (Home home : this.application.getHomes()) {
      this.application.deleteHome(home);
    }
    // Let application decide what to do when there's no more home
  }
}
