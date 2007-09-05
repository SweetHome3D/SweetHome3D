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
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * This bootstrap class loads Sweet Home 3D application classes from jars in classpath 
 * or from extension jars stored as resources.
 * @author Emmanuel Puybaret
 */
public class SweetHome3DBootstrap {
  public static void main(String [] args) throws MalformedURLException, IllegalAccessException, 
        InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
    Class SweetHome3DBootstrapClass = SweetHome3DBootstrap.class;
    String [] java3DFiles = {
      "j3dcore.jar", // Main Java 3D jars
      "vecmath.jar",
      "j3dutils.jar",
      "j3dcore-d3d.dll", // Windows DLLs
      "j3dcore-ogl.dll",
      "j3dutils.dll",
      "libj3dcore-ogl.so", // Linux DLLs
      "libj3dcore-ogl-cg.so",
      "gluegen-rt.jar", // Mac OS X jars and DLLs
      "jogl.jar",
      "libgluegen-rt.jnilib",
      "libjogl.jnilib",
      "libjogl_awt.jnilib",
      "libjogl_cg.jnilib"};
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
        java3DFiles, applicationPackages);  
    
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

    private final Map<String, String> extensionDlls = new HashMap<String, String>();
    private JarFile [] extensionJars = null;


    private SweetHome3DClassLoader(ClassLoader parent, 
                                   ProtectionDomain protectionDomain, 
                                   String [] extensionJarsAndDlls,
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
      
      // Find extension Jars and DLLs
      ArrayList<JarFile> extensionJars = new ArrayList<JarFile>();
      for (String extensionJarOrDll : extensionJarsAndDlls) {
        try {
          URL extensionJarOrDllUrl = getResource(extensionJarOrDll);
          if (extensionJarOrDllUrl != null) {
            if (extensionJarOrDll.endsWith(".jar")) {
              // Copy jar to a tmp file
              String extensionJar = copyInputStreamToTmpFile(extensionJarOrDllUrl.openStream(), ".jar");
              // Add tmp file to extension jars list
              extensionJars.add(new JarFile(extensionJar, false));
            } else if (extensionJarOrDll.endsWith(dllSuffix)) {
              // Copy DLL to a tmp file
              String extensionDll = copyInputStreamToTmpFile(extensionJarOrDllUrl.openStream(), dllSuffix);
              // Add tmp file to extension DLLs map
              this.extensionDlls.put(extensionJarOrDll.substring(dllPrefix.length(), 
                  extensionJarOrDll.indexOf(dllSuffix)), extensionDll);
            }          
          }
        } catch (IOException ex) {
          throw new RuntimeException("Couldn't extract extension jars", ex);
        }
      }
      // Create extensionJars array
      if (extensionJars.size() > 0) {
        this.extensionJars = extensionJars.toArray(new JarFile [extensionJars.size()]);                    
      }
    }

    /**
     * Returns the file name of a temporary copy of <code>input</code> content.
     */
    private String copyInputStreamToTmpFile(InputStream input, 
                                            String suffix) throws IOException {
      File tmpFile = File.createTempFile("extension", suffix);
      tmpFile.deleteOnExit();
      OutputStream output = null;
      try {
        output = new BufferedOutputStream(new FileOutputStream(tmpFile));
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
      return tmpFile.toString();
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
      // Build class file from its name 
      String classFile = name.replace('.', '/') + ".class";
      InputStream classInputStream = null;
      // Check if searched class is an extension class
      for (JarFile extensionJar : this.extensionJars) {
        JarEntry jarEntry = extensionJar.getJarEntry(classFile);
        if (jarEntry != null) {
          try {
            classInputStream = extensionJar.getInputStream(jarEntry);
          } catch (IOException ex) {
            throw new ClassNotFoundException("Couldn't read class " + name, ex);
          }
        }
      }
      // If it's not an extension class, search if its an application 
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
      return this.extensionDlls.get(libname);
    }

    @Override
    protected URL findResource(String name) {
      if (this.extensionJars != null) {
        // Try to find if resource belongs to one of the extracted jars
        for (JarFile extensionJar : this.extensionJars) {
          JarEntry jarEntry = extensionJar.getJarEntry(name);
          if (jarEntry != null) {
            try {
              return new URL("jar:file:" + extensionJar.getName() + ":" + jarEntry.getName());
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
      // If no extension jars couldn't be found
      if (this.extensionJars == null) {
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