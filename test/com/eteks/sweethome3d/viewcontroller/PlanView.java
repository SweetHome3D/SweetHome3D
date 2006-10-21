/*
 * PlanView.java 29 mai 2006
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
package com.eteks.sweethome3d.viewcontroller;

/**
 * A MVC view for plan component.
 * @author Emmanuel Puybaret
 */
public interface PlanView extends View {
  /**
   * Sets rectangle selection feedback coordinates. 
   */
  public void setRectangleFeedback(float x0, float y0, float x1, float y1);

  /**
   * Deletes rectangle feed back.
   */
  public void deleteRectangleFeedback();

  /**
   * Ensures selected walls are visible at screen and moves
   * scroll bars if needed.
   */
  public void makeSelectionVisible();

  /**
   * Ensures the point at (<code>xPixel</code>, <code>yPixel</code>) is visible,
   * moving scroll bars if needed.
   */
  public void makePointVisible(float x, float y);

  /**
   * Sets mouse cursor, depending on mode.
   */
  public void setCursor(PlanController.Mode mode);

  /**
   * Returns the scale used to display the plan.
   */
  public float getScale();

  /**
   * Sets the scale used to display the plan.
   */
  public void setScale(float scale);

  /**
   * Sets the cursor of this component as rotation cursor. 
   */
  public void setRotationCursor();
}