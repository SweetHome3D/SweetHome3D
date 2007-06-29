/*
 * UserPreferences.java 15 mai 2006
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * User preferences.
 * @author Emmanuel Puybaret
 */
public abstract class UserPreferences {
  public enum Property {UNIT, MAGNETISM_ENABLED, RULERS_VISIBLE,  
                        NEW_HOME_WALL_HEIGHT, NEW_WALL_THICKNESS, RECENT_HOMES}
  
  private Map<Property, List<WeakReference<PropertyChangeListener>>> propertiesListeners;

  /**
   * Unit used for dimensions.
   */
  public enum Unit {
    CENTIMETER, INCH;

    public static float centimerToInch(float length) {
      return length / 2.54f;
    }

    public static float centimerToFoot(float length) {
      return length / 2.54f / 12;
    }
    
    public static float inchToCentimer(float length) {
      return length * 2.54f;
    }
  }

  private Catalog      catalog;
  private Unit         unit;
  private boolean      magnetismEnabled = true;
  private boolean      rulersVisible    = true;
  private float        newWallThickness;
  private float        newHomeWallHeight;
  private List<String> recentHomes;

  public UserPreferences() {
    // PropertyChangeListener instances are stored in a map of WeakReference to each listener, 
    // because user preferences are in fact referenced by a static variable during all application life
    this.propertiesListeners = 
        new HashMap<Property, List<WeakReference<PropertyChangeListener>>>();
  }
  
  /**
   * Writes user preferences.
   * @throws RecorderException if user preferences couldn'y be saved.
   */
  public abstract void write() throws RecorderException;
  
  /**
   * Adds the <code>listener</code> in parameter to these preferences. 
   * Caution : listener is stored with a weak reference. 
   */
  public void addPropertyChangeListener(Property property, 
                                        PropertyChangeListener listener) {
    List<WeakReference<PropertyChangeListener>> propertyListeners =
        this.propertiesListeners.get(property);
    if (propertyListeners == null) {
      propertyListeners = new ArrayList<WeakReference<PropertyChangeListener>>();
      this.propertiesListeners.put(property, propertyListeners);
    }
    propertyListeners.add(new WeakReference<PropertyChangeListener>(listener));
  }

  /**
   * Removes the <code>listener</code> in parameter from these preferences.
   */
  public void removePropertyChangeListener(Property property, 
                                           PropertyChangeListener listener) {
    List<WeakReference<PropertyChangeListener>> propertyListeners =
        this.propertiesListeners.get(property);
    for (Iterator<WeakReference<PropertyChangeListener>> it = propertyListeners.iterator(); 
         it.hasNext(); ) {
      if (it.next().get() == listener) {
        it.remove();
      }
     }
  }

  /**
   * Returns the catalog.
   */
  public Catalog getCatalog() {
    return this.catalog;
  }

  protected void setCatalog(Catalog catalog) {
    this.catalog = catalog;
  }

  /**
   * Returns the unit currently in use.
   */
  public Unit getUnit() {
    return this.unit;
  }
  
  /**
   * Changes the unit currently in use, and notifies
   * listeners of this change. 
   * @param unit one of the values of Unit.
   */
  public void setUnit(Unit unit) {
    if (this.unit != unit) {
      Unit oldUnit = this.unit;
      this.unit = unit;
      firePropertyChange(Property.UNIT, oldUnit, unit);
    }
  }

  /**
   * Returns <code>true</code> if magnetism is enabled.
   * @return <code>true</code> by default.
   */
  public boolean isMagnetismEnabled() {
    return this.magnetismEnabled;
  }

  /**
   * Sets whether magnetism is enabled or not, and notifies
   * listeners of this change. 
   * @param magnetismEnabled <code>true</code> if magnetism is enabled,
   *          <code>false</code> otherwise.
   */
  public void setMagnetismEnabled(boolean magnetismEnabled) {
    if (this.magnetismEnabled != magnetismEnabled) {
      this.magnetismEnabled = magnetismEnabled;
      firePropertyChange(Property.MAGNETISM_ENABLED, 
          !magnetismEnabled, magnetismEnabled);
    }
  }

  /**
   * Returns <code>true</code> if rulers are visible.
   * @return <code>true</code> by default.
   */
  public boolean isRulersVisible() {
    return this.rulersVisible;
  }

  /**
   * Sets whether rulers are visible or not, and notifies
   * listeners of this change. 
   * @param rulersVisible <code>true</code> if rulers are visible,
   *          <code>false</code> otherwise.
   */
  public void setRulersVisible(boolean rulersVisible) {
    if (this.rulersVisible != rulersVisible) {
      this.rulersVisible = rulersVisible;
      firePropertyChange(Property.RULERS_VISIBLE, 
          !rulersVisible, rulersVisible);
    }
  }

  /**
   * Returns default thickness of new walls in home. 
   */
  public float getNewWallThickness() {
    return this.newWallThickness;
  }

  /**
   * Sets default thickness of new walls in home, and notifies
   * listeners of this change.  
   */
  public void setNewWallThickness(float newWallThickness) {
    if (this.newWallThickness != newWallThickness) {
      float oldDefaultThickness = this.newWallThickness;
      this.newWallThickness = newWallThickness;
      firePropertyChange(Property.NEW_WALL_THICKNESS, 
          oldDefaultThickness, newWallThickness);
    }
  }

  /**
   * Returns default wall height of new home walls. 
   */
  public float getNewHomeWallHeight() {
    return this.newHomeWallHeight;
  }

  /**
   * Sets default wall height of new home walls, and notifies
   * listeners of this change. 
   */
  public void setNewHomeWallHeight(float newHomeWallHeight) {
    if (this.newHomeWallHeight != newHomeWallHeight) {
      float oldHomeWallHeight = this.newHomeWallHeight;
      this.newHomeWallHeight = newHomeWallHeight;
      firePropertyChange(Property.NEW_HOME_WALL_HEIGHT, 
          oldHomeWallHeight, newHomeWallHeight);
    }
  }
  
  
  
  /**
   * Returns an unmodifiable list of the recent homes.
   */
  public List<String> getRecentHomes() {
    return Collections.unmodifiableList(this.recentHomes);
  }

  /**
   * Sets the recent homes list and notifies listeners of this change.
   */
  public void setRecentHomes(List<String> recentHomes) {
    if (!recentHomes.equals(this.recentHomes)) {
      List<String> oldRecentHomes = this.recentHomes;
      this.recentHomes = new ArrayList<String>(recentHomes);
      firePropertyChange(Property.RECENT_HOMES, 
          oldRecentHomes, getRecentHomes());
    }
  }

  @SuppressWarnings("unchecked")
  private void firePropertyChange(Property property, Object oldValue, Object newValue) {
    List<WeakReference<PropertyChangeListener>> propertyListeners =
        this.propertiesListeners.get(property);
    if (propertyListeners != null && !propertyListeners.isEmpty()) {
      PropertyChangeEvent propertyChangeEvent = 
          new PropertyChangeEvent(this, property.toString(), oldValue, newValue);
      // Work on a copy of propertiesListeners to ensure a listener 
      // can modify safely listeners list
      WeakReference<PropertyChangeListener> [] listeners = propertyListeners.
          toArray(new WeakReference [propertyListeners.size()]);
      for (WeakReference<PropertyChangeListener> listenerReference : listeners) {
        PropertyChangeListener listener = listenerReference.get();
        if (listener != null) {
          listener.propertyChange(propertyChangeEvent);
        } else {
          removePropertyChangeListener(property, listener);
        }
      }
    }
  }
}
