/*
 * ColorButton.java 29 mai 07
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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.UIManager;

/**
 * Button displaying a color as an icon.
 */
public class ColorButton extends JButton {
  public static final String COLOR_PROPERTY = "color";
  public static final String COLOR_DIALOG_TITLE_PROPERTY = "colorDialogTitle";
  
  // Share color chooser between ColorButton instances to keep recent colors
  private static JColorChooser colorChooser;
  private static Locale        colorChooserLocale;
  
  private Integer color;
  private String  colorDialogTitle;

  /**
   * Creates a color button.
   */
  public ColorButton() {
    JLabel colorLabel = new JLabel("Color");
    Dimension iconDimension = colorLabel.getPreferredSize();
    final int iconWidth = iconDimension.width;
    final int iconHeight = iconDimension.height;
    setIcon(new Icon() {
      public int getIconWidth() {
        return iconWidth;
      }

      public int getIconHeight() {
        return iconHeight;
      }

      public void paintIcon(Component c, Graphics g, int x, int y) {
        g.setColor(Color.BLACK);
        g.drawRect(x + 2, y + 2, iconWidth - 5, iconHeight - 5);
        if (color != null) {
          g.setColor(new Color(color));
          g.fillRect(x + 3, y + 3, iconWidth - 6,
                  iconHeight - 6);
        }
      }
    });

    // Add a listener to update color
    addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        // Create color chooser instance each time default locale changed 
        if (colorChooser == null
            || !Locale.getDefault().equals(colorChooserLocale)) {
          try {
            // Read Swing localized properties because Swing doesn't update its internal strings automatically
            // when default Locale is updated (see bug http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4884480)
            ResourceBundle resource = 
                ResourceBundle.getBundle("com.sun.swing.internal.plaf.basic.resources.basic");
            // Update UIManager properties
            for (Enumeration iter = resource.getKeys(); iter.hasMoreElements(); ) {
              String property = (String)iter.nextElement();
              UIManager.put(property, resource.getString(property));
            }      
          } catch (MissingResourceException ex) {
            // Let labels unchanged
          }
          
          colorChooser = new JColorChooser();
          colorChooserLocale = Locale.getDefault();
        }
        // Update edited color in furniture color chooser
        colorChooser.setColor(color != null 
            ? new Color(color)
            : null);
        JDialog colorDialog = JColorChooser.createDialog(getParent(), 
            colorDialogTitle, true, colorChooser,
            new ActionListener () { 
              public void actionPerformed(ActionEvent e) {
                // Change button color when user click on ok button
                setColor(colorChooser.getColor().getRGB());
              }
            }, null);
        colorDialog.setVisible(true);
      }
    });
  }

  /**
   * Returns the color displayed by this button.
   * @return the RGB code of the color of this button or <code>null</code>.
   */
  public Integer getColor() {
    return this.color;
  }

  /**
   * Sets the color displayed by this button.
   * @param color RGB code of the color or <code>null</code>.
   */
  public void setColor(Integer color) {
    if (color != this.color
        || (color != null && !color.equals(this.color))) {
      Integer oldColor = this.color;
      this.color = color;
      firePropertyChange(COLOR_PROPERTY, oldColor, color);
      repaint();
    }
  }

  /**
   * Returns the title of color dialog displayed when this button is pressed.  
   */
  public String getColorDialogTitle() {
    return this.colorDialogTitle;
  }

  /**
   * Sets the title of color dialog displayed when this button is pressed.  
   */
  public void setColorDialogTitle(String colorDialogTitle) {
    if (colorDialogTitle != this.colorDialogTitle
        || (colorDialogTitle != null && !colorDialogTitle.equals(this.colorDialogTitle))) {
      String oldColorDialogTitle = this.colorDialogTitle;
      this.colorDialogTitle = colorDialogTitle;
      firePropertyChange(COLOR_DIALOG_TITLE_PROPERTY, oldColorDialogTitle, colorDialogTitle);
      repaint();
    }
  }
}