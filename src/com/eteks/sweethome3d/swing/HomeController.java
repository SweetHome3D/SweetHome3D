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
import java.util.concurrent.Callable;

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
import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.ContentManager;
import com.eteks.sweethome3d.model.FurnitureCatalog;
import com.eteks.sweethome3d.model.FurnitureEvent;
import com.eteks.sweethome3d.model.FurnitureListener;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeApplication;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.InterruptedRecorderException;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.SelectionEvent;
import com.eteks.sweethome3d.model.SelectionListener;
import com.eteks.sweethome3d.model.TextureEvent;
import com.eteks.sweethome3d.model.TextureListener;
import com.eteks.sweethome3d.model.TexturesCatalog;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.model.WallEvent;
import com.eteks.sweethome3d.model.WallListener;

/**
 * A MVC controller for the home view.
 * @author Emmanuel Puybaret
 */
public class HomeController  {
  private Home                       home;
  private UserPreferences            preferences;
  private ContentManager             contentManager;
  private HomeApplication            application;
  private JComponent                 homeView;
  private FurnitureCatalogController catalogController;
  private FurnitureController        furnitureController;
  private PlanController             planController;
  private HomeController3D           homeController3D;
  private static HelpController      helpController;  // Only one help controller 
  private UndoableEditSupport        undoSupport;
  private UndoManager                undoManager;
  private ResourceBundle             resource;
  private int                        saveUndoLevel;
  private JComponent                 focusedView;
  private SelectionListener          catalogSelectionListener;
  private boolean                    catalogFurnitureSelectionSynchronized;

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

  private HomeController(final Home home, 
                         UserPreferences preferences,
                         ContentManager contentManager,
                         HomeApplication application) {
    this.home = home;
    this.preferences = preferences;
    this.contentManager = contentManager;
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
    
    this.catalogController   = new FurnitureCatalogController(
        preferences.getFurnitureCatalog(), preferences, contentManager);
    this.furnitureController = new FurnitureController(
        home, preferences, contentManager, this.undoSupport);
    this.planController = new PlanController(
        home, preferences, contentManager, undoSupport);
    this.homeController3D = new HomeController3D(
        home, preferences, contentManager, this.undoSupport);
    helpController = new HelpController(preferences);
    
    this.homeView = new HomePane(home, preferences, contentManager, this);
    addListeners();
    enableDefaultActions((HomePane)this.homeView);
    
    // Update recent homes list
    if (home.getName() != null) {
      List<String> recentHomes = new ArrayList<String>(this.preferences.getRecentHomes());
      recentHomes.remove(home.getName());
      recentHomes.add(0, home.getName());
      updateUserPreferencesRecentHomes(recentHomes);
      
      // If home version is more recent than current version
      if (home.getVersion() > Home.CURRENT_VERSION) {
        // Warn the user will display a home created with a more recent version 
        ((HomePane)getView()).invokeLater(new Runnable() { 
            public void run() {
              String message = String.format(resource.getString("moreRecentVersionHome"), home.getName());
              ((HomePane)getView()).showMessage(message);
            }
          });
      }
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
    homeView.setEnabled(HomePane.ActionType.PAGE_SETUP, true);
    homeView.setEnabled(HomePane.ActionType.PRINT_PREVIEW, true);
    homeView.setEnabled(HomePane.ActionType.PRINT, true);
    homeView.setEnabled(HomePane.ActionType.PRINT_TO_PDF, true);
    homeView.setEnabled(HomePane.ActionType.PREFERENCES, true);
    homeView.setEnabled(HomePane.ActionType.EXIT, applicationExists);
    homeView.setEnabled(HomePane.ActionType.IMPORT_FURNITURE, true);
    homeView.setEnabled(HomePane.ActionType.IMPORT_FURNITURE_LIBRARY, true);
    homeView.setEnabled(HomePane.ActionType.SORT_HOME_FURNITURE_BY_NAME, true);
    homeView.setEnabled(HomePane.ActionType.SORT_HOME_FURNITURE_BY_WIDTH, true);
    homeView.setEnabled(HomePane.ActionType.SORT_HOME_FURNITURE_BY_HEIGHT, true);
    homeView.setEnabled(HomePane.ActionType.SORT_HOME_FURNITURE_BY_DEPTH, true);
    homeView.setEnabled(HomePane.ActionType.SORT_HOME_FURNITURE_BY_X, true);
    homeView.setEnabled(HomePane.ActionType.SORT_HOME_FURNITURE_BY_Y, true);
    homeView.setEnabled(HomePane.ActionType.SORT_HOME_FURNITURE_BY_ELEVATION, true);
    homeView.setEnabled(HomePane.ActionType.SORT_HOME_FURNITURE_BY_ANGLE, true);
    homeView.setEnabled(HomePane.ActionType.SORT_HOME_FURNITURE_BY_COLOR, true);
    homeView.setEnabled(HomePane.ActionType.SORT_HOME_FURNITURE_BY_MOVABILITY, true);
    homeView.setEnabled(HomePane.ActionType.SORT_HOME_FURNITURE_BY_TYPE, true);
    homeView.setEnabled(HomePane.ActionType.SORT_HOME_FURNITURE_BY_VISIBILITY, true);
    homeView.setEnabled(HomePane.ActionType.SORT_HOME_FURNITURE_BY_DESCENDING_ORDER, 
        this.home.getFurnitureSortedProperty() != null);
    homeView.setEnabled(HomePane.ActionType.DISPLAY_HOME_FURNITURE_NAME, true); 
    homeView.setEnabled(HomePane.ActionType.DISPLAY_HOME_FURNITURE_WIDTH, true); 
    homeView.setEnabled(HomePane.ActionType.DISPLAY_HOME_FURNITURE_DEPTH, true); 
    homeView.setEnabled(HomePane.ActionType.DISPLAY_HOME_FURNITURE_HEIGHT, true); 
    homeView.setEnabled(HomePane.ActionType.DISPLAY_HOME_FURNITURE_X, true); 
    homeView.setEnabled(HomePane.ActionType.DISPLAY_HOME_FURNITURE_Y, true); 
    homeView.setEnabled(HomePane.ActionType.DISPLAY_HOME_FURNITURE_ELEVATION, true); 
    homeView.setEnabled(HomePane.ActionType.DISPLAY_HOME_FURNITURE_ANGLE, true); 
    homeView.setEnabled(HomePane.ActionType.DISPLAY_HOME_FURNITURE_COLOR, true); 
    homeView.setEnabled(HomePane.ActionType.DISPLAY_HOME_FURNITURE_MOVABLE, true); 
    homeView.setEnabled(HomePane.ActionType.DISPLAY_HOME_FURNITURE_DOOR_OR_WINDOW, true); 
    homeView.setEnabled(HomePane.ActionType.DISPLAY_HOME_FURNITURE_VISIBLE, true);
    homeView.setEnabled(HomePane.ActionType.SELECT, true);
    homeView.setEnabled(HomePane.ActionType.CREATE_WALLS, true);
    homeView.setEnabled(HomePane.ActionType.CREATE_DIMENSION_LINES, true);
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
    homeView.setEnabled(HomePane.ActionType.EXPORT_TO_OBJ, 
        this.home.getFurniture().size() > 0 
        || this.home.getWalls().size() > 0);
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
  public FurnitureCatalogController getCatalogController() {
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
    CatalogWriterListener catalogsListener = new CatalogWriterListener(this);
    this.preferences.getFurnitureCatalog().addFurnitureListener(catalogsListener);
    this.preferences.getTexturesCatalog().addTextureListener(catalogsListener);
    addHomeBackgroundImageListener();
    addHomeSelectionListener();
    addFurnitureSortListener();
    addUndoSupportListener();
    addHomeFurnitureListener();
    addHomeWallListener();
    addPlanControllerListener();
    addLanguageListener();
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
        this.preferences.getFurnitureCatalog().addSelectionListener(catalogSelectionListener);
      } else {
        this.preferences.getFurnitureCatalog().removeSelectionListener(catalogSelectionListener);
      }
      this.catalogFurnitureSelectionSynchronized = catalogFurnitureSelectionSynchronized;
    }
    getCatalogController().setFurnitureSelectionSynchronized(catalogFurnitureSelectionSynchronized);
  }
  
  /**
   * Catalog listener that writes preferences each time a piece of furniture or a texture
   * is deleted or added in furniture or textures catalog. This listener is bound to this controller 
   * with a weak reference to avoid strong link between catalog and this controller.  
   */
  private static class CatalogWriterListener implements FurnitureListener, TextureListener {
    // Stores the currently writing preferences 
    private static Set<UserPreferences> writingPreferences = new HashSet<UserPreferences>();
    private WeakReference<HomeController> homeController;
    
    public CatalogWriterListener(HomeController homeController) {
      this.homeController = new WeakReference<HomeController>(homeController);
    }
    
    public void pieceOfFurnitureChanged(FurnitureEvent ev) {
      // If controller was garbage collected, remove this listener from catalog
      final HomeController controller = this.homeController.get();
      if (controller == null) {
        ((FurnitureCatalog)ev.getSource()).removeFurnitureListener(this);
      } else {
        writePreferences(controller);
      }
    }

    public void textureChanged(TextureEvent ev) {
      // If controller was garbage collected, remove this listener from catalog
      final HomeController controller = this.homeController.get();
      if (controller == null) {
        ((TexturesCatalog)ev.getSource()).removeTextureListener(this);
      } else {
        writePreferences(controller);
      }
    }
    
    private void writePreferences(final HomeController controller) {
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

  /**
   * Adds a property change listener to <code>preferences</code> to update
   * undo and redo presentation names when preferred language changes.
   */
  private void addLanguageListener() {
    preferences.addPropertyChangeListener(UserPreferences.Property.LANGUAGE, 
        new LanguageChangeListener(this));
  }

  /**
   * Preferences property listener bound to this component with a weak reference to avoid
   * strong link between preferences and this component.  
   */
  private static class LanguageChangeListener implements PropertyChangeListener {
    private WeakReference<HomeController> homeController;

    public LanguageChangeListener(HomeController homeController) {
      this.homeController = new WeakReference<HomeController>(homeController);
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
      // If home pane was garbage collected, remove this listener from preferences
      HomeController homeController = this.homeController.get();
      if (homeController == null) {
        ((UserPreferences)ev.getSource()).removePropertyChangeListener(
            UserPreferences.Property.LANGUAGE, this);
      } else {
        homeController.resource = ResourceBundle.getBundle(
            HomeController.class.getName());
        // Update undo and redo name
        ((HomePane)homeController.getView()).setUndoRedoName(
            homeController.undoManager.canUndo() 
                ? homeController.undoManager.getUndoPresentationName()
                : null,
            homeController.undoManager.canRedo() 
                ? homeController.undoManager.getRedoPresentationName()
                : null);
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
    boolean selectionMode =  
        getPlanController().getMode() == PlanController.Mode.SELECTION;
    
    // Search if catalog selection contains at least one piece
    List<CatalogPieceOfFurniture> catalogSelectedItems = 
        this.preferences.getFurnitureCatalog().getSelectedFurniture();    
    boolean catalogSelectionContainsFurniture = !catalogSelectedItems.isEmpty();
    boolean catalogSelectionContainsOneModifiablePiece = catalogSelectedItems.size() == 1
        && catalogSelectedItems.get(0).isModifiable();
    
    // Search if home selection contains at least one piece, one wall or one dimension line
    List<Object> selectedItems = this.home.getSelectedItems();
    boolean homeSelectionContainsFurniture = false;
    boolean homeSelectionContainsOneCopiableObjectOrMore = false;
    boolean homeSelectionContainsTwoPiecesOfFurnitureOrMore = false;
    boolean homeSelectionContainsWalls = false;
    boolean homeSelectionContainsOneWall = false;
    if (selectionMode) {
      homeSelectionContainsFurniture = !Home.getFurnitureSubList(selectedItems).isEmpty();
      homeSelectionContainsTwoPiecesOfFurnitureOrMore = 
          Home.getFurnitureSubList(selectedItems).size() >= 2;
      List<Wall> selectedWalls = Home.getWallsSubList(selectedItems);
      homeSelectionContainsWalls = !selectedWalls.isEmpty();
      homeSelectionContainsOneWall = selectedWalls.size() == 1;
      boolean homeSelectionContainsDimensionLines = !Home.getDimensionLinesSubList(selectedItems).isEmpty();
      homeSelectionContainsOneCopiableObjectOrMore = 
          homeSelectionContainsFurniture || homeSelectionContainsWalls || homeSelectionContainsDimensionLines; 
    }

    HomePane view = ((HomePane)getView());
    if (this.focusedView == getCatalogController().getView()) {
      view.setEnabled(HomePane.ActionType.COPY,
          selectionMode && catalogSelectionContainsFurniture);
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
    view.setEnabled(HomePane.ActionType.REVERSE_WALL_DIRECTION,
        homeSelectionContainsWalls);
    view.setEnabled(HomePane.ActionType.SPLIT_WALL,
        homeSelectionContainsOneWall);
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
      boolean selectionMode =  
          getPlanController().getMode() == PlanController.Mode.SELECTION;
      view.setEnabled(HomePane.ActionType.PASTE,
          selectionMode && !view.isClipboardEmpty());
    } else {
      view.setEnabled(HomePane.ActionType.PASTE, false);
    }
  }

  /**
   * Enables select all action if home isn't empty.
   */
  private void enableSelectAllAction() {
    HomePane view = ((HomePane)getView());
    boolean selectionMode =  
      getPlanController().getMode() == PlanController.Mode.SELECTION;
    if (this.focusedView == getFurnitureController().getView()) {
      view.setEnabled(HomePane.ActionType.SELECT_ALL,
          selectionMode 
          && this.home.getFurniture().size() > 0);
    } else if (this.focusedView == getPlanController().getView()
               || this.focusedView == getHomeController3D().getView()) {
      view.setEnabled(HomePane.ActionType.SELECT_ALL,
          selectionMode
          && (this.home.getFurniture().size() > 0 
              || this.home.getWalls().size() > 0 
              || this.home.getDimensionLines().size() > 0));
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
   * Enables zoom actions depending on current scale.
   */
  private void enableExportActions() {
    HomePane view = ((HomePane)getView());
    view.setEnabled(HomePane.ActionType.EXPORT_TO_OBJ, 
        this.home.getFurniture().size() > 0 
        || this.home.getWalls().size() > 0);
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
              getPlanController().getMode() == PlanController.Mode.SELECTION);
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
            enableExportActions();
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
          enableExportActions();
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
      this.preferences.getFurnitureCatalog().getSelectedFurniture();
    if (!selectedFurniture.isEmpty()) {
      List<HomePieceOfFurniture> newFurniture = 
          new ArrayList<HomePieceOfFurniture>();
      for (CatalogPieceOfFurniture piece : selectedFurniture) {
        HomePieceOfFurniture homePiece = new HomePieceOfFurniture(piece);
        // If magnetism is enabled, adjust piece size and elevation
        if (this.preferences.isMagnetismEnabled()) {
          this.home.setPieceOfFurnitureSize(homePiece, 
              this.preferences.getUnit().getMagnetizedLength(homePiece.getWidth(), 0.1f),
              this.preferences.getUnit().getMagnetizedLength(homePiece.getDepth(), 0.1f),
              this.preferences.getUnit().getMagnetizedLength(homePiece.getHeight(), 0.1f)); 
          this.home.setPieceOfFurnitureElevation(homePiece,
              this.preferences.getUnit().getMagnetizedLength(homePiece.getElevation(), 0.1f));
        }
        newFurniture.add(homePiece);
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
   * Imports a furniture library chosen by the user.  
   */
  public void importFurnitureLibrary() {
    ((HomePane)getView()).invokeLater(new Runnable() {
        public void run() {
          final String furnitureLibraryName = ((HomePane)getView()).showImportFurnitureLibraryDialog();
          if (furnitureLibraryName != null) {
            importFurnitureLibrary(furnitureLibraryName);
          }
        }
      });
  }

  /**
   * Imports a given furniture library. 
   */
  public void importFurnitureLibrary(String furnitureLibraryName) {
    try {
      if (!this.preferences.furnitureLibraryExists(furnitureLibraryName) 
          || ((HomePane)getView()).confirmReplaceFurnitureLibrary(furnitureLibraryName)) {
        this.preferences.addFurnitureLibrary(furnitureLibraryName);
      }
    } catch (RecorderException ex) {
      String message = String.format(resource.getString("importFurnitureLibraryError"), furnitureLibraryName);
      ((HomePane)getView()).showError(message);
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
    addItems(items, 20, 20, "undoPasteName");
  }

  /**
   * Adds items to home, moves them of (dx, dy) 
   * and post a drop operation to undo support.
   */
  public void drop(final List<? extends Object> items, float dx, float dy) {
    // Always use selection mode after a drop operation
    getPlanController().setMode(PlanController.Mode.SELECTION);
    addItems(items, dx, dy, "undoDropName");
  }

  /**
   * Adds items to home.
   */
  private void addItems(final List<? extends Object> items, 
                        float dx, float dy, final String presentationNameKey) {
    if (!items.isEmpty()) {
      // Start a compound edit that adds walls, furniture and dimension lines to home
      this.undoSupport.beginUpdate();
      List<HomePieceOfFurniture> addedFurniture = Home.getFurnitureSubList(items);
      // If magnetism is enabled, adjust furniture size and elevation
      if (this.preferences.isMagnetismEnabled()) {
        for (HomePieceOfFurniture piece : addedFurniture) {
          this.home.setPieceOfFurnitureSize(piece, 
              this.preferences.getUnit().getMagnetizedLength(piece.getWidth(), 0.1f),
              this.preferences.getUnit().getMagnetizedLength(piece.getDepth(), 0.1f),
              this.preferences.getUnit().getMagnetizedLength(piece.getHeight(), 0.1f)); 
          this.home.setPieceOfFurnitureElevation(piece,
              this.preferences.getUnit().getMagnetizedLength(piece.getElevation(), 0.1f));
        }
      }
      getPlanController().addFurniture(addedFurniture);
      getPlanController().addWalls(Home.getWallsSubList(items));
      getPlanController().addDimensionLines(Home.getDimensionLinesSubList(items));
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
            return resource.getString(presentationNameKey);
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
        new Home(this.preferences.getNewWallHeight()));
  }

  /**
   * Opens a home. This method displays an {@link HomePane#showOpenDialog() open dialog} 
   * in view, reads the home from the chosen name and adds it to application home list.
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
  public void open(final String homeName) {
    // Check if requested home isn't already opened
    for (Home home : this.application.getHomes()) {
      if (homeName.equals(home.getName())) {
        String message = String.format(this.resource.getString("alreadyOpen"), homeName);
        ((HomePane)getView()).showMessage(message);
        return;
      }
    }
    
    // Read home in a threaded task
    Callable<Void> exportToObjTask = new Callable<Void>() {
          public Void call() throws RecorderException {
            // Read home with application recorder
            Home openedHome = application.getHomeRecorder().readHome(homeName);
            openedHome.setName(homeName); 
            addHomeToApplication(openedHome);
            return null;
          }
        };
    ThreadedTaskController.ExceptionHandler exceptionHandler = 
        new ThreadedTaskController.ExceptionHandler() {
          public void handleException(Exception ex) {
            if (!(ex instanceof InterruptedRecorderException)) {
              if (ex instanceof RecorderException) {
                String message = String.format(resource.getString("openError"), homeName);
                ((HomePane)getView()).showError(message);
              } else {
                ex.printStackTrace();
              }
            }
          }
        };
    new ThreadedTaskController(exportToObjTask, 
        this.resource.getString("openMessage"), exceptionHandler);
  }
  
  /**
   * Adds the given home to application.
   */
  private void addHomeToApplication(final Home home) {
    ((HomePane)getView()).invokeLater(new Runnable() {
        public void run() {
          application.addHome(home);
        }
      });
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
    close(null);
  }

  
  /**
   * Manages home close operation. If the home managed by this controller is modified,
   * this method will {@link HomePane#confirmSave(String) confirm} 
   * in view whether home should be saved. Once home is actually saved,
   * home is removed from application homes list and postCloseTask is called if
   * it's not <code>null</code>.
   */
  protected void close(final Runnable postCloseTask) {
    // Create a task that deletes home and run postCloseTask
    Runnable closeTask = new Runnable() {
        public void run() {
          application.deleteHome(home);
          if (postCloseTask != null) {
            postCloseTask.run();
          }
        }
      };
      
    if (this.home.isModified()) {
      switch (((HomePane)getView()).confirmSave(this.home.getName())) {
        case SAVE   : save(closeTask); // Falls through
        case CANCEL : return;
      }  
    }
    closeTask.run();
  }
  
  /**
   * Saves the home managed by this controller. If home name doesn't exist, 
   * this method will act as {@link #saveAs() saveAs} method.
   */
  public void save() {
    save(null);
  }

  /**
   * Saves the home managed by this controller and executes <code>postSaveTask</code> 
   * if it's not <code>null</code>.
   */
  private void save(Runnable postSaveTask) {
    if (this.home.getName() == null) {
      saveAs(postSaveTask);
    } else {
      save(this.home.getName(), postSaveTask);
    }
  }
  
  /**
   * Saves the home managed by this controller with a different name. 
   * This method displays a {@link HomePane#showSaveDialog(String) save dialog} in  view, 
   * and saves home with the chosen name if any. 
   */
  public void saveAs() {
    saveAs(null);
  }

  /**
   * Saves the home managed by this controller with a different name. 
   */
  private void saveAs(Runnable postSaveTask) {
    String newName = ((HomePane)getView()).showSaveDialog(this.home.getName());
    if (newName != null) {
      save(newName, postSaveTask);
    }
  }

  /**
   * Actually saves the home managed by this controller and executes <code>postSaveTask</code> 
   * if it's not <code>null</code>.
   */
  private void save(final String homeName, final Runnable postSaveTask) {
    // If home version is older than current version
    // or if home name is changed
    // or if user confirms to save a home created with a newer version
    if (this.home.getVersion() <= Home.CURRENT_VERSION
        || !homeName.equals(this.home.getName()) 
        || ((HomePane)getView()).confirmSaveNewerHome(homeName)) {
      // Save home in a threaded task
      Callable<Void> exportToObjTask = new Callable<Void>() {
            public Void call() throws RecorderException {
              // Write home with application recorder
              application.getHomeRecorder().writeHome(home, homeName);
              updateSavedHome(homeName, postSaveTask);
              return null;
            }
          };
      ThreadedTaskController.ExceptionHandler exceptionHandler = 
          new ThreadedTaskController.ExceptionHandler() {
            public void handleException(Exception ex) {
              if (!(ex instanceof InterruptedRecorderException)) {
                if (ex instanceof RecorderException) {
                  String message = String.format(resource.getString("saveError"), homeName);
                  ((HomePane)getView()).showError(message);
                } else {
                  ex.printStackTrace();
                }
              }
            }
          };
      new ThreadedTaskController(exportToObjTask, 
          this.resource.getString("saveMessage"), exceptionHandler);
    }
  }
  
  /**
   * Updates the saved home and executes <code>postSaveTask</code> 
   * if it's not <code>null</code>.
   */
  private void updateSavedHome(final String homeName,
                               final Runnable postSaveTask) {
    ((HomePane)getView()).invokeLater(new Runnable() {
        public void run() {
          home.setName(homeName);
          saveUndoLevel = 0;
          home.setModified(false);
          // Update recent homes list
          List<String> recentHomes = new ArrayList<String>(preferences.getRecentHomes());
          int homeNameIndex = recentHomes.indexOf(homeName);
          if (homeNameIndex >= 0) {
            recentHomes.remove(homeNameIndex);
          }
          recentHomes.add(0, homeName);
          updateUserPreferencesRecentHomes(recentHomes);
          
          if (postSaveTask != null) {
            postSaveTask.run();
          }
        }
      });
  }

  /**
   * Controls the export of the 3D view of current home to a OBJ file.
   */
  public void exportToOBJ() {
    final String objName = ((HomePane)getView()).showExportToOBJDialog(this.home.getName());    
    if (objName != null) {
      // Export 3D view in a threaded task
      Callable<Void> exportToObjTask = new Callable<Void>() {
            public Void call() throws RecorderException {
              getHomeController3D().exportToOBJ(objName);
              return null;
            }
          };
      ThreadedTaskController.ExceptionHandler exceptionHandler = 
          new ThreadedTaskController.ExceptionHandler() {
            public void handleException(Exception ex) {
              if (!(ex instanceof InterruptedRecorderException)) {
                if (ex instanceof RecorderException) {
                  String message = String.format(resource.getString("exportToOBJError"), objName);
                  ((HomePane)getView()).showError(message);
                } else {
                  ex.printStackTrace();
                }
              }
            }
          };
      new ThreadedTaskController(exportToObjTask, 
          this.resource.getString("exportToOBJMessage"), exceptionHandler);
    }
  }
  
  /**
   * Controls page setup.
   */
  public void setupPage() {
    new PageSetupController(this.home, this.undoSupport);
  }

  /**
   * Controls the print preview.
   */
  public void previewPrint() {
    new PrintPreviewController(this.home, this);
  }

  /**
   * Controls the print of this home.
   */
  public void print() {
    final Callable<Void> printTask = ((HomePane)getView()).showPrintDialog();    
    if (printTask != null) {
      // Print in a threaded task
      ThreadedTaskController.ExceptionHandler exceptionHandler = 
          new ThreadedTaskController.ExceptionHandler() {
            public void handleException(Exception ex) {
              if (!(ex instanceof InterruptedRecorderException)) {
                if (ex instanceof RecorderException) {
                  String message = String.format(resource.getString("printError"), home.getName());
                  ((HomePane)getView()).showError(message);
                } else {
                  ex.printStackTrace();
                }
              }
            }
          };
      new ThreadedTaskController(printTask, 
        this.resource.getString("printMessage"), exceptionHandler);      
    }
  }

  /**
   * Controls the print of this home in a PDF file.
   */
  public void printToPDF() {
    final String pdfName = ((HomePane)getView()).showPrintToPDFDialog(this.home.getName());    
    if (pdfName != null) {
      // Print to PDF in a threaded task
      Callable<Void> printToPdfTask = new Callable<Void>() {
          public Void call() throws RecorderException {
            ((HomePane)getView()).printToPDF(pdfName);
            return null;
          }
        };
      ThreadedTaskController.ExceptionHandler exceptionHandler = 
          new ThreadedTaskController.ExceptionHandler() {
            public void handleException(Exception ex) {
              if (!(ex instanceof InterruptedRecorderException)) {
                if (ex instanceof RecorderException) {
                  String message = String.format(resource.getString("printToPDFError"), pdfName);
                  ((HomePane)getView()).showError(message);
                } else {
                  ex.printStackTrace();
                }
              }
            }
          };
      new ThreadedTaskController(printToPdfTask, 
          this.resource.getString("printToPDFMessage"), exceptionHandler);
    }
  }

  /**
   * Controls application exit. If any home in application homes list is modified,
   * the user will be {@link HomePane#confirmExit() prompted} in view whether he wants
   * to discard his modifications ot not.  
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
      this.preferences.setLanguage(preferencesPanel.getLanguage());
      this.preferences.setUnit(preferencesPanel.getUnit());
      this.preferences.setMagnetismEnabled(preferencesPanel.isMagnetismEnabled());
      this.preferences.setRulersVisible(preferencesPanel.isRulersVisible());
      this.preferences.setGridVisible(preferencesPanel.isGridVisible());
      this.preferences.setNewWallThickness(preferencesPanel.getNewWallThickness());
      this.preferences.setNewWallHeight(preferencesPanel.getNewWallHeight());
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
   * Displays the wizard that helps to import home background image. 
   */
  public void importBackgroundImage() {
    new BackgroundImageWizardController(this.home, this.preferences, 
        this.contentManager, this.undoSupport);
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

  /**
   * Controls the change of value of a visual property in home.
   */
  public void setVisualProperty(String propertyName,
                                Object propertyValue) {
    this.home.setVisualProperty(propertyName, propertyValue);
  }
}
