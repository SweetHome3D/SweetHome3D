/*
 * ExtensionsClassLoader.java 2 sept. 2007
 *
 * Sweet Home 3D, Copyright (c) 2007-2008 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Class loader able to load classes and DLLs with a higher priority from a given set of JARs. 
 * Its bytecode is Java 1.1 compatible to be loadable by old JVMs.
 * @author Emmanuel Puybaret
 */
public class ExtensionsClassLoader extends ClassLoader {
  private final ProtectionDomain protectionDomain;
  private final String []        applicationPackages;

  private final Map    extensionDlls = new HashMap();
  private JarFile []   extensionJars = null;

  /**
   * Creates a class loader. It will consider JARs and DLLs of <code>extensionJarsAndDlls</code> accessed as resources
   * as classpath and libclasspath elements with a higher priority than the ones of default classpath, 
   * and will load itself all the classes belonging to packages of <code>applicationPackages</code>.
   * No cache will be used.
   */
  public ExtensionsClassLoader(ClassLoader parent, 
                               ProtectionDomain protectionDomain, 
                               String [] extensionJarsAndDlls,
                               String [] applicationPackages) {
    this(parent, protectionDomain, extensionJarsAndDlls, new URL [0], applicationPackages, null, null);
  }
  
  /**
   * Creates a class loader. It will consider JARs and DLLs of <code>extensionJarAndDllResources</code>
   * and <code>extensionJarAndDllUrls</code> as classpath and libclasspath elements with a higher priority 
   * than the ones of default classpath, and will load itself all the classes belonging to packages of 
   * <code>applicationPackages</code>.<br>
   * Copies of <code>extensionJarAndDllResources</code> and <code>extensionJarAndDllUrls</code> will be stored 
   * in the given cache folder, each file being prefixed by <code>cachedFilesPrefix</code>.
   */
  public ExtensionsClassLoader(ClassLoader parent, 
                               ProtectionDomain protectionDomain, 
                               String [] extensionJarAndDllResources,
                               URL [] extensionJarAndDllUrls,
                               String [] applicationPackages,
                               File cacheFolder,
                               String cachedFilesPrefix) {
    super(parent);
    this.protectionDomain = protectionDomain;
    this.applicationPackages = applicationPackages;
    String extensionPrefix = cachedFilesPrefix == null ? "" : cachedFilesPrefix;

    // Compute DLLs prefix and suffix
    String dllSuffix;
    String dllPrefix;
    
    String osName = System.getProperty("os.name");
    if (osName.startsWith("Windows")) {
      dllSuffix = ".dll";
      dllPrefix = "";
    } else if (osName.startsWith("Mac OS X")) {
      dllSuffix = ".jnilib";
      dllPrefix = "lib";
    } else {
      dllSuffix = ".so";
      dllPrefix = "lib";
    }
    
    // Create a list containing only URLs
    ArrayList extensionJarsAndDlls = new ArrayList();
    for (int i = 0; i < extensionJarAndDllResources.length; i++) {
      URL extensionJarOrDllUrl = getResource(extensionJarAndDllResources [i]);
      if (extensionJarOrDllUrl != null) {
        extensionJarsAndDlls.add(extensionJarOrDllUrl);
      }
    }
    extensionJarsAndDlls.addAll(Arrays.asList(extensionJarAndDllUrls));
    
    // Find extension Jars and DLLs
    ArrayList extensionJars = new ArrayList();
    for (int i = 0; i < extensionJarsAndDlls.size(); i++) {
      URL extensionJarOrDllUrl = (URL)extensionJarsAndDlls.get(i);
      try {
        String extensionJarOrDllUrlFile = extensionJarOrDllUrl.getFile();
        URLConnection connection = null;
        long extensionJarOrDllFileDate;
        String extensionJarOrDllFile;
        if (extensionJarOrDllUrl.getProtocol().equals("jar")) {
          // Don't instantiate connection to a file accessed by jar protocol otherwise it might download again its jar container
          URL jarEntryUrl = new URL(extensionJarOrDllUrlFile.substring(0, extensionJarOrDllUrlFile.indexOf('!')));
          URLConnection jarEntryUrlConnection = jarEntryUrl.openConnection(); 
          // As connection.getLastModified() on an entry returns get modification date of the jar file itself 
          extensionJarOrDllFileDate = jarEntryUrlConnection.getLastModified();
          extensionJarOrDllFile = extensionJarOrDllUrlFile.substring(extensionJarOrDllUrlFile.indexOf('!') + 2);
        } else {
          connection = extensionJarOrDllUrl.openConnection();
          extensionJarOrDllFileDate = connection.getLastModified();
          extensionJarOrDllFile = extensionJarOrDllUrlFile;
        }        
        String extensionJarOrDllFileName;
        int lastSlashIndex = extensionJarOrDllFile.lastIndexOf('/');
        if (extensionJarOrDllFile.endsWith(".jar")) {
          extensionJarOrDllFileName = extensionPrefix 
              + extensionJarOrDllFile.substring(lastSlashIndex + 1);
        } else {
          extensionJarOrDllFileName = extensionPrefix 
              + extensionJarOrDllFile.substring(lastSlashIndex + 1 + dllPrefix.length());
        }
        
        if (cacheFolder != null 
            && ((cacheFolder.exists()
                  && cacheFolder.isDirectory())
                || cacheFolder.mkdirs())) {
          try {
            File cachedFile = new File(cacheFolder, extensionJarOrDllFileName);            
            if (!cachedFile.exists() 
                || cachedFile.lastModified() < extensionJarOrDllFileDate) {
              // Copy jar to cache
              if (connection == null) {
                connection = extensionJarOrDllUrl.openConnection();
              }
              copyInputStreamToFile(connection.getInputStream(), cachedFile);
            }
            if (extensionJarOrDllFile.endsWith(".jar")) {
              // Add tmp file to extension jars list
              extensionJars.add(new JarFile(cachedFile.toString(), false));
            } else if (extensionJarOrDllFile.endsWith(dllSuffix)) {
              // Add tmp file to extension DLLs map
              this.extensionDlls.put(extensionJarOrDllFileName.substring(extensionPrefix.length(), 
                  extensionJarOrDllFileName.indexOf(dllSuffix)), cachedFile.toString());
            }
            continue;
          } catch (IOException ex) {
            // Try without cache
          }          
        } 
        
        if (connection == null) {
          connection = extensionJarOrDllUrl.openConnection();
        }
        InputStream input = connection.getInputStream();          
        if (extensionJarOrDllFile.endsWith(".jar")) {
          // Copy jar to a tmp file
          String extensionJar = copyInputStreamToTmpFile(input, ".jar");
          // Add tmp file to extension jars list
          extensionJars.add(new JarFile(extensionJar, false));
        } else if (extensionJarOrDllFile.endsWith(dllSuffix)) {
          // Copy DLL to a tmp file
          String extensionDll = copyInputStreamToTmpFile(input, dllSuffix);
          // Add tmp file to extension DLLs map
          this.extensionDlls.put(extensionJarOrDllFileName.substring(extensionPrefix.length(), 
              extensionJarOrDllFileName.indexOf(dllSuffix)), extensionDll);
        }          
      } catch (IOException ex) {
        throw new RuntimeException("Couldn't extract extension jars", ex);
      }
    }
    
    // Create extensionJars array
    if (extensionJars.size() > 0) {
      this.extensionJars = (JarFile [])extensionJars.toArray(new JarFile [extensionJars.size()]);                    
    }
  }

  /**
   * Returns the file name of a temporary copy of <code>input</code> content.
   */
  private String copyInputStreamToTmpFile(InputStream input, 
                                          String suffix) throws IOException {
    File tmpFile = File.createTempFile("extension", suffix);
    tmpFile.deleteOnExit();
    copyInputStreamToFile(input, tmpFile);
    return tmpFile.toString();
  }

  /**
   * Copies the <code>input</code> content to the given file.
   */
  public void copyInputStreamToFile(InputStream input, File file) throws FileNotFoundException, IOException {
    OutputStream output = null;
    try {
      output = new BufferedOutputStream(new FileOutputStream(file));
      byte [] buffer = new byte [8192];
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
  }
  
  /**
   * Finds and defines the given class among the extension JARs  
   * given in constructor, then among resources. 
   */
  protected Class findClass(String name) throws ClassNotFoundException {
    // Build class file from its name 
    String classFile = name.replace('.', '/') + ".class";
    InputStream classInputStream = null;
    if (this.extensionJars != null) {
      // Check if searched class is an extension class
      for (int i = 0; i < this.extensionJars.length; i++) {
        JarFile extensionJar = this.extensionJars [i];
        JarEntry jarEntry = extensionJar.getJarEntry(classFile);
        if (jarEntry != null) {
          try {
            classInputStream = extensionJar.getInputStream(jarEntry);
          } catch (IOException ex) {
            throw new ClassNotFoundException("Couldn't read class " + name, ex);
          }
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
      byte [] buffer = new byte [8192];
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
  
  /**
   * Returns the library path of an extension DLL.
   */
  protected String findLibrary(String libname) {
    return (String)this.extensionDlls.get(libname);
  }
  
  /**
   * Returns the URL of the given resource searching first if it exists among 
   * the extension JARs given in constructor. 
   */
  protected URL findResource(String name) {
    if (this.extensionJars != null) {
      // Try to find if resource belongs to one of the extracted jars
      for (int i = 0; i < this.extensionJars.length; i++) {
        JarFile extensionJar = this.extensionJars [i];
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

  /**
   * Loads a class with this class loader if its package belongs to <code>applicationPackages</code>
   * given in constructor.
   */
  protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
    // If no extension jars couldn't be found
    if (this.extensionJars == null) {
      // Let default class loader do its job
      return super.loadClass(name, resolve);
    }
    // Check if the class has already been loaded
    Class loadedClass = findLoadedClass(name);
    if (loadedClass == null) {
      try {
        // Try to find if class belongs to one of the application packages
        for (int i = 0; i < this.applicationPackages.length; i++) {
          String applicationPackage = this.applicationPackages [i];
          int applicationPackageLength = applicationPackage.length();
          if (   (applicationPackageLength == 0 
                 && name.indexOf('.') == 0)
              || (applicationPackageLength > 0
                 && name.startsWith(applicationPackage))) {
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
