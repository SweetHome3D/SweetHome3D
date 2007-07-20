/*
 * HelpPane.java 20 juil. 07
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
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * A pane displaying Sweet Home 3D help.
 * @author Emmanuel Puybaret
 */
public class HelpPane extends JRootPane {
  public enum ActionType {SHOW_PREVIOUS, SHOW_NEXT}

  private ResourceBundle resource;
  private JFrame         frame;
  private JToolBar       toolBar;
  private JEditorPane    helpEditorPane;
  
  public HelpPane(HelpController controller) {
    this.resource = ResourceBundle.getBundle(HelpPane.class.getName());
    createActions(controller);
    createComponents(controller);
    layoutComponents();
  }

  /** 
   * Creates actions bound to <code>controller</code>.
   */
  private void createActions(final HelpController controller) {
    ActionMap actions = getActionMap();    
    actions.put(ActionType.SHOW_PREVIOUS, new ResourceAction(
            this.resource, ActionType.SHOW_PREVIOUS.toString()) {
        @Override
        public void actionPerformed(ActionEvent ev) {
          controller.showPrevious();
        }
      });
    actions.put(ActionType.SHOW_NEXT, new ResourceAction(
            this.resource, ActionType.SHOW_NEXT.toString()) {
        @Override
        public void actionPerformed(ActionEvent ev) {
          controller.showNext();
        }
      });
  }

  /**
   * Creates the components diaplayed by this view.
   */
  private void createComponents(final HelpController controller) {
    this.toolBar = new JToolBar();
    this.toolBar.setFloatable(false);
    ActionMap actions = getActionMap();    
    this.toolBar.add(actions.get(ActionType.SHOW_PREVIOUS));
    this.toolBar.add(actions.get(ActionType.SHOW_NEXT));
    
    // Remove focusable property on buttons
    for (int i = 0, n = toolBar.getComponentCount(); i < n; i++) {
      toolBar.getComponentAtIndex(i).setFocusable(false);      
    }
    
    this.helpEditorPane = new JEditorPane();
    this.helpEditorPane.setBorder(null);
    this.helpEditorPane.setEditable(false);
    this.helpEditorPane.setContentType("text/html");
    this.helpEditorPane.putClientProperty(JEditorPane.W3C_LENGTH_UNITS, Boolean.TRUE);
    this.helpEditorPane.addHyperlinkListener(new HyperlinkListener() {
        public void hyperlinkUpdate(HyperlinkEvent ev) {
          if (ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            controller.showPage(ev.getURL());
          }
        }
      });
    
    // As no menu is created for this pane, change its input map to ensure accelerators work
    InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
    inputMap.put((KeyStroke)actions.get(ActionType.SHOW_PREVIOUS).getValue(Action.ACCELERATOR_KEY), 
        ActionType.SHOW_PREVIOUS);
    inputMap.put((KeyStroke)actions.get(ActionType.SHOW_NEXT).getValue(Action.ACCELERATOR_KEY), 
        ActionType.SHOW_NEXT);
  }

  /**
   * Layouts the components diaplayed by this view.
   */
  private void layoutComponents() {
    getContentPane().add(this.toolBar, BorderLayout.NORTH);
    getContentPane().add(new JScrollPane(this.helpEditorPane), BorderLayout.CENTER);
  }

  /**
   * Displays this pane in a frame.
   */
  public void displayView() {
    if (this.frame == null) {
      this.frame = new JFrame() {
          {
            // Replace frame rootPane by help view
            setRootPane(HelpPane.this);
          }
        };
      // Update frame image ans title 
      this.frame.setIconImage(new ImageIcon(
          HelpPane.class.getResource("resources/helpFrameIcon.gif")).getImage());
      this.frame.setTitle(this.resource.getString("helpFrame.title"));
      // Compute frame size and location
      computeFrameBounds(this.frame);
      // Just hide help frame when user close it
      this.frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }
    
    // Show frame
    this.frame.setVisible(true);
    this.frame.setState(JFrame.NORMAL);
    this.frame.toFront();
  }
  
  /**
   * Computes <code>frame</code> size and location to fit into screen.
   */
  private void computeFrameBounds(JFrame frame) {
    frame.setLocationByPlatform(true);
    Dimension screenSize = getToolkit().getScreenSize();
    Insets screenInsets = getToolkit().getScreenInsets(getGraphicsConfiguration());
    screenSize.width -= screenInsets.left + screenInsets.right;
    screenSize.height -= screenInsets.top + screenInsets.bottom;
    frame.setSize(2 * screenSize.width / 3, screenSize.height * 4 / 5);
  }
  
  /**
   * Displays <code>url</code> in this pane.
   */
  public void setPage(URL url) {
    try {
      this.helpEditorPane.setPage(url);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Displays <code>url</code> in standard browser.
   */
  public void setBrowserPage(URL url) {
    try { 
      // Lookup the javax.jnlp.BasicService object 
      BasicService service = (BasicService)ServiceManager.lookup("javax.jnlp.BasicService"); 
      service.showDocument(url); 
    } catch (UnavailableServiceException ex) {
      // Too bad : service is unavailable 
    }
  }

  /**
   * Sets whether previous button should be enabled or not.
   */
  public void setPreviousEnabled(boolean enabled) {
    getActionMap().get(ActionType.SHOW_PREVIOUS).setEnabled(enabled);
  }

  /**
   * Sets whether next button should be enabled or not.
   */
  public void setNextEnabled(boolean enabled) {
    getActionMap().get(ActionType.SHOW_NEXT).setEnabled(enabled);
  }
}
