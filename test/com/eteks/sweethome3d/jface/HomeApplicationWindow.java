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

import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.viewcontroller.HomeController;
import com.eteks.sweethome3d.viewcontroller.HomeView;

/**
 * The MVC application view of Sweet Home 3D. 
 * @author Emmanuel Puybaret
 */
public class HomeApplicationWindow extends ApplicationWindow implements HomeView {
  private Home home;
  private UserPreferences preferences;

  private Map<ActionType, ResourceAction> actions;

  public HomeApplicationWindow(Home home, UserPreferences preferences, HomeController controller) {
    super(null);
    this.home = home;
    this.preferences = preferences;
    createActions(controller);  
    addMenuBar();
    addCoolBar(SWT.NONE);
  }

  @Override
  protected void configureShell(Shell shell) {
    super.configureShell(shell);
    shell.setText("Sweet Home 3D");
    shell.setLayout(new GridLayout());
  }

  @Override
  protected Control createContents(Composite parent) {
    // Create the other view components
    Composite homeComposite = new HomeComposite(parent, home, preferences).getHomeComposite();
    homeComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
    return parent;
  }
  
  @Override
  protected CoolBarManager createCoolBarManager(int style) {    
    ToolBarManager toolBarManager = new ToolBarManager();
    toolBarManager.add(this.actions.get(ActionType.ADD_HOME_FURNITURE));
    toolBarManager.add(this.actions.get(ActionType.DELETE_HOME_FURNITURE));
    toolBarManager.add(new Separator());
    toolBarManager.add(this.actions.get(ActionType.UNDO));
    toolBarManager.add(this.actions.get(ActionType.REDO));
    
    CoolBarManager coolBarManager = new CoolBarManager(style);
    coolBarManager.add(toolBarManager);
    return coolBarManager;
  }
  
  @Override
  protected MenuManager createMenuManager() {
    ResourceBundle resource = ResourceBundle.getBundle(
        HomeApplicationWindow.class.getName());
    
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

  private void createActions(final HomeController controller) {
    this.actions = new HashMap<ActionType, ResourceAction>();
    ResourceBundle resource = ResourceBundle.getBundle(
        HomeApplicationWindow.class.getName());
    try {
      this.actions.put(ActionType.ADD_HOME_FURNITURE,
          new ControllerAction(resource, ActionType.ADD_HOME_FURNITURE.toString(),
              controller, "addHomeFurniture"));
      this.actions.put(ActionType.DELETE_HOME_FURNITURE,
          new ControllerAction(resource, ActionType.DELETE_HOME_FURNITURE.toString(),
              controller, "deleteHomeFurniture"));
      this.actions.put(ActionType.UNDO,
          new ControllerAction(resource, ActionType.UNDO.toString(),
              controller, "undo"));
      this.actions.put(ActionType.REDO,
          new ControllerAction(resource, ActionType.REDO.toString(),
              controller, "redo"));
    } catch (NoSuchMethodException ex) {
      throw new RuntimeException(ex);
    }
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
    }
    action.setText("&" + name);
    action.setToolTipText(name);
  }
}
