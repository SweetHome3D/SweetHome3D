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
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
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
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.filechooser.FileFilter;

import com.eteks.sweethome3d.model.BackgroundImage;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * Wizard panel for background image choice. 
 * @author Emmanuel Puybaret
 */
public class BackgroundImageWizardStepsPanel extends JPanel {
  private static final FileFilter [] IMAGE_FILTERS = {
    new FileFilter() {
      @Override
      public boolean accept(File file) {
        // Accept directories and .sh3d files
        return file.isDirectory()
               || file.getName().toLowerCase().endsWith(".bmp")
               || file.getName().toLowerCase().endsWith(".wbmp");
      }
  
      @Override
      public String getDescription() {
        return "BMP";
      }
    },
    new FileFilter() {
      @Override
      public boolean accept(File file) {
        // Accept directories and .sh3d files
        return file.isDirectory()
               || file.getName().toLowerCase().endsWith(".gif");
      }
  
      @Override
      public String getDescription() {
        return "GIF";
      }
    },
    new FileFilter() {
      @Override
      public boolean accept(File file) {
        // Accept directories and .sh3d files
        return file.isDirectory()
               || file.getName().toLowerCase().endsWith(".jpg")
               || file.getName().toLowerCase().endsWith(".jpeg");
      }
  
      @Override
      public String getDescription() {
        return "JPEG";
      }
    }, 
    new FileFilter() {
      @Override
      public boolean accept(File file) {
        // Accept directories and .sh3d files
        return file.isDirectory()
               || file.getName().toLowerCase().endsWith(".png");
      }
  
      @Override
      public String getDescription() {
        return "PNG";
      }
    }};

  private BackgroundImageWizardController controller;
  private ResourceBundle                  resource;
  private CardLayout                      cardLayout;
  private JLabel                          imageChoiceOrChangeLabel;
  private JButton                         imageChoiceOrChangeButton;
  private JLabel                          imageChoiceErrorLabel;
  private ImagePreviewComponent           imageChoicePreviewComponent;
  private JLabel                          imageScaleLabel;
  private JLabel                          imageScaleDistanceLabel;
  private NullableSpinner                 imageScaleDistanceSpinner;
  private ScaleImagePreviewComponent      imageScalePreviewComponent;
  private JLabel                          imageOriginLabel;
  private JLabel                          imageXOriginLabel;
  private NullableSpinner                 imageXOriginSpinner;
  private JLabel                          imageYOriginLabel;
  private NullableSpinner                 imageYOriginSpinner;
  private ImagePreviewComponent           imageOriginPreviewComponent;
  
  private static Executor                 imageLoader = Executors.newSingleThreadExecutor();
  private static BufferedImage            waitImage;

  /**
   * Creates a view for background image choice, scale and origin. 
   */
  public BackgroundImageWizardStepsPanel(BackgroundImage backgroundImage, UserPreferences preferences, 
                                        BackgroundImageWizardController controller) {
    this.controller = controller;
    this.resource = ResourceBundle.getBundle(BackgroundImageWizardStepsPanel.class.getName());
    createComponents(preferences);
    setMnemonics();
    layoutComponents();
    updateComponents(backgroundImage);
  }

  /**
   * Creates components displayed by this panel.
   */
  private void createComponents(UserPreferences preferences) {
    // Get unit text matching current unit 
    String unitText = this.resource.getString(
        preferences.getUnit() == UserPreferences.Unit.CENTIMETER
            ? "centimeterUnit"
            : "inchUnit");

    // Image choice panel components
    this.imageChoiceOrChangeLabel = new JLabel(); 
    this.imageChoiceOrChangeButton = new JButton();
    this.imageChoiceOrChangeButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          Content content = showImageChoiceDialog();
          if (content != null) {
            updateComponents(content);
          }
        }
      });
    this.imageChoiceErrorLabel = new JLabel(resource.getString("imageChoiceErrolLabel.text"));
    // Make imageChoiceErrorLabel visible only if an error occured during image content loading
    this.imageChoiceErrorLabel.setVisible(false);
    this.imageChoicePreviewComponent = new ImagePreviewComponent();
    
    // Image scale panel components
    this.imageScaleLabel = new JLabel(this.resource.getString("imageScaleLabel.text"));
    this.imageScaleDistanceLabel = new JLabel(
        String.format(this.resource.getString("imageScaleDistanceLabel.text"), unitText));
    this.imageScaleDistanceSpinner = new NullableSpinner(
        new NullableSpinner.NullableSpinnerLengthModel(preferences, 1f, 1000000f));
    this.imageScaleDistanceSpinner.getModel().addChangeListener(new ChangeListener () {
        public void stateChanged(ChangeEvent ev) {
          // If spinner value changes update controller
          controller.setScaleDistance(
              ((NullableSpinner.NullableSpinnerLengthModel)imageScaleDistanceSpinner.getModel()).getLength());
        }
      });
    this.imageScalePreviewComponent = new ScaleImagePreviewComponent(this.controller);
    
    // Image origin panel components
    this.imageOriginLabel = new JLabel(this.resource.getString("imageOriginLabel.text"));
    this.imageXOriginLabel = new JLabel(
        String.format(this.resource.getString("imageXOriginLabel.text"), unitText)); 
    this.imageYOriginLabel = new JLabel(
        String.format(this.resource.getString("imageYOriginLabel.text"), unitText)); 
    final NullableSpinner.NullableSpinnerLengthModel xOriginSpinnerModel = 
        new NullableSpinner.NullableSpinnerLengthModel(preferences, 0f, 1000000f);
    this.imageXOriginSpinner = new NullableSpinner(xOriginSpinnerModel);
    final NullableSpinner.NullableSpinnerLengthModel yOriginSpinnerModel = 
        new NullableSpinner.NullableSpinnerLengthModel(preferences, 0f, 1000000f);
    this.imageYOriginSpinner = new NullableSpinner(yOriginSpinnerModel);
    ChangeListener originSpinnerListener = new ChangeListener () {
        public void stateChanged(ChangeEvent ev) {
          // If origin spinners value changes update controller
          controller.setOrigin(xOriginSpinnerModel.getLength(), yOriginSpinnerModel.getLength());
        }
      };
    xOriginSpinnerModel.addChangeListener(originSpinnerListener);
    yOriginSpinnerModel.addChangeListener(originSpinnerListener);
    PropertyChangeListener originChangeListener = new PropertyChangeListener () {
        public void propertyChange(PropertyChangeEvent evt) {
          // If origin values changes update origin spinners
          ((NullableSpinner.NullableSpinnerLengthModel)imageXOriginSpinner.getModel()).setLength(
              controller.getXOrigin());
          ((NullableSpinner.NullableSpinnerLengthModel)imageYOriginSpinner.getModel()).setLength(
              controller.getYOrigin());
        }
      };
    controller.addPropertyChangeListener(BackgroundImageWizardController.Property.X_ORIGIN, originChangeListener);
    controller.addPropertyChangeListener(BackgroundImageWizardController.Property.Y_ORIGIN, originChangeListener);
    
    this.imageOriginPreviewComponent = new OriginImagePreviewComponent(this.controller);
  }

  /**
   * Sets components mnemonics and label / component associations.
   */
  private void setMnemonics() {
    if (!System.getProperty("os.name").startsWith("Mac OS X")) {
      this.imageScaleDistanceLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("imageScaleDistanceLabel.mnemonic")).getKeyCode());
      this.imageScaleDistanceLabel.setLabelFor(this.imageScaleDistanceSpinner);
      this.imageXOriginLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("imageXOriginLabel.mnemonic")).getKeyCode());
      this.imageXOriginLabel.setLabelFor(this.imageXOriginSpinner);
      this.imageYOriginLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("imageYOriginLabel.mnemonic")).getKeyCode());
      this.imageYOriginLabel.setLabelFor(this.imageYOriginSpinner);
    }
  }
  
  /**
   * Layouts components in 3 panels added to this panel as cards. 
   */
  private void layoutComponents() {
    this.cardLayout = new CardLayout();
    setLayout(this.cardLayout);
    
    JPanel imageChoicePanel = new JPanel(new GridBagLayout());
    imageChoicePanel.add(this.imageChoiceOrChangeLabel, new GridBagConstraints(
        0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.NONE, new Insets(5, 0, 5, 0), 0, 0));
    imageChoicePanel.add(this.imageChoiceOrChangeButton, new GridBagConstraints(
        0, 1, 1, 1, 0, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    imageChoicePanel.add(this.imageChoiceErrorLabel, new GridBagConstraints(
        0, 2, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.NONE, new Insets(5, 0, 0, 0), 0, 0));
    imageChoicePanel.add(this.imageChoicePreviewComponent, new GridBagConstraints(
        0, 3, 1, 1, 1, 1, GridBagConstraints.CENTER, 
        GridBagConstraints.NONE, new Insets(5, 0, 5, 0), 0, 0));
    
    JPanel imageScalePanel = new JPanel(new GridBagLayout());
    imageScalePanel.add(this.imageScaleLabel, new GridBagConstraints(
        0, 0, 2, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.NONE, new Insets(5, 0, 5, 0), 0, 0));
    imageScalePanel.add(this.imageScaleDistanceLabel, new GridBagConstraints(
        0, 1, 1, 1, 0, 1, GridBagConstraints.WEST, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
    imageScalePanel.add(this.imageScaleDistanceSpinner, new GridBagConstraints(
        1, 1, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 0), 0, 0));
    imageScalePanel.add(this.imageScalePreviewComponent, new GridBagConstraints(
        0, 2, 2, 1, 1, 1, GridBagConstraints.CENTER, 
        GridBagConstraints.NONE, new Insets(5, 0, 5, 0), 0, 0));

    JPanel imageOriginPanel = new JPanel(new GridBagLayout());
    imageOriginPanel.add(this.imageOriginLabel, new GridBagConstraints(
        0, 0, 4, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.NONE, new Insets(5, 0, 5, 0), 0, 0));
    imageOriginPanel.add(this.imageXOriginLabel, new GridBagConstraints(
        0, 1, 1, 1, 0, 1, GridBagConstraints.WEST, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
    imageOriginPanel.add(this.imageXOriginSpinner, new GridBagConstraints(
        1, 1, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 10), -10, 0));
    imageOriginPanel.add(this.imageYOriginLabel, new GridBagConstraints(
        2, 1, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
    imageOriginPanel.add(this.imageYOriginSpinner, new GridBagConstraints(
        3, 1, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 0), -10, 0));
    imageOriginPanel.add(this.imageOriginPreviewComponent, new GridBagConstraints(
        0, 2, 4, 1, 1, 1, GridBagConstraints.CENTER, 
        GridBagConstraints.NONE, new Insets(5, 0, 5, 0), 0, 0));

    add(imageChoicePanel, BackgroundImageWizardController.Step.CHOICE.name());
    add(imageScalePanel, BackgroundImageWizardController.Step.SCALE.name());
    add(imageOriginPanel, BackgroundImageWizardController.Step.ORIGIN.name());
  }
  
  /**
   * Switches to the component card maching <code>step</code>.   
   */
  public void setStep(BackgroundImageWizardController.Step step) {
    this.cardLayout.show(this, step.name());
  }

  /**
   * Updates components initial values from <code>backgroundImage</code>. 
   */
  private void updateComponents(final BackgroundImage backgroundImage) {
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
                    ((NullableSpinner.NullableSpinnerLengthModel)imageScaleDistanceSpinner.getModel()).setLength(
                        backgroundImage.getScaleDistance());
                    imageScalePreviewComponent.setScaleDistancePoints(backgroundImage.getScaleDistanceXStart(),
                        backgroundImage.getScaleDistanceYStart(), backgroundImage.getScaleDistanceXEnd(),
                        backgroundImage.getScaleDistanceYEnd());
                    ((NullableSpinner.NullableSpinnerLengthModel)imageXOriginSpinner.getModel()).setLength(
                        backgroundImage.getXOrigin());
                    ((NullableSpinner.NullableSpinnerLengthModel)imageYOriginSpinner.getModel()).setLength(
                        backgroundImage.getYOrigin());
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
   * Updates components values from <code>imageContent</code>.
   */
  private void updateComponents(final Content imageContent) {
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
                  ((NullableSpinner.NullableSpinnerLengthModel)imageScaleDistanceSpinner.getModel()).setNullable(true);
                  ((NullableSpinner.NullableSpinnerLengthModel)imageScaleDistanceSpinner.getModel()).setLength(null);
                  float scaleDistanceXStart = readImage.getWidth() * 0.1f;
                  float scaleDistanceYStart = readImage.getHeight() / 2f;
                  float scaleDistanceXEnd = readImage.getWidth() * 0.9f;
                  imageScalePreviewComponent.setScaleDistancePoints(scaleDistanceXStart, scaleDistanceYStart, 
                      scaleDistanceXEnd, scaleDistanceYStart);
                  ((NullableSpinner.NullableSpinnerLengthModel)imageXOriginSpinner.getModel()).setLength(0f);
                  ((NullableSpinner.NullableSpinnerLengthModel)imageYOriginSpinner.getModel()).setLength(0f);
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
            getResource("resources/wait.png"));
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
    this.imageScalePreviewComponent.setImage(image);
    this.imageOriginPreviewComponent.setImage(image);
  }

  /**
   * Sets the texts of label and button of image choice panel with
   * change texts. 
   */
  private void setImageChangeTexts() {
    this.imageChoiceOrChangeLabel.setText(this.resource.getString("imageChangeLabel.text")); 
    this.imageChoiceOrChangeButton.setText(this.resource.getString("imageChangeButton.text"));
    if (!System.getProperty("os.name").startsWith("Mac OS X")) {
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
    if (!System.getProperty("os.name").startsWith("Mac OS X")) {
      this.imageChoiceOrChangeButton.setMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("imageChoiceButton.mnemonic")).getKeyCode());
    }
  }
  
  /**
   * Returns a content choosen for a file chooser dialog. You may 
   * override this method to use a different source of image.
   */
  protected Content showImageChoiceDialog() {
    String file = FileUtilities.showOpenFileDialog(this, 
        this.resource.getString("imageChoiceDialog.title"), IMAGE_FILTERS);
    if (file != null) {
      try {
        // Copy chosen file in a temporary file to ensure the image will 
        // still be available at saving time even if user moved it meanwhile   
        return FileUtilities.copyToTempFile(file);
      } catch (IOException ex) {
        JOptionPane.showMessageDialog(this, 
            String.format(this.resource.getString("imageChoiceError"), file));
      }
    }
    return null;
  }

  /**
   * Preview component for image choice. 
   */
  private static class ImagePreviewComponent extends JComponent {
    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 200;
    private BufferedImage image;

    @Override
    public Dimension getPreferredSize() {
      if (image == null) { 
        return new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
      } else {
        if (image.getWidth() < DEFAULT_WIDTH && image.getHeight() < DEFAULT_HEIGHT) {
          return new Dimension(image.getWidth(), image.getHeight());
        } else if (image.getWidth() * DEFAULT_HEIGHT / DEFAULT_WIDTH > image.getHeight()) {
          return new Dimension(DEFAULT_WIDTH, image.getHeight() * DEFAULT_WIDTH / image.getWidth());
        } else {
          return new Dimension(image.getWidth() * DEFAULT_HEIGHT / image.getHeight(), DEFAULT_HEIGHT);
        }
      }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
      paintImage(g, null);
    }

    /**
     * Paints the image with a given <code>composite</code>. 
     * Image is scaled to fill width of the component. 
     */
    protected void paintImage(Graphics g, AlphaComposite composite) {
      if (image != null) {
        Graphics2D g2D = (Graphics2D)g;
        g2D.setRenderingHint(RenderingHints.KEY_RENDERING, 
            RenderingHints.VALUE_RENDER_QUALITY);
        AffineTransform oldTransform = g2D.getTransform();
        Composite oldComposite = g2D.getComposite();
        float scale = getPreviewScale();
        g2D.scale(scale, scale);    
        
        if (composite != null) {
          g2D.setComposite(composite);
        }
        // Draw image with composite
        g2D.drawImage(this.image, 0, 0, this);
        g2D.setComposite(oldComposite);
        g2D.setTransform(oldTransform);
      }
    }
    
    /**
     * Sets the image drawn by this component.
     */
    public void setImage(BufferedImage image) {
      this.image = image;
      this.revalidate();
      this.repaint();
    }

    /**
     * Returns the image drawn by this component.
     */
    public BufferedImage getImage() {
      return this.image;
    }
    
    /**
     * Returns the scale used to draw the image of this component.
     */
    public float getPreviewScale() {
      if (image != null) {
        return (float)getWidth() / image.getWidth();
      } else {
        return 0;
      }
    }
  }
  
  /**
   * Preview component for image scale distance choice. 
   */
  private static class ScaleImagePreviewComponent extends ImagePreviewComponent {
    private BackgroundImageWizardController controller;

    public ScaleImagePreviewComponent(BackgroundImageWizardController controller) {
      this.controller = controller;
      addMouseListeners();
      setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }
    
    /**
     * Sets the scale distance start and end points. 
     */
    public void setScaleDistancePoints(float scaleDistanceXStart, float scaleDistanceYStart, 
                                       float scaleDistanceXEnd, float scaleDistanceYEnd) {
      this.controller.setScaleDistancePoints(scaleDistanceXStart, scaleDistanceYStart, 
          scaleDistanceXEnd, scaleDistanceYEnd);
    }

    /**
     * Adds to this component a mouse listeners that allows the user to move the start point 
     * or the end point of the scale distance line.
     */
    public void addMouseListeners() {
      MouseInputAdapter mouseListener = new MouseInputAdapter() {
        private int     lastX;
        private int     lastY;
        private boolean inComponent;
        private boolean distanceStartPoint;
        private boolean distanceEndPoint;
        
        @Override
        public void mousePressed(MouseEvent ev) {
          if (!ev.isPopupTrigger()) {
            this.inComponent = true;
            this.distanceStartPoint = 
            this.distanceEndPoint = false;
            float [][] scaleDistancePoints = controller.getScaleDistancePoints();
            // Check if user clicked on start or end point of distance line
            if (Math.abs(scaleDistancePoints [0][0] * getPreviewScale() - ev.getX()) < 2
                && Math.abs(scaleDistancePoints [0][1] * getPreviewScale() - ev.getY()) < 2) {
              this.lastX = ev.getX();
              this.lastY = ev.getY();
              this.distanceStartPoint = true;
            } else if (Math.abs(scaleDistancePoints [1][0] * getPreviewScale() - ev.getX()) < 2
                       && Math.abs(scaleDistancePoints [1][1] * getPreviewScale() - ev.getY()) < 2) {
              this.lastX = ev.getX();
              this.lastY = ev.getY();
              this.distanceEndPoint = true;
            } 
          }
        }

        @Override
        public void mouseDragged(MouseEvent ev) {
          if (this.inComponent) {
            float [][] scaleDistancePoints = controller.getScaleDistancePoints();
            if (this.distanceStartPoint) {
              // Compute start point of distance line
              scaleDistancePoints [0][0] += (ev.getX() - this.lastX) / getPreviewScale(); 
              scaleDistancePoints [0][1] += (ev.getY() - this.lastY) / getPreviewScale();
            } else if (this.distanceEndPoint) {
              // Compute end point of distance line
              scaleDistancePoints [1][0] += (ev.getX() - this.lastX) / getPreviewScale(); 
              scaleDistancePoints [1][1] += (ev.getY() - this.lastY) / getPreviewScale();
            }
            
            // Accept new points only if distance is greater that 2 pixels
            if ((this.distanceStartPoint
                 || this.distanceEndPoint)
                && Point2D.distanceSq(scaleDistancePoints [0][0] * getPreviewScale(), 
                    scaleDistancePoints [0][1] * getPreviewScale(),
                    scaleDistancePoints [1][0] * getPreviewScale(), 
                    scaleDistancePoints [1][1] * getPreviewScale()) >= 4) {
              setScaleDistancePoints(
                  scaleDistancePoints [0][0], scaleDistancePoints [0][1],
                  scaleDistancePoints [1][0], scaleDistancePoints [1][1]);
              repaint();
            }

            this.lastX = ev.getX();
            this.lastY = ev.getY();
          }
        }
        
        @Override
        public void mouseExited(MouseEvent ev) {
          this.inComponent = false;
        }
        
        @Override
        public void mouseEntered(MouseEvent ev) {
          this.inComponent = true;
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
        
        // Fill background
        g2D.setColor(UIManager.getColor("window"));
        g2D.fillRect(0, 0, getWidth(), getHeight());
        
        // Paint image with a 0.5 alpha
        paintImage(g, AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));        

        // Use same scale as image drawing in super class
        float scale = getPreviewScale();
        g2D.scale(scale, scale);
        
        Color scaleDistanceLineColor = UIManager.getColor("textHighlight");
        g2D.setPaint(scaleDistanceLineColor);
        
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
        AffineTransform oldTransform = g2D.getTransform();
        g2D.translate(scaleDistancePoints [0][0], scaleDistancePoints [0][1]);
        g2D.rotate(angle);
        Shape endLine = new Line2D.Double(0, 5 / scale, 0, -5 / scale);
        g2D.draw(endLine);
        g2D.setTransform(oldTransform);
        
        // Draw end point line
        g2D.translate(scaleDistancePoints [1][0], scaleDistancePoints [1][1]);
        g2D.rotate(angle);
        g2D.draw(endLine);
      }
    }
  }
  
  /**
   * Preview component for image scale distance choice. 
   */
  private static class OriginImagePreviewComponent extends ImagePreviewComponent {
    private BackgroundImageWizardController controller;

    public OriginImagePreviewComponent(BackgroundImageWizardController controller) {
      this.controller = controller;
      addChangeListeners(controller);
      addMouseListener(controller);
      setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    /**
     * Adds listeners to <code>controller</code> 
     * to update the location of the origin drawn by this component.
     */
    private void addChangeListeners(final BackgroundImageWizardController controller) {
      PropertyChangeListener originChangeListener = new PropertyChangeListener () {
          public void propertyChange(PropertyChangeEvent evt) {
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
            if (!ev.isPopupTrigger()) {
              float [][] scaleDistancePoints = controller.getScaleDistancePoints();
              float scale = getPreviewScale() / BackgroundImage.getScale(controller.getScaleDistance(), 
                  scaleDistancePoints [0][0], scaleDistancePoints [0][1], 
                  scaleDistancePoints [1][0], scaleDistancePoints [1][1]);
              controller.setOrigin(Math.round(ev.getX() / scale * 10) / 10.f, 
                  Math.round(ev.getY() / scale * 10) / 10.f);
            }
          }
          
          @Override
          public void mouseDragged(MouseEvent ev) {
            mousePressed(ev);
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
        
        // Fill background
        g2D.setColor(UIManager.getColor("window"));
        g2D.fillRect(0, 0, getWidth(), getHeight());
        
        // Paint image with a 0.5 alpha 
        paintImage(g, AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));        
        
        Color scaleDistanceLineColor = UIManager.getColor("textHighlight");
        g2D.setPaint(scaleDistanceLineColor);
        // Rescale according to scale distance
        float [][] scaleDistancePoints = this.controller.getScaleDistancePoints();
        float scale = getPreviewScale() / BackgroundImage.getScale(this.controller.getScaleDistance(), 
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
      }
    }
  }
}
