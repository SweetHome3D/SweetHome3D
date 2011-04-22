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
import java.awt.geom.GeneralPath;
import java.util.HashMap;
import java.util.Map;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.Material;
import javax.media.j3d.PolygonAttributes;
import javax.vecmath.Color3f;

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

  private static final Map<Long, Material> materials = new HashMap<Long, Material>();

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
}