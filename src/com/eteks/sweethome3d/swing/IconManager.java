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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.tools.URLContent;

/**
 * Singleton managing icons cache.
 * @author Emmanuel Puybaret
 */
public class IconManager {
  private static IconManager          instance;
  // Icon used if an image content couldn't be loaded
  private Content                     errorIcon;
  // Icon used while an image content is loaded
  private Content                     waitIcon;
  // Executor used by IconProxy to load images
  private Executor                    iconsLoader;
  // Map storing loaded icons
  private Map<ContentHeightKey, Icon> icons;

  private IconManager() {
    this.errorIcon = new URLContent (
        getClass().getResource("resources/error.png"));
    this.waitIcon = new URLContent (
        getClass().getResource("resources/wait.png"));
    this.iconsLoader = Executors.newCachedThreadPool();
    this.icons = new HashMap<ContentHeightKey,Icon>();
  }
  
  /**
   * Returns an instance of this singleton. 
   */
  public static IconManager getInstance() {
    if (instance == null) {
      instance = new IconManager();
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
      if (content == waitIcon) {
        // Load waitIcon immediately 
        icon = createIcon(content, height, waitingComponent);
      } else {
        // For other content, use a virtual proxy
        icon = new IconProxy(content, height, waitingComponent);
      }
      // Store the icon in icons map
      this.icons.put(contentKey, icon);
    }
    return icon;    
  }
  
  /**
   * Returns an icon created and scaled from the content of contentKey.
   * @param content the content from which the icon image is read
   * @param height  the desired height of the returned icon
   * @param waitingComponent a waiting component
   */
  private Icon createIcon(Content content, int height, Component waitingComponent) {
    try {
      // Read the icon of the piece 
      InputStream contentStream = content.openStream();
      BufferedImage image = ImageIO.read(contentStream);
      contentStream.close();
      if (image != null) {
        int width = image.getWidth() * height / image.getHeight();
        Image scaledImage = image.getScaledInstance(
            width, height, Image.SCALE_SMOOTH);
        return new ImageIcon (scaledImage);
      }
    } catch (IOException ex) {
      // Too bad, we'll use errorIcon
    }
    //  Get errorIcon stored in cache
    return getIcon (errorIcon, height, waitingComponent);
  }

  /**
   * Proxy icon that displays a temporary icon while waiting 
   * image loading completion. 
   */
  private class IconProxy implements Icon {
    private Icon icon;
    
    public IconProxy(final Content content, final int height,
                     final Component waitingComponent) {
      // Get waitIcon stored in cache
      icon = getIcon(waitIcon, height, waitingComponent); 
      // Load the icon in a different thread
      iconsLoader.execute(new Runnable () {
          public void run() {
            icon = createIcon(content, height, waitingComponent);
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
