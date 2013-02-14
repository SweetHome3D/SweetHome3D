/*
 * RoomPanel.java 20 nov. 2008
 *
 * Sweet Home 3D, Copyright (c) 2007 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.RoomController;
import com.eteks.sweethome3d.viewcontroller.TextureChoiceController;
import com.eteks.sweethome3d.viewcontroller.View;

/**
 * Room editing panel.
 * @author Emmanuel Puybaret
 */
public class RoomPanel extends JPanel implements DialogView {
  private final RoomController  controller;
  private JLabel                nameLabel;
  private JTextField            nameTextField;
  private NullableCheckBox      areaVisibleCheckBox;
  private NullableCheckBox      floorVisibleCheckBox;
  private JRadioButton          floorColorRadioButton;
  private ColorButton           floorColorButton;
  private JRadioButton          floorTextureRadioButton;
  private JComponent            floorTextureComponent;
  private JRadioButton          floorMattRadioButton;
  private JRadioButton          floorShinyRadioButton;
  private NullableCheckBox      ceilingVisibleCheckBox;
  private JRadioButton          ceilingColorRadioButton;
  private ColorButton           ceilingColorButton;
  private JRadioButton          ceilingTextureRadioButton;
  private JComponent            ceilingTextureComponent;
  private JRadioButton          ceilingMattRadioButton;
  private JRadioButton          ceilingShinyRadioButton;
  private JCheckBox             splitSurroundingWallsCheckBox;
  private JRadioButton          wallSidesColorRadioButton;
  private ColorButton           wallSidesColorButton;
  private JRadioButton          wallSidesTextureRadioButton;
  private JComponent            wallSidesTextureComponent;
  private JRadioButton          wallSidesMattRadioButton;
  private JRadioButton          wallSidesShinyRadioButton;
  private boolean               firstWallChange;
  private String                dialogTitle;

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
    createComponents(preferences, controller);
    setMnemonics(preferences);
    layoutComponents(preferences);
    this.firstWallChange = true;
  }

  /**
   * Creates and initializes components and spinners model.
   */
  private void createComponents(UserPreferences preferences, 
                                final RoomController controller) {
    if (controller.isPropertyEditable(RoomController.Property.NAME)) {
      // Create name label and its text field bound to NAME controller property
      this.nameLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, 
          RoomPanel.class, "nameLabel.text"));
      this.nameTextField = new AutoCompleteTextField(controller.getName(), 10, preferences.getAutoCompletionStrings("RoomName"));
      if (!OperatingSystem.isMacOSXLeopardOrSuperior()) {
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
    }
    
    if (controller.isPropertyEditable(RoomController.Property.AREA_VISIBLE)) {
      // Create area visible check box bound to AREA_VISIBLE controller property
      this.areaVisibleCheckBox = new NullableCheckBox(SwingTools.getLocalizedLabelText(preferences, 
          RoomPanel.class, "areaVisibleCheckBox.text"));
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
    }
    
    if (controller.isPropertyEditable(RoomController.Property.FLOOR_VISIBLE)) {
      // Create floor visible check box bound to FLOOR_VISIBLE controller property
      this.floorVisibleCheckBox = new NullableCheckBox(SwingTools.getLocalizedLabelText(preferences, 
          RoomPanel.class, "floorVisibleCheckBox.text"));
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
    }
    
    if (controller.isPropertyEditable(RoomController.Property.FLOOR_PAINT)) {
      // Floor color and texture buttons bound to floor controller properties
      this.floorColorRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences, 
          RoomPanel.class, "floorColorRadioButton.text"));
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
              updateFloorColorRadioButtons(controller);
            }
          });
      
      this.floorColorButton = new ColorButton(preferences);
      this.floorColorButton.setColorDialogTitle(preferences.getLocalizedString(
          RoomPanel.class, "floorColorDialog.title"));
      this.floorColorButton.setColor(controller.getFloorColor());
      this.floorColorButton.addPropertyChangeListener(ColorButton.COLOR_PROPERTY, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              controller.setFloorColor(floorColorButton.getColor());
              controller.setFloorPaint(RoomController.RoomPaint.COLORED);
            }
          });
      controller.addPropertyChangeListener(RoomController.Property.FLOOR_COLOR, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              floorColorButton.setColor(controller.getFloorColor());
            }
          });
    
      this.floorTextureRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences, 
          RoomPanel.class, "floorTextureRadioButton.text"));
      this.floorTextureRadioButton.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            if (floorTextureRadioButton.isSelected()) {
              controller.setFloorPaint(RoomController.RoomPaint.TEXTURED);
            }
          }
        });
      
      this.floorTextureComponent = (JComponent)controller.getFloorTextureController().getView();
      
      ButtonGroup floorButtonColorGroup = new ButtonGroup();
      floorButtonColorGroup.add(this.floorColorRadioButton);
      floorButtonColorGroup.add(this.floorTextureRadioButton);
      updateFloorColorRadioButtons(controller);
    }
      
    if (controller.isPropertyEditable(RoomController.Property.FLOOR_SHININESS)) {
      // Floor shininess radio buttons bound to FLOOR_SHININESS controller property
      this.floorMattRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences, 
          RoomPanel.class, "floorMattRadioButton.text"));
      this.floorMattRadioButton.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            if (floorMattRadioButton.isSelected()) {
              controller.setFloorShininess(0f);
            }
          }
        });
      PropertyChangeListener floorShininessListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            updateFloorShininessRadioButtons(controller);
          }
        };
      controller.addPropertyChangeListener(RoomController.Property.FLOOR_SHININESS, 
          floorShininessListener);
  
      this.floorShinyRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences, 
          RoomPanel.class, "floorShinyRadioButton.text"));
      this.floorShinyRadioButton.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            if (floorShinyRadioButton.isSelected()) {
              controller.setFloorShininess(0.25f);
            }
          }
        });
      controller.addPropertyChangeListener(RoomController.Property.FLOOR_SHININESS, 
          floorShininessListener);
      
      ButtonGroup floorShininessButtonGroup = new ButtonGroup();
      floorShininessButtonGroup.add(this.floorMattRadioButton);
      floorShininessButtonGroup.add(this.floorShinyRadioButton);
      updateFloorShininessRadioButtons(controller);
    }
    
    if (controller.isPropertyEditable(RoomController.Property.CEILING_VISIBLE)) {
      // Create ceiling visible check box bound to CEILING_VISIBLE controller property
      this.ceilingVisibleCheckBox = new NullableCheckBox(SwingTools.getLocalizedLabelText(preferences, 
          RoomPanel.class, "ceilingVisibleCheckBox.text"));
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
    }
  
    if (controller.isPropertyEditable(RoomController.Property.CEILING_PAINT)) {
      // Ceiling color and texture buttons bound to ceiling controller properties
      this.ceilingColorRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences, 
          RoomPanel.class, "ceilingColorRadioButton.text"));
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
              updateCeilingColorRadioButtons(controller);
            }
          });
    
      this.ceilingColorButton = new ColorButton(preferences);
      this.ceilingColorButton.setColor(controller.getCeilingColor());
      this.ceilingColorButton.setColorDialogTitle(preferences.getLocalizedString(
          RoomPanel.class, "ceilingColorDialog.title"));
      this.ceilingColorButton.addPropertyChangeListener(ColorButton.COLOR_PROPERTY, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              controller.setCeilingColor(ceilingColorButton.getColor());
              controller.setCeilingPaint(RoomController.RoomPaint.COLORED);
            }
          });
      controller.addPropertyChangeListener(RoomController.Property.CEILING_COLOR, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              ceilingColorButton.setColor(controller.getCeilingColor());
            }
          });
      
      this.ceilingTextureRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences, 
          RoomPanel.class, "ceilingTextureRadioButton.text"));
      this.ceilingTextureRadioButton.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent e) {
            if (ceilingTextureRadioButton.isSelected()) {
              controller.setCeilingPaint(RoomController.RoomPaint.TEXTURED);
            }
          }
        });
    
      this.ceilingTextureComponent = (JComponent)controller.getCeilingTextureController().getView();
  
      ButtonGroup ceilingColorButtonGroup = new ButtonGroup();
      ceilingColorButtonGroup.add(this.ceilingColorRadioButton);
      ceilingColorButtonGroup.add(this.ceilingTextureRadioButton);
      updateCeilingColorRadioButtons(controller);
    }
    
    if (controller.isPropertyEditable(RoomController.Property.CEILING_SHININESS)) {
      // Ceiling shininess radio buttons bound to CEILING_SHININESS controller property
      this.ceilingMattRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences, 
          RoomPanel.class, "ceilingMattRadioButton.text"));
      this.ceilingMattRadioButton.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            if (ceilingMattRadioButton.isSelected()) {
              controller.setCeilingShininess(0f);
            }
          }
        });
      PropertyChangeListener ceilingShininessListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            updateCeilingShininessRadioButtons(controller);
          }
        };
      controller.addPropertyChangeListener(RoomController.Property.CEILING_SHININESS, 
          ceilingShininessListener);
  
      this.ceilingShinyRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences, 
          RoomPanel.class, "ceilingShinyRadioButton.text"));
      this.ceilingShinyRadioButton.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            if (ceilingShinyRadioButton.isSelected()) {
              controller.setCeilingShininess(0.25f);
            }
          }
        });
      controller.addPropertyChangeListener(RoomController.Property.CEILING_SHININESS, 
          ceilingShininessListener);
      
      ButtonGroup ceilingShininessButtonGroup = new ButtonGroup();
      ceilingShininessButtonGroup.add(this.ceilingMattRadioButton);
      ceilingShininessButtonGroup.add(this.ceilingShinyRadioButton);
      updateCeilingShininessRadioButtons(controller);
    }
    
    if (controller.isPropertyEditable(RoomController.Property.SPLIT_SURROUNDING_WALLS)) {
      // Create visible check box bound to SPLIT_SURROUNDING_WALLS controller property
      this.splitSurroundingWallsCheckBox = new JCheckBox(SwingTools.getLocalizedLabelText(preferences, 
          RoomPanel.class, "splitSurroundingWallsCheckBox.text"));
      final String splitSurroundingWallsToolTip = 
          preferences.getLocalizedString(RoomPanel.class, "splitSurroundingWallsCheckBox.tooltip");
      PropertyChangeListener splitSurroundingWallsChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          splitSurroundingWallsCheckBox.setEnabled(controller.isSplitSurroundingWallsNeeded());
          if (splitSurroundingWallsToolTip.length() > 0 && controller.isSplitSurroundingWallsNeeded()) {
            splitSurroundingWallsCheckBox.setToolTipText(splitSurroundingWallsToolTip);
          } else {
            splitSurroundingWallsCheckBox.setToolTipText(null);
          }
          splitSurroundingWallsCheckBox.setSelected(controller.isSplitSurroundingWalls());
        }
      };
      splitSurroundingWallsChangeListener.propertyChange(null);
      controller.addPropertyChangeListener(RoomController.Property.SPLIT_SURROUNDING_WALLS, splitSurroundingWallsChangeListener);
      this.splitSurroundingWallsCheckBox.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ev) {
            controller.setSplitSurroundingWalls(splitSurroundingWallsCheckBox.isSelected());
            firstWallChange = false;
          }
        });
    }
    
    if (controller.isPropertyEditable(RoomController.Property.WALL_SIDES_PAINT)) {
      // Wall sides color and texture buttons bound to walls controller properties
      this.wallSidesColorRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences, 
          RoomPanel.class, "wallSidesColorRadioButton.text"));
      this.wallSidesColorRadioButton.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent e) {
            if (wallSidesColorRadioButton.isSelected()) {
              controller.setWallSidesPaint(RoomController.RoomPaint.COLORED);
              selectSplitSurroundingWallsAtFirstChange();
            }
          }
        });
      controller.addPropertyChangeListener(RoomController.Property.WALL_SIDES_PAINT, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              updateWallSidesRadioButtons(controller);
            }
          });
  
      this.wallSidesColorButton = new ColorButton(preferences);
      this.wallSidesColorButton.setColor(controller.getWallSidesColor());
      this.wallSidesColorButton.setColorDialogTitle(preferences.getLocalizedString(
          RoomPanel.class, "wallSidesColorDialog.title"));
      this.wallSidesColorButton.addPropertyChangeListener(ColorButton.COLOR_PROPERTY, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              controller.setWallSidesColor(wallSidesColorButton.getColor());
            }
          });
      controller.addPropertyChangeListener(RoomController.Property.WALL_SIDES_COLOR, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              wallSidesColorButton.setColor(controller.getWallSidesColor());
              selectSplitSurroundingWallsAtFirstChange();
            }
          });
      
      this.wallSidesTextureRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences, 
          RoomPanel.class, "wallSidesTextureRadioButton.text"));
      this.wallSidesTextureRadioButton.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent e) {
            if (wallSidesTextureRadioButton.isSelected()) {
              controller.setWallSidesPaint(RoomController.RoomPaint.TEXTURED);
              selectSplitSurroundingWallsAtFirstChange();
            }
          }
        });
    
      this.wallSidesTextureComponent = (JComponent)controller.getWallSidesTextureController().getView();
      controller.getWallSidesTextureController().addPropertyChangeListener(TextureChoiceController.Property.TEXTURE, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              selectSplitSurroundingWallsAtFirstChange();
            }
          });
  
      ButtonGroup wallSidesButtonGroup = new ButtonGroup();
      wallSidesButtonGroup.add(this.wallSidesColorRadioButton);
      wallSidesButtonGroup.add(this.wallSidesTextureRadioButton);
      updateWallSidesRadioButtons(controller);
    }
      
    if (controller.isPropertyEditable(RoomController.Property.WALL_SIDES_SHININESS)) {
      // Wall sides shininess radio buttons bound to WALL_SIDES_SHININESS controller property
      this.wallSidesMattRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences, 
          RoomPanel.class, "wallSidesMattRadioButton.text"));
      this.wallSidesMattRadioButton.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            if (wallSidesMattRadioButton.isSelected()) {
              controller.setWallSidesShininess(0f);
              selectSplitSurroundingWallsAtFirstChange();
            }
          }
        });
      PropertyChangeListener wallSidesShininessListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            updateWallSidesShininessRadioButtons(controller);
          }
        };
      controller.addPropertyChangeListener(RoomController.Property.WALL_SIDES_SHININESS, 
          wallSidesShininessListener);
  
      this.wallSidesShinyRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences, 
          RoomPanel.class, "wallSidesShinyRadioButton.text"));
      this.wallSidesShinyRadioButton.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            if (wallSidesShinyRadioButton.isSelected()) {
              controller.setWallSidesShininess(0.25f);
              selectSplitSurroundingWallsAtFirstChange();
            }
          }
        });
      controller.addPropertyChangeListener(RoomController.Property.WALL_SIDES_SHININESS, 
          wallSidesShininessListener);
      
      ButtonGroup wallSidesShininessButtonGroup = new ButtonGroup();
      wallSidesShininessButtonGroup.add(this.wallSidesMattRadioButton);
      wallSidesShininessButtonGroup.add(this.wallSidesShinyRadioButton);
      updateWallSidesShininessRadioButtons(controller);
    }
    
    this.dialogTitle = preferences.getLocalizedString(RoomPanel.class, "room.title");
  }

  /**
   * Updates floor color radio buttons. 
   */
  private void updateFloorColorRadioButtons(RoomController controller) {
    if (controller.getFloorPaint() == RoomController.RoomPaint.COLORED) {
      this.floorColorRadioButton.setSelected(true);
    } else if (controller.getFloorPaint() == RoomController.RoomPaint.TEXTURED) {
      this.floorTextureRadioButton.setSelected(true);
    } else { // null
      SwingTools.deselectAllRadioButtons(this.floorColorRadioButton, this.floorTextureRadioButton);
    }
  }

  /**
   * Updates floor shininess radio buttons. 
   */
  private void updateFloorShininessRadioButtons(RoomController controller) {
    if (controller.getFloorShininess() == null) {
      SwingTools.deselectAllRadioButtons(this.floorMattRadioButton, this.floorShinyRadioButton);
    } else if (controller.getFloorShininess() == 0) {
      this.floorMattRadioButton.setSelected(true);
    } else { // null
      this.floorShinyRadioButton.setSelected(true);
    }
  }

  /**
   * Updates ceiling color radio buttons. 
   */
  private void updateCeilingColorRadioButtons(RoomController controller) {
    if (controller.getCeilingPaint() == RoomController.RoomPaint.COLORED) {
      this.ceilingColorRadioButton.setSelected(true);
    } else if (controller.getCeilingPaint() == RoomController.RoomPaint.TEXTURED) {
      this.ceilingTextureRadioButton.setSelected(true);
    } else { // null
      SwingTools.deselectAllRadioButtons(this.ceilingColorRadioButton, this.ceilingTextureRadioButton);
    }
  }

  /**
   * Updates ceiling shininess radio buttons. 
   */
  private void updateCeilingShininessRadioButtons(RoomController controller) {
    if (controller.getCeilingShininess() == null) {
      SwingTools.deselectAllRadioButtons(this.ceilingMattRadioButton, this.ceilingShinyRadioButton);
    } else if (controller.getCeilingShininess() == 0) {
      this.ceilingMattRadioButton.setSelected(true);
    } else { // null
      this.ceilingShinyRadioButton.setSelected(true);
    }
  }

  /**
   * Updates wall sides radio buttons. 
   */
  private void updateWallSidesRadioButtons(RoomController controller) {
    if (controller.getWallSidesPaint() == RoomController.RoomPaint.COLORED) {
      this.wallSidesColorRadioButton.setSelected(true);
    } else if (controller.getWallSidesPaint() == RoomController.RoomPaint.TEXTURED) {
      this.wallSidesTextureRadioButton.setSelected(true);
    } else { // null
      SwingTools.deselectAllRadioButtons(this.wallSidesColorRadioButton, this.wallSidesTextureRadioButton);
    }
  }

  /**
   * Updates wall sides shininess radio buttons. 
   */
  private void updateWallSidesShininessRadioButtons(RoomController controller) {
    if (controller.getWallSidesShininess() == null) {
      SwingTools.deselectAllRadioButtons(this.wallSidesMattRadioButton, this.wallSidesShinyRadioButton);
    } else if (controller.getWallSidesShininess() == 0) {
      this.wallSidesMattRadioButton.setSelected(true);
    } else { // null
      this.wallSidesShinyRadioButton.setSelected(true);
    }
  }

  private void selectSplitSurroundingWallsAtFirstChange() {
    if (this.firstWallChange
        && this.splitSurroundingWallsCheckBox != null
        && this.splitSurroundingWallsCheckBox.isEnabled()) {
      this.splitSurroundingWallsCheckBox.doClick();
      this.firstWallChange = false;
    }    
  }

  /**
   * Sets components mnemonics and label / component associations.
   */
  private void setMnemonics(UserPreferences preferences) {
    if (!OperatingSystem.isMacOSX()) {
      if (this.nameLabel != null) {
        this.nameLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(RoomPanel.class, "nameLabel.mnemonic")).getKeyCode());
        this.nameLabel.setLabelFor(this.nameTextField);
      }
      if (this.areaVisibleCheckBox != null) {
        this.areaVisibleCheckBox.setMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(RoomPanel.class, "areaVisibleCheckBox.mnemonic")).getKeyCode());
      }
      if (this.floorVisibleCheckBox != null) {
        this.floorVisibleCheckBox.setMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(RoomPanel.class, "floorVisibleCheckBox.mnemonic")).getKeyCode());
      }
      if (this.floorColorRadioButton != null) {
        this.floorColorRadioButton.setMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(RoomPanel.class, "floorColorRadioButton.mnemonic")).getKeyCode());
        this.floorTextureRadioButton.setMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(RoomPanel.class, "floorTextureRadioButton.mnemonic")).getKeyCode());
      }
      if (this.floorMattRadioButton != null) {
        this.floorMattRadioButton.setMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(RoomPanel.class, "floorMattRadioButton.mnemonic")).getKeyCode());
        this.floorShinyRadioButton.setMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(RoomPanel.class, "floorShinyRadioButton.mnemonic")).getKeyCode());
      }
      if (this.ceilingVisibleCheckBox != null) {
        this.ceilingVisibleCheckBox.setMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(RoomPanel.class, "ceilingVisibleCheckBox.mnemonic")).getKeyCode());
      }
      if (this.ceilingColorRadioButton != null) {
        this.ceilingColorRadioButton.setMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(RoomPanel.class, "ceilingColorRadioButton.mnemonic")).getKeyCode());
        this.ceilingTextureRadioButton.setMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(RoomPanel.class, "ceilingTextureRadioButton.mnemonic")).getKeyCode());
      }
      if (this.ceilingMattRadioButton != null) {
        this.ceilingMattRadioButton.setMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(RoomPanel.class, "ceilingMattRadioButton.mnemonic")).getKeyCode());
        this.ceilingShinyRadioButton.setMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(RoomPanel.class, "ceilingShinyRadioButton.mnemonic")).getKeyCode());
      }
      if (this.splitSurroundingWallsCheckBox != null) {
        this.splitSurroundingWallsCheckBox.setMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(RoomPanel.class, "splitSurroundingWallsCheckBox.mnemonic")).getKeyCode());
      }
      if (this.wallSidesColorRadioButton != null) {
        this.wallSidesColorRadioButton.setMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(RoomPanel.class, "wallSidesColorRadioButton.mnemonic")).getKeyCode());
        this.wallSidesTextureRadioButton.setMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(RoomPanel.class, "wallSidesTextureRadioButton.mnemonic")).getKeyCode());
      }
      if (this.wallSidesMattRadioButton != null) {
        this.wallSidesMattRadioButton.setMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(RoomPanel.class, "wallSidesMattRadioButton.mnemonic")).getKeyCode());
        this.wallSidesShinyRadioButton.setMnemonic(KeyStroke.getKeyStroke(
            preferences.getLocalizedString(RoomPanel.class, "wallSidesShinyRadioButton.mnemonic")).getKeyCode());
      }
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
    if (this.nameLabel != null || this.areaVisibleCheckBox != null) {
      JPanel nameAndAreaPanel = SwingTools.createTitledPanel(preferences.getLocalizedString(
          RoomPanel.class, "nameAndAreaPanel.title"));
      if (this.nameLabel != null) {
        nameAndAreaPanel.add(this.nameLabel, new GridBagConstraints(
            0, 0, 1, 1, 0, 0, labelAlignment, 
            GridBagConstraints.HORIZONTAL, new Insets(0, 8, 0, 5), 0, 0));
        nameAndAreaPanel.add(this.nameTextField, new GridBagConstraints(
            1, 0, 1, 1, 1, 0, GridBagConstraints.LINE_START, 
            GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 10), 0, 0));
      }
      if (this.areaVisibleCheckBox != null) {
        nameAndAreaPanel.add(this.areaVisibleCheckBox, new GridBagConstraints(
            2, 0, 1, 1, 1, 0, GridBagConstraints.LINE_START, 
            GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
      }
      Insets rowInsets;
      if (OperatingSystem.isMacOSXLeopardOrSuperior()) {
        // User smaller insets for Mac OS X 10.5
        rowInsets = new Insets(0, 0, 0, 0);
      } else {
        rowInsets = new Insets(0, 0, 5, 0);
      }
      add(nameAndAreaPanel, new GridBagConstraints(
          0, 0, 3, 1, 0, 0, GridBagConstraints.LINE_START, 
          GridBagConstraints.HORIZONTAL, rowInsets, 0, 0));
    }
    // Last row
    if (this.floorVisibleCheckBox != null || this.floorColorRadioButton != null || this.floorMattRadioButton != null) {
      JPanel floorPanel = createVerticalTitledPanel(preferences.getLocalizedString(
          RoomPanel.class, "floorPanel.title"),
          new JComponent [][] {{this.floorVisibleCheckBox, null,
                                this.floorColorRadioButton, this.floorColorButton, 
                                this.floorTextureRadioButton, this.floorTextureComponent},
                                {this.floorMattRadioButton, this.floorShinyRadioButton}});
      add(floorPanel, new GridBagConstraints(
          0, 1, 1, 1, 1, 0, GridBagConstraints.NORTH,
          GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    }      
    if (this.ceilingVisibleCheckBox != null || this.ceilingColorRadioButton != null || this.ceilingMattRadioButton != null) {
      JPanel ceilingPanel = createVerticalTitledPanel(preferences.getLocalizedString(
          RoomPanel.class, "ceilingPanel.title"),
          new JComponent [][] {{this.ceilingVisibleCheckBox, null,
                                this.ceilingColorRadioButton, this.ceilingColorButton, 
                                this.ceilingTextureRadioButton, this.ceilingTextureComponent},
                                {this.ceilingMattRadioButton, this.ceilingShinyRadioButton}});
      add(ceilingPanel, new GridBagConstraints(
          1, 1, 1, 1, 1, 0, GridBagConstraints.NORTH,
          GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    }  
    if (this.wallSidesColorRadioButton != null || this.wallSidesMattRadioButton != null) {
      JPanel wallSidesPanel = createVerticalTitledPanel(preferences.getLocalizedString(
          RoomPanel.class, "wallSidesPanel.title"),
          new JComponent [][] {{this.splitSurroundingWallsCheckBox, null,
                               this.wallSidesColorRadioButton, this.wallSidesColorButton, 
                               this.wallSidesTextureRadioButton, this.wallSidesTextureComponent},
                               {this.wallSidesMattRadioButton, this.wallSidesShinyRadioButton}});
      add(wallSidesPanel, new GridBagConstraints(
          2, 1, 1, 1, 1, 0, GridBagConstraints.NORTH,
          GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    }
  }
  
  private JPanel createVerticalTitledPanel(String title, JComponent [][] componentGroups) {
    JPanel titledPanel = SwingTools.createTitledPanel(title);    
    
    int row = 0;
    for (int i = 0; i < componentGroups.length; i++) {
      JComponent [] components = componentGroups [i];
      for (int j = 0; j < components.length; j += 2) {
        int bottomInset = j < components.length - 2  ? 2  : 0;      
        JComponent component = components [j];
        JComponent nextComponent = components [j + 1];
        if (component != null) {
          if (nextComponent != null) {
            titledPanel.add(component, new GridBagConstraints(
                0, row, 1, 1, 1, 0, GridBagConstraints.LINE_START, 
                GridBagConstraints.NONE,  new Insets(0, 0, bottomInset, 5), 0, 0));
            titledPanel.add(nextComponent, new GridBagConstraints(
                1, row++, 1, 1, 1, 0, GridBagConstraints.LINE_START, 
                GridBagConstraints.HORIZONTAL, new Insets(0, 0, bottomInset, 0), 0, 0));
          } else {
            titledPanel.add(component, new GridBagConstraints(
                0, row++, 2, 1, 1, 0, GridBagConstraints.LINE_START, 
                GridBagConstraints.HORIZONTAL, new Insets(0, 0, bottomInset, 0), 0, 0));
          }
        }
      }
      
      if (i < componentGroups.length - 1) {
        // Add a separator between groups
        for (JComponent otherComponent : componentGroups [i + 1]) {
          if (otherComponent != null) {
            titledPanel.add(new JSeparator(), new GridBagConstraints(
                0, row++, 2, 1, 1, 0, GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL, new Insets(3, 0, 3, 0), 0, 0));
            break;
          }
        }
      }
    }
    
    return titledPanel;
  }
  
  /**
   * Displays this panel in a modal dialog box. 
   */
  public void displayView(View parentView) {
    if (SwingTools.showConfirmDialog((JComponent)parentView, 
            this, this.dialogTitle, this.nameTextField) == JOptionPane.OK_OPTION
        && this.controller != null) {
      this.controller.modifyRooms();
    }
  }
}
