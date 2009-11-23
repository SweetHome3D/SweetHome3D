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

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeDoorOrWindow;
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
  public enum Property {NAME, NAME_VISIBLE, X, Y, ELEVATION, ANGLE_IN_DEGREES, 
      WIDTH, DEPTH,  HEIGHT, COLOR, VISIBLE, MODEL_MIRRORED, RESIZABLE}
  
  private final Home                  home;
  private final UserPreferences       preferences;
  private final ViewFactory           viewFactory;
  private final UndoableEditSupport   undoSupport;
  private final PropertyChangeSupport propertyChangeSupport;
  private DialogView                  homeFurnitureView;

  private String  name;
  private Boolean nameVisible;
  private Float   x;
  private Float   y;
  private Float   elevation;
  private Integer angleInDegrees;
  private Float   width;
  private Float   depth;
  private Float   height;
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
      setNameVisible(null); 
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
      
      Boolean nameVisible = firstPiece.isNameVisible();
      for (int i = 1; i < selectedFurniture.size(); i++) {
        if (nameVisible != selectedFurniture.get(i).isNameVisible()) {
          nameVisible = null;
          break;
        }
      }
      setNameVisible(nameVisible);
      
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
   * Returns whether furniture name should be drawn or not. 
   */
  public Boolean getNameVisible() {
    return this.nameVisible;  
  }
  
  /**
   * Sets whether furniture name is visible or not.
   */
  public void setNameVisible(Boolean nameVisible) {
    if (nameVisible != this.nameVisible) {
      Boolean oldNameVisible = this.nameVisible;
      this.nameVisible = nameVisible;
      this.propertyChangeSupport.firePropertyChange(Property.NAME_VISIBLE.name(), oldNameVisible, nameVisible);
    }
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
    List<Selectable> oldSelection = this.home.getSelectedItems(); 
    List<HomePieceOfFurniture> selectedFurniture = 
        Home.getFurnitureSubList(oldSelection);
    if (!selectedFurniture.isEmpty()) {
      String name = getName();
      Boolean nameVisible = getNameVisible();
      Float x = getX();
      Float y = getY();
      Float elevation = getElevation();
      Float angle = getAngleInDegrees() != null
          ? (float)Math.toRadians(getAngleInDegrees())  : null;
      Float width = getWidth();
      Float depth = getDepth();
      Float height = getHeight();
      Integer color = getColor();
      Boolean visible = getVisible();
      Boolean modelMirrored = getModelMirrored();
      
      // Create an array of modified furniture with their current properties values
      ModifiedPieceOfFurniture [] modifiedFurniture = 
          new ModifiedPieceOfFurniture [selectedFurniture.size()]; 
      for (int i = 0; i < modifiedFurniture.length; i++) {
        HomePieceOfFurniture piece = selectedFurniture.get(i);
        if (piece instanceof HomeDoorOrWindow) {
          modifiedFurniture [i] = new ModifiedDoorOrWindow((HomeDoorOrWindow)piece);
        } else {
          modifiedFurniture [i] = new ModifiedPieceOfFurniture(piece);
        }
      }
      // Apply modification
      doModifyFurniture(modifiedFurniture, 
          name, nameVisible, x, y, width, depth, height, elevation, angle, color, visible, modelMirrored); 
      if (this.undoSupport != null) {
        UndoableEdit undoableEdit = new FurnitureModificationUndoableEdit(
            this.home, this.preferences, oldSelection, modifiedFurniture, 
            name, nameVisible, x, y, width, depth, height, 
            elevation, angle, color, modelMirrored, visible);
        this.undoSupport.postEdit(undoableEdit);
      }
    }
  }
  
  /**
   * Undoable edit for furniture modification. This class isn't anonymous to avoid
   * being bound to controller and its view.
   */
  private static class FurnitureModificationUndoableEdit extends AbstractUndoableEdit {
    private final Home                        home;
    private final UserPreferences             preferences;
    private final ModifiedPieceOfFurniture [] modifiedFurniture;
    private final List<Selectable>            oldSelection;
    private final String                      name;
    private final Boolean                     nameVisible;
    private final Float                       x;
    private final Float                       y;
    private final Float                       width;
    private final Float                       depth;
    private final Float                       height;
    private final Float                       elevation;
    private final Float                       angle;
    private final Integer                     color;
    private final Boolean                     modelMirrored;
    private final Boolean                     visible;

    private FurnitureModificationUndoableEdit(Home home,
                                              UserPreferences preferences, 
                                              List<Selectable> oldSelection,
                                              ModifiedPieceOfFurniture [] modifiedFurniture,
                                              String name, Boolean nameVisible, 
                                              Float x, Float y,
                                              Float width, Float depth, Float height,
                                              Float elevation, Float angle,
                                              Integer color,
                                              Boolean modelMirrored,
                                              Boolean visible) {
      this.home = home;
      this.preferences = preferences;
      this.oldSelection = oldSelection;
      this.modifiedFurniture = modifiedFurniture;
      this.name = name;
      this.nameVisible = nameVisible;
      this.x = x;
      this.y = y;
      this.width = width;
      this.depth = depth;
      this.height = height;
      this.elevation = elevation;
      this.angle = angle;
      this.modelMirrored = modelMirrored;
      this.visible = visible;
      this.color = color;
    }

    @Override
    public void undo() throws CannotUndoException {
      super.undo();
      undoModifyFurniture(this.modifiedFurniture); 
      home.setSelectedItems(this.oldSelection); 
    }

    @Override
    public void redo() throws CannotRedoException {
      super.redo();
      doModifyFurniture(this.modifiedFurniture, 
          this.name, this.nameVisible, this.x, this.y, this.width, 
          this.depth, this.height, this.elevation, this.angle, this.color, this.visible, this.modelMirrored); 
      home.setSelectedItems(this.oldSelection); 
    }

    @Override
    public String getPresentationName() {
      return this.preferences.getLocalizedString(HomeFurnitureController.class, 
          "undoModifyFurnitureName");
    }
  }

  /**
   * Modifies furniture properties with the values in parameter.
   */
  private static void doModifyFurniture(ModifiedPieceOfFurniture [] modifiedFurniture, 
                                        String name, Boolean nameVisible, 
                                        Float x, Float y, Float width, 
                                        Float depth, Float height, Float elevation, 
                                        Float angle, Integer color, 
                                        Boolean visible, Boolean modelMirrored) {
    for (ModifiedPieceOfFurniture modifiedPiece : modifiedFurniture) {
      HomePieceOfFurniture piece = modifiedPiece.getPieceOfFurniture();
      piece.setName(name != null 
          ? name : piece.getName());
      piece.setNameVisible(nameVisible != null 
          ? nameVisible : piece.isNameVisible());
      piece.setX(x != null 
          ? x.floatValue() : piece.getX());
      piece.setY(y != null 
          ? y.floatValue() : piece.getY());
      piece.setAngle(angle != null ? angle.floatValue() : piece.getAngle());
      if (piece.isResizable()) {
        piece.setWidth(width != null 
            ? width.floatValue() : piece.getWidth());
        piece.setDepth(depth != null  
            ? depth.floatValue() : piece.getDepth());
        piece.setHeight(height != null  
            ? height.floatValue() : piece.getHeight());
        piece.setModelMirrored(modelMirrored != null  
            ? modelMirrored.booleanValue() : piece.isModelMirrored());
      }
      piece.setElevation(elevation != null 
          ? elevation.floatValue() : piece.getElevation());
      piece.setColor(color != null 
          ? color : piece.getColor());
      piece.setVisible(visible != null 
          ? visible.booleanValue() : piece.isVisible());
    }
  }

  /**
   * Restores furniture properties from the values stored in <code>modifiedFurniture</code>.
   */
  private static void undoModifyFurniture(ModifiedPieceOfFurniture [] modifiedFurniture) {
    for (ModifiedPieceOfFurniture modifiedPiece : modifiedFurniture) {
      modifiedPiece.reset();
    }
  }

  /**
   * Stores the current properties values of a modified piece of furniture.
   */
  private static class ModifiedPieceOfFurniture {
    private final HomePieceOfFurniture piece;
    private final String  name;
    private final boolean nameVisible;
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
      this.nameVisible = piece.isNameVisible();
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
        
    public void reset() {
      this.piece.setName(this.name);
      this.piece.setNameVisible(this.nameVisible);
      this.piece.setX(this.x);
      this.piece.setY(this.y);
      this.piece.setElevation(this.elevation);
      this.piece.setAngle(this.angle);
      if (this.piece.isResizable()) {
        this.piece.setWidth(this.width);
        this.piece.setDepth(this.depth);
        this.piece.setHeight(this.height);
        this.piece.setModelMirrored(modelMirrored);
      }
      this.piece.setColor(this.color);
      this.piece.setVisible(this.visible);
    }
  }
  
  /**
   * Stores the current properties values of a modified door or window.
   */
  private static class ModifiedDoorOrWindow extends ModifiedPieceOfFurniture {
    private final boolean boundToWall;
    
    public ModifiedDoorOrWindow(HomeDoorOrWindow doorOrWindow) {
      super(doorOrWindow);
      this.boundToWall = doorOrWindow.isBoundToWall();
    }

    public void reset() {
      super.reset();
      ((HomeDoorOrWindow)getPieceOfFurniture()).setBoundToWall(this.boundToWall);
    }
  }
}
