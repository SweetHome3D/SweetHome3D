/*
 * ModelMaterialsComponent.java 26 oct. 2012
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

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Locale;

import javax.media.j3d.BranchGroup;
import javax.swing.AbstractButton;
import javax.swing.AbstractListModel;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.eteks.sweethome3d.j3d.ModelManager;
import com.eteks.sweethome3d.model.HomeMaterial;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.ModelMaterialsController;
import com.eteks.sweethome3d.viewcontroller.TextureChoiceController;
import com.eteks.sweethome3d.viewcontroller.View;

/**
 * Button giving access to materials editor. When the user clicks
 * on this button a dialog appears to let him choose materials.
 */
public class ModelMaterialsComponent extends JButton implements View {
  /**
   * Creates a texture button.
   */
  public ModelMaterialsComponent(final UserPreferences preferences,
                                 final ModelMaterialsController controller) {
    setText(SwingTools.getLocalizedLabelText(preferences, ModelMaterialsComponent.class, "modifyButton.text"));
    if (!OperatingSystem.isMacOSX()) {
      setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          ModelMaterialsComponent.class, "modifyButton.mnemonic")).getKeyCode());
    }
    // Add a listener to update materials
    addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          final ModelMaterialsPanel texturePanel = new ModelMaterialsPanel(preferences, controller);
          texturePanel.displayView(ModelMaterialsComponent.this);
        }
      });
  }
  
  /**
   * A panel that displays available textures in a list to let user make choose one. 
   */
  private static class ModelMaterialsPanel extends JPanel {
    private final ModelMaterialsController controller;
    
    private JLabel                 previewLabel;
    private ModelPreviewComponent  previewComponent;
    private JLabel                 materialsLabel;
    private JList                  materialsList;
    private JLabel                 colorAndTextureLabel;
    private JRadioButton           defaultColorAndTextureRadioButton;
    private JRadioButton           colorRadioButton;
    private ColorButton            colorButton;
    private JRadioButton           textureRadioButton;
    private JComponent             textureComponent;
    private JLabel                 shininessLabel;
    private JSlider                shininessSlider;
    private PropertyChangeListener textureChangeListener;

    public ModelMaterialsPanel(UserPreferences preferences, 
                               ModelMaterialsController controller) {
      super(new GridBagLayout());
      this.controller = controller;
      createComponents(preferences, controller);
      setMnemonics(preferences);
      layoutComponents();
    }
  
    /**
     * Creates and initializes components.
     */
    private void createComponents(final UserPreferences preferences, 
                                  final ModelMaterialsController controller) {
      this.materialsLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, 
          ModelMaterialsComponent.class, "materialsLabel.text"));
      this.materialsList = new JList(new MaterialsListModel(controller));
      this.materialsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      this.materialsList.setCellRenderer(new MaterialListCellRenderer());
      
      this.previewLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, 
          ModelMaterialsComponent.class, "previewLabel.text"));
      this.previewComponent = new ModelPreviewComponent(true);
      this.previewComponent.setFocusable(false);
      ModelManager.getInstance().loadModel(controller.getModel(), new ModelManager.ModelObserver() {
          public void modelUpdated(BranchGroup modelRoot) {
            final MaterialsListModel materialsListModel = (MaterialsListModel)materialsList.getModel();
            previewComponent.setModel(controller.getModel());
            previewComponent.setModelRotationAndSize(controller.getModelRotation(),
                controller.getModelWidth(), controller.getModelDepth(), controller.getModelHeight());
            previewComponent.setBackFaceShown(controller.isBackFaceShown());
            previewComponent.setModelMaterials(materialsListModel.getMaterials());
            materialsListModel.addListDataListener(new ListDataListener() {
                public void contentsChanged(ListDataEvent ev) {
                  previewComponent.setModelMaterials(materialsListModel.getMaterials());
                }
                
                public void intervalRemoved(ListDataEvent ev) {
                }
                
                public void intervalAdded(ListDataEvent ev) {
                }
              });
          }
  
          public void modelError(Exception ex) {
            // Should happen only if model is missing
            previewLabel.setVisible(false); 
            previewComponent.setVisible(false);
          }
        });
      
      this.colorAndTextureLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, 
          ModelMaterialsComponent.class, "colorAndTextureLabel.text"));

      // Create color and texture image radio buttons properties
      this.defaultColorAndTextureRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences,
          ModelMaterialsComponent.class, "defaultColorAndTextureRadioButton.text"));
      final ChangeListener defaultChoiceChangeListener = new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            if (defaultColorAndTextureRadioButton.isEnabled() && defaultColorAndTextureRadioButton.isSelected()) {
              HomeMaterial material = (HomeMaterial)materialsList.getSelectedValue();
              ((MaterialsListModel)materialsList.getModel()).setMaterialAt(
                  new HomeMaterial(material.getName(), null, null, material.getShininess()),
                  materialsList.getSelectedIndex());
            }
          }
        };
      this.defaultColorAndTextureRadioButton.addChangeListener(defaultChoiceChangeListener);
      
      this.colorRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences, 
          ModelMaterialsComponent.class, "colorRadioButton.text"));
      final ChangeListener colorChoiceChangeListener = new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            if (colorRadioButton.isEnabled() && colorRadioButton.isSelected()) {
              HomeMaterial material = (HomeMaterial)materialsList.getSelectedValue();
              ((MaterialsListModel)materialsList.getModel()).setMaterialAt(
                  new HomeMaterial(material.getName(), colorButton.getColor(), null, material.getShininess()),
                  materialsList.getSelectedIndex());
            }
          }
        };
      this.colorRadioButton.addChangeListener(colorChoiceChangeListener);
      this.colorButton = new ColorButton(preferences);
      this.colorButton.setColorDialogTitle(preferences.getLocalizedString(ModelMaterialsComponent.class, "colorDialog.title"));
      final PropertyChangeListener colorChangeListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            if (!colorRadioButton.isSelected()) {
              colorRadioButton.setSelected(true);
            } else {
              colorChoiceChangeListener.stateChanged(null);
            }
          }
        };
      this.colorButton.addPropertyChangeListener(ColorButton.COLOR_PROPERTY, colorChangeListener);
      
      final TextureChoiceController textureController = controller.getTextureController();
      this.textureRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences,
          ModelMaterialsComponent.class, "textureRadioButton.text"));
      final ChangeListener textureChoiceChangeListener = new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            if (textureRadioButton.isEnabled() && textureRadioButton.isSelected()) {
              HomeMaterial material = (HomeMaterial)materialsList.getSelectedValue();
              ((MaterialsListModel)materialsList.getModel()).setMaterialAt(
                  new HomeMaterial(material.getName(), null, textureController.getTexture(), material.getShininess()),
                  materialsList.getSelectedIndex());
            }
          }
        };
      this.textureRadioButton.addChangeListener(textureChoiceChangeListener);
      this.textureComponent = (JComponent)textureController.getView();
      this.textureChangeListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            if (!textureRadioButton.isSelected()) {
              textureRadioButton.setSelected(true);
            } else {
              textureChoiceChangeListener.stateChanged(null);
            }
          }
        };
      // Listener added and removed in displayView()
        
      ButtonGroup buttonGroup = new ButtonGroup();
      buttonGroup.add(this.defaultColorAndTextureRadioButton);
      buttonGroup.add(this.colorRadioButton);
      buttonGroup.add(this.textureRadioButton);
      
      this.shininessLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, 
          ModelMaterialsComponent.class, "shininessLabel.text"));
      this.shininessSlider = new JSlider(0, 128);
      JLabel mattLabel = new JLabel(preferences.getLocalizedString(
          ModelMaterialsComponent.class, "mattLabel.text"));
      JLabel shinyLabel = new JLabel(preferences.getLocalizedString(
          ModelMaterialsComponent.class, "shinyLabel.text"));
      Dictionary<Integer,JComponent> shininessSliderLabelTable = new Hashtable<Integer,JComponent>();
      shininessSliderLabelTable.put(0, mattLabel);
      shininessSliderLabelTable.put(128, shinyLabel);
      this.shininessSlider.setLabelTable(shininessSliderLabelTable);
      this.shininessSlider.setPaintLabels(true);
      this.shininessSlider.setPaintTicks(true);
      this.shininessSlider.setMajorTickSpacing(16);
      final ChangeListener shininessChangeListener = new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            HomeMaterial material = (HomeMaterial)materialsList.getSelectedValue();
            int shininess = shininessSlider.getValue();
            ((MaterialsListModel)materialsList.getModel()).setMaterialAt(
                new HomeMaterial(material.getName(), material.getColor(), material.getTexture(), shininess / 128f),
                materialsList.getSelectedIndex());
          }
        };
      this.shininessSlider.addChangeListener(shininessChangeListener);

      this.materialsList.getSelectionModel().addListSelectionListener(
          new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent ev) {
              if (!materialsList.isSelectionEmpty()) {
                defaultColorAndTextureRadioButton.removeChangeListener(defaultChoiceChangeListener);
                colorRadioButton.removeChangeListener(colorChoiceChangeListener);
                textureRadioButton.removeChangeListener(textureChoiceChangeListener);
                colorButton.removePropertyChangeListener(ColorButton.COLOR_PROPERTY, colorChangeListener);
                if (((JComponent)textureController.getView()).isShowing()) {
                  // Remove listener only if its texture component is shown because its listener is added later
                  textureController.removePropertyChangeListener(TextureChoiceController.Property.TEXTURE, textureChangeListener);
                }
                shininessSlider.removeChangeListener(shininessChangeListener);
                
                HomeMaterial material = (HomeMaterial)materialsList.getSelectedValue();              
                HomeTexture texture = material.getTexture();
                Integer color = material.getColor();
                Float shininess = material.getShininess();
                HomeMaterial defaultMaterial = ((MaterialsListModel)materialsList.getModel()).getDefaultMaterialAt(materialsList.getSelectedIndex());
                if (color == null && texture == null) {
                  defaultColorAndTextureRadioButton.setSelected(true);
                  // Display default color or texture in buttons
                  texture = defaultMaterial.getTexture();
                  if (texture != null) {
                    colorButton.setColor(null);
                    controller.getTextureController().setTexture(texture);
                  } else {
                    color = defaultMaterial.getColor();
                    if (color != null) {
                      textureController.setTexture(null);
                      colorButton.setColor(color);
                    }
                  }
                } else if (texture != null) {
                  textureRadioButton.setSelected(true);
                  colorButton.setColor(null);
                  textureController.setTexture(texture);
                } else {
                  colorRadioButton.setSelected(true);
                  textureController.setTexture(null);
                  colorButton.setColor(color);
                }         

                if (shininess != null) {
                  shininessSlider.setValue((int)(shininess * 128));
                } else {
                  shininessSlider.setValue((int)(defaultMaterial.getShininess() * 128));
                }
                
                defaultColorAndTextureRadioButton.addChangeListener(defaultChoiceChangeListener);
                colorRadioButton.addChangeListener(colorChoiceChangeListener);
                textureRadioButton.addChangeListener(textureChoiceChangeListener);
                colorButton.addPropertyChangeListener(ColorButton.COLOR_PROPERTY, colorChangeListener);
                if (((JComponent)textureController.getView()).isShowing()) {
                  textureController.addPropertyChangeListener(TextureChoiceController.Property.TEXTURE, textureChangeListener);
                }
                shininessSlider.addChangeListener(shininessChangeListener);
              }
              enableComponents();
            }
          });
      
      this.materialsList.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent ev) {
          // Open color or texture choice dialogs on double clicks
          if (ev.getClickCount() == 2 && !materialsList.isSelectionEmpty()) {
            if (colorButton.getColor() != null) {
              colorButton.doClick(200);
            } else if (controller.getTextureController().getTexture() != null
                       && textureComponent instanceof AbstractButton) {
              ((AbstractButton)textureComponent).doClick(200);
            }
          }
        }
      });

      if (this.materialsList.getModel().getSize() > 0) {
        this.materialsList.setSelectedIndex(0);
      } else {
        // Add a listener that will select first row as soon as the list contains some data      
        this.materialsList.getModel().addListDataListener(new ListDataListener() {
            public void intervalAdded(ListDataEvent ev) {     
              materialsList.setSelectedIndex(0);
              ((ListModel)ev.getSource()).removeListDataListener(this);
            }
            
            public void contentsChanged(ListDataEvent ev) {
              intervalAdded(ev);
            }
            
            public void intervalRemoved(ListDataEvent ev) {
            }          
          });
      }

      enableComponents();
    }
  
    /**
     * Renderer used to display the materials in list. 
     */
    private class MaterialListCellRenderer extends DefaultListCellRenderer {
      private Font defaultFont;
      private Icon emptyIcon = new Icon() {
          public void paintIcon(Component c, Graphics g, int x, int y) {
          }
  
          public int getIconHeight() {
            return defaultFont.getSize();
          }
    
          public int getIconWidth() {
            return getIconHeight();
          }
        };
      
      @Override
      public Component getListCellRendererComponent(final JList list, Object value, int index, 
                                                    boolean isSelected, boolean cellHasFocus) {
        // Initialize fonts if not done
        if (this.defaultFont == null) {
          this.defaultFont = getFont();
        }
        HomeMaterial material = (HomeMaterial)value;
        super.getListCellRendererComponent(list, material.getName(), index, isSelected, cellHasFocus);        
        HomeTexture materialTexture = material.getTexture();
        Integer materialColor = material.getColor();
        HomeMaterial defaultMaterial = ((MaterialsListModel)materialsList.getModel()).getDefaultMaterialAt(index);
        if (materialTexture == null && materialColor == null) {
          materialTexture = defaultMaterial.getTexture();
          if (materialTexture == null) {
            materialColor = defaultMaterial.getColor();
          }
        }
        if (materialTexture != null) {
          // Display material texture image with an icon  
          final HomeTexture texture = materialTexture;
          setIcon(new Icon() {
              public int getIconHeight() {
                return defaultFont.getSize();
              }
        
              public int getIconWidth() {
                return getIconHeight();
              }
              
              public void paintIcon(Component c, Graphics g, int x, int y) {
                Icon icon = IconManager.getInstance().getIcon(texture.getImage(), getIconHeight(), list);
                if (icon.getIconWidth() != icon.getIconHeight()) {
                  Graphics2D g2D = (Graphics2D)g;
                  AffineTransform previousTransform = g2D.getTransform();
                  g2D.translate(x, y);
                  g2D.scale((float)icon.getIconHeight() / icon.getIconWidth(), 1);
                  icon.paintIcon(c, g2D, 0, 0);
                  g2D.setTransform(previousTransform);
                } else {
                  icon.paintIcon(c, g, x, y);
                }
              }
            });          
        } else if (materialColor != null) {
          // Display material color with an icon  
          final Color color = new Color(materialColor);
          setIcon(new Icon () {
              public int getIconHeight() {
                return getFont().getSize();
              }
      
              public int getIconWidth() {
                return getIconHeight();
              }
      
              public void paintIcon(Component c, Graphics g, int x, int y) {
                int squareSize = getIconHeight();
                g.setColor(color);          
                g.fillRect(x + 2, y + 2, squareSize - 3, squareSize - 3);
                g.setColor(c.getParent().getParent().getForeground());
                g.drawRect(x + 1, y + 1, squareSize - 2, squareSize - 2);
              }
            });
        } else {
          setIcon(this.emptyIcon);
        }
        return this;
      }
    }
  
    /**
     * Enables editing components according to current selection in materials list.
     */
    private void enableComponents() {
      boolean selectionEmpty = this.materialsList.isSelectionEmpty();
      defaultColorAndTextureRadioButton.setEnabled(!selectionEmpty);
      textureRadioButton.setEnabled(!selectionEmpty);
      textureComponent.setEnabled(!selectionEmpty);
      colorRadioButton.setEnabled(!selectionEmpty);
      colorButton.setEnabled(!selectionEmpty);
      shininessSlider.setEnabled(!selectionEmpty);
    }

    /**
     * Sets components mnemonics and label / component associations.
     */
    private void setMnemonics(UserPreferences preferences) {
      if (!OperatingSystem.isMacOSX()) {
        this.materialsLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            ModelMaterialsComponent.class, "materialsLabel.mnemonic")).getKeyCode());
        this.materialsLabel.setLabelFor(this.materialsList);
        this.defaultColorAndTextureRadioButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            ModelMaterialsComponent.class, "defaultColorAndTextureRadioButton.mnemonic")).getKeyCode());
        this.colorRadioButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            ModelMaterialsComponent.class, "colorRadioButton.mnemonic")).getKeyCode());
        this.textureRadioButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            ModelMaterialsComponent.class, "textureRadioButton.mnemonic")).getKeyCode());
        this.shininessLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
            ModelMaterialsComponent.class, "shininessLabel.mnemonic")).getKeyCode());
        this.shininessLabel.setLabelFor(this.shininessSlider);
      }
    }
    
    /**
     * Layouts components in panel with their labels. 
     */
    private void layoutComponents() {
      // Preview
      add(this.previewLabel, new GridBagConstraints(
          0, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.NONE, new Insets(0, 0, 5, 10), 0, 0));
      this.previewComponent.setPreferredSize(new Dimension(150, 150));
      add(this.previewComponent, new GridBagConstraints(
          0, 1, 1, 5, 0, 0, GridBagConstraints.NORTH,
          GridBagConstraints.NONE, new Insets(2, 0, 0, 15), 0, 0));
      
      // Materials list
      add(this.materialsLabel, new GridBagConstraints(
          1, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.NONE, new Insets(0, 0, 5, 15), 0, 0));
      JScrollPane scrollPane = new JScrollPane(this.materialsList);
      Dimension preferredSize = scrollPane.getPreferredSize();
      scrollPane.setPreferredSize(new Dimension(Math.min(150, preferredSize.width), preferredSize.height));
      add(scrollPane, new GridBagConstraints(
          1, 1, 1, 5, 1, 1, GridBagConstraints.CENTER,
          GridBagConstraints.BOTH, new Insets(0, 0, 0, 15), 0, 0));
      SwingTools.installFocusBorder(this.materialsList);
      
      // Color and Texture 
      add(this.colorAndTextureLabel, new GridBagConstraints(
          2, 0, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
          GridBagConstraints.NONE, new Insets(0, 0, 5, 0), 0, 0));
      add(this.defaultColorAndTextureRadioButton, new GridBagConstraints(
          2, 1, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
          GridBagConstraints.NONE, new Insets(0, 0, 5, 0), 0, 0));
      add(this.colorRadioButton, new GridBagConstraints(
          2, 2, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
          GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
      add(this.colorButton, new GridBagConstraints(
          3, 2, 1, 1, 0, 0, GridBagConstraints.CENTER, 
          GridBagConstraints.NONE, new Insets(0, 0, 5, 0), 0, 0));
      add(this.textureRadioButton, new GridBagConstraints(
          2, 3, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
          GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
      add(this.textureComponent, new GridBagConstraints(
          3, 3, 1, 1, 0, 0, GridBagConstraints.CENTER, 
          GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
      this.textureComponent.setPreferredSize(this.colorButton.getPreferredSize());

      // Shininess 
      add(this.shininessLabel, new GridBagConstraints(
          2, 4, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
          GridBagConstraints.NONE, new Insets(15, 0, 5, 0), 0, 0));
      add(this.shininessSlider, new GridBagConstraints(
          2, 5, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
          GridBagConstraints.NONE, new Insets(0, 0, 5, 0), -20, 0));
    }
    
    public void displayView(View parent) {
      // Show panel in a resizable modal dialog
      final JOptionPane optionPane = new JOptionPane(this, JOptionPane.PLAIN_MESSAGE, 
          JOptionPane.OK_CANCEL_OPTION);
      JComponent parentComponent = SwingUtilities.getRootPane((JComponent)parent);
      if (parentComponent != null) {
        optionPane.setComponentOrientation(parentComponent.getComponentOrientation());
      }
      final JDialog dialog = optionPane.createDialog(parentComponent, controller.getDialogTitle());
      dialog.applyComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));
      dialog.setResizable(true);
      // Pack again because resize decorations may have changed dialog preferred size
      dialog.pack();
      dialog.setMinimumSize(getPreferredSize());
      this.controller.getTextureController().addPropertyChangeListener(
          TextureChoiceController.Property.TEXTURE, this.textureChangeListener);
      // Add a listener that transfer focus to focusable field of texture panel when dialog is shown
      dialog.addComponentListener(new ComponentAdapter() {
          @Override
          public void componentShown(ComponentEvent ev) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent(ModelMaterialsPanel.this);
            dialog.removeComponentListener(this);
          }
        });
      dialog.setVisible(true);
      dialog.dispose();
      this.controller.getTextureController().removePropertyChangeListener(
          TextureChoiceController.Property.TEXTURE, this.textureChangeListener);
      if (Integer.valueOf(JOptionPane.OK_OPTION).equals(optionPane.getValue())) {
        this.controller.setMaterials(((MaterialsListModel)this.materialsList.getModel()).getMaterials());
      }
    }
 
    
    /**
     * Model for materials list.
     */
    private static class MaterialsListModel extends AbstractListModel {
      private HomeMaterial [] defaultMaterials;
      private HomeMaterial [] materials;

      public MaterialsListModel(ModelMaterialsController controller) {
        this.materials = controller.getMaterials();
        ModelManager.getInstance().loadModel(controller.getModel(), 
          new ModelManager.ModelObserver() {
            public void modelUpdated(BranchGroup modelRoot) {
              defaultMaterials = ModelManager.getInstance().getMaterials(modelRoot);
              fireContentsChanged(MaterialsListModel.this, 0, defaultMaterials.length);
            }

            public void modelError(Exception ex) {
              // Let the list be empty  
            }
          });
      }

      public Object getElementAt(int index) {
        if (this.materials != null
            && this.materials [index] != null) {
          return this.materials [index];
        } else {
          return new HomeMaterial(this.defaultMaterials [index].getName(), null, null, null); 
        }
      }

      public int getSize() {
        if (this.defaultMaterials != null) {
          return this.defaultMaterials.length;
        } else {
          return 0;
        }
      }
      
      public HomeMaterial getDefaultMaterialAt(int index) {
        return this.defaultMaterials [index];
      }

      /**
       * Sets the material at the given <code>index</code>.
       */
      public void setMaterialAt(HomeMaterial material, int index) {
        if (this.materials != null
            && material.getColor() == null
            && material.getTexture() == null
            && material.getShininess() == null) {
          this.materials [index] = null;
          boolean containsOnlyNull = true;
          for (HomeMaterial m : this.materials) {
            if (m != null) {
              containsOnlyNull = false;
              break;
            }
          }
          if (containsOnlyNull) {
            this.materials = null;
          }
        } else {
          if (this.materials == null || this.materials.length != this.defaultMaterials.length) {
            this.materials = new HomeMaterial [this.defaultMaterials.length];
          }
          this.materials [index] = material;
        }
        fireContentsChanged(this, index, index);
      }

      /**
       * Returns the edited materials. 
       */
      public HomeMaterial [] getMaterials() {
        return this.materials;
      }
    }
  }
}