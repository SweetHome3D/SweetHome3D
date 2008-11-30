/*
 * LabelPanel.java 29 nov. 2008
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.LabelController;
import com.eteks.sweethome3d.viewcontroller.View;

/**
 * Label editing panel.
 * @author Emmanuel Puybaret
 */
public class LabelPanel extends JPanel implements DialogView {
  private final boolean         labelModification;
  private final LabelController controller;
  private JLabel                textLabel;
  private JTextField            textTextField;
  private ResourceBundle        resource;

  /**
   * Creates a panel that displays label data.
   * @param modification specifies whether this panel edits an existing label or new one
   * @param preferences user preferences
   * @param controller the controller of this panel
   */
  public LabelPanel(boolean modification,
                    UserPreferences preferences,
                    LabelController controller) {
    super(new GridBagLayout());
    this.labelModification = modification;
    this.controller = controller;
    this.resource = ResourceBundle.getBundle(LabelPanel.class.getName());
    createComponents(controller);
    setMnemonics();
    layoutComponents(controller);
  }

  /**
   * Creates and initializes components.
   */
  private void createComponents(final LabelController controller) {
    // Create text label and its text field bound to NAME controller property
    this.textLabel = new JLabel(this.resource.getString("textLabel.text"));
    this.textTextField = new JTextField(controller.getText(), 20);
    if (!OperatingSystem.isMacOSX()) {
      SwingTools.addAutoSelectionOnFocusGain(this.textTextField);
    }
    final PropertyChangeListener textChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          textTextField.setText(controller.getText());
        }
      };
    controller.addPropertyChangeListener(LabelController.Property.TEXT, textChangeListener);
    this.textTextField.getDocument().addDocumentListener(new DocumentListener() {
        public void changedUpdate(DocumentEvent ev) {
          controller.removePropertyChangeListener(LabelController.Property.TEXT, textChangeListener);
          String text = textTextField.getText(); 
          if (text == null || text.trim().length() == 0) {
            controller.setText("");
          } else {
            controller.setText(text);
          }
          controller.addPropertyChangeListener(LabelController.Property.TEXT, textChangeListener);
        }
  
        public void insertUpdate(DocumentEvent ev) {
          changedUpdate(ev);
        }
  
        public void removeUpdate(DocumentEvent ev) {
          changedUpdate(ev);
        }
      });
  }

  /**
   * Sets components mnemonics and label / component associations.
   */
  private void setMnemonics() {
    if (!OperatingSystem.isMacOSX()) {
      this.textLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("textLabel.mnemonic")).getKeyCode());
      this.textLabel.setLabelFor(this.textTextField);
    }
  }
  
  /**
   * Layouts panel components in panel with their labels. 
   */
  private void layoutComponents(final LabelController controller) {
    int labelAlignment = OperatingSystem.isMacOSX() 
        ? GridBagConstraints.LINE_END
        : GridBagConstraints.LINE_START;
    add(this.textLabel, new GridBagConstraints(
        0, 0, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
    add(this.textTextField, new GridBagConstraints(
        0, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
  }

  /**
   * Displays this panel in a modal dialog box. 
   */
  public void displayView(View parentView) {
    String dialogTitle = resource.getString(this.labelModification 
        ? "labelModification.title"
        : "labelCreation.title");
    if (SwingTools.showConfirmDialog((JComponent)parentView, 
            dialogTitle, this, this.textTextField) == JOptionPane.OK_OPTION
        && this.controller != null) {
      if (this.labelModification) {
        this.controller.modifyLabels();
      } else {
        this.controller.createLabel();
      }
    }
  }
}
