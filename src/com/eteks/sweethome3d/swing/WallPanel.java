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

import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;

/**
 * Wall editing panel.
 * @author Emmanuel Puybaret
 */
public class WallPanel extends JPanel {
  private WallController controller;
  private ResourceBundle resource;
  private JLabel         xStartLabel;
  private JSpinner       xStartSpinner;
  private JLabel         yStartLabel;
  private JSpinner       yStartSpinner;
  private JLabel         xEndLabel;
  private JSpinner       xEndSpinner;
  private JLabel         yEndLabel;
  private JSpinner       yEndSpinner;
  private JLabel         leftSideColorLabel;
  private ColorButton    leftSideColorButton;
  private JLabel         rightSideColorLabel;
  private ColorButton    rightSideColorButton;
  private JLabel         thicknessLabel;
  private JSpinner       thicknessSpinner;
  private JLabel         heightLabel;
  private JSpinner       heightSpinner;
  private JLabel         wallOrientationLabel;

  /**
   * Creates a panel that displays wall data according to the units set in
   * <code>preferences</code>.
   * @param home home from which selected walls are edited by this panel
   * @param preferences user preferences
   * @param controller the controller of this panel
   */
  public WallPanel(Home home, UserPreferences preferences,
                   WallController controller) {
    super(new GridBagLayout());
    this.controller = controller;
    this.resource = ResourceBundle.getBundle(WallPanel.class.getName());
    createComponents(preferences);
    setMnemonics();
    layoutComponents(preferences);
    updateComponents(home);
  }

  /**
   * Creates and initializes components and spinners model.
   */
  private void createComponents(UserPreferences preferences) {
    // Get unit text matching current unit 
    String unitText = this.resource.getString(
        preferences.getUnit() == UserPreferences.Unit.CENTIMETER
            ? "centimeterUnit"
            : "inchUnit");
    
    this.xStartLabel = new JLabel(String.format(this.resource.getString("xLabel.text"), unitText));
    this.xStartSpinner = new JSpinner(
        new NullableSpinner.NullableSpinnerLengthModel(preferences, -100000f, 100000f));
    this.yStartLabel = new JLabel(String.format(this.resource.getString("yLabel.text"), unitText));
    this.yStartSpinner = new JSpinner(
        new NullableSpinner.NullableSpinnerLengthModel(preferences, -100000f, 100000f));
    this.xEndLabel = new JLabel(String.format(this.resource.getString("xLabel.text"), unitText));
    this.xEndSpinner = new JSpinner(
        new NullableSpinner.NullableSpinnerLengthModel(preferences, -100000f, 100000f));
    this.yEndLabel = new JLabel(String.format(this.resource.getString("yLabel.text"), unitText));
    this.yEndSpinner = new JSpinner(
        new NullableSpinner.NullableSpinnerLengthModel(preferences, -100000f, 100000f));
    this.leftSideColorLabel = new JLabel(this.resource.getString("leftSideColorLabel.text"));
    this.leftSideColorButton = new ColorButton();
    this.leftSideColorButton.setColorDialogTitle(this.resource.getString("leftSideColorDialog.title"));
    this.rightSideColorLabel = new JLabel(this.resource.getString("rightSideColorLabel.text"));
    this.rightSideColorButton = new ColorButton();
    this.rightSideColorButton.setColorDialogTitle(this.resource.getString("rightSideColorDialog.title"));
    this.thicknessLabel = new JLabel(String.format(this.resource.getString("thicknessLabel.text"), unitText));
    this.thicknessSpinner = new NullableSpinner(
        new NullableSpinner.NullableSpinnerLengthModel(preferences, 0.09999f, 1000f));
    this.heightLabel = new JLabel(String.format(this.resource.getString("heightLabel.text"), unitText));
    this.heightSpinner = new NullableSpinner(
        new NullableSpinner.NullableSpinnerLengthModel(preferences, 0.09999f, 2000f));
    // wallOrientationLabel shows an HTML explanation of wall orientation with an image URL in resource
    this.wallOrientationLabel = new JLabel(
        String.format(this.resource.getString("wallOrientationLabel.text"), 
            WallPanel.class.getResource("resources/wallOrientation.png")), JLabel.CENTER);
    // Use same font for label as tooltips
    this.wallOrientationLabel.setFont(UIManager.getFont("ToolTip.font"));
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
      this.leftSideColorLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("leftSideColorLabel.mnemonic")).getKeyCode());
      this.leftSideColorLabel.setLabelFor(this.leftSideColorButton);
      this.rightSideColorLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("rightSideColorLabel.mnemonic")).getKeyCode());
      this.rightSideColorLabel.setLabelFor(this.rightSideColorButton);
      this.thicknessLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("thicknessLabel.mnemonic")).getKeyCode());
      this.thicknessLabel.setLabelFor(this.thicknessSpinner);
      this.heightLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("heightLabel.mnemonic")).getKeyCode());
      this.heightLabel.setLabelFor(this.heightSpinner);
    }
  }
  
  /**
   * Layouts panel composants in panel with their labels. 
   */
  private void layoutComponents(UserPreferences preferences) {
    // First row
    JPanel startPointPanel = createTitledPanel(
        this.resource.getString("startPointPanel.title"),
        new JComponent [] {this.xStartLabel, this.xStartSpinner, 
                           this.yStartLabel, this.yStartSpinner});
    Insets rowInsets = new Insets(0, 0, 5, 0);
    add(startPointPanel, new GridBagConstraints(
        0, 0, 4, 1, 0, 0, GridBagConstraints.WEST,
        GridBagConstraints.HORIZONTAL, rowInsets, 0, 0));
    // Second row
    JPanel endPointPanel = createTitledPanel(
        this.resource.getString("endPointPanel.title"),
        new JComponent [] {this.xEndLabel, this.xEndSpinner, 
                           this.yEndLabel, this.yEndSpinner});
    add(endPointPanel, new GridBagConstraints(
        0, 1, 4, 1, 0, 0, GridBagConstraints.WEST,
        GridBagConstraints.HORIZONTAL, rowInsets, 0, 0));
    // Third row
    JPanel colorsPanel = createTitledPanel(
        this.resource.getString("colorsPanel.title"),
        new JComponent [] {this.leftSideColorLabel, this.leftSideColorButton, 
                           this.rightSideColorLabel, this.rightSideColorButton});
    add(colorsPanel, new GridBagConstraints(
        0, 2, 4, 1, 0, 0, GridBagConstraints.WEST,
        GridBagConstraints.HORIZONTAL, rowInsets, 0, 0));
    // Fourth row
    add(this.thicknessLabel, new GridBagConstraints(
        0, 3, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.NONE, 
        new Insets(0, 6, 10, colorsPanel.getBorder().getBorderInsets(colorsPanel).left), 0, 0));
    add(this.thicknessSpinner, new GridBagConstraints(
        1, 3, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.NONE, new Insets(0, 0, 10, 10), 0, 0));
    add(this.heightLabel, new GridBagConstraints(
        2, 3, 1, 1, 1, 0, GridBagConstraints.EAST, 
        GridBagConstraints.NONE, new Insets(0, 5, 10, 5), 0, 0));
    add(this.heightSpinner, new GridBagConstraints(
        3, 3, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.NONE, 
        new Insets(0, 0, 10, colorsPanel.getBorder().getBorderInsets(colorsPanel).right), 0, 0));
    // Last row
    add(this.wallOrientationLabel, new GridBagConstraints(
        0, 4, 4, 1, 0, 0, GridBagConstraints.CENTER,
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
  }
  
  private JPanel createTitledPanel(String title, JComponent [] components) {
    JPanel titledPanel = new JPanel(new GridBagLayout());
    titledPanel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createTitledBorder(title),
        BorderFactory.createEmptyBorder(0, 2, 2, 2)));    
    Insets labelInsets = new Insets(0, 0, 0, 5);
    Insets insets = new Insets(0, 0, 0, 5);
    for (int i = 0; i < components.length - 1; i++) {
      titledPanel.add(components [i], new GridBagConstraints(
          i, 0, 1, 1, 1, 0, GridBagConstraints.WEST, 
          GridBagConstraints.HORIZONTAL, 
          i % 2 == 0 ? labelInsets : insets, 0, 0));
    }
    titledPanel.add(components [components.length - 1], new GridBagConstraints(
        components.length - 1, 0, 1, 1, 1, 0, GridBagConstraints.WEST, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    return titledPanel;
  }

  /**
   * Updates components values from selected walls in <code>home</code>.
   */
  private void updateComponents(Home home) {
    List<Wall> selectedWalls = Home.getWallsSubList(home.getSelectedItems());
    if (selectedWalls.isEmpty()) {
      setVisible(false); // Nothing to edit
    } else {
      setVisible(true);
      // Search the common properties among selected walls
      Wall firstWall = selectedWalls.get(0);
      boolean multipleSelection = selectedWalls.size() > 1;
      ((NullableSpinner.NullableSpinnerLengthModel)this.xStartSpinner.getModel())
          .setNullable(multipleSelection);
      ((NullableSpinner.NullableSpinnerLengthModel)this.xStartSpinner.getModel())
          .setLength(multipleSelection ? null : firstWall.getXStart());
      ((NullableSpinner.NullableSpinnerLengthModel)this.yStartSpinner.getModel())
          .setNullable(multipleSelection);
      ((NullableSpinner.NullableSpinnerLengthModel)this.yStartSpinner.getModel())
          .setLength(multipleSelection ? null : firstWall.getYStart());
      ((NullableSpinner.NullableSpinnerLengthModel)this.xEndSpinner.getModel())
          .setNullable(multipleSelection);
      ((NullableSpinner.NullableSpinnerLengthModel)this.xEndSpinner.getModel())
          .setLength(multipleSelection ? null : firstWall.getXEnd());
      ((NullableSpinner.NullableSpinnerLengthModel)this.yEndSpinner.getModel())
          .setNullable(multipleSelection);
      ((NullableSpinner.NullableSpinnerLengthModel)this.yEndSpinner.getModel())
          .setLength(multipleSelection ? null : firstWall.getYEnd());
      
      // Make start and end points panels visible only if one wall is selected
      this.xStartLabel.getParent().setVisible(!multipleSelection);
      this.xEndLabel.getParent().setVisible(!multipleSelection);

      Integer leftSideColor = firstWall.getLeftSideColor();
      if (leftSideColor != null) {
        for (int i = 1; i < selectedWalls.size(); i++) {
          if (!leftSideColor.equals(selectedWalls.get(i).getLeftSideColor())) {
            leftSideColor = null;
            break;
          }
        }
      }
      this.leftSideColorButton.setColor(leftSideColor);
      
      Integer rightSideColor = firstWall.getRightSideColor();
      if (rightSideColor != null) {
        for (int i = 1; i < selectedWalls.size(); i++) {
          if (!rightSideColor.equals(selectedWalls.get(i).getRightSideColor())) {
            rightSideColor = null;
            break;
          }
        }
      }
      this.rightSideColorButton.setColor(rightSideColor);
      
      Float thickness = firstWall.getThickness();
      for (int i = 1; i < selectedWalls.size(); i++) {
        if (thickness != selectedWalls.get(i).getThickness()) {
          thickness = null;
          break;
        }
      }
      ((NullableSpinner.NullableSpinnerLengthModel)this.thicknessSpinner.getModel())
          .setNullable(thickness == null);
      ((NullableSpinner.NullableSpinnerLengthModel)this.thicknessSpinner.getModel())
          .setLength(thickness);

      Float height = firstWall.getHeight();
      // If wall height was never set, use home wall height
      if (height == null && firstWall.getHeight() == null) {
        height = home.getWallHeight(); 
      }
      for (int i = 1; i < selectedWalls.size(); i++) {
        Wall wall = selectedWalls.get(i);
        float wallHeight = wall.getHeight() == null 
            ? home.getWallHeight()
            : wall.getHeight();  
        if (height != wallHeight) {
          height = null;
          break;
        }
      }
      ((NullableSpinner.NullableSpinnerLengthModel)this.heightSpinner.getModel())
          .setNullable(height == null);
      ((NullableSpinner.NullableSpinnerLengthModel)this.heightSpinner.getModel())
          .setLength(height);
    }
  }

  /**
   * Returns the abscissa of the start point of the wall.
   */
  public Float getWallXStart() {
    return ((NullableSpinner.NullableSpinnerLengthModel)this.xStartSpinner.getModel()).getLength();
  }

  /**
   * Returns the ordinate of the start point of the wall.
   */
  public Float getWallYStart() {
    return ((NullableSpinner.NullableSpinnerLengthModel)this.yStartSpinner.getModel()).getLength();
  }

  /**
   * Returns the abscissa of the end point of the wall.
   */
  public Float getWallXEnd() {
    return ((NullableSpinner.NullableSpinnerLengthModel)this.xEndSpinner.getModel()).getLength();
  }

  /**
   * Returns the ordinate of the end point of the wall.
   */
  public Float getWallYEnd() {
    return ((NullableSpinner.NullableSpinnerLengthModel)this.yEndSpinner.getModel()).getLength();
  }

  /**
   * Returns the edited color of the wall(s) left part or <code>null</code>.
   */
  public Integer getWallLeftSideColor() {
    return this.leftSideColorButton.getColor();
  }

  /**
   * Returns the edited color of the wall(s) right part or <code>null</code>.
   */
  public Integer getWallRightSideColor() {
    return this.rightSideColorButton.getColor();
  }
  
  /**
   * Returns the edited thickness of the wall(s) or <code>null</code>.
   */
  public Float getWallThickness() {
    return ((NullableSpinner.NullableSpinnerLengthModel)this.thicknessSpinner.getModel()).getLength();
  }

  /**
   * Returns the edited height of the wall(s) or <code>null</code>.
   */
  public Float getWallHeight() {
    return ((NullableSpinner.NullableSpinnerLengthModel)this.heightSpinner.getModel()).getLength();
  }

  /**
   * Displays this panel in a modal dialog box. 
   */
  public void displayView() {
    String dialogTitle = resource.getString("wall.title");
    Component parent = null;
    for (Frame frame : Frame.getFrames()) {
      if (frame.isActive()) {
        parent = frame;
        break;
      }
    }
    if (JOptionPane.showConfirmDialog(parent, this, dialogTitle, 
          JOptionPane.OK_CANCEL_OPTION, 
          JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION
        && this.controller != null) {
      this.controller.modifySelection();
    }
  }
}
