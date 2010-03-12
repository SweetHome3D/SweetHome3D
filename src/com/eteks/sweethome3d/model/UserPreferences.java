/*
 * UserPreferences.java 15 mai 2006
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
package com.eteks.sweethome3d.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * User preferences.
 * @author Emmanuel Puybaret
 */
public abstract class UserPreferences {
  /**
   * The properties of user preferences that may change. <code>PropertyChangeListener</code>s added 
   * to user preferences will be notified under a property name equal to the string value of one these properties.
   */
  public enum Property {LANGUAGE, UNIT, MAGNETISM_ENABLED, RULERS_VISIBLE, GRID_VISIBLE, 
                        FURNITURE_VIEWED_FROM_TOP, ROOM_FLOOR_COLORED_OR_TEXTURED, WALL_PATTERN,    
                        NEW_WALL_HEIGHT, NEW_WALL_THICKNESS, RECENT_HOMES, IGNORED_ACTION_TIP,
                        FURNITURE_CATALOG_VIEWED_IN_TREE, NAVIGATION_PANEL_VISIBLE}
  
  private static final String [] SUPPORTED_LANGUAGES; 
  private static final List<ClassLoader> DEFAULT_CLASS_LOADER = 
      Arrays.asList(new ClassLoader [] {UserPreferences.class.getClassLoader()});

  private static final TextStyle DEFAULT_TEXT_STYLE = new TextStyle(18f);
  private static final TextStyle DEFAULT_ROOM_TEXT_STYLE = new TextStyle(24f);

  static {
    ResourceBundle resource = ResourceBundle.getBundle(UserPreferences.class.getName());
    SUPPORTED_LANGUAGES = resource.getString("supportedLanguages").split("\\s");
  }
  
  private final PropertyChangeSupport          propertyChangeSupport;
  private final Map<Class<?>, ResourceBundle>  classResourceBundles;
  private final Map<String, ResourceBundle>    resourceBundles;

  private FurnitureCatalog furnitureCatalog;
  private TexturesCatalog  texturesCatalog;
  private PatternsCatalog  patternsCatalog;
  private final String     defaultCountry;
  private String           language;
  private String           currency;
  private LengthUnit       unit;
  private boolean          furnitureCatalogViewedInTree = true;
  private boolean          navigationPanelVisible = true;
  private boolean          magnetismEnabled    = true;
  private boolean          rulersVisible       = true;
  private boolean          gridVisible         = true;
  private boolean          furnitureViewedFromTop;
  private boolean          roomFloorColoredOrTextured;
  private TextureImage     wallPattern;
  private float            newWallThickness;
  private float            newWallHeight;
  private List<String>     recentHomes;

  public UserPreferences() {
    this.propertyChangeSupport = new PropertyChangeSupport(this);
    this.classResourceBundles = new HashMap<Class<?>, ResourceBundle>();
    this.resourceBundles = new HashMap<String, ResourceBundle>();

    this.defaultCountry = Locale.getDefault().getCountry();    
    String defaultLanguage = Locale.getDefault().getLanguage();
    // Find closest language among supported languages in Sweet Home 3D
    // For example, use simplified Chinese even for Chinese users (zh_?) not from China (zh_CN)
    // unless their exact locale is supported as in Taiwan (zh_TW)
    for (String supportedLanguage : SUPPORTED_LANGUAGES) {
      if (supportedLanguage.equals(defaultLanguage + "_" + this.defaultCountry)) {
        this.language = supportedLanguage;
        break; // Found the exact supported language
      } else if (this.language == null 
                 && supportedLanguage.startsWith(defaultLanguage)) {
        this.language = supportedLanguage; // Found a supported language
      }
    }
    // If no language was found, let's use English by default
    if (this.language == null) {
      this.language = "en";
    }
    updateDefaultLocale();
  }

  /**
   * Updates default locale from preferences language.
   */
  private void updateDefaultLocale() {
    int underscoreIndex = this.language.indexOf("_");
    if (underscoreIndex != -1) {
      Locale.setDefault(new Locale(this.language.substring(0, underscoreIndex), 
          this.language.substring(underscoreIndex + 1)));
    } else {
      Locale.setDefault(new Locale(this.language, this.defaultCountry));
    }
  }

  /**
   * Writes user preferences.
   * @throws RecorderException if user preferences couldn'y be saved.
   */
  public abstract void write() throws RecorderException;
  
  /**
   * Adds the <code>listener</code> in parameter to these preferences. 
   * <br>Caution: a user preferences instance generally exists during all the application ;
   * therefore you should take care of not bounding permanently listeners to this
   * object (for example, do not create anonymous listeners on user preferences
   * in classes depending on an edited home).
   */
  public void addPropertyChangeListener(Property property, 
                                        PropertyChangeListener listener) {
    this.propertyChangeSupport.addPropertyChangeListener(property.name(), listener);
  }

  /**
   * Removes the <code>listener</code> in parameter from these preferences.
   */
  public void removePropertyChangeListener(Property property, 
                                           PropertyChangeListener listener) {
    this.propertyChangeSupport.removePropertyChangeListener(property.name(), listener);
  }

  /**
   * Returns the furniture catalog.
   */
  public FurnitureCatalog getFurnitureCatalog() {
    return this.furnitureCatalog;
  }

  /**
   * Sets furniture catalog.
   */
  protected void setFurnitureCatalog(FurnitureCatalog catalog) {
    this.furnitureCatalog = catalog;
  }

  /**
   * Returns the textures catalog.
   */
  public TexturesCatalog getTexturesCatalog() {
    return this.texturesCatalog;
  }

  /**
   * Sets textures catalog.
   */
  protected void setTexturesCatalog(TexturesCatalog catalog) {
    this.texturesCatalog = catalog;
  }

  /**
   * Returns the patterns catalog available to fill plan areas. 
   */
  public PatternsCatalog getPatternsCatalog() {
    return this.patternsCatalog;
  }
  
  /**
   * Sets the patterns available to fill plan areas.
   */
  protected void setPatternsCatalog(PatternsCatalog catalog) {
    this.patternsCatalog = catalog;
  }

  /**
   * Returns the length unit currently in use.
   */
  public LengthUnit getLengthUnit() {
    return this.unit;
  }
  
  /**
   * Changes the unit currently in use, and notifies listeners of this change. 
   * @param unit one of the values of Unit.
   */
  public void setUnit(LengthUnit unit) {
    if (this.unit != unit) {
      LengthUnit oldUnit = this.unit;
      this.unit = unit;
      this.propertyChangeSupport.firePropertyChange(Property.UNIT.name(), oldUnit, unit);
    }
  }

  /**
   * Returns the preferred language to display information, noted with an ISO 639 code
   * that may be followed by an underscore and an ISO 3166 code. 
   */
  public String getLanguage() {
    return this.language;
  }

  /**
   * Sets the preferred language to display information, changes current default locale accordingly 
   * and notifies listeners of this change.
   * @param language an ISO 639 code that may be followed by an underscore and an ISO 3166 code
   *            (for example fr, de, it, en_US, zh_CN). 
   */
  public void setLanguage(String language) {
    if (!language.equals(this.language)) {
      String oldLanguage = this.language;
      this.language = language;      
      updateDefaultLocale();
      this.classResourceBundles.clear();
      this.resourceBundles.clear();
      this.propertyChangeSupport.firePropertyChange(Property.LANGUAGE.name(), 
          oldLanguage, language);
    }
  }

  /**
   * Returns the array of available languages in Sweet Home 3D.
   */
  public String [] getSupportedLanguages() {
    return SUPPORTED_LANGUAGES.clone();
  }

  /**
   * Returns the string matching <code>resourceKey</code> in current language in the 
   * context of <code>resourceClass</code>.
   * If <code>resourceParameters</code> isn't empty the string is considered
   * as a format string, and the returned string will be formatted with these parameters. 
   * This implementation searches first the key in a properties file named as 
   * <code>resourceClass</code>, then if this file doesn't exist, it searches 
   * the key prefixed by <code>resourceClass</code> name and a dot in a package.properties file 
   * in the directory matching the package of <code>resourceClass</code>. 
   * @exception IllegalArgumentException if no string for the given key can be found
   */
  public String getLocalizedString(Class<?> resourceClass,
                                   String   resourceKey, 
                                   Object ... resourceParameters) {
    ResourceBundle classResourceBundle = this.classResourceBundles.get(resourceClass);
    if (classResourceBundle == null) {
      try {      
        classResourceBundle = getResourceBundle(resourceClass.getName());
        this.classResourceBundles.put(resourceClass, classResourceBundle);
      } catch (IOException ex) {
        try {
          String className = resourceClass.getName();
          int lastIndex = className.lastIndexOf(".");
          String resourceFamily;
          if (lastIndex != -1) {
            resourceFamily = className.substring(0, lastIndex) + ".package";
          } else {
            resourceFamily = "package";
          }
          classResourceBundle = new PrefixedResourceBundle(getResourceBundle(resourceFamily), 
              resourceClass.getSimpleName() + ".");
          this.classResourceBundles.put(resourceClass, classResourceBundle);
        } catch (IOException ex2) {
          throw new IllegalArgumentException(
              "Can't find resource bundle for " + resourceClass, ex);
        }
      }
    } 

    return getLocalizedString(classResourceBundle, resourceKey, resourceParameters);
  }
  
  /**
   * Returns the string matching <code>resourceKey</code> in current language 
   * for the given resource family.
   * <code>resourceFamily</code> should match the absolute path of a .properties resource family,
   * shouldn't start by a slash and may contain dots '.' or slash '/' as directory separators. 
   * If <code>resourceParameters</code> isn't empty the string is considered
   * as a format string, and the returned string will be formatted with these parameters. 
   * This implementation searches the key in a properties file named as 
   * <code>resourceFamily</code>. 
   * @exception IllegalArgumentException if no string for the given key can be found
   * @since 2.3
   */
  public String getLocalizedString(String resourceFamily,
                                   String resourceKey, 
                                   Object ... resourceParameters) {
    try {      
      ResourceBundle resourceBundle = getResourceBundle(resourceFamily);
      return getLocalizedString(resourceBundle, resourceKey, resourceParameters);
    } catch (IOException ex) {
      throw new IllegalArgumentException(
          "Can't find resource bundle for " + resourceFamily, ex);
    }
  }

  /**
   * Returns a new resource bundle for the given <code>familyName</code> 
   * that matches current default locale. The search will be done
   * only among .properties files.
   * @throws IOException if no .properties file was found
   */
  private ResourceBundle getResourceBundle(String resourceFamily) throws IOException {
    resourceFamily = resourceFamily.replace('.', '/');
    ResourceBundle localizedResourceBundle = this.resourceBundles.get(resourceFamily);
    if (localizedResourceBundle != null) {
      return localizedResourceBundle;
    }
    Locale defaultLocale = Locale.getDefault();
    String language = defaultLocale.getLanguage();
    String country = defaultLocale.getCountry();
    ReparentableResourceBundle childResourceBundle = null;
    // First search resource bundle for language + country
    for (ClassLoader classLoader : getResourceClassLoaders()) {
      InputStream languageCountryProperties = 
          classLoader.getResourceAsStream(resourceFamily + "_" + language + "_" + country + ".properties");
      if (languageCountryProperties != null) {
        try {
          localizedResourceBundle =
          childResourceBundle = new ReparentableResourceBundle(languageCountryProperties);
          break;
        } finally {
          languageCountryProperties.close();
        }
      }
    }
    // Second search resource bundle for language
    for (ClassLoader classLoader : getResourceClassLoaders()) {
      InputStream languageProperties = 
          classLoader.getResourceAsStream(resourceFamily + "_" + language  + ".properties");
      if (languageProperties != null) {
        try {
          ReparentableResourceBundle resourceBundle = new ReparentableResourceBundle(languageProperties);
          if (childResourceBundle != null) {
            childResourceBundle.setParent(resourceBundle);
          } else {
            localizedResourceBundle = resourceBundle;
          }
          childResourceBundle = resourceBundle;
          break;
        } finally {
          languageProperties.close();
        }
      }
    }
    // Last search resource bundle for default language 
    for (ClassLoader classLoader : getResourceClassLoaders()) {
      InputStream defaultProperties = classLoader.getResourceAsStream(resourceFamily + ".properties");
      if (defaultProperties != null) {
        try {
          ReparentableResourceBundle resourceBundle = new ReparentableResourceBundle(defaultProperties);
          if (childResourceBundle != null) {
            childResourceBundle.setParent(resourceBundle);
          } else {
            localizedResourceBundle = resourceBundle;
          }
          break;
        } finally {
          defaultProperties.close();
        }
      }
    }
    if (localizedResourceBundle == null) {
      throw new IOException("No available resource bundle for " + resourceFamily);
    }
    this.resourceBundles.put(resourceFamily, localizedResourceBundle);
    return localizedResourceBundle;
  }

  /**
   * Returns the string matching <code>resourceKey</code> for the given resource bundle.
   */
  private String getLocalizedString(ResourceBundle resourceBundle, 
                                    String         resourceKey, 
                                    Object...      resourceParameters) {
    try {
      String localizedString = resourceBundle.getString(resourceKey);
      if (resourceParameters.length > 0) {
        localizedString = String.format(localizedString, resourceParameters);
      }      
      return localizedString;
    } catch (MissingResourceException ex) {
      throw new IllegalArgumentException("Unknown key " + resourceKey);
    }
  }

  /**
   * Returns the class loaders through which localized strings returned by 
   * {@link #getLocalizedString(Class, String, Object...) getLocalizedString} might be loaded.
   * @since 2.3
   */
  protected List<ClassLoader> getResourceClassLoaders() {
    return DEFAULT_CLASS_LOADER;
  }
  
  /**
   * Returns the currency in use, noted with ISO 4217 code, or <code>null</code> 
   * if prices aren't used in application.
   */
  public String getCurrency() {
    return this.currency;
  }

  /**
   * Sets currency in use.
   */
  protected void setCurrency(String currency) {
    this.currency = currency;
  }
    
  /**
   * Returns <code>true</code> if the furniture catalog should be viewed in a tree.
   * @since 2.3
   */
  public boolean isFurnitureCatalogViewedInTree() {
    return this.furnitureCatalogViewedInTree;
  }
  
  /**
   * Sets whether the furniture catalog should be viewed in a tree or a different way.
   * @since 2.3
   */
  public void setFurnitureCatalogViewedInTree(boolean furnitureCatalogViewedInTree) {
    if (this.furnitureCatalogViewedInTree != furnitureCatalogViewedInTree) {
      this.furnitureCatalogViewedInTree = furnitureCatalogViewedInTree;
      this.propertyChangeSupport.firePropertyChange(Property.FURNITURE_CATALOG_VIEWED_IN_TREE.name(), 
          !furnitureCatalogViewedInTree, furnitureCatalogViewedInTree);
    }
  }
  
  /**
   * Returns <code>true</code> if the navigation panel should be displayed.
   * @since 2.3
   */
  public boolean isNavigationPanelVisible() {
    return this.navigationPanelVisible;
  }
  
  /**
   * Sets whether the navigation panel should be displayed or not.
   * @since 2.3
   */
  public void setNavigationPanelVisible(boolean navigationPanelVisible) {
    if (this.navigationPanelVisible != navigationPanelVisible) {
      this.navigationPanelVisible = navigationPanelVisible;
      this.propertyChangeSupport.firePropertyChange(Property.NAVIGATION_PANEL_VISIBLE.name(), 
          !navigationPanelVisible, navigationPanelVisible);
    }
  }
  
  /**
   * Returns <code>true</code> if magnetism is enabled.
   * @return <code>true</code> by default.
   */
  public boolean isMagnetismEnabled() {
    return this.magnetismEnabled;
  }

  /**
   * Sets whether magnetism is enabled or not, and notifies
   * listeners of this change. 
   * @param magnetismEnabled <code>true</code> if magnetism is enabled,
   *          <code>false</code> otherwise.
   */
  public void setMagnetismEnabled(boolean magnetismEnabled) {
    if (this.magnetismEnabled != magnetismEnabled) {
      this.magnetismEnabled = magnetismEnabled;
      this.propertyChangeSupport.firePropertyChange(Property.MAGNETISM_ENABLED.name(), 
          !magnetismEnabled, magnetismEnabled);
    }
  }

  /**
   * Returns <code>true</code> if rulers are visible.
   * @return <code>true</code> by default.
   */
  public boolean isRulersVisible() {
    return this.rulersVisible;
  }

  /**
   * Sets whether rulers are visible or not, and notifies
   * listeners of this change. 
   * @param rulersVisible <code>true</code> if rulers are visible,
   *          <code>false</code> otherwise.
   */
  public void setRulersVisible(boolean rulersVisible) {
    if (this.rulersVisible != rulersVisible) {
      this.rulersVisible = rulersVisible;
      this.propertyChangeSupport.firePropertyChange(Property.RULERS_VISIBLE.name(), 
          !rulersVisible, rulersVisible);
    }
  }
  
  /**
   * Returns <code>true</code> if plan grid visible.
   * @return <code>true</code> by default.
   */
  public boolean isGridVisible() {
    return this.gridVisible;
  }
  
  /**
   * Sets whether plan grid is visible or not, and notifies
   * listeners of this change. 
   * @param gridVisible <code>true</code> if grid is visible,
   *          <code>false</code> otherwise.
   */
  public void setGridVisible(boolean gridVisible) {
    if (this.gridVisible != gridVisible) {
      this.gridVisible = gridVisible;
      this.propertyChangeSupport.firePropertyChange(Property.GRID_VISIBLE.name(), 
          !gridVisible, gridVisible);
    }
  }

  /**
   * Returns <code>true</code> if furniture should be viewed from its top in plan.
   * @since 2.0
   */
  public boolean isFurnitureViewedFromTop() {
    return this.furnitureViewedFromTop;
  }
  
  /**
   * Sets how furniture icon should be displayed in plan, and notifies
   * listeners of this change. 
   * @param furnitureViewedFromTop if <code>true</code> the furniture 
   *    should be viewed from its top.
   * @since 2.0
   */
  public void setFurnitureViewedFromTop(boolean furnitureViewedFromTop) {
    if (this.furnitureViewedFromTop != furnitureViewedFromTop) {
      this.furnitureViewedFromTop = furnitureViewedFromTop;
      this.propertyChangeSupport.firePropertyChange(Property.FURNITURE_VIEWED_FROM_TOP.name(), 
          !furnitureViewedFromTop, furnitureViewedFromTop);
    }
  }

  /**
   * Returns <code>true</code> if room floors should be rendered with color or texture 
   * in plan.
   * @return <code>false</code> by default.
   * @since 2.0
   */
  public boolean isRoomFloorColoredOrTextured() {
    return this.roomFloorColoredOrTextured;
  }
  
  /**
   * Sets whether room floors should be rendered with color or texture, 
   * and notifies listeners of this change. 
   * @param roomFloorColoredOrTextured <code>true</code> if floor color 
   *          or texture is used, <code>false</code> otherwise.
   * @since 2.0
   */
  public void setFloorColoredOrTextured(boolean roomFloorColoredOrTextured) {
    if (this.roomFloorColoredOrTextured != roomFloorColoredOrTextured) {
      this.roomFloorColoredOrTextured = roomFloorColoredOrTextured;
      this.propertyChangeSupport.firePropertyChange(Property.ROOM_FLOOR_COLORED_OR_TEXTURED.name(), 
          !roomFloorColoredOrTextured, roomFloorColoredOrTextured);
    }
  }

  /**
   * Returns the wall pattern in plan.
   * @since 2.0
   */
  public TextureImage getWallPattern() {
    return this.wallPattern;
  }
  
  /**
   * Sets how walls should be displayed in plan, and notifies
   * listeners of this change.
   * @since 2.0 
   */
  public void setWallPattern(TextureImage wallPattern) {
    if (this.wallPattern != wallPattern) {
      TextureImage oldWallPattern = this.wallPattern;
      this.wallPattern = wallPattern;
      this.propertyChangeSupport.firePropertyChange(Property.WALL_PATTERN.name(), 
          oldWallPattern, wallPattern);
    }
  }

  /**
   * Returns default thickness of new walls in home. 
   */
  public float getNewWallThickness() {
    return this.newWallThickness;
  }

  /**
   * Sets default thickness of new walls in home, and notifies
   * listeners of this change.  
   */
  public void setNewWallThickness(float newWallThickness) {
    if (this.newWallThickness != newWallThickness) {
      float oldDefaultThickness = this.newWallThickness;
      this.newWallThickness = newWallThickness;
      this.propertyChangeSupport.firePropertyChange(Property.NEW_WALL_THICKNESS.name(), 
          oldDefaultThickness, newWallThickness);
    }
  }

  /**
   * Returns default wall height of new home walls. 
   */
  public float getNewWallHeight() {
    return this.newWallHeight;
  }

  /**
   * Sets default wall height of new walls, and notifies
   * listeners of this change. 
   */
  public void setNewWallHeight(float newWallHeight) {
    if (this.newWallHeight != newWallHeight) {
      float oldWallHeight = this.newWallHeight;
      this.newWallHeight = newWallHeight;
      this.propertyChangeSupport.firePropertyChange(Property.NEW_WALL_HEIGHT.name(), 
          oldWallHeight, newWallHeight);
    }
  }
  
  /**
   * Returns an unmodifiable list of the recent homes.
   */
  public List<String> getRecentHomes() {
    return Collections.unmodifiableList(this.recentHomes);
  }

  /**
   * Sets the recent homes list and notifies listeners of this change.
   */
  public void setRecentHomes(List<String> recentHomes) {
    if (!recentHomes.equals(this.recentHomes)) {
      List<String> oldRecentHomes = this.recentHomes;
      this.recentHomes = new ArrayList<String>(recentHomes);
      this.propertyChangeSupport.firePropertyChange(Property.RECENT_HOMES.name(), 
          oldRecentHomes, getRecentHomes());
    }
  }
  
  /**
   * Sets which action tip should be ignored.
   * <br>This method should be overridden to store the ignore information.
   * By default it just notifies listeners of this change. 
   */
  public void setActionTipIgnored(String actionKey) {    
    this.propertyChangeSupport.firePropertyChange(Property.IGNORED_ACTION_TIP.name(), null, actionKey);
  }
  
  /**
   * Returns whether an action tip should be ignored or not. 
   * <br>This method should be overridden to return the the display information
   * stored in {@link #setActionTipIgnored(String) setActionTipDisplayed}.
   * By default it returns <code>true</code>. 
   */
  public boolean isActionTipIgnored(String actionKey) {
    return true;
  }
  
  /**
   * Resets the ignore flag of action tips.
   * <br>This method should be overridden to clear all the display flags.
   * By default it just notifies listeners of this change. 
   */
  public void resetIgnoredActionTips() {    
    this.propertyChangeSupport.firePropertyChange(Property.IGNORED_ACTION_TIP.name(), null, null);
  }
  
  /**
   * Returns the default text style of a class of selectable item. 
   */
  public TextStyle getDefaultTextStyle(Class<? extends Selectable> selectableClass) {
    if (Room.class.isAssignableFrom(selectableClass)) {
      return DEFAULT_ROOM_TEXT_STYLE;
    } else {
      return DEFAULT_TEXT_STYLE;
    }
  }

  /**
   * Adds <code>languageLibraryName</code> to language catalog  
   * to make the language library it contains available available to supported languages.
   * @param languageLibraryName  the name of the resource in which the library will be written. 
   * @since 2.3 
   */
  public abstract void addLanguageLibrary(String languageLibraryName) throws RecorderException;
  
  /**
   * Returns <code>true</code> if the given language library exists.
   * @param languageLibraryName the name of the resource to check
   * @since 2.3 
   */
  public abstract boolean languageLibraryExists(String languageLibraryName) throws RecorderException;

  /**
   * Adds <code>furnitureLibraryName</code> to furniture catalog  
   * to make the furniture library it contains available.
   * @param furnitureLibraryName  the name of the resource in which the library will be written. 
   */
  public abstract void addFurnitureLibrary(String furnitureLibraryName) throws RecorderException;
  
  /**
   * Returns <code>true</code> if the given furniture library exists.
   * @param furnitureLibraryName the name of the resource to check
   */
  public abstract boolean furnitureLibraryExists(String furnitureLibraryName) throws RecorderException;

  /**
   * Adds <code>texturesLibraryName</code> to textures catalog  
   * to make the textures library it contains available.
   * @param texturesLibraryName  the name of the resource in which the library will be written.
   * @since 2.3 
   */
  public abstract void addTexturesLibrary(String texturesLibraryName) throws RecorderException;
  
  /**
   * Returns <code>true</code> if the given textures library exists.
   * @param texturesLibraryName the name of the resource to check
   * @since 2.3 
   */
  public abstract boolean texturesLibraryExists(String texturesLibraryName) throws RecorderException;

  /**
   * A reparentable resource bundle.
   */
  private static class ReparentableResourceBundle extends PropertyResourceBundle {
    public ReparentableResourceBundle(InputStream inputStream) throws IOException {
      super(inputStream);
    }
    
    // Increase <code>setParent</code> visibility. 
    @Override
    public void setParent(ResourceBundle parent) {
      super.setParent(parent);
    }    
  }

  /**
   * A resource bundle with a prefix added to resource key.
   */
  private static class PrefixedResourceBundle extends ResourceBundle {
    private ResourceBundle resourceBundle;
    private String         keyPrefix;

    public PrefixedResourceBundle(ResourceBundle resourceBundle, 
                                  String keyPrefix) {
      this.resourceBundle = resourceBundle;
      this.keyPrefix = keyPrefix;
    }
    
    @Override
    public Locale getLocale() {
      return this.resourceBundle.getLocale();
    }
    
    @Override
    protected Object handleGetObject(String key) {
      key = this.keyPrefix + key;
      return this.resourceBundle.getObject(key);
    }    

    @Override
    public Enumeration<String> getKeys() {
      return this.resourceBundle.getKeys();
    }    
  }
}
