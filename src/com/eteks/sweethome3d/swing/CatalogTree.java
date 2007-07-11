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
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
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
import com.eteks.sweethome3d.model.FurnitureEvent;
import com.eteks.sweethome3d.model.FurnitureListener;
import com.eteks.sweethome3d.model.SelectionEvent;
import com.eteks.sweethome3d.model.SelectionListener;

/**
 * A tree displaying furniture catalog by category.
 * @author Emmanuel Puybaret
 */
public class CatalogTree extends JTree {
  private Catalog               catalog;
  private TreeSelectionListener treeSelectionListener;
  private SelectionListener     modelSelectionListener;
  private boolean               furnitureSelectionSynchronized;

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
    this.catalog = catalog;
    setModel(new CatalogTreeModel(catalog));
    setRootVisible(false);
    setShowsRootHandles(true);
    setCellRenderer(new CatalogCellRenderer());
    updateTreeSelectedFurniture(catalog); 
    if (controller != null) {
      createSelectionListeners(catalog, controller);
      setFurnitureSelectionSynchronized(true);
      addMouseListener(controller);
    }
    setDragEnabled(true);
  }
  
  /** 
   * Creates the listeners that manage selection synchronization in this tree. 
   */
  private void createSelectionListeners(final Catalog catalog, 
                                        final CatalogController controller) {
    this.treeSelectionListener = 
      new TreeSelectionListener () {
        public void valueChanged(TreeSelectionEvent ev) {
          catalog.removeSelectionListener(modelSelectionListener);
          // Set the new selection in catalog with controller
          controller.setSelectedFurniture(getSelectedFurniture());
          catalog.addSelectionListener(modelSelectionListener);
        }
      };
    this.modelSelectionListener = 
      new SelectionListener() {
        public void selectionChanged(SelectionEvent ev) {
          getSelectionModel().removeTreeSelectionListener(treeSelectionListener);
          updateTreeSelectedFurniture(catalog);        
          getSelectionModel().addTreeSelectionListener(treeSelectionListener);
        }
      };
  }

  /**
   * If <code>catalogSelectionSynchronized</code> is <code>true</code>, the selected 
   * furniture in the catalog model will be synchronized with be the selection displayed 
   * by this view. By default, selection is synchronized. 
   */
  public void setFurnitureSelectionSynchronized(boolean furnitureSelectionSynchronized) {
    if (this.furnitureSelectionSynchronized ^ furnitureSelectionSynchronized) {
      if (furnitureSelectionSynchronized) {
        updateTreeSelectedFurniture(this.catalog); 
        this.catalog.addSelectionListener(this.modelSelectionListener);
        getSelectionModel().addTreeSelectionListener(this.treeSelectionListener);
      } else {
        this.catalog.removeSelectionListener(this.modelSelectionListener);
        getSelectionModel().removeTreeSelectionListener(this.treeSelectionListener);
      }
      this.furnitureSelectionSynchronized = furnitureSelectionSynchronized;
    }
  }
  
  /**
   * Updates selected nodes in tree from <code>catalog</code> selected furniture. 
   */
  private void updateTreeSelectedFurniture(Catalog catalog) {
    clearSelection();
    for (CatalogPieceOfFurniture piece : catalog.getSelectedFurniture()) {
      TreePath path = new TreePath(new Object [] {catalog, piece.getCategory(), piece});
      addSelectionPath(path);
      scrollRowToVisible(getRowForPath(path));
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
   * Adds a double click mouse listener to modify selected furniture.
   */
  private void addMouseListener(final CatalogController controller) {
    addMouseListener(new MouseAdapter () {
        @Override
        public void mouseClicked(MouseEvent ev) {
          if (ev.getClickCount() == 2) {
            TreePath clickedPath = getPathForLocation(ev.getX(), ev.getY());
            if (clickedPath != null
                && clickedPath.getLastPathComponent() instanceof CatalogPieceOfFurniture) {
              controller.modifySelectedFurniture();
            }
          }
        }
      });
  }

  /**
   * Cell renderer for this catalog tree.
   */
  private static class CatalogCellRenderer extends DefaultTreeCellRenderer {
    private static final int DEFAULT_ICON_HEIGHT = 32;
    private Font defaultFont;
    private Font modifiablePieceFont;
    
    @Override
    public Component getTreeCellRendererComponent(JTree tree, 
        Object value, boolean selected, boolean expanded, 
        boolean leaf, int row, boolean hasFocus) {
      // Get default label with its icon, background and focus colors 
      JLabel label = (JLabel)super.getTreeCellRendererComponent( 
          tree, value, selected, expanded, leaf, row, hasFocus);
      // Initialize fonts if not done
      if (defaultFont == null) {
        this.defaultFont = label.getFont();
        this.modifiablePieceFont = new Font(defaultFont.getFamily(), Font.ITALIC, defaultFont.getSize());
        
      }
      // If node is a category, change label text
      if (value instanceof Category) {
        label.setText(((Category)value).getName());
        label.setFont(defaultFont);
      } 
      // Else if node is a piece of furntiure, change label text and icon
      else if (value instanceof CatalogPieceOfFurniture) {
        CatalogPieceOfFurniture piece = (CatalogPieceOfFurniture)value;
        label.setText(piece.getName());
        label.setIcon(getLabelIcon(tree, piece.getIcon()));
        label.setFont(piece.isModifiable() 
            ? this.modifiablePieceFont : this.defaultFont);
      }
      return label;
    }

    /**
     * Returns an Icon instance with the read image scaled at the tree row height or
     * an empty image if the image couldn't be read.
     * @param content the content of an image.
     */
    private Icon getLabelIcon(JTree tree, Content content) {
      int rowHeight = tree.isFixedRowHeight()
                         ? tree.getRowHeight()
                         : DEFAULT_ICON_HEIGHT;
      return IconManager.getInstance().getIcon(content, rowHeight, tree);
    }
  }
  
  /**
   * Tree model adaptor to Catalog / Category / PieceOfFurniture classes.  
   */
  private static class CatalogTreeModel implements TreeModel {
    private Catalog                 catalog;
    private List<TreeModelListener> listeners;
    
    public CatalogTreeModel(Catalog catalog) {
      this.catalog = catalog;
      this.listeners = new ArrayList<TreeModelListener>(2);
      catalog.addFurnitureListener(new CatalogFurnitureListener(this));
    }

    public Object getRoot() {
      return this.catalog;
    }

    public Object getChild(Object parent, int index) {
      if (parent instanceof Catalog) {
        return ((Catalog)parent).getCategory(index);
      } else {
        return ((Category)parent).getPieceOfFurniture(index);
      }
    }

    public int getChildCount(Object parent) {
      if (parent instanceof Catalog) {
        return ((Catalog)parent).getCategoriesCount();
      } else {
        return ((Category)parent).getFurnitureCount();
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
      // Tree isn't editable
    }

    public void addTreeModelListener(TreeModelListener l) {
      this.listeners.add(l);
    }

    public void removeTreeModelListener(TreeModelListener l) {
      this.listeners.remove(l);
    }
    
    private void fireTreeNodesInserted(TreeModelEvent treeModelEvent) {
      // Work on a copy of listeners to ensure a listener 
      // can modify safely listeners list
      TreeModelListener [] listeners = this.listeners.
          toArray(new TreeModelListener [this.listeners.size()]);
      for (TreeModelListener listener : listeners) {
        listener.treeNodesInserted(treeModelEvent);
      }
    }

    private void fireTreeNodesRemoved(TreeModelEvent treeModelEvent) {
      // Work on a copy of listeners to ensure a listener 
      // can modify safely listeners list
      TreeModelListener [] listeners = this.listeners.
          toArray(new TreeModelListener [this.listeners.size()]);
      for (TreeModelListener listener : listeners) {
        listener.treeNodesRemoved(treeModelEvent);
      }
    }
    
    /**
     * Catalog furniture listener bound to this tree model with a weak reference to avoid
     * strong link between catalog and this tree.  
     */
    private static class CatalogFurnitureListener implements FurnitureListener {
      private WeakReference<CatalogTreeModel>  catalogTreeModel;

      public CatalogFurnitureListener(CatalogTreeModel catalogTreeModel) {
        this.catalogTreeModel = new WeakReference<CatalogTreeModel>(catalogTreeModel);
      }
      
      public void pieceOfFurnitureChanged(FurnitureEvent ev) {
        // If catalog tree model was garbage collected, remove this listener from catalog
        CatalogTreeModel catalogTreeModel = this.catalogTreeModel.get();
        Catalog catalog = (Catalog)ev.getSource();
        if (catalogTreeModel == null) {
          (catalog).removeFurnitureListener(this);
        } else {
          CatalogPieceOfFurniture piece = (CatalogPieceOfFurniture)ev.getPieceOfFurniture();
          switch (ev.getType()) {
            case ADD :
              if (piece.getCategory().getFurnitureCount() == 1) {
                // Fire nodes inserted for new category
                catalogTreeModel.fireTreeNodesInserted(new TreeModelEvent(this,
                    new Object [] {catalog}, 
                    new int [] {Collections.binarySearch(catalog.getCategories(), piece.getCategory())}, 
                    new Object [] {piece.getCategory()}));
              } else {
                // Fire nodes inserted for new piece
                catalogTreeModel.fireTreeNodesInserted(new TreeModelEvent(this,
                    new Object [] {catalog, piece.getCategory()},
                    new int [] {ev.getIndex()},
                    new Object [] {piece}));
              }
              break;
            case DELETE :
              if (piece.getCategory().getFurnitureCount() == 0) {
                // Fire nodes removed for deleted category
                catalogTreeModel.fireTreeNodesRemoved(new TreeModelEvent(this,
                    new Object [] {catalog},
                    new int [] {-(Collections.binarySearch(catalog.getCategories(), piece.getCategory()) + 1)},
                    new Object [] {piece.getCategory()}));
              } else {
                // Fire nodes removed for deleted piece
                catalogTreeModel.fireTreeNodesRemoved(new TreeModelEvent(this, 
                    new Object [] {catalog, piece.getCategory()},
                    new int [] {ev.getIndex()},
                    new Object [] {piece}));
              }
              break;
          }
        }
      }
    }
  }
}