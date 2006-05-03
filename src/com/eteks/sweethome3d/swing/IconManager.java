/*
 * IconManager.java 2 mai 2006
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
import java.awt.Cursor;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.eteks.sweethome3d.model.Content;

/**
 * Singleton managing icons cache.
 * @author Emmanuel Puybaret
 */
public class IconManager {
  private volatile static IconManager instance;
  private static Icon errorIcon = new ImageIcon (
      IconManager.class.getResource("resources/error.png"));
  
  private Map<ContentHeightKey,Icon> icons =
    new HashMap<ContentHeightKey,Icon>();
  
  private IconManager() {  
  }
  
  /**
   * Returns an instance of this singleton. 
   */
  public static IconManager getInstance() {
    if (instance == null) {
      synchronized (IconManager.class) {
        if (instance == null) {
          instance = new IconManager();
        }
      }
    }
    return instance;
  }

  /**
   * Returns an icon read from <code>content</code> and rescaled at a given <code>height</code>.
   * @param content an objet containing an image
   * @param height  the desired height of the returned icon
   * @param waitingComponent a waiting component
   */
  public Icon getIcon (Content content, int height, Component waitingComponent) {
    ContentHeightKey key = new ContentHeightKey(content, height);
    Icon icon = this.icons.get(key);
    if (icon == null) {
      icon = createIcon(content, height, waitingComponent);
      this.icons.put(key, icon);
    }
    return icon;    
  }
  
  private Icon createIcon(Content content, int height, Component waitingComponent) {
    Cursor currentCursor = waitingComponent.getCursor();
    waitingComponent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    try {
      // Read the icon of the piece 
      InputStream contentStream = content.openStream();
      BufferedImage image = ImageIO.read(contentStream);
      contentStream.close();
      if (image != null) {
        // Scale the read icon  
        int width = image.getWidth() * height / image.getHeight();
        Image scaledImage = image.getScaledInstance(
            width, height, Image.SCALE_SMOOTH);
        return new ImageIcon (scaledImage);
      } else {
        return errorIcon;
      }
    } catch (IOException ex) {
      return errorIcon;
    } finally {
      waitingComponent.setCursor(currentCursor);
    }
  }

  /** 
   * Key used to access to icons.
   */
  private static final class ContentHeightKey {
    private final Content content;
    private final int     height;
 
    public ContentHeightKey(Content content, int height) {
      this.content = content;
      this.height = height;
    }

    @Override
    public boolean equals(Object obj) {
      ContentHeightKey key = (ContentHeightKey)obj;
      return this.content == key.content 
             && this.height == key.height;
    }

    @Override
    public int hashCode() {
      return this.content.hashCode() + this.height;
    }  
  }
}
