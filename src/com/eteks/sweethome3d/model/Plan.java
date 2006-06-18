/*
 * Plan.java 3 juin 2006
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
package com.eteks.sweethome3d.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A plan that contains walls.
 * @author Emmanuel Puybaret
 */
public class Plan {
  private Collection<Wall>   walls;
  private List<WallListener> wallListeners;

  /**
   * Creates a plan with no wall.
   */
  public Plan() {
    this.walls = new ArrayList<Wall>();
    this.wallListeners = new ArrayList<WallListener>();
  }

  /**
   * Adds the wall <code>listener</code> in parameter to this plan.
   * <br>Caution : This method isn't thread safe.
   */
  public void addWallListener(WallListener listener) {
    this.wallListeners.add(listener);
  }
  
  /**
   * Removes the wall <code>listener</code> in parameter from this plan.
   * <br>Caution : This method isn't thread safe.
   */
  public void removeWallListener(WallListener listener) {
    this.wallListeners.remove(listener); 
  } 

  /**
   * Returns an unmodifiable collection of the walls of this plan.
   */
  public Collection<Wall> getWalls() {
    return Collections.unmodifiableCollection(this.walls);
  }

  /**
   * Adds a given <code>wall</code> to the set of walls of this plan.
   * Once the <code>wall</code> is added, wall listeners added to this plan will receive a
   * {@link WallListener#wallChanged(WallEvent) wallChanged}
   * notification, with an {@link WallEvent#getType() event type} 
   * equal to {@link WallEvent.Type#ADD ADD}. 
   * <br>Caution : This method isn't thread safe.
   */
  public void addWall(Wall wall) {
    this.walls.add(wall);
    fireWallEvent(wall, WallEvent.Type.ADD);
  }

  /**
   * Removes a given <code>wall</code> from the set of walls of this plan.
   * Once the <code>wall</code> is removed, wall listeners added to this plan will receive a
   * {@link WallListener#wallChanged(WallEvent) wallChanged}
   * notification, with an {@link WallEvent#getType() event type} 
   * equal to {@link WallEvent.Type#DELETE DELETE}.
   * If any wall is attached to <code>wall</code> they will be detached from it ;
   * therefore wall listeners will receive a 
   * {@link WallListener#wallChanged(WallEvent) wallChanged}
   * notification, with an {@link WallEvent#getType() event type} 
   * equal to {@link WallEvent.Type#UPDATE UPDATE}. 
   * <br>Caution : This method isn't thread safe.
   */
  public void deleteWall(Wall wall) {
    // Detach any other wall attached to wall
    for (Wall otherWall : getWalls()) {
      if (wall.equals(otherWall.getWallAtStart())) {
        setWallAtStart(otherWall, null);
      } else if (wall.equals(otherWall.getWallAtEnd())) {
        setWallAtEnd(otherWall, null);
      }
    }
    this.walls.remove(wall);
    fireWallEvent(wall, WallEvent.Type.DELETE);
  }

  /**
   * Moves <code>wall</code> start point to (<code>x</code>, <code>y</code>).
   * Once the <code>wall</code> is updated, wall listeners added to this plan will receive a
   * {@link WallListener#wallChanged(WallEvent) wallChanged}
   * notification, with an {@link WallEvent#getType() event type} 
   * equal to {@link WallEvent.Type#UPDATE UPDATE}. 
   * No change is made on walls attached to <code>wall</code>.
   * <br>Caution : This method isn't thread safe.
   */
  public void moveWallStartPointTo(Wall wall, float x, float y) {
    if (x != wall.getXStart() || y != wall.getYStart()) {
      wall.setXStart(x);
      wall.setYStart(y);
      fireWallEvent(wall, WallEvent.Type.UPDATE);
    }
  }

  /**
   * Moves <code>wall</code> end point to (<code>x</code>, <code>y</code>) pixels.
   * Once the <code>wall</code> is updated, wall listeners added to this plan will receive a
   * {@link WallListener#wallChanged(WallEvent) wallChanged}
   * notification, with an {@link WallEvent#getType() event type} 
   * equal to {@link WallEvent.Type#UPDATE UPDATE}. 
   * No change is made on walls attached to <code>wall</code>.
   * <br>Caution : This method isn't thread safe.
   */
  public void moveWallEndPointTo(Wall wall, float x, float y) {
    if (x != wall.getXEnd() || y != wall.getYEnd()) {
      wall.setXEnd(x);
      wall.setYEnd(y);
      fireWallEvent(wall, WallEvent.Type.UPDATE);
    }
  }

  /**
   * Sets the wall at start of <code>wall</code> as <code>wallAtEnd</code>. 
   * Once the <code>wall</code> is updated, wall listeners added to this plan will receive a
   * {@link WallListener#wallChanged(WallEvent) wallChanged} notification, with
   * an {@link WallEvent#getType() event type} equal to
   * {@link WallEvent.Type#UPDATE UPDATE}. 
   * If the wall attached to <code>wall</code> start point is attached itself
   * to <code>wall</code>, this wall will be detached from <code>wall</code>, 
   * and wall listeners will receive
   * {@link WallListener#wallChanged(WallEvent) wallChanged}
   * notification about this wall, with an {@link WallEvent#getType() event type} 
   * equal to {@link WallEvent.Type#UPDATE UPDATE}. 
   * <br>Caution : This method isn't thread safe.
   * @param wallAtStart a wall or <code>null</code> to detach <code>wall</code>
   *          from any wall it was attached to before.
   */
  public void setWallAtStart(Wall wall, Wall wallAtStart) {
    detachJoinedWall(wall, wall.getWallAtStart());    
    wall.setWallAtStart(wallAtStart);
    fireWallEvent(wall, WallEvent.Type.UPDATE);
  }

  /**
   * Sets the wall at end of <code>wall</code> as <code>wallAtEnd</code>. 
   * Once the <code>wall</code> is updated, wall listeners added to this plan will receive a
   * {@link WallListener#wallChanged(WallEvent) wallChanged} notification, with
   * an {@link WallEvent#getType() event type} equal to
   * {@link WallEvent.Type#UPDATE UPDATE}. 
   * If the wall attached to <code>wall</code> end point is attached itself
   * to <code>wall</code>, this wall will be detached from <code>wall</code>, 
   * and wall listeners will receive
   * {@link WallListener#wallChanged(WallEvent) wallChanged}
   * notification about this wall, with an {@link WallEvent#getType() event type} 
   * equal to {@link WallEvent.Type#UPDATE UPDATE}. 
   * <br>Caution : This method isn't thread safe.
   * @param wallAtEnd a wall or <code>null</code> to detach <code>wall</code>
   *          from any wall it was attached to before.
   */
  public void setWallAtEnd(Wall wall, Wall wallAtEnd) {
    detachJoinedWall(wall, wall.getWallAtEnd());    
    wall.setWallAtEnd(wallAtEnd);
    fireWallEvent(wall, WallEvent.Type.UPDATE);
  }

  /**
   * Detaches <code>joinedWall</code> from <code>wall</code>.
   */
  private void detachJoinedWall(Wall wall, Wall joinedWall) {
    // Detach the previously attached wall to wall in parameter
    if (joinedWall != null) {
      if (wall.equals(joinedWall.getWallAtStart())) {
        joinedWall.setWallAtStart(null);
        fireWallEvent(joinedWall, WallEvent.Type.UPDATE);
      } else if (wall.equals(joinedWall.getWallAtEnd())) {
        joinedWall.setWallAtEnd(null);
        fireWallEvent(joinedWall, WallEvent.Type.UPDATE);
      } 
    }
  }

  /**
   * Notifies all wall listeners added to this plan an event of 
   * a given <code>type</code>.
   */
  private void fireWallEvent(Wall wall, WallEvent.Type eventType) {
    if (!this.wallListeners.isEmpty()) {
      WallEvent wallEvent = new WallEvent(this, wall, eventType);
      for (WallListener listener : this.wallListeners) {
        listener.wallChanged(wallEvent);
      }
    }
  }
}
