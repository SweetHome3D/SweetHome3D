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
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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
import javax.media.j3d.Node;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Texture;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
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
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.TexCoord2f;
import javax.vecmath.Vector3f;

import com.eteks.sweethome3d.j3d.Component3DManager;
import com.eteks.sweethome3d.j3d.Ground3D;
import com.eteks.sweethome3d.j3d.HomePieceOfFurniture3D;
import com.eteks.sweethome3d.j3d.Object3DBranch;
import com.eteks.sweethome3d.j3d.Room3D;
import com.eteks.sweethome3d.j3d.TextureManager;
import com.eteks.sweethome3d.j3d.Wall3D;
import com.eteks.sweethome3d.model.Camera;
import com.eteks.sweethome3d.model.CollectionEvent;
import com.eteks.sweethome3d.model.CollectionListener;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeEnvironment;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.viewcontroller.HomeController3D;
import com.sun.j3d.utils.geometry.GeometryInfo;
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
  private final Map<Selectable, Object3DBranch>    homeObjects = new HashMap<Selectable, Object3DBranch>();
  private Collection<Selectable>                   homeObjectsToUpdate;
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
   * Creates a 3D component that displays <code>home</code> walls, rooms and furniture, 
   * with no controller.
   * @throws IllegalRenderingStateException  if the canvas 3D displayed 
   *             by this component couldn't be created.
   */
  public HomeComponent3D(Home home) {
    this(home, null);  
  }
  
  /**
   * Creates a 3D component that displays <code>home</code> walls, rooms and furniture.
   * @throws IllegalRenderingStateException  if the canvas 3D displayed 
   *             by this component couldn't be created.
   */
  public HomeComponent3D(Home home, HomeController3D controller) {
    this(home, null, controller);
  }

  /**
   * Creates a 3D component that displays <code>home</code> walls, rooms  and furniture.
   * @throws IllegalRenderingStateException  if the canvas 3D displayed 
   *            by this component couldn't be created.
   */
  public HomeComponent3D(Home home,
                         UserPreferences  preferences,
                         HomeController3D controller) {
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
        // Correct the bottom of the hemisphere to avoid seeing a black line at the horizon
        float y = j != 0 ? sinBeta : -0.05f;
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
    final Ground3D ground3D = new Ground3D(home, 
        groundOriginX, groundOriginY, groundWidth, groundDepth, false);
    
    // Add a listener on ground color and texture properties change 
    this.groundColorAndTextureListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          ground3D.update();
          clearPrintedImageCache();
        }
      };
    HomeEnvironment homeEnvironment = home.getEnvironment();
    homeEnvironment.addPropertyChangeListener(
        HomeEnvironment.Property.GROUND_COLOR, this.groundColorAndTextureListener); 
    homeEnvironment.addPropertyChangeListener(
        HomeEnvironment.Property.GROUND_TEXTURE, this.groundColorAndTextureListener); 
    
    return ground3D;
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
  private void deleteObject(Selectable homeObject) {
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
    Room3D room3D = new Room3D(room, home);
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
      this.homeObjectsToUpdate = new HashSet<Selectable>(objects);
      // Invoke later the update of objects of homeObjectsToUpdate
      EventQueue.invokeLater(new Runnable () {
        public void run() {
          for (Selectable object : homeObjectsToUpdate) {
            Object3DBranch objectBranch = homeObjects.get(object);
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
}
