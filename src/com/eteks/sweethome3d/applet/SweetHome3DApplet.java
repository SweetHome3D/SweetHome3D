/*
 * SweetHome3DApplet.java 10 oct. 2008
 *
 * Sweet Home 3D, Copyright (c) 2008 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
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
 *     <li><code>furnitureResourcesURLBase</code> specifies the URL used as a base to build the URLs of 
 *     the 3D models and icons cited in the <code>PluginFurnitureCatalog.properties</code> file of a 
 *     furniture catalog. If this URL isn't an absolute URL it will be considered relative to 
 *     applet codebase. If this URL base should the applet code base itself, use a value equal to ".".
 *     <br>If this parameter isn't defined, the URLs of 3D model and icons will be relative to their 
 *     furniture catalog file or absolute.</li>
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
 *     <li><code>texturesResourcesURLBase</code> specifies the URL used as a base to build the URLs of
 *     the texture images cited in the <code>PluginTexturesCatalog.properties</code> file of a 
 *     textures catalog. If this URL isn't an absolute URL it will be considered relative to 
 *     applet codebase. If this URL base should the applet code base itself, use a value equal to ".".
 *     <br>If this parameter isn't defined, the URLs of texture images will be relative to their 
 *     textures catalog file or absolute.</li>
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
 *     This service must return 1 if it wrote the uploaded data successfully.
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
 *     parameter.</li>
 * 
 *     <li><code>writePreferencesURL</code> specifies the URL of the HTTP service able 
 *     to write the XML content describing the user preferences. This data will be uploaded 
 *     in the parameter named <code>preferences</code> of a POST request.
 *     This service must return 1 if it completed successfully.
 *     <br>By default, this URL is empty and if it's not an absolute URL 
 *     it will be considered as relative to applet codebase.</li>
 *     
 *     <li><code>readPreferencesURL</code> specifies the URL of the HTTP service able 
 *     to return an XML content describing the user preferences as a set of properties. 
 *     The DTD of the XML content supported by the applet is specified at 
 *     <a href="http://java.sun.com/dtd/properties.dtd">http://java.sun.com/dtd/properties.dtd</a>.
 *     <br>By default, this URL is empty and if it's not an absolute URL it will be 
 *     considered as relative to applet codebase.</li>
 *     
 *     <li><code>enableExportToSH3D</code> specifies whether this applet should enable
 *     the action that lets the user export the edited home to a SH3D file. 
 *     <br>By default, the value of this parameter is <code>false</code>.</li>
 *     
 *     <li><code>enableExportToSVG</code> specifies whether this applet should enable
 *     the action that lets the user export the plan of the edited home to a SVG file. 
 *     <br>By default, the value of this parameter is <code>false</code>.</li>
 *     
 *     <li><code>enableExportToOBJ</code> specifies whether this applet should enable
 *     the action that lets the user export the 3D view of the edited home to an OBJ file. 
 *     <br>By default, the value of this parameter is <code>false</code>.</li>
 *     
 *     <li><code>enablePrintToPDF</code> specifies whether this applet should enable
 *     the action that lets the user print the edited home to a PDF file. 
 *     <br>By default, the value of this parameter is <code>false</code>.</li>
 *     
 *     <li><code>enableCreatePhoto</code> specifies whether this applet should enable
 *     the action that lets the user create a photo from the 3D view of the edited home. 
 *     <br>By default, the value of this parameter is <code>false</code>.</li>
 *     
 *     <li><code>enableCreateVideo</code> specifies whether this applet should enable
 *     the action that lets the user create a 3D video of the edited home. 
 *     <br>By default, the value of this parameter is <code>false</code>.</li>
 *     
 *     <li><code>showMemoryStatus</code> specifies whether this applet should display
 *     each second the available memory in browser status bar when it has focus. 
 *     <br>By default, the value of this parameter is <code>false</code> and 
 *     the status message won't be modified by the applet.</li>
 *     
 *     <li><code>userLanguage</code> specifies the ISO 639 code (fr, en...) of the 
 *     language used by the items displayed by this applet. 
 *     <br>By default, the selected language depends on the user environment.</li></ul>
 *     
 * <p>The bytecode of this class is Java 1.1 compatible to be able to notify users that 
 * it requires Java 5 when it's run under an old JVM.
 *     
 * @author Emmanuel Puybaret
 */
public class SweetHome3DApplet extends JApplet {
  private Object appletApplication;
   
  public void init() {
    if (!isJava5OrSuperior()) {
      showText(getLocalizedString("requirementsMessage"));
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
    return false;
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
   * Returns the localized string matching the given <code>key</code>. 
   */
  private String getLocalizedString(String key) {
    Class SweetHome3DAppletClass = SweetHome3DApplet.class;
    return ResourceBundle.getBundle(SweetHome3DAppletClass.getPackage().getName().replace('.', '/') + "/package").
        getString(SweetHome3DAppletClass.getName().substring(SweetHome3DAppletClass.getName().lastIndexOf('.') + 1) + "." + key);
  }
  
  /**
   * Shows the given text in a label.
   */
  private void showText(String text) {
    JLabel label = new JLabel(text, JLabel.CENTER);
    setContentPane(label);
  }
  
  /**
   * Reports the given exception at screen.
   */
  private void showError(Throwable ex) {
    showText("<html>" + getLocalizedString("startError") 
        + "<br>Exception " + ex.getClass().getName() 
        + (ex.getMessage() != null ? " " + ex.getMessage() : ""));
    ex.printStackTrace();
  }

  /**
   * Creates a new <code>AppletApplication</code> instance that manages this applet content.
   */
  private void createAppletApplication() {
    String applicationClassName = null;
    try {
      applicationClassName = getApplicationClassName();
      Class sweetHome3DAppletClass = SweetHome3DApplet.class;
      List java3DFiles = new ArrayList();
      if (!System.getProperty("os.name").startsWith("Mac OS X")
          || System.getProperty("java.version").startsWith("1.5")
          || System.getProperty("java.version").startsWith("1.6")) {
        java3DFiles.addAll(Arrays.asList(new String [] {
            "j3dcore.jar", // Main Java 3D jars
            "vecmath.jar",
            "j3dutils.jar",
            "macosx/gluegen-rt.jar", // Mac OS X jars and DLLs
            "macosx/jogl.jar",
            "macosx/libgluegen-rt.jnilib",
            "macosx/libjogl.jnilib",
            "macosx/libjogl_awt.jnilib",
            "macosx/libjogl_cg.jnilib"}));
      } else {
        java3DFiles.addAll(Arrays.asList(new String [] {
            "macosx/java3d-1.6/j3dcore.jar", // Mac OS X Java 3D 1.6 jars and DLLs
            "macosx/java3d-1.6/vecmath.jar",
            "macosx/java3d-1.6/j3dutils.jar",
            "macosx/java3d-1.6/gluegen.jar", 
            "macosx/java3d-1.6/jogl-java3d.jar",
            "macosx/java3d-1.6/libgluegen-rt.jnilib",
            "macosx/java3d-1.6/libjogl_desktop.jnilib",
            "macosx/java3d-1.6/libnativewindow_awt.jnilib",
            "macosx/java3d-1.6/libnativewindow_macosx.jnilib"}));
        // Disable JOGL library loader
        System.setProperty("jogamp.gluegen.UseTempJarCache", "false");
        System.setProperty("com.eteks.sweethome3d.j3d.useOffScreen3DView", "true");
      }
      if ("64".equals(System.getProperty("sun.arch.data.model"))) {
        java3DFiles.add("linux/x64/libj3dcore-ogl.so"); // Linux 64 bits DLLs
        java3DFiles.add("windows/x64/j3dcore-ogl.dll"); // Windows 64 bits DLLs
      } else {
        java3DFiles.addAll(Arrays.asList(new String [] {
            "linux/i386/libj3dcore-ogl.so", // Linux 32 bits DLLs
            "linux/i386/libj3dcore-ogl-cg.so", // Windows 32 bits DLLs
            "windows/i386/j3dcore-d3d.dll",
            "windows/i386/j3dcore-ogl.dll",
            "windows/i386/j3dcore-ogl-cg.dll",
            "windows/i386/j3dcore-ogl-chk.dll"}));
      }
      
      List applicationPackages = new ArrayList(Arrays.asList(new String [] {
          "com.eteks.sweethome3d",
          "com.eteks.sweethome3d",
          "javax.media",
          "javax.vecmath",
          "com.sun.j3d",
          "com.sun.opengl",
          "com.sun.gluegen.runtime",
          "com.jogamp",
          "jogamp",
          "javax.media.opengl",
          "javax.media.nativewindow",
          "com.sun.media",
          "com.ibm.media",
          "jmpapps.util",
          "com.microcrowd.loader.java3d",
          "org.sunflow",
          "org.apache.batik"}));
      applicationPackages.addAll(getPluginsPackages());
      
      if (!applicationClassName.startsWith((String)applicationPackages.get(0))) {
        String [] applicationClassParts = applicationClassName.split("\\.");
        String applicationClassPackageBase = ""; 
        // Contains the two first part of class package at most
        for (int i = 0, n = Math.min(applicationClassParts.length - 1, 2); i < n; i++) {
          if (i > 0) {
            applicationClassPackageBase += ".";
          }
          applicationClassPackageBase += applicationClassParts [i];
        }
        applicationPackages.add(applicationClassPackageBase);
      }
      
      ClassLoader extensionsClassLoader = new ExtensionsClassLoader(
          sweetHome3DAppletClass.getClassLoader(), sweetHome3DAppletClass.getProtectionDomain(),
          (String [])java3DFiles.toArray(new String [java3DFiles.size()]), 
          (String [])applicationPackages.toArray(new String [applicationPackages.size()]));
      startApplication(applicationClassName, extensionsClassLoader);
    } catch (AccessControlException ex) {
      String runWithoutSignature = getParameter("runWithoutSignature");
      if (runWithoutSignature != null && Boolean.parseBoolean(runWithoutSignature)) {
        // Try to run application without 3D
        startApplication(applicationClassName, getClass().getClassLoader());
      } else {
        showText(getLocalizedString("signatureError"));
      }
    } catch (Throwable ex) {
      showError(ex);
    }
  }

  private void startApplication(String applicationClassName, ClassLoader extensionsClassLoader) {
    try {
      // Call application constructor with reflection
      Class applicationClass = extensionsClassLoader.loadClass(applicationClassName);
      Constructor applicationConstructor = applicationClass.getConstructor(new Class [] {JApplet.class});
      this.appletApplication = applicationConstructor.newInstance(new Object [] {this});
    } catch (Exception ex) {
      showError(ex);
    }
  }

  /**
   * Returns the name of the {@linkplain AppletApplication application} class associated to this applet. 
   * This class must have a constructor taking in parameter a <code>JApplet</code>. 
   */
  protected String getApplicationClassName() {
    return "com.eteks.sweethome3d.applet.AppletApplication";
  }  
  
  /**
   * Returns the application instance created by the applet. 
   */
  protected Object getApplication() {
    return this.appletApplication;
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
