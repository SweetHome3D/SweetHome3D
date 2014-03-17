/*
 * TextureManager.java 2 oct 2007
 *
 * Sweet Home 3D, Copyright (c) 2007 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
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
import com.eteks.sweethome3d.model.HomeTexture;
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
  private final Map<RotatedContentKey, TextureKey> contentTextureKeys;
  // Map storing loaded textures
  private final Map<TextureKey, Texture> textures;
  // Map storing model nodes being loaded
  private Map<RotatedContentKey, List<TextureObserver>> loadingTextureObservers;
  // Executor used to load images
  private ExecutorService                texturesLoader;

  private TextureManager() {
    this.errorTexture = getColoredImageTexture(Color.RED);
    this.waitTexture = getColoredImageTexture(Color.WHITE);
    this.contentTextureKeys = new WeakHashMap<RotatedContentKey, TextureKey>();
    this.textures = new WeakHashMap<TextureKey, Texture>();
    this.loadingTextureObservers = new HashMap<RotatedContentKey, List<TextureObserver>>();
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
    loadTexture(content, 0, synchronous, textureObserver);
  }

  /**
   * Reads a texture image from <code>content</code> notified to <code>textureObserver</code>. 
   * If the texture isn't loaded in cache yet and <code>synchronous</code> is false, a one pixel 
   * white image texture will be notified immediately to the given <code>textureObserver</code>, 
   * then a second notification will be given in Event Dispatch Thread once the image texture is loaded. 
   * If the texture is in cache, it will be notified immediately to the given <code>textureObserver</code>.
   * @param content an object containing an image
   * @param angle   the rotation angle applied to the image
   * @param synchronous if <code>true</code>, this method will return only once image content is loaded.
   * @param textureObserver the observer that will be notified once the texture is available
   * @throws IllegalStateException if synchronous is <code>false</code> and the current thread isn't 
   *    the Event Dispatch Thread.  
   */
  public void loadTexture(final Content content,
                          final float   angle,
                          boolean synchronous,
                          final TextureObserver textureObserver) {
    Texture texture;
    TextureKey textureKey;
    final RotatedContentKey contentKey = new RotatedContentKey(content, angle);
    synchronized (this.textures) { // Use one mutex for both maps
      textureKey = this.contentTextureKeys.get(contentKey);
      if (textureKey != null) {
        texture = this.textures.get(textureKey);
      } else {
        texture = null;
      }
    }
    if (texture == null) {
      if (synchronous) {
        texture = shareTexture(loadTexture(content, angle), contentKey);
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
        
        List<TextureObserver> observers = this.loadingTextureObservers.get(contentKey);
        if (observers != null) {
          // If observers list exists, content texture is already being loaded
          // register observer for future notification
          observers.add(textureObserver);
        } else {
          // Create a list of observers that will be notified once content texture is loaded
          observers = new ArrayList<TextureObserver>();
          observers.add(textureObserver);
          this.loadingTextureObservers.put(contentKey, observers);

          // Load the image in a different thread
          this.texturesLoader.execute(new Runnable () {
              public void run() {
                final Texture texture = shareTexture(loadTexture(content, angle), contentKey);
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                      // Notify loaded texture to observer
                      for (TextureObserver observer : loadingTextureObservers.remove(contentKey)) {
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
  public Texture loadTexture(final Content content) {
    return loadTexture(content, 0);
  }
  
  /**
   * Returns a texture created from the image from <code>content</code>  
   * and rotated of a given <code>angle</code> in radians. 
   */
  private Texture loadTexture(final Content content, float angle) {
    try {
      // Read the image 
      InputStream contentStream = content.openStream();
      BufferedImage image = ImageIO.read(contentStream);
      if (angle != 0) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        BufferedImage rotatedImage = new BufferedImage((int)Math.round(Math.abs(image.getWidth() * cos) + Math.abs(image.getHeight() * sin)), 
            (int)Math.round(Math.abs(image.getWidth() * sin) + Math.abs(image.getHeight() * cos)), image.getType());
        Graphics2D g2D = (Graphics2D)rotatedImage.getGraphics();
        g2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2D.setPaint(new TexturePaint(image, 
                        new Rectangle2D.Float(0, 0, image.getWidth(), image.getHeight())));
        g2D.rotate(angle);
        float maxDimension = Math.max(rotatedImage.getWidth(), rotatedImage.getHeight());
        g2D.fill(new Rectangle2D.Float(-maxDimension, -maxDimension, 3 * maxDimension, 3 * maxDimension));
        g2D.dispose();
        image = rotatedImage;
      }
      contentStream.close();
      if (image != null) {
        Texture texture = new TextureLoader(image).getTexture();
        // Keep in user data the URL of the texture image
        if (content instanceof URLContent) {
          texture.setUserData(((URLContent)content).getURL());
        }
        return texture;
      } else {
        return errorTexture;
      }
    } catch (IOException ex) {
      // Too bad, we'll use errorTexture
      return errorTexture;
    } catch (RuntimeException ex) {
      // Take into account exceptions of Java 3D 1.5 ImageException class
      // in such a way program can run in Java 3D 1.3.1
      if (ex.getClass().getName().equals("com.sun.j3d.utils.image.ImageException")) {
        // Images not supported by TextureLoader
        return errorTexture;
      } else {
        throw ex;
      }
    }            
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
                               final RotatedContentKey contentKey) {
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
        for (TextureKey key : this.textures.keySet()) {
          if (key.getTexture() == sharedTexture) {
            textureKey = key;
            break;
          }
        }
      }
      if (contentKey != null) {
        this.contentTextureKeys.put(contentKey, textureKey);
      }
    }
    return sharedTexture;
  }

  /**
   * Sets the attributes and capabilities of a shared <code>texture</code>.
   */
  private void setSharedTextureAttributesAndCapabilities(Texture texture) {
    if (!texture.isLive()) {
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
   * Returns the width of the given texture once its rotation angle is applied.
   */
  public float getRotatedTextureWidth(HomeTexture texture) {
    float angle = texture.getAngle();
    if (angle != 0) {
      return (float)Math.rint(Math.abs(texture.getWidth() * Math.cos(angle)) 
          + Math.abs(texture.getHeight() * Math.sin(angle)));
    } else {
      return texture.getWidth();
    }
  }

  /**
   * Returns the height of the given texture once its rotation angle is applied.
   */
  public float getRotatedTextureHeight(HomeTexture texture) {
    float angle = texture.getAngle();
    if (angle != 0) {
      return (float)Math.rint(Math.abs(texture.getWidth() * Math.sin(angle)) 
          + Math.abs(texture.getHeight() * Math.cos(angle)));
    } else {
      return texture.getHeight();
    }
  }

  /**
   * An observer that receives texture loading notifications. 
   */
  public static interface TextureObserver {
    public void textureUpdated(Texture texture); 
  }
  
  /**
   * Key used to ensure rotated content uniqueness.
   */
  private static class RotatedContentKey {
    private Content content;
    private float   angle;
    
    public RotatedContentKey(Content content, float angle) {
      this.content = content;
      this.angle = angle;
    }
    
    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if (obj instanceof RotatedContentKey) {
        RotatedContentKey rotatedContentKey = (RotatedContentKey)obj;
        return this.content.equals(rotatedContentKey.content)
            && this.angle == rotatedContentKey.angle;
      }
      return false;
    }

    @Override
    public int hashCode() {
      return this.content.hashCode() 
          + Float.floatToIntBits(this.angle);
    }    
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
        if (this.transparent) {
          this.transparent = containsTransparentPixels(imageBits);
        }
        this.imageBits = new WeakReference<int[]>(imageBits);
      }
      return imageBits;
    }

    /**
     * Returns <code>true</code> if the image contains at least a transparent pixel. 
     */
    private boolean containsTransparentPixels(int [] imageBits) {
      boolean transparentPixel = false;
      for (int argb : imageBits) {
        if ((argb & 0xFF000000) != 0xFF000000) {
          transparentPixel = true;
          break;
        }
      }
      return transparentPixel;
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
