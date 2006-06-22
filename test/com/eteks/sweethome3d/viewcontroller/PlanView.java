/*
 * HomeView.java 29 mai 2006
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

import java.util.Collection;
import java.util.List;

import com.eteks.sweethome3d.model.Wall;

/**
 * A MVC view for plan component.
 * @author Emmanuel Puybaret
 */
public interface PlanView extends View {
  /**
   * Returns an unmodifiable list of the selected walls in plan.
   */
  public List<Wall> getSelectedWalls();

  /**
   * Sets the selected walls in plan.
   * @param selectedWalls the list of walls to selected.
   */
  public void setSelectedWalls(List<Wall> selectedWalls);

  /**
   * Sets rectangle selection feedback coordinates. 
   */
  public void setRectangleFeedback(float x0, float y0, float x1, float y1);

  /**
   * Deletes rectangle feed back.
   */
  public void deleteRectangleFeedback();

  /**
   * Returns <code>true</code> if <code>wall</code> intersects
   * with the horizontal rectangle which opposite corners are at points
   * (<code>x0</code>, <code>y0</code>) and (<code>x1</code>, <code>y1</code>).
   */
  public boolean doesWallIntersectRectangle(Wall wall, float x0, float y0, float x1, float y1);

  /**
   * Returns <code>true</code> if <code>wall</code> contains 
   * the point at (<code>x</code>, <code>y</code>)
   * with a margin of 2 pixels.
   */
  public boolean containsWallAt(Wall wall, float x, float y);

  /**
   * Returns <code>true</code> if <code>wall</code> start line contains 
   * the point at (<code>x</code>, <code>y</code>)
   * with a margin of 2 pixels around the wall start line.
   */
  public boolean containsWallStartAt(Wall wall, float x, float y);

  /**
   * Returns <code>true</code> if <code>wall</code> end line contains 
   * the point at (<code>x</code>, <code>y</code>)
   * with a margin of 2 pixels around the wall end line.
   */
  public boolean containsWallEndAt(Wall wall, float x, float y);

  /**
   * Ensures <code>walls</code> are visible at screen and moves
   * scroll bars if needed.
   */
  public void ensureWallsAreVisible(Collection<Wall> walls);

  /**
   * Ensures the point at (<code>xPixel</code>, <code>yPixel</code>) is visible,
   * moving scroll bars if needed.
   */
  public void ensurePointIsVisible(float x, float y);

  /**
   * Sets mouse cursor, depending on mode.
   */
  public void setCursor(PlanController.Mode mode);
}