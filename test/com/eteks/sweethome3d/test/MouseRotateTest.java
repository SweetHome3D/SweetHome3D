/*
 * MouseRotate.java 23 août 2006
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

import java.io.FileNotFoundException;

import javax.media.j3d.BoundingBox;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Light;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.behaviors.mouse.MouseRotate;

/**
 * Displays an <code>.obj</code> file with Java 3D.
 * @author Emmanuel Puybaret
 */
public class MouseRotateTest {
  public static void main (String [] args) throws FileNotFoundException {
    // Create the tree of the displayed scene 
    BranchGroup root = new BranchGroup();
    // Create a transform group modified by a MouseRotate behavior
    TransformGroup transformGroup = new TransformGroup();
    transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
    transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
    MouseRotate mouseBehavior = new MouseRotate(transformGroup);
    mouseBehavior.setSchedulingBounds(new BoundingBox());
    // Add behavior and transform group to scene tree
    transformGroup.addChild(mouseBehavior);    
    root.addChild(transformGroup);
    
    // Add to the transform group a child that displays an .obj file
    transformGroup.addChild(ObjectFileTest.loadObjectFile(
        MouseRotateTest.class.getResource("resources/plant.obj")));
    
    // Add a white directional light
    Light light1 = new DirectionalLight(
        new Color3f(1, 1, 1), new Vector3f(1, -1, -1));
    light1.setInfluencingBounds(new BoundingBox());
    transformGroup.addChild(light1);
    
    Java3DTest.viewSceneTree(root);
  }  
}
