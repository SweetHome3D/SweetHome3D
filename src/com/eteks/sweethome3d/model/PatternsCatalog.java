/*
 * PatternsCatalog.java 26 mai 2009
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A catalog of texture images used as patterns to fill plan areas.
 * @author Emmanuel Puybaret
 * @since  2.0
 */
public class PatternsCatalog {
  private List<TextureImage> patterns;
  
  /**
   * Creates a patterns catalog.
   */
  public PatternsCatalog(List<TextureImage> patterns) {
    this.patterns = new ArrayList<TextureImage>(patterns);
  }

  /**
   * Returns the patterns list.
   * @return an unmodifiable list of furniture.
   */
  public List<TextureImage> getPatterns() {
    return Collections.unmodifiableList(this.patterns);
  }

  /**
   * Returns the count of patterns in this category.
   */
  public int getPatternsCount() {
    return this.patterns.size();
  }

  /**
   * Returns the pattern at a given <code>index</code>.
   */
  public TextureImage getPattern(int index) {
    return this.patterns.get(index);
  }

  /**
   * Returns the pattern with a given <code>name</code>.
   * @throws IllegalArgumentException if no pattern with the given <code>name</code> exists
   */
  public TextureImage getPattern(String name) {
    for (TextureImage pattern : patterns) {
      if (name.equals(pattern.getName())) {
        return pattern;
      }
    }
    throw new IllegalArgumentException("No pattern with name " + name);
  }
}
