/*
 * TextureButton.java 05 oct. 2007
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

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.eteks.sweethome3d.model.CatalogTexture;
import com.eteks.sweethome3d.model.CollectionEvent;
import com.eteks.sweethome3d.model.CollectionListener;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.TextureImage;
import com.eteks.sweethome3d.model.TexturesCatalog;
import com.eteks.sweethome3d.model.TexturesCategory;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;

/**
 * Button displaying a texture as an icon. When the user clicks
 * on this button a dialog appears to let him choose an other texture.
 */
public class TextureChoiceComponent extends JButton {
  /**
   * Creates a texture button.
   */
  public TextureChoiceComponent(final String title, 
                       final UserPreferences preferences,
                       final TextureChoiceController controller) {
    JLabel dummyLabel = new JLabel("Text");
    Dimension iconDimension = dummyLabel.getPreferredSize();
    final int iconHeight = iconDimension.height;

    controller.addPropertyChangeListener(TextureChoiceController.Property.TEXTURE, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            repaint();
          }
        });
    
    setIcon(new Icon() {
        public int getIconWidth() {
          return iconHeight;
        }
  
        public int getIconHeight() {
          return iconHeight;
        }
  
        public void paintIcon(Component c, Graphics g, int x, int y) {
          g.setColor(Color.BLACK);
          g.drawRect(x + 2, y + 2, iconHeight - 5, iconHeight - 5);
          HomeTexture texture = controller.getTexture();
          if (texture != null) {
            Icon icon = IconManager.getInstance().getIcon(
                texture.getImage(), iconHeight - 6, TextureChoiceComponent.this);
            if (icon.getIconWidth() != icon.getIconHeight()) {
              Graphics2D g2D = (Graphics2D)g;
              AffineTransform previousTransform = g2D.getTransform();
              g2D.translate(x + 3, y + 3);
              g2D.scale((float)icon.getIconHeight() / icon.getIconWidth(), 1);
              icon.paintIcon(c, g2D, 0, 0);
              g2D.setTransform(previousTransform);
            } else {
              icon.paintIcon(c, g, x + 3, y + 3);
            }
          }
        }
      });
    
    // Add a listener to update texture
    addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        final TexturePanel texturePanel = new TexturePanel(preferences, controller);
        // Update edited texture in texture panel
        texturePanel.setPreviewTexture(controller.getTexture());
        // Show panel in a resizable modal dialog
        JOptionPane optionPane = new JOptionPane(texturePanel, JOptionPane.PLAIN_MESSAGE, 
            JOptionPane.OK_CANCEL_OPTION);
        final JDialog dialog = optionPane.createDialog(
            SwingUtilities.getRootPane(TextureChoiceComponent.this), title);
        dialog.applyComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));
        dialog.setResizable(true);
        // Pack again because resize decorations may have changed dialog preferred size
        dialog.pack();
        dialog.setMinimumSize(getPreferredSize());
        // Add a listener that transfer focus to focusable field of texture panel when dialog is shown
        dialog.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent ev) {
              KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent(texturePanel);
              dialog.removeComponentListener(this);
            }
          });
        dialog.setVisible(true);
        dialog.dispose();
        if (Integer.valueOf(JOptionPane.OK_OPTION).equals(optionPane.getValue())) {
          TextureImage selectedTexture = texturePanel.getPreviewTexture();
          if (selectedTexture instanceof HomeTexture
              || selectedTexture == null) {
            controller.setTexture((HomeTexture)selectedTexture);
          } else {
            controller.setTexture(new HomeTexture(selectedTexture));
          }
        }
      }
    });
  }

  /**
   * Displays a dialog that let user choose whether he wants to delete 
   * the selected texture from catalog or not.
   * @return <code>true</code> if user confirmed to delete.
   */
  public boolean confirmDeleteSelectedCatalogTexture() {
    // Retrieve displayed text in buttons and message
    ResourceBundle resource = ResourceBundle.getBundle(TextureChoiceComponent.class.getName());
    String message = resource.getString("confirmDeleteSelectedCatalogTexture.message");
    String title = resource.getString("confirmDeleteSelectedCatalogTexture.title");
    String delete = resource.getString("confirmDeleteSelectedCatalogTexture.delete");
    String cancel = resource.getString("confirmDeleteSelectedCatalogTexture.cancel");
    
    return JOptionPane.showOptionDialog(
        KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow(), message, title, 
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
        null, new Object [] {delete, cancel}, cancel) == JOptionPane.OK_OPTION;
  }

  /**
   * A panel that displays available textures in a list to let user make choose one. 
   */
  private static class TexturePanel extends JPanel {
    private static final int PREVIEW_ICON_HEIGHT = 64; 
    
    private TextureChoiceController controller;
    private ResourceBundle          resource;
    
    private TextureImage            previewTexture;
    private JLabel                  chosenTextureLabel;
    private JLabel                  texturePreviewLabel;
    private JLabel                  availableTexturesLabel;
    private JList                   availableTexturesList;
    private JButton                 importTextureButton;
    private JButton                 modifyTextureButton;
    private JButton                 deleteTextureButton;

    public TexturePanel(UserPreferences preferences, 
                        TextureChoiceController controller) {
      super(new GridBagLayout());
      this.controller = controller;
      this.resource = ResourceBundle.getBundle(TextureChoiceComponent.class.getName());
      createComponents(preferences);
      setMnemonics();
      layoutComponents();
    }

    /**
     * Creates and initializes components.
     */
    private void createComponents(final UserPreferences preferences) {
      this.availableTexturesLabel = new JLabel(this.resource.getString("availableTexturesLabel.text"));
      this.availableTexturesList = new JList(createListModel(preferences.getTexturesCatalog()));
      this.availableTexturesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      this.availableTexturesList.setCellRenderer(new DefaultListCellRenderer() {
          @Override
          public Component getListCellRendererComponent(final JList list, Object value, 
              int index, boolean isSelected, boolean cellHasFocus) {
            final CatalogTexture texture = (CatalogTexture)value;
            value = texture.getName();
            value = texture.getCategory().getName() + " - " + value;
            Component component = super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);
            setIcon(new Icon() {
                public int getIconWidth() {
                  return 16;
                }
          
                public int getIconHeight() {
                  return 16;
                }
          
                public void paintIcon(Component c, Graphics g, int x, int y) {
                  Icon icon = IconManager.getInstance().getIcon(
                      texture.getImage(), getIconHeight(), list);
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
            return component;
          }
        });
      this.availableTexturesList.getSelectionModel().addListSelectionListener(
          new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent ev) {
              CatalogTexture selectedTexture = (CatalogTexture)availableTexturesList.getSelectedValue();
              setPreviewTexture(selectedTexture);
              modifyTextureButton.setEnabled(selectedTexture != null && selectedTexture.isModifiable());
              deleteTextureButton.setEnabled(selectedTexture != null && selectedTexture.isModifiable());
            }
          });

      this.chosenTextureLabel = new JLabel(this.resource.getString("chosenTextureLabel.text"));
      this.texturePreviewLabel = new JLabel();
      this.texturePreviewLabel.setBorder(BorderFactory.createLoweredBevelBorder());
      // Add to label a transfer handler to let user drag and drop a file on it 
      this.texturePreviewLabel.setTransferHandler(new TransferHandler() {
          @Override
          public boolean canImport(JComponent comp, DataFlavor [] flavors) {
            return Arrays.asList(flavors).contains(DataFlavor.javaFileListFlavor);
          }
          
          @Override
          public boolean importData(JComponent comp, Transferable transferedFiles) {
            try {
              List<File> files = (List<File>)transferedFiles.getTransferData(DataFlavor.javaFileListFlavor);
              String textureName = files.get(0).getAbsolutePath();
              controller.importTexture(textureName);
              return true;
            } catch (UnsupportedFlavorException ex) {
              return false;
            } catch (IOException ex) {
              return false;
            }
          }
        });
      
      this.importTextureButton = new JButton(this.resource.getString("importTextureButton.text"));
      this.importTextureButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ev) {
            controller.importTexture();
          }
        });
      this.modifyTextureButton = new JButton(this.resource.getString("modifyTextureButton.text"));
      this.modifyTextureButton.setEnabled(false);
      this.modifyTextureButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ev) {
            controller.modifyTexture((CatalogTexture)availableTexturesList.getSelectedValue());
          }
        });    
      this.deleteTextureButton = new JButton(this.resource.getString("deleteTextureButton.text"));
      this.deleteTextureButton.setEnabled(false);
      this.deleteTextureButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ev) {
            controller.deleteTexture((CatalogTexture)availableTexturesList.getSelectedValue());
          }
        });
      
      preferences.getTexturesCatalog().addTexturesListener(new TexturesCatalogListener(this));
    }

    /**
     * Catalog listener that updates textures list each time a texture
     * is deleted or added in textures catalog. This listener is bound to this component
     * with a weak reference to avoid strong link between catalog and this component.  
     */
    private static class TexturesCatalogListener implements CollectionListener<CatalogTexture> {
      private WeakReference<TexturePanel> texturePanel;
      
      public TexturesCatalogListener(TexturePanel texturePanel) {
        this.texturePanel = new WeakReference<TexturePanel>(texturePanel);
      }
      
      public void collectionChanged(CollectionEvent<CatalogTexture> ev) {
        // If controller was garbage collected, remove this listener from catalog
        final TexturePanel texturePanel = this.texturePanel.get();
        if (texturePanel == null) {
          ((TexturesCatalog)ev.getSource()).removeTexturesListener(this);
        } else {
          texturePanel.availableTexturesList.setModel(
              texturePanel.createListModel((TexturesCatalog)ev.getSource()));
          switch (ev.getType()) {
            case ADD:
              texturePanel.availableTexturesList.setSelectedValue(ev.getItem(), true);
              break;
            case DELETE:
              texturePanel.availableTexturesList.clearSelection();
              break;
          }       
        }
      }
    }

    /**
     * Sets components mnemonics and label / component associations.
     */
    private void setMnemonics() {
      if (!OperatingSystem.isMacOSX()) {
        this.availableTexturesLabel.setDisplayedMnemonic(
            KeyStroke.getKeyStroke(this.resource.getString("availableTexturesLabel.mnemonic")).getKeyCode());
        this.availableTexturesLabel.setLabelFor(this.availableTexturesList);
        this.importTextureButton.setMnemonic(
            KeyStroke.getKeyStroke(this.resource.getString("importTextureButton.mnemonic")).getKeyCode());
        this.modifyTextureButton.setMnemonic(
            KeyStroke.getKeyStroke(this.resource.getString("modifyTextureButton.mnemonic")).getKeyCode());
        this.deleteTextureButton.setMnemonic(
            KeyStroke.getKeyStroke(this.resource.getString("deleteTextureButton.mnemonic")).getKeyCode());
      }
    }
    
    /**
     * Layouts components in panel with their labels. 
     */
    private void layoutComponents() {
      // First row
      add(this.availableTexturesLabel, new GridBagConstraints(
          0, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.NONE, new Insets(0, 0, 5, 15), 0, 0));
      add(this.chosenTextureLabel, new GridBagConstraints(
          1, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.NONE, new Insets(0, 0, 5, 0), 0, 0));
      // Second row
      add(new JScrollPane(this.availableTexturesList), new GridBagConstraints(
          0, 1, 1, 4, 1, 1, GridBagConstraints.CENTER,
          GridBagConstraints.BOTH, new Insets(0, 0, 5, 15), 0, 0));
      add(this.texturePreviewLabel, new GridBagConstraints(
          1, 1, 1, 1, 0, 0, GridBagConstraints.NORTH,
          GridBagConstraints.NONE, new Insets(0, 0, 10, 0), 0, 0));
      // Third row
      add(this.importTextureButton, new GridBagConstraints(
          1, 2, 1, 1, 0, 0, GridBagConstraints.NORTH,
          GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
      // Forth row
      add(this.modifyTextureButton, new GridBagConstraints(
          1, 3, 1, 1, 0, 0, GridBagConstraints.NORTH,
          GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
      // Fifth row
      add(this.deleteTextureButton, new GridBagConstraints(
          1, 4, 1, 1, 0, 0, GridBagConstraints.NORTH,
          GridBagConstraints.HORIZONTAL, new Insets(0, 0, 10, 0), 0, 0));
    }
    
    /**
     * Returns the chosen texture.
     */
    public TextureImage getPreviewTexture() {
      return this.previewTexture;
    }

    /**
     * Sets the chosen texture.
     */
    public void setPreviewTexture(TextureImage previewTexture) {
      this.previewTexture = previewTexture;
      if (previewTexture != null) {
        this.texturePreviewLabel.setIcon(
            IconManager.getInstance().getIcon(previewTexture.getImage(), PREVIEW_ICON_HEIGHT, this.texturePreviewLabel)); 
        this.texturePreviewLabel.revalidate();
      } else {
        // Preview a dummy empty icon
        this.texturePreviewLabel.setIcon(new Icon() {
          public int getIconHeight() {
            return PREVIEW_ICON_HEIGHT;
          }
          
          public int getIconWidth() {
            return PREVIEW_ICON_HEIGHT;
          }
          
          public void paintIcon(Component c, Graphics g, int x, int y) {
          }
        });
      }
      // Update selection in texture list
      this.availableTexturesList.setSelectedValue(previewTexture, true);
      if (this.availableTexturesList.getSelectedValue() != previewTexture) {
        int selectedIndex = this.availableTexturesList.getSelectedIndex();
        this.availableTexturesList.removeSelectionInterval(selectedIndex, selectedIndex);
      }
    }

    /**
     * Returns a list model from textures catalog.
     */
    private AbstractListModel createListModel(TexturesCatalog texturesCatalog) {
      final CatalogTexture [] textures = getTextures(texturesCatalog);
      return new AbstractListModel() {
          public Object getElementAt(int index) {
            return textures [index];
          }
    
          public int getSize() {
            return textures.length;
          }
        };
    }

    /**
     * Returns the array of textures in catalog.
     */
    private CatalogTexture [] getTextures(TexturesCatalog texturesCatalog) {
      List<CatalogTexture> textures = new ArrayList<CatalogTexture>();
      for (TexturesCategory category : texturesCatalog.getCategories()) {
        for (CatalogTexture texture : category.getTextures()) {
          textures.add(texture);
        }
      }
      return textures.toArray(new CatalogTexture [textures.size()]);
    }
  }
}