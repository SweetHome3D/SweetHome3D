/*
 * UserPreferencesPanel.java 18 sept. 2006
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

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.eteks.sweethome3d.model.LengthUnit;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.UserPreferencesController;
import com.eteks.sweethome3d.viewcontroller.View;

/**
 * User preferences panel.
 * @author Emmanuel Puybaret
 */
public class UserPreferencesPanel extends JPanel implements DialogView {
  private final UserPreferencesController controller;
  private ResourceBundle resource;
  private JLabel         languageLabel;
  private JComboBox      languageComboBox;
  private JLabel         unitLabel;
  private JRadioButton   centimeterRadioButton;
  private JRadioButton   inchRadioButton;
  private JLabel         magnetismEnabledLabel;
  private JCheckBox      magnetismCheckBox;
  private JLabel         rulersVisibleLabel;
  private JCheckBox      rulersCheckBox;
  private JLabel         gridVisibleLabel;
  private JCheckBox      gridCheckBox;
  private JLabel         newWallThicknessLabel;
  private JSpinner       newWallThicknessSpinner;
  private JLabel         newWallHeightLabel;
  private JSpinner       newWallHeightSpinner;
  private JButton        resetDisplayedActionTipsButton;
  
  /**
   * Creates a preferences panel that layouts the editable properties
   * of its <code>controller</code>. 
   */
  public UserPreferencesPanel(UserPreferences preferences,
                              UserPreferencesController controller) {
    super(new GridBagLayout());
    this.controller = controller;
    this.resource = ResourceBundle.getBundle(
            UserPreferencesPanel.class.getName());
    createComponents(preferences, controller);
    setMnemonics();
    layoutComponents();
  }
  
  /**
   * Creates and initializes components and spinners model.
   */
  private void createComponents(UserPreferences preferences,
                                final UserPreferencesController controller) {
    // Create language label and combo box bound to controller LANGUAGE property
    this.languageLabel = new JLabel(this.resource.getString("languageLabel.text"));    
    this.languageComboBox = new JComboBox(new DefaultComboBoxModel(preferences.getSupportedLanguages()));
    this.languageComboBox.setRenderer(new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(JList list, 
            Object value, int index, boolean isSelected, boolean cellHasFocus) {
          Locale locale = new Locale((String)value);
          String displayedValue = locale.getDisplayLanguage(locale);
          displayedValue = Character.toUpperCase(displayedValue.charAt(0)) + displayedValue.substring(1);
          return super.getListCellRendererComponent(list, displayedValue, index, isSelected,
              cellHasFocus);
        }
      });
    this.languageComboBox.setMaximumRowCount(this.languageComboBox.getModel().getSize());
    this.languageComboBox.setSelectedItem(controller.getLanguage());
    this.languageComboBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          controller.setLanguage((String)languageComboBox.getSelectedItem());
        }
      });
    controller.addPropertyChangeListener(UserPreferencesController.Property.LANGUAGE, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            languageComboBox.setSelectedItem(controller.getLanguage());
          }
        });
    
    // Create unit label and radio buttons bound to controller UNIT property
    this.unitLabel = new JLabel(this.resource.getString("unitLabel.text"));
    this.centimeterRadioButton = new JRadioButton(this.resource.getString("centimeterRadioButton.text"), 
        controller.getUnit() == LengthUnit.CENTIMETER);
    this.inchRadioButton = new JRadioButton(this.resource.getString("inchRadioButton.text"), 
        controller.getUnit() == LengthUnit.INCH);
    ButtonGroup unitButtonGroup = new ButtonGroup();
    unitButtonGroup.add(this.centimeterRadioButton);
    unitButtonGroup.add(this.inchRadioButton);

    ItemListener unitChangeListener = new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          controller.setUnit(centimeterRadioButton.isSelected() 
              ? LengthUnit.CENTIMETER
              : LengthUnit.INCH);
        }
      };
    this.centimeterRadioButton.addItemListener(unitChangeListener);
    this.inchRadioButton.addItemListener(unitChangeListener);
    controller.addPropertyChangeListener(UserPreferencesController.Property.UNIT, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            centimeterRadioButton.setSelected(controller.getUnit() == LengthUnit.CENTIMETER);
          }
        });

    // Create magnetism label and check box bound to controller MAGNETISM_ENABLED property
    this.magnetismEnabledLabel = new JLabel(this.resource.getString("magnetismLabel.text"));
    this.magnetismCheckBox = new JCheckBox(this.resource.getString("magnetismCheckBox.text"), 
        controller.isMagnetismEnabled());
    this.magnetismCheckBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          controller.setMagnetismEnabled(magnetismCheckBox.isSelected());
        }
      });
    controller.addPropertyChangeListener(UserPreferencesController.Property.MAGNETISM_ENABLED, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            magnetismCheckBox.setSelected(controller.isMagnetismEnabled());
          }
        });

    // Create rulers label and check box bound to controller RULERS_VISIBLE property
    this.rulersVisibleLabel = new JLabel(this.resource.getString("rulersLabel.text"));
    this.rulersCheckBox = new JCheckBox(this.resource.getString("rulersCheckBox.text"),
        controller.isRulersVisible());
    this.rulersCheckBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          controller.setRulersVisible(rulersCheckBox.isSelected());
        }
      });
    controller.addPropertyChangeListener(UserPreferencesController.Property.RULERS_VISIBLE, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            rulersCheckBox.setSelected(controller.isRulersVisible());
          }
        });
    
    // Create grid label and check box bound to controller GRID_VISIBLE property
    this.gridVisibleLabel = new JLabel(this.resource.getString("gridLabel.text"));
    this.gridCheckBox = new JCheckBox(this.resource.getString("gridCheckBox.text"),
        controller.isGridVisible());
    this.gridCheckBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          controller.setGridVisible(gridCheckBox.isSelected());
        }
      });
    controller.addPropertyChangeListener(UserPreferencesController.Property.GRID_VISIBLE, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            gridCheckBox.setSelected(controller.isGridVisible());
          }
        });
    
    // Create wall thickness label and spinner bound to controller NEW_WALL_THICKNESS property
    this.newWallThicknessLabel = new JLabel(this.resource.getString("newWallThicknessLabel.text"));
    final SpinnerLengthModel newWallThicknessSpinnerModel = new SpinnerLengthModel(
        0.5f, 0.125f, this.centimeterRadioButton);
    this.newWallThicknessSpinner = new AutoCommitSpinner(newWallThicknessSpinnerModel);
    newWallThicknessSpinnerModel.setLength(controller.getNewWallThickness());
    newWallThicknessSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.setNewWallThickness(newWallThicknessSpinnerModel.getLength());
        }
      });
    controller.addPropertyChangeListener(UserPreferencesController.Property.NEW_WALL_THICKNESS, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            newWallThicknessSpinnerModel.setLength(controller.getNewWallThickness());
          }
        });
    
    
    // Create wall height label and spinner bound to controller NEW_WALL_HEIGHT property
    this.newWallHeightLabel = new JLabel(this.resource.getString("newWallHeightLabel.text"));
    final SpinnerLengthModel newWallHeightSpinnerModel = new SpinnerLengthModel(
        10f, 2f, this.centimeterRadioButton);
    this.newWallHeightSpinner = new AutoCommitSpinner(newWallHeightSpinnerModel);
    newWallHeightSpinnerModel.setLength(controller.getNewWallHeight());
    newWallHeightSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.setNewWallHeight(newWallHeightSpinnerModel.getLength());
        }
      });
    controller.addPropertyChangeListener(UserPreferencesController.Property.NEW_WALL_HEIGHT, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            newWallHeightSpinnerModel.setLength(controller.getNewWallHeight());
          }
        });
    
    this.resetDisplayedActionTipsButton = new JButton(new ResourceAction.ButtonAction(
        new ResourceAction(this.resource, "RESET_DISPLAYED_ACTION_TIPS", true) {
          @Override
          public void actionPerformed(ActionEvent ev) {
            controller.resetDisplayedActionTips();
          }
        }));
  }
  
  /**
   * Sets components mnemonics and label / component associations.
   */
  private void setMnemonics() {
    if (!OperatingSystem.isMacOSX()) {
      this.languageLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("languageLabel.mnemonic")).getKeyCode());
      this.languageLabel.setLabelFor(this.languageComboBox);
      this.centimeterRadioButton.setMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("centimeterRadioButton.mnemonic")).getKeyCode());
      this.inchRadioButton.setMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("inchRadioButton.mnemonic")).getKeyCode());
      this.magnetismCheckBox.setMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("magnetismCheckBox.mnemonic")).getKeyCode());
      this.rulersCheckBox.setMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("rulersCheckBox.mnemonic")).getKeyCode());
      this.gridCheckBox.setMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("gridCheckBox.mnemonic")).getKeyCode());
      this.newWallThicknessLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("newWallThicknessLabel.mnemonic")).getKeyCode());
      this.newWallThicknessLabel.setLabelFor(this.newWallThicknessSpinner);
      this.newWallHeightLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("newWallHeightLabel.mnemonic")).getKeyCode());
      this.newWallHeightLabel.setLabelFor(this.newWallHeightSpinner);
    }
  }
  
  /**
   * Layouts panel components in panel with their labels. 
   */
  private void layoutComponents() {
    int labelAlignment = OperatingSystem.isMacOSX() 
        ? GridBagConstraints.LINE_END
        : GridBagConstraints.LINE_START;
    Insets labelInsets = new Insets(0, 0, 5, 5);
    // First row
    add(this.languageLabel, new GridBagConstraints(
        0, 0, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    Insets rightComponentInsets = new Insets(0, 0, 5, 0);
    add(this.languageComboBox, new GridBagConstraints(
        1, 0, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, rightComponentInsets, 0, 0));
    // Second row
    add(this.unitLabel, new GridBagConstraints(
        0, 1, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    add(this.centimeterRadioButton, new GridBagConstraints(
        1, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    add(this.inchRadioButton, new GridBagConstraints(
        2, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, rightComponentInsets , 0, 0));
    // Third row
    add(this.magnetismEnabledLabel, new GridBagConstraints(
        0, 2, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    add(this.magnetismCheckBox, new GridBagConstraints(
        1, 2, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, rightComponentInsets, 0, 0));
    // Fourth row
    add(this.rulersVisibleLabel, new GridBagConstraints(
        0, 3, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    add(this.rulersCheckBox, new GridBagConstraints(
        1, 3, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, rightComponentInsets, 0, 0));
    // Fifth row
    add(this.gridVisibleLabel, new GridBagConstraints(
        0, 4, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    add(this.gridCheckBox, new GridBagConstraints(
        1, 4, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, rightComponentInsets, 0, 0));
    // Sixth row
    add(this.newWallThicknessLabel, new GridBagConstraints(
        0, 5, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    add(this.newWallThicknessSpinner, new GridBagConstraints(
        1, 5, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, rightComponentInsets, 0, 0));
    // Seventh row
    add(this.newWallHeightLabel, new GridBagConstraints(
        0, 6, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    add(this.newWallHeightSpinner, new GridBagConstraints(
        1, 6, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, rightComponentInsets, 0, 0));
    // Last row
    add(this.resetDisplayedActionTipsButton, new GridBagConstraints(
        0, 7, 3, 1, 0, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
  }

  /**
   * Displays this panel in a dialog box. 
   */
  public void displayView(View parentView) {
    String dialogTitle = resource.getString("preferences.title");
    if (SwingTools.showConfirmDialog((JComponent)parentView, 
            this, dialogTitle, this.languageComboBox) == JOptionPane.OK_OPTION
        && this.controller != null) {
      this.controller.modifyUserPreferences();
    }
  }

  private static class SpinnerLengthModel extends SpinnerNumberModel {
    private LengthUnit unit = 
      LengthUnit.CENTIMETER;

    public SpinnerLengthModel(final float centimeterStepSize, 
                              final float inchStepSize,
                              final AbstractButton centimeterButton) {
      // Invoke constructor that take objects in parameter to avoid any ambiguity
      super(new Float(1f), new Float(0f), new Float(100000f), new Float(centimeterStepSize));
      // Add a listener to convert value and step 
      // to centimeter when button model is selected 
      centimeterButton.addChangeListener(
        new ChangeListener () {
          public void stateChanged(ChangeEvent ev) {
            if (centimeterButton.isSelected()) {
              if (unit == LengthUnit.INCH) {
                setStepSize(centimeterStepSize);
                setValue(LengthUnit.inchToCentimeter(
                    getNumber().floatValue()));
                unit = LengthUnit.CENTIMETER;
              }
            } else {
              if (unit == LengthUnit.CENTIMETER) {
                setStepSize(inchStepSize);
                setValue(LengthUnit.centimeterToInch(
                    getNumber().floatValue()));
                unit = LengthUnit.INCH;
              }
            }
          }
        });
    }

    /**
     * Returns the displayed value in centimeter.
     */
    public float getLength() {
      if (unit == LengthUnit.INCH) {
        return LengthUnit.inchToCentimeter(getNumber().floatValue());
      } else {
        return getNumber().floatValue();
      }
    }

    /**
     * Sets the length in centimeter displayed in this model.
     */
    public void setLength(float length) {
      if (unit == LengthUnit.INCH) {
        length = LengthUnit.centimeterToInch(length);
      } 
      setValue(length);
    }
  }
}
