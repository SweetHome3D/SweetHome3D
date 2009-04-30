/*
 * SwingTools.java 21 oct. 2008
 *
 * Copyright (c) 2008 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;

import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;

/**
 * Gathers some useful tools for Swing.
 * @author Emmanuel Puybaret
 */
public class SwingTools {
  private SwingTools() {
    // This class contains only tools
  }

  /**
   * Updates the Swing resource bundle in use from the current Locale. 
   */
  public static void updateSwingResourceLanguage() {
    // Read Swing localized properties because Swing doesn't update its internal strings automatically
    // when default Locale is updated (see bug http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4884480)
    String [] swingResources = {"com.sun.swing.internal.plaf.basic.resources.basic",
                                "com.sun.swing.internal.plaf.metal.resources.metal"};
    for (String swingResource : swingResources) {
      ResourceBundle resource;
      try {
        resource = ResourceBundle.getBundle(swingResource);
      } catch (MissingResourceException ex) {
        resource = ResourceBundle.getBundle(swingResource, Locale.ENGLISH);
      }
      // Update UIManager properties
      for (Enumeration<?> it = resource.getKeys(); it.hasMoreElements(); ) {
        String property = (String)it.nextElement();
        UIManager.put(property, resource.getString(property));
      }      
    };
  }
  
  /**
   * Returns a localized text for menus items and labels depending on the system.
   */
  public static String getLocalizedLabelText(UserPreferences preferences,
                                             Class<?> resourceClass,
                                             String   resourceKey, 
                                             Object ... resourceParameters) {
    String localizedString = preferences.getLocalizedString(resourceClass, resourceKey, resourceParameters);
    // Under Mac OS X, remove bracketed upper case roman letter used in oriental languages to indicate mnemonic 
    String language = Locale.getDefault().getLanguage();
    if (OperatingSystem.isMacOSX()
        && (language.equals(Locale.CHINESE.getLanguage())
            || language.equals(Locale.JAPANESE.getLanguage())
            || language.equals(Locale.KOREAN.getLanguage()))) {
      int openingBracketIndex = localizedString.indexOf('(');
      if (openingBracketIndex != -1) {
        int closingBracketIndex = localizedString.indexOf(')');
        if (openingBracketIndex == closingBracketIndex - 2) {
          char c = localizedString.charAt(openingBracketIndex + 1);
          if (c >= 'A' && c <= 'Z') {
            localizedString = localizedString.substring(0, openingBracketIndex) 
                + localizedString.substring(closingBracketIndex + 1);
          }
        }
      }
    }
    return localizedString;
  }
  
  /**
   * Adds focus and mouse listeners to the given <code>textComponent</code> that will
   * select all its text when it gains focus by transfer.
   */
  public static void addAutoSelectionOnFocusGain(final JTextComponent textComponent) {
    // A focus and mouse listener able to select text field characters 
    // when it gains focus after a focus transfer
    class SelectionOnFocusManager extends MouseAdapter implements FocusListener {
      private boolean mousePressedInTextField = false;
      private int selectionStartBeforeFocusLost = -1;
      private int selectionEndBeforeFocusLost = -1;

      @Override
      public void mousePressed(MouseEvent ev) {
        this.mousePressedInTextField = true;
        this.selectionStartBeforeFocusLost = -1;
      }
      
      public void focusLost(FocusEvent ev) {
        if (ev.getOppositeComponent() == null
            || SwingUtilities.getWindowAncestor(ev.getOppositeComponent()) 
                != SwingUtilities.getWindowAncestor(textComponent)) {
          // Keep selection indices when focus on text field is transfered 
          // to an other window 
          this.selectionStartBeforeFocusLost = textComponent.getSelectionStart();
          this.selectionEndBeforeFocusLost = textComponent.getSelectionEnd();
        } else {
          this.selectionStartBeforeFocusLost = -1;
        }
      }

      public void focusGained(FocusEvent ev) {
        if (this.selectionStartBeforeFocusLost != -1) {
          EventQueue.invokeLater(new Runnable() {
              public void run() {
                // Reselect the same characters in text field
                textComponent.setSelectionStart(selectionStartBeforeFocusLost);
                textComponent.setSelectionEnd(selectionEndBeforeFocusLost);
              }
            });
        } else if (!this.mousePressedInTextField 
                   && ev.getOppositeComponent() != null
                   && SwingUtilities.getWindowAncestor(ev.getOppositeComponent()) 
                       == SwingUtilities.getWindowAncestor(textComponent)) {
          EventQueue.invokeLater(new Runnable() {
              public void run() {
                // Select all characters when text field got the focus because of a transfer
                textComponent.selectAll();
              }
            });
        }
        this.mousePressedInTextField = false;
      }
    };
    
    SelectionOnFocusManager selectionOnFocusManager = new SelectionOnFocusManager();
    textComponent.addFocusListener(selectionOnFocusManager);
    textComponent.addMouseListener(selectionOnFocusManager);
  }
  
  /**
   * Forces radio buttons to be deselected even if they belong to a button group. 
   */
  public static void deselectAllRadioButtons(JRadioButton ... radioButtons) {
    for (JRadioButton radioButton : radioButtons) {
      ButtonGroup group = ((JToggleButton.ToggleButtonModel)radioButton.getModel()).getGroup();
      group.remove(radioButton);
      radioButton.setSelected(false);
      group.add(radioButton);
    }    
  }
  
  /**
   * Displays <code>messageComponent</code> in a modal dialog box, giving focus to one of its components. 
   */
  public static int showConfirmDialog(JComponent parentComponent,
                                      JComponent messageComponent,
                                      String title,
                                      final JComponent focusedComponent) {
    JOptionPane optionPane = new JOptionPane(messageComponent, 
        JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
    final JDialog dialog = optionPane.createDialog(SwingUtilities.getRootPane(parentComponent), title);
    // Add a listener that transfer focus to focusedComponent when dialog is shown
    dialog.addComponentListener(new ComponentAdapter() {
        @Override
        public void componentShown(ComponentEvent ev) {
          focusedComponent.requestFocusInWindow();
          dialog.removeComponentListener(this);
        }
      });
    dialog.setVisible(true);
    
    dialog.dispose();
    Object value = optionPane.getValue();
    if (value instanceof Integer) {
      return (Integer)value;
    } else {
      return JOptionPane.CLOSED_OPTION;
    }
  }

  /**
   * Displays <code>messageComponent</code> in a modal dialog box, giving focus to one of its components. 
   */
  public static void showMessageDialog(JComponent parentComponent,
                                       JComponent messageComponent,
                                       String title,
                                       int messageType,
                                       final JComponent focusedComponent) {
    JOptionPane optionPane = new JOptionPane(messageComponent, 
        messageType, JOptionPane.DEFAULT_OPTION);
    final JDialog dialog = optionPane.createDialog(SwingUtilities.getRootPane(parentComponent), title);
    // Add a listener that transfer focus to focusedComponent when dialog is shown
    dialog.addComponentListener(new ComponentAdapter() {
        @Override
        public void componentShown(ComponentEvent ev) {
          focusedComponent.requestFocusInWindow();
          dialog.removeComponentListener(this);
        }
      });
    dialog.setVisible(true);    
    dialog.dispose();
  }

  /**
   * Returns the image matching a given pattern.
   */
  public static BufferedImage getPatternImage(UserPreferences.Pattern pattern,
                                              Color backgroundColor, 
                                              Color foregroundColor) {
    BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
    Graphics2D imageGraphics = (Graphics2D)image.getGraphics();
    switch (pattern) {
      case FOREGROUND :
        imageGraphics.setColor(foregroundColor);
        imageGraphics.fillRect(0, 0, 10, 10);
        break;
      case HATCH_UP :
        // Draw an upward diagonal line
        imageGraphics.setPaint(backgroundColor);
        imageGraphics.fillRect(0, 0, 10, 10);
        imageGraphics.setColor(foregroundColor);
        imageGraphics.drawLine(0, 9, 9, 0);
        break;
      case HATCH_DOWN :
        // Draw a downward diagonal line
        imageGraphics.setPaint(backgroundColor);
        imageGraphics.fillRect(0, 0, 10, 10);
        imageGraphics.setColor(foregroundColor);
        imageGraphics.drawLine(0, 0, 9, 9);
        break;
      case BACKGROUND :
      default :
        imageGraphics.setColor(backgroundColor);
        imageGraphics.fillRect(0, 0, 10, 10);
        break;        
    }
    imageGraphics.dispose();
    return image;
  }
  
  /**
   * Displays the image referenced by <code>imageUrl</code> in an AWT window 
   * disposed once an other AWT frame is created.
   * If the <code>imageUrl</code> is incorrect, nothing happens.
   */
  public static void showSplashScreenWindow(URL imageUrl) {
    try {
      final BufferedImage image = ImageIO.read(imageUrl);
      final Window splashScreenWindow = new Window(new Frame()) {
          @Override
          public void paint(Graphics g) {
            g.drawImage(image, 0, 0, this);
          }
        };
        
      splashScreenWindow.setSize(image.getWidth(), image.getHeight());
      splashScreenWindow.setLocationRelativeTo(null);
      splashScreenWindow.setVisible(true);
          
      Executors.newSingleThreadExecutor().execute(new Runnable() {
          public void run() {
            try {
              while (splashScreenWindow.isVisible()) {
                Thread.sleep(500);
                // If an other frame is created, dispose splash window
                  EventQueue.invokeLater(new Runnable() {
                    public void run() {
                      if (Frame.getFrames().length > 1) {
                        splashScreenWindow.dispose();
                      }
                    }
                  });
                }
              } catch (InterruptedException ex) {
                EventQueue.invokeLater(new Runnable() {
                  public void run() {
                    splashScreenWindow.dispose();
                  }
                });
              };
            }
          });
    } catch (IOException ex) {
      // Ignore splash screen
    }
  }
}
