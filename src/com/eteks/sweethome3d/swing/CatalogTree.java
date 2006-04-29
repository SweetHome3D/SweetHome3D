/*
 * CatalogTree.java 7 avr. 2006
 *
 * Copyright (c) 2006 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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
package com.eteks.sweethome3d.swing;

import java.awt.Component;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.eteks.sweethome3d.model.Catalog;
import com.eteks.sweethome3d.model.Category;
import com.eteks.sweethome3d.model.PieceOfFurniture;

/**
 * A tree displaying furniture catalog by category.
 * @author Emmanuel Puybaret
 */
public class CatalogTree extends JTree {
  public CatalogTree(Catalog catalog) {
    // Create Nodes hierarchy from DefaultFurniture 
    DefaultMutableTreeNode root = new DefaultMutableTreeNode();
    for(Category category : catalog.getCategories()) {
      DefaultMutableTreeNode categoryNode = new DefaultMutableTreeNode (category);
      root.add(categoryNode);
      for (PieceOfFurniture piece : category.getFurniture()) {
        categoryNode.add(new DefaultMutableTreeNode (piece, false));
      }      
    }
      
    setModel(new DefaultTreeModel (root));
    setRootVisible(false);
    setShowsRootHandles(true);
    setCellRenderer(new CatalogCellRenderer());
  }

  /**
   * Returns the furniture currently selected in the tree.
   * @return an array of furniture. If no furniture is selected, the array length is 0.
   */
  public PieceOfFurniture [] getSelectedFurniture() {
    TreePath [] selectionPaths  = getSelectionPaths();
    List<PieceOfFurniture> selectedFurniture = new ArrayList<PieceOfFurniture>();
    for (TreePath path : selectionPaths) {
      // Add to selectedFurniture all the nodes that matches a piece of furniture
      if (path.getPathCount() == 3) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
        selectedFurniture.add((PieceOfFurniture)node.getUserObject());
      }        
    }
    return selectedFurniture.toArray(new PieceOfFurniture [selectedFurniture.size()]);
  }  
  
  private class CatalogCellRenderer extends DefaultTreeCellRenderer {
    private static final int DEFAULT_ICON_HEIGHT = 32;
    @Override
    public Component getTreeCellRendererComponent(JTree tree, 
        Object value, boolean selected, boolean expanded, 
        boolean leaf, int row, boolean hasFocus) {
      // Get default label with its icon, background and focus colors 
      JLabel label = (JLabel)super.getTreeCellRendererComponent( 
          tree, value, selected, expanded, leaf, row, hasFocus);
      // Get the rendered node
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
      // If user's objet node is a category, change label text
      if (node.getUserObject() instanceof Category) {
        Category category = (Category)node.getUserObject();
        label.setText(category.getName());
      } 
      // Else if user's objet node is a piece of furntiure, change label text and icon
      else if (node.getUserObject() instanceof PieceOfFurniture) {
        PieceOfFurniture piece = 
            (PieceOfFurniture)node.getUserObject();
        label.setText(piece.getName());
        try {
          // Read the icon of the piece 
          InputStream iconStream = piece.getIcon().openStream();
          BufferedImage image = ImageIO.read(iconStream);
          iconStream.close();
          // Scale the read icon  
          int rowHeight = isFixedRowHeight() 
                              ? getRowHeight() 
                              : DEFAULT_ICON_HEIGHT;
          int imageWidth = image.getWidth() * rowHeight 
                                            / image.getHeight();
          Image scaledImage = image.getScaledInstance(
              imageWidth, rowHeight, Image.SCALE_SMOOTH);
          label.setIcon(new ImageIcon (scaledImage));
        } catch (IOException ex) {
          // Too bad the icon can't be read
          ex.printStackTrace();
        }
      }
      return label;
    }
  }
}