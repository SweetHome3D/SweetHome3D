/*
 * CatalogItemToolTip.java 17 avr. 2013
 *
 * Sweet Home 3D, Copyright (c) 2013 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JToolTip;

import com.eteks.sweethome3d.model.CatalogItem;
import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.tools.URLContent;

/**
 * A tool tip displaying the information and the icon of a catalog item.
 * @author Emmanuel Puybaret
 */
public class CatalogItemToolTip extends JToolTip {
  /**
   * Type of information displayed by a tool tip.
   */
  public enum DisplayedInformation {
    /**
     * A tool tip displaying only the icon of a piece of furniture.
     */
    ICON, 
    /**
     * A tool tip displaying only the icon, the name and the author of a piece of furniture.
     */
    ICON_NAME_AUTHOR, 
    /**
     * A tool tip displaying only the icon, the name, the author and the category 
     * of a piece of furniture.
     */
    ICON_NAME_AUTHOR_CATEGORY}

  private static final int        ICON_SIZE = 128;
  
  private final DisplayedInformation displayedInformation;
  private final UserPreferences      preferences;
  private final JLabel               itemIconLabel;
  private CatalogItem                catalogItem;

  /**
   * Creates a tool tip that displays the icon of a piece of furniture, its name
   * and its category if <code>ignoreCategory</code> is <code>true</code>.
   */
  public CatalogItemToolTip(boolean ignoreCategory, 
                            UserPreferences preferences) {
    this(ignoreCategory 
           ? DisplayedInformation.ICON_NAME_AUTHOR 
           : DisplayedInformation.ICON_NAME_AUTHOR_CATEGORY, 
         preferences);
  }
  
  /**
   * Creates a tool tip that displays furniture information.
   */
  public CatalogItemToolTip(DisplayedInformation displayedInformation,
                            UserPreferences preferences) {
    this.displayedInformation = displayedInformation;
    this.preferences = preferences;
    this.itemIconLabel = new JLabel();
    this.itemIconLabel.setPreferredSize(new Dimension(ICON_SIZE, ICON_SIZE));
    this.itemIconLabel.setHorizontalAlignment(JLabel.CENTER);
    this.itemIconLabel.setVerticalAlignment(JLabel.CENTER);
    setLayout(new GridBagLayout());
    add(this.itemIconLabel, new GridBagConstraints(0, 0, 1, 1, 1, 1, 
        GridBagConstraints.SOUTH, GridBagConstraints.NONE, new Insets(3, 3, 3, 3), 0, 0));
  }
  
  /**
   * Sets the catalog item displayed by this tool tip.
   */
  public void setCatalogItem(CatalogItem item) {
    if (item != this.catalogItem) {
      String tipTextCreator = null;
      if (this.preferences != null) {
        String creator = item.getCreator();
        if (creator != null) {
          tipTextCreator = this.preferences.getLocalizedString(FurnitureCatalogTree.class, "tooltipCreator", creator);
        }
      }
      
      // Use HTML presentation in the tip text only from Java 6 because with Java 5, 
      // HTML tags are not reinterpreted as soon as the tip text is changed 
      String tipText;
      boolean iconInHtmlImgTag = false;
      if (OperatingSystem.isJavaVersionGreaterOrEqual("1.6")) {
        if (this.displayedInformation != DisplayedInformation.ICON) { 
          tipText = "<html><center>";
          if (this.displayedInformation == DisplayedInformation.ICON_NAME_AUTHOR_CATEGORY 
              && (item instanceof CatalogPieceOfFurniture)) {
            tipText += "- <b>" + ((CatalogPieceOfFurniture)item).getCategory().getName() + "</b> -<br>";
          }
          
          tipText += "<b>" + item.getName() + "</b>";
          if (tipTextCreator != null) {
            tipText += "<br>" + tipTextCreator;
          }
          tipText += "</center>";
        } else {
          tipText = "";
        }        
      } else if (isTipTextComplete()) {
        // Use an alternate HTML presentation that includes icon in an <img> tag
        // for the Mac OS X users who still run Sweet Home 3D under Java 5
        // because jar protocol bug mentioned further doesn't cause issues under this system
        iconInHtmlImgTag = true;
        
        tipText = "<html><table>";
        if (this.displayedInformation != DisplayedInformation.ICON) { 
          tipText += "<tr><td align='center'>";
          if (this.displayedInformation == DisplayedInformation.ICON_NAME_AUTHOR_CATEGORY
              && (item instanceof CatalogPieceOfFurniture)) {
            tipText += "- <b>" + ((CatalogPieceOfFurniture)item).getCategory().getName() + "</b> -<br>";
          }
          tipText += "<b>" + item.getName() + "</b>";
          if (tipTextCreator != null) {
            tipText += "<br>" + tipTextCreator;
          }
          tipText += "</td></tr>";
        }
      } else if (this.displayedInformation != DisplayedInformation.ICON) {
          // Use plain text presentation
          tipText = item.getName();
          if (tipTextCreator != null) {
            tipText += " " + tipTextCreator;
          }
      } else {
        tipText = null;
      }
      
      this.itemIconLabel.setIcon(null);
      if (item.getIcon() instanceof URLContent) {
        InputStream iconStream = null;
        try {
          // Ensure image will always be viewed in a 128x128 pixels cell
          iconStream = item.getIcon().openStream();
          BufferedImage image = ImageIO.read(iconStream);
          if (image != null) {
            int width = Math.round(ICON_SIZE * Math.min(1f, (float)image.getWidth() / image.getHeight()));
            int height = Math.round((float)width * image.getHeight() / image.getWidth());
            if (iconInHtmlImgTag) {
              tipText += "<tr><td width='" + ICON_SIZE + "' height='" + ICON_SIZE + "' align='center' valign='middle'><img width='" + width 
                  + "' height='" + height + "' src='" 
                  + ((URLContent)item.getIcon()).getURL() + "'></td></tr>";
            } else {
              // Prefer to use a JLabel for the piece icon instead of a HTML <img> tag
              // to avoid using cache to access files with jar protocol as suggested 
              // in http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6962459 
              this.itemIconLabel.setIcon(new ImageIcon(image.getHeight() != height
                  ? image.getScaledInstance(width, height, Image.SCALE_SMOOTH)
                  : image));
            } 
          }
        } catch (IOException ex) {
        } finally {
          if (iconStream != null) {
            try {
              iconStream.close();
            } catch (IOException ex) {
            }
          }
        }
      }

      if (iconInHtmlImgTag) {
        tipText += "</table>";
      }
      setTipText(tipText);
      this.catalogItem = item;
    } 
  }
    
  /**
   * Returns <code>true</code> if the text of this tool tip contains
   * all the information that the tool tip should display.
   */
  public boolean isTipTextComplete() {
    return !OperatingSystem.isJavaVersionGreaterOrEqual("1.6")
        && OperatingSystem.isMacOSX();
  }
  
  @Override
  public Dimension getPreferredSize() {
    Dimension preferredSize = super.getPreferredSize();
    if (this.itemIconLabel.getIcon() != null) {
      preferredSize.width = Math.max(preferredSize.width, ICON_SIZE + 6);
      preferredSize.height += ICON_SIZE + 6;
    }
    return preferredSize;
  }
}

