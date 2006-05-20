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
  private String  name;
  private Content icon;
  private Content model;
  private float   width;
  private float   depth;
  private float   height;
  private boolean movable;
  private boolean doorOrWindow;
  private static final Collator COMPARATOR = Collator.getInstance();

  /**
   * Creates a catalog piece of furniture with all its values.
   * @param name  the name of the new piece
   * @param icon an URL to the icon file of the new piece
   * @param model an URL to the 3D model file of the new piece
   * @param width  the width in meters of the new piece
   * @param depth  the depth in meters of the new piece
   * @param height  the height in meters of the new piece
   * @param movable if true, the new piece is movable
   * @param doorOrWindow if true, the new piece is a door or a window
   */
  public CatalogPieceOfFurniture(String name, Content icon, Content model, 
                          float width, float depth, float height, boolean movable, boolean doorOrWindow) {
    this.name = name;
    this.icon = icon;
    this.model = model;
    this.width = width;
    this.depth = depth;
    this.height = height;
    this.movable = movable;
    this.doorOrWindow = doorOrWindow;
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
