/*
 * WizardPane.java 7 juin 07
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
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

/**
 * Wizard pane. 
 * @author Emmanuel Puybaret
 */
public class WizardPane extends JOptionPane {
  private WizardController controller;
  private ResourceBundle   resource;
  private JButton          backOptionButton;
  private JButton          nextFinishOptionButton;
  private boolean          lastStep;
  private String           title;
  private JDialog          dialog;

  public WizardPane(WizardController controller) {
    super();
    this.controller = controller;
    this.resource = ResourceBundle.getBundle(WizardPane.class.getName());
    this.title = resource.getString("wizard.title");
    
    createOptionButtons();    
    setOptionType(DEFAULT_OPTION);
    String cancelOption = resource.getString("cancelOption");
    // Make backOptionButton appear at left of nextFinishOptionButton
    if (UIManager.getBoolean("OptionPane.isYesLast")
        || System.getProperty("os.name").startsWith("Mac OS X")) {
      setOptions(new Object [] {cancelOption, this.nextFinishOptionButton, this.backOptionButton});      
    } else {
      setOptions(new Object [] {cancelOption, this.backOptionButton, this.nextFinishOptionButton});      
    }
  }

  private void createOptionButtons() {
    this.backOptionButton = new JButton(resource.getString("backOptionButton.text"));
    this.backOptionButton.setEnabled(false);
    if (!System.getProperty("os.name").startsWith("Mac OS X")) {
      this.backOptionButton.setMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("backOptionButton.mnemonic")).getKeyCode());
    }

    this.nextFinishOptionButton = new JButton();
    this.nextFinishOptionButton.setEnabled(false);
    // Update nextFinishButton text and mnemonic
    setLastStep(false);
    
    // Add action listeners
    this.backOptionButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          controller.goBackToPreviousStep();
        }
      });
    this.nextFinishOptionButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          if (lastStep) {
            setValue(nextFinishOptionButton);
            if (dialog != null) {
              dialog.setVisible(false);
            }
            controller.finish();
          } else {
            controller.goToNextStep();
          }
        }
      });
  }

  public void setBackStepEnabled(boolean enabled) {
    this.backOptionButton.setEnabled(enabled);
  }

  public void setNextStepEnabled(boolean enabled) {
    this.nextFinishOptionButton.setEnabled(enabled);
  }

  public void setLastStep(boolean lastStep) {
    this.lastStep = lastStep;
    this.nextFinishOptionButton.setText(resource.getString(lastStep 
        ? "finishOptionButton.text" 
        : "nextOptionButton.text"));
    if (!System.getProperty("os.name").startsWith("Mac OS X")) {
      this.nextFinishOptionButton.setMnemonic(KeyStroke.getKeyStroke(this.resource.getString(
          lastStep 
              ? "finishOptionButton.mnemonic" 
              : "nextOptionButton.mnemonic")).getKeyCode());
    }
  }
  
  public void setStepMessage(JComponent stepView) {
    setMessage(stepView);
    if (this.dialog != null) {
      this.dialog.pack();
    }
  }

  public void setStepIcon(URL stepIcon) {
    setIcon(stepIcon == null 
              ? null 
              : new ImageIcon(stepIcon));
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void displayView() {
    Component parent = null;
    for (Frame frame : Frame.getFrames()) {
      if (frame.isActive()) {
        parent = frame;
        break;
      }
    }
    
    this.dialog = createDialog(parent, this.title);
    this.dialog.setVisible(true);
    this.dialog.dispose();
  }
}
