/*
 * AspectRatio.java 14 may 2009
 *
 * Copyright (c) 2009 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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

/**
 * The aspect ratio of pictures.
 * @since 2.0
 */
public enum AspectRatio {
  FREE_RATIO(null), 
  VIEW_3D_RATIO(null), 
  RATIO_4_3(4f / 3), 
  RATIO_3_2(1.5f), 
  RATIO_16_9(16f / 9), 
  SQUARE_RATIO(1f);
  
  private final Float value;
  
  private AspectRatio(Float value) {
    this.value = value;
  }    
  
  /**
   * Returns the value of this aspect ratio (width / height) or <code>null</code> if it's not known.
   */
  public Float getValue() {
    return value;
  }
}