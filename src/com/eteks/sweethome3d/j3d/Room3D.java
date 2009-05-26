/*
 * Room3D.java 23 jan. 09
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

import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import javax.vecmath.TexCoord2f;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.Wall;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;

/**
 * Root of room branch.
 */
public class Room3D extends Object3DBranch {
  private static final Material DEFAULT_MATERIAL = new Material();
  private static final Map<Integer, Material> materials = new HashMap<Integer, Material>();
  
  static {
    DEFAULT_MATERIAL.setCapability(Material.ALLOW_COMPONENT_READ);
  }
  
  private static final int FLOOR_PART  = 0;
  private static final int CEILING_PART = 1;
  
  private final Home home;

  /**
   * Creates the 3D room matching the given home <code>room</code>.
   */
  public Room3D(Room room, Home home) {
    this(room, home, false, false, false);
  }

  /**
   * Creates the 3D room matching the given home <code>room</code>.
   */
  public Room3D(Room room, Home home,
                boolean ignoreCeilingPart, 
                boolean ignoreInvisiblePart,
                boolean waitTextureLoadingEnd) {
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
    updateRoomAppearance(waitTextureLoadingEnd);
    
    if (ignoreCeilingPart
        || (ignoreInvisiblePart
            && !room.isCeilingVisible())) {
      removeChild(CEILING_PART);
    }
    if (ignoreInvisiblePart
        && !room.isFloorVisible()) {
      removeChild(FLOOR_PART);
    }
  }

  /**
   * Returns a new room part shape with no geometry  
   * and a default appearance with a white material.
   */
  private Node createRoomPartShape() {
    Shape3D roomShape = new Shape3D();
    // Allow room shape to change its geometry
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
    // Mix texture and room color
    TextureAttributes textureAttributes = new TextureAttributes ();
    textureAttributes.setTextureMode(TextureAttributes.MODULATE);
    roomAppearance.setTextureAttributes(textureAttributes);
    
    return roomShape;
  }

  @Override
  public void update() {
    updateRoomGeometry();
    updateRoomAppearance(false);
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
   * Returns an array that cites <code>points</code> in reverse order.
   */
  private float [][] getReversedArray(float [][] points) {
    List<float []> pointList = Arrays.asList(points);
    Collections.reverse(pointList);
    points = pointList.toArray(points);
    return points;
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
  private void updateRoomAppearance(boolean waitTextureLoadingEnd) {
    Room room = (Room)getUserData();
    updateRoomSideAppearance(((Shape3D)getChild(FLOOR_PART)).getAppearance(), 
        room.getFloorTexture(), waitTextureLoadingEnd, room.getFloorColor(), room.isFloorVisible());
    updateRoomSideAppearance(((Shape3D)getChild(CEILING_PART)).getAppearance(), 
        room.getCeilingTexture(), waitTextureLoadingEnd, room.getCeilingColor(), room.isCeilingVisible());
  }
  
  /**
   * Sets room side appearance with its color, texture and visibility.
   */
  private void updateRoomSideAppearance(final Appearance roomSideAppearance, 
                                        final HomeTexture roomSideTexture,
                                        boolean waitTextureLoadingEnd,
                                        Integer roomSideColor,
                                        boolean visible) {
    if (roomSideTexture == null) {
      roomSideAppearance.setMaterial(getMaterial(roomSideColor));
      roomSideAppearance.setTexture(null);
    } else {
      // Update material and texture of room side
      roomSideAppearance.setMaterial(DEFAULT_MATERIAL);
      final TextureManager textureManager = TextureManager.getInstance();
      textureManager.loadTexture(roomSideTexture.getImage(), waitTextureLoadingEnd,
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