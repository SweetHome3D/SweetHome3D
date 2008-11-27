/*
 * SwingTools.java 21 oct. 2008
 *
 * Copyright (c) 2008 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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

import java.awt.EventQueue;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;

/**
 * Gathers some useful tools for Swing.
 * @author Emmanuel Puybaret
 */
public class SwingTools {
  private SwingTools() {
    // This class contains only tools
  }

  /**
   * Updates the Swing resource bundle in use from the current Locale. 
   */
  public static void updateSwingResourceLanguage() {
    // Read Swing localized properties because Swing doesn't update its internal strings automatically
    // when default Locale is updated (see bug http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4884480)
    String [] swingResources = {"com.sun.swing.internal.plaf.basic.resources.basic",
                                "com.sun.swing.internal.plaf.metal.resources.metal"};
    for (String swingResource : swingResources) {
      ResourceBundle resource;
      try {
        resource = ResourceBundle.getBundle(swingResource);
      } catch (MissingResourceException ex) {
        resource = ResourceBundle.getBundle(swingResource, Locale.ENGLISH);
      }
      // Update UIManager properties
      for (Enumeration iter = resource.getKeys(); iter.hasMoreElements(); ) {
        String property = (String)iter.nextElement();
        UIManager.put(property, resource.getString(property));
      }      
    };
  }
  
  /**
   * Adds focus and mouse listeners to the given <code>textComponent</code> that will
   * select all its text when it gains focus by transfer.
   */
  public static void addAutoSelectionOnFocusGain(final JTextComponent textComponent) {
    // A focus and mouse listener able to select text field characters 
    // when it gains focus after a focus transfer
    class SelectionOnFocusManager extends MouseAdapter implements FocusListener {
      private boolean mousePressedInTextField = false;
      private int selectionStartBeforeFocusLost = -1;
      private int selectionEndBeforeFocusLost = -1;

      @Override
      public void mousePressed(MouseEvent ev) {
        this.mousePressedInTextField = true;
        this.selectionStartBeforeFocusLost = -1;
      }
      
      public void focusLost(FocusEvent ev) {
        if (ev.getOppositeComponent() == null
            || SwingUtilities.getWindowAncestor(ev.getOppositeComponent()) 
                != SwingUtilities.getWindowAncestor(textComponent)) {
          // Keep selection indices when focus on text field is transfered 
          // to an other window 
          this.selectionStartBeforeFocusLost = textComponent.getSelectionStart();
          this.selectionEndBeforeFocusLost = textComponent.getSelectionEnd();
        } else {
          this.selectionStartBeforeFocusLost = -1;
        }
      }

      public void focusGained(FocusEvent ev) {
        if (this.selectionStartBeforeFocusLost != -1) {
          EventQueue.invokeLater(new Runnable() {
              public void run() {
                // Reselect the same characters in text field
                textComponent.setSelectionStart(selectionStartBeforeFocusLost);
                textComponent.setSelectionEnd(selectionEndBeforeFocusLost);
              }
            });
        } else if (!this.mousePressedInTextField 
                   && ev.getOppositeComponent() != null
                   && SwingUtilities.getWindowAncestor(ev.getOppositeComponent()) 
                       == SwingUtilities.getWindowAncestor(textComponent)) {
          EventQueue.invokeLater(new Runnable() {
              public void run() {
                // Select all characters when text field got the focus because of a transfer
                textComponent.selectAll();
              }
            });
        }
        this.mousePressedInTextField = false;
      }
    };
    
    SelectionOnFocusManager selectionOnFocusManager = new SelectionOnFocusManager();
    textComponent.addFocusListener(selectionOnFocusManager);
    textComponent.addMouseListener(selectionOnFocusManager);
  }
  
  /**
   * Forces radio buttons to be deselected even if they belong to a button group. 
   */
  public static void deselectAllRadioButtons(JRadioButton ... radioButtons) {
    for (JRadioButton radioButton : radioButtons) {
      ButtonGroup group = ((JToggleButton.ToggleButtonModel)radioButton.getModel()).getGroup();
      group.remove(radioButton);
      radioButton.setSelected(false);
      group.add(radioButton);
    }    
  }
  
  /**
   * Displays <code>messageComponent</code> in a modal dialog box, giving focus to ones of its component. 
   */
  public static int showConfirmDialog(JComponent parentComponent,
                                      String title,
                                      JComponent messageComponent,
                                      final JComponent focusedComponent) {
    JOptionPane optionPane = new JOptionPane(messageComponent, 
        JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
    final JDialog dialog = optionPane.createDialog(SwingUtilities.getRootPane(parentComponent), title);
    // Add a listener that transfer focus to focusedComponent when dialog is shown
    dialog.addComponentListener(new ComponentAdapter() {
        @Override
        public void componentShown(ComponentEvent ev) {
          focusedComponent.requestFocusInWindow();
          dialog.removeComponentListener(this);
        }
      });
    dialog.setVisible(true);
    
    dialog.dispose();
    Object value = optionPane.getValue();
    if (value instanceof Integer) {
      return (Integer)value;
    } else {
      return JOptionPane.CLOSED_OPTION;
    }
  }
}
