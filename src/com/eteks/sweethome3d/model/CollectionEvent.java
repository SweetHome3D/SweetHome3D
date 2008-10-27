/*
 * CollectionEvent.java 27 oct. 2008
 *
 * Copyright (c) 2008 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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
 * Type of event notified when an item is added or deleted from a list.
 * <code>T</code> is the type of item stored in the collection.
 * @author Emmanuel Puybaret
 */
public class CollectionEvent<T> extends EventObject {
  /**
   * The type of change in the collection.
   */
  public enum Type {ADD, DELETE}

  private T    item;
  private int  index;
  private Type type;

  /**
   * Creates an event for an item that has no index. 
   */
  public CollectionEvent(Object source, T item, Type type) {
    this(source, item, -1, type);
  }

  /**
   * Creates an event for an item with its index. 
   */
  public CollectionEvent(Object source, T item, int index, Type type) {
    super(source);
    this.item = item;
    this.index = index;
    this.type =  type;
  }
  
  /**
   * Returns the added or deleted item.
   */
  public T getItem() {
    return this.item;
  }

  /**
   * Returns the index of the item in collection or -1 if this index is unknown.
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
