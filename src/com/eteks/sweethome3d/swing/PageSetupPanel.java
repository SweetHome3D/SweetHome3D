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

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.eteks.sweethome3d.model.HomePrint;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.PageSetupController;
import com.eteks.sweethome3d.viewcontroller.View;

/**
 * Home page setup editing panel.
 * @author Emmanuel Puybaret
 */
public class PageSetupPanel extends JPanel implements DialogView {
  private final PageSetupController controller;
  private ResourceBundle      resource;
  private PageFormat          pageFormat;
  private JButton             pageFormatButton;
  private JCheckBox           furniturePrintedCheckBox;
  private JCheckBox           planPrintedCheckBox;
  private JCheckBox           view3DPrintedCheckBox;
  private JLabel              headerFormatLabel;
  private JTextField          headerFormatTextField;
  private JLabel              footerFormatLabel;
  private JTextField          footerFormatTextField;
  private JLabel              dynamicFieldsLabel;
  private JToolBar            dynamicFieldButtonsToolBar;  

  /**
   * Creates a panel that displays page setup.
   * @param controller the controller of this panel
   */
  public PageSetupPanel(PageSetupController controller) {
    super(new GridBagLayout());
    this.controller = controller;
    this.resource = ResourceBundle.getBundle(
        PageSetupPanel.class.getName());
    createActions();
    createComponents(controller);
    setMnemonics();
    layoutComponents();
  }

  /**
   * Creates actions for dynamic fields.
   */
  private void createActions() {
    ActionMap actions = getActionMap();
    actions.put(HomePrintableComponent.DynamicField.PAGE_NUMBER, new ResourceAction(
        this.resource, "INSERT_" + HomePrintableComponent.DynamicField.PAGE_NUMBER.name()) {
        @Override
        public void actionPerformed(ActionEvent ev) {
          insertDynamicCode(HomePrintableComponent.DynamicField.PAGE_NUMBER.getUserCode());
        }
      });
    actions.put(HomePrintableComponent.DynamicField.PAGE_COUNT, new ResourceAction(
        this.resource, "INSERT_" + HomePrintableComponent.DynamicField.PAGE_COUNT.name()) {
        @Override
        public void actionPerformed(ActionEvent ev) {
          insertDynamicCode(HomePrintableComponent.DynamicField.PAGE_COUNT.getUserCode());
        }
      });
    actions.put(HomePrintableComponent.DynamicField.DATE, new ResourceAction(
        this.resource, "INSERT_" + HomePrintableComponent.DynamicField.DATE.name()) {
        @Override
        public void actionPerformed(ActionEvent ev) {
          insertDynamicCode(HomePrintableComponent.DynamicField.DATE.getUserCode());
        }
      });
    actions.put(HomePrintableComponent.DynamicField.TIME, new ResourceAction(
        this.resource, "INSERT_" + HomePrintableComponent.DynamicField.TIME.name()) {
        @Override
        public void actionPerformed(ActionEvent ev) {
          insertDynamicCode(HomePrintableComponent.DynamicField.TIME.getUserCode());
        }
      });
    actions.put(HomePrintableComponent.DynamicField.HOME_PRESENTATION_NAME, new ResourceAction(
        this.resource, "INSERT_" + HomePrintableComponent.DynamicField.HOME_PRESENTATION_NAME.name()) {
        @Override
        public void actionPerformed(ActionEvent ev) {
          insertDynamicCode(HomePrintableComponent.DynamicField.HOME_PRESENTATION_NAME.getUserCode());
        }
      });
    actions.put(HomePrintableComponent.DynamicField.HOME_NAME, new ResourceAction(
        this.resource, "INSERT_" + HomePrintableComponent.DynamicField.HOME_NAME.name()) {
        @Override
        public void actionPerformed(ActionEvent ev) {
          insertDynamicCode(HomePrintableComponent.DynamicField.HOME_NAME.getUserCode());
        }
      });
  }

  /**
   * Inserts a code in the text field that has the focus and selects it.
   */
  private void insertDynamicCode(String userCode) {
    Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
    if (focusOwner instanceof JTextField) {
      JTextField textField = (JTextField)focusOwner;
      textField.replaceSelection(userCode);
      int lastCharacter = textField.getCaretPosition();
      int firstCharacter = lastCharacter - userCode.length();
      textField.setSelectionStart(firstCharacter);
      textField.setSelectionEnd(lastCharacter);
    }
  }

  /**
   * Creates and initializes components.
   */
  private void createComponents(final PageSetupController controller) {
    final PropertyChangeListener printChangeListener = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent ev) {
        updateComponents(controller.getPrint());
      }
    };

    this.pageFormatButton = new JButton(this.resource.getString("pageFormatButton.text"));
    this.pageFormatButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          // Show the page setup dialog
          PrinterJob printerJob = PrinterJob.getPrinterJob();
          pageFormat = printerJob.pageDialog(pageFormat);
          updateController(controller);
        }
      });
    this.furniturePrintedCheckBox = new JCheckBox(
        this.resource.getString("furniturePrintedCheckBox.text"));
    ItemListener checkBoxListener = new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          updateController(controller);
        }
      };
    this.furniturePrintedCheckBox.addItemListener(checkBoxListener);
    this.planPrintedCheckBox = new JCheckBox(
        this.resource.getString("planPrintedCheckBox.text")); 
    this.planPrintedCheckBox.addItemListener(checkBoxListener);
    this.view3DPrintedCheckBox = new JCheckBox(
        this.resource.getString("view3DPrintedCheckBox.text")); 
    this.view3DPrintedCheckBox.addItemListener(checkBoxListener);

    this.headerFormatLabel = new JLabel(this.resource.getString("headerFormatLabel.text"));
    this.headerFormatTextField = new JTextField(20);
    if (!OperatingSystem.isMacOSX()) {
      SwingTools.addAutoSelectionOnFocusGain(this.headerFormatTextField);
    }
    DocumentListener documentListener = new DocumentListener() {
        public void changedUpdate(DocumentEvent ev) {
          controller.removePropertyChangeListener(PageSetupController.Property.PRINT, printChangeListener);
          updateController(controller);
          controller.addPropertyChangeListener(PageSetupController.Property.PRINT, printChangeListener);
        }
  
        public void insertUpdate(DocumentEvent ev) {
          changedUpdate(ev);
        }
  
        public void removeUpdate(DocumentEvent ev) {
          changedUpdate(ev);
        }
      };
    this.headerFormatTextField.getDocument().addDocumentListener(documentListener);
    FocusListener textFieldFocusListener = new FocusListener() {
        public void focusGained(FocusEvent ev) {
          ActionMap actionMap = getActionMap();
          for (HomePrintableComponent.DynamicField field : HomePrintableComponent.DynamicField.values()) {
            actionMap.get(field).setEnabled(true);
          }
        }
  
        public void focusLost(FocusEvent ev) {
          ActionMap actionMap = getActionMap();
          for (HomePrintableComponent.DynamicField field : HomePrintableComponent.DynamicField.values()) {
            actionMap.get(field).setEnabled(false);
          }
        }
      };
    this.headerFormatTextField.addFocusListener(textFieldFocusListener);
    
    this.footerFormatLabel = new JLabel(this.resource.getString("footerFormatLabel.text"));
    this.footerFormatTextField = new JTextField(20);
    if (!OperatingSystem.isMacOSX()) {
      SwingTools.addAutoSelectionOnFocusGain(this.footerFormatTextField);
    }
    this.footerFormatTextField.getDocument().addDocumentListener(documentListener);
    this.footerFormatTextField.addFocusListener(textFieldFocusListener);

    // Create dynamic fields buttons tool bar
    this.dynamicFieldsLabel = new JLabel(this.resource.getString("dynamicFieldsLabel.text"));
    this.dynamicFieldButtonsToolBar = new JToolBar();
    this.dynamicFieldButtonsToolBar.setFloatable(false);
    ActionMap actions = getActionMap();
    this.dynamicFieldButtonsToolBar.add(actions.get(HomePrintableComponent.DynamicField.PAGE_NUMBER));
    this.dynamicFieldButtonsToolBar.add(actions.get(HomePrintableComponent.DynamicField.PAGE_COUNT));
    this.dynamicFieldButtonsToolBar.add(actions.get(HomePrintableComponent.DynamicField.DATE));
    this.dynamicFieldButtonsToolBar.add(actions.get(HomePrintableComponent.DynamicField.TIME));
    this.dynamicFieldButtonsToolBar.add(actions.get(HomePrintableComponent.DynamicField.HOME_PRESENTATION_NAME));
    this.dynamicFieldButtonsToolBar.add(actions.get(HomePrintableComponent.DynamicField.HOME_NAME));
    for (int i = 0, n = this.dynamicFieldButtonsToolBar.getComponentCount(); i < n; i++) {        
      JComponent component = (JComponent)this.dynamicFieldButtonsToolBar.getComponentAtIndex(i); 
      // Remove focusable property on buttons
      component.setFocusable(false);
    }
    
    controller.addPropertyChangeListener(PageSetupController.Property.PRINT, printChangeListener);
    updateComponents(controller.getPrint());    
  }
  
  /**
   * Updates components from <code>homePrint</code> attributes.
   */
  private void updateComponents(HomePrint homePrint) {
    this.pageFormat = HomePrintableComponent.getPageFormat(homePrint);
    // Check if off screen image is supported 
    boolean offscreenCanvas3DSupported = Component3DManager.getInstance().isOffScreenImageSupported();
    if (homePrint != null) {
      this.furniturePrintedCheckBox.setSelected(homePrint.isFurniturePrinted());
      this.planPrintedCheckBox.setSelected(homePrint.isPlanPrinted());
      this.view3DPrintedCheckBox.setSelected(homePrint.isView3DPrinted() && offscreenCanvas3DSupported);
      String headerFormat = homePrint.getHeaderFormat();
      this.headerFormatTextField.setText(headerFormat != null ? headerFormat : "");
      String footerFormat = homePrint.getFooterFormat();
      this.footerFormatTextField.setText(footerFormat != null ? footerFormat : "");
    } else {
      this.furniturePrintedCheckBox.setSelected(true);
      this.planPrintedCheckBox.setSelected(true);
      this.view3DPrintedCheckBox.setSelected(offscreenCanvas3DSupported);
      this.headerFormatTextField.setText("");
      this.footerFormatTextField.setText("");
    }
    this.view3DPrintedCheckBox.setEnabled(offscreenCanvas3DSupported);
  }

  /**
   * Updates controller print attributes.
   */
  public void updateController(PageSetupController controller) {
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
    HomePrint homePrint = new HomePrint(paperOrientation, (float)paper.getWidth(), (float)paper.getHeight(),
        (float)paper.getImageableY(), (float)paper.getImageableX(),
        (float)(paper.getHeight() - paper.getImageableHeight() - paper.getImageableY()),
        (float)(paper.getWidth() - paper.getImageableWidth() - paper.getImageableX()),
        this.furniturePrintedCheckBox.isSelected(),
        this.planPrintedCheckBox.isSelected(),
        this.view3DPrintedCheckBox.isSelected(),
        this.headerFormatTextField.getText().trim(),
        this.footerFormatTextField.getText().trim());
    controller.setPrint(homePrint);
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
      this.headerFormatLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("headerFormatLabel.mnemonic")).getKeyCode());
      this.headerFormatLabel.setLabelFor(this.headerFormatTextField);
      this.footerFormatLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("footerFormatLabel.mnemonic")).getKeyCode());
      this.footerFormatLabel.setLabelFor(this.footerFormatTextField);
    }
  }
  
  /**
   * Layouts panel components in panel with their labels. 
   */
  private void layoutComponents() {
    // First row
    JPanel topPanel = new JPanel(new GridBagLayout());
    topPanel.add(this.pageFormatButton, new GridBagConstraints(
        0, 0, 2, 1, 0, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.NONE, new Insets(0, 0, 10, 0) , 0, 0));
    Insets lastComponentInsets = new Insets(0, 0, 5, 0);
    topPanel.add(this.furniturePrintedCheckBox, new GridBagConstraints(
        0, 1, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, lastComponentInsets , 0, 0));
    topPanel.add(this.planPrintedCheckBox, new GridBagConstraints(
        0, 2, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, lastComponentInsets , 0, 0));
    topPanel.add(this.view3DPrintedCheckBox, new GridBagConstraints(
        0, 3, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    add(topPanel, new GridBagConstraints(
        0, 0, 2, 1, 0, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 0), 0, 0));
    // Second row
    add(new JSeparator(), new GridBagConstraints(
        0, 1, 2, 1, 0, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), -10, 0));
    // Third row
    int labelAlignment = OperatingSystem.isMacOSX() 
        ? GridBagConstraints.LINE_END
        : GridBagConstraints.LINE_START;
    add(this.headerFormatLabel, new GridBagConstraints(
        0, 2, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
    add(this.headerFormatTextField, new GridBagConstraints(
        1, 2, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, lastComponentInsets, 0, 0));
    // Forth row
    add(this.footerFormatLabel, new GridBagConstraints(
        0, 3, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
    add(this.footerFormatTextField, new GridBagConstraints(
        1, 3, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, lastComponentInsets, 0, 0));
    // Last row
    add(this.dynamicFieldsLabel, new GridBagConstraints(
        0, 4, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
    add(this.dynamicFieldButtonsToolBar, new GridBagConstraints(
        1, 4, 1, 1, 0, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
  }

  /**
   * Displays this panel in a modal dialog box. 
   */
  public void displayView(View parentView) {
    String dialogTitle = resource.getString("pageSetup.title");
    if (SwingTools.showConfirmDialog((JComponent)parentView, 
            dialogTitle, this, this.pageFormatButton) == JOptionPane.OK_OPTION
        && this.controller != null) {
          this.controller.modifyPageSetup();
    }
  }
}
