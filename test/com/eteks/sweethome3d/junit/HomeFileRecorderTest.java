/*
 * HomeFileRecorderTest.java 28 aout 2006
 * 
 * Copyright (c) 2006 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights
 * Reserved.
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import com.eteks.sweethome3d.io.DefaultCatalog;
import com.eteks.sweethome3d.io.HomeFileRecorder;
import com.eteks.sweethome3d.model.Catalog;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.HomeRecorder;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.Wall;

/**
 * Tests FileHome class.
 * @author Emmanuel Puybaret
 */
public class HomeFileRecorderTest extends TestCase {
  public void testWriteReadHome() throws RecorderException {
    // 1. Create an empty home
    Home home1 = new Home();
    // Add to home a wall and a piece of furniture
    Wall wall = new Wall(0, 10, 100, 80, 10);
    home1.addWall(wall);
    Catalog catalog = new DefaultCatalog();
    HomePieceOfFurniture piece = new HomePieceOfFurniture(
        catalog.getCategories().get(0).getFurniture().get(0));
    home1.addPieceOfFurniture(piece);
    
    // 2. Record home in a file named test.sh3d in current directory
    HomeRecorder recorder = new HomeFileRecorder();
    recorder.writeHome(home1,
        new File("test.sh3d").getAbsolutePath()); 
    // Check test.sh3d file exists
    assertTrue("File test.sh3d doesn't exist", recorder.exists(home1.getName()));
    
    // 3. Read test.sh3d file in a new home
    Home home2 = recorder.readHome(home1.getName());
    // Compare home content
    assertNotSame("Home not loaded", home1, home2);
    assertEquals("Home wall height", 
        home1.getWallHeight(), home2.getWallHeight());
    assertEquals("Home walls wrong count", 
        home1.getWalls().size(), home2.getWalls().size());
    assertEquals(wall, home2.getWalls().iterator().next());
    assertEquals("Home furniture wrong count", 
        home1.getFurniture().size(), home2.getFurniture().size());
    assertEquals(piece, home2.getFurniture().get(0));

    // Delete file
    if (!new File(home1.getName()).delete()) {
      fail("Couldn't delete file " + home1.getName());
    }
  }
  
  /**
   * Asserts <code>wall1</code> and <code>wall2</code> are different walls 
   * containing the same data. 
   */
  private void assertEquals(Wall wall1, Wall wall2) {
    assertNotSame("Wall not loaded", wall1, wall2);
    assertEquals("Different X start", wall1.getXStart(), wall2.getXStart());     
    assertEquals("Different Y start", wall1.getYStart(), wall2.getYStart());     
    assertEquals("Different X end", wall1.getXEnd(), wall2.getXEnd());     
    assertEquals("Different Y end", wall1.getYEnd(), wall2.getYEnd());     
    assertEquals("Different thickness", wall1.getThickness(), wall2.getThickness());
    if (wall1.getWallAtStart() == null) {
      assertEquals("Different wall at start", wall2.getWallAtStart(), null);
    } else {
      assertFalse("Different wall at start", wall2.getWallAtStart() == null);
      assertNotSame("Wall at start not loaded", wall1.getWallAtStart(), wall2.getWallAtEnd());
    }
    if (wall1.getWallAtEnd() == null) {
      assertEquals("Different wall at end", wall2.getWallAtEnd(), null);
    } else {
      assertFalse("Different wall at end", wall2.getWallAtEnd() == null);
      assertNotSame("Wall at end not loaded", wall1.getWallAtStart(), wall2.getWallAtEnd());
    }
  }

  /**
   * Asserts <code>piece1</code> and <code>piece2</code> are different pieces 
   * containing the same data.
   */
  private void assertEquals(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
    assertNotSame("Piece not loaded", piece1, piece2);
    assertEquals("Different X", piece1.getX(), piece2.getX());     
    assertEquals("Different Y", piece1.getY(), piece2.getY());     
    assertEquals("Different color", piece1.getColor(), piece2.getColor());     
    assertEquals("Different width", piece1.getWidth(), piece2.getWidth());     
    assertEquals("Different height", piece1.getHeight(), piece2.getHeight());     
    assertEquals("Different depth", piece1.getDepth(), piece2.getDepth());     
    assertEquals("Different name", piece1.getName(), piece2.getName());     
    assertNotSame("Piece icon not loaded", piece1.getIcon(), piece2.getIcon());
    assertContentEquals("Different icon content", piece1.getIcon(), piece2.getIcon());     
    assertNotSame("Piece model not loaded", piece1.getModel(), piece2.getModel());
    assertContentEquals("Different model content", piece1.getModel(), piece2.getModel());     
  }

  /**
   * Asserts <code>content1</code> and <code>content2</code> are equals.
   */
  private void assertContentEquals(String message, Content content1, Content content2) {
    InputStream stream1 = null;
    InputStream stream2 = null;
    try {
      stream1 = new BufferedInputStream(content1.openStream());
      stream2 = new BufferedInputStream(content2.openStream());
      for (int b; (b = stream1.read()) != -1; ) {
        assertEquals(message, b, stream2.read());   
      }
      assertEquals(message, -1, stream2.read());
    } catch (IOException ex) {
      fail("Can't access to content");
    } finally {
      try {
        stream1.close();
        stream2.close();
      } catch (IOException ex) {
        fail("Can't close content stream");
      }
    }
  }
}
