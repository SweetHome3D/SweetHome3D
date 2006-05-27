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
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

/**
 * The MVC view that edits home furniture. 
 * @author Emmanuel Puybaret
 */
public class HomePane extends JRootPane {
  public enum ActionType {undo, redo, addHomeFurniture, deleteFurniture}
  
  private Map<ActionType, Action> actions;

  /**
   * Create this view associated with its controller.
   */
  public HomePane(HomeController controller) {
    createActions(controller);
    setJMenuBar(getHomeMenuBar());
    getContentPane().add(getToolBar(), BorderLayout.NORTH);
    getContentPane().add(getCatalogFurniturePane(controller));
  }

  private void createActions(HomeController controller) {
    try {
      this.actions = new HashMap<ActionType, Action>();
      actions.put(ActionType.undo,
          new ViewControllerAction(ActionType.undo + "Action", this,
              controller, "undo"));
      actions.put(ActionType.redo,
          new ViewControllerAction(ActionType.redo + "Action", this,
              controller, "redo"));
      actions.put(ActionType.addHomeFurniture,
          new ViewControllerAction(ActionType.addHomeFurniture + "Action", this,
              controller, "addHomeFurniture"));
      actions.put(ActionType.deleteFurniture,
          new ViewControllerAction(ActionType.deleteFurniture + "Action", this,
              controller.getFurnitureController(), "deleteFurniture"));
    } catch (NoSuchMethodException ex) {
      throw new RuntimeException(ex);
    }
  }
  
  private JMenuBar getHomeMenuBar() {
    JMenu editMenu = new JMenu(new ResourceAction("editMenu", this));
    editMenu.add(actions.get(ActionType.undo));
    editMenu.add(actions.get(ActionType.redo));
    JMenu furnitureMenu = new JMenu(new ResourceAction("furnitureMenu", this));
    editMenu.add(actions.get(ActionType.addHomeFurniture));
    editMenu.add(actions.get(ActionType.deleteFurniture));
    JMenuBar menuBar = new JMenuBar();
    menuBar.add(editMenu);
    return menuBar;
  }
  
  private JToolBar getToolBar() {
    JToolBar toolBar = new JToolBar();
    toolBar.add(actions.get(ActionType.undo));
    toolBar.add(actions.get(ActionType.redo));
    toolBar.add(actions.get(ActionType.addHomeFurniture));
    toolBar.add(actions.get(ActionType.deleteFurniture));
    return toolBar;
  }

  private JComponent getCatalogFurniturePane(HomeController controller) {
    JComponent catalogView = controller.getCatalogController().getView();
    JComponent furnitureView = controller.getFurnitureController().getView();
    // Create a split pane that displays both components
    JSplitPane catalogFurniturePane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
        new JScrollPane(catalogView), new JScrollPane(furnitureView));
    catalogFurniturePane.setResizeWeight(0.5);
    return catalogFurniturePane;
  }
}
