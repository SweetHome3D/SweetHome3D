/*
 * BackgroundImageWizardStepsPanel.java 8 juin 07
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

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
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
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;

import com.eteks.sweethome3d.model.BackgroundImage;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.ContentManager;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.tools.TemporaryURLContent;

/**
 * Wizard panel for background image choice. 
 * @author Emmanuel Puybaret
 */
public class BackgroundImageWizardStepsPanel extends JPanel {
  private BackgroundImageWizardController controller;
  private ResourceBundle                  resource;
  private CardLayout                      cardLayout;
  private JLabel                          imageChoiceOrChangeLabel;
  private JButton                         imageChoiceOrChangeButton;
  private JLabel                          imageChoiceErrorLabel;
  private ScaledImageComponent            imageChoicePreviewComponent;
  private JLabel                          scaleLabel;
  private JLabel                          scaleDistanceLabel;
  private JSpinner                        scaleDistanceSpinner;
  private ScaledImageComponent            scalePreviewComponent;
  private JLabel                          originLabel;
  private JLabel                          xOriginLabel;
  private JSpinner                        xOriginSpinner;
  private JLabel                          yOriginLabel;
  private JSpinner                        yOriginSpinner;
  private ScaledImageComponent            originPreviewComponent;
  
  private static Executor                 imageLoader = Executors.newSingleThreadExecutor();
  private static BufferedImage            waitImage;

  /**
   * Creates a view for background image choice, scale and origin. 
   */
  public BackgroundImageWizardStepsPanel(BackgroundImage backgroundImage, 
                                         UserPreferences preferences, 
                                         ContentManager contentManager,
                                         BackgroundImageWizardController controller) {
    this.controller = controller;
    this.resource = ResourceBundle.getBundle(BackgroundImageWizardStepsPanel.class.getName());
    createComponents(preferences, contentManager);
    setMnemonics();
    layoutComponents();
    updateController(backgroundImage);
  }

  /**
   * Creates components displayed by this panel.
   */
  private void createComponents(UserPreferences preferences, 
                                final ContentManager contentManager) {
    // Get unit name matching current unit 
    String unitName = preferences.getUnit().getName();

    // Image choice panel components
    this.imageChoiceOrChangeLabel = new JLabel(); 
    this.imageChoiceOrChangeButton = new JButton();
    this.imageChoiceOrChangeButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          Content content = showImageChoiceDialog(contentManager);
          if (content != null) {
            updateController(content);
          }
        }
      });
    this.imageChoiceErrorLabel = new JLabel(resource.getString("imageChoiceErrolLabel.text"));
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
            updateController(TemporaryURLContent.copyToTemporaryURLContent(
                contentManager.getContent(files.get(0).getAbsolutePath())));
          } catch (UnsupportedFlavorException ex) {
            success = false;
          } catch (IOException ex) {
            success = false;
          } catch (RecorderException ex) {
            success = false;
          }
          if (!success) {
            JOptionPane.showMessageDialog(BackgroundImageWizardStepsPanel.this, 
                resource.getString("imageChoiceError"));
          }
          return success;
        }
      });
    this.imageChoicePreviewComponent.setBorder(BorderFactory.createLoweredBevelBorder());
    
    // Image scale panel components
    this.scaleLabel = new JLabel(this.resource.getString("scaleLabel.text"));
    this.scaleDistanceLabel = new JLabel(
        String.format(this.resource.getString("scaleDistanceLabel.text"), unitName));
    final NullableSpinner.NullableSpinnerLengthModel scaleDistanceSpinnerModel = 
        new NullableSpinner.NullableSpinnerLengthModel(preferences, 0.99f, 1000000f);
    this.scaleDistanceSpinner = new NullableSpinner(scaleDistanceSpinnerModel);
    this.scaleDistanceSpinner.getModel().addChangeListener(new ChangeListener () {
        public void stateChanged(ChangeEvent ev) {
          // If spinner value changes update controller
          controller.setScaleDistance(
              ((NullableSpinner.NullableSpinnerLengthModel)scaleDistanceSpinner.getModel()).getLength());
        }
      });
    this.controller.addPropertyChangeListener(BackgroundImageWizardController.Property.SCALE_DISTANCE,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            // If scale distance changes updates scale spinner
            scaleDistanceSpinnerModel.setNullable(controller.getScaleDistance() == null);
            scaleDistanceSpinnerModel.setLength(controller.getScaleDistance());
          }
        });
    this.scalePreviewComponent = new ScaleImagePreviewComponent(this.controller);
    
    // Image origin panel components
    this.originLabel = new JLabel(this.resource.getString("originLabel.text"));
    this.xOriginLabel = new JLabel(
        String.format(this.resource.getString("xOriginLabel.text"), unitName)); 
    this.yOriginLabel = new JLabel(
        String.format(this.resource.getString("yOriginLabel.text"), unitName)); 
    final NullableSpinner.NullableSpinnerLengthModel xOriginSpinnerModel = 
        new NullableSpinner.NullableSpinnerLengthModel(preferences, 0f, 1000000f);
    this.xOriginSpinner = new NullableSpinner(xOriginSpinnerModel);
    final NullableSpinner.NullableSpinnerLengthModel yOriginSpinnerModel = 
        new NullableSpinner.NullableSpinnerLengthModel(preferences, 0f, 1000000f);
    this.yOriginSpinner = new NullableSpinner(yOriginSpinnerModel);
    ChangeListener originSpinnersListener = new ChangeListener () {
        public void stateChanged(ChangeEvent ev) {
          // If origin spinners value changes update controller
          controller.setOrigin(xOriginSpinnerModel.getLength(), yOriginSpinnerModel.getLength());
        }
      };
    xOriginSpinnerModel.addChangeListener(originSpinnersListener);
    yOriginSpinnerModel.addChangeListener(originSpinnersListener);
    this.controller.addPropertyChangeListener(BackgroundImageWizardController.Property.X_ORIGIN, 
        new PropertyChangeListener () {
          public void propertyChange(PropertyChangeEvent ev) {
            // If origin values changes update x origin spinner
            xOriginSpinnerModel.setLength(controller.getXOrigin());
          }
        });
    this.controller.addPropertyChangeListener(BackgroundImageWizardController.Property.Y_ORIGIN, 
        new PropertyChangeListener () {
          public void propertyChange(PropertyChangeEvent ev) {
            // If origin values changes update y origin spinner
            yOriginSpinnerModel.setLength(controller.getYOrigin());
          }
        });
    
    this.originPreviewComponent = new OriginImagePreviewComponent(this.controller);
  }

  /**
   * Sets components mnemonics and label / component associations.
   */
  private void setMnemonics() {
    if (!OperatingSystem.isMacOSX()) {
      this.scaleDistanceLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("scaleDistanceLabel.mnemonic")).getKeyCode());
      this.scaleDistanceLabel.setLabelFor(this.scaleDistanceSpinner);
      this.xOriginLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("xOriginLabel.mnemonic")).getKeyCode());
      this.xOriginLabel.setLabelFor(this.xOriginSpinner);
      this.yOriginLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("yOriginLabel.mnemonic")).getKeyCode());
      this.yOriginLabel.setLabelFor(this.yOriginSpinner);
    }
  }
  
  /**
   * Layouts components in 3 panels added to this panel as cards. 
   */
  private void layoutComponents() {
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
    imageChoicePanel.add(this.imageChoicePreviewComponent, 
        ProportionalLayout.Constraints.BOTTOM);
    
    JPanel scaleTopPanel = new JPanel(new GridBagLayout());
    scaleTopPanel.add(this.scaleLabel, new GridBagConstraints(
        0, 0, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 0), 0, 0));
    scaleTopPanel.add(this.scaleDistanceLabel, new GridBagConstraints(
        0, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
    scaleTopPanel.add(this.scaleDistanceSpinner, new GridBagConstraints(
        1, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 0), 0, 0));
    
    JPanel scalePanel = new JPanel(new ProportionalLayout());
    scalePanel.add(scaleTopPanel, ProportionalLayout.Constraints.TOP);
    scalePanel.add(this.scalePreviewComponent, 
        ProportionalLayout.Constraints.BOTTOM);


    JPanel originTopPanel = new JPanel(new GridBagLayout());
    originTopPanel.add(this.originLabel, new GridBagConstraints(
        0, 0, 4, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 0), 0, 0));
    originTopPanel.add(this.xOriginLabel, new GridBagConstraints(
        0, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
    originTopPanel.add(this.xOriginSpinner, new GridBagConstraints(
        1, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 10), -10, 0));
    originTopPanel.add(this.yOriginLabel, new GridBagConstraints(
        2, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
    originTopPanel.add(this.yOriginSpinner, new GridBagConstraints(
        3, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 0), -10, 0));
    
    JPanel originPanel = new JPanel(new ProportionalLayout());
    originPanel.add(originTopPanel, ProportionalLayout.Constraints.TOP);
    originPanel.add(this.originPreviewComponent, 
        ProportionalLayout.Constraints.BOTTOM);

    add(imageChoicePanel, BackgroundImageWizardController.Step.CHOICE.name());
    add(scalePanel, BackgroundImageWizardController.Step.SCALE.name());
    add(originPanel, BackgroundImageWizardController.Step.ORIGIN.name());
  }
  
  /**
   * Switches to the component card matching <code>step</code>.   
   */
  public void setStep(final BackgroundImageWizardController.Step step) {
    this.cardLayout.show(this, step.name());    
    switch (step) {
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
  private void updateController(final BackgroundImage backgroundImage) {
    if (backgroundImage == null) {
      setImageChoiceTexts();
      updatePreviewComponentsImage(null);
    } else {
      setImageChangeTexts();
      // Read image in imageLoader executor
      imageLoader.execute(new Runnable() {
          public void run() {
            BufferedImage image = null;
            try {
              image = readImage(backgroundImage.getImage());
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
                    setImageChoiceTexts();
                    imageChoiceErrorLabel.setVisible(true);
                  }
                } 
              });
            
          } 
        });
    }
  }

  /**
   * Updates controller values from <code>imageContent</code>.
   */
  private void updateController(final Content imageContent) {
    // Read image in imageLoader executor
    imageLoader.execute(new Runnable() {
        public void run() {
          BufferedImage image = null;
          try {
            image = readImage(imageContent);
          } catch (IOException ex) {
            // image is null
          }
          final BufferedImage readImage = image;
          // Update components in dispatch thread
          EventQueue.invokeLater(new Runnable() {
              public void run() {
                if (readImage != null) {
                  controller.setImage(imageContent);
                  setImageChangeTexts();
                  imageChoiceErrorLabel.setVisible(false);
                  // Initialize distance and origin with default values
                  controller.setScaleDistance(null);
                  float scaleDistanceXStart = readImage.getWidth() * 0.1f;
                  float scaleDistanceYStart = readImage.getHeight() / 2f;
                  float scaleDistanceXEnd = readImage.getWidth() * 0.9f;
                  controller.setScaleDistancePoints(scaleDistanceXStart, scaleDistanceYStart, 
                      scaleDistanceXEnd, scaleDistanceYStart);
                  controller.setOrigin(0, 0);
                } else {
                  controller.setImage(null);
                  setImageChoiceTexts();
                  JOptionPane.showMessageDialog(BackgroundImageWizardStepsPanel.this, 
                      resource.getString("imageChoiceFormatError"));
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
  private BufferedImage readImage(Content imageContent) throws IOException {
    try {
      // Display a waiting image while loading
      if (waitImage == null) {
        waitImage = ImageIO.read(BackgroundImageWizardStepsPanel.class.
            getResource(resource.getString("waitIcon")));
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
  private void setImageChangeTexts() {
    this.imageChoiceOrChangeLabel.setText(this.resource.getString("imageChangeLabel.text")); 
    this.imageChoiceOrChangeButton.setText(this.resource.getString("imageChangeButton.text"));
    if (!OperatingSystem.isMacOSX()) {
      this.imageChoiceOrChangeButton.setMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("imageChangeButton.mnemonic")).getKeyCode());
    }
  }

  /**
   * Sets the texts of label and button of image choice panel with
   * choice texts. 
   */
  private void setImageChoiceTexts() {
    this.imageChoiceOrChangeLabel.setText(this.resource.getString("imageChoiceLabel.text")); 
    this.imageChoiceOrChangeButton.setText(this.resource.getString("imageChoiceButton.text"));
    if (!OperatingSystem.isMacOSX()) {
      this.imageChoiceOrChangeButton.setMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("imageChoiceButton.mnemonic")).getKeyCode());
    }
  }
  
  /**
   * Returns an image content chosen for a content chooser dialog.
   */
  private Content showImageChoiceDialog(ContentManager contentManager) {
    String imageName = contentManager.showOpenDialog( 
        this.resource.getString("imageChoiceDialog.title"), ContentManager.ContentType.IMAGE);
    if (imageName != null) {
      try {
        return TemporaryURLContent.copyToTemporaryURLContent(contentManager.getContent(imageName));
      } catch (RecorderException ex) {
        // Error message displayed below 
      } catch (IOException ex) {
        // Error message displayed below 
      }
      JOptionPane.showMessageDialog(this, 
          String.format(this.resource.getString("imageChoiceError"), imageName));
    }
    return null;
  }

  /**
   * Preview component for image scale distance choice. 
   */
  private static class ScaleImagePreviewComponent extends ScaledImageComponent {
    private BackgroundImageWizardController controller;

    public ScaleImagePreviewComponent(BackgroundImageWizardController controller) {
      this.controller = controller;
      addChangeListeners(controller);
      addMouseListeners(controller);
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
        private int     lastX;
        private int     lastY;
        private boolean distanceStartPoint;
        private boolean distanceEndPoint;
        
        @Override
        public void mousePressed(MouseEvent ev) {
          if (!ev.isPopupTrigger()) {
            mouseMoved(ev);
            if (this.distanceStartPoint
                || this.distanceEndPoint) {
              this.lastX = ev.getX();
              this.lastY = ev.getY();
            } 
          }
        }

        @Override
        public void mouseDragged(MouseEvent ev) {
          if (isPointInImage(ev.getX(), ev.getY())) {
            float [][] scaleDistancePoints = controller.getScaleDistancePoints();
            float scale = getImageScale();
            if (this.distanceStartPoint) {
              // Compute start point of distance line
              scaleDistancePoints [0][0] += (ev.getX() - this.lastX) / scale; 
              scaleDistancePoints [0][1] += (ev.getY() - this.lastY) / scale;
            } else if (this.distanceEndPoint) {
              // Compute end point of distance line
              scaleDistancePoints [1][0] += (ev.getX() - this.lastX) / scale; 
              scaleDistancePoints [1][1] += (ev.getY() - this.lastY) / scale;
            }
            
            // Accept new points only if distance is greater that 2 pixels
            if ((this.distanceStartPoint
                 || this.distanceEndPoint)
                && Point2D.distanceSq(scaleDistancePoints [0][0] * scale, 
                    scaleDistancePoints [0][1] * scale,
                    scaleDistancePoints [1][0] * scale, 
                    scaleDistancePoints [1][1] * scale) >= 4) {
              controller.setScaleDistancePoints(
                  scaleDistancePoints [0][0], scaleDistancePoints [0][1],
                  scaleDistancePoints [1][0], scaleDistancePoints [1][1]);
              repaint();
            }

            this.lastX = ev.getX();
            this.lastY = ev.getY();
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
            if (Math.abs(scaleDistancePoints [0][0] * scale - ev.getX() + translation.x) < 2
                && Math.abs(scaleDistancePoints [0][1] * scale - ev.getY() + translation.y) < 2) {
              this.distanceStartPoint = true;
            } else if (Math.abs(scaleDistancePoints [1][0] * scale - ev.getX() + translation.x) < 2
                       && Math.abs(scaleDistancePoints [1][1] * scale - ev.getY() + translation.y) < 2) {
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

        Color scaleDistanceLineColor = OperatingSystem.isMacOSXLeopardOrSuperior() 
            ? UIManager.getColor("List.selectionBackground") 
            : UIManager.getColor("textHighlight");
     
        g2D.setPaint(scaleDistanceLineColor);
        
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
    private BackgroundImageWizardController controller;

    public OriginImagePreviewComponent(BackgroundImageWizardController controller) {
      this.controller = controller;
      addChangeListeners(controller);
      addMouseListener(controller);
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
              Point translation = getImageTranslation();
              float [][] scaleDistancePoints = controller.getScaleDistancePoints();
              float rescale = getImageScale() / BackgroundImage.getScale(controller.getScaleDistance(), 
                  scaleDistancePoints [0][0], scaleDistancePoints [0][1], 
                  scaleDistancePoints [1][0], scaleDistancePoints [1][1]);
              float xOrigin = Math.round((ev.getX() - translation.x) / rescale * 10) / 10.f;
              float yOrigin = Math.round((ev.getY() - translation.y) / rescale * 10) / 10.f;
              controller.setOrigin(xOrigin, yOrigin);
            }
          }
          
          @Override
          public void mouseDragged(MouseEvent ev) {
            mouseMoved(ev);
            mousePressed(ev);
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
        
        Color scaleDistanceLineColor = OperatingSystem.isMacOSXLeopardOrSuperior() 
            ? UIManager.getColor("List.selectionBackground") 
            : UIManager.getColor("textHighlight");;
        g2D.setPaint(scaleDistanceLineColor);
        
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
