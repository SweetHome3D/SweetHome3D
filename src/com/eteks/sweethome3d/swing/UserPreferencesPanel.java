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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ResourceBundle;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.eteks.sweethome3d.model.UserPreferences;

/**
 * User preferences panel.
 * @author Emmanuel Puybaret
 */
public class UserPreferencesPanel extends JPanel {
  private ResourceBundle       resource;
  private JRadioButton         centimeterRadioButton;
  private JRadioButton         inchRadioButton;
  private JCheckBox            magnetismCheckBox;
  private SpinnerLengthModel   newWallThicknessModel;
  private SpinnerLengthModel   newHomeWallHeightModel;
  
  /**
   * Creates a preferences panel that layouts the mutable properties
   * of <code>preferences</code>. 
   */
  public UserPreferencesPanel() {
    super(new GridBagLayout());
    this.resource = ResourceBundle.getBundle(
            UserPreferencesPanel.class.getName());
    createComponentsAndModels();
    layoutComponents();
  }
  
  /**
   * Creates and initializes components and spinners model.
   */
  private void createComponentsAndModels() {
    this.centimeterRadioButton = new JRadioButton(
        this.resource.getString("centimeterRadioButton.text"), true);
    this.centimeterRadioButton.setMnemonic(
        this.resource.getString("centimeterRadioButton.mnemonic").charAt(0));
    this.inchRadioButton = new JRadioButton(
        this.resource.getString("inchRadioButton.text"));    
    this.inchRadioButton.setMnemonic(
        this.resource.getString("inchRadioButton.mnemonic").charAt(0));
    
    ButtonGroup unitButtonGroup = new ButtonGroup();
    unitButtonGroup.add(this.centimeterRadioButton);
    unitButtonGroup.add(this.inchRadioButton);
  
    this.magnetismCheckBox = new JCheckBox(
        this.resource.getString("magnetismCheckBox.text"));
    this.magnetismCheckBox.setMnemonic(
        this.resource.getString("magnetismCheckBox.mnemonic").charAt(0));
    
    this.newWallThicknessModel = new SpinnerLengthModel(
        0.5f, 100f, 0.5f, 0.125f, 
        this.centimeterRadioButton.getModel(), 
        this.inchRadioButton.getModel());
    this.newHomeWallHeightModel = new SpinnerLengthModel(
        1f, 1000f, 10f, 2f, 
        this.centimeterRadioButton.getModel(), 
        this.inchRadioButton.getModel());
  }
  
  /**
   * Layouts panel composants in panel with their labels. 
   */
  private void layoutComponents() {
    JLabel unitLabel = 
      new JLabel(this.resource.getString("unitLabel.text"));
    JLabel magnetismEnabledLabel = 
        new JLabel(this.resource.getString("magnetismLabel.text"));
    JLabel newWallThicknessLabel = 
        new JLabel(this.resource.getString("newWallThicknessLabel.text"));
    newWallThicknessLabel.setDisplayedMnemonic(
        this.resource.getString("newWallThicknessLabel.mnemonic").charAt(0));
    JLabel newHomeWallHeightLabel = 
        new JLabel(this.resource.getString("newHomeWallHeightLabel.text"));
    newHomeWallHeightLabel.setDisplayedMnemonic(
        this.resource.getString("newHomeWallHeightLabel.mnemonic").charAt(0));
    
    JSpinner newWallThicknessSpinner = 
        new JSpinner(this.newWallThicknessModel);
    newWallThicknessLabel.setLabelFor(newWallThicknessSpinner.getEditor());
    JSpinner newHomeWallHeightSpinner = 
        new JSpinner(this.newHomeWallHeightModel);
    newHomeWallHeightLabel.setLabelFor(newHomeWallHeightSpinner.getEditor());
    
    Insets labelInsets = new Insets(0, 0, 5, 5);
    add(unitLabel, new GridBagConstraints(
        0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    add(this.centimeterRadioButton, new GridBagConstraints(
        1, 0, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    Insets rightComponentInsets = new Insets(0, 0, 5, 0);
    add(this.inchRadioButton, new GridBagConstraints(
        2, 0, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.NONE, rightComponentInsets , 0, 0));
    
    add(magnetismEnabledLabel, new GridBagConstraints(
        0, 1, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    add(this.magnetismCheckBox, new GridBagConstraints(
        1, 1, 2, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.NONE, rightComponentInsets, 0, 0));
    
    add(newWallThicknessLabel, new GridBagConstraints(
        0, 2, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    add(newWallThicknessSpinner, new GridBagConstraints(
        1, 2, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.HORIZONTAL, rightComponentInsets, 0, 0));
    
    add(newHomeWallHeightLabel, new GridBagConstraints(
        0, 3, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
    add(newHomeWallHeightSpinner, new GridBagConstraints(
        1, 3, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
  }

  /**
   * Sets components value from <code>preferences</code>.
   */
  public void setPreferences(UserPreferences preferences) {
    this.magnetismCheckBox.setSelected(
        preferences.isMagnetismEnabled());    
    this.newWallThicknessModel.setValue(
        preferences.getNewWallThickness());
    this.newHomeWallHeightModel.setValue(
        preferences.getNewHomeWallHeight());
    // Initialize radio buttons at end because this may change
    // values displayed in spinners
    if (preferences.getUnit() == UserPreferences.Unit.INCH) {
      this.inchRadioButton.setSelected(true);
    } else {
      this.centimeterRadioButton.setSelected(true);
    }
  }
  
  /**
   * Displays this panel in a dialog box. 
   */
  public boolean showDialog(JComponent parent) {
    String dialogTitle = resource.getString("preferences.title");
    // TODO test setAlwaysOnTop
    return JOptionPane.showConfirmDialog(parent, this, dialogTitle, 
        JOptionPane.OK_CANCEL_OPTION, 
        JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION;
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
   * Returns the new wall thickness in panel.
   */
  public float getNewWallThickness() {
    return this.newWallThicknessModel.getLength();
  }

  /**
   * Returns the new home wall height in panel.
   */
  public float getNewHomeWallHeight() {
    return this.newHomeWallHeightModel.getLength();
  }

  /**
   * Returns <code>true</code> if magnetism is enabled in panel.
   */
  public boolean isMagnetismEnabled() {
    return this.magnetismCheckBox.isSelected();
  } 
  
  private static class SpinnerLengthModel extends SpinnerNumberModel {
    private UserPreferences.Unit unit = 
      UserPreferences.Unit.CENTIMETER;

    public SpinnerLengthModel(final float centimeterMin, 
                              final float centimeterMax, 
                              final float centimeterStepSize, 
                              final float inchStepSize,
                              final ButtonModel centimeterButtonModel,
                              final ButtonModel inchButtonModel) {
      super(centimeterMin, centimeterMin, centimeterMax, centimeterStepSize);
      // Add a listener to convert value, main, max and step 
      // to cemtimeter when button model is selected 
      centimeterButtonModel.addChangeListener(new ChangeListener () {
          public void stateChanged(ChangeEvent ev) {
            if (unit == UserPreferences.Unit.INCH
                && centimeterButtonModel.isSelected()) {
              setValue(UserPreferences.Unit.inchToCentimer(
                  getNumber().floatValue()));
              setStepSize(centimeterStepSize);
              setMinimum(centimeterMin);
              setMaximum(centimeterMax);
              unit = UserPreferences.Unit.CENTIMETER;
            }
          }
        });
      // Add a listener to convert value, min, max and step 
      // to inch when button model is selected 
      inchButtonModel.addChangeListener(new ChangeListener () {
          public void stateChanged(ChangeEvent ev) {
            if (unit == UserPreferences.Unit.CENTIMETER
                && inchButtonModel.isSelected()) {
              setValue(UserPreferences.Unit.centimerToInch(
                  getNumber().floatValue()));
              setStepSize(inchStepSize);
              setMinimum(UserPreferences.Unit.centimerToInch(centimeterMin));
              setMaximum(UserPreferences.Unit.centimerToInch(centimeterMax));
              unit = UserPreferences.Unit.INCH;
            }
          }
        });
    }

    /**
     * Returns the diplayed value in centimeter.
     */
    public float getLength() {
      if (unit == UserPreferences.Unit.INCH) {
        return UserPreferences.Unit.inchToCentimer(getNumber().floatValue());
      } else {
        return getNumber().floatValue();
      }
    }
  }
}
