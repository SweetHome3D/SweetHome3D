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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * SweetHome3DBootstrap works with jars in classpath and java.library.path set.
 * @author Emmanuel Puybaret
 */
public class SweetHome3DBootstrap {
  public static void main(String [] args) throws MalformedURLException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
    final Class SweetHome3DBootstrapClass = SweetHome3DBootstrap.class;

    ClassLoader java3DClassLoader = new ClassLoader(SweetHome3DBootstrapClass.getClassLoader()) {
        private JarFile [] java3DJars;
        
        
        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
          if (this.java3DJars == null) {
            // Initialize Java 3D jars
            URL url = getResource("com/sun/gluegen/runtime/CPU.class");
            String urlString = url.toString();
            int libIndex = urlString.indexOf("gluegen-rt");
            String prefix = urlString.substring("jar:file:".length(), libIndex);
            String suffix = urlString.substring(libIndex + "gluegen-rt".length(), urlString.indexOf('!'));
            try {
              this.java3DJars = new JarFile [] {
                  new JarFile(prefix + "j3dcore" + suffix),                                
                  new JarFile(prefix + "vecmath" + suffix),                                
                  new JarFile(prefix + "j3dutils" + suffix),                                
              };
            } catch (IOException ex) {
              throw new ClassNotFoundException("Class " + name, ex);
            }
          }

          InputStream inputStream = null;
          String classFile = name.replace('.', '/') + ".class";
          if (name.startsWith("com.eteks.sweethome3d")
              || name.startsWith("com.microcrowd.loader.java3d")) {
            URL url = getResource(classFile);
            if (url == null) {
              throw new ClassNotFoundException("Class " + name);
            }
            try {
              inputStream = url.openStream();
            } catch (IOException ex) {
              throw new ClassNotFoundException("Class " + name, ex);
            }
          } else {
            for (JarFile java3DJar : this.java3DJars) {
              JarEntry jarEntry = java3DJar.getJarEntry(classFile);
              if (jarEntry != null) {
                try {
                  inputStream = java3DJar.getInputStream(jarEntry);
                } catch (IOException ex) {
                  throw new ClassNotFoundException("Class " + name, ex);
                }
              }
            }
          }
          
          try {
            // Read class input content
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            BufferedInputStream in = new BufferedInputStream(inputStream);
            byte [] buffer = new byte [8096];
            int size; 
            while ((size = in.read(buffer)) != -1) {
              out.write(buffer, 0, size);
            }
            in.close();
            return defineClass(name, out.toByteArray(), 0, out.size(), SweetHome3DBootstrapClass.getProtectionDomain());
          } catch (IOException ex) {
            throw new ClassNotFoundException("Class " + name, ex);
          }
        }
      
        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
          // First, check if the class has already been loaded
          Class loadedClass = findLoadedClass(name);
          if (loadedClass == null) {
            try {
              if (name.startsWith("com.eteks.sweethome3d")
                  || name.startsWith("javax.media.j3d")
                  || name.startsWith("javax.vecmath")
                  || name.startsWith("com.sun.j3d")
                  || name.startsWith("com.microcrowd.loader.java3d")) {
                loadedClass = findClass(name);
                System.out.println("-> found class " + name);
              } else {
                loadedClass = super.loadClass(name, resolve);
              }
            } catch(ClassNotFoundException ex) {
              loadedClass = super.loadClass(name, resolve);
            }
          }
          if (resolve) {
            resolveClass(loadedClass);
          }
          return loadedClass;
        }
      };
      
    java3DClassLoader.loadClass("com.eteks.sweethome3d.SweetHome3D").
        getMethod("main", Array.newInstance(String.class, 0).getClass()).invoke(null, new Object [] {args});
  }
}