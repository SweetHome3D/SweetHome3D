/*
 * Ground3D.java 23 janv. 2009
 *
 * Sweet Home 3D, Copyright (c) 2009 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.media.j3d.Appearance;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Texture;
import javax.media.j3d.TextureAttributes;
import javax.vecmath.Point3f;
import javax.vecmath.TexCoord2f;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.Level;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.Wall;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;

/**
 * Root of a the 3D ground.
 * @author Emmanuel Puybaret
 */
public class Ground3D extends Object3DBranch {
  private final float originX;
  private final float originY;
  private final float width;
  private final float depth;

  /**
   * Creates a 3D ground for the given <code>home</code>.
   */
  public Ground3D(Home home,
                  float originX,
                  float originY,
                  float width,
                  float depth, 
                  boolean waitTextureLoadingEnd) {
    setUserData(home);
    this.originX = originX;
    this.originY = originY;
    this.width = width;
    this.depth = depth;

    Appearance groundAppearance = new Appearance();
    groundAppearance.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
    groundAppearance.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
    TextureAttributes textureAttributes = new TextureAttributes();
    textureAttributes.setTextureMode(TextureAttributes.MODULATE);
    groundAppearance.setTextureAttributes(textureAttributes);

    final Shape3D groundShape = new Shape3D();
    groundShape.setAppearance(groundAppearance);
    groundShape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
    groundShape.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
    groundShape.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
    
    setCapability(ALLOW_CHILDREN_READ);
    
    addChild(groundShape);

    update(waitTextureLoadingEnd);    
  }
  
  /**
   * Updates ground coloring and texture attributes from home ground color and texture.
   */
  @Override
  public void update() {
    update(false);
  }
  
  /**
   * Updates the geometry and attributes of ground and sublevels.
   */
  private void update(boolean waitTextureLoadingEnd) {
    Home home = (Home)getUserData();
    Shape3D groundShape = (Shape3D)getChild(0);
    int currentGeometriesCount = groundShape.numGeometries();
    
    final Appearance groundAppearance = groundShape.getAppearance();
    HomeTexture groundTexture = home.getEnvironment().getGroundTexture();
    if (groundTexture == null) {
      int groundColor = home.getEnvironment().getGroundColor();
      groundAppearance.setMaterial(getMaterial(groundColor, groundColor, 0));
      groundAppearance.setTexture(null);
    } else {
      groundAppearance.setMaterial(getMaterial(DEFAULT_COLOR, DEFAULT_COLOR, 0));
      final TextureManager imageManager = TextureManager.getInstance();
      imageManager.loadTexture(groundTexture.getImage(), waitTextureLoadingEnd,
          new TextureManager.TextureObserver() {
              public void textureUpdated(Texture texture) {
                groundAppearance.setTexture(texture);
              }
            });
    }
    
    Area areaRemovedFromGround = new Area();
    // Compute the union of the rooms, the underground walls and furniture areas 
    Comparator<Level> levelComparator = new Comparator<Level>() {
        public int compare(Level level1, Level level2) {
          return -Float.compare(level1.getElevation(), level2.getElevation());
        }
      };
    Map<Level, Area> undergroundAreas = new TreeMap<Level, Area>(levelComparator);
    Map<Level, Area> roomAreas = new TreeMap<Level, Area>(levelComparator);
    for (Room room : home.getRooms()) {
      Level roomLevel = room.getLevel();
      if (room.isFloorVisible()) {
        float [][] roomPoints = room.getPoints();
        if (roomPoints.length > 2) {
          Area roomArea = null;
          if (roomLevel == null
              || (roomLevel.getElevation() <= 0
                  && roomLevel.isVisible())) {
            roomArea = new Area(getShape(roomPoints));
            areaRemovedFromGround.add(roomArea);
            updateUndergroundAreas(roomAreas, room.getLevel(), roomPoints, roomArea);
          }
          updateUndergroundAreas(undergroundAreas, room.getLevel(), roomPoints, roomArea);
        }
      }
    }
    
    // Search all items at negative levels that could dig the ground 
    for (HomePieceOfFurniture piece : home.getFurniture()) {
      if (piece.getStaircaseCutOutShape() == null) {
        updateUndergroundAreas(undergroundAreas, piece.getLevel(), piece.getPoints(), null);
      } else {
        updateUndergroundAreas(undergroundAreas, piece.getLevel(), null, ModelManager.getInstance().getAreaOnFloor(piece));
      }
    }
    Map<Level, Area> wallAreas = new HashMap<Level, Area>();
    for (Wall wall : home.getWalls()) {
      updateUndergroundAreas(wallAreas, wall.getLevel(), wall.getPoints(), null);
    }
    // Consider that walls around a closed area define a hole 
    for (Map.Entry<Level, Area> wallAreaEntry : wallAreas.entrySet()) {
      for (float [][] points : getAreaPoints(wallAreaEntry.getValue())) {
        if (!new Room(points).isClockwise()) {
          updateUndergroundAreas(undergroundAreas, wallAreaEntry.getKey(), points, null);
        }
      }
    }
    
    Map<Level, Area> undergroundSideAreas = new TreeMap<Level, Area>(levelComparator);
    Map<Level, Area> upperLevelAreas = new HashMap<Level, Area>();
    for (Map.Entry<Level, Area> undergroundAreaEntry : undergroundAreas.entrySet()) {
      Level level = undergroundAreaEntry.getKey();
      Area area = undergroundAreaEntry.getValue();
      undergroundSideAreas.put(level, (Area)area.clone());
      upperLevelAreas.put(level, new Area());
      // Remove lower levels areas from the area at the current level
      for (Map.Entry<Level, Area> otherUndergroundAreaEntry : undergroundAreas.entrySet()) {
        if (otherUndergroundAreaEntry.getKey().getElevation() < level.getElevation()) {
          for (float [][] points : getAreaPoints(otherUndergroundAreaEntry.getValue())) {
            if (!new Room(points).isClockwise()) {
              Area pointsArea = new Area(getShape(points));
              area.subtract(pointsArea);
              undergroundSideAreas.get(level).add(pointsArea);
            }
          }
        }
      }      
      // Add underground area to ground area at ground level
      for (float [][] points : getAreaPoints(area)) {
        if (new Room(points).isClockwise()) {
          upperLevelAreas.get(level).add(new Area(getShape(points)));
        } else {
          areaRemovedFromGround.add(new Area(getShape(points)));
        }
      }
    }
    // Remove room areas 
    for (Map.Entry<Level, Area> undergroundAreaEntry : undergroundAreas.entrySet()) {
      Level level = undergroundAreaEntry.getKey();
      Area area = undergroundAreaEntry.getValue();
      Area roomArea = roomAreas.get(level);
      if (roomArea != null) {
        area.subtract(roomArea);
      }
    }
    
    // Define ground and underground levels surfaces
    Area groundArea = new Area(getShape(new float [][] {
        {this.originX, this.originY}, 
        {this.originX, this.originY + this.depth},
        {this.originX + this.width, this.originY + this.depth},
        {this.originX + this.width, this.originY}}));
    Rectangle2D removedAreaBounds = areaRemovedFromGround.getBounds2D();
    if (!groundArea.getBounds2D().equals(removedAreaBounds)) {
      Area outsideGroundArea = groundArea;
      if (areaRemovedFromGround.isEmpty()) {
        removedAreaBounds = new Rectangle2D.Float(Math.max(-5E3f, this.originX), Math.max(-5E3f, this.originY), 0, 0);
        removedAreaBounds.add(Math.min(5E3f, this.originX + this.width), 
            Math.min(5E3f, this.originY + this.depth));            
      } else {
        removedAreaBounds.add(Math.max(removedAreaBounds.getMinX() - 5E3, this.originX), 
            Math.max(removedAreaBounds.getMinY() - 5E3, this.originY));
        removedAreaBounds.add(Math.min(removedAreaBounds.getMaxX() + 5E3, this.originX + this.width), 
            Math.min(removedAreaBounds.getMaxY() + 5E3, this.originY + this.depth));
      }
      groundArea = new Area(removedAreaBounds);
      outsideGroundArea.subtract(groundArea);
      // Divide the ground at level 0 in two geometries to limit visual artifacts on large zone  
      addAreaGeometry(groundShape, groundTexture, outsideGroundArea, 0);
    }
    groundArea.subtract(areaRemovedFromGround);
    undergroundAreas.put(new Level("Ground", 0, 0, 0), groundArea);
    float previousLevelElevation = 0;
    for (Map.Entry<Level, Area> undergroundAreaEntry : undergroundAreas.entrySet()) {
      Level level = undergroundAreaEntry.getKey();
      float elevation = level.getElevation();
      addAreaGeometry(groundShape, groundTexture, undergroundAreaEntry.getValue(), elevation);
      if (previousLevelElevation - elevation > 0) {
        for (float [][] points : getAreaPoints(undergroundSideAreas.get(level))) {
          addAreaSidesGeometry(groundShape, groundTexture, points, elevation, previousLevelElevation - elevation);
        }
        for (float [][] points : getAreaPoints(upperLevelAreas.get(level))) {
          addAreaGeometry(groundShape, groundTexture, points, null, previousLevelElevation);
        }
      }
      previousLevelElevation = elevation;
    }

    // Remove old geometries
    for (int i = currentGeometriesCount - 1; i >= 0; i--) {
      groundShape.removeGeometry(i);
    }
  }

  /**
   * Returns the list of points that defines the given area.
   */
  private List<float [][]> getAreaPoints(Area area) {
    List<float [][]> areaPoints = new ArrayList<float [][]>();
    List<float []>   areaPartPoints  = new ArrayList<float[]>();
    float [] previousRoomPoint = null;
    for (PathIterator it = area.getPathIterator(null, 1); !it.isDone(); ) {
      float [] roomPoint = new float[2];
      if (it.currentSegment(roomPoint) == PathIterator.SEG_CLOSE) {
        if (areaPartPoints.get(0) [0] == previousRoomPoint [0] 
            && areaPartPoints.get(0) [1] == previousRoomPoint [1]) {
          areaPartPoints.remove(areaPartPoints.size() - 1);
        }
        if (areaPartPoints.size() > 2) {
          areaPoints.add(areaPartPoints.toArray(new float [areaPartPoints.size()][]));
        }
        areaPartPoints.clear();
        previousRoomPoint = null;
      } else {
        if (previousRoomPoint == null
            || roomPoint [0] != previousRoomPoint [0] 
            || roomPoint [1] != previousRoomPoint [1]) {
          areaPartPoints.add(roomPoint);
        }
        previousRoomPoint = roomPoint;
      }
      it.next();
    }
    return areaPoints;
  }

  /**
   * Adds the given area to the underground areas for level below zero.
   */
  private void updateUndergroundAreas(Map<Level, Area> undergroundAreas, 
                                      Level      level, 
                                      float [][] points, 
                                      Area       area) {
    if (level != null 
        && level.getElevation() < 0) {
      Area itemsArea = undergroundAreas.get(level);
      if (itemsArea == null) {
        itemsArea = new Area();
        undergroundAreas.put(level, itemsArea);
      }
      itemsArea.add(area != null
          ? area
          : new Area(getShape(points)));
    }
  }

  /**
   * Adds to ground shape the geometry matching the given area.
   */
  private void addAreaGeometry(Shape3D groundShape, HomeTexture groundTexture, Area area, float elevation) {
    List<float [][]> levelHolesPoints = new ArrayList<float[][]>();
    for (float [][] points : getAreaPoints(area)) {
      if (new Room(points).isClockwise()) {
        levelHolesPoints.add(points);
      } else {
        addAreaGeometry(groundShape, groundTexture, points, levelHolesPoints, elevation);
        levelHolesPoints.clear();
      }
    }
  }

  /**
   * Adds to ground shape the geometry matching the area defined by the given points and hole points.
   */
  private void addAreaGeometry(Shape3D groundShape, 
                               HomeTexture groundTexture, 
                               float [][] areaPoints,
                               List<float [][]> holesPoints, 
                               float elevation) {
    if (holesPoints == null) {
      holesPoints = Collections.emptyList();
    }
    int pointCount = areaPoints.length;    
    for (float [][] holePoints : holesPoints) {
      pointCount += holePoints.length;
    }
    Point3f [] geometryCoords = new Point3f [pointCount];
    int [] stripCounts = new int [1 + holesPoints.size()];
    int [] contourCounts = new int [stripCounts.length - holesPoints.size()];    
    TexCoord2f [] geometryTextureCoords = groundTexture != null 
        ? new TexCoord2f [pointCount]
        : null;
    
    stripCounts [0] = areaPoints.length;
    Arrays.fill(contourCounts, 1);
    for (int i = 0; i < holesPoints.size(); i++) {
      stripCounts [i + 1] = holesPoints.get(i).length;
      contourCounts [0]++;
    }
    int j = 0;
    for (int i = 0; i < areaPoints.length; i++, j++) {
      float [] point = areaPoints [i];
      geometryCoords [j] = new Point3f(point [0], elevation, point [1]);
      if (groundTexture != null) {
        geometryTextureCoords [j] = new TexCoord2f((point [0] - this.originX) / groundTexture.getWidth(), 
            (this.originY - point [1]) / groundTexture.getHeight());
      }
    }
    for (float [][] holePoints : holesPoints) {
      for (int i = 0; i < holePoints.length; i++, j++) {
        float [] point = holePoints [i];
        geometryCoords [j] = new Point3f(point [0], elevation, point [1]);
        if (groundTexture != null) {
          geometryTextureCoords [j] = new TexCoord2f((point [0] - this.originX) / groundTexture.getWidth(), 
              (this.originY - point [1]) / groundTexture.getHeight());
        }
      }
    }
    
    GeometryInfo geometryInfo = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
    geometryInfo.setCoordinates (geometryCoords);
    if (groundTexture != null) {
      geometryInfo.setTextureCoordinateParams(1, 2);
      geometryInfo.setTextureCoordinates(0, geometryTextureCoords);
    }
    geometryInfo.setStripCounts(stripCounts);
    geometryInfo.setContourCounts(contourCounts);
    new NormalGenerator(0).generateNormals(geometryInfo);
    groundShape.addGeometry(geometryInfo.getIndexedGeometryArray());
  }

  /**
   * Adds to ground shape the geometry matching the given area sides.
   */
  private void addAreaSidesGeometry(Shape3D groundShape, 
                                    HomeTexture groundTexture, 
                                    float [][] areaPoints,
                                    float elevation, 
                                    float sideHeight) {
    Point3f [] geometryCoords = new Point3f [areaPoints.length * 4];
    int [] stripCounts = new int [areaPoints.length];
    int [] contourCounts = new int [stripCounts.length];    
    TexCoord2f [] geometryTextureCoords = groundTexture != null 
        ? new TexCoord2f [geometryCoords.length]
        : null;
    Arrays.fill(stripCounts, 4);
    Arrays.fill(contourCounts, 1);
    for (int i = 0, j = 0; i < areaPoints.length; i++) {
      float [] point = areaPoints [i];
      float [] nextPoint = areaPoints [i < areaPoints.length - 1 ? i + 1 : 0];
      geometryCoords [j++] = new Point3f(point [0], elevation, point [1]);
      geometryCoords [j++] = new Point3f(point [0], elevation + sideHeight, point [1]);
      geometryCoords [j++] = new Point3f(nextPoint [0], elevation + sideHeight, nextPoint [1]);
      geometryCoords [j++] = new Point3f(nextPoint [0], elevation, nextPoint [1]);
      if (groundTexture != null) {
        float distance = (float)Point2D.distance(point [0], point [1], nextPoint [0], nextPoint [1]);
        geometryTextureCoords [j - 4] = new TexCoord2f(point [0] / groundTexture.getWidth(), elevation / groundTexture.getHeight());
        geometryTextureCoords [j - 3] = new TexCoord2f(point [0] / groundTexture.getWidth(), (elevation + sideHeight) / groundTexture.getHeight());
        geometryTextureCoords [j - 2] = new TexCoord2f((point [0] - distance) / groundTexture.getWidth(), (elevation + sideHeight) / groundTexture.getHeight());
        geometryTextureCoords [j - 1] = new TexCoord2f((point [0] - distance) / groundTexture.getWidth(), elevation / groundTexture.getHeight());
      }
    }

    GeometryInfo geometryInfo = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
    geometryInfo.setCoordinates (geometryCoords);
    if (groundTexture != null) {
      geometryInfo.setTextureCoordinateParams(1, 2);
      geometryInfo.setTextureCoordinates(0, geometryTextureCoords);
    }
    geometryInfo.setStripCounts(stripCounts);
    geometryInfo.setContourCounts(contourCounts);
    new NormalGenerator(0).generateNormals(geometryInfo);
    groundShape.addGeometry(geometryInfo.getIndexedGeometryArray());
  }
}

