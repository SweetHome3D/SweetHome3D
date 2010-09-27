/*
 * CompassPanel.java 22 sept. 2010
 *
 * Copyright (c) 2010 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.TimeZone;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.CompassController;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.View;

/**
 * Compass editing panel.
 * @author Emmanuel Puybaret
 */
public class CompassPanel extends JPanel implements DialogView {
  private final CompassController controller;
  private JLabel                  xLabel;
  private JSpinner                xSpinner;
  private JLabel                  yLabel;
  private JSpinner                ySpinner;
  private JLabel                  diameterLabel;
  private JSpinner                diameterSpinner;
  private JCheckBox               visibleCheckBox;
  private JComponent              northDirectionComponent;
  private JLabel                  longitudeLabel;
  private JSpinner                longitudeSpinner;
  private JLabel                  latitudeLabel;
  private JSpinner                latitudeSpinner;
  private JLabel                  timeZoneLabel;
  private JComboBox               timeZoneComboBox;
  private JLabel                  northDirectionLabel;
  private JSpinner                northDirectionSpinner;
  private String                  dialogTitle;

  /**
   * Creates a panel that displays compass data.
   * @param preferences user preferences
   * @param controller the controller of this panel
   */
  public CompassPanel(UserPreferences preferences,
                      CompassController controller) {
    super(new GridBagLayout());
    this.controller = controller;
    createComponents(preferences, controller);
    setMnemonics(preferences);
    layoutComponents(preferences);
  }

  /**
   * Creates and initializes components.
   */
  private void createComponents(UserPreferences preferences, 
                                final CompassController controller) {
    // Get unit name matching current unit 
    String unitName = preferences.getLengthUnit().getName();

    // Create X label and its spinner bound to X controller property
    this.xLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, 
        CompassPanel.class, "xLabel.text", unitName));
    final NullableSpinner.NullableSpinnerLengthModel xSpinnerModel = 
        new NullableSpinner.NullableSpinnerLengthModel(preferences, -100000f, 100000f);
    this.xSpinner = new JSpinner(xSpinnerModel);
    xSpinnerModel.setLength(controller.getX());
    final PropertyChangeListener xChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          xSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(CompassController.Property.X, xChangeListener);
    xSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(CompassController.Property.X, xChangeListener);
          controller.setX(xSpinnerModel.getLength());
          controller.addPropertyChangeListener(CompassController.Property.X, xChangeListener);
        }
      });
    
    // Create Y label and its spinner bound to Y controller property
    this.yLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, 
        CompassPanel.class, "yLabel.text", unitName));
    final NullableSpinner.NullableSpinnerLengthModel ySpinnerModel = 
        new NullableSpinner.NullableSpinnerLengthModel(preferences, -100000f, 100000f);
    this.ySpinner = new NullableSpinner(ySpinnerModel);
    ySpinnerModel.setLength(controller.getY());
    final PropertyChangeListener yChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          ySpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(CompassController.Property.Y, yChangeListener);
    ySpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(CompassController.Property.Y, yChangeListener);
          controller.setY(ySpinnerModel.getLength());
          controller.addPropertyChangeListener(CompassController.Property.Y, yChangeListener);
        }
      });
    
    // Create diameter label and its spinner bound to DIAMETER controller property
    this.diameterLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, 
        CompassPanel.class, "diameterLabel.text", unitName));
    final NullableSpinner.NullableSpinnerLengthModel diameterSpinnerModel = 
        new NullableSpinner.NullableSpinnerLengthModel(preferences, 0.1f, 2500f);
    this.diameterSpinner = new NullableSpinner(diameterSpinnerModel);
    diameterSpinnerModel.setLength(controller.getDiameter());
    final PropertyChangeListener diameterChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          diameterSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(CompassController.Property.DIAMETER, 
        diameterChangeListener);
    diameterSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(CompassController.Property.DIAMETER, 
              diameterChangeListener);
          controller.setDiameter(diameterSpinnerModel.getLength());
          controller.addPropertyChangeListener(CompassController.Property.DIAMETER, 
              diameterChangeListener);
        }
      });
    
    // Create visible check box bound to VISIBLE controller property
    this.visibleCheckBox = new JCheckBox(SwingTools.getLocalizedLabelText(preferences, 
        CompassPanel.class, "visibleCheckBox.text"));
    this.visibleCheckBox.setSelected(controller.isVisible());
    final PropertyChangeListener visibleChangeListener = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent ev) {
        visibleCheckBox.setSelected((Boolean)ev.getNewValue());
      }
    };
    controller.addPropertyChangeListener(CompassController.Property.VISIBLE, visibleChangeListener);
    this.visibleCheckBox.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(CompassController.Property.VISIBLE, visibleChangeListener);
          controller.setVisible(visibleCheckBox.isSelected());
          controller.addPropertyChangeListener(CompassController.Property.VISIBLE, visibleChangeListener);
        }
      });

    this.latitudeLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, CompassPanel.class, "latitudeLabel.text"));
    final SpinnerNumberModel latitudeSpinnerModel = new SpinnerNumberModel(0., -90., 90., 5);
    this.latitudeSpinner = new JSpinner(latitudeSpinnerModel);
    // Change positive / negative notation by North / South
    JFormattedTextField textField = ((DefaultEditor)this.latitudeSpinner.getEditor()).getTextField();
    NumberFormatter numberFormatter = (NumberFormatter)((DefaultFormatterFactory)textField.getFormatterFactory()).getDefaultFormatter();
    numberFormatter.setFormat(new DecimalFormat("N ##0.000;S ##0.000"));
    textField.setFormatterFactory(new DefaultFormatterFactory(numberFormatter));
    SwingTools.addAutoSelectionOnFocusGain(textField);
    latitudeSpinnerModel.setValue(controller.getLatitudeInDegrees());
    final PropertyChangeListener latitudeChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          latitudeSpinnerModel.setValue((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(CompassController.Property.LATITUDE_IN_DEGREES, latitudeChangeListener);
    latitudeSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(CompassController.Property.LATITUDE_IN_DEGREES, latitudeChangeListener);
          controller.setLatitudeInDegrees(((Number)latitudeSpinnerModel.getValue()).floatValue());
          controller.addPropertyChangeListener(CompassController.Property.LATITUDE_IN_DEGREES, latitudeChangeListener);
        }
      });
    
    this.longitudeLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, CompassPanel.class, "longitudeLabel.text"));
    final SpinnerNumberModel longitudeSpinnerModel = new SpinnerNumberModel(0., -180., 180., 5);
    this.longitudeSpinner = new JSpinner(longitudeSpinnerModel);
    // Change positive / negative notation by East / West
    textField = ((DefaultEditor)this.longitudeSpinner.getEditor()).getTextField();
    numberFormatter = (NumberFormatter)((DefaultFormatterFactory)textField.getFormatterFactory()).getDefaultFormatter();
    numberFormatter.setFormat(new DecimalFormat("E ##0.000;W ##0.000"));
    textField.setFormatterFactory(new DefaultFormatterFactory(numberFormatter));
    SwingTools.addAutoSelectionOnFocusGain(textField);
    longitudeSpinnerModel.setValue(controller.getLongitudeInDegrees());
    final PropertyChangeListener longitudeChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          longitudeSpinnerModel.setValue((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(CompassController.Property.LONGITUDE_IN_DEGREES, longitudeChangeListener);
    longitudeSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(CompassController.Property.LONGITUDE_IN_DEGREES, longitudeChangeListener);
          controller.setLongitudeInDegrees(((Number)longitudeSpinnerModel.getValue()).floatValue());
          controller.addPropertyChangeListener(CompassController.Property.LONGITUDE_IN_DEGREES, longitudeChangeListener);
        }
      });

    this.timeZoneLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, CompassPanel.class, "timeZoneLabel.text"));
    String [] timeZoneIDs = TimeZone.getAvailableIDs();
    Arrays.sort(timeZoneIDs);
    this.timeZoneComboBox = new JComboBox(timeZoneIDs);
    this.timeZoneComboBox.setSelectedItem(controller.getTimeZone());
    final PropertyChangeListener timeZoneChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          timeZoneComboBox.setSelectedItem(ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(CompassController.Property.TIME_ZONE, timeZoneChangeListener);
    this.timeZoneComboBox.setRenderer(new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
          String timeZoneId = (String)value;
          String timeZoneDisplayName = TimeZone.getTimeZone(timeZoneId).getDisplayName();
          value = timeZoneId + " - " + timeZoneDisplayName;
          return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
      });

    this.timeZoneComboBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          controller.removePropertyChangeListener(CompassController.Property.TIME_ZONE, timeZoneChangeListener);
          controller.setTimeZone((String)timeZoneComboBox.getSelectedItem());
          controller.addPropertyChangeListener(CompassController.Property.TIME_ZONE, timeZoneChangeListener);
        }
      });
    
    this.northDirectionLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, CompassPanel.class, "northDirectionLabel.text"));
    // Create a spinner model able to choose an angle modulo 360
    final SpinnerNumberModel northDirectionSpinnerModel = new SpinnerNumberModel(0, 0, 360, 5) {
        @Override
        public Object getNextValue() {
          if (((Number)getValue()).intValue() + ((Number)getStepSize()).intValue() < ((Number)getMaximum()).intValue()) {
            return super.getNextValue();
          } else {
            return ((Number)getValue()).intValue() + ((Number)getStepSize()).intValue() - ((Number)getMaximum()).intValue() + ((Number)getMinimum()).intValue();
          }
        }
        
        @Override
        public Object getPreviousValue() {
          if (((Number)getValue()).intValue() - ((Number)getStepSize()).intValue() >= ((Number)getMinimum()).intValue()) {
            return super.getPreviousValue();
          } else {
            return ((Number)getValue()).intValue() - ((Number)getStepSize()).intValue() - ((Number)getMinimum()).intValue() + ((Number)getMaximum()).intValue();
          }
        }
      };
    this.northDirectionSpinner = new AutoCommitSpinner(northDirectionSpinnerModel);
    northDirectionSpinnerModel.setValue(controller.getNorthDirectionInDegrees());
    this.northDirectionComponent = new JComponent() {
        @Override
        public Dimension getPreferredSize() {
          return new Dimension(35, 35);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
          Graphics2D g2D = (Graphics2D) g;
          g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
          g2D.translate(getWidth() / 2, getHeight() / 2);
          g2D.scale(getWidth() / 2, getWidth() / 2);
          g2D.rotate(Math.toRadians(controller.getNorthDirectionInDegrees()));
          // Draw a round arc
          g2D.setStroke(new BasicStroke(0.5f / getWidth()));
          g2D.draw(new Ellipse2D.Float(-0.7f, -0.7f, 1.4f, 1.4f));
          g2D.draw(new Line2D.Float(-0.85f, 0, -0.7f, 0));
          g2D.draw(new Line2D.Float(0.85f, 0, 0.7f, 0));
          g2D.draw(new Line2D.Float(0, -0.8f, 0, -0.7f));
          g2D.draw(new Line2D.Float(0, 0.85f, 0, 0.7f));
          // Draw a N
          GeneralPath path = new GeneralPath();
          path.moveTo(-0.1f, -0.8f);
          path.lineTo(-0.1f, -1f);
          path.lineTo(0.1f, -0.8f);
          path.lineTo(0.1f, -1f);
          g2D.setStroke(new BasicStroke(1.5f / getWidth()));
          g2D.draw(path);
          // Draw the needle
          GeneralPath needlePath = new GeneralPath();
          needlePath.moveTo(0, -0.75f);
          needlePath.lineTo(0.2f, 0.7f);
          needlePath.lineTo(0, 0.5f);
          needlePath.lineTo(-0.2f, 0.7f);
          needlePath.closePath();
          needlePath.moveTo(-0.02f, 0);
          needlePath.lineTo(0.02f, 0);
          g2D.setStroke(new BasicStroke(4 / getWidth()));
          g2D.draw(needlePath);
        }
      };
    this.northDirectionComponent.setOpaque(false);
    final PropertyChangeListener northDirectionChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          northDirectionSpinnerModel.setValue((Float)ev.getNewValue());
          northDirectionComponent.repaint();
        }
      };
    controller.addPropertyChangeListener(CompassController.Property.NORTH_DIRECTION_IN_DEGREES, northDirectionChangeListener);
    northDirectionSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(CompassController.Property.NORTH_DIRECTION_IN_DEGREES, northDirectionChangeListener);
          controller.setNorthDirectionInDegrees(((Number)northDirectionSpinnerModel.getValue()).intValue());
          northDirectionComponent.repaint();
          controller.addPropertyChangeListener(CompassController.Property.NORTH_DIRECTION_IN_DEGREES, northDirectionChangeListener);
        }
      });

    this.dialogTitle = preferences.getLocalizedString(CompassPanel.class, "compass.title");
  }

  /**
   * Sets components mnemonics and label / component associations.
   */
  private void setMnemonics(UserPreferences preferences) {
    if (!OperatingSystem.isMacOSX()) {
      this.xLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          CompassPanel.class, "xLabel.mnemonic")).getKeyCode());
      this.xLabel.setLabelFor(this.xSpinner);
      this.yLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          CompassPanel.class, "yLabel.mnemonic")).getKeyCode());
      this.yLabel.setLabelFor(this.ySpinner);
      this.diameterLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          CompassPanel.class, "diameterLabel.mnemonic")).getKeyCode());
      this.diameterLabel.setLabelFor(this.diameterSpinner);
      this.visibleCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          CompassPanel.class, "visibleCheckBox.mnemonic")).getKeyCode());
      this.latitudeLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          CompassPanel.class, "latitudeLabel.mnemonic")).getKeyCode());
      this.latitudeLabel.setLabelFor(this.latitudeSpinner);
      this.longitudeLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          CompassPanel.class, "longitudeLabel.mnemonic")).getKeyCode());
      this.longitudeLabel.setLabelFor(this.longitudeSpinner);
      this.timeZoneLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          CompassPanel.class, "timeZoneLabel.mnemonic")).getKeyCode());
      this.timeZoneLabel.setLabelFor(this.timeZoneComboBox);
      this.northDirectionLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          CompassPanel.class, "northDirectionLabel.mnemonic")).getKeyCode());
      this.northDirectionLabel.setLabelFor(this.northDirectionSpinner);
    }
  }
  
  /**
   * Layouts panel components in panel with their labels. 
   */
  private void layoutComponents(UserPreferences preferences) {
    int labelAlignment = OperatingSystem.isMacOSX() 
        ? GridBagConstraints.LINE_END
        : GridBagConstraints.LINE_START;
    // First row
    JPanel compassRosePanel = SwingTools.createTitledPanel(preferences.getLocalizedString(
        CompassPanel.class, "compassRosePanel.title"));
    Insets labelInsets = new Insets(0, 0, 5, 5);
    Insets componentInsets = new Insets(0, 0, 5, 10);
    Insets lastComponentInsets = new Insets(0, 0, 5, 0);
    compassRosePanel.add(this.xLabel, new GridBagConstraints(
        0, 0, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    compassRosePanel.add(this.xSpinner, new GridBagConstraints(
        1, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, componentInsets, -10, 0));
    compassRosePanel.add(this.visibleCheckBox, new GridBagConstraints(
        2, 0, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, lastComponentInsets, 0, 0));
    compassRosePanel.add(this.yLabel, new GridBagConstraints(
        0, 1, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
    compassRosePanel.add(this.ySpinner, new GridBagConstraints(
        1, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 10), -10, 0));
    compassRosePanel.add(this.diameterLabel, new GridBagConstraints(
        2, 1, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
    compassRosePanel.add(this.diameterSpinner, new GridBagConstraints(
        3, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    add(compassRosePanel, new GridBagConstraints(
        0, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    // Second row
    JPanel geographicLocationPanel = SwingTools.createTitledPanel(preferences.getLocalizedString(
        CompassPanel.class, "geographicLocationPanel.title"));
    geographicLocationPanel.add(this.latitudeLabel, new GridBagConstraints(
        0, 0, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    geographicLocationPanel.add(this.latitudeSpinner, new GridBagConstraints(
        1, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, componentInsets, 20, 0));
    geographicLocationPanel.add(this.northDirectionLabel, new GridBagConstraints(
        2, 0, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    geographicLocationPanel.add(this.northDirectionSpinner, new GridBagConstraints(
        3, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));
    geographicLocationPanel.add(this.northDirectionComponent, new GridBagConstraints(
        4, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 0), 0, 0));
    geographicLocationPanel.add(this.longitudeLabel, new GridBagConstraints(
        0, 1, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
    geographicLocationPanel.add(this.longitudeSpinner, new GridBagConstraints(
        1, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 10), 20, 0));
    geographicLocationPanel.add(this.timeZoneLabel, new GridBagConstraints(
        2, 1, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
    geographicLocationPanel.add(this.timeZoneComboBox, new GridBagConstraints(
        3, 1, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 40, 0));
    this.timeZoneComboBox.setPreferredSize(new Dimension(this.latitudeSpinner.getPreferredSize().width, 
        this.timeZoneComboBox.getPreferredSize().height));
    add(geographicLocationPanel, new GridBagConstraints(
        0, 1, 1, 1, 1, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
  }

  /**
   * Displays this panel in a modal dialog box. 
   */
  public void displayView(View parentView) {
    JFormattedTextField northDirectionTextField = 
        ((JSpinner.DefaultEditor)this.northDirectionSpinner.getEditor()).getTextField();
    if (SwingTools.showConfirmDialog((JComponent)parentView, 
            this, this.dialogTitle, northDirectionTextField) == JOptionPane.OK_OPTION
        && this.controller != null) {
      this.controller.modifyCompass();
    }
  }
}
