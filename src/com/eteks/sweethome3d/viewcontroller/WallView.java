/*
 * WallView.java 28 oct 2008
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
 * Wall editing view.
 * @author Emmanuel Puybaret
 */
public interface WallView extends View {
  /**
   * Returns the abscissa of the start point of the wall.
   */
  public abstract Float getWallXStart();

  /**
   * Returns the ordinate of the start point of the wall.
   */
  public abstract Float getWallYStart();

  /**
   * Returns the abscissa of the end point of the wall.
   */
  public abstract Float getWallXEnd();

  /**
   * Returns the ordinate of the end point of the wall.
   */
  public abstract Float getWallYEnd();

  /**
   * Returns the edited color of the wall(s) left side or <code>null</code>.
   */
  public abstract Integer getWallLeftSideColor();

  /**
   * Returns the edited texture of the wall(s) left side or <code>null</code>.
   */
  public abstract HomeTexture getWallLeftSideTexture();

  /**
   * Returns the edited color of the wall(s) right side or <code>null</code>.
   */
  public abstract Integer getWallRightSideColor();
  
  /**
   * Returns the edited texture of the wall(s) right side or <code>null</code>.
   */
  public abstract HomeTexture getWallRightSideTexture();

  /**
   * Returns the edited thickness of the wall(s) or <code>null</code>.
   */
  public abstract Float getWallThickness();

  /**
   * Returns the edited height of the wall(s) or <code>null</code>.
   */
  public abstract Float getWallHeight();

  /**
   * Returns the edited height at end of the wall(s) or <code>null</code>.
   */
  public abstract Float getWallHeightAtEnd();

  /**
   * Displays this panel in a modal dialog box. 
   */
  public void displayView(View parentView);
}
