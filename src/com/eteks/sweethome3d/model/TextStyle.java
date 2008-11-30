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
  private static final long serialVersionUID = 1L;
  
  private final float   fontSize;
  private final boolean bold;
  private final boolean italic;
  
  public TextStyle(float fontSize) {
    this(fontSize, false, false);    
  }
  
  public TextStyle(float fontSize, boolean bold, boolean italic) {
    this.fontSize = fontSize;
    this.bold = bold;
    this.italic = italic;
  }
  
  /**
   * Returns the font size of this text style.  
   */
  public float getFontSize() {
    return this.fontSize;
  }
  
  /**
   * Returns whether this text style is bold or not.
   */
  public boolean isBold() {
    return this.bold;
  }
  
  /**
   * Returns whether this text style is italic or not.
   */
  public boolean isItalic() {
    return this.italic;
  }

  /**
   * Returns a derived style of this text style with a given font size.
   */
  public TextStyle deriveStyle(float fontSize) {
    if (getFontSize() == fontSize) {
      return this;
    } else {
      return new TextStyle(fontSize, isBold(), isItalic());
    }
  }

  /**
   * Returns a derived style of this text style with a given bold style.
   */
  public TextStyle deriveBoldStyle(boolean bold) {
    if (isBold() == bold) {
      return this;
    } else {
      return new TextStyle(getFontSize(), bold, isItalic());
    }
  }

  /**
   * Returns a derived style of this text style with a given italic style.
   */
  public TextStyle deriveItalicStyle(boolean italic) {
    if (isItalic() == italic) {
      return this;
    } else {
      return new TextStyle(getFontSize(), isBold(), italic);
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
          && textStyle.bold == this.bold
          && textStyle.italic == this.italic;
    }
    return false;
  }
  
  /**
   * Returns a hash code for this text style.
   */
  @Override
  public int hashCode() {
    int hashCode = Float.floatToIntBits(this.fontSize);
    if (this.bold) {
      hashCode++;
    }
    if (this.italic) {
      hashCode++;
    }
    return hashCode;
  }
}
