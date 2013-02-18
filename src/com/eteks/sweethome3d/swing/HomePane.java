/*
 * HomePane.java 15 mai 2006
 *
 * Sweet Home 3D, Copyright (c) 2006 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
import java.awt.FocusTraversalPolicy;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
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
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.LayoutFocusTraversalPolicy;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.RootPaneContainer;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
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
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.SwingPropertyChangeSupport;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.text.JTextComponent;

import com.eteks.sweethome3d.j3d.Ground3D;
import com.eteks.sweethome3d.j3d.HomePieceOfFurniture3D;
import com.eteks.sweethome3d.j3d.OBJWriter;
import com.eteks.sweethome3d.j3d.Room3D;
import com.eteks.sweethome3d.j3d.Wall3D;
import com.eteks.sweethome3d.model.BackgroundImage;
import com.eteks.sweethome3d.model.Camera;
import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.CollectionEvent;
import com.eteks.sweethome3d.model.CollectionListener;
import com.eteks.sweethome3d.model.Compass;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.DimensionLine;
import com.eteks.sweethome3d.model.Elevatable;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeEnvironment;
import com.eteks.sweethome3d.model.HomeFurnitureGroup;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.InterruptedRecorderException;
import com.eteks.sweethome3d.model.Label;
import com.eteks.sweethome3d.model.Level;
import com.eteks.sweethome3d.model.Library;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.SelectionEvent;
import com.eteks.sweethome3d.model.SelectionListener;
import com.eteks.sweethome3d.model.TextStyle;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.plugin.HomePluginController;
import com.eteks.sweethome3d.plugin.Plugin;
import com.eteks.sweethome3d.plugin.PluginAction;
import com.eteks.sweethome3d.plugin.PluginManager;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.ContentManager;
import com.eteks.sweethome3d.viewcontroller.FurnitureController;
import com.eteks.sweethome3d.viewcontroller.HomeController;
import com.eteks.sweethome3d.viewcontroller.HomeController3D;
import com.eteks.sweethome3d.viewcontroller.HomeView;
import com.eteks.sweethome3d.viewcontroller.PlanController;
import com.eteks.sweethome3d.viewcontroller.PlanController.Mode;
import com.eteks.sweethome3d.viewcontroller.PlanView;
import com.eteks.sweethome3d.viewcontroller.View;

/**
 * The MVC view that edits a home. 
 * @author Emmanuel Puybaret
 */
public class HomePane extends JRootPane implements HomeView {
  private enum MenuActionType {FILE_MENU, EDIT_MENU, FURNITURE_MENU, PLAN_MENU, VIEW_3D_MENU, HELP_MENU, 
      OPEN_RECENT_HOME_MENU, ALIGN_OR_DISTRIBUTE_MENU, SORT_HOME_FURNITURE_MENU, DISPLAY_HOME_FURNITURE_PROPERTY_MENU, 
      MODIFY_TEXT_STYLE, GO_TO_POINT_OF_VIEW, SELECT_OBJECT_MENU}
  
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
  
  private final Home            home;
  private final UserPreferences preferences;
  private final HomeController  controller;
  private JComponent            lastFocusedComponent;
  private PlanController.Mode   previousPlanControllerMode;
  private TransferHandler       catalogTransferHandler;
  private TransferHandler       furnitureTransferHandler;
  private TransferHandler       planTransferHandler;
  private boolean               transferHandlerEnabled;
  private MouseInputAdapter     furnitureCatalogDragAndDropListener;
  private boolean               clipboardEmpty = true;
  private boolean               exportAllToOBJ = true;
  private ActionMap             menuActionMap;
  private List<Action>          pluginActions;
  
  /**
   * Creates home view associated with its controller.
   */
  public HomePane(Home home, UserPreferences preferences, 
                  final HomeController controller) {
    this.home = home;
    this.preferences = preferences;
    this.controller = controller;

    JPopupMenu.setDefaultLightWeightPopupEnabled(false);
    ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);    
    
    createActions(home, preferences, controller);
    createMenuActions(preferences, controller);
    createPluginActions(controller instanceof HomePluginController
        ? ((HomePluginController)controller).getPlugins()
        : null);
    createTransferHandlers(home, controller);
    addHomeListener(home);
    addLevelVisibilityListener(home);
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
  private void createActions(Home home,
                             UserPreferences preferences, 
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
    createClipboardAction(ActionType.CUT, preferences, TransferHandler.getCutAction(), true);
    createClipboardAction(ActionType.COPY, preferences, TransferHandler.getCopyAction(), true);
    createClipboardAction(ActionType.PASTE, preferences, TransferHandler.getPasteAction(), false);
    createAction(ActionType.DELETE, preferences, controller, "delete");
    createAction(ActionType.SELECT_ALL, preferences, controller, "selectAll");
    
    createAction(ActionType.ADD_HOME_FURNITURE, preferences, controller, "addHomeFurniture");
    FurnitureController furnitureController = controller.getFurnitureController();
    createAction(ActionType.DELETE_HOME_FURNITURE, preferences, furnitureController, "deleteSelection");
    createAction(ActionType.MODIFY_FURNITURE, preferences, controller, "modifySelectedFurniture");
    createAction(ActionType.GROUP_FURNITURE, preferences, furnitureController, "groupSelectedFurniture");
    createAction(ActionType.UNGROUP_FURNITURE, preferences, furnitureController, "ungroupSelectedFurniture");
    createAction(ActionType.ALIGN_FURNITURE_ON_TOP, preferences, furnitureController, "alignSelectedFurnitureOnTop");
    createAction(ActionType.ALIGN_FURNITURE_ON_BOTTOM, preferences, furnitureController, "alignSelectedFurnitureOnBottom");
    createAction(ActionType.ALIGN_FURNITURE_ON_LEFT, preferences, furnitureController, "alignSelectedFurnitureOnLeft");
    createAction(ActionType.ALIGN_FURNITURE_ON_RIGHT, preferences, furnitureController, "alignSelectedFurnitureOnRight");
    createAction(ActionType.ALIGN_FURNITURE_ON_FRONT_SIDE, preferences, furnitureController, "alignSelectedFurnitureOnFrontSide");
    createAction(ActionType.ALIGN_FURNITURE_ON_BACK_SIDE, preferences, furnitureController, "alignSelectedFurnitureOnBackSide");
    createAction(ActionType.ALIGN_FURNITURE_ON_LEFT_SIDE, preferences, furnitureController, "alignSelectedFurnitureOnLeftSide");
    createAction(ActionType.ALIGN_FURNITURE_ON_RIGHT_SIDE, preferences, furnitureController, "alignSelectedFurnitureOnRightSide");
    createAction(ActionType.ALIGN_FURNITURE_SIDE_BY_SIDE, preferences, furnitureController, "alignSelectedFurnitureSideBySide");
    createAction(ActionType.DISTRIBUTE_FURNITURE_HORIZONTALLY, preferences, furnitureController, "distributeSelectedFurnitureHorizontally");
    createAction(ActionType.DISTRIBUTE_FURNITURE_VERTICALLY, preferences, furnitureController, "distributeSelectedFurnitureVertically");
    final HomeController3D homeController3D = controller.getHomeController3D();
    if (homeController3D.getView() != null) {
      createAction(ActionType.IMPORT_FURNITURE, preferences, controller, "importFurniture");
    }
    createAction(ActionType.IMPORT_FURNITURE_LIBRARY, preferences, controller, "importFurnitureLibrary");
    createAction(ActionType.IMPORT_TEXTURE, preferences, controller, "importTexture");
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
    createAction(ActionType.SORT_HOME_FURNITURE_BY_LEVEL, preferences, 
        furnitureController, "toggleFurnitureSort", HomePieceOfFurniture.SortableProperty.LEVEL);
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
    createAction(ActionType.DISPLAY_HOME_FURNITURE_LEVEL, preferences, 
        furnitureController, "toggleFurnitureVisibleProperty", HomePieceOfFurniture.SortableProperty.LEVEL);
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
    createAction(ActionType.EXPORT_TO_CSV, preferences, controller, "exportToCSV");
    
    PlanController planController = controller.getPlanController();
    if (planController.getView() != null) {
      ButtonGroup modeGroup = new ButtonGroup();
      createToggleAction(ActionType.SELECT, planController.getMode() == PlanController.Mode.SELECTION, modeGroup, 
          preferences, controller, "setMode", PlanController.Mode.SELECTION);
      createToggleAction(ActionType.PAN, planController.getMode() == PlanController.Mode.PANNING, modeGroup, 
          preferences, controller, "setMode", PlanController.Mode.PANNING);
      createToggleAction(ActionType.CREATE_WALLS, planController.getMode() == PlanController.Mode.WALL_CREATION, modeGroup, 
          preferences, controller, "setMode", PlanController.Mode.WALL_CREATION);
      createToggleAction(ActionType.CREATE_ROOMS, planController.getMode() == PlanController.Mode.ROOM_CREATION, modeGroup, 
          preferences, controller, "setMode", PlanController.Mode.ROOM_CREATION);
      createToggleAction(ActionType.CREATE_DIMENSION_LINES, planController.getMode() == PlanController.Mode.DIMENSION_LINE_CREATION, modeGroup, 
          preferences, controller, "setMode", PlanController.Mode.DIMENSION_LINE_CREATION);
      createToggleAction(ActionType.CREATE_LABELS, planController.getMode() == PlanController.Mode.LABEL_CREATION, modeGroup, 
          preferences, controller, "setMode", PlanController.Mode.LABEL_CREATION);
      createAction(ActionType.DELETE_SELECTION, preferences, planController, "deleteSelection");
      createAction(ActionType.LOCK_BASE_PLAN, preferences, planController, "lockBasePlan");
      createAction(ActionType.UNLOCK_BASE_PLAN, preferences, planController, "unlockBasePlan");
      createAction(ActionType.MODIFY_COMPASS, preferences, planController, "modifyCompass");
      createAction(ActionType.MODIFY_WALL, preferences, planController, "modifySelectedWalls");
      createAction(ActionType.MODIFY_ROOM, preferences, planController, "modifySelectedRooms");
      createAction(ActionType.MODIFY_LABEL, preferences, planController, "modifySelectedLabels");
      createAction(ActionType.INCREASE_TEXT_SIZE, preferences, planController, "increaseTextSize");
      createAction(ActionType.DECREASE_TEXT_SIZE, preferences, planController, "decreaseTextSize");
      // Use special toggle models for bold and italic check box menu items and tool bar buttons 
      // that are selected texts in home selected items are all bold or italic
      Action toggleBoldAction = createAction(ActionType.TOGGLE_BOLD_STYLE, preferences, planController, "toggleBoldStyle");
      toggleBoldAction.putValue(ResourceAction.TOGGLE_BUTTON_MODEL, createBoldStyleToggleModel(home, preferences));
      Action toggleItalicAction = createAction(ActionType.TOGGLE_ITALIC_STYLE, preferences, planController, "toggleItalicStyle");
      toggleItalicAction.putValue(ResourceAction.TOGGLE_BUTTON_MODEL, createItalicStyleToggleModel(home, preferences));
      createAction(ActionType.REVERSE_WALL_DIRECTION, preferences, planController, "reverseSelectedWallsDirection");
      createAction(ActionType.SPLIT_WALL, preferences, planController, "splitSelectedWall");
      createAction(ActionType.IMPORT_BACKGROUND_IMAGE, preferences, controller, "importBackgroundImage");
      createAction(ActionType.MODIFY_BACKGROUND_IMAGE, preferences, controller, "modifyBackgroundImage");
      createAction(ActionType.HIDE_BACKGROUND_IMAGE, preferences, controller, "hideBackgroundImage");
      createAction(ActionType.SHOW_BACKGROUND_IMAGE, preferences, controller, "showBackgroundImage");
      createAction(ActionType.DELETE_BACKGROUND_IMAGE, preferences, controller, "deleteBackgroundImage");
      createAction(ActionType.ADD_LEVEL, preferences, planController, "addLevel");
      createAction(ActionType.MODIFY_LEVEL, preferences, planController, "modifySelectedLevel");
      createAction(ActionType.DELETE_LEVEL, preferences, planController, "deleteSelectedLevel");
      createAction(ActionType.ZOOM_IN, preferences, controller, "zoomIn");
      createAction(ActionType.ZOOM_OUT, preferences, controller, "zoomOut");
      createAction(ActionType.EXPORT_TO_SVG, preferences, controller, "exportToSVG");
    }
    
    if (homeController3D.getView() != null) {
      ButtonGroup viewGroup = new ButtonGroup();
      createToggleAction(ActionType.VIEW_FROM_TOP, home.getCamera() == home.getTopCamera(), viewGroup, 
          preferences, homeController3D, "viewFromTop");
      createToggleAction(ActionType.VIEW_FROM_OBSERVER, home.getCamera() == home.getObserverCamera(), viewGroup, 
          preferences, homeController3D, "viewFromObserver");
      createAction(ActionType.MODIFY_OBSERVER, preferences, planController, "modifyObserverCamera");
      createAction(ActionType.STORE_POINT_OF_VIEW, preferences, controller, "storeCamera");
      createAction(ActionType.DELETE_POINTS_OF_VIEW, preferences, controller, "deleteCameras");
      getActionMap().put(ActionType.DETACH_3D_VIEW, 
          new ResourceAction(preferences, HomePane.class, ActionType.DETACH_3D_VIEW.name()) {
            @Override
            public void actionPerformed(ActionEvent ev) {
              controller.detachView(homeController3D.getView());
            }
          });
      getActionMap().put(ActionType.ATTACH_3D_VIEW, 
          new ResourceAction(preferences, HomePane.class, ActionType.ATTACH_3D_VIEW.name()) {
            @Override
            public void actionPerformed(ActionEvent ev) {
              controller.attachView(homeController3D.getView());
            }
          });

      ButtonGroup displayLevelGroup = new ButtonGroup();
      boolean allLevelsVisible = home.getEnvironment().isAllLevelsVisible();
      createToggleAction(ActionType.DISPLAY_ALL_LEVELS, allLevelsVisible, displayLevelGroup, preferences, 
          homeController3D, "displayAllLevels");
      createToggleAction(ActionType.DISPLAY_SELECTED_LEVEL, !allLevelsVisible, displayLevelGroup, preferences, 
          homeController3D, "displaySelectedLevel");
      createAction(ActionType.MODIFY_3D_ATTRIBUTES, preferences, homeController3D, "modifyAttributes");
      createAction(ActionType.CREATE_PHOTO, preferences, controller, "createPhoto");
      createAction(ActionType.CREATE_PHOTOS_AT_POINTS_OF_VIEW, preferences, controller, "createPhotos");
      createAction(ActionType.CREATE_VIDEO, preferences, controller, "createVideo");
      createAction(ActionType.EXPORT_TO_OBJ, preferences, controller, "exportToOBJ");
    }
    
    createAction(ActionType.HELP, preferences, controller, "help");
    createAction(ActionType.ABOUT, preferences, controller, "about");
  }

  /**
   * Returns a new <code>ControllerAction</code> object that calls on <code>controller</code> a given
   * <code>method</code> with its <code>parameters</code>. This action is added to the action map of this component.
   */
  private Action createAction(ActionType actionType,
                              UserPreferences preferences,                            
                              Object controller, 
                              String method, 
                              Object ... parameters) {
    try {
      ControllerAction action = new ControllerAction(
          preferences, HomePane.class, actionType.name(), controller, method, parameters);
      getActionMap().put(actionType, action);
      return action;
    } catch (NoSuchMethodException ex) {
      throw new RuntimeException(ex);
    }
  }
  
  /**
   * Returns a new <code>ControllerAction</code> object associated with a <code>ToggleButtonModel</code> instance 
   * set as selected or not.
   */
  private Action createToggleAction(ActionType actionType,
                                    boolean selected,
                                    ButtonGroup group,
                                    UserPreferences preferences,                            
                                    Object controller, 
                                    String method, 
                                    Object ... parameters) {
    Action action = createAction(actionType, preferences, controller, method, parameters);
    JToggleButton.ToggleButtonModel toggleButtonModel = new JToggleButton.ToggleButtonModel();
    toggleButtonModel.setSelected(selected);
    if (group != null) {
      toggleButtonModel.setGroup(group);
    }
    action.putValue(ResourceAction.TOGGLE_BUTTON_MODEL, toggleButtonModel);
    return action;
  }
  
  /**
   * Creates a <code>ReourceAction</code> object that calls 
   * <code>actionPerfomed</code> method on a given 
   * existing <code>clipboardAction</code> with a source equal to focused component.
   */
  private void createClipboardAction(ActionType actionType,
                                     UserPreferences preferences,
                                     final Action clipboardAction,
                                     final boolean copyAction) {
    getActionMap().put(actionType,
        new ResourceAction (preferences, HomePane.class, actionType.name()) {
          public void actionPerformed(ActionEvent ev) {
            if (copyAction) {
              clipboardEmpty = false;
            }
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
    createMenuAction(preferences, MenuActionType.ALIGN_OR_DISTRIBUTE_MENU);
    createMenuAction(preferences, MenuActionType.DISPLAY_HOME_FURNITURE_PROPERTY_MENU);
    createMenuAction(preferences, MenuActionType.MODIFY_TEXT_STYLE);
    createMenuAction(preferences, MenuActionType.GO_TO_POINT_OF_VIEW);
    createMenuAction(preferences, MenuActionType.SELECT_OBJECT_MENU);
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
        new FurnitureCatalogTransferHandler(controller.getContentManager(), 
            controller.getFurnitureCatalogController(), controller.getFurnitureController());
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
            setToggleButtonModelSelected(ActionType.VIEW_FROM_TOP, home.getCamera() == home.getTopCamera());
            setToggleButtonModelSelected(ActionType.VIEW_FROM_OBSERVER, home.getCamera() == home.getObserverCamera());
          }
        });
  }

  /**
   * Changes the selection of the toggle model matching the given action.
   */
  private void setToggleButtonModelSelected(ActionType actionType, boolean selected) {
    ((JToggleButton.ToggleButtonModel)getActionMap().get(actionType).getValue(ResourceAction.TOGGLE_BUTTON_MODEL)).
        setSelected(selected);
  }
  
  /**
   * Adds listener to <code>home</code> to update
   * Display all levels and Display selected level toggle models 
   * according their visibility.
   */
  private void addLevelVisibilityListener(final Home home) {
    home.getEnvironment().addPropertyChangeListener(HomeEnvironment.Property.ALL_LEVELS_VISIBLE, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            boolean allLevelsVisible = home.getEnvironment().isAllLevelsVisible();
            setToggleButtonModelSelected(ActionType.DISPLAY_ALL_LEVELS, allLevelsVisible);
            setToggleButtonModelSelected(ActionType.DISPLAY_SELECTED_LEVEL, !allLevelsVisible);
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
        SwingTools.updateSwingResourceLanguage((UserPreferences)ev.getSource());
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
            Mode mode = planController.getMode();
            setToggleButtonModelSelected(ActionType.SELECT, mode == PlanController.Mode.SELECTION);
            setToggleButtonModelSelected(ActionType.PAN, mode == PlanController.Mode.PANNING);
            setToggleButtonModelSelected(ActionType.CREATE_WALLS, mode == PlanController.Mode.WALL_CREATION);
            setToggleButtonModelSelected(ActionType.CREATE_ROOMS, mode == PlanController.Mode.ROOM_CREATION);
            setToggleButtonModelSelected(ActionType.CREATE_DIMENSION_LINES, mode == PlanController.Mode.DIMENSION_LINE_CREATION);
            setToggleButtonModelSelected(ActionType.CREATE_LABELS, mode == PlanController.Mode.LABEL_CREATION);
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
          KeyboardFocusManager.getCurrentKeyboardFocusManager().removePropertyChangeListener("focusOwner", 
              this.focusChangeListener);
          this.focusChangeListener = null;
        } else if (SwingUtilities.isDescendingFrom(homePane, (Component)ev.getNewValue())) {
          this.focusChangeListener = new FocusOwnerChangeListener(homePane);             
          KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("focusOwner", 
              this.focusChangeListener);          
        }
      }
    }
  }
  
  /**
   * Property listener bound to this component with a weak reference to avoid
   * strong link between KeyboardFocusManager and this component.  
   */
  private static class FocusOwnerChangeListener implements PropertyChangeListener {
    private WeakReference<HomePane> homePane;
    
    private FocusOwnerChangeListener(HomePane homePane) {
      this.homePane = new WeakReference<HomePane>(homePane);
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
      // If home pane was garbage collected, remove this listener from KeyboardFocusManager
      final HomePane homePane = this.homePane.get();
      if (homePane == null) {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removePropertyChangeListener("focusOwner", this);
      } else {
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
            View [] focusableViews = {homePane.controller.getFurnitureCatalogController().getView(),
                                      homePane.controller.getFurnitureController().getView(),
                                      homePane.controller.getPlanController().getView(),
                                      homePane.controller.getHomeController3D().getView()};
            // Notify controller that active view changed
            for (View view : focusableViews) {
              if (view != null && SwingUtilities.isDescendingFrom(gainedFocusedComponent, (JComponent)view)) {
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
    addActionToMenu(ActionType.ALIGN_FURNITURE_ON_FRONT_SIDE, furnitureMenu);
    addActionToMenu(ActionType.ALIGN_FURNITURE_ON_BACK_SIDE, furnitureMenu);
    addActionToMenu(ActionType.ALIGN_FURNITURE_ON_LEFT_SIDE, furnitureMenu);
    addActionToMenu(ActionType.ALIGN_FURNITURE_ON_RIGHT_SIDE, furnitureMenu);
    addActionToMenu(ActionType.ALIGN_FURNITURE_SIDE_BY_SIDE, furnitureMenu);
    addActionToMenu(ActionType.DISTRIBUTE_FURNITURE_HORIZONTALLY, furnitureMenu);
    addActionToMenu(ActionType.DISTRIBUTE_FURNITURE_VERTICALLY, furnitureMenu);
    furnitureMenu.addSeparator();
    addActionToMenu(ActionType.IMPORT_FURNITURE, furnitureMenu);
    addActionToMenu(ActionType.IMPORT_FURNITURE_LIBRARY, furnitureMenu);
    addActionToMenu(ActionType.IMPORT_TEXTURE, furnitureMenu);
    addActionToMenu(ActionType.IMPORT_TEXTURES_LIBRARY, furnitureMenu);
    furnitureMenu.addSeparator();
    furnitureMenu.add(createFurnitureSortMenu(home, preferences));
    furnitureMenu.add(createFurnitureDisplayPropertyMenu(home, preferences));
    furnitureMenu.addSeparator();
    addActionToMenu(ActionType.EXPORT_TO_CSV, furnitureMenu);
    
    // Create Plan menu
    JMenu planMenu = new JMenu(this.menuActionMap.get(MenuActionType.PLAN_MENU));
    addToggleActionToMenu(ActionType.SELECT, true, planMenu);
    addToggleActionToMenu(ActionType.PAN, true, planMenu);
    addToggleActionToMenu(ActionType.CREATE_WALLS, true, planMenu);
    addToggleActionToMenu(ActionType.CREATE_ROOMS, true, planMenu);
    addToggleActionToMenu(ActionType.CREATE_DIMENSION_LINES, true, planMenu);
    addToggleActionToMenu(ActionType.CREATE_LABELS, true, planMenu);
    planMenu.addSeparator();
    JMenuItem lockUnlockBasePlanMenuItem = createLockUnlockBasePlanMenuItem(home, false);
    if (lockUnlockBasePlanMenuItem != null) {
      planMenu.add(lockUnlockBasePlanMenuItem);
    }
    addActionToMenu(ActionType.MODIFY_COMPASS, planMenu);
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
    addActionToMenu(ActionType.ADD_LEVEL, planMenu);
    addActionToMenu(ActionType.MODIFY_LEVEL, planMenu);
    addActionToMenu(ActionType.DELETE_LEVEL, planMenu);
    planMenu.addSeparator();
    addActionToMenu(ActionType.ZOOM_IN, planMenu);
    addActionToMenu(ActionType.ZOOM_OUT, planMenu);
    planMenu.addSeparator();
    addActionToMenu(ActionType.EXPORT_TO_SVG, planMenu);

    // Create 3D Preview menu
    JMenu preview3DMenu = new JMenu(this.menuActionMap.get(MenuActionType.VIEW_3D_MENU));
    addToggleActionToMenu(ActionType.VIEW_FROM_TOP, true, preview3DMenu);
    addToggleActionToMenu(ActionType.VIEW_FROM_OBSERVER, true, preview3DMenu);
    addActionToMenu(ActionType.MODIFY_OBSERVER, preview3DMenu);
    addActionToMenu(ActionType.STORE_POINT_OF_VIEW, preview3DMenu);
    JMenu goToPointOfViewMenu = createGoToPointOfViewMenu(home, preferences, controller);
    if (goToPointOfViewMenu != null) {
      preview3DMenu.add(goToPointOfViewMenu);
    }
    addActionToMenu(ActionType.DELETE_POINTS_OF_VIEW, preview3DMenu);
    preview3DMenu.addSeparator();
    JMenuItem attachDetach3DViewMenuItem = createAttachDetach3DViewMenuItem(controller, false);
    if (attachDetach3DViewMenuItem != null) {
      preview3DMenu.add(attachDetach3DViewMenuItem);
    }
    addToggleActionToMenu(ActionType.DISPLAY_ALL_LEVELS, true, preview3DMenu);
    addToggleActionToMenu(ActionType.DISPLAY_SELECTED_LEVEL, true, preview3DMenu);
    addActionToMenu(ActionType.MODIFY_3D_ATTRIBUTES, preview3DMenu);
    preview3DMenu.addSeparator();
    addActionToMenu(ActionType.CREATE_PHOTO, preview3DMenu);
    addActionToMenu(ActionType.CREATE_PHOTOS_AT_POINTS_OF_VIEW, preview3DMenu);
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
    if (controller.getPlanController().getView() != null) {
      menuBar.add(planMenu);
    }
    if (controller.getHomeController3D().getView() != null) {
      menuBar.add(preview3DMenu);
    }
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
   * Adds the given action to <code>menu</code>.
   */
  private void addActionToMenu(ActionType actionType, JMenu menu) {
    addActionToMenu(actionType, false, menu);
  }

  /**
   * Adds the given action to <code>menu</code>.
   */
  private void addActionToMenu(ActionType actionType, 
                               boolean popup,
                               JMenu menu) {
    Action action = getActionMap().get(actionType);
    if (action != null && action.getValue(Action.NAME) != null) {
      menu.add(popup 
          ? new ResourceAction.PopupMenuItemAction(action)
          : new ResourceAction.MenuItemAction(action));
    }
  }

  /**
   * Adds to <code>menu</code> the menu item matching the given <code>actionType</code>.
   */
  private void addToggleActionToMenu(ActionType actionType,
                                     boolean radioButton,
                                     JMenu menu) {
    addToggleActionToMenu(actionType, false, radioButton, menu);
  }

  /**
   * Adds to <code>menu</code> the menu item matching the given <code>actionType</code>.
   */
  private void addToggleActionToMenu(ActionType actionType,
                                     boolean popup,
                                     boolean radioButton,
                                     JMenu menu) {
    Action action = getActionMap().get(actionType);
    if (action != null && action.getValue(Action.NAME) != null) {
      menu.add(createToggleMenuItem(action, popup, radioButton));
    }
  }

  /**
   * Creates a menu item for a toggle action.
   */
  private JMenuItem createToggleMenuItem(Action action, 
                                         boolean popup,
                                         boolean radioButton) {
    JMenuItem menuItem;
    if (radioButton) {
      menuItem = new JRadioButtonMenuItem();
    } else {
      menuItem = new JCheckBoxMenuItem();
    }
    // Configure model
    menuItem.setModel((JToggleButton.ToggleButtonModel)action.getValue(ResourceAction.TOGGLE_BUTTON_MODEL));
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
    if (action != null && action.getValue(Action.NAME) != null) {
      menu.add(new ResourceAction.PopupMenuItemAction(action));
    }
  }

  /**
   * Adds to <code>menu</code> the menu item matching the given <code>actionType</code> 
   * and returns <code>true</code> if it was added.
   */
  private void addToggleActionToPopupMenu(ActionType actionType,
                                          boolean radioButton,
                                          JPopupMenu menu) {
    Action action = getActionMap().get(actionType);
    if (action != null && action.getValue(Action.NAME) != null) {
      menu.add(createToggleMenuItem(action, true, radioButton));
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
   * Returns align or distribute menu.
   */
  private JMenu createAlignOrDistributeMenu(final Home home,
                                            final UserPreferences preferences,
                                            boolean popup) {
    JMenu alignOrDistributeMenu = new JMenu(this.menuActionMap.get(MenuActionType.ALIGN_OR_DISTRIBUTE_MENU));    
    addActionToMenu(ActionType.ALIGN_FURNITURE_ON_TOP, popup, alignOrDistributeMenu);
    addActionToMenu(ActionType.ALIGN_FURNITURE_ON_BOTTOM, popup, alignOrDistributeMenu);
    addActionToMenu(ActionType.ALIGN_FURNITURE_ON_LEFT, popup, alignOrDistributeMenu);
    addActionToMenu(ActionType.ALIGN_FURNITURE_ON_RIGHT, popup, alignOrDistributeMenu);
    addActionToMenu(ActionType.ALIGN_FURNITURE_ON_FRONT_SIDE, popup, alignOrDistributeMenu);
    addActionToMenu(ActionType.ALIGN_FURNITURE_ON_BACK_SIDE, popup, alignOrDistributeMenu);
    addActionToMenu(ActionType.ALIGN_FURNITURE_ON_LEFT_SIDE, popup, alignOrDistributeMenu);
    addActionToMenu(ActionType.ALIGN_FURNITURE_ON_RIGHT_SIDE, popup, alignOrDistributeMenu);
    addActionToMenu(ActionType.ALIGN_FURNITURE_SIDE_BY_SIDE, popup, alignOrDistributeMenu);
    addActionToMenu(ActionType.DISTRIBUTE_FURNITURE_HORIZONTALLY, popup, alignOrDistributeMenu);
    addActionToMenu(ActionType.DISTRIBUTE_FURNITURE_VERTICALLY, popup, alignOrDistributeMenu);
    return alignOrDistributeMenu;
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
    addActionToMap(ActionType.SORT_HOME_FURNITURE_BY_LEVEL, 
        sortActions, HomePieceOfFurniture.SortableProperty.LEVEL);
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
    if (action != null && action.getValue(Action.NAME) != null) {
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
    addActionToMap(ActionType.DISPLAY_HOME_FURNITURE_LEVEL, 
        displayPropertyActions, HomePieceOfFurniture.SortableProperty.LEVEL);
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
    if (unlockBasePlanAction != null
        && unlockBasePlanAction.getValue(Action.NAME) != null
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
    if (unlockBasePlanAction != null
        && unlockBasePlanAction.getValue(Action.NAME) != null
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
    addToggleActionToMenu(ActionType.TOGGLE_BOLD_STYLE, popup, false, modifyTextStyleMenu);
    addToggleActionToMenu(ActionType.TOGGLE_ITALIC_STYLE, popup, false, modifyTextStyleMenu);
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
    if (importBackgroundImageAction != null
        && importBackgroundImageAction.getValue(Action.NAME) != null
        && modifyBackgroundImageAction.getValue(Action.NAME) != null) {
      final JMenuItem importModifyBackgroundImageMenuItem = new JMenuItem(
          createImportModifyBackgroundImageAction(home, popup));
      // Add a listener to home and levels on backgroundImage property change to 
      // switch action according to backgroundImage change
      addBackgroundImageChangeListener(home, new PropertyChangeListener() {
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
   * Adds to home and levels the given listener to follow background image changes.
   */
  private void addBackgroundImageChangeListener(final Home home, final PropertyChangeListener listener) {
    home.addPropertyChangeListener(Home.Property.BACKGROUND_IMAGE, listener);    
    home.addPropertyChangeListener(Home.Property.SELECTED_LEVEL, listener);
    final PropertyChangeListener levelChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          if (Level.Property.BACKGROUND_IMAGE.name().equals(ev.getPropertyName())) {
            listener.propertyChange(ev);
          }
        }
      };
    for (Level level : this.home.getLevels()) {
      level.addPropertyChangeListener(levelChangeListener);
    }
    this.home.addLevelsListener(new CollectionListener<Level>() {
        public void collectionChanged(CollectionEvent<Level> ev) {
          switch (ev.getType()) {
            case ADD :
              ev.getItem().addPropertyChangeListener(levelChangeListener);
              break;
            case DELETE :
              ev.getItem().removePropertyChangeListener(levelChangeListener);
              break;
          }
        }
      });
  }
  
  /**
   * Returns the action active on Import / Modify menu item.
   */
  private Action createImportModifyBackgroundImageAction(Home home, boolean popup) {
    BackgroundImage backgroundImage = home.getSelectedLevel() != null
        ? home.getSelectedLevel().getBackgroundImage()
        : home.getBackgroundImage();
    ActionType backgroundImageActionType = backgroundImage == null 
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
    if (hideBackgroundImageAction != null
        && hideBackgroundImageAction.getValue(Action.NAME) != null
        && showBackgroundImageAction.getValue(Action.NAME) != null) {
      final JMenuItem hideShowBackgroundImageMenuItem = new JMenuItem(
          createHideShowBackgroundImageAction(home, popup));
      // Add a listener to home and levels on backgroundImage property change to 
      // switch action according to backgroundImage change
      addBackgroundImageChangeListener(home, new PropertyChangeListener() {
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
    BackgroundImage backgroundImage = home.getSelectedLevel() != null
        ? home.getSelectedLevel().getBackgroundImage()
        : home.getBackgroundImage();
    ActionType backgroundImageActionType = backgroundImage == null || backgroundImage.isVisible()        
        ? ActionType.HIDE_BACKGROUND_IMAGE
        : ActionType.SHOW_BACKGROUND_IMAGE;
    Action backgroundImageAction = getActionMap().get(backgroundImageActionType);
    return popup 
        ? new ResourceAction.PopupMenuItemAction(backgroundImageAction)
        : new ResourceAction.MenuItemAction(backgroundImageAction);
  }
  
  /**
   * Returns Go to point of view menu.
   */
  private JMenu createGoToPointOfViewMenu(final Home home,
                                          UserPreferences preferences,
                                          final HomeController controller) {
    Action goToPointOfViewAction = this.menuActionMap.get(MenuActionType.GO_TO_POINT_OF_VIEW);
    if (goToPointOfViewAction.getValue(Action.NAME) != null) {
      final JMenu goToPointOfViewMenu = new JMenu(goToPointOfViewAction);
      updateGoToPointOfViewMenu(goToPointOfViewMenu, home, controller);
      home.addPropertyChangeListener(Home.Property.STORED_CAMERAS, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              updateGoToPointOfViewMenu(goToPointOfViewMenu, home, controller);
            }
          });
      return goToPointOfViewMenu;
    } else {
      return null;
    }
  }
  
  /**
   * Updates Go to point of view menu items from the cameras stored in home. 
   */
  private void updateGoToPointOfViewMenu(JMenu goToPointOfViewMenu, 
                                         Home home,
                                         final HomeController controller) {
    List<Camera> storedCameras = home.getStoredCameras();
    goToPointOfViewMenu.removeAll();
    if (storedCameras.isEmpty()) {
      goToPointOfViewMenu.setEnabled(false);
      goToPointOfViewMenu.add(new ResourceAction(preferences, HomePane.class, "NoStoredPointOfView", false));
    } else {
      goToPointOfViewMenu.setEnabled(true);
      for (final Camera camera : storedCameras) {
        goToPointOfViewMenu.add(
            new AbstractAction(camera.getName()) {
              public void actionPerformed(ActionEvent e) {
                controller.getHomeController3D().goToCamera(camera);
              }
            });
      }
    }
  }

  /**
   * Returns Attach / Detach menu item for the 3D view.
   */
  private JMenuItem createAttachDetach3DViewMenuItem(final HomeController controller, 
                                                     final boolean popup) {
    ActionMap actionMap = getActionMap();
    Action display3DViewInSeparateWindowAction = actionMap.get(ActionType.DETACH_3D_VIEW);
    Action display3DViewInMainWindowAction = actionMap.get(ActionType.ATTACH_3D_VIEW);
    if (display3DViewInSeparateWindowAction != null
        && display3DViewInSeparateWindowAction.getValue(Action.NAME) != null
        && display3DViewInMainWindowAction.getValue(Action.NAME) != null) {
      final JMenuItem attachDetach3DViewMenuItem = new JMenuItem(
          createAttachDetach3DViewAction(controller, popup));
      // Add a listener to 3D view to switch action when its parent changes
      JComponent view3D = (JComponent)controller.getHomeController3D().getView();
      view3D.addAncestorListener(new AncestorListener() {        
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
    if (!OperatingSystem.isMacOSX()) {
      addActionToToolBar(ActionType.PREFERENCES, toolBar);
    }
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
   
    addToggleActionToToolBar(ActionType.SELECT, toolBar);
    addToggleActionToToolBar(ActionType.PAN, toolBar);
    addToggleActionToToolBar(ActionType.CREATE_WALLS, toolBar);
    addToggleActionToToolBar(ActionType.CREATE_ROOMS, toolBar);
    addToggleActionToToolBar(ActionType.CREATE_DIMENSION_LINES, toolBar);
    addToggleActionToToolBar(ActionType.CREATE_LABELS, toolBar);
    toolBar.add(Box.createRigidArea(new Dimension(2, 2)));
    
    addActionToToolBar(ActionType.INCREASE_TEXT_SIZE, toolBar);
    addActionToToolBar(ActionType.DECREASE_TEXT_SIZE, toolBar);
    addToggleActionToToolBar(ActionType.TOGGLE_BOLD_STYLE, toolBar);
    addToggleActionToToolBar(ActionType.TOGGLE_ITALIC_STYLE, toolBar);
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
        addActionToToolBar(new ResourceAction.ToolBarAction(pluginAction), toolBar);
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

    if (OperatingSystem.isMacOSXLeopardOrSuperior() && OperatingSystem.isJavaVersionGreaterOrEqual("1.7")) {
      // Reduce tool bar height to balance segmented buttons with higher insets 
      toolBar.setPreferredSize(new Dimension(0, toolBar.getPreferredSize().height - 4));
    }

    return toolBar;
  }

  /**
   * Adds to tool bar the button matching the given <code>actionType</code> 
   * and returns <code>true</code> if it was added.
   */
  private void addToggleActionToToolBar(ActionType actionType,
                                        JToolBar toolBar) {
    Action action = getActionMap().get(actionType);
    if (action!= null && action.getValue(Action.NAME) != null) {
      Action toolBarAction = new ResourceAction.ToolBarAction(action);    
      JToggleButton toggleButton;
      if (OperatingSystem.isMacOSXLeopardOrSuperior() && OperatingSystem.isJavaVersionGreaterOrEqual("1.7")) {
        // Use higher insets to ensure the top and bottom of segmented buttons are correctly drawn 
        toggleButton = new JToggleButton(toolBarAction) {
            @Override
            public Insets getInsets() {
              Insets insets = super.getInsets();
              insets.top += 3;
              insets.bottom += 3;
              return insets;
            }
          };
      } else {
        toggleButton = new JToggleButton(toolBarAction);
      }
      toggleButton.setModel((JToggleButton.ToggleButtonModel)action.getValue(ResourceAction.TOGGLE_BUTTON_MODEL));
      toolBar.add(toggleButton);
    }
  }

  /**
   * Adds to tool bar the button matching the given <code>actionType</code>. 
   */
  private void addActionToToolBar(ActionType actionType,
                                  JToolBar toolBar) {
    Action action = getActionMap().get(actionType);
    if (action!= null && action.getValue(Action.NAME) != null) {
      addActionToToolBar(new ResourceAction.ToolBarAction(action), toolBar);
    }
  }
    
  /**
   * Adds to tool bar the button matching the given <code>action</code>. 
   */
  private void addActionToToolBar(Action action,
                                  JToolBar toolBar) {
    if (OperatingSystem.isMacOSXLeopardOrSuperior() && OperatingSystem.isJavaVersionGreaterOrEqual("1.7")) {
      // Add a button with higher insets to ensure the top and bottom of segmented buttons are correctly drawn 
      toolBar.add(new JButton(new ResourceAction.ToolBarAction(action)) {
          @Override
          public Insets getInsets() {
            Insets insets = super.getInsets();
            insets.top += 3;
            insets.bottom += 3;
            return insets;
          }
        });
    } else {
      toolBar.add(new JButton(new ResourceAction.ToolBarAction(action)));
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
    boolean dragAndDropWithTransferHandlerSupported;
    try {
      // Don't use transfer handlers for drag and drop with Plugin2 under Mac OS X or when in an unsigned applet
      dragAndDropWithTransferHandlerSupported = !Boolean.getBoolean("com.eteks.sweethome3d.dragAndDropWithoutTransferHandler");
    } catch (AccessControlException ex) {
      dragAndDropWithTransferHandlerSupported = false;
    }
    
    JComponent catalogView = (JComponent)this.controller.getFurnitureCatalogController().getView();
    JComponent furnitureView = (JComponent)this.controller.getFurnitureController().getView();
    JComponent planView = (JComponent)this.controller.getPlanController().getView();
    if (enabled) {
      if (catalogView != null) {
        catalogView.setTransferHandler(this.catalogTransferHandler);
      }
      if (furnitureView != null) {
        furnitureView.setTransferHandler(this.furnitureTransferHandler);
        if (furnitureView instanceof Scrollable) {
          ((JViewport)furnitureView.getParent()).setTransferHandler(this.furnitureTransferHandler);
        }
      }
      if (planView != null) {
        planView.setTransferHandler(this.planTransferHandler);
      }
      if (!dragAndDropWithTransferHandlerSupported) {
        if (catalogView != null) {
          // Check if furniture catalog is handled by a subcomponent
          List<JViewport> viewports = SwingTools.findChildren(catalogView, JViewport.class);
          JComponent catalogComponent;
          if (viewports.size() > 0) {
            catalogComponent = (JComponent)viewports.get(0).getView();
          } else {
            catalogComponent = catalogView;
          }
          if (this.furnitureCatalogDragAndDropListener == null) {
            this.furnitureCatalogDragAndDropListener = createFurnitureCatalogMouseListener();
          }
          catalogComponent.addMouseListener(this.furnitureCatalogDragAndDropListener);
          catalogComponent.addMouseMotionListener(this.furnitureCatalogDragAndDropListener);
        }
      }
    } else {
      if (catalogView != null) {
        catalogView.setTransferHandler(null);
      }
      if (furnitureView != null) {
        furnitureView.setTransferHandler(null);
        if (furnitureView instanceof Scrollable) {
          ((JViewport)furnitureView.getParent()).setTransferHandler(null);
        }
      }
      if (planView != null) {
        planView.setTransferHandler(null);
      }
      if (!dragAndDropWithTransferHandlerSupported) {
        if (catalogView != null) {
          List<JViewport> viewports = SwingTools.findChildren(catalogView, JViewport.class);
          JComponent catalogComponent;
          if (viewports.size() > 0) {
            catalogComponent = (JComponent)viewports.get(0).getView();
          } else {
            catalogComponent = catalogView;
          }
          catalogComponent.removeMouseListener(this.furnitureCatalogDragAndDropListener);
          catalogComponent.removeMouseMotionListener(this.furnitureCatalogDragAndDropListener);
        }
      }        
    }
    this.transferHandlerEnabled = enabled;
  }

  /**
   * Returns a mouse listener for catalog that acts as catalog view, furniture view and plan transfer handlers 
   * for drag and drop operations.
   */
  private MouseInputAdapter createFurnitureCatalogMouseListener() {
    return new MouseInputAdapter() {
        private CatalogPieceOfFurniture selectedPiece;
        private TransferHandler         transferHandler;
        private boolean                 autoscrolls;
        private Cursor                  previousCursor;
        private View                    previousView;
    
        @Override
        public void mousePressed(MouseEvent ev) {
          if (SwingUtilities.isLeftMouseButton(ev)) {
            List<CatalogPieceOfFurniture> selectedFurniture = controller.getFurnitureCatalogController().getSelectedFurniture();
            if (selectedFurniture.size() > 0) {
              JComponent source = (JComponent)ev.getSource();
              this.transferHandler = source.getTransferHandler();
              source.setTransferHandler(null);
              this.autoscrolls = source.getAutoscrolls();
              source.setAutoscrolls(false);
              this.selectedPiece = selectedFurniture.get(0);
              this.previousCursor = null;
              this.previousView = null;
            }
          }
        }
        
        @Override
        public void mouseDragged(MouseEvent ev) {
          if (SwingUtilities.isLeftMouseButton(ev)
              && this.selectedPiece != null) {
            // Force selection again
            List<CatalogPieceOfFurniture> emptyList = Collections.emptyList();
            controller.getFurnitureCatalogController().setSelectedFurniture(emptyList);
            controller.getFurnitureCatalogController().setSelectedFurniture(Arrays.asList(new CatalogPieceOfFurniture [] {this.selectedPiece}));
            
            List<Selectable> transferredFurniture = Arrays.asList(
                new Selectable [] {controller.getFurnitureController().createHomePieceOfFurniture(this.selectedPiece)});
            View view;
            float [] pointInView = getPointInPlanView(ev, transferredFurniture);
            if (pointInView != null) {
              view = controller.getPlanController().getView();
            } else {
              view = controller.getFurnitureController().getView();
              pointInView = getPointInFurnitureView(ev);
            }

            if (this.previousView != view) {
              if (this.previousView != null) {
                if (this.previousView == controller.getPlanController().getView()) {
                  controller.getPlanController().stopDraggedItems();
                }
                ((JComponent)this.previousView).setCursor(this.previousCursor);
                this.previousCursor = null;
                this.previousView = null;
              }
              if (view != null) {
                JComponent component = (JComponent)view;
                this.previousCursor = component.getCursor();
                this.previousView = view;
                component.setCursor(DragSource.DefaultCopyDrop);
                if (component.getParent() instanceof JViewport) {
                  ((JViewport)component.getParent()).setCursor(DragSource.DefaultCopyDrop);
                }
                if (view == controller.getPlanController().getView()) {
                  controller.getPlanController().startDraggedItems(transferredFurniture, pointInView [0], pointInView [1]);
                }
              }
            } else if (pointInView != null) {
              controller.getPlanController().moveMouse(pointInView [0], pointInView [1]);
            }
          }
        }
        
        private float [] getPointInPlanView(MouseEvent ev, List<Selectable> transferredFurniture) {
          PlanView planView = controller.getPlanController().getView();
          if (planView != null) {
            JComponent planComponent = (JComponent)planView;
            Point pointInPlanComponent = SwingUtilities.convertPoint(ev.getComponent(), ev.getPoint(), planComponent);
            if (planComponent.getParent() instanceof JViewport 
                    && ((JViewport)planComponent.getParent()).contains(
                        SwingUtilities.convertPoint(ev.getComponent(), ev.getPoint(), planComponent.getParent()))
                || !(planComponent.getParent() instanceof JViewport)
                    && planView.canImportDraggedItems(transferredFurniture, pointInPlanComponent.x, pointInPlanComponent.y)) {
              return new float [] {planView.convertXPixelToModel(pointInPlanComponent.x), planView.convertYPixelToModel(pointInPlanComponent.y)};
            }
          } 
          return null;
        }
        
        private float [] getPointInFurnitureView(MouseEvent ev) {
          View furnitureView = controller.getFurnitureController().getView();
          if (furnitureView != null) {
            JComponent furnitureComponent = (JComponent)furnitureView;
            Point point = SwingUtilities.convertPoint(ev.getComponent(), ev.getX(), ev.getY(), 
                furnitureComponent.getParent() instanceof JViewport
                   ? furnitureComponent.getParent()
                   : furnitureComponent);
            if (furnitureComponent.getParent() instanceof JViewport 
                && ((JViewport)furnitureComponent.getParent()).contains(point)
            || !(furnitureComponent.getParent() instanceof JViewport)
                && furnitureComponent.contains(point)) {
              return new float [] {0, 0};
            }
          } 
          return null;
        }
        
        @Override
        public void mouseReleased(MouseEvent ev) {
          if (SwingUtilities.isLeftMouseButton(ev)
              && this.selectedPiece != null) {
            List<Selectable> transferredFurniture = Arrays.asList(
                new Selectable [] {controller.getFurnitureController().createHomePieceOfFurniture(this.selectedPiece)});
            View view;
            float [] pointInView = getPointInPlanView(ev, transferredFurniture);
            if (pointInView != null) {
              controller.getPlanController().stopDraggedItems();
              view = controller.getPlanController().getView();
            } else {
              view = controller.getFurnitureController().getView();
              pointInView = getPointInFurnitureView(ev);
            }
            if (pointInView != null) {
              controller.drop(transferredFurniture, view, pointInView [0], pointInView [1]);
              ((JComponent)this.previousView).setCursor(this.previousCursor);
            }
            this.selectedPiece = null;
            JComponent source = (JComponent)ev.getSource();
            source.setTransferHandler(this.transferHandler);
            source.setAutoscrolls(this.autoscrolls);
          }
        }
      };
  }

  /**
   * Returns the main pane with catalog tree, furniture table and plan pane. 
   */
  private JComponent createMainPane(Home home, UserPreferences preferences, 
                                    HomeController controller) {
    final JComponent catalogFurniturePane = createCatalogFurniturePane(home, preferences, controller);
    final JComponent planView3DPane = createPlanView3DPane(home, preferences, controller);

    if (catalogFurniturePane == null) {
      return planView3DPane;
    } else if (planView3DPane == null) {
      return catalogFurniturePane;
    } else {
      final JSplitPane mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, catalogFurniturePane, planView3DPane);
      // Set default divider location
      mainPane.setDividerLocation(360);
      configureSplitPane(mainPane, home, 
          MAIN_PANE_DIVIDER_LOCATION_VISUAL_PROPERTY, 0.3, true, controller);
      return mainPane;
    }
  }

  /**
   * Configures <code>splitPane</code> divider location. 
   * If <code>dividerLocationProperty</code> visual property exists in <code>home</code>,
   * its value will be used, otherwise the given resize weight will be used.
   */
  private void configureSplitPane(final JSplitPane splitPane,
                                  Home home,
                                  final String dividerLocationProperty,
                                  final double defaultResizeWeight,
                                  boolean showBorder,
                                  final HomeController controller) {
    splitPane.setContinuousLayout(true);
    splitPane.setOneTouchExpandable(true);
    splitPane.setResizeWeight(defaultResizeWeight);
    if (!showBorder) {
      splitPane.setBorder(null);
    }
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
                    if (focusedComponent != null) {
                      focusedComponent.requestFocusInWindow();
                    }
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
    if (catalogView != null) {
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
      SwingTools.hideDisabledMenuItems(catalogViewPopup);
      catalogView.setComponentPopupMenu(catalogViewPopup);
  
      preferences.addPropertyChangeListener(UserPreferences.Property.FURNITURE_CATALOG_VIEWED_IN_TREE, 
          new FurnitureCatalogViewChangeListener(this, catalogView));
      if (catalogView instanceof Scrollable) {
        catalogView = SwingTools.createScrollPane(catalogView);
      }
    }
    
    // Configure furniture view
    JComponent furnitureView = (JComponent)controller.getFurnitureController().getView();
    if (furnitureView != null) {
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
      furnitureViewPopup.add(createAlignOrDistributeMenu(home, preferences, true));
      furnitureViewPopup.addSeparator();
      furnitureViewPopup.add(createFurnitureSortMenu(home, preferences));
      furnitureViewPopup.add(createFurnitureDisplayPropertyMenu(home, preferences));
      furnitureViewPopup.addSeparator();
      addActionToPopupMenu(ActionType.EXPORT_TO_CSV, furnitureViewPopup);
      SwingTools.hideDisabledMenuItems(furnitureViewPopup);
      furnitureView.setComponentPopupMenu(furnitureViewPopup);
  
      if (furnitureView instanceof Scrollable) {
        JScrollPane furnitureScrollPane = SwingTools.createScrollPane(furnitureView);
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
    }

    if (catalogView == null) {
      return furnitureView;
    } else if (furnitureView == null) {
      return catalogView;
    } else {
      // Create a split pane that displays both components
      JSplitPane catalogFurniturePane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
          catalogView, furnitureView);
      catalogFurniturePane.setBorder(null);
      catalogFurniturePane.setMinimumSize(new Dimension());
      configureSplitPane(catalogFurniturePane, home, 
          CATALOG_PANE_DIVIDER_LOCATION_VISUAL_PROPERTY, 0.5, false, controller);
      return catalogFurniturePane;
    }
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
        if (oldFurnitureCatalogView != null) {
          boolean transferHandlerEnabled = homePane.transferHandlerEnabled; 
          homePane.setTransferEnabled(false);
          JComponent newFurnitureCatalogView = (JComponent)homePane.controller.getFurnitureCatalogController().getView();
          newFurnitureCatalogView.setComponentPopupMenu(oldFurnitureCatalogView.getComponentPopupMenu());
          homePane.setTransferEnabled(transferHandlerEnabled);
          JComponent splitPaneTopComponent = newFurnitureCatalogView; 
          if (newFurnitureCatalogView instanceof Scrollable) {
            splitPaneTopComponent = SwingTools.createScrollPane(newFurnitureCatalogView);
          } else {
            splitPaneTopComponent = newFurnitureCatalogView;
          }
          ((JSplitPane)SwingUtilities.getAncestorOfClass(JSplitPane.class, oldFurnitureCatalogView)).
              setTopComponent(splitPaneTopComponent);
          this.furnitureCatalogView = new WeakReference<JComponent>(newFurnitureCatalogView);
        }
      }
    }
  }
  
  /**
   * Returns the plan view and 3D view pane. 
   */
  private JComponent createPlanView3DPane(final Home home, UserPreferences preferences, 
                                          final HomeController controller) {
    JComponent planView = (JComponent)controller.getPlanController().getView();
    if (planView != null) {
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
      Action selectObjectAction = this.menuActionMap.get(MenuActionType.SELECT_OBJECT_MENU);
      JMenu selectObjectMenu;
      if (selectObjectAction.getValue(Action.NAME) != null) {
        selectObjectMenu = new JMenu(selectObjectAction);
        planViewPopup.add(selectObjectMenu);
      } else {
        selectObjectMenu = null;
      }
      addActionToPopupMenu(ActionType.SELECT_ALL, planViewPopup);
      planViewPopup.addSeparator();
      addToggleActionToPopupMenu(ActionType.SELECT, true, planViewPopup);
      addToggleActionToPopupMenu(ActionType.PAN, true, planViewPopup);
      addToggleActionToPopupMenu(ActionType.CREATE_WALLS, true, planViewPopup);
      addToggleActionToPopupMenu(ActionType.CREATE_ROOMS, true, planViewPopup);
      addToggleActionToPopupMenu(ActionType.CREATE_DIMENSION_LINES, true, planViewPopup);
      addToggleActionToPopupMenu(ActionType.CREATE_LABELS, true, planViewPopup);
      planViewPopup.addSeparator();
      JMenuItem lockUnlockBasePlanMenuItem = createLockUnlockBasePlanMenuItem(home, true);
      if (lockUnlockBasePlanMenuItem != null) {
        planViewPopup.add(lockUnlockBasePlanMenuItem);
      }
      addActionToPopupMenu(ActionType.MODIFY_FURNITURE, planViewPopup);
      addActionToPopupMenu(ActionType.GROUP_FURNITURE, planViewPopup);
      addActionToPopupMenu(ActionType.UNGROUP_FURNITURE, planViewPopup);
      planViewPopup.add(createAlignOrDistributeMenu(home, preferences, true));
      addActionToPopupMenu(ActionType.MODIFY_COMPASS, planViewPopup);
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
      addActionToPopupMenu(ActionType.ADD_LEVEL, planViewPopup);
      addActionToPopupMenu(ActionType.MODIFY_LEVEL, planViewPopup);
      addActionToPopupMenu(ActionType.DELETE_LEVEL, planViewPopup);
      planViewPopup.addSeparator();
      addActionToPopupMenu(ActionType.ZOOM_OUT, planViewPopup);
      addActionToPopupMenu(ActionType.ZOOM_IN, planViewPopup);
      planViewPopup.addSeparator();
      addActionToPopupMenu(ActionType.EXPORT_TO_SVG, planViewPopup);
      SwingTools.hideDisabledMenuItems(planViewPopup);
      if (selectObjectMenu != null) {
        // Add a second popup listener to manage Select object sub menu before the menu is hidden when empty
        addSelectObjectMenuItems(selectObjectMenu, controller.getPlanController(), preferences);
      }
      planView.setComponentPopupMenu(planViewPopup);
      
      final JScrollPane planScrollPane;
      if (planView instanceof Scrollable) {
        planView = planScrollPane
                 = SwingTools.createScrollPane(planView);
      } else {
        List<JScrollPane> scrollPanes = SwingTools.findChildren(planView, JScrollPane.class);
        if (scrollPanes.size() == 1) {
          planScrollPane = scrollPanes.get(0);
        } else {
          planScrollPane = null;
        }
      }
      
      if (planScrollPane != null) {
        setPlanRulersVisible(planScrollPane, controller, preferences.isRulersVisible());
        if (planScrollPane.getCorner(JScrollPane.UPPER_LEADING_CORNER) == null) {
          final JComponent lockUnlockBasePlanButton = createLockUnlockBasePlanButton(home);
          if (lockUnlockBasePlanButton != null) {
            planScrollPane.setCorner(JScrollPane.UPPER_LEADING_CORNER, lockUnlockBasePlanButton);
            planScrollPane.addPropertyChangeListener("componentOrientation", new PropertyChangeListener() {
              public void propertyChange(PropertyChangeEvent ev) {
                if (lockUnlockBasePlanButton.getParent() != null) {
                  planScrollPane.setCorner(JScrollPane.UPPER_LEADING_CORNER, lockUnlockBasePlanButton);
                }
              }
            });
          }
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
      }
    }

    // Configure 3D view
    JComponent view3D = (JComponent)controller.getHomeController3D().getView();
    if (view3D != null) {
      view3D.setPreferredSize(planView != null 
          ? planView.getPreferredSize()
          : new Dimension(400, 400));
      view3D.setMinimumSize(new Dimension());
      
      // Create 3D view popup menu
      JPopupMenu view3DPopup = new JPopupMenu();
      addToggleActionToPopupMenu(ActionType.VIEW_FROM_TOP, true, view3DPopup);
      addToggleActionToPopupMenu(ActionType.VIEW_FROM_OBSERVER, true, view3DPopup);
      addActionToPopupMenu(ActionType.MODIFY_OBSERVER, view3DPopup);
      addActionToPopupMenu(ActionType.STORE_POINT_OF_VIEW, view3DPopup);
      JMenu goToPointOfViewMenu = createGoToPointOfViewMenu(home, preferences, controller);
      if (goToPointOfViewMenu != null) {
        view3DPopup.add(goToPointOfViewMenu);
      }
      addActionToPopupMenu(ActionType.DELETE_POINTS_OF_VIEW, view3DPopup);
      view3DPopup.addSeparator();
      JMenuItem attachDetach3DViewMenuItem = createAttachDetach3DViewMenuItem(controller, true);
      if (attachDetach3DViewMenuItem != null) {
        view3DPopup.add(attachDetach3DViewMenuItem);
      }
      addToggleActionToPopupMenu(ActionType.DISPLAY_ALL_LEVELS, true, view3DPopup);
      addToggleActionToPopupMenu(ActionType.DISPLAY_SELECTED_LEVEL, true, view3DPopup);
      addActionToPopupMenu(ActionType.MODIFY_3D_ATTRIBUTES, view3DPopup);
      view3DPopup.addSeparator();
      addActionToPopupMenu(ActionType.CREATE_PHOTO, view3DPopup);
      addActionToPopupMenu(ActionType.CREATE_PHOTOS_AT_POINTS_OF_VIEW, view3DPopup);
      addActionToPopupMenu(ActionType.CREATE_VIDEO, view3DPopup);
      view3DPopup.addSeparator();
      addActionToPopupMenu(ActionType.EXPORT_TO_OBJ, view3DPopup);
      SwingTools.hideDisabledMenuItems(view3DPopup);
      view3D.setComponentPopupMenu(view3DPopup);
      
      if (view3D instanceof Scrollable) {
        view3D = SwingTools.createScrollPane(view3D);
      }
    
      JComponent planView3DPane;
      Boolean detachedView3DProperty = (Boolean)home.getVisualProperty(view3D.getClass().getName() + DETACHED_VIEW_VISUAL_PROPERTY);
      boolean detachedView3D = detachedView3DProperty != null && detachedView3DProperty.booleanValue();        
      if (planView != null) {
        // Create a split pane that displays both components
        final JSplitPane planView3DSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, planView, view3D);
        planView3DSplitPane.setMinimumSize(new Dimension());
        configureSplitPane((JSplitPane)planView3DSplitPane, home, 
            PLAN_PANE_DIVIDER_LOCATION_VISUAL_PROPERTY, 0.5, false, controller);

        final Integer dividerLocation = (Integer)home.getVisualProperty(PLAN_PANE_DIVIDER_LOCATION_VISUAL_PROPERTY);
        if (OperatingSystem.isMacOSX() 
            && !detachedView3D && dividerLocation != null && dividerLocation > 2) {
          // Under Mac OS X, ensure that the 3D view of an existing home will be displayed during a while
          // to avoid a freeze when the 3D view was saved as hidden and then the window displaying the 3D view is enlarged 
          planView3DSplitPane.addAncestorListener(new AncestorListener() {
              public void ancestorAdded(AncestorEvent event) {
                planView3DSplitPane.removeAncestorListener(this);
                if (planView3DSplitPane.getRightComponent().getHeight() == 0) {
                  // If the 3D view is invisible, make it appear during a while
                  planView3DSplitPane.setDividerLocation(dividerLocation - 2);
                  new Timer(1000, new ActionListener() {
                      public void actionPerformed(ActionEvent ev) {
                        ((Timer)ev.getSource()).stop();
                        planView3DSplitPane.setDividerLocation(dividerLocation);
                      }
                    }).start();
                }
              }
              
              public void ancestorRemoved(AncestorEvent event) {
              }
              
              public void ancestorMoved(AncestorEvent event) {
              }
            });
        }
        
        planView3DPane = planView3DSplitPane;
      } else {
        planView3DPane = view3D;
      }
    
      // Detach 3D view if it was detached when saved and its dialog can be viewed in one of the screen devices
      if (detachedView3D) {
        // Check 3D view can be viewed in one of the available screens      
        final Integer dialogX = (Integer)this.home.getVisualProperty(view3D.getClass().getName() + DETACHED_VIEW_X_VISUAL_PROPERTY);
        final Integer dialogY = (Integer)this.home.getVisualProperty(view3D.getClass().getName() + DETACHED_VIEW_Y_VISUAL_PROPERTY);
        final Integer dialogWidth = (Integer)home.getVisualProperty(view3D.getClass().getName() + DETACHED_VIEW_WIDTH_VISUAL_PROPERTY);
        final Integer dialogHeight = (Integer)home.getVisualProperty(view3D.getClass().getName() + DETACHED_VIEW_HEIGHT_VISUAL_PROPERTY);
        if (dialogX != null && dialogY != null && dialogWidth != null && dialogHeight != null
            && SwingTools.isRectangleVisibleAtScreen(new Rectangle(dialogX, dialogY, dialogWidth, dialogHeight))) {
          EventQueue.invokeLater(new Runnable() {
              public void run() {
                View view3D = controller.getHomeController3D().getView();
                detachView(view3D, dialogX, dialogY, dialogWidth, dialogHeight);
              }
            });
          return planView3DPane;
        }
        if (planView3DPane instanceof JSplitPane) {
          ((JSplitPane)planView3DPane).setDividerLocation(0.5);
        }
        controller.setVisualProperty(view3D.getClass().getName() + DETACHED_VIEW_X_VISUAL_PROPERTY, null);
      }
      
      return planView3DPane;
    } else {
      return planView;
    }    
  }

  /**
   * Adds to the menu a listener that will update the menu items able to select 
   * the selectable items in plan at the location where the menu will be triggered.
   */
  private void addSelectObjectMenuItems(final JMenu            selectObjectMenu, 
                                         final PlanController  planController, 
                                         final UserPreferences preferences) {
    JComponent planView = (JComponent)planController.getView();
    final Point lastMouseMoveLocation = new Point(-1, -1);
    ((JPopupMenu)selectObjectMenu.getParent()).addPopupMenuListener(new PopupMenuListener() {
        @SuppressWarnings({"rawtypes", "unchecked"})
        public void popupMenuWillBecomeVisible(PopupMenuEvent ev) {
          if (lastMouseMoveLocation.getX() >= 0
              && !planController.isModificationState()) {
            final List<Selectable> items = planController.getSelectableItemsAt(
                planController.getView().convertXPixelToModel(lastMouseMoveLocation.x),
                planController.getView().convertYPixelToModel(lastMouseMoveLocation.y));
            // Prepare localized formatters
            Map<Class<? extends Selectable>, SelectableFormat> formatters = 
                new HashMap<Class<? extends Selectable>, SelectableFormat>();            
            formatters.put(Compass.class, new SelectableFormat<Compass>() {
                public String format(Compass compass) {
                  return preferences.getLocalizedString(HomePane.class, "selectObject.compass");
                }
              });
            formatters.put(HomePieceOfFurniture.class, new SelectableFormat<HomePieceOfFurniture>() {
                public String format(HomePieceOfFurniture piece) {
                  if (piece.getName().length() > 0) {
                    return piece.getName();
                  } else {
                    return preferences.getLocalizedString(HomePane.class, "selectObject.furniture");
                  }
                }
              });
            formatters.put(Wall.class, new SelectableFormat<Wall>() {
                public String format(Wall wall) {
                  return preferences.getLocalizedString(HomePane.class, "selectObject.wall", 
                      preferences.getLengthUnit().getFormatWithUnit().format(wall.getLength()));
                }
              });
            formatters.put(Room.class, new SelectableFormat<Room>() {
                public String format(Room room) {
                  String roomInfo = room.getName() != null && room.getName().length() > 0
                      ? room.getName()
                      : (room.isAreaVisible() 
                            ? preferences.getLengthUnit().getAreaFormatWithUnit().format(room.getArea())
                            : "");
                  if (room.isFloorVisible() && !room.isCeilingVisible()) {
                    return preferences.getLocalizedString(HomePane.class, "selectObject.floor", roomInfo);
                  } else if (!room.isFloorVisible() && room.isCeilingVisible()) {
                    return preferences.getLocalizedString(HomePane.class, "selectObject.ceiling", roomInfo);
                  } else {
                    return preferences.getLocalizedString(HomePane.class, "selectObject.room", roomInfo);
                  }
                }
              });
            formatters.put(DimensionLine.class, new SelectableFormat<DimensionLine>() {
                public String format(DimensionLine dimensionLine) {
                  return preferences.getLocalizedString(HomePane.class, "selectObject.dimensionLine", 
                      preferences.getLengthUnit().getFormatWithUnit().format(dimensionLine.getLength()));
                }
              });
            formatters.put(Label.class, new SelectableFormat<Label>() {
                public String format(Label label) {
                  if (label.getText().length() > 0) {
                    return label.getText();
                  } else {
                    return preferences.getLocalizedString(HomePane.class, "selectObject.label");
                  }
                }
              });
            
            for (final Selectable item : items) {
              String format = null;
              for (Map.Entry<Class<? extends Selectable>, SelectableFormat> entry : formatters.entrySet()) {
                if (entry.getKey().isInstance(item)) {
                  format = entry.getValue().format(item);
                  break;
                }
              }
              if (format != null) {
                selectObjectMenu.add(new JMenuItem(new AbstractAction(format) {
                    public void actionPerformed(ActionEvent ev) {
                      planController.selectItem(item);
                    }
                  }));
              }
            }
          }
        }
 
        public void popupMenuWillBecomeInvisible(PopupMenuEvent ev) {
          selectObjectMenu.removeAll();
        }
 
        public void popupMenuCanceled(PopupMenuEvent ev) {
        }
      });
    planView.addMouseMotionListener(new MouseMotionAdapter() {
        @Override
        public void mouseMoved(MouseEvent ev) {
          lastMouseMoveLocation.setLocation(ev.getPoint());
        }
      });      
    planView.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseExited(MouseEvent e) {
          lastMouseMoveLocation.x = -1;
        }
      });
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
    if (view != null) {
      ((JComponent)view).addMouseListener(listener);
      ((JComponent)view).addFocusListener(listener);
    }
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
    final JDialog separateDialog = new JDialog(defaultFrame, defaultFrame.getTitle(), false);
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
    
    // Copy action map and input map to enable shortcuts in the window
    ActionMap actionMap = getActionMap();
    separateDialog.getRootPane().setActionMap(actionMap);
    InputMap inputMap = separateDialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    for (Object key : actionMap.allKeys()) {
      Action action = actionMap.get(key);
      KeyStroke accelerator = (KeyStroke)action.getValue(Action.ACCELERATOR_KEY);
      if (key != ActionType.CLOSE
          && key != ActionType.DETACH_3D_VIEW 
          && accelerator != null) {
        inputMap.put(accelerator, key);
      }
    }

    separateDialog.setBounds(x, y, width, height);
    separateDialog.setLocationByPlatform(!SwingTools.isRectangleVisibleAtScreen(separateDialog.getBounds()));
    separateDialog.setVisible(true);
    
    this.controller.setVisualProperty(view.getClass().getName() + DETACHED_VIEW_VISUAL_PROPERTY, true);
  }
  
  /**
   * Attaches the given <code>view</code> to home view.
   */
  public void attachView(View view) {
    this.controller.setVisualProperty(view.getClass().getName() + DETACHED_VIEW_VISUAL_PROPERTY, false);

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
      ((RootPaneContainer)window).getRootPane().setActionMap(null);
      window.dispose();
    }
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
   * Displays a content chooser open dialog to choose a language library.
   */
  public String showImportLanguageLibraryDialog() {
    return this.controller.getContentManager().showOpenDialog(this, 
        this.preferences.getLocalizedString(HomePane.class, "importLanguageLibraryDialog.title"), 
        ContentManager.ContentType.LANGUAGE_LIBRARY);
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
    JComponent messagePane = createEditorPane(message);
    messagePane.setOpaque(false);
    
    String title = this.preferences.getLocalizedString(HomePane.class, "about.title");
    Icon   icon  = new ImageIcon(HomePane.class.getResource(
        this.preferences.getLocalizedString(HomePane.class, "about.icon")));
    try {      
      String close = this.preferences.getLocalizedString(HomePane.class, "about.close");
      String showLibraries = this.preferences.getLocalizedString(HomePane.class, "about.showLibraries");
      List<Library> libraries = this.preferences.getLibraries();
      if (!libraries.isEmpty()) {
        if (JOptionPane.showOptionDialog(this, messagePane, title, 
              JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE,
              icon, new Object [] {close, showLibraries}, close) == JOptionPane.NO_OPTION) {
          showLibrariesDialog(libraries);
        }
        return;
      }
    } catch (UnsupportedOperationException ex) {
      // Environment doesn't support libraries
    } catch (IllegalArgumentException ex) {
      // Unknown about.close or about.libraries libraries
    }
    JOptionPane.showMessageDialog(this, messagePane, title, JOptionPane.INFORMATION_MESSAGE, icon);
  }

  /**
   * Returns a component able to display message with active links.
   */
  private JComponent createEditorPane(String message) {
    // Use an uneditable editor pane to let user select text in dialog
    JEditorPane messagePane = new JEditorPane("text/html", message);
    messagePane.setEditable(false);
    // Add a listener that displays hyperlinks content in browser
    messagePane.addHyperlinkListener(new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent ev) {
        if (ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          SwingTools.showDocumentInBrowser(ev.getURL()); 
        }
      }
    });
    return messagePane;
  }

  /**
   * Displays the given <code>libraries</code> in a dialog.
   */
  private void showLibrariesDialog(List<Library> libraries) {
    String title = this.preferences.getLocalizedString(HomePane.class, "libraries.title");
    Map<String, String> librariesLabels = new LinkedHashMap<String, String>();
    librariesLabels.put(UserPreferences.FURNITURE_LIBRARY_TYPE, 
        this.preferences.getLocalizedString(HomePane.class, "libraries.furnitureLibraries"));
    librariesLabels.put(UserPreferences.TEXTURES_LIBRARY_TYPE, 
        this.preferences.getLocalizedString(HomePane.class, "libraries.texturesLibraries"));
    librariesLabels.put(UserPreferences.LANGUAGE_LIBRARY_TYPE, 
        this.preferences.getLocalizedString(HomePane.class, "libraries.languageLibraries"));
    librariesLabels.put(PluginManager.PLUGIN_LIBRARY_TYPE, 
        this.preferences.getLocalizedString(HomePane.class, "libraries.plugins"));
    
    JPanel messagePanel = new JPanel(new GridBagLayout());
    int row = 0;
    for (Map.Entry<String, String> librariesEntry : librariesLabels.entrySet()) {
      final List<Library> typeLibraries = new ArrayList<Library>();
      for (Library library : libraries) {
        if (librariesEntry.getKey().equals(library.getType())) {
          typeLibraries.add(library);
        }
      }
      // If there's some library of the given type
      if (!typeLibraries.isEmpty()) {
        // Add a label
        messagePanel.add(new JLabel(librariesEntry.getValue()), new GridBagConstraints(
            0, row++, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
            GridBagConstraints.NONE, new Insets(row == 0 ? 0 : 5, 2, 2, 0), 0, 0));
        // Add a description table
        JTable librariesTable = createLibrariesTable(typeLibraries);
        JScrollPane librariesScrollPane = SwingTools.createScrollPane(librariesTable);
        librariesScrollPane.setPreferredSize(new Dimension(500, 
            OperatingSystem.isWindows() ? 95 : 100));
        messagePanel.add(librariesScrollPane, new GridBagConstraints(
            0, row++, 1, 1, 1, 1, GridBagConstraints.CENTER, 
            GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
      }
    }
    JOptionPane.showMessageDialog(this, messagePanel, title, JOptionPane.PLAIN_MESSAGE);
  }

  /**
   * Returns a table describing each library of the given collection.
   */
  private JTable createLibrariesTable(final List<Library> libraries) {
    AbstractTableModel librariesTableModel = new AbstractTableModel() {
        private String [] columnNames = {
            preferences.getLocalizedString(HomePane.class, "libraries.libraryFileColumn"),
            preferences.getLocalizedString(HomePane.class, "libraries.libraryNameColumn"),
            preferences.getLocalizedString(HomePane.class, "libraries.libraryVersionColumn"),
            preferences.getLocalizedString(HomePane.class, "libraries.libraryLicenseColumn"),
            preferences.getLocalizedString(HomePane.class, "libraries.libraryProviderColumn")};

        public int getRowCount() {
          return libraries.size();
        }
 
        public int getColumnCount() {
          return columnNames.length;
        }
        
        @Override
        public String getColumnName(int column) {
          return columnNames [column];
        }
 
        public Object getValueAt(int rowIndex, int columnIndex) {
          Library library = libraries.get(rowIndex);
          switch (columnIndex) {
            case 0 : return library.getLocation();
            case 1 : return library.getName() != null 
                ? library.getName()
                : library.getDescription();
            case 2 : return library.getVersion();
            case 3 : return library.getLicense();
            case 4 : return library.getProvider();
            default : throw new IllegalArgumentException();
          }
        }
      };
      
    final JTable librariesTable = new JTable(librariesTableModel) {
        @Override
        public String getToolTipText(MouseEvent ev) {
          if (columnAtPoint(ev.getPoint()) == 0) {
            int row = rowAtPoint(ev.getPoint());
            if (row >= 0) {
              // Display the full path of the library as a tool tip
              return libraries.get(row).getLocation();
            }
          }
          return null;
        }
      };
    
    // Set column widths
    librariesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    TableColumnModel columnModel = librariesTable.getColumnModel();
    int [] columnMinWidths = {15, 20, 7, 50, 20};
    Font defaultFont = new DefaultTableCellRenderer().getFont();
    int charWidth;
    if (defaultFont != null) {
      charWidth = getFontMetrics(defaultFont).getWidths() ['A'];
    } else {
      charWidth = 10;
    }      
    for (int i = 0; i < columnMinWidths.length; i++) {
      columnModel.getColumn(i).setPreferredWidth(columnMinWidths [i] * charWidth);
    }

    // Check if it's possible to show a folder
    Object desktopInstance = null;
    Method openMethod = null; 
    if (OperatingSystem.isJavaVersionGreaterOrEqual("1.6")) {
      try {
        // Call Java SE 6 java.awt.Desktop browse method by reflection to
        // ensure Java SE 5 compatibility
        Class<?> desktopClass = Class.forName("java.awt.Desktop");
        desktopInstance = desktopClass.getMethod("getDesktop").invoke(null);
        openMethod = desktopClass.getMethod("open", File.class);
      } catch (Exception ex) {
        // For any exception, let's consider simply the open method isn't available
      }
    }
    final boolean canOpenFolder = openMethod != null || OperatingSystem.isMacOSX() || OperatingSystem.isLinux();
    
    // Display first column as a link
    columnModel.getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
        {
          if (canOpenFolder) {
            setForeground(Color.BLUE);
          }
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value, 
                              boolean isSelected, boolean hasFocus, int row, int column) {
          String location = (String)value;
          try {
            location = new URL(location).getFile().substring(location.lastIndexOf('/') + 1);
          } catch (MalformedURLException ex) {
            // Must be a file
            location = location.substring(location.lastIndexOf(File.separatorChar) + 1);
          }
          super.getTableCellRendererComponent(table, location, isSelected, hasFocus, row, column);
          return this;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
          super.paintComponent(g);
          if (canOpenFolder) {
            // Paint underline
            Insets insets = getInsets();
            g.drawLine(insets.left, getHeight() - 1 - insets.bottom, 
                Math.min(getPreferredSize().width, getWidth()) - insets.right, 
                getHeight() - 1 - insets.bottom);
          }
        }
      });
    if (canOpenFolder) {
      final Object finalDesktopInstance = desktopInstance;
      final Method finalOpenMethod = openMethod; 
      librariesTable.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent ev) {
            if (librariesTable.columnAtPoint(ev.getPoint()) == 0) {
              int row = librariesTable.rowAtPoint(ev.getPoint());
              if (row >= 0) {
                String location = libraries.get(row).getLocation();
                try {
                  new URL(location);
                } catch (MalformedURLException ex) {
                  File directory = new File(location).getParentFile();
                  try {
                    if (finalOpenMethod != null) {
                      finalOpenMethod.invoke(finalDesktopInstance, directory);
                    } else if (OperatingSystem.isMacOSX()) {
                      Runtime.getRuntime().exec(new String [] {"open", directory.getAbsolutePath()});
                    } else { // Linux
                      Runtime.getRuntime().exec(new String [] {"xdg-open", directory.getAbsolutePath()});
                    } 
                  } catch (Exception ex2) {
                    ex2.printStackTrace();
                  }
                }
              }
            }
          }
        });
    }
    return librariesTable;
  }

  /**
   * Displays the given message and returns <code>false</code> if the user 
   * doesn't want to be informed of the shown updates anymore. 
   */
  public boolean showUpdatesMessage(String updatesMessage, boolean showOnlyMessage) {
    JPanel updatesPanel = new JPanel(new GridBagLayout());
    final JScrollPane messageScrollPane = new JScrollPane(createEditorPane(updatesMessage));
    messageScrollPane.setPreferredSize(new Dimension(500, 400));
    messageScrollPane.addAncestorListener(new AncestorListener() {
        public void ancestorAdded(AncestorEvent ev) {
          // Force view position to the origin
          messageScrollPane.getViewport().setViewPosition(new Point(0, 0));
        }

        public void ancestorRemoved(AncestorEvent ev) {
        }

        public void ancestorMoved(AncestorEvent ev) {
        }
      });
    updatesPanel.add(messageScrollPane, new GridBagConstraints(
        0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, 
        GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));

    // Add a check box that lets user choose whether he wants to display the update again at next program launch
    JCheckBox doNotDisplayShownUpdatesCheckBox = new JCheckBox(
        SwingTools.getLocalizedLabelText(this.preferences, HomePane.class, "doNotDisplayShownUpdatesCheckBox.text"));
    if (!OperatingSystem.isMacOSX()) {
      doNotDisplayShownUpdatesCheckBox.setMnemonic(KeyStroke.getKeyStroke(
          this.preferences.getLocalizedString(HomePane.class, "doNotDisplayShownUpdatesCheckBox.mnemonic")).getKeyCode());
    }
    if (!showOnlyMessage) {
      updatesPanel.add(doNotDisplayShownUpdatesCheckBox, new GridBagConstraints(
          0, 1, 1, 1, 1, 1, GridBagConstraints.CENTER, 
          GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    }
    
    SwingTools.showMessageDialog(this, updatesPanel, 
        this.preferences.getLocalizedString(HomePane.class, "showUpdatesMessage.title"), 
        JOptionPane.PLAIN_MESSAGE, doNotDisplayShownUpdatesCheckBox);
    return !doNotDisplayShownUpdatesCheckBox.isSelected();
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
   * Caution !!! This method may be called from an other thread than EDT.  
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
   * Shows a content chooser save dialog to export furniture list in a CSV file.
   */
  public String showExportToCSVDialog(String homeName) {
    return this.controller.getContentManager().showSaveDialog(this,
        this.preferences.getLocalizedString(HomePane.class, "exportToCSVDialog.title"), 
        ContentManager.ContentType.CSV, homeName);
  }

  /**
   * Exports furniture list to a given SVG file.
   * Caution !!! This method may be called from an other thread than EDT.  
   */
  public void exportToCSV(String csvFile) throws RecorderException {
    View furnitureView = this.controller.getFurnitureController().getView();
    FurnitureTable furnitureTable;
    if (furnitureView instanceof FurnitureTable) {
      furnitureTable = (FurnitureTable)furnitureView;
    } else {
      furnitureTable = new FurnitureTable(this.home, this.preferences);
    }    
    
    Writer writer = null;
    boolean exportInterrupted = false;
    try {
      writer = new BufferedWriter(new FileWriter(csvFile));
      furnitureTable.exportToCSV(writer, '\t');
    } catch (InterruptedIOException ex) {
      exportInterrupted = true;
      throw new InterruptedRecorderException("Export to " + csvFile + " interrupted");
    } catch (IOException ex) {
      throw new RecorderException("Couldn't export to SVG in " + csvFile, ex);
    } finally {
      if (writer != null) {
        try {
          writer.close();
          // Delete the file if exporting is interrupted
          if (exportInterrupted) {
            new File(csvFile).delete();
          }
        } catch (IOException ex) {
          throw new RecorderException("Couldn't export to SVG in " + csvFile, ex);
        }
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
   * Caution !!! This method may be called from an other thread than EDT.  
   */
  public void exportToSVG(String svgFile) throws RecorderException {
    View planView = this.controller.getPlanController().getView();
    final PlanComponent planComponent;
    if (planView instanceof PlanComponent) {
      planComponent = (PlanComponent)planView;
    } else {
      planComponent = new PlanComponent(cloneHomeInEventDispatchThread(this.home), this.preferences, null);
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
    homeName = this.controller.getContentManager().showSaveDialog(this,
        this.preferences.getLocalizedString(HomePane.class, "exportToOBJDialog.title"), 
        ContentManager.ContentType.OBJ, homeName);
    
    this.exportAllToOBJ = true;
    if (homeName != null
        && !this.home.getSelectedItems().isEmpty()) {
      String message = this.preferences.getLocalizedString(HomePane.class, "confirmExportAllToOBJ.message");
      String title = this.preferences.getLocalizedString(HomePane.class, "confirmExportAllToOBJ.title");
      String exportAll = this.preferences.getLocalizedString(HomePane.class, "confirmExportAllToOBJ.exportAll");
      String exportSelection = this.preferences.getLocalizedString(HomePane.class, "confirmExportAllToOBJ.exportSelection");
      String cancel = this.preferences.getLocalizedString(HomePane.class, "confirmExportAllToOBJ.cancel");
      int response = JOptionPane.showOptionDialog(this, message, title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
              null, new Object [] {exportAll, exportSelection, cancel}, exportAll);
      if (response == JOptionPane.NO_OPTION) {
        this.exportAllToOBJ = false;
      } else if (response != JOptionPane.YES_OPTION) {
        return null;
      }
    }
    return homeName;
  }
  
  /**
   * Exports the objects of the 3D view to the given OBJ file.
   * Caution !!! This method may be called from an other thread than EDT.  
   */
  public void exportToOBJ(String objFile) throws RecorderException {
    String header = this.preferences != null
        ? this.preferences.getLocalizedString(HomePane.class, 
                                              "exportToOBJ.header", new Date())
        : "";
        
    // Use a clone of home to ignore selection and for thread safety
    OBJExporter.exportHomeToFile(cloneHomeInEventDispatchThread(this.home), objFile, header, this.exportAllToOBJ);
  }

  /**
   * Returns a clone of the given <code>home</code> safely cloned in the EDT.
   */
  private Home cloneHomeInEventDispatchThread(final Home home) throws RecorderException {
    if (EventQueue.isDispatchThread()) {
      return home.clone();
    } else {
      try {
        final AtomicReference<Home> clonedHome = new AtomicReference<Home>();
        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
              clonedHome.set(home.clone());
            }
          });
        return clonedHome.get();
      } catch (InterruptedException ex) {
        throw new InterruptedRecorderException(ex.getMessage());
      } catch (InvocationTargetException ex) {
        throw new RecorderException("Couldn't clone home", ex.getCause());
      } 
    }
  }
  
  /**
   * Export to OBJ in a separate class to be able to run HomePane without Java 3D classes.
   */
  private static class OBJExporter {
    public static void exportHomeToFile(Home home, String objFile, String header, boolean exportAllToOBJ) throws RecorderException {
      OBJWriter writer = null;
      boolean exportInterrupted = false;
      try {
        writer = new OBJWriter(objFile, header, -1);
  
        List<HomePieceOfFurniture> exportedFurniture;
        List<Room> exportedRooms;
        Collection<Wall> exportedWalls;
        if (exportAllToOBJ) {
          exportedFurniture = home.getFurniture();
          exportedRooms = home.getRooms();
          exportedWalls = home.getWalls();
        } else {
          List<Selectable> selectedItems = home.getSelectedItems();
          exportedFurniture = Home.getFurnitureSubList(selectedItems);
          exportedRooms = Home.getRoomsSubList(selectedItems);
          exportedWalls = Home.getWallsSubList(selectedItems);
        }
        
        List<Selectable> emptySelection = Collections.emptyList();
        home.setSelectedItems(emptySelection);
        if (exportAllToOBJ) {
          // Create a not alive new ground to be able to explore its coordinates without setting capabilities
          Rectangle2D homeBounds = getExportedHomeBounds(home);
          if (homeBounds != null) {
            Ground3D groundNode = new Ground3D(home, 
                (float)homeBounds.getX(), (float)homeBounds.getY(), 
                (float)homeBounds.getWidth(), (float)homeBounds.getHeight(), true);
            writer.writeNode(groundNode, "ground");
          }
        }
        
        // Write 3D walls 
        int i = 0;
        for (Wall wall : exportedWalls) {
          // Create a not alive new wall to be able to explore its coordinates without setting capabilities 
          Wall3D wallNode = new Wall3D(wall, home, true, true);
          writer.writeNode(wallNode, "wall_" + ++i);
        }
        // Write 3D furniture 
        i = 0;
        for (HomePieceOfFurniture piece : exportedFurniture) {
          if (piece.isVisible()) {
            // Create a not alive new piece to be able to explore its coordinates without setting capabilities
            HomePieceOfFurniture3D pieceNode = new HomePieceOfFurniture3D(piece, home, true, true);
            writer.writeNode(pieceNode);
          }
        }
        // Write 3D rooms 
        i = 0;
        for (Room room : exportedRooms) {
          // Create a not alive new room to be able to explore its coordinates without setting capabilities 
          Room3D roomNode = new Room3D(room, home, false, true, true);
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
     * Returns <code>home</code> bounds. 
     */
    private static Rectangle2D getExportedHomeBounds(Home home) {
      // Compute bounds that include walls and furniture
      Rectangle2D homeBounds = updateObjectsBounds(null, home.getWalls());
      for (HomePieceOfFurniture piece : getVisibleFurniture(home.getFurniture())) {
        for (float [] point : piece.getPoints()) {
          if (homeBounds == null) {
            homeBounds = new Rectangle2D.Float(point [0], point [1], 0, 0);
          } else {
            homeBounds.add(point [0], point [1]);
          }
        }
      }
      return updateObjectsBounds(homeBounds, home.getRooms());
    }

    /**
     * Returns all the visible pieces in the given <code>furniture</code>.  
     */
    private static List<HomePieceOfFurniture> getVisibleFurniture(List<HomePieceOfFurniture> furniture) {
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
     * Updates <code>objectBounds</code> to include the bounds of <code>items</code>.
     */
    private static Rectangle2D updateObjectsBounds(Rectangle2D objectBounds,
                                            Collection<? extends Selectable> items) {
      for (Selectable item : items) {
        if (!(item instanceof Elevatable)
            || ((Elevatable)item).getLevel() == null
            || ((Elevatable)item).getLevel().isVisible()) {
          for (float [] point : item.getPoints()) {
            if (objectBounds == null) {
              objectBounds = new Rectangle2D.Float(point [0], point [1], 0, 0);
            } else {
              objectBounds.add(point [0], point [1]);
            }
          }
        }
      }
      return objectBounds;
    }
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
   * Displays a dialog that lets the user choose a name for the current camera.
   * @return the chosen name or <code>null</code> if the user canceled.
   */
  public String showStoreCameraDialog(String cameraName) {
    // Retrieve displayed text in dialog
    String message = this.preferences.getLocalizedString(HomePane.class, "showStoreCameraDialog.message");
    String title = this.preferences.getLocalizedString(HomePane.class, "showStoreCameraDialog.title");
    
    List<Camera> storedCameras = this.home.getStoredCameras();
    JComponent cameraNameChooser;
    JTextComponent cameraNameTextComponent;
    if (storedCameras.isEmpty()) {
      cameraNameChooser = cameraNameTextComponent = new JTextField(cameraName, 20);
    } else {
      // If cameras are already stored in home propose an editable combo box to user
      // to let him choose more easily an existing one if he want to overwrite it
      String [] storedCameraNames = new String [storedCameras.size()];
      for (int i = 0; i < storedCameraNames.length; i++) {
        storedCameraNames [i] = storedCameras.get(i).getName();
      }
      JComboBox cameraNameComboBox = new JComboBox(storedCameraNames);
      cameraNameComboBox.setEditable(true);
      cameraNameComboBox.getEditor().setItem(cameraName);
      Component editorComponent = cameraNameComboBox.getEditor().getEditorComponent();
      if (editorComponent instanceof JTextComponent) {
        cameraNameTextComponent = (JTextComponent)editorComponent;
        cameraNameChooser = cameraNameComboBox;
      } else {
        cameraNameChooser = cameraNameTextComponent = new JTextField(cameraName, 20);
      }
    }
    JPanel cameraNamePanel = new JPanel(new BorderLayout(2, 2));
    cameraNamePanel.add(new JLabel(message), BorderLayout.NORTH);
    cameraNamePanel.add(cameraNameChooser, BorderLayout.SOUTH);
    if (SwingTools.showConfirmDialog(this, cameraNamePanel, 
        title, cameraNameTextComponent) == JOptionPane.OK_OPTION) {
      cameraName = cameraNameTextComponent.getText().trim();
      if (cameraName.length() > 0) {
        return cameraName;
      }
    } 
      
    return null;
  }

  /**
   * Displays a dialog showing the list of cameras stored in home 
   * and returns the ones selected by the user to be deleted.  
   */
  public List<Camera> showDeletedCamerasDialog() {    
    // Build stored cameras list
    List<Camera> storedCameras = this.home.getStoredCameras();
    final List<Camera> selectedCameras = new ArrayList<Camera>();
    final JList camerasList = new JList(storedCameras.toArray());
    camerasList.setCellRenderer(new ListCellRenderer() {
        private JCheckBox cameraCheckBox = new JCheckBox();
        
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
          this.cameraCheckBox.setText(((Camera)value).getName());
          this.cameraCheckBox.setSelected(selectedCameras.contains(value));
          this.cameraCheckBox.setOpaque(true);
          if (isSelected && list.hasFocus()) {
            this.cameraCheckBox.setBackground(list.getSelectionBackground());
            this.cameraCheckBox.setForeground(list.getSelectionForeground());
          }
          else {
            this.cameraCheckBox.setBackground(list.getBackground());
            this.cameraCheckBox.setForeground(list.getForeground());
          }
          return this.cameraCheckBox;
        }
      });
    camerasList.getInputMap().put(KeyStroke.getKeyStroke("pressed SPACE"), "toggleSelection");
    final AbstractAction toggleSelectionAction = new AbstractAction() {
        public void actionPerformed(ActionEvent ev) {
          Camera selectedCamera = (Camera)camerasList.getSelectedValue();
          if (selectedCamera != null) {
            int index = selectedCameras.indexOf(selectedCamera);
            if (index >= 0) {
              selectedCameras.remove(index);
            } else {
              selectedCameras.add(selectedCamera);
            }
            camerasList.repaint();
          }
        }
      };
    camerasList.getActionMap().put("toggleSelection", toggleSelectionAction);
    camerasList.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent ev) {
          toggleSelectionAction.actionPerformed(null);
        }
      });
    camerasList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    camerasList.setSelectedIndex(0);

    // Retrieve displayed text in dialog
    String message = this.preferences.getLocalizedString(HomePane.class, "showDeletedCamerasDialog.message");
    String title = this.preferences.getLocalizedString(HomePane.class, "showDeletedCamerasDialog.title");

    JPanel camerasPanel = new JPanel(new BorderLayout(0, 5));
    camerasPanel.add(new JLabel(message), BorderLayout.NORTH);
    camerasPanel.add(SwingTools.createScrollPane(camerasList), BorderLayout.CENTER);

    if (SwingTools.showConfirmDialog(this, camerasPanel, title, camerasList) == JOptionPane.OK_OPTION) {
      String confirmMessage = this.preferences.getLocalizedString(HomePane.class, "confirmDeleteCameras.message");
      String delete = this.preferences.getLocalizedString(HomePane.class, "confirmDeleteCameras.delete");
      String cancel = this.preferences.getLocalizedString(HomePane.class, "confirmDeleteCameras.cancel");      
      if (JOptionPane.showOptionDialog(this, confirmMessage, title, 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
            null, new Object [] {delete, cancel}, cancel) == JOptionPane.OK_OPTION) {
        return selectedCameras;
      }
    } 
    return null;    
  }

  /**
   * Returns <code>true</code> if clipboard contains data that
   * components are able to handle.
   */
  public boolean isClipboardEmpty() {
    try {
      Clipboard clipboard = getToolkit().getSystemClipboard();
      return !(clipboard.isDataFlavorAvailable(HomeTransferableList.HOME_FLAVOR)
          || getToolkit().getSystemClipboard().isDataFlavorAvailable(DataFlavor.javaFileListFlavor));
    } catch (AccessControlException ex) {
      // AWT uses a private clipboard that won't be empty as soon as a copy action will be done
      return this.clipboardEmpty;
    }    
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
            Object newValue = ev.getNewValue();
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
                      Action.SHORT_DESCRIPTION, oldValue, newValue));
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
   * An object able to format a selectable item.
   */
  private abstract interface SelectableFormat<T extends Selectable> {
    public abstract String format(T item);
  }
}
