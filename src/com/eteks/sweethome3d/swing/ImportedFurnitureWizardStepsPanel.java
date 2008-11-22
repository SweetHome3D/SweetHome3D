/*
 * ImportedFurnitureWizardStepsPanel.java 4 juil. 07
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

import java.awt.AWTException;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;
import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;
import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Group;
import javax.media.j3d.IllegalRenderingStateException;
import javax.media.j3d.Light;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.PhysicalBody;
import javax.media.j3d.PhysicalEnvironment;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MouseInputAdapter;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.FurnitureCategory;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.tools.TemporaryURLContent;
import com.eteks.sweethome3d.tools.URLContent;
import com.eteks.sweethome3d.viewcontroller.ContentManager;
import com.eteks.sweethome3d.viewcontroller.ImportedFurnitureWizardController;
import com.eteks.sweethome3d.viewcontroller.ImportedFurnitureWizardStepsView;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.ViewingPlatform;

/**
 * Wizard panel for furniture import. 
 * @author Emmanuel Puybaret
 */
public class ImportedFurnitureWizardStepsPanel extends JPanel 
                                               implements ImportedFurnitureWizardStepsView {
  private final ImportedFurnitureWizardController controller;
  private ResourceBundle                    resource;
  private CardLayout                        cardLayout;
  private JLabel                            modelChoiceOrChangeLabel;
  private JButton                           modelChoiceOrChangeButton;
  private JButton                           findModelsButton;
  private JLabel                            modelChoiceErrorLabel;
  private ModelPreviewComponent             modelPreviewComponent;
  private JLabel                            orientationLabel;
  private JButton                           turnLeftButton;
  private JButton                           turnRightButton;
  private JButton                           turnUpButton;
  private JButton                           turnDownButton;
  private RotationPreviewComponent          rotationPreviewComponent;
  private JLabel                            backFaceShownLabel;
  private JCheckBox                         backFaceShownCheckBox;
  private JLabel                            attributesLabel;
  private JLabel                            nameLabel;
  private JTextField                        nameTextField;
  private JCheckBox                         addToCatalogCheckBox;
  private JLabel                            categoryLabel;
  private JComboBox                         categoryComboBox;
  private JLabel                            widthLabel;
  private JSpinner                          widthSpinner;
  private JLabel                            depthLabel;
  private JSpinner                          depthSpinner;
  private JLabel                            heightLabel;
  private JSpinner                          heightSpinner;
  private JCheckBox                         keepProportionsCheckBox;
  private JLabel                            elevationLabel;
  private JSpinner                          elevationSpinner;
  private AttributesPreviewComponent        attributesPreviewComponent;
  private JCheckBox                         movableCheckBox;
  private JCheckBox                         doorOrWindowCheckBox;
  private JLabel                            colorLabel;
  private ColorButton                       colorButton;
  private JButton                           clearColorButton;
  private JLabel                            iconLabel;
  private IconPreviewComponent              iconPreviewComponent;
  private Cursor                            defaultCursor; 
  private Executor                          modelLoader;

  /**
   * Creates a view for furniture import. 
   */
  public ImportedFurnitureWizardStepsPanel(CatalogPieceOfFurniture piece,
                                           String modelName,
                                           boolean importHomePiece,
                                           UserPreferences preferences, 
                                           ContentManager contentManager,
                                           final ImportedFurnitureWizardController controller) {
    this.controller = controller;
    this.resource = ResourceBundle.getBundle(ImportedFurnitureWizardStepsPanel.class.getName());
    this.modelLoader = Executors.newSingleThreadExecutor();
    createComponents(importHomePiece, preferences, contentManager, controller);
    setMnemonics();
    layoutComponents();
    updateController(piece);
    if (modelName != null) {
      updateController(modelName, contentManager,  
          importHomePiece 
              ? null 
              : preferences.getFurnitureCatalog().getCategories().get(0), true);
    }

    controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.STEP, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent evt) {
            updateStep(controller);
          }
        });
  }

  /**
   * Creates components displayed by this panel.
   */
  private void createComponents(final boolean importHomePiece, 
                                final UserPreferences preferences,
                                final ContentManager contentManager,
                                final ImportedFurnitureWizardController controller) {
    // Get unit name matching current unit 
    String unitName = preferences.getLengthUnit().getName();

    // Model panel components
    this.modelChoiceOrChangeLabel = new JLabel(); 
    this.modelChoiceOrChangeButton = new JButton();
    final FurnitureCategory defaultModelCategory = 
        (importHomePiece || preferences.getFurnitureCatalog().getCategories().size() == 0) 
            ? null
            : preferences.getFurnitureCatalog().getCategories().get(0);
    this.modelChoiceOrChangeButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          String modelName = showModelChoiceDialog(contentManager);
          if (modelName != null) {
            updateController(modelName, contentManager, defaultModelCategory, false);
          }
        }
      });
    this.findModelsButton = new JButton(this.resource.getString("findModelsButton.text"));
    BasicService basicService = null;
    try { 
      // Lookup the javax.jnlp.BasicService object 
      basicService = (BasicService)ServiceManager.lookup("javax.jnlp.BasicService"); 
      // Ignore the basic service, if it doesn't support web browser
      if (!basicService.isWebBrowserSupported()) {
        basicService = null;
      }
    } catch (UnavailableServiceException ex) {
      // Too bad : service is unavailable
    }
    final BasicService service = basicService;
    this.findModelsButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          boolean documentShown = false;
          if (service != null) {
            try { 
              // Display Find models page in browser
              final URL findModelsUrl = new URL(resource.getString("findModelsButton.url"));
              documentShown = service.showDocument(findModelsUrl); 
            } catch (MalformedURLException ex) {
              // Document isn't shown
            }
          } 
          if (!documentShown) {
            // If the document wasn't shown, display a message 
            // with a copiable URL in a message box 
            JTextArea findModelsMessageTextArea = new JTextArea( 
                resource.getString("findModelsMessage.text"));
            findModelsMessageTextArea.setEditable(false);
            findModelsMessageTextArea.setOpaque(false);
            JOptionPane.showMessageDialog(ImportedFurnitureWizardStepsPanel.this, 
                findModelsMessageTextArea, 
                resource.getString("findModelsMessage.title"), 
                JOptionPane.INFORMATION_MESSAGE);
          }
        }
      });
    this.modelChoiceErrorLabel = new JLabel(this.resource.getString("modelChoiceErrolLabel.text"));
    // Make modelChoiceErrorLabel visible only if an error occurred during model content loading
    this.modelChoiceErrorLabel.setVisible(false);
    this.modelPreviewComponent = new ModelPreviewComponent();
    // Add a transfer handler to model preview component to let user drag and drop a file in component
    this.modelPreviewComponent.setTransferHandler(new TransferHandler() {
        @Override
        public boolean canImport(JComponent comp, DataFlavor [] flavors) {
          return Arrays.asList(flavors).contains(DataFlavor.javaFileListFlavor);
        }
        
        @Override
        public boolean importData(JComponent comp, Transferable transferedFiles) {
          boolean success = true;
          try {
            List<File> files = (List<File>)transferedFiles.getTransferData(DataFlavor.javaFileListFlavor);
            String modelName = files.get(0).getAbsolutePath();
            updateController(modelName, contentManager, defaultModelCategory, false);
          } catch (UnsupportedFlavorException ex) {
            success = false;
          } catch (IOException ex) {
            success = false;
          }
          if (!success) {
            JOptionPane.showMessageDialog(ImportedFurnitureWizardStepsPanel.this, 
                resource.getString("modelChoiceError"));
          }
          return success;
        }
      });
    this.modelPreviewComponent.setBorder(BorderFactory.createLoweredBevelBorder());
   
    // Orientation panel components
    this.orientationLabel = new JLabel(this.resource.getString("orientationLabel.text"));
    this.turnLeftButton = new JButton(new ResourceAction(this.resource, "TURN_LEFT", true) {
        @Override
        public void actionPerformed(ActionEvent ev) {
          Transform3D oldTransform = getModelRotationTransform();
          Transform3D leftRotation = new Transform3D();
          leftRotation.rotY(-Math.PI / 2);
          leftRotation.mul(oldTransform);
          updateModelRotation(leftRotation);
        }
      });
    this.turnRightButton = new JButton(new ResourceAction(this.resource, "TURN_RIGHT", true) {
        @Override
        public void actionPerformed(ActionEvent ev) {
          Transform3D oldTransform = getModelRotationTransform();
          Transform3D rightRotation = new Transform3D();
          rightRotation.rotY(Math.PI / 2);
          rightRotation.mul(oldTransform);
          updateModelRotation(rightRotation);
        }
      });
    this.turnUpButton = new JButton(new ResourceAction(this.resource, "TURN_UP", true) {
        @Override
        public void actionPerformed(ActionEvent ev) {
          Transform3D oldTransform = getModelRotationTransform();
          Transform3D upRotation = new Transform3D();
          upRotation.rotX(-Math.PI / 2);
          upRotation.mul(oldTransform);
          updateModelRotation(upRotation);
        }
      });
    this.turnDownButton = new JButton(new ResourceAction(this.resource, "TURN_DOWN", true) {
        @Override
        public void actionPerformed(ActionEvent ev) {
          Transform3D oldTransform = getModelRotationTransform();
          Transform3D downRotation = new Transform3D();
          downRotation.rotX(Math.PI / 2);
          downRotation.mul(oldTransform);
          updateModelRotation(downRotation);
        }
      });
    this.backFaceShownLabel = new JLabel(this.resource.getString("backFaceShownLabel.text"));
    this.backFaceShownCheckBox = new JCheckBox(this.resource.getString("backFaceShownCheckBox.text"));
    this.backFaceShownCheckBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          controller.setBackFaceShown(backFaceShownCheckBox.isSelected());
        }
      });
    controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.BACK_FACE_SHWON,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            // If back face shown changes update back face shown check box
            backFaceShownCheckBox.setSelected(controller.isBackFaceShown());
          }
        });
    this.rotationPreviewComponent = new RotationPreviewComponent(controller);
    
    // Attributes panel components
    this.attributesLabel = new JLabel(this.resource.getString("attributesLabel.text"));
    this.nameLabel = new JLabel(this.resource.getString("nameLabel.text"));
    this.nameTextField = new JTextField(10);
    if (!OperatingSystem.isMacOSX()) {
      SwingTools.addAutoSelectionOnFocusGain(this.nameTextField);
    }
    final Color defaultNameTextFieldColor = this.nameTextField.getForeground();
    DocumentListener nameListener = new DocumentListener() {
        public void changedUpdate(DocumentEvent ev) {
          nameTextField.getDocument().removeDocumentListener(this);
          controller.setName(nameTextField.getText().trim());
          nameTextField.getDocument().addDocumentListener(this);
        }
  
        public void insertUpdate(DocumentEvent ev) {
          changedUpdate(ev);
        }
  
        public void removeUpdate(DocumentEvent ev) {
          changedUpdate(ev);
        }
      };
    this.nameTextField.getDocument().addDocumentListener(nameListener);
    controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.NAME,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            // If name changes update name text field
            if (!nameTextField.getText().trim().equals(controller.getName())) {
              nameTextField.setText(controller.getName());
            }
            updateNameTextFieldForeground(defaultNameTextFieldColor);
          }
        });

    this.addToCatalogCheckBox = new JCheckBox(this.resource.getString("addToCatalogCheckBox.text"));
    // Propose the add to catalog option only for home furniture import
    this.addToCatalogCheckBox.setVisible(importHomePiece);
    this.addToCatalogCheckBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          if (addToCatalogCheckBox.isSelected()) {
            categoryComboBox.setEnabled(true);
            controller.setCategory((FurnitureCategory)categoryComboBox.getSelectedItem());
          } else {
            categoryComboBox.setEnabled(false);
            controller.setCategory(null);
          }
        }
      });
    this.categoryLabel = new JLabel(this.resource.getString("categoryLabel.text")); 
    this.categoryComboBox = new JComboBox(preferences.getFurnitureCatalog().getCategories().toArray());
    // The piece category isn't enabled by default for home furniture import
    this.categoryComboBox.setEnabled(!importHomePiece);
    this.categoryComboBox.setEditable(true); 
    final ComboBoxEditor defaultEditor = this.categoryComboBox.getEditor();
    // Change editor to edit category name
    this.categoryComboBox.setEditor(new ComboBoxEditor() {
        public Object getItem() {
          String name = (String)defaultEditor.getItem();
          name = name.trim();
          // If category is empty, replace it by the last selected item
          if (name.length() == 0) {
            setItem(categoryComboBox.getSelectedItem());
          } 
          FurnitureCategory category = new FurnitureCategory(name);
          // Search an existing category
          List<FurnitureCategory> categories = preferences.getFurnitureCatalog().getCategories();
          int categoryIndex = Collections.binarySearch(categories, category);
          if (categoryIndex >= 0) {
            return categories.get(categoryIndex);
          }
          // If no existing category was found, return a new one          
          return category;
        }
      
        public void setItem(Object value) {
          if (value != null) {
            FurnitureCategory category = (FurnitureCategory)value;
            defaultEditor.setItem(category.getName());
          }
        }

        public void addActionListener(ActionListener l) {
          defaultEditor.addActionListener(l);
        }

        public Component getEditorComponent() {
          return defaultEditor.getEditorComponent();
        }

        public void removeActionListener(ActionListener l) {
          defaultEditor.removeActionListener(l);
        }

        public void selectAll() {
          defaultEditor.selectAll();
        }
      });
    this.categoryComboBox.setRenderer(new DefaultListCellRenderer() {
        public Component getListCellRendererComponent(JList list, Object value, int index, 
                                                      boolean isSelected, boolean cellHasFocus) {
          FurnitureCategory category = (FurnitureCategory)value;
          return super.getListCellRendererComponent(list, category.getName(), index, isSelected, cellHasFocus);
        }
      });
    this.categoryComboBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          controller.setCategory((FurnitureCategory)ev.getItem());
        }
      });
    controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.CATEGORY,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            // If category changes update category combo box
            FurnitureCategory category = controller.getCategory();
            if (category != null) {
              categoryComboBox.setSelectedItem(category);
            }
            updateNameTextFieldForeground(defaultNameTextFieldColor);
          }
        });
    if (this.categoryComboBox.getItemCount() > 0) {
      this.categoryComboBox.setSelectedIndex(0);
    }

    this.widthLabel = new JLabel(
        String.format(this.resource.getString("widthLabel.text"), unitName)); 
    final NullableSpinner.NullableSpinnerLengthModel widthSpinnerModel = 
        new NullableSpinner.NullableSpinnerLengthModel(preferences, 0.1f, 1000f);
    this.widthSpinner = new NullableSpinner(widthSpinnerModel);
    widthSpinnerModel.addChangeListener(new ChangeListener () {
        public void stateChanged(ChangeEvent ev) {
          widthSpinnerModel.removeChangeListener(this);
          // If width spinner value changes update controller
          controller.setWidth(widthSpinnerModel.getLength());
          widthSpinnerModel.addChangeListener(this);
        }
      });
    controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.WIDTH,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            // If width changes update width spinner
            widthSpinnerModel.setLength(controller.getWidth());
          }
        });
    
    this.depthLabel = new JLabel(
        String.format(this.resource.getString("depthLabel.text"), unitName)); 
    final NullableSpinner.NullableSpinnerLengthModel depthSpinnerModel = 
        new NullableSpinner.NullableSpinnerLengthModel(preferences, 0.1f, 1000f);
    this.depthSpinner = new NullableSpinner(depthSpinnerModel);
    depthSpinnerModel.addChangeListener(new ChangeListener () {
        public void stateChanged(ChangeEvent ev) {
          depthSpinnerModel.removeChangeListener(this);
          // If depth spinner value changes update controller
          controller.setDepth(depthSpinnerModel.getLength());
          depthSpinnerModel.addChangeListener(this);
        }
      });
    controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.DEPTH,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            // If depth changes update depth spinner
            depthSpinnerModel.setLength(controller.getDepth());
          }
        });
    
    this.heightLabel = new JLabel(
        String.format(this.resource.getString("heightLabel.text"), unitName)); 
    final NullableSpinner.NullableSpinnerLengthModel heightSpinnerModel = 
        new NullableSpinner.NullableSpinnerLengthModel(preferences, 0.1f, 1000f);
    this.heightSpinner = new NullableSpinner(heightSpinnerModel);
    heightSpinnerModel.addChangeListener(new ChangeListener () {
        public void stateChanged(ChangeEvent ev) {
          heightSpinnerModel.removeChangeListener(this);
          // If width spinner value changes update controller
          controller.setHeight(heightSpinnerModel.getLength());
          heightSpinnerModel.addChangeListener(this);
        }
      });
    controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.HEIGHT,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            // If height changes update height spinner
            heightSpinnerModel.setLength(controller.getHeight());
          }
        });
    this.keepProportionsCheckBox = new JCheckBox(
        this.resource.getString("keepProportionsCheckBox.text"));
    this.keepProportionsCheckBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          controller.setProportional(keepProportionsCheckBox.isSelected());
        }
      });
    controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.PROPORTIONAL,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            // If proportional property changes update keep proportions check box
            keepProportionsCheckBox.setSelected(controller.isProportional());
          }
        });
    
    this.elevationLabel = new JLabel(
        String.format(this.resource.getString("elevationLabel.text"), unitName)); 
    final NullableSpinner.NullableSpinnerLengthModel elevationSpinnerModel = 
        new NullableSpinner.NullableSpinnerLengthModel(preferences, 0f, 500f);
    this.elevationSpinner = new NullableSpinner(elevationSpinnerModel);
    elevationSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          widthSpinnerModel.removeChangeListener(this);
          controller.setElevation(elevationSpinnerModel.getLength());
          widthSpinnerModel.addChangeListener(this);
        }
      });
    controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.ELEVATION,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            // If elevation changes update elevation spinner
            elevationSpinnerModel.setLength(controller.getElevation());
          }
        });
    
    this.movableCheckBox = new JCheckBox(this.resource.getString("movableCheckBox.text"));
    this.movableCheckBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          controller.setMovable(movableCheckBox.isSelected());
        }
      });
    controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.MOVABLE,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            // If movable changes update movable check box
            movableCheckBox.setSelected(controller.isMovable());
          }
        });

    this.doorOrWindowCheckBox = new JCheckBox(this.resource.getString("doorOrWindowCheckBox.text"));
    this.doorOrWindowCheckBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          controller.setDoorOrWindow(doorOrWindowCheckBox.isSelected());
        }
      });
    controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.DOOR_OR_WINDOW,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            // If door or window changes update door or window check box
            doorOrWindowCheckBox.setSelected(controller.isDoorOrWindow());
          }
        });

    this.colorLabel = new JLabel(
        String.format(this.resource.getString("colorLabel.text"), unitName));
    this.colorButton = new ColorButton();
    this.colorButton.setColorDialogTitle(this.resource.getString("colorDialog.title"));
    this.colorButton.addPropertyChangeListener(ColorButton.COLOR_PROPERTY, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            controller.setColor(colorButton.getColor());
          }
        });
    this.clearColorButton = new JButton(this.resource.getString("clearColorButton.text"));
    this.clearColorButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          controller.setColor(null);
        }
      });
    this.clearColorButton.setEnabled(false);
    controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.COLOR, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            // If color changes update color buttons
            colorButton.setColor(controller.getColor());
            clearColorButton.setEnabled(controller.getColor() != null);
          }
        });
    
    this.attributesPreviewComponent = new AttributesPreviewComponent(controller);

    // Icon panel components
    this.iconLabel = new JLabel(this.resource.getString("iconLabel.text"));
    this.iconPreviewComponent = new IconPreviewComponent(this.controller);
  }

  /**
   * Sets components mnemonics and label / component associations.
   */
  private void setMnemonics() {
    if (!OperatingSystem.isMacOSX()) {
      this.findModelsButton.setMnemonic(
          KeyStroke.getKeyStroke(resource.getString("findModelsButton.mnemonic")).getKeyCode());
      this.backFaceShownCheckBox.setMnemonic(
          KeyStroke.getKeyStroke(resource.getString("backFaceShownCheckBox.mnemonic")).getKeyCode());
      this.nameLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("nameLabel.mnemonic")).getKeyCode());
      this.nameLabel.setLabelFor(this.nameTextField);
      this.categoryLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("categoryLabel.mnemonic")).getKeyCode());
      this.categoryLabel.setLabelFor(this.categoryComboBox);
      this.addToCatalogCheckBox.setMnemonic(
          KeyStroke.getKeyStroke(resource.getString("addToCatalogCheckBox.mnemonic")).getKeyCode());;
      this.widthLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("widthLabel.mnemonic")).getKeyCode());
      this.widthLabel.setLabelFor(this.widthSpinner);
      this.depthLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("depthLabel.mnemonic")).getKeyCode());
      this.depthLabel.setLabelFor(this.depthSpinner);
      this.heightLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("heightLabel.mnemonic")).getKeyCode());
      this.heightLabel.setLabelFor(this.heightSpinner);
      this.keepProportionsCheckBox.setMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("keepProportionsCheckBox.mnemonic")).getKeyCode());
      this.elevationLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("elevationLabel.mnemonic")).getKeyCode());
      this.elevationLabel.setLabelFor(this.elevationSpinner);
      this.movableCheckBox.setMnemonic(
          KeyStroke.getKeyStroke(resource.getString("movableCheckBox.mnemonic")).getKeyCode());;
      this.doorOrWindowCheckBox.setMnemonic(
          KeyStroke.getKeyStroke(resource.getString("doorOrWindowCheckBox.mnemonic")).getKeyCode());;
      this.colorLabel.setDisplayedMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("colorLabel.mnemonic")).getKeyCode());
      this.colorLabel.setLabelFor(this.colorButton);
      this.clearColorButton.setMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("clearColorButton.mnemonic")).getKeyCode());
    }
  }
  
  /**
   * Layouts components in 4 panels added to this panel as cards. 
   */
  private void layoutComponents() {
    this.cardLayout = new CardLayout();
    setLayout(this.cardLayout);
    
    JPanel modelPanel = new JPanel(new GridBagLayout());
    modelPanel.add(this.modelChoiceOrChangeLabel, new GridBagConstraints(
        0, 0, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(5, 0, 5, 0), 0, 0));
    modelPanel.add(this.modelChoiceOrChangeButton, new GridBagConstraints(
        0, 1, 1, 1, 1, 0, GridBagConstraints.LINE_END, 
        GridBagConstraints.NONE, new Insets(0, 0, 0, 10), 0, 0));
    modelPanel.add(this.findModelsButton, new GridBagConstraints(
        1, 1, 1, 1, 1, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    modelPanel.add(this.modelChoiceErrorLabel, new GridBagConstraints(
        0, 2, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(5, 0, 0, 0), 0, 0));
    modelPanel.add(this.modelPreviewComponent, new GridBagConstraints(
        0, 3, 2, 1, 0, 1, GridBagConstraints.CENTER, 
        GridBagConstraints.NONE, new Insets(5, 0, 5, 0), 0, 0));
    
    JPanel orientationPanel = new JPanel(new GridBagLayout());
    orientationPanel.add(this.orientationLabel, new GridBagConstraints(
        0, 0, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(5, 0, 5, 0), 0, 0));
    orientationPanel.add(this.rotationPreviewComponent, new GridBagConstraints(
        0, 1, 1, 1, 1, 1, GridBagConstraints.LINE_END, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 15), 0, 0));    
    JPanel rotationButtonsPanel = new JPanel(new GridBagLayout()) {
        @Override
        public void applyComponentOrientation(ComponentOrientation o) {
          // Ignore panel orientation to ensure left button is always at left of panel
        }
      };
    rotationButtonsPanel.add(this.turnUpButton, new GridBagConstraints(
        1, 0, 1, 1, 0, 0, GridBagConstraints.SOUTH, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 0), 0, 0));    
    rotationButtonsPanel.add(this.turnLeftButton, new GridBagConstraints(
        0, 1, 1, 1, 0, 0, GridBagConstraints.EAST, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
    rotationButtonsPanel.add(this.turnRightButton, new GridBagConstraints(
        2, 1, 1, 1, 1, 0, GridBagConstraints.WEST, 
        GridBagConstraints.NONE, new Insets(0, 5, 5, 0), 0, 0));
    rotationButtonsPanel.add(this.turnDownButton, new GridBagConstraints(
        1, 2, 1, 1, 0, 0, GridBagConstraints.NORTH, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 0), 0, 0));
    orientationPanel.add(rotationButtonsPanel, new GridBagConstraints(
        1, 1, 1, 1, 1, 1, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 0), 0, 0));    
    orientationPanel.add(this.backFaceShownLabel, new GridBagConstraints(
        0, 4, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(5, 0, 5, 0), 0, 0));
    orientationPanel.add(this.backFaceShownCheckBox, new GridBagConstraints(
        0, 5, 2, 1, 0, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 0), 0, 0));

    JPanel attributesPanel = new JPanel(new GridBagLayout());
    attributesPanel.add(this.attributesLabel, new GridBagConstraints(
        0, 0, 3, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(5, 0, 5, 0), 0, 0));
    attributesPanel.add(this.attributesPreviewComponent, new GridBagConstraints(
        0, 1, 1, 13, 1, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
    attributesPanel.add(this.nameLabel, new GridBagConstraints(
        1, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 0, 10, 5), 0, 0));
    attributesPanel.add(this.nameTextField, new GridBagConstraints(
        2, 1, 1, 1, 0, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 10, 0), 0, 0));
    attributesPanel.add(this.addToCatalogCheckBox, new GridBagConstraints(
        1, 2, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 0, 10, 0), 0, 0));
    attributesPanel.add(this.categoryLabel, new GridBagConstraints(
        1, 3, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 0, 10, 5), 0, 0));
    attributesPanel.add(this.categoryComboBox, new GridBagConstraints(
        2, 3, 1, 1, 0, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 10, 0), 0, 0));
    attributesPanel.add(this.widthLabel, new GridBagConstraints(
        1, 4, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 0, 10, 5), 0, 0));
    attributesPanel.add(this.widthSpinner, new GridBagConstraints(
        2, 4, 1, 1, 0, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 10, 0), 0, 0));
    attributesPanel.add(this.depthLabel, new GridBagConstraints(
        1, 5, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 0, 10, 5), 0, 0));
    attributesPanel.add(this.depthSpinner, new GridBagConstraints(
        2, 5, 1, 1, 0, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 10, 0), 0, 0));
    attributesPanel.add(this.heightLabel, new GridBagConstraints(
        1, 6, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 0, 10, 5), 0, 0));
    attributesPanel.add(this.heightSpinner, new GridBagConstraints(
        2, 6, 1, 1, 0, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 10, 0), 0, 0));
    attributesPanel.add(this.keepProportionsCheckBox, new GridBagConstraints(
        1, 7, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 0, 15, 0), 0, 0));
    attributesPanel.add(this.elevationLabel, new GridBagConstraints(
        1, 8, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 0, 10, 5), 0, 0));
    attributesPanel.add(this.elevationSpinner, new GridBagConstraints(
        2, 8, 1, 1, 0, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 10, 0), 0, 0));
    attributesPanel.add(this.movableCheckBox, new GridBagConstraints(
        1, 9, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 0, 10, 0), 0, 0));
    attributesPanel.add(this.doorOrWindowCheckBox, new GridBagConstraints(
        1, 10, 2, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 0, 10, 0), 0, 0));
    attributesPanel.add(this.colorLabel, new GridBagConstraints(
        1, 11, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
    attributesPanel.add(this.colorButton, new GridBagConstraints(
        2, 11, 1, 1, 0, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
    attributesPanel.add(this.clearColorButton, new GridBagConstraints(
        2, 12, 1, 1, 0, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
    // Add a dummy label to force components to be at top of panel
    attributesPanel.add(new JLabel(), new GridBagConstraints(
        1, 13, 1, 1, 1, 1, GridBagConstraints.CENTER, 
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

    JPanel iconPanel = new JPanel(new GridBagLayout());
    iconPanel.add(this.iconLabel, new GridBagConstraints(
        0, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.NONE, new Insets(5, 0, 5, 0), 0, 0));
    iconPanel.add(this.iconPreviewComponent, new GridBagConstraints(
        0, 1, 1, 1, 1, 1, GridBagConstraints.CENTER, 
        GridBagConstraints.NONE, new Insets(5, 0, 5, 0), 0, 0));
    
    add(modelPanel, ImportedFurnitureWizardController.Step.MODEL.name());
    add(orientationPanel, ImportedFurnitureWizardController.Step.ROTATION.name());
    add(attributesPanel, ImportedFurnitureWizardController.Step.ATTRIBUTES.name());
    add(iconPanel, ImportedFurnitureWizardController.Step.ICON.name());
  }
  
  /**
   * Switches to the component card matching current step.   
   */
  public void updateStep(ImportedFurnitureWizardController controller) {
    ImportedFurnitureWizardController.Step step = controller.getStep();
    this.cardLayout.show(this, step.name());
    switch (step) {
      case MODEL:
        this.modelChoiceOrChangeButton.requestFocusInWindow();
        break;
      case ATTRIBUTES:
        this.nameTextField.requestFocusInWindow();
        break;
    }
  }

  /**
   * Updates name text field foreground color depending on the validity
   * of the piece name.
   */
  private void updateNameTextFieldForeground(Color defaultNameTextFieldColor) {
    nameTextField.setForeground(this.controller.isPieceOfFurnitureNameValid() 
        ? defaultNameTextFieldColor
        : Color.RED);
  }
  
  /**
   * Returns the transformation matching current model rotation.
   */
  private Transform3D getModelRotationTransform() {
    float [][] modelRotation = this.controller.getModelRotation();
    Matrix3f modelRotationMatrix = new Matrix3f(modelRotation [0][0], modelRotation [0][1], modelRotation [0][2],
        modelRotation [1][0], modelRotation [1][1], modelRotation [1][2],
        modelRotation [2][0], modelRotation [2][1], modelRotation [2][2]);
    Transform3D transform = new Transform3D();
    transform.setRotation(modelRotationMatrix);
    return transform;
  }
  
  /**
   * Updates model rotation from the values of <code>transform</code>.
   */
  private void updateModelRotation(Transform3D transform) {
    Matrix3f modelRotationMatrix = new Matrix3f();
    transform.getRotationScale(modelRotationMatrix);
    this.controller.setModelRotation(new float [][] {{modelRotationMatrix.m00, modelRotationMatrix.m01, modelRotationMatrix.m02},
                                                     {modelRotationMatrix.m10, modelRotationMatrix.m11, modelRotationMatrix.m12},
                                                     {modelRotationMatrix.m20, modelRotationMatrix.m21, modelRotationMatrix.m22}});
  }
  
  /**
   * Updates controller initial values from <code>piece</code>. 
   */
  private void updateController(final CatalogPieceOfFurniture piece) {
    if (piece == null) {
      setModelChoiceTexts();
      updatePreviewComponentsModel(null);
    } else {
      setModelChangeTexts();
      // Read model in modelLoader executor
      this.modelLoader.execute(new Runnable() {
          public void run() {
            BranchGroup model = null;
            try {
              model = readModel(piece.getModel());
            } catch (IOException ex) {
              // Model loading failed
            }
            final BranchGroup readModel = model;
            // Update components in dispatch thread
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                  if (readModel != null) {
                    controller.setModel(piece.getModel());
                    controller.setModelRotation(piece.getModelRotation());
                    controller.setBackFaceShown(piece.isBackFaceShown());
                    controller.setName(piece.getName());
                    controller.setCategory(piece.getCategory());
                    controller.setWidth(piece.getWidth());
                    controller.setDepth(piece.getDepth());
                    controller.setHeight(piece.getHeight());
                    controller.setMovable(piece.isMovable());
                    controller.setDoorOrWindow(piece.isDoorOrWindow());
                    controller.setElevation(piece.getElevation());
                    controller.setColor(piece.getColor());
                    controller.setIconYaw(piece.getIconYaw());
                    controller.setProportional(piece.isProportional());
                  } else {
                    controller.setModel(null);
                    setModelChoiceTexts();
                    modelChoiceErrorLabel.setVisible(true);
                  }
                } 
              });
            
          } 
        });
    }
  }

  /**
   * Reads model from <code>modelName</code> and updates controller values.
   */
  private void updateController(final String modelName,
                                final ContentManager contentManager,
                                final FurnitureCategory defaultCategory,
                                final boolean ignoreException) {
    // Read model in modelLoader executor
    this.modelLoader.execute(new Runnable() {
        public void run() {
          Content modelContent = null;
          try {
            // Copy model content to a temporary content
            modelContent = TemporaryURLContent.copyToTemporaryURLContent(
                contentManager.getContent(modelName));
          } catch (RecorderException ex) {
            // Error message displayed below 
          } catch (IOException ex) {
            // Error message displayed below 
          }
          if (modelContent == null) {
            if (!ignoreException) {
              EventQueue.invokeLater(new Runnable() {
                  public void run() {
                    JOptionPane.showMessageDialog(ImportedFurnitureWizardStepsPanel.this, 
                        String.format(resource.getString("modelChoiceError"), modelName));
                  }
                });
            }
            return;
          }
          
          BranchGroup model = null;
          try {
            model = readModel(modelContent);
          } catch (IOException ex) {
            // If content couldn't be loaded, try to load model as a zipped file
            if (modelContent instanceof URLContent) {
              URLContent urlContent = (URLContent)modelContent;
              ZipInputStream zipIn = null;
              try {
                // Open zipped stream
                zipIn = new ZipInputStream(urlContent.openStream());
                // Parse entries to see if one is readable
                for (ZipEntry entry; (entry = zipIn.getNextEntry()) != null; ) {
                  try {
                    String entryName = entry.getName();
                    // Ignore directory entries and entries starting by a dot
                    if (!entryName.endsWith("/")) {
                      int slashIndex = entryName.lastIndexOf('/');
                      String entryFileName = entryName.substring(++slashIndex);
                      if (!entryFileName.startsWith(".")) {
                        URL entryUrl = new URL("jar:" + urlContent.getURL() + "!/" + entryName);
                        modelContent = modelContent instanceof TemporaryURLContent 
                            ? new TemporaryURLContent(entryUrl)
                            : new URLContent(entryUrl);
                        model = readModel(modelContent);
                        break;
                      }
                    }
                  } catch (IOException ex3) {
                    // Ignore exception and try next entry
                  }
                }
              } catch (IOException ex2) {
                model = null;
              } finally {
                try {
                  if (zipIn != null) {
                    zipIn.close();
                  }
                } catch (IOException ex2) {
                  // Ignore close exception
                }
              }
            }
          }
          
          final BranchGroup readModel = model;
          final Content     readContent = modelContent;
          // Update components in dispatch thread
          EventQueue.invokeLater(new Runnable() {
              public void run() {
                if (readModel != null) {
                  controller.setModel(readContent);
                  setModelChangeTexts();
                  modelChoiceErrorLabel.setVisible(false);
                  controller.setModelRotation(new float [][] {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}});
                  controller.setBackFaceShown(false);
                  controller.setName(contentManager.getPresentationName(
                      modelName, ContentManager.ContentType.MODEL));
                  controller.setCategory(defaultCategory);
                  // Initialize size with default values
                  Vector3f size = ModelManager.getInstance().getSize(readModel);
                  controller.setWidth(size.x);
                  controller.setDepth(size.z);
                  controller.setHeight(size.y);
                  controller.setMovable(true);
                  controller.setDoorOrWindow(false);
                  controller.setColor(null);                  
                  controller.setIconYaw((float)Math.PI / 8);
                  controller.setProportional(true);
                } else if (isShowing()) {
                  controller.setModel(null);
                  setModelChoiceTexts();
                  JOptionPane.showMessageDialog(ImportedFurnitureWizardStepsPanel.this, 
                      resource.getString("modelChoiceFormatError"));
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
  private BranchGroup readModel(Content modelContent) throws IOException {
    try {
      EventQueue.invokeLater(new Runnable() {
          public void run() {
            modelChoiceOrChangeButton.setEnabled(false);
            Component rootPane = SwingUtilities.getRoot(ImportedFurnitureWizardStepsPanel.this);
            if (defaultCursor == null) {
              defaultCursor = rootPane.getCursor();
            }
            rootPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            updatePreviewComponentsModel(null);
          }
        });
            
      // Load piece model 
      final BranchGroup modelNode = ModelManager.getInstance().getModel(modelContent);
      
      // Change live object in Event Dispatch Thread
      EventQueue.invokeLater(new Runnable() {
          public void run() {
            updatePreviewComponentsModel(modelNode);
          }
        });
      return modelNode;
    } finally {
      EventQueue.invokeLater(new Runnable() {
          public void run() {
            modelChoiceOrChangeButton.setEnabled(true);
            SwingUtilities.getRoot(ImportedFurnitureWizardStepsPanel.this).setCursor(defaultCursor);
          }
        });
    } 
  }
  
  /**
   * Updates the <code>image</code> displayed by preview components.  
   */
  private void updatePreviewComponentsModel(final BranchGroup model) {
    modelPreviewComponent.setModel(model);
    rotationPreviewComponent.setModel(model);
    attributesPreviewComponent.setModel(model);
    iconPreviewComponent.setModel(model);
  }

  /**
   * Sets the texts of label and button of model panel with change texts. 
   */
  private void setModelChangeTexts() {
    this.modelChoiceOrChangeLabel.setText(this.resource.getString("modelChangeLabel.text")); 
    this.modelChoiceOrChangeButton.setText(this.resource.getString("modelChangeButton.text"));
    if (!OperatingSystem.isMacOSX()) {
      this.modelChoiceOrChangeButton.setMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("modelChangeButton.mnemonic")).getKeyCode());
    }
  }

  /**
   * Sets the texts of label and button of model panel with choice texts. 
   */
  private void setModelChoiceTexts() {
    this.modelChoiceOrChangeLabel.setText(this.resource.getString("modelChoiceLabel.text")); 
    this.modelChoiceOrChangeButton.setText(this.resource.getString("modelChoiceButton.text"));
    if (!OperatingSystem.isMacOSX()) {
      this.modelChoiceOrChangeButton.setMnemonic(
          KeyStroke.getKeyStroke(this.resource.getString("modelChoiceButton.mnemonic")).getKeyCode());
    }
  }
  
  /**
   * Returns the model name chosen for a file chooser dialog.
   */
  private String showModelChoiceDialog(ContentManager contentManager) {
    return contentManager.showOpenDialog(this, 
        this.resource.getString("modelChoiceDialog.title"), 
        ContentManager.ContentType.MODEL);
  }
  
  /**
   * Returns the icon content of the chosen piece.
   * Icon is created once on demand of view's controller, because it demands either  
   * icon panel being displayed, or an offscreen 3D buffer that costs too much to create at each yaw change.
   */
  public Content getIcon() {
    try {
      File tempFile = File.createTempFile("urlContent", "tmp");
      tempFile.deleteOnExit();
      ImageIO.write(this.iconPreviewComponent.getIconImage(), "png", tempFile);
      return new TemporaryURLContent(tempFile.toURI().toURL());
    } catch (IOException ex) {
      try {
        return new URLContent(new URL("file:/dummySweetHome3DContent"));
      } catch (MalformedURLException ex1) {
        return null;
      }
    }
  }

  
  /**
   * Super class of 3D preview component for model. 
   */
  private static class ModelPreviewComponent extends JComponent {
    private SimpleUniverse     universe;
    private Canvas3D           canvas3D;
    private BranchGroup        sceneTree;
    private float              viewYaw = (float) Math.PI / 8;

    public ModelPreviewComponent() {
      this.canvas3D = Component3DManager.getInstance().getOnscreenCanvas3D();

      // Layout canvas3D
      setLayout(new GridLayout(1, 1));
      add(this.canvas3D);
      this.canvas3D.setFocusable(false);      
      addMouseListeners(this.canvas3D);
      
      setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

      this.sceneTree = createSceneTree();
      
      // Add an ancestor listener to create canvas universe once this component is made visible 
      // and clean up universe once its parent frame is disposed
      addAncestorListener();
    }

    @Override
    public Dimension getPreferredSize() {
      return new Dimension(200, 200);
    }

    /**
     * Returns the canvas 3D displayed by this component.
     */
    protected Canvas3D getCanvas3D() {
      return this.canvas3D;
    }
    
    /**
     * Adds an AWT mouse listener to canvas that will udate view platform transform.  
     */
    private void addMouseListeners(Canvas3D canvas3D) {
      MouseInputAdapter mouseListener = new MouseInputAdapter() {
          private int xLastMouseMove;
          
          @Override
          public void mousePressed(MouseEvent ev) {
            this.xLastMouseMove = ev.getX();
          }
    
          @Override
          public void mouseDragged(MouseEvent ev) {
            final float ANGLE_FACTOR = 0.02f;
            if (getModel() != null) {
              // Mouse move along X axis changes yaw 
              setViewYaw(getViewYaw() - ANGLE_FACTOR * (ev.getX() - this.xLastMouseMove));                            
              this.xLastMouseMove = ev.getX();
            }
          }
        };

      canvas3D.addMouseListener(mouseListener);
      canvas3D.addMouseMotionListener(mouseListener);
    }

    /**
     * Adds an ancestor listener to this component to manage canvas universe 
     * creation and clean up.  
     */
    private void addAncestorListener() {
      addAncestorListener(new AncestorListener() {
          public void ancestorAdded(AncestorEvent event) {
            if (universe == null) {
              createUniverse();
            }
          }
          
          public void ancestorRemoved(AncestorEvent event) {
            if (universe != null) {
              disposeUniverse();
            }
          }
          
          public void ancestorMoved(AncestorEvent event) {
          }        
        });
    }
    
    /**
     * Creates universe bound to canvas.
     */
    private void createUniverse() {
      // Link canvas 3D to a default universe
      this.universe = new SimpleUniverse(this.canvas3D);
      this.canvas3D.setFocusable(false);
      // Set viewer location 
      updateViewPlatformTransform(this.universe.getViewingPlatform().getViewPlatformTransform(), 
          getViewYaw(), getViewPitch());
      // Link scene to universe
      this.universe.addBranchGraph(this.sceneTree);

      if (OperatingSystem.isMacOSX()) {
        final Component root = SwingUtilities.getRoot(ModelPreviewComponent.this);
        EventQueue.invokeLater(new Runnable() {
            public void run() {
              // Force a real repaint of the component with a resize of its root, 
              // otherwise some canvas 3D may not be displayed
              Dimension rootSize = root.getSize();
              root.setSize(new Dimension(rootSize.width + 1, rootSize.height));
              try {
                Thread.sleep(100);
              } catch (InterruptedException ex) {
              }
              root.setSize(new Dimension(rootSize.width, rootSize.height));
              // Request focus again even if dialog isn't supposed to have lost focus !
              if (KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow() != root) {
                root.requestFocus();
              }
            } 
          });
      }
    }
    
    /**
     * Disposes universe bound to canvas.
     */
    private void disposeUniverse() {
      // Unlink scene to universe
      this.universe.getLocale().removeBranchGraph(this.sceneTree);
      this.universe.cleanup();
      this.universe = null;
    }
    
    /**
     * Creates a view bound to universe that views current model from a point of view oriented with 
     * <code>yaw</code> and <code>pitch</code> angles.
     */
    protected View createView(float yaw, float pitch, int projectionPolicy) {
      if (this.universe == null) {
        createUniverse();
      }
      // Reuse same physical body and environment
      PhysicalBody physicalBody = this.universe.getViewer().getPhysicalBody();
      PhysicalEnvironment physicalEnvironment = this.universe.getViewer().getPhysicalEnvironment();
      
      // Create a view associated with canvas3D
      View view = new View();
      view.setPhysicalBody(physicalBody);
      view.setPhysicalEnvironment(physicalEnvironment);
      view.setProjectionPolicy(projectionPolicy);
      // Create a viewing platform and attach it to view and universe locale
      ViewingPlatform viewingPlatform = new ViewingPlatform();
      viewingPlatform.setUniverse(this.universe);
      this.universe.getLocale().addBranchGraph(
          (BranchGroup)viewingPlatform.getViewPlatformTransform().getParent());
      view.attachViewPlatform(viewingPlatform.getViewPlatform());

      // Set user point of view depending on yaw and pitch angles
      updateViewPlatformTransform(viewingPlatform.getViewPlatformTransform(), yaw, pitch);
      return view;
    }
    
    /**
     * Returns the <code>yaw</code> angle used by view platform transform.
     */
    protected float getViewYaw() {
      return this.viewYaw;
    }
    
    /**
     * Sets the <code>yaw</code> angle used by view platform transform.
     */
    protected void setViewYaw(float viewYaw) {
      this.viewYaw = viewYaw;
      if (this.universe != null) {
        updateViewPlatformTransform(this.universe.getViewingPlatform().getViewPlatformTransform(), 
            this.viewYaw, getViewPitch());
      }
    }

    /**
     * Returns the <code>pitch</code> angle used by view platform transform.
     */
    protected float getViewPitch() {
      return -(float)Math.PI / 16;
    }
  
    /**
     * Updates the view platform transformation from current yaw and pitch angles. 
     */
    private void updateViewPlatformTransform(TransformGroup viewPlatformTransform,
                                             float viewYaw, float viewPitch) {
      // Default distance used to view a 2 unit wide scene
      double nominalDistanceToCenter = 1.4 / Math.tan(Math.PI / 8);
      // We don't use a TransformGroup in scene tree to be able to share the same scene 
      // in the different views displayed by OrientationPreviewComponent class 
      Transform3D pitchRotation = new Transform3D();
      pitchRotation.rotX(viewPitch);
      Transform3D yawRotation = new Transform3D();
      yawRotation.rotY(viewYaw);
      Transform3D transform = new Transform3D();
      transform.setTranslation(
          new Vector3d(Math.sin(viewYaw) * nominalDistanceToCenter * Math.cos(viewPitch), 
              nominalDistanceToCenter * Math.sin(-viewPitch), 
              Math.cos(viewYaw) * nominalDistanceToCenter * Math.cos(viewPitch)));
      yawRotation.mul(pitchRotation);
      transform.mul(yawRotation);
      viewPlatformTransform.setTransform(transform);
    }
    
    /**
     * Returns scene tree root.
     */
    private BranchGroup createSceneTree() {
      BranchGroup root = new BranchGroup();
      root.setCapability(BranchGroup.ALLOW_DETACH);
      root.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
      // Build scene tree
      root.addChild(getModelTree());
      root.addChild(getBackgroundNode());
      for (Light light : getLights()) {
        root.addChild(light);
      }
      return root;
    }
    
    /**
     * Returns the background node.  
     */
    private Node getBackgroundNode() {
      Background background = new Background(new Color3f(0.9f, 0.9f, 0.9f));
      background.setCapability(Background.ALLOW_COLOR_WRITE);
      background.setApplicationBounds(new BoundingBox());
      return background;
    }
    
    /**
     * Sets the background color.
     */
    protected void setBackgroundColor(Color backgroundColor) {
      ((Background)this.sceneTree.getChild(1)).setColor(new Color3f(backgroundColor));
    }
    
    /**
     * Returns the lights of the scene.
     */
    private Light [] getLights() {
      Light [] lights = {
          new DirectionalLight(new Color3f(0.9f, 0.9f, 0.9f), new Vector3f(1.732f, -0.8f, -1)), 
          new DirectionalLight(new Color3f(0.9f, 0.9f, 0.9f), new Vector3f(-1.732f, -0.8f, -1)), 
          new DirectionalLight(new Color3f(0.9f, 0.9f, 0.9f), new Vector3f(0, -0.8f, 1)), 
          new AmbientLight(new Color3f(0.2f, 0.2f, 0.2f))}; 

      for (Light light : lights) {
        light.setInfluencingBounds(new BoundingSphere(new Point3d(0, 0, 0), 100));
      }
      return lights;
    }

    /**
     * Returns the root of model tree.
     */
    private Node getModelTree() {
      TransformGroup modelTransformGroup = new TransformGroup();      
      //  Allow transform group to have new children
      modelTransformGroup.setCapability(Group.ALLOW_CHILDREN_READ);
      modelTransformGroup.setCapability(Group.ALLOW_CHILDREN_WRITE);
      modelTransformGroup.setCapability(Group.ALLOW_CHILDREN_EXTEND);
      // Allow the change of the transformation that sets model size and position
      modelTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
      return modelTransformGroup;
    }

    /**
     * Returns the <code>model</code> displayed by this component. 
     */
    protected BranchGroup getModel() {
      TransformGroup modelTransformGroup = (TransformGroup)this.sceneTree.getChild(0);
      if (modelTransformGroup.numChildren() > 0) {
        return (BranchGroup)modelTransformGroup.getChild(0);
      } else {
        return null;
      }
    }
    
    /**
     * Sets the <code>model</code> displayed by this component. 
     * The model is shown at its default orientation and size.
     */
    public void setModel(BranchGroup model) {
      TransformGroup modelTransformGroup = (TransformGroup)this.sceneTree.getChild(0);
      modelTransformGroup.removeAllChildren();
      if (model != null) {
        model = (BranchGroup)model.cloneTree(true);
        model.setCapability(BranchGroup.ALLOW_DETACH);
        setNodeCapabilities(model);
        
        BoundingBox bounds = ModelManager.getInstance().getBounds(model);
        Point3d lower = new Point3d();
        bounds.getLower(lower);
        Point3d upper = new Point3d();
        bounds.getUpper(upper);
        
        // Translate model to center
        Transform3D translation = new Transform3D ();
        translation.setTranslation(
            new Vector3d(-lower.x - (upper.x - lower.x) / 2, 
                         -lower.y - (upper.y - lower.y) / 2, 
                         -lower.z - (upper.z - lower.z) / 2));
        // Scale model to make it fit in a 1.8 unit wide box
        Transform3D modelTransform = new Transform3D();
        modelTransform.setScale (1.8 / Math.max (Math.max (upper.x -lower.x, upper.y - lower.y), 
                                                 upper.z - lower.z));
        modelTransform.mul(translation);
        
        modelTransformGroup.setTransform(modelTransform);
        modelTransformGroup.addChild(model);
      }
    }
    
    /**
     * Sets the capability to read bounds, to write polygon and material attributes  
     * for all children of <code>node</code>.
     */
    private void setNodeCapabilities(Node node) {
      if (node instanceof Group) {
        node.setCapability(Group.ALLOW_CHILDREN_READ);
        Enumeration enumeration = ((Group)node).getAllChildren(); 
        while (enumeration.hasMoreElements()) {
          setNodeCapabilities((Node)enumeration.nextElement());
        }
      } else if (node instanceof Shape3D) {
        node.setCapability(Node.ALLOW_BOUNDS_READ);
        Appearance appearance = ((Shape3D)node).getAppearance();
        if (appearance == null) {
          appearance = new Appearance();
          ((Shape3D)node).setAppearance(appearance);
        }
        appearance.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_READ);
        appearance.setCapability(Appearance.ALLOW_MATERIAL_READ);
        appearance.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
        node.setCapability(Shape3D.ALLOW_APPEARANCE_READ);

        PolygonAttributes polygonAttributes = appearance.getPolygonAttributes();
        if (polygonAttributes == null) {
          polygonAttributes = new PolygonAttributes();
          polygonAttributes.setCapability(PolygonAttributes.ALLOW_CULL_FACE_WRITE);
          polygonAttributes.setCapability(PolygonAttributes.ALLOW_NORMAL_FLIP_WRITE);
          appearance.setPolygonAttributes(polygonAttributes);
        }
      }
    }
    
    /**
     * Sets the back face visibility of all <code>Shape3D</code> children nodes of displayed model.
     */
    protected void setBackFaceShown(boolean backFaceShown) {
      setBackFaceShown(this.sceneTree.getChild(0), backFaceShown);
    }
    
    /**
     * Sets the back face visibility of all <code>Shape3D</code> children nodes of <code>node</code>.
     */
    private void setBackFaceShown(Node node, boolean backFaceShown) {
      if (node instanceof Group) {
        // Set visibility of all children
        Enumeration enumeration = ((Group)node).getAllChildren(); 
        while (enumeration.hasMoreElements()) {
          setBackFaceShown((Node)enumeration.nextElement(), backFaceShown);
        }
      } else if (node instanceof Shape3D) {
        Appearance appearance = ((Shape3D)node).getAppearance();
        PolygonAttributes polygonAttributes = appearance.getPolygonAttributes();
        // Change cull face
        polygonAttributes.setCullFace(backFaceShown 
            ? PolygonAttributes.CULL_FRONT
            : PolygonAttributes.CULL_BACK);
        // Change back face normal flip
        polygonAttributes.setBackFaceNormalFlip(backFaceShown);
      }
    }

    /**
     * Updates the rotation of the model displayed by this component. 
     * The model is shown at its default size.
     */
    protected void setModelRotation(float [][] modelRotation) {
      BoundingBox bounds = ModelManager.getInstance().getBounds(getModel());
      Point3d lower = new Point3d();
      bounds.getLower(lower);
      Point3d upper = new Point3d();
      bounds.getUpper(upper);
      
      // Translate model to center
      Transform3D translation = new Transform3D ();
      translation.setTranslation(
          new Vector3d(-lower.x - (upper.x - lower.x) / 2, 
                       -lower.y - (upper.y - lower.y) / 2, 
                       -lower.z - (upper.z - lower.z) / 2));
      // Apply model rotation
      Transform3D rotationTransform = new Transform3D();
      if (modelRotation != null) {
        Matrix3f modelRotationMatrix = new Matrix3f(modelRotation [0][0], modelRotation [0][1], modelRotation [0][2],
            modelRotation [1][0], modelRotation [1][1], modelRotation [1][2],
            modelRotation [2][0], modelRotation [2][1], modelRotation [2][2]);
        rotationTransform.setRotation(modelRotationMatrix);
      }
      rotationTransform.mul(translation);
      // Scale model to make it fit in a 1.8 unit wide box      
      Transform3D modelTransform = new Transform3D();
      modelTransform.setScale(1.8 / Math.max(Math.max((upper.x -lower.x), (upper.z - lower.z)), (upper.y - lower.y)));
      modelTransform.mul(rotationTransform);
      
      TransformGroup modelTransformGroup = (TransformGroup)this.sceneTree.getChild(0);
      modelTransformGroup.setTransform(modelTransform);
    }
    
    /**
     * Updates the rotation and the size of the model displayed by this component. 
     */
    protected void setModelRotationAndSize(float [][] modelRotation,
                                           float width, float depth, float height) {
      BoundingBox bounds = ModelManager.getInstance().getBounds(getModel());
      Point3d lower = new Point3d();
      bounds.getLower(lower);
      Point3d upper = new Point3d();
      bounds.getUpper(upper);
      
      // Translate model to center
      Transform3D translation = new Transform3D ();
      translation.setTranslation(
          new Vector3d(-lower.x - (upper.x - lower.x) / 2, 
                       -lower.y - (upper.y - lower.y) / 2, 
                       -lower.z - (upper.z - lower.z) / 2));
      // Scale model to make it fill a 1 unit wide box
      Transform3D scaleOneTransform = new Transform3D();
      scaleOneTransform.setScale (
          new Vector3d(1 / (upper.x -lower.x), 
              1 / (upper.y - lower.y), 
              1 / (upper.z - lower.z)));
      scaleOneTransform.mul(translation);
      // Apply model rotation
      Transform3D rotationTransform = new Transform3D();
      if (modelRotation != null) {
        Matrix3f modelRotationMatrix = new Matrix3f(modelRotation [0][0], modelRotation [0][1], modelRotation [0][2],
            modelRotation [1][0], modelRotation [1][1], modelRotation [1][2],
            modelRotation [2][0], modelRotation [2][1], modelRotation [2][2]);
        rotationTransform.setRotation(modelRotationMatrix);
      }
      rotationTransform.mul(scaleOneTransform);
      // Scale model to its size
      Transform3D scaleTransform = new Transform3D();
      if (width != 0 && depth != 0 && height != 0) {
        scaleTransform.setScale (new Vector3d(width, height, depth));
      }
      scaleTransform.mul(rotationTransform);
      // Scale model to make it fit in a 1.8 unit wide box      
      Transform3D modelTransform = new Transform3D();
      if (width != 0 && depth != 0 && height != 0) {
        modelTransform.setScale(1.8 / Math.max(Math.max(width, height), depth));
      } else {
        modelTransform.setScale(1.8 / Math.max(Math.max((upper.x -lower.x), (upper.z - lower.z)), (upper.y - lower.y)));
      }
      modelTransform.mul(scaleTransform);
      
      TransformGroup modelTransformGroup = (TransformGroup)this.sceneTree.getChild(0);
      modelTransformGroup.setTransform(modelTransform);
    }

    /**
     * Sets the color applied to piece model.
     */
    protected void setModelColor(Integer color) {
      if (color != null) {
        Color3f materialColor = new Color3f(((color >>> 16) & 0xFF) / 256f,
                                             ((color >>> 8) & 0xFF) / 256f,
                                                     (color & 0xFF) / 256f);
        setMaterial(this.sceneTree.getChild(0), 
            new Material(materialColor, new Color3f(), materialColor, materialColor, 64));
      } else {
        // Set default material of model
        setMaterial(this.sceneTree.getChild(0), null);
      }
    }

    /**
     * Sets the material attribute of all <code>Shape3D</code> children nodes of <code>node</code> 
     * with a given <code>material</code>. 
     */
    private void setMaterial(Node node, Material material) {
      if (node instanceof Group) {
        // Set material of all children
        Enumeration enumeration = ((Group)node).getAllChildren(); 
        while (enumeration.hasMoreElements()) {
          setMaterial((Node)enumeration.nextElement(), material);
        }
      } else if (node instanceof Shape3D) {
        Shape3D shape = (Shape3D)node;
        String shapeName = (String)shape.getUserData();
        // Change material of all shape that are not window panes
        if (shapeName == null
            || !shapeName.startsWith(ModelManager.WINDOW_PANE_SHAPE_PREFIX)) {
          Appearance appearance = shape.getAppearance();
          // Use appearance user data to store shape default material
          Material defaultMaterial = (Material)appearance.getUserData();
          if (defaultMaterial == null) {
            defaultMaterial = appearance.getMaterial();
            appearance.setUserData(defaultMaterial);
          }
          // Change material
          if (material != null) {
            appearance.setMaterial(material);
          } else {
            // Restore default material
            appearance.setMaterial(defaultMaterial);
          }
        }
      }
    }
  }
  
  
  /**
   * Preview component for model changes. 
   */
  private static abstract class AbstractModelPreviewComponent extends ModelPreviewComponent {
    /**
     * Adds listeners to <code>controller</code> to update the rotation of the piece model
     * displayed by this component.
     */
    protected void addRotationListener(final ImportedFurnitureWizardController controller) {
      controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.BACK_FACE_SHWON, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              setBackFaceShown(controller.isBackFaceShown());
            }
          });
      PropertyChangeListener rotationChangeListener = new PropertyChangeListener () {
          public void propertyChange(PropertyChangeEvent ev) {
            setModelRotation(controller.getModelRotation());
            
            // Update size when a new rotation is provided
            if (ev.getOldValue() != null) {
              float width = controller.getWidth();
              float depth = controller.getDepth();
              float height = controller.getHeight();
              
              // Compute size before old model rotation
              float [][] oldModelRotation = (float [][])ev.getOldValue();
              Matrix3f oldModelRotationMatrix = new Matrix3f(oldModelRotation [0][0], oldModelRotation [0][1], oldModelRotation [0][2],
                  oldModelRotation [1][0], oldModelRotation [1][1], oldModelRotation [1][2],
                  oldModelRotation [2][0], oldModelRotation [2][1], oldModelRotation [2][2]);
              oldModelRotationMatrix.invert();
              float oldWidth = oldModelRotationMatrix.m00 * width 
                  + oldModelRotationMatrix.m01 * height 
                  + oldModelRotationMatrix.m02 * depth;
              float oldHeight = oldModelRotationMatrix.m10 * width 
                  + oldModelRotationMatrix.m11 * height 
                  + oldModelRotationMatrix.m12 * depth;
              float oldDepth = oldModelRotationMatrix.m20 * width 
                  + oldModelRotationMatrix.m21 * height 
                  + oldModelRotationMatrix.m22 * depth;
              
              // Compute size after new model rotation
              float [][] newModelRotation = (float [][])ev.getNewValue();
              controller.setWidth(Math.abs(newModelRotation [0][0] * oldWidth 
                  + newModelRotation [0][1] * oldHeight 
                  + newModelRotation [0][2] * oldDepth));
              controller.setHeight(Math.abs(newModelRotation [1][0] * oldWidth 
                  + newModelRotation [1][1] * oldHeight 
                  + newModelRotation [1][2] * oldDepth));
              controller.setDepth(Math.abs(newModelRotation [2][0] * oldWidth 
                  + newModelRotation [2][1] * oldHeight 
                  + newModelRotation [2][2] * oldDepth));
            }
          }
        };
      controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.MODEL_ROTATION,
          rotationChangeListener);
    }

    /**
     * Adds listeners to <code>controller</code> to update the rotation and the size of the piece model
     * displayed by this component.
     */
    protected void addSizeListeners(final ImportedFurnitureWizardController controller) {
      controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.BACK_FACE_SHWON, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              setBackFaceShown(controller.isBackFaceShown());
            }
          });
      PropertyChangeListener sizeChangeListener = new PropertyChangeListener () {
          public void propertyChange(PropertyChangeEvent ev) {
            setModelRotationAndSize(controller.getModelRotation(),
                controller.getWidth(), controller.getDepth(), controller.getHeight());
          }
        };
      controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.MODEL_ROTATION,
          sizeChangeListener);
      controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.WIDTH,
          sizeChangeListener);
      controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.DEPTH,
          sizeChangeListener);
      controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.HEIGHT,
          sizeChangeListener);
    }

    /**
     * Adds listener to <code>controller</code> to update the color of the piece 
     * displayed by this component.
     */
    protected void addColorListener(final ImportedFurnitureWizardController controller) {
      PropertyChangeListener colorChangeListener = new PropertyChangeListener () {
          public void propertyChange(PropertyChangeEvent ev) {
            setModelColor(controller.getColor());
          }
        };
      controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.COLOR,
          colorChangeListener);
    }
    
    /**
     * Adds listener to <code>controller</code> to update the yaw of the piece icon
     * displayed by this component.
     */
    protected void addIconYawListener(final ImportedFurnitureWizardController controller) {
      PropertyChangeListener iconYawChangeListener = new PropertyChangeListener () {
          public void propertyChange(PropertyChangeEvent ev) {
            setViewYaw(controller.getIconYaw());
          }
        };
      controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.ICON_YAW,
          iconYawChangeListener);
    }
  }
  
  
  /**
   * Preview component for model orientation. 
   */
  private static class RotationPreviewComponent extends AbstractModelPreviewComponent {
    private JLabel   frontViewLabel;
    private Canvas3D frontViewCanvas;
    private JLabel   sideViewLabel;
    private Canvas3D sideViewCanvas;
    private JLabel   topViewLabel;
    private Canvas3D topViewCanvas;
    private JLabel   perspectiveViewLabel;

    public RotationPreviewComponent(final ImportedFurnitureWizardController controller) {
      addRotationListener(controller);
      createComponents();
      layoutComponents();
    }

    /**
     * Creates components displayed by this panel.
     */
    private void createComponents() {
      ResourceBundle resource = 
          ResourceBundle.getBundle(ImportedFurnitureWizardStepsPanel.class.getName());

      this.frontViewLabel = new JLabel(resource.getString("frontViewLabel.text"));
      Component3DManager canvas3DManager = Component3DManager.getInstance();
      this.frontViewCanvas = canvas3DManager.getOnscreenCanvas3D();
      this.sideViewLabel = new JLabel(resource.getString("sideViewLabel.text"));
      this.sideViewCanvas = canvas3DManager.getOnscreenCanvas3D();
      this.topViewLabel = new JLabel(resource.getString("topViewLabel.text"));
      this.topViewCanvas = canvas3DManager.getOnscreenCanvas3D();
      this.perspectiveViewLabel = new JLabel(resource.getString("perspectiveViewLabel.text"));
      
      setBorder(null);
      // Add a hierarchy listener to link canvases to universe once this component is made visible 
      addHierarchyListener();
    }

    @Override
    public Dimension getPreferredSize() {
      return new Dimension(200, 204 + this.frontViewLabel.getPreferredSize().height * 2);
    }

    /**
     * Adds a hierarchy listener to this component to manage canvases attachment to universe.  
     */
    private void addHierarchyListener() {
      addAncestorListener(new AncestorListener() {
          public void ancestorAdded(AncestorEvent event) {
            // Attach the 3 other canvases to super class universe with their own view
            createView(0, 0, View.PARALLEL_PROJECTION).addCanvas3D(frontViewCanvas);
            createView(Locale.getDefault().equals(Locale.US) 
                          ? -(float)Math.PI / 2 
                          : (float)Math.PI / 2, 
                0, View.PARALLEL_PROJECTION).addCanvas3D(sideViewCanvas);
            createView(0, -(float)Math.PI / 2, View.PARALLEL_PROJECTION).addCanvas3D(topViewCanvas);
          }
          
          public void ancestorRemoved(AncestorEvent event) {
            // Detach the 3 canvases from their view
            frontViewCanvas.getView().removeCanvas3D(frontViewCanvas);
            sideViewCanvas.getView().removeCanvas3D(sideViewCanvas);
            topViewCanvas.getView().removeCanvas3D(topViewCanvas);
            // Super class did the remaining of clean up
          }
          
          public void ancestorMoved(AncestorEvent event) {
          }        
        });
    }
    
    /**
     * Returns a bordered panel that includes <code>canvas3D</code>.
     */
    private JPanel getCanvas3DBorderedPanel(Canvas3D canvas3D) {
      JPanel canvasPanel = new JPanel(new GridLayout(1, 1));
      canvasPanel.add(canvas3D);
      canvas3D.setFocusable(false);      
      canvasPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));      
      return canvasPanel;
    }

    /**
     * Layouts components. 
     */
    private void layoutComponents() {
      // Remove default canvas 3D to put it in another place
      Canvas3D defaultCanvas = getCanvas3D(); 
      remove(defaultCanvas);
      setLayout(new GridBagLayout());
      
      // Place the 4 canvas differently depending on US or other country
      if (Locale.getDefault().equals(Locale.US)) {
        // Default projection view at top left
        add(this.perspectiveViewLabel, new GridBagConstraints(
            0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, 
            GridBagConstraints.NONE, new Insets(0, 0, 2, 5), 0, 0));
        add(getCanvas3DBorderedPanel(defaultCanvas), new GridBagConstraints(
            0, 1, 1, 1, 1, 1, GridBagConstraints.CENTER, 
            GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));
        // Top view at top right
        add(this.topViewLabel, new GridBagConstraints(
            1, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, 
            GridBagConstraints.NONE, new Insets(0, 0, 2, 0), 0, 0));
        add(getCanvas3DBorderedPanel(this.topViewCanvas), new GridBagConstraints(
            1, 1, 1, 1, 1, 1, GridBagConstraints.CENTER, 
            GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));
        // Left view at bottom left
        add(this.sideViewLabel, new GridBagConstraints(
            0, 2, 1, 1, 0, 0, GridBagConstraints.CENTER, 
            GridBagConstraints.NONE, new Insets(0, 0, 2, 5), 0, 0));
        add(getCanvas3DBorderedPanel(this.sideViewCanvas), new GridBagConstraints(
            0, 3, 1, 1, 1, 1, GridBagConstraints.CENTER, 
            GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));
        // Front view at bottom right
        add(this.frontViewLabel, new GridBagConstraints(
            1, 2, 1, 1, 0, 0, GridBagConstraints.CENTER, 
            GridBagConstraints.NONE, new Insets(0, 0, 2, 0), 0, 0));
        add(getCanvas3DBorderedPanel(this.frontViewCanvas), new GridBagConstraints(
            1, 3, 1, 1, 1, 1, GridBagConstraints.CENTER, 
            GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
      } else {
        // Right view at top left
        add(this.sideViewLabel, new GridBagConstraints(
            0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, 
            GridBagConstraints.NONE, new Insets(0, 0, 2, 5), 0, 0));
        add(getCanvas3DBorderedPanel(this.sideViewCanvas), new GridBagConstraints(
            0, 1, 1, 1, 1, 1, GridBagConstraints.CENTER, 
            GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));
        // Front view at top right
        add(this.frontViewLabel, new GridBagConstraints(
            1, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, 
            GridBagConstraints.NONE, new Insets(0, 0, 2, 0), 0, 0));
        add(getCanvas3DBorderedPanel(this.frontViewCanvas), new GridBagConstraints(
            1, 1, 1, 1, 1, 1, GridBagConstraints.CENTER, 
            GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));
        // Default projection view at bottom left
        add(this.perspectiveViewLabel, new GridBagConstraints(
            0, 2, 1, 1, 0, 0, GridBagConstraints.CENTER, 
            GridBagConstraints.NONE, new Insets(0, 0, 2, 5), 0, 0));
        add(getCanvas3DBorderedPanel(defaultCanvas), new GridBagConstraints(
            0, 3, 1, 1, 1, 1, GridBagConstraints.CENTER, 
            GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));
        // Top view at bottom right
        add(this.topViewLabel, new GridBagConstraints(
            1, 2, 1, 1, 0, 0, GridBagConstraints.CENTER, 
            GridBagConstraints.NONE, new Insets(0, 0, 2, 0), 0, 0));
        add(getCanvas3DBorderedPanel(this.topViewCanvas), new GridBagConstraints(
            1, 3, 1, 1, 1, 1, GridBagConstraints.CENTER, 
            GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
      }
    }
  }
  
  
  /**
   * Preview component for model attributes. 
   */
  private static class AttributesPreviewComponent extends AbstractModelPreviewComponent {
    public AttributesPreviewComponent(ImportedFurnitureWizardController controller) {
      addSizeListeners(controller);
      addColorListener(controller);
    }
  }
  
  
  /**
   * Preview component for model icon. 
   */
  private static class IconPreviewComponent extends AbstractModelPreviewComponent {
    private ImportedFurnitureWizardController controller;

    public IconPreviewComponent(ImportedFurnitureWizardController controller) {
      this.controller = controller;
      addSizeListeners(controller);
      addColorListener(controller);
      addIconYawListener(controller);

      setBackgroundColor(UIManager.getColor("window"));
    }

    @Override
    public Dimension getPreferredSize() {
      return new Dimension(128, 128);
    }

    /**
     * Sets the <code>yaw</code> angle used by view platform transform.
     */
    protected void setViewYaw(float viewYaw) {
      super.setViewYaw(viewYaw);
      this.controller.setIconYaw(viewYaw);
    }
    
    /**
     * Returns the icon image matching the displayed view.  
     */
    private BufferedImage getIconImage() {
      BufferedImage iconImage = null;
      // Under Mac OS X 10.5 (build 1.5.0_13-b05-237 or 1.5.0_16-b06-284) / Java 3D 1.5, 
      // there's a very strange bug with 3D offscreen images that happens *only* 
      // when the user imports furniture from the menu :
      // all screen menu bar items get disabled until an other dialog is opened   
      if (!OperatingSystem.isMacOSXLeopardOrSuperior()) {
        try {
          // Create icon from an off screen image  
          Dimension iconSize = getPreferredSize();        
          View view = createView(getViewYaw(), getViewPitch(), View.PERSPECTIVE_PROJECTION);
          iconImage = Component3DManager.getInstance().
              getOffScreenImage(view, iconSize.width, iconSize.height);
        } catch (IllegalRenderingStateException ex) {
          // Catch exception to create an image with Robot 
        }
      }
      
      if (iconImage == null) {
        // If off screen canvas fails, capture current canvas with Robot
        Component canvas3D = getCanvas3D();
        Point canvas3DOrigin = new Point();
        SwingUtilities.convertPointToScreen(canvas3DOrigin, canvas3D);
        try {
          iconImage = new Robot().createScreenCapture(
              new Rectangle(canvas3DOrigin, canvas3D.getSize()));
        } catch (AWTException ex2) {
          throw new RuntimeException(ex2);
        }
      }
      
      return iconImage;
    }
  }
}
