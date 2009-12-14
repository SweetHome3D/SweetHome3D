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
 * Furniture default catalog read from resources localized in <code>.properties</code> files.
 * @author Emmanuel Puybaret
 */
public class DefaultFurnitureCatalog extends FurnitureCatalog {
  /**
   * The key for the ID of a piece of furniture (optional). 
   * Two pieces of furniture read in a furniture catalog can't have the same ID
   * and the second one will be ignored.   
   */
  public static final String KEY_ID = "id";
  /**
   * The key for the name of a piece of furniture (mandatory).
   */
  public static final String KEY_NAME = "name";
  /**
   * The key for the description of a piece of furniture (optional). 
   * This may give detailed information about a piece of furniture.
   */
  public static final String KEY_DESCRIPTION = "description";
  /**
   * The key for the category's name of a piece of furniture (mandatory).
   * A new category with this name will be created if it doesn't exist.
   */
  public static final String KEY_CATEGORY = "category";
  /**
   * The key for the icon file of a piece of furniture (mandatory). 
   * This icon file can be either the path to an image relative to classpath
   * or an absolute URL. 
   */
  public static final String KEY_ICON = "icon";
  /**
   * The key for the plan icon file of a piece of furniture (optional).
   * This icon file can be either the path to an image relative to classpath
   * or an absolute URL. 
   */
  public static final String KEY_PLAN_ICON = "planIcon";
  /**
   * The key for the 3D model file of a piece of furniture (mandatory).
   * The 3D model file can be either a path relative to classpath
   * or an absolute URL. 
   */
  public static final String KEY_MODEL = "model";
  /**
   * The key for a piece of furniture with multiple parts (optional).
   * If the value of this key is <code>true</code>, all the files
   * stored in the same directory as the 3D model file (MTL, texture files...)
   * will be considered as being necessary to view correctly the 3D model. 
   */
  public static final String KEY_MULTI_PART_MODEL = "multiPartModel";
  /**
   * The key for the width in centimeters of a piece of furniture (mandatory).
   */
  public static final String KEY_WIDTH = "width";
  /**
   * The key for the height in centimeters of a piece of furniture (mandatory).
   */
  public static final String KEY_DEPTH = "depth";
  /**
   * The key for the height in centimeters of a piece of furniture (mandatory).
   */
  public static final String KEY_HEIGHT = "height";
  /**
   * The key for the movability of a piece of furniture (mandatory).
   * If the value of this key is <code>true</code>, the piece of furniture
   * will be considered as a movable piece. 
   */
  public static final String KEY_MOVABLE = "movable";
  /**
   * The key for the door or window type of a piece of furniture (mandatory).
   * If the value of this key is <code>true</code>, the piece of furniture
   * will be considered as a door or a window. 
   */
  public static final String KEY_DOOR_OR_WINDOW = "doorOrWindow";
  /**
   * The key for the wall thickness in centimeters of a door or a window (optional).
   * By default, a door or a window has the same depth as the wall it belongs to.
   */
  public static final String KEY_DOOR_OR_WINDOW_WALL_THICKNESS = "doorOrWindowWallThickness";
  /**
   * The key for the distance in centimeters of a door or a window to its wall (optional).
   * By default, this distance is zero.
   */
  public static final String KEY_DOOR_OR_WINDOW_WALL_DISTANCE = "doorOrWindowWallDistance";
  /**
   * The key for the sash axis distance(s) of a door or a window along X axis (optional).
   * If a door or a window has more than one sash, the values of each sash should be 
   * separated by spaces.  
   */
  public static final String KEY_DOOR_OR_WINDOW_SASH_X_AXIS = "doorOrWindowSashXAxis";
  /**
   * The key for the sash axis distance(s) of a door or a window along Y axis 
   * (mandatory if sash axis distance along X axis is defined).
   */
  public static final String KEY_DOOR_OR_WINDOW_SASH_Y_AXIS = "doorOrWindowSashYAxis";
  /**
   * The key for the sash width(s) of a door or a window  
   * (mandatory if sash axis distance along X axis is defined).
   */
  public static final String KEY_DOOR_OR_WINDOW_SASH_WIDTH = "doorOrWindowSashWidth";
  /**
   * The key for the sash start angle(s) of a door or a window  
   * (mandatory if sash axis distance along X axis is defined).
   */
  public static final String KEY_DOOR_OR_WINDOW_SASH_START_ANGLE = "doorOrWindowSashStartAngle";
  /**
   * The key for the sash end angle(s) of a door or a window  
   * (mandatory if sash axis distance along X axis is defined).
   */
  public static final String KEY_DOOR_OR_WINDOW_SASH_END_ANGLE = "doorOrWindowSashEndAngle";
  /**
   * The key for the abscissa(s) of light sources in a light (optional).
   * If a light has more than one light source, the values of each light source should 
   * be separated by spaces.
   */
  public static final String KEY_LIGHT_SOURCE_X = "lightSourceX";
  /**
   * The key for the ordinate(s) of light sources in a light (mandatory if light source abscissa is defined).
   */
  public static final String KEY_LIGHT_SOURCE_Y = "lightSourceY";
  /**
   * The key for the elevation(s) of light sources in a light (mandatory if light source abscissa is defined).
   */
  public static final String KEY_LIGHT_SOURCE_Z = "lightSourceZ";
  /**
   * The key for the color(s) of light sources in a light (mandatory if light source abscissa is defined).
   */
  public static final String KEY_LIGHT_SOURCE_COLOR = "lightSourceColor";
  /**
   * The key for the elevation in centimeters of a piece of furniture (optional).
   */
  public static final String KEY_ELEVATION = "elevation";
  /**
   * The key for the transformation matrix values applied to a piece of furniture (optional).
   * If the 3D model of a piece of furniture isn't correctly oriented, 
   * the value of this key should give the 9 values of the transformation matrix 
   * that will orient it correctly.  
   */
  public static final String KEY_MODEL_ROTATION = "modelRotation";
  /**
   * The key for the creator of a piece of furniture (optional).
   * By default, creator is eTeks.
   */
  public static final String KEY_CREATOR = "creator";
  /**
   * The key for the resizability of a piece of furniture (optional).
   * If the value of this key is <code>false</code>, the piece of furniture
   * will be considered as a piece with a fixed size. 
   */
  public static final String KEY_RESIZABLE = "resizable";
  /**
   * The key for the price of a piece of furniture (optional).
   */
  public static final String KEY_PRICE = "price";
  /**
   * The key for the VAT percentage of a piece of furniture (optional).
   */
  public static final String KEY_VALUE_ADDED_TAX_PERCENTAGE = "valueAddedTaxPercentage";

  /**
   * Separator between a <code>KEY_...</code> constant and the order number of a piece of furniture.
   */
  public static final String SEPARATOR = "#";

  /**
   * The name of <code>.properties</code> family files in furniture plug-in files. 
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
      resource = ResourceBundle.getBundle(classPackage + "." + ADDITIONAL_FURNITURE_CATALOG_FAMILY);
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
          name = resource.getString(KEY_NAME + SEPARATOR + i);
        } catch (MissingResourceException ex) {
          // Stop the loop when a key name# doesn't exist
          break;
        }
        String description = getOptionalString(resource, KEY_DESCRIPTION + SEPARATOR + i, null);
        String category = resource.getString(KEY_CATEGORY + SEPARATOR + i);
        Content icon  = getContent(resource, KEY_ICON + SEPARATOR + i, furnitureUrl, false, false);
        Content planIcon = getContent(resource, KEY_PLAN_ICON + SEPARATOR + i, furnitureUrl, false, true);
        boolean multiPartModel = false;
        try {
          multiPartModel = Boolean.parseBoolean(resource.getString(KEY_MULTI_PART_MODEL + SEPARATOR + i));
        } catch (MissingResourceException ex) {
          // By default inDirectory is false
        }
        Content model = getContent(resource, KEY_MODEL + SEPARATOR + i, furnitureUrl, multiPartModel, false);
        float width = Float.parseFloat(resource.getString(KEY_WIDTH + SEPARATOR + i));
        float depth = Float.parseFloat(resource.getString(KEY_DEPTH + SEPARATOR + i));
        float height = Float.parseFloat(resource.getString(KEY_HEIGHT + SEPARATOR + i));
        boolean movable = Boolean.parseBoolean(resource.getString(KEY_MOVABLE + SEPARATOR + i));
        boolean doorOrWindow = Boolean.parseBoolean(resource.getString(KEY_DOOR_OR_WINDOW + SEPARATOR + i));
        float elevation = getOptionalFloat(resource, KEY_ELEVATION + SEPARATOR + i, 0);
        float [][] modelRotation = getModelRotation(resource, KEY_MODEL_ROTATION + SEPARATOR + i);
        // By default creator is eTeks
        String creator = getOptionalString(resource, KEY_CREATOR + SEPARATOR + i, "eTeks");
        String id = getOptionalString(resource, KEY_ID + SEPARATOR + i, null);
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
          resizable = Boolean.parseBoolean(resource.getString(KEY_RESIZABLE + SEPARATOR + i));
        } catch (MissingResourceException ex) {
          // By default piece is resizable
        }
        BigDecimal price = null;
        try {
          price = new BigDecimal(resource.getString(KEY_PRICE + SEPARATOR + i));
        } catch (MissingResourceException ex) {
          // By default price is null
        }
        BigDecimal valueAddedTaxPercentage = null;
        try {
          valueAddedTaxPercentage = new BigDecimal(resource.getString(KEY_VALUE_ADDED_TAX_PERCENTAGE + SEPARATOR + i));
        } catch (MissingResourceException ex) {
          // By default price is null
        }
  
        FurnitureCategory pieceCategory = new FurnitureCategory(category);
        CatalogPieceOfFurniture piece;
        if (doorOrWindow) {
          float wallThicknessPercentage = getOptionalFloat(
              resource, KEY_DOOR_OR_WINDOW_WALL_THICKNESS + SEPARATOR + i, depth) / depth;
          float wallDistancePercentage = getOptionalFloat(
              resource, KEY_DOOR_OR_WINDOW_WALL_DISTANCE + SEPARATOR + i, 0) / depth;
          Sash [] sashes = getDoorOrWindowSashes(resource, i, width, depth);
          piece = new CatalogDoorOrWindow(id, name, description, icon, planIcon, model,
              width, depth, height, elevation, movable, 
              wallThicknessPercentage, wallDistancePercentage, sashes, modelRotation, creator, 
              resizable, price, valueAddedTaxPercentage);
        } else {
          LightSource [] lightSources = getLightSources(resource, i, width, depth, height);
          if (lightSources != null) {
            piece = new CatalogLight(id, name, description, icon, planIcon, model,
                width, depth, height, elevation, movable, lightSources, modelRotation, creator, 
                resizable, price, valueAddedTaxPercentage);
          } else {
            piece = new CatalogPieceOfFurniture(id, name, description, icon, planIcon, model,
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
   * @param multiPartModel if <code>true</code> the resource is a multi part resource stored 
   *                 in a directory with other required resources
   * @throws IllegalArgumentException if the file value doesn't match a valid resource or URL.
   */
  private Content getContent(ResourceBundle resource, 
                             String contentKey, 
                             URL furnitureUrl, 
                             boolean multiPartModel,
                             boolean optional) {
    String contentFile = optional
       ? getOptionalString(resource, contentKey, null)
       : resource.getString(contentKey);
    if (optional && contentFile == null) {
      return null;
    }
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
    String sashXAxisString = getOptionalString(resource, KEY_DOOR_OR_WINDOW_SASH_X_AXIS + SEPARATOR + index, null);
    if (sashXAxisString != null) {
      String [] sashXAxisValues = sashXAxisString.split(" ");
      // If doorOrWindowHingesX#i key exists the 3 other keys with the same count of numbers must exist too
      String [] sashYAxisValues = resource.getString(KEY_DOOR_OR_WINDOW_SASH_Y_AXIS + SEPARATOR + index).split(" ");
      if (sashYAxisValues.length != sashXAxisValues.length) {
        throw new IllegalArgumentException(
            "Expected " + sashXAxisValues.length + " values in " + KEY_DOOR_OR_WINDOW_SASH_Y_AXIS + SEPARATOR + index + " key");
      }
      String [] sashWidths = resource.getString(KEY_DOOR_OR_WINDOW_SASH_WIDTH + SEPARATOR + index).split(" ");
      if (sashWidths.length != sashXAxisValues.length) {
        throw new IllegalArgumentException(
            "Expected " + sashXAxisValues.length + " values in " + KEY_DOOR_OR_WINDOW_SASH_WIDTH + SEPARATOR + index + " key");
      }
      String [] sashStartAngles = resource.getString(KEY_DOOR_OR_WINDOW_SASH_START_ANGLE + SEPARATOR + index).split(" ");
      if (sashStartAngles.length != sashXAxisValues.length) {
        throw new IllegalArgumentException(
            "Expected " + sashXAxisValues.length + " values in " + KEY_DOOR_OR_WINDOW_SASH_START_ANGLE + SEPARATOR + index + " key");
      }
      String [] sashEndAngles = resource.getString(KEY_DOOR_OR_WINDOW_SASH_END_ANGLE + SEPARATOR + index).split(" ");
      if (sashEndAngles.length != sashXAxisValues.length) {
        throw new IllegalArgumentException(
            "Expected " + sashXAxisValues.length + " values in " + KEY_DOOR_OR_WINDOW_SASH_END_ANGLE + SEPARATOR + index + " key");
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
    String lightSourceXString = getOptionalString(resource, KEY_LIGHT_SOURCE_X + SEPARATOR + index, null);
    if (lightSourceXString != null) {
      String [] lightSourceX = lightSourceXString.split(" ");
      // If doorOrWindowHingesX#i key exists the 3 other keys with the same count of numbers must exist too
      String [] lightSourceY = resource.getString(KEY_LIGHT_SOURCE_Y + SEPARATOR + index).split(" ");
      if (lightSourceY.length != lightSourceX.length) {
        throw new IllegalArgumentException(
            "Expected " + lightSourceX.length + " values in " + KEY_LIGHT_SOURCE_Y + SEPARATOR + index + " key");
      }
      String [] lightSourceZ = resource.getString(KEY_LIGHT_SOURCE_Z + SEPARATOR + index).split(" ");
      if (lightSourceZ.length != lightSourceX.length) {
        throw new IllegalArgumentException(
            "Expected " + lightSourceX.length + " values in " + KEY_LIGHT_SOURCE_Z + SEPARATOR + index + " key");
      }
      String [] lightSourceColors = resource.getString(KEY_LIGHT_SOURCE_COLOR + SEPARATOR + index).split(" ");
      if (lightSourceColors.length != lightSourceX.length) {
        throw new IllegalArgumentException(
            "Expected " + lightSourceX.length + " values in " + KEY_LIGHT_SOURCE_COLOR + SEPARATOR + index + " key");
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

