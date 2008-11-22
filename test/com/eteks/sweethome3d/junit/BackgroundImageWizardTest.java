/*
 * BackgroundImageWizardTest.java 22 sept. 2008
 * 
 * Copyright (c) 2008 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.eteks.sweethome3d.junit;

import java.awt.KeyboardFocusManager;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JSpinner;

import junit.extensions.abbot.ComponentTestFixture;
import abbot.finder.AWTHierarchy;
import abbot.finder.ComponentSearchException;
import abbot.tester.JComponentTester;

import com.eteks.sweethome3d.io.DefaultUserPreferences;
import com.eteks.sweethome3d.model.BackgroundImage;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.LengthUnit;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.swing.BackgroundImageWizardStepsPanel;
import com.eteks.sweethome3d.swing.HomePane;
import com.eteks.sweethome3d.swing.SwingViewFactory;
import com.eteks.sweethome3d.swing.WizardPane;
import com.eteks.sweethome3d.tools.URLContent;
import com.eteks.sweethome3d.viewcontroller.BackgroundImageWizardController;
import com.eteks.sweethome3d.viewcontroller.ContentManager;
import com.eteks.sweethome3d.viewcontroller.HomeController;
import com.eteks.sweethome3d.viewcontroller.View;
import com.eteks.sweethome3d.viewcontroller.ViewFactory;

/**
 * Tests background image wizard.
 * @author Emmanuel Puybaret
 */
public class BackgroundImageWizardTest extends ComponentTestFixture {
  public void testBackgroundImageWizard() throws ComponentSearchException, InterruptedException, 
      NoSuchFieldException, IllegalAccessException, InvocationTargetException {
    final UserPreferences preferences = new DefaultUserPreferences();
    // Ensure we use centimeter unit
    preferences.setUnit(LengthUnit.CENTIMETER);
    final URL testedImageName = BackgroundImageWizardTest.class.getResource("resources/test.png");
    // Create a dummy content manager
    final ContentManager contentManager = new ContentManager() {
      public Content getContent(String contentName) throws RecorderException {
        try {
          // Let's consider contentName is a URL
          return new URLContent(new URL(contentName));
        } catch (IOException ex) {
          fail();
          return null;
        }
      }

      public String getPresentationName(String contentName, ContentType contentType) {
        return "test";
      }

      public boolean isAcceptable(String contentName, ContentType contentType) {
        return true;
      }

      public String showOpenDialog(View parentView, String dialogTitle, ContentType contentType) {
        // Return tested model name URL
        return testedImageName.toString();
      }

      public String showSaveDialog(View parentView, String dialogTitle, ContentType contentType, String name) {
        return null;
      }      
    };
    Home home = new Home();
    ViewFactory viewFactory = new SwingViewFactory();    
    final HomeController controller = new HomeController(home, preferences, viewFactory, contentManager);
    final JComponent homeView = (JComponent)controller.getView();

    // 1. Create a frame that displays a home view 
    JFrame frame = new JFrame("Background Image Wizard Test");    
    frame.add(homeView);
    frame.pack();

    // Show home plan frame
    showWindow(frame);
    JComponentTester tester = new JComponentTester();
    tester.waitForIdle();    
    // Check home background image is empty
    assertEquals("Home background image isn't empty", null, home.getBackgroundImage());

    // 2. Open wizard to import a background image
    tester.invokeLater(new Runnable() { 
        public void run() {
          // Display dialog box later in Event Dispatch Thread to avoid blocking test thread
          homeView.getActionMap().get(HomePane.ActionType.IMPORT_BACKGROUND_IMAGE).actionPerformed(null);
        }
      });
    // Wait for import furniture view to be shown
    tester.waitForFrameShowing(new AWTHierarchy(), ResourceBundle.getBundle(
        BackgroundImageWizardController.class.getName()).getString("wizard.title"));
    // Check dialog box is displayed
    JDialog wizardDialog = (JDialog)TestUtilities.findComponent(frame, JDialog.class);
    assertTrue("Wizard view dialog not showing", wizardDialog.isShowing());

    // Retrieve ImportedFurnitureWizardStepsPanel components
    BackgroundImageWizardStepsPanel panel = (BackgroundImageWizardStepsPanel)TestUtilities.findComponent(
        wizardDialog, BackgroundImageWizardStepsPanel.class);
    JButton imageChoiceOrChangeButton = (JButton)TestUtilities.getField(panel, "imageChoiceOrChangeButton");
    JSpinner scaleDistanceSpinner = (JSpinner)TestUtilities.getField(panel, "scaleDistanceSpinner");
    JSpinner xOriginSpinner = (JSpinner)TestUtilities.getField(panel, "xOriginSpinner");
    JSpinner yOriginSpinner = (JSpinner)TestUtilities.getField(panel, "yOriginSpinner");
    
    // Check current step is image
    tester.waitForIdle();
    assertStepShowing(panel, true, false, false);    
    
    // 3. Choose tested image
    String imageChoiceOrChangeButtonText = imageChoiceOrChangeButton.getText();
    imageChoiceOrChangeButton.doClick();
    // Wait 100 s to let time to Java to load the image
    Thread.sleep(100);
    // Check choice button text changed
    assertFalse("Choice button text didn't change", 
        imageChoiceOrChangeButtonText.equals(imageChoiceOrChangeButton.getText()));
    // Click on next button
    WizardPane view = (WizardPane)TestUtilities.findComponent(wizardDialog, WizardPane.class);
    // Retrieve wizard view next button
    final JButton nextFinishOptionButton = (JButton)TestUtilities.getField(view, "nextFinishOptionButton"); 
    assertTrue("Next button isn't enabled", nextFinishOptionButton.isEnabled());
    nextFinishOptionButton.doClick();
    // Check current step is scale
    tester.waitForIdle();
    assertStepShowing(panel, false, true, false);
    
    // 4. Check scale distance spinner value is empty
    assertEquals("Scale distance spinner isn't empty", null, scaleDistanceSpinner.getValue());
    assertFalse("Next button is enabled", nextFinishOptionButton.isEnabled());
    // Check scale spinner field has focus
    assertSame("Scale spinner doesn't have focus", ((JSpinner.DefaultEditor)scaleDistanceSpinner.getEditor()).getTextField(),
        KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner());    
    // Enter as scale
    tester.actionKeyString("100");    
    // Check next button is enabled 
    assertTrue("Next button isn't enabled", nextFinishOptionButton.isEnabled());
    nextFinishOptionButton.doClick();
    // Check current step is origin
    tester.waitForIdle();
    assertStepShowing(panel, false, false, true);
    
    // 5. Check origin x and y spinners value is 0
    assertEquals("Wrong origin x spinner value", new Float(0), xOriginSpinner.getValue());
    assertEquals("Wrong origin y spinner value", new Float(0), yOriginSpinner.getValue());
    assertTrue("Next button isn't enabled", nextFinishOptionButton.isEnabled());
    assertSame("Origin x spinner doesn't have focus", ((JSpinner.DefaultEditor)xOriginSpinner.getEditor()).getTextField(),
        KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner());    
    // Change origin
    tester.actionKeyString("10");    
    assertEquals("Wrong origin x spinner value", 10f, xOriginSpinner.getValue());

    nextFinishOptionButton.doClick();
    tester.waitForIdle();    
    // Check home has a background image
    BackgroundImage backgroundImage = home.getBackgroundImage();
    assertTrue("No background image in home", backgroundImage != null);
    assertEquals("Background image wrong scale", 100f, backgroundImage.getScaleDistance());
    assertEquals("Background image wrong x origin", 10f, backgroundImage.getXOrigin());
    assertEquals("Background image wrong y origin", 0f, backgroundImage.getYOrigin());
        
    // 6. Undo background image choice in home
    homeView.getActionMap().get(HomePane.ActionType.UNDO).actionPerformed(null);
    // Check home background image is empty
    assertEquals("Home background image isn't empty", null, home.getBackgroundImage());
    // Redo
    homeView.getActionMap().get(HomePane.ActionType.REDO).actionPerformed(null);
    // Check home background image is back
    assertSame("No background image in home", backgroundImage, home.getBackgroundImage());
    
    // 7. Delete background image
    homeView.getActionMap().get(HomePane.ActionType.DELETE_BACKGROUND_IMAGE).actionPerformed(null);
    // Check home background image is empty
    assertEquals("Home background image isn't empty", null, home.getBackgroundImage());
  }

  /**
   * Asserts if each <code>panel</code> step preview component is showing or not. 
   */
  private void assertStepShowing(BackgroundImageWizardStepsPanel panel,
                                 boolean imageStepShwing,
                                 boolean scaleStepShowing,
                                 boolean originStepShowing) throws NoSuchFieldException, IllegalAccessException {
    assertEquals("Wrong image step visibility", imageStepShwing,
        ((JComponent)TestUtilities.getField(panel, "imageChoicePreviewComponent")).isShowing());
    assertEquals("Wrong scale step visibility", scaleStepShowing,
        ((JComponent)TestUtilities.getField(panel, "scalePreviewComponent")).isShowing());
    assertEquals("Wrong origin step visibility", originStepShowing,
        ((JComponent)TestUtilities.getField(panel, "originPreviewComponent")).isShowing());
  }
}
