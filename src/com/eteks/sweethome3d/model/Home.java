/*
 * Home.java 15 mai 2006
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
 * The home managed by the application with its furniture.
 * @author Emmanuel Puybaret
 */
public class Home {
  private List<HomePieceOfFurniture> furniture;
  private List<FurnitureListener>    furnitureListeners;
  private Collection<Wall>           walls;
  private List<WallListener>         wallListeners;

  /**
   * Creates a home with no furniture.
   */
  public Home() {
    this.furniture = new ArrayList<HomePieceOfFurniture>();
    this.furnitureListeners = new ArrayList<FurnitureListener>();
    this.walls = new ArrayList<Wall>();
    this.wallListeners = new ArrayList<WallListener>();
  }

  /**
   * Adds the furniture <code>listener</code> in parameter to this home.
   * <br>Caution : This method isn't thread safe.
   */
  public void addFurnitureListener(FurnitureListener listener) {
    this.furnitureListeners.add(listener);
  }

  /**
   * Removes the furniture <code>listener</code> in parameter from this home.
   * <br>Caution : This method isn't thread safe.
   */
  public void removeFurnitureListener(FurnitureListener listener) {
    this.furnitureListeners.remove(listener);
  }

  /**
   * Returns an unmodifiable list of the furniture managed by this home.
   * @return the furniture in the order they were
   *         {@link #add(HomePieceOfFurniture) added}.
   */
  public List<HomePieceOfFurniture> getFurniture() {
    return Collections.unmodifiableList(this.furniture);
  }

  /**
   * Adds a <code>piece</code> in parameter at a given <code>index</code>.
   * Once the <code>piece</code> is added, all furniture listeners added to this home will receive a
   * {@link FurnitureListener#pieceOfFurnitureAdded(FurnitureEvent) pieceOfFurnitureAdded}
   * notification.
   * <br>Caution : This method isn't thread safe.
   */
  public void add(HomePieceOfFurniture piece, int index) {
    this.furniture.add(index, piece);
    if (!this.furnitureListeners.isEmpty()) {
      FurnitureEvent furnitureEvent = new FurnitureEvent(this, piece);
      for (FurnitureListener listener : this.furnitureListeners) {
        listener.pieceOfFurnitureAdded(furnitureEvent);
      }
    }
  }

  /**
   * Removes a given <code>piece</code> of furniture from this home.
   * Once the <code>piece</code> is removed, all furniture listeners added to this home will receive a
   * {@link FurnitureListener#pieceOfFurnitureDeleted(FurnitureEvent) pieceOfFurnitureDeleted}
   * notification.
   * <br>Caution : This method isn't thread safe.
   */
  public void delete(HomePieceOfFurniture piece) {
    this.furniture.remove(piece);
    if (!this.furnitureListeners.isEmpty()) {
      FurnitureEvent furnitureEvent = new FurnitureEvent(this, piece);
      for (FurnitureListener listener : this.furnitureListeners) {
        listener.pieceOfFurnitureDeleted(furnitureEvent);
      }
    }
  }

  /**
   * Adds the wall <code>listener</code> in parameter to this home.
   * <br>Caution : This method isn't thread safe.
   */
  public void addWallListener(WallListener listener) {
    this.wallListeners.add(listener);
  }
  
  /**
   * Removes the wall <code>listener</code> in parameter from this home.
   * <br>Caution : This method isn't thread safe.
   */
  public void removeWallListener(WallListener listener) {
    this.wallListeners.remove(listener); 
  } 

  /**
   * Returns an unmodifiable collection of the walls of this home.
   */
  public Collection<Wall> getWalls() {
    return Collections.unmodifiableCollection(this.walls);
  }

  /**
   * Adds a given <code>wall</code> to the set of walls of this home.
   * Once the <code>wall</code> is added, all wall listeners added to this home will receive a
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
   * Removes a given <code>wall</code> from the set of walls of this home.
   * Once the <code>wall</code> is removed, all wall listeners added to this home will receive a
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
    this.walls.remove(wall);
    fireWallEvent(wall, WallEvent.Type.DELETE);
    // Detach any other wall attached to wall
    for (Wall otherWall : getWalls()) {
      if (wall.equals(otherWall.getWallAtStart())) {
        setWallAtStart(otherWall, null);
      }
      if (wall.equals(otherWall.getWallAtEnd())) {
        setWallAtEnd(otherWall, null);
      }
    }
  }

  /**
   * Moves <code>wall</code> start point of (<code>dx</code>, <code>dy</code>) pixels.
   * Once the <code>wall</code> is updated, all wall listeners added to this home will receive a
   * {@link WallListener#wallChanged(WallEvent) wallChanged}
   * notification, with an {@link WallEvent#getType() event type} 
   * equal to {@link WallEvent.Type#UPDATE UPDATE}. 
   * If the wall attached to <code>wall</code> start point is attached itself
   * to <code>wall</code>, wall listeners will receive
   * {@link WallListener#wallChanged(WallEvent) wallChanged}
   * notification about this wall, with an {@link WallEvent#getType() event type} 
   * equal to {@link WallEvent.Type#UPDATE UPDATE}. 
   * <br>Caution : This method isn't thread safe.
   */
  public void moveWallStartPoint(Wall wall, float dx, float dy) {
    moveWallPoint(wall, dx, dy, wall.getStartPoint());
    Wall wallAtStart = wall.getWallAtStart();
    if (wallAtStart != null
        && (wall.equals(wallAtStart.getWallAtStart())
            || wall.equals(wallAtStart.getWallAtEnd()))) {
      fireWallEvent(wallAtStart, WallEvent.Type.UPDATE);
    }
  }

  /**
   * Moves <code>wall</code> end point of (<code>dx</code>, <code>dy</code>) pixels.
   * Once the <code>wall</code> is updated, all wall listeners added to this home will receive a
   * {@link WallListener#wallChanged(WallEvent) wallChanged}
   * notification, with an {@link WallEvent#getType() event type} 
   * equal to {@link WallEvent.Type#UPDATE UPDATE}. 
   * If the wall attached to <code>wall</code> end point is attached itself
   * to <code>wall</code>, wall listeners will receive
   * {@link WallListener#wallChanged(WallEvent) wallChanged}
   * notification about this wall, with an {@link WallEvent#getType() event type} 
   * equal to {@link WallEvent.Type#UPDATE UPDATE}. 
   * <br>Caution : This method isn't thread safe.
   */
  public void moveWallEndPoint(Wall wall, float dx, float dy) {
    moveWallPoint(wall, dx, dy, wall.getStartPoint());
    Wall wallAtEnd = wall.getWallAtEnd();
    if (wallAtEnd != null
        && (wall.equals(wallAtEnd.getWallAtStart())
            || wall.equals(wallAtEnd.getWallAtEnd()))) {
      fireWallEvent(wallAtEnd, WallEvent.Type.UPDATE);
    }
  }

  private void moveWallPoint(Wall wall, float dx, float dy, Point point) {
    point.setX(point.getX() + dx);
    point.setY(point.getY() + dy);
    fireWallEvent(wall, WallEvent.Type.UPDATE);
  }

  /**
   * Sets the wall at start of <code>wall</code> as <code>wallAtEnd</code>. 
   * Once the <code>wall</code> is updated, all wall listeners added to this home will receive a
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
    Wall oldWallAtStart = wall.getWallAtStart();
    wall.setWallAtStart(wallAtStart);
    fireWallEvent(wall, WallEvent.Type.UPDATE);
    // Detach the previously attached wall to wall in parameter
    if (oldWallAtStart != null) {
      if (wall.equals(oldWallAtStart.getWallAtStart())) {
        setWallAtStart(oldWallAtStart, null);
      } else if (wall.equals(oldWallAtStart.getWallAtEnd())) {
        setWallAtEnd(oldWallAtStart, null);
      } 
    }    
  }

  /**
   * Sets the wall at end of <code>wall</code> as <code>wallAtEnd</code>. 
   * Once the <code>wall</code> is updated, all wall listeners added to this home will receive a
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
    Wall oldWallAtEnd = wall.getWallAtEnd();
    wall.setWallAtEnd(wallAtEnd);
    fireWallEvent(wall, WallEvent.Type.UPDATE);
    // Detach the previously attached wall to wall in parameter
    if (oldWallAtEnd != null) {
      if (wall.equals(oldWallAtEnd.getWallAtStart())) {
        setWallAtStart(oldWallAtEnd, null);
      } else if (wall.equals(oldWallAtEnd.getWallAtEnd())) {
        setWallAtEnd(oldWallAtEnd, null);
      } 
    }    
  }

  /**
   * Notifies all wall listeners added to this home an event of 
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
