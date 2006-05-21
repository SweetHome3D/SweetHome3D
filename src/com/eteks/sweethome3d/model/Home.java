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
import java.util.Collections;
import java.util.List;

/**
 * The home managed by the application with its furniture.
 * @author Emmanuel Puybaret
 */
public class Home {
  private List<HomePieceOfFurniture> furniture;
  private List<HomeListener> furnitureListeners;

  /**
   * Creates a home with no furniture.
   */
  public Home() {
    this.furniture = new ArrayList<HomePieceOfFurniture>();
    this.furnitureListeners = new ArrayList<HomeListener>();
  }

  /**
   * Adds the <code>listener</code> in paramter to this home.
   * Caution : This method isn't thread safe.
   */
  public void addHomeListener(HomeListener listener) {
    furnitureListeners.add(listener);
  }

  /**
   * Removes the <code>listener</code> in paramter from this home.
   * Caution : This method isn't thread safe.
   */
  public void removeHomeListener(HomeListener listener) {
    furnitureListeners.remove(listener);
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
   * Adds the <code>piece</code> in parameter at a given <code>index</code>.
   * Once added, all listeners added to this home will receive a
   * {@link HomeListener#pieceOfFurnitureAdded(HomeEvent) pieceOfFurnitureAdded}
   * notification.
   */
  public void add(int index, HomePieceOfFurniture piece) {
    this.furniture.add(index, piece);
    if (furnitureListeners.size() > 0) {
      HomeEvent homeEvent = null;
      for (HomeListener listener : furnitureListeners) {
        listener.pieceOfFurnitureAdded(homeEvent);
      }
    }
  }

  /**
   * Removes a given <code>piece</code> of furniture from this home.
   * Once removed, all listeners added to this home will receive a
   * {@link HomeListener#pieceOfFurnitureDeleted(HomeEvent) pieceOfFurnitureDeleted}
   * notification.
   */
  public void delete(HomePieceOfFurniture piece) {
    this.furniture.remove(piece);
    if (furnitureListeners.size() > 0) {
      HomeEvent homeEvent = new HomeEvent(this, piece);
      for (HomeListener listener : furnitureListeners) {
        listener.pieceOfFurnitureDeleted(homeEvent);
      }
    }
  } 
}
