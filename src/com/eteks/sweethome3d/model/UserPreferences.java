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
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * User preferences.
 * @author Emmanuel Puybaret
 */
public abstract class UserPreferences {
  public enum Property {LANGUAGE, UNIT, MAGNETISM_ENABLED, RULERS_VISIBLE, GRID_VISIBLE, 
                        NEW_WALL_HEIGHT, NEW_WALL_THICKNESS, RECENT_HOMES}
  private static final String [] SUPPORTED_LANGUAGES = {"cs", "de", "en", "es", "fr", "it", "hu", "pl", "pt", "sv", "ru"}; 
  
  private PropertyChangeSupport propertyChangeSupport;

  /**
   * Unit used for sizes.
   */
  public enum Unit {
    CENTIMETER {
      private Locale        formatLocale;  
      private String        name;
      private DecimalFormat formatWithUnit;
      private DecimalFormat format;
      
      @Override
      public Format getLengthFormatWithUnit() {
        checkLocaleChange();
        return this.formatWithUnit;
      }

      @Override
      public Format getLengthFormat() {
        checkLocaleChange();
        return this.format;
      }
      
      @Override
      public String getName() {
        checkLocaleChange();
        return this.name;
      }
      
      private void checkLocaleChange() {
        // Instantiate formats if locale changed
        if (!Locale.getDefault().equals(this.formatLocale)) {
          this.formatLocale = Locale.getDefault();  
          ResourceBundle resource = ResourceBundle.getBundle(UserPreferences.class.getName());
          this.name = resource.getString("centimerUnit");
          this.formatWithUnit = new DecimalFormat("#,##0.# " + this.name);          
          this.format = new DecimalFormat("#,##0.#");          
        }
      }

      @Override
      public float getMagnetizedLength(float length, float maxDelta) {
        // Use a maximum precision of 1 mm depending on maxDelta
        maxDelta *= 2;
        float precision = 1 / 10f;
        if (maxDelta > 100) {
          precision = 100;
        } else if (maxDelta > 10) {
          precision = 10;
        } else if (maxDelta > 5) {
          precision = 5;
        } else if (maxDelta > 1) {
          precision = 1;
        } else if  (maxDelta > 0.5f) {
          precision = 0.5f;
        } 
        return Math.round(length / precision) * precision;
      }

      @Override
      public float getMinimumLength() {
        return 0.1f;
      }
    }, 
    
    INCH {
      private Locale        formatLocale;
      private String        name;
      private DecimalFormat inchFormat;
      private final char [] fractionCharacters = {'\u215b',   // 1/8
                                                  '\u00bc',   // 1/4  
                                                  '\u215c',   // 3/8
                                                  '\u00bd',   // 1/2
                                                  '\u215d',   // 5/8
                                                  '\u00be',   // 3/4
                                                  '\u215e'};  // 7/8
      
      @Override
      public Format getLengthFormatWithUnit() {
        checkLocaleChange();
        return this.inchFormat;
      }

      @Override
      public Format getLengthFormat() {
        return getLengthFormatWithUnit();
      }

      
      @Override
      public String getName() {
        checkLocaleChange();
        return this.name;
      }
      
      private void checkLocaleChange() {
        // Instantiate format if locale changed
        if (!Locale.getDefault().equals(this.formatLocale)) {
          this.formatLocale = Locale.getDefault();  
          ResourceBundle resource = ResourceBundle.getBundle(UserPreferences.class.getName());
          this.name = resource.getString("inchUnit");
          
          // Create format for feet and inches
          final Format footFormat = new DecimalFormat("#,##0''");
          this.inchFormat = new DecimalFormat("0.000\"") {            
            @Override
            public StringBuffer format(double number, StringBuffer result,
                                       FieldPosition fieldPosition) {
              float feet = (float)Math.floor(centimeterToFoot((float)number));              
              float remainingInches = centimeterToInch((float)number - feetToCentimeter(feet));
              if (remainingInches >= 11.9995f) {
                feet++;
                remainingInches -= 12;
              }
              footFormat.format(feet, result, fieldPosition);
              // Format remaining inches only if it's larger that 0.0005
              if (remainingInches >= 0.0005f) {
                // Try to format decimals with 1/8, 1/4, 1/2 fractions first
                int integerPart = (int)Math.floor(remainingInches);
                float fractionPart = remainingInches - integerPart;
                float remainderToClosestEighth = fractionPart % 0.125f;
                if (remainderToClosestEighth <= 0.0005f || remainderToClosestEighth >= 0.1245f) {
                  int eighth = Math.round(fractionPart * 8); 
                  String remainingInchesString;
                  if (eighth == 0 || eighth == 8) {
                    remainingInchesString = Math.round(remainingInches) + "\"";
                  } else {
                    remainingInchesString = String.valueOf(integerPart) + fractionCharacters [eighth - 1] + "\"";
                  }
                  result.append(remainingInchesString);
                  fieldPosition.setEndIndex(fieldPosition.getEndIndex() + remainingInchesString.length());
                } else {                
                  super.format(remainingInches, result, fieldPosition);
                }
              }
              return result;
            }
          };
        }
      }
      
      @Override
      public float getMagnetizedLength(float length, float maxDelta) {
        // Use a maximum precision of 1/8 inch depending on maxDelta
        maxDelta = centimeterToInch(maxDelta) * 2;
        float precision = 1 / 8f;
        if (maxDelta > 6) {
          precision = 6;
        } else if (maxDelta > 3) {
          precision = 3;
        } else if (maxDelta > 1) {
          precision = 1;
        } else if  (maxDelta > 0.5f) {
          precision = 0.5f;
        } else if  (maxDelta > 0.25f) {
          precision = 0.25f;
        }
        return inchToCentimeter(Math.round(centimeterToInch(length) / precision) * precision);
      }

      @Override
      public float getMinimumLength() {        
        return UserPreferences.Unit.inchToCentimeter(0.125f);
      }
    };

    public static float centimeterToInch(float length) {
      return length / 2.54f;
    }

    public static float centimeterToFoot(float length) {
      return length / 2.54f / 12;
    }
    
    public static float inchToCentimeter(float length) {
      return length * 2.54f;
    }
    
    public static float feetToCentimeter(float length) {
      return length * 2.54f * 12;
    }
    
    /**
     * Returns a format able to format lengths with their localized unit.
     */
    public abstract Format getLengthFormatWithUnit(); 

    /**
     * Returns a format able to format lengths.
     */
    public abstract Format getLengthFormat(); 

    /**
     * Returns a localized name of this unit.
     */
    public abstract String getName();
    
    /**
     * Returns the value close to the given length under magnetism. 
     */
    public abstract float getMagnetizedLength(float length, float maxDelta);

    /**
     * Returns the minimum value for length.
     */
    public abstract float getMinimumLength();
  }

  private FurnitureCatalog furnitureCatalog;
  private TexturesCatalog  texturesCatalog;
  private String           language;
  private Unit             unit;
  private boolean          magnetismEnabled = true;
  private boolean          rulersVisible    = true;
  private boolean          gridVisible      = true;
  private float            newWallThickness;
  private float            newWallHeight;
  private List<String>     recentHomes;

  public UserPreferences() {
    this.propertyChangeSupport = new PropertyChangeSupport(this);
    
    final Locale defaultLocale = Locale.getDefault();
    this.language = defaultLocale.getLanguage();
    // If current default locale isn't supported in Sweet Home 3D, 
    // let's use English as default language
    if (!Arrays.asList(SUPPORTED_LANGUAGES).contains(this.language)) {
      this.language = "en";
    }
    Locale.setDefault(new Locale(this.language, defaultLocale.getCountry()));
  }
  
  /**
   * Writes user preferences.
   * @throws RecorderException if user preferences couldn'y be saved.
   */
  public abstract void write() throws RecorderException;
  
  /**
   * Adds the <code>listener</code> in parameter to these preferences. 
   */
  public void addPropertyChangeListener(Property property, 
                                        PropertyChangeListener listener) {
    this.propertyChangeSupport.addPropertyChangeListener(property.toString(), listener);
  }

  /**
   * Removes the <code>listener</code> in parameter from these preferences.
   */
  public void removePropertyChangeListener(Property property, 
                                           PropertyChangeListener listener) {
    this.propertyChangeSupport.removePropertyChangeListener(property.toString(), listener);
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
   * Returns the unit currently in use.
   */
  public Unit getUnit() {
    return this.unit;
  }
  
  /**
   * Returns the preferred language to display information, noted with ISO 639 code. 
   */
  public String getLanguage() {
    return this.language;
  }

  /**
   * Sets the preferred language to display information, changes current default locale accordingly 
   * and notifies listeners of this change.
   */
  public void setLanguage(String language) {
    if (!language.equals(this.language)) {
      String oldLanguage = this.language;
      this.language = language;      
      Locale.setDefault(new Locale(language, Locale.getDefault().getCountry()));
      this.propertyChangeSupport.firePropertyChange(Property.LANGUAGE.toString(), 
          oldLanguage, language);
    }
  }

  /**
   * Returns the array of available languages in Sweet Home 3D.
   */
  public String [] getSupportedLanguages() {
    return SUPPORTED_LANGUAGES;
  }

  /**
   * Changes the unit currently in use, and notifies listeners of this change. 
   * @param unit one of the values of Unit.
   */
  public void setUnit(Unit unit) {
    if (this.unit != unit) {
      Unit oldUnit = this.unit;
      this.unit = unit;
      this.propertyChangeSupport.firePropertyChange(Property.UNIT.toString(), oldUnit, unit);
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
      this.propertyChangeSupport.firePropertyChange(Property.MAGNETISM_ENABLED.toString(), 
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
      this.propertyChangeSupport.firePropertyChange(Property.RULERS_VISIBLE.toString(), 
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
      this.propertyChangeSupport.firePropertyChange(Property.GRID_VISIBLE.toString(), 
          !gridVisible, gridVisible);
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
      this.propertyChangeSupport.firePropertyChange(Property.NEW_WALL_THICKNESS.toString(), 
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
      this.propertyChangeSupport.firePropertyChange(Property.NEW_WALL_HEIGHT.toString(), 
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
      this.propertyChangeSupport.firePropertyChange(Property.RECENT_HOMES.toString(), 
          oldRecentHomes, getRecentHomes());
    }
  }
}
