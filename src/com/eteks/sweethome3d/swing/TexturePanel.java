/*
 * TexturePanel.java 05 oct. 2007
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
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.eteks.sweethome3d.model.CatalogTexture;
import com.eteks.sweethome3d.model.TextureImage;
import com.eteks.sweethome3d.model.TexturesCategory;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * A panel that displays available textures in a list to let user make choose one. 
 */
class TexturePanel extends JPanel {
  private static final int PREVIEW_ICON_HEIGHT = 64; 
  
  private TextureImage texture;
  private JLabel       chosenTextureLabel;
  private JLabel       texturePreviewLabel;
  private JLabel       availableTexturesLabel;
  private JList        availableTexturesList;

  public TexturePanel(UserPreferences preferences) {
    super(new GridBagLayout());
    createComponents(preferences);
    updateComponents(preferences);
    layoutComponents();
    preferences.addPropertyChangeListener(UserPreferences.Property.LANGUAGE, 
        new LanguageChangeListener(this));
  }

  /**
   * Creates and initializes components.
   */
  private void createComponents(UserPreferences preferences) {
    this.availableTexturesLabel = new JLabel();
    this.availableTexturesList = new JList();
    this.availableTexturesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    this.availableTexturesList.setCellRenderer(new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, 
            int index, boolean isSelected, boolean cellHasFocus) {
          CatalogTexture texture = (CatalogTexture)value;
          setIcon(IconManager.getInstance().getIcon(texture.getImage(), 16, list));
          value = texture.getName();
          value = texture.getCategory().getName() + " - " + value;
          Component component = super.getListCellRendererComponent(
              list, value, index, isSelected, cellHasFocus);
          setIcon(IconManager.getInstance().getIcon(texture.getImage(), 16, list));
          return component;
        }
      });
    this.availableTexturesList.getSelectionModel().addListSelectionListener(
        new ListSelectionListener() {
          public void valueChanged(ListSelectionEvent ev) {
            setTexture((TextureImage)availableTexturesList.getSelectedValue());
          }
        });

    this.chosenTextureLabel = new JLabel();
    this.texturePreviewLabel = new JLabel();
    this.texturePreviewLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
  }

  /**
   * Layouts composants in panel with their labels. 
   */
  private void layoutComponents() {
    // First row
    add(this.availableTexturesLabel, new GridBagConstraints(
        0, 0, 1, 1, 0, 0, GridBagConstraints.WEST,
        GridBagConstraints.NONE, new Insets(0, 0, 5, 15), 0, 0));
    add(this.chosenTextureLabel, new GridBagConstraints(
        1, 0, 1, 1, 0, 0, GridBagConstraints.WEST,
        GridBagConstraints.NONE, new Insets(0, 0, 5, 0), 0, 0));
    // Last row
    add(new JScrollPane(this.availableTexturesList), new GridBagConstraints(
        0, 1, 1, 1, 1, 1, GridBagConstraints.CENTER,
        GridBagConstraints.BOTH, new Insets(0, 0, 5, 15), 0, 0));
    add(this.texturePreviewLabel, new GridBagConstraints(
        1, 1, 1, 1, 0, 0, GridBagConstraints.NORTH,
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
  }
  
  /**
   * Updates labels text and textures list content.
   */
  private void updateComponents(UserPreferences preferences) {
    ResourceBundle resource = ResourceBundle.getBundle(TexturePanel.class.getName());
    this.chosenTextureLabel.setText(resource.getString("chosenTextureLabel.text"));
    this.availableTexturesLabel.setText(resource.getString("availableTexturesLabel.text"));
    // Change list model created from textures list
    final CatalogTexture[] textures = getTextures(preferences);
    this.availableTexturesList.setModel(new AbstractListModel() {
        public Object getElementAt(int index) {
          return textures [index];
        }
  
        public int getSize() {
          return textures.length;
        }
      });
  }

  /**
   * Returns the chosen texture.
   */
  public TextureImage getTexture() {
    return this.texture;
  }

  /**
   * Sets the chosen texture
   */
  public void setTexture(TextureImage texture) {
    this.texture = texture;
    if (texture != null) {
      this.texturePreviewLabel.setIcon(
          IconManager.getInstance().getIcon(texture.getImage(), PREVIEW_ICON_HEIGHT, this.texturePreviewLabel)); 
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
    this.availableTexturesList.setSelectedValue(texture, true);
    if (this.availableTexturesList.getSelectedValue() != texture) {
      int selectedIndex = this.availableTexturesList.getSelectedIndex();
      this.availableTexturesList.removeSelectionInterval(selectedIndex, selectedIndex);
    }
  }
  
  /**
   * Returns the array of textures in catalog.
   */
  private CatalogTexture[] getTextures(UserPreferences preferences) {
    List<CatalogTexture> textures = new ArrayList<CatalogTexture>();
    for (TexturesCategory category : preferences.getTexturesCatalog().getCategories()) {
      for (CatalogTexture texture : category.getTextures()) {
        textures.add(texture);
      }
    }
    return textures.toArray(new CatalogTexture [textures.size()]);
  }

  /**
   * Preferences property listener that updates texture panel when language changes.  
   */
  private static class LanguageChangeListener implements PropertyChangeListener {
    private WeakReference<TexturePanel>  texturePanel;

    public LanguageChangeListener(TexturePanel planComponent) {
      this.texturePanel = new WeakReference<TexturePanel>(planComponent);
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
      // If texture panel was garbage collected, remove this listener from preferences
      TexturePanel texturePanel = this.texturePanel.get();
      if (texturePanel == null) {
        ((UserPreferences)ev.getSource()).removePropertyChangeListener(
            UserPreferences.Property.LANGUAGE, this);
      } else {
        texturePanel.updateComponents((UserPreferences)ev.getSource());
      }
    }
  }
}