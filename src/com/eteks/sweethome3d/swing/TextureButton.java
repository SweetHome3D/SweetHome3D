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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.util.Locale;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.eteks.sweethome3d.model.TextureImage;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * Button displaying a texture as an icon. When the user clicks
 * on this button a dialog appears to let him choose an other texture.
 */
public class TextureButton extends JButton {
  private TextureImage texture;
  private String       textureDialogTitle;

  /**
   * Creates a texture button.
   */
  public TextureButton(final UserPreferences preferences) {
    JLabel dummyLabel = new JLabel("Text");
    Dimension iconDimension = dummyLabel.getPreferredSize();
    final int iconHeight = iconDimension.height;

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
          if (texture != null) {
            Icon icon = IconManager.getInstance().getIcon(
                texture.getImage(), iconHeight - 6, TextureButton.this);
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
        TexturePanel texturePanel = new TexturePanel(preferences);
        // Update edited texture in texture panel
        texturePanel.setTexture(texture);
        // Show panel in a resizable modal dialog
        JOptionPane optionPane = new JOptionPane(texturePanel, JOptionPane.PLAIN_MESSAGE, 
            JOptionPane.OK_CANCEL_OPTION);
        JDialog dialog = optionPane.createDialog(
            SwingUtilities.getRootPane(TextureButton.this), textureDialogTitle);
        dialog.applyComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));
        dialog.setMinimumSize(getPreferredSize());
        dialog.setResizable(true);
        dialog.setVisible(true);
        dialog.dispose();
        if (Integer.valueOf(JOptionPane.OK_OPTION).equals(optionPane.getValue())) {
          setTexture(texturePanel.getTexture());
        }
      }
    });
  }

  /**
   * Returns the texture displayed by this button.
   * @return the texture of this button or <code>null</code>.
   */
  public TextureImage getTexture() {
    return this.texture;
  }

  /**
   * Sets the texture displayed by this button.
   * @param texture the texture or <code>null</code>.
   */
  public void setTexture(TextureImage texture) {
    if (texture != this.texture
        || (texture != null && !texture.equals(this.texture))) {
      TextureImage oldTexture = this.texture;
      this.texture = texture;
      firePropertyChange("texture", oldTexture, texture);
      repaint();
    }
  }

  /**
   * Returns the title of texture dialog displayed when this button is pressed.  
   */
  public String getTextureDialogTitle() {
    return this.textureDialogTitle;
  }

  /**
   * Sets the title of texture dialog displayed when this button is pressed.  
   */
  public void setTextureDialogTitle(String textureDialogTitle) {
    if (textureDialogTitle != this.textureDialogTitle
        || (textureDialogTitle != null && !textureDialogTitle.equals(this.textureDialogTitle))) {
      String oldTextureDialogTitle = this.textureDialogTitle;
      this.textureDialogTitle = textureDialogTitle;
      firePropertyChange("textureDialogTitle", oldTextureDialogTitle, textureDialogTitle);
      repaint();
    }
  }
}