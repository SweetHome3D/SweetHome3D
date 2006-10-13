/*
 * JFaceFurnitureTableTest.java 20 juin 2006
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

import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEditSupport;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
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
import com.eteks.sweethome3d.jface.PlanViewer;
import com.eteks.sweethome3d.model.Catalog;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.viewcontroller.CatalogController;
import com.eteks.sweethome3d.viewcontroller.CatalogView;
import com.eteks.sweethome3d.viewcontroller.FurnitureController;
import com.eteks.sweethome3d.viewcontroller.FurnitureView;
import com.eteks.sweethome3d.viewcontroller.HomeController;
import com.eteks.sweethome3d.viewcontroller.HomeView;
import com.eteks.sweethome3d.viewcontroller.PlanController;
import com.eteks.sweethome3d.viewcontroller.PlanView;
import com.eteks.sweethome3d.viewcontroller.ViewFactory;

/**
 * Tests plan viewer JFace implementation.
 * @author Emmanuel Puybaret
 */
public class JFacePlanViewerTest {
  public static void main(String [] args) {
    final UserPreferences preferences = new DefaultUserPreferences();
    final Home home = new Home();
    
    // Create an application window that displays an instance of HomeComposite with a toolbar
    ApplicationWindow window = new ApplicationWindow(null) {
      private PlanController controller;
      
      protected void configureShell(Shell shell) {
        shell.setText("Plan Control Test");
        shell.setLayout(new GridLayout());
      }

      protected Control createContents(final Composite parent) {
        UndoManager undoManager = new UndoManager();
        CoolBar coolBar = createCoolBar(parent, undoManager);
        final ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        
        // Set how plan view will be created
        ViewFactory planViewFactory = new ViewFactory() {
          public PlanView createPlanView(
              Home home, UserPreferences userPreferences, PlanController controller) {
            return new PlanViewer(scrolledComposite, home, preferences, controller);
          }

          // Other components won't be created in this test
          public HomeView createHomeView(Home home, UserPreferences preferences, 
                                         HomeController controller) {
            return null;
          }

          public CatalogView createCatalogView(Catalog catalog, CatalogController controller) {
            return null;
          }

          public FurnitureView createFurnitureView(Home home, UserPreferences preferences, 
                                                   FurnitureController controller) {
            return null;
          }
        };
        // Create controller and the plan view 
        UndoableEditSupport undoSupport = new UndoableEditSupport(); 
        undoSupport.addUndoableEditListener(undoManager);
        this.controller = new PlanController(
            planViewFactory, home, preferences, undoSupport);
        
        Viewer planViewer = (Viewer)controller.getView();
        // Configure scrolledComposite content with planViewer control 
        scrolledComposite.setContent(planViewer.getControl());
        scrolledComposite.setMinSize(planViewer.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT));
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        // Let scrolledComposite fill the space under the coolBar
        scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        return parent;
      }

      private CoolBar createCoolBar(Composite parent, final UndoManager undoManager) {
        CoolBar coolBar = new CoolBar(parent, SWT.NONE);
        ToolBar editToolBar = new ToolBar(coolBar, SWT.NONE);
        // Add button
        final ToolItem addToolItem = new ToolItem(editToolBar, SWT.CHECK);
        addToolItem.setImage(new Image(Display.getCurrent(),
                getClass().getResourceAsStream("resources/Add16.gif")));
        addToolItem.addSelectionListener(new SelectionAdapter () {
          @Override
          public void widgetSelected(SelectionEvent e) {
            if (addToolItem.getSelection()) {
              controller.setMode(PlanController.Mode.WALL_CREATION); 
            } else {
              controller.setMode(PlanController.Mode.SELECTION); 
            }             
          } 
        });
        // Undo button
        ToolItem undoToolItem = new ToolItem(editToolBar, SWT.PUSH);
        undoToolItem.setImage(new Image(Display.getCurrent(),
                getClass().getResourceAsStream("resources/Undo16.gif")));
        undoToolItem.addSelectionListener(new SelectionAdapter () {
          @Override
          public void widgetSelected(SelectionEvent e) {
            undoManager.undo();
          } 
        });
        // Redo button
        ToolItem redoToolItem = new ToolItem(editToolBar, SWT.PUSH);
        redoToolItem.setImage(new Image(Display.getCurrent(),
                getClass().getResourceAsStream("resources/Redo16.gif")));
        redoToolItem.addSelectionListener(new SelectionAdapter () {
          @Override
          public void widgetSelected(SelectionEvent e) {
            undoManager.redo();
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
