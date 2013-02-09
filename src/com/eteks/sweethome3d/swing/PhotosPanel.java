/*
 * PhotosPanel.java 5 Nov 2012
 *
 * Sweet Home 3D, Copyright (c) 2012 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.ActionMap;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultListCellRenderer;
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
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.eteks.sweethome3d.j3d.PhotoRenderer;
import com.eteks.sweethome3d.model.Camera;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.ContentManager;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.PhotosController;
import com.eteks.sweethome3d.viewcontroller.View;

/**
 * A panel to edit photos created at home points of view. 
 * @author Emmanuel Puybaret
 */
public class PhotosPanel extends JPanel implements DialogView {
  private enum ActionType {START_PHOTOS_CREATION, STOP_PHOTOS_CREATION, CLOSE}

  private static final String PHOTOS_DIALOG_X_VISUAL_PROPERTY = "com.eteks.sweethome3d.swing.PhotosPanel.PhotoDialogX";
  private static final String PHOTOS_DIALOG_Y_VISUAL_PROPERTY = "com.eteks.sweethome3d.swing.PhotosPanel.PhotoDialogY";

  private static final String TIP_CARD      = "tip";
  private static final String PROGRESS_CARD = "progress";
  private static final String END_CARD      = "end";
  
  private final Home               home;
  private final UserPreferences    preferences;
  private final PhotosController   controller;
  private JLabel                   selectedCamerasLabel;
  private JList                    selectedCamerasList;
  private CardLayout               statusLayout;
  private JPanel                   statusPanel;
  private JLabel                   tipLabel;
  private JLabel                   progressLabel;
  private JProgressBar             progressBar;
  private JLabel                   endLabel;
  private ScaledImageComponent     photoComponent; 
  private PhotoSizeAndQualityPanel sizeAndQualityPanel;
  private JLabel                   fileFormatLabel;
  private JComboBox                fileFormatComboBox;
  private String                   dialogTitle;
  private ExecutorService          photosCreationExecutor;
  private JButton                  startStopButton;
  private JButton                  closeButton;

  private static PhotosPanel     currentPhotosPanel; // Support only one photos panel opened at a time

  public PhotosPanel(Home home, 
                     UserPreferences preferences, 
                     PhotosController controller) {
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
    actions.put(ActionType.START_PHOTOS_CREATION, 
        new ResourceAction(preferences, PhotosPanel.class, ActionType.START_PHOTOS_CREATION.name(), true) {
          @Override
          public void actionPerformed(ActionEvent ev) {
            startPhotosCreation();
          }
        });
    actions.put(ActionType.STOP_PHOTOS_CREATION, 
        new ResourceAction(preferences, PhotosPanel.class, ActionType.STOP_PHOTOS_CREATION.name(), true) {
          @Override
          public void actionPerformed(ActionEvent ev) {
            stopPhotosCreation();
          }
        });
    actions.put(ActionType.CLOSE, 
        new ResourceAction(preferences, PhotosPanel.class, ActionType.CLOSE.name(), true) {
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
                                final PhotosController controller) {
    // Create selected cameras label and list bound to SELECTED_CAMERAS controller property
    this.selectedCamerasLabel = new JLabel();
    this.selectedCamerasList = new JList(new CamerasListModel());
    this.selectedCamerasList.setCellRenderer(new ListCellRenderer() {
        private JCheckBox cameraCheckBox = new JCheckBox();
        
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
          this.cameraCheckBox.setText(((Camera)value).getName());
          this.cameraCheckBox.setSelected(controller.getSelectedCameras().contains(value));
          this.cameraCheckBox.setOpaque(true);
          if (isSelected && list.hasFocus()) {
            this.cameraCheckBox.setBackground(list.getSelectionBackground());
            this.cameraCheckBox.setForeground(list.getSelectionForeground());
          }
          else {
            this.cameraCheckBox.setBackground(list.getBackground());
            this.cameraCheckBox.setForeground(list.getForeground());
          }
          return this.cameraCheckBox;
        }
      });
    this.selectedCamerasList.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent ev) {
          if (selectedCamerasList.isEnabled()) {
            int index = selectedCamerasList.locationToIndex(ev.getPoint());
            if (index >= 0) {
              toggleCameraSelection((Camera)selectedCamerasList.getModel().getElementAt(index), controller);
            }
          }
        }
      });
    this.selectedCamerasList.getInputMap().put(KeyStroke.getKeyStroke("pressed SPACE"), "toggleSelection");
    this.selectedCamerasList.getActionMap().put("toggleSelection", new AbstractAction() {
        public void actionPerformed(ActionEvent ev) {
          if (selectedCamerasList.isEnabled()) {
            Camera camera = (Camera)selectedCamerasList.getSelectedValue();
            if (camera != null) {
              toggleCameraSelection(camera, controller);
            }
          }
        }
      });
    this.selectedCamerasList.setSelectedIndex(0);
    controller.addPropertyChangeListener(PhotosController.Property.SELECTED_CAMERAS, new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          selectedCamerasList.repaint();
          getActionMap().get(ActionType.START_PHOTOS_CREATION).setEnabled(!((List)ev.getNewValue()).isEmpty());
          statusLayout.show(statusPanel, TIP_CARD);
        }
      });
    controller.addPropertyChangeListener(PhotosController.Property.CAMERAS, new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          selectedCamerasList.repaint();
        }
      });

    // Create tip / progress / end components
    this.tipLabel = new JLabel();
    Font toolTipFont = UIManager.getFont("ToolTip.font");
    this.tipLabel.setFont(toolTipFont);
    
    this.progressLabel = new JLabel();
    this.progressLabel.setFont(toolTipFont);
    this.progressLabel.setHorizontalAlignment(JLabel.CENTER);
    
    this.progressBar = new JProgressBar();
    this.progressBar.setIndeterminate(true);
    this.progressBar.getModel().addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          int progressValue = progressBar.getValue();
          progressBar.setIndeterminate(progressValue < 0);
          if (progressValue >= 0) {
            progressLabel.setText(preferences.getLocalizedString(PhotosPanel.class, "progressLabel.format", 
                progressValue + 1, progressBar.getMaximum()));
          }          
        }
      });
    
    this.endLabel = new JLabel();
    this.endLabel.setFont(toolTipFont);
    this.endLabel.setHorizontalAlignment(JLabel.CENTER);

    this.photoComponent = new ScaledImageComponent();
    this.photoComponent.setPreferredSize(new Dimension(toolTipFont.getSize() * 5, toolTipFont.getSize() * 5));
    
    // Create size and quality panel
    this.sizeAndQualityPanel = new PhotoSizeAndQualityPanel(home, preferences, controller);

    // Create file format label and combo box bound to FILE_FORMAT / FILE_COMPRESSION_QUALITY controller properties
    this.fileFormatLabel = new JLabel();
    this.fileFormatComboBox = new JComboBox(new Object [] {"PNG", "JPEG 0.3", "JPEG 0.5", "JPEG 0.7", "JPEG 0.9"});
    this.fileFormatComboBox.setRenderer(new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, 
                                                      int index, boolean isSelected, boolean cellHasFocus) {
          String string = (String)value;
          String displayedValue = "";
          if ("PNG".equals(string)) {
            displayedValue = preferences.getLocalizedString(
                PhotosPanel.class, "fileFormatComboBox.png.text");
          } else if (((String)string).startsWith("JPEG")) {
            float compressionQuality = Float.parseFloat(string.substring(string.lastIndexOf(' ') + 1));
            displayedValue = preferences.getLocalizedString(
                PhotosPanel.class, "fileFormatComboBox.jpeg.text", Math.round(compressionQuality * 100));
          }
          return super.getListCellRendererComponent(list, displayedValue, index, isSelected,
              cellHasFocus);
        }
      });
    ItemListener fileFormatItemListener = new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          String value = (String)fileFormatComboBox.getSelectedItem();
          if (value.startsWith("JPEG")) {
            controller.setFileFormat("JPEG");
            controller.setFileCompressionQuality(new Float(value.substring(value.lastIndexOf(' ') + 1)));
          } else {
            controller.setFileFormat("PNG");
            controller.setFileCompressionQuality(null);
          }
        }
      };
    this.fileFormatComboBox.addItemListener(fileFormatItemListener);
    PropertyChangeListener fileFormatChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          fileFormatComboBox.setSelectedItem(controller.getFileFormat() 
              + (controller.getFileCompressionQuality() != null  ? " " + controller.getFileCompressionQuality()  : ""));
        }
      };
    controller.addPropertyChangeListener(PhotosController.Property.FILE_FORMAT, fileFormatChangeListener);
    controller.addPropertyChangeListener(PhotosController.Property.FILE_COMPRESSION_QUALITY, fileFormatChangeListener);
    if (controller.getFileFormat() != null) {
      fileFormatChangeListener.propertyChange(null);
    } else {
      fileFormatItemListener.itemStateChanged(null);
    }
    
    final JComponent view3D = (JComponent)controller.get3DView();
    controller.set3DViewAspectRatio((float)view3D.getWidth() / view3D.getHeight());

    final ActionMap actionMap = getActionMap();
    this.startStopButton = new JButton(actionMap.get(ActionType.START_PHOTOS_CREATION));
    this.closeButton = new JButton(actionMap.get(ActionType.CLOSE));

    setComponentTexts(preferences);
  }

  /**
   * Toggles the selected status of the given <code>camera</code>.
   */
  private void toggleCameraSelection(Camera camera, final PhotosController controller) {
    List<Camera> selectedCameras = new ArrayList<Camera>(controller.getSelectedCameras());
    if (selectedCameras.contains(camera)) {
      selectedCameras.remove(camera);
    } else {
      selectedCameras.add(camera);
    }
    controller.setSelectedCameras(selectedCameras);
  }

  /**
   * Sets the texts of the components.
   */
  private void setComponentTexts(UserPreferences preferences) {
    this.tipLabel.setText(preferences.getLocalizedString(PhotosPanel.class, "tipLabel.text"));
    this.endLabel.setText(preferences.getLocalizedString(PhotosPanel.class, "endLabel.text"));
    this.selectedCamerasLabel.setText(SwingTools.getLocalizedLabelText(preferences, 
        PhotosPanel.class, "selectedCamerasLabel.text"));
    this.fileFormatLabel.setText(SwingTools.getLocalizedLabelText(preferences, 
        PhotosPanel.class, "fileFormatLabel.text"));
    this.dialogTitle = preferences.getLocalizedString(PhotosPanel.class, "createPhoto.title");
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
      this.selectedCamerasLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          PhotosPanel.class, "selectedCamerasLabel.mnemonic")).getKeyCode());
      this.selectedCamerasLabel.setLabelFor(this.selectedCamerasList);
      this.fileFormatLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          PhotosPanel.class, "fileFormatLabel.mnemonic")).getKeyCode());
      this.fileFormatLabel.setLabelFor(this.fileFormatComboBox);
    }
  }

  /**
   * Preferences property listener bound to this panel with a weak reference to avoid
   * strong link between user preferences and this panel.  
   */
  public static class LanguageChangeListener implements PropertyChangeListener {
    private final WeakReference<PhotosPanel> photoPanel;

    public LanguageChangeListener(PhotosPanel photoPanel) {
      this.photoPanel = new WeakReference<PhotosPanel>(photoPanel);
    }

    public void propertyChange(PropertyChangeEvent ev) {
      // If photo panel was garbage collected, remove this listener from preferences
      PhotosPanel photoPanel = this.photoPanel.get();
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
    // Add tip and progress bar to a card panel 
    this.statusLayout = new CardLayout();
    this.statusPanel = new JPanel(this.statusLayout);
    this.statusPanel.add(this.tipLabel, TIP_CARD);
    this.tipLabel.setMinimumSize(this.tipLabel.getPreferredSize());
    JPanel progressPanel = new JPanel(new GridBagLayout());
    progressPanel.add(this.photoComponent, new GridBagConstraints(
        0, 0, 1, 2, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    progressPanel.add(this.progressBar, new GridBagConstraints(
        1, 0, 1, 1, 1, 1, GridBagConstraints.SOUTH, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 5, 5, 0), 0, 0));
    progressPanel.add(this.progressLabel, new GridBagConstraints(
        1, 1, 1, 1, 0, 1, GridBagConstraints.NORTH, 
        GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0));
    this.statusPanel.add(progressPanel, PROGRESS_CARD);
    this.statusPanel.add(this.endLabel, END_CARD);
    this.endLabel.setMinimumSize(this.endLabel.getPreferredSize());
    // First row
    add(this.selectedCamerasLabel, new GridBagConstraints(
        0, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 0), 0, 0));
    // Second row
    add(SwingTools.createScrollPane(this.selectedCamerasList), new GridBagConstraints(
        0, 1, 1, 1, 1, 1, GridBagConstraints.CENTER, 
        GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));
    // Third row
    add(this.statusPanel, new GridBagConstraints(
        0, 2, 1, 1, 1, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.NONE, new Insets(0, 0, 10, 0), 0, 0));
    // Fourth row
    add(this.sizeAndQualityPanel, new GridBagConstraints(
        0, 3, 1, 1, 0, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    // Last row
    JPanel fileFormatPanel = new JPanel();
    fileFormatPanel.add(this.fileFormatLabel);
    fileFormatPanel.add(this.fileFormatComboBox);
    this.fileFormatLabel.setHorizontalAlignment(labelAlignment);
    add(fileFormatPanel, new GridBagConstraints(
        0, 7, 4, 1, 0, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.NONE, new Insets(10, 0, 0, 0), 0, 0));
  }
  
  /**
   * Displays this panel in a non modal dialog.
   */
  public void displayView(View parentView) {
    if (currentPhotosPanel == this) {
      SwingUtilities.getWindowAncestor(PhotosPanel.this).toFront();
    } else {
      if (currentPhotosPanel != null) {
        currentPhotosPanel.close();
      }
      final JOptionPane optionPane = new JOptionPane(this, 
          JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION,
          null, new Object [] {this.startStopButton, this.closeButton}, this.startStopButton);
      if (parentView != null) {
        optionPane.setComponentOrientation(((JComponent)parentView).getComponentOrientation());
      }
      final JDialog dialog = optionPane.createDialog(SwingUtilities.getRootPane((Component)parentView), this.dialogTitle);
      dialog.setModal(false);
     
      Component homeRoot = SwingUtilities.getRoot((Component)parentView);
      Point dialogLocation = null;
      if (homeRoot != null) {
        // Restore location if it exists
        Integer x = (Integer)this.home.getVisualProperty(PHOTOS_DIALOG_X_VISUAL_PROPERTY);
        Integer y = (Integer)this.home.getVisualProperty(PHOTOS_DIALOG_Y_VISUAL_PROPERTY);      

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
          stopPhotosCreation();
          currentPhotosPanel = null;
        }
      });
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
            controller.setVisualProperty(PHOTOS_DIALOG_X_VISUAL_PROPERTY, dialog.getX());
            controller.setVisualProperty(PHOTOS_DIALOG_Y_VISUAL_PROPERTY, dialog.getY());
          }
        });
      dialog.setVisible(true);
      currentPhotosPanel = this;
    }
  }

  /**
   * Creates the photo images in a folder depending on the quality requested by the user.
   */
  private void startPhotosCreation() {
    final String directory = this.controller.getContentManager().showSaveDialog(this,
        this.preferences.getLocalizedString(PhotosPanel.class, "selectPhotosFolderDialog.title"), 
        ContentManager.ContentType.PHOTOS_DIRECTORY, this.home.getName());
    if (directory != null) {
      // Build file names list
      final Map<Camera, File> cameraFiles = new LinkedHashMap<Camera, File>();
      List<Camera> selectedCameras = this.controller.getSelectedCameras();
      ContentManager contentManager = this.controller.getContentManager();
      boolean overwriteConfirmed = false;
      for (Camera camera : this.controller.getCameras()) {
        if (selectedCameras.contains(camera)) {
          String fileName = "";
          if (this.home.getName() != null) {
            fileName += contentManager.getPresentationName(this.home.getName(), ContentManager.ContentType.SWEET_HOME_3D);
            if (contentManager instanceof FileContentManager) {
              fileName = fileName.substring(0, 
                  fileName.length()  
                  - ((FileContentManager)contentManager).getDefaultFileExtension(
                      ContentManager.ContentType.SWEET_HOME_3D).length());
            }
            fileName += " - ";
          }
          fileName += camera.getName();
          fileName = fileName.replaceAll("/|\\\\|:|;", "-").replace(File.pathSeparatorChar, '-').replace(File.separatorChar, '-');
          if (contentManager instanceof FileContentManager) {
            fileName += ((FileContentManager)contentManager).getDefaultFileExtension(
                ContentManager.ContentType.valueOf(controller.getFileFormat()));
          }
          File cameraFile = new File(directory, fileName);
          if (!overwriteConfirmed && cameraFile.exists()) {
            if (JOptionPane.showConfirmDialog(this, 
                  this.preferences.getLocalizedString(PhotosPanel.class, "confirmOverwrite.message", directory),
                  this.preferences.getLocalizedString(PhotosPanel.class, "confirmOverwrite.title"), JOptionPane.YES_NO_OPTION) 
                 == JOptionPane.NO_OPTION) {
              return;
            } else {
              overwriteConfirmed = true;
            }
          }
          cameraFiles.put(camera, cameraFile);
        }
      }     
      
      this.photoComponent.setImage(null);
      this.selectedCamerasList.setEnabled(false);
      this.sizeAndQualityPanel.setEnabled(false);
      this.fileFormatComboBox.setEnabled(false);
      getRootPane().setDefaultButton(this.startStopButton);
      this.startStopButton.setAction(getActionMap().get(ActionType.STOP_PHOTOS_CREATION));
      this.statusLayout.show(this.statusPanel, PROGRESS_CARD);
      this.progressBar.setMinimum(-1);
      this.progressBar.setValue(-1);
      this.progressLabel.setText("");
      this.photoComponent.setImage(null);
      
      // Compute photos in an other executor thread
      // Use a clone of home because the user can modify home during photos computation
      final Home home = this.home.clone();
      List<Selectable> emptySelection = Collections.emptyList();
      home.setSelectedItems(emptySelection);
      this.photosCreationExecutor = Executors.newSingleThreadExecutor();
      this.photosCreationExecutor.execute(new Runnable() {
          public void run() {
            computePhotos(home, cameraFiles);
          }
        });
    }
  }

  /**
   * Computes the photo of the given home.
   * Caution : this method must be thread safe because it's called from an executor. 
   */
  private void computePhotos(Home home, final Map<Camera, File> cameraFiles) {
    BufferedImage image = null;
    boolean success = false;
    try {
      int photoIndex = 0;
      for (Map.Entry<Camera, File> cameraEntry : cameraFiles.entrySet()) {
        int quality = this.controller.getQuality();
        int imageWidth = this.controller.getWidth();
        int imageHeight = this.controller.getHeight();
        Camera camera = cameraEntry.getKey();
        home.setCamera(camera);
        if (quality >= 2) {
          // Use photo renderer
          PhotoRenderer photoRenderer = new PhotoRenderer(home, 
            quality == 2 
                ? PhotoRenderer.Quality.LOW 
                : PhotoRenderer.Quality.HIGH);
          int bestImageHeight;
          // Update ratio if lens is fisheye or spherical
          if (camera.getLens() == Camera.Lens.FISHEYE) {
            bestImageHeight = imageWidth;
          } else if (camera.getLens() == Camera.Lens.SPHERICAL) {
            bestImageHeight = imageWidth * 2;
          } else {           
            bestImageHeight = imageHeight;
          }
          if (this.photosCreationExecutor != null) {
            image = new BufferedImage(imageWidth, bestImageHeight, BufferedImage.TYPE_INT_RGB);
            this.photoComponent.setImage(image);
            updateProgressBar(photoIndex++, cameraFiles.size());
            photoRenderer.render(image, camera, this.photoComponent);
          }
        } else {
          // Compute 3D view offscreen image
          HomeComponent3D homeComponent3D = new HomeComponent3D(home, this.preferences, quality == 1);
          updateProgressBar(photoIndex++, cameraFiles.size());
          image = homeComponent3D.getOffScreenImage(imageWidth, imageHeight);
          this.photoComponent.setImage(image);
        }

        try {
          if (this.photosCreationExecutor != null) {
            savePhoto(image, cameraEntry.getValue());
          }
        } catch (final IOException ex) {
          showPhotoSaveError(ex);
          return;
        }

        if (this.photosCreationExecutor == null) {
          return;
        }
      }
      success = true;
    } catch (OutOfMemoryError ex) {
      showPhotosComputingError(ex);
    } catch (IllegalStateException ex) {
      showPhotosComputingError(ex);
    } catch (IOException ex) {
      showPhotosComputingError(ex);
    } finally { 
      final boolean succeeded = success;
      EventQueue.invokeLater(new Runnable() {
          public void run() {
            startStopButton.setAction(getActionMap().get(ActionType.START_PHOTOS_CREATION));
            selectedCamerasList.setEnabled(true);
            sizeAndQualityPanel.setEnabled(true);
            sizeAndQualityPanel.setProportionsChoiceEnabled(true);
            fileFormatComboBox.setEnabled(true);
            if (succeeded) {
              statusLayout.show(statusPanel, END_CARD);
            } else {
              statusLayout.show(statusPanel, TIP_CARD);
            }
            photosCreationExecutor = null;
          }
        });
    }
  }

  private void updateProgressBar(final int photoIndex, final int photoCount) {
    final BoundedRangeModel progressModel = this.progressBar.getModel();
    EventQueue.invokeLater(new Runnable() {
        public void run() {
          progressModel.setMinimum(0);
          progressModel.setMaximum(photoCount);
          progressModel.setValue(photoIndex);
        }
      });
  }
  
  /**
   * Displays an error message box for save errors.
   */
  private void showPhotoSaveError(final Throwable ex) {
    try {
      EventQueue.invokeAndWait(new Runnable() {
          public void run() {
            String messageFormat = preferences.getLocalizedString(PhotosPanel.class, "savePhotosError.message");
            JOptionPane.showMessageDialog(SwingUtilities.getRootPane(PhotosPanel.this), String.format(messageFormat, ex.getMessage()), 
                preferences.getLocalizedString(PhotosPanel.class, "savePhotosError.title"), JOptionPane.ERROR_MESSAGE);
          }
        });
    } catch (InterruptedException ex1) {
      ex1.printStackTrace();
    } catch (InvocationTargetException ex1) {
      throw new RuntimeException(ex1);
    }
  }

  /**
   * Displays an error message box.
   */
  public void showPhotosComputingError(Throwable exception) {
    EventQueue.invokeLater(new Runnable() {
        public void run() {
          String title = preferences.getLocalizedString(PhotosPanel.class, "error.title");
          String message = preferences.getLocalizedString(PhotosPanel.class, "error.message");
          JOptionPane.showMessageDialog(PhotosPanel.this, message, title, JOptionPane.ERROR_MESSAGE);
        }
      });
  }

  /**
   * Stops photos creation.
   */
  private void stopPhotosCreation() {
    if (this.photosCreationExecutor != null) { 
      // Will interrupt executor thread      
      this.photosCreationExecutor.shutdownNow();
      this.photosCreationExecutor = null;
      this.startStopButton.setAction(getActionMap().get(ActionType.START_PHOTOS_CREATION));
    }
  }

  /**
   * Saves the given <code>image</code>.
   */
  private void savePhoto(BufferedImage image, File file) throws IOException {
    String fileFormat = this.controller.getFileFormat();
    Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName(fileFormat);
    ImageWriter writer = (ImageWriter)iter.next();
    ImageWriteParam writeParam = writer.getDefaultWriteParam();
    if ("JPEG".equals(fileFormat)) {
      writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
      writeParam.setCompressionQuality(this.controller.getFileCompressionQuality());
    }
    FileImageOutputStream output = new FileImageOutputStream(file);
    writer.setOutput(output);
    writer.write(null, new IIOImage(image, null, null), writeParam);
    writer.dispose();
    output.close();
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
  
  /**
   * List model for cameras.
   */
  private final class CamerasListModel extends AbstractListModel {
    private List<Camera> cameras;
    
    public CamerasListModel() {
      this.cameras = controller.getCameras();
      controller.addPropertyChangeListener(PhotosController.Property.CAMERAS, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              cameras = controller.getCameras();
              fireContentsChanged(this, 0, cameras.size());
            }
          });
    }

    public int getSize() {
      return this.cameras.size();
    }

    public Object getElementAt(int index) {
      return this.cameras.get(index);
    }
  }
}
