/*
 * BoxGeometryTest.java 22 août 2006
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

import java.io.IOException;
import java.net.URL;

import javax.media.j3d.Background;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.BranchGroup;

import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.objectfile.ObjectFile;

/**
 * Displays an <code>.obj</code> file with Java 3D.
 * @author Emmanuel Puybaret
 */
public class ObjectFileTest {
  private static BranchGroup loadObjectFile(URL file) throws IOException {
    ObjectFile loader = new ObjectFile();
    Scene scene = loader.load(file);
    return scene.getSceneGroup();
  }
  
  public static void main (String [] args) throws IOException {
    // Create the tree of the displayed scene 
    BranchGroup root = new BranchGroup();
    // Add a child that displays an .obj file
    root.addChild(loadObjectFile(
        ObjectFileTest.class.getResource("resources/plant.obj")));
    // Add a light gray background to view the loaded model, 
    // that is black with no lights in the scene
    Background background = new Background(0.9f, 0.9f, 0.9f);
    background.setApplicationBounds(new BoundingBox());
    root.addChild(background);

    Java3DTest.viewSceneTree(root);
  }  
}
