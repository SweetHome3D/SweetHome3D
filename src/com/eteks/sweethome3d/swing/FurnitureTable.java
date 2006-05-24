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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.FurnitureEvent;
import com.eteks.sweethome3d.model.FurnitureListener;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * A table displaying furniture.
 * @author Emmanuel Puybaret
 */
public class FurnitureTable extends JTable {
  /**
   * Create this view associated with its controller.
   * @param controller  the controller of this view
   * @param home        the home displayed by this view
   * @param preferences the preferences of the application
   */
  public FurnitureTable(FurnitureController controller, Home home, UserPreferences preferences) {
    ResourceBundle resource = 
      ResourceBundle.getBundle(getClass().getName());
    String [] columnNames = getColumnNames(resource);
    setModel(new FurnitureTableModel(home, columnNames));
    setColumnRenderers(preferences);
  }

  /**
   * Returns localized column names.
   */
  private String [] getColumnNames(ResourceBundle resource) {
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
    TableColumnModel columnModel = getColumnModel();
    for (int i = 0, n = getColumnCount(); i < n; i++) {
      columnModel.getColumn(i).setCellRenderer(columnRenderers [i]);
    }
  }

  /**
   * Returns a renderer that displays its value with row piece of furniture icon ahead. 
   */
  private TableCellRenderer getNameWithIconRenderer() {
    final TableCellRenderer stringRenderer = getDefaultRenderer(String.class); 
    return new TableCellRenderer() {
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel label = (JLabel)stringRenderer.
           getTableCellRendererComponent(table, value, 
               isSelected, hasFocus, row, column);
        FurnitureTableModel model = (FurnitureTableModel)getModel();
        Content iconContent = model.getPieceOfFurnitureAtRow(row).getIcon();
        label.setIcon(IconManager.getInstance().getIcon(iconContent,
            getRowHeight(), FurnitureTable.this));
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
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (preferences.getUnit() == INCH) {
          value = centimerToInch(((Number)value).floatValue());
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
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
            row, column);
        if (value != null) {
          int color = (Integer)value;
          label.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2),
              BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(color ^ 0xFFFFFF), 1),
                                                 BorderFactory.createLineBorder(new Color(color), getRowHeight() / 2 - 2))));
          label.setText(null);
        } else {
          label.setBorder(null);
        }
        return label;
      } 
    };
  }

  /**
   * Returns the list of selected furniture in table.
   */
  public List<HomePieceOfFurniture> getSelectedFurniture() {
    FurnitureTableModel model = (FurnitureTableModel)getModel();
    int [] selectedRows = getSelectedRows();
    List<HomePieceOfFurniture> selectedFurniture = 
        new ArrayList<HomePieceOfFurniture>(selectedRows.length);
    for (int row : selectedRows) {
      selectedFurniture.add(model.getPieceOfFurnitureAtRow(row));
    }
    return selectedFurniture;
  }

  /**
   * Sets the list of selected furniture in table and ensures the first and the
   * last one is visible.
   * @param furniture the furniture to select
   */
  public void setSelectedFurniture(List<HomePieceOfFurniture> furniture) {
    clearSelection();
    FurnitureTableModel model = (FurnitureTableModel)getModel();
    for (HomePieceOfFurniture piece : furniture) {
      int row = model.getPieceOfFurnitureRow(piece);
      addRowSelectionInterval(row, row);
    }
  }

  /**
   * Ensures the rectangle which displays <code>furniture</code> is visible.
   */
  public void ensureFurnitureIsVisible(List<HomePieceOfFurniture> furniture) {
    if (!furniture.isEmpty()) {
      FurnitureTableModel model = (FurnitureTableModel)getModel();
      Rectangle includingRectangle = null;
      int lastColumn = getColumnCount() - 1;
      // Compute the rectangle that includes all the furniture 
      for (HomePieceOfFurniture piece : furniture) {
        int row = model.getPieceOfFurnitureRow(piece);
        if (includingRectangle == null) {
          includingRectangle = getCellRect(row, 0, true);
        } else {
          includingRectangle = includingRectangle.
              union(getCellRect(row, 0, true));
        }
        includingRectangle = includingRectangle.
            union(getCellRect(row, lastColumn, true));
      }
      // Scroll to make including rectangle visible
      scrollRectToVisible(includingRectangle);
    }
  }

  /**
   * Model used by this table
   */
  private static class FurnitureTableModel extends DefaultTableModel {
    // Copy of home furniture that contains the same furniture and 
    // the deleted ones before they are deleted
    private List<HomePieceOfFurniture> displayedFurniture;
    
    public FurnitureTableModel(Home home, String [] columnNames) {
      super(columnNames, 0);
      // Fill table with existing furniture
      this.displayedFurniture = 
        new ArrayList<HomePieceOfFurniture> (home.getFurniture());
      for (HomePieceOfFurniture piece : displayedFurniture)
        addPieceOfFurniture(home, piece);
      // Add a listener on home to receive furniture notifications
      home.addFurnitureListener(new FurnitureListener () {
        public void pieceOfFurnitureAdded(FurnitureEvent ev) {
          addPieceOfFurniture((Home)ev.getSource(), 
              (HomePieceOfFurniture)ev.getPieceOfFurniture());
        }
        public void pieceOfFurnitureDeleted(FurnitureEvent ev) {
          removePieceOfFurniture((HomePieceOfFurniture)ev.getPieceOfFurniture());
        }
      });
    }
    
    private void addPieceOfFurniture(Home home, HomePieceOfFurniture piece) {
      Object [] rowValues = {piece.getName(), piece.getWidth(),
                             piece.getHeight(), piece.getHeight(),
                             piece.getColor(), piece.isMovable(),
                             piece.isDoorOrWindow(), piece.isVisible()};
      addRow(rowValues);
      this.displayedFurniture.add(home.getFurniture().indexOf(piece), piece);
    }

    private void removePieceOfFurniture(HomePieceOfFurniture piece) {
      removeRow(getPieceOfFurnitureRow(piece));
      this.displayedFurniture.remove(piece);
    }
    
    private int getPieceOfFurnitureRow(HomePieceOfFurniture piece) {
      return this.displayedFurniture.indexOf(piece);
    }

    private HomePieceOfFurniture getPieceOfFurnitureAtRow(int row) {
      return this.displayedFurniture.get(row);
    }
  }
}
