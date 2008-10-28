/*
 * HomeView.java 28 oct 2008
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
package com.eteks.sweethome3d.viewcontroller;

/**
 * A view that edits home furniture attributes. 
 * @author Emmanuel Puybaret
 */
public interface HomeFurnitureView extends View {
  /**
   * Returns the edited name of the furniture or <code>null</code>.
   */
  public abstract String getFurnitureName();

  /**
   * Returns the edited width of the furniture or <code>null</code>.
   */
  public abstract Float getFurnitureWidth();

  /**
   * Returns the edited depth of the furniture or <code>null</code>.
   */
  public abstract Float getFurnitureDepth();

  /**
   * Returns the edited height of the furniture or <code>null</code>.
   */
  public abstract Float getFurnitureHeight();

  /**
   * Returns the edited color of the furniture or <code>null</code>.
   */
  public abstract Integer getFurnitureColor();

  /**
   * Returns whether the furniture is visible or not.
   */
  public abstract Boolean isFurnitureVisible();

  /**
   * Returns whether the furniture model is mirrored or not.
   */
  public abstract Boolean isFurnitureModelMirrored();

  /**
   * Returns the edited abscissa of the furniture or <code>null</code>.
   */
  public abstract Float getFurnitureX();

  /**
   * Returns the edited ordinate of the furniture or <code>null</code>.
   */
  public abstract Float getFurnitureY();

  /**
   * Returns the edited elevation of the furniture or <code>null</code>.
   */
  public abstract Float getFurnitureElevation();

  /**
   * Returns the edited angle of the furniture or <code>null</code>.
   */
  public abstract Float getFurnitureAngle();

  /**
   * Displays this view in a modal dialog box. 
   */
  public abstract void displayView(View parentView);

}