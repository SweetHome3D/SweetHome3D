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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.HomeFurnitureController;
import com.eteks.sweethome3d.viewcontroller.TextureChoiceController;
import com.eteks.sweethome3d.viewcontroller.View;

/**
 * Home furniture editing panel.
 * @author Emmanuel Puybaret
 */
public class HomeFurniturePanel extends JPanel implements DialogView {
  private final HomeFurnitureController controller;
  private JLabel                  nameLabel;
  private JTextField              nameTextField;
  private NullableCheckBox        nameVisibleCheckBox;
  private JLabel                  xLabel;
  private JSpinner                xSpinner;
  private JLabel                  yLabel;
  private JSpinner                ySpinner;
  private JLabel                  elevationLabel;
  private JSpinner                elevationSpinner;
  private JLabel                  angleLabel;
  private JSpinner                angleSpinner;
  private JLabel                  widthLabel;
  private JSpinner                widthSpinner;
  private JLabel                  depthLabel;
  private JSpinner                depthSpinner;
  private JLabel                  heightLabel;
  private JSpinner                heightSpinner;
  private JRadioButton            defaultRadioButton;
  private JRadioButton            colorRadioButton;
  private ColorButton             colorButton;
  private JRadioButton            textureRadioButton;
  private JComponent              textureComponent;
  private NullableCheckBox        visibleCheckBox;
  private NullableCheckBox        mirroredModelCheckBox;
  private String                  dialogTitle;

  /**
   * Creates a panel that displays home furniture data according to the units 
   * set in <code>preferences</code>.
   * @param preferences user preferences
   * @param controller the controller of this panel
   */
  public HomeFurniturePanel(UserPreferences preferences,
                            HomeFurnitureController controller) {
    super(new GridBagLayout());
    this.controller = controller;
    createComponents(preferences, controller);
    setMnemonics(preferences);
    layoutComponents();
  }

  /**
   * Creates and initializes components and spinners model.
   */
  private void createComponents(UserPreferences preferences, 
                                final HomeFurnitureController controller) {
    // Get unit name matching current unit 
    String unitName = preferences.getLengthUnit().getName();
    
    // Create name label and its text field bound to NAME controller property
    this.nameLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, HomeFurniturePanel.class, "nameLabel.text"));
    this.nameTextField = new JTextField(controller.getName(), 10);
    if (!OperatingSystem.isMacOSXLeopardOrSuperior()) {
      SwingTools.addAutoSelectionOnFocusGain(this.nameTextField);
    }
    final PropertyChangeListener nameChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          nameTextField.setText(controller.getName());
        }
      };
    controller.addPropertyChangeListener(HomeFurnitureController.Property.NAME, nameChangeListener);
    this.nameTextField.getDocument().addDocumentListener(new DocumentListener() {
        public void changedUpdate(DocumentEvent ev) {
          controller.removePropertyChangeListener(HomeFurnitureController.Property.NAME, nameChangeListener);
          String name = nameTextField.getText(); 
          if (name == null || name.trim().length() == 0) {
            controller.setName(null);
          } else {
            controller.setName(name);
          }
          controller.addPropertyChangeListener(HomeFurnitureController.Property.NAME, nameChangeListener);
        }
  
        public void insertUpdate(DocumentEvent ev) {
          changedUpdate(ev);
        }
  
        public void removeUpdate(DocumentEvent ev) {
          changedUpdate(ev);
        }
      });
        
    // Create name visible check box bound to NAME_VISIBLE controller property
    this.nameVisibleCheckBox = new NullableCheckBox(SwingTools.getLocalizedLabelText(preferences, 
        HomeFurniturePanel.class, "nameVisibleCheckBox.text"));
    this.nameVisibleCheckBox.setNullable(controller.getNameVisible() == null);
    this.nameVisibleCheckBox.setValue(controller.getNameVisible());
    final PropertyChangeListener nameVisibleChangeListener = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent ev) {
        nameVisibleCheckBox.setNullable(ev.getNewValue() == null);
        nameVisibleCheckBox.setValue((Boolean)ev.getNewValue());
      }
    };
    controller.addPropertyChangeListener(HomeFurnitureController.Property.NAME_VISIBLE, nameVisibleChangeListener);
    this.nameVisibleCheckBox.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(HomeFurnitureController.Property.NAME_VISIBLE, nameVisibleChangeListener);
          controller.setNameVisible(nameVisibleCheckBox.getValue());
          controller.addPropertyChangeListener(HomeFurnitureController.Property.NAME_VISIBLE, nameVisibleChangeListener);
        }
      });
    
    // Create X label and its spinner bound to X controller property
    this.xLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, 
        HomeFurniturePanel.class, "xLabel.text", unitName));
    final NullableSpinner.NullableSpinnerLengthModel xSpinnerModel = 
        new NullableSpinner.NullableSpinnerLengthModel(preferences, -100000f, 100000f);
    this.xSpinner = new NullableSpinner(xSpinnerModel);
    xSpinnerModel.setNullable(controller.getX() == null);
    xSpinnerModel.setLength(controller.getX());
    final PropertyChangeListener xChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          xSpinnerModel.setNullable(ev.getNewValue() == null);
          xSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(HomeFurnitureController.Property.X, xChangeListener);
    xSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(HomeFurnitureController.Property.X, xChangeListener);
          controller.setX(xSpinnerModel.getLength());
          controller.addPropertyChangeListener(HomeFurnitureController.Property.X, xChangeListener);
        }
      });
    
    // Create Y label and its spinner bound to Y controller property
    this.yLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, 
        HomeFurniturePanel.class, "yLabel.text", unitName));
    final NullableSpinner.NullableSpinnerLengthModel ySpinnerModel = 
        new NullableSpinner.NullableSpinnerLengthModel(preferences, -100000f, 100000f);
    this.ySpinner = new NullableSpinner(ySpinnerModel);
    ySpinnerModel.setNullable(controller.getY() == null);
    ySpinnerModel.setLength(controller.getY());
    final PropertyChangeListener yChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          ySpinnerModel.setNullable(ev.getNewValue() == null);
          ySpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(HomeFurnitureController.Property.Y, yChangeListener);
    ySpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(HomeFurnitureController.Property.Y, yChangeListener);
          controller.setY(ySpinnerModel.getLength());
          controller.addPropertyChangeListener(HomeFurnitureController.Property.Y, yChangeListener);
        }
      });
    
    // Create elevation label and its spinner bound to ELEVATION controller property
    this.elevationLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, 
        HomeFurniturePanel.class, "elevationLabel.text", unitName));
    final NullableSpinner.NullableSpinnerLengthModel elevationSpinnerModel = 
        new NullableSpinner.NullableSpinnerLengthModel(preferences, 0f, 2500f);
    this.elevationSpinner = new NullableSpinner(elevationSpinnerModel);
    elevationSpinnerModel.setNullable(controller.getElevation() == null);
    elevationSpinnerModel.setLength(controller.getElevation());
    final PropertyChangeListener elevationChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          elevationSpinnerModel.setNullable(ev.getNewValue() == null);
          elevationSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(HomeFurnitureController.Property.ELEVATION, 
        elevationChangeListener);
    elevationSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(HomeFurnitureController.Property.ELEVATION, 
              elevationChangeListener);
          controller.setElevation(elevationSpinnerModel.getLength());
          controller.addPropertyChangeListener(HomeFurnitureController.Property.ELEVATION, 
              elevationChangeListener);
        }
      });
    
    // Create angle label and its spinner bound to ANGLE_IN_DEGREES controller property
    this.angleLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, 
        HomeFurniturePanel.class, "angleLabel.text"));
    final NullableSpinner.NullableSpinnerNumberModel angleSpinnerModel = 
        new NullableSpinner.NullableSpinnerNumberModel(0, 0, 360, 1);
    this.angleSpinner = new NullableSpinner(angleSpinnerModel);
    Integer angle = controller.getAngleInDegrees();
    angleSpinnerModel.setNullable(angle == null);
    angleSpinnerModel.setValue(angle);
    final PropertyChangeListener angleChangeListener = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent ev) {
        Float newAngle = (Float)ev.getNewValue();
        angleSpinnerModel.setNullable(newAngle == null);
        angleSpinnerModel.setValue(newAngle);
      }
    };
    controller.addPropertyChangeListener(HomeFurnitureController.Property.ANGLE_IN_DEGREES, angleChangeListener);
    angleSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(HomeFurnitureController.Property.ANGLE_IN_DEGREES, angleChangeListener);
          Number value = (Number)angleSpinnerModel.getValue();
          if (value == null) {
            controller.setAngleInDegrees(null);
          } else {
            controller.setAngleInDegrees(value.intValue());
          }
          controller.addPropertyChangeListener(HomeFurnitureController.Property.ANGLE_IN_DEGREES, angleChangeListener);
        }
      });
   
    // Create width label and its spinner bound to WIDTH controller property
    this.widthLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, 
        HomeFurniturePanel.class, "widthLabel.text", unitName));
    final NullableSpinner.NullableSpinnerLengthModel widthSpinnerModel = 
        new NullableSpinner.NullableSpinnerLengthModel(preferences, 0.09999f, 100000f);
    this.widthSpinner = new NullableSpinner(widthSpinnerModel);
    widthSpinnerModel.setNullable(controller.getWidth() == null);
    widthSpinnerModel.setLength(controller.getWidth());
    final PropertyChangeListener widthChangeListener = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent ev) {
        widthSpinnerModel.setNullable(ev.getNewValue() == null);
        widthSpinnerModel.setLength((Float)ev.getNewValue());
      }
    };
    controller.addPropertyChangeListener(HomeFurnitureController.Property.WIDTH, widthChangeListener);
    widthSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(HomeFurnitureController.Property.WIDTH, widthChangeListener);
          controller.setWidth(widthSpinnerModel.getLength());
          controller.addPropertyChangeListener(HomeFurnitureController.Property.WIDTH, widthChangeListener);
        }
      });
    
    // Create depth label and its spinner bound to DEPTH controller property
    this.depthLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, 
        HomeFurniturePanel.class, "depthLabel.text", unitName));
    final NullableSpinner.NullableSpinnerLengthModel depthSpinnerModel = 
        new NullableSpinner.NullableSpinnerLengthModel(preferences, 0.09999f, 100000f);
    this.depthSpinner = new NullableSpinner(depthSpinnerModel);
    depthSpinnerModel.setNullable(controller.getDepth() == null);
    depthSpinnerModel.setLength(controller.getDepth());
    final PropertyChangeListener depthChangeListener = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent ev) {
        depthSpinnerModel.setNullable(ev.getNewValue() == null);
        depthSpinnerModel.setLength((Float)ev.getNewValue());
      }
    };
    controller.addPropertyChangeListener(HomeFurnitureController.Property.DEPTH, depthChangeListener);
    depthSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(HomeFurnitureController.Property.DEPTH, depthChangeListener);
          controller.setDepth(depthSpinnerModel.getLength());
          controller.addPropertyChangeListener(HomeFurnitureController.Property.DEPTH, depthChangeListener);
        }
      });
    
    // Create height label and its spinner bound to HEIGHT controller property
    this.heightLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, 
        HomeFurniturePanel.class, "heightLabel.text", unitName));
    final NullableSpinner.NullableSpinnerLengthModel heightSpinnerModel = 
        new NullableSpinner.NullableSpinnerLengthModel(preferences, 0.09999f, 100000f);
    this.heightSpinner = new NullableSpinner(heightSpinnerModel);
    heightSpinnerModel.setNullable(controller.getHeight() == null);
    heightSpinnerModel.setLength(controller.getHeight());
    final PropertyChangeListener heightChangeListener = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent ev) {
        heightSpinnerModel.setNullable(ev.getNewValue() == null);
        heightSpinnerModel.setLength((Float)ev.getNewValue());
      }
    };
    controller.addPropertyChangeListener(HomeFurnitureController.Property.HEIGHT, heightChangeListener);
    heightSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(HomeFurnitureController.Property.HEIGHT, heightChangeListener);
          controller.setHeight(heightSpinnerModel.getLength());
          controller.addPropertyChangeListener(HomeFurnitureController.Property.HEIGHT, heightChangeListener);
        }
      });
    
    // Create radio buttons bound to COLOR and TEXTURE controller properties
    this.defaultRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences, 
        HomeFurniturePanel.class, "defaultRadioButton.text"));
    this.defaultRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (defaultRadioButton.isSelected()) {
            controller.setPaint(HomeFurnitureController.FurniturePaint.DEFAULT);
          }
        }
      });
    controller.addPropertyChangeListener(HomeFurnitureController.Property.PAINT, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            updateRadioButtons(controller);
          }
        });
    this.colorRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences, 
        HomeFurniturePanel.class, "colorRadioButton.text"));
    this.colorRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (colorRadioButton.isSelected()) {
            controller.setPaint(HomeFurnitureController.FurniturePaint.COLORED);
          }
        }
      });
    
    this.colorButton = new ColorButton();
    this.colorButton.setColorDialogTitle(preferences.getLocalizedString(
        HomeFurniturePanel.class, "colorDialog.title"));
    this.colorButton.setColor(controller.getColor());
    this.colorButton.addPropertyChangeListener(ColorButton.COLOR_PROPERTY, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            controller.setColor(colorButton.getColor());
            controller.setPaint(HomeFurnitureController.FurniturePaint.COLORED);
          }
        });
    controller.addPropertyChangeListener(HomeFurnitureController.Property.COLOR, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            colorButton.setColor(controller.getColor());
          }
        });

    this.textureRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences, 
        HomeFurniturePanel.class, "textureRadioButton.text"));
    this.textureRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (textureRadioButton.isSelected()) {
            controller.setPaint(HomeFurnitureController.FurniturePaint.TEXTURED);
          }
        }
      });
    
    TextureChoiceController textureController = controller.getTextureController();
    if (textureController != null) {
      this.textureComponent = (JComponent)textureController.getView();
    }

    ButtonGroup buttonGroup = new ButtonGroup();
    buttonGroup.add(this.defaultRadioButton);
    buttonGroup.add(this.colorRadioButton);
    buttonGroup.add(this.textureRadioButton);
    updateRadioButtons(controller);

    // Create visible check box bound to VISIBLE controller property
    this.visibleCheckBox = new NullableCheckBox(SwingTools.getLocalizedLabelText(preferences, 
        HomeFurniturePanel.class, "visibleCheckBox.text"));
    this.visibleCheckBox.setNullable(controller.getVisible() == null);
    this.visibleCheckBox.setValue(controller.getVisible());
    final PropertyChangeListener visibleChangeListener = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent ev) {
        visibleCheckBox.setNullable(ev.getNewValue() == null);
        visibleCheckBox.setValue((Boolean)ev.getNewValue());
      }
    };
    controller.addPropertyChangeListener(HomeFurnitureController.Property.VISIBLE, visibleChangeListener);
    this.visibleCheckBox.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(HomeFurnitureController.Property.VISIBLE, visibleChangeListener);
          controller.setVisible(visibleCheckBox.getValue());
          controller.addPropertyChangeListener(HomeFurnitureController.Property.VISIBLE, visibleChangeListener);
        }
      });
    
    // Create mirror check box bound to MODEL_MIRRORED controller property
    this.mirroredModelCheckBox = new NullableCheckBox(SwingTools.getLocalizedLabelText(preferences, 
        HomeFurniturePanel.class, "mirroredModelCheckBox.text"));
    this.mirroredModelCheckBox.setNullable(controller.getModelMirrored() == null);
    this.mirroredModelCheckBox.setValue(controller.getModelMirrored());
    final PropertyChangeListener mirroredModelChangeListener = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent ev) {
        mirroredModelCheckBox.setNullable(ev.getNewValue() == null);
        mirroredModelCheckBox.setValue((Boolean)ev.getNewValue());
      }
    };
    controller.addPropertyChangeListener(HomeFurnitureController.Property.MODEL_MIRRORED, mirroredModelChangeListener);
    this.mirroredModelCheckBox.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(HomeFurnitureController.Property.MODEL_MIRRORED, mirroredModelChangeListener);
          controller.setModelMirrored(mirroredModelCheckBox.getValue());
          controller.addPropertyChangeListener(HomeFurnitureController.Property.MODEL_MIRRORED, mirroredModelChangeListener);
        }
      });
    
    updateSizeComponents(controller);     
    // Add a listener that enables / disables size fields depending on furniture resizable
    controller.addPropertyChangeListener(HomeFurnitureController.Property.RESIZABLE, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            updateSizeComponents(controller);     
          }
        });
    
    this.dialogTitle = preferences.getLocalizedString(HomeFurniturePanel.class, "homeFurniture.title");
  }
  
  /**
   * Updates radio buttons. 
   */
  private void updateRadioButtons(HomeFurnitureController controller) {
    if (controller.getPaint() == HomeFurnitureController.FurniturePaint.DEFAULT) {
      this.defaultRadioButton.setSelected(true);
    } else if (controller.getPaint() == HomeFurnitureController.FurniturePaint.COLORED) {
      this.colorRadioButton.setSelected(true);
    } else if (controller.getPaint() == HomeFurnitureController.FurniturePaint.TEXTURED) {
      this.textureRadioButton.setSelected(true);
    } else { // null
      SwingTools.deselectAllRadioButtons(this.defaultRadioButton, this.colorRadioButton, this.textureRadioButton);
    }
  }

  /**
   * Updates size components depending on the fact that furniture is resizable or not.
   */
  private void updateSizeComponents(final HomeFurnitureController controller) {
    boolean editableSize = controller.isResizable();
    this.widthLabel.setEnabled(editableSize);
    this.widthSpinner.setEnabled(editableSize);
    this.depthLabel.setEnabled(editableSize);
    this.depthSpinner.setEnabled(editableSize);
    this.heightLabel.setEnabled(editableSize);
    this.heightSpinner.setEnabled(editableSize);
    this.mirroredModelCheckBox.setEnabled(editableSize);
  }
  
  /**
   * Sets components mnemonics and label / component associations.
   */
  private void setMnemonics(UserPreferences preferences) {
    if (!OperatingSystem.isMacOSX()) {
      this.nameLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          HomeFurniturePanel.class, "nameLabel.mnemonic")).getKeyCode());
      this.nameLabel.setLabelFor(this.nameTextField);
      this.nameVisibleCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          HomeFurniturePanel.class, "nameVisibleCheckBox.mnemonic")).getKeyCode());
      this.xLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          HomeFurniturePanel.class, "xLabel.mnemonic")).getKeyCode());
      this.xLabel.setLabelFor(this.xSpinner);
      this.yLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          HomeFurniturePanel.class, "yLabel.mnemonic")).getKeyCode());
      this.yLabel.setLabelFor(this.ySpinner);
      this.elevationLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          HomeFurniturePanel.class, "elevationLabel.mnemonic")).getKeyCode());
      this.elevationLabel.setLabelFor(this.elevationSpinner);
      this.angleLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          HomeFurniturePanel.class, "angleLabel.mnemonic")).getKeyCode());
      this.angleLabel.setLabelFor(this.angleSpinner);
      this.widthLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          HomeFurniturePanel.class, "widthLabel.mnemonic")).getKeyCode());
      this.widthLabel.setLabelFor(this.widthSpinner);
      this.depthLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          HomeFurniturePanel.class, "depthLabel.mnemonic")).getKeyCode());
      this.depthLabel.setLabelFor(this.depthSpinner);
      this.heightLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          HomeFurniturePanel.class, "heightLabel.mnemonic")).getKeyCode());
      this.heightLabel.setLabelFor(this.heightSpinner);
      this.defaultRadioButton.setMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(HomeFurniturePanel.class, "defaultRadioButton.mnemonic")).getKeyCode());
      this.colorRadioButton.setMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(HomeFurniturePanel.class, "colorRadioButton.mnemonic")).getKeyCode());
      this.textureRadioButton.setMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(HomeFurniturePanel.class, "textureRadioButton.mnemonic")).getKeyCode());
      this.visibleCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          HomeFurniturePanel.class, "visibleCheckBox.mnemonic")).getKeyCode());
      this.mirroredModelCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          HomeFurniturePanel.class, "mirroredModelCheckBox.mnemonic")).getKeyCode());
    }
  }
  
  /**
   * Layouts panel components in panel with their labels. 
   */
  private void layoutComponents() {
    int labelAlignment = OperatingSystem.isMacOSX() 
        ? GridBagConstraints.LINE_END
        : GridBagConstraints.LINE_START;
    // First row
    Insets labelInsets = new Insets(0, 0, 10, 5);
    add(this.nameLabel, new GridBagConstraints(
        0, 0, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    Insets componentInsets = new Insets(0, 0, 10, 15);
    add(this.nameTextField, new GridBagConstraints(
        1, 0, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 10, 5), 0, 0));
    Insets rightComponentInsets = new Insets(0, 0, 10, 0);
    add(this.nameVisibleCheckBox, new GridBagConstraints(
        3, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    // Second row
    add(this.xLabel, new GridBagConstraints(
        0, 1, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    add(this.xSpinner, new GridBagConstraints(
        1, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, componentInsets, -15, 0));
    add(this.widthLabel, new GridBagConstraints(
        2, 1, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    add(this.widthSpinner, new GridBagConstraints(
        3, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, rightComponentInsets, -10, 0));
    // Third row
    add(this.yLabel, new GridBagConstraints(
        0, 2, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    add(this.ySpinner, new GridBagConstraints(
        1, 2, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, componentInsets, -15, 0));
    add(this.depthLabel, new GridBagConstraints(
        2, 2, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    add(this.depthSpinner, new GridBagConstraints(
        3, 2, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, rightComponentInsets, -10, 0));
    // Forth row
    add(this.elevationLabel, new GridBagConstraints(
        0, 3, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    add(this.elevationSpinner, new GridBagConstraints(
        1, 3, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, componentInsets, -10, 0));
    add(this.heightLabel, new GridBagConstraints(
        2, 3, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    add(this.heightSpinner, new GridBagConstraints(
        3, 3, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, rightComponentInsets, -10, 0));
    // Fifth row
    add(this.angleLabel, new GridBagConstraints(
        0, 4, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
    add(this.angleSpinner, new GridBagConstraints(
        1, 4, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 15), -15, 0));
    add(this.defaultRadioButton, new GridBagConstraints(
        2, 4, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 0), 0, 0));
    // Sixth row
    add(this.mirroredModelCheckBox, new GridBagConstraints(
        1, 5, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 15), 0, 0));
    add(this.colorRadioButton, new GridBagConstraints(
        2, 5, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
    add(this.colorButton, new GridBagConstraints(
        3, 5, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 0), 0, 0));
    // Last row
    add(this.visibleCheckBox, new GridBagConstraints(
        1, 6, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 0, 0, 15), 0, 0));
    if (this.textureComponent != null) {
      add(this.textureRadioButton, new GridBagConstraints(
          2, 6, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
          GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
      add(this.textureComponent, new GridBagConstraints(
          3, 6, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
          GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
      this.textureComponent.setPreferredSize(this.colorButton.getPreferredSize());
    }
  }

  /**
   * Displays this panel in a modal dialog box. 
   */
  public void displayView(View parentView) {
    if (SwingTools.showConfirmDialog((JComponent)parentView, 
            this, this.dialogTitle, this.nameTextField) == JOptionPane.OK_OPTION) {
      this.controller.modifyFurniture();
    }
  }
}
