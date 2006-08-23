/*
 * BoxGeometryTest.java 21 août 2006
 *
 * Copyright (c) 2006 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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
package com.eteks.sweethome3d.test;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Point3f;

import com.sun.j3d.utils.geometry.GeometryInfo;

/**
 * Displays a box built with <code>GeometryInfo</code> Java 3D class.
 * @author Emmanuel Puybaret
 */
public class BoxGeometryTest {
  public static Shape3D createBox(float x, float y, float z) {
    // 8 vetices of a box centered at origin
    Point3f a = new Point3f(x, y, z);
    Point3f b = new Point3f(-x, y, z);
    Point3f c = new Point3f(-x, -y, z);
    Point3f d = new Point3f(x, -y, z);
    Point3f e = new Point3f(x, y, -z);
    Point3f f = new Point3f(-x, y, -z);
    Point3f g = new Point3f(-x, -y, -z);
    Point3f h = new Point3f(x, -y, -z);
    Point3f [] boxCoodinates = {a, b, c, d,   e, h, g, f,
                                a, e, f, b,   c, d, h, g,
                                a, d, h, e,   b, f, g, c};
    
    GeometryInfo geometryInfo = 
      new GeometryInfo(GeometryInfo.QUAD_ARRAY);
    geometryInfo.setCoordinates(boxCoodinates);
    return new Shape3D(geometryInfo.getIndexedGeometryArray()); 
  }
  
  public static void main (String [] args) {
    // Create the tree of the displayed scene 
    BranchGroup root = new BranchGroup();
    //  Create a transform group that applies to its children 
    // a rotation of PI / 6 rad along y axis
    Transform3D rotationY = new Transform3D();
    rotationY.rotY(Math.PI / 6);
    TransformGroup rotationYGroup = new TransformGroup(rotationY);
    //  Create a transform group that applies to its children 
    // a rotation of PI / 4 rad along x axis
    Transform3D rotationX = new Transform3D();
    rotationX.rotX(Math.PI / 4);
    TransformGroup rotationXGroup = new TransformGroup(rotationX);
    // Add transform groups and a box to tree 
    rotationXGroup.addChild(createBox(0.3f, 0.6f, 0.5f));
    rotationYGroup.addChild(rotationXGroup);
    root.addChild(rotationYGroup);

    Java3DTest.viewSceneTree(root);
  }  
}
