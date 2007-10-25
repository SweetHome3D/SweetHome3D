/*
 * Canvas3DManager.java 25 oct. 07
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

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.media.j3d.Canvas3D;
import javax.media.j3d.GraphicsConfigTemplate3D;
import javax.media.j3d.IllegalRenderingStateException;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.RenderingError;
import javax.media.j3d.RenderingErrorListener;
import javax.media.j3d.Screen3D;
import javax.media.j3d.View;
import javax.media.j3d.VirtualUniverse;

import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.Viewer;
import com.sun.j3d.utils.universe.ViewingPlatform;

/**
 * Manager of <code>Canvas3D</code> instantiations and Java 3D error listeners.
 * @author Emmanuel Puybaret
 */
public class Component3DManager {
  private static Component3DManager instance;
  
  private RenderingErrorListener renderingErrorListener;
  private Boolean                offScreenImageSupported;

  private Component3DManager() {
  }
  
  /**
   * Returns an instance of this singleton. 
   */
  public static Component3DManager getInstance() {
    if (instance == null) {
      instance = new Component3DManager();
    }
    return instance;
  }
  
  /**
   * Sets the current rendering error listener bound to <code>VirtualUniverse</code>.
   */
  public void setRenderingErrorListener(RenderingErrorListener listener) {
    try {
      if (this.renderingErrorListener != null) {
        VirtualUniverse.removeRenderingErrorListener(this.renderingErrorListener);
      }
      VirtualUniverse.addRenderingErrorListener(listener);
      this.renderingErrorListener = listener;
    } catch (NoSuchMethodError ex) {
      // As addRenderingErrorListener is available since Java 3D 1.5, use 
      // default rendering error reporting if Sweet Home 3D is linked to a previous version
    }
  }
  
  /**
   * Returns the current rendering error listener bound to <code>VirtualUniverse</code>.
   */
  public RenderingErrorListener getRenderingErrorListener() {
    return this.renderingErrorListener;
  }
  
  /**
   * Returns <code>true</code> if offscreen is supported in Java 3D on user system. 
   */
  public boolean isOffScreenImageSupported() {
    if (this.offScreenImageSupported == null) {
      SimpleUniverse universe = null;
      try {
        // Create a universe bound to no canvas 3D
        ViewingPlatform viewingPlatform = new ViewingPlatform();
        Viewer viewer = new Viewer(new Canvas3D [0]);
        universe = new SimpleUniverse(viewingPlatform, viewer);     
        // Create a dummy 3D image to check if it can be rendered in current Java 3D configuration
        getOffScreenImage(viewer.getView(), 1, 1);
        this.offScreenImageSupported = true;
      } catch (IllegalRenderingStateException ex) {
        this.offScreenImageSupported = false;
      } catch (NullPointerException ex) {
        this.offScreenImageSupported = false;
      } catch (IllegalArgumentException ex) {
        this.offScreenImageSupported = false;
      } finally {
        if (universe != null) {
          universe.cleanup();
        }
      }
    }
    return this.offScreenImageSupported;
  }

  /**
   * Returns a new <code>canva3D</code> instance.
   * @throws IllegalRenderingStateException  if the canvas 3D couldn't be created.
   */
  private Canvas3D createCanvas3D(boolean offscreen) {
    GraphicsConfigTemplate3D gc = new GraphicsConfigTemplate3D();
    // Try to get antialiasing
    gc.setSceneAntialiasing(GraphicsConfigTemplate3D.PREFERRED);
    if (offscreen) {
      gc.setDoubleBuffer(GraphicsConfigTemplate3D.UNNECESSARY);
    }
    GraphicsConfiguration configuration = GraphicsEnvironment.getLocalGraphicsEnvironment().
            getDefaultScreenDevice().getBestConfiguration(gc);
    if (configuration == null) {
      configuration = GraphicsEnvironment.getLocalGraphicsEnvironment().
          getDefaultScreenDevice().getBestConfiguration(new GraphicsConfigTemplate3D());
      if (configuration == null) {
        throw new IllegalRenderingStateException("Can't create graphics environment for Canvas 3D");
      }
    }
    
    Canvas3D canvas3D;
    try {
      // Create the Java 3D canvas that will display home 
      canvas3D = new Canvas3D(configuration, offscreen);
    } catch (IllegalArgumentException ex) {
      IllegalRenderingStateException ex2 = new IllegalRenderingStateException("Can't create Canvas 3D");
      ex2.initCause(ex);
      throw ex2;
    }
    
    return canvas3D;
  }
  
  /**
   * Returns a new on screen <code>canva3D</code> instance.
   * @throws IllegalRenderingStateException  if the canvas 3D couldn't be created.
   */
  public Canvas3D createOnscreenCanvas3D() {
    return createCanvas3D(false);
  }
  
  /**
   * Returns a new off screen <code>canva3D</code> at the given size.
   * @throws IllegalRenderingStateException  if the canvas 3D couldn't be created.
   */
  private Canvas3D createOffScreenCanvas(int width, int height) {
    Canvas3D offScreenCanvas = createCanvas3D(true);
    // Configure canvas 3D for offscreen
    Screen3D screen3D = offScreenCanvas.getScreen3D();
    screen3D.setSize(width, height);
    screen3D.setPhysicalScreenWidth(2f);
    screen3D.setPhysicalScreenHeight(2f / width * height);
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    ImageComponent2D imageComponent2D = new ImageComponent2D(ImageComponent2D.FORMAT_RGB, image);
    imageComponent2D.setCapability(ImageComponent2D.ALLOW_IMAGE_READ);
    offScreenCanvas.setOffScreenBuffer(imageComponent2D);
    return offScreenCanvas;
  }
  
  /**
   * Returns an image at the given size of the 3D <code>view</code>. 
   * This image is created with an off screen canvas.
   * @throws IllegalRenderingStateException  if the image couldn't be created.
   */
  public BufferedImage getOffScreenImage(View view, int width, int height)  {
    Canvas3D offScreenCanvas = null;
    RenderingErrorListener previousRenderingErrorListener = getRenderingErrorListener();
    try {
      // Replace current rendering error listener by a listener that counts down
      // a latch to check further if a rendering error happened during off screen rendering
      // (rendering error listener is called from a notification thread)
      final CountDownLatch latch = new CountDownLatch(1); 
      setRenderingErrorListener(new RenderingErrorListener() {
          public void errorOccurred(RenderingError error) {
            latch.countDown();
          }
        });
      
      // Create an off sreen canvas and bind it to view
      offScreenCanvas = createOffScreenCanvas(width, height);
      view.addCanvas3D(offScreenCanvas);
      
      // Render off screen canvas
      offScreenCanvas.renderOffScreenBuffer();
      offScreenCanvas.waitForOffScreenRendering();
      
      // If latch count becomes equal to 0 during the past instructions or in the coming 10 milliseconds, 
      // this means that a rendering error happened
      if (latch.await(10, TimeUnit.MILLISECONDS)) {
        throw new IllegalRenderingStateException("Off screen rendering unavailable");
      }
      
      return offScreenCanvas.getOffScreenBuffer().getImage();
    } catch (InterruptedException ex) {
      IllegalRenderingStateException ex2 = 
          new IllegalRenderingStateException("Off screen rendering interrupted");
      ex2.initCause(ex);
      throw ex2;
    } finally {
      if (offScreenCanvas != null) {
        view.removeCanvas3D(offScreenCanvas);
      }
      // Reset previous rendering error listener
      setRenderingErrorListener(previousRenderingErrorListener);
    }
  }
}
