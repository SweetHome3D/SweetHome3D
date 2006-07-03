/*
 * CatalogTreeTest.java 6 avr. 2006
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

import java.awt.Component;
import java.text.Collator;
import java.util.List;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;

import junit.framework.TestCase;

import com.eteks.sweethome3d.io.DefaultCatalog;
import com.eteks.sweethome3d.model.Catalog;
import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.Category;
import com.eteks.sweethome3d.swing.CatalogTree;

/**
 * Tests furniture catalog tree component.
 * @author Emmanuel Puybaret
 */
public class CatalogTreeTest extends TestCase {
  public void testCatalogTreeCreation() {
    // 1. Create a furniture catalog read from English locale resources
    Locale.setDefault(Locale.US);
    Catalog catalog = new DefaultCatalog();

    // Get the name of the first category 
    List<Category> categories = catalog.getCategories();
    Category firstCategory = categories.get(0);
    String firstCategoryEnglishName = firstCategory.getName(); 
    // Get the name of the first piece of furniture
    List<CatalogPieceOfFurniture> categoryFurniture = firstCategory.getFurniture();
    CatalogPieceOfFurniture firstPiece = categoryFurniture.get(0); 
    String firstPieceEnglishName = firstPiece.getName();
    
    // 2. Read the furniture catalog from French locale resources
    Locale.setDefault(Locale.FRENCH);
    catalog = new DefaultCatalog();
    // Get the french names of the first category and its first piece of furniture
    firstCategory = catalog.getCategories().get(0);
    String firstCategoryFrenchName = firstCategory.getName();
    firstPiece = firstCategory.getFurniture().get(0); 
    String firstPieceFrenchName = firstPiece.getName();
    // Check categories and furniture names in English and French locale are different
    assertFalse("Same name for first category",
        firstCategoryEnglishName.equals(firstCategoryFrenchName));
    assertFalse("Same name for first piece",
        firstPieceEnglishName.equals(firstPieceFrenchName)); 

    // 3. Create a tree from default catalog
    JTree tree = new CatalogTree(catalog);

    // Check root isn't visible and root handles are showed
    assertFalse("Root is visible", tree.isRootVisible());
    assertTrue("Handles not showed", tree.getShowsRootHandles());
    
    // 4. Check alphabetical order of categories and furniture in tree
    assertTreeIsSorted(tree);
  }
  
  public void assertTreeIsSorted(JTree tree) {
    TreeModel model = tree.getModel();
    Object    root  = model.getRoot();
    Collator  comparator = Collator.getInstance();
    // For each category 
    for (int i = 0, n = model.getChildCount(root); i < n; i++) {
      Object rootChild = model.getChild(root, i);
      if (i < n - 1) {
        Object nextChild = model.getChild(root, i + 1);
        // Check alphatical order of categories nodes in tree 
        assertTrue("Categories not sorted", comparator.compare(
            getNodeText(tree, rootChild), 
            getNodeText(tree, nextChild)) <= 0);
      }
      // For each piece of furniture of a category
      for (int j = 0, m = model.getChildCount(rootChild) - 1; 
           j < m; j++) {
        Object child = model.getChild(rootChild, j);
        if (j < m - 1) {
          Object nextChild = model.getChild(rootChild, j + 1);
          // Check alphatical order of furniture nodes in tree 
          assertTrue("Furniture not sorted", comparator.compare(
              getNodeText(tree, child), 
              getNodeText(tree, nextChild)) <= 0);
        }
        assertTrue("Piece not a leaf", model.isLeaf(child));
      }
    }
  }

  /**
   * Returns the label text of <code>node</code> in <code>tree</code>.
   */
  private String getNodeText(JTree tree, Object node) {
    TreeCellRenderer renderer = tree.getCellRenderer();
    Component childLabel = renderer.
        getTreeCellRendererComponent(tree, node, 
           false, true, false, 0, false);
    return ((JLabel)childLabel).getText();
  }
  
  public static void main(String [] args) {
    // Create a furniture tree from the default locale catalog
    CatalogTree tree = new CatalogTree(new DefaultCatalog());
    JFrame frame = new JFrame("Catalog Tree Test");
    frame.add(new JScrollPane(tree));
    frame.pack();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);
  }
}
