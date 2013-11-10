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
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.Material;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Texture;
import javax.vecmath.Color3f;

import com.eteks.sweethome3d.model.Home;
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

  private static final Map<Long, Material>              materials = new HashMap<Long, Material>();
  private static final Map<Home, Map<Texture, Texture>> homesTextures = new WeakHashMap<Home, Map<Texture, Texture>>();
  
  static {
    DEFAULT_MATERIAL.setCapability(Material.ALLOW_COMPONENT_READ);
    DEFAULT_MATERIAL.setShininess(1);
    DEFAULT_MATERIAL.setSpecularColor(0, 0, 0);
  }
  
  /**
   * Updates the this branch from the home object.
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
            new Color3f(shininess, shininess, shininess), shininess * 128);
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
    List<List<float []>> areaPointsLists = new ArrayList<List<float[]>>();
    List<List<float []>> areaHolesLists = new ArrayList<List<float[]>>();
    ArrayList<float []>  currentPathPoints = null;
    float [] previousPoint = null;
    for (PathIterator pathIterator = area.getPathIterator(null, flatness); 
         !pathIterator.isDone(); ) {
      float [] point = new float [2];
      switch (pathIterator.currentSegment(point)) {
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
      pathIterator.next();        
    }
    
    List<float [][]> areaPointsWithoutHoles = new ArrayList<float[][]>(); 
    if (areaHolesLists.isEmpty() && areaPoints != null) {
      areaPointsWithoutHoles.addAll(areaPoints);
    } else if (areaPointsLists.isEmpty() && !areaHolesLists.isEmpty()) {
      if (areaHoles != null) {
        areaHoles.clear();
      }
    } else {
      List<Area> subAreas = new ArrayList<Area>(areaPointsLists.size());
      for (List<float []> holePoints : areaHolesLists) {
        List<float []> enclosingAreaPartPoints = null;
        if (areaPointsLists.size() == 1) {
          enclosingAreaPartPoints = areaPointsLists.get(0);
        } else {
          // Search the sub area that contains the current hole
          for (int i = 0; i < areaPointsLists.size() && enclosingAreaPartPoints == null; i++) {
            Area subArea;
            if (i >= subAreas.size()) {
              List<float []> areaPartPoints = areaPointsLists.get(i);
              subArea = new Area(getShape(areaPartPoints.toArray(new float [areaPartPoints.size()][])));
              subAreas.add(subArea);
            } else {
              subArea = subAreas.get(i);
            }
            if (subArea.contains(holePoints.get(0) [0], holePoints.get(0) [1])) {
              enclosingAreaPartPoints = areaPointsLists.get(i);
            }
          }
        }
        
        // In some very rare cases, ignore very small areas that were wrongly considered as holes
        if (enclosingAreaPartPoints != null) {
          // Search the closest points in the enclosing area and the hole part
          float minDistance = Float.MAX_VALUE;
          int holeClosestPointIndex = 0;
          int areaClosestPointIndex = 0;
          for (int i = 0; i < holePoints.size() && minDistance > 0; i++) {
            for (int j = 0; j < enclosingAreaPartPoints.size() && minDistance > 0; j++) {
              float distance = (float)Point2D.distanceSq(holePoints.get(i) [0], holePoints.get(i) [1],
                  enclosingAreaPartPoints.get(j) [0], enclosingAreaPartPoints.get(j) [1]);
              if (distance < minDistance) {
                minDistance = distance;
                holeClosestPointIndex = i;
                areaClosestPointIndex = j;
              }
            }
          }
          // Combine the areas at their closest points
          if (minDistance != 0) {
            enclosingAreaPartPoints.add(areaClosestPointIndex, enclosingAreaPartPoints.get(areaClosestPointIndex));
            enclosingAreaPartPoints.add(++areaClosestPointIndex, holePoints.get(holeClosestPointIndex));
          }
          List<float []> lastPartPoints = holePoints.subList(holeClosestPointIndex, holePoints.size());
          enclosingAreaPartPoints.addAll(areaClosestPointIndex, lastPartPoints);
          enclosingAreaPartPoints.addAll(areaClosestPointIndex + lastPartPoints.size(), holePoints.subList(0, holeClosestPointIndex));
        }
      }
      
      for (List<float []> pathPoints : areaPointsLists) {
        if (reversed) {
          Collections.reverse(pathPoints);
        }
        areaPointsWithoutHoles.add(pathPoints.toArray(new float [pathPoints.size()][]));
      }
    }
    
    return areaPointsWithoutHoles;
  }
}