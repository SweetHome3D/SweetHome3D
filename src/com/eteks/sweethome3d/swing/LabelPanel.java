/*
 * LabelPanel.java 29 nov. 2008
 *
 * Sweet Home 3D, Copyright (c) 2008 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.HashSet;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.eteks.sweethome3d.model.TextStyle;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.LabelController;
import com.eteks.sweethome3d.viewcontroller.View;

/**
 * Label editing panel.
 * @author Emmanuel Puybaret
 */
public class LabelPanel extends JPanel implements DialogView {
  private final boolean         labelModification;
  private final LabelController controller;
  private JLabel                textLabel;
  private JTextArea             textTextArea;
  private JLabel                alignmentLabel;
  private JRadioButton          leftAlignmentRadioButton;
  private JRadioButton          centerAlignmentRadioButton;
  private JRadioButton          rightAlignmentRadioButton;
  private JLabel                fontNameLabel;
  private FontNameComboBox      fontNameComboBox;
  private JLabel                fontSizeLabel;
  private JSpinner              fontSizeSpinner;
  private JLabel                colorLabel;
  private ColorButton           colorButton;
  private NullableCheckBox      visibleIn3DViewCheckBox;
  private JLabel                pitchLabel;
  private JRadioButton          pitch0DegreeRadioButton;
  private JRadioButton          pitch90DegreeRadioButton;
  private JLabel                elevationLabel;
  private JSpinner              elevationSpinner;
  private String                dialogTitle;

  /**
   * Creates a panel that displays label data.
   * @param modification specifies whether this panel edits an existing label or new one
   * @param preferences user preferences
   * @param controller the controller of this panel
   */
  public LabelPanel(boolean modification,
                    UserPreferences preferences,
                    LabelController controller) {
    super(new GridBagLayout());
    this.labelModification = modification;
    this.controller = controller;
    createComponents(modification, preferences, controller);
    setMnemonics(preferences);
    layoutComponents(controller, preferences);
  }

  /**
   * Creates and initializes components.
   */
  private void createComponents(boolean modification,
                                UserPreferences preferences,
                                final LabelController controller) {
    // Create text label and its text field bound to NAME controller property
    this.textLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        LabelPanel.class, "textLabel.text"));
    this.textTextArea = new JTextArea(controller.getText(), 3, 20);
    if (!OperatingSystem.isMacOSX()) {
      this.textTextArea.setFont(UIManager.getFont("TextField.font"));
    }
    this.textTextArea.setDocument(new AutoCompleteDocument(this.textTextArea, preferences.getAutoCompletionStrings("LabelText")));
    // Set tab key as traversal keys
    this.textTextArea.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
        new HashSet<KeyStroke>(Arrays.asList(KeyStroke.getKeyStroke("pressed TAB"))));
    this.textTextArea.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
        new HashSet<KeyStroke>(Arrays.asList(KeyStroke.getKeyStroke("shift pressed TAB"))));
    SwingTools.addAutoSelectionOnFocusGain(this.textTextArea);
    final PropertyChangeListener textChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          ((AutoCompleteDocument)textTextArea.getDocument()).setAutoCompletionEnabled(false);
          textTextArea.setText(controller.getText());
          ((AutoCompleteDocument)textTextArea.getDocument()).setAutoCompletionEnabled(true);
        }
      };
    controller.addPropertyChangeListener(LabelController.Property.TEXT, textChangeListener);
    DocumentListener documentListener = new DocumentListener() {
        public void changedUpdate(DocumentEvent ev) {
          controller.removePropertyChangeListener(LabelController.Property.TEXT, textChangeListener);
          String text = textTextArea.getText();
          if (text == null || text.trim().length() == 0) {
            controller.setText("");
          } else {
            controller.setText(text);
          }
          controller.addPropertyChangeListener(LabelController.Property.TEXT, textChangeListener);
        }

        public void insertUpdate(DocumentEvent ev) {
          changedUpdate(ev);
        }

        public void removeUpdate(DocumentEvent ev) {
          changedUpdate(ev);
        }
      };
    this.textTextArea.getDocument().addDocumentListener(documentListener);

    // Create alignment label and radio buttons bound to controller ALIGNMENT property
    this.alignmentLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        LabelPanel.class, "alignmentLabel.text"));
    this.leftAlignmentRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences,
        LabelPanel.class, "leftAlignmentRadioButton.text"));
    this.centerAlignmentRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences,
        LabelPanel.class, "centerAlignmentRadioButton.text"));
    this.rightAlignmentRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences,
        LabelPanel.class, "rightAlignmentRadioButton.text"));
    ItemListener alignmentRadioButtonsItemListener = new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          if (leftAlignmentRadioButton.isSelected()) {
            controller.setAlignment(TextStyle.Alignment.LEFT);
          } else if (centerAlignmentRadioButton.isSelected()) {
            controller.setAlignment(TextStyle.Alignment.CENTER);
          } else if (rightAlignmentRadioButton.isSelected()) {
            controller.setAlignment(TextStyle.Alignment.RIGHT);
          }
        }
      };
    this.leftAlignmentRadioButton.addItemListener(alignmentRadioButtonsItemListener);
    this.centerAlignmentRadioButton.addItemListener(alignmentRadioButtonsItemListener);
    this.rightAlignmentRadioButton.addItemListener(alignmentRadioButtonsItemListener);
    ButtonGroup alignmentGroup = new ButtonGroup();
    alignmentGroup.add(this.leftAlignmentRadioButton);
    alignmentGroup.add(this.centerAlignmentRadioButton);
    alignmentGroup.add(this.rightAlignmentRadioButton);
    PropertyChangeListener alignmentChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          if (controller.getAlignment() == TextStyle.Alignment.LEFT) {
            leftAlignmentRadioButton.setSelected(true);
          } else if (controller.getAlignment() == TextStyle.Alignment.CENTER) {
            centerAlignmentRadioButton.setSelected(true);
          } else if (controller.getAlignment() == TextStyle.Alignment.RIGHT) {
            rightAlignmentRadioButton.setSelected(true);
          } else {
            SwingTools.deselectAllRadioButtons(leftAlignmentRadioButton, centerAlignmentRadioButton, rightAlignmentRadioButton);
          }
        }
      };
    controller.addPropertyChangeListener(LabelController.Property.ALIGNMENT, alignmentChangeListener);
    alignmentChangeListener.propertyChange(null);

    // Create font name label and combo box bound to controller FONT_NAME property
    this.fontNameLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        LabelPanel.class, "fontNameLabel.text"));
    this.fontNameComboBox = new FontNameComboBox(preferences);
    this.fontNameComboBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          String selectedItem = (String)fontNameComboBox.getSelectedItem();
          controller.setFontName(selectedItem == FontNameComboBox.DEFAULT_SYSTEM_FONT_NAME
              ? null : selectedItem);
        }
      });
    PropertyChangeListener fontNameChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          if (controller.isFontNameSet()) {
            String fontName = controller.getFontName();
            fontNameComboBox.setSelectedItem(fontName == null
                ? FontNameComboBox.DEFAULT_SYSTEM_FONT_NAME : fontName);
          } else {
            fontNameComboBox.setSelectedItem(null);
          }
        }
      };
    controller.addPropertyChangeListener(LabelController.Property.FONT_NAME, fontNameChangeListener);
    fontNameChangeListener.propertyChange(null);

    // Create font size label and its spinner bound to FONT_SIZE controller property
    String unitName = preferences.getLengthUnit().getName();
    this.fontSizeLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, LabelPanel.class,
        "fontSizeLabel.text", unitName));
    final NullableSpinner.NullableSpinnerLengthModel fontSizeSpinnerModel = new NullableSpinner.NullableSpinnerLengthModel(
        preferences, 5, 999);
    this.fontSizeSpinner = new NullableSpinner(fontSizeSpinnerModel);
    final PropertyChangeListener fontSizeChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          Float fontSize = controller.getFontSize();
          fontSizeSpinnerModel.setNullable(fontSize == null);
          fontSizeSpinnerModel.setLength(fontSize);
        }
      };
    fontSizeChangeListener.propertyChange(null);
    controller.addPropertyChangeListener(LabelController.Property.FONT_SIZE, fontSizeChangeListener);
    fontSizeSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(LabelController.Property.FONT_SIZE, fontSizeChangeListener);
          controller.setFontSize(fontSizeSpinnerModel.getLength());
          controller.addPropertyChangeListener(LabelController.Property.FONT_SIZE, fontSizeChangeListener);
        }
      });

    // Create color label and button bound to controller COLOR property
    this.colorLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        LabelPanel.class, "colorLabel.text"));

    this.colorButton = new ColorButton(preferences);
    if (OperatingSystem.isMacOSX()) {
      this.colorButton.putClientProperty("JButton.buttonType", "segmented");
      this.colorButton.putClientProperty("JButton.segmentPosition", "only");
    }
    this.colorButton.setColorDialogTitle(preferences
        .getLocalizedString(LabelPanel.class, "colorDialog.title"));
    this.colorButton.setColor(controller.getColor() != null ? controller.getColor() : getForeground().getRGB());
    this.colorButton.addPropertyChangeListener(ColorButton.COLOR_PROPERTY, new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          controller.setColor(colorButton.getColor());
        }
      });
    controller.addPropertyChangeListener(LabelController.Property.COLOR, new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          colorButton.setColor(controller.getColor());
        }
      });

    // Create pitch components bound to PITCH controller property
    final PropertyChangeListener pitchChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          update3DViewComponents(controller);
        }
      };
    controller.addPropertyChangeListener(LabelController.Property.PITCH, pitchChangeListener);
    this.visibleIn3DViewCheckBox = new NullableCheckBox(SwingTools.getLocalizedLabelText(preferences,
        LabelPanel.class, "visibleIn3DViewCheckBox.text"));
    if (controller.isPitchEnabled() != null) {
      this.visibleIn3DViewCheckBox.setValue(controller.isPitchEnabled());
    } else {
      this.visibleIn3DViewCheckBox.setNullable(true);
      this.visibleIn3DViewCheckBox.setValue(null);
    }
    this.visibleIn3DViewCheckBox.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(LabelController.Property.PITCH, pitchChangeListener);
          if (visibleIn3DViewCheckBox.isNullable()) {
            visibleIn3DViewCheckBox.setNullable(false);
          }
          if (Boolean.FALSE.equals(visibleIn3DViewCheckBox.getValue())) {
            controller.setPitch(null);
          } else if (pitch90DegreeRadioButton.isSelected()) {
            controller.setPitch((float)(Math.PI / 2));
          } else {
            controller.setPitch(0f);
          }
          update3DViewComponents(controller);
          controller.addPropertyChangeListener(LabelController.Property.PITCH, pitchChangeListener);
        }
      });

    this.pitchLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        LabelPanel.class, "pitchLabel.text"));
    this.pitch0DegreeRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences,
        LabelPanel.class, "pitch0DegreeRadioButton.text"));
    ItemListener pitchRadioButtonsItemListener = new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          if (pitch0DegreeRadioButton.isSelected()) {
            controller.setPitch(0f);
          } else if (pitch90DegreeRadioButton.isSelected()) {
            controller.setPitch((float)(Math.PI / 2));
          }
        }
      };
    this.pitch0DegreeRadioButton.addItemListener(pitchRadioButtonsItemListener);
    this.pitch90DegreeRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences,
        LabelPanel.class, "pitch90DegreeRadioButton.text"));
    this.pitch90DegreeRadioButton.addItemListener(pitchRadioButtonsItemListener);
    ButtonGroup pitchGroup = new ButtonGroup();
    pitchGroup.add(this.pitch0DegreeRadioButton);
    pitchGroup.add(this.pitch90DegreeRadioButton);

    // Create elevation label and its spinner bound to ELEVATION controller property
    this.elevationLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        LabelPanel.class, "elevationLabel.text", unitName));
    final NullableSpinner.NullableSpinnerLengthModel elevationSpinnerModel = new NullableSpinner.NullableSpinnerLengthModel(
        preferences, 0f, preferences.getLengthUnit().getMaximumElevation());
    this.elevationSpinner = new NullableSpinner(elevationSpinnerModel);
    elevationSpinnerModel.setNullable(controller.getElevation() == null);
    elevationSpinnerModel.setLength(controller.getElevation());
    final PropertyChangeListener elevationChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          elevationSpinnerModel.setNullable(ev.getNewValue() == null);
          elevationSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(LabelController.Property.ELEVATION, elevationChangeListener);
    elevationSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(LabelController.Property.ELEVATION, elevationChangeListener);
          controller.setElevation(elevationSpinnerModel.getLength());
          controller.addPropertyChangeListener(LabelController.Property.ELEVATION, elevationChangeListener);
        }
      });

    update3DViewComponents(controller);

    this.dialogTitle = preferences.getLocalizedString(LabelPanel.class,
        modification
            ? "labelModification.title"
            : "labelCreation.title");
  }

  private void update3DViewComponents(LabelController controller) {
    boolean visibleIn3D = Boolean.TRUE.equals(controller.isPitchEnabled());
    this.pitch0DegreeRadioButton.setEnabled(visibleIn3D);
    this.pitch90DegreeRadioButton.setEnabled(visibleIn3D);
    this.elevationSpinner.setEnabled(visibleIn3D);
    if (controller.getPitch() != null) {
      if (controller.getPitch() == 0) {
        this.pitch0DegreeRadioButton.setSelected(true);
      } else if (controller.getPitch() == (float)(Math.PI / 2)) {
        this.pitch90DegreeRadioButton.setSelected(true);
      }
    }
  }

  /**
   * Sets components mnemonics and label / component associations.
   */
  private void setMnemonics(UserPreferences preferences) {
    if (!OperatingSystem.isMacOSX()) {
      this.textLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          LabelPanel.class, "textLabel.mnemonic")).getKeyCode());
      this.textLabel.setLabelFor(this.textTextArea);
      this.leftAlignmentRadioButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          LabelPanel.class, "leftAlignmentRadioButton.mnemonic")).getKeyCode());
      this.centerAlignmentRadioButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          LabelPanel.class, "centerAlignmentRadioButton.mnemonic")).getKeyCode());
      this.rightAlignmentRadioButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          LabelPanel.class, "rightAlignmentRadioButton.mnemonic")).getKeyCode());
      this.fontNameLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          LabelPanel.class, "fontNameLabel.mnemonic")).getKeyCode());
      this.fontNameLabel.setLabelFor(this.fontNameComboBox);
      this.fontSizeLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(LabelPanel.class, "fontSizeLabel.mnemonic")).getKeyCode());
      this.fontSizeLabel.setLabelFor(this.fontSizeSpinner);
      this.visibleIn3DViewCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          LabelPanel.class, "visibleIn3DViewCheckBox.mnemonic")).getKeyCode());
      this.pitch0DegreeRadioButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          LabelPanel.class, "pitch0DegreeRadioButton.mnemonic")).getKeyCode());
      this.pitch90DegreeRadioButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          LabelPanel.class, "pitch90DegreeRadioButton.mnemonic")).getKeyCode());
      this.elevationLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          LabelPanel.class, "elevationLabel.mnemonic")).getKeyCode());
      this.elevationLabel.setLabelFor(this.elevationSpinner);
    }
  }

  /**
   * Layouts panel components in panel with their labels.
   */
  private void layoutComponents(final LabelController controller, UserPreferences preferences) {
    int labelAlignment = OperatingSystem.isMacOSX()
        ? GridBagConstraints.LINE_END
        : GridBagConstraints.LINE_START;
    int standardGap = Math.round(5 * SwingTools.getResolutionScale());
    JPanel nameAndStylePanel = SwingTools.createTitledPanel(
        preferences.getLocalizedString(LabelPanel.class, "textAndStylePanel.title"));
    nameAndStylePanel.add(this.textLabel, new GridBagConstraints(
        0, 0, 1, 1, 0, 0, OperatingSystem.isMacOSX()  ? GridBagConstraints.NORTHEAST  : GridBagConstraints.NORTHWEST,
        GridBagConstraints.NONE, new Insets(OperatingSystem.isMacOSX() ? 0 : standardGap, 0, standardGap, 5), 0, 0));
    nameAndStylePanel.add(SwingTools.createScrollPane(this.textTextArea), new GridBagConstraints(
        1, 0, 3, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, new Insets(OperatingSystem.isMacOSX() ? 0 : standardGap, 0, standardGap, 0), 0, 0));
    nameAndStylePanel.add(this.alignmentLabel, new GridBagConstraints(
        0, 1, 1, 1, 0, 0, labelAlignment,
        GridBagConstraints.NONE, new Insets(0, 0, standardGap, standardGap), 0, 0));
    JPanel alignmentPanel = new JPanel(new GridBagLayout());
    alignmentPanel.setOpaque(false);
    alignmentPanel.add(this.leftAlignmentRadioButton, new GridBagConstraints(
        0, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, 0, standardGap), 0, 0));
    alignmentPanel.add(this.centerAlignmentRadioButton, new GridBagConstraints(
        1, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, 0, standardGap), 0, 0));
    alignmentPanel.add(this.rightAlignmentRadioButton, new GridBagConstraints(
        2, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, 0, standardGap), 0, 0));
    nameAndStylePanel.add(alignmentPanel, new GridBagConstraints(
        1, 1, 3, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, standardGap, 0), 0, 0));
    nameAndStylePanel.add(this.fontNameLabel, new GridBagConstraints(
        0, 2, 1, 1, 0, 0, labelAlignment,
        GridBagConstraints.NONE, new Insets(0, 0, standardGap, standardGap), 0, 0));
    Dimension preferredSize = this.fontNameComboBox.getPreferredSize();
    preferredSize.width = Math.min(preferredSize.width, this.textTextArea.getPreferredSize().width);
    this.fontNameComboBox.setPreferredSize(preferredSize);
    nameAndStylePanel.add(this.fontNameComboBox, new GridBagConstraints(
        1, 2, 3, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, standardGap, 0), 0, 0));
    nameAndStylePanel.add(this.fontSizeLabel, new GridBagConstraints(
        0, 3, 1, 1, 0, 0, labelAlignment,
        GridBagConstraints.NONE, new Insets(0, 0, 0, standardGap), 0, 0));
    nameAndStylePanel.add(this.fontSizeSpinner, new GridBagConstraints(
        1, 3, 1, 1, 1, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 5, 0));
    nameAndStylePanel.add(this.colorLabel, new GridBagConstraints(
        2, 3, 1, 1, 0, 0, labelAlignment,
        GridBagConstraints.NONE, new Insets(0, 10, 0, standardGap), 0, 0));
    nameAndStylePanel.add(this.colorButton, new GridBagConstraints(
        3, 3, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, 0, OperatingSystem.isMacOSX()  ? 6  : 0), 0, 0));
    int rowGap = OperatingSystem.isMacOSXLeopardOrSuperior() ? 0 : standardGap;
    add(nameAndStylePanel, new GridBagConstraints(
        0, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.BOTH, new Insets(0, 0, rowGap, 0), 0, 0));

    JPanel rendering3DPanel = SwingTools.createTitledPanel(
        preferences.getLocalizedString(LabelPanel.class, "rendering3DPanel.title"));
    rendering3DPanel.add(this.visibleIn3DViewCheckBox, new GridBagConstraints(
        0, 0, 3, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, OperatingSystem.isMacOSX() ? -8 : 0, standardGap, 0), 0, 0));
    rendering3DPanel.add(this.pitchLabel, new GridBagConstraints(
        0, 1, 1, 1, 0, 0, labelAlignment,
        GridBagConstraints.NONE, new Insets(0, 0, standardGap, standardGap), 0, 0));
    rendering3DPanel.add(this.pitch0DegreeRadioButton, new GridBagConstraints(
        1, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, standardGap, standardGap), 0, 0));
    rendering3DPanel.add(this.pitch90DegreeRadioButton, new GridBagConstraints(
        2, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, standardGap, 0), 0, 0));
    rendering3DPanel.add(this.elevationLabel, new GridBagConstraints(
        0, 3, 1, 1, 0, 0, labelAlignment,
        GridBagConstraints.NONE, new Insets(0, 0, 0, standardGap), 0, 0));
    rendering3DPanel.add(this.elevationSpinner, new GridBagConstraints(
        1, 3, 2, 1, 1, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    add(rendering3DPanel, new GridBagConstraints(
        0, 2, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
  }

  /**
   * Displays this panel in a modal dialog box.
   */
  public void displayView(View parentView) {
    if (SwingTools.showConfirmDialog((JComponent)parentView,
            this, this.dialogTitle, this.textTextArea) == JOptionPane.OK_OPTION
        && this.controller != null) {
      if (this.labelModification) {
        this.controller.modifyLabels();
      } else {
        this.controller.createLabel();
      }
    }
  }
}
