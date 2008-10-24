/*
 * PageSetupPanel.java 27 aout 07
 *
 * Copyright (c) 2007 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterJob;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePrint;
import com.eteks.sweethome3d.tools.OperatingSystem;

/**
 * Home page setup editing panel.
 * @author Emmanuel Puybaret
 */
public class PageSetupPanel extends JPanel {

  private PageSetupController controller;
  private ResourceBundle      resource;
  private PageFormat          pageFormat;
  private JButton             pageFormatButton;
  private JCheckBox           furniturePrintedCheckBox;
  private JCheckBox           planPrintedCheckBox;
  private JCheckBox           view3DPrintedCheckBox;

  /**
   * Creates a panel that displays page setup.
   * @param home home with print attributes edited by this panel
   * @param controller the controller of this panel
   */
  public PageSetupPanel(Home home,
                        PageSetupController controller) {
    super(new GridBagLayout());
    this.controller = controller;
    this.resource = ResourceBundle.getBundle(
        PageSetupPanel.class.getName());
    createComponents();
    setMnemonics();
    layoutComponents();
    updateComponents(home);
  }

  /**
   * Creates and initializes components.
   */
  private void createComponents() {
    this.pageFormatButton = new JButton(this.resource.getString("pageFormatButton.text"));
    this.pageFormatButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          // Show the page setup dialog
          PrinterJob printerJob = PrinterJob.getPrinterJob();
          pageFormat = printerJob.pageDialog(pageFormat);
        }
      });
    this.furniturePrintedCheckBox = new JCheckBox(
        this.resource.getString("furniturePrintedCheckBox.text")); 
    this.planPrintedCheckBox = new JCheckBox(
        this.resource.getString("planPrintedCheckBox.text")); 
    this.view3DPrintedCheckBox = new JCheckBox(
        this.resource.getString("view3DPrintedCheckBox.text")); 
  }
  
  /**
   * Sets components mnemonics and label / component associations.
   */
  private void setMnemonics() {
    if (!OperatingSystem.isMacOSX()) {
      this.pageFormatButton.setMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("pageFormatButton.mnemonic")).getKeyCode());
      this.furniturePrintedCheckBox.setMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("furniturePrintedCheckBox.mnemonic")).getKeyCode());
      this.planPrintedCheckBox.setMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("planPrintedCheckBox.mnemonic")).getKeyCode());
      this.view3DPrintedCheckBox.setMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("view3DPrintedCheckBox.mnemonic")).getKeyCode());
    }
  }
  
  /**
   * Layouts panel components in panel with their labels. 
   */
  private void layoutComponents() {
    // First row
    add(this.pageFormatButton, new GridBagConstraints(
        0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.NONE, new Insets(0, 0, 10, 0) , 0, 0));
    // Second row
    Insets componentInsets = new Insets(0, 0, 5, 0);
    add(this.furniturePrintedCheckBox, new GridBagConstraints(
        0, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, componentInsets , 0, 0));
    // Third row
    add(this.planPrintedCheckBox, new GridBagConstraints(
        0, 2, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, componentInsets , 0, 0));
    // Last row
    add(this.view3DPrintedCheckBox, new GridBagConstraints(
        0, 3, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
  }

  /**
   * Updates components.
   */
  private void updateComponents(Home home) {
    HomePrint homePrint = home.getPrint();
    this.pageFormat = PageSetupPanel.getPageFormat(homePrint);
    // Check if off screen image is supported 
    boolean offscreenCanvas3DSupported = Component3DManager.getInstance().isOffScreenImageSupported();
    if (homePrint != null) {
      this.furniturePrintedCheckBox.setSelected(homePrint.isFurniturePrinted());
      this.planPrintedCheckBox.setSelected(homePrint.isPlanPrinted());
      this.view3DPrintedCheckBox.setSelected(homePrint.isView3DPrinted() && offscreenCanvas3DSupported);
    } else {
      this.furniturePrintedCheckBox.setSelected(true);
      this.planPrintedCheckBox.setSelected(true);
      this.view3DPrintedCheckBox.setSelected(offscreenCanvas3DSupported);
    }
    this.view3DPrintedCheckBox.setEnabled(offscreenCanvas3DSupported);
  }
  
  /**
   * Returns the print attributes matching user choice.
   */
  public HomePrint getHomePrint() {
    // Return an HomePrint instance matching returnedPageFormat
    HomePrint.PaperOrientation paperOrientation; 
    switch (this.pageFormat.getOrientation()) {
      case PageFormat.LANDSCAPE :
        paperOrientation = HomePrint.PaperOrientation.LANDSCAPE;
        break;
      case PageFormat.REVERSE_LANDSCAPE :
        paperOrientation = HomePrint.PaperOrientation.REVERSE_LANDSCAPE;
        break;
      default :
        paperOrientation = HomePrint.PaperOrientation.PORTRAIT;
        break;
    }
    Paper paper = this.pageFormat.getPaper();
    return new HomePrint(paperOrientation, (float)paper.getWidth(), (float)paper.getHeight(),
        (float)paper.getImageableY(), (float)paper.getImageableX(),
        (float)(paper.getHeight() - paper.getImageableHeight() - paper.getImageableY()),
        (float)(paper.getWidth() - paper.getImageableWidth() - paper.getImageableX()),
        this.furniturePrintedCheckBox.isSelected(),
        this.planPrintedCheckBox.isSelected(),
        this.view3DPrintedCheckBox.isSelected());
  }

  /**
   * Displays this panel in a modal dialog box. 
   */
  public void displayView(JComponent parent) {
    String dialogTitle = resource.getString("pageSetup.title");
    if (JOptionPane.showConfirmDialog(SwingUtilities.getRootPane(parent), this, dialogTitle, 
        JOptionPane.OK_CANCEL_OPTION, 
        JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION
        && this.controller != null) {
      this.controller.modifyHomePrint();
    }
  }

  /**
   * Returns a <code>PageFormat</code> object created from <code>homePrint</code>.
   */
  public static PageFormat getPageFormat(HomePrint homePrint) {
    PrinterJob printerJob = PrinterJob.getPrinterJob();
    if (homePrint == null) {
      return printerJob.defaultPage();
    } else {
      PageFormat pageFormat = new PageFormat();
      switch (homePrint.getPaperOrientation()) {
        case PORTRAIT :
          pageFormat.setOrientation(PageFormat.PORTRAIT);
          break;
        case LANDSCAPE :
          pageFormat.setOrientation(PageFormat.LANDSCAPE);
          break;
        case REVERSE_LANDSCAPE :
          pageFormat.setOrientation(PageFormat.REVERSE_LANDSCAPE);
          break;
      }
      Paper paper = new Paper();
      paper.setSize(homePrint.getPaperWidth(), homePrint.getPaperHeight());
      paper.setImageableArea(homePrint.getPaperLeftMargin(), homePrint.getPaperTopMargin(), 
          homePrint.getPaperWidth() - homePrint.getPaperLeftMargin() - homePrint.getPaperRightMargin(), 
          homePrint.getPaperHeight() - homePrint.getPaperTopMargin() - homePrint.getPaperBottomMargin());
      pageFormat.setPaper(paper);
      pageFormat = printerJob.validatePage(pageFormat);
      return pageFormat;
    }
  }
}
