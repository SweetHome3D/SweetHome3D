/*
 * WallPanelTest.java 29 mai 07
 *
 * Copyright (c) 2007 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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
package com.eteks.sweethome3d.junit;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Locale;

import javax.swing.JSpinner;

import junit.framework.TestCase;

import com.eteks.sweethome3d.io.DefaultUserPreferences;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.TextureImage;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.swing.FileContentManager;
import com.eteks.sweethome3d.swing.SwingViewFactory;
import com.eteks.sweethome3d.viewcontroller.ViewFactory;
import com.eteks.sweethome3d.viewcontroller.WallController;
import com.eteks.sweethome3d.viewcontroller.WallView;

/**
 * Tests {@link com.eteks.sweethome3d.swing.HomeFurniturePanel home piece of furniture panel}.
 * @author Emmanuel Puybaret
 */
public class WallPanelTest extends TestCase {
  public void testHomePieceOfFurniturePanel() throws NoSuchFieldException, IllegalAccessException {
    // 1. Create default preferences for a user that uses centimeter
    Locale.setDefault(Locale.FRANCE);
    UserPreferences preferences = new DefaultUserPreferences();
    ViewFactory viewFactory = new SwingViewFactory();
    // Create a home and add a selected wall to it
    Home home = new Home();
    Wall wall1 = new Wall(0.1f, 0.2f, 100.1f, 100.2f, 7.5f);
    home.addWall(wall1);
    wall1.setLeftSideColor(10);
    wall1.setRightSideColor(20);
    home.setSelectedItems(Arrays.asList(new Wall [] {wall1}));
    
    // 2. Create a wall panel to edit the selected wall
    WallView wallView = new WallController(home, preferences, viewFactory, null, null).getView();
    // Check values stored by wall panel components are equal to the ones set
    assertWallPanelEquals(wall1.getXStart(), wall1.getYStart(),
        wall1.getXEnd(), wall1.getYEnd(),
        (float)Point2D.distance(wall1.getXStart(), wall1.getYStart(),
            wall1.getXEnd(), wall1.getYEnd()),
        wall1.getThickness(), home.getWallHeight(), wall1.getHeightAtEnd(),
        wall1.getLeftSideColor(), wall1.getLeftSideTexture(), 
        wall1.getRightSideColor(), wall1.getRightSideTexture(), wallView);    
 
    // 3. Modify wall right side texture with first available texture
    TextureImage firstTexture = preferences.getTexturesCatalog().getCategories().get(0).getTexture(0);
    wall1.setRightSideColor(null);
    wall1.setRightSideTexture(new HomeTexture(firstTexture));
    wallView = new WallController(home, preferences, viewFactory, null, null).getView();
    assertWallPanelEquals(wall1.getXStart(), wall1.getYStart(),
        wall1.getXEnd(), wall1.getYEnd(),
        (float)Point2D.distance(wall1.getXStart(), wall1.getYStart(),
            wall1.getXEnd(), wall1.getYEnd()),
        wall1.getThickness(), home.getWallHeight(), wall1.getHeightAtEnd(),
        wall1.getLeftSideColor(), wall1.getLeftSideTexture(), 
        null, wall1.getRightSideTexture(), wallView);
    
    // 4. Increase length in dialog
    JSpinner lengthSpinner = 
        (JSpinner)TestUtilities.getField(wallView, "lengthSpinner");
    lengthSpinner.setValue((Float)lengthSpinner.getValue() + 20f);
    // Check wall end coordinates changed accordingly
    assertEquals("Wrong X end", wall1.getXEnd() + 20f * (float)Math.cos(Math.PI / 4), wallView.getWallXEnd());
    assertEquals("Wrong Y end", wall1.getYEnd() + 20f * (float)Math.sin(Math.PI / 4), wallView.getWallYEnd());
    
    // 5. Add a second selected wall to home
    Wall wall2 = new Wall(0.1f, 0.3f, 200.1f, 200.2f, 5f);
    home.addWall(wall2);
    wall2.setHeight(300f);
    wall2.setLeftSideColor(10);
    wall2.setRightSideColor(50);
    home.setSelectedItems(Arrays.asList(new Wall [] {wall1, wall2}));
    // Check if wall panel edits null values if walls thickness or colors are the same
    wallView = new WallController(home, preferences, viewFactory, null, null).getView();
    // Check values stored by furniture panel components are equal to the ones set
    assertWallPanelEquals(null, null, null,
        null, null, null, null, null, 10, null, null, null, wallView);
  }
  
  /**
   * Assert values in parameter are the same as the ones 
   * stored in <code>panel</code> components.
   */
  private void assertWallPanelEquals(Float xStart, Float yStart, Float xEnd, Float yEnd, Float length,
                                     Float thickness, Float height, Float heightAtEnd,
                                     Integer leftColor, TextureImage leftTexture,
                                     Integer rightColor, TextureImage rightTexture,
                                     WallView wallView) {
    assertEquals("Wrong X start", xStart, wallView.getWallXStart());
    assertEquals("Wrong Y start", yStart, wallView.getWallYStart());
    assertEquals("Wrong X end", xEnd, wallView.getWallXEnd());
    assertEquals("Wrong Y end", yEnd, wallView.getWallYEnd());
    if (wallView.getWallXStart() != null && wallView.getWallYStart() != null
        && wallView.getWallXEnd() != null && wallView.getWallYEnd() != null) {
      assertEquals("Wrong length", length, (float)Point2D.distance(wallView.getWallXStart(), wallView.getWallYStart(),
          wallView.getWallXEnd(), wallView.getWallYEnd()));
    } else {
      assertEquals("Wrong length", length, null);
    }
    assertEquals("Wrong thickness", thickness, wallView.getWallThickness());
    assertEquals("Wrong height", height, wallView.getWallHeight());
    assertEquals("Wrong heightAtEnd", height, wallView.getWallHeightAtEnd());
    assertEquals("Wrong left side color", leftColor, wallView.getWallLeftSideColor());
    if (leftTexture == null) {
      assertEquals("Wrong left side texture", leftTexture, wallView.getWallLeftSideTexture());
    } else {
      assertEquals("Wrong left side texture", leftTexture.getName(), wallView.getWallLeftSideTexture().getName());
    }
    assertEquals("Wrong right side color", rightColor, wallView.getWallRightSideColor());
    if (leftTexture == null) {
      assertEquals("Wrong right side texture", rightTexture, wallView.getWallRightSideTexture());
    } else {
      assertEquals("Wrong right side texture", rightTexture.getName(), wallView.getWallRightSideTexture().getName());
    }
  }

  public static void main(String [] args) {
    // Create a selected wall in a home and display it in a wall panel
    Home home = new Home();
    Wall wall1 = new Wall(0.1f, 0.2f, 100.1f, 100.2f, 7.5f);
    home.addWall(wall1);
    wall1.setLeftSideColor(null);
    wall1.setRightSideColor(0xFFFF00);
    home.setSelectedItems(Arrays.asList(new Wall [] {wall1}));
    
    new WallController(home, new DefaultUserPreferences(), 
          new SwingViewFactory(), new FileContentManager(), null).displayView(null);
  }
}
