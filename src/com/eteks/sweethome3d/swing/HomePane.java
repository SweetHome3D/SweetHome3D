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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ResourceBundle;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionMap;
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
import javax.swing.filechooser.FileFilter;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * The MVC view that edits home. 
 * @author Emmanuel Puybaret
 */
public class HomePane extends JRootPane {
  public enum ActionType {
    NEW_HOME, CLOSE, OPEN, SAVE, SAVE_AS, EXIT, 
    UNDO, REDO, 
    ADD_HOME_FURNITURE, DELETE_HOME_FURNITURE,
    WALL_CREATION, DELETE_SELECTION}
  public enum SaveAnswer {SAVE, CANCEL, DO_NOT_SAVE}

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

  private JCheckBoxMenuItem   wallCreationCheckBoxMenuItem;
  private JToggleButton       wallCreationToggleButton;
  private ResourceBundle      resource;
  
  /**
   * Creates this view associated with its controller.
   */
  public HomePane(Home home, UserPreferences preferences, HomeController controller) {
    this.resource = ResourceBundle.getBundle(HomePane.class.getName());
    JPopupMenu.setDefaultLightWeightPopupEnabled(false);
    createActions(controller);
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
    createAction(ActionType.EXIT, controller, "exit");
    createAction(ActionType.UNDO, controller, "undo");
    createAction(ActionType.REDO, controller, "redo");
    createAction(ActionType.ADD_HOME_FURNITURE, controller, "addHomeFurniture");
    createAction(ActionType.DELETE_HOME_FURNITURE, controller, "deleteHomeFurniture");
    getActionMap().put(ActionType.WALL_CREATION,
        new ResourceAction (this.resource, ActionType.WALL_CREATION.toString()) {
          public void actionPerformed(ActionEvent ev) {
            boolean selected = ((AbstractButton)ev.getSource()).isSelected();
            if (selected) {
              controller.setWallCreationMode();
            } else {
              controller.setSelectionMode();
            }
            // Update selected state of tool bar button and menu item
            wallCreationToggleButton.setSelected(selected);
            wallCreationCheckBoxMenuItem.setSelected(selected);
          }
        });
    createAction(ActionType.DELETE_SELECTION, 
        controller.getPlanController(), "deleteSelection");
  }
  
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
    fileMenu.addSeparator();
    fileMenu.add(actions.get(ActionType.EXIT));

    // Create Edit menu
    JMenu editMenu = new JMenu(
        new ResourceAction(this.resource, "EDIT_MENU"));
    editMenu.setEnabled(true);
    editMenu.add(actions.get(ActionType.UNDO));
    editMenu.add(actions.get(ActionType.REDO));

    // Create Furniture menu
    JMenu furnitureMenu = new JMenu(
        new ResourceAction(this.resource, "FURNITURE_MENU"));
    furnitureMenu.setEnabled(true);
    furnitureMenu.add(actions.get(ActionType.ADD_HOME_FURNITURE));
    furnitureMenu.add(actions.get(ActionType.DELETE_HOME_FURNITURE));
    
    // Create Plan menu
    JMenu planMenu = new JMenu(
        new ResourceAction(this.resource, "PLAN_MENU"));
    planMenu.setEnabled(true);
    this.wallCreationCheckBoxMenuItem = 
        new JCheckBoxMenuItem(actions.get(ActionType.WALL_CREATION));
    planMenu.add(this.wallCreationCheckBoxMenuItem);
    planMenu.add(actions.get(ActionType.DELETE_SELECTION));

    // Add menus to menu bar
    JMenuBar menuBar = new JMenuBar();
    menuBar.add(fileMenu);
    menuBar.add(editMenu);
    menuBar.add(furnitureMenu);
    menuBar.add(planMenu);
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
    toolBar.add(actions.get(ActionType.DELETE_HOME_FURNITURE));
    toolBar.addSeparator();
    this.wallCreationToggleButton = 
      new JToggleButton(actions.get(ActionType.WALL_CREATION));
    // Don't display text with icon
    this.wallCreationToggleButton.setText("");
    toolBar.add(this.wallCreationToggleButton);
    toolBar.add(actions.get(ActionType.DELETE_SELECTION));
    toolBar.addSeparator();
    toolBar.add(actions.get(ActionType.UNDO));
    toolBar.add(actions.get(ActionType.REDO));
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
   * Returns the main pane with catalog tree, furniture table and plan pane. 
   */
  private JComponent getMainPane(Home home, UserPreferences preferences, 
                                 HomeController controller) {
    JSplitPane mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
        getCatalogFurniturePane(home, preferences), 
        getPlanView3DPane(home, controller));
    mainPane.setContinuousLayout(true);
    mainPane.setOneTouchExpandable(true);
    mainPane.setResizeWeight(0.3);
    return mainPane;
  }

  /**
   * Returns the catalog tree and furniture table pane. 
   */
  private JComponent getCatalogFurniturePane(Home home, UserPreferences preferences) {
    JComponent catalogView = new CatalogTree(preferences.getCatalog());
    JComponent furnitureView = new FurnitureTable(home, preferences);
    // Create a split pane that displays both components
    JSplitPane catalogFurniturePane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
        new JScrollPane(catalogView), new JScrollPane(furnitureView));
    catalogFurniturePane.setContinuousLayout(true);
    catalogFurniturePane.setOneTouchExpandable(true);
    catalogFurniturePane.setResizeWeight(0.5);
    return catalogFurniturePane;
  }

  /**
   * Returns the plan view and 3D view pane. 
   */
  private JComponent getPlanView3DPane(Home home, HomeController controller) {
    JComponent planView = controller.getPlanController().getView();
    JComponent view3D = new HomeComponent3D(home);
    view3D.setPreferredSize(planView.getPreferredSize());
    view3D.setMinimumSize(new Dimension(0, 0));
    // Create a split pane that displays both components
    JSplitPane planView3DPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
        new JScrollPane(planView), view3D);
    planView3DPane.setContinuousLayout(true);
    planView3DPane.setOneTouchExpandable(true);
    planView3DPane.setResizeWeight(0.5);
    return planView3DPane;
  }

  /**
   * Displays a file chooser dialog to open a .sh3d file.
   */
  public String showOpenDialog() {
    return showFileChooser(false, null);
  }

  /**
   * Displays a file chooser dialog to save a home in a .sh3d file.
   */
  public String showSaveDialog(String name) {
    String file = showFileChooser(true, name);
    if (file != null && !file.toLowerCase().endsWith(SWEET_HOME_3D_EXTENSION)) {
      file += SWEET_HOME_3D_EXTENSION;
    }
    return file;
  }
  
  /**
   * Displays a file chooser.
   */
  public String showFileChooser(boolean save, String name) {
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
      // Return choosen file
      return fileChooser.getSelectedFile().toString();
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
}
