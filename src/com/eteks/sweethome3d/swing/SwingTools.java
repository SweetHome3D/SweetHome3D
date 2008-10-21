/*
 * SwingTools.java 21 oct. 2008
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
package com.eteks.sweethome3d.swing;

import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.UIManager;

/**
 * Gathers some useful tools for Swing.
 * @author Emmanuel Puybaret
 */
public class SwingTools {
  private SwingTools() {
    // This class contains only tools
  }

  /**
   * Updates the Swing resource bundle in use from the current Locale. 
   */
  public static void updateSwingResourceLanguage() {
    // Read Swing localized properties because Swing doesn't update its internal strings automatically
    // when default Locale is updated (see bug http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4884480)
    String [] swingResources = {"com.sun.swing.internal.plaf.basic.resources.basic",
                                "com.sun.swing.internal.plaf.metal.resources.metal"};
    for (String swingResource : swingResources) {
      ResourceBundle resource;
      try {
        resource = ResourceBundle.getBundle(swingResource);
      } catch (MissingResourceException ex) {
        resource = ResourceBundle.getBundle(swingResource, Locale.ENGLISH);
      }
      // Update UIManager properties
      for (Enumeration iter = resource.getKeys(); iter.hasMoreElements(); ) {
        String property = (String)iter.nextElement();
        UIManager.put(property, resource.getString(property));
      }      
    };
  }
}
