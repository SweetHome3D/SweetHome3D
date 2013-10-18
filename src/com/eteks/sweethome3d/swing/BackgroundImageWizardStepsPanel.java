/*
 * BackgroundImageWizardStepsPanel.java 8 juin 07
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

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;

import com.eteks.sweethome3d.model.BackgroundImage;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.tools.TemporaryURLContent;
import com.eteks.sweethome3d.viewcontroller.BackgroundImageWizardController;
import com.eteks.sweethome3d.viewcontroller.ContentManager;
import com.eteks.sweethome3d.viewcontroller.View;

/**
 * Wizard panel for background image choice. 
 * @author Emmanuel Puybaret
 */
public class BackgroundImageWizardStepsPanel extends JPanel implements View {
  private final BackgroundImageWizardController controller;
  private final Executor                        imageLoader;
  private CardLayout                            cardLayout;
  private JLabel                                imageChoiceOrChangeLabel;
  private JButton                               imageChoiceOrChangeButton;
  private JLabel                                imageChoiceErrorLabel;
  private ScaledImageComponent                  imageChoicePreviewComponent;
  private JLabel                                scaleLabel;
  private JLabel                                scaleDistanceLabel;
  private JSpinner                              scaleDistanceSpinner;
  private ScaledImageComponent                  scalePreviewComponent;
  private JLabel                                originLabel;
  private JLabel                                xOriginLabel;
  private JSpinner                              xOriginSpinner;
  private JLabel                                yOriginLabel;
  private JSpinner                              yOriginSpinner;
  private ScaledImageComponent                  originPreviewComponent;
  private static BufferedImage                  waitImage;

  /**
   * Creates a view for background image choice, scale and origin. 
   */
  public BackgroundImageWizardStepsPanel(BackgroundImage backgroundImage, 
                                         UserPreferences preferences, 
                                         final BackgroundImageWizardController controller) {
    this.controller = controller;
    this.imageLoader = Executors.newSingleThreadExecutor();
    createComponents(preferences, controller);
    setMnemonics(preferences);
    layoutComponents(preferences);
    updateController(backgroundImage, preferences);

    controller.addPropertyChangeListener(BackgroundImageWizardController.Property.STEP, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent evt) {
            updateStep(controller);
          }
        });
  }

  /**
   * Creates components displayed by this panel.
   */
  private void createComponents(final UserPreferences preferences, 
                                final BackgroundImageWizardController controller) {
    // Get unit name matching current unit 
    String unitName = preferences.getLengthUnit().getName();

    // Image choice panel components
    this.imageChoiceOrChangeLabel = new JLabel(); 
    this.imageChoiceOrChangeButton = new JButton();
    this.imageChoiceOrChangeButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          String imageName = showImageChoiceDialog(preferences, controller.getContentManager());
          if (imageName != null) {
            updateController(imageName, preferences, controller.getContentManager());
          }
        }
      });
    this.imageChoiceErrorLabel = new JLabel(preferences.getLocalizedString(
        BackgroundImageWizardStepsPanel.class, "imageChoiceErrorLabel.text"));
    // Make imageChoiceErrorLabel visible only if an error occurred during image content loading
    this.imageChoiceErrorLabel.setVisible(false);
    this.imageChoicePreviewComponent = new ScaledImageComponent();
    // Add a transfer handler to image preview component to let user drag and drop an image in component
    this.imageChoicePreviewComponent.setTransferHandler(new TransferHandler() {
        @Override
        public boolean canImport(JComponent comp, DataFlavor [] flavors) {
          return Arrays.asList(flavors).contains(DataFlavor.javaFileListFlavor);
        }
        
        @Override
        public boolean importData(JComponent comp, Transferable transferedFiles) {
          boolean success = true;
          try {
            List<File> files = (List<File>)transferedFiles.getTransferData(DataFlavor.javaFileListFlavor);
            final String imageName = files.get(0).getAbsolutePath();
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                  updateController(imageName, preferences, controller.getContentManager());
                }
              });
          } catch (UnsupportedFlavorException ex) {
            success = false;
          } catch (IOException ex) {
            success = false;
          }
          if (!success) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                  JOptionPane.showMessageDialog(SwingUtilities.getRootPane(BackgroundImageWizardStepsPanel.this), 
                      preferences.getLocalizedString(BackgroundImageWizardStepsPanel.class, "imageChoiceError"));
                }
              });
          }
          return success;
        }
      });
    this.imageChoicePreviewComponent.setBorder(SwingTools.getDropableComponentBorder());
    
    // Image scale panel components
    this.scaleLabel = new JLabel(preferences.getLocalizedString(
        BackgroundImageWizardStepsPanel.class, "scaleLabel.text"));
    this.scaleDistanceLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        BackgroundImageWizardStepsPanel.class, "scaleDistanceLabel.text", unitName));
    final float maximumLength = preferences.getLengthUnit().getMaximumLength();
    final NullableSpinner.NullableSpinnerLengthModel scaleDistanceSpinnerModel = 
        new NullableSpinner.NullableSpinnerLengthModel(preferences, preferences.getLengthUnit().getMinimumLength(), maximumLength);
    this.scaleDistanceSpinner = new NullableSpinner(scaleDistanceSpinnerModel);
    this.scaleDistanceSpinner.getModel().addChangeListener(new ChangeListener () {
        public void stateChanged(ChangeEvent ev) {
          // If spinner value changes update controller
          controller.setScaleDistance(
              ((NullableSpinner.NullableSpinnerLengthModel)scaleDistanceSpinner.getModel()).getLength());
        }
      });
    controller.addPropertyChangeListener(BackgroundImageWizardController.Property.SCALE_DISTANCE,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            // If scale distance changes updates scale spinner
            scaleDistanceSpinnerModel.setNullable(controller.getScaleDistance() == null);
            scaleDistanceSpinnerModel.setLength(controller.getScaleDistance());
          }
        });
    this.scalePreviewComponent = new ScaleImagePreviewComponent(controller);
    
    // Image origin panel components
    this.originLabel = new JLabel(preferences.getLocalizedString(
        BackgroundImageWizardStepsPanel.class, "originLabel.text"));
    this.xOriginLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
            BackgroundImageWizardStepsPanel.class, "xOriginLabel.text", unitName)); 
    this.yOriginLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
            BackgroundImageWizardStepsPanel.class, "yOriginLabel.text", unitName)); 
    final NullableSpinner.NullableSpinnerLengthModel xOriginSpinnerModel = 
        new NullableSpinner.NullableSpinnerLengthModel(preferences, controller.getXOrigin(), -maximumLength, maximumLength);
    this.xOriginSpinner = new NullableSpinner(xOriginSpinnerModel);
    final NullableSpinner.NullableSpinnerLengthModel yOriginSpinnerModel = 
        new NullableSpinner.NullableSpinnerLengthModel(preferences, controller.getYOrigin(), -maximumLength, maximumLength);
    this.yOriginSpinner = new NullableSpinner(yOriginSpinnerModel);
    ChangeListener originSpinnersListener = new ChangeListener () {
        public void stateChanged(ChangeEvent ev) {
          // If origin spinners value changes update controller
          controller.setOrigin(xOriginSpinnerModel.getLength(), yOriginSpinnerModel.getLength());
        }
      };
    xOriginSpinnerModel.addChangeListener(originSpinnersListener);
    yOriginSpinnerModel.addChangeListener(originSpinnersListener);
    controller.addPropertyChangeListener(BackgroundImageWizardController.Property.X_ORIGIN, 
        new PropertyChangeListener () {
          public void propertyChange(PropertyChangeEvent ev) {
            // If origin values changes update x origin spinner
            xOriginSpinnerModel.setLength(controller.getXOrigin());
          }
        });
    controller.addPropertyChangeListener(BackgroundImageWizardController.Property.Y_ORIGIN, 
        new PropertyChangeListener () {
          public void propertyChange(PropertyChangeEvent ev) {
            // If origin values changes update y origin spinner
            yOriginSpinnerModel.setLength(controller.getYOrigin());
          }
        });
    
    this.originPreviewComponent = new OriginImagePreviewComponent(controller);
  }

  /**
   * Sets components mnemonics and label / component associations.
   */
  private void setMnemonics(UserPreferences preferences) {
    if (!OperatingSystem.isMacOSX()) {
      this.scaleDistanceLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(preferences.getLocalizedString(
              BackgroundImageWizardStepsPanel.class, "scaleDistanceLabel.mnemonic")).getKeyCode());
      this.scaleDistanceLabel.setLabelFor(this.scaleDistanceSpinner);
      this.xOriginLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(preferences.getLocalizedString(
              BackgroundImageWizardStepsPanel.class, "xOriginLabel.mnemonic")).getKeyCode());
      this.xOriginLabel.setLabelFor(this.xOriginSpinner);
      this.yOriginLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(preferences.getLocalizedString(
              BackgroundImageWizardStepsPanel.class, "yOriginLabel.mnemonic")).getKeyCode());
      this.yOriginLabel.setLabelFor(this.yOriginSpinner);
    }
  }
  
  /**
   * Layouts components in 3 panels added to this panel as cards. 
   */
  private void layoutComponents(UserPreferences preferences) {
    this.cardLayout = new CardLayout();
    setLayout(this.cardLayout);
    
    JPanel imageChoiceTopPanel = new JPanel(new GridBagLayout());
    imageChoiceTopPanel.add(this.imageChoiceOrChangeLabel, new GridBagConstraints(
        0, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 0), 0, 0));
    imageChoiceTopPanel.add(this.imageChoiceOrChangeButton, new GridBagConstraints(
        0, 1, 1, 1, 0, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    imageChoiceTopPanel.add(this.imageChoiceErrorLabel, new GridBagConstraints(
        0, 2, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(5, 0, 0, 0), 0, 0));
    
    JPanel imageChoicePanel = new JPanel(new ProportionalLayout());
    imageChoicePanel.add(imageChoiceTopPanel, ProportionalLayout.Constraints.TOP);
    imageChoicePanel.add(this.imageChoicePreviewComponent, ProportionalLayout.Constraints.BOTTOM);
    
    JPanel scalePanel = new JPanel(new GridBagLayout());
    scalePanel.add(this.scaleLabel, new GridBagConstraints(
        0, 0, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 0), 0, 0));
    scalePanel.add(this.scaleDistanceLabel, new GridBagConstraints(
        0, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 0, 10, 5), 0, 0));
    scalePanel.add(this.scaleDistanceSpinner, new GridBagConstraints(
        1, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 0, 10, 0), 0, 0));
    scalePanel.add(createScalableImageComponent(this.scalePreviewComponent, preferences), new GridBagConstraints(
        0, 2, 2, 1, 1, 1, GridBagConstraints.CENTER, 
        GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

    JPanel originPanel = new JPanel(new GridBagLayout());
    originPanel.add(this.originLabel, new GridBagConstraints(
        0, 0, 4, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 0), 0, 0));
    originPanel.add(this.xOriginLabel, new GridBagConstraints(
        0, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
    originPanel.add(this.xOriginSpinner, new GridBagConstraints(
        1, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 10), -10, 0));
    originPanel.add(this.yOriginLabel, new GridBagConstraints(
        2, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
    originPanel.add(this.yOriginSpinner, new GridBagConstraints(
        3, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 0), -10, 0));
    originPanel.add(createScalableImageComponent(this.originPreviewComponent, preferences), new GridBagConstraints(
        0, 2, 4, 1, 1, 1, GridBagConstraints.CENTER, 
        GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    
    add(imageChoicePanel, BackgroundImageWizardController.Step.CHOICE.name());
    add(scalePanel, BackgroundImageWizardController.Step.SCALE.name());
    add(originPanel, BackgroundImageWizardController.Step.ORIGIN.name());
  }
  
  /**
   * Returns a panel displaying the given component along with zoom in and out buttons.
   */
  private JComponent createScalableImageComponent(final ScaledImageComponent imageComponent,
                                                  UserPreferences preferences) {
    final JButton zoomInButton = new JButton();
    final JButton zoomOutButton = new JButton();
    zoomInButton.setAction(new ResourceAction(preferences, 
        BackgroundImageWizardStepsPanel.class, "ZOOM_IN", true) {
        @Override
        public void actionPerformed(ActionEvent ev) {
          final Rectangle viewRect = ((JViewport)imageComponent.getParent()).getViewRect();
          imageComponent.setScaleMultiplier(2 * imageComponent.getScaleMultiplier());
          zoomOutButton.setEnabled(imageComponent.getScaleMultiplier() > 1f);
          zoomInButton.setEnabled(imageComponent.getScaleMultiplier() < 32f);
          EventQueue.invokeLater(new Runnable() {
              public void run() {
                // Keep viewport centered on the same point
                ((JViewport)imageComponent.getParent()).setViewPosition(
                    new Point((int)(viewRect.getCenterX() * 2 - viewRect.width / 2), 
                              (int)(viewRect.getCenterY() * 2 - viewRect.height / 2)));
              }
            });
        }
      });
    zoomInButton.setPreferredSize(new Dimension(30, 30));

    zoomOutButton.setAction(new ResourceAction(preferences, 
        BackgroundImageWizardStepsPanel.class, "ZOOM_OUT", false) {
        @Override
        public void actionPerformed(ActionEvent ev) {
          final Rectangle viewRect = ((JViewport)imageComponent.getParent()).getViewRect();
          imageComponent.setScaleMultiplier(.5f * imageComponent.getScaleMultiplier());
          zoomOutButton.setEnabled(imageComponent.getScaleMultiplier() > 1f);
          zoomInButton.setEnabled(imageComponent.getScaleMultiplier() < 128f);
          EventQueue.invokeLater(new Runnable() {
              public void run() {
                // Keep viewport centered on the same point
                ((JViewport)imageComponent.getParent()).setViewPosition(
                    new Point(Math.max(0, (int)(viewRect.getCenterX() / 2 - viewRect.width / 2)), 
                              Math.max(0, (int)(viewRect.getCenterY() / 2 - viewRect.height / 2))));
              }
            });
        }
      });
    zoomOutButton.setPreferredSize(new Dimension(30, 30));
    
    JPanel panel = new JPanel(new GridBagLayout());
    JScrollPane scrollPane = SwingTools.createScrollPane(imageComponent);
    panel.add(scrollPane, new GridBagConstraints(
        0, 0, 1, 2, 1, 1, GridBagConstraints.CENTER, 
        GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));
    panel.add(zoomInButton, new GridBagConstraints(
        1, 0, 1, 1, 0, 0, GridBagConstraints.NORTH, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 0), 0, 0));
    panel.add(zoomOutButton, new GridBagConstraints(
        1, 1, 1, 1, 0, 1, GridBagConstraints.NORTH, 
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    return panel;
  }
  
  /**
   * Switches to the view card matching current step.   
   */
  public void updateStep(BackgroundImageWizardController controller) {
    BackgroundImageWizardController.Step step = controller.getStep();
    this.cardLayout.show(this, step.name());    
    switch (step) {
      case CHOICE:
        this.imageChoiceOrChangeButton.requestFocusInWindow();
        break;
      case SCALE:
        ((JSpinner.DefaultEditor)this.scaleDistanceSpinner.getEditor()).getTextField().requestFocusInWindow();
        break;
      case ORIGIN:
        ((JSpinner.DefaultEditor)this.xOriginSpinner.getEditor()).getTextField().requestFocusInWindow();
        break;
    }
  }

  /**
   * Updates controller initial values from <code>backgroundImage</code>. 
   */
  private void updateController(final BackgroundImage backgroundImage,
                                final UserPreferences preferences) {
    if (backgroundImage == null) {
      setImageChoiceTexts(preferences);
      updatePreviewComponentsImage(null);
    } else {
      setImageChangeTexts(preferences);
      // Read image in imageLoader executor
      this.imageLoader.execute(new Runnable() {
          public void run() {
            BufferedImage image = null;
            try {
              image = readImage(backgroundImage.getImage(), preferences);
            } catch (IOException ex) {
              // image is null
            }
            final BufferedImage readImage = image;
            // Update components in dispatch thread
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                  if (readImage != null) {
                    controller.setImage(backgroundImage.getImage());
                    controller.setScaleDistance(backgroundImage.getScaleDistance());
                    controller.setScaleDistancePoints(backgroundImage.getScaleDistanceXStart(),
                        backgroundImage.getScaleDistanceYStart(), backgroundImage.getScaleDistanceXEnd(),
                        backgroundImage.getScaleDistanceYEnd());
                    controller.setOrigin(backgroundImage.getXOrigin(), backgroundImage.getYOrigin());
                  } else {
                    controller.setImage(null);
                    setImageChoiceTexts(preferences);
                    imageChoiceErrorLabel.setVisible(true);
                  }
                } 
              });            
          } 
        });
    }
  }

  /**
   * Reads image from <code>imageName</code> and updates controller values.
   */
  private void updateController(final String imageName,
                                final UserPreferences preferences,
                                final ContentManager contentManager) {
    // Read image in imageLoader executor
    this.imageLoader.execute(new Runnable() {
        public void run() {
          Content imageContent = null;
          try {
            // Copy image to a temporary content to keep a safe access to it until home is saved
            imageContent = TemporaryURLContent.copyToTemporaryURLContent(
                contentManager.getContent(imageName));
          } catch (RecorderException ex) {
            // Error message displayed below 
          } catch (IOException ex) {
            // Error message displayed below 
          }
          if (imageContent == null) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                  JOptionPane.showMessageDialog(SwingUtilities.getRootPane(BackgroundImageWizardStepsPanel.this), 
                      preferences.getLocalizedString(BackgroundImageWizardStepsPanel.class, 
                          "imageChoiceError", imageName));
                }
              });
            return;
          }

          BufferedImage image = null;
          try {
            image = readImage(imageContent, preferences);
          } catch (IOException ex) {
            // image is null
          }
          
          final BufferedImage readImage = image;
          final Content       readContent = imageContent;
          // Update components in dispatch thread
          EventQueue.invokeLater(new Runnable() {
              public void run() {
                if (readImage != null) {
                  controller.setImage(readContent);
                  setImageChangeTexts(preferences);
                  imageChoiceErrorLabel.setVisible(false);
                  // Initialize distance and origin with default values
                  controller.setScaleDistance(null);
                  float scaleDistanceXStart = readImage.getWidth() * 0.1f;
                  float scaleDistanceYStart = readImage.getHeight() / 2f;
                  float scaleDistanceXEnd = readImage.getWidth() * 0.9f;
                  controller.setScaleDistancePoints(scaleDistanceXStart, scaleDistanceYStart, 
                      scaleDistanceXEnd, scaleDistanceYStart);
                  controller.setOrigin(0, 0);
                } else if (isShowing()){
                  controller.setImage(null);
                  setImageChoiceTexts(preferences);
                  JOptionPane.showMessageDialog(SwingUtilities.getRootPane(BackgroundImageWizardStepsPanel.this), 
                      preferences.getLocalizedString(BackgroundImageWizardStepsPanel.class, 
                          "imageChoiceFormatError"));
                }
              }
            });
        }
      });
  }

  /**
   * Reads image from <code>imageContent</code>. 
   * Caution : this method must be thread safe because it's called from image loader executor. 
   */
  private BufferedImage readImage(Content imageContent, 
                                  UserPreferences preferences) throws IOException {
    try {
      // Display a waiting image while loading
      if (waitImage == null) {
        waitImage = ImageIO.read(BackgroundImageWizardStepsPanel.class.
            getResource(preferences.getLocalizedString(BackgroundImageWizardStepsPanel.class, "waitIcon")));
      }
      updatePreviewComponentsImage(waitImage);
      
      // Read the image content
      InputStream contentStream = imageContent.openStream();
      BufferedImage image = ImageIO.read(contentStream);
      contentStream.close();

      if (image != null) {
        updatePreviewComponentsImage(image);
        return image;
      } else {
        throw new IOException();
      }
    } catch (IOException ex) {
      updatePreviewComponentsImage(null);
      throw ex;
    } 
  }
  
  /**
   * Updates the <code>image</code> displayed by preview components.  
   */
  private void updatePreviewComponentsImage(BufferedImage image) {
    this.imageChoicePreviewComponent.setImage(image);
    this.scalePreviewComponent.setImage(image);
    this.originPreviewComponent.setImage(image);
  }

  /**
   * Sets the texts of label and button of image choice panel with
   * change texts. 
   */
  private void setImageChangeTexts(UserPreferences preferences) {
    this.imageChoiceOrChangeLabel.setText(preferences.getLocalizedString(
        BackgroundImageWizardStepsPanel.class, "imageChangeLabel.text")); 
    this.imageChoiceOrChangeButton.setText(SwingTools.getLocalizedLabelText(preferences,
        BackgroundImageWizardStepsPanel.class, "imageChangeButton.text"));
    if (!OperatingSystem.isMacOSX()) {
      this.imageChoiceOrChangeButton.setMnemonic(
          KeyStroke.getKeyStroke(preferences.getLocalizedString(
              BackgroundImageWizardStepsPanel.class, "imageChangeButton.mnemonic")).getKeyCode());
    }
  }

  /**
   * Sets the texts of label and button of image choice panel with
   * choice texts. 
   */
  private void setImageChoiceTexts(UserPreferences preferences) {
    this.imageChoiceOrChangeLabel.setText(preferences.getLocalizedString(
        BackgroundImageWizardStepsPanel.class, "imageChoiceLabel.text")); 
    this.imageChoiceOrChangeButton.setText(SwingTools.getLocalizedLabelText(preferences,
        BackgroundImageWizardStepsPanel.class, "imageChoiceButton.text"));
    if (!OperatingSystem.isMacOSX()) {
      this.imageChoiceOrChangeButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
         BackgroundImageWizardStepsPanel.class, "imageChoiceButton.mnemonic")).getKeyCode());
    }
  }
  
  /**
   * Returns an image chosen for a content chooser dialog.
   */
  private String showImageChoiceDialog(UserPreferences preferences, 
                                       ContentManager contentManager) {
    return contentManager.showOpenDialog(this,preferences.getLocalizedString(
       BackgroundImageWizardStepsPanel.class, "imageChoiceDialog.title"), ContentManager.ContentType.IMAGE);
  }

  /**
   * Returns the selection color used in preview components.
   */
  private static Color getSelectionColor() {
    Color selectionColor = OperatingSystem.isMacOSXLeopardOrSuperior() 
        ? UIManager.getColor("List.selectionBackground") 
        : UIManager.getColor("textHighlight");
    float [] hsb = new float [3];
    Color.RGBtoHSB(selectionColor.getRed(), selectionColor.getGreen(), selectionColor.getBlue(), hsb);
    if (hsb [1] < 0.4f) {
      // If color is too gray, return a default blue color
      selectionColor = new Color(40, 89, 208);
    }
    return selectionColor;
  }

  /**
   * Preview component for image scale distance choice. 
   */
  private static class ScaleImagePreviewComponent extends ScaledImageComponent {
    private enum ActionType {ACTIVATE_ALIGNMENT, DEACTIVATE_ALIGNMENT};
    
    private final BackgroundImageWizardController controller;
    
    public ScaleImagePreviewComponent(BackgroundImageWizardController controller) {
      super(null, true);
      this.controller = controller;
      addChangeListeners(controller);
      addMouseListeners(controller);
      setBorder(null);      
      
      InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
      inputMap.put(KeyStroke.getKeyStroke("shift pressed SHIFT"), ActionType.ACTIVATE_ALIGNMENT);
      inputMap.put(KeyStroke.getKeyStroke("alt shift pressed SHIFT"), ActionType.ACTIVATE_ALIGNMENT);
      inputMap.put(KeyStroke.getKeyStroke("control shift pressed SHIFT"), ActionType.ACTIVATE_ALIGNMENT);
      inputMap.put(KeyStroke.getKeyStroke("meta shift pressed SHIFT"), ActionType.ACTIVATE_ALIGNMENT);
      inputMap.put(KeyStroke.getKeyStroke("released SHIFT"), ActionType.DEACTIVATE_ALIGNMENT);
      inputMap.put(KeyStroke.getKeyStroke("alt released SHIFT"), ActionType.DEACTIVATE_ALIGNMENT);
      inputMap.put(KeyStroke.getKeyStroke("control released SHIFT"), ActionType.DEACTIVATE_ALIGNMENT);
      inputMap.put(KeyStroke.getKeyStroke("meta released SHIFT"), ActionType.DEACTIVATE_ALIGNMENT);
      setInputMap(WHEN_IN_FOCUSED_WINDOW, inputMap);
    }
    
    /**
     * Adds listeners to <code>controller</code> 
     * to update the scale distance points of the origin drawn by this component.
     */
    private void addChangeListeners(final BackgroundImageWizardController controller) {
      controller.addPropertyChangeListener(BackgroundImageWizardController.Property.SCALE_DISTANCE_POINTS, 
          new PropertyChangeListener () {
            public void propertyChange(PropertyChangeEvent ev) {
              // If origin values changes update displayed origin
              repaint();
            }
          });
    }
    
    /**
     * Adds to this component a mouse listeners that allows the user to move the start point 
     * or the end point of the scale distance line.
     */
    public void addMouseListeners(final BackgroundImageWizardController controller) {
      MouseInputAdapter mouseListener = new MouseInputAdapter() {
        private int         deltaXMousePressed;
        private int         deltaYMousePressed;
        private boolean     distanceStartPoint;
        private boolean     distanceEndPoint;
        private Point       lastMouseLocation;
        
        @Override
        public void mousePressed(MouseEvent ev) {
          if (!ev.isPopupTrigger()) {
            mouseMoved(ev);
            
            if (this.distanceStartPoint
                || this.distanceEndPoint) {
              float [][] scaleDistancePoints = controller.getScaleDistancePoints();
              Point translationorigin = getImageTranslation();
              float scale = getImageScale();
              this.deltaXMousePressed = (ev.getX() - translationorigin.x);
              this.deltaYMousePressed = (ev.getY() - translationorigin.y);
              if (this.distanceStartPoint) {
                this.deltaXMousePressed -= scaleDistancePoints [0][0] * scale;
                this.deltaYMousePressed -= scaleDistancePoints [0][1] * scale;
              } else {
                this.deltaXMousePressed -= scaleDistancePoints [1][0] * scale;
                this.deltaYMousePressed -= scaleDistancePoints [1][1] * scale;
              }
              // Set actions used to activate/deactivate alignment
              ActionMap actionMap = getActionMap();
              actionMap.put(ActionType.ACTIVATE_ALIGNMENT, new AbstractAction() {
                  public void actionPerformed(ActionEvent ev) {
                    mouseDragged(null, true);
                  }
                });                  
              actionMap.put(ActionType.DEACTIVATE_ALIGNMENT, new AbstractAction() {
                  public void actionPerformed(ActionEvent ev) {
                    mouseDragged(null, false);
                  }
                });   
              setActionMap(actionMap);
            }
          }
          this.lastMouseLocation = ev.getPoint();
        }
        
        @Override
        public void mouseReleased(MouseEvent ev) {
          ActionMap actionMap = getActionMap();
          // Remove actions used to activate/deactivate alignment
          actionMap.remove(ActionType.ACTIVATE_ALIGNMENT);                  
          actionMap.remove(ActionType.DEACTIVATE_ALIGNMENT);   
          setActionMap(actionMap);
        }

        @Override
        public void mouseDragged(MouseEvent ev) {
          mouseDragged(ev.getPoint(), ev.isShiftDown());
          this.lastMouseLocation = ev.getPoint();
        }
        
        public void mouseDragged(Point mouseLocation, boolean keepHorizontalVertical) {
          if (this.distanceStartPoint
              || this.distanceEndPoint) {
            if (mouseLocation == null) {
              mouseLocation = this.lastMouseLocation;
            }            
            Point point = getPointConstrainedInImage(
                mouseLocation.x - this.deltaXMousePressed, mouseLocation.y - this.deltaYMousePressed);
            Point translation = getImageTranslation();
            float [][] scaleDistancePoints = controller.getScaleDistancePoints();
            float [] updatedPoint;
            float [] fixedPoint;
            if (this.distanceStartPoint) {
              updatedPoint = scaleDistancePoints [0];
              fixedPoint   = scaleDistancePoints [1];
            } else {
              updatedPoint = scaleDistancePoints [1];
              fixedPoint   = scaleDistancePoints [0];
            }

            float scale = getImageScale();
            // Compute updated point of distance line
            float newX = (float)((point.getX() - translation.x) / scale);
            float newY = (float)((point.getY() - translation.y) / scale);
            // Accept new points only if distance is greater that 2 pixels
            if (Point2D.distanceSq(fixedPoint [0] * scale, fixedPoint [1] * scale, 
                    newX * scale, newY * scale) >= 4) {
              // If shift is down constrain keep the line vertical or horizontal
              if (keepHorizontalVertical) {
                double angle = Math.abs(Math.atan2(fixedPoint [1] - newY, newX - fixedPoint [0]));
                if (angle > Math.PI / 4 && angle <= 3 * Math.PI / 4) {
                  newX = fixedPoint [0];
                } else {
                  newY = fixedPoint [1];
                }
              }
              updatedPoint [0] = newX; 
              updatedPoint [1] = newY;
              controller.setScaleDistancePoints(
                scaleDistancePoints [0][0], scaleDistancePoints [0][1],
                scaleDistancePoints [1][0], scaleDistancePoints [1][1]);
              repaint();
            }
          }
        }
        
        @Override
        public void mouseMoved(MouseEvent ev) {
          this.distanceStartPoint = 
          this.distanceEndPoint = false;
          if (isPointInImage(ev.getX(), ev.getY())) {
            float [][] scaleDistancePoints = controller.getScaleDistancePoints();
            Point translation = getImageTranslation();
            float scale = getImageScale();
            // Check if user clicked on start or end point of distance line
            if (Math.abs(scaleDistancePoints [0][0] * scale - ev.getX() + translation.x) <= 3
                && Math.abs(scaleDistancePoints [0][1] * scale - ev.getY() + translation.y) <= 3) {
              this.distanceStartPoint = true;
            } else if (Math.abs(scaleDistancePoints [1][0] * scale - ev.getX() + translation.x) <= 3
                       && Math.abs(scaleDistancePoints [1][1] * scale - ev.getY() + translation.y) <= 3) {
              this.distanceEndPoint = true;
            }
          }
          
          if (this.distanceStartPoint || this.distanceEndPoint) {
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
          } else {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
          }
        }
      };
      addMouseListener(mouseListener);
      addMouseMotionListener(mouseListener);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
      if (getImage() != null) {
        Graphics2D g2D = (Graphics2D)g;
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
            RenderingHints.VALUE_ANTIALIAS_ON);
        
        Point translation = getImageTranslation();
        float scale = getImageScale();
        // Fill image background
        g2D.setColor(UIManager.getColor("window"));
        g2D.fillRect(translation.x, translation.y, (int)(getImage().getWidth() * scale), 
            (int)(getImage().getHeight() * scale));
        
        // Paint image with a 0.5 alpha
        paintImage(g2D, AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));        

        g2D.setPaint(getSelectionColor());
        
        AffineTransform oldTransform = g2D.getTransform();
        Stroke oldStroke = g2D.getStroke();
        // Use same origin and scale as image drawing in super class
        g2D.translate(translation.x, translation.y);
        g2D.scale(scale, scale);       
        // Draw a scale distance line        
        g2D.setStroke(new BasicStroke(5 / scale, 
            BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        float [][] scaleDistancePoints = this.controller.getScaleDistancePoints();
        g2D.draw(new Line2D.Float(scaleDistancePoints [0][0], scaleDistancePoints [0][1], 
                                  scaleDistancePoints [1][0], scaleDistancePoints [1][1]));
        // Draw start point line
        g2D.setStroke(new BasicStroke(1 / scale, 
            BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        double angle = Math.atan2(scaleDistancePoints [1][1] - scaleDistancePoints [0][1], 
                    scaleDistancePoints [1][0] - scaleDistancePoints [0][0]);
        AffineTransform oldTransform2 = g2D.getTransform();
        g2D.translate(scaleDistancePoints [0][0], scaleDistancePoints [0][1]);
        g2D.rotate(angle);
        Shape endLine = new Line2D.Double(0, 5 / scale, 0, -5 / scale);
        g2D.draw(endLine);
        g2D.setTransform(oldTransform2);
        
        // Draw end point line
        g2D.translate(scaleDistancePoints [1][0], scaleDistancePoints [1][1]);
        g2D.rotate(angle);
        g2D.draw(endLine);
        g2D.setTransform(oldTransform);
        g2D.setStroke(oldStroke);
      }
    }
  }
  
  /**
   * Preview component for image scale distance choice. 
   */
  private static class OriginImagePreviewComponent extends ScaledImageComponent {
    private final BackgroundImageWizardController controller;

    public OriginImagePreviewComponent(BackgroundImageWizardController controller) {
      super(null, true);
      this.controller = controller;
      addChangeListeners(controller);
      addMouseListener(controller);
      setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    }

    /**
     * Adds listeners to <code>controller</code> 
     * to update the location of the origin drawn by this component.
     */
    private void addChangeListeners(final BackgroundImageWizardController controller) {
      PropertyChangeListener originChangeListener = new PropertyChangeListener () {
          public void propertyChange(PropertyChangeEvent ev) {
            // If origin values changes update displayed origin
            repaint();
          }
        };
      controller.addPropertyChangeListener(BackgroundImageWizardController.Property.X_ORIGIN, originChangeListener);
      controller.addPropertyChangeListener(BackgroundImageWizardController.Property.Y_ORIGIN, originChangeListener);
    }
    
    /**
     * Adds a mouse listener to this component to update the origin stored 
     * by <code>controller</code> when the user clicks in component.
     */
    public void addMouseListener(final BackgroundImageWizardController controller) {
      MouseInputAdapter mouseAdapter = new MouseInputAdapter() {
          @Override
          public void mousePressed(MouseEvent ev) {
            if (!ev.isPopupTrigger()
                && isPointInImage(ev.getX(), ev.getY())) {
              updateOrigin(ev.getPoint());
            }
          }

          private void updateOrigin(Point point) {
            Point translation = getImageTranslation();
            float [][] scaleDistancePoints = controller.getScaleDistancePoints();
            float rescale = getImageScale() / BackgroundImage.getScale(controller.getScaleDistance(), 
                scaleDistancePoints [0][0], scaleDistancePoints [0][1], 
                scaleDistancePoints [1][0], scaleDistancePoints [1][1]);
            float xOrigin = Math.round((point.getX() - translation.x) / rescale * 10) / 10.f;
            float yOrigin = Math.round((point.getY() - translation.y) / rescale * 10) / 10.f;
            controller.setOrigin(xOrigin, yOrigin);
          }
          
          @Override
          public void mouseDragged(MouseEvent ev) {
            updateOrigin(getPointConstrainedInImage(ev.getX(), ev.getY()));
          }
          
          @Override
          public void mouseMoved(MouseEvent ev) {
            if (isPointInImage(ev.getX(), ev.getY())) {
              setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            } else {
              setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
          }
        };
      addMouseListener(mouseAdapter);
      addMouseMotionListener(mouseAdapter);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
      if (getImage() != null) {
        Graphics2D g2D = (Graphics2D)g;
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
            RenderingHints.VALUE_ANTIALIAS_ON);
        
        Point translation = getImageTranslation();
        // Fill image background
        g2D.setColor(UIManager.getColor("window"));
        g2D.fillRect(translation.x, translation.y, (int)(getImage().getWidth() * getImageScale()), 
            (int)(getImage().getHeight() * getImageScale()));
        
        // Paint image with a 0.5 alpha 
        paintImage(g, AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));        
        
        g2D.setPaint(getSelectionColor());
        
        AffineTransform oldTransform = g2D.getTransform();
        Stroke oldStroke = g2D.getStroke();
        g2D.translate(translation.x, translation.y);
        // Rescale according to scale distance
        float [][] scaleDistancePoints = this.controller.getScaleDistancePoints();
        float scale = getImageScale() / BackgroundImage.getScale(this.controller.getScaleDistance(), 
            scaleDistancePoints [0][0], scaleDistancePoints [0][1], 
            scaleDistancePoints [1][0], scaleDistancePoints [1][1]);
        g2D.scale(scale, scale);
        
        // Draw a dot at origin
        g2D.translate(this.controller.getXOrigin(), this.controller.getYOrigin());
        
        float originRadius = 4 / scale;
        g2D.fill(new Ellipse2D.Float(-originRadius, -originRadius,
            originRadius * 2, originRadius * 2));
        
        // Draw a cross
        g2D.setStroke(new BasicStroke(1 / scale, 
            BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));        
        g2D.draw(new Line2D.Double(8 / scale, 0, -8 / scale, 0));
        g2D.draw(new Line2D.Double(0, 8 / scale, 0, -8 / scale));
        g2D.setTransform(oldTransform);
        g2D.setStroke(oldStroke);
      }
    }
  }
}
