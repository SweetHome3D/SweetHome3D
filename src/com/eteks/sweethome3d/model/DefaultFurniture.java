/*
 * DefaultFurniture.java 7 avr. 2006
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
package com.eteks.sweethome3d.model;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Default furniture.
 * @author Emmanuel Puybaret
 */
public class DefaultFurniture {
  private static DefaultFurniture instance;
  private List<PieceOfFurniture>  furniture;

  private DefaultFurniture() {
    this.furniture = new ArrayList<PieceOfFurniture>();
    ResourceBundle resource = ResourceBundle.getBundle(
        "com.eteks.sweethome3d.model.resources.DefaultFurniture");
    for (int i = 1; ; i++) {
      String name = null;
      try {
        name = resource.getString("name#" + i);
      } catch (MissingResourceException ex) {
        // Stop the loop when a key name# doesn't exist  
        break;
      }
      String category = resource.getString("category#" + i);
      URL iconURL = getClass().getResource(resource.getString("iconURL#" + i));
      URL modelURL = getClass().getResource(resource.getString("modelURL#" + i));
      float width = Float.parseFloat(resource.getString("width#" + i));
      float depth = Float.parseFloat(resource.getString("depth#" + i));
      float height = Float.parseFloat(resource.getString("height#" + i));
      boolean doorOrWindow = Boolean.parseBoolean(resource.getString("doorOrWindow#" + i));

      this.furniture.add(new PieceOfFurniture(name, category, iconURL,
          modelURL, width, depth, height, doorOrWindow));
    }
  }

  /**
   * Returns the default furniture instance.
   */
  public static synchronized DefaultFurniture getInstance() {
    if (instance == null) {
      instance = new DefaultFurniture();
    }
    return instance;
  }

  /**
   * Returns the piece of furniture at index <code>i</code>. 
   * @param i a value between 0 and <code>size() - 1</code>
   * @return the piece of furniture at index <code>i</code>
   */
  public PieceOfFurniture get(int i) { 
    return this.furniture.get(i);
  }

  /**
   * Returns the count of default furniture.
   */
  public int size() {
    return this.furniture.size();
  }  
}
