/*
 * TestDefaultFurniture.java 6 avr. 2006
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

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;

import junit.framework.TestCase;

import com.eteks.sweethome3d.model.DefaultFurniture;
import com.eteks.sweethome3d.model.PieceOfFurniture;
import com.eteks.sweethome3d.swing.DefaultFurnitureTree;

/**
 * Tests default furniture tree component.
 * @author Emmanuel Puybaret
 */
public class DefaultFurnitureTest extends TestCase {

  public DefaultFurnitureTest(String name) {
    super(name);
  }

  public void testDefaultFurnitureTreeCreation() throws NoSuchFieldException, IllegalAccessException {
    // Get the default furniture for English locale
    Locale.setDefault(Locale.ENGLISH); 
    DefaultFurniture defaultFurniture = DefaultFurniture.getInstance();

    // Get the name of the first category 
    Set<String> categories = defaultFurniture.getCategories();
    String firstCategoryEnglishName = categories.iterator().next(); 
    // Get the name of the first piece of furniture
    Set<PieceOfFurniture> categoryFurniture = 
        defaultFurniture.getFurniture(firstCategoryEnglishName);
    PieceOfFurniture firstPiece = categoryFurniture.iterator().next(); 
    String firstPieceEnglishName = firstPiece.getName();
    
    // Cancel instance singleton with reflexion before changing default locale 
    Field instanceField = DefaultFurniture.class.getDeclaredField("instance");
    instanceField.setAccessible(true);
    instanceField.set(DefaultFurniture.class, null);

    Locale.setDefault(Locale.FRENCH);
    defaultFurniture = DefaultFurniture.getInstance();
    // Get the french names of the first category and its first piece of furniture
    String firstCategoryFrenchName = 
        defaultFurniture.getCategories().iterator().next();
    firstPiece = defaultFurniture.
        getFurniture(firstCategoryFrenchName).iterator().next();
    String firstPieceFrenchName = firstPiece.getName();
    // Compare categories and furniture names in English and French locale
    assertFalse("Same name for first category",
        firstCategoryEnglishName.equals(firstCategoryFrenchName));
    assertFalse("Same name for first piece",
        firstPieceEnglishName.equals(firstPieceFrenchName)); 

    // Create a tree from default furniture
    JTree tree = new DefaultFurnitureTree();

    // Select first piece in tree
    tree.expandRow(0);
    tree.setSelectionRow(1);
    assertEquals("No piece of furniture selected", 
        1, tree.getSelectionRows().length);
    assertEquals("First piece not selected", 
        1, tree.getSelectionRows() [0]);

    // Select second piece in tree 
    tree.addSelectionRow(2);
    assertFalse("More than one selected piece", 
        tree.getSelectionRows().length > 1);
    assertEquals("Second piece not selected",
        2, tree.getSelectionRows() [0]);
  }
  
  public static void main(String [] args) {
    DefaultFurnitureTree tree = new DefaultFurnitureTree();
    JFrame frame = new JFrame("Default Furniture Test");
    frame.add(new JScrollPane(tree));
    frame.pack();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);
  }
}
