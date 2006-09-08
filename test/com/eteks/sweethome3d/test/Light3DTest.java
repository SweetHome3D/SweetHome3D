/*
 * Light3DTest.java 22 août 2006
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

import javax.media.j3d.AmbientLight;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Light;
import javax.vecmath.Color3f;
import javax.vecmath.Vector3f;

/**
 * Displays an <code>.obj</code> file with Java 3D.
 * @author Emmanuel Puybaret
 */
public class Light3DTest {
  public static void main (String [] args) throws FileNotFoundException {
    // Create the tree of the displayed scene 
    BranchGroup root = new BranchGroup();
    // Add a child that displays an .obj file
    root.addChild(ObjectFileTest.loadObjectFile(
        Light3DTest.class.getResource("resources/plant.obj")));
    
    // Add a gray ambient light 
    Light ambientLight = new AmbientLight(new Color3f(0.8f, 0.8f, 0.8f));
    ambientLight.setInfluencingBounds(new BoundingBox());
    root.addChild(ambientLight);
    
    // Add a white directional light
    Light light1 = new DirectionalLight(new Color3f(1, 1, 1), new Vector3f(1, -1, -1));
    light1.setInfluencingBounds(new BoundingBox());
    root.addChild(light1);

    // Uncomment next line if you want to turn off ambientLight
    // ambientLight.setEnable(false);
    // Uncomment next line if you want to turn off light1
    // light1.setEnable(false);
    Java3DTest.viewSceneTree(root);
  }  
}
