/*
 * PhotoPanel.java 5 mai 2009
 *
 * Copyright (c) 2009 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.eteks.sweethome3d.j3d.Component3DManager;
import com.eteks.sweethome3d.j3d.PhotoRenderer;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.ContentManager;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.PhotoController;
import com.eteks.sweethome3d.viewcontroller.View;

/**
 * A panel to edit photo creation. 
 * @author Emmanuel Puybaret
 */
public class PhotoPanel extends JPanel implements DialogView {
  private enum ActionType {CREATE, SAVE, CLOSE}

  private static final String WAIT_CARD  = "wait";
  private static final String PHOTO_CARD = "photo";
  
  private final Home            home;
  private final UserPreferences preferences;
  private final PhotoController controller;
  private ScaledImageComponent  photoComponent; 
  private JLabel                animatedWaitLabel;
  private JLabel                widthLabel;
  private JSpinner              widthSpinner;
  private JLabel                heightLabel;
  private JSpinner              heightSpinner;
  private JCheckBox             use3DViewProportionsCheckBox;
  private JLabel                qualityLabel;
  private JSlider               qualitySlider;
  private String                dialogTitle;
  private JPanel                photoPanel;
  private CardLayout            photoCardLayout;
  private ExecutorService       photoCreationExecutor;
  private PhotoRenderer         photoRenderer;
  
  private static PhotoPanel     currentPhotoPanel; // There can be only one photo panel opened at a time

  public PhotoPanel(Home home, 
                    UserPreferences preferences, 
                    PhotoController controller) {
    super(new GridBagLayout());
    this.home = home;
    this.preferences = preferences;
    this.controller = controller;
    createActions(preferences);
    createComponents(preferences, controller);
    setMnemonics(preferences);
    layoutComponents();    

    preferences.addPropertyChangeListener(UserPreferences.Property.LANGUAGE, new LanguageChangeListener(this));
  }
  
  /**
   * Creates actions for variables.
   */
  private void createActions(UserPreferences preferences) {
    ActionMap actions = getActionMap();
    actions.put(ActionType.CREATE, 
        new ResourceAction(preferences, PhotoPanel.class, ActionType.CREATE.name(), true) {
          @Override
          public void actionPerformed(ActionEvent ev) {
            createPhoto();
          }
        });
    actions.put(ActionType.SAVE, 
        new ResourceAction(preferences, PhotoPanel.class, ActionType.SAVE.name(), false) {
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
  private void createComponents(UserPreferences preferences,
                                final PhotoController controller) {
    this.photoComponent = new ScaledImageComponent();
    this.photoComponent.setPreferredSize(new Dimension(400, 400));
    this.photoComponent.setBorder(null);

    this.animatedWaitLabel = new JLabel(new ImageIcon(PhotoPanel.class.getResource("resources/animatedWait.gif")));

    // Create observer width label and spinner bound to WIDTH controller property
    this.widthLabel = new JLabel();
    final SpinnerNumberModel widthSpinnerModel = new SpinnerNumberModel(480, 10, 3000, 10);
    this.widthSpinner = new AutoCommitSpinner(widthSpinnerModel);
    widthSpinnerModel.setValue(controller.getWidth());
    widthSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.setWidth(((Number)widthSpinnerModel.getValue()).intValue());
        }
      });
    controller.addPropertyChangeListener(PhotoController.Property.WIDTH, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            widthSpinnerModel.setValue(controller.getWidth());
          }
        });

    // Create observer height label and spinner bound to HEIGHT controller property
    this.heightLabel = new JLabel();
    final SpinnerNumberModel heightSpinnerModel = new SpinnerNumberModel(480, 10, 3000, 10);
    this.heightSpinner = new AutoCommitSpinner(heightSpinnerModel);
    heightSpinnerModel.setValue(controller.getHeight());
    heightSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.setHeight(((Number)heightSpinnerModel.getValue()).intValue());
        }
      });
    controller.addPropertyChangeListener(PhotoController.Property.HEIGHT, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            heightSpinnerModel.setValue(controller.getHeight());
          }
        });
    
    // Keep proportions check box bound to PROPORTIONAL controller property
    this.use3DViewProportionsCheckBox = new JCheckBox();
    this.use3DViewProportionsCheckBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          controller.setProportional(use3DViewProportionsCheckBox.isSelected());
        }
      });
    controller.addPropertyChangeListener(PhotoController.Property.PROPORTIONAL,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            // If proportional property changes update check box
            use3DViewProportionsCheckBox.setSelected(controller.isProportional());
          }
        });

    // Quality label and slider bound to QUALITY controller property
    this.qualityLabel = new JLabel();
    this.qualitySlider = new JSlider(0, 3);
    this.qualitySlider.setPaintLabels(true);
    this.qualitySlider.setPaintTicks(true);    
    this.qualitySlider.setMajorTickSpacing(1);
    this.qualitySlider.setSnapToTicks(true);
    final boolean offScreenImageSupported = Component3DManager.getInstance().isOffScreenImageSupported();
    if (offScreenImageSupported) {
      this.qualitySlider.setValue(controller.getQuality());
    } else {
      // Can't support 2 first quality levels if offscreen image isn't supported 
      this.qualitySlider.setValue(Math.max(2, controller.getQuality()));
    }
    this.qualitySlider.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (!offScreenImageSupported) {
            qualitySlider.setValue(Math.max(2, controller.getQuality()));
          }
          controller.setQuality(qualitySlider.getValue());
        }
      });
    controller.addPropertyChangeListener(PhotoController.Property.QUALITY, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            qualitySlider.setValue(controller.getQuality());
          }
        });
    
    setComponentTexts(preferences);
  }

  /**
   * Sets the texts of the components.
   */
  private void setComponentTexts(UserPreferences preferences) {
    this.widthLabel.setText(SwingTools.getLocalizedLabelText(preferences, 
        PhotoPanel.class, "widthLabel.text"));
    this.heightLabel.setText(SwingTools.getLocalizedLabelText(preferences, 
        PhotoPanel.class, "heightLabel.text"));
    this.use3DViewProportionsCheckBox.setText(SwingTools.getLocalizedLabelText(preferences, 
        PhotoPanel.class, "use3DViewProportionsCheckBox.text"));
    this.qualityLabel.setText(SwingTools.getLocalizedLabelText(preferences, 
        PhotoPanel.class, "qualityLabel.text"));
    JLabel fastLabel = new JLabel(preferences.getLocalizedString(
        PhotoPanel.class, "fastLabel.text"));
    JLabel bestLabel = new JLabel(preferences.getLocalizedString(
        PhotoPanel.class, "bestLabel.text"));
    Dictionary<Integer,JComponent> qualitySliderLabelTable = new Hashtable<Integer,JComponent>();
    qualitySliderLabelTable.put(this.qualitySlider.getMinimum(), fastLabel);
    qualitySliderLabelTable.put(this.qualitySlider.getMaximum(), bestLabel);
    this.qualitySlider.setLabelTable(qualitySliderLabelTable);
    this.dialogTitle = preferences.getLocalizedString(PhotoPanel.class, "createPhoto.title");
    Window window = SwingUtilities.getWindowAncestor(this);  
    if (window != null) {
      ((JDialog)window).setTitle(this.dialogTitle);
    }
  }

  /**
   * Sets components mnemonics and label / component associations.
   */
  private void setMnemonics(UserPreferences preferences) {
    if (!OperatingSystem.isMacOSX()) {
      this.widthLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(preferences.getLocalizedString(
              PhotoPanel.class, "widthLabel.mnemonic")).getKeyCode());
      this.widthLabel.setLabelFor(this.widthSpinner);
      this.heightLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(preferences.getLocalizedString(
              PhotoPanel.class, "heightLabel.mnemonic")).getKeyCode());
      this.heightLabel.setLabelFor(this.heightSpinner);
      this.use3DViewProportionsCheckBox.setMnemonic(
          KeyStroke.getKeyStroke(preferences.getLocalizedString(
              PhotoPanel.class, "use3DViewProportionsCheckBox.mnemonic")).getKeyCode());
      this.qualityLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(preferences.getLocalizedString(
              PhotoPanel.class, "qualityLabel.mnemonic")).getKeyCode());
      this.qualityLabel.setLabelFor(this.qualitySlider);
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
        ? GridBagConstraints.LINE_END
        : GridBagConstraints.LINE_START;
    // Add animatedWaitLabel and photoComponent to a card panel 
    this.photoCardLayout = new CardLayout();
    this.photoPanel = new JPanel(this.photoCardLayout);
    photoPanel.add(this.photoComponent, PHOTO_CARD);
    photoPanel.add(this.animatedWaitLabel, WAIT_CARD);
    photoPanel.setBorder(BorderFactory.createEtchedBorder());
    // First row
    add(photoPanel, new GridBagConstraints(
        0, 0, 6, 1, 1, 1, labelAlignment, 
        GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));
    // Second row
    // Add a dummy label at left and right
    add(new JLabel(), new GridBagConstraints(
        0, 1, 1, 3, 0.5f, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    add(new JLabel(), new GridBagConstraints(
        5, 1, 1, 3, 0.5f, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    Insets labelInsets = new Insets(0, 0, 5, 5);
    add(this.widthLabel, new GridBagConstraints(
        1, 1, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    Insets componentInsets = new Insets(0, 0, 5, 10);
    add(this.widthSpinner, new GridBagConstraints(
        2, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, componentInsets, 0, 0));
    add(this.heightLabel, new GridBagConstraints(
        3, 1, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    add(this.heightSpinner, new GridBagConstraints(
        4, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
    // Third row
    add(this.use3DViewProportionsCheckBox, new GridBagConstraints(
        1, 2, 4, 1, 0, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.NONE, new Insets(0, 0, 10, 0), 0, 0));
    // Last row
    add(this.qualityLabel, new GridBagConstraints(
        1, 3, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
    add(this.qualitySlider, new GridBagConstraints(
        2, 3, 3, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
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
      ActionMap actionMap = getActionMap();
      JButton createButton = new JButton(actionMap.get(ActionType.CREATE));
      JButton saveButton = new JButton(actionMap.get(ActionType.SAVE));
      JButton closeButton = new JButton(actionMap.get(ActionType.CLOSE));
      
      final JOptionPane optionPane = new JOptionPane(this, 
          JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION,
          null, new Object [] {createButton, saveButton, closeButton}, createButton);
      final JDialog dialog = optionPane.createDialog((Component)parentView, this.dialogTitle);
      dialog.setModal(false);
      dialog.addComponentListener(new ComponentAdapter() {
          @Override
          public void componentHidden(ComponentEvent ev) {
            if (optionPane.getValue() != null
                && optionPane.getValue() != JOptionPane.UNINITIALIZED_VALUE) {
              close();
            }
          }
        });
      
      dialog.setLocationByPlatform(true);
      Window homeWindow = SwingUtilities.getWindowAncestor((Component)parentView);
      if (homeWindow != null) {
        int windowRightBorder = homeWindow.getX() + homeWindow.getWidth();
        Dimension screenSize = getToolkit().getScreenSize();
        Insets screenInsets = getToolkit().getScreenInsets(getGraphicsConfiguration());
        int screenRightBorder = screenSize.width - screenInsets.right;
        int dialogWidth = dialog.getWidth();
        // If there some space left at the right of the window
        if (screenRightBorder - windowRightBorder > dialogWidth / 2) {
          // Move the dialog to the right of window
          dialog.setLocationByPlatform(false);
          dialog.setLocation(Math.min(windowRightBorder + 5, screenRightBorder - dialogWidth), 
              homeWindow.getY());
        }
      }
      dialog.setVisible(true);
      currentPhotoPanel = this;
    }
  }

  /**
   * Creates the photo image depending on the quality requested by the user.
   */
  private void createPhoto() {
    this.photoComponent.setImage(null);
    getActionMap().get(ActionType.SAVE).setEnabled(false);
    getActionMap().get(ActionType.CREATE).setEnabled(false);
    this.photoCardLayout.show(this.photoPanel, WAIT_CARD);
    
    this.photoCreationExecutor = Executors.newSingleThreadExecutor();
    this.photoCreationExecutor.execute(new Runnable() {
        public void run() {
          computePhoto();
        }
      });
  }

  /**
   * Computes the photo.
   */
  private void computePhoto() {
    BufferedImage image = null;
    try {
      int quality = this.controller.getQuality();
      if (quality >= 2) {
        // Use photo renderer
        this.photoRenderer = new PhotoRenderer(this.home, quality == 2 
            ? PhotoRenderer.Quality.LOW 
            : PhotoRenderer.Quality.HIGH);
        if (!Thread.interrupted()) {
          image = new BufferedImage(this.controller.getWidth(), 
              this.controller.getHeight(), BufferedImage.TYPE_INT_RGB);
          this.photoComponent.setImage(image);
          EventQueue.invokeLater(new Runnable() {
            public void run() {
              photoCardLayout.show(photoPanel, PHOTO_CARD);
            }
          });
          this.photoRenderer.render(image, this.home.getCamera(), this.photoComponent);
        }
        this.photoRenderer = null;
      } else {
        // Compute 3D view offscreen image
        HomeComponent3D homeComponent3D = new HomeComponent3D(this.home, this.preferences, quality == 1);
        image = homeComponent3D.getOffScreenImage(
            this.controller.getWidth(), this.controller.getHeight());
      }
    } catch (OutOfMemoryError ex) {
      image = getErrorImage();
      throw ex;
    } catch (IOException ex) {
      image = getErrorImage();
    } finally {           
      final BufferedImage photoImage = image;
      EventQueue.invokeLater(new Runnable() {
          public void run() {
            photoComponent.setImage(photoImage);
            photoCardLayout.show(photoPanel, PHOTO_CARD);
            getActionMap().get(ActionType.SAVE).setEnabled(photoImage != null);
            getActionMap().get(ActionType.CREATE).setEnabled(true);
            photoCreationExecutor = null;
          }
        });
    }
  }
  
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
   * Saves the created image.
   */
  private void savePhoto() {
    String pngFile = this.controller.getContentManager().showSaveDialog(this,
        this.preferences.getLocalizedString(PhotoPanel.class, "savePhotoDialog.title"), 
        ContentManager.ContentType.PNG, this.home.getName());
    try {
      ImageIO.write(this.photoComponent.getImage(), "PNG", new File(pngFile));
    } catch (IOException ex) {
      String messageFormat = this.preferences.getLocalizedString(PhotoPanel.class, "savePhotoError.message");
      JOptionPane.showMessageDialog(this, String.format(messageFormat, ex.getMessage()), 
          this.preferences.getLocalizedString(PhotoPanel.class, "savePhotoError.title"), JOptionPane.ERROR_MESSAGE);
    }
  }

  private void close() {
    SwingUtilities.getWindowAncestor(this).dispose();
    
    if (this.photoCreationExecutor != null) {
      this.photoCreationExecutor.shutdownNow();
      this.photoCreationExecutor = null;

      if (this.photoRenderer != null) {
        this.photoRenderer.stop();
        this.photoRenderer = null;
      }
    }
    currentPhotoPanel = null;
  }
}
