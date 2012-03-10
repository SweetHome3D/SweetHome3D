/*
 * ObserverCameraPanel.java 09 mars 2012
 *
 * Sweet Home 3D, Copyright (c) 2012 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.ObserverCameraController;
import com.eteks.sweethome3d.viewcontroller.View;

/**
 * Observer camera editing panel.
 * @author Emmanuel Puybaret
 */
public class ObserverCameraPanel extends JPanel implements DialogView {
  private final ObserverCameraController controller;
  private JLabel        xLabel;
  private JSpinner      xSpinner;
  private JLabel        yLabel;
  private JSpinner      ySpinner;
  private JLabel        elevationLabel;
  private JSpinner      elevationSpinner;
  private JLabel        yawLabel;
  private JSpinner      yawSpinner;
  private JLabel        pitchLabel;
  private JSpinner      pitchSpinner;
  private JLabel        fieldOfViewLabel;
  private JSpinner      fieldOfViewSpinner;
  private JCheckBox     adjustObserverCameraElevationCheckBox;
  private String        dialogTitle;

  /**
   * Creates a panel that displays observer camera attributes data according to the units 
   * set in <code>preferences</code>.
   * @param preferences user preferences
   * @param controller the controller of this panel
   */
  public ObserverCameraPanel(UserPreferences preferences,
                             ObserverCameraController controller) {
    super(new GridBagLayout());
    this.controller = controller;
    createComponents(preferences, controller);
    setMnemonics(preferences);
    layoutComponents(preferences);
  }

  /**
   * Creates and initializes components and spinners model.
   */
  private void createComponents(UserPreferences preferences,
                                final ObserverCameraController controller) {
    // Get unit name matching current unit 
    String unitName = preferences.getLengthUnit().getName();
    
    // Create X label and its spinner bound to X controller property
    this.xLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, 
        HomeFurniturePanel.class, "xLabel.text", unitName));
    final float maximumLength = 5E5f;
    final NullableSpinner.NullableSpinnerLengthModel xSpinnerModel = 
        new NullableSpinner.NullableSpinnerLengthModel(preferences, -maximumLength, maximumLength);
    this.xSpinner = new NullableSpinner(xSpinnerModel);
    xSpinnerModel.setLength(controller.getX());
    final PropertyChangeListener xChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          xSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(ObserverCameraController.Property.X, xChangeListener);
    xSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.setX(xSpinnerModel.getLength());
        }
      });
    
    // Create Y label and its spinner bound to Y controller property
    this.yLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, 
        HomeFurniturePanel.class, "yLabel.text", unitName));
    final NullableSpinner.NullableSpinnerLengthModel ySpinnerModel = 
        new NullableSpinner.NullableSpinnerLengthModel(preferences, -maximumLength, maximumLength);
    this.ySpinner = new NullableSpinner(ySpinnerModel);
    ySpinnerModel.setLength(controller.getY());
    final PropertyChangeListener yChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          ySpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(ObserverCameraController.Property.Y, yChangeListener);
    ySpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.setY(ySpinnerModel.getLength());
        }
      });

    // Create camera elevation label and spinner bound to ELEVATION controller property
    this.elevationLabel = new JLabel(String.format(SwingTools.getLocalizedLabelText(preferences, 
        ObserverCameraPanel.class, "elevationLabel.text"), unitName));
    float maximumElevation = preferences.getLengthUnit().getMaximumElevation();
    final NullableSpinner.NullableSpinnerLengthModel elevationSpinnerModel = 
        new NullableSpinner.NullableSpinnerLengthModel(preferences, controller.getMinimumElevation(), maximumElevation);
    this.elevationSpinner = new AutoCommitSpinner(elevationSpinnerModel);    
    elevationSpinnerModel.setLength((float)Math.round(controller.getElevation() * 100) / 100);
    elevationSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.setElevation(elevationSpinnerModel.getLength());
        }
      });
    PropertyChangeListener elevationChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          elevationSpinnerModel.setLength((float)Math.round(controller.getElevation() * 100) / 100);
        }
      };
    controller.addPropertyChangeListener(ObserverCameraController.Property.ELEVATION, elevationChangeListener);
    
    // Create yaw label and spinner bound to YAW_IN_DEGREES controller property
    this.yawLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, 
        ObserverCameraPanel.class, "yawLabel.text"));
    final SpinnerNumberModel yawSpinnerModel = new SpinnerNumberModel(0, -10000, 10000, 5);
    this.yawSpinner = new AutoCommitSpinner(yawSpinnerModel);
    yawSpinnerModel.setValue(controller.getYawInDegrees());
    yawSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.setYawInDegrees(((Number)yawSpinnerModel.getValue()).intValue());
        }
      });
    controller.addPropertyChangeListener(ObserverCameraController.Property.YAW_IN_DEGREES, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            yawSpinnerModel.setValue(controller.getYawInDegrees());
          }
        });
    
    // Create pitch label and spinner bound to SPIN_IN_DEGREES controller property
    this.pitchLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, 
        ObserverCameraPanel.class, "pitchLabel.text"));
    final SpinnerNumberModel pitchSpinnerModel = new SpinnerNumberModel(0, -60, 90, 5);
    this.pitchSpinner = new AutoCommitSpinner(pitchSpinnerModel);
    pitchSpinnerModel.setValue(controller.getPitchInDegrees());
    pitchSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.setPitchInDegrees(((Number)pitchSpinnerModel.getValue()).intValue());
        }
      });
    controller.addPropertyChangeListener(ObserverCameraController.Property.PITCH_IN_DEGREES, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            pitchSpinnerModel.setValue(controller.getPitchInDegrees());
          }
        });
    
    // Create field of view label and spinner bound to FIELD_OF_VIEW_IN_DEGREES controller property
    this.fieldOfViewLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, 
        ObserverCameraPanel.class, "fieldOfViewLabel.text"));
    final SpinnerNumberModel fieldOfViewSpinnerModel = new SpinnerNumberModel(10, 10, 120, 1);
    this.fieldOfViewSpinner = new AutoCommitSpinner(fieldOfViewSpinnerModel);
    fieldOfViewSpinnerModel.setValue(controller.getFieldOfViewInDegrees());
    fieldOfViewSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.setFieldOfViewInDegrees(((Number)fieldOfViewSpinnerModel.getValue()).intValue());
        }
      });
    controller.addPropertyChangeListener(ObserverCameraController.Property.FIELD_OF_VIEW_IN_DEGREES, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            fieldOfViewSpinnerModel.setValue(controller.getFieldOfViewInDegrees());
          }
        });
    
    this.adjustObserverCameraElevationCheckBox = new JCheckBox(SwingTools.getLocalizedLabelText(preferences, 
        ObserverCameraPanel.class, "adjustObserverCameraElevationCheckBox.text"), controller.isElevationAdjusted());
    this.adjustObserverCameraElevationCheckBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          controller.setElevationAdjusted(adjustObserverCameraElevationCheckBox.isSelected());
        }
      });
    controller.addPropertyChangeListener(ObserverCameraController.Property.OBSERVER_CAMERA_ELEVATION_ADJUSTED, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            adjustObserverCameraElevationCheckBox.setSelected(controller.isElevationAdjusted());
          }
        });

    controller.addPropertyChangeListener(ObserverCameraController.Property.MINIMUM_ELEVATION, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            if (elevationSpinnerModel.getLength() != null) {
              elevationSpinnerModel.setLength(Math.max(elevationSpinnerModel.getLength(), controller.getMinimumElevation()));
            }
            elevationSpinnerModel.setMinimum(controller.getMinimumElevation());
          }
        });

    this.dialogTitle = preferences.getLocalizedString(
        ObserverCameraPanel.class, "observerCamera.title");
  }

  /**
   * Sets components mnemonics and label / component associations.
   */
  private void setMnemonics(UserPreferences preferences) {
    if (!OperatingSystem.isMacOSX()) {
      this.xLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          ObserverCameraPanel.class, "xLabel.mnemonic")).getKeyCode());
      this.xLabel.setLabelFor(this.xSpinner);
      this.yLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          ObserverCameraPanel.class, "yLabel.mnemonic")).getKeyCode());
      this.yLabel.setLabelFor(this.ySpinner);
      this.elevationLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          ObserverCameraPanel.class, "elevationLabel.mnemonic")).getKeyCode());
      this.elevationLabel.setLabelFor(this.elevationSpinner);
      this.fieldOfViewLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          ObserverCameraPanel.class, "fieldOfViewLabel.mnemonic")).getKeyCode());
      this.fieldOfViewLabel.setLabelFor(this.fieldOfViewLabel);
      this.yawLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          ObserverCameraPanel.class, "yawLabel.mnemonic")).getKeyCode());
      this.yawLabel.setLabelFor(this.yawLabel);
      this.pitchLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          ObserverCameraPanel.class, "pitchLabel.mnemonic")).getKeyCode());
      this.pitchLabel.setLabelFor(this.pitchLabel);
      this.adjustObserverCameraElevationCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          ObserverCameraPanel.class, "adjustObserverCameraElevationCheckBox.mnemonic")).getKeyCode());
    }
  }
  
  /**
   * Layouts panel components in panel with their labels. 
   */
  private void layoutComponents(UserPreferences preferences) {
    int labelAlignment = OperatingSystem.isMacOSX() 
        ? GridBagConstraints.LINE_END
        : GridBagConstraints.LINE_START;
    JPanel locationPanel = SwingTools.createTitledPanel(preferences.getLocalizedString(
        ObserverCameraPanel.class, "locationPanel.title"));
    // First row
    Insets labelInsets = new Insets(0, 0, 5, 5);
    locationPanel.add(this.xLabel, new GridBagConstraints(
        0, 0, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    Insets componentInsets = new Insets(0, 0, 5, 0);
    locationPanel.add(this.xSpinner, new GridBagConstraints(
        1, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, componentInsets, -15, 0));
    // Second row
    locationPanel.add(this.yLabel, new GridBagConstraints(
        0, 1, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    locationPanel.add(this.ySpinner, new GridBagConstraints(
        1, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, componentInsets, -15, 0));
    // Third row
    locationPanel.add(this.elevationLabel, new GridBagConstraints(
        0, 2, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    locationPanel.add(this.elevationSpinner, new GridBagConstraints(
        1, 2, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, componentInsets, 0, 0));
    Insets rowInsets;
    if (OperatingSystem.isMacOSXLeopardOrSuperior()) {
      // User smaller insets for Mac OS X 10.5
      rowInsets = new Insets(0, 0, 0, 0);
    } else {
      rowInsets = new Insets(0, 0, 5, 0);
    }
    add(locationPanel, new GridBagConstraints(
        0, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, rowInsets, 0, 0));

    JPanel anglesPanel = SwingTools.createTitledPanel(preferences.getLocalizedString(
        ObserverCameraPanel.class, "anglesPanel.title"));
    anglesPanel.add(this.yawLabel, new GridBagConstraints(
        0, 0, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    anglesPanel.add(this.yawSpinner, new GridBagConstraints(
        1, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, componentInsets, -10, 0));
    anglesPanel.add(this.pitchLabel, new GridBagConstraints(
        0, 1, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    anglesPanel.add(this.pitchSpinner, new GridBagConstraints(
        1, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, componentInsets, 0, 0));
    anglesPanel.add(this.fieldOfViewLabel, new GridBagConstraints(
        0, 2, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    anglesPanel.add(this.fieldOfViewSpinner, new GridBagConstraints(
        1, 2, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, componentInsets, 0, 0));
    add(anglesPanel, new GridBagConstraints(
        1, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, rowInsets, 0, 0));

    if (controller.isObserverCameraElevationAdjustedEditable()) {
      add(this.adjustObserverCameraElevationCheckBox, new GridBagConstraints(
          0, 1, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
          GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    }
  }

  /**
   * Displays this panel in a modal dialog box. 
   */
  public void displayView(View parentView) {
    JFormattedTextField elevationSpinnerTextField = 
        ((JSpinner.DefaultEditor)this.elevationSpinner.getEditor()).getTextField();
    if (SwingTools.showConfirmDialog((JComponent)parentView, this, this.dialogTitle, 
            elevationSpinnerTextField) == JOptionPane.OK_OPTION
        && this.controller != null) {
      this.controller.modifyObserverCamera();
    }
  }
}
