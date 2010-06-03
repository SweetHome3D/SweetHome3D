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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.eteks.sweethome3d.model.CatalogTexture;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.IllegalHomonymException;
import com.eteks.sweethome3d.model.TexturesCatalog;
import com.eteks.sweethome3d.model.TexturesCategory;
import com.eteks.sweethome3d.model.UserPreferences;
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
     * The key for the ID of a texture (optional). 
     * Two textures read in a texture catalog can't have the same ID
     * and the second one will be ignored.   
     */
    ID("id"),
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
     * or an absolute URL. It should be encoded in application/x-www-form-urlencoded format 
     * if needed.
     */
    IMAGE("image"),
    /**
     * The key for the width in centimeters of a texture (mandatory).
     */
    WIDTH("width"),
    /**
     * The key for the height in centimeters of a texture (mandatory).
     */
    HEIGHT("height"),
    /**
     * The key for the creator of a piece of furniture (optional).
     * By default, creator is <code>null</code>.
     */
    CREATOR("creator");

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
   * textures plugin folder if <code>texturesPluginFolder</code> isn't <code>null</code>.
   */
  public DefaultTexturesCatalog(File texturesPluginFolder) {
    this(null, texturesPluginFolder);
  }
  
  /**
   * Creates a default textures catalog read from resources and   
   * textures plugin folder if <code>texturesPluginFolder</code> isn't <code>null</code>.
   */
  public DefaultTexturesCatalog(final UserPreferences preferences, 
                                File texturesPluginFolder) {
    Map<TexturesCategory, Map<CatalogTexture, Integer>> textureHomonymsCounter = 
        new HashMap<TexturesCategory, Map<CatalogTexture,Integer>>();
    List<String> identifiedTextures = new ArrayList<String>();
    
    // Try to load com.eteks.sweethome3d.io.DefaultTexturesCatalog property file from classpath 
    final String defaultTexturesCatalogFamily = DefaultTexturesCatalog.class.getName();
    if (preferences != null) {
      // Adapt getLocalizedString to ResourceBundle
      ResourceBundle resource = new ResourceBundle() {
          @Override
          protected Object handleGetObject(String key) {
            try {
              return preferences.getLocalizedString(defaultTexturesCatalogFamily, key);
            } catch (IllegalArgumentException ex) {
              throw new MissingResourceException("Unknown key " + key, 
                  defaultTexturesCatalogFamily + "_" + Locale.getDefault(), key);
            }
          }
          
          @Override
          public Enumeration<String> getKeys() {
            // Not needed
            throw new UnsupportedOperationException();
          }
        };
      readTextures(resource, null, null, textureHomonymsCounter, identifiedTextures);
    } else {
      try {
        ResourceBundle resource = ResourceBundle.getBundle(defaultTexturesCatalogFamily);
        readTextures(resource, null, null, textureHomonymsCounter, identifiedTextures);
      } catch (MissingResourceException ex) {
        // Ignore texture catalog
      }
    }
    
    if (texturesPluginFolder != null) {
      // Try to load sh3t files from textures plugin folder
      File [] pluginTexturesCatalogFiles = texturesPluginFolder.listFiles(new FileFilter () {
        public boolean accept(File pathname) {
          return pathname.isFile();
        }
      });
      
      if (pluginTexturesCatalogFiles != null) {
        // Treat textures catalog files in reverse order so file named with a date will be taken into account 
        // from most recent to least recent
        Arrays.sort(pluginTexturesCatalogFiles, Collections.reverseOrder());
        for (File pluginTexturesCatalogFile : pluginTexturesCatalogFiles) {
          try {
            // Try to load the properties file describing textures catalog from current file  
            URL pluginTexturesCatalogUrl = pluginTexturesCatalogFile.toURI().toURL();
            readTextures(ResourceBundle.getBundle(PLUGIN_TEXTURES_CATALOG_FAMILY, Locale.getDefault(), 
                                                  new URLClassLoader(new URL [] {pluginTexturesCatalogUrl})), 
                pluginTexturesCatalogUrl, null, textureHomonymsCounter, identifiedTextures);
          } catch (MalformedURLException ex) {
            // Ignore file
          } catch (MissingResourceException ex) {
            // Ignore malformed textures catalog
          } catch (IllegalArgumentException ex) {
            // Ignore malformed textures catalog
          }
        }
      }
    }
  }
  
  /**
   * Creates a default textures catalog read only from resources in the given URLs.
   */
  public DefaultTexturesCatalog(URL [] pluginTexturesCatalogUrls) {
    this(pluginTexturesCatalogUrls, null);
  }
  
  /**
   * Creates a default textures catalog read only from resources in the given URLs.
   * Texture image URLs will built from <code>texturesResourcesUrlBase</code> if it isn't <code>null</code>.
   */
  public DefaultTexturesCatalog(URL [] pluginTexturesCatalogUrls,
                                URL    texturesResourcesUrlBase) {
    Map<TexturesCategory, Map<CatalogTexture, Integer>> textureHomonymsCounter = 
        new HashMap<TexturesCategory, Map<CatalogTexture,Integer>>();
    List<String> identifiedTextures = new ArrayList<String>();

    for (URL pluginTexturesCatalogUrl : pluginTexturesCatalogUrls) {
      try {
        // Try do load the properties file describing textures catalog from current file  
        readTextures(ResourceBundle.getBundle(PLUGIN_TEXTURES_CATALOG_FAMILY, Locale.getDefault(), 
            new URLClassLoader(new URL [] {pluginTexturesCatalogUrl})), 
            pluginTexturesCatalogUrl, texturesResourcesUrlBase, textureHomonymsCounter, identifiedTextures);
      } catch (MissingResourceException ex) {
        // Ignore malformed textures catalog
      } catch (IllegalArgumentException ex) {
        // Ignore malformed textures catalog
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
                            URL texturesResourcesUrlBase,
                            Map<TexturesCategory, Map<CatalogTexture, Integer>> textureHomonymsCounter,
                            List<String> identifiedTextures) {
    for (int index = 1;; index++) {
      String name = null;
      try {
        name = resource.getString(PropertyKey.NAME.getKey(index));
      } catch (MissingResourceException ex) {
        // Stop the loop when a key name# doesn't exist
        break;
      }
      String category = resource.getString(PropertyKey.CATEGORY.getKey(index));
      Content image  = getContent(resource, PropertyKey.IMAGE.getKey(index), 
          texturesUrl, texturesResourcesUrlBase);
      float width = Float.parseFloat(resource.getString(PropertyKey.WIDTH.getKey(index)));
      float height = Float.parseFloat(resource.getString(PropertyKey.HEIGHT.getKey(index)));
      String creator = getOptionalString(resource, PropertyKey.CREATOR.getKey(index));
      String id = getOptionalString(resource, PropertyKey.ID.getKey(index));

      CatalogTexture texture = new CatalogTexture(id, name, image, width, height, creator);
      if (texture.getId() != null) {
        // Take into account only texture that have an ID
        if (identifiedTextures.contains(texture.getId())) {
          continue;
        } else {
          // Add id to identifiedTextures to be sure that two textures with a same ID
          // won't be added twice to texture catalog (in case they are cited twice
          // in different texture properties files)
          identifiedTextures.add(texture.getId());
        }
      }

      add(new TexturesCategory(category), texture, textureHomonymsCounter);
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
   * @param texturesUrl the URL of the file containing the target resource if it's not <code>null</code> 
   * @param resourceUrlBase the URL used as a base to build the URL to content file  
   *            or <code>null</code> if it's read from current classpath or <code>texturesUrl</code>.
   * @throws IllegalArgumentException if the file value doesn't match a valid resource or URL.
   */
  private Content getContent(ResourceBundle resource, 
                             String         contentKey,
                             URL            texturesUrl,
                             URL            resourceUrlBase) {
    String contentFile = resource.getString(contentKey);
    try {
      // Try first to interpret contentFile as an absolute URL 
      // or an URL relative to resourceUrlBase if it's not null
      URL url;
      if (resourceUrlBase != null) {
        url = new URL(resourceUrlBase, contentFile);
      } else {
        url = new URL(contentFile);
      }
      return new URLContent(url);
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

  /**
   * Returns the value of <code>propertyKey</code> in <code>resource</code>, 
   * or <code>null</code> if the property doesn't exist.
   */
  private String getOptionalString(ResourceBundle resource, 
                                   String propertyKey) {
    try {
      return resource.getString(propertyKey);
    } catch (MissingResourceException ex) {
      return null;
    }
  }
}
