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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JApplet;
import javax.swing.JLabel;

import com.eteks.sweethome3d.tools.ExtensionsClassLoader;

/**
 * This applet class displays the 3D view of a Sweet Home 3D file from its URL.
 * <p>This applet accepts the following parameters:
 * 
 * <ul><li><code>homeURL</code> specifies the URL of the home that will be downloaded 
 *     and displayed at applet launch. 
 *     <br>This URL should return a content at SH3D file format. If it's not an absolute URL, 
 *     it will be read relative to applet document base. By default, this parameter is 
 *     equal to <code>default.sh3d</code>.</li>
 *     
 *     <li><code>ignoreCache</code> specifies whether home file may be read from Java 
 *     cache or not. 
 *     <br>If its value is <code>true</code>, then each time the applet is launched the
 *     home file will be downloaded ignoring the file that may exist in cache. 
 *     By default, its value is <code>false</code>.</li></ul>
 *     
 * <p>Use space bar to switch between aerial view and virtual visitor view.</p>
 * 
 * <p>The bytecode of this class is Java 1.1 compatible to be able to notify users that 
 * it requires Java 5 when it's run under an old JVM.
 *     
 * @author Emmanuel Puybaret
 */
public class SweetHome3DViewer extends JApplet {
  private Object appletApplication;
  
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
    System.gc();
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
      Class sweetHome3DAppletClass = SweetHome3DViewer.class;
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
      
      ClassLoader extensionsClassLoader = new ExtensionsClassLoader(
          sweetHome3DAppletClass.getClassLoader(), sweetHome3DAppletClass.getProtectionDomain(),
          java3DFiles, (String [])applicationPackages.toArray(new String [applicationPackages.size()]));
      
      // Call application constructor with reflection
      String applicationClassName = "com.eteks.sweethome3d.applet.ViewerHelper";
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
}
