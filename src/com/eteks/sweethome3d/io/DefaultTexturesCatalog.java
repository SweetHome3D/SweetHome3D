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

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.eteks.sweethome3d.model.CatalogTexture;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.IllegalHomonymException;
import com.eteks.sweethome3d.model.TexturesCatalog;
import com.eteks.sweethome3d.model.TexturesCategory;
import com.eteks.sweethome3d.tools.ResourceURLContent;
import com.eteks.sweethome3d.tools.URLContent;

/**
 * Textures default catalog read from localized resources.
 * @author Emmanuel Puybaret
 */
public class DefaultTexturesCatalog extends TexturesCatalog {
  /**
   * The keys of the properties values read in <code>.properties</code> files.
   */
  public enum PropertyKey {
    /**
     * The key for the name of a texture (mandatory).
     */
    NAME("name"),
    /**
     * The key for the category's name of a texture (mandatory).
     * A new category with this name will be created if it doesn't exist.
     */
    CATEGORY("category"),
    /**
     * The key for the image file of a texture (mandatory). 
     * This image file can be either the path to an image relative to classpath
     * or an absolute URL. 
     */
    IMAGE("image"),
    /**
     * The key for the width in centimeters of a texture (mandatory).
     */
    WIDTH("width"),
    /**
     * The key for the height in centimeters of a texture (mandatory).
     */
    HEIGHT("height");
    
    private String keyPrefix;

    private PropertyKey(String keyPrefix) {
      this.keyPrefix = keyPrefix;
    }
    
    /**
     * Returns the key for the piece property of the given index.
     */
    public String getKey(int textureIndex) {
      return keyPrefix + "#" + textureIndex;
    }
  }

  private static final String PLUGIN_TEXTURES_CATALOG_FAMILY = "PluginTexturesCatalog";

  private static final String HOMONYM_TEXTURE_FORMAT = "%s -%d-";

  /**
   * Creates a default textures catalog read from resources.
   */
  public DefaultTexturesCatalog() {
    this((File)null);
  }
  
  /**
   * Creates a default textures catalog read from resources and   
   * plugin textures folder if <code>texturesPluginFolder</code> isn't <code>null</code>.
   */
  public DefaultTexturesCatalog(File texturesPluginFolder) {
    Map<TexturesCategory, Map<CatalogTexture, Integer>> textureHomonymsCounter = 
        new HashMap<TexturesCategory, Map<CatalogTexture,Integer>>();
    ResourceBundle resource;
    try {
      // Try to load DefaultTexturesCatalog property file from classpath 
      resource = ResourceBundle.getBundle(DefaultTexturesCatalog.class.getName());
    } catch (MissingResourceException ex) {
      // Ignore texture catalog
      resource = null;
    }
    readTextures(resource, null, textureHomonymsCounter);
    
    if (texturesPluginFolder != null) {
      // Try to load sh3t files from plugin folder
      File [] texturesFiles = texturesPluginFolder.listFiles(new FileFilter () {
        public boolean accept(File pathname) {
          return pathname.isFile();
        }
      });
      
      if (texturesFiles != null) {
        // Treat textures files in reverse order so file named with a date will be taken into account 
        // from most recent to least recent
        Arrays.sort(texturesFiles, Collections.reverseOrder());
        for (File texturesFile : texturesFiles) {
          try {
            // Try to load Furniture property file from current file  
            URL texturesUrl = texturesFile.toURI().toURL();
            readTextures(ResourceBundle.getBundle(PLUGIN_TEXTURES_CATALOG_FAMILY, Locale.getDefault(), 
                                                  new URLClassLoader(new URL [] {texturesUrl})), 
                texturesUrl, textureHomonymsCounter);
          } catch (MalformedURLException ex) {
            // Ignore file
          } catch (MissingResourceException ex) {
            // Ignore malformed plugin textures catalog
          } catch (IllegalArgumentException ex) {
            // Ignore malformed plugin textures catalog
          }
        }
      }
    }
  }
  
  /**
   * Creates a default textures catalog read from resources in the given URLs.
   */
  public DefaultTexturesCatalog(URL [] pluginTexturesCatalogUrls) {
    Map<TexturesCategory, Map<CatalogTexture, Integer>> textureHomonymsCounter = 
        new HashMap<TexturesCategory, Map<CatalogTexture,Integer>>();
    
    for (URL pluginTextureCatalogUrl : pluginTexturesCatalogUrls) {
      try {
        // Try do load Furniture property file from current file  
        readTextures(ResourceBundle.getBundle(PLUGIN_TEXTURES_CATALOG_FAMILY, Locale.getDefault(), 
            new URLClassLoader(new URL [] {pluginTextureCatalogUrl})), 
            pluginTextureCatalogUrl, textureHomonymsCounter);
      } catch (MissingResourceException ex) {
        // Ignore malformed plugin furniture catalog
      }
    }
  }

  /**
   * Reads each texture described in <code>resource</code> bundle.
   * Resources described in texture properties will be loaded from <code>texturesUrl</code> 
   * if it isn't <code>null</code>. 
   */
  private void readTextures(ResourceBundle resource, 
                            URL texturesUrl,
                            Map<TexturesCategory, Map<CatalogTexture, Integer>> textureHomonymsCounter) {
    if (resource != null) {
      for (int index = 1;; index++) {
        String name = null;
        try {
          name = resource.getString(PropertyKey.NAME.getKey(index));
        } catch (MissingResourceException ex) {
          // Stop the loop when a key name# doesn't exist
          break;
        }
        String category = resource.getString(PropertyKey.CATEGORY.getKey(index));
        Content image  = getContent(resource, PropertyKey.IMAGE.getKey(index), texturesUrl);
        float width = Float.parseFloat(resource.getString(PropertyKey.WIDTH.getKey(index)));
        float height = Float.parseFloat(resource.getString(PropertyKey.HEIGHT.getKey(index)));
  
        add(new TexturesCategory(category),
            new CatalogTexture(name, image, width, height),
            textureHomonymsCounter);
      }
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
   * Returns a valid content instance from the resource file or URL value of key.
   * @param resource a resource bundle
   * @param contentKey the key of a resource file
   * @throws IllegalArgumentException if the file value doesn't match a valid resource or URL.
   */
  private Content getContent(ResourceBundle resource, 
                             String         contentKey,
                             URL            texturesUrl) {
    String contentFile = resource.getString(contentKey);
    try {
      // Try first to interpret contentFile as a URL
      return new URLContent(new URL(contentFile));
    } catch (MalformedURLException ex) {
      if (texturesUrl == null) {
        // Otherwise find if it's a resource
        return new ResourceURLContent(DefaultFurnitureCatalog.class, contentFile);
      } else {
        try {
          return new URLContent(new URL("jar:" + texturesUrl + "!" + contentFile));
        } catch (MalformedURLException ex2) {
          throw new IllegalArgumentException("Invalid URL", ex2);
        }
      }
    }
  }
}
