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
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;

import com.eteks.sweethome3d.model.TextureImage;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;

/**
 * Gathers some useful tools for Swing.
 * @author Emmanuel Puybaret
 */
public class SwingTools {
  // Borders for focused views
  private static Border unfocusedViewBorder;
  private static Border focusedViewBorder;

  private SwingTools() {
    // This class contains only tools
  }

  /**
   * Updates the border of <code>component</code> with an empty border
   * changed to a colored border when it will gain focus.
   * If the <code>component</code> component is the child of a <code>JViewPort</code>
   * instance this border will be installed on its scroll pane parent. 
   */
  public static void installFocusBorder(JComponent component) {
    if (unfocusedViewBorder == null) {
      Border unfocusedViewInteriorBorder = new AbstractBorder() {
          private Color  topLeftColor;
          private Color  botomRightColor;
          private Insets insets = new Insets(1, 1, 1, 1);
          
          {
            if (OperatingSystem.isMacOSX()) {
              this.topLeftColor = Color.GRAY;
              this.botomRightColor = Color.LIGHT_GRAY;
            } else {
              this.topLeftColor = UIManager.getColor("TextField.darkShadow");
              this.botomRightColor  = UIManager.getColor("TextField.shadow");
            }
          }
          
          public Insets getBorderInsets(Component c) {
            return this.insets;
          }
    
          public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Color previousColor = g.getColor();
            Rectangle rect = getInteriorRectangle(c, x, y, width, height);
            g.setColor(topLeftColor);
            g.drawLine(rect.x - 1, rect.y - 1, rect.x + rect.width, rect.y - 1);
            g.drawLine(rect.x - 1, rect.y - 1, rect.x - 1, rect.y  + rect.height);
            g.setColor(botomRightColor);
            g.drawLine(rect.x, rect.y  + rect.height, rect.x + rect.width, rect.y  + rect.height);
            g.drawLine(rect.x + rect.width, rect.y, rect.x + rect.width, rect.y  + rect.height); 
            g.setColor(previousColor);
          }
        };
      
      if (OperatingSystem.isMacOSXLeopardOrSuperior()) {
        unfocusedViewBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIManager.getColor("Panel.background"), 2),
            unfocusedViewInteriorBorder);
        focusedViewBorder = new AbstractBorder() {
            private Insets insets = new Insets(3, 3, 3, 3);
            
            public Insets getBorderInsets(Component c) {
              return this.insets;
            }
      
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
              Color previousColor = g.getColor();
              // Paint a gradient paint around component
              Rectangle rect = getInteriorRectangle(c, x, y, width, height);
              g.setColor(Color.GRAY);
              g.drawLine(rect.x - 1, rect.y - 1, rect.x + rect.width, rect.y - 1);
              g.drawLine(rect.x - 1, rect.y - 1, rect.x - 1, rect.y  + rect.height);
              g.setColor(Color.LIGHT_GRAY);
              g.drawLine(rect.x, rect.y  + rect.height, rect.x + rect.width, rect.y  + rect.height);
              g.drawLine(rect.x + rect.width, rect.y, rect.x + rect.width, rect.y  + rect.height);
              Color focusColor = UIManager.getColor("Focus.color");
              int   transparencyOutline = 128;
              int   transparencyInline  = 180;
              if (focusColor == null) {
                focusColor = UIManager.getColor("textHighlight");
                transparencyOutline = 128;
                transparencyInline = 255;
              }
              g.setColor(new Color(focusColor.getRed(), focusColor.getGreen(), focusColor.getBlue(), transparencyOutline));
              g.drawRoundRect(rect.x - 3, rect.y - 3, rect.width + 5, rect.height + 5, 6, 6);
              g.drawRect(rect.x - 1, rect.y - 1, rect.width + 1, rect.height + 1);
              g.setColor(new Color(focusColor.getRed(), focusColor.getGreen(), focusColor.getBlue(), transparencyInline));
              g.drawRoundRect(rect.x - 2, rect.y - 2, rect.width + 3, rect.height + 3, 4, 4);

              // Draw corners
              g.setColor(UIManager.getColor("Panel.background"));
              g.drawLine(rect.x - 3, rect.y - 3, rect.x - 2, rect.y - 3);
              g.drawLine(rect.x - 3, rect.y - 2, rect.x - 3, rect.y - 2);
              g.drawLine(rect.x + rect.width + 1, rect.y - 3, rect.x + rect.width + 2, rect.y - 3);
              g.drawLine(rect.x + rect.width + 2, rect.y - 2, rect.x + rect.width + 2, rect.y - 2);
              g.drawLine(rect.x - 3, rect.y + rect.height + 2, rect.x - 2, rect.y + rect.height + 2);
              g.drawLine(rect.x - 3, rect.y + rect.height + 1, rect.x - 3, rect.y + rect.height + 1);
              g.drawLine(rect.x + rect.width + 1, rect.y + rect.height + 2, rect.x + rect.width + 2, rect.y + rect.height + 2);
              g.drawLine(rect.x + rect.width + 2, rect.y + rect.height + 1, rect.x + rect.width + 2, rect.y + rect.height + 1);

              g.setColor(previousColor);
            }
          };
      } else {
        if (OperatingSystem.isMacOSX()) {
          unfocusedViewBorder = BorderFactory.createCompoundBorder(
              BorderFactory.createLineBorder(UIManager.getColor("Panel.background"), 1),
              unfocusedViewInteriorBorder);
        } else {
          unfocusedViewBorder = BorderFactory.createCompoundBorder(
              BorderFactory.createEmptyBorder(1, 1, 1, 1), 
              unfocusedViewInteriorBorder);
        }
        focusedViewBorder = BorderFactory.createLineBorder(UIManager.getColor("textHighlight"), 2);
      }
    }
    
    final JComponent feedbackComponent;
    if (component.getParent() instanceof JViewport
        && component.getParent().getParent() instanceof JScrollPane) {
      feedbackComponent = (JComponent)component.getParent().getParent(); 
    } else {
      feedbackComponent = component;
    }
    feedbackComponent.setBorder(unfocusedViewBorder);
    component.addFocusListener(new FocusListener() {
        public void focusLost(FocusEvent ev) {
          feedbackComponent.setBorder(unfocusedViewBorder);
        }
        
        public void focusGained(FocusEvent ev) {
          feedbackComponent.setBorder(focusedViewBorder);
        }
      });
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
                                      Object messageComponent,
                                      String title,
                                      final JComponent focusedComponent) {
    JOptionPane optionPane = new JOptionPane(messageComponent, 
        JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
    final JDialog dialog = optionPane.createDialog(SwingUtilities.getRootPane(parentComponent), title);
    if (focusedComponent != null) {
      // Add a listener that transfer focus to focusedComponent when dialog is shown
      dialog.addComponentListener(new ComponentAdapter() {
          @Override
          public void componentShown(ComponentEvent ev) {
            focusedComponent.requestFocusInWindow();
            dialog.removeComponentListener(this);
          }
        });
    }
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
    if (focusedComponent != null) {
      // Add a listener that transfer focus to focusedComponent when dialog is shown
      dialog.addComponentListener(new ComponentAdapter() {
          @Override
          public void componentShown(ComponentEvent ev) {
            focusedComponent.requestFocusInWindow();
            dialog.removeComponentListener(this);
          }
        });
    }
    dialog.setVisible(true);    
    dialog.dispose();
  }

  private static Map<TextureImage, BufferedImage> patternImages;
  
  /**
   * Returns the image matching a given pattern.
   */
  public static BufferedImage getPatternImage(TextureImage pattern,
                                              Color backgroundColor, 
                                              Color foregroundColor) {
    if (patternImages == null) {
      patternImages = new HashMap<TextureImage, BufferedImage>();
    }
    BufferedImage image = new BufferedImage(
        (int)pattern.getWidth(), (int)pattern.getHeight(), BufferedImage.TYPE_INT_RGB);
    Graphics2D imageGraphics = (Graphics2D)image.getGraphics();
    imageGraphics.setColor(backgroundColor);
    imageGraphics.fillRect(0, 0, image.getWidth(), image.getHeight());
    // Get pattern image from cache
    BufferedImage patternImage = patternImages.get(pattern); 
    if (patternImage == null) {
      try {
        InputStream imageInput = pattern.getImage().openStream();
        patternImage = ImageIO.read(imageInput);
        imageInput.close();
        patternImages.put(pattern, patternImage);
      } catch (IOException ex) {
        throw new IllegalArgumentException("Can't read pattern image " + pattern.getName());
      }
    }
    // Draw the pattern image with foreground color
    final int foregroundColorRgb = foregroundColor.getRGB() & 0xFFFFFF;
    imageGraphics.drawImage(Toolkit.getDefaultToolkit().createImage(
        new FilteredImageSource(patternImage.getSource(),
        new RGBImageFilter() {
          {
            this.canFilterIndexColorModel = true;
          }

          @Override
          public int filterRGB(int x, int y, int rgba) {
            // Always use foreground color and alpha
            return (rgba & 0xFF000000) | foregroundColorRgb;
          }
        })), 0, 0, null);
    imageGraphics.dispose();
    return image;
  }
  
  /**
   * Returns the border of a component where a user may drop objects.
   */
  public static Border getDropableComponentBorder() {
    Border border = null;
    if (OperatingSystem.isMacOSXLeopardOrSuperior()) {
      border = UIManager.getBorder("InsetBorder.aquaVariant");
    }
    if (border == null) {
      border = BorderFactory.createLoweredBevelBorder(); 
    }
    return border;
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
  
  /**
   * Returns a new panel with a border and the given <code>title</code>
   */
  public static JPanel createTitledPanel(String title) {
    JPanel titledPanel = new JPanel(new GridBagLayout());
    Border panelBorder = BorderFactory.createTitledBorder(title);
    // For systems different from Mac OS X 10.5, add an empty border 
    if (!OperatingSystem.isMacOSXLeopardOrSuperior()) {
      panelBorder = BorderFactory.createCompoundBorder(
          panelBorder, BorderFactory.createEmptyBorder(0, 2, 2, 2));
    }    
    titledPanel.setBorder(panelBorder);    
    return titledPanel;
  }
}
