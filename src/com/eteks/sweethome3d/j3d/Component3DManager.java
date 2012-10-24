/*
 * Canvas3DManager.java 25 oct. 07
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

import java.awt.GraphicsConfigTemplate;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
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
 * Note: this class is compatible with Java 3D 1.3 at runtime but requires Java 3D 1.5 to compile.
 * @author Emmanuel Puybaret
 */
public class Component3DManager {
  private static final String CHECK_OFF_SCREEN_IMAGE_SUPPORT = "com.eteks.sweethome3d.j3d.checkOffScreenSupport";
  
  private static Component3DManager instance;
  
  private RenderingErrorObserver renderingErrorObserver;
  // The Java 3D listener matching renderingErrorObserver 
  // (use Object class to ensure Component3DManager class can run with Java 3D 1.3.1)
  private Object                 renderingErrorListener; 
  private Boolean                offScreenImageSupported;
  private GraphicsConfiguration  defaultScreenConfiguration;

  private Component3DManager() {
    if (!GraphicsEnvironment.isHeadless()) {
      GraphicsConfigTemplate3D template = createGraphicsConfigurationTemplate3D();
      GraphicsDevice defaultScreenDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
      this.defaultScreenConfiguration = defaultScreenDevice.getBestConfiguration(template);
      if (this.defaultScreenConfiguration == null) {
        this.defaultScreenConfiguration = defaultScreenDevice.getBestConfiguration(new GraphicsConfigTemplate3D());
      }
    } else {
      this.offScreenImageSupported = Boolean.FALSE;
    }
  }

  /**
   * Returns the template to configure the graphics of canvas 3D.
   */
  private GraphicsConfigTemplate3D createGraphicsConfigurationTemplate3D() {
    if (System.getProperty("j3d.implicitAntialiasing") == null) {
      System.setProperty("j3d.implicitAntialiasing", "true");
    }
    // Retrieve graphics configuration once 
    GraphicsConfigTemplate3D template = new GraphicsConfigTemplate3D();
    // Try to get antialiasing
    template.setSceneAntialiasing(GraphicsConfigTemplate3D.PREFERRED);
    
    // From http://www.java.net/node/683852
    // Check if the user has set the Java 3D stereo option.
    String stereo = System.getProperty("j3d.stereo");
    if (stereo != null) {
      if ("REQUIRED".equals(stereo))
        template.setStereo(GraphicsConfigTemplate.REQUIRED);
      else if ("PREFERRED".equals(stereo))
        template.setStereo(GraphicsConfigTemplate.PREFERRED);
    }
    return template;
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
  public void setRenderingErrorObserver(RenderingErrorObserver observer) {
    try {
      Class.forName("javax.media.j3d.RenderingErrorListener");
      this.renderingErrorListener = RenderingErrorListenerManager.setRenderingErrorObserver(
          observer, this.renderingErrorListener);
      this.renderingErrorObserver = observer;
    } catch (ClassNotFoundException ex) {
      // As RenderingErrorListener and addRenderingErrorListener are available since Java 3D 1.5, 
      // use the default rendering error reporting if Sweet Home 3D is linked to a previous version
    }
  }
  
  /**
   * Returns the current rendering error listener bound to <code>VirtualUniverse</code>.
   */
  public RenderingErrorObserver getRenderingErrorObserver() {
    return this.renderingErrorObserver;
  }
  
  /**
   * Returns <code>true</code> if offscreen is supported in Java 3D on user system.
   * Will always return <code>false</code> if <code>com.eteks.sweethome3d.j3d.checkOffScreenSupport</code>
   * system is equal to <code>false</code>. By default, <code>com.eteks.sweethome3d.j3d.checkOffScreenSupport</code>
   * is equal to <code>true</code>.
   */
  public boolean isOffScreenImageSupported() {
    if (this.offScreenImageSupported == null) {
      if ("false".equalsIgnoreCase(System.getProperty(CHECK_OFF_SCREEN_IMAGE_SUPPORT, "true"))) {
        this.offScreenImageSupported = Boolean.FALSE;
      } else {
        SimpleUniverse universe = null;
        try {
          // Create a universe bound to no canvas 3D
          ViewingPlatform viewingPlatform = new ViewingPlatform();
          Viewer viewer = new Viewer(new Canvas3D [0]);
          universe = new SimpleUniverse(viewingPlatform, viewer);     
          // Create a dummy 3D image to check if it can be rendered in current Java 3D configuration
          getOffScreenImage(viewer.getView(), 1, 1);
          this.offScreenImageSupported = Boolean.TRUE;
        } catch (IllegalRenderingStateException ex) {
          this.offScreenImageSupported = Boolean.FALSE;
        } catch (NullPointerException ex) {
          this.offScreenImageSupported = Boolean.FALSE;
        } catch (IllegalArgumentException ex) {
          this.offScreenImageSupported = Boolean.FALSE;
        } finally {
          if (universe != null) {
            universe.cleanup();
          }
        }
      }
    }
    return this.offScreenImageSupported;
  }

  /**
   * Returns a new <code>canva3D</code> instance that will call <code>renderingObserver</code>
   * methods during the rendering loop.
   * @throws IllegalRenderingStateException  if the canvas 3D couldn't be created.
   */
  private Canvas3D getCanvas3D(GraphicsConfiguration deviceConfiguration,
                               boolean offscreen,
                               final RenderingObserver renderingObserver) {
    GraphicsConfiguration configuration;
    if (GraphicsEnvironment.isHeadless()) {
      configuration = null;
    } else if (deviceConfiguration == null
               || deviceConfiguration.getDevice() == this.defaultScreenConfiguration.getDevice()) {
      configuration = this.defaultScreenConfiguration;
    } else {
      GraphicsConfigTemplate3D template = createGraphicsConfigurationTemplate3D();      
      configuration = deviceConfiguration.getDevice().getBestConfiguration(template);
      if (configuration == null) {
        configuration = deviceConfiguration.getDevice().getBestConfiguration(new GraphicsConfigTemplate3D());
      }
    }
    if (configuration == null) {
      throw new IllegalRenderingStateException("Can't create graphics environment for Canvas 3D");
    }
    try {
      // Ensure unused canvases are freed
      System.gc();
      // Create a Java 3D canvas  
      return new Canvas3D(configuration, offscreen) {
          @Override
          public void preRender() {
            if (renderingObserver != null) {
              renderingObserver.canvas3DPreRendered(this);
            }
          }
          
          @Override
          public void postRender() {
            if (renderingObserver != null) {
              renderingObserver.canvas3DPostRendered(this);
            }
          }
          
          @Override
          public void postSwap() {
            if (renderingObserver != null) {
              renderingObserver.canvas3DSwapped(this);
            }
          }
        };
    } catch (IllegalArgumentException ex) {
      IllegalRenderingStateException ex2 = new IllegalRenderingStateException("Can't create Canvas 3D");
      ex2.initCause(ex);
      throw ex2;
    }
  }

  /**
   * Returns a new on screen <code>canva3D</code> instance. The returned canvas 3D will be associated 
   * with the graphics configuration of the default screen device. 
   * @throws IllegalRenderingStateException  if the canvas 3D couldn't be created.
   */
  public Canvas3D getOnscreenCanvas3D() {
    return getOnscreenCanvas3D(null);
  }
  
  /**
   * Returns a new on screen <code>canva3D</code> instance which rendering will be observed
   * with the given rendering observer. The returned canvas 3D will be associated with the 
   * graphics configuration of the default screen device. 
   * @param renderingObserver an observer of the 3D rendering process of the returned canvas.
   *            Caution: The methods of the observer will be called in 3D rendering loop thread. 
   * @throws IllegalRenderingStateException  if the canvas 3D couldn't be created.
   */
  public Canvas3D getOnscreenCanvas3D(RenderingObserver renderingObserver) {
    return getCanvas3D(null, false, renderingObserver);
  }
  
  /**
   * Returns a new on screen <code>canva3D</code> instance which rendering will be observed
   * with the given rendering observer.  
   * @param renderingObserver an observer of the 3D rendering process of the returned canvas.
   *            Caution: The methods of the observer will be called in 3D rendering loop thread. 
   * @throws IllegalRenderingStateException  if the canvas 3D couldn't be created.
   */
  public Canvas3D getOnscreenCanvas3D(GraphicsConfiguration deviceConfiguration, 
                                      RenderingObserver renderingObserver) {
    return getCanvas3D(deviceConfiguration, false, renderingObserver);
  }

  /**
   * Returns a new off screen <code>canva3D</code> at the given size. 
   * @throws IllegalRenderingStateException  if the canvas 3D couldn't be created.
   *    To avoid this exception, call {@link #isOffScreenImageSupported() isOffScreenImageSupported()} first.
   */
  public Canvas3D getOffScreenCanvas3D(int width, int height) {
    Canvas3D offScreenCanvas = getCanvas3D(null, true, null);
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
    RenderingErrorObserver previousRenderingErrorObserver = getRenderingErrorObserver();
    try {
      // Replace current rendering error observer by a listener that counts down
      // a latch to check further if a rendering error happened during off screen rendering
      // (rendering error listener is called from a notification thread)
      final CountDownLatch latch = new CountDownLatch(1); 
      setRenderingErrorObserver(new RenderingErrorObserver() {
          public void errorOccured(int errorCode, String errorMessage) {
            latch.countDown();
          }
        });
      
      // Create an off screen canvas and bind it to view
      offScreenCanvas = getOffScreenCanvas3D(width, height);
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
        try {
          // Free off screen buffer and context
          offScreenCanvas.setOffScreenBuffer(null);
        } catch (NullPointerException ex) {
          // Java 3D 1.3 may throw an exception
        }
      }
      // Reset previous rendering error listener
      setRenderingErrorObserver(previousRenderingErrorObserver);
    }
  }
  
  /**
   * An observer that receives error notifications in Java 3D.
   */
  public static interface RenderingErrorObserver {
    void errorOccured(int errorCode, String errorMessage);
  }
  
  /**
   * Manages Java 3D 1.5 <code>RenderingErrorListener</code> change matching the given
   * rendering error observer.
   */
  private static class RenderingErrorListenerManager {
    public static Object setRenderingErrorObserver(final RenderingErrorObserver observer,
                                                   Object previousRenderingErrorListener) {
      if (previousRenderingErrorListener != null) {
        VirtualUniverse.removeRenderingErrorListener(
            (RenderingErrorListener)previousRenderingErrorListener);
      }
      RenderingErrorListener renderingErrorListener = new RenderingErrorListener() {
        public void errorOccurred(RenderingError error) {
          observer.errorOccured(error.getErrorCode(), error.getErrorMessage());
        }
      }; 
      VirtualUniverse.addRenderingErrorListener(renderingErrorListener);
      return renderingErrorListener;
    }
  }
  
  /**
   * An observer that receives notifications during the different steps 
   * of the loop rendering a canvas 3D.
   */
  public static interface RenderingObserver {
    /**
     * Called before <code>canvas3D</code> is rendered.
     */
    public void canvas3DPreRendered(Canvas3D canvas3D); 

    /**
     * Called after <code>canvas3D</code> is rendered.
     */
    public void canvas3DPostRendered(Canvas3D canvas3D); 

    /**
     * Called after <code>canvas3D</code> buffer is swapped.
     */
    public void canvas3DSwapped(Canvas3D canvas3D); 
  }
}
