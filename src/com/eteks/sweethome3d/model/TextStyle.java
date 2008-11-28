/*
 * TextStyle.java 27 nov. 2008
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
package com.eteks.sweethome3d.model;

import java.io.Serializable;

/**
 * The different attributes that defines a text style. 
 * @author Emmanuel Puybaret
 */
public class TextStyle implements Serializable {
  /**
   * The different styles applicable to font. 
   */
  public enum FontStyle {PLAIN, ITALIC, BOLD, BOLD_ITALIC}

  private static final long serialVersionUID = 1L;
  
  private final float  fontSize;
  private final String fontStyleName; // Keep font style as a string to avoid future serialization 
                                      // conflicts if FontStyle is enriched 
  
  public TextStyle(float fontSize) {
    this(fontSize, FontStyle.PLAIN);    
  }
  
  public TextStyle(float fontSize, FontStyle fontStyle) {
    this.fontSize = fontSize;
    this.fontStyleName = fontStyle.name();    
  }
  
  /**
   * Returns the font size of this text style.  
   */
  public float getFontSize() {
    return this.fontSize;
  }
  
  /**
   * Returns the font style of this text style.
   */
  public FontStyle getFontStyle() {
    try {
      return FontStyle.valueOf(this.fontStyleName);
    } catch (IllegalArgumentException ex) {
      return null;
    }
  }
  
  /**
   * Returns <code>true</code> if this text style is equal to <code>object</code>.
   */
  @Override
  public boolean equals(Object object) {
    if (object instanceof TextStyle) {
      TextStyle textStyle = (TextStyle)object;
      return textStyle.fontSize == this.fontSize
          && (textStyle.getFontStyle() == getFontStyle());
    }
    return false;
  }
  
  /**
   * Returns a hash code for this text style.
   */
  @Override
  public int hashCode() {
    int hashCode = Float.floatToIntBits(this.fontSize);
    FontStyle fontStyle = getFontStyle();
    if (fontStyle != null) {
      hashCode += fontStyle.hashCode();
    }
    return hashCode;
  }
}
