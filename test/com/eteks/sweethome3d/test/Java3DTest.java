/*
 * Java3DTest.java 18 août 2006
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
import javax.media.j3d.Canvas3D;
import javax.swing.JFrame;

import com.sun.j3d.utils.geometry.ColorCube;
import com.sun.j3d.utils.universe.SimpleUniverse;

/**
 * Tests Java 3D with a colored cube displayed in a window.
 * @author Emmanuel Puybaret
 */
public class Java3DTest {
  public static void main (String [] args) {
    // Create the tree of the displayed scene 
    BranchGroup root = new BranchGroup();
    // Create a colored cube of 1 unit size centered at origin
    ColorCube cube = new ColorCube (0.5);
    // Add cube to tree root
    root.addChild (cube);
    
    viewSceneTree(root);
  }

  public static void viewSceneTree(BranchGroup root) {
    // Create a 3D component
    Canvas3D component3D = new Canvas3D(
        SimpleUniverse.getPreferredConfiguration());

    // Create a universe bound to component and displayed scene
    SimpleUniverse universe = new SimpleUniverse(component3D);
    universe.addBranchGraph(root);
    // Move observer to let him view the scene
    universe.getViewingPlatform().
        setNominalViewingTransform();
    
    // Display 3D component in a frame
    JFrame frame = new JFrame("Java 3D Test");
    frame.add(component3D);
    frame.setSize(200, 200);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);
  }  
}
