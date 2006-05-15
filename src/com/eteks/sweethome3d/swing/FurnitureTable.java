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

import java.awt.Component;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.TableColumn;

import com.eteks.sweethome3d.model.HomePieceOfFurniture;

/**
 * A table displaying furniture.
 * @author Emmanuel Puybaret
 */
public class FurnitureTable extends JTable {
  /**
   * Returns the list of selected furniture in table.
   */
  public List<HomePieceOfFurniture> getSelectedFurniture() {
    // TODO Return the list of selected furniture in table
    return null;
  }

  /**
   * Sets the column used for sort.
   * @param nameColumn <code>null</code> if insert order or the column order. 
   */
  public void setSortedColumn(String column) {
    // TODO Store the column value
  }

  /**
   * Sets the sort order.
   * @param ascending ascending order if <code>true</code>.
   */
  public void setAscendingSort(boolean ascending) {
    // TODO Store the ascending value
    
  }
}
