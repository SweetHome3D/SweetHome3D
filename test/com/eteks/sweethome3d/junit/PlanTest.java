/*
 * HomeTest.java 6 juin 2006
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
package com.eteks.sweethome3d.junit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import com.eteks.sweethome3d.model.Plan;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.model.WallEvent;
import com.eteks.sweethome3d.model.WallListener;

/**
 * Tests {@link com.eteks.sweethome3d.model.Plan Plan} class.
 * @author Emmanuel Puybaret
 */
public class PlanTest extends TestCase {
  public void testPlanWalls() {
    // Create a plan and a wall listener that updates lists when notified 
    Plan plan = new Plan();
    final List<Wall> addedWalls = new ArrayList<Wall>();
    final List<Wall> deletedWalls = new ArrayList<Wall>();
    final List<Wall> updatedWalls = new ArrayList<Wall>();
    plan.addWallListener(new WallListener () {
      public void wallChanged(WallEvent ev) {
        switch (ev.getType()) {
          case ADD :
            addedWalls.add(ev.getWall());
            break;
          case DELETE :
            deletedWalls.add(ev.getWall());
            break;
          case UPDATE :
            updatedWalls.add(ev.getWall());
            break;
        }
      }
    });
    
    // Create 2 walls
    Wall wall1 = new Wall(0, 0, 100, 0, 0, 0, 0);
    Wall wall2 = new Wall(100, 0, 100, 100, 0, 0, 0);
    // Add them to plan
    plan.addWall(wall1);
    plan.addWall(wall2);
    // Check they were added and that wall listener received a notification for each wall
    assertWallCollectionContains(plan.getWalls(), wall1, wall2);
    assertWallCollectionContains(addedWalls, wall1, wall2);
    
    // Join end point of first wall to start point of second wall
    plan.setWallAtEnd(wall1, wall2);
    // Check wall1 end wall is wall2 and that wall listener received 1 notification
    assertSame("Wall not joined", wall2, wall1.getWallAtEnd());
    assertWallCollectionContains(updatedWalls, wall1);

    // Join start point of second wall to end point of first wall
    updatedWalls.clear();
    plan.setWallAtStart(wall2, wall1);
    // Check wall2 start wall is wall1 and that wall listener received 1 notification
    assertSame("Wall not joined", wall1, wall2.getWallAtStart());
    assertWallCollectionContains(updatedWalls, wall2);
    
    // Move end point of second wall
    updatedWalls.clear();
    plan.moveWallEndPointTo(wall2, 60, 100);
    // Check wall2 end position and that wall listener received 1 notifications
    assertEquals("Incorrect abscissa", 60f, wall2.getXEnd());
    assertEquals("Incorrect ordinate", 100f, wall2.getYEnd());
    assertWallCollectionContains(updatedWalls, wall2);

    // Move point shared by the two walls
    updatedWalls.clear();
    plan.moveWallStartPointTo(wall2, 60, 0);
    // Check wall2 start point position
    assertEquals("Incorrect abscissa", 60f, wall2.getXStart());
    assertEquals("Incorrect ordinate", 0f, wall2.getYStart());
    // Check that wall listener received 2 notifications
    assertWallCollectionContains(updatedWalls, wall2);

    updatedWalls.clear();
    plan.moveWallEndPointTo(wall1, 60, 0);
    // Check wall1 end point position
    assertEquals("Incorrect abscissa", 60f, wall1.getXEnd());
    assertEquals("Incorrect ordinate", 0f, wall1.getYEnd());
    // Check that wall listener received 2 notifications
    assertWallCollectionContains(updatedWalls, wall1);
    
    // Detach second wall from first wall
    updatedWalls.clear();
    plan.setWallAtStart(wall2, null);
    // Check wall2 and wall1 are not joined and that wall listener received 2 notifications
    assertSame("Wall joined", null, wall1.getWallAtEnd());
    assertSame("Wall joined", null, wall2.getWallAtStart());
    assertWallCollectionContains(updatedWalls, wall1, wall2);
    
    // Delete second wall
    plan.deleteWall(wall2);
    // Check it was removed and that wall listener received a notification 
    assertWallCollectionContains(plan.getWalls(), wall1);
    assertWallCollectionContains(deletedWalls, wall2);
  }

  private void assertWallCollectionContains(Collection<Wall> wallCollection, Wall ... walls) {
    assertEquals("Walls incorrect count", walls.length, wallCollection.size());
    for (Wall wall : walls) {
      assertTrue("Wall doesn't belong to collection", wallCollection.contains(wall));
    }
  }
}
