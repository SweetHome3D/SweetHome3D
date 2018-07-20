/*
 * PlanView.java 20 july 2018
 *
 * Sweet Home 3D, Copyright (c) 2018 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
package com.eteks.sweethome3d.viewcontroller;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;

/**
 * The view that displays the furniture of a home.
 * @author Emmanuel Puybaret
 */
public interface FurnitureView extends TransferableView, ExportableView {
  /**
   * Sets the filter applied to the furniture.
   */
  public void setFurnitureFilter(FurnitureFilter furnitureFilter);

  /**
   * Returns the filter applied to the furniture.
   */
  public FurnitureFilter getFurnitureFilter();

  /**
   * The super type used to specify how furniture should be filtered in viewed furniture.
   */
  public static interface FurnitureFilter {
    /**
     * Returns <code>true</code> if the given <code>piece</code> should be shown,
     * otherwise returns <code>false</code> if the <code>piece</code> should be hidden.
     */
    public abstract boolean include(Home home, HomePieceOfFurniture piece);
  }
}