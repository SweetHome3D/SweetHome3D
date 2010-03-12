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
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FocusTraversalPolicy;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.LayoutFocusTraversalPolicy;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.SwingPropertyChangeSupport;
import javax.swing.text.JTextComponent;

import com.eteks.sweethome3d.j3d.Ground3D;
import com.eteks.sweethome3d.j3d.HomePieceOfFurniture3D;
import com.eteks.sweethome3d.j3d.OBJWriter;
import com.eteks.sweethome3d.j3d.Room3D;
import com.eteks.sweethome3d.j3d.Wall3D;
import com.eteks.sweethome3d.model.BackgroundImage;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.DimensionLine;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeFurnitureGroup;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.InterruptedRecorderException;
import com.eteks.sweethome3d.model.Label;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.SelectionEvent;
import com.eteks.sweethome3d.model.SelectionListener;
import com.eteks.sweethome3d.model.TextStyle;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.plugin.Plugin;
import com.eteks.sweethome3d.plugin.PluginAction;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.ContentManager;
import com.eteks.sweethome3d.viewcontroller.FurnitureController;
import com.eteks.sweethome3d.viewcontroller.HomeController;
import com.eteks.sweethome3d.viewcontroller.HomeView;
import com.eteks.sweethome3d.viewcontroller.PlanController;
import com.eteks.sweethome3d.viewcontroller.View;

/**
 * The MVC view that edits a home. 
 * @author Emmanuel Puybaret
 */
public class HomePane extends JRootPane implements HomeView {
  private enum MenuActionType {FILE_MENU, EDIT_MENU, FURNITURE_MENU, PLAN_MENU, VIEW_3D_MENU, HELP_MENU, 
      OPEN_RECENT_HOME_MENU, SORT_HOME_FURNITURE_MENU, DISPLAY_HOME_FURNITURE_PROPERTY_MENU, MODIFY_TEXT_STYLE}
  
  private static final String MAIN_PANE_DIVIDER_LOCATION_VISUAL_PROPERTY     = "com.eteks.sweethome3d.SweetHome3D.MainPaneDividerLocation";
  private static final String CATALOG_PANE_DIVIDER_LOCATION_VISUAL_PROPERTY  = "com.eteks.sweethome3d.SweetHome3D.CatalogPaneDividerLocation";
  private static final String PLAN_PANE_DIVIDER_LOCATION_VISUAL_PROPERTY     = "com.eteks.sweethome3d.SweetHome3D.PlanPaneDividerLocation";
  private static final String PLAN_VIEWPORT_X_VISUAL_PROPERTY                = "com.eteks.sweethome3d.SweetHome3D.PlanViewportX";
  private static final String PLAN_VIEWPORT_Y_VISUAL_PROPERTY                = "com.eteks.sweethome3d.SweetHome3D.PlanViewportY";
  private static final String FURNITURE_VIEWPORT_Y_VISUAL_PROPERTY           = "com.eteks.sweethome3d.SweetHome3D.FurnitureViewportY";
  private static final String DETACHED_VIEW_VISUAL_PROPERTY                  = ".detachedView";
  private static final String DETACHED_VIEW_DIVIDER_LOCATION_VISUAL_PROPERTY = ".detachedViewDividerLocation";
  private static final String DETACHED_VIEW_X_VISUAL_PROPERTY                = ".detachedViewX";
  private static final String DETACHED_VIEW_Y_VISUAL_PROPERTY                = ".detachedViewY";
  private static final String DETACHED_VIEW_WIDTH_VISUAL_PROPERTY            = ".detachedViewWidth";
  private static final String DETACHED_VIEW_HEIGHT_VISUAL_PROPERTY           = ".detachedViewHeight";

  private static final int    DEFAULT_SMALL_ICON_HEIGHT = 16;
  
  private final Home                            home;
  private final UserPreferences                 preferences;
  private final HomeController                  controller;
  // Button models shared by Select, Pan, Create walls, Create rooms, Create dimensions 
  // and Create labels menu items and their matching tool bar buttons
  private final JToggleButton.ToggleButtonModel selectToggleModel;
  private final JToggleButton.ToggleButtonModel panToggleModel;
  private final JToggleButton.ToggleButtonModel createWallsToggleModel;
  private final JToggleButton.ToggleButtonModel createRoomsToggleModel;
  private final JToggleButton.ToggleButtonModel createDimensionLinesToggleModel;
  private final JToggleButton.ToggleButtonModel createLabelsToggleModel;
  // Button models shared by Bold and Italic menu items and their matching tool bar buttons
  private final JToggleButton.ToggleButtonModel boldStyleToggleModel;
  private final JToggleButton.ToggleButtonModel italicStyleToggleModel;
  // Button models shared by View from top and View from observer menu items and
  // the matching tool bar buttons
  private final JToggleButton.ToggleButtonModel viewFromTopToggleModel;
  private final JToggleButton.ToggleButtonModel viewFromObserverToggleModel;
  private JComponent                            lastFocusedComponent;
  private PlanController.Mode                   previousPlanControllerMode;
  private TransferHandler                       catalogTransferHandler;
  private TransferHandler                       furnitureTransferHandler;
  private TransferHandler                       planTransferHandler;
  private ActionMap                             menuActionMap;
  private List<Action>                          pluginActions;
  
  /**
   * Creates home view associated with its controller.
   */
  public HomePane(Home home, UserPreferences preferences, 
                  HomeController controller) {
    this.home = home;
    this.preferences = preferences;
    this.controller = controller;
    // Create unique toggle button models for Selection / Pan / Wall creation / Room creation / 
    // Dimension line creation / Label creation states
    // so the matching menu items and tool bar buttons always reflect the same toggle state at screen
    this.selectToggleModel = new JToggleButton.ToggleButtonModel();
    this.selectToggleModel.setSelected(controller.getPlanController().getMode() 
        == PlanController.Mode.SELECTION);
    this.panToggleModel = new JToggleButton.ToggleButtonModel();
    this.panToggleModel.setSelected(controller.getPlanController().getMode() 
        == PlanController.Mode.PANNING);
    this.createWallsToggleModel = new JToggleButton.ToggleButtonModel();
    this.createWallsToggleModel.setSelected(controller.getPlanController().getMode() 
        == PlanController.Mode.WALL_CREATION);
    this.createRoomsToggleModel = new JToggleButton.ToggleButtonModel();
    this.createRoomsToggleModel.setSelected(controller.getPlanController().getMode() 
        == PlanController.Mode.ROOM_CREATION);
    this.createDimensionLinesToggleModel = new JToggleButton.ToggleButtonModel();
    this.createDimensionLinesToggleModel.setSelected(controller.getPlanController().getMode() 
        == PlanController.Mode.DIMENSION_LINE_CREATION);
    this.createLabelsToggleModel = new JToggleButton.ToggleButtonModel();
    this.createLabelsToggleModel.setSelected(controller.getPlanController().getMode() 
        == PlanController.Mode.LABEL_CREATION);
    
    ButtonGroup modeGroup = new ButtonGroup();
    this.selectToggleModel.setGroup(modeGroup);
    this.panToggleModel.setGroup(modeGroup);
    this.createWallsToggleModel.setGroup(modeGroup);
    this.createRoomsToggleModel.setGroup(modeGroup);
    this.createDimensionLinesToggleModel.setGroup(modeGroup);
    this.createLabelsToggleModel.setGroup(modeGroup);
    
    // Use special models for bold and italic check box menu items and tool bar buttons 
    // that are selected texts in home selected items are all bold or italic
    this.boldStyleToggleModel = createBoldStyleToggleModel(home, preferences);
    this.italicStyleToggleModel = createItalicStyleToggleModel(home, preferences);
    // Create unique toggle button models for top and observer cameras
    // so View from top and View from observer creation menu items and tool bar buttons 
    // always reflect the same toggle state at screen
    this.viewFromTopToggleModel = new JToggleButton.ToggleButtonModel();
    this.viewFromTopToggleModel.setSelected(home.getCamera() == home.getTopCamera());
    this.viewFromObserverToggleModel = new JToggleButton.ToggleButtonModel();
    this.viewFromObserverToggleModel.setSelected(home.getCamera() == home.getObserverCamera());

    ButtonGroup viewGroup = new ButtonGroup();
    this.viewFromTopToggleModel.setGroup(viewGroup);
    this.viewFromObserverToggleModel.setGroup(viewGroup);

    JPopupMenu.setDefaultLightWeightPopupEnabled(false);
    ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);    
    
    createActions(preferences, controller);
    createMenuActions(preferences, controller);   
    createPluginActions(controller.getPlugins());
    createTransferHandlers(home, controller);
    addHomeListener(home);
    addLanguageListener(preferences);
    addPlanControllerListener(controller.getPlanController());
    addFocusListener();
    updateFocusTraversalPolicy();
    JMenuBar homeMenuBar = createMenuBar(home, preferences, controller);
    setJMenuBar(homeMenuBar);
    Container contentPane = getContentPane();
    contentPane.add(createToolBar(home), BorderLayout.NORTH);
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
  private void createActions(UserPreferences preferences, 
                             final HomeController controller) {
    createAction(ActionType.NEW_HOME, preferences, controller, "newHome");
    createAction(ActionType.OPEN, preferences, controller, "open");
    createAction(ActionType.DELETE_RECENT_HOMES, preferences, controller, "deleteRecentHomes");
    createAction(ActionType.CLOSE, preferences, controller, "close");
    createAction(ActionType.SAVE, preferences, controller, "save");
    createAction(ActionType.SAVE_AS, preferences, controller, "saveAs");
    createAction(ActionType.SAVE_AND_COMPRESS, preferences, controller, "saveAndCompress");
    createAction(ActionType.PAGE_SETUP, preferences, controller, "setupPage");
    createAction(ActionType.PRINT_PREVIEW, preferences, controller, "previewPrint");
    createAction(ActionType.PRINT, preferences, controller, "print");
    createAction(ActionType.PRINT_TO_PDF, preferences, controller, "printToPDF");
    createAction(ActionType.PREFERENCES, preferences, controller, "editPreferences");
    createAction(ActionType.EXIT, preferences, controller, "exit");
    
    createAction(ActionType.UNDO, preferences, controller, "undo");
    createAction(ActionType.REDO, preferences, controller, "redo");
    createClipboardAction(ActionType.CUT, preferences, TransferHandler.getCutAction());
    createClipboardAction(ActionType.COPY, preferences, TransferHandler.getCopyAction());
    createClipboardAction(ActionType.PASTE, preferences, TransferHandler.getPasteAction());
    createAction(ActionType.DELETE, preferences, controller, "delete");
    createAction(ActionType.SELECT_ALL, preferences, controller, "selectAll");
    
    createAction(ActionType.ADD_HOME_FURNITURE, preferences, controller, "addHomeFurniture");
    FurnitureController furnitureController = controller.getFurnitureController();
    createAction(ActionType.DELETE_HOME_FURNITURE, preferences,
        furnitureController, "deleteSelection");
    createAction(ActionType.MODIFY_FURNITURE, preferences, controller, "modifySelectedFurniture");
    createAction(ActionType.GROUP_FURNITURE, preferences, 
        furnitureController, "groupSelectedFurniture");
    createAction(ActionType.UNGROUP_FURNITURE, preferences, 
        furnitureController, "ungroupSelectedFurniture");
    createAction(ActionType.ALIGN_FURNITURE_ON_TOP, preferences, 
        furnitureController, "alignSelectedFurnitureOnTop");
    createAction(ActionType.ALIGN_FURNITURE_ON_BOTTOM, preferences, 
        furnitureController, "alignSelectedFurnitureOnBottom");
    createAction(ActionType.ALIGN_FURNITURE_ON_LEFT, preferences, 
        furnitureController, "alignSelectedFurnitureOnLeft");
    createAction(ActionType.ALIGN_FURNITURE_ON_RIGHT, preferences, 
        furnitureController, "alignSelectedFurnitureOnRight");
    createAction(ActionType.IMPORT_FURNITURE, preferences, controller, "importFurniture");
    createAction(ActionType.IMPORT_FURNITURE_LIBRARY, preferences, controller, "importFurnitureLibrary");
    createAction(ActionType.IMPORT_TEXTURES_LIBRARY, preferences, controller, "importTexturesLibrary");
    createAction(ActionType.SORT_HOME_FURNITURE_BY_CATALOG_ID, preferences, 
        furnitureController, "toggleFurnitureSort", HomePieceOfFurniture.SortableProperty.CATALOG_ID);
    createAction(ActionType.SORT_HOME_FURNITURE_BY_NAME, preferences, 
        furnitureController, "toggleFurnitureSort", HomePieceOfFurniture.SortableProperty.NAME);
    createAction(ActionType.SORT_HOME_FURNITURE_BY_WIDTH, preferences, 
        furnitureController, "toggleFurnitureSort", HomePieceOfFurniture.SortableProperty.WIDTH);
    createAction(ActionType.SORT_HOME_FURNITURE_BY_DEPTH, preferences, 
        furnitureController, "toggleFurnitureSort", HomePieceOfFurniture.SortableProperty.DEPTH);
    createAction(ActionType.SORT_HOME_FURNITURE_BY_HEIGHT, preferences, 
        furnitureController, "toggleFurnitureSort", HomePieceOfFurniture.SortableProperty.HEIGHT);
    createAction(ActionType.SORT_HOME_FURNITURE_BY_X, preferences, 
        furnitureController, "toggleFurnitureSort", HomePieceOfFurniture.SortableProperty.X);
    createAction(ActionType.SORT_HOME_FURNITURE_BY_Y, preferences, 
        furnitureController, "toggleFurnitureSort", HomePieceOfFurniture.SortableProperty.Y);
    createAction(ActionType.SORT_HOME_FURNITURE_BY_ELEVATION, preferences, 
        furnitureController, "toggleFurnitureSort", HomePieceOfFurniture.SortableProperty.ELEVATION);
    createAction(ActionType.SORT_HOME_FURNITURE_BY_ANGLE, preferences, 
        furnitureController, "toggleFurnitureSort", HomePieceOfFurniture.SortableProperty.ANGLE);
    createAction(ActionType.SORT_HOME_FURNITURE_BY_COLOR, preferences, 
        furnitureController, "toggleFurnitureSort", HomePieceOfFurniture.SortableProperty.COLOR);
    createAction(ActionType.SORT_HOME_FURNITURE_BY_TEXTURE, preferences, 
        furnitureController, "toggleFurnitureSort", HomePieceOfFurniture.SortableProperty.TEXTURE);
    createAction(ActionType.SORT_HOME_FURNITURE_BY_MOVABILITY, preferences, 
        furnitureController, "toggleFurnitureSort", HomePieceOfFurniture.SortableProperty.MOVABLE);
    createAction(ActionType.SORT_HOME_FURNITURE_BY_TYPE, preferences, 
        furnitureController, "toggleFurnitureSort", HomePieceOfFurniture.SortableProperty.DOOR_OR_WINDOW);
    createAction(ActionType.SORT_HOME_FURNITURE_BY_VISIBILITY, preferences, 
        furnitureController, "toggleFurnitureSort", HomePieceOfFurniture.SortableProperty.VISIBLE);
    createAction(ActionType.SORT_HOME_FURNITURE_BY_PRICE, preferences, 
        furnitureController, "toggleFurnitureSort", HomePieceOfFurniture.SortableProperty.PRICE);
    createAction(ActionType.SORT_HOME_FURNITURE_BY_VALUE_ADDED_TAX_PERCENTAGE, preferences, 
        furnitureController, "toggleFurnitureSort", HomePieceOfFurniture.SortableProperty.VALUE_ADDED_TAX_PERCENTAGE);
    createAction(ActionType.SORT_HOME_FURNITURE_BY_VALUE_ADDED_TAX, preferences, 
        furnitureController, "toggleFurnitureSort", HomePieceOfFurniture.SortableProperty.VALUE_ADDED_TAX);
    createAction(ActionType.SORT_HOME_FURNITURE_BY_PRICE_VALUE_ADDED_TAX_INCLUDED, preferences, 
        furnitureController, "toggleFurnitureSort", HomePieceOfFurniture.SortableProperty.PRICE_VALUE_ADDED_TAX_INCLUDED);
    createAction(ActionType.SORT_HOME_FURNITURE_BY_DESCENDING_ORDER, preferences, 
        furnitureController, "toggleFurnitureSortOrder");
    createAction(ActionType.DISPLAY_HOME_FURNITURE_CATALOG_ID, preferences, 
        furnitureController, "toggleFurnitureVisibleProperty", HomePieceOfFurniture.SortableProperty.CATALOG_ID);
    createAction(ActionType.DISPLAY_HOME_FURNITURE_NAME, preferences, 
        furnitureController, "toggleFurnitureVisibleProperty", HomePieceOfFurniture.SortableProperty.NAME);
    createAction(ActionType.DISPLAY_HOME_FURNITURE_WIDTH, preferences, 
        furnitureController, "toggleFurnitureVisibleProperty", HomePieceOfFurniture.SortableProperty.WIDTH);
    createAction(ActionType.DISPLAY_HOME_FURNITURE_DEPTH, preferences, 
        furnitureController, "toggleFurnitureVisibleProperty", HomePieceOfFurniture.SortableProperty.DEPTH);
    createAction(ActionType.DISPLAY_HOME_FURNITURE_HEIGHT, preferences, 
        furnitureController, "toggleFurnitureVisibleProperty", HomePieceOfFurniture.SortableProperty.HEIGHT);
    createAction(ActionType.DISPLAY_HOME_FURNITURE_X, preferences, 
        furnitureController, "toggleFurnitureVisibleProperty", HomePieceOfFurniture.SortableProperty.X);
    createAction(ActionType.DISPLAY_HOME_FURNITURE_Y, preferences, 
        furnitureController, "toggleFurnitureVisibleProperty", HomePieceOfFurniture.SortableProperty.Y);
    createAction(ActionType.DISPLAY_HOME_FURNITURE_ELEVATION, preferences, 
        furnitureController, "toggleFurnitureVisibleProperty", HomePieceOfFurniture.SortableProperty.ELEVATION);
    createAction(ActionType.DISPLAY_HOME_FURNITURE_ANGLE, preferences, 
        furnitureController, "toggleFurnitureVisibleProperty", HomePieceOfFurniture.SortableProperty.ANGLE);
    createAction(ActionType.DISPLAY_HOME_FURNITURE_COLOR, preferences, 
        furnitureController, "toggleFurnitureVisibleProperty", HomePieceOfFurniture.SortableProperty.COLOR);
    createAction(ActionType.DISPLAY_HOME_FURNITURE_TEXTURE, preferences, 
        furnitureController, "toggleFurnitureVisibleProperty", HomePieceOfFurniture.SortableProperty.TEXTURE);
    createAction(ActionType.DISPLAY_HOME_FURNITURE_MOVABLE, preferences, 
        furnitureController, "toggleFurnitureVisibleProperty", HomePieceOfFurniture.SortableProperty.MOVABLE);
    createAction(ActionType.DISPLAY_HOME_FURNITURE_DOOR_OR_WINDOW, preferences, 
        furnitureController, "toggleFurnitureVisibleProperty", HomePieceOfFurniture.SortableProperty.DOOR_OR_WINDOW);
    createAction(ActionType.DISPLAY_HOME_FURNITURE_VISIBLE, preferences, 
        furnitureController, "toggleFurnitureVisibleProperty", HomePieceOfFurniture.SortableProperty.VISIBLE);
    createAction(ActionType.DISPLAY_HOME_FURNITURE_PRICE, preferences, 
        furnitureController, "toggleFurnitureVisibleProperty", HomePieceOfFurniture.SortableProperty.PRICE);
    createAction(ActionType.DISPLAY_HOME_FURNITURE_VALUE_ADDED_TAX_PERCENTAGE, preferences, 
        furnitureController, "toggleFurnitureVisibleProperty", HomePieceOfFurniture.SortableProperty.VALUE_ADDED_TAX_PERCENTAGE);
    createAction(ActionType.DISPLAY_HOME_FURNITURE_VALUE_ADDED_TAX, preferences, 
        furnitureController, "toggleFurnitureVisibleProperty", HomePieceOfFurniture.SortableProperty.VALUE_ADDED_TAX);
    createAction(ActionType.DISPLAY_HOME_FURNITURE_PRICE_VALUE_ADDED_TAX_INCLUDED, preferences, 
        furnitureController, "toggleFurnitureVisibleProperty", HomePieceOfFurniture.SortableProperty.PRICE_VALUE_ADDED_TAX_INCLUDED);
    
    createAction(ActionType.SELECT, preferences, controller, "setMode", 
        PlanController.Mode.SELECTION);
    createAction(ActionType.PAN, preferences, controller, "setMode", 
        PlanController.Mode.PANNING);
    createAction(ActionType.CREATE_WALLS, preferences, controller, "setMode",
        PlanController.Mode.WALL_CREATION);
    createAction(ActionType.CREATE_ROOMS, preferences, controller, "setMode",
        PlanController.Mode.ROOM_CREATION);
    createAction(ActionType.CREATE_DIMENSION_LINES, preferences, controller, "setMode",
        PlanController.Mode.DIMENSION_LINE_CREATION);
    createAction(ActionType.CREATE_LABELS, preferences, controller, "setMode",
        PlanController.Mode.LABEL_CREATION);
    createAction(ActionType.DELETE_SELECTION, preferences, 
        controller.getPlanController(), "deleteSelection");
    createAction(ActionType.LOCK_BASE_PLAN, preferences, 
        controller.getPlanController(), "lockBasePlan");
    createAction(ActionType.UNLOCK_BASE_PLAN, preferences, 
        controller.getPlanController(), "unlockBasePlan");
    createAction(ActionType.MODIFY_WALL, preferences, 
        controller.getPlanController(), "modifySelectedWalls");
    createAction(ActionType.MODIFY_ROOM, preferences, 
        controller.getPlanController(), "modifySelectedRooms");
    createAction(ActionType.INCREASE_TEXT_SIZE, preferences, 
        controller.getPlanController(), "increaseTextSize");
    createAction(ActionType.DECREASE_TEXT_SIZE, preferences, 
        controller.getPlanController(), "decreaseTextSize");
    createAction(ActionType.TOGGLE_BOLD_STYLE, preferences, 
        controller.getPlanController(), "toggleBoldStyle");
    createAction(ActionType.TOGGLE_ITALIC_STYLE, preferences, 
        controller.getPlanController(), "toggleItalicStyle");
    createAction(ActionType.MODIFY_LABEL, preferences, 
        controller.getPlanController(), "modifySelectedLabels");
    createAction(ActionType.REVERSE_WALL_DIRECTION, preferences, 
        controller.getPlanController(), "reverseSelectedWallsDirection");
    createAction(ActionType.SPLIT_WALL, preferences, 
        controller.getPlanController(), "splitSelectedWall");
    createAction(ActionType.IMPORT_BACKGROUND_IMAGE, preferences, 
        controller, "importBackgroundImage");
    createAction(ActionType.MODIFY_BACKGROUND_IMAGE, preferences, 
        controller, "modifyBackgroundImage");
    createAction(ActionType.HIDE_BACKGROUND_IMAGE, preferences, 
        controller, "hideBackgroundImage");
    createAction(ActionType.SHOW_BACKGROUND_IMAGE, preferences, 
        controller, "showBackgroundImage");
    createAction(ActionType.DELETE_BACKGROUND_IMAGE, preferences, 
        controller, "deleteBackgroundImage");
    createAction(ActionType.ZOOM_IN, preferences, controller, "zoomIn");
    createAction(ActionType.ZOOM_OUT, preferences, controller, "zoomOut");
    createAction(ActionType.EXPORT_TO_SVG, preferences, controller, "exportToSVG");
    
    createAction(ActionType.VIEW_FROM_TOP, preferences, 
        controller.getHomeController3D(), "viewFromTop");
    createAction(ActionType.VIEW_FROM_OBSERVER, preferences, 
        controller.getHomeController3D(), "viewFromObserver");
    getActionMap().put(ActionType.DETACH_3D_VIEW, 
        new ResourceAction(preferences, HomePane.class, ActionType.DETACH_3D_VIEW.name()) {
          @Override
          public void actionPerformed(ActionEvent ev) {
            controller.detachView(controller.getHomeController3D().getView());
          }
        });
    getActionMap().put(ActionType.ATTACH_3D_VIEW, 
        new ResourceAction(preferences, HomePane.class, ActionType.ATTACH_3D_VIEW.name()) {
          @Override
          public void actionPerformed(ActionEvent ev) {
            controller.attachView(controller.getHomeController3D().getView());
          }
        });
    createAction(ActionType.MODIFY_3D_ATTRIBUTES, preferences, 
        controller.getHomeController3D(), "modifyAttributes");
    createAction(ActionType.CREATE_PHOTO, preferences, controller, "createPhoto");
    createAction(ActionType.CREATE_VIDEO, preferences, controller, "createVideo");
    createAction(ActionType.EXPORT_TO_OBJ, preferences, controller, "exportToOBJ");
    
    createAction(ActionType.HELP, preferences, controller, "help");
    createAction(ActionType.ABOUT, preferences, controller, "about");
  }

  /**
   * Creates a <code>ControllerAction</code> object that calls on <code>controller</code> a given
   * <code>method</code> with its <code>parameters</code>.
   */
  private void createAction(ActionType actionType,
                            UserPreferences preferences,                            
                            Object controller, 
                            String method, 
                            Object ... parameters) {
    try {
      getActionMap().put(actionType, new ControllerAction(
          preferences, HomePane.class, actionType.name(), controller, method, parameters));
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
                                     UserPreferences preferences,
                                     final Action clipboardAction) {
    getActionMap().put(actionType,
        new ResourceAction (preferences, HomePane.class, actionType.name()) {
          public void actionPerformed(ActionEvent ev) {
            ev = new ActionEvent(lastFocusedComponent, ActionEvent.ACTION_PERFORMED, null);
            clipboardAction.actionPerformed(ev);
          }
        });
  }

  /**
   * Create the actions map used to create menus of this component.
   */
  private void createMenuActions(UserPreferences preferences, 
                                 HomeController controller) {
    this.menuActionMap = new ActionMap();
    createMenuAction(preferences, MenuActionType.FILE_MENU);
    createMenuAction(preferences, MenuActionType.EDIT_MENU);
    createMenuAction(preferences, MenuActionType.FURNITURE_MENU);
    createMenuAction(preferences, MenuActionType.PLAN_MENU);
    createMenuAction(preferences, MenuActionType.VIEW_3D_MENU);
    createMenuAction(preferences, MenuActionType.HELP_MENU);
    createMenuAction(preferences, MenuActionType.OPEN_RECENT_HOME_MENU);
    createMenuAction(preferences, MenuActionType.SORT_HOME_FURNITURE_MENU);
    createMenuAction(preferences, MenuActionType.DISPLAY_HOME_FURNITURE_PROPERTY_MENU);
    createMenuAction(preferences, MenuActionType.MODIFY_TEXT_STYLE);
  }
  
  /**
   * Creates a <code>ResourceAction</code> object stored in menu action map.
   */
  private void createMenuAction(UserPreferences preferences, 
                                MenuActionType action) {
    this.menuActionMap.put(action, new ResourceAction(
        preferences, HomePane.class, action.name(), true));
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
  private void createTransferHandlers(Home home, 
                                      HomeController controller) {
    this.catalogTransferHandler = 
        new FurnitureCatalogTransferHandler(controller.getContentManager(), controller.getFurnitureCatalogController());
    this.furnitureTransferHandler = 
        new FurnitureTransferHandler(home, controller.getContentManager(), controller);
    this.planTransferHandler = 
        new PlanTransferHandler(home, controller.getContentManager(), controller);
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
        SwingTools.updateSwingResourceLanguage();
      }
    }
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
            panToggleModel.setSelected(planController.getMode() 
                == PlanController.Mode.PANNING);
            createWallsToggleModel.setSelected(planController.getMode() 
                == PlanController.Mode.WALL_CREATION);
            createRoomsToggleModel.setSelected(planController.getMode() 
                == PlanController.Mode.ROOM_CREATION);
            createDimensionLinesToggleModel.setSelected(planController.getMode() 
                == PlanController.Mode.DIMENSION_LINE_CREATION);
            createLabelsToggleModel.setSelected(planController.getMode() 
                == PlanController.Mode.LABEL_CREATION);
          }
        });
  }
  
  /**
   * Adds a focus change listener to report to controller focus changes.  
   */
  private void addFocusListener() {
    KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("currentFocusCycleRoot", 
        new FocusCycleRootChangeListener(this));    
  }
    
  /**
   * Property listener bound to this component with a weak reference to avoid
   * strong link between KeyboardFocusManager and this component.  
   */
  private static class FocusCycleRootChangeListener implements PropertyChangeListener {
    private WeakReference<HomePane> homePane;
    private PropertyChangeListener  focusChangeListener;

    public FocusCycleRootChangeListener(HomePane homePane) {
      this.homePane = new WeakReference<HomePane>(homePane);
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
      // If home pane was garbage collected, remove this listener from KeyboardFocusManager
      final HomePane homePane = this.homePane.get();
      if (homePane == null) {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().
            removePropertyChangeListener("currentFocusCycleRoot", this);
      } else {
        if (SwingUtilities.isDescendingFrom(homePane, (Component)ev.getOldValue())) {
          this.focusChangeListener = null;
          KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("focusOwner", 
              this.focusChangeListener);
        } else if (SwingUtilities.isDescendingFrom(homePane, (Component)ev.getNewValue())) {
          this.focusChangeListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              if (homePane.lastFocusedComponent != null) {
                // Update component which lost focused 
                JComponent lostFocusedComponent = homePane.lastFocusedComponent;
                if (SwingUtilities.isDescendingFrom(lostFocusedComponent, SwingUtilities.getWindowAncestor(homePane))) {
                  lostFocusedComponent.removeKeyListener(homePane.specialKeysListener);
                  // Restore previous plan mode if plan view had focus and window is deactivated
                  if (homePane.previousPlanControllerMode != null
                      && (lostFocusedComponent == homePane.controller.getPlanController().getView()
                          || ev.getNewValue() == null)) {
                    homePane.controller.getPlanController().setMode(homePane.previousPlanControllerMode);
                    homePane.previousPlanControllerMode = null;
                  }
                }
              }

              if (ev.getNewValue() != null) {
                // Retrieve component which gained focused 
                Component gainedFocusedComponent = (Component)ev.getNewValue(); 
                if (SwingUtilities.isDescendingFrom(gainedFocusedComponent, SwingUtilities.getWindowAncestor(homePane))
                    && gainedFocusedComponent instanceof JComponent) {
                  View [] focusableViews = {
                     homePane.controller.getFurnitureCatalogController().getView(),
                     homePane.controller.getFurnitureController().getView(),
                     homePane.controller.getPlanController().getView(),
                     homePane.controller.getHomeController3D().getView()};
                  // Notify controller that active view changed
                  for (View view : focusableViews) {
                    if (SwingUtilities.isDescendingFrom(gainedFocusedComponent, (JComponent)view)) {
                      homePane.controller.focusedViewChanged(view);
  
                      gainedFocusedComponent.addKeyListener(homePane.specialKeysListener);
                      
                      // Update the component used by clipboard actions
                      homePane.lastFocusedComponent = (JComponent)gainedFocusedComponent;
                      break;
                    }
                  }
                }
              }
            }
          }; 
            
          KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("focusOwner", 
              this.focusChangeListener);          
        }
      }
    }
  }

  private KeyListener specialKeysListener = new KeyAdapter() {
      public void keyPressed(KeyEvent ev) {
        // Temporarily toggle plan controller mode to panning mode when space bar is pressed  
        PlanController planController = controller.getPlanController();
        if (ev.getKeyCode() == KeyEvent.VK_SPACE 
            && (ev.getModifiers() & (KeyEvent.ALT_MASK | KeyEvent.CTRL_MASK | KeyEvent.META_MASK)) == 0
            && getActionMap().get(ActionType.PAN).getValue(Action.NAME) != null 
            && planController.getMode() != PlanController.Mode.PANNING
            && !planController.isModificationState()
            && SwingUtilities.isDescendingFrom(lastFocusedComponent, HomePane.this)
            && !isSpaceUsedByComponent(lastFocusedComponent)) {
          previousPlanControllerMode = planController.getMode();
          planController.setMode(PlanController.Mode.PANNING);
          ev.consume();
        }
      }
      
      private boolean isSpaceUsedByComponent(JComponent component) {
        return component instanceof JTextComponent
            || component instanceof JComboBox;
      }
    
      public void keyReleased(KeyEvent ev) {
        if (ev.getKeyCode() == KeyEvent.VK_SPACE 
            && previousPlanControllerMode != null) {
          controller.getPlanController().setMode(previousPlanControllerMode);
          previousPlanControllerMode = null;
          ev.consume();
        }
      }
      
      @Override
      public void keyTyped(KeyEvent ev) {
        // This listener manages accelerator keys that may require the use of shift key 
        // depending on keyboard layout (like + - or ?) 
        ActionMap actionMap = getActionMap();
        Action [] specialKeyActions = {actionMap.get(ActionType.ZOOM_IN), 
                                       actionMap.get(ActionType.ZOOM_OUT), 
                                       actionMap.get(ActionType.INCREASE_TEXT_SIZE), 
                                       actionMap.get(ActionType.DECREASE_TEXT_SIZE), 
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

  /**
   * Sets a focus traversal policy that ignores invisible split pane components.
   */
  private void updateFocusTraversalPolicy() {
    setFocusTraversalPolicy(new LayoutFocusTraversalPolicy() {
        @Override
        protected boolean accept(Component component) {
          if (super.accept(component)) {
            for (JSplitPane splitPane; 
                 (splitPane = (JSplitPane)SwingUtilities.getAncestorOfClass(JSplitPane.class, component)) != null; 
                 component = splitPane) {
              if (isChildComponentInvisible(splitPane, component)) {
                return false;                
              }                
            }
            return true;
          } else {
            return false;
          }
        }
      });
    setFocusTraversalPolicyProvider(true);
  }

  /**
   * Returns <code>true</code> if the top or the bottom component of the <code>splitPane</code> 
   * is a parent of the given child component and is too small enough to show it. 
   */
  private boolean isChildComponentInvisible(JSplitPane splitPane, Component childComponent) {
    return (SwingUtilities.isDescendingFrom(childComponent, splitPane.getTopComponent())
           && (splitPane.getTopComponent().getWidth() == 0
              || splitPane.getTopComponent().getHeight() == 0))
        || (SwingUtilities.isDescendingFrom(childComponent, splitPane.getBottomComponent())
           && (splitPane.getBottomComponent().getWidth() == 0
              || splitPane.getBottomComponent().getHeight() == 0));
  }

  /**
   * Returns the menu bar displayed in this pane.
   */
  private JMenuBar createMenuBar(final Home home, 
                                 UserPreferences preferences,
                                 final HomeController controller) {
    // Create File menu
    JMenu fileMenu = new JMenu(this.menuActionMap.get(MenuActionType.FILE_MENU));
    addActionToMenu(ActionType.NEW_HOME, fileMenu);
    addActionToMenu(ActionType.OPEN, fileMenu);
    
    final JMenu openRecentHomeMenu = 
        new JMenu(this.menuActionMap.get(MenuActionType.OPEN_RECENT_HOME_MENU));
    addActionToMenu(ActionType.DELETE_RECENT_HOMES, openRecentHomeMenu);
    openRecentHomeMenu.addMenuListener(new MenuListener() {
        public void menuSelected(MenuEvent ev) {
          updateOpenRecentHomeMenu(openRecentHomeMenu, controller);
        }
      
        public void menuCanceled(MenuEvent ev) {
        }
  
        public void menuDeselected(MenuEvent ev) {
        }
      });
    
    fileMenu.add(openRecentHomeMenu);
    fileMenu.addSeparator();
    addActionToMenu(ActionType.CLOSE, fileMenu);
    addActionToMenu(ActionType.SAVE, fileMenu);
    addActionToMenu(ActionType.SAVE_AS, fileMenu);
    addActionToMenu(ActionType.SAVE_AND_COMPRESS, fileMenu);
    fileMenu.addSeparator();
    addActionToMenu(ActionType.PAGE_SETUP, fileMenu);
    addActionToMenu(ActionType.PRINT_PREVIEW, fileMenu);
    addActionToMenu(ActionType.PRINT, fileMenu);
    // Don't add PRINT_TO_PDF, PREFERENCES and EXIT menu items under Mac OS X, 
    // because PREFERENCES and EXIT items are displayed in application menu
    // and PRINT_TO_PDF is available in standard Mac OS X Print dialog
    if (!OperatingSystem.isMacOSX()) {
      addActionToMenu(ActionType.PRINT_TO_PDF, fileMenu);
      fileMenu.addSeparator();
      addActionToMenu(ActionType.PREFERENCES, fileMenu);
    }

    // Create Edit menu
    JMenu editMenu = new JMenu(this.menuActionMap.get(MenuActionType.EDIT_MENU));
    addActionToMenu(ActionType.UNDO, editMenu);
    addActionToMenu(ActionType.REDO, editMenu);
    editMenu.addSeparator();
    addActionToMenu(ActionType.CUT, editMenu);
    addActionToMenu(ActionType.COPY, editMenu);
    addActionToMenu(ActionType.PASTE, editMenu);
    editMenu.addSeparator();
    addActionToMenu(ActionType.DELETE, editMenu);
    addActionToMenu(ActionType.SELECT_ALL, editMenu);

    // Create Furniture menu
    JMenu furnitureMenu = new JMenu(this.menuActionMap.get(MenuActionType.FURNITURE_MENU));
    addActionToMenu(ActionType.ADD_HOME_FURNITURE, furnitureMenu);
    addActionToMenu(ActionType.MODIFY_FURNITURE, furnitureMenu);
    addActionToMenu(ActionType.GROUP_FURNITURE, furnitureMenu);
    addActionToMenu(ActionType.UNGROUP_FURNITURE, furnitureMenu);
    furnitureMenu.addSeparator();
    addActionToMenu(ActionType.ALIGN_FURNITURE_ON_TOP, furnitureMenu);
    addActionToMenu(ActionType.ALIGN_FURNITURE_ON_BOTTOM, furnitureMenu);
    addActionToMenu(ActionType.ALIGN_FURNITURE_ON_LEFT, furnitureMenu);
    addActionToMenu(ActionType.ALIGN_FURNITURE_ON_RIGHT, furnitureMenu);
    furnitureMenu.addSeparator();
    addActionToMenu(ActionType.IMPORT_FURNITURE, furnitureMenu);
    addActionToMenu(ActionType.IMPORT_FURNITURE_LIBRARY, furnitureMenu);
    addActionToMenu(ActionType.IMPORT_TEXTURES_LIBRARY, furnitureMenu);
    furnitureMenu.addSeparator();
    furnitureMenu.add(createFurnitureSortMenu(home, preferences));
    furnitureMenu.add(createFurnitureDisplayPropertyMenu(home, preferences));
    
    // Create Plan menu
    JMenu planMenu = new JMenu(this.menuActionMap.get(MenuActionType.PLAN_MENU));
    addToggleActionToMenu(ActionType.SELECT, 
        this.selectToggleModel, true, planMenu);
    addToggleActionToMenu(ActionType.PAN, 
        this.panToggleModel, true, planMenu);
    addToggleActionToMenu(ActionType.CREATE_WALLS, 
        this.createWallsToggleModel, true, planMenu);
    addToggleActionToMenu(ActionType.CREATE_ROOMS, 
        this.createRoomsToggleModel, true, planMenu);
    addToggleActionToMenu(ActionType.CREATE_DIMENSION_LINES, 
        this.createDimensionLinesToggleModel, true, planMenu);
    addToggleActionToMenu(ActionType.CREATE_LABELS, 
        this.createLabelsToggleModel, true, planMenu);
    planMenu.addSeparator();
    JMenuItem lockUnlockBasePlanMenuItem = createLockUnlockBasePlanMenuItem(home, false);
    if (lockUnlockBasePlanMenuItem != null) {
      planMenu.add(lockUnlockBasePlanMenuItem);
    }
    addActionToMenu(ActionType.MODIFY_WALL, planMenu);
    addActionToMenu(ActionType.REVERSE_WALL_DIRECTION, planMenu);
    addActionToMenu(ActionType.SPLIT_WALL, planMenu);
    addActionToMenu(ActionType.MODIFY_ROOM, planMenu);
    addActionToMenu(ActionType.MODIFY_LABEL, planMenu);
    planMenu.add(createTextStyleMenu(home, preferences, false));
    planMenu.addSeparator();
    JMenuItem importModifyBackgroundImageMenuItem = createImportModifyBackgroundImageMenuItem(home, false);
    if (importModifyBackgroundImageMenuItem != null) {
      planMenu.add(importModifyBackgroundImageMenuItem);
    }
    JMenuItem hideShowBackgroundImageMenuItem = createHideShowBackgroundImageMenuItem(home, false);
    if (hideShowBackgroundImageMenuItem != null) {
      planMenu.add(hideShowBackgroundImageMenuItem);
    }
    addActionToMenu(ActionType.DELETE_BACKGROUND_IMAGE, planMenu);
    planMenu.addSeparator();
    addActionToMenu(ActionType.ZOOM_IN, planMenu);
    addActionToMenu(ActionType.ZOOM_OUT, planMenu);
    planMenu.addSeparator();
    addActionToMenu(ActionType.EXPORT_TO_SVG, planMenu);

    // Create 3D Preview menu
    JMenu preview3DMenu = new JMenu(this.menuActionMap.get(MenuActionType.VIEW_3D_MENU));
    addToggleActionToMenu(ActionType.VIEW_FROM_TOP, 
        this.viewFromTopToggleModel, true, preview3DMenu);
    addToggleActionToMenu(ActionType.VIEW_FROM_OBSERVER, 
        this.viewFromObserverToggleModel, true, preview3DMenu);
    preview3DMenu.addSeparator();
    JMenuItem attachDetach3DViewMenuItem = createAttachDetach3DViewMenuItem(controller, false);
    if (attachDetach3DViewMenuItem != null) {
      preview3DMenu.add(attachDetach3DViewMenuItem);
    }
    addActionToMenu(ActionType.MODIFY_3D_ATTRIBUTES, preview3DMenu);
    preview3DMenu.addSeparator();
    addActionToMenu(ActionType.CREATE_PHOTO, preview3DMenu);
    addActionToMenu(ActionType.CREATE_VIDEO, preview3DMenu);
    preview3DMenu.addSeparator();
    addActionToMenu(ActionType.EXPORT_TO_OBJ, preview3DMenu);
    
    // Create Help menu
    JMenu helpMenu = new JMenu(this.menuActionMap.get(MenuActionType.HELP_MENU));
    addActionToMenu(ActionType.HELP, helpMenu);      
    if (!OperatingSystem.isMacOSX()) {
      addActionToMenu(ActionType.ABOUT, helpMenu);      
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
      String pluginMenu = (String)pluginAction.getValue(PluginAction.Property.MENU.name());
      if (pluginMenu != null) {
        boolean pluginActionAdded = false;
        for (int i = 0; i < menuBar.getMenuCount(); i++) {
          JMenu menu = menuBar.getMenu(i);
          if (menu.getText().equals(pluginMenu)) {
            // Add menu item to existing menu
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

    // Add EXIT action at end to ensure it's the last item of file menu
    if (!OperatingSystem.isMacOSX()) {
      fileMenu.addSeparator();
      addActionToMenu(ActionType.EXIT, fileMenu);
    }

    removeUselessSeparatorsAndEmptyMenus(menuBar);    
    return menuBar;
  }

  /**
   * Adds the given action to <code>menu</code> and returns <code>true</code> if it was added.
   */
  private void addActionToMenu(ActionType actionType, JMenu menu) {
    addActionToMenu(actionType, false, menu);
  }

  /**
   * Adds the given action to <code>menu</code> and returns <code>true</code> if it was added.
   */
  private void addActionToMenu(ActionType actionType, 
                               boolean popup,
                               JMenu menu) {
    Action action = getActionMap().get(actionType);
    if (action.getValue(Action.NAME) != null) {
      menu.add(popup 
          ? new ResourceAction.PopupMenuItemAction(action)
          : new ResourceAction.MenuItemAction(action));
    }
  }

  /**
   * Adds to <code>menu</code> the menu item matching the given <code>actionType</code> 
   * and returns <code>true</code> if it was added.
   */
  private void addToggleActionToMenu(ActionType actionType,
                                     JToggleButton.ToggleButtonModel toggleButtonModel,
                                     boolean radioButton,
                                     JMenu menu) {
    addToggleActionToMenu(actionType, false, toggleButtonModel, radioButton, menu);
  }

  /**
   * Adds to <code>menu</code> the menu item matching the given <code>actionType</code> 
   * and returns <code>true</code> if it was added.
   */
  private void addToggleActionToMenu(ActionType actionType,
                                     boolean popup,
                                     JToggleButton.ToggleButtonModel toggleButtonModel,
                                     boolean radioButton,
                                     JMenu menu) {
    Action action = getActionMap().get(actionType);
    if (action.getValue(Action.NAME) != null) {
      menu.add(createToggleMenuItem(action, popup, toggleButtonModel, radioButton));
    }
  }

  /**
   * Creates a menu item for a toggle action.
   */
  private JMenuItem createToggleMenuItem(Action action, 
                                         boolean popup,
                                         JToggleButton.ToggleButtonModel toggleButtonModel,
                                         boolean radioButton) {
    JMenuItem menuItem;
    if (radioButton) {
      menuItem = new JRadioButtonMenuItem();
    } else {
      menuItem = new JCheckBoxMenuItem();
    }
    // Configure model
    menuItem.setModel(toggleButtonModel);
    // Configure menu item action after setting its model to avoid losing its mnemonic
    menuItem.setAction(popup
        ? new ResourceAction.PopupMenuItemAction(action)
        : new ResourceAction.MenuItemAction(action));
    return menuItem;
  }

  /**
   * Adds the given action to <code>menu</code>.
   */
  private void addActionToPopupMenu(ActionType actionType, JPopupMenu menu) {
    Action action = getActionMap().get(actionType);
    if (action.getValue(Action.NAME) != null) {
      menu.add(new ResourceAction.PopupMenuItemAction(action));
    }
  }

  /**
   * Adds to <code>menu</code> the menu item matching the given <code>actionType</code> 
   * and returns <code>true</code> if it was added.
   */
  private void addToggleActionToPopupMenu(ActionType actionType,
                                          JToggleButton.ToggleButtonModel toggleButtonModel,
                                          boolean radioButton,
                                          JPopupMenu menu) {
    Action action = getActionMap().get(actionType);
    if (action.getValue(Action.NAME) != null) {
      menu.add(createToggleMenuItem(action, true, toggleButtonModel, radioButton));
    }
  }

  /**
   * Removes the useless separators and empty menus among children of component.
   */
  private void removeUselessSeparatorsAndEmptyMenus(JComponent component) {
    for (int i = component.getComponentCount() - 1; i >= 0; i--) {
      Component child = component.getComponent(i);
      if (child instanceof JSeparator
          && (i == component.getComponentCount() - 1
              || component.getComponent(i - 1) instanceof JSeparator)) {
        component.remove(i);
      } else if (child instanceof JMenu) {
        removeUselessSeparatorsAndEmptyMenus(((JMenu)child).getPopupMenu());
      }
      if (child instanceof JMenu
          && (((JMenu)child).getMenuComponentCount() == 0
              || ((JMenu)child).getMenuComponentCount() == 1
                  && ((JMenu)child).getMenuComponent(0) instanceof JSeparator)) {
        component.remove(i);
      }
    }
    // Don't let a menu start with a separator
    if (component.getComponentCount() > 0 
        && component.getComponent(0) instanceof JSeparator) {
      component.remove(0);
    }
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
      addActionToMap(ActionType.SORT_HOME_FURNITURE_BY_CATALOG_ID, 
          sortActions, HomePieceOfFurniture.SortableProperty.CATALOG_ID); 
    }
    addActionToMap(ActionType.SORT_HOME_FURNITURE_BY_NAME, 
        sortActions, HomePieceOfFurniture.SortableProperty.NAME); 
    addActionToMap(ActionType.SORT_HOME_FURNITURE_BY_WIDTH, 
        sortActions, HomePieceOfFurniture.SortableProperty.WIDTH);
    addActionToMap(ActionType.SORT_HOME_FURNITURE_BY_DEPTH, 
        sortActions, HomePieceOfFurniture.SortableProperty.DEPTH);
    addActionToMap(ActionType.SORT_HOME_FURNITURE_BY_HEIGHT, 
        sortActions, HomePieceOfFurniture.SortableProperty.HEIGHT);
    addActionToMap(ActionType.SORT_HOME_FURNITURE_BY_X, 
        sortActions, HomePieceOfFurniture.SortableProperty.X);
    addActionToMap(ActionType.SORT_HOME_FURNITURE_BY_Y, 
        sortActions, HomePieceOfFurniture.SortableProperty.Y);
    addActionToMap(ActionType.SORT_HOME_FURNITURE_BY_ELEVATION, 
        sortActions, HomePieceOfFurniture.SortableProperty.ELEVATION);
    addActionToMap(ActionType.SORT_HOME_FURNITURE_BY_ANGLE, 
        sortActions, HomePieceOfFurniture.SortableProperty.ANGLE);
    addActionToMap(ActionType.SORT_HOME_FURNITURE_BY_COLOR, 
        sortActions, HomePieceOfFurniture.SortableProperty.COLOR);
    addActionToMap(ActionType.SORT_HOME_FURNITURE_BY_TEXTURE, 
        sortActions, HomePieceOfFurniture.SortableProperty.TEXTURE);
    addActionToMap(ActionType.SORT_HOME_FURNITURE_BY_MOVABILITY, 
        sortActions, HomePieceOfFurniture.SortableProperty.MOVABLE);
    addActionToMap(ActionType.SORT_HOME_FURNITURE_BY_TYPE, 
        sortActions, HomePieceOfFurniture.SortableProperty.DOOR_OR_WINDOW);
    addActionToMap(ActionType.SORT_HOME_FURNITURE_BY_VISIBILITY, 
        sortActions, HomePieceOfFurniture.SortableProperty.VISIBLE);
    // Use prices if currency isn't null
    if (preferences.getCurrency() != null) {
      addActionToMap(ActionType.SORT_HOME_FURNITURE_BY_PRICE, 
          sortActions, HomePieceOfFurniture.SortableProperty.PRICE);
      addActionToMap(ActionType.SORT_HOME_FURNITURE_BY_VALUE_ADDED_TAX_PERCENTAGE, 
          sortActions, HomePieceOfFurniture.SortableProperty.VALUE_ADDED_TAX_PERCENTAGE);
      addActionToMap(ActionType.SORT_HOME_FURNITURE_BY_VALUE_ADDED_TAX, 
          sortActions, HomePieceOfFurniture.SortableProperty.VALUE_ADDED_TAX);
      addActionToMap(ActionType.SORT_HOME_FURNITURE_BY_PRICE_VALUE_ADDED_TAX_INCLUDED, 
          sortActions, HomePieceOfFurniture.SortableProperty.PRICE_VALUE_ADDED_TAX_INCLUDED);
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
      sortMenuItem.setAction(new ResourceAction.MenuItemAction(sortAction));
      sortMenu.add(sortMenuItem);
      sortButtonGroup.add(sortMenuItem);
    }
    Action sortOrderAction = getActionMap().get(ActionType.SORT_HOME_FURNITURE_BY_DESCENDING_ORDER);
    if (sortOrderAction.getValue(Action.NAME) != null) {
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
      sortOrderCheckBoxMenuItem.setAction(new ResourceAction.MenuItemAction(sortOrderAction));
      sortMenu.add(sortOrderCheckBoxMenuItem);
    }
    return sortMenu;
  }
  
  /**
   * Adds to <code>actions</code> the action matching <code>actionType</code>.
   */
  private void addActionToMap(ActionType actionType,
                              Map<HomePieceOfFurniture.SortableProperty, Action> actions,
                              HomePieceOfFurniture.SortableProperty key) {
    Action action = getActionMap().get(actionType);
    if (action.getValue(Action.NAME) != null) {
      actions.put(key, action);
    }
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
      addActionToMap(ActionType.DISPLAY_HOME_FURNITURE_CATALOG_ID, 
          displayPropertyActions, HomePieceOfFurniture.SortableProperty.CATALOG_ID); 
    }
    addActionToMap(ActionType.DISPLAY_HOME_FURNITURE_NAME, 
        displayPropertyActions, HomePieceOfFurniture.SortableProperty.NAME); 
    addActionToMap(ActionType.DISPLAY_HOME_FURNITURE_WIDTH, 
        displayPropertyActions, HomePieceOfFurniture.SortableProperty.WIDTH);
    addActionToMap(ActionType.DISPLAY_HOME_FURNITURE_DEPTH, 
        displayPropertyActions, HomePieceOfFurniture.SortableProperty.DEPTH);
    addActionToMap(ActionType.DISPLAY_HOME_FURNITURE_HEIGHT, 
        displayPropertyActions, HomePieceOfFurniture.SortableProperty.HEIGHT);
    addActionToMap(ActionType.DISPLAY_HOME_FURNITURE_X, 
        displayPropertyActions, HomePieceOfFurniture.SortableProperty.X);
    addActionToMap(ActionType.DISPLAY_HOME_FURNITURE_Y, 
        displayPropertyActions, HomePieceOfFurniture.SortableProperty.Y);
    addActionToMap(ActionType.DISPLAY_HOME_FURNITURE_ELEVATION, 
        displayPropertyActions, HomePieceOfFurniture.SortableProperty.ELEVATION);
    addActionToMap(ActionType.DISPLAY_HOME_FURNITURE_ANGLE, 
        displayPropertyActions, HomePieceOfFurniture.SortableProperty.ANGLE);
    addActionToMap(ActionType.DISPLAY_HOME_FURNITURE_COLOR, 
        displayPropertyActions, HomePieceOfFurniture.SortableProperty.COLOR);
    addActionToMap(ActionType.DISPLAY_HOME_FURNITURE_TEXTURE, 
        displayPropertyActions, HomePieceOfFurniture.SortableProperty.TEXTURE);
    addActionToMap(ActionType.DISPLAY_HOME_FURNITURE_MOVABLE, 
        displayPropertyActions, HomePieceOfFurniture.SortableProperty.MOVABLE);
    addActionToMap(ActionType.DISPLAY_HOME_FURNITURE_DOOR_OR_WINDOW, 
        displayPropertyActions, HomePieceOfFurniture.SortableProperty.DOOR_OR_WINDOW);
    addActionToMap(ActionType.DISPLAY_HOME_FURNITURE_VISIBLE, 
        displayPropertyActions, HomePieceOfFurniture.SortableProperty.VISIBLE);
    // Use prices if currency isn't null
    if (preferences.getCurrency() != null) {
      addActionToMap(ActionType.DISPLAY_HOME_FURNITURE_PRICE, 
          displayPropertyActions, HomePieceOfFurniture.SortableProperty.PRICE);
      addActionToMap(ActionType.DISPLAY_HOME_FURNITURE_VALUE_ADDED_TAX_PERCENTAGE, 
          displayPropertyActions, HomePieceOfFurniture.SortableProperty.VALUE_ADDED_TAX_PERCENTAGE);
      addActionToMap(ActionType.DISPLAY_HOME_FURNITURE_VALUE_ADDED_TAX, 
          displayPropertyActions, HomePieceOfFurniture.SortableProperty.VALUE_ADDED_TAX);
      addActionToMap(ActionType.DISPLAY_HOME_FURNITURE_PRICE_VALUE_ADDED_TAX_INCLUDED, 
          displayPropertyActions, HomePieceOfFurniture.SortableProperty.PRICE_VALUE_ADDED_TAX_INCLUDED);
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
   * Returns Lock / Unlock base plan menu item.
   */
  private JMenuItem createLockUnlockBasePlanMenuItem(final Home home, 
                                                       final boolean popup) {
    ActionMap actionMap = getActionMap();
    final Action unlockBasePlanAction = actionMap.get(ActionType.UNLOCK_BASE_PLAN);
    final Action lockBasePlanAction = actionMap.get(ActionType.LOCK_BASE_PLAN);
    if (unlockBasePlanAction.getValue(Action.NAME) != null
        && lockBasePlanAction.getValue(Action.NAME) != null) {
      final JMenuItem lockUnlockBasePlanMenuItem = new JMenuItem(
          createLockUnlockBasePlanAction(home, popup));
      // Add a listener to home on basePlanLocked property change to 
      // switch action according to basePlanLocked change
      home.addPropertyChangeListener(Home.Property.BASE_PLAN_LOCKED, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              lockUnlockBasePlanMenuItem.setAction(
                  createLockUnlockBasePlanAction(home, popup));
            }
          });    
      return lockUnlockBasePlanMenuItem;
    } else {
      return null;
    }
  }
  
  /**
   * Returns the action active on Lock / Unlock base plan menu item.
   */
  private Action createLockUnlockBasePlanAction(Home home, boolean popup) {
    ActionType actionType = home.isBasePlanLocked() 
        ? ActionType.UNLOCK_BASE_PLAN
        : ActionType.LOCK_BASE_PLAN;
    Action action = getActionMap().get(actionType);
    return popup 
        ? new ResourceAction.PopupMenuItemAction(action)
        : new ResourceAction.MenuItemAction(action);
  }

  /**
   * Returns Lock / Unlock base plan button.
   */
  private JComponent createLockUnlockBasePlanButton(final Home home) {
    ActionMap actionMap = getActionMap();
    final Action unlockBasePlanAction = actionMap.get(ActionType.UNLOCK_BASE_PLAN);
    final Action lockBasePlanAction = actionMap.get(ActionType.LOCK_BASE_PLAN);
    if (unlockBasePlanAction.getValue(Action.NAME) != null
        && lockBasePlanAction.getValue(Action.NAME) != null) {
      final JButton lockUnlockBasePlanButton = new JButton(
          new ResourceAction.ToolBarAction(home.isBasePlanLocked() 
              ? unlockBasePlanAction
              : lockBasePlanAction));
      lockUnlockBasePlanButton.setBorderPainted(false);
      lockUnlockBasePlanButton.setContentAreaFilled(false);
      lockUnlockBasePlanButton.setFocusable(false);
      // Add a listener to home on basePlanLocked property change to 
      // switch action according to basePlanLocked change
      home.addPropertyChangeListener(Home.Property.BASE_PLAN_LOCKED, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              lockUnlockBasePlanButton.setAction(
                  new ResourceAction.ToolBarAction(home.isBasePlanLocked() 
                      ? unlockBasePlanAction
                      : lockBasePlanAction));
            }
          });    
      return lockUnlockBasePlanButton;
    } else {
      return null;
    }
  }
  
  /**
   * Returns text style menu.
   */
  private JMenu createTextStyleMenu(final Home home,
                                    final UserPreferences preferences,
                                    boolean popup) {
    JMenu modifyTextStyleMenu = new JMenu(this.menuActionMap.get(MenuActionType.MODIFY_TEXT_STYLE));
    
    addActionToMenu(ActionType.INCREASE_TEXT_SIZE, popup, modifyTextStyleMenu);
    addActionToMenu(ActionType.DECREASE_TEXT_SIZE, popup, modifyTextStyleMenu);
    modifyTextStyleMenu.addSeparator();
    addToggleActionToMenu(ActionType.TOGGLE_BOLD_STYLE, popup, 
        this.boldStyleToggleModel, false, modifyTextStyleMenu);
    addToggleActionToMenu(ActionType.TOGGLE_ITALIC_STYLE, popup, 
        this.italicStyleToggleModel, false, modifyTextStyleMenu);
    return modifyTextStyleMenu;
  }

  /**
   * Creates a toggle button model that is selected when all the text of the 
   * selected items in <code>home</code> use bold style.  
   */
  private JToggleButton.ToggleButtonModel createBoldStyleToggleModel(final Home home, 
                                                                     final UserPreferences preferences) {
    return new JToggleButton.ToggleButtonModel() {
      {
        home.addSelectionListener(new SelectionListener() {
          public void selectionChanged(SelectionEvent ev) {
            fireStateChanged();
          }
        });
      }
      
      @Override
      public boolean isSelected() {
        // Find if selected items are all bold or not
        Boolean selectionBoldStyle = null;
        for (Selectable item : home.getSelectedItems()) {
          Boolean bold;
          if (item instanceof Label) {
            bold = isItemTextBold(item, ((Label)item).getStyle());
          } else if (item instanceof HomePieceOfFurniture
              && ((HomePieceOfFurniture)item).isVisible()) {
            bold = isItemTextBold(item, ((HomePieceOfFurniture)item).getNameStyle());
          } else if (item instanceof Room) {
            Room room = (Room)item;
            bold = isItemTextBold(room, room.getNameStyle());
            if (bold != isItemTextBold(room, room.getAreaStyle())) {
              bold = null;
            }
          } else if (item instanceof DimensionLine) {
            bold = isItemTextBold(item, ((DimensionLine)item).getLengthStyle());
          } else {
            continue;
          }
          if (selectionBoldStyle == null) {
            selectionBoldStyle = bold;
          } else if (bold == null || !selectionBoldStyle.equals(bold)) {
            selectionBoldStyle = null;
            break;
          }
        }
        return selectionBoldStyle != null && selectionBoldStyle;
      }
      
      private boolean isItemTextBold(Selectable item, TextStyle textStyle) {
        if (textStyle == null) {
          textStyle = preferences.getDefaultTextStyle(item.getClass());              
        }
        
        return textStyle.isBold();
      }        
    };
  }

  /**
   * Creates a toggle button model that is selected when all the text of the 
   * selected items in <code>home</code> use italic style.  
   */
  private JToggleButton.ToggleButtonModel createItalicStyleToggleModel(final Home home,
                                                                       final UserPreferences preferences) {
    return new JToggleButton.ToggleButtonModel() {
      {
        home.addSelectionListener(new SelectionListener() {
          public void selectionChanged(SelectionEvent ev) {
            fireStateChanged();
          }
        });
      }
      
      @Override
      public boolean isSelected() {
        // Find if selected items are all italic or not
        Boolean selectionItalicStyle = null;
        for (Selectable item : home.getSelectedItems()) {
          Boolean italic;
          if (item instanceof Label) {
            italic = isItemTextItalic(item, ((Label)item).getStyle());
          } else if (item instanceof HomePieceOfFurniture
              && ((HomePieceOfFurniture)item).isVisible()) {
            italic = isItemTextItalic(item, ((HomePieceOfFurniture)item).getNameStyle());
          } else if (item instanceof Room) {
            Room room = (Room)item;
            italic = isItemTextItalic(room, room.getNameStyle());
            if (italic != isItemTextItalic(room, room.getAreaStyle())) {
              italic = null;
            }
          } else if (item instanceof DimensionLine) {
            italic = isItemTextItalic(item, ((DimensionLine)item).getLengthStyle());
          } else {
            continue;
          }
          if (selectionItalicStyle == null) {
            selectionItalicStyle = italic;
          } else if (italic == null || !selectionItalicStyle.equals(italic)) {
            selectionItalicStyle = null;
            break;
          }
        }
        return selectionItalicStyle != null && selectionItalicStyle;
      }
      
      private boolean isItemTextItalic(Selectable item, TextStyle textStyle) {
        if (textStyle == null) {
          textStyle = preferences.getDefaultTextStyle(item.getClass());              
        }          
        return textStyle.isItalic();
      }
    };
  }
  
  /**
   * Returns Import / Modify background image menu item.
   */
  private JMenuItem createImportModifyBackgroundImageMenuItem(final Home home, 
                                                                final boolean popup) {
    ActionMap actionMap = getActionMap();
    Action importBackgroundImageAction = actionMap.get(ActionType.IMPORT_BACKGROUND_IMAGE);
    Action modifyBackgroundImageAction = actionMap.get(ActionType.MODIFY_BACKGROUND_IMAGE);
    if (importBackgroundImageAction.getValue(Action.NAME) != null
        && modifyBackgroundImageAction.getValue(Action.NAME) != null) {
      final JMenuItem importModifyBackgroundImageMenuItem = new JMenuItem(
          createImportModifyBackgroundImageAction(home, popup));
      // Add a listener to home on backgroundImage property change to 
      // switch action according to backgroundImage change
      home.addPropertyChangeListener(Home.Property.BACKGROUND_IMAGE, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              importModifyBackgroundImageMenuItem.setAction(
                  createImportModifyBackgroundImageAction(home, popup));
            }
          });    
      return importModifyBackgroundImageMenuItem;
    } else {
      return null;
    }
  }
  
  /**
   * Returns the action active on Import / Modify menu item.
   */
  private Action createImportModifyBackgroundImageAction(Home home, boolean popup) {
    ActionType backgroundImageActionType = home.getBackgroundImage() == null 
        ? ActionType.IMPORT_BACKGROUND_IMAGE
        : ActionType.MODIFY_BACKGROUND_IMAGE;
    Action backgroundImageAction = getActionMap().get(backgroundImageActionType);
    return popup 
        ? new ResourceAction.PopupMenuItemAction(backgroundImageAction)
        : new ResourceAction.MenuItemAction(backgroundImageAction);
  }
  
  /**
   * Returns Hide / Show background image menu item.
   */
  private JMenuItem createHideShowBackgroundImageMenuItem(final Home home, 
                                                            final boolean popup) {
    ActionMap actionMap = getActionMap();
    Action hideBackgroundImageAction = actionMap.get(ActionType.HIDE_BACKGROUND_IMAGE);
    Action showBackgroundImageAction = actionMap.get(ActionType.SHOW_BACKGROUND_IMAGE);
    if (hideBackgroundImageAction.getValue(Action.NAME) != null
        && showBackgroundImageAction.getValue(Action.NAME) != null) {
      final JMenuItem hideShowBackgroundImageMenuItem = new JMenuItem(
          createHideShowBackgroundImageAction(home, popup));
      // Add a listener to home on backgroundImage property change to 
      // switch action according to backgroundImage change
      home.addPropertyChangeListener(Home.Property.BACKGROUND_IMAGE, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              hideShowBackgroundImageMenuItem.setAction(
                  createHideShowBackgroundImageAction(home, popup));
            }
          });    
      return hideShowBackgroundImageMenuItem;
    } else {
      return null;
    }
  }
  
  /**
   * Returns the action active on Hide / Show menu item.
   */
  private Action createHideShowBackgroundImageAction(Home home, boolean popup) {
    BackgroundImage backgroundImage = home.getBackgroundImage();
    ActionType backgroundImageActionType = backgroundImage == null || backgroundImage.isVisible()        
        ? ActionType.HIDE_BACKGROUND_IMAGE
        : ActionType.SHOW_BACKGROUND_IMAGE;
    Action backgroundImageAction = getActionMap().get(backgroundImageActionType);
    return popup 
        ? new ResourceAction.PopupMenuItemAction(backgroundImageAction)
        : new ResourceAction.MenuItemAction(backgroundImageAction);
  }
  
  /**
   * Returns Attach / Detach menu item for the 3D view.
   */
  private JMenuItem createAttachDetach3DViewMenuItem(final HomeController controller, 
                                                                      final boolean popup) {
    ActionMap actionMap = getActionMap();
    Action display3DViewInSeparateWindowAction = actionMap.get(ActionType.DETACH_3D_VIEW);
    Action display3DViewInMainWindowAction = actionMap.get(ActionType.ATTACH_3D_VIEW);
    if (display3DViewInSeparateWindowAction.getValue(Action.NAME) != null
        && display3DViewInMainWindowAction.getValue(Action.NAME) != null) {
      final JMenuItem attachDetach3DViewMenuItem = new JMenuItem(
          createAttachDetach3DViewAction(controller, popup));
      // Add a listener to 3D view to switch action when its parent changes
      ((JComponent)controller.getHomeController3D().getView()).addAncestorListener(new AncestorListener() {        
          public void ancestorAdded(AncestorEvent ev) {
            attachDetach3DViewMenuItem.setAction(
                createAttachDetach3DViewAction(controller, popup));
          }
          
          public void ancestorRemoved(AncestorEvent ev) {
          }
          
          public void ancestorMoved(AncestorEvent ev) {
          }        
        });    
      return attachDetach3DViewMenuItem;
    } else {
      return null;
    }
  }
  
  /**
   * Returns the action Attach / Detach menu item.
   */
  private Action createAttachDetach3DViewAction(HomeController controller, boolean popup) {
    JRootPane view3DRootPane = SwingUtilities.getRootPane((JComponent)controller.getHomeController3D().getView());
    ActionType display3DViewActionType = view3DRootPane == this        
        ? ActionType.DETACH_3D_VIEW
        : ActionType.ATTACH_3D_VIEW;
    Action backgroundImageAction = getActionMap().get(display3DViewActionType);
    return popup 
        ? new ResourceAction.PopupMenuItemAction(backgroundImageAction)
        : new ResourceAction.MenuItemAction(backgroundImageAction);
  }
  
  /**
   * Updates <code>openRecentHomeMenu</code> from current recent homes in preferences.
   */
  protected void updateOpenRecentHomeMenu(JMenu openRecentHomeMenu, 
                                          final HomeController controller) {
    openRecentHomeMenu.removeAll();
    for (final String homeName : controller.getRecentHomes()) {
      openRecentHomeMenu.add(
          new AbstractAction(controller.getContentManager().getPresentationName(
                  homeName, ContentManager.ContentType.SWEET_HOME_3D)) {
            public void actionPerformed(ActionEvent e) {
              controller.open(homeName);
            }
          });
    }
    if (openRecentHomeMenu.getMenuComponentCount() > 0) {
      openRecentHomeMenu.addSeparator();
    }
    addActionToMenu(ActionType.DELETE_RECENT_HOMES, openRecentHomeMenu);
  }

  /**
   * Returns the tool bar displayed in this pane.
   */
  private JToolBar createToolBar(Home home) {
    final JToolBar toolBar = new UnfocusableToolBar();
    addActionToToolBar(ActionType.NEW_HOME, toolBar);
    addActionToToolBar(ActionType.OPEN, toolBar);
    addActionToToolBar(ActionType.SAVE, toolBar);
    toolBar.addSeparator();

    addActionToToolBar(ActionType.UNDO, toolBar);
    addActionToToolBar(ActionType.REDO, toolBar);
    toolBar.add(Box.createRigidArea(new Dimension(2, 2)));
    addActionToToolBar(ActionType.CUT, toolBar);
    addActionToToolBar(ActionType.COPY, toolBar);
    addActionToToolBar(ActionType.PASTE, toolBar);
    toolBar.addSeparator();

    addActionToToolBar(ActionType.ADD_HOME_FURNITURE, toolBar);
    toolBar.addSeparator();
   
    addToggleActionToToolBar(ActionType.SELECT, this.selectToggleModel, toolBar);
    addToggleActionToToolBar(ActionType.PAN, this.panToggleModel, toolBar);
    addToggleActionToToolBar(ActionType.CREATE_WALLS, this.createWallsToggleModel, toolBar);
    addToggleActionToToolBar(ActionType.CREATE_ROOMS, this.createRoomsToggleModel, toolBar);
    addToggleActionToToolBar(ActionType.CREATE_DIMENSION_LINES, this.createDimensionLinesToggleModel, toolBar);
    addToggleActionToToolBar(ActionType.CREATE_LABELS, this.createLabelsToggleModel, toolBar);
    toolBar.add(Box.createRigidArea(new Dimension(2, 2)));
    
    addActionToToolBar(ActionType.INCREASE_TEXT_SIZE, toolBar);
    addActionToToolBar(ActionType.DECREASE_TEXT_SIZE, toolBar);
    addToggleActionToToolBar(ActionType.TOGGLE_BOLD_STYLE, this.boldStyleToggleModel, toolBar);
    addToggleActionToToolBar(ActionType.TOGGLE_ITALIC_STYLE, this.italicStyleToggleModel, toolBar);
    toolBar.add(Box.createRigidArea(new Dimension(2, 2)));
    
    addActionToToolBar(ActionType.ZOOM_IN, toolBar);
    addActionToToolBar(ActionType.ZOOM_OUT, toolBar);
    toolBar.addSeparator();
    addActionToToolBar(ActionType.CREATE_PHOTO, toolBar);
    addActionToToolBar(ActionType.CREATE_VIDEO, toolBar);
    toolBar.addSeparator();
    
    // Add plugin actions buttons
    boolean pluginActionsAdded = false;
    for (Action pluginAction : this.pluginActions) {
      if (Boolean.TRUE.equals(pluginAction.getValue(PluginAction.Property.TOOL_BAR.name()))) {
        toolBar.add(new ResourceAction.ToolBarAction(pluginAction));
        pluginActionsAdded = true;
      }
    }
    if (pluginActionsAdded) {
      toolBar.addSeparator();
    }
    
    addActionToToolBar(ActionType.HELP, toolBar);

    // Remove useless separators 
    for (int i = toolBar.getComponentCount() - 1; i > 0; i--) {
      Component child = toolBar.getComponent(i);
      if (child instanceof JSeparator
          && (i == toolBar.getComponentCount() - 1
              || toolBar.getComponent(i - 1) instanceof JSeparator)) {
        toolBar.remove(i);
      } 
    }

    return toolBar;
  }

  /**
   * Adds to tool bar the button matching the given <code>actionType</code> 
   * and returns <code>true</code> if it was added.
   */
  private void addToggleActionToToolBar(ActionType actionType,
                                        JToggleButton.ToggleButtonModel toggleButtonModel,
                                        JToolBar toolBar) {
    Action action = getActionMap().get(actionType);
    if (action.getValue(Action.NAME) != null) {
      Action toolBarAction = new ResourceAction.ToolBarAction(action);    
      JToggleButton toggleButton = new JToggleButton(toolBarAction);
      toggleButton.setModel(toggleButtonModel);
      toolBar.add(toggleButton);
    }
  }

  /**
   * Adds to tool bar the button matching the given <code>actionType</code>. 
   */
  private void addActionToToolBar(ActionType actionType,
                                  JToolBar toolBar) {
    Action action = getActionMap().get(actionType);
    if (action.getValue(Action.NAME) != null) {
      toolBar.add(new ResourceAction.ToolBarAction(action));
    }
  }
    
  /**
   * Enables or disables the action matching <code>actionType</code>.
   */
  public void setEnabled(ActionType actionType, 
                         boolean enabled) {
    Action action = getActionMap().get(actionType);
    if (action != null) {
      action.setEnabled(enabled);
    }
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
    if (action != null) {
      if (name == null) {
        name = (String)action.getValue(Action.DEFAULT);
      }
      action.putValue(Action.NAME, name);
      action.putValue(Action.SHORT_DESCRIPTION, name);
    }
  }

  /**
   * Enables or disables transfer between components.  
   */
  public void setTransferEnabled(boolean enabled) {
    JComponent catalogView = (JComponent)this.controller.getFurnitureCatalogController().getView();
    JComponent furnitureView = (JComponent)this.controller.getFurnitureController().getView();
    JComponent planView = (JComponent)this.controller.getPlanController().getView();
    if (enabled) {
      catalogView.setTransferHandler(this.catalogTransferHandler);
      furnitureView.setTransferHandler(this.furnitureTransferHandler);
      if (furnitureView instanceof Scrollable) {
        ((JViewport)furnitureView.getParent()).setTransferHandler(this.furnitureTransferHandler);
      }
      planView.setTransferHandler(this.planTransferHandler);
    } else {
      catalogView.setTransferHandler(null);
      furnitureView.setTransferHandler(null);
      if (furnitureView instanceof Scrollable) {
        ((JViewport)furnitureView.getParent()).setTransferHandler(null);
      }
      planView.setTransferHandler(null);
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
    // Set default divider location
    mainPane.setDividerLocation(360);
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
          public void propertyChange(final PropertyChangeEvent ev) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                  Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                  if (focusOwner != null && isChildComponentInvisible(splitPane, focusOwner)) {
                    FocusTraversalPolicy focusTraversalPolicy = getFocusTraversalPolicy();              
                    Component focusedComponent = focusTraversalPolicy.getComponentAfter(HomePane.this, focusOwner);
                    if (focusedComponent == null) {
                      focusedComponent = focusTraversalPolicy.getComponentBefore(HomePane.this, focusOwner);
                    }                
                    focusedComponent.requestFocusInWindow();              
                  }
                  controller.setVisualProperty(dividerLocationProperty, ev.getNewValue());
                }
              });
          }
        });
  }

  /**
   * Returns the catalog tree and furniture table pane. 
   */
  private JComponent createCatalogFurniturePane(Home home,
                                                UserPreferences preferences,
                                                final HomeController controller) {
    JComponent catalogView = (JComponent)controller.getFurnitureCatalogController().getView();
    
    // Create catalog view popup menu
    JPopupMenu catalogViewPopup = new JPopupMenu();
    addActionToPopupMenu(ActionType.COPY, catalogViewPopup);
    catalogViewPopup.addSeparator();
    addActionToPopupMenu(ActionType.DELETE, catalogViewPopup);
    catalogViewPopup.addSeparator();
    addActionToPopupMenu(ActionType.ADD_HOME_FURNITURE, catalogViewPopup);
    addActionToPopupMenu(ActionType.MODIFY_FURNITURE, catalogViewPopup);
    catalogViewPopup.addSeparator();
    addActionToPopupMenu(ActionType.IMPORT_FURNITURE, catalogViewPopup);
    catalogViewPopup.addPopupMenuListener(new MenuItemsVisibilityListener());
    catalogView.setComponentPopupMenu(catalogViewPopup);

    preferences.addPropertyChangeListener(UserPreferences.Property.FURNITURE_CATALOG_VIEWED_IN_TREE, 
        new FurnitureCatalogViewChangeListener(this, catalogView));
    if (catalogView instanceof Scrollable) {
      catalogView = new HomeScrollPane(catalogView);
    }
    
    // Configure furniture view
    JComponent furnitureView = (JComponent)controller.getFurnitureController().getView();
    // Set default traversal keys of furniture view
    KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
    furnitureView.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
        focusManager.getDefaultFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
    furnitureView.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
        focusManager.getDefaultFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));

    // Create furniture view popup menu
    JPopupMenu furnitureViewPopup = new JPopupMenu();
    addActionToPopupMenu(ActionType.UNDO, furnitureViewPopup);
    addActionToPopupMenu(ActionType.REDO, furnitureViewPopup);
    furnitureViewPopup.addSeparator();
    addActionToPopupMenu(ActionType.CUT, furnitureViewPopup);
    addActionToPopupMenu(ActionType.COPY, furnitureViewPopup);
    addActionToPopupMenu(ActionType.PASTE, furnitureViewPopup);
    furnitureViewPopup.addSeparator();
    addActionToPopupMenu(ActionType.DELETE, furnitureViewPopup);
    addActionToPopupMenu(ActionType.SELECT_ALL, furnitureViewPopup);
    furnitureViewPopup.addSeparator();
    addActionToPopupMenu(ActionType.MODIFY_FURNITURE, furnitureViewPopup);
    addActionToPopupMenu(ActionType.GROUP_FURNITURE, furnitureViewPopup);
    addActionToPopupMenu(ActionType.UNGROUP_FURNITURE, furnitureViewPopup);
    furnitureViewPopup.addSeparator();
    furnitureViewPopup.add(createFurnitureSortMenu(home, preferences));
    furnitureViewPopup.add(createFurnitureDisplayPropertyMenu(home, preferences));
    furnitureViewPopup.addPopupMenuListener(new MenuItemsVisibilityListener());
    furnitureView.setComponentPopupMenu(furnitureViewPopup);

    if (furnitureView instanceof Scrollable) {
      JScrollPane furnitureScrollPane = new HomeScrollPane(furnitureView);
      // Add a mouse listener that gives focus to furniture view when
      // user clicks in its viewport (tables don't spread vertically if their row count is too small)
      final JViewport viewport = furnitureScrollPane.getViewport();
      viewport.addMouseListener(
          new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent ev) {
              viewport.getView().requestFocusInWindow();
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
      ((JViewport)furnitureView.getParent()).setComponentPopupMenu(furnitureViewPopup);
      furnitureView = furnitureScrollPane;
    }    

    // Create a split pane that displays both components
    JSplitPane catalogFurniturePane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
        catalogView, furnitureView);
    configureSplitPane(catalogFurniturePane, home, 
        CATALOG_PANE_DIVIDER_LOCATION_VISUAL_PROPERTY, 0.5, controller);
    return catalogFurniturePane;
  }

  /**
   * Preferences property listener bound to this component with a weak reference to avoid
   * strong link between preferences and this component.  
   */
  private static class FurnitureCatalogViewChangeListener implements PropertyChangeListener {
    private WeakReference<HomePane>   homePane;
    private WeakReference<JComponent> furnitureCatalogView;

    public FurnitureCatalogViewChangeListener(HomePane homePane, JComponent furnitureCatalogView) {
      this.homePane = new WeakReference<HomePane>(homePane);
      this.furnitureCatalogView = new WeakReference<JComponent>(furnitureCatalogView);
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
      // If home pane was garbage collected, remove this listener from preferences
      HomePane homePane = this.homePane.get();
      if (homePane == null) {
        ((UserPreferences)ev.getSource()).removePropertyChangeListener(
            UserPreferences.Property.FURNITURE_CATALOG_VIEWED_IN_TREE, this);
      } else {
        // Replace previous furniture catalog view by the new one
        JComponent oldFurnitureCatalogView = this.furnitureCatalogView.get();        
        JComponent newFurnitureCatalogView = (JComponent)homePane.controller.getFurnitureCatalogController().getView();
        newFurnitureCatalogView.setComponentPopupMenu(oldFurnitureCatalogView.getComponentPopupMenu());
        TransferHandler transferHandler = oldFurnitureCatalogView.getTransferHandler();
        newFurnitureCatalogView.setTransferHandler(transferHandler);
        JComponent splitPaneTopComponent = newFurnitureCatalogView; 
        if (newFurnitureCatalogView instanceof Scrollable) {
          splitPaneTopComponent = new HomeScrollPane(newFurnitureCatalogView);
        } else {
          splitPaneTopComponent = newFurnitureCatalogView;
        }
        ((JSplitPane)SwingUtilities.getAncestorOfClass(JSplitPane.class, oldFurnitureCatalogView)).
            setTopComponent(splitPaneTopComponent);
        this.furnitureCatalogView = new WeakReference<JComponent>(newFurnitureCatalogView);
      }
    }
  }
  
  /**
   * Returns the plan view and 3D view pane. 
   */
  private JComponent createPlanView3DPane(final Home home, UserPreferences preferences, 
                                          final HomeController controller) {
    JComponent planView = (JComponent)controller.getPlanController().getView();

    // Create plan view popup menu
    JPopupMenu planViewPopup = new JPopupMenu();
    addActionToPopupMenu(ActionType.UNDO, planViewPopup);
    addActionToPopupMenu(ActionType.REDO, planViewPopup);
    planViewPopup.addSeparator();
    addActionToPopupMenu(ActionType.CUT, planViewPopup);
    addActionToPopupMenu(ActionType.COPY, planViewPopup);
    addActionToPopupMenu(ActionType.PASTE, planViewPopup);
    planViewPopup.addSeparator();
    addActionToPopupMenu(ActionType.DELETE, planViewPopup);
    addActionToPopupMenu(ActionType.SELECT_ALL, planViewPopup);
    planViewPopup.addSeparator();
    addToggleActionToPopupMenu(ActionType.SELECT, 
        this.selectToggleModel, true, planViewPopup);
    addToggleActionToPopupMenu(ActionType.PAN, 
        this.panToggleModel, true, planViewPopup);
    addToggleActionToPopupMenu(ActionType.CREATE_WALLS, 
        this.createWallsToggleModel, true, planViewPopup);
    addToggleActionToPopupMenu(ActionType.CREATE_ROOMS, 
        this.createRoomsToggleModel, true, planViewPopup);
    addToggleActionToPopupMenu(ActionType.CREATE_DIMENSION_LINES, 
        this.createDimensionLinesToggleModel, true, planViewPopup);
    addToggleActionToPopupMenu(ActionType.CREATE_LABELS, 
        this.createLabelsToggleModel, true, planViewPopup);
    planViewPopup.addSeparator();
    JMenuItem lockUnlockBasePlanMenuItem = createLockUnlockBasePlanMenuItem(home, true);
    if (lockUnlockBasePlanMenuItem != null) {
      planViewPopup.add(lockUnlockBasePlanMenuItem);
    }
    addActionToPopupMenu(ActionType.MODIFY_FURNITURE, planViewPopup);
    addActionToPopupMenu(ActionType.GROUP_FURNITURE, planViewPopup);
    addActionToPopupMenu(ActionType.UNGROUP_FURNITURE, planViewPopup);
    addActionToPopupMenu(ActionType.MODIFY_WALL, planViewPopup);
    addActionToPopupMenu(ActionType.REVERSE_WALL_DIRECTION, planViewPopup);
    addActionToPopupMenu(ActionType.SPLIT_WALL, planViewPopup);
    addActionToPopupMenu(ActionType.MODIFY_ROOM, planViewPopup);
    addActionToPopupMenu(ActionType.MODIFY_LABEL, planViewPopup);
    planViewPopup.add(createTextStyleMenu(home, preferences, true));
    planViewPopup.addSeparator();
    JMenuItem importModifyBackgroundImageMenuItem = createImportModifyBackgroundImageMenuItem(home, true);
    if (importModifyBackgroundImageMenuItem != null) {
      planViewPopup.add(importModifyBackgroundImageMenuItem);
    }
    JMenuItem hideShowBackgroundImageMenuItem = createHideShowBackgroundImageMenuItem(home, true);
    if (hideShowBackgroundImageMenuItem != null) {
      planViewPopup.add(hideShowBackgroundImageMenuItem);
    }
    addActionToPopupMenu(ActionType.DELETE_BACKGROUND_IMAGE, planViewPopup);
    planViewPopup.addSeparator();
    addActionToPopupMenu(ActionType.ZOOM_OUT, planViewPopup);
    addActionToPopupMenu(ActionType.ZOOM_IN, planViewPopup);
    planViewPopup.addSeparator();
    addActionToPopupMenu(ActionType.EXPORT_TO_SVG, planViewPopup);
    planViewPopup.addPopupMenuListener(new MenuItemsVisibilityListener());
    planView.setComponentPopupMenu(planViewPopup);
    
    if (planView instanceof Scrollable) {
      JScrollPane planScrollPane = new HomeScrollPane(planView);
      setPlanRulersVisible(planScrollPane, controller, preferences.isRulersVisible());
      JComponent lockUnlockBasePlanButton = createLockUnlockBasePlanButton(home);
      if (lockUnlockBasePlanButton != null) {
        planScrollPane.setCorner(JScrollPane.UPPER_LEADING_CORNER, 
            lockUnlockBasePlanButton);
      }
      // Add a listener to update rulers visibility in preferences
      preferences.addPropertyChangeListener(UserPreferences.Property.RULERS_VISIBLE, 
          new RulersVisibilityChangeListener(this, planScrollPane, controller));
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
      planView = planScrollPane;
    }

    // Configure 3D view
    final JComponent view3D = (JComponent)controller.getHomeController3D().getView();
    view3D.setPreferredSize(planView.getPreferredSize());
    view3D.setMinimumSize(new Dimension(0, 0));
    
    // Create 3D view popup menu
    JPopupMenu view3DPopup = new JPopupMenu();
    addToggleActionToPopupMenu(ActionType.VIEW_FROM_TOP, 
        this.viewFromTopToggleModel, true, view3DPopup);
    addToggleActionToPopupMenu(ActionType.VIEW_FROM_OBSERVER, 
        this.viewFromObserverToggleModel, true, view3DPopup);
    view3DPopup.addSeparator();
    JMenuItem attachDetach3DViewMenuItem = createAttachDetach3DViewMenuItem(controller, true);
    if (attachDetach3DViewMenuItem != null) {
      view3DPopup.add(attachDetach3DViewMenuItem);
    }
    addActionToPopupMenu(ActionType.MODIFY_3D_ATTRIBUTES, view3DPopup);
    view3DPopup.addSeparator();
    addActionToPopupMenu(ActionType.CREATE_PHOTO, view3DPopup);
    addActionToPopupMenu(ActionType.CREATE_VIDEO, view3DPopup);
    view3DPopup.addSeparator();
    addActionToPopupMenu(ActionType.EXPORT_TO_OBJ, view3DPopup);
    view3DPopup.addPopupMenuListener(new MenuItemsVisibilityListener());
    view3D.setComponentPopupMenu(view3DPopup);
    
    JComponent component3D = view3D;
    if (view3D instanceof Scrollable) {
      component3D = new HomeScrollPane(planView);
    } 
    
    // Create a split pane that displays both components
    JSplitPane planView3DPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, planView, component3D);
    configureSplitPane(planView3DPane, home, 
        PLAN_PANE_DIVIDER_LOCATION_VISUAL_PROPERTY, 0.5, controller);
    
    // Detach 3D view if it was detached when saved and its dialog can be viewed in one of the screen devices
    Boolean detachedView3D = (Boolean)home.getVisualProperty(view3D.getClass().getName() + DETACHED_VIEW_VISUAL_PROPERTY);
    if (detachedView3D != null && detachedView3D.booleanValue()) {
      // Check 3D view can be viewed in one of the available screens      
      final Integer dialogX = (Integer)this.home.getVisualProperty(view3D.getClass().getName() + DETACHED_VIEW_X_VISUAL_PROPERTY);
      final Integer dialogY = (Integer)this.home.getVisualProperty(view3D.getClass().getName() + DETACHED_VIEW_Y_VISUAL_PROPERTY);
      if (dialogX != null) {
        for (GraphicsDevice screenDevice : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
          for (GraphicsConfiguration screenConfiguration : screenDevice.getConfigurations()) {
            if (screenConfiguration.getBounds().contains(dialogX, dialogY)) {
              EventQueue.invokeLater(new Runnable() {
                  public void run() {
                    detachView((View)view3D, dialogX, dialogY,
                        (Integer)home.getVisualProperty(view3D.getClass().getName() + DETACHED_VIEW_WIDTH_VISUAL_PROPERTY),
                        (Integer)home.getVisualProperty(view3D.getClass().getName() + DETACHED_VIEW_HEIGHT_VISUAL_PROPERTY));
                  }
                });
              return planView3DPane;
            }
          }
        }
      }
      planView3DPane.setDividerLocation(0.5);
      controller.setVisualProperty(view3D.getClass().getName() + DETACHED_VIEW_X_VISUAL_PROPERTY, null);
    }
    
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
          (JComponent)controller.getPlanController().getHorizontalRulerView());
      planScrollPane.setRowHeaderView(
          (JComponent)controller.getPlanController().getVerticalRulerView());
    } else {
      planScrollPane.setColumnHeaderView(null);
      planScrollPane.setRowHeaderView(null);
    }
  }
  
  /**
   * Adds to <code>view</code> a mouse listener that disables all menu items of
   * <code>menuBar</code> during a drag and drop operation in <code>view</code>.
   */
  private void disableMenuItemsDuringDragAndDrop(View view, 
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
    ((JComponent)view).addMouseListener(listener);
    ((JComponent)view).addFocusListener(listener);
  }
  

  /**
   * Detaches the given <code>view</code> from home view.
   */
  public void detachView(final View view) {
    JComponent component = (JComponent)view;
    Container parent = component.getParent();
    if (parent instanceof JViewport) {
      component = (JComponent)parent.getParent();
      parent = component.getParent();
    }
    
    float dividerLocation;
    if (parent instanceof JSplitPane) {
      JSplitPane splitPane = (JSplitPane)parent;
      if (splitPane.getOrientation() == JSplitPane.VERTICAL_SPLIT) {
        dividerLocation = (float)splitPane.getDividerLocation() 
            / (splitPane.getHeight() - splitPane.getDividerSize());
      } else {
        dividerLocation = (float)splitPane.getDividerLocation() 
          / (splitPane.getWidth() - splitPane.getDividerSize());
      }
    } else {
      dividerLocation = -1;
    }
    
    Integer dialogX = (Integer)this.home.getVisualProperty(view.getClass().getName() + DETACHED_VIEW_X_VISUAL_PROPERTY);
    Integer dialogWidth = (Integer)this.home.getVisualProperty(view.getClass().getName() + DETACHED_VIEW_WIDTH_VISUAL_PROPERTY);
    if (dialogX != null && dialogWidth != null) {
      detachView(view, dialogX, 
          (Integer)this.home.getVisualProperty(view.getClass().getName() + DETACHED_VIEW_Y_VISUAL_PROPERTY),
          dialogWidth,
          (Integer)this.home.getVisualProperty(view.getClass().getName() + DETACHED_VIEW_HEIGHT_VISUAL_PROPERTY));
    } else {
      Point componentLocation = new Point();
      Dimension componentSize = component.getSize();
      SwingUtilities.convertPointToScreen(componentLocation, component);
      
      Insets insets = new JDialog().getInsets();
      detachView(view, componentLocation.x - insets.left, 
          componentLocation.y - insets.top,
          componentSize.width + insets.left + insets.right,
          componentSize.height + insets.top + insets.bottom);
    }
    this.controller.setVisualProperty(view.getClass().getName() + DETACHED_VIEW_DIVIDER_LOCATION_VISUAL_PROPERTY, dividerLocation);
  }
  
  /**
   * Detaches a <code>view</code> at the given location and size.
   */
  private void detachView(final View view, int x, int y, int width, int height) {
    JComponent component = (JComponent)view;
    Container parent = component.getParent();
    if (parent instanceof JViewport) {
      component = (JComponent)parent.getParent();
      parent = component.getParent();
    }
    
    // Replace component by a dummy label to find easily where to attach back the component
    JLabel dummyLabel = new JLabel();
    dummyLabel.setMaximumSize(new Dimension());
    dummyLabel.setName(view.getClass().getName());
    dummyLabel.setBorder(component.getBorder());
    
    if (parent instanceof JSplitPane) {
      JSplitPane splitPane = (JSplitPane)parent;
      splitPane.setDividerSize(0);
      if (splitPane.getLeftComponent() == component) {
        splitPane.setLeftComponent(dummyLabel);
        splitPane.setDividerLocation(0f);
      } else {
        splitPane.setRightComponent(dummyLabel);
        splitPane.setDividerLocation(1f);
      }
    } else {
      int componentIndex = parent.getComponentZOrder(component);
      parent.remove(componentIndex);
      parent.add(dummyLabel, componentIndex);
    }
    
    // Display view in a separate non modal dialog
    Window window = SwingUtilities.getWindowAncestor(this);
    if (!(window instanceof JFrame)) {
      window = JOptionPane.getRootFrame();
    }
    JFrame defaultFrame = (JFrame)window;
    // Create a dialog with the same title as home frame 
    final JDialog separateDialog = new JDialog(defaultFrame, defaultFrame.getTitle(), false,
        component.getGraphicsConfiguration());
    separateDialog.setResizable(true);
    defaultFrame.addPropertyChangeListener("title", new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          separateDialog.setTitle((String)ev.getNewValue());
        }
      });
    // Use same document modified indicator
    if (OperatingSystem.isMacOSXLeopardOrSuperior()) {
      defaultFrame.getRootPane().addPropertyChangeListener("Window.documentModified", new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          separateDialog.getRootPane().putClientProperty("Window.documentModified", ev.getNewValue());
        }
      });      
    } else if (OperatingSystem.isMacOSX()) {
      defaultFrame.getRootPane().addPropertyChangeListener("windowModified", new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            separateDialog.getRootPane().putClientProperty("windowModified", ev.getNewValue());
          }
        });      
    }
    separateDialog.setContentPane(component);
    separateDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    separateDialog.addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent ev) {
          controller.attachView(view);
        }
      });
    separateDialog.addComponentListener(new ComponentAdapter() {
        @Override
        public void componentResized(ComponentEvent ev) {
          controller.setVisualProperty(view.getClass().getName() + DETACHED_VIEW_WIDTH_VISUAL_PROPERTY, separateDialog.getWidth());
          controller.setVisualProperty(view.getClass().getName() + DETACHED_VIEW_HEIGHT_VISUAL_PROPERTY, separateDialog.getHeight());
        }
        
        @Override
        public void componentMoved(ComponentEvent ev) {
          controller.setVisualProperty(view.getClass().getName() + DETACHED_VIEW_X_VISUAL_PROPERTY, separateDialog.getX());
          controller.setVisualProperty(view.getClass().getName() + DETACHED_VIEW_Y_VISUAL_PROPERTY, separateDialog.getY());
        }
      });

    separateDialog.setBounds(x, y, width, height);
    separateDialog.setVisible(true);
    
    this.controller.setVisualProperty(view.getClass().getName() + DETACHED_VIEW_VISUAL_PROPERTY, true);
  }
  
  /**
   * Attaches the given <code>view</code> to home view.
   */
  public void attachView(View view) {
    JComponent dummyComponent = (JComponent)findChild(this, view.getClass().getName());
    if (dummyComponent != null) {
      JComponent component = (JComponent)view;
      Window window = SwingUtilities.getWindowAncestor(component);
      component.setBorder(dummyComponent.getBorder());      
      Container parent = dummyComponent.getParent();
      if (parent instanceof JSplitPane) {
        JSplitPane splitPane = (JSplitPane)parent;
        float dividerLocation = (Float)this.home.getVisualProperty(
            view.getClass().getName() + DETACHED_VIEW_DIVIDER_LOCATION_VISUAL_PROPERTY);
        splitPane.setDividerSize(UIManager.getInt("SplitPane.dividerSize"));
        splitPane.setDividerLocation(dividerLocation);
        if (splitPane.getLeftComponent() == dummyComponent) {
          splitPane.setLeftComponent(component);
        } else {
          splitPane.setRightComponent(component);
        }
      } else {
        int componentIndex = parent.getComponentZOrder(dummyComponent);
        parent.remove(componentIndex);
        parent.add(component, componentIndex);
      }
      window.dispose();
    }
    
    this.controller.setVisualProperty(view.getClass().getName() + DETACHED_VIEW_VISUAL_PROPERTY, false);
  }
  
  /**
   * Returns among <code>parent</code> children the first child with the given name.
   */
  private Component findChild(Container parent, String childName) {
    for (int i = 0; i < parent.getComponentCount(); i++) {
      Component child = parent.getComponent(i);
      if (childName.equals(child.getName())) {
        return child;
      } else if (child instanceof Container) {
        child = findChild((Container)child, childName);
        if (child != null) {
          return child;
        }
      }            
    }
    return null;
  }
  
  /**
   * Displays a content chooser open dialog to choose the name of a home.
   */
  public String showOpenDialog() {
    return this.controller.getContentManager().showOpenDialog(this, 
        this.preferences.getLocalizedString(HomePane.class, "openHomeDialog.title"), 
        ContentManager.ContentType.SWEET_HOME_3D);
  }

  /**
   * Displays a dialog that lets user choose whether he wants to overwrite
   * an existing language library or not. 
   */
  public boolean confirmReplaceLanguageLibrary(String languageLibraryName) {
    // Retrieve displayed text in buttons and message
    String message = this.preferences.getLocalizedString(HomePane.class, "confirmReplaceLanguageLibrary.message", 
        new File(languageLibraryName).getName());
    String title = this.preferences.getLocalizedString(HomePane.class, "confirmReplaceLanguageLibrary.title");
    String replace = this.preferences.getLocalizedString(HomePane.class, "confirmReplaceLanguageLibrary.replace");
    String doNotReplace = this.preferences.getLocalizedString(HomePane.class, "confirmReplaceLanguageLibrary.doNotReplace");
        
    return JOptionPane.showOptionDialog(this, 
        message, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
        null, new Object [] {replace, doNotReplace}, doNotReplace) == JOptionPane.OK_OPTION;
  }
  
  /**
   * Displays a content chooser open dialog to choose a furniture library.
   */
  public String showImportFurnitureLibraryDialog() {
    return this.controller.getContentManager().showOpenDialog(this, 
        this.preferences.getLocalizedString(HomePane.class, "importFurnitureLibraryDialog.title"), 
        ContentManager.ContentType.FURNITURE_LIBRARY);
  }

  /**
   * Displays a dialog that lets user choose whether he wants to overwrite
   * an existing furniture library or not. 
   */
  public boolean confirmReplaceFurnitureLibrary(String furnitureLibraryName) {
    // Retrieve displayed text in buttons and message
    String message = this.preferences.getLocalizedString(HomePane.class, "confirmReplaceFurnitureLibrary.message", 
        new File(furnitureLibraryName).getName());
    String title = this.preferences.getLocalizedString(HomePane.class, "confirmReplaceFurnitureLibrary.title");
    String replace = this.preferences.getLocalizedString(HomePane.class, "confirmReplaceFurnitureLibrary.replace");
    String doNotReplace = this.preferences.getLocalizedString(HomePane.class, "confirmReplaceFurnitureLibrary.doNotReplace");
        
    return JOptionPane.showOptionDialog(this, 
        message, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
        null, new Object [] {replace, doNotReplace}, doNotReplace) == JOptionPane.OK_OPTION;
  }
  
  /**
   * Displays a content chooser open dialog to choose a textures library.
   */
  public String showImportTexturesLibraryDialog() {
    return this.controller.getContentManager().showOpenDialog(this, 
        this.preferences.getLocalizedString(HomePane.class, "importTexturesLibraryDialog.title"), 
        ContentManager.ContentType.TEXTURES_LIBRARY);
  }

  /**
   * Displays a dialog that lets user choose whether he wants to overwrite
   * an existing textures library or not. 
   */
  public boolean confirmReplaceTexturesLibrary(String texturesLibraryName) {
    // Retrieve displayed text in buttons and message
    String message = this.preferences.getLocalizedString(HomePane.class, "confirmReplaceTexturesLibrary.message", 
        new File(texturesLibraryName).getName());
    String title = this.preferences.getLocalizedString(HomePane.class, "confirmReplaceTexturesLibrary.title");
    String replace = this.preferences.getLocalizedString(HomePane.class, "confirmReplaceTexturesLibrary.replace");
    String doNotReplace = this.preferences.getLocalizedString(HomePane.class, "confirmReplaceTexturesLibrary.doNotReplace");
        
    return JOptionPane.showOptionDialog(this, 
        message, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
        null, new Object [] {replace, doNotReplace}, doNotReplace) == JOptionPane.OK_OPTION;
  }
  
  /**
   * Displays a dialog that lets user choose whether he wants to overwrite
   * an existing plug-in or not. 
   */
  public boolean confirmReplacePlugin(String pluginName) {
    // Retrieve displayed text in buttons and message
    String message = this.preferences.getLocalizedString(HomePane.class, "confirmReplacePlugin.message", 
        new File(pluginName).getName());
    String title = this.preferences.getLocalizedString(HomePane.class, "confirmReplacePlugin.title");
    String replace = this.preferences.getLocalizedString(HomePane.class, "confirmReplacePlugin.replace");
    String doNotReplace = this.preferences.getLocalizedString(HomePane.class, "confirmReplacePlugin.doNotReplace");
        
    return JOptionPane.showOptionDialog(this, 
        message, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
        null, new Object [] {replace, doNotReplace}, doNotReplace) == JOptionPane.OK_OPTION;
  }
  
  /**
   * Displays a content chooser save dialog to choose the name of a home.
   */
  public String showSaveDialog(String homeName) {
    return this.controller.getContentManager().showSaveDialog(this,
        this.preferences.getLocalizedString(HomePane.class, "saveHomeDialog.title"), 
        ContentManager.ContentType.SWEET_HOME_3D, homeName);
  }
  
  /**
   * Displays <code>message</code> in an error message box.
   */
  public void showError(String message) {
    String title = this.preferences.getLocalizedString(HomePane.class, "error.title");
    JOptionPane.showMessageDialog(this, message, title, 
        JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Displays <code>message</code> in a message box.
   */
  public void showMessage(String message) {
    String title = this.preferences.getLocalizedString(HomePane.class, "message.title");
    JOptionPane.showMessageDialog(this, message, title, 
        JOptionPane.INFORMATION_MESSAGE);
  }

  /**
   * Displays the tip matching <code>actionTipKey</code> and 
   * returns <code>true</code> if the user chose not to display again the tip.
   */
  public boolean showActionTipMessage(String actionTipKey) {
    String title = this.preferences.getLocalizedString(HomePane.class, actionTipKey + ".tipTitle");
    String message = this.preferences.getLocalizedString(HomePane.class, actionTipKey + ".tipMessage");
    if (message.length() > 0) {
      JPanel tipPanel = new JPanel(new GridBagLayout());
      
      JLabel messageLabel = new JLabel(message);
      tipPanel.add(messageLabel, new GridBagConstraints(
          0, 0, 1, 1, 0, 0, GridBagConstraints.NORTH, 
          GridBagConstraints.NONE, new Insets(0, 0, 10, 0), 0, 0));
      
      // Add a check box that lets user choose whether he wants to display again the tip or not
      JCheckBox doNotDisplayTipCheckBox = new JCheckBox(
          SwingTools.getLocalizedLabelText(this.preferences, HomePane.class, "doNotDisplayTipCheckBox.text"));
      if (!OperatingSystem.isMacOSX()) {
        doNotDisplayTipCheckBox.setMnemonic(KeyStroke.getKeyStroke(
            this.preferences.getLocalizedString(HomePane.class, "doNotDisplayTipCheckBox.mnemonic")).getKeyCode());
      }
      tipPanel.add(doNotDisplayTipCheckBox, new GridBagConstraints(
          0, 1, 1, 1, 0, 1, GridBagConstraints.CENTER, 
          GridBagConstraints.NONE, new Insets(0, 0, 5, 0), 0, 0));
      
      SwingTools.showMessageDialog(this, tipPanel, title, 
          JOptionPane.INFORMATION_MESSAGE, doNotDisplayTipCheckBox);
      return doNotDisplayTipCheckBox.isSelected();
    } else {
      // Ignore untranslated tips
      return true;
    }
  }
  
  /**
   * Displays a dialog that lets user choose whether he wants to save
   * the current home or not.
   * @return {@link com.eteks.sweethome3d.viewcontroller.HomeView.SaveAnswer#SAVE} 
   * if the user chose to save home,
   * {@link com.eteks.sweethome3d.viewcontroller.HomeView.SaveAnswer#DO_NOT_SAVE} 
   * if he doesn't want to save home,
   * or {@link com.eteks.sweethome3d.viewcontroller.HomeView.SaveAnswer#CANCEL} 
   * if he doesn't want to continue current operation.
   */
  public SaveAnswer confirmSave(String homeName) {
    // Retrieve displayed text in buttons and message
    String message;
    if (homeName != null) {
      message = this.preferences.getLocalizedString(HomePane.class, "confirmSave.message", 
          "\"" + this.controller.getContentManager().getPresentationName(
              homeName, ContentManager.ContentType.SWEET_HOME_3D) + "\"");
    } else {
      message = this.preferences.getLocalizedString(HomePane.class, "confirmSave.message", "");
    }
    String title = this.preferences.getLocalizedString(HomePane.class, "confirmSave.title");
    String save = this.preferences.getLocalizedString(HomePane.class, "confirmSave.save");
    String doNotSave = this.preferences.getLocalizedString(HomePane.class, "confirmSave.doNotSave");
    String cancel = this.preferences.getLocalizedString(HomePane.class, "confirmSave.cancel");

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
    String message = this.preferences.getLocalizedString(HomePane.class, "confirmSaveNewerHome.message", 
        this.controller.getContentManager().getPresentationName(
            homeName, ContentManager.ContentType.SWEET_HOME_3D));
    String title = this.preferences.getLocalizedString(HomePane.class, "confirmSaveNewerHome.title");
    String save = this.preferences.getLocalizedString(HomePane.class, "confirmSaveNewerHome.save");
    String doNotSave = this.preferences.getLocalizedString(HomePane.class, "confirmSaveNewerHome.doNotSave");
    
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
    String message = this.preferences.getLocalizedString(HomePane.class, "confirmExit.message");
    String title = this.preferences.getLocalizedString(HomePane.class, "confirmExit.title");
    String quit = this.preferences.getLocalizedString(HomePane.class, "confirmExit.quit");
    String doNotQuit = this.preferences.getLocalizedString(HomePane.class, "confirmExit.doNotQuit");
    
    return JOptionPane.showOptionDialog(this, message, title, 
        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
        null, new Object [] {quit, doNotQuit}, doNotQuit) == JOptionPane.YES_OPTION;
  }
  
  /**
   * Displays an about dialog.
   */
  public void showAboutDialog() {
    String messageFormat = this.preferences.getLocalizedString(HomePane.class, "about.message");
    String aboutVersion = this.controller.getVersion();
    String message = String.format(messageFormat, aboutVersion, System.getProperty("java.version"));
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
    
    String title = this.preferences.getLocalizedString(HomePane.class, "about.title");
    Icon   icon  = new ImageIcon(HomePane.class.getResource(
        this.preferences.getLocalizedString(HomePane.class, "about.icon")));
    JOptionPane.showMessageDialog(this, messagePane, title,  
        JOptionPane.INFORMATION_MESSAGE, icon);
  }

  /**
   * Shows a print dialog to print the home displayed by this pane.  
   * @return a print task to execute or <code>null</code> if the user canceled print.
   *    The <code>call</code> method of the returned task may throw a 
   *    {@link RecorderException RecorderException} exception if print failed 
   *    or an {@link InterruptedRecorderException InterruptedRecorderException}
   *    exception if it was interrupted.
   */
  public Callable<Void> showPrintDialog() {
    PageFormat pageFormat = HomePrintableComponent.getPageFormat(this.home.getPrint());
    final PrinterJob printerJob = PrinterJob.getPrinterJob();
    printerJob.setPrintable(new HomePrintableComponent(this.home, this.controller, getFont()), pageFormat);
    String jobName = this.preferences.getLocalizedString(HomePane.class, "print.jobName");
    if (this.home.getName() != null) {
      jobName += " - " + this.controller.getContentManager().getPresentationName(
          this.home.getName(), ContentManager.ContentType.SWEET_HOME_3D);
    }
    printerJob.setJobName(jobName);
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
    return this.controller.getContentManager().showSaveDialog(this,
        this.preferences.getLocalizedString(HomePane.class, "printToPDFDialog.title"), 
        ContentManager.ContentType.PDF, homeName);
  }
  
  /**
   * Prints a home to a given PDF file. This method may be overridden
   * to write to another kind of output stream.
   */
  public void printToPDF(String pdfFile) throws RecorderException {
    OutputStream outputStream = null;
    boolean printInterrupted = false;
    try {
      outputStream = new FileOutputStream(pdfFile);
      new HomePDFPrinter(this.home, this.preferences, this.controller, getFont())
          .write(outputStream);
    } catch (InterruptedIOException ex) {
      printInterrupted = true;
      throw new InterruptedRecorderException("Print interrupted");      
    } catch (IOException ex) {
      throw new RecorderException("Couldn't export to PDF", ex);
    } finally {
      try {
        if (outputStream != null) {
          outputStream.close();
        }
        // Delete the file if printing is interrupted
        if (printInterrupted) {
          new File(pdfFile).delete();
        }
      } catch (IOException ex) {
        throw new RecorderException("Couldn't export to PDF", ex);
      }
    }
  }
  
  /**
   * Shows a content chooser save dialog to export a home plan in a SVG file.
   */
  public String showExportToSVGDialog(String homeName) {
    return this.controller.getContentManager().showSaveDialog(this,
        this.preferences.getLocalizedString(HomePane.class, "exportToSVGDialog.title"), 
        ContentManager.ContentType.SVG, homeName);
  }

  /**
   * Exports the plan objects to a given SVG file.
   */
  public void exportToSVG(String svgFile) throws RecorderException {
    View planView = this.controller.getPlanController().getView();
    PlanComponent planComponent;
    if (planView instanceof PlanComponent) {
      planComponent = (PlanComponent)planView;
    } else {
      planComponent = new PlanComponent(this.home, this.preferences, null);
    }    
    
    OutputStream outputStream = null;
    boolean exportInterrupted = false;
    try {
      outputStream = new BufferedOutputStream(new FileOutputStream(svgFile));
      planComponent.exportToSVG(outputStream);
    } catch (InterruptedIOException ex) {
      exportInterrupted = true;
      throw new InterruptedRecorderException("Export to " + svgFile + " interrupted");
    } catch (IOException ex) {
      throw new RecorderException("Couldn't export to SVG in " + svgFile, ex);
    } finally {
      if (outputStream != null) {
        try {
          outputStream.close();
          // Delete the file if exporting is interrupted
          if (exportInterrupted) {
            new File(svgFile).delete();
          }
        } catch (IOException ex) {
          throw new RecorderException("Couldn't export to SVG in " + svgFile, ex);
        }
      }
    }
  }

  /**
   * Shows a content chooser save dialog to export a 3D home in a OBJ file.
   */
  public String showExportToOBJDialog(String homeName) {
    return this.controller.getContentManager().showSaveDialog(this,
        this.preferences.getLocalizedString(HomePane.class, "exportToOBJDialog.title"), 
        ContentManager.ContentType.OBJ, homeName);
  }
  
  /**
   * Exports the objects of the 3D view to the given OBJ file.
   */
  public void exportToOBJ(String objFile) throws RecorderException {
    OBJWriter writer = null;
    boolean exportInterrupted = false;
    try {
      String header = this.preferences != null
          ? this.preferences.getLocalizedString(HomePane.class, 
                                                "exportToOBJ.header", new Date())
          : "";      
      writer = new OBJWriter(objFile, header, -1);

      if (this.home.getWalls().size() > 0) {
        // Create a not alive new ground to be able to explore its coordinates without setting capabilities
        Rectangle2D homeBounds = getExportedHomeBounds();
        Ground3D groundNode = new Ground3D(this.home, 
            (float)homeBounds.getX(), (float)homeBounds.getY(), 
            (float)homeBounds.getWidth(), (float)homeBounds.getHeight(), true);
        writer.writeNode(groundNode, "ground");
      }
      
      // Write 3D walls 
      int i = 0;
      for (Wall wall : this.home.getWalls()) {
        // Create a not alive new wall to be able to explore its coordinates without setting capabilities 
        Wall3D wallNode = new Wall3D(wall, this.home, true, true);
        writer.writeNode(wallNode, "wall_" + ++i);
      }
      // Write 3D furniture 
      i = 0;
      for (HomePieceOfFurniture piece : this.home.getFurniture()) {
        if (piece.isVisible()) {
          // Create a not alive new piece to be able to explore its coordinates without setting capabilities
          HomePieceOfFurniture3D pieceNode = new HomePieceOfFurniture3D(piece, this.home, true, true);
          writer.writeNode(pieceNode, "piece_" + ++i);
        }
      }
      // Write 3D rooms 
      i = 0;
      for (Room room : this.home.getRooms()) {
        // Create a not alive new room to be able to explore its coordinates without setting capabilities 
        Room3D roomNode = new Room3D(room, this.home, false, true, true);
        writer.writeNode(roomNode, "room_" + ++i);
      }
    } catch (InterruptedIOException ex) {
      exportInterrupted = true;
      throw new InterruptedRecorderException("Export to " + objFile + " interrupted");
    } catch (IOException ex) {
      throw new RecorderException("Couldn't export to OBJ in " + objFile, ex);
    } finally {
      if (writer != null) {
        try {
          writer.close();
          // Delete the file if exporting is interrupted
          if (exportInterrupted) {
            new File(objFile).delete();
          }
        } catch (IOException ex) {
          throw new RecorderException("Couldn't export to OBJ in " + objFile, ex);
        }
      }
    }
  }
  
  /**
   * Returns home bounds. 
   */
  private Rectangle2D getExportedHomeBounds() {
    // Compute bounds that include walls and furniture
    Rectangle2D homeBounds = updateObjectsBounds(null, this.home.getWalls());
    for (HomePieceOfFurniture piece : getVisibleFurniture(this.home.getFurniture())) {
      if (piece.isVisible()) {
        for (float [] point : piece.getPoints()) {
          if (homeBounds == null) {
            homeBounds = new Rectangle2D.Float(point [0], point [1], 0, 0);
          } else {
            homeBounds.add(point [0], point [1]);
          }
        }
      }
    }
    return updateObjectsBounds(homeBounds, this.home.getRooms());
  }
  
  /**
   * Returns all the visible pieces in the given <code>furniture</code>.  
   */
  private List<HomePieceOfFurniture> getVisibleFurniture(List<HomePieceOfFurniture> furniture) {
    List<HomePieceOfFurniture> visibleFurniture = new ArrayList<HomePieceOfFurniture>(furniture.size());
    for (HomePieceOfFurniture piece : furniture) {
      if (piece.isVisible()) {
        if (piece instanceof HomeFurnitureGroup) {
          visibleFurniture.addAll(getVisibleFurniture(((HomeFurnitureGroup)piece).getFurniture()));
        } else {
          visibleFurniture.add(piece);
        }
      }
    }
    return visibleFurniture;
  }

  /**
   * Updates <code>objectBounds</code> to include the bounds of <code>objects</code>.
   */
  private Rectangle2D updateObjectsBounds(Rectangle2D objectBounds,
                                          Collection<? extends Selectable> objects) {
    for (Selectable wall : objects) {
      for (float [] point : wall.getPoints()) {
        if (objectBounds == null) {
          objectBounds = new Rectangle2D.Float(point [0], point [1], 0, 0);
        } else {
          objectBounds.add(point [0], point [1]);
        }
      }
    }
    return objectBounds;
  }

  /**
   * Displays a dialog that let user choose whether he wants to delete 
   * the selected furniture from catalog or not.
   * @return <code>true</code> if user confirmed to delete.
   */
  public boolean confirmDeleteCatalogSelection() {
    // Retrieve displayed text in buttons and message
    String message = this.preferences.getLocalizedString(HomePane.class, "confirmDeleteCatalogSelection.message");
    String title = this.preferences.getLocalizedString(HomePane.class, "confirmDeleteCatalogSelection.title");
    String delete = this.preferences.getLocalizedString(HomePane.class, "confirmDeleteCatalogSelection.delete");
    String cancel = this.preferences.getLocalizedString(HomePane.class, "confirmDeleteCatalogSelection.cancel");
    
    return JOptionPane.showOptionDialog(this, message, title, 
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
        null, new Object [] {delete, cancel}, cancel) == JOptionPane.OK_OPTION;
  }
  
  /**
   * Returns <code>true</code> if clipboard contains data that
   * components are able to handle.
   */
  public boolean isClipboardEmpty() {
    Clipboard clipboard = getToolkit().getSystemClipboard();
    return !(clipboard.isDataFlavorAvailable(HomeTransferableList.HOME_FLAVOR)
        || getToolkit().getSystemClipboard().isDataFlavorAvailable(DataFlavor.javaFileListFlavor));    
  }

  /**
   * Execute <code>runnable</code> asynchronously in the thread 
   * that manages toolkit events.  
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
            if (PluginAction.Property.ENABLED.name().equals(propertyName)) {
              propertyChangeSupport.firePropertyChange(
                  new PropertyChangeEvent(ev.getSource(), "enabled", oldValue, newValue));
            } else {
              // In case a property value changes, fire the new value decorated in subclasses
              // unless new value is null (most Swing listeners don't check new value is null !)
              if (newValue != null) {
                if (PluginAction.Property.NAME.name().equals(propertyName)) {
                  propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(ev.getSource(), 
                      Action.NAME, oldValue, newValue));
                } else if (PluginAction.Property.SHORT_DESCRIPTION.name().equals(propertyName)) {
                  propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(ev.getSource(), 
                      Action.NAME, oldValue, newValue));
                } else if (PluginAction.Property.MNEMONIC.name().equals(propertyName)) {
                  propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(ev.getSource(), 
                      Action.MNEMONIC_KEY, 
                      oldValue != null 
                          ? new Integer((Character)oldValue) 
                          : null, newValue));
                } else if (PluginAction.Property.SMALL_ICON.name().equals(propertyName)) {
                  propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(ev.getSource(), 
                      Action.SMALL_ICON, 
                      oldValue != null 
                         ? IconManager.getInstance().getIcon((Content)oldValue, DEFAULT_SMALL_ICON_HEIGHT, HomePane.this) 
                         : null, newValue));
                } else {
                  propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(ev.getSource(), 
                      propertyName, oldValue, newValue));
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
      } else if (PluginAction.Property.TOOL_BAR.name().equals(key)) {
        return this.pluginAction.getPropertyValue(PluginAction.Property.TOOL_BAR);
      } else if (PluginAction.Property.MENU.name().equals(key)) {
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
      } else if (PluginAction.Property.TOOL_BAR.name().equals(key)) {
        this.pluginAction.putPropertyValue(PluginAction.Property.TOOL_BAR, value);
      } else if (PluginAction.Property.MENU.name().equals(key)) {
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
      SwingTools.installFocusBorder(view);
    }
  }
  
  /**
   * A popup menu listener that displays only enabled menu items.
   */
  private static class MenuItemsVisibilityListener implements PopupMenuListener {
    public void popupMenuWillBecomeVisible(PopupMenuEvent ev) {        
      JPopupMenu popupMenu = (JPopupMenu)ev.getSource();
      // Make visible only enabled menu items   
      for (int i = 0; i < popupMenu.getComponentCount(); i++) {
        Component component = popupMenu.getComponent(i);
        if (component instanceof JMenu) {
          component.setVisible(containsEnabledItems((JMenu)component));
        } else if (component instanceof JMenuItem) {
          component.setVisible(component.isEnabled());
        }
      }
      hideUselessSeparators(popupMenu);
      // Ensure at least one item is visible
      boolean allItemsInvisible = true;
      for (int i = 0; i < popupMenu.getComponentCount(); i++) {
        if (popupMenu.getComponent(i).isVisible()) {
          allItemsInvisible = false;
          break;
        }
      }  
      if (allItemsInvisible) {
        popupMenu.getComponent(0).setVisible(true);
      }
    }

    /**
     * Makes useless separators invisible.
     */
    private void hideUselessSeparators(JPopupMenu popupMenu) {
      boolean allMenuItemsInvisible = true;
      int lastVisibleSeparatorIndex = -1;
      for (int i = 0; i < popupMenu.getComponentCount(); i++) {
        Component component = popupMenu.getComponent(i);
        if (allMenuItemsInvisible && (component instanceof JMenuItem)) {
          if (component.isVisible()) {
            allMenuItemsInvisible = false;
          }
        } else if (component instanceof JSeparator) {          
          component.setVisible(!allMenuItemsInvisible);
          if (!allMenuItemsInvisible) {
            lastVisibleSeparatorIndex = i;
          }
          allMenuItemsInvisible = true;
        }
      }  
      if (lastVisibleSeparatorIndex != -1 && allMenuItemsInvisible) {
        // Check if last separator is the first visible component
        boolean allComponentsBeforeLastVisibleSeparatorInvisible = true;
        for (int i = lastVisibleSeparatorIndex - 1; i >= 0; i--) {
          if (popupMenu.getComponent(i).isVisible()) {
            allComponentsBeforeLastVisibleSeparatorInvisible = false;
            break;
          }
        }
        boolean allComponentsAfterLastVisibleSeparatorInvisible = true;
        for (int i = lastVisibleSeparatorIndex; i < popupMenu.getComponentCount(); i++) {
          if (popupMenu.getComponent(i).isVisible()) {
            allComponentsBeforeLastVisibleSeparatorInvisible = false;
            break;
          }
        }
        
        popupMenu.getComponent(lastVisibleSeparatorIndex).setVisible(
            !allComponentsBeforeLastVisibleSeparatorInvisible && !allComponentsAfterLastVisibleSeparatorInvisible);
      }
    }

    /**
     * Returns <code>true</code> if the given <code>menu</code> contains 
     * at least one enabled menu item.
     */
    private boolean containsEnabledItems(JMenu menu) {
      boolean menuContainsEnabledItems = false;
      for (int i = 0; i < menu.getMenuComponentCount() && !menuContainsEnabledItems; i++) {
        Component component = menu.getMenuComponent(i);
        if (component instanceof JMenu) {
          menuContainsEnabledItems = containsEnabledItems((JMenu)component);
        } else if (component instanceof JMenuItem) {
          menuContainsEnabledItems = component.isEnabled();
        }
      }
      return menuContainsEnabledItems;
    }

    public void popupMenuCanceled(PopupMenuEvent ev) {
    }

    public void popupMenuWillBecomeInvisible(PopupMenuEvent ev) {
    }
  }
}
