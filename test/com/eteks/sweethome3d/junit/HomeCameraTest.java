/*
 * HomeCameraTest.java 21 juin 2007
 * 
 * Copyright (c) 2007 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.eteks.sweethome3d.junit;

import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;

import junit.extensions.abbot.ComponentTestFixture;
import abbot.finder.ComponentSearchException;
import abbot.tester.ComponentLocation;
import abbot.tester.JComponentTester;

import com.eteks.sweethome3d.io.DefaultUserPreferences;
import com.eteks.sweethome3d.model.Camera;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.swing.HomeComponent3D;
import com.eteks.sweethome3d.swing.HomeController;
import com.eteks.sweethome3d.swing.HomePane;
import com.eteks.sweethome3d.swing.PlanComponent;

/**
 * Tests camera changes in home.
 * @author Emmanuel Puybaret
 */
public class HomeCameraTest extends ComponentTestFixture {
  public void testTransferHandler() throws ComponentSearchException, InterruptedException {
    UserPreferences preferences = new DefaultUserPreferences();
    Home home = new Home();
    HomeController controller = new HomeController(home, preferences);
    PlanComponent planComponent = (PlanComponent)TestUtilities.findComponent(
         controller.getView(), PlanComponent.class);
    HomeComponent3D component3D = (HomeComponent3D)TestUtilities.findComponent(
        controller.getView(), HomeComponent3D.class);

    // 1. Create a frame that displays a home view 
    JFrame frame = new JFrame("Home Camera Test");    
    frame.add(controller.getView());
    frame.pack();

    // Show home plan frame
    showWindow(frame);
    JComponentTester tester = new JComponentTester();
    tester.waitForIdle();
    // Transfer focus to plan view with TAB keys
    tester.actionKeyStroke(KeyEvent.VK_TAB);
    tester.actionKeyStroke(KeyEvent.VK_TAB);
    // Check plan view has focus
    assertTrue("Plan component doesn't have the focus", planComponent.isFocusOwner());
    // Check default camera is the top camera
    assertSame("Default camera isn't top camera", 
        home.getTopCamera(), home.getCamera());
    // Check default camera location and angles
    assertCoordinatesAndAnglesEqualCameraLocationAndAngles(500, 1500, 1000, 
        (float)Math.PI, (float)Math.PI / 4, home.getCamera());
    
    // 2. Create one wall between points (50, 50) and (150, 50) at a bigger scale
    runAction(controller, HomePane.ActionType.CREATE_WALLS);
    runAction(controller, HomePane.ActionType.ZOOM_IN);
    tester.actionClick(planComponent, 50, 50);
    tester.actionClick(planComponent, 150, 50, InputEvent.BUTTON1_MASK, 2);
    // Check wall length is 100 * plan scale
    Wall wall = home.getWalls().iterator().next();
    assertTrue("Incorrect wall length ", 
        Math.abs(wall.getXEnd() - wall.getXStart() - 100 / planComponent.getScale()) < 1E-3);
    float xWallMiddle = (wall.getXEnd() + wall.getXStart()) / 2;
    float yWallMiddle = (wall.getYEnd() + wall.getYStart()) / 2;
    // Check camera location and angles
    assertCoordinatesAndAnglesEqualCameraLocationAndAngles(xWallMiddle, yWallMiddle + 1000, 1000, 
        (float)Math.PI, (float)Math.PI / 4, home.getCamera());
    
    // 3. Transfer focus to 3D view with TAB key 
    tester.actionKeyStroke(KeyEvent.VK_TAB);
    // Check 3D view has focus
    assertTrue("3D component doesn't have the focus", component3D.isFocusOwner());
    // Add 1° to camera pitch
    tester.actionKeyStroke(KeyEvent.VK_PAGE_UP);
    // Check camera location and angles
    assertCoordinatesAndAnglesEqualCameraLocationAndAngles(xWallMiddle, 1009.0617f, 1017.3002f, 
        (float)Math.PI, (float)Math.PI / 4 + (float)Math.PI / 180, home.getCamera());
    
    // 4. Remove 10° from camera yaw 
    tester.actionKeyStroke(KeyEvent.VK_LEFT);
    // Check camera location and angles
    assertCoordinatesAndAnglesEqualCameraLocationAndAngles(263.9243f, 994.1371f, 1017.3002f, 
        (float)Math.PI - 10 * (float)Math.PI / 180, (float)Math.PI / 4 + (float)Math.PI / 180, home.getCamera());
    // Add 1° to camera yaw 
    tester.actionKeyPress(KeyEvent.VK_SHIFT);
    tester.actionKeyStroke(KeyEvent.VK_RIGHT);
    tester.actionKeyRelease(KeyEvent.VK_SHIFT);
    // Check camera location and angles
    assertCoordinatesAndAnglesEqualCameraLocationAndAngles(247.0138f, 996.967f, 1017.3002f, 
        (float)Math.PI - 9 * (float)Math.PI / 180, (float)Math.PI / 4 + (float)Math.PI / 180, home.getCamera());
    
    // 5. Move camera 1cm forward
    tester.actionKeyPress(KeyEvent.VK_SHIFT);
    tester.actionKeyStroke(KeyEvent.VK_UP);
    tester.actionKeyRelease(KeyEvent.VK_SHIFT);
    // Check camera location and angles
    assertCoordinatesAndAnglesEqualCameraLocationAndAngles(246.9051f, 996.2808f, 1016.5808f, 
        (float)Math.PI - 9 * (float)Math.PI / 180, (float)Math.PI / 4 + (float)Math.PI / 180, home.getCamera());
    // Move camera 10 backward 
    tester.actionKeyStroke(KeyEvent.VK_DOWN);
    // Check camera location and angles
    assertCoordinatesAndAnglesEqualCameraLocationAndAngles(247.992f, 1003.142f, 1023.774f, 
        (float)Math.PI - 9 * (float)Math.PI / 180, (float)Math.PI / 4 + (float)Math.PI / 180, home.getCamera());
    
    // 6. View from observer
    runAction(controller, HomePane.ActionType.VIEW_FROM_OBSERVER);
    tester.waitForIdle();
    // Check camera is the observer camera
    assertSame("Camera isn't observer camera", home.getObserverCamera(), home.getCamera());
    // Check default camera location and angles
    assertCoordinatesAndAnglesEqualCameraLocationAndAngles(100, 100, 170, 
        3 * (float)Math.PI / 4, (float)Math.PI / 16, home.getCamera());
    // Check observer camera is selected
    assertEquals("Wrong selected items count", 1, home.getSelectedItems().size());
    assertTrue("Camera isn't selected", home.getSelectedItems().contains(home.getCamera()));

    // Try to select wall and observer camera 
    runAction(controller, HomePane.ActionType.SELECT);
    tester.actionClick(planComponent, 50, 50);
    tester.actionKeyPress(KeyEvent.VK_SHIFT);
    tester.actionClick(planComponent, (int)(140 * planComponent.getScale()), 
        (int)(140 * planComponent.getScale()));
    tester.actionKeyRelease(KeyEvent.VK_SHIFT);
    // Check selected items contains only wall
    assertEquals("Wrong selected items count", 1, home.getSelectedItems().size());
    assertTrue("Wall isn't selected", home.getSelectedItems().contains(wall));
    
    // Select observer camera
    Thread.sleep(1000); // Wait 1s to avoid double click
    tester.actionClick(planComponent, (int)(140 * planComponent.getScale()), 
        (int)(140 * planComponent.getScale()));
    // Check observer camera is selected
    assertEquals("Wrong selected items count", 1, home.getSelectedItems().size());
    assertTrue("Camera isn't selected", home.getSelectedItems().contains(home.getCamera()));
    
    // 7. Move camera at right and down
    tester.actionKeyStroke(KeyEvent.VK_RIGHT);
    tester.actionKeyStroke(KeyEvent.VK_DOWN);
    // Check camera location and angles
    assertCoordinatesAndAnglesEqualCameraLocationAndAngles(100 + 1 / planComponent.getScale(), 
        100 + 1 / planComponent.getScale(), 170, 
        3 * (float)Math.PI / 4, (float)Math.PI / 16, home.getCamera());
    
    // 8. Change camera yaw by moving its yaw indicator
    float [][] cameraPoints = home.getObserverCamera().getPoints();
    int xYawIndicator = (int)(((40 + (cameraPoints[0][0] + cameraPoints[3][0]) / 2)) * planComponent.getScale());
    int yYawIndicator = (int)(((40 + (cameraPoints[0][1] + cameraPoints[3][1]) / 2)) * planComponent.getScale());
    tester.actionMousePress(planComponent, new ComponentLocation(
        new Point(xYawIndicator, yYawIndicator)));
    tester.actionMouseMove(planComponent, new ComponentLocation(
        new Point(xYawIndicator + 2, yYawIndicator + 2)));
    tester.actionMouseRelease();
    // Check camera yaw angle changed
    assertCoordinatesAndAnglesEqualCameraLocationAndAngles(100 + 1 / planComponent.getScale(), 
        100 + 1 / planComponent.getScale(), 170, 
        2.5156f, (float)Math.PI / 16, home.getCamera());

    // Change camera pitch by moving its pitch indicator
    cameraPoints = home.getObserverCamera().getPoints();
    int xPitchIndicator = (int)(((40 + (cameraPoints[1][0] + cameraPoints[2][0]) / 2)) * planComponent.getScale());
    int yPitchIndicator = (int)(((40 + (cameraPoints[1][1] + cameraPoints[2][1]) / 2)) * planComponent.getScale());
    tester.actionMousePress(planComponent, new ComponentLocation(
        new Point(xPitchIndicator, yPitchIndicator)));
    tester.actionMouseMove(planComponent, new ComponentLocation(
        new Point(xPitchIndicator + 2, yPitchIndicator + 2)));
    tester.actionMouseRelease();
    // Check camera pitch angle changed
    assertCoordinatesAndAnglesEqualCameraLocationAndAngles(100 + 1 / planComponent.getScale(), 
        100 + 1 / planComponent.getScale(), 170, 
        2.5156f, 0.0389f, home.getCamera());
    
    // 9. Change camera location with mouse in 3D view
    tester.actionMousePress(component3D, new ComponentLocation(new Point(10, 10)));
    tester.actionKeyPress(KeyEvent.VK_ALT);
    tester.actionMouseMove(component3D, new ComponentLocation(new Point(10, 20)));
    tester.actionKeyRelease(KeyEvent.VK_ALT);
    tester.actionMouseRelease();
    // Check camera location changed
    assertCoordinatesAndAnglesEqualCameraLocationAndAngles(130.6284f, 141.8525f, 170, 
        2.5156f, 0.0389f, home.getCamera());

    // 10. Change camera yaw with mouse in 3D view
    tester.actionMousePress(component3D, new ComponentLocation(new Point(10, 20)));
    tester.actionMouseMove(component3D, new ComponentLocation(new Point(20, 20)));
    tester.actionMouseRelease();
    // Check camera yaw changed
    assertCoordinatesAndAnglesEqualCameraLocationAndAngles(130.6284f, 141.8525f, 170, 
        3.2156f, 0.0389f, home.getCamera());
    
    // Change camera pitch with mouse in 3D view
    tester.actionMousePress(component3D, new ComponentLocation(new Point(20, 20)));
    tester.actionMouseMove(component3D, new ComponentLocation(new Point(20, 30)));
    tester.actionMouseRelease();
    // Check camera yaw changed
    assertCoordinatesAndAnglesEqualCameraLocationAndAngles(130.6284f, 141.8525f, 170, 
        3.2156f, 0.1089f, home.getCamera());
  }
  
  /**
   * Runs <code>actionPerformed</code> method matching <code>actionType</code> 
   * in <code>HomePane</code>. 
   */
  private void runAction(HomeController controller,
                         HomePane.ActionType actionType) {
    controller.getView().getActionMap().get(actionType).actionPerformed(null);
  }

  /**
   * Asserts the location and angles of <code>camera</code> are the point 
   * at (<code>x</code>, <code>y</code>, <code>z</code>) and the angles
   * <code>yaw</code> and <code>pitch</code>. 
   */
  private void assertCoordinatesAndAnglesEqualCameraLocationAndAngles(float x, float y, 
                                                float z, float yaw, float pitch, 
                                                Camera camera) {
    assertTrue("Incorrect X " + camera.getX() + ", should be " + x, 
        Math.abs(x - camera.getX()) < 1E-3);
    assertTrue("Incorrect Y " + camera.getY() + ", should be " + y, 
        Math.abs(y - camera.getY()) < 1E-3);
    assertTrue("Incorrect Z " + camera.getZ() + ", should be " + z, 
        Math.abs(z - camera.getZ()) < 1E-3);
    assertTrue("Incorrect yaw " + camera.getYaw() + ", should be " + yaw, 
        Math.abs(yaw - camera.getYaw()) < 1E-3);
    assertTrue("Incorrect pitch " + camera.getPitch() + ", should be " + pitch, 
        Math.abs(pitch - camera.getPitch()) < 1E-3);
  }
}
