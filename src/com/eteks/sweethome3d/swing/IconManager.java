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
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.tools.ResourceURLContent;

/**
 * Singleton managing icons cache.
 * @author Emmanuel Puybaret
 */
public class IconManager {
  private static IconManager                     instance;
  // Icon used if an image content couldn't be loaded
  private final Content                          errorIconContent;
  // Icon used while an image content is loaded
  private final Content                          waitIconContent;
  // Map storing loaded icons
  private final Map<Content, Map<Integer, Icon>> icons;
  // Executor used by IconProxy to load images
  private ExecutorService                        iconsLoader;

  private IconManager() {
    this.errorIconContent = new ResourceURLContent(IconManager.class, "resources/icons/tango/image-missing.png");
    this.waitIconContent = new ResourceURLContent(IconManager.class, "resources/icons/tango/image-loading.png");
    this.icons = new WeakHashMap<Content, Map<Integer, Icon>>();
    this.iconsLoader = Executors.newFixedThreadPool(5);
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
   * Clears the loaded resources cache and shutdowns the multithreaded service 
   * that load icons. 
   */
  public void clear() {
    if (this.iconsLoader != null) {
      this.iconsLoader.shutdownNow();
      this.iconsLoader = null;
    }
    this.icons.clear();
  }
  
  /**
   * Returns an icon read from <code>content</code> and rescaled at a given <code>height</code>.
   * @param content an objet containing an image
   * @param height  the desired height of the returned icon
   * @param waitingComponent a waiting component. If <code>null</code>, the returned icon will
   *            be read immediately in the current thread.
   */
  public Icon getIcon(Content content, int height, Component waitingComponent) {
    Map<Integer, Icon> contentIcons = this.icons.get(content);
    if (contentIcons == null) {
      contentIcons = new HashMap<Integer, Icon>();
      this.icons.put(content, contentIcons);
    }
    Icon icon = contentIcons.get(height);
    if (icon == null) {
      if (content == this.errorIconContent ||
          content == this.waitIconContent) {
        // Load error and wait icons immediately in this thread 
        icon = createIcon(content, height, null); 
      } else if (waitingComponent == null) {
        // Load icon immediately in this thread 
        icon = createIcon(content, height, 
            getIcon(this.errorIconContent, height, null)); 
      } else {
        // For content different from error icon and wait icon, 
        // laod it in a different thread with a virtual proxy 
        icon = new IconProxy(content, height, waitingComponent,
                 getIcon(this.errorIconContent, height, null),
                 getIcon(this.waitIconContent, height, null));
      }
      // Store the icon in icons map
      contentIcons.put(height, icon);
    }
    return icon;    
  }
  
  /**
   * Returns an icon created and scaled from the content of contentKey.
   * @param content the content from which the icon image is read
   * @param height  the desired height of the returned icon
   * @param errorIcon the returned icon in cas of error
   */
  private Icon createIcon(Content content, int height, Icon errorIcon) {
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
    return errorIcon;
  }

  /**
   * Proxy icon that displays a temporary icon while waiting 
   * image loading completion. 
   */
  private class IconProxy implements Icon {
    private Icon icon;
    
    public IconProxy(final Content content, final int height,
                     final Component waitingComponent,
                     final Icon errorIcon, Icon waitIcon) {
      this.icon = waitIcon;
      if (iconsLoader == null) {
        iconsLoader = Executors.newFixedThreadPool(5);
      }
      // Load the icon in a different thread
      iconsLoader.execute(new Runnable () {
          public void run() {
            icon = createIcon(content, height, errorIcon);
            waitingComponent.repaint();
          }
        });
    }

    public int getIconWidth() {
      return this.icon.getIconWidth();
    }

    public int getIconHeight() {
      return this.icon.getIconHeight();
    }
    
    public void paintIcon(Component c, Graphics g, int x, int y) {
      this.icon.paintIcon(c, g, x, y);
    }
  }
}
