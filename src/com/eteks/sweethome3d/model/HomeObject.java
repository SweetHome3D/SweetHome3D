/*
 * HomeObject.java 08 Sept. 2016
 *
 * Sweet Home 3D, Copyright (c) 2016 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An object with data where users can stored their own properties.
 * @author Emmanuel Puybaret
 * @since 5.3
 */
public abstract class HomeObject implements Serializable, Cloneable {
  private static final long serialVersionUID = 1L;
  
  private Map<String, String> properties;
  
  /**
   * Returns the value of the property <code>name</code> associated with this object.
   * @return the value of the property or <code>null</code> if it doesn't exist. 
   */
  public String getProperty(String name) {
    if (this.properties != null) {
      return this.properties.get(name);
    } else {
      return null;
    }
  }
  
  /**
   * Sets a property associated with this object.
   * @param name   the name of the property to set
   * @param value  the new value of the property 
   */
  public void setProperty(String name, String value) {
    if (value == null) {
      if (this.properties != null && this.properties.containsKey(name)) {
        this.properties.remove(name);
        if (this.properties.size() == 0) {
          this.properties = null;
        }
      }
    } else {
      if (this.properties == null) {
        // Create properties map on the fly with a singleton map first
        this.properties = Collections.singletonMap(name, value); 
      } else {
        if (this.properties.size() == 1) {
          // Then a HashMap if the user needs more than a property
          this.properties = new HashMap<String, String>(this.properties);
        }
        this.properties.put(name, value);
      }
    }
  }
  
  /**
   * Returns the property names.
   * @return a collection of all the names of the properties set with {@link #setProperty(String, String) setProperty} 
   */
  public Collection<String> getPropertyNames() {
    if (this.properties != null) {
      return this.properties.keySet();
    } else {
      return Collections.emptySet();
    }
  }

  /**
   * Returns a clone of this object.
   */
  @Override
  public HomeObject clone() {
    try {
      HomeObject clone = (HomeObject)super.clone();
      if (this.properties != null) {
        clone.properties = clone.properties.size() == 1 
            ? Collections.singletonMap(this.properties.keySet().iterator().next(), this.properties.values().iterator().next())
            : new HashMap<String, String>(this.properties);
      }
      return clone;
    } catch (CloneNotSupportedException ex) {
      throw new IllegalStateException("Super class isn't cloneable"); 
    }
  }
}
