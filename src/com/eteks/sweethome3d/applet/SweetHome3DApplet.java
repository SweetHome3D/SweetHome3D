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

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.TextArea;
import java.lang.reflect.Constructor;

import com.eteks.sweethome3d.tools.ExtensionsClassLoader;

/**
 * This applet class loads Sweet Home 3classes from jars in classpath 
 * or from extension jars stored as resources.
 * It's Java 1.1 compatible to be loadable by old JVMs.
 * @author Emmanuel Puybaret
 */
public class SweetHome3DApplet extends Applet {
  static {
    initSystemProperties();
  }
  
  /**
   * Sets various <code>System</code> properties required to be set before Applet is displayed.
   */
  private static void initSystemProperties() {
    // Enables Java 5 bug correction about dragging directly
    // a tree element without selecting it before :
    // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4521075
    System.setProperty("sun.swing.enableImprovedDragGesture", "true");
    // Use Quartz renderer under Mac OS X
    System.setProperty("apple.awt.graphics.UseQuartz", "true");
  }
  
  public void init() {
    setLayout(new BorderLayout());
    try {
      if (!isJava5OrSuperior()) {
        showError("This applet may be run under Windows, Mac OS X 10.4 / 10.5 and Linux.\n" +
            "It requires Java version 5 or superior.\n" +
            "Please, update your Java Runtime to the latest version available at java.com");
      } else {
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
        String [] applicationPackages = {
            "com.domusventures.floorplanner",
            "com.eteks.sweethome3d",
            "javax.media.j3d",
            "javax.vecmath",
            "com.sun.j3d",
            "com.sun.opengl",
            "com.sun.gluegen.runtime",
            "javax.media.opengl",
            "com.microcrowd.loader.java3d"};
        ClassLoader extensionsClassLoader = new ExtensionsClassLoader(
            sweetHome3DAppletClass.getClassLoader(), sweetHome3DAppletClass.getProtectionDomain(),
            java3DFiles, applicationPackages);  
        // Call application constructor with reflection
        String applicationClassName = "com.eteks.sweethome3d.applet.AppletApplication";
        Class applicationClass = extensionsClassLoader.loadClass(applicationClassName);
        Constructor applicationConstructor = 
            applicationClass.getConstructor(new Class [] {Applet.class});
        applicationConstructor.newInstance(new Object [] {this});
      }
    } catch (Throwable ex) {
      showError("Can't start applet:\nException" 
          + ex.getClass().getName() + " " + ex.getMessage());
      ex.printStackTrace();
    }
  }
  
  private void showError(String text) {
    TextArea textArea = new TextArea(text, 10, 10, TextArea.SCROLLBARS_NONE);
    textArea.setEditable(false);
    removeAll();    
    add(textArea, BorderLayout.CENTER);
    validate();
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
}
