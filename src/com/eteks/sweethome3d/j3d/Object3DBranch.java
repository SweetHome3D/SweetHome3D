/*
 * Object3DBranch.java 23 jan. 09
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

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.Material;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Texture;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.Transform3D;
import javax.vecmath.Color3f;
import javax.vecmath.Vector3d;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.Room;

/**
 * Root of a branch that matches a home object. 
 */
public abstract class Object3DBranch extends BranchGroup {
  // The coloring attributes used for drawing outline 
  protected static final ColoringAttributes OUTLINE_COLORING_ATTRIBUTES = 
      new ColoringAttributes(new Color3f(0.16f, 0.16f, 0.16f), ColoringAttributes.FASTEST);
  protected static final PolygonAttributes OUTLINE_POLYGON_ATTRIBUTES = 
      new PolygonAttributes(PolygonAttributes.POLYGON_LINE, PolygonAttributes.CULL_BACK, 0);
  protected static final LineAttributes OUTLINE_LINE_ATTRIBUTES = 
      new LineAttributes(0.5f, LineAttributes.PATTERN_SOLID, true);

  protected static final Integer  DEFAULT_COLOR         = 0xFFFFFF;
  protected static final Integer  DEFAULT_AMBIENT_COLOR = 0x333333;
  protected static final Material DEFAULT_MATERIAL      = new Material();

  private static final Map<Long, Material>                materials = new HashMap<Long, Material>();
  private static final Map<TextureKey, TextureAttributes> textureAttributes = new HashMap<TextureKey, TextureAttributes>();
  private static final Map<Home, Map<Texture, Texture>>   homesTextures = new WeakHashMap<Home, Map<Texture, Texture>>();
  
  static {
    DEFAULT_MATERIAL.setCapability(Material.ALLOW_COMPONENT_READ);
    DEFAULT_MATERIAL.setShininess(1);
    DEFAULT_MATERIAL.setSpecularColor(0, 0, 0);
  }
  
  /**
   * Updates this branch from the home object.
   */
  public abstract void update();

  /**
   * Returns a cloned instance of texture shared per <code>home</code> or 
   * the texture itself if <code>home</code> is <code>null</code>.
   * As sharing textures across universes might cause some problems, 
   * it's safer to handle a copy of textures for a given home. 
   */
  protected Texture getHomeTextureClone(Texture texture, Home home) {
    if (home == null || texture == null) {
      return texture;
    } else {
      Map<Texture, Texture> homeTextures = homesTextures.get(home);
      if (homeTextures == null) {
        homeTextures = new WeakHashMap<Texture, Texture>();
        homesTextures.put(home, homeTextures);
      }
      Texture clonedTexture = homeTextures.get(texture);
      if (clonedTexture == null) {
        clonedTexture = (Texture)texture.cloneNodeComponent(false);
        homeTextures.put(texture, clonedTexture);
      }
      return clonedTexture;
    }
  }
  
  /**
   * Returns the shape matching the coordinates in <code>points</code> array.
   */
  protected Shape getShape(float [][] points) {
    GeneralPath path = new GeneralPath();
    path.moveTo(points [0][0], points [0][1]);
    for (int i = 1; i < points.length; i++) {
      path.lineTo(points [i][0], points [i][1]);
    }
    path.closePath();
    return path;
  }
  
  /**
   * Returns a shared material instance matching the given color.
   */
  protected Material getMaterial(Integer diffuseColor, Integer ambientColor, float shininess) {
    if (diffuseColor != null) {
      Long materialKey = new Long(diffuseColor + (ambientColor << 24) + ((char)(shininess * 128) << 48));
      Material material = materials.get(materialKey); 
      if (material == null) {
        Color3f ambientMaterialColor = new Color3f(((ambientColor >>> 16) & 0xFF) / 255f,
                                                    ((ambientColor >>> 8) & 0xFF) / 255f,
                                                            (ambientColor & 0xFF) / 255f);
        Color3f diffuseMaterialColor = new Color3f(((diffuseColor >>> 16) & 0xFF) / 255f,
                                                    ((diffuseColor >>> 8) & 0xFF) / 255f,
                                                            (diffuseColor & 0xFF) / 255f);
        material = new Material(ambientMaterialColor, new Color3f(), diffuseMaterialColor, 
            new Color3f(shininess, shininess, shininess), Math.max(1, shininess * 128));
        material.setCapability(Material.ALLOW_COMPONENT_READ);
        // Store created materials in cache
        materials.put(materialKey, material);
      }
      return material;
    } else {
      return getMaterial(DEFAULT_COLOR, DEFAULT_AMBIENT_COLOR, shininess);
    }
  }
  
  /**
   * Returns shared texture attributes matching transformation applied to the given texture.
   */
  protected TextureAttributes getTextureAttributes(HomeTexture texture) {
    return getTextureAttributes(texture, false);
  }
  
  /**
   * Returns shared texture attributes matching transformation applied to the given texture 
   * and scaled if required.
   */
  protected TextureAttributes getTextureAttributes(HomeTexture texture, boolean scaled) {
    float textureWidth = texture.getWidth();
    float textureHeight = texture.getHeight();
    if (textureWidth == -1 || textureHeight == -1) {
      // Set a default value of 1m for textures with width and height equal to -1
      // (this may happen for textures retrieved from 3D models)
      textureWidth = 100;
      textureHeight = 100;
    }
    float textureAngle = texture.getAngle();
    float textureScale = 1 / texture.getScale();
    TextureKey key = scaled
        ? new TextureKey(textureWidth, textureHeight, textureAngle, textureScale)
        : new TextureKey(-1f, -1f, textureAngle, textureScale);
    TextureAttributes textureAttributes = Object3DBranch.textureAttributes.get(key);
    if (textureAttributes == null) {
      textureAttributes = new TextureAttributes();
      // Mix texture and color
      textureAttributes.setTextureMode(TextureAttributes.MODULATE);
      Transform3D rotation = new Transform3D();
      rotation.rotZ(textureAngle);
      Transform3D transform = new Transform3D();
      // Change scale if required
      if (scaled) {
        transform.setScale(new Vector3d(textureScale / textureWidth, textureScale / textureHeight, textureScale));
      } else {
        transform.setScale(textureScale);
      }
      transform.mul(rotation);
      textureAttributes.setTextureTransform(transform);
      textureAttributes.setCapability(TextureAttributes.ALLOW_TRANSFORM_READ);
      Object3DBranch.textureAttributes.put(key, textureAttributes);
    }
    return textureAttributes;
  }

  /**
   * Key used to share texture attributes instances.
   */
  private static class TextureKey {
    private final float width;
    private final float height;
    private final float angle;
    private final float scale;
    
    public TextureKey(float width, float height, float angle, float scale) {
      this.width = width;
      this.height = height;
      this.angle = angle;
      this.scale = scale;
    }
    
    @Override
    public boolean equals(Object obj) {
      TextureKey key = (TextureKey)obj;
      return this.width == key.width 
          && this.height == key.height 
          && this.angle == key.angle 
          && this.scale == key.scale;
    }
    
    @Override
    public int hashCode() {
      return Float.floatToIntBits(this.width) * 31 
          + Float.floatToIntBits(this.height) * 31
          + Float.floatToIntBits(this.angle) * 31
          + Float.floatToIntBits(this.scale);
    }
  }

  /**
   * Returns the list of polygons points matching the given <code>area</code>.
   */
  protected List<float [][]> getAreaPoints(Area area, 
                                           float flatness, 
                                           boolean reversed) {
    return getAreaPoints(area, null, null, flatness, reversed);
  }
  
  /**
   * Returns the list of polygons points matching the given <code>area</code> with detailed information in 
   * <code>areaPoints</code> and <code>areaHoles</code>.
   */
  protected List<float [][]> getAreaPoints(Area area, 
                                           List<float [][]> areaPoints,
                                           List<float [][]> areaHoles,
                                           float flatness, 
                                           boolean reversed) {
    List<List<float []>> areaPointsLists = new LinkedList<List<float[]>>();
    List<List<float []>> areaHolesLists = new LinkedList<List<float[]>>();
    ArrayList<float []>  currentPathPoints = null;
    float [] previousPoint = null;
    for (PathIterator it = area.getPathIterator(null, flatness); !it.isDone(); it.next()) {
      float [] point = new float [2];
      switch (it.currentSegment(point)) {
        case PathIterator.SEG_MOVETO :
          currentPathPoints = new ArrayList<float[]>();
          currentPathPoints.add(point);
          previousPoint = point;          
          break;
        case PathIterator.SEG_LINETO : 
          if (point [0] != previousPoint [0] 
              || point [1] != previousPoint [1]) {
            currentPathPoints.add(point);
          }
          previousPoint = point;          
          break;
        case PathIterator.SEG_CLOSE:
          float [] firstPoint = currentPathPoints.get(0);
          if (firstPoint [0] == previousPoint [0]
              && firstPoint [1] == previousPoint [1]) {
            currentPathPoints.remove(currentPathPoints.size() - 1);
          }
          if (currentPathPoints.size() > 2) {
            float [][] areaPartPoints = currentPathPoints.toArray(new float [currentPathPoints.size()][]); 
            Room subRoom = new Room(areaPartPoints);
            if (subRoom.getArea() > 0) {
              boolean pathPointsClockwise = subRoom.isClockwise();
              if (pathPointsClockwise) {
                // Keep holes points to remove them from the area once all points are retrieved
                areaHolesLists.add(currentPathPoints);
              } else {
                areaPointsLists.add(currentPathPoints);
              }
              
              if (areaPoints != null || areaHoles != null) {
                // Store path points in returned lists
                if (pathPointsClockwise ^ reversed) {
                  currentPathPoints = (ArrayList<float []>)currentPathPoints.clone();
                  Collections.reverse(currentPathPoints);
                  currentPathPoints.toArray(areaPartPoints);
                }
                if (pathPointsClockwise) {
                  if (areaHoles != null) {
                    areaHoles.add(areaPartPoints);
                  }
                } else {
                  if (areaPoints != null) {
                    areaPoints.add(areaPartPoints);
                  }
                }
              }
            }
          }
          break;
      }
    }
    
    List<float [][]> areaPointsWithoutHoles = new ArrayList<float[][]>(); 
    if (areaHolesLists.isEmpty() && areaPoints != null) {
      areaPointsWithoutHoles.addAll(areaPoints);
    } else if (areaPointsLists.isEmpty() && !areaHolesLists.isEmpty()) {
      if (areaHoles != null) {
        areaHoles.clear();
      }
    } else {
      // Sort areas from larger areas to smaller ones included in larger ones
      List<List<float []>> sortedAreaPoints;
      Map<List<float []>, Area> subAreas = new HashMap<List<float []>, Area>(areaPointsLists.size());
      if (areaPointsLists.size() > 1) {
        sortedAreaPoints = new ArrayList<List<float[]>>(areaPointsLists.size());
        for (int i = 0; !areaPointsLists.isEmpty(); ) {
          List<float []> testedArea = areaPointsLists.get(i);
          int j = 0;
          for ( ; j < areaPointsLists.size(); j++) {
            if (i != j) {
              List<float []> testedAreaPoints = areaPointsLists.get(j);
              Area subArea = subAreas.get(testedAreaPoints);
              if (subArea == null) {
                subArea = new Area(getShape(testedAreaPoints.toArray(new float [testedAreaPoints.size()][])));
                // Store computed area for future reuse 
                subAreas.put(testedAreaPoints, subArea);
              }
              if (subArea.contains(testedArea.get(0) [0], testedArea.get(0) [1])) {
                break;
              }
            }
          }
          if (j == areaPointsLists.size()) {
            areaPointsLists.remove(i);
            sortedAreaPoints.add(testedArea);
            i = 0;
          } else if (i < areaPointsLists.size()) {
            i++;
          } else {
            i = 0;
          }
        }
      } else {
        sortedAreaPoints = areaPointsLists;
      }
      for (int i = sortedAreaPoints.size() - 1; i >= 0; i--) {
        List<float []> enclosingAreaPartPoints = sortedAreaPoints.get(i);
        Area subArea = subAreas.get(enclosingAreaPartPoints);
        if (subArea == null) {
          subArea = new Area(getShape(enclosingAreaPartPoints.toArray(new float [enclosingAreaPartPoints.size()][])));
          // No need to store computed area because it won't be reused 
        }
        List<List<float []>> holesInArea = new ArrayList<List<float []>>();
        // Search the holes contained in the current area part
        for (List<float []> holePoints : areaHolesLists) {
          if (subArea.contains(holePoints.get(0) [0], holePoints.get(0) [1])) {
            holesInArea.add(holePoints);
          }
        }
        
        while (!holesInArea.isEmpty()) {
          // Search the closest points in the enclosing area and the holes
          float minDistance = Float.MAX_VALUE;
          int closestHolePointsIndex = 0;
          int closestPointIndex = 0;
          int areaClosestPointIndex = 0;
          for (int j = 0; j < holesInArea.size() && minDistance > 0; j++) {
            List<float []> holePoints = holesInArea.get(j);
            for (int k = 0; k < holePoints.size() && minDistance > 0; k++) {
              for (int l = 0; l < enclosingAreaPartPoints.size() && minDistance > 0; l++) {
                float distance = (float)Point2D.distanceSq(holePoints.get(k) [0], holePoints.get(k) [1],
                    enclosingAreaPartPoints.get(l) [0], enclosingAreaPartPoints.get(l) [1]);
                if (distance < minDistance) {
                  minDistance = distance;
                  closestHolePointsIndex = j;
                  closestPointIndex = k;
                  areaClosestPointIndex = l;
                }
              }
            }
          }
          // Combine the areas at their closest points
          List<float []> closestHolePoints = holesInArea.get(closestHolePointsIndex);
          if (minDistance != 0) {
            enclosingAreaPartPoints.add(areaClosestPointIndex, enclosingAreaPartPoints.get(areaClosestPointIndex));
            enclosingAreaPartPoints.add(++areaClosestPointIndex, closestHolePoints.get(closestPointIndex));
          }
          List<float []> lastPartPoints = closestHolePoints.subList(closestPointIndex, closestHolePoints.size());
          enclosingAreaPartPoints.addAll(areaClosestPointIndex, lastPartPoints);
          enclosingAreaPartPoints.addAll(areaClosestPointIndex + lastPartPoints.size(), closestHolePoints.subList(0, closestPointIndex));
          
          holesInArea.remove(closestHolePointsIndex);
          areaHolesLists.remove(closestHolePoints);
        }
      }
      
      for (List<float []> pathPoints : sortedAreaPoints) {
        if (reversed) {
          Collections.reverse(pathPoints);
        }
        areaPointsWithoutHoles.add(pathPoints.toArray(new float [pathPoints.size()][]));
      }
    }
    
    return areaPointsWithoutHoles;
  }
}