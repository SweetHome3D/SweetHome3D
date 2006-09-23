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
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.ResourceBundle;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * The MVC view that edits home. 
 * @author Emmanuel Puybaret
 */
public class HomePane extends JRootPane {
  public enum ActionType {
    NEW_HOME, CLOSE, OPEN, SAVE, SAVE_AS, PREFERENCES, EXIT, 
    UNDO, REDO, CUT, COPY, PASTE, DELETE, 
    ADD_HOME_FURNITURE, DELETE_HOME_FURNITURE,
    WALL_CREATION, DELETE_SELECTION, ABOUT}
  public enum SaveAnswer {SAVE, CANCEL, DO_NOT_SAVE}
  public enum FocusableView {CATALOG, FURNITURE, PLAN}

  private static final String SWEET_HOME_3D_EXTENSION = ".sh3d";
  private static final FileFilter SWEET_HOME_3D_FILTER = new FileFilter() {
      @Override
      public boolean accept(File file) {
        // Accept directories and .sh3d files
        return file.isDirectory()
               || file.getName().toLowerCase().
                      endsWith(SWEET_HOME_3D_EXTENSION);
      }
  
      @Override
      public String getDescription() {
        return "Sweet Home 3D";
      }
    }; 
  private static File currentDirectory;

  private ResourceBundle                  resource;
  // Button model shared by Wall creation menu item and the matching tool bar button
  private JToggleButton.ToggleButtonModel wallCreationToggleModel;
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
  public HomePane(Home home, UserPreferences preferences, HomeController controller) {
    this.resource = ResourceBundle.getBundle(HomePane.class.getName());
    // Create a unique toggle button model for Wall creation / Selection states
    // so Wall creation menu item and tool bar button 
    // always reflect the same toggle state at screen
    this.wallCreationToggleModel = new JToggleButton.ToggleButtonModel();
    JPopupMenu.setDefaultLightWeightPopupEnabled(false);
    createActions(controller);
    createTransferHandlers(home, preferences, controller);
    setJMenuBar(getHomeMenuBar());
    getContentPane().add(getToolBar(), BorderLayout.NORTH);
    getContentPane().add(getMainPane(home, preferences, controller));
  }
  
  /**
   * Creates the actions map of this component.
   */
  private void createActions(final HomeController controller) {
    createAction(ActionType.NEW_HOME, controller, "newHome");
    createAction(ActionType.OPEN, controller, "open");
    createAction(ActionType.CLOSE, controller, "close");
    createAction(ActionType.SAVE, controller, "save");
    createAction(ActionType.SAVE_AS, controller, "saveAs");
    createAction(ActionType.PREFERENCES, controller, "editPreferences");
    createAction(ActionType.EXIT, controller, "exit");
    createAction(ActionType.UNDO, controller, "undo");
    createAction(ActionType.REDO, controller, "redo");
    createClipboardAction(ActionType.CUT, TransferHandler.getCutAction());
    createClipboardAction(ActionType.COPY, TransferHandler.getCopyAction());
    createClipboardAction(ActionType.PASTE, TransferHandler.getPasteAction());
    createAction(ActionType.DELETE, controller, "delete");
    createAction(ActionType.DELETE_HOME_FURNITURE, controller, "deleteHomeFurniture");
    createAction(ActionType.ADD_HOME_FURNITURE, controller, "addHomeFurniture");
    getActionMap().put(ActionType.WALL_CREATION,
        new ResourceAction (this.resource, ActionType.WALL_CREATION.toString()) {
          public void actionPerformed(ActionEvent ev) {
            boolean selected = ((AbstractButton)ev.getSource()).isSelected();
            if (selected) {
              controller.setWallCreationMode();
            } else {
              controller.setSelectionMode();
            }
          }
        });
    createAction(ActionType.DELETE_SELECTION, 
        controller.getPlanController(), "deleteSelection");
    createAction(ActionType.ABOUT, controller, "about");
  }
  
  /**
   * Creates a <code>ControllerAction</code> object that calls a given
   * <code>method</code> on <code>controller</code>.
   */
  private void createAction(ActionType action, Object controller, 
                            String method)  {
    try {
      getActionMap().put(action, new ControllerAction(
          this.resource, action.toString(), controller, method));
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
                                      HomeController controller) {
    this.catalogTransferHandler = 
        new CatalogTransferHandler(preferences.getCatalog());
    this.furnitureTransferHandler = 
        new FurnitureTransferHandler(home, controller);
    this.planTransferHandler = 
        new PlanTransferHandler(home, controller);
  }

  /**
   * Returns the menu bar displayed in this pane.
   */
  private JMenuBar getHomeMenuBar() {
    ActionMap actions = getActionMap();
    
    // Create File menu
    JMenu fileMenu = new JMenu(
        new ResourceAction(this.resource, "FILE_MENU"));
    fileMenu.setEnabled(true);
    fileMenu.add(actions.get(ActionType.NEW_HOME));
    fileMenu.add(actions.get(ActionType.OPEN));
    fileMenu.addSeparator();
    fileMenu.add(actions.get(ActionType.CLOSE));
    fileMenu.add(actions.get(ActionType.SAVE));
    fileMenu.add(actions.get(ActionType.SAVE_AS));
    // Don't add EXIT menu under Mac OS X, it's displayed in application menu  
    if (!System.getProperty("os.name").startsWith("Mac OS X")) {
      fileMenu.addSeparator();
      fileMenu.add(actions.get(ActionType.PREFERENCES));
      fileMenu.addSeparator();
      fileMenu.add(actions.get(ActionType.EXIT));
    }

    // Create Edit menu
    JMenu editMenu = new JMenu(
        new ResourceAction(this.resource, "EDIT_MENU"));
    editMenu.setEnabled(true);
    editMenu.add(actions.get(ActionType.UNDO));
    editMenu.add(actions.get(ActionType.REDO));
    editMenu.addSeparator();
    editMenu.add(actions.get(ActionType.CUT));
    editMenu.add(actions.get(ActionType.COPY));
    editMenu.add(actions.get(ActionType.PASTE));
    editMenu.addSeparator();
    editMenu.add(actions.get(ActionType.DELETE));

    // Create Furniture menu
    JMenu furnitureMenu = new JMenu(
        new ResourceAction(this.resource, "FURNITURE_MENU"));
    furnitureMenu.setEnabled(true);
    furnitureMenu.add(actions.get(ActionType.ADD_HOME_FURNITURE));
    
    // Create Plan menu
    JMenu planMenu = new JMenu(
        new ResourceAction(this.resource, "PLAN_MENU"));
    planMenu.setEnabled(true);
    JCheckBoxMenuItem wallCreationCheckBoxMenuItem = 
        new JCheckBoxMenuItem(actions.get(ActionType.WALL_CREATION));
    // Use the same model as Wall creation tool bar button
    wallCreationCheckBoxMenuItem.setModel(this.wallCreationToggleModel);
    planMenu.add(wallCreationCheckBoxMenuItem);

    // Create Help menu
    JMenu helpMenu = null;
    if (!System.getProperty("os.name").startsWith("Mac OS X")) {
      helpMenu = new JMenu(
          new ResourceAction(this.resource, "HELP_MENU"));
      helpMenu.setEnabled(true);
      helpMenu.add(actions.get(ActionType.ABOUT));
    }

    // Add menus to menu bar
    JMenuBar menuBar = new JMenuBar();
    menuBar.add(fileMenu);
    menuBar.add(editMenu);
    menuBar.add(furnitureMenu);
    menuBar.add(planMenu);
    if (helpMenu != null) {
      menuBar.add(helpMenu);
    }
    
    return menuBar;
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
    toolBar.add(actions.get(ActionType.ADD_HOME_FURNITURE));
    JToggleButton wallCreationToggleButton = 
        new JToggleButton(actions.get(ActionType.WALL_CREATION));
    // Use the same model as Wall creation menu item
    wallCreationToggleButton.setModel(this.wallCreationToggleModel);
    // Don't display text with icon
    wallCreationToggleButton.setText("");
    toolBar.add(wallCreationToggleButton);
    toolBar.addSeparator();
    toolBar.add(actions.get(ActionType.UNDO));
    toolBar.add(actions.get(ActionType.REDO));
    toolBar.addSeparator();
    toolBar.add(actions.get(ActionType.CUT));
    toolBar.add(actions.get(ActionType.COPY));
    toolBar.add(actions.get(ActionType.PASTE));
    toolBar.addSeparator();
    toolBar.add(actions.get(ActionType.DELETE));
    
    // Remove focuable property on buttons
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
        getCatalogFurniturePane(home, preferences, controller), 
        getPlanView3DPane(home, controller));
    mainPane.setContinuousLayout(true);
    mainPane.setOneTouchExpandable(true);
    mainPane.setResizeWeight(0.3);
    return mainPane;
  }

  /**
   * Returns the catalog tree and furniture table pane. 
   */
  private JComponent getCatalogFurniturePane(Home home, UserPreferences preferences, 
                                             HomeController controller) {
    this.catalogView = new CatalogTree(preferences.getCatalog());
    JScrollPane catalogScrollPane = new HomeScrollPane(this.catalogView);
    // Add focus listener to catalog tree
    this.catalogView.addFocusListener(new FocusableViewListener(
        controller, catalogScrollPane, FocusableView.CATALOG));
    
    this.furnitureView = new FurnitureTable(home, preferences);
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
        controller, furnitureScrollPane, FocusableView.FURNITURE));
    // Add a mouse listener that gives focus to furniture view when
    // user clicks in its viewport
    ((JViewport)this.furnitureView.getParent()).addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent ev) {
            furnitureView.requestFocusInWindow();
          }
        });    
    
    // Create a split pane that displays both components
    JSplitPane catalogFurniturePane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
        catalogScrollPane, furnitureScrollPane);
    catalogFurniturePane.setContinuousLayout(true);
    catalogFurniturePane.setOneTouchExpandable(true);
    catalogFurniturePane.setResizeWeight(0.5);
    return catalogFurniturePane;
  }

  /**
   * Returns the plan focusedView and 3D focusedView pane. 
   */
  private JComponent getPlanView3DPane(Home home, HomeController controller) {
    this.planView = controller.getPlanController().getView();
    JScrollPane planScrollPane = new HomeScrollPane(this.planView);
    this.planView.addFocusListener(new FocusableViewListener(
        controller, planScrollPane, FocusableView.PLAN));

    JComponent view3D = new HomeComponent3D(home);
    view3D.setPreferredSize(this.planView.getPreferredSize());
    view3D.setMinimumSize(new Dimension(0, 0));
    // Create a split pane that displays both components
    JSplitPane planView3DPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
        planScrollPane, view3D);
    planView3DPane.setContinuousLayout(true);
    planView3DPane.setOneTouchExpandable(true);
    planView3DPane.setResizeWeight(0.5);
    
    return planView3DPane;
  }

  /**
   * Displays a file chooser dialog to open a .sh3d file.
   */
  public String showOpenDialog() {
    // Use native file dialog under Mac OS X
    if (System.getProperty("os.name").startsWith("Mac OS X")) {
      return showFileDialog(false, null);
    } else {
      return showFileChooser(false, null);
    }
  }

  /**
   * Displays a file chooser dialog to save a home in a .sh3d file.
   */
  public String showSaveDialog(String name) {
    String file;
    // Use native file dialog under Mac OS X
    if (System.getProperty("os.name").startsWith("Mac OS X")) {
      file = showFileDialog(true, name);
    } else {
      file = showFileChooser(true, name);
    }
    if (file != null && !file.toLowerCase().endsWith(SWEET_HOME_3D_EXTENSION)) {
      file += SWEET_HOME_3D_EXTENSION;
    }
    return file;
  }
  
  /**
   * Displays a Swing file chooser.
   */
  private String showFileChooser(boolean save, String name) {
    JFileChooser fileChooser = new JFileChooser();
    // Choose file chooser type
    if (save && name != null) {
      fileChooser.setSelectedFile(new File(name));
    }
    // Set .sh3d files filter 
    fileChooser.setFileFilter(SWEET_HOME_3D_FILTER);
    // Update current directory
    if (currentDirectory != null) {
      fileChooser.setCurrentDirectory(currentDirectory);
    }
    int option;
    if (save) {
      option = fileChooser.showSaveDialog(this);
    } else {
      option = fileChooser.showOpenDialog(this);
    }
    if (option == JFileChooser.APPROVE_OPTION) {
      // Retrieve current directory for future calls
      currentDirectory = fileChooser.getCurrentDirectory();
      // Return selected file
      return fileChooser.getSelectedFile().toString();
    } else {
      return null;
    }
  }

  /**
   * Displays an AWT file dialog.
   */
  private String showFileDialog(boolean save, String name) {
    FileDialog fileDialog = 
      new FileDialog(JOptionPane.getFrameForComponent(this));

    // Choose file chooser type
    if (save && name != null) {
      fileDialog.setFile(new File(name).getName());
    }
    // Set .sh3d files filter 
    fileDialog.setFilenameFilter(new FilenameFilter() {
        public boolean accept(File dir, String name) {
          return name.toLowerCase().endsWith(SWEET_HOME_3D_EXTENSION);
        }
      });
    // Update current directory
    if (currentDirectory != null) {
      fileDialog.setDirectory(currentDirectory.toString());
    }
    if (save) {
      fileDialog.setMode(FileDialog.SAVE);
      fileDialog.setTitle(this.resource.getString("fileDialog.saveTitle"));
    } else {
      fileDialog.setMode(FileDialog.LOAD);
      fileDialog.setTitle(this.resource.getString("fileDialog.openTitle"));
    }
    fileDialog.setVisible(true);
    String selectedFile = fileDialog.getFile();
    // If user choosed a file
    if (selectedFile != null) {
      // Retrieve current directory for future calls
      currentDirectory = new File(fileDialog.getDirectory());
      // Return selected file
      return currentDirectory + File.separator + selectedFile;
    } else {
      return null;
    }
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
   * Displays a dialog that let user choose whether he wants to overwrite 
   * file <code>name</code> or not.
   * @return <code>true</code> if user confirmed to overwrite.
   */
  public boolean confirmOverwrite(String name) {
    // Retrieve displayed text in buttons and message
    String messageFormat = this.resource.getString("confirmOverwrite.message");
    String message = String.format(messageFormat, new File(name).getName());
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
  public SaveAnswer confirmSave(String name) {
    // Retrieve displayed text in buttons and message
    String messageFormat = this.resource.getString("confirmSave.message");
    String message;
    if (name != null) {
      message = String.format(messageFormat, 
          "\"" + new File(name).getName() + "\"");
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
    return JOptionPane.showConfirmDialog(this, message, title, 
        JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION;
  }

  /**
   * Displays an about dialog.
   */
  public void showAboutDialog() {
    String title   = this.resource.getString("about.title");
    String message = this.resource.getString("about.message");
    URL    iconUrl = HomePane.class.getResource(
        this.resource.getString("about.icon"));
    JOptionPane.showMessageDialog(this, message, title,  
        JOptionPane.INFORMATION_MESSAGE, new ImageIcon(iconUrl));
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
   * A scroll pane that always displays scroll bar on Mac OS X.
   */
  private static class HomeScrollPane extends JScrollPane {
    public HomeScrollPane(Component view) {
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
    private FocusableView  viewKey;
  
    public FocusableViewListener(HomeController controller, 
                                 JComponent     feedbackComponent,
                                 FocusableView  viewKey) {
      this.controller = controller;
      this.feedbackComponent = feedbackComponent;
      this.viewKey  = viewKey;
      feedbackComponent.setBorder(UNFOCUSED_BORDER);
    }
        
    public void focusGained(FocusEvent ev) {
      // Display a colored border
      this.feedbackComponent.setBorder(FOCUSED_BORDER);
      // Update the component used by clipboard actions
      focusedComponent = (JComponent)ev.getComponent();
      // Notify controller that active view changed
      this.controller.focusedViewChanged(this.viewKey);
    }
    
    public void focusLost(FocusEvent ev) {
      this.feedbackComponent.setBorder(UNFOCUSED_BORDER);
    }
  }
}
