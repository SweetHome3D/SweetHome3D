/*
 * PieceOfFurniture.java 15 mai 2006
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
package com.eteks.sweethome3d.model;

import java.io.Serializable;

/**
 * A piece of furniture.
 * @author Emmanuel Puybaret
 */
public interface PieceOfFurniture extends Serializable {
  /**
   * Returns the name of this piece of furniture.
   */
  public abstract String getName();

  /**
   * Returns the depth of this piece of furniture.
   */
  public abstract float getDepth();

  /**
   * Returns the height of this piece of furniture.
   */
  public abstract float getHeight();

  /**
   * Returns the width of this piece of furniture.
   */
  public abstract float getWidth();

  /**
   * Returns the elevation of this piece of furniture.
   */
  public abstract float getElevation();

  /**
   * Returns <code>true</code> if this piece of furniture is movable.
   */
  public abstract boolean isMovable();

  /**
   * Returns <code>true</code> if this piece of furniture is a door or a window.
   */
  public abstract boolean isDoorOrWindow();

  /**
   * Returns the icon of this piece of furniture.
   */
  public abstract Content getIcon();

  /**
   * Returns the 3D model of this piece of furniture.
   */
  public abstract Content getModel();
  
  /**
   * Returns the rotation 3 by 3 matrix of this piece of furniture that ensures 
   * its model is correctly oriented.
   */
  public float [][] getModelRotation();
  
  /**
   * Returns <code>true</code> if the back face of the piece of furniture
   * model should be displayed.
   */
  public abstract boolean isBackFaceShown();
  
  /**
   * Returns the color of this piece of furniture.
   */
  public abstract Integer getColor();
}