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

import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.FurnitureCatalog;
import com.eteks.sweethome3d.model.FurnitureCategory;
import com.eteks.sweethome3d.model.IllegalHomonymException;
import com.eteks.sweethome3d.tools.ResourceURLContent;
import com.eteks.sweethome3d.tools.URLContent;

/**
 * Furniture default catalog read from localized resources.
 * @author Emmanuel Puybaret
 */
public class DefaultFurnitureCatalog extends FurnitureCatalog {
  private static final String ID               = "id#";
  private static final String NAME             = "name#";
  private static final String CATEGORY         = "category#";
  private static final String ICON             = "icon#";
  private static final String MODEL            = "model#";
  private static final String MULTI_PART_MODEL = "multiPartModel#";
  private static final String WIDTH            = "width#";
  private static final String DEPTH            = "depth#";
  private static final String HEIGHT           = "height#";
  private static final String MOVABLE          = "movable#";
  private static final String DOOR_OR_WINDOW   = "doorOrWindow#";
  private static final String ELEVATION        = "elevation#";
  private static final String MODEL_ROTATION   = "modelRotation#";
  private static final String CREATOR          = "creator#";
  private static final String RESIZABLE        = "resizable#";
  private static final String PRICE            = "price#";
  private static final String VALUE_ADDED_TAX_PERCENTAGE = "valueAddedTaxPercentage#";
  
  private static final String CONTRIBUTED_FURNITURE_CATALOG_FAMILY = "ContributedFurnitureCatalog";
  private static final String ADDITIONAL_FURNITURE_CATALOG_FAMILY  = "AdditionalFurnitureCatalog";
  private static final String PLUGIN_FURNITURE_CATALOG_FAMILY      = "PluginFurnitureCatalog";
  
  private static final String HOMONYM_FURNITURE_FORMAT = "%s -%d-";
  
  /**
   * Creates a default furniture catalog read from resources in the package of this class.
   */
  public DefaultFurnitureCatalog() {
    this(null);
  }
  
  /**
   * Creates a default furniture catalog read from resources in the package of this class and 
   * plugin furniture folder if <code>furniturePluginFolder</code> isn't <code>null</code>.
   */
  public DefaultFurnitureCatalog(File furniturePluginFolder) {
    this(DefaultFurnitureCatalog.class.getName().substring(0, DefaultFurnitureCatalog.class.getName().lastIndexOf(".")), 
        furniturePluginFolder);
  }
  
  /**
   * Creates a default furniture catalog read from resources in <code>resourcePackage</code> and 
   * plugin furniture folder if <code>furniturePluginFolder</code> isn't <code>null</code>.
   */
  public DefaultFurnitureCatalog(String  resourcePackage,
                                 File    furniturePluginFolder) {
    Map<FurnitureCategory, Map<CatalogPieceOfFurniture, Integer>> furnitureHomonymsCounter = 
        new HashMap<FurnitureCategory, Map<CatalogPieceOfFurniture,Integer>>();
    List<String> identifiedFurniture = new ArrayList<String>();
    
    // Load DefaultFurnitureCatalog property file from classpath 
    String defaultFurnitureCatalogFamily = DefaultFurnitureCatalog.class.getName();
    defaultFurnitureCatalogFamily = defaultFurnitureCatalogFamily.substring(
        defaultFurnitureCatalogFamily.lastIndexOf('.') + 1);
    readFurniture(ResourceBundle.getBundle(resourcePackage + "." + defaultFurnitureCatalogFamily), 
        null, furnitureHomonymsCounter, identifiedFurniture);
    
    try {
      // Try do load ContributedFurnitureCatalog property file from classpath 
      readFurniture(ResourceBundle.getBundle(resourcePackage + "." + CONTRIBUTED_FURNITURE_CATALOG_FAMILY), 
          null, furnitureHomonymsCounter, identifiedFurniture);
    } catch (MissingResourceException ex) {
      // Ignore contributed furniture catalog
    }
    
    try {
      // Try do load AdditionalFurnitureCatalog property file from classpath 
      readFurniture(ResourceBundle.getBundle(resourcePackage + "." + ADDITIONAL_FURNITURE_CATALOG_FAMILY), 
          null, furnitureHomonymsCounter, identifiedFurniture);
    } catch (MissingResourceException ex) {
      // Ignore additional furniture catalog
    }
    
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
            // Try do load Furniture property file from current file  
            readFurniture(ResourceBundle.getBundle(PLUGIN_FURNITURE_CATALOG_FAMILY, Locale.getDefault(), 
                new URLClassLoader(new URL [] {furnitureFile.toURI().toURL()})), 
                furnitureFile, furnitureHomonymsCounter, identifiedFurniture);
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
   * Reads each piece of furniture described in <code>resource</code> bundle.
   * Resources described in piece properties will be loaded from <code>furnitureFile</code> 
   * if it isn't <code>null</code>. 
   */
  private void readFurniture(ResourceBundle resource, 
                             File furnitureFile,
                             Map<FurnitureCategory, Map<CatalogPieceOfFurniture, Integer>> furnitureHomonymsCounter,
                             List<String> identifiedFurniture) {
    for (int i = 1;; i++) {
      String name = null;
      try {
        name = resource.getString(NAME + i);
      } catch (MissingResourceException ex) {
        // Stop the loop when a key name# doesn't exist
        break;
      }
      String category = resource.getString(CATEGORY + i);
      Content icon  = getContent(resource, ICON + i, furnitureFile, false);
      boolean multiPartModel = false;
      try {
        multiPartModel = Boolean.parseBoolean(resource.getString(MULTI_PART_MODEL + i));
      } catch (MissingResourceException ex) {
        // By default inDirectory is false
      }
      Content model = getContent(resource, MODEL + i, furnitureFile, multiPartModel);
      float width = Float.parseFloat(resource.getString(WIDTH + i));
      float depth = Float.parseFloat(resource.getString(DEPTH + i));
      float height = Float.parseFloat(resource.getString(HEIGHT + i));
      boolean movable = Boolean.parseBoolean(resource.getString(MOVABLE + i));
      boolean doorOrWindow = Boolean.parseBoolean(
          resource.getString(DOOR_OR_WINDOW + i));
      float elevation = 0;
      try {
        elevation = Float.parseFloat(resource.getString(ELEVATION + i));
      } catch (MissingResourceException ex) {
        // By default elevation is null
      }
      float [][] modelRotation = getModelRotation(resource, MODEL_ROTATION + i);
      String creator = "eTeks";
      try {
        creator = resource.getString(CREATOR + i);
      } catch (MissingResourceException ex) {
        // By default creator is eTeks
      }
      String id = null;
      try {
        id = resource.getString(ID + i);
        if (identifiedFurniture.contains(id)) {
          continue;
        } else {
          // Add id to identifiedFurniture to be sure that two pieces with a same ID
          // won't be added twice to furniture catalog (in case they are cited twice
          // in different furniture properties files)
          identifiedFurniture.add(id);
        }
      } catch (MissingResourceException ex) {
        // Don't take into account furniture that don't have an ID
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
      CatalogPieceOfFurniture piece = new CatalogPieceOfFurniture(id, name, icon, model,
          width, depth, height, elevation, movable, doorOrWindow, modelRotation, creator, 
          resizable, price, valueAddedTaxPercentage);
      add(pieceCategory, piece, furnitureHomonymsCounter);
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
      piece = new CatalogPieceOfFurniture(piece.getId(),
          String.format(HOMONYM_FURNITURE_FORMAT, piece.getName(), pieceHomonymCounter), 
          piece.getIcon(), piece.getModel(),
          piece.getWidth(), piece.getDepth(), piece.getHeight(), piece.getElevation(), 
          piece.isMovable(), piece.isDoorOrWindow(), piece.getModelRotation(), piece.getCreator(),
          piece.isResizable(), piece.getPrice(), piece.getValueAddedTaxPercentage());
      add(pieceCategory, piece, furnitureHomonymsCounter);
    }
  }
  
  /**
   * Returns a valid content instance from the resource file or URL value of key.
   * @param resource a resource bundle
   * @param contentKey  the key of a resource content file
   * @param furnitureFile the file containing the target resource if it's not <code>null</code> 
   * @param multiPartModel if <code>true</code> the resource is a multi part resource stored 
   *                 in a directory with other required resources
   * @throws IllegalArgumentException if the file value doesn't match a valid resource or URL.
   */
  private Content getContent(ResourceBundle resource, 
                             String contentKey, 
                             File furnitureFile, 
                             boolean multiPartModel) {
    String contentFile = resource.getString(contentKey);
    if (furnitureFile == null) {
      try {
        // Try first to interpret contentFile as a URL
        return new URLContent(new URL(contentFile));
      } catch (MalformedURLException ex) {
        // Otherwise find if it's a resource
        return new ResourceURLContent(DefaultFurnitureCatalog.class, contentFile, multiPartModel);
      }
    } else {
      try {
        return new ResourceURLContent(new URL("jar:" + furnitureFile.toURI().toURL() + "!" + contentFile), multiPartModel);
      } catch (MalformedURLException ex) {
        throw new IllegalArgumentException("Invalid URL", ex);
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
}
