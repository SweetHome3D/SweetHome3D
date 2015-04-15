/*
 * FontNameComboBox.java 30 mars 2015
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

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;

import com.eteks.sweethome3d.model.UserPreferences;

/**
 * A combo box used to choose the name of a font.
 * @author Emmanuel Puybaret
 */
public class FontNameComboBox extends JComboBox {
  public static final String DEFAULT_SYSTEM_FONT_NAME = "DEFAULT_SYSTEM_FONT_NAME";
  
  private static final String [] availableFontNames;
  
  static {
    availableFontNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    Arrays.sort(availableFontNames);
  }
  
  private UserPreferences preferences;
  private String          unavailableFontName;
  
  public FontNameComboBox(UserPreferences preferences) {
    this.preferences = preferences;
    DefaultComboBoxModel fontNamesModel = new DefaultComboBoxModel(availableFontNames);
    fontNamesModel.insertElementAt(DEFAULT_SYSTEM_FONT_NAME, 0);
    setModel(fontNamesModel);
    final String systemFontName = preferences.getLocalizedString(FontNameComboBox.class, "systemFontName");
    setRenderer(new DefaultListCellRenderer() {
        private Font rendererDefaultFont;
        private Font rendererSpecialFont;

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
          if (value == DEFAULT_SYSTEM_FONT_NAME) {
            value = systemFontName;
          } else if (value != null && Arrays.binarySearch(availableFontNames, value) < 0) {
            value = unavailableFontName;
          }
            
          super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
          
          if (this.rendererDefaultFont == null) {
            this.rendererDefaultFont = this.getFont();
            this.rendererSpecialFont = 
                new Font(this.rendererDefaultFont.getFontName(), Font.ITALIC, this.rendererDefaultFont.getSize());        
          }
          if (value == null
              || value == systemFontName
              || value == unavailableFontName) {
            setFont(this.rendererSpecialFont);
          } else {
            setFont(this.rendererDefaultFont);
          }
          return this;
        }
      });
  }
  
  @Override
  public void setSelectedItem(final Object item) {
    final DefaultComboBoxModel model = (DefaultComboBoxModel)getModel();
    Object firstItem = model.getElementAt(0);
    // If selected item is null, add null as first item
    if (firstItem != null && item == null) {
      if (firstItem != DEFAULT_SYSTEM_FONT_NAME) {
        model.removeElementAt(0);
      }
      model.insertElementAt(null, 0);
    // If selected item is not null, and first item is, remove first item
    } else if (firstItem == null && item != null) {
      // Remove item and reselect item later to make it work
      EventQueue.invokeLater(new Runnable() {
          public void run() {
            model.removeElementAt(0);
            FontNameComboBox.super.setSelectedItem(item);
          }
        });
    // If selected item is an unknown font, add it as first item
    } else if (firstItem != item 
               && Arrays.binarySearch(availableFontNames, item) < 0 
               && item != DEFAULT_SYSTEM_FONT_NAME) {
      if (firstItem != DEFAULT_SYSTEM_FONT_NAME) {
        model.removeElementAt(0);
      }
      model.insertElementAt(item, 0);
      this.unavailableFontName = preferences.getLocalizedString(FontNameComboBox.class, "unavailableFontName", item);
    }
    super.setSelectedItem(item);
  }
}
