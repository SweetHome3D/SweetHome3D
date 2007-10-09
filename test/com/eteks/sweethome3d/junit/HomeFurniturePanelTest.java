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

import java.util.Arrays;
import java.util.Locale;

import junit.framework.TestCase;

import com.eteks.sweethome3d.io.DefaultUserPreferences;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.PieceOfFurniture;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.swing.HomeFurniturePanel;

/**
 * Tests {@link com.eteks.sweethome3d.swing.HomeFurniturePanel home piece of furniture panel}.
 * @author Emmanuel Puybaret
 */
public class HomeFurniturePanelTest extends TestCase {
  public void testHomePieceOfFurniturePanel() {
    // 1. Create default preferences for a user that uses centimeter
    Locale.setDefault(Locale.FRANCE);
    UserPreferences preferences = new DefaultUserPreferences();
    // Create a home and add a selected piece of furniture to it
    Home home = new Home();
    PieceOfFurniture firstPiece = preferences.getFurnitureCatalog().
        getCategories().get(0).getFurniture().get(0);
    HomePieceOfFurniture piece1 = new HomePieceOfFurniture(firstPiece); 
    home.addPieceOfFurniture(piece1);
    home.setSelectedItems(Arrays.asList(new HomePieceOfFurniture [] {piece1}));

    // 2. Create a home piece of furniture panel to edit piece
    HomeFurniturePanel panel = new HomeFurniturePanel(home, preferences, null);
    // Check values stored by furniture panel components are equal to the ones set
    assertFurniturePanelEquals(piece1.getName(), piece1.getX(),
        piece1.getY(), piece1.getElevation(), piece1.getAngle(), piece1.getWidth(),
        piece1.getDepth(), piece1.getHeight(), piece1.getColor(),
        piece1.isVisible(), piece1.isModelMirrored(), panel);

    // 3. Add a second selected piece to home
    HomePieceOfFurniture piece2 = new HomePieceOfFurniture(firstPiece); 
    home.addPieceOfFurniture(piece2);
    home.setPieceOfFurnitureLocation(piece2, piece1.getX(), piece1.getY() + 10);
    home.setPieceOfFurnitureElevation(piece2, piece1.getElevation() + 10);
    home.setPieceOfFurnitureSize(piece2, 
        piece1.getWidth(), piece1.getDepth() + 10, piece1.getHeight() + 10);
    home.setPieceOfFurnitureColor(piece2, 0xFF00FF);
    home.setPieceOfFurnitureVisible(piece2, !piece1.isVisible());
    home.setPieceOfFurnitureModelMirrored(piece2, !piece1.isModelMirrored());
    home.setSelectedItems(Arrays.asList(new HomePieceOfFurniture [] {piece1, piece2}));
    // Check if furniture panel edits null values 
    // if some furniture properties are the same
    panel = new HomeFurniturePanel(home, preferences, null);
    // Check values stored by furniture panel components are equal to the ones set
    assertFurniturePanelEquals(piece1.getName(), piece1.getX(), null, null, piece1.getAngle(), 
        piece1.getWidth(), null, null, null, null, null, panel);
  }
  
  /**
   * Assert values in parameter are the same as the ones 
   * stored in <code>panel</code> components.
   */
  private void assertFurniturePanelEquals(String name, Float x, Float y, Float elevation, Float angle, 
                                          Float width, Float depth, Float height, Integer color, 
                                          Boolean visible, Boolean modelMirrored, 
                                          HomeFurniturePanel panel) {
    assertEquals("Wrong name", name, panel.getFurnitureName());
    assertEquals("Wrong X", x, panel.getFurnitureX());
    assertEquals("Wrong Y", y, panel.getFurnitureY());
    assertEquals("Wrong elevation", elevation, panel.getFurnitureElevation());
    assertEquals("Wrong angle", angle, panel.getFurnitureAngle());
    assertEquals("Wrong width", width, panel.getFurnitureWidth());
    assertEquals("Wrong depth", depth, panel.getFurnitureDepth());
    assertEquals("Wrong height", height, panel.getFurnitureHeight());
    assertEquals("Wrong color", color, panel.getFurnitureColor());
    assertEquals("Wrong visibility", visible, panel.isFurnitureVisible());
    assertEquals("Wrong model mirrored", modelMirrored, panel.isFurnitureModelMirrored());
  }

  public static void main(String [] args) {
    // Create a selected piece of furniture in a home and display it in a furniture panel
    UserPreferences preferences = new DefaultUserPreferences();
    Home home = new Home();
    PieceOfFurniture firstPiece = preferences.getFurnitureCatalog().
        getCategories().get(0).getFurniture().get(0);
    HomePieceOfFurniture piece1 = new HomePieceOfFurniture(firstPiece); 
    home.addPieceOfFurniture(piece1);
    home.setSelectedItems(Arrays.asList(new HomePieceOfFurniture [] {piece1}));
    
    HomeFurniturePanel furniturePanel = 
        new HomeFurniturePanel(home, preferences, null);
    furniturePanel.displayView();
  }
}
