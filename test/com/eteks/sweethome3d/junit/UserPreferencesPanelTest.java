/*
 * UserPreferencesPanelTest.java 23 sept. 2006
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
package com.eteks.sweethome3d.junit;

import java.lang.reflect.Field;
import java.util.Locale;

import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;

import junit.framework.TestCase;

import com.eteks.sweethome3d.io.DefaultUserPreferences;
import com.eteks.sweethome3d.io.FileUserPreferences;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.swing.UserPreferencesPanel;

/**
 * Tests {@link com.eteks.sweethome3d.swing.UserPreferencesPanel user preferences panel}.
 * @author Emmanuel Puybaret
 */
public class UserPreferencesPanelTest extends TestCase {
  public void testUserPreferencesPanel() 
       throws RecorderException, NoSuchFieldException, IllegalAccessException {
    // 1. Create default preferences for a user that uses centimeter
    Locale.setDefault(Locale.FRANCE);
    UserPreferences defaultPreferences = new DefaultUserPreferences();
    // Copy this preferences into system preferences
    UserPreferences preferences = new FileUserPreferences();
    preferences.setUnit(defaultPreferences.getUnit());
    preferences.setMagnetismEnabled(
        defaultPreferences.isMagnetismEnabled());
    preferences.setNewWallThickness(
        defaultPreferences.getNewWallThickness());
    preferences.setNewHomeWallHeight(
        defaultPreferences.getNewHomeWallHeight());
    
    // 2. Create a user preferences panel 
    UserPreferencesPanel panel = new UserPreferencesPanel();
    panel.setPreferences(preferences);
    JRadioButton centimeterRadioButton = 
        (JRadioButton)getField(panel, "centimeterRadioButton");
    JRadioButton inchRadioButton = 
        (JRadioButton)getField(panel, "inchRadioButton");
    JCheckBox    magnetismCheckBox = 
        (JCheckBox)getField(panel, "magnetismCheckBox");
    JSpinner newWallThicknessSpinner = 
        (JSpinner)getField(panel, "newWallThicknessSpinner");
    JSpinner newHomeWallHeightSpinner = 
        (JSpinner)getField(panel, "newHomeWallHeightSpinner");
    // Check panel components value
    assertTrue("Centimeter radio button isn't selected", 
        centimeterRadioButton.isSelected());
    assertFalse("Inch radio button isn't selected", 
        inchRadioButton.isSelected());
    assertTrue("Magnestism isn't selected", magnetismCheckBox.isSelected());
    assertEquals("Wrong default thickness", 
        newWallThicknessSpinner.getValue(), defaultPreferences.getNewWallThickness());
    assertEquals("Wrong default wall height", 
        newHomeWallHeightSpinner.getValue(), defaultPreferences.getNewHomeWallHeight());
    
    // 3. Change panel values
    inchRadioButton.setSelected(true);
    magnetismCheckBox.setSelected(false);
    newWallThicknessSpinner.setValue(1);
    newHomeWallHeightSpinner.setValue(100);
    
    // 4. Retrieve panel values into preferences 
    preferences.setUnit(panel.getUnit());
    preferences.setMagnetismEnabled(panel.isMagnetismEnabled());
    preferences.setNewWallThickness(panel.getNewWallThickness());
    preferences.setNewHomeWallHeight(panel.getNewHomeWallHeight());
    // Check preferences value
    assertPreferencesEqual(UserPreferences.Unit.INCH, false, 
        UserPreferences.Unit.inchToCentimer(1), 
        UserPreferences.Unit.inchToCentimer(100), 
        preferences);
    
    // 5. Save preferences and read them in an other system preferences object
    preferences.write();
    UserPreferences readPreferences = new FileUserPreferences();
    // Check if readPreferences and preferences have the same values
    assertPreferencesEqual(preferences.getUnit(),
        preferences.isMagnetismEnabled(), 
        preferences.getNewWallThickness(),  
        preferences.getNewHomeWallHeight(), readPreferences);
  }
  
  /**
   * Assert values in parameter are the same as the ones 
   * stored in <code>preferences</code>.
   */
  private void assertPreferencesEqual(UserPreferences.Unit unit,
                                      boolean magnetism,
                                      float newWallThickness,
                                      float newHomeWallHeigt,
                                      UserPreferences preferences) {
    
    assertEquals("Wrong unit", unit, preferences.getUnit());
    assertEquals("Wrong magnestism", magnetism,
        preferences.isMagnetismEnabled());
    assertEquals("Wrong new wall thickness", newWallThickness, 
        preferences.getNewWallThickness());
    assertEquals("Wrong new home wall height", newHomeWallHeigt,
        preferences.getNewHomeWallHeight());
  }

  /**
   * Returns a reference to <code>fieldName</code> 
   * in a given <code>instance</code> by reflection.
   */
  private Object getField(Object instance, String fieldName)
      throws NoSuchFieldException, IllegalAccessException {
    Field field = instance.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    return field.get(instance);
  }
}
