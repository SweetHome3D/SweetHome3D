/*
 * Home.java 15 mai 2006
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

import java.util.List;

/**
 * @author Emmanuel Puybaret
 */
public class Home {

  /**
   * Returns an unmodifiable list of the furniture managed by this home.
   */
  public List<HomePieceOfFurniture> getFurniture() {
    // TODO Return an unmodifiable list of the furniture managed by this home
    // Sort here or in view ? if true, change "should be" in "is"
    return null;
  }

  /**
   * Adds the <code>piece</code> in parameter at a given <code>index</code>.
   */
  public void add(HomePieceOfFurniture piece, int index) {
    // Add piece in furniture list 
  }

  /**
   * Removes a given <code>piece</code> of furniture from this home.
   */
  public void delete(HomePieceOfFurniture piece) {
    // TODO Auto-generated method stub
    
  }

  /**
   * Returns the property on which the furniture of this home should be sorted.
   * @return the name of a property or <code>null</code> if the furniture isn't sorted.
   */
  public String getSortedProperty() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Sets the property on which the furniture of this home should be sorted.
   */
  public void setSortedProperty(String property) {
    // TODO Auto-generated method stub
    
  }

  /**
   * Sets whether the furniture of this home should be sorted in ascending or
   * descending order.
   * @param ascending if <code>true</code> the furniture should be sorted in
   *          ascending order on the
   *          {@link #getSortedProperty() sorted property}.
   */
  public void setAscendingSort(boolean ascending) {
    // TODO Auto-generated method stub
    
  }

  /**
   * Returns whether the furniture of this home should be sorted in ascending or
   * descending order.
   * @return  <code>true</code> the furniture should be sorted in
   *          ascending order on the
   *          {@link #getSortedProperty() sorted property}.
   */
  public boolean isAscendingSort() {
    // TODO Auto-generated method stub
    return false;
  }
}
