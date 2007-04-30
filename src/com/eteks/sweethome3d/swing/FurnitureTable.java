/*
 * FurnitureTable.java 15 mai 2006
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

import static com.eteks.sweethome3d.model.UserPreferences.Unit.INCH;
import static com.eteks.sweethome3d.model.UserPreferences.Unit.centimerToInch;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.FurnitureEvent;
import com.eteks.sweethome3d.model.FurnitureListener;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.SelectionEvent;
import com.eteks.sweethome3d.model.SelectionListener;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.HomePieceOfFurniture.SortableProperty;

/**
 * A table displaying furniture.
 * @author Emmanuel Puybaret
 */
public class FurnitureTable extends JTable {
  private ListSelectionListener tableSelectionListener;

  /**
   * Creates a table that displays furniture of <code>home</code>.
   * @param home        the home displayed by this view
   * @param preferences the preferences of the application
   */
  public FurnitureTable(Home home, UserPreferences preferences) {
    this(home, preferences, null);
  }

  /**
   * Creates a table controlled by <code>controller</code>
   * that displays furniture of <code>home</code>.
   */
  public FurnitureTable(Home home, UserPreferences preferences, 
                       FurnitureController controller) {
    String [] columnNames = getColumnNames();
    setModel(new FurnitureTableModel(home, columnNames));
    setColumnIdentifiers();
    setColumnRenderers(preferences);
    setTableHeaderRenderer(home);
    // Add listeners to model
    if (controller != null) {
      addSelectionListeners(home, controller);
      // Enable sort in table with click in header
      setTableHeaderListener(controller);
    }
    addHomeListener(home);
    addUserPreferencesListener(preferences);
  }
  
  /**
   * Adds selection listeners to this table.
   */
  private void addSelectionListeners(final Home home,
                                     final FurnitureController controller) {   
    final SelectionListener homeSelectionListener  = 
      new SelectionListener() {
        public void selectionChanged(SelectionEvent ev) {
          updateTableSelectedFurniture(ev.getSelectedItems());        
        }
      };
    this.tableSelectionListener = 
      new ListSelectionListener () {
        public void valueChanged(ListSelectionEvent ev) {
          if (!ev.getValueIsAdjusting()) {
            home.removeSelectionListener(homeSelectionListener);
            int [] selectedRows = getSelectedRows();
            // Build the list of selected furniture
            List<HomePieceOfFurniture> selectedFurniture =
              new ArrayList<HomePieceOfFurniture>(selectedRows.length);
            FurnitureTableModel tableModel = (FurnitureTableModel)getModel();
            for (int index : selectedRows) {
              // Add to selectedFurniture table model first column value that stores piece
              selectedFurniture.add((HomePieceOfFurniture)tableModel.getValueAt(index, 0));
            }
            // Set the new selection in home with controller
            controller.setSelectedFurniture(selectedFurniture);
            home.addSelectionListener(homeSelectionListener);
          }
        }
      };
    getSelectionModel().addListSelectionListener(this.tableSelectionListener);
    home.addSelectionListener(homeSelectionListener);
  }

  /**
   * Updates selected furniture in table from <code>selectedItems</code>. 
   */
  private void updateTableSelectedFurniture(List<Object> selectedItems) {
    getSelectionModel().removeListSelectionListener(tableSelectionListener);
    clearSelection();
    FurnitureTableModel tableModel = (FurnitureTableModel)getModel();
    for (Object item : selectedItems) {
      if (item instanceof HomePieceOfFurniture) {
        // Search index of piece in sorted table model
        int index = tableModel.getPieceOfFurnitureIndex((HomePieceOfFurniture)item);
        addRowSelectionInterval(index, index);
        makeRowVisible(index);
      }          
    }
    getSelectionModel().addListSelectionListener(tableSelectionListener);
  }
  
  /**
   * Adds a listener to <code>preferences</code> to repaint this table
   * when unit changes.  
   */
  private void addUserPreferencesListener(UserPreferences preferences) {
    preferences.addPropertyChangeListener("unit", 
      new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          repaint();
        }
      });
  }

  /**
   * Adds a <code>PropertyChange</code> listener to home to update furniture sort
   * in table when <code>furnitureSortedProperty</code> or <code>furnitureAscendingSorted</code> 
   * in <code>home</code> changes.
   */
  private void addHomeListener(final Home home) {
    PropertyChangeListener sortListener = 
      new PropertyChangeListener () {
        public void propertyChange(PropertyChangeEvent ev) {
          ((FurnitureTableModel)getModel()).sortFurniture(home);
          // Update selected rows
          updateTableSelectedFurniture(home.getSelectedItems());
          getTableHeader().repaint();
        }
      };
    home.addPropertyChangeListener("furnitureSortedProperty", sortListener);
    home.addPropertyChangeListener("furnitureDescendingSorted", sortListener);    
  }

  /**
   * Ensures the rectangle which displays row is visible.
   */
  private void makeRowVisible(int row) {
    // Compute the rectangle that includes a row 
    Rectangle includingRectangle = getCellRect(row, 0, true);
    if (getAutoResizeMode() == AUTO_RESIZE_OFF) {
      int lastColumn = getColumnCount() - 1;
      includingRectangle = includingRectangle.
          union(getCellRect(row, lastColumn, true));
    }
    scrollRectToVisible(includingRectangle);
  }

  /**
   * Returns localized column names.
   */
  private String [] getColumnNames() {
    ResourceBundle resource = 
      ResourceBundle.getBundle(FurnitureTable.class.getName());
    String [] columnNames = {
       resource.getString("nameColumn"),
       resource.getString("widthColumn"),
       resource.getString("heightColumn"),
       resource.getString("depthColumn"),
       resource.getString("colorColumn"),
       resource.getString("movableColumn"),
       resource.getString("doorOrWindowColumn"),
       resource.getString("visibleColumn")};
    return columnNames;
  }

  /**
   * Sets column unique identifiers matching furniture sortable properties.
   */
  private void setColumnIdentifiers() {
    HomePieceOfFurniture.SortableProperty [] furnitureProperties = 
        {SortableProperty.NAME, SortableProperty.WIDTH, SortableProperty.HEIGHT, SortableProperty.DEPTH, 
         SortableProperty.COLOR, SortableProperty.MOVABLE, 
         SortableProperty.DOOR_OR_WINDOW, SortableProperty.VISIBLE};
    // Set identifiers of each column
    TableColumnModel columnModel = getColumnModel();
    for (int i = 0, n = columnModel.getColumnCount(); i < n; i++) {
      columnModel.getColumn(i).setIdentifier(furnitureProperties [i]);
    }
  }

  /**
   * Sets column renderers.
   */
  private void setColumnRenderers(UserPreferences preferences) {
    // Renderer for width, height and depth columns
    TableCellRenderer sizeRenderer = getSizeRenderer(preferences);
    // Renderer for movable, doorOrWindow and visible columns
    TableCellRenderer checkBoxRenderer = getDefaultRenderer(Boolean.class);
    // Create an array for the renderers of each column
    TableCellRenderer [] columnRenderers = { 
      getNameWithIconRenderer(), // Renderer for name column
      sizeRenderer, sizeRenderer, sizeRenderer,
      getColorRenderer(),        // Renderer for color column
      checkBoxRenderer, checkBoxRenderer, checkBoxRenderer};
    
    // Set renderers of each column
    TableColumnModel columnModel = getColumnModel();
    for (int i = 0, n = getColumnCount(); i < n; i++) {
      columnModel.getColumn(i).setCellRenderer(columnRenderers [i]);
    }
  }

  /**
   * Returns a renderer that displays its value with row piece of furniture icon ahead. 
   */
  private TableCellRenderer getNameWithIconRenderer() {
    return new DefaultTableCellRenderer() { 
      @Override
      public Component getTableCellRendererComponent(JTable table, 
           Object value, boolean isSelected, boolean hasFocus, 
           int row, int column) {
        HomePieceOfFurniture piece = (HomePieceOfFurniture)value; 
        JLabel label = (JLabel)super.getTableCellRendererComponent(
          table, piece.getName(), isSelected, hasFocus, row, column); 
        Content iconContent = piece.getIcon(); 
        label.setIcon(IconManager.getInstance().getIcon(
            iconContent, getRowHeight(), table)); 
        return label;
      }
    };
  }

  /**
   * Returns a renderer that converts the displayed value to inch in case preferences unit us equal to INCH. 
   */
  private TableCellRenderer getSizeRenderer(final UserPreferences preferences) {
    final TableCellRenderer floatRenderer = getDefaultRenderer(Float.class); 
    return new TableCellRenderer () {
      public Component getTableCellRendererComponent(JTable table, 
           Object value, boolean isSelected, boolean hasFocus, 
           int row, int column) {
        if (preferences.getUnit() == INCH) {
          value = centimerToInch((Float)value);
        }        
        return floatRenderer.getTableCellRendererComponent(
            table, value, isSelected, hasFocus, row, column);
      }      
    };
  }

  /**
   * Returns a renderer that displays the RGB value of an int in a bordered label.
   */
  private TableCellRenderer getColorRenderer() {
    return new DefaultTableCellRenderer() {
      @Override
      public Component getTableCellRendererComponent(JTable table, 
           Object value, boolean isSelected, boolean hasFocus, 
           int row, int column) {
        JLabel label = (JLabel)super.getTableCellRendererComponent(
            table, value, isSelected, hasFocus, row, column);
        if (value != null) {
          label.setText("\u25fc");
          label.setForeground(new Color((Integer)value));
        } else {
          label.setText("-");
          label.setForeground(table.getForeground());
        }
        label.setHorizontalAlignment(JLabel.CENTER);
        return label;
      } 
    };
  }
 
  /**
   * Sets header renderer that displays an ascending or a descending icon when column is sorted.
   */
  private void setTableHeaderRenderer(final Home home) {
    JTableHeader tableHeader = getTableHeader();
    final TableCellRenderer currentRenderer = tableHeader.getDefaultRenderer();
    // Change table renderer to display the icon matching current sort
    tableHeader.setDefaultRenderer(new TableCellRenderer() {
      final ImageIcon ascendingSortIcon = new ImageIcon(getClass().getResource("resources/ascending.png"));
      final ImageIcon descendingSortIcon = new ImageIcon(getClass().getResource("resources/descending.png"));
      
      public Component getTableCellRendererComponent(JTable table, 
           Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        // Get default label
        JLabel label = (JLabel)currentRenderer.getTableCellRendererComponent(
            table, value, isSelected, hasFocus, row, column);
        // Add to column an icon matching sort
        TableColumnModel columnModel = getColumnModel();
        if (columnModel.getColumn(column).getIdentifier().equals(
            home.getFurnitureSortedProperty())) {
          label.setHorizontalTextPosition(JLabel.LEADING);
          if (home.isFurnitureDescendingSorted()) {
            label.setIcon(descendingSortIcon);
          } else {
            label.setIcon(ascendingSortIcon);
          }
        } else {
          label.setIcon(null);
        }
        return label;
      }
    });
  }
  
  /**
   * Adds a mouse listener on table header that will call <code>controller</code> sort method.
   */
  private void setTableHeaderListener(final FurnitureController controller) {
    // Sort on click in column header 
    getTableHeader().addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent ev) {
        int columnIndex = getTableHeader().columnAtPoint(ev.getPoint());
        HomePieceOfFurniture.SortableProperty property = 
            (HomePieceOfFurniture.SortableProperty)getColumnModel().getColumn(columnIndex).getIdentifier();
        controller.sortFurniture(property);
      }
    });
  }

  /**
   * Model used by this table
   */
  private static class FurnitureTableModel extends AbstractTableModel {
    private String []                        columnNames;
    private List<HomePieceOfFurniture>       sortedFurniture;
    
    public FurnitureTableModel(Home home, String [] columnNames) {
      this.columnNames = columnNames;
      addHomeListener(home);
      sortFurniture(home);
    }

    private void addHomeListener(final Home home) {
      home.addFurnitureListener(new FurnitureListener() {
        public void pieceOfFurnitureChanged(FurnitureEvent ev) {
          int pieceIndex = ev.getIndex();
          HomePieceOfFurniture piece = (HomePieceOfFurniture)ev.getPieceOfFurniture();
          switch (ev.getType()) {
            case ADD :
              int sortedIndex = getPieceOfFurnitureIndex(piece, home, pieceIndex);
              sortedFurniture.add(sortedIndex, piece);
              fireTableRowsInserted(sortedIndex, sortedIndex);
              break;
            case DELETE :
              sortedIndex = getPieceOfFurnitureIndex(piece, home, pieceIndex);
              sortedFurniture.remove(sortedIndex);
              fireTableRowsDeleted(sortedIndex, sortedIndex);
              break;
          }
        }

        /**
         * Returns the index of <code>piece</code> in furniture table, with a default index
         * of <code>homePieceIndex</code> if <code>home</code> furniture isn't sorted.
         * If <code>piece</code> isn't added to furniture table, the returned value is
         * equals to the insertion index where piece should be added.
         */
        private int getPieceOfFurnitureIndex(HomePieceOfFurniture piece, Home home, int homePieceIndex) {
          if (home.getFurnitureSortedProperty() == null) {
            return homePieceIndex;
          } else {
            int sortedIndex = Collections.binarySearch(sortedFurniture, piece, getFurnitureComparator(home));
            if (sortedIndex >= 0) {
              return sortedIndex;
            } else {
              return -(sortedIndex + 1);
            }              
          }
        }
      });
    }

    @Override
    public String getColumnName(int columnIndex) {
      return this.columnNames [columnIndex];
    }

    public int getColumnCount() {
      return this.columnNames.length;
    }

    public int getRowCount() {
      return this.sortedFurniture.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
      HomePieceOfFurniture piece = this.sortedFurniture.get(rowIndex);
      switch (columnIndex) {
        case 0 : return piece;
        case 1 : return piece.getWidth();
        case 2 : return piece.getHeight();
        case 3 : return piece.getDepth();
        case 4 : return piece.getColor();
        case 5 : return piece.isMovable();
        case 6 : return piece.isDoorOrWindow();
        case 7 : return piece.isVisible();
        default : throw new IllegalArgumentException("Unknown column " + columnIndex);
      }
    }

    /**
     * Returns the index of <code>piece</code> in furniture table.
     */
    public int getPieceOfFurnitureIndex(HomePieceOfFurniture piece) {
      return this.sortedFurniture.indexOf(piece);
    }

    /**
     * Sorts <code>home</code> furniture.
     */
    public void sortFurniture(Home home) {
      this.sortedFurniture = new ArrayList<HomePieceOfFurniture>(home.getFurniture());           
      if (home.getFurnitureSortedProperty() != null) {
        Comparator<HomePieceOfFurniture> furnitureComparator = getFurnitureComparator(home);
        Collections.sort(this.sortedFurniture, furnitureComparator);         
      }
      
      fireTableRowsUpdated(0, getRowCount() - 1);
    }

    private Comparator<HomePieceOfFurniture> getFurnitureComparator(Home home) {
      Comparator<HomePieceOfFurniture> furnitureComparator = 
        HomePieceOfFurniture.getFurnitureComparator(home.getFurnitureSortedProperty());
      if (home.isFurnitureDescendingSorted()) {
        furnitureComparator = Collections.reverseOrder(furnitureComparator);
      }
      return furnitureComparator;
    }
  }
}
