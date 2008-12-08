/*
 * RoomPanel.java 20 nov. 2008
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
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.RoomController;
import com.eteks.sweethome3d.viewcontroller.View;

/**
 * Room editing panel.
 * @author Emmanuel Puybaret
 */
public class RoomPanel extends JPanel implements DialogView {
  private final RoomController controller;
  private ResourceBundle       resource;
  private JLabel               nameLabel;
  private JTextField           nameTextField;
  private NullableCheckBox     areaVisibleCheckBox;
  private NullableCheckBox     floorVisibleCheckBox;
  private JRadioButton         floorColorRadioButton;
  private ColorButton          floorColorButton;
  private JRadioButton         floorTextureRadioButton;
  private JComponent           floorTextureComponent;
  private NullableCheckBox     ceilingVisibleCheckBox;
  private JRadioButton         ceilingColorRadioButton;
  private ColorButton          ceilingColorButton;
  private JRadioButton         ceilingTextureRadioButton;
  private JComponent           ceilingTextureComponent;

  /**
   * Creates a panel that displays room data according to the units set in
   * <code>preferences</code>.
   * @param preferences user preferences
   * @param controller the controller of this panel
   */
  public RoomPanel(UserPreferences preferences,
                   RoomController controller) {
    super(new GridBagLayout());
    this.controller = controller;
    this.resource = ResourceBundle.getBundle(RoomPanel.class.getName());
    createComponents(preferences, controller);
    setMnemonics();
    layoutComponents(preferences, controller);
  }

  /**
   * Creates and initializes components and spinners model.
   */
  private void createComponents(UserPreferences preferences, 
                                final RoomController controller) {
    // Create name label and its text field bound to NAME controller property
    this.nameLabel = new JLabel(this.resource.getString("nameLabel.text"));
    this.nameTextField = new JTextField(controller.getName(), 10);
    if (!OperatingSystem.isMacOSX()) {
      SwingTools.addAutoSelectionOnFocusGain(this.nameTextField);
    }
    final PropertyChangeListener nameChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          nameTextField.setText(controller.getName());
        }
      };
    controller.addPropertyChangeListener(RoomController.Property.NAME, nameChangeListener);
    this.nameTextField.getDocument().addDocumentListener(new DocumentListener() {
        public void changedUpdate(DocumentEvent ev) {
          controller.removePropertyChangeListener(RoomController.Property.NAME, nameChangeListener);
          String name = nameTextField.getText(); 
          if (name == null || name.trim().length() == 0) {
            controller.setName("");
          } else {
            controller.setName(name);
          }
          controller.addPropertyChangeListener(RoomController.Property.NAME, nameChangeListener);
        }
  
        public void insertUpdate(DocumentEvent ev) {
          changedUpdate(ev);
        }
  
        public void removeUpdate(DocumentEvent ev) {
          changedUpdate(ev);
        }
      });
    
    // Create area visible check box bound to AREA_VISIBLE controller property
    this.areaVisibleCheckBox = new NullableCheckBox(this.resource.getString("areaVisibleCheckBox.text"));
    this.areaVisibleCheckBox.setNullable(controller.getAreaVisible() == null);
    this.areaVisibleCheckBox.setValue(controller.getAreaVisible());
    final PropertyChangeListener visibleChangeListener = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent ev) {
        areaVisibleCheckBox.setNullable(ev.getNewValue() == null);
        areaVisibleCheckBox.setValue((Boolean)ev.getNewValue());
      }
    };
    controller.addPropertyChangeListener(RoomController.Property.AREA_VISIBLE, visibleChangeListener);
    this.areaVisibleCheckBox.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(RoomController.Property.AREA_VISIBLE, visibleChangeListener);
          controller.setAreaVisible(areaVisibleCheckBox.getValue());
          controller.addPropertyChangeListener(RoomController.Property.AREA_VISIBLE, visibleChangeListener);
        }
      });

    // Create floor visible check box bound to FLOOR_VISIBLE controller property
    this.floorVisibleCheckBox = new NullableCheckBox(this.resource.getString("floorVisibleCheckBox.text"));
    this.floorVisibleCheckBox.setNullable(controller.getFloorVisible() == null);
    this.floorVisibleCheckBox.setValue(controller.getFloorVisible());
    final PropertyChangeListener floorVisibleChangeListener = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent ev) {
        floorVisibleCheckBox.setNullable(ev.getNewValue() == null);
        floorVisibleCheckBox.setValue((Boolean)ev.getNewValue());
      }
    };
    controller.addPropertyChangeListener(RoomController.Property.FLOOR_VISIBLE, floorVisibleChangeListener);
    this.floorVisibleCheckBox.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(RoomController.Property.FLOOR_VISIBLE, floorVisibleChangeListener);
          controller.setFloorVisible(floorVisibleCheckBox.getValue());
          controller.addPropertyChangeListener(RoomController.Property.FLOOR_VISIBLE, floorVisibleChangeListener);
        }
      });
    
    // Floor color and texture buttons bound to floor controller properties
    this.floorColorRadioButton = new JRadioButton(this.resource.getString("floorColorRadioButton.text"));
    this.floorColorRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (floorColorRadioButton.isSelected()) {
            controller.setFloorPaint(RoomController.RoomPaint.COLORED);
          }
        }
      });
    controller.addPropertyChangeListener(RoomController.Property.FLOOR_PAINT, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            updateFloorRadioButtons(controller);
          }
        });
    
    this.floorColorButton = new ColorButton();
    this.floorColorButton.setColorDialogTitle(this.resource.getString("floorColorDialog.title"));
    this.floorColorButton.setColor(controller.getFloorColor());
    this.floorColorButton.addPropertyChangeListener(ColorButton.COLOR_PROPERTY, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            controller.setFloorColor(floorColorButton.getColor());
          }
        });
    controller.addPropertyChangeListener(RoomController.Property.FLOOR_COLOR, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            floorColorButton.setColor(controller.getFloorColor());
          }
        });

    this.floorTextureRadioButton = new JRadioButton(this.resource.getString("floorTextureRadioButton.text"));
    this.floorTextureRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (floorTextureRadioButton.isSelected()) {
            controller.setFloorPaint(RoomController.RoomPaint.TEXTURED);
          }
        }
      });
    
    this.floorTextureComponent = (JComponent)controller.getFloorTextureController().getView();
    
    ButtonGroup floorButtonGroup = new ButtonGroup();
    floorButtonGroup.add(this.floorColorRadioButton);
    floorButtonGroup.add(this.floorTextureRadioButton);
    updateFloorRadioButtons(controller);
    
    // Create ceiling visible check box bound to CEILING_VISIBLE controller property
    this.ceilingVisibleCheckBox = new NullableCheckBox(this.resource.getString("ceilingVisibleCheckBox.text"));
    this.ceilingVisibleCheckBox.setNullable(controller.getCeilingVisible() == null);
    this.ceilingVisibleCheckBox.setValue(controller.getCeilingVisible());
    final PropertyChangeListener ceilingVisibleChangeListener = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent ev) {
        ceilingVisibleCheckBox.setNullable(ev.getNewValue() == null);
        ceilingVisibleCheckBox.setValue((Boolean)ev.getNewValue());
      }
    };
    controller.addPropertyChangeListener(RoomController.Property.CEILING_VISIBLE, ceilingVisibleChangeListener);
    this.ceilingVisibleCheckBox.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(RoomController.Property.CEILING_VISIBLE, ceilingVisibleChangeListener);
          controller.setCeilingVisible(ceilingVisibleCheckBox.getValue());
          controller.addPropertyChangeListener(RoomController.Property.CEILING_VISIBLE, ceilingVisibleChangeListener);
        }
      });

    // Ceiling color and texture buttons bound to ceiling controller properties
    this.ceilingColorRadioButton = new JRadioButton(this.resource.getString("ceilingColorRadioButton.text"));
    this.ceilingColorRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          if (ceilingColorRadioButton.isSelected()) {
            controller.setCeilingPaint(RoomController.RoomPaint.COLORED);
          }
        }
      });
    controller.addPropertyChangeListener(RoomController.Property.CEILING_PAINT, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            updateCeilingRadioButtons(controller);
          }
        });

    this.ceilingColorButton = new ColorButton();
    this.ceilingColorButton.setColor(controller.getCeilingColor());
    this.ceilingColorButton.setColorDialogTitle(this.resource.getString("ceilingColorDialog.title"));
    this.ceilingColorButton.addPropertyChangeListener(ColorButton.COLOR_PROPERTY, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            controller.setCeilingColor(ceilingColorButton.getColor());
          }
        });
    controller.addPropertyChangeListener(RoomController.Property.CEILING_COLOR, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            ceilingColorButton.setColor(controller.getCeilingColor());
          }
        });
    
    this.ceilingTextureRadioButton = new JRadioButton(this.resource.getString("ceilingTextureRadioButton.text"));
    this.ceilingTextureRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          if (ceilingTextureRadioButton.isSelected()) {
            controller.setCeilingPaint(RoomController.RoomPaint.TEXTURED);
          }
        }
      });
  
    this.ceilingTextureComponent = (JComponent)controller.getCeilingTextureController().getView();

    ButtonGroup ceilingButtonGroup = new ButtonGroup();
    ceilingButtonGroup.add(this.ceilingColorRadioButton);
    ceilingButtonGroup.add(this.ceilingTextureRadioButton);
    updateCeilingRadioButtons(controller);
  }

  /**
   * Updates floor radio buttons. 
   */
  private void updateFloorRadioButtons(RoomController controller) {
    if (controller.getFloorPaint() == RoomController.RoomPaint.COLORED) {
      this.floorColorRadioButton.setSelected(true);
    } else if (controller.getFloorPaint() == RoomController.RoomPaint.TEXTURED) {
      this.floorTextureRadioButton.setSelected(true);
    } else { // null
      SwingTools.deselectAllRadioButtons(this.floorColorRadioButton, this.floorTextureRadioButton);
    }
  }

  /**
   * Updates ceiling radio buttons. 
   */
  private void updateCeilingRadioButtons(RoomController controller) {
    if (controller.getCeilingPaint() == RoomController.RoomPaint.COLORED) {
      this.ceilingColorRadioButton.setSelected(true);
    } else if (controller.getCeilingPaint() == RoomController.RoomPaint.TEXTURED) {
      this.ceilingTextureRadioButton.setSelected(true);
    } else { // null
      SwingTools.deselectAllRadioButtons(this.ceilingColorRadioButton, this.ceilingTextureRadioButton);
    }
  }

  /**
   * Sets components mnemonics and label / component associations.
   */
  private void setMnemonics() {
    if (!OperatingSystem.isMacOSX()) {
      this.nameLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("nameLabel.mnemonic")).getKeyCode());
      this.nameLabel.setLabelFor(this.nameTextField);
      this.areaVisibleCheckBox.setMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("areaVisibleCheckBox.mnemonic")).getKeyCode());
      this.floorVisibleCheckBox.setMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("floorVisibleCheckBox.mnemonic")).getKeyCode());
      this.floorColorRadioButton.setMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("floorColorRadioButton.mnemonic")).getKeyCode());
      this.floorTextureRadioButton.setMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("floorTextureRadioButton.mnemonic")).getKeyCode());
      this.ceilingVisibleCheckBox.setMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("ceilingVisibleCheckBox.mnemonic")).getKeyCode());
      this.ceilingColorRadioButton.setMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("ceilingColorRadioButton.mnemonic")).getKeyCode());
      this.ceilingTextureRadioButton.setMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("ceilingTextureRadioButton.mnemonic")).getKeyCode());
    }
  }
  
  /**
   * Layouts panel components in panel with their labels. 
   */
  private void layoutComponents(UserPreferences preferences, 
                                final RoomController controller) {
    int labelAlignment = OperatingSystem.isMacOSX() 
        ? GridBagConstraints.LINE_END
        : GridBagConstraints.LINE_START;
    // First row
    Insets rowInsets;
    JPanel nameAndAreaPanel = createTitledPanel(
        this.resource.getString("nameAndAreaPanel.title"));
    nameAndAreaPanel.add(this.nameLabel, new GridBagConstraints(
        0, 0, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 8, 0, 5), 0, 0));
    nameAndAreaPanel.add(this.nameTextField, new GridBagConstraints(
        1, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 10), 0, 0));
    nameAndAreaPanel.add(this.areaVisibleCheckBox, new GridBagConstraints(
        2, 0, 1, 1, 1, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    if (OperatingSystem.isMacOSXLeopardOrSuperior()) {
      // User smaller insets for Mac OS X 10.5
      rowInsets = new Insets(0, 0, 0, 0);
    } else {
      rowInsets = new Insets(0, 0, 5, 0);
    }
    add(nameAndAreaPanel, new GridBagConstraints(
        0, 0, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, rowInsets, 0, 0));
    // Last row
    JPanel floorPanel = createVerticalTitledPanel(
        this.resource.getString("floorPanel.title"),
        new JComponent [] {this.floorVisibleCheckBox, null,
                           this.floorColorRadioButton, this.floorColorButton, 
                           this.floorTextureRadioButton, this.floorTextureComponent});
    add(floorPanel, new GridBagConstraints(
        0, 1, 1, 1, 1, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, rowInsets, 0, 0));
    JPanel ceilingPanel = createVerticalTitledPanel(
        this.resource.getString("ceilingPanel.title"),
        new JComponent [] {this.ceilingVisibleCheckBox, null,
                           this.ceilingColorRadioButton, this.ceilingColorButton, 
                           this.ceilingTextureRadioButton, this.ceilingTextureComponent});
    add(ceilingPanel, new GridBagConstraints(
        1, 1, 1, 1, 1, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, rowInsets, 0, 0));
  }
  
  private JPanel createVerticalTitledPanel(String title, JComponent [] components) {
    JPanel titledPanel = createTitledPanel(title);    
    
    for (int i = 0; i < components.length; i += 2) {
      int bottomInset = i < components.length - 2  ? 2  : 0;      
      JComponent component = components [i];
      JComponent nextComponent = components [i + 1];
      if (nextComponent != null) {
        titledPanel.add(component, new GridBagConstraints(
            0, i / 2, 1, 1, 1, 0, GridBagConstraints.LINE_START, 
            GridBagConstraints.NONE, 
            new Insets(0, 0, bottomInset, 5), 0, 0));
        titledPanel.add(nextComponent, new GridBagConstraints(
            1, i / 2, 1, 1, 1, 0, GridBagConstraints.LINE_START, 
            GridBagConstraints.HORIZONTAL, new Insets(0, 0, bottomInset, 0), 0, 0));
      } else {
        titledPanel.add(component, new GridBagConstraints(
            0, i / 2, 2, 1, 1, 0, GridBagConstraints.LINE_START, 
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
    String dialogTitle = resource.getString("room.title");
    if (SwingTools.showConfirmDialog((JComponent)parentView, 
            this, dialogTitle, this.nameTextField) == JOptionPane.OK_OPTION
        && this.controller != null) {
      this.controller.modifyRooms();
    }
  }
}
