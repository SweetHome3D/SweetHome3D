/*
 * BaseboardChoiceComponent.java 24 mai 2015
 *
 * Sweet Home 3D, Copyright (c) 2015 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.BaseboardChoiceController;
import com.eteks.sweethome3d.viewcontroller.View;

/**
 * Baseboard editing panel.
 * @author Emmanuel Puybaret
 */
public class BaseboardChoiceComponent extends JPanel implements View {
  private NullableCheckBox      visibleCheckBox;
  private JRadioButton          sameColorAsWallRadioButton;
  private JRadioButton          colorRadioButton;
  private ColorButton           colorButton;
  private JRadioButton          textureRadioButton;
  private JComponent            textureComponent;
  private JLabel                heightLabel;
  private JSpinner              heightSpinner;
  private JLabel                thicknessLabel;
  private JSpinner              thicknessSpinner;

  /**
   * Creates a panel that displays baseboard data.
   * @param preferences user preferences
   * @param controller the controller of this panel
   */
  public BaseboardChoiceComponent(UserPreferences preferences,
                                  BaseboardChoiceController controller) {
    super(new GridBagLayout());
    createComponents(preferences, controller);
    setMnemonics(preferences);
    layoutComponents(preferences);
  }

  /**
   * Creates and initializes components and spinners model.
   */
  private void createComponents(UserPreferences preferences,
                                final BaseboardChoiceController controller) {
    // Baseboard visible check box bound to VISIBLE controller property
    this.visibleCheckBox = new NullableCheckBox(SwingTools.getLocalizedLabelText(preferences,
        BaseboardChoiceComponent.class, "visibleCheckBox.text"));
    this.visibleCheckBox.setNullable(controller.getVisible() == null);
    this.visibleCheckBox.setValue(controller.getVisible());
    this.visibleCheckBox.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.setVisible(visibleCheckBox.getValue());
        }
      });
    PropertyChangeListener visibleChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          Boolean visible = controller.getVisible();
          visibleCheckBox.setValue(visible);
          boolean componentsEnabled = visible != Boolean.FALSE;
          sameColorAsWallRadioButton.setEnabled(componentsEnabled);
          colorRadioButton.setEnabled(componentsEnabled);
          textureRadioButton.setEnabled(componentsEnabled);
          colorButton.setEnabled(componentsEnabled);
          ((JComponent)controller.getTextureController().getView()).setEnabled(componentsEnabled);
          heightSpinner.setEnabled(componentsEnabled);
          thicknessSpinner.setEnabled(componentsEnabled);
        }
      };
    controller.addPropertyChangeListener(BaseboardChoiceController.Property.VISIBLE,
        visibleChangeListener);

    this.sameColorAsWallRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences,
        BaseboardChoiceComponent.class, "sameColorAsWallRadioButton.text"));
    this.sameColorAsWallRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (sameColorAsWallRadioButton.isSelected()) {
            controller.setPaint(BaseboardChoiceController.BaseboardPaint.DEFAULT);
          }
        }
      });
    controller.addPropertyChangeListener(BaseboardChoiceController.Property.PAINT,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            updateColorRadioButtons(controller);
          }
        });

    // Baseboard color and texture buttons bound to baseboard controller properties
    this.colorRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences,
        BaseboardChoiceComponent.class, "colorRadioButton.text"));
    this.colorRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (colorRadioButton.isSelected()) {
            controller.setPaint(BaseboardChoiceController.BaseboardPaint.COLORED);
          }
        }
      });
    controller.addPropertyChangeListener(BaseboardChoiceController.Property.PAINT,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            updateColorRadioButtons(controller);
          }
        });

    this.colorButton = new ColorButton(preferences);
    this.colorButton.setColorDialogTitle(preferences.getLocalizedString(
        BaseboardChoiceComponent.class, "colorDialog.title"));
    this.colorButton.setColor(controller.getColor());
    this.colorButton.addPropertyChangeListener(ColorButton.COLOR_PROPERTY,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            controller.setColor(colorButton.getColor());
            controller.setPaint(BaseboardChoiceController.BaseboardPaint.COLORED);
          }
        });
    controller.addPropertyChangeListener(BaseboardChoiceController.Property.COLOR,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            colorButton.setColor(controller.getColor());
          }
        });

    this.textureRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences,
        BaseboardChoiceComponent.class, "textureRadioButton.text"));
    this.textureRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (textureRadioButton.isSelected()) {
            controller.setPaint(BaseboardChoiceController.BaseboardPaint.TEXTURED);
          }
        }
      });

    this.textureComponent = (JComponent)controller.getTextureController().getView();

    ButtonGroup colorButtonGroup = new ButtonGroup();
    colorButtonGroup.add(this.colorRadioButton);
    colorButtonGroup.add(this.textureRadioButton);
    colorButtonGroup.add(this.sameColorAsWallRadioButton);
    updateColorRadioButtons(controller);

    // Create baseboard height label and its spinner bound to HEIGHT controller property
    String unitName = preferences.getLengthUnit().getName();
    float minimumLength = preferences.getLengthUnit().getMinimumLength();
    this.heightLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        BaseboardChoiceComponent.class, "heightLabel.text", unitName));
    final NullableSpinner.NullableSpinnerLengthModel heightSpinnerModel =
        new NullableSpinner.NullableSpinnerLengthModel(preferences, minimumLength,
            controller.getMaxHeight() == null
                ? preferences.getLengthUnit().getMaximumLength() / 10
                : controller.getMaxHeight());
    this.heightSpinner = new NullableSpinner(heightSpinnerModel);
    heightSpinnerModel.setNullable(controller.getHeight() == null);
    final PropertyChangeListener heightChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          heightSpinnerModel.setNullable(ev.getNewValue() == null);
          heightSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(BaseboardChoiceController.Property.HEIGHT,
        heightChangeListener);
    if (controller.getHeight() != null && controller.getMaxHeight() != null) {
      heightSpinnerModel.setLength(Math.min(controller.getHeight(), controller.getMaxHeight()));
    } else {
      heightSpinnerModel.setLength(controller.getHeight());
    }
    heightSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(BaseboardChoiceController.Property.HEIGHT,
              heightChangeListener);
          controller.setHeight(heightSpinnerModel.getLength());
          controller.addPropertyChangeListener(BaseboardChoiceController.Property.HEIGHT,
              heightChangeListener);
        }
      });
    controller.addPropertyChangeListener(BaseboardChoiceController.Property.MAX_HEIGHT,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            if (ev.getOldValue() == null
                || controller.getMaxHeight() != null
                   && ((Number)heightSpinnerModel.getMaximum()).floatValue() < controller.getMaxHeight()) {
              // Change max only if larger value to avoid taking into account intermediate max values
              // that may be fired by auto commit spinners while entering a value
              heightSpinnerModel.setMaximum(controller.getMaxHeight());
            }
          }
        });

    // Create baseboard thickness label and its spinner bound to THICKNESS controller property
    this.thicknessLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        BaseboardChoiceComponent.class, "thicknessLabel.text", unitName));
    final NullableSpinner.NullableSpinnerLengthModel thicknessSpinnerModel =
        new NullableSpinner.NullableSpinnerLengthModel(preferences, minimumLength, 2);
    this.thicknessSpinner = new NullableSpinner(thicknessSpinnerModel);
    thicknessSpinnerModel.setNullable(controller.getThickness() == null);
    thicknessSpinnerModel.setLength(controller.getThickness());
    final PropertyChangeListener thicknessChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          thicknessSpinnerModel.setNullable(ev.getNewValue() == null);
          thicknessSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(BaseboardChoiceController.Property.THICKNESS,
        thicknessChangeListener);
    thicknessSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(BaseboardChoiceController.Property.THICKNESS,
              thicknessChangeListener);
          controller.setThickness(thicknessSpinnerModel.getLength());
          controller.addPropertyChangeListener(BaseboardChoiceController.Property.THICKNESS,
              thicknessChangeListener);
        }
      });

    visibleChangeListener.propertyChange(null);
  }

  /**
   * Updates baseboard color radio buttons.
   */
  private void updateColorRadioButtons(BaseboardChoiceController controller) {
    if (controller.getPaint() == BaseboardChoiceController.BaseboardPaint.COLORED) {
      this.colorRadioButton.setSelected(true);
    } else if (controller.getPaint() == BaseboardChoiceController.BaseboardPaint.TEXTURED) {
      this.textureRadioButton.setSelected(true);
    } else if (controller.getPaint() == BaseboardChoiceController.BaseboardPaint.DEFAULT) {
      this.sameColorAsWallRadioButton.setSelected(true);
    } else { // null
      SwingTools.deselectAllRadioButtons(this.colorRadioButton, this.textureRadioButton,
          this.sameColorAsWallRadioButton);
    }
  }

  /**
   * Sets components mnemonics and label / component associations.
   */
  private void setMnemonics(UserPreferences preferences) {
    if (!OperatingSystem.isMacOSX()) {
      this.visibleCheckBox.setMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(BaseboardChoiceComponent.class, "visibleCheckBox.mnemonic")).getKeyCode());
      this.sameColorAsWallRadioButton.setMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(BaseboardChoiceComponent.class, "sameColorAsWallRadioButton.mnemonic")).getKeyCode());
      this.colorRadioButton.setMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(BaseboardChoiceComponent.class, "colorRadioButton.mnemonic")).getKeyCode());
      this.textureRadioButton.setMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(BaseboardChoiceComponent.class, "textureRadioButton.mnemonic")).getKeyCode());
      this.heightLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(BaseboardChoiceComponent.class, "heightLabel.mnemonic")).getKeyCode());
      this.heightLabel.setLabelFor(this.heightSpinner);
      this.thicknessLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(BaseboardChoiceComponent.class, "thicknessLabel.mnemonic")).getKeyCode());
      this.thicknessLabel.setLabelFor(this.thicknessSpinner);
    }
  }

  /**
   * Layouts panel components in panel with their labels.
   */
  private void layoutComponents(UserPreferences preferences) {
    int labelAlignment = OperatingSystem.isMacOSX()
        ? GridBagConstraints.LINE_END
        : GridBagConstraints.LINE_START;
    int standardGap = Math.round(5 * SwingTools.getResolutionScale());
    add(this.visibleCheckBox, new GridBagConstraints(
        3, 0, 2, 1, 0, 0, GridBagConstraints.CENTER,
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 10, 0), 0, 0));
    add(this.sameColorAsWallRadioButton, new GridBagConstraints(
        3, 1, 2, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE,  new Insets(0, 0, standardGap, standardGap), 0, 0));
    add(this.colorRadioButton, new GridBagConstraints(
        3, 2, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE,  new Insets(0, 0, 2, standardGap), 0, 0));
    add(this.colorButton, new GridBagConstraints(
        4, 2, 1, 1, 1, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 2, 0), 0, 0));
    add(this.textureRadioButton, new GridBagConstraints(
        3, 3, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE,  new Insets(0, 0, 0, standardGap), 0, 0));
    add(this.textureComponent, new GridBagConstraints(
        4, 3, 1, 1, 1, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    add(new JSeparator(), new GridBagConstraints(
        3, 4, 2, 1, 1, 0, GridBagConstraints.CENTER,
        GridBagConstraints.HORIZONTAL, new Insets(3, 0, Math.round(3 * SwingTools.getResolutionScale()), 0), 0, 0));
    add(this.heightLabel, new GridBagConstraints(
        3, 5, 1, 1, 0, 0, labelAlignment,
        GridBagConstraints.NONE, new Insets(0, 0, standardGap, standardGap), 0, 0));
    int spinnerPadX = OperatingSystem.isMacOSX()  ? -20  : -10;
    add(this.heightSpinner, new GridBagConstraints(
        4, 5, 1, 1, 1, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, standardGap, 0), spinnerPadX, 0));
    add(this.thicknessLabel, new GridBagConstraints(
        3, 6, 1, 1, 0, 0, labelAlignment,
        GridBagConstraints.NONE, new Insets(0, 0, 0, standardGap), 0, 0));
    add(this.thicknessSpinner, new GridBagConstraints(
        4, 6, 1, 1, 1, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), spinnerPadX, 0));
    setOpaque(false);
  }
}
