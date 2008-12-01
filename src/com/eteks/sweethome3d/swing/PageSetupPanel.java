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
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.eteks.sweethome3d.model.HomePrint;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.swing.NullableSpinner.NullableSpinnerNumberModel;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.PageSetupController;
import com.eteks.sweethome3d.viewcontroller.View;

/**
 * Home page setup editing panel.
 * @author Emmanuel Puybaret
 */
public class PageSetupPanel extends JPanel implements DialogView {
  private static final int DEFAULT_SCALE = 10;
  
  private final PageSetupController controller;
  private ResourceBundle      resource;
  private PageFormat          pageFormat;
  private JButton             pageFormatButton;
  private JCheckBox           furniturePrintedCheckBox;
  private JCheckBox           planPrintedCheckBox;
  private JRadioButton        bestFitPlanScaleRadioButton;
  private JRadioButton        userPlanScaleRadioButton;
  private JSpinner            userPlanScaleSpinner;
  private JCheckBox           view3DPrintedCheckBox;
  private JLabel              headerFormatLabel;
  private JTextField          headerFormatTextField;
  private JLabel              footerFormatLabel;
  private JTextField          footerFormatTextField;
  private JLabel              variablesLabel;
  private JToolBar            variableButtonsToolBar;  

  /**
   * Creates a panel that displays page setup.
   * @param controller the controller of this panel
   */
  public PageSetupPanel(UserPreferences preferences, 
                        PageSetupController controller) {
    super(new GridBagLayout());
    this.controller = controller;
    this.resource = ResourceBundle.getBundle(
        PageSetupPanel.class.getName());
    createActions();
    createComponents(preferences, controller);
    setMnemonics();
    layoutComponents();
  }

  /**
   * Creates actions for variables.
   */
  private void createActions() {
    ActionMap actions = getActionMap();
    for (final HomePrintableComponent.Variable variable : 
                      HomePrintableComponent.Variable.values()) {
      actions.put(variable, 
          new ResourceAction(this.resource, "INSERT_" + variable.name()) {
            @Override
            public void actionPerformed(ActionEvent ev) {
              insertVariable(variable.getUserCode());
            }
          });
    }
  }

  /**
   * Inserts a code in the text field that has the focus and selects it.
   */
  private void insertVariable(String userCode) {
    Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
    if (focusOwner instanceof JTextField) {
      JTextField textField = (JTextField)focusOwner;
      textField.replaceSelection(userCode);
      int lastCharacter = textField.getCaretPosition();
      int firstCharacter = lastCharacter - userCode.length();
      textField.select(firstCharacter, lastCharacter);
    }
  }

  /**
   * Creates and initializes components.
   */
  private void createComponents(UserPreferences preferences, 
                                final PageSetupController controller) {
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
    ItemListener itemListener = new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          updateController(controller);
        }
      };
    this.furniturePrintedCheckBox.addItemListener(itemListener);
    this.planPrintedCheckBox = new JCheckBox(
        this.resource.getString("planPrintedCheckBox.text")); 
    this.planPrintedCheckBox.addItemListener(itemListener);

    // Create scale radio buttons and user's scale spinner
    this.bestFitPlanScaleRadioButton = new JRadioButton(
        this.resource.getString("bestFitPlanScaleRadioButton.text"));
    this.userPlanScaleRadioButton = new JRadioButton(
        this.resource.getString("userPlanScaleRadioButton.text"));
    ButtonGroup scaleButtonsGroup = new ButtonGroup();
    scaleButtonsGroup.add(this.bestFitPlanScaleRadioButton);
    scaleButtonsGroup.add(this.userPlanScaleRadioButton);
    final NullableSpinner.NullableSpinnerNumberModel userPlanScaleSpinnerModel = 
        new NullableSpinner.NullableSpinnerNumberModel(10, 1, 10000, 10);
    this.userPlanScaleSpinner = new AutoCommitSpinner(userPlanScaleSpinnerModel);
    userPlanScaleSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          updateController(controller);
        }
      });
    this.bestFitPlanScaleRadioButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          updateController(controller);
        }
      });
    this.userPlanScaleRadioButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          if (userPlanScaleRadioButton.isSelected() 
              && userPlanScaleSpinnerModel.getValue() == null) {
            userPlanScaleSpinnerModel.setValue(DEFAULT_SCALE);
          } else {
            updateController(controller);
          }
        }
      });    

    this.view3DPrintedCheckBox = new JCheckBox(
        this.resource.getString("view3DPrintedCheckBox.text")); 
    this.view3DPrintedCheckBox.addItemListener(itemListener);

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
          for (HomePrintableComponent.Variable field : HomePrintableComponent.Variable.values()) {
            actionMap.get(field).setEnabled(true);
          }
        }
  
        public void focusLost(FocusEvent ev) {
          ActionMap actionMap = getActionMap();
          for (HomePrintableComponent.Variable field : HomePrintableComponent.Variable.values()) {
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

    // Create variables buttons tool bar
    this.variablesLabel = new JLabel(this.resource.getString("variablesLabel.text"));
    this.variableButtonsToolBar = new JToolBar();
    this.variableButtonsToolBar.setFloatable(false);
    ActionMap actions = getActionMap();
    this.variableButtonsToolBar.add(actions.get(HomePrintableComponent.Variable.PAGE_NUMBER));
    this.variableButtonsToolBar.add(actions.get(HomePrintableComponent.Variable.PAGE_COUNT));
    this.variableButtonsToolBar.add(actions.get(HomePrintableComponent.Variable.PLAN_SCALE));
    this.variableButtonsToolBar.add(actions.get(HomePrintableComponent.Variable.DATE));
    this.variableButtonsToolBar.add(actions.get(HomePrintableComponent.Variable.TIME));
    this.variableButtonsToolBar.add(actions.get(HomePrintableComponent.Variable.HOME_PRESENTATION_NAME));
    this.variableButtonsToolBar.add(actions.get(HomePrintableComponent.Variable.HOME_NAME));
    for (int i = 0, n = this.variableButtonsToolBar.getComponentCount(); i < n; i++) {        
      JComponent component = (JComponent)this.variableButtonsToolBar.getComponentAtIndex(i); 
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
    final NullableSpinnerNumberModel userPlanScaleSpinnerModel = 
        (NullableSpinner.NullableSpinnerNumberModel)this.userPlanScaleSpinner.getModel();
    if (homePrint != null) {
      this.furniturePrintedCheckBox.setSelected(homePrint.isFurniturePrinted());
      this.planPrintedCheckBox.setSelected(homePrint.isPlanPrinted());
      this.bestFitPlanScaleRadioButton.setEnabled(homePrint.isPlanPrinted());      
      this.bestFitPlanScaleRadioButton.setSelected(homePrint.getPlanScale() == null);
      this.userPlanScaleRadioButton.setEnabled(homePrint.isPlanPrinted());      
      this.userPlanScaleRadioButton.setSelected(homePrint.getPlanScale() != null);      
      this.userPlanScaleSpinner.setEnabled(homePrint.isPlanPrinted() && homePrint.getPlanScale() != null);      
      this.view3DPrintedCheckBox.setSelected(homePrint.isView3DPrinted() && offscreenCanvas3DSupported);
      userPlanScaleSpinnerModel.setNullable(homePrint.getPlanScale() == null);
      userPlanScaleSpinnerModel.setValue(homePrint.getPlanScale() != null
          ? new Integer(Math.round(1 / homePrint.getPlanScale()))
          : null);
      String headerFormat = homePrint.getHeaderFormat();
      this.headerFormatTextField.setText(headerFormat != null ? headerFormat : "");
      String footerFormat = homePrint.getFooterFormat();
      this.footerFormatTextField.setText(footerFormat != null ? footerFormat : "");
    } else {
      this.furniturePrintedCheckBox.setSelected(true);
      this.planPrintedCheckBox.setSelected(true);
      this.bestFitPlanScaleRadioButton.setSelected(true);
      this.bestFitPlanScaleRadioButton.setEnabled(false);
      this.userPlanScaleRadioButton.setEnabled(true);
      this.userPlanScaleSpinner.setEnabled(false);      
      this.view3DPrintedCheckBox.setSelected(offscreenCanvas3DSupported);
      userPlanScaleSpinnerModel.setNullable(true);
      userPlanScaleSpinnerModel.setValue(null);
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
        this.userPlanScaleRadioButton.isSelected() && this.userPlanScaleSpinner.getValue() != null
            ? 1f / ((Number)this.userPlanScaleSpinner.getValue()).intValue()
            : null,
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
      this.bestFitPlanScaleRadioButton.setMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("bestFitPlanScaleRadioButton.mnemonic")).getKeyCode());
      this.userPlanScaleRadioButton.setMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("userPlanScaleRadioButton.mnemonic")).getKeyCode());
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
    int labelAlignment = OperatingSystem.isMacOSX() 
        ? GridBagConstraints.LINE_END
        : GridBagConstraints.LINE_START;
    // First row
    JPanel topPanel = new JPanel(new GridBagLayout());
    topPanel.add(this.pageFormatButton, new GridBagConstraints(
        0, 0, 2, 1, 0, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.NONE, new Insets(0, 0, 10, 0) , 0, 0));
    Insets lastComponentInsets = new Insets(0, 0, 5, 0);
    // Furniture component
    topPanel.add(this.furniturePrintedCheckBox, new GridBagConstraints(
        0, 1, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, lastComponentInsets , 0, 0));
    // Plan components
    topPanel.add(this.planPrintedCheckBox, new GridBagConstraints(
        0, 2, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 0, 2, 0), 0, 0));
    topPanel.add(this.bestFitPlanScaleRadioButton, new GridBagConstraints(
        0, 3, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 20, 2, 0), 0, 0));
    topPanel.add(this.userPlanScaleRadioButton, new GridBagConstraints(
        0, 4, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, new Insets(0, 20, 5, 0), 0, 0));
    topPanel.add(this.userPlanScaleSpinner, new GridBagConstraints(
        1, 4, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, lastComponentInsets, 0, 0));
    // 3D view component
    topPanel.add(this.view3DPrintedCheckBox, new GridBagConstraints(
        0, 5, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    
    add(topPanel, new GridBagConstraints(
        0, 0, 2, 1, 0, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 0), 0, 0));
    // Second row
    add(new JSeparator(), new GridBagConstraints(
        0, 1, 2, 1, 0, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), -10, 0));
    // Third row
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
    add(this.variablesLabel, new GridBagConstraints(
        0, 4, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
    add(this.variableButtonsToolBar, new GridBagConstraints(
        1, 4, 1, 1, 0, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
  }

  /**
   * Displays this panel in a modal dialog box. 
   */
  public void displayView(View parentView) {
    String dialogTitle = resource.getString("pageSetup.title");
    if (SwingTools.showConfirmDialog((JComponent)parentView, 
            this, dialogTitle, this.pageFormatButton) == JOptionPane.OK_OPTION
        && this.controller != null) {
          this.controller.modifyPageSetup();
    }
  }
}
