/*
 * PluginManager.java 24 oct. 2008
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
package com.eteks.sweethome3d.plugin;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.CollectionEvent;
import com.eteks.sweethome3d.model.CollectionListener;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeApplication;
import com.eteks.sweethome3d.model.Library;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.HomeController;

/**
 * Sweet Home 3D plug-ins manager.
 * @author Emmanuel Puybaret
 */
public class PluginManager {
  public static final String PLUGIN_LIBRARY_TYPE = "Plugin"; 
  
  private static final String ID                          = "id";
  private static final String NAME                        = "name";
  private static final String CLASS                       = "class";
  private static final String DESCRIPTION                 = "description";
  private static final String VERSION                     = "version";
  private static final String LICENSE                     = "license";
  private static final String PROVIDER                    = "provider";
  private static final String APPLICATION_MINIMUM_VERSION = "applicationMinimumVersion";
  private static final String JAVA_MINIMUM_VERSION        = "javaMinimumVersion";

  private static final String APPLICATION_PLUGIN_FAMILY   = "ApplicationPlugin";

  private static final String DEFAULT_APPLICATION_PLUGIN_PROPERTIES_FILE = 
      APPLICATION_PLUGIN_FAMILY + ".properties";

  private final File [] pluginFolders;
  private final Map<String, PluginLibrary> pluginLibraries = 
      new TreeMap<String, PluginLibrary>();
  private final Map<Home, List<Plugin>> homePlugins = new LinkedHashMap<Home, List<Plugin>>();
  
  /**
   * Reads application plug-ins from resources in the given plug-in folder.
   */
  public PluginManager(File pluginFolder) {
    this(new File [] {pluginFolder});
  }
  
  /**
   * Reads application plug-ins from resources in the given plug-in folders.
   * @since 3.0
   */
  public PluginManager(File [] pluginFolders) {
    this.pluginFolders = pluginFolders;
    if (pluginFolders != null) {
      for (File pluginFolder : pluginFolders) {
        // Try to load plugin files from plugin folder
        File [] pluginFiles = pluginFolder.listFiles(new FileFilter () {
          public boolean accept(File pathname) {
            return pathname.isFile();
          }
        });
        
        if (pluginFiles != null) {
          // Treat plug in files in reverse order of their version number
          Arrays.sort(pluginFiles, Collections.reverseOrder(OperatingSystem.getFileVersionComparator()));
          for (File pluginFile : pluginFiles) {
            try {
              loadPlugins(pluginFile.toURI().toURL(), pluginFile.getAbsolutePath());
            } catch (MalformedURLException ex) {
              // Files are supposed to exist !
            }
          }
        }
      }
    }
  }

  /**
   * Reads application plug-ins from resources in the given URLs.
   */
  public PluginManager(URL [] pluginUrls) {
    this.pluginFolders = null;
    for (URL pluginUrl : pluginUrls) {
      loadPlugins(pluginUrl, pluginUrl.toExternalForm());
    }
  }

  /**
   * Loads the plug-ins that may be available in the given URL.
   */
  private void loadPlugins(URL pluginUrl, String pluginLocation) {
    ZipInputStream zipIn = null;
    try {
      // Open a zip input from pluginUrl
      zipIn = new ZipInputStream(pluginUrl.openStream());
      // Try do find a plugin properties file in current zip stream  
      for (ZipEntry entry; (entry = zipIn.getNextEntry()) != null; ) {
        String zipEntryName = entry.getName();
        int lastIndex = zipEntryName.lastIndexOf(DEFAULT_APPLICATION_PLUGIN_PROPERTIES_FILE);
        if (lastIndex != -1
            && (lastIndex == 0
                || zipEntryName.charAt(lastIndex - 1) == '/')) {
          try {
            // Build application plugin family with its package 
            String applicationPluginFamily = zipEntryName.substring(0, lastIndex);
            applicationPluginFamily += APPLICATION_PLUGIN_FAMILY;
            ClassLoader classLoader = new URLClassLoader(new URL [] {pluginUrl}, getClass().getClassLoader());
            readPlugin(ResourceBundle.getBundle(applicationPluginFamily, Locale.getDefault(), classLoader), 
                pluginLocation, 
                "jar:" + pluginUrl.toString() + "!/" + URLEncoder.encode(zipEntryName, "UTF-8").replace("+", "%20"),
                classLoader);
          } catch (MissingResourceException ex) {
            // Ignore malformed plugins
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
  }
  
  /**
   * Reads the plug-in properties from the given <code>resource</code>.
   */
  private void readPlugin(ResourceBundle resource,
                          String         pluginLocation,
                          String         pluginEntry,
                          ClassLoader    pluginClassLoader) {
    try {
      String name = resource.getString(NAME);

      // Check Java and application versions
      String javaMinimumVersion = resource.getString(JAVA_MINIMUM_VERSION);
      if (!OperatingSystem.isJavaVersionAtLeast(javaMinimumVersion)) {
        System.err.println("Invalid plug-in " + pluginEntry + ":\n" 
            + "Not compatible Java version " + System.getProperty("java.version"));
        return;
      }
      
      String applicationMinimumVersion = resource.getString(APPLICATION_MINIMUM_VERSION);
      if (!isApplicationVersionSuperiorTo(applicationMinimumVersion)) {
        System.err.println("Invalid plug-in " + pluginEntry + ":\n" 
            + "Not compatible application version");
        return;
      }
      
      String pluginClassName = resource.getString(CLASS);
      Class<? extends Plugin> pluginClass = getPluginClass(pluginClassLoader, pluginClassName);
      
      String id = getOptionalString(resource, ID, null);
      String description = resource.getString(DESCRIPTION);
      String version = resource.getString(VERSION);
      String license = resource.getString(LICENSE);
      String provider = resource.getString(PROVIDER);
      
      // Store plug-in properties if they don't exist yet
      if (this.pluginLibraries.get(name) == null) {
        this.pluginLibraries.put(name, new PluginLibrary(
            pluginLocation, id, name, description, version, license, provider, pluginClass, pluginClassLoader));
      }      
    } catch (MissingResourceException ex) {
      System.err.println("Invalid plug-in " + pluginEntry + ":\n" + ex.getMessage());
    } catch (IllegalArgumentException ex) {
      System.err.println("Invalid plug-in " + pluginEntry + ":\n" + ex.getMessage());
    } 
  }

  /**
   * Returns the value of the property with the given <code>key</code> or the default value 
   * if the property isn't defined.
   */
  private String getOptionalString(ResourceBundle resource, String key, String defaultValue) {
    try {
      return resource.getString(key);
    } catch (MissingResourceException ex) {
      return defaultValue;
    }
  }
  
  /**
   * Returns <code>true</code> if the given version is smaller than the version 
   * of the application. Versions are compared only on their first two parts.
   */
  private boolean isApplicationVersionSuperiorTo(String applicationMinimumVersion) {
    String [] applicationMinimumVersionParts = applicationMinimumVersion.split("\\.|_|\\s");
    if (applicationMinimumVersionParts.length >= 1) {
      try {
        // Compare digits in first part
        int applicationVersionFirstPart = (int)(Home.CURRENT_VERSION / 1000);
        int applicationMinimumVersionFirstPart = Integer.parseInt(applicationMinimumVersionParts [0]);        
        if (applicationVersionFirstPart > applicationMinimumVersionFirstPart) {
          return true;
        } else if (applicationVersionFirstPart == applicationMinimumVersionFirstPart 
                   && applicationMinimumVersionParts.length >= 2) { 
          // Compare digits in second part
          return ((Home.CURRENT_VERSION / 100) % 10) >= Integer.parseInt(applicationMinimumVersionParts [1]);
        }
      } catch (NumberFormatException ex) {
      }
    }
    return false;
  }
  
  /**
   * Returns the <code>Class</code> instance of the class named <code>pluginClassName</code>,
   * after checking plug-in class exists, may be instantiated and has a default public constructor.
   */
  @SuppressWarnings("unchecked")
  private Class<? extends Plugin> getPluginClass(ClassLoader pluginClassLoader,
                                                 String pluginClassName) {
    try {
      Class<? extends Plugin> pluginClass = 
          (Class<? extends Plugin>)pluginClassLoader.loadClass(pluginClassName);
      if (!Plugin.class.isAssignableFrom(pluginClass)) {
        throw new IllegalArgumentException(
            pluginClassName + " not a subclass of " + Plugin.class.getName());
      } else if (Modifier.isAbstract(pluginClass.getModifiers())
                 || !Modifier.isPublic(pluginClass.getModifiers())) {
        throw new IllegalArgumentException( 
            pluginClassName + " not a public static class");
      }
      Constructor<? extends Plugin> constructor = pluginClass.getConstructor(new Class [0]);
      if (!Modifier.isPublic(constructor.getModifiers())) {
        throw new IllegalArgumentException( 
            pluginClassName + " constructor not accessible");
      }
      return pluginClass;
    } catch (NoClassDefFoundError ex) {
      throw new IllegalArgumentException(ex.getMessage(), ex);
    } catch (ClassNotFoundException ex) {
      throw new IllegalArgumentException(ex.getMessage(), ex);
    } catch (NoSuchMethodException ex) {
      throw new IllegalArgumentException(ex.getMessage(), ex);
    }
  }

  /**
   * Returns the available plug-in libraries.
   * @since 4.0
   */
  public List<Library> getPluginLibraries() {
    return Collections.unmodifiableList(new ArrayList<Library>(this.pluginLibraries.values()));
  }
  
  /**
   * Returns an unmodifiable list of plug-in instances initialized with the 
   * given parameters.
   */
  public List<Plugin> getPlugins(final HomeApplication application, 
                                 final Home home, 
                                 UserPreferences preferences,                                 
                                 UndoableEditSupport undoSupport) {
    return getPlugins(application, home, preferences, null, undoSupport);
  }
    
  /**
   * Returns an unmodifiable list of plug-in instances initialized with the 
   * given parameters.
   * @since 3.5
   */
  List<Plugin> getPlugins(final HomeApplication application, 
                          final Home home, 
                          UserPreferences preferences,
                          HomeController homeController,
                          UndoableEditSupport undoSupport) {
    if (application.getHomes().contains(home)) {
      List<Plugin> plugins = this.homePlugins.get(home);
      if (plugins == null) {
        plugins = new ArrayList<Plugin>();
        // Instantiate each plug-in class
        for (PluginLibrary pluginLibrary : this.pluginLibraries.values()) {
          try {
            Plugin plugin = pluginLibrary.getPluginClass().newInstance();                      
            plugin.setPluginClassLoader(pluginLibrary.getPluginClassLoader());
            plugin.setName(pluginLibrary.getName());
            plugin.setDescription(pluginLibrary.getDescription());
            plugin.setVersion(pluginLibrary.getVersion());
            plugin.setLicense(pluginLibrary.getLicense());
            plugin.setProvider(pluginLibrary.getProvider());
            plugin.setUserPreferences(preferences);
            plugin.setHome(home);
            plugin.setHomeController(homeController);
            plugin.setUndoableEditSupport(undoSupport);
            plugins.add(plugin);
          } catch (InstantiationException ex) {
            // Shouldn't happen : plug-in class was checked during readPlugin call
            throw new RuntimeException(ex);
          } catch (IllegalAccessException ex) {
            // Shouldn't happen : plug-in class was checked during readPlugin call
            throw new RuntimeException(ex);
          } 
        }
        
        plugins = Collections.unmodifiableList(plugins);
        this.homePlugins.put(home, plugins);
        
        // Add a listener that will destroy all plug-ins when home is deleted
        application.addHomesListener(new CollectionListener<Home>() {
            public void collectionChanged(CollectionEvent<Home> ev) {
              if (ev.getType() == CollectionEvent.Type.DELETE
                  && ev.getItem() == home) {
                for (Plugin plugin : homePlugins.get(home)) {
                  plugin.destroy();
                }                
                homePlugins.remove(home);
                application.removeHomesListener(this);
              }
            }
          });
      }
      return plugins;
    } else {
      return Collections.emptyList();
    }
  }
  
  /**
   * Returns <code>true</code> if a plug-in in the given file name already exists
   * in the first plug-ins folder.
   * @throws RecorderException if no plug-ins folder is associated to this manager.
   */
  public boolean pluginExists(String pluginLocation) throws RecorderException {
    if (this.pluginFolders == null
        || this.pluginFolders.length == 0) {
      throw new RecorderException("Can't access to plugins folder");
    } else {
      String pluginFileName = new File(pluginLocation).getName();
      return new File(this.pluginFolders [0], pluginFileName).exists();
    }
  }

  /**
   * Deletes the given plug-in <code>libraries</code> from managed plug-ins. 
   * @since 4.0
   */
  public void deletePlugins(List<Library> libraries) throws RecorderException {
    for (Library library : libraries) {
      for (Iterator<Map.Entry<String, PluginLibrary>> it = this.pluginLibraries.entrySet().iterator(); it.hasNext(); ) {
        String pluginLocation = it.next().getValue().getLocation();
        if (pluginLocation.equals(library.getLocation())) {
          if (new File(pluginLocation).exists()
              && !new File(pluginLocation).delete()) {
            throw new RecorderException("Couldn't delete file " + library.getLocation());
          }
          it.remove();
        }
      }
    }
  }
  
  /**
   * Adds the file at the given location to the first plug-ins folders if it exists.
   * Once added, the plug-in will be available at next application start. 
   * @throws RecorderException if no plug-ins folder is associated to this manager.
   */
  public void addPlugin(String pluginPath) throws RecorderException {
    try {
      if (this.pluginFolders == null
          || this.pluginFolders.length == 0) {
        throw new RecorderException("Can't access to plugins folder");
      }
      String pluginFileName = new File(pluginPath).getName();
      File destinationFile = new File(this.pluginFolders [0], pluginFileName);

      // Copy furnitureCatalogFile to furniture plugin folder
      InputStream tempIn = null;
      OutputStream tempOut = null;
      try {
        tempIn = new BufferedInputStream(new FileInputStream(pluginPath));
        this.pluginFolders [0].mkdirs();
        tempOut = new FileOutputStream(destinationFile);          
        byte [] buffer = new byte [8192];
        int size; 
        while ((size = tempIn.read(buffer)) != -1) {
          tempOut.write(buffer, 0, size);
        }
      } finally {
        if (tempIn != null) {
          tempIn.close();
        }
        if (tempOut != null) {
          tempOut.close();
        }
      }
    } catch (IOException ex) {
      throw new RecorderException(
          "Can't write " + pluginPath +  " in plugins folder", ex);
    }
  }

  /**
   * The properties required to instantiate a plug-in.
   */
  private static class PluginLibrary implements Library {
    private final String                  location;
    private final String                  name;
    private final String                  id;
    private final String                  description;
    private final String                  version;
    private final String                  license;
    private final String                  provider;
    private final Class<? extends Plugin> pluginClass;
    private final ClassLoader             pluginClassLoader;
    
    /**
     * Creates plug-in properties from parameters. 
     */
    public PluginLibrary(String location,
                         String id,
                         String name, String description, String version, 
                         String license, String provider,
                         Class<? extends Plugin> pluginClass, ClassLoader pluginClassLoader) {
      this.location = location;
      this.id = id;
      this.name = name;
      this.description = description;
      this.version = version;
      this.license = license;
      this.provider = provider;
      this.pluginClass = pluginClass;
      this.pluginClassLoader = pluginClassLoader;
    }

    public Class<? extends Plugin> getPluginClass() {
      return this.pluginClass;
    }

    public ClassLoader getPluginClassLoader() {
      return this.pluginClassLoader;
    }
    
    public String getType() {
      return PluginManager.PLUGIN_LIBRARY_TYPE;
    }
    
    public String getLocation() {
      return this.location;
    }
    
    public String getId() {
      return this.id;
    }

    public String getName() {
      return this.name;
    }
    
    public String getDescription() {
      return this.description;
    }

    public String getVersion() {
      return this.version;
    }

    public String getLicense() {
      return this.license;
    }

    public String getProvider() {
      return this.provider;
    }
  }
}
