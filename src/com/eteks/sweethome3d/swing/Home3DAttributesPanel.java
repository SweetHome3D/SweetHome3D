/*
 * Home3DAttributesPanel.java 25 juin 07
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.ResourceBundle;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.ObserverCamera;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.Home3DAttributesController;
import com.eteks.sweethome3d.viewcontroller.Home3DAttributesView;
import com.eteks.sweethome3d.viewcontroller.TextureChoiceController;
import com.eteks.sweethome3d.viewcontroller.View;

/**
 * Home 3D attributes editing panel.
 * @author Emmanuel Puybaret
 */
public class Home3DAttributesPanel extends JPanel 
                                   implements Home3DAttributesView {
  private final Home3DAttributesController controller;
  private JLabel                           observerFieldOfViewLabel;
  private JSpinner                         observerFieldOfViewSpinner;
  private JLabel                           observerHeightLabel;
  private JSpinner                         observerHeightSpinner;
  private ResourceBundle                   resource;
  private JRadioButton                     groundColorRadioButton;
  private ColorButton                      groundColorButton;
  private JRadioButton                     groundTextureRadioButton;
  private JComponent                       groundTextureComponent;
  private JLabel                           skyColorLabel;
  private ColorButton                      skyColorButton;
  private JLabel                           brightnessLabel;
  private JSlider                          brightnessSlider;
  private JLabel                           wallsTransparencyLabel;
  private JSlider                          wallsTransparencySlider;

  /**
   * Creates a panel that displays home 3D attributes data according to the units 
   * set in <code>preferences</code>.
   * @param home home from which 3D parameters are edited by this panel
   * @param preferences user preferences
   * @param controller the controller of this panel
   */
  public Home3DAttributesPanel(Home home, UserPreferences preferences,
                               Home3DAttributesController controller) {
    super(new GridBagLayout());
    this.controller = controller;
    this.resource = ResourceBundle.getBundle(
        Home3DAttributesPanel.class.getName());
    createComponents(preferences, controller);
    setMnemonics();
    layoutComponents();
    updateComponents(home);
  }

  /**
   * Creates and initializes components and spinners model.
   */
  private void createComponents(UserPreferences preferences,
                                Home3DAttributesController controller) {
    // Get unit name matching current unit 
    String unitName = preferences.getUnit().getName();
    
    this.observerFieldOfViewLabel = new JLabel(this.resource.getString("observerFieldOfViewLabel.text"));
    this.observerFieldOfViewSpinner = new AutoCommitSpinner(new SpinnerNumberModel(10, 10, 120, 1));
    this.observerHeightLabel = new JLabel(String.format(this.resource.getString("observerHeightLabel.text"), unitName));
    this.observerHeightSpinner = new AutoCommitSpinner(
        new NullableSpinner.NullableSpinnerLengthModel(preferences, 10f, 1000f));
    
    this.groundColorRadioButton = new JRadioButton(this.resource.getString("groundColorRadioButton.text"));
    this.groundColorButton = new ColorButton();
    this.groundColorButton.setColorDialogTitle(this.resource.getString("groundColorDialog.title"));
    this.groundColorButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          groundColorRadioButton.setSelected(true);
        }
      });
    this.groundTextureRadioButton = new JRadioButton(this.resource.getString("groundTextureRadioButton.text"));
    this.groundTextureComponent = (JComponent)controller.getGroundTextureController().getView();
    controller.getGroundTextureController().addPropertyChangeListener(
        TextureChoiceController.Property.TEXTURE,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            groundTextureRadioButton.setSelected(true);
          }
        });
    ButtonGroup group = new ButtonGroup();
    group.add(this.groundColorRadioButton);
    group.add(this.groundTextureRadioButton);
    
    this.skyColorLabel = new JLabel(this.resource.getString("skyColorLabel.text"));
    this.skyColorButton = new ColorButton();
    this.skyColorButton.setColorDialogTitle(this.resource.getString("skyColorDialog.title"));
    
    this.brightnessLabel = new JLabel(this.resource.getString("brightnessLabel.text"));
    this.brightnessSlider = new JSlider(0, 255);
    JLabel darkLabel = new JLabel(this.resource.getString("darkLabel.text"));
    JLabel brightLabel = new JLabel(this.resource.getString("brightLabel.text"));
    Dictionary<Integer,JComponent> brightnessSliderLabelTable = new Hashtable<Integer,JComponent>();
    brightnessSliderLabelTable.put(0, darkLabel);
    brightnessSliderLabelTable.put(255, brightLabel);
    this.brightnessSlider.setLabelTable(brightnessSliderLabelTable);
    this.brightnessSlider.setPaintLabels(true);
    this.brightnessSlider.setPaintTicks(true);
    this.brightnessSlider.setMajorTickSpacing(16);
    
    this.wallsTransparencyLabel = new JLabel(this.resource.getString("wallsTransparencyLabel.text"));
    this.wallsTransparencySlider = new JSlider(0, 255);
    JLabel opaqueLabel = new JLabel(this.resource.getString("opaqueLabel.text"));
    JLabel invisibleLabel = new JLabel(this.resource.getString("invisibleLabel.text"));
    Dictionary<Integer,JComponent> wallsTransparencySliderLabelTable = new Hashtable<Integer,JComponent>();
    wallsTransparencySliderLabelTable.put(0, opaqueLabel);
    wallsTransparencySliderLabelTable.put(255, invisibleLabel);
    this.wallsTransparencySlider.setLabelTable(wallsTransparencySliderLabelTable);
    this.wallsTransparencySlider.setPaintLabels(true);
    this.wallsTransparencySlider.setPaintTicks(true);
    this.wallsTransparencySlider.setMajorTickSpacing(16);
  }
  
  /**
   * Sets components mnemonics and label / component associations.
   */
  private void setMnemonics() {
    if (!OperatingSystem.isMacOSX()) {
      this.observerFieldOfViewLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("observerFieldOfViewLabel.mnemonic")).getKeyCode());
      this.observerFieldOfViewLabel.setLabelFor(this.observerFieldOfViewLabel);
      this.observerHeightLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("observerHeightLabel.mnemonic")).getKeyCode());
      this.observerHeightLabel.setLabelFor(this.observerHeightSpinner);
      this.groundColorRadioButton.setMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("groundColorRadioButton.mnemonic")).getKeyCode());
      this.groundTextureRadioButton.setMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("groundTextureRadioButton.mnemonic")).getKeyCode());
      this.skyColorLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("skyColorLabel.mnemonic")).getKeyCode());
      this.skyColorLabel.setLabelFor(this.skyColorButton);
      this.brightnessLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("brightnessLabel.mnemonic")).getKeyCode());
      this.brightnessLabel.setLabelFor(this.brightnessSlider);
      this.wallsTransparencyLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("wallsTransparencyLabel.mnemonic")).getKeyCode());
      this.wallsTransparencyLabel.setLabelFor(this.wallsTransparencySlider);
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
    add(this.observerFieldOfViewLabel, new GridBagConstraints(
        0, 0, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    Insets componentInsets = new Insets(0, 0, 10, 10);
    add(this.observerFieldOfViewSpinner, new GridBagConstraints(
        1, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, componentInsets, 10, 0));
    add(this.observerHeightLabel, new GridBagConstraints(
        2, 0, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    Insets rightComponentInsets = new Insets(0, 0, 10, 0);
    add(this.observerHeightSpinner, new GridBagConstraints(
        3, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, rightComponentInsets, 10, 0));
    // Second row
    Insets closeLabelInsets = new Insets(0, 0, 2, 5);
    add(this.groundColorRadioButton, new GridBagConstraints(
        0, 1, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, closeLabelInsets, 0, 0));
    add(this.groundColorButton, new GridBagConstraints(
        1, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 2, 10), 0, 0));
    add(this.skyColorLabel, new GridBagConstraints(
        2, 1, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, closeLabelInsets, 0, 0));
    add(this.skyColorButton, new GridBagConstraints(
        3, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 2, 0), 0, 0));
    // Third row
    add(this.groundTextureRadioButton, new GridBagConstraints(
        0, 2, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    add(this.groundTextureComponent, new GridBagConstraints(
        1, 2, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, componentInsets, 0, 0));
    // Fourth row
    add(this.brightnessLabel, new GridBagConstraints(
        0, 3, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
    add(this.brightnessSlider, new GridBagConstraints(
        1, 3, 3, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    // Last row
    add(this.wallsTransparencyLabel, new GridBagConstraints(
        0, 4, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
    add(this.wallsTransparencySlider, new GridBagConstraints(
        1, 4, 3, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
  }

  /**
   * Updates components values from selected walls in <code>home</code>.
   */
  private void updateComponents(Home home) {
    ObserverCamera observerCamera = home.getObserverCamera();
    this.observerFieldOfViewSpinner.setValue((int)Math.round(Math.toDegrees(observerCamera.getFieldOfView())));
    ((NullableSpinner.NullableSpinnerLengthModel)this.observerHeightSpinner.getModel()).
        setLength((float)Math.round(observerCamera.getHeight() * 100) / 100);
    this.groundColorButton.setColor(home.getGroundColor());
    HomeTexture groundTexture = home.getGroundTexture();
    this.controller.getGroundTextureController().setTexture(groundTexture);
    if (groundTexture != null) {
      this.groundTextureRadioButton.setSelected(true);
    } else {
      this.groundColorRadioButton.setSelected(true);
    }
    this.skyColorButton.setColor(home.getSkyColor());
    this.brightnessSlider.setValue(home.getLightColor() & 0xFF);
    this.wallsTransparencySlider.setValue((int)(home.getWallsAlpha() * 255));
  }

  /**
   * Returns the edited field of view of the observer camera in radians.
   */
  public float getObserverCameraFieldOfView() {
    return (float)Math.toRadians(((Number)this.observerFieldOfViewSpinner.getValue()).doubleValue());
  }
  
  /**
   * Returns the edited height of the observer camera.
   */
  public float getObserverCameraHeight() {
    return ((NullableSpinner.NullableSpinnerLengthModel)this.observerHeightSpinner.getModel()).getLength();
  }
  
  /**
   * Returns the edited ground color.
   */
  public int getGroundColor() {
    return this.groundColorButton.getColor();
  }

  /**
   * Returns the edited ground texture.
   */
  public HomeTexture getGroundTexture() {
    if (this.groundTextureRadioButton.isSelected()) {
      return this.controller.getGroundTextureController().getTexture();
    } else {
      return null;
    }
  }

  /**
   * Returns the edited sky color.
   */
  public int getSkyColor() {
    return this.skyColorButton.getColor();
  }

  /**
   * Returns the edited light color.
   */
  public int getLightColor() {
    int brightness = this.brightnessSlider.getValue();
    return (brightness << 16) + (brightness << 8) + brightness;
  }

  /**
   * Returns the edited walls alpha.
   */
  public float getWallsAlpha() {
    return this.wallsTransparencySlider.getValue() / 255f;
  }

  /**
   * Displays this panel in a modal dialog box. 
   */
  public void displayView(View parentView) {
    String dialogTitle = resource.getString("home3DAttributes.title");
    JOptionPane optionPane = new JOptionPane(this, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
    final JDialog dialog = optionPane.createDialog(SwingUtilities.getRootPane((JComponent)parentView), dialogTitle);
    // Add a listener that transfer focus to first text field when dialog is shown
    dialog.addComponentListener(new ComponentAdapter() {
        @Override
        public void componentShown(ComponentEvent ev) {
          ((JSpinner.DefaultEditor)observerFieldOfViewSpinner.getEditor()).getTextField().requestFocusInWindow();
          dialog.removeComponentListener(this);
        }
      });
    dialog.setVisible(true);
    
    dialog.dispose();
    if (new Integer(JOptionPane.OK_OPTION).equals(optionPane.getValue()) 
        && this.controller != null) {
      this.controller.modifyHome();
    }
  }
}
