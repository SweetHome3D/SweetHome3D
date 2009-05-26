/*
 * TextureManager.java 2 oct 2007
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
package com.eteks.sweethome3d.j3d;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.Texture;

import com.eteks.sweethome3d.model.Content;
import com.sun.j3d.utils.image.TextureLoader;

/**
 * Singleton managing texture image cache.
 * @author Emmanuel Puybaret
 */
public class TextureManager {
  private static TextureManager       instance;
  // Image used if an image content couldn't be loaded
  private final Texture               errorTexture;
  // Image used while an image content is loaded
  private final Texture               waitTexture;
  // Map storing loaded images
  private final Map<Content, Texture> textures;
  // Executor used to load images
  private ExecutorService             texturesLoader;

  private TextureManager() {
    this.errorTexture = getColoredImageTexture(Color.RED);
    this.waitTexture = getColoredImageTexture(Color.WHITE);
    this.textures = Collections.synchronizedMap(new WeakHashMap<Content, Texture>());
    this.texturesLoader = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
  }

  /**
   * Returns an instance of this singleton. 
   */
  public static TextureManager getInstance() {
    if (instance == null) {
      instance = new TextureManager();
    }
    return instance;
  }

  /**
   * Shutdowns the multithreaded service that load textures. 
   */
  public void clear() {
    if (this.texturesLoader != null) {
      this.texturesLoader.shutdownNow();
      this.texturesLoader = null;
    }
    this.textures.clear();
  }
  
  /**
   * Returns a texture image of one pixel of the given <code>color</code>. 
   */
  private Texture getColoredImageTexture(Color color) {
    BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
    Graphics g = image.getGraphics();
    g.setColor(color);
    g.drawLine(0, 0, 0, 0);
    g.dispose();
    Texture texture = new TextureLoader(image).getTexture();
    texture.setCapability(Texture.ALLOW_IMAGE_READ);
    texture.getImage(0).setCapability(ImageComponent2D.ALLOW_IMAGE_READ);
    return texture;
  }
  
  /**
   * Reads a texture image from <code>content</code> notified to <code>textureObserver</code>
   * If the texture isn't loaded in cache yet, a one pixel white image texture will be notified 
   * immediately to the given <code>textureObserver</code>, then a second notification will 
   * be given once the image texture is loaded in Event Dispatch Thread. If the texture is in cache, 
   * it will be notified immediately to the given <code>textureObserver</code>.
   * @param content an object containing an image
   * @param textureObserver the observer that will be notified once the texture is available
   */
  public void loadTexture(final Content content, final TextureObserver textureObserver) {
    loadTexture(content, false, textureObserver);
  }
  
  /**
   * Reads a texture image from <code>content</code> notified to <code>textureObserver</code>. 
   * If the texture isn't loaded in cache yet and <code>synchronous</code> is false, a one pixel 
   * white image texture will be notified immediately to the given <code>textureObserver</code>, 
   * then a second notification will be given once the image texture is loaded in Event Dispatch Thread. 
   * If the texture is in cache, it will be notified immediately to the given <code>textureObserver</code>.
   * @param content an object containing an image
   * @param synchronous if <code>true</code>, this method will return only once image content is loaded.
   * @param textureObserver the observer that will be notified once the texture is available
   */
  public void loadTexture(final Content content,
                          boolean synchronous,
                          final TextureObserver textureObserver) {
    Texture texture = this.textures.get(content);
    if (texture == null) {
      if (synchronous) {
        texture = readTexture(content);
        this.textures.put(content, texture);
        // Notify loaded texture to observer
        textureObserver.textureUpdated(texture);
      } else {
        // Notify wait texture to observer
        textureObserver.textureUpdated(this.waitTexture);
        if (this.texturesLoader == null) {
          this.texturesLoader = Executors.newSingleThreadExecutor();
        }
        // Load the image in a different thread
        this.texturesLoader.execute(new Runnable () {
            public void run() {
              final Texture texture = readTexture(content);
              EventQueue.invokeLater(new Runnable() {
                  public void run() {
                    textures.put(content, texture);
                    // Notify loaded or error texture to observer
                    textureObserver.textureUpdated(texture);
                  }
                });
            }
          });
      }
    } else {
      // Notify cached texture to observer
      textureObserver.textureUpdated(texture);
    }
  }

  /**
   * Returns a texture created from the image from <code>content</code>. 
   */
  private Texture readTexture(final Content content) {
    BufferedImage image = null;
    try {
      // Read the image 
      InputStream contentStream = content.openStream();
      image = ImageIO.read(contentStream);
      contentStream.close();
    } catch (IOException ex) {
      // Too bad, we'll use errorTexture
    }            
    final Texture texture;
    if (image == null) {
      texture = errorTexture;
    } else {
      texture = new TextureLoader(image).getTexture();
      texture.setMinFilter(Texture.NICEST);
      texture.setMagFilter(Texture.NICEST);
      texture.setCapability(Texture.ALLOW_IMAGE_READ);
      texture.getImage(0).setCapability(ImageComponent2D.ALLOW_IMAGE_READ);
      texture.setUserData(content);
    }
    return texture;
  }

  /**
   * An observer that receives texture loading notifications. 
   */
  public static interface TextureObserver {
    public void textureUpdated(Texture texture); 
  }
}
