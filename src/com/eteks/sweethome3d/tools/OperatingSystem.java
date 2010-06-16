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

import java.io.File;
import java.io.IOException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.apple.eio.FileManager;

/**
 * Tools used to test current user operating system.
 * @author Emmanuel Puybaret
 */
public class OperatingSystem {
  private static final String EDITOR_SUB_FOLDER; 
  private static final String APPLICATION_SUB_FOLDER;
  private static final String TEMPORARY_SUB_FOLDER;
  
  static {
    // Retrieve sub folders where is stored application data
    ResourceBundle resource = ResourceBundle.getBundle(OperatingSystem.class.getName());
    if (OperatingSystem.isMacOSX()) {
      EDITOR_SUB_FOLDER = resource.getString("editorSubFolder.Mac OS X");
      APPLICATION_SUB_FOLDER = resource.getString("applicationSubFolder.Mac OS X");
    } else if (OperatingSystem.isWindows()) {
      EDITOR_SUB_FOLDER = resource.getString("editorSubFolder.Windows");
      APPLICATION_SUB_FOLDER = resource.getString("applicationSubFolder.Windows");
    } else {
      EDITOR_SUB_FOLDER = resource.getString("editorSubFolder");
      APPLICATION_SUB_FOLDER = resource.getString("applicationSubFolder");
    }
    
    String temporarySubFolder;
    try {
      temporarySubFolder = resource.getString("temporarySubFolder");
      if (temporarySubFolder.trim().length() == 0) {
        temporarySubFolder = null;
      }
    } catch (MissingResourceException ex) {
      temporarySubFolder = "work";
    }
    TEMPORARY_SUB_FOLDER = temporarySubFolder;
  }
 
  // This class contains only static methods
  private OperatingSystem() {    
  }

  /**
   * Returns <code>true</code> if current operating is Linux.
   */
  public static boolean isLinux() {
    return System.getProperty("os.name").startsWith("Linux");
  }

  /**
   * Returns <code>true</code> if current operating is Windows.
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

  /**
   * Returns the default folder used to store temporary files created in the program.
   */
  public static File getDefaultTemporaryFolder() throws IOException {
    File temporaryFolder = TEMPORARY_SUB_FOLDER != null 
        ? new File(getDefaultApplicationFolder(), TEMPORARY_SUB_FOLDER)
        : null;
    if (temporaryFolder != null
        && !temporaryFolder.exists()
        && temporaryFolder.mkdirs()) {
      throw new IOException("Can't create temporary folder " + temporaryFolder);
    }
    return temporaryFolder;
  }
  
  /**
   * Returns default application folder. 
   */
  public static File getDefaultApplicationFolder() throws IOException {
    File userApplicationFolder; 
    if (isMacOSX()) {
      userApplicationFolder = new File(MacOSXFileManager.getApplicationSupportFolder());
    } else if (isWindows()) {
      userApplicationFolder = new File(System.getProperty("user.home"), "Application Data");
      // If user Application Data directory doesn't exist, use user home
      if (!userApplicationFolder.exists()) {
        userApplicationFolder = new File(System.getProperty("user.home"));
      }
    } else { 
      // Unix
      userApplicationFolder = new File(System.getProperty("user.home"));
    }
    return new File(userApplicationFolder, 
        EDITOR_SUB_FOLDER + File.separator + APPLICATION_SUB_FOLDER);
  }

  /**
   * File manager class that accesses to Mac OS X specifics.
   * Do not invoke methods of this class without checking first if 
   * <code>os.name</code> System property is <code>Mac OS X</code>.
   * This class requires some classes of <code>com.apple.eio</code> package  
   * to compile.
   */
  private static class MacOSXFileManager {
    public static String getApplicationSupportFolder() throws IOException {
      // Find application support folder (0x61737570) for user domain (-32763)
      return FileManager.findFolder((short)-32763, 0x61737570);
    }
  }
}
