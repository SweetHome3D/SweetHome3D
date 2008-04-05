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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.TextureImage;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.tools.OperatingSystem;

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
  private JRadioButton   leftSideColorRadioButton;
  private ColorButton    leftSideColorButton;
  private JRadioButton   leftSideTextureRadioButton;
  private TextureButton  leftSideTextureButton;
  private JRadioButton   rightSideColorRadioButton;
  private ColorButton    rightSideColorButton;
  private JRadioButton   rightSideTextureRadioButton;
  private TextureButton  rightSideTextureButton;
  private JRadioButton   rectangularWallRadioButton;
  private JLabel         rectangularWallHeightLabel;
  private JSpinner       rectangularWallHeightSpinner;
  private JRadioButton   slopingWallRadioButton;
  private JLabel         slopingWallHeightAtStartLabel;
  private JSpinner       slopingWallHeightAtStartSpinner;
  private JLabel         slopingWallHeightAtEndLabel;
  private JSpinner       slopingWallHeightAtEndSpinner;
  private JLabel         thicknessLabel;
  private JSpinner       thicknessSpinner;
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
    
    this.leftSideColorRadioButton = new JRadioButton(this.resource.getString("leftSideColorRadioButton.text"));
    this.leftSideColorButton = new ColorButton();
    this.leftSideColorButton.setColorDialogTitle(this.resource.getString("leftSideColorDialog.title"));
    this.leftSideColorButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          leftSideColorRadioButton.setSelected(true);
        }
      });
    this.leftSideTextureRadioButton = new JRadioButton(this.resource.getString("leftSideTextureRadioButton.text"));
    this.leftSideTextureButton = new TextureButton(preferences);
    this.leftSideTextureButton.setTextureDialogTitle(this.resource.getString("leftSideTextureDialog.title"));
    this.leftSideTextureButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          leftSideTextureRadioButton.setSelected(true);
        }
      });
    ButtonGroup leftSideButtonGroup = new ButtonGroup();
    leftSideButtonGroup.add(this.leftSideColorRadioButton);
    leftSideButtonGroup.add(this.leftSideTextureRadioButton);
    
    this.rightSideColorRadioButton = new JRadioButton(this.resource.getString("rightSideColorRadioButton.text"));
    this.rightSideColorButton = new ColorButton();
    this.rightSideColorButton.setColorDialogTitle(this.resource.getString("rightSideColorDialog.title"));
    this.rightSideColorButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          rightSideColorRadioButton.setSelected(true);
        }
      });
    this.rightSideTextureRadioButton = new JRadioButton(this.resource.getString("rightSideTextureRadioButton.text"));
    this.rightSideTextureButton = new TextureButton(preferences);
    this.rightSideTextureButton.setTextureDialogTitle(this.resource.getString("rightSideTextureDialog.title"));
    this.rightSideTextureButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          rightSideTextureRadioButton.setSelected(true);
        }
      });
    ButtonGroup rightSideButtonGroup = new ButtonGroup();
    rightSideButtonGroup.add(this.rightSideColorRadioButton);
    rightSideButtonGroup.add(this.rightSideTextureRadioButton);

    this.rectangularWallRadioButton = new JRadioButton(
        this.resource.getString("rectangularWallRadioButton.text"));
    this.rectangularWallHeightLabel = new JLabel(
        String.format(this.resource.getString("rectangularWallHeightLabel.text"), unitText));
    this.rectangularWallHeightSpinner = new NullableSpinner(
        new NullableSpinner.NullableSpinnerLengthModel(preferences, 0.09999f, 2000f));
    this.rectangularWallHeightSpinner.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          rectangularWallRadioButton.setSelected(true);
        }
      });
    this.slopingWallRadioButton = new JRadioButton(
        this.resource.getString("slopingWallRadioButton.text"));
    this.slopingWallHeightAtStartLabel = new JLabel(this.resource.getString("slopingWallHeightAtStartLabel.text"));
    this.slopingWallHeightAtStartSpinner = new NullableSpinner(
        new NullableSpinner.NullableSpinnerLengthModel(preferences, 0.09999f, 2000f));
    this.slopingWallHeightAtStartSpinner.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          slopingWallRadioButton.setSelected(true);
        }
      });
    this.slopingWallHeightAtEndLabel = new JLabel(this.resource.getString("slopingWallHeightAtEndLabel.text"));
    this.slopingWallHeightAtEndSpinner = new NullableSpinner(
        new NullableSpinner.NullableSpinnerLengthModel(preferences, 0.09999f, 2000f));
    this.slopingWallHeightAtEndSpinner.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          slopingWallRadioButton.setSelected(true);
        }
      });

    ButtonGroup wallHeightButtonGroup = new ButtonGroup();
    wallHeightButtonGroup.add(this.rectangularWallRadioButton);
    wallHeightButtonGroup.add(this.slopingWallRadioButton);

    this.thicknessLabel = new JLabel(String.format(this.resource.getString("thicknessLabel.text"), unitText));
    this.thicknessSpinner = new NullableSpinner(
        new NullableSpinner.NullableSpinnerLengthModel(preferences, 0.09999f, 1000f));
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
    if (!OperatingSystem.isMacOSX()) {
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

      this.leftSideColorRadioButton.setMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("leftSideColorRadioButton.mnemonic")).getKeyCode());
      this.leftSideTextureRadioButton.setMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("leftSideTextureRadioButton.mnemonic")).getKeyCode());
      this.rightSideColorRadioButton.setMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("rightSideColorRadioButton.mnemonic")).getKeyCode());
      this.rightSideTextureRadioButton.setMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("rightSideTextureRadioButton.mnemonic")).getKeyCode());
      
      this.rectangularWallRadioButton.setMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("rectangularWallRadioButton.mnemonic")).getKeyCode());
      this.rectangularWallHeightLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("rectangularWallHeightLabel.mnemonic")).getKeyCode());
      this.rectangularWallHeightLabel.setLabelFor(this.rectangularWallHeightSpinner);
      this.slopingWallRadioButton.setMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("slopingWallRadioButton.mnemonic")).getKeyCode());
      this.slopingWallHeightAtStartLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("slopingWallHeightAtStartLabel.mnemonic")).getKeyCode());
      this.slopingWallHeightAtStartLabel.setLabelFor(this.slopingWallHeightAtStartSpinner);
      this.slopingWallHeightAtEndLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("slopingWallHeightAtEndLabel.mnemonic")).getKeyCode());
      this.slopingWallHeightAtEndLabel.setLabelFor(this.slopingWallHeightAtEndSpinner);
      
      this.thicknessLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("thicknessLabel.mnemonic")).getKeyCode());
      this.thicknessLabel.setLabelFor(this.thicknessSpinner);
    }
  }
  
  /**
   * Layouts panel composants in panel with their labels. 
   */
  private void layoutComponents(UserPreferences preferences) {
    int labelAlignment = OperatingSystem.isMacOSX() 
        ? GridBagConstraints.LINE_END
        : GridBagConstraints.LINE_START;
    // First row
    JPanel startPointPanel = createTitledPanel(
        this.resource.getString("startPointPanel.title"),
        new JComponent [] {this.xStartLabel, this.xStartSpinner, 
                           this.yStartLabel, this.yStartSpinner}, true);
    Insets rowInsets;
    if (OperatingSystem.isMacOSXLeopardOrSuperior()) {
      // User smaller insets for Mac OS X 10.5
      rowInsets = new Insets(0, 0, 0, 0);
    } else {
      rowInsets = new Insets(0, 0, 5, 0);
    }
    add(startPointPanel, new GridBagConstraints(
        0, 0, 2, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, rowInsets, 0, 0));
    // Second row
    JPanel endPointPanel = createTitledPanel(
        this.resource.getString("endPointPanel.title"),
        new JComponent [] {this.xEndLabel, this.xEndSpinner, 
                           this.yEndLabel, this.yEndSpinner}, true);
    add(endPointPanel, new GridBagConstraints(
        0, 1, 2, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, rowInsets, 0, 0));
    // Third row
    JPanel leftSidePanel = createTitledPanel(
        this.resource.getString("leftSidePanel.title"),
        new JComponent [] {this.leftSideColorRadioButton, this.leftSideColorButton, 
                           this.leftSideTextureRadioButton, this.leftSideTextureButton}, false);
    add(leftSidePanel, new GridBagConstraints(
        0, 2, 1, 1, 1, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, rowInsets, 0, 0));
    JPanel rightSidePanel = createTitledPanel(
        this.resource.getString("rightSidePanel.title"),
        new JComponent [] {this.rightSideColorRadioButton, this.rightSideColorButton, 
                           this.rightSideTextureRadioButton, this.rightSideTextureButton}, false);
    add(rightSidePanel, new GridBagConstraints(
        1, 2, 1, 1, 1, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, rowInsets, 0, 0));
    // Fourth row
    JPanel heightPanel = new JPanel(new GridBagLayout());
    heightPanel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createTitledBorder(this.resource.getString("heightPanel.title")),
        BorderFactory.createEmptyBorder(0, 2, 2, 2)));   
    // First row of height panel
    heightPanel.add(this.rectangularWallRadioButton, new GridBagConstraints(
        0, 0, 5, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 2, 0), 0, 0));
    // Second row of height panel
    // Add a dummy label to align second and fourth row on radio buttons text
    heightPanel.add(new JLabel(), new GridBagConstraints(
        0, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 0), new JRadioButton().getPreferredSize().width + 2, 0));
    heightPanel.add(this.rectangularWallHeightLabel, new GridBagConstraints(
        1, 1, 1, 1, 1, 0, labelAlignment, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
    heightPanel.add(this.rectangularWallHeightSpinner, new GridBagConstraints(
        2, 1, 1, 1, 1, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));
    // Third row of height panel
    heightPanel.add(this.slopingWallRadioButton, new GridBagConstraints(
        0, 2, 5, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 0, 2, 0), 0, 0));
    // Fourth row of height panel
    heightPanel.add(this.slopingWallHeightAtStartLabel, new GridBagConstraints(
        1, 3, 1, 1, 1, 0, labelAlignment, 
        GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
    heightPanel.add(this.slopingWallHeightAtStartSpinner, new GridBagConstraints(
        2, 3, 1, 1, 1, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0));
    heightPanel.add(this.slopingWallHeightAtEndLabel, new GridBagConstraints(
        3, 3, 1, 1, 1, 0, labelAlignment, 
        GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
    heightPanel.add(this.slopingWallHeightAtEndSpinner, new GridBagConstraints(
        4, 3, 1, 1, 1, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    
    add(heightPanel, new GridBagConstraints(
        0, 3, 2, 1, 1, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, rowInsets, 0, 0));    
    // Fifth row
    JPanel ticknessPanel = new JPanel(new GridBagLayout());
    ticknessPanel.add(this.thicknessLabel, new GridBagConstraints(
        0, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 8, 0, 5), 62, 0));
    if (OperatingSystem.isMacOSX()) {
      this.thicknessLabel.setHorizontalAlignment(JLabel.TRAILING);
    }
    ticknessPanel.add(this.thicknessSpinner, new GridBagConstraints(
        1, 0, 1, 1, 1, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    add(ticknessPanel, new GridBagConstraints(
        0, 4, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(5, 0, 10, 0), 0, 0));
    // Last row
    add(this.wallOrientationLabel, new GridBagConstraints(
        0, 5, 2, 1, 0, 0, GridBagConstraints.CENTER,
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
  }
  
  private JPanel createTitledPanel(String title, JComponent [] components, boolean horizontal) {
    JPanel titledPanel = new JPanel(new GridBagLayout());
    Border panelBorder = BorderFactory.createTitledBorder(title);
    // For systems different of Mac OS X 10.5, add an empty border 
    if (!OperatingSystem.isMacOSXLeopardOrSuperior()) {
      panelBorder = BorderFactory.createCompoundBorder(
          panelBorder, BorderFactory.createEmptyBorder(0, 2, 2, 2));
    }    
    titledPanel.setBorder(panelBorder);    
    
    if (horizontal) {
      int labelAlignment = OperatingSystem.isMacOSX() 
          ? GridBagConstraints.LINE_END
          : GridBagConstraints.LINE_START;
      Insets labelInsets = new Insets(0, 0, 0, 5);
      Insets insets = new Insets(0, 0, 0, 5);
      for (int i = 0; i < components.length - 1; i += 2) {
        titledPanel.add(components [i], new GridBagConstraints(
            i, 0, 1, 1, 1, 0, labelAlignment, 
            GridBagConstraints.NONE, labelInsets, 0, 0));
        titledPanel.add(components [i + 1], new GridBagConstraints(
            i + 1, 0, 1, 1, 1, 0, GridBagConstraints.LINE_START, 
            GridBagConstraints.HORIZONTAL, insets, 0, 0));
      }
    
      titledPanel.add(components [components.length - 1], new GridBagConstraints(
          components.length - 1, 0, 1, 1, 1, 0, GridBagConstraints.LINE_START, 
          GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    } else {
      for (int i = 0; i < components.length; i += 2) {
        int bottomInset = i < components.length - 2  ? 2  : 0;
        titledPanel.add(components [i], new GridBagConstraints(
            0, i / 2, 1, 1, 1, 0, GridBagConstraints.LINE_START, 
            GridBagConstraints.NONE, 
            new Insets(0, 0, bottomInset , 5), 0, 0));
        titledPanel.add(components [i + 1], new GridBagConstraints(
            1, i / 2, 1, 1, 1, 0, GridBagConstraints.LINE_START, 
            GridBagConstraints.HORIZONTAL, new Insets(0, 0, bottomInset, 0), 0, 0));
      }
    }
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
      HomeTexture leftSideTexture = firstWall.getLeftSideTexture();
      if (leftSideTexture != null) {
        for (int i = 1; i < selectedWalls.size(); i++) {
          if (!leftSideTexture.equals(selectedWalls.get(i).getLeftSideTexture())) {
            leftSideTexture = null;
            break;
          }
        }
      }
      this.leftSideTextureButton.setTexture(leftSideTexture);

      if (leftSideColor != null && leftSideTexture != null) {
        deselectAllRadioButtons(this.leftSideTextureRadioButton, this.leftSideColorRadioButton);
      } else if (leftSideTexture != null) {
        this.leftSideTextureRadioButton.setSelected(true);
      } else if (leftSideColor != null){
        this.leftSideColorRadioButton.setSelected(true);
      } 
      
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
      HomeTexture rightSideTexture = firstWall.getRightSideTexture();
      if (rightSideTexture != null) {
        for (int i = 1; i < selectedWalls.size(); i++) {
          if (!rightSideTexture.equals(selectedWalls.get(i).getRightSideTexture())) {
            rightSideTexture = null;
            break;
          }
        }
      }
      this.rightSideTextureButton.setTexture(rightSideTexture);
      
      if (rightSideColor != null && rightSideTexture != null) {
        deselectAllRadioButtons(this.rightSideTextureRadioButton, this.rightSideColorRadioButton);
      } else if (rightSideTexture != null) {
        this.rightSideTextureRadioButton.setSelected(true);
      } else if (rightSideColor != null) {
        this.rightSideColorRadioButton.setSelected(true);
      } 
      
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
      ((NullableSpinner.NullableSpinnerLengthModel)this.rectangularWallHeightSpinner.getModel())
          .setNullable(height == null);
      ((NullableSpinner.NullableSpinnerLengthModel)this.rectangularWallHeightSpinner.getModel())
          .setLength(height);
      ((NullableSpinner.NullableSpinnerLengthModel)this.slopingWallHeightAtStartSpinner.getModel())
          .setNullable(height == null);
      ((NullableSpinner.NullableSpinnerLengthModel)this.slopingWallHeightAtStartSpinner.getModel())
          .setLength(height);

      Float heightAtEnd = firstWall.getHeightAtEnd();
      if (heightAtEnd != null) {
        for (int i = 1; i < selectedWalls.size(); i++) {
          if (!heightAtEnd.equals(selectedWalls.get(i).getHeightAtEnd())) {
            heightAtEnd = null;
            break;
          }
        }
      }
      boolean allWallsTrapezoidal = firstWall.isTrapezoidal();
      boolean allWallsRectangular = !firstWall.isTrapezoidal();
      for (int i = 1; i < selectedWalls.size(); i++) {
        if (!selectedWalls.get(i).isTrapezoidal()) {
          allWallsTrapezoidal = false;
        } else {
          allWallsRectangular = false;
        }
      }
      ((NullableSpinner.NullableSpinnerLengthModel)this.slopingWallHeightAtEndSpinner.getModel())
          .setNullable(heightAtEnd == null);
      ((NullableSpinner.NullableSpinnerLengthModel)this.slopingWallHeightAtEndSpinner.getModel())
          .setLength(heightAtEnd == null && selectedWalls.size() == 1 ? height : heightAtEnd);

      if (allWallsTrapezoidal) {
        this.slopingWallRadioButton.setSelected(true);
      } else if (allWallsRectangular) {
        this.rectangularWallRadioButton.setSelected(true);
      } else {
        deselectAllRadioButtons(this.slopingWallRadioButton, this.rectangularWallRadioButton);
      }

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
    }
  }

  /**
   * Forces radio buttons to be deselected even if thay belong to a button group. 
   */
  private void deselectAllRadioButtons(JRadioButton ... radioButtons) {
    for (JRadioButton radioButton : radioButtons) {
      ButtonGroup group = ((JToggleButton.ToggleButtonModel)radioButton.getModel()).getGroup();
      group.remove(radioButton);
      radioButton.setSelected(false);
      group.add(radioButton);
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
   * Returns the edited color of the wall(s) left side or <code>null</code>.
   */
  public Integer getWallLeftSideColor() {
    if (this.leftSideColorRadioButton.isSelected()) {
      return this.leftSideColorButton.getColor();
    } else {
      return null;
    }
  }

  /**
   * Returns the edited texture of the wall(s) left side or <code>null</code>.
   */
  public HomeTexture getWallLeftSideTexture() {
    if (this.leftSideTextureRadioButton.isSelected()) {
      TextureImage selectedTexture = (TextureImage)this.leftSideTextureButton.getTexture();
      if (selectedTexture instanceof HomeTexture
          || selectedTexture == null) {
        return (HomeTexture)selectedTexture;
      } else {
        return new HomeTexture(selectedTexture);
      }
    } else {
      return null;
    }
  }

  /**
   * Returns the edited color of the wall(s) right side or <code>null</code>.
   */
  public Integer getWallRightSideColor() {
    if (this.rightSideColorRadioButton.isSelected()) {
      return this.rightSideColorButton.getColor();
    } else {
      return null;
    }
  }
  
  /**
   * Returns the edited texture of the wall(s) right side or <code>null</code>.
   */
  public HomeTexture getWallRightSideTexture() {
    if (this.rightSideTextureRadioButton.isSelected()) {
      TextureImage selectedTexture = (TextureImage)this.rightSideTextureButton.getTexture();
      if (selectedTexture instanceof HomeTexture
          || selectedTexture == null) {
        return (HomeTexture)selectedTexture;
      } else {
        return new HomeTexture(selectedTexture);
      }
    } else {
      return null;
    }
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
    if (this.slopingWallRadioButton.isSelected()) {
      return ((NullableSpinner.NullableSpinnerLengthModel)this.slopingWallHeightAtStartSpinner.getModel()).getLength();
    } else if (this.rectangularWallRadioButton.isSelected()) {
      return ((NullableSpinner.NullableSpinnerLengthModel)this.rectangularWallHeightSpinner.getModel()).getLength();
    } else {
      return null;
    }
  }

  /**
   * Returns the edited height at end of the wall(s) or <code>null</code>.
   */
  public Float getWallHeightAtEnd() {
    if (this.slopingWallRadioButton.isSelected()) {
      return ((NullableSpinner.NullableSpinnerLengthModel)this.slopingWallHeightAtEndSpinner.getModel()).getLength();
    } else if (this.rectangularWallRadioButton.isSelected()) {
      return ((NullableSpinner.NullableSpinnerLengthModel)this.rectangularWallHeightSpinner.getModel()).getLength();
    } else {
      return null;
    }
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
