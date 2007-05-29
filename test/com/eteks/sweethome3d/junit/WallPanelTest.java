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

import java.util.Locale;

import junit.framework.TestCase;

import com.eteks.sweethome3d.io.DefaultUserPreferences;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.PieceOfFurniture;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.swing.HomeFurniturePanel;
import com.eteks.sweethome3d.swing.WallPanel;

/**
 * Tests {@link com.eteks.sweethome3d.swing.HomeFurniturePanel home piece of furniture panel}.
 * @author Emmanuel Puybaret
 */
public class WallPanelTest extends TestCase {
  public void testHomePieceOfFurniturePanel() {
    // 1. Create default preferences for a user that uses centimeter
    Locale.setDefault(Locale.FRANCE);
    UserPreferences preferences = new DefaultUserPreferences();
    // Create a wall
    Wall wall = new Wall(0.1f, 0.2f, 100.1f, 100.2f, 7.5f);
    
    // 2. Create a wall panel to edit it
    WallPanel panel = new WallPanel(preferences);
    panel.setWallStartPoint(wall.getXStart(), wall.getYStart());
    panel.setWallEndPoint(wall.getXEnd(), wall.getYEnd());
    panel.setWallThickness(wall.getThickness());
    panel.setWallLeftSideColor(wall.getLeftSideColor());
    panel.setWallRightSideColor(wall.getRightSideColor());
    // Check values stored by wall panel components are equal to the ones set
    assertFurniturePanelEquals(panel, wall.getXStart(), wall.getYStart(),
        wall.getXEnd(), wall.getYEnd(),
        wall.getThickness(),
        wall.getLeftSideColor(), wall.getRightSideColor());

    // 3. Check if furniture panel is able to edit null values for thickness and colors
    panel.setWallThickness(null);
    panel.setWallLeftSideColor(null);
    panel.setWallRightSideColor(null);
    // Check values stored by furniture panel components are equal to the ones set
    assertFurniturePanelEquals(panel, wall.getXStart(), wall.getYStart(),
        wall.getXEnd(), wall.getYEnd(), null, null, null);
  }
  
  /**
   * Assert values in parameter are the same as the ones 
   * stored in <code>panel</code> components.
   */
  private void assertFurniturePanelEquals(WallPanel panel, 
                                          float xStart, float yStart, float xEnd, float yEnd, 
                                          Float thickness, 
                                          Integer leftColor, Integer rightColor) {
    assertEquals(panel.getWallXStart(), xStart);
    assertEquals(panel.getWallYStart(), yStart);
    assertEquals(panel.getWallXEnd(), xEnd);
    assertEquals(panel.getWallYEnd(), yEnd);
    assertEquals(panel.getWallThickness(), thickness);
    assertEquals(panel.getWallLeftSideColor(), leftColor);
    assertEquals(panel.getWallRightSideColor(), rightColor);
  }

  public static void main(String [] args) {
    Locale.setDefault(Locale.US);
    WallPanel wallPanel = new WallPanel(new DefaultUserPreferences());
//    wallPanel.setWallPointsVisible(false);
    wallPanel.showDialog(null);
  }
}
