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
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URL;

import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Vector3f;

import junit.framework.TestCase;

import com.eteks.sweethome3d.io.DefaultUserPreferences;
import com.eteks.sweethome3d.io.HomeFileRecorder;
import com.eteks.sweethome3d.j3d.OBJLoader;
import com.eteks.sweethome3d.j3d.OBJWriter;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.swing.SwingViewFactory;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.HomeController;
import com.eteks.sweethome3d.viewcontroller.ViewFactory;
import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.geometry.Sphere;

/**
 * Test {@link com.eteks.sweethome3d.j3d.OBJWriter OBJ writer} features.
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
  public void testHomeExportToOBJ() throws RecorderException, IOException {
    // 1. Create an empty home and a 3D controller
    ViewFactory viewFactory = new SwingViewFactory();
    UserPreferences preferences = new DefaultUserPreferences();
    Home home = new Home();
    HomeController homeController = 
        new HomeController(home, preferences, viewFactory);
    
    // 2. Add to home a wall and a piece of furniture
    home.addWall(new Wall(0, 0, 0, 1000, 10, home.getWallHeight()));
    HomePieceOfFurniture piece = new HomePieceOfFurniture(
        preferences.getFurnitureCatalog().getCategory(0).getPieceOfFurniture(0));
    home.addPieceOfFurniture(piece);
    piece.setX(500);
    piece.setY(500);
    assertEquals("Incorrect wall count", 1, home.getWalls().size());
    assertEquals("Incorrect furniture count", 1, home.getFurniture().size());

    // 3. Export home to OBJ file
    File dir = File.createTempFile("Tmp@#", ".obj1");
    dir.delete();
    assertTrue("Can't create temporary directory", dir.mkdir());    
    File objFile = new File(dir, "Test.obj");
    File mtlFile = new File(dir, "Test.mtl");
    
    homeController.getView().exportToOBJ(objFile.toString());
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
  
  /**
   * Tests if the content of an OBJ file is as expected after exporting a complex home
   * (with holes for windows and staircase).
   */
  public void testExportToOBJContent() throws RecorderException, IOException {
    // 1. Read an existing file
    String testFile = OBJWriterTest.class.getResource("resources/holes.sh3d").getFile();
    if (OperatingSystem.isWindows()) {
      testFile = testFile.substring(1).replace("%20", " ");
    }
    Home home = new HomeFileRecorder().readHome(testFile);
    
    // 2. Export home to OBJ file
    File dir = File.createTempFile("Tmp@#", ".obj2");
    dir.delete();
    assertTrue("Can't create temporary directory", dir.mkdir());    
    File objFile = new File(dir, "holes.obj");
    File mtlFile = new File(dir, "holes.mtl");
    
    ViewFactory viewFactory = new SwingViewFactory();
    UserPreferences preferences = new DefaultUserPreferences() {
      @Override
      public String getLocalizedString(Class<?> resourceClass, String resourceKey, Object ... resourceParameters) {
        if ("exportToOBJ.header".equals(resourceKey)) {
          return ""; // Avoid header with a date to simplify comparison
        } else {
          return super.getLocalizedString(resourceClass, resourceKey, resourceParameters);
        }
      }
    };
    HomeController homeController = new HomeController(home, preferences, viewFactory);
    homeController.getView().exportToOBJ(objFile.toString());
    
    assertEquals("Not same line count in OBJ file", 475, getLineCount(objFile.toURI().toURL()));
    assertEquals("Not same line count in MTL file", 43, getLineCount(mtlFile.toURI().toURL()));
    // Read file to check if its content is correct
    new OBJLoader().load(objFile.getAbsolutePath());
    
    for (File file : dir.listFiles()) {
      if (!file.delete()) {
        fail("Couldn't delete test file " + file);
      }
    }
    if (!dir.delete()) {
      fail("Couldn't delete test dir");
    }
  }

  /**
   * Returns the line count in the given URL.
   */
  private int getLineCount(URL contentUrl) throws IOException {
    LineNumberReader in = new LineNumberReader(new InputStreamReader(contentUrl.openStream(), "ISO-8859-1"));
    while (in.readLine() != null) {
    }
    in.close();
    return in.getLineNumber();
  }
}
