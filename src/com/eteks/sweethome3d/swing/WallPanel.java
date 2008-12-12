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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.tools.ResourceURLContent;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.View;
import com.eteks.sweethome3d.viewcontroller.WallController;

/**
 * Wall editing panel.
 * @author Emmanuel Puybaret
 */
public class WallPanel extends JPanel implements DialogView {
  private final WallController controller;
  private JLabel         xStartLabel;
  private JSpinner       xStartSpinner;
  private JLabel         yStartLabel;
  private JSpinner       yStartSpinner;
  private JLabel         xEndLabel;
  private JSpinner       xEndSpinner;
  private JLabel         yEndLabel;
  private JSpinner       yEndSpinner;
  private JLabel         lengthLabel;
  private JSpinner       lengthSpinner;
  private JRadioButton   leftSideColorRadioButton;
  private ColorButton    leftSideColorButton;
  private JRadioButton   leftSideTextureRadioButton;
  private JComponent     leftSideTextureComponent;
  private JRadioButton   rightSideColorRadioButton;
  private ColorButton    rightSideColorButton;
  private JRadioButton   rightSideTextureRadioButton;
  private JComponent     rightSideTextureComponent;
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
  private String         dialogTitle;

  /**
   * Creates a panel that displays wall data according to the units set in
   * <code>preferences</code>.
   * @param preferences user preferences
   * @param controller the controller of this panel
   */
  public WallPanel(UserPreferences preferences,
                   WallController controller) {
    super(new GridBagLayout());
    this.controller = controller;
    createComponents(preferences, controller);
    setMnemonics(preferences);
    layoutComponents(preferences, controller);
  }

  /**
   * Creates and initializes components and spinners model.
   */
  private void createComponents(UserPreferences preferences, 
                                final WallController controller) {
    // Get unit name matching current unit 
    String unitName = preferences.getLengthUnit().getName();
    
    // Create X start label and its spinner bound to X_START controller property
    this.xStartLabel = new JLabel(preferences.getLocalizedString(
        WallPanel.class, "xLabel.text", unitName));
    final NullableSpinner.NullableSpinnerLengthModel xStartSpinnerModel = 
        new NullableSpinner.NullableSpinnerLengthModel(preferences, -100000f, 100000f);
    this.xStartSpinner = new NullableSpinner(xStartSpinnerModel);
    xStartSpinnerModel.setNullable(controller.getXStart() == null);
    xStartSpinnerModel.setLength(controller.getXStart());
    final PropertyChangeListener xStartChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          xStartSpinnerModel.setNullable(ev.getNewValue() == null);
          xStartSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(WallController.Property.X_START, xStartChangeListener);
    xStartSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(WallController.Property.X_START, xStartChangeListener);
          controller.setXStart(xStartSpinnerModel.getLength());
          controller.addPropertyChangeListener(WallController.Property.X_START, xStartChangeListener);
        }
      });
    
    // Create Y start label and its spinner bound to Y_START controller property
    this.yStartLabel = new JLabel(preferences.getLocalizedString(
        WallPanel.class, "yLabel.text", unitName));
    final NullableSpinner.NullableSpinnerLengthModel yStartSpinnerModel = 
        new NullableSpinner.NullableSpinnerLengthModel(preferences, -100000f, 100000f);
    this.yStartSpinner = new NullableSpinner(yStartSpinnerModel);
    yStartSpinnerModel.setNullable(controller.getYStart() == null);
    yStartSpinnerModel.setLength(controller.getYStart());
    final PropertyChangeListener yStartChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          yStartSpinnerModel.setNullable(ev.getNewValue() == null);
          yStartSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(WallController.Property.Y_START, yStartChangeListener);
    yStartSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(WallController.Property.Y_START, yStartChangeListener);
          controller.setYStart(yStartSpinnerModel.getLength());
          controller.addPropertyChangeListener(WallController.Property.Y_START, yStartChangeListener);
        }
      });
    
    // Create X end label and its spinner bound to X_END controller property
    this.xEndLabel = new JLabel(preferences.getLocalizedString(
        WallPanel.class, "xLabel.text", unitName));
    final NullableSpinner.NullableSpinnerLengthModel xEndSpinnerModel = 
        new NullableSpinner.NullableSpinnerLengthModel(preferences, -100000f, 100000f);
    this.xEndSpinner = new NullableSpinner(xEndSpinnerModel);
    xEndSpinnerModel.setNullable(controller.getXEnd() == null);
    xEndSpinnerModel.setLength(controller.getXEnd());
    final PropertyChangeListener xEndChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          xEndSpinnerModel.setNullable(ev.getNewValue() == null);
          xEndSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(WallController.Property.X_END, xEndChangeListener);
    xEndSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(WallController.Property.X_END, xEndChangeListener);
          controller.setXEnd(xEndSpinnerModel.getLength());
          controller.addPropertyChangeListener(WallController.Property.X_END, xEndChangeListener);
        }
      });
    
    // Create Y end label and its spinner bound to Y_END controller property
    this.yEndLabel = new JLabel(preferences.getLocalizedString(
        WallPanel.class, "yLabel.text", unitName));
    final NullableSpinner.NullableSpinnerLengthModel yEndSpinnerModel = 
        new NullableSpinner.NullableSpinnerLengthModel(preferences, -100000f, 100000f);
    this.yEndSpinner = new NullableSpinner(yEndSpinnerModel);
    yEndSpinnerModel.setNullable(controller.getYEnd() == null);
    yEndSpinnerModel.setLength(controller.getYEnd());
    final PropertyChangeListener yEndChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          yEndSpinnerModel.setNullable(ev.getNewValue() == null);
          yEndSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(WallController.Property.Y_END, yEndChangeListener);
    yEndSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(WallController.Property.Y_END, yEndChangeListener);
          controller.setYEnd(yEndSpinnerModel.getLength());
          controller.addPropertyChangeListener(WallController.Property.Y_END, yEndChangeListener);
        }
      });

    // Create length label and its spinner bound to LENGTH controller property
    this.lengthLabel = new JLabel(preferences.getLocalizedString(
        WallPanel.class, "lengthLabel.text", unitName));
    final NullableSpinner.NullableSpinnerLengthModel lengthSpinnerModel = 
        new NullableSpinner.NullableSpinnerLengthModel(preferences, 0.09999f, 100000f);
    this.lengthSpinner = new NullableSpinner(lengthSpinnerModel);
    lengthSpinnerModel.setNullable(controller.getLength() == null);
    lengthSpinnerModel.setLength(controller.getLength());
    final PropertyChangeListener lengthChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          lengthSpinnerModel.setNullable(ev.getNewValue() == null);
          lengthSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(WallController.Property.LENGTH, 
        lengthChangeListener);
    lengthSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(WallController.Property.LENGTH, 
              lengthChangeListener);
          controller.setLength(lengthSpinnerModel.getLength());
          controller.addPropertyChangeListener(WallController.Property.LENGTH, 
              lengthChangeListener);
        }
      });

    // Left side color and texture buttons bound to left side controller properties
    this.leftSideColorRadioButton = new JRadioButton(preferences.getLocalizedString(
        WallPanel.class, "leftSideColorRadioButton.text"));
    this.leftSideColorRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (leftSideColorRadioButton.isSelected()) {
            controller.setLeftSidePaint(WallController.WallPaint.COLORED);
          }
        }
      });
    controller.addPropertyChangeListener(WallController.Property.LEFT_SIDE_PAINT, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            updateLeftSideRadioButtons(controller);
          }
        });
    
    this.leftSideColorButton = new ColorButton();
    this.leftSideColorButton.setColorDialogTitle(preferences.getLocalizedString(
        WallPanel.class, "leftSideColorDialog.title"));
    this.leftSideColorButton.setColor(controller.getLeftSideColor());
    this.leftSideColorButton.addPropertyChangeListener(ColorButton.COLOR_PROPERTY, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            controller.setLeftSideColor(leftSideColorButton.getColor());
          }
        });
    controller.addPropertyChangeListener(WallController.Property.LEFT_SIDE_COLOR, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            leftSideColorButton.setColor(controller.getLeftSideColor());
          }
        });

    this.leftSideTextureRadioButton = new JRadioButton(preferences.getLocalizedString(
        WallPanel.class, "leftSideTextureRadioButton.text"));
    this.leftSideTextureRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (leftSideTextureRadioButton.isSelected()) {
            controller.setLeftSidePaint(WallController.WallPaint.TEXTURED);
          }
        }
      });
    
    this.leftSideTextureComponent = (JComponent)controller.getLeftSideTextureController().getView();
    
    ButtonGroup leftSideButtonGroup = new ButtonGroup();
    leftSideButtonGroup.add(this.leftSideColorRadioButton);
    leftSideButtonGroup.add(this.leftSideTextureRadioButton);
    updateLeftSideRadioButtons(controller);
    
    // Right side color and texture buttons bound to right side controller properties
    this.rightSideColorRadioButton = new JRadioButton(preferences.getLocalizedString(
        WallPanel.class, "rightSideColorRadioButton.text"));
    this.rightSideColorRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          if (rightSideColorRadioButton.isSelected()) {
            controller.setRightSidePaint(WallController.WallPaint.COLORED);
          }
        }
      });
    controller.addPropertyChangeListener(WallController.Property.RIGHT_SIDE_PAINT, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            updateRightSideRadioButtons(controller);
          }
        });

    this.rightSideColorButton = new ColorButton();
    this.rightSideColorButton.setColor(controller.getRightSideColor());
    this.rightSideColorButton.setColorDialogTitle(preferences.getLocalizedString(
        WallPanel.class, "rightSideColorDialog.title"));
    this.rightSideColorButton.addPropertyChangeListener(ColorButton.COLOR_PROPERTY, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            controller.setRightSideColor(rightSideColorButton.getColor());
          }
        });
    controller.addPropertyChangeListener(WallController.Property.RIGHT_SIDE_COLOR, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            rightSideColorButton.setColor(controller.getRightSideColor());
          }
        });
    
    this.rightSideTextureRadioButton = new JRadioButton(preferences.getLocalizedString(
        WallPanel.class, "rightSideTextureRadioButton.text"));
    this.rightSideTextureRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          if (rightSideTextureRadioButton.isSelected()) {
            controller.setRightSidePaint(WallController.WallPaint.TEXTURED);
          }
        }
      });
  
    this.rightSideTextureComponent = (JComponent)controller.getRightSideTextureController().getView();

    ButtonGroup rightSideButtonGroup = new ButtonGroup();
    rightSideButtonGroup.add(this.rightSideColorRadioButton);
    rightSideButtonGroup.add(this.rightSideTextureRadioButton);
    updateRightSideRadioButtons(controller);

    this.rectangularWallRadioButton = new JRadioButton(preferences.getLocalizedString(
        WallPanel.class, "rectangularWallRadioButton.text"));
    this.rectangularWallRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (rectangularWallRadioButton.isSelected()) {
            controller.setShape(WallController.WallShape.RECTANGULAR_WALL);
          }
        }
      });
    controller.addPropertyChangeListener(WallController.Property.SHAPE, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            updateWallShapeRadioButtons(controller);
          }
        });

    // Create height label and its spinner bound to RECTANGULAR_WALL_HEIGHT controller property
    this.rectangularWallHeightLabel = new JLabel(preferences.getLocalizedString(
            WallPanel.class, "rectangularWallHeightLabel.text", unitName));
    final NullableSpinner.NullableSpinnerLengthModel rectangularWallHeightSpinnerModel = 
        new NullableSpinner.NullableSpinnerLengthModel(preferences, 0.09999f, 2000f);
    this.rectangularWallHeightSpinner = new NullableSpinner(rectangularWallHeightSpinnerModel);
    rectangularWallHeightSpinnerModel.setNullable(controller.getRectangularWallHeight() == null);
    rectangularWallHeightSpinnerModel.setLength(controller.getRectangularWallHeight());
    final PropertyChangeListener rectangularWallHeightChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          rectangularWallHeightSpinnerModel.setNullable(ev.getNewValue() == null);
          rectangularWallHeightSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(WallController.Property.RECTANGULAR_WALL_HEIGHT, 
        rectangularWallHeightChangeListener);
    rectangularWallHeightSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(WallController.Property.RECTANGULAR_WALL_HEIGHT, 
              rectangularWallHeightChangeListener);
          controller.setRectangularWallHeight(rectangularWallHeightSpinnerModel.getLength());
          controller.addPropertyChangeListener(WallController.Property.RECTANGULAR_WALL_HEIGHT, 
              rectangularWallHeightChangeListener);
        }
      });
   
    this.slopingWallRadioButton = new JRadioButton(preferences.getLocalizedString(
        WallPanel.class, "slopingWallRadioButton.text"));
    this.slopingWallRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (slopingWallRadioButton.isSelected()) {
            controller.setShape(WallController.WallShape.SLOPING_WALL);
          }
        }
      });
    ButtonGroup wallHeightButtonGroup = new ButtonGroup();
    wallHeightButtonGroup.add(this.rectangularWallRadioButton);
    wallHeightButtonGroup.add(this.slopingWallRadioButton);
    updateWallShapeRadioButtons(controller);

    // Create height at start label and its spinner bound to SLOPING_WALL_HEIGHT_AT_START controller property
    this.slopingWallHeightAtStartLabel = new JLabel(preferences.getLocalizedString(
        WallPanel.class, "slopingWallHeightAtStartLabel.text"));
    final NullableSpinner.NullableSpinnerLengthModel slopingWallHeightAtStartSpinnerModel = 
        new NullableSpinner.NullableSpinnerLengthModel(preferences, 0.09999f, 2000f);
    this.slopingWallHeightAtStartSpinner = new NullableSpinner(slopingWallHeightAtStartSpinnerModel);
    slopingWallHeightAtStartSpinnerModel.setNullable(controller.getSlopingWallHeightAtStart() == null);
    slopingWallHeightAtStartSpinnerModel.setLength(controller.getSlopingWallHeightAtStart());
    final PropertyChangeListener slopingWallHeightAtStartChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          slopingWallHeightAtStartSpinnerModel.setNullable(ev.getNewValue() == null);
          slopingWallHeightAtStartSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(WallController.Property.SLOPING_WALL_HEIGHT_AT_START, 
        slopingWallHeightAtStartChangeListener);
    slopingWallHeightAtStartSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(WallController.Property.SLOPING_WALL_HEIGHT_AT_START, 
              slopingWallHeightAtStartChangeListener);
          controller.setSlopingWallHeightAtStart(slopingWallHeightAtStartSpinnerModel.getLength());
          controller.addPropertyChangeListener(WallController.Property.SLOPING_WALL_HEIGHT_AT_START, 
              slopingWallHeightAtStartChangeListener);
        }
      });
    
    // Create height at end label and its spinner bound to SLOPING_WALL_HEIGHT_AT_END controller property
    this.slopingWallHeightAtEndLabel = new JLabel(preferences.getLocalizedString(
        WallPanel.class, "slopingWallHeightAtEndLabel.text"));
    final NullableSpinner.NullableSpinnerLengthModel slopingWallHeightAtEndSpinnerModel = 
        new NullableSpinner.NullableSpinnerLengthModel(preferences, 0.09999f, 2000f);
    this.slopingWallHeightAtEndSpinner = new NullableSpinner(slopingWallHeightAtEndSpinnerModel);
    slopingWallHeightAtEndSpinnerModel.setNullable(controller.getSlopingWallHeightAtEnd() == null);
    slopingWallHeightAtEndSpinnerModel.setLength(controller.getSlopingWallHeightAtEnd());
    final PropertyChangeListener slopingWallHeightAtEndChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          slopingWallHeightAtEndSpinnerModel.setNullable(ev.getNewValue() == null);
          slopingWallHeightAtEndSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(WallController.Property.SLOPING_WALL_HEIGHT_AT_END, 
        slopingWallHeightAtEndChangeListener);
    slopingWallHeightAtEndSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(WallController.Property.SLOPING_WALL_HEIGHT_AT_END, 
              slopingWallHeightAtEndChangeListener);
          controller.setSlopingWallHeightAtEnd(slopingWallHeightAtEndSpinnerModel.getLength());
          controller.addPropertyChangeListener(WallController.Property.SLOPING_WALL_HEIGHT_AT_END, 
              slopingWallHeightAtEndChangeListener);
        }
      });

    // Create thickness label and its spinner bound to THICKNESS controller property
    this.thicknessLabel = new JLabel(preferences.getLocalizedString(
        WallPanel.class, "thicknessLabel.text", unitName));
    final NullableSpinner.NullableSpinnerLengthModel thicknessSpinnerModel = 
        new NullableSpinner.NullableSpinnerLengthModel(preferences, 0.09999f, 1000f);
    this.thicknessSpinner = new NullableSpinner(thicknessSpinnerModel);
    thicknessSpinnerModel.setNullable(controller.getThickness() == null);
    thicknessSpinnerModel.setLength(controller.getThickness());
    final PropertyChangeListener thicknessChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          thicknessSpinnerModel.setNullable(ev.getNewValue() == null);
          thicknessSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(WallController.Property.THICKNESS, 
        thicknessChangeListener);
    thicknessSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(WallController.Property.THICKNESS, 
              thicknessChangeListener);
          controller.setThickness(thicknessSpinnerModel.getLength());
          controller.addPropertyChangeListener(WallController.Property.THICKNESS, 
              thicknessChangeListener);
        }
      });
    
    // wallOrientationLabel shows an HTML explanation of wall orientation with an image URL in resource
    this.wallOrientationLabel = new JLabel(preferences.getLocalizedString(
            WallPanel.class, "wallOrientationLabel.text", 
            new ResourceURLContent(WallPanel.class, "resources/wallOrientation.png").getURL()), 
        JLabel.CENTER);
    // Use same font for label as tooltips
    this.wallOrientationLabel.setFont(UIManager.getFont("ToolTip.font"));
    
    this.dialogTitle = preferences.getLocalizedString(WallPanel.class, "wall.title");
  }

  /**
   * Updates left side radio buttons. 
   */
  private void updateLeftSideRadioButtons(WallController controller) {
    if (controller.getLeftSidePaint() == WallController.WallPaint.COLORED) {
      this.leftSideColorRadioButton.setSelected(true);
    } else if (controller.getLeftSidePaint() == WallController.WallPaint.TEXTURED) {
      this.leftSideTextureRadioButton.setSelected(true);
    } else { // null
      SwingTools.deselectAllRadioButtons(this.leftSideColorRadioButton, this.leftSideTextureRadioButton);
    }
  }

  /**
   * Updates right side radio buttons. 
   */
  private void updateRightSideRadioButtons(WallController controller) {
    if (controller.getRightSidePaint() == WallController.WallPaint.COLORED) {
      this.rightSideColorRadioButton.setSelected(true);
    } else if (controller.getRightSidePaint() == WallController.WallPaint.TEXTURED) {
      this.rightSideTextureRadioButton.setSelected(true);
    } else { // null
      SwingTools.deselectAllRadioButtons(this.rightSideColorRadioButton, this.rightSideTextureRadioButton);
    }
  }

  /**
   * Updates rectangular and sloping wall radio buttons. 
   */
  private void updateWallShapeRadioButtons(WallController controller) {
    if (controller.getShape() == WallController.WallShape.SLOPING_WALL) {
      this.slopingWallRadioButton.setSelected(true);
    } else if (controller.getShape() == WallController.WallShape.RECTANGULAR_WALL) {
      this.rectangularWallRadioButton.setSelected(true);
    } else { // null
      SwingTools.deselectAllRadioButtons(this.slopingWallRadioButton, this.rectangularWallRadioButton);
    }
  }
  
  /**
   * Sets components mnemonics and label / component associations.
   */
  private void setMnemonics(UserPreferences preferences) {
    if (!OperatingSystem.isMacOSX()) {
      this.xStartLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(WallPanel.class, "xLabel.mnemonic")).getKeyCode());
      this.xStartLabel.setLabelFor(this.xStartSpinner);
      this.yStartLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(WallPanel.class, "yLabel.mnemonic")).getKeyCode());
      this.yStartLabel.setLabelFor(this.yStartSpinner);
      this.xEndLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(WallPanel.class, "xLabel.mnemonic")).getKeyCode());
      this.xEndLabel.setLabelFor(this.xEndSpinner);
      this.yEndLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(WallPanel.class, "yLabel.mnemonic")).getKeyCode());
      this.yEndLabel.setLabelFor(this.yEndSpinner);
      this.lengthLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(WallPanel.class, "lengthLabel.mnemonic")).getKeyCode());
      this.lengthLabel.setLabelFor(this.lengthSpinner);

      this.leftSideColorRadioButton.setMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(WallPanel.class, "leftSideColorRadioButton.mnemonic")).getKeyCode());
      this.leftSideTextureRadioButton.setMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(WallPanel.class, "leftSideTextureRadioButton.mnemonic")).getKeyCode());
      this.rightSideColorRadioButton.setMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(WallPanel.class, "rightSideColorRadioButton.mnemonic")).getKeyCode());
      this.rightSideTextureRadioButton.setMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(WallPanel.class, "rightSideTextureRadioButton.mnemonic")).getKeyCode());
      
      this.rectangularWallRadioButton.setMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(WallPanel.class, "rectangularWallRadioButton.mnemonic")).getKeyCode());
      this.rectangularWallHeightLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(WallPanel.class, "rectangularWallHeightLabel.mnemonic")).getKeyCode());
      this.rectangularWallHeightLabel.setLabelFor(this.rectangularWallHeightSpinner);
      this.slopingWallRadioButton.setMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(WallPanel.class, "slopingWallRadioButton.mnemonic")).getKeyCode());
      this.slopingWallHeightAtStartLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(WallPanel.class, "slopingWallHeightAtStartLabel.mnemonic")).getKeyCode());
      this.slopingWallHeightAtStartLabel.setLabelFor(this.slopingWallHeightAtStartSpinner);
      this.slopingWallHeightAtEndLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(WallPanel.class, "slopingWallHeightAtEndLabel.mnemonic")).getKeyCode());
      this.slopingWallHeightAtEndLabel.setLabelFor(this.slopingWallHeightAtEndSpinner);
      
      this.thicknessLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(WallPanel.class, "thicknessLabel.mnemonic")).getKeyCode());
      this.thicknessLabel.setLabelFor(this.thicknessSpinner);
    }
  }
  
  /**
   * Layouts panel components in panel with their labels. 
   */
  private void layoutComponents(UserPreferences preferences, 
                                final WallController controller) {
    int labelAlignment = OperatingSystem.isMacOSX() 
        ? GridBagConstraints.LINE_END
        : GridBagConstraints.LINE_START;
    // First row
    final JPanel startPointPanel = createTitledPanel(
        preferences.getLocalizedString(WallPanel.class, "startPointPanel.title"),
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
    final JPanel endPointPanel = createTitledPanel(
        preferences.getLocalizedString(WallPanel.class, "endPointPanel.title"),
        new JComponent [] {this.xEndLabel, this.xEndSpinner, 
                           this.yEndLabel, this.yEndSpinner}, true);
    // Add length label and spinner at the end of second row of endPointPanel
    endPointPanel.add(this.lengthLabel, new GridBagConstraints(
        0, 1, 3, 1, 1, 0, GridBagConstraints.LINE_END, 
        GridBagConstraints.NONE, new Insets(5, 0, 0, 5), 0, 0));
    endPointPanel.add(this.lengthSpinner, new GridBagConstraints(
        3, 1, 1, 1, 1, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, new Insets(5, 0, 0, 0), 0, 0));

    add(endPointPanel, new GridBagConstraints(
        0, 1, 2, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, rowInsets, 0, 0));    
    // Third row
    JPanel leftSidePanel = createTitledPanel(
        preferences.getLocalizedString(WallPanel.class, "leftSidePanel.title"),
        new JComponent [] {this.leftSideColorRadioButton, this.leftSideColorButton, 
                           this.leftSideTextureRadioButton, this.leftSideTextureComponent}, false);
    add(leftSidePanel, new GridBagConstraints(
        0, 2, 1, 1, 1, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, rowInsets, 0, 0));
    JPanel rightSidePanel = createTitledPanel(
        preferences.getLocalizedString(WallPanel.class, "rightSidePanel.title"),
        new JComponent [] {this.rightSideColorRadioButton, this.rightSideColorButton, 
                           this.rightSideTextureRadioButton, this.rightSideTextureComponent}, false);
    add(rightSidePanel, new GridBagConstraints(
        1, 2, 1, 1, 1, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, rowInsets, 0, 0));
    // Fourth row
    JPanel heightPanel = createTitledPanel(
        preferences.getLocalizedString(WallPanel.class, "heightPanel.title"));   
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
        GridBagConstraints.HORIZONTAL, new Insets(0, 8, 0, 5), 50, 0));
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
    
    // Make startPointPanel and endPointPanel visible depending on editable points property
    controller.addPropertyChangeListener(WallController.Property.EDITABLE_POINTS, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            startPointPanel.setVisible(controller.isEditablePoints());
            endPointPanel.setVisible(controller.isEditablePoints());
          }
        });
    startPointPanel.setVisible(controller.isEditablePoints());
    endPointPanel.setVisible(controller.isEditablePoints());
  }
  
  private JPanel createTitledPanel(String title, JComponent [] components, boolean horizontal) {
    JPanel titledPanel = createTitledPanel(title);    
    
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
  
  private JPanel createTitledPanel(String title) {
    JPanel titledPanel = new JPanel(new GridBagLayout());
    Border panelBorder = BorderFactory.createTitledBorder(title);
    // For systems different from Mac OS X 10.5, add an empty border 
    if (!OperatingSystem.isMacOSXLeopardOrSuperior()) {
      panelBorder = BorderFactory.createCompoundBorder(
          panelBorder, BorderFactory.createEmptyBorder(0, 2, 2, 2));
    }    
    titledPanel.setBorder(panelBorder);    
    return titledPanel;
  }

  /**
   * Displays this panel in a modal dialog box. 
   */
  public void displayView(View parentView) {
    JFormattedTextField thicknessTextField = 
        ((JSpinner.DefaultEditor)thicknessSpinner.getEditor()).getTextField();
    if (SwingTools.showConfirmDialog((JComponent)parentView, 
            this, this.dialogTitle, thicknessTextField) == JOptionPane.OK_OPTION) {
      this.controller.modifyWalls();
    }
  }
}
