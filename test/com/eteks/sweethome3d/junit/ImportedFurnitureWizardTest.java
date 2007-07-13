/*
 * ImportedFurnitureWizardTest.java 4 juil. 2007
 * 
 * Copyright (c) 2007 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;

import junit.extensions.abbot.ComponentTestFixture;
import abbot.finder.AWTHierarchy;
import abbot.finder.BasicFinder;
import abbot.finder.ComponentSearchException;
import abbot.finder.Matcher;
import abbot.tester.JComponentTester;

import com.eteks.sweethome3d.io.FileUserPreferences;
import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.Category;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.ContentManager;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.swing.ColorButton;
import com.eteks.sweethome3d.swing.HomeController;
import com.eteks.sweethome3d.swing.HomePane;
import com.eteks.sweethome3d.swing.ImportedFurnitureWizardController;
import com.eteks.sweethome3d.swing.ImportedFurnitureWizardStepsPanel;
import com.eteks.sweethome3d.swing.WizardPane;
import com.eteks.sweethome3d.tools.URLContent;

/**
 * Tests imported furniture wizard.
 * @author Emmanuel Puybaret
 */
public class ImportedFurnitureWizardTest extends ComponentTestFixture {
  public void testImportFurnitureWizard() throws ComponentSearchException, InterruptedException, 
      NoSuchFieldException, IllegalAccessException, InvocationTargetException {
    Locale.setDefault(Locale.FRANCE);
    final UserPreferences preferences = new FileUserPreferences();
    final URL testedModelName = ImportedFurnitureWizardTest.class.getResource("resources/test.obj");
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

      public String showOpenDialog(String dialogTitle, ContentType contentType) {
        // Return tested model name URL
        return testedModelName.toString();
      }

      public String showSaveDialog(String dialogTitle, ContentType contentType, String name) {
        return null;
      }      
    };
    Home home = new Home();
    final HomeController controller = new HomeController(home, preferences, contentManager);

    // 1. Create a frame that displays a home view 
    JFrame frame = new JFrame("Imported Furniture Wizard Test");    
    frame.add(controller.getView());
    frame.pack();

    // Show home plan frame
    showWindow(frame);
    JComponentTester tester = new JComponentTester();
    tester.waitForIdle();
    
    // 2. Transfer focus to plan view with TAB keys
    tester.actionKeyStroke(KeyEvent.VK_TAB);
    tester.actionKeyStroke(KeyEvent.VK_TAB);
    // Check plan view has focus
    assertTrue("Plan component doesn't have the focus", 
        controller.getPlanController().getView().isFocusOwner());
    // Open wizard to import a test object (1 width x 2 depth x 3 height) in plan
    tester.invokeLater(new Runnable() { 
        public void run() {
          // Display dialog box later in Event Dispatch Thread to avoid blocking test thread
          controller.getView().getActionMap().get(HomePane.ActionType.IMPORT_FURNITURE).actionPerformed(null);
        }
      });
    // Wait for import furniture view to be shown
    tester.waitForFrameShowing(new AWTHierarchy(), ResourceBundle.getBundle(
        ImportedFurnitureWizardController.class.getName()).getString("importFurnitureWizard.title"));
    // Check dialog box is displayed
    JDialog wizardDialog = (JDialog)TestUtilities.findComponent(frame, JDialog.class);
    assertTrue("Wizard view dialog not showing", wizardDialog.isShowing());

    // Retrieve ImportedFurnitureWizardStepsPanel components
    ImportedFurnitureWizardStepsPanel panel = (ImportedFurnitureWizardStepsPanel)TestUtilities.findComponent(
        frame, ImportedFurnitureWizardStepsPanel.class);
    JButton modelChoiceOrChangeButton = (JButton)TestUtilities.getField(panel, "modelChoiceOrChangeButton");
    JButton turnLeftButton = (JButton)TestUtilities.getField(panel, "turnLeftButton");
    JButton turnDownButton = (JButton)TestUtilities.getField(panel, "turnDownButton");
    JCheckBox backFaceShownCheckBox = (JCheckBox)TestUtilities.getField(panel, "backFaceShownCheckBox");
    JTextField nameTextField = (JTextField)TestUtilities.getField(panel, "nameTextField");
    JCheckBox addToCatalogCheckBox = (JCheckBox)TestUtilities.getField(panel, "addToCatalogCheckBox");
    JComboBox categoryComboBox = (JComboBox)TestUtilities.getField(panel, "categoryComboBox");
    JSpinner widthSpinner = (JSpinner)TestUtilities.getField(panel, "widthSpinner");
    JSpinner heightSpinner = (JSpinner)TestUtilities.getField(panel, "heightSpinner");
    JSpinner depthSpinner = (JSpinner)TestUtilities.getField(panel, "depthSpinner");
    JCheckBox keepProportionsCheckBox = (JCheckBox)TestUtilities.getField(panel, "keepProportionsCheckBox");
    JSpinner elevationSpinner = (JSpinner)TestUtilities.getField(panel, "elevationSpinner");
    JCheckBox movableCheckBox = (JCheckBox)TestUtilities.getField(panel, "movableCheckBox");
    JCheckBox doorOrWindowCheckBox = (JCheckBox)TestUtilities.getField(panel, "doorOrWindowCheckBox");
    ColorButton colorButton = (ColorButton)TestUtilities.getField(panel, "colorButton");
    JButton clearColorButton = (JButton)TestUtilities.getField(panel, "clearColorButton");
    
    // Check current step is model
    tester.waitForIdle();
    assertStepShowing(panel, true, false, false, false);    
    
    // 3. Choose tested model
    String modelChoiceOrChangeButtonText = modelChoiceOrChangeButton.getText();
    modelChoiceOrChangeButton.doClick();
    // Wait 1 s to let time to Java 3D to load the model
    Thread.sleep(1000);
    // Check choice button text changed
    assertFalse("Choice button text didn't change", 
        modelChoiceOrChangeButtonText.equals(modelChoiceOrChangeButton.getText()));
    // Click on next button
    WizardPane view = (WizardPane)TestUtilities.findComponent(frame, WizardPane.class);
    // Retrieve wizard view next button
    final JButton nextFinishOptionButton = (JButton)TestUtilities.getField(view, "nextFinishOptionButton"); 
    assertTrue("Next button isn't enabled", nextFinishOptionButton.isEnabled());
    nextFinishOptionButton.doClick();
    // Check current step is rotation
    tester.waitForIdle();
    assertStepShowing(panel, false, true, false, false);    
    // Check back face shown check box isn't selected by default
    assertFalse("Back face shown check box is selected", backFaceShownCheckBox.isSelected());
    
    // 4. Click on left button
    float width = (Float)widthSpinner.getValue();
    float depth = (Float)depthSpinner.getValue();
    float height = (Float)heightSpinner.getValue();
    turnLeftButton.doClick();
    // Check depth and width values were swapped 
    float newWidth = (Float)widthSpinner.getValue();
    float newDepth = (Float)depthSpinner.getValue();
    float newHeight = (Float)heightSpinner.getValue();
    assertEpsilonEquals("width", depth, newWidth);
    assertEpsilonEquals("depth", width, newDepth);
    assertEpsilonEquals("height", height, newHeight);
    // Click on down button
    width = newWidth;
    depth = newDepth;
    height = newHeight;
    turnDownButton.doClick();
    // Check height and depth values were swapped 
    newWidth = (Float)widthSpinner.getValue();
    newDepth = (Float)depthSpinner.getValue();
    newHeight = (Float)heightSpinner.getValue();
    assertEpsilonEquals("width", width, newWidth);
    assertEpsilonEquals("depth", height, newDepth);
    assertEpsilonEquals("height", depth, newHeight);
    
    // 5. Click on next button
    assertTrue("Next button isn't enabled", nextFinishOptionButton.isEnabled());
    nextFinishOptionButton.doClick();
    // Check current step is attributes
    tester.waitForIdle();
    assertStepShowing(panel, false, false, true, false);    
    
    // 6. Check default furniture name is the presentation name proposed by content manager
    assertEquals("Wrong default name", "test", 
        contentManager.getPresentationName(testedModelName.toString(), ContentManager.ContentType.MODEL));
    // Check Add to catalog check box isn't selected and category combo box 
    // is disabled when furniture is imported in home
    assertFalse("Add to catalog check box is selected", addToCatalogCheckBox.isSelected());
    assertFalse("Category combo box isn't disabled", categoryComboBox.isEnabled());
    // Check default category is first category  
    Category firstCategory = preferences.getCatalog().getCategories().get(0);
    assertEquals("Wrong default category", firstCategory, categoryComboBox.getSelectedItem());
    // Rename furniture with the name of the catalog first piece
    nameTextField.setText(firstCategory.getFurniture().get(0).getName());    
    // Check next button is enabled 
    assertTrue("Next button isn't enabled", nextFinishOptionButton.isEnabled());
    // Select Add to catalog check box
    addToCatalogCheckBox.setSelected(true);
    // Check next button is disabled because imported furniture has a wrong name
    assertFalse("Next button isn't disabled", nextFinishOptionButton.isEnabled());
    // Rename furniture and its category 
    final String testName = "#@" + System.currentTimeMillis() + "@#";
    nameTextField.setText(testName);    
    categoryComboBox.getEditor().selectAll();
    tester.actionKeyString(categoryComboBox.getEditor().getEditorComponent(), testName);    
    // Check next button is enabled again
    assertTrue("Next button isn't enabled", nextFinishOptionButton.isEnabled());
    
    // 7. Check keep proportions check box is selected by default
    assertTrue("Keep proportions check box isn't selected", keepProportionsCheckBox.isSelected());
    // Change width with a value 10 times greater
    width = newWidth;
    depth = newDepth;
    height = newHeight;
    widthSpinner.setValue(newWidth * 10);
    // Check height and depth values are 10 times greater 
    newWidth = (Float)widthSpinner.getValue();
    newDepth = (Float)depthSpinner.getValue();
    newHeight = (Float)heightSpinner.getValue();
    assertEpsilonEquals("width", 10 * width, newWidth);
    assertEpsilonEquals("depth", 10 * depth, newDepth);
    assertEpsilonEquals("height", 10 * height, newHeight);
    // Deselect keep proportions check box 
    keepProportionsCheckBox.setSelected(false);
    // Change width with a value 2 times greater
    width = newWidth;
    depth = newDepth;
    height = newHeight;
    widthSpinner.setValue(newWidth * 2);
    // Check height and depth values didn't change
    newWidth = (Float)widthSpinner.getValue();
    newDepth = (Float)depthSpinner.getValue();
    newHeight = (Float)heightSpinner.getValue();
    assertEpsilonEquals("width", 2 * width, newWidth);
    assertEpsilonEquals("depth", depth, newDepth);
    assertEpsilonEquals("height", height, newHeight);

    // 8. Change elevation, movable, door or window, color default values
    assertEquals("Wrong default elevation", 0f, (Float)elevationSpinner.getValue());
    elevationSpinner.setValue(10);
    assertTrue("Movable check box isn't selected", movableCheckBox.isSelected());
    movableCheckBox.setSelected(false);
    assertFalse("Door or window check box is selected", doorOrWindowCheckBox.isSelected());
    doorOrWindowCheckBox.setSelected(true);
    assertEquals("Wrong default color", null, colorButton.getColor());
    assertFalse("Clear color button isn't disabled", clearColorButton.isEnabled());
    // Change color
    colorButton.setColor(0x000000);
    // Check clear color button is enabled
    assertTrue("Clear color button isn't enabled", clearColorButton.isEnabled());
    // Click on clear color button
    clearColorButton.doClick();
    // Check color is null and clear color button is disabled
    assertEquals("Wrong color", null, colorButton.getColor());
    assertFalse("Clear color button isn't disabled", clearColorButton.isEnabled());
 
    // 9. Click on next button
    nextFinishOptionButton.doClick();
    // Check current step is icon
    tester.waitForIdle();
    assertStepShowing(panel, false, false, false, true);    
    tester.invokeAndWait(new Runnable() {
        public void run() {
          // Click on Finish to hide dialog box in Event Dispatch Thread
          nextFinishOptionButton.doClick(); 
        }
      });
    
    // 10. Check the matching new catalog piece of furniture was created and it's the selected piece
    List<CatalogPieceOfFurniture> selectedCatalogFurniture = 
        preferences.getCatalog().getSelectedFurniture();
    assertEquals("Wrong selected furniture count in catalog", 1, selectedCatalogFurniture.size());
    CatalogPieceOfFurniture catalogPiece = selectedCatalogFurniture.get(0);
    assertEquals("Wrong catalog piece name", testName, catalogPiece.getName());
    assertEquals("Wrong catalog piece category name", testName, catalogPiece.getCategory().getName());
    assertTrue("Catalog doesn't contain new piece", 
        preferences.getCatalog().getCategories().contains(catalogPiece.getCategory()));
    assertEpsilonEquals("width", newWidth, catalogPiece.getWidth());
    assertEpsilonEquals("depth", newDepth, catalogPiece.getDepth());
    assertEpsilonEquals("height", newHeight, catalogPiece.getHeight());
    assertEpsilonEquals("elevation", 10, catalogPiece.getElevation());
    assertFalse("Catalog piece is movable", catalogPiece.isMovable());
    assertTrue("Catalog piece isn't a door or window", catalogPiece.isDoorOrWindow());
    assertEquals("Wrong catalog piece color", null, catalogPiece.getColor());
    assertTrue("Catalog piece isn't modifiable", catalogPiece.isModifiable());
    
    // Check a new home piece of furniture was created and it's the selected piece in home
    List<Object> homeSelectedItems = home.getSelectedItems();
    assertEquals("Wrong selected furniture count in home", 1, homeSelectedItems.size());
    HomePieceOfFurniture homePiece = (HomePieceOfFurniture)homeSelectedItems.get(0);
    assertEquals("Wrong home piece name", testName, homePiece.getName());
    
    // 11. Transfer focus to catalog view with TAB keys
    tester.actionKeyStroke(KeyEvent.VK_TAB);
    tester.actionKeyStroke(KeyEvent.VK_TAB);
    // Check plan view has focus
    assertTrue("Catalog tree doesn't have the focus", 
        controller.getCatalogController().getView().isFocusOwner());
    // Delete new catalog piece of furniture
    final Action deleteAction = controller.getView().getActionMap().get(HomePane.ActionType.DELETE);
    assertTrue("Delete action isn't enable", deleteAction.isEnabled());
    tester.invokeLater(new Runnable() { 
        public void run() {
          deleteAction.actionPerformed(null);
        }
      });
    // Wait for confirm dialog to be shown
    tester.waitForFrameShowing(new AWTHierarchy(), ResourceBundle.getBundle(
        HomePane.class.getName()).getString("confirmDeleteCatalogSelection.title"));
    // Find displayed dialog box
    JDialog confirmDeleteCatalogSelectionDialog = (JDialog)new BasicFinder().find(frame, 
        new Matcher () {
          public boolean matches(Component component) {
            return component instanceof JDialog && component.isShowing();
          }
        });
    // Click on Ok in dialog box
    final JOptionPane optionPane = (JOptionPane)TestUtilities.findComponent(
        confirmDeleteCatalogSelectionDialog, JOptionPane.class);
    tester.invokeAndWait(new Runnable() {
        public void run() {
          // Select delete option to hide dialog box in Event Dispatch Thread
          optionPane.setValue(ResourceBundle.getBundle(
              HomePane.class.getName()).getString("confirmDeleteCatalogSelection.delete")); 
        }
      });
    // Check selection is empty
    selectedCatalogFurniture = preferences.getCatalog().getSelectedFurniture();
    assertTrue("Catalog selected furniture isn't empty", selectedCatalogFurniture.isEmpty());
    // Check catalog doesn't contain the new piece
    assertFalse("Piece is still in catalog", 
        preferences.getCatalog().getCategories().contains(catalogPiece.getCategory()));
    // Check home piece of furniture is still in home and selected
    assertTrue("Home piece isn't in home", home.getFurniture().contains(homePiece));
    assertTrue("Home piece isn't selecteed", home.getSelectedItems().contains(homePiece));
    
    // 12. Undo furniture creation in home
    controller.getView().getActionMap().get(HomePane.ActionType.UNDO).actionPerformed(null);
    // Check home is empty
    assertTrue("Home isn't empty", home.getFurniture().isEmpty());
    // Redo
    controller.getView().getActionMap().get(HomePane.ActionType.REDO).actionPerformed(null);
    // Check home piece of furniture is in home and selected
    assertTrue("Home piece isn't in home", home.getFurniture().contains(homePiece));
    assertTrue("Home piece isn't selecteed", home.getSelectedItems().contains(homePiece));
  }

  /**
   * Asserts if each <code>panel</code> step preview component is showing or not. 
   */
  private void assertStepShowing(ImportedFurnitureWizardStepsPanel panel,
                                 boolean modelStepShwing,
                                 boolean rotationStepShowing,
                                 boolean attributesStepShowing,
                                 boolean iconStepShowing) throws NoSuchFieldException, IllegalAccessException {
    
    assertEquals("Wrong model step visibility", modelStepShwing,
        ((JComponent)TestUtilities.getField(panel, "modelPreviewComponent")).isShowing());
    assertEquals("Wrong rotation step visibility", rotationStepShowing,
        ((JComponent)TestUtilities.getField(panel, "rotationPreviewComponent")).isShowing());
    assertEquals("Wrong attributes step visibility", attributesStepShowing,
        ((JComponent)TestUtilities.getField(panel, "attributesPreviewComponent")).isShowing());
    assertEquals("Wrong icon step visibility", iconStepShowing,
        ((JComponent)TestUtilities.getField(panel, "iconPreviewComponent")).isShowing());
  }
  
  /**
   * Asserts <code>expectedValue</code> is equal to <code>value</code>. 
   */
  private void assertEpsilonEquals(String valueName, float expectedValue, float value) {
    assertTrue("Incorrect " + valueName + " " + value + ", should be " + expectedValue, 
        Math.abs(expectedValue - value) < 1E-3);
  }
}
