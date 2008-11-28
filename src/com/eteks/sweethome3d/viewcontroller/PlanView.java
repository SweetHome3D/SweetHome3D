/*
 * PlanView.java 28 oct 2008
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

import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.TextStyle;

/**
 * The view that displays the plan of a home.
 * @author Emmanuel Puybaret
 */
public interface PlanView extends View {
  public enum CursorType {SELECTION, DRAW, ROTATION, ELEVATION, HEIGHT, RESIZE, DUPLICATION}

  /**
   * Sets rectangle selection feedback coordinates. 
   */
  public abstract void setRectangleFeedback(float x0, float y0,
                                            float x1, float y1);

  /**
   * Deletes rectangle feed back.
   */
  public abstract void deleteRectangleFeedback();

  /**
   * Ensures selected items are visible at screen and moves
   * scroll bars if needed.
   */
  public abstract void makeSelectionVisible();

  /**
   * Ensures the point at (<code>xPixel</code>, <code>yPixel</code>) is visible,
   * moving scroll bars if needed.
   */
  public abstract void makePointVisible(float x, float y);

  /**
   * Returns the scale used to display the plan.
   */
  public abstract float getScale();

  /**
   * Sets the scale used to display the plan.
   */
  public abstract void setScale(float scale);

  /**
   * Returns <code>x</code> converted in model coordinates space.
   */
  public abstract float convertXPixelToModel(int x);

  /**
   * Returns <code>y</code> converted in model coordinates space.
   */
  public abstract float convertYPixelToModel(int y);

  /**
   * Returns the length in centimeters of a pixel with the current scale.
   */
  public abstract float getPixelLength();

  /**
   * Returns the coordinates of the bounding rectangle of the <code>text</code> displayed at
   * the point (<code>x</code>,<code>y</code>).  
   */
  public abstract float [][] getTextBounds(String text, TextStyle style, 
                                           float x, float y, float angle);

  /**
   * Sets the cursor of this component as rotation cursor. 
   */
  public abstract void setCursor(CursorType cursorType);

  /**
   * Sets tool tip text displayed as feedback. 
   * @param toolTipFeedback the text displayed in the tool tip 
   *                    or <code>null</code> to make tool tip disappear.
   */
  public abstract void setToolTipFeedback(String toolTipFeedback,
                                          float x, float y);

  /**
   * Deletes tool tip text from screen. 
   */
  public abstract void deleteToolTipFeedback();

  /**
   * Sets whether the resize indicator of selected wall or piece of furniture 
   * should be visible or not. 
   */
  public abstract void setResizeIndicatorVisible(boolean resizeIndicatorVisible);

  /**
   * Sets the location point for alignment feedback.
   */
  public abstract void setAlignmentFeedback(Class<? extends Selectable> alignedObjectClass,
                                            Selectable alignedObject,
                                            float x, 
                                            float y, 
                                            boolean showPoint);
  /**
   * Deletes the alignment feedback. 
   */
  public abstract void deleteAlignmentFeedback();

  /**
   * Returns the component used as an horizontal ruler for this plan.
   */
  public abstract View getHorizontalRuler();

  /**
   * Returns the component used as a vertical ruler for this plan.
   */
  public abstract View getVerticalRuler();
}