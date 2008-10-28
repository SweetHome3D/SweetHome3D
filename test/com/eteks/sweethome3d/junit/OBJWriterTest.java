/*
 * OBJWriterTest.java 18 sept. 2008
 * 
 * Copyright (c) 2008 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.eteks.sweethome3d.junit;

import java.io.File;
import java.io.IOException;

import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Vector3f;

import junit.framework.TestCase;

import com.eteks.sweethome3d.io.DefaultUserPreferences;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.swing.OBJWriter;
import com.eteks.sweethome3d.swing.SwingViewFactory;
import com.eteks.sweethome3d.viewcontroller.HomeController3D;
import com.eteks.sweethome3d.viewcontroller.ViewFactory;
import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.geometry.Sphere;

/**
 * Test {@link com.eteks.sweethome3d.swing.OBJWriter OBJ writer} features.
 * @author Emmanuel Puybaret
 */
public class OBJWriterTest extends TestCase {
  /**
   * Simple test of OBJWriter class with Java 3D objects.
   */
  public void testOBJWriter() throws IOException {
    // 1. Open the OBJ file "Test.obj"
    OBJWriter writer = new OBJWriter("Test@#.obj", "Test", 3);
    assertTrue("Test@#.obj not created", new File("Test@#.obj").exists());
    
    // 2. Write a box at center 
    writer.writeNode(new Box());
    
    // Write a sphere centered at (2, 0, 2) 
    Transform3D translation = new Transform3D();
    translation.setTranslation(new Vector3f(2f, 0, 2f));
    TransformGroup translationGroup = new TransformGroup(translation);
    
    translationGroup.addChild(new Sphere());
    writer.writeNode(translationGroup);
    
    // 3. Close file
    writer.close();
    assertTrue("Test@#.mtl not created", new File("Test@#.mtl").exists());
    
    if (!new File("Test@#.obj").delete()
        || !new File("Test@#.mtl").delete()) {
      fail("Couldn't delete test files");
    }
  }
  
  /**
   * Tests home export to OBJ format.
   */
  public void testHomeExportToOBJ() throws RecorderException {
    // 1. Create an empty home and a 3D controller
    ViewFactory viewFactory = new SwingViewFactory();
    UserPreferences preferences = new DefaultUserPreferences();
    Home home = new Home();
    HomeController3D homeController3D = 
        new HomeController3D(home, preferences, viewFactory, null, null);
    
    // 2. Add to home a wall and a piece of furniture
    home.addWall(new Wall(0, 0, 0, 1000, 10));
    HomePieceOfFurniture piece = new HomePieceOfFurniture(
        preferences.getFurnitureCatalog().getCategory(0).getPieceOfFurniture(0));
    home.addPieceOfFurniture(piece);
    piece.setX(500);
    piece.setY(500);
    assertEquals("Incorrect wall count", 1, home.getWalls().size());
    assertEquals("Incorrect furniture count", 1, home.getFurniture().size());

    // 3. Export home to OBJ file
    File dir = new File("Tmp@#");
    dir.mkdir();
    File objFile = new File(dir, "Test.obj");
    File mtlFile = new File(dir, "Test.mtl");
    assertFalse(objFile + " exists", objFile.exists());
    assertFalse(mtlFile + " exists", mtlFile.exists());
    
    homeController3D.exportToOBJ(objFile.toString());
    assertTrue(objFile + " wasn't created", objFile.exists());
    assertTrue(mtlFile + " wasn't created", mtlFile.exists());
    
    for (File file : dir.listFiles()) {
      if (!file.delete()) {
        fail("Couldn't delete test file " + file);
      }
    }
    if (!dir.delete()) {
      fail("Couldn't delete test dir");
    }
  }
}
