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
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import com.eteks.sweethome3d.tools.ResourceURLContent;
import com.eteks.sweethome3d.tools.URLContent;

/**
 * Furniture default catalog read from localized resources.
 * @author Emmanuel Puybaret
 */
public class DefaultFurnitureCatalog extends FurnitureCatalog {
  private static final String ID                              = "id#";
  private static final String NAME                            = "name#";
  private static final String DESCRIPTION                     = "description#";
  private static final String CATEGORY                        = "category#";
  private static final String ICON                            = "icon#";
  private static final String MODEL                           = "model#";
  private static final String MULTI_PART_MODEL                = "multiPartModel#";
  private static final String WIDTH                           = "width#";
  private static final String DEPTH                           = "depth#";
  private static final String HEIGHT                          = "height#";
  private static final String MOVABLE                         = "movable#";
  private static final String DOOR_OR_WINDOW                  = "doorOrWindow#";
  private static final String DOOR_OR_WINDOW_WALL_THICKNESS   = "doorOrWindowWallThickness#";
  private static final String DOOR_OR_WINDOW_WALL_DISTANCE    = "doorOrWindowWallDistance#";
  private static final String DOOR_OR_WINDOW_SASH_X_AXIS      = "doorOrWindowSashXAxis#";
  private static final String DOOR_OR_WINDOW_SASH_Y_AXIS      = "doorOrWindowSashYAxis#";
  private static final String DOOR_OR_WINDOW_SASH_WIDTH       = "doorOrWindowSashWidth#";
  private static final String DOOR_OR_WINDOW_SASH_START_ANGLE = "doorOrWindowSashStartAngle#";
  private static final String DOOR_OR_WINDOW_SASH_END_ANGLE   = "doorOrWindowSashEndAngle#";
  private static final String LIGHT_SOURCE_X                  = "lightSourceX#";
  private static final String LIGHT_SOURCE_Y                  = "lightSourceY#";
  private static final String LIGHT_SOURCE_Z                  = "lightSourceZ#";
  private static final String LIGHT_SOURCE_COLOR              = "lightSourceColor#";
  private static final String ELEVATION                       = "elevation#";
  private static final String MODEL_ROTATION                  = "modelRotation#";
  private static final String CREATOR                         = "creator#";
  private static final String RESIZABLE                       = "resizable#";
  private static final String PRICE                           = "price#";
  private static final String VALUE_ADDED_TAX_PERCENTAGE      = "valueAddedTaxPercentage#";
  
  private static final String CONTRIBUTED_FURNITURE_CATALOG_FAMILY = "ContributedFurnitureCatalog";
  private static final String ADDITIONAL_FURNITURE_CATALOG_FAMILY  = "AdditionalFurnitureCatalog";
  private static final String PLUGIN_FURNITURE_CATALOG_FAMILY      = "PluginFurnitureCatalog";
  
  private static final String HOMONYM_FURNITURE_FORMAT = "%s -%d-";
  
  /**
   * Creates a default furniture catalog read from resources in the package of this class.
   */
  public DefaultFurnitureCatalog() {
    this((File)null);
  }
  
  /**
   * Creates a default furniture catalog read from resources in  
   * plugin furniture folder if <code>furniturePluginFolder</code> isn't <code>null</code>.
   */
  public DefaultFurnitureCatalog(File furniturePluginFolder) {
    Map<FurnitureCategory, Map<CatalogPieceOfFurniture, Integer>> furnitureHomonymsCounter = 
        new HashMap<FurnitureCategory, Map<CatalogPieceOfFurniture,Integer>>();
    List<String> identifiedFurniture = new ArrayList<String>();
    
    ResourceBundle resource;
    try {
      // Try to load DefaultFurnitureCatalog property file from classpath 
      resource = ResourceBundle.getBundle(DefaultFurnitureCatalog.class.getName());
    } catch (MissingResourceException ex) {
      // Ignore furniture catalog
      resource = null;
    }
    readFurniture(resource, null, furnitureHomonymsCounter, identifiedFurniture);
    
    String classPackage = DefaultFurnitureCatalog.class.getName();
    classPackage = classPackage.substring(0, classPackage.lastIndexOf("."));
    try {
      // Try to load com.eteks.sweethome3d.io.ContributedFurnitureCatalog property file from classpath 
      resource = ResourceBundle.getBundle(classPackage + "." + CONTRIBUTED_FURNITURE_CATALOG_FAMILY);
    } catch (MissingResourceException ex) {
      // Ignore contributed furniture catalog
      resource = null;
    }
    readFurniture(resource, null, furnitureHomonymsCounter, identifiedFurniture);
    
    try {
      // Try to load com.eteks.sweethome3d.io.AdditionalFurnitureCatalog property file from classpath
      ResourceBundle.getBundle(classPackage + "." + ADDITIONAL_FURNITURE_CATALOG_FAMILY);
    } catch (MissingResourceException ex) {
      // Ignore additional furniture catalog
      resource = null;
    }
    readFurniture(resource, null, furnitureHomonymsCounter, identifiedFurniture);
    
    if (furniturePluginFolder != null) {
      // Try to load sh3f files from plugin folder
      File [] furnitureFiles = furniturePluginFolder.listFiles(new FileFilter () {
        public boolean accept(File pathname) {
          return pathname.isFile();
        }
      });
      
      if (furnitureFiles != null) {
        // Treat furniture files in reverse order so file named with a date will be taken into account 
        // from most recent to least recent
        Arrays.sort(furnitureFiles, Collections.reverseOrder());
        for (File furnitureFile : furnitureFiles) {
          try {
            // Try to load Furniture property file from current file  
            URL furnitureUrl = furnitureFile.toURI().toURL();
            readFurniture(ResourceBundle.getBundle(PLUGIN_FURNITURE_CATALOG_FAMILY, Locale.getDefault(), 
                                                   new URLClassLoader(new URL [] {furnitureUrl})), 
                furnitureUrl, furnitureHomonymsCounter, identifiedFurniture);
          } catch (MalformedURLException ex) {
            // Ignore file
          } catch (MissingResourceException ex) {
            // Ignore malformed plugin furniture catalog
          }
        }
      }
    }
  }
  
  /**
   * Creates a default furniture catalog read from resources in the given URLs.
   */
  public DefaultFurnitureCatalog(URL [] pluginFurnitureCatalogUrls) {
    Map<FurnitureCategory, Map<CatalogPieceOfFurniture, Integer>> furnitureHomonymsCounter = 
        new HashMap<FurnitureCategory, Map<CatalogPieceOfFurniture,Integer>>();
    List<String> identifiedFurniture = new ArrayList<String>();

    for (URL pluginFurnitureCatalogUrl : pluginFurnitureCatalogUrls) {
      try {
        // Try do load Furniture property file from current file  
        readFurniture(ResourceBundle.getBundle(PLUGIN_FURNITURE_CATALOG_FAMILY, Locale.getDefault(), 
            new URLClassLoader(new URL [] {pluginFurnitureCatalogUrl})), 
            pluginFurnitureCatalogUrl, furnitureHomonymsCounter, identifiedFurniture);
      } catch (MissingResourceException ex) {
        // Ignore malformed plugin furniture catalog
      }
    }
  }
  
  /**
   * Reads each piece of furniture described in <code>resource</code> bundle.
   * Resources described in piece properties will be loaded from <code>furnitureUrl</code> 
   * if it isn't <code>null</code>. 
   */
  private void readFurniture(ResourceBundle resource, 
                             URL furnitureUrl,
                             Map<FurnitureCategory, Map<CatalogPieceOfFurniture, Integer>> furnitureHomonymsCounter,
                             List<String> identifiedFurniture) {
    if (resource != null) {
      for (int i = 1;; i++) {
        String name = null;
        try {
          name = resource.getString(NAME + i);
        } catch (MissingResourceException ex) {
          // Stop the loop when a key name# doesn't exist
          break;
        }
        String description = getOptionalString(resource, DESCRIPTION + i, null);
        String category = resource.getString(CATEGORY + i);
        Content icon  = getContent(resource, ICON + i, furnitureUrl, false);
        boolean multiPartModel = false;
        try {
          multiPartModel = Boolean.parseBoolean(resource.getString(MULTI_PART_MODEL + i));
        } catch (MissingResourceException ex) {
          // By default inDirectory is false
        }
        Content model = getContent(resource, MODEL + i, furnitureUrl, multiPartModel);
        float width = Float.parseFloat(resource.getString(WIDTH + i));
        float depth = Float.parseFloat(resource.getString(DEPTH + i));
        float height = Float.parseFloat(resource.getString(HEIGHT + i));
        boolean movable = Boolean.parseBoolean(resource.getString(MOVABLE + i));
        boolean doorOrWindow = Boolean.parseBoolean(resource.getString(DOOR_OR_WINDOW + i));
        float elevation = getOptionalFloat(resource, ELEVATION + i, 0);
        float [][] modelRotation = getModelRotation(resource, MODEL_ROTATION + i);
        // By default creator is eTeks
        String creator = getOptionalString(resource, CREATOR + i, "eTeks");
        String id = getOptionalString(resource, ID + i, null);
        if (id != null) {
          // Take into account only furniture that have an ID
          if (identifiedFurniture.contains(id)) {
            continue;
          } else {
            // Add id to identifiedFurniture to be sure that two pieces with a same ID
            // won't be added twice to furniture catalog (in case they are cited twice
            // in different furniture properties files)
            identifiedFurniture.add(id);
          }
        }
        boolean resizable = true;
        try {
          resizable = Boolean.parseBoolean(resource.getString(RESIZABLE + i));
        } catch (MissingResourceException ex) {
          // By default piece is resizable
        }
        BigDecimal price = null;
        try {
          price = new BigDecimal(resource.getString(PRICE + i));
        } catch (MissingResourceException ex) {
          // By default price is null
        }
        BigDecimal valueAddedTaxPercentage = null;
        try {
          valueAddedTaxPercentage = new BigDecimal(resource.getString(VALUE_ADDED_TAX_PERCENTAGE + i));
        } catch (MissingResourceException ex) {
          // By default price is null
        }
  
        FurnitureCategory pieceCategory = new FurnitureCategory(category);
        CatalogPieceOfFurniture piece;
        if (doorOrWindow) {
          float wallThicknessPercentage = getOptionalFloat(
              resource, DOOR_OR_WINDOW_WALL_THICKNESS + i, depth) / depth;
          float wallDistancePercentage = getOptionalFloat(
              resource, DOOR_OR_WINDOW_WALL_DISTANCE + i, 0) / depth;
          Sash [] sashes = getDoorOrWindowSashes(resource, i, width, depth);
          piece = new CatalogDoorOrWindow(id, name, description, icon, model,
              width, depth, height, elevation, movable, 
              wallThicknessPercentage, wallDistancePercentage, sashes, modelRotation, creator, 
              resizable, price, valueAddedTaxPercentage);
        } else {
          LightSource [] lightSources = getLightSources(resource, i, width, depth, height);
          if (lightSources != null) {
            piece = new CatalogLight(id, name, description, icon, model,
                width, depth, height, elevation, movable, lightSources, modelRotation, creator, 
                resizable, price, valueAddedTaxPercentage);
          } else {
            piece = new CatalogPieceOfFurniture(id, name, description, icon, model,
                width, depth, height, elevation, movable, modelRotation, creator, 
                resizable, price, valueAddedTaxPercentage);
          }
        }
        add(pieceCategory, piece, furnitureHomonymsCounter);
      }
    }
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
            doorOrWindow.getDescription(), doorOrWindow.getIcon(), doorOrWindow.getModel(),
            doorOrWindow.getWidth(), doorOrWindow.getDepth(), doorOrWindow.getHeight(), doorOrWindow.getElevation(), 
            doorOrWindow.isMovable(), doorOrWindow.getWallThickness(), 
            doorOrWindow.getWallDistance(), doorOrWindow.getSashes(), 
            doorOrWindow.getModelRotation(), doorOrWindow.getCreator(),
            doorOrWindow.isResizable(), doorOrWindow.getPrice(), doorOrWindow.getValueAddedTaxPercentage());
      } else if (piece instanceof CatalogLight) {
        CatalogLight light = (CatalogLight)piece;
        piece = new CatalogLight(light.getId(), suffixedName,
            light.getDescription(), light.getIcon(), light.getModel(),
            light.getWidth(), light.getDepth(), light.getHeight(), light.getElevation(), 
            light.isMovable(), light.getLightSources(), 
            light.getModelRotation(), light.getCreator(),
            light.isResizable(), light.getPrice(), light.getValueAddedTaxPercentage());
      } else {
        piece = new CatalogPieceOfFurniture(piece.getId(), suffixedName,
            piece.getDescription(), piece.getIcon(), piece.getModel(),
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
   * @param multiPartModel if <code>true</code> the resource is a multi part resource stored 
   *                 in a directory with other required resources
   * @throws IllegalArgumentException if the file value doesn't match a valid resource or URL.
   */
  private Content getContent(ResourceBundle resource, 
                             String contentKey, 
                             URL furnitureUrl, 
                             boolean multiPartModel) {
    String contentFile = resource.getString(contentKey);
    try {
      // Try first to interpret contentFile as a URL
      return new URLContent(new URL(contentFile));
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
    String sashXAxisString = getOptionalString(resource, DOOR_OR_WINDOW_SASH_X_AXIS + index, null);
    if (sashXAxisString != null) {
      String [] sashXAxisValues = sashXAxisString.split(" ");
      // If doorOrWindowHingesX#i key exists the 3 other keys with the same count of numbers must exist too
      String [] sashYAxisValues = resource.getString(DOOR_OR_WINDOW_SASH_Y_AXIS + index).split(" ");
      if (sashYAxisValues.length != sashXAxisValues.length) {
        throw new IllegalArgumentException(
            "Expected " + sashXAxisValues.length + " values in " + DOOR_OR_WINDOW_SASH_Y_AXIS + index + " key");
      }
      String [] sashWidths = resource.getString(DOOR_OR_WINDOW_SASH_WIDTH + index).split(" ");
      if (sashWidths.length != sashXAxisValues.length) {
        throw new IllegalArgumentException(
            "Expected " + sashXAxisValues.length + " values in " + DOOR_OR_WINDOW_SASH_WIDTH + index + " key");
      }
      String [] sashStartAngles = resource.getString(DOOR_OR_WINDOW_SASH_START_ANGLE + index).split(" ");
      if (sashStartAngles.length != sashXAxisValues.length) {
        throw new IllegalArgumentException(
            "Expected " + sashXAxisValues.length + " values in " + DOOR_OR_WINDOW_SASH_START_ANGLE + index + " key");
      }
      String [] sashEndAngles = resource.getString(DOOR_OR_WINDOW_SASH_END_ANGLE + index).split(" ");
      if (sashEndAngles.length != sashXAxisValues.length) {
        throw new IllegalArgumentException(
            "Expected " + sashXAxisValues.length + " values in " + DOOR_OR_WINDOW_SASH_END_ANGLE + index + " key");
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
    String lightSourceXString = getOptionalString(resource, LIGHT_SOURCE_X + index, null);
    if (lightSourceXString != null) {
      String [] lightSourceX = lightSourceXString.split(" ");
      // If doorOrWindowHingesX#i key exists the 3 other keys with the same count of numbers must exist too
      String [] lightSourceY = resource.getString(LIGHT_SOURCE_Y + index).split(" ");
      if (lightSourceY.length != lightSourceX.length) {
        throw new IllegalArgumentException(
            "Expected " + lightSourceX.length + " values in " + LIGHT_SOURCE_Y + index + " key");
      }
      String [] lightSourceZ = resource.getString(LIGHT_SOURCE_Z + index).split(" ");
      if (lightSourceZ.length != lightSourceX.length) {
        throw new IllegalArgumentException(
            "Expected " + lightSourceX.length + " values in " + LIGHT_SOURCE_Z + index + " key");
      }
      String [] lightSourceColors = resource.getString(LIGHT_SOURCE_COLOR + index).split(" ");
      if (lightSourceColors.length != lightSourceX.length) {
        throw new IllegalArgumentException(
            "Expected " + lightSourceX.length + " values in " + LIGHT_SOURCE_COLOR + index + " key");
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
            color);
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

