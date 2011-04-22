/*
 * ModelManager.java 4 juil. 07
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

import java.awt.EventQueue;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Geometry;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.GeometryStripArray;
import javax.media.j3d.Group;
import javax.media.j3d.IndexedGeometryArray;
import javax.media.j3d.IndexedGeometryStripArray;
import javax.media.j3d.IndexedQuadArray;
import javax.media.j3d.IndexedTriangleArray;
import javax.media.j3d.IndexedTriangleFanArray;
import javax.media.j3d.IndexedTriangleStripArray;
import javax.media.j3d.Light;
import javax.media.j3d.Link;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.QuadArray;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.SharedGroup;
import javax.media.j3d.Texture;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.media.j3d.TriangleArray;
import javax.media.j3d.TriangleFanArray;
import javax.media.j3d.TriangleStripArray;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.tools.TemporaryURLContent;
import com.eteks.sweethome3d.tools.URLContent;
import com.microcrowd.loader.java3d.max3ds.Loader3DS;
import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.Loader;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.lw3d.Lw3dLoader;

/**
 * Singleton managing 3D models cache.
 * This manager supports 3D models with an OBJ, 3DS or LWS format by default. 
 * Additional classes implementing Java 3D <code>Loader</code> interface may be 
 * specified in the <code>com.eteks.sweethome3d.j3d.additionalLoaderClasses</code>
 * (separated by a space or a colon :) to enable the support of other formats.<br> 
 * Note: this class is compatible with Java 3D 1.3.
 * @author Emmanuel Puybaret
 */
public class ModelManager {
  /**
   * <code>Shape3D</code> user data prefix for window pane shapes. 
   */
  public static final String WINDOW_PANE_SHAPE_PREFIX = "sweethome3d_window_pane";
  /**
   * <code>Shape3D</code> user data prefix for mirror shapes. 
   */
  public static final String MIRROR_SHAPE_PREFIX = "sweethome3d_window_mirror";
  /**
   * <code>Shape3D</code> user data prefix for lights. 
   */
  public static final String LIGHT_SHAPE_PREFIX = "sweethome3d_light";
  
  private static final TransparencyAttributes WINDOW_PANE_TRANSPARENCY_ATTRIBUTES = 
      new TransparencyAttributes(TransparencyAttributes.NICEST, 0.5f);

  private static final Material               DEFAULT_MATERIAL = new Material();
  
  private static final float MINIMUM_SIZE = 0.001f;

  private static final String ADDITIONAL_LOADER_CLASSES = "com.eteks.sweethome3d.j3d.additionalLoaderClasses";
  
  private static ModelManager instance;
  
  // Map storing loaded model nodes
  private Map<Content, BranchGroup> loadedModelNodes;
  // Map storing model nodes being loaded
  private Map<Content, List<ModelObserver>> loadingModelObservers;
  // Executor used to load models
  private ExecutorService           modelsLoader;
  // List of additional loader classes
  private Class<Loader> []          additionalLoaderClasses;
  
  private ModelManager() {    
    // This class is a singleton
    this.loadedModelNodes = new WeakHashMap<Content, BranchGroup>();
    this.loadingModelObservers = new HashMap<Content, List<ModelObserver>>();
    // Load other optional Loader classes 
    List<Class<Loader>> loaderClasses = new ArrayList<Class<Loader>>();
    String loaderClassNames = System.getProperty(ADDITIONAL_LOADER_CLASSES);
    if (loaderClassNames != null) {
      for (String loaderClassName : loaderClassNames.split("\\s|:")) {
        try {
          loaderClasses.add(getLoaderClass(loaderClassName));
        } catch (IllegalArgumentException ex) {
          System.err.println("Invalid loader class " + loaderClassName + ":\n" + ex.getMessage());
        }
      }
    }
    this.additionalLoaderClasses = loaderClasses.toArray(new Class [loaderClasses.size()]);
  }

  /**
   * Returns the class of name <code>loaderClassName</code>.
   */
  @SuppressWarnings("unchecked")
  private Class<Loader> getLoaderClass(String loaderClassName) {
    try {
      Class<Loader> loaderClass = (Class<Loader>)getClass().getClassLoader().loadClass(loaderClassName);
      if (!Loader.class.isAssignableFrom(loaderClass)) {
        throw new IllegalArgumentException(loaderClassName + " not a subclass of " + Loader.class.getName());
      } else if (Modifier.isAbstract(loaderClass.getModifiers()) || !Modifier.isPublic(loaderClass.getModifiers())) {
        throw new IllegalArgumentException(loaderClassName + " not a public static class");
      }
      Constructor<Loader> constructor = loaderClass.getConstructor(new Class [0]);
      // Try to instantiate it now to see if it won't cause any problem
      constructor.newInstance(new Object [0]);
      return loaderClass;
    } catch (ClassNotFoundException ex) {
      throw new IllegalArgumentException(ex.getMessage(), ex);
    } catch (NoSuchMethodException ex) {
      throw new IllegalArgumentException(ex.getMessage(), ex);
    } catch (InvocationTargetException ex) {
      throw new IllegalArgumentException(ex.getMessage(), ex);
    } catch (IllegalAccessException ex) {
      throw new IllegalArgumentException(loaderClassName + " constructor not accessible");
    } catch (InstantiationException ex) {
      throw new IllegalArgumentException(loaderClassName + " not a public static class");
    }
  }
  
  /**
   * Returns an instance of this singleton. 
   */
  public static ModelManager getInstance() {
    if (instance == null) {
      instance = new ModelManager();
    }
    return instance;
  }

  /**
   * Shutdowns the multithreaded service that load textures. 
   */
  public void clear() {
    if (this.modelsLoader != null) {
      this.modelsLoader.shutdownNow();
      this.modelsLoader = null;
    }
    synchronized (this.loadedModelNodes) {
      this.loadedModelNodes.clear();
    }
  }
  
  /**
   * Returns the size of 3D shapes under <code>node</code>.
   * This method computes the exact box that contains all the shapes,
   * contrary to <code>node.getBounds()</code> that returns a bounding 
   * sphere for a scene.
   */
  public Vector3f getSize(Node node) {
    BoundingBox bounds = getBounds(node);
    Point3d lower = new Point3d();
    bounds.getLower(lower);
    Point3d upper = new Point3d();
    bounds.getUpper(upper);
    return new Vector3f(Math.max(MINIMUM_SIZE, (float)(upper.x - lower.x)), 
        Math.max(MINIMUM_SIZE, (float)(upper.y - lower.y)), 
        Math.max(MINIMUM_SIZE, (float)(upper.z - lower.z)));
  }
  
  /**
   * Returns the bounds of 3D shapes under <code>node</code>.
   * This method computes the exact box that contains all the shapes,
   * contrary to <code>node.getBounds()</code> that returns a bounding 
   * sphere for a scene.
   */
  public BoundingBox getBounds(Node node) {
    BoundingBox objectBounds = new BoundingBox(
        new Point3d(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY),
        new Point3d(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY));
    computeBounds(node, objectBounds, new Transform3D());
    Point3d lower = new Point3d();
    objectBounds.getLower(lower);
    if (lower.x == Double.POSITIVE_INFINITY) {
      throw new IllegalArgumentException("Node has no bounds");
    }
    return objectBounds;
  }
  
  private void computeBounds(Node node, BoundingBox bounds, Transform3D parentTransformations) {
    if (node instanceof Group) {
      if (node instanceof TransformGroup) {
        parentTransformations = new Transform3D(parentTransformations);
        Transform3D transform = new Transform3D();
        ((TransformGroup)node).getTransform(transform);
        parentTransformations.mul(transform);
      }
      // Compute the bounds of all the node children
      Enumeration<?> enumeration = ((Group)node).getAllChildren();
      while (enumeration.hasMoreElements ()) {
        computeBounds((Node)enumeration.nextElement(), bounds, parentTransformations);
      }
    } else if (node instanceof Link) {
      computeBounds(((Link)node).getSharedGroup(), bounds, parentTransformations);
    } else if (node instanceof Shape3D) {
      Bounds shapeBounds = ((Shape3D)node).getBounds();
      shapeBounds.transform(parentTransformations);
      bounds.combine(shapeBounds);
    }
  }

  /**
   * Returns a transform group that will transform the model <code>node</code>
   * to let it fill a box of the given <code>width</code> centered on the origin.
   * @param node     the root of a model with any size and location
   * @param modelRotation the rotation applied to the model at the end 
   *                 or <code>null</code> if no transformation should be applied to node.
   * @param width    the width of the box
   */
  public TransformGroup getNormalizedTransformGroup(Node node, float [][] modelRotation, float width) {
    // Get model bounding box size
    BoundingBox modelBounds = getBounds(node);
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
        new Vector3d(width / Math.max(MINIMUM_SIZE, upper.x -lower.x), 
            width / Math.max(MINIMUM_SIZE, upper.y - lower.y), 
            width / Math.max(MINIMUM_SIZE, upper.z - lower.z)));
    scaleOneTransform.mul(translation);
    Transform3D modelTransform = new Transform3D();
    if (modelRotation != null) {
      // Apply model rotation
      Matrix3f modelRotationMatrix = new Matrix3f(modelRotation [0][0], modelRotation [0][1], modelRotation [0][2],
          modelRotation [1][0], modelRotation [1][1], modelRotation [1][2],
          modelRotation [2][0], modelRotation [2][1], modelRotation [2][2]);
      modelTransform.setRotation(modelRotationMatrix);
    }
    modelTransform.mul(scaleOneTransform);
    
    return new TransformGroup(modelTransform);
  }
  
  /**
   * Reads asynchronously a 3D node from <code>content</code> with supported loaders
   * and notifies the loaded model to the given <code>modelObserver</code> once available. 
   * @param content an object containing a model
   * @param modelObserver the observer that will be notified once the model is available
   *    or if an error happens
   * @throws IllegalStateException if the current thread isn't the Event Dispatch Thread.  
   */
  public void loadModel(Content content,
                        ModelObserver modelObserver) {
    loadModel(content, false, modelObserver);
  }
  
  /**
   * Reads a 3D node from <code>content</code> with supported loaders
   * and notifies the loaded model to the given <code>modelObserver</code> once available.
   * @param content an object containing a model
   * @param synchronous if <code>true</code>, this method will return only once model content is loaded
   * @param modelObserver the observer that will be notified once the model is available
   *    or if an error happens. When the model is loaded synchronously, the observer will be notified
   *    in the same thread as the caller, otherwise the observer will be notified in the Event 
   *    Dispatch Thread and this method must be called in Event Dispatch Thread too.
   * @throws IllegalStateException if synchronous is <code>false</code> and the current thread isn't 
   *    the Event Dispatch Thread.  
   */
  public void loadModel(final Content content,
                        boolean synchronous,
                        ModelObserver modelObserver) {
    BranchGroup modelRoot;
    synchronized (this.loadedModelNodes) {
      modelRoot = this.loadedModelNodes.get(content);
    }
    if (modelRoot != null) {
      // Notify cached model to observer with a clone of the model
      modelObserver.modelUpdated((BranchGroup)cloneNode(modelRoot));
    } else if (synchronous) {
      try {
        modelRoot = loadModel(content);
        synchronized (this.loadedModelNodes) {
          // Store in cache model node for future copies 
          this.loadedModelNodes.put(content, (BranchGroup)modelRoot);
        }
        modelObserver.modelUpdated((BranchGroup)cloneNode(modelRoot));
      } catch (IOException ex) {
        modelObserver.modelError(ex);
      }
    } else if (!EventQueue.isDispatchThread()) {
      throw new IllegalStateException("Asynchronous call out of Event Dispatch Thread");
    } else {  
      if (this.modelsLoader == null) {
        this.modelsLoader = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
      }
      List<ModelObserver> observers = this.loadingModelObservers.get(content);
      if (observers != null) {
        // If observers list exists, content model is already being loaded
        // register observer for future notification
        observers.add(modelObserver);
      } else {
        // Create a list of observers that will be notified once content model is loaded
        observers = new ArrayList<ModelObserver>();
        observers.add(modelObserver);
        this.loadingModelObservers.put(content, observers);
        
        // Load the model in an other thread
        this.modelsLoader.execute(new Runnable() {
          public void run() {
            try {
              final BranchGroup loadedModel = loadModel(content);
              synchronized (loadedModelNodes) {
                // Update loaded models cache and notify registered observers
                loadedModelNodes.put(content, loadedModel);
              }
              EventQueue.invokeLater(new Runnable() {
                  public void run() {
                    for (final ModelObserver observer : loadingModelObservers.remove(content)) {
                      observer.modelUpdated((BranchGroup)cloneNode(loadedModel));
                    }
                  }
                });
            } catch (final IOException ex) {
              EventQueue.invokeLater(new Runnable() {
                  public void run() {
                    for (final ModelObserver observer : loadingModelObservers.remove(content)) {
                      observer.modelError(ex);
                    }
                  }
                });
            }
          }
        });
      }
    }
  }
  
  /**
   * Returns a clone of the given <code>node</code>.
   * All the children and the attributes of the given node are duplicated except the geometries 
   * and the texture images of shapes.
   */
  public Node cloneNode(Node node) {
    // Clone node in a synchronized block because cloneNodeComponent is not thread safe
    synchronized (this.loadedModelNodes) {  
      return cloneNode(node, new HashMap<SharedGroup, SharedGroup>());
    }
  }
    
  private Node cloneNode(Node node, Map<SharedGroup, SharedGroup> clonedSharedGroups) {
    if (node instanceof Shape3D) {
      Shape3D shape = (Shape3D)node;
      Shape3D clonedShape = (Shape3D)shape.cloneNode(false);
      Appearance appearance = shape.getAppearance();
      if (appearance != null) {
        // Force only duplication of node's appearance except its texture
        Appearance clonedAppearance = (Appearance)appearance.cloneNodeComponent(true);        
        Texture texture = appearance.getTexture();
        if (texture != null) {
          clonedAppearance.setTexture(texture);
        }
        clonedShape.setAppearance(clonedAppearance);
      }
      return clonedShape;
    } else if (node instanceof Link) {
      Link clonedLink = (Link)node.cloneNode(true);
      // Force duplication of shared groups too
      SharedGroup sharedGroup = clonedLink.getSharedGroup();
      if (sharedGroup != null) {
        SharedGroup clonedSharedGroup = clonedSharedGroups.get(sharedGroup);
        if (clonedSharedGroup == null) {
          clonedSharedGroup = (SharedGroup)cloneNode(sharedGroup, clonedSharedGroups);
          clonedSharedGroups.put(sharedGroup, clonedSharedGroup);          
        }
        clonedLink.setSharedGroup(clonedSharedGroup);
      }
      return clonedLink;
    } else {
      Node clonedNode = node.cloneNode(true);
      if (node instanceof Group) {
        Group group = (Group)node;
        Group clonedGroup = (Group)clonedNode;
        for (int i = 0, n = group.numChildren(); i < n; i++) {
          Node clonedChild = cloneNode(group.getChild(i), clonedSharedGroups);
          clonedGroup.addChild(clonedChild);
        }
      }
      return clonedNode;
    }
  }
  
  /**
   * Returns the node loaded synchronously from <code>content</code> with supported loaders. 
   * This method is threadsafe and may be called from any thread.
   * @param content an object containing a model
   */
  public BranchGroup loadModel(Content content) throws IOException {
    // Ensure we use a URLContent object
    URLContent urlContent;
    if (content instanceof URLContent) {
      urlContent = (URLContent)content;
    } else {
      urlContent = TemporaryURLContent.copyToTemporaryURLContent(content);
    }
    
    Loader3DS loader3DSWithNoStackTraces = new Loader3DS() {
      @Override
      public Scene load(URL url) throws FileNotFoundException, IncorrectFormatException {
        try {
          // Check magic number 0x4D4D
          InputStream in = url.openStream();
          if (in.read() != 0x4D
              && in.read() != 0x4D) {
            throw new IncorrectFormatException("Bad magic number");
          }
          in.close();
        } catch (FileNotFoundException ex) {
          throw ex;
        } catch (IOException ex) {
          throw new ParsingErrorException("Can't read url " + url);
        }
        
        PrintStream defaultSystemErrorStream = System.err;
        try {
          // Ignore stack traces on System.err during 3DS file loading
          System.setErr(new PrintStream (new OutputStream() {
              @Override
              public void write(int b) throws IOException {
                // Do nothing
              }
            }));
          // Default load
          return super.load(url);
        } finally {
          // Reset default err print stream
          System.setErr(defaultSystemErrorStream);
        }
      }
    };

    Loader []  defaultLoaders = new Loader [] {new OBJLoader(),
                                               new DAELoader(),
                                               loader3DSWithNoStackTraces,
                                               new Lw3dLoader()};
    Loader [] loaders = new Loader [defaultLoaders.length + this.additionalLoaderClasses.length];
    System.arraycopy(defaultLoaders, 0, loaders, 0, defaultLoaders.length);
    for (int i = 0; i < this.additionalLoaderClasses.length; i++) {
      try {
        loaders [defaultLoaders.length + i] = this.additionalLoaderClasses [i].newInstance();
      } catch (InstantiationException ex) {
        // Can't happen: getLoaderClass checked this class is instantiable
        throw new InternalError(ex.getMessage());
      } catch (IllegalAccessException ex) {
        // Can't happen: getLoaderClass checked this class is instantiable 
        throw new InternalError(ex.getMessage());
      } 
    }
    
    Exception lastException = null;
    for (Loader loader : loaders) {
      try {     
        // Ask loader to ignore lights, fogs...
        loader.setFlags(loader.getFlags() 
            & ~(Loader.LOAD_LIGHT_NODES | Loader.LOAD_FOG_NODES 
                | Loader.LOAD_BACKGROUND_NODES | Loader.LOAD_VIEW_GROUPS));
        // Return the first scene that can be loaded from model URL content
        Scene scene = loader.load(urlContent.getURL());

        BranchGroup modelNode = scene.getSceneGroup();
        // If model doesn't have any child, consider the file as wrong
        if (modelNode.numChildren() == 0) {
          throw new IllegalArgumentException("Empty model");
        }
        
        // Update transparency of scene window panes shapes
        updateShapeNamesAndWindowPanesTransparency(scene);
        
        // Turn off lights because some loaders don't take into account the ~LOAD_LIGHT_NODES flag
        turnOffLightsShareAndModulateTextures(modelNode);

        return modelNode;
      } catch (IllegalArgumentException ex) {
        lastException = ex;
      } catch (IncorrectFormatException ex) {
        lastException = ex;
      } catch (ParsingErrorException ex) {
        lastException = ex;
      } catch (IOException ex) {
        lastException = ex;
      } catch (RuntimeException ex) {
        // Take into account exceptions of Java 3D 1.5 ImageException class
        // in such a way program can run in Java 3D 1.3.1
        if (ex.getClass().getName().equals("com.sun.j3d.utils.image.ImageException")) {
          lastException = ex;
        } else {
          throw ex;
        }
      }
    }
    
    if (lastException instanceof IOException) {
      throw (IOException)lastException;
    } else if (lastException instanceof IncorrectFormatException) {
      IOException incorrectFormatException = new IOException("Incorrect format");
      incorrectFormatException.initCause(lastException);
      throw incorrectFormatException;
    } else if (lastException instanceof ParsingErrorException) {
      IOException incorrectFormatException = new IOException("Parsing error");
      incorrectFormatException.initCause(lastException);
      throw incorrectFormatException;
    } else {
      IOException otherException = new IOException();
      otherException.initCause(lastException);
      throw otherException;
    } 
  }  
  
  /**
   * Updates the name of scene shapes and transparency window panes shapes.
   */
  @SuppressWarnings("unchecked")
  private void updateShapeNamesAndWindowPanesTransparency(Scene scene) {
    Map<String, Object> namedObjects = scene.getNamedObjects();
    for (Map.Entry<String, Object> entry : namedObjects.entrySet()) {
      if (entry.getValue() instanceof Shape3D) {
        String shapeName = entry.getKey();
        // Assign shape name to its user data
        Shape3D shape = (Shape3D)entry.getValue();
        shape.setUserData(shapeName);
        if (shapeName.startsWith(WINDOW_PANE_SHAPE_PREFIX)) {
          Appearance appearance = shape.getAppearance();
          if (appearance == null) {
            appearance = new Appearance();
            shape.setAppearance(appearance);
          }
          if (appearance.getTransparencyAttributes() == null) {
            appearance.setTransparencyAttributes(WINDOW_PANE_TRANSPARENCY_ATTRIBUTES);
          }
        }
      }
    }
  }
  
  /**
   * Turns off light nodes of <code>node</code> children, 
   * and modulates textures if needed.
   */
  private void turnOffLightsShareAndModulateTextures(Node node) {
    if (node instanceof Group) {
      // Enumerate children
      Enumeration<?> enumeration = ((Group)node).getAllChildren(); 
      while (enumeration.hasMoreElements()) {
        turnOffLightsShareAndModulateTextures((Node)enumeration.nextElement());
      }
    } else if (node instanceof Link) {
      turnOffLightsShareAndModulateTextures(((Link)node).getSharedGroup());
    } else if (node instanceof Light) {
      ((Light)node).setEnable(false);
    } else if (node instanceof Shape3D) {
      Appearance appearance = ((Shape3D)node).getAppearance();
      if (appearance != null) {
        Texture texture = appearance.getTexture();
        if (texture != null) {
          // Share textures data as much as possible
          Texture sharedTexture = TextureManager.getInstance().shareTexture(texture);
          if (sharedTexture != texture) {
            appearance.setTexture(sharedTexture);
          }
          TextureAttributes textureAttributes = appearance.getTextureAttributes();
          if (textureAttributes == null) {
            // Mix texture and shape color
            textureAttributes = new TextureAttributes();
            textureAttributes.setTextureMode(TextureAttributes.MODULATE);
            appearance.setTextureAttributes(textureAttributes);
            // Check shape color is white
            Material material = appearance.getMaterial();
            if (material == null) {
              appearance.setMaterial((Material)DEFAULT_MATERIAL.cloneNodeComponent(true));
            } else {
              Color3f color = new Color3f();
              DEFAULT_MATERIAL.getDiffuseColor(color);
              material.setDiffuseColor(color);
              DEFAULT_MATERIAL.getAmbientColor(color);
              material.setAmbientColor(color);
            }
          }
          
          // If texture image supports transparency
          if (TextureManager.getInstance().isTextureTransparent(sharedTexture)) {
            if (appearance.getTransparencyAttributes() == null) {
              // Add transparency attributes to ensure transparency works
              appearance.setTransparencyAttributes(
                  new TransparencyAttributes(TransparencyAttributes.NICEST, 0));
            }             
          }
        }
      }
    } 
  }

  /**
   * Returns the 2D area of the 3D shapes children of the given <code>node</code> 
   * projected on the floor (plan y = 0). 
   */
  public Area getAreaOnFloor(Node node) {
    Area modelAreaOnFloor;
    int vertexCount = getVertexCount(node);
    if (vertexCount < 10000) {
      modelAreaOnFloor = new Area();
      computeAreaOnFloor(node, modelAreaOnFloor, new Transform3D());
    } else {
      List<float []> vertices = new ArrayList<float[]>(vertexCount); 
      computeVerticesOnFloor(node, vertices, new Transform3D());
      float [][] surroundingPolygon = getSurroundingPolygon(vertices.toArray(new float [vertices.size()][]));
      GeneralPath generalPath = new GeneralPath(GeneralPath.WIND_NON_ZERO, surroundingPolygon.length);
      generalPath.moveTo(surroundingPolygon [0][0], surroundingPolygon [0][1]);
      for (int i = 0; i < surroundingPolygon.length; i++) {
        generalPath.lineTo(surroundingPolygon [i][0], surroundingPolygon [i][1]);
      }
      generalPath.closePath();
      modelAreaOnFloor = new Area(generalPath);
    }
    return modelAreaOnFloor;
  }
  
  /**
   * Returns the total count of vertices in all geometries.
   */
  private int getVertexCount(Node node) {
    int count = 0;
    if (node instanceof Group) {
      // Enumerate all children
      Enumeration<?> enumeration = ((Group)node).getAllChildren();
      while (enumeration.hasMoreElements()) {
        count += getVertexCount((Node)enumeration.nextElement());
      }
    } else if (node instanceof Link) {
      count = getVertexCount(((Link)node).getSharedGroup());
    } else if (node instanceof Shape3D) {
      Shape3D shape = (Shape3D)node;
      Appearance appearance = shape.getAppearance();
      RenderingAttributes renderingAttributes = appearance != null 
          ? appearance.getRenderingAttributes() : null;
      if (renderingAttributes == null
          || renderingAttributes.getVisible()) {
        for (int i = 0, n = shape.numGeometries(); i < n; i++) {
          Geometry geometry = shape.getGeometry(i);
          if (geometry instanceof GeometryArray) {
            count += ((GeometryArray)geometry).getVertexCount();
          }
        }
      }
    }    
    return count;
  }
  
  /**
   * Computes the vertices coordinates projected on floor of the 3D shapes children of <code>node</code>.
   */
  private void computeVerticesOnFloor(Node node, List<float []> vertices, Transform3D parentTransformations) {
    if (node instanceof Group) {
      if (node instanceof TransformGroup) {
        parentTransformations = new Transform3D(parentTransformations);
        Transform3D transform = new Transform3D();
        ((TransformGroup)node).getTransform(transform);
        parentTransformations.mul(transform);
      }
      // Compute all children
      Enumeration<?> enumeration = ((Group)node).getAllChildren(); 
      while (enumeration.hasMoreElements()) {
        computeVerticesOnFloor((Node)enumeration.nextElement(), vertices, parentTransformations);
      }
    } else if (node instanceof Link) {
      computeVerticesOnFloor(((Link)node).getSharedGroup(), vertices, parentTransformations);
    } else if (node instanceof Shape3D) {
      Shape3D shape = (Shape3D)node;
      Appearance appearance = shape.getAppearance();
      RenderingAttributes renderingAttributes = appearance != null 
          ? appearance.getRenderingAttributes() : null;
      TransparencyAttributes transparencyAttributes = appearance != null 
          ? appearance.getTransparencyAttributes() : null;
      if ((renderingAttributes == null
            || renderingAttributes.getVisible())
          && (transparencyAttributes == null
              || transparencyAttributes.getTransparency() < 1)) {
        // Compute shape geometries area
        for (int i = 0, n = shape.numGeometries(); i < n; i++) {
          Geometry geometry = shape.getGeometry(i);
          if (geometry instanceof GeometryArray) {
            GeometryArray geometryArray = (GeometryArray)geometry;      

            int vertexCount = geometryArray.getVertexCount();
            Point3f vertex = new Point3f();
            if ((geometryArray.getVertexFormat() & GeometryArray.BY_REFERENCE) != 0) {
              if ((geometryArray.getVertexFormat() & GeometryArray.INTERLEAVED) != 0) {
                float [] vertexData = geometryArray.getInterleavedVertices();
                int vertexSize = vertexData.length / vertexCount;
                // Store vertices coordinates 
                for (int index = 0, j = vertexSize - 3; index < vertexCount; j += vertexSize, index++) {
                  vertex.x = vertexData [j];
                  vertex.y = vertexData [j + 1];
                  vertex.z = vertexData [j + 2];
                  parentTransformations.transform(vertex);
                  vertices.add(new float [] {vertex.x, vertex.z});
                }
              } else {
                // Store vertices coordinates
                float [] vertexCoordinates = geometryArray.getCoordRefFloat();
                for (int index = 0, j = 0; index < vertexCount; j += 3, index++) {
                  vertex.x = vertexCoordinates [j];
                  vertex.y = vertexCoordinates [j + 1];
                  vertex.z = vertexCoordinates [j + 2];
                  parentTransformations.transform(vertex);
                  vertices.add(new float [] {vertex.x, vertex.z});
                }
              }
            } else {
              // Store vertices coordinates
              for (int index = 0, j = 0; index < vertexCount; j++, index++) {
                geometryArray.getCoordinate(j, vertex);
                parentTransformations.transform(vertex);
                vertices.add(new float [] {vertex.x, vertex.z});
              }
            }
          }
        }
      }
    }    
  }
  
  /**
   * Computes the 2D area on floor of the 3D shapes children of <code>node</code>.
   */
  private void computeAreaOnFloor(Node node, Area nodeArea, Transform3D parentTransformations) {
    if (node instanceof Group) {
      if (node instanceof TransformGroup) {
        parentTransformations = new Transform3D(parentTransformations);
        Transform3D transform = new Transform3D();
        ((TransformGroup)node).getTransform(transform);
        parentTransformations.mul(transform);
      }
      // Compute all children
      Enumeration<?> enumeration = ((Group)node).getAllChildren(); 
      while (enumeration.hasMoreElements()) {
        computeAreaOnFloor((Node)enumeration.nextElement(), nodeArea, parentTransformations);
      }
    } else if (node instanceof Link) {
      computeAreaOnFloor(((Link)node).getSharedGroup(), nodeArea, parentTransformations);
    } else if (node instanceof Shape3D) {
      Shape3D shape = (Shape3D)node;
      Appearance appearance = shape.getAppearance();
      RenderingAttributes renderingAttributes = appearance != null 
          ? appearance.getRenderingAttributes() : null;
      TransparencyAttributes transparencyAttributes = appearance != null 
          ? appearance.getTransparencyAttributes() : null;
      if ((renderingAttributes == null
            || renderingAttributes.getVisible())
          && (transparencyAttributes == null
              || transparencyAttributes.getTransparency() < 1)) {
        // Compute shape geometries area
        for (int i = 0, n = shape.numGeometries(); i < n; i++) {
          computeGeometryAreaOnFloor(shape.getGeometry(i), parentTransformations, nodeArea);
        }
      }
    }    
  }
  
  /**
   * Computes the area on floor of a 3D geometry.
   */
  private void computeGeometryAreaOnFloor(Geometry geometry, 
                                          Transform3D parentTransformations, 
                                          Area nodeArea) {
    if (geometry instanceof GeometryArray) {
      GeometryArray geometryArray = (GeometryArray)geometry;      

      int vertexCount = geometryArray.getVertexCount();
      float [] vertices = new float [vertexCount * 2]; 
      Point3f vertex = new Point3f();
      if ((geometryArray.getVertexFormat() & GeometryArray.BY_REFERENCE) != 0) {
        if ((geometryArray.getVertexFormat() & GeometryArray.INTERLEAVED) != 0) {
          float [] vertexData = geometryArray.getInterleavedVertices();
          int vertexSize = vertexData.length / vertexCount;
          // Store vertices coordinates 
          for (int index = 0, i = vertexSize - 3; index < vertices.length; i += vertexSize) {
            vertex.x = vertexData [i];
            vertex.y = vertexData [i + 1];
            vertex.z = vertexData [i + 2];
            parentTransformations.transform(vertex);
            vertices [index++] = vertex.x;
            vertices [index++] = vertex.z;
          }
        } else {
          // Store vertices coordinates
          float [] vertexCoordinates = geometryArray.getCoordRefFloat();
          for (int index = 0, i = 0; index < vertices.length; i += 3) {
            vertex.x = vertexCoordinates [i];
            vertex.y = vertexCoordinates [i + 1];
            vertex.z = vertexCoordinates [i + 2];
            parentTransformations.transform(vertex);
            vertices [index++] = vertex.x;
            vertices [index++] = vertex.z;
          }
        }
      } else {
        // Store vertices coordinates
        for (int index = 0, i = 0; index < vertices.length; i++) {
          geometryArray.getCoordinate(i, vertex);
          parentTransformations.transform(vertex);
          vertices [index++] = vertex.x;
          vertices [index++] = vertex.z;
        }
      }

      // Create path from triangles or quadrilaterals of geometry
      GeneralPath geometryPath = null;
      if (geometryArray instanceof IndexedGeometryArray) {
        if (geometryArray instanceof IndexedTriangleArray) {
          IndexedTriangleArray triangleArray = (IndexedTriangleArray)geometryArray;
          geometryPath = new GeneralPath(GeneralPath.WIND_NON_ZERO, 1000);
          for (int i = 0, triangleIndex = 0, n = triangleArray.getIndexCount(); i < n; i += 3) {
            addIndexedTriangleToPath(triangleArray, i, i + 1, i + 2, vertices, 
                geometryPath, triangleIndex++, nodeArea);
          }
        } else if (geometryArray instanceof IndexedQuadArray) {
          IndexedQuadArray quadArray = (IndexedQuadArray)geometryArray;
          geometryPath = new GeneralPath(GeneralPath.WIND_NON_ZERO, 1000);
          for (int i = 0, quadrilateralIndex = 0, n = quadArray.getIndexCount(); i < n; i += 4) {
            addIndexedQuadrilateralToPath(quadArray, i, i + 1, i + 2, i + 3, vertices, 
                geometryPath, quadrilateralIndex++, nodeArea); 
          }
        } else if (geometryArray instanceof IndexedGeometryStripArray) {
          IndexedGeometryStripArray geometryStripArray = (IndexedGeometryStripArray)geometryArray;
          int [] stripIndexCounts = new int [geometryStripArray.getNumStrips()];
          geometryStripArray.getStripIndexCounts(stripIndexCounts);
          geometryPath = new GeneralPath(GeneralPath.WIND_NON_ZERO, 1000);
          int initialIndex = 0; 
          
          if (geometryStripArray instanceof IndexedTriangleStripArray) {
            for (int strip = 0, triangleIndex = 0; strip < stripIndexCounts.length; strip++) {
              for (int i = initialIndex, n = initialIndex + stripIndexCounts [strip] - 2, j = 0; i < n; i++, j++) {
                if (j % 2 == 0) {
                  addIndexedTriangleToPath(geometryStripArray, i, i + 1, i + 2, vertices, 
                      geometryPath, triangleIndex++, nodeArea); 
                } else { // Vertices of odd triangles are in reverse order               
                  addIndexedTriangleToPath(geometryStripArray, i, i + 2, i + 1, vertices, 
                      geometryPath, triangleIndex++, nodeArea);
                }
              }
              initialIndex += stripIndexCounts [strip];
            }
          } else if (geometryStripArray instanceof IndexedTriangleFanArray) {
            for (int strip = 0, triangleIndex = 0; strip < stripIndexCounts.length; strip++) {
              for (int i = initialIndex, n = initialIndex + stripIndexCounts [strip] - 2; i < n; i++) {
                addIndexedTriangleToPath(geometryStripArray, initialIndex, i + 1, i + 2, vertices, 
                    geometryPath, triangleIndex++, nodeArea); 
              }
              initialIndex += stripIndexCounts [strip];
            }
          }
        }
      } else {
        if (geometryArray instanceof TriangleArray) {
          TriangleArray triangleArray = (TriangleArray)geometryArray;
          geometryPath = new GeneralPath(GeneralPath.WIND_NON_ZERO, 1000);
          for (int i = 0, triangleIndex = 0; i < vertexCount; i += 3) {
            addTriangleToPath(triangleArray, i, i + 1, i + 2, vertices, 
                geometryPath, triangleIndex++, nodeArea);
          }
        } else if (geometryArray instanceof QuadArray) {
          QuadArray quadArray = (QuadArray)geometryArray;
          geometryPath = new GeneralPath(GeneralPath.WIND_NON_ZERO, 1000);
          for (int i = 0, quadrilateralIndex = 0; i < vertexCount; i += 4) {
            addQuadrilateralToPath(quadArray, i, i + 1, i + 2, i + 3, vertices, 
                geometryPath, quadrilateralIndex++, nodeArea);
          }
        } else if (geometryArray instanceof GeometryStripArray) {
          GeometryStripArray geometryStripArray = (GeometryStripArray)geometryArray;
          int [] stripVertexCounts = new int [geometryStripArray.getNumStrips()];
          geometryStripArray.getStripVertexCounts(stripVertexCounts);
          geometryPath = new GeneralPath(GeneralPath.WIND_NON_ZERO, 1000);
          int initialIndex = 0;
          
          if (geometryStripArray instanceof TriangleStripArray) {
            for (int strip = 0, triangleIndex = 0; strip < stripVertexCounts.length; strip++) {
              for (int i = initialIndex, n = initialIndex + stripVertexCounts [strip] - 2, j = 0; i < n; i++, j++) {
                if (j % 2 == 0) {
                  addTriangleToPath(geometryStripArray, i, i + 1, i + 2, vertices, 
                      geometryPath, triangleIndex++, nodeArea);
                } else { // Vertices of odd triangles are in reverse order               
                  addTriangleToPath(geometryStripArray, i, i + 2, i + 1, vertices, 
                      geometryPath, triangleIndex++, nodeArea);
                }
              }
              initialIndex += stripVertexCounts [strip];
            }
          } else if (geometryStripArray instanceof TriangleFanArray) {
            for (int strip = 0, triangleIndex = 0; strip < stripVertexCounts.length; strip++) {
              for (int i = initialIndex, n = initialIndex + stripVertexCounts [strip] - 2; i < n; i++) {
                addTriangleToPath(geometryStripArray, initialIndex, i + 1, i + 2, vertices, 
                    geometryPath, triangleIndex++, nodeArea);
              }
              initialIndex += stripVertexCounts [strip];
            }
          }
        }
      }
      
      if (geometryPath != null) {
        nodeArea.add(new Area(geometryPath));
      }
    } 
  }

  /**
   * Adds to <code>nodePath</code> the triangle joining vertices at 
   * vertexIndex1, vertexIndex2, vertexIndex3 indices.
   */
  private void addIndexedTriangleToPath(IndexedGeometryArray geometryArray, 
                                    int vertexIndex1, int vertexIndex2, int vertexIndex3, 
                                    float [] vertices, 
                                    GeneralPath geometryPath, int triangleIndex, Area nodeArea) {
    addTriangleToPath(geometryArray, geometryArray.getCoordinateIndex(vertexIndex1), 
        geometryArray.getCoordinateIndex(vertexIndex2), 
        geometryArray.getCoordinateIndex(vertexIndex3), vertices, geometryPath, triangleIndex, nodeArea);
  }
  
  /**
   * Adds to <code>nodePath</code> the quadrilateral joining vertices at 
   * vertexIndex1, vertexIndex2, vertexIndex3, vertexIndex4 indices.
   */
  private void addIndexedQuadrilateralToPath(IndexedGeometryArray geometryArray, 
                                         int vertexIndex1, int vertexIndex2, int vertexIndex3, int vertexIndex4, 
                                         float [] vertices, 
                                         GeneralPath geometryPath, int quadrilateralIndex, Area nodeArea) {
    addQuadrilateralToPath(geometryArray, geometryArray.getCoordinateIndex(vertexIndex1), 
        geometryArray.getCoordinateIndex(vertexIndex2), 
        geometryArray.getCoordinateIndex(vertexIndex3), 
        geometryArray.getCoordinateIndex(vertexIndex4), vertices, geometryPath, quadrilateralIndex, nodeArea);
  }
  
  /**
   * Adds to <code>nodePath</code> the triangle joining vertices at 
   * vertexIndex1, vertexIndex2, vertexIndex3 indices, 
   * only if the triangle has a positive orientation. 
   */
  private void addTriangleToPath(GeometryArray geometryArray, 
                             int vertexIndex1, int vertexIndex2, int vertexIndex3, 
                             float [] vertices, 
                             GeneralPath geometryPath, int triangleIndex, Area nodeArea) {
    float xVertex1 = vertices [2 * vertexIndex1];
    float yVertex1 = vertices [2 * vertexIndex1 + 1];
    float xVertex2 = vertices [2 * vertexIndex2];
    float yVertex2 = vertices [2 * vertexIndex2 + 1];
    float xVertex3 = vertices [2 * vertexIndex3];
    float yVertex3 = vertices [2 * vertexIndex3 + 1];
    if ((xVertex2 - xVertex1) * (yVertex3 - yVertex2) - (yVertex2 - yVertex1) * (xVertex3 - xVertex2) > 0) {
      if (triangleIndex > 0 && triangleIndex % 1000 == 0) {
        // Add now current path to area otherwise area gets too slow
        nodeArea.add(new Area(geometryPath));
        geometryPath.reset();
      }
      geometryPath.moveTo(xVertex1, yVertex1);      
      geometryPath.lineTo(xVertex2, yVertex2);      
      geometryPath.lineTo(xVertex3, yVertex3);
      geometryPath.closePath();
    }
  }
  
  /**
   * Adds to <code>nodePath</code> the quadrilateral joining vertices at 
   * vertexIndex1, vertexIndex2, vertexIndex3, vertexIndex4 indices, 
   * only if the quadrilateral has a positive orientation. 
   */
  private void addQuadrilateralToPath(GeometryArray geometryArray, 
                                      int vertexIndex1, int vertexIndex2, int vertexIndex3, int vertexIndex4, 
                                      float [] vertices, 
                                      GeneralPath geometryPath, int quadrilateralIndex, Area nodeArea) {
    float xVertex1 = vertices [2 * vertexIndex1];
    float yVertex1 = vertices [2 * vertexIndex1 + 1];
    float xVertex2 = vertices [2 * vertexIndex2];
    float yVertex2 = vertices [2 * vertexIndex2 + 1];
    float xVertex3 = vertices [2 * vertexIndex3];
    float yVertex3 = vertices [2 * vertexIndex3 + 1];
    if ((xVertex2 - xVertex1) * (yVertex3 - yVertex2) - (yVertex2 - yVertex1) * (xVertex3 - xVertex2) > 0) {
      if (quadrilateralIndex > 0 && quadrilateralIndex % 1000 == 0) {
        // Add now current path to area otherwise area gets too slow
        nodeArea.add(new Area(geometryPath));
        geometryPath.reset();
      }
      geometryPath.moveTo(xVertex1, yVertex1);      
      geometryPath.lineTo(xVertex2, yVertex2);      
      geometryPath.lineTo(xVertex3, yVertex3);
      geometryPath.lineTo(vertices [2 * vertexIndex4], vertices [2 * vertexIndex4 + 1]);
      geometryPath.closePath();
    }
  }

  /**
   * Returns the convex polygon that surrounds the given <code>vertices</code>.
   * From Andrew's monotone chain 2D convex hull algorithm described at
   * http://softsurfer.com/Archive/algorithm%5F0109/algorithm%5F0109.htm
   */
  private float [][] getSurroundingPolygon(float [][] vertices) {
    Arrays.sort(vertices, new Comparator<float []> () {
        public int compare(float [] vertex1, float [] vertex2) {
          if (vertex1 [0] == vertex2 [0]) {
            return (int)Math.signum(vertex2 [1] - vertex1 [1]);
          } else {
            return (int)Math.signum(vertex2 [0] - vertex1 [0]);
          }
        }
      });
    float [][] polygon = new float [vertices.length][];
    // The output array polygon [] will be used as the stack
    int bottom = 0, top = -1; // indices for bottom and top of the stack
    int i; // array scan index

    // Get the indices of points with min x-coord and min|max y-coord
    int minMin = 0, minMax;
    float xmin = vertices [0][0];
    for (i = 1; i < vertices.length; i++) {
      if (vertices [i][0] != xmin) {
        break;
      }
    }
    minMax = i - 1;
    if (minMax == vertices.length - 1) { 
      // Degenerate case: all x-coords == xmin
      polygon [++top] = vertices [minMin];
      if (vertices [minMax][1] != vertices [minMin][1]) { 
        // A nontrivial segment
        polygon [++top] = vertices [minMax];
      }
      // Add polygon end point
      polygon [++top] = vertices [minMin];
      float [][] surroundingPolygon = new float [top + 1][];
      System.arraycopy(polygon, 0, surroundingPolygon, 0, surroundingPolygon.length);
    }

    // Get the indices of points with max x-coord and min|max y-coord
    int maxMin, maxMax = vertices.length - 1;
    float xMax = vertices [vertices.length - 1][0];
    for (i = vertices.length - 2; i >= 0; i--) {
      if (vertices [i][0] != xMax) {
        break;
      }
    }
    maxMin = i + 1;

    // Compute the lower hull on the stack polygon
    polygon [++top] = vertices [minMin]; // push minmin point onto stack
    i = minMax;
    while (++i <= maxMin) {
      // The lower line joins points [minmin] with points [maxmin]
      if (isLeft(vertices [minMin], vertices [maxMin], vertices [i]) >= 0 && i < maxMin) {
        // ignore points [i] above or on the lower line
        continue; 
      }

      while (top > 0) // There are at least 2 points on the stack
      {
        // Test if points [i] is left of the line at the stack top
        if (isLeft(polygon [top - 1], polygon [top], vertices [i]) > 0)
          break; // points [i] is a new hull vertex
        else
          top--; // pop top point off stack
      }
      polygon [++top] = vertices [i]; // push points [i] onto stack
    }

    // Next, compute the upper hull on the stack polygon above the bottom hull
    // If distinct xmax points
    if (maxMax != maxMin) { 
      // Push maxmax point onto stack
      polygon [++top] = vertices [maxMax]; 
    }
    // The bottom point of the upper hull stack
    bottom = top; 
    i = maxMin;
    while (--i >= minMax) {
      // The upper line joins points [maxmax] with points [minmax]
      if (isLeft(vertices [maxMax], vertices [minMax], vertices [i]) >= 0 && i > minMax) {
        // Ignore points [i] below or on the upper line
        continue; 
      }

      // At least 2 points on the upper stack
      while (top > bottom) 
      {
        // Test if points [i] is left of the line at the stack top
        if (isLeft(polygon [top - 1], polygon [top], vertices [i]) > 0) {
          // points [i] is a new hull vertex
          break; 
        } else {
          // Pop top point off stack
          top--; 
        }
      }
      // Push points [i] onto stack
      polygon [++top] = vertices [i]; 
    }
    if (minMax != minMin) {
      // Push joining endpoint onto stack
      polygon [++top] = vertices [minMin]; 
    }

    float [][] surroundingPolygon = new float [top + 1][];
    System.arraycopy(polygon, 0, surroundingPolygon, 0, surroundingPolygon.length);
    return surroundingPolygon;
  }

  private float isLeft(float [] vertex0, float [] vertex1, float [] vertex2) {
    return (vertex1 [0] - vertex0 [0]) * (vertex2 [1] - vertex0 [1]) 
         - (vertex2 [0] - vertex0 [0]) * (vertex1 [1] - vertex0 [1]);
  }

  /**
   * An observer that receives model loading notifications. 
   */
  public static interface ModelObserver {
    public void modelUpdated(BranchGroup modelRoot); 
    
    public void modelError(Exception ex);
  }
}
