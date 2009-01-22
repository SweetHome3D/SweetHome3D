/*
 * ModelManager.java 4 juil. 07
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PushbackInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Enumeration;
import java.util.Map;
import java.util.WeakHashMap;

import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.Light;
import javax.media.j3d.Node;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Point3d;
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
import com.sun.j3d.loaders.objectfile.ObjectFile;

/**
 * Singleton managing 3D models cache.
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

  private static final TransparencyAttributes WINDOW_PANE_TRANSPARENCY_ATTRIBUTES = 
      new TransparencyAttributes(TransparencyAttributes.NICEST, 0.5f);
  
  private static ModelManager instance;
  
  // Map storing loaded model nodes
  private Map<Content, BranchGroup> modelNodes;
  
  private ModelManager() {    
    // This class is a singleton
    this.modelNodes = new WeakHashMap<Content, BranchGroup>();
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
    return new Vector3f((float)(upper.x - lower.x), (float)(upper.y - lower.y), (float)(upper.z - lower.z));
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
    computeBounds(node, objectBounds);
    return objectBounds;
  }
  
  private void computeBounds(Node node, BoundingBox bounds) {
    if (node instanceof Group) {
      // Compute the bounds of all the node children
      Enumeration enumeration = ((Group)node).getAllChildren();
      while (enumeration.hasMoreElements ()) {
        computeBounds((Node)enumeration.nextElement (), bounds);
      }
    } else if (node instanceof Shape3D) {
      Bounds shapeBounds = ((Shape3D)node).getBounds();
      bounds.combine(shapeBounds);
    }
  }
  
  /**
   * Returns the node loaded synchronously from <code>content</code> with supported loaders.
   */
  public BranchGroup getModel(Content content) throws IOException {
    BranchGroup modelNode = this.modelNodes.get(content);
    if (modelNode != null) {
      return (BranchGroup)modelNode.cloneTree(true);
    }
    
    // Ensure we use a URLContent object
    URLContent urlContent;
    if (content instanceof URLContent) {
      urlContent = (URLContent)content;
    } else {
      urlContent = TemporaryURLContent.copyToTemporaryURLContent(content);
    }
    
    modelNode = loadModel(urlContent);
    
    // Store in cache a clone of model node for future copies 
    modelNodes.put(content, (BranchGroup)modelNode.cloneTree(true));
    return modelNode;
  }
  
  /**
   * Returns the model read from <code>urlContent</code> with one of the loaders in parameter.
   */
  private BranchGroup loadModel(URLContent urlContent) throws IOException {
    Loader3DS loader3DSWithNoStackTraces = new Loader3DS() {
      @Override
      public Scene load(URL url) throws FileNotFoundException {
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

    Loader []  loaders = new Loader [] {new ObjectFile(),
                                        new ObjectFileTranslator(),
                                        loader3DSWithNoStackTraces,
                                        new Lw3dLoader()};
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
        turnOffLights(modelNode);

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
    
    if (lastException instanceof IncorrectFormatException) {
      IOException incorrectFormatException = new IOException("Incorrect format");
      incorrectFormatException.initCause(lastException);
      throw incorrectFormatException;
    } else if (lastException instanceof ParsingErrorException) {
      IOException incorrectFormatException = new IOException("Parsing error");
      incorrectFormatException.initCause(lastException);
      throw incorrectFormatException;
    } else {
      throw (IOException)lastException;
    } 
  }  
  
  /**
   * Updates the name of scene shapes and transparency window panes shapes.
   */
  private void updateShapeNamesAndWindowPanesTransparency(Scene scene) {
    Map<String, Object> namedObjects = scene.getNamedObjects();
    for (Map.Entry<String, Object> entry : namedObjects.entrySet()) {
      if (entry.getValue() instanceof Shape3D) {
        // Assign shape name to its user data
        Shape3D shape = (Shape3D)entry.getValue();
        shape.setUserData(entry.getKey());
        if (entry.getKey().startsWith(WINDOW_PANE_SHAPE_PREFIX)) {
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
   * Turn off light nodes of <code>node</code> children.
   */
  private void turnOffLights(Node node) {
    if (node instanceof Group) {
      // Remove lights of all children
      Enumeration enumeration = ((Group)node).getAllChildren(); 
      while (enumeration.hasMoreElements()) {
        turnOffLights((Node)enumeration.nextElement());
      }
    } else if (node instanceof Light) {
      ((Light)node).setEnable(false);
    }
  }

  /**
   * An object file loader that translates lines starting by "o" unsupported by 
   * <code>ObjectFile</code> loader to lines starting by "g".
   */
  private static class ObjectFileTranslator extends ObjectFile {
    @Override
    public Scene load(final URL defaultUrl) throws FileNotFoundException, IncorrectFormatException, ParsingErrorException {
      try {
        // Load scene with a filtered URL input stream
        return super.load(new URL(defaultUrl, "", new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL url) throws IOException {
              // Get default connection
              final URLConnection defaultConnection = defaultUrl.openConnection();
              return new URLConnection(url) {
                  @Override
                  public void connect() throws IOException {
                    defaultConnection.connect();
                  }
                  
                  @Override
                  public InputStream getInputStream() throws IOException {
                    InputStream defaultInputStream = defaultConnection.getInputStream();
                    return new OPrefixToGPrefixFilterInputStream(defaultInputStream);
                  }
                };
            }
          }));
      } catch (MalformedURLException ex) {
        // If url is malformed, let default implementation decide what to do  
        return super.load(defaultUrl);
      }
    }
  }

  /**
   * An input stream filter that replaces lines starting by 'o' letter 
   * followed by a space by 'g' letters.
   */
  private static class OPrefixToGPrefixFilterInputStream extends PushbackInputStream {
    public OPrefixToGPrefixFilterInputStream(InputStream in) {
      super(in, 2);
    }

    @Override
    public int read() throws IOException {
      int b = super.read();
      // If read byte is a line return followed by a 'o' and a space or a tab, 
      // replace 'o' by 'g'
      if (b == '\n'
          || b == '\r') {
        int nextByte = super.read();
        if (nextByte == 'o') {
          int nextNextByte = super.read();
          if (nextNextByte != -1) {
            unread(nextNextByte);
          }
          if (nextNextByte == ' ' || nextNextByte == '\t') {
            unread('g');
          } else {
            unread(nextByte);
          }
        } else if (nextByte != -1) {
          unread(nextByte);
        }
      }
      return b;
    }

    @Override
    public int read(byte [] bytes, int off, int len) throws IOException {
      int byteCount = super.read(bytes, off, len);
      if (byteCount >= 0) {
        for (int i = 0; i < byteCount; i++) {
          byte b = bytes [off + i];
          // If read byte is a line return followed by a 'o' and a space or a tab, 
          // replace 'o' by 'g'
          if (b == '\n'
              || b == '\r') {
            int nextByte;
            if (i >= byteCount - 1) {
              nextByte = super.read();
            } else {
              nextByte = bytes [off + i + 1];
            }
            
            if (nextByte == 'o') {
              int nextNextByte;
              if (i >= byteCount - 2) {
                nextNextByte = super.read();
                if (nextNextByte != -1) {
                  unread(nextNextByte);
                }
              } else {
                nextNextByte = bytes [off + i + 2];
              }
              
              if (nextNextByte == ' ' || nextNextByte == '\t') {
                if (i >= byteCount - 1) {
                  unread('g');
                } else {
                  bytes [off + i + 1] = 'g';
                }
              } 
            } else if (i >= byteCount - 1 && nextByte != -1) {              
              unread(nextByte);
            }
          }
        }
      }
      return byteCount;
    }
  }
}
