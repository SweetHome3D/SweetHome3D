/*
 * HelpView.java  28 oct. 2008
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

import java.net.URL;

/**
 * A view that displays Sweet Home 3D help.
 * @author Emmanuel Puybaret
 */
public interface HelpView extends View {
  /**
   * Displays this pane in a frame.
   */
  public abstract void displayView();

  /**
   * Displays <code>url</code> in this pane.
   */
  public abstract void setPage(URL url);

  /**
   * Displays <code>url</code> in standard browser.
   */
  public abstract void setBrowserPage(URL url);

  /**
   * Sets whether previous button should be enabled or not.
   */
  public abstract void setPreviousEnabled(boolean enabled);

  /**
   * Sets whether next button should be enabled or not.
   */
  public abstract void setNextEnabled(boolean enabled);

}