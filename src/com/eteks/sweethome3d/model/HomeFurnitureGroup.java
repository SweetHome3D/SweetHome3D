/*
 * HomeFurnitureGroup.java 4 févr. 2010
 *
 * Copyright (c) 2010 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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
package com.eteks.sweethome3d.model;

import java.awt.geom.Rectangle2D;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A group of furniture of furniture.
 * @since 2.3
 * @author Emmanuel Puybaret
 */
public class HomeFurnitureGroup extends HomePieceOfFurniture {
  private static final long serialVersionUID = 1L;
  
  private static final float [][] IDENTITY = new float [][] {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};

  private List<HomePieceOfFurniture> furniture;
  private boolean                    resizable;
  private boolean                    movable;
  private boolean                    doorOrWindow;
  private float                      fixedWidth;
  private float                      fixedDepth;
  private float                      fixedHeight;
  private BigDecimal                 price;
  private BigDecimal                 valueAddedTaxPercentage;
  private BigDecimal                 valueAddedTax;
  private BigDecimal                 priceValueAddedTaxIncluded;
  private List<Integer>              furnitureDefaultColors;
  private List<HomeTexture>          furnitureDefaultTextures;

  /**
   * Creates a group from the given <code>furniture</code> list. 
   */
  public HomeFurnitureGroup(List<HomePieceOfFurniture> furniture,
                            String name) {
    super(furniture.get(furniture.size() - 1));
    this.furniture = Collections.unmodifiableList(furniture);    
    Rectangle2D boundingRectangle = null;
    for (HomePieceOfFurniture piece : getFurnitureWithoutGroups(furniture)) {
      for (float [] point : piece.getPoints()) {
        if (boundingRectangle == null) {
          boundingRectangle = new Rectangle2D.Float(point [0], point [1], 0, 0);
        } else {
          boundingRectangle.add(point [0], point [1]);
        }
      }
    }
    float elevation = Float.MAX_VALUE;
    float height    = 0;
    this.resizable = true;
    this.movable = true;
    this.doorOrWindow = true;
    boolean visible = false;
    boolean modelMirrored = true;
    this.valueAddedTaxPercentage = furniture.get(0).getValueAddedTaxPercentage();
    for (HomePieceOfFurniture piece : furniture) {
      elevation = Math.min(elevation, piece.getElevation());      
      height = Math.max(height, piece.getElevation() + piece.getHeight());
      this.resizable &= piece.isResizable();
      this.movable &= piece.isMovable();
      this.doorOrWindow &= piece.isDoorOrWindow();
      visible |= piece.isVisible();
      modelMirrored &= piece.isModelMirrored();
      
      // Add price and VAT
      if (piece.getPrice() != null) {
        if (this.price == null) {
          this.price = piece.getPrice();
          this.priceValueAddedTaxIncluded = piece.getPriceValueAddedTaxIncluded();
        } else {
          this.price = this.price.add(piece.getPrice()); 
          this.priceValueAddedTaxIncluded = this.priceValueAddedTaxIncluded.add(piece.getPriceValueAddedTaxIncluded());
        }
      }
      if (piece.getValueAddedTax() != null) {
        if (this.valueAddedTax == null) {
          this.valueAddedTax = piece.getValueAddedTax();
        } else {
          this.valueAddedTax = this.valueAddedTax.add(piece.getValueAddedTax());
        }
      }
      if (this.valueAddedTaxPercentage != null) {
        if (piece.getValueAddedTaxPercentage() == null
            || !piece.getValueAddedTaxPercentage().equals(this.valueAddedTaxPercentage)) {
          this.valueAddedTaxPercentage = null; 
        }
      }
    }
    if (this.resizable) {
      super.setWidth((float)boundingRectangle.getWidth());
      super.setDepth((float)boundingRectangle.getHeight());
      super.setHeight(height - elevation);
    } else {
      this.fixedWidth = (float)boundingRectangle.getWidth();
      this.fixedDepth = (float)boundingRectangle.getHeight();
      this.fixedHeight = height - elevation;
    }
    setName(name);
    setNameVisible(false);
    setNameXOffset(0);
    setNameYOffset(0);
    setNameStyle(null);
    setDescription(null);
    setVisible(visible);
    super.setColor(null);
    super.setTexture(null);
    super.setX((float)boundingRectangle.getCenterX());
    super.setY((float)boundingRectangle.getCenterY());
    super.setElevation(elevation);
    super.setAngle(0);
    super.setModelMirrored(modelMirrored);
  }

  /**
   * Returns all the pieces of the given <code>furniture</code> list.  
   */
  private List<HomePieceOfFurniture> getFurnitureWithoutGroups(List<HomePieceOfFurniture> furniture) {
    List<HomePieceOfFurniture> pieces = new ArrayList<HomePieceOfFurniture>();
    for (HomePieceOfFurniture piece : furniture) {
      if (piece instanceof HomeFurnitureGroup) {
        pieces.addAll(getFurnitureWithoutGroups(((HomeFurnitureGroup)piece).getFurniture()));
      } else {
        pieces.add(piece);
      }
    }
    return pieces;
  }
  
  /**
   * Returns an unmodifiable list of the furniture of this group.
   */
  public List<HomePieceOfFurniture> getFurniture() {
    return this.furniture;
  }
  
  /**
   * Returns <code>null</code>.
   */
  @Override
  public String getCatalogId() {
    return null;
  }

  /**
   * Returns <code>true</code> if all furniture of this group are movable.
   */
  @Override
  public boolean isMovable() {
    return this.movable;
  }

  /**
   * Returns <code>true</code> if all furniture of this group are doors or windows.
   */
  @Override
  public boolean isDoorOrWindow() {
    return this.doorOrWindow;
  }

  /**
   * Returns <code>true</code> if all furniture of this group are resizable.
   */
  @Override
  public boolean isResizable() {
    return this.resizable;
  }
  
  /**
   * Returns the width of this group.
   */
  @Override
  public float getWidth() {
    if (!this.resizable) {
      return this.fixedWidth;
    } else {
      return super.getWidth();
    }
  }
  
  /**
   * Returns the depth of this group.
   */
  @Override
  public float getDepth() {
    if (!this.resizable) {
      return this.fixedDepth;
    } else {
      return super.getDepth();
    }
  }
  
  /**
   * Returns the height of this group.
   */
  @Override
  public float getHeight() {
    if (!this.resizable) {
      return this.fixedHeight;
    } else {
      return super.getHeight();
    }
  }
  
  /**
   * Returns <code>null</code>.
   */
  public Content getIcon() {
    return null;
  }

  /**
   * Returns <code>null</code>.
   */
  public Content getPlanIcon() {
    return null;
  }

  /**
   * Returns <code>null</code>.
   */
  public Content getModel() {
    return null;
  }
  
  /**
   * Returns an identity matrix.
   */
  @Override
  public float [][] getModelRotation() {
    return IDENTITY;
  }
  
  /**
   * Returns the price of the furniture of this group with a price.
   */
  @Override
  public BigDecimal getPrice() {
    return this.price;
  }
  
  /**
   * Returns the VAT percentage of the furniture of this group 
   * or <code>null</code> if one piece has no VAT percentage 
   * or has a VAT percentage different from the other furniture.
   */
  @Override
  public BigDecimal getValueAddedTaxPercentage() {
    return this.priceValueAddedTaxIncluded;
  }
  
  /**
   * Returns the VAT of the furniture of this group.
   */
  @Override
  public BigDecimal getValueAddedTax() {
    return this.valueAddedTax;
  }
  
  /**
   * Returns the total price of the furniture of this group.
   */
  @Override
  public BigDecimal getPriceValueAddedTaxIncluded() {
    return this.priceValueAddedTaxIncluded;
  }
  
  /**
   * Returns <code>false</code>.
   */
  @Override
  public boolean isBackFaceShown() {
    return false;
  }
  
  /**
   * Sets the <code>color</code> of the furniture of this group.
   */
  @Override
  public void setColor(Integer color) {
    super.setColor(color);
    if (color != null) {      
      storeDefaultColorsAndTextures();
      for (HomePieceOfFurniture piece : this.furniture) {
        piece.setTexture(null);
        piece.setColor(color);
      } 
    } else {
      restoreDefaultColorsAndTextures();
    }
  }

  /**
   * Sets the <code>texture</code> of the furniture of this group.
   */
  @Override
  public void setTexture(HomeTexture texture) {
    super.setTexture(texture);
    if (texture != null) {      
      storeDefaultColorsAndTextures();
      for (HomePieceOfFurniture piece : this.furniture) {
        piece.setColor(null);
        piece.setTexture(texture);
      } 
    } else {
      restoreDefaultColorsAndTextures();
    }
  }
  
  /**
   * Stores default colors and textures. 
   */
  private void storeDefaultColorsAndTextures() {
    if (this.furnitureDefaultColors == null) {
      // Retrieve default color and texture of child furniture
      Integer [] furnitureDefaultColors = new Integer [this.furniture.size()];
      HomeTexture [] furnitureDefaultTextures = new HomeTexture [this.furniture.size()];
      for (int i = 0; i < this.furniture.size(); i++) {
        furnitureDefaultColors [i] = this.furniture.get(i).getColor();
        furnitureDefaultTextures [i] = this.furniture.get(i).getTexture();
      } 
      this.furnitureDefaultColors = Arrays.asList(furnitureDefaultColors);
      this.furnitureDefaultTextures = Arrays.asList(furnitureDefaultTextures);
    }
  }
  
  /**
   * Restores default colors and textures
   */
  private void restoreDefaultColorsAndTextures() {
    if (this.furnitureDefaultColors != null) {
      for (int i = 0; i < this.furniture.size(); i++) {
        this.furniture.get(i).setColor(this.furnitureDefaultColors.get(i));
        this.furniture.get(i).setTexture(this.furnitureDefaultTextures.get(i));
      }
      this.furnitureDefaultColors = null;
      this.furnitureDefaultTextures = null;
    }
  }

  /**
   * Sets the <code>angle</code> of the furniture of this group.
   */
  @Override
  public void setAngle(float angle) {
    if (angle != getAngle()) {
      float angleDelta = angle - getAngle();
      super.setAngle(angle);
      double cosAngleDelta = Math.cos(angleDelta);
      double sinAngleDelta = Math.sin(angleDelta);
      for (HomePieceOfFurniture piece : this.furniture) {
        piece.setAngle(piece.getAngle() + angleDelta);     
        float newX = getX() + (float)((piece.getX() - getX()) * cosAngleDelta - (piece.getY() - getY()) * sinAngleDelta);
        float newY = getY() + (float)((piece.getX() - getX()) * sinAngleDelta + (piece.getY() - getY()) * cosAngleDelta);
        piece.setX(newX);
        piece.setY(newY);
      }
    }
  }
  
  /**
   * Sets the <code>abscissa</code> of this group and moves its furniture accordingly.
   */
  @Override
  public void setX(float x) {
    if (x != getX()) {
      float dx = x - getX();
      super.setX(x);
      for (HomePieceOfFurniture piece : this.furniture) {
        piece.setX(piece.getX() + dx);
      }
    }
  }

  /**
   * Sets the <code>ordinate</code> of this group and moves its furniture accordingly.
   */
  @Override
  public void setY(float y) {
    if (y != getY()) {
      float dy = y - getY();
      super.setY(y);
      for (HomePieceOfFurniture piece : this.furniture) {
        piece.setY(piece.getY() + dy);
      }
    }
  }

  /**
   * Sets the <code>width</code> of this group, then moves and resizes its furniture accordingly.
   */
  @Override
  public void setWidth(float width) {
    if (width != getWidth()) {
      float widthFactor = width / getWidth();
      super.setWidth(width);
      float angle = getAngle();
      for (HomePieceOfFurniture piece : this.furniture) {
        float angleDelta = piece.getAngle() - angle;
        float pieceWidth = piece.getWidth();
        float pieceDepth = piece.getDepth();
        piece.setWidth(pieceWidth + pieceWidth * (widthFactor - 1) * Math.abs((float)Math.cos(angleDelta)));
        piece.setDepth(pieceDepth + pieceDepth * (widthFactor - 1) * Math.abs((float)Math.sin(angleDelta)));
        // Rotate piece to angle 0
        double cosAngle = Math.cos(angle);
        double sinAngle = Math.sin(angle);
        float newX = getX() + (float)((piece.getX() - getX()) * cosAngle + (piece.getY() - getY()) * sinAngle);
        float newY = getY() + (float)((piece.getX() - getX()) * -sinAngle + (piece.getY() - getY()) * cosAngle);
        // Update its abscissa
        newX = getX() + (newX - getX()) * widthFactor; 
        // Rotate piece back to its angle
        piece.setX(getX() + (float)((newX - getX()) * cosAngle - (newY - getY()) * sinAngle));
        piece.setY(getY() + (float)((newX - getX()) * sinAngle + (newY - getY()) * cosAngle));
      }
    }
  }

  /**
   * Sets the <code>depth</code> of this group, then moves and resizes its furniture accordingly.
   */
  @Override
  public void setDepth(float depth) {
    if (depth != getDepth()) {
      float depthFactor = depth / getDepth();
      super.setDepth(depth);
      float angle = getAngle();
      for (HomePieceOfFurniture piece : this.furniture) {
        float angleDelta = piece.getAngle() - angle;
        float pieceWidth = piece.getWidth();
        float pieceDepth = piece.getDepth();
        piece.setWidth(pieceWidth + pieceWidth * (depthFactor - 1) * Math.abs((float)Math.sin(angleDelta)));
        piece.setDepth(pieceDepth + pieceDepth * (depthFactor - 1) * Math.abs((float)Math.cos(angleDelta)));
        // Rotate piece to angle 0
        double cosAngle = Math.cos(angle);
        double sinAngle = Math.sin(angle);
        float newX = getX() + (float)((piece.getX() - getX()) * cosAngle + (piece.getY() - getY()) * sinAngle);
        float newY = getY() + (float)((piece.getX() - getX()) * -sinAngle + (piece.getY() - getY()) * cosAngle);
        // Update its ordinate
        newY = getY() + (newY - getY()) * depthFactor;
        // Rotate piece back to its angle
        piece.setX(getX() + (float)((newX - getX()) * cosAngle - (newY - getY()) * sinAngle));
        piece.setY(getY() + (float)((newX - getX()) * sinAngle + (newY - getY()) * cosAngle));
      }
    }
  }

  /**
   * Sets the <code>height</code> of this group, then moves and resizes its furniture accordingly.
   */
  @Override
  public void setHeight(float height) {
    if (height != getHeight()) {
      float heightFactor = height / getHeight();
      super.setHeight(height);
      for (HomePieceOfFurniture piece : this.furniture) {
        piece.setHeight(piece.getHeight() * heightFactor);
        piece.setElevation(getElevation() 
            + (piece.getElevation() - getElevation()) * heightFactor);
      }
    }
  }

  /**
   * Sets the <code>elevation</code> of this group, then moves its furniture accordingly.
   */
  @Override
  public void setElevation(float elevation) {
    if (elevation != getElevation()) {
      float elevationDelta = elevation - getElevation();
      super.setElevation(elevation);
      for (HomePieceOfFurniture piece : this.furniture) {
        piece.setElevation(piece.getElevation() + elevationDelta);
      }
    }
  }
  
  /**
   * Sets whether the furniture of this group should be mirrored or not.
   */
  @Override
  public void setModelMirrored(boolean modelMirrored) {
    if (modelMirrored != isModelMirrored()) {
      super.setModelMirrored(modelMirrored);
      float angle = getAngle();
      for (HomePieceOfFurniture piece : this.furniture) {
        piece.setModelMirrored(!piece.isModelMirrored());
        // Rotate piece to angle 0
        double cosAngle = Math.cos(angle);
        double sinAngle = Math.sin(angle);
        float newX = getX() + (float)((piece.getX() - getX()) * cosAngle + (piece.getY() - getY()) * sinAngle);
        float newY = getY() + (float)((piece.getX() - getX()) * -sinAngle + (piece.getY() - getY()) * cosAngle);
        // Update its abscissa
        newX = getX() - (newX - getX()); 
        // Rotate piece back to its angle
        piece.setX(getX() + (float)((newX - getX()) * cosAngle - (newY - getY()) * sinAngle));
        piece.setY(getY() + (float)((newX - getX()) * sinAngle + (newY - getY()) * cosAngle));
      }
    }
  }
  
  /**
   * Sets whether the furniture of this group should be visible or not.
   */
  @Override
  public void setVisible(boolean visible) {
    if (visible != isVisible()) {
      super.setVisible(visible);
      for (HomePieceOfFurniture piece : this.furniture) {
        piece.setVisible(visible);
      }
    }
  }

  /**
   * Returns a clone of this group with cloned furniture.
   */
  @Override
  public HomeFurnitureGroup clone() {
    HomeFurnitureGroup clone = (HomeFurnitureGroup)super.clone();
    // Deep clone furniture managed by this group
    clone.furniture = new ArrayList<HomePieceOfFurniture>(this.furniture.size());
    for (HomePieceOfFurniture piece : this.furniture) {
      clone.furniture.add(piece.clone());
    }
    clone.furniture = Collections.unmodifiableList(clone.furniture);
    if (this.furnitureDefaultColors != null)  {
      clone.furnitureDefaultColors = new ArrayList<Integer>(this.furnitureDefaultColors);
    }
    if (this.furnitureDefaultTextures != null) {
      clone.furnitureDefaultTextures = new ArrayList<HomeTexture>(this.furnitureDefaultTextures);
    }
    return clone;
  }
}

