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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeDoorOrWindow;
import com.eteks.sweethome3d.model.HomeFurnitureGroup;
import com.eteks.sweethome3d.model.HomeLight;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.HomeTexture;
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
  public enum Property {NAME, NAME_VISIBLE, X, Y, ELEVATION, ANGLE_IN_DEGREES, BASE_PLAN_ITEM, 
      WIDTH, DEPTH,  HEIGHT, PROPORTIONAL, COLOR, PAINT, SHININESS, VISIBLE, MODEL_MIRRORED, LIGHT_POWER, 
      RESIZABLE, DEFORMABLE, TEXTURABLE}
  
  /**
   * The possible values for {@linkplain #getPaint() paint type}.
   */
  public enum FurniturePaint {DEFAULT, COLORED, TEXTURED} 

  /**
   * The possible values for {@linkplain #getShininess() shininess type}.
   */
  public enum FurnitureShininess {DEFAULT, MATT, SHINY} 

  private final Home                  home;
  private final UserPreferences       preferences;
  private final ViewFactory           viewFactory;
  private final ContentManager        contentManager;
  private final UndoableEditSupport   undoSupport;
  private TextureChoiceController     textureController;
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
  private boolean proportional;
  private Integer color;
  private FurniturePaint     paint;
  private FurnitureShininess shininess;
  private Boolean visible;
  private Boolean modelMirrored;
  private Boolean basePlanItem;
  private boolean basePlanItemEditable;
  private boolean lightPowerEditable;
  private Float   lightPower;
  private boolean resizable;
  private boolean deformable;
  private boolean texturable;
  
  /**
   * Creates the controller of home furniture view with undo support.
   */
  public HomeFurnitureController(Home home, 
                                 UserPreferences preferences, 
                                 ViewFactory viewFactory, 
                                 UndoableEditSupport undoSupport) {
    this(home, preferences, viewFactory, null, undoSupport);
  }

  /**
   * Creates the controller of home furniture view with undo support.
   */
  public HomeFurnitureController(Home home, 
                                 UserPreferences preferences, 
                                 ViewFactory viewFactory,
                                 ContentManager  contentManager,
                                 UndoableEditSupport undoSupport) {
    this.home = home;
    this.preferences = preferences;
    this.viewFactory = viewFactory;
    this.contentManager = contentManager;
    this.undoSupport = undoSupport;
    this.propertyChangeSupport = new PropertyChangeSupport(this);
    
    updateProperties();
  }

  /**
   * Returns the texture controller of the piece.
   */
  public TextureChoiceController getTextureController() {
    // Create sub controller lazily only once it's needed
    if (this.textureController == null
        && this.contentManager != null) {
      this.textureController = new TextureChoiceController(
          this.preferences.getLocalizedString(HomeFurnitureController.class, "textureTitle"), 
          this.preferences, this.viewFactory, this.contentManager);
      this.textureController.addPropertyChangeListener(TextureChoiceController.Property.TEXTURE,
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              setPaint(FurniturePaint.TEXTURED);
            }
          });
    }
    return this.textureController;
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
    TextureChoiceController textureController = getTextureController();
    if (selectedFurniture.isEmpty()) {
      setName(null); // Nothing to edit
      setNameVisible(null); 
      setAngleInDegrees(null);
      setX(null);
      setY(null);
      setElevation(null);
      this.basePlanItemEditable = false;
      setWidth(null, false);
      setDepth(null, false);
      setHeight(null, false);
      setColor(null);
      if (textureController != null) {
        textureController.setTexture(null);
      }
      setPaint(null);
      setShininess(null);
      setVisible(null);
      setModelMirrored(null);
      this.lightPowerEditable = false;
      setLightPower(null);
      setResizable(true);
      setDeformable(true);
      setTexturable(true);
      setProportional(false);
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

      boolean basePlanItemEditable = !firstPiece.isDoorOrWindow();
      for (int i = 1; !basePlanItemEditable && i < selectedFurniture.size(); i++) {
        if (!selectedFurniture.get(i).isDoorOrWindow()) {
          basePlanItemEditable = true;
        }
      }
      this.basePlanItemEditable = basePlanItemEditable;

      Boolean basePlanItem = !firstPiece.isMovable();
      for (int i = 1; i < selectedFurniture.size(); i++) {
        if (basePlanItem.booleanValue() != !selectedFurniture.get(i).isMovable()) {
          basePlanItem = null;
          break;
        }
      }
      setBasePlanItem(basePlanItem);

      Float width = firstPiece.getWidth();
      for (int i = 1; i < selectedFurniture.size(); i++) {
        if (width.floatValue() != selectedFurniture.get(i).getWidth()) {
          width = null;
          break;
        }
      }
      setWidth(width, false);

      Float depth = firstPiece.getDepth();
      for (int i = 1; i < selectedFurniture.size(); i++) {
        if (depth.floatValue() != selectedFurniture.get(i).getDepth()) {
          depth = null;
          break;
        }
      }
      setDepth(depth, false);

      Float height = firstPiece.getHeight();
      for (int i = 1; i < selectedFurniture.size(); i++) {
        if (height.floatValue() != selectedFurniture.get(i).getHeight()) {
          height = null;
          break;
        }
      }
      setHeight(height, false);

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

      HomeTexture firstPieceTexture = firstPiece.getTexture();
      HomeTexture texture = firstPieceTexture;
      if (texture != null) {
        for (int i = 1; i < selectedFurniture.size(); i++) {
          if (!texture.equals(selectedFurniture.get(i).getTexture())) {
            texture = null;
            break;
          }
        }
      }
      if (textureController != null) {
        // Texture management available since version 2.3 only
        textureController.setTexture(texture);
      }

      boolean defaultColorsAndTextures = true;
      for (int i = 0; i < selectedFurniture.size(); i++) {
        HomePieceOfFurniture piece = selectedFurniture.get(i);
        if (piece.getColor() != null
            || piece.getTexture() != null) {
          defaultColorsAndTextures = false;
          break;
        }
      }

      if (color != null) {
        setPaint(FurniturePaint.COLORED);
      } else if (texture != null) {
        setPaint(FurniturePaint.TEXTURED);
      } else if (defaultColorsAndTextures) {
        setPaint(FurniturePaint.DEFAULT);
      } else {
        setPaint(null);
      }

      Float firstPieceShininess = firstPiece.getShininess();
      FurnitureShininess shininess = firstPieceShininess == null 
          ? FurnitureShininess.DEFAULT
          : (firstPieceShininess.floatValue() == 0
              ? FurnitureShininess.MATT
              : FurnitureShininess.SHINY);
      for (int i = 1; i < selectedFurniture.size(); i++) {
        HomePieceOfFurniture piece = selectedFurniture.get(i);
        if (firstPieceShininess != piece.getShininess()
            || (firstPieceShininess != null && !firstPieceShininess.equals(piece.getShininess()))) {
          shininess = null;
          break;
        }
      }
      setShininess(shininess);

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

      boolean lightPowerEditable = firstPiece instanceof HomeLight;
      for (int i = 1; lightPowerEditable && i < selectedFurniture.size(); i++) {
        if (!(selectedFurniture.get(i) instanceof HomeLight)) {
          lightPowerEditable = false;
        }
      }
      this.lightPowerEditable = lightPowerEditable;

      if (lightPowerEditable) {
        Float lightPower = ((HomeLight)firstPiece).getPower();
        for (int i = 1; i < selectedFurniture.size(); i++) {
          if (lightPower.floatValue() != ((HomeLight)selectedFurniture.get(i)).getPower()) {
            lightPower = null;
            break;
          }
        }
        setLightPower(lightPower);
      } else {
        setLightPower(null);
      }
      
      // Enable size components only if all pieces are resizable
      Boolean resizable = firstPiece.isResizable();
      for (int i = 1; i < selectedFurniture.size(); i++) {
        if (resizable.booleanValue() != selectedFurniture.get(i).isResizable()) {
          resizable = null;
          break;
        }
      }
      setResizable(resizable != null && resizable.booleanValue());

      Boolean deformable = firstPiece.isDeformable();
      for (int i = 1; i < selectedFurniture.size(); i++) {
        if (deformable.booleanValue() != selectedFurniture.get(i).isDeformable()) {
          deformable = null;
          break;
        }
      }
      setDeformable(deformable != null && deformable.booleanValue());
      if (!isDeformable()) {
        setProportional(true);
      }

      Boolean texturable = firstPiece.isTexturable();
      for (int i = 1; i < selectedFurniture.size(); i++) {
        if (texturable.booleanValue() != selectedFurniture.get(i).isTexturable()) {
          texturable = null;
          break;
        }
      }
      setTexturable(texturable == null || texturable.booleanValue());
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
   * Returns <code>true</code> if base plan item is an editable property.
   */
  public boolean isBasePlanItemEditable() {
    return this.basePlanItemEditable;
  }
  
  /**
   * Sets whether furniture is a base plan item or not.
   */
  public void setBasePlanItem(Boolean basePlanItem) {
    if (basePlanItem != this.basePlanItem) {
      Boolean oldMovable = this.basePlanItem;
      this.basePlanItem = basePlanItem;
      this.propertyChangeSupport.firePropertyChange(Property.BASE_PLAN_ITEM.name(), oldMovable, basePlanItem);
    }
  }

  /**
   * Returns whether furniture is a base plan item or not.
   */
  public Boolean getBasePlanItem() {
    return this.basePlanItem;
  }
  
  /**
   * Sets the edited width.
   */
  public void setWidth(Float width) {
    setWidth(width, isProportional());
  }

  private void setWidth(Float width, boolean updateDepthAndHeight) {
    if (width != this.width) {
      Float oldWidth = this.width;
      this.width = width;
      this.propertyChangeSupport.firePropertyChange(Property.WIDTH.name(), oldWidth, width);
      if (oldWidth != null && width != null && updateDepthAndHeight) {
        float ratio = width / oldWidth;
        if (getDepth() != null) {
          setDepth(getDepth() * ratio, false);
        }
        if (getHeight() != null) {
          setHeight(getHeight() * ratio, false);
        }
      }
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
    setDepth(depth, isProportional());
  }

  private void setDepth(Float depth, boolean updateWidthAndHeight) {
    if (depth != this.depth) {
      Float oldDepth = this.depth;
      this.depth = depth;
      this.propertyChangeSupport.firePropertyChange(Property.DEPTH.name(), oldDepth, depth);
      if (oldDepth != null && depth != null && updateWidthAndHeight) {
        float ratio = depth / oldDepth;
        if (getWidth() != null) {
          setWidth(getWidth() * ratio, false);
        }
        if (getHeight() != null) {
          setHeight(getHeight() * ratio, false);
        }
      }
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
    setHeight(height, isProportional());
  }

  public void setHeight(Float height, boolean updateWidthAndDepth) {
    if (height != this.height) {
      Float oldHeight = this.height;
      this.height = height;
      this.propertyChangeSupport.firePropertyChange(Property.HEIGHT.name(), oldHeight, height);
      if (oldHeight != null && height != null && updateWidthAndDepth) {
        float ratio = height / oldHeight;
        if (getWidth() != null) {
          setWidth(getWidth() * ratio, false);
        }
        if (getDepth() != null) {
          setDepth(getDepth() * ratio, false);
        }
      }
    }
  }

  /**
   * Returns the edited height.
   */
  public Float getHeight() {
    return this.height;
  }
  
  /**
   * Sets whether furniture proportions should be kept.
   */
  public void setProportional(boolean proportional) {
    if (proportional != this.proportional) {
      boolean oldProportional = this.proportional;
      this.proportional = proportional;
      this.propertyChangeSupport.firePropertyChange(Property.PROPORTIONAL.name(), oldProportional, proportional);
    }
  }

  /**
   * Returns whether furniture proportions should be kept or not.
   */
  public boolean isProportional() {
    return this.proportional;
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
   * Sets whether the piece is colored, textured or unknown painted.
   */
  public void setPaint(FurniturePaint paint) {
    if (paint != this.paint) {
      FurniturePaint oldPaint = this.paint;
      this.paint = paint;
      this.propertyChangeSupport.firePropertyChange(Property.PAINT.name(), oldPaint, paint);
    }
  }
  
  /**
   * Returns whether the piece is colored, textured or unknown painted.
   */
  public FurniturePaint getPaint() {
    return this.paint;
  }

  /**
   * Sets whether the piece shininess is the default one, matt, shiny or unknown.
   */
  public void setShininess(FurnitureShininess shininess) {
    if (shininess != this.shininess) {
      FurnitureShininess oldShininess = this.shininess;
      this.shininess = shininess;
      this.propertyChangeSupport.firePropertyChange(Property.SHININESS.name(), oldShininess, shininess);
    }
  }
  
  /**
   * Returns whether the piece is shininess is the default one, matt, shiny or unknown.
   */
  public FurnitureShininess getShininess() {
    return this.shininess;
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
   * Returns <code>true</code> if light power is an editable property.
   */
  public boolean isLightPowerEditable() {
    return this.lightPowerEditable;
  }
  
  /**
   * Returns the edited light power.
   */
  public Float getLightPower() {
    return this.lightPower;
  }
  
  /**
   * Sets the edited light power.
   */
  public void setLightPower(Float lightPower) {
    if (lightPower != this.lightPower) {
      Float oldLightPower = this.lightPower;
      this.lightPower = lightPower;
      this.propertyChangeSupport.firePropertyChange(Property.LIGHT_POWER.name(), oldLightPower, lightPower);
    }
  }

  /**
   * Sets whether furniture model can be resized or not.
   */
  private void setResizable(boolean resizable) {
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
   * Sets whether furniture model can be deformed or not.
   */
  private void setDeformable(boolean deformable) {
    if (deformable != this.deformable) {
      boolean oldDeformable = this.deformable;
      this.deformable = deformable;
      this.propertyChangeSupport.firePropertyChange(Property.DEFORMABLE.name(), oldDeformable, deformable);
    }
  }

  /**
   * Returns whether furniture model can be deformed or not.
   */
  public boolean isDeformable() {
    return this.deformable;
  }
  
  /**
   * Sets whether the color or the texture of the furniture model can be changed or not.
   */
  private void setTexturable(boolean texturable) {
    if (texturable != this.texturable) {
      boolean oldTexturable = this.texturable;
      this.texturable = texturable;
      this.propertyChangeSupport.firePropertyChange(Property.TEXTURABLE.name(), oldTexturable, texturable);
    }
  }

  /**
   * Returns whether the color or the texture of the furniture model can be changed or not.
   */
  public boolean isTexturable() {
    return this.texturable;
  }
  
  /**
   * Controls the modification of selected furniture in the edited home.
   */
  public void modifyFurniture() {
    List<Selectable> oldSelection = this.home.getSelectedItems(); 
    List<HomePieceOfFurniture> selectedFurniture = Home.getFurnitureSubList(oldSelection);
    if (!selectedFurniture.isEmpty()) {
      String name = getName();
      Boolean nameVisible = getNameVisible();
      Float x = getX();
      Float y = getY();
      Float elevation = getElevation();
      Float angle = getAngleInDegrees() != null
          ? (float)Math.toRadians(getAngleInDegrees())  : null;
      Boolean basePlanItem = getBasePlanItem();
      Float width = getWidth();
      Float depth = getDepth();
      Float height = getHeight();
      boolean defaultColorsAndTextures = getPaint() == FurniturePaint.DEFAULT;
      Integer color = getPaint() == FurniturePaint.COLORED 
          ? getColor() : null;
      TextureChoiceController textureController = getTextureController();
      HomeTexture texture;
      if (textureController != null && getPaint() == FurniturePaint.TEXTURED) {
        texture = textureController.getTexture();
      } else {
        texture = null;
      }
      boolean defaultShininess = getShininess() == FurnitureShininess.DEFAULT;
      Float shininess = getShininess() == FurnitureShininess.SHINY
          ? new Float(0.5f)
          : (getShininess() == FurnitureShininess.MATT
              ? new Float(0) : null);
      Boolean visible = getVisible();
      Boolean modelMirrored = getModelMirrored();
      Float lightPower = getLightPower();
      
      // Create an array of modified furniture with their current properties values
      ModifiedPieceOfFurniture [] modifiedFurniture = 
          new ModifiedPieceOfFurniture [selectedFurniture.size()]; 
      for (int i = 0; i < modifiedFurniture.length; i++) {
        HomePieceOfFurniture piece = selectedFurniture.get(i);
        if (piece instanceof HomeLight) {
          modifiedFurniture [i] = new ModifiedLight((HomeLight)piece);
        } else if (piece instanceof HomeDoorOrWindow) {
          modifiedFurniture [i] = new ModifiedDoorOrWindow((HomeDoorOrWindow)piece);
        } else if (piece instanceof HomeFurnitureGroup) {
          modifiedFurniture [i] = new ModifiedFurnitureGroup((HomeFurnitureGroup)piece);
        } else {
          modifiedFurniture [i] = new ModifiedPieceOfFurniture(piece);
        }
      }
      // Apply modification
      doModifyFurniture(modifiedFurniture, name, nameVisible, x, y, elevation, angle, basePlanItem, width, depth, height, 
          defaultColorsAndTextures, color, texture, defaultShininess, shininess, visible, modelMirrored, lightPower);
      List<Selectable> newSelection = this.home.getSelectedItems(); 
      if (this.undoSupport != null) {
        UndoableEdit undoableEdit = new FurnitureModificationUndoableEdit(
            this.home, this.preferences, oldSelection, newSelection, modifiedFurniture, 
            name, nameVisible, x, y, elevation, angle, basePlanItem, width, depth, height, 
            defaultColorsAndTextures, color, texture, defaultShininess, shininess, visible, modelMirrored, lightPower);
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
    private final List<Selectable>            newSelection;
    private final String                      name;
    private final Boolean                     nameVisible;
    private final Float                       x;
    private final Float                       y;
    private final Float                       elevation;
    private final Float                       angle;
    private final Boolean                     basePlanItem;
    private final Float                       width;
    private final Float                       depth;
    private final Float                       height;
    private final boolean                     defaultColorsAndTextures;
    private final Integer                     color;
    private final HomeTexture                 texture;
    private final boolean                     defaultShininess;
    private final Float                       shininess;
    private final Boolean                     visible;
    private final Boolean                     modelMirrored;
    private final Float                       lightPower;

    private FurnitureModificationUndoableEdit(Home home,
                                              UserPreferences preferences, 
                                              List<Selectable> oldSelection,
                                              List<Selectable> newSelection,
                                              ModifiedPieceOfFurniture [] modifiedFurniture,
                                              String name, Boolean nameVisible, 
                                              Float x, Float y, Float elevation, Float angle, Boolean basePlanItem, 
                                              Float width, Float depth, Float height,
                                              boolean defaultColorsAndTextures, Integer color, HomeTexture texture,
                                              boolean defaultShininess, Float shininess,
                                              Boolean visible,
                                              Boolean modelMirrored,
                                              Float lightPower) {
      this.home = home;
      this.preferences = preferences;
      this.oldSelection = oldSelection;
      this.newSelection = newSelection;
      this.modifiedFurniture = modifiedFurniture;
      this.name = name;
      this.nameVisible = nameVisible;
      this.x = x;
      this.y = y;
      this.elevation = elevation;
      this.angle = angle;
      this.basePlanItem = basePlanItem;
      this.width = width;
      this.depth = depth;
      this.height = height;
      this.defaultColorsAndTextures = defaultColorsAndTextures;
      this.color = color;
      this.texture = texture;
      this.defaultShininess = defaultShininess;
      this.shininess = shininess;
      this.visible = visible;
      this.modelMirrored = modelMirrored;
      this.lightPower = lightPower;
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
          this.name, this.nameVisible, this.x, this.y, this.elevation, 
          this.angle, this.basePlanItem, this.width, this.depth, this.height, 
          this.defaultColorsAndTextures, this.color, this.texture, 
          this.defaultShininess, this.shininess,
          this.visible, this.modelMirrored, this.lightPower); 
      home.setSelectedItems(this.newSelection); 
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
                                        Float x, Float y, Float elevation, Float angle, Boolean basePlanItem, 
                                        Float width, Float depth, Float height, 
                                        boolean defaultColorsAndTextures, Integer color, HomeTexture texture,
                                        boolean defaultShininess, Float shininess,
                                        Boolean visible, Boolean modelMirrored, 
                                        Float lightPower) {
    for (ModifiedPieceOfFurniture modifiedPiece : modifiedFurniture) {
      HomePieceOfFurniture piece = modifiedPiece.getPieceOfFurniture();
      if (name != null) {
        piece.setName(name);
      }
      if (nameVisible != null) {
        piece.setNameVisible(nameVisible);
      }
      if (x != null) {
        piece.setX(x);
      }
      if (y != null) {
        piece.setY(y);
      }
      if (elevation != null) {
        piece.setElevation(elevation); 
      }
      if (angle != null) {
        piece.setAngle(angle);
      }
      if (basePlanItem != null && !piece.isDoorOrWindow()) {
        piece.setMovable(!basePlanItem);
      }
      if (piece.isResizable()) {
        if (width != null) {
          piece.setWidth(width);
        }
        if (depth != null) {
          piece.setDepth(depth);
        }
        if (height != null) {
          piece.setHeight(height);
        }
        if (modelMirrored != null) {
          piece.setModelMirrored(modelMirrored);
        }
      }
      if (piece.isTexturable()) {
        if (defaultColorsAndTextures) {
          piece.setColor(null);
          piece.setTexture(null);
        } else if (texture != null) {
          piece.setColor(null);
          piece.setTexture(texture);
        } else if (color != null) {
          piece.setTexture(null);
          piece.setColor(color);
        }
        if (defaultShininess) {
          piece.setShininess(null);
        } else if (shininess != null) {
          piece.setShininess(shininess);
        }
      }
      if (visible != null) {
        piece.setVisible(visible);
      }
      if (lightPower != null) {
        ((HomeLight)piece).setPower(lightPower);
      }
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
    private final String               name;
    private final boolean              nameVisible;
    private final float                x;
    private final float                y;
    private final float                elevation;
    private final float                angle;
    private final boolean              movable;
    private final float                width;
    private final float                depth;
    private final float                height;
    private final Integer              color;
    private final HomeTexture          texture;
    private final Float                shininess;
    private final boolean              visible;
    private final boolean              modelMirrored;

    public ModifiedPieceOfFurniture(HomePieceOfFurniture piece) {
      this.piece = piece;
      this.name = piece.getName();
      this.nameVisible = piece.isNameVisible();
      this.x = piece.getX();
      this.y = piece.getY();
      this.elevation = piece.getElevation();
      this.angle = piece.getAngle();
      this.movable = piece.isMovable();
      this.width = piece.getWidth();
      this.depth = piece.getDepth();
      this.height = piece.getHeight();
      this.color = piece.getColor();
      this.texture = piece.getTexture();
      this.shininess = piece.getShininess();
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
      this.piece.setMovable(this.movable);
      if (this.piece.isResizable()) {
        this.piece.setWidth(this.width);
        this.piece.setDepth(this.depth);
        this.piece.setHeight(this.height);
        this.piece.setModelMirrored(this.modelMirrored);
      }
      if (this.piece.isTexturable()) {
        this.piece.setColor(this.color);
        this.piece.setTexture(this.texture);
        this.piece.setShininess(this.shininess);
      }
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

  /**
   * Stores the current properties values of a modified light.
   */
  private static class ModifiedLight extends ModifiedPieceOfFurniture {
    private final float power;
    
    public ModifiedLight(HomeLight light) {
      super(light);
      this.power = light.getPower();
    }

    public void reset() {
      super.reset();
      ((HomeLight)getPieceOfFurniture()).setPower(this.power);
    }
  }

  /**
   * Stores the current properties values of a modified group.
   */
  private static class ModifiedFurnitureGroup extends ModifiedPieceOfFurniture {
    private final float [] groupFurnitureX;
    private final float [] groupFurnitureY;
    private final float [] groupFurnitureWidth;
    private final float [] groupFurnitureDepth;
    
    public ModifiedFurnitureGroup(HomeFurnitureGroup group) {
      super(group);
      List<HomePieceOfFurniture> groupFurniture = getGroupFurniture((HomeFurnitureGroup)group);
      this.groupFurnitureX = new float [groupFurniture.size()];
      this.groupFurnitureY = new float [groupFurniture.size()];
      this.groupFurnitureWidth = new float [groupFurniture.size()];
      this.groupFurnitureDepth = new float [groupFurniture.size()];
      for (int i = 0; i < groupFurniture.size(); i++) {
        HomePieceOfFurniture groupPiece = groupFurniture.get(i);
        this.groupFurnitureX [i] = groupPiece.getX();
        this.groupFurnitureY [i] = groupPiece.getY();
        this.groupFurnitureWidth [i] = groupPiece.getWidth();
        this.groupFurnitureDepth [i] = groupPiece.getDepth();
      }
    }

    public void reset() {
      super.reset();
      HomeFurnitureGroup group = (HomeFurnitureGroup)getPieceOfFurniture();
      List<HomePieceOfFurniture> groupFurniture = getGroupFurniture(group);
      for (int i = 0; i < groupFurniture.size(); i++) {
        HomePieceOfFurniture groupPiece = groupFurniture.get(i);
        if (group.isResizable()) {
          // Restore group furniture location and size because resizing a group isn't reversible 
          groupPiece.setX(this.groupFurnitureX [i]);
          groupPiece.setY(this.groupFurnitureY [i]);
          groupPiece.setWidth(this.groupFurnitureWidth [i]);
          groupPiece.setDepth(this.groupFurnitureDepth [i]);
        }
      }
    }
    
    /**
     * Returns all the children of the given <code>furnitureGroup</code>.  
     */
    private List<HomePieceOfFurniture> getGroupFurniture(HomeFurnitureGroup furnitureGroup) {
      List<HomePieceOfFurniture> pieces = new ArrayList<HomePieceOfFurniture>();
      for (HomePieceOfFurniture piece : furnitureGroup.getFurniture()) {
        pieces.add(piece);
        if (piece instanceof HomeFurnitureGroup) {
          pieces.addAll(getGroupFurniture((HomeFurnitureGroup)piece));
        } 
      }
      return pieces;
    }
  }
}
