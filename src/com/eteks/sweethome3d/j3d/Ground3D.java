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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.media.j3d.Appearance;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Texture;
import javax.media.j3d.TransparencyAttributes;
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
    groundAppearance.setCapability(Appearance.ALLOW_TEXTURE_ATTRIBUTES_WRITE);
    groundAppearance.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_READ);
    TransparencyAttributes transparencyAttributes = new TransparencyAttributes();
    transparencyAttributes.setCapability(TransparencyAttributes.ALLOW_MODE_WRITE);
    groundAppearance.setTransparencyAttributes(transparencyAttributes);

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
    final Home home = (Home)getUserData();
    Shape3D groundShape = (Shape3D)getChild(0);
    int currentGeometriesCount = groundShape.numGeometries();
    
    final Appearance groundAppearance = groundShape.getAppearance();
    HomeTexture groundTexture = home.getEnvironment().getGroundTexture();
    if (groundTexture == null) {
      int groundColor = home.getEnvironment().getGroundColor();
      groundAppearance.setMaterial(getMaterial(groundColor, groundColor, 0));
      groundAppearance.setTexture(null);
      groundAppearance.getTransparencyAttributes().setTransparencyMode(TransparencyAttributes.NONE);      
    } else {
      groundAppearance.setMaterial(getMaterial(DEFAULT_COLOR, DEFAULT_COLOR, 0));
      groundAppearance.setTextureAttributes(getTextureAttributes(groundTexture, true));
      final TextureManager textureManager = TextureManager.getInstance();
      textureManager.loadTexture(groundTexture.getImage(), waitTextureLoadingEnd,
          new TextureManager.TextureObserver() {
              public void textureUpdated(Texture texture) {
                groundAppearance.setTexture(getHomeTextureClone(texture, home));
                TransparencyAttributes transparencyAttributes = groundAppearance.getTransparencyAttributes();
                // If texture isn't transparent, turn off transparency  
                transparencyAttributes.setTransparencyMode(TextureManager.getInstance().isTextureTransparent(texture) 
                    ? TransparencyAttributes.NICEST
                    : TransparencyAttributes.NONE);      
              }
            });
    }
    
    Area areaRemovedFromGround = new Area();
    // Compute the union of the rooms, the underground walls and furniture areas
    Map<Level, LevelAreas> undergroundLevelAreas = new HashMap<Level, LevelAreas>();
    for (Room room : home.getRooms()) {
      Level roomLevel = room.getLevel();
      if ((roomLevel == null || roomLevel.isViewable())
          && room.isFloorVisible()) {
        float [][] roomPoints = room.getPoints();
        if (roomPoints.length > 2) {
          Area roomArea = new Area(getShape(roomPoints));
          LevelAreas levelAreas = roomLevel != null && roomLevel.getElevation() < 0 
              ? getUndergroundAreas(undergroundLevelAreas, roomLevel)
              : null;
          if (roomLevel == null
              || (roomLevel.getElevation() <= 0
                  && roomLevel.isViewableAndVisible())) {
            areaRemovedFromGround.add(roomArea);
            if (levelAreas != null) {
              levelAreas.getRoomArea().add(roomArea);
            }
          }
          if (levelAreas != null) {
            levelAreas.getUndergroundArea().add(roomArea);
          }
        }
      }
    }
    
    // Search all items at negative levels that could dig the ground 
    for (HomePieceOfFurniture piece : home.getFurniture()) {
      Level pieceLevel = piece.getLevel();
      if (piece.getGroundElevation() < 0
          && pieceLevel != null
          && pieceLevel.isViewable()
          && pieceLevel.getElevation() < 0) {
        LevelAreas levelAreas = getUndergroundAreas(undergroundLevelAreas, pieceLevel);
        if (piece.getStaircaseCutOutShape() == null) {
          levelAreas.getUndergroundArea().add(new Area(getShape(piece.getPoints())));
        } else {
          levelAreas.getUndergroundArea().add(ModelManager.getInstance().getAreaOnFloor(piece));
        }
      }
    }
    for (Wall wall : home.getWalls()) {
      Level wallLevel = wall.getLevel();
      if (wallLevel != null 
          && wallLevel.isViewable()
          && wallLevel.getElevation() < 0) {
        LevelAreas levelAreas = getUndergroundAreas(undergroundLevelAreas, wallLevel);
        levelAreas.getWallArea().add(new Area(getShape(wall.getPoints())));
      }
    }
    // Consider that walls around a closed area define a hole 
    List<LevelAreas> undergroundAreas = new ArrayList<LevelAreas>(undergroundLevelAreas.values());
    for (LevelAreas levelAreas : undergroundAreas) {
      for (float [][] points : getPoints(levelAreas.getWallArea())) {
        if (!new Room(points).isClockwise()) {
          levelAreas.getUndergroundArea().add(new Area(getShape(points)));
        }
      }
    }
    
    // Sort underground areas in the reverse order of level elevation
    Collections.sort(undergroundAreas, new Comparator<LevelAreas>() {
        public int compare(LevelAreas levelAreas1, LevelAreas levelAreas2) {
          return -Float.compare(levelAreas1.getLevel().getElevation(), levelAreas2.getLevel().getElevation());
        }
      });
    for (LevelAreas levelAreas : undergroundAreas) {
      Level level = levelAreas.getLevel();
      Area area = levelAreas.getUndergroundArea();
      Area areaAtStart = (Area)area.clone();
      levelAreas.getUndergroundSideArea().add((Area)area.clone());
      // Remove lower levels areas from the area at the current level
      for (LevelAreas otherLevelAreas : undergroundAreas) {
        if (otherLevelAreas.getLevel().getElevation() < level.getElevation()) {
          for (float [][] points : getPoints(otherLevelAreas.getUndergroundArea())) {
            if (!new Room(points).isClockwise()) {
              Area pointsArea = new Area(getShape(points));
              area.subtract(pointsArea);
              levelAreas.getUndergroundSideArea().add(pointsArea);
            }
          }
        }
      }      
      // Add underground area to ground area at ground level
      for (float [][] points : getPoints(area)) {
        if (new Room(points).isClockwise()) {
          // Hole surrounded by a union of rooms that form a polygon  
          Area coveredHole = new Area(getShape(points));
          // Compute the missing hole area in the level area before other sublevels were subtracted from it 
          coveredHole.exclusiveOr(areaAtStart);
          coveredHole.subtract(areaAtStart);
          levelAreas.getUpperLevelArea().add(coveredHole);
        } else {
          areaRemovedFromGround.add(new Area(getShape(points)));
        }
      }
    }
    // Remove room areas because they are displayed by Room3D instances
    for (LevelAreas levelAreas : undergroundAreas) {
      Area roomArea = levelAreas.getRoomArea();
      if (roomArea != null) {
        Area area = levelAreas.getUndergroundArea();
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
    // Add level areas for ground level at index 0 because it's the highest level in the list 
    undergroundAreas.add(0, new LevelAreas(new Level("Ground", 0, 0, 0), groundArea));
    float previousLevelElevation = 0;
    for (LevelAreas levelAreas : undergroundAreas) {
      float elevation = levelAreas.getLevel().getElevation();
      addAreaGeometry(groundShape, groundTexture, levelAreas.getUndergroundArea(), elevation);
      if (previousLevelElevation - elevation > 0) {
        for (float [][] points : getPoints(levelAreas.getUndergroundSideArea())) {
          addAreaSidesGeometry(groundShape, groundTexture, points, elevation, previousLevelElevation - elevation);
        }
        addAreaGeometry(groundShape, groundTexture, levelAreas.getUpperLevelArea(), previousLevelElevation);
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
  private List<float [][]> getPoints(Area area) {
    List<float [][]> areaPoints = new ArrayList<float [][]>();
    List<float []>   areaPartPoints  = new ArrayList<float[]>();
    float [] previousRoomPoint = null;
    for (PathIterator it = area.getPathIterator(null, 1); !it.isDone(); it.next()) {
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
    }
    return areaPoints;
  }

  /**
   * Returns the {@link LevelAreas} instance matching the given level.
   */
  private LevelAreas getUndergroundAreas(Map<Level, LevelAreas> undergroundAreas, Level level) {
    LevelAreas levelAreas = undergroundAreas.get(level);
    if (levelAreas == null) {
      undergroundAreas.put(level, levelAreas = new LevelAreas(level));
    }
    return levelAreas;
  }

  /**
   * Adds to ground shape the geometry matching the given area.
   */
  private void addAreaGeometry(Shape3D groundShape, 
                               HomeTexture groundTexture, 
                               Area area, float elevation) {
    List<float [][]> areaPoints = getAreaPoints(area, 1, false);

    if (!areaPoints.isEmpty()) {
      int vertexCount = 0;    
      int [] stripCounts = new int [areaPoints.size()];
      for (int i = 0; i < stripCounts.length; i++) {
        stripCounts [i] = areaPoints.get(i).length;
        vertexCount += stripCounts [i];
      }
      Point3f [] geometryCoords = new Point3f [vertexCount];
      TexCoord2f [] geometryTextureCoords = groundTexture != null 
          ? new TexCoord2f [vertexCount]
          : null;
      
      int j = 0;
      for (float [][] areaPartPoints : areaPoints) {
        for (int i = 0; i < areaPartPoints.length; i++, j++) {
          float [] point = areaPartPoints [i];
          geometryCoords [j] = new Point3f(point [0], elevation, point [1]);
          if (groundTexture != null) {
            geometryTextureCoords [j] = new TexCoord2f(point [0] - this.originX, this.originY - point [1]);
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
      new NormalGenerator(0).generateNormals(geometryInfo);
      groundShape.addGeometry(geometryInfo.getIndexedGeometryArray());
    }
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
    TexCoord2f [] geometryTextureCoords = groundTexture != null 
        ? new TexCoord2f [geometryCoords.length]
        : null;
    for (int i = 0, j = 0; i < areaPoints.length; i++) {
      float [] point = areaPoints [i];
      float [] nextPoint = areaPoints [i < areaPoints.length - 1 ? i + 1 : 0];
      geometryCoords [j++] = new Point3f(point [0], elevation, point [1]);
      geometryCoords [j++] = new Point3f(point [0], elevation + sideHeight, point [1]);
      geometryCoords [j++] = new Point3f(nextPoint [0], elevation + sideHeight, nextPoint [1]);
      geometryCoords [j++] = new Point3f(nextPoint [0], elevation, nextPoint [1]);
      if (groundTexture != null) {
        float distance = (float)Point2D.distance(point [0], point [1], nextPoint [0], nextPoint [1]);
        geometryTextureCoords [j - 4] = new TexCoord2f(point [0], elevation);
        geometryTextureCoords [j - 3] = new TexCoord2f(point [0], elevation + sideHeight);
        geometryTextureCoords [j - 2] = new TexCoord2f(point [0] - distance, elevation + sideHeight);
        geometryTextureCoords [j - 1] = new TexCoord2f(point [0] - distance, elevation);
      }
    }

    GeometryInfo geometryInfo = new GeometryInfo(GeometryInfo.QUAD_ARRAY);
    geometryInfo.setCoordinates (geometryCoords);
    if (groundTexture != null) {
      geometryInfo.setTextureCoordinateParams(1, 2);
      geometryInfo.setTextureCoordinates(0, geometryTextureCoords);
    }
    new NormalGenerator(0).generateNormals(geometryInfo);
    groundShape.addGeometry(geometryInfo.getIndexedGeometryArray());
  }
  
  /**
   * Areas of underground levels.
   */
  private static class LevelAreas {
    private Level level;
    private Area undergroundArea;
    private Area roomArea = new Area();
    private Area wallArea = new Area();
    private Area undergroundSideArea = new Area();
    private Area upperLevelArea = new Area();

    public LevelAreas(Level level) {
      this(level, new Area());
    }

    public LevelAreas(Level level, Area undergroundArea) {
      this.level = level;
      this.undergroundArea = undergroundArea;
    }

    public Level getLevel() {
      return this.level;
    }

    public Area getUndergroundArea() {
      return this.undergroundArea;
    }

    public Area getRoomArea() {
      return this.roomArea;
    }

    public Area getWallArea() {
      return this.wallArea;
    }

    public Area getUndergroundSideArea() {
      return this.undergroundSideArea;
    }

    public Area getUpperLevelArea() {
      return this.upperLevelArea;
    }    
  }
}
