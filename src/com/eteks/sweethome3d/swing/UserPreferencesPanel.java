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

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.eteks.sweethome3d.model.UserPreferences;

/**
 * User preferences panel.
 * @author Emmanuel Puybaret
 */
public class UserPreferencesPanel extends JPanel {
  private ResourceBundle resource;
  private JLabel         unitLabel;
  private JRadioButton   centimeterRadioButton;
  private JRadioButton   inchRadioButton;
  private JLabel         magnetismEnabledLabel;
  private JCheckBox      magnetismCheckBox;
  private JLabel         rulersVisibleLabel;
  private JCheckBox      rulersCheckBox;
  private JLabel         newWallThicknessLabel;
  private JSpinner       newWallThicknessSpinner;
  private JLabel         newHomeWallHeightLabel;
  private JSpinner       newHomeWallHeightSpinner;
  
  /**
   * Creates a preferences panel that layouts the mutable properties
   * of <code>preferences</code>. 
   */
  public UserPreferencesPanel() {
    super(new GridBagLayout());
    this.resource = ResourceBundle.getBundle(
            UserPreferencesPanel.class.getName());
    createComponents();
    setMnemonics();
    layoutComponents();
  }
  
  /**
   * Creates and initializes components and spinners model.
   */
  private void createComponents() {
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
    
    this.newWallThicknessLabel = new JLabel(this.resource.getString("newWallThicknessLabel.text"));
    this.newWallThicknessSpinner = new JSpinner(new SpinnerLengthModel(
        0.5f, 0.125f, this.centimeterRadioButton));
    this.newHomeWallHeightLabel = new JLabel(this.resource.getString("newHomeWallHeightLabel.text"));
    this.newHomeWallHeightSpinner = new JSpinner(new SpinnerLengthModel(
        10f, 2f, this.centimeterRadioButton));
  }
  
  /**
   * Sets components mnemonics and label / component associations.
   */
  private void setMnemonics() {
    if (!System.getProperty("os.name").startsWith("Mac OS X")) {
      this.centimeterRadioButton.setMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("centimeterRadioButton.mnemonic")).getKeyCode());
      this.inchRadioButton.setMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("inchRadioButton.mnemonic")).getKeyCode());
      this.magnetismCheckBox.setMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("magnetismCheckBox.mnemonic")).getKeyCode());
      this.rulersCheckBox.setMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("rulersCheckBox.mnemonic")).getKeyCode());
      this.newWallThicknessLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("newWallThicknessLabel.mnemonic")).getKeyCode());
      this.newWallThicknessLabel.setLabelFor(this.newWallThicknessSpinner);
      this.newHomeWallHeightLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("newHomeWallHeightLabel.mnemonic")).getKeyCode());
      this.newHomeWallHeightLabel.setLabelFor(this.newHomeWallHeightSpinner);
    }
  }
  
  /**
   * Layouts panel composants in panel with their labels. 
   */
  private void layoutComponents() {
    Insets labelInsets = new Insets(0, 0, 5, 5);
    add(this.unitLabel, new GridBagConstraints(
        0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    add(this.centimeterRadioButton, new GridBagConstraints(
        1, 0, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    Insets rightComponentInsets = new Insets(0, 0, 5, 0);
    add(this.inchRadioButton, new GridBagConstraints(
        2, 0, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.NONE, rightComponentInsets , 0, 0));
    
    add(this.magnetismEnabledLabel, new GridBagConstraints(
        0, 1, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    add(this.magnetismCheckBox, new GridBagConstraints(
        1, 1, 2, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.NONE, rightComponentInsets, 0, 0));
    
    add(this.rulersVisibleLabel, new GridBagConstraints(
        0, 2, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    add(this.rulersCheckBox, new GridBagConstraints(
        1, 2, 2, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.NONE, rightComponentInsets, 0, 0));
    
    add(this.newWallThicknessLabel, new GridBagConstraints(
        0, 3, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    add(this.newWallThicknessSpinner, new GridBagConstraints(
        1, 3, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.HORIZONTAL, rightComponentInsets, 0, 0));
    
    add(this.newHomeWallHeightLabel, new GridBagConstraints(
        0, 4, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
    add(this.newHomeWallHeightSpinner, new GridBagConstraints(
        1, 4, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
  }

  /**
   * Sets components value from <code>preferences</code>.
   */
  public void setPreferences(UserPreferences preferences) {
    if (preferences.getUnit() == UserPreferences.Unit.INCH) {
      this.inchRadioButton.setSelected(true);
    } else {
      this.centimeterRadioButton.setSelected(true);
    }
    this.magnetismCheckBox.setSelected(
        preferences.isMagnetismEnabled());    
    this.rulersCheckBox.setSelected(
        preferences.isRulersVisible());    
    ((SpinnerLengthModel)this.newWallThicknessSpinner.getModel()).
        setLength(preferences.getNewWallThickness());
    ((SpinnerLengthModel)this.newHomeWallHeightSpinner.getModel()).
        setLength(preferences.getNewHomeWallHeight());
  }
  
  /**
   * Displays this panel in a dialog box. 
   */
  public boolean showDialog(JComponent parent) {
    String dialogTitle = resource.getString("preferences.title");
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
   * Returns the new wall thickness in panel.
   */
  public float getNewWallThickness() {
    return ((SpinnerLengthModel)this.newWallThicknessSpinner.getModel()).getLength();
  }

  /**
   * Returns the new home wall height in panel.
   */
  public float getNewHomeWallHeight() {
    return ((SpinnerLengthModel)this.newHomeWallHeightSpinner.getModel()).getLength();
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
      // to cemtimeter when button model is selected 
      centimeterButton.addChangeListener(
        new ChangeListener () {
          public void stateChanged(ChangeEvent ev) {
            if (centimeterButton.isSelected()) {
              if (unit == UserPreferences.Unit.INCH) {
                setStepSize(centimeterStepSize);
                setValue(UserPreferences.Unit.inchToCentimer(
                    getNumber().floatValue()));
                unit = UserPreferences.Unit.CENTIMETER;
              }
            } else {
              if (unit == UserPreferences.Unit.CENTIMETER) {
                setStepSize(inchStepSize);
                setValue(UserPreferences.Unit.centimerToInch(
                    getNumber().floatValue()));
                unit = UserPreferences.Unit.INCH;
              }
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

    /**
     * Sets the length in centimeter displayed in this model.
     */
    public void setLength(float length) {
      if (unit == UserPreferences.Unit.INCH) {
        length = UserPreferences.Unit.centimerToInch(length);
      } 
      setValue(length);
    }
  }
}
