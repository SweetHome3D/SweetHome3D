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
import java.awt.ComponentOrientation;
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
import java.util.Locale;

import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
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
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.HelpController;
import com.eteks.sweethome3d.viewcontroller.HelpView;

/**
 * A pane displaying Sweet Home 3D help.
 * @author Emmanuel Puybaret
 */
public class HelpPane extends JRootPane implements HelpView {
  private enum ActionType {SHOW_PREVIOUS, SHOW_NEXT, SEARCH, CLOSE}

  private final UserPreferences preferences;
  private JFrame                frame;
  private JLabel                searchLabel;
  private JTextField            searchTextField;
  private JEditorPane           helpEditorPane;
  
  public HelpPane(UserPreferences preferences, 
                  final HelpController controller) {
    this.preferences = preferences;
    createActions(preferences, controller);
    createComponents(preferences);
    setMnemonics(preferences);
    layoutComponents();
    addLanguageListener(preferences);
    if (controller != null) {
      addHyperlinkListener(controller);
      installKeyboardActions();
    }
    
    setPage(controller.getHelpPage());
    controller.addPropertyChangeListener(HelpController.Property.HELP_PAGE, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            setPage(controller.getHelpPage());
          }
        });
    controller.addPropertyChangeListener(HelpController.Property.BROWSER_PAGE, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            setBrowserPage(controller.getBrowserPage());
          }
        });
  }

  /** 
   * Creates actions bound to <code>controller</code>.
   */
  private void createActions(UserPreferences preferences, 
                             final HelpController controller) {
    ActionMap actions = getActionMap();    
    try {
      final ControllerAction showPreviousAction = new ControllerAction(
          preferences, HelpPane.class, ActionType.SHOW_PREVIOUS.name(), controller, "showPrevious");
      showPreviousAction.setEnabled(controller.isPreviousPageEnabled());
      controller.addPropertyChangeListener(HelpController.Property.PREVIOUS_PAGE_ENABLED, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              showPreviousAction.setEnabled(controller.isPreviousPageEnabled());
            }
          });
      actions.put(ActionType.SHOW_PREVIOUS, showPreviousAction);
      
      final ControllerAction showNextAction = new ControllerAction(
          preferences, HelpPane.class, ActionType.SHOW_NEXT.name(), controller, "showNext");
      showNextAction.setEnabled(controller.isNextPageEnabled());
      controller.addPropertyChangeListener(HelpController.Property.NEXT_PAGE_ENABLED, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              showNextAction.setEnabled(controller.isNextPageEnabled());
            }
          });
      actions.put(ActionType.SHOW_NEXT, showNextAction);
      
      actions.put(ActionType.SEARCH, new ResourceAction(preferences, HelpPane.class, ActionType.SEARCH.name()) {
          @Override
          public void actionPerformed(ActionEvent ev) {
            final Cursor previousCursor = getCursor();
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            try {
              String searchedText = searchTextField.getText().trim();
              if (searchedText.length() > 0) {
                controller.search(searchedText);
              }
            } finally {
              setCursor(previousCursor);
            }
          }
        });
    } catch (NoSuchMethodException ex) {
      throw new RuntimeException(ex);
    }
    actions.put(ActionType.CLOSE, new ResourceAction(
          preferences, HelpPane.class, ActionType.CLOSE.name(), true) {
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
      UserPreferences preferences = (UserPreferences)ev.getSource();
      if (helpPane == null) {
        preferences.removePropertyChangeListener(
            UserPreferences.Property.LANGUAGE, this);
      } else {
        // Update frame title and search label with new locale
        if (helpPane.frame != null) {
          helpPane.frame.setTitle(preferences.getLocalizedString(HelpPane.class, "helpFrame.title"));
          helpPane.frame.applyComponentOrientation(
              ComponentOrientation.getOrientation(Locale.getDefault()));
        }
        helpPane.searchLabel.setText(SwingTools.getLocalizedLabelText(preferences, HelpPane.class, "searchLabel.text"));
        helpPane.searchTextField.setText("");
        helpPane.setMnemonics(preferences);
      }
    }
  }
  
  /**
   * Creates the components displayed by this view.
  */
  private void createComponents(UserPreferences preferences) {
    this.searchLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, HelpPane.class, "searchLabel.text"));
    this.searchTextField = new JTextField(12);
    // Under Mac OS 10.5 use client properties to use search text field look and feel
    if (OperatingSystem.isMacOSXLeopardOrSuperior()) {
      this.searchTextField.putClientProperty("JTextField.variant", "search");
      this.searchTextField.putClientProperty("JTextField.Search.FindAction",
          getActionMap().get(ActionType.SEARCH));
    } 
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
  private void setMnemonics(UserPreferences preferences) {
    if (!OperatingSystem.isMacOSX()) {
      this.searchLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(HelpPane.class, "searchLabel.mnemonic")).getKeyCode());
      this.searchLabel.setLabelFor(this.searchTextField);
    }
  }
  
  /**
   * Layouts the components displayed by this view.
   */
  private void layoutComponents() {
    final JToolBar toolBar = new JToolBar();
    toolBar.setFloatable(false);
    ActionMap actions = getActionMap();    
    toolBar.add(actions.get(ActionType.SHOW_PREVIOUS));
    toolBar.add(actions.get(ActionType.SHOW_NEXT));
    updateToolBarButtonsStyle(toolBar);
    toolBar.addPropertyChangeListener("componentOrientation", 
        new PropertyChangeListener () {
          public void propertyChange(PropertyChangeEvent evt) {
            updateToolBarButtonsStyle(toolBar);
          }
        });
    toolBar.add(Box.createHorizontalStrut(5));
    
    toolBar.add(Box.createGlue());
    if (!OperatingSystem.isMacOSXLeopardOrSuperior()) {
      toolBar.add(this.searchLabel);
      toolBar.add(Box.createHorizontalStrut(2));
    }
    toolBar.add(this.searchTextField);
    this.searchTextField.setMaximumSize(this.searchTextField.getPreferredSize());
    // Ignore search button under Mac OS X 10.5 (it's included in the search field)
    if (!OperatingSystem.isMacOSXLeopardOrSuperior()) {
      toolBar.add(Box.createHorizontalStrut(2));
      toolBar.add(actions.get(ActionType.SEARCH));
    }
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
      // Update frame image and title 
      this.frame.setIconImage(new ImageIcon(HelpPane.class.getResource(
          this.preferences.getLocalizedString(HelpPane.class, "helpFrame.icon"))).getImage());
      this.frame.setTitle(this.preferences.getLocalizedString(HelpPane.class, "helpFrame.title"));
      this.frame.applyComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));
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
    frame.setSize(Math.min(2 * screenSize.width / 3, 800), screenSize.height * 4 / 5);
  }
  
  /**
   * Displays <code>url</code> in this pane.
   */
  private void setPage(URL url) {
    try {
      this.helpEditorPane.setPage(url);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Displays <code>url</code> in standard browser.
   */
  private void setBrowserPage(URL url) {
    try { 
      // Lookup the javax.jnlp.BasicService object 
      BasicService service = (BasicService)ServiceManager.lookup("javax.jnlp.BasicService"); 
      service.showDocument(url); 
    } catch (UnavailableServiceException ex) {
      // Too bad : service is unavailable 
    }
  }
}
