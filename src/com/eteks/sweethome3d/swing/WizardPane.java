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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;

import com.eteks.sweethome3d.tools.OperatingSystem;

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
  private boolean          resizable;

  /**
   * Creates a wizard view controlled by <code>controller</code>.
   */
  public WizardPane(WizardController controller) {
    super();
    this.controller = controller;
    this.resource = ResourceBundle.getBundle(WizardPane.class.getName());
    this.title = resource.getString("wizard.title");
    
    setMessage(new JPanel(new BorderLayout(10, 0)));
    
    createOptionButtons();    
    setOptionType(DEFAULT_OPTION);
    String cancelOption = resource.getString("cancelOption");
    // Make backOptionButton appear at left of nextFinishOptionButton
    if (UIManager.getBoolean("OptionPane.isYesLast")
        || OperatingSystem.isMacOSX()) {
      setOptions(new Object [] {cancelOption, this.nextFinishOptionButton, this.backOptionButton});      
    } else {
      setOptions(new Object [] {cancelOption, this.backOptionButton, this.nextFinishOptionButton});      
    }
    setInitialValue(this.nextFinishOptionButton);
  }

  private void createOptionButtons() {
    this.backOptionButton = new JButton(resource.getString("backOptionButton.text"));
    this.backOptionButton.setEnabled(false);
    if (!OperatingSystem.isMacOSX()) {
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
            controller.finish();
            setValue(nextFinishOptionButton);
            if (dialog != null) {
              dialog.setVisible(false);
            }
          } else {
            controller.goToNextStep();
          }
        }
      });
  }

  /**
   * Sets whether the back step button is <code>enabled</code> or not.
   */
  public void setBackStepEnabled(boolean enabled) {
    this.backOptionButton.setEnabled(enabled);
  }

  /**
   * Sets whether the next step button is <code>enabled</code> or not.
   */
  public void setNextStepEnabled(boolean enabled) {
    this.nextFinishOptionButton.setEnabled(enabled);
  }

  /**
   * Sets whether this wizard view is displaying the last step or not.
   */
  public void setLastStep(boolean lastStep) {
    this.lastStep = lastStep;
    this.nextFinishOptionButton.setText(resource.getString(lastStep 
        ? "finishOptionButton.text" 
        : "nextOptionButton.text"));
    if (!OperatingSystem.isMacOSX()) {
      this.nextFinishOptionButton.setMnemonic(KeyStroke.getKeyStroke(this.resource.getString(
          lastStep 
              ? "finishOptionButton.mnemonic" 
              : "nextOptionButton.mnemonic")).getKeyCode());
    }
  }
  
  /**
   * Sets the step view displayed by this wizard view.
   */
  public void setStepMessage(JComponent stepView) {
    JPanel messagePanel = (JPanel)getMessage();
    // Clean previous step message
    Component previousStepView = ((BorderLayout)messagePanel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
    if (previousStepView != null) {
      messagePanel.remove(previousStepView);
    }
    // Add new message
    if (stepView != null) {
      messagePanel.add(stepView, BorderLayout.CENTER);
    } 
    if (this.dialog != null && !this.resizable) {
      this.dialog.pack();
    }
  }

  /**
   * Sets the step icon displayed by this wizard view.
   */
  public void setStepIcon(URL stepIcon) {
    JPanel messagePanel = (JPanel)getMessage();
    Component previousStepIconLabel = ((BorderLayout)messagePanel.getLayout()).getLayoutComponent(BorderLayout.WEST);
    if (previousStepIconLabel != null) {
      // Clean previous icon label
      messagePanel.remove(previousStepIconLabel);
    }
    // Add new icon
    if (stepIcon != null) {
      JLabel iconLabel = new JLabel(new ImageIcon(stepIcon)) {
          @Override
          protected void paintComponent(Graphics g) {
            Graphics2D g2D = (Graphics2D)g;
             // Paint a blue gradient behind icon
            g2D.setPaint(new GradientPaint(0, 0, new Color(163, 168, 226), 
                                           0, getHeight(), new Color(80, 86, 158)));
            g.fillRect(0, 0, getWidth(), getHeight());
            super.paintComponent(g);
          }
        };
      // Use a bevel border 1 pixel wide
      iconLabel.setBorder(new BevelBorder(BevelBorder.LOWERED) {
          @Override
          public Insets getBorderInsets(Component c) {
            return new Insets(1, 1, 1, 1);
          }
          
          @Override
          protected void paintLoweredBevel(Component c, Graphics g, int x, int y,
                                           int width, int height)  {
            Color oldColor = g.getColor();
            g.translate(x, y);

            g.setColor(getShadowInnerColor(c));
            g.drawLine(0, 0, 0, height - 1);
            g.drawLine(0, 0, width - 1, 0);

            g.setColor(getHighlightInnerColor(c));
            g.drawLine(0, height - 1, width - 1, height - 1);
            g.drawLine(width - 1, 1, width - 1, height - 2);

            g.translate(-x, -y);
            g.setColor(oldColor);
          }
        });
      // We don't use JOptionPane icon to let icon background spread in all height 
      messagePanel.add(iconLabel, BorderLayout.LINE_START);
    } 
  }

  /**
   * Sets the title of this wizard view.
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Sets whether this wizard view is <code>resizable</code> or not.
   */
  public void setResizable(boolean resizable) {
    this.resizable = resizable;
    if (this.dialog != null) {
      this.dialog.setResizable(resizable);
    }
  }
  
  /**
   * Displays this wizard view in a modal dialog.
   */
  public void displayView(JComponent parent) {
    this.dialog = createDialog(SwingUtilities.getRootPane(parent), this.title);
    this.dialog.applyComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));    
    this.dialog.setResizable(this.resizable);
    // Pack again because resize decorations may have changed dialog preferred size
    this.dialog.pack();
    this.dialog.setMinimumSize(getSize());
    this.dialog.setVisible(true);
    this.dialog.dispose();
  }
}
