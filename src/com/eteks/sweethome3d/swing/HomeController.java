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
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.BackgroundImage;
import com.eteks.sweethome3d.model.Catalog;
import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.ContentManager;
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
  private Home                   home;
  private UserPreferences        preferences;
  private HomeApplication        application;
  private JComponent             homeView;
  private CatalogController      catalogController;
  private FurnitureController    furnitureController;
  private PlanController         planController;
  private HomeController3D       homeController3D;
  private static HelpController  helpController;  // Only one help controller 
  private UndoableEditSupport    undoSupport;
  private UndoManager            undoManager;
  private ResourceBundle         resource;
  private int                    saveUndoLevel;
  private JComponent             focusedView;
  private SelectionListener      catalogSelectionListener;
  private boolean                catalogFurnitureSelectionSynchronized;

  /**
   * Creates the controller of home view.
   * @param home the home edited by this controller and its view.
   * @param application the instance of current application.
   */
  public HomeController(Home home, HomeApplication application) {
    this(home, application.getUserPreferences(), application.getContentManager(), application);
  }

  /**
   * Creates the controller of home view. 
   * @param home        the home edited by this controller and its view.
   * @param preferences the preferences of the application.
   */
  public HomeController(Home home, UserPreferences preferences) {
    this(home, preferences, null, null);
  }

  /**
   * Creates the controller of home view. 
   * @param home        the home edited by this controller and its view.
   * @param preferences the preferences of the application.
   * @param contentManager the content manager of the application.
   */
  public HomeController(Home home, 
                        UserPreferences preferences,
                        ContentManager contentManager) {
    this(home, preferences, contentManager, null);
  }

  private HomeController(Home home, 
                         UserPreferences preferences,
                         ContentManager contentManager,
                         HomeApplication application) {
    this.home = home;
    this.preferences = preferences;
    this.application = application;
    this.undoSupport = new UndoableEditSupport() {
        @Override
        protected void _postEdit(UndoableEdit edit) {
          // Ignore not significant compound edit
          if (!(edit instanceof CompoundEdit)
              || edit.isSignificant()) {
            super._postEdit(edit);
          }
        }
      };
    this.undoManager = new UndoManager();
    this.undoSupport.addUndoableEditListener(this.undoManager);
    this.resource = ResourceBundle.getBundle(
        HomeController.class.getName());
    
    this.catalogController   = new CatalogController(
        preferences.getCatalog(), preferences, contentManager);
    this.furnitureController = new FurnitureController(
        home, preferences, contentManager, this.undoSupport);
    this.planController = new PlanController(
        home, preferences, undoSupport);
    this.homeController3D = new HomeController3D(
        home, preferences, this.undoSupport);
    helpController = new HelpController();
    
    this.homeView = new HomePane(home, preferences, contentManager, this);
    addListeners();
    enableDefaultActions((HomePane)this.homeView);
    
    // Update recent homes list
    if (home.getName() != null) {
      List<String> recentHomes = new ArrayList<String>(this.preferences.getRecentHomes());
      recentHomes.remove(home.getName());
      recentHomes.add(0, home.getName());
      updateUserPreferencesRecentHomes(recentHomes);
    }
  }

  /**
   * Enables actions at controller instantiation. 
   */
  private void enableDefaultActions(HomePane homeView) {
    boolean applicationExists = this.application != null;
    
    homeView.setEnabled(HomePane.ActionType.NEW_HOME, applicationExists);
    homeView.setEnabled(HomePane.ActionType.OPEN, applicationExists);
    homeView.setEnabled(HomePane.ActionType.DELETE_RECENT_HOMES, 
        applicationExists && !this.preferences.getRecentHomes().isEmpty());
    homeView.setEnabled(HomePane.ActionType.CLOSE, applicationExists);
    homeView.setEnabled(HomePane.ActionType.SAVE, applicationExists);
    homeView.setEnabled(HomePane.ActionType.SAVE_AS, applicationExists);
    homeView.setEnabled(HomePane.ActionType.PREFERENCES, true);
    homeView.setEnabled(HomePane.ActionType.EXIT, applicationExists);
    homeView.setEnabled(HomePane.ActionType.IMPORT_FURNITURE, true);
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
    homeView.setEnabled(HomePane.ActionType.SELECT, true);
    homeView.setEnabled(HomePane.ActionType.CREATE_WALLS, true);
    homeView.setEnabled(HomePane.ActionType.IMPORT_BACKGROUND_IMAGE, true);
    ((HomePane)getView()).setEnabled(HomePane.ActionType.MODIFY_BACKGROUND_IMAGE, 
        this.home.getBackgroundImage() != null);
    ((HomePane)getView()).setEnabled(HomePane.ActionType.DELETE_BACKGROUND_IMAGE, 
        this.home.getBackgroundImage() != null);
    homeView.setEnabled(HomePane.ActionType.ZOOM_IN, true);
    homeView.setEnabled(HomePane.ActionType.ZOOM_OUT, true);
    homeView.setEnabled(HomePane.ActionType.VIEW_FROM_TOP, true);
    homeView.setEnabled(HomePane.ActionType.VIEW_FROM_OBSERVER, true);
    homeView.setEnabled(HomePane.ActionType.MODIFY_3D_ATTRIBUTES, true);
    homeView.setEnabled(HomePane.ActionType.HELP, true);
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
   * Returns the controller of home 3D view.
   */
  public HomeController3D getHomeController3D() {
    return this.homeController3D;
  }

  /**
   * Adds listeners that updates the enabled / disabled state of actions.
   */
  private void addListeners() {
    createCatalogSelectionListener();
    setCatalogFurnitureSelectionSynchronized(true);
    addCatalogFurnitureListener();
    addHomeBackgroundImageListener();
    addHomeSelectionListener();
    addFurnitureSortListener();
    addUndoSupportListener();
    addHomeFurnitureListener();
    addHomeWallListener();
    addPlanControllerListener();
  }

  /**
   * Adds a selection listener to catalog that enables / disables Add Furniture action.
   */
  private void createCatalogSelectionListener() {
    this.catalogSelectionListener = new SelectionListener() {
        public void selectionChanged(SelectionEvent ev) {
          enableActionsOnSelection();
        }
      };
  }

  /**
   * If <code>catalogSelectionSynchronized</code> is <code>true</code>, the selected 
   * furniture in the catalog model will be synchronized with be the selection displayed 
   * by the catalog view managed by the catalog controller.
   * By default, selection is synchronized. 
   */
  public void setCatalogFurnitureSelectionSynchronized(boolean catalogFurnitureSelectionSynchronized) {
    if (this.catalogFurnitureSelectionSynchronized ^ catalogFurnitureSelectionSynchronized) {
      if (catalogFurnitureSelectionSynchronized) {
        this.preferences.getCatalog().addSelectionListener(catalogSelectionListener);
      } else {
        this.preferences.getCatalog().removeSelectionListener(catalogSelectionListener);
      }
      this.catalogFurnitureSelectionSynchronized = catalogFurnitureSelectionSynchronized;
    }
    getCatalogController().setFurnitureSelectionSynchronized(catalogFurnitureSelectionSynchronized);
  }
  
  /**
   * Adds a furniture listener to preferences catalog to write preferences 
   * when catalog is modified.
   */
  private void addCatalogFurnitureListener() {
    this.preferences.getCatalog().addFurnitureListener(
        new CatalogWriterFurnitureListener(this));
  }

  /**
   * Catalog listener that writes preferences each time a piece of furniture 
   * is deleted or added in catalog. This listener is bound to this controller 
   * with a weak reference to avoid strong link between catalog and this controller.  
   */
  private static class CatalogWriterFurnitureListener implements FurnitureListener {
    // Stores the currently writing preferences 
    private static Set<UserPreferences> writingPreferences = new HashSet<UserPreferences>();
    private WeakReference<HomeController> homeController;
    
    public CatalogWriterFurnitureListener(HomeController homeController) {
      this.homeController = new WeakReference<HomeController>(homeController);
    }
    
    public void pieceOfFurnitureChanged(FurnitureEvent ev) {
      // If controller was garbage collected, remove this listener from catalog
      final HomeController controller = this.homeController.get();
      if (controller == null) {
        ((Catalog)ev.getSource()).removeFurnitureListener(this);
      } else {
        if (!writingPreferences.contains(controller.preferences)) {
          writingPreferences.add(controller.preferences);
          // Write preferences later once all catalog modifications are notified 
          ((HomePane)controller.getView()).invokeLater(new Runnable() {
              public void run() {
                try {
                  controller.preferences.write();
                  writingPreferences.remove(controller.preferences);
                } catch (RecorderException ex) {
                  ((HomePane)controller.getView()).showError(
                        controller.resource.getString("savePreferencesError"));
                }
              }
            });
        }
      }
    }
  }

  /**
   *  Adds a selection listener to home that enables / disables actions on selection.
   */
  private void addHomeSelectionListener() {
    if (this.home != null) {
      this.home.addSelectionListener(new SelectionListener() {
        public void selectionChanged(SelectionEvent ev) {
          enableActionsOnSelection();
        }
      });
    }
  }

  /**
   *  Adds a property change listener to home that enables / disables sort order action.
   */
  private void addFurnitureSortListener() {
    if (this.home != null) {
      this.home.addPropertyChangeListener(Home.Property.FURNITURE_SORTED_PROPERTY, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            ((HomePane)getView()).setEnabled(HomePane.ActionType.SORT_HOME_FURNITURE_BY_DESCENDING_ORDER, 
                ev.getNewValue() != null);
          }
        });
    }
  }

  /**
   *  Adds a property change listener to home that enables / disables background image actions.
   */
  private void addHomeBackgroundImageListener() {
    if (this.home != null) {
      this.home.addPropertyChangeListener(Home.Property.BACKGROUND_IMAGE, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              ((HomePane)getView()).setEnabled(HomePane.ActionType.MODIFY_BACKGROUND_IMAGE, 
                  ev.getNewValue() != null);
              ((HomePane)getView()).setEnabled(HomePane.ActionType.DELETE_BACKGROUND_IMAGE, 
                  ev.getNewValue() != null);
            }
          });
    }
  }

  /**
   * Enables action bound to selection. 
   */
  private void enableActionsOnSelection() {
    boolean wallCreationMode =  
        getPlanController().getMode() == PlanController.Mode.WALL_CREATION;
    
    // Search if catalog selection contains at least one piece
    List<CatalogPieceOfFurniture> catalogSelectedItems = 
        this.preferences.getCatalog().getSelectedFurniture();    
    boolean catalogSelectionContainsFurniture = !catalogSelectedItems.isEmpty();
    boolean catalogSelectionContainsOneModifiablePiece = catalogSelectedItems.size() == 1
        && catalogSelectedItems.get(0).isModifiable();
    
    // Search if home selection contains at least one piece or one wall
    List<Object> selectedItems = this.home.getSelectedItems();
    boolean homeSelectionContainsFurniture = false;
    boolean homeSelectionContainsOneCopiableObjectOrMore = false;
    boolean homeSelectionContainsTwoPiecesOfFurnitureOrMore = false;
    boolean homeSelectionContainsWalls = false;
    if (!wallCreationMode) {
      homeSelectionContainsFurniture = !Home.getFurnitureSubList(selectedItems).isEmpty();
      homeSelectionContainsTwoPiecesOfFurnitureOrMore = 
          Home.getFurnitureSubList(selectedItems).size() >= 2;
      homeSelectionContainsWalls = !Home.getWallsSubList(selectedItems).isEmpty();
      homeSelectionContainsOneCopiableObjectOrMore = homeSelectionContainsFurniture || homeSelectionContainsWalls; 
    }

    HomePane view = ((HomePane)getView());
    if (this.focusedView == getCatalogController().getView()) {
      view.setEnabled(HomePane.ActionType.COPY,
          !wallCreationMode && catalogSelectionContainsFurniture);
      view.setEnabled(HomePane.ActionType.CUT, false);
      view.setEnabled(HomePane.ActionType.DELETE, false);
      for (CatalogPieceOfFurniture piece : catalogSelectedItems) {
        if (piece.isModifiable()) {
          // Only modifiable catalog furniture may be deleted
          view.setEnabled(HomePane.ActionType.DELETE, true);
          break;
        }
      }
    } else if (this.focusedView == getFurnitureController().getView()) {
      view.setEnabled(HomePane.ActionType.COPY, homeSelectionContainsFurniture);
      view.setEnabled(HomePane.ActionType.CUT, homeSelectionContainsFurniture);
      view.setEnabled(HomePane.ActionType.DELETE, homeSelectionContainsFurniture);
    } else if (this.focusedView == getPlanController().getView()) {
      view.setEnabled(HomePane.ActionType.COPY, homeSelectionContainsOneCopiableObjectOrMore);
      view.setEnabled(HomePane.ActionType.CUT, homeSelectionContainsOneCopiableObjectOrMore);
      view.setEnabled(HomePane.ActionType.DELETE, homeSelectionContainsOneCopiableObjectOrMore);
    } else {
      view.setEnabled(HomePane.ActionType.COPY, false);
      view.setEnabled(HomePane.ActionType.CUT, false);
      view.setEnabled(HomePane.ActionType.DELETE, false);
    }

    view.setEnabled(HomePane.ActionType.ADD_HOME_FURNITURE,
        catalogSelectionContainsFurniture);
    // In creation mode all actions bound to selection are disabled
    view.setEnabled(HomePane.ActionType.DELETE_HOME_FURNITURE,
        homeSelectionContainsFurniture);
    view.setEnabled(HomePane.ActionType.DELETE_SELECTION,
        (catalogSelectionContainsFurniture
            && this.focusedView == getCatalogController().getView())
        || (homeSelectionContainsOneCopiableObjectOrMore 
            && (this.focusedView == getFurnitureController().getView()
                || this.focusedView == getPlanController().getView()
                || this.focusedView == getHomeController3D().getView())));
    view.setEnabled(HomePane.ActionType.MODIFY_FURNITURE,
        (catalogSelectionContainsOneModifiablePiece
             && this.focusedView == getCatalogController().getView())
        || (homeSelectionContainsFurniture 
             && (this.focusedView == getFurnitureController().getView()
                 || this.focusedView == getPlanController().getView()
                 || this.focusedView == getHomeController3D().getView())));
    view.setEnabled(HomePane.ActionType.MODIFY_WALL,
        homeSelectionContainsWalls);
    view.setEnabled(HomePane.ActionType.ALIGN_FURNITURE_ON_TOP,
        homeSelectionContainsTwoPiecesOfFurnitureOrMore);
    view.setEnabled(HomePane.ActionType.ALIGN_FURNITURE_ON_BOTTOM,
        homeSelectionContainsTwoPiecesOfFurnitureOrMore);
    view.setEnabled(HomePane.ActionType.ALIGN_FURNITURE_ON_LEFT,
        homeSelectionContainsTwoPiecesOfFurnitureOrMore);
    view.setEnabled(HomePane.ActionType.ALIGN_FURNITURE_ON_RIGHT,
        homeSelectionContainsTwoPiecesOfFurnitureOrMore);
  }

  /**
   * Enables clipboard paste action if clipboard isn't empty.
   */
  public void enablePasteAction() {
    HomePane view = ((HomePane)getView());
    if (this.focusedView == getFurnitureController().getView()
        || this.focusedView == getPlanController().getView()
        || this.focusedView == getHomeController3D().getView()) {
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
    } else if (this.focusedView == getPlanController().getView()
               || this.focusedView == getHomeController3D().getView()) {
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
   * Adds a property change listener to plan controller to 
   * enable/disable authorized actions according to current mode.
   */
  private void addPlanControllerListener() {
    getPlanController().addPropertyChangeListener(PlanController.Property.MODE, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            enableActionsOnSelection();
            enableSelectAllAction();
            HomePane view = ((HomePane)getView());
            if (getPlanController().getMode() == PlanController.Mode.SELECTION) {
              enablePasteAction();
              view.setEnabled(HomePane.ActionType.UNDO, undoManager.canUndo());
              view.setEnabled(HomePane.ActionType.REDO, undoManager.canRedo());
            } else {
              view.setEnabled(HomePane.ActionType.PASTE, false);
              view.setEnabled(HomePane.ActionType.UNDO, false);
              view.setEnabled(HomePane.ActionType.REDO, false);
            }
          }
        });
  }
  
  /**
   * Adds the selected furniture in catalog to home and selects it.  
   */
  public void addHomeFurniture() {
    // Use automatically selection mode  
    getPlanController().setMode(PlanController.Mode.SELECTION);
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
   * Modifies the selected furniture of the focused view.  
   */
  public void modifySelectedFurniture() {
    if (this.focusedView == getCatalogController().getView()) {
      getCatalogController().modifySelectedFurniture();
    } else if (this.focusedView == getFurnitureController().getView()
               || this.focusedView == getPlanController().getView()
               || this.focusedView == getHomeController3D().getView()) {
      getFurnitureController().modifySelectedFurniture();
    }    
  }

  /**
   * Imports furniture to the catalog or home depending on the focused view.  
   */
  public void importFurniture() {
    if (this.focusedView == getCatalogController().getView()) {
      getCatalogController().importFurniture();
    } else if (this.focusedView == getFurnitureController().getView()
        || this.focusedView == getPlanController().getView()
        || this.focusedView == getHomeController3D().getView()) {
      getFurnitureController().importFurniture();
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
    // Always use selection mode after a drop operation
    getPlanController().setMode(PlanController.Mode.SELECTION);
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
   * Adds imported models to home, moves them of (dx, dy) 
   * and post a drop operation to undo support.
   */
  public void dropFiles(final List<String> importableModels, float dx, float dy) {
    // Always use selection mode after a drop operation
    getPlanController().setMode(PlanController.Mode.SELECTION);
    // Add to home a listener to track imported furniture 
    final List<HomePieceOfFurniture> importedFurniture = 
        new ArrayList<HomePieceOfFurniture>(importableModels.size());
    FurnitureListener addedFurnitureListener = new FurnitureListener () {
        public void pieceOfFurnitureChanged(FurnitureEvent ev) {
          importedFurniture.add((HomePieceOfFurniture)ev.getPieceOfFurniture());
        }
      };
    this.home.addFurnitureListener(addedFurnitureListener);
    
    // Start a compound edit that adds furniture to home
    this.undoSupport.beginUpdate();
    // Import furniture
    for (String model : importableModels) {
      getFurnitureController().importFurniture(model);
    }
    this.home.removeFurnitureListener(addedFurnitureListener);
    
    if (importedFurniture.size() > 0) {
      getPlanController().moveItems(importedFurniture, dx, dy);
      this.home.setSelectedItems(importedFurniture);
      
      // Add a undoable edit that will select the imported furniture at redo
      this.undoSupport.postEdit(new AbstractUndoableEdit() {      
          @Override
          public void redo() throws CannotRedoException {
            super.redo();
            home.setSelectedItems(importedFurniture);
          }
  
          @Override
          public String getPresentationName() {
            return resource.getString("undoDropName");
          }      
        });
    }
   
    // End compound edit
    this.undoSupport.endUpdate();
  }

  /**
   * Deletes the selection in the focused component.
   */
  public void delete() {
    if (this.focusedView == getCatalogController().getView()) {
      if (((HomePane)getView()).confirmDeleteCatalogSelection()) {
        getCatalogController().deleteSelection();
      }
    } else if (this.focusedView == getFurnitureController().getView()) {
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
      getFurnitureController().selectAll();
    } else if (this.focusedView == getPlanController().getView()) {
      getPlanController().selectAll();
    }
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
    ((HomePane)getView()).invokeLater(new Runnable() {
      public void run() {
        final String homeName = ((HomePane)getView()).showOpenDialog();
        if (homeName != null) {
          open(homeName);
        }
      }
    });
  }

  /**
   * Opens a given <code>homeName</code>home.
   */
  public void open(String homeName) {
    // Check if requested home isn't already opened
    for (Home home : this.application.getHomes()) {
      if (homeName.equals(home.getName())) {
        String message = String.format(this.resource.getString("alreadyOpen"), homeName);
        ((HomePane)getView()).showMessage(message);
        return;
      }
    }
    try {
      Home openedHome = this.application.getHomeRecorder().readHome(homeName);
      openedHome.setName(homeName); 
      this.application.addHome(openedHome);
    } catch (RecorderException ex) {
      String message = String.format(this.resource.getString("openError"), homeName);
      ((HomePane)getView()).showError(message);
    }
  }
  
  /**
   * Updates user preferences <code>recentHomes</code> and write preferences. 
   */
  private void updateUserPreferencesRecentHomes(List<String> recentHomes) {
    if (this.application != null) {
      try {
        // Check every recent home exists
        for (Iterator<String> it = recentHomes.iterator(); it.hasNext(); ) {
          try {
            if (!this.application.getHomeRecorder().exists(it.next())) {
              it.remove();
            }
          } catch (RecorderException ex) {
            // If homeName can't be checked ignore it
          }
        }
        this.preferences.setRecentHomes(recentHomes);
        this.preferences.write();
      } catch (RecorderException ex) {
        HomePane homeView = (HomePane)getView();
        // As this method may called from constructor, check if homeView isn't null 
        if (homeView != null) {
          homeView.showError(this.resource.getString("savePreferencesError"));
        }
      }
    }
  }

  /**
   * Returns a list of displayable recent homes. 
   */
  public List<String> getRecentHomes() {
    if (this.application != null) {
      List<String> recentHomes = new ArrayList<String>();
      for (String homeName : this.preferences.getRecentHomes()) {
        try {
          if (this.application.getHomeRecorder().exists(homeName)) {
            recentHomes.add(homeName);
            if (recentHomes.size() == 4) {
              break;
            }
          }
        } catch (RecorderException ex) {
          // If homeName can't be checked ignore it
        }
      }
      ((HomePane)getView()).setEnabled(HomePane.ActionType.DELETE_RECENT_HOMES, 
          !recentHomes.isEmpty());
      return Collections.unmodifiableList(recentHomes);
    } else {
      return new ArrayList<String>();
    }
  }
  
  /**
   * Deletes the list of recent homes in user preferences. 
   */
  public void deleteRecentHomes() {
    updateUserPreferencesRecentHomes(new ArrayList<String>());
    ((HomePane)getView()).setEnabled(HomePane.ActionType.DELETE_RECENT_HOMES, false);
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
      String oldHomeName = this.home.getName();
      this.home.setName(homeName);
      this.saveUndoLevel = 0;
      this.home.setModified(false);
      // Update recent homes list
      List<String> recentHomes = new ArrayList<String>(this.preferences.getRecentHomes());
      if (homeName.equals(oldHomeName)) {
        recentHomes.remove(oldHomeName);
      }
      recentHomes.add(0, homeName);
      updateUserPreferencesRecentHomes(recentHomes);
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
   * Displays help window.
   */
  public void help() {
    helpController.displayView();
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
    new BackgroundImageWizardController(this.home, this.preferences, 
        this.application.getContentManager(), this.undoSupport);
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
