/*
 * VideoPanel.java 15 fevr. 2010
 *
 * Sweet Home 3D, Copyright (c) 2010 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.MediaLocator;
import javax.media.Time;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PullBufferStream;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultListCellRenderer;
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
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.vecmath.Point3f;

import com.eteks.sweethome3d.j3d.Component3DManager;
import com.eteks.sweethome3d.j3d.PhotoRenderer;
import com.eteks.sweethome3d.model.AspectRatio;
import com.eteks.sweethome3d.model.Camera;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.ObserverCamera;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.tools.ResourceURLContent;
import com.eteks.sweethome3d.viewcontroller.ContentManager;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.VideoController;
import com.eteks.sweethome3d.viewcontroller.View;

/**
 * A panel used for video creation. 
 * @author Emmanuel Puybaret
 */
public class VideoPanel extends JPanel implements DialogView {
  private enum ActionType {START_VIDEO_CREATION, STOP_VIDEO_CREATION, SAVE_VIDEO, CLOSE, 
      DELETE_CAMERA_PATH, PLAYBACK, PAUSE, RECORD, SEEK_BACKWARD, SEEK_FORWARD, SKIP_BACKWARD, SKIP_FORWARD, DELETE_LAST_RECORD}

  private static final String VIDEO_DIALOG_X_VISUAL_PROPERTY = "com.eteks.sweethome3d.swing.VideoPanel.VideoDialogX";
  private static final String VIDEO_DIALOG_Y_VISUAL_PROPERTY = "com.eteks.sweethome3d.swing.VideoPanel.VideoDialogY";

  private static final int MINIMUM_DELAY_BEFORE_DISCARDING_WITHOUT_WARNING = 30000;
  
  private static final VideoFormat [] VIDEO_FORMATS = {
      new VideoFormat(VideoFormat.JPEG, new Dimension(176, 132), Format.NOT_SPECIFIED, Format.byteArray, 12), // 4/3
      new VideoFormat(VideoFormat.JPEG, new Dimension(320, 240), Format.NOT_SPECIFIED, Format.byteArray, 25),
      new VideoFormat(VideoFormat.JPEG, new Dimension(480, 360), Format.NOT_SPECIFIED, Format.byteArray, 25),
      new VideoFormat(VideoFormat.JPEG, new Dimension(640, 480), Format.NOT_SPECIFIED, Format.byteArray, 25),
      new VideoFormat(VideoFormat.JPEG, new Dimension(720, 540), Format.NOT_SPECIFIED, Format.byteArray, 25),
      new VideoFormat(VideoFormat.JPEG, new Dimension(1024, 768), Format.NOT_SPECIFIED, Format.byteArray, 25),
      new VideoFormat(VideoFormat.JPEG, new Dimension(1280, 960), Format.NOT_SPECIFIED, Format.byteArray, 25),
      new VideoFormat(VideoFormat.JPEG, new Dimension(720, 405), Format.NOT_SPECIFIED, Format.byteArray, 25), // 16/9
      new VideoFormat(VideoFormat.JPEG, new Dimension(1280, 720), Format.NOT_SPECIFIED, Format.byteArray, 25),
      new VideoFormat(VideoFormat.JPEG, new Dimension(1920, 1080), Format.NOT_SPECIFIED, Format.byteArray, 25)};

  private static final String TIP_CARD      = "tip";
  private static final String PROGRESS_CARD = "progress";

  private final Home            home;
  private final UserPreferences preferences;
  private final VideoController controller;
  private PlanComponent         planComponent; 
  private JToolBar              videoToolBar;
  private JButton               playbackPauseButton;
  private Timer                 playbackTimer;
  private ListIterator<Camera>  cameraPathIterator;
  private CardLayout            statusLayout;
  private JPanel                statusPanel;
  private JLabel                tipLabel;
  private JLabel                progressLabel;
  private JProgressBar          progressBar;
  private JLabel                videoFormatLabel;
  private JComboBox             videoFormatComboBox;
  private String                videoFormatComboBoxFormat;
  private JLabel                qualityLabel;
  private JSlider               qualitySlider;
  private Component             advancedComponentsSeparator;
  private JLabel                dateLabel;
  private JSpinner              dateSpinner;
  private JLabel                timeLabel;
  private JSpinner              timeSpinner;
  private JLabel                dayNightLabel;
  private JCheckBox             ceilingLightEnabledCheckBox;
  private String                dialogTitle;
  private ExecutorService       videoCreationExecutor;
  private long                  videoCreationStartTime;
  private File                  videoFile;
  private JButton               createButton;
  private JButton               saveButton;
  private JButton               closeButton;

  private static VideoPanel     currentVideoPanel; // Support only one video panel opened at a time

  /**
   * Creates a video panel.
   */
  public VideoPanel(Home home, 
                    UserPreferences preferences, 
                    VideoController controller) {
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
    actions.put(ActionType.PLAYBACK, 
        new ResourceAction(preferences, VideoPanel.class, ActionType.PLAYBACK.name()) {
          @Override
          public void actionPerformed(ActionEvent ev) {
            playback();
          }
        });
    actions.put(ActionType.PAUSE, 
        new ResourceAction(preferences, VideoPanel.class, ActionType.PAUSE.name(), true) {
          @Override
          public void actionPerformed(ActionEvent ev) {
            pausePlayback();
          }
        });
    actions.put(ActionType.RECORD, 
        new ResourceAction(preferences, VideoPanel.class, ActionType.RECORD.name(), true) {
          @Override
          public void actionPerformed(ActionEvent ev) {
            recordCameraLocation();
          }
        });
    actions.put(ActionType.SEEK_BACKWARD, 
        new ResourceAction(preferences, VideoPanel.class, ActionType.SEEK_BACKWARD.name()) {
          @Override
          public void actionPerformed(ActionEvent ev) {
            seekBackward();
          }
        });
    actions.put(ActionType.SEEK_FORWARD, 
        new ResourceAction(preferences, VideoPanel.class, ActionType.SEEK_FORWARD.name()) {
          @Override
          public void actionPerformed(ActionEvent ev) {
            seekForward();
          }
        });
    actions.put(ActionType.SKIP_BACKWARD, 
        new ResourceAction(preferences, VideoPanel.class, ActionType.SKIP_BACKWARD.name()) {
          @Override
          public void actionPerformed(ActionEvent ev) {
            skipBackward();
          }
        });
    actions.put(ActionType.SKIP_FORWARD, 
        new ResourceAction(preferences, VideoPanel.class, ActionType.SKIP_FORWARD.name()) {
          @Override
          public void actionPerformed(ActionEvent ev) {
            skipForward();
          }
        });
    actions.put(ActionType.DELETE_LAST_RECORD, 
        new ResourceAction(preferences, VideoPanel.class, ActionType.DELETE_LAST_RECORD.name()) {
          @Override
          public void actionPerformed(ActionEvent ev) {
            deleteLastRecordedCameraLocation();
          }
        });
    actions.put(ActionType.DELETE_CAMERA_PATH, 
        new ResourceAction(preferences, VideoPanel.class, ActionType.DELETE_CAMERA_PATH.name()) {
          @Override
          public void actionPerformed(ActionEvent ev) {
            deleteCameraPath();
          }
        });
    
    actions.put(ActionType.START_VIDEO_CREATION, 
        new ResourceAction(preferences, VideoPanel.class, ActionType.START_VIDEO_CREATION.name()) {
          @Override
          public void actionPerformed(ActionEvent ev) {
            startVideoCreation();
          }
        });
    actions.put(ActionType.STOP_VIDEO_CREATION, 
        new ResourceAction(preferences, VideoPanel.class, ActionType.STOP_VIDEO_CREATION.name(), true) {
          @Override
          public void actionPerformed(ActionEvent ev) {
            stopVideoCreation(true);
          }
        });
    actions.put(ActionType.SAVE_VIDEO, 
        new ResourceAction(preferences, VideoPanel.class, ActionType.SAVE_VIDEO.name()) {
          @Override
          public void actionPerformed(ActionEvent ev) {
            saveVideo();
          }
        });
    actions.put(ActionType.CLOSE, 
        new ResourceAction(preferences, VideoPanel.class, ActionType.CLOSE.name(), true) {
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
                                final VideoController controller) {
    final Dimension preferredSize = new Dimension(getToolkit().getScreenSize().width <= 1024 ? 324 : 404, 404);
    this.planComponent = new PlanComponent(home, preferences, null) {
        private void updateScale() {
          if (getWidth() > 0 && getHeight() > 0) {
            // Adapt scale to always view the home  
            float oldScale = getScale();
            Dimension preferredSize = super.getPreferredSize();
            Insets insets = getInsets();
            float planWidth = (preferredSize.width - insets.left - insets.right) / oldScale;
            float planHeight = (preferredSize.height - insets.top - insets.bottom) / oldScale;          
            setScale(Math.min((getWidth() - insets.left - insets.right) / planWidth, 
                (getHeight() - insets.top - insets.bottom) / planHeight));
          }
        }
        
        @Override
        public Dimension getPreferredSize() {
          return preferredSize;
        }
        
        @Override
        public void revalidate() {
          super.revalidate();
          updateScale();
        }

        @Override
        public void setBounds(int x, int y, int width, int height) {
          super.setBounds(x, y, width, height);
          updateScale();
        }
        
        @Override
        protected List<Selectable> getPaintedItems() {
          List<Selectable> paintedItems = super.getPaintedItems();
          // Take into account camera locations in plan bounds
          for (Camera camera : controller.getCameraPath()) {
            paintedItems.add(new ObserverCamera(camera.getX(), camera.getY(), camera.getZ(), 
                camera.getYaw(), camera.getPitch(), camera.getFieldOfView()));
          }
          return paintedItems;
        }
        
        @Override
        protected Rectangle2D getItemBounds(Graphics g, Selectable item) {
          if (item instanceof ObserverCamera) {
            return new Rectangle2D.Float(((ObserverCamera)item).getX() - 1, ((ObserverCamera)item).getY() - 1, 2, 2);
          } else {
            return super.getItemBounds(g, item);
          }
        }
        
        @Override
        protected void paintComponent(Graphics g) {
          Graphics2D g2D = (Graphics2D)g;
          g2D.setColor(getBackground());
          g2D.fillRect(0, 0, getWidth(), getHeight());
          super.paintComponent(g);
        }

        @Override
        protected void paintHomeItems(Graphics g, float planScale, Color backgroundColor, Color foregroundColor,
                                      PaintMode paintMode) throws InterruptedIOException {
          Graphics2D g2D = (Graphics2D)g;
          Composite oldComposite = g2D.getComposite();
          g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
          super.paintHomeItems(g, planScale, backgroundColor, foregroundColor, paintMode);
          
          // Paint recorded camera path
          g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
          g2D.setColor(getSelectionColor());
          float cameraCircleRadius = 7 / getScale();
          Ellipse2D ellipse = new Ellipse2D.Float(-cameraCircleRadius, -cameraCircleRadius, 
              2 * cameraCircleRadius, 2 * cameraCircleRadius);
          List<Camera> cameraPath = controller.getCameraPath();
          for (int i = 0; i < cameraPath.size(); i++) {
            Camera camera = cameraPath.get(i);
            AffineTransform previousTransform = g2D.getTransform();
            g2D.translate(camera.getX(), camera.getY());
            g2D.rotate(camera.getYaw());
            // Paint camera location
            g2D.fill(ellipse);
            // Paint field of sight angle
            double sin = (float)Math.sin(camera.getFieldOfView() / 2);
            double cos = (float)Math.cos(camera.getFieldOfView() / 2);
            float xStartAngle = (float)(1.2f * cameraCircleRadius * sin);
            float yStartAngle = (float)(1.2f * cameraCircleRadius * cos);
            float xEndAngle = (float)(2.5f * cameraCircleRadius * sin);
            float yEndAngle = (float)(2.5f * cameraCircleRadius * cos);
            GeneralPath cameraFieldOfViewAngle = new GeneralPath();
            g2D.setStroke(new BasicStroke(1 / getScale()));
            cameraFieldOfViewAngle.moveTo(xStartAngle, yStartAngle);
            cameraFieldOfViewAngle.lineTo(xEndAngle, yEndAngle);
            cameraFieldOfViewAngle.moveTo(-xStartAngle, yStartAngle);
            cameraFieldOfViewAngle.lineTo(-xEndAngle, yEndAngle);
            g2D.draw(cameraFieldOfViewAngle);
            g2D.setTransform(previousTransform);
            
            if (i > 0) {
              g2D.setStroke(new BasicStroke(2 / getScale()));
              g2D.draw(new Line2D.Float(camera.getX(), camera.getY(), 
                  cameraPath.get(i - 1).getX(), cameraPath.get(i - 1).getY()));
            }
          }
          g2D.setComposite(oldComposite);
        }
      };
    this.planComponent.setSelectedItemsOutlinePainted(false);
    this.planComponent.setBackgroundPainted(false);
    this.planComponent.setBorder(BorderFactory.createEtchedBorder());
    this.controller.addPropertyChangeListener(VideoController.Property.CAMERA_PATH, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent evt) {
            planComponent.revalidate();
            updatePlaybackTimer();
          }
        });

    // Create tool bar to play recorded animation in 3D view 
    this.videoToolBar = new JToolBar();
    this.videoToolBar.setFloatable(false);
    final ActionMap actionMap = getActionMap();
    this.videoToolBar.add(actionMap.get(ActionType.DELETE_CAMERA_PATH));
    this.videoToolBar.addSeparator();
    this.videoToolBar.add(actionMap.get(ActionType.SKIP_BACKWARD));
    this.videoToolBar.add(actionMap.get(ActionType.SEEK_BACKWARD));
    this.videoToolBar.add(actionMap.get(ActionType.RECORD));
    this.playbackPauseButton = new JButton(actionMap.get(ActionType.PLAYBACK));
    this.videoToolBar.add(this.playbackPauseButton);
    this.videoToolBar.add(actionMap.get(ActionType.SEEK_FORWARD));
    this.videoToolBar.add(actionMap.get(ActionType.SKIP_FORWARD));
    this.videoToolBar.addSeparator();
    this.videoToolBar.add(actionMap.get(ActionType.DELETE_LAST_RECORD));
    for (int i = 0; i < videoToolBar.getComponentCount(); i++) {
      Component component = this.videoToolBar.getComponent(i);
      if (component instanceof JButton) {
        JButton button = (JButton)component;
        button.setBorderPainted(true);
        button.setFocusable(true);
      }
    }

    this.tipLabel = new JLabel();
    Font toolTipFont = UIManager.getFont("ToolTip.font");
    this.tipLabel.setFont(toolTipFont);
    
    this.progressLabel = new JLabel();
    this.progressLabel.setFont(toolTipFont);
    this.progressLabel.setHorizontalAlignment(JLabel.CENTER);
    
    this.progressBar = new JProgressBar();
    this.progressBar.setIndeterminate(true);
    this.progressBar.getModel().addChangeListener(new ChangeListener() {
        private long timeAfterFirstImage; 
        
        public void stateChanged(ChangeEvent ev) {
          int progressValue = progressBar.getValue();
          progressBar.setIndeterminate(progressValue <= progressBar.getMinimum() + 1);
          if (progressValue == progressBar.getMinimum()
              || progressValue == progressBar.getMaximum()) {
            progressLabel.setText("");
            if (progressValue == progressBar.getMinimum()) {
              int framesCount = progressBar.getMaximum() - progressBar.getMinimum();
              String progressLabelFormat = preferences.getLocalizedString(VideoPanel.class, "progressStartLabel.format");
              progressLabel.setText(String.format(progressLabelFormat, framesCount,
                  formatDuration(framesCount * 1000 / controller.getFrameRate())));
            }
          } else if (progressValue == progressBar.getMinimum() + 1) {
            this.timeAfterFirstImage = System.currentTimeMillis(); 
          } else {
            // Update progress label once the second image is generated 
            // (the first one can take more time because of initialization process)
            String progressLabelFormat = preferences.getLocalizedString(VideoPanel.class, "progressLabel.format");
            long estimatedRemainingTime = (System.currentTimeMillis() - this.timeAfterFirstImage) 
                / (progressValue - 1 - progressBar.getMinimum())  
                * (progressBar.getMaximum() - progressValue - 1);
            String estimatedRemainingTimeText = formatDuration(estimatedRemainingTime);
            progressLabel.setText(String.format(progressLabelFormat, 
                progressValue, progressBar.getMaximum(), estimatedRemainingTimeText));
          }          
        }

        /**
         * Returns a localized string of <code>duration</code> in millis.
         */
        private String formatDuration(long duration) {
          long durationInSeconds = duration / 1000;
          if (duration - durationInSeconds * 1000 >= 500) {
            durationInSeconds++;
          }
          String estimatedRemainingTimeText;
          if (durationInSeconds < 60) {
            estimatedRemainingTimeText = String.format(preferences.getLocalizedString(
                VideoPanel.class, "seconds.format"), durationInSeconds);
          } else if (durationInSeconds < 3600) {
            estimatedRemainingTimeText = String.format(preferences.getLocalizedString(
                VideoPanel.class, "minutesSeconds.format"), durationInSeconds / 60, durationInSeconds % 60);
          } else {
            long hours = durationInSeconds / 3600;
            long minutes = (durationInSeconds % 3600) / 60;
            estimatedRemainingTimeText = String.format(preferences.getLocalizedString(
                VideoPanel.class, "hoursMinutes.format"), hours, minutes);
          }
          return estimatedRemainingTimeText;
        }
      });
    
    // Create video format label and combo box bound to WIDTH, HEIGHT, ASPECT_RATIO and FRAME_RATE controller properties
    this.videoFormatLabel = new JLabel();
    this.videoFormatComboBox = new JComboBox(VIDEO_FORMATS);
    this.videoFormatComboBox.setMaximumRowCount(VIDEO_FORMATS.length);
    this.videoFormatComboBox.setRenderer(new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, 
                                                      int index, boolean isSelected, boolean cellHasFocus) {
          VideoFormat videoFormat = (VideoFormat)value;
          String aspectRatio;
          switch (getAspectRatio(videoFormat)) {
            case RATIO_4_3 :
              aspectRatio = "4/3";
              break;
            case RATIO_16_9 :
            default :
              aspectRatio = "16/9";
              break;
          }
          Dimension videoSize = videoFormat.getSize();
          String displayedValue = String.format(videoFormatComboBoxFormat, videoSize.width, videoSize.height,
              aspectRatio, (int)videoFormat.getFrameRate());          
          return super.getListCellRendererComponent(list, displayedValue, index, isSelected,
              cellHasFocus);
        }
      });
    this.videoFormatComboBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          controller.setWidth(((VideoFormat)videoFormatComboBox.getSelectedItem()).getSize().width);
          controller.setAspectRatio(getAspectRatio((VideoFormat)videoFormatComboBox.getSelectedItem()));
          controller.setFrameRate((int)((VideoFormat)videoFormatComboBox.getSelectedItem()).getFrameRate());
        }
      });
    PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          videoFormatComboBox.setSelectedItem(controller.getAspectRatio());
        }
      };
    controller.addPropertyChangeListener(VideoController.Property.WIDTH, propertyChangeListener);
    controller.addPropertyChangeListener(VideoController.Property.HEIGHT, propertyChangeListener);
    controller.addPropertyChangeListener(VideoController.Property.ASPECT_RATIO, propertyChangeListener);
    controller.addPropertyChangeListener(VideoController.Property.FRAME_RATE, propertyChangeListener);

    // Quality label and slider bound to QUALITY controller property
    this.qualityLabel = new JLabel();
    this.qualitySlider = new JSlider(1, controller.getQualityLevelCount()) {
        @Override
        public String getToolTipText(MouseEvent ev) {
          float valueUnderMouse = getSliderValueAt(this, ev.getX(), preferences);
          float valueToTick = valueUnderMouse - (float)Math.floor(valueUnderMouse);
          if (valueToTick < 0.25f || valueToTick > 0.75f) {
            // Display a tooltip that explains the different quality levels
            return "<html><table><tr valign='middle'>"
                + "<td><img border='1' src='" 
                + new ResourceURLContent(PhotoPanel.class, "resources/quality" + Math.round(valueUnderMouse - qualitySlider.getMinimum()) + ".jpg").getURL() + "'></td>"
                + "<td>" + preferences.getLocalizedString(VideoPanel.class, "quality" + Math.round(valueUnderMouse - qualitySlider.getMinimum()) + "DescriptionLabel.text") + "</td>"
                + "</tr></table>";
          } else {
            return null;
          }
        }
      };
    // Add a listener that displays also the tool tip when user clicks on the slider
    this.qualitySlider.addMouseListener(new MouseAdapter() {
        @Override
        public void mousePressed(final MouseEvent ev) {
          EventQueue.invokeLater(new Runnable() {
              public void run() {
                float valueUnderMouse = getSliderValueAt(qualitySlider, ev.getX(), preferences);
                if (qualitySlider.getValue() == Math.round(valueUnderMouse)) {
                  ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
                  int initialDelay = toolTipManager.getInitialDelay();
                  toolTipManager.setInitialDelay(Math.min(initialDelay, 150));
                  toolTipManager.mouseMoved(ev);
                  toolTipManager.setInitialDelay(initialDelay);
                }
              }
            });
        }
      });
    this.qualitySlider.setPaintLabels(true);
    this.qualitySlider.setPaintTicks(true);    
    this.qualitySlider.setMajorTickSpacing(1);
    this.qualitySlider.setSnapToTicks(true);
    final boolean offScreenImageSupported = Component3DManager.getInstance().isOffScreenImageSupported();
    this.qualitySlider.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (!offScreenImageSupported) {
            // Can't support 2 first quality levels if offscreen image isn't supported 
            qualitySlider.setValue(Math.max(qualitySlider.getMinimum() + 2, qualitySlider.getValue()));
          }
          controller.setQuality(qualitySlider.getValue() - qualitySlider.getMinimum());
        }
      });
    controller.addPropertyChangeListener(VideoController.Property.QUALITY, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            qualitySlider.setValue(qualitySlider.getMinimum() + controller.getQuality());
            updateAdvancedComponents();
          }
        });
    this.qualitySlider.setValue(this.qualitySlider.getMinimum() + controller.getQuality());
    
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
    controller.addPropertyChangeListener(VideoController.Property.TIME, timeChangeListener);
    final ChangeListener dateTimeChangeListener = new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(VideoController.Property.TIME, timeChangeListener);
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
          controller.addPropertyChangeListener(VideoController.Property.TIME, timeChangeListener);
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
    controller.addPropertyChangeListener(VideoController.Property.TIME, dayNightListener);
    home.getCompass().addPropertyChangeListener(dayNightListener);
    dayNightListener.propertyChange(null);
    
    this.ceilingLightEnabledCheckBox = new JCheckBox();
    this.ceilingLightEnabledCheckBox.setSelected(controller.getCeilingLightColor() > 0);
    controller.addPropertyChangeListener(VideoController.Property.CEILING_LIGHT_COLOR, 
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

    this.createButton = new JButton(actionMap.get(ActionType.START_VIDEO_CREATION));
    this.saveButton = new JButton(actionMap.get(ActionType.SAVE_VIDEO));
    this.closeButton = new JButton(actionMap.get(ActionType.CLOSE));

    setComponentTexts(preferences);
    updatePlaybackTimer();

    this.videoFormatComboBox.setSelectedItem(new VideoFormat(VideoFormat.JPEG, 
        new Dimension(controller.getWidth(), controller.getHeight()), Format.NOT_SPECIFIED, Format.byteArray, controller.getFrameRate()));
  }

  /**
   * Returns the slider value matching a given x.
   */
  private float getSliderValueAt(JSlider qualitySlider, int x, UserPreferences preferences) {
    int fastLabelOffset = OperatingSystem.isLinux() 
        ? 0
        : new JLabel(SwingTools.getLocalizedLabelText(preferences,
              VideoPanel.class, "fastLabel.text")).getPreferredSize().width / 2;
    int bestLabelOffset = OperatingSystem.isLinux() 
        ? 0
        : new JLabel(SwingTools.getLocalizedLabelText(preferences,
              VideoPanel.class, "bestLabel.text")).getPreferredSize().width / 2;
    int sliderWidth = qualitySlider.getWidth() - fastLabelOffset - bestLabelOffset;
    return qualitySlider.getMinimum()
        + (float)(x - (qualitySlider.getComponentOrientation().isLeftToRight() 
                          ? fastLabelOffset 
                          : bestLabelOffset))
        / sliderWidth * (qualitySlider.getMaximum() - qualitySlider.getMinimum());
  }
  
  /**
   * Sets the texts of the components.
   */
  private void setComponentTexts(UserPreferences preferences) {
    this.tipLabel.setText(preferences.getLocalizedString(VideoPanel.class, "tipLabel.text"));
    this.videoFormatLabel.setText(preferences.getLocalizedString(
        VideoPanel.class, "videoFormatLabel.text"));
    this.videoFormatComboBoxFormat = preferences.getLocalizedString(
        VideoPanel.class, "videoFormatComboBox.format");
    this.qualityLabel.setText(SwingTools.getLocalizedLabelText(preferences, 
        VideoPanel.class, "qualityLabel.text"));
    this.dateLabel.setText(SwingTools.getLocalizedLabelText(preferences, 
        VideoPanel.class, "dateLabel.text"));
    this.timeLabel.setText(SwingTools.getLocalizedLabelText(preferences, 
        VideoPanel.class, "timeLabel.text"));
    this.ceilingLightEnabledCheckBox.setText(SwingTools.getLocalizedLabelText(preferences, 
        VideoPanel.class, "ceilingLightEnabledCheckBox.text"));
    JLabel fastLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, 
        VideoPanel.class, "fastLabel.text"));
    if (!Component3DManager.getInstance().isOffScreenImageSupported()) {
      fastLabel.setEnabled(false);
    }
    JLabel bestLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, 
        VideoPanel.class, "bestLabel.text"));
    Dictionary<Integer,JComponent> qualitySliderLabelTable = new Hashtable<Integer,JComponent>();
    qualitySliderLabelTable.put(this.qualitySlider.getMinimum(), fastLabel);
    qualitySliderLabelTable.put(this.qualitySlider.getMaximum(), bestLabel);
    this.qualitySlider.setLabelTable(qualitySliderLabelTable);
    this.dialogTitle = preferences.getLocalizedString(VideoPanel.class, "createVideo.title");
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
      this.videoFormatLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(preferences.getLocalizedString(
              VideoPanel.class, "videoFormatLabel.mnemonic")).getKeyCode());
      this.videoFormatLabel.setLabelFor(this.videoFormatComboBox);
      this.qualityLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(preferences.getLocalizedString(
              VideoPanel.class, "qualityLabel.mnemonic")).getKeyCode());
      this.qualityLabel.setLabelFor(this.qualitySlider);
      this.dateLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          VideoPanel.class, "dateLabel.mnemonic")).getKeyCode());
      this.dateLabel.setLabelFor(this.dateSpinner);
      this.timeLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          VideoPanel.class, "timeLabel.mnemonic")).getKeyCode());
      this.timeLabel.setLabelFor(this.timeSpinner);
      this.ceilingLightEnabledCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString( 
          VideoPanel.class, "ceilingLightEnabledCheckBox.mnemonic")).getKeyCode());
    }
  }

  /**
   * Preferences property listener bound to this panel with a weak reference to avoid
   * strong link between user preferences and this panel.  
   */
  public static class LanguageChangeListener implements PropertyChangeListener {
    private final WeakReference<VideoPanel> videoPanel;

    public LanguageChangeListener(VideoPanel videoPanel) {
      this.videoPanel = new WeakReference<VideoPanel>(videoPanel);
    }

    public void propertyChange(PropertyChangeEvent ev) {
      // If video panel was garbage collected, remove this listener from preferences
      VideoPanel videoPanel = this.videoPanel.get();
      UserPreferences preferences = (UserPreferences)ev.getSource();
      if (videoPanel == null) {
        preferences.removePropertyChangeListener(UserPreferences.Property.LANGUAGE, this);
      } else {
        videoPanel.setComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));
        videoPanel.setComponentTexts(preferences);
        videoPanel.setMnemonics(preferences);
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
    // Add tip and progress bar to a card panel 
    this.statusLayout = new CardLayout();
    this.statusPanel = new JPanel(this.statusLayout);
    this.statusPanel.add(this.tipLabel, TIP_CARD);
    this.tipLabel.setMinimumSize(this.tipLabel.getPreferredSize());
    JPanel progressPanel = new JPanel(new BorderLayout(5, 2));
    progressPanel.add(this.progressBar, BorderLayout.NORTH);
    progressPanel.add(this.progressLabel);
    this.statusPanel.add(progressPanel, PROGRESS_CARD);
    // First row
    add(this.planComponent, new GridBagConstraints(
        0, 0, 4, 1, 1, 1, labelAlignment, 
        GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));
    // Second row
    add(this.videoToolBar, new GridBagConstraints(
        0, 1, 4, 1, 0, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 0), 0, 0));
    // Third row
    add(this.statusPanel, new GridBagConstraints(
        0, 2, 4, 1, 0, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.NONE, new Insets(0, 0, 10, 0), 0, 0));
    // Fourth row
    // Add a dummy label at left and right
    add(new JLabel(), new GridBagConstraints(
        0, 3, 1, 6, 0.5f, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    add(new JLabel(), new GridBagConstraints(
        3, 3, 1, 6, 0.5f, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    Insets labelInsets = new Insets(0, 0, 0, 5);
    add(this.videoFormatLabel, new GridBagConstraints(
        1, 3, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    Insets componentInsets = new Insets(0, 0, 0, 10);
    add(this.videoFormatComboBox, new GridBagConstraints(
        2, 3, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, componentInsets, -50, 0));
    // Fifth row
    add(this.qualityLabel, new GridBagConstraints(
        1, 4, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, new Insets(0, 0, 2, 5), 0, 0));
    add(this.qualitySlider, new GridBagConstraints(
        2, 4, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 2, 0), 0, 0));
    // Sixth row
    add(this.advancedComponentsSeparator, new GridBagConstraints(
        1, 6, 4, 1, 0, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.HORIZONTAL, new Insets(3, 0, 3, 0), 0, 0));
    // Seventh row
    JPanel advancedPanel = new JPanel(new GridBagLayout());
    advancedPanel.add(this.dateLabel, new GridBagConstraints(
        1, 7, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
    advancedPanel.add(this.dateSpinner, new GridBagConstraints(
        2, 7, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 10), 1, 0));
    advancedPanel.add(this.timeLabel, new GridBagConstraints(
        3, 7, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
    advancedPanel.add(this.timeSpinner, new GridBagConstraints(
        4, 7, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));
    advancedPanel.add(this.dayNightLabel, new GridBagConstraints(
        5, 7, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
    // Last row
    advancedPanel.add(this.ceilingLightEnabledCheckBox, new GridBagConstraints(
        1, 8, 5, 1, 0, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    
    add(advancedPanel, new GridBagConstraints(
        0, 7, 4, 1, 0, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
  }
  
  private void updateAdvancedComponents() {
    Component root = SwingUtilities.getRoot(this);
    if (root != null) {
      boolean highQuality = controller.getQuality() >= 2;
      boolean advancedComponentsVisible = this.advancedComponentsSeparator.isVisible();
      if (advancedComponentsVisible != highQuality) {
        int componentsHeight = this.advancedComponentsSeparator.getPreferredSize().height + 6
            + this.dateSpinner.getPreferredSize().height + 5
            + this.ceilingLightEnabledCheckBox.getPreferredSize().height;
        this.advancedComponentsSeparator.setVisible(highQuality);
        this.dateLabel.setVisible(highQuality);
        this.dateSpinner.setVisible(highQuality);
        this.timeLabel.setVisible(highQuality);
        this.timeSpinner.setVisible(highQuality);
        this.dayNightLabel.setVisible(highQuality);
        this.ceilingLightEnabledCheckBox.setVisible(highQuality);
        root.setSize(root.getWidth(), 
            root.getHeight() + (advancedComponentsVisible ? -componentsHeight : componentsHeight));
      }
    }   
  }

  /**
   * Displays this panel in a non modal dialog.
   */
  public void displayView(View parentView) {
    if (currentVideoPanel == this) {
      SwingUtilities.getWindowAncestor(VideoPanel.this).toFront();
    } else {
      if (currentVideoPanel != null) {
        currentVideoPanel.close();
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
        Integer x = (Integer)this.home.getVisualProperty(VIDEO_DIALOG_X_VISUAL_PROPERTY);
        Integer y = (Integer)this.home.getVisualProperty(VIDEO_DIALOG_Y_VISUAL_PROPERTY);      

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
          // If there some space left at the right of the window
          // move the dialog to the right of window
          dialogLocation = new Point(Math.min(windowRightBorder + 5, screenRightBorder - dialogWidth), 
              Math.max(Math.min(homeRoot.getY() + dialog.getInsets().top, 
                  screenSize.height - dialogHeight - screenInsets.bottom), screenInsets.top));
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
          stopVideoCreation(false);
          if (playbackTimer != null) {
            pausePlayback();
          }
          if (videoFile != null) {
            videoFile.delete();
          }
          currentVideoPanel = null;
        }
      });
      
      updateAdvancedComponents();
      ToolTipManager.sharedInstance().registerComponent(this.qualitySlider);
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
            controller.setVisualProperty(VIDEO_DIALOG_X_VISUAL_PROPERTY, dialog.getX());
            controller.setVisualProperty(VIDEO_DIALOG_Y_VISUAL_PROPERTY, dialog.getY());
          }
        });
      dialog.setVisible(true);
      currentVideoPanel = this;
    }
  }

  /**
   * Records the location and the angles of the current camera.
   */
  private void recordCameraLocation() {
    List<Camera> cameraPath = this.controller.getCameraPath();
    Camera camera = this.home.getCamera();    
    Camera lastCamera = null;    
    if (cameraPath.size() > 0) {
      lastCamera = cameraPath.get(cameraPath.size() - 1);
    }
    if (lastCamera == null      
        || !compareCameraLocation(lastCamera, camera)) {
      // Record only new locations
      cameraPath = new ArrayList<Camera>(cameraPath);
      Camera recordedCamera = camera.clone();
      recordedCamera.setLens(Camera.Lens.PINHOLE);
      recordedCamera.setTime(this.controller.getTime());
      cameraPath.add(recordedCamera);
      this.controller.setCameraPath(cameraPath);
    }
  }
  
  /**
   * Returns <code>true</code> if the given cameras are at the same location.
   */
  private boolean compareCameraLocation(Camera camera1, Camera camera2) {
    return camera1.getX() == camera2.getX() 
        && camera1.getY() == camera2.getY() 
        && camera1.getZ() == camera2.getZ() 
        && camera1.getYaw() == camera2.getYaw()
        && camera1.getPitch() == camera2.getPitch() 
        && camera1.getFieldOfView() == camera2.getFieldOfView()
        && camera1.getTime() == camera2.getTime();
  }

  /**
   * Updates the timer used for playback.
   */
  private void updatePlaybackTimer() {
    final List<Camera> cameraPath = this.controller.getCameraPath();
    final ActionMap actionMap = getActionMap();
    boolean playable = cameraPath.size() > 1;
    if (playable) {
      Camera [] videoFramesPath = getVideoFramesPath(12);
      // Find current camera location
      Camera homeCamera = home.getCamera();
      int index = videoFramesPath.length;
      while (--index > 0 
          && !compareCameraLocation(videoFramesPath [index], homeCamera)) {        
      }
      // Prefer last location
      if (index < 0 || index == videoFramesPath.length - 1) {
        index = videoFramesPath.length;
      }
      this.cameraPathIterator = Arrays.asList(videoFramesPath).listIterator(index);
      ActionListener playbackAction = new ActionListener() {
          public void actionPerformed(ActionEvent ev) {
            if ("backward".equals(ev.getActionCommand())) {
              if (cameraPathIterator.hasPrevious()) {
                Camera camera = cameraPathIterator.previous();
                home.getCamera().setCamera(camera);
                controller.setTime(camera.getTime());
              } else {
                pausePlayback();
              }
            } else {
              if (cameraPathIterator.hasNext()) {
                Camera camera = cameraPathIterator.next();
                home.getCamera().setCamera(camera);
                controller.setTime(camera.getTime());
              } else {
                pausePlayback();
              }
            }
            boolean pathEditable = videoCreationExecutor == null && !((Timer)ev.getSource()).isRunning();
            actionMap.get(ActionType.RECORD).setEnabled(pathEditable);
            actionMap.get(ActionType.DELETE_CAMERA_PATH).setEnabled(pathEditable && cameraPath.size() > 0);
            actionMap.get(ActionType.DELETE_LAST_RECORD).setEnabled(pathEditable && cameraPath.size() > 0);
            actionMap.get(ActionType.SEEK_BACKWARD).setEnabled(cameraPathIterator.hasPrevious());
            actionMap.get(ActionType.SKIP_BACKWARD).setEnabled(cameraPathIterator.hasPrevious());
            actionMap.get(ActionType.SEEK_FORWARD).setEnabled(cameraPathIterator.hasNext());
            actionMap.get(ActionType.SKIP_FORWARD).setEnabled(cameraPathIterator.hasNext());
          }
        };
      if (this.playbackTimer != null) {
        this.playbackTimer.stop();
      }
      this.playbackTimer = new Timer(1000 / 12, playbackAction);
      this.playbackTimer.setInitialDelay(0);
      this.playbackTimer.setCoalesce(false);
    }
    actionMap.get(ActionType.PLAYBACK).setEnabled(playable);
    actionMap.get(ActionType.RECORD).setEnabled(this.videoCreationExecutor == null);
    boolean emptyCameraPath = cameraPath.isEmpty();
    actionMap.get(ActionType.DELETE_CAMERA_PATH).setEnabled(this.videoCreationExecutor == null && !emptyCameraPath);
    actionMap.get(ActionType.DELETE_LAST_RECORD).setEnabled(this.videoCreationExecutor == null && !emptyCameraPath);
    actionMap.get(ActionType.SEEK_BACKWARD).setEnabled(playable && this.cameraPathIterator.hasPrevious());
    actionMap.get(ActionType.SKIP_BACKWARD).setEnabled(playable && this.cameraPathIterator.hasPrevious());
    actionMap.get(ActionType.SEEK_FORWARD).setEnabled(playable && this.cameraPathIterator.hasNext());
    actionMap.get(ActionType.SKIP_FORWARD).setEnabled(playable && this.cameraPathIterator.hasNext());
    actionMap.get(ActionType.START_VIDEO_CREATION).setEnabled(playable);
  }

  /**
   * Deletes the last recorded camera location.
   */
  private void deleteLastRecordedCameraLocation() {
    List<Camera> cameraPath = new ArrayList<Camera>(this.controller.getCameraPath());
    cameraPath.remove(cameraPath.size() - 1);
    this.controller.setCameraPath(cameraPath);
  }

  /**
   * Deletes the recorded camera path.
   */
  private void deleteCameraPath() {
    List<Camera> cameraPath = Collections.emptyList();
    this.controller.setCameraPath(cameraPath);
  }

  /**
   * Plays back the camera locations. 
   */
  private void playback() {
    if (!this.cameraPathIterator.hasNext()) {
      skipBackward();
    }
    this.playbackTimer.start();
    this.playbackPauseButton.setAction(getActionMap().get(ActionType.PAUSE));
  }

  /**
   * Pauses play back. 
   */
  private void pausePlayback() {
    this.playbackTimer.stop();
    this.playbackPauseButton.setAction(getActionMap().get(ActionType.PLAYBACK));
    getActionMap().get(ActionType.RECORD).setEnabled(this.videoCreationExecutor == null);
    boolean emptyCameraPath = this.controller.getCameraPath().isEmpty();
    getActionMap().get(ActionType.DELETE_CAMERA_PATH).setEnabled(this.videoCreationExecutor == null && !emptyCameraPath);
    getActionMap().get(ActionType.DELETE_LAST_RECORD).setEnabled(this.videoCreationExecutor == null && !emptyCameraPath);
  }

  /**
   * Moves quickly camera 10 steps backward. 
   */
  private void seekBackward() {
    for (int i = 0; i < 10 && this.cameraPathIterator.hasPrevious(); i++) {
      this.playbackTimer.getActionListeners() [0].actionPerformed(
          new ActionEvent(this.playbackTimer, 0, "backward", System.currentTimeMillis(), 0));
    }
  }
  
  /**
   * Moves quickly camera 10 steps forward. 
   */
  private void seekForward() {
    for (int i = 0; i < 10 && this.cameraPathIterator.hasNext(); i++) {
      this.playbackTimer.getActionListeners() [0].actionPerformed(
          new ActionEvent(this.playbackTimer, 0, "forward", System.currentTimeMillis(), 0));
    }
  }

  /**
   * Moves camera to animation start and restarts animation if it was running. 
   */
  private void skipBackward() {
    while (this.cameraPathIterator.hasPrevious()) {
      seekBackward();
    }
  }
  
  /**
   * Moves camera to animation end and stops animation. 
   */
  private void skipForward() {
    while (this.cameraPathIterator.hasNext()) {
      seekForward();
    }
  }

  /**
   * Returns the camera path that should be used to create each frame of an animation. 
   */
  private Camera [] getVideoFramesPath(int frameRate) {
    List<Camera> videoFramesPath = new ArrayList<Camera>();
    final float moveDistancePerFrame = 240000f / 3600 / frameRate;  // 3 cm/frame = 1800 m / 3600 s / 25 frame/s = 2.4 km/h
    final float moveAnglePerFrame = (float)(Math.PI / 180 * 30 / frameRate);
    final float elapsedTimePerFrame = 345600 / frameRate * 25; // 250 frame/day at 25 frame/second
    
    List<Camera> cameraPath = this.controller.getCameraPath();
    Camera camera = cameraPath.get(0);
    float x = camera.getX(); 
    float y = camera.getY(); 
    float z = camera.getZ();
    float yaw = camera.getYaw(); 
    float pitch = camera.getPitch(); 
    float fieldOfView = camera.getFieldOfView();
    long  time = camera.getTime();
    videoFramesPath.add(camera.clone());
    
    for (int i = 1; i < cameraPath.size(); i++) {
      camera = cameraPath.get(i);                  
      float newX = camera.getX(); 
      float newY = camera.getY(); 
      float newZ = camera.getZ();
      float newYaw = camera.getYaw(); 
      float newPitch = camera.getPitch(); 
      float newFieldOfView = camera.getFieldOfView();
      long  newTime = camera.getTime();
      
      float distance = new Point3f(x, y, z).distance(new Point3f(newX, newY, newZ));
      float moveCount = distance / moveDistancePerFrame;
      float yawAngleCount = Math.abs(newYaw - yaw) / moveAnglePerFrame;
      float pitchAngleCount = Math.abs(newPitch - pitch) / moveAnglePerFrame;
      float fieldOfViewAngleCount = Math.abs(newFieldOfView - fieldOfView) / moveAnglePerFrame;
      float timeCount = Math.abs(newTime - time) / elapsedTimePerFrame;

      int frameCount = (int)Math.max(moveCount, Math.max(yawAngleCount, 
          Math.max(pitchAngleCount, Math.max(fieldOfViewAngleCount, timeCount))));
      
      float deltaX = (newX - x) / frameCount;
      float deltaY = (newY - y) / frameCount;
      float deltaZ = (newZ - z) / frameCount;
      float deltaYawAngle = (newYaw - yaw) / frameCount;
      float deltaPitchAngle = (newPitch - pitch) / frameCount;
      float deltaFieldOfViewAngle = (newFieldOfView - fieldOfView) / frameCount;
      long deltaTime = Math.round(((double)newTime - time) / frameCount);
      
      for (int j = 1; j <= frameCount; j++) {
        videoFramesPath.add(new Camera(
            x + deltaX * j, y + deltaY * j, z + deltaZ * j, 
            yaw + deltaYawAngle * j, pitch + deltaPitchAngle * j, 
            fieldOfView + deltaFieldOfViewAngle * j,
            time + deltaTime * j,
            Camera.Lens.PINHOLE));
      }
      
      x = newX;
      y = newY;
      z = newZ;
      yaw = newYaw;
      pitch = newPitch;
      fieldOfView = newFieldOfView;
      time = newTime;
    }

    return videoFramesPath.toArray(new Camera [videoFramesPath.size()]);
  }

  /**
   * Creates the video image depending on the quality requested by the user.
   */
  private void startVideoCreation() {
    ActionMap actionMap = getActionMap();
    actionMap.get(ActionType.SAVE_VIDEO).setEnabled(false);
    this.createButton.setAction(getActionMap().get(ActionType.STOP_VIDEO_CREATION));
    actionMap.get(ActionType.RECORD).setEnabled(false);
    actionMap.get(ActionType.DELETE_CAMERA_PATH).setEnabled(false);
    actionMap.get(ActionType.DELETE_LAST_RECORD).setEnabled(false);
    getRootPane().setDefaultButton(this.createButton);
    this.videoFormatComboBox.setEnabled(false);
    this.qualitySlider.setEnabled(false);
    this.dateSpinner.setEnabled(false);
    this.timeSpinner.setEnabled(false);
    this.ceilingLightEnabledCheckBox.setEnabled(false);
    this.statusLayout.show(this.statusPanel, PROGRESS_CARD);
    this.progressBar.setIndeterminate(true);
    this.progressLabel.setText("");

    // Compute video in an other executor thread
    // Use a clone of home because the user can modify home during video computation
    final Home home = this.home.clone();
    this.videoCreationExecutor = Executors.newSingleThreadExecutor();
    this.videoCreationExecutor.execute(new Runnable() {
        public void run() {
          computeVideo(home);
        }
      });
  }

  /**
   * Computes the video of the given home.
   * Caution : this method must be thread safe because it's called from an executor. 
   */
  private void computeVideo(Home home) {
    this.videoCreationStartTime = System.currentTimeMillis();
    int frameRate = this.controller.getFrameRate();
    int quality = this.controller.getQuality();
    int width = this.controller.getWidth();
    int height = this.controller.getHeight();
    final Camera [] videoFramesPath = getVideoFramesPath(frameRate);
    // Set initial camera location because its type may change rendering setting
    home.setCamera(videoFramesPath [0]);
    final BoundedRangeModel progressModel = this.progressBar.getModel();
    EventQueue.invokeLater(new Runnable() {
        public void run() {
          progressModel.setMinimum(0);
          progressModel.setMaximum(videoFramesPath.length);
          progressModel.setValue(0);
        }
      });
    FrameGenerator frameGenerator = null;
    // Delete previous file if it exists 
    if (this.videoFile != null) {
      this.videoFile.delete();
      this.videoFile = null;
    }
    File file = null;
    try {
      file = OperatingSystem.createTemporaryFile("video", ".mov"); 
      if (quality >= 2) {
        frameGenerator = new PhotoImageGenerator(home, width, height, quality == 2 
              ? PhotoRenderer.Quality.LOW
              : PhotoRenderer.Quality.HIGH);        
      } else {
        frameGenerator = new Image3DGenerator(home, width, height, quality == 1); 
      }
      if (!Thread.currentThread().isInterrupted()) {
        ImageDataSource sourceStream = new ImageDataSource((VideoFormat)this.videoFormatComboBox.getSelectedItem(), 
            frameGenerator, videoFramesPath, progressModel);
        new JPEGImagesToVideo().createVideoFile(width, height, frameRate, sourceStream, file);
      }
    } catch (InterruptedIOException ex) {
      if (file != null) {
        file.delete();
        file = null;
      }
    } catch (IOException ex) {
      showError("createVideoError.message", ex.getMessage());
      file = null;
    } catch (OutOfMemoryError ex) {
      showError("createVideoError.message", 
          preferences.getLocalizedString(VideoPanel.class, "outOfMemory.message"));
      file = null;
    } finally {
      if (videoCreationExecutor != null) {
        this.videoFile = file;
        file.deleteOnExit();
      } else {
        this.videoFile = file;
        if (file != null) {
          file.delete();
        }
      }
      EventQueue.invokeLater(new Runnable() {
          public void run() {
            ActionMap actionMap = getActionMap();
            actionMap.get(ActionType.SAVE_VIDEO).setEnabled(videoFile != null);
            createButton.setAction(actionMap.get(ActionType.START_VIDEO_CREATION));
            actionMap.get(ActionType.RECORD).setEnabled(true);
            actionMap.get(ActionType.DELETE_CAMERA_PATH).setEnabled(true);
            actionMap.get(ActionType.DELETE_LAST_RECORD).setEnabled(true);
            if (videoFile != null) {
              getRootPane().setDefaultButton(saveButton);
            }
            videoFormatComboBox.setEnabled(true);
            qualitySlider.setEnabled(true);
            dateSpinner.setEnabled(true);
            timeSpinner.setEnabled(true);
            ceilingLightEnabledCheckBox.setEnabled(true);
            statusLayout.show(statusPanel, TIP_CARD);
            videoCreationExecutor = null;
          }
        });
    }
  }

  /**
   * Shows a message error dialog. 
   */
  private void showError(final String messageKey, 
                         final String messageDetail) {
    EventQueue.invokeLater(new Runnable() {
        public void run() {
          String messageFormat = preferences.getLocalizedString(VideoPanel.class, messageKey);
          JOptionPane.showMessageDialog(SwingUtilities.getRootPane(VideoPanel.this), String.format(messageFormat, messageDetail), 
              preferences.getLocalizedString(VideoPanel.class, "videoError.title"), JOptionPane.ERROR_MESSAGE);
        }
      });
  }
  
  /**
   * Stops video creation.
   */
  private void stopVideoCreation(boolean confirmStop) {
    if (this.videoCreationExecutor != null
        // Confirm the stop if a rendering has been running for more than 30 s 
        && (!confirmStop
            || System.currentTimeMillis() - this.videoCreationStartTime < MINIMUM_DELAY_BEFORE_DISCARDING_WITHOUT_WARNING
            || JOptionPane.showConfirmDialog(getRootPane(), 
                  this.preferences.getLocalizedString(VideoPanel.class, "confirmStopCreation.message"),
                  this.preferences.getLocalizedString(VideoPanel.class, "confirmStopCreation.title"), 
                  JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION)) {
      if (this.videoCreationExecutor != null) { // Check a second time in case rendering stopped meanwhile
        // Interrupt executor thread
        this.videoCreationExecutor.shutdownNow();
        this.videoCreationExecutor = null;
        this.createButton.setAction(getActionMap().get(ActionType.START_VIDEO_CREATION));
      }
    }
  }

  /**
   * Saves the created video.
   */
  private void saveVideo() {
    final String movFileName = this.controller.getContentManager().showSaveDialog(this,
        this.preferences.getLocalizedString(VideoPanel.class, "saveVideoDialog.title"), 
        ContentManager.ContentType.MOV, this.home.getName());
    if (movFileName != null) {
      final Component rootPane = SwingUtilities.getRoot(this);
      final Cursor defaultCursor = rootPane.getCursor();
      rootPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      
      // Disable panel actions
      ActionMap actionMap = getActionMap();
      final boolean [] actionEnabledStates = new boolean [ActionType.values().length];
      for (ActionType action : ActionType.values()) {
        actionEnabledStates [action.ordinal()] = actionMap.get(action).isEnabled();        
        actionMap.get(action).setEnabled(false);        
      }
      Executors.newSingleThreadExecutor().execute(new Runnable() {
          public void run() {
            OutputStream out = null;
            InputStream in = null;
            IOException exception = null;
            try {
              // Copy temporary file to home file
              // Overwriting home file will ensure that its rights are kept
              out = new FileOutputStream(movFileName);
              byte [] buffer = new byte [8192];
              in = new FileInputStream(videoFile);          
              int size; 
              while ((size = in.read(buffer)) != -1 && isDisplayable()) {
                out.write(buffer, 0, size);
              }
            } catch (IOException ex) {
              exception = ex;
            } finally {
              try {
                if (out != null) {          
                  out.close();
                }
              } catch (IOException ex) {
                if (exception == null) {
                  exception = ex;
                }
              }
              try {
                if (in != null) {          
                  in.close();
                }
              } catch (IOException ex) {
                // Ignore close exception
              }
              // Delete saved file in case of error or if panel was closed meanwhile
              if (exception != null || !isDisplayable()) {
                new File(movFileName).delete();
                if (!isDisplayable()) {
                  exception = null;
                }
              }
              
              final IOException caughtException = exception;
              EventQueue.invokeLater(new Runnable() {
                  public void run() {
                    // Restore action state 
                    ActionMap actionMap = getActionMap();
                    for (ActionType action : ActionType.values()) {
                      actionMap.get(action).setEnabled(actionEnabledStates [action.ordinal()]);        
                    }
                    
                    rootPane.setCursor(defaultCursor);
                    if (caughtException != null) {
                      showError("saveVideoError.message", caughtException.getMessage());
                    }
                  }
                });
            }
          }
        });
    }
  }

  /**
   * Manages closing of this pane.
   */
  private void close() {
    Window window = SwingUtilities.getWindowAncestor(this);
    if (window.isDisplayable()) {
      ToolTipManager.sharedInstance().unregisterComponent(this.qualitySlider);
      window.dispose();
    }    
  }
  
  /**
   * Returns the aspect ration of the given video format.
   */
  private AspectRatio getAspectRatio(VideoFormat videoFormat) {
    Dimension videoSize = videoFormat.getSize();
    return Math.abs((float)videoSize.width / videoSize.height - 4f / 3) < 0.001f
       ? AspectRatio.RATIO_4_3 
       : AspectRatio.RATIO_16_9;
  }

  /**
   * A data source able to create JPEG buffers on the fly from camera path 
   * and turn that into a stream of JMF buffers. 
   */
  private static class ImageDataSource extends PullBufferDataSource {
    private ImageSourceStream stream;

    public ImageDataSource(VideoFormat format,
                           FrameGenerator frameGenerator,
                           Camera []      framesPath,
                           BoundedRangeModel progressModel) {
      this.stream = new ImageSourceStream(format, frameGenerator, framesPath, progressModel);
    }

    @Override
    public void setLocator(MediaLocator source) {
    }

    @Override
    public MediaLocator getLocator() {
      return null;
    }

    /**
     * Returns RAW since buffers of video frames are sent without a container format.
     */
    @Override
    public String getContentType() {
      return ContentDescriptor.RAW;
    }

    @Override
    public void connect() {
    }

    @Override
    public void disconnect() {
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    public PullBufferStream [] getStreams() {
      return new PullBufferStream [] {this.stream};
    }

    /**
     * Not necessary to compute the duration.
     */
    @Override
    public Time getDuration() {
      return DURATION_UNKNOWN;
    }

    @Override
    public Object [] getControls() {
      return new Object [0];
    }

    @Override
    public Object getControl(String type) {
      return null;
    }
  }

  /**
   * A source of video images. 
   */
  private static class ImageSourceStream implements PullBufferStream {
    private final FrameGenerator                 frameGenerator;
    private final Camera []                      framesPath;
    private final BoundedRangeModel              progressModel;
    private final javax.media.format.VideoFormat format;
    private int                                  imageIndex;
    private boolean                              stopped;

    public ImageSourceStream(VideoFormat format, 
                             FrameGenerator frameGenerator,
                             Camera [] framesPath, 
                             final BoundedRangeModel progressModel) {
      this.frameGenerator = frameGenerator;
      this.framesPath = framesPath;
      this.progressModel = progressModel;
      this.format = format;      
    }

    /**
     * Return <code>false</code> because source stream doesn't 
     * need to block assuming data can be created on demand.
     */
    public boolean willReadBlock() {
      return false;
    }

    /**
     * This is called from the Processor to read a frame worth of video data.
     */
    public void read(Buffer buffer) throws IOException {
      buffer.setOffset(0);
      // Check if we've finished all the frames
      if (endOfStream()) {
        buffer.setEOM(true);
        buffer.setLength(0);
      } else {  
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BufferedImage frame = this.frameGenerator.renderImageAt(this.framesPath [this.imageIndex],
            this.imageIndex == this.framesPath.length - 1);           
        ImageIO.write(frame, "JPEG", outputStream);
        byte [] data = outputStream.toByteArray();
        buffer.setData(data);
        buffer.setLength(data.length);
        buffer.setFormat(this.format);
        buffer.setFlags(buffer.getFlags() | Buffer.FLAG_KEY_FRAME);
        
        final int progressionValue = this.imageIndex++;
        EventQueue.invokeLater(new Runnable() {
            public void run() {
              progressModel.setValue(progressionValue);
            }
          });
      }
    }

    /**
     * Return the format of each video frame. That will be JPEG.
     */
    public Format getFormat() {
      return this.format;
    }

    public ContentDescriptor getContentDescriptor() {
      return new ContentDescriptor(ContentDescriptor.RAW);
    }

    public long getContentLength() {
      return 0;
    }

    public boolean endOfStream() {
      return this.stopped || this.imageIndex == this.framesPath.length;
    }

    public Object [] getControls() {
      return new Object [0];
    }

    public Object getControl(String type) {
      return null;
    }
  }

  
  /**
   * An object able to generate a frame of a video at a camera location. 
   */
  private static abstract class FrameGenerator {
    private Thread launchingThread;

    protected FrameGenerator() {
      this.launchingThread = Thread.currentThread();
    }
    
    public abstract BufferedImage renderImageAt(Camera frameCamera, boolean last) throws IOException;
    
    protected void checkLaunchingThreadIsntInterrupted() throws InterruptedIOException {
      if (this.launchingThread.isInterrupted()) {
        throw new InterruptedIOException("Lauching thread interrupted");
      }
    }
  }

  /**
   * A frame generator using photo renderer.
   */
  private static class PhotoImageGenerator extends FrameGenerator {
    private PhotoRenderer renderer;
    private BufferedImage image;
    
    public PhotoImageGenerator(Home home, int width, int height, 
                               PhotoRenderer.Quality quality) throws IOException {
      this.renderer = new PhotoRenderer(home, quality); 
      this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    public BufferedImage renderImageAt(Camera frameCamera, boolean last) throws IOException {
      try {
        checkLaunchingThreadIsntInterrupted();
        this.renderer.render(this.image, frameCamera, null);   
        checkLaunchingThreadIsntInterrupted();
        return image;
      } catch(InterruptedIOException ex) {
        this.renderer = null;
        throw ex;
      } finally {
        if (last) {
          this.renderer = null;
        }
      }
    }
  }

  /**
   * A frame generator using 3D offscreen images.
   */
  private static class Image3DGenerator extends FrameGenerator {
    private final Home      home;
    private HomeComponent3D homeComponent3D;
    private BufferedImage   image;

    public Image3DGenerator(Home home, int width, int height, 
                            boolean displayShadowOnFloor) {
      this.home = home;
      this.homeComponent3D = new HomeComponent3D(home, null, displayShadowOnFloor);
      this.homeComponent3D.startOffscreenImagesCreation();
      this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }
    
    public BufferedImage renderImageAt(Camera frameCamera, boolean last) throws IOException {
      try {
        checkLaunchingThreadIsntInterrupted();
        // Replace home camera with frameCamera to avoid animation interpolator in 3D component 
        this.home.setCamera(frameCamera);
        // Get a twice bigger offscreen image for better quality 
        // (antialiasing isn't always available for offscreen canvas)
        BufferedImage offScreenImage = this.homeComponent3D.getOffScreenImage(
            2 * this.image.getWidth(), 2 * this.image.getHeight());

        checkLaunchingThreadIsntInterrupted();
        Graphics graphics = this.image.getGraphics();
        graphics.drawImage(offScreenImage.getScaledInstance(
            this.image.getWidth(), this.image.getHeight(), Image.SCALE_SMOOTH), 0, 0, null);
        graphics.dispose();
        checkLaunchingThreadIsntInterrupted();
        return this.image;
      } catch(InterruptedIOException ex) {
        this.homeComponent3D.endOffscreenImagesCreation();
        throw ex;
      } finally {
        if (last) {
          this.homeComponent3D.endOffscreenImagesCreation();
        }
      }
    }
  }
}
