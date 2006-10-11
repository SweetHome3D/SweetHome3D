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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import com.eteks.sweethome3d.model.Catalog;
import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.Category;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.SelectionEvent;
import com.eteks.sweethome3d.model.SelectionListener;

/**
 * A tree displaying furniture catalog by category.
 * @author Emmanuel Puybaret
 */
public class CatalogTree extends JTree {
  private TreeSelectionListener treeSelectionListener;

  /**
   * Creates a tree that displays <code>catalog</code> content.
   */
  public CatalogTree(Catalog catalog) {
    this(catalog, null);
  }

  /**
   * Creates a tree controlled by <code>controller</code>
   * that displays <code>catalog</code> content.
   */
  public CatalogTree(Catalog catalog, CatalogController controller) {
    setModel(new CatalogTreeModel (catalog));
    setRootVisible(false);
    setShowsRootHandles(true);
    setCellRenderer(new CatalogCellRenderer());
    if (controller != null) {
      addSelectionListeners(catalog, controller);
    }
  }
  
  /**
   * Adds selection listeners to this tree.
   */
  private void addSelectionListeners(final Catalog catalog, 
                                     final CatalogController controller) {    
    final SelectionListener modelSelectionListener = 
      new SelectionListener() {
        public void selectionChanged(SelectionEvent ev) {
          getSelectionModel().removeTreeSelectionListener(treeSelectionListener);
          clearSelection();
          for (Object item : ev.getSelectedItems()) {
            selectPieceOfFurniture(catalog, (CatalogPieceOfFurniture)item);
          }        
          getSelectionModel().addTreeSelectionListener(treeSelectionListener);
        }
      };
    this.treeSelectionListener = 
      new TreeSelectionListener () {
        public void valueChanged(TreeSelectionEvent ev) {
          catalog.removeSelectionListener(modelSelectionListener);
          // Set the new selection in catalog with controller
          controller.setSelectedFurniture(getSelectedFurniture());
          catalog.addSelectionListener(modelSelectionListener);
        }
      };
    getSelectionModel().addTreeSelectionListener(this.treeSelectionListener);
    catalog.addSelectionListener(modelSelectionListener);
  }

  /**
   * Selects in the tree the piece <code>selectedPiece</code>. 
   */
  private void selectPieceOfFurniture(Catalog catalog, 
                                      CatalogPieceOfFurniture selectedPiece) {
    // Search the parent category of piece
    for (Category category : catalog.getCategories()) {
      for (CatalogPieceOfFurniture piece : category.getFurniture()) {
        // Don't use list methods to search to avoid equality between pieces with same name
        if (piece == selectedPiece) {
          TreePath path = new TreePath(new Object [] {catalog, category, piece});
          addSelectionPath(path);
          scrollRowToVisible(getRowForPath(path));
          break;
        }
      }
    }
  }

  /**
   * Returns the selected furniture in tree.
   */
  private List<CatalogPieceOfFurniture> getSelectedFurniture() {
    // Build the list of selected furniture
    List<CatalogPieceOfFurniture> selectedFurniture =
      new ArrayList<CatalogPieceOfFurniture>();
    TreePath [] selectionPaths = getSelectionPaths(); 
    if (selectionPaths != null) {
      for (TreePath path : selectionPaths) {
        // Add to selectedFurniture all the nodes that matches a piece of furniture
        if (path.getPathCount() == 3) {
          selectedFurniture.add((CatalogPieceOfFurniture)path.getLastPathComponent());
        }        
      }
    }   
    return selectedFurniture;
  }

  /**
   * Cell renderer for this catalog tree.
   */
  private class CatalogCellRenderer extends DefaultTreeCellRenderer {
    private static final int DEFAULT_ICON_HEIGHT = 32;
    @Override
    public Component getTreeCellRendererComponent(JTree tree, 
        Object value, boolean selected, boolean expanded, 
        boolean leaf, int row, boolean hasFocus) {
      // Get default label with its icon, background and focus colors 
      JLabel label = (JLabel)super.getTreeCellRendererComponent( 
          tree, value, selected, expanded, leaf, row, hasFocus);
      // If node is a category, change label text
      if (value instanceof Category) {
        label.setText(((Category)value).getName());
      } 
      // Else if node is a piece of furntiure, change label text and icon
      else if (value instanceof CatalogPieceOfFurniture) {
        CatalogPieceOfFurniture piece = (CatalogPieceOfFurniture)value;
        label.setText(piece.getName());
        label.setIcon(getLabelIcon(piece.getIcon()));
      }
      return label;
    }

    /**
     * Returns an Icon instance with the read image scaled at the tree row height or
     * an empty image if the image couldn't be read.
     * @param content the content of an image.
     */
    private Icon getLabelIcon(Content content) {
      int rowHeight = isFixedRowHeight()
                         ? getRowHeight()
                         : DEFAULT_ICON_HEIGHT;
      return IconManager.getInstance().getIcon(
          content, rowHeight, CatalogTree.this);
    }
  }
  
  /**
   * Tree model adaptor to Catalog / Category / PieceOfFurniture classes.  
   */
  private static class CatalogTreeModel implements TreeModel {
    private Catalog catalog;
    
    public CatalogTreeModel(Catalog catalog) {
      this.catalog = catalog;
    }

    public Object getRoot() {
      return this.catalog;
    }

    public Object getChild(Object parent, int index) {
      if (parent instanceof Catalog) {
        return ((Catalog)parent).getCategories().get(index);
      } else {
        return ((Category)parent).getFurniture().get(index);
      }
    }

    public int getChildCount(Object parent) {
      if (parent instanceof Catalog) {
        return ((Catalog)parent).getCategories().size();
      } else {
        return ((Category)parent).getFurniture().size();
      }
    }

    public int getIndexOfChild(Object parent, Object child) {
      if (parent instanceof Catalog) {
        return Collections.binarySearch(((Catalog)parent).getCategories(), (Category)child);
      } else {
        return Collections.binarySearch(((Category)parent).getFurniture(), (CatalogPieceOfFurniture)child);
      }
    }

    public boolean isLeaf(Object node) {
      return node instanceof CatalogPieceOfFurniture;
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
    }

    public void addTreeModelListener(TreeModelListener l) {
    }

    public void removeTreeModelListener(TreeModelListener l) {
    }
  }
}