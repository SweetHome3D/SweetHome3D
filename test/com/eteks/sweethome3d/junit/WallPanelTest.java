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
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.ViewFactory;
import com.eteks.sweethome3d.viewcontroller.WallController;

/**
 * Tests {@linkplain com.eteks.sweethome3d.swing.HomeFurniturePanel home piece of furniture panel}.
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
    WallController wallController = new WallController(home, preferences, viewFactory, null, null);
    // Check values stored by wall panel components are equal to the ones set
    assertWallControllerEquals(wall1.getXStart(), wall1.getYStart(),
        wall1.getXEnd(), wall1.getYEnd(),
        (float)Point2D.distance(wall1.getXStart(), wall1.getYStart(),
            wall1.getXEnd(), wall1.getYEnd()),
        wall1.getThickness(), home.getWallHeight(), wall1.getHeightAtEnd(),
        wall1.getLeftSideColor(), wall1.getLeftSideTexture(), 
        wall1.getRightSideColor(), wall1.getRightSideTexture(), wallController);    
 
    // 3. Modify wall right side texture with first available texture
    TextureImage firstTexture = preferences.getTexturesCatalog().getCategories().get(0).getTexture(0);
    wall1.setRightSideColor(null);
    wall1.setRightSideTexture(new HomeTexture(firstTexture));
    wallController = new WallController(home, preferences, viewFactory, null, null);
    assertWallControllerEquals(wall1.getXStart(), wall1.getYStart(),
        wall1.getXEnd(), wall1.getYEnd(),
        (float)Point2D.distance(wall1.getXStart(), wall1.getYStart(),
            wall1.getXEnd(), wall1.getYEnd()),
        wall1.getThickness(), home.getWallHeight(), wall1.getHeightAtEnd(),
        wall1.getLeftSideColor(), wall1.getLeftSideTexture(), 
        null, wall1.getRightSideTexture(), wallController);
    
    // 4. Increase length in dialog
    DialogView wallView = wallController.getView();
    JSpinner distanceToEndPointSpinner = 
        (JSpinner)TestUtilities.getField(wallView, "distanceToEndPointSpinner");
    distanceToEndPointSpinner.setValue((Float)distanceToEndPointSpinner.getValue() + 20f);
    // Check wall end coordinates changed accordingly
    assertTrue("Wrong X end", Math.abs(wall1.getXEnd() + 20f * (float)Math.cos(Math.PI / 4) - wallController.getXEnd()) < 1E-5);
    assertTrue("Wrong Y end", Math.abs(wall1.getYEnd() + 20f * (float)Math.sin(Math.PI / 4) - wallController.getYEnd()) < 1E-5);
    
    // 5. Add a second selected wall to home
    Wall wall2 = new Wall(0.1f, 0.3f, 200.1f, 200.2f, 5f);
    home.addWall(wall2);
    wall2.setHeight(300f);
    wall2.setLeftSideColor(10);
    wall2.setRightSideColor(50);
    home.setSelectedItems(Arrays.asList(new Wall [] {wall1, wall2}));
    // Check if wall panel edits null values if walls thickness or colors are the same
    wallController = new WallController(home, preferences, viewFactory, null, null);
    // Check values stored by furniture panel components are equal to the ones set
    assertWallControllerEquals(0.1f, null, null,
        null, null, null, null, null, 10, null, null, null, wallController);
  }
  
  /**
   * Assert values in parameter are the same as the ones 
   * stored in <code>controller</code>.
   */
  private void assertWallControllerEquals(Float xStart, Float yStart, Float xEnd, Float yEnd, Float length,
                                     Float thickness, Float height, Float heightAtEnd,
                                     Integer leftColor, TextureImage leftTexture,
                                     Integer rightColor, TextureImage rightTexture,
                                     WallController wallController) {
    assertEquals("Wrong X start", xStart, wallController.getXStart());
    assertEquals("Wrong Y start", yStart, wallController.getYStart());
    assertEquals("Wrong X end", xEnd, wallController.getXEnd());
    assertEquals("Wrong Y end", yEnd, wallController.getYEnd());
    if (wallController.getXStart() != null && wallController.getYStart() != null
        && wallController.getXEnd() != null && wallController.getYEnd() != null) {
      assertEquals("Wrong length", length, (float)Point2D.distance(wallController.getXStart(), wallController.getYStart(),
          wallController.getXEnd(), wallController.getYEnd()));
    } else {
      assertEquals("Wrong length", length, null);
    }
    assertEquals("Wrong thickness", thickness, wallController.getThickness());
    assertEquals("Wrong height", height, wallController.getRectangularWallHeight());
    assertEquals("Wrong heightAtEnd", height, wallController.getSlopingWallHeightAtEnd());
    assertEquals("Wrong left side color", leftColor, wallController.getLeftSideColor());
    if (leftTexture == null) {
      assertEquals("Wrong left side texture", leftTexture, wallController.getLeftSideTextureController().getTexture());
    } else {
      assertEquals("Wrong left side texture", leftTexture.getName(), wallController.getLeftSideTextureController().getTexture().getName());
    }
    assertEquals("Wrong right side color", rightColor, wallController.getRightSideColor());
    if (leftTexture == null) {
      assertEquals("Wrong right side texture", rightTexture, wallController.getRightSideTextureController().getTexture());
    } else {
      assertEquals("Wrong right side texture", rightTexture.getName(), wallController.getRightSideTextureController().getTexture().getName());
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
    
    DefaultUserPreferences preferences = new DefaultUserPreferences();
    new WallController(home, preferences, 
          new SwingViewFactory(), new FileContentManager(preferences), null).displayView(null);
  }
}
