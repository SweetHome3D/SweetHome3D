/*
 * UserPreferencesPanel.java 18 sept. 2006
 *
 * Copyright (c) 2006 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Locale;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.eteks.sweethome3d.j3d.Component3DManager;
import com.eteks.sweethome3d.model.LengthUnit;
import com.eteks.sweethome3d.model.TextureImage;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.UserPreferencesController;
import com.eteks.sweethome3d.viewcontroller.View;

/**
 * User preferences panel.
 * @author Emmanuel Puybaret
 */
public class UserPreferencesPanel extends JPanel implements DialogView {
  private final UserPreferencesController controller;
  private JLabel         languageLabel;
  private JComboBox      languageComboBox;
  private JLabel         unitLabel;
  private JRadioButton   centimeterRadioButton;
  private JRadioButton   inchRadioButton;
  private JRadioButton   millimeterRadioButton;
  private JRadioButton   meterRadioButton;
  private JLabel         furnitureCatalogViewLabel;
  private JRadioButton   treeRadioButton;
  private JRadioButton   listRadioButton;
  private JLabel         navigationPanelLabel;
  private JCheckBox      navigationPanelCheckBox;
  private JLabel         magnetismLabel;
  private JCheckBox      magnetismCheckBox;
  private JLabel         rulersLabel;
  private JCheckBox      rulersCheckBox;
  private JLabel         gridLabel;
  private JCheckBox      gridCheckBox;
  private JLabel         furnitureIconLabel;
  private JRadioButton   catalogIconRadioButton;
  private JRadioButton   topViewRadioButton;
  private JLabel         roomRenderingLabel;
  private JRadioButton   monochromeRadioButton;
  private JRadioButton   floorColorOrTextureRadioButton;
  private JLabel         wallPatternLabel;
  private JComboBox      wallPatternComboBox;
  private JLabel         newWallThicknessLabel;
  private JSpinner       newWallThicknessSpinner;
  private JLabel         newWallHeightLabel;
  private JSpinner       newWallHeightSpinner;
  private JButton        resetDisplayedActionTipsButton;
  private String         dialogTitle;
  
  /**
   * Creates a preferences panel that layouts the editable properties
   * of its <code>controller</code>. 
   */
  public UserPreferencesPanel(UserPreferences preferences,
                              UserPreferencesController controller) {
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
                                final UserPreferencesController controller) {
    if (controller.isPropertyEditable(UserPreferencesController.Property.LANGUAGE)) {
      // Create language label and combo box bound to controller LANGUAGE property
      this.languageLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, 
          UserPreferencesPanel.class, "languageLabel.text"));    
      this.languageComboBox = new JComboBox(new DefaultComboBoxModel(preferences.getSupportedLanguages()));
      this.languageComboBox.setRenderer(new DefaultListCellRenderer() {
          @Override
          public Component getListCellRendererComponent(JList list, 
              Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String language = (String)value;
            Locale locale;
            int underscoreIndex = language.indexOf("_");
            if (underscoreIndex != -1) {
              locale = new Locale(language.substring(0, underscoreIndex), 
                  language.substring(underscoreIndex + 1));
            } else {
              locale = new Locale(language);
            }
            String displayedValue = locale.getDisplayLanguage(locale);
            displayedValue = Character.toUpperCase(displayedValue.charAt(0)) + displayedValue.substring(1);
            if (underscoreIndex != -1) {
              displayedValue += " - " + locale.getDisplayCountry(locale); 
            }
            return super.getListCellRendererComponent(list, displayedValue, index, isSelected,
                cellHasFocus);
          }
        });
      this.languageComboBox.setMaximumRowCount(this.languageComboBox.getModel().getSize());
      this.languageComboBox.setSelectedItem(controller.getLanguage());
      this.languageComboBox.addItemListener(new ItemListener() {
          public void itemStateChanged(ItemEvent ev) {
            controller.setLanguage((String)languageComboBox.getSelectedItem());
          }
        });
      controller.addPropertyChangeListener(UserPreferencesController.Property.LANGUAGE, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              languageComboBox.setSelectedItem(controller.getLanguage());
            }
          });
    }
    
    if (controller.isPropertyEditable(UserPreferencesController.Property.UNIT)) {
      // Create unit label and radio buttons bound to controller UNIT property
      this.unitLabel = new JLabel(preferences.getLocalizedString(
          UserPreferencesPanel.class, "unitLabel.text"));
      this.centimeterRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences, 
          UserPreferencesPanel.class, "centimeterRadioButton.text"), 
          controller.getUnit() == LengthUnit.CENTIMETER);
      this.centimeterRadioButton.setActionCommand(LengthUnit.CENTIMETER.name());
      this.inchRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences, 
          UserPreferencesPanel.class, "inchRadioButton.text"), 
          controller.getUnit() == LengthUnit.INCH);
      this.inchRadioButton.setActionCommand(LengthUnit.INCH.name());
      this.millimeterRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences, 
          UserPreferencesPanel.class, "millimeterRadioButton.text"), 
          controller.getUnit() == LengthUnit.MILLIMETER);
      this.millimeterRadioButton.setActionCommand(LengthUnit.MILLIMETER.name());
      this.meterRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences, 
          UserPreferencesPanel.class, "meterRadioButton.text"), 
          controller.getUnit() == LengthUnit.METER);
      this.meterRadioButton.setActionCommand(LengthUnit.METER.name());
      final ButtonGroup unitButtonGroup = new ButtonGroup();
      unitButtonGroup.add(this.centimeterRadioButton);
      unitButtonGroup.add(this.inchRadioButton);
      unitButtonGroup.add(this.millimeterRadioButton);
      unitButtonGroup.add(this.meterRadioButton);

      ItemListener unitChangeListener = new ItemListener() {
          public void itemStateChanged(ItemEvent ev) {
            controller.setUnit(LengthUnit.valueOf(unitButtonGroup.getSelection().getActionCommand())); 
          }
        };
      this.centimeterRadioButton.addItemListener(unitChangeListener);
      this.inchRadioButton.addItemListener(unitChangeListener);
      this.millimeterRadioButton.addItemListener(unitChangeListener);
      this.meterRadioButton.addItemListener(unitChangeListener);
      controller.addPropertyChangeListener(UserPreferencesController.Property.UNIT, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              switch (controller.getUnit()) {
                case CENTIMETER :
                  centimeterRadioButton.setSelected(true);
                  break;
                case INCH :
                  inchRadioButton.setSelected(true);
                  break;
                case MILLIMETER :
                  millimeterRadioButton.setSelected(true);
                  break;
                case METER :
                  meterRadioButton.setSelected(true);
                  break;
              }
            }
          });
    }
    
    if (controller.isPropertyEditable(UserPreferencesController.Property.FURNITURE_CATALOG_VIEWED_IN_TREE)) {
      // Create furniture catalog label and radio buttons bound to controller FURNITURE_CATALOG_VIEWED_IN_TREE property
      this.furnitureCatalogViewLabel = new JLabel(preferences.getLocalizedString(
          UserPreferencesPanel.class, "furnitureCatalogViewLabel.text"));
      this.treeRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences, 
          UserPreferencesPanel.class, "treeRadioButton.text"), 
          controller.isFurnitureCatalogViewedInTree());
      this.listRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences, 
          UserPreferencesPanel.class, "listRadioButton.text"), 
          !controller.isFurnitureCatalogViewedInTree());
      ButtonGroup furnitureCatalogViewButtonGroup = new ButtonGroup();
      furnitureCatalogViewButtonGroup.add(this.treeRadioButton);
      furnitureCatalogViewButtonGroup.add(this.listRadioButton);
  
      ItemListener furnitureCatalogViewChangeListener = new ItemListener() {
          public void itemStateChanged(ItemEvent ev) {
            controller.setFurnitureCatalogViewedInTree(treeRadioButton.isSelected());
          }
        };
      this.treeRadioButton.addItemListener(furnitureCatalogViewChangeListener);
      this.listRadioButton.addItemListener(furnitureCatalogViewChangeListener);
      controller.addPropertyChangeListener(UserPreferencesController.Property.FURNITURE_CATALOG_VIEWED_IN_TREE, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              treeRadioButton.setSelected(controller.isFurnitureCatalogViewedInTree());
            }
          });
    }

    if (controller.isPropertyEditable(UserPreferencesController.Property.NAVIGATION_PANEL_VISIBLE)) {
      // Create navigation panel label and check box bound to controller NAVIGATION_PANEL_VISIBLE property
      this.navigationPanelLabel = new JLabel(preferences.getLocalizedString(
          UserPreferencesPanel.class, "navigationPanelLabel.text"));
      this.navigationPanelCheckBox = new JCheckBox(SwingTools.getLocalizedLabelText(preferences, 
          UserPreferencesPanel.class, "navigationPanelCheckBox.text"));
      if (!OperatingSystem.isMacOSX()
          || OperatingSystem.isMacOSXLeopardOrSuperior()) {
        this.navigationPanelCheckBox.setSelected(controller.isNavigationPanelVisible());
        this.navigationPanelCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ev) {
              controller.setNavigationPanelVisible(navigationPanelCheckBox.isSelected());
            }
          });
        controller.addPropertyChangeListener(UserPreferencesController.Property.NAVIGATION_PANEL_VISIBLE, 
            new PropertyChangeListener() {
              public void propertyChange(PropertyChangeEvent ev) {
                navigationPanelCheckBox.setSelected(controller.isNavigationPanelVisible());
              }
            });
      } else {
        // No support for navigation panel under Mac OS X Tiger (too unstable)
        this.navigationPanelCheckBox.setEnabled(false);
      }
    }

    if (controller.isPropertyEditable(UserPreferencesController.Property.MAGNETISM_ENABLED)) {
      // Create magnetism label and check box bound to controller MAGNETISM_ENABLED property
      this.magnetismLabel = new JLabel(preferences.getLocalizedString(
          UserPreferencesPanel.class, "magnetismLabel.text"));
      this.magnetismCheckBox = new JCheckBox(SwingTools.getLocalizedLabelText(preferences, 
          UserPreferencesPanel.class, "magnetismCheckBox.text"), controller.isMagnetismEnabled());
      this.magnetismCheckBox.addItemListener(new ItemListener() {
          public void itemStateChanged(ItemEvent ev) {
            controller.setMagnetismEnabled(magnetismCheckBox.isSelected());
          }
        });
      controller.addPropertyChangeListener(UserPreferencesController.Property.MAGNETISM_ENABLED, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              magnetismCheckBox.setSelected(controller.isMagnetismEnabled());
            }
          });
    }

    if (controller.isPropertyEditable(UserPreferencesController.Property.RULERS_VISIBLE)) {
      // Create rulers label and check box bound to controller RULERS_VISIBLE property
      this.rulersLabel = new JLabel(preferences.getLocalizedString(
          UserPreferencesPanel.class, "rulersLabel.text"));
      this.rulersCheckBox = new JCheckBox(SwingTools.getLocalizedLabelText(preferences, 
          UserPreferencesPanel.class, "rulersCheckBox.text"), controller.isRulersVisible());
      this.rulersCheckBox.addItemListener(new ItemListener() {
          public void itemStateChanged(ItemEvent ev) {
            controller.setRulersVisible(rulersCheckBox.isSelected());
          }
        });
      controller.addPropertyChangeListener(UserPreferencesController.Property.RULERS_VISIBLE, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              rulersCheckBox.setSelected(controller.isRulersVisible());
            }
          });
    }
    
    if (controller.isPropertyEditable(UserPreferencesController.Property.GRID_VISIBLE)) {
      // Create grid label and check box bound to controller GRID_VISIBLE property
      this.gridLabel = new JLabel(preferences.getLocalizedString(
          UserPreferencesPanel.class, "gridLabel.text"));
      this.gridCheckBox = new JCheckBox(SwingTools.getLocalizedLabelText(preferences, 
          UserPreferencesPanel.class, "gridCheckBox.text"), controller.isGridVisible());
      this.gridCheckBox.addItemListener(new ItemListener() {
          public void itemStateChanged(ItemEvent ev) {
            controller.setGridVisible(gridCheckBox.isSelected());
          }
        });
      controller.addPropertyChangeListener(UserPreferencesController.Property.GRID_VISIBLE, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              gridCheckBox.setSelected(controller.isGridVisible());
            }
          });
    }
    
    if (controller.isPropertyEditable(UserPreferencesController.Property.FURNITURE_VIEWED_FROM_TOP)) {
      // Create furniture appearance label and radio buttons bound to controller FURNITURE_VIEWED_FROM_TOP property
      this.furnitureIconLabel = new JLabel(preferences.getLocalizedString(
          UserPreferencesPanel.class, "furnitureIconLabel.text"));
      this.catalogIconRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences, 
          UserPreferencesPanel.class, "catalogIconRadioButton.text"), 
          !controller.isFurnitureViewedFromTop());
      this.topViewRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences, 
          UserPreferencesPanel.class, "topViewRadioButton.text"), 
          controller.isFurnitureViewedFromTop());
      if (Component3DManager.getInstance().isOffScreenImageSupported()) {
        ButtonGroup furnitureAppearanceButtonGroup = new ButtonGroup();
        furnitureAppearanceButtonGroup.add(this.catalogIconRadioButton);
        furnitureAppearanceButtonGroup.add(this.topViewRadioButton);
    
        ItemListener furnitureAppearanceChangeListener = new ItemListener() {
            public void itemStateChanged(ItemEvent ev) {
              controller.setFurnitureViewedFromTop(topViewRadioButton.isSelected());
            }
          };
        this.catalogIconRadioButton.addItemListener(furnitureAppearanceChangeListener);
        this.topViewRadioButton.addItemListener(furnitureAppearanceChangeListener);
        controller.addPropertyChangeListener(UserPreferencesController.Property.FURNITURE_VIEWED_FROM_TOP, 
            new PropertyChangeListener() {
              public void propertyChange(PropertyChangeEvent ev) {
                topViewRadioButton.setSelected(controller.isFurnitureViewedFromTop());
              }
            });
      } else {
        this.catalogIconRadioButton.setEnabled(false);
        this.topViewRadioButton.setEnabled(false);
      }
    }

    if (controller.isPropertyEditable(UserPreferencesController.Property.ROOM_FLOOR_COLORED_OR_TEXTURED)) {
      // Create room rendering label and radio buttons bound to controller ROOM_FLOOR_COLORED_OR_TEXTURED property
      this.roomRenderingLabel = new JLabel(preferences.getLocalizedString(
          UserPreferencesPanel.class, "roomRenderingLabel.text"));
      this.monochromeRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences, 
          UserPreferencesPanel.class, "monochromeRadioButton.text"), 
          !controller.isRoomFloorColoredOrTextured());
      this.floorColorOrTextureRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences, 
          UserPreferencesPanel.class, "floorColorOrTextureRadioButton.text"), 
          controller.isRoomFloorColoredOrTextured());
      ButtonGroup roomRenderingButtonGroup = new ButtonGroup();
      roomRenderingButtonGroup.add(this.monochromeRadioButton);
      roomRenderingButtonGroup.add(this.floorColorOrTextureRadioButton);
      ItemListener roomRenderingChangeListener = new ItemListener() {
          public void itemStateChanged(ItemEvent ev) {
            controller.setRoomFloorColoredOrTextured(floorColorOrTextureRadioButton.isSelected());
          }
        };
      this.monochromeRadioButton.addItemListener(roomRenderingChangeListener);
      this.floorColorOrTextureRadioButton.addItemListener(roomRenderingChangeListener);
      controller.addPropertyChangeListener(UserPreferencesController.Property.ROOM_FLOOR_COLORED_OR_TEXTURED, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              floorColorOrTextureRadioButton.setSelected(controller.isRoomFloorColoredOrTextured());
            }
          });
    }

    if (controller.isPropertyEditable(UserPreferencesController.Property.WALL_PATTERN)) {
      // Create wall pattern label and combo box bound to controller WALL_PATTERN property
      this.wallPatternLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, 
          UserPreferencesPanel.class, "wallPatternLabel.text"));    
      List<TextureImage> patterns = preferences.getPatternsCatalog().getPatterns();
      this.wallPatternComboBox = new JComboBox(new DefaultComboBoxModel(patterns.toArray()));
      this.wallPatternComboBox.setRenderer(new DefaultListCellRenderer() {
          @Override
          public Component getListCellRendererComponent(final JList list, 
              Object value, int index, boolean isSelected, boolean cellHasFocus) {
            TextureImage wallPattern = (TextureImage)value;
            final Component component = super.getListCellRendererComponent(
                list, "", index, isSelected, cellHasFocus);
            final BufferedImage patternImage = SwingTools.getPatternImage(
                wallPattern, list.getBackground(), list.getForeground());
            setIcon(new Icon() {
                public int getIconWidth() {
                  return patternImage.getWidth() * 4 + 1;
                }
          
                public int getIconHeight() {
                  return patternImage.getHeight() + 2;
                }
          
                public void paintIcon(Component c, Graphics g, int x, int y) {
                  Graphics2D g2D = (Graphics2D)g;
                  for (int i = 0; i < 4; i++) {
                    g2D.drawImage(patternImage, x + i * patternImage.getWidth(), y + 1, list);
                  }
                  g2D.setColor(list.getForeground());
                  g2D.drawRect(x, y, getIconWidth() - 2, getIconHeight() - 1);
                }
              });
            return component;
          }
        });
      this.wallPatternComboBox.setSelectedItem(controller.getWallPattern());
      this.wallPatternComboBox.addItemListener(new ItemListener() {
          public void itemStateChanged(ItemEvent ev) {
            controller.setWallPattern((TextureImage)wallPatternComboBox.getSelectedItem());
          }
        });
      controller.addPropertyChangeListener(UserPreferencesController.Property.WALL_PATTERN, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              wallPatternComboBox.setSelectedItem(controller.getWallPattern());
            }
          });
    }
    
    if (controller.isPropertyEditable(UserPreferencesController.Property.NEW_WALL_THICKNESS)) {
      // Create wall thickness label and spinner bound to controller NEW_WALL_THICKNESS property
      this.newWallThicknessLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, 
          UserPreferencesPanel.class, "newWallThicknessLabel.text"));
      final SpinnerLengthModel newWallThicknessSpinnerModel = new SpinnerLengthModel(
          0.5f, 0.125f, 5f, 0.005f, controller);
      this.newWallThicknessSpinner = new AutoCommitSpinner(newWallThicknessSpinnerModel);
      newWallThicknessSpinnerModel.setLength(controller.getNewWallThickness());
      newWallThicknessSpinnerModel.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            controller.setNewWallThickness(newWallThicknessSpinnerModel.getLength());
          }
        });
      controller.addPropertyChangeListener(UserPreferencesController.Property.NEW_WALL_THICKNESS, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              newWallThicknessSpinnerModel.setLength(controller.getNewWallThickness());
            }
          });
    }
    
    if (controller.isPropertyEditable(UserPreferencesController.Property.NEW_WALL_HEIGHT)) {
      // Create wall height label and spinner bound to controller NEW_WALL_HEIGHT property
      this.newWallHeightLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, 
          UserPreferencesPanel.class, "newWallHeightLabel.text"));
      final SpinnerLengthModel newWallHeightSpinnerModel = new SpinnerLengthModel(
          10f, 2f, 100f, 0.1f, controller);
      this.newWallHeightSpinner = new AutoCommitSpinner(newWallHeightSpinnerModel);
      newWallHeightSpinnerModel.setLength(controller.getNewWallHeight());
      newWallHeightSpinnerModel.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            controller.setNewWallHeight(newWallHeightSpinnerModel.getLength());
          }
        });
      controller.addPropertyChangeListener(UserPreferencesController.Property.NEW_WALL_HEIGHT, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              newWallHeightSpinnerModel.setLength(controller.getNewWallHeight());
            }
          });
    }
    
    this.resetDisplayedActionTipsButton = new JButton(new ResourceAction.ButtonAction(
        new ResourceAction(preferences, UserPreferencesPanel.class, "RESET_DISPLAYED_ACTION_TIPS", true) {
          @Override
          public void actionPerformed(ActionEvent ev) {
            controller.resetDisplayedActionTips();
          }
        }));
    
    this.dialogTitle = preferences.getLocalizedString(UserPreferencesPanel.class, "preferences.title");
  }
  
  /**
   * Sets components mnemonics and label / component associations.
   */
  private void setMnemonics(UserPreferences preferences) {
    if (!OperatingSystem.isMacOSX()) {
      if (this.languageLabel != null) {
        this.languageLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            UserPreferencesPanel.class, "languageLabel.mnemonic")).getKeyCode());
        this.languageLabel.setLabelFor(this.languageComboBox);
      }
      if (this.unitLabel != null) {
        this.centimeterRadioButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            UserPreferencesPanel.class, "centimeterRadioButton.mnemonic")).getKeyCode());
        this.inchRadioButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            UserPreferencesPanel.class, "inchRadioButton.mnemonic")).getKeyCode());
        this.millimeterRadioButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            UserPreferencesPanel.class, "millimeterRadioButton.mnemonic")).getKeyCode());
        this.meterRadioButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            UserPreferencesPanel.class, "meterRadioButton.mnemonic")).getKeyCode());
      }
      if (this.furnitureCatalogViewLabel != null) {
        this.treeRadioButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            UserPreferencesPanel.class, "treeRadioButton.mnemonic")).getKeyCode());
        this.listRadioButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            UserPreferencesPanel.class, "listRadioButton.mnemonic")).getKeyCode());
      }
      if (this.navigationPanelLabel != null) {
        this.navigationPanelCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            UserPreferencesPanel.class, "navigationPanelCheckBox.mnemonic")).getKeyCode());
      }
      if (this.magnetismLabel != null) {
        this.magnetismCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            UserPreferencesPanel.class, "magnetismCheckBox.mnemonic")).getKeyCode());
      }
      if (this.rulersLabel != null) {
        this.rulersCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            UserPreferencesPanel.class, "rulersCheckBox.mnemonic")).getKeyCode());
      }
      if (this.gridLabel != null) {
        this.gridCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            UserPreferencesPanel.class, "gridCheckBox.mnemonic")).getKeyCode());
      }
      if (this.furnitureIconLabel != null) {
        this.catalogIconRadioButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            UserPreferencesPanel.class, "catalogIconRadioButton.mnemonic")).getKeyCode());
        this.topViewRadioButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            UserPreferencesPanel.class, "topViewRadioButton.mnemonic")).getKeyCode());
      }
      if (this.roomRenderingLabel != null) {
        this.monochromeRadioButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            UserPreferencesPanel.class, "monochromeRadioButton.mnemonic")).getKeyCode());
        this.floorColorOrTextureRadioButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            UserPreferencesPanel.class, "floorColorOrTextureRadioButton.mnemonic")).getKeyCode());
      }
      if (this.wallPatternLabel != null) {
        this.wallPatternLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            UserPreferencesPanel.class, "wallPatternLabel.mnemonic")).getKeyCode());
        this.wallPatternLabel.setLabelFor(this.wallPatternComboBox);
      }
      if (this.newWallThicknessLabel != null) {
        this.newWallThicknessLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            UserPreferencesPanel.class, "newWallThicknessLabel.mnemonic")).getKeyCode());
        this.newWallThicknessLabel.setLabelFor(this.newWallThicknessSpinner);
      }
      if (this.newWallHeightLabel != null) {
        this.newWallHeightLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            UserPreferencesPanel.class, "newWallHeightLabel.mnemonic")).getKeyCode());
        this.newWallHeightLabel.setLabelFor(this.newWallHeightSpinner);
      }
    }
  }
  
  /**
   * Layouts panel components in panel with their labels. 
   */
  private void layoutComponents() {
    int labelAlignment = OperatingSystem.isMacOSX() 
        ? GridBagConstraints.LINE_END
        : GridBagConstraints.LINE_START;
    Insets labelInsets = new Insets(0, 0, 5, 5);
    Insets labelInsetsWithSpace = new Insets(0, 0, 10, 5);
    Insets rightComponentInsets = new Insets(0, 0, 5, 0);
    Insets rightComponentInsetsWithSpace = new Insets(0, 0, 10, 0);
    if (this.languageLabel != null) {
      // First row
      add(this.languageLabel, new GridBagConstraints(
          0, 0, 1, 1, 0, 0, labelAlignment, 
          GridBagConstraints.NONE, labelInsets, 0, 0));
      add(this.languageComboBox, new GridBagConstraints(
          1, 0, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
          GridBagConstraints.NONE, rightComponentInsets, 0, 0));
    }
    if (this.unitLabel != null) {
      // Second row
      add(this.unitLabel, new GridBagConstraints(
          0, 1, 1, 1, 0, 0, labelAlignment, 
          GridBagConstraints.NONE, labelInsets, 0, 0));
      add(this.centimeterRadioButton, new GridBagConstraints(
          1, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
          GridBagConstraints.NONE, new Insets(0, 0, 2, 5), 0, 0));
      add(this.inchRadioButton, new GridBagConstraints(
          2, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
          GridBagConstraints.NONE, new Insets(0, 0, 2, 0), 0, 0));
      // Third row
      add(this.millimeterRadioButton, new GridBagConstraints(
          1, 2, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
          GridBagConstraints.NONE, labelInsetsWithSpace, 0, 0));
      add(this.meterRadioButton, new GridBagConstraints(
          2, 2, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
          GridBagConstraints.NONE, rightComponentInsetsWithSpace, 0, 0));
    }
    if (this.furnitureCatalogViewLabel != null) {
      // Fourth row
      add(this.furnitureCatalogViewLabel, new GridBagConstraints(
          0, 3, 1, 1, 0, 0, labelAlignment, 
          GridBagConstraints.NONE, labelInsets, 0, 0));
      add(this.treeRadioButton, new GridBagConstraints(
          1, 3, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
          GridBagConstraints.NONE, labelInsets, 0, 0));
      add(this.listRadioButton, new GridBagConstraints(
          2, 3, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
          GridBagConstraints.NONE, rightComponentInsets, 0, 0));
    }
    if (this.navigationPanelLabel != null) {
      // Fifth row
      add(this.navigationPanelLabel, new GridBagConstraints(
          0, 4, 1, 1, 0, 0, labelAlignment, 
          GridBagConstraints.NONE, labelInsetsWithSpace, 0, 0));
      add(this.navigationPanelCheckBox, new GridBagConstraints(
          1, 4, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
          GridBagConstraints.NONE, rightComponentInsetsWithSpace, 0, 0));
    }
    if (this.magnetismLabel != null) {
      // Sixth row
      add(this.magnetismLabel, new GridBagConstraints(
          0, 5, 1, 1, 0, 0, labelAlignment, 
          GridBagConstraints.NONE, labelInsets, 0, 0));
      add(this.magnetismCheckBox, new GridBagConstraints(
          1, 5, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
          GridBagConstraints.NONE, rightComponentInsets, 0, 0));
    }
    if (this.rulersLabel != null) {
      // Seventh row
      add(this.rulersLabel, new GridBagConstraints(
          0, 6, 1, 1, 0, 0, labelAlignment, 
          GridBagConstraints.NONE, labelInsets, 0, 0));
      add(this.rulersCheckBox, new GridBagConstraints(
          1, 6, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
          GridBagConstraints.NONE, rightComponentInsets, 0, 0));
    }
    if (this.gridLabel != null) {
      // Eighth row
      add(this.gridLabel, new GridBagConstraints(
          0, 7, 1, 1, 0, 0, labelAlignment, 
          GridBagConstraints.NONE, labelInsets, 0, 0));
      add(this.gridCheckBox, new GridBagConstraints(
          1, 7, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
          GridBagConstraints.NONE, rightComponentInsets, 0, 0));
    }
    if (this.furnitureIconLabel != null) {
      // Ninth row
      add(this.furnitureIconLabel, new GridBagConstraints(
          0, 8, 1, 1, 0, 0, labelAlignment, 
          GridBagConstraints.NONE, labelInsets, 0, 0));
      add(this.catalogIconRadioButton, new GridBagConstraints(
          1, 8, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
          GridBagConstraints.NONE, labelInsets, 0, 0));
      add(this.topViewRadioButton, new GridBagConstraints(
          2, 8, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
          GridBagConstraints.NONE, rightComponentInsets , 0, 0));
    }
    if (this.roomRenderingLabel != null) {
      // Tenth row
      add(this.roomRenderingLabel, new GridBagConstraints(
          0, 9, 1, 1, 0, 0, labelAlignment, 
          GridBagConstraints.NONE, labelInsets, 0, 0));
      add(this.monochromeRadioButton, new GridBagConstraints(
          1, 9, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
          GridBagConstraints.NONE, labelInsets, 0, 0));
      add(this.floorColorOrTextureRadioButton, new GridBagConstraints(
          2, 9, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
          GridBagConstraints.NONE, rightComponentInsets , 0, 0));
    }
    if (this.wallPatternLabel != null) {
      // Eleventh row
      add(this.wallPatternLabel, new GridBagConstraints(
          0, 10, 1, 1, 0, 0, labelAlignment, 
          GridBagConstraints.NONE, labelInsets, 0, 0));
      add(this.wallPatternComboBox, new GridBagConstraints(
          1, 10, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
          GridBagConstraints.NONE, rightComponentInsets, 0, 0));
    }
    if (this.newWallThicknessLabel != null) {
      // Twelfth  row
      add(this.newWallThicknessLabel, new GridBagConstraints(
          0, 11, 1, 1, 0, 0, labelAlignment, 
          GridBagConstraints.NONE, labelInsets, 0, 0));
      add(this.newWallThicknessSpinner, new GridBagConstraints(
          1, 11, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
          GridBagConstraints.HORIZONTAL, rightComponentInsets, 0, 0));
    }
    if (this.newWallHeightLabel != null) {
      // Thirteenth  row
      add(this.newWallHeightLabel, new GridBagConstraints(
          0, 12, 1, 1, 0, 0, labelAlignment, 
          GridBagConstraints.NONE, labelInsets, 0, 0));
      add(this.newWallHeightSpinner, new GridBagConstraints(
          1, 12, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
          GridBagConstraints.HORIZONTAL, rightComponentInsets, 0, 0));
    }
    // Last row
    if (this.resetDisplayedActionTipsButton.getText() != null
        && this.resetDisplayedActionTipsButton.getText().length() > 0) {
      // Display reset button only if its text isn't empty 
      add(this.resetDisplayedActionTipsButton, new GridBagConstraints(
          0, 13, 3, 1, 0, 0, GridBagConstraints.CENTER, 
          GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    }
  }

  /**
   * Displays this panel in a dialog box. 
   */
  public void displayView(View parentView) {
    if (SwingTools.showConfirmDialog((JComponent)parentView, 
            this, this.dialogTitle, this.languageComboBox) == JOptionPane.OK_OPTION
        && this.controller != null) {
      this.controller.modifyUserPreferences();
    }
  }

  private static class SpinnerLengthModel extends SpinnerNumberModel {
    private LengthUnit unit = LengthUnit.CENTIMETER;

    public SpinnerLengthModel(final float centimeterStepSize, 
                              final float inchStepSize,
                              final float millimeterStepSize, 
                              final float meterStepSize, 
                              final UserPreferencesController controller) {
      // Invoke constructor that take objects in parameter to avoid any ambiguity
      super(new Float(1f), new Float(0f), new Float(100000f), new Float(centimeterStepSize));
      // Add a listener to convert value and step when unit changes 
      controller.addPropertyChangeListener(UserPreferencesController.Property.UNIT,
        new PropertyChangeListener () {
          public void propertyChange(PropertyChangeEvent ev) {
            updateStepsAndLength(centimeterStepSize, inchStepSize, millimeterStepSize, meterStepSize, controller);
          }
        });
      updateStepsAndLength(centimeterStepSize, inchStepSize, millimeterStepSize, meterStepSize, controller);
    }
    
    private void updateStepsAndLength(float centimeterStepSize, 
                                      float inchStepSize,
                                      float millimeterStepSize, 
                                      float meterStepSize,
                                      UserPreferencesController controller) {
      float lengthInCentimeter = getLength();
      unit = controller.getUnit();
      switch (controller.getUnit()) {
        case CENTIMETER :
          setStepSize(centimeterStepSize);
          break;
        case INCH :
          setStepSize(inchStepSize);
          break;
        case MILLIMETER :
          setStepSize(millimeterStepSize);
          break;
        case METER :
          setStepSize(meterStepSize);
          break;
      }
      setLength(lengthInCentimeter);
    }

    /**
     * Returns the displayed value in centimeter.
     */
    public float getLength() {
      return this.unit.unitToCentimeter(getNumber().floatValue());
    }

    /**
     * Sets the length in centimeter displayed in this model.
     */
    public void setLength(float length) {
      setValue(this.unit.centimeterToUnit(length));
    }
  }
}
