/*
 * DimensionLineEvent.java 17 sept 2007
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
package com.eteks.sweethome3d.model;

import java.util.EventObject;

/**
 * Type of event notified when {@link Home home} dimension lines are modified.
 * @author Emmanuel Puybaret
 */
public class DimensionLineEvent extends EventObject {
  public enum Type {ADD, DELETE, UPDATE}

  private DimensionLine dimensionLine;
  private Type type;
  
  /**
   * Creates an event emitted by <code>source</code> for a notification
   * of a given <code>{@link Type type}</code> about a given dimension line. 
   */
  public DimensionLineEvent(Object source, DimensionLine dimensionLine, Type type) {
    super(source);
    this.dimensionLine = dimensionLine;
    this.type = type;
  }

  /**
   * Returns the dimension line associated to this event.
   */
  public DimensionLine getDimensionLine() {
    return this.dimensionLine;
  }
  
  /**
   * Returns the {@link Type type} of notification this event represents.
   */
  public Type getType() {
    return this.type;
  }
}
