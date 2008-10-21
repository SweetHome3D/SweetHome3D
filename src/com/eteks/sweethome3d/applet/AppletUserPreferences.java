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
import java.util.ArrayList;
import java.util.ResourceBundle;

import com.eteks.sweethome3d.io.DefaultFurnitureCatalog;
import com.eteks.sweethome3d.io.DefaultTexturesCatalog;
import com.eteks.sweethome3d.io.DefaultUserPreferences;
import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.CatalogTexture;
import com.eteks.sweethome3d.model.FurnitureCatalog;
import com.eteks.sweethome3d.model.FurnitureCategory;
import com.eteks.sweethome3d.model.IllegalHomonymException;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.TexturesCatalog;
import com.eteks.sweethome3d.model.TexturesCategory;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * Applet user preferences.
 * @author Emmanuel Puybaret
 */
public class AppletUserPreferences extends UserPreferences {
  /**
   * Creates default user preferences read from resource files.
   */
  public AppletUserPreferences() {
    String appletResourcePackage = AppletUserPreferences.class.getName();
    appletResourcePackage = appletResourcePackage.substring(0, appletResourcePackage.lastIndexOf("."));
    // Read default furniture catalog
    setFurnitureCatalog(new DefaultFurnitureCatalog(appletResourcePackage, false));
    // Read default textures catalog
    setTexturesCatalog(new DefaultTexturesCatalog(appletResourcePackage));   
 
    // Read other preferences from resource bundle
    ResourceBundle resource = ResourceBundle.getBundle(DefaultUserPreferences.class.getName());
    Unit defaultUnit = Unit.valueOf(resource.getString("unit").toUpperCase());
    setUnit(defaultUnit);
    setNewWallThickness(Float.parseFloat(resource.getString("newWallThickness")));
    setNewWallHeight(Float.parseFloat(resource.getString("newHomeWallHeight")));
    setRecentHomes(new ArrayList<String>());
    
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
    // Read again default furniture and textures catalogs with new default locale
    DefaultUserPreferences defaultPreferences = new DefaultUserPreferences();
    // Add default pieces that don't have homonym among user catalog
    FurnitureCatalog defaultFurnitureCatalog = defaultPreferences.getFurnitureCatalog();
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
    TexturesCatalog defaultTexturesCatalog = defaultPreferences.getTexturesCatalog();
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
   * Does nothing, preferences aren't saved.
   */
  @Override
  public void write() throws RecorderException {
  }

  /**
   * Throws an exception because default user preferences can't manage furniture libraries.
   */
  @Override
  public boolean furnitureLibraryExists(String name) throws RecorderException {
    throw new RecorderException("No furniture libraries");
  }

  /**
   * Throws an exception because default user preferences can't manage additional furniture libraries.
   */
  @Override
  public void addFurnitureLibrary(String name) throws RecorderException {
    throw new RecorderException("No furniture libraries");
  }
}
