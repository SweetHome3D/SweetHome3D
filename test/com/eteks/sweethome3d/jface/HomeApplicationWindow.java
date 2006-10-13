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

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

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
  private SashForm        catalogFurnitureSashForm;
  
  public HomeApplicationWindow(Home home, UserPreferences preferences) {
    super(null);
    this.home = home;
    this.preferences = preferences;
  }

  @Override
  protected void configureShell(Shell shell) {
    shell.setText("Home Controller Test");
  }

  @Override
  protected Control createContents(Composite parent) {
    parent.setLayout(new GridLayout());
    CoolBar coolBar = createCoolBar(parent);
    this.catalogFurnitureSashForm = new SashForm(parent, SWT.VERTICAL);
    this.catalogFurnitureSashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
    // Create controller and the other view components
    this.controller = new HomeController(this, home, preferences);
    return parent;
  }

  private CoolBar createCoolBar(Composite parent) {
    CoolBar coolBar = new CoolBar(parent, SWT.NONE);
    ToolBar editToolBar = new ToolBar(coolBar, SWT.NONE);
    // Add button
    ToolItem addToolItem = new ToolItem(editToolBar, SWT.PUSH);
    addToolItem.setImage(new Image(Display.getCurrent(),
            getClass().getResourceAsStream("resources/Add16.gif")));
    addToolItem.addSelectionListener(new SelectionAdapter () {
      @Override
      public void widgetSelected(SelectionEvent e) {
        controller.addHomeFurniture();
      } 
    });
    // Delete button
    ToolItem deleteToolItem = new ToolItem(editToolBar, SWT.PUSH);
    deleteToolItem.setImage(new Image(Display.getCurrent(),
            getClass().getResourceAsStream("resources/Delete16.gif")));
    deleteToolItem.addSelectionListener(new SelectionAdapter () {
      @Override
      public void widgetSelected(SelectionEvent e) {
        controller.getFurnitureController().deleteSelection();
      } 
    });

    CoolItem coolItem = new CoolItem(coolBar, SWT.NONE);
    coolItem.setControl(editToolBar);
    // Compute coolItem size
    Point size = editToolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
    coolItem.setSize(coolItem.computeSize(size.x, size.y));
    return coolBar;
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
