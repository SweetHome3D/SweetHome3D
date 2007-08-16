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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
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
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import com.eteks.sweethome3d.model.ContentManager;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.HomePrint;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * The MVC view that edits a home. 
 * @author Emmanuel Puybaret
 */
public class HomePane extends JRootPane {
  public enum ActionType {
    NEW_HOME, CLOSE, OPEN, DELETE_RECENT_HOMES, SAVE, SAVE_AS, PAGE_SETUP, PRINT_PREVIEW, PRINT, PREFERENCES, EXIT, 
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
    SELECT, CREATE_WALLS, DELETE_SELECTION, MODIFY_WALL, 
    IMPORT_BACKGROUND_IMAGE, MODIFY_BACKGROUND_IMAGE, DELETE_BACKGROUND_IMAGE, ZOOM_OUT, ZOOM_IN,  
    VIEW_FROM_TOP, VIEW_FROM_OBSERVER, MODIFY_3D_ATTRIBUTES,
    HELP, ABOUT}
  public enum SaveAnswer {SAVE, CANCEL, DO_NOT_SAVE}
  
  private ContentManager                  contentManager;
  private ResourceBundle                  resource;
  // Button models shared by Select and Create wall menu items and the matching tool bar buttons
  private JToggleButton.ToggleButtonModel selectToggleModel;
  private JToggleButton.ToggleButtonModel createWallsToggleModel;
  // Button models shared by View from top and View from observer menu items and the matching tool bar buttons
  private JToggleButton.ToggleButtonModel viewFromTopToggleModel;
  private JToggleButton.ToggleButtonModel viewFromObserverToggleModel;
  private JComponent                      focusedComponent;
  private JComponent                      catalogView;
  private JComponent                      furnitureView;
  private JComponent                      planView;
  private TransferHandler                 catalogTransferHandler;
  private TransferHandler                 furnitureTransferHandler;
  private TransferHandler                 planTransferHandler;
  
  /**
   * Creates this view associated with its controller.
   */
  public HomePane(Home home, UserPreferences preferences, 
                  ContentManager contentManager, HomeController controller) {
    this.contentManager = contentManager;
    this.resource = ResourceBundle.getBundle(HomePane.class.getName());
    // Create unique toggle button models for Selection / Wall creation states
    // so Select and Create walls creation menu items and tool bar buttons 
    // always reflect the same toggle state at screen
    this.selectToggleModel = new JToggleButton.ToggleButtonModel();
    this.selectToggleModel.setSelected(controller.getPlanController().getMode() 
        == PlanController.Mode.SELECTION);
    this.createWallsToggleModel = new JToggleButton.ToggleButtonModel();
    this.createWallsToggleModel.setSelected(controller.getPlanController().getMode() 
        == PlanController.Mode.WALL_CREATION);
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
    createTransferHandlers(home, preferences, contentManager, controller);
    addHomeListener(home);
    addPlanControllerListener(controller.getPlanController());
    JMenuBar homeMenuBar = getHomeMenuBar(home, controller, contentManager);
    setJMenuBar(homeMenuBar);
    getContentPane().add(getToolBar(), BorderLayout.NORTH);
    getContentPane().add(getMainPane(home, preferences, controller));
    
    disableMenuItemsDuringDragAndDrop(this.planView, homeMenuBar);
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
    createAction(ActionType.DELETE_SELECTION, 
        controller.getPlanController(), "deleteSelection");
    createAction(ActionType.MODIFY_WALL, 
        controller.getPlanController(), "modifySelectedWalls");
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
   * Creates components transfer handlers.
   */
  private void createTransferHandlers(Home home, UserPreferences preferences,
                                      ContentManager contentManager,
                                      HomeController controller) {
    this.catalogTransferHandler = 
        new CatalogTransferHandler(preferences.getCatalog(), contentManager, controller.getCatalogController());
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
    JMenu fileMenu = new JMenu(new ResourceAction(this.resource, "FILE_MENU", true));
    fileMenu.add(getMenuAction(ActionType.NEW_HOME));
    fileMenu.add(getMenuAction(ActionType.OPEN));
    
    final JMenu openRecentHomeMenu = 
        new JMenu(new ResourceAction(this.resource, "OPEN_RECENT_HOME_MENU", true));
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
    // Don't add EXIT menu under Mac OS X, it's displayed in application menu  
    if (!System.getProperty("os.name").startsWith("Mac OS X")) {
      fileMenu.addSeparator();
      fileMenu.add(getMenuAction(ActionType.PREFERENCES));
      fileMenu.addSeparator();
      fileMenu.add(getMenuAction(ActionType.EXIT));
    }

    // Create Edit menu
    JMenu editMenu = new JMenu(new ResourceAction(this.resource, "EDIT_MENU", true));
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
    JMenu furnitureMenu = new JMenu(new ResourceAction(this.resource, "FURNITURE_MENU", true));
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
    JMenu planMenu = new JMenu(new ResourceAction(this.resource, "PLAN_MENU", true));
    JRadioButtonMenuItem selectRadioButtonMenuItem = getSelectRadioButtonMenuItem(false);
    planMenu.add(selectRadioButtonMenuItem);
    JRadioButtonMenuItem createWallsRadioButtonMenuItem = getCreateWallsRadioButtonMenuItem(false);
    planMenu.add(createWallsRadioButtonMenuItem);
    // Add Select and Create Walls menu items to radio group 
    ButtonGroup group = new ButtonGroup();
    group.add(selectRadioButtonMenuItem);
    group.add(createWallsRadioButtonMenuItem);  
    planMenu.addSeparator();
    planMenu.add(getMenuAction(ActionType.MODIFY_WALL));
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
    JMenu preview3DMenu = new JMenu(new ResourceAction(this.resource, "VIEW_3D_MENU", true));
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
    
    // Create Help menu
    JMenu helpMenu = new JMenu(new ResourceAction(this.resource, "HELP_MENU", true));
    helpMenu.add(getMenuAction(ActionType.HELP));      
    if (!System.getProperty("os.name").startsWith("Mac OS X")) {
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
    JMenu sortMenu = new JMenu(new ResourceAction(this.resource, "SORT_HOME_FURNITURE_MENU", true));
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
    JMenu displayPropertyMenu = new JMenu(new ResourceAction(
        this.resource, "DISPLAY_HOME_FURNITURE_PROPERTY_MENU", true));
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
   * @param contentManager 
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
    JToolBar toolBar = new JToolBar();
    ActionMap actions = getActionMap();    
    toolBar.add(actions.get(ActionType.NEW_HOME));
    toolBar.add(actions.get(ActionType.OPEN));
    toolBar.add(actions.get(ActionType.SAVE));
    toolBar.addSeparator();

    toolBar.add(actions.get(ActionType.UNDO));
    toolBar.add(actions.get(ActionType.REDO));
    toolBar.addSeparator();
    
    toolBar.add(actions.get(ActionType.CUT));
    toolBar.add(actions.get(ActionType.COPY));
    toolBar.add(actions.get(ActionType.PASTE));
    toolBar.addSeparator();
    
    toolBar.add(actions.get(ActionType.DELETE));
    toolBar.addSeparator();

    toolBar.add(actions.get(ActionType.ADD_HOME_FURNITURE));
    toolBar.add(actions.get(ActionType.IMPORT_FURNITURE));
    toolBar.add(actions.get(ActionType.ALIGN_FURNITURE_ON_TOP));
    toolBar.add(actions.get(ActionType.ALIGN_FURNITURE_ON_BOTTOM));
    toolBar.add(actions.get(ActionType.ALIGN_FURNITURE_ON_LEFT));
    toolBar.add(actions.get(ActionType.ALIGN_FURNITURE_ON_RIGHT));
    toolBar.addSeparator();
   
    JToggleButton selectToggleButton = 
        new JToggleButton(actions.get(ActionType.SELECT));
    // Use the same model as Select menu item
    selectToggleButton.setModel(this.selectToggleModel);
    // Don't display text with icon
    selectToggleButton.setText("");
    toolBar.add(selectToggleButton);
    JToggleButton createWallsToggleButton = 
        new JToggleButton(actions.get(ActionType.CREATE_WALLS));
    // Use the same model as Create walls menu item
    createWallsToggleButton.setModel(this.createWallsToggleModel);
    // Don't display text with icon
    createWallsToggleButton.setText("");
    toolBar.add(createWallsToggleButton);
    // Add Select and Create Walls buttons to radio group 
    ButtonGroup group = new ButtonGroup();
    group.add(selectToggleButton);
    group.add(createWallsToggleButton);
    toolBar.addSeparator();
    
    toolBar.add(actions.get(ActionType.ZOOM_OUT));
    toolBar.add(actions.get(ActionType.ZOOM_IN));
    toolBar.addSeparator();
    
    toolBar.add(actions.get(ActionType.HELP));
    
    // Remove focusable property on buttons
    for (int i = 0, n = toolBar.getComponentCount(); i < n; i++) {
      toolBar.getComponentAtIndex(i).setFocusable(false);      
    }
    
    return toolBar;
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
      this.catalogView.setTransferHandler(this.catalogTransferHandler);
      this.furnitureView.setTransferHandler(this.furnitureTransferHandler);
      this.planView.setTransferHandler(this.planTransferHandler);
      ((JViewport)this.furnitureView.getParent()).setTransferHandler(this.furnitureTransferHandler);
    } else {
      this.catalogView.setTransferHandler(null);
      this.furnitureView.setTransferHandler(null);
      this.planView.setTransferHandler(null);
      ((JViewport)this.furnitureView.getParent()).setTransferHandler(null);
    }
  }

  /**
   * Returns the main pane with catalog tree, furniture table and plan pane. 
   */
  private JComponent getMainPane(Home home, UserPreferences preferences, 
                                 HomeController controller) {
    JSplitPane mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
        getCatalogFurniturePane(controller), 
        getPlanView3DPane(home, preferences, controller));
    mainPane.setContinuousLayout(true);
    mainPane.setOneTouchExpandable(true);
    mainPane.setResizeWeight(0.3);
    return mainPane;
  }

  /**
   * Returns the catalog tree and furniture table pane. 
   */
  private JComponent getCatalogFurniturePane(HomeController controller) {
    this.catalogView = controller.getCatalogController().getView();
    JScrollPane catalogScrollPane = new HomeScrollPane(this.catalogView);
    // Add focus listener to catalog tree
    this.catalogView.addFocusListener(new FocusableViewListener(
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
    this.catalogView.setComponentPopupMenu(catalogViewPopup);

    // Configure furniture view
    this.furnitureView = controller.getFurnitureController().getView();
    JScrollPane furnitureScrollPane = new HomeScrollPane(this.furnitureView);
    // Set default traversal keys of furniture view
    KeyboardFocusManager focusManager =
        KeyboardFocusManager.getCurrentKeyboardFocusManager();
    this.furnitureView.setFocusTraversalKeys(
        KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
        focusManager.getDefaultFocusTraversalKeys(
            KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
    this.furnitureView.setFocusTraversalKeys(
        KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
        focusManager.getDefaultFocusTraversalKeys(
            KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));

    // Add focus listener to furniture table 
    this.furnitureView.addFocusListener(new FocusableViewListener(
        controller, furnitureScrollPane));
    // Add a mouse listener that gives focus to furniture view when
    // user clicks in its viewport
    ((JViewport)this.furnitureView.getParent()).addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent ev) {
            furnitureView.requestFocusInWindow();
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
    this.furnitureView.setComponentPopupMenu(furnitureViewPopup);
    ((JViewport)this.furnitureView.getParent()).setComponentPopupMenu(furnitureViewPopup);
    
    // Create a split pane that displays both components
    JSplitPane catalogFurniturePane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
        catalogScrollPane, furnitureScrollPane);
    catalogFurniturePane.setContinuousLayout(true);
    catalogFurniturePane.setOneTouchExpandable(true);
    catalogFurniturePane.setResizeWeight(0.5);
    return catalogFurniturePane;
  }

  /**
   * Returns the plan view and 3D view pane. 
   */
  private JComponent getPlanView3DPane(Home home, UserPreferences preferences, 
                                       HomeController controller) {
    this.planView = controller.getPlanController().getView();
    JScrollPane planScrollPane = new HomeScrollPane(this.planView);
    setPlanRulersVisible(planScrollPane, controller, preferences.isRulersVisible());
    // Add a listener to update rulers visibility in preferences
    preferences.addPropertyChangeListener(UserPreferences.Property.RULERS_VISIBLE, 
        new RulersVisibilityChangeListener(this, planScrollPane, controller));
    this.planView.addFocusListener(new FocusableViewListener(
        controller, planScrollPane));

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
    // Add Select and Create Walls menu items to radio group 
    ButtonGroup group = new ButtonGroup();
    group.add(selectRadioButtonMenuItem);
    group.add(createWallsRadioButtonMenuItem);
    planViewPopup.addSeparator();
    planViewPopup.add(getPopupAction(ActionType.MODIFY_FURNITURE));
    planViewPopup.add(getPopupAction(ActionType.MODIFY_WALL));
    planViewPopup.addSeparator();
    planViewPopup.add(getPopupAction(ActionType.ZOOM_OUT));
    planViewPopup.add(getPopupAction(ActionType.ZOOM_IN));
    this.planView.setComponentPopupMenu(planViewPopup);
    
    // Configure 3D view
    JComponent view3D = controller.getHomeController3D().getView();
    view3D.setPreferredSize(this.planView.getPreferredSize());
    view3D.setMinimumSize(new Dimension(0, 0));
    view3D.addFocusListener(new FocusableViewListener(
        controller, view3D));
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
    
    // Create a split pane that displays both components
    JSplitPane planView3DPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
        planScrollPane, view3D);
    planView3DPane.setContinuousLayout(true);
    planView3DPane.setOneTouchExpandable(true);
    planView3DPane.setResizeWeight(0.5);
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
   * Displays a dialog that let user choose whether he wants to overwrite 
   * file <code>name</code> or not.
   * @return <code>true</code> if user confirmed to overwrite.
   */
  public boolean confirmOverwrite(String homeName) {
    // Retrieve displayed text in buttons and message
    String messageFormat = this.resource.getString("confirmOverwrite.message");
    String message = String.format(messageFormat, 
        this.contentManager.getPresentationName(
            homeName, ContentManager.ContentType.SWEET_HOME_3D));
    String title = this.resource.getString("confirmOverwrite.title");
    String replace = this.resource.getString("confirmOverwrite.overwrite");
    String cancel = this.resource.getString("confirmOverwrite.cancel");
    
    return JOptionPane.showOptionDialog(this, message, title, 
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
        null, new Object [] {replace, cancel}, cancel) == JOptionPane.OK_OPTION;
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
    String message = String.format(messageFormat, System.getProperty("java.version"));
    // Use an uneditable editor pane to let user select text in dialog
    JEditorPane messagePane = new JEditorPane("text/html", message);
    messagePane.setOpaque(false);
    messagePane.setEditable(false);
    messagePane.addHyperlinkListener(new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent ev) {
        if (ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          viewURL(ev.getURL());
        }
      }
    });
    
    String title = this.resource.getString("about.title");
    Icon   icon  = new ImageIcon(HomePane.class.getResource(
        this.resource.getString("about.icon")));
    JOptionPane.showMessageDialog(this, messagePane, title,  
        JOptionPane.INFORMATION_MESSAGE, icon);
  }

  /**
   * Launches browser with <code>url</code>.
   */
  private void viewURL(URL url) {
    try { 
      // Lookup the javax.jnlp.BasicService object 
      BasicService service = 
          (BasicService)ServiceManager.lookup("javax.jnlp.BasicService"); 
      service.showDocument(url); 
    } catch (UnavailableServiceException ex) {
      // Too bad : service is unavailable 
    } 
  }

  /**
   * Shows the page setup dialog matching <code>homePrint</code> 
   * and returns the print attributes chosen by user.
   * If user cancelled his choice, <code>homePrint</code> parameter is returned unchanged.
   */
  public HomePrint setupPage(HomePrint homePrint) {
    PrinterJob printerJob = PrinterJob.getPrinterJob();
    
    PageFormat pageFormat;
    if (homePrint == null) {
      pageFormat = printerJob.defaultPage();
    } else {
      pageFormat = printerJob.validatePage(getPageFormat(homePrint));
    }
    PageFormat returnedPageFormat = printerJob.pageDialog(pageFormat);
    if (returnedPageFormat == pageFormat) {
      // User clicked on Cancel
      return homePrint;
    } else {
      // Return an HomePrint instance matching returnedPageFormat
      HomePrint.PaperOrientation paperOrientation; 
      switch (returnedPageFormat.getOrientation()) {
        case PageFormat.LANDSCAPE :
          paperOrientation = HomePrint.PaperOrientation.LANDSCAPE;
          break;
        case PageFormat.REVERSE_LANDSCAPE :
          paperOrientation = HomePrint.PaperOrientation.REVERSE_LANDSCAPE;
          break;
        default :
          paperOrientation = HomePrint.PaperOrientation.PORTRAIT;
          break;
      }
      Paper paper = returnedPageFormat.getPaper();
      return new HomePrint(paperOrientation, (float)paper.getWidth(), (float)paper.getHeight(),
          (float)paper.getImageableY(), (float)paper.getImageableX(),
          (float)(paper.getHeight() - paper.getImageableHeight() - paper.getImageableY()),
          (float)(paper.getWidth() - paper.getImageableWidth() - paper.getImageableX()),
          homePrint == null ? true : homePrint.isFurniturePrinted(),
          homePrint == null ? true : homePrint.isPlanPrinted(),
          homePrint == null ? true : homePrint.isView3DPrinted());
    }
  }
  
  /**
   * Returns a <code>PageFormat</code> object created from <code>homePrint</code>.
   */
  private PageFormat getPageFormat(HomePrint homePrint) {
    PageFormat pageFormat = new PageFormat();
    switch (homePrint.getPaperOrientation()) {
      case PORTRAIT :
        pageFormat.setOrientation(PageFormat.PORTRAIT);
        break;
      case LANDSCAPE :
        pageFormat.setOrientation(PageFormat.LANDSCAPE);
        break;
      case REVERSE_LANDSCAPE :
        pageFormat.setOrientation(PageFormat.REVERSE_LANDSCAPE);
        break;
    }
    Paper paper = new Paper();
    paper.setSize(homePrint.getPaperWidth(), homePrint.getPaperHeight());
    paper.setImageableArea(homePrint.getPaperLeftMargin(), homePrint.getPaperTopMargin(), 
        homePrint.getPaperWidth() - homePrint.getPaperLeftMargin() - homePrint.getPaperRightMargin(), 
        homePrint.getPaperHeight() - homePrint.getPaperTopMargin() - homePrint.getPaperBottomMargin());
    pageFormat.setPaper(paper);
    return pageFormat;
  }
  
  /**
   * Prints the views managed by the controllers in parameter.
   */
  public boolean print(HomePrint homePrint,
                       HomeController controller) {
    PrinterJob printerJob = PrinterJob.getPrinterJob();
    PageFormat pageFormat;
    if (homePrint == null) {
      pageFormat = printerJob.defaultPage();
    } else {
      pageFormat = printerJob.validatePage(getPageFormat(homePrint));
    }
    printerJob.setPrintable(new PrintableComponent(homePrint, controller), pageFormat);
    if (printerJob.printDialog()) {
      // Create a waiting glass pane for this lengthy operation
      Component previousGlassPane = getGlassPane(); 
      JLabel waitGlassPane = new JLabel();
      waitGlassPane.setOpaque(false);
      waitGlassPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      setGlassPane(waitGlassPane);
      waitGlassPane.setVisible(true);
      
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
      if (System.getProperty("os.name").startsWith("Mac OS X")) {
        setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_ALWAYS);
        setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
      }
    }
  }

  private static final Border UNFOCUSED_BORDER = 
    BorderFactory.createEmptyBorder(2, 2, 2, 2);
  private static final Border FOCUSED_BORDER = 
    BorderFactory.createLineBorder(UIManager.getColor("textHighlight"), 2); 

  /**
   * A focus listener that calls <code>focusChanged</code> in 
   * home controller.
   */
  private class FocusableViewListener implements FocusListener {
    private HomeController controller;
    private JComponent     feedbackComponent;
  
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
    }
    
    public void focusLost(FocusEvent ev) {
      this.feedbackComponent.setBorder(UNFOCUSED_BORDER);
    }
  }
  
  /**
   * A printable component used to print furniture view, plan view 
   * and 3D view.
   */
  private static class PrintableComponent extends JComponent implements Printable {
    private HomePrint      homePrint;
    private HomeController controller;
    private int            planViewIndex = 0;
    
    public PrintableComponent(HomePrint homePrint, HomeController controller) {
      this.homePrint = homePrint;
      this.controller = controller;
    }
    
    public int print(Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException {
      Graphics2D g2D = (Graphics2D)g;
      g2D.setColor(Color.WHITE);
      g2D.fill(new Rectangle2D.Double(0, 0, pageFormat.getWidth(), 
                                      pageFormat.getHeight()));
      
      int pageExists = NO_SUCH_PAGE;
      if ((this.homePrint == null || this.homePrint.isFurniturePrinted())
          && pageIndex <= this.planViewIndex) {
        // Try to print next furniture view page
        pageExists = ((Printable)this.controller.getFurnitureController().getView()).print(g2D, pageFormat, pageIndex);
        if (pageExists == PAGE_EXISTS) {
          this.planViewIndex = pageIndex + 1;
        }
      } 
      if ((this.homePrint == null || this.homePrint.isPlanPrinted())
          && pageIndex == this.planViewIndex) {
        ((Printable)this.controller.getPlanController().getView()).print(g2D, pageFormat, 0);
        pageExists = PAGE_EXISTS;
      } else if ((this.homePrint == null && pageIndex == this.planViewIndex + 1)
                 || (this.homePrint != null
                     && this.homePrint.isView3DPrinted()
                     && ((this.homePrint.isPlanPrinted()
                           && pageIndex == this.planViewIndex + 1)
                         || (!this.homePrint.isPlanPrinted()
                             && pageIndex == this.planViewIndex)))) {
        ((Printable)this.controller.getHomeController3D().getView()).print(g2D, pageFormat, 0);
        pageExists = PAGE_EXISTS;
      }
      
      return pageExists;
    }
  }
}
