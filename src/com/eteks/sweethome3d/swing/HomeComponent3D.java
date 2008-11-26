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
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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
import javax.media.j3d.Group;
import javax.media.j3d.IllegalRenderingStateException;
import javax.media.j3d.Light;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Texture;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
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
import com.eteks.sweethome3d.model.CollectionEvent;
import com.eteks.sweethome3d.model.CollectionListener;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeEnvironment;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.InterruptedRecorderException;
import com.eteks.sweethome3d.model.PieceOfFurniture;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.viewcontroller.HomeController3D;
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
public class HomeComponent3D extends JComponent implements com.eteks.sweethome3d.viewcontroller.View, Printable {
  private enum ActionType {MOVE_CAMERA_FORWARD, MOVE_CAMERA_FAST_FORWARD, MOVE_CAMERA_BACKWARD, MOVE_CAMERA_FAST_BACKWARD,  
      ROTATE_CAMERA_YAW_LEFT, ROTATE_CAMERA_YAW_FAST_LEFT, ROTATE_CAMERA_YAW_RIGHT, ROTATE_CAMERA_YAW_FAST_RIGHT, 
      ROTATE_CAMERA_PITCH_UP, ROTATE_CAMERA_PITCH_DOWN}
  
  private final Home                               home;
  private final Map<Object, ObjectBranch>          homeObjects = new HashMap<Object, ObjectBranch>();
  private Collection<Object>                       homeObjectsToUpdate;
  private SimpleUniverse                           universe;
  // Listeners bound to home that updates 3D scene objects
  private PropertyChangeListener                   cameraChangeListener;
  private PropertyChangeListener                   homeCameraListener;
  private PropertyChangeListener                   skyColorListener;
  private PropertyChangeListener                   groundColorAndTextureListener;
  private PropertyChangeListener                   lightColorListener;
  private PropertyChangeListener                   wallsAlphaListener;
  private PropertyChangeListener                   drawingModeListener;
  private CollectionListener<Wall>                 wallListener;
  private PropertyChangeListener                   wallChangeListener;
  private CollectionListener<HomePieceOfFurniture> furnitureListener;
  private PropertyChangeListener                   furnitureChangeListener;
  private CollectionListener<Room>                 roomListener;
  private PropertyChangeListener                   roomChangeListener;
  // Offscreen printed image cache
  // Creating an offscreen buffer is a quite lengthy operation so we keep the last printed image in this field
  // This image should be set to null each time the 3D view changes
  private BufferedImage                            printedImage;

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
    home.removePropertyChangeListener(Home.Property.CAMERA, this.homeCameraListener);
    HomeEnvironment homeEnvironment = home.getEnvironment();
    homeEnvironment.removePropertyChangeListener(HomeEnvironment.Property.SKY_COLOR, this.skyColorListener);
    homeEnvironment.removePropertyChangeListener(HomeEnvironment.Property.SKY_TEXTURE, this.skyColorListener);
    homeEnvironment.removePropertyChangeListener(HomeEnvironment.Property.GROUND_COLOR, this.groundColorAndTextureListener);
    homeEnvironment.removePropertyChangeListener(HomeEnvironment.Property.GROUND_TEXTURE, this.groundColorAndTextureListener);
    homeEnvironment.removePropertyChangeListener(HomeEnvironment.Property.LIGHT_COLOR, this.lightColorListener);
    homeEnvironment.removePropertyChangeListener(HomeEnvironment.Property.WALLS_ALPHA, this.wallsAlphaListener);
    homeEnvironment.removePropertyChangeListener(HomeEnvironment.Property.DRAWING_MODE, this.drawingModeListener);
    home.getCamera().removePropertyChangeListener(this.cameraChangeListener);
    home.removeWallsListener(this.wallListener);
    for (Wall wall : home.getWalls()) {
      wall.removePropertyChangeListener(this.wallChangeListener);
    }
    home.removeFurnitureListener(this.furnitureListener);
    for (HomePieceOfFurniture piece : home.getFurniture()) {
      piece.removePropertyChangeListener(this.furnitureChangeListener);
    }
    home.removeRoomsListener(this.roomListener);
    for (Room room : home.getRooms()) {
      room.removePropertyChangeListener(this.roomChangeListener);
    }
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
    this.cameraChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          // Update view transform later to avoid flickering in case of multiple camera changes 
          EventQueue.invokeLater(new Runnable() {
            public void run() {
              updateView(view, home.getCamera(), home.getObserverCamera() == home.getCamera());
              updateViewPlatformTransform(viewPlatformTransform, home.getCamera());
            }
          });
        }
      };
    home.getCamera().addPropertyChangeListener(this.cameraChangeListener);
    this.homeCameraListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          updateView(view, home.getCamera(), home.getObserverCamera() == home.getCamera());
          updateViewPlatformTransform(viewPlatformTransform, home.getCamera());
          // Add camera change listener to new active camera
          ((Camera)ev.getOldValue()).removePropertyChangeListener(cameraChangeListener);
          home.getCamera().addPropertyChangeListener(cameraChangeListener);
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
    double frontClipDistance = observerCamera ? 2 : 5;
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
    final Appearance backgroundAppearance = new Appearance();
    ColoringAttributes backgroundColoringAttributes = new ColoringAttributes();
    backgroundAppearance.setColoringAttributes(backgroundColoringAttributes);
    // Allow background color and texture to change
    backgroundAppearance.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
    backgroundAppearance.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_READ);
    backgroundColoringAttributes.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
    
    Geometry halfSphereGeometry = createHalfSphereGeometry();   
    final Shape3D halfSphere = new Shape3D(halfSphereGeometry, backgroundAppearance);
    BranchGroup backgroundBranch = new BranchGroup();
    backgroundBranch.addChild(halfSphere);
    
    final Background background = new Background(backgroundBranch);
    updateBackgroundColorAndTexture(backgroundAppearance, home);
    background.setImageScaleMode(Background.SCALE_FIT_ALL);
    background.setApplicationBounds(new BoundingBox(
        new Point3d(-1E6, -1E6, -1E6), 
        new Point3d(1E6, 1E6, 1E6)));    
    
    // Add a listener on sky color and texture properties change 
    this.skyColorListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          updateBackgroundColorAndTexture(backgroundAppearance, home);
        }
      };
    home.getEnvironment().addPropertyChangeListener(
        HomeEnvironment.Property.SKY_COLOR, this.skyColorListener);
    home.getEnvironment().addPropertyChangeListener(
        HomeEnvironment.Property.SKY_TEXTURE, this.skyColorListener);
    return background;
  }

  /**
   * Returns a half sphere oriented inward and with texture ordinates 
   * that spread along an hemisphere. 
   */
  private Geometry createHalfSphereGeometry() {
    final int divisionCount = 48; 
    Point3f [] coords = new Point3f [divisionCount * divisionCount];
    TexCoord2f [] textureCoords = new TexCoord2f [divisionCount * divisionCount];
    for (int i = 0, k = 0; i < divisionCount; i++) {
      double alpha = i * 2 * Math.PI / divisionCount;
      float cosAlpha = (float)Math.cos(alpha);
      float sinAlpha = (float)Math.sin(alpha);
      double nextAlpha = (i  + 1) * 2 * Math.PI / divisionCount;
      float cosNextAlpha = (float)Math.cos(nextAlpha);
      float sinNextAlpha = (float)Math.sin(nextAlpha);
      for (int j = 0; j < divisionCount / 4; j++) {
        double beta = 2 * j * Math.PI / divisionCount;
        float cosBeta = (float)Math.cos(beta);
        float sinBeta = (float)Math.sin(beta);
        // Correct the bottom of the hemisphere to avoid seeing at black line at the horizon
        float y = j != 0 ? sinBeta : -0.01f;
        double nextBeta = 2 * (j + 1) * Math.PI / divisionCount;
        float cosNextBeta = (float)Math.cos(nextBeta);
        float sinNextBeta = (float)Math.sin(nextBeta);
        coords [k] = new Point3f(cosAlpha * cosBeta, y, sinAlpha * cosBeta);
        textureCoords [k++] = new TexCoord2f((float)i / divisionCount, sinBeta); 
        
        coords [k] = new Point3f(cosNextAlpha * cosBeta, y, sinNextAlpha * cosBeta);
        textureCoords [k++] = new TexCoord2f((float)(i + 1) / divisionCount, sinBeta); 
        
        coords [k] = new Point3f(cosNextAlpha * cosNextBeta, sinNextBeta, sinNextAlpha * cosNextBeta);
        textureCoords [k++] = new TexCoord2f((float)(i + 1) / divisionCount, sinNextBeta); 
        
        coords [k] = new Point3f(cosAlpha * cosNextBeta, sinNextBeta, sinAlpha * cosNextBeta);
        textureCoords [k++] = new TexCoord2f((float)i / divisionCount, sinNextBeta); 
      }
    }
    
    GeometryInfo geometryInfo = new GeometryInfo(GeometryInfo.QUAD_ARRAY);
    geometryInfo.setCoordinates(coords);
    geometryInfo.setTextureCoordinateParams(1, 2);
    geometryInfo.setTextureCoordinates(0, textureCoords);
    geometryInfo.indexify();
    geometryInfo.compact();
    Geometry halfSphereGeometry = geometryInfo.getIndexedGeometryArray();
    return halfSphereGeometry;
  }

  /**
   * Updates<code>backgroundAppearance</code> color and texture from <code>home</code> sky color and texture.
   */
  private void updateBackgroundColorAndTexture(final Appearance backgroundAppearance, Home home) {
    Color3f skyColor = new Color3f(new Color(home.getEnvironment().getSkyColor()));
    backgroundAppearance.getColoringAttributes().setColor(skyColor);
    HomeTexture skyTexture = home.getEnvironment().getSkyTexture();
    if (skyTexture != null) {
      final TextureManager imageManager = TextureManager.getInstance();
      imageManager.loadTexture(skyTexture.getImage(), 
          new TextureManager.TextureObserver() {
              public void textureUpdated(Texture texture) {
                backgroundAppearance.setTexture(texture);
              }
            });
    } else {
      backgroundAppearance.setTexture(null);
    }

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
    
    // Add a listener on ground color and texture properties change 
    this.groundColorAndTextureListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          updateGroundColorAndTexture(groundShape, home, 
              groundOriginX, groundOriginY, groundWidth, groundDepth);
        }
      };
    HomeEnvironment homeEnvironment = home.getEnvironment();
    homeEnvironment.addPropertyChangeListener(
        HomeEnvironment.Property.GROUND_COLOR, this.groundColorAndTextureListener); 
    homeEnvironment.addPropertyChangeListener(
        HomeEnvironment.Property.GROUND_TEXTURE, this.groundColorAndTextureListener); 
    
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
    Color3f groundColor = new Color3f(new Color(home.getEnvironment().getGroundColor()));
    final Appearance groundAppearance = groundShape.getAppearance();
    groundAppearance.getColoringAttributes().setColor(groundColor);
    HomeTexture groundTexture = home.getEnvironment().getGroundTexture();
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
    float groundOffset = 0f;
    List<Point3f> coords = new ArrayList<Point3f>();
    List<Integer> stripCounts = new ArrayList<Integer>();
    // First add the coordinates of the ground rectangle
    coords.add(new Point3f(groundOriginX, groundOffset, groundOriginY)); 
    coords.add(new Point3f(groundOriginX, groundOffset, groundOriginY + groundDepth));
    coords.add(new Point3f(groundOriginX + groundWidth, groundOffset, groundOriginY + groundDepth));
    coords.add(new Point3f(groundOriginX + groundWidth, groundOffset, groundOriginY));
    // Compute ground texture coordinates if necessary
    List<TexCoord2f> textureCoords = new ArrayList<TexCoord2f>();
    if (groundTexture != null) {
      textureCoords.add(new TexCoord2f(0, 0));
      textureCoords.add(new TexCoord2f(0, groundDepth / groundTexture.getHeight()));
      textureCoords.add(new TexCoord2f(groundWidth / groundTexture.getWidth(), groundDepth / groundTexture.getHeight()));
      textureCoords.add(new TexCoord2f(groundWidth / groundTexture.getWidth(), 0));
    }
    stripCounts.add(4);

    // Compute the union of the rooms
    Area roomsArea = new Area();
    for (Room room : home.getRooms()) {
      if (room.isFloorVisible()) {
        float [][] points = room.getPoints();
        if (points.length > 2) {
          roomsArea.add(new Area(getShape(points)));
        }
      }
    }
    // Then, define all the holes in ground from rooms area
    int pointsCount = 0;
    for (PathIterator it = roomsArea.getPathIterator(null); !it.isDone(); ) {
      float [] roomPoint = new float[2];
      if (it.currentSegment(roomPoint) == PathIterator.SEG_CLOSE) {
        stripCounts.add(pointsCount);
        pointsCount = 0;
      } else {
        coords.add(new Point3f(roomPoint [0], groundOffset, roomPoint [1]));
        if (groundTexture != null) {
          textureCoords.add(new TexCoord2f((roomPoint [0] - groundOriginX) / groundTexture.getWidth(), 
              (roomPoint [1] - groundOriginY) / groundTexture.getHeight()));
        }
        pointsCount++;
      }
      it.next();
    }

    GeometryInfo geometryInfo = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
    geometryInfo.setCoordinates (coords.toArray(new Point3f [coords.size()]));
    int [] stripCountsArray = new int [stripCounts.size()];
    for (int i = 0; i < stripCountsArray.length; i++) {
      stripCountsArray [i] = stripCounts.get(i);
    }
    geometryInfo.setStripCounts(stripCountsArray);
    geometryInfo.setContourCounts(new int [] {stripCountsArray.length});

    if (groundTexture != null) {
      geometryInfo.setTextureCoordinateParams(1, 2);
      geometryInfo.setTextureCoordinates(0, textureCoords.toArray(new TexCoord2f [textureCoords.size()]));
    }
    
    groundShape.setGeometry(geometryInfo.getIndexedGeometryArray());

    clearPrintedImageCache();
  }

  /**
   * Returns the shape matching the coordinates in <code>points</code> array.
   */
  private static Shape getShape(float [][] points) {
    GeneralPath path = new GeneralPath();
    path.moveTo(points [0][0], points [0][1]);
    for (int i = 1; i < points.length; i++) {
      path.lineTo(points [i][0], points [i][1]);
    }
    path.closePath();
    return path;
  }

  /**
   * Returns an array that cites <code>points</code> in reverse order.
   */
  private static float [][] getReversedArray(float [][] points) {
    List<float []> pointList = Arrays.asList(points);
    Collections.reverse(pointList);
    points = pointList.toArray(points);
    return points;
  }
  
  /**
   * Returns the lights of the scene.
   */
  private Light [] createLights(final Home home) {
    final Light [] lights = {
        new DirectionalLight(new Color3f(1, 1, 1), new Vector3f(1.5f, -0.8f, -1)),         
        new DirectionalLight(new Color3f(1, 1, 1), new Vector3f(-1.5f, -0.8f, -1)), 
        new DirectionalLight(new Color3f(1, 1, 1), new Vector3f(0, -0.8f, 1)), 
        new DirectionalLight(new Color3f(0.7f, 0.7f, 0.7f), new Vector3f(0, 1f, 0)), 
        new AmbientLight(new Color3f(0.2f, 0.2f, 0.2f))}; 
    for (int i = 0; i < lights.length - 1; i++) {
      // Allow directional lights color to change
      lights [i].setCapability(DirectionalLight.ALLOW_COLOR_WRITE);
      // Store default color in user data
      Color3f defaultColor = new Color3f();
      lights [i].getColor(defaultColor);
      lights [i].setUserData(defaultColor);
      updateLightColor(lights [i], home);
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
    home.getEnvironment().addPropertyChangeListener(
        HomeEnvironment.Property.LIGHT_COLOR, this.lightColorListener); 
    
    return lights;
  }

  /**
   * Updates<code>light</code> color from <code>home</code> light color.
   */
  private void updateLightColor(Light light, Home home) {
    Color3f defaultColor = (Color3f)light.getUserData();
    int lightColor = home.getEnvironment().getLightColor();
    light.setColor(new Color3f(((lightColor >>> 16) & 0xFF) / 256f * defaultColor.x,
                                ((lightColor >>> 8) & 0xFF) / 256f * defaultColor.y,
                                        (lightColor & 0xFF) / 256f * defaultColor.z));
    clearPrintedImageCache();
  }
  
  /**
   * Exports this 3D view to the given OBJ file.
   */
  public void exportToOBJ(String objName) throws RecorderException {
    try {
      String headerFormat = ResourceBundle.getBundle(HomeComponent3D.class.getName()).
          getString("exportToOBJ.header");      
      OBJWriter writer = new OBJWriter(objName, String.format(headerFormat, new Date()), -1);

      if (this.home.getWalls().size() > 0) {
        // Create a not alive new ground to be able to explore its coordinates without setting capabilities
        Rectangle2D homeBounds = getExportedHomeBounds();
        Node groundNode = createGroundNode(this.home, 
            (float)homeBounds.getX(), (float)homeBounds.getY(), 
            (float)homeBounds.getWidth(), (float)homeBounds.getHeight());
        writer.writeNode(groundNode, "ground");
      }
      
      // Write 3D walls 
      int i = 0;
      for (Wall wall : this.home.getWalls()) {
        // Create a not alive new wall to be able to explore its coordinates without setting capabilities 
        Wall3D wallNode = new Wall3D(wall, this.home, true);
        writer.writeNode(wallNode, "wall_" + ++i);
      }
      // Write 3D furniture 
      i = 0;
      for (HomePieceOfFurniture piece : this.home.getFurniture()) {
        if (piece.isVisible()) {
          // Create a not alive new piece to be able to explore its coordinates without setting capabilities
          HomePieceOfFurniture3D pieceNode = new HomePieceOfFurniture3D(piece, this.home, true, true);
          writer.writeNode(pieceNode, "piece_" + ++i);
        }
      }
      // Write 3D rooms 
      i = 0;
      for (Room room : this.home.getRooms()) {
        // Create a not alive new room to be able to explore its coordinates without setting capabilities 
        Room3D roomNode = new Room3D(room, this.home, true);
        writer.writeNode(roomNode, "room_" + ++i);
      }
      writer.close();
    } catch (InterruptedIOException ex) {
      throw new InterruptedRecorderException("Export to " + objName + " interrupted");
    } catch (IOException ex) {
      throw new RecorderException("Failed to export to " + objName, ex);
    } 
  }
  
  /**
   * Returns home bounds. 
   */
  private Rectangle2D getExportedHomeBounds() {
    // Compute bounds that include walls and furniture
    Rectangle2D homeBounds = updateObjectsBounds(null, this.home.getWalls());
    for (HomePieceOfFurniture piece : this.home.getFurniture()) {
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
    return updateObjectsBounds(homeBounds, this.home.getRooms());
  }
  
  /**
   * Updates <code>objectBounds</code> to include the bounds of <code>objects</code>.
   */
  private Rectangle2D updateObjectsBounds(Rectangle2D objectBounds,
                                          Collection<? extends Selectable> objects) {
    for (Selectable wall : objects) {
      for (float [] point : wall.getPoints()) {
        if (objectBounds == null) {
          objectBounds = new Rectangle2D.Float(point [0], point [1], 0, 0);
        } else {
          objectBounds.add(point [0], point [1]);
        }
      }
    }
    return objectBounds;
  }
  
  /**
   * Returns a <code>home</code> new tree node, with branches for each wall 
   * and piece of furniture of <code>home</code>. 
   */
  private Node createHomeTree(Home home) {
    Group homeRoot = createHomeRoot();
    // Add walls, pieces and rooms already available 
    for (Wall wall : home.getWalls()) {
      addWall(homeRoot, wall, home);
    }
    for (HomePieceOfFurniture piece : home.getFurniture()) {
      addPieceOfFurniture(homeRoot, piece, home);
    }
    for (Room room : home.getRooms()) {
      addRoom(homeRoot, room, home);
    }
    // Add wall, furniture, room listeners to home for further update
    addWallListener(home, homeRoot);
    addFurnitureListener(home, homeRoot);
    addRoomListener(home, homeRoot);
    // Add environment listeners
    addEnvironmentListeners(home);
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
   * Adds a wall listener to <code>home</code> walls  
   * that updates the scene <code>homeRoot</code>, each time a wall is added, updated or deleted. 
   */
  private void addWallListener(final Home home, final Group homeRoot) {
    this.wallChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          updateWall((Wall)ev.getSource());          
          updateObjects(home.getRooms());
        }
      };
    for (Wall wall : home.getWalls()) {
      wall.addPropertyChangeListener(this.wallChangeListener);
    }      
    this.wallListener = new CollectionListener<Wall>() {
        public void collectionChanged(CollectionEvent<Wall> ev) {
          Wall wall = ev.getItem();
          switch (ev.getType()) {
            case ADD :
              addWall(homeRoot, wall, home);
              wall.addPropertyChangeListener(wallChangeListener);
              break;
            case DELETE :
              deleteObject(wall);
              wall.removePropertyChangeListener(wallChangeListener);
              break;
          }
          updateObjects(home.getRooms());
        }
      };
    home.addWallsListener(this.wallListener);
  }

  /**
   * Adds a furniture listener to <code>home</code> that updates the scene <code>homeRoot</code>, 
   * each time a piece of furniture is added, updated or deleted. 
   */
  private void addFurnitureListener(final Home home, final Group homeRoot) {
    this.furnitureChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          if (!ev.getPropertyName().equals(HomePieceOfFurniture.Property.NAME.name())) {
            HomePieceOfFurniture piece = (HomePieceOfFurniture)ev.getSource();
            updatePieceOfFurniture(piece);
            // If piece is a door or a window, update walls that intersect with piece
            if (piece.isDoorOrWindow()) {
              updateObjects(home.getWalls());
            }
          }
        }
      };
    for (HomePieceOfFurniture piece : home.getFurniture()) {
      piece.addPropertyChangeListener(this.furnitureChangeListener);
    }      
    this.furnitureListener = new CollectionListener<HomePieceOfFurniture>() {
        public void collectionChanged(CollectionEvent<HomePieceOfFurniture> ev) {
          HomePieceOfFurniture piece = (HomePieceOfFurniture)ev.getItem();
          switch (ev.getType()) {
            case ADD :
              addPieceOfFurniture(homeRoot, piece, home);
              piece.addPropertyChangeListener(furnitureChangeListener);
              break;
            case DELETE :
              deleteObject(piece);
              piece.removePropertyChangeListener(furnitureChangeListener);
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
   * Adds a room listener to <code>home</code> rooms  
   * that updates the scene <code>homeRoot</code>, each time a room is added, updated or deleted. 
   */
  private void addRoomListener(final Home home, final Group homeRoot) {
    this.roomChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          updateRoom((Room)ev.getSource());
          groundColorAndTextureListener.propertyChange(null);
        }
      };
    for (Room room : home.getRooms()) {
      room.addPropertyChangeListener(this.roomChangeListener);
    }      
    this.roomListener = new CollectionListener<Room>() {
        public void collectionChanged(CollectionEvent<Room> ev) {
          Room room = ev.getItem();
          switch (ev.getType()) {
            case ADD :
              addRoom(homeRoot, room, home);
              room.addPropertyChangeListener(roomChangeListener);
              break;
            case DELETE :
              deleteObject(room);
              room.removePropertyChangeListener(roomChangeListener);
              break;
          }
          groundColorAndTextureListener.propertyChange(null);
        }
      };
    home.addRoomsListener(this.roomListener);
  }

  /**
   * Adds a walls alpha change listener and drawing mode change listener to <code>home</code> 
   * environment that updates the home scene objects appearance. 
   */
  private void addEnvironmentListeners(final Home home) {
    this.wallsAlphaListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          updateObjects(home.getWalls());
        }
      };
    home.getEnvironment().addPropertyChangeListener(
        HomeEnvironment.Property.WALLS_ALPHA, this.wallsAlphaListener); 
    this.drawingModeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          updateObjects(home.getWalls());
          updateObjects(home.getFurniture());
        }
      };
    home.getEnvironment().addPropertyChangeListener(
        HomeEnvironment.Property.DRAWING_MODE, this.drawingModeListener); 
  }

  /**
   * Adds to <code>homeRoot</code> a wall branch matching <code>wall</code>.
   */
  private void addWall(Group homeRoot, Wall wall, Home home) {
    Wall3D wall3D = new Wall3D(wall, home, false);
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
  private void addPieceOfFurniture(Group homeRoot, HomePieceOfFurniture piece, Home home) {
    HomePieceOfFurniture3D piece3D = new HomePieceOfFurniture3D(piece, home);
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
   * Adds to <code>homeRoot</code> a room branch matching <code>room</code>.
   */
  private void addRoom(Group homeRoot, Room room, Home home) {
    Room3D room3D = new Room3D(room, home, false);
    this.homeObjects.put(room, room3D);
    homeRoot.addChild(room3D);
    clearPrintedImageCache();
  }

  /**
   * Updates <code>room</code> geometry and its appearance.
   */
  private void updateRoom(Room room) {
    updateObjects(Arrays.asList(new Room [] {room}));
  }
  
  /**
   * Updates <code>objects</code> later. Should be invoked from Event Dispatch Thread.
   */
  private void updateObjects(Collection<? extends Selectable> objects) {
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

  private static final ColoringAttributes OUTLINE_COLORING_ATTRIBUTES = 
      new ColoringAttributes(new Color3f(), ColoringAttributes.FASTEST);
  private static final PolygonAttributes OUTLINE_POLYGON_ATTRIBUTES = 
      new PolygonAttributes(PolygonAttributes.POLYGON_LINE, PolygonAttributes.CULL_BACK, 0);
  private static final LineAttributes OUTLINE_LINE_ATTRIBUTES = 
      new LineAttributes(0.5f, LineAttributes.PATTERN_SOLID, true);

  /**
   * Root of wall branch.
   */
  private static class Wall3D extends ObjectBranch {
    private static final Material DEFAULT_MATERIAL = new Material();
    private static final Map<Integer, Material> materials = new HashMap<Integer, Material>();
    
    static {
      DEFAULT_MATERIAL.setCapability(Material.ALLOW_COMPONENT_READ);
    }
    
    private static final int LEFT_WALL_SIDE  = 0;
    private static final int RIGHT_WALL_SIDE = 1;
    
    private Home home;

    public Wall3D(Wall wall, Home home, boolean ignoreDrawingMode) {
      setUserData(wall);
      this.home = home;

      // Allow wall branch to be removed from its parent
      setCapability(BranchGroup.ALLOW_DETACH);
      // Allow to read branch shape children
      setCapability(BranchGroup.ALLOW_CHILDREN_READ);
      
      // Add wall left and right empty shapes to branch
      addChild(createWallPartShape(false));
      addChild(createWallPartShape(false));
      if (!ignoreDrawingMode) {
        // Add wall left and right empty outline shapes to branch
        addChild(createWallPartShape(true));
        addChild(createWallPartShape(true));
      }
      // Set wall shape geometry and appearance
      updateWallGeometry();
      updateWallAppearance();
    }

    /**
     * Returns a new wall part shape with no geometry  
     * and a default appearance with a white material.
     */
    private Node createWallPartShape(boolean outline) {
      Shape3D wallShape = new Shape3D();
      // Allow wall shape to change its geometry
      wallShape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
      wallShape.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
      wallShape.setCapability(Shape3D.ALLOW_APPEARANCE_READ);

      Appearance wallAppearance = new Appearance();
      wallShape.setAppearance(wallAppearance);
      wallAppearance.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_READ);
      TransparencyAttributes transparencyAttributes = new TransparencyAttributes();
      transparencyAttributes.setCapability(TransparencyAttributes.ALLOW_VALUE_WRITE);
      transparencyAttributes.setCapability(TransparencyAttributes.ALLOW_MODE_WRITE);
      wallAppearance.setTransparencyAttributes(transparencyAttributes);
      wallAppearance.setCapability(Appearance.ALLOW_RENDERING_ATTRIBUTES_READ);
      RenderingAttributes renderingAttributes = new RenderingAttributes();
      renderingAttributes.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
      wallAppearance.setRenderingAttributes(renderingAttributes);
      
      if (outline) {
        wallAppearance.setColoringAttributes(OUTLINE_COLORING_ATTRIBUTES);
        wallAppearance.setPolygonAttributes(OUTLINE_POLYGON_ATTRIBUTES);
        wallAppearance.setLineAttributes(OUTLINE_LINE_ATTRIBUTES);
      } else {
        wallAppearance.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
        wallAppearance.setMaterial(DEFAULT_MATERIAL);      
        wallAppearance.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
        // Mix texture and wall color
        TextureAttributes textureAttributes = new TextureAttributes ();
        textureAttributes.setTextureMode(TextureAttributes.MODULATE);
        wallAppearance.setTextureAttributes(textureAttributes);
      }
      
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
      Shape3D wallFilledShape = (Shape3D)getChild(wallSide);
      Shape3D wallOutlineShape = numChildren() > 2 
          ? (Shape3D)getChild(wallSide + 2)
          : null; 
      int currentGeometriesCount = wallFilledShape.numGeometries();
      for (Geometry wallGeometry : createWallGeometries(wallSide, texture)) {
        wallFilledShape.addGeometry(wallGeometry);
        if (wallOutlineShape != null) {
          wallOutlineShape.addGeometry(wallGeometry);
        }
      }
      for (int i = currentGeometriesCount - 1; i >= 0; i--) {
        wallFilledShape.removeGeometry(i);
        if (wallOutlineShape != null) {
          wallOutlineShape.removeGeometry(i);
        }
      }
    }
    
    /**
     * Returns <code>wall</code> geometries computed with windows or doors 
     * that intersect wall.
     */
    private Geometry [] createWallGeometries(int wallSide, HomeTexture texture) {
      Shape wallShape = HomeComponent3D.getShape(getWallSidePoints(wallSide));
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
          Shape pieceShape = HomeComponent3D.getShape(piece.getPoints());
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
      updateFilledWallSideAppearance(((Shape3D)getChild(LEFT_WALL_SIDE)).getAppearance(), 
          wall.getLeftSideTexture(), wall.getLeftSideColor());
      updateFilledWallSideAppearance(((Shape3D)getChild(RIGHT_WALL_SIDE)).getAppearance(), 
          wall.getRightSideTexture(), wall.getRightSideColor());
      if (numChildren() > 2) {
        updateOutlineWallSideAppearance(((Shape3D)getChild(LEFT_WALL_SIDE + 2)).getAppearance());
        updateOutlineWallSideAppearance(((Shape3D)getChild(RIGHT_WALL_SIDE + 2)).getAppearance());
      }
    }
    
    /**
     * Sets filled wall side appearance with its color, texture, transparency and visibility.
     */
    private void updateFilledWallSideAppearance(final Appearance wallSideAppearance, 
                                                final HomeTexture wallSideTexture,
                                                Integer wallSideColor) {
      if (wallSideTexture == null) {
        wallSideAppearance.setMaterial(getMaterial(wallSideColor));
        wallSideAppearance.setTexture(null);
      } else {
        // Update material and texture of wall side
        wallSideAppearance.setMaterial(DEFAULT_MATERIAL);
        final TextureManager textureManager = TextureManager.getInstance();
        textureManager.loadTexture(wallSideTexture.getImage(), 
            new TextureManager.TextureObserver() {
                public void textureUpdated(Texture texture) {
                  wallSideAppearance.setTexture(texture);
                }
              });
      }
      // Update wall side transparency
      float wallsAlpha = this.home.getEnvironment().getWallsAlpha();
      TransparencyAttributes transparencyAttributes = wallSideAppearance.getTransparencyAttributes();
      transparencyAttributes.setTransparency(wallsAlpha);
      // If walls alpha is equal to zero, turn off transparency to get better results 
      transparencyAttributes.setTransparencyMode(wallsAlpha == 0 
          ? TransparencyAttributes.NONE 
          : TransparencyAttributes.NICEST);      
      // Update wall side visibility
      RenderingAttributes renderingAttributes = wallSideAppearance.getRenderingAttributes();
      HomeEnvironment.DrawingMode drawingMode = this.home.getEnvironment().getDrawingMode();
      renderingAttributes.setVisible(drawingMode == null
          || drawingMode == HomeEnvironment.DrawingMode.FILL 
          || drawingMode == HomeEnvironment.DrawingMode.FILL_AND_OUTLINE);
    }
    
    /**
     * Sets outline wall side visibility.
     */
    private void updateOutlineWallSideAppearance(final Appearance wallSideAppearance) {
      // Update wall side visibility
      RenderingAttributes renderingAttributes = wallSideAppearance.getRenderingAttributes();
      HomeEnvironment.DrawingMode drawingMode = this.home.getEnvironment().getDrawingMode();
      renderingAttributes.setVisible(drawingMode == HomeEnvironment.DrawingMode.OUTLINE 
          || drawingMode == HomeEnvironment.DrawingMode.FILL_AND_OUTLINE);
    }
    
    private Material getMaterial(Integer color) {
      if (color != null) {
        Material material = materials.get(color); 
        if (material == null) {
          Color3f materialColor = new Color3f(((color >>> 16) & 0xFF) / 256f,
                                              ((color >>> 8) & 0xFF) / 256f,
                                                      (color & 0xFF) / 256f);
          material = new Material(materialColor, new Color3f(), materialColor, materialColor, 64);
          material.setCapability(Material.ALLOW_COMPONENT_READ);
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

    private Home home;
    
    public HomePieceOfFurniture3D(HomePieceOfFurniture piece, Home home) {
      this(piece, home, false, false);
    }

    public HomePieceOfFurniture3D(HomePieceOfFurniture piece, 
                                  Home home, 
                                  boolean ignoreDrawingMode, 
                                  boolean waitModelLoadingEnd) {
      setUserData(piece);      
      this.home = home;

      // Allow piece branch to be removed from its parent
      setCapability(BranchGroup.ALLOW_DETACH);
      // Allow to read branch transform child
      setCapability(BranchGroup.ALLOW_CHILDREN_READ);
      
      createPieceOfFurnitureNode(ignoreDrawingMode, waitModelLoadingEnd);
    }

    /**
     * Creates the piece node with its transform group and add it to the piece branch. 
     */
    private void createPieceOfFurnitureNode(final boolean ignoreDrawingMode, 
                                            boolean waitModelLoadingEnd) {
      final TransformGroup pieceTransformGroup = new TransformGroup();
      // Allow the change of the transformation that sets piece size and position
      pieceTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
      pieceTransformGroup.setCapability(Group.ALLOW_CHILDREN_READ);
      addChild(pieceTransformGroup);

      if (waitModelLoadingEnd) {
        BranchGroup modelBranch = createModelBranchGroup(ignoreDrawingMode);
        pieceTransformGroup.addChild(modelBranch);
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
              final BranchGroup modelBranch = createModelBranchGroup(ignoreDrawingMode);              
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
        Material material = new Material(materialColor, new Color3f(), materialColor, materialColor, 64);
        setMaterial(getFilledModelNode(), material);
      } else {
        // Set default material of model
        setMaterial(getFilledModelNode(), null);
      }
    }

    /**
     * Returns the node of the filled model.
     */
    private Node getFilledModelNode() {
      TransformGroup transformGroup = (TransformGroup)getChild(0);
      BranchGroup branchGroup = (BranchGroup)transformGroup.getChild(0);
      return branchGroup.getChild(0);
    }

    /**
     * Returns the node of the outline model.
     */
    private Node getOutlineModelNode() {
      TransformGroup transformGroup = (TransformGroup)getChild(0);
      BranchGroup branchGroup = (BranchGroup)transformGroup.getChild(0);
      if (branchGroup.numChildren() > 1) {
        return branchGroup.getChild(1);
      } else {
        return null;
      }
    }

    /**
     * Sets whether this piece model is visible or not.
     */
    private void updatePieceOfFurnitureVisibility() {
      HomePieceOfFurniture piece = (HomePieceOfFurniture)getUserData();
      HomeEnvironment.DrawingMode drawingMode = this.home.getEnvironment().getDrawingMode();
      // Update visibility of filled model shapes
      setVisible(getFilledModelNode(), piece.isVisible()
          && (drawingMode == null
              || drawingMode == HomeEnvironment.DrawingMode.FILL 
              || drawingMode == HomeEnvironment.DrawingMode.FILL_AND_OUTLINE));
      Node outlineModelNode = getOutlineModelNode();
      if (outlineModelNode != null) {
        // Update visibility of outline model shapes
        setVisible(outlineModelNode, piece.isVisible()
            && (drawingMode == HomeEnvironment.DrawingMode.OUTLINE
                || drawingMode == HomeEnvironment.DrawingMode.FILL_AND_OUTLINE));
      }
    }

    /**
     * Sets whether this piece model is mirrored or not.
     */
    private void updatePieceOfFurnitureModelMirrored() {
      HomePieceOfFurniture piece = (HomePieceOfFurniture)getUserData();
      // Cull front or back model faces whether its model is mirrored or not
      setCullFace(getFilledModelNode(), 
          piece.isModelMirrored() ^ piece.isBackFaceShown() 
              ? PolygonAttributes.CULL_FRONT 
              : PolygonAttributes.CULL_BACK);
      // Flip normals if back faces of model are shown
      if (piece.isBackFaceShown()) {
        setBackFaceNormalFlip(getFilledModelNode(), true);
      }
    }

    /**
     * Returns a new branch group with its model node as a child.
     */
    private BranchGroup createModelBranchGroup(boolean ignoreDrawingMode) {
      BranchGroup modelBranch = new BranchGroup();
      // Add model node to branch group
      Node filledModelNode = getModelNode();
      modelBranch.addChild(filledModelNode);
      if (!ignoreDrawingMode) {
        // Add outline model node 
        modelBranch.addChild(createOutlineModelNode(filledModelNode));
      }
      // Allow appearance change on all children
      setAppearanceChangeCapability(modelBranch);
      return modelBranch;
    }
    
    /**
     * Returns the 3D model of this piece that fits in a 1 unit wide box 
     * centered at the origin. 
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
     * Returns a clone of the given node with an outline appearance on its shapes.
     */
    private Node createOutlineModelNode(Node modelNode) {
      Node node = modelNode.cloneTree();
      setOutlineAppearance(node);
      return node;
    }
    
    /**
     * Sets the outline appearance on all the children of <code>node</code>.
     */
    private void setOutlineAppearance(Node node) {
      if (node instanceof Group) {
        Enumeration enumeration = ((Group)node).getAllChildren(); 
        while (enumeration.hasMoreElements()) {
          setOutlineAppearance((Node)enumeration.nextElement());
        }
      } else if (node instanceof Shape3D) {        
        Appearance outlineAppearance = new Appearance();
        ((Shape3D)node).setAppearance(outlineAppearance);
        outlineAppearance.setCapability(Appearance.ALLOW_RENDERING_ATTRIBUTES_READ);
        RenderingAttributes renderingAttributes = new RenderingAttributes();
        renderingAttributes.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
        outlineAppearance.setRenderingAttributes(renderingAttributes);
        outlineAppearance.setColoringAttributes(OUTLINE_COLORING_ATTRIBUTES);
        outlineAppearance.setPolygonAttributes(OUTLINE_POLYGON_ATTRIBUTES);
        outlineAppearance.setLineAttributes(OUTLINE_LINE_ATTRIBUTES);
      }
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
   * Root of room branch.
   */
  private static class Room3D extends ObjectBranch {
    private static final Material DEFAULT_MATERIAL = new Material();
    private static final Map<Integer, Material> materials = new HashMap<Integer, Material>();
    
    static {
      DEFAULT_MATERIAL.setCapability(Material.ALLOW_COMPONENT_READ);
    }
    
    private static final int FLOOR_PART  = 0;
    private static final int CEILING_PART = 1;
    
    private Home home;

    public Room3D(Room room, Home home, boolean ignoreInvisiblePart) {
      setUserData(room);
      this.home = home;

      // Allow room branch to be removed from its parent
      setCapability(BranchGroup.ALLOW_DETACH);
      // Allow to read branch shape children
      setCapability(BranchGroup.ALLOW_CHILDREN_READ);
      
      // Add room floor and cellar empty shapes to branch
      addChild(createRoomPartShape());
      addChild(createRoomPartShape());
      // Set room shape geometry and appearance
      updateRoomGeometry();
      updateRoomAppearance();
      
      if (ignoreInvisiblePart) {
        if (!room.isCeilingVisible()) {
          removeChild(CEILING_PART);
        }
        if (!room.isFloorVisible()) {
          removeChild(FLOOR_PART);
        }
      }
    }

    /**
     * Returns a new wall part shape with no geometry  
     * and a default appearance with a white material.
     */
    private Node createRoomPartShape() {
      Shape3D roomShape = new Shape3D();
      // Allow wall shape to change its geometry
      roomShape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
      roomShape.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
      roomShape.setCapability(Shape3D.ALLOW_APPEARANCE_READ);

      Appearance roomAppearance = new Appearance();
      roomShape.setAppearance(roomAppearance);
      roomAppearance.setCapability(Appearance.ALLOW_RENDERING_ATTRIBUTES_READ);
      RenderingAttributes renderingAttributes = new RenderingAttributes();
      renderingAttributes.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
      roomAppearance.setRenderingAttributes(renderingAttributes);
      roomAppearance.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
      roomAppearance.setMaterial(DEFAULT_MATERIAL);      
      roomAppearance.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
      // Mix texture and wall color
      TextureAttributes textureAttributes = new TextureAttributes ();
      textureAttributes.setTextureMode(TextureAttributes.MODULATE);
      roomAppearance.setTextureAttributes(textureAttributes);
      
      return roomShape;
    }

    @Override
    public void update() {
      updateRoomGeometry();
      updateRoomAppearance();
    }
    
    /**
     * Sets the 3D geometry of this room shapes that matches its 2D geometry.  
     */
    private void updateRoomGeometry() {
      updateRoomSideGeometry(FLOOR_PART, ((Room)getUserData()).getFloorTexture());
      updateRoomSideGeometry(CEILING_PART, ((Room)getUserData()).getCeilingTexture());
    }
    
    private void updateRoomSideGeometry(int roomSide, HomeTexture texture) {
      Shape3D roomShape = (Shape3D)getChild(roomSide);
      int currentGeometriesCount = roomShape.numGeometries();
      for (Geometry roomGeometry : createRoomGeometries(roomSide, texture)) {
        roomShape.addGeometry(roomGeometry);
      }
      for (int i = currentGeometriesCount - 1; i >= 0; i--) {
        roomShape.removeGeometry(i);
      }
    }
    
    /**
     * Returns room geometry computed from its points.
     */
    private Geometry [] createRoomGeometries(int roomPart, HomeTexture texture) {
      Room room = (Room)getUserData();
      float [][] points = room.getPoints();
      if (points.length > 2) {
        boolean clockwise = room.isClockwise();
        if (clockwise && roomPart == FLOOR_PART
            || !clockwise && roomPart == CEILING_PART) {
          // Reverse points order if they are in the good order
          points = getReversedArray(points);
        }
        
        // If room isn't singular retrieve all the points of its different polygons 
        List<float [][]> roomPoints = new ArrayList<float[][]>();
        if (!room.isSingular()) {        
          GeneralPath roomShape = new GeneralPath();
          roomShape.moveTo(points [0][0], points [0][1]);
          for (int i = 1; i < points.length; i++) {
            roomShape.lineTo(points [i][0], points [i][1]);
          }
          roomShape.closePath();
          // Retrieve the points of the different polygons 
          // and reverse their points order if necessary
          Area roomArea = new Area(roomShape);
          List<float []> currentPathPoints = new ArrayList<float[]>();
          roomPoints = new ArrayList<float[][]>();
          for (PathIterator it = roomArea.getPathIterator(null); !it.isDone(); ) {
            float [] roomPoint = new float[2];
            switch (it.currentSegment(roomPoint)) {
              case PathIterator.SEG_MOVETO : 
                currentPathPoints.add(roomPoint);
                break;
              case PathIterator.SEG_LINETO : 
                currentPathPoints.add(roomPoint);
                break;
              case PathIterator.SEG_CLOSE :
                float [][] pathPoints = 
                    currentPathPoints.toArray(new float [currentPathPoints.size()][]);
                boolean pathPointsClockwise = new Room(pathPoints).isClockwise();
                if (pathPointsClockwise && roomPart == FLOOR_PART
                    || !pathPointsClockwise && roomPart == CEILING_PART) {
                  pathPoints = getReversedArray(pathPoints);
                }
                roomPoints.add(pathPoints);
                currentPathPoints.clear();
                break;
            }
            it.next();        
          }
        } else {
          roomPoints = Arrays.asList(new float [][][] {points});
        }
        
        Geometry [] geometries = new Geometry [roomPoints.size()];
        for (int i = 0; i < geometries.length; i++) {
          float [][] geometryPoints = roomPoints.get(i);
          // Compute room coordinates
          Point3f [] coords = new Point3f [geometryPoints.length];
          for (int j = 0; j < geometryPoints.length; j++) {
            float y = roomPart == FLOOR_PART 
                ? 0 
                : getRoomHeightAt(geometryPoints [j][0], geometryPoints [j][1]);
            coords [j] = new Point3f(geometryPoints [j][0], y, geometryPoints [j][1]);
          }
          GeometryInfo geometryInfo = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
          geometryInfo.setCoordinates (coords);
          geometryInfo.setStripCounts(new int [] {coords.length});
          // Compute room texture coordinates
          if (texture != null) {
            TexCoord2f [] textureCoords = new TexCoord2f [geometryPoints.length];
            for (int j = 0; j < geometryPoints.length; j++) {
              textureCoords [j] = new TexCoord2f(geometryPoints[j][0] / texture.getWidth(), 
                  geometryPoints[j][1] / texture.getHeight());
            }
            geometryInfo.setTextureCoordinateParams(1, 2);
            geometryInfo.setTextureCoordinates(0, textureCoords);
          }
          
          // Generate normals
          new NormalGenerator(0).generateNormals(geometryInfo);
          geometries [i] = geometryInfo.getIndexedGeometryArray();
        }
        return geometries;
      } else {
        return new Geometry [0];
      }
    }
    
    /**
     * Returns the room height at the given point. 
     */
    private float getRoomHeightAt(float x, float y) {
      double smallestDistance = Float.POSITIVE_INFINITY;
      float roomHeight = this.home.getWallHeight();
      // Search the closest wall point to x, y
      for (Wall wall : this.home.getWalls()) {
        Float wallHeightAtStart = wall.getHeight();
        float [][] points = wall.getPoints();
        for (int i = 0; i < points.length; i++) {
          double distanceToWallPoint = Point2D.distanceSq(points [i][0], points [i][1], x, y);
          if (distanceToWallPoint < smallestDistance) {
            smallestDistance = distanceToWallPoint; 
            if (i == 0 || i == 3) { // Wall start
              roomHeight = wallHeightAtStart != null 
                  ? wallHeightAtStart 
                  : this.home.getWallHeight();
            } else { // Wall end
              roomHeight = wall.isTrapezoidal() 
                  ? wall.getHeightAtEnd() 
                  : (wallHeightAtStart != null ? wallHeightAtStart : this.home.getWallHeight());
            }
          }
        }
      }
      return roomHeight;
    }

    /**
     * Sets room appearance with its color, texture.
     */
    private void updateRoomAppearance() {
      Room room = (Room)getUserData();
      updateRoomSideAppearance(((Shape3D)getChild(FLOOR_PART)).getAppearance(), 
          room.getFloorTexture(), room.getFloorColor(), room.isFloorVisible());
      updateRoomSideAppearance(((Shape3D)getChild(CEILING_PART)).getAppearance(), 
          room.getCeilingTexture(), room.getCeilingColor(), room.isCeilingVisible());
    }
    
    /**
     * Sets room side appearance with its color, texture and visibility.
     */
    private void updateRoomSideAppearance(final Appearance roomSideAppearance, 
                                          final HomeTexture roomSideTexture,
                                          Integer roomSideColor,
                                          boolean visible) {
      if (roomSideTexture == null) {
        roomSideAppearance.setMaterial(getMaterial(roomSideColor));
        roomSideAppearance.setTexture(null);
      } else {
        // Update material and texture of room side
        roomSideAppearance.setMaterial(DEFAULT_MATERIAL);
        final TextureManager textureManager = TextureManager.getInstance();
        textureManager.loadTexture(roomSideTexture.getImage(), 
            new TextureManager.TextureObserver() {
                public void textureUpdated(Texture texture) {
                  roomSideAppearance.setTexture(texture);
                }
              });
      }
      // Update room side visibility
      RenderingAttributes renderingAttributes = roomSideAppearance.getRenderingAttributes();
      renderingAttributes.setVisible(visible);
    }
    
    private Material getMaterial(Integer color) {
      if (color != null) {
        Material material = materials.get(color); 
        if (material == null) {
          Color3f materialColor = new Color3f(((color >>> 16) & 0xFF) / 256f,
                                               ((color >>> 8) & 0xFF) / 256f,
                                                       (color & 0xFF) / 256f);
          material = new Material(materialColor, new Color3f(), materialColor, materialColor, 64);
          material.setCapability(Material.ALLOW_COMPONENT_READ);
          // Store created materials in cache
          materials.put(color, material);
        }
        return material;
      } else {
        return DEFAULT_MATERIAL;
      }
    }
  }
}
