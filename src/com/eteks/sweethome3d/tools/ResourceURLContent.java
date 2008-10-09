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

import java.net.MalformedURLException;
import java.net.URL;


/**
 * URL content read from a class resource.
 * @author Emmanuel Puybaret
 */
public class ResourceURLContent extends URLContent {
  private static final long serialVersionUID = 1L;

  private boolean multiPartResource;
  
  /**
   * Creates a content for <code>resourceName</code> relative to <code>resourceClass</code>.
   * @param resourceClass the class relative to the resource name to load
   * @param resourceName  the name of the resource
   * @throws IllegalArgumentException if the resource doesn't match a valid resource.
   */
  public ResourceURLContent(Class<?> resourceClass, 
                            String resourceName) {
    this(resourceClass, resourceName, false);
  }

  /**
   * Creates a content for <code>resourceName</code> relative to <code>resourceClass</code>.
   * @param resourceClass the class relative to the resource name to load
   * @param resourceName  the name of the resource
   * @param multiPartResource  if <code>true</code> then the resource is a multi part resource 
   *           stored in a directory with other required resources
   * @throws IllegalArgumentException if the resource doesn't match a valid resource.
   */
  public ResourceURLContent(Class<?> resourceClass,
                            String resourceName, 
                            boolean multiPartResource) {
    super(getClassResource(resourceClass, resourceName));
    if (getURL() == null) {
      throw new IllegalArgumentException("Unknown resource " + resourceName);
    }
    this.multiPartResource = multiPartResource;
  }
  
  private static final boolean isJava1dot5dot0_16 = 
      System.getProperty("java.version").startsWith("1.5.0_16"); 
  
  /**
   * Returns the URL of the given resource relative to <code>resourceClass</code>.
   */
  private static URL getClassResource(Class<?> resourceClass,
                                      String resourceName) {
    URL defaultUrl = resourceClass.getResource(resourceName);
    // Fix for bug #6746185
    // http://bugs.sun.com/view_bug.do?bug_id=6746185
    if (isJava1dot5dot0_16
        && defaultUrl != null
        && "jar".equalsIgnoreCase(defaultUrl.getProtocol())) {
      String defaultUrlExternalForm = defaultUrl.toExternalForm();
      if (defaultUrl.toExternalForm().indexOf("!/") == -1) {
        String fixedUrl = "jar:" 
          + resourceClass.getProtectionDomain().getCodeSource().getLocation().toExternalForm() 
          + "!/" + defaultUrl.getPath();
        
        if (!fixedUrl.equals(defaultUrlExternalForm)) {
          try {
            return new URL(fixedUrl);
          } catch (MalformedURLException ex) {
            // Too bad: keep defaultUrl
          } 
        }
      }
    }
    return defaultUrl;
  }

  /**
   * Creates a content for <code>resourceUrl</code> 
   * @param url  the URL of the resource
   */
  public ResourceURLContent(URL url, boolean multiPartResource) {
    super(url);
    this.multiPartResource = multiPartResource;
  }

  /**
   * Returns <code>true</code> if the resource is a multi part resource stored 
   * in a directory with other required resources.
   */
  public boolean isMultiPartResource() {
    return this.multiPartResource;
  }
}
