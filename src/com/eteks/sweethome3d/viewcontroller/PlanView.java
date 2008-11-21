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

import com.eteks.sweethome3d.model.DimensionLine;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.Wall;

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
   * Sets the cursor of this component as rotation cursor. 
   */
  public abstract void setCursor(CursorType cursorType);

  /**
   * Sets tool tip text displayed as feeback. 
   * @param toolTipFeedback the text displayed in the tool tip 
   *                    or <code>null</code> to make tool tip disapear.
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
   * Sets the location point for <code>wall</code> alignment feedback. 
   */
  public abstract void setWallAlignmentFeedback(Wall wall, float x, float y);

  /**
   * Deletes the wall alignment feedback. 
   */
  public abstract void deleteWallAlignmentFeedback();

  /**
   * Sets the location point for <code>room</code> alignment feedback.
   */
  public abstract void setRoomAlignmentFeedback(Room room,
                                                float x, 
                                                float y, boolean magnetizedPoint);

  /**
   * Deletes the room point feedback.
   */
  public abstract void deleteRoomAlignmentFeedback();

  /**
   * Sets the location point for <code>dimensionLine</code> alignment feedback. 
   */
  public abstract void setDimensionLineAlignmentFeedback(DimensionLine dimensionLine,
                                                        float x,
                                                        float y);

  /**
   * Deletes the dimension line alignment feedback. 
   */
  public abstract void deleteDimensionLineAlignmentFeedback();

  /**
   * Returns the component used as an horizontal ruler for this plan.
   */
  public abstract View getHorizontalRuler();

  /**
   * Returns the component used as a vertical ruler for this plan.
   */
  public abstract View getVerticalRuler();
}