/*
 * FileUserPreferences.java 18 sept 2006
 *
 * Copyright (c) 2006 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * User preferences intialized from 
 * {@link com.eteks.sweethome3d.io.DefaultUserPreferences default user preferences}
 * and stored in user preferences on local file system. 
 * @author Emmanuel Puybaret
 */
public class FileUserPreferences extends UserPreferences {
  /**
   * Creates user preferences read either from user preferences in file system, 
   * or from resource files.
   */
  public FileUserPreferences() {
    DefaultUserPreferences defaultPreferences = 
      new DefaultUserPreferences();
    // Use default catalog at this time as user has no way to modified it
    setCatalog(defaultPreferences.getCatalog());   
    // Read other preferences from current user preferences in system
    Preferences preferences = getPreferences();
    Unit unit = Unit.valueOf(preferences.get("unit", 
        defaultPreferences.getUnit().toString()));
    setUnit(unit);
    setMagnetismEnabled(preferences.getBoolean("magnetismEnabled", true));
    setNewWallThickness(preferences.getFloat("newWallThickness", 
            defaultPreferences.getNewWallThickness()));
    setNewHomeWallHeight(preferences.getFloat("newHomeWallHeight",
            defaultPreferences.getNewHomeWallHeight()));    
  }

  /**
   * Writes user preferences in current user preferences in system.
   */
  @Override
  public void write() throws RecorderException {
    Preferences preferences = getPreferences();
    preferences.put("unit", getUnit().toString());   
    preferences.putBoolean("magnetismEnabled", isMagnetismEnabled());
    preferences.putFloat("newWallThickness", getNewWallThickness());   
    preferences.putFloat("newHomeWallHeight", getNewHomeWallHeight());
    try {
      // Write preferences 
      preferences.sync();
    } catch (BackingStoreException ex) {
      throw new RecorderException("Couldn't write preferences", ex);
    }
  }

  /**
   * Returns Java preferences for current system user.
   */
  private Preferences getPreferences() {
    return Preferences.userNodeForPackage(FileUserPreferences.class);
  }
}
