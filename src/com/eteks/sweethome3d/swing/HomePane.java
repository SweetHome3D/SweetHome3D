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
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * The MVC view that edits home. 
 * @author Emmanuel Puybaret
 */
public class HomePane extends JRootPane {
  public enum ActionType {
    ADD_HOME_FURNITURE, DELETE_HOME_FURNITURE, UNDO, REDO}

  /**
   * Create this view associated with its controller.
   */
  public HomePane(Home home, UserPreferences preferences, HomeController controller) {
    createActions(controller);
    getContentPane().add(getToolBar(), BorderLayout.NORTH);
    getContentPane().add(getCatalogFurniturePane(home, preferences));
  }
  
  private void createActions(final HomeController controller) {
    ResourceBundle resource = ResourceBundle.getBundle(
        HomePane.class.getName());
    ActionMap actions = getActionMap();    
    actions.put(ActionType.ADD_HOME_FURNITURE,
      new ResourceAction(resource, ActionType.ADD_HOME_FURNITURE.toString()) {
        @Override
        public void actionPerformed(ActionEvent ev) {
          controller.addHomeFurniture();
        }
      });
    actions.put(ActionType.DELETE_HOME_FURNITURE,
      new ResourceAction(resource, ActionType.DELETE_HOME_FURNITURE.toString()){
        @Override
        public void actionPerformed(ActionEvent ev) {
          controller.deleteHomeFurniture();
        }
      });
    actions.put(ActionType.UNDO,
      new ResourceAction(resource, ActionType.UNDO.toString()){
        @Override
        public void actionPerformed(ActionEvent ev) {
          controller.undo();
        }
      });
    actions.put(ActionType.REDO,
      new ResourceAction(resource, ActionType.REDO.toString()){
        @Override
        public void actionPerformed(ActionEvent ev) {
          controller.redo();
        }
      });
  }
  
  /**
   * Returns the tool bar displayed in this pane.
   */
  private JToolBar getToolBar() {
    JToolBar toolBar = new JToolBar();
    ActionMap actions = getActionMap();    
    toolBar.add(actions.get(ActionType.ADD_HOME_FURNITURE));
    toolBar.add(actions.get(ActionType.DELETE_HOME_FURNITURE));
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
   * Returns the catalog tree and furniture table pane. 
   */
  private JComponent getCatalogFurniturePane(Home home, UserPreferences preferences) {
    JComponent catalogView = new CatalogTree(preferences.getCatalog());
    JComponent furnitureView = new FurnitureTable(home, preferences);
    // Create a split pane that displays both components
    JSplitPane catalogFurniturePane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
        new JScrollPane(catalogView), new JScrollPane(furnitureView));
    catalogFurniturePane.setResizeWeight(0.5);
    return catalogFurniturePane;
  }
}
