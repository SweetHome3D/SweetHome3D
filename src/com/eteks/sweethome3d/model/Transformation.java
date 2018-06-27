/*
 * Transformation.java 25 june 2018
 *
 * Sweet Home 3D, Copyright (c) 2018 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
import java.util.Arrays;

/**
 * The transformation applied to some model parts.
 * @since 6.0
 * @author Emmanuel Puybaret
 */
public class Transformation implements Serializable {
  private static final long serialVersionUID = 1L;

  private final String      name;
  private final float [][]  matrix;

  /**
   * Creates a material instance from parameters.
   * @since 6.0
   */
  public Transformation(String name, float [][]  matrix) {
    this.name = name;
    this.matrix = matrix;
  }

  /**
   * Returns the name of this transformation.
   * @return the name of the transformation.
   * @since 6.0
   */
  public String getName() {
    return this.name;
  }

  /**
   * Returns the matrix of this transformation.
   * @return a 4x3 float array.
   * @since 6.0
   */
  public float [][] getMatrix() {
    return new float [][] {{this.matrix[0][0], this.matrix[0][1], this.matrix[0][2], this.matrix[0][3]},
                           {this.matrix[1][0], this.matrix[1][1], this.matrix[1][2], this.matrix[1][3]},
                           {this.matrix[2][0], this.matrix[2][1], this.matrix[2][2], this.matrix[2][3]}};
  }

  /**
   * Returns <code>true</code> if this transformation is equal to <code>object</code>.
   * @since 6.0
   */
  @Override
  public boolean equals(Object object) {
    if (object instanceof Transformation) {
      Transformation transformation = (Transformation)object;
      if (transformation.name.equals(this.name)) {
        for (int i = 0; i < this.matrix.length; i++) {
          for (int j = 0; j < this.matrix [i].length; j++) {
            if (transformation.matrix [i][j] != this.matrix [i][j]) {
              return false;
            }
          }
        }
        return true;
      }
    }
    return false;
  }

  /**
   * Returns a hash code for this transformation.
   * @since 6.0
   */
  @Override
  public int hashCode() {
    return this.name.hashCode() + Arrays.deepHashCode(this.matrix);
  }
}
