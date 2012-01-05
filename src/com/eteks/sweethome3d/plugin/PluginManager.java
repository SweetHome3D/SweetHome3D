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
import java.util.HashMap;
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
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.viewcontroller.HomeController;

/**
 * Sweet Home 3D plug-ins manager.
 * @author Emmanuel Puybaret
 */
public class PluginManager {
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
  private final Map<String, PluginDefinition> pluginDefinitions = 
      new TreeMap<String, PluginDefinition>();
  private final Map<Home, List<Plugin>> homePlugins = new HashMap<Home, List<Plugin>>();
  
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
          // Treat plug in files in reverse order so file named with a date will be taken into account 
          // from most recent to least recent
          Arrays.sort(pluginFiles, Collections.reverseOrder());
          for (File pluginFile : pluginFiles) {
            try {
              loadPlugins(pluginFile.toURI().toURL());
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
      loadPlugins(pluginUrl);
    }
  }

  /**
   * Loads the plug-ins that may be available in the given URL.
   */
  private void loadPlugins(URL pluginUrl) {
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
                          ClassLoader    pluginClassLoader) {
    try {
      String name = resource.getString(NAME);

      // Check Java and application versions
      String javaMinimumVersion = resource.getString(JAVA_MINIMUM_VERSION);
      if (!isJavaVersionSuperiorTo(javaMinimumVersion)) {
        System.err.println("Invalid plug-in " + pluginLocation + ":\n" 
            + "Not compatible Java version " + System.getProperty("java.version"));
        return;
      }
      
      String applicationMinimumVersion = resource.getString(APPLICATION_MINIMUM_VERSION);
      if (!isApplicationVersionSuperiorTo(applicationMinimumVersion)) {
        System.err.println("Invalid plug-in " + pluginLocation + ":\n" 
            + "Not compatible application version");
        return;
      }
      
      String pluginClassName = resource.getString(CLASS);
      Class<? extends Plugin> pluginClass = getPluginClass(pluginClassLoader, pluginClassName);
      
      String description = resource.getString(DESCRIPTION);
      String version = resource.getString(VERSION);
      String license = resource.getString(LICENSE);
      String provider = resource.getString(PROVIDER);
      
      // Store plug-in properties if they don't exist yet
      if (this.pluginDefinitions.get(name) == null) {
        this.pluginDefinitions.put(name, new PluginDefinition(
            name, pluginClass, pluginClassLoader, description, version, license, provider));
      }      
    } catch (MissingResourceException ex) {
      System.err.println("Invalid plug-in " + pluginLocation + ":\n" + ex.getMessage());
    } catch (IllegalArgumentException ex) {
      System.err.println("Invalid plug-in " + pluginLocation + ":\n" + ex.getMessage());
    } 
  }
  
  /**
   * Returns <code>true</code> if the given version is smaller than the version 
   * of the current JVM. Versions are compared only on their first two parts.
   */
  private boolean isJavaVersionSuperiorTo(String javaMinimumVersion) {
    String javaVersion = System.getProperty("java.version");
    String [] javaVersionParts = javaVersion.split("\\.|_");
    String [] javaMinimumVersionParts = javaMinimumVersion.split("\\.|_");
    if (javaVersionParts.length >= 1
        && javaMinimumVersionParts.length >= 1) {
      try {
        // Compare digits in first part
        int javaVersionFirstPart = Integer.parseInt(javaVersionParts [0]);
        int javaMinimumVersionFirstPart = Integer.parseInt(javaMinimumVersionParts [0]);        
        if (javaVersionFirstPart > javaMinimumVersionFirstPart) {
          return true;
        } else if (javaVersionFirstPart == javaMinimumVersionFirstPart 
                   && javaVersionParts.length >= 2
                   && javaMinimumVersionParts.length >= 2) { 
          // Compare digits in second part (this may work even if second part is > 10)
          return Integer.parseInt(javaVersionParts [1]) >= Integer.parseInt(javaMinimumVersionParts [1]);
        }
      } catch (NumberFormatException ex) {
      }
    }
    return false;
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
        for (PluginDefinition pluginDefinition : this.pluginDefinitions.values()) {
          try {
            Plugin plugin = pluginDefinition.getPluginClass().newInstance();                      
            plugin.setPluginClassLoader(pluginDefinition.getPluginClassLoader());
            plugin.setName(pluginDefinition.getName());
            plugin.setDescription(pluginDefinition.getDescription());
            plugin.setVersion(pluginDefinition.getVersion());
            plugin.setLicense(pluginDefinition.getLicense());
            plugin.setProvider(pluginDefinition.getProvider());
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
   * Returns <code>true</code> if a plug-in with the given file name already exists
   * in the first plug-ins folder.
   * @throws RecorderException if no plug-ins folder is associated to this manager.
   */
  public boolean pluginExists(String pluginName) throws RecorderException {
    if (this.pluginFolders == null
        || this.pluginFolders.length == 0) {
      throw new RecorderException("Can't access to plugins folder");
    } else {
      String pluginFileName = new File(pluginName).getName();
      return new File(this.pluginFolders [0], pluginFileName).exists();
    }
  }

  /**
   * Adds the file <code>pluginName</code> to the first plug-ins folders if it exists.
   * Once added, the plug-in will be available at next application start. 
   * @throws RecorderException if no plug-ins folder is associated to this manager.
   */
  public void addPlugin(String pluginName) throws RecorderException {
    try {
      if (this.pluginFolders == null
          || this.pluginFolders.length == 0) {
        throw new RecorderException("Can't access to plugins folder");
      }
      String pluginFileName = new File(pluginName).getName();
      File destinationFile = new File(this.pluginFolders [0], pluginFileName);

      // Copy furnitureCatalogFile to furniture plugin folder
      InputStream tempIn = null;
      OutputStream tempOut = null;
      try {
        tempIn = new BufferedInputStream(new FileInputStream(pluginName));
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
          "Can't write " + pluginName +  " in furniture libraries plugin folder", ex);
    }
  }

  /**
   * The properties required to instantiate a plug-in.
   */
  private static class PluginDefinition {
    private final String                  name;
    private final Class<? extends Plugin> pluginClass;
    private final ClassLoader             pluginClassLoader;
    private final String                  description;
    private final String                  version;
    private final String                  license;
    private final String                  provider;
    
    /**
     * Creates plug-in properties from parameters. 
     */
    public PluginDefinition(String name,
                            Class<? extends Plugin> pluginClass,
                            ClassLoader pluginClassLoader, 
                            String description, String version,
                            String license, String provider) {
      this.name = name;
      this.pluginClass = pluginClass;
      this.pluginClassLoader = pluginClassLoader;
      this.description = description;
      this.version = version;
      this.license = license;
      this.provider = provider;
    }

    public String getName() {
      return this.name;
    }

    public Class<? extends Plugin> getPluginClass() {
      return this.pluginClass;
    }

    public ClassLoader getPluginClassLoader() {
      return this.pluginClassLoader;
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
