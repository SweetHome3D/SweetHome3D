/*
 * DefaultTexturesCatalog.java 5 oct. 2007
 * 
 * Copyright (c) 2007 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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

import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.eteks.sweethome3d.model.CatalogTexture;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.IllegalHomonymException;
import com.eteks.sweethome3d.model.TexturesCatalog;
import com.eteks.sweethome3d.model.TexturesCategory;
import com.eteks.sweethome3d.tools.ResourceURLContent;

/**
 * Textures default catalog read from localized resources.
 * @author Emmanuel Puybaret
 */
public class DefaultTexturesCatalog extends TexturesCatalog {
  private static final String NAME           = "name#";
  private static final String CATEGORY       = "category#";
  private static final String IMAGE          = "image#";
  private static final String WIDTH          = "width#";
  private static final String HEIGHT         = "height#";

  private static final String HOMONYM_TEXTURE_FORMAT = "%s -%d-";
  
  /**
   * Creates a default textures catalog read from resources.
   */
  public DefaultTexturesCatalog() {
    Map<TexturesCategory, Map<CatalogTexture, Integer>> textureHomonymsCounter = 
        new HashMap<TexturesCategory, Map<CatalogTexture,Integer>>();

    ResourceBundle resource = ResourceBundle.getBundle(
        DefaultTexturesCatalog.class.getName());
    for (int i = 1;; i++) {
      String name = null;
      try {
        name = resource.getString(NAME + i);
      } catch (MissingResourceException ex) {
        // Stop the loop when a key name# doesn't exist
        break;
      }
      String category = resource.getString(CATEGORY + i);
      Content image  = getContent(resource, IMAGE + i);
      float width = Float.parseFloat(resource.getString(WIDTH + i));
      float height = Float.parseFloat(resource.getString(HEIGHT + i));

      add(new TexturesCategory(category),
          new CatalogTexture(name, image, width, height),
          textureHomonymsCounter);
    }
  }
  
  /**
   * Adds a <code>piece</code> to its category in catalog. If <code>piece</code> has an homonym
   * in its category its name will be suffixed indicating its sequence.
   */
  private void add(TexturesCategory textureCategory,
                   CatalogTexture texture,
                   Map<TexturesCategory, Map<CatalogTexture, Integer>> textureHomonymsCounter) {
    try {        
      add(textureCategory, texture);
    } catch (IllegalHomonymException ex) {
      // Search the counter of piece name
      Map<CatalogTexture, Integer> categoryTextureHomonymsCounter = 
        textureHomonymsCounter.get(textureCategory);
      if (categoryTextureHomonymsCounter == null) {
        categoryTextureHomonymsCounter = new HashMap<CatalogTexture, Integer>();
        textureHomonymsCounter.put(textureCategory, categoryTextureHomonymsCounter);
      }
      Integer textureHomonymCounter = categoryTextureHomonymsCounter.get(texture);
      if (textureHomonymCounter == null) {
        textureHomonymCounter = 1;
      }
      categoryTextureHomonymsCounter.put(texture, ++textureHomonymCounter);
      // Try to add texture again to catalog with a suffix indicating its sequence
      texture = new CatalogTexture(String.format(HOMONYM_TEXTURE_FORMAT, texture.getName(), textureHomonymCounter), 
          texture.getImage(), texture.getWidth(), texture.getHeight());
      add(textureCategory, texture, textureHomonymsCounter);
    }
  }

  /**
   * Returns a valid content instance from the resource file value of key.
   * @param resource a resource bundle
   * @param key      the key of a resource file
   * @throws IllegalArgumentException if the file value doesn't match a valid resource.
   */
  private Content getContent(ResourceBundle resource, String key) {
    String file = resource.getString(key);
    return new ResourceURLContent(DefaultTexturesCatalog.class, file);
  }
}
