/*
 * CollectionChangeSupport.java 28 oct. 2008
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
package com.eteks.sweethome3d.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A helper class for {@link CollectionListener CollectionListener} management.
 * <code>T</code> is the type of item stored in the collection.
 * @author Emmanuel Puybaret
 */
class CollectionChangeSupport<T> {
  private Object                      source;
  private List<CollectionListener<T>> collectionListeners;
  
  public CollectionChangeSupport(Object source) {
    this.source = source;
    this.collectionListeners = new ArrayList<CollectionListener<T>>(5);
  }
  
  /**
   * Adds the <code>listener</code> in parameter to the list of listeners that may be notified.
   */
  public void addCollectionListener(CollectionListener<T> listener) {
    this.collectionListeners.add(listener);
  }

  /**
   * Removes the <code>listener</code> in parameter to the list of listeners that may be notified.
   */
  public void removeCollectionListener(CollectionListener<T> listener) {
    this.collectionListeners.remove(listener);
  }

  /**
   * Fires a collection event about <code>item</code>.
   */
  public void fireCollectionChanged(T item, CollectionEvent.Type eventType) {
    fireCollectionChanged(item, -1, eventType);
  }

  /**
   * Fires a collection event about <code>item</code> at a given <code>index</code>.
   */
  @SuppressWarnings("unchecked")
  public void fireCollectionChanged(T item, int index, 
                                    CollectionEvent.Type eventType) {
    if (!this.collectionListeners.isEmpty()) {
      CollectionEvent<T> furnitureEvent = 
          new CollectionEvent<T>(this.source, item, index, eventType);
      // Work on a copy of collectionListeners to ensure a listener 
      // can modify safely listeners list
      CollectionListener<T> [] listeners = this.collectionListeners.
        toArray(new CollectionListener [this.collectionListeners.size()]);
      for (CollectionListener<T> listener : listeners) {
        listener.collectionChanged(furnitureEvent);
      }
    }
  }
}
