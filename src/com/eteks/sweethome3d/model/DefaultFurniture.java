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
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Default furniture.
 * @author Emmanuel Puybaret
 */
public class DefaultFurniture {
  private static DefaultFurniture            instance;
  private Map<String, Set<PieceOfFurniture>> furniture;

  private DefaultFurniture() {
    this.furniture = new TreeMap<String,Set<PieceOfFurniture>> (Collator.getInstance());   
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

      addPieceOfFurniture(new PieceOfFurniture(name, category, iconURL,
          modelURL, width, depth, height, doorOrWindow));
          
    } 
  }
  
  /**
   * Adds <code>piece</code> to <code>furniture</code> map.
   * @param piece  a piece of default furniture.
   */
  private void addPieceOfFurniture(PieceOfFurniture piece) {
    String category = piece.getCategory();
    Set<PieceOfFurniture> categoryFurniture = furniture.get(category);
    // If category doesn't exist yet, create a new entry in sortedFurniture
    if (categoryFurniture == null) {
      final Collator namesComparator = Collator.getInstance();
      categoryFurniture = new TreeSet<PieceOfFurniture> (new Comparator<PieceOfFurniture> () {
        public int compare(PieceOfFurniture piece1, PieceOfFurniture piece2) {
          return namesComparator.compare (piece1.getName(), piece2.getName());
        }
      });
      furniture.put(category, categoryFurniture);        
    }
    // Add current piece of furniture to category set
    categoryFurniture.add(piece);
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
   * Returns an unmodifiable ordered set of default furniture categories.
   */
  public Set<String> getCategories() {
    Set<String> categories = this.furniture.keySet();
    return Collections.unmodifiableSet(categories);
  }

  /**
   * Returns an unmodifiable ordered set of <code>category</code> funiture. 
   */
  public Set<PieceOfFurniture> getFurniture(String category) {
    Set<PieceOfFurniture> categoryFurniture = this.furniture.get(category);
    if (categoryFurniture != null)
      return Collections.unmodifiableSet(categoryFurniture);
    else
      throw new IllegalArgumentException("Unknown category " + category);
  }
}
