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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.HomeFurnitureController;
import com.eteks.sweethome3d.viewcontroller.View;

/**
 * Home furniture editing panel.
 * @author Emmanuel Puybaret
 */
public class HomeFurniturePanel extends JPanel implements DialogView {
  private final HomeFurnitureController controller;
  private ResourceBundle          resource;
  private JLabel                  nameLabel;
  private JTextField              nameTextField;
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
  private JLabel                  colorLabel;
  private ColorButton             colorButton;
  private NullableCheckBox        visibleCheckBox;
  private NullableCheckBox        mirroredModelCheckBox;

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
    this.resource = ResourceBundle.getBundle(
        HomeFurniturePanel.class.getName());
    createComponents(preferences, controller);
    setMnemonics();
    layoutComponents();
  }

  /**
   * Creates and initializes components and spinners model.
   */
  private void createComponents(UserPreferences preferences, 
                                final HomeFurnitureController controller) {
    // Get unit name matching current unit 
    String unitName = preferences.getUnit().getName();
    
    // Create name label and its text field bound to NAME controller property
    this.nameLabel = new JLabel(this.resource.getString("nameLabel.text"));
    this.nameTextField = new JTextField(controller.getName(), 10);
    if (!OperatingSystem.isMacOSX()) {
      SwingTools.addAutoSelectionOnFocusGain(this.nameTextField);
    }
    this.nameTextField.getDocument().addDocumentListener(new DocumentListener() {
        public void changedUpdate(DocumentEvent ev) {
          String name = nameTextField.getText(); 
          if (name == null || name.trim().length() == 0) {
            controller.setName(null);
          } else {
            controller.setName(name);
          }
        }
  
        public void insertUpdate(DocumentEvent ev) {
          changedUpdate(ev);
        }
  
        public void removeUpdate(DocumentEvent ev) {
          changedUpdate(ev);
        }
      });
    controller.addPropertyChangeListener(HomeFurnitureController.Property.NAME, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            nameTextField.setText(controller.getName());
          }
        });
        
    // Create X label and its spinner bound to X controller property
    this.xLabel = new JLabel(String.format(this.resource.getString("xLabel.text"), unitName));
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
    this.yLabel = new JLabel(String.format(this.resource.getString("yLabel.text"), unitName));
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
    this.elevationLabel = new JLabel(String.format(this.resource.getString("elevationLabel.text"), unitName));
    final NullableSpinner.NullableSpinnerLengthModel elevationSpinnerModel = 
        new NullableSpinner.NullableSpinnerLengthModel(preferences, 0f, 1000f);
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
    this.angleLabel = new JLabel(this.resource.getString("angleLabel.text"));
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
    this.widthLabel = new JLabel(String.format(this.resource.getString("widthLabel.text"), unitName));
    final NullableSpinner.NullableSpinnerLengthModel widthSpinnerModel = 
        new NullableSpinner.NullableSpinnerLengthModel(preferences, 0.09999f, 100000f);
    this.widthSpinner = new AutoCommitSpinner(widthSpinnerModel);
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
    this.depthLabel = new JLabel(String.format(this.resource.getString("depthLabel.text"), unitName));
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
    this.heightLabel = new JLabel(String.format(this.resource.getString("heightLabel.text"), unitName));
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
    
    // Create color label and its button bound to COLOR controller property
    this.colorLabel = new JLabel(this.resource.getString("colorLabel.text"));
    this.colorButton = new ColorButton();
    this.colorButton.setColorDialogTitle(this.resource.getString("colorDialog.title"));
    this.colorButton.setColor(controller.getColor());
    this.colorButton.addPropertyChangeListener(ColorButton.COLOR_PROPERTY, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            controller.setColor(colorButton.getColor());
          }
        });
    controller.addPropertyChangeListener(HomeFurnitureController.Property.COLOR, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            colorButton.setColor(controller.getColor());
          }
        });

    // Create visible check box bound to VISIBLE controller property
    this.visibleCheckBox = new NullableCheckBox(this.resource.getString("visibleCheckBox.text"));
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
    this.mirroredModelCheckBox = new NullableCheckBox(this.resource.getString("mirroredModelCheckBox.text"));
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
  private void setMnemonics() {
    if (!OperatingSystem.isMacOSX()) {
      this.nameLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("nameLabel.mnemonic")).getKeyCode());
      this.nameLabel.setLabelFor(this.nameTextField);
      this.xLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("xLabel.mnemonic")).getKeyCode());
      this.xLabel.setLabelFor(this.xSpinner);
      this.yLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("yLabel.mnemonic")).getKeyCode());
      this.yLabel.setLabelFor(this.ySpinner);
      this.elevationLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("elevationLabel.mnemonic")).getKeyCode());
      this.elevationLabel.setLabelFor(this.elevationSpinner);
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
          KeyStroke.getKeyStroke(this.resource.getString("visibleCheckBox.mnemonic")).getKeyCode());
      this.mirroredModelCheckBox.setMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("mirroredModelCheckBox.mnemonic")).getKeyCode());
    }
  }
  
  /**
   * Layouts panel composants in panel with their labels. 
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
    Insets componentInsets = new Insets(0, 0, 10, 10);
    add(this.nameTextField, new GridBagConstraints(
        1, 0, 3, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, componentInsets, 0, 0));
    add(this.angleLabel, new GridBagConstraints(
        4, 0, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    Insets rightComponentInsets = new Insets(0, 0, 10, 0);
    add(this.angleSpinner, new GridBagConstraints(
        5, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, rightComponentInsets, -15, 0));
    // Second row
    add(this.xLabel, new GridBagConstraints(
        0, 1, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    add(this.xSpinner, new GridBagConstraints(
        1, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, componentInsets, -15, 0));
    add(this.yLabel, new GridBagConstraints(
        2, 1, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    add(this.ySpinner, new GridBagConstraints(
        3, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, componentInsets, -15, 0));
    add(this.elevationLabel, new GridBagConstraints(
        4, 1, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    add(this.elevationSpinner, new GridBagConstraints(
        5, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, rightComponentInsets, -10, 0));
    // Third row
    add(this.widthLabel, new GridBagConstraints(
        0, 2, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    add(this.widthSpinner, new GridBagConstraints(
        1, 2, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, componentInsets, -15, 0));
    add(this.depthLabel, new GridBagConstraints(
        2, 2, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    add(this.depthSpinner, new GridBagConstraints(
        3, 2, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, componentInsets, -15, 0));
    add(this.heightLabel, new GridBagConstraints(
        4, 2, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    add(this.heightSpinner, new GridBagConstraints(
        5, 2, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, rightComponentInsets, -15, 0));
    // Last row
    Insets lastRowInsets = new Insets(0, 0, 0, 5);
    add(this.colorLabel, new GridBagConstraints(
        0, 3, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, lastRowInsets, 0, 0));
    add(this.colorButton, new GridBagConstraints(
        1, 3, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 0, 0, 10), 0, 0));
    add(this.mirroredModelCheckBox, new GridBagConstraints(
        2, 3, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, lastRowInsets, 0, 0));
    add(this.visibleCheckBox, new GridBagConstraints(
        4, 3, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
  }

  /**
   * Displays this panel in a modal dialog box. 
   */
  public void displayView(View parentView) {
    String dialogTitle = resource.getString("homeFurniture.title");
    JOptionPane optionPane = new JOptionPane(this, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
    final JDialog dialog = optionPane.createDialog(SwingUtilities.getRootPane((JComponent)parentView), dialogTitle);
    // Add a listener that transfer focus to first text field when dialog is shown
    dialog.addComponentListener(new ComponentAdapter() {
        @Override
        public void componentShown(ComponentEvent ev) {
          nameTextField.requestFocusInWindow();
          dialog.removeComponentListener(this);
        }
      });
    dialog.setVisible(true);
    
    dialog.dispose();
    if (new Integer(JOptionPane.OK_OPTION).equals(optionPane.getValue())) {
      this.controller.modifyFurniture();
    }
  }
}
