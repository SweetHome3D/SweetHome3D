/*
 * DefaultTexturesCatalog.java 5 oct. 2007
 * 
 * Sweet Home 3D, Copyright (c) 2007 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessControlException;
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
import com.eteks.sweethome3d.model.Library;
import com.eteks.sweethome3d.model.TexturesCatalog;
import com.eteks.sweethome3d.model.TexturesCategory;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.tools.ResourceURLContent;
import com.eteks.sweethome3d.tools.TemporaryURLContent;
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
     * The key for the creator of a texture (optional).
     * By default, creator is <code>null</code>.
     */
    CREATOR("creator");

    private String keyPrefix;

    private PropertyKey(String keyPrefix) {
      this.keyPrefix = keyPrefix;
    }
    
    /**
     * Returns the key for the texture property of the given index.
     */
    public String getKey(int textureIndex) {
      return keyPrefix + "#" + textureIndex;
    }
  }

  /**
   * The name of <code>.properties</code> family files in plugin textures catalog files. 
   */
  public static final String PLUGIN_TEXTURES_CATALOG_FAMILY = "PluginTexturesCatalog";

  private static final String ADDITIONAL_TEXTURES_CATALOG_FAMILY  = "AdditionalTexturesCatalog";

  private List<Library> libraries = new ArrayList<Library>();
  
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
    this(preferences, texturesPluginFolder == null ? null : new File [] {texturesPluginFolder});
  }
  
  /**
   * Creates a default textures catalog read from resources and   
   * textures plugin folders if <code>texturesPluginFolders</code> isn't <code>null</code>.
   */
  public DefaultTexturesCatalog(final UserPreferences preferences, 
                                File [] texturesPluginFolders) {
    List<String> identifiedTextures = new ArrayList<String>();

    readDefaultTexturesCatalogs(preferences, identifiedTextures);

    if (texturesPluginFolders != null) {
      for (File texturesPluginFolder : texturesPluginFolders) {
        // Try to load sh3t files from textures plugin folder
        File [] pluginTexturesCatalogFiles = texturesPluginFolder.listFiles(new FileFilter () {
          public boolean accept(File pathname) {
            return pathname.isFile();
          }
        });
        
        if (pluginTexturesCatalogFiles != null) {
          // Treat textures catalog files in reverse order of their version
          Arrays.sort(pluginTexturesCatalogFiles, Collections.reverseOrder(OperatingSystem.getFileVersionComparator()));
          for (File pluginTexturesCatalogFile : pluginTexturesCatalogFiles) {
            // Try to load the properties file describing textures catalog from current file  
            readPluginTexturesCatalog(pluginTexturesCatalogFile, identifiedTextures);
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
    List<String> identifiedTextures = new ArrayList<String>();
    try {
      SecurityManager securityManager = System.getSecurityManager();
      if (securityManager != null) {
        securityManager.checkCreateClassLoader();
      }

      for (URL pluginTexturesCatalogUrl : pluginTexturesCatalogUrls) {
        try {
          ResourceBundle resource = ResourceBundle.getBundle(PLUGIN_TEXTURES_CATALOG_FAMILY, Locale.getDefault(),
                  new URLClassLoader(new URL [] {pluginTexturesCatalogUrl}));
          this.libraries.add(0, new DefaultLibrary(pluginTexturesCatalogUrl.toExternalForm(), 
              UserPreferences.TEXTURES_LIBRARY_TYPE, resource));
          readTextures(resource, pluginTexturesCatalogUrl, texturesResourcesUrlBase, identifiedTextures);
        } catch (MissingResourceException ex) {
          // Ignore malformed textures catalog
        } catch (IllegalArgumentException ex) {
          // Ignore malformed textures catalog
        }
      }
    } catch (AccessControlException ex) {
      // Use only textures accessible through classpath
      ResourceBundle resource = ResourceBundle.getBundle(PLUGIN_TEXTURES_CATALOG_FAMILY, Locale.getDefault());
      readTextures(resource, null, texturesResourcesUrlBase, identifiedTextures);
    }
  }

  /**
   * Returns the furniture libraries at initialization.
   * @since 4.0 
   */
  public List<Library> getLibraries() {
    return Collections.unmodifiableList(this.libraries);
  }

  private static final Map<File,URL> pluginTexturesCatalogUrlUpdates = new HashMap<File,URL>(); 
  
  /**
   * Reads plug-in textures catalog from the <code>pluginTexturesCatalogFile</code> file. 
   */
  private void readPluginTexturesCatalog(File pluginTexturesCatalogFile,
                                         List<String> identifiedTextures) {
    try {
      URL pluginTexturesCatalogUrl = pluginTexturesCatalogFile.toURI().toURL();
      long urlModificationDate = pluginTexturesCatalogFile.lastModified();
      URL urlUpdate = pluginTexturesCatalogUrlUpdates.get(pluginTexturesCatalogFile);
      boolean modifiableUrl = pluginTexturesCatalogFile.canWrite();
      if (modifiableUrl
          && (urlUpdate == null 
              || urlUpdate.openConnection().getLastModified() < urlModificationDate)) {
        // Copy updated resource URL content to a temporary file to ensure textures used in home can safely 
        // reference any file of the catalog file even if its content is changed afterwards
        TemporaryURLContent contentCopy = TemporaryURLContent.copyToTemporaryURLContent(new URLContent(pluginTexturesCatalogUrl));
        URL temporaryTexturesCatalogUrl = contentCopy.getURL();
        pluginTexturesCatalogUrlUpdates.put(pluginTexturesCatalogFile, temporaryTexturesCatalogUrl);
        pluginTexturesCatalogUrl = temporaryTexturesCatalogUrl;
      } else if (urlUpdate != null) {
        pluginTexturesCatalogUrl = urlUpdate;
      }
      
      ResourceBundle resourceBundle = ResourceBundle.getBundle(PLUGIN_TEXTURES_CATALOG_FAMILY, Locale.getDefault(),
          new URLClassLoader(new URL [] {pluginTexturesCatalogUrl}));      
      this.libraries.add(0, new DefaultLibrary(pluginTexturesCatalogFile.getCanonicalPath(), 
          UserPreferences.TEXTURES_LIBRARY_TYPE, resourceBundle));
      readTextures(resourceBundle, pluginTexturesCatalogUrl, null, identifiedTextures);
    } catch (MissingResourceException ex) {
      // Ignore malformed textures catalog
    } catch (IllegalArgumentException ex) {
      // Ignore malformed textures catalog
    } catch (IOException ex) {
      // Ignore unaccessible catalog
    }
  }
  
  /**
   * Reads the default textures described in properties files accessible through classpath.
   */
  private void readDefaultTexturesCatalogs(final UserPreferences preferences,
                                           List<String> identifiedTextures) {
    // Try to load com.eteks.sweethome3d.io.DefaultTexturesCatalog property file from classpath 
    final String defaultTexturesCatalogFamily = DefaultTexturesCatalog.class.getName();
    readTexturesCatalog(defaultTexturesCatalogFamily, 
        preferences, identifiedTextures);

    // Try to load com.eteks.sweethome3d.io.AdditionalTexturesCatalog property file from classpath 
    String classPackage = defaultTexturesCatalogFamily.substring(0, defaultTexturesCatalogFamily.lastIndexOf("."));
    readTexturesCatalog(classPackage + "." + ADDITIONAL_TEXTURES_CATALOG_FAMILY, 
        preferences, identifiedTextures);
  }
  
  /**
   * Reads textures of a given catalog family from resources.
   */
  private void readTexturesCatalog(final String texturesCatalogFamily,
                                   final UserPreferences preferences,
                                   List<String> identifiedTextures) {
    ResourceBundle resource;
    if (preferences != null) {
      // Adapt getLocalizedString to ResourceBundle
      resource = new ResourceBundle() {
          @Override
          protected Object handleGetObject(String key) {
            try {
              return preferences.getLocalizedString(texturesCatalogFamily, key);
            } catch (IllegalArgumentException ex) {
              throw new MissingResourceException("Unknown key " + key, 
                  texturesCatalogFamily + "_" + Locale.getDefault(), key);
            }
          }
          
          @Override
          public Enumeration<String> getKeys() {
            // Not needed
            throw new UnsupportedOperationException();
          }
        };
    } else {
      try {
        resource = ResourceBundle.getBundle(texturesCatalogFamily);
      } catch (MissingResourceException ex) {
        return;
      }
    }
    readTextures(resource, null, null, identifiedTextures);
  }
  
  /**
   * Reads each texture described in <code>resource</code> bundle.
   * Resources described in texture properties will be loaded from <code>texturesUrl</code> 
   * if it isn't <code>null</code>. 
   */
  private void readTextures(ResourceBundle resource, 
                            URL texturesCatalogUrl,
                            URL texturesResourcesUrlBase,
                            List<String> identifiedTextures) {
    CatalogTexture texture;
    for (int i = 1; (texture = readTexture(resource, i, texturesCatalogUrl, texturesResourcesUrlBase)) != null; i++) {
      if (texture.getId() != null) {
        // Take into account only texture that have an ID
        if (identifiedTextures.contains(texture.getId())) {
          continue;
        } else {
          // Add id to identifiedTextures to be sure that two textures with a same ID
          // won't be added twice to textures catalog (in case they are cited twice
          // in different textures properties files)
          identifiedTextures.add(texture.getId());
        }
      }
      TexturesCategory textureCategory = readTexturesCategory(resource, i);
      add(textureCategory, texture);
    }
  }
  
  /**
   * Returns the texture at the given <code>index</code> of a 
   * localized <code>resource</code> bundle. 
   * @param resource             a resource bundle 
   * @param index                the index of the read texture
   * @param texturesCatalogUrl  the URL from which texture resources will be loaded 
   *            or <code>null</code> if it's read from current classpath.
   * @param texturesResourcesUrlBase the URL used as a base to build the URL to texture resources  
   *            or <code>null</code> if it's read from current classpath or <code>texturesCatalogUrl</code>
   * @return the read texture or <code>null</code> if the piece at the given index doesn't exist.
   * @throws MissingResourceException if mandatory keys are not defined.
   */
  /**
   * Reads each texture described in <code>resource</code> bundle.
   * Resources described in texture properties will be loaded from <code>texturesUrl</code> 
   * if it isn't <code>null</code>. 
   */
  protected CatalogTexture readTexture(ResourceBundle resource,
                                       int index,
                                       URL texturesUrl,
                                       URL texturesResourcesUrlBase) {
    String name = null;
    try {
      name = resource.getString(PropertyKey.NAME.getKey(index));
    } catch (MissingResourceException ex) {
      // Return null if key name# doesn't exist
      return null;
    }
    Content image  = getContent(resource, PropertyKey.IMAGE.getKey(index), 
        texturesUrl, texturesResourcesUrlBase);
    float width = Float.parseFloat(resource.getString(PropertyKey.WIDTH.getKey(index)));
    float height = Float.parseFloat(resource.getString(PropertyKey.HEIGHT.getKey(index)));
    String creator = getOptionalString(resource, PropertyKey.CREATOR.getKey(index));
    String id = getOptionalString(resource, PropertyKey.ID.getKey(index));

    return new CatalogTexture(id, name, image, width, height, creator);
  }
  
  /**
   * Returns the category of a texture at the given <code>index</code> of a 
   * localized <code>resource</code> bundle. 
   * @throws MissingResourceException if mandatory keys are not defined.
   */
  protected TexturesCategory readTexturesCategory(ResourceBundle resource, int index) {
    String category = resource.getString(PropertyKey.CATEGORY.getKey(index));
    return new TexturesCategory(category);
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
      if (resourceUrlBase == null) {
        url = new URL(contentFile);
      } else {
        url = contentFile.startsWith("?") 
            ? new URL(resourceUrlBase + contentFile)
            : new URL(resourceUrlBase, contentFile);
      }
      return new URLContent(url);
    } catch (MalformedURLException ex) {
      if (texturesUrl == null) {
        // Otherwise find if it's a resource
        return new ResourceURLContent(DefaultTexturesCatalog.class, contentFile);
      } else {
        try {
          return new ResourceURLContent(new URL("jar:" + texturesUrl + "!" + contentFile), false);
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
