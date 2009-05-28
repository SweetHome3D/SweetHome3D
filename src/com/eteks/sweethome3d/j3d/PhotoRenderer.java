/*
 * PhotoRenderer.java 22 janv. 2009
 *
 * Copyright (c) 2009 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.media.j3d.Appearance;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Geometry;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Group;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.IndexedGeometryArray;
import javax.media.j3d.IndexedQuadArray;
import javax.media.j3d.IndexedTriangleArray;
import javax.media.j3d.IndexedTriangleFanArray;
import javax.media.j3d.IndexedTriangleStripArray;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Texture;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.media.j3d.TriangleArray;
import javax.media.j3d.TriangleFanArray;
import javax.media.j3d.TriangleStripArray;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import javax.vecmath.TexCoord2f;
import javax.vecmath.Vector3f;

import org.sunflow.PluginRegistry;
import org.sunflow.SunflowAPI;
import org.sunflow.core.Display;
import org.sunflow.core.Instance;
import org.sunflow.core.light.SphereLight;
import org.sunflow.image.Color;
import org.sunflow.math.Matrix4;
import org.sunflow.math.Point3;
import org.sunflow.math.Vector3;
import org.sunflow.system.UI;
import org.sunflow.system.ui.SilentInterface;

import com.eteks.sweethome3d.model.Camera;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.Wall;

/**
 * A renderer able to create a photo realistic image of a home.
 * @author Emmanuel Puybaret
 */
public class PhotoRenderer {
  public enum Quality {LOW, HIGH}
  
  private final static String CAMERA_NAME = "camera";
  
  private final Quality quality;
  private final SunflowAPI sunflow;
  private final Map<Texture, File> textureImageFilesCache = new HashMap<Texture, File>();

  static {
    // Ignore logs
    UI.set(new SilentInterface());
    PluginRegistry.lightSourcePlugins.registerPlugin("sphere", SphereLightWithNoRepresentation.class);
  }

  /**
   * Creates an instance ready to render the scene matching the given <code>home</code>.
   * @throws IOException if texture image files required in the scene couldn't be created. 
   */
  public PhotoRenderer(Home home, Quality quality) throws IOException {
    // As only one SunFlow renderer can run at a time,
    // wait 10s max that SunFlow rendering threads end
    for (int i = 0; UI.taskCanceled() && i < 100; i++) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException ex) {
        break;
      }
    }
    // If SunFlow rendering threads are still running, 
    // there must be a big problem
    if (UI.taskCanceled()) {
      throw new IllegalStateException("Can't stop SunFlow");
    }
    
    this.sunflow = new SunflowAPI();
    this.quality = quality;
    int samples = quality == Quality.LOW ? 4 : 8;
    
    // Export to SunFlow the Java 3D shapes and appearance of the ground, the walls, the furniture and the rooms           
    final boolean useNormals = true;
    Ground3D ground = new Ground3D(home, -1E7f / 2, -1E7f / 2, 1E7f, 1E7f, true);
    exportNode(ground, useNormals , true);
    for (Wall wall : home.getWalls()) {
      Wall3D wall3D = new Wall3D(wall, home, true, true);
      exportNode(wall3D, useNormals, false);
    }
    for (HomePieceOfFurniture piece : home.getFurniture()) {
      HomePieceOfFurniture3D piece3D = new HomePieceOfFurniture3D(piece, home, true, true);
      exportNode(piece3D, useNormals, false);
    }
    for (Room room : home.getRooms()) {
      Room3D room3D = new Room3D(room, home, home.getCamera() == home.getTopCamera(), true, true);
      exportNode(room3D, useNormals, false);
    } 

    // Set light settings 
    boolean observerCamera = home.getCamera() == home.getObserverCamera();
    HomeTexture skyTexture = home.getEnvironment().getSkyTexture();
    if (observerCamera 
        && skyTexture != null
        && quality == Quality.HIGH) {
      // If observer camera is used and high quality is requested, 
      // create an image base light from sky texture  
      BufferedImage skyImage = ImageIO.read(skyTexture.getImage().openStream());
      // Create a temporary image base light twice as high that will contain sky image in the top part
      BufferedImage imageBaseLightImage = new BufferedImage(skyImage.getWidth(), 
          skyImage.getHeight() * 2, BufferedImage.TYPE_INT_RGB);
      Graphics2D g2D = (Graphics2D)imageBaseLightImage.getGraphics();
      g2D.drawRenderedImage(skyImage, null);
      g2D.dispose();
      File imageFile = File.createTempFile("ibl", ".jpg");
      imageFile.deleteOnExit();
      ImageIO.write(imageBaseLightImage, "JPEG", imageFile);
      
      this.sunflow.parameter("texture", imageFile.getAbsolutePath());
      this.sunflow.parameter("center", new Vector3(-1, 0, 0));
      this.sunflow.parameter("up", new Vector3(0, 1, 0));
      this.sunflow.parameter("fixed", true);
      this.sunflow.parameter("samples", samples);
      this.sunflow.light(UUID.randomUUID().toString(), "ibl");
    } else {
      // Use sun sky light
      this.sunflow.parameter("up", new Vector3(0, 1, 0));
      this.sunflow.parameter("east", new Vector3(0, 0, 1));
      this.sunflow.parameter("sundir", new Vector3(1, 1, 1));
      this.sunflow.parameter("turbidity", 6f);
      this.sunflow.parameter("samples", samples * 3 / 2);
      this.sunflow.light(UUID.randomUUID().toString(), "sunsky");
    }
    
    // Add lights at the top of each room when observer camera is used
    if (observerCamera) {
      for (Room room : home.getRooms()) {
        if (room.isCeilingVisible()) {
          float xCenter = room.getXCenter();
          float yCenter = room.getYCenter();
          
          double smallestDistance = Float.POSITIVE_INFINITY;
          float roomHeight = home.getWallHeight();
          
          // Search the height of the wall closest to the point xCenter, yCenter
          for (Wall wall : home.getWalls()) {
            Float wallHeightAtStart = wall.getHeight();
            float [][] points = wall.getPoints();
            for (int i = 0; i < points.length; i++) {
              double distanceToWallPoint = Point2D.distanceSq(points [i][0], points [i][1], xCenter, yCenter);
              if (distanceToWallPoint < smallestDistance) {
                smallestDistance = distanceToWallPoint; 
                if (i == 0 || i == 3) { // Wall start
                  roomHeight = wallHeightAtStart != null 
                      ? wallHeightAtStart 
                      : home.getWallHeight();
                } else { // Wall end
                  roomHeight = wall.isTrapezoidal() 
                      ? wall.getHeightAtEnd() 
                      : (wallHeightAtStart != null ? wallHeightAtStart : home.getWallHeight());
                }
              }
            }
          }
          
          float power = (float)Math.sqrt(room.getArea()) / 3;
          int lightColor = home.getEnvironment().getLightColor();
          this.sunflow.parameter("radiance", null, 
              (lightColor >> 16) * power / 255, ((lightColor >> 8) & 0xFF) * power / 255, (lightColor & 0xFF) * power / 255);
          this.sunflow.parameter("center", new Point3(xCenter, roomHeight - 25, yCenter));                    
          this.sunflow.parameter("radius", 20f);
          this.sunflow.parameter("samples", samples);
          this.sunflow.light(UUID.randomUUID().toString(), "sphere");
        } 
      }
    }

    // Create pinhole camera at default location 
    this.sunflow.camera(CAMERA_NAME, "pinhole");
  }

  /**
   * Renders home in <code>image</code> at the given <code>camera</code> location and image size.
   * The rendered objects of the home are the ones given in constructor, meaning any change made in 
   * home since the instantiation of this renderer won't be updated. 
   */
  public void render(final BufferedImage image, 
                     Camera camera, 
                     final ImageObserver observer) {
    // Update pinhole camera lens from camera location in parameter
    Point3 eye = new Point3(camera.getX(), camera.getZ(), camera.getY());
    Matrix4 transform;
    float yaw = camera.getYaw();
    float pitch = camera.getPitch();
    double pitchCos = Math.cos(pitch);
    if (Math.abs(pitchCos) > 1E-6) {
      // Set the point the camera is pointed to 
      Point3 target = new Point3(
          camera.getX() - (float)(Math.sin(yaw) * pitchCos), 
          camera.getZ() - (float)Math.sin(pitch), 
          camera.getY() + (float)(Math.cos(yaw) * pitchCos)); 
      Vector3 up = new Vector3(0, 1, 0);              
      transform = Matrix4.lookAt(eye, target, up);
    } else {
      // Compute matrix directly when the camera points is at top
      transform = new Matrix4((float)-Math.cos(yaw), (float)-Math.sin(yaw), 0, camera.getX(), 
          0, 0, 1, camera.getZ(), 
          (float)-Math.sin(yaw), (float)Math.cos(yaw), 0, camera.getY());
    }
    this.sunflow.parameter("transform", transform);
    this.sunflow.parameter("fov", (float)Math.toDegrees(camera.getFieldOfView()));
    this.sunflow.parameter("aspect", (float)image.getWidth() / image.getHeight());
    // Update camera
    this.sunflow.camera(CAMERA_NAME, null);

    // Set image size and quality
    this.sunflow.parameter("resolutionX", image.getWidth());
    this.sunflow.parameter("resolutionY", image.getHeight());
    this.sunflow.parameter("filter", "gaussian"); // box, gaussian, blackman-harris, sinc, mitchell or triangle
    
    if (this.quality == Quality.HIGH) {
      // The bigger aa.max is, the cleanest rendering you get
      this.sunflow.parameter("aa.min", 1);
      this.sunflow.parameter("aa.max",  2);
    } else {
      this.sunflow.parameter("aa.min", 0);
      this.sunflow.parameter("aa.max",  1); 
      this.sunflow.parameter("sampler", "fast"); // ipr, fast or bucket 
    }
    // Render image with default camera
    this.sunflow.parameter("camera", CAMERA_NAME);
    this.sunflow.options(SunflowAPI.DEFAULT_OPTIONS);
    this.sunflow.render(SunflowAPI.DEFAULT_OPTIONS, new BufferedImageDisplay(image, observer));
  }
  
  /**
   * Stops the rendering process.
   */
  public void stop() {
    UI.taskCancel();
  }

  /**
   * Exports the given Java 3D <code>node</code> and its children to Sunflow API.  
   */
  private void exportNode(Node node, 
                          boolean useNormals, boolean noConstantShader) throws IOException {
    exportNode(node, node, useNormals, noConstantShader);
  }

  /**
   * Exports all the 3D shapes children of <code>node</code> at OBJ format.
   */ 
  private void exportNode(Node parent, Node node, 
                          boolean useNormals, 
                          boolean noConstantShader) throws IOException {
    if (node instanceof Group) {
      // Export all children
      Enumeration<?> enumeration = ((Group)node).getAllChildren(); 
      while (enumeration.hasMoreElements()) {
        exportNode(parent, (Node)enumeration.nextElement(), useNormals, noConstantShader);
      }
    } else if (node instanceof Shape3D) {
      Shape3D shape = (Shape3D)node;
      String shapeName = (String)shape.getUserData();
      
      // Retrieve transformation needed to be applied to vertices
      Transform3D transformationToParent = getTransformationToParent(parent, node);

      // Build a unique object name
      String uuid = UUID.randomUUID().toString();

      Appearance appearance = shape.getAppearance();
      String appearanceName = null;
      if (appearance != null) {
        appearanceName = "shader" + uuid;
        boolean mirror = shapeName != null
            && shapeName.startsWith(ModelManager.MIRROR_SHAPE_PREFIX);
        exportAppearance(appearance, appearanceName, mirror, noConstantShader);
      }

      // Export object geometries
      for (int i = 0, n = shape.numGeometries(); i < n; i++) {
        String objectName = "object" + uuid + "-" + i;
        // Always ignore normals on walls
        exportNodeGeometry(shape.getGeometry(i), transformationToParent, objectName, 
            useNormals && !(shape.getParent() instanceof Wall3D));
        if (appearanceName != null) {
          this.sunflow.parameter("shaders", new String [] {appearanceName});
        }
        this.sunflow.instance(objectName + ".instance", objectName);
      }
    }    
  }
  
  /**
   * Returns the transformation applied to a <code>child</code> 
   * on the path to <code>parent</code>. 
   */
  private Transform3D getTransformationToParent(Node parent, Node child) {
    Transform3D transform = new Transform3D();
    if (child instanceof TransformGroup) {
      ((TransformGroup)child).getTransform(transform);
    }
    if (child != parent) {
      Transform3D parentTransform = getTransformationToParent(parent, child.getParent());
      parentTransform.mul(transform);
      return parentTransform;
    } else {
      return transform;
    }
  }
  
  /**
   * Exports a 3D geometry in Sunflow API.
   */
  private void exportNodeGeometry(Geometry geometry, Transform3D transformationToParent, 
                                  String objectName, boolean useNormals) {
    if (geometry instanceof GeometryArray) {
      GeometryArray geometryArray = (GeometryArray)geometry;      
      
      float [] vertices = new float [geometryArray.getVertexCount() * 3];
      
      float [] normals = (geometryArray.getVertexFormat() & GeometryArray.NORMALS) != 0
         ? new float [geometryArray.getVertexCount() * 3]
         : null;
      
      float [] uvs = (geometryArray.getVertexFormat() & GeometryArray.TEXTURE_COORDINATE_2) != 0
         ? new float [geometryArray.getVertexCount() * 2]
         : null;
         
      if ((geometryArray.getVertexFormat() & GeometryArray.BY_REFERENCE) != 0) {
        if ((geometryArray.getVertexFormat() & GeometryArray.INTERLEAVED) != 0) {
          float [] vertexData = geometryArray.getInterleavedVertices();
          int vertexSize = vertexData.length / geometryArray.getVertexCount();
          // Export vertices coordinates 
          for (int index = 0, i = vertexSize - 3, n = geometryArray.getVertexCount(); 
               index < n; index++, i += vertexSize) {
            Point3f vertex = new Point3f(vertexData [i], vertexData [i + 1], vertexData [i + 2]);
            exportVertex(transformationToParent, vertex, index, vertices);
          }
          // Export normals
          if (normals != null) {
            for (int index = 0, i = vertexSize - 6, n = geometryArray.getVertexCount(); 
                 index < n; index++, i += vertexSize) {
              Vector3f normal = new Vector3f(vertexData [i], vertexData [i + 1], vertexData [i + 2]);
              exportNormal(transformationToParent, normal, index, normals);
            }
          }
          // Export texture coordinates
          if (uvs != null) {
            for (int index = 0, i = 0, n = geometryArray.getVertexCount(); 
                  index < n; index++, i += vertexSize) {
              TexCoord2f textureCoordinates = new TexCoord2f(vertexData [i], vertexData [i + 1]);
              exportTextureCoordinates(textureCoordinates, index, uvs);
            }
          }
        } else {
          // Export vertices coordinates
          float [] vertexCoordinates = geometryArray.getCoordRefFloat();
          for (int index = 0, i = 0, n = geometryArray.getVertexCount(); index < n; index++, i += 3) {
            Point3f vertex = new Point3f(vertexCoordinates [i], vertexCoordinates [i + 1], vertexCoordinates [i + 2]);
            exportVertex(transformationToParent, vertex, index, vertices);
          }
          // Export normals
          if (normals != null) {
            float [] normalCoordinates = geometryArray.getNormalRefFloat();
            for (int index = 0, i = 0, n = geometryArray.getVertexCount(); index < n; index++, i += 3) {
              Vector3f normal = new Vector3f(normalCoordinates [i], normalCoordinates [i + 1], normalCoordinates [i + 2]);
              exportNormal(transformationToParent, normal, index, normals);
            }
          }
          // Export texture coordinates
          if (uvs != null) {
            float [] textureCoordinatesArray = geometryArray.getTexCoordRefFloat(0);
            for (int index = 0, i = 0, n = geometryArray.getVertexCount(); index < n; index++, i += 2) {
              TexCoord2f textureCoordinates = new TexCoord2f(textureCoordinatesArray [i], textureCoordinatesArray [i + 1]);
              exportTextureCoordinates(textureCoordinates, index, uvs);
            }
          }
        }
      } else {
        // Export vertices coordinates
        for (int index = 0, n = geometryArray.getVertexCount(); index < n; index++) {
          Point3f vertex = new Point3f();
          geometryArray.getCoordinate(index, vertex);
          exportVertex(transformationToParent, vertex, index, vertices);
        }
        // Export normals
        if (normals != null) {
          for (int index = 0, n = geometryArray.getVertexCount(); index < n; index++) {
            Vector3f normal = new Vector3f();
            geometryArray.getNormal(index, normal);
            exportNormal(transformationToParent, normal, index, normals);
          }
        }
        // Export texture coordinates
        if (uvs != null) {
          for (int index = 0, n = geometryArray.getVertexCount(); index < n; index++) {
            TexCoord2f textureCoordinates = new TexCoord2f();
            geometryArray.getTextureCoordinate(0, index, textureCoordinates);
            exportTextureCoordinates(textureCoordinates, index, uvs);
          }
        }
      }

      int [] triangles = null;
      
      // Export triangles or quadrilaterals depending on the geometry
      if (geometryArray instanceof IndexedGeometryArray) {
        if (geometryArray instanceof IndexedTriangleArray) {
          IndexedTriangleArray triangleArray = (IndexedTriangleArray)geometryArray;
          triangles = new int [triangleArray.getIndexCount()];
          for (int i = 0, n = triangleArray.getIndexCount(); i < n; i += 3) {
            exportIndexedTriangle(triangleArray, i, i + 1, i + 2, triangles, i);
          }
        } else if (geometryArray instanceof IndexedQuadArray) {
          IndexedQuadArray quadArray = (IndexedQuadArray)geometryArray;
          triangles = new int [quadArray.getIndexCount() * 3 / 2];
          for (int i = 0, n = quadArray.getIndexCount(); i < n; i += 4) {
            exportIndexedTriangle(quadArray, i, i + 1, i + 2, triangles, i * 3 / 2);
            exportIndexedTriangle(quadArray, i, i + 2, i + 3, triangles, i * 3 / 2 + 3);
          }
        } else if (geometryArray instanceof IndexedTriangleStripArray) {
          IndexedTriangleStripArray triangleStripArray = (IndexedTriangleStripArray)geometryArray;
          int [] stripVertexCount = new int [triangleStripArray.getNumStrips()];
          triangleStripArray.getStripIndexCounts(stripVertexCount);
          triangles = new int [getTriangleCount(stripVertexCount) * 3];
          for (int initialIndex = 0, triangleIndex = 0, strip = 0; strip < stripVertexCount.length; strip++) {
            for (int i = initialIndex, n = initialIndex + stripVertexCount [strip] - 2, j = 0; 
                 i < n; i++, j++, triangleIndex += 3) {
              if (j % 2 == 0) {
                exportIndexedTriangle(triangleStripArray, i, i + 1, i + 2, triangles, triangleIndex);
              } else { // Vertices of odd triangles are in reverse order               
                exportIndexedTriangle(triangleStripArray, i, i + 2, i + 1, triangles, triangleIndex);
              }
            }
            initialIndex += stripVertexCount [strip];
          }
        } else if (geometryArray instanceof IndexedTriangleFanArray) {
          IndexedTriangleFanArray triangleFanArray = (IndexedTriangleFanArray)geometryArray;
          int [] stripVertexCount = new int [triangleFanArray.getNumStrips()];
          triangleFanArray.getStripIndexCounts(stripVertexCount);
          triangles = new int [getTriangleCount(stripVertexCount) * 3];
          for (int initialIndex = 0, triangleIndex = 0, strip = 0; strip < stripVertexCount.length; strip++) {
            for (int i = initialIndex, n = initialIndex + stripVertexCount [strip] - 2; 
                 i < n; i++, triangleIndex += 3) {
              exportIndexedTriangle(triangleFanArray, initialIndex, i + 1, i + 2, triangles, triangleIndex);
            }
            initialIndex += stripVertexCount [strip];
          }
        } 
      } else {
        if (geometryArray instanceof TriangleArray) {
          TriangleArray triangleArray = (TriangleArray)geometryArray;
          triangles = new int [triangleArray.getVertexCount()];
          for (int i = 0, n = triangleArray.getVertexCount(); i < n; i += 3) {
            exportTriangle(triangleArray, i, i + 1, i + 2, triangles, i);
          }
        } else if (geometryArray instanceof QuadArray) {
          QuadArray quadArray = (QuadArray)geometryArray;
          triangles = new int [quadArray.getVertexCount() * 3 / 2];
          for (int i = 0, n = quadArray.getVertexCount(); i < n; i += 4) {
            exportTriangle(quadArray, i, i + 1, i + 2, triangles, i * 3 / 2);
            exportTriangle(quadArray, i + 2, i + 3, i, triangles, i * 3 / 2 + 3);
          }
        } else if (geometryArray instanceof TriangleStripArray) {
          TriangleStripArray triangleStripArray = (TriangleStripArray)geometryArray;
          int [] stripVertexCount = new int [triangleStripArray.getNumStrips()];
          triangleStripArray.getStripVertexCounts(stripVertexCount);
          triangles = new int [getTriangleCount(stripVertexCount) * 3];
          for (int initialIndex = 0, triangleIndex = 0, strip = 0; strip < stripVertexCount.length; strip++) {
            for (int i = initialIndex, n = initialIndex + stripVertexCount [strip] - 2, j = 0; 
                 i < n; i++, j++, triangleIndex += 3) {
              if (j % 2 == 0) {
                exportTriangle(triangleStripArray, i, i + 1, i + 2, triangles, triangleIndex);
              } else { // Vertices of odd triangles are in reverse order               
                exportTriangle(triangleStripArray, i, i + 2, i + 1, triangles, triangleIndex);
              }
            }
            initialIndex += stripVertexCount [strip];
          }
        } else if (geometryArray instanceof TriangleFanArray) {
          TriangleFanArray triangleFanArray = (TriangleFanArray)geometryArray;
          int [] stripVertexCount = new int [triangleFanArray.getNumStrips()];
          triangleFanArray.getStripVertexCounts(stripVertexCount);
          triangles = new int [getTriangleCount(stripVertexCount) * 3];
          for (int initialIndex = 0, triangleIndex = 0, strip = 0; strip < stripVertexCount.length; strip++) {
            for (int i = initialIndex, n = initialIndex + stripVertexCount [strip] - 2; 
                 i < n; i++, triangleIndex += 3) {
              exportTriangle(triangleFanArray, initialIndex, i + 1, i + 2, triangles, triangleIndex);
            }
            initialIndex += stripVertexCount [strip];
          }
        }
      }
      
      if (triangles != null) {
        this.sunflow.parameter("triangles", triangles);
        this.sunflow.parameter("points", "point", "vertex", vertices);
        if (useNormals && normals != null) {
          this.sunflow.parameter("normals", "vector", "vertex", normals);
        }
        if (uvs != null) {
          this.sunflow.parameter("uvs", "texcoord", "vertex", uvs);
        }
        this.sunflow.geometry(objectName, "triangle_mesh");
      }
    } 
  }
  
  /**
   * Returns the sum of the integers in <code>stripVertexCount</code> array.
   */
  private int getTriangleCount(int [] stripVertexCount) {
    int triangleCount = 0;
    for (int strip = 0; strip < stripVertexCount.length; strip++) {
      triangleCount += stripVertexCount [strip] - 2;
    }
    return triangleCount;
  }

  /**
   * Applies to <code>vertex</code> the given transformation, and stores it in <code>vertices</code>.  
   */
  private void exportVertex(Transform3D transformationToParent,
                            Point3f vertex, int index,
                            float [] vertices) {
    transformationToParent.transform(vertex);
    index *= 3;
    vertices [index++] = vertex.x;
    vertices [index++] = vertex.y;
    vertices [index++] = vertex.z;
  }

  /**
   * Applies to <code>normal</code> the given transformation, and stores it in <code>normals</code>.  
   */
  private void exportNormal(Transform3D transformationToParent,
                            Vector3f normal, int index,
                            float [] normals) {
    transformationToParent.transform(normal);
    index *= 3;
    normals [index++] = normal.x;
    normals [index++] = normal.y;
    normals [index++] = normal.z;
  }

  /**
   * Stores <code>textureCoordinates</code> in <code>uvs</code>.  
   */
  private void exportTextureCoordinates(TexCoord2f textureCoordinates, int index,
                                       float [] uvs) {
    index *= 2;
    uvs [index++] = textureCoordinates.x;
    uvs [index++] = textureCoordinates.y;
  }

  /**
   * Stores in <code>triangles</code> the indices given at vertexIndex1, vertexIndex2, vertexIndex3. 
   */
  private void exportIndexedTriangle(IndexedGeometryArray geometryArray, 
                                     int vertexIndex1, int vertexIndex2, int vertexIndex3,
                                     int [] triangles, int index) {
    triangles [index++] = geometryArray.getCoordinateIndex(vertexIndex1);
    triangles [index++] = geometryArray.getCoordinateIndex(vertexIndex2);
    triangles [index++] = geometryArray.getCoordinateIndex(vertexIndex3);
  }
    
  /**
   * Stores in <code>triangles</code> the indices vertexIndex1, vertexIndex2, vertexIndex3. 
   */
  private void exportTriangle(GeometryArray geometryArray, 
                              int vertexIndex1, int vertexIndex2, int vertexIndex3,
                              int [] triangles, int index) {
    triangles [index++] = vertexIndex1;
    triangles [index++] = vertexIndex2;
    triangles [index++] = vertexIndex3;
  }
    
  /**
   * Stores an appearance as a Sunflow shader.  
   */
  private void exportAppearance(Appearance appearance,
                                String appearanceName, 
                                boolean mirror,
                                boolean noConstantShader) throws IOException {
    Texture texture = appearance.getTexture();    
    if (mirror) {
      Material material = appearance.getMaterial();
      if (material != null) {
        Color3f color = new Color3f();
        material.getDiffuseColor(color);
        this.sunflow.parameter("color", null, new float [] {color.x, color.y, color.z});
      }
      this.sunflow.shader(appearanceName, "mirror");
    } else if (texture != null) {
      File imageFile = this.textureImageFilesCache.get(texture);
      if (imageFile == null) {
        ImageComponent2D imageComponent = (ImageComponent2D)texture.getImage(0);
        RenderedImage image = imageComponent.getRenderedImage();
        imageFile = File.createTempFile("texture", ".jpg");
        imageFile.deleteOnExit();
        ImageIO.write(image, "JPEG", imageFile);
        this.textureImageFilesCache.put(texture, imageFile);
      }
      this.sunflow.parameter("texture", imageFile.getAbsolutePath());
        
      Material material = appearance.getMaterial();
      if (material != null
          && material.getShininess() > 64) {
        this.sunflow.parameter("shiny", material.getShininess() / 512f);
        this.sunflow.shader(appearanceName, "textured_shiny_diffuse");
      } else {
        this.sunflow.shader(appearanceName, "textured_diffuse");
      }
    } else {
      Material material = appearance.getMaterial();
      if (material != null) {
        Color3f color = new Color3f();
        material.getDiffuseColor(color);
        float [] diffuseColor = new float [] {color.x, color.y, color.z};

        TransparencyAttributes transparencyAttributes = appearance.getTransparencyAttributes();
        if (transparencyAttributes != null
            && transparencyAttributes.getTransparency() > 0) {
          if (material instanceof OBJMaterial
              && ((OBJMaterial)material).isOpticalDensitySet()) {
            this.sunflow.parameter("eta", ((OBJMaterial)material).getOpticalDensity());
          } else {
            // Use glass ETA as default
            this.sunflow.parameter("eta", 1.55f);
          }
          // Increase color to render better transparent objects
          this.sunflow.parameter("color", null,
              new float [] {Math.min(diffuseColor [0] * 2f, 1f), Math.min(diffuseColor [1] * 2f, 1f), Math.min(diffuseColor [2] * 2f, 1f)});
          this.sunflow.parameter("absorbtion.distance", 0f);          
          float transparency = transparencyAttributes.getTransparency();
          this.sunflow.parameter("absorbtion.color", null, new float [] {transparency, transparency, transparency}); 
          this.sunflow.shader(appearanceName, "glass");
        } else {  
          this.sunflow.parameter("diffuse", null, diffuseColor);
          float shininess = material.getShininess();
          if (shininess > 64) {
            this.sunflow.parameter("shiny", shininess / 256f);
            this.sunflow.shader(appearanceName, "shiny_diffuse");
          } else {
            this.sunflow.shader(appearanceName, "diffuse");
          }
        }
      } else {
        ColoringAttributes coloringAttributes = appearance.getColoringAttributes();
        if (coloringAttributes != null) {
          Color3f color = new Color3f();
          coloringAttributes.getColor(color);
          if (noConstantShader) {
            this.sunflow.parameter("diffuse", null, new float [] {color.x, color.y, color.z});
            this.sunflow.shader(appearanceName, "diffuse");
          } else {
            this.sunflow.parameter("color", null, new float [] {color.x, color.y, color.z});
            this.sunflow.shader(appearanceName, "constant");
          }
        }
      }
    }
  }

  /**
   * A SunFlow display that updates an existing image.
   * Implementation mostly copied from org.sunflow.system.ImagePanel.
   */
  private static final class BufferedImageDisplay implements Display {
    private static final int BASE_INFO_FLAGS = ImageObserver.WIDTH | ImageObserver.HEIGHT | ImageObserver.PROPERTIES;
    private static final int [] BORDERS = {Color.RED.toRGB(), Color.GREEN.toRGB(), Color.BLUE.toRGB(), 
                                           Color.YELLOW.toRGB(), Color.CYAN.toRGB(), Color.MAGENTA.toRGB()};
    
    private final ImageObserver observer;
    private final BufferedImage image;

    private BufferedImageDisplay(BufferedImage image, ImageObserver observer) {
      this.observer = observer;
      this.image = image;
    }

    public synchronized void imageBegin(int width, int height, int bucketSize) {
      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          int rgba = this.image.getRGB(x, y);
          this.image.setRGB(x, y, ((rgba & 0xFEFEFEFE) >>> 1) + ((rgba & 0xFCFCFCFC) >>> 2));
        }
      }
      notifyObserver(ImageObserver.FRAMEBITS | BASE_INFO_FLAGS, 0, 0, width, height);
    }

    public synchronized void imagePrepare(int x, int y, int width, int height, int id) {
      int border = BORDERS [id % BORDERS.length] | 0xFF000000;
      for (int by = 0; by < height; by++) {
        for (int bx = 0; bx < width; bx++) {
          if (bx < 2 || bx > width - 3) {
            if (5 * by < height || 5 * (height - by - 1) < height) {
              this.image.setRGB(x + bx, y + by, border);
            }
          } else if (by < 2 || by > height - 3) {
            if (5 * bx < width || 5 * (width - bx - 1) < width) {
              this.image.setRGB(x + bx, y + by, border);
            }
          }
        }
      }
      notifyObserver(ImageObserver.SOMEBITS | BASE_INFO_FLAGS, x, y, width, height);
    }

    public synchronized void imageUpdate(int x, int y, int width, int height, Color [] data, float [] alpha) {
      for (int j = 0, index = 0; j < height; j++) {
        for (int i = 0; i < width; i++, index++) {
          this.image.setRGB(x + i, y + j, 
              data [index].copy().mul(1.0f / alpha [index]).toNonLinear().toRGBA(alpha [index]));
        }
      }
      notifyObserver(ImageObserver.SOMEBITS | BASE_INFO_FLAGS, x, y, width, height);
    }

    public synchronized void imageFill(int x, int y, int width, int height, Color c, float alpha) {
      int rgba = c.copy().mul(1.0f / alpha).toNonLinear().toRGBA(alpha);
      for (int j = 0, index = 0; j < height; j++) {
        for (int i = 0; i < width; i++, index++) {
          this.image.setRGB(x + i, y + j, rgba);
        }
      }
      notifyObserver(ImageObserver.SOMEBITS | BASE_INFO_FLAGS, x, y, width, height);
    }

    public void imageEnd() {
      notifyObserver(ImageObserver.FRAMEBITS | BASE_INFO_FLAGS, 
            0, 0, this.image.getWidth(), this.image.getHeight());
    }

    private void notifyObserver(final int flags, final int x, final int y, final int width, final int height) {
      EventQueue.invokeLater(new Runnable() {
          public void run() {
            if (observer != null) {
              observer.imageUpdate(image, flags, x, y, width, height);
            }
          }
        });
    }
  }

  /**
   * A SunFlow sphere light with no representation.
   */
  public static class SphereLightWithNoRepresentation extends SphereLight {
    public Instance createInstance() {
      return null;
    }
  }
}
