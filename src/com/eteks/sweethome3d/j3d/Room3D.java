/*
 * Room3D.java 23 jan. 09
 *
 * Sweet Home 3D, Copyright (c) 2007-2009 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Geometry;
import javax.media.j3d.Node;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Texture;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Point3f;
import javax.vecmath.TexCoord2f;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeFurnitureGroup;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.Level;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.Wall;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;

/**
 * Root of room branch.
 */
public class Room3D extends Object3DBranch {
  private static final TextureAttributes MODULATE_TEXTURE_ATTRIBUTES = new TextureAttributes();
  
  static {
    MODULATE_TEXTURE_ATTRIBUTES.setTextureMode(TextureAttributes.MODULATE);
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
    roomAppearance.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_READ);
    TransparencyAttributes transparencyAttributes = new TransparencyAttributes();
    transparencyAttributes.setCapability(TransparencyAttributes.ALLOW_VALUE_WRITE);
    transparencyAttributes.setCapability(TransparencyAttributes.ALLOW_MODE_WRITE);
    roomAppearance.setTransparencyAttributes(transparencyAttributes);
    roomAppearance.setCapability(Appearance.ALLOW_RENDERING_ATTRIBUTES_READ);
    RenderingAttributes renderingAttributes = new RenderingAttributes();
    renderingAttributes.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
    roomAppearance.setRenderingAttributes(renderingAttributes);
    roomAppearance.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
    roomAppearance.setMaterial(DEFAULT_MATERIAL);      
    roomAppearance.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
    roomAppearance.setCapability(Appearance.ALLOW_TEXTURE_READ);
    // Mix texture and room color
    roomAppearance.setTextureAttributes(MODULATE_TEXTURE_ATTRIBUTES);
    
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
    updateRoomPartGeometry(FLOOR_PART, ((Room)getUserData()).getFloorTexture());
    updateRoomPartGeometry(CEILING_PART, ((Room)getUserData()).getCeilingTexture());
  }
  
  private void updateRoomPartGeometry(int roomPart, HomeTexture texture) {
    Shape3D roomShape = (Shape3D)getChild(roomPart);
    int currentGeometriesCount = roomShape.numGeometries();
    Room room = (Room)getUserData();
    if (room.getLevel() == null || room.getLevel().isVisible()) {
      for (Geometry roomGeometry : createRoomGeometries(roomPart, texture)) {
        roomShape.addGeometry(roomGeometry);
      }
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
    if ((roomPart == FLOOR_PART && room.isFloorVisible()
         || roomPart == CEILING_PART && room.isCeilingVisible())
        && points.length > 2) {
      Level roomLevel = room.getLevel();
      List<Level> levels = this.home.getLevels();
      boolean lastLevel = isLastLevel(roomLevel, levels);
      float floorBottomElevation;
      float roomElevation;
      if (roomLevel != null) {
        roomElevation = roomLevel.getElevation();
        floorBottomElevation = roomElevation - roomLevel.getFloorThickness();
      } else {
        roomElevation = 0;
        floorBottomElevation = 0;
      }

      float firstLevelElevation;
      if (levels.size() == 0) {
        firstLevelElevation = 0;
      } else {
        firstLevelElevation = levels.get(0).getElevation();
      }
      boolean floorBottomVisible = roomPart == FLOOR_PART 
          && roomLevel != null 
          && roomElevation != firstLevelElevation;

      // Find rooms at the same elevation 
      // and room ceilings at same elevation as the floor bottom  
      List<Room> roomsAtSameElevation = new ArrayList<Room>();
      List<Room> ceilingsAtSameFloorBottomElevation = new ArrayList<Room>();
      for (Room homeRoom : this.home.getRooms()) {
        Level homeRoomLevel = homeRoom.getLevel();
        if (room == homeRoom // Store also the room itself to know its order among rooms at same elevation
            || roomLevel == homeRoomLevel
                && (roomPart == FLOOR_PART && homeRoom.isFloorVisible()
                    || roomPart == CEILING_PART && homeRoom.isCeilingVisible()) 
            || roomLevel != null 
                && homeRoomLevel != null 
                && (roomPart == FLOOR_PART 
                        && homeRoom.isFloorVisible()
                        && Math.abs(roomElevation - homeRoomLevel.getElevation()) < 1E-4
                    || roomPart == CEILING_PART 
                        && homeRoom.isCeilingVisible()
                        && !lastLevel
                        && !isLastLevel(homeRoomLevel, levels)
                        && Math.abs(roomElevation + roomLevel.getHeight() - (homeRoomLevel.getElevation() + homeRoomLevel.getHeight())) < 1E-4)) {         
          roomsAtSameElevation.add(homeRoom);
        } else if (floorBottomVisible 
                    && homeRoomLevel != null 
                    && homeRoom.isCeilingVisible() 
                    && !isLastLevel(homeRoomLevel, levels) 
                    && Math.abs(floorBottomElevation - (homeRoomLevel.getElevation() + homeRoomLevel.getHeight())) < 1E-4) {
          ceilingsAtSameFloorBottomElevation.add(homeRoom);
        }
      }
      
      List<HomePieceOfFurniture> visibleStaircases;
      if (roomLevel == null
          || roomPart == CEILING_PART
              && lastLevel) {
        visibleStaircases = Collections.emptyList();
      } else {
        visibleStaircases = getVisibleStaircases(this.home.getFurniture(), roomPart, roomLevel, 
            roomLevel.getElevation() == firstLevelElevation);
      }

      // Check ceiling points of the last level are at the same elevation
      boolean sameElevation = true;
      if (roomPart == CEILING_PART
          && (roomLevel == null || lastLevel)) {
        float firstPointElevation = getRoomHeightAt(points [0][0], points [0][1]);
        for (int i = 1; i < points.length && sameElevation; i++) {
          sameElevation = getRoomHeightAt(points [i][0], points [i][1]) == firstPointElevation;
        }
      }
      
      // Retrieve room points
      List<float [][]> roomPoints;
      List<float [][]> roomHoles;
      List<float [][]> roomPointsWithoutHoles;
      Area roomVisibleArea;
      // If room isn't singular retrieve all the points of its different polygons 
      if (!room.isSingular() 
          || sameElevation
              && (roomsAtSameElevation.get(roomsAtSameElevation.size() - 1) != room
                  || visibleStaircases.size() > 0)) {        
        roomVisibleArea = new Area(getShape(points));
        if (roomsAtSameElevation.contains(room)) {
          // Remove other rooms surface that may overlap the current room
          for (int i = roomsAtSameElevation.size() - 1; i > 0 && roomsAtSameElevation.get(i) != room; i--) {
            Room otherRoom = roomsAtSameElevation.get(i);
            roomVisibleArea.subtract(new Area(getShape(otherRoom.getPoints())));
          }
        }        
        removeStaircasesFromArea(visibleStaircases, roomVisibleArea);
        roomPoints = new ArrayList<float[][]>();
        roomHoles = new ArrayList<float[][]>();
        roomPointsWithoutHoles = getAreaPoints(roomVisibleArea, roomPoints, roomHoles, 1, roomPart == CEILING_PART);
      } else {
        boolean clockwise = room.isClockwise();
        if (clockwise && roomPart == FLOOR_PART
            || !clockwise && roomPart == CEILING_PART) {
          // Reverse points order 
          points = getReversedArray(points);
        }
        roomPointsWithoutHoles = 
        roomPoints = Arrays.asList(new float [][][] {points});
        roomHoles = Collections.emptyList();
        roomVisibleArea = null;
      }
      
      List<Geometry> geometries = new ArrayList<Geometry> (3);      
      final float subpartSize = this.home.getEnvironment().getSubpartSizeUnderLight();
      
      if (!roomPointsWithoutHoles.isEmpty()) {
        List<float []> roomPointElevations = new ArrayList<float[]>();
        boolean roomAtSameElevation = true;
        for (int i = 0; i < roomPointsWithoutHoles.size(); i++) {
          float [][] roomPartPoints = roomPointsWithoutHoles.get(i);
          float [] roomPartPointElevations = new float [roomPartPoints.length];
          for (int j = 0; j < roomPartPoints.length; j++) {
            roomPartPointElevations [j] = roomPart == FLOOR_PART 
                ? roomElevation 
                : getRoomHeightAt(roomPartPoints [j][0], roomPartPoints [j][1]);
            if (roomAtSameElevation && j > 0) {
              roomAtSameElevation = roomPartPointElevations [j] == roomPartPointElevations [j - 1];
            }
          }
          roomPointElevations.add(roomPartPointElevations);
        }

        // Compute room geometry
        if (roomAtSameElevation && subpartSize > 0) {
          for (int i = 0; i < roomPointsWithoutHoles.size(); i++) {
            float [][] roomPartPoints = roomPointsWithoutHoles.get(i);
            // Subdivide area in smaller squares to ensure a smoother effect with point lights         
            float xMin = Float.MAX_VALUE;
            float xMax = Float.MIN_VALUE;
            float zMin = Float.MAX_VALUE;
            float zMax = Float.MIN_VALUE;
            for (float [] point : roomPartPoints) {
              xMin = Math.min(xMin, point [0]);
              xMax = Math.max(xMax, point [0]);
              zMin = Math.min(zMin, point [1]);
              zMax = Math.max(zMax, point [1]);
            }
            
            Area roomPartArea = new Area(getShape(roomPartPoints));        
            for (float xSquare = xMin; xSquare < xMax; xSquare += subpartSize) {
              for (float zSquare = zMin; zSquare < zMax; zSquare += subpartSize) {
                Area roomPartSquare = new Area(new Rectangle2D.Float(xSquare, zSquare, subpartSize, subpartSize));
                roomPartSquare.intersect(roomPartArea);
                if (!roomPartSquare.isEmpty()) {
                  List<float [][]> geometryPartPointsWithoutHoles = 
                      getAreaPoints(roomPartSquare, 1, roomPart == CEILING_PART);
                  if (!geometryPartPointsWithoutHoles.isEmpty()) {
                    geometries.add(computeRoomPartGeometry(geometryPartPointsWithoutHoles, 
                        null, roomLevel, roomPointElevations.get(i) [0], floorBottomElevation, 
                        roomPart == FLOOR_PART, false, texture));
                  }
                }
              }
            }
          }
        } else {
          geometries.add(computeRoomPartGeometry(roomPointsWithoutHoles, roomPointElevations, roomLevel,
              roomElevation, floorBottomElevation, roomPart == FLOOR_PART, false, texture));
        }
          
        // Compute border geometry
        if (roomLevel != null
            && roomPart == FLOOR_PART 
            && roomLevel.getElevation() != firstLevelElevation) {
          geometries.add(computeRoomBorderGeometry(roomPoints, roomHoles, roomLevel, roomElevation, texture));
        }
      }

      // Retrieve points of the room floor bottom
      if (floorBottomVisible) {
        List<float [][]> floorBottomPointsWithoutHoles;
        if (roomVisibleArea != null 
            || ceilingsAtSameFloorBottomElevation.size() > 0) {        
          Area floorBottomVisibleArea = roomVisibleArea != null ? roomVisibleArea : new Area(getShape(points));
          // Remove other rooms surface that may overlap the floor bottom
          for (Room otherRoom : ceilingsAtSameFloorBottomElevation) {
           floorBottomVisibleArea.subtract(new Area(getShape(otherRoom.getPoints())));
          }          
          floorBottomPointsWithoutHoles = getAreaPoints(floorBottomVisibleArea, 1, true);
        } else {
          floorBottomPointsWithoutHoles = Arrays.asList(new float [][][] {getReversedArray(points)});
        }
        
        if (!floorBottomPointsWithoutHoles.isEmpty()) {
          // Compute floor bottom geometry
          if (subpartSize > 0) {
            for (int i = 0 ; i < floorBottomPointsWithoutHoles.size(); i++) {
              float [][] floorBottomPartPoints = floorBottomPointsWithoutHoles.get(i);
              float xMin = Float.MAX_VALUE;
              float xMax = Float.MIN_VALUE;
              float zMin = Float.MAX_VALUE;
              float zMax = Float.MIN_VALUE;
              for (float [] point : floorBottomPartPoints) {
                xMin = Math.min(xMin, point [0]);
                xMax = Math.max(xMax, point [0]);
                zMin = Math.min(zMin, point [1]);
                zMax = Math.max(zMax, point [1]);
              }
              
              Area floorBottomPartArea = new Area(getShape(floorBottomPartPoints));
              for (float xSquare = xMin; xSquare < xMax; xSquare += subpartSize) {
                for (float zSquare = zMin; zSquare < zMax; zSquare += subpartSize) {
                  Area floorBottomPartSquare = new Area(new Rectangle2D.Float(xSquare, zSquare, subpartSize, subpartSize));
                  floorBottomPartSquare.intersect(floorBottomPartArea);
                  if (!floorBottomPartSquare.isEmpty()) {
                    List<float [][]> geometryPartPointsWithoutHoles = getAreaPoints(floorBottomPartSquare, 1, true);
                    if (!geometryPartPointsWithoutHoles.isEmpty()) {
                      geometries.add(computeRoomPartGeometry(geometryPartPointsWithoutHoles, 
                          null, roomLevel, roomElevation, floorBottomElevation, 
                          true, true, texture));
                    }
                  }
                }
              }
            }
          } else {
            geometries.add(computeRoomPartGeometry(floorBottomPointsWithoutHoles, null, roomLevel,
                roomElevation, floorBottomElevation, true, true, texture));
          }
        }
      }

      return geometries.toArray(new Geometry [geometries.size()]);
    } else {
      return new Geometry [0];
    }
  }

  /**
   * Returns the room part geometry matching the given points.
   */
  private Geometry computeRoomPartGeometry(List<float [][]> geometryPoints, 
                                           List<float []> roomPointElevations,
                                           Level roomLevel,
                                           float roomPartElevation, float floorBottomElevation,
                                           boolean floorPart, boolean floorBottomPart, 
                                           HomeTexture texture) {
    int [] stripCounts = new int [geometryPoints.size()];
    int vertexCount = 0;
    for (int i = 0; i < geometryPoints.size(); i++) {
      float [][] areaPoints = geometryPoints.get(i);
      stripCounts [i] = areaPoints.length;
      vertexCount += stripCounts [i]; 
    }
    Point3f [] coords = new Point3f [vertexCount];
    int i = 0;
    for (int j = 0; j < geometryPoints.size(); j++) {
      float [][] areaPoints = geometryPoints.get(j);
      float [] roomPartPointElevations = roomPointElevations != null
          ? roomPointElevations.get(j)
          : null;
      for (int k = 0; k < areaPoints.length; k++) {
        float y = floorBottomPart 
            ? floorBottomElevation 
            : (roomPartPointElevations != null
                  ? roomPartPointElevations [k]
                  : roomPartElevation);
        coords [i++] = new Point3f(areaPoints [k][0], y, areaPoints [k][1]);
      }
    }
    GeometryInfo geometryInfo = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
    geometryInfo.setCoordinates(coords);
    geometryInfo.setStripCounts(stripCounts);
    
    if (texture != null) {
      float textureWidth = TextureManager.getInstance().getRotatedTextureWidth(texture);
      float textureHeight = TextureManager.getInstance().getRotatedTextureHeight(texture);
      TexCoord2f [] textureCoords = new TexCoord2f [vertexCount];
      i = 0;
      // Compute room texture coordinates
      for (float [][] areaPoints : geometryPoints) {
        for (int k = 0; k < areaPoints.length; k++) {
          textureCoords [i++] = new TexCoord2f(areaPoints [k][0] / textureWidth, 
              floorPart 
                  ? -areaPoints [k][1] / textureHeight
                  : areaPoints [k][1] / textureHeight);
        }
      }
      geometryInfo.setTextureCoordinateParams(1, 2);
      geometryInfo.setTextureCoordinates(0, textureCoords);
    }
    
    // Generate normals
    new NormalGenerator().generateNormals(geometryInfo);
    return geometryInfo.getIndexedGeometryArray();
  }

  /**
   * Returns the room border geometry matching the given points.
   */
  private Geometry computeRoomBorderGeometry(List<float [][]> geometryRooms, 
                                             List<float [][]> geometryHoles,
                                             Level roomLevel, float roomElevation, 
                                             HomeTexture texture) {
    int vertexCount = 0;
    for (float [][] geometryPoints : geometryRooms) {
      vertexCount += geometryPoints.length;
    }
    for (float [][] geometryHole : geometryHoles) {
      vertexCount += geometryHole.length;
    }
    vertexCount = vertexCount * 4;

    int i = 0;
    Point3f [] coords = new Point3f [vertexCount];
    float floorBottomElevation = roomElevation - roomLevel.getFloorThickness();
    // Compute room borders coordinates
    for (float [][] geometryPoints : geometryRooms) {
      for (int j = 0; j < geometryPoints.length; j++) {
        coords [i++] = new Point3f(geometryPoints [j][0], roomElevation, geometryPoints [j][1]);
        coords [i++] = new Point3f(geometryPoints [j][0], floorBottomElevation, geometryPoints [j][1]);
        int nextPoint = j < geometryPoints.length - 1  
            ? j + 1
            : 0;
        coords [i++] = new Point3f(geometryPoints [nextPoint][0], floorBottomElevation, geometryPoints [nextPoint][1]);
        coords [i++] = new Point3f(geometryPoints [nextPoint][0], roomElevation, geometryPoints [nextPoint][1]);
      }
    }
    // Compute holes borders coordinates
    for (float [][] geometryHole : geometryHoles) {
      for (int j = 0; j < geometryHole.length; j++) {
        coords [i++] = new Point3f(geometryHole [j][0], roomElevation, geometryHole [j][1]);
        int nextPoint = j < geometryHole.length - 1  
            ? j + 1
            : 0;
        coords [i++] = new Point3f(geometryHole [nextPoint][0], roomElevation, geometryHole [nextPoint][1]);
        coords [i++] = new Point3f(geometryHole [nextPoint][0], floorBottomElevation, geometryHole [nextPoint][1]);
        coords [i++] = new Point3f(geometryHole [j][0], floorBottomElevation, geometryHole [j][1]);
      }
    }

    GeometryInfo geometryInfo = new GeometryInfo(GeometryInfo.QUAD_ARRAY);
    geometryInfo.setCoordinates(coords);
    
    if (texture != null) {
      float textureWidth = TextureManager.getInstance().getRotatedTextureWidth(texture);
      float textureHeight = TextureManager.getInstance().getRotatedTextureHeight(texture);
      TexCoord2f [] textureCoords = new TexCoord2f [vertexCount];
      i = 0;
      // Compute room border texture coordinates
      for (float [][] geometryPoints : geometryRooms) {
        for (int j = 0; j < geometryPoints.length; j++) {
          textureCoords [i++] = new TexCoord2f(geometryPoints [j][0] / textureWidth, -geometryPoints [j][1] / textureHeight);
          textureCoords [i++] = new TexCoord2f(geometryPoints [j][0] / textureWidth, -(geometryPoints [j][1] - roomLevel.getFloorThickness()) / textureHeight);
          int nextPoint = j < geometryPoints.length - 1  
              ? j + 1
              : 0;
          textureCoords [i++] = new TexCoord2f(geometryPoints [nextPoint][0] / textureWidth, -(geometryPoints [nextPoint][1] - roomLevel.getFloorThickness()) / textureHeight);
          textureCoords [i++] = new TexCoord2f(geometryPoints [nextPoint][0] / textureWidth, -geometryPoints [nextPoint][1] / textureHeight);
        }
      }
      // Compute holes borders texture coordinates
      for (float [][] geometryHole : geometryHoles) {
        for (int j = 0; j < geometryHole.length; j++) {
          textureCoords [i++] = new TexCoord2f(geometryHole [j][0] / textureWidth, -geometryHole [j][1] / textureHeight);
          int nextPoint = j < geometryHole.length - 1  
              ? j + 1
              : 0;
          textureCoords [i++] = new TexCoord2f(geometryHole [nextPoint][0] / textureWidth, -geometryHole [nextPoint][1] / textureHeight);
          textureCoords [i++] = new TexCoord2f(geometryHole [nextPoint][0] / textureWidth, -(geometryHole [nextPoint][1] - roomLevel.getFloorThickness()) / textureHeight);
          textureCoords [i++] = new TexCoord2f(geometryHole [j][0] / textureWidth, -(geometryHole [j][1] - roomLevel.getFloorThickness()) / textureHeight);
        }
      }
      geometryInfo.setTextureCoordinateParams(1, 2);
      geometryInfo.setTextureCoordinates(0, textureCoords);
    }
    
    // Generate normals
    new NormalGenerator(Math.PI / 8).generateNormals(geometryInfo);
    return geometryInfo.getIndexedGeometryArray();
  }

  private void removeStaircasesFromArea(List<HomePieceOfFurniture> visibleStaircases, Area area) {
    // Remove from room area all the staircases that intersect it
    ModelManager modelManager = ModelManager.getInstance();
    for (HomePieceOfFurniture staircase : visibleStaircases) {
      area.subtract(modelManager.getAreaOnFloor(staircase));
    }
  }

  /**
   * Returns the visible staircases among the given <code>furniture</code>.  
   */
  private List<HomePieceOfFurniture> getVisibleStaircases(List<HomePieceOfFurniture> furniture, 
                                                          int roomPart, Level roomLevel,
                                                          boolean firstLevel) {
    List<HomePieceOfFurniture> visibleStaircases = new ArrayList<HomePieceOfFurniture>(furniture.size());
    for (HomePieceOfFurniture piece : furniture) {
      if (piece.isVisible()) {
        if (piece instanceof HomeFurnitureGroup) {
          visibleStaircases.addAll(getVisibleStaircases(((HomeFurnitureGroup)piece).getFurniture(), roomPart, roomLevel, firstLevel));
        } else if (piece.getStaircaseCutOutShape() != null
            && !"false".equalsIgnoreCase(piece.getStaircaseCutOutShape())
            && ((roomPart == FLOOR_PART 
                    && piece.getGroundElevation() < roomLevel.getElevation()
                    && piece.getGroundElevation() + piece.getHeight() >= roomLevel.getElevation() - (firstLevel ? 0 : roomLevel.getFloorThickness())
                || roomPart == CEILING_PART
                    && piece.getGroundElevation() < roomLevel.getElevation() + roomLevel.getHeight()
                    && piece.getGroundElevation() + piece.getHeight() >= roomLevel.getElevation() + roomLevel.getHeight()))) {
          visibleStaircases.add(piece);
        }
      }
    }
    return visibleStaircases;
  }

  /**
   * Returns an array that cites <code>points</code> in reverse order.
   */
  private float [][] getReversedArray(float [][] points) {
    points = points.clone();
    List<float []> pointList = Arrays.asList(points);
    Collections.reverse(pointList);
    return pointList.toArray(points);
  }
  
  /**
   * Returns the room height at the given point. 
   */
  private float getRoomHeightAt(float x, float y) {
    double smallestDistance = Float.POSITIVE_INFINITY;
    Room room = (Room)getUserData();
    Level roomLevel = room.getLevel();
    float roomElevation = roomLevel != null
        ? roomLevel.getElevation()
        : 0;
    float roomHeight = roomElevation + 
        (roomLevel == null ? this.home.getWallHeight() : roomLevel.getHeight());
    List<Level> levels = this.home.getLevels();
    if (roomLevel == null || isLastLevel(roomLevel, levels)) {
      // Search the closest wall point to x, y at last level
      Wall closestWall = null;
      float [][] closestWallPoints = null;
      int closestIndex = -1;
      for (Wall wall : this.home.getWalls()) {
        if (wall.isAtLevel(roomLevel)) {
          float [][] points = wall.getPoints();
          for (int i = 0; i < points.length; i++) {
            double distanceToWallPoint = Point2D.distanceSq(points [i][0], points [i][1], x, y);
            if (distanceToWallPoint < smallestDistance) {
              closestWall = wall;
              closestWallPoints = points;
              closestIndex = i;
              smallestDistance = distanceToWallPoint;
            }
          }
        }
      }
      
      if (closestWall != null) {
        roomHeight = closestWall.getLevel() == null ? 0 : closestWall.getLevel().getElevation();
        Float wallHeightAtStart = closestWall.getHeight();
        if (closestIndex == 0 || closestIndex == closestWallPoints.length - 1) { // Wall start
          roomHeight += wallHeightAtStart != null 
              ? wallHeightAtStart 
              : this.home.getWallHeight();
        } else { // Wall end
          if (closestWall.isTrapezoidal()) {
            Float arcExtent = closestWall.getArcExtent();
            if (arcExtent == null
                || closestIndex == closestWallPoints.length / 2 
                || closestIndex == closestWallPoints.length / 2 - 1) {
              roomHeight += closestWall.getHeightAtEnd();
            } else {
              // Compute the angle between start point and the current point of the wall
              // to get the relative height at that point
              float xArcCircleCenter = closestWall.getXArcCircleCenter();
              float yArcCircleCenter = closestWall.getYArcCircleCenter();
              float xClosestPoint = closestWallPoints [closestIndex][0];
              float yClosestPoint = closestWallPoints [closestIndex][1];
              double centerToClosestPointDistance = Point2D.distance(xArcCircleCenter, yArcCircleCenter, xClosestPoint, yClosestPoint);
              float xStart = closestWall.getXStart();
              float yStart = closestWall.getYStart();
              double centerToStartPointDistance = Point2D.distance(xArcCircleCenter, yArcCircleCenter, xStart, yStart);
              double scalarProduct = (xClosestPoint - xArcCircleCenter) * (xStart - xArcCircleCenter) 
                  + (yClosestPoint - yArcCircleCenter) * (yStart - yArcCircleCenter);
              scalarProduct /= (centerToClosestPointDistance * centerToStartPointDistance);
              double arcExtentToClosestWallPoint = Math.acos(scalarProduct) * Math.signum(arcExtent);
              roomHeight += (float)(wallHeightAtStart 
                  + (closestWall.getHeightAtEnd() - wallHeightAtStart) * arcExtentToClosestWallPoint / arcExtent);
            }
          } else {
            roomHeight += (wallHeightAtStart != null ? wallHeightAtStart : this.home.getWallHeight());
          }
        }
      }
    }
    return roomHeight;
  }
  
  /**
   * Returns <code>true</code> if the given level is the last level in home.
   */
  private boolean isLastLevel(Level level, List<Level> levels) {
    return levels.indexOf(level) == levels.size() - 1;
  }

  /**
   * Sets room appearance with its color, texture.
   */
  private void updateRoomAppearance(boolean waitTextureLoadingEnd) {
    Room room = (Room)getUserData();
    boolean ignoreFloorTransparency = room.getLevel() == null || room.getLevel().getElevation() <= 0;
    updateRoomPartAppearance(((Shape3D)getChild(FLOOR_PART)).getAppearance(), 
        room.getFloorTexture(), waitTextureLoadingEnd, room.getFloorColor(), room.getFloorShininess(), room.isFloorVisible(), ignoreFloorTransparency);
    // Ignore ceiling transparency for rooms without level for backward compatibility 
    boolean ignoreCeillingTransparency = room.getLevel() == null; 
    updateRoomPartAppearance(((Shape3D)getChild(CEILING_PART)).getAppearance(), 
        room.getCeilingTexture(), waitTextureLoadingEnd, room.getCeilingColor(), room.getCeilingShininess(), room.isCeilingVisible(), ignoreCeillingTransparency);
  }
  
  /**
   * Sets room part appearance with its color, texture and visibility.
   */
  private void updateRoomPartAppearance(final Appearance roomPartAppearance, 
                                        final HomeTexture roomPartTexture,
                                        boolean waitTextureLoadingEnd,
                                        Integer roomPartColor,
                                        float shininess,
                                        boolean visible,
                                        boolean ignoreTransparency) {
    if (roomPartTexture == null) {
      roomPartAppearance.setMaterial(getMaterial(roomPartColor, roomPartColor, shininess));
      roomPartAppearance.setTexture(null);
    } else {
      // Update material and texture of room part
      roomPartAppearance.setMaterial(getMaterial(DEFAULT_COLOR, DEFAULT_AMBIENT_COLOR, shininess));
      final TextureManager textureManager = TextureManager.getInstance();
      textureManager.loadTexture(roomPartTexture.getImage(), roomPartTexture.getAngle(), waitTextureLoadingEnd,
          new TextureManager.TextureObserver() {
              public void textureUpdated(Texture texture) {
                texture = getHomeTextureClone(texture, home);
                if (roomPartAppearance.getTexture() != texture) {
                  roomPartAppearance.setTexture(texture);
                }
              }
            });
    }
    if (!ignoreTransparency) { 
      // Update room part transparency
      float upperRoomsAlpha = this.home.getEnvironment().getWallsAlpha();
      TransparencyAttributes transparencyAttributes = roomPartAppearance.getTransparencyAttributes();
      transparencyAttributes.setTransparency(upperRoomsAlpha);
      // If alpha is equal to zero, turn off transparency to get better results 
      transparencyAttributes.setTransparencyMode(upperRoomsAlpha == 0 
          ? TransparencyAttributes.NONE 
          : TransparencyAttributes.NICEST);
    }
    // Update room part visibility
    RenderingAttributes renderingAttributes = roomPartAppearance.getRenderingAttributes();
    renderingAttributes.setVisible(visible);
  }
}