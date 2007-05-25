/*
 * HomeFurniturePanelTest.java 16 mai 07
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

import com.eteks.sweethome3d.io.DefaultUserPreferences;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.PieceOfFurniture;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.swing.HomeFurniturePanel;

import junit.framework.TestCase;

/**
 * Tests {@link com.eteks.sweethome3d.swing.HomeFurniturePanel home piece of furniture panel}.
 * @author Emmanuel Puybaret
 */
public class HomeFurniturePanelTest extends TestCase {
  public void testHomePieceOfFurniturePanel() {
    // 1. Create default preferences for a user that uses centimeter
    Locale.setDefault(Locale.FRANCE);
    UserPreferences preferences = new DefaultUserPreferences();
    // Create a home piece of furniture from first piece in catalog
    PieceOfFurniture firstPiece = preferences.getCatalog().
        getCategories().get(0).getFurniture().get(0);
    HomePieceOfFurniture piece = new HomePieceOfFurniture(firstPiece); 
    
    // 2. Create a home piece of furniture panel to edit piece
    HomeFurniturePanel panel = new HomeFurniturePanel(preferences);
    panel.setFurnitureName(piece.getName());
    panel.setFurnitureLocation(piece.getX(), piece.getY());
    panel.setFurnitureAngle(piece.getAngle());
    panel.setFurnitureDimension(piece.getWidth(), piece.getDepth(), piece.getHeight());
    panel.setFurnitureColor(piece.getColor());
    panel.setFurnitureVisible(piece.isVisible());
    // Check values stored by furniture panel components are equal to the ones set
    assertFurniturePanelEquals(panel, piece.getName(),
        piece.getX(), piece.getY(), piece.getAngle(),
        piece.getWidth(), piece.getDepth(), piece.getHeight(),
        piece.getColor(), piece.isVisible());

    // 3. Check if furniture panel is able to edit null values
    panel.setFurnitureName(null);
    panel.setFurnitureLocation(null, null);
    panel.setFurnitureAngle(null);
    panel.setFurnitureDimension(null, null, null);
    panel.setFurnitureColor(null);
    panel.setFurnitureVisible(null);
    // Check values stored by furniture panel components are equal to the ones set
    assertFurniturePanelEquals(panel, null, null, null, null, null, null, null, null, null);
  }
  
  /**
   * Assert values in parameter are the same as the ones 
   * stored in <code>panel</code> components.
   */
  private void assertFurniturePanelEquals(HomeFurniturePanel panel, 
                                          String name, Float x, Float y, Float angle, 
                                          Float width, Float depth, Float height, 
                                          Integer color, Boolean visible) {
    assertEquals(panel.getFurnitureName(), name);
    assertEquals(panel.getFurnitureX(), x);
    assertEquals(panel.getFurnitureY(), y);
    assertEquals(panel.getFurnitureAngle(), angle);
    assertEquals(panel.getFurnitureWidth(), width);
    assertEquals(panel.getFurnitureDepth(), depth);
    assertEquals(panel.getFurnitureHeight(), height);
    assertEquals(panel.getFurnitureColor(), color);
    assertEquals(panel.isFurnitureVisible(), visible);
  }

  public static void main(String [] args) {
    new HomeFurniturePanel(new DefaultUserPreferences()).showDialog(null);
  }
}
