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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.imageio.ImageIO;
import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.GraphicsConfigTemplate3D;
import javax.media.j3d.Group;
import javax.media.j3d.Light;
import javax.media.j3d.Link;
import javax.media.j3d.Node;
import javax.media.j3d.PhysicalBody;
import javax.media.j3d.PhysicalEnvironment;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Texture;
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
import com.eteks.sweethome3d.j3d.HomePieceOfFurniture3D;
import com.eteks.sweethome3d.j3d.ModelManager;
import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.HomeMaterial;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
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
  private SimpleUniverse          universe;
  private JPanel                  component3DPanel;
  private Component               component3D;
  private BranchGroup             sceneTree;
  private float                   viewYaw   = (float) Math.PI / 8;
  private float                   viewPitch = -(float) Math.PI / 16;
  private float                   viewScale = 1;
  private Object                  iconImageLock;
  private HomePieceOfFurniture    previewedPiece;
  private Map<Texture, Texture>   pieceTextures = new HashMap<Texture, Texture>();

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

  @Override
  public void addMouseMotionListener(final MouseMotionListener l) {
    super.addMouseMotionListener(l);
    if (this.component3D != null) {
      this.component3D.addMouseMotionListener(new MouseMotionListener() {
          public void mouseMoved(MouseEvent ev) {
            l.mouseMoved(SwingUtilities.convertMouseEvent(component3D, ev, ModelPreviewComponent.this));
          }
          
          public void mouseDragged(MouseEvent ev) {
            l.mouseDragged(SwingUtilities.convertMouseEvent(component3D, ev, ModelPreviewComponent.this));
          }
        });
    }
  }
  
  @Override
  public void addMouseListener(final MouseListener l) {
    super.addMouseListener(l);
    this.component3D.addMouseListener(new MouseListener() {
        public void mouseReleased(MouseEvent ev) {
          l.mouseReleased(SwingUtilities.convertMouseEvent(component3D, ev, ModelPreviewComponent.this));
        }
        
        public void mousePressed(MouseEvent ev) {
          l.mousePressed(SwingUtilities.convertMouseEvent(component3D, ev, ModelPreviewComponent.this));
        }
        
        public void mouseExited(MouseEvent ev) {
          l.mouseExited(SwingUtilities.convertMouseEvent(component3D, ev, ModelPreviewComponent.this));
        }
        
        public void mouseEntered(MouseEvent ev) {
          l.mouseEntered(SwingUtilities.convertMouseEvent(component3D, ev, ModelPreviewComponent.this));
        }
        
        public void mouseClicked(MouseEvent ev) {
          l.mouseClicked(SwingUtilities.convertMouseEvent(component3D, ev, ModelPreviewComponent.this));
        }
      });
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
    for (final MouseMotionListener l : getListeners(MouseMotionListener.class)) {
      component3D.addMouseMotionListener(new MouseMotionListener() {
          public void mouseMoved(MouseEvent ev) {
            l.mouseMoved(SwingUtilities.convertMouseEvent(component3D, ev, ModelPreviewComponent.this));
          }
          
          public void mouseDragged(MouseEvent ev) {
            l.mouseDragged(SwingUtilities.convertMouseEvent(component3D, ev, ModelPreviewComponent.this));
          }
        });
    }
    for (final MouseListener l : getListeners(MouseListener.class)) {
      component3D.addMouseListener(new MouseListener() {
          public void mouseReleased(MouseEvent ev) {
            l.mouseReleased(SwingUtilities.convertMouseEvent(component3D, ev, ModelPreviewComponent.this));
          }
          
          public void mousePressed(MouseEvent ev) {
            l.mousePressed(SwingUtilities.convertMouseEvent(component3D, ev, ModelPreviewComponent.this));
          }
          
          public void mouseExited(MouseEvent ev) {
            l.mouseExited(SwingUtilities.convertMouseEvent(component3D, ev, ModelPreviewComponent.this));
          }
          
          public void mouseEntered(MouseEvent ev) {
            l.mouseEntered(SwingUtilities.convertMouseEvent(component3D, ev, ModelPreviewComponent.this));
          }
          
          public void mouseClicked(MouseEvent ev) {
            l.mouseClicked(SwingUtilities.convertMouseEvent(component3D, ev, ModelPreviewComponent.this));
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
    root.addChild(createModelTree());
    root.addChild(createBackgroundNode());
    for (Light light : createLights()) {
      root.addChild(light);
    }
    return root;
  }
  
  /**
   * Returns the background node.  
   */
  private Node createBackgroundNode() {
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
  private Light [] createLights() {
    Light [] lights = {
        new DirectionalLight(new Color3f(0.9f, 0.9f, 0.9f), new Vector3f(1.732f, -0.8f, -1)), 
        new DirectionalLight(new Color3f(0.9f, 0.9f, 0.9f), new Vector3f(-1.732f, -0.8f, -1)), 
        new DirectionalLight(new Color3f(0.9f, 0.9f, 0.9f), new Vector3f(0, -0.8f, 1)), 
        new DirectionalLight(new Color3f(0.66f, 0.66f, 0.66f), new Vector3f(0, 1f, 0)), 
        new AmbientLight(new Color3f(0.2f, 0.2f, 0.2f))}; 

    for (Light light : lights) {
      light.setInfluencingBounds(new BoundingSphere(new Point3d(0, 0, 0), 100));
    }
    return lights;
  }

  /**
   * Returns the root of model tree.
   */
  private Node createModelTree() {
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
  public Content getModel() {
    return this.previewedPiece.getModel();
  }

  /**
   * Sets the 3D model content displayed by this component. 
   * The model is shown at its default orientation and in a box 1 unit wide.
   */
  public void setModel(final Content model) {
    final TransformGroup modelTransformGroup = (TransformGroup)this.sceneTree.getChild(0);
    modelTransformGroup.removeAllChildren();
    this.previewedPiece = null;
    this.pieceTextures.clear();
    if (model != null) {
      final AtomicReference<IllegalArgumentException> exception = new AtomicReference<IllegalArgumentException>();
      // Load content model synchronously (or get it from cache)
      ModelManager.getInstance().loadModel(model, true, new ModelManager.ModelObserver() {        
          public void modelUpdated(BranchGroup modelRoot) {
            if (modelRoot.numChildren() > 0) {
              try {
                Vector3f size = ModelManager.getInstance().getSize(modelRoot);
                previewedPiece = new HomePieceOfFurniture(
                    new CatalogPieceOfFurniture(null, null, model, 
                        size.x, size.z, size.y, 0, false, null, null, false, 0, false));
                previewedPiece.setX(0);
                previewedPiece.setY(0);
                previewedPiece.setElevation(-previewedPiece.getHeight() / 2);
                
                Transform3D modelTransform = new Transform3D();
                modelTransform.setScale(1.8 / Math.max(Math.max(size.x, size.z), size.y));
                modelTransformGroup.setTransform(modelTransform);

                modelTransformGroup.addChild(new HomePieceOfFurniture3D(previewedPiece, null, true, true));
              } catch (IllegalArgumentException ex) {
                // Model is empty
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
  }

  /**
   * Sets the back face visibility of the children nodes of the displayed 3D model.
   */
  protected void setBackFaceShown(boolean backFaceShown) {
    if (this.previewedPiece != null) {
      // Create a new piece from the existing one with an updated backFaceShown flag 
      this.previewedPiece = new HomePieceOfFurniture(
          new CatalogPieceOfFurniture(null, null, this.previewedPiece.getModel(), 
              this.previewedPiece.getWidth(), 
              this.previewedPiece.getDepth(),
              this.previewedPiece.getHeight(),
              0, false, this.previewedPiece.getColor(), null, backFaceShown, 0, false));
      this.previewedPiece.setX(0);
      this.previewedPiece.setY(0);
      this.previewedPiece.setElevation(-previewedPiece.getHeight() / 2);
    
      TransformGroup modelTransformGroup = (TransformGroup)this.sceneTree.getChild(0);
      modelTransformGroup.addChild(new HomePieceOfFurniture3D(this.previewedPiece, null, true, true));
      if (modelTransformGroup.numChildren() > 1) {
        modelTransformGroup.removeChild(0);
      }
    }
  }

  /**
   * Returns the 3D model node displayed by this component. 
   */
  private HomePieceOfFurniture3D getModelNode() {
    TransformGroup modelTransformGroup = (TransformGroup)this.sceneTree.getChild(0);
    if (modelTransformGroup.numChildren() > 0) {
      return (HomePieceOfFurniture3D)modelTransformGroup.getChild(0);
    } else {
      return null;
    }
  }
  
  /**
   * Updates the rotation of the 3D model displayed by this component. 
   * The model is shown at its default size.
   */
  protected void setModelRotation(float [][] modelRotation) {
    BranchGroup modelNode = getModelNode();
    if (modelNode != null && modelNode.numChildren() > 0) {
      // Apply model rotation
      Transform3D rotationTransform = new Transform3D();
      if (modelRotation != null) {
        Matrix3f modelRotationMatrix = new Matrix3f(modelRotation [0][0], modelRotation [0][1], modelRotation [0][2],
            modelRotation [1][0], modelRotation [1][1], modelRotation [1][2],
            modelRotation [2][0], modelRotation [2][1], modelRotation [2][2]);
        rotationTransform.setRotation(modelRotationMatrix);
      }
      // Scale model to make it fit in a 1.8 unit wide box      
      Transform3D modelTransform = new Transform3D();
      Vector3f size = ModelManager.getInstance().getSize(modelNode);
      modelTransform.setScale(1.8 / Math.max(Math.max(size.x, size.z), size.y));
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
   * Sets the color applied to 3D model.
   */
  protected void setModelColor(Integer color) {
    if (this.previewedPiece != null
        && this.previewedPiece.getColor() != color) {
      this.previewedPiece.setColor(color);
      getModelNode().update();
    }
  }

  /**
   * Sets the materials applied to 3D model.
   */
  public void setModelMaterials(HomeMaterial [] materials) {
    if (this.previewedPiece != null) {
      this.previewedPiece.setModelMaterials(materials);
      getModelNode().update();
      // Replace textures by clones because Java 3D doesn't accept all the time to share textures 
      cloneTexture(getModelNode(), this.pieceTextures);
    }
  }

  /**
   * Replace the textures set on <code>node</code> shapes by clones. 
   */
  private void cloneTexture(Node node, Map<Texture, Texture> replacedTextures) {
    if (node instanceof Group) {
      // Enumerate children
      Enumeration<?> enumeration = ((Group)node).getAllChildren(); 
      while (enumeration.hasMoreElements()) {
        cloneTexture((Node)enumeration.nextElement(), replacedTextures);
      }
    } else if (node instanceof Link) {
      cloneTexture(((Link)node).getSharedGroup(), replacedTextures);
    } else if (node instanceof Shape3D) {
      Appearance appearance = ((Shape3D)node).getAppearance();
      if (appearance != null) {
        Texture texture = appearance.getTexture();
        if (texture != null) {
          Texture replacedTexture = replacedTextures.get(texture);
          if (replacedTexture == null) {
            replacedTexture = (Texture)texture.cloneNodeComponent(false);
            replacedTextures.put(texture, replacedTexture);
          }
          appearance.setTexture(replacedTexture);
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