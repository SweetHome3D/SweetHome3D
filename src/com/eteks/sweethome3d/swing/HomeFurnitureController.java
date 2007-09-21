/*
 * HomeFurnitureController.java 30 mai 07
 *
 * Copyright (c) 2007 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * A MVC controller for home furniture view.
 * @author Emmanuel Puybaret
 */
public class HomeFurnitureController {
  private Home                home;
  private UndoableEditSupport undoSupport;
  private ResourceBundle      resource;
  private JComponent          homeFurnitureView;

  /**
   * Creates the controller of home furniture view with undo support.
   */
  public HomeFurnitureController(Home home, 
                        UserPreferences preferences, 
                        UndoableEditSupport undoSupport) {
    this.home = home;
    this.undoSupport = undoSupport;
    this.resource    = ResourceBundle.getBundle(
        HomeFurnitureController.class.getName());
    this.homeFurnitureView = new HomeFurniturePanel(home, preferences, this); 
    ((HomeFurniturePanel)this.homeFurnitureView).displayView();
  }

  /**
   * Returns the view associated with this controller.
   */
  public JComponent getView() {
    return this.homeFurnitureView;
  }

  /**
   * Controls the modification of selected furniture.
   */
  public void modifySelection() {
    final List<Object> oldSelection = this.home.getSelectedItems(); 
    List<HomePieceOfFurniture> selectedFurniture = 
        Home.getFurnitureSubList(oldSelection);
    if (!selectedFurniture.isEmpty()) {
      HomeFurniturePanel furniturePanel = (HomeFurniturePanel)getView();
      final String name = furniturePanel.getFurnitureName();
      final Float x = furniturePanel.getFurnitureX();
      final Float y = furniturePanel.getFurnitureY();
      final Float elevation = furniturePanel.getFurnitureElevation();
      final Float angle = furniturePanel.getFurnitureAngle();
      final Float width = furniturePanel.getFurnitureWidth();
      final Float depth = furniturePanel.getFurnitureDepth();
      final Float height = furniturePanel.getFurnitureHeight();
      final Integer color = furniturePanel.getFurnitureColor();
      final Boolean visible = furniturePanel.isFurnitureVisible();
      final Boolean modelMirrored = furniturePanel.isFurnitureModelMirrored();
      
      // Create an array of modified furniture with their current properties values
      final ModifiedPieceOfFurniture [] modifiedFurniture = 
          new ModifiedPieceOfFurniture [selectedFurniture.size()]; 
      for (int i = 0; i < modifiedFurniture.length; i++) {
        modifiedFurniture [i] = new ModifiedPieceOfFurniture(selectedFurniture.get(i));
      }
      // Apply modification
      doModifyFurniture(modifiedFurniture, 
          name, width, depth, height, x, y, elevation, angle, color, visible, modelMirrored); 
      if (this.undoSupport != null) {
        UndoableEdit undoableEdit = new AbstractUndoableEdit() {
          @Override
          public void undo() throws CannotUndoException {
            super.undo();
            undoModifyFurniture(modifiedFurniture); 
            home.setSelectedItems(oldSelection); 
          }
          
          @Override
          public void redo() throws CannotRedoException {
            super.redo();
            doModifyFurniture(modifiedFurniture, 
                name, width, depth, height, x, y, elevation, angle, color, visible, modelMirrored); 
            home.setSelectedItems(oldSelection); 
          }
          
          @Override
          public String getPresentationName() {
            return resource.getString("undoModifyFurnitureName");
          }
        };
        this.undoSupport.postEdit(undoableEdit);
      }
    }
  }
  
  /**
   * Modifies furniture properties with the values in parameter.
   */
  private void doModifyFurniture(ModifiedPieceOfFurniture [] modifiedFurniture, 
                                 String name, Float width, Float depth, Float height, 
                                 Float x, Float y, Float elevation, 
                                 Float angle, Integer color, 
                                 Boolean visible, Boolean modelMirrored) {
    for (ModifiedPieceOfFurniture modifiedPiece : modifiedFurniture) {
      HomePieceOfFurniture piece = modifiedPiece.getPieceOfFurniture();
      this.home.setPieceOfFurnitureName(piece, 
          name != null ? name : piece.getName());
      this.home.setPieceOfFurnitureLocation(piece, 
          x != null ? x.floatValue() : piece.getX(), 
          y != null ? y.floatValue() : piece.getY());
      this.home.setPieceOfFurnitureAngle(piece, 
          angle != null ? angle.floatValue() : piece.getAngle());
      this.home.setPieceOfFurnitureSize(piece, 
          width != null ? width.floatValue() : piece.getWidth(), 
          depth != null ? depth.floatValue() : piece.getDepth(), 
          height != null ? height.floatValue() : piece.getHeight());
      this.home.setPieceOfFurnitureElevation(piece, 
          elevation != null ? elevation.floatValue() : piece.getElevation());
      this.home.setPieceOfFurnitureColor(piece, 
          color != null ? color : piece.getColor());
      this.home.setPieceOfFurnitureVisible(piece, 
          visible != null ? visible.booleanValue() : piece.isVisible());
      this.home.setPieceOfFurnitureModelMirrored(piece, 
          modelMirrored != null ? modelMirrored.booleanValue() : piece.isModelMirrored());
    }
  }

  /**
   * Restores furniture properties from the values stored in <code>modifiedFurnitures</code>.
   */
  private void undoModifyFurniture(ModifiedPieceOfFurniture [] modifiedFurniture) {
    for (ModifiedPieceOfFurniture modifiedPiece : modifiedFurniture) {
      HomePieceOfFurniture piece = modifiedPiece.getPieceOfFurniture();
      this.home.setPieceOfFurnitureName(piece, modifiedPiece.getName());
      this.home.setPieceOfFurnitureLocation(piece, modifiedPiece.getX(), modifiedPiece.getY());
      this.home.setPieceOfFurnitureElevation(piece, modifiedPiece.getElevation());
      this.home.setPieceOfFurnitureAngle(piece, modifiedPiece.getAngle());
      this.home.setPieceOfFurnitureSize(piece, 
          modifiedPiece.getWidth(), modifiedPiece.getDepth(), modifiedPiece.getHeight());
      this.home.setPieceOfFurnitureColor(piece, modifiedPiece.getColor());
      this.home.setPieceOfFurnitureVisible(piece, modifiedPiece.isVisible());
      this.home.setPieceOfFurnitureModelMirrored(piece, modifiedPiece.isModelMirrored());
    }
  }

  /**
   * Stores the current properties values of a modified piece of furniture.
   */
  private static final class ModifiedPieceOfFurniture {
    private final HomePieceOfFurniture piece;
    private final String  name;
    private final float   x;
    private final float   y;
    private final float   elevation;
    private final float   angle;
    private final float   width;
    private final float   depth;
    private final float   height;
    private final Integer color;
    private final boolean visible;
    private final boolean modelMirrored;

    public ModifiedPieceOfFurniture(HomePieceOfFurniture piece) {
      this.piece = piece;
      this.name = piece.getName();
      this.x = piece.getX();
      this.y = piece.getY();
      this.elevation = piece.getElevation();
      this.angle = piece.getAngle();
      this.width = piece.getWidth();
      this.depth = piece.getDepth();
      this.height = piece.getHeight();
      this.color = piece.getColor();
      this.visible = piece.isVisible();
      this.modelMirrored = piece.isModelMirrored();
    }

    public HomePieceOfFurniture getPieceOfFurniture() {
      return this.piece;
    }
    
    public String getName() {
      return this.name;
    }

    public float getDepth() {
      return this.depth;
    }

    public float getHeight() {
      return this.height;
    }

    public float getWidth() {
      return this.width;
    }

    public Integer getColor() {
      return this.color;
    }

    public boolean isVisible() {
      return this.visible;
    }
    
    public float getX() {
      return this.x;
    }

    public float getY() {
      return this.y;
    }

    public float getElevation() {
      return this.elevation;
    }

    public float getAngle() {
      return this.angle;
    }

    public boolean isModelMirrored() {
      return this.modelMirrored;
    }
  }
}
