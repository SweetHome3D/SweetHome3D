/*
 * SweetHome3DApplet.java 10 oct. 2008
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
package com.eteks.sweethome3d.applet;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JApplet;
import javax.swing.JLabel;

import com.eteks.sweethome3d.tools.ExtensionsClassLoader;

/**
 * This applet class loads Sweet Home 3D classes from jars in classpath or from extension 
 * jars stored as resources.
 * <p>This applet accepts the following parameters:
 * 
 * <ul><li><code>furnitureCatalogURLs</code> specifies the URLs of the furniture libraries available 
 *     in Sweet Home 3D catalog. These URLs are comma or space separated, and if they are not 
 *     absolute URLs, they will be considered as relative to applet codebase. Each URL is a ZIP file 
 *     that must contain a file named <code>PluginFurnitureCatalog.properties</code> describing the 
 *     properties of each piece of furniture proposed by the URL file.
 *     <br>By default, the value of this parameter is <code>catalog.zip</code>. If this file
 *     or one of the URLs specified by this parameter doesn't exist, it will be ignored.</li>
 *     
 *     <li><code>texturesCatalogURLs</code> specifies the URLs of the textures libraries available 
 *     in Sweet Home 3D catalog. These URLs are comma or space separated, and if they are not 
 *     absolute URLs, they will be considered as relative to applet codebase. Each URL is a ZIP file 
 *     that must contain a file named <code>PluginTexturesCatalog.properties</code> describing the 
 *     properties of each texture proposed by the URL file.
 *     <br>By default, the value of this parameter is <code>catalog.zip</code>, meaning that the 
 *     furniture and textures can be stored in the same file. If this file
 *     or one of the URLs specified by this parameter doesn't exist, it will be ignored.</li>
 *     
 *     <li><code>pluginURLs</code> specifies the URLs of the actions available to users through 
 *     {@link com.eteks.sweethome3d.plugin.Plugin plugins}.These URLs are comma or space separated, 
 *     and if they are not absolute URLs, they will be considered as relative to applet codebase. 
 *     If some classes of a plugin needs to access to resources protected by applet sandbox,
 *     its JAR file should be signed, added to <code>archive</code> applet attribute and
 *     and in a <code>jar</code> element of applet JNLP file.
 *     <br>By default, the value of this parameter is empty. If one of the URLs specified by 
 *     this parameter doesn't exist, it will be ignored.</li>
 *     
 *     <li><code>writeHomeURL</code> specifies the URL of the HTTP service able 
 *     to write the data of a home. This data will be uploaded in the file parameter named 
 *     <code>home</code> of a POST request encoded with multipart/form-data MIME type, with 
 *     the name of the uploaded home being stored in its <code>filename</code> attribute.
 *     This service must return 1 if it wrote the uploaded successfully.
 *     <br>By default, this URL is <code>writeHome.php</code> and if it's not an absolute URL 
 *     it will be considered as relative to applet codebase. If its value is empty,
 *     <i>New</i>, <i>Save</i> and <i>Save as...</i> actions will be disabled and their buttons 
 *     won't be displayed.</li>
 *     
 *     <li><code>readHomeURL</code> specifies the URL of the HTTP service able 
 *     to return the data of a home written with the previous service. The home name
 *     is specified by the parameter named <code>home</code> of a GET request.
 *     <br>By default, this URL is <code>readHome.php?home=%s</code> (the %s sign will be 
 *     replaced by the requested home name). If it's not an absolute URL it will be 
 *     considered as relative to applet codebase.</li>
 *     
 *     <li><code>listHomesURL</code> specifies the URL of the HTTP service able 
 *     to return the list of home names able to be read from server. It must return
 *     these names in a string, separated from each other by a carriage return (\n).
 *     <br>By default, this URL is <code>listHomes.php</code> and if it's not an absolute URL 
 *     it will be considered as relative to applet codebase. If its value is empty,
 *     <i>New</i>, <i>Open</i> and <i>Save as...</i> actions will be disabled and their buttons 
 *     won't be displayed. If <code>defaultHome</code> is empty, <i>Save</i> action
 *     will be also disabled</li>
 *     
 *     <li><code>defaultHome</code> specifies the home that will be opened at applet launch
 *     with <code>readHomeURL</code> service. 
 *     <br>Omit this parameter or let its value empty, if no home should be opened.
 *     <br>If you want the applet open a home at launch without creating a <code>readHomeURL</code> 
 *     service, set <code>%s</code> value for <code>readHomeURL</code> parameter and put the absolute 
 *     URL of the home file or its URL relative to applet codebase in <code>defaultHome</code> 
 *     parameter.</li></ul>
 * 
 * <p>The bytecode of this class is Java 1.1 compatible to be able to notify users that 
 * it requires Java 5 when it's run under an old JVM.
 *     
 * @author Emmanuel Puybaret
 */
public class SweetHome3DApplet extends JApplet {
  static {
    initSystemProperties();
  }
  
  private Object appletApplication;
  
  /**
   * Sets various <code>System</code> properties required to be set before Applet is displayed.
   */
  private static void initSystemProperties() {
    try {
      // Enables Java 5 bug correction about dragging directly
      // a tree element without selecting it before :
      // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4521075
      System.setProperty("sun.swing.enableImprovedDragGesture", "true");
    } catch (SecurityException ex) {
      // Too bad Java refuses access to system properties
    }
  }
  
  public void init() {
    if (!isJava5OrSuperior()) {
      showError("<html><p>This applet may be run under Windows, Mac OS X 10.4 / 10.5, Linux and Solaris." +
          "<br>It requires Java version 5 or superior.</p>" +
          "<p>Please, check Java version set in Java preferences under Mac OS X," +
          "<br>or update your Java Runtime to the latest version available at java.com under the other systems.</p>");
    } else {
      createAppletApplication();
    }
  }

  public void destroy() {
    if (this.appletApplication != null) {
      try {
        Method destroyMethod = this.appletApplication.getClass().getMethod("destroy", new Class [0]);
        destroyMethod.invoke(this.appletApplication, new Object [0]);
      } catch (Exception ex) {
        // Can't do better than print stack trace when applet is destroyed
        ex.printStackTrace();
      }
    }
    this.appletApplication = null;
    // Collect deleted objects (seems to be required under Mac OS X when the applet is being reloaded)
    System.gc();
  }
  
  /**
   * Returns <code>true</code> if one of the homes edited by this applet is modified. 
   */
  public boolean isModified() {
    if (this.appletApplication != null) {
      try {
        Method destroyMethod = this.appletApplication.getClass().getMethod("isModified", new Class [0]);
        return ((Boolean)destroyMethod.invoke(this.appletApplication, new Object [0])).booleanValue();
      } catch (Exception ex) {
        // Can't do better than print stack trace
        ex.printStackTrace();
      }
    }
    return true;
  }
  
  /**
   * Returns <code>true</code> if current JVM version is 5+. 
   */
  private boolean isJava5OrSuperior() {
    String javaVersion = System.getProperty("java.version");
    String [] javaVersionParts = javaVersion.split("\\.|_");
    if (javaVersionParts.length >= 1) {
      try {
        // Return true for Java SE 5 and superior
        if (Integer.parseInt(javaVersionParts [1]) >= 5) {
          return true;
        }
      } catch (NumberFormatException ex) {
      }
    }
    return false;
  }

  /**
   * Shows the given text in a label.
   */
  private void showError(String text) {
    JLabel label = new JLabel(text, JLabel.CENTER);
    setContentPane(label);
  }
  
  /**
   * Creates a new <code>AppletApplication</code> instance that manages this applet content.
   */
  private void createAppletApplication() {
    try {
      Class sweetHome3DAppletClass = SweetHome3DApplet.class;
      String [] java3DFiles = {
          "j3dcore.jar", // Main Java 3D jars
          "vecmath.jar",
          "j3dutils.jar",
          "j3dcore-d3d.dll", // Windows DLLs
          "j3dcore-ogl.dll",
          "j3dcore-ogl-cg.dll",
          "j3dcore-ogl-chk.dll",
          "libj3dcore-ogl.so", // Linux DLLs
          "libj3dcore-ogl-cg.so",
          "gluegen-rt.jar", // Mac OS X jars and DLLs
          "jogl.jar",
          "libgluegen-rt.jnilib",
          "libjogl.jnilib",
          "libjogl_awt.jnilib",
          "libjogl_cg.jnilib"};
      List applicationPackages = new ArrayList(Arrays.asList(new String [] {
          "com.eteks.sweethome3d",
          "javax.media.j3d",
          "javax.vecmath",
          "com.sun.j3d",
          "com.sun.opengl",
          "com.sun.gluegen.runtime",
          "javax.media.opengl",
          "com.microcrowd.loader.java3d"}));
      applicationPackages.addAll(getPluginsPackages());
      
      ClassLoader extensionsClassLoader = new ExtensionsClassLoader(
          sweetHome3DAppletClass.getClassLoader(), sweetHome3DAppletClass.getProtectionDomain(),
          java3DFiles, (String [])applicationPackages.toArray(new String [applicationPackages.size()]));
      
      // Call application constructor with reflection
      String applicationClassName = "com.eteks.sweethome3d.applet.AppletApplication";
      Class applicationClass = extensionsClassLoader.loadClass(applicationClassName);
      Constructor applicationConstructor = 
          applicationClass.getConstructor(new Class [] {JApplet.class});
      this.appletApplication = applicationConstructor.newInstance(new Object [] {this});
    } catch (Throwable ex) {
      if (ex instanceof InvocationTargetException) {
        ex = ((InvocationTargetException)ex).getCause();
      }
      showError("<html>Can't start applet:<br>Exception" 
          + ex.getClass().getName() + " " + ex.getMessage());
      ex.printStackTrace();
    }
  }  
  
  /**
   * Returns the collection of packages that are found in plugins. 
   */
  private Collection getPluginsPackages() {
    String pluginURLs = getParameter("pluginURLs");
    if (pluginURLs != null) {        
      Set pluginPackages = new HashSet();
      // Add to pluginPackages all the packages contained in the plugin URLs
      String [] urlStrings = pluginURLs.split("\\s|,");
      for (int i = 0; i < urlStrings.length; i++) {
        try {
          URL pluginUrl = new URL(getCodeBase(), urlStrings [i]);
          ZipInputStream zipIn = null;
          try {
            // Open a zip input from pluginUrl
            zipIn = new ZipInputStream(pluginUrl.openStream());
            // Try directories in current zip stream  
            for (ZipEntry entry; (entry = zipIn.getNextEntry()) != null; ) {
              String zipEntryName = entry.getName();
              int lastIndex = zipEntryName.lastIndexOf('/');
              if (zipEntryName.endsWith(".class")) {
                if (lastIndex == -1) {
                  pluginPackages.add(""); // Add empty package
                } else {
                  pluginPackages.add(zipEntryName.substring(0, lastIndex).replace('/', '.'));
                }
              }
            }
          } catch (IOException ex) {
            // Ignore furniture plugin 
          } finally {
            if (zipIn != null) {
              try {
                zipIn.close();
              } catch (IOException ex) {
              }
            }
          }
        } catch (MalformedURLException ex) {
          // Ignore malformed URLs
        }
      }
      return pluginPackages;
    }
    return Collections.EMPTY_SET;
  }
}
