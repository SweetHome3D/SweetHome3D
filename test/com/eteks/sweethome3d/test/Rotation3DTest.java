/*
 * CubeTest.java 18 août 2006
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
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;

import com.sun.j3d.utils.geometry.ColorCube;

/**
 * Tests the rotation on a colored cube displayed in a window.
 * @author Emmanuel Puybaret
 */
public class Rotation3DTest {
  public static void main (String [] args) {
    // Create the tree of the displayed scene 
    BranchGroup root = new BranchGroup();
    // Create a colored cube of 1 unit size centered at origin
    ColorCube cube = new ColorCube (0.5);
    // Create a transform group that applies to its children 
    // a rotation of PI / 4 rad along x axis
    Transform3D rotationX = new Transform3D();
    rotationX.rotX(Math.PI / 4);
    TransformGroup rotationXGroup = new TransformGroup(rotationX);
    // Add a cube to transform group
    rotationXGroup.addChild (cube);
    // Add transform group to tree root
    root.addChild(rotationXGroup);

    Java3DTest.viewSceneTree(root);
  }
}
