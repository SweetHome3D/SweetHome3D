/*
 * FurnitureEvent.java 21 mai 2006
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

import java.util.EventObject;

/**
 * Type of event notified when {@link Home home} furniture is added, updated or deleted.
 * @author Emmanuel Puybaret
 */
public class FurnitureEvent extends EventObject {
  public enum Type {ADD, DELETE}

  private PieceOfFurniture piece;
  private int index;
  private Type type;

  /**
   * Creates an event with <code>home</code> as source, and an associated <code>piece</code>.
   * @param index 
   * @param eventType 
   */
  public FurnitureEvent(Object source, PieceOfFurniture piece, int index, Type type) {
    super(source);
    this.piece = piece;
    this.index = index;
    this.type =  type;
  }

  /**
   * Returns the piece of furniture added or deleted.
   */
  public PieceOfFurniture getPieceOfFurniture() {
    return this.piece;
  }

  /**
   * Returns the index of the piece of furniture in home.
   */
  public int getIndex() {
    return this.index;
  }

  /**
   * Returns the type of event. 
   */
  public Type getType() {
    return this.type;
  }
}
