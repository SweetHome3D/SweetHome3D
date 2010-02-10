/*
 * DefaultUserPreferences.java 15 mai 2006
 *
 * Copyright (c) 2006 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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
package com.eteks.sweethome3d.io;

import java.util.ArrayList;
import java.util.List;

import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.LengthUnit;
import com.eteks.sweethome3d.model.PatternsCatalog;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.TextureImage;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.ResourceURLContent;

/**
 * Default user preferences.
 * @author Emmanuel Puybaret
 */
public class DefaultUserPreferences extends UserPreferences {
  /**
   * Creates default user preferences read from resource files.
   */
  public DefaultUserPreferences() {
    // Read default furniture catalog
    setFurnitureCatalog(new DefaultFurnitureCatalog());
    // Read default textures catalog
    setTexturesCatalog(new DefaultTexturesCatalog());
    // Build default patterns catalog
    List<TextureImage> patterns = new ArrayList<TextureImage>();
    patterns.add(new PatternTexture("foreground"));
    patterns.add(new PatternTexture("hatchUp"));
    patterns.add(new PatternTexture("hatchDown"));
    patterns.add(new PatternTexture("background"));
    PatternsCatalog patternsCatalog = new PatternsCatalog(patterns);
    setPatternsCatalog(patternsCatalog);
    // Read other preferences from resource bundle
    setFurnitureCatalogViewedInTree(Boolean.parseBoolean(
        getLocalizedString(DefaultUserPreferences.class, "furnitureCatalogViewedInTree")));
    setUnit(LengthUnit.valueOf(getLocalizedString(DefaultUserPreferences.class, "unit").toUpperCase()));
    setRulersVisible(Boolean.parseBoolean(getLocalizedString(DefaultUserPreferences.class, "rulersVisible")));
    setGridVisible(Boolean.parseBoolean(getLocalizedString(DefaultUserPreferences.class, "gridVisible")));
    setFurnitureViewedFromTop(Boolean.parseBoolean(getLocalizedString(DefaultUserPreferences.class, "furnitureViewedFromTop")));
    setFloorColoredOrTextured(Boolean.parseBoolean(getLocalizedString(DefaultUserPreferences.class, "roomFloorColoredOrTextured")));
    setWallPattern(patternsCatalog.getPattern(getLocalizedString(DefaultUserPreferences.class, "wallPattern")));
    setNewWallThickness(Float.parseFloat(getLocalizedString(DefaultUserPreferences.class, "newWallThickness")));
    setNewWallHeight(Float.parseFloat(getLocalizedString(DefaultUserPreferences.class, "newHomeWallHeight")));
    setRecentHomes(new ArrayList<String>());
    try {
      setCurrency(getLocalizedString(DefaultUserPreferences.class, "currency"));
    } catch (IllegalArgumentException ex) {
      // Don't use currency and prices in program
    }
  }

  /**
   * Throws an exception because default user preferences can't be written 
   * with this class.
   */
  @Override
  public void write() throws RecorderException {
    throw new RecorderException("Default user preferences can't be written");
  }

  /**
   * Throws an exception because default user preferences can't manage furniture libraries.
   */
  @Override
  public boolean furnitureLibraryExists(String name) throws RecorderException {
    throw new RecorderException("Default user preferences can't manage furniture libraries");
  }

  /**
   * Throws an exception because default user preferences can't manage additional furniture libraries.
   */
  @Override
  public void addFurnitureLibrary(String name) throws RecorderException {
    throw new RecorderException("Default user preferences can't manage furniture libraries");
  }
  
  /**
   * A fixed sized pattern.
   */
  private static class PatternTexture implements TextureImage {
    private final String name;
    private final Content image;

    public PatternTexture(String name) {
      this.name = name;
      this.image = new ResourceURLContent(PatternTexture.class, "resources/patterns/" + name + ".png");
    }

    public String getName() {
      return this.name;
    }

    public Content getImage() {
      return this.image;
    }
    
    public float getWidth() {
      return 10;
    }
    
    public float getHeight() {
      return 10;
    }
  }
}
