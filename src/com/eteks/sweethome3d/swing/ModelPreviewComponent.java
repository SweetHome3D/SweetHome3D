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
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

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
import javax.media.j3d.GraphicsConfigTemplate3D;
import javax.media.j3d.Group;
import javax.media.j3d.Light;
import javax.media.j3d.Link;
import javax.media.j3d.Node;
import javax.media.j3d.RenderingAttributes;
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
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import com.eteks.sweethome3d.j3d.Component3DManager;
import com.eteks.sweethome3d.j3d.HomePieceOfFurniture3D;
import com.eteks.sweethome3d.j3d.ModelManager;
import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.HomeMaterial;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.Transformation;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.tools.TemporaryURLContent;
import com.sun.j3d.exp.swing.JCanvas3D;
import com.sun.j3d.utils.geometry.Cone;
import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.j3d.utils.picking.PickCanvas;
import com.sun.j3d.utils.picking.PickResult;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.Viewer;
import com.sun.j3d.utils.universe.ViewingPlatform;

/**
 * Super class of 3D preview component for model.
 */
public class ModelPreviewComponent extends JComponent {
  private static final int MODEL_PREFERRED_SIZE = Math.round(200 * SwingTools.getResolutionScale());

  private SimpleUniverse          universe;
  private JPanel                  component3DPanel;
  private Component               component3D;
  private BranchGroup             sceneTree;
  private float                   viewYaw   = (float) Math.PI / 8;
  private float                   viewPitch = -(float) Math.PI / 16;
  private float                   viewScale = 1;
  private boolean                 parallelProjection;
  private Object                  iconImageLock;
  private HomePieceOfFurniture    previewedPiece;
  private boolean                 internalRotationAndSize;
  private Map<Texture, Texture>   pieceTextures = new HashMap<Texture, Texture>();

  /**
   * Returns an 3D model preview component that lets the user change its yaw.
   */
  public ModelPreviewComponent() {
    this(false);
  }

  /**
   * Returns an 3D model preview component that lets the user change its pitch and scale
   * if <code>pitchAndScaleChangeSupported</code> is <code>true</code>.
   */
  public ModelPreviewComponent(boolean pitchAndScaleChangeSupported) {
    this(true, pitchAndScaleChangeSupported, pitchAndScaleChangeSupported);
  }

  /**
   * Returns an 3D model preview component that lets the user change its yaw, pitch and scale
   * according to parameters.
   */
  public ModelPreviewComponent(boolean yawChangeSupported,
                               boolean pitchChangeSupported,
                               boolean scaleChangeSupported) {
    this(yawChangeSupported, pitchChangeSupported, scaleChangeSupported, false);
  }

  /**
   * Returns an 3D model preview component that lets the user change its yaw, pitch and scale
   * according to parameters.
   */
  public ModelPreviewComponent(boolean yawChangeSupported,
                               boolean pitchChangeSupported,
                               boolean scaleChangeSupported,
                               boolean transformationsChangeSupported) {
    setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

    this.sceneTree = createSceneTree(transformationsChangeSupported);

    this.component3DPanel = new JPanel();
    setLayout(new BorderLayout());
    add(this.component3DPanel);

    GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
    if (graphicsEnvironment.getScreenDevices().length == 1) {
      // If only one screen device is available, create 3D component immediately,
      // otherwise create it once the screen device of the parent is known
      createComponent3D(graphicsEnvironment.getDefaultScreenDevice().getDefaultConfiguration(),
          yawChangeSupported, pitchChangeSupported, scaleChangeSupported, transformationsChangeSupported);
    }

    // Add an ancestor listener to create 3D component and its universe once this component is made visible
    // and clean up universe once its parent frame is disposed
    addAncestorListener(yawChangeSupported, pitchChangeSupported, scaleChangeSupported, transformationsChangeSupported);
  }

  /**
   * Returns component preferred size.
   */
  @Override
  public Dimension getPreferredSize() {
    if (isPreferredSizeSet()) {
      return super.getPreferredSize();
    } else {
      return new Dimension(MODEL_PREFERRED_SIZE, MODEL_PREFERRED_SIZE);
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
    if (this.component3D != null) {
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
  private void addAncestorListener(final boolean yawChangeSupported,
                                   final boolean pitchChangeSupported,
                                   final boolean scaleChangeSupported,
                                   final boolean transformationsChangeSupported) {
    addAncestorListener(new AncestorListener() {
        public void ancestorAdded(AncestorEvent ev) {
          if (component3D == null) {
            createComponent3D(ev.getAncestor().getGraphicsConfiguration(),
                yawChangeSupported, pitchChangeSupported, scaleChangeSupported, transformationsChangeSupported);
          }
          if (universe == null) {
            createUniverse();
          }
        }

        public void ancestorRemoved(AncestorEvent ev) {
          if (universe != null) {
            // Dispose universe later to avoid conflicts if canvas is currently being redrawn
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                  disposeUniverse();
                  component3DPanel.removeAll();
                  component3D = null;
                }
              });
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
                                 boolean yawChangeSupported,
                                 boolean pitchChangeSupported,
                                 boolean scaleChangeSupported,
                                 boolean transformationsChangeSupported) {
    if (Boolean.getBoolean("com.eteks.sweethome3d.j3d.useOffScreen3DView")) {
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
    addMouseListeners(this.component3D, yawChangeSupported, pitchChangeSupported, scaleChangeSupported, transformationsChangeSupported);
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
                                 final boolean yawChangeSupported,
                                 final boolean pitchChangeSupported,
                                 final boolean scaleChangeSupported,
                                 final boolean transformationsChangeSupported) {
    final float ANGLE_FACTOR = 0.02f;
    final float ZOOM_FACTOR = 0.02f;
    MouseInputAdapter mouseListener = new MouseInputAdapter() {
        private int            xLastMouseMove;
        private int            yLastMouseMove;
        private boolean        boundedPitch;
        private TransformGroup pickedTransformGroup;
        private Point2d        pivotCenterPixel;
        private Transform3D    translationFromOrigin;
        private Transform3D    translationToOrigin;
        private BoundingBox    modelBounds;

        private Point getMouseLocation(MouseEvent ev) {
          if (!OperatingSystem.isMacOSX()
              && OperatingSystem.isJavaVersionGreaterOrEqual("1.9")) {
            try {
              // Dirty hack that scales mouse coordinates with xcale and yscale private fields of Canvas3D
              Field xscaleField = Canvas3D.class.getDeclaredField("xscale");
              xscaleField.setAccessible(true);
              double xscale = (Double)(xscaleField.get(ev.getSource()));
              Field yscaleField = Canvas3D.class.getDeclaredField("yscale");
              yscaleField.setAccessible(true);
              double yscale = (Double)(yscaleField.get(ev.getSource()));
              return new Point((int)(ev.getX() * xscale), (int)(ev.getY() * yscale));
            } catch (Exception ex) {
            }
          }
          return ev.getPoint();
        }

        @Override
        public void mousePressed(MouseEvent ev) {
          Point mouseLocation = getMouseLocation(ev);
          this.xLastMouseMove = mouseLocation.x;
          this.yLastMouseMove = mouseLocation.y;
          this.pickedTransformGroup = null;
          this.pivotCenterPixel = null;
          this.boundedPitch = true;
          if (transformationsChangeSupported
              && getModelNode() != null) {
            ModelManager modelManager = ModelManager.getInstance();
            this.boundedPitch = !modelManager.containsDeformableNode(getModelNode());
            Canvas3D canvas;
            if (component3D instanceof JCanvas3D) {
              canvas = ((JCanvas3D)component3D).getOffscreenCanvas3D();
            } else {
              canvas = (Canvas3D)component3D;
            }
            PickCanvas pickCanvas = new PickCanvas(canvas, getModelNode());
            pickCanvas.setMode(PickCanvas.GEOMETRY);
            pickCanvas.setShapeLocation(mouseLocation.x, mouseLocation.y);
            PickResult result = pickCanvas.pickClosest();
            if (result != null) {
              this.pickedTransformGroup = (TransformGroup)result.getNode(PickResult.TRANSFORM_GROUP);
              if (pickedTransformGroup != null) {
                // The pivot node is the first sibling node which is not a transform group
                Group group = (Group)this.pickedTransformGroup.getParent();
                int i = group.indexOfChild(pickedTransformGroup) - 1;
                while (i >= 0 && (group.getChild(i) instanceof TransformGroup)) {
                  i--;
                }
                if (i >= 0) {
                  Node referenceNode = group.getChild(i);
                  Point3f nodeCenter = modelManager.getCenter(referenceNode);
                  Point3f nodeCenterAtScreen = new Point3f(nodeCenter);
                  Transform3D pivotTransform = getTransformBetweenNodes(referenceNode.getParent(), sceneTree);
                  pivotTransform.transform(nodeCenterAtScreen);
                  Transform3D transformToCanvas = new Transform3D();
                  canvas.getVworldToImagePlate(transformToCanvas);
                  transformToCanvas.transform(nodeCenterAtScreen);
                  this.pivotCenterPixel = new Point2d();
                  canvas.getPixelLocationFromImagePlate(new Point3d(nodeCenterAtScreen), this.pivotCenterPixel);

                  String transformationName = (String)this.pickedTransformGroup.getUserData();
                  this.translationFromOrigin = new Transform3D();
                  this.translationFromOrigin.setTranslation(new Vector3d(nodeCenter));
                  Transform3D transformBetweenNodes = getTransformBetweenNodes(referenceNode.getParent(), getModelNode());
                  transformBetweenNodes.setTranslation(new Vector3d());
                  transformBetweenNodes.invert();
                  this.translationFromOrigin.mul(transformBetweenNodes);

                  Transform3D pitchRotation = new Transform3D();
                  pitchRotation.rotX(viewPitch);
                  Transform3D yawRotation = new Transform3D();
                  yawRotation.rotY(viewYaw);

                  if (transformationName.startsWith(ModelManager.HINGE_PREFIX)
                      || transformationName.startsWith(ModelManager.RAIL_PREFIX)) {
                    Transform3D rotation = new Transform3D();
                    Vector3f nodeSize = modelManager.getSize(referenceNode);
                    getTransformBetweenNodes(getModelRoot(referenceNode), getModelNode()).transform(nodeSize);
                    nodeSize.absolute();

                    Transform3D modelRotationAtScreen = new Transform3D(yawRotation);
                    modelRotationAtScreen.mul(pitchRotation);
                    modelRotationAtScreen.invert();

                    // Set rotation around (or translation along) hinge largest dimension
                    // taking into account the direction of the axis at screen
                    if (nodeSize.y > nodeSize.x && nodeSize.y > nodeSize.z) {
                      Vector3f yAxisAtScreen = new Vector3f(0, 1, 0);
                      modelRotationAtScreen.transform(yAxisAtScreen);
                      if (transformationName.startsWith(ModelManager.RAIL_PREFIX)
                          ? yAxisAtScreen.y > 0
                          : yAxisAtScreen.z < 0) {
                        rotation.rotX(Math.PI / 2);
                      } else {
                        rotation.rotX(-Math.PI / 2);
                      }
                    } else if (nodeSize.z > nodeSize.x && nodeSize.z > nodeSize.y) {
                      Vector3f zAxisAtScreen = new Vector3f(0, 0, 1);
                      modelRotationAtScreen.transform(zAxisAtScreen);
                      if (transformationName.startsWith(ModelManager.RAIL_PREFIX)
                          ? zAxisAtScreen.x > 0
                          : zAxisAtScreen.z < 0) {
                      rotation.rotX(Math.PI);
                      }
                    } else {
                      Vector3f xAxisAtScreen = new Vector3f(1, 0, 0);
                      modelRotationAtScreen.transform(xAxisAtScreen);
                      if (transformationName.startsWith(ModelManager.RAIL_PREFIX)
                          ? xAxisAtScreen.x > 0
                          : xAxisAtScreen.z < 0) {
                        rotation.rotY(-Math.PI / 2);
                      } else {
                        rotation.rotY(Math.PI / 2);
                      }
                    }
                    this.translationFromOrigin.mul(rotation);
                  } else {
                    // Set rotation in the screen plan for mannequin or ball handling
                    this.translationFromOrigin.mul(yawRotation);
                    this.translationFromOrigin.mul(pitchRotation);
                  }

                  this.translationToOrigin = new Transform3D(this.translationFromOrigin);
                  this.translationToOrigin.invert();

                  this.modelBounds = modelManager.getBounds(getModelNode());
                }
              }
            }
          }
        }

        private Transform3D getTransformBetweenNodes(Node node, Node parent) {
          Transform3D transform = new Transform3D();
          if (node instanceof TransformGroup) {
            ((TransformGroup)node).getTransform(transform);
          }
          if (node != parent) {
            Node nodeParent = node.getParent();
            if (nodeParent instanceof Group) {
              transform.mul(getTransformBetweenNodes(nodeParent, parent), transform);
            } else {
              throw new IllegalStateException("Can't retrieve node transform");
            }
          }
          return transform;
        }

        private BranchGroup getModelRoot(Node node) {
          // Return the branch group parent which stores the model content
          if (node instanceof BranchGroup
              && node.getUserData() instanceof Content) {
            return (BranchGroup)node;
          } else if (node.getParent() != null) {
            return getModelRoot(node.getParent());
          } else {
            return null;
          }
        }

        @Override
        public void mouseDragged(MouseEvent ev) {
          Point mouseLocation = getMouseLocation(ev);
          if (getModelNode() != null) {
            if (this.pivotCenterPixel != null) {
              String transformationName = (String)this.pickedTransformGroup.getUserData();
              Transform3D additionalTransform = new Transform3D();
              if (transformationName.startsWith(ModelManager.RAIL_PREFIX)) {
                additionalTransform.setTranslation(new Vector3f(0, 0,
                    (float)Point2D.distance(mouseLocation.x, mouseLocation.y, this.xLastMouseMove, this.yLastMouseMove) * Math.signum(this.xLastMouseMove - mouseLocation.x)));
              } else {
                double angle = Math.atan2(this.pivotCenterPixel.y - mouseLocation.y, mouseLocation.x - this.pivotCenterPixel.x)
                    - Math.atan2(this.pivotCenterPixel.y - this.yLastMouseMove, this.xLastMouseMove - this.pivotCenterPixel.x);
                additionalTransform.rotZ(angle);
              }

              additionalTransform.mul(additionalTransform, this.translationToOrigin);
              additionalTransform.mul(this.translationFromOrigin, additionalTransform);

              Transform3D newTransform = new Transform3D();
              this.pickedTransformGroup.getTransform(newTransform);
              newTransform.mul(additionalTransform, newTransform);
              this.pickedTransformGroup.setTransform(newTransform);

              // Update size with model normalization and main transformation
              Point3d modelLower = new Point3d();
              this.modelBounds.getLower(modelLower);
              Point3d modelUpper = new Point3d();
              this.modelBounds.getUpper(modelUpper);
              ModelManager modelManager = ModelManager.getInstance();
              BoundingBox newBounds = modelManager.getBounds(getModelNode());
              Point3d newLower = new Point3d();
              newBounds.getLower(newLower);
              Point3d newUpper = new Point3d();
              newBounds.getUpper(newUpper);
              previewedPiece.setX(previewedPiece.getX() + (float)(newUpper.x + newLower.x) / 2 - (float)(modelUpper.x + modelLower.x) / 2);
              previewedPiece.setY(previewedPiece.getY() + (float)(newUpper.z + newLower.z) / 2 - (float)(modelUpper.z + modelLower.z) / 2);
              previewedPiece.setElevation(previewedPiece.getElevation() + (float)(newLower.y - modelLower.y));
              previewedPiece.setWidth((float)(newUpper.x - newLower.x));
              previewedPiece.setDepth((float)(newUpper.z - newLower.z));
              previewedPiece.setHeight((float)(newUpper.y - newLower.y));
              this.modelBounds = newBounds;

              // Update matching piece of furniture transformations array
              Transformation[] transformations = previewedPiece.getModelTransformations();
              ArrayList<Transformation> transformationsList = new ArrayList<Transformation>();
              if (transformations != null) {
                transformationsList.addAll(Arrays.asList(transformations));
              }
              transformationName = transformationName.substring(0, transformationName.length() - ModelManager.DEFORMABLE_TRANSFORM_GROUP_SUFFIX.length());
              for (Iterator<Transformation> it = transformationsList.iterator(); it.hasNext();) {
                Transformation transformation = it.next();
                if (transformationName.equals(transformation.getName())) {
                  it.remove();
                  break;
                }
              }
              float [] matrix = new float [16];
              newTransform.get(matrix);
              transformationsList.add(new Transformation(transformationName, new float [][] {
                  {matrix [0], matrix [1], matrix [2], matrix [3]},
                  {matrix [4], matrix [5], matrix [6], matrix [7]},
                  {matrix [8], matrix [9], matrix [10], matrix [11]}}));
              previewedPiece.setModelTransformations(transformationsList.toArray(new Transformation [transformationsList.size()]));
            } else {
              if (yawChangeSupported) {
                // Mouse move along X axis changes yaw
                setViewYaw(getViewYaw() - ANGLE_FACTOR * (mouseLocation.x - this.xLastMouseMove));
              }

              if (scaleChangeSupported && ev.isAltDown()) {
                // Mouse move along Y axis with Alt down changes scale
                setViewScale(Math.max(0.5f, Math.min(1.3f, getViewScale() * (float)Math.exp((mouseLocation.y - this.yLastMouseMove) * ZOOM_FACTOR))));
              } else if (pitchChangeSupported && !ev.isAltDown()) {
                // Mouse move along Y axis changes pitch
                float viewPitch = getViewPitch() - ANGLE_FACTOR * (mouseLocation.y - this.yLastMouseMove);
                if (this.boundedPitch) {
                  setViewPitch(Math.max(-(float)Math.PI / 4, Math.min(0, viewPitch)));
                } else {
                  // Allow any rotation around the model
                  setViewPitch(viewPitch);
                }
              }
            }
          }
          this.xLastMouseMove = mouseLocation.x;
          this.yLastMouseMove = mouseLocation.y;
        }
      };

    component3D.addMouseListener(mouseListener);
    component3D.addMouseMotionListener(mouseListener);

    if (scaleChangeSupported) {
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
    // Link scene to universe
    this.universe.addBranchGraph(this.sceneTree);
    this.universe.getViewer().getView().setProjectionPolicy(this.parallelProjection
        ? View.PARALLEL_PROJECTION
        : View.PERSPECTIVE_PROJECTION);
    // Set viewer location
    updateViewPlatformTransform(this.universe.getViewingPlatform().getViewPlatformTransform(),
        getViewYaw(), getViewPitch(), getViewScale());

    revalidate();
    repaint();
    if (OperatingSystem.isMacOSX()) {
      final Component root = SwingUtilities.getRoot(this);
      EventQueue.invokeLater(new Runnable() {
          public void run() {
            // Request focus again even if dialog isn't supposed to have lost focus!
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
   * Sets whether the component 3D should use parallel or perspective projection.
   */
  protected void setParallelProjection(boolean parallelProjection) {
    this.parallelProjection = parallelProjection;
    if (this.universe != null) {
      this.universe.getViewer().getView().setProjectionPolicy(parallelProjection
          ? View.PARALLEL_PROJECTION
          : View.PERSPECTIVE_PROJECTION);
    }
  }

  /**
   * Returns <code>true</code> if the component 3D uses parallel projection.
   */
  protected boolean isParallelProjection() {
    return this.parallelProjection;
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
    Transform3D translation = new Transform3D();
    translation.setTranslation(new Vector3d(0, 0, nominalDistanceToCenter));
    Transform3D pitchRotation = new Transform3D();
    pitchRotation.rotX(viewPitch);
    Transform3D yawRotation = new Transform3D();
    yawRotation.rotY(viewYaw);
    Transform3D scale = new Transform3D();
    scale.setScale(viewScale);

    pitchRotation.mul(translation);
    yawRotation.mul(pitchRotation);
    scale.mul(yawRotation);
    viewPlatformTransform.setTransform(scale);

    // Update axes transformation to show current orientation and display it in bottom left corner
    Transform3D axesTransform = new Transform3D();
    axesTransform.setScale(viewScale);
    pitchRotation.rotX(-viewPitch);
    yawRotation.rotY(-viewYaw);
    axesTransform.mul(yawRotation, axesTransform);
    axesTransform.mul(pitchRotation, axesTransform);
    translation = new Transform3D();
    translation.setTranslation(new Vector3f(-.82f * viewScale, -.82f * viewScale, .82f * viewScale));
    pitchRotation.rotX(viewPitch);
    yawRotation.rotY(viewYaw);
    axesTransform.mul(translation, axesTransform);
    axesTransform.mul(pitchRotation, axesTransform);
    axesTransform.mul(yawRotation, axesTransform);
    ((TransformGroup)this.sceneTree.getChild(2)).setTransform(axesTransform);
  }

  /**
   * Returns scene tree root.
   */
  private BranchGroup createSceneTree(boolean visibleAxes) {
    BranchGroup root = new BranchGroup();
    root.setCapability(BranchGroup.ALLOW_DETACH);
    root.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
    // Build scene tree
    root.addChild(createModelTree());
    root.addChild(createBackgroundNode());
    root.addChild(createAxes(visibleAxes));
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
   * Returns a RGB axes system.
   */
  private Node createAxes(boolean visible) {
    RenderingAttributes renderingAttributes = new RenderingAttributes();
    renderingAttributes.setVisible(visible);
    Appearance red = new Appearance();
    red.setColoringAttributes(new ColoringAttributes(new Color3f(1, 0, 0), ColoringAttributes.SHADE_FLAT));
    red.setRenderingAttributes(renderingAttributes);
    Appearance green = new Appearance();
    green.setColoringAttributes(new ColoringAttributes(new Color3f(0, 1,0), ColoringAttributes.SHADE_FLAT));
    green.setRenderingAttributes(renderingAttributes);
    Appearance blue = new Appearance();
    blue.setColoringAttributes(new ColoringAttributes(new Color3f(0, 0, 1), ColoringAttributes.SHADE_FLAT));
    blue.setRenderingAttributes(renderingAttributes);

    Group axesGroup = new Group();
    Transform3D axisRotation = new Transform3D();
    axisRotation.rotZ(-Math.PI / 2);
    axesGroup.addChild(createAxis(axisRotation, red));
    axesGroup.addChild(createAxis(new Transform3D(), blue));
    axisRotation.rotX(Math.PI / 2);
    axesGroup.addChild(createAxis(axisRotation, green));
    TransformGroup axes = new TransformGroup();
    axes.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
    axes.addChild(axesGroup);
    return axes;
  }

  private Node createAxis(Transform3D axisRotation, Appearance appearance) {
    Cylinder cylinder = new Cylinder(0.00275f, 0.2f, appearance);
    Transform3D cylinderTranslation = new Transform3D();
    cylinderTranslation.setTranslation(new Vector3f(0, 0.1f, 0));
    TransformGroup cylinderGroup = new TransformGroup(cylinderTranslation);
    cylinderGroup.addChild(cylinder);

    Cone cone = new Cone(0.01f, 0.04f, appearance);
    Transform3D coneTranslation = new Transform3D();
    coneTranslation.setTranslation(new Vector3f(0, 0.2f, 0));
    TransformGroup coneGroup = new TransformGroup(coneTranslation);
    coneGroup.addChild(cone);

    TransformGroup axisGroup = new TransformGroup(axisRotation);
    axisGroup.addChild(coneGroup);
    axisGroup.addChild(cylinderGroup);
    return axisGroup;
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
    return this.previewedPiece != null
        ? this.previewedPiece.getModel()
        : null;
  }

  /**
   * Sets the 3D model content displayed by this component.
   * The model is shown at its default orientation and in a box 1 unit wide.
   */
  public void setModel(Content model) {
    setModel(model, false, null, -1, -1, -1);
  }

  /**
   * Sets the 3D model content displayed by this component.
   */
  void setModel(final Content model, final boolean backFaceShown, final float [][] modelRotation,
                final float width, final float depth, final float height) {
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
                Vector3f size = width < 0
                    ? ModelManager.getInstance().getSize(modelRoot)
                    : new Vector3f(width, height, depth);
                internalRotationAndSize = modelRotation != null;
                previewedPiece = new HomePieceOfFurniture(
                    new CatalogPieceOfFurniture(null, null, model,
                        size.x, size.z, size.y, 0, false, null, modelRotation, backFaceShown, 0, false));
                previewedPiece.setX(0);
                previewedPiece.setY(0);
                previewedPiece.setElevation(-previewedPiece.getHeight() / 2);

                Transform3D modelTransform = new Transform3D();
                modelTransform.setScale(1.8 / Math.max(Math.max(size.x, size.z), size.y));
                modelTransformGroup.setTransform(modelTransform);

                HomePieceOfFurniture3D piece3D = new HomePieceOfFurniture3D(previewedPiece, null, true, true);
                if (OperatingSystem.isMacOSX()) {
                  cloneTextures(piece3D, pieceTextures);
                }
                modelTransformGroup.addChild(piece3D);
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
              0, false, this.previewedPiece.getColor(),
              this.previewedPiece.getModelRotation(), backFaceShown, 0, false));
      this.previewedPiece.setX(0);
      this.previewedPiece.setY(0);
      this.previewedPiece.setElevation(-previewedPiece.getHeight() / 2);

      TransformGroup modelTransformGroup = (TransformGroup)this.sceneTree.getChild(0);
      HomePieceOfFurniture3D piece3D = new HomePieceOfFurniture3D(previewedPiece, null, true, true);
      if (OperatingSystem.isMacOSX()) {
        this.pieceTextures.clear();
        cloneTextures(piece3D, this.pieceTextures);
      }
      modelTransformGroup.addChild(piece3D);
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
      // Check rotation isn't set on model node
      if (this.internalRotationAndSize) {
        throw new IllegalStateException("Can't set rotation");
      }
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
      // Check rotation isn't set on model node
      if (this.internalRotationAndSize) {
        throw new IllegalStateException("Can't set rotation and size");
      }
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
      cloneTextures(getModelNode(), this.pieceTextures);
    }
  }

  /**
   * Sets the transformations applied to 3D model.
   */
  public void setModelTranformations(Transformation [] transformations) {
    if (this.previewedPiece != null) {
      this.previewedPiece.setModelTransformations(transformations);
      getModelNode().update();
    }
  }

  void resetModelTranformations() {
    if (this.previewedPiece != null) {
      ModelManager modelManager = ModelManager.getInstance();
      BoundingBox oldBounds = modelManager.getBounds(getModelNode());
      Point3d oldLower = new Point3d();
      oldBounds.getLower(oldLower);
      Point3d oldUpper = new Point3d();
      oldBounds.getUpper(oldUpper);

      resetTranformations(getModelNode());

      BoundingBox newBounds = modelManager.getBounds(getModelNode());
      Point3d newLower = new Point3d();
      newBounds.getLower(newLower);
      Point3d newUpper = new Point3d();
      newBounds.getUpper(newUpper);
      previewedPiece.setX(previewedPiece.getX() + (float)(newUpper.x + newLower.x) / 2 - (float)(oldUpper.x + oldLower.x) / 2);
      previewedPiece.setY(previewedPiece.getY() + (float)(newUpper.z + newLower.z) / 2 - (float)(oldUpper.z + oldLower.z) / 2);
      previewedPiece.setElevation(previewedPiece.getElevation() + (float)(newLower.y - oldLower.y));
      this.previewedPiece.setWidth((float)(newUpper.x - newLower.x));
      this.previewedPiece.setDepth((float)(newUpper.z - newLower.z));
      this.previewedPiece.setHeight((float)(newUpper.y - newLower.y));
      this.previewedPiece.setModelTransformations(null);
    }
  }

  private void resetTranformations(Node node) {
    if (node instanceof Group) {
      if (node instanceof TransformGroup
          && node.getUserData() instanceof String
          && ((String)node.getUserData()).endsWith(ModelManager.DEFORMABLE_TRANSFORM_GROUP_SUFFIX)) {
        ((TransformGroup)node).setTransform(new Transform3D());
      }
      Enumeration<?> enumeration = ((Group)node).getAllChildren();
      while (enumeration.hasMoreElements()) {
        resetTranformations((Node)enumeration.nextElement());
      }
    }
  }

  /**
   * Returns the transformations applied to 3D model.
   */
  Transformation [] getModelTransformations() {
    if (this.previewedPiece != null) {
      return this.previewedPiece.getModelTransformations();
    } else {
      return null;
    }
  }

  /**
   * Returns the abscissa of the 3D model.
   */
  float getModelX() {
    return this.previewedPiece.getX();
  }

  /**
   * Returns the ordinate of the 3D model.
   */
  float getModelY() {
    return this.previewedPiece.getY();
  }

  /**
   * Returns the elevation of the 3D model.
   */
  float getModelElevation() {
    return this.previewedPiece.getElevation();
  }

  /**
   * Returns the width of the 3D model.
   */
  float getModelWidth() {
    return this.previewedPiece.getWidth();
  }

  /**
   * Returns the depth of the 3D model.
   */
  float getModelDepth() {
    return this.previewedPiece.getDepth();
  }

  /**
   * Returns the height of the 3D model.
   */
  float getModelHeight() {
    return this.previewedPiece.getHeight();
  }

  /**
   * Replace the textures set on <code>node</code> shapes by clones.
   */
  private void cloneTextures(Node node, Map<Texture, Texture> replacedTextures) {
    if (node instanceof Group) {
      // Enumerate children
      Enumeration<?> enumeration = ((Group)node).getAllChildren();
      while (enumeration.hasMoreElements()) {
        cloneTextures((Node)enumeration.nextElement(), replacedTextures);
      }
    } else if (node instanceof Link) {
      cloneTextures(((Link)node).getSharedGroup(), replacedTextures);
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
            if (OperatingSystem.isMacOSX()) {
              // Under Mac OS X, sleep an additional time to ensure the screen got refreshed
              Thread.sleep(30);
            }
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
            if (OperatingSystem.isMacOSX()) {
              // Under Mac OS X, sleep an additional time to ensure the screen got refreshed
              Thread.sleep(30);
            }
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