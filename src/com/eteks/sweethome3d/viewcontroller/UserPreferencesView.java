/*
 * UserPreferencesView.java 28 oct 2008
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
package com.eteks.sweethome3d.viewcontroller;

import com.eteks.sweethome3d.model.UserPreferences;

/**
 * A MVC view that edits user preferences. 
 * @author Emmanuel Puybaret
 */
public interface UserPreferencesView extends View {
  /**
   * Returns the chosen language in panel.
   */
  public abstract String getLanguage();

  /**
   * Returns the chosen unit in panel.
   */
  public abstract UserPreferences.Unit getUnit();

  /**
   * Returns <code>true</code> if magnetism is enabled in panel.
   */
  public abstract boolean isMagnetismEnabled();

  /**
   * Returns <code>true</code> if rulers are visible in panel.
   */
  public abstract boolean isRulersVisible();

  /**
   * Returns <code>true</code> if grid is visible in panel.
   */
  public abstract boolean isGridVisible();

  /**
   * Returns the new wall thickness in panel.
   */
  public abstract float getNewWallThickness();

  /**
   * Returns the new wall height in panel.
   */
  public abstract float getNewWallHeight();

  /**
   * Displays this panel in a dialog box. 
   */
  public abstract void displayView(View parentView);
}