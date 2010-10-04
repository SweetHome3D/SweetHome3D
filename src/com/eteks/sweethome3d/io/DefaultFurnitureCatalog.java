/*
 * DefaultFurnitureCatalog.java 7 avr. 2006
 * 
 * Copyright (c) 2006 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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
import java.math.BigDecimal;
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

import com.eteks.sweethome3d.model.CatalogDoorOrWindow;
import com.eteks.sweethome3d.model.CatalogLight;
import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.FurnitureCatalog;
import com.eteks.sweethome3d.model.FurnitureCategory;
import com.eteks.sweethome3d.model.IllegalHomonymException;
import com.eteks.sweethome3d.model.LightSource;
import com.eteks.sweethome3d.model.Sash;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.ResourceURLContent;
import com.eteks.sweethome3d.tools.TemporaryURLContent;
import com.eteks.sweethome3d.tools.URLContent;

/**
 * Furniture default catalog read from resources localized in <code>.properties</code> files.
 * @author Emmanuel Puybaret
 */
public class DefaultFurnitureCatalog extends FurnitureCatalog {
  /**
   * The keys of the properties values read in <code>.properties</code> files.
   */
  public enum PropertyKey {
    /**
     * The key for the ID of a piece of furniture (optional). 
     * Two pieces of furniture read in a furniture catalog can't have the same ID
     * and the second one will be ignored.   
     */
    ID("id"),
    /**
     * The key for the name of a piece of furniture (mandatory).
     */
    NAME("name"),
    /**
     * The key for the description of a piece of furniture (optional). 
     * This may give detailed information about a piece of furniture.
     */
    DESCRIPTION("description"),
    /**
     * The key for the category's name of a piece of furniture (mandatory).
     * A new category with this name will be created if it doesn't exist.
     */
    CATEGORY("category"),
    /**
     * The key for the icon file of a piece of furniture (mandatory). 
     * This icon file can be either the path to an image relative to classpath
     * or an absolute URL. It should be encoded in application/x-www-form-urlencoded  
     * format if needed. 
     */
    ICON("icon"),
    /**
     * The key for the plan icon file of a piece of furniture (optional).
     * This icon file can be either the path to an image relative to classpath
     * or an absolute URL. It should be encoded in application/x-www-form-urlencoded  
     * format if needed.
     */
    PLAN_ICON("planIcon"),
    /**
     * The key for the 3D model file of a piece of furniture (mandatory).
     * The 3D model file can be either a path relative to classpath
     * or an absolute URL.  It should be encoded in application/x-www-form-urlencoded  
     * format if needed.
     */
    MODEL("model"),
    /**
     * The key for a piece of furniture with multiple parts (optional).
     * If the value of this key is <code>true</code>, all the files
     * stored in the same directory as the 3D model file (MTL, texture files...)
     * will be considered as being necessary to view correctly the 3D model. 
     */
    MULTI_PART_MODEL("multiPartModel"),
    /**
     * The key for the width in centimeters of a piece of furniture (mandatory).
     */
    WIDTH("width"),
    /**
     * The key for the height in centimeters of a piece of furniture (mandatory).
     */
    DEPTH("depth"),
    /**
     * The key for the height in centimeters of a piece of furniture (mandatory).
     */
    HEIGHT("height"),
    /**
     * The key for the movability of a piece of furniture (mandatory).
     * If the value of this key is <code>true</code>, the piece of furniture
     * will be considered as a movable piece. 
     */
    MOVABLE("movable"),
    /**
     * The key for the door or window type of a piece of furniture (mandatory).
     * If the value of this key is <code>true</code>, the piece of furniture
     * will be considered as a door or a window. 
     */
    DOOR_OR_WINDOW("doorOrWindow"),
    /**
     * The key for the wall thickness in centimeters of a door or a window (optional).
     * By default, a door or a window has the same depth as the wall it belongs to.
     */
    DOOR_OR_WINDOW_WALL_THICKNESS("doorOrWindowWallThickness"),
    /**
     * The key for the distance in centimeters of a door or a window to its wall (optional).
     * By default, this distance is zero.
     */
    DOOR_OR_WINDOW_WALL_DISTANCE("doorOrWindowWallDistance"),
    /**
     * The key for the sash axis distance(s) of a door or a window along X axis (optional).
     * If a door or a window has more than one sash, the values of each sash should be 
     * separated by spaces.  
     */
    DOOR_OR_WINDOW_SASH_X_AXIS("doorOrWindowSashXAxis"),
    /**
     * The key for the sash axis distance(s) of a door or a window along Y axis 
     * (mandatory if sash axis distance along X axis is defined).
     */
    DOOR_OR_WINDOW_SASH_Y_AXIS("doorOrWindowSashYAxis"),
    /**
     * The key for the sash width(s) of a door or a window  
     * (mandatory if sash axis distance along X axis is defined).
     */
    DOOR_OR_WINDOW_SASH_WIDTH("doorOrWindowSashWidth"),
    /**
     * The key for the sash start angle(s) of a door or a window  
     * (mandatory if sash axis distance along X axis is defined).
     */
    DOOR_OR_WINDOW_SASH_START_ANGLE("doorOrWindowSashStartAngle"),
    /**
     * The key for the sash end angle(s) of a door or a window  
     * (mandatory if sash axis distance along X axis is defined).
     */
    DOOR_OR_WINDOW_SASH_END_ANGLE("doorOrWindowSashEndAngle"),
    /**
     * The key for the abscissa(s) of light sources in a light (optional).
     * If a light has more than one light source, the values of each light source should 
     * be separated by spaces.
     */
    LIGHT_SOURCE_X("lightSourceX"),
    /**
     * The key for the ordinate(s) of light sources in a light (mandatory if light source abscissa is defined).
     */
    LIGHT_SOURCE_Y("lightSourceY"),
    /**
     * The key for the elevation(s) of light sources in a light (mandatory if light source abscissa is defined).
     */
    LIGHT_SOURCE_Z("lightSourceZ"),
    /**
     * The key for the color(s) of light sources in a light (mandatory if light source abscissa is defined).
     */
    LIGHT_SOURCE_COLOR("lightSourceColor"),
    /**
     * The key for the color(s) of light sources in a light (optional).
     */
    LIGHT_SOURCE_DIAMETER("lightSourceDiameter"),
    /**
     * The key for the elevation in centimeters of a piece of furniture (optional).
     */
    ELEVATION("elevation"),
    /**
     * The key for the transformation matrix values applied to a piece of furniture (optional).
     * If the 3D model of a piece of furniture isn't correctly oriented, 
     * the value of this key should give the 9 values of the transformation matrix 
     * that will orient it correctly.  
     */
    MODEL_ROTATION("modelRotation"),
    /**
     * The key for the creator of a piece of furniture (optional).
     * By default, creator is eTeks.
     */
    CREATOR("creator"),
    /**
     * The key for the resizability of a piece of furniture (optional).
     * If the value of this key is <code>false</code>, the piece of furniture
     * will be considered as a piece with a fixed size. 
     */
    RESIZABLE("resizable"),
    /**
     * The key for the price of a piece of furniture (optional).
     */
    PRICE("price"),
    /**
     * The key for the VAT percentage of a piece of furniture (optional).
     */
    VALUE_ADDED_TAX_PERCENTAGE("valueAddedTaxPercentage");
    
    private String keyPrefix;

    private PropertyKey(String keyPrefix) {
      this.keyPrefix = keyPrefix;
    }
    
    /**
     * Returns the key for the piece property of the given index.
     */
    public String getKey(int pieceIndex) {
      return keyPrefix + "#" + pieceIndex;
    }
  }

  /**
   * The name of <code>.properties</code> family files in plugin furniture catalog files. 
   */
  public static final String PLUGIN_FURNITURE_CATALOG_FAMILY = "PluginFurnitureCatalog";
  
  private static final String CONTRIBUTED_FURNITURE_CATALOG_FAMILY = "ContributedFurnitureCatalog";
  private static final String ADDITIONAL_FURNITURE_CATALOG_FAMILY  = "AdditionalFurnitureCatalog";
  
  private static final String HOMONYM_FURNITURE_FORMAT = "%s -%d-";
  
  /**
   * Creates a default furniture catalog read from resources in the package of this class.
   */
  public DefaultFurnitureCatalog() {
    this((File)null);
  }
  
  /**
   * Creates a default furniture catalog read from resources and   
   * furniture plugin folder if <code>furniturePluginFolder</code> isn't <code>null</code>.
   */
  public DefaultFurnitureCatalog(File furniturePluginFolder) {
    this(null, furniturePluginFolder);
  }
  
  /**
   * Creates a default furniture catalog read from resources and   
   * furniture plugin folder if <code>furniturePluginFolder</code> isn't <code>null</code>.
   */
  public DefaultFurnitureCatalog(final UserPreferences preferences, 
                                 File furniturePluginFolder) {
    this(preferences, furniturePluginFolder == null ? null : new File [] {furniturePluginFolder});
  }
  
  /**
   * Creates a default furniture catalog read from resources and   
   * furniture plugin folders if <code>furniturePluginFolders</code> isn't <code>null</code>.
   */
  public DefaultFurnitureCatalog(final UserPreferences preferences, 
                                 File [] furniturePluginFolders) {
    Map<FurnitureCategory, Map<CatalogPieceOfFurniture, Integer>> furnitureHomonymsCounter = 
        new HashMap<FurnitureCategory, Map<CatalogPieceOfFurniture,Integer>>();
    List<String> identifiedFurniture = new ArrayList<String>();
    
    // Try to load com.eteks.sweethome3d.io.DefaultFurnitureCatalog property file from classpath 
    final String defaultFurnitureCatalogFamily = DefaultFurnitureCatalog.class.getName();
    readFurnitureCatalog(defaultFurnitureCatalogFamily, 
        preferences, furnitureHomonymsCounter, identifiedFurniture);
    
    // Try to load com.eteks.sweethome3d.io.ContributedFurnitureCatalog property file from classpath 
    String classPackage = defaultFurnitureCatalogFamily.substring(0, defaultFurnitureCatalogFamily.lastIndexOf("."));
    readFurnitureCatalog(classPackage + "." + CONTRIBUTED_FURNITURE_CATALOG_FAMILY, 
        preferences, furnitureHomonymsCounter, identifiedFurniture);
    
    // Try to load com.eteks.sweethome3d.io.AdditionalFurnitureCatalog property file from classpath
    readFurnitureCatalog(classPackage + "." + ADDITIONAL_FURNITURE_CATALOG_FAMILY, 
        preferences, furnitureHomonymsCounter, identifiedFurniture);
    
    if (furniturePluginFolders != null) {
      for (File furniturePluginFolder : furniturePluginFolders) {
        // Try to load sh3f files from furniture plugin folder
        File [] pluginFurnitureCatalogFiles = furniturePluginFolder.listFiles(new FileFilter () {
          public boolean accept(File pathname) {
            return pathname.isFile();
          }
        });
        
        if (pluginFurnitureCatalogFiles != null) {
          // Treat furniture catalog files in reverse order so file named with a date will be taken into account 
          // from most recent to least recent
          Arrays.sort(pluginFurnitureCatalogFiles, Collections.reverseOrder());
          for (File pluginFurnitureCatalogFile : pluginFurnitureCatalogFiles) {
            // Try to load the properties file describing furniture catalog from current file  
            readPluginFurnitureCatalog(pluginFurnitureCatalogFile, furnitureHomonymsCounter, identifiedFurniture);
          }
        }
      }
    }
  }
  
  /**
   * Creates a default furniture catalog read only from resources in the given URLs.
   */
  public DefaultFurnitureCatalog(URL [] pluginFurnitureCatalogUrls) {
    this(pluginFurnitureCatalogUrls, null);
  }
  
  /**
   * Creates a default furniture catalog read only from resources in the given URLs.
   * Model and icon URLs will built from <code>furnitureResourcesUrlBase</code> if it isn't <code>null</code>.
   */
  public DefaultFurnitureCatalog(URL [] pluginFurnitureCatalogUrls,
                                 URL    furnitureResourcesUrlBase) {
    Map<FurnitureCategory, Map<CatalogPieceOfFurniture, Integer>> furnitureHomonymsCounter = 
        new HashMap<FurnitureCategory, Map<CatalogPieceOfFurniture,Integer>>();
    List<String> identifiedFurniture = new ArrayList<String>();

    for (URL pluginFurnitureCatalogUrl : pluginFurnitureCatalogUrls) {
      try {
        readFurniture(ResourceBundle.getBundle(PLUGIN_FURNITURE_CATALOG_FAMILY, Locale.getDefault(), 
                new URLClassLoader(new URL [] {pluginFurnitureCatalogUrl})), 
            pluginFurnitureCatalogUrl, furnitureResourcesUrlBase, furnitureHomonymsCounter, identifiedFurniture);
      } catch (MissingResourceException ex) {
        // Ignore malformed furniture catalog
      } catch (IllegalArgumentException ex) {
        // Ignore malformed furniture catalog
      }
    }
  }

  private static final Map<File,URL> pluginFurnitureCatalogUrlUpdates = new HashMap<File,URL>(); 
  
  /**
   * Reads plug-in furniture catalog from the <code>pluginFurnitureCatalogFile</code> file. 
   */
  private void readPluginFurnitureCatalog(File pluginFurnitureCatalogFile,
                                          Map<FurnitureCategory, Map<CatalogPieceOfFurniture, Integer>> furnitureHomonymsCounter, 
                                          List<String> identifiedFurniture) {
    try {
      URL pluginFurnitureCatalogUrl = pluginFurnitureCatalogFile.toURI().toURL();
      long urlModificationDate = pluginFurnitureCatalogFile.lastModified();
      URL urlUpdate = pluginFurnitureCatalogUrlUpdates.get(pluginFurnitureCatalogUrl.toString());
      if (pluginFurnitureCatalogFile.canWrite()
          && (urlUpdate == null 
              || urlUpdate.openConnection().getLastModified() < urlModificationDate)) {
        // Copy updated resource URL content to a temporary file to ensure furniture added to home can safely 
        // reference any file of the catalog file even if its content is changed afterwards
        TemporaryURLContent contentCopy = TemporaryURLContent.copyToTemporaryURLContent(new URLContent(pluginFurnitureCatalogUrl));
        URL temporaryFurnitureCatalogUrl = contentCopy.getURL();
        pluginFurnitureCatalogUrlUpdates.put(pluginFurnitureCatalogFile, temporaryFurnitureCatalogUrl);
        pluginFurnitureCatalogUrl = temporaryFurnitureCatalogUrl;
      } else if (urlUpdate != null) {
        pluginFurnitureCatalogUrl = urlUpdate;
      }
      
      ResourceBundle resourceBundle = ResourceBundle.getBundle(PLUGIN_FURNITURE_CATALOG_FAMILY, Locale.getDefault(), 
          new URLClassLoader(new URL [] {pluginFurnitureCatalogUrl}));      
      readFurniture(resourceBundle, pluginFurnitureCatalogUrl, null, furnitureHomonymsCounter,
          identifiedFurniture);
    } catch (MissingResourceException ex) {
      // Ignore malformed furniture catalog
    } catch (IllegalArgumentException ex) {
      // Ignore malformed furniture catalog
    } catch (IOException ex) {
      // Ignore unaccessible catalog
    }
  }
  
  /**
   * Reads furniture of a given catalog family from resources.
   */
  private void readFurnitureCatalog(final String furnitureCatalogFamily,
                                    final UserPreferences preferences,
                                    Map<FurnitureCategory, Map<CatalogPieceOfFurniture, Integer>> furnitureHomonymsCounter,
                                    List<String> identifiedFurniture) {
    ResourceBundle resource;
    if (preferences != null) {
      // Adapt getLocalizedString to ResourceBundle
      resource = new ResourceBundle() {
          @Override
          protected Object handleGetObject(String key) {
            try {
              return preferences.getLocalizedString(furnitureCatalogFamily, key);
            } catch (IllegalArgumentException ex) {
              throw new MissingResourceException("Unknown key " + key, 
                  furnitureCatalogFamily + "_" + Locale.getDefault(), key);
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
        resource = ResourceBundle.getBundle(furnitureCatalogFamily);
      } catch (MissingResourceException ex) {
        return;
      }
    }
    readFurniture(resource, null, null, furnitureHomonymsCounter, identifiedFurniture);
  }
  
  /**
   * Reads each piece of furniture described in <code>resource</code> bundle.
   * Resources described in piece properties will be loaded from <code>furnitureUrl</code> 
   * if it isn't <code>null</code> or relative to <code>furnitureResourcesUrlBase</code>. 
   */
  private void readFurniture(ResourceBundle resource, 
                             URL furnitureCatalogUrl,
                             URL furnitureResourcesUrlBase,
                             Map<FurnitureCategory, Map<CatalogPieceOfFurniture, Integer>> furnitureHomonymsCounter,
                             List<String> identifiedFurniture) {
    CatalogPieceOfFurniture piece;
    for (int i = 1; (piece = readPieceOfFurniture(resource, i, furnitureCatalogUrl, furnitureResourcesUrlBase)) != null; i++) {
      if (piece.getId() != null) {
        // Take into account only furniture that have an ID
        if (identifiedFurniture.contains(piece.getId())) {
          continue;
        } else {
          // Add id to identifiedFurniture to be sure that two pieces with a same ID
          // won't be added twice to furniture catalog (in case they are cited twice
          // in different furniture properties files)
          identifiedFurniture.add(piece.getId());
        }
      }
      FurnitureCategory pieceCategory = readFurnitureCategory(resource, i);
      add(pieceCategory, piece, furnitureHomonymsCounter);
    }
  }

  /**
   * Returns the piece of furniture at the given <code>index</code> of a 
   * localized <code>resource</code> bundle. 
   * @param resource             a resource bundle 
   * @param index                the index of the read piece
   * @param furnitureCatalogUrl  the URL from which piece resources will be loaded 
   *            or <code>null</code> if it's read from current classpath.
   * @param furnitureResourcesUrlBase the URL used as a base to build the URL to piece resources  
   *            or <code>null</code> if it's read from current classpath or <code>furnitureCatalogUrl</code>
   * @return the read piece of furniture or <code>null</code> if the piece at the given index doesn't exist.
   * @throws MissingResourceException if mandatory keys are not defined.
   */
  protected CatalogPieceOfFurniture readPieceOfFurniture(ResourceBundle resource, 
                                                         int index, 
                                                         URL furnitureCatalogUrl,
                                                         URL furnitureResourcesUrlBase) {
    String name = null;
    try {
      name = resource.getString(PropertyKey.NAME.getKey(index));
    } catch (MissingResourceException ex) {
      // Return null if key name# doesn't exist
      return null;
    }
    String description = getOptionalString(resource, PropertyKey.DESCRIPTION.getKey(index), null);
    Content icon  = getContent(resource, PropertyKey.ICON.getKey(index), 
        furnitureCatalogUrl, furnitureResourcesUrlBase, false, false);
    Content planIcon = getContent(resource, PropertyKey.PLAN_ICON.getKey(index), 
        furnitureCatalogUrl, furnitureResourcesUrlBase, false, true);
    boolean multiPartModel = false;
    try {
      multiPartModel = Boolean.parseBoolean(resource.getString(PropertyKey.MULTI_PART_MODEL.getKey(index)));
    } catch (MissingResourceException ex) {
      // By default inDirectory is false
    }
    Content model = getContent(resource, PropertyKey.MODEL.getKey(index), 
        furnitureCatalogUrl, furnitureResourcesUrlBase, multiPartModel, false);
    float width = Float.parseFloat(resource.getString(PropertyKey.WIDTH.getKey(index)));
    float depth = Float.parseFloat(resource.getString(PropertyKey.DEPTH.getKey(index)));
    float height = Float.parseFloat(resource.getString(PropertyKey.HEIGHT.getKey(index)));
    boolean movable = Boolean.parseBoolean(resource.getString(PropertyKey.MOVABLE.getKey(index)));
    boolean doorOrWindow = Boolean.parseBoolean(resource.getString(PropertyKey.DOOR_OR_WINDOW.getKey(index)));
    float elevation = getOptionalFloat(resource, PropertyKey.ELEVATION.getKey(index), 0);
    float [][] modelRotation = getModelRotation(resource, PropertyKey.MODEL_ROTATION.getKey(index));
    // By default creator is eTeks
    String creator = getOptionalString(resource, PropertyKey.CREATOR.getKey(index), null);
    String id = getOptionalString(resource, PropertyKey.ID.getKey(index), null);
    boolean resizable = true;
    try {
      resizable = Boolean.parseBoolean(resource.getString(PropertyKey.RESIZABLE.getKey(index)));
    } catch (MissingResourceException ex) {
      // By default piece is resizable
    }
    BigDecimal price = null;
    try {
      price = new BigDecimal(resource.getString(PropertyKey.PRICE.getKey(index)));
    } catch (MissingResourceException ex) {
      // By default price is null
    }
    BigDecimal valueAddedTaxPercentage = null;
    try {
      valueAddedTaxPercentage = new BigDecimal(resource.getString(PropertyKey.VALUE_ADDED_TAX_PERCENTAGE.getKey(index)));
    } catch (MissingResourceException ex) {
      // By default price is null
    }

    if (doorOrWindow) {
      float wallThicknessPercentage = getOptionalFloat(
          resource, PropertyKey.DOOR_OR_WINDOW_WALL_THICKNESS.getKey(index), depth) / depth;
      float wallDistancePercentage = getOptionalFloat(
          resource, PropertyKey.DOOR_OR_WINDOW_WALL_DISTANCE.getKey(index), 0) / depth;
      Sash [] sashes = getDoorOrWindowSashes(resource, index, width, depth);
      return new CatalogDoorOrWindow(id, name, description, icon, planIcon, model,
          width, depth, height, elevation, movable, 
          wallThicknessPercentage, wallDistancePercentage, sashes, modelRotation, creator, 
          resizable, price, valueAddedTaxPercentage);
    } else {
      LightSource [] lightSources = getLightSources(resource, index, width, depth, height);
      if (lightSources != null) {
        return new CatalogLight(id, name, description, icon, planIcon, model,
            width, depth, height, elevation, movable, lightSources, modelRotation, creator, 
            resizable, price, valueAddedTaxPercentage);
      } else {
        return new CatalogPieceOfFurniture(id, name, description, icon, planIcon, model,
            width, depth, height, elevation, movable, modelRotation, creator, 
            resizable, price, valueAddedTaxPercentage);
      }
    }
  }
  
  /**
   * Returns the furniture category of a piece at the given <code>index</code> of a 
   * localized <code>resource</code> bundle. 
   * @throws MissingResourceException if mandatory keys are not defined.
   */
  protected FurnitureCategory readFurnitureCategory(ResourceBundle resource, int index) {
    String category = resource.getString(PropertyKey.CATEGORY.getKey(index));
    return new FurnitureCategory(category);
  }
    
  /**
   * Adds a <code>piece</code> to its category in catalog. If <code>piece</code> has an homonym
   * in its category its name will be suffixed indicating its sequence.
   */
  private void add(FurnitureCategory pieceCategory,
                   CatalogPieceOfFurniture piece,
                   Map<FurnitureCategory, Map<CatalogPieceOfFurniture, Integer>> furnitureHomonymsCounter) {
    try {        
      add(pieceCategory, piece);
    } catch (IllegalHomonymException ex) {
      // Search the counter of piece name
      Map<CatalogPieceOfFurniture, Integer> categoryFurnitureHomonymsCounter = 
          furnitureHomonymsCounter.get(pieceCategory);
      if (categoryFurnitureHomonymsCounter == null) {
        categoryFurnitureHomonymsCounter = new HashMap<CatalogPieceOfFurniture, Integer>();
        furnitureHomonymsCounter.put(pieceCategory, categoryFurnitureHomonymsCounter);
      }
      Integer pieceHomonymCounter = categoryFurnitureHomonymsCounter.get(piece);
      if (pieceHomonymCounter == null) {
        pieceHomonymCounter = 1;
      }
      categoryFurnitureHomonymsCounter.put(piece, ++pieceHomonymCounter);
      // Try to add piece again to catalog with a suffix indicating its sequence
      String suffixedName = String.format(HOMONYM_FURNITURE_FORMAT, piece.getName(), pieceHomonymCounter);
      if (piece instanceof CatalogDoorOrWindow) {
        CatalogDoorOrWindow doorOrWindow = (CatalogDoorOrWindow)piece;
        piece = new CatalogDoorOrWindow(doorOrWindow.getId(), suffixedName,
            doorOrWindow.getDescription(), doorOrWindow.getIcon(), doorOrWindow.getPlanIcon(), doorOrWindow.getModel(),
            doorOrWindow.getWidth(), doorOrWindow.getDepth(), doorOrWindow.getHeight(), doorOrWindow.getElevation(), 
            doorOrWindow.isMovable(), doorOrWindow.getWallThickness(), 
            doorOrWindow.getWallDistance(), doorOrWindow.getSashes(), 
            doorOrWindow.getModelRotation(), doorOrWindow.getCreator(),
            doorOrWindow.isResizable(), doorOrWindow.getPrice(), doorOrWindow.getValueAddedTaxPercentage());
      } else if (piece instanceof CatalogLight) {
        CatalogLight light = (CatalogLight)piece;
        piece = new CatalogLight(light.getId(), suffixedName,
            light.getDescription(), light.getIcon(), light.getPlanIcon(), light.getModel(),
            light.getWidth(), light.getDepth(), light.getHeight(), light.getElevation(), 
            light.isMovable(), light.getLightSources(), 
            light.getModelRotation(), light.getCreator(),
            light.isResizable(), light.getPrice(), light.getValueAddedTaxPercentage());
      } else {
        piece = new CatalogPieceOfFurniture(piece.getId(), suffixedName,
            piece.getDescription(), piece.getIcon(), piece.getPlanIcon(), piece.getModel(),
            piece.getWidth(), piece.getDepth(), piece.getHeight(), piece.getElevation(), 
            piece.isMovable(), piece.getModelRotation(), piece.getCreator(),
            piece.isResizable(), piece.getPrice(), piece.getValueAddedTaxPercentage());
      }
      add(pieceCategory, piece, furnitureHomonymsCounter);
    }
  }
  
  /**
   * Returns a valid content instance from the resource file or URL value of key.
   * @param resource a resource bundle
   * @param contentKey  the key of a resource content file
   * @param furnitureUrl the URL of the file containing the target resource if it's not <code>null</code> 
   * @param resourceUrlBase the URL used as a base to build the URL to content file  
   *            or <code>null</code> if it's read from current classpath or <code>furnitureCatalogUrl</code>.
   * @param multiPartModel if <code>true</code> the resource is a multi part resource stored 
   *                 in a directory with other required resources
   * @throws IllegalArgumentException if the file value doesn't match a valid resource or URL.
   */
  private Content getContent(ResourceBundle resource, 
                             String contentKey, 
                             URL furnitureUrl,
                             URL resourceUrlBase, 
                             boolean multiPartModel,
                             boolean optional) {
    String contentFile = optional
       ? getOptionalString(resource, contentKey, null)
       : resource.getString(contentKey);
    if (optional && contentFile == null) {
      return null;
    }
    try {
      // Try first to interpret contentFile as an absolute URL 
      // or an URL relative to resourceUrlBase if it's not null
      URL url;
      if (resourceUrlBase != null) {
        if (contentFile.indexOf('!') < 0 || contentFile.startsWith("jar:")) {
          url = new URL(resourceUrlBase, contentFile);
        } else {
          url = new URL("jar:" + new URL(resourceUrlBase, contentFile));
        }
      } else {
        url = new URL(contentFile);
      }
      return new URLContent(url);
    } catch (MalformedURLException ex) {
      if (furnitureUrl == null) {
        // Otherwise find if it's a resource
        return new ResourceURLContent(DefaultFurnitureCatalog.class, contentFile, multiPartModel);
      } else {
        try {
          return new ResourceURLContent(new URL("jar:" + furnitureUrl + "!" + contentFile), multiPartModel);
        } catch (MalformedURLException ex2) {
          throw new IllegalArgumentException("Invalid URL", ex2);
        }
      }
    }
  }
  
  /**
   * Returns model rotation parsed from key value.
   */
  private float [][] getModelRotation(ResourceBundle resource, String key) {
    try {
      String modelRotationString = resource.getString(key);
      String [] values = modelRotationString.split(" ", 9);
      
      if (values.length == 9) {
        return new float [][] {{Float.parseFloat(values [0]), 
                                Float.parseFloat(values [1]), 
                                Float.parseFloat(values [2])}, 
                               {Float.parseFloat(values [3]), 
                                Float.parseFloat(values [4]), 
                                Float.parseFloat(values [5])}, 
                               {Float.parseFloat(values [6]), 
                                Float.parseFloat(values [7]), 
                                Float.parseFloat(values [8])}};
      } else {
        return null;
      }
    } catch (MissingResourceException ex) {
      return null;
    } catch (NumberFormatException ex) {
      return null;
    }
  }
  
  /**
   * Returns optional door or windows sashes.
   */
  private Sash [] getDoorOrWindowSashes(ResourceBundle resource, int index, 
                                        float doorOrWindowWidth, 
                                        float doorOrWindowDepth) throws MissingResourceException {
    Sash [] sashes;
    String sashXAxisString = getOptionalString(resource, PropertyKey.DOOR_OR_WINDOW_SASH_X_AXIS.getKey(index), null);
    if (sashXAxisString != null) {
      String [] sashXAxisValues = sashXAxisString.split(" ");
      // If doorOrWindowHingesX#i key exists the 3 other keys with the same count of numbers must exist too
      String [] sashYAxisValues = resource.getString(PropertyKey.DOOR_OR_WINDOW_SASH_Y_AXIS.getKey(index)).split(" ");
      if (sashYAxisValues.length != sashXAxisValues.length) {
        throw new IllegalArgumentException(
            "Expected " + sashXAxisValues.length + " values in " + PropertyKey.DOOR_OR_WINDOW_SASH_Y_AXIS.getKey(index) + " key");
      }
      String [] sashWidths = resource.getString(PropertyKey.DOOR_OR_WINDOW_SASH_WIDTH.getKey(index)).split(" ");
      if (sashWidths.length != sashXAxisValues.length) {
        throw new IllegalArgumentException(
            "Expected " + sashXAxisValues.length + " values in " + PropertyKey.DOOR_OR_WINDOW_SASH_WIDTH.getKey(index) + " key");
      }
      String [] sashStartAngles = resource.getString(PropertyKey.DOOR_OR_WINDOW_SASH_START_ANGLE.getKey(index)).split(" ");
      if (sashStartAngles.length != sashXAxisValues.length) {
        throw new IllegalArgumentException(
            "Expected " + sashXAxisValues.length + " values in " + PropertyKey.DOOR_OR_WINDOW_SASH_START_ANGLE.getKey(index) + " key");
      }
      String [] sashEndAngles = resource.getString(PropertyKey.DOOR_OR_WINDOW_SASH_END_ANGLE.getKey(index)).split(" ");
      if (sashEndAngles.length != sashXAxisValues.length) {
        throw new IllegalArgumentException(
            "Expected " + sashXAxisValues.length + " values in " + PropertyKey.DOOR_OR_WINDOW_SASH_END_ANGLE.getKey(index) + " key");
      }
      
      sashes = new Sash [sashXAxisValues.length];
      for (int i = 0; i < sashes.length; i++) {
        // Create the matching sash, converting cm to percentage of width or depth, and degrees to radians
        sashes [i] = new Sash(Float.parseFloat(sashXAxisValues [i]) / doorOrWindowWidth, 
            Float.parseFloat(sashYAxisValues [i]) / doorOrWindowDepth, 
            Float.parseFloat(sashWidths [i]) / doorOrWindowWidth, 
            (float)Math.toRadians(Float.parseFloat(sashStartAngles [i])), 
            (float)Math.toRadians(Float.parseFloat(sashEndAngles [i])));
      }
    } else {
      sashes = new Sash [0];
    }
    
    return sashes;
  }

  /**
   * Returns optional light sources.
   */
  private LightSource [] getLightSources(ResourceBundle resource, int index, 
                                         float lightWidth, 
                                         float lightDepth,
                                         float lightHeight) throws MissingResourceException {
    LightSource [] lightSources = null;
    String lightSourceXString = getOptionalString(resource, PropertyKey.LIGHT_SOURCE_X.getKey(index), null);
    if (lightSourceXString != null) {
      String [] lightSourceX = lightSourceXString.split(" ");
      // If doorOrWindowHingesX#i key exists the 3 other keys with the same count of numbers must exist too
      String [] lightSourceY = resource.getString(PropertyKey.LIGHT_SOURCE_Y.getKey(index)).split(" ");
      if (lightSourceY.length != lightSourceX.length) {
        throw new IllegalArgumentException(
            "Expected " + lightSourceX.length + " values in " + PropertyKey.LIGHT_SOURCE_Y.getKey(index) + " key");
      }
      String [] lightSourceZ = resource.getString(PropertyKey.LIGHT_SOURCE_Z.getKey(index)).split(" ");
      if (lightSourceZ.length != lightSourceX.length) {
        throw new IllegalArgumentException(
            "Expected " + lightSourceX.length + " values in " + PropertyKey.LIGHT_SOURCE_Z.getKey(index) + " key");
      }
      String [] lightSourceColors = resource.getString(PropertyKey.LIGHT_SOURCE_COLOR.getKey(index)).split(" ");
      if (lightSourceColors.length != lightSourceX.length) {
        throw new IllegalArgumentException(
            "Expected " + lightSourceX.length + " values in " + PropertyKey.LIGHT_SOURCE_COLOR.getKey(index) + " key");
      }
      String lightSourceDiametersString = getOptionalString(resource, PropertyKey.LIGHT_SOURCE_DIAMETER.getKey(index), null);
      String [] lightSourceDiameters;
      if (lightSourceDiametersString != null) {
        lightSourceDiameters = lightSourceDiametersString.split(" ");
        if (lightSourceDiameters.length != lightSourceX.length) {
          throw new IllegalArgumentException(
              "Expected " + lightSourceX.length + " values in " + PropertyKey.LIGHT_SOURCE_DIAMETER.getKey(index) + " key");
        }
      } else {
        lightSourceDiameters = null;
      }
      
      lightSources = new LightSource [lightSourceX.length];
      for (int i = 0; i < lightSources.length; i++) {
        int color = lightSourceColors [i].startsWith("#")
            ? Integer.parseInt(lightSourceColors [i].substring(1), 16)
            : Integer.parseInt(lightSourceColors [i]);
        // Create the matching light source, converting cm to percentage of width, depth and height
        lightSources [i] = new LightSource(Float.parseFloat(lightSourceX [i]) / lightWidth, 
            Float.parseFloat(lightSourceY [i]) / lightDepth, 
            Float.parseFloat(lightSourceZ [i]) / lightHeight, 
            color,
            lightSourceDiameters != null
                ? Float.parseFloat(lightSourceDiameters [i]) / lightWidth
                : null);
      }
    }     
    return lightSources;
  }

  /**
   * Returns the value of <code>propertyKey</code> in <code>resource</code>, 
   * or <code>defaultValue</code> if the property doesn't exist.
   */
  private String getOptionalString(ResourceBundle resource, 
                                   String propertyKey,
                                   String defaultValue) {
    try {
      return resource.getString(propertyKey);
    } catch (MissingResourceException ex) {
      return defaultValue;
    }
  }

  /**
   * Returns the value of <code>propertyKey</code> in <code>resource</code>, 
   * or <code>defaultValue</code> if the property doesn't exist.
   */
  private float getOptionalFloat(ResourceBundle resource, 
                                 String propertyKey,
                                 float defaultValue) {
    try {
      return Float.parseFloat(resource.getString(propertyKey));
    } catch (MissingResourceException ex) {
      return defaultValue;
    }
  }
}

