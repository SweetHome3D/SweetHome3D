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

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Locale;
import java.util.concurrent.Callable;

import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.undo.UndoableEditSupport;

import junit.framework.TestCase;

import com.eteks.sweethome3d.io.DefaultUserPreferences;
import com.eteks.sweethome3d.io.FileUserPreferences;
import com.eteks.sweethome3d.model.BackgroundImage;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.LengthUnit;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.swing.FileContentManager;
import com.eteks.sweethome3d.swing.SwingViewFactory;
import com.eteks.sweethome3d.swing.UserPreferencesPanel;
import com.eteks.sweethome3d.tools.URLContent;
import com.eteks.sweethome3d.viewcontroller.BackgroundImageWizardController;
import com.eteks.sweethome3d.viewcontroller.CompassController;
import com.eteks.sweethome3d.viewcontroller.FurnitureCatalogController;
import com.eteks.sweethome3d.viewcontroller.FurnitureController;
import com.eteks.sweethome3d.viewcontroller.HelpController;
import com.eteks.sweethome3d.viewcontroller.Home3DAttributesController;
import com.eteks.sweethome3d.viewcontroller.HomeController;
import com.eteks.sweethome3d.viewcontroller.HomeController3D;
import com.eteks.sweethome3d.viewcontroller.HomeFurnitureController;
import com.eteks.sweethome3d.viewcontroller.ImportedFurnitureWizardController;
import com.eteks.sweethome3d.viewcontroller.ImportedTextureWizardController;
import com.eteks.sweethome3d.viewcontroller.LabelController;
import com.eteks.sweethome3d.viewcontroller.LevelController;
import com.eteks.sweethome3d.viewcontroller.PageSetupController;
import com.eteks.sweethome3d.viewcontroller.PhotoController;
import com.eteks.sweethome3d.viewcontroller.PlanController;
import com.eteks.sweethome3d.viewcontroller.PrintPreviewController;
import com.eteks.sweethome3d.viewcontroller.RoomController;
import com.eteks.sweethome3d.viewcontroller.TextureChoiceController;
import com.eteks.sweethome3d.viewcontroller.ThreadedTaskController;
import com.eteks.sweethome3d.viewcontroller.UserPreferencesController;
import com.eteks.sweethome3d.viewcontroller.VideoController;
import com.eteks.sweethome3d.viewcontroller.WallController;

/**
 * Tests {@link com.eteks.sweethome3d.swing.UserPreferencesPanel user preferences panel}.
 * @author Emmanuel Puybaret
 */
public class UserPreferencesPanelTest extends TestCase {
  /**
   * Tests user preferences panel.
   */
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
    preferences.setFurnitureViewedFromTop(
        defaultPreferences.isFurnitureViewedFromTop());
    preferences.setFloorColoredOrTextured(
        defaultPreferences.isFurnitureViewedFromTop());
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
    JRadioButton meterRadioButton = 
        (JRadioButton)TestUtilities.getField(panel, "meterRadioButton");
    JRadioButton millimeterRadioButton = 
        (JRadioButton)TestUtilities.getField(panel, "millimeterRadioButton");
    JCheckBox    magnetismCheckBox = 
        (JCheckBox)TestUtilities.getField(panel, "magnetismCheckBox");
    JCheckBox    rulersCheckBox = 
        (JCheckBox)TestUtilities.getField(panel, "rulersCheckBox");
    JCheckBox    gridCheckBox = 
        (JCheckBox)TestUtilities.getField(panel, "gridCheckBox");
    JRadioButton catalogIconRadioButton = 
      (JRadioButton)TestUtilities.getField(panel, "catalogIconRadioButton");
    JRadioButton topViewRadioButton = 
        (JRadioButton)TestUtilities.getField(panel, "topViewRadioButton");
    JRadioButton monochromeRadioButton = 
        (JRadioButton)TestUtilities.getField(panel, "monochromeRadioButton");
    JRadioButton floorColorOrTextureRadioButton = 
        (JRadioButton)TestUtilities.getField(panel, "floorColorOrTextureRadioButton");
    JSpinner newWallThicknessSpinner = 
        (JSpinner)TestUtilities.getField(panel, "newWallThicknessSpinner");
    JSpinner newHomeWallHeightSpinner = 
        (JSpinner)TestUtilities.getField(panel, "newWallHeightSpinner");
    // Check panel components value
    assertTrue("Centimeter radio button isn't selected", centimeterRadioButton.isSelected());
    assertFalse("Inch radio button is selected",  inchRadioButton.isSelected());
    assertFalse("Meter radio button is selected", meterRadioButton.isSelected());
    assertFalse("Millimeter radio button is selected", millimeterRadioButton.isSelected());
    assertTrue("Magnestism isn't selected", magnetismCheckBox.isSelected());
    assertTrue("Rulers isn't selected", rulersCheckBox.isSelected());
    assertTrue("Grid isn't selected", gridCheckBox.isSelected());
    assertTrue("Catalog icon radio button isn't selected", catalogIconRadioButton.isSelected());
    assertFalse("Top view button is selected",  topViewRadioButton.isSelected());
    assertTrue("Monochrome radio button isn't selected", monochromeRadioButton.isSelected());
    assertFalse("Floor color radio button is selected",  floorColorOrTextureRadioButton.isSelected());
    assertEquals("Wrong default thickness", 
        newWallThicknessSpinner.getValue(), defaultPreferences.getNewWallThickness());
    assertEquals("Wrong default wall height", 
        newHomeWallHeightSpinner.getValue(), defaultPreferences.getNewWallHeight());
    
    // 3. Change panel values
    inchRadioButton.setSelected(true);
    magnetismCheckBox.setSelected(false);
    rulersCheckBox.setSelected(false);
    gridCheckBox.setSelected(false);
    topViewRadioButton.setSelected(true);
    floorColorOrTextureRadioButton.setSelected(true);
    newWallThicknessSpinner.setValue(1);
    newHomeWallHeightSpinner.setValue(100);
    
    // 4. Retrieve panel values into preferences
    controller.modifyUserPreferences();
    // Check preferences value
    assertPreferencesEqual(LengthUnit.INCH, false, false, false,
        true, true,
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
        preferences.isFurnitureViewedFromTop(), 
        preferences.isRoomFloorColoredOrTextured(), 
        preferences.getNewWallThickness(),  
        preferences.getNewWallHeight(), readPreferences);
    
    // Restore previous preferences
    previousPreferences.write();
  }
  
  /**
   * Asserts values in parameter are the same as the ones 
   * stored in <code>preferences</code>.
   */
  private void assertPreferencesEqual(LengthUnit unit,
                                      boolean magnetism,
                                      boolean rulers,
                                      boolean grid,
                                      boolean topView,
                                      boolean floorColorOrTexture,
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
    assertEquals("Wrong furniture rendering", topView,
        preferences.isFurnitureViewedFromTop());
    assertEquals("Wrong room rendering", floorColorOrTexture,
        preferences.isRoomFloorColoredOrTextured());
    assertEquals("Wrong new wall thickness", newWallThickness, 
        preferences.getNewWallThickness());
    assertEquals("Wrong new home wall height", newHomeWallHeight,
        preferences.getNewWallHeight());
  }
  
  /**
   * Tests length unit conversions.
   */
  public void testUnitLength() throws ParseException {
    Locale.setDefault(Locale.FRANCE);
    // Test formats without unit
    assertEquals("Wrong conversion", "102", LengthUnit.CENTIMETER.getFormat().format(102));
    assertEquals("Wrong conversion", "1,02", LengthUnit.METER.getFormat().format(102));
    // \u00a0 is a no-break space
    assertEquals("Wrong conversion", "1\u00a0020", LengthUnit.MILLIMETER.getFormat().format(102));
    assertEquals("Wrong conversion", "0'11\"", 
        LengthUnit.INCH.getFormat().format(LengthUnit.inchToCentimeter(11)));
    assertEquals("Wrong conversion", "1'11\"", 
        LengthUnit.INCH.getFormatWithUnit().format(LengthUnit.inchToCentimeter(11 + 12)));
    assertEquals("Wrong conversion", "1'11\u215b\"", 
        LengthUnit.INCH.getFormatWithUnit().format(LengthUnit.inchToCentimeter(11 + 12 + 0.125f)));
    
    // Test formats with unit
    assertEquals("Wrong conversion", "102 cm", LengthUnit.CENTIMETER.getFormatWithUnit().format(102));
    assertEquals("Wrong conversion", "1,02 m", LengthUnit.METER.getFormatWithUnit().format(102));
    // \u00a0 is a no-break space
    assertEquals("Wrong conversion", "1\u00a0020 mm", LengthUnit.MILLIMETER.getFormatWithUnit().format(102));
    assertEquals("Wrong conversion", "0'11\"", 
        LengthUnit.INCH.getFormatWithUnit().format(LengthUnit.inchToCentimeter(11)));
    assertEquals("Wrong conversion", "1'11\"", 
        LengthUnit.INCH.getFormatWithUnit().format(LengthUnit.inchToCentimeter(11 + 12)));
    assertEquals("Wrong conversion", "1'11\u215b\"", 
        LengthUnit.INCH.getFormatWithUnit().format(LengthUnit.inchToCentimeter(11 + 12 + 0.125f)));
    
    // Test parsing
    assertEquals("Wrong parsing", 102f, LengthUnit.CENTIMETER.getFormat().parseObject("102"));
    assertEquals("Wrong parsing", 102f, LengthUnit.METER.getFormat().parseObject("1,02"));
    assertEquals("Wrong parsing", 102f, LengthUnit.MILLIMETER.getFormat().parseObject("1020"));
    TestUtilities.assertEqualsWithinEpsilon("Wrong conversion",  LengthUnit.inchToCentimeter(11),
        ((Number)LengthUnit.INCH.getFormat().parseObject("0'11\"")).floatValue(), 1E-10f);
    TestUtilities.assertEqualsWithinEpsilon("Wrong conversion",  LengthUnit.inchToCentimeter(10 + 12),
        ((Number)LengthUnit.INCH.getFormat().parseObject("1 ' 10 \"")).floatValue(), 1E-10f);
    TestUtilities.assertEqualsWithinEpsilon("Wrong conversion",  LengthUnit.inchToCentimeter(24),
        ((Number)LengthUnit.INCH.getFormat().parseObject("2'")).floatValue(), 1E-10f);
    TestUtilities.assertEqualsWithinEpsilon("Wrong conversion",  LengthUnit.inchToCentimeter(11),
        ((Number)LengthUnit.INCH.getFormat().parseObject("11\"")).floatValue(), 1E-10f);
    TestUtilities.assertEqualsWithinEpsilon("Wrong conversion",  LengthUnit.inchToCentimeter(11),
        ((Number)LengthUnit.INCH.getFormat().parseObject("11")).floatValue(), 1E-10f);
    TestUtilities.assertEqualsWithinEpsilon("Wrong conversion",  LengthUnit.inchToCentimeter(11.125f),
        ((Number)LengthUnit.INCH.getFormat().parseObject("11,125")).floatValue(), 1E-10f);
    TestUtilities.assertEqualsWithinEpsilon("Wrong conversion",  LengthUnit.inchToCentimeter(12 + 11 + 3 * 0.125f),
        ((Number)LengthUnit.INCH.getFormat().parseObject("1'11\u215c\"")).floatValue(), 1E-10f);
    TestUtilities.assertEqualsWithinEpsilon("Wrong conversion",  LengthUnit.inchToCentimeter(12 + 11 + 3 * 0.125f),
        ((Number)LengthUnit.INCH.getFormat().parseObject("1' 11 \u215c")).floatValue(), 1E-10f);
    try {
      LengthUnit.INCH.getFormat().parseObject("'");
      fail("' not a number");
    } catch (Exception ex) {
      // Expected a failure
    }
    try {
      LengthUnit.INCH.getFormat().parseObject("\"");
      fail("\" not a number");
    } catch (Exception ex) {
      // Expected a failure
    }
    try {
      LengthUnit.INCH.getFormat().parseObject("10A'");
      fail("10A' not a number");
    } catch (Exception ex) {
      // Expected a failure
    }
    try {
      LengthUnit.INCH.getFormat().parseObject("10,2'");
      fail("10,2' not a number"); // Accept fraction part only for inches
    } catch (Exception ex) {
      // Expected a failure
    }
    ParsePosition parsePosition = new ParsePosition(0);
    LengthUnit.INCH.getFormat().parseObject("10'2A", parsePosition);
    assertEquals("Wrong parse position", "10'2A".indexOf('A'), parsePosition.getIndex());
  }
  
  /**
   * Tests language changes on the GUI. 
   */
  public void testLanguageChange() {
    Locale defaultLocale = Locale.getDefault();
    Locale.setDefault(Locale.US);
    UserPreferences preferences = new DefaultUserPreferences() {
      @Override
      public void write() throws RecorderException {
        // Ignore write requests
      }
    };

    Home home = new Home();
    Content imageContent = new URLContent(UserPreferencesPanelTest.class.getResource("resources/test.png"));
    home.setBackgroundImage(new BackgroundImage(imageContent, 1, 0, 1, 0, 1, 0, 0));

    SwingViewFactory viewFactory = new SwingViewFactory();
    FileContentManager contentManager = new FileContentManager(preferences);
    UndoableEditSupport undoableEditSupport = new UndoableEditSupport();
    
    for (String language : preferences.getSupportedLanguages()) {
      preferences.setLanguage(language);
      // Instantiate all views available in Sweet Home 3D 
      HomeController homeController = new HomeController(home, preferences, viewFactory, contentManager);
      homeController.getView();
      preferences.setFurnitureCatalogViewedInTree(false);
      new FurnitureCatalogController(preferences.getFurnitureCatalog(), preferences, viewFactory, contentManager).getView();
      preferences.setFurnitureCatalogViewedInTree(true);
      new FurnitureCatalogController(preferences.getFurnitureCatalog(), preferences, viewFactory, contentManager).getView();
      new FurnitureController(home, preferences, viewFactory).getView();
      new PlanController(home, preferences, viewFactory, contentManager, undoableEditSupport).getView();
      new HomeController3D(home, preferences, viewFactory, contentManager, undoableEditSupport).getView();
      
      new PageSetupController(home, preferences, viewFactory, undoableEditSupport).getView();
      new PrintPreviewController(home, preferences, homeController, viewFactory).getView();
      new UserPreferencesController(preferences, viewFactory, contentManager).getView();
      new HomeFurnitureController(home, preferences, viewFactory, contentManager, undoableEditSupport).getView();
      new LevelController(home, preferences, viewFactory, undoableEditSupport).getView();
      new WallController(home, preferences, viewFactory, contentManager, undoableEditSupport).getView();
      new RoomController(home, preferences, viewFactory, contentManager, undoableEditSupport).getView();
      new LabelController(home, preferences, viewFactory, undoableEditSupport).getView();
      new CompassController(home, preferences, viewFactory, undoableEditSupport).getView();
      new Home3DAttributesController(home, preferences, viewFactory, contentManager, undoableEditSupport).getView();
      new PhotoController(home, preferences, homeController.getHomeController3D().getView(), viewFactory, contentManager).getView();
      new VideoController(home, preferences, viewFactory, contentManager).getView();
      
      new TextureChoiceController("", preferences, viewFactory, contentManager).getView();
      new ThreadedTaskController(new Callable<Void>() { 
          public Void call() throws Exception {
            return null;
          }
        }, "", null, preferences, viewFactory).getView();
      
      new BackgroundImageWizardController(home, preferences, viewFactory, contentManager, undoableEditSupport).getView();
      new ImportedFurnitureWizardController(preferences, viewFactory, contentManager).getView();
      new ImportedTextureWizardController(preferences, viewFactory, contentManager).getView();
      
      new HelpController(preferences, viewFactory).getView();
      Locale.setDefault(defaultLocale);
    }
  }
}
