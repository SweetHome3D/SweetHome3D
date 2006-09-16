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
import java.awt.Container;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.FurnitureEvent;
import com.eteks.sweethome3d.model.FurnitureListener;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.SelectionEvent;
import com.eteks.sweethome3d.model.SelectionListener;
import com.eteks.sweethome3d.model.UserPreferences;

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
    String [] columnNames = getColumnNames();
    setModel(new FurnitureTableModel(home, columnNames));
    setColumnRenderers(preferences);
    addSelectionListeners(home);
  }
  
  /**
   * Adds selection listeners to this table.
   */
  private void addSelectionListeners(final Home home) {   
    final SelectionListener homeSelectionListener  = 
      new SelectionListener() {
        public void selectionChanged(SelectionEvent ev) {
          getSelectionModel().removeListSelectionListener(tableSelectionListener);
          List<HomePieceOfFurniture> furniture = home.getFurniture();
          clearSelection();
          for (Object item : ev.getSelectedItems()) {
            if (item instanceof HomePieceOfFurniture) {
              int index = furniture.indexOf(item); 
              addRowSelectionInterval(index, index);
              makeRowVisible(index);
            }          
          }        
          getSelectionModel().addListSelectionListener(tableSelectionListener);
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
            List<HomePieceOfFurniture> furniture = home.getFurniture();
            for (int index : selectedRows) {
              selectedFurniture.add(furniture.get(index));
            }
            // Set the new selection in home
            home.setSelectedItems(selectedFurniture);
            home.addSelectionListener(homeSelectionListener);
          }
        }
      };
    getSelectionModel().addListSelectionListener(this.tableSelectionListener);
    home.addSelectionListener(homeSelectionListener);
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
    for (int i = 0, n = getColumnCount(); i < n; i++) {
      getColumn(getColumnName(i)).setCellRenderer(columnRenderers [i]);
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
            iconContent, getRowHeight(), FurnitureTable.this)); 
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
   * Model used by this table
   */
  private static class FurnitureTableModel extends AbstractTableModel {
    private Home      home;
    private String [] columnNames;
    
    public FurnitureTableModel(Home home, String [] columnNames) {
      this.home = home;
      this.columnNames = columnNames;
      addHomeListener(home);
    }

    private void addHomeListener(Home home) {
      home.addFurnitureListener(new FurnitureListener() {
        public void pieceOfFurnitureChanged(FurnitureEvent ev) {
          int pieceIndex = ev.getIndex();
          switch (ev.getType()) {
            case ADD :
              fireTableRowsInserted(pieceIndex, pieceIndex);
              break;
            case DELETE :
              fireTableRowsDeleted(pieceIndex, pieceIndex);
              break;
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
      return this.home.getFurniture().size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
      HomePieceOfFurniture piece = this.home.getFurniture().get(rowIndex);
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
  }
}
