/*
 * Elevatable.java 25 oct. 2011
 *
 * Sweet Home 3D, Copyright (c) 2011 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

/**
 * An object that belongs to a level.
 * @author Emmanuel Puybaret
 * @since 3.4
 */
public interface Elevatable {
  /**
   * Returns the level of this object.
   */
  public abstract Level getLevel();
  
  /**
   * Returns <code>true</code> if this object can be viewed at the given level.
   */
  public abstract boolean isAtLevel(Level level);
}
