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
package com.eteks.sweethome3d.viewcontroller;

import java.util.List;
import java.util.ResourceBundle;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * A MVC controller for home furniture view.
 * @author Emmanuel Puybaret
 */
public class HomeFurnitureController implements Controller {
  private final Home                home;
  private final UserPreferences     preferences;
  private final ViewFactory         viewFactory;
  private final UndoableEditSupport undoSupport;
  private HomeFurnitureView         homeFurnitureView;

  /**
   * Creates the controller of home furniture view with undo support.
   */
  public HomeFurnitureController(Home home, 
                                 UserPreferences preferences, 
                                 ViewFactory viewFactory, 
                                 UndoableEditSupport undoSupport) {
    this.home = home;
    this.preferences = preferences;
    this.viewFactory = viewFactory;
    this.undoSupport = undoSupport;
  }

  /**
   * Returns the view associated with this controller.
   */
  public HomeFurnitureView getView() {
    // Create view lazily only once it's needed
    if (this.homeFurnitureView == null) {
      this.homeFurnitureView = this.viewFactory.createHomeFurnitureView(
          this.home, this.preferences, this); 
    }
    return this.homeFurnitureView;
  }
  
  /**
   * Displays the view controlled by this controller.
   */
  public void displayView(View parentView) {
    getView().displayView(parentView);
  }

  /**
   * Controls the modification of selected furniture.
   */
  public void modifySelection() {
    final List<Selectable> oldSelection = this.home.getSelectedItems(); 
    List<HomePieceOfFurniture> selectedFurniture = 
        Home.getFurnitureSubList(oldSelection);
    if (!selectedFurniture.isEmpty()) {
      HomeFurnitureView furnitureView = getView();
      final String name = furnitureView.getFurnitureName();
      final Float x = furnitureView.getFurnitureX();
      final Float y = furnitureView.getFurnitureY();
      final Float elevation = furnitureView.getFurnitureElevation();
      final Float angle = furnitureView.getFurnitureAngle();
      final Float width = furnitureView.getFurnitureWidth();
      final Float depth = furnitureView.getFurnitureDepth();
      final Float height = furnitureView.getFurnitureHeight();
      final Integer color = furnitureView.getFurnitureColor();
      final Boolean visible = furnitureView.isFurnitureVisible();
      final Boolean modelMirrored = furnitureView.isFurnitureModelMirrored();
      
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
            return ResourceBundle.getBundle(HomeFurnitureController.class.getName()).
                getString("undoModifyFurnitureName");
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
      piece.setName(name != null 
          ? name : piece.getName());
      piece.setX(x != null 
          ? x.floatValue() : piece.getX());
      piece.setY(y != null 
          ? y.floatValue() : piece.getY());
      piece.setAngle(angle != null ? angle.floatValue() : piece.getAngle());
      piece.setWidth(width != null && piece.isResizable() 
          ? width.floatValue() : piece.getWidth());
      piece.setDepth(depth != null && piece.isResizable() 
          ? depth.floatValue() : piece.getDepth());
      piece.setHeight(height != null && piece.isResizable() 
          ? height.floatValue() : piece.getHeight());
      piece.setElevation(elevation != null 
          ? elevation.floatValue() : piece.getElevation());
      piece.setColor(color != null 
          ? color : piece.getColor());
      piece.setVisible(visible != null 
          ? visible.booleanValue() : piece.isVisible());
      piece.setModelMirrored(modelMirrored != null && piece.isResizable() 
          ? modelMirrored.booleanValue() : piece.isModelMirrored());
    }
  }

  /**
   * Restores furniture properties from the values stored in <code>modifiedFurnitures</code>.
   */
  private void undoModifyFurniture(ModifiedPieceOfFurniture [] modifiedFurniture) {
    for (ModifiedPieceOfFurniture modifiedPiece : modifiedFurniture) {
      HomePieceOfFurniture piece = modifiedPiece.getPieceOfFurniture();
      piece.setName(modifiedPiece.getName());
      piece.setX(modifiedPiece.getX());
      piece.setY(modifiedPiece.getY());
      piece.setElevation(modifiedPiece.getElevation());
      piece.setAngle(modifiedPiece.getAngle());
      if (piece.isResizable()) {
        piece.setWidth(modifiedPiece.getWidth());
        piece.setDepth(modifiedPiece.getDepth());
        piece.setHeight(modifiedPiece.getHeight());
      }
      piece.setColor(modifiedPiece.getColor());
      piece.setVisible(modifiedPiece.isVisible());
      piece.setModelMirrored(modifiedPiece.isModelMirrored());
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
