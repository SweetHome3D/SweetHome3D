/*
 * Home3DAttributesView.java  28 oct. 2008
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

import com.eteks.sweethome3d.model.HomeTexture;

/**
 * Home 3D attributes editing view.
 * @author Emmanuel Puybaret
 */
public interface Home3DAttributesView extends View {
  /**
   * Returns the edited field of view of the observer camera in radians.
   */
  public abstract float getObserverCameraFieldOfView();
  
  /**
   * Returns the edited height of the observer camera.
   */
  public abstract float getObserverCameraHeight();
  
  /**
   * Returns the edited ground color.
   */
  public abstract int getGroundColor();

  /**
   * Returns the edited ground texture.
   */
  public abstract HomeTexture getGroundTexture();

  /**
   * Returns the edited sky color.
   */
  public abstract int getSkyColor();

  /**
   * Returns the edited light color.
   */
  public abstract int getLightColor();

  /**
   * Returns the edited walls alpha.
   */
  public abstract float getWallsAlpha();

  /**
   * Displays this view in a modal dialog box. 
   */
  public abstract void displayView(View parentView);
}
