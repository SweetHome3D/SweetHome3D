/*
 * Library.java 18 janv. 2013
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
package com.eteks.sweethome3d.model;

/**
 * A library able to provide additional capabilities to Sweet Home 3D.  
 * @author Emmanuel Puybaret
 * @since 4.0
 */
public interface Library {
  /**
   * Returns the location where this library is stored.
   */
  public String getLocation();
  
  /**
   * Returns the id of this library.
   */
  public String getId();
  
  /**
   * Returns the type of this library.
   */
  public String getType();
  
  /**
   * Returns the name of this library.
   */
  public String getName();

  /**
   * Returns the description of this library.
   */
  public String getDescription();

  /**
   * Returns the version of this library.
   */
  public String getVersion();

  /**
   * Returns the license of this library.
   */
  public String getLicense();

  /**
   * Returns the provider of this library.
   */
  public String getProvider();
}
