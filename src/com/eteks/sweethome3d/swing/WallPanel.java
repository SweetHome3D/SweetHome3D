/*
 * WallPanel.java 29 mai 07
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;

import com.eteks.sweethome3d.model.UserPreferences;

/**
 * Wall editing panel.
 * @author Emmanuel Puybaret
 */
public class WallPanel extends JPanel {
  private ResourceBundle   resource;
  private JLabel           xStartLabel;
  private JSpinner         xStartSpinner;
  private JLabel           yStartLabel;
  private JSpinner         yStartSpinner;
  private JLabel           xEndLabel;
  private JSpinner         xEndSpinner;
  private JLabel           yEndLabel;
  private JSpinner         yEndSpinner;
  private JLabel           thicknessLabel;
  private JSpinner         thicknessSpinner;
  private JLabel           leftSideColorLabel;
  private ColorButton      leftSideColorButton;
  private JLabel           rightSideColorLabel;
  private ColorButton      rightSideColorButton;

  /**
   * Creates a panel that will displayed wall data according to the units
   * set in <code>preferences</code>.
   * @param preferences user preferences
   */
  public WallPanel(UserPreferences preferences) {
    super(new GridBagLayout());
    this.resource = ResourceBundle.getBundle(WallPanel.class.getName());
    createComponents(preferences);
    setMnemonics();
    layoutComponents();
  }

  /**
   * Creates and initializes components and spinners model.
   */
  private void createComponents(UserPreferences preferences) {
    this.xStartLabel = new JLabel(this.resource.getString("xLabel.text"));
    this.xStartSpinner = new JSpinner(
        new NullableSpinner.NullableSpinnerLengthModel(preferences, -100000, 100000));
    this.yStartLabel = new JLabel(this.resource.getString("yLabel.text"));
    this.yStartSpinner = new JSpinner(
        new NullableSpinner.NullableSpinnerLengthModel(preferences, -100000, 100000));
    this.xEndLabel = new JLabel(this.resource.getString("xLabel.text"));
    this.xEndSpinner = new JSpinner(
        new NullableSpinner.NullableSpinnerLengthModel(preferences, -100000, 100000));
    this.yEndLabel = new JLabel(this.resource.getString("yLabel.text"));
    this.yEndSpinner = new JSpinner(
        new NullableSpinner.NullableSpinnerLengthModel(preferences, -100000, 100000));
    this.thicknessLabel = new JLabel(this.resource.getString("thicknessLabel.text"));
    this.thicknessSpinner = new NullableSpinner(
        new NullableSpinner.NullableSpinnerLengthModel(preferences, 0.1f, 1000));
    this.leftSideColorLabel = new JLabel(this.resource.getString("leftSideColorLabel.text"));
    this.leftSideColorButton = new ColorButton();
    this.leftSideColorButton.setColorDialogTitle(this.resource.getString("leftSideColorDialog.title"));
    this.rightSideColorLabel = new JLabel(this.resource.getString("rightSideColorLabel.text"));
    this.rightSideColorButton = new ColorButton();
    this.rightSideColorButton.setColorDialogTitle(this.resource.getString("rightSideColorDialog.title"));
  }
  
  /**
   * Sets components mnemonics and label / component associations.
   */
  private void setMnemonics() {
    if (!System.getProperty("os.name").startsWith("Mac OS X")) {
      this.xStartLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("xLabel.mnemonic")).getKeyCode());
      this.xStartLabel.setLabelFor(this.xStartSpinner);
      this.yStartLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("yLabel.mnemonic")).getKeyCode());
      this.yStartLabel.setLabelFor(this.yStartSpinner);
      this.xEndLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("xLabel.mnemonic")).getKeyCode());
      this.xEndLabel.setLabelFor(this.xEndSpinner);
      this.yEndLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("yLabel.mnemonic")).getKeyCode());
      this.yEndLabel.setLabelFor(this.yEndSpinner);
      this.thicknessLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("thicknessLabel.mnemonic")).getKeyCode());
      this.thicknessLabel.setLabelFor(this.thicknessSpinner);
      this.leftSideColorLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("leftSideColorLabel.mnemonic")).getKeyCode());
      this.leftSideColorLabel.setLabelFor(this.leftSideColorButton);
      this.rightSideColorLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("rightSideColorLabel.mnemonic")).getKeyCode());
      this.rightSideColorLabel.setLabelFor(this.rightSideColorButton);
    }
  }
  
  /**
   * Layouts panel composants in panel with their labels. 
   */
  private void layoutComponents() {
    // First row
    JPanel startPointPanel = createTitledPanel(
        this.resource.getString("startPointPanel.title"),
        new JComponent [] {this.xStartLabel, this.xStartSpinner, 
                           this.yStartLabel, this.yStartSpinner});
    Insets rowInsets = new Insets(0, 0, 5, 0);
    add(startPointPanel, new GridBagConstraints(
        0, 0, 2, 1, 0, 0, GridBagConstraints.WEST,
        GridBagConstraints.HORIZONTAL, rowInsets, 0, 0));
    // Second row
    JPanel endPointPanel = createTitledPanel(
        this.resource.getString("endPointPanel.title"),
        new JComponent [] {this.xEndLabel, this.xEndSpinner, 
                           this.yEndLabel, this.yEndSpinner});
    add(endPointPanel, new GridBagConstraints(
        0, 1, 2, 1, 0, 0, GridBagConstraints.WEST,
        GridBagConstraints.HORIZONTAL, rowInsets, 0, 0));
    // Third row
    JPanel colorsPanel = createTitledPanel(
        this.resource.getString("colorsPanel.title"),
        new JComponent [] {this.leftSideColorLabel, this.leftSideColorButton, 
                           this.rightSideColorLabel, this.rightSideColorButton});
    add(colorsPanel, new GridBagConstraints(
        0, 2, 2, 1, 0, 0, GridBagConstraints.WEST,
        GridBagConstraints.HORIZONTAL, rowInsets, 0, 0));
    // Last row
    add(this.thicknessLabel, new GridBagConstraints(
        0, 3, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
    add(this.thicknessSpinner, new GridBagConstraints(
        1, 3, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
  }
  
  private JPanel createTitledPanel(String title, JComponent [] components) {
    JPanel titledPanel = new JPanel(new GridBagLayout());
    titledPanel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createTitledBorder(title),
        BorderFactory.createEmptyBorder(0, 2, 2, 2)));    
    Insets insets = new Insets(0, 0, 0, 5);
    for (int i = 0; i < components.length - 1; i++) {
      titledPanel.add(components [i], new GridBagConstraints(
          i, 0, 1, 1, 1, 0, GridBagConstraints.WEST, 
          GridBagConstraints.HORIZONTAL, insets, 0, 0));
    }
    titledPanel.add(components [components.length - 1], new GridBagConstraints(
        components.length - 1, 0, 1, 1, 1, 0, GridBagConstraints.WEST, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    return titledPanel;
  }

  /**
   * Sets the start point of the wall.
   * @param x the abscissa of the start point
   * @param y the ordinate of the start point
   */
  public void setWallStartPoint(float x, float y) {
    ((NullableSpinner.NullableSpinnerLengthModel)this.xStartSpinner.getModel()).setLength(x);
    ((NullableSpinner.NullableSpinnerLengthModel)this.yStartSpinner.getModel()).setLength(y);
  }

  /**
   * Sets the end point of the wall.
   * @param x the abscissa of the end point
   * @param y the ordinate of the end point
   */
  public void setWallEndPoint(float x, float y) {
    ((NullableSpinner.NullableSpinnerLengthModel)this.xEndSpinner.getModel()).setLength(x);
    ((NullableSpinner.NullableSpinnerLengthModel)this.yEndSpinner.getModel()).setLength(y);
  }

  /**
   * Returns the abscissa of the start point of the wall.
   */
  public float getWallXStart() {
    return ((NullableSpinner.NullableSpinnerLengthModel)this.xStartSpinner.getModel()).getLength();
  }

  /**
   * Returns the ordinate of the start point of the wall.
   */
  public float getWallYStart() {
    return ((NullableSpinner.NullableSpinnerLengthModel)this.yStartSpinner.getModel()).getLength();
  }

  /**
   * Returns the abscissa of the end point of the wall.
   */
  public float getWallXEnd() {
    return ((NullableSpinner.NullableSpinnerLengthModel)this.xEndSpinner.getModel()).getLength();
  }

  /**
   * Returns the ordinate of the end point of the wall.
   */
  public float getWallYEnd() {
    return ((NullableSpinner.NullableSpinnerLengthModel)this.yEndSpinner.getModel()).getLength();
  }

  /**
   * Sets whether start and end points fields should be visible.
   */
  public void setWallPointsVisible(boolean visible) {
    // Change visibility of start and end points panel
    this.xStartLabel.getParent().setVisible(visible);
    this.xEndLabel.getParent().setVisible(visible);
  }
  
  /**
   * Sets the edited thickness of the wall(s).
   * @param thickness  the thickness of the walls or <code>null</code> 
   */
  public void setWallThickness(Float thickness) {
    ((NullableSpinner.NullableSpinnerLengthModel)this.thicknessSpinner.getModel()).setNullable(thickness == null);
    ((NullableSpinner.NullableSpinnerLengthModel)this.thicknessSpinner.getModel()).setLength(thickness);
  }

  /**
   * Returns the edited thickness of the wall(s) or <code>null</code>.
   */
  public Float getWallThickness() {
    return ((NullableSpinner.NullableSpinnerLengthModel)this.thicknessSpinner.getModel()).getLength();
  }

  /**
   * Sets the edited color of the wall(s) left part.
   * @param color  the color of the wall(s) left part or <code>null</code>
   */ 
  public void setWallLeftSideColor(Integer color) {
    this.leftSideColorButton.setColor(color);
  }

  /**
   * Returns the edited color of the wall(s) left part or <code>null</code>.
   */
  public Integer getWallLeftSideColor() {
    return this.leftSideColorButton.getColor();
  }

  /**
   * Sets the edited color of the wall(s) right part.
   * @param color  the color of the wall(s) right part or <code>null</code>
   */ 
  public void setWallRightSideColor(Integer color) {
    this.rightSideColorButton.setColor(color);
  }

  /**
   * Returns the edited color of the wall(s) right part or <code>null</code>.
   */
  public Integer getWallRightSideColor() {
    return this.rightSideColorButton.getColor();
  }
  
  /**
   * Displays this panel in a dialog box. 
   */
  public boolean showDialog(JComponent parent) {
    String dialogTitle = resource.getString("wall.title");
    return JOptionPane.showConfirmDialog(parent, this, dialogTitle, 
        JOptionPane.OK_CANCEL_OPTION, 
        JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION;
  }
}
