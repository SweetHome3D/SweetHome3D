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
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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
  // Icon used if an image content couldn't be loaded
  private static ImageIcon errorIcon = new ImageIcon (
      IconManager.class.getResource("resources/error.png"));
  // Icon used while if an image content is loaded
  private static ImageIcon waitIcon = new ImageIcon (
      IconManager.class.getResource("resources/wait.png"));
  // Executor used by IconProxy to load images
  private static Executor iconsLoader = Executors.newCachedThreadPool();
  
  private Map<ContentHeightKey,Icon> icons;
  
  private IconManager() {  
    icons = Collections.synchronizedMap(new HashMap<ContentHeightKey,Icon>());
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
    ContentHeightKey contentKey = new ContentHeightKey(content, height);
    Icon icon = this.icons.get(contentKey);
    if (icon == null) {
      icon = new IconProxy(contentKey, waitingComponent);
    }
    return icon;    
  }
  
  /**
   * Returns an icon created and scaled from the content of contentKey.
   * @param contentKey the content from which the icon image is read
   */
  private Icon createIcon(ContentHeightKey contentKey) {
    Icon icon;
    try {
      // Read the icon of the piece 
      InputStream contentStream = contentKey.getContent().openStream();
      BufferedImage image = ImageIO.read(contentStream);
      contentStream.close();
      if (image != null) {
        // Scale the read icon  
        icon = getScaledIcon (image, contentKey.getHeight());
      } else {
        icon = getScaledIcon (errorIcon.getImage(), contentKey.getHeight()); 
      }
    } catch (IOException ex) {
      // Too bad, we'll use errorIcon
      icon = getScaledIcon (errorIcon.getImage(), contentKey.getHeight()); 
    }
    // Store the icon in icons map
    this.icons.put(contentKey, icon); 
    return icon;
  }

  /**
   * Returns an icon scaled to newHeight.
   */
  private Icon getScaledIcon(Image image, int newHeight) {
    int newWidth = image.getWidth(null) * newHeight / image.getHeight(null);
    Image scaledImage = image.getScaledInstance(newWidth,
        newHeight, Image.SCALE_SMOOTH);
    return new ImageIcon(scaledImage);
  }

  /**
   * Proxy icon that displays a temporary icon while waiting 
   * image loading completion. 
   */
  private class IconProxy implements Icon {
    private Icon icon;
    
    public IconProxy(final ContentHeightKey contentKey, 
                     final Component waitingComponent) {
      icon = getScaledIcon(waitIcon.getImage(), contentKey.getHeight());
      // Load the icon in a different thread
      iconsLoader.execute(new Runnable () {
          public void run() {
            icon = createIcon(contentKey);
            waitingComponent.repaint();
          }
        });
    }

    public int getIconWidth() {
      return icon.getIconWidth();
    }

    public int getIconHeight() {
      return icon.getIconHeight();
    }
    
    public void paintIcon(Component c, Graphics g, int x, int y) {
      icon.paintIcon(c, g, x, y);
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

    public Content getContent() {
      return this.content;
    }

    public int getHeight() {
      return this.height;
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
