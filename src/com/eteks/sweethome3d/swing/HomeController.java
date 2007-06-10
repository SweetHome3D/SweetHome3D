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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.BackgroundImage;
import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.FurnitureEvent;
import com.eteks.sweethome3d.model.FurnitureListener;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeApplication;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.SelectionEvent;
import com.eteks.sweethome3d.model.SelectionListener;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.WallEvent;
import com.eteks.sweethome3d.model.WallListener;

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
  private JComponent          focusedView;

  /**
   * Creates the controller of home view.
   * @param home the home edited by this controller and its view.
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
    homeView.setEnabled(HomePane.ActionType.PREFERENCES, true);
    homeView.setEnabled(HomePane.ActionType.EXIT, true);
    homeView.setEnabled(HomePane.ActionType.SORT_HOME_FURNITURE_BY_NAME, true);
    homeView.setEnabled(HomePane.ActionType.SORT_HOME_FURNITURE_BY_WIDTH, true);
    homeView.setEnabled(HomePane.ActionType.SORT_HOME_FURNITURE_BY_HEIGHT, true);
    homeView.setEnabled(HomePane.ActionType.SORT_HOME_FURNITURE_BY_DEPTH, true);
    homeView.setEnabled(HomePane.ActionType.SORT_HOME_FURNITURE_BY_COLOR, true);
    homeView.setEnabled(HomePane.ActionType.SORT_HOME_FURNITURE_BY_MOVABILITY, true);
    homeView.setEnabled(HomePane.ActionType.SORT_HOME_FURNITURE_BY_TYPE, true);
    homeView.setEnabled(HomePane.ActionType.SORT_HOME_FURNITURE_BY_VISIBILITY, true);
    homeView.setEnabled(HomePane.ActionType.SORT_HOME_FURNITURE_BY_DESCENDING_ORDER, 
        this.home.getFurnitureSortedProperty() != null);
    homeView.setEnabled(HomePane.ActionType.WALL_CREATION, true);
    homeView.setEnabled(HomePane.ActionType.IMPORT_BACKGROUND_IMAGE, true);
    ((HomePane)getView()).setEnabled(HomePane.ActionType.MODIFY_BACKGROUND_IMAGE, 
        this.home.getBackgroundImage() != null);
    ((HomePane)getView()).setEnabled(HomePane.ActionType.DELETE_BACKGROUND_IMAGE, 
        this.home.getBackgroundImage() != null);
    homeView.setEnabled(HomePane.ActionType.ZOOM_IN, true);
    homeView.setEnabled(HomePane.ActionType.ZOOM_OUT, true);
    homeView.setEnabled(HomePane.ActionType.ABOUT, true);
    homeView.setTransferEnabled(true);
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
    addHomeBackgroundImageListener();
    addHomeSelectionListener();
    addFurnitureSortListener();
    addUndoSupportListener();
    addHomeFurnitureListener();
    addHomeWallListener();
  }

  /**
   * Adds a selection listener to catalog that enables / disables Add Furniture action.
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
   *  Adds a selection listener to home that enables / disables actions on selection.
   */
  private void addHomeSelectionListener() {
    this.home.addSelectionListener(new SelectionListener() {
      public void selectionChanged(SelectionEvent ev) {
        enableActionsOnSelection();
      }
    });
  }

  /**
   *  Adds a property change listener to home that enables / disables sort order action.
   */
  private void addFurnitureSortListener() {
    this.home.addPropertyChangeListener("furnitureSortedProperty", 
      new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          ((HomePane)getView()).setEnabled(HomePane.ActionType.SORT_HOME_FURNITURE_BY_DESCENDING_ORDER, 
              ev.getNewValue() != null);
        }
      });
  }

  /**
   *  Adds a property change listener to home that enables / disables background image actions.
   */
  private void addHomeBackgroundImageListener() {
    this.home.addPropertyChangeListener("backgroundImage", 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            ((HomePane)getView()).setEnabled(HomePane.ActionType.MODIFY_BACKGROUND_IMAGE, 
                ev.getNewValue() != null);
            ((HomePane)getView()).setEnabled(HomePane.ActionType.DELETE_BACKGROUND_IMAGE, 
                ev.getNewValue() != null);
          }
        });
  }

  /**
   * Enables action bound to selection. 
   */
  private void enableActionsOnSelection() {
    boolean wallCreationMode =  
        getPlanController().getMode() == PlanController.Mode.WALL_CREATION;
    
    // Search if selection contains at least one piece or one wall
    List<Object> selectedItems = this.home.getSelectedItems();
    boolean selectionContainsFurniture = false;
    boolean selectionContainsTwoPiecesOfFurnitureOrMore = false;
    boolean selectionContainsWalls = false;
    if (!wallCreationMode) {
      selectionContainsFurniture = !Home.getFurnitureSubList(selectedItems).isEmpty();
      selectionContainsTwoPiecesOfFurnitureOrMore = 
          Home.getFurnitureSubList(selectedItems).size() >= 2;
      selectionContainsWalls = !Home.getWallsSubList(selectedItems).isEmpty();
    }

    List catalogSelectedItems = this.preferences.getCatalog().getSelectedFurniture();    
    HomePane view = ((HomePane)getView());
    if (this.focusedView == getCatalogController().getView()) {
      view.setEnabled(HomePane.ActionType.COPY,
          !wallCreationMode && !catalogSelectedItems.isEmpty());
      view.setEnabled(HomePane.ActionType.CUT, false);
      view.setEnabled(HomePane.ActionType.DELETE, false);
    } else if (this.focusedView == getFurnitureController().getView()) {
      view.setEnabled(HomePane.ActionType.COPY, selectionContainsFurniture);
      view.setEnabled(HomePane.ActionType.CUT, selectionContainsFurniture);
      view.setEnabled(HomePane.ActionType.DELETE, selectionContainsFurniture);
    } else if (this.focusedView == getPlanController().getView()) {
      boolean copyEnabled = !wallCreationMode && !selectedItems.isEmpty();
      view.setEnabled(HomePane.ActionType.COPY, copyEnabled);
      view.setEnabled(HomePane.ActionType.CUT, copyEnabled);
      view.setEnabled(HomePane.ActionType.DELETE, copyEnabled);
    } else {
      view.setEnabled(HomePane.ActionType.COPY, false);
      view.setEnabled(HomePane.ActionType.CUT, false);
      view.setEnabled(HomePane.ActionType.DELETE, false);
    }

    // In creation mode all actions bound to selection are disabled
    view.setEnabled(HomePane.ActionType.ADD_HOME_FURNITURE,
        !wallCreationMode && !catalogSelectedItems.isEmpty());
    view.setEnabled(HomePane.ActionType.DELETE_HOME_FURNITURE,
        selectionContainsFurniture);
    view.setEnabled(HomePane.ActionType.DELETE_SELECTION,
        !wallCreationMode && !selectedItems.isEmpty());
    view.setEnabled(HomePane.ActionType.MODIFY_HOME_FURNITURE,
        selectionContainsFurniture);
    view.setEnabled(HomePane.ActionType.MODIFY_WALL,
        selectionContainsWalls);
    view.setEnabled(HomePane.ActionType.ALIGN_FURNITURE_ON_TOP,
        selectionContainsTwoPiecesOfFurnitureOrMore);
    view.setEnabled(HomePane.ActionType.ALIGN_FURNITURE_ON_BOTTOM,
        selectionContainsTwoPiecesOfFurnitureOrMore);
    view.setEnabled(HomePane.ActionType.ALIGN_FURNITURE_ON_LEFT,
        selectionContainsTwoPiecesOfFurnitureOrMore);
    view.setEnabled(HomePane.ActionType.ALIGN_FURNITURE_ON_RIGHT,
        selectionContainsTwoPiecesOfFurnitureOrMore);
  }

  /**
   * Enables clipboard paste action if clipboard isn't empty.
   */
  public void enablePasteAction() {
    HomePane view = ((HomePane)getView());
    if (this.focusedView == getFurnitureController().getView()
        || this.focusedView == getPlanController().getView()) {
      boolean wallCreationMode =  
        getPlanController().getMode() == PlanController.Mode.WALL_CREATION;
      view.setEnabled(HomePane.ActionType.PASTE,
          !wallCreationMode && !view.isClipboardEmpty());
    } else {
      view.setEnabled(HomePane.ActionType.PASTE, false);
    }
  }

  /**
   * Enables select all action if home isn't empty.
   */
  private void enableSelectAllAction() {
    HomePane view = ((HomePane)getView());
    boolean wallCreationMode =  
      getPlanController().getMode() == PlanController.Mode.WALL_CREATION;
    if (this.focusedView == getFurnitureController().getView()) {
      view.setEnabled(HomePane.ActionType.SELECT_ALL,
          !wallCreationMode 
          && this.home.getFurniture().size() > 0);
    } else if (this.focusedView == getPlanController().getView()) {
      view.setEnabled(HomePane.ActionType.SELECT_ALL,
          !wallCreationMode 
          && (this.home.getFurniture().size() > 0 
              || this.home.getWalls().size() > 0));
    } else {
      view.setEnabled(HomePane.ActionType.SELECT_ALL, false);
    }
  }

  /**
   * Enables zoom actions depending on current scale.
   */
  private void enableZoomActions() {
    float scale = getPlanController().getScale();
    HomePane view = ((HomePane)getView());
    view.setEnabled(HomePane.ActionType.ZOOM_IN, scale <= 5);
    view.setEnabled(HomePane.ActionType.ZOOM_OUT, scale >= 0.1f);    
  }
  
  /**
   * Adds undoable edit listener to undo support that enables Undo action.
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
   * Adds a furniture listener to home that enables / disables actions on furniture list change.
   */
  private void addHomeFurnitureListener() {
    this.home.addFurnitureListener(new FurnitureListener() {
        public void pieceOfFurnitureChanged(FurnitureEvent ev) {
          if (ev.getType() == FurnitureEvent.Type.ADD 
              || ev.getType() == FurnitureEvent.Type.DELETE) {
            enableSelectAllAction();
          }
        }
      });
  }

  /**
   * Adds a wall listener to home that enables / disables actions on walls list change.
   */
  private void addHomeWallListener() {
    this.home.addWallListener(new WallListener() {
      public void wallChanged(WallEvent ev) {
        if (ev.getType() == WallEvent.Type.ADD 
            || ev.getType() == WallEvent.Type.DELETE) {
          enableSelectAllAction();
        }
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
      this.home.setSelectedItems(items);
  
      // Add a undoable edit that will select all the items at redo
      this.undoSupport.postEdit(new AbstractUndoableEdit() {      
          @Override
          public void redo() throws CannotRedoException {
            super.redo();
            home.setSelectedItems(items);
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
    if (this.focusedView == getFurnitureController().getView()) {
      getFurnitureController().deleteSelection();
    } else if (this.focusedView == getPlanController().getView()) {
      getPlanController().deleteSelection();
    }
  }
  
  /**
   * Updates actions when focused view changed.
   */
  public void focusedViewChanged(JComponent focusedView) {
    this.focusedView = focusedView;
    enableActionsOnSelection();
    enablePasteAction();
    enableSelectAllAction();
  }
  
  /**
   * Selects everything in the focused component.
   */
  public void selectAll() {
    if (this.focusedView == getFurnitureController().getView()) {
      this.home.setSelectedItems(this.home.getFurniture());
    } else if (this.focusedView == getPlanController().getView()) {
      List<Object> all = new ArrayList<Object>(this.home.getFurniture());
      all.addAll(this.home.getWalls());
      this.home.setSelectedItems(all);
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
    enableSelectAllAction();
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
        openedHome.setName(homeName); 
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

  /**
   * Edits preferences and changes them if user agrees.
   */
  public void editPreferences() {
    UserPreferencesPanel preferencesPanel = new UserPreferencesPanel();
    preferencesPanel.setPreferences(this.preferences);
    if (preferencesPanel.showDialog(getView())) {
      this.preferences.setUnit(preferencesPanel.getUnit());
      this.preferences.setMagnetismEnabled(preferencesPanel.isMagnetismEnabled());
      this.preferences.setRulersVisible(preferencesPanel.isRulersVisible());
      this.preferences.setNewWallThickness(preferencesPanel.getNewWallThickness());
      this.preferences.setNewHomeWallHeight(preferencesPanel.getNewHomeWallHeight());
      try {
        this.preferences.write();
      } catch (RecorderException ex) {
        ((HomePane)getView()).showError(
            this.resource.getString("savePreferencesError"));
      }
    }
  }

  /**
   * Displays about dialog.
   */
  public void about() {
    ((HomePane)getView()).showAboutDialog();
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
   * Displays the wizard that helps to import home background image. 
   */
  public void importBackgroundImage() {
    new BackgroundImageWizardController(this.home, this.preferences, this.undoSupport);
  }
  
  /**
   * Displays the wizard that helps to change home background image. 
   */
  public void modifyBackgroundImage() {
    importBackgroundImage();
  }
  
  /**
   * Deletes home background image and posts and posts an undoable operation. 
   */
  public void deleteBackgroundImage() {
    final BackgroundImage oldImage = this.home.getBackgroundImage();
    this.home.setBackgroundImage(null);
    UndoableEdit undoableEdit = new AbstractUndoableEdit() {
      @Override
      public void undo() throws CannotUndoException {
        super.undo();
        home.setBackgroundImage(oldImage); 
      }
      
      @Override
      public void redo() throws CannotRedoException {
        super.redo();
        home.setBackgroundImage(null);
      }
      
      @Override
      public String getPresentationName() {
        return resource.getString("undoDeleteBackgroundImageName");
      }
    };
    this.undoSupport.postEdit(undoableEdit);
  }
  
  /**
   * Zooms out in plan.
   */
  public void zoomOut() {
    PlanController planController = getPlanController();
    float newScale = planController.getScale() / 1.5f;
    planController.setScale(newScale);
    enableZoomActions();  
  }

  /**
   * Zooms in in plan.
   */
  public void zoomIn() {
    PlanController planController = getPlanController();
    float newScale = planController.getScale() * 1.5f;
    planController.setScale(newScale);
    enableZoomActions();
  }
}
