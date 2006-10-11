/*
 * FurnitureController.java 15 mai 2006
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

import javax.swing.JComponent;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.FurnitureEvent;
import com.eteks.sweethome3d.model.FurnitureListener;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.PieceOfFurniture;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;

/**
 * A MVC controller for the furniture table.
 * @author Emmanuel Puybaret
 */
public class FurnitureController {
  private Home       home;
  private JComponent furnitureView;

  /**
   * Creates the controller of home furniture view.
   * @param home the home edited by this controller and its view
   * @param preferences the preferences of the application
   */
  public FurnitureController(Home home, 
                             UserPreferences preferences) {
    this.home = home;
    this.furnitureView = 
        new FurnitureTable(home, preferences, this); 
  }

  /**
   * Returns the view associated with this controller.
   */
  public JComponent getView() {
    return this.furnitureView;
  }

  /**
   * Controls new furniture added to home. Once added the furniture will be selected in view..
   * @param furniture the furniture to add.
   */
  public void addFurniture(List<HomePieceOfFurniture> furniture) {
    for (HomePieceOfFurniture piece : furniture) {
      this.home.addPieceOfFurniture(piece);
    }
    this.home.setSelectedItems(furniture);
  }

  /**
   * Controls the deletion of the current selected furniture in home.
   */
  public void deleteSelection() {
    for (Object item : this.home.getSelectedItems()) {
      if (item instanceof HomePieceOfFurniture) {
        this.home.deletePieceOfFurniture(
            (HomePieceOfFurniture)item);
      }
    }
  }

  /**
   * Updates the selected furniture in home.
   */
  public void setSelectedFurniture(
           List<HomePieceOfFurniture> selectedFurniture) {
    this.home.setSelectedItems(selectedFurniture);
  }
}
