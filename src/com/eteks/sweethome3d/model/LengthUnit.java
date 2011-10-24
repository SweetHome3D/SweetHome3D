/*
 * LengthUnit.java 22 nov. 2008
 *
 * Sweet Home 3D, Copyright (c) 2006-2008 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Unit used for lengths.
 */
public enum LengthUnit {
  /**
   * Centimeter unit.
   */
  CENTIMETER {
    private Locale        formatLocale;  
    private String        name;
    private DecimalFormat lengthFormatWithUnit;
    private DecimalFormat lengthFormat;
    private DecimalFormat areaFormatWithUnit;
    
    @Override
    public Format getFormatWithUnit() {
      checkLocaleChange();
      return this.lengthFormatWithUnit;
    }

    @Override
    public Format getAreaFormatWithUnit() {
      checkLocaleChange();
      return this.areaFormatWithUnit;
    }

    @Override
    public Format getFormat() {
      checkLocaleChange();
      return this.lengthFormat;
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
        ResourceBundle resource = ResourceBundle.getBundle(LengthUnit.class.getName());
        this.name = resource.getString("centimeterUnit");
        this.lengthFormatWithUnit = new MeterFamilyFormat("#,##0.# " + this.name, 1);          
        this.lengthFormat = new MeterFamilyFormat("#,##0.#", 1);
        String squareMeterUnit = resource.getString("squareMeterUnit");
        this.areaFormatWithUnit = new SquareMeterAreaFormatWithUnit(squareMeterUnit);
      }
    }

    @Override
    public float getMagnetizedLength(float length, float maxDelta) {
      return getMagnetizedMeterLength(length, maxDelta);
    }

    @Override
    public float getMinimumLength() {
      return 0.1f;
    }
    
    @Override
    public float centimeterToUnit(float length) {
      return length;
    }

    @Override
    public float unitToCentimeter(float length) {
      return length;
    }
  }, 
  
  /**
   * Millimeter unit.
   * @since 2.0
   */
  MILLIMETER {
    private Locale        formatLocale;  
    private String        name;
    private DecimalFormat lengthFormatWithUnit;
    private DecimalFormat lengthFormat;
    private DecimalFormat areaFormatWithUnit;
    
    @Override
    public Format getFormatWithUnit() {
      checkLocaleChange();
      return this.lengthFormatWithUnit;
    }

    @Override
    public Format getAreaFormatWithUnit() {
      checkLocaleChange();
      return this.areaFormatWithUnit;
    }

    @Override
    public Format getFormat() {
      checkLocaleChange();
      return this.lengthFormat;
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
        ResourceBundle resource = ResourceBundle.getBundle(LengthUnit.class.getName());
        this.name = resource.getString("millimeterUnit");
        this.lengthFormatWithUnit = new MeterFamilyFormat("#,##0 " + this.name, 10);          
        this.lengthFormat = new MeterFamilyFormat("#,##0", 10);
        String squareMeterUnit = resource.getString("squareMeterUnit");
        this.areaFormatWithUnit = new SquareMeterAreaFormatWithUnit(squareMeterUnit);
      }
    }

    @Override
    public float getMagnetizedLength(float length, float maxDelta) {
      return getMagnetizedMeterLength(length, maxDelta);
    }

    @Override
    public float getMinimumLength() {
      return 0.1f;
    }
    
    @Override
    public float centimeterToUnit(float length) {
      return length * 10;
    }

    @Override
    public float unitToCentimeter(float length) {
      return length / 10;
    }
  }, 
  
  /**
   * Meter unit.
   * @since 2.0
   */
  METER {
    private Locale        formatLocale;  
    private String        name;
    private DecimalFormat lengthFormatWithUnit;
    private DecimalFormat lengthFormat;
    private DecimalFormat areaFormatWithUnit;
    
    @Override
    public Format getFormatWithUnit() {
      checkLocaleChange();
      return this.lengthFormatWithUnit;
    }

    @Override
    public Format getAreaFormatWithUnit() {
      checkLocaleChange();
      return this.areaFormatWithUnit;
    }

    @Override
    public Format getFormat() {
      checkLocaleChange();
      return this.lengthFormat;
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
        ResourceBundle resource = ResourceBundle.getBundle(LengthUnit.class.getName());
        this.name = resource.getString("meterUnit");
        this.lengthFormatWithUnit = new MeterFamilyFormat("#,##0.00# " + this.name, 0.01f);          
        this.lengthFormat = new MeterFamilyFormat("#,##0.00#", 0.01f);
        String squareMeterUnit = resource.getString("squareMeterUnit");
        this.areaFormatWithUnit = new SquareMeterAreaFormatWithUnit(squareMeterUnit);
      }
    }

    @Override
    public float getMagnetizedLength(float length, float maxDelta) {
      return getMagnetizedMeterLength(length, maxDelta);
    }

    @Override
    public float getMinimumLength() {
      return 0.1f;
    }

    @Override
    public float centimeterToUnit(float length) {
      return length / 100;
    }

    @Override
    public float unitToCentimeter(float length) {
      return length * 100;
    }
  }, 
  
  /**
   * Inch unit.
   */
  INCH {
    private Locale        formatLocale;
    private String        name;
    private DecimalFormat lengthFormat;
    private DecimalFormat areaFormatWithUnit;

    @Override
    public Format getFormatWithUnit() {
      checkLocaleChange();
      return this.lengthFormat;
    }

    @Override
    public Format getFormat() {
      return getFormatWithUnit();
    }

    @Override
    public Format getAreaFormatWithUnit() {
      checkLocaleChange();
      return this.areaFormatWithUnit;
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
        ResourceBundle resource = ResourceBundle.getBundle(LengthUnit.class.getName());
        this.name = resource.getString("inchUnit");
        
        // Create format for feet and inches
        final MessageFormat footFormat = new MessageFormat(resource.getString("footFormat"));
        final MessageFormat footInchFormat = new MessageFormat(resource.getString("footInchFormat"));
        final MessageFormat footInchEighthFormat = new MessageFormat(resource.getString("footInchEighthFormat"));
        final String        footInchSeparator = resource.getString("footInchSeparator");
        final boolean       inchDecimalsRoundedToClosestHeighth = 
            resource.getString("inchDecimalsRoundedToClosestHeighth").toLowerCase().equals("true");        
        final NumberFormat  footNumberFormat = NumberFormat.getIntegerInstance();
        final NumberFormat  inchNumberFormat = NumberFormat.getNumberInstance();
        final char [] inchFractionCharacters = {'\u215b',   // 1/8
                                                '\u00bc',   // 1/4  
                                                '\u215c',   // 3/8
                                                '\u00bd',   // 1/2
                                                '\u215d',   // 5/8
                                                '\u00be',   // 3/4
                                                '\u215e'};  // 7/8        
        this.lengthFormat = new DecimalFormat("0.000\"") {
            @Override
            public StringBuffer format(double number, StringBuffer result,
                                       FieldPosition fieldPosition) {
              double feet = Math.floor(centimeterToFoot((float)number));              
              float remainingInches = centimeterToInch((float)number - footToCentimeter((float)feet));
              if (remainingInches >= 11.9995f
                  || (inchDecimalsRoundedToClosestHeighth
                      && remainingInches >= 11.9375f)) {
                feet++;
                remainingInches -= 12;
              }
              fieldPosition.setEndIndex(fieldPosition.getEndIndex() + 1);
              // Format remaining inches only if it's larger that 0.0005
              if (remainingInches >= 0.0005f) {
                // Try to format decimals with 1/8, 1/4, 1/2 fractions first
                int integerPart = (int)Math.floor(remainingInches);
                float fractionPart = remainingInches - integerPart;
                if (inchDecimalsRoundedToClosestHeighth) {
                  int eighth = Math.round(fractionPart * 8); 
                  if (eighth == 0 || eighth == 8) {
                    footInchFormat.format(new Object [] {feet, Math.round(remainingInches * 8) / 8f}, result, fieldPosition);
                  } else { 
                    footInchEighthFormat.format(new Object [] {feet, integerPart, inchFractionCharacters [eighth - 1]}, result, fieldPosition);
                  }
                } else {
                  float remainderToClosestEighth = fractionPart % 0.125f;
                  if (remainderToClosestEighth <= 0.0005f || remainderToClosestEighth >= 0.1245f) {
                    int eighth = Math.round(fractionPart * 8); 
                    if (eighth == 0 || eighth == 8) {
                      footInchFormat.format(new Object [] {feet, remainingInches}, result, fieldPosition);
                    } else {
                      footInchEighthFormat.format(new Object [] {feet, integerPart, inchFractionCharacters [eighth - 1]}, result, fieldPosition);
                    }
                  } else {                
                    footInchFormat.format(new Object [] {feet, remainingInches}, result, fieldPosition);
                  }
                }
              } else {
                footFormat.format(new Object [] {feet}, result, fieldPosition);
              }
              return result;
            }
            
            @Override
            public Number parse(String text, ParsePosition parsePosition) {
              double value = 0;
              ParsePosition numberPosition = new ParsePosition(parsePosition.getIndex());
              skipWhiteSpaces(text, numberPosition);
              // Parse feet
              int quoteIndex = text.indexOf('\'', parsePosition.getIndex());
              if (quoteIndex != -1) {
                Number feet = footNumberFormat.parse(text, numberPosition);
                if (feet == null) {
                  parsePosition.setErrorIndex(numberPosition.getErrorIndex());
                  return null;
                }
                skipWhiteSpaces(text, numberPosition);
                if (numberPosition.getIndex() != quoteIndex) {
                  parsePosition.setErrorIndex(numberPosition.getIndex());
                  return null;
                }
                value = footToCentimeter(feet.intValue());                
                numberPosition = new ParsePosition(quoteIndex + 1);
                skipWhiteSpaces(text, numberPosition);
                // Test optional foot inch separator
                if (numberPosition.getIndex() < text.length()
                    && footInchSeparator.indexOf(text.charAt(numberPosition.getIndex())) >= 0) {
                  numberPosition.setIndex(numberPosition.getIndex() + 1);
                  skipWhiteSpaces(text, numberPosition);
                }
                if (numberPosition.getIndex() == text.length()) {
                  parsePosition.setIndex(text.length());
                  return value;
                }
              } 
              // Parse inches
              Number inches = inchNumberFormat.parse(text, numberPosition);
              if (inches == null) {
                parsePosition.setErrorIndex(numberPosition.getErrorIndex());
                return null;
              }
              value += inchToCentimeter(inches.floatValue());
              // Parse fraction
              skipWhiteSpaces(text, numberPosition);
              if (numberPosition.getIndex() == text.length()) {
                parsePosition.setIndex(text.length());
                return value;
              }
              char fractionChar = text.charAt(numberPosition.getIndex());              
              if (text.charAt(numberPosition.getIndex()) == '\"') {
                parsePosition.setIndex(numberPosition.getIndex() + 1);
                return value;
              }

              for (int i = 0; i < inchFractionCharacters.length; i++) {
                if (inchFractionCharacters [i] == fractionChar) {
                  // Check no decimal fraction was specified
                  int lastDecimalSeparatorIndex = text.lastIndexOf(getDecimalFormatSymbols().getDecimalSeparator(), 
                      numberPosition.getIndex() - 1);
                  if (lastDecimalSeparatorIndex > quoteIndex) {
                    return null;
                  } else {
                    value += inchToCentimeter((i + 1) / 8f);
                    parsePosition.setIndex(numberPosition.getIndex() + 1);
                    skipWhiteSpaces(text, parsePosition);
                    if (parsePosition.getIndex() < text.length() 
                        && text.charAt(parsePosition.getIndex()) == '\"') {
                      parsePosition.setIndex(parsePosition.getIndex() + 1);
                    }
                    return value;
                  }
                }
              }
              
              parsePosition.setIndex(numberPosition.getIndex());
              return value;
            }
            
            /**
             * Increases the index of <code>fieldPosition</code> to skip white spaces. 
             */
            private void skipWhiteSpaces(String text, ParsePosition fieldPosition) {
              while (fieldPosition.getIndex() < text.length()
                  && Character.isWhitespace(text.charAt(fieldPosition.getIndex()))) {
                fieldPosition.setIndex(fieldPosition.getIndex() + 1);
              }
            }
          };
        
        String squareFootUnit = resource.getString("squareFootUnit");
        this.areaFormatWithUnit = new DecimalFormat("#,##0.## " + squareFootUnit){
            @Override
            public StringBuffer format(double number, StringBuffer result,
                                       FieldPosition fieldPosition) {
              // Convert square centimeter to square foot
              return super.format(number / 929.0304, result, fieldPosition);                
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
      float magnetizedLength = inchToCentimeter(Math.round(centimeterToInch(length) / precision) * precision);
      if (magnetizedLength == 0 && length > 0) {
        return length;
      } else {
        return magnetizedLength;
      }
    }

    @Override
    public float getMinimumLength() {        
      return LengthUnit.inchToCentimeter(0.125f);
    }

    @Override
    public float centimeterToUnit(float length) {
      return centimeterToInch(length);
    }

    @Override
    public float unitToCentimeter(float length) {
      return inchToCentimeter(length);
    }
  };

  /**
   * Returns the <code>length</code> given in centimeters converted to inches.
   */
  public static float centimeterToInch(float length) {
    return length / 2.54f;
  }

  /**
   * Returns the <code>length</code> given in centimeters converted to feet.
   */
  public static float centimeterToFoot(float length) {
    return length / 2.54f / 12;
  }
  
  /**
   * Returns the <code>length</code> given in inches converted to centimeters.
   */
  public static float inchToCentimeter(float length) {
    return length * 2.54f;
  }
  
  /**
   * Returns the <code>length</code> given in feet converted to centimeters.
   */
  public static float footToCentimeter(float length) {
    return length * 2.54f * 12;
  }
  
  /**
   * Returns a format able to format lengths with their localized unit.
   */
  public abstract Format getFormatWithUnit(); 

  /**
   * Returns a format able to format lengths.
   */
  public abstract Format getFormat(); 

  /**
   * A decimal format for meter family units.
   */
  private static class MeterFamilyFormat extends DecimalFormat {
    private final float unitMultiplier;

    public MeterFamilyFormat(String pattern, float unitMultiplier) {
      super(pattern);
      this.unitMultiplier = unitMultiplier;
      
    }

    @Override
    public StringBuffer format(double number, StringBuffer result,
                               FieldPosition fieldPosition) {
      // Convert centimeter to millimeter
      return super.format(number * this.unitMultiplier, result, fieldPosition);                
    }

    @Override
    public StringBuffer format(long number, StringBuffer result,
                               FieldPosition fieldPosition) {
      return format((double)number, result, fieldPosition);
    }
    
    @Override
    public Number parse(String text, ParsePosition pos) {
      Number number = super.parse(text, pos);
      if (number == null) {
        return null;
      } else {
        return number.floatValue() / this.unitMultiplier;
      }
    }
  }
  
  /**
   * Returns a format able to format areas with their localized unit.
   */
  public abstract Format getAreaFormatWithUnit();

  /**
   * A decimal format for square meter.
   */
  private static class SquareMeterAreaFormatWithUnit extends DecimalFormat {
    public SquareMeterAreaFormatWithUnit(String squareMeterUnit) {
      super("#,##0.## " + squareMeterUnit);
    }
    
    @Override
    public StringBuffer format(double number, StringBuffer result,
                               FieldPosition fieldPosition) {
      // Convert square centimeter to square meter
      return super.format(number / 10000, result, fieldPosition);                
    }
  }
  
  /**
   * Returns a localized name of this unit.
   */
  public abstract String getName();
  
  /**
   * Returns the value close to the given <code>length</code> in centimeter under magnetism. 
   */
  public abstract float getMagnetizedLength(float length, float maxDelta);

  /**
   * Returns the value close to the given length under magnetism for meter units.
   */
  private static float getMagnetizedMeterLength(float length, float maxDelta) {
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
    float magnetizedLength = Math.round(length / precision) * precision;
    if (magnetizedLength == 0 && length > 0) {
      return length;
    } else {
      return magnetizedLength;
    }
  }

  /**
   * Returns the minimum value for length in centimeter.
   */
  public abstract float getMinimumLength();
  
  /**
   * Returns the <code>length</code> given in centimeters converted 
   * to a value expressed in this unit.
   * @since 2.0
   */
  public abstract float centimeterToUnit(float length);

  /**
   * Returns the <code>length</code> given in this unit converted 
   * to a value expressed in centimeter.
   * @since 2.0
   */
  public abstract float unitToCentimeter(float length);
}