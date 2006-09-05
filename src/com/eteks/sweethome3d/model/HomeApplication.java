/*
 * HomeApplication.java 1 sept. 2006
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
 * Application managing a list of homes diplayed at screen.
 * @author Emmanuel Puybaret
 */
public abstract class HomeApplication {
  private List<Home> homes = new ArrayList<Home>();
  private List<HomeListener> homeListeners = new ArrayList<HomeListener>();

  /**
   * Adds the home <code>listener</code> in parameter to this application.
   */
  public void addHomeListener(HomeListener listener) {
    this.homeListeners.add(listener);
  }
  
  /**
   * Removes the home <code>listener</code> in parameter from this application.
   */
  public void removeHomeListener(HomeListener listener) {
    this.homeListeners.remove(listener);
  } 

  /**
   * Returns an unmodifiable collection of the homes of this application.
   */
  public List<Home> getHomes() {
    return Collections.unmodifiableList(this.homes);
  }

  /**
   * Adds a given <code>home</code> to the homes list of this application.
   * Once the <code>home</code> is added, home listeners added 
   * to this application will receive a
   * {@link HomeListener#homeChanged(HomeEvent) homeChanged}
   * notification, with an {@link HomeEvent#getType() event type} 
   * equal to {@link HomeEvent.Type#ADD ADD}. 
   */
  public void addHome(Home home) {
    this.homes = new ArrayList<Home>(this.homes);
    this.homes.add(home);
    fireHomeEvent(home, HomeEvent.Type.ADD);
  }

  /**
   * Removes a given <code>home</code> from the homes list  of this application.
   * Once the <code>home</code> is removed, home listeners added 
   * to this application will receive a
   * {@link HomeListener#homeChanged(HomeEvent) homeChanged}
   * notification, with an {@link HomeEvent#getType() event type} 
   * equal to {@link HomeEvent.Type#DELETE DELETE}.
   */
  public void deleteHome(Home home) {
    this.homes = new ArrayList<Home>(this.homes);
    this.homes.remove(home);
    fireHomeEvent(home, HomeEvent.Type.DELETE);
  }
  
  /**
   * Notifies all home listeners added to this application an event of 
   * a given type.
   */
  private void fireHomeEvent(Home home, HomeEvent.Type eventType) {
    if (!this.homeListeners.isEmpty()) {
      HomeEvent homeEvent = new HomeEvent(this, home, eventType);
      // Work on a copy of homeListeners to ensure a listener 
      // can modify safely listeners list
      HomeListener [] listeners = this.homeListeners.
        toArray(new HomeListener [this.homeListeners.size()]);
      for (HomeListener listener : listeners) {
        listener.homeChanged(homeEvent);
      }
    }
  }

  /**
   * Returns a recorder able to write and read homes.
   */
  public abstract HomeRecorder getHomeRecorder();

  /**
   * Returns user preferences.
   */
  public abstract UserPreferences getUserPreferences();
}
