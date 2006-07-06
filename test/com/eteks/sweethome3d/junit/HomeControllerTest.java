/*
 * HomeControllerTest.java 15 mai 2006
 * 
 * Copyright (c) 2006 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights
 * Reserved.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.eteks.sweethome3d.junit;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import junit.framework.TestCase;

import com.eteks.sweethome3d.io.DefaultUserPreferences;
import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.swing.CatalogTree;
import com.eteks.sweethome3d.swing.FurnitureTable;
import com.eteks.sweethome3d.swing.HomeController;

/**
 * Tests furniture table component.
 * @author Emmanuel Puybaret
 */
public class HomeControllerTest extends TestCase {
  public void testHomeFurniture() {
    // 1. Create model objects
    UserPreferences preferences = new DefaultUserPreferences();
    Home home = new Home();
    // Create home controller
    HomeController homeController = 
        new HomeController(home, preferences);
    // Retrieve table object created by home pane 
    CatalogTree tree = (CatalogTree)getComponent(
        homeController.getView(), CatalogTree.class);
    // Retrieve table object created by home pane 
    FurnitureTable table = (FurnitureTable)getComponent(
        homeController.getView(), FurnitureTable.class);

    // 2. Select the two first pieces of furniture in catalog and add them to the table
    tree.expandRow(0); 
    tree.addSelectionInterval(1, 2);
    homeController.addHomeFurniture();

    // Check the table contains two pieces
    assertEquals("Table doesn't contain 2 pieces", 
        2, table.getRowCount());
    //  Check the two pieces in table are selected
    assertEquals("Table doesn't display 2 selected pieces", 
        2, table.getSelectedRowCount());

    // 3. Select the first piece in table, delete it
    table.setRowSelectionInterval(0, 0);
    homeController.deleteHomeFurniture();
    // Check the table contains only one piece
    assertEquals("Table doesn't contain 1 piece", 
        1, table.getRowCount());
    // Check the table doesn't display any selection
    assertEquals("Table selection isn't empty", 
        0, table.getSelectedRowCount());
  }

  /**
   * Returns the first component of a given class in <code>container</code> hierarchy.
   */
  private Component getComponent(Container container, Class componentClass) {
    for (int i = 0, n = container.getComponentCount(); i < n; i++) {
      Component component = container.getComponent(i);
      if (componentClass.isInstance(component)) {
        return component;
      } else if (component instanceof Container) {
        component = getComponent((Container)component, componentClass);
        if (component != null) {
          return component;
        }
      }
    }
    return null;
  }

  public static void main(String [] args) throws InterruptedException, InvocationTargetException {
    UserPreferences preferences = new DefaultUserPreferences();
    Home home = new Home();
    new ControllerTest(home, preferences);
  }

  private static class ControllerTest extends HomeController {
    public ControllerTest(Home home, UserPreferences preferences) {
      super(home, preferences);
      new ViewTest(this).displayView();
    }
  }

  private static class ViewTest extends JRootPane {
    public ViewTest(final HomeController controller) {
      // Create buttons that will launch controler methods
      JButton addButton = new JButton(new ImageIcon(
          getClass().getResource("resources/Add16.gif")));
      addButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          controller.addHomeFurniture();
        }
      });
      JButton deleteButton = new JButton(new ImageIcon(
          getClass().getResource("resources/Delete16.gif")));
      deleteButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          controller.deleteHomeFurniture();
        }
      });
      // Put them it a tool bar
      JToolBar toolBar = new JToolBar();
      toolBar.add(addButton);
      toolBar.add(deleteButton);
      // Display the tool bar and main view in this pane
      getContentPane().add(toolBar, BorderLayout.NORTH);
      getContentPane().add(controller.getView(), BorderLayout.CENTER);
    }

    public void displayView() {
      JFrame frame = new JFrame("Home Controller Test") {
        {
          setRootPane(ViewTest.this);
        }
      };
      frame.pack();
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setVisible(true);
    } 
  }
}
