/*
 * OperatingSystem.java 1 nov. 07
 *
 * Sweet Home 3D, Copyright (c) 2007 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
import java.io.FileFilter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import com.apple.eio.FileManager;
import com.eteks.sweethome3d.model.Home;

/**
 * Tools used to test current user operating system.
 * @author Emmanuel Puybaret
 */
public class OperatingSystem {
  private static final String EDITOR_SUB_FOLDER; 
  private static final String APPLICATION_SUB_FOLDER;
  private static final String TEMPORARY_SUB_FOLDER;
  private static final String TEMPORARY_SESSION_SUB_FOLDER;
  
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
    try {
      temporarySubFolder = System.getProperty(
          "com.eteks.sweethome3d.tools.temporarySubFolder", temporarySubFolder);
    } catch (AccessControlException ex) {
      // Don't change temporarySubFolder value
    }
    TEMPORARY_SUB_FOLDER = temporarySubFolder;
    TEMPORARY_SESSION_SUB_FOLDER = UUID.randomUUID().toString();
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
   * Returns <code>true</code> if current operating is Mac OS X 10.5 or superior.
   */
  public static boolean isMacOSXLeopardOrSuperior() {
    // Just need to test is OS version is different of 10.4 because Sweet Home 3D
    // isn't supported under Mac OS X versions previous to 10.4
    return isMacOSX()
        && !System.getProperty("os.version").startsWith("10.4");
  }
  
  /**
   * Returns <code>true</code> if current operating is Mac OS X 10.7 or superior.
   * @since 4.1
   */
  public static boolean isMacOSXLionOrSuperior() {
    return isMacOSX()
        && compareVersions(System.getProperty("os.version"), "10.7") >= 0;
  }
  
  /**
   * Returns <code>true</code> if the given version is greater than or equal to the version 
   * of the current JVM. 
   * @since 4.0
   */
  public static boolean isJavaVersionGreaterOrEqual(String javaMinimumVersion) {
    return compareVersions(javaMinimumVersion, System.getProperty("java.version")) <= 0;
  }

  /**
   * Returns a negative number if <code>version1</code> &lt; <code>version2</code>,
   * 0 if <code>version1</code> = <code>version2</code>
   * and a positive number if <code>version1</code> &gt; <code>version2</code>.
   * Version strings are first split into parts, each subpart ending at each punctuation, space 
   * or when a character of a different type is encountered (letter vs digit). Then each numeric 
   * or string subparts are compared to each other, strings being considered greater than numbers
   * except for pre release strings (i.e. alpha, beta, rc). Examples:<pre>
   * "" < "1"
   * "0" < "1.0"
   * "1.2beta" < "1.2"
   * "1.2beta" < "1.2beta2"
   * "1.2beta" < "1.2.0"
   * "1.2beta4" < "1.2beta10"
   * "1.2beta4" < "1.2"
   * "1.2beta4" < "1.2rc"
   * "1.2alpha" < "1.2beta"
   * "1.2beta" < "1.2rc"
   * "1.2rc" < "1.2"
   * "1.2rc" < "1.2a"
   * "1.2" < "1.2a"
   * "1.2a" < "1.2b"
   * "1.7.0_11" < "1.7.0_12"
   * "1.7.0_11rc1" < "1.7.0_11rc2"
   * "1.7.0_11rc" < "1.7.0_11"
   * "1.7.0_9" < "1.7.0_11rc"
   * "1.2" < "1.2.1"
   * "1.2" < "1.2.0.1"
   * 
   * "1.2" = "1.2.0.0" (missing information is considered as 0)
   * "1.2beta4" = "1.2 beta-4" (punctuation, space or missing punctuation doesn't influence result)
   * "1.2beta4" = "1,2,beta,4"
   * </pre>
   * @since 4.0
   */
  public static int compareVersions(String version1, String version2) {
    List<Object> version1Parts = splitVersion(version1);
    List<Object> version2Parts = splitVersion(version2);
    int i = 0;
    for ( ; i < version1Parts.size() || i < version2Parts.size(); i++) {
      Object version1Part = i < version1Parts.size() 
          ? convertPreReleaseVersion(version1Parts.get(i))
          : BigInteger.ZERO; // Missing part is considered as 0
      Object version2Part = i < version2Parts.size() 
          ? convertPreReleaseVersion(version2Parts.get(i))
          : BigInteger.ZERO;
      if (version1Part.getClass() == version2Part.getClass()) {
        @SuppressWarnings({"unchecked", "rawtypes"})
        int comparison = ((Comparable)version1Part).compareTo(version2Part);
        if (comparison != 0) {
          return comparison;
        }
      } else if (version1Part instanceof String) {
        // An integer subpart is smaller than a string (except for pre release strings)
        return 1;
      } else {
        // A string subpart is greater than an integer 
        return -1;
      }
    }
    return 0;
  }
  
  /**
   * Returns the substrings components of the given <code>version</code>.
   */
  private static List<Object> splitVersion(String version) {
    List<Object> versionParts = new ArrayList<Object>();
    StringBuilder subPart = new StringBuilder();
    // First split version with punctuation and space
    for (String part : version.split("\\p{Punct}|\\s")) {
      for (int i = 0; i < part.length(); ) {
        subPart.setLength(0);
        char c = part.charAt(i);
        if (Character.isDigit(c)) {
          for ( ; i < part.length() && Character.isDigit(c = part.charAt(i)); i++) {
            subPart.append(c);
          }
          versionParts.add(new BigInteger(subPart.toString()));
        } else {
          for ( ; i < part.length() && !Character.isDigit(c = part.charAt(i)); i++) {
            subPart.append(c);
          }
          versionParts.add(subPart.toString());
        }  
      }
    }    
    return versionParts;
  }
  
  /**
   * Returns negative values if the given version part matches a pre release (i.e. alpha, beta, rc)
   * or returns the parameter itself.
   */
  private static Object convertPreReleaseVersion(Object versionPart) {
    if (versionPart instanceof String) {
      String versionPartString = (String)versionPart;
      if ("alpha".equalsIgnoreCase(versionPartString)) {
        return new BigInteger("-3");
      } else if ("beta".equalsIgnoreCase(versionPartString)) {
        return new BigInteger("-2");
      } else if ("rc".equalsIgnoreCase(versionPartString)) {
        return new BigInteger("-1");
      }
    }
    return versionPart;
  }

  /**
   * Returns a temporary file that will be deleted when JVM will exit.
   * @throws IOException if the file couldn't be created
   */
  public static File createTemporaryFile(String prefix, String suffix) throws IOException {
    File temporaryFolder;
    try {
      temporaryFolder = getDefaultTemporaryFolder(true);
    } catch (IOException ex) {
      // In case creating default temporary folder failed, use default temporary files folder
      temporaryFolder = null;
    }
    File temporaryFile = File.createTempFile(prefix, suffix, temporaryFolder);
    temporaryFile.deleteOnExit();
    return temporaryFile;
  }
  
  /**
   * Returns a file comparator that sorts file names according to their version number. 
   */
  public static Comparator<File> getFileVersionComparator() {
    return new Comparator<File>() {
        public int compare(File file1, File file2) {
          return OperatingSystem.compareVersions(file1.getName(), file2.getName());
        }
      };
  }

  /**
   * Deletes all the temporary files created with {@link #createTemporaryFile(String, String) createTemporaryFile}.
   */
  public static void deleteTemporaryFiles() {
    try {
      File temporaryFolder = getDefaultTemporaryFolder(false);
      if (temporaryFolder != null) {
        for (File temporaryFile : temporaryFolder.listFiles()) {
          temporaryFile.delete();
        }
        temporaryFolder.delete();
      }
    } catch (IOException ex) {
      // Ignore temporary folder that can't be found
    } catch (AccessControlException ex) {
    }
  }

  /**
   * Returns the default folder used to store temporary files created in the program.
   */
  private synchronized static File getDefaultTemporaryFolder(boolean create) throws IOException {
    if (TEMPORARY_SUB_FOLDER != null) {
      File temporaryFolder;
      if (new File(TEMPORARY_SUB_FOLDER).isAbsolute()) {
        temporaryFolder = new File(TEMPORARY_SUB_FOLDER);
      } else {
        temporaryFolder = new File(getDefaultApplicationFolder(), TEMPORARY_SUB_FOLDER);
      }
      final String versionPrefix = Home.CURRENT_VERSION + "-";
      final File sessionTemporaryFolder = new File(temporaryFolder, 
          versionPrefix + TEMPORARY_SESSION_SUB_FOLDER);      
      if (!sessionTemporaryFolder.exists()) {
        // Retrieve existing folders working with same Sweet Home 3D version in temporary folder
        final File [] siblingTemporaryFolders = temporaryFolder.listFiles(new FileFilter() {
            public boolean accept(File file) {
              return file.isDirectory() 
                  && file.getName().startsWith(versionPrefix);
            }
          });
        
        // Create temporary folder  
        if (!createTemporaryFolders(sessionTemporaryFolder)) {
          throw new IOException("Can't create temporary folder " + sessionTemporaryFolder);
        }
        
        // Launch a timer that updates modification date of the temporary folder each minute
        final long updateDelay = 60000;
        new Timer(true).schedule(new TimerTask() {
            @Override
            public void run() {
              // Ensure modification date is always growing in case system time was adjusted
              sessionTemporaryFolder.setLastModified(Math.max(System.currentTimeMillis(),
                  sessionTemporaryFolder.lastModified() + updateDelay));
            }
          }, updateDelay, updateDelay);
        
        if (siblingTemporaryFolders != null
            && siblingTemporaryFolders.length > 0) {
          // Launch a timer that will delete in 10 min temporary folders older than a week 
          final long deleteDelay = 10 * 60000;
          final long age = 7 * 24 * 3600000;
          new Timer(true).schedule(new TimerTask() {
              @Override
              public void run() {
                long now = System.currentTimeMillis();
                for (File siblingTemporaryFolder : siblingTemporaryFolders) {
                  if (siblingTemporaryFolder.exists()
                      && now - siblingTemporaryFolder.lastModified() > age) {
                    File [] temporaryFiles = siblingTemporaryFolder.listFiles();
                    for (File temporaryFile : temporaryFiles) {
                      temporaryFile.delete();
                    }
                    siblingTemporaryFolder.delete();
                  }
                }
              }
            }, deleteDelay);
        }
      }
      return sessionTemporaryFolder;
    } else {
      return null;
    }
  }
  
  /**
   * Creates the temporary folders in parameters and returns <code>true</code> if it was successful.
   */
  private static boolean createTemporaryFolders(File temporaryFolder) {
    // Inspired from java.io.File#mkdirs
    if (temporaryFolder.exists()) {
      return false;
    }
    if (temporaryFolder.mkdir()) {
      temporaryFolder.deleteOnExit();
      return true;
    }
    File canonicalFile = null;
    try {
      canonicalFile = temporaryFolder.getCanonicalFile();
    } catch (IOException e) {
      return false;
    }
    File parent = canonicalFile.getParentFile();
    if (parent != null 
        && (createTemporaryFolders(parent) || parent.exists()) 
        && canonicalFile.mkdir()) {
      temporaryFolder.deleteOnExit();
      return true;
    } else {
      return false;
    }
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
