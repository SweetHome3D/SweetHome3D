/*
 * PrintPreviewPanel.java 27 aout 07
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

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.AbstractBorder;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.HomeController;
import com.eteks.sweethome3d.viewcontroller.PrintPreviewController;
import com.eteks.sweethome3d.viewcontroller.View;

/**
 * Home print preview editing panel.
 * @author Emmanuel Puybaret
 */
public class PrintPreviewPanel extends JPanel implements DialogView {
  private enum ActionType {SHOW_PREVIOUS_PAGE, SHOW_NEXT_PAGE}

  private final UserPreferences  preferences;
  private JToolBar               toolBar;
  private HomePrintableComponent printableComponent;
  private JLabel                 pageLabel;

  /**
   * Creates a panel that displays print preview.
   * @param home home previewed by this panel
   * @param preferences the user preferences from which localized data is retrieved
   * @param homeController the controller of <code>home</code>
   * @param printPreviewController the controller of this panel
   */
  public PrintPreviewPanel(Home home,
                           UserPreferences preferences, 
                           HomeController homeController,
                           PrintPreviewController printPreviewController) {
    super(new ProportionalLayout());
    this.preferences = preferences;
    createActions(preferences);
    installKeyboardActions();
    createComponents(home, homeController);
    layoutComponents();
    updateComponents();
  }

  /**
   * Creates actions.  
   */
  private void createActions(UserPreferences preferences) {
    // Show previous page action
    Action showPreviousPageAction = new ResourceAction(
          preferences, PrintPreviewPanel.class, ActionType.SHOW_PREVIOUS_PAGE.name()) {
        @Override
        public void actionPerformed(ActionEvent e) {
          printableComponent.setPage(printableComponent.getPage() - 1);
          updateComponents();
        }
      };
    // Show next page action
    Action showNextPageAction = new ResourceAction(
          preferences, PrintPreviewPanel.class, ActionType.SHOW_NEXT_PAGE.name()) {
        @Override
        public void actionPerformed(ActionEvent e) {
          printableComponent.setPage(printableComponent.getPage() + 1);
          updateComponents();
        }
      };
    ActionMap actionMap = getActionMap();
    actionMap.put(ActionType.SHOW_PREVIOUS_PAGE, showPreviousPageAction);
    actionMap.put(ActionType.SHOW_NEXT_PAGE, showNextPageAction);
  }

  /**
   * Installs keys bound to actions. 
   */
  private void installKeyboardActions() {
    InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
    inputMap.put(KeyStroke.getKeyStroke("LEFT"), ActionType.SHOW_PREVIOUS_PAGE);
    inputMap.put(KeyStroke.getKeyStroke("UP"), ActionType.SHOW_PREVIOUS_PAGE);
    inputMap.put(KeyStroke.getKeyStroke("PAGE_UP"), ActionType.SHOW_PREVIOUS_PAGE);
    inputMap.put(KeyStroke.getKeyStroke("RIGHT"), ActionType.SHOW_NEXT_PAGE);
    inputMap.put(KeyStroke.getKeyStroke("DOWN"), ActionType.SHOW_NEXT_PAGE);
    inputMap.put(KeyStroke.getKeyStroke("PAGE_DOWN"), ActionType.SHOW_NEXT_PAGE);
  }

  /**
   * Creates and initializes components.
   */
  private void createComponents(Home home, HomeController homeController) {
    this.printableComponent = new HomePrintableComponent(home, homeController, getFont());
    this.printableComponent.setBorder(BorderFactory.createCompoundBorder(
        new AbstractBorder() {
          @Override
          public Insets getBorderInsets(Component c) {
            return new Insets(0, 0, 5, 5);
          }

          @Override
          public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2D = (Graphics2D)g;
            Color oldColor = g2D.getColor();
            // Fill left and right border with a gradient
            for (int i = 0; i < 5; i++) {
              g2D.setColor(new Color(128, 128, 128, 200 - i * 45));
              g2D.drawLine(x + width - 5 + i, y + i, x + width - 5 + i, y + height - 5 + i);
              g2D.drawLine(x + i, y + height - 5 + i, x + width - 5 + i - 1, y + height - 5 + i);
            }
            g2D.setColor(oldColor);
          }
        },
        BorderFactory.createLineBorder(Color.BLACK)));
    
    this.pageLabel = new JLabel();
    
    this.toolBar = new JToolBar();
    this.toolBar.setFloatable(false);
    ActionMap actions = getActionMap();    
    this.toolBar.add(actions.get(ActionType.SHOW_PREVIOUS_PAGE));
    this.toolBar.add(actions.get(ActionType.SHOW_NEXT_PAGE));
    updateToolBarButtonsStyle(this.toolBar);
    this.toolBar.addPropertyChangeListener("componentOrientation", 
        new PropertyChangeListener () {
          public void propertyChange(PropertyChangeEvent evt) {
            updateToolBarButtonsStyle(toolBar);
          }
        });
    
    this.toolBar.add(Box.createHorizontalStrut(20));
    this.toolBar.add(this.pageLabel);
    
    // Remove focusable property on buttons
    for (int i = 0, n = toolBar.getComponentCount(); i < n; i++) {
      toolBar.getComponentAtIndex(i).setFocusable(false);      
    }
  }
  
  /**
   * Under Mac OS X 10.5 use segmented buttons with properties 
   * depending on toolbar orientation.
   */
  private void updateToolBarButtonsStyle(JToolBar toolBar) {
    // Use segmented buttons under Mac OS X 10.5
    if (OperatingSystem.isMacOSXLeopardOrSuperior()) {
      // Retrieve component orientation because Mac OS X 10.5 miserably doesn't it take into account 
      ComponentOrientation orientation = toolBar.getComponentOrientation();
      JComponent previousButton = (JComponent)toolBar.getComponentAtIndex(0);
      previousButton.putClientProperty("JButton.buttonType", "segmentedTextured");
      previousButton.putClientProperty("JButton.segmentPosition", 
          orientation == ComponentOrientation.LEFT_TO_RIGHT 
            ? "first"
            : "last");
      JComponent nextButton = (JComponent)toolBar.getComponentAtIndex(1);
      nextButton.putClientProperty("JButton.buttonType", "segmentedTextured");
      nextButton.putClientProperty("JButton.segmentPosition", 
          orientation == ComponentOrientation.LEFT_TO_RIGHT 
            ? "last"
            : "first");
    }
  }
    
  /**
   * Layouts panel components in panel with their labels. 
   */
  private void layoutComponents() {
    // Add toolbar at top in a flow layout panel to make it centered
    JPanel panel = new JPanel();
    panel.add(this.toolBar);
    add(panel, ProportionalLayout.Constraints.TOP);
    // Add printable component at bottom of proportional layout panel
    add(this.printableComponent, ProportionalLayout.Constraints.BOTTOM);
  }

  /**
   * Updates components.
   */
  private void updateComponents() {
    ActionMap actions = getActionMap();    
    actions.get(ActionType.SHOW_PREVIOUS_PAGE).setEnabled(this.printableComponent.getPage() > 0);
    actions.get(ActionType.SHOW_NEXT_PAGE).setEnabled(
        this.printableComponent.getPage() < this.printableComponent.getPageCount() - 1);
    this.pageLabel.setText(preferences.getLocalizedString(
        PrintPreviewPanel.class, "pageLabel.text", 
        this.printableComponent.getPage() + 1, this.printableComponent.getPageCount()));
  }

  /**
   * Displays this panel in a modal resizable dialog box. 
   */
  public void displayView(View parentView) {
    String dialogTitle = preferences.getLocalizedString(
        PrintPreviewPanel.class, "printPreview.title");
    JOptionPane optionPane = new JOptionPane(this, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION); 
    JDialog dialog = optionPane.createDialog(SwingUtilities.getRootPane((JComponent)parentView), dialogTitle);
    dialog.applyComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));    
    dialog.setResizable(true);
    // Pack again because resize decorations may have changed dialog preferred size
    dialog.pack();
    dialog.setMinimumSize(dialog.getPreferredSize());
    dialog.setVisible(true);
    dialog.dispose();
  }
}
