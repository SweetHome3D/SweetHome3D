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
package com.eteks.sweethome3d.viewcontroller;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

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
import com.eteks.sweethome3d.model.CatalogDoorOrWindow;
import com.eteks.sweethome3d.model.CatalogLight;
import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.CatalogTexture;
import com.eteks.sweethome3d.model.CollectionEvent;
import com.eteks.sweethome3d.model.CollectionListener;
import com.eteks.sweethome3d.model.Compass;
import com.eteks.sweethome3d.model.DimensionLine;
import com.eteks.sweethome3d.model.DoorOrWindow;
import com.eteks.sweethome3d.model.FurnitureCatalog;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeApplication;
import com.eteks.sweethome3d.model.HomeDoorOrWindow;
import com.eteks.sweethome3d.model.HomeFurnitureGroup;
import com.eteks.sweethome3d.model.HomeLight;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.HomeRecorder;
import com.eteks.sweethome3d.model.InterruptedRecorderException;
import com.eteks.sweethome3d.model.Label;
import com.eteks.sweethome3d.model.Light;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.SelectionEvent;
import com.eteks.sweethome3d.model.SelectionListener;
import com.eteks.sweethome3d.model.TexturesCatalog;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.plugin.Plugin;
import com.eteks.sweethome3d.plugin.PluginManager;
import com.eteks.sweethome3d.viewcontroller.PlanController.Mode;

/**
 * A MVC controller for the home view.
 * @author Emmanuel Puybaret
 */
public class HomeController implements Controller {
  private final Home                  home;
  private final UserPreferences       preferences;
  private final HomeApplication       application;
  private final ViewFactory           viewFactory;
  private final ContentManager        contentManager;
  private final PluginManager         pluginManager;
  private final UndoableEditSupport   undoSupport;
  private final UndoManager           undoManager;
  private HomeView                    homeView;
  private FurnitureCatalogController  furnitureCatalogController;
  private FurnitureController         furnitureController;
  private PlanController              planController;
  private HomeController3D            homeController3D;
  private static HelpController       helpController;  // Only one help controller
  private int                         saveUndoLevel;
  private View                        focusedView;

  /**
   * Creates the controller of home view.
   * @param home the home edited by this controller and its view.
   * @param application the instance of current application.
   * @param viewFactory a factory able to create views.
   * @param contentManager the content manager of the application.
   * @param pluginManager  the plug-in manager of the application.
   */
  public HomeController(Home home, 
                        HomeApplication application,
                        ViewFactory    viewFactory, 
                        ContentManager contentManager, 
                        PluginManager pluginManager) {
    this(home, application.getUserPreferences(), viewFactory, 
        contentManager, application, pluginManager);
  }

  /**
   * Creates the controller of home view.
   * @param home the home edited by this controller and its view.
   * @param application the instance of current application.
   * @param viewFactory a factory able to create views.
   */
  public HomeController(Home home, 
                        HomeApplication application,
                        ViewFactory viewFactory) {
    this(home, application.getUserPreferences(), viewFactory, null, application, null);
  }

  /**
   * Creates the controller of home view. 
   * @param home        the home edited by this controller and its view.
   * @param preferences the preferences of the application.
   * @param viewFactory a factory able to create views.
   */
  public HomeController(Home home, 
                        UserPreferences preferences,
                        ViewFactory viewFactory) {
    this(home, preferences, viewFactory, null, null, null);
  }

  /**
   * Creates the controller of home view. 
   * @param home        the home edited by this controller and its view.
   * @param preferences the preferences of the application.
   * @param viewFactory a factory able to create views.
   * @param contentManager the content manager of the application.
   */
  public HomeController(Home home, 
                        UserPreferences preferences,
                        ViewFactory    viewFactory,
                        ContentManager contentManager) {
    this(home, preferences, viewFactory, contentManager, null, null);
  }

  private HomeController(final Home home, 
                         final UserPreferences preferences,
                         ViewFactory    viewFactory,
                         ContentManager contentManager,
                         HomeApplication application,
                         PluginManager pluginManager) {
    this.home = home;
    this.preferences = preferences;
    this.viewFactory = viewFactory;
    this.contentManager = contentManager;
    this.application = application;
    this.pluginManager = pluginManager;
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
    
    // Update recent homes list
    if (home.getName() != null) {
      List<String> recentHomes = new ArrayList<String>(this.preferences.getRecentHomes());
      recentHomes.remove(home.getName());
      recentHomes.add(0, home.getName());
      updateUserPreferencesRecentHomes(recentHomes);
      
      // If home version is more recent than current version
      if (home.getVersion() > Home.CURRENT_VERSION) {
        // Warn the user that view will display a home created with a more recent version 
        getView().invokeLater(new Runnable() { 
            public void run() {
              String message = preferences.getLocalizedString(HomeController.class, 
                  "moreRecentVersionHome", home.getName());
              getView().showMessage(message);
            }
          });
      }
    }
  }

  /**
   * Enables actions at controller instantiation. 
   */
  private void enableDefaultActions(HomeView homeView) {
    boolean applicationExists = this.application != null;
    
    homeView.setEnabled(HomeView.ActionType.NEW_HOME, applicationExists);
    homeView.setEnabled(HomeView.ActionType.OPEN, applicationExists);
    homeView.setEnabled(HomeView.ActionType.DELETE_RECENT_HOMES, 
        applicationExists && !this.preferences.getRecentHomes().isEmpty());
    homeView.setEnabled(HomeView.ActionType.CLOSE, applicationExists);
    homeView.setEnabled(HomeView.ActionType.SAVE, applicationExists);
    homeView.setEnabled(HomeView.ActionType.SAVE_AS, applicationExists);
    homeView.setEnabled(HomeView.ActionType.SAVE_AND_COMPRESS, applicationExists);
    homeView.setEnabled(HomeView.ActionType.PAGE_SETUP, true);
    homeView.setEnabled(HomeView.ActionType.PRINT_PREVIEW, true);
    homeView.setEnabled(HomeView.ActionType.PRINT, true);
    homeView.setEnabled(HomeView.ActionType.PRINT_TO_PDF, true);
    homeView.setEnabled(HomeView.ActionType.PREFERENCES, true);
    homeView.setEnabled(HomeView.ActionType.EXIT, applicationExists);
    homeView.setEnabled(HomeView.ActionType.IMPORT_FURNITURE, true);
    homeView.setEnabled(HomeView.ActionType.IMPORT_FURNITURE_LIBRARY, true);
    homeView.setEnabled(HomeView.ActionType.IMPORT_TEXTURES_LIBRARY, true);
    homeView.setEnabled(HomeView.ActionType.SORT_HOME_FURNITURE_BY_CATALOG_ID, true);
    homeView.setEnabled(HomeView.ActionType.SORT_HOME_FURNITURE_BY_NAME, true);
    homeView.setEnabled(HomeView.ActionType.SORT_HOME_FURNITURE_BY_WIDTH, true);
    homeView.setEnabled(HomeView.ActionType.SORT_HOME_FURNITURE_BY_HEIGHT, true);
    homeView.setEnabled(HomeView.ActionType.SORT_HOME_FURNITURE_BY_DEPTH, true);
    homeView.setEnabled(HomeView.ActionType.SORT_HOME_FURNITURE_BY_X, true);
    homeView.setEnabled(HomeView.ActionType.SORT_HOME_FURNITURE_BY_Y, true);
    homeView.setEnabled(HomeView.ActionType.SORT_HOME_FURNITURE_BY_ELEVATION, true);
    homeView.setEnabled(HomeView.ActionType.SORT_HOME_FURNITURE_BY_ANGLE, true);
    homeView.setEnabled(HomeView.ActionType.SORT_HOME_FURNITURE_BY_COLOR, true);
    homeView.setEnabled(HomeView.ActionType.SORT_HOME_FURNITURE_BY_TEXTURE, true);
    homeView.setEnabled(HomeView.ActionType.SORT_HOME_FURNITURE_BY_MOVABILITY, true);
    homeView.setEnabled(HomeView.ActionType.SORT_HOME_FURNITURE_BY_TYPE, true);
    homeView.setEnabled(HomeView.ActionType.SORT_HOME_FURNITURE_BY_VISIBILITY, true);
    homeView.setEnabled(HomeView.ActionType.SORT_HOME_FURNITURE_BY_PRICE, true);
    homeView.setEnabled(HomeView.ActionType.SORT_HOME_FURNITURE_BY_VALUE_ADDED_TAX_PERCENTAGE, true);
    homeView.setEnabled(HomeView.ActionType.SORT_HOME_FURNITURE_BY_VALUE_ADDED_TAX, true);
    homeView.setEnabled(HomeView.ActionType.SORT_HOME_FURNITURE_BY_PRICE_VALUE_ADDED_TAX_INCLUDED, true);
    homeView.setEnabled(HomeView.ActionType.SORT_HOME_FURNITURE_BY_DESCENDING_ORDER, 
        this.home.getFurnitureSortedProperty() != null);
    homeView.setEnabled(HomeView.ActionType.DISPLAY_HOME_FURNITURE_CATALOG_ID, true); 
    homeView.setEnabled(HomeView.ActionType.DISPLAY_HOME_FURNITURE_NAME, true); 
    homeView.setEnabled(HomeView.ActionType.DISPLAY_HOME_FURNITURE_WIDTH, true); 
    homeView.setEnabled(HomeView.ActionType.DISPLAY_HOME_FURNITURE_DEPTH, true); 
    homeView.setEnabled(HomeView.ActionType.DISPLAY_HOME_FURNITURE_HEIGHT, true); 
    homeView.setEnabled(HomeView.ActionType.DISPLAY_HOME_FURNITURE_X, true); 
    homeView.setEnabled(HomeView.ActionType.DISPLAY_HOME_FURNITURE_Y, true); 
    homeView.setEnabled(HomeView.ActionType.DISPLAY_HOME_FURNITURE_ELEVATION, true); 
    homeView.setEnabled(HomeView.ActionType.DISPLAY_HOME_FURNITURE_ANGLE, true); 
    homeView.setEnabled(HomeView.ActionType.DISPLAY_HOME_FURNITURE_COLOR, true); 
    homeView.setEnabled(HomeView.ActionType.DISPLAY_HOME_FURNITURE_TEXTURE, true); 
    homeView.setEnabled(HomeView.ActionType.DISPLAY_HOME_FURNITURE_MOVABLE, true); 
    homeView.setEnabled(HomeView.ActionType.DISPLAY_HOME_FURNITURE_DOOR_OR_WINDOW, true); 
    homeView.setEnabled(HomeView.ActionType.DISPLAY_HOME_FURNITURE_VISIBLE, true);
    homeView.setEnabled(HomeView.ActionType.DISPLAY_HOME_FURNITURE_PRICE, true);
    homeView.setEnabled(HomeView.ActionType.DISPLAY_HOME_FURNITURE_VALUE_ADDED_TAX_PERCENTAGE, true);
    homeView.setEnabled(HomeView.ActionType.DISPLAY_HOME_FURNITURE_VALUE_ADDED_TAX, true);
    homeView.setEnabled(HomeView.ActionType.DISPLAY_HOME_FURNITURE_PRICE_VALUE_ADDED_TAX_INCLUDED, true);
    homeView.setEnabled(HomeView.ActionType.SELECT, true);
    homeView.setEnabled(HomeView.ActionType.PAN, true);
    homeView.setEnabled(HomeView.ActionType.CREATE_WALLS, true);
    homeView.setEnabled(HomeView.ActionType.CREATE_ROOMS, true);
    homeView.setEnabled(HomeView.ActionType.CREATE_DIMENSION_LINES, true);
    homeView.setEnabled(HomeView.ActionType.CREATE_LABELS, true);
    homeView.setEnabled(HomeView.ActionType.LOCK_BASE_PLAN, true);
    homeView.setEnabled(HomeView.ActionType.UNLOCK_BASE_PLAN, true);
    homeView.setEnabled(HomeView.ActionType.MODIFY_COMPASS, true);
    homeView.setEnabled(HomeView.ActionType.IMPORT_BACKGROUND_IMAGE, true);
    BackgroundImage backgroundImage = this.home.getBackgroundImage();
    boolean homeHasBackgroundImage = backgroundImage != null;
    homeView.setEnabled(HomeView.ActionType.MODIFY_BACKGROUND_IMAGE, homeHasBackgroundImage);
    homeView.setEnabled(HomeView.ActionType.HIDE_BACKGROUND_IMAGE, 
        homeHasBackgroundImage && backgroundImage.isVisible());
    homeView.setEnabled(HomeView.ActionType.SHOW_BACKGROUND_IMAGE, 
        homeHasBackgroundImage && !backgroundImage.isVisible());
    homeView.setEnabled(HomeView.ActionType.DELETE_BACKGROUND_IMAGE, homeHasBackgroundImage);
    homeView.setEnabled(HomeView.ActionType.ZOOM_IN, true);
    homeView.setEnabled(HomeView.ActionType.ZOOM_OUT, true);
    homeView.setEnabled(HomeView.ActionType.EXPORT_TO_SVG, true); 
    homeView.setEnabled(HomeView.ActionType.VIEW_FROM_TOP, true);
    homeView.setEnabled(HomeView.ActionType.VIEW_FROM_OBSERVER, true);
    homeView.setEnabled(HomeView.ActionType.STORE_POINT_OF_VIEW, true);
    homeView.setEnabled(HomeView.ActionType.DETACH_3D_VIEW, true);
    homeView.setEnabled(HomeView.ActionType.ATTACH_3D_VIEW, true);
    homeView.setEnabled(HomeView.ActionType.VIEW_FROM_OBSERVER, true);
    homeView.setEnabled(HomeView.ActionType.MODIFY_3D_ATTRIBUTES, true);
    homeView.setEnabled(HomeView.ActionType.CREATE_PHOTO, true);
    homeView.setEnabled(HomeView.ActionType.CREATE_VIDEO, true);
    homeView.setEnabled(HomeView.ActionType.EXPORT_TO_OBJ, true);
    homeView.setEnabled(HomeView.ActionType.HELP, true);
    homeView.setEnabled(HomeView.ActionType.ABOUT, true);
    homeView.setTransferEnabled(true);
  }

  /**
   * Returns the view associated with this controller.
   */
  public HomeView getView() {
    if (this.homeView == null) {
      this.homeView = this.viewFactory.createHomeView(this.home, this.preferences, this);
      enableDefaultActions(this.homeView);
      addListeners();
    }
    return this.homeView;
  }

  /**
   * Returns the plug-ins available with this controller.
   */
  public List<Plugin> getPlugins() {
    if (this.application != null && this.pluginManager != null) {
      // Retrieve home plug-ins
      return this.pluginManager.getPlugins(
          this.application, this.home, this.preferences, getUndoableEditSupport());
    } else {
      List<Plugin> plugins = Collections.emptyList();
      return plugins;
    }
  }

  /**
   * Returns the content manager of this controller.
   */
  public ContentManager getContentManager() {
    return this.contentManager;
  }

  /**
   * Returns the furniture catalog controller managed by this controller.
   */
  public FurnitureCatalogController getFurnitureCatalogController() {
    // Create sub controller lazily only once it's needed
    if (this.furnitureCatalogController == null) {
      this.furnitureCatalogController = new FurnitureCatalogController(
          this.preferences.getFurnitureCatalog(), this.preferences, this.viewFactory, this.contentManager);
    }
    return this.furnitureCatalogController;
  }

  /**
   * Returns the furniture controller managed by this controller.
   */
  public FurnitureController getFurnitureController() {
    // Create sub controller lazily only once it's needed
    if (this.furnitureController == null) {
      this.furnitureController = new FurnitureController(
          this.home, this.preferences, this.viewFactory, this.contentManager, getUndoableEditSupport());
    }
    return this.furnitureController;
  }

  /**
   * Returns the controller of home plan.
   */
  public PlanController getPlanController() {
    // Create sub controller lazily only once it's needed
    if (this.planController == null) {
      this.planController = new PlanController(
          this.home, this.preferences, this.viewFactory, this.contentManager, getUndoableEditSupport());
    }
    return this.planController;
  }

  /**
   * Returns the controller of home 3D view.
   */
  public HomeController3D getHomeController3D() {
    // Create sub controller lazily only once it's needed
    if (this.homeController3D == null) {
      this.homeController3D = new HomeController3D(
          this.home, this.preferences, this.viewFactory, this.contentManager, getUndoableEditSupport());
    }
    return this.homeController3D;
  }

  /**
   * Returns the undoable edit support managed by this controller.
   */
  protected final UndoableEditSupport getUndoableEditSupport() {
    return this.undoSupport;
  }
  
  /**
   * Adds listeners that updates the enabled / disabled state of actions.
   */
  private void addListeners() {
    // Save preferences when they change
    this.preferences.getFurnitureCatalog().addFurnitureListener(
        new FurnitureCatalogChangeListener(this));
    this.preferences.getTexturesCatalog().addTexturesListener(
        new TexturesCatalogChangeListener(this));
    UserPreferencesPropertiesChangeListener listener = 
        new UserPreferencesPropertiesChangeListener(this);
    for (UserPreferences.Property property : UserPreferences.Property.values()) {
      this.preferences.addPropertyChangeListener(property, listener);
    }
      
    addCatalogSelectionListener();
    addHomeBackgroundImageListener();
    addHomeSelectionListener();
    addFurnitureSortListener();
    addUndoSupportListener();
    addHomeItemsListener();
    addPlanControllerListeners();
    addLanguageListener();
  }

  /**
   * Super class of catalog listeners that writes preferences each time a piece of furniture or a texture
   * is deleted or added in furniture or textures catalog.
   */
  private abstract static class UserPreferencesChangeListener {
    // Stores the currently writing preferences 
    private static Set<UserPreferences> writingPreferences = new HashSet<UserPreferences>();
    
    public void writePreferences(final HomeController controller) {
      if (!writingPreferences.contains(controller.preferences)) {
        writingPreferences.add(controller.preferences);
        // Write preferences later once all catalog modifications are notified 
        controller.getView().invokeLater(new Runnable() {
            public void run() {
              try {
                controller.preferences.write();
                writingPreferences.remove(controller.preferences);
              } catch (RecorderException ex) {
                controller.getView().showError(controller.preferences.getLocalizedString(
                    HomeController.class, "savePreferencesError"));
              }
            }
          });
      }
    }
  }

  /**
   * Furniture catalog listener that writes preferences each time a piece of furniture 
   * is deleted or added in furniture catalog. This listener is bound to this controller 
   * with a weak reference to avoid strong link between catalog and this controller.  
   */
  private static class FurnitureCatalogChangeListener extends UserPreferencesChangeListener 
                                                      implements CollectionListener<CatalogPieceOfFurniture> {
    private WeakReference<HomeController> homeController;
    
    public FurnitureCatalogChangeListener(HomeController homeController) {
      this.homeController = new WeakReference<HomeController>(homeController);
    }
    
    public void collectionChanged(CollectionEvent<CatalogPieceOfFurniture> ev) {
      // If controller was garbage collected, remove this listener from catalog
      final HomeController controller = this.homeController.get();
      if (controller == null) {
        ((FurnitureCatalog)ev.getSource()).removeFurnitureListener(this);
      } else {
        writePreferences(controller);
      }
    }
  }

  /**
   * Textures catalog listener that writes preferences each time a texture 
   * is deleted or added in textures catalog. This listener is bound to this controller 
   * with a weak reference to avoid strong link between catalog and this controller.  
   */
  private static class TexturesCatalogChangeListener extends UserPreferencesChangeListener
                                                     implements CollectionListener<CatalogTexture> { 
    private WeakReference<HomeController> homeController;
    
    public TexturesCatalogChangeListener(HomeController homeController) {
      this.homeController = new WeakReference<HomeController>(homeController);
    }
    
    public void collectionChanged(CollectionEvent<CatalogTexture> ev) {
      // If controller was garbage collected, remove this listener from catalog
      final HomeController controller = this.homeController.get();
      if (controller == null) {
        ((TexturesCatalog)ev.getSource()).removeTexturesListener(this);
      } else {
        writePreferences(controller);
      }
    }
  }

  /**
   * Properties listener that writes preferences each time the value of one of its properties changes. 
   * This listener is bound to this controller with a weak reference to avoid strong link 
   * between catalog and this controller.  
   */
  private static class UserPreferencesPropertiesChangeListener extends UserPreferencesChangeListener
                                                               implements PropertyChangeListener { 
    private WeakReference<HomeController> homeController;
    
    public UserPreferencesPropertiesChangeListener(HomeController homeController) {
      this.homeController = new WeakReference<HomeController>(homeController);
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
      // If controller was garbage collected, remove this listener from catalog
      final HomeController controller = this.homeController.get();
      if (controller == null) {
        ((UserPreferences)ev.getSource()).removePropertyChangeListener(
            UserPreferences.Property.valueOf(ev.getPropertyName()), this);
      } else {
        writePreferences(controller);
      }
    }
  }

  /**
   * Adds a selection listener to catalog that enables / disables Add Furniture action.
   */
  private void addCatalogSelectionListener() {
    getFurnitureCatalogController().addSelectionListener(new SelectionListener() {
          public void selectionChanged(SelectionEvent ev) {
            enableActionsBoundToSelection();
          }
        });
  }

  /**
   * Adds a property change listener to <code>preferences</code> to update
   * undo and redo presentation names when preferred language changes.
   */
  private void addLanguageListener() {
    this.preferences.addPropertyChangeListener(UserPreferences.Property.LANGUAGE, 
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
        // Update undo and redo name
        homeController.getView().setUndoRedoName(
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
          enableActionsBoundToSelection();
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
            getView().setEnabled(HomeView.ActionType.SORT_HOME_FURNITURE_BY_DESCENDING_ORDER, 
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
              BackgroundImage backgroundImage = (BackgroundImage)ev.getNewValue();
              boolean homeHasBackgroundImage = backgroundImage != null;
              getView().setEnabled(HomeView.ActionType.MODIFY_BACKGROUND_IMAGE, homeHasBackgroundImage);
              getView().setEnabled(HomeView.ActionType.HIDE_BACKGROUND_IMAGE, 
                  homeHasBackgroundImage && backgroundImage.isVisible());
              getView().setEnabled(HomeView.ActionType.SHOW_BACKGROUND_IMAGE, 
                  homeHasBackgroundImage && !backgroundImage.isVisible());
              getView().setEnabled(HomeView.ActionType.DELETE_BACKGROUND_IMAGE, homeHasBackgroundImage);
            }
          });
    }
  }

  /**
   * Enables or disables action bound to selection. 
   * This method will be called when selection in plan or in catalog changes and when 
   * focused component or modification state in plan changes. 
   */
  protected void enableActionsBoundToSelection() {
    boolean modificationState = getPlanController().isModificationState();
    
    // Search if catalog selection contains at least one piece
    List<CatalogPieceOfFurniture> catalogSelectedItems = 
        getFurnitureCatalogController().getSelectedFurniture();    
    boolean catalogSelectionContainsFurniture = !catalogSelectedItems.isEmpty();
    boolean catalogSelectionContainsOneModifiablePiece = catalogSelectedItems.size() == 1
        && catalogSelectedItems.get(0).isModifiable();
    
    // Search if home selection contains at least one piece, one wall or one dimension line
    List<Selectable> selectedItems = this.home.getSelectedItems();
    boolean homeSelectionContainsDeletableItems = false;
    boolean homeSelectionContainsFurniture = false;
    boolean homeSelectionContainsDeletableFurniture = false;
    boolean homeSelectionContainsOneCopiableItemOrMore = false;
    boolean homeSelectionContainsTwoMovablePiecesOfFurnitureOrMore = false;
    boolean homeSelectionContainsFurnitureGroup = false;
    boolean homeSelectionContainsWalls = false;
    boolean homeSelectionContainsRooms = false;
    boolean homeSelectionContainsOneWall = false;
    boolean homeSelectionContainsOneLabel = false;
    boolean homeSelectionContainsItemsWithText = false;
    boolean homeSelectionContainsCompass = false;
    FurnitureController furnitureController = getFurnitureController();
    if (!modificationState) {
      for (Selectable item : selectedItems) {
        if (getPlanController().isItemDeletable(item)) {
          homeSelectionContainsDeletableItems = true;
          break;
        }
      }
      List<HomePieceOfFurniture> selectedFurniture = Home.getFurnitureSubList(selectedItems);
      homeSelectionContainsFurniture = !selectedFurniture.isEmpty();
      for (HomePieceOfFurniture piece : selectedFurniture) {
        if (furnitureController.isPieceOfFurnitureDeletable(piece)) {
          homeSelectionContainsDeletableFurniture = true;
          break;
        }
      }
      for (HomePieceOfFurniture piece : selectedFurniture) {
        if (piece instanceof HomeFurnitureGroup) {
          homeSelectionContainsFurnitureGroup = true;
          break;
        }
      }
      int movablePiecesOfFurnitureCount = 0;
      for (HomePieceOfFurniture piece : selectedFurniture) {
        if (furnitureController.isPieceOfFurnitureMovable(piece)) {
          movablePiecesOfFurnitureCount++;
          if (movablePiecesOfFurnitureCount >= 2) {
            homeSelectionContainsTwoMovablePiecesOfFurnitureOrMore = true;
            break;
          }
        }
      }
      List<Wall> selectedWalls = Home.getWallsSubList(selectedItems);
      homeSelectionContainsWalls = !selectedWalls.isEmpty();
      homeSelectionContainsOneWall = selectedWalls.size() == 1;
      homeSelectionContainsRooms = !Home.getRoomsSubList(selectedItems).isEmpty();
      boolean homeSelectionContainsDimensionLines = !Home.getDimensionLinesSubList(selectedItems).isEmpty();
      final List<Label> selectedLabels = Home.getLabelsSubList(selectedItems);
      boolean homeSelectionContainsLabels = !selectedLabels.isEmpty();
      homeSelectionContainsCompass = selectedItems.contains(this.home.getCompass());
      homeSelectionContainsOneLabel = selectedLabels.size() == 1;
      homeSelectionContainsOneCopiableItemOrMore = 
          homeSelectionContainsFurniture || homeSelectionContainsWalls 
          || homeSelectionContainsRooms || homeSelectionContainsDimensionLines
          || homeSelectionContainsLabels || homeSelectionContainsCompass; 
      homeSelectionContainsItemsWithText = 
          homeSelectionContainsFurniture || homeSelectionContainsRooms 
          || homeSelectionContainsDimensionLines || homeSelectionContainsLabels;
    }

    HomeView view = getView();
    if (this.focusedView == getFurnitureCatalogController().getView()) {
      view.setEnabled(HomeView.ActionType.COPY,
          !modificationState && catalogSelectionContainsFurniture);
      view.setEnabled(HomeView.ActionType.CUT, false);
      view.setEnabled(HomeView.ActionType.DELETE, false);
      for (CatalogPieceOfFurniture piece : catalogSelectedItems) {
        if (piece.isModifiable()) {
          // Only modifiable catalog furniture may be deleted
          view.setEnabled(HomeView.ActionType.DELETE, true);
          break;
        }
      }
    } else if (this.focusedView == furnitureController.getView()) {
      view.setEnabled(HomeView.ActionType.COPY, homeSelectionContainsFurniture);
      view.setEnabled(HomeView.ActionType.CUT, homeSelectionContainsDeletableFurniture);
      view.setEnabled(HomeView.ActionType.DELETE, homeSelectionContainsDeletableFurniture);
    } else if (this.focusedView == getPlanController().getView()) {
      view.setEnabled(HomeView.ActionType.COPY, homeSelectionContainsOneCopiableItemOrMore);
      view.setEnabled(HomeView.ActionType.CUT, homeSelectionContainsDeletableItems);
      view.setEnabled(HomeView.ActionType.DELETE, homeSelectionContainsDeletableItems);
    } else {
      view.setEnabled(HomeView.ActionType.COPY, false);
      view.setEnabled(HomeView.ActionType.CUT, false);
      view.setEnabled(HomeView.ActionType.DELETE, false);
    }

    view.setEnabled(HomeView.ActionType.ADD_HOME_FURNITURE, catalogSelectionContainsFurniture);
    // In creation mode all actions bound to selection are disabled
    view.setEnabled(HomeView.ActionType.DELETE_HOME_FURNITURE,
        homeSelectionContainsDeletableFurniture);
    view.setEnabled(HomeView.ActionType.DELETE_SELECTION,
        (catalogSelectionContainsFurniture
            && this.focusedView == getFurnitureCatalogController().getView())
        || (homeSelectionContainsDeletableItems 
            && (this.focusedView == furnitureController.getView()
                || this.focusedView == getPlanController().getView()
                || this.focusedView == getHomeController3D().getView())));
    view.setEnabled(HomeView.ActionType.MODIFY_FURNITURE,
        (catalogSelectionContainsOneModifiablePiece
             && this.focusedView == getFurnitureCatalogController().getView())
        || (homeSelectionContainsFurniture 
             && (this.focusedView == furnitureController.getView()
                 || this.focusedView == getPlanController().getView()
                 || this.focusedView == getHomeController3D().getView())));
    view.setEnabled(HomeView.ActionType.MODIFY_WALL,
        homeSelectionContainsWalls);
    view.setEnabled(HomeView.ActionType.REVERSE_WALL_DIRECTION,
        homeSelectionContainsWalls);
    view.setEnabled(HomeView.ActionType.SPLIT_WALL,
        homeSelectionContainsOneWall);
    view.setEnabled(HomeView.ActionType.MODIFY_ROOM,
        homeSelectionContainsRooms);
    view.setEnabled(HomeView.ActionType.MODIFY_LABEL,
        homeSelectionContainsOneLabel);
    view.setEnabled(HomeView.ActionType.TOGGLE_BOLD_STYLE, 
        homeSelectionContainsItemsWithText);
    view.setEnabled(HomeView.ActionType.TOGGLE_ITALIC_STYLE, 
        homeSelectionContainsItemsWithText);
    view.setEnabled(HomeView.ActionType.INCREASE_TEXT_SIZE, 
        homeSelectionContainsItemsWithText);
    view.setEnabled(HomeView.ActionType.DECREASE_TEXT_SIZE, 
        homeSelectionContainsItemsWithText);
    view.setEnabled(HomeView.ActionType.ALIGN_FURNITURE_ON_TOP,
        homeSelectionContainsTwoMovablePiecesOfFurnitureOrMore);
    view.setEnabled(HomeView.ActionType.ALIGN_FURNITURE_ON_BOTTOM,
        homeSelectionContainsTwoMovablePiecesOfFurnitureOrMore);
    view.setEnabled(HomeView.ActionType.ALIGN_FURNITURE_ON_LEFT,
        homeSelectionContainsTwoMovablePiecesOfFurnitureOrMore);
    view.setEnabled(HomeView.ActionType.ALIGN_FURNITURE_ON_RIGHT,
        homeSelectionContainsTwoMovablePiecesOfFurnitureOrMore);
    view.setEnabled(HomeView.ActionType.GROUP_FURNITURE,
        homeSelectionContainsTwoMovablePiecesOfFurnitureOrMore);
    view.setEnabled(HomeView.ActionType.UNGROUP_FURNITURE,
        homeSelectionContainsFurnitureGroup);
  }

  /**
   * Enables clipboard paste action if clipboard isn't empty.
   */
  public void enablePasteAction() {
    HomeView view = getView();
    if (this.focusedView == getFurnitureController().getView()
        || this.focusedView == getPlanController().getView()) {
      view.setEnabled(HomeView.ActionType.PASTE,
          !getPlanController().isModificationState() && !view.isClipboardEmpty());
    } else {
      view.setEnabled(HomeView.ActionType.PASTE, false);
    }
  }

  /**
   * Enables select all action if home isn't empty.
   */
  protected void enableSelectAllAction() {
    HomeView view = getView();
    boolean modificationState = getPlanController().isModificationState();
    if (this.focusedView == getFurnitureController().getView()) {
      view.setEnabled(HomeView.ActionType.SELECT_ALL,
          !modificationState 
          && this.home.getFurniture().size() > 0);
    } else if (this.focusedView == getPlanController().getView()
               || this.focusedView == getHomeController3D().getView()) {
      boolean homeContainsOneSelectableItemOrMore = !this.home.isEmpty()
          || this.home.getCompass().isVisible();
      view.setEnabled(HomeView.ActionType.SELECT_ALL,
          !modificationState && homeContainsOneSelectableItemOrMore);
    } else {
      view.setEnabled(HomeView.ActionType.SELECT_ALL, false);
    }
  }

  /**
   * Enables zoom actions depending on current scale.
   */
  private void enableZoomActions() {
    PlanController planController = getPlanController();
    float scale = planController.getScale();
    HomeView view = getView();
    view.setEnabled(HomeView.ActionType.ZOOM_IN, scale < planController.getMaximumScale());
    view.setEnabled(HomeView.ActionType.ZOOM_OUT, scale > planController.getMinimumScale());    
  }
  
  /**
   * Adds undoable edit listener to undo support that enables Undo action.
   */
  private void addUndoSupportListener() {
    getUndoableEditSupport().addUndoableEditListener(
      new UndoableEditListener () {
        public void undoableEditHappened(UndoableEditEvent ev) {
          HomeView view = getView();
          view.setEnabled(HomeView.ActionType.UNDO, 
              !getPlanController().isModificationState());
          view.setEnabled(HomeView.ActionType.REDO, false);
          view.setUndoRedoName(ev.getEdit().getUndoPresentationName(), null);
          saveUndoLevel++;
          home.setModified(true);
        }
      });
  }

  /**
   * Adds a furniture listener to home that enables / disables actions on furniture list change.
   */
  @SuppressWarnings("unchecked")
  private void addHomeItemsListener() {
    CollectionListener homeItemsListener = 
        new CollectionListener() {
          public void collectionChanged(CollectionEvent ev) {
            if (ev.getType() == CollectionEvent.Type.ADD 
                || ev.getType() == CollectionEvent.Type.DELETE) {
              enableSelectAllAction();
            }
          }
        };
    this.home.addFurnitureListener((CollectionListener<HomePieceOfFurniture>)homeItemsListener);
    this.home.addWallsListener((CollectionListener<Wall>)homeItemsListener);
    this.home.addRoomsListener((CollectionListener<Room>)homeItemsListener);
    this.home.addDimensionLinesListener((CollectionListener<DimensionLine>)homeItemsListener);
    this.home.addLabelsListener((CollectionListener<Label>)homeItemsListener);
    this.home.getCompass().addPropertyChangeListener(new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          if (Compass.Property.VISIBLE.equals(ev.getPropertyName())) {
            enableSelectAllAction();
          }
        }
      });
  }

  /**
   * Adds a property change listener to plan controller to 
   * enable/disable authorized actions according to its modification state and the plan scale.
   */
  private void addPlanControllerListeners() {
    getPlanController().addPropertyChangeListener(PlanController.Property.MODIFICATION_STATE, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            enableActionsBoundToSelection();
            enableSelectAllAction();
            HomeView view = getView();
            if (getPlanController().isModificationState()) {
              view.setEnabled(HomeView.ActionType.PASTE, false);
              view.setEnabled(HomeView.ActionType.UNDO, false);
              view.setEnabled(HomeView.ActionType.REDO, false);
            } else {
              enablePasteAction();
              view.setEnabled(HomeView.ActionType.UNDO, undoManager.canUndo());
              view.setEnabled(HomeView.ActionType.REDO, undoManager.canRedo());
            }
          }
        });
    getPlanController().addPropertyChangeListener(PlanController.Property.SCALE, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            enableZoomActions();
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
      getFurnitureCatalogController().getSelectedFurniture();
    if (!selectedFurniture.isEmpty()) {
      List<HomePieceOfFurniture> newFurniture = 
          new ArrayList<HomePieceOfFurniture>();
      for (CatalogPieceOfFurniture piece : selectedFurniture) {
        HomePieceOfFurniture homePiece;
        if (piece instanceof CatalogDoorOrWindow) {
          homePiece = new HomeDoorOrWindow((DoorOrWindow)piece);
        } else if (piece instanceof CatalogLight) {
          homePiece = new HomeLight((Light)piece);
        } else {
          homePiece = new HomePieceOfFurniture(piece);
        }
        // If magnetism is enabled, adjust piece size and elevation
        if (this.preferences.isMagnetismEnabled()) {
          if (homePiece.isResizable()) {
            homePiece.setWidth(this.preferences.getLengthUnit().getMagnetizedLength(homePiece.getWidth(), 0.1f));
            homePiece.setDepth(this.preferences.getLengthUnit().getMagnetizedLength(homePiece.getDepth(), 0.1f));
            homePiece.setHeight(this.preferences.getLengthUnit().getMagnetizedLength(homePiece.getHeight(), 0.1f));
          }
          homePiece.setElevation(this.preferences.getLengthUnit().getMagnetizedLength(homePiece.getElevation(), 0.1f));
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
    if (this.focusedView == getFurnitureCatalogController().getView()) {
      getFurnitureCatalogController().modifySelectedFurniture();
    } else if (this.focusedView == getFurnitureController().getView()
               || this.focusedView == getPlanController().getView()
               || this.focusedView == getHomeController3D().getView()) {
      getFurnitureController().modifySelectedFurniture();
    }    
  }
  
  /**
   * Imports a given language library. 
   */
  public void importLanguageLibrary(String languageLibraryName) {
    try {
      if (!this.preferences.languageLibraryExists(languageLibraryName) 
          || getView().confirmReplaceLanguageLibrary(languageLibraryName)) {
        this.preferences.addLanguageLibrary(languageLibraryName);
      }
    } catch (RecorderException ex) {
      String message = this.preferences.getLocalizedString(HomeController.class, 
          "importLanguageLibraryError", languageLibraryName);
      getView().showError(message);
    }
  }

  /**
   * Imports furniture to the catalog or home depending on the focused view.  
   */
  public void importFurniture() {
    // Always use selection mode after an import furniture operation
    getPlanController().setMode(PlanController.Mode.SELECTION);
    if (this.focusedView == getFurnitureCatalogController().getView()) {
      getFurnitureCatalogController().importFurniture();
    } else {
      getFurnitureController().importFurniture();
    }    
  }

  /**
   * Imports a furniture library chosen by the user.  
   */
  public void importFurnitureLibrary() {
    getView().invokeLater(new Runnable() {
        public void run() {
          final String furnitureLibraryName = getView().showImportFurnitureLibraryDialog();
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
          || getView().confirmReplaceFurnitureLibrary(furnitureLibraryName)) {
        this.preferences.addFurnitureLibrary(furnitureLibraryName);
      }
    } catch (RecorderException ex) {
      String message = this.preferences.getLocalizedString(HomeController.class, 
          "importFurnitureLibraryError", furnitureLibraryName);
      getView().showError(message);
    }
  }

  /**
   * Imports a textures library chosen by the user.  
   */
  public void importTexturesLibrary() {
    getView().invokeLater(new Runnable() {
        public void run() {
          final String texturesLibraryName = getView().showImportTexturesLibraryDialog();
          if (texturesLibraryName != null) {
            importTexturesLibrary(texturesLibraryName);
          }
        }
      });
  }

  /**
   * Imports a given textures library. 
   */
  public void importTexturesLibrary(String texturesLibraryName) {
    try {
      if (!this.preferences.texturesLibraryExists(texturesLibraryName) 
          || getView().confirmReplaceTexturesLibrary(texturesLibraryName)) {
        this.preferences.addTexturesLibrary(texturesLibraryName);
      }
    } catch (RecorderException ex) {
      String message = this.preferences.getLocalizedString(HomeController.class, 
          "importTexturesLibraryError", texturesLibraryName);
      getView().showError(message);
    }
  }

  /**
   * Imports a given plugin.
   */
  public void importPlugin(String pluginName) {
    if (this.pluginManager != null) {
      try {
        if (!this.pluginManager.pluginExists(pluginName) 
            || getView().confirmReplacePlugin(pluginName)) {
          this.pluginManager.addPlugin(pluginName);
          getView().showMessage(this.preferences.getLocalizedString(HomeController.class, 
              "importedPluginMessage"));
        }
      } catch (RecorderException ex) {
        String message = this.preferences.getLocalizedString(HomeController.class, 
            "importPluginError", pluginName);
        getView().showError(message);
      }
    }
  }
  
  /**
   * Undoes last operation.
   */
  public void undo() {
    this.undoManager.undo();
    HomeView view = getView();
    boolean moreUndo = this.undoManager.canUndo();
    view.setEnabled(HomeView.ActionType.UNDO, moreUndo);
    view.setEnabled(HomeView.ActionType.REDO, true);
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
    HomeView view = getView();
    boolean moreRedo = this.undoManager.canRedo();
    view.setEnabled(HomeView.ActionType.UNDO, true);
    view.setEnabled(HomeView.ActionType.REDO, moreRedo);
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
  public void cut(List<? extends Selectable> items) {
    // Start a compound edit that deletes items and changes presentation name
    UndoableEditSupport undoSupport = getUndoableEditSupport();
    undoSupport.beginUpdate();
    getPlanController().deleteItems(items);
    // Add a undoable edit to change presentation name
    undoSupport.postEdit(new AbstractUndoableEdit() { 
        @Override
        public String getPresentationName() {
          return preferences.getLocalizedString(HomeController.class, "undoCutName");
        }      
      });
    // End compound edit
    undoSupport.endUpdate();
  }
  
  /**
   * Adds items to home and posts a paste operation to undo support.
   */
  public void paste(final List<? extends Selectable> items) {
    // Check if pasted items and currently selected items overlap 
    List<Selectable> selectedItems = this.home.getSelectedItems();
    float pastedItemsDelta = 0;
    if (items.size() == selectedItems.size()) {
      // The default delta used to be able to distinguish dropped items from previous selection
      pastedItemsDelta = 20; 
      for (Selectable pastedItem : items) {      
        // Search which item of selected items it may overlap
        float [][] pastedItemPoints = pastedItem.getPoints();
        boolean pastedItemOverlapSelectedItem = false;
        for (Selectable selectedItem : selectedItems) {
          if (Arrays.deepEquals(pastedItemPoints, selectedItem.getPoints())) {
            pastedItemOverlapSelectedItem = true;
            break;
          }
        }
        if (!pastedItemOverlapSelectedItem) {
          pastedItemsDelta = 0;
          break;
        }
      }
    }
    addItems(items, pastedItemsDelta, pastedItemsDelta, false, "undoPasteName");
  }

  /**
   * Adds items to home, moves them of (dx, dy) 
   * and posts a drop operation to undo support.
   */
  public void drop(final List<? extends Selectable> items, float dx, float dy) {
    drop(items, null, dx, dy);
  }

  /**
   * Adds items to home, moves them of (dx, dy) 
   * and posts a drop operation to undo support.
   */
  public void drop(final List<? extends Selectable> items, View destinationView, float dx, float dy) {
    addItems(items, dx, dy, destinationView == getPlanController().getView(), "undoDropName");
  }

  /**
   * Adds items to home.
   */
  private void addItems(final List<? extends Selectable> items, 
                        float dx, float dy, final boolean isDropInPlanView, 
                        final String presentationNameKey) {
    if (items.size() > 1
        || (items.size() == 1
            && !(items.get(0) instanceof Compass))) {
      // Always use selection mode after a drop or a paste operation
      getPlanController().setMode(PlanController.Mode.SELECTION);
      // Start a compound edit that adds walls, furniture, rooms, dimension lines and labels to home
      UndoableEditSupport undoSupport = getUndoableEditSupport();
      undoSupport.beginUpdate();
      List<HomePieceOfFurniture> addedFurniture = Home.getFurnitureSubList(items);
      // If magnetism is enabled, adjust furniture size and elevation
      if (this.preferences.isMagnetismEnabled()) {
        for (HomePieceOfFurniture piece : addedFurniture) {
          if (piece.isResizable()) {
            piece.setWidth(this.preferences.getLengthUnit().getMagnetizedLength(piece.getWidth(), 0.1f));
            piece.setDepth(this.preferences.getLengthUnit().getMagnetizedLength(piece.getDepth(), 0.1f));
            piece.setHeight(this.preferences.getLengthUnit().getMagnetizedLength(piece.getHeight(), 0.1f));
          }
          piece.setElevation(this.preferences.getLengthUnit().getMagnetizedLength(piece.getElevation(), 0.1f));
        }
      }
      getPlanController().moveItems(items, dx, dy);
      if (isDropInPlanView 
          && this.preferences.isMagnetismEnabled()
          && items.size() == 1
          && addedFurniture.size() == 1) {
        // Adjust piece when it's dropped in plan view  
        getPlanController().adjustMagnetizedPieceOfFurniture((HomePieceOfFurniture)items.get(0), dx, dy);
      } 
      getPlanController().addFurniture(addedFurniture);
      getPlanController().addWalls(Home.getWallsSubList(items));
      getPlanController().addRooms(Home.getRoomsSubList(items));
      getPlanController().addDimensionLines(Home.getDimensionLinesSubList(items));
      getPlanController().addLabels(Home.getLabelsSubList(items));
      this.home.setSelectedItems(items);
  
      // Add a undoable edit that will select all the items at redo
      undoSupport.postEdit(new AbstractUndoableEdit() {      
          @Override
          public void redo() throws CannotRedoException {
            super.redo();
            home.setSelectedItems(items);
          }
  
          @Override
          public String getPresentationName() {
            return preferences.getLocalizedString(HomeController.class, presentationNameKey);
          }      
        });
     
      // End compound edit
      undoSupport.endUpdate();
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
    CollectionListener<HomePieceOfFurniture> addedFurnitureListener = 
        new CollectionListener<HomePieceOfFurniture>() {
          public void collectionChanged(CollectionEvent<HomePieceOfFurniture> ev) {
            importedFurniture.add(ev.getItem());
          }
        };
    this.home.addFurnitureListener(addedFurnitureListener);
    
    // Start a compound edit that adds furniture to home
    UndoableEditSupport undoSupport = getUndoableEditSupport();
    undoSupport.beginUpdate();
    // Import furniture
    for (String model : importableModels) {
      getFurnitureController().importFurniture(model);
    }
    this.home.removeFurnitureListener(addedFurnitureListener);
    
    if (importedFurniture.size() > 0) {
      getPlanController().moveItems(importedFurniture, dx, dy);
      this.home.setSelectedItems(importedFurniture);
      
      // Add a undoable edit that will select the imported furniture at redo
      undoSupport.postEdit(new AbstractUndoableEdit() {      
          @Override
          public void redo() throws CannotRedoException {
            super.redo();
            home.setSelectedItems(importedFurniture);
          }
  
          @Override
          public String getPresentationName() {
            return preferences.getLocalizedString(HomeController.class, "undoDropName");
          }      
        });
    }
   
    // End compound edit
    undoSupport.endUpdate();
  }

  /**
   * Deletes the selection in the focused component.
   */
  public void delete() {
    if (this.focusedView == getFurnitureCatalogController().getView()) {
      if (getView().confirmDeleteCatalogSelection()) {
        getFurnitureCatalogController().deleteSelection();
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
  public void focusedViewChanged(View focusedView) {
    this.focusedView = focusedView;
    enableActionsBoundToSelection();
    enablePasteAction();
    enableSelectAllAction();
  }
  
  /**
   * Selects everything in the focused component.
   */
  public void selectAll() {
    if (this.focusedView == getFurnitureController().getView()) {
      getFurnitureController().selectAll();
    } else if (this.focusedView == getPlanController().getView()
               || this.focusedView == getHomeController3D().getView()) {
      getPlanController().selectAll();
    }
  }

  /**
   * Creates a new home and adds it to application home list.
   */
  public void newHome() {
    Home home;
    if (this.application != null) {
      home = this.application.createHome();
    } else {
      home = new Home(this.preferences.getNewWallHeight());
    }
    this.application.addHome(home);
  }

  /**
   * Opens a home. This method displays an {@link HomeView#showOpenDialog() open dialog} 
   * in view, reads the home from the chosen name and adds it to application home list.
   */
  public void open() {
    getView().invokeLater(new Runnable() {
      public void run() {
        final String homeName = getView().showOpenDialog();
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
        String message = this.preferences.getLocalizedString(
            HomeController.class, "alreadyOpen", homeName);
        getView().showMessage(message);
        return;
      }
    }
    
    // Read home in a threaded task
    Callable<Void> openTask = new Callable<Void>() {
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
                String message = preferences.getLocalizedString(
                    HomeController.class, "openError", homeName);
                getView().showError(message);
              } else {
                ex.printStackTrace();
              }
            }
          }
        };
    new ThreadedTaskController(openTask, 
        this.preferences.getLocalizedString(HomeController.class, "openMessage"), exceptionHandler, 
        this.preferences, this.viewFactory).executeTask(getView());
  }
  
  /**
   * Adds the given home to application.
   */
  private void addHomeToApplication(final Home home) {
    getView().invokeLater(new Runnable() {
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
            if (recentHomes.size() == this.preferences.getRecentHomesMaxCount()) {
              break;
            }
          }
        } catch (RecorderException ex) {
          // If homeName can't be checked ignore it
        }
      }
      getView().setEnabled(HomeView.ActionType.DELETE_RECENT_HOMES, 
          !recentHomes.isEmpty());
      return Collections.unmodifiableList(recentHomes);
    } else {
      return new ArrayList<String>();
    }
  }
  
  /**
   * Returns the version of the application.
   */
  public String getVersion() {
    if (this.application != null) {
      return this.application.getVersion();
    } else {
      return "";
    }
  }
  
  /**
   * Deletes the list of recent homes in user preferences. 
   */
  public void deleteRecentHomes() {
    updateUserPreferencesRecentHomes(new ArrayList<String>());
    getView().setEnabled(HomeView.ActionType.DELETE_RECENT_HOMES, false);
  }
  
  /**
   * Manages home close operation. If the home managed by this controller is modified,
   * this method will {@link HomeView#confirmSave(String) confirm} 
   * in view whether home should be saved. Once home is actually saved,
   * home is removed from application homes list.
   */
  public void close() {
    close(null);
  }

  
  /**
   * Manages home close operation. If the home managed by this controller is modified,
   * this method will {@link HomeView#confirmSave(String) confirm} 
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
      switch (getView().confirmSave(this.home.getName())) {
        case SAVE   : save(HomeRecorder.Type.DEFAULT, closeTask); // Falls through
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
    save(HomeRecorder.Type.DEFAULT, null);
  }

  /**
   * Saves the home managed by this controller and executes <code>postSaveTask</code> 
   * if it's not <code>null</code>.
   */
  private void save(HomeRecorder.Type recorderType, Runnable postSaveTask) {
    if (this.home.getName() == null) {
      saveAs(recorderType, postSaveTask);
    } else {
      save(this.home.getName(), recorderType, postSaveTask);
    }
  }
  
  /**
   * Saves the home managed by this controller with a different name. 
   * This method displays a {@link HomeView#showSaveDialog(String) save dialog} in  view, 
   * and saves home with the chosen name if any. 
   */
  public void saveAs() {
    saveAs(HomeRecorder.Type.DEFAULT, null);
  }

  /**
   * Saves the home managed by this controller with a different name. 
   */
  private void saveAs(HomeRecorder.Type recorderType, Runnable postSaveTask) {
    String newName = getView().showSaveDialog(this.home.getName());
    if (newName != null) {
      save(newName, recorderType, postSaveTask);
    }
  }

  /**
   * Saves the home managed by this controller and compresses it. If home name doesn't exist, 
   * this method will prompt user to choose a home name.
   */
  public void saveAndCompress() {
    save(HomeRecorder.Type.COMPRESSED, null);
  }
  
  /**
   * Actually saves the home managed by this controller and executes <code>postSaveTask</code> 
   * if it's not <code>null</code>.
   */
  private void save(final String homeName, 
                    final HomeRecorder.Type recorderType, 
                    final Runnable postSaveTask) {
    // If home version is older than current version
    // or if home name is changed
    // or if user confirms to save a home created with a newer version
    if (this.home.getVersion() <= Home.CURRENT_VERSION
        || !homeName.equals(this.home.getName()) 
        || getView().confirmSaveNewerHome(homeName)) {
      // Save home in a threaded task
      Callable<Void> saveTask = new Callable<Void>() {
            public Void call() throws RecorderException {
              // Write home with application recorder
              application.getHomeRecorder(recorderType).writeHome(home, homeName);
              updateSavedHome(homeName, postSaveTask);
              return null;
            }
          };
      ThreadedTaskController.ExceptionHandler exceptionHandler = 
          new ThreadedTaskController.ExceptionHandler() {
            public void handleException(Exception ex) {
              if (!(ex instanceof InterruptedRecorderException)) {
                if (ex instanceof RecorderException) {
                  String message = preferences.getLocalizedString(
                      HomeController.class, "saveError", homeName);
                  getView().showError(message);
                } else {
                  ex.printStackTrace();
                }
              }
            }
          };
      new ThreadedTaskController(saveTask, 
          this.preferences.getLocalizedString(HomeController.class, "saveMessage"), exceptionHandler, 
          this.preferences, this.viewFactory).executeTask(getView());
    }
  }
  
  /**
   * Updates the saved home and executes <code>postSaveTask</code> 
   * if it's not <code>null</code>.
   */
  private void updateSavedHome(final String homeName,
                               final Runnable postSaveTask) {
    getView().invokeLater(new Runnable() {
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
   * Controls the export of the 3D view of current home to a SVG file.
   */
  public void exportToSVG() {
    final String svgName = getView().showExportToSVGDialog(this.home.getName());    
    if (svgName != null) {
      // Export 3D view in a threaded task
      Callable<Void> exportToSvgTask = new Callable<Void>() {
            public Void call() throws RecorderException {
              getView().exportToSVG(svgName);
              return null;
            }
          };
      ThreadedTaskController.ExceptionHandler exceptionHandler = 
          new ThreadedTaskController.ExceptionHandler() {
            public void handleException(Exception ex) {
              if (!(ex instanceof InterruptedRecorderException)) {
                if (ex instanceof RecorderException) {
                  String message = preferences.getLocalizedString(
                      HomeController.class, "exportToSVGError", svgName);
                  getView().showError(message);
                } else {
                  ex.printStackTrace();
                }
              }
            }
          };
      new ThreadedTaskController(exportToSvgTask, 
          this.preferences.getLocalizedString(HomeController.class, "exportToSVGMessage"), exceptionHandler, 
          this.preferences, this.viewFactory).executeTask(getView());
    }
  }
  
  /**
   * Controls the export of the 3D view of current home to an OBJ file.
   */
  public void exportToOBJ() {
    final String objName = getView().showExportToOBJDialog(this.home.getName());    
    if (objName != null) {
      // Export 3D view in a threaded task
      Callable<Void> exportToObjTask = new Callable<Void>() {
            public Void call() throws RecorderException {
              getView().exportToOBJ(objName);
              return null;
            }
          };
      ThreadedTaskController.ExceptionHandler exceptionHandler = 
          new ThreadedTaskController.ExceptionHandler() {
            public void handleException(Exception ex) {
              if (!(ex instanceof InterruptedRecorderException)) {
                if (ex instanceof RecorderException) {
                  String message = preferences.getLocalizedString(
                      HomeController.class, "exportToOBJError", objName);
                  getView().showError(message);
                } else {
                  ex.printStackTrace();
                }
              }
            }
          };
      new ThreadedTaskController(exportToObjTask, 
          this.preferences.getLocalizedString(HomeController.class, "exportToOBJMessage"), exceptionHandler, 
          this.preferences, this.viewFactory).executeTask(getView());
    }
  }
  
  /**
   * Controls the creation of photo-realistic images.
   */
  public void createPhoto() {
    PhotoController photoController = new PhotoController(this.home, this.preferences, 
        getHomeController3D().getView(), this.viewFactory, this.contentManager);
    photoController.displayView(getView());
  }
  
  /**
   * Controls the creation of 3D videos.
   */
  public void createVideo() {
    getPlanController().setMode(PlanController.Mode.SELECTION);
    getHomeController3D().viewFromObserver();
    VideoController videoController = new VideoController(this.home, this.preferences, 
        this.viewFactory, this.contentManager);
    videoController.displayView(getView());
  }
  
  /**
   * Controls page setup.
   */
  public void setupPage() {
    new PageSetupController(this.home, this.preferences, 
        this.viewFactory, getUndoableEditSupport()).displayView(getView());
  }

  /**
   * Controls the print preview.
   */
  public void previewPrint() {
    new PrintPreviewController(this.home, this.preferences, 
        this, this.viewFactory).displayView(getView());
  }

  /**
   * Controls the print of this home.
   */
  public void print() {
    final Callable<Void> printTask = getView().showPrintDialog();    
    if (printTask != null) {
      // Print in a threaded task
      ThreadedTaskController.ExceptionHandler exceptionHandler = 
          new ThreadedTaskController.ExceptionHandler() {
            public void handleException(Exception ex) {
              if (!(ex instanceof InterruptedRecorderException)) {
                if (ex instanceof RecorderException) {
                  String message = preferences.getLocalizedString(
                      HomeController.class, "printError", home.getName());
                  getView().showError(message);
                } else {
                  ex.printStackTrace();
                }
              }
            }
          };
      new ThreadedTaskController(printTask, 
          this.preferences.getLocalizedString(HomeController.class, "printMessage"), exceptionHandler, 
          this.preferences, this.viewFactory).executeTask(getView());      
    }
  }

  /**
   * Controls the print of this home in a PDF file.
   */
  public void printToPDF() {
    final String pdfName = getView().showPrintToPDFDialog(this.home.getName());    
    if (pdfName != null) {
      // Print to PDF in a threaded task
      Callable<Void> printToPdfTask = new Callable<Void>() {
          public Void call() throws RecorderException {
            getView().printToPDF(pdfName);
            return null;
          }
        };
      ThreadedTaskController.ExceptionHandler exceptionHandler = 
          new ThreadedTaskController.ExceptionHandler() {
            public void handleException(Exception ex) {
              if (!(ex instanceof InterruptedRecorderException)) {
                if (ex instanceof RecorderException) {
                  String message = preferences.getLocalizedString(
                      HomeController.class, "printToPDFError", pdfName);
                  getView().showError(message);
                } else {
                  ex.printStackTrace();
                }
              }
            }
          };
      new ThreadedTaskController(printToPdfTask, 
          preferences.getLocalizedString(HomeController.class, "printToPDFMessage"), exceptionHandler, 
          this.preferences, this.viewFactory).executeTask(getView());
    }
  }

  /**
   * Controls application exit. If any home in application homes list is modified,
   * the user will be {@link HomeView#confirmExit() prompted} in view whether he wants
   * to discard his modifications ot not.  
   */
  public void exit() {
    for (Home home : this.application.getHomes()) {
      if (home.isModified()) {
        if (getView().confirmExit()) {
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
    new UserPreferencesController(this.preferences, 
        this.viewFactory, this.contentManager).displayView(getView());
  }
  
  /**
   * Displays a tip message dialog depending on the given mode and 
   * sets the active mode of the plan controller. 
   */
  public void setMode(Mode mode) {
    if (getPlanController().getMode() != mode) {
      final String actionKey;
      if (mode == Mode.WALL_CREATION) {
        actionKey = HomeView.ActionType.CREATE_WALLS.name();
      } else if (mode == Mode.ROOM_CREATION) {
        actionKey = HomeView.ActionType.CREATE_ROOMS.name();
      } else if (mode == Mode.DIMENSION_LINE_CREATION) {
        actionKey = HomeView.ActionType.CREATE_DIMENSION_LINES.name();
      } else if (mode == Mode.LABEL_CREATION) {
        actionKey = HomeView.ActionType.CREATE_LABELS.name();
      } else {
        actionKey = null;
      }
      // Display the tip message dialog matching mode
      if (actionKey != null 
          && !this.preferences.isActionTipIgnored(actionKey)) {
        getView().invokeLater(new Runnable() {
            public void run() {
              // Show tip later to let the mode switch finish first
              if (getView().showActionTipMessage(actionKey)) {
                preferences.setActionTipIgnored(actionKey);
              }
            }
          });
      }
      getPlanController().setMode(mode);
    }
  }

  /**
   * Displays the wizard that helps to import home background image. 
   */
  public void importBackgroundImage() {
    new BackgroundImageWizardController(this.home, this.preferences, 
        this.viewFactory, this.contentManager, getUndoableEditSupport()).displayView(getView());
  }
  
  /**
   * Displays the wizard that helps to change home background image. 
   */
  public void modifyBackgroundImage() {
    importBackgroundImage();
  }
  
  /**
   * Hides the home background image. 
   */
  public void hideBackgroundImage() {     
    toggleBackgroundImageVisibility("undoHideBackgroundImageName");
  }
  
  /**
   * Shows the home background image. 
   */
  public void showBackgroundImage() {
    toggleBackgroundImageVisibility("undoShowBackgroundImageName");
  }
  
  /**
   * Toggles visibility of the background image and posts an undoable operation.
   */
  private void toggleBackgroundImageVisibility(final String presentationName) {
    doToggleBackgroundImageVisibility(); 
    UndoableEdit undoableEdit = new AbstractUndoableEdit() {
      @Override
      public void undo() throws CannotUndoException {
        super.undo();
        doToggleBackgroundImageVisibility(); 
      }
      
      @Override
      public void redo() throws CannotRedoException {
        super.redo();
        doToggleBackgroundImageVisibility();
      }
      
      @Override
      public String getPresentationName() {
        return preferences.getLocalizedString(HomeController.class, presentationName);
      }
    };
    getUndoableEditSupport().postEdit(undoableEdit);
  }

  /**
   * Toggles visibility of the background image.
   */
  private void doToggleBackgroundImageVisibility() {
    BackgroundImage backgroundImage = this.home.getBackgroundImage();
    this.home.setBackgroundImage(new BackgroundImage(backgroundImage.getImage(),
        backgroundImage.getScaleDistance(), 
        backgroundImage.getScaleDistanceXStart(), backgroundImage.getScaleDistanceYStart(), 
        backgroundImage.getScaleDistanceXEnd(), backgroundImage.getScaleDistanceYEnd(),
        backgroundImage.getXOrigin(), backgroundImage.getYOrigin(), !backgroundImage.isVisible()));
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
        return preferences.getLocalizedString(HomeController.class, "undoDeleteBackgroundImageName");
      }
    };
    getUndoableEditSupport().postEdit(undoableEdit);
  }
  
  /**
   * Zooms out in plan.
   */
  public void zoomOut() {
    PlanController planController = getPlanController();
    float newScale = planController.getScale() / 1.5f;
    planController.setScale(newScale);
  }

  /**
   * Zooms in in plan.
   */
  public void zoomIn() {
    PlanController planController = getPlanController();
    float newScale = planController.getScale() * 1.5f;
    planController.setScale(newScale);
  }

  /**
   * Prompts a name for the current camera and stores it in home.
   */
  public void storeCamera() {
    String now = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(new Date());
    String name = getView().showStoreCameraDialog(now);
    if (name != null) {
      getHomeController3D().storeCamera(name);
    }
  }

  /**
   * Detaches the given <code>view</code> from home view.
   */
  public void detachView(View view) {
    if (view != null) {
      getView().detachView(view);
    }
  }
      		
  /**
   * Attaches the given <code>view</code> to home view.
   */
  public void attachView(View view) {
    if (view != null) {
      getView().attachView(view);
    }
  }
                
  /**
   * Displays help window.
   */
  public void help() {
    if (helpController == null) {
      helpController = new HelpController(this.preferences, this.viewFactory);
    }
    helpController.displayView();
  }

  /**
   * Displays about dialog.
   */
  public void about() {
    getView().showAboutDialog();
  }

  /**
   * Controls the change of value of a visual property in home.
   */
  public void setVisualProperty(String propertyName,
                                Object propertyValue) {
    this.home.setVisualProperty(propertyName, propertyValue);
  }
}
