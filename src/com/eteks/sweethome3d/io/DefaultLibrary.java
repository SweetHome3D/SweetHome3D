/*
 * DefaultLibrary.java 18 janv. 2013
 *
 * Sweet Home 3D, Copyright (c) 2013 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
package com.eteks.sweethome3d.io;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.eteks.sweethome3d.model.Library;

/**
 * Basic implementation of an immutable library initialized from the properties of a resource bundle.
 * @author Emmanuel Puybaret
 * @since 4.0  
 */
class DefaultLibrary implements Library {
  private static final String ID          = "id"; 
  private static final String NAME        = "name"; 
  private static final String DESCRIPTION = "description"; 
  private static final String VERSION     = "version"; 
  private static final String LICENSE     = "license"; 
  private static final String PROVIDER    = "provider"; 
  
  private final String location;
  private final String type;
  private final String id;
  private final String name;
  private final String description;
  private final String version;
  private final String license; 
  private final String provider;

  /**
   * Initializes a library from the given parameters.
   */
  public DefaultLibrary(String location, String type,  
                        String id, String name, String description, String version, String license,
                        String provider) {
    this.location = location;
    this.type = type;
    this.id = id;
    this.name = name;
    this.description = description;
    this.version = version;
    this.license = license;
    this.provider = provider;
  }
  
  /**
   * Initializes a library from the values of the <code>id</code>, <code>name</code>, 
   * <code>description</code>, <code>version</code>, <code>license</code> and 
   * <code>provider</code> keys in the given resource bundle.
   */
  public DefaultLibrary(String location, String type, ResourceBundle resource) {
    this.location = location;
    this.type = type;
    this.id = getOptionalString(resource, ID);
    this.name = getOptionalString(resource, NAME);
    this.description = getOptionalString(resource, DESCRIPTION);
    this.version = getOptionalString(resource, VERSION);
    this.license = getOptionalString(resource, LICENSE);
    this.provider = getOptionalString(resource, PROVIDER);
  }

  private String getOptionalString(ResourceBundle resource, String propertyKey) {
    try {
      return resource.getString(propertyKey);
    } catch (MissingResourceException ex) {
      return null;
    }
  }

  /**
   * Returns the location where this library is stored.
   */
  public String getLocation() {
    return this.location;
  }
  
  /**
   * Returns the id of this library.
   */
  public String getId() {
    return this.id;
  }
  
  /**
   * Returns the type of this library.
   */
  public String getType() {
    return this.type;
  }
  
  /**
   * Returns the name of this library.
   */
  public String getName() {
    return this.name;
  }

  /**
   * Returns the description of this library.
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * Returns the version of this library.
   */
  public String getVersion() {
    return this.version;
  }

  /**
   * Returns the license of this library.
   */
  public String getLicense() {
    return this.license;
  }

  /**
   * Returns the provider of this library.
   */
  public String getProvider() {
    return this.provider;
  }
}
