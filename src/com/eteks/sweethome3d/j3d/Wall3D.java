/*
 * Wall3D.java 23 jan. 09
 *
 * Copyright (c) 2007-2009 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Geometry;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Texture;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import javax.vecmath.TexCoord2f;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeEnvironment;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.Wall;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;

/**
 * Root of wall branch.
 */
public class Wall3D extends Object3DBranch {
  private static final Material DEFAULT_MATERIAL = new Material();
  private static final Map<Integer, Material> materials = new HashMap<Integer, Material>();
  
  static {
    DEFAULT_MATERIAL.setCapability(Material.ALLOW_COMPONENT_READ);
  }
  
  private static final int LEFT_WALL_SIDE  = 0;
  private static final int RIGHT_WALL_SIDE = 1;
  
  private final Home home;

  /**
   * Creates the 3D wall matching the given home <code>wall</code>.
   */
  public Wall3D(Wall wall, Home home) {
    this(wall, home, false, false);
  }
  
  /**
   * Creates the 3D wall matching the given home <code>wall</code>.
   */
  public Wall3D(Wall wall, Home home, boolean ignoreDrawingMode, 
                boolean waitTextureLoadingEnd) {
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
    updateWallAppearance(waitTextureLoadingEnd);
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
      wallAppearance.setColoringAttributes(Object3DBranch.OUTLINE_COLORING_ATTRIBUTES);
      wallAppearance.setPolygonAttributes(Object3DBranch.OUTLINE_POLYGON_ATTRIBUTES);
      wallAppearance.setLineAttributes(Object3DBranch.OUTLINE_LINE_ATTRIBUTES);
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
    updateWallAppearance(false);
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
    Shape wallShape = getShape(getWallSidePoints(wallSide));
    Area wallArea = new Area(wallShape);
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
    Map<HomePieceOfFurniture, Area> windowIntersections = new HashMap<HomePieceOfFurniture, Area>();
    for (HomePieceOfFurniture piece : this.home.getFurniture()) {
      if (piece.isDoorOrWindow() 
          && piece.getElevation() < maxWallHeight) {
        Shape pieceShape = getShape(piece.getPoints());
        Area pieceArea = new Area(pieceShape);
        Area intersectionArea = new Area(wallShape);
        intersectionArea.intersect(pieceArea);
        if (!intersectionArea.isEmpty()) {
          windowIntersections.put(piece, intersectionArea);
          // Remove from wall area the piece shape
          wallArea.subtract(pieceArea);
        }
      }
    }
    List<Geometry> wallGeometries = new ArrayList<Geometry>();
    List<float[]> wallPoints = new ArrayList<float[]>(4);
    // Generate geometry for each wall part that doesn't contain a window
    float [] previousWallPoint = null;
    for (PathIterator it = wallArea.getPathIterator(null, 0.1f); !it.isDone(); ) {
      float [] wallPoint = new float[2];
      if (it.currentSegment(wallPoint) == PathIterator.SEG_CLOSE) {
        if (wallPoints.size() > 2) {
          // Remove last point if it's equal to first point
          if (Arrays.equals(wallPoints.get(0), wallPoints.get(wallPoints.size() - 1))) {
            wallPoints.remove(wallPoints.size() - 1);
          }
          if (wallPoints.size() > 2) {
            float [][] wallPartPoints = wallPoints.toArray(new float[wallPoints.size()][]);
            // Compute geometry for vertical part
            wallGeometries.add(createWallVerticalPartGeometry(wallPartPoints, 0, 
                cosWallYawAngle, sinWallYawAngle, topLineAlpha, topLineBeta, texture));
            // Compute geometry for bottom part
            wallGeometries.add(createWallHorizontalPartGeometry(wallPartPoints, 0));
            // Compute geometry for top part
            wallGeometries.add(createWallTopPartGeometry(wallPartPoints, 
                cosWallYawAngle, sinWallYawAngle, topLineAlpha, topLineBeta));
          }
        }
        wallPoints.clear();
        previousWallPoint = null;
      } else if (previousWallPoint == null
                 || !Arrays.equals(wallPoint, previousWallPoint)) {
        wallPoints.add(wallPoint);
        previousWallPoint = wallPoint;
      }
      it.next();
    }
    
    // Generate geometry for each wall part above and below a window
    previousWallPoint = null;
    for (Map.Entry<HomePieceOfFurniture, Area> windowIntersection : windowIntersections.entrySet()) {
      for (PathIterator it = windowIntersection.getValue().getPathIterator(null, 0.1f); !it.isDone(); ) {
        float [] wallPoint = new float[2];
        if (it.currentSegment(wallPoint) == PathIterator.SEG_CLOSE) {
          if (wallPoints.size() > 2) {
            // Remove last point if it's equal to first point
            if (Arrays.equals(wallPoints.get(0), wallPoints.get(wallPoints.size() - 1))) {
              wallPoints.remove(wallPoints.size() - 1);
            }
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
          }
          wallPoints.clear();
          previousWallPoint = null;
        } else if (previousWallPoint == null
                   || !Arrays.equals(wallPoint, previousWallPoint)) {
          wallPoints.add(wallPoint);
          previousWallPoint = wallPoint;
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
   * and spread from <code>yMin</code> to a top line (y = ax + b) described by <code>topLineAlpha</code>
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
  private void updateWallAppearance(boolean waitTextureLoadingEnd) {
    Wall wall = (Wall)getUserData();
    updateFilledWallSideAppearance(((Shape3D)getChild(LEFT_WALL_SIDE)).getAppearance(), 
        wall.getLeftSideTexture(), waitTextureLoadingEnd, wall.getLeftSideColor());
    updateFilledWallSideAppearance(((Shape3D)getChild(RIGHT_WALL_SIDE)).getAppearance(), 
        wall.getRightSideTexture(), waitTextureLoadingEnd, wall.getRightSideColor());
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
                                              boolean waitTextureLoadingEnd,
                                              Integer wallSideColor) {
    if (wallSideTexture == null) {
      wallSideAppearance.setMaterial(getMaterial(wallSideColor));
      wallSideAppearance.setTexture(null);
    } else {
      // Update material and texture of wall side
      wallSideAppearance.setMaterial(DEFAULT_MATERIAL);
      final TextureManager textureManager = TextureManager.getInstance();
      textureManager.loadTexture(wallSideTexture.getImage(), waitTextureLoadingEnd,
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