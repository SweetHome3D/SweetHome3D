/*
 * PrintTest.java 27 aout 2007
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
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

import junit.extensions.abbot.ComponentTestFixture;
import abbot.finder.AWTHierarchy;
import abbot.finder.BasicFinder;
import abbot.finder.ComponentSearchException;
import abbot.finder.Matcher;
import abbot.tester.JComponentTester;

import com.eteks.sweethome3d.io.DefaultUserPreferences;
import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePrint;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.swing.HomeController;
import com.eteks.sweethome3d.swing.HomePane;
import com.eteks.sweethome3d.swing.HomePrintableComponent;
import com.eteks.sweethome3d.swing.PageSetupPanel;
import com.eteks.sweethome3d.swing.PrintPreviewPanel;

/**
 * Tests page setup and print preview panes in home.
 * @author Emmanuel Puybaret
 */
public class PrintTest extends ComponentTestFixture {
  public void testPageSetupAndPrintPreview() throws ComponentSearchException, InterruptedException, 
      NoSuchFieldException, IllegalAccessException, InvocationTargetException {
    UserPreferences preferences = new DefaultUserPreferences();
    Home home = new Home();
    final HomeController controller = new HomeController(home, preferences);

    // 1. Create a frame that displays a home view 
    JFrame frame = new JFrame("Home Print Test");    
    frame.add(controller.getView());
    frame.pack();

    // Show home frame
    showWindow(frame);
    JComponentTester tester = new JComponentTester();
    tester.waitForIdle();
    // Add a piece of furniture to home
    List<CatalogPieceOfFurniture> selectedPieces = Arrays.asList(
        new CatalogPieceOfFurniture [] {preferences.getCatalog().getCategories().get(0).getFurniture().get(0)}); 
    preferences.getCatalog().setSelectedFurniture(selectedPieces);
    runAction(controller, HomePane.ActionType.ADD_HOME_FURNITURE);
    // Check home contains one piece
    assertEquals("Home doesn't contain any furniture", 1, home.getFurniture().size());
    
    // 2. Edit page setup dialog box
    tester.invokeLater(new Runnable() { 
        public void run() {
          // Display dialog box later in Event Dispatch Thread to avoid blocking test thread
          runAction(controller, HomePane.ActionType.PAGE_SETUP);
        }
      });
    // Wait for page setup to be shown
    tester.waitForFrameShowing(new AWTHierarchy(), ResourceBundle.getBundle(
        PageSetupPanel.class.getName()).getString("pageSetup.title"));
    // Check dialog box is displayed
    JDialog pageSetupDialog = (JDialog)TestUtilities.findComponent(
        frame, JDialog.class);
    assertTrue("Page setup dialog not showing", pageSetupDialog.isShowing());
    // Retrieve PageSetupPanel components
    PageSetupPanel pageSetupPanel = (PageSetupPanel)TestUtilities.findComponent(
        frame, PageSetupPanel.class);
    JCheckBox furniturePrintedCheckBox = 
        (JCheckBox)TestUtilities.getField(pageSetupPanel, "furniturePrintedCheckBox");
    JCheckBox planPrintedCheckBox = 
        (JCheckBox)TestUtilities.getField(pageSetupPanel, "planPrintedCheckBox");;
    JCheckBox view3DPrintedCheckBox =
        (JCheckBox)TestUtilities.getField(pageSetupPanel, "view3DPrintedCheckBox");
    // Check default edited values
    assertTrue("Furniture printed not checked", furniturePrintedCheckBox.isSelected());
    assertTrue("Plan printed not checked", planPrintedCheckBox.isSelected());
    assertTrue("View 3D printed not checked", view3DPrintedCheckBox.isSelected());
    
    // 3. Change dialog box values
    planPrintedCheckBox.setSelected(false);
    // Click on Ok in dialog box
    final JOptionPane pageSetupOptionPane = (JOptionPane)TestUtilities.findComponent(
        pageSetupDialog, JOptionPane.class);
    tester.invokeAndWait(new Runnable() {
        public void run() {
          // Select Ok option to hide dialog box in Event Dispatch Thread
          pageSetupOptionPane.setValue(JOptionPane.OK_OPTION); 
        }
      });
    assertFalse("Page setup dialog still showing", pageSetupDialog.isShowing());
    PageFormat pageFormat = PrinterJob.getPrinterJob().defaultPage();
    // Check home print attributes are modified accordingly
    assertHomePrintEqualPrintAttributes(pageFormat, true, false, true, home);
    
    // 4. Undo changes
    runAction(controller, HomePane.ActionType.UNDO);
    // Check home attributes have previous values
    assertEquals("Home print set", null, home.getPrint());
    // Redo
    runAction(controller, HomePane.ActionType.REDO);
    // Check home attributes are modified accordingly
    assertHomePrintEqualPrintAttributes(pageFormat, true, false, true, home);
    
    // 5. Show print preview dialog box
    tester.invokeLater(new Runnable() { 
        public void run() {
          // Display dialog box later in Event Dispatch Thread to avoid blocking test thread
          runAction(controller, HomePane.ActionType.PRINT_PREVIEW);
        }
      });
    // Wait for print preview to be shown
    tester.waitForFrameShowing(new AWTHierarchy(), ResourceBundle.getBundle(
        PrintPreviewPanel.class.getName()).getString("printPreview.title"));
    // Check dialog box is displayed
    JDialog printPreviewDialog = (JDialog)new BasicFinder().find(frame, 
        new Matcher () {
            public boolean matches(Component component) {
              return component instanceof JDialog && component.isShowing();              
            }
          });
    assertTrue("Print preview dialog not showing", printPreviewDialog.isShowing());
    // Retrieve PageSetupPanel components
    PrintPreviewPanel printPreviewPanel = (PrintPreviewPanel)TestUtilities.findComponent(
        frame, PrintPreviewPanel.class);
    JToolBar toolBar = 
        (JToolBar)TestUtilities.getField(printPreviewPanel, "toolBar");
    JButton previousButton = (JButton)toolBar.getComponent(0); 
    JButton nextButton = (JButton)toolBar.getComponent(1); 
    HomePrintableComponent printableComponent = 
        (HomePrintableComponent)TestUtilities.getField(printPreviewPanel, "printableComponent");;
    // Check if buttons are enabled and if printable component displays the first page
    assertFalse("Previous button is enabled", previousButton.isEnabled());
    assertTrue("Next button is disabled", nextButton.isEnabled());
    assertEquals("Printable component doesn't display first page", 0, printableComponent.getPage());
    assertEquals("Wrong printable component page count", 2, printableComponent.getPageCount());
    
    // 6. Click on next page button
    nextButton.doClick();
    // Check if buttons are enabled and if printable component displays the second page
    assertTrue("Previous button is enabled", previousButton.isEnabled());
    assertFalse("Next button is disabled", nextButton.isEnabled());
    assertEquals("Printable component doesn't display second page", 1, printableComponent.getPage());
    
    // Click on Ok in dialog box
    final JOptionPane printPreviewOptionPane = (JOptionPane)TestUtilities.findComponent(
        printPreviewDialog, JOptionPane.class);
    tester.invokeAndWait(new Runnable() {
        public void run() {
          // Select Ok option to hide dialog box in Event Dispatch Thread
          printPreviewOptionPane.setValue(JOptionPane.OK_OPTION); 
        }
      });
    assertFalse("Print preview dialog still showing", printPreviewDialog.isShowing());
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
   * Asserts the print attributes given in parameter match <code>home</code> print.
   */
  private void assertHomePrintEqualPrintAttributes(PageFormat pageFormat,
                                                   boolean furniturePrinted,
                                                   boolean planPrinted,
                                                   boolean view3DPrinted, 
                                                   Home home) {
    HomePrint homePrint = home.getPrint();
    assertEquals("Wrong paper width", (float)pageFormat.getWidth(), homePrint.getPaperWidth());
    assertEquals("Wrong paper height", (float)pageFormat.getHeight(), homePrint.getPaperHeight());
    assertEquals("Wrong paper left margin", (float)pageFormat.getImageableX(), homePrint.getPaperLeftMargin());
    assertEquals("Wrong paper top margin", (float)pageFormat.getImageableY(), homePrint.getPaperTopMargin());
    assertEquals("Wrong paper right margin", 
        (float)(pageFormat.getWidth() - pageFormat.getImageableX() - pageFormat.getImageableWidth()), 
        homePrint.getPaperRightMargin());
    assertEquals("Wrong paper bottom margin", 
        (float)(pageFormat.getHeight() - pageFormat.getImageableY() - pageFormat.getImageableHeight()), 
        homePrint.getPaperBottomMargin());
    switch (pageFormat.getOrientation()) {
      case PageFormat.PORTRAIT :
        assertEquals("Wrong paper orientation", 
            HomePrint.PaperOrientation.PORTRAIT, homePrint.getPaperOrientation());
        break;
      case PageFormat.LANDSCAPE :
        assertEquals("Wrong paper orientation", 
            HomePrint.PaperOrientation.LANDSCAPE, homePrint.getPaperOrientation());
        break;
      case PageFormat.REVERSE_LANDSCAPE :
        assertEquals("Wrong paper orientation", 
            HomePrint.PaperOrientation.REVERSE_LANDSCAPE, homePrint.getPaperOrientation());
        break;
    }
    assertEquals("Wrong furniture printed", furniturePrinted, homePrint.isFurniturePrinted());
    assertEquals("Wrong plan printed", planPrinted, homePrint.isPlanPrinted());
    assertEquals("Wrong view 3D printed", view3DPrinted, homePrint.isView3DPrinted());
  }
}
