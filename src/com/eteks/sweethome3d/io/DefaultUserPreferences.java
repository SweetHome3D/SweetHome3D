/*
 * DefaultUserPreferences.java 15 mai 2006
 *
 * Sweet Home 3D, Copyright (c) 2006 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.FurnitureCatalog;
import com.eteks.sweethome3d.model.LengthUnit;
import com.eteks.sweethome3d.model.PatternsCatalog;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.TextureImage;
import com.eteks.sweethome3d.model.TexturesCatalog;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.ResourceURLContent;

/**
 * Default user preferences.
 * @author Emmanuel Puybaret
 */
public class DefaultUserPreferences extends UserPreferences {
  /**
   * Creates default user preferences read from resource files in the default language.
   */
  public DefaultUserPreferences() {
    this(true, null);
  }
  
  /**
   * Creates default user preferences read from resource files.
   * @param readCatalogs          if <code>false</code> furniture and texture catalog won't be read
   * @param localizedPreferences  preferences used to read localized resource files
   */
  DefaultUserPreferences(boolean readCatalogs,
                         UserPreferences localizedPreferences) {
    if (localizedPreferences == null) {
      localizedPreferences = this;
    } else {
      setLanguage(localizedPreferences.getLanguage());
    }
    // Read default furniture catalog
    setFurnitureCatalog(readCatalogs 
        ? new DefaultFurnitureCatalog(localizedPreferences, (File)null) 
        : new FurnitureCatalog() { });
    // Read default textures catalog
    setTexturesCatalog(readCatalogs 
        ? new DefaultTexturesCatalog(localizedPreferences, (File)null)
        : new TexturesCatalog() { });
    // Build default patterns catalog
    List<TextureImage> patterns = new ArrayList<TextureImage>();
    patterns.add(new DefaultPatternTexture("foreground"));
    patterns.add(new DefaultPatternTexture("hatchUp"));
    patterns.add(new DefaultPatternTexture("hatchDown"));
    patterns.add(new DefaultPatternTexture("crossHatch"));
    patterns.add(new DefaultPatternTexture("background"));
    PatternsCatalog patternsCatalog = new PatternsCatalog(patterns);
    setPatternsCatalog(patternsCatalog);
    // Read other preferences from resource bundle
    setFurnitureCatalogViewedInTree(Boolean.parseBoolean(
        localizedPreferences.getLocalizedString(DefaultUserPreferences.class, "furnitureCatalogViewedInTree")));
    setNavigationPanelVisible(Boolean.parseBoolean(localizedPreferences.getLocalizedString(DefaultUserPreferences.class, "navigationPanelVisible")));    
    setUnit(LengthUnit.valueOf(localizedPreferences.getLocalizedString(DefaultUserPreferences.class, "unit").toUpperCase(Locale.ENGLISH)));
    setRulersVisible(Boolean.parseBoolean(localizedPreferences.getLocalizedString(DefaultUserPreferences.class, "rulersVisible")));
    setGridVisible(Boolean.parseBoolean(localizedPreferences.getLocalizedString(DefaultUserPreferences.class, "gridVisible")));
    setFurnitureViewedFromTop(Boolean.parseBoolean(localizedPreferences.getLocalizedString(DefaultUserPreferences.class, "furnitureViewedFromTop")));
    setFloorColoredOrTextured(Boolean.parseBoolean(localizedPreferences.getLocalizedString(DefaultUserPreferences.class, "roomFloorColoredOrTextured")));
    setWallPattern(patternsCatalog.getPattern(localizedPreferences.getLocalizedString(DefaultUserPreferences.class, "wallPattern")));
    setNewWallThickness(Float.parseFloat(localizedPreferences.getLocalizedString(DefaultUserPreferences.class, "newWallThickness")));
    setNewWallHeight(Float.parseFloat(localizedPreferences.getLocalizedString(DefaultUserPreferences.class, "newHomeWallHeight")));
    try {
      setNewFloorThickness(Float.parseFloat(localizedPreferences.getLocalizedString(DefaultUserPreferences.class, "newFloorThickness")));
    } catch (IllegalArgumentException ex) {
      setNewFloorThickness(12);
    }
    try {
      setAutoSaveDelayForRecovery(Integer.parseInt(localizedPreferences.getLocalizedString(DefaultUserPreferences.class, "autoSaveDelayForRecovery")));
    } catch (IllegalArgumentException ex) {
      // Disable auto save
      setAutoSaveDelayForRecovery(0);
    }
    setRecentHomes(new ArrayList<String>());
    try {
      setCurrency(localizedPreferences.getLocalizedString(DefaultUserPreferences.class, "currency"));
    } catch (IllegalArgumentException ex) {
      // Don't use currency and prices in program
    }
    for (String property : new String [] {"LevelName", "HomePieceOfFurnitureName", "RoomName", "LabelText"}) {
      try {
        String [] autoCompletionStrings = localizedPreferences.getLocalizedString(DefaultUserPreferences.class, "autoCompletionStrings#" + property).trim().split(",");
        if (autoCompletionStrings.length > 0) {
          for (int i = 0; i < autoCompletionStrings.length; i++) {
            autoCompletionStrings [i] = autoCompletionStrings [i].trim();
          }
          setAutoCompletionStrings(property, Arrays.asList(autoCompletionStrings));
        }
      } catch (IllegalArgumentException ex) {
        // No default auto completion strings
      }
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
   * Throws an exception because default user preferences can't manage language libraries.
   */
  @Override
  public boolean languageLibraryExists(String name) throws RecorderException {
    throw new RecorderException("Default user preferences can't manage language libraries");
  }

  /**
   * Throws an exception because default user preferences can't manage additional language libraries.
   */
  @Override
  public void addLanguageLibrary(String name) throws RecorderException {
    throw new RecorderException("Default user preferences can't manage language libraries");
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
   * Throws an exception because default user preferences can't manage textures libraries.
   */
  @Override
  public boolean texturesLibraryExists(String name) throws RecorderException {
    throw new RecorderException("Default user preferences can't manage textures libraries");
  }

  /**
   * Throws an exception because default user preferences can't manage additional textures libraries.
   */
  @Override
  public void addTexturesLibrary(String name) throws RecorderException {
    throw new RecorderException("Default user preferences can't manage textures libraries");
  }
}

/**
 * A pattern built from resources.
 * @since 3.3
 */
class DefaultPatternTexture implements TextureImage {
  private static final long serialVersionUID = 1L;

  private final String      name;
  private transient Content image;
  
  public DefaultPatternTexture(String name) {
    this.name = name;
    this.image = new ResourceURLContent(DefaultPatternTexture.class, "resources/patterns/" + this.name + ".png");
  }
  
  /**
   * Initializes transient fields and reads pattern from <code>in</code> stream with default reading method.
   */
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    try {
      this.image = new ResourceURLContent(DefaultPatternTexture.class, "resources/patterns/" + this.name + ".png");
    } catch (IllegalArgumentException ex) {
      this.image = new ResourceURLContent(DefaultPatternTexture.class, "resources/patterns/foreground.png");
    }
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

  /**
   * Returns <code>true</code> if the object in parameter is equal to this pattern.
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (obj instanceof DefaultPatternTexture) {
      DefaultPatternTexture pattern = (DefaultPatternTexture)obj;
      return pattern.name.equals(this.name);
    } else {
      return false;
    }
  }
  
  /**
   * Returns a hash code for this pattern.
   */
  @Override
  public int hashCode() {
    return this.name.hashCode();
  }
}
