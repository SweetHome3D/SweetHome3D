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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

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

import com.eteks.sweethome3d.model.ContentManager;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;

/**
 * The MVC view that edits a home. 
 * @author Emmanuel Puybaret
 */
public class HomePane extends JRootPane {
  public enum ActionType {
      NEW_HOME, CLOSE, OPEN, DELETE_RECENT_HOMES, SAVE, SAVE_AS, PAGE_SETUP, PRINT_PREVIEW, PRINT, PRINT_TO_PDF, PREFERENCES, EXIT, 
      UNDO, REDO, CUT, COPY, PASTE, DELETE, SELECT_ALL,
      ADD_HOME_FURNITURE, DELETE_HOME_FURNITURE, MODIFY_FURNITURE, IMPORT_FURNITURE, 
      SORT_HOME_FURNITURE_BY_NAME, SORT_HOME_FURNITURE_BY_WIDTH, SORT_HOME_FURNITURE_BY_DEPTH, SORT_HOME_FURNITURE_BY_HEIGHT, 
      SORT_HOME_FURNITURE_BY_X, SORT_HOME_FURNITURE_BY_Y, SORT_HOME_FURNITURE_BY_ELEVATION, 
      SORT_HOME_FURNITURE_BY_ANGLE, SORT_HOME_FURNITURE_BY_COLOR, 
      SORT_HOME_FURNITURE_BY_MOVABILITY, SORT_HOME_FURNITURE_BY_TYPE, SORT_HOME_FURNITURE_BY_VISIBILITY, 
      SORT_HOME_FURNITURE_BY_DESCENDING_ORDER,
      DISPLAY_HOME_FURNITURE_NAME, DISPLAY_HOME_FURNITURE_WIDTH, DISPLAY_HOME_FURNITURE_DEPTH, DISPLAY_HOME_FURNITURE_HEIGHT, 
      DISPLAY_HOME_FURNITURE_X, DISPLAY_HOME_FURNITURE_Y, DISPLAY_HOME_FURNITURE_ELEVATION, 
      DISPLAY_HOME_FURNITURE_ANGLE, DISPLAY_HOME_FURNITURE_COLOR, 
      DISPLAY_HOME_FURNITURE_MOVABLE, DISPLAY_HOME_FURNITURE_DOOR_OR_WINDOW, DISPLAY_HOME_FURNITURE_VISIBLE,
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
  
  /**
   * Creates this view associated with its controller.
   */
  public HomePane(Home home, UserPreferences preferences, 
                  ContentManager contentManager, HomeController controller) {
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
    createTransferHandlers(home, preferences, contentManager, controller);
    addHomeListener(home);
    addLanguageListener(preferences);
    addPlanControllerListener(controller.getPlanController());
    JMenuBar homeMenuBar = getHomeMenuBar(home, controller, contentManager);
    setJMenuBar(homeMenuBar);
    Container contentPane = getContentPane();
    contentPane.add(getToolBar(), BorderLayout.NORTH);
    contentPane.add(getMainPane(home, preferences, controller));
    if (OperatingSystem.isMacOSXLeopardOrSuperior()) {
      // Under Mac OS X 10.5, add some dummy labels at left and right borders
      // to avoid the tool bar to be attached on these borders
      // (segmented buttons created on this system aren't properly rendered
      // when they are aligned vertically)
      contentPane.add(new JLabel(), BorderLayout.WEST);
      contentPane.add(new JLabel(), BorderLayout.EAST);
    }
    
    disableMenuItemsDuringDragAndDrop(controller.getPlanController().getView(), homeMenuBar);
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
    createAction(ActionType.ALIGN_FURNITURE_ON_TOP, 
        furnitureController, "alignSelectedFurnitureOnTop");
    createAction(ActionType.ALIGN_FURNITURE_ON_BOTTOM, 
        furnitureController, "alignSelectedFurnitureOnBottom");
    createAction(ActionType.ALIGN_FURNITURE_ON_LEFT, 
        furnitureController, "alignSelectedFurnitureOnLeft");
    createAction(ActionType.ALIGN_FURNITURE_ON_RIGHT, 
        furnitureController, "alignSelectedFurnitureOnRight");
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
    createAction(ActionType.SORT_HOME_FURNITURE_BY_DESCENDING_ORDER, furnitureController, "toggleFurnitureSortOrder");
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
    
    // Read Swing localized properties because Swing doesn't update its internal strings automatically
    // when default Locale is updated (see bug http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4884480)
    String [] swingResources = {"com.sun.swing.internal.plaf.basic.resources.basic",
                                "com.sun.swing.internal.plaf.metal.resources.metal"};
    for (String swingResource : swingResources) {
      ResourceBundle resource;
      try {
        resource = ResourceBundle.getBundle(swingResource);
      } catch (MissingResourceException ex) {
        resource = ResourceBundle.getBundle(swingResource, Locale.ENGLISH);
      }
      // Update UIManager properties
      for (Enumeration iter = resource.getKeys(); iter.hasMoreElements(); ) {
        String property = (String)iter.nextElement();
        UIManager.put(property, resource.getString(property));
      }      
    };
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
  private JMenuBar getHomeMenuBar(final Home home, 
                                  final HomeController controller,
                                  final ContentManager contentManager) {
    // Create File menu
    JMenu fileMenu = new JMenu(this.menuActionMap.get(MenuActionType.FILE_MENU));
    fileMenu.add(getMenuAction(ActionType.NEW_HOME));
    fileMenu.add(getMenuAction(ActionType.OPEN));
    
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
    fileMenu.add(getMenuAction(ActionType.CLOSE));
    fileMenu.add(getMenuAction(ActionType.SAVE));
    fileMenu.add(getMenuAction(ActionType.SAVE_AS));
    fileMenu.addSeparator();
    fileMenu.add(getMenuAction(ActionType.PAGE_SETUP));
    fileMenu.add(getMenuAction(ActionType.PRINT_PREVIEW));
    fileMenu.add(getMenuAction(ActionType.PRINT));
    // Don't add PRINT_TO_PDF, PREFERENCES and EXIT menu items under Mac OS X, 
    // because PREFERENCES and EXIT items are displayed in application menu
    // and PRINT_TO_PDF is available in standard Mac OS X Print dialog
    if (!OperatingSystem.isMacOSX()) {
      fileMenu.add(getMenuAction(ActionType.PRINT_TO_PDF));
      fileMenu.addSeparator();
      fileMenu.add(getMenuAction(ActionType.PREFERENCES));
      fileMenu.addSeparator();
      fileMenu.add(getMenuAction(ActionType.EXIT));
    }

    // Create Edit menu
    JMenu editMenu = new JMenu(this.menuActionMap.get(MenuActionType.EDIT_MENU));
    editMenu.add(getMenuAction(ActionType.UNDO));
    editMenu.add(getMenuAction(ActionType.REDO));
    editMenu.addSeparator();
    editMenu.add(getMenuAction(ActionType.CUT));
    editMenu.add(getMenuAction(ActionType.COPY));
    editMenu.add(getMenuAction(ActionType.PASTE));
    editMenu.addSeparator();
    editMenu.add(getMenuAction(ActionType.DELETE));
    editMenu.add(getMenuAction(ActionType.SELECT_ALL));

    // Create Furniture menu
    JMenu furnitureMenu = new JMenu(this.menuActionMap.get(MenuActionType.FURNITURE_MENU));
    furnitureMenu.add(getMenuAction(ActionType.ADD_HOME_FURNITURE));
    furnitureMenu.add(getMenuAction(ActionType.MODIFY_FURNITURE));
    furnitureMenu.addSeparator();
    furnitureMenu.add(getMenuAction(ActionType.IMPORT_FURNITURE));
    furnitureMenu.addSeparator();
    furnitureMenu.add(getMenuAction(ActionType.ALIGN_FURNITURE_ON_TOP));
    furnitureMenu.add(getMenuAction(ActionType.ALIGN_FURNITURE_ON_BOTTOM));
    furnitureMenu.add(getMenuAction(ActionType.ALIGN_FURNITURE_ON_LEFT));
    furnitureMenu.add(getMenuAction(ActionType.ALIGN_FURNITURE_ON_RIGHT));
    furnitureMenu.addSeparator();
    furnitureMenu.add(getFurnitureSortMenu(home));
    furnitureMenu.add(getFurnitureDisplayPropertyMenu(home));
    
    // Create Plan menu
    JMenu planMenu = new JMenu(this.menuActionMap.get(MenuActionType.PLAN_MENU));
    JRadioButtonMenuItem selectRadioButtonMenuItem = getSelectRadioButtonMenuItem(false);
    planMenu.add(selectRadioButtonMenuItem);
    JRadioButtonMenuItem createWallsRadioButtonMenuItem = getCreateWallsRadioButtonMenuItem(false);
    planMenu.add(createWallsRadioButtonMenuItem);
    JRadioButtonMenuItem createDimensionLinesRadioButtonMenuItem = getCreateDimensionLinesRadioButtonMenuItem(false);
    planMenu.add(createDimensionLinesRadioButtonMenuItem);
    // Add Select, Create Walls and Create dimensions menu items to radio group 
    ButtonGroup group = new ButtonGroup();
    group.add(selectRadioButtonMenuItem);
    group.add(createWallsRadioButtonMenuItem);  
    group.add(createDimensionLinesRadioButtonMenuItem);  
    planMenu.addSeparator();
    planMenu.add(getMenuAction(ActionType.MODIFY_WALL));
    planMenu.add(getMenuAction(ActionType.REVERSE_WALL_DIRECTION));
    planMenu.add(getMenuAction(ActionType.SPLIT_WALL));
    planMenu.addSeparator();
    final JMenuItem importModifyBackgroundImageMenuItem = new JMenuItem( 
        getMenuAction(home.getBackgroundImage() == null 
            ? ActionType.IMPORT_BACKGROUND_IMAGE
            : ActionType.MODIFY_BACKGROUND_IMAGE));
    // Add a listener to home on backgroundImage property change to 
    // switch action according to backgroundImage change
    home.addPropertyChangeListener(Home.Property.BACKGROUND_IMAGE, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            importModifyBackgroundImageMenuItem.setAction(
                getMenuAction(home.getBackgroundImage() == null 
                    ? ActionType.IMPORT_BACKGROUND_IMAGE
                    : ActionType.MODIFY_BACKGROUND_IMAGE));
          }
        });    
    planMenu.add(importModifyBackgroundImageMenuItem);
    planMenu.add(getMenuAction(ActionType.DELETE_BACKGROUND_IMAGE));
    planMenu.addSeparator();
    planMenu.add(getMenuAction(ActionType.ZOOM_OUT));
    planMenu.add(getMenuAction(ActionType.ZOOM_IN));

    // Create 3D Preview menu
    JMenu preview3DMenu = new JMenu(this.menuActionMap.get(MenuActionType.VIEW_3D_MENU));
    JRadioButtonMenuItem viewFromTopRadioButtonMenuItem = getViewFromTopRadioButtonMenuItem(false);
    preview3DMenu.add(viewFromTopRadioButtonMenuItem);
    JRadioButtonMenuItem viewFromObserverRadioButtonMenuItem = getViewFromObserverRadioButtonMenuItem(false);
    preview3DMenu.add(viewFromObserverRadioButtonMenuItem);
    // Add View from top and View from observer menu items to radio group 
    group = new ButtonGroup();
    group.add(viewFromTopRadioButtonMenuItem);
    group.add(viewFromObserverRadioButtonMenuItem);
    preview3DMenu.addSeparator();
    preview3DMenu.add(getMenuAction(ActionType.MODIFY_3D_ATTRIBUTES));
    preview3DMenu.addSeparator();
    preview3DMenu.add(getMenuAction(ActionType.EXPORT_TO_OBJ));
    
    // Create Help menu
    JMenu helpMenu = new JMenu(this.menuActionMap.get(MenuActionType.HELP_MENU));
    helpMenu.add(getMenuAction(ActionType.HELP));      
    if (!OperatingSystem.isMacOSX()) {
      helpMenu.add(getMenuAction(ActionType.ABOUT));      
    }

    // Add menus to menu bar
    JMenuBar menuBar = new JMenuBar();
    menuBar.add(fileMenu);
    menuBar.add(editMenu);
    menuBar.add(furnitureMenu);
    menuBar.add(planMenu);
    menuBar.add(preview3DMenu);
    menuBar.add(helpMenu);

    return menuBar;
  }

  /**
   * Returns furniture sort menu.
   */
  private JMenu getFurnitureSortMenu(final Home home) {
    // Create Furniture Sort submenu
    JMenu sortMenu = new JMenu(this.menuActionMap.get(MenuActionType.SORT_HOME_FURNITURE_MENU));
    // Map sort furniture properties to sort actions
    Map<HomePieceOfFurniture.SortableProperty, Action> sortActions = 
        new LinkedHashMap<HomePieceOfFurniture.SortableProperty, Action>(); 
    sortActions.put(HomePieceOfFurniture.SortableProperty.NAME, 
        getMenuAction(ActionType.SORT_HOME_FURNITURE_BY_NAME)); 
    sortActions.put(HomePieceOfFurniture.SortableProperty.WIDTH, 
        getMenuAction(ActionType.SORT_HOME_FURNITURE_BY_WIDTH));
    sortActions.put(HomePieceOfFurniture.SortableProperty.DEPTH, 
        getMenuAction(ActionType.SORT_HOME_FURNITURE_BY_DEPTH));
    sortActions.put(HomePieceOfFurniture.SortableProperty.HEIGHT, 
        getMenuAction(ActionType.SORT_HOME_FURNITURE_BY_HEIGHT));
    sortActions.put(HomePieceOfFurniture.SortableProperty.X, 
        getMenuAction(ActionType.SORT_HOME_FURNITURE_BY_X));
    sortActions.put(HomePieceOfFurniture.SortableProperty.Y, 
        getMenuAction(ActionType.SORT_HOME_FURNITURE_BY_Y));
    sortActions.put(HomePieceOfFurniture.SortableProperty.ELEVATION, 
        getMenuAction(ActionType.SORT_HOME_FURNITURE_BY_ELEVATION));
    sortActions.put(HomePieceOfFurniture.SortableProperty.ANGLE, 
        getMenuAction(ActionType.SORT_HOME_FURNITURE_BY_ANGLE));
    sortActions.put(HomePieceOfFurniture.SortableProperty.COLOR, 
        getMenuAction(ActionType.SORT_HOME_FURNITURE_BY_COLOR));
    sortActions.put(HomePieceOfFurniture.SortableProperty.MOVABLE, 
        getMenuAction(ActionType.SORT_HOME_FURNITURE_BY_MOVABILITY));
    sortActions.put(HomePieceOfFurniture.SortableProperty.DOOR_OR_WINDOW, 
        getMenuAction(ActionType.SORT_HOME_FURNITURE_BY_TYPE));
    sortActions.put(HomePieceOfFurniture.SortableProperty.VISIBLE, 
        getMenuAction(ActionType.SORT_HOME_FURNITURE_BY_VISIBILITY));
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
        getMenuAction(ActionType.SORT_HOME_FURNITURE_BY_DESCENDING_ORDER));
    sortMenu.add(sortOrderCheckBoxMenuItem);
    return sortMenu;
  }
  
  /**
   * Returns furniture display property menu.
   */
  private JMenu getFurnitureDisplayPropertyMenu(final Home home) {
    // Create Furniture Display property submenu
    JMenu displayPropertyMenu = new JMenu(
        this.menuActionMap.get(MenuActionType.DISPLAY_HOME_FURNITURE_PROPERTY_MENU));
    // Map displayProperty furniture properties to displayProperty actions
    Map<HomePieceOfFurniture.SortableProperty, Action> displayPropertyActions = 
        new LinkedHashMap<HomePieceOfFurniture.SortableProperty, Action>(); 
    displayPropertyActions.put(HomePieceOfFurniture.SortableProperty.NAME, 
        getMenuAction(ActionType.DISPLAY_HOME_FURNITURE_NAME)); 
    displayPropertyActions.put(HomePieceOfFurniture.SortableProperty.WIDTH, 
        getMenuAction(ActionType.DISPLAY_HOME_FURNITURE_WIDTH));
    displayPropertyActions.put(HomePieceOfFurniture.SortableProperty.DEPTH, 
        getMenuAction(ActionType.DISPLAY_HOME_FURNITURE_DEPTH));
    displayPropertyActions.put(HomePieceOfFurniture.SortableProperty.HEIGHT, 
        getMenuAction(ActionType.DISPLAY_HOME_FURNITURE_HEIGHT));
    displayPropertyActions.put(HomePieceOfFurniture.SortableProperty.X, 
        getMenuAction(ActionType.DISPLAY_HOME_FURNITURE_X));
    displayPropertyActions.put(HomePieceOfFurniture.SortableProperty.Y, 
        getMenuAction(ActionType.DISPLAY_HOME_FURNITURE_Y));
    displayPropertyActions.put(HomePieceOfFurniture.SortableProperty.ELEVATION, 
        getMenuAction(ActionType.DISPLAY_HOME_FURNITURE_ELEVATION));
    displayPropertyActions.put(HomePieceOfFurniture.SortableProperty.ANGLE, 
        getMenuAction(ActionType.DISPLAY_HOME_FURNITURE_ANGLE));
    displayPropertyActions.put(HomePieceOfFurniture.SortableProperty.COLOR, 
        getMenuAction(ActionType.DISPLAY_HOME_FURNITURE_COLOR));
    displayPropertyActions.put(HomePieceOfFurniture.SortableProperty.MOVABLE, 
        getMenuAction(ActionType.DISPLAY_HOME_FURNITURE_MOVABLE));
    displayPropertyActions.put(HomePieceOfFurniture.SortableProperty.DOOR_OR_WINDOW, 
        getMenuAction(ActionType.DISPLAY_HOME_FURNITURE_DOOR_OR_WINDOW));
    displayPropertyActions.put(HomePieceOfFurniture.SortableProperty.VISIBLE, 
        getMenuAction(ActionType.DISPLAY_HOME_FURNITURE_VISIBLE));
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
    openRecentHomeMenu.add(getMenuAction(ActionType.DELETE_RECENT_HOMES));
  }

  /**
   * Returns an action decorated for menu items.
   */
  private Action getMenuAction(ActionType actionType) {
    return new ResourceAction.MenuAction(getActionMap().get(actionType));
  }

  /**
   * Returns an action decorated for popup menu items.
   */
  private Action getPopupAction(ActionType actionType) {
    return new ResourceAction.PopupAction(getActionMap().get(actionType));
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
    return getRadioButtonMenuItemFromModel(this.selectToggleModel, 
        ActionType.SELECT, popup);
  }
  
  /**
   * Returns a radio button menu item for Create walls action. 
   */
  private JRadioButtonMenuItem getCreateWallsRadioButtonMenuItem(boolean popup) {
    return getRadioButtonMenuItemFromModel(this.createWallsToggleModel, 
        ActionType.CREATE_WALLS, popup);
  }
  
  /**
   * Returns a radio button menu item for Create dimensions action. 
   */
  private JRadioButtonMenuItem getCreateDimensionLinesRadioButtonMenuItem(boolean popup) {
    return getRadioButtonMenuItemFromModel(this.createDimensionLinesToggleModel, 
        ActionType.CREATE_DIMENSION_LINES, popup);
  }
  
  /**
   * Returns a radio button menu item for View from top action. 
   */
  private JRadioButtonMenuItem getViewFromTopRadioButtonMenuItem(boolean popup) {
    return getRadioButtonMenuItemFromModel(this.viewFromTopToggleModel, 
        ActionType.VIEW_FROM_TOP, popup);
  }
  
  /**
   * Returns a radio button menu item for View from observer action. 
   */
  private JRadioButtonMenuItem getViewFromObserverRadioButtonMenuItem(boolean popup) {
    return getRadioButtonMenuItemFromModel(this.viewFromObserverToggleModel, 
        ActionType.VIEW_FROM_OBSERVER, popup);
  }
  
  private JRadioButtonMenuItem getRadioButtonMenuItemFromModel(
                                   JToggleButton.ToggleButtonModel model,
                                   ActionType action,
                                   boolean popup) {
    JRadioButtonMenuItem radioButtonMenuItem = new JRadioButtonMenuItem();
    // Configure shared model
    radioButtonMenuItem.setModel(model);
    // Configure check box menu item action after setting its model to avoid losing its mnemonic
    radioButtonMenuItem.setAction(
        popup ? getPopupAction(action)
              : getMenuAction(action));
    return radioButtonMenuItem;
  }
  
  /**
   * Returns the tool bar displayed in this pane.
   */
  private JToolBar getToolBar() {
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
    
    toolBar.add(getToolBarAction(ActionType.HELP));
    
    // Remove focusable property on buttons
    for (int i = 0, n = toolBar.getComponentCount(); i < n; i++) {
      toolBar.getComponentAtIndex(i).setFocusable(false);      
    }
    
    updateToolBarButtonsStyle(toolBar);
    toolBar.addPropertyChangeListener("componentOrientation", 
        new PropertyChangeListener () {
          public void propertyChange(PropertyChangeEvent evt) {
            updateToolBarButtonsStyle(toolBar);
          }
        });
    
    return toolBar;
  }

  /**
   * Under Mac OS X 10.5 use segmented buttons and group them depending
   * on toolbar orientation and whether a button is after or before a separator.
   */
  private void updateToolBarButtonsStyle(final JToolBar toolBar) {
    if (OperatingSystem.isMacOSXLeopardOrSuperior()) {
      // Retrieve component orientation because Mac OS X 10.5 miserably doesn't it take into account 
      ComponentOrientation orientation = toolBar.getComponentOrientation();
      Component previousComponent = null;
      for (int i = 0, n = toolBar.getComponentCount(); i < n; i++) {        
        JComponent component = (JComponent)toolBar.getComponentAtIndex(i); 
        if (!(component instanceof AbstractButton)) {
          previousComponent = null;
          continue;
        }          
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
  private JComponent getMainPane(Home home, UserPreferences preferences, 
                                 HomeController controller) {
    JSplitPane mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
        getCatalogFurniturePane(home, controller), 
        getPlanView3DPane(home, preferences, controller));
    configureSplitPane(mainPane, home, MAIN_PANE_DIVIDER_LOCATION_VISUAL_PROPERTY, 0.3, controller);
    return mainPane;
  }

  /**
   * Configures <code>splitPane</code> divider location. 
   * If <code>dividerLocationProperty</code> visual property exists in <code>home</code>,
   * its value will be used, otherwise the given resize weight will be used.
   */
  private void configureSplitPane(JSplitPane splitPane,
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
            controller.setVisualProperty(dividerLocationProperty, ev.getNewValue());
          }
        });
  }

  /**
   * Returns the catalog tree and furniture table pane. 
   */
  private JComponent getCatalogFurniturePane(Home home, final HomeController controller) {
    JComponent catalogView = controller.getCatalogController().getView();
    JScrollPane catalogScrollPane = new HomeScrollPane(catalogView);
    // Add focus listener to catalog tree
    catalogView.addFocusListener(new FocusableViewListener(
        controller, catalogScrollPane));
    
    // Create catalog view popup menu
    JPopupMenu catalogViewPopup = new JPopupMenu();
    catalogViewPopup.add(getPopupAction(ActionType.COPY));
    catalogViewPopup.addSeparator();
    catalogViewPopup.add(getPopupAction(ActionType.DELETE));
    catalogViewPopup.addSeparator();
    catalogViewPopup.add(getPopupAction(ActionType.ADD_HOME_FURNITURE));
    catalogViewPopup.add(getPopupAction(ActionType.MODIFY_FURNITURE));
    catalogViewPopup.addSeparator();
    catalogViewPopup.add(getPopupAction(ActionType.IMPORT_FURNITURE));
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
    furnitureViewPopup.add(getPopupAction(ActionType.UNDO));
    furnitureViewPopup.add(getPopupAction(ActionType.REDO));
    furnitureViewPopup.addSeparator();
    furnitureViewPopup.add(getPopupAction(ActionType.CUT));
    furnitureViewPopup.add(getPopupAction(ActionType.COPY));
    furnitureViewPopup.add(getPopupAction(ActionType.PASTE));
    furnitureViewPopup.addSeparator();
    furnitureViewPopup.add(getPopupAction(ActionType.DELETE));
    furnitureViewPopup.add(getPopupAction(ActionType.SELECT_ALL));
    furnitureViewPopup.addSeparator();
    furnitureViewPopup.add(getPopupAction(ActionType.MODIFY_FURNITURE));
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
  private JComponent getPlanView3DPane(Home home, UserPreferences preferences, 
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
    planViewPopup.add(getPopupAction(ActionType.UNDO));
    planViewPopup.add(getPopupAction(ActionType.REDO));
    planViewPopup.addSeparator();
    planViewPopup.add(getPopupAction(ActionType.CUT));
    planViewPopup.add(getPopupAction(ActionType.COPY));
    planViewPopup.add(getPopupAction(ActionType.PASTE));
    planViewPopup.addSeparator();
    planViewPopup.add(getPopupAction(ActionType.DELETE));
    planViewPopup.add(getPopupAction(ActionType.SELECT_ALL));
    planViewPopup.addSeparator();
    JRadioButtonMenuItem selectRadioButtonMenuItem = getSelectRadioButtonMenuItem(true);
    planViewPopup.add(selectRadioButtonMenuItem);
    JRadioButtonMenuItem createWallsRadioButtonMenuItem = getCreateWallsRadioButtonMenuItem(true);
    planViewPopup.add(createWallsRadioButtonMenuItem);
    JRadioButtonMenuItem createDimensionLinesRadioButtonMenuItem = getCreateDimensionLinesRadioButtonMenuItem(true);
    planViewPopup.add(createDimensionLinesRadioButtonMenuItem);
    // Add Select and Create Walls menu items to radio group 
    ButtonGroup group = new ButtonGroup();
    group.add(selectRadioButtonMenuItem);
    group.add(createWallsRadioButtonMenuItem);
    group.add(createDimensionLinesRadioButtonMenuItem);
    planViewPopup.addSeparator();
    planViewPopup.add(getPopupAction(ActionType.MODIFY_FURNITURE));
    planViewPopup.add(getPopupAction(ActionType.MODIFY_WALL));
    planViewPopup.add(getPopupAction(ActionType.REVERSE_WALL_DIRECTION));
    planViewPopup.add(getPopupAction(ActionType.SPLIT_WALL));
    planViewPopup.addSeparator();
    planViewPopup.add(getPopupAction(ActionType.ZOOM_OUT));
    planViewPopup.add(getPopupAction(ActionType.ZOOM_IN));
    planView.setComponentPopupMenu(planViewPopup);
    
    // Configure 3D view
    JComponent view3D = controller.getHomeController3D().getView();
    view3D.setPreferredSize(planView.getPreferredSize());
    view3D.setMinimumSize(new Dimension(0, 0));
    view3D.addFocusListener(new FocusableViewListener(controller, view3D));
    // Create 3D view popup menu
    JPopupMenu view3DPopup = new JPopupMenu();
    JRadioButtonMenuItem viewFromTopRadioButtonMenuItem = getViewFromTopRadioButtonMenuItem(true);
    view3DPopup.add(viewFromTopRadioButtonMenuItem);
    JRadioButtonMenuItem viewFromObserverRadioButtonMenuItem = getViewFromObserverRadioButtonMenuItem(true);
    view3DPopup.add(viewFromObserverRadioButtonMenuItem);
    // Add View from top and View from observer menu items to radio group 
    group = new ButtonGroup();
    group.add(viewFromTopRadioButtonMenuItem);
    group.add(viewFromObserverRadioButtonMenuItem);
    view3D.setComponentPopupMenu(view3DPopup);
    view3DPopup.addSeparator();
    view3DPopup.add(getMenuAction(ActionType.MODIFY_3D_ATTRIBUTES));
    view3DPopup.addSeparator();
    view3DPopup.add(getMenuAction(ActionType.EXPORT_TO_OBJ));
    
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
    return this.contentManager.showOpenDialog( 
        this.resource.getString("openHomeDialog.title"), 
        ContentManager.ContentType.SWEET_HOME_3D);
  }

  /**
   * Displays a content chooser save dialog to save a home in a .sh3d file.
   */
  public String showSaveDialog(String homeName) {
    return this.contentManager.showSaveDialog(
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
   * Displays a dialog that let user choose whether he wants to save
   * the current home or not.
   * @return {@link SaveAnswer#SAVE} if user choosed to save home,
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
   * Prints the home displayed by this pane and returns <code>true</code> if print was successful.
   */
  public boolean print() {
    PageFormat pageFormat = PageSetupPanel.getPageFormat(this.home.getPrint());
    PrinterJob printerJob = PrinterJob.getPrinterJob();
    printerJob.setPrintable(new HomePrintableComponent(this.home, this.controller, getFont()), pageFormat);
    if (printerJob.printDialog()) {
      Component previousGlassPane = getWaitGlassPane();
      try {
        printerJob.print();
      } catch (PrinterException ex) {
        return false;
      } finally {
        setGlassPane(previousGlassPane);
      }
    }
    return true;
  }

  /**
   * Returns a waiting glass pane for lengthy operations.
   */
  private Component getWaitGlassPane() {
    Component previousGlassPane = getGlassPane(); 
    JLabel waitGlassPane = new JLabel();
    waitGlassPane.setOpaque(true);
    waitGlassPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    setGlassPane(waitGlassPane);
    waitGlassPane.setVisible(true);
    return previousGlassPane;
  }
  
  /**
   * Shows a content chooser save dialog to print a home in a PDF file.
   */
  public String showPrintToPDFDialog(String homeName) {
    return this.contentManager.showSaveDialog(
        this.resource.getString("printToPDFDialog.title"), 
        ContentManager.ContentType.PDF, null);
  }
  
  /**
   * Prints a home to a given PDF file. This method may be overriden
   * to write to another kind of output stream.
   */
  public boolean printToPDF(String pdfFile) {
    Component previousGlassPane = getWaitGlassPane();
    OutputStream outputStream = null;
    try {
      outputStream = new FileOutputStream(pdfFile);
      new HomePDFPrinter(this.home, this.contentManager, this.controller, getFont())
          .write(outputStream);
      return true;
    } catch (IOException ex) {
      return false;
    } finally {
      setGlassPane(previousGlassPane);
      try {
        if (outputStream != null) {
          outputStream.close();
        }
      } catch (IOException ex) {
        return false;
      }
    }
  }
  
  /**
   * Shows a content chooser save dialog to export a 3D home in a OBJ file.
   */
  public String showExportToOBJDialog(String homeName) {
    return this.contentManager.showSaveDialog(
        this.resource.getString("exportToOBJDialog.title"), 
        ContentManager.ContentType.OBJ, null);
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
            if (focusColor == null) {
              focusColor = UIManager.getColor("textHighlight");
            }
            g.setColor(new Color(focusColor.getRed(), focusColor.getGreen(), focusColor.getBlue(), 160));
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
