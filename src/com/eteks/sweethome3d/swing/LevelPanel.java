/*
 * LevelPanel.java  27 oct 2011
 *
 * Sweet Home 3D, Copyright (c) 2011 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
import com.eteks.sweethome3d.viewcontroller.LevelController;
import com.eteks.sweethome3d.viewcontroller.View;

/**
 * Level editing panel.
 * @author Emmanuel Puybaret
 */
public class LevelPanel extends JPanel implements DialogView {
  private final LevelController controller;
  private JLabel                nameLabel;
  private JTextField            nameTextField;
  private JLabel                elevationLabel;
  private JSpinner              elevationSpinner;
  private JLabel                floorThicknessLabel;
  private JSpinner              floorThicknessSpinner;
  private JLabel                heightLabel;
  private JSpinner              heightSpinner;
  private String                dialogTitle;

  /**
   * Creates a panel that displays home levels data according to the units 
   * set in <code>preferences</code>.
   * @param preferences user preferences
   * @param controller the controller of this panel
   */
  public LevelPanel(UserPreferences preferences,
                            LevelController controller) {
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
                                final LevelController controller) {
    // Get unit name matching current unit 
    String unitName = preferences.getLengthUnit().getName();
    
    if (controller.isPropertyEditable(LevelController.Property.NAME)) {
      // Create name label and its text field bound to NAME controller property
      this.nameLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, LevelPanel.class, "nameLabel.text"));
      this.nameTextField = new AutoCompleteTextField(controller.getName(), 15, preferences.getAutoCompletionStrings("LevelName"));
      if (!OperatingSystem.isMacOSXLeopardOrSuperior()) {
        SwingTools.addAutoSelectionOnFocusGain(this.nameTextField);
      }
      final PropertyChangeListener nameChangeListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            nameTextField.setText(controller.getName());
          }
        };
      controller.addPropertyChangeListener(LevelController.Property.NAME, nameChangeListener);
      this.nameTextField.getDocument().addDocumentListener(new DocumentListener() {
          public void changedUpdate(DocumentEvent ev) {
            controller.removePropertyChangeListener(LevelController.Property.NAME, nameChangeListener);
            String name = nameTextField.getText(); 
            if (name == null || name.trim().length() == 0) {
              controller.setName(null);
            } else {
              controller.setName(name);
            }
            controller.addPropertyChangeListener(LevelController.Property.NAME, nameChangeListener);
          }
    
          public void insertUpdate(DocumentEvent ev) {
            changedUpdate(ev);
          }
    
          public void removeUpdate(DocumentEvent ev) {
            changedUpdate(ev);
          }
        });
    }
        
    if (controller.isPropertyEditable(LevelController.Property.ELEVATION)) {
      // Create elevation label and its spinner bound to ELEVATION controller property
      this.elevationLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, 
          LevelPanel.class, "elevationLabel.text", unitName));
      final NullableSpinner.NullableSpinnerLengthModel elevationSpinnerModel = 
          new NullableSpinner.NullableSpinnerLengthModel(preferences, -1000f, 2500f);
      this.elevationSpinner = new NullableSpinner(elevationSpinnerModel);
      elevationSpinnerModel.setNullable(controller.getElevation() == null);
      elevationSpinnerModel.setLength(controller.getElevation());
      final PropertyChangeListener elevationChangeListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            elevationSpinnerModel.setNullable(ev.getNewValue() == null);
            elevationSpinnerModel.setLength((Float)ev.getNewValue());
          }
        };
      controller.addPropertyChangeListener(LevelController.Property.ELEVATION, 
          elevationChangeListener);
      elevationSpinnerModel.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            controller.removePropertyChangeListener(LevelController.Property.ELEVATION, 
                elevationChangeListener);
            controller.setElevation(elevationSpinnerModel.getLength());
            controller.addPropertyChangeListener(LevelController.Property.ELEVATION, 
                elevationChangeListener);
          }
        });
    }
    
    final float minimumLength = preferences.getLengthUnit().getMinimumLength();
    if (controller.isPropertyEditable(LevelController.Property.FLOOR_THICKNESS)) {
      // Create floor thickness label and its spinner bound to FLOOR_THICKNESS controller property
      this.floorThicknessLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, 
          LevelPanel.class, "floorThicknessLabel.text", unitName));
      final NullableSpinner.NullableSpinnerLengthModel floorThicknessSpinnerModel = 
          new NullableSpinner.NullableSpinnerLengthModel(preferences, minimumLength, 1000f);
      this.floorThicknessSpinner = new NullableSpinner(floorThicknessSpinnerModel);
      final PropertyChangeListener floorThicknessChangeListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            Float floorThickness = controller.getFloorThickness();
            floorThicknessSpinnerModel.setNullable(floorThickness == null);
            floorThicknessSpinnerModel.setLength(floorThickness);
          }
        };
      floorThicknessChangeListener.propertyChange(null);
      controller.addPropertyChangeListener(LevelController.Property.FLOOR_THICKNESS, floorThicknessChangeListener);
      floorThicknessSpinnerModel.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            controller.removePropertyChangeListener(LevelController.Property.FLOOR_THICKNESS, floorThicknessChangeListener);
            controller.setFloorThickness(floorThicknessSpinnerModel.getLength());
            controller.addPropertyChangeListener(LevelController.Property.FLOOR_THICKNESS, floorThicknessChangeListener);
          }
        });
    }
    
    if (controller.isPropertyEditable(LevelController.Property.HEIGHT)) {
      // Create floor thickness label and its spinner bound to HEIGHT controller property
      this.heightLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, 
          LevelPanel.class, "heightLabel.text", unitName));
      final NullableSpinner.NullableSpinnerLengthModel heightSpinnerModel = 
          new NullableSpinner.NullableSpinnerLengthModel(preferences, minimumLength, 100000f);
      this.heightSpinner = new NullableSpinner(heightSpinnerModel);
      final PropertyChangeListener heightChangeListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            Float height = controller.getHeight();
            heightSpinnerModel.setNullable(height == null);
            heightSpinnerModel.setLength(height);
          }
        };
      heightChangeListener.propertyChange(null);
      controller.addPropertyChangeListener(LevelController.Property.HEIGHT, heightChangeListener);
      heightSpinnerModel.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            controller.removePropertyChangeListener(LevelController.Property.HEIGHT, heightChangeListener);
            controller.setHeight(heightSpinnerModel.getLength());
            controller.addPropertyChangeListener(LevelController.Property.HEIGHT, heightChangeListener);
          }
        });
    }
    
    this.dialogTitle = preferences.getLocalizedString(LevelPanel.class, "level.title");
  }
  
  /**
   * Sets components mnemonics and label / component associations.
   */
  private void setMnemonics(UserPreferences preferences) {
    if (!OperatingSystem.isMacOSX()) {
      if (this.nameLabel != null) {
        this.nameLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            LevelPanel.class, "nameLabel.mnemonic")).getKeyCode());
        this.nameLabel.setLabelFor(this.nameTextField);
      }
      if (this.elevationLabel != null) {
        this.elevationLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            LevelPanel.class, "elevationLabel.mnemonic")).getKeyCode());
        this.elevationLabel.setLabelFor(this.elevationSpinner);
      }
      if (this.floorThicknessLabel != null) {
        this.floorThicknessLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            LevelPanel.class, "floorThicknessLabel.mnemonic")).getKeyCode());
        this.floorThicknessLabel.setLabelFor(this.floorThicknessSpinner);
      }
      if (this.heightLabel != null) {
        this.heightLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            LevelPanel.class, "heightLabel.mnemonic")).getKeyCode());
        this.heightLabel.setLabelFor(this.heightSpinner);
      }
    }
  }
  
  /**
   * Layouts panel components in panel with their labels. 
   */
  private void layoutComponents(UserPreferences preferences, 
                                final LevelController controller) {
    int labelAlignment = OperatingSystem.isMacOSX() 
        ? GridBagConstraints.LINE_END
        : GridBagConstraints.LINE_START;
    Insets labelInsets = new Insets(0, 0, 5, 5);
    Insets rightComponentInsets = new Insets(0, 0, 5, 0);
    if (this.nameLabel != null) {
      // First row    
      add(this.nameLabel, new GridBagConstraints(
          0, 0, 1, 1, 0, 0, labelAlignment, 
          GridBagConstraints.NONE, labelInsets, 0, 0));
      add(this.nameTextField, new GridBagConstraints(
          1, 0, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
          GridBagConstraints.HORIZONTAL, rightComponentInsets, 0, 0));
    }
    if (this.elevationLabel != null) {
      // Second row
      add(this.elevationLabel, new GridBagConstraints(
          0, 1, 1, 1, 0, 0, labelAlignment, 
          GridBagConstraints.NONE, labelInsets, 0, 0));
      add(this.elevationSpinner, new GridBagConstraints(
          1, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
          GridBagConstraints.HORIZONTAL, rightComponentInsets, -15, 0));
    }
    if (this.floorThicknessLabel != null) {
      // Third row
      add(this.floorThicknessLabel, new GridBagConstraints(
          0, 2, 1, 1, 0, 0, labelAlignment, 
          GridBagConstraints.NONE, labelInsets, 0, 0));
      add(this.floorThicknessSpinner, new GridBagConstraints(
          1, 2, 1, 1, 0, 1, GridBagConstraints.LINE_START, 
          GridBagConstraints.NONE, rightComponentInsets, -15, 0));
    }
    if (this.heightLabel != null) {
      // Last row
      add(this.heightLabel, new GridBagConstraints(
          0, 3, 1, 1, 0, 0, labelAlignment, 
          GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
      add(this.heightSpinner, new GridBagConstraints(
          1, 3, 1, 1, 0, 1, GridBagConstraints.LINE_START, 
          GridBagConstraints.NONE, new Insets(0, 0, 0, 0), -15, 0));
    }
  }

  /**
   * Displays this panel in a modal dialog box. 
   */
  public void displayView(View parentView) {
    if (SwingTools.showConfirmDialog((JComponent)parentView, 
            this, this.dialogTitle, this.nameTextField) == JOptionPane.OK_OPTION) {
      this.controller.modifyLevels();
    }
  }
}
