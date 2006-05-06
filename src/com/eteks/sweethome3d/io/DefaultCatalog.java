/*
 * DefaultCatalog.java 7 avr. 2006
 * 
 * Copyright (c) 2006 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights
 * Reserved.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.eteks.sweethome3d.io;

import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.eteks.sweethome3d.model.Catalog;
import com.eteks.sweethome3d.model.Category;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.PieceOfFurniture;
import com.eteks.sweethome3d.tools.URLContent;

/**
 * Furniture default catalog read from localized resources.
 * @author Emmanuel Puybaret
 */
public class DefaultCatalog extends Catalog {
  public DefaultCatalog() {
    ResourceBundle resource = ResourceBundle.getBundle(
        "com.eteks.sweethome3d.io.resources.DefaultCatalog");
    for (int i = 1;; i++) {
      String name = null;
      try {
        name = resource.getString("name#" + i);
      } catch (MissingResourceException ex) {
        // Stop the loop when a key name# doesn't exist
        break;
      }
      String category = resource.getString("category#" + i);
      Content icon  = getContent(resource, "icon#" + i);
      Content model = getContent(resource, "model#" + i);
      float width = Float.parseFloat(
          resource.getString("width#" + i));
      float depth = Float.parseFloat(
          resource.getString("depth#" + i));
      float height = Float.parseFloat(
          resource.getString("height#" + i));
      boolean movable = Boolean.parseBoolean(
          resource.getString("movable#" + i));
      boolean doorOrWindow = Boolean.parseBoolean(
          resource.getString("doorOrWindow#" + i));

      add(new Category(category),
          new PieceOfFurniture(name, icon, model,
              width, depth, height, movable, doorOrWindow));
    }
  }
  
  /**
   * Returns a valid URLContent instance from the resource file value of key.
   * @param resource a resource bundle
   * @param key      the key of a resource file
   * @throws IllegalArgumentException if the file value doesn't match a valid resource.
   */
  private Content getContent(ResourceBundle resource, String key) {
    String file = resource.getString(key);
    URL url = getClass().getResource(file);
    if (url == null) {
      throw new IllegalArgumentException("Unknown resource " + file);
    } else {
      return new URLContent(url); 
    }
  }
}
