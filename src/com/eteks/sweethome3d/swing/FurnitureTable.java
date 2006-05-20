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

import java.util.List;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

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
    // TODO Auto-generated constructor stub

    ListSelectionListener selectionListener = new ListSelectionListener () {
      public void valueChanged(ListSelectionEvent ev) {
        if (ev.getValueIsAdjusting()) {
          int firstIndex = ev.getFirstIndex();
          int lastIndex = ev.getLastIndex();
        }
      } 
    };
    getSelectionModel().addListSelectionListener(selectionListener);
  }

  /**
   * Returns the list of selected furniture in table.
   */
  public List<HomePieceOfFurniture> getSelectedFurniture() {
    // TODO Return the list of selected furniture in table
    // Reduce returned list to its minimum size
    return null;
  }

  /**
   * Sets the list of selected furniture in table and ensures the first and the
   * last one is visible.
   * @param furniture the furniture to select
   */
  public void setSelectedFurniture(List<HomePieceOfFurniture> furniture) {
    // TODO Auto-generated method stub
    // Remove selectionListener before changing selection
  }
}
