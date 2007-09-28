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
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.DefaultFocusTraversalPolicy;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ResourceBundle;

import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import com.eteks.sweethome3d.model.UserPreferences;

/**
 * A pane displaying Sweet Home 3D help.
 * @author Emmanuel Puybaret
 */
public class HelpPane extends JRootPane {
  private enum ActionType {SHOW_PREVIOUS, SHOW_NEXT, SEARCH, CLOSE}

  private JFrame      frame;
  private JLabel      searchLabel;
  private JTextField  searchTextField;
  private JEditorPane helpEditorPane;
  
  public HelpPane(UserPreferences preferences, HelpController controller) {
    createActions(controller);
    createComponents();
    setMnemonics();
    layoutComponents();
    addLanguageListener(preferences);
    if (controller != null) {
      addHyperlinkListener(controller);
      installKeyboardActions();
    }
  }

  /** 
   * Creates actions bound to <code>controller</code>.
   */
  private void createActions(final HelpController controller) {
    ResourceBundle resource = ResourceBundle.getBundle(HelpPane.class.getName());    
    ActionMap actions = getActionMap();    
    try {
      actions.put(ActionType.SHOW_PREVIOUS, new ControllerAction(
          resource, ActionType.SHOW_PREVIOUS.toString(), controller, "showPrevious"));
      actions.put(ActionType.SHOW_NEXT, new ControllerAction(
          resource, ActionType.SHOW_NEXT.toString(), controller, "showNext"));
      actions.put(ActionType.SEARCH, new ResourceAction(resource, ActionType.SEARCH.toString()) {
          @Override
          public void actionPerformed(ActionEvent ev) {
            final Cursor previousCursor = getCursor();
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            try {
              controller.search(searchTextField.getText());
            } finally {
              setCursor(previousCursor);
            }
          }
        });
    } catch (NoSuchMethodException ex) {
      throw new RuntimeException(ex);
    }
    actions.put(ActionType.CLOSE, new ResourceAction(
            resource, ActionType.CLOSE.toString(), true) {
        @Override
        public void actionPerformed(ActionEvent ev) {
          frame.setVisible(false);
        }
      });
  }

  /**
   * Adds a property change listener to <code>preferences</code> to update
   * actions when preferred language changes.
   */
  private void addLanguageListener(UserPreferences preferences) {
    preferences.addPropertyChangeListener(UserPreferences.Property.LANGUAGE, 
        new LanguageChangeListener(this));
  }

  /**
   * Preferences property listener bound to this component with a weak reference to avoid
   * strong link between preferences and this component.  
   */
  private static class LanguageChangeListener implements PropertyChangeListener {
    private WeakReference<HelpPane> helpPane;

    public LanguageChangeListener(HelpPane helpPane) {
      this.helpPane = new WeakReference<HelpPane>(helpPane);
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
      // If help pane was garbage collected, remove this listener from preferences
      HelpPane helpPane = this.helpPane.get();
      if (helpPane == null) {
        ((UserPreferences)ev.getSource()).removePropertyChangeListener(
            UserPreferences.Property.LANGUAGE, this);
      } else {
        // Update actions from current default locale
        ResourceBundle resource = ResourceBundle.getBundle(HelpPane.class.getName());
        ActionMap actions = helpPane.getActionMap();    
        for (ActionType actionType : ActionType.values()) {
          ((ResourceAction)actions.get(actionType)).setResource(resource);
        }
        // Update frame title and search label
        if (helpPane.frame != null) {
          helpPane.frame.setTitle(resource.getString("helpFrame.title"));
          helpPane.searchLabel.setText(resource.getString("searchLabel.text"));
          helpPane.searchTextField.setText("");
          helpPane.setMnemonics();
        }
      }
    }
  }
  
  /**
   * Creates the components diaplayed by this view.
   */
  private void createComponents() {
    ResourceBundle resource = ResourceBundle.getBundle(HelpPane.class.getName());
    this.searchLabel = new JLabel(resource.getString("searchLabel.text"));
    this.searchTextField = new JTextField(12);
    this.searchTextField.addActionListener(getActionMap().get(ActionType.SEARCH));
    // Enable search only if search text field isn't empty
    this.searchTextField.getDocument().addDocumentListener(new DocumentListener() {
        public void changedUpdate(DocumentEvent ev) {
          getActionMap().get(ActionType.SEARCH).setEnabled(searchTextField.getText().trim().length() > 0);
        }
    
        public void insertUpdate(DocumentEvent ev) {
          changedUpdate(ev);
        }
    
        public void removeUpdate(DocumentEvent ev) {
          changedUpdate(ev);
        }
      });
    
    this.helpEditorPane = new JEditorPane();
    this.helpEditorPane.setBorder(null);
    this.helpEditorPane.setEditable(false);
    this.helpEditorPane.setContentType("text/html");
    this.helpEditorPane.putClientProperty(JEditorPane.W3C_LENGTH_UNITS, Boolean.TRUE);
    
    setFocusTraversalPolicy(new DefaultFocusTraversalPolicy() {
        @Override
        public Component getDefaultComponent(Container container) {
          return helpEditorPane;
        }
      });
  }

  /**
   * Sets components mnemonics and label / component associations.
   */
  private void setMnemonics() {
    if (!System.getProperty("os.name").startsWith("Mac OS X")) {
      ResourceBundle resource = ResourceBundle.getBundle(HelpPane.class.getName());
      this.searchLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(resource.getString("searchLabel.mnemonic")).getKeyCode());
      this.searchLabel.setLabelFor(this.searchTextField);
    }
  }
  
  /**
   * Layouts the components diaplayed by this view.
   */
  private void layoutComponents() {
    JToolBar toolBar = new JToolBar();
    toolBar.setFloatable(false);
    ActionMap actions = getActionMap();    
    toolBar.add(actions.get(ActionType.SHOW_PREVIOUS));
    toolBar.add(actions.get(ActionType.SHOW_NEXT));
    toolBar.add(Box.createHorizontalStrut(5));
    
    toolBar.add(Box.createGlue());
    toolBar.add(this.searchLabel);
    toolBar.add(Box.createHorizontalStrut(2));
    toolBar.add(this.searchTextField);
    this.searchTextField.setMaximumSize(this.searchTextField.getPreferredSize());
    toolBar.add(Box.createHorizontalStrut(2));
    toolBar.add(actions.get(ActionType.SEARCH));

    // Remove focusable property on buttons
    for (int i = 0, n = toolBar.getComponentCount(); i < n; i++) {      
      Component component = toolBar.getComponentAtIndex(i);
      if (component instanceof JButton) {
        component.setFocusable(false);
      }
    }
    
    getContentPane().add(toolBar, BorderLayout.NORTH);
    getContentPane().add(new JScrollPane(this.helpEditorPane), BorderLayout.CENTER);
  }

  /**
   * Adds an hyperlink listener on the editor pane displayed by this pane.
   */
  private void addHyperlinkListener(final HelpController controller) {
    this.helpEditorPane.addHyperlinkListener(new HyperlinkListener() {
        public void hyperlinkUpdate(HyperlinkEvent ev) {
          if (ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            controller.showPage(ev.getURL());
          }
        }
      });
  }

  /**
   * Installs keys bound to actions. 
   */
  private void installKeyboardActions() {
    ActionMap actions = getActionMap();      
    // As no menu is created for this pane, change its input map to ensure accelerators work
    InputMap inputMap = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    inputMap.put((KeyStroke)actions.get(ActionType.SHOW_PREVIOUS).getValue(Action.ACCELERATOR_KEY), 
        ActionType.SHOW_PREVIOUS);
    inputMap.put((KeyStroke)actions.get(ActionType.SHOW_NEXT).getValue(Action.ACCELERATOR_KEY), 
        ActionType.SHOW_NEXT);
    inputMap.put((KeyStroke)actions.get(ActionType.CLOSE).getValue(Action.ACCELERATOR_KEY), 
        ActionType.CLOSE);
    inputMap.put((KeyStroke)actions.get(ActionType.SEARCH).getValue(Action.ACCELERATOR_KEY), 
        ActionType.SEARCH);
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
      this.frame.setTitle(ResourceBundle.getBundle(HelpPane.class.getName()).
          getString("helpFrame.title"));
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
