/*
 * PolylinePanel.java
 *
 * Copyright (c) 2009 Plan PHP All Rights Reserved.
 */
package com.eteks.sweethome3d.swing;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.eteks.sweethome3d.j3d.ShapeTools;
import com.eteks.sweethome3d.model.Polyline;
import com.eteks.sweethome3d.model.Polyline.ArrowStyle;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.PolylineController;
import com.eteks.sweethome3d.viewcontroller.View;

/**
 * User preferences panel.
 * @author Emmanuel Puybaret
 */
public class PolylinePanel extends JPanel implements DialogView {
  private final PolylineController controller;
  private JLabel           thicknessLabel;
  private JSpinner         thicknessSpinner;
  private JLabel           arrowsStyleLabel;
  private JComboBox        arrowsStyleComboBox;
  private JLabel           joinStyleLabel;
  private JComboBox        joinStyleComboBox;
  private JLabel           dashStyleLabel;
  private JComboBox        dashStyleComboBox;
  private JLabel           dashOffsetLabel;
  private JSpinner         dashOffsetSpinner;
  private JLabel           colorLabel;
  private ColorButton      colorButton;
  private NullableCheckBox visibleIn3DViewCheckBox;
  private String           dialogTitle;

  /**
   * Creates a preferences panel that layouts the editable properties
   * of its <code>controller</code>.
   */
  public PolylinePanel(UserPreferences preferences,
                              PolylineController controller) {
    super(new GridBagLayout());
    this.controller = controller;
    createComponents(preferences, controller);
    setMnemonics(preferences);
    layoutComponents();
  }

  /**
   * Creates and initializes components and spinners model.
   */
  private void createComponents(UserPreferences preferences,
                                final PolylineController controller) {
    // Create thickness label and spinner bound to controller THICKNESS property
    this.thicknessLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        PolylinePanel.class, "thicknessLabel.text", preferences.getLengthUnit().getName()));
    final NullableSpinner.NullableSpinnerLengthModel thicknessSpinnerModel =
        new NullableSpinner.NullableSpinnerLengthModel(preferences, preferences.getLengthUnit().getMinimumLength(), 50f);
    this.thicknessSpinner = new NullableSpinner(thicknessSpinnerModel);
    thicknessSpinnerModel.setNullable(controller.getThickness() == null);
    thicknessSpinnerModel.setLength(controller.getThickness());
    thicknessSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.setThickness(thicknessSpinnerModel.getLength());
        }
      });
    controller.addPropertyChangeListener(PolylineController.Property.THICKNESS,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            thicknessSpinnerModel.setLength(controller.getThickness());
          }
        });

    // Create cap style label and combo box bound to controller CAP_STYLE property
    this.arrowsStyleLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        PolylinePanel.class, "arrowsStyleLabel.text"));
    ArrowsStyle[] arrowsStyles = ArrowsStyle.getArrowsStyle();
    if (controller.getCapStyle() == null) {
      List<ArrowsStyle> arrowsStylesList = new ArrayList<ArrowsStyle>();
      arrowsStylesList.add(null);
      arrowsStylesList.addAll(Arrays.asList(arrowsStyles));
      arrowsStyles = arrowsStylesList.toArray(new ArrowsStyle [arrowsStylesList.size()]);
    }
    this.arrowsStyleComboBox = new JComboBox(new DefaultComboBoxModel(arrowsStyles));
    this.arrowsStyleComboBox.setMaximumRowCount(arrowsStyles.length);
    final float resolutionScale = SwingTools.getResolutionScale();
    this.arrowsStyleComboBox.setRenderer(new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(final JList list,
            Object value, int index, boolean isSelected, boolean cellHasFocus) {
          final ArrowsStyle arrowsStyle = (ArrowsStyle)value;
          final Component component = super.getListCellRendererComponent(
              list, "", index, isSelected, cellHasFocus);
          setIcon(new Icon() {
              public int getIconWidth() {
                return Math.round(64 * resolutionScale);
              }

              public int getIconHeight() {
                return Math.round(16 * resolutionScale);
              }

              public void paintIcon(Component c, Graphics g, int x, int y) {
                if (arrowsStyle != null) {
                  Graphics2D g2D = (Graphics2D)g;
                  g2D.scale(resolutionScale, resolutionScale);
                  if (OperatingSystem.isMacOSXLeopardOrSuperior()) {
                    g2D.translate(0, 2);
                  }
                  g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                  g2D.setColor(list.getForeground());
                  int iconWidth = 64;
                  g2D.setStroke(new BasicStroke(2));
                  g2D.drawLine(6, 8, iconWidth - 6, 8);
                  switch (arrowsStyle.getStartArrowStyle()) {
                    case NONE :
                      break;
                    case DISC :
                      g2D.fillOval(4, 4, 9, 9);
                      break;
                    case OPEN :
                      g2D.drawPolyline(new int [] {15, 5, 15}, new int [] {4, 8, 12}, 3);
                      break;
                    case DELTA :
                      g2D.fillPolygon(new int [] {3, 15, 15}, new int [] {8, 3, 13}, 3);
                      break;
                  }
                  switch (arrowsStyle.getEndArrowStyle()) {
                    case NONE :
                      break;
                    case DISC :
                      g2D.fillOval(iconWidth - 12, 4, 9, 9);
                      break;
                    case OPEN :
                      g2D.drawPolyline(new int [] {iconWidth - 14, iconWidth - 4, iconWidth - 14}, new int [] {4, 8, 12}, 3);
                      break;
                    case DELTA :
                      g2D.fillPolygon(new int [] {iconWidth - 2, iconWidth - 14, iconWidth - 14}, new int [] {8, 3, 13}, 3);
                      break;
                  }
                }
              }
            });
          return component;
        }
      });
    this.arrowsStyleComboBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent ev) {
          ArrowsStyle arrowsStyle = (ArrowsStyle)arrowsStyleComboBox.getSelectedItem();
          if (arrowsStyle != null) {
            controller.setStartArrowStyle(arrowsStyle.getStartArrowStyle());
            controller.setEndArrowStyle(arrowsStyle.getEndArrowStyle());
          } else {
            controller.setStartArrowStyle(null);
            controller.setEndArrowStyle(null);
          }
        }
      });
    PropertyChangeListener arrowStyleChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          ArrowStyle startArrowStyle = controller.getStartArrowStyle();
          ArrowStyle endArrowStyle = controller.getEndArrowStyle();
          if (startArrowStyle != null && endArrowStyle != null) {
            arrowsStyleComboBox.setSelectedItem(new ArrowsStyle(startArrowStyle, endArrowStyle));
          } else {
            arrowsStyleComboBox.setSelectedItem(null);
          }
          arrowsStyleLabel.setEnabled(controller.isArrowsStyleEditable());
          arrowsStyleComboBox.setEnabled(controller.isArrowsStyleEditable());
        }
      };
    controller.addPropertyChangeListener(PolylineController.Property.START_ARROW_STYLE,
        arrowStyleChangeListener);
    controller.addPropertyChangeListener(PolylineController.Property.END_ARROW_STYLE,
        arrowStyleChangeListener);
    arrowStyleChangeListener.propertyChange(null);

    // Create join style label and combo box bound to controller JOIN_STYLE property
    this.joinStyleLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        PolylinePanel.class, "joinStyleLabel.text"));
    Polyline.JoinStyle [] joinStyles = Polyline.JoinStyle.values();
    if (controller.getJoinStyle() == null) {
      List<Polyline.JoinStyle> joinStylesList = new ArrayList<Polyline.JoinStyle>();
      joinStylesList.add(null);
      joinStylesList.addAll(Arrays.asList(joinStyles));
      joinStyles = joinStylesList.toArray(new Polyline.JoinStyle [joinStylesList.size()]);
    }
    this.joinStyleComboBox = new JComboBox(new DefaultComboBoxModel(joinStyles));
    final GeneralPath joinPath = new GeneralPath();
    joinPath.moveTo(4, 4);
    joinPath.lineTo(58, 4);
    joinPath.lineTo(36, 14);
    final Shape curvedPath = new Arc2D.Float(-7, 6, 80, 40, 47, 86, Arc2D.OPEN);
    this.joinStyleComboBox.setRenderer(new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(final JList list,
            Object value, int index, boolean isSelected, boolean cellHasFocus) {
          final Polyline.JoinStyle joinStyle = (Polyline.JoinStyle)value;
          final Component component = super.getListCellRendererComponent(
              list, "", index, isSelected, cellHasFocus);
          setIcon(new Icon() {
              public int getIconWidth() {
                return Math.round(64 * resolutionScale);
              }

              public int getIconHeight() {
                return Math.round(16 * resolutionScale);
              }

              public void paintIcon(Component c, Graphics g, int x, int y) {
                if (joinStyle != null) {
                  Graphics2D g2D = (Graphics2D)g;
                  g2D.scale(resolutionScale, resolutionScale);
                  if (OperatingSystem.isMacOSXLeopardOrSuperior()) {
                    g2D.translate(0, 2);
                  }
                  g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                  g2D.setColor(list.getForeground());
                  g2D.setStroke(SwingTools.getStroke(6, Polyline.CapStyle.BUTT, joinStyle, Polyline.DashStyle.SOLID));
                  if (joinStyle == Polyline.JoinStyle.CURVED) {
                    g2D.draw(curvedPath);
                  } else {
                    g2D.draw(joinPath);
                  }
                }
              }
            });
          return component;
        }
      });
    this.joinStyleComboBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent ev) {
          controller.setJoinStyle((Polyline.JoinStyle)joinStyleComboBox.getSelectedItem());
        }
      });
    PropertyChangeListener joinStyleChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          joinStyleLabel.setEnabled(controller.isJoinStyleEditable());
          joinStyleComboBox.setEnabled(controller.isJoinStyleEditable());
          joinStyleComboBox.setSelectedItem(controller.getJoinStyle());
        }
      };
    controller.addPropertyChangeListener(PolylineController.Property.JOIN_STYLE,
        joinStyleChangeListener);
    joinStyleChangeListener.propertyChange(null);

    // Create dash style label and combo box bound to controller DASH_STYLE property
    this.dashStyleLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        PolylinePanel.class, "dashStyleLabel.text"));
    List<Polyline.DashStyle> dashStyles = new ArrayList<Polyline.DashStyle>(Arrays.asList(Polyline.DashStyle.values()));
    if (controller.getDashStyle() != Polyline.DashStyle.CUSTOMIZED) {
      dashStyles.remove(Polyline.DashStyle.CUSTOMIZED);
    }
    if (controller.getDashStyle() == null) {
      dashStyles.add(0, null);
    }
    this.dashStyleComboBox = new JComboBox(new DefaultComboBoxModel(dashStyles.toArray(new Polyline.DashStyle [dashStyles.size()])));
    this.dashStyleComboBox.setRenderer(new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(final JList list,
            Object value, int index, boolean isSelected, boolean cellHasFocus) {
          final Polyline.DashStyle dashStyle = (Polyline.DashStyle)value;
          final Component component = super.getListCellRendererComponent(
              list, "", index, isSelected, cellHasFocus);
          setIcon(new Icon() {
              public int getIconWidth() {
                return Math.round(64 * resolutionScale);
              }

              public int getIconHeight() {
                return Math.round(16 * resolutionScale);
              }

              public void paintIcon(Component c, Graphics g, int x, int y) {
                if (dashStyle != null) {
                  Graphics2D g2D = (Graphics2D)g;
                  g2D.scale(resolutionScale, resolutionScale);
                  if (OperatingSystem.isMacOSXLeopardOrSuperior()) {
                    g2D.translate(0, 2);
                  }
                  g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                  g2D.setColor(list.getForeground());
                  float dashOffset = controller.getDashOffset() != null ? controller.getDashOffset().floatValue() : 0;
                  g2D.setStroke(ShapeTools.getStroke(2, Polyline.CapStyle.BUTT, Polyline.JoinStyle.MITER,
                      dashStyle != Polyline.DashStyle.CUSTOMIZED ? dashStyle.getDashPattern() : controller.getDashPattern(), dashOffset));
                  g2D.drawLine(4, 8, getIconWidth() - 4, 8);
                }
              }
            });
          return component;
        }
      });
    this.dashStyleComboBox.setSelectedItem(controller.getDashStyle());
    this.dashStyleComboBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent ev) {
          controller.setDashStyle((Polyline.DashStyle)dashStyleComboBox.getSelectedItem());
        }
      });
    controller.addPropertyChangeListener(PolylineController.Property.DASH_STYLE,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            dashStyleComboBox.setSelectedItem(controller.getDashStyle());
            dashOffsetSpinner.setEnabled(controller.getDashStyle() != Polyline.DashStyle.SOLID);
          }
        });

    // Create dash offset label and spinner bound to controller DASH_OFFSET property
    this.dashOffsetLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, PolylinePanel.class, "dashOffsetLabel.text"));
    final NullableSpinner.NullableSpinnerNumberModel dashOffsetSpinnerModel =
        new NullableSpinner.NullableSpinnerNumberModel(0f, 0f, 100f, 5f);
    this.dashOffsetSpinner = new NullableSpinner(dashOffsetSpinnerModel);
    dashOffsetSpinnerModel.setNullable(controller.getDashOffset() == null);
    dashOffsetSpinnerModel.setValue(controller.getDashOffset() != null ? controller.getDashOffset() * 100 : null);
    dashOffsetSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.setDashOffset(dashOffsetSpinnerModel.getValue() != null
              ? ((Number)dashOffsetSpinnerModel.getValue()).floatValue() / 100
              : null);
        }
      });
    controller.addPropertyChangeListener(PolylineController.Property.DASH_OFFSET,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            dashOffsetSpinnerModel.setValue(controller.getDashOffset() != null ? controller.getDashOffset() * 100 : null);
            dashStyleComboBox.repaint();
          }
        });
    this.dashOffsetSpinner.setEnabled(controller.getDashStyle() != Polyline.DashStyle.SOLID);

    // Create color label and its button bound to COLOR controller property
    this.colorLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        PolylinePanel.class, "colorLabel.text"));
    this.colorButton = new ColorButton(preferences);
    this.colorButton.setColorDialogTitle(preferences.getLocalizedString(
        PolylinePanel.class, "colorDialog.title"));
    this.colorButton.setColor(controller.getColor());
    this.colorButton.addPropertyChangeListener(ColorButton.COLOR_PROPERTY,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            controller.setColor(colorButton.getColor());
          }
        });
    controller.addPropertyChangeListener(PolylineController.Property.COLOR,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            colorButton.setColor(controller.getColor());
          }
        });

    // Create components bound to ELEVATION controller property
    this.visibleIn3DViewCheckBox = new NullableCheckBox(SwingTools.getLocalizedLabelText(preferences,
        PolylinePanel.class, "visibleIn3DViewCheckBox.text"));
    if (controller.isElevationEnabled() != null) {
      this.visibleIn3DViewCheckBox.setValue(controller.isElevationEnabled());
    } else {
      this.visibleIn3DViewCheckBox.setNullable(true);
      this.visibleIn3DViewCheckBox.setValue(null);
    }
    this.visibleIn3DViewCheckBox.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (visibleIn3DViewCheckBox.isNullable()) {
            visibleIn3DViewCheckBox.setNullable(false);
          }
          if (Boolean.FALSE.equals(visibleIn3DViewCheckBox.getValue())) {
            controller.setElevation(null);
          } else {
            controller.setElevation(0f);
          }
        }
      });

    this.dialogTitle = preferences.getLocalizedString(PolylinePanel.class, "polyline.title");
  }

  /**
   * Sets components mnemonics and label / component associations.
   */
  private void setMnemonics(UserPreferences preferences) {
    if (!OperatingSystem.isMacOSX()) {
      this.thicknessLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          PolylinePanel.class, "thicknessLabel.mnemonic")).getKeyCode());
      this.thicknessLabel.setLabelFor(this.thicknessSpinner);
      this.arrowsStyleLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          PolylinePanel.class, "arrowsStyleLabel.mnemonic")).getKeyCode());
      this.arrowsStyleLabel.setLabelFor(this.arrowsStyleComboBox);
      this.joinStyleLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          PolylinePanel.class, "joinStyleLabel.mnemonic")).getKeyCode());
      this.joinStyleLabel.setLabelFor(this.joinStyleComboBox);
      this.dashStyleLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          PolylinePanel.class, "dashStyleLabel.mnemonic")).getKeyCode());
      this.dashStyleLabel.setLabelFor(this.dashStyleComboBox);
      this.dashOffsetLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          PolylinePanel.class, "dashOffsetLabel.mnemonic")).getKeyCode());
      this.dashOffsetLabel.setLabelFor(this.dashOffsetSpinner);
      this.colorLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          PolylinePanel.class, "colorLabel.mnemonic")).getKeyCode());
      this.colorLabel.setLabelFor(this.colorButton);
      this.visibleIn3DViewCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          PolylinePanel.class, "visibleIn3DViewCheckBox.mnemonic")).getKeyCode());
    }
  }

  /**
   * Layouts panel components in panel with their labels.
   */
  private void layoutComponents() {
    int labelAlignment = OperatingSystem.isMacOSX()
        ? GridBagConstraints.LINE_END
        : GridBagConstraints.LINE_START;
    int standardGap = Math.round(5 * SwingTools.getResolutionScale());
    Insets labelInsets = new Insets(0, 0, standardGap, standardGap);
    // First row
    add(this.thicknessLabel, new GridBagConstraints(
        0, 0, 1, 1, 0, 0, labelAlignment,
        GridBagConstraints.NONE, labelInsets, 0, 0));
    Insets rightComponentInsets = new Insets(0, 0, standardGap, 0);
    add(this.thicknessSpinner, new GridBagConstraints(
        1, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, rightComponentInsets, 0, 0));
    // Second row
    add(this.arrowsStyleLabel, new GridBagConstraints(
        0, 1, 1, 1, 0, 0, labelAlignment,
        GridBagConstraints.NONE, labelInsets, 0, 0));
    add(this.arrowsStyleComboBox, new GridBagConstraints(
        1, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, rightComponentInsets, 0, 0));
    // Third row
    add(this.joinStyleLabel, new GridBagConstraints(
        0, 2, 1, 1, 0, 0, labelAlignment,
        GridBagConstraints.NONE, labelInsets, 0, 0));
    add(this.joinStyleComboBox, new GridBagConstraints(
        1, 2, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, rightComponentInsets, 0, 0));
    // Fourth row
    add(this.dashStyleLabel, new GridBagConstraints(
        0, 3, 1, 1, 0, 0, labelAlignment,
        GridBagConstraints.NONE, labelInsets, 0, 0));
    add(this.dashStyleComboBox, new GridBagConstraints(
        1, 3, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, rightComponentInsets, 0, 0));
    // Fifth row
    add(this.dashOffsetLabel, new GridBagConstraints(
        0, 4, 1, 1, 0, 0, labelAlignment,
        GridBagConstraints.NONE, labelInsets, 0, 0));
    add(this.dashOffsetSpinner, new GridBagConstraints(
        1, 4, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, rightComponentInsets, 0, 0));
    // Sixth row
    add(this.colorLabel, new GridBagConstraints(
        0, 5, 1, 1, 0, 0, labelAlignment,
        GridBagConstraints.NONE, labelInsets, 0, 0));
    add(this.colorButton, new GridBagConstraints(
        1, 5, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, new Insets(0, OperatingSystem.isMacOSX() ? 2  : -1, standardGap, OperatingSystem.isMacOSX() ? 3  : -1), 0, 0));
    // Last row
    add(this.visibleIn3DViewCheckBox, new GridBagConstraints(
        0, 6, 2, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
  }

  /**
   * Displays this panel in a dialog box.
   */
  public void displayView(View parentView) {
    if (SwingTools.showConfirmDialog((JComponent)parentView, this, this.dialogTitle,
          ((JSpinner.DefaultEditor)this.thicknessSpinner.getEditor()).getTextField()) == JOptionPane.OK_OPTION
        && this.controller != null) {
      this.controller.modifyPolylines();
    }
  }

  /**
   * A tuple storing start and end arrow styles.
   * @author Emmanuel Puybaret
   */
  private static class ArrowsStyle {
    private static List<ArrowsStyle> arrowsStyle;
    private final Polyline.ArrowStyle startArrowStyle;
    private final Polyline.ArrowStyle endArrowStyle;

    public ArrowsStyle(ArrowStyle startArrowStyle, ArrowStyle endArrowStyle) {
      this.startArrowStyle = startArrowStyle;
      this.endArrowStyle = endArrowStyle;
    }

    public Polyline.ArrowStyle getStartArrowStyle() {
      return this.startArrowStyle;
    }

    public Polyline.ArrowStyle getEndArrowStyle() {
      return this.endArrowStyle;
    }



    @Override
    public int hashCode() {
      int hashCode = 0;
      if (this.startArrowStyle != null) {
        hashCode = this.startArrowStyle.hashCode();
      }
      if (this.endArrowStyle != null) {
        hashCode += this.endArrowStyle.hashCode();
      }
      return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof ArrowsStyle) {
        ArrowsStyle arrowsStyle = (ArrowsStyle)obj;
        return this.startArrowStyle == arrowsStyle.startArrowStyle
            && this.endArrowStyle == arrowsStyle.endArrowStyle;
      } else {
        return false;
      }
    }

    public static ArrowsStyle [] getArrowsStyle() {
      if (arrowsStyle == null) {
        ArrowStyle [] arrowStyles = Polyline.ArrowStyle.values();
        arrowsStyle = new ArrayList<ArrowsStyle>(arrowStyles.length * arrowStyles.length);
        for (ArrowStyle startArrowStyle : arrowStyles) {
          for (ArrowStyle endArrowStyle : arrowStyles) {
            arrowsStyle.add(new ArrowsStyle(startArrowStyle, endArrowStyle));
          }
        }
      }
      return arrowsStyle.toArray(new ArrowsStyle [arrowsStyle.size()]);
    }
  }
}
