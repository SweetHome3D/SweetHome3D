/*
 * ModelPreviewComponent 16 jan. 2010
 *
 * Sweet Home 3D, Copyright (c) 2007-2010 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicReference;

import javax.imageio.ImageIO;
import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Geometry;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.GraphicsConfigTemplate3D;
import javax.media.j3d.Group;
import javax.media.j3d.Light;
import javax.media.j3d.Link;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.PhysicalBody;
import javax.media.j3d.PhysicalEnvironment;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.MouseInputAdapter;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import com.eteks.sweethome3d.j3d.Component3DManager;
import com.eteks.sweethome3d.j3d.ModelManager;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.tools.TemporaryURLContent;
import com.sun.j3d.exp.swing.JCanvas3D;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.Viewer;
import com.sun.j3d.utils.universe.ViewingPlatform;

/**
 * Super class of 3D preview component for model. 
 */
public class ModelPreviewComponent extends JComponent {
  private SimpleUniverse     universe;
  private JPanel             component3DPanel;
  private Component          component3D;
  private BranchGroup        sceneTree;
  private float              viewYaw = (float) Math.PI / 8;
  private float              viewPitch = -(float)Math.PI / 16;
  private float              viewScale = 1;
  private Object             iconImageLock;
  private Content            model; 

  /**
   * Returns an 3D model preview component.
   */
  public ModelPreviewComponent() {
    this(false);
  }
  
  /**
   * Returns an 3D model preview component that lets the user change its pitch and scale 
   * if <code>pitchAndScaleChangeSupported</code> is <code>true</code>.
   */
  public ModelPreviewComponent(boolean pitchAndScaleChangeSupported) {
    setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

    this.sceneTree = createSceneTree();
    
    this.component3DPanel = new JPanel();
    setLayout(new BorderLayout());
    add(this.component3DPanel);

    GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
    if (graphicsEnvironment.getScreenDevices().length == 1) {
      // If only one screen device is available, create 3D component immediately, 
      // otherwise create it once the screen device of the parent is known
      createComponent3D(graphicsEnvironment.getDefaultScreenDevice().getDefaultConfiguration(), pitchAndScaleChangeSupported);
    }

    // Add an ancestor listener to create 3D component and its universe once this component is made visible 
    // and clean up universe once its parent frame is disposed
    addAncestorListener(pitchAndScaleChangeSupported);
  }

  /**
   * Returns component preferred size.
   */
  @Override
  public Dimension getPreferredSize() {
    if (isPreferredSizeSet()) {
      return super.getPreferredSize();
    } else {
      return new Dimension(200, 200);
    }
  }

  /**
   * Returns the 3D component of this component.
   */
  JComponent getComponent3D() {
    return this.component3DPanel;
  }
  
  /**
   * Adds an ancestor listener to this component to manage the creation of the 3D component and its universe 
   * and clean up the universe.  
   */
  private void addAncestorListener(final boolean pitchAndScaleChangeSupported) {
    addAncestorListener(new AncestorListener() {
        public void ancestorAdded(AncestorEvent ev) {
          if (component3D == null) {
            createComponent3D(ev.getAncestor().getGraphicsConfiguration(), pitchAndScaleChangeSupported);
          }
          if (universe == null) {
            createUniverse();
          }
        }
        
        public void ancestorRemoved(AncestorEvent ev) {
          if (universe != null) {
            disposeUniverse();
          }
        }
        
        public void ancestorMoved(AncestorEvent ev) {
        }        
      });
  }
  
  /**
   * Creates the 3D component associated with the given <code>configuration</code> device.
   */
  private void createComponent3D(GraphicsConfiguration graphicsConfiguration, 
                              boolean pitchAndScaleChangeSupported) {
    if (Boolean.valueOf(System.getProperty("com.eteks.sweethome3d.j3d.useOffScreen3DView", "false"))) {
      GraphicsConfigTemplate3D gc = new GraphicsConfigTemplate3D();
      gc.setSceneAntialiasing(GraphicsConfigTemplate3D.PREFERRED);
      try {
        // Instantiate JCanvas3DWithNotifiedPaint inner class by reflection
        // to be able to run under Java 3D 1.3
        this.component3D = (Component)Class.forName(ModelPreviewComponent.class.getName() + "$JCanvas3DWithNotifiedPaint").
            getConstructor(ModelPreviewComponent.class, GraphicsConfigTemplate3D.class).newInstance(this, gc);
      } catch (ClassNotFoundException ex) {
        throw new UnsupportedOperationException("Java 3D 1.5 required to display an offscreen 3D view");
      } catch (Exception ex) {
        UnsupportedOperationException ex2 = new UnsupportedOperationException();
        ex2.initCause(ex);
        throw ex2;
      }
    } else {
      this.component3D = Component3DManager.getInstance().getOnscreenCanvas3D(graphicsConfiguration,
          new Component3DManager.RenderingObserver() {
            public void canvas3DPreRendered(Canvas3D canvas3d) {
            }
            
            public void canvas3DPostRendered(Canvas3D canvas3d) {
            }
            
            public void canvas3DSwapped(Canvas3D canvas3d) {
              ModelPreviewComponent.this.canvas3DSwapped();
            }            
          });
    }
    this.component3D.setBackground(new Color(0xE5E5E5));

    // Layout 3D component
    this.component3DPanel.setLayout(new GridLayout());
    this.component3DPanel.add(this.component3D);
    this.component3D.setFocusable(false);      
    addMouseListeners(this.component3D, pitchAndScaleChangeSupported);
  }

  /**
   * A <code>JCanvas</code> canvas that sends a notification when it's drawn.
   */
  private static class JCanvas3DWithNotifiedPaint extends JCanvas3D {
    private final ModelPreviewComponent homeComponent3D;

    public JCanvas3DWithNotifiedPaint(ModelPreviewComponent component,
                                      GraphicsConfigTemplate3D template) {
      super(template);
      this.homeComponent3D = component;
    }

    public void paintComponent(Graphics g) {
      super.paintComponent(g);
      this.homeComponent3D.canvas3DSwapped();
    }
  }
  
  /**
   * Adds an AWT mouse listener to component that will update view platform transform.  
   */
  private void addMouseListeners(final Component component3D, 
                                 final boolean pitchAndScaleChangeSupported) {
    final float ANGLE_FACTOR = 0.02f;
    final float ZOOM_FACTOR = 0.02f;
    MouseInputAdapter mouseListener = new MouseInputAdapter() {
        private int xLastMouseMove;
        private int yLastMouseMove;
        
        @Override
        public void mousePressed(MouseEvent ev) {
          this.xLastMouseMove = ev.getX();
          this.yLastMouseMove = ev.getY();
        }
  
        @Override
        public void mouseDragged(MouseEvent ev) {
          if (getModelNode() != null) {
            // Mouse move along X axis changes yaw 
            setViewYaw(getViewYaw() - ANGLE_FACTOR * (ev.getX() - this.xLastMouseMove));    
            this.xLastMouseMove = ev.getX();
            
            if (pitchAndScaleChangeSupported) {
              if (ev.isAltDown()) {
                // Mouse move along Y axis with Alt down changes scale
                setViewScale(Math.max(0.5f, Math.min(1.3f, getViewScale() * (float)Math.exp((ev.getY() - this.yLastMouseMove) * ZOOM_FACTOR))));
              } else {
                // Mouse move along Y axis changes pitch
                setViewPitch(Math.max(-(float)Math.PI / 4, Math.min(0, getViewPitch() - ANGLE_FACTOR * (ev.getY() - this.yLastMouseMove))));
              }
              this.yLastMouseMove = ev.getY();
            }
          }
        }
      };

    component3D.addMouseListener(mouseListener);
    component3D.addMouseMotionListener(mouseListener);
    
    if (pitchAndScaleChangeSupported) {
      component3D.addMouseWheelListener(new MouseWheelListener() {
          public void mouseWheelMoved(MouseWheelEvent ev) {
            // Mouse move along Y axis with Alt down changes scale
            setViewScale(Math.max(0.5f, Math.min(1.3f, getViewScale() * (float)Math.exp(ev.getWheelRotation() * ZOOM_FACTOR))));
          }
        });
    }
    
    // Redirect mouse events to the 3D component
    for (final MouseListener l : getMouseListeners()) {
      component3D.addMouseListener(new MouseListener() {
          public void mouseReleased(MouseEvent ev) {
            l.mouseReleased(SwingUtilities.convertMouseEvent(ev.getComponent(), ev, component3D));
          }
          
          public void mousePressed(MouseEvent ev) {
            l.mousePressed(SwingUtilities.convertMouseEvent(ev.getComponent(), ev, component3D));
          }
          
          public void mouseExited(MouseEvent ev) {
            l.mouseExited(SwingUtilities.convertMouseEvent(ev.getComponent(), ev, component3D));
          }
          
          public void mouseEntered(MouseEvent ev) {
            l.mouseEntered(SwingUtilities.convertMouseEvent(ev.getComponent(), ev, component3D));
          }
          
          public void mouseClicked(MouseEvent ev) {
            l.mouseClicked(SwingUtilities.convertMouseEvent(ev.getComponent(), ev, component3D));
          }
        });
    }
    for (final MouseMotionListener l : getMouseMotionListeners()) {
      component3D.addMouseMotionListener(new MouseMotionListener() {
          public void mouseMoved(MouseEvent ev) {
            l.mouseMoved(SwingUtilities.convertMouseEvent(ev.getComponent(), ev, component3D));
          }
          
          public void mouseDragged(MouseEvent ev) {
            l.mouseDragged(SwingUtilities.convertMouseEvent(ev.getComponent(), ev, component3D));
          }
        });
    }
  }

  /**
   * Creates universe bound to the 3D component.
   */
  private void createUniverse() {
    Canvas3D canvas3D;
    if (this.component3D instanceof Canvas3D) {
      canvas3D = (Canvas3D)this.component3D;
    } else {
      try {
        // Call JCanvas3D#getOffscreenCanvas3D by reflection to be able to run under Java 3D 1.3
        canvas3D = (Canvas3D)Class.forName("com.sun.j3d.exp.swing.JCanvas3D").getMethod("getOffscreenCanvas3D").invoke(this.component3D);
      } catch (Exception ex) {
        UnsupportedOperationException ex2 = new UnsupportedOperationException();
        ex2.initCause(ex);
        throw ex2;
      }
    }    
    // Create a universe bound to component 3D
    ViewingPlatform viewingPlatform = new ViewingPlatform();
    Viewer viewer = new Viewer(canvas3D);
    this.universe = new SimpleUniverse(viewingPlatform, viewer);
    // Set viewer location 
    updateViewPlatformTransform(this.universe.getViewingPlatform().getViewPlatformTransform(), 
        getViewYaw(), getViewPitch(), getViewScale());
    // Link scene to universe
    this.universe.addBranchGraph(this.sceneTree);

    revalidate();
    repaint();
    if (OperatingSystem.isMacOSX()) {
      final Component root = SwingUtilities.getRoot(this);
      EventQueue.invokeLater(new Runnable() {
          public void run() {
            // Request focus again even if dialog isn't supposed to have lost focus !
            if (KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow() != root) {
              root.requestFocus();
            }
          }
        });
    } 
  }
  
  /**
   * Disposes universe bound to canvas.
   */
  private void disposeUniverse() {
    // Unlink scene to universe
    this.universe.getLocale().removeBranchGraph(this.sceneTree);
    this.universe.cleanup();
    this.universe = null;
  }
  
  /**
   * Creates a view bound to the universe that views current model from a point of view oriented with 
   * <code>yaw</code> and <code>pitch</code> angles.
   */
  View createView(float yaw, float pitch, float scale, int projectionPolicy) {
    if (this.universe == null) {
      createUniverse();
    }
    // Reuse same physical body and environment
    PhysicalBody physicalBody = this.universe.getViewer().getPhysicalBody();
    PhysicalEnvironment physicalEnvironment = this.universe.getViewer().getPhysicalEnvironment();
    
    // Create a view associated with canvas3D
    View view = new View();
    view.setPhysicalBody(physicalBody);
    view.setPhysicalEnvironment(physicalEnvironment);
    view.setProjectionPolicy(projectionPolicy);
    // Create a viewing platform and attach it to view and universe locale
    ViewingPlatform viewingPlatform = new ViewingPlatform();
    viewingPlatform.setUniverse(this.universe);
    this.universe.getLocale().addBranchGraph(
        (BranchGroup)viewingPlatform.getViewPlatformTransform().getParent());
    view.attachViewPlatform(viewingPlatform.getViewPlatform());

    // Set user point of view depending on yaw and pitch angles
    updateViewPlatformTransform(viewingPlatform.getViewPlatformTransform(), yaw, pitch, scale);
    return view;
  }
  
  /**
   * Returns the <code>yaw</code> angle used by view platform transform.
   */
  protected float getViewYaw() {
    return this.viewYaw;
  }
  
  /**
   * Sets the <code>yaw</code> angle used by view platform transform.
   */
  protected void setViewYaw(float viewYaw) {
    this.viewYaw = viewYaw;
    if (this.universe != null) {
      updateViewPlatformTransform(this.universe.getViewingPlatform().getViewPlatformTransform(), 
          getViewYaw(), getViewPitch(), getViewScale());
    }
  }

  /**
   * Returns the zoom factor used by view platform transform.
   */
  protected float getViewScale() {
    return this.viewScale;
  }
  
  /**
   * Sets the zoom factor used by view platform transform.
   */
  protected void setViewScale(float viewScale) {
    this.viewScale = viewScale;
    if (this.universe != null) {
      updateViewPlatformTransform(this.universe.getViewingPlatform().getViewPlatformTransform(), 
          getViewYaw(), getViewPitch(), getViewScale());
    }
  }

  /**
   * Returns the <code>pitch</code> angle used by view platform transform.
   */
  protected float getViewPitch() {
    return this.viewPitch;
  }

  /**
   * Sets the <code>pitch</code> angle used by view platform transform.
   */
  protected void setViewPitch(float viewPitch) {
    this.viewPitch = viewPitch;
    if (this.universe != null) {
      updateViewPlatformTransform(this.universe.getViewingPlatform().getViewPlatformTransform(), 
          getViewYaw(), getViewPitch(), getViewScale());
    }
  }

  /**
   * Updates the given view platform transformation from yaw angle, pitch angle and scale. 
   */
  private void updateViewPlatformTransform(TransformGroup viewPlatformTransform,
                                           float viewYaw, float viewPitch,
                                           float viewScale) {
    // Default distance used to view a 2 unit wide scene
    double nominalDistanceToCenter = 1.4 / Math.tan(Math.PI / 8);
    // We don't use a TransformGroup in scene tree to be able to share the same scene 
    // in the different views displayed by OrientationPreviewComponent class 
    Transform3D pitchRotation = new Transform3D();
    pitchRotation.rotX(viewPitch);
    Transform3D yawRotation = new Transform3D();
    yawRotation.rotY(viewYaw);
    Transform3D transform = new Transform3D();
    transform.setTranslation(
        new Vector3d(Math.sin(viewYaw) * nominalDistanceToCenter * Math.cos(viewPitch), 
            nominalDistanceToCenter * Math.sin(-viewPitch), 
            Math.cos(viewYaw) * nominalDistanceToCenter * Math.cos(viewPitch)));
    Transform3D scale = new Transform3D();
    scale.setScale(viewScale);
    
    yawRotation.mul(pitchRotation);
    transform.mul(yawRotation);
    scale.mul(transform);
    viewPlatformTransform.setTransform(scale);
  }
  
  /**
   * Returns scene tree root.
   */
  private BranchGroup createSceneTree() {
    BranchGroup root = new BranchGroup();
    root.setCapability(BranchGroup.ALLOW_DETACH);
    root.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
    // Build scene tree
    root.addChild(getModelTree());
    root.addChild(getBackgroundNode());
    for (Light light : getLights()) {
      root.addChild(light);
    }
    return root;
  }
  
  /**
   * Returns the background node.  
   */
  private Node getBackgroundNode() {
    Background background = new Background(new Color3f(0.9f, 0.9f, 0.9f));
    background.setCapability(Background.ALLOW_COLOR_WRITE);
    background.setApplicationBounds(new BoundingSphere(new Point3d(0, 0, 0), 100));
    return background;
  }
  
  /**
   * Sets the background color.
   */
  public void setBackground(Color backgroundColor) {
    super.setBackground(backgroundColor);
    ((Background)this.sceneTree.getChild(1)).setColor(new Color3f(backgroundColor));
  }
  
  /**
   * Returns the lights of the scene.
   */
  private Light [] getLights() {
    Light [] lights = {
        new DirectionalLight(new Color3f(0.9f, 0.9f, 0.9f), new Vector3f(1.732f, -0.8f, -1)), 
        new DirectionalLight(new Color3f(0.9f, 0.9f, 0.9f), new Vector3f(-1.732f, -0.8f, -1)), 
        new DirectionalLight(new Color3f(0.9f, 0.9f, 0.9f), new Vector3f(0, -0.8f, 1)), 
        new AmbientLight(new Color3f(0.2f, 0.2f, 0.2f))}; 

    for (Light light : lights) {
      light.setInfluencingBounds(new BoundingSphere(new Point3d(0, 0, 0), 100));
    }
    return lights;
  }

  /**
   * Returns the root of model tree.
   */
  private Node getModelTree() {
    TransformGroup modelTransformGroup = new TransformGroup();      
    //  Allow transform group to have new children
    modelTransformGroup.setCapability(Group.ALLOW_CHILDREN_READ);
    modelTransformGroup.setCapability(Group.ALLOW_CHILDREN_WRITE);
    modelTransformGroup.setCapability(Group.ALLOW_CHILDREN_EXTEND);
    // Allow the change of the transformation that sets model size and position
    modelTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
    return modelTransformGroup;
  }

  /**
   * Returns the 3D model content displayed by this component.
   */
  public Content getModel() throws IOException {
    return this.model;
  }

  /**
   * Sets the 3D model content displayed by this component. 
   * The model is shown at its default orientation and in a box 1 unit wide.
   */
  public void setModel(final Content model) {
    final TransformGroup modelTransformGroup = (TransformGroup)this.sceneTree.getChild(0);
    modelTransformGroup.removeAllChildren();
    if (model != null) {
      final AtomicReference<IllegalArgumentException> exception = new AtomicReference<IllegalArgumentException>();
      // Load content model synchronously (or get it from cache)
      ModelManager.getInstance().loadModel(model, true, new ModelManager.ModelObserver() {        
          public void modelUpdated(BranchGroup modelRoot) { 
            modelRoot.setCapability(BranchGroup.ALLOW_DETACH);
            setNodeCapabilities(modelRoot);
            
            if (modelRoot.numChildren() > 0) {
              BoundingBox bounds = null;
              try {
                bounds = ModelManager.getInstance().getBounds(modelRoot);
              } catch (IllegalArgumentException ex) {
                // Model is empty
              }
              if (bounds != null) {
                Point3d lower = new Point3d();
                bounds.getLower(lower);
                Point3d upper = new Point3d();
                bounds.getUpper(upper);
                
                // Translate model to center
                Transform3D translation = new Transform3D ();
                translation.setTranslation(
                    new Vector3d(-lower.x - (upper.x - lower.x) / 2, 
                                 -lower.y - (upper.y - lower.y) / 2, 
                                 -lower.z - (upper.z - lower.z) / 2));
                // Scale model to make it fit in a 1.8 unit wide box
                Transform3D modelTransform = new Transform3D();
                modelTransform.setScale (1.8 / Math.max (Math.max (upper.x -lower.x, upper.y - lower.y), 
                                                         upper.z - lower.z));
                modelTransform.mul(translation);
                
                modelTransformGroup.setTransform(modelTransform);
                modelTransformGroup.addChild(modelRoot);
              }
            }
          }
          
          public void modelError(Exception ex) {
            exception.set(new IllegalArgumentException("Couldn't load model", ex));
          }
        });
      
      if (exception.get() != null) {
        throw exception.get(); 
      }
    }
    this.model = model;
  }
  
  /**
   * Returns the 3D model node displayed by this component. 
   */
  BranchGroup getModelNode() {
    TransformGroup modelTransformGroup = (TransformGroup)this.sceneTree.getChild(0);
    if (modelTransformGroup.numChildren() > 0) {
      return (BranchGroup)modelTransformGroup.getChild(0);
    } else {
      return null;
    }
  }

  /**
   * Sets the capability to read bounds, to write polygon and material attributes  
   * for all children of <code>node</code>.
   */
  private void setNodeCapabilities(Node node) {
    if (node instanceof Group) {
      node.setCapability(Group.ALLOW_CHILDREN_READ);
      if (node instanceof TransformGroup) {
        node.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
      }
      Enumeration<?> enumeration = ((Group)node).getAllChildren(); 
      while (enumeration.hasMoreElements()) {
        setNodeCapabilities((Node)enumeration.nextElement());
      }
    } else if (node instanceof Link) {
      node.setCapability(Link.ALLOW_SHARED_GROUP_READ);
      setNodeCapabilities(((Link)node).getSharedGroup());
    } else if (node instanceof Shape3D) {
      Shape3D shape = (Shape3D)node;
      node.setCapability(Node.ALLOW_BOUNDS_READ);
      Appearance appearance = ((Shape3D)node).getAppearance();
      if (appearance == null) {
        appearance = new Appearance();
        shape.setAppearance(appearance);
      }
      appearance.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_READ);
      appearance.setCapability(Appearance.ALLOW_MATERIAL_READ);
      appearance.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
      node.setCapability(Shape3D.ALLOW_APPEARANCE_READ);

      PolygonAttributes polygonAttributes = appearance.getPolygonAttributes();
      if (polygonAttributes == null) {
        polygonAttributes = new PolygonAttributes();
        appearance.setPolygonAttributes(polygonAttributes);
      }
      polygonAttributes.setCapability(PolygonAttributes.ALLOW_CULL_FACE_WRITE);
      polygonAttributes.setCapability(PolygonAttributes.ALLOW_NORMAL_FLIP_WRITE);
      
      Enumeration<?> enumeration = shape.getAllGeometries();
      while (enumeration.hasMoreElements()) {
        Geometry geometry = (Geometry) enumeration.nextElement();
        if (!geometry.isLive()
            && geometry instanceof GeometryArray) {
          geometry.setCapability(GeometryArray.ALLOW_FORMAT_READ);
          geometry.setCapability(GeometryArray.ALLOW_COUNT_READ);
          geometry.setCapability(GeometryArray.ALLOW_COORDINATE_READ);
          geometry.setCapability(GeometryArray.ALLOW_NORMAL_READ);
          geometry.setCapability(GeometryArray.ALLOW_TEXCOORD_READ);
          geometry.setCapability(GeometryArray.ALLOW_REF_DATA_READ);
        }
      }
    }
  }
  
  /**
   * Sets the back face visibility of all <code>Shape3D</code> children nodes of displayed 3D model.
   */
  protected void setBackFaceShown(boolean backFaceShown) {
    setBackFaceShown(this.sceneTree.getChild(0), backFaceShown);
  }
  
  /**
   * Sets the back face visibility of all <code>Shape3D</code> children nodes of <code>node</code>.
   */
  private void setBackFaceShown(Node node, boolean backFaceShown) {
    if (node instanceof Group) {
      // Set visibility of all children
      Enumeration<?> enumeration = ((Group)node).getAllChildren(); 
      while (enumeration.hasMoreElements()) {
        setBackFaceShown((Node)enumeration.nextElement(), backFaceShown);
      }
    } else if (node instanceof Link) {
      setBackFaceShown(((Link)node).getSharedGroup(), backFaceShown);
    } else if (node instanceof Shape3D) {
      Appearance appearance = ((Shape3D)node).getAppearance();
      PolygonAttributes polygonAttributes = appearance.getPolygonAttributes();
      // Change cull face
      if (polygonAttributes.getCullFace() != PolygonAttributes.CULL_NONE) {
        polygonAttributes.setCullFace(backFaceShown 
            ? PolygonAttributes.CULL_FRONT
            : PolygonAttributes.CULL_BACK);
      }
      // Change back face normal flip
      polygonAttributes.setBackFaceNormalFlip(backFaceShown);
    }
  }

  /**
   * Updates the rotation of the 3D model displayed by this component. 
   * The model is shown at its default size.
   */
  protected void setModelRotation(float [][] modelRotation) {
    BranchGroup modelNode = getModelNode();
    if (modelNode != null && modelNode.numChildren() > 0) {
      BoundingBox bounds = ModelManager.getInstance().getBounds(modelNode);
      Point3d lower = new Point3d();
      bounds.getLower(lower);
      Point3d upper = new Point3d();
      bounds.getUpper(upper);
    
      // Translate model to center
      Transform3D translation = new Transform3D ();
      translation.setTranslation(
          new Vector3d(-lower.x - (upper.x - lower.x) / 2, 
                       -lower.y - (upper.y - lower.y) / 2, 
                       -lower.z - (upper.z - lower.z) / 2));
      // Apply model rotation
      Transform3D rotationTransform = new Transform3D();
      if (modelRotation != null) {
        Matrix3f modelRotationMatrix = new Matrix3f(modelRotation [0][0], modelRotation [0][1], modelRotation [0][2],
            modelRotation [1][0], modelRotation [1][1], modelRotation [1][2],
            modelRotation [2][0], modelRotation [2][1], modelRotation [2][2]);
        rotationTransform.setRotation(modelRotationMatrix);
      }
      rotationTransform.mul(translation);
      // Scale model to make it fit in a 1.8 unit wide box      
      Transform3D modelTransform = new Transform3D();
      modelTransform.setScale(1.8 / Math.max(Math.max((upper.x -lower.x), (upper.z - lower.z)), (upper.y - lower.y)));
      modelTransform.mul(rotationTransform);
      
      TransformGroup modelTransformGroup = (TransformGroup)this.sceneTree.getChild(0);
      modelTransformGroup.setTransform(modelTransform);
    }
  }
  
  /**
   * Updates the rotation and the size of the 3D model displayed by this component. 
   */
  protected void setModelRotationAndSize(float [][] modelRotation,
                                         float width, float depth, float height) {
    BranchGroup modelNode = getModelNode();
    if (modelNode != null && modelNode.numChildren() > 0) {
      Transform3D normalization = ModelManager.getInstance().getNormalizedTransform(modelNode, modelRotation, 1f);
      // Scale model to its size
      Transform3D scaleTransform = new Transform3D();
      if (width != 0 && depth != 0 && height != 0) {
        scaleTransform.setScale(new Vector3d(width, height, depth));
      }
      scaleTransform.mul(normalization);
      // Scale model to make it fit in a 1.8 unit wide box      
      Transform3D modelTransform = new Transform3D();
      if (width != 0 && depth != 0 && height != 0) {
        modelTransform.setScale(1.8 / Math.max(Math.max(width, height), depth));
      } else {
        Vector3f size = ModelManager.getInstance().getSize(modelNode);
        modelTransform.setScale(1.8 / Math.max(Math.max(size.x, size.z), size.y));
      }
      modelTransform.mul(scaleTransform);
      
      TransformGroup modelTransformGroup = (TransformGroup)this.sceneTree.getChild(0);
      modelTransformGroup.setTransform(modelTransform);
    }
  }

  /**
   * Sets the color applied to piece model.
   */
  protected void setModelColor(Integer color) {
    if (color != null) {
      Color3f materialColor = new Color3f(((color >>> 16) & 0xFF) / 255f,
                                           ((color >>> 8) & 0xFF) / 255f,
                                                   (color & 0xFF) / 255f);
      setMaterial(this.sceneTree.getChild(0), 
          new Material(materialColor, new Color3f(), materialColor, materialColor, 64));
    } else {
      // Set default material of model
      setMaterial(this.sceneTree.getChild(0), null);
    }
  }

  /**
   * Sets the material attribute of all <code>Shape3D</code> children nodes of <code>node</code> 
   * with a given <code>material</code>. 
   */
  private void setMaterial(Node node, Material material) {
    if (node instanceof Group) {
      // Set material of all children
      Enumeration<?> enumeration = ((Group)node).getAllChildren(); 
      while (enumeration.hasMoreElements()) {
        setMaterial((Node)enumeration.nextElement(), material);
      }
    } else if (node instanceof Link) {
      setMaterial(((Link)node).getSharedGroup(), material);
    } else if (node instanceof Shape3D) {
      Shape3D shape = (Shape3D)node;
      String shapeName = (String)shape.getUserData();
      // Change material of all shape that are not window panes
      if (shapeName == null
          || !shapeName.startsWith(ModelManager.WINDOW_PANE_SHAPE_PREFIX)) {
        Appearance appearance = shape.getAppearance();
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
   * Returns the icon image matching the displayed view.  
   */
  private BufferedImage getIconImage(int maxWaitingDelay) {
    Color backgroundColor = getBackground();
    
    BufferedImage imageWithWhiteBackgound = null;
    BufferedImage imageWithBlackBackgound = null;
    
    this.iconImageLock = new Object();
    try {
      // Instead of using off screen images that may cause some problems
      // use Robot to capture canvas 3D image 
      Point component3DOrigin = new Point();
      SwingUtilities.convertPointToScreen(component3DOrigin, this.component3D);
      
      Robot robot = new Robot();
      // Render scene with a white background
      if (this.iconImageLock != null) {
        synchronized (this.iconImageLock) {
          setBackground(Color.WHITE);
          try {
            this.iconImageLock.wait(maxWaitingDelay / 2);
          } catch (InterruptedException ex) {
            ex.printStackTrace();
          }
        }
      }
      imageWithWhiteBackgound = robot.createScreenCapture(
          new Rectangle(component3DOrigin, this.component3D.getSize()));
      
      // Render scene with a black background
      if (this.iconImageLock != null) {
        synchronized (this.iconImageLock) {
          setBackground(Color.BLACK);
          try {
            this.iconImageLock.wait(maxWaitingDelay / 2);
          } catch (InterruptedException ex) {
            ex.printStackTrace();
          }
        }
      }
      imageWithBlackBackgound = robot.createScreenCapture(
          new Rectangle(component3DOrigin, this.component3D.getSize()));
    } catch (AWTException ex) {
      throw new RuntimeException(ex);
    } finally {
      this.iconImageLock = null;
      setBackground(backgroundColor);
    }

    int [] imageWithWhiteBackgoundPixels = imageWithWhiteBackgound.getRGB(
        0, 0, imageWithWhiteBackgound.getWidth(), imageWithWhiteBackgound.getHeight(), null,
        0, imageWithWhiteBackgound.getWidth());
    int [] imageWithBlackBackgoundPixels = imageWithBlackBackgound.getRGB(
        0, 0, imageWithBlackBackgound.getWidth(), imageWithBlackBackgound.getHeight(), null,
        0, imageWithBlackBackgound.getWidth());
    
    // Create an image with transparent pixels where model isn't drawn
    for (int i = 0; i < imageWithBlackBackgoundPixels.length; i++) {
      if (imageWithBlackBackgoundPixels [i] != imageWithWhiteBackgoundPixels [i]
          && imageWithBlackBackgoundPixels [i] == 0xFF000000
          && imageWithWhiteBackgoundPixels [i] == 0xFFFFFFFF) {
        imageWithWhiteBackgoundPixels [i] = 0;
      }           
    }
    
    BufferedImage iconImage = new BufferedImage(imageWithWhiteBackgound.getWidth(), imageWithWhiteBackgound.getHeight(), 
        BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2D = (Graphics2D)iconImage.getGraphics();
    g2D.drawImage(Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(
        imageWithWhiteBackgound.getWidth(), imageWithWhiteBackgound.getHeight(), 
        imageWithWhiteBackgoundPixels, 0, imageWithWhiteBackgound.getWidth())), null, null);
    g2D.dispose();

    return iconImage;
  }

  /**
   * Returns a temporary content of the icon matching the displayed view.
   */
  public Content getIcon(int maxWaitingDelay) throws IOException {
    File tempIconFile = OperatingSystem.createTemporaryFile("icon", ".png");
    ImageIO.write(getIconImage(maxWaitingDelay), "png", tempIconFile);
    return new TemporaryURLContent(tempIconFile.toURI().toURL());
  }
  
  /**
   * Notifies the canvas 3D displayed by this component was swapped.
   */
  private void canvas3DSwapped() {
    if (this.iconImageLock != null) {
      synchronized (this.iconImageLock) {
        this.iconImageLock.notify();
      }
    }
  }
}