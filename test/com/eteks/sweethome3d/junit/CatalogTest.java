/*
 * CatalogTest.java 6 avr. 2006
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

import java.util.List;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;

import junit.framework.TestCase;

import com.eteks.sweethome3d.model.Catalog;
import com.eteks.sweethome3d.model.Category;
import com.eteks.sweethome3d.model.PieceOfFurniture;
import com.eteks.sweethome3d.swing.CatalogTree;
import com.eteks.sweethome3d.io.DefaultCatalog;

/**
 * Tests furniture catalog tree component.
 * @author Emmanuel Puybaret
 */
public class CatalogTest extends TestCase {

  public CatalogTest(String name) {
    super(name);
  }

  public void testCatalogTreeCreation() {
    // Read the furniture catalog from English locale resources
    Locale.setDefault(Locale.US);
    Catalog catalog = new DefaultCatalog();
    catalog.readFurniture();

    // Get the name of the first category 
    List<Category> categories = catalog.getCategories();
    Category firstCategory = categories.get(0);
    String firstCategoryEnglishName = firstCategory.getName(); 
    // Get the name of the first piece of furniture
    List<PieceOfFurniture> categoryFurniture = firstCategory.getFurniture();
    PieceOfFurniture firstPiece = categoryFurniture.get(0); 
    String firstPieceEnglishName = firstPiece.getName();
    
    // Read the furniture catalog from French locale resources
    Locale.setDefault(Locale.FRENCH);
    catalog = new DefaultCatalog();
    catalog.readFurniture();
    // Get the french names of the first category and its first piece of furniture
    firstCategory = catalog.getCategories().get(0);
    String firstCategoryFrenchName = firstCategory.getName();
    String firstPieceFrenchName = firstCategory.getFurniture().get(0).getName();
    // Compare categories and furniture names in English and French locale
    assertFalse("Same name for first category",
        firstCategoryEnglishName.equals(firstCategoryFrenchName));
    assertFalse("Same name for first piece",
        firstPieceEnglishName.equals(firstPieceFrenchName)); 

    // Create a tree from default furniture
    JTree tree = new CatalogTree(catalog);

    // Select first piece in tree
    tree.expandRow(0); 
    tree.setSelectionRow(1); 
    PieceOfFurniture [] selectedFurniture = 
        ((CatalogTree)tree).getSelectedFurniture();
    assertEquals("No piece of furniture selected", 
        1, selectedFurniture.length);
    assertEquals("First piece not selected", 
        firstPieceFrenchName, selectedFurniture [0].getName()); 
  }

  
  public static void main(String [] args) {
    // Read the furniture catalog from default locale resources
    Catalog catalog = new DefaultCatalog();
    catalog.readFurniture();
    CatalogTree tree = new CatalogTree(catalog);
    JFrame frame = new JFrame("Catalog Test");
    frame.add(new JScrollPane(tree));
    frame.pack();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);
  }
}
