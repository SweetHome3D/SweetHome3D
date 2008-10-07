/*
 * ImportedTextureWizardTest.java 07 Oct. 2008
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

import java.awt.Component;
import java.awt.Container;
import java.awt.KeyboardFocusManager;
import java.awt.event.InputEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;

import junit.extensions.abbot.ComponentTestFixture;
import abbot.finder.AWTHierarchy;
import abbot.finder.BasicFinder;
import abbot.finder.ComponentSearchException;
import abbot.finder.Matcher;
import abbot.finder.matchers.ClassMatcher;
import abbot.finder.matchers.WindowMatcher;
import abbot.tester.JComponentTester;

import com.eteks.sweethome3d.io.FileUserPreferences;
import com.eteks.sweethome3d.model.CatalogTexture;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.ContentManager;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.TexturesCategory;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.swing.HomeController;
import com.eteks.sweethome3d.swing.HomePane;
import com.eteks.sweethome3d.swing.ImportedTextureWizardController;
import com.eteks.sweethome3d.swing.ImportedTextureWizardStepsPanel;
import com.eteks.sweethome3d.swing.PlanComponent;
import com.eteks.sweethome3d.swing.TextureChoiceComponent;
import com.eteks.sweethome3d.swing.WallController;
import com.eteks.sweethome3d.swing.WallPanel;
import com.eteks.sweethome3d.swing.WizardPane;
import com.eteks.sweethome3d.tools.URLContent;

/**
 * Tests imported texture wizard.
 * @author Emmanuel Puybaret
 */
public class ImportedTextureWizardTest extends ComponentTestFixture {
  public void testImportedTextureWizard() throws ComponentSearchException, InterruptedException, 
      NoSuchFieldException, IllegalAccessException, InvocationTargetException {
    String language = Locale.getDefault().getLanguage();
    final UserPreferences preferences = new FileUserPreferences();
    // Ensure we use default language and centimeter unit
    preferences.setLanguage(language);
    preferences.setUnit(UserPreferences.Unit.CENTIMETER);
    // Create a dummy content manager
    final URL testedImageName = BackgroundImageWizardTest.class.getResource("resources/test.png");
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

      public String showOpenDialog(String dialogTitle, ContentType contentType) {
        // Return tested model name URL
        return testedImageName.toString();
      }

      public String showSaveDialog(String dialogTitle, ContentType contentType, String name) {
        return null;
      }      
    };
    Home home = new Home();
    final HomeController controller = new HomeController(home, preferences, contentManager);
    PlanComponent planComponent = (PlanComponent)TestUtilities.findComponent(
         controller.getView(), PlanComponent.class);

    // 1. Create a frame that displays a home view 
    JFrame frame = new JFrame("Imported Texture Wizard Test");    
    frame.add(controller.getView());
    frame.pack();

    // Show home plan frame
    showWindow(frame);
    JComponentTester tester = new JComponentTester();
    tester.waitForIdle();
    // Transfer focus to plan view 
    planComponent.requestFocusInWindow();
    tester.waitForIdle();
    
    // Check plan view has focus
    assertTrue("Plan component doesn't have the focus", planComponent.isFocusOwner());
    
    // 2. Create two wall between points (50, 50), (150, 50) and (150, 150) 
    runAction(controller, HomePane.ActionType.CREATE_WALLS);
    tester.actionClick(planComponent, 50, 50);
    tester.actionClick(planComponent, 150, 50);
    tester.actionClick(planComponent, 150, 150, InputEvent.BUTTON1_MASK, 2);
    runAction(controller, HomePane.ActionType.SELECT);
    // Check two walls were created and selected
    assertEquals("Wrong wall count in home", 2, home.getWalls().size());
    assertEquals("Wrong selected items count in home", 2, home.getSelectedItems().size());
    Iterator<Wall> iterator = home.getWalls().iterator();
    Wall wall1 = iterator.next();
    Wall wall2 = iterator.next();
    // Check walls don't use texture yet
    assertNull("Wrong texture on wall 1 left side", wall1.getLeftSideTexture());
    assertNull("Wrong texture on wall 2 left side", wall2.getLeftSideTexture());
    assertNull("Wrong texture on wall 1 right side", wall1.getRightSideTexture());
    assertNull("Wrong texture on wall 2 right side", wall2.getRightSideTexture());
    
    // 3. Edit walls
    JDialog attributesDialog = showWallPanel(controller, frame, tester);
    // Retrieve WallPanel components
    WallPanel wallPanel = (WallPanel)TestUtilities.findComponent(
        attributesDialog, WallPanel.class);
    JSpinner xStartSpinner =
        (JSpinner)TestUtilities.getField(wallPanel, "xStartSpinner");
    JSpinner xEndSpinner =
        (JSpinner)TestUtilities.getField(wallPanel, "xEndSpinner");
    TextureChoiceComponent rightSideTextureComponent =
        (TextureChoiceComponent)TestUtilities.getField(wallPanel, "rightSideTextureComponent");
    // Check xStartSpinner and xEndSpinner panels aren't visible
    assertFalse("X start spinner panel is visible", xStartSpinner.getParent().isVisible());
    assertFalse("X end spinner panel is visible", xEndSpinner.getParent().isVisible());
    // Edit right side texture
    JDialog textureDialog = showTexturePanel(rightSideTextureComponent, false, attributesDialog, tester);
    JList availableTexturesList = (JList)new BasicFinder().find(textureDialog, 
        new ClassMatcher(JList.class, true));
    int textureCount = availableTexturesList.getModel().getSize();
    CatalogTexture defaultTexture = (CatalogTexture)availableTexturesList.getSelectedValue();
    // Import texture
    JDialog textureWizardDialog = showImportTextureWizard(textureDialog, tester, false);    
    // Retrieve ImportedFurnitureWizardStepsPanel components
    ImportedTextureWizardStepsPanel panel = (ImportedTextureWizardStepsPanel)TestUtilities.findComponent(
        textureWizardDialog, ImportedTextureWizardStepsPanel.class);
    JButton imageChoiceOrChangeButton = (JButton)TestUtilities.getField(panel, "imageChoiceOrChangeButton");
    JTextField nameTextField = (JTextField)TestUtilities.getField(panel, "nameTextField");
    JComboBox categoryComboBox = (JComboBox)TestUtilities.getField(panel, "categoryComboBox");
    JSpinner widthSpinner = (JSpinner)TestUtilities.getField(panel, "widthSpinner");
    JSpinner heightSpinner = (JSpinner)TestUtilities.getField(panel, "heightSpinner");
    
    // Check current step is image
    tester.waitForIdle();
    assertStepShowing(panel, true, false);    
    WizardPane view = (WizardPane)TestUtilities.findComponent(textureWizardDialog, WizardPane.class);
    // Check wizard view next button is disabled
    final JButton nextFinishOptionButton = (JButton)TestUtilities.getField(view, "nextFinishOptionButton");
    assertFalse("Next button is enabled", nextFinishOptionButton.isEnabled());
    
    // 4. Choose tested image 
    String imageChoiceOrChangeButtonText = imageChoiceOrChangeButton.getText();
    imageChoiceOrChangeButton.doClick();
    // Wait 200 s to let time to Java to load the image
    Thread.sleep(200);
    // Check choice button text changed
    assertFalse("Choice button text didn't change", 
        imageChoiceOrChangeButtonText.equals(imageChoiceOrChangeButton.getText()));
    // Click on next button
    nextFinishOptionButton.doClick();
    // Check current step is attributes
    tester.waitForIdle();
    assertStepShowing(panel, false, true);

    // 5. Check default furniture name is the presentation name proposed by content manager
    assertEquals("Wrong default name", 
        contentManager.getPresentationName(testedImageName.toString(), ContentManager.ContentType.IMAGE),
        nameTextField.getText()); 
    // Check name text field has focus
    assertSame("Name text field doesn't have focus", nameTextField,
        KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner());    
    // Check default category is user category  
    String userCategoryName = ResourceBundle.getBundle(
        ImportedTextureWizardStepsPanel.class.getName()).getString("userCategory");
    assertEquals("Wrong default category", userCategoryName, 
        ((TexturesCategory)categoryComboBox.getSelectedItem()).getName());
    // Rename texture  
    String textureTestName = "#@" + System.currentTimeMillis() + "@#";
    nameTextField.setText(textureTestName);    
    // Check next button is enabled again
    assertTrue("Next button isn't enabled", nextFinishOptionButton.isEnabled());

    // 6. Change width with a value 5 times greater
    float width = (Float)widthSpinner.getValue();
    float height = (Float)heightSpinner.getValue();
    widthSpinner.setValue(width * 5);
    // Check height is 5 times greater 
    float newWidth = (Float)widthSpinner.getValue();
    float newHeight = (Float)heightSpinner.getValue();
    assertEquals("width", 5 * width, newWidth);
    assertEquals("height", 5 * height, newHeight);
    
    tester.invokeAndWait(new Runnable() {
        public void run() {
          // Click on Finish to hide dialog box in Event Dispatch Thread
          nextFinishOptionButton.doClick(); 
        }
      });
    assertFalse("Import texture wizard still showing", textureWizardDialog.isShowing());

    // Check the list of available textures has one more selected modifiable texture 
    assertEquals("Wrong texture count in list", textureCount + 1, availableTexturesList.getModel().getSize());
    assertEquals("No selected texture in list", 1, availableTexturesList.getSelectedValues().length);
    CatalogTexture importedTexture = (CatalogTexture)availableTexturesList.getSelectedValue();
    assertNotSame("Wrong selected texture in list", defaultTexture, importedTexture);
    // Check the attributes of the new texture
    assertEquals("Wrong name", textureTestName, importedTexture.getName());
    assertEquals("Wrong category", userCategoryName, importedTexture.getCategory().getName());
    assertEquals("Wrong width", newWidth, importedTexture.getWidth());
    assertEquals("Wrong height", newHeight, importedTexture.getHeight());
    assertTrue("New texture isn't modifiable", importedTexture.isModifiable());
        
    // 7. Click on OK in texture dialog box
    doClickOnOkInDialog(textureDialog, tester);
    // Click on OK in wall dialog box
    doClickOnOkInDialog(attributesDialog, tester);    
    // Check wall attributes are modified accordingly
    assertNull("Wrong texture on wall 1 left side", wall1.getLeftSideTexture());
    assertNull("Wrong texture on wall 2 left side", wall2.getLeftSideTexture());
    assertEquals("Wrong texture on wall 1 right side", textureTestName, wall1.getRightSideTexture().getName());
    assertEquals("Wrong texture on wall 2 right side", textureTestName, wall2.getRightSideTexture().getName());
    
    // 8. Edit left side texture of first wall
    home.setSelectedItems(Arrays.asList(wall1));
    assertEquals("Wrong selected items count in home", 1, home.getSelectedItems().size());
    attributesDialog = showWallPanel(controller, frame, tester);
    // Retrieve WallPanel components
    wallPanel = (WallPanel)TestUtilities.findComponent(attributesDialog, WallPanel.class);
    xStartSpinner = (JSpinner)TestUtilities.getField(wallPanel, "xStartSpinner");
    xEndSpinner = (JSpinner)TestUtilities.getField(wallPanel, "xEndSpinner");
    TextureChoiceComponent leftSideTextureComponent =
        (TextureChoiceComponent)TestUtilities.getField(wallPanel, "leftSideTextureComponent");
    // Check xStartSpinner and xEndSpinner panels are visible
    assertTrue("X start spinner panel isn't visible", xStartSpinner.getParent().isVisible());
    assertTrue("X end spinner panel isn't visible", xEndSpinner.getParent().isVisible());
    // Edit left side texture
    textureDialog = showTexturePanel(leftSideTextureComponent, true, attributesDialog, tester);
    availableTexturesList = (JList)new BasicFinder().find(textureDialog, 
        new ClassMatcher(JList.class, true));
    textureCount = availableTexturesList.getModel().getSize();
    // Select imported texture
    availableTexturesList.setSelectedValue(importedTexture, true);
    // Modify texture
    textureWizardDialog = showImportTextureWizard(textureDialog, tester, true);    
    // Retrieve ImportedFurnitureWizardStepsPanel components
    panel = (ImportedTextureWizardStepsPanel)TestUtilities.findComponent(
        textureWizardDialog, ImportedTextureWizardStepsPanel.class);
    imageChoiceOrChangeButton = (JButton)TestUtilities.getField(panel, "imageChoiceOrChangeButton");
    widthSpinner = (JSpinner)TestUtilities.getField(panel, "widthSpinner");
    final JButton nextFinishOptionButton2 = (JButton)TestUtilities.getField(
        TestUtilities.findComponent(textureWizardDialog, WizardPane.class), "nextFinishOptionButton");
    tester.waitForIdle();
    nextFinishOptionButton2.doClick();
    
    // Change width
    widthSpinner.setValue((Float)widthSpinner.getValue() * 2);
    newWidth = (Float)widthSpinner.getValue();
    tester.invokeAndWait(new Runnable() {
        public void run() {
          // Click on Finish to hide dialog box in Event Dispatch Thread
          nextFinishOptionButton2.doClick(); 
        }
      });
    assertFalse("Import texture wizard still showing", textureWizardDialog.isShowing());
    // Check the list of available textures has the same texture count 
    // and a new selected texture 
    assertEquals("Wrong texture count in list", textureCount, availableTexturesList.getModel().getSize());
    assertEquals("No selected texture in list", 1, availableTexturesList.getSelectedValues().length);
    CatalogTexture modifiedTexture = (CatalogTexture)availableTexturesList.getSelectedValue();
    assertNotSame("Wrong selected texture in list", importedTexture, modifiedTexture);
    // Check the attributes of the new texture
    assertEquals("Wrong name", textureTestName, modifiedTexture.getName());
    assertEquals("Wrong category", userCategoryName, modifiedTexture.getCategory().getName());
    assertEquals("Wrong width", newWidth, modifiedTexture.getWidth());
    assertTrue("New texture isn't modifiable", modifiedTexture.isModifiable());

    // 9. Click on OK in texture dialog box
    doClickOnOkInDialog(textureDialog, tester);
    // Click on OK in wall dialog box
    doClickOnOkInDialog(attributesDialog, tester);    
    // Check wall attributes are modified accordingly
    assertEquals("Wrong texture on wall 1 left side", newWidth, wall1.getLeftSideTexture().getWidth());
    assertNull("Wrong texture on wall 2 left side", wall2.getLeftSideTexture());
    assertEquals("Wrong texture on wall 1 right side", newWidth / 2, wall1.getRightSideTexture().getWidth());
    assertEquals("Wrong texture on wall 2 right side", newWidth / 2, wall2.getRightSideTexture().getWidth());
    
    // 10. Open wall dialog a last time to delete the modified texture
    attributesDialog = showWallPanel(controller, frame, tester);
    // Retrieve WallPanel components
    wallPanel = (WallPanel)TestUtilities.findComponent(attributesDialog, WallPanel.class);
    leftSideTextureComponent = (TextureChoiceComponent)TestUtilities.getField(wallPanel, "leftSideTextureComponent");
    // Edit left side texture
    textureDialog = showTexturePanel(leftSideTextureComponent, true, attributesDialog, tester);
    availableTexturesList = (JList)new BasicFinder().find(textureDialog, 
        new ClassMatcher(JList.class, true));
    textureCount = availableTexturesList.getModel().getSize();
    // Select modified texture
    availableTexturesList.setSelectedValue(modifiedTexture, true);
    final JButton deleteButton = (JButton)new BasicFinder().find(textureDialog, 
        new Matcher() {
          public boolean matches(Component c) {
            return c instanceof JButton && ((JButton)c).getText().equals(ResourceBundle.getBundle(
                TextureChoiceComponent.class.getName()).getString("deleteTextureButton.text"));
          }
        });
    tester.invokeLater(new Runnable() { 
      public void run() {
        // Display confirm dialog box later in Event Dispatch Thread to avoid blocking test thread
        deleteButton.doClick();        }
    });
    // Wait for confirm dialog to be shown
    final String confirmDeleteSelectedCatalogTextureDialogTitle = ResourceBundle.getBundle(
        TextureChoiceComponent.class.getName()).getString("confirmDeleteSelectedCatalogTexture.title");
    tester.waitForFrameShowing(new AWTHierarchy(), confirmDeleteSelectedCatalogTextureDialogTitle);
    // Check dialog box is displayed
    JDialog confirmDialog = (JDialog)new BasicFinder().find(textureDialog,  
        new Matcher() {
            public boolean matches(Component c) {
              return c instanceof JDialog && ((JDialog)c).getTitle().equals(
                  confirmDeleteSelectedCatalogTextureDialogTitle);
            }
          });
    assertTrue("Confirm dialog not showing", confirmDialog.isShowing());
    doClickOnOkInDialog(confirmDialog, tester);
    tester.waitForIdle();
    // Check the list of available textures has one less texture and no selected texture 
    assertEquals("Wrong texture count in list", textureCount - 1, availableTexturesList.getModel().getSize());
    assertEquals("No selected texture in list", 0, availableTexturesList.getSelectedValues().length);
    // Check delete button is disabled
    assertFalse("Delete button isn't disabled", deleteButton.isEnabled());
    // Click on OK in texture dialog box
    doClickOnOkInDialog(textureDialog, tester);
    // Click on OK in wall dialog box
    doClickOnOkInDialog(attributesDialog, tester);    
    // Check wall attributes didn't change
    assertNotNull("Wrong texture on wall 1 left side", wall1.getLeftSideTexture());
    assertNull("Wrong texture on wall 2 left side", wall2.getLeftSideTexture());
    assertNotNull("Wrong texture on wall 1 right side", wall1.getRightSideTexture());
    assertNotNull("Wrong texture on wall 2 right side", wall2.getRightSideTexture());
  }

  /**
   * Returns the dialog that displays wall attributes. 
   */
  private JDialog showWallPanel(final HomeController controller, 
                                JFrame parent, JComponentTester tester) 
            throws ComponentSearchException {
    tester.invokeLater(new Runnable() { 
        public void run() {
          // Display dialog box later in Event Dispatch Thread to avoid blocking test thread
          runAction(controller, HomePane.ActionType.MODIFY_WALL);
        }
      });
    // Wait for wall view to be shown
    tester.waitForFrameShowing(new AWTHierarchy(), ResourceBundle.getBundle(
        WallPanel.class.getName()).getString("wall.title"));
    // Check dialog box is displayed
    JDialog attributesDialog = (JDialog)new BasicFinder().find(parent, 
        new ClassMatcher (JDialog.class, true));
    assertTrue("Wall dialog not showing", attributesDialog.isShowing());
    return attributesDialog;
  }
  
  /**
   * Returns the dialog that displays texture panel. 
   */
  private JDialog showTexturePanel(final TextureChoiceComponent textureComponent, 
                                   boolean leftSide,
                                   Container parent, JComponentTester tester) 
            throws ComponentSearchException {
    tester.invokeLater(new Runnable() { 
        public void run() {
          // Display dialog box later in Event Dispatch Thread to avoid blocking test thread
          textureComponent.doClick();        }
      });
    // Wait for texture panel to be shown
    String textureTitle = ResourceBundle.getBundle(
        WallController.class.getName()).getString(leftSide ? "leftSideTextureTitle" : "rightSideTextureTitle");
    tester.waitForFrameShowing(new AWTHierarchy(), textureTitle);
    // Check texture dialog box is displayed
    JDialog textureDialog = (JDialog)new BasicFinder().find(parent, 
        new WindowMatcher(textureTitle));
    assertTrue("Texture dialog not showing", textureDialog.isShowing());
    return textureDialog;
  }

  /**
   * Returns the dialog that displays texture import wizard. 
   */
  private JDialog showImportTextureWizard(Container parent, JComponentTester tester, 
                                          final boolean modify) 
            throws ComponentSearchException {
    final JButton button = (JButton)new BasicFinder().find(parent, 
        new Matcher() {
          public boolean matches(Component c) {
            return c instanceof JButton && ((JButton)c).getText().equals(ResourceBundle.getBundle(
                TextureChoiceComponent.class.getName()).getString(
                    modify ? "modifyTextureButton.text" : "importTextureButton.text"));
          }
        });
    tester.invokeLater(new Runnable() { 
        public void run() {
          // Display dialog box later in Event Dispatch Thread to avoid blocking test thread
          button.doClick();        }
      });
    // Wait for texture wizard to be shown
    String textureWizardTitle = ResourceBundle.getBundle(
        ImportedTextureWizardController.class.getName()).getString(
            modify ? "modifyTextureWizard.title" : "importTextureWizard.title");
    tester.waitForFrameShowing(new AWTHierarchy(), textureWizardTitle);
    // Check texture dialog box is displayed
    JDialog textureDialog = (JDialog)new BasicFinder().find(parent, 
        new WindowMatcher(textureWizardTitle));
    assertTrue("Texture wizard not showing", textureDialog.isShowing());
    return textureDialog;
  }

  /**
   * Clicks on OK in dialog to close it.
   */
  private void doClickOnOkInDialog(JDialog dialog, JComponentTester tester) 
            throws ComponentSearchException {
    final JOptionPane attributesOptionPane = (JOptionPane)TestUtilities.findComponent(
        dialog, JOptionPane.class);
    tester.invokeAndWait(new Runnable() {
        public void run() {
          // Select Ok option to hide dialog box in Event Dispatch Thread
          if (attributesOptionPane.getOptions() != null) {
            attributesOptionPane.setValue(attributesOptionPane.getOptions() [0]);
          } else {
            attributesOptionPane.setValue(JOptionPane.OK_OPTION);
          }
        }
      });
    assertFalse("Dialog still showing", dialog.isShowing());
  }

  /**
   * Runs <code>actionPerformed</code> method matching <code>actionType</code> 
   * in <code>HomePane</code>. 
   */
  private void runAction(HomeController controller,
                         HomePane.ActionType actionType) {
    controller.getView().getActionMap().get(actionType).actionPerformed(null);
  }

  /**
   * Asserts if each <code>panel</code> step preview component is showing or not. 
   */
  private void assertStepShowing(ImportedTextureWizardStepsPanel panel,
                                 boolean imageStepShwing,
                                 boolean attributesStepShowing) throws NoSuchFieldException, IllegalAccessException {    
    assertEquals("Wrong image step visibility", imageStepShwing,
        ((JComponent)TestUtilities.getField(panel, "imageChoicePreviewComponent")).isShowing());
    assertEquals("Wrong attributes step visibility", attributesStepShowing,
        ((JComponent)TestUtilities.getField(panel, "attributesPreviewComponent")).isShowing());
  }
}
