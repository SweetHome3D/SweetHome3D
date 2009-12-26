/*
 * Ground3D.java 23 janv. 2009
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

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;

import javax.media.j3d.Appearance;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Texture;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import javax.vecmath.TexCoord2f;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.Room;
import com.sun.j3d.utils.geometry.GeometryInfo;

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
                  float groundOriginX,
                  float groundOriginY,
                  float groundWidth,
                  float groundDepth, 
                  boolean waitTextureLoadingEnd) {
    setUserData(home);
    this.originX = groundOriginX;
    this.originY = groundOriginY;
    this.width = groundWidth;
    this.depth = groundDepth;

    // Use coloring attributes for ground to avoid ground lighting
    ColoringAttributes groundColoringAttributes = new ColoringAttributes();
    groundColoringAttributes.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
    
    Appearance groundAppearance = new Appearance();
    groundAppearance.setColoringAttributes(groundColoringAttributes);
    groundAppearance.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_READ);
    groundAppearance.setCapability(Appearance.ALLOW_TEXTURE_WRITE);

    final Shape3D groundShape = new Shape3D();
    groundShape.setAppearance(groundAppearance);
    groundShape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
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
   * Updates ground coloring and texture attributes from home ground color and texture.
   */
  private void update(boolean waitTextureLoadingEnd) {
    Home home = (Home)getUserData();
    Shape3D groundShape = (Shape3D)getChild(0);
    
    Color3f groundColor = new Color3f(new Color(home.getEnvironment().getGroundColor()));
    final Appearance groundAppearance = groundShape.getAppearance();
    groundAppearance.getColoringAttributes().setColor(groundColor);
    HomeTexture groundTexture = home.getEnvironment().getGroundTexture();
    if (groundTexture != null) {
      final TextureManager imageManager = TextureManager.getInstance();
      imageManager.loadTexture(groundTexture.getImage(), waitTextureLoadingEnd,
          new TextureManager.TextureObserver() {
              public void textureUpdated(Texture texture) {
                groundAppearance.setTexture(texture);
              }
            });
    } else {
      groundAppearance.setTexture(null);
    }
    
    // Create ground geometry
    float groundOffset = 0f;
    List<Point3f> coords = new ArrayList<Point3f>();
    List<Integer> stripCounts = new ArrayList<Integer>();
    // First add the coordinates of the ground rectangle
    coords.add(new Point3f(this.originX, groundOffset, this.originY)); 
    coords.add(new Point3f(this.originX, groundOffset, this.originY + this.depth));
    coords.add(new Point3f(this.originX + this.width, groundOffset, this.originY + this.depth));
    coords.add(new Point3f(this.originX + this.width, groundOffset, this.originY));
    // Compute ground texture coordinates if necessary
    List<TexCoord2f> textureCoords = new ArrayList<TexCoord2f>();
    if (groundTexture != null) {
      textureCoords.add(new TexCoord2f(0, 0));
      textureCoords.add(new TexCoord2f(0, this.depth / groundTexture.getHeight()));
      textureCoords.add(new TexCoord2f(this.width / groundTexture.getWidth(), this.depth / groundTexture.getHeight()));
      textureCoords.add(new TexCoord2f(this.width / groundTexture.getWidth(), 0));
    }
    stripCounts.add(4);

    // Define all the holes in ground from rooms area
    Area roomsArea = new Area();
    for (Room room : home.getRooms()) {
      if (room.isFloorVisible()) {
        float [][] points = room.getPoints();
        if (points.length > 2) {
          Shape roomShape = getShape(points);
          Area roomArea = new Area(roomShape);
          // Remove from room area the already defined hole part if it exists
          Area roomAreaIntersection = new Area(roomArea);
          roomAreaIntersection.intersect(roomsArea);
          if (!roomAreaIntersection.isEmpty()) {
            roomArea.exclusiveOr(roomAreaIntersection);
          }
          
          if (!roomArea.isEmpty()) {
            int pointsCount = 0;
            for (PathIterator it = roomArea.getPathIterator(null); !it.isDone(); ) {
              float [] roomPoint = new float[2];
              if (it.currentSegment(roomPoint) == PathIterator.SEG_CLOSE) {
                stripCounts.add(pointsCount);
                pointsCount = 0;
              } else {
                coords.add(new Point3f(roomPoint [0], groundOffset, roomPoint [1]));
                if (groundTexture != null) {
                  textureCoords.add(new TexCoord2f((roomPoint [0] - this.originX) / groundTexture.getWidth(), 
                      (roomPoint [1] - this.originY) / groundTexture.getHeight()));
                }
                pointsCount++;
              }
              it.next();
            }
            roomsArea.add(roomArea);
          }
        }
      }
    }

    GeometryInfo geometryInfo = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
    geometryInfo.setCoordinates (coords.toArray(new Point3f [coords.size()]));
    int [] stripCountsArray = new int [stripCounts.size()];
    for (int i = 0; i < stripCountsArray.length; i++) {
      stripCountsArray [i] = stripCounts.get(i);
    }
    geometryInfo.setStripCounts(stripCountsArray);
    geometryInfo.setContourCounts(new int [] {stripCountsArray.length});

    if (groundTexture != null) {
      geometryInfo.setTextureCoordinateParams(1, 2);
      geometryInfo.setTextureCoordinates(0, textureCoords.toArray(new TexCoord2f [textureCoords.size()]));
    }
    
    groundShape.setGeometry(geometryInfo.getIndexedGeometryArray());
  }
}
