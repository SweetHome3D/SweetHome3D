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

import java.util.Locale;

import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;

import junit.framework.TestCase;

import com.eteks.sweethome3d.io.DefaultUserPreferences;
import com.eteks.sweethome3d.io.FileUserPreferences;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.LengthUnit;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.swing.SwingViewFactory;
import com.eteks.sweethome3d.swing.UserPreferencesPanel;
import com.eteks.sweethome3d.viewcontroller.UserPreferencesController;

/**
 * Tests {@link com.eteks.sweethome3d.swing.UserPreferencesPanel user preferences panel}.
 * @author Emmanuel Puybaret
 */
public class UserPreferencesPanelTest extends TestCase {
  public void testUserPreferencesPanel() 
       throws RecorderException, NoSuchFieldException, IllegalAccessException {
    // 0. Keep a copy of current preferences
    UserPreferences previousPreferences = new FileUserPreferences();
    
    // 1. Create default preferences for a user that uses centimeter
    Locale.setDefault(Locale.FRANCE);
    UserPreferences defaultPreferences = new DefaultUserPreferences();
    // Copy these preferences into system preferences
    UserPreferences preferences = new FileUserPreferences();
    preferences.setUnit(defaultPreferences.getLengthUnit());
    preferences.setRulersVisible(
        defaultPreferences.isRulersVisible());
    preferences.setGridVisible(
        defaultPreferences.isGridVisible());
    preferences.setMagnetismEnabled(
        defaultPreferences.isMagnetismEnabled());
    preferences.setNewWallThickness(
        defaultPreferences.getNewWallThickness());
    preferences.setNewWallHeight(
        defaultPreferences.getNewWallHeight());
    
    // 2. Create a user preferences panel
    UserPreferencesController controller = 
        new UserPreferencesController(preferences, new SwingViewFactory(), null);
    UserPreferencesPanel panel = (UserPreferencesPanel)controller.getView();
    JRadioButton centimeterRadioButton = 
        (JRadioButton)TestUtilities.getField(panel, "centimeterRadioButton");
    JRadioButton inchRadioButton = 
        (JRadioButton)TestUtilities.getField(panel, "inchRadioButton");
    JCheckBox    magnetismCheckBox = 
        (JCheckBox)TestUtilities.getField(panel, "magnetismCheckBox");
    JCheckBox    rulersCheckBox = 
      (JCheckBox)TestUtilities.getField(panel, "rulersCheckBox");
    JCheckBox    gridCheckBox = 
      (JCheckBox)TestUtilities.getField(panel, "gridCheckBox");
    JSpinner newWallThicknessSpinner = 
        (JSpinner)TestUtilities.getField(panel, "newWallThicknessSpinner");
    JSpinner newHomeWallHeightSpinner = 
        (JSpinner)TestUtilities.getField(panel, "newWallHeightSpinner");
    // Check panel components value
    assertTrue("Centimeter radio button isn't selected", 
        centimeterRadioButton.isSelected());
    assertFalse("Inch radio button isn't selected", 
        inchRadioButton.isSelected());
    assertTrue("Magnestism isn't selected", magnetismCheckBox.isSelected());
    assertTrue("Rulers isn't selected", rulersCheckBox.isSelected());
    assertTrue("Grid isn't selected", gridCheckBox.isSelected());
    assertEquals("Wrong default thickness", 
        newWallThicknessSpinner.getValue(), defaultPreferences.getNewWallThickness());
    assertEquals("Wrong default wall height", 
        newHomeWallHeightSpinner.getValue(), defaultPreferences.getNewWallHeight());
    
    // 3. Change panel values
    inchRadioButton.setSelected(true);
    magnetismCheckBox.setSelected(false);
    rulersCheckBox.setSelected(false);
    gridCheckBox.setSelected(false);
    newWallThicknessSpinner.setValue(1);
    newHomeWallHeightSpinner.setValue(100);
    
    // 4. Retrieve panel values into preferences 
    preferences.setUnit(controller.getUnit());
    preferences.setMagnetismEnabled(controller.isMagnetismEnabled());
    preferences.setRulersVisible(controller.isRulersVisible());
    preferences.setGridVisible(controller.isGridVisible());
    preferences.setNewWallThickness(controller.getNewWallThickness());
    preferences.setNewWallHeight(controller.getNewWallHeight());
    // Check preferences value
    assertPreferencesEqual(LengthUnit.INCH, false, false, false,
        LengthUnit.inchToCentimeter(1), 
        LengthUnit.inchToCentimeter(100), 
        preferences);
    
    // 5. Save preferences and read them in an other system preferences object
    preferences.write();
    UserPreferences readPreferences = new FileUserPreferences();
    // Check if readPreferences and preferences have the same values
    assertPreferencesEqual(preferences.getLengthUnit(),
        preferences.isMagnetismEnabled(), 
        preferences.isRulersVisible(), 
        preferences.isGridVisible(), 
        preferences.getNewWallThickness(),  
        preferences.getNewWallHeight(), readPreferences);
    
    // Restore previous preferences
    previousPreferences.write();
  }
  
  /**
   * Assert values in parameter are the same as the ones 
   * stored in <code>preferences</code>.
   */
  private void assertPreferencesEqual(LengthUnit unit,
                                      boolean magnetism,
                                      boolean rulers,
                                      boolean grid,
                                      float newWallThickness,
                                      float newHomeWallHeight,
                                      UserPreferences preferences) {
    
    assertEquals("Wrong unit", unit, preferences.getLengthUnit());
    assertEquals("Wrong magnestism", magnetism,
        preferences.isMagnetismEnabled());
    assertEquals("Wrong rulers visibility", rulers,
        preferences.isRulersVisible());
    assertEquals("Wrong grid visibility", grid,
        preferences.isGridVisible());
    assertEquals("Wrong new wall thickness", newWallThickness, 
        preferences.getNewWallThickness());
    assertEquals("Wrong new home wall height", newHomeWallHeight,
        preferences.getNewWallHeight());
  }
}
