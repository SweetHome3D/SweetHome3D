/*
 * HomeEvent.java 1 sept. 2006
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
 * Type of event notified when a {@link Home home} is added to or deleted 
 * from an {@link HomeApplication application}.
 * @author Emmanuel Puybaret
 */
public class HomeEvent extends EventObject {
  public enum Type {ADD, DELETE}

  private Home home;
  private Type type;

  /**
   * Creates an event with <code>home</code> as source, and an associated <code>piece</code>.
   */
  public HomeEvent(Object source, Home home, Type type) {
    super(source);
    this.home = home;
    this.type =  type;
  }

  /**
   * Returns the home added or deleted.
   */
  public Home getHome() {
    return this.home;
  }

  /**
   * Returns the type of event. 
   */
  public Type getType() {
    return this.type;
  }
}
