/*
 * HomeFurniturePanel.java 16 mai 07
 *
 * Copyright (c) 2007 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ResourceBundle;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import com.eteks.sweethome3d.model.UserPreferences;

/**
 * Home furniture editing panel.
 * @author Emmanuel Puybaret
 */
public class HomeFurniturePanel extends JPanel {
  private ResourceBundle   resource;
  private JLabel           nameLabel;
  private JTextField       nameTextField;
  private JLabel           xLabel;
  private JSpinner         xSpinner;
  private JLabel           yLabel;
  private JSpinner         ySpinner;
  private JLabel           angleLabel;
  private JSpinner         angleSpinner;
  private JLabel           widthLabel;
  private JSpinner         widthSpinner;
  private JLabel           depthLabel;
  private JSpinner         depthSpinner;
  private JLabel           heightLabel;
  private JSpinner         heightSpinner;
  private JLabel           colorLabel;
  private ColorButton      colorButton;
  private NullableCheckBox visibleCheckBox;

  /**
   * Creates a panel that will displayed piece dimensions according to the units
   * set in <code>preferences</code>.
   * @param preferences user preferences
   */
  public HomeFurniturePanel(UserPreferences preferences) {
    super(new GridBagLayout());
    this.resource = ResourceBundle.getBundle(
        HomeFurniturePanel.class.getName());
    createComponents(preferences);
    setMnemonics();
    layoutComponents();
  }

  /**
   * Creates and initializes components and spinners model.
   */
  private void createComponents(UserPreferences preferences) {
    this.nameLabel = new JLabel(this.resource.getString("nameLabel.text"));
    this.nameTextField = new JTextField(10);
    this.xLabel = new JLabel(this.resource.getString("xLabel.text"));
    this.xSpinner = new NullableSpinner(
        new NullableSpinner.NullableSpinnerLengthModel(preferences, -100000, 100000));
    this.yLabel = new JLabel(this.resource.getString("yLabel.text"));
    this.ySpinner = new NullableSpinner(
        new NullableSpinner.NullableSpinnerLengthModel(preferences, -100000, 100000));
    this.angleLabel = new JLabel(this.resource.getString("angleLabel.text"));
    this.angleSpinner = new NullableSpinner(
        new NullableSpinner.NullableSpinnerNumberModel(0, 0, 360, 1));
    this.widthLabel = new JLabel(this.resource.getString("widthLabel.text"));
    this.widthSpinner = new NullableSpinner(
        new NullableSpinner.NullableSpinnerLengthModel(preferences, 0.1f, 100000));
    this.depthLabel = new JLabel(this.resource.getString("depthLabel.text"));
    this.depthSpinner = new NullableSpinner(
        new NullableSpinner.NullableSpinnerLengthModel(preferences, 0.1f, 100000));
    this.heightLabel = new JLabel(this.resource.getString("heightLabel.text"));
    this.heightSpinner = new NullableSpinner(
        new NullableSpinner.NullableSpinnerLengthModel(preferences, 0.1f, 100000));
    this.colorLabel = new JLabel(this.resource.getString("colorLabel.text"));
    this.colorButton = new ColorButton();
    this.colorButton.setColorDialogTitle(this.resource.getString("colorDialog.title"));
    this.visibleCheckBox = new NullableCheckBox(this.resource.getString("visibleLabel.text"));
  }
  
  /**
   * Sets components mnemonics and label / component associations.
   */
  private void setMnemonics() {
    if (!System.getProperty("os.name").startsWith("Mac OS X")) {
      this.nameLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("nameLabel.mnemonic")).getKeyCode());
      this.nameLabel.setLabelFor(this.nameTextField);
      this.xLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("xLabel.mnemonic")).getKeyCode());
      this.xLabel.setLabelFor(this.xSpinner);
      this.yLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("yLabel.mnemonic")).getKeyCode());
      this.yLabel.setLabelFor(this.ySpinner);
      this.angleLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("angleLabel.mnemonic")).getKeyCode());
      this.angleLabel.setLabelFor(this.angleSpinner);
      this.widthLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("widthLabel.mnemonic")).getKeyCode());
      this.widthLabel.setLabelFor(this.widthSpinner);
      this.depthLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("depthLabel.mnemonic")).getKeyCode());
      this.depthLabel.setLabelFor(this.depthSpinner);
      this.heightLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("heightLabel.mnemonic")).getKeyCode());
      this.heightLabel.setLabelFor(this.heightSpinner);
      this.colorLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("colorLabel.mnemonic")).getKeyCode());
      this.colorLabel.setLabelFor(this.colorButton);
      this.visibleCheckBox.setMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("visibleLabel.mnemonic")).getKeyCode());
    }
  }
  
  /**
   * Layouts panel composants in panel with their labels. 
   */
  private void layoutComponents() {
    // First row
    Insets labelInsets = new Insets(0, 0, 10, 5);
    add(this.nameLabel, new GridBagConstraints(
        0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    Insets rightComponentInsets = new Insets(0, 0, 10, 0);
    add(this.nameTextField, new GridBagConstraints(
        1, 0, 5, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.HORIZONTAL, rightComponentInsets, 0, 0));
    // Second row
    Insets spinnerInsets = new Insets(0, 0, 10, 10);
    add(this.xLabel, new GridBagConstraints(
        0, 1, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    add(this.xSpinner, new GridBagConstraints(
        1, 1, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.HORIZONTAL, spinnerInsets, -15, 0));
    add(this.yLabel, new GridBagConstraints(
        2, 1, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    add(this.ySpinner, new GridBagConstraints(
        3, 1, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.HORIZONTAL, spinnerInsets, -15, 0));
    add(this.angleLabel, new GridBagConstraints(
        4, 1, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    add(this.angleSpinner, new GridBagConstraints(
        5, 1, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.HORIZONTAL, rightComponentInsets, -15, 0));
    // Third row
    add(this.widthLabel, new GridBagConstraints(
        0, 2, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    add(this.widthSpinner, new GridBagConstraints(
        1, 2, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.HORIZONTAL, spinnerInsets, -15, 0));
    add(this.depthLabel, new GridBagConstraints(
        2, 2, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    add(this.depthSpinner, new GridBagConstraints(
        3, 2, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.HORIZONTAL, spinnerInsets, -15, 0));
    add(this.heightLabel, new GridBagConstraints(
        4, 2, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    add(this.heightSpinner, new GridBagConstraints(
        5, 2, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.HORIZONTAL, rightComponentInsets, -15, 0));
    // Last row
    Insets lastRowInsets = new Insets(0, 0, 0, 5);
    add(this.colorLabel, new GridBagConstraints(
        0, 3, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.NONE, lastRowInsets, 0, 0));
    add(this.colorButton, new GridBagConstraints(
        1, 3, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.NONE, lastRowInsets, 0, 0));
    add(this.visibleCheckBox, new GridBagConstraints(
        3, 3, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.NONE, lastRowInsets, 0, 0));
  }

  /**
   * Sets the edited name of the furniture.
   * @param name the name of the furniture or <code>null</code>
   */
  public void setFurnitureName(String name) {
    this.nameTextField.setText(name);
  }

  /**
   * Returns the edited name of the furniture or <code>null</code>.
   */
  public String getFurnitureName() {
    String name = this.nameTextField.getText();
    if (name == null || name.trim().length() == 0) {
      return null;
    } else {
      return name;
    }
  }
  
  /**
   * Sets the edited dimension of the furniture.
   * @param width  the width of the furniture or <code>null</code> 
   * @param depth  the depth of the furniture or <code>null</code>
   * @param height the height of the furniture or <code>null</code>
   */
  public void setFurnitureDimension(Float width, Float depth, Float height) {
    ((NullableSpinner.NullableSpinnerLengthModel)this.widthSpinner.getModel()).setNullable(width == null);
    ((NullableSpinner.NullableSpinnerLengthModel)this.widthSpinner.getModel()).setLength(width);
    ((NullableSpinner.NullableSpinnerLengthModel)this.depthSpinner.getModel()).setNullable(depth == null);
    ((NullableSpinner.NullableSpinnerLengthModel)this.depthSpinner.getModel()).setLength(depth);
    ((NullableSpinner.NullableSpinnerLengthModel)this.heightSpinner.getModel()).setNullable(height == null);
    ((NullableSpinner.NullableSpinnerLengthModel)this.heightSpinner.getModel()).setLength(height);
  }

  /**
   * Returns the edited width of the furniture or <code>null</code>.
   */
  public Float getFurnitureWidth() {
    return ((NullableSpinner.NullableSpinnerLengthModel)this.widthSpinner.getModel()).getLength();
  }

  /**
   * Returns the edited depth of the furniture or <code>null</code>.
   */
  public Float getFurnitureDepth() {
    return ((NullableSpinner.NullableSpinnerLengthModel)this.depthSpinner.getModel()).getLength();
  }

  /**
   * Returns the edited height of the furniture or <code>null</code>.
   */
  public Float getFurnitureHeight() {
    return ((NullableSpinner.NullableSpinnerLengthModel)this.heightSpinner.getModel()).getLength();
  }

  /**
   * Sets the edited color of the furniture.
   * @param color  the color of the furniture or <code>null</code>
   */ 
  public void setFurnitureColor(Integer color) {
    this.colorButton.setColor(color);
  }

  /**
   * Returns the edited color of the furniture or <code>null</code>.
   */
  public Integer getFurnitureColor() {
    return this.colorButton.getColor();
  }

  /**
   * Sets whether the furniture is visible or not.
   * @param visible <code>Boolean.TRUE</code>, <code>Boolean.FALSE</code> or <code>null</code>
   */
  public void setFurnitureVisible(Boolean visible) {
    this.visibleCheckBox.setNullable(visible == null);
    this.visibleCheckBox.setValue(visible);   
  }

  /**
   * Returns whether the furniture is visible or not.
   */
  public Boolean isFurnitureVisible() {
    return this.visibleCheckBox.getValue();
  }

  /**
   * Sets the edited location of the furniture.
   * @param x the abscissa of the furniture or <code>null</code>
   * @param y the ordinate of the furniture or <code>null</code>
   */
  public void setFurnitureLocation(Float x, Float y) {
    ((NullableSpinner.NullableSpinnerLengthModel)this.xSpinner.getModel()).setNullable(x == null);
    ((NullableSpinner.NullableSpinnerLengthModel)this.xSpinner.getModel()).setLength(x);
    ((NullableSpinner.NullableSpinnerLengthModel)this.ySpinner.getModel()).setNullable(y == null);
    ((NullableSpinner.NullableSpinnerLengthModel)this.ySpinner.getModel()).setLength(y);
  }

  /**
   * Returns the edited abscissa of the furniture or <code>null</code>.
   */
  public Float getFurnitureX() {
    return ((NullableSpinner.NullableSpinnerLengthModel)this.xSpinner.getModel()).getLength();
  }

  /**
   * Returns the edited ordinate of the furniture or <code>null</code>.
   */
  public Float getFurnitureY() {
    return ((NullableSpinner.NullableSpinnerLengthModel)this.ySpinner.getModel()).getLength();
  }

  /**
   * Sets the edited angle of the furniture.
   * @param angle the angle of the furniture or <code>null</code>
   */
  public void setFurnitureAngle(Float angle) {
    ((NullableSpinner.NullableSpinnerNumberModel)this.angleSpinner.getModel()).setNullable(angle == null);
    if (angle == null) {
      this.angleSpinner.setValue(null);
    } else {
      this.angleSpinner.setValue((Math.round(Math.toDegrees(angle)) + 360) % 360);
    }
  }
  
  /**
   * Returns the edited angle of the furniture or <code>null</code>.
   */
  public Float getFurnitureAngle() {
    Number angle = (Number)this.angleSpinner.getValue();
    if (angle == null) {
      return null;
    } else {
      return (float)Math.toRadians(angle.doubleValue());
    }
  }

  /**
   * Displays this panel in a dialog box. 
   */
  public boolean showDialog(JComponent parent) {
    String dialogTitle = resource.getString("homeFurniture.title");
    return JOptionPane.showConfirmDialog(parent, this, dialogTitle, 
        JOptionPane.OK_CANCEL_OPTION, 
        JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION;
  }

  /**
   * A check box that accepts <code>null</code> values. Thus this check box is able to
   * display 3 states : <code>null</code>, <code>false</code> and <code>true</code>.
   */
  private static class NullableCheckBox extends JComponent {
    private JCheckBox    checkBox;
    private Boolean      value = Boolean.FALSE;
    private boolean      nullable;
    private ItemListener checkBoxListener;
    
    /**
     * Creates a nullable check box.
     */
    public NullableCheckBox(String text) {
      // Measure check box size alone without its text
      final Dimension checkBoxSize = new JCheckBox().getPreferredSize();
      // Create a check box that displays a dash upon default check box for a null value
      this.checkBox = new JCheckBox(text) {
        @Override
        protected void paintComponent(Graphics g) {
          super.paintComponent(g);
          if (value == null) {
            g.drawRect(checkBoxSize.width / 2 - 3, checkBoxSize.height / 2, 6, 1);
          }
        }
      };
      // Add an item listener to change default checking logic 
      this.checkBoxListener = new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          // If this check box is nullable
          if (nullable) {
            // Checking sequence will be null, true, false
            if (getValue() == Boolean.FALSE) {
              setValue(null);
            } else if (getValue() == null) {
              setValue(Boolean.TRUE);
            } else {
              setValue(Boolean.FALSE);
            }
          } else {
            setValue(checkBox.isSelected());
          }
        }
      };
      this.checkBox.addItemListener(this.checkBoxListener);
      
      // Add the check box and its label to this component
      setLayout(new BorderLayout(2, 0));
      add(this.checkBox, BorderLayout.WEST);
    }
    
    /**
     * Returns <code>null</code>, <code>Boolean.TRUE</code> or <code>Boolean.FALSE</code>.
     */
    public Boolean getValue() {
      return this.value;
    }

    /**
     * Sets displayed value in check box. 
     * @param value <code>null</code>, <code>Boolean.TRUE</code> or <code>Boolean.FALSE</code>
     */
    public void setValue(Boolean value) {
      this.value = value;
      this.checkBox.removeItemListener(this.checkBoxListener);
      try {
        if (value != null) {
          this.checkBox.setSelected(value);
        } else if (isNullable()) {
          // Unselect check box to display a dash in its middle
          this.checkBox.setSelected(false);
        } else {
          throw new IllegalArgumentException("Check box isn't nullable");
        }
      } finally {
        this.checkBox.addItemListener(this.checkBoxListener);
      }      
    }
    
    /**
     * Returns <code>true</code> if this check box is nullable.
     */
    public boolean isNullable() {
      return this.nullable;
    }

    /**
     * Sets whether this check box is nullable.
     */
    public void setNullable(boolean nullable) {
      this.nullable = nullable;
      if (!nullable && getValue() == null) {
        setValue(Boolean.FALSE);
      }
    }
    
    /**
     * Sets the mnemonic of this component.
     * @param mnemonic a <code>VK_...</code> code defined in <code>java.awt.event.KeyEvent</code>. 
     */
    public void setMnemonic(int mnemonic) {
      this.checkBox.setMnemonic(mnemonic);
    }
    
    /**
     * Returns the mnemonic of this component.
     */
    public int getMnemonic() {
      return this.checkBox.getMnemonic();
    }

    @Override
    public void setEnabled(boolean enabled) {
      this.checkBox.setEnabled(enabled);
    }

    @Override
    public boolean isEnabled() {
      return this.checkBox.isEnabled();
    }
  }
}
