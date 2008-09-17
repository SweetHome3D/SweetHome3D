/*
 * HomeComponent3D.java 24 ao?t 2006
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

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;
import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Geometry;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Group;
import javax.media.j3d.IllegalRenderingStateException;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.IndexedGeometryArray;
import javax.media.j3d.IndexedQuadArray;
import javax.media.j3d.IndexedTriangleArray;
import javax.media.j3d.IndexedTriangleFanArray;
import javax.media.j3d.IndexedTriangleStripArray;
import javax.media.j3d.Light;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Texture;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.media.j3d.TriangleArray;
import javax.media.j3d.TriangleFanArray;
import javax.media.j3d.TriangleStripArray;
import javax.media.j3d.View;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.MouseInputAdapter;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.TexCoord2f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import com.eteks.sweethome3d.model.Camera;
import com.eteks.sweethome3d.model.CameraEvent;
import com.eteks.sweethome3d.model.CameraListener;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.FurnitureEvent;
import com.eteks.sweethome3d.model.FurnitureListener;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.PieceOfFurniture;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.model.WallEvent;
import com.eteks.sweethome3d.model.WallListener;
import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.Viewer;
import com.sun.j3d.utils.universe.ViewingPlatform;

/**
 * A component that displays home walls and furniture with Java 3D. 
 * @author Emmanuel Puybaret
 */
public class HomeComponent3D extends JComponent implements Printable {
  private enum ActionType {MOVE_CAMERA_FORWARD, MOVE_CAMERA_FAST_FORWARD, MOVE_CAMERA_BACKWARD, MOVE_CAMERA_FAST_BACKWARD,  
      ROTATE_CAMERA_YAW_LEFT, ROTATE_CAMERA_YAW_FAST_LEFT, ROTATE_CAMERA_YAW_RIGHT, ROTATE_CAMERA_YAW_FAST_RIGHT, 
      ROTATE_CAMERA_PITCH_UP, ROTATE_CAMERA_PITCH_DOWN}
  
  private static final NumberFormat OBJ_NUMBER_FORMAT = new DecimalFormat("0.###", new DecimalFormatSymbols(Locale.US));
  private static final boolean OBJ_REDUCE_FILE_SIZE = false;
  
  private Home                      home;
  private SimpleUniverse            universe;
  private Map<Object, ObjectBranch> homeObjects = new HashMap<Object, ObjectBranch>();
  private Collection<Object>        homeObjectsToUpdate;
  // Listeners bound to home that updates 3D scene objects
  private CameraListener            cameraListener;
  private PropertyChangeListener    homeCameraListener;
  private PropertyChangeListener    skyColorListener;
  private PropertyChangeListener    groundColorAndTextureListener;
  private PropertyChangeListener    lightColorListener;
  private WallListener              wallListener;
  private PropertyChangeListener    wallsAlphaListener;
  private FurnitureListener         furnitureListener;
  // Offscreen printed image cache 
  // Creating an offscreen buffer is a quite lengthy operation so we keep the last printed image in this field
  // This image should be set to null each time the 3D view changes
  private BufferedImage             printedImage;

  /**
   * Creates a 3D component that displays <code>home</code> walls and furniture, 
   * with no controller.
   */
  public HomeComponent3D(Home home) {
    this(home, null);  
  }
  
  /**
   * Creates a 3D component that displays <code>home</code> walls and furniture.
   * @throws IllegalRenderingStateException  if the canvas 3D displayed by this component couldn't be created.
   */
  public HomeComponent3D(Home home, HomeController3D controller) {
    this.home = home;
    
    // Create the Java 3D canvas that will display home 
    Canvas3D canvas3D = Component3DManager.getInstance().getOnscreenCanvas3D();    
    // Layout canvas3D
    setLayout(new GridLayout(1, 1));
    add(canvas3D);

    if (controller != null) {
      addMouseListeners(controller, canvas3D);
      createActions(controller);
      installKeyboardActions();
      // Let this component manage focus
      setFocusable(true);
    }

    // Add an ancestor listener to create canvas universe once this component is made visible 
    // and clean up universe once its parent frame is disposed
    addAncestorListener(canvas3D, home);
  }

  /**
   * Adds an ancestor listener to this component to manage canvas universe 
   * creation and clean up.  
   */
  private void addAncestorListener(final Canvas3D canvas3D, 
                                    final Home home) {
    addAncestorListener(new AncestorListener() {        
        public void ancestorAdded(AncestorEvent event) {
          universe = createUniverse(home);
          // Bind universe to canvas3D
          universe.getViewer().getView().addCanvas3D(canvas3D);
          canvas3D.setFocusable(false);
        }
        
        public void ancestorRemoved(AncestorEvent event) {
          universe.cleanup();
          removeHomeListeners(home);
        }
        
        public void ancestorMoved(AncestorEvent event) {
        }        
      });
  }

  /**
   * Returns a new 3D universe that displays <code>home</code> objects.
   */
  private SimpleUniverse createUniverse(Home home) {
    // Create a universe bound to no canvas 3D
    ViewingPlatform viewingPlatform = new ViewingPlatform();
    Viewer viewer = new Viewer(new Canvas3D [0]);
    SimpleUniverse universe = new SimpleUniverse(viewingPlatform, viewer);
    
    View view = viewer.getView();
    // Update field of view from current camera
    updateView(view, home.getCamera(), home.getObserverCamera() == home.getCamera());
    
    TransformGroup viewPlatformTransform = viewingPlatform.getViewPlatformTransform();
    // Update point of view from current camera
    updateViewPlatformTransform(viewPlatformTransform, home.getCamera());
    
    // Add camera listeners to update later point of view from camera
    addCameraListeners(home, view, viewPlatformTransform);
    
    // Link scene matching home to universe
    universe.addBranchGraph(createSceneTree(home));
    
    return universe;
  }
  
  /**
   * Remove all listeners bound to home that updates 3D scene objects.
   */
  private void removeHomeListeners(Home home) {
    home.removeCameraListener(this.cameraListener);
    home.removePropertyChangeListener(Home.Property.CAMERA, this.homeCameraListener);
    home.removePropertyChangeListener(Home.Property.SKY_COLOR, this.skyColorListener);
    home.removePropertyChangeListener(Home.Property.GROUND_COLOR, this.groundColorAndTextureListener);
    home.removePropertyChangeListener(Home.Property.GROUND_TEXTURE, this.groundColorAndTextureListener);
    home.removePropertyChangeListener(Home.Property.LIGHT_COLOR, this.lightColorListener);
    home.removeWallListener(this.wallListener);
    home.removePropertyChangeListener(Home.Property.WALLS_ALPHA, this.wallsAlphaListener);
    home.removeFurnitureListener(this.furnitureListener);
  }

  /**
   * Prints this component to make it fill <code>pageFormat</code> imageable size.
   */
  public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
    if (pageIndex == 0) {     
      // Compute printed image size to render 3D view in 150 dpi
      double printSize = Math.min(pageFormat.getImageableWidth(), 
          pageFormat.getImageableHeight());
      int printedImageSize = (int)(printSize / 72 * 150);
      if (this.printedImage == null 
          || this.printedImage.getWidth() != printedImageSize) {
        SimpleUniverse printUniverse = null;
        try {
          View view;
          if (this.universe == null) {
            printUniverse = createUniverse(this.home);
            view = printUniverse.getViewer().getView();
          } else {
            view = this.universe.getViewer().getView();
          }
       
          this.printedImage = Component3DManager.getInstance().
              getOffScreenImage(view, printedImageSize, printedImageSize);
        } catch (IllegalRenderingStateException ex) {
          // If off screen canvas failed, consider that 3D view page doesn't exist
          return NO_SUCH_PAGE;
        } finally {
          if (printUniverse != null) {
            printUniverse.cleanup();
            removeHomeListeners(this.home);
          } 
        }
      }
  
      Graphics2D g2D = (Graphics2D)g.create();
      // Center the 3D view in component
      g2D.translate(pageFormat.getImageableX() + (pageFormat.getImageableWidth() - printSize) / 2, 
          pageFormat.getImageableY() + (pageFormat.getImageableHeight() - printSize) / 2);
      double scale = printSize / printedImageSize;
      g2D.scale(scale, scale);
      g2D.drawImage(this.printedImage, 0, 0, this);
      g2D.dispose();

      return PAGE_EXISTS;
    } else {
      return NO_SUCH_PAGE;
    }
  }
  
  /**
   * Adds listeners to home to update point of view from current camera.
   */
  private void addCameraListeners(final Home home, final View view, 
                                  final TransformGroup viewPlatformTransform) {
    this.cameraListener = new CameraListener() {
        public void cameraChanged(CameraEvent ev) {
          // Update view transform later to avoid flickering in case of mulitple camera changes 
          EventQueue.invokeLater(new Runnable() {
              public void run() {
                updateView(view, home.getCamera(), home.getObserverCamera() == home.getCamera());
                updateViewPlatformTransform(viewPlatformTransform, home.getCamera());
              }
            });
        }
      };
    home.addCameraListener(this.cameraListener);
    this.homeCameraListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          updateView(view, home.getCamera(), home.getObserverCamera() == home.getCamera());
          updateViewPlatformTransform(viewPlatformTransform, home.getCamera());
        }
      };
    home.addPropertyChangeListener(Home.Property.CAMERA, this.homeCameraListener);
  }

  /**
   * Updates <code>view</code> from <code>camera</code> field of view.
   */
  private void updateView(View view, Camera camera, boolean observerCamera) {
    float fieldOfView = camera.getFieldOfView();
    if (fieldOfView == 0) {
      fieldOfView = (float)(Math.PI * 63 / 180);
    }
    view.setFieldOfView(fieldOfView);
    // Use a different front clip distance for observer camera 
    // to obtain better results
    double frontClipDistance = observerCamera ? 2 : 20;
    // Update front and back clip distance to ensure their ratio is less than 3000
    view.setFrontClipDistance(frontClipDistance);
    view.setBackClipDistance(frontClipDistance * 3000);
    clearPrintedImageCache();
  }

  /**
   * Frees printed image kept in cache.
   */
  private void clearPrintedImageCache() {
    this.printedImage = null;
  }
  
  /**
   * Updates <code>viewPlatformTransform</code> transform from <code>camera</code> angles and location.
   */
  private void updateViewPlatformTransform(TransformGroup viewPlatformTransform, Camera camera) {
    Transform3D yawRotation = new Transform3D();
    yawRotation.rotY(-camera.getYaw() + Math.PI);
    
    Transform3D pitchRotation = new Transform3D();
    pitchRotation.rotX(-camera.getPitch());
    yawRotation.mul(pitchRotation);
    
    Transform3D transform = new Transform3D();
    transform.setTranslation(new Vector3f(camera.getX(), camera.getZ(), camera.getY()));
    transform.mul(yawRotation);
    
    viewPlatformTransform.setTransform(transform);
    clearPrintedImageCache();
  }
  
  /**
   * Adds AWT mouse listeners to <code>canvas3D</code> that calls back <code>controller</code> methods.  
   */
  private void addMouseListeners(final HomeController3D controller, Component canvas3D) {
    MouseInputAdapter mouseListener = new MouseInputAdapter() {
        private int xLastMouseMove;
        private int yLastMouseMove;
        
        @Override
        public void mousePressed(MouseEvent ev) {
          if (ev.isPopupTrigger()) {
            mouseReleased(ev);
          } else if (isEnabled()) {
            requestFocusInWindow();
            this.xLastMouseMove = ev.getX();
            this.yLastMouseMove = ev.getY();
          }
        }
  
        @Override
        public void mouseReleased(MouseEvent ev) {
          if (ev.isPopupTrigger()) {
            getComponentPopupMenu().show(HomeComponent3D.this, ev.getX(), ev.getY());
          } 
        }
  
        @Override
        public void mouseDragged(MouseEvent ev) {
          if (isEnabled()) {
            if (ev.isAltDown()) {
              // Mouse move along Y axis while alt is down changes camera location
              float delta = 0.5f * (this.yLastMouseMove - ev.getY());
              // Multiply delta by 10 if shift isn't down
              if (!ev.isShiftDown()) {
                delta *= 10;
              } 
              controller.moveCamera(delta);
            } else {
              final float ANGLE_FACTOR = 0.007f;
              // Mouse move along X axis changes camera yaw 
              float yawDelta = ANGLE_FACTOR * (ev.getX() - this.xLastMouseMove);
              // Multiply yaw delta by 10 if shift isn't down
              if (!ev.isShiftDown()) {
                yawDelta *= 10;
              } 
              controller.rotateCameraYaw(yawDelta);
              
              // Mouse move along Y axis changes camera pitch 
              float pitchDelta = ANGLE_FACTOR * (ev.getY() - this.yLastMouseMove);
              controller.rotateCameraPitch(pitchDelta);
            }
            
            this.xLastMouseMove = ev.getX();
            this.yLastMouseMove = ev.getY();
          }
        }
      };
    MouseWheelListener mouseWheelListener = new MouseWheelListener() {
        public void mouseWheelMoved(MouseWheelEvent ev) {
          if (isEnabled()) {
            // Mouse wheel changes camera location 
            float delta = -ev.getWheelRotation();
            // Multiply delta by 10 if shift isn't down
            if (!ev.isShiftDown()) {
              delta *= 10;
            } 
            controller.moveCamera(delta);
          }
        }
      };
    
    canvas3D.addMouseListener(mouseListener);
    canvas3D.addMouseMotionListener(mouseListener);
    canvas3D.addMouseWheelListener(mouseWheelListener);
    // Add a mouse listener to this component to request focus in case user clicks in component border
    this.addMouseListener(new MouseInputAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
          requestFocusInWindow();
        }
      });
  }

  /**
   * Installs keys bound to actions. 
   */
  private void installKeyboardActions() {
    InputMap inputMap = getInputMap(WHEN_FOCUSED);
    inputMap.put(KeyStroke.getKeyStroke("shift UP"), ActionType.MOVE_CAMERA_FORWARD);
    inputMap.put(KeyStroke.getKeyStroke("shift W"), ActionType.MOVE_CAMERA_FORWARD);
    inputMap.put(KeyStroke.getKeyStroke("UP"), ActionType.MOVE_CAMERA_FAST_FORWARD);
    inputMap.put(KeyStroke.getKeyStroke("W"), ActionType.MOVE_CAMERA_FAST_FORWARD);
    inputMap.put(KeyStroke.getKeyStroke("shift DOWN"), ActionType.MOVE_CAMERA_BACKWARD);
    inputMap.put(KeyStroke.getKeyStroke("shift S"), ActionType.MOVE_CAMERA_BACKWARD);
    inputMap.put(KeyStroke.getKeyStroke("DOWN"), ActionType.MOVE_CAMERA_FAST_BACKWARD);
    inputMap.put(KeyStroke.getKeyStroke("S"), ActionType.MOVE_CAMERA_FAST_BACKWARD);
    inputMap.put(KeyStroke.getKeyStroke("shift LEFT"), ActionType.ROTATE_CAMERA_YAW_LEFT);
    inputMap.put(KeyStroke.getKeyStroke("shift A"), ActionType.ROTATE_CAMERA_YAW_LEFT);
    inputMap.put(KeyStroke.getKeyStroke("LEFT"), ActionType.ROTATE_CAMERA_YAW_FAST_LEFT);
    inputMap.put(KeyStroke.getKeyStroke("A"), ActionType.ROTATE_CAMERA_YAW_FAST_LEFT);
    inputMap.put(KeyStroke.getKeyStroke("shift RIGHT"), ActionType.ROTATE_CAMERA_YAW_RIGHT);
    inputMap.put(KeyStroke.getKeyStroke("shift D"), ActionType.ROTATE_CAMERA_YAW_RIGHT);
    inputMap.put(KeyStroke.getKeyStroke("RIGHT"), ActionType.ROTATE_CAMERA_YAW_FAST_RIGHT);
    inputMap.put(KeyStroke.getKeyStroke("D"), ActionType.ROTATE_CAMERA_YAW_FAST_RIGHT);
    inputMap.put(KeyStroke.getKeyStroke("PAGE_UP"), ActionType.ROTATE_CAMERA_PITCH_UP);
    inputMap.put(KeyStroke.getKeyStroke("PAGE_DOWN"), ActionType.ROTATE_CAMERA_PITCH_DOWN);
  }
 
  /**
   * Creates actions that calls back <code>controller</code> methods.  
   */
  private void createActions(final HomeController3D controller) {
    // Move camera action mapped to arrow keys 
    class MoveCameraAction extends AbstractAction {
      private final int delta;
      
      public MoveCameraAction(int delta) {
        this.delta = delta;
      }

      public void actionPerformed(ActionEvent e) {
        controller.moveCamera(this.delta);
      }
    }
    // Rotate camera yaw action mapped to arrow keys 
    class RotateCameraYawAction extends AbstractAction {
      private final float delta;
      
      public RotateCameraYawAction(float delta) {
        this.delta = delta;
      }

      public void actionPerformed(ActionEvent e) {
        controller.rotateCameraYaw(this.delta);
      }
    }
    // Rotate camera pitch action mapped to arrow keys 
    class RotateCameraPitchAction extends AbstractAction {
      private final float delta;
      
      public RotateCameraPitchAction(float delta) {
        this.delta = delta;
      }

      public void actionPerformed(ActionEvent e) {
        controller.rotateCameraPitch(this.delta);
      }
    }
    ActionMap actionMap = getActionMap();
    actionMap.put(ActionType.MOVE_CAMERA_FORWARD, new MoveCameraAction(1));
    actionMap.put(ActionType.MOVE_CAMERA_FAST_FORWARD, new MoveCameraAction(10));
    actionMap.put(ActionType.MOVE_CAMERA_BACKWARD, new MoveCameraAction(-1));
    actionMap.put(ActionType.MOVE_CAMERA_FAST_BACKWARD, new MoveCameraAction(-10));
    actionMap.put(ActionType.ROTATE_CAMERA_YAW_LEFT, new RotateCameraYawAction(-(float)Math.PI / 180));
    actionMap.put(ActionType.ROTATE_CAMERA_YAW_FAST_LEFT, new RotateCameraYawAction(-(float)Math.PI / 18));
    actionMap.put(ActionType.ROTATE_CAMERA_YAW_RIGHT, new RotateCameraYawAction((float)Math.PI / 180));
    actionMap.put(ActionType.ROTATE_CAMERA_YAW_FAST_RIGHT, new RotateCameraYawAction((float)Math.PI / 18));
    actionMap.put(ActionType.ROTATE_CAMERA_PITCH_UP, new RotateCameraPitchAction(-(float)Math.PI / 180));
    actionMap.put(ActionType.ROTATE_CAMERA_PITCH_DOWN, new RotateCameraPitchAction((float)Math.PI / 180));
  }

  /**
   * Returns a new scene tree root.
   */
  private BranchGroup createSceneTree(Home home) {
    BranchGroup root = new BranchGroup();

    // Build scene tree
    root.addChild(createHomeTree(home));
    root.addChild(createBackgroundNode(home));
    root.addChild(createGroundNode(home, -1E5f / 2, -1E5f / 2, 1E5f, 1E5f));
    for (Light light : createLights(home)) {
      root.addChild(light);
    }

    return root;
  }

  /**
   * Returns a new background node.  
   */
  private Node createBackgroundNode(final Home home) {
    final Background background = new Background();
    updateBackgroundColor(background, home);
    // Allow background color to change
    background.setCapability(Background.ALLOW_COLOR_WRITE);
    background.setApplicationBounds(new BoundingBox(
        new Point3d(-100000, -100000, -100000), 
        new Point3d(100000, 100000, 100000)));
    
    // Add a listener on sky color property change to home
    this.skyColorListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          updateBackgroundColor(background, home);
        }
      };
    home.addPropertyChangeListener(Home.Property.SKY_COLOR, this.skyColorListener);
    return background;
  }

  /**
   * Updates<code>background</code> color from <code>home</code> sky color.
   */
  private void updateBackgroundColor(Background background, Home home) {
    background.setColor(new Color3f(new Color(home.getSkyColor())));
    clearPrintedImageCache();
  }
  
  /**
   * Returns a new ground node.  
   */
  private Node createGroundNode(final Home home,
                                final float groundOriginX,
                                final float groundOriginY,
                                final float groundWidth,
                                final float groundDepth) {
    // Use coloring attributes for ground to avoid ground lighting
    ColoringAttributes groundColoringAttributes = new ColoringAttributes();
    groundColoringAttributes.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
    
    Appearance groundAppearance = new Appearance();
    groundAppearance.setColoringAttributes(groundColoringAttributes);
    groundAppearance.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_READ);
    groundAppearance.setCapability(Appearance.ALLOW_TEXTURE_WRITE);

    final Shape3D groundShape = new Shape3D();
    groundShape.setAppearance(groundAppearance);
    groundShape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
    groundShape.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
    
    updateGroundColorAndTexture(groundShape, home, 
        groundOriginX, groundOriginY, groundWidth, groundDepth);
    
    // Add a listener on ground color and texture property change to home
    this.groundColorAndTextureListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          updateGroundColorAndTexture(groundShape, home, 
              groundOriginX, groundOriginY, groundWidth, groundDepth);
        }
      };
    home.addPropertyChangeListener(Home.Property.GROUND_COLOR, this.groundColorAndTextureListener); 
    home.addPropertyChangeListener(Home.Property.GROUND_TEXTURE, this.groundColorAndTextureListener); 
    
    return groundShape;
  }
  
  /**
   * Updates ground coloring and texture attributes from <code>home</code> ground color and texture.
   */
  private void updateGroundColorAndTexture(Shape3D groundShape, 
                                           Home home, 
                                           float groundOriginX,
                                           float groundOriginY,
                                           float groundWidth,
                                           float groundDepth) {
    Color3f groundColor = new Color3f(new Color(home.getGroundColor()));
    final Appearance groundAppearance = groundShape.getAppearance();
    groundAppearance.getColoringAttributes().setColor(groundColor);
    HomeTexture groundTexture = home.getGroundTexture();
    if (groundTexture != null) {
      final TextureManager imageManager = TextureManager.getInstance();
      imageManager.loadTexture(groundTexture.getImage(), 
          new TextureManager.TextureObserver() {
              public void textureUpdated(Texture texture) {
                groundAppearance.setTexture(texture);
              }
            });
    } else {
      groundAppearance.setTexture(null);
    }
    
    // Create ground geometry
    Point3f [] coords = {new Point3f(groundOriginX, 0, groundOriginY), 
                         new Point3f(groundOriginX, 0, groundOriginY + groundDepth),
                         new Point3f(groundOriginX + groundWidth, 0, groundOriginY + groundDepth),
                         new Point3f(groundOriginX + groundWidth, 0, groundOriginY)};
    GeometryInfo geometryInfo = new GeometryInfo(GeometryInfo.QUAD_ARRAY);
    geometryInfo.setCoordinates (coords);

    // Compute ground texture coordinates
    if (groundTexture != null) {
      TexCoord2f [] textureCoords = {new TexCoord2f(0, 0),
                                     new TexCoord2f(0, groundDepth / groundTexture.getHeight()),
                                     new TexCoord2f(groundWidth / groundTexture.getWidth(), groundDepth / groundTexture.getHeight()),
                                     new TexCoord2f(groundWidth / groundTexture.getWidth(), 0)};
      geometryInfo.setTextureCoordinateParams(1, 2);
      geometryInfo.setTextureCoordinates(0, textureCoords);
    }
    
    groundShape.setGeometry(geometryInfo.getIndexedGeometryArray());

    clearPrintedImageCache();
  }
  
  /**
   * Returns the lights of the scene.
   */
  private Light [] createLights(final Home home) {
    final Light [] lights = {
        new DirectionalLight(new Color3f(), new Vector3f(1.5f, -0.8f, -1)),         
        new DirectionalLight(new Color3f(), new Vector3f(-1.5f, -0.8f, -1)), 
        new DirectionalLight(new Color3f(), new Vector3f(0, -0.8f, 1)), 
        new AmbientLight(new Color3f(0.2f, 0.2f, 0.2f))}; 
    for (int i = 0; i < lights.length - 1; i++) {
      updateLightColor(lights [i], home);
      // Allow directional lights color to change
      lights [i].setCapability(DirectionalLight.ALLOW_COLOR_WRITE);
    }
    
    for (Light light : lights) {
      light.setInfluencingBounds(new BoundingSphere(new Point3d(), 10000));
    }
    
    // Add a listener on light color property change to home
    this.lightColorListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          for (int i = 0; i < lights.length - 1; i++) {
            updateLightColor(lights [i], home);
          }
        }
      };
    home.addPropertyChangeListener(Home.Property.LIGHT_COLOR, this.lightColorListener); 
    
    return lights;
  }

  /**
   * Updates<code>light</code> color from <code>home</code> light color.
   */
  private void updateLightColor(Light light, Home home) {
    light.setColor(new Color3f(new Color(home.getLightColor())));
    clearPrintedImageCache();
  }
  
  /**
   * Exports this 3D view to the given OBJ file.
   */
  public boolean exportToOBJ(String objName) {
    String mtlName = objName.substring(0, objName.length() - 4) + ".mtl";
    try {
      Writer writer = new OutputStreamWriter(
          new BufferedOutputStream(new FileOutputStream(objName)), "ISO-8859-1");      
      String header = "#\n# Generated by Sweet Home 3D - " + new Date() + "\n# http://sweethome3d.sourceforge.net/\n#\n";
      writer.write(header);
      writer.write("mtllib " + new File(mtlName).getName() + "\n");
      
      AtomicInteger vertexOffset = new AtomicInteger();
      AtomicInteger normalOffset = new AtomicInteger();
      AtomicInteger textureCoordinatesOffset = new AtomicInteger();
      Map<ComparableAppearance, String> appearances = new LinkedHashMap<ComparableAppearance, String>();
      
      if (this.home.getWalls().size() > 0) {
        // Create a not alive new ground to be able to explore its coordinates without setting capabilities
        Rectangle2D homeBounds = computeExportedHomeBounds();
        Node groundNode = createGroundNode(this.home, 
            (float)homeBounds.getX(), (float)homeBounds.getY(), 
            (float)homeBounds.getWidth(), (float)homeBounds.getHeight());
        writeNode(writer, groundNode, "ground", vertexOffset, normalOffset, textureCoordinatesOffset, appearances);
      }
      
      // Write 3D walls 
      int i = 0;
      for (Wall wall : this.home.getWalls()) {
        // Create a not alive new wall to be able to explore its coordinates without setting capabilities 
        Wall3D wallNode = new Wall3D(wall, this.home);
        String objectName = "wall_" + ++i;
        writeNode(writer, wallNode, objectName, vertexOffset, normalOffset, textureCoordinatesOffset, appearances);
      }
      // Write 3D furniture 
      i = 0;
      for (HomePieceOfFurniture piece : this.home.getFurniture()) {
        // Create a not alive new piece to be able to explore its coordinates without setting capabilities
        HomePieceOfFurniture3D pieceNode = new HomePieceOfFurniture3D(piece, true);
        String objectName = "piece_" + ++i;
        writeNode(writer, pieceNode, objectName, vertexOffset, normalOffset, textureCoordinatesOffset, appearances);
      }
      writer.close();
      
      exportAppearancesToMTL(mtlName, header, appearances);
      
      return true;
    } catch (IOException ex) {
      return false;
    } 
  }
  
  /**
   * Exports a set of appearance to the given file.  
   */
  private void exportAppearancesToMTL(String mtlName, String header, 
                                      Map<ComparableAppearance, String> appearances) throws IOException {
    Writer writer = null;
    try {
      writer = new OutputStreamWriter(
          new BufferedOutputStream(new FileOutputStream(mtlName)), "ISO-8859-1");      
      writer.write(header);
      
      for (Map.Entry<ComparableAppearance, String> appearanceEntry : appearances.entrySet()) {
        Appearance appearance = appearanceEntry.getKey().getAppearance();        
        String appearanceName = appearanceEntry.getValue();
        writer.write("\nnewmtl " + appearanceName + "\n");
        Material material = appearance.getMaterial();
        if (material != null) {
          writer.write("illum 1\n");
          Color3f color = new Color3f();
          material.getAmbientColor(color);          
          writer.write("Ka " + color.getX() + " " + color.getY() + " " + color.getZ() + "\n");
          material.getDiffuseColor(color);          
          writer.write("Kd " + color.getX() + " " + color.getY() + " " + color.getZ() + "\n");
          material.getSpecularColor(color);          
          writer.write("Ks " + color.getX() + " " + color.getY() + " " + color.getZ() + "\n");
          writer.write("Ns " + material.getShininess() + "\n");
        } else {
          ColoringAttributes coloringAttributes = appearance.getColoringAttributes();
          if (coloringAttributes != null) {
            writer.write("illum 0\n");
            Color3f color = new Color3f();
            coloringAttributes.getColor(color);          
            writer.write("Ka " + color.getX() + " " + color.getY() + " " + color.getZ() + "\n");
            writer.write("Kd " + color.getX() + " " + color.getY() + " " + color.getZ() + "\n");
            writer.write("Ks " + color.getX() + " " + color.getY() + " " + color.getZ() + "\n");
          }
        }
        TransparencyAttributes transparency = appearance.getTransparencyAttributes();
        if (transparency != null && transparency.getTransparency() != 0) {
          writer.write("Ni 1\n");
          writer.write("d " + transparency.getTransparency() + "\n");
        }
        Texture texture = appearance.getTexture();
        if (texture != null) {
          ImageComponent2D imageComponent = (ImageComponent2D)texture.getImage(0);
          RenderedImage image = imageComponent.getRenderedImage();
          File imageFile = new File(mtlName.substring(0, mtlName.length() - 4) + "_" + appearanceName + ".jpg");
          ImageIO.write(image, "JPEG", imageFile);
          writer.write("map_Kd " + imageFile.getName() + "\n");
        }
      }      
    } finally {
      if (writer != null) {
        writer.close();
      }
    }
  }

  /**
   * Returns home bounds. 
   */
  private Rectangle2D computeExportedHomeBounds() {
    Rectangle2D homeBounds = null;
    // Compute plan bounds to include walls and furniture
    for (Wall wall : home.getWalls()) {
      if (homeBounds == null) {
        homeBounds = new Rectangle2D.Float(wall.getXStart(), wall.getYStart(), 0, 0);
      } else {
        homeBounds.add(wall.getXStart(), wall.getYStart());
      }
      homeBounds.add(wall.getXEnd(), wall.getYEnd());
    }
    for (HomePieceOfFurniture piece : home.getFurniture()) {
      if (piece.isVisible()) {
        for (float [] point : piece.getPoints()) {
          if (homeBounds == null) {
            homeBounds = new Rectangle2D.Float(point [0], point [1], 0, 0);
          } else {
            homeBounds.add(point [0], point [1]);
          }
        }
      }
    }
    return homeBounds;
  }
  
  /**
   * Writes  all the shapes children of <code>node</code> at OBJ format. 
   */
  private void writeNode(Writer writer, Node node, 
                         String nodeName, AtomicInteger vertexOffset, 
                         AtomicInteger normalOffset, 
                         AtomicInteger textureCoordinatesOffset, 
                         Map<ComparableAppearance, String> appearances) throws IOException {
    if (node instanceof Group) {
      // Write all children
      Enumeration enumeration = ((Group)node).getAllChildren(); 
      while (enumeration.hasMoreElements()) {
        writeNode(writer, (Node)enumeration.nextElement(), nodeName, 
            vertexOffset, normalOffset, textureCoordinatesOffset, appearances);
      }
    } else if (node instanceof Shape3D) {
      Shape3D shape = (Shape3D)node;
      Appearance appearance = shape.getAppearance();
      // Retrieve transformation needed to be applied to vertices
      Transform3D transformToRoot = new Transform3D();
      shape.getLocalToVworld(transformToRoot);
      // Build a unique object name
      String shapeName = null;
      if (shape.getUserData() instanceof String) {
        shapeName = (String)shape.getUserData(); 
      }
      String objectName;
      if (shapeName != null && accept(shapeName)) {
        objectName = nodeName + "_" + vertexOffset + "_" + shapeName;
      } else {
        objectName = nodeName + "_" + vertexOffset;
      }
      
      writer.write("g " + objectName + "\n");
      if (appearance != null) {
        ComparableAppearance comparableAppearance = new ComparableAppearance(appearance);
        String appearanceName = appearances.get(comparableAppearance);
        if (appearanceName == null) {
          // Store appearance
          appearanceName = objectName;
          appearances.put(comparableAppearance, appearanceName);
        } 
        writer.write("usemtl " + appearanceName + "\n");
      }
      // Write object geometries
      for (int i = 0, n = shape.numGeometries(); i < n; i++) {
        writeNodeGeometry(writer, shape.getGeometry(i), 
            vertexOffset, normalOffset, textureCoordinatesOffset, transformToRoot);
      }
    }    
  }
  
  public boolean accept(String shapeName) {
    for (int i = 0; i < shapeName.length(); i++) {
      char car = shapeName.charAt(i);
      if (!(car >= 'a' && car < 'z'
            || car >= 'A' && car < 'Z'
            || car == '_')) {
        return false;
      }
    }
    return true;
  }

  /**
   * Writes a 3D geometry at OBJ format.
   */
  private void writeNodeGeometry(Writer writer, Geometry geometry, 
                                 AtomicInteger vertexOffset, 
                                 AtomicInteger normalOffset, 
                                 AtomicInteger textureCoordinatesOffset, 
                                 Transform3D transformToRoot) throws IOException {
    if (geometry instanceof GeometryArray) {
      GeometryArray geometryArray = (GeometryArray)geometry;      
      int vertexOffsetValue = vertexOffset.get() + 1;
      int normalOffsetValue = normalOffset.get() + 1;
      int textureCoordinatesOffsetValue = textureCoordinatesOffset.get() + 1;      
      
      Map<Point3f, Integer> vertexIndices = new HashMap<Point3f, Integer>();
      int [] vertexIndexSubstitutes = new int [geometryArray.getVertexCount()];
      
      boolean normalsDefined = (geometryArray.getVertexFormat() & GeometryArray.NORMALS) != 0;
      Map<Vector3f, Integer> normalIndices = new HashMap<Vector3f, Integer>();
      int [] normalIndexSubstitutes = new int [geometryArray.getVertexCount()];
      
      boolean textureCoordinatesDefined = (geometryArray.getVertexFormat() & GeometryArray.TEXTURE_COORDINATE_2) != 0;
      Map<TexCoord2f, Integer> textureCoordinatesIndices = new HashMap<TexCoord2f, Integer>();
      int [] textureCoordinatesIndexSubstitutes = new int [geometryArray.getVertexCount()];
      
      if ((geometryArray.getVertexFormat() & GeometryArray.BY_REFERENCE) != 0) {
        if ((geometryArray.getVertexFormat() & GeometryArray.INTERLEAVED) != 0) {
          float [] vertexData = geometryArray.getInterleavedVertices();
          int vertexSize = vertexData.length / geometryArray.getVertexCount();
          // Write vertices coordinates 
          for (int index = 0, i = vertexSize - 3, n = geometryArray.getVertexCount(); 
               index < n; index++, i += vertexSize) {
            Point3f vertex = new Point3f(vertexData [i], vertexData [i + 1], vertexData [i + 2]);
            writeVertex(writer, transformToRoot, vertex, index,
                vertexIndices, vertexIndexSubstitutes);
          }
          // Write normals
          if (normalsDefined) {
            for (int index = 0, i = vertexSize - 6, n = geometryArray.getVertexCount(); 
                 index < n; index++, i += vertexSize) {
              Vector3f normal = new Vector3f(vertexData [i], vertexData [i + 1], vertexData [i + 2]);
              writeNormal(writer, transformToRoot, normal, index,
                  normalIndices, normalIndexSubstitutes);
            }
          }
          // Write texture coordinates
          if (textureCoordinatesDefined) {
            for (int index = 0, i = 0, n = geometryArray.getVertexCount(); 
                  index < n; index++, i += vertexSize) {
              TexCoord2f textureCoordinates = new TexCoord2f(vertexData [i], vertexData [i + 1]);
              writeTextureCoordinates(writer, textureCoordinates, index, textureCoordinatesIndices, textureCoordinatesIndexSubstitutes);
            }
          }
        } else {
          // Write vertices coordinates
          float [] vertexCoordinates = geometryArray.getCoordRefFloat();
          for (int index = 0, i = 0, n = geometryArray.getVertexCount(); index < n; index++, i += 3) {
            Point3f vertex = new Point3f(vertexCoordinates [i], vertexCoordinates [i + 1], vertexCoordinates [i + 2]);
            writeVertex(writer, transformToRoot, vertex, index,
                vertexIndices, vertexIndexSubstitutes);
          }
          // Write normals
          if (normalsDefined) {
            float [] normalCoordinates = geometryArray.getNormalRefFloat();
            for (int index = 0, i = 0, n = geometryArray.getVertexCount(); index < n; index++, i += 3) {
              Vector3f normal = new Vector3f(normalCoordinates [i], normalCoordinates [i + 1], normalCoordinates [i + 2]);
              writeNormal(writer, transformToRoot, normal, index,
                  normalIndices, normalIndexSubstitutes);
            }
          }
          // Write texture coordinates
          if (textureCoordinatesDefined) {
            float [] textureCoordinatesArray = geometryArray.getTexCoordRefFloat(0);
            for (int index = 0, i = 0, n = geometryArray.getVertexCount(); index < n; index++, i += 2) {
              TexCoord2f textureCoordinates = new TexCoord2f(textureCoordinatesArray [i], textureCoordinatesArray [i + 1]);
              writeTextureCoordinates(writer, textureCoordinates, index, textureCoordinatesIndices, textureCoordinatesIndexSubstitutes);
            }
          }
        }
      } else {
        // Write vertices coordinates
        for (int index = 0, n = geometryArray.getVertexCount(); index < n; index++) {
          Point3f vertex = new Point3f();
          geometryArray.getCoordinate(index, vertex);
          writeVertex(writer, transformToRoot, vertex, index,
              vertexIndices, vertexIndexSubstitutes);
        }
        // Write normals
        if (normalsDefined) {
          for (int index = 0, n = geometryArray.getVertexCount(); index < n; index++) {
            Vector3f normal = new Vector3f();
            geometryArray.getNormal(index, normal);
            writeNormal(writer, transformToRoot, normal, index,
                normalIndices, normalIndexSubstitutes);
          }
        }
        // Write texture coordinates
        if (textureCoordinatesDefined) {
          for (int index = 0, n = geometryArray.getVertexCount(); index < n; index++) {
            TexCoord2f textureCoordinates = new TexCoord2f();
            geometryArray.getTextureCoordinate(0, index, textureCoordinates);
            writeTextureCoordinates(writer, textureCoordinates, index, textureCoordinatesIndices, textureCoordinatesIndexSubstitutes);
          }
        }
      }

      // Write triangles or quadrilaterals depending on the geometry
      if (geometryArray instanceof IndexedGeometryArray) {
        if (geometryArray instanceof IndexedTriangleArray) {
          IndexedTriangleArray triangleArray = (IndexedTriangleArray)geometryArray;
          for (int i = 0, n = triangleArray.getIndexCount(); i < n; i += 3) {
            writeIndexedTriangle(writer, triangleArray, i, i + 1, i + 2, 
                vertexIndexSubstitutes, vertexOffsetValue,  
                normalIndexSubstitutes, normalOffsetValue, 
                textureCoordinatesIndexSubstitutes, textureCoordinatesOffsetValue);
          }
        } else if (geometryArray instanceof IndexedQuadArray) {
          IndexedQuadArray quadArray = (IndexedQuadArray)geometryArray;
          for (int i = 0, n = quadArray.getIndexCount(); i < n; i += 4) {
            writeIndexedQuadrilateral(writer, quadArray, i, i + 1, i + 2, i + 3, 
                vertexIndexSubstitutes, vertexOffsetValue,  
                normalIndexSubstitutes, normalOffsetValue,  
                textureCoordinatesIndexSubstitutes, textureCoordinatesOffsetValue);
          }
        } else if (geometryArray instanceof IndexedTriangleStripArray) {
          IndexedTriangleStripArray triangleStripArray = (IndexedTriangleStripArray)geometryArray;
          int [] stripVertexCount = new int [triangleStripArray.getNumStrips()];
          triangleStripArray.getStripIndexCounts(stripVertexCount);
          int initialIndex = 0;
          for (int strip = 0; strip < stripVertexCount.length; strip++) {
            for (int i = initialIndex, n = initialIndex + stripVertexCount [strip] - 2, j = 0; i < n; i++, j++) {
              if (j % 2 == 0) {
                writeIndexedTriangle(writer, triangleStripArray, i, i + 1, i + 2, 
                    vertexIndexSubstitutes, vertexOffsetValue, 
                    normalIndexSubstitutes, normalOffsetValue,  
                    textureCoordinatesIndexSubstitutes, textureCoordinatesOffsetValue);
              } else { // Vertices of odd triangles are in reverse order               
                writeIndexedTriangle(writer, triangleStripArray, i, i + 2, i + 1, 
                    vertexIndexSubstitutes, vertexOffsetValue, 
                    normalIndexSubstitutes, normalOffsetValue,  
                    textureCoordinatesIndexSubstitutes, textureCoordinatesOffsetValue);
              }
            }
            initialIndex += stripVertexCount [strip];
          }
        } else if (geometryArray instanceof IndexedTriangleFanArray) {
          IndexedTriangleFanArray triangleFanArray = (IndexedTriangleFanArray)geometryArray;
          int [] stripVertexCount = new int [triangleFanArray.getNumStrips()];
          triangleFanArray.getStripIndexCounts(stripVertexCount);
          int initialIndex = 0;
          for (int strip = 0; strip < stripVertexCount.length; strip++) {
            for (int i = initialIndex, n = initialIndex + stripVertexCount [strip] - 2; i < n; i++) {
              writeIndexedTriangle(writer, triangleFanArray, initialIndex, i + 1, i + 2, 
                  vertexIndexSubstitutes, vertexOffsetValue,  
                  normalIndexSubstitutes, normalOffsetValue,  
                  textureCoordinatesIndexSubstitutes, textureCoordinatesOffsetValue);
            }
            initialIndex += stripVertexCount [strip];
          }
        } 
      } else {
        if (geometryArray instanceof TriangleArray) {
          TriangleArray triangleArray = (TriangleArray)geometryArray;
          for (int i = 0, n = triangleArray.getVertexCount(); i < n; i += 3) {
            writeTriangle(writer, triangleArray, i, i + 1, i + 2, 
                vertexIndexSubstitutes, vertexOffsetValue,  
                normalIndexSubstitutes, normalOffsetValue,  
                textureCoordinatesIndexSubstitutes, textureCoordinatesOffsetValue);
          }
        } else if (geometryArray instanceof QuadArray) {
          QuadArray quadArray = (QuadArray)geometryArray;
          for (int i = 0, n = quadArray.getVertexCount(); i < n; i += 4) {
            writeQuadrilateral(writer, quadArray, i, i + 1, i + 2, i + 3, 
                vertexIndexSubstitutes, vertexOffsetValue,  
                normalIndexSubstitutes, normalOffsetValue,  
                textureCoordinatesIndexSubstitutes, textureCoordinatesOffsetValue);
          }
        } else if (geometryArray instanceof TriangleStripArray) {
          TriangleStripArray triangleStripArray = (TriangleStripArray)geometryArray;
          int [] stripVertexCount = new int [triangleStripArray.getNumStrips()];
          triangleStripArray.getStripVertexCounts(stripVertexCount);
          int initialIndex = 0;
          for (int strip = 0; strip < stripVertexCount.length; strip++) {
            for (int i = initialIndex, n = initialIndex + stripVertexCount [strip] - 2, j = 0; i < n; i++, j++) {
              if (j % 2 == 0) {
                writeTriangle(writer, triangleStripArray, i, i + 1, i + 2, 
                    vertexIndexSubstitutes, vertexOffsetValue,  
                    normalIndexSubstitutes, normalOffsetValue,  
                    textureCoordinatesIndexSubstitutes, textureCoordinatesOffsetValue);
              } else { // Vertices of odd triangles are in reverse order               
                writeTriangle(writer, triangleStripArray, i, i + 2, i + 1, 
                    vertexIndexSubstitutes, vertexOffsetValue,  
                    normalIndexSubstitutes, normalOffsetValue,  
                    textureCoordinatesIndexSubstitutes, textureCoordinatesOffsetValue);
              }
            }
            initialIndex += stripVertexCount [strip];
          }
        } else if (geometryArray instanceof TriangleFanArray) {
          TriangleFanArray triangleFanArray = (TriangleFanArray)geometryArray;
          int [] stripVertexCount = new int [triangleFanArray.getNumStrips()];
          triangleFanArray.getStripVertexCounts(stripVertexCount);
          int initialIndex = 0;
          for (int strip = 0; strip < stripVertexCount.length; strip++) {
            for (int i = initialIndex, n = initialIndex + stripVertexCount [strip] - 2; i < n; i++) {
              writeTriangle(writer, triangleFanArray, initialIndex, i + 1, i + 2, 
                  vertexIndexSubstitutes, vertexOffsetValue,  
                  normalIndexSubstitutes, normalOffsetValue,  
                  textureCoordinatesIndexSubstitutes, textureCoordinatesOffsetValue);
            }
            initialIndex += stripVertexCount [strip];
          }
        }
      }
      
      vertexOffset.set(vertexOffset.get() + vertexIndices.size());
      if (normalsDefined) {
        normalOffset.set(normalOffset.get() + normalIndices.size());
      }        
      if (textureCoordinatesDefined) {
        textureCoordinatesOffset.set(textureCoordinatesOffset.get() + textureCoordinatesIndices.size());
      } 
    } 
  }

  /**
   * Applies to <code>vertex</code> the given transformation, and writes it in
   * a line v at OBJ format, if the vertex isn't a key of <code>vertexIndices</code> yet.  
   */
  private void writeVertex(Writer writer,
                           Transform3D transformToRoot,
                           Point3f vertex, int index,
                           Map<Point3f, Integer> vertexIndices,
                           int [] vertexIndexSubstitutes) throws IOException {
    transformToRoot.transform(vertex);
    Integer vertexIndex = vertexIndices.get(vertex);
    if (vertexIndex == null) {
      vertexIndexSubstitutes [index] = vertexIndices.size();
      vertexIndices.put(vertex, vertexIndexSubstitutes [index]);
      // Write only once unique vertices
      if (OBJ_REDUCE_FILE_SIZE) {
        writer.write("v " + OBJ_NUMBER_FORMAT.format(vertex.x) 
            + " " + OBJ_NUMBER_FORMAT.format(vertex.y) 
            + " " + OBJ_NUMBER_FORMAT.format(vertex.z) + "\n");
      } else {
        writer.write("v " + vertex.x + " " + vertex.y + " " + vertex.z + "\n");
      }
    } else {
      vertexIndexSubstitutes [index] = vertexIndex;
    }
  }

  /**
   * Applies to <code>normal</code> the given transformation, and writes it in
   * a line vn at OBJ format, if the normal isn't a key of <code>normalIndices</code> yet.  
   */
  private void writeNormal(Writer writer,
                           Transform3D transformToRoot,
                           Vector3f normal, int index,
                           Map<Vector3f, Integer> normalIndices,
                           int [] normalIndexSubstitutes) throws IOException {
    transformToRoot.transform(normal);
    Integer normalIndex = normalIndices.get(normal);
    if (normalIndex == null) {
      normalIndexSubstitutes [index] = normalIndices.size();
      normalIndices.put(normal, normalIndexSubstitutes [index]);
      // Write only once unique normals
      if (OBJ_REDUCE_FILE_SIZE) {
        writer.write("vn " + OBJ_NUMBER_FORMAT.format(normal.x) 
            + " " + OBJ_NUMBER_FORMAT.format(normal.y) 
            + " " + OBJ_NUMBER_FORMAT.format(normal.z) + "\n");
      } else {
        writer.write("vn " + normal.x + " " + normal.y + " " + normal.z + "\n");
      }
    } else {
      normalIndexSubstitutes [index] = normalIndex;
    }
  }

  /**
   * Writes <code>textureCoordinates</code> in a line vt at OBJ format, 
   * if the texture coordinates isn't a key of <code>textureCoordinatesIndices</code> yet.  
   */
  private void writeTextureCoordinates(Writer writer,
                                       TexCoord2f textureCoordinates, int index,
                                       Map<TexCoord2f, Integer> textureCoordinatesIndices,
                                       int [] textureCoordinatesIndexSubstitutes) throws IOException {
    Integer textureCoordinatesIndex = textureCoordinatesIndices.get(textureCoordinates);
    if (textureCoordinatesIndex == null) {
      textureCoordinatesIndexSubstitutes [index] = textureCoordinatesIndices.size();
      textureCoordinatesIndices.put(textureCoordinates, textureCoordinatesIndexSubstitutes [index]);
      // Write only once unique texture coordinates
      if (OBJ_REDUCE_FILE_SIZE) {
        writer.write("vt " + OBJ_NUMBER_FORMAT.format(textureCoordinates.x) 
            + " " + OBJ_NUMBER_FORMAT.format(textureCoordinates.y) + " 0\n");
      } else {
        writer.write("vt " + textureCoordinates.x + " " + textureCoordinates.y + " 0\n");
      }
    } else {
      textureCoordinatesIndexSubstitutes [index] = textureCoordinatesIndex;
    }
  }

  /**
   * Writes the triangle indices given at vertexIndex1, vertexIndex2, vertexIndex3, 
   * in a line f at OBJ format. 
   */
  private void writeIndexedTriangle(Writer writer, IndexedGeometryArray geometryArray, 
                                    int vertexIndex1, int vertexIndex2, int vertexIndex3, 
                                    int [] vertexIndexSubstitutes, int vertexOffset, 
                                    int [] normalIndexSubstitutes, int normalOffset,                                     
                                    int [] textureCoordinatesIndexSubstitutes, int textureCoordinatesOffset) throws IOException {
    if ((geometryArray.getVertexFormat() & GeometryArray.TEXTURE_COORDINATE_2) != 0) {
      if ((geometryArray.getVertexFormat() & GeometryArray.NORMALS) != 0) {
        writer.write("f " + (vertexOffset + vertexIndexSubstitutes [geometryArray.getCoordinateIndex(vertexIndex1)]) 
            + "/" + (textureCoordinatesOffset + textureCoordinatesIndexSubstitutes [geometryArray.getTextureCoordinateIndex(0, vertexIndex1)]) 
            + "/" + (normalOffset + normalIndexSubstitutes [geometryArray.getNormalIndex(vertexIndex1)]) 
            + " " + (vertexOffset + vertexIndexSubstitutes [geometryArray.getCoordinateIndex(vertexIndex2)]) 
            + "/" + (textureCoordinatesOffset + textureCoordinatesIndexSubstitutes [geometryArray.getTextureCoordinateIndex(0, vertexIndex2)]) 
            + "/" + (normalOffset + normalIndexSubstitutes [geometryArray.getNormalIndex(vertexIndex2)]) 
            + " " + (vertexOffset + vertexIndexSubstitutes [geometryArray.getCoordinateIndex(vertexIndex3)]) 
            + "/" + (textureCoordinatesOffset + textureCoordinatesIndexSubstitutes [geometryArray.getTextureCoordinateIndex(0, vertexIndex3)]) 
            + "/" + (normalOffset + normalIndexSubstitutes [geometryArray.getNormalIndex(vertexIndex3)]) + "\n");
      } else {
        writer.write("f " + (vertexOffset + vertexIndexSubstitutes [geometryArray.getCoordinateIndex(vertexIndex1)]) 
            + "/" + (textureCoordinatesOffset + textureCoordinatesIndexSubstitutes [geometryArray.getTextureCoordinateIndex(0, vertexIndex1)]) 
            + " " + (vertexOffset + vertexIndexSubstitutes [geometryArray.getCoordinateIndex(vertexIndex2)]) 
            + "/" + (textureCoordinatesOffset + textureCoordinatesIndexSubstitutes [geometryArray.getTextureCoordinateIndex(0, vertexIndex2)]) 
            + " " + (vertexOffset + vertexIndexSubstitutes [geometryArray.getCoordinateIndex(vertexIndex3)]) 
            + "/" + (textureCoordinatesOffset + textureCoordinatesIndexSubstitutes [geometryArray.getTextureCoordinateIndex(0, vertexIndex3)]) + "\n");
      }
    } else {
      if ((geometryArray.getVertexFormat() & GeometryArray.NORMALS) != 0) {
        writer.write("f " + (vertexOffset + vertexIndexSubstitutes [geometryArray.getCoordinateIndex(vertexIndex1)]) 
            + "//" + (normalOffset + normalIndexSubstitutes [geometryArray.getNormalIndex(vertexIndex1)]) 
            + " "  + (vertexOffset + vertexIndexSubstitutes [geometryArray.getCoordinateIndex(vertexIndex2)]) 
            + "//" + (normalOffset + normalIndexSubstitutes [geometryArray.getNormalIndex(vertexIndex2)]) 
            + " "  + (vertexOffset + vertexIndexSubstitutes [geometryArray.getCoordinateIndex(vertexIndex3)]) 
            + "//" + (normalOffset + normalIndexSubstitutes [geometryArray.getNormalIndex(vertexIndex3)]) + "\n");
      } else {
        writer.write("f " + (vertexOffset + vertexIndexSubstitutes [geometryArray.getCoordinateIndex(vertexIndex1)]) 
            + " "  + (vertexOffset + vertexIndexSubstitutes [geometryArray.getCoordinateIndex(vertexIndex2)]) 
            + " "  + (vertexOffset + vertexIndexSubstitutes [geometryArray.getCoordinateIndex(vertexIndex3)]) + "\n");
      }
    }
  }
  
  /**
   * Writes the quadrilateral indices given at vertexIndex1, vertexIndex2, vertexIndex3, vertexIndex4,
   * in a line f at OBJ format. 
   */
  private void writeIndexedQuadrilateral(Writer writer, IndexedGeometryArray geometryArray, 
                                         int vertexIndex1, int vertexIndex2, int vertexIndex3, int vertexIndex4, 
                                         int [] vertexIndexSubstitutes, int vertexOffset, 
                                         int [] normalIndexSubstitutes, int normalOffset,                                      
                                         int [] textureCoordinatesIndexSubstitutes, int textureCoordinatesOffset) throws IOException {
    if ((geometryArray.getVertexFormat() & GeometryArray.TEXTURE_COORDINATE_2) != 0) {
      if ((geometryArray.getVertexFormat() & GeometryArray.NORMALS) != 0) {
        writer.write("f " + (vertexOffset + vertexIndexSubstitutes [geometryArray.getCoordinateIndex(vertexIndex1)]) 
            + "/" + (textureCoordinatesOffset + textureCoordinatesIndexSubstitutes [geometryArray.getTextureCoordinateIndex(0, vertexIndex1)]) 
            + "/" + (normalOffset + normalIndexSubstitutes [geometryArray.getNormalIndex(vertexIndex1)]) 
            + " " + (vertexOffset + vertexIndexSubstitutes [geometryArray.getCoordinateIndex(vertexIndex2)]) 
            + "/" + (textureCoordinatesOffset + textureCoordinatesIndexSubstitutes [geometryArray.getTextureCoordinateIndex(0, vertexIndex2)]) 
            + "/" + (normalOffset + normalIndexSubstitutes [geometryArray.getNormalIndex(vertexIndex2)]) 
            + " " + (vertexOffset + vertexIndexSubstitutes [geometryArray.getCoordinateIndex(vertexIndex3)]) 
            + "/" + (textureCoordinatesOffset + textureCoordinatesIndexSubstitutes [geometryArray.getTextureCoordinateIndex(0, vertexIndex3)]) 
            + "/" + (normalOffset + normalIndexSubstitutes [geometryArray.getNormalIndex(vertexIndex3)]) 
            + " " + (vertexOffset + vertexIndexSubstitutes [geometryArray.getCoordinateIndex(vertexIndex4)]) 
            + "/" + (textureCoordinatesOffset + textureCoordinatesIndexSubstitutes [geometryArray.getTextureCoordinateIndex(0, vertexIndex4)]) 
            + "/" + (normalOffset + normalIndexSubstitutes [geometryArray.getNormalIndex(vertexIndex4)]) + "\n");
      } else {
        writer.write("f " + (vertexOffset + vertexIndexSubstitutes [geometryArray.getCoordinateIndex(vertexIndex1)]) 
            + "/" + (textureCoordinatesOffset + textureCoordinatesIndexSubstitutes [geometryArray.getTextureCoordinateIndex(0, vertexIndex1)]) 
            + " " + (vertexOffset + vertexIndexSubstitutes [geometryArray.getCoordinateIndex(vertexIndex2)]) 
            + "/" + (textureCoordinatesOffset + textureCoordinatesIndexSubstitutes [geometryArray.getTextureCoordinateIndex(0, vertexIndex2)]) 
            + " " + (vertexOffset + vertexIndexSubstitutes [geometryArray.getCoordinateIndex(vertexIndex3)]) 
            + "/" + (textureCoordinatesOffset + textureCoordinatesIndexSubstitutes [geometryArray.getTextureCoordinateIndex(0, vertexIndex3)]) 
            + " " + (vertexOffset + vertexIndexSubstitutes [geometryArray.getCoordinateIndex(vertexIndex4)]) 
            + "/" + (textureCoordinatesOffset + textureCoordinatesIndexSubstitutes [geometryArray.getTextureCoordinateIndex(0, vertexIndex4)]) + "\n");
      }
    } else {
      if ((geometryArray.getVertexFormat() & GeometryArray.NORMALS) != 0) {
        writer.write("f " + (vertexOffset + vertexIndexSubstitutes [geometryArray.getCoordinateIndex(vertexIndex1)]) 
            + "//" + (normalOffset + normalIndexSubstitutes [geometryArray.getNormalIndex(vertexIndex1)]) 
            + " "  + (vertexOffset + vertexIndexSubstitutes [geometryArray.getCoordinateIndex(vertexIndex2)]) 
            + "//" + (normalOffset + normalIndexSubstitutes [geometryArray.getNormalIndex(vertexIndex2)]) 
            + " "  + (vertexOffset + vertexIndexSubstitutes [geometryArray.getCoordinateIndex(vertexIndex3)]) 
            + "//" + (normalOffset + normalIndexSubstitutes [geometryArray.getNormalIndex(vertexIndex3)]) 
            + " "  + (vertexOffset + vertexIndexSubstitutes [geometryArray.getCoordinateIndex(vertexIndex4)]) 
            + "//" + (normalOffset + normalIndexSubstitutes [geometryArray.getNormalIndex(vertexIndex4)]) + "\n");
      } else {
        writer.write("f " + (vertexOffset + vertexIndexSubstitutes [geometryArray.getCoordinateIndex(vertexIndex1)]) 
            + " "  + (vertexOffset + vertexIndexSubstitutes [geometryArray.getCoordinateIndex(vertexIndex2)]) 
            + " "  + (vertexOffset + vertexIndexSubstitutes [geometryArray.getCoordinateIndex(vertexIndex3)]) 
            + " "  + (vertexOffset + vertexIndexSubstitutes [geometryArray.getCoordinateIndex(vertexIndex4)]) + "\n");
      }
    }
  }
  
  /**
   * Writes the triangle indices given at vertexIndex1, vertexIndex2, vertexIndex3, 
   * in a line f at OBJ format. 
   */
  private void writeTriangle(Writer writer, GeometryArray geometryArray, 
                             int vertexIndex1, int vertexIndex2, int vertexIndex3, 
                             int [] vertexIndexSubstitutes, int vertexOffset, 
                             int [] normalIndexSubstitutes, int normalOffset,                                      
                             int [] textureCoordinatesIndexSubstitutes, int textureCoordinatesOffset) throws IOException {
    if ((geometryArray.getVertexFormat() & GeometryArray.TEXTURE_COORDINATE_2) != 0) {
      if ((geometryArray.getVertexFormat() & GeometryArray.NORMALS) != 0) {
        writer.write("f " + (vertexOffset + vertexIndexSubstitutes [vertexIndex1]) 
            + "/" + (textureCoordinatesOffset + textureCoordinatesIndexSubstitutes [vertexIndex1]) 
            + "/" + (normalOffset + normalIndexSubstitutes [vertexIndex1]) 
            + " " + (vertexOffset + vertexIndexSubstitutes [vertexIndex2]) 
            + "/" + (textureCoordinatesOffset + textureCoordinatesIndexSubstitutes [vertexIndex2]) 
            + "/" + (normalOffset + normalIndexSubstitutes [vertexIndex2]) 
            + " " + (vertexOffset + vertexIndexSubstitutes [vertexIndex3]) 
            + "/" + (textureCoordinatesOffset + textureCoordinatesIndexSubstitutes [vertexIndex3]) 
            + "/" + (normalOffset + normalIndexSubstitutes [vertexIndex3]) + "\n");
      } else {
        writer.write("f " + (vertexOffset + vertexIndexSubstitutes [vertexIndex1]) 
            + "/" + (textureCoordinatesOffset + textureCoordinatesIndexSubstitutes [vertexIndex1]) 
            + " " + (vertexOffset + vertexIndexSubstitutes [vertexIndex2]) 
            + "/" + (textureCoordinatesOffset + textureCoordinatesIndexSubstitutes [vertexIndex2]) 
            + " " + (vertexOffset + vertexIndexSubstitutes [vertexIndex3]) 
            + "/" + (textureCoordinatesOffset + textureCoordinatesIndexSubstitutes [vertexIndex3]) + "\n");
      }
    } else {
      if ((geometryArray.getVertexFormat() & GeometryArray.NORMALS) != 0) {
        writer.write("f " + (vertexOffset + vertexIndexSubstitutes [vertexIndex1]) 
            + "//" + (normalOffset + normalIndexSubstitutes [vertexIndex1]) 
            + " "  + (vertexOffset + vertexIndexSubstitutes [vertexIndex2]) 
            + "//" + (normalOffset + normalIndexSubstitutes [vertexIndex2]) 
            + " "  + (vertexOffset + vertexIndexSubstitutes [vertexIndex3]) 
            + "//" + (normalOffset + normalIndexSubstitutes [vertexIndex3]) + "\n");
      } else {
        writer.write("f " + (vertexOffset + vertexIndexSubstitutes [vertexIndex1]) 
            + " "  + (vertexOffset + vertexIndex2) 
            + " "  + (vertexOffset + vertexIndex3) + "\n");
      }
    }
  }
  
  /**
   * Writes the quadrilateral indices given at vertexIndex1, vertexIndex2, vertexIndex3, vertexIndex4,
   * in a line f at OBJ format. 
   */
  private void writeQuadrilateral(Writer writer, GeometryArray geometryArray, 
                                  int vertexIndex1, int vertexIndex2, int vertexIndex3, int vertexIndex4, 
                                  int [] vertexIndexSubstitutes, int vertexOffset, 
                                  int [] normalIndexSubstitutes, int normalOffset,                                      
                                  int [] textureCoordinatesIndexSubstitutes, int textureCoordinatesOffset) throws IOException {
    if ((geometryArray.getVertexFormat() & GeometryArray.TEXTURE_COORDINATE_2) != 0) {
      if ((geometryArray.getVertexFormat() & GeometryArray.NORMALS) != 0) {
        writer.write("f " + (vertexOffset + vertexIndexSubstitutes [vertexIndex1]) 
            + "/" + (textureCoordinatesOffset + textureCoordinatesIndexSubstitutes [vertexIndex1]) 
            + "/" + (normalOffset + normalIndexSubstitutes [vertexIndex1]) 
            + " " + (vertexOffset + vertexIndexSubstitutes [vertexIndex2]) 
            + "/" + (textureCoordinatesOffset + textureCoordinatesIndexSubstitutes [vertexIndex2]) 
            + "/" + (normalOffset + normalIndexSubstitutes [vertexIndex2]) 
            + " " + (vertexOffset + vertexIndexSubstitutes [vertexIndex3]) 
            + "/" + (textureCoordinatesOffset + textureCoordinatesIndexSubstitutes [vertexIndex3]) 
            + "/" + (normalOffset + normalIndexSubstitutes [vertexIndex3]) 
            + " " + (vertexOffset + vertexIndexSubstitutes [vertexIndex4]) 
            + "/" + (textureCoordinatesOffset + textureCoordinatesIndexSubstitutes [vertexIndex4]) 
            + "/" + (normalOffset + normalIndexSubstitutes [vertexIndex4]) + "\n");
      } else {
        writer.write("f " + (vertexOffset + vertexIndexSubstitutes [vertexIndex1]) 
            + "/" + (textureCoordinatesOffset + textureCoordinatesIndexSubstitutes [vertexIndex1]) 
            + " " + (vertexOffset + vertexIndexSubstitutes [vertexIndex2]) 
            + "/" + (textureCoordinatesOffset + textureCoordinatesIndexSubstitutes [vertexIndex2]) 
            + " " + (vertexOffset + vertexIndexSubstitutes [vertexIndex3]) 
            + "/" + (textureCoordinatesOffset + textureCoordinatesIndexSubstitutes [vertexIndex3]) 
            + " " + (vertexOffset + vertexIndexSubstitutes [vertexIndex4]) 
            + "/" + (textureCoordinatesOffset + textureCoordinatesIndexSubstitutes [vertexIndex4]) + "\n");
      }
    } else {
      if ((geometryArray.getVertexFormat() & GeometryArray.NORMALS) != 0) {
        writer.write("f " + (vertexOffset + vertexIndexSubstitutes [vertexIndex1]) 
            + "//" + (normalOffset + normalIndexSubstitutes [vertexIndex1]) 
            + " "  + (vertexOffset + vertexIndexSubstitutes [vertexIndex2]) 
            + "//" + (normalOffset + normalIndexSubstitutes [vertexIndex2]) 
            + " "  + (vertexOffset + vertexIndexSubstitutes [vertexIndex3]) 
            + "//" + (normalOffset + normalIndexSubstitutes [vertexIndex3]) 
            + " "  + (vertexOffset + vertexIndexSubstitutes [vertexIndex4]) 
            + "//" + (normalOffset + normalIndexSubstitutes [vertexIndex4]) + "\n");
      } else {
        writer.write("f " + (vertexOffset + vertexIndexSubstitutes [vertexIndex1]) 
            + " "  + (vertexOffset + vertexIndexSubstitutes [vertexIndex2]) 
            + " "  + (vertexOffset + vertexIndexSubstitutes [vertexIndex3]) 
            + " "  + (vertexOffset + vertexIndexSubstitutes [vertexIndex4]) + "\n");
      }
    }
  }
  
  /**
   * Returns a <code>home</code> new tree node, with branches for each wall 
   * and piece of furniture of <code>home</code>. 
   */
  private Node createHomeTree(Home home) {
    Group homeRoot = createHomeRoot();
    // Add walls and pieces already available 
    for (Wall wall : home.getWalls()) {
      addWall(homeRoot, wall, home);
    }
    for (HomePieceOfFurniture piece : home.getFurniture()) {
      addPieceOfFurniture(homeRoot, piece);
    }
    // Add wall and furniture listeners to home for further update
    addWallListener(home, homeRoot);
    addFurnitureListener(home, homeRoot);
    return homeRoot;
  }

  /**
   * Returns a new group at home subtree root.
   */
  private Group createHomeRoot() {
    Group homeGroup = new Group();    
    //  Allow group to have new children
    homeGroup.setCapability(Group.ALLOW_CHILDREN_WRITE);
    homeGroup.setCapability(Group.ALLOW_CHILDREN_EXTEND);
    return homeGroup;
  }

  /**
   * Adds a wall listener and walls alpha change listener to <code>home</code> 
   * that updates the scene <code>homeRoot</code>, each time a wall is added, updated or deleted. 
   */
  private void addWallListener(final Home home, final Group homeRoot) {
    this.wallListener = new WallListener() {
        public void wallChanged(WallEvent ev) {
          Wall wall = ev.getWall();
          switch (ev.getType()) {
            case ADD :
              addWall(homeRoot, wall, home);
              break;
            case UPDATE :
              updateWall(wall);
              break;
            case DELETE :
              deleteObject(wall);
              break;
          }
        }
      };
    home.addWallListener(this.wallListener);
    this.wallsAlphaListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          updateObjects(home.getWalls());
        }
      };
    home.addPropertyChangeListener(Home.Property.WALLS_ALPHA, this.wallsAlphaListener); 
  }

  /**
   * Adds a furniture listener to <code>home</code> that updates the scene <code>homeRoot</code>, 
   * each time a piece of furniture is added, updated or deleted. 
   */
  private void addFurnitureListener(final Home home, final Group homeRoot) {
    this.furnitureListener = new FurnitureListener() {
        public void pieceOfFurnitureChanged(FurnitureEvent ev) {
          HomePieceOfFurniture piece = (HomePieceOfFurniture)ev.getPieceOfFurniture();
          switch (ev.getType()) {
            case ADD :
              addPieceOfFurniture(homeRoot, piece);
              break;
            case UPDATE :
              updatePieceOfFurniture(piece);
              break;
            case DELETE :
              deleteObject(piece);
              break;
          }
          // If piece is a door or a window, update walls that intersect with piece
          if (piece.isDoorOrWindow()) {
            updateObjects(home.getWalls());
          }
        }
      };
    home.addFurnitureListener(this.furnitureListener);
  }

  /**
   * Adds to <code>homeRoot</code> a wall branch matching <code>wall</code>.
   */
  private void addWall(Group homeRoot, Wall wall, Home home) {
    Wall3D wall3D = new Wall3D(wall, home);
    this.homeObjects.put(wall, wall3D);
    homeRoot.addChild(wall3D);
    clearPrintedImageCache();
  }

  /**
   * Updates <code>wall</code> geometry, 
   * and the walls at its end or start.
   */
  private void updateWall(Wall wall) {
    Collection<Wall> wallsToUpdate = new ArrayList<Wall>(3);
    wallsToUpdate.add(wall);
    if (wall.getWallAtStart() != null) {
      wallsToUpdate.add(wall.getWallAtStart());                
    }
    if (wall.getWallAtEnd() != null) {
      wallsToUpdate.add(wall.getWallAtEnd());                
    }
    updateObjects(wallsToUpdate);
  }
  
  /**
   * Detaches from the scene the branch matching <code>homeObject</code>.
   */
  private void deleteObject(Object homeObject) {
    this.homeObjects.get(homeObject).detach();
    this.homeObjects.remove(homeObject);
    clearPrintedImageCache();
  }

  /**
   * Adds to <code>homeRoot</code> a piece branch matching <code>piece</code>.
   */
  private void addPieceOfFurniture(Group homeRoot, HomePieceOfFurniture piece) {
    HomePieceOfFurniture3D piece3D = new HomePieceOfFurniture3D(piece);
    this.homeObjects.put(piece, piece3D);
    homeRoot.addChild(piece3D);
    clearPrintedImageCache();
  }

  /**
   * Updates <code>piece</code> scale, angle and location.
   */
  private void updatePieceOfFurniture(HomePieceOfFurniture piece) {
    updateObjects(Arrays.asList(new HomePieceOfFurniture [] {piece}));
  }

  /**
   * Updates <code>objects</code> later. Should be invoked from Event Dispatch Thread.
   */
  private void updateObjects(Collection<? extends Object> objects) {
    if (this.homeObjectsToUpdate != null) {
      this.homeObjectsToUpdate.addAll(objects);
    } else {
      this.homeObjectsToUpdate = new HashSet<Object>(objects);
      // Invoke later the update of objects of homeObjectsToUpdate
      EventQueue.invokeLater(new Runnable () {
        public void run() {
          for (Object object : homeObjectsToUpdate) {
            ObjectBranch objectBranch = homeObjects.get(object);
            // Check object wasn't deleted since updateObjects call
            if (objectBranch != null) { 
              homeObjects.get(object).update();
            }
          }
          homeObjectsToUpdate = null;
        }
      });
    }
    clearPrintedImageCache();
  }
  
  /**
   * Root of a branch that matches a home object. 
   */
  private static abstract class ObjectBranch extends BranchGroup {
    public abstract void update();
  }

  /**
   * Root of wall branch.
   */
  private static class Wall3D extends ObjectBranch {
    private static final Material DEFAULT_MATERIAL = new Material();
    private static final Map<Integer, Material> materials = new HashMap<Integer, Material>();
    
    private static final int LEFT_WALL_SIDE  = 0;
    private static final int RIGHT_WALL_SIDE = 1;
    
    private Home home;

    public Wall3D(Wall wall, Home home) {
      setUserData(wall);
      this.home = home;

      // Allow wall branch to be removed from its parent
      setCapability(BranchGroup.ALLOW_DETACH);
      // Allow to read branch shape children
      setCapability(BranchGroup.ALLOW_CHILDREN_READ);
      
      // Add wall left and right empty shapes to branch
      addChild(createWallPartShape());
      addChild(createWallPartShape());
      // Set wall shape geometry and appearance
      updateWallGeometry();
      updateWallAppearance();
    }

    /**
     * Returns a new wall part shape with no geometry  
     * and a default appearance with a white material.
     */
    private Node createWallPartShape() {
      Shape3D wallShape = new Shape3D();
      // Allow wall shape to change its geometry
      wallShape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
      wallShape.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
      wallShape.setCapability(Shape3D.ALLOW_APPEARANCE_READ);

      Appearance wallAppearance = new Appearance();
      wallAppearance.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
      wallAppearance.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
      // Combinaison texture and wall color
      TextureAttributes textureAttributes = new TextureAttributes ();
      textureAttributes.setTextureMode(TextureAttributes.MODULATE);
      wallAppearance.setTextureAttributes(textureAttributes);      
      wallAppearance.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_READ);
      wallAppearance.setMaterial(DEFAULT_MATERIAL);
      TransparencyAttributes transparencyAttributes = new TransparencyAttributes();
      transparencyAttributes.setCapability(TransparencyAttributes.ALLOW_VALUE_WRITE);
      transparencyAttributes.setCapability(TransparencyAttributes.ALLOW_MODE_WRITE);
      wallAppearance.setTransparencyAttributes(transparencyAttributes);
      wallShape.setAppearance(wallAppearance);
      
      return wallShape;
    }

    @Override
    public void update() {
      updateWallGeometry();
      updateWallAppearance();
    }
    
    /**
     * Sets the 3D geometry of this wall shapes that matches its 2D geometry.  
     */
    private void updateWallGeometry() {
      updateWallSideGeometry(LEFT_WALL_SIDE, ((Wall)getUserData()).getLeftSideTexture());
      updateWallSideGeometry(RIGHT_WALL_SIDE, ((Wall)getUserData()).getRightSideTexture());
    }
    
    private void updateWallSideGeometry(int wallSide, HomeTexture texture) {
      Shape3D wallShape = (Shape3D)getChild(wallSide);
      int currentGeometriesCount = wallShape.numGeometries();
      for (Geometry wallGeometry : createWallGeometries(wallSide, texture)) {
        wallShape.addGeometry(wallGeometry);
      }
      for (int i = currentGeometriesCount - 1; i >= 0; i--) {
        wallShape.removeGeometry(i);
      }
    }
    
    /**
     * Returns <code>wall</code> geometries computed with windows or doors 
     * that intersect wall.
     */
    private Geometry [] createWallGeometries(int wallSide, HomeTexture texture) {
      Shape wallShape = getShape(getWallSidePoints(wallSide));
      float wallHeightAtStart = getWallHeightAtStart();
      float wallHeightAtEnd = getWallHeightAtEnd();
      float maxWallHeight = Math.max(wallHeightAtStart, wallHeightAtEnd);
      
      // Compute wall angles and top line factors
      Wall wall = (Wall)getUserData();
      double wallYawAngle = Math.atan2(wall.getYEnd() - wall.getYStart(), wall.getXEnd() - wall.getXStart()); 
      double cosWallYawAngle = Math.cos(wallYawAngle);
      double sinWallYawAngle = Math.sin(wallYawAngle);
      double wallXStartWithZeroYaw = cosWallYawAngle * wall.getXStart() + sinWallYawAngle * wall.getYStart();
      double wallXEndWithZeroYaw = cosWallYawAngle * wall.getXEnd() + sinWallYawAngle * wall.getYEnd();
      double topLineAlpha;
      double topLineBeta;
      if (wallHeightAtStart == wallHeightAtEnd) {
        topLineAlpha = 0;
        topLineBeta = wallHeightAtStart;
      } else {
        topLineAlpha = (wallHeightAtEnd - wallHeightAtStart) / (wallXEndWithZeroYaw - wallXStartWithZeroYaw);
        topLineBeta = wallHeightAtStart - topLineAlpha * wallXStartWithZeroYaw;
      }
      
      // Search which doors or windows intersect with this wall side
      Map<HomePieceOfFurniture, Area> intersections = new HashMap<HomePieceOfFurniture, Area>();
      for (HomePieceOfFurniture piece : this.home.getFurniture()) {
        if (piece.isDoorOrWindow() 
            && piece.getElevation() < maxWallHeight) {
          Shape pieceShape = getShape(piece.getPoints());
          Area wallArea = new Area(wallShape);
          wallArea.intersect(new Area(pieceShape));
          boolean wallPieceIntersectionEmpty = wallArea.isEmpty();
          if (!wallPieceIntersectionEmpty) {
            intersections.put(piece, wallArea);
          }
        }
      }
      List<Geometry> wallGeometries = new ArrayList<Geometry>();
      List<float[]> wallPoints = new ArrayList<float[]>(4);
      // Get wall shape excluding window intersections
      Area wallArea = new Area(wallShape);
      for (Area intersection : intersections.values()) {
        wallArea.exclusiveOr(intersection);
      }
      
      // Generate geometry for each wall part that doesn't contain a window
      for (PathIterator it = wallArea.getPathIterator(null); !it.isDone(); ) {
        float [] wallPoint = new float[2];
        if (it.currentSegment(wallPoint) == PathIterator.SEG_CLOSE) {
          float [][] wallPartPoints = wallPoints.toArray(new float[wallPoints.size()][]);
          // Compute geometry for vertical part
          wallGeometries.add(createWallVerticalPartGeometry(wallPartPoints, 0, 
              cosWallYawAngle, sinWallYawAngle, topLineAlpha, topLineBeta, texture));
          // Compute geometry for bottom part
          wallGeometries.add(createWallHorizontalPartGeometry(wallPartPoints, 0));
          // Compute geometry for top part
          wallGeometries.add(createWallTopPartGeometry(wallPartPoints, 
              cosWallYawAngle, sinWallYawAngle, topLineAlpha, topLineBeta));
          wallPoints.clear();
        } else {
          wallPoints.add(wallPoint);
        }
        it.next();
      }
      
      // Generate geometry for each wall part above and below a window
      for (Map.Entry<HomePieceOfFurniture, Area> windowIntersection : intersections.entrySet()) {
        for (PathIterator it = windowIntersection.getValue().getPathIterator(null); !it.isDone(); ) {
          float [] wallPoint = new float[2];
          if (it.currentSegment(wallPoint) == PathIterator.SEG_CLOSE) {
            float [][] wallPartPoints = wallPoints.toArray(new float[wallPoints.size()][]);
            HomePieceOfFurniture doorOrWindow = windowIntersection.getKey();            
            float doorOrWindowTop = doorOrWindow.getElevation() + doorOrWindow.getHeight();
            // Compute the minimum vertical position of wallPartPoints
            double minTopY = maxWallHeight;
            for (int i = 0; i < wallPartPoints.length; i++) {
              double xTopPointWithZeroYaw = cosWallYawAngle * wallPartPoints[i][0] + sinWallYawAngle * wallPartPoints[i][1];
              minTopY = Math.min(minTopY, topLineAlpha * xTopPointWithZeroYaw + topLineBeta);
            }            
            // Generate geometry for wall part above window
            if (doorOrWindowTop < minTopY) {
              wallGeometries.add(createWallVerticalPartGeometry(wallPartPoints, doorOrWindowTop, 
                  cosWallYawAngle, sinWallYawAngle, topLineAlpha, topLineBeta, texture));
              wallGeometries.add(createWallHorizontalPartGeometry(
                  wallPartPoints, doorOrWindowTop));
              wallGeometries.add(createWallTopPartGeometry(wallPartPoints, 
                  cosWallYawAngle, sinWallYawAngle, topLineAlpha, topLineBeta));
            }
            // Generate geometry for wall part below window
            if (doorOrWindow.getElevation() > 0) {
              wallGeometries.add(createWallVerticalPartGeometry(wallPartPoints, 0, 
                  cosWallYawAngle, sinWallYawAngle, 0, doorOrWindow.getElevation(), texture));
              wallGeometries.add(createWallHorizontalPartGeometry(wallPartPoints, 0));
              wallGeometries.add(createWallHorizontalPartGeometry(wallPartPoints, doorOrWindow.getElevation()));
            }
            wallPoints.clear();
          } else {
            wallPoints.add(wallPoint);
          }
          it.next();
        }
      }
      return wallGeometries.toArray(new Geometry [wallGeometries.size()]);
    }
    
    /**
     * Returns the shape matching the coordinates in <code>points</code> array.
     */
    private Shape getShape(float [][] points) {
      GeneralPath wallPath = new GeneralPath();
      wallPath.moveTo(points [0][0], points [0][1]);
      for (int i = 1; i < points.length; i++) {
        wallPath.lineTo(points [i][0], points [i][1]);
      }
      wallPath.closePath();
      return wallPath;
    }
    
    /**
     * Returns the points of one of the side of this wall. 
     */
    private float [][] getWallSidePoints(int wallSide) {
      Wall wall = (Wall)getUserData();
      float [][] wallPoints = wall.getPoints();
      
      if (wallSide == LEFT_WALL_SIDE) {
        wallPoints [2][0] = wall.getXEnd();
        wallPoints [2][1] = wall.getYEnd();
        wallPoints [3][0] = wall.getXStart();
        wallPoints [3][1] = wall.getYStart();
      } else { // RIGHT_WALL_SIDE
        wallPoints [1][0] = wall.getXEnd();
        wallPoints [1][1] = wall.getYEnd();
        wallPoints [0][0] = wall.getXStart();
        wallPoints [0][1] = wall.getYStart();
      }
      return wallPoints;
    }

    /**
     * Returns the vertical rectangles that join each point of <code>points</code>
     * and spread from <code>yMin</code> to a top line described by <code>topLineAlpha</code>
     * and <code>topLineBeta</code> factors in a vertical plan that is rotated around
     * vertical axis matching <code>cosWallYawAngle</code> and <code>sinWallYawAngle</code>. 
     */
    private Geometry createWallVerticalPartGeometry(float [][] points, float yMin, 
                                                    double cosWallYawAngle, double sinWallYawAngle, 
                                                    double topLineAlpha, double topLineBeta, 
                                                    HomeTexture texture) {
      // Compute wall coordinates
      Point3f [] bottom = new Point3f [points.length];
      Point3f [] top    = new Point3f [points.length];
      for (int i = 0; i < points.length; i++) {
        bottom [i] = new Point3f(points[i][0], yMin, points[i][1]);
        // Compute vertical top point 
        double xTopPointWithZeroYaw = cosWallYawAngle * points[i][0] + sinWallYawAngle * points[i][1];
        float topY = (float)(topLineAlpha * xTopPointWithZeroYaw + topLineBeta);
        top [i] = new Point3f(points[i][0], topY, points[i][1]);
      }
      Point3f [] coords = new Point3f [points.length * 4];
      int j = 0;
      for (int i = 0; i < points.length - 1; i++) {
        coords [j++] = bottom [i];
        coords [j++] = bottom [i + 1];
        coords [j++] = top [i + 1];
        coords [j++] = top [i];
      }
      coords [j++] = bottom [points.length - 1];
      coords [j++] = bottom [0];
      coords [j++] = top [0];
      coords [j++] = top [points.length - 1];
      
      GeometryInfo geometryInfo = new GeometryInfo(GeometryInfo.QUAD_ARRAY);
      geometryInfo.setCoordinates (coords);

      // Compute wall texture coordinates
      if (texture != null) {
        TexCoord2f [] textureCoords = new TexCoord2f [points.length * 4];
        float yMinTextureCoords = yMin / texture.getHeight();
        TexCoord2f firstTextureCoords = new TexCoord2f(0, yMinTextureCoords);
        j = 0;
        for (int i = 0; i < points.length - 1; i++) {
          float horizontalTextureCoords = (float)Point2D.distance(points[i][0], points[i][1], 
              points[i + 1][0], points[i + 1][1]) / texture.getWidth();
          textureCoords [j++] = firstTextureCoords;
          textureCoords [j++] = new TexCoord2f(horizontalTextureCoords, yMinTextureCoords);
          textureCoords [j++] = new TexCoord2f(horizontalTextureCoords, top [i + 1].y / texture.getHeight());
          textureCoords [j++] = new TexCoord2f(0, top [i].y / texture.getHeight());
        }
        float horizontalTextureCoords = (float)Point2D.distance(points[0][0], points[0][1], 
            points[points.length - 1][0], points[points.length - 1][1]) / texture.getWidth();
        textureCoords [j++] = firstTextureCoords;
        textureCoords [j++] = new TexCoord2f(horizontalTextureCoords, yMinTextureCoords);
        textureCoords [j++] = new TexCoord2f(horizontalTextureCoords, top [top.length - 1].y / texture.getHeight());
        textureCoords [j++] = new TexCoord2f(0, top [0].y / texture.getHeight());
        geometryInfo.setTextureCoordinateParams(1, 2);
        geometryInfo.setTextureCoordinates(0, textureCoords);
      }
      
      // Generate normals
      new NormalGenerator(0).generateNormals(geometryInfo);
      return geometryInfo.getIndexedGeometryArray();
    }

    /**
     * Returns the geometry of an horizontal part of a wall at <code>y</code>.
     */
    private Geometry createWallHorizontalPartGeometry(float [][] points, float y) {
      Point3f [] coords = new Point3f [points.length];
      for (int i = 0; i < points.length; i++) {
        coords [i] = new Point3f(points[i][0], y, points[i][1]);
      }
      GeometryInfo geometryInfo = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
      geometryInfo.setCoordinates (coords);
      geometryInfo.setStripCounts(new int [] {coords.length});
      // Generate normals
      new NormalGenerator(0).generateNormals(geometryInfo);
      return geometryInfo.getIndexedGeometryArray ();
    }
    
    /**
     * Returns the geometry of the top part of a wall.
     */
    private Geometry createWallTopPartGeometry(float [][] points, 
                                               double cosWallYawAngle, double sinWallYawAngle, 
                                               double topLineAlpha, double topLineBeta) {
      Point3f [] coords = new Point3f [points.length];
      for (int i = 0; i < points.length; i++) {
        double xTopPointWithZeroYaw = cosWallYawAngle * points[i][0] + sinWallYawAngle * points[i][1];
        float topY = (float)(topLineAlpha * xTopPointWithZeroYaw + topLineBeta);
        coords [i] = new Point3f(points[i][0], topY, points[i][1]);
      }
      GeometryInfo geometryInfo = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
      geometryInfo.setCoordinates (coords);
      geometryInfo.setStripCounts(new int [] {coords.length});
      // Generate normals
      new NormalGenerator(0).generateNormals(geometryInfo);
      return geometryInfo.getIndexedGeometryArray ();
    }
    
    /**
     * Returns the height at the start of the wall managed by this 3D object.
     */
    private float getWallHeightAtStart() {
      Float wallHeight = ((Wall)getUserData()).getHeight();      
      if (wallHeight != null) {
        return wallHeight;
      } else {
        // If wall height isn't set, use home wall height
        return this.home.getWallHeight();
      }
    }
    
    /**
     * Returns the height at the end of the wall managed by this 3D object.
     */
    private float getWallHeightAtEnd() {
      Wall wall = (Wall)getUserData();      
      if (wall.isTrapezoidal()) {
        return wall.getHeightAtEnd();
      } else {
        // If the wall isn't trapezoidal, use same height as at wall start
        return getWallHeightAtStart();
      }
    }
    
    /**
     * Sets wall appearance with its color, texture and transparency.
     */
    private void updateWallAppearance() {
      Wall wall = (Wall)getUserData();
      updateWallSideAppearance(((Shape3D)getChild(LEFT_WALL_SIDE)).getAppearance(), 
          wall.getLeftSideTexture(), wall.getLeftSideColor());
      updateWallSideAppearance(((Shape3D)getChild(RIGHT_WALL_SIDE)).getAppearance(), 
          wall.getRightSideTexture(), wall.getRightSideColor());
    }
    
    /**
     * Sets wall side appearance with its color, texture and transparency.
     */
    private void updateWallSideAppearance(final Appearance  wallSideAppearance, 
                                          final HomeTexture wallSideTexture,
                                          Integer wallSideColor) {
      // Update material and texture of wall left side
      if (wallSideTexture == null) {
        wallSideAppearance.setMaterial(getMaterial(wallSideColor));
        wallSideAppearance.setTexture(null);
      } else {
        wallSideAppearance.setMaterial(DEFAULT_MATERIAL);
        final TextureManager textureManager = TextureManager.getInstance();
        textureManager.loadTexture(wallSideTexture.getImage(), 
            new TextureManager.TextureObserver() {
                public void textureUpdated(Texture texture) {
                  wallSideAppearance.setTexture(texture);
                }
              });
      }
      // Update wall left side transparency
      float wallsAlpha = this.home.getWallsAlpha();
      TransparencyAttributes transparencyAttributes = wallSideAppearance.getTransparencyAttributes();
      transparencyAttributes.setTransparency(wallsAlpha);
      // If walls alpha is equal to zero, turn off transparency to get better results 
      transparencyAttributes.setTransparencyMode(wallsAlpha == 0 
          ? TransparencyAttributes.NONE 
          : TransparencyAttributes.NICEST);
    }
    
    private Material getMaterial(Integer color) {
      if (color != null) {
        Material material = materials.get(color); 
        if (material == null) {
          Color3f materialColor = new Color3f(((color >>> 16) & 0xFF) / 256f,
                                              ((color >>> 8) & 0xFF) / 256f,
                                                      (color & 0xFF) / 256f);
          material = new Material(materialColor, new Color3f(), materialColor, materialColor, 64);
          // Store created materials in cache
          materials.put(color, material);
        }
        return material;
      } else {
        return DEFAULT_MATERIAL;
      }
    }
  }

  /**
   * Root of piece of furniture branch.
   */
  private static class HomePieceOfFurniture3D extends ObjectBranch {
    private static Executor modelLoader = Executors.newSingleThreadExecutor();

    public HomePieceOfFurniture3D(HomePieceOfFurniture piece) {
      this(piece, false);
    }

    public HomePieceOfFurniture3D(HomePieceOfFurniture piece, boolean waitModelLoadingEnd) {
      setUserData(piece);      

      // Allow piece branch to be removed from its parent
      setCapability(BranchGroup.ALLOW_DETACH);
      // Allow to read branch transform child
      setCapability(BranchGroup.ALLOW_CHILDREN_READ);
      
      createPieceOfFurnitureNode(waitModelLoadingEnd);
    }

    /**
     * Creates the piece node with its transform group and add it to the piece branch. 
     */
    private void createPieceOfFurnitureNode(boolean waitModelLoadingEnd) {
      final TransformGroup pieceTransformGroup = new TransformGroup();
      // Allow the change of the transformation that sets piece size and position
      pieceTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
      pieceTransformGroup.setCapability(Group.ALLOW_CHILDREN_READ);
      addChild(pieceTransformGroup);

      if (waitModelLoadingEnd) {
        Node modelNode = getModelNode();
        pieceTransformGroup.addChild(modelNode);
        // Allow appearance change on all children
        setAppearanceChangeCapability(modelNode);
        update();
      } else {
        pieceTransformGroup.setCapability(Group.ALLOW_CHILDREN_WRITE);
        pieceTransformGroup.setCapability(Group.ALLOW_CHILDREN_EXTEND);
        
        // While loading model use a temporary node that displays a white box  
        final BranchGroup waitBranch = new BranchGroup();
        waitBranch.setCapability(BranchGroup.ALLOW_DETACH);
        waitBranch.addChild(getModelBox(Color.WHITE));      
        // Allow appearance change on all children
        setAppearanceChangeCapability(waitBranch);
        
        pieceTransformGroup.addChild(waitBranch);
        
        // Set piece model initial location, orientation and size      
        updatePieceOfFurnitureTransform();
        
        // Load piece real 3D model
        modelLoader.execute(new Runnable() {
            public void run() {
              Node modelNode = getModelNode();
              final BranchGroup modelBranch = new BranchGroup();
              modelBranch.addChild(modelNode);
              // Allow appearance change on all children
              setAppearanceChangeCapability(modelBranch);
              
              // Change live objects in Event Dispatch Thread
              EventQueue.invokeLater(new Runnable() {
                  public void run() {
                    // Add model branch to live scene
                    pieceTransformGroup.addChild(modelBranch);
                    // Remove temporary node
                    waitBranch.detach();
                    // Update piece color, visibility and model mirror in dispatch thread as
                    // these attributes may be changed in that thread
                    updatePieceOfFurnitureColor();      
                    updatePieceOfFurnitureVisibility();
                    updatePieceOfFurnitureModelMirrored();
                  }
                });
            }
          });
      }
    }

    @Override
    public void update() {
      updatePieceOfFurnitureTransform();
      updatePieceOfFurnitureColor();      
      updatePieceOfFurnitureVisibility();      
      updatePieceOfFurnitureModelMirrored();
    }

    /**
     * Sets the transformation applied to piece model to match
     * its location, its angle and its size.
     */
    private void updatePieceOfFurnitureTransform() {
      HomePieceOfFurniture piece = (HomePieceOfFurniture)getUserData();
      // Set piece size
      Transform3D scale = new Transform3D();
      float pieceWidth = piece.getWidth();
      // If piece model is mirrored, inverse its width
      if (piece.isModelMirrored()) {
        pieceWidth *= -1;
      }
      scale.setScale(new Vector3d(pieceWidth, piece.getHeight(), piece.getDepth()));
      // Change its angle around y axis
      Transform3D orientation = new Transform3D();
      orientation.rotY(-piece.getAngle());
      orientation.mul(scale);
      // Translate it to its location
      Transform3D pieceTransform = new Transform3D();
      pieceTransform.setTranslation(new Vector3f(
          piece.getX(), piece.getElevation() + piece.getHeight() / 2, piece.getY()));      
      pieceTransform.mul(orientation);
      
      // Change model transformation      
      ((TransformGroup)getChild(0)).setTransform(pieceTransform);
    }

    /**
     * Sets the color applied to piece model.
     */
    private void updatePieceOfFurnitureColor() {
      HomePieceOfFurniture piece = (HomePieceOfFurniture)getUserData();
      if (piece.getColor() != null) {
        Integer color = piece.getColor();
        Color3f materialColor = new Color3f(((color >>> 16) & 0xFF) / 256f,
                                             ((color >>> 8) & 0xFF) / 256f,
                                                     (color & 0xFF) / 256f);
        setMaterial(getChild(0), 
            new Material(materialColor, new Color3f(), materialColor, materialColor, 64));
      } else {
        // Set default material of model
        setMaterial(getChild(0), null);
      }
    }

    /**
     * Sets whether this piece model is visible or not.
     */
    private void updatePieceOfFurnitureVisibility() {
      HomePieceOfFurniture piece = (HomePieceOfFurniture)getUserData();
      setVisible(getChild(0), piece.isVisible());
    }

    /**
     * Sets whether this piece model is mirrored or not.
     */
    private void updatePieceOfFurnitureModelMirrored() {
      HomePieceOfFurniture piece = (HomePieceOfFurniture)getUserData();
      // Cull front or back model faces whether its model is mirrored or not
      setCullFace(getChild(0), 
          piece.isModelMirrored() ^ piece.isBackFaceShown() 
              ? PolygonAttributes.CULL_FRONT 
              : PolygonAttributes.CULL_BACK);
      // Flip normals if back faces of model are shown
      if (piece.isBackFaceShown()) {
        setBackFaceNormalFlip(getChild(0), true);
      }
    }

    /**
     * Returns the 3D model of this piece that fits 
     * in a 1 unit wide box centered at the origin. 
     */
    private Node getModelNode() {
      PieceOfFurniture piece = (PieceOfFurniture)getUserData();
      // If same model was already loaded return a clone from its cache 
      Content model = piece.getModel();
      
      try {
        BranchGroup modelNode = ModelManager.getInstance().getModel(model);
        // Get model bounding box size
        BoundingBox modelBounds = ModelManager.getInstance().getBounds(modelNode);
        Point3d lower = new Point3d();
        modelBounds.getLower(lower);
        Point3d upper = new Point3d();
        modelBounds.getUpper(upper);
        
        // Translate model to its center
        Transform3D translation = new Transform3D();
        translation.setTranslation(
            new Vector3d(-lower.x - (upper.x - lower.x) / 2, 
                -lower.y - (upper.y - lower.y) / 2, 
                -lower.z - (upper.z - lower.z) / 2));      
        // Scale model to make it fill a 1 unit wide box
        Transform3D scaleOneTransform = new Transform3D();
        scaleOneTransform.setScale (
            new Vector3d(1 / (upper.x -lower.x), 
                1 / (upper.y - lower.y), 
                1 / (upper.z - lower.z)));
        scaleOneTransform.mul(translation);
        // Apply model rotation
        Transform3D modelTransform = new Transform3D();
        float [][] modelRotation = piece.getModelRotation();
        Matrix3f modelRotationMatrix = new Matrix3f(modelRotation [0][0], modelRotation [0][1], modelRotation [0][2],
            modelRotation [1][0], modelRotation [1][1], modelRotation [1][2],
            modelRotation [2][0], modelRotation [2][1], modelRotation [2][2]);
        modelTransform.setRotation(modelRotationMatrix);
        modelTransform.mul(scaleOneTransform);
        
        // Add model scene to transform group
        TransformGroup modelTransformGroup = new TransformGroup(modelTransform);
        modelTransformGroup.addChild(modelNode);
        return modelTransformGroup;
      } catch (IOException ex) {
        // In case of problem return a default box
        return getModelBox(Color.RED);
      } 
    }

    /**
     * Returns a box that may replace model. 
     */
    private Node getModelBox(Color color) {
      Material material = new Material();
      material.setDiffuseColor(new Color3f(color));
      material.setAmbientColor(new Color3f(color.darker()));
      
      Appearance boxAppearance = new Appearance();
      boxAppearance.setMaterial(material);
      return new Box(0.5f, 0.5f, 0.5f, boxAppearance);
    }

    /**
     * Sets the capability to change material and rendering attributes
     * for all children of <code>node</code>.
     */
    private void setAppearanceChangeCapability(Node node) {
      if (node instanceof Group) {
        node.setCapability(Group.ALLOW_CHILDREN_READ);
        Enumeration enumeration = ((Group)node).getAllChildren(); 
        while (enumeration.hasMoreElements()) {
          setAppearanceChangeCapability((Node)enumeration.nextElement());
        }
      } else if (node instanceof Shape3D) {        
        Appearance appearance = ((Shape3D)node).getAppearance();
        if (appearance != null) {
          setAppearanceCapabilities(appearance);
        }
        node.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
        node.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
      }
    }

    /**
     * Sets the material attribute of all <code>Shape3D</code> children nodes of <code>node</code> 
     * with a given <code>material</code>. 
     */
    private void setMaterial(Node node, Material material) {
      if (node instanceof Group) {
        // Set material of all children
        Enumeration enumeration = ((Group)node).getAllChildren(); 
        while (enumeration.hasMoreElements()) {
          setMaterial((Node)enumeration.nextElement(), material);
        }
      } else if (node instanceof Shape3D) {
        Shape3D shape = (Shape3D)node;
        String shapeName = (String)shape.getUserData();
        // Change material of all shape that are not window panes
        if (shapeName == null
            || !shapeName.startsWith(ModelManager.WINDOW_PANE_SHAPE_PREFIX)) {
          Appearance appearance = shape.getAppearance();
          if (appearance == null) {
            appearance = createAppearanceWithChangeCapabilities();
            ((Shape3D)node).setAppearance(appearance);
          }
          // Use appearance user data to store shape default material
          Material defaultMaterial = (Material)appearance.getUserData();
          if (defaultMaterial == null) {
            defaultMaterial = appearance.getMaterial();
            appearance.setUserData(defaultMaterial);
          }
          // Change material
          if (material != null) {
            appearance.setMaterial(material);
          } else {
            // Restore default material
            appearance.setMaterial(defaultMaterial);
          }
        }
      }
    }

    /**
     * Sets the visible attribute of all <code>Shape3D</code> children nodes of <code>node</code>. 
     */
    private void setVisible(Node node, boolean visible) {
      if (node instanceof Group) {
        // Set visibility of all children
        Enumeration enumeration = ((Group)node).getAllChildren(); 
        while (enumeration.hasMoreElements()) {
          setVisible((Node)enumeration.nextElement(), visible);
        }
      } else if (node instanceof Shape3D) {
        Appearance appearance = ((Shape3D)node).getAppearance();
        if (appearance == null) {
          appearance = createAppearanceWithChangeCapabilities();
          ((Shape3D)node).setAppearance(appearance);
        }
        RenderingAttributes renderingAttributes = appearance.getRenderingAttributes();
        if (renderingAttributes == null) {
          renderingAttributes = new RenderingAttributes();
          renderingAttributes.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
          appearance.setRenderingAttributes(renderingAttributes);
        }
        
        // Change visibility
        renderingAttributes.setVisible(visible);
      }
    }

    /**
     * Sets the cull face of all <code>Shape3D</code> children nodes of <code>node</code>.
     * @param cullFace <code>PolygonAttributes.CULL_FRONT</code> or <code>PolygonAttributes.CULL_BACK</code>
     */
    private void setCullFace(Node node, int cullFace) {
      if (node instanceof Group) {
        // Set cull face of all children
        Enumeration enumeration = ((Group)node).getAllChildren(); 
        while (enumeration.hasMoreElements()) {
          setCullFace((Node)enumeration.nextElement(), cullFace);
        }
      } else if (node instanceof Shape3D) {
        Appearance appearance = ((Shape3D)node).getAppearance();
        if (appearance == null) {
          appearance = createAppearanceWithChangeCapabilities();
          ((Shape3D)node).setAppearance(appearance);
        }
        PolygonAttributes polygonAttributes = appearance.getPolygonAttributes();
        if (polygonAttributes == null) {
          polygonAttributes = createPolygonAttributesWithChangeCapabilities();
          appearance.setPolygonAttributes(polygonAttributes);
        }
        
        // Change cull face
        polygonAttributes.setCullFace(cullFace);
      }
    }
    
    /**
     * Sets whether all <code>Shape3D</code> children nodes of <code>node</code> should have 
     * their normal flipped or not.
     * @param backFaceNormalFlip <code>true</code> if normals should be flipped.
     */
    private void setBackFaceNormalFlip(Node node, boolean backFaceNormalFlip) {
      if (node instanceof Group) {
        // Set back face normal flip of all children
        Enumeration enumeration = ((Group)node).getAllChildren(); 
        while (enumeration.hasMoreElements()) {
          setBackFaceNormalFlip((Node)enumeration.nextElement(), backFaceNormalFlip);
        }
      } else if (node instanceof Shape3D) {
        Appearance appearance = ((Shape3D)node).getAppearance();
        if (appearance == null) {
          appearance = createAppearanceWithChangeCapabilities();
          ((Shape3D)node).setAppearance(appearance);
        }
        PolygonAttributes polygonAttributes = appearance.getPolygonAttributes();
        if (polygonAttributes == null) {
          polygonAttributes = createPolygonAttributesWithChangeCapabilities();
          appearance.setPolygonAttributes(polygonAttributes);
        }
        
        // Change back face normal flip
        polygonAttributes.setBackFaceNormalFlip(backFaceNormalFlip);
      }
    }

    private PolygonAttributes createPolygonAttributesWithChangeCapabilities() {
      PolygonAttributes polygonAttributes = new PolygonAttributes();
      polygonAttributes.setCapability(PolygonAttributes.ALLOW_CULL_FACE_WRITE);
      polygonAttributes.setCapability(PolygonAttributes.ALLOW_NORMAL_FLIP_WRITE);
      return polygonAttributes;
    }

    private Appearance createAppearanceWithChangeCapabilities() {
      Appearance appearance = new Appearance();
      setAppearanceCapabilities(appearance);
      return appearance;
    }

    private void setAppearanceCapabilities(Appearance appearance) {
      // Allow future material and rendering attributes changes
      appearance.setCapability(Appearance.ALLOW_MATERIAL_READ);
      appearance.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
      appearance.setCapability(Appearance.ALLOW_RENDERING_ATTRIBUTES_READ);
      appearance.setCapability(Appearance.ALLOW_RENDERING_ATTRIBUTES_WRITE);
      appearance.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_READ);
      appearance.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_WRITE);
    }
  }
  
  /**
   * An <code>Appearance</code> wrapper able to compare appearance equality.  
   */
  public static class ComparableAppearance {
    private Appearance appearance;
    
    public ComparableAppearance(Appearance appearance) {
      this.appearance = appearance;
    }
    
    public Appearance getAppearance() {
      return this.appearance;
    }
    
    @Override
    public boolean equals(Object obj) {
      if (obj instanceof ComparableAppearance) {
        Appearance appearance2 = ((ComparableAppearance)obj).appearance; 
        Material material1 = this.appearance.getMaterial();
        Material material2 = appearance2.getMaterial();
        if ((material1 == null) ^ (material2 == null)) {
          return false;
        } else if (material1 != material2) {
          Color3f color1 = new Color3f();
          Color3f color2 = new Color3f();
          material1.getAmbientColor(color1);
          material2.getAmbientColor(color2);
          if (!color1.equals(color2)) {
            return false;
          } else {
            material1.getDiffuseColor(color1);
            material2.getDiffuseColor(color2);
            if (!color1.equals(color2)) {
              return false;
            } else {
              material1.getEmissiveColor(color1);
              material2.getEmissiveColor(color2);
              if (!color1.equals(color2)) {
                return false;
              } else {
                material1.getSpecularColor(color1);
                material2.getSpecularColor(color2);
                if (!color1.equals(color2)) {
                  return false;
                } else if (material1.getShininess() != material2.getShininess()) {
                  return false;
                }
              }
            }
          }
        }
        TransparencyAttributes transparency1 = this.appearance.getTransparencyAttributes();
        TransparencyAttributes transparency2 = appearance2.getTransparencyAttributes();
        if ((transparency1 == null) ^ (transparency2 == null)) {
          return false;
        } else if (transparency1 != transparency2) {
          if (transparency1.getTransparency() != transparency2.getTransparency()) {
            return false;
          }
        }
        Texture texture1 = this.appearance.getTexture();
        Texture texture2 = appearance2.getTexture();
        if ((texture1 == null) ^ (texture2 == null)) {
          return false;
        } else if (texture1 != texture2) {
          if (texture1.getImage(0) != texture2.getImage(0)) {
            return false;
          }
        }
        return true;
      }
      return false;
    }
    
    @Override
    public int hashCode() {
      int code = 0;
      Material material = this.appearance.getMaterial();
      if (material != null) {
        Color3f color = new Color3f();
        material.getAmbientColor(color);
        code += color.hashCode();
        material.getDiffuseColor(color);
        code += color.hashCode();
        material.getEmissiveColor(color);
        code += color.hashCode();
        material.getSpecularColor(color);
        code += color.hashCode();
        code += Float.floatToIntBits(material.getShininess());
      }
      TransparencyAttributes transparency = this.appearance.getTransparencyAttributes();
      if (transparency != null) {
        code += Float.floatToIntBits(transparency.getTransparency());
      }
      Texture texture = this.appearance.getTexture();
      if (texture != null) {
        code += texture.getImage(0).hashCode();
      }
      return code;
    }
  }
}
