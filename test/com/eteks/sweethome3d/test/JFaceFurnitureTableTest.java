/*
 * JFaceTreeCatalogTest.java 1 mai 2006
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
package com.eteks.sweethome3d.test;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
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

import com.eteks.sweethome3d.io.DefaultUserPreferences;
import com.eteks.sweethome3d.jface.HomeComposite;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.viewcontroller.HomeController;

/**
 * Tests furniture furniture table JFace implementation.
 * @author Emmanuel Puybaret
 */
public class JFaceFurnitureTableTest {
  public static void main(String [] args) {
    final UserPreferences preferences = new DefaultUserPreferences();
    final Home home = new Home();
    
    // Create an application window that displays an instance of HomeComposite with a toolbar
    ApplicationWindow window = new ApplicationWindow(null) {
      private HomeController controller;
      
      protected void configureShell(Shell shell) {
        shell.setText("Category Tree Test");
        shell.setLayout(new GridLayout());
      }

      protected Control createContents(Composite parent) {
        CoolBar coolBar = createCoolBar(parent);
        // As SWT requires a parent for each component, we're obliged to create 
        // first HomeComposite which implements also ViewFactory
        HomeComposite homeComposite = new HomeComposite(parent);
        homeComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        // Create controller and the other view components
        this.controller = new HomeController(homeComposite, home, preferences);
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
            controller.getFurnitureController().deleteFurniture();
          } 
        });
        // Undo button
        ToolItem undoToolItem = new ToolItem(editToolBar, SWT.PUSH);
        undoToolItem.setImage(new Image(Display.getCurrent(),
                getClass().getResourceAsStream("resources/Undo16.gif")));
        undoToolItem.addSelectionListener(new SelectionAdapter () {
          @Override
          public void widgetSelected(SelectionEvent e) {
            controller.undo();
          } 
        });
        // Redo button
        ToolItem redoToolItem = new ToolItem(editToolBar, SWT.PUSH);
        redoToolItem.setImage(new Image(Display.getCurrent(),
                getClass().getResourceAsStream("resources/Redo16.gif")));
        redoToolItem.addSelectionListener(new SelectionAdapter () {
          @Override
          public void widgetSelected(SelectionEvent e) {
            controller.redo();
          } 
        });

        CoolItem coolItem = new CoolItem(coolBar, SWT.NONE);
        coolItem.setControl(editToolBar);
        // Compute coolItem size
        Point size = editToolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        coolItem.setSize(coolItem.computeSize(size.x, size.y));
        return coolBar;
      }
    };
    window.setBlockOnOpen(true);
    window.open();
    Display.getCurrent().dispose(); 
  } 
}
