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
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
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
  private Home                   home;
  private UserPreferences        preferences;
  private HomeApplication        application;
  private JComponent             homeView;
  private CatalogController      catalogController;
  private FurnitureController    furnitureController;
  private PlanController         planController;
  private UndoableEditSupport    undoSupport;
  private UndoManager            undoManager;
  private ResourceBundle         resource;
  private int                    saveUndoLevel;
  private HomePane.FocusableView focusedView;

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
    List<Object> selectedItems = this.home.getSelectedItems();
    boolean selectionContainsFurniture = false;
    if (!wallCreationMode) {
      selectionContainsFurniture = !Home.getFurnitureSubList(selectedItems).isEmpty();
    }

    List catalogSelectedItems = this.preferences.getCatalog().getSelectedFurniture();    
    HomePane view = ((HomePane)getView());
    if (this.focusedView == null) {
      view.setEnabled(HomePane.ActionType.COPY, false);
      view.setEnabled(HomePane.ActionType.CUT, false);
      view.setEnabled(HomePane.ActionType.DELETE, false);
    } else {
      switch (this.focusedView) {
        case CATALOG :
          view.setEnabled(HomePane.ActionType.COPY,
              !wallCreationMode && !catalogSelectedItems.isEmpty());
          view.setEnabled(HomePane.ActionType.CUT, false);
          view.setEnabled(HomePane.ActionType.DELETE, false);
          break;
        case FURNITURE :
          view.setEnabled(HomePane.ActionType.COPY, selectionContainsFurniture);
          view.setEnabled(HomePane.ActionType.CUT, selectionContainsFurniture);
          view.setEnabled(HomePane.ActionType.DELETE, selectionContainsFurniture);
          break;
        case PLAN :
          boolean copyEnabled = !wallCreationMode && !selectedItems.isEmpty();
          view.setEnabled(HomePane.ActionType.COPY, copyEnabled);
          view.setEnabled(HomePane.ActionType.CUT, copyEnabled);
          view.setEnabled(HomePane.ActionType.DELETE, copyEnabled);
          break;
      }
    }

    // In creation mode all actions bound to selection are disabled
    view.setEnabled(HomePane.ActionType.DELETE_HOME_FURNITURE,
        !wallCreationMode && selectionContainsFurniture);
    view.setEnabled(HomePane.ActionType.DELETE_SELECTION,
        !wallCreationMode && !selectedItems.isEmpty());
    view.setEnabled(HomePane.ActionType.ADD_HOME_FURNITURE,
        !wallCreationMode && !catalogSelectedItems.isEmpty());
  }

  /**
   * Enables clipboard paste action if clipboard isn't empty.
   */
  public void enablePasteAction() {
    HomePane view = ((HomePane)getView());
    if (this.focusedView == HomePane.FocusableView.FURNITURE
        || this.focusedView == HomePane.FocusableView.PLAN) {
      boolean wallCreationMode =  
        getPlanController().getMode() == PlanController.Mode.WALL_CREATION;
      view.setEnabled(HomePane.ActionType.PASTE,
          !wallCreationMode && !view.isClipboardEmpty());
    } else {
      view.setEnabled(HomePane.ActionType.PASTE, false);
    }
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
              getPlanController().getMode() != PlanController.Mode.WALL_CREATION);
          view.setEnabled(HomePane.ActionType.REDO, false);
          view.setUndoRedoName(ev.getEdit().getUndoPresentationName(), null);
          saveUndoLevel++;
          home.setModified(true);
        }
      });
  }

  /**
   * Enables actions at controller instantiation. 
   */
  private void enableDefaultActions() {
    HomePane view = ((HomePane)getView());
    view.setEnabled(HomePane.ActionType.NEW_HOME, true);
    view.setEnabled(HomePane.ActionType.OPEN, true);
    view.setEnabled(HomePane.ActionType.CLOSE, true);
    view.setEnabled(HomePane.ActionType.SAVE, true);
    view.setEnabled(HomePane.ActionType.SAVE_AS, true);
    view.setEnabled(HomePane.ActionType.EXIT, true);
    view.setEnabled(HomePane.ActionType.WALL_CREATION, true);
    view.setTransferEnabled(true);
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
   * Deletes items and post a cut operation to undo support.
   */
  public void cut(List<? extends Object> items) {
    // Start a compound edit that deletes items and changes presentation name
    this.undoSupport.beginUpdate();
    getPlanController().deleteItems(items);
    // Add a undoable edit to change presentation name
    this.undoSupport.postEdit(new AbstractUndoableEdit() { 
        @Override
        public String getPresentationName() {
          return resource.getString("undoCutName");
        }      
      });
    // End compound edit
    this.undoSupport.endUpdate();
  }
  
  /**
   * Adds items to home and post a paste operation to undo support.
   */
  public void paste(final List<? extends Object> items) {
    addItems(items, 20, 20, resource.getString("undoPasteName"));
  }

  /**
   * Adds items to home, moves them of (dx, dy) 
   * and post a drop operation to undo support.
   */
  public void drop(final List<? extends Object> items, float dx, float dy) {
    addItems(items, dx, dy, resource.getString("undoDropName"));
  }

  /**
   * Adds items to home.
   */
  private void addItems(final List<? extends Object> items, 
                        float dx, float dy, final String presentationName) {
    if (!items.isEmpty()) {
      // Start a compound edit that adds walls and furniture to home
      this.undoSupport.beginUpdate();
      getFurnitureController().addFurniture(Home.getFurnitureSubList(items));
      getPlanController().addWalls(Home.getWallsSubList(items));
      getPlanController().moveItems(items, dx, dy);
      getPlanController().selectAndShowItems(items);
  
      // Add a undoable edit that will select all the items at redo
      this.undoSupport.postEdit(new AbstractUndoableEdit() {      
          @Override
          public void redo() throws CannotRedoException {
            super.redo();
            getPlanController().selectAndShowItems(items);
          }
  
          @Override
          public String getPresentationName() {
            return presentationName;
          }      
        });
     
      // End compound edit
      this.undoSupport.endUpdate();
    }
  }

  /**
   * Deletes the selection in the focused component.
   */
  public void delete() {
    switch (this.focusedView) {
      case FURNITURE :
        getFurnitureController().deleteSelection();
        break;
      case PLAN :
        getPlanController().deleteSelection();
        break;
    }
  }
  
  /**
   * Updates actions when focused view changed.
   */
  public void focusedViewChanged(HomePane.FocusableView focusedView) {
    this.focusedView = focusedView;
    enableActionsOnSelection();
    enablePasteAction();
  }
  
  /**
   * Sets wall creation mode in plan controller, 
   * and disables forbidden actions in this mode.  
   */
  public void setWallCreationMode() {
    getPlanController().setMode(PlanController.Mode.WALL_CREATION);
    enableActionsOnSelection();
    HomePane view = ((HomePane)getView());
    view.setTransferEnabled(false);
    view.setEnabled(HomePane.ActionType.PASTE, false);
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
    enablePasteAction();
    HomePane view = ((HomePane)getView());
    view.setTransferEnabled(true);
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
