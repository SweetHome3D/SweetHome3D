/*
 * ColorButton.java 29 mai 07
 *
 * Sweet Home 3D, Copyright (c) 2007 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.colorchooser.DefaultColorSelectionModel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.JTextComponent;

import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;

/**
 * Button displaying a color as an icon.
 */
public class ColorButton extends JButton {
  public static final String COLOR_PROPERTY = "color";
  public static final String COLOR_DIALOG_TITLE_PROPERTY = "colorDialogTitle";
  
  // Share color chooser between ColorButton instances to keep recent colors
  private static JColorChooser colorChooser;
  private static Locale        colorChooserLocale;
  
  private Integer color;
  private String  colorDialogTitle;

  /**
   * Creates a color button.
   */
  public ColorButton() {
    this(null);
  }
  
  /**
   * Creates a color button.
   */
  public ColorButton(final UserPreferences preferences) {
    JLabel colorLabel = new JLabel("Color");
    Dimension iconDimension = colorLabel.getPreferredSize();
    final int iconWidth = iconDimension.width;
    final int iconHeight = iconDimension.height;
    setIcon(new Icon() {
      public int getIconWidth() {
        return iconWidth;
      }

      public int getIconHeight() {
        return iconHeight;
      }

      public void paintIcon(Component c, Graphics g, int x, int y) {
        g.setColor(getForeground());
        g.drawRect(x + 2, y + 2, iconWidth - 5, iconHeight - 5);
        if (color != null) {
          g.setColor(new Color(color));
          g.fillRect(x + 3, y + 3, iconWidth - 6,
                  iconHeight - 6);
        }
      }
    });

    // Add a listener to update color
    addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        if (colorChooser == null
            || !Locale.getDefault().equals(colorChooserLocale)) {
          // Create color chooser instance each time default locale changed 
          colorChooser = createColorChooser(preferences);          
          colorChooserLocale = Locale.getDefault();
        }
        
        // Update edited color in furniture color chooser
        colorChooser.setColor(color != null 
            ? new Color(color)
            : getBackground());
        JDialog colorDialog = JColorChooser.createDialog(getParent(), 
            colorDialogTitle, true, colorChooser,
            new ActionListener () { 
              public void actionPerformed(ActionEvent e) {
                // Change button color when user click on ok button
                Integer color = colorChooser.getColor().getRGB();
                setColor(color);
                List<Integer> recentColors = new ArrayList<Integer>(preferences.getRecentColors());
                int colorIndex = recentColors.indexOf(color);
                if (colorIndex != 0) {
                  // Move color at the beginning of the list and ensure it doesn't contain more than 20 colors
                  if (colorIndex > 0) {
                    recentColors.remove(colorIndex);
                  } else if (recentColors.size() == RecentColorsPanel.MAX_COLORS) {
                    recentColors.remove(RecentColorsPanel.MAX_COLORS - 1);
                  }
                  recentColors.add(0, color);
                  preferences.setRecentColors(recentColors);     
                }
              }
            }, null);   
        if (preferences != null) {
          AbstractColorChooserPanel colorChooserPanel = colorChooser.getChooserPanels() [0];
          if (colorChooserPanel instanceof PalettesColorChooserPanel) {
            ((PalettesColorChooserPanel)colorChooserPanel).setInitialColor(colorChooser.getColor());
            colorChooser.getPreviewPanel().getParent().setVisible(!preferences.getRecentColors().isEmpty());
            colorDialog.pack();
          }
        }
        colorDialog.setVisible(true);
      }
    });
  }

  /**
   * Creates a new color chooser.
   */
  private JColorChooser createColorChooser(UserPreferences preferences) {
    final JColorChooser colorChooser;
    if (preferences != null) {
      // Replace preview title by recent
      UIManager.put("ColorChooser.previewText", 
          preferences.getLocalizedString(ColorButton.class, "recentPanel.title"));
      ColorSelectionModel colorSelectionModel = new DefaultColorSelectionModel();
      final JPanel previewPanel = new RecentColorsPanel(colorSelectionModel, preferences);          
      final PalettesColorChooserPanel palettesPanel = new PalettesColorChooserPanel(preferences);
      palettesPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
      colorChooser = new JColorChooser(colorSelectionModel) {
          public void updateUI() {
            super.updateUI();
            // Add customized color chooser panel in updateUI, because an outside call to setChooserPanels 
            // might be ignored when the color chooser dialog is created
            List<AbstractColorChooserPanel> chooserPanels = new ArrayList<AbstractColorChooserPanel>(
                Arrays.asList(getChooserPanels()));
            if (chooserPanels.get(0).getClass().getName().contains("DefaultSwatchChooserPanel")) {
              chooserPanels.remove(0);
            }
            chooserPanels.add(0, palettesPanel);
            setChooserPanels(chooserPanels.toArray(new AbstractColorChooserPanel [chooserPanels.size()]));
            setPreviewPanel(previewPanel);
            // Add auto selection to color chooser panels text fields
            addAutoSelectionOnTextFields(this);
          }
        };
    } else {
      colorChooser = new JColorChooser();
    }
    
    // Add Esc key management
    colorChooser.getActionMap().put("close", new AbstractAction() {
        public void actionPerformed(ActionEvent ev) {
          ((Window)SwingUtilities.getRoot(colorChooser)).dispose();
        }
      });
    colorChooser.getInputMap(JColorChooser.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "close");
    return colorChooser;
  }

  private void addAutoSelectionOnTextFields(JComponent component) {
    if (component instanceof JTextComponent) {
      SwingTools.addAutoSelectionOnFocusGain((JTextComponent)component);
    } else if (component instanceof JSpinner) {
      JComponent editor = ((JSpinner)component).getEditor();
      if (editor instanceof JSpinner.DefaultEditor) {
        SwingTools.addAutoSelectionOnFocusGain(((JSpinner.DefaultEditor)editor).getTextField());
      }
    }
    for (int i = 0, n = component.getComponentCount(); i < n; i++) {
      Component childComponent = component.getComponent(i);
      if (childComponent instanceof JComponent) {
        addAutoSelectionOnTextFields((JComponent)childComponent);
      }
    }
  }

  /**
   * Returns the color displayed by this button.
   * @return the RGB code of the color of this button or <code>null</code>.
   */
  public Integer getColor() {
    return this.color;
  }

  /**
   * Sets the color displayed by this button.
   * @param color RGB code of the color or <code>null</code>.
   */
  public void setColor(Integer color) {
    if (color != this.color
        || (color != null && !color.equals(this.color))) {
      Integer oldColor = this.color;
      this.color = color;
      firePropertyChange(COLOR_PROPERTY, oldColor, color);
      repaint();
    }
  }

  /**
   * Returns the title of color dialog displayed when this button is pressed.  
   */
  public String getColorDialogTitle() {
    return this.colorDialogTitle;
  }

  /**
   * Sets the title of color dialog displayed when this button is pressed.  
   */
  public void setColorDialogTitle(String colorDialogTitle) {
    if (colorDialogTitle != this.colorDialogTitle
        || (colorDialogTitle != null && !colorDialogTitle.equals(this.colorDialogTitle))) {
      String oldColorDialogTitle = this.colorDialogTitle;
      this.colorDialogTitle = colorDialogTitle;
      firePropertyChange(COLOR_DIALOG_TITLE_PROPERTY, oldColorDialogTitle, colorDialogTitle);
      repaint();
    }
  }

  /**
   * Color chooser panel showing different palettes.
   */
  private static class PalettesColorChooserPanel extends AbstractColorChooserPanel {
    private final UserPreferences preferences;
    private GrayColorChart        grayColorChart;
    private ColorChart            colorChart;
    private JComponent            colorComponent;
    private JFormattedTextField   rgbTextField;
    private PaletteComboBox       ralComboBox;
    private PaletteComboBox       creativeCommonsComboBox;
    private Color                 initialColor;
      
    public PalettesColorChooserPanel(UserPreferences preferences) {
      this.preferences = preferences;
      addAncestorListener(new AncestorListener() {
          private int initialDelay;
  
          public void ancestorAdded(AncestorEvent event) {
            // Set a shorter delay to display tool tips
            ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
            this.initialDelay = toolTipManager.getInitialDelay();
            toolTipManager.setInitialDelay(Math.min(this.initialDelay, 100));
          }
          
          public void ancestorRemoved(AncestorEvent event) {
            // Restore default delay
            ToolTipManager.sharedInstance().setInitialDelay(this.initialDelay);
          }
          
          public void ancestorMoved(AncestorEvent event) {
          }
        });
    }

    public void setInitialColor(Color referenceColor) {
      this.initialColor = referenceColor;
      if (this.colorComponent != null) {
        this.colorComponent.repaint();
      }
    }
    
    @Override
    public void updateChooser() {
      Color selectedColor = getColorFromModel();
      this.colorComponent.repaint();
      if (!selectedColor.equals(this.rgbTextField.getValue())) {
        this.rgbTextField.setValue(selectedColor);
      }
      this.ralComboBox.setSelectedItem(this.ralComboBox.getColorCode(selectedColor));
      this.creativeCommonsComboBox.setSelectedItem(this.creativeCommonsComboBox.getColorCode(selectedColor));
    }

    @Override
    protected void buildChooser() {
      removeAll();
      // Create components
      this.grayColorChart = new GrayColorChart();
      this.grayColorChart.setBorder(BorderFactory.createLineBorder(Color.GRAY));
      final Cursor pipetteCursor = SwingTools.createCustomCursor(
          OperatingSystem.isMacOSX()
              ? ColorButton.class.getResource("resources/cursors/pipette16x16-macosx.png")
              : ColorButton.class.getResource("resources/cursors/pipette16x16.png"),
          ColorButton.class.getResource("resources/cursors/pipette32x32.png"), 
          0, 1, "Pipette", Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
      this.grayColorChart.setCursor(pipetteCursor);
      ToolTipManager.sharedInstance().registerComponent(this.grayColorChart);
      MouseAdapter grayColorCharMouseListener = new MouseAdapter() {
          @Override
          public void mousePressed(MouseEvent ev) {
            getColorSelectionModel().setSelectedColor(grayColorChart.getColorAt(ev.getY()));
          }
         
          @Override
          public void mouseDragged(MouseEvent ev) {
            mousePressed(ev);
          }
        };
      this.grayColorChart.addMouseListener(grayColorCharMouseListener);
      this.grayColorChart.addMouseMotionListener(grayColorCharMouseListener);

      this.colorChart = new ColorChart();
      this.colorChart.setBorder(BorderFactory.createLineBorder(Color.GRAY));
      this.colorChart.setCursor(pipetteCursor);
      ToolTipManager.sharedInstance().registerComponent(this.colorChart);
      MouseAdapter colorChartMouseListener = new MouseAdapter() {
          @Override
          public void mousePressed(MouseEvent ev) {
            getColorSelectionModel().setSelectedColor(colorChart.getColorAt(ev.getX(), ev.getY()));
          }
          
          @Override
          public void mouseDragged(MouseEvent ev) {
            mousePressed(ev);
          }
        };
      this.colorChart.addMouseListener(colorChartMouseListener);
      this.colorChart.addMouseMotionListener(colorChartMouseListener);
      
      JLabel colorLabel = new JLabel(
          this.preferences.getLocalizedString(ColorButton.class, "colorLabel.text"));
      this.colorComponent = new JComponent() {
          @Override
          protected void paintComponent(Graphics g) {
            Insets insets = getInsets();
            int drawnWidth = getWidth() - insets.right - insets.left;
            int drawnHeight = getHeight() - insets.bottom - insets.top;
            g.setColor(Color.GRAY);
            g.translate(insets.left, insets.top);            
            g.drawRect(0, 0, drawnWidth / 2 - 1, drawnHeight - 1);
            g.drawRect(drawnWidth / 2, 0, drawnWidth / 2 - 1, drawnHeight - 1);
            g.setColor(initialColor);
            g.fillRect(1, 1, drawnWidth / 2 - 2, drawnHeight - 2);
            g.setColor(getColorSelectionModel().getSelectedColor());
            g.fillRect(drawnWidth / 2 + 1, 1, drawnWidth / 2 - 2, drawnHeight - 2);
          }
          
          @Override
          public Dimension getPreferredSize() {
            return new Dimension(60, 30);
          }
        };
      this.colorComponent.addMouseListener(new MouseAdapter() {
          @Override
          public void mousePressed(MouseEvent ev) {
            if (ev.getX() < ev.getComponent().getWidth() / 2) {
              getColorSelectionModel().setSelectedColor(initialColor);
            }
          }
        });
      this.colorComponent.setCursor(pipetteCursor);
      
      AbstractAction pipetteAction = new AbstractAction() {
          public void actionPerformed(ActionEvent ev) {
            // Grab desktop with a 1x1 window
            final JDialog pipetteWindow = new JDialog((Window)SwingUtilities.getRoot(getParent()));
            pipetteWindow.setUndecorated(true);
            pipetteWindow.setModal(true);
            Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
            pipetteWindow.setBounds(mouseLocation.x - 1, mouseLocation.y - 1, 3, 3);
            try {
              if (OperatingSystem.isJavaVersionAtLeast("1.7")) {
                // Call pipetteWindow.setOpacity(0.05f) by reflection to ensure Java SE 5 compatibility
                // Opacity is set to 0.05f to be almost transparent but still visible enough by the
                // the system not to switch to another application when the user clicks
                Window.class.getMethod("setOpacity", float.class).invoke(pipetteWindow, 0.05f);
              } else if (OperatingSystem.isJavaVersionAtLeast("1.6")) {
                // Call com.sun.awt.AWTUtilities.setWindowOpacity(pipetteWindow, 0.05f)
                Class<?> awtUtilitiesClass = Class.forName("com.sun.awt.AWTUtilities");
                awtUtilitiesClass.getMethod("setWindowOpacity", Window.class, float.class).invoke(null, pipetteWindow, 0.05f);
              } 
            } catch (Exception ex) {
              // For any exception, let's consider simply the method failed or doesn't exist
            }
            pipetteWindow.setCursor(pipetteCursor);
            // Follow mouse moves with a timer because mouse listeners would miss some events
            final Timer timer = new Timer(50, new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                  Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
                  pipetteWindow.setLocation(mouseLocation.x - 1, mouseLocation.y - 1);
                  pipetteWindow.setCursor(pipetteCursor);
                }
              });
            timer.start();
            pipetteWindow.getRootPane().getInputMap(JRootPane.WHEN_IN_FOCUSED_WINDOW).
                put(KeyStroke.getKeyStroke("ESCAPE"), "Close");
            final AbstractAction closeAction = new AbstractAction() {
                public void actionPerformed(ActionEvent ev) {
                  timer.stop();
                  pipetteWindow.dispose();
                }
              };
            pipetteWindow.getRootPane().getActionMap().put("Close", closeAction);
            pipetteWindow.addWindowFocusListener(new WindowFocusListener() {
                public void windowLostFocus(WindowEvent ev) {
                  closeAction.actionPerformed(null);
                }
                
                public void windowGainedFocus(WindowEvent ev) {
                }
              });
            pipetteWindow.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent ev) {
                  try {
                    closeAction.actionPerformed(null);
                    getColorSelectionModel().setSelectedColor(getColorAtMouseLocation());
                  } catch (AWTException ex) {
                    ex.printStackTrace();
                  }
                }
              });
            
            pipetteWindow.setVisible(true);
          }

          private Color getColorAtMouseLocation() throws AWTException {
            BufferedImage screenCapture = new Robot().createScreenCapture(
                new Rectangle(MouseInfo.getPointerInfo().getLocation(), new Dimension(1, 1)));
            int [] pixel = screenCapture.getRGB(0, 0, 1, 1, null, 0, 1);
            return new Color(pixel [0]);
          }
        };
      pipetteAction.putValue(Action.SMALL_ICON, 
          new ImageIcon(OperatingSystem.isMacOSX()
              ? ColorButton.class.getResource("resources/cursors/pipette16x16-macosx.png")
              : ColorButton.class.getResource("resources/cursors/pipette16x16.png")));
      JButton pipetteButton = new JButton(pipetteAction);
      pipetteButton.setFocusable(false);
      pipetteButton.setPreferredSize(new Dimension(30, 30));

      JLabel rgbLabel = new JLabel(this.preferences.getLocalizedString(ColorButton.class, "rgbLabel.text"));
      this.rgbTextField = new JFormattedTextField(new Format() {
          @Override
          public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            toAppendTo.append(String.format("#%6X", ((Color)obj).getRGB() & 0xFFFFFF).replace(' ', '0'));
            pos.setEndIndex(pos.getEndIndex() + 7);
            return toAppendTo;
          }
  
          @Override
          public Object parseObject(String source, ParsePosition pos) {
            if (source.length() == 7 && source.charAt(0) == '#') {
              try {                
                Color color = Color.decode(source);
                pos.setIndex(pos.getIndex() + 7);
                return color;
              } catch (NumberFormatException ex) {
              }
            }
            return null;
          }
        });
      this.rgbTextField.setColumns(5);
      this.rgbTextField.getDocument().addDocumentListener(new DocumentListener() {
          public void removeUpdate(DocumentEvent ev) {
            changedUpdate(ev);
          }
          
          public void insertUpdate(DocumentEvent ev) {
            changedUpdate(ev);
          }
          
          public void changedUpdate(DocumentEvent ev) {
            try {
              rgbTextField.commitEdit();
              getColorSelectionModel().setSelectedColor((Color)rgbTextField.getValue());
            } catch (ParseException ex) {
            }
          }
        });
      
      JLabel ralLabel = new JLabel(this.preferences.getLocalizedString(ColorButton.class, "ralLabel.text"));
      this.ralComboBox = new PaletteComboBox(ColorCode.RAL_COLORS);
      ItemListener paletteChangeListener = new ItemListener() {
          public void itemStateChanged(ItemEvent ev) {
            ColorCode ralColor = (ColorCode)((JComboBox)ev.getSource()).getSelectedItem();
            if (ralColor != null) {
              getColorSelectionModel().setSelectedColor(new Color(ralColor.getRGB()));
            }
          }
        };
      this.ralComboBox.addItemListener(paletteChangeListener);

      JLabel creativeCommonsLabel = new JLabel(
          this.preferences.getLocalizedString(ColorButton.class, "creativeCommonsLabel.text"));
      this.creativeCommonsComboBox = new PaletteComboBox(ColorCode.CREATIVE_COMMONS_COLORS);
      this.creativeCommonsComboBox.addItemListener(paletteChangeListener);

      if (!OperatingSystem.isMacOSX()) {
        rgbLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
            this.preferences.getLocalizedString(ColorButton.class, "rgbLabel.mnemonic")).getKeyCode());
        rgbLabel.setLabelFor(this.rgbTextField);
        ralLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
            this.preferences.getLocalizedString(ColorButton.class, "ralLabel.mnemonic")).getKeyCode());
        ralLabel.setLabelFor(this.ralComboBox);
        creativeCommonsLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
            this.preferences.getLocalizedString(ColorButton.class, "creativeCommonsLabel.mnemonic")).getKeyCode());
        creativeCommonsLabel.setLabelFor(this.creativeCommonsComboBox);
      }
      
      // Layout components
      setLayout(new GridBagLayout());
      int labelAlignment = GridBagConstraints.LINE_START;
      add(this.grayColorChart, new GridBagConstraints(
          0, 0, 1, 7, 0, 0, GridBagConstraints.CENTER, 
          GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));
      add(this.colorChart, new GridBagConstraints(
          1, 0, 1, 7, 0, 0, GridBagConstraints.CENTER, 
          GridBagConstraints.BOTH, new Insets(0, 0, 0, 10), 0, 0));           
      add(colorLabel, new GridBagConstraints(
          2, 0, 1, 1, 0, 0, labelAlignment, 
          GridBagConstraints.NONE, new Insets(0, 0, 8, 5), 0, 0));           
      add(this.colorComponent, new GridBagConstraints(
          3, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
          GridBagConstraints.HORIZONTAL, new Insets(0, 0, 8, 0), 0, 0));
      add(pipetteButton, new GridBagConstraints(
          4, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, 
          GridBagConstraints.NONE, new Insets(0, 5, 8, 0), 0, 0));
      add(rgbLabel, new GridBagConstraints(
          2, 1, 3, 1, 0, 0, labelAlignment, 
          GridBagConstraints.NONE, new Insets(0, 0, 2, 0), 0, 0));           
      add(this.rgbTextField, new GridBagConstraints(
          2, 2, 3, 1, 0, 0, GridBagConstraints.LINE_START, 
          GridBagConstraints.HORIZONTAL, 
          OperatingSystem.isMacOSX() 
              ? new Insets(0, 1, 5, 1) 
              : new Insets(0, 0, 5, 0), 0, 0));           
      add(ralLabel, new GridBagConstraints(
          2, 3, 3, 1, 0, 0, labelAlignment, 
          GridBagConstraints.NONE, new Insets(0, 0, 2, 0), 0, 0));           
      add(this.ralComboBox, new GridBagConstraints(
          2, 4, 3, 1, 0, 0, GridBagConstraints.LINE_START, 
          GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));           
      add(creativeCommonsLabel, new GridBagConstraints(
          2, 5, 3, 1, 0, 0, labelAlignment, 
          GridBagConstraints.NONE, new Insets(0, 0, 2, 0), 0, 0));           
      add(this.creativeCommonsComboBox, new GridBagConstraints(
          2, 6, 3, 1, 0, 0, GridBagConstraints.LINE_START, 
          GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
      
      getColorSelectionModel().addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            updateChooser();
          }
        });
    }

    @Override
    public String getDisplayName() {
      return this.preferences.getLocalizedString(ColorButton.class, "chooserPanel.title");
    }

    @Override
    public Icon getSmallDisplayIcon() {
      return null;
    }

    @Override
    public Icon getLargeDisplayIcon() {
      return null;
    }
  }
  
  /**
   * A gray color chart.
   */
  private static final class GrayColorChart extends JComponent {
    @Override
    protected void paintComponent(Graphics g) {          
      Insets insets = getInsets();
      g.translate(insets.left, 0);
      int drawnWidth = getWidth() - insets.right - insets.left;
      for (int y = insets.top, m = getHeight() - insets.bottom; y < m; y++) {
        g.setColor(getColorAt(y));
        g.fillRect(0, y, drawnWidth, 1);
      }
      g.translate(-insets.left, 0);
    }

    @Override
    public Dimension getPreferredSize() {
      Insets insets = getInsets();
      return new Dimension(15 + insets.right + insets.left, 128 + insets.bottom + insets.top);
    }

    public Color getColorAt(int y) {
      Insets insets = getInsets();
      float scale = (float)(Math.min(Math.max(insets.top, y), getHeight() - insets.bottom - 1) - insets.top) 
          / (getHeight() - insets.bottom - insets.top - 1);
      return new Color(scale, scale, scale);
    }
    
    @Override
    public String getToolTipText(MouseEvent ev) {
      String color = String.format("#%6X", getColorAt(ev.getY()).getRGB() & 0xFFFFFF).replace(' ', '0');
      return "<html><table><tr><td width='50' height='50' bgcolor='" + color + "'></td></tr>"
          + "<tr><td align='center'>" + color + "</td></tr>";
    }
  }
  
  /**
   * A color chart.
   */
  private static final class ColorChart extends JComponent {
    @Override
    protected void paintComponent(Graphics g) {          
      Insets insets = getInsets();
      for (int x = insets.left, n = getWidth() - insets.right; x < n; x++) {
        for (int y = insets.top, m = getHeight() - insets.bottom; y < m; y++) {
          g.setColor(getColorAt(x, y));
          g.fillRect(x, y, 1, 1);
        }
      }
    }

    @Override
    public Dimension getPreferredSize() {
      Insets insets = getInsets();
      return new Dimension(256 + insets.right + insets.left, 128 + insets.bottom + insets.top);
    }

    public Color getColorAt(int x, int y) {
      Insets insets = getInsets();
      x = Math.min(Math.max(0, x - insets.left), getWidth() - insets.right - 1);
      y = Math.min(Math.max(0, y - insets.top), getHeight() - insets.bottom - 1);
      int drawnWidth = getWidth() - insets.right - insets.left;
      int drawnHeight = getHeight() - insets.bottom - insets.top;
      if (y < drawnHeight / 2) {
        return Color.getHSBColor((float)x / (drawnWidth - 1), 1, (float)y / (drawnHeight - 1) * 2);
      } else {
        return Color.getHSBColor((float)x / (drawnWidth - 1), (float)(drawnHeight - 1 - y) / (drawnHeight - 1) * 2, 1);
      }
    }

    @Override
    public String getToolTipText(MouseEvent ev) {
      String color = String.format("#%6X", getColorAt(ev.getX(), ev.getY()).getRGB() & 0xFFFFFF).replace(' ', '0');
      return "<html><table><td width='50' height='50' bgcolor='" + color + "'></td></tr>"
          + "<tr><td align='center'>" + color + "</td></tr>";
    }
  }

  /**
   * A combo box able to display a color palette.
   */
  private static class PaletteComboBox extends JComboBox {
    private PaletteComboBox(ColorCode [] colors) {
      super(colors);
      // Set combo box popup wider as suggested at http://forums.java.net/jive/message.jspa?messageID=61267
      PopupMenuListener comboBoxPopupMenuListener = new PopupMenuListener() {
          public void popupMenuWillBecomeVisible(PopupMenuEvent ev) {
            JComboBox comboBox = (JComboBox)ev.getSource();
            Object comp = comboBox.getUI().getAccessibleChild(comboBox, 0);
            if (!(comp instanceof JPopupMenu)) {
              return;
            }
            JComponent scrollPane = (JComponent)((JPopupMenu)comp).getComponent(0);
            Dimension size = new Dimension();
            size.width = comboBox.getPreferredSize().width - 10;
            size.height = scrollPane.getPreferredSize().height;
            scrollPane.setPreferredSize(size);
            scrollPane.setMaximumSize(size);
          }
          
          public void popupMenuCanceled(PopupMenuEvent e) {
          }
          
          public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
          }
        };
      if (!OperatingSystem.isMacOSX()) {
        addPopupMenuListener(comboBoxPopupMenuListener);
      } 
      setRenderer(new DefaultListCellRenderer() {
          @Override
          public Component getListCellRendererComponent(final JList list, 
                    Object value, int index, boolean isSelected, boolean cellHasFocus) {
            final ColorCode color = (ColorCode)value;
            if (color == null) {
              value = " ";
            } else {
              value = color.getId();
            }
            Component component = super.getListCellRendererComponent(list, value,
                index, isSelected, cellHasFocus);
            if (color != null) {
              setIcon(new Icon() {
                  public int getIconWidth() {
                    return 32;
                  }
            
                  public int getIconHeight() {
                    return 16;
                  }
            
                  public void paintIcon(Component c, Graphics g, int x, int y) {
                    g.setColor(new Color(color.getRGB()));
                    g.fillRect(x + 1, y + 1, getIconWidth() - 3, getIconHeight() - 2);
                    g.setColor(list.getForeground());
                    g.drawRect(x, y, getIconWidth() - 2, getIconHeight() - 1);
                  }
                });
            }
            return component;
          }
        });
    }
    
    /**
     * Returns the color code matching the one given in parameter or <code>null</code> if not found.
     */
    public ColorCode getColorCode(Color color) {
      if (color != null) {
        for (int i = 1, n = getModel().getSize(); i < n; i++) {
          ColorCode colorCode = (ColorCode)getModel().getElementAt(i);
          if (colorCode.getRGB() == (color.getRGB() & 0xFFFFFF)) {
            return colorCode;
          }
        }
      }
      return null;
    }
  }
  
  /**
   * Color code.
   */
  private static final class ColorCode {
    private final String id;
    private final int rgb;

    public ColorCode(String id, int rgb) {
      this.id = id;
      this.rgb = rgb;
    }
    
    public String getId() {
      return this.id;
    }

    public int getRGB() {
      return this.rgb;
    }
    
    // From http://www.ralcolor.com/
    public static final ColorCode RAL_COLORS [] = {
        new ColorCode("1000", 0xBEBD7F),
        new ColorCode("1001", 0xC2B078),
        new ColorCode("1002", 0xC6A664),
        new ColorCode("1003", 0xE5BE01),
        new ColorCode("1004", 0xCDA434),
        new ColorCode("1005", 0xA98307),
        new ColorCode("1006", 0xE4A010),
        new ColorCode("1007", 0xDC9D00),
        new ColorCode("1011", 0x8A6642),
        new ColorCode("1012", 0xC7B446),
        new ColorCode("1013", 0xEAE6CA),
        new ColorCode("1014", 0xE1CC4F),
        new ColorCode("1015", 0xE6D690),
        new ColorCode("1016", 0xEDFF21),
        new ColorCode("1017", 0xF5D033),
        new ColorCode("1018", 0xF8F32B),
        new ColorCode("1019", 0x9E9764),
        new ColorCode("1020", 0x999950),
        new ColorCode("1021", 0xF3DA0B),
        new ColorCode("1023", 0xFAD201),
        new ColorCode("1024", 0xAEA04B),
        new ColorCode("1026", 0xFFFF00),
        new ColorCode("1027", 0x9D9101),
        new ColorCode("1028", 0xF4A900),
        new ColorCode("1032", 0xD6AE01),
        new ColorCode("1033", 0xF3A505),
        new ColorCode("1034", 0xEFA94A),
        new ColorCode("1035", 0x6A5D4D),
        new ColorCode("1036", 0x705335),
        new ColorCode("1037", 0xF39F18),
        new ColorCode("2000", 0xED760E),
        new ColorCode("2001", 0xC93C20),
        new ColorCode("2002", 0xCB2821),
        new ColorCode("2003", 0xFF7514),
        new ColorCode("2004", 0xF44611),
        new ColorCode("2005", 0xFF2301),
        new ColorCode("2007", 0xFFA420),
        new ColorCode("2008", 0xF75E25),
        new ColorCode("2009", 0xF54021),
        new ColorCode("2010", 0xD84B20),
        new ColorCode("2011", 0xEC7C26),
        new ColorCode("2012", 0xE55137),
        new ColorCode("2013", 0xC35831),
        new ColorCode("3000", 0xAF2B1E),
        new ColorCode("3001", 0xA52019),
        new ColorCode("3002", 0xA2231D),
        new ColorCode("3003", 0x9B111E),
        new ColorCode("3004", 0x75151E),
        new ColorCode("3005", 0x5E2129),
        new ColorCode("3007", 0x412227),
        new ColorCode("3009", 0x642424),
        new ColorCode("3011", 0x781F19),
        new ColorCode("3012", 0xC1876B),
        new ColorCode("3013", 0xA12312),
        new ColorCode("3014", 0xD36E70),
        new ColorCode("3015", 0xEA899A),
        new ColorCode("3016", 0xB32821),
        new ColorCode("3017", 0xE63244),
        new ColorCode("3018", 0xD53032),
        new ColorCode("3020", 0xCC0605),
        new ColorCode("3022", 0xD95030),
        new ColorCode("3024", 0xF80000),
        new ColorCode("3026", 0xFE0000),
        new ColorCode("3027", 0xC51D34),
        new ColorCode("3031", 0xB32428),
        new ColorCode("3032", 0x721422),
        new ColorCode("3033", 0xB44C43),
        new ColorCode("4001", 0x6D3F5B),
        new ColorCode("4002", 0x922B3E),
        new ColorCode("4003", 0xDE4C8A),
        new ColorCode("4004", 0x641C34),
        new ColorCode("4005", 0x6C4675),
        new ColorCode("4006", 0xA03472),
        new ColorCode("4007", 0x4A192C),
        new ColorCode("4008", 0x924E7D),
        new ColorCode("4009", 0xA18594),
        new ColorCode("4010", 0xCF3476),
        new ColorCode("4011", 0x8673A1),
        new ColorCode("4012", 0x6C6874),
        new ColorCode("5000", 0x354D73),
        new ColorCode("5001", 0x1F3438),
        new ColorCode("5002", 0x20214F),
        new ColorCode("5003", 0x1D1E33),
        new ColorCode("5004", 0x18171C),
        new ColorCode("5005", 0x1E2460),
        new ColorCode("5007", 0x3E5F8A),
        new ColorCode("5008", 0x26252D),
        new ColorCode("5009", 0x025669),
        new ColorCode("5010", 0x0E294B),
        new ColorCode("5011", 0x231A24),
        new ColorCode("5012", 0x3B83BD),
        new ColorCode("5013", 0x1E213D),
        new ColorCode("5014", 0x606E8C),
        new ColorCode("5015", 0x2271B3),
        new ColorCode("5017", 0x063971),
        new ColorCode("5018", 0x3F888F),
        new ColorCode("5019", 0x1B5583),
        new ColorCode("5020", 0x1D334A),
        new ColorCode("5021", 0x256D7B),
        new ColorCode("5022", 0x252850),
        new ColorCode("5023", 0x49678D),
        new ColorCode("5024", 0x5D9B9B),
        new ColorCode("5025", 0x2A6478),
        new ColorCode("5026", 0x102C54),
        new ColorCode("6000", 0x316650),
        new ColorCode("6001", 0x287233),
        new ColorCode("6002", 0x2D572C),
        new ColorCode("6003", 0x424632),
        new ColorCode("6004", 0x1F3A3D),
        new ColorCode("6005", 0x2F4538),
        new ColorCode("6006", 0x3E3B32),
        new ColorCode("6007", 0x343B29),
        new ColorCode("6008", 0x39352A),
        new ColorCode("6009", 0x31372B),
        new ColorCode("6010", 0x35682D),
        new ColorCode("6011", 0x587246),
        new ColorCode("6012", 0x343E40),
        new ColorCode("6013", 0x6C7156),
        new ColorCode("6014", 0x47402E),
        new ColorCode("6015", 0x3B3C36),
        new ColorCode("6016", 0x1E5945),
        new ColorCode("6017", 0x4C9141),
        new ColorCode("6018", 0x57A639),
        new ColorCode("6019", 0xBDECB6),
        new ColorCode("6020", 0x2E3A23),
        new ColorCode("6021", 0x89AC76),
        new ColorCode("6022", 0x25221B),
        new ColorCode("6024", 0x308446),
        new ColorCode("6025", 0x3D642D),
        new ColorCode("6026", 0x015D52),
        new ColorCode("6027", 0x84C3BE),
        new ColorCode("6028", 0x2C5545),
        new ColorCode("6029", 0x20603D),
        new ColorCode("6032", 0x317F43),
        new ColorCode("6033", 0x497E76),
        new ColorCode("6034", 0x7FB5B5),
        new ColorCode("6035", 0x1C542D),
        new ColorCode("6036", 0x193737),
        new ColorCode("7000", 0x78858B),
        new ColorCode("7001", 0x8A9597),
        new ColorCode("7002", 0x7E7B52),
        new ColorCode("7003", 0x6C7059),
        new ColorCode("7004", 0x969992),
        new ColorCode("7005", 0x646B63),
        new ColorCode("7006", 0x6D6552),
        new ColorCode("7008", 0x6A5F31),
        new ColorCode("7009", 0x4D5645),
        new ColorCode("7010", 0x4C514A),
        new ColorCode("7011", 0x434B4D),
        new ColorCode("7012", 0x4E5754),
        new ColorCode("7013", 0x464531),
        new ColorCode("7015", 0x434750),
        new ColorCode("7016", 0x293133),
        new ColorCode("7021", 0x23282B),
        new ColorCode("7022", 0x332F2C),
        new ColorCode("7023", 0x686C5E),
        new ColorCode("7024", 0x474A51),
        new ColorCode("7026", 0x2F353B),
        new ColorCode("7030", 0x8B8C7A),
        new ColorCode("7031", 0x474B4E),
        new ColorCode("7032", 0xB8B799),
        new ColorCode("7033", 0x7D8471),
        new ColorCode("7034", 0x8F8B66),
        new ColorCode("7035", 0xD7D7D7),
        new ColorCode("7036", 0x7F7679),
        new ColorCode("7037", 0x7D7F7D),
        new ColorCode("7038", 0xB5B8B1),
        new ColorCode("7039", 0x6C6960),
        new ColorCode("7040", 0x9DA1AA),
        new ColorCode("7042", 0x8D948D),
        new ColorCode("7043", 0x4E5452),
        new ColorCode("7044", 0xCAC4B0),
        new ColorCode("7045", 0x909090),
        new ColorCode("7046", 0x82898F),
        new ColorCode("7047", 0xD0D0D0),
        new ColorCode("7048", 0x898176),
        new ColorCode("8000", 0x826C34),
        new ColorCode("8001", 0x955F20),
        new ColorCode("8002", 0x6C3B2A),
        new ColorCode("8003", 0x734222),
        new ColorCode("8004", 0x8E402A),
        new ColorCode("8007", 0x59351F),
        new ColorCode("8008", 0x6F4F28),
        new ColorCode("8011", 0x5B3A29),
        new ColorCode("8012", 0x592321),
        new ColorCode("8014", 0x382C1E),
        new ColorCode("8015", 0x633A34),
        new ColorCode("8016", 0x4C2F27),
        new ColorCode("8017", 0x45322E),
        new ColorCode("8019", 0x403A3A),
        new ColorCode("8022", 0x212121),
        new ColorCode("8023", 0xA65E2E),
        new ColorCode("8024", 0x79553D),
        new ColorCode("8025", 0x755C48),
        new ColorCode("8028", 0x4E3B31),
        new ColorCode("8029", 0x763C28),
        new ColorCode("9001", 0xFDF4E3),
        new ColorCode("9002", 0xE7EBDA),
        new ColorCode("9003", 0xF4F4F4),
        new ColorCode("9004", 0x282828),
        new ColorCode("9005", 0x0A0A0A),
        new ColorCode("9006", 0xA5A5A5),
        new ColorCode("9007", 0x8F8F8F),
        new ColorCode("9010", 0xFFFFFF),
        new ColorCode("9011", 0x1C1C1C),
        new ColorCode("9016", 0xF6F6F6),
        new ColorCode("9017", 0x1E1E1E),
        new ColorCode("9018", 0xD7D7D7),
        new ColorCode("9022", 0x9C9C9C),
        new ColorCode("9023", 0x828282)};   
    
    // From http://wiki.creativecommons.org/Colors/
    public static final ColorCode CREATIVE_COMMONS_COLORS [] = {
        new ColorCode("Magenta 1", 0xf7b2cc), 
        new ColorCode("Magenta 2", 0xb62b6e),
        new ColorCode("Magenta 3", 0x7b1b53), 
        new ColorCode("Magenta 4", 0x4f1035),
        new ColorCode("Purple 1", 0xd6b4d6), 
        new ColorCode("Purple 2", 0x9628c6),
        new ColorCode("Purple 3", 0x662978),
        new ColorCode("Purple 4", 0x42194f),
        new ColorCode("Blue 1", 0xbfceea),
        new ColorCode("Blue 2", 0x4374b7),
        new ColorCode("Blue 3", 0x214184),
        new ColorCode("Blue 4", 0x102548),
        new ColorCode("CC Green 1", 0xe0e4e1),
        new ColorCode("CC Green 2", 0xabb8af),
        new ColorCode("CC Green 3", 0x43594a),
        new ColorCode("CC Green 4", 0x2a372f),
        new ColorCode("Green 1", 0xe4eba0),
        new ColorCode("Green 2", 0x98c807),
        new ColorCode("Green 3", 0x627d0f),
        new ColorCode("Green 4", 0x3d5024),
        new ColorCode("Sand 1", 0xebe0b2),
        new ColorCode("Sand 2", 0xb1a24a),
        new ColorCode("Sand 3", 0x6d6118),
        new ColorCode("Sand 4", 0x44411f),
        new ColorCode("NC Yellow 1", 0xfff3a2),
        new ColorCode("NC Yellow 1", 0xedd812),
        new ColorCode("NC Yellow 2", 0xb09a11),
        new ColorCode("NC Yellow 3", 0x4e4917),
        new ColorCode("Orange 1", 0xffd89e),
        new ColorCode("Orange 2", 0xef9421),
        new ColorCode("Orange 3", 0xa46810),
        new ColorCode("Orange 4", 0x543d03),
        new ColorCode("Red 1", 0xfab499),
        new ColorCode("Red 2", 0xd13814),
        new ColorCode("Red 3", 0x912b10),
        new ColorCode("Red 4", 0x5a1c0e),
        new ColorCode("Grey 1", 0xededee),
        new ColorCode("Grey 2", 0xdcddde),
        new ColorCode("Grey 3", 0xa7a9ac),
        new ColorCode("Grey 4", 0x636466),
        new ColorCode("Grey 5", 0x393839),
        new ColorCode("Grey 5", 0x121513)};
  }

  /**
   * Panel showing recent colors.
   */
  private static class RecentColorsPanel extends JPanel {
    public static final int MAX_COLORS = 20;
    
    private Cursor pipetteCursor;
    
    private RecentColorsPanel(final ColorSelectionModel colorSelectionModel,
                              final UserPreferences preferences) {
      super(new GridBagLayout());
      this.pipetteCursor = SwingTools.createCustomCursor(
          OperatingSystem.isMacOSX()
              ? ColorButton.class.getResource("resources/cursors/pipette16x16-macosx.png")
              : ColorButton.class.getResource("resources/cursors/pipette16x16.png"),
          ColorButton.class.getResource("resources/cursors/pipette32x32.png"), 
          0, 1, "Pipette", Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
      preferences.addPropertyChangeListener(UserPreferences.Property.RECENT_COLORS, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              setRecentColors(colorSelectionModel, preferences);
            }
          });
      setRecentColors(colorSelectionModel, preferences);
    }

    private void setRecentColors(final ColorSelectionModel colorSelectionModel, 
                                 UserPreferences preferences) {
      removeAll();
      int i = 0;
      if (UIManager.getLookAndFeel().getID().equals("GTK")) {
        // Add a label to replace the border that is not active
        add(new JLabel(UIManager.getString("ColorChooser.previewText")), new GridBagConstraints(
            i++, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
            GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
      }      
      for (final Integer color : preferences.getRecentColors()) {
        Component colorComponent = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
              Insets insets = getInsets();
              int drawnWidth = getWidth() - insets.right - insets.left;
              int drawnHeight = getHeight() - insets.bottom - insets.top;
              g.setColor(Color.GRAY);
              g.translate(insets.left, insets.top);            
              g.drawRect(0, 0, drawnWidth - 1, drawnHeight - 1);
              g.setColor(new Color(color));
              g.fillRect(1, 1, drawnWidth - 2, drawnHeight - 2);
            }
            
            @Override
            public Dimension getPreferredSize() {
              return new Dimension(20, 20);
            }
          };
        colorComponent.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent ev) {
              colorSelectionModel.setSelectedColor(new Color(color));
            }
          });
        colorComponent.setCursor(this.pipetteCursor);
        add(colorComponent, new GridBagConstraints(
            i++, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, 
            GridBagConstraints.NONE, new Insets(0, 0, 0, 2), 0, 0));
      }
    }

    public Insets getInsets() { 
      if (OperatingSystem.isJavaVersionAtLeast("1.7")) {
        return super.getInsets();
      } else {
        if (UIManager.getLookAndFeel().getID().equals("GTK")) {
          return new Insets(5, 5, 5, 5);
        } else {
          // Tip to ensure the preview panel isn't ignored by BasicColorChooserUI
          return new Insets(1, 0, 0, 0);
        }
      }
    }
  }
}