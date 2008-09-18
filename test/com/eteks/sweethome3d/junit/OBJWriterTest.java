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

import com.eteks.sweethome3d.swing.OBJWriter;
import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.geometry.Sphere;

/**
 * Test {@link com.eteks.sweethome3d.swing.OBJWriter OBJ writer} features.
 * @author Emmanuel Puybaret
 */
public class OBJWriterTest extends TestCase {
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
}
