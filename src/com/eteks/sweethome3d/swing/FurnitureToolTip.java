/*
 * CatalogFurnitureToolTip.java 17 avr. 2013
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
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JToolTip;

import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.PieceOfFurniture;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.URLContent;

/**
 * A tool tip displaying the information and the icon of a piece of furniture.
 * @author Emmanuel Puybaret
 */
public class FurnitureToolTip extends JToolTip {
  private static final int        ICON_SIZE = 128;
  
  private final boolean           ignoreCategory;
  private final UserPreferences   preferences;
  private final JLabel            pieceIconLabel;
  private PieceOfFurniture        piece;

  
  public FurnitureToolTip(boolean ignoreCategory, 
                          UserPreferences preferences) {
    this.ignoreCategory = ignoreCategory;
    this.preferences = preferences;
    this.pieceIconLabel = new JLabel();
    this.pieceIconLabel.setPreferredSize(new Dimension(ICON_SIZE, ICON_SIZE));
    this.pieceIconLabel.setHorizontalAlignment(JLabel.CENTER);
    this.pieceIconLabel.setVerticalAlignment(JLabel.CENTER);
    setLayout(new GridBagLayout());
    add(this.pieceIconLabel, new GridBagConstraints(0, 0, 1, 1, 1, 1, 
        GridBagConstraints.SOUTH, GridBagConstraints.NONE, new Insets(3, 3, 3, 3), 0, 0));
  }
  
  /**
   * Sets the piece displayed by this tool tip.
   */
  public void setPieceOfFurniture(PieceOfFurniture piece) {
    if (piece != this.piece) {
      String toolTipText = "<html><center>";
      if (!this.ignoreCategory 
          && (piece instanceof CatalogPieceOfFurniture)) {
        toolTipText += "- <b>" + ((CatalogPieceOfFurniture)piece).getCategory().getName() + "</b> -<br>";
      }
      
      toolTipText += "<b>" + piece.getName() + "</b>";
      
      if (this.preferences != null 
          && (piece instanceof CatalogPieceOfFurniture)) {
        String creator = ((CatalogPieceOfFurniture)piece).getCreator();
        if (creator != null) {
          toolTipText += "<br>" + this.preferences.getLocalizedString(FurnitureCatalogTree.class, "tooltipCreator", creator);
        }
      }
      toolTipText += "</center>";
      setToolTipText(toolTipText);
      
      this.pieceIconLabel.setIcon(null);
      if (piece.getIcon() instanceof URLContent) {
        InputStream iconStream = null;
        try {
          // Ensure image will always be viewed in a 128x128 pixels cell
          iconStream = piece.getIcon().openStream();
          BufferedImage image = ImageIO.read(iconStream);
          if (image != null) {
            int width = Math.round(ICON_SIZE * Math.min(1f, (float)image.getWidth() / image.getHeight()));
            int height = Math.round((float)width * image.getHeight() / image.getWidth());
            // Prefer to use a JLabel for the piece icon instead of a HTML <img> tag
            // to avoid using cache to access files with jar protocol as suggested 
            // in http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6962459 
            this.pieceIconLabel.setIcon(IconManager.getInstance().getIcon(piece.getIcon(), height, this)); 
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
      this.piece = piece;
    } 
  }
  
  @Override
  public Dimension getPreferredSize() {
    Dimension preferredSize = super.getPreferredSize();
    if (this.pieceIconLabel.getIcon() != null) {
      preferredSize.width = Math.max(preferredSize.width, ICON_SIZE + 6);
      preferredSize.height += ICON_SIZE + 6;
    }
    return preferredSize;
  }
}

