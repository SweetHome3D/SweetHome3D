/*
 * SweetHome3DBootstrap.java 2 sept. 07
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
package com.eteks.sweethome3d;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * This bootstrap class loads Sweet Home 3D application classes from jars in classpath 
 * or from jars stored in the jar file containing j3dcore.jar.
 * @author Emmanuel Puybaret
 */
public class SweetHome3DBootstrap {
  public static void main(String [] args) throws MalformedURLException, IllegalAccessException, 
        InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
    Class SweetHome3DBootstrapClass = SweetHome3DBootstrap.class;
    String [] applicationPackages = {
        "com.eteks.sweethome3d",
        "javax.media.j3d",
        "javax.vecmath",
        "com.sun.j3d",
        "com.sun.opengl",
        "com.sun.gluegen.runtime",
        "javax.media.opengl",
        "com.microcrowd.loader.java3d"};
    ClassLoader java3DClassLoader = new SweetHome3DClassLoader(
        SweetHome3DBootstrapClass.getClassLoader(), SweetHome3DBootstrapClass.getProtectionDomain(),
        applicationPackages);  
    
    String applicationClassName = "com.eteks.sweethome3d.SweetHome3D";
    Class applicationClass = java3DClassLoader.loadClass(applicationClassName);
    Method applicationClassMain = 
      applicationClass.getMethod("main", Array.newInstance(String.class, 0).getClass());
    // Call application class main method with reflection
    applicationClassMain.invoke(null, new Object [] {args});
  }

  /**
   * Class loader used by this bootstrap class to load 
   * all the other application classes.
   */
  private static class SweetHome3DClassLoader extends ClassLoader {
    private final ProtectionDomain protectionDomain;
    private final String [] applicationPackages;

    private final Map<String, String> java3DDLLs = new HashMap<String, String>();
    private JarFile [] java3DJars = null;


    private SweetHome3DClassLoader(ClassLoader parent, 
                                   ProtectionDomain protectionDomain, 
                                   String [] applicationPackages) {
      super(parent);
      this.protectionDomain = protectionDomain;
      this.applicationPackages = applicationPackages;

      // Compute DLLs prefix and suffix
      String dllSuffix;
      String dllPrefix;
      
      String os = System.getProperty("os.name");
      if (os.startsWith("Windows")) {
        dllSuffix = ".dll";
        dllPrefix = "";
      } else if (os.startsWith("Mac OS X")) {
        dllSuffix = ".jnilib";
        dllPrefix = "lib";
      } else {
        dllSuffix = ".so";
        dllPrefix = "lib";
      }
      
      // Find Java 3D core jar file
      URL java3DCoreUrl = getResource("j3dcore.jar");
      if (java3DCoreUrl != null
          && (java3DCoreUrl.toString().startsWith("jar:file")
              || java3DCoreUrl.toString().startsWith("jar:http"))) {
        try {
          String java3DFile;
          String urlString = java3DCoreUrl.toString();
          if (urlString.startsWith("jar:file:")) {
            // On Java 5, compute from which file Java 3D core jar comes
            java3DFile = urlString.substring("jar:file:".length(), urlString.indexOf('!'));
          } else {
            // From Java 6, java3DCoreUrl points to its origin URL   
            URL java3DURL = new URL(urlString.substring("jar:".length(), urlString.indexOf('!')));
            // Copy URL content to a tmp file
            java3DFile = copyInputStreamToTmpFile(java3DURL.openStream(), "jar");
          }
                    
          // Find the jars and the DLLs contained in java3DFile
          JarFile jarFile = new JarFile(java3DFile, false);
          ArrayList<JarFile> java3DJars = new ArrayList<JarFile>();
          for (Enumeration<JarEntry> jarEntryEnum = jarFile.entries(); 
               jarEntryEnum.hasMoreElements(); ) {
            JarEntry jarEntry = jarEntryEnum.nextElement();                
            String jarEntryName = jarEntry.getName();
            boolean jarEntryIsAJar = jarEntryName.endsWith(".jar");
            if (jarEntryIsAJar) {
              // Copy jar to a tmp file
              String java3DJar = copyInputStreamToTmpFile(jarFile.getInputStream(jarEntry), ".jar");
              // Add tmp file to Java 3D jars list
              java3DJars.add(new JarFile(java3DJar, false));
            } else if (jarEntryName.endsWith(dllSuffix)) {
              // Copy DLL to a tmp file
              String java3DDLL = copyInputStreamToTmpFile(jarFile.getInputStream(jarEntry), dllSuffix);
              // Add tmp file to Java 3D DLLs map
              java3DDLLs.put(jarEntryName.substring(dllPrefix.length(), 
                  jarEntryName.indexOf(dllSuffix)), java3DDLL);
            }
          }
          // Create java3DJars array
          this.java3DJars = java3DJars.toArray(new JarFile [java3DJars.size()]);                    
        } catch (IOException ex) {
          throw new RuntimeException("Couldn't extract Java 3D jars", ex);
        }
      }
    }

    /**
     * Returns the file name of a temporary copy of <code>input</code> content.
     */
    private String copyInputStreamToTmpFile(InputStream input, 
                                            String suffix) throws IOException {
      File java3DJar = File.createTempFile("Java3D", suffix);
      java3DJar.deleteOnExit();
      OutputStream output = null;
      try {
        output = new BufferedOutputStream(new FileOutputStream(java3DJar));
        byte [] buffer = new byte [8096];
        int size; 
        while ((size = input.read(buffer)) != -1) {
          output.write(buffer, 0, size);
        }
      } finally {
        if (input != null) {
          input.close();
        }
        if (output != null) {
          output.close();
        }
      }
      return java3DJar.toString();
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
      // Build class file from its name 
      String classFile = name.replace('.', '/') + ".class";
      InputStream classInputStream = null;
      // Check if searched class is a Java 3D class
      for (JarFile java3DJar : this.java3DJars) {
        JarEntry jarEntry = java3DJar.getJarEntry(classFile);
        if (jarEntry != null) {
          try {
            classInputStream = java3DJar.getInputStream(jarEntry);
          } catch (IOException ex) {
            throw new ClassNotFoundException("Couldn't read class " + name, ex);
          }
        }
      }
      // If it's not a Java 3D class, search if its an application 
      // class that can be read from resources
      if (classInputStream == null) {
        URL url = getResource(classFile);
        if (url == null) {
          throw new ClassNotFoundException("Class " + name);
        }
        try {
          classInputStream = url.openStream();
        } catch (IOException ex) {
          throw new ClassNotFoundException("Couldn't read class " + name, ex);
        }
      } 
      
      try {
        // Read class input content to a byte array
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BufferedInputStream in = new BufferedInputStream(classInputStream);
        byte [] buffer = new byte [8096];
        int size; 
        while ((size = in.read(buffer)) != -1) {
          out.write(buffer, 0, size);
        }
        in.close();
        // Define class
        return defineClass(name, out.toByteArray(), 0, out.size(), 
            this.protectionDomain);
      } catch (IOException ex) {
        throw new ClassNotFoundException("Class " + name, ex);
      }
    }

    @Override
    protected String findLibrary(String libname) {
      return this.java3DDLLs.get(libname);
    }

    @Override
    protected URL findResource(String name) {
      if (this.java3DJars != null) {
        // Try to find if resource belongs to one of the extracted jars
        for (JarFile java3DJar : this.java3DJars) {
          JarEntry jarEntry = java3DJar.getJarEntry(name);
          if (jarEntry != null) {
            try {
              return new URL("jar:file:" + java3DJar.getName() + ":" + jarEntry.getName());
            } catch (MalformedURLException ex) {
              // Forget that we could have found a resource
            }
          }
        }
      }
      return super.findResource(name);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
      // If Java 3D jars couldn't be found
      if (this.java3DJars == null) {
        // Let default class loader do its job
        return super.loadClass(name, resolve);
      }
      // First, check if the class has already been loaded
      Class loadedClass = findLoadedClass(name);
      if (loadedClass == null) {
        try {
          // Try to find if class belongs to one of the application packages
          for (String applicationPackage : applicationPackages) {
            if (name.startsWith(applicationPackage)) {
              loadedClass = findClass(name);
              break;
            }
          }
        } catch (ClassNotFoundException ex) {
          // Let a chance to class to be loaded by default implementation
        }
        if (loadedClass == null) {
          loadedClass = super.loadClass(name, resolve);
        }
      }
      if (resolve) {
        resolveClass(loadedClass);
      }
      return loadedClass;
    }
  }
}