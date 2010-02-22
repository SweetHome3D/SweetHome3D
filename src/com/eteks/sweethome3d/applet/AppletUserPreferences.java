/*
 * AppletUserPreferences.java 11 Oct 2008
 *
 * Copyright (c) 2008 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.eteks.sweethome3d.applet;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import com.eteks.sweethome3d.io.DefaultFurnitureCatalog;
import com.eteks.sweethome3d.io.DefaultTexturesCatalog;
import com.eteks.sweethome3d.io.DefaultUserPreferences;
import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.CatalogTexture;
import com.eteks.sweethome3d.model.FurnitureCatalog;
import com.eteks.sweethome3d.model.FurnitureCategory;
import com.eteks.sweethome3d.model.IllegalHomonymException;
import com.eteks.sweethome3d.model.LengthUnit;
import com.eteks.sweethome3d.model.PatternsCatalog;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.TexturesCatalog;
import com.eteks.sweethome3d.model.TexturesCategory;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * Applet user preferences.
 * @author Emmanuel Puybaret
 */
public class AppletUserPreferences extends UserPreferences {
  private static final String LANGUAGE                         = "language";
  private static final String UNIT                             = "unit";
  private static final String FURNITURE_CATALOG_VIEWED_IN_TREE = "furnitureCatalogViewedInTree";
  private static final String NAVIGATION_PANEL_VISIBLE         = "navigationPanelVisible";
  private static final String MAGNETISM_ENABLED                = "magnetismEnabled";
  private static final String RULERS_VISIBLE                   = "rulersVisible";
  private static final String GRID_VISIBLE                     = "gridVisible";
  private static final String FURNITURE_VIEWED_FROM_TOP        = "furnitureViewedFromTop";
  private static final String ROOM_FLOOR_COLORED_OR_TEXTURED   = "roomFloorColoredOrTextured";
  private static final String WALL_PATTERN                     = "wallPattern";
  private static final String NEW_WALL_HEIGHT                  = "newHomeWallHeight";
  private static final String NEW_WALL_THICKNESS               = "newWallThickness";
  private static final String RECENT_HOMES                     = "recentHomes#";
  private static final String IGNORED_ACTION_TIP               = "ignoredActionTip#";  

  private final URL [] pluginFurnitureCatalogURLs;
  private final URL [] pluginTexturesCatalogURLs;
  private Properties   properties;
  private final URL    writePreferencesURL;
  private final URL    readPreferencesURL;
  
  private final Map<String, Boolean> ignoredActionTips = new HashMap<String, Boolean>();

  /**
   * Creates default user preferences read from resource files and catalogs urls given in parameter.
   */
  public AppletUserPreferences(URL [] pluginFurnitureCatalogURLs,
                               URL [] pluginTexturesCatalogURLs) {
    this(pluginFurnitureCatalogURLs, pluginTexturesCatalogURLs, null, null, null);
  }
  
  /**
   * Creates default user preferences read from resource files and catalogs urls given in parameter, 
   * then reads saved user preferences from the XML content returned by <code>readPreferencesURL</code>, 
   * if URL isn't <code>null</code> or empty. 
   * Preferences modifications will be notified to <code>writePreferencesURL</code> with 
   * an XML content describing preferences in a parameter named preferences, 
   * if URL isn't <code>null</code> or empty.
   * The DTD of XML content is specified at 
   * <a href="http://java.sun.com/dtd/properties.dtd">http://java.sun.com/dtd/properties.dtd</a>.
   * Preferences written with this class don't include imported furniture and textures.
   */
  public AppletUserPreferences(URL [] pluginFurnitureCatalogURLs,
                               URL [] pluginTexturesCatalogURLs, 
                               URL writePreferencesURL, 
                               URL readPreferencesURL) {
    this(pluginFurnitureCatalogURLs, pluginTexturesCatalogURLs, writePreferencesURL, readPreferencesURL, null);
  }
  
  /**
   * Creates default user preferences read from resource files and catalogs urls given in parameter, 
   * then reads saved user preferences from the XML content returned by <code>readPreferencesURL</code>, 
   * if URL isn't <code>null</code> or empty. 
   * Preferences modifications will be notified to <code>writePreferencesURL</code> with 
   * an XML content describing preferences in a parameter named preferences, 
   * if URL isn't <code>null</code> or empty.
   * The DTD of XML content is specified at 
   * <a href="http://java.sun.com/dtd/properties.dtd">http://java.sun.com/dtd/properties.dtd</a>.
   * Preferences written with this class don't include imported furniture and textures.
   */
  public AppletUserPreferences(URL [] pluginFurnitureCatalogURLs,
                               URL [] pluginTexturesCatalogURLs, 
                               URL writePreferencesURL, 
                               URL readPreferencesURL,
                               String userLanguage) {
    this.pluginFurnitureCatalogURLs = pluginFurnitureCatalogURLs;
    this.pluginTexturesCatalogURLs = pluginTexturesCatalogURLs;
    this.writePreferencesURL = writePreferencesURL;
    this.readPreferencesURL = readPreferencesURL;
    
    final Properties properties = getProperties();
    
    if (userLanguage == null) {
      userLanguage = getLanguage();
    } 
    if (!Arrays.asList(getSupportedLanguages()).contains(userLanguage)) {
      userLanguage = Locale.ENGLISH.getLanguage();
    }
    setLanguage(properties.getProperty(LANGUAGE, userLanguage));    

    // Read default furniture catalog
    setFurnitureCatalog(new DefaultFurnitureCatalog(pluginFurnitureCatalogURLs));
    // Read default textures catalog
    setTexturesCatalog(new DefaultTexturesCatalog(pluginTexturesCatalogURLs));   
 
    DefaultUserPreferences defaultPreferences = new DefaultUserPreferences();
    
    // Fill default patterns catalog 
    PatternsCatalog patternsCatalog = defaultPreferences.getPatternsCatalog();
    setPatternsCatalog(patternsCatalog);

    // Read other preferences 
    setUnit(LengthUnit.valueOf(properties.getProperty(UNIT, defaultPreferences.getLengthUnit().name())));
    setFurnitureCatalogViewedInTree(Boolean.parseBoolean(properties.getProperty(FURNITURE_CATALOG_VIEWED_IN_TREE, 
        String.valueOf(defaultPreferences.isFurnitureCatalogViewedInTree()))));
    setNavigationPanelVisible(Boolean.parseBoolean(properties.getProperty(NAVIGATION_PANEL_VISIBLE, 
        String.valueOf(defaultPreferences.isNavigationPanelVisible()))));
    setMagnetismEnabled(Boolean.parseBoolean(properties.getProperty(MAGNETISM_ENABLED, "true")));
    setRulersVisible(Boolean.parseBoolean(properties.getProperty(RULERS_VISIBLE, 
        String.valueOf(defaultPreferences.isMagnetismEnabled()))));
    setGridVisible(Boolean.parseBoolean(properties.getProperty(GRID_VISIBLE, 
        String.valueOf(defaultPreferences.isGridVisible()))));
    setFurnitureViewedFromTop(Boolean.parseBoolean(properties.getProperty(FURNITURE_VIEWED_FROM_TOP, 
        String.valueOf(defaultPreferences.isFurnitureViewedFromTop()))));
    setFloorColoredOrTextured(Boolean.parseBoolean(properties.getProperty(ROOM_FLOOR_COLORED_OR_TEXTURED, 
        String.valueOf(defaultPreferences.isRoomFloorColoredOrTextured()))));
    try {
      setWallPattern(patternsCatalog.getPattern(properties.getProperty(WALL_PATTERN, 
          defaultPreferences.getWallPattern().getName())));
    } catch (IllegalArgumentException ex) {
      // Ensure wall pattern always exists even if new patterns are added in future versions
      setWallPattern(defaultPreferences.getWallPattern());
    }
    setNewWallThickness(Float.parseFloat(properties.getProperty(NEW_WALL_THICKNESS, 
            String.valueOf(defaultPreferences.getNewWallThickness()))));
    setNewWallHeight(Float.parseFloat(properties.getProperty(NEW_WALL_HEIGHT,
        String.valueOf(defaultPreferences.getNewWallHeight()))));    
    setCurrency(defaultPreferences.getCurrency());    
    // Read recent homes list
    List<String> recentHomes = new ArrayList<String>();
    for (int i = 1; i <= 4; i++) {
      String recentHome = properties.getProperty(RECENT_HOMES + i, null);
      if (recentHome != null) {
        recentHomes.add(recentHome);
      }
    }
    setRecentHomes(recentHomes);
    // Read ignored action tips
    for (int i = 1; ; i++) {
      String ignoredActionTip = properties.getProperty(IGNORED_ACTION_TIP + i, "");
      if (ignoredActionTip.length() == 0) {
        break;
      } else {
        this.ignoredActionTips.put(ignoredActionTip, true);
      }
    }
    
    addPropertyChangeListener(Property.LANGUAGE, new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
          updateDefaultCatalogs();
        }
      });
  }

  /**
   * Reloads furniture and textures default catalogs.
   */
  private void updateDefaultCatalogs() {
    // Delete default pieces of current furniture catalog          
    FurnitureCatalog furnitureCatalog = getFurnitureCatalog();
    for (FurnitureCategory category : furnitureCatalog.getCategories()) {
      for (CatalogPieceOfFurniture piece : category.getFurniture()) {
        if (!piece.isModifiable()) {
          furnitureCatalog.delete(piece);
        }
      }
    }
    // Add default pieces that don't have homonym among user catalog
    FurnitureCatalog defaultFurnitureCatalog = 
        new DefaultFurnitureCatalog(this.pluginFurnitureCatalogURLs);
    for (FurnitureCategory category : defaultFurnitureCatalog.getCategories()) {
      for (CatalogPieceOfFurniture piece : category.getFurniture()) {
        try {
          furnitureCatalog.add(category, piece);
        } catch (IllegalHomonymException ex) {
          // Ignore pieces that have the same name as an existing piece
        }
      }
    }
    
    // Delete default textures of current textures catalog          
    TexturesCatalog texturesCatalog = getTexturesCatalog();
    for (TexturesCategory category : texturesCatalog.getCategories()) {
      for (CatalogTexture texture : category.getTextures()) {
        if (!texture.isModifiable()) {
          texturesCatalog.delete(texture);
        }
      }
    }
    // Add default textures that don't have homonym among user catalog
    TexturesCatalog defaultTexturesCatalog = 
        new DefaultTexturesCatalog(this.pluginTexturesCatalogURLs);
    for (TexturesCategory category : defaultTexturesCatalog.getCategories()) {
      for (CatalogTexture texture : category.getTextures()) {
        try {
          texturesCatalog.add(category, texture);
        } catch (IllegalHomonymException ex) {
          // Ignore textures that have the same name as an existing piece
        }
      }
    }
  }

  /**
   * Writes user preferences. This method sends to the <code>writePreferencesURL</code> 
   * given at the creation a XML content describing preferences in a parameter named preferences.
   */
  @Override
  public void write() throws RecorderException {
    Properties properties = getProperties();
    // Write other preferences 
    properties.setProperty(LANGUAGE, getLanguage());
    properties.setProperty(UNIT, getLengthUnit().name());   
    properties.setProperty(FURNITURE_CATALOG_VIEWED_IN_TREE, String.valueOf(isFurnitureCatalogViewedInTree()));
    properties.setProperty(NAVIGATION_PANEL_VISIBLE, String.valueOf(isNavigationPanelVisible()));    
    properties.setProperty(MAGNETISM_ENABLED, String.valueOf(isMagnetismEnabled()));
    properties.setProperty(RULERS_VISIBLE, String.valueOf(isRulersVisible()));
    properties.setProperty(GRID_VISIBLE, String.valueOf(isGridVisible()));
    properties.setProperty(FURNITURE_VIEWED_FROM_TOP, String.valueOf(isFurnitureViewedFromTop()));
    properties.setProperty(ROOM_FLOOR_COLORED_OR_TEXTURED, String.valueOf(isRoomFloorColoredOrTextured()));
    properties.setProperty(WALL_PATTERN, getWallPattern().getName());
    properties.setProperty(NEW_WALL_THICKNESS, String.valueOf(getNewWallThickness()));   
    properties.setProperty(NEW_WALL_HEIGHT, String.valueOf(getNewWallHeight()));
    // Write recent homes list
    int i = 1;
    for (Iterator<String> it = getRecentHomes().iterator(); it.hasNext() && i <= 4; i ++) {
      properties.setProperty(RECENT_HOMES + i, it.next());
    }
    // Write ignored action tips
    i = 1;
    for (Iterator<Map.Entry<String, Boolean>> it = this.ignoredActionTips.entrySet().iterator();
         it.hasNext(); ) {
      Entry<String, Boolean> ignoredActionTipEntry = it.next();
      if (ignoredActionTipEntry.getValue()) {
        properties.setProperty(IGNORED_ACTION_TIP + i++, ignoredActionTipEntry.getKey());
      } 
    }
    
    try {
      // Write preferences 
      if (this.writePreferencesURL != null) {
        writePreferences(getProperties());
      }
    } catch (IOException ex) {
      throw new RecorderException("Couldn't write preferences", ex);
    }
  }

  /**
   * Returns Java preferences for current system user.
   */
  private Properties getProperties() {
    if (this.properties == null) {
      this.properties = new Properties();
      if (this.readPreferencesURL != null) {
        readPreferences(this.properties);
      }
    }
    return this.properties;
  }
  
  /**
   * Reads user preferences.
   */
  private void readPreferences(Properties properties) {
    URLConnection connection = null;
    InputStream in = null;
    try {
      // Open an input stream to server 
      connection = this.readPreferencesURL.openConnection();
      connection.setRequestProperty("Content-Type", "charset=UTF-8");
      connection.setUseCaches(false);
      in = connection.getInputStream();
      properties.loadFromXML(in);
      in.close();
    } catch (IOException ex) {
      // Let default preferences unchanged
    } finally {
      try {
        if (in != null) {
          in.close();
        }
      } catch (IOException ex) {
        // Let default preferences unchanged
      }
    }
  }
  
  /**
   * Writes user preferences.
   */
  private void writePreferences(Properties properties) throws IOException {
    HttpURLConnection connection = null;
    try {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      properties.storeToXML(bytes, "Applet user preferences 1.0");
      bytes.close();

      connection = (HttpURLConnection)this.writePreferencesURL.openConnection();
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
      connection.setDoOutput(true);
      connection.setDoInput(true);
      connection.setUseCaches(false);
      OutputStream out = connection.getOutputStream();
      out.write("preferences=".getBytes("UTF-8"));
      out.write(URLEncoder.encode(new String(bytes.toByteArray(), "UTF-8"), "UTF-8").getBytes("UTF-8"));
      out.close();

      // Read response
      InputStream in = connection.getInputStream();
      int read = in.read();
      in.close();
      
      if (read != '1') {
        throw new IOException("Saving preferences failed");
      } 
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }

  /**
   * Sets which action tip should be ignored.
   */
  @Override
  public void setActionTipIgnored(String actionKey) {   
    this.ignoredActionTips.put(actionKey, true);
    super.setActionTipIgnored(actionKey);
  }
  
  /**
   * Returns whether an action tip should be ignored or not. 
   */
  @Override
  public boolean isActionTipIgnored(String actionKey) {
    Boolean ignoredActionTip = this.ignoredActionTips.get(actionKey);
    return ignoredActionTip != null && ignoredActionTip.booleanValue();
  }
  
  /**
   * Resets the display flag of action tips.
   */
  @Override
  public void resetIgnoredActionTips() {
    for (Iterator<Map.Entry<String, Boolean>> it = this.ignoredActionTips.entrySet().iterator();
         it.hasNext(); ) {
      Entry<String, Boolean> ignoredActionTipEntry = it.next();
      ignoredActionTipEntry.setValue(false);
    }
    super.resetIgnoredActionTips();
  }

  /**
   * Throws an exception because applet user preferences can't manage furniture libraries.
   */
  @Override
  public boolean furnitureLibraryExists(String name) throws RecorderException {
    throw new RecorderException("No furniture libraries");
  }

  /**
   * Throws an exception because applet user preferences can't manage additional furniture libraries.
   */
  @Override
  public void addFurnitureLibrary(String name) throws RecorderException {
    throw new RecorderException("No furniture libraries");
  }

  /**
   * Throws an exception because applet user preferences can't manage textures libraries.
   */
  @Override
  public boolean texturesLibraryExists(String name) throws RecorderException {
    throw new RecorderException("No textures libraries");
  }

  /**
   * Throws an exception because applet user preferences can't manage additional textures libraries.
   */
  @Override
  public void addTexturesLibrary(String name) throws RecorderException {
    throw new RecorderException("No textures libraries");
  }
}
