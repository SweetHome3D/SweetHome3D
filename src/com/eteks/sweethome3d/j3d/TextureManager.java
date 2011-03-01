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
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.media.j3d.ImageComponent;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.Texture;

import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.tools.URLContent;
import com.sun.j3d.utils.image.TextureLoader;

/**
 * Singleton managing texture image cache.
 * @author Emmanuel Puybaret
 */
public class TextureManager {
  private static TextureManager          instance;
  // Image used if an image content couldn't be loaded
  private final Texture                  errorTexture;
  // Image used while an image content is loaded
  private final Texture                  waitTexture;
  // Map storing loaded texture contents
  private final Map<Content, TextureKey> contentTextureKeys;
  // Map storing loaded textures
  private final Map<TextureKey, Texture> textures;
  // Map storing model nodes being loaded
  private Map<Content, List<TextureObserver>> loadingTextureObservers;
  // Executor used to load images
  private ExecutorService                texturesLoader;

  private TextureManager() {
    this.errorTexture = getColoredImageTexture(Color.RED);
    this.waitTexture = getColoredImageTexture(Color.WHITE);
    this.contentTextureKeys = new WeakHashMap<Content, TextureKey>();
    this.textures = new WeakHashMap<TextureKey, Texture>();
    this.loadingTextureObservers = new HashMap<Content, List<TextureObserver>>();
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
    synchronized (this.textures) {
      this.contentTextureKeys.clear();
      this.textures.clear();
    }
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
    texture.setCapability(Texture.ALLOW_FORMAT_READ);
    texture.getImage(0).setCapability(ImageComponent2D.ALLOW_IMAGE_READ);
    texture.getImage(0).setCapability(ImageComponent2D.ALLOW_FORMAT_READ);
    return texture;
  }
  
  /**
   * Reads a texture image from <code>content</code> notified to <code>textureObserver</code>
   * If the texture isn't loaded in cache yet, a one pixel white image texture will be notified 
   * immediately to the given <code>textureObserver</code>, then a second notification will 
   * be given in Event Dispatch Thread once the image texture is loaded. If the texture is in cache, 
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
   * then a second notification will be given in Event Dispatch Thread once the image texture is loaded. 
   * If the texture is in cache, it will be notified immediately to the given <code>textureObserver</code>.
   * @param content an object containing an image
   * @param synchronous if <code>true</code>, this method will return only once image content is loaded.
   * @param textureObserver the observer that will be notified once the texture is available
   * @throws IllegalStateException if synchronous is <code>false</code> and the current thread isn't 
   *    the Event Dispatch Thread.  
   */
  public void loadTexture(final Content content,
                          boolean synchronous,
                          final TextureObserver textureObserver) {
    Texture texture;
    TextureKey textureKey;
    synchronized (this.textures) { // Use one mutex for both maps
      textureKey = this.contentTextureKeys.get(content);
      if (textureKey == null) {
        texture = this.textures.get(textureKey);
      } else {
        texture = null;
      }
    }
    if (texture == null) {
      if (synchronous) {
        texture = shareTexture(readTexture(content), content);
        // Notify loaded texture to observer
        textureObserver.textureUpdated(texture);
      } else if (!EventQueue.isDispatchThread()) {
        throw new IllegalStateException("Asynchronous call out of Event Dispatch Thread");
      } else {
        // Notify wait texture to observer
        textureObserver.textureUpdated(this.waitTexture);
        if (this.texturesLoader == null) {
          this.texturesLoader = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        }
        
        List<TextureObserver> observers = this.loadingTextureObservers.get(content);
        if (observers != null) {
          // If observers list exists, content texture is already being loaded
          // register observer for future notification
          observers.add(textureObserver);
        } else {
          // Create a list of observers that will be notified once content texture is loaded
          observers = new ArrayList<TextureObserver>();
          observers.add(textureObserver);
          this.loadingTextureObservers.put(content, observers);

          // Load the image in a different thread
          this.texturesLoader.execute(new Runnable () {
              public void run() {
                final Texture texture = shareTexture(readTexture(content), content);
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                      // Notify loaded texture to observer
                      for (TextureObserver observer : loadingTextureObservers.remove(content)) {
                        observer.textureUpdated(texture);
                      }
                    }
                  });
              }
            });
        }
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
      // Keep in user data the URL of the texture image
      if (content instanceof URLContent) {
        texture.setUserData(((URLContent)content).getURL());
      }
    }
    return texture;
  }

  /**
   * Returns either the <code>texture</code> in parameter or a shared texture 
   * if the same texture as the one in parameter is already shared.
   */
  public Texture shareTexture(Texture texture) {
    return shareTexture(texture, null);
  }
  
  /**
   * Returns the texture matching <code>content</code>, either 
   * the <code>texture</code> in parameter or a shared texture if the 
   * same texture as the one in parameter is already shared.
   */
  private Texture shareTexture(final Texture texture,
                               final Content content) {
    TextureKey textureKey = new TextureKey(texture);
    Texture sharedTexture;
    synchronized (this.textures) { // Use one mutex for both maps
      sharedTexture = this.textures.get(textureKey);
      if (sharedTexture == null) {
        sharedTexture = texture;
        setSharedTextureAttributesAndCapabilities(sharedTexture);
        this.textures.put(textureKey, sharedTexture);
      } else {
        // Search which key matches sharedTexture to keep unique keys
        for (TextureKey key : textures.keySet()) {
          if (key.getTexture() == sharedTexture) {
            textureKey = key;
            break;
          }
        }
      }
      if (content != null) {
        this.contentTextureKeys.put(content, textureKey);
      }
    }
    return sharedTexture;
  }

  /**
   * Sets the attributes and capabilities of a shared <code>texture</code>.
   */
  private void setSharedTextureAttributesAndCapabilities(Texture texture) {
    texture.setMinFilter(Texture.NICEST);
    texture.setMagFilter(Texture.NICEST);
    texture.setCapability(Texture.ALLOW_FORMAT_READ);
    texture.setCapability(Texture.ALLOW_IMAGE_READ);
    for (ImageComponent image : texture.getImages()) {
      if (!image.isLive()) {
        image.setCapability(ImageComponent.ALLOW_FORMAT_READ);
        image.setCapability(ImageComponent.ALLOW_IMAGE_READ);
      }
    }
  }
 
  /**
   * Returns <code>true</code> if the texture is shared and its image contains 
   * at least one transparent pixel.
   */
  public boolean isTextureTransparent(Texture texture) {
    synchronized (this.textures) { // Use one mutex for both maps
      // Search which key matches texture
      for (TextureKey key : textures.keySet()) {
        if (key.getTexture() == texture) {
          return key.isTransparent();
        }
      }
      return texture.getFormat() == Texture.RGBA;
    }
  }
  
  /**
   * An observer that receives texture loading notifications. 
   */
  public static interface TextureObserver {
    public void textureUpdated(Texture texture); 
  }
  
  /**
   * Key used to ensure texture uniqueness in textures map.
   * Image bits of the texture are stored in a weak reference to avoid grabbing memory uselessly.
   */
  private static class TextureKey {
    private Texture               texture;
    private WeakReference<int []> imageBits; 
    private int                   hashCodeCache;
    private boolean               hashCodeSet;
    private boolean               transparent;

    public TextureKey(Texture texture) {
      this.texture = texture;      
    }
    
    public Texture getTexture() {
      return this.texture;
    }
    
    /**
     * Returns the pixels of the given <code>image</code>.
     */
    private int [] getImagePixels() {
      int [] imageBits = null;
      if (this.imageBits != null) {
        imageBits = this.imageBits.get();
      }
      if (imageBits == null) {
        BufferedImage image = ((ImageComponent2D)this.texture.getImage(0)).getImage();
        if (image.getType() != BufferedImage.TYPE_INT_RGB
            && image.getType() != BufferedImage.TYPE_INT_ARGB) {
          // Transform as TYPE_INT_ARGB or TYPE_INT_RGB (much faster than calling image.getRGB())
          BufferedImage tmp = new BufferedImage(image.getWidth(), image.getHeight(), 
              this.texture.getFormat() == Texture.RGBA ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
          Graphics2D g = (Graphics2D)tmp.getGraphics();
          g.drawImage(image, null, 0, 0);
          g.dispose();
          image = tmp;
        }
        imageBits = (int [])image.getRaster().getDataElements(0, 0, image.getWidth(), image.getHeight(), null);
        this.transparent = image.getTransparency() != BufferedImage.OPAQUE;
        this.imageBits = new WeakReference<int[]>(imageBits);
      }
      return imageBits;
    }

    /**
     * Returns <code>true</code> if the image of the texture contains at least one transparent pixel.
     */
    public boolean isTransparent() {
      return this.transparent;
    }
    
    /**
     * Returns <code>true</code> if the image of this texture and 
     * the image of the object in parameter are the same. 
     */
    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if (obj instanceof TextureKey) {
        TextureKey textureKey = (TextureKey)obj;
        if (this.texture == textureKey.texture) {
          return true;
        } else if (hashCode() == textureKey.hashCode()){
          return Arrays.equals(getImagePixels(), textureKey.getImagePixels());
        }
      }
      return false;
    }

    @Override
    public int hashCode() {
      if (!this.hashCodeSet) {
        this.hashCodeCache = Arrays.hashCode(getImagePixels());
        this.hashCodeSet = true;
      }
      return this.hashCodeCache;
    }
  }
}
