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
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
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

import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.UserPreferencesController;
import com.eteks.sweethome3d.viewcontroller.UserPreferencesView;
import com.eteks.sweethome3d.viewcontroller.View;

/**
 * User preferences panel.
 * @author Emmanuel Puybaret
 */
public class UserPreferencesPanel extends JPanel implements UserPreferencesView {
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
  
  /**
   * Creates a preferences panel that layouts the mutable properties
   * of <code>preferences</code>. 
   */
  public UserPreferencesPanel(UserPreferences preferences,
                              UserPreferencesController userPreferencesController) {
    super(new GridBagLayout());
    this.controller = userPreferencesController;
    this.resource = ResourceBundle.getBundle(
            UserPreferencesPanel.class.getName());
    createComponents();
    setMnemonics();
    layoutComponents();
    setUserPreferences(preferences);
  }
  
  /**
   * Creates and initializes components and spinners model.
   */
  private void createComponents() {
    this.languageLabel = new JLabel(this.resource.getString("languageLabel.text"));    
    this.languageComboBox = new JComboBox();
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
    this.unitLabel = new JLabel(this.resource.getString("unitLabel.text"));
    this.centimeterRadioButton = new JRadioButton(
        this.resource.getString("centimeterRadioButton.text"), true);
    this.inchRadioButton = new JRadioButton(
        this.resource.getString("inchRadioButton.text"));    
    
    ButtonGroup unitButtonGroup = new ButtonGroup();
    unitButtonGroup.add(this.centimeterRadioButton);
    unitButtonGroup.add(this.inchRadioButton);
  
    this.magnetismEnabledLabel = new JLabel(this.resource.getString("magnetismLabel.text"));
    this.magnetismCheckBox = new JCheckBox(
        this.resource.getString("magnetismCheckBox.text"));
    
    this.rulersVisibleLabel = new JLabel(this.resource.getString("rulersLabel.text"));
    this.rulersCheckBox = new JCheckBox(
        this.resource.getString("rulersCheckBox.text"));
    
    this.gridVisibleLabel = new JLabel(this.resource.getString("gridLabel.text"));
    this.gridCheckBox = new JCheckBox(
        this.resource.getString("gridCheckBox.text"));
    
    this.newWallThicknessLabel = new JLabel(this.resource.getString("newWallThicknessLabel.text"));
    this.newWallThicknessSpinner = new AutoCommitSpinner(new SpinnerLengthModel(
        0.5f, 0.125f, this.centimeterRadioButton));
    this.newWallHeightLabel = new JLabel(this.resource.getString("newWallHeightLabel.text"));
    this.newWallHeightSpinner = new AutoCommitSpinner(new SpinnerLengthModel(
        10f, 2f, this.centimeterRadioButton));
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
        GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
    add(this.newWallHeightSpinner, new GridBagConstraints(
        1, 6, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
  }

  /**
   * Sets components value from <code>preferences</code>.
   */
  private void setUserPreferences(UserPreferences preferences) {
    this.languageComboBox.setModel(new DefaultComboBoxModel(preferences.getSupportedLanguages()));
    this.languageComboBox.setMaximumRowCount(this.languageComboBox.getModel().getSize());
    this.languageComboBox.setSelectedItem(preferences.getLanguage());
    if (preferences.getUnit() == UserPreferences.Unit.INCH) {
      this.inchRadioButton.setSelected(true);
    } else {
      this.centimeterRadioButton.setSelected(true);
    }
    this.magnetismCheckBox.setSelected(
        preferences.isMagnetismEnabled());    
    this.rulersCheckBox.setSelected(
        preferences.isRulersVisible());    
    this.gridCheckBox.setSelected(
        preferences.isGridVisible());    
    ((SpinnerLengthModel)this.newWallThicknessSpinner.getModel()).
        setLength(preferences.getNewWallThickness());
    ((SpinnerLengthModel)this.newWallHeightSpinner.getModel()).
        setLength(preferences.getNewWallHeight());
  }
  
  /**
   * Displays this panel in a dialog box. 
   */
  public void displayView(View parentView) {
    String dialogTitle = resource.getString("preferences.title");
    if (JOptionPane.showConfirmDialog((JComponent)parentView, this, dialogTitle, 
            JOptionPane.OK_CANCEL_OPTION, 
            JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION
        && this.controller != null) {
      this.controller.modifyUserPreferences();
    }
  }

  /**
   * Returns the chosen language in panel.
   */
  public String getLanguage() {
    return (String)this.languageComboBox.getSelectedItem();
  }

  /**
   * Returns the chosen unit in panel.
   */
  public UserPreferences.Unit getUnit() {
    if (this.inchRadioButton.isSelected()) {
      return UserPreferences.Unit.INCH;
    } else {
      return UserPreferences.Unit.CENTIMETER;
    }
  }

  /**
   * Returns <code>true</code> if magnetism is enabled in panel.
   */
  public boolean isMagnetismEnabled() {
    return this.magnetismCheckBox.isSelected();
  } 
  
  /**
   * Returns <code>true</code> if rulers are visible in panel.
   */
  public boolean isRulersVisible() {
    return this.rulersCheckBox.isSelected();
  } 
  
  /**
   * Returns <code>true</code> if grid is visible in panel.
   */
  public boolean isGridVisible() {
    return this.gridCheckBox.isSelected();
  } 
  
  /**
   * Returns the new wall thickness in panel.
   */
  public float getNewWallThickness() {
    return ((SpinnerLengthModel)this.newWallThicknessSpinner.getModel()).getLength();
  }

  /**
   * Returns the new wall height in panel.
   */
  public float getNewWallHeight() {
    return ((SpinnerLengthModel)this.newWallHeightSpinner.getModel()).getLength();
  }

  private static class SpinnerLengthModel extends SpinnerNumberModel {
    private UserPreferences.Unit unit = 
      UserPreferences.Unit.CENTIMETER;

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
              if (unit == UserPreferences.Unit.INCH) {
                setStepSize(centimeterStepSize);
                setValue(UserPreferences.Unit.inchToCentimeter(
                    getNumber().floatValue()));
                unit = UserPreferences.Unit.CENTIMETER;
              }
            } else {
              if (unit == UserPreferences.Unit.CENTIMETER) {
                setStepSize(inchStepSize);
                setValue(UserPreferences.Unit.centimeterToInch(
                    getNumber().floatValue()));
                unit = UserPreferences.Unit.INCH;
              }
            }
          }
        });
    }

    /**
     * Returns the displayed value in centimeter.
     */
    public float getLength() {
      if (unit == UserPreferences.Unit.INCH) {
        return UserPreferences.Unit.inchToCentimeter(getNumber().floatValue());
      } else {
        return getNumber().floatValue();
      }
    }

    /**
     * Sets the length in centimeter displayed in this model.
     */
    public void setLength(float length) {
      if (unit == UserPreferences.Unit.INCH) {
        length = UserPreferences.Unit.centimeterToInch(length);
      } 
      setValue(length);
    }
  }
}
