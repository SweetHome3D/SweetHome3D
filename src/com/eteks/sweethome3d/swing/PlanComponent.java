/*
 * PlanComponent.java 2 juin 2006
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
package com.eteks.sweethome3d.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.event.MouseInputAdapter;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;

/**
 * A component displaying the plan of a home.
 * @author Emmanuel Puybaret
 */
public class PlanComponent extends JComponent {
  public PlanComponent(PlanController controller, Home home,
                       UserPreferences preferences) {
    // TODO
  }

  /**
   * Returns the scale used to display the plan in component width.
   */
  public float getScale() {
    // TODO
    return 0;
  }

  /**
   * Sets the scale used to display the plan in component width.
   */
  public void setScale(float scale) {
    // TODO Auto-generated method stub
  }

  /**
   * Returns the selected walls in plan.
   */
  public List<Wall> getSelectedWalls() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Sets the selected walls in plan.
   * @param selectedWalls the list of walls to selected.
   */
  public void setSelectedWalls(List<Wall> selectedWalls) {
    // TODO
  }

  /**
   * Sets rectangle selection feedback coordinates. 
   */
  public void setRectangleFeedback(int x, int y, int width, int height) {
    // TODO
  }
  
  /**
   * Deletes feed back.
   */
  public void deleteFeedback() {
    // TODO
  }
}
