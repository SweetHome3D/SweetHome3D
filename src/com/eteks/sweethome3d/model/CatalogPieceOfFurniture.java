/*
 * CatalogPieceOfFurniture.java 7 avr. 2006
 * 
 * Copyright (c) 2006 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights
 * Reserved.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.eteks.sweethome3d.model;

import java.text.Collator;

/**
 * A catalog piece of furniture.
 * @author Emmanuel Puybaret
 */
public class CatalogPieceOfFurniture implements Comparable<CatalogPieceOfFurniture>, PieceOfFurniture {
  private static final long serialVersionUID = 1L;

  private String     name;
  private Content    icon;
  private Content    model;
  private float      width;
  private float      depth;
  private float      height;
  private boolean    proportional;
  private float      elevation;
  private boolean    movable;
  private boolean    doorOrWindow;
  private float [][] modelRotation;
  private boolean    backFaceShown;
  private Integer    color;
  private float      iconYaw;
  private boolean    modifiable;

  private Category  category;

  private static final Collator COMPARATOR = Collator.getInstance();

  /**
   * Creates a catalog piece of furniture.
   * @param name  the name of the new piece
   * @param icon an URL to the icon file of the new piece
   * @param model an URL to the 3D model file of the new piece
   * @param width  the width in centimeters of the new piece
   * @param depth  the depth in centimeters of the new piece
   * @param height  the height in centimeters of the new piece
   * @param movable if <code>true</code>, the new piece is movable
   * @param doorOrWindow if <code>true</code>, the new piece is a door or a window
   */
  public CatalogPieceOfFurniture(String name, Content icon, Content model, 
                                 float width, float depth, float height, 
                                 boolean movable, boolean doorOrWindow) {
    this(name, icon, model, width, depth, height, 0, movable, doorOrWindow);
  }

  /**
   * Creates an unmodifiable catalog piece of furniture of the default catalog.
   * @param name  the name of the new piece
   * @param icon an URL to the icon file of the new piece
   * @param model an URL to the 3D model file of the new piece
   * @param width  the width in centimeters of the new piece
   * @param depth  the depth in centimeters of the new piece
   * @param height  the height in centimeters of the new piece
   * @param elevation  the elevation in centimeters of the new piece
   * @param movable if <code>true</code>, the new piece is movable
   * @param doorOrWindow if <code>true</code>, the new piece is a door or a window
   */
  public CatalogPieceOfFurniture(String name, Content icon, Content model, 
                                 float width, float depth, float height, float elevation, 
                                 boolean movable, boolean doorOrWindow) {
    this(name, icon, model, width, depth, height, elevation, movable, doorOrWindow, null, 
        new float [][] {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}}, false, (float)Math.PI / 8, true, false);
  }
         
  /**
   * Creates a modifiable catalog piece of furniture with all its values.
   * @param name  the name of the new piece
   * @param icon an URL to the icon file of the new piece
   * @param model an URL to the 3D model file of the new piece
   * @param width  the width in centimeters of the new piece
   * @param depth  the depth in centimeters of the new piece
   * @param height  the height in centimeters of the new piece
   * @param elevation  the elevation in centimeters of the new piece
   * @param movable if <code>true</code>, the new piece is movable
   * @param doorOrWindow if <code>true</code>, the new piece is a door or a window
   * @param color the color of the piece as RGB code or <code>null</code> if piece color is unchanged
   * @param modelRotation the rotation 3 by 3 matrix applied to the piece model
   * @param backFaceShown <code>true</code> if back face should be shown
   * @param iconYaw the yaw angle used to create the piece icon
   * @param proportional if <code>true</code>, size proportions will be kept 
   */
  public CatalogPieceOfFurniture(String name, Content icon, Content model, 
                                 float width, float depth, float height, float elevation, 
                                 boolean movable, boolean doorOrWindow, Integer color,
                                 float [][] modelRotation, boolean backFaceShown,
                                 float iconYaw, boolean proportional) {
    this(name, icon, model, width, depth, height, elevation, movable, doorOrWindow, 
        color, modelRotation, backFaceShown, iconYaw, proportional, true);
  }
  
  private CatalogPieceOfFurniture(String name, Content icon, Content model, 
                                  float width, float depth, float height, float elevation, 
                                  boolean movable, boolean doorOrWindow, Integer color,
                                  float [][] modelRotation, boolean backFaceShown,
                                  float iconYaw, boolean proportional, boolean modifiable) {
    this.name = name;
    this.icon = icon;
    this.model = model;
    this.width = width;
    this.depth = depth;
    this.height = height;
    this.elevation = elevation;
    this.movable = movable;
    this.doorOrWindow = doorOrWindow;
    this.modelRotation = deepCopy(modelRotation);
    this.backFaceShown = backFaceShown;
    this.color = color;
    this.iconYaw = iconYaw;
    this.proportional = proportional;
    this.modifiable = modifiable;
  }

  /**
   * Returns the name of this piece of furniture.
   */
   public String getName() {
    return this.name;
  }

  /**
   * Returns the depth of this piece of furniture.
   */
  public float getDepth() {
    return this.depth;
  }

  /**
   * Returns the height of this piece of furniture.
   */
  public float getHeight() {
    return this.height;
  }

  /**
   * Returns the width of this piece of furniture.
   */
  public float getWidth() {
    return this.width;
  }

  /**
   * Returns the elevation of this piece of furniture.
   */
  public float getElevation() {
    return this.elevation;
  }

  /**
   * Returns <code>true</code> if this piece of furniture is movable.
   */
  public boolean isMovable() {
    return this.movable;
  }

  /**
   * Returns <code>true</code> if this piece of furniture is a door or a window.
   */
  public boolean isDoorOrWindow() {
    return this.doorOrWindow;
  }

  /**
   * Returns the icon of this piece of furniture.
   */
  public Content getIcon() {
    return this.icon;
  }

  /**
   * Returns the 3D model of this piece of furniture.
   */
  public Content getModel() {
    return this.model;
  }

  /**
   * Returns the rotation 3 by 3 matrix of this piece of furniture that ensures 
   * its model is correctly oriented.
   */
  public float [][] getModelRotation() {
    // Return a deep copy to avoid any misuse of piece data
    return deepCopy(this.modelRotation);
  }

  private float [][] deepCopy(float [][] modelRotation) {
    return new float [][] {{modelRotation [0][0], modelRotation [0][1], modelRotation [0][2]},
                           {modelRotation [1][0], modelRotation [1][1], modelRotation [1][2]},
                           {modelRotation [2][0], modelRotation [2][1], modelRotation [2][2]}};
  }

  /**
   * Returns <code>true</code> if the back face of the piece of furniture
   * model should be displayed.
   */
  public boolean isBackFaceShown() {
    return this.backFaceShown;
  }
  
  /**
   * Returns the color of this piece of furniture.
   */
  public Integer getColor() {
    return this.color;
  }
  
  /**
   * Returns the yaw angle used to create the piece icon.
   */
  public float getIconYaw() {
    return this.iconYaw;
  }
  
  /**
   * Returns <code>true</code> if size proportions should be kept.
   */
  public boolean isProportional() {
    return this.proportional;
  }
  
  /**
   * Returns <code>true</code> if this piece is modifiable (not read from resources).
   */
  public boolean isModifiable() {
    return this.modifiable;
  }
  
  /**
   * Returns the category of this piece of furniture.
   */
  public Category getCategory() {
    return this.category;
  }
  
  /**
   * Sets the category of this piece of furniture.
   */
  void setCategory(Category category) {
    this.category = category;
  }
  
  /** 
   * Returns true if this piece and the one in parameter have the same name.
   */
  @Override
  public boolean equals(Object obj) {
    return obj instanceof CatalogPieceOfFurniture
           && COMPARATOR.equals(this.name, ((CatalogPieceOfFurniture)obj).name);
  }

  /** 
   * Returns a hash code computed from the name of this piece.
   */
   @Override
  public int hashCode() {
    return this.name.hashCode();
  }

  /** 
   * Compares the names of this piece and the one in parameter.
   */
  public int compareTo(CatalogPieceOfFurniture piece) {
    return COMPARATOR.compare(this.name, piece.name);
  }
}
