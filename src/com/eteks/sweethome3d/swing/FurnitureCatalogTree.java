/*
 * FurnitureCatalogTree.java 7 avr. 2006
 *
 * Sweet Home 3D, Copyright (c) 2006 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.dnd.DnDConstants;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.CollectionEvent;
import com.eteks.sweethome3d.model.CollectionListener;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.FurnitureCatalog;
import com.eteks.sweethome3d.model.FurnitureCategory;
import com.eteks.sweethome3d.model.SelectionEvent;
import com.eteks.sweethome3d.model.SelectionListener;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.URLContent;
import com.eteks.sweethome3d.viewcontroller.FurnitureCatalogController;
import com.eteks.sweethome3d.viewcontroller.View;

/**
 * A tree displaying furniture catalog by category.
 * @author Emmanuel Puybaret
 */
public class FurnitureCatalogTree extends JTree implements View {
  private final UserPreferences preferences;
  private TreeSelectionListener treeSelectionListener;

  /**
   * Creates a tree that displays <code>catalog</code> content.
   */
  public FurnitureCatalogTree(FurnitureCatalog catalog) {
    this(catalog, null);
  }

  /**
   * Creates a tree controlled by <code>controller</code> that displays 
   * <code>catalog</code> content and its selection.
   */
  public FurnitureCatalogTree(FurnitureCatalog catalog, 
                              FurnitureCatalogController controller) {
    this(catalog, null, controller);
  }
  
  /**
   * Creates a tree controlled by <code>controller</code> that displays 
   * <code>catalog</code> content and its selection.
   */
  public FurnitureCatalogTree(FurnitureCatalog catalog, 
                              UserPreferences preferences, 
                              FurnitureCatalogController controller) {
    this.preferences = preferences;
    setModel(new CatalogTreeModel(catalog));
    setRootVisible(false);
    setShowsRootHandles(true);
    setCellRenderer(new CatalogCellRenderer());
    addDragListener();
    if (controller != null) {
      updateTreeSelectedFurniture(catalog, controller);
      addSelectionListeners(catalog, controller);
      addMouseListeners(controller);
    }
    ToolTipManager.sharedInstance().registerComponent(this);
    // Remove Select all action
    getActionMap().getParent().remove("selectAll");
  }

  /**
   * Adds a mouse motion listener that will initiate a drag operation 
   * when the user drags a piece of furniture.
   */
  private void addDragListener() {
    addMouseMotionListener(new MouseMotionAdapter() {
      public void mouseDragged(MouseEvent ev) {
        if (SwingUtilities.isLeftMouseButton(ev)) {
          TreePath clickedPath = getPathForLocation(ev.getX(), ev.getY());
          if (clickedPath != null
              && clickedPath.getLastPathComponent() instanceof CatalogPieceOfFurniture
              && getTransferHandler() != null) {
            getTransferHandler().exportAsDrag(FurnitureCatalogTree.this, ev, DnDConstants.ACTION_COPY);
          }
        }
      }
    });
  }
  
  /** 
   * Adds the listeners that manage selection synchronization in this tree. 
   */
  private void addSelectionListeners(final FurnitureCatalog catalog, 
                                     final FurnitureCatalogController controller) {
    final SelectionListener modelSelectionListener = new SelectionListener() {
        public void selectionChanged(SelectionEvent selectionEvent) {
          updateTreeSelectedFurniture(catalog, controller);        
        }
      };
    this.treeSelectionListener = new TreeSelectionListener () {
        public void valueChanged(TreeSelectionEvent ev) {
          // Updates selected furniture in catalog from selected nodes in tree. 
          controller.removeSelectionListener(modelSelectionListener);
          controller.setSelectedFurniture(getSelectedFurniture());
          controller.addSelectionListener(modelSelectionListener);
        }
      };
      
    controller.addSelectionListener(modelSelectionListener);
    getSelectionModel().addTreeSelectionListener(this.treeSelectionListener);
  }
  
  /**
   * Updates selected nodes in tree from <code>catalog</code> selected furniture. 
   */
  private void updateTreeSelectedFurniture(FurnitureCatalog catalog,
                                           FurnitureCatalogController controller) {
    if (this.treeSelectionListener != null) {
      getSelectionModel().removeTreeSelectionListener(this.treeSelectionListener);
    }
    
    clearSelection();
    for (CatalogPieceOfFurniture piece : controller.getSelectedFurniture()) {
      TreePath path = new TreePath(new Object [] {catalog, piece.getCategory(), piece});
      addSelectionPath(path);
      scrollRowToVisible(getRowForPath(path));
    }
    
    if (this.treeSelectionListener != null) {
      getSelectionModel().addTreeSelectionListener(this.treeSelectionListener);
    }
  }

  /**
   * Returns the selected furniture in tree.
   */
  private List<CatalogPieceOfFurniture> getSelectedFurniture() {
    // Build the list of selected furniture
    List<CatalogPieceOfFurniture> selectedFurniture = new ArrayList<CatalogPieceOfFurniture>();
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
   * Adds mouse listeners to modify selected furniture and manage links in piece information.
   */
  private void addMouseListeners(final FurnitureCatalogController controller) {
    final Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);
    MouseInputAdapter mouseListener = new MouseInputAdapter() {
        @Override
        public void mouseClicked(MouseEvent ev) {
          if (SwingUtilities.isLeftMouseButton(ev)) {
            if (ev.getClickCount() == 2) {
              TreePath clickedPath = getPathForLocation(ev.getX(), ev.getY());
              if (clickedPath != null
                  && clickedPath.getLastPathComponent() instanceof CatalogPieceOfFurniture) {
                controller.modifySelectedFurniture();
              }
            } else if (getCellRenderer() instanceof CatalogCellRenderer) {
              URL url = ((CatalogCellRenderer)getCellRenderer()).getURLAt(ev.getPoint(), (JTree)ev.getSource());
              if (url != null) {
                SwingTools.showDocumentInBrowser(url);
              }
            }
          }
        }
        
        @Override
        public void mouseMoved(MouseEvent ev) {
          if (getCellRenderer() instanceof CatalogCellRenderer) {
            URL url = ((CatalogCellRenderer)getCellRenderer()).getURLAt(ev.getPoint(), (JTree)ev.getSource());
            if (url != null) {
              EventQueue.invokeLater(new Runnable() {                  
                  public void run() {
                    setCursor(handCursor);
                  }
                });
            }
          }
          setCursor(Cursor.getDefaultCursor());
        }
      };
    addMouseListener(mouseListener);
    addMouseMotionListener(mouseListener);
  }

  /**
   * Returns a tooltip for furniture pieces described in this tree.
   */
  @Override
  public String getToolTipText(MouseEvent ev) {
    TreePath path = getPathForLocation(ev.getX(), ev.getY());
    if (this.preferences != null
        && path != null
        && path.getPathCount() == 3) {
      CatalogPieceOfFurniture piece = (CatalogPieceOfFurniture)path.getLastPathComponent();
      String tooltip = "<html><table><tr><td align='center'><b>" + piece.getName() + "</b>";
      String creator = piece.getCreator();
      if (creator != null) {
        tooltip += "<br>" + this.preferences.getLocalizedString(FurnitureCatalogTree.class, 
            "tooltipCreator", creator + "</td></tr>");
      }
      if (piece.getIcon() instanceof URLContent) {
        try {
          // Ensure image will always be viewed in a 128x128 pixels cell
          BufferedImage image = ImageIO.read(((URLContent)piece.getIcon()).getURL());
          if (image == null) {
            return null;
          }
          int width = Math.round(128f * Math.min(1, image.getWidth() / image.getHeight()));
          int height = Math.round((float)width * image.getHeight() / image.getWidth());
          tooltip += "<tr><td width='128' height='128' align='center' valign='middle'><img width='" + width 
              + "' height='" + height + "' src='" 
              + ((URLContent)piece.getIcon()).getURL() + "'></td></tr>";
        } catch (IOException ex) {
          return null;
        }
      }
      return tooltip + "</table>";
    } else {
      return null;
    }
  }
  
  /**
   * Cell renderer for this catalog tree.
   */
  private class CatalogCellRenderer extends JComponent implements TreeCellRenderer {
    private static final int        DEFAULT_ICON_HEIGHT = 32;
    private Font                    defaultFont;
    private Font                    modifiablePieceFont;
    private DefaultTreeCellRenderer nameLabel;
    private JEditorPane             informationPane;
    
    public CatalogCellRenderer() {
      setLayout(null);
      this.nameLabel = new DefaultTreeCellRenderer();
      this.informationPane = new JEditorPane("text/html", null);
      this.informationPane.setOpaque(false);
      this.informationPane.setEditable(false);
      add(this.nameLabel);
      add(this.informationPane);
    }
    
    public Component getTreeCellRendererComponent(JTree tree, 
        Object value, boolean selected, boolean expanded, 
        boolean leaf, int row, boolean hasFocus) {
      // Configure name label with its icon, background and focus colors 
      this.nameLabel.getTreeCellRendererComponent( 
          tree, value, selected, expanded, leaf, row, hasFocus);
      // Initialize fonts if not done
      if (this.defaultFont == null) {
        this.defaultFont = this.nameLabel.getFont();
        String bodyRule = "body { font-family: " + this.defaultFont.getFamily() + "; " 
            + "font-size: " + this.defaultFont.getSize() + "pt; " 
            + "top-margin: 0; }";
        ((HTMLDocument)this.informationPane.getDocument()).getStyleSheet().addRule(bodyRule);
        this.modifiablePieceFont = 
            new Font(this.defaultFont.getFontName(), Font.ITALIC, this.defaultFont.getSize());        
      }
      // If node is a category, change label text
      if (value instanceof FurnitureCategory) {
        this.nameLabel.setText(((FurnitureCategory)value).getName());
        this.nameLabel.setFont(this.defaultFont);
        this.informationPane.setVisible(false);
      } 
      // Else if node is a piece of furniture, change label text and icon
      else if (value instanceof CatalogPieceOfFurniture) {
        CatalogPieceOfFurniture piece = (CatalogPieceOfFurniture)value;
        this.nameLabel.setText(piece.getName());
        this.nameLabel.setIcon(getLabelIcon(tree, piece.getIcon()));
        this.nameLabel.setFont(piece.isModifiable() 
            ? this.modifiablePieceFont : this.defaultFont);
        
        String information = piece.getInformation();
        if (information != null) {
          this.informationPane.setText(information);
          this.informationPane.setVisible(true);
        } else {
          this.informationPane.setVisible(false);
        }
      }
      return this;
    }
    
    @Override
    public void doLayout() {
      Dimension namePreferredSize = this.nameLabel.getPreferredSize();
      this.nameLabel.setSize(namePreferredSize);
      if (this.informationPane.isVisible()) {
        Dimension informationPreferredSize = this.informationPane.getPreferredSize();
        this.informationPane.setBounds(namePreferredSize.width + 2, 
            (namePreferredSize.height - informationPreferredSize.height) / 2,
            informationPreferredSize.width, namePreferredSize.height);
      }
    }
    
    @Override
    public Dimension getPreferredSize() {
      Dimension preferredSize = this.nameLabel.getPreferredSize();
      if (this.informationPane.isVisible()) {
        preferredSize.width += 2 + this.informationPane.getPreferredSize().width;
      }
      return preferredSize;
    }
    
    /**
     * The following methods are overridden for performance reasons.
     */
    @Override
    public void revalidate() {      
    }
    
    @Override
    public void repaint(long tm, int x, int y, int width, int height) {      
    }

    @Override
    public void repaint(Rectangle r) {      
    }

    @Override
    public void repaint() {      
    }

    /**
     * Returns an Icon instance with the read image scaled at the tree row height or
     * an empty image if the image couldn't be read.
     * @param content the content of an image.
     */
    private Icon getLabelIcon(JTree tree, Content content) {
      return IconManager.getInstance().getIcon(content, getRowHeight(tree), tree);
    }

    /**
     * Returns the height of rows in tree.
     */
    private int getRowHeight(JTree tree) {
      return tree.isFixedRowHeight()
          ? tree.getRowHeight()
          : DEFAULT_ICON_HEIGHT;
    }
    
    @Override
    protected void paintChildren(Graphics g) {
      // Force text anti aliasing on texts
      ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
          RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      super.paintChildren(g);
    }

    public URL getURLAt(Point point, JTree tree) {
      TreePath path = tree.getPathForLocation(point.x, point.y);
      if (path != null
          && path.getLastPathComponent() instanceof CatalogPieceOfFurniture) {
        CatalogPieceOfFurniture piece = (CatalogPieceOfFurniture)path.getLastPathComponent();
        String information = piece.getInformation();
        if (information != null) {
          int row = tree.getRowForPath(path);
          getTreeCellRendererComponent(tree, piece, false, false, false, row, false).doLayout();
          Rectangle rowBounds = tree.getRowBounds(row);
          point.x -= rowBounds.x + this.informationPane.getX(); 
          point.y -= rowBounds.y + this.informationPane.getY(); 
          if (point.x > 0 && point.y > 0) {
            // Search in information pane if point is over a HTML link
            int position = this.informationPane.viewToModel(point);
            if (position > 0) {
              HTMLDocument hdoc = (HTMLDocument)this.informationPane.getDocument();
              Element element = hdoc.getCharacterElement(position);
              AttributeSet a = element.getAttributes();
              AttributeSet anchor = (AttributeSet)a.getAttribute(HTML.Tag.A);
              if (anchor != null) {
                String href = (String)anchor.getAttribute(HTML.Attribute.HREF);
                if (href != null) {
                  try {
                    return new URL(href);
                  } catch (MalformedURLException ex) {
                    // Ignore malformed URL
                  }
                }
              }
            }
          }
        }
      }
      return null;
    }
  }
  
  /**
   * Tree model adaptor to Catalog / Category / PieceOfFurniture classes.  
   */
  private static class CatalogTreeModel implements TreeModel {
    private FurnitureCatalog        catalog;
    private List<TreeModelListener> listeners;
    
    public CatalogTreeModel(FurnitureCatalog catalog) {
      this.catalog = catalog;
      this.listeners = new ArrayList<TreeModelListener>(2);
      catalog.addFurnitureListener(new CatalogFurnitureListener(this));
    }

    public Object getRoot() {
      return this.catalog;
    }

    public Object getChild(Object parent, int index) {
      if (parent instanceof FurnitureCatalog) {
        return ((FurnitureCatalog)parent).getCategory(index);
      } else {
        return ((FurnitureCategory)parent).getPieceOfFurniture(index);
      }
    }

    public int getChildCount(Object parent) {
      if (parent instanceof FurnitureCatalog) {
        return ((FurnitureCatalog)parent).getCategoriesCount();
      } else {
        return ((FurnitureCategory)parent).getFurnitureCount();
      } 
    }

    public int getIndexOfChild(Object parent, Object child) {
      if (parent instanceof FurnitureCatalog) {
        return Collections.binarySearch(((FurnitureCatalog)parent).getCategories(), (FurnitureCategory)child);
      } else {
        return ((FurnitureCategory)parent).getIndexOfPieceOfFurniture((CatalogPieceOfFurniture)child);
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
    private static class CatalogFurnitureListener implements CollectionListener<CatalogPieceOfFurniture> {
      private WeakReference<CatalogTreeModel>  catalogTreeModel;

      public CatalogFurnitureListener(CatalogTreeModel catalogTreeModel) {
        this.catalogTreeModel = new WeakReference<CatalogTreeModel>(catalogTreeModel);
      }
      
      public void collectionChanged(CollectionEvent<CatalogPieceOfFurniture> ev) {
        // If catalog tree model was garbage collected, remove this listener from catalog
        CatalogTreeModel catalogTreeModel = this.catalogTreeModel.get();
        FurnitureCatalog catalog = (FurnitureCatalog)ev.getSource();
        if (catalogTreeModel == null) {
          catalog.removeFurnitureListener(this);
        } else {
          CatalogPieceOfFurniture piece = ev.getItem();
          switch (ev.getType()) {
            case ADD :
              if (piece.getCategory().getFurnitureCount() == 1) {
                // Fire nodes inserted for new category
                catalogTreeModel.fireTreeNodesInserted(new TreeModelEvent(catalogTreeModel,
                    new Object [] {catalog}, 
                    new int [] {Collections.binarySearch(catalog.getCategories(), piece.getCategory())}, 
                    new Object [] {piece.getCategory()}));
              } else {
                // Fire nodes inserted for new piece
                catalogTreeModel.fireTreeNodesInserted(new TreeModelEvent(catalogTreeModel,
                    new Object [] {catalog, piece.getCategory()},
                    new int [] {ev.getIndex()},
                    new Object [] {piece}));
              }
              break;
            case DELETE :
              if (piece.getCategory().getFurnitureCount() == 0) {
                // Fire nodes removed for deleted category
                catalogTreeModel.fireTreeNodesRemoved(new TreeModelEvent(catalogTreeModel,
                    new Object [] {catalog},
                    new int [] {-(Collections.binarySearch(catalog.getCategories(), piece.getCategory()) + 1)},
                    new Object [] {piece.getCategory()}));
              } else {
                // Fire nodes removed for deleted piece
                catalogTreeModel.fireTreeNodesRemoved(new TreeModelEvent(catalogTreeModel, 
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