/*
 * OperatingSystem.java 1 nov. 07
 *
 * Copyright (c) 2007 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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
package com.eteks.sweethome3d.tools;

/**
 * Tools used to test current user operating system.
 * @author Emmanuel Puybaret
 */
public class OperatingSystem {
  // This class contains only static methods
  private OperatingSystem() {    
  }

  /**
   * Returns <code>true</code> if current operating is Windows
   */
  public static boolean isWindows() {
    return System.getProperty("os.name").startsWith("Windows");
  }

  /**
   * Returns <code>true</code> if current operating is Mac OS X.
   */
  public static boolean isMacOSX() {
    return System.getProperty("os.name").startsWith("Mac OS X");
  }

  /**
   * Returns <code>true</code> if current operating is Mac OS X 10.5 
   * or superior.
   */
  public static boolean isMacOSXLeopardOrSuperior() {
    // Just need to test is OS version is different of 10.4 because Sweet Home 3D
    // isn't supported under Mac OS X versions previous to 10.4
    return isMacOSX()
        && !System.getProperty("os.version").startsWith("10.4");
  }
}
