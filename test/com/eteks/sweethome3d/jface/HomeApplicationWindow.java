/*
 * HomeApplicationWindow.java 10 aout 2006
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
package com.eteks.sweethome3d.jface;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.eteks.sweethome3d.model.Catalog;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.viewcontroller.CatalogController;
import com.eteks.sweethome3d.viewcontroller.CatalogView;
import com.eteks.sweethome3d.viewcontroller.FurnitureController;
import com.eteks.sweethome3d.viewcontroller.FurnitureView;
import com.eteks.sweethome3d.viewcontroller.HomeController;
import com.eteks.sweethome3d.viewcontroller.HomeView;
import com.eteks.sweethome3d.viewcontroller.ViewFactory;

/**
 * The MVC application view of Sweet Home 3D. This class implements <code>ViewFactory</code>
 * interface to keep control on the creation order of components and their parent. 
 * @author Emmanuel Puybaret
 */
public class HomeApplicationWindow extends ApplicationWindow implements ViewFactory, HomeView {
  private HomeController  controller;
  private Home            home;
  private UserPreferences preferences;
  
  private ResourceBundle  resource;

  private SashForm        catalogFurnitureSashForm;
  private Map<ActionType, ResourceAction> actions;
  
  public HomeApplicationWindow(Home home, UserPreferences preferences) {
    super(null);
    this.home = home;
    this.preferences = preferences;
    this.resource = ResourceBundle.getBundle(
        HomeApplicationWindow.class.getName());
    // Create actions first because createToolBarManager and createMenuManager needs them 
    createActions();
    addMenuBar();
    addToolBar(SWT.FLAT);
  }

  @Override
  protected void configureShell(Shell shell) {
    super.configureShell(shell);
    shell.setText("Home Controller Test");
  }

  @Override
  protected Control createContents(Composite parent) {
    this.catalogFurnitureSashForm = new SashForm(parent, SWT.VERTICAL);
    // Create controller and the other view components
    this.controller = new HomeController(this, home, preferences);
    return parent;
  }

  @Override
  protected ToolBarManager createToolBarManager(int style) {    
    ToolBarManager toolBarManager = new ToolBarManager(style);
    toolBarManager.add(this.actions.get(ActionType.ADD_HOME_FURNITURE));
    toolBarManager.add(this.actions.get(ActionType.DELETE_HOME_FURNITURE));
    toolBarManager.add(new Separator());
    toolBarManager.add(this.actions.get(ActionType.UNDO));
    toolBarManager.add(this.actions.get(ActionType.REDO));
    return toolBarManager;
  }
  
  @Override
  protected MenuManager createMenuManager() {
    // Create main menu manager
    MenuManager menuManager = new MenuManager();
    
    // Create Edit menu manager
    MenuManager editMenuManager = 
      new MenuManager(new ResourceAction(resource, "EDIT_MENU").getText());
    menuManager.add(editMenuManager);
    editMenuManager.add(this.actions.get(ActionType.UNDO));
    editMenuManager.add(this.actions.get(ActionType.REDO));

    // Create Furniture menu manager
    MenuManager furnitureMenuManager = 
      new MenuManager(new ResourceAction(resource, "FURNITURE_MENU").getText());
    menuManager.add(furnitureMenuManager);
    furnitureMenuManager.add(this.actions.get(ActionType.ADD_HOME_FURNITURE));
    furnitureMenuManager.add(this.actions.get(ActionType.DELETE_HOME_FURNITURE));

    return menuManager;
  }

  /**
   * Create menu and tool bar actions. 
   */
  private void createActions() {
    this.actions = new HashMap<ActionType, ResourceAction>();
    this.actions.put(ActionType.ADD_HOME_FURNITURE,
      new ResourceAction(resource, ActionType.ADD_HOME_FURNITURE.toString()) {
        @Override
        public void run() {
          controller.addHomeFurniture();
        }
      });
    this.actions.put(ActionType.DELETE_HOME_FURNITURE,
      new ResourceAction(resource, ActionType.DELETE_HOME_FURNITURE.toString()) {
        @Override
        public void run() {
          controller.getFurnitureController().deleteSelection();
        }
      });
    this.actions.put(ActionType.UNDO,
      new ResourceAction(resource, ActionType.UNDO.toString()){
        @Override
        public void run() {
          controller.undo();
        }
      });
    this.actions.put(ActionType.REDO,
      new ResourceAction(resource, ActionType.REDO.toString()){
        @Override
        public void run() {
          controller.redo();
        }
      });
  }

  /**
   * Enables or disables the action matching <code>actionType</code>.
   */
  public void setEnabled(ActionType actionType, 
                         boolean enabled) {
    this.actions.get(actionType).setEnabled(enabled);
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
    ResourceAction action = this.actions.get(actionType);
    if (name == null) {
      action.setText(action.getDefaultText());
      action.setToolTipText(action.getDefaultToolTipText());
    } else {
      action.setText("&" + name);
      action.setToolTipText(name);
    }
  }

  /**
   * Returns this application object. 
   */
  public HomeView createHomeView(Home home, UserPreferences preferences, HomeController controller) {
    return this;
  }

  public CatalogView createCatalogView(Catalog catalog, CatalogController controller) {
    return new CatalogTree(this.catalogFurnitureSashForm, catalog, controller);
  }

  public FurnitureView createFurnitureView(Home home, UserPreferences preferences, FurnitureController controller) {
    return new FurnitureTable(this.catalogFurnitureSashForm, home, preferences, controller);
  }
}
