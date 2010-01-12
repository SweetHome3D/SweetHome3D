/*
 * UnfocusableToolBar.java 12 janv. 2010
 *
 * Copyright (c) 2010 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JToolBar;

import com.eteks.sweethome3d.tools.OperatingSystem;

/**
 * A tool bar where all components are maintained unfocusable.
 * Under Mac OS X 10.5 and superior, it also uses segmented buttons and groups them.
 * @author Emmanuel Puybaret
 */
public class UnfocusableToolBar extends JToolBar {
  /**
   * Creates an unfocusable toolbar.
   */
  public UnfocusableToolBar() {
    // Update toolBar buttons when component orientation changes 
    // and when buttons are added or removed to it  
    addPropertyChangeListener("componentOrientation", 
        new PropertyChangeListener () {
          public void propertyChange(PropertyChangeEvent evt) {
            updateToolBarButtons();
          }
        });
    addContainerListener(new ContainerListener() {
        public void componentAdded(ContainerEvent ev) {
          updateToolBarButtons();
        }
        
        public void componentRemoved(ContainerEvent ev) {}
      });
  }

  /**
   * Ensures that all the children of this tool bar aren't focusable. 
   * Under Mac OS X 10.5, it also uses segmented buttons and groups them depending
   * on toolbar orientation and whether a button is after or before a separator.
   */
  private void updateToolBarButtons() {
    // Retrieve component orientation because Mac OS X 10.5 miserably doesn't it take into account 
    ComponentOrientation orientation = getComponentOrientation();
    Component previousComponent = null;
    for (int i = 0, n = getComponentCount(); i < n; i++) {        
      JComponent component = (JComponent)getComponentAtIndex(i); 
      // Remove focusable property on buttons
      component.setFocusable(false);
      
      if (!(component instanceof AbstractButton)) {
        previousComponent = null;
        continue;
      }          
      if (OperatingSystem.isMacOSXLeopardOrSuperior()) {
        Component nextComponent;
        if (i < n - 1) {
          nextComponent = getComponentAtIndex(i + 1);
        } else {
          nextComponent = null;
        }
        component.putClientProperty("JButton.buttonType", "segmentedTextured");
        if (previousComponent == null
            && !(nextComponent instanceof AbstractButton)) {
          component.putClientProperty("JButton.segmentPosition", "only");
        } else if (previousComponent == null) {
          component.putClientProperty("JButton.segmentPosition", 
              orientation.isLeftToRight() 
                ? "first"
                : "last");
        } else if (!(nextComponent instanceof AbstractButton)) {
          component.putClientProperty("JButton.segmentPosition",
              orientation.isLeftToRight() 
                ? "last"
                : "first");
        } else {
          component.putClientProperty("JButton.segmentPosition", "middle");
        }
        previousComponent = component;
      }
    }
  }
}
