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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
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
  /**
   * The properties that may be edited by the view associated to this controller. 
   */
  public enum Property {NAME, X, Y, ELEVATION, ANGLE_IN_DEGREES, 
      WIDTH, DEPTH,  HEIGHT, COLOR, VISIBLE, MODEL_MIRRORED, RESIZABLE}
  
  private final Home                  home;
  private final UserPreferences       preferences;
  private final ViewFactory           viewFactory;
  private final UndoableEditSupport   undoSupport;
  private final PropertyChangeSupport propertyChangeSupport;
  private DialogView                  homeFurnitureView;

  private String name;
  private Float x;
  private Float y;
  private Float elevation;
  private Integer angleInDegrees;
  private Float width;
  private Float depth;
  private Float height;
  private Integer color;
  private Boolean visible;
  private Boolean modelMirrored;
  private boolean resizable;
  
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
    this.propertyChangeSupport = new PropertyChangeSupport(this);
    
    updateProperties();
  }

  /**
   * Returns the view associated with this controller.
   */
  public DialogView getView() {
    // Create view lazily only once it's needed
    if (this.homeFurnitureView == null) {
      this.homeFurnitureView = this.viewFactory.createHomeFurnitureView(
          this.preferences, this); 
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
   * Adds the property change <code>listener</code> in parameter to this controller.
   */
  public void addPropertyChangeListener(Property property, PropertyChangeListener listener) {
    this.propertyChangeSupport.addPropertyChangeListener(property.name(), listener);
  }

  /**
   * Removes the property change <code>listener</code> in parameter from this controller.
   */
  public void removePropertyChangeListener(Property property, PropertyChangeListener listener) {
    this.propertyChangeSupport.removePropertyChangeListener(property.name(), listener);
  }

  /**
   * Updates edited properties from selected furniture in the home edited by this controller.
   */
  protected void updateProperties() {
    List<HomePieceOfFurniture> selectedFurniture = 
        Home.getFurnitureSubList(this.home.getSelectedItems());
    if (selectedFurniture.isEmpty()) {
      setName(null); // Nothing to edit
      setAngleInDegrees(null);
      setX(null);
      setY(null);
      setElevation(null);
      setWidth(null);
      setDepth(null);
      setHeight(null);
      setColor(null);
      setVisible(null);
      setModelMirrored(null);
    } else {
      // Search the common properties among selected furniture
      HomePieceOfFurniture firstPiece = selectedFurniture.get(0);
      String name = firstPiece.getName();
      if (name != null) {
        for (int i = 1; i < selectedFurniture.size(); i++) {
          if (!name.equals(selectedFurniture.get(i).getName())) {
            name = null;
            break;
          }
        }
      }
      setName(name);
      
      Float angle = firstPiece.getAngle();
      for (int i = 1; i < selectedFurniture.size(); i++) {
        if (angle.floatValue() != selectedFurniture.get(i).getAngle()) {
          angle = null;
          break;
        }
      }
      if (angle == null) {
        setAngleInDegrees(null);
      } else {
        setAngleInDegrees((int)(Math.round(Math.toDegrees(angle)) + 360) % 360);
      }      

      Float x = firstPiece.getX();
      for (int i = 1; i < selectedFurniture.size(); i++) {
        if (x.floatValue() != selectedFurniture.get(i).getX()) {
          x = null;
          break;
        }
      }
      setX(x);

      Float y = firstPiece.getY();
      for (int i = 1; i < selectedFurniture.size(); i++) {
        if (y.floatValue() != selectedFurniture.get(i).getY()) {
          y = null;
          break;
        }
      }
      setY(y);

      Float elevation = firstPiece.getElevation();
      for (int i = 1; i < selectedFurniture.size(); i++) {
        if (elevation.floatValue() != selectedFurniture.get(i).getElevation()) {
          elevation = null;
          break;
        }
      }
      setElevation(elevation);

      Float width = firstPiece.getWidth();
      for (int i = 1; i < selectedFurniture.size(); i++) {
        if (width.floatValue() != selectedFurniture.get(i).getWidth()) {
          width = null;
          break;
        }
      }
      setWidth(width);

      Float depth = firstPiece.getDepth();
      for (int i = 1; i < selectedFurniture.size(); i++) {
        if (depth.floatValue() != selectedFurniture.get(i).getDepth()) {
          depth = null;
          break;
        }
      }
      setDepth(depth);

      Float height = firstPiece.getHeight();
      for (int i = 1; i < selectedFurniture.size(); i++) {
        if (height.floatValue() != selectedFurniture.get(i).getHeight()) {
          height = null;
          break;
        }
      }
      setHeight(height);

      Integer color = firstPiece.getColor();
      if (color != null) {
        for (int i = 1; i < selectedFurniture.size(); i++) {
          if (!color.equals(selectedFurniture.get(i).getColor())) {
            color = null;
            break;
          }
        }
      }
      setColor(color);

      Boolean visible = firstPiece.isVisible();
      for (int i = 1; i < selectedFurniture.size(); i++) {
        if (visible != selectedFurniture.get(i).isVisible()) {
          visible = null;
          break;
        }
      }
      setVisible(visible);           

      Boolean modelMirrored = firstPiece.isModelMirrored();
      for (int i = 1; i < selectedFurniture.size(); i++) {
        if (modelMirrored != selectedFurniture.get(i).isModelMirrored()) {
          modelMirrored = null;
          break;
        }
      }
      setModelMirrored(modelMirrored);     
      
      // Enable size components only if all pieces are resizable
      Boolean resizable = firstPiece.isResizable();
      for (int i = 1; i < selectedFurniture.size(); i++) {
        if (resizable.booleanValue() != selectedFurniture.get(i).isResizable()) {
          resizable = null;
          break;
        }
      }
      setResizable(resizable != null && resizable.booleanValue());
    }
  }  
  
  /**
   * Sets the edited name.
   */
  public void setName(String name) {
    if (name != this.name) {
      String oldName = this.name;
      this.name = name;
      this.propertyChangeSupport.firePropertyChange(Property.NAME.name(), oldName, name);
    }
  }

  /**
   * Returns the edited name.
   */
  public String getName() {
    return this.name;
  }
  
  /**
   * Sets the edited abscissa.
   */
  public void setX(Float x) {
    if (x != this.x) {
      Float oldX = this.x;
      this.x = x;
      this.propertyChangeSupport.firePropertyChange(Property.X.name(), oldX, x);
    }
  }

  /**
   * Returns the edited abscissa.
   */
  public Float getX() {
    return this.x;
  }
  
  /**
   * Sets the edited ordinate.
   */
  public void setY(Float y) {
    if (y != this.y) {
      Float oldY = this.y;
      this.y = y;
      this.propertyChangeSupport.firePropertyChange(Property.Y.name(), oldY, y);
    }
  }

  /**
   * Returns the edited ordinate.
   */
  public Float getY() {
    return this.y;
  }
  
  /**
   * Sets the edited elevation.
   */
  public void setElevation(Float elevation) {
    if (elevation != this.elevation) {
      Float oldElevation = this.elevation;
      this.elevation = elevation;
      this.propertyChangeSupport.firePropertyChange(Property.ELEVATION.name(), oldElevation, elevation);
    }
  }

  /**
   * Returns the edited elevation.
   */
  public Float getElevation() {
    return this.elevation;
  }
  
  /**
   * Sets the edited angle in degrees.
   */
  public void setAngleInDegrees(Integer angleInDegrees) {
    if (angleInDegrees != this.angleInDegrees) {
      Integer oldAngleInDegrees = this.angleInDegrees;
      this.angleInDegrees = angleInDegrees;
      this.propertyChangeSupport.firePropertyChange(Property.ANGLE_IN_DEGREES.name(), oldAngleInDegrees, angleInDegrees);
    }
  }

  /**
   * Returns the edited angle.
   */
  public Integer getAngleInDegrees() {
    return this.angleInDegrees;
  }
  
  /**
   * Sets the edited width.
   */
  public void setWidth(Float width) {
    if (width != this.width) {
      Float oldWidth = this.width;
      this.width = width;
      this.propertyChangeSupport.firePropertyChange(Property.WIDTH.name(), oldWidth, width);
    }
  }

  /**
   * Returns the edited width.
   */
  public Float getWidth() {
    return this.width;
  }
  
  /**
   * Sets the edited depth.
   */
  public void setDepth(Float depth) {
    if (depth != this.depth) {
      Float oldDepth = this.depth;
      this.depth = depth;
      this.propertyChangeSupport.firePropertyChange(Property.DEPTH.name(), oldDepth, depth);
    }
  }

  /**
   * Returns the edited depth.
   */
  public Float getDepth() {
    return this.depth;
  }
  
  /**
   * Sets the edited height.
   */
  public void setHeight(Float height) {
    if (height != this.height) {
      Float oldHeight = this.height;
      this.height = height;
      this.propertyChangeSupport.firePropertyChange(Property.HEIGHT.name(), oldHeight, height);
    }
  }

  /**
   * Returns the edited height.
   */
  public Float getHeight() {
    return this.height;
  }
  
  /**
   * Sets the edited color.
   */
  public void setColor(Integer color) {
    if (color != this.color) {
      Integer oldColor = this.color;
      this.color = color;
      this.propertyChangeSupport.firePropertyChange(Property.COLOR.name(), oldColor, color);
    }
  }

  /**
   * Returns the edited color.
   */
  public Integer getColor() {
    return this.color;
  }
  
  /**
   * Sets whether furniture is visible or not.
   */
  public void setVisible(Boolean visible) {
    if (visible != this.visible) {
      Boolean oldVisible = this.visible;
      this.visible = visible;
      this.propertyChangeSupport.firePropertyChange(Property.VISIBLE.name(), oldVisible, visible);
    }
  }

  /**
   * Returns whether furniture is visible or not.
   */
  public Boolean getVisible() {
    return this.visible;
  }

  /**
   * Sets whether furniture model is mirrored or not.
   */
  public void setModelMirrored(Boolean modelMirrored) {
    if (modelMirrored != this.modelMirrored) {
      Boolean oldModelMirrored = this.modelMirrored;
      this.modelMirrored = modelMirrored;
      this.propertyChangeSupport.firePropertyChange(Property.MODEL_MIRRORED.name(), oldModelMirrored, modelMirrored);
    }
  }

  /**
   * Returns whether furniture model is mirrored or not.
   */
  public Boolean getModelMirrored() {
    return this.modelMirrored;
  }
  
  /**
   * Sets whether furniture model can be resized or not.
   */
  public void setResizable(boolean resizable) {
    if (resizable != this.resizable) {
      boolean oldResizable = this.resizable;
      this.resizable = resizable;
      this.propertyChangeSupport.firePropertyChange(Property.RESIZABLE.name(), oldResizable, resizable);
    }
  }

  /**
   * Returns whether furniture model can be resized or not.
   */
  public boolean isResizable() {
    return this.resizable;
  }
  
  /**
   * Controls the modification of selected furniture in the edited home.
   */
  public void modifyFurniture() {
    final List<Selectable> oldSelection = this.home.getSelectedItems(); 
    List<HomePieceOfFurniture> selectedFurniture = 
        Home.getFurnitureSubList(oldSelection);
    if (!selectedFurniture.isEmpty()) {
      final String name = getName();
      final Float x = getX();
      final Float y = getY();
      final Float elevation = getElevation();
      final Float angle = getAngleInDegrees() != null
          ? (float)Math.toRadians(getAngleInDegrees())  : null;
      final Float width = getWidth();
      final Float depth = getDepth();
      final Float height = getHeight();
      final Integer color = getColor();
      final Boolean visible = getVisible();
      final Boolean modelMirrored = getModelMirrored();
      
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
