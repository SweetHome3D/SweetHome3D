/*
 * IOTools.java 14 juin 2010
 *
 * Copyright (c) 2010 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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
package com.eteks.sweethome3d.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Tools for files and IO layer.
 * @author Emmanuel Puybaret
 */
public class IOTools {
  private IOTools() {
    // This class contains only tools
  }
  
  /**
   * Returns a resource bundle able to read localized data from the properties files of the 
   * given <code>propertiesFamily</code> stored in the <code>resourceUrl</code> content. 
   * The returned resource bundle doesn't use any cache.
   * @throws MissingResourceException if no properties files of the given family were found.
   */
  public static ResourceBundle getUpdatedResourceBundle(URL resourceUrl,
                                                        String propertiesFamily) {
    URLClassLoader classLoader = new URLClassLoader(new URL [] {resourceUrl});
    Locale locale = Locale.getDefault();
    String country = locale.getCountry();
    String language = locale.getLanguage();
    String propertiesPath = propertiesFamily.replace('.', '/');
    URL [] propertiesUrls = { 
        classLoader.getResource(propertiesPath + ".properties"),
        classLoader.getResource(propertiesPath + "_" + language + ".properties"),
        classLoader.getResource(propertiesPath + "_" + language + "_" + country + ".properties")};
    ResourceBundle resourceBundle = null;
    for (URL url : propertiesUrls) {
      if (url != null) {
        final ResourceBundle parentResourceBundle = resourceBundle;
        try {
          InputStream in = url.openStream();
          resourceBundle = new PropertyResourceBundle(in) {
              {
                setParent(parentResourceBundle);
              }
            };
          in.close();
        } catch (IOException ex) {
          // Ignore malformed plugins
        }
      }
    }
    if (resourceBundle != null) {
      return resourceBundle;
    } else {
      throw new MissingResourceException("No properties files found for " + propertiesFamily, propertiesFamily, "");
    }
  }
  

}
