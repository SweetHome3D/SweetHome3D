/*
 * ResourceURLContent.java 9 juil. 2007
 *
 * Copyright (c) 2007 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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
package com.eteks.sweethome3d.tools;

/**
 * URL content read from a class resource.
 * @author Emmanuel Puybaret
 */
public class ResourceURLContent extends URLContent {
  private static final long serialVersionUID = 1L;

  private Class             resourceClass;
  private String            resourceName;
  private boolean           multiPartResource;
  
  /**
   * Creates a content for <code>resourceName</code> relative to <code>resourceClass</code>.
   * @param resourceClass the class to which the resource name is relative
   * @param resourceName  the name of the resource
   * @throws IllegalArgumentException if the resource doesn't match a valid resource.
   */
  public ResourceURLContent(Class resourceClass, String resourceName) {
    this(resourceClass, resourceName, false);    
  }

  /**
   * Creates a content for <code>resourceName</code> relative to <code>resourceClass</code>.
   * @param resourceClass the class to which the resource name is relative
   * @param resourceName  the name of the resource
   * @param multiPartResource  if <code>true</code> then the resource is a multi part resource 
   *           stored in a directory with other required resources
   * @throws IllegalArgumentException if the resource doesn't match a valid resource.
   */
  public ResourceURLContent(Class resourceClass, String resourceName, 
                            boolean multiPartResource) {
    super(resourceClass.getResource(resourceName));
    if (getURL() == null) {
      throw new IllegalArgumentException("Unknown resource " + resourceName);
    }
    this.resourceClass = resourceClass;
    this.resourceName = resourceName;    
    this.multiPartResource = multiPartResource;
  }

  /**
   * Returns the class to which the resource is relative.
   */
  public Class getResourceClass() {
    return this.resourceClass;
  }

  /**
   * Returns the resource name relative to resource class.
   */
  public String getResourceName() {
    return this.resourceName;
  }
  
  /**
   * Returns <code>true</code> if the resource is a multi part resource stored 
   * in a directory with other required resources.
   */
  public boolean isMultiPartResource() {
    return this.multiPartResource;
  }
}
