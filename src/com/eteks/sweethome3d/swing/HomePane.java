/*
 * HomePane.java 15 mai 2006
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;

import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.SwingPropertyChangeSupport;

import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.InterruptedRecorderException;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.plugin.Plugin;
import com.eteks.sweethome3d.plugin.PluginAction;
import com.eteks.sweethome3d.tools.OperatingSystem;

/**
 * The MVC view that edits a home. 
 * @author Emmanuel Puybaret
 */
public class HomePane extends JRootPane {
  public enum ActionType {
      NEW_HOME, CLOSE, OPEN, DELETE_RECENT_HOMES, SAVE, SAVE_AS, PAGE_SETUP, PRINT_PREVIEW, PRINT, PRINT_TO_PDF, PREFERENCES, EXIT, 
      UNDO, REDO, CUT, COPY, PASTE, DELETE, SELECT_ALL,
      ADD_HOME_FURNITURE, DELETE_HOME_FURNITURE, MODIFY_FURNITURE, IMPORT_FURNITURE, IMPORT_FURNITURE_LIBRARY,
      SORT_HOME_FURNITURE_BY_CATALOG_ID, SORT_HOME_FURNITURE_BY_NAME, 
      SORT_HOME_FURNITURE_BY_WIDTH, SORT_HOME_FURNITURE_BY_DEPTH, SORT_HOME_FURNITURE_BY_HEIGHT, 
      SORT_HOME_FURNITURE_BY_X, SORT_HOME_FURNITURE_BY_Y, SORT_HOME_FURNITURE_BY_ELEVATION, 
      SORT_HOME_FURNITURE_BY_ANGLE, SORT_HOME_FURNITURE_BY_COLOR, 
      SORT_HOME_FURNITURE_BY_MOVABILITY, SORT_HOME_FURNITURE_BY_TYPE, SORT_HOME_FURNITURE_BY_VISIBILITY, 
      SORT_HOME_FURNITURE_BY_PRICE, SORT_HOME_FURNITURE_BY_VALUE_ADDED_TAX_PERCENTAGE, 
      SORT_HOME_FURNITURE_BY_VALUE_ADDED_TAX, SORT_HOME_FURNITURE_BY_PRICE_VALUE_ADDED_TAX_INCLUDED,
      SORT_HOME_FURNITURE_BY_DESCENDING_ORDER,
      DISPLAY_HOME_FURNITURE_CATALOG_ID, DISPLAY_HOME_FURNITURE_NAME, 
      DISPLAY_HOME_FURNITURE_WIDTH, DISPLAY_HOME_FURNITURE_DEPTH, DISPLAY_HOME_FURNITURE_HEIGHT, 
      DISPLAY_HOME_FURNITURE_X, DISPLAY_HOME_FURNITURE_Y, DISPLAY_HOME_FURNITURE_ELEVATION, 
      DISPLAY_HOME_FURNITURE_ANGLE, DISPLAY_HOME_FURNITURE_COLOR, 
      DISPLAY_HOME_FURNITURE_MOVABLE, DISPLAY_HOME_FURNITURE_DOOR_OR_WINDOW, DISPLAY_HOME_FURNITURE_VISIBLE,
      DISPLAY_HOME_FURNITURE_PRICE, DISPLAY_HOME_FURNITURE_VALUE_ADDED_TAX_PERCENTAGE,
      DISPLAY_HOME_FURNITURE_VALUE_ADDED_TAX, DISPLAY_HOME_FURNITURE_PRICE_VALUE_ADDED_TAX_INCLUDED,
      ALIGN_FURNITURE_ON_TOP, ALIGN_FURNITURE_ON_BOTTOM, ALIGN_FURNITURE_ON_LEFT, ALIGN_FURNITURE_ON_RIGHT,
      SELECT, CREATE_WALLS, CREATE_DIMENSION_LINES, DELETE_SELECTION, MODIFY_WALL, REVERSE_WALL_DIRECTION, SPLIT_WALL,
      IMPORT_BACKGROUND_IMAGE, MODIFY_BACKGROUND_IMAGE, DELETE_BACKGROUND_IMAGE, ZOOM_OUT, ZOOM_IN,  
      VIEW_FROM_TOP, VIEW_FROM_OBSERVER, MODIFY_3D_ATTRIBUTES, EXPORT_TO_OBJ,
      HELP, ABOUT}
  public enum SaveAnswer {SAVE, CANCEL, DO_NOT_SAVE}
  private enum MenuActionType {FILE_MENU, EDIT_MENU, FURNITURE_MENU, PLAN_MENU, VIEW_3D_MENU, HELP_MENU, 
    OPEN_RECENT_HOME_MENU, SORT_HOME_FURNITURE_MENU, DISPLAY_HOME_FURNITURE_PROPERTY_MENU}
  
  private static final String MAIN_PANE_DIVIDER_LOCATION_VISUAL_PROPERTY    = "com.eteks.sweethome3d.SweetHome3D.MainPaneDividerLocation";
  private static final String CATALOG_PANE_DIVIDER_LOCATION_VISUAL_PROPERTY = "com.eteks.sweethome3d.SweetHome3D.CatalogPaneDividerLocation";
  private static final String PLAN_PANE_DIVIDER_LOCATION_VISUAL_PROPERTY    = "com.eteks.sweethome3d.SweetHome3D.PlanPaneDividerLocation";
  private static final String PLAN_VIEWPORT_X_VISUAL_PROPERTY               = "com.eteks.sweethome3d.SweetHome3D.PlanViewportX";
  private static final String PLAN_VIEWPORT_Y_VISUAL_PROPERTY               = "com.eteks.sweethome3d.SweetHome3D.PlanViewportY";
  private static final String FURNITURE_VIEWPORT_Y_VISUAL_PROPERTY          = "com.eteks.sweethome3d.SweetHome3D.FurnitureViewportY";

  private static final int    DEFAULT_SMALL_ICON_HEIGHT = 16;
  
  private ContentManager                  contentManager;
  private Home                            home;
  private HomeController                  controller;
  private ResourceBundle                  resource;
  // Button models shared by Select, Create walls and Create dimensions menu items and the matching tool bar buttons
  private JToggleButton.ToggleButtonModel selectToggleModel;
  private JToggleButton.ToggleButtonModel createWallsToggleModel;
  private JToggleButton.ToggleButtonModel createDimensionLinesToggleModel;
  // Button models shared by View from top and View from observer menu items and the matching tool bar buttons
  private JToggleButton.ToggleButtonModel viewFromTopToggleModel;
  private JToggleButton.ToggleButtonModel viewFromObserverToggleModel;
  private JComponent                      focusedComponent;
  private TransferHandler                 catalogTransferHandler;
  private TransferHandler                 furnitureTransferHandler;
  private TransferHandler                 planTransferHandler;
  private ActionMap                       menuActionMap;
  private List<Action>                    pluginActions;
  
  /**
   * Creates this view associated with its controller.
   */
  public HomePane(Home home, UserPreferences preferences, 
                  ContentManager contentManager, HomeController controller) {
    this(home, preferences, contentManager, null, controller);
  }
  
  /**
   * Creates this view associated with its controller.
   */
  public HomePane(Home home, UserPreferences preferences,
                  ContentManager contentManager, List<Plugin> plugins,
                  HomeController controller) {
    this.home = home;
    this.contentManager = contentManager;
    this.controller = controller;
    this.resource = ResourceBundle.getBundle(HomePane.class.getName());
    // Create unique toggle button models for Selection / Wall creation / Dimension line creation states
    // so Select, Create walls and Create Dimension lines menu items and tool bar buttons 
    // always reflect the same toggle state at screen
    this.selectToggleModel = new JToggleButton.ToggleButtonModel();
    this.selectToggleModel.setSelected(controller.getPlanController().getMode() 
        == PlanController.Mode.SELECTION);
    this.createWallsToggleModel = new JToggleButton.ToggleButtonModel();
    this.createWallsToggleModel.setSelected(controller.getPlanController().getMode() 
        == PlanController.Mode.WALL_CREATION);
    this.createDimensionLinesToggleModel = new JToggleButton.ToggleButtonModel();
    this.createDimensionLinesToggleModel.setSelected(controller.getPlanController().getMode() 
        == PlanController.Mode.DIMENSION_LINE_CREATION);
    // Create unique toggle button models for top and observer cameras
    // so View from top and View from observer creation menu items and tool bar buttons 
    // always reflect the same toggle state at screen
    this.viewFromTopToggleModel = new JToggleButton.ToggleButtonModel();
    this.viewFromTopToggleModel.setSelected(home.getCamera() == home.getTopCamera());
    this.viewFromObserverToggleModel = new JToggleButton.ToggleButtonModel();
    this.viewFromObserverToggleModel.setSelected(home.getCamera() == home.getObserverCamera());
    
    JPopupMenu.setDefaultLightWeightPopupEnabled(false);
    ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);    
    
    createActions(controller);
    createMenuActions(controller);   
    createPluginActions(plugins);
    createTransferHandlers(home, preferences, contentManager, controller);
    addHomeListener(home);
    addLanguageListener(preferences);
    addPlanControllerListener(controller.getPlanController());
    JMenuBar homeMenuBar = createHomeMenuBar(home, preferences, controller, contentManager);
    setJMenuBar(homeMenuBar);
    Container contentPane = getContentPane();
    contentPane.add(createToolBar(), BorderLayout.NORTH);
    contentPane.add(createMainPane(home, preferences, controller));
    if (OperatingSystem.isMacOSXLeopardOrSuperior()) {
      // Under Mac OS X 10.5, add some dummy labels at left and right borders
      // to avoid the tool bar to be attached on these borders
      // (segmented buttons created on this system aren't properly rendered
      // when they are aligned vertically)
      contentPane.add(new JLabel(), BorderLayout.WEST);
      contentPane.add(new JLabel(), BorderLayout.EAST);
    }

    disableMenuItemsDuringDragAndDrop(controller.getPlanController().getView(), homeMenuBar);
    // Change component orientation
    applyComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));  
  }

  /**
   * Create the actions map of this component.
   */
  private void createActions(final HomeController controller) {
    createAction(ActionType.NEW_HOME, controller, "newHome");
    createAction(ActionType.OPEN, controller, "open");
    createAction(ActionType.DELETE_RECENT_HOMES, controller, "deleteRecentHomes");
    createAction(ActionType.CLOSE, controller, "close");
    createAction(ActionType.SAVE, controller, "save");
    createAction(ActionType.SAVE_AS, controller, "saveAs");
    createAction(ActionType.PAGE_SETUP, controller, "setupPage");
    createAction(ActionType.PRINT_PREVIEW, controller, "previewPrint");
    createAction(ActionType.PRINT, controller, "print");
    createAction(ActionType.PRINT_TO_PDF, controller, "printToPDF");
    createAction(ActionType.PREFERENCES, controller, "editPreferences");
    createAction(ActionType.EXIT, controller, "exit");
    
    createAction(ActionType.UNDO, controller, "undo");
    createAction(ActionType.REDO, controller, "redo");
    createClipboardAction(ActionType.CUT, TransferHandler.getCutAction());
    createClipboardAction(ActionType.COPY, TransferHandler.getCopyAction());
    createClipboardAction(ActionType.PASTE, TransferHandler.getPasteAction());
    createAction(ActionType.DELETE, controller, "delete");
    createAction(ActionType.SELECT_ALL, controller, "selectAll");
    
    createAction(ActionType.ADD_HOME_FURNITURE, controller, "addHomeFurniture");
    FurnitureController furnitureController = controller.getFurnitureController();
    createAction(ActionType.DELETE_HOME_FURNITURE,
        furnitureController, "deleteSelection");
    createAction(ActionType.MODIFY_FURNITURE, controller, "modifySelectedFurniture");
    createAction(ActionType.IMPORT_FURNITURE, controller, "importFurniture");
    createAction(ActionType.IMPORT_FURNITURE_LIBRARY, controller, "importFurnitureLibrary");
    createAction(ActionType.ALIGN_FURNITURE_ON_TOP, 
        furnitureController, "alignSelectedFurnitureOnTop");
    createAction(ActionType.ALIGN_FURNITURE_ON_BOTTOM, 
        furnitureController, "alignSelectedFurnitureOnBottom");
    createAction(ActionType.ALIGN_FURNITURE_ON_LEFT, 
        furnitureController, "alignSelectedFurnitureOnLeft");
    createAction(ActionType.ALIGN_FURNITURE_ON_RIGHT, 
        furnitureController, "alignSelectedFurnitureOnRight");
    createAction(ActionType.SORT_HOME_FURNITURE_BY_CATALOG_ID, furnitureController, "toggleFurnitureSort", 
        HomePieceOfFurniture.SortableProperty.CATALOG_ID);
    createAction(ActionType.SORT_HOME_FURNITURE_BY_NAME, furnitureController, "toggleFurnitureSort", 
        HomePieceOfFurniture.SortableProperty.NAME);
    createAction(ActionType.SORT_HOME_FURNITURE_BY_WIDTH, furnitureController, "toggleFurnitureSort", 
        HomePieceOfFurniture.SortableProperty.WIDTH);
    createAction(ActionType.SORT_HOME_FURNITURE_BY_DEPTH, furnitureController, "toggleFurnitureSort", 
        HomePieceOfFurniture.SortableProperty.DEPTH);
    createAction(ActionType.SORT_HOME_FURNITURE_BY_HEIGHT, furnitureController, "toggleFurnitureSort", 
        HomePieceOfFurniture.SortableProperty.HEIGHT);
    createAction(ActionType.SORT_HOME_FURNITURE_BY_X, furnitureController, "toggleFurnitureSort", 
        HomePieceOfFurniture.SortableProperty.X);
    createAction(ActionType.SORT_HOME_FURNITURE_BY_Y, furnitureController, "toggleFurnitureSort", 
        HomePieceOfFurniture.SortableProperty.Y);
    createAction(ActionType.SORT_HOME_FURNITURE_BY_ELEVATION, furnitureController, "toggleFurnitureSort", 
        HomePieceOfFurniture.SortableProperty.ELEVATION);
    createAction(ActionType.SORT_HOME_FURNITURE_BY_ANGLE, furnitureController, "toggleFurnitureSort", 
        HomePieceOfFurniture.SortableProperty.ANGLE);
    createAction(ActionType.SORT_HOME_FURNITURE_BY_COLOR, furnitureController, "toggleFurnitureSort", 
        HomePieceOfFurniture.SortableProperty.COLOR);
    createAction(ActionType.SORT_HOME_FURNITURE_BY_MOVABILITY, furnitureController, "toggleFurnitureSort", 
        HomePieceOfFurniture.SortableProperty.MOVABLE);
    createAction(ActionType.SORT_HOME_FURNITURE_BY_TYPE, furnitureController, "toggleFurnitureSort", 
        HomePieceOfFurniture.SortableProperty.DOOR_OR_WINDOW);
    createAction(ActionType.SORT_HOME_FURNITURE_BY_VISIBILITY, furnitureController, "toggleFurnitureSort", 
        HomePieceOfFurniture.SortableProperty.VISIBLE);
    createAction(ActionType.SORT_HOME_FURNITURE_BY_PRICE, furnitureController, "toggleFurnitureSort", 
        HomePieceOfFurniture.SortableProperty.PRICE);
    createAction(ActionType.SORT_HOME_FURNITURE_BY_VALUE_ADDED_TAX_PERCENTAGE, furnitureController, "toggleFurnitureSort", 
        HomePieceOfFurniture.SortableProperty.VALUE_ADDED_TAX_PERCENTAGE);
    createAction(ActionType.SORT_HOME_FURNITURE_BY_VALUE_ADDED_TAX, furnitureController, "toggleFurnitureSort", 
        HomePieceOfFurniture.SortableProperty.VALUE_ADDED_TAX);
    createAction(ActionType.SORT_HOME_FURNITURE_BY_PRICE_VALUE_ADDED_TAX_INCLUDED, furnitureController, "toggleFurnitureSort", 
        HomePieceOfFurniture.SortableProperty.PRICE_VALUE_ADDED_TAX_INCLUDED);
    createAction(ActionType.SORT_HOME_FURNITURE_BY_DESCENDING_ORDER, furnitureController, "toggleFurnitureSortOrder");
    createAction(ActionType.DISPLAY_HOME_FURNITURE_CATALOG_ID, furnitureController, "toggleFurnitureVisibleProperty", 
        HomePieceOfFurniture.SortableProperty.CATALOG_ID);
    createAction(ActionType.DISPLAY_HOME_FURNITURE_NAME, furnitureController, "toggleFurnitureVisibleProperty", 
        HomePieceOfFurniture.SortableProperty.NAME);
    createAction(ActionType.DISPLAY_HOME_FURNITURE_WIDTH, furnitureController, "toggleFurnitureVisibleProperty", 
        HomePieceOfFurniture.SortableProperty.WIDTH);
    createAction(ActionType.DISPLAY_HOME_FURNITURE_DEPTH, furnitureController, "toggleFurnitureVisibleProperty", 
        HomePieceOfFurniture.SortableProperty.DEPTH);
    createAction(ActionType.DISPLAY_HOME_FURNITURE_HEIGHT, furnitureController, "toggleFurnitureVisibleProperty", 
        HomePieceOfFurniture.SortableProperty.HEIGHT);
    createAction(ActionType.DISPLAY_HOME_FURNITURE_X, furnitureController, "toggleFurnitureVisibleProperty", 
        HomePieceOfFurniture.SortableProperty.X);
    createAction(ActionType.DISPLAY_HOME_FURNITURE_Y, furnitureController, "toggleFurnitureVisibleProperty", 
        HomePieceOfFurniture.SortableProperty.Y);
    createAction(ActionType.DISPLAY_HOME_FURNITURE_ELEVATION, furnitureController, "toggleFurnitureVisibleProperty", 
        HomePieceOfFurniture.SortableProperty.ELEVATION);
    createAction(ActionType.DISPLAY_HOME_FURNITURE_ANGLE, furnitureController, "toggleFurnitureVisibleProperty", 
        HomePieceOfFurniture.SortableProperty.ANGLE);
    createAction(ActionType.DISPLAY_HOME_FURNITURE_COLOR, furnitureController, "toggleFurnitureVisibleProperty", 
        HomePieceOfFurniture.SortableProperty.COLOR);
    createAction(ActionType.DISPLAY_HOME_FURNITURE_MOVABLE, furnitureController, "toggleFurnitureVisibleProperty", 
        HomePieceOfFurniture.SortableProperty.MOVABLE);
    createAction(ActionType.DISPLAY_HOME_FURNITURE_DOOR_OR_WINDOW, furnitureController, "toggleFurnitureVisibleProperty", 
        HomePieceOfFurniture.SortableProperty.DOOR_OR_WINDOW);
    createAction(ActionType.DISPLAY_HOME_FURNITURE_VISIBLE, furnitureController, "toggleFurnitureVisibleProperty", 
        HomePieceOfFurniture.SortableProperty.VISIBLE);
    createAction(ActionType.DISPLAY_HOME_FURNITURE_PRICE, furnitureController, "toggleFurnitureVisibleProperty", 
        HomePieceOfFurniture.SortableProperty.PRICE);
    createAction(ActionType.DISPLAY_HOME_FURNITURE_VALUE_ADDED_TAX_PERCENTAGE, furnitureController, "toggleFurnitureVisibleProperty", 
        HomePieceOfFurniture.SortableProperty.VALUE_ADDED_TAX_PERCENTAGE);
    createAction(ActionType.DISPLAY_HOME_FURNITURE_VALUE_ADDED_TAX, furnitureController, "toggleFurnitureVisibleProperty", 
        HomePieceOfFurniture.SortableProperty.VALUE_ADDED_TAX);
    createAction(ActionType.DISPLAY_HOME_FURNITURE_PRICE_VALUE_ADDED_TAX_INCLUDED, furnitureController, "toggleFurnitureVisibleProperty", 
        HomePieceOfFurniture.SortableProperty.PRICE_VALUE_ADDED_TAX_INCLUDED);
    
    createAction(ActionType.SELECT, controller.getPlanController(), "setMode", 
        PlanController.Mode.SELECTION);
    createAction(ActionType.CREATE_WALLS, controller.getPlanController(), "setMode",
        PlanController.Mode.WALL_CREATION);
    createAction(ActionType.CREATE_DIMENSION_LINES, controller.getPlanController(), "setMode",
        PlanController.Mode.DIMENSION_LINE_CREATION);
    createAction(ActionType.DELETE_SELECTION, 
        controller.getPlanController(), "deleteSelection");
    createAction(ActionType.MODIFY_WALL, 
        controller.getPlanController(), "modifySelectedWalls");
    createAction(ActionType.REVERSE_WALL_DIRECTION, 
        controller.getPlanController(), "reverseSelectedWallsDirection");
    createAction(ActionType.SPLIT_WALL, 
        controller.getPlanController(), "splitSelectedWall");
    createAction(ActionType.IMPORT_BACKGROUND_IMAGE, 
        controller, "importBackgroundImage");
    createAction(ActionType.MODIFY_BACKGROUND_IMAGE, 
        controller, "modifyBackgroundImage");
    createAction(ActionType.DELETE_BACKGROUND_IMAGE, 
        controller, "deleteBackgroundImage");
    createAction(ActionType.ZOOM_OUT, controller, "zoomOut");
    createAction(ActionType.ZOOM_IN, controller, "zoomIn");
    
    createAction(ActionType.VIEW_FROM_TOP, 
        controller.getHomeController3D(), "viewFromTop");
    createAction(ActionType.VIEW_FROM_OBSERVER, 
        controller.getHomeController3D(), "viewFromObserver");
    createAction(ActionType.MODIFY_3D_ATTRIBUTES, 
        controller.getHomeController3D(), "modifyAttributes");
    createAction(ActionType.EXPORT_TO_OBJ, controller, "exportToOBJ");
    
    createAction(ActionType.HELP, controller, "help");
    createAction(ActionType.ABOUT, controller, "about");
  }

  /**
   * Creates a <code>ControllerAction</code> object that calls on <code>controller</code> a given
   * <code>method</code> with its <code>parameters</code>.
   */
  private void createAction(ActionType action, Object controller, String method, Object ... parameters) {
    try {
      getActionMap().put(action, new ControllerAction(
          this.resource, action.toString(), controller, method, parameters));
    } catch (NoSuchMethodException ex) {
      throw new RuntimeException(ex);
    }
  }
  
  /**
   * Creates a <code>ReourceAction</code> object that calls 
   * <code>actionPerfomed</code> method on a given 
   * existing <code>clipboardAction</code> with a source equal to focused component.
   */
  private void createClipboardAction(ActionType actionType, 
                                     final Action clipboardAction) {
    getActionMap().put(actionType,
        new ResourceAction (this.resource, actionType.toString()) {
          public void actionPerformed(ActionEvent ev) {
            ev = new ActionEvent(focusedComponent, ActionEvent.ACTION_PERFORMED, null);
            clipboardAction.actionPerformed(ev);
          }
        });
  }

  /**
   * Create the actions map used to create menus of this component.
   */
  private void createMenuActions(HomeController controller) {
    this.menuActionMap = new ActionMap();
    createMenuAction(MenuActionType.FILE_MENU);
    createMenuAction(MenuActionType.EDIT_MENU);
    createMenuAction(MenuActionType.FURNITURE_MENU);
    createMenuAction(MenuActionType.PLAN_MENU);
    createMenuAction(MenuActionType.VIEW_3D_MENU);
    createMenuAction(MenuActionType.HELP_MENU);
    createMenuAction(MenuActionType.OPEN_RECENT_HOME_MENU);
    createMenuAction(MenuActionType.SORT_HOME_FURNITURE_MENU);
    createMenuAction(MenuActionType.DISPLAY_HOME_FURNITURE_PROPERTY_MENU);
  }
  
  /**
   * Creates a <code>ResourceAction</code> object stored in menu action map.
   */
  private void createMenuAction(MenuActionType action) {
    this.menuActionMap.put(action, new ResourceAction(
          this.resource, action.toString(), true));
  }

  /**
   * Creates the Swing actions matching each actions available in <code>plugins</code>.
   */
  private void createPluginActions(List<Plugin> plugins) {
    this.pluginActions = new ArrayList<Action>();
    if (plugins != null) {
      for (Plugin plugin : plugins) {
        for (final PluginAction pluginAction : plugin.getActions()) {
          // Create a Swing action adapter to plug-in action
          this.pluginActions.add(new ActionAdapter(pluginAction)); 
        }
      }
    }
  }

  /**
   * Creates components transfer handlers.
   */
  private void createTransferHandlers(Home home, UserPreferences preferences,
                                      ContentManager contentManager,
                                      HomeController controller) {
    this.catalogTransferHandler = 
        new FurnitureCatalogTransferHandler(preferences.getFurnitureCatalog(), contentManager, controller.getCatalogController());
    this.furnitureTransferHandler = 
        new FurnitureTransferHandler(home, contentManager, controller);
    this.planTransferHandler = 
        new PlanTransferHandler(home, contentManager, controller);
  }

  /**
   * Adds a property change listener to <code>home</code> to update
   * View from top and View from observer toggle models according to used camera.
   */
  private void addHomeListener(final Home home) {
    home.addPropertyChangeListener(Home.Property.CAMERA, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            viewFromTopToggleModel.setSelected(
                home.getCamera() == home.getTopCamera());
            viewFromObserverToggleModel.setSelected(
                home.getCamera() == home.getObserverCamera());
          }
        });
  }

  /**
   * Adds a property change listener to <code>preferences</code> to update
   * actions when preferred language changes.
   */
  private void addLanguageListener(UserPreferences preferences) {
    preferences.addPropertyChangeListener(UserPreferences.Property.LANGUAGE, 
        new LanguageChangeListener(this));
  }

  /**
   * Preferences property listener bound to this component with a weak reference to avoid
   * strong link between preferences and this component.  
   */
  private static class LanguageChangeListener implements PropertyChangeListener {
    private WeakReference<HomePane> homePane;

    public LanguageChangeListener(HomePane homePane) {
      this.homePane = new WeakReference<HomePane>(homePane);
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
      // If home pane was garbage collected, remove this listener from preferences
      HomePane homePane = this.homePane.get();
      if (homePane == null) {
        ((UserPreferences)ev.getSource()).removePropertyChangeListener(
            UserPreferences.Property.LANGUAGE, this);
      } else {
        homePane.setResourceLanguage((UserPreferences)ev.getSource());
      }
    }
  }
  
  /**
   * Sets the resource bundle from the preferred language, 
   * and updates actions accordingly. 
   */
  private void setResourceLanguage(UserPreferences preferences) {
    this.resource = ResourceBundle.getBundle(HomePane.class.getName());
    ActionMap actions = getActionMap();    
    for (ActionType actionType : ActionType.values()) {
      ((ResourceAction)actions.get(actionType)).setResource(this.resource);
    }
    for (MenuActionType menuActionType : MenuActionType.values()) {
      ((ResourceAction)this.menuActionMap.get(menuActionType)).setResource(this.resource);
    }
    
    SwingTools.updateSwingResourceLanguage();
  }

  /**
   * Adds a property change listener to <code>planController</code> to update
   * Select and Create walls toggle models according to current mode.
   */
  private void addPlanControllerListener(final PlanController planController) {
    planController.addPropertyChangeListener(PlanController.Property.MODE, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            selectToggleModel.setSelected(planController.getMode() 
                == PlanController.Mode.SELECTION);
            createWallsToggleModel.setSelected(planController.getMode() 
                == PlanController.Mode.WALL_CREATION);
            createDimensionLinesToggleModel.setSelected(planController.getMode() 
                == PlanController.Mode.DIMENSION_LINE_CREATION);
          }
        });
  }
  
  /**
   * Returns the menu bar displayed in this pane.
   */
  private JMenuBar createHomeMenuBar(final Home home, 
                                     UserPreferences preferences,
                                     final HomeController controller,
                                     final ContentManager contentManager) {
    // Create File menu
    JMenu fileMenu = new JMenu(this.menuActionMap.get(MenuActionType.FILE_MENU));
    fileMenu.add(getMenuItemAction(ActionType.NEW_HOME));
    fileMenu.add(getMenuItemAction(ActionType.OPEN));
    
    final JMenu openRecentHomeMenu = 
        new JMenu(this.menuActionMap.get(MenuActionType.OPEN_RECENT_HOME_MENU));
    openRecentHomeMenu.addMenuListener(new MenuListener() {
        public void menuSelected(MenuEvent ev) {
          updateOpenRecentHomeMenu(openRecentHomeMenu, controller, contentManager);
        }
      
        public void menuCanceled(MenuEvent ev) {
        }
  
        public void menuDeselected(MenuEvent ev) {
        }
      });
    
    fileMenu.add(openRecentHomeMenu);
    fileMenu.addSeparator();
    fileMenu.add(getMenuItemAction(ActionType.CLOSE));
    fileMenu.add(getMenuItemAction(ActionType.SAVE));
    fileMenu.add(getMenuItemAction(ActionType.SAVE_AS));
    fileMenu.addSeparator();
    fileMenu.add(getMenuItemAction(ActionType.PAGE_SETUP));
    fileMenu.add(getMenuItemAction(ActionType.PRINT_PREVIEW));
    fileMenu.add(getMenuItemAction(ActionType.PRINT));
    // Don't add PRINT_TO_PDF, PREFERENCES and EXIT menu items under Mac OS X, 
    // because PREFERENCES and EXIT items are displayed in application menu
    // and PRINT_TO_PDF is available in standard Mac OS X Print dialog
    if (!OperatingSystem.isMacOSX()) {
      fileMenu.add(getMenuItemAction(ActionType.PRINT_TO_PDF));
      fileMenu.addSeparator();
      fileMenu.add(getMenuItemAction(ActionType.PREFERENCES));
      fileMenu.addSeparator();
      fileMenu.add(getMenuItemAction(ActionType.EXIT));
    }

    // Create Edit menu
    JMenu editMenu = new JMenu(this.menuActionMap.get(MenuActionType.EDIT_MENU));
    editMenu.add(getMenuItemAction(ActionType.UNDO));
    editMenu.add(getMenuItemAction(ActionType.REDO));
    editMenu.addSeparator();
    editMenu.add(getMenuItemAction(ActionType.CUT));
    editMenu.add(getMenuItemAction(ActionType.COPY));
    editMenu.add(getMenuItemAction(ActionType.PASTE));
    editMenu.addSeparator();
    editMenu.add(getMenuItemAction(ActionType.DELETE));
    editMenu.add(getMenuItemAction(ActionType.SELECT_ALL));

    // Create Furniture menu
    JMenu furnitureMenu = new JMenu(this.menuActionMap.get(MenuActionType.FURNITURE_MENU));
    furnitureMenu.add(getMenuItemAction(ActionType.ADD_HOME_FURNITURE));
    furnitureMenu.add(getMenuItemAction(ActionType.MODIFY_FURNITURE));
    furnitureMenu.addSeparator();
    furnitureMenu.add(getMenuItemAction(ActionType.IMPORT_FURNITURE));
    furnitureMenu.add(getMenuItemAction(ActionType.IMPORT_FURNITURE_LIBRARY));
    furnitureMenu.addSeparator();
    furnitureMenu.add(getMenuItemAction(ActionType.ALIGN_FURNITURE_ON_TOP));
    furnitureMenu.add(getMenuItemAction(ActionType.ALIGN_FURNITURE_ON_BOTTOM));
    furnitureMenu.add(getMenuItemAction(ActionType.ALIGN_FURNITURE_ON_LEFT));
    furnitureMenu.add(getMenuItemAction(ActionType.ALIGN_FURNITURE_ON_RIGHT));
    furnitureMenu.addSeparator();
    furnitureMenu.add(createFurnitureSortMenu(home, preferences));
    furnitureMenu.add(createFurnitureDisplayPropertyMenu(home, preferences));
    
    // Create Plan menu
    JMenu planMenu = new JMenu(this.menuActionMap.get(MenuActionType.PLAN_MENU));
    JRadioButtonMenuItem selectRadioButtonMenuItem = getSelectRadioButtonMenuItem(false);
    planMenu.add(selectRadioButtonMenuItem);
    JRadioButtonMenuItem createWallsRadioButtonMenuItem = createCreateWallsRadioButtonMenuItem(false);
    planMenu.add(createWallsRadioButtonMenuItem);
    JRadioButtonMenuItem createDimensionLinesRadioButtonMenuItem = createCreateDimensionLinesRadioButtonMenuItem(false);
    planMenu.add(createDimensionLinesRadioButtonMenuItem);
    // Add Select, Create Walls and Create dimensions menu items to radio group 
    ButtonGroup group = new ButtonGroup();
    group.add(selectRadioButtonMenuItem);
    group.add(createWallsRadioButtonMenuItem);  
    group.add(createDimensionLinesRadioButtonMenuItem);  
    planMenu.addSeparator();
    planMenu.add(getMenuItemAction(ActionType.MODIFY_WALL));
    planMenu.add(getMenuItemAction(ActionType.REVERSE_WALL_DIRECTION));
    planMenu.add(getMenuItemAction(ActionType.SPLIT_WALL));
    planMenu.addSeparator();
    planMenu.add(createImportModifyBackgroundImageMenuItem(home));
    planMenu.add(getMenuItemAction(ActionType.DELETE_BACKGROUND_IMAGE));
    planMenu.addSeparator();
    planMenu.add(getMenuItemAction(ActionType.ZOOM_OUT));
    planMenu.add(getMenuItemAction(ActionType.ZOOM_IN));

    // Create 3D Preview menu
    JMenu preview3DMenu = new JMenu(this.menuActionMap.get(MenuActionType.VIEW_3D_MENU));
    JRadioButtonMenuItem viewFromTopRadioButtonMenuItem = createViewFromTopRadioButtonMenuItem(false);
    preview3DMenu.add(viewFromTopRadioButtonMenuItem);
    JRadioButtonMenuItem viewFromObserverRadioButtonMenuItem = createViewFromObserverRadioButtonMenuItem(false);
    preview3DMenu.add(viewFromObserverRadioButtonMenuItem);
    // Add View from top and View from observer menu items to radio group 
    group = new ButtonGroup();
    group.add(viewFromTopRadioButtonMenuItem);
    group.add(viewFromObserverRadioButtonMenuItem);
    preview3DMenu.addSeparator();
    preview3DMenu.add(getMenuItemAction(ActionType.MODIFY_3D_ATTRIBUTES));
    preview3DMenu.addSeparator();
    preview3DMenu.add(getMenuItemAction(ActionType.EXPORT_TO_OBJ));
    
    // Create Help menu
    JMenu helpMenu = new JMenu(this.menuActionMap.get(MenuActionType.HELP_MENU));
    helpMenu.add(getMenuItemAction(ActionType.HELP));      
    if (!OperatingSystem.isMacOSX()) {
      helpMenu.add(getMenuItemAction(ActionType.ABOUT));      
    }
    
    // Add menus to menu bar
    JMenuBar menuBar = new JMenuBar();
    menuBar.add(fileMenu);
    menuBar.add(editMenu);
    menuBar.add(furnitureMenu);
    menuBar.add(planMenu);
    menuBar.add(preview3DMenu);
    menuBar.add(helpMenu);

    // Add plugin actions menu items
    for (Action pluginAction : this.pluginActions) {
      String pluginMenu = (String)pluginAction.getValue(PluginAction.Property.MENU.toString());
      if (pluginMenu != null) {
        boolean pluginActionAdded = false;
        for (int i = 0; i < menuBar.getMenuCount(); i++) {
          JMenu menu = menuBar.getMenu(i);
          if (menu.getText().equals(pluginMenu)) {
            menu.addSeparator();
            menu.add(new ResourceAction.MenuItemAction(pluginAction));
            pluginActionAdded = true;
            break;
          }
        }
        if (!pluginActionAdded) {
          // Create missing menu before last menu
          JMenu menu = new JMenu(pluginMenu);
          menu.add(new ResourceAction.MenuItemAction(pluginAction));
          menuBar.add(menu, menuBar.getMenuCount() - 1);
        }
      }
    }

    return menuBar;
  }

  /**
   * Returns furniture sort menu.
   */
  private JMenu createFurnitureSortMenu(final Home home, UserPreferences preferences) {
    // Create Furniture Sort submenu
    JMenu sortMenu = new JMenu(this.menuActionMap.get(MenuActionType.SORT_HOME_FURNITURE_MENU));
    // Map sort furniture properties to sort actions
    Map<HomePieceOfFurniture.SortableProperty, Action> sortActions = 
        new LinkedHashMap<HomePieceOfFurniture.SortableProperty, Action>(); 
    // Use catalog id if currency isn't null
    if (preferences.getCurrency() != null) {
      sortActions.put(HomePieceOfFurniture.SortableProperty.CATALOG_ID, 
          getMenuItemAction(ActionType.SORT_HOME_FURNITURE_BY_CATALOG_ID)); 
    }
    sortActions.put(HomePieceOfFurniture.SortableProperty.NAME, 
        getMenuItemAction(ActionType.SORT_HOME_FURNITURE_BY_NAME)); 
    sortActions.put(HomePieceOfFurniture.SortableProperty.WIDTH, 
        getMenuItemAction(ActionType.SORT_HOME_FURNITURE_BY_WIDTH));
    sortActions.put(HomePieceOfFurniture.SortableProperty.DEPTH, 
        getMenuItemAction(ActionType.SORT_HOME_FURNITURE_BY_DEPTH));
    sortActions.put(HomePieceOfFurniture.SortableProperty.HEIGHT, 
        getMenuItemAction(ActionType.SORT_HOME_FURNITURE_BY_HEIGHT));
    sortActions.put(HomePieceOfFurniture.SortableProperty.X, 
        getMenuItemAction(ActionType.SORT_HOME_FURNITURE_BY_X));
    sortActions.put(HomePieceOfFurniture.SortableProperty.Y, 
        getMenuItemAction(ActionType.SORT_HOME_FURNITURE_BY_Y));
    sortActions.put(HomePieceOfFurniture.SortableProperty.ELEVATION, 
        getMenuItemAction(ActionType.SORT_HOME_FURNITURE_BY_ELEVATION));
    sortActions.put(HomePieceOfFurniture.SortableProperty.ANGLE, 
        getMenuItemAction(ActionType.SORT_HOME_FURNITURE_BY_ANGLE));
    sortActions.put(HomePieceOfFurniture.SortableProperty.COLOR, 
        getMenuItemAction(ActionType.SORT_HOME_FURNITURE_BY_COLOR));
    sortActions.put(HomePieceOfFurniture.SortableProperty.MOVABLE, 
        getMenuItemAction(ActionType.SORT_HOME_FURNITURE_BY_MOVABILITY));
    sortActions.put(HomePieceOfFurniture.SortableProperty.DOOR_OR_WINDOW, 
        getMenuItemAction(ActionType.SORT_HOME_FURNITURE_BY_TYPE));
    sortActions.put(HomePieceOfFurniture.SortableProperty.VISIBLE, 
        getMenuItemAction(ActionType.SORT_HOME_FURNITURE_BY_VISIBILITY));
    // Use prices if currency isn't null
    if (preferences.getCurrency() != null) {
      sortActions.put(HomePieceOfFurniture.SortableProperty.PRICE, 
          getMenuItemAction(ActionType.SORT_HOME_FURNITURE_BY_PRICE));
      sortActions.put(HomePieceOfFurniture.SortableProperty.VALUE_ADDED_TAX_PERCENTAGE, 
          getMenuItemAction(ActionType.SORT_HOME_FURNITURE_BY_VALUE_ADDED_TAX_PERCENTAGE));
      sortActions.put(HomePieceOfFurniture.SortableProperty.VALUE_ADDED_TAX, 
          getMenuItemAction(ActionType.SORT_HOME_FURNITURE_BY_VALUE_ADDED_TAX));
      sortActions.put(HomePieceOfFurniture.SortableProperty.PRICE_VALUE_ADDED_TAX_INCLUDED, 
          getMenuItemAction(ActionType.SORT_HOME_FURNITURE_BY_PRICE_VALUE_ADDED_TAX_INCLUDED));
    }
    // Add radio button menu items to sub menu and make them share the same radio button group
    ButtonGroup sortButtonGroup = new ButtonGroup();
    for (Map.Entry<HomePieceOfFurniture.SortableProperty, Action> entry : sortActions.entrySet()) {
      final HomePieceOfFurniture.SortableProperty furnitureProperty = entry.getKey();
      Action sortAction = entry.getValue();
      JRadioButtonMenuItem sortMenuItem = new JRadioButtonMenuItem();
      // Use a special model for sort radio button menu item that is selected if
      // home is sorted on furnitureProperty criterion
      sortMenuItem.setModel(new JToggleButton.ToggleButtonModel() {
          @Override
          public boolean isSelected() {
            return furnitureProperty == home.getFurnitureSortedProperty();
          }
        }); 
      // Configure check box menu item action after setting its model to avoid losing its mnemonic
      sortMenuItem.setAction(sortAction);
      sortMenu.add(sortMenuItem);
      sortButtonGroup.add(sortMenuItem);
    }
    sortMenu.addSeparator();
    JCheckBoxMenuItem sortOrderCheckBoxMenuItem = new JCheckBoxMenuItem();
    // Use a special model for sort order check box menu item that is selected depending on
    // home sort order property
    sortOrderCheckBoxMenuItem.setModel(new JToggleButton.ToggleButtonModel() {
        @Override
        public boolean isSelected() {
          return home.isFurnitureDescendingSorted();
        }
      });
    sortOrderCheckBoxMenuItem.setAction(
        getMenuItemAction(ActionType.SORT_HOME_FURNITURE_BY_DESCENDING_ORDER));
    sortMenu.add(sortOrderCheckBoxMenuItem);
    return sortMenu;
  }
  
  /**
   * Returns furniture display property menu.
   */
  private JMenu createFurnitureDisplayPropertyMenu(final Home home, UserPreferences preferences) {
    // Create Furniture Display property submenu
    JMenu displayPropertyMenu = new JMenu(
        this.menuActionMap.get(MenuActionType.DISPLAY_HOME_FURNITURE_PROPERTY_MENU));
    // Map displayProperty furniture properties to displayProperty actions
    Map<HomePieceOfFurniture.SortableProperty, Action> displayPropertyActions = 
        new LinkedHashMap<HomePieceOfFurniture.SortableProperty, Action>(); 
    // Use catalog id if currency isn't null
    if (preferences.getCurrency() != null) {
      displayPropertyActions.put(HomePieceOfFurniture.SortableProperty.CATALOG_ID, 
          getMenuItemAction(ActionType.DISPLAY_HOME_FURNITURE_CATALOG_ID)); 
    }
    displayPropertyActions.put(HomePieceOfFurniture.SortableProperty.NAME, 
        getMenuItemAction(ActionType.DISPLAY_HOME_FURNITURE_NAME)); 
    displayPropertyActions.put(HomePieceOfFurniture.SortableProperty.WIDTH, 
        getMenuItemAction(ActionType.DISPLAY_HOME_FURNITURE_WIDTH));
    displayPropertyActions.put(HomePieceOfFurniture.SortableProperty.DEPTH, 
        getMenuItemAction(ActionType.DISPLAY_HOME_FURNITURE_DEPTH));
    displayPropertyActions.put(HomePieceOfFurniture.SortableProperty.HEIGHT, 
        getMenuItemAction(ActionType.DISPLAY_HOME_FURNITURE_HEIGHT));
    displayPropertyActions.put(HomePieceOfFurniture.SortableProperty.X, 
        getMenuItemAction(ActionType.DISPLAY_HOME_FURNITURE_X));
    displayPropertyActions.put(HomePieceOfFurniture.SortableProperty.Y, 
        getMenuItemAction(ActionType.DISPLAY_HOME_FURNITURE_Y));
    displayPropertyActions.put(HomePieceOfFurniture.SortableProperty.ELEVATION, 
        getMenuItemAction(ActionType.DISPLAY_HOME_FURNITURE_ELEVATION));
    displayPropertyActions.put(HomePieceOfFurniture.SortableProperty.ANGLE, 
        getMenuItemAction(ActionType.DISPLAY_HOME_FURNITURE_ANGLE));
    displayPropertyActions.put(HomePieceOfFurniture.SortableProperty.COLOR, 
        getMenuItemAction(ActionType.DISPLAY_HOME_FURNITURE_COLOR));
    displayPropertyActions.put(HomePieceOfFurniture.SortableProperty.MOVABLE, 
        getMenuItemAction(ActionType.DISPLAY_HOME_FURNITURE_MOVABLE));
    displayPropertyActions.put(HomePieceOfFurniture.SortableProperty.DOOR_OR_WINDOW, 
        getMenuItemAction(ActionType.DISPLAY_HOME_FURNITURE_DOOR_OR_WINDOW));
    displayPropertyActions.put(HomePieceOfFurniture.SortableProperty.VISIBLE, 
        getMenuItemAction(ActionType.DISPLAY_HOME_FURNITURE_VISIBLE));
    // Use prices if currency isn't null
    if (preferences.getCurrency() != null) {
      displayPropertyActions.put(HomePieceOfFurniture.SortableProperty.PRICE, 
          getMenuItemAction(ActionType.DISPLAY_HOME_FURNITURE_PRICE));
      displayPropertyActions.put(HomePieceOfFurniture.SortableProperty.VALUE_ADDED_TAX_PERCENTAGE, 
          getMenuItemAction(ActionType.DISPLAY_HOME_FURNITURE_VALUE_ADDED_TAX_PERCENTAGE));
      displayPropertyActions.put(HomePieceOfFurniture.SortableProperty.VALUE_ADDED_TAX, 
          getMenuItemAction(ActionType.DISPLAY_HOME_FURNITURE_VALUE_ADDED_TAX));
      displayPropertyActions.put(HomePieceOfFurniture.SortableProperty.PRICE_VALUE_ADDED_TAX_INCLUDED, 
          getMenuItemAction(ActionType.DISPLAY_HOME_FURNITURE_PRICE_VALUE_ADDED_TAX_INCLUDED));
    }
    // Add radio button menu items to sub menu 
    for (Map.Entry<HomePieceOfFurniture.SortableProperty, Action> entry : displayPropertyActions.entrySet()) {
      final HomePieceOfFurniture.SortableProperty furnitureProperty = entry.getKey();
      Action displayPropertyAction = entry.getValue();
      JCheckBoxMenuItem displayPropertyMenuItem = new JCheckBoxMenuItem();
      // Use a special model for displayProperty check box menu item that is selected if
      // home furniture visible properties contains furnitureProperty
      displayPropertyMenuItem.setModel(new JToggleButton.ToggleButtonModel() {
          @Override
          public boolean isSelected() {
            return home.getFurnitureVisibleProperties().contains(furnitureProperty);
          }
        }); 
      // Configure check box menu item action after setting its model to avoid losing its mnemonic
      displayPropertyMenuItem.setAction(displayPropertyAction);
      displayPropertyMenu.add(displayPropertyMenuItem);
    }
    return displayPropertyMenu;
  }
  
  /**
   * Returns Import / Modify background image menu item.
   */
  private JMenuItem createImportModifyBackgroundImageMenuItem(final Home home) {
    final JMenuItem importModifyBackgroundImageMenuItem = new JMenuItem( 
        getMenuItemAction(home.getBackgroundImage() == null 
            ? ActionType.IMPORT_BACKGROUND_IMAGE
            : ActionType.MODIFY_BACKGROUND_IMAGE));
    // Add a listener to home on backgroundImage property change to 
    // switch action according to backgroundImage change
    home.addPropertyChangeListener(Home.Property.BACKGROUND_IMAGE, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            importModifyBackgroundImageMenuItem.setAction(
                getMenuItemAction(home.getBackgroundImage() == null 
                    ? ActionType.IMPORT_BACKGROUND_IMAGE
                    : ActionType.MODIFY_BACKGROUND_IMAGE));
          }
        });    
    return importModifyBackgroundImageMenuItem;
  }
  
  /**
   * Updates <code>openRecentHomeMenu</code> from current recent homes in preferences.
   */
  protected void updateOpenRecentHomeMenu(JMenu openRecentHomeMenu, 
                                          final HomeController controller, 
                                          ContentManager contentManager) {
    openRecentHomeMenu.removeAll();
    for (final String homeName : controller.getRecentHomes()) {
      openRecentHomeMenu.add(
          new AbstractAction(contentManager.getPresentationName(
                  homeName, ContentManager.ContentType.SWEET_HOME_3D)) {
            public void actionPerformed(ActionEvent e) {
              controller.open(homeName);
            }
          });
    }
    if (openRecentHomeMenu.getMenuComponentCount() > 0) {
      openRecentHomeMenu.addSeparator();
    }
    openRecentHomeMenu.add(getMenuItemAction(ActionType.DELETE_RECENT_HOMES));
  }

  /**
   * Returns an action decorated for menu items.
   */
  private Action getMenuItemAction(ActionType actionType) {
    return new ResourceAction.MenuItemAction(getActionMap().get(actionType));
  }

  /**
   * Returns an action decorated for popup menu items.
   */
  private Action getPopupMenuItemAction(ActionType actionType) {
    return new ResourceAction.PopupMenuItemAction(getActionMap().get(actionType));
  }

  /**
   * Returns an action decorated for tool bar buttons.
   */
  private Action getToolBarAction(ActionType actionType) {
    return new ResourceAction.ToolBarAction(getActionMap().get(actionType));
  }

  /**
   * Returns a radio button menu item for Select action. 
   */
  private JRadioButtonMenuItem getSelectRadioButtonMenuItem(boolean popup) {
    return createRadioButtonMenuItemFromModel(this.selectToggleModel, 
        ActionType.SELECT, popup);
  }
  
  /**
   * Returns a radio button menu item for Create walls action. 
   */
  private JRadioButtonMenuItem createCreateWallsRadioButtonMenuItem(boolean popup) {
    return createRadioButtonMenuItemFromModel(this.createWallsToggleModel, 
        ActionType.CREATE_WALLS, popup);
  }
  
  /**
   * Returns a radio button menu item for Create dimensions action. 
   */
  private JRadioButtonMenuItem createCreateDimensionLinesRadioButtonMenuItem(boolean popup) {
    return createRadioButtonMenuItemFromModel(this.createDimensionLinesToggleModel, 
        ActionType.CREATE_DIMENSION_LINES, popup);
  }
  
  /**
   * Returns a radio button menu item for View from top action. 
   */
  private JRadioButtonMenuItem createViewFromTopRadioButtonMenuItem(boolean popup) {
    return createRadioButtonMenuItemFromModel(this.viewFromTopToggleModel, 
        ActionType.VIEW_FROM_TOP, popup);
  }
  
  /**
   * Returns a radio button menu item for View from observer action. 
   */
  private JRadioButtonMenuItem createViewFromObserverRadioButtonMenuItem(boolean popup) {
    return createRadioButtonMenuItemFromModel(this.viewFromObserverToggleModel, 
        ActionType.VIEW_FROM_OBSERVER, popup);
  }
  
  private JRadioButtonMenuItem createRadioButtonMenuItemFromModel(
                                   JToggleButton.ToggleButtonModel model,
                                   ActionType action,
                                   boolean popup) {
    JRadioButtonMenuItem radioButtonMenuItem = new JRadioButtonMenuItem();
    // Configure shared model
    radioButtonMenuItem.setModel(model);
    // Configure check box menu item action after setting its model to avoid losing its mnemonic
    radioButtonMenuItem.setAction(
        popup ? getPopupMenuItemAction(action)
              : getMenuItemAction(action));
    return radioButtonMenuItem;
  }
  
  /**
   * Returns the tool bar displayed in this pane.
   */
  private JToolBar createToolBar() {
    final JToolBar toolBar = new JToolBar();
    toolBar.add(getToolBarAction(ActionType.NEW_HOME));
    toolBar.add(getToolBarAction(ActionType.OPEN));
    toolBar.add(getToolBarAction(ActionType.SAVE));
    toolBar.addSeparator();

    toolBar.add(getToolBarAction(ActionType.UNDO));
    toolBar.add(getToolBarAction(ActionType.REDO));
    toolBar.add(Box.createRigidArea(new Dimension(2, 2)));
    toolBar.add(getToolBarAction(ActionType.CUT));
    toolBar.add(getToolBarAction(ActionType.COPY));
    toolBar.add(getToolBarAction(ActionType.PASTE));
    toolBar.add(Box.createRigidArea(new Dimension(2, 2)));
    toolBar.add(getToolBarAction(ActionType.DELETE));
    toolBar.addSeparator();

    toolBar.add(getToolBarAction(ActionType.ADD_HOME_FURNITURE));
    toolBar.add(getToolBarAction(ActionType.IMPORT_FURNITURE));
    toolBar.add(Box.createRigidArea(new Dimension(2, 2)));
    toolBar.add(getToolBarAction(ActionType.ALIGN_FURNITURE_ON_TOP));
    toolBar.add(getToolBarAction(ActionType.ALIGN_FURNITURE_ON_BOTTOM));
    toolBar.add(getToolBarAction(ActionType.ALIGN_FURNITURE_ON_LEFT));
    toolBar.add(getToolBarAction(ActionType.ALIGN_FURNITURE_ON_RIGHT));
    toolBar.addSeparator();
   
    JToggleButton selectToggleButton = 
        new JToggleButton(getToolBarAction(ActionType.SELECT));
    // Use the same model as Select menu item
    selectToggleButton.setModel(this.selectToggleModel);
    toolBar.add(selectToggleButton);
    JToggleButton createWallsToggleButton = 
        new JToggleButton(getToolBarAction(ActionType.CREATE_WALLS));
    // Use the same model as Create walls menu item
    createWallsToggleButton.setModel(this.createWallsToggleModel);
    toolBar.add(createWallsToggleButton);
    JToggleButton createDimensionLinesToggleButton = 
        new JToggleButton(getToolBarAction(ActionType.CREATE_DIMENSION_LINES));
    // Use the same model as Create dimensions menu item
    createDimensionLinesToggleButton.setModel(this.createDimensionLinesToggleModel);
    toolBar.add(createDimensionLinesToggleButton);
    // Add Select, Create Walls and Create dimensions buttons to radio group 
    ButtonGroup group = new ButtonGroup();
    group.add(selectToggleButton);
    group.add(createWallsToggleButton);
    group.add(createDimensionLinesToggleButton);
    toolBar.add(Box.createRigidArea(new Dimension(2, 2)));
    
    toolBar.add(getToolBarAction(ActionType.ZOOM_OUT));
    toolBar.add(getToolBarAction(ActionType.ZOOM_IN));
    toolBar.addSeparator();
    
    // Add plugin actions buttons
    boolean pluginActionsAdded = false;
    for (Action pluginAction : this.pluginActions) {
      if (Boolean.TRUE.equals(pluginAction.getValue(PluginAction.Property.IN_TOOL_BAR.toString()))) {
        toolBar.add(new ResourceAction.ToolBarAction(pluginAction));
        pluginActionsAdded = true;
      }
    }
    if (pluginActionsAdded) {
      toolBar.addSeparator();
    }
    
    toolBar.add(getToolBarAction(ActionType.HELP));
    
    updateToolBarButtons(toolBar);
    // Update toolBar buttons when component orientation changes 
    // and when buttons are added or removed to it  
    toolBar.addPropertyChangeListener("componentOrientation", 
        new PropertyChangeListener () {
          public void propertyChange(PropertyChangeEvent evt) {
            updateToolBarButtons(toolBar);
          }
        });
    toolBar.addContainerListener(new ContainerListener() {
        public void componentAdded(ContainerEvent ev) {
          updateToolBarButtons(toolBar);
        }
        
        public void componentRemoved(ContainerEvent ev) {
          updateToolBarButtons(toolBar);
        }
      });
    
    return toolBar;
  }

  /**
   * Ensures that all the children of toolBar aren't focusable. 
   * Under Mac OS X 10.5, it also uses segmented buttons and groups them depending
   * on toolbar orientation and whether a button is after or before a separator.
   */
  private void updateToolBarButtons(final JToolBar toolBar) {
    // Retrieve component orientation because Mac OS X 10.5 miserably doesn't it take into account 
    ComponentOrientation orientation = toolBar.getComponentOrientation();
    Component previousComponent = null;
    for (int i = 0, n = toolBar.getComponentCount(); i < n; i++) {        
      JComponent component = (JComponent)toolBar.getComponentAtIndex(i); 
      // Remove focusable property on buttons
      component.setFocusable(false);
      
      if (!(component instanceof AbstractButton)) {
        previousComponent = null;
        continue;
      }          
      if (OperatingSystem.isMacOSXLeopardOrSuperior()) {
        Component nextComponent;
        if (i < n - 1) {
          nextComponent = toolBar.getComponentAtIndex(i + 1);
        } else {
          nextComponent = null;
        }
        component.putClientProperty("JButton.buttonType", "segmentedTextured");
        if (previousComponent == null
            && !(nextComponent instanceof AbstractButton)) {
          component.putClientProperty("JButton.segmentPosition", "only");
        } else if (previousComponent == null) {
          component.putClientProperty("JButton.segmentPosition", 
              orientation == ComponentOrientation.LEFT_TO_RIGHT 
                ? "first"
                : "last");
        } else if (!(nextComponent instanceof AbstractButton)) {
          component.putClientProperty("JButton.segmentPosition",
              orientation == ComponentOrientation.LEFT_TO_RIGHT 
                ? "last"
                : "first");
        } else {
          component.putClientProperty("JButton.segmentPosition", "middle");
        }
        previousComponent = component;
      }
    }
  }
  
  /**
   * Enables or disables the action matching <code>actionType</code>.
   */
  public void setEnabled(ActionType actionType, 
                         boolean enabled) {
    getActionMap().get(actionType).setEnabled(enabled);
  }
  
  /**
   * Sets the <code>NAME</code> and <code>SHORT_DESCRIPTION</code> properties value 
   * of undo and redo actions. If a parameter is null,
   * the properties will be reset to their initial values.
   */
  public void setUndoRedoName(String undoText, String redoText) {
    setNameAndShortDescription(ActionType.UNDO, undoText);
    setNameAndShortDescription(ActionType.REDO, redoText);
  }
  
  /**
   * Sets the <code>NAME</code> and <code>SHORT_DESCRIPTION</code> properties value 
   * matching <code>actionType</code>. If <code>name</code> is null,
   * the properties will be reset to their initial values.
   */
  private void setNameAndShortDescription(ActionType actionType, String name) {
    Action action = getActionMap().get(actionType);
    if (name == null) {
      name = (String)action.getValue(Action.DEFAULT);
    }
    action.putValue(Action.NAME, name);
    action.putValue(Action.SHORT_DESCRIPTION, name);
  }

  /**
   * Enables or disables transfer between components.  
   */
  public void setTransferEnabled(boolean enabled) {
    if (enabled) {
      this.controller.getCatalogController().getView().setTransferHandler(this.catalogTransferHandler);
      this.controller.getFurnitureController().getView().setTransferHandler(this.furnitureTransferHandler);
      this.controller.getPlanController().getView().setTransferHandler(this.planTransferHandler);
      ((JViewport)this.controller.getFurnitureController().getView().getParent()).
          setTransferHandler(this.furnitureTransferHandler);
    } else {
      this.controller.getCatalogController().getView().setTransferHandler(null);
      this.controller.getFurnitureController().getView().setTransferHandler(null);
      this.controller.getPlanController().getView().setTransferHandler(null);
      ((JViewport)this.controller.getFurnitureController().getView().getParent()).
          setTransferHandler(null);
    }
  }

  /**
   * Returns the main pane with catalog tree, furniture table and plan pane. 
   */
  private JComponent createMainPane(Home home, UserPreferences preferences, 
                                    HomeController controller) {
    JSplitPane mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
        createCatalogFurniturePane(home, preferences, controller), 
        createPlanView3DPane(home, preferences, controller));
    configureSplitPane(mainPane, home, MAIN_PANE_DIVIDER_LOCATION_VISUAL_PROPERTY, 0.3, controller);
    return mainPane;
  }

  /**
   * Configures <code>splitPane</code> divider location. 
   * If <code>dividerLocationProperty</code> visual property exists in <code>home</code>,
   * its value will be used, otherwise the given resize weight will be used.
   */
  private void configureSplitPane(final JSplitPane splitPane,
                                  Home home,
                                  final String dividerLocationProperty,
                                  double defaultResizeWeight, 
                                  final HomeController controller) {
    splitPane.setContinuousLayout(true);
    splitPane.setOneTouchExpandable(true);
    splitPane.setResizeWeight(defaultResizeWeight);
    // Restore divider location previously set 
    Integer dividerLocation = (Integer)home.getVisualProperty(dividerLocationProperty);
    if (dividerLocation != null) {
      splitPane.setDividerLocation(dividerLocation);
    }
    splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
            if (focusOwner != null && isChildComponentInvisible(focusOwner)) {
              List<JComponent> splitPanesFocusableComponents = Arrays.asList(new JComponent [] {
                  controller.getCatalogController().getView(),
                  controller.getFurnitureController().getView(),
                  controller.getPlanController().getView(),
                  controller.getHomeController3D().getView()});      
              // Find the first child component that is visible among split panes
              int focusOwnerIndex = splitPanesFocusableComponents.indexOf(focusOwner);
              for (int i = 1; i < splitPanesFocusableComponents.size(); i++) {
                JComponent focusableComponent = splitPanesFocusableComponents.get(
                    (focusOwnerIndex + i) % splitPanesFocusableComponents.size());
                if (!isChildComponentInvisible(focusableComponent)) {
                  focusableComponent.requestFocusInWindow();
                  break;
                }
              }
            }
            controller.setVisualProperty(dividerLocationProperty, ev.getNewValue());
          }

          /**
           * Returns <code>true</code> if the top or the bottom component is a parent 
           * of the given child component and is too small enough to show it. 
           */
          private boolean isChildComponentInvisible(Component childComponent) {
            return (SwingUtilities.isDescendingFrom(childComponent, splitPane.getTopComponent())
                 && splitPane.getDividerLocation() < splitPane.getMinimumDividerLocation())
                || (SwingUtilities.isDescendingFrom(childComponent, splitPane.getBottomComponent())
                    && splitPane.getDividerLocation() > splitPane.getMaximumDividerLocation());
          }
        });
  }

  /**
   * Returns the catalog tree and furniture table pane. 
   */
  private JComponent createCatalogFurniturePane(Home home,
                                                UserPreferences preferences,
                                                final HomeController controller) {
    JComponent catalogView = controller.getCatalogController().getView();
    JScrollPane catalogScrollPane = new HomeScrollPane(catalogView);
    // Add focus listener to catalog tree
    catalogView.addFocusListener(new FocusableViewListener(
        controller, catalogScrollPane));
    
    // Create catalog view popup menu
    JPopupMenu catalogViewPopup = new JPopupMenu();
    catalogViewPopup.add(getPopupMenuItemAction(ActionType.COPY));
    catalogViewPopup.addSeparator();
    catalogViewPopup.add(getPopupMenuItemAction(ActionType.DELETE));
    catalogViewPopup.addSeparator();
    catalogViewPopup.add(getPopupMenuItemAction(ActionType.ADD_HOME_FURNITURE));
    catalogViewPopup.add(getPopupMenuItemAction(ActionType.MODIFY_FURNITURE));
    catalogViewPopup.addSeparator();
    catalogViewPopup.add(getPopupMenuItemAction(ActionType.IMPORT_FURNITURE));
    catalogView.setComponentPopupMenu(catalogViewPopup);

    // Configure furniture view
    final JComponent furnitureView = controller.getFurnitureController().getView();
    JScrollPane furnitureScrollPane = new HomeScrollPane(furnitureView);
    // Set default traversal keys of furniture view
    KeyboardFocusManager focusManager =
        KeyboardFocusManager.getCurrentKeyboardFocusManager();
    furnitureView.setFocusTraversalKeys(
        KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
        focusManager.getDefaultFocusTraversalKeys(
            KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
    furnitureView.setFocusTraversalKeys(
        KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
        focusManager.getDefaultFocusTraversalKeys(
            KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));

    // Add focus listener to furniture table 
    furnitureView.addFocusListener(new FocusableViewListener(
        controller, furnitureScrollPane));
    // Add a mouse listener that gives focus to furniture view when
    // user clicks in its viewport
    final JViewport viewport = furnitureScrollPane.getViewport();
    viewport.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent ev) {
            furnitureView.requestFocusInWindow();
          }
        });    
    Integer viewportY = (Integer)home.getVisualProperty(FURNITURE_VIEWPORT_Y_VISUAL_PROPERTY);
    if (viewportY != null) {
      viewport.setViewPosition(new Point(0, viewportY));
    }
    viewport.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.setVisualProperty(FURNITURE_VIEWPORT_Y_VISUAL_PROPERTY, viewport.getViewPosition().y);
        }
      });
    
    // Create furniture view popup menu
    JPopupMenu furnitureViewPopup = new JPopupMenu();
    furnitureViewPopup.add(getPopupMenuItemAction(ActionType.UNDO));
    furnitureViewPopup.add(getPopupMenuItemAction(ActionType.REDO));
    furnitureViewPopup.addSeparator();
    furnitureViewPopup.add(getPopupMenuItemAction(ActionType.CUT));
    furnitureViewPopup.add(getPopupMenuItemAction(ActionType.COPY));
    furnitureViewPopup.add(getPopupMenuItemAction(ActionType.PASTE));
    furnitureViewPopup.addSeparator();
    furnitureViewPopup.add(getPopupMenuItemAction(ActionType.DELETE));
    furnitureViewPopup.add(getPopupMenuItemAction(ActionType.SELECT_ALL));
    furnitureViewPopup.addSeparator();
    furnitureViewPopup.add(getPopupMenuItemAction(ActionType.MODIFY_FURNITURE));
    furnitureViewPopup.addSeparator();
    furnitureViewPopup.add(createFurnitureSortMenu(home, preferences));
    furnitureViewPopup.add(createFurnitureDisplayPropertyMenu(home, preferences));
    furnitureView.setComponentPopupMenu(furnitureViewPopup);
    ((JViewport)furnitureView.getParent()).setComponentPopupMenu(furnitureViewPopup);
    
    // Create a split pane that displays both components
    JSplitPane catalogFurniturePane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
        catalogScrollPane, furnitureScrollPane);
    configureSplitPane(catalogFurniturePane, home, 
        CATALOG_PANE_DIVIDER_LOCATION_VISUAL_PROPERTY, 0.5, controller);
    return catalogFurniturePane;
  }

  /**
   * Returns the plan view and 3D view pane. 
   */
  private JComponent createPlanView3DPane(Home home, UserPreferences preferences, 
                                       final HomeController controller) {
    JComponent planView = controller.getPlanController().getView();
    JScrollPane planScrollPane = new HomeScrollPane(planView);
    setPlanRulersVisible(planScrollPane, controller, preferences.isRulersVisible());
    // Add a listener to update rulers visibility in preferences
    preferences.addPropertyChangeListener(UserPreferences.Property.RULERS_VISIBLE, 
        new RulersVisibilityChangeListener(this, planScrollPane, controller));
    planView.addFocusListener(new FocusableViewListener(controller, planScrollPane));
    // Restore viewport position if it exists
    final JViewport viewport = planScrollPane.getViewport();
    Integer viewportX = (Integer)home.getVisualProperty(PLAN_VIEWPORT_X_VISUAL_PROPERTY);
    Integer viewportY = (Integer)home.getVisualProperty(PLAN_VIEWPORT_Y_VISUAL_PROPERTY);
    if (viewportX != null && viewportY != null) {
      viewport.setViewPosition(new Point(viewportX, viewportY));
    }
    viewport.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          Point viewportPosition = viewport.getViewPosition();
          controller.setVisualProperty(PLAN_VIEWPORT_X_VISUAL_PROPERTY, viewportPosition.x);
          controller.setVisualProperty(PLAN_VIEWPORT_Y_VISUAL_PROPERTY, viewportPosition.y);
        }
      });

    // Create plan view popup menu
    JPopupMenu planViewPopup = new JPopupMenu();
    planViewPopup.add(getPopupMenuItemAction(ActionType.UNDO));
    planViewPopup.add(getPopupMenuItemAction(ActionType.REDO));
    planViewPopup.addSeparator();
    planViewPopup.add(getPopupMenuItemAction(ActionType.CUT));
    planViewPopup.add(getPopupMenuItemAction(ActionType.COPY));
    planViewPopup.add(getPopupMenuItemAction(ActionType.PASTE));
    planViewPopup.addSeparator();
    planViewPopup.add(getPopupMenuItemAction(ActionType.DELETE));
    planViewPopup.add(getPopupMenuItemAction(ActionType.SELECT_ALL));
    planViewPopup.addSeparator();
    JRadioButtonMenuItem selectRadioButtonMenuItem = getSelectRadioButtonMenuItem(true);
    planViewPopup.add(selectRadioButtonMenuItem);
    JRadioButtonMenuItem createWallsRadioButtonMenuItem = createCreateWallsRadioButtonMenuItem(true);
    planViewPopup.add(createWallsRadioButtonMenuItem);
    JRadioButtonMenuItem createDimensionLinesRadioButtonMenuItem = createCreateDimensionLinesRadioButtonMenuItem(true);
    planViewPopup.add(createDimensionLinesRadioButtonMenuItem);
    // Add Select and Create Walls menu items to radio group 
    ButtonGroup group = new ButtonGroup();
    group.add(selectRadioButtonMenuItem);
    group.add(createWallsRadioButtonMenuItem);
    group.add(createDimensionLinesRadioButtonMenuItem);
    planViewPopup.addSeparator();
    planViewPopup.add(getPopupMenuItemAction(ActionType.MODIFY_FURNITURE));
    planViewPopup.add(getPopupMenuItemAction(ActionType.MODIFY_WALL));
    planViewPopup.add(getPopupMenuItemAction(ActionType.REVERSE_WALL_DIRECTION));
    planViewPopup.add(getPopupMenuItemAction(ActionType.SPLIT_WALL));
    planViewPopup.addSeparator();
    planViewPopup.add(createImportModifyBackgroundImageMenuItem(home));
    planViewPopup.add(getMenuItemAction(ActionType.DELETE_BACKGROUND_IMAGE));
    planViewPopup.addSeparator();
    planViewPopup.add(getPopupMenuItemAction(ActionType.ZOOM_OUT));
    planViewPopup.add(getPopupMenuItemAction(ActionType.ZOOM_IN));
    planView.setComponentPopupMenu(planViewPopup);
    
    // Configure 3D view
    JComponent view3D = controller.getHomeController3D().getView();
    view3D.setPreferredSize(planView.getPreferredSize());
    view3D.setMinimumSize(new Dimension(0, 0));
    view3D.addFocusListener(new FocusableViewListener(controller, view3D));
    // Create 3D view popup menu
    JPopupMenu view3DPopup = new JPopupMenu();
    JRadioButtonMenuItem viewFromTopRadioButtonMenuItem = createViewFromTopRadioButtonMenuItem(true);
    view3DPopup.add(viewFromTopRadioButtonMenuItem);
    JRadioButtonMenuItem viewFromObserverRadioButtonMenuItem = createViewFromObserverRadioButtonMenuItem(true);
    view3DPopup.add(viewFromObserverRadioButtonMenuItem);
    // Add View from top and View from observer menu items to radio group 
    group = new ButtonGroup();
    group.add(viewFromTopRadioButtonMenuItem);
    group.add(viewFromObserverRadioButtonMenuItem);
    view3D.setComponentPopupMenu(view3DPopup);
    view3DPopup.addSeparator();
    view3DPopup.add(getMenuItemAction(ActionType.MODIFY_3D_ATTRIBUTES));
    view3DPopup.addSeparator();
    view3DPopup.add(getMenuItemAction(ActionType.EXPORT_TO_OBJ));
    
    // Create a split pane that displays both components
    JSplitPane planView3DPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
        planScrollPane, view3D);
    configureSplitPane(planView3DPane, home, 
        PLAN_PANE_DIVIDER_LOCATION_VISUAL_PROPERTY, 0.5, controller);
    return planView3DPane;
  }
  
  /**
   * Preferences property listener bound to this component with a weak reference to avoid
   * strong link between preferences and this component.  
   */
  private static class RulersVisibilityChangeListener implements PropertyChangeListener {
    private WeakReference<HomePane>       homePane;
    private WeakReference<JScrollPane>    planScrollPane;
    private WeakReference<HomeController> controller;

    public RulersVisibilityChangeListener(HomePane homePane,
                                          JScrollPane planScrollPane, 
                                          HomeController controller) {
      this.homePane = new WeakReference<HomePane>(homePane);
      this.planScrollPane = new WeakReference<JScrollPane>(planScrollPane);
      this.controller = new WeakReference<HomeController>(controller);
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
      // If home pane was garbage collected, remove this listener from preferences
      HomePane homePane = this.homePane.get();
      JScrollPane planScrollPane = this.planScrollPane.get();
      HomeController controller = this.controller.get();
      if (homePane == null
          || planScrollPane == null
          || controller == null) {
        ((UserPreferences)ev.getSource()).removePropertyChangeListener(
            UserPreferences.Property.RULERS_VISIBLE, this);
      } else {
        homePane.setPlanRulersVisible(planScrollPane, controller, (Boolean)ev.getNewValue());
      }
    }
  }

  /**
   * Sets the rulers visible in plan view.
   */
  private void setPlanRulersVisible(JScrollPane planScrollPane, 
                                    HomeController controller, boolean visible) {
    if (visible) {
      // Change column and row header views
      planScrollPane.setColumnHeaderView(
          controller.getPlanController().getHorizontalRulerView());
      planScrollPane.setRowHeaderView(
          controller.getPlanController().getVerticalRulerView());
    } else {
      planScrollPane.setColumnHeaderView(null);
      planScrollPane.setRowHeaderView(null);
    }
  }
  
  /**
   * Adds to <code>view</code> a mouse listener that disables all menu items of
   * <code>menuBar</code> during a drag and drop operation in <code>view</code>.
   */
  private void disableMenuItemsDuringDragAndDrop(JComponent view, 
                                                 final JMenuBar menuBar) {
    class MouseAndFocusListener extends MouseAdapter implements FocusListener {      
      @Override
      public void mousePressed(MouseEvent ev) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
              for (int i = 0, n = menuBar.getMenuCount(); i < n; i++) {
                setMenuItemsEnabled(menuBar.getMenu(i), false);
              }
            }
          });
      }
      
      @Override
      public void mouseReleased(MouseEvent ev) {
        enableMenuItems(menuBar);
      }

      private void enableMenuItems(final JMenuBar menuBar) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
              for (int i = 0, n = menuBar.getMenuCount(); i < n; i++) {
                setMenuItemsEnabled(menuBar.getMenu(i), true);
              }
            }
          });
      }

      private void setMenuItemsEnabled(JMenu menu, boolean enabled) {
        for (int i = 0, n = menu.getItemCount(); i < n; i++) {
          JMenuItem item = menu.getItem(i);
          if (item instanceof JMenu) {
            setMenuItemsEnabled((JMenu)item, enabled);
          } else if (item != null) {
            item.setEnabled(enabled 
                ? item.getAction().isEnabled()
                : false);
          }
        }
      }

      // Need to take into account focus events because a mouse released event 
      // isn't dispatched when the component loses focus  
      public void focusGained(FocusEvent ev) {
        enableMenuItems(menuBar);
      }

      public void focusLost(FocusEvent ev) {
        enableMenuItems(menuBar);
      }
    };
    
    MouseAndFocusListener listener = new MouseAndFocusListener();
    view.addMouseListener(listener);
    view.addFocusListener(listener);
  }
  
  /**
   * Displays a content chooser open dialog to open a .sh3d file.
   */
  public String showOpenDialog() {
    return this.contentManager.showOpenDialog(this, 
        this.resource.getString("openHomeDialog.title"), 
        ContentManager.ContentType.SWEET_HOME_3D);
  }

  /**
   * Displays a content chooser open dialog to open a .sh3f file.
   */
  public String showImportFurnitureLibraryDialog() {
    return this.contentManager.showOpenDialog(this, 
        this.resource.getString("importFurnitureLibraryDialog.title"), 
        ContentManager.ContentType.FURNITURE_LIBRARY);
  }

  /**
   * Displays a dialog that lets user choose whether he wants to overwrite
   * an existing furniture library or not. 
   */
  public boolean confirmReplaceFurnitureLibrary(String furnitureLibraryName) {
    // Retrieve displayed text in buttons and message
    String messageFormat = this.resource.getString("confirmReplaceFurnitureLibrary.message");
    String message = String.format(messageFormat, new File(furnitureLibraryName).getName());
    String title = this.resource.getString("confirmReplaceFurnitureLibrary.title");
    String replace = this.resource.getString("confirmReplaceFurnitureLibrary.replace");
    String doNotReplace = this.resource.getString("confirmReplaceFurnitureLibrary.doNotReplace");
        
    return JOptionPane.showOptionDialog(this, 
        message, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
        null, new Object [] {replace, doNotReplace}, doNotReplace) == JOptionPane.OK_OPTION;
  }
  
  /**
   * Displays a content chooser save dialog to save a home in a .sh3d file.
   */
  public String showSaveDialog(String homeName) {
    return this.contentManager.showSaveDialog(this,
        this.resource.getString("saveHomeDialog.title"), 
        ContentManager.ContentType.SWEET_HOME_3D, homeName);
  }
  
  /**
   * Displays <code>message</code> in an error message box.
   */
  public void showError(String message) {
    String title = this.resource.getString("error.title");
    JOptionPane.showMessageDialog(this, message, title, 
        JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Displays <code>message</code> in a message box.
   */
  public void showMessage(String message) {
    String title = this.resource.getString("message.title");
    JOptionPane.showMessageDialog(this, message, title, 
        JOptionPane.INFORMATION_MESSAGE);
  }

  /**
   * Displays a dialog that lets user choose whether he wants to save
   * the current home or not.
   * @return {@link SaveAnswer#SAVE} if user chose to save home,
   * {@link SaveAnswer#DO_NOT_SAVE} if user don't want to save home,
   * or {@link SaveAnswer#CANCEL} if doesn't want to continue current operation.
   */
  public SaveAnswer confirmSave(String homeName) {
    // Retrieve displayed text in buttons and message
    String messageFormat = this.resource.getString("confirmSave.message");
    String message;
    if (homeName != null) {
      message = String.format(messageFormat, 
          "\"" + this.contentManager.getPresentationName(
              homeName, ContentManager.ContentType.SWEET_HOME_3D) + "\"");
    } else {
      message = String.format(messageFormat, "");
    }
    String title = this.resource.getString("confirmSave.title");
    String save = this.resource.getString("confirmSave.save");
    String doNotSave = this.resource.getString("confirmSave.doNotSave");
    String cancel = this.resource.getString("confirmSave.cancel");

    switch (JOptionPane.showOptionDialog(this, message, title, 
        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
        null, new Object [] {save, doNotSave, cancel}, save)) {
      // Convert showOptionDialog answer to SaveAnswer enum constants
      case JOptionPane.YES_OPTION:
        return SaveAnswer.SAVE;
      case JOptionPane.NO_OPTION:
        return SaveAnswer.DO_NOT_SAVE;
      default : return SaveAnswer.CANCEL;
    }
  }

  /**
   * Displays a dialog that let user choose whether he wants to save
   * a home that was created with a newer version of Sweet Home 3D.
   * @return <code>true</code> if user confirmed to save.
   */
  public boolean confirmSaveNewerHome(String homeName) {
    String message = String.format(this.resource.getString("confirmSaveNewerHome.message"), 
        this.contentManager.getPresentationName(
            homeName, ContentManager.ContentType.SWEET_HOME_3D));
    String title = this.resource.getString("confirmSaveNewerHome.title");
    String save = this.resource.getString("confirmSaveNewerHome.save");
    String doNotSave = this.resource.getString("confirmSaveNewerHome.doNotSave");
    
    return JOptionPane.showOptionDialog(this, message, title, 
        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
        null, new Object [] {save, doNotSave}, doNotSave) == JOptionPane.YES_OPTION;
  }
  
  /**
   * Displays a dialog that let user choose whether he wants to exit 
   * application or not.
   * @return <code>true</code> if user confirmed to exit.
   */
  public boolean confirmExit() {
    String message = this.resource.getString("confirmExit.message");
    String title = this.resource.getString("confirmExit.title");
    String quit = this.resource.getString("confirmExit.quit");
    String doNotQuit = this.resource.getString("confirmExit.doNotQuit");
    
    return JOptionPane.showOptionDialog(this, message, title, 
        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
        null, new Object [] {quit, doNotQuit}, doNotQuit) == JOptionPane.YES_OPTION;
  }
  
  /**
   * Displays an about dialog.
   */
  public void showAboutDialog() {
    String messageFormat = this.resource.getString("about.message");
    String aboutVersion = this.resource.getString("about.version");
    String message = String.format(messageFormat, aboutVersion, 
        System.getProperty("java.version"));
    // Use an uneditable editor pane to let user select text in dialog
    JEditorPane messagePane = new JEditorPane("text/html", message);
    messagePane.setOpaque(false);
    messagePane.setEditable(false);
    try { 
      // Lookup the javax.jnlp.BasicService object 
      final BasicService service = (BasicService)ServiceManager.lookup("javax.jnlp.BasicService");
      // If basic service supports  web browser
      if (service.isWebBrowserSupported()) {
        // Add a listener that displays hyperlinks content in browser
        messagePane.addHyperlinkListener(new HyperlinkListener() {
          public void hyperlinkUpdate(HyperlinkEvent ev) {
            if (ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
              service.showDocument(ev.getURL()); 
            }
          }
        });
      }
    } catch (UnavailableServiceException ex) {
      // Too bad : service is unavailable             
    } 
    
    String title = this.resource.getString("about.title");
    Icon   icon  = new ImageIcon(HomePane.class.getResource(
        this.resource.getString("about.icon")));
    JOptionPane.showMessageDialog(this, messagePane, title,  
        JOptionPane.INFORMATION_MESSAGE, icon);
  }

  /**
   * Shows a print dialog to print the home displayed by this pane.  
   * @return a print task to execute or <code>null</code> if the user canceled print.
   *    The <code>call<code> method of the returned task may throw a 
   *    {@link RecorderException RecorderException} exception if print failed 
   *    or an {@link InterruptedRecorderException InterruptedRecorderException}
   *    exception if it was interrupted.
   */
  public Callable<Void> showPrintDialog() {
    PageFormat pageFormat = PageSetupPanel.getPageFormat(this.home.getPrint());
    final PrinterJob printerJob = PrinterJob.getPrinterJob();
    printerJob.setPrintable(new HomePrintableComponent(this.home, this.controller, getFont()), pageFormat);
    if (printerJob.printDialog()) {
      return new Callable<Void>() {
          public Void call() throws RecorderException {
            try {
              printerJob.print();
              return null;
            } catch (InterruptedPrinterException ex) {
              throw new InterruptedRecorderException("Print interrupted");
            } catch (PrinterException ex) {
              throw new RecorderException("Couldn't print", ex);
            } 
          }
        };
    } else {
      return null;
    }
  }

  /**
   * Shows a content chooser save dialog to print a home in a PDF file.
   */
  public String showPrintToPDFDialog(String homeName) {
    return this.contentManager.showSaveDialog(this,
        this.resource.getString("printToPDFDialog.title"), 
        ContentManager.ContentType.PDF, homeName);
  }
  
  /**
   * Prints a home to a given PDF file. This method may be overridden
   * to write to another kind of output stream.
   */
  public void printToPDF(String pdfFile) throws RecorderException {
    OutputStream outputStream = null;
    try {
      outputStream = new FileOutputStream(pdfFile);
      new HomePDFPrinter(this.home, this.contentManager, this.controller, getFont())
          .write(outputStream);
    } catch (InterruptedIOException ex) {
      throw new InterruptedRecorderException("Print interrupted");
    } catch (IOException ex) {
      throw new RecorderException("Couldn't export to PDF", ex);
    } finally {
      try {
        if (outputStream != null) {
          outputStream.close();
        }
      } catch (IOException ex) {
        throw new RecorderException("Couldn't export to PDF", ex);
      }
    }
  }
  
  /**
   * Shows a content chooser save dialog to export a 3D home in a OBJ file.
   */
  public String showExportToOBJDialog(String homeName) {
    return this.contentManager.showSaveDialog(this,
        this.resource.getString("exportToOBJDialog.title"), 
        ContentManager.ContentType.OBJ, homeName);
  }
  
  /**
   * Displays a dialog that let user choose whether he wants to delete 
   * the selected furniture from catalog or not.
   * @return <code>true</code> if user confirmed to delete.
   */
  public boolean confirmDeleteCatalogSelection() {
    // Retrieve displayed text in buttons and message
    String message = this.resource.getString("confirmDeleteCatalogSelection.message");
    String title = this.resource.getString("confirmDeleteCatalogSelection.title");
    String delete = this.resource.getString("confirmDeleteCatalogSelection.delete");
    String cancel = this.resource.getString("confirmDeleteCatalogSelection.cancel");
    
    return JOptionPane.showOptionDialog(this, message, title, 
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
        null, new Object [] {delete, cancel}, cancel) == JOptionPane.OK_OPTION;
  }
  
  /**
   * Returns <code>true</code> if clipboard contains data that
   * components are able to handle.
   */
  public boolean isClipboardEmpty() {
    return !getToolkit().getSystemClipboard().
        isDataFlavorAvailable(HomeTransferableList.HOME_FLAVOR);
  }

  /**
   * Execute <code>runnable</code> asynchronously in the Event Dispatch Thread.  
   */
  public void invokeLater(Runnable runnable) {
    EventQueue.invokeLater(runnable);
  }

  /**
   * A Swing action adapter to a plug-in action.
   */
  private class ActionAdapter implements Action {
    private PluginAction               pluginAction;
    private SwingPropertyChangeSupport propertyChangeSupport;
    
    private ActionAdapter(PluginAction pluginAction) {
      this.pluginAction = pluginAction;
      this.propertyChangeSupport = new SwingPropertyChangeSupport(this);
      this.pluginAction.addPropertyChangeListener(new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            String propertyName = ev.getPropertyName();
            Object oldValue = ev.getOldValue();
            Object newValue = getValue(propertyName);
            if (PluginAction.Property.ENABLED.toString().equals(propertyName)) {
              propertyChangeSupport.firePropertyChange(
                  new PropertyChangeEvent(ev.getSource(), "enabled", oldValue, newValue));
            } else {
              // In case a property value changes, fire the new value decorated in subclasses
              // unless new value is null (most Swing listeners don't check new value is null !)
              if (newValue != null) {
                switch (PluginAction.Property.valueOf(propertyName)) {
                  case NAME:
                    propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(ev.getSource(), 
                        Action.NAME, oldValue, newValue));
                    break;
                  case SHORT_DESCRIPTION:
                    propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(ev.getSource(), 
                        Action.NAME, oldValue, newValue));
                    break;
                  case MNEMONIC:
                    propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(ev.getSource(), 
                        Action.MNEMONIC_KEY, 
                        oldValue != null 
                            ? new Integer((Character)oldValue) 
                            : null, newValue));
                    break;
                  case SMALL_ICON:
                    propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(ev.getSource(), 
                        Action.SMALL_ICON, 
                        oldValue != null 
                           ? IconManager.getInstance().getIcon((Content)oldValue, DEFAULT_SMALL_ICON_HEIGHT, HomePane.this) 
                           : null, newValue));
                    break;
                  default:
                    propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(ev.getSource(), 
                        propertyName, oldValue, newValue));
                    break;
                }
              }
            }
          }
        });
    }

    public void actionPerformed(ActionEvent ev) {
      this.pluginAction.execute();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
      this.propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
      this.propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public Object getValue(String key) {
      if (NAME.equals(key)) {
        return this.pluginAction.getPropertyValue(PluginAction.Property.NAME);
      } else if (SHORT_DESCRIPTION.equals(key)) {
        return this.pluginAction.getPropertyValue(PluginAction.Property.SHORT_DESCRIPTION);
      } else if (SMALL_ICON.equals(key)) {
        Content smallIcon = (Content)this.pluginAction.getPropertyValue(PluginAction.Property.SMALL_ICON);
        return smallIcon != null
            ? IconManager.getInstance().getIcon(smallIcon, DEFAULT_SMALL_ICON_HEIGHT, HomePane.this)
            : null;
      } else if (MNEMONIC_KEY.equals(key)) {
        Character mnemonic = (Character)this.pluginAction.getPropertyValue(PluginAction.Property.MNEMONIC);
        return mnemonic != null
            ? new Integer(mnemonic)
            : null;
      } else if (PluginAction.Property.IN_TOOL_BAR.toString().equals(key)) {
        return this.pluginAction.getPropertyValue(PluginAction.Property.IN_TOOL_BAR);
      } else if (PluginAction.Property.MENU.toString().equals(key)) {
        return this.pluginAction.getPropertyValue(PluginAction.Property.MENU);
      } else { 
        return null;
      }
    }

    public void putValue(String key, Object value) {
      if (NAME.equals(key)) {
        this.pluginAction.putPropertyValue(PluginAction.Property.NAME, value);
      } else if (SHORT_DESCRIPTION.equals(key)) {
        this.pluginAction.putPropertyValue(PluginAction.Property.SHORT_DESCRIPTION, value);
      } else if (SMALL_ICON.equals(key)) {
        // Ignore icon change
      } else if (MNEMONIC_KEY.equals(key)) {
        this.pluginAction.putPropertyValue(PluginAction.Property.MNEMONIC, 
            new Character((char)((Integer)value).intValue()));
      } else if (PluginAction.Property.IN_TOOL_BAR.toString().equals(key)) {
        this.pluginAction.putPropertyValue(PluginAction.Property.IN_TOOL_BAR, value);
      } else if (PluginAction.Property.MENU.toString().equals(key)) {
        this.pluginAction.putPropertyValue(PluginAction.Property.MENU, value);
      } 
    }

    public boolean isEnabled() {
      return this.pluginAction.isEnabled();
    }

    public void setEnabled(boolean enabled) {
      this.pluginAction.setEnabled(enabled);
    }
  }

  /**
   * A scroll pane that always displays scroll bar on Mac OS X.
   */
  private static class HomeScrollPane extends JScrollPane {
    public HomeScrollPane(JComponent view) {
      super(view);
      if (OperatingSystem.isMacOSX()) {
        setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_ALWAYS);
        setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
      }
    }
  }

  private static final Border UNFOCUSED_BORDER;
  private static final Border FOCUSED_BORDER;
  
  static {
    if (OperatingSystem.isMacOSXLeopardOrSuperior()) {
      UNFOCUSED_BORDER = BorderFactory.createCompoundBorder(
          BorderFactory.createEmptyBorder(2, 2, 2, 2),
          BorderFactory.createLineBorder(Color.LIGHT_GRAY));
      FOCUSED_BORDER = new AbstractBorder() {
          private Insets insets = new Insets(3, 3, 3, 3);
          
          public Insets getBorderInsets(Component c) {
            return this.insets;
          }
    
          public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Color previousColor = g.getColor();
            // Paint a gradient paint around component
            Rectangle rect = getInteriorRectangle(c, x, y, width, height);
            g.setColor(Color.GRAY);
            g.drawRect(rect.x - 1, rect.y - 1, rect.width + 1, rect.height + 1);
            Color focusColor = UIManager.getColor("Focus.color");
            int   transparency = 192;
            if (focusColor == null) {
              focusColor = UIManager.getColor("textHighlight");
              transparency = 128;
            }
            g.setColor(new Color(focusColor.getRed(), focusColor.getGreen(), focusColor.getBlue(), transparency));
            g.drawRect(rect.x - 1, rect.y - 1, rect.width + 1, rect.height + 1);
            g.drawRoundRect(rect.x - 3, rect.y - 3, rect.width + 5, rect.height + 5, 2, 2);
            g.setColor(focusColor);
            g.drawRoundRect(rect.x - 2, rect.y - 2, rect.width + 3, rect.height + 3, 1, 1);
            
            g.setColor(previousColor);
          }
        };
    } else {
      if (OperatingSystem.isMacOSX()) {
        UNFOCUSED_BORDER = BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(1, 1, 1, 1),
            BorderFactory.createLineBorder(Color.LIGHT_GRAY));
      } else {
        UNFOCUSED_BORDER = BorderFactory.createEmptyBorder(2, 2, 2, 2);
      }
      FOCUSED_BORDER = BorderFactory.createLineBorder(UIManager.getColor("textHighlight"), 2);
    }
  }

  /**
   * A focus listener that calls <code>focusChanged</code> in 
   * home controller.
   */
  private class FocusableViewListener implements FocusListener {
    private HomeController controller;
    private JComponent     feedbackComponent;
    private KeyListener    specialKeysListener = new KeyAdapter() {
        @Override
        public void keyTyped(KeyEvent ev) {
          // This listener manages accelerator keys that may require the use of shift key 
          // depending on keyboard layout (like + - or ?) 
          ActionMap actionMap = getActionMap();
          Action [] specialKeyActions = {actionMap.get(ActionType.ZOOM_IN), 
                                         actionMap.get(ActionType.ZOOM_OUT), 
                                         actionMap.get(ActionType.HELP)};
          int modifiersMask = KeyEvent.ALT_MASK | KeyEvent.CTRL_MASK | KeyEvent.META_MASK;
          for (Action specialKeyAction : specialKeyActions) {
            KeyStroke actionKeyStroke = (KeyStroke)specialKeyAction.getValue(Action.ACCELERATOR_KEY);
            if (ev.getKeyChar() == actionKeyStroke.getKeyChar()
                && (ev.getModifiers() & modifiersMask) == (actionKeyStroke.getModifiers() & modifiersMask)
                && specialKeyAction.isEnabled()) {
              specialKeyAction.actionPerformed(new ActionEvent(HomePane.this, 
                  ActionEvent.ACTION_PERFORMED, (String)specialKeyAction.getValue(Action.ACTION_COMMAND_KEY)));
              ev.consume();
            }
          }
        }
      };
  
    public FocusableViewListener(HomeController controller, 
                                 JComponent     feedbackComponent) {
      this.controller = controller;
      this.feedbackComponent = feedbackComponent;
      feedbackComponent.setBorder(UNFOCUSED_BORDER);
    }
        
    public void focusGained(FocusEvent ev) {
      // Display a colored border
      this.feedbackComponent.setBorder(FOCUSED_BORDER);
      // Update the component used by clipboard actions
      focusedComponent = (JComponent)ev.getComponent();
      // Notify controller that active view changed
      this.controller.focusedViewChanged(focusedComponent);
      focusedComponent.addKeyListener(specialKeysListener);
    }
    
    public void focusLost(FocusEvent ev) {
      this.feedbackComponent.setBorder(UNFOCUSED_BORDER);
      focusedComponent.removeKeyListener(specialKeysListener);
    }
  }
}
