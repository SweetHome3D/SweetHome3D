/*
 * PhotoPanel.java 5 mai 2009
 *
 * Sweet Home 3D, Copyright (c) 2009 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.ActionMap;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.eteks.sweethome3d.j3d.PhotoRenderer;
import com.eteks.sweethome3d.model.AspectRatio;
import com.eteks.sweethome3d.model.Camera;
import com.eteks.sweethome3d.model.Camera.Lens;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.AbstractPhotoController;
import com.eteks.sweethome3d.viewcontroller.ContentManager;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.PhotoController;
import com.eteks.sweethome3d.viewcontroller.View;

/**
 * A panel to edit photo creation. 
 * @author Emmanuel Puybaret
 */
public class PhotoPanel extends JPanel implements DialogView {
  private enum ActionType {START_PHOTO_CREATION, STOP_PHOTO_CREATION, SAVE_PHOTO, CLOSE}

  private static final String PHOTO_DIALOG_X_VISUAL_PROPERTY = "com.eteks.sweethome3d.swing.PhotoPanel.PhotoDialogX";
  private static final String PHOTO_DIALOG_Y_VISUAL_PROPERTY = "com.eteks.sweethome3d.swing.PhotoPanel.PhotoDialogY";

  private static final int MINIMUM_DELAY_BEFORE_DISCARDING_WITHOUT_WARNING = 30000;
  
  private static final String WAIT_CARD  = "wait";
  private static final String PHOTO_CARD = "photo";
  
  private final Home               home;
  private final UserPreferences    preferences;
  private final PhotoController    controller;
  private ScaledImageComponent     photoComponent; 
  private JLabel                   animatedWaitLabel;
  private PhotoSizeAndQualityPanel sizeAndQualityPanel;
  private Component                advancedComponentsSeparator;
  private JLabel                   dateLabel;
  private JSpinner                 dateSpinner;
  private JLabel                   timeLabel;
  private JSpinner                 timeSpinner;
  private JLabel                   dayNightLabel;
  private JLabel                   lensLabel;
  private JComboBox                lensComboBox;
  private JCheckBox                ceilingLightEnabledCheckBox;
  private String                   dialogTitle;
  private JPanel                   photoPanel;
  private CardLayout               photoCardLayout;
  private ExecutorService          photoCreationExecutor;
  private long                     photoCreationStartTime;
  private JButton                  createButton;
  private JButton                  saveButton;
  private JButton                  closeButton;

  private static PhotoPanel        currentPhotoPanel; // Support only one photo panel opened at a time

  public PhotoPanel(Home home, 
                    UserPreferences preferences, 
                    PhotoController controller) {
    super(new GridBagLayout());
    this.home = home;
    this.preferences = preferences;
    this.controller = controller;
    createActions(preferences);
    createComponents(home, preferences, controller);
    setMnemonics(preferences);
    layoutComponents();    

    preferences.addPropertyChangeListener(UserPreferences.Property.LANGUAGE, new LanguageChangeListener(this));
  }
  
  /**
   * Creates actions for variables.
   */
  private void createActions(UserPreferences preferences) {
    final ActionMap actions = getActionMap();
    actions.put(ActionType.START_PHOTO_CREATION, 
        new ResourceAction(preferences, PhotoPanel.class, ActionType.START_PHOTO_CREATION.name(), true) {
          @Override
          public void actionPerformed(ActionEvent ev) {
            startPhotoCreation();
          }
        });
    actions.put(ActionType.STOP_PHOTO_CREATION, 
        new ResourceAction(preferences, PhotoPanel.class, ActionType.STOP_PHOTO_CREATION.name(), true) {
          @Override
          public void actionPerformed(ActionEvent ev) {
            stopPhotoCreation(true);
          }
        });
    actions.put(ActionType.SAVE_PHOTO, 
        new ResourceAction(preferences, PhotoPanel.class, ActionType.SAVE_PHOTO.name(), false) {
          @Override
          public void actionPerformed(ActionEvent ev) {
            savePhoto();
          }
        });
    actions.put(ActionType.CLOSE, 
        new ResourceAction(preferences, PhotoPanel.class, ActionType.CLOSE.name(), true) {
          @Override
          public void actionPerformed(ActionEvent ev) {
            close();
          }
        });
  }

  /**
   * Creates and initializes components.
   */
  private void createComponents(final Home home, 
                                final UserPreferences preferences,
                                final PhotoController controller) {
    this.photoComponent = new ScaledImageComponent();
    this.photoComponent.setPreferredSize(new Dimension(getToolkit().getScreenSize().width <= 1024 ? 320 : 400, 400));
    // Under Mac OS X, set a transfer handler and a mouse listener on photo component 
    // to let the user drag and drop the created image (Windows support seems to fail) 
    if (OperatingSystem.isMacOSX()) {
      this.photoComponent.setTransferHandler(new VisualTransferHandler() {
          @Override
          public int getSourceActions(JComponent component) {
            return COPY_OR_MOVE;
          }
          
          @Override
          protected Transferable createTransferable(JComponent component) {
            return new Transferable() {
                public Object getTransferData(DataFlavor flavor) {
                  return photoComponent.getImage();
                }
    
                public DataFlavor [] getTransferDataFlavors() {
                  return new DataFlavor [] {DataFlavor.imageFlavor};
                }
    
                public boolean isDataFlavorSupported(DataFlavor flavor) {
                  return flavor.equals(DataFlavor.imageFlavor);
                }
              };            
          }
          
          @Override
          public Icon getVisualRepresentation(Transferable transferable) {
            try {
              if (transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                // Create a 128x128 icon from the transfered image
                BufferedImage transferedImage = 
                    (BufferedImage)transferable.getTransferData(DataFlavor.imageFlavor);
                float scale = Math.min(1, 128f / Math.max(transferedImage.getWidth(), transferedImage.getHeight()));
                BufferedImage iconImage = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2D = (Graphics2D)iconImage.getGraphics();
                g2D.scale(scale, scale);
                g2D.drawRenderedImage(transferedImage, null);
                g2D.dispose();
                return new ImageIcon(iconImage);
              } 
            } catch (UnsupportedFlavorException ex) {
              // Use default representation
            } catch (IOException ex) {
              // Use default representation
            }
            return super.getVisualRepresentation(transferable);
          }  
        });
      this.photoComponent.addMouseListener(new MouseAdapter() {
          @Override
          public void mousePressed(MouseEvent ev) {
            if (SwingUtilities.isLeftMouseButton(ev)
                && photoComponent.getImage() != null
                && photoComponent.isPointInImage(ev.getX(), ev.getY())) {
              photoComponent.getTransferHandler().exportAsDrag(photoComponent, ev, TransferHandler.COPY);
            }
          }
        });
    }

    this.animatedWaitLabel = new JLabel(new ImageIcon(PhotoPanel.class.getResource("resources/animatedWait.gif")));

    // Create size and quality panel
    this.sizeAndQualityPanel = new PhotoSizeAndQualityPanel(home, preferences, controller);
    controller.addPropertyChangeListener(AbstractPhotoController.Property.QUALITY, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            updateAdvancedComponents();
          }
        });

    this.advancedComponentsSeparator = new JSeparator();

    // Create date and time labels and spinners bound to TIME controller property
    Date time = new Date(Camera.convertTimeToTimeZone(controller.getTime(), TimeZone.getDefault().getID()));
    this.dateLabel = new JLabel();
    final SpinnerDateModel dateSpinnerModel = new SpinnerDateModel();
    dateSpinnerModel.setValue(time);
    this.dateSpinner = new JSpinner(dateSpinnerModel);
    String datePattern = ((SimpleDateFormat)DateFormat.getDateInstance(DateFormat.SHORT)).toPattern();
    if (datePattern.indexOf("yyyy") == -1) {
      datePattern = datePattern.replace("yy", "yyyy");
    }
    JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(this.dateSpinner, datePattern);
    this.dateSpinner.setEditor(dateEditor);
    SwingTools.addAutoSelectionOnFocusGain(dateEditor.getTextField());
    
    this.timeLabel = new JLabel();
    final SpinnerDateModel timeSpinnerModel = new SpinnerDateModel();
    timeSpinnerModel.setValue(time);
    this.timeSpinner = new JSpinner(timeSpinnerModel);
    // From http://en.wikipedia.org/wiki/12-hour_clock#Use_by_country
    String [] twelveHoursCountries = { 
        "AU",  // Australia
        "BD",  // Bangladesh
        "CA",  // Canada (excluding Quebec, in French)
        "CO",  // Colombia
        "EG",  // Egypt
        "HN",  // Honduras
        "JO",  // Jordan
        "MX",  // Mexico
        "MY",  // Malaysia
        "NI",  // Nicaragua
        "NZ",  // New Zealand
        "PH",  // Philippines
        "PK",  // Pakistan
        "SA",  // Saudi Arabia
        "SV",  // El Salvador
        "US",  // United States
        "VE"}; // Venezuela         
    SimpleDateFormat timeInstance;
    if ("en".equals(Locale.getDefault().getLanguage())) {
      if (Arrays.binarySearch(twelveHoursCountries, Locale.getDefault().getCountry()) >= 0) {
        timeInstance = (SimpleDateFormat)DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US); // 12 hours notation
      } else {
        timeInstance = (SimpleDateFormat)DateFormat.getTimeInstance(DateFormat.SHORT, Locale.UK); // 24 hours notation
      }
    } else {
      timeInstance = (SimpleDateFormat)DateFormat.getTimeInstance(DateFormat.SHORT);
    }
    JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(this.timeSpinner, timeInstance.toPattern());
    this.timeSpinner.setEditor(timeEditor);
    SwingTools.addAutoSelectionOnFocusGain(timeEditor.getTextField());

    final PropertyChangeListener timeChangeListener = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent ev) {
        Date date = new Date(Camera.convertTimeToTimeZone(controller.getTime(), TimeZone.getDefault().getID()));
        dateSpinnerModel.setValue(date);
        timeSpinnerModel.setValue(date);
      }
    };
    controller.addPropertyChangeListener(PhotoController.Property.TIME, timeChangeListener);
    final ChangeListener dateTimeChangeListener = new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(PhotoController.Property.TIME, timeChangeListener);
          // Merge date and time
          GregorianCalendar dateCalendar = new GregorianCalendar();
          dateCalendar.setTime((Date)dateSpinnerModel.getValue());
          GregorianCalendar timeCalendar = new GregorianCalendar();
          timeCalendar.setTime((Date)timeSpinnerModel.getValue());
          Calendar utcCalendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
          utcCalendar.set(GregorianCalendar.YEAR, dateCalendar.get(GregorianCalendar.YEAR));
          utcCalendar.set(GregorianCalendar.MONTH, dateCalendar.get(GregorianCalendar.MONTH));
          utcCalendar.set(GregorianCalendar.DAY_OF_MONTH, dateCalendar.get(GregorianCalendar.DAY_OF_MONTH));
          utcCalendar.set(GregorianCalendar.HOUR_OF_DAY, timeCalendar.get(GregorianCalendar.HOUR_OF_DAY));
          utcCalendar.set(GregorianCalendar.MINUTE, timeCalendar.get(GregorianCalendar.MINUTE));
          utcCalendar.set(GregorianCalendar.SECOND, timeCalendar.get(GregorianCalendar.SECOND));
          controller.setTime(utcCalendar.getTimeInMillis());
          controller.addPropertyChangeListener(PhotoController.Property.TIME, timeChangeListener);
        }
      };
    dateSpinnerModel.addChangeListener(dateTimeChangeListener);
    timeSpinnerModel.addChangeListener(dateTimeChangeListener);

    this.dayNightLabel = new JLabel();
    final ImageIcon dayIcon = new ImageIcon(PhotoPanel.class.getResource("resources/day.png"));
    final ImageIcon nightIcon = new ImageIcon(PhotoPanel.class.getResource("resources/night.png"));
    PropertyChangeListener dayNightListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          if (home.getCompass().getSunElevation(
                Camera.convertTimeToTimeZone(controller.getTime(), home.getCompass().getTimeZone())) > 0) {
            dayNightLabel.setIcon(dayIcon);
          } else {
            dayNightLabel.setIcon(nightIcon);
          }
        }
      };
    controller.addPropertyChangeListener(PhotoController.Property.TIME, dayNightListener);
    home.getCompass().addPropertyChangeListener(dayNightListener);
    dayNightListener.propertyChange(null);
    
    // Create lens label and combo box
    this.lensLabel = new JLabel();
    this.lensComboBox = new JComboBox(Camera.Lens.values());
    this.lensComboBox.setRenderer(new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, 
                                                      int index, boolean isSelected, boolean cellHasFocus) {
          String displayedValue;
          switch ((Camera.Lens)value) {
            case NORMAL :
              displayedValue = preferences.getLocalizedString(PhotoPanel.class, "lensComboBox.normalLens.text");
              break;
            case SPHERICAL :
              displayedValue = preferences.getLocalizedString(PhotoPanel.class, "lensComboBox.sphericalLens.text");
              break;
            case FISHEYE :
              displayedValue = preferences.getLocalizedString(PhotoPanel.class, "lensComboBox.fisheyeLens.text");
              break;
            case PINHOLE :
            default :
              displayedValue = preferences.getLocalizedString(PhotoPanel.class, "lensComboBox.pinholeLens.text");
              break;
          }
          return super.getListCellRendererComponent(list, displayedValue, index, isSelected,
              cellHasFocus);
        }
      });
    this.lensComboBox.setSelectedItem(controller.getLens());
    controller.addPropertyChangeListener(PhotoController.Property.LENS, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            lensComboBox.setSelectedItem(controller.getLens());            
          }
        });
    this.lensComboBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          Camera.Lens lens = (Camera.Lens)lensComboBox.getSelectedItem();
          controller.setLens(lens);
          if (lens == Camera.Lens.SPHERICAL) {
            controller.setAspectRatio(AspectRatio.RATIO_2_1);
          } else if (lens == Camera.Lens.FISHEYE) {
            controller.setAspectRatio(AspectRatio.SQUARE_RATIO);
          }  
          updateRatioComponents();
        }
      });

    this.ceilingLightEnabledCheckBox = new JCheckBox();
    this.ceilingLightEnabledCheckBox.setSelected(controller.getCeilingLightColor() > 0);
    controller.addPropertyChangeListener(AbstractPhotoController.Property.CEILING_LIGHT_COLOR, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            ceilingLightEnabledCheckBox.setSelected(controller.getCeilingLightColor() > 0);
          }
        });
    this.ceilingLightEnabledCheckBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          controller.setCeilingLightColor(ceilingLightEnabledCheckBox.isSelected() ? 0xD0D0D0 : 0);
        }
      });
    
    final JComponent view3D = (JComponent)controller.get3DView();
    controller.set3DViewAspectRatio((float)view3D.getWidth() / view3D.getHeight());

    final ActionMap actionMap = getActionMap();
    this.createButton = new JButton(actionMap.get(ActionType.START_PHOTO_CREATION));
    this.saveButton = new JButton(actionMap.get(ActionType.SAVE_PHOTO));
    this.closeButton = new JButton(actionMap.get(ActionType.CLOSE));

    setComponentTexts(preferences);
    updateRatioComponents();
  }

  /**
   * Sets the texts of the components.
   */
  private void setComponentTexts(UserPreferences preferences) {
    this.dateLabel.setText(SwingTools.getLocalizedLabelText(preferences, 
        PhotoPanel.class, "dateLabel.text"));
    this.timeLabel.setText(SwingTools.getLocalizedLabelText(preferences, 
        PhotoPanel.class, "timeLabel.text"));
    this.lensLabel.setText(SwingTools.getLocalizedLabelText(preferences, 
        PhotoPanel.class, "lensLabel.text"));
    this.ceilingLightEnabledCheckBox.setText(SwingTools.getLocalizedLabelText(preferences, 
        PhotoPanel.class, "ceilingLightEnabledCheckBox.text"));
    this.dialogTitle = preferences.getLocalizedString(PhotoPanel.class, "createPhoto.title");
    Window window = SwingUtilities.getWindowAncestor(this);  
    if (window != null) {
      ((JDialog)window).setTitle(this.dialogTitle);
    }
    // Buttons text changes automatically through their action
  }

  /**
   * Sets components mnemonics and label / component associations.
   */
  private void setMnemonics(UserPreferences preferences) {
    if (!OperatingSystem.isMacOSX()) {
      this.dateLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          PhotoPanel.class, "dateLabel.mnemonic")).getKeyCode());
      this.dateLabel.setLabelFor(this.dateSpinner);
      this.timeLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          PhotoPanel.class, "timeLabel.mnemonic")).getKeyCode());
      this.timeLabel.setLabelFor(this.timeSpinner);
      this.lensLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          PhotoPanel.class, "lensLabel.mnemonic")).getKeyCode());
      this.lensLabel.setLabelFor(this.lensComboBox);
      this.ceilingLightEnabledCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString( 
          PhotoPanel.class, "ceilingLightEnabledCheckBox.mnemonic")).getKeyCode());
    }
  }

  /**
   * Preferences property listener bound to this panel with a weak reference to avoid
   * strong link between user preferences and this panel.  
   */
  public static class LanguageChangeListener implements PropertyChangeListener {
    private final WeakReference<PhotoPanel> photoPanel;

    public LanguageChangeListener(PhotoPanel photoPanel) {
      this.photoPanel = new WeakReference<PhotoPanel>(photoPanel);
    }

    public void propertyChange(PropertyChangeEvent ev) {
      // If photo panel was garbage collected, remove this listener from preferences
      PhotoPanel photoPanel = this.photoPanel.get();
      UserPreferences preferences = (UserPreferences)ev.getSource();
      if (photoPanel == null) {
        preferences.removePropertyChangeListener(UserPreferences.Property.LANGUAGE, this);
      } else {
        photoPanel.setComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));
        photoPanel.setComponentTexts(preferences);
        photoPanel.setMnemonics(preferences);
      }
    }
  }

  /**
   * Layouts panel components in panel with their labels. 
   */
  private void layoutComponents() {
    int labelAlignment = OperatingSystem.isMacOSX()
        ? JLabel.TRAILING
        : JLabel.LEADING;
    // Add animatedWaitLabel and photoComponent to a card panel 
    this.photoCardLayout = new CardLayout();
    this.photoPanel = new JPanel(this.photoCardLayout);
    photoPanel.add(this.photoComponent, PHOTO_CARD);
    photoPanel.add(this.animatedWaitLabel, WAIT_CARD);
    // First row
    add(this.photoPanel, new GridBagConstraints(
        0, 0, 3, 1, 1, 1, GridBagConstraints.CENTER, 
        GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));
    // Second row
    // Add a dummy label at left and right
    add(new JLabel(), new GridBagConstraints(
        0, 1, 1, 8, 0.5f, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    add(new JLabel(), new GridBagConstraints(
        2, 1, 1, 8, 0.5f, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    add(this.sizeAndQualityPanel, new GridBagConstraints(
        1, 1, 1, 1, 0, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    // Third row
    add(this.advancedComponentsSeparator, new GridBagConstraints(
        1, 2, 1, 1, 0, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.HORIZONTAL, new Insets(3, 0, 3, 0), 0, 0));
    // Forth row
    JPanel advancedPanel = new JPanel(new GridBagLayout());
    advancedPanel.add(this.dateLabel, new GridBagConstraints(
        0, 1, 1, 1, 0, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));
    this.dateLabel.setHorizontalAlignment(labelAlignment);
    advancedPanel.add(this.dateSpinner, new GridBagConstraints(
        1, 1, 1, 1, 1, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 10), 0, 0));
    advancedPanel.add(this.timeLabel, new GridBagConstraints(
        2, 1, 1, 1, 0, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));
    this.timeLabel.setHorizontalAlignment(labelAlignment);
    advancedPanel.add(this.timeSpinner, new GridBagConstraints(
        3, 1, 1, 1, 1, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));
    advancedPanel.add(this.dayNightLabel, new GridBagConstraints(
        4, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 0), 0, 0));
    // Last row
    advancedPanel.add(this.lensLabel, new GridBagConstraints(
        0, 2, 1, 1, 0, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0));
    this.lensLabel.setHorizontalAlignment(labelAlignment);
    advancedPanel.add(this.lensComboBox, new GridBagConstraints(
        1, 2, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 10), 0, 0));
    advancedPanel.add(this.ceilingLightEnabledCheckBox, new GridBagConstraints(
        2, 2, 3, 1, 0, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));    
    add(advancedPanel, new GridBagConstraints(
        1, 3, 1, 1, 0, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
  }
  
  private void updateAdvancedComponents() {
    Component root = SwingUtilities.getRoot(this);
    if (root != null) {
      boolean highQuality = controller.getQuality() >= 2;
      boolean advancedComponentsVisible = this.advancedComponentsSeparator.isVisible();
      if (advancedComponentsVisible != highQuality) {
        setAdvancedComponentsVisible(highQuality);
        int componentsHeight = this.advancedComponentsSeparator.getPreferredSize().height + 6
            + this.dateSpinner.getPreferredSize().height + 5
            + this.lensComboBox.getPreferredSize().height;
        root.setSize(root.getWidth(), 
            root.getHeight() + (advancedComponentsVisible ? -componentsHeight : componentsHeight));
      }
    }   
  }

  private void setAdvancedComponentsVisible(boolean visible) {
    this.advancedComponentsSeparator.setVisible(visible);
    this.dateLabel.setVisible(visible);
    this.dateSpinner.setVisible(visible);
    this.timeLabel.setVisible(visible);
    this.timeSpinner.setVisible(visible);
    this.dayNightLabel.setVisible(visible);
    this.lensLabel.setVisible(visible);
    this.lensComboBox.setVisible(visible);
    this.ceilingLightEnabledCheckBox.setVisible(visible);
  }

  /**
   * Updates photo height.  
   */
  private void updateRatioComponents() {
    Lens lens = this.controller.getLens();
    boolean fixedProportions = lens == Camera.Lens.FISHEYE 
        || lens == Camera.Lens.SPHERICAL;
    this.sizeAndQualityPanel.setProportionsChoiceEnabled(!fixedProportions);
  }

  /**
   * Displays this panel in a non modal dialog.
   */
  public void displayView(View parentView) {
    if (currentPhotoPanel == this) {
      SwingUtilities.getWindowAncestor(PhotoPanel.this).toFront();
    } else {
      if (currentPhotoPanel != null) {
        currentPhotoPanel.close();
      }
      final JOptionPane optionPane = new JOptionPane(this, 
          JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION,
          null, new Object [] {this.createButton, this.saveButton, this.closeButton}, this.createButton);
      if (parentView != null) {
        optionPane.setComponentOrientation(((JComponent)parentView).getComponentOrientation());
      }
      final JDialog dialog = optionPane.createDialog(SwingUtilities.getRootPane((Component)parentView), this.dialogTitle);
      dialog.setModal(false);
     
      Component homeRoot = SwingUtilities.getRoot((Component)parentView);
      Point dialogLocation = null;
      if (homeRoot != null) {
        // Restore location if it exists
        Integer x = (Integer)this.home.getVisualProperty(PHOTO_DIALOG_X_VISUAL_PROPERTY);
        Integer y = (Integer)this.home.getVisualProperty(PHOTO_DIALOG_Y_VISUAL_PROPERTY);      

        int windowRightBorder = homeRoot.getX() + homeRoot.getWidth();
        Dimension screenSize = getToolkit().getScreenSize();
        Insets screenInsets = getToolkit().getScreenInsets(getGraphicsConfiguration());
        int screenRightBorder = screenSize.width - screenInsets.right;
        // Check dialog isn't too high
        int screenHeight = screenSize.height - screenInsets.top - screenInsets.bottom;
        if (OperatingSystem.isLinux() && screenHeight == screenSize.height) {
          // Let's consider that under Linux at least an horizontal bar exists 
          screenHeight -= 30;
        }
        int screenBottomBorder = screenSize.height - screenInsets.bottom;
        int dialogWidth = dialog.getWidth();
        if (dialog.getHeight() > screenHeight) {
          dialog.setSize(dialogWidth, screenHeight);
        }
        int dialogHeight = dialog.getHeight();
        if (x != null && y != null 
            && x + dialogWidth <= screenRightBorder
            && y + dialogHeight <= screenBottomBorder) {
          dialogLocation = new Point(x, y);
        } else if (screenRightBorder - windowRightBorder > dialogWidth / 2
                   || dialogHeight == screenHeight) {
          // If there some space left at the right of the window,
          // move the dialog to the right of window
          dialogLocation = new Point(Math.min(windowRightBorder + 5, screenRightBorder - dialogWidth), 
              Math.max(Math.min(homeRoot.getY(), screenSize.height - dialogHeight - screenInsets.bottom), screenInsets.top));
        }
      }
      if (dialogLocation != null
          && SwingTools.isRectangleVisibleAtScreen(new Rectangle(dialogLocation, dialog.getSize()))) {
        dialog.setLocationByPlatform(false);
        dialog.setLocation(dialogLocation);
      } else {
        dialog.setLocationByPlatform(true);
      }
      
      dialog.addWindowListener(new WindowAdapter() {
          public void windowClosed(WindowEvent ev) {
            stopPhotoCreation(false);
            currentPhotoPanel = null;
          }
        });

      updateAdvancedComponents();
      dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
      dialog.addComponentListener(new ComponentAdapter() {
          @Override
          public void componentHidden(ComponentEvent ev) {
            if (optionPane.getValue() != null
                && optionPane.getValue() != JOptionPane.UNINITIALIZED_VALUE) {
              close();
            }
          }

          @Override
          public void componentMoved(ComponentEvent ev) {
            controller.setVisualProperty(PHOTO_DIALOG_X_VISUAL_PROPERTY, dialog.getX());
            controller.setVisualProperty(PHOTO_DIALOG_Y_VISUAL_PROPERTY, dialog.getY());
          }
        });
      dialog.setVisible(true);
      currentPhotoPanel = this;
    }
  }

  /**
   * Creates the photo image depending on the quality requested by the user.
   */
  private void startPhotoCreation() {
    this.photoComponent.setImage(null);
    this.sizeAndQualityPanel.setEnabled(false);
    this.dateSpinner.setEnabled(false);
    this.timeSpinner.setEnabled(false);
    this.lensComboBox.setEnabled(false);
    this.ceilingLightEnabledCheckBox.setEnabled(false);
    getActionMap().get(ActionType.SAVE_PHOTO).setEnabled(false);
    getRootPane().setDefaultButton(this.createButton);
    this.createButton.setAction(getActionMap().get(ActionType.STOP_PHOTO_CREATION));
    this.photoCardLayout.show(this.photoPanel, WAIT_CARD);
    
    // Compute photo in an other executor thread
    // Use a clone of home because the user can modify home during photo computation
    final Home home = this.home.clone();
    List<Selectable> emptySelection = Collections.emptyList();
    home.setSelectedItems(emptySelection);
    this.photoCreationExecutor = Executors.newSingleThreadExecutor();
    this.photoCreationExecutor.execute(new Runnable() {
        public void run() {
          computePhoto(home);
        }
      });
  }

  /**
   * Computes the photo of the given home.
   * Caution : this method must be thread safe because it's called from an executor. 
   */
  private void computePhoto(Home home) {
    this.photoCreationStartTime = System.currentTimeMillis();
    BufferedImage image = null;
    try {
      int quality = this.controller.getQuality();
      int imageWidth = this.controller.getWidth();
      int imageHeight = this.controller.getHeight();
      if (quality >= 2) {
        // Use photo renderer
        PhotoRenderer photoRenderer = new PhotoRenderer(home, 
            quality == 2 
                ? PhotoRenderer.Quality.LOW 
                : PhotoRenderer.Quality.HIGH);
        int bestImageHeight;
        // Check correct ratio if lens is fisheye or spherical
        Camera camera = home.getCamera();
        if (camera.getLens() == Camera.Lens.FISHEYE) {
          bestImageHeight = imageWidth;
        } else if (camera.getLens() == Camera.Lens.SPHERICAL) {
          bestImageHeight = imageWidth * 2;
        } else {           
          bestImageHeight = imageHeight;
        }
        if (photoCreationExecutor != null) {
          image = new BufferedImage(imageWidth, bestImageHeight, BufferedImage.TYPE_INT_RGB);
          this.photoComponent.setImage(image);
          EventQueue.invokeLater(new Runnable() {
            public void run() {
              photoCardLayout.show(photoPanel, PHOTO_CARD);
            }
          });
          photoRenderer.render(image, camera, this.photoComponent);
        }
      } else {
        // Compute 3D view offscreen image
        HomeComponent3D homeComponent3D = new HomeComponent3D(home, this.preferences, quality == 1);
        image = homeComponent3D.getOffScreenImage(imageWidth, imageHeight);
      }
    } catch (OutOfMemoryError ex) {
      image = getErrorImage();
      throw ex;
    } catch (IllegalStateException ex) {
      image = getErrorImage();
      throw ex;
    } catch (IOException ex) {
      image = getErrorImage();
    } finally {           
      final BufferedImage photoImage = this.photoCreationExecutor != null
          ? image
          : null;
      EventQueue.invokeLater(new Runnable() {
          public void run() {
            getActionMap().get(ActionType.SAVE_PHOTO).setEnabled(photoImage != null);
            if (photoImage != null) {
              getRootPane().setDefaultButton(saveButton);
            }
            createButton.setAction(getActionMap().get(ActionType.START_PHOTO_CREATION));
            photoComponent.setImage(photoImage);
            sizeAndQualityPanel.setEnabled(true);
            updateRatioComponents();
            dateSpinner.setEnabled(true);
            timeSpinner.setEnabled(true);
            lensComboBox.setEnabled(true);
            ceilingLightEnabledCheckBox.setEnabled(true);
            photoCardLayout.show(photoPanel, PHOTO_CARD);
            photoCreationExecutor = null;
          }
        });
    }
  }
  
  /**
   * Returns the image used in case of an error.
   */
  private BufferedImage getErrorImage() {
    Icon errorIcon = IconManager.getInstance().getErrorIcon(16);
    BufferedImage errorImage = new BufferedImage(
        errorIcon.getIconWidth(), errorIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2D = (Graphics2D)errorImage.getGraphics();
    errorIcon.paintIcon(this, g2D, 0, 0);
    g2D.dispose();
    return errorImage;
  }
  
  /**
   * Stops photo creation.
   */
  private void stopPhotoCreation(boolean confirmStop) {
    if (this.photoCreationExecutor != null
        // Confirm the stop if a rendering has been running for more than 30 s 
        && (!confirmStop
            || System.currentTimeMillis() - this.photoCreationStartTime < MINIMUM_DELAY_BEFORE_DISCARDING_WITHOUT_WARNING
            || JOptionPane.showConfirmDialog(getRootPane(), 
                  this.preferences.getLocalizedString(PhotoPanel.class, "confirmStopCreation.message"),
                  this.preferences.getLocalizedString(PhotoPanel.class, "confirmStopCreation.title"), 
                  JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION)) {
      if (this.photoCreationExecutor != null) { // Check a second time in case rendering stopped meanwhile
        // Will interrupt executor thread      
        this.photoCreationExecutor.shutdownNow();
        this.photoCreationExecutor = null;
        this.createButton.setAction(getActionMap().get(ActionType.START_PHOTO_CREATION));
      }
    }
  }

  /**
   * Saves the created image.
   */
  private void savePhoto() {
    String pngFile = this.controller.getContentManager().showSaveDialog(this,
        this.preferences.getLocalizedString(PhotoPanel.class, "savePhotoDialog.title"), 
        ContentManager.ContentType.PNG, this.home.getName());
    try {
      if (pngFile != null) {
        ImageIO.write(this.photoComponent.getImage(), "PNG", new File(pngFile));
      }
    } catch (IOException ex) {
      String messageFormat = this.preferences.getLocalizedString(PhotoPanel.class, "savePhotoError.message");
      JOptionPane.showMessageDialog(SwingUtilities.getRootPane(this), String.format(messageFormat, ex.getMessage()), 
          this.preferences.getLocalizedString(PhotoPanel.class, "savePhotoError.title"), JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Manages closing of this pane.
   */
  private void close() {
    Window window = SwingUtilities.getWindowAncestor(this);
    if (window.isDisplayable()) {
      window.dispose();
    }    
  }
}
