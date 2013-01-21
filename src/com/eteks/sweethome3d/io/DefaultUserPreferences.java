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
import com.eteks.sweethome3d.model.Library;
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
        : new FurnitureCatalog());
    // Read default textures catalog
    setTexturesCatalog(readCatalogs 
        ? new DefaultTexturesCatalog(localizedPreferences, (File)null)
        : new TexturesCatalog());
    // Build default patterns catalog
    List<TextureImage> patterns = new ArrayList<TextureImage>();
    patterns.add(new DefaultPatternTexture("foreground"));
    patterns.add(new DefaultPatternTexture("reversedHatchUp"));
    patterns.add(new DefaultPatternTexture("reversedHatchDown"));
    patterns.add(new DefaultPatternTexture("reversedCrossHatch"));
    patterns.add(new DefaultPatternTexture("background"));
    patterns.add(new DefaultPatternTexture("hatchUp"));
    patterns.add(new DefaultPatternTexture("hatchDown"));
    patterns.add(new DefaultPatternTexture("crossHatch"));
    PatternsCatalog patternsCatalog = new PatternsCatalog(patterns);
    setPatternsCatalog(patternsCatalog);
    // Read other preferences from resource bundle
    setFurnitureCatalogViewedInTree(Boolean.parseBoolean(
        localizedPreferences.getLocalizedString(DefaultUserPreferences.class, "furnitureCatalogViewedInTree")));
    setNavigationPanelVisible(Boolean.parseBoolean(localizedPreferences.getLocalizedString(DefaultUserPreferences.class, "navigationPanelVisible")));  
    setAerialViewCenteredOnSelectionEnabled(Boolean.parseBoolean(getOptionalLocalizedString(localizedPreferences, "aerialViewCenteredOnSelectionEnabled", "false")));
    setUnit(LengthUnit.valueOf(localizedPreferences.getLocalizedString(DefaultUserPreferences.class, "unit").toUpperCase(Locale.ENGLISH)));
    setRulersVisible(Boolean.parseBoolean(localizedPreferences.getLocalizedString(DefaultUserPreferences.class, "rulersVisible")));
    setGridVisible(Boolean.parseBoolean(localizedPreferences.getLocalizedString(DefaultUserPreferences.class, "gridVisible")));
    // Allow furnitureViewedFromTop and roomFloorColoredOrTextured to be different according to the running OS
    String osName = System.getProperty("os.name");
    setFurnitureViewedFromTop(Boolean.parseBoolean(getOptionalLocalizedString(localizedPreferences, "furnitureViewedFromTop." + osName, 
        localizedPreferences.getLocalizedString(DefaultUserPreferences.class, "furnitureViewedFromTop"))));
    setFloorColoredOrTextured(Boolean.parseBoolean(getOptionalLocalizedString(localizedPreferences, "roomFloorColoredOrTextured." + osName,
        localizedPreferences.getLocalizedString(DefaultUserPreferences.class, "roomFloorColoredOrTextured"))));
    setWallPattern(patternsCatalog.getPattern(localizedPreferences.getLocalizedString(DefaultUserPreferences.class, "wallPattern")));
    String newWallPattern = localizedPreferences.getLocalizedString(DefaultUserPreferences.class, "wallPattern");
    if (newWallPattern != null) {
      setNewWallPattern(patternsCatalog.getPattern(newWallPattern));
    }
    setNewWallThickness(Float.parseFloat(localizedPreferences.getLocalizedString(DefaultUserPreferences.class, "newWallThickness")));
    setNewWallHeight(Float.parseFloat(localizedPreferences.getLocalizedString(DefaultUserPreferences.class, "newHomeWallHeight")));
    setNewFloorThickness(Float.parseFloat(getOptionalLocalizedString(localizedPreferences, "newFloorThickness", "12")));
    setAutoSaveDelayForRecovery(Integer.parseInt(getOptionalLocalizedString(localizedPreferences, "autoSaveDelayForRecovery", "0")));
    setRecentHomes(new ArrayList<String>());
    setCurrency(getOptionalLocalizedString(localizedPreferences, "currency", null)); 
    for (String property : new String [] {"LevelName", "HomePieceOfFurnitureName", "RoomName", "LabelText"}) {
      String autoCompletionStringsList = getOptionalLocalizedString(localizedPreferences, "autoCompletionStrings#" + property, null);
      if (autoCompletionStringsList != null) {
        String [] autoCompletionStrings = autoCompletionStringsList.trim().split(",");
        if (autoCompletionStrings.length > 0) {
          for (int i = 0; i < autoCompletionStrings.length; i++) {
            autoCompletionStrings [i] = autoCompletionStrings [i].trim();
          }
          setAutoCompletionStrings(property, Arrays.asList(autoCompletionStrings));
        }
      }
    }
  }
  
  private String getOptionalLocalizedString(UserPreferences localizedPreferences, 
                                            String   resourceKey,
                                            String   defaultValue) {
    try {
      return localizedPreferences.getLocalizedString(DefaultUserPreferences.class, resourceKey);
    } catch (IllegalArgumentException ex) {
      return defaultValue;
    }
  }

  /**
   * Throws an exception because default user preferences can't be written 
   * with this class.
   */
  @Override
  public void write() throws RecorderException {
    throw new UnsupportedOperationException("Default user preferences can't be written");
  }

  /**
   * Throws an exception because default user preferences can't manage language libraries.
   */
  @Override
  public boolean languageLibraryExists(String name) throws RecorderException {
    throw new UnsupportedOperationException("Default user preferences can't manage language libraries");
  }

  /**
   * Throws an exception because default user preferences can't manage additional language libraries.
   */
  @Override
  public void addLanguageLibrary(String name) throws RecorderException {
    throw new UnsupportedOperationException("Default user preferences can't manage language libraries");
  }
  
  /**
   * Returns <code>false</code>.
   */
  @Override
  public boolean furnitureLibraryExists(String name) throws RecorderException {
    return false;
  }

  /**
   * Throws an exception because default user preferences can't manage additional furniture libraries.
   */
  @Override
  public void addFurnitureLibrary(String name) throws RecorderException {
    throw new UnsupportedOperationException("Default user preferences can't manage furniture libraries");
  }
  
  /**
   * Returns <code>false</code>.
   */
  @Override
  public boolean texturesLibraryExists(String name) throws RecorderException {
    return false;
  }

  /**
   * Throws an exception because default user preferences can't manage additional textures libraries.
   */
  @Override
  public void addTexturesLibrary(String name) throws RecorderException {
    throw new UnsupportedOperationException("Default user preferences can't manage textures libraries");
  }

  /**
   * Throws an exception because default user preferences don't support libraries.
   * @since 4.0
   */
  @Override
  public List<Library> getLibraries() throws RecorderException {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws an exception because default user preferences don't support libraries.
   * @since 4.0
   */
  @Override
  public void deleteLibraries(List<Library> libraries) throws RecorderException {
    throw new UnsupportedOperationException();
  }
  
  /**
   * Returns <code>false</code>.
   * @since 4.0
   */
  @Override
  public boolean isLibraryDeletable(Library library) throws RecorderException {
    return false;
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
