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
import java.net.URL;
import java.util.ArrayList;

import com.eteks.sweethome3d.io.DefaultFurnitureCatalog;
import com.eteks.sweethome3d.io.DefaultTexturesCatalog;
import com.eteks.sweethome3d.io.DefaultUserPreferences;
import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.CatalogTexture;
import com.eteks.sweethome3d.model.FurnitureCatalog;
import com.eteks.sweethome3d.model.FurnitureCategory;
import com.eteks.sweethome3d.model.IllegalHomonymException;
import com.eteks.sweethome3d.model.LengthUnit;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.TexturesCatalog;
import com.eteks.sweethome3d.model.TexturesCategory;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * Applet user preferences.
 * @author Emmanuel Puybaret
 */
public class AppletUserPreferences extends UserPreferences {
  private URL [] pluginFurnitureCatalogUrls;
  private URL [] pluginTexturesCatalogUrls;

  /**
   * Creates default user preferences read from resource files.
   */
  public AppletUserPreferences(URL [] pluginFurnitureCatalogUrls,
                               URL [] pluginTexturesCatalogUrls) {
    this.pluginFurnitureCatalogUrls = pluginFurnitureCatalogUrls;
    this.pluginTexturesCatalogUrls = pluginTexturesCatalogUrls;
    
    // Read default furniture catalog
    setFurnitureCatalog(new DefaultFurnitureCatalog(pluginFurnitureCatalogUrls));
    // Read default textures catalog
    setTexturesCatalog(new DefaultTexturesCatalog(pluginTexturesCatalogUrls));   
 
    // Read other preferences from resource bundle
    LengthUnit defaultUnit = LengthUnit.valueOf(getLocalizedString(
        DefaultUserPreferences.class, "unit").toUpperCase());
    setUnit(defaultUnit);
    setNewWallThickness(Float.parseFloat(getLocalizedString(
        DefaultUserPreferences.class, "newWallThickness")));
    setNewWallHeight(Float.parseFloat(getLocalizedString(
        DefaultUserPreferences.class, "newHomeWallHeight")));
    setRecentHomes(new ArrayList<String>());
    try {
      setCurrency(getLocalizedString(DefaultUserPreferences.class, "currency"));
    } catch (IllegalArgumentException ex) {
      // Don't use currency and prices in program
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
        new DefaultFurnitureCatalog(this.pluginFurnitureCatalogUrls);
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
        new DefaultTexturesCatalog(this.pluginTexturesCatalogUrls);
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
