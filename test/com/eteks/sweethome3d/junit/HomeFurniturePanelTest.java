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
import com.eteks.sweethome3d.swing.SwingViewFactory;
import com.eteks.sweethome3d.viewcontroller.HomeFurnitureController;

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
    HomeFurnitureController controller = new HomeFurnitureController(home, preferences, new SwingViewFactory(), null);
    HomeFurniturePanel panel = new HomeFurniturePanel(preferences, controller);
    // Check values stored by furniture panel components are equal to the ones set
    assertFurnitureControllerEquals(piece1.getName(), piece1.getX(),
        piece1.getY(), piece1.getElevation(), piece1.getAngle(), piece1.getWidth(),
        piece1.getDepth(), piece1.getHeight(), piece1.getColor(),
        piece1.isVisible(), piece1.isModelMirrored(), controller);

    // 3. Add a second selected piece to home
    HomePieceOfFurniture piece2 = new HomePieceOfFurniture(firstPiece); 
    home.addPieceOfFurniture(piece2);
    piece2.setX(piece1.getX());
    piece2.setY(piece1.getY() + 10);
    piece2.setElevation(piece1.getElevation() + 10);
    piece2.setWidth(piece1.getWidth());
    piece2.setDepth(piece1.getDepth() + 10);
    piece2.setHeight(piece1.getHeight() + 10);
    piece2.setColor(0xFF00FF);
    piece2.setVisible(!piece1.isVisible());
    piece2.setModelMirrored(!piece1.isModelMirrored());
    home.setSelectedItems(Arrays.asList(new HomePieceOfFurniture [] {piece1, piece2}));
    // Check if furniture panel edits null values 
    // if some furniture properties are the same
    controller = new HomeFurnitureController(home, preferences, new SwingViewFactory(), null);
    panel = new HomeFurniturePanel(preferences, controller);
    // Check values stored by furniture panel components are equal to the ones set
    assertFurnitureControllerEquals(piece1.getName(), piece1.getX(), null, null, piece1.getAngle(), 
        piece1.getWidth(), null, null, null, null, null, controller);
  }
  
  /**
   * Assert values in parameter are the same as the ones 
   * stored in <code>controller</code>.
   */
  private void assertFurnitureControllerEquals(String name, Float x, Float y, Float elevation, Float angle, 
                                          Float width, Float depth, Float height, Integer color, 
                                          Boolean visible, Boolean modelMirrored, 
                                          HomeFurnitureController controller) {
    assertEquals("Wrong name", name, controller.getName());
    assertEquals("Wrong X", x, controller.getX());
    assertEquals("Wrong Y", y, controller.getY());
    assertEquals("Wrong elevation", elevation, controller.getElevation());
    assertEquals("Wrong angle", angle, controller.getAngleInDegrees());
    assertEquals("Wrong width", width, controller.getWidth());
    assertEquals("Wrong depth", depth, controller.getDepth());
    assertEquals("Wrong height", height, controller.getHeight());
    assertEquals("Wrong color", color, controller.getColor());
    assertEquals("Wrong visibility", visible, controller.getVisible());
    assertEquals("Wrong model mirrored", modelMirrored, controller.getModelMirrored());
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
    
    HomeFurnitureController controller = new HomeFurnitureController(home, preferences, new SwingViewFactory(), null);
    HomeFurniturePanel furniturePanel = 
        new HomeFurniturePanel(preferences, controller);
    furniturePanel.displayView(null);
  }
}
