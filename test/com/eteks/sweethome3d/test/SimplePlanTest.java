/*
 * SimplePlanTest.java 13 juin 2006
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
package com.eteks.sweethome3d.test;

import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JFrame;

import com.eteks.sweethome3d.io.DefaultUserPreferences;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.swing.PlanComponent;

/**
 * Displays in a frame a plan with a few walls. 
 * @author Emmanuel Puybaret
 */
public class SimplePlanTest {
  public static void main(String [] args) {
    // Create 3 walls
    Wall wall1 = new Wall(-100, 0, 200, 0, 25);
    Wall wall2 = new Wall(200, 0, 500, 300, 25);
    Wall wall3 = new Wall(-100, 0, -100, 300, 10);
    // Add them to a Plan instance
    Home home = new Home();
    home.addWall(wall1);
    home.addWall(wall2);
    home.addWall(wall3);
    // Join the two first walls
    home.setWallAtEnd(wall1, wall2);
    home.setWallAtStart(wall2, wall1);
    // Create a component that displays this plan  
    PlanComponent planComponent = new PlanComponent(home, new DefaultUserPreferences(), null);
    planComponent.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    home.setSelectedItems(Arrays.asList(new Wall [] {wall2, wall3}));
    planComponent.setRectangleFeedback(-125, 100, 700, 225);
    // Show the component in a frame
    JFrame frame = new JFrame("Plan Test");
    frame.add(planComponent);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);
  }
}
