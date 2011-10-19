/*
 * ImportedFurnitureWizardStepsPanel.java 4 juil. 07
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
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.View;
import javax.swing.Action;
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
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import com.eteks.sweethome3d.j3d.Component3DManager;
import com.eteks.sweethome3d.j3d.ModelManager;
import com.eteks.sweethome3d.j3d.OBJWriter;
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

/**
 * Wizard panel for furniture import. 
 * @author Emmanuel Puybaret
 */
public class ImportedFurnitureWizardStepsPanel extends JPanel 
                                               implements ImportedFurnitureWizardStepsView {
  private final ImportedFurnitureWizardController controller;
  private CardLayout                        cardLayout;
  private JLabel                            modelChoiceOrChangeLabel;
  private JButton                           modelChoiceOrChangeButton;
  private JButton                           findModelsButton;
  private JLabel                            modelChoiceErrorLabel;
  private ModelPreviewComponent             modelPreviewComponent;
  private JLabel                            orientationLabel;
  private JButton                           defaultOrientationButton;
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
                                           final ImportedFurnitureWizardController controller) {
    this.controller = controller;
    // Create a model loader for each wizard in case model loading hangs
    this.modelLoader = Executors.newSingleThreadExecutor();
    createComponents(importHomePiece, preferences, controller);
    setMnemonics(preferences);
    layoutComponents();
    updateController(piece, preferences);
    if (modelName != null) {
      updateController(modelName, preferences, controller.getContentManager(),  
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
          String modelName = showModelChoiceDialog(preferences, controller.getContentManager());
          if (modelName != null) {
            updateController(modelName, preferences, 
                controller.getContentManager(), defaultModelCategory, false);
          }
        }
      });
    this.findModelsButton = new JButton(SwingTools.getLocalizedLabelText(preferences, 
        ImportedFurnitureWizardStepsPanel.class, "findModelsButton.text"));
    this.findModelsButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          boolean documentShown = false;
          try { 
            // Display Find models page in browser
            final URL findModelsUrl = new URL(preferences.getLocalizedString(
                ImportedFurnitureWizardStepsPanel.class, "findModelsButton.url"));
            documentShown = SwingTools.showDocumentInBrowser(findModelsUrl); 
          } catch (MalformedURLException ex) {
            // Document isn't shown
          }
          if (!documentShown) {
            // If the document wasn't shown, display a message 
            // with a copiable URL in a message box 
            JTextArea findModelsMessageTextArea = new JTextArea(preferences.getLocalizedString(
                ImportedFurnitureWizardStepsPanel.class, "findModelsMessage.text"));
            String findModelsTitle = preferences.getLocalizedString(
                ImportedFurnitureWizardStepsPanel.class, "findModelsMessage.title");
            findModelsMessageTextArea.setEditable(false);
            findModelsMessageTextArea.setOpaque(false);
            JOptionPane.showMessageDialog(SwingUtilities.getRootPane(ImportedFurnitureWizardStepsPanel.this), 
                findModelsMessageTextArea, findModelsTitle, 
                JOptionPane.INFORMATION_MESSAGE);
          }
        }
      });
    this.modelChoiceErrorLabel = new JLabel(preferences.getLocalizedString(
        ImportedFurnitureWizardStepsPanel.class, "modelChoiceErrorLabel.text"));
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
            final String modelName = files.get(0).getAbsolutePath();
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                  updateController(modelName, preferences, 
                      controller.getContentManager(), defaultModelCategory, false);
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
                  JOptionPane.showMessageDialog(SwingUtilities.getRootPane(ImportedFurnitureWizardStepsPanel.this), 
                      preferences.getLocalizedString(ImportedFurnitureWizardStepsPanel.class, "modelChoiceError"));
                }
              });
          }
          return success;
        }
      });
    this.modelPreviewComponent.setBorder(SwingTools.getDropableComponentBorder());
   
    // Orientation panel components
    this.orientationLabel = new JLabel(preferences.getLocalizedString(
        ImportedFurnitureWizardStepsPanel.class, "orientationLabel.text"));
    this.defaultOrientationButton = new JButton(new ResourceAction(preferences, 
            ImportedFurnitureWizardStepsPanel.class, "DEFAULT_ORIENTATION", true) {
        @Override
        public void actionPerformed(ActionEvent ev) {
          updateModelRotation(new Transform3D());
        }
      });
    this.turnLeftButton = new AutoRepeatButton(new ResourceAction(preferences, 
            ImportedFurnitureWizardStepsPanel.class, "TURN_LEFT", true) {
        @Override
        public void actionPerformed(ActionEvent ev) {
          Transform3D oldTransform = getModelRotationTransform();
          Transform3D leftRotation = new Transform3D();
          leftRotation.rotY((ev.getModifiers() & ActionEvent.SHIFT_MASK) == 0 
              ? -Math.PI / 2 
              : Math.toRadians(-1));
          leftRotation.mul(oldTransform);
          updateModelRotation(leftRotation);
        }
      });
    this.turnRightButton = new AutoRepeatButton(new ResourceAction(preferences, 
            ImportedFurnitureWizardStepsPanel.class, "TURN_RIGHT", true) {
        @Override
        public void actionPerformed(ActionEvent ev) {
          Transform3D oldTransform = getModelRotationTransform();
          Transform3D rightRotation = new Transform3D();
          rightRotation.rotY((ev.getModifiers() & ActionEvent.SHIFT_MASK) == 0 
              ? Math.PI / 2 
              : Math.toRadians(1));
          rightRotation.mul(oldTransform);
          updateModelRotation(rightRotation);
        }
      });
    this.turnUpButton = new AutoRepeatButton(new ResourceAction(preferences, 
            ImportedFurnitureWizardStepsPanel.class, "TURN_UP", true) {
        @Override
        public void actionPerformed(ActionEvent ev) {
          Transform3D oldTransform = getModelRotationTransform();
          Transform3D upRotation = new Transform3D();
          upRotation.rotX((ev.getModifiers() & ActionEvent.SHIFT_MASK) == 0 
              ? -Math.PI / 2 
              : Math.toRadians(-1));
          upRotation.mul(oldTransform);
          updateModelRotation(upRotation);
        }
      });
    this.turnDownButton = new AutoRepeatButton(new ResourceAction(preferences, 
            ImportedFurnitureWizardStepsPanel.class, "TURN_DOWN", true) {
        @Override
        public void actionPerformed(ActionEvent ev) {
          Transform3D oldTransform = getModelRotationTransform();
          Transform3D downRotation = new Transform3D();
          downRotation.rotX((ev.getModifiers() & ActionEvent.SHIFT_MASK) == 0 
              ? Math.PI / 2 
              : Math.toRadians(1));
          downRotation.mul(oldTransform);
          updateModelRotation(downRotation);
        }
      });
    
    this.backFaceShownLabel = new JLabel(preferences.getLocalizedString(
        ImportedFurnitureWizardStepsPanel.class, "backFaceShownLabel.text"));
    this.backFaceShownCheckBox = new JCheckBox(SwingTools.getLocalizedLabelText(preferences, 
        ImportedFurnitureWizardStepsPanel.class, "backFaceShownCheckBox.text"));
    this.backFaceShownCheckBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          controller.setBackFaceShown(backFaceShownCheckBox.isSelected());
        }
      });
    controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.BACK_FACE_SHOWN,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            // If back face shown changes update back face shown check box
            backFaceShownCheckBox.setSelected(controller.isBackFaceShown());
          }
        });
    this.rotationPreviewComponent = new RotationPreviewComponent(preferences, controller);
    
    // Attributes panel components
    this.attributesLabel = new JLabel(preferences.getLocalizedString(
        ImportedFurnitureWizardStepsPanel.class, "attributesLabel.text"));
    this.nameLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, 
        ImportedFurnitureWizardStepsPanel.class, "nameLabel.text"));
    this.nameTextField = new JTextField(10);
    if (!OperatingSystem.isMacOSXLeopardOrSuperior()) {
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

    this.addToCatalogCheckBox = new JCheckBox(SwingTools.getLocalizedLabelText(preferences, 
        ImportedFurnitureWizardStepsPanel.class, "addToCatalogCheckBox.text"));
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
    this.categoryLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, 
        ImportedFurnitureWizardStepsPanel.class, "categoryLabel.text")); 
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

    this.widthLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, 
        ImportedFurnitureWizardStepsPanel.class, "widthLabel.text", unitName)); 
    final float minimumLength = preferences.getLengthUnit().getMinimumLength();
    final NullableSpinner.NullableSpinnerLengthModel widthSpinnerModel = 
        new NullableSpinner.NullableSpinnerLengthModel(preferences, Math.min(controller.getWidth(), minimumLength), 100000f);
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
            widthSpinnerModel.setMinimum(Math.min(controller.getWidth(), minimumLength));
          }
        });
    
    this.depthLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, 
        ImportedFurnitureWizardStepsPanel.class, "depthLabel.text", unitName)); 
    final NullableSpinner.NullableSpinnerLengthModel depthSpinnerModel = 
        new NullableSpinner.NullableSpinnerLengthModel(preferences, Math.min(controller.getDepth(), minimumLength), 100000f);
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
            depthSpinnerModel.setMinimum(Math.min(controller.getDepth(), minimumLength));
          }
        });
    
    this.heightLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, 
            ImportedFurnitureWizardStepsPanel.class, "heightLabel.text", unitName)); 
    final NullableSpinner.NullableSpinnerLengthModel heightSpinnerModel = 
        new NullableSpinner.NullableSpinnerLengthModel(preferences, Math.min(controller.getHeight(), minimumLength), 100000f);
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
            heightSpinnerModel.setMinimum(Math.min(controller.getHeight(), minimumLength));
          }
        });
    this.keepProportionsCheckBox = new JCheckBox(SwingTools.getLocalizedLabelText(preferences, 
        ImportedFurnitureWizardStepsPanel.class, "keepProportionsCheckBox.text"));
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
    
    this.elevationLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, 
        ImportedFurnitureWizardStepsPanel.class, "elevationLabel.text", unitName)); 
    final NullableSpinner.NullableSpinnerLengthModel elevationSpinnerModel = 
        new NullableSpinner.NullableSpinnerLengthModel(preferences, 0f, 2500f);
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
    
    this.movableCheckBox = new JCheckBox(SwingTools.getLocalizedLabelText(preferences, 
        ImportedFurnitureWizardStepsPanel.class, "movableCheckBox.text"));
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

    this.doorOrWindowCheckBox = new JCheckBox(SwingTools.getLocalizedLabelText(preferences, 
        ImportedFurnitureWizardStepsPanel.class, "doorOrWindowCheckBox.text"));
    this.doorOrWindowCheckBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          controller.setDoorOrWindow(doorOrWindowCheckBox.isSelected());
        }
      });
    controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.DOOR_OR_WINDOW,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            // If door or window changes update door or window check box
            boolean doorOrWindow = controller.isDoorOrWindow();
            doorOrWindowCheckBox.setSelected(doorOrWindow);
            movableCheckBox.setEnabled(!doorOrWindow);
          }
        });

    this.colorLabel = new JLabel(
        String.format(SwingTools.getLocalizedLabelText(preferences, 
            ImportedFurnitureWizardStepsPanel.class, "colorLabel.text"), unitName));
    this.colorButton = new ColorButton();
    this.colorButton.setColorDialogTitle(preferences.getLocalizedString(
        ImportedFurnitureWizardStepsPanel.class, "colorDialog.title"));
    this.colorButton.addPropertyChangeListener(ColorButton.COLOR_PROPERTY, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            controller.setColor(colorButton.getColor());
          }
        });
    this.clearColorButton = new JButton(SwingTools.getLocalizedLabelText(preferences, 
        ImportedFurnitureWizardStepsPanel.class, "clearColorButton.text"));
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
    this.iconLabel = new JLabel(preferences.getLocalizedString(
        ImportedFurnitureWizardStepsPanel.class, "iconLabel.text"));
    this.iconPreviewComponent = new IconPreviewComponent(this.controller);
  }

  /**
   * A button that repeats its action when kept pressed.
   */
  private static class AutoRepeatButton extends JButton {
    private boolean shiftPressed;

    public AutoRepeatButton(final Action action) {
      super(action);
      // Create a timer that will repeat action each 40 ms when SHIFT is pressed
      final Timer timer = new Timer(40, new ActionListener() {
          public void actionPerformed(ActionEvent ev) {
            action.actionPerformed(
                new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null, ev.getWhen(), ActionEvent.SHIFT_MASK));
          }
        });
      timer.setInitialDelay(250);
      
      // Update timer when button is armed
      addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                  if (shiftPressed) {
                    if (getModel().isArmed()
                        && !timer.isRunning()) {
                      timer.restart();
                    } else if (!getModel().isArmed()
                               && timer.isRunning()) {
                      timer.stop();
                    }
                  }
                }
              });
          }
        });
      addMouseListener(new MouseAdapter() {
          @Override
          public void mousePressed(MouseEvent ev) {
            shiftPressed = ev.isShiftDown();
          }
        });
    }
  }
  
  /**
   * Sets components mnemonics and label / component associations.
   */
  private void setMnemonics(UserPreferences preferences) {
    if (!OperatingSystem.isMacOSX()) {
      this.findModelsButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          ImportedFurnitureWizardStepsPanel.class, "findModelsButton.mnemonic")).getKeyCode());
      this.backFaceShownCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          ImportedFurnitureWizardStepsPanel.class, "backFaceShownCheckBox.mnemonic")).getKeyCode());
      this.nameLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          ImportedFurnitureWizardStepsPanel.class, "nameLabel.mnemonic")).getKeyCode());
      this.nameLabel.setLabelFor(this.nameTextField);
      this.categoryLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          ImportedFurnitureWizardStepsPanel.class, "categoryLabel.mnemonic")).getKeyCode());
      this.categoryLabel.setLabelFor(this.categoryComboBox);
      this.addToCatalogCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          ImportedFurnitureWizardStepsPanel.class, "addToCatalogCheckBox.mnemonic")).getKeyCode());;
      this.widthLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          ImportedFurnitureWizardStepsPanel.class, "widthLabel.mnemonic")).getKeyCode());
      this.widthLabel.setLabelFor(this.widthSpinner);
      this.depthLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          ImportedFurnitureWizardStepsPanel.class, "depthLabel.mnemonic")).getKeyCode());
      this.depthLabel.setLabelFor(this.depthSpinner);
      this.heightLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          ImportedFurnitureWizardStepsPanel.class, "heightLabel.mnemonic")).getKeyCode());
      this.heightLabel.setLabelFor(this.heightSpinner);
      this.keepProportionsCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          ImportedFurnitureWizardStepsPanel.class, "keepProportionsCheckBox.mnemonic")).getKeyCode());
      this.elevationLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          ImportedFurnitureWizardStepsPanel.class, "elevationLabel.mnemonic")).getKeyCode());
      this.elevationLabel.setLabelFor(this.elevationSpinner);
      this.movableCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          ImportedFurnitureWizardStepsPanel.class, "movableCheckBox.mnemonic")).getKeyCode());;
      this.doorOrWindowCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          ImportedFurnitureWizardStepsPanel.class, "doorOrWindowCheckBox.mnemonic")).getKeyCode());;
      this.colorLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          ImportedFurnitureWizardStepsPanel.class, "colorLabel.mnemonic")).getKeyCode());
      this.colorLabel.setLabelFor(this.colorButton);
      this.clearColorButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          ImportedFurnitureWizardStepsPanel.class, "clearColorButton.mnemonic")).getKeyCode());
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
        public void applyComponentOrientation(ComponentOrientation orientation) {
          // Ignore panel orientation to ensure left button is always at left of panel
        }
      };
    rotationButtonsPanel.add(this.turnUpButton, new GridBagConstraints(
        1, 0, 1, 1, 0, 0, GridBagConstraints.SOUTH, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 0), 0, 0));    
    rotationButtonsPanel.add(this.turnLeftButton, new GridBagConstraints(
        0, 1, 1, 1, 0, 0, GridBagConstraints.EAST, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
    rotationButtonsPanel.add(this.defaultOrientationButton, new GridBagConstraints(
        1, 1, 1, 1, 0, 0, GridBagConstraints.CENTER, 
        GridBagConstraints.NONE, new Insets(0, 0, 5, 0), 0, 0));
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
  private void updateController(final CatalogPieceOfFurniture piece,
                                final UserPreferences preferences) {
    if (piece == null) {
      setModelChoiceTexts(preferences);
      updatePreviewComponentsModel(null);
    } else {
      setModelChangeTexts(preferences);
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
                    setModelChoiceTexts(preferences);
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
                                final UserPreferences preferences,
                                final ContentManager contentManager,
                                final FurnitureCategory defaultCategory,
                                final boolean ignoreException) {
    // Cancel current model
    this.controller.setModel(null);
    // Read model in modelLoader executor
    this.modelLoader.execute(new Runnable() {
        public void run() {
          Content modelContent = null;
          try {
            modelContent = contentManager.getContent(modelName);
          } catch (RecorderException ex) {
            if (!ignoreException) {
              showModelChoiceError(modelName, preferences);
            }
            return;
          } 
          
          BranchGroup model = null;
          Vector3f    modelSize = null;
          try {
            model = readModel(modelContent);
            modelSize = ModelManager.getInstance().getSize(model);
            // Copy model to a temporary OBJ content with materials and textures
            modelContent = copyToTemporaryOBJContent(model, modelName);
          } catch (IOException ex) {
            model = null;
          } catch (IllegalArgumentException ex) {
            // Model is empty
            model = null;
          }
          
          if (model == null) {
            try {
              // Copy model content to a temporary content
              modelContent = TemporaryURLContent.copyToTemporaryURLContent(modelContent);
            } catch (IOException ex2) {
              if (!ignoreException) {
                showModelChoiceError(modelName, preferences);
              }
              return;
            }
            
            // If content couldn't be loaded, try to load model as a zipped file
            ZipInputStream zipIn = null;
            try {
              URLContent urlContent = (URLContent)modelContent;
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
                      URL entryUrl = new URL("jar:" + urlContent.getURL() + "!/" 
                          + URLEncoder.encode(entryName, "UTF-8").replace("+", "%20").replace("%2F", "/"));
                      modelContent = new TemporaryURLContent(entryUrl);
                      model = readModel(modelContent);
                      modelSize = ModelManager.getInstance().getSize(model);
                      break;
                    }
                  }
                } catch (IOException ex3) {
                  // Ignore exception and try next entry
                  model = null;
                } catch (IllegalArgumentException ex3) {
                  // Model is empty
                  model = null;
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
          
          final BranchGroup readModel = model;
          final Vector3f    readModelSize = modelSize;
          final Content     readContent = modelContent;
          // Update components in dispatch thread
          EventQueue.invokeLater(new Runnable() {
              public void run() {
                if (readModel != null) {
                  controller.setModel(readContent);
                  setModelChangeTexts(preferences);
                  modelChoiceErrorLabel.setVisible(false);
                  controller.setModelRotation(new float [][] {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}});
                  controller.setBackFaceShown(false);
                  controller.setName(contentManager.getPresentationName(
                      modelName, ContentManager.ContentType.MODEL));
                  controller.setCategory(defaultCategory);
                  // Initialize size with default values
                  controller.setWidth(readModelSize.x);
                  controller.setDepth(readModelSize.z);
                  controller.setHeight(readModelSize.y);
                  controller.setMovable(true);
                  controller.setDoorOrWindow(false);
                  controller.setColor(null);                  
                  controller.setIconYaw((float)Math.PI / 8);
                  controller.setProportional(true);
                } else if (isShowing()) {
                  controller.setModel(null);
                  setModelChoiceTexts(preferences);
                  JOptionPane.showMessageDialog(SwingUtilities.getRootPane(ImportedFurnitureWizardStepsPanel.this), 
                      preferences.getLocalizedString(ImportedFurnitureWizardStepsPanel.class, "modelChoiceFormatError"));
                }
              }
            });
        }
      });
  }

  /**
   * Shows an error message about the chosen model.
   */
  private void showModelChoiceError(final String modelName,
                                    final UserPreferences preferences) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        JOptionPane.showMessageDialog(SwingUtilities.getRootPane(ImportedFurnitureWizardStepsPanel.this), 
            preferences.getLocalizedString(
                ImportedFurnitureWizardStepsPanel.class, "modelChoiceError", modelName));
      }
    });
  }
  
  /**
   * Returns a copy of a given <code>model</code> as a zip content at OBJ format.
   * Caution : this method must be thread safe because it's called from model loader executor. 
   */
  private Content copyToTemporaryOBJContent(BranchGroup model, String modelName) throws IOException {
    try {
      setReadingState();
      String objFile = new File(modelName).getName();
      if (!objFile.toLowerCase().endsWith(".obj")) {
        objFile += ".obj";
      }
      File tempZipFile = OperatingSystem.createTemporaryFile("import", ".zip");
      OBJWriter.writeNodeInZIPFile(model, tempZipFile, 0, objFile, "3D model import " + modelName);
      return new TemporaryURLContent(new URL("jar:" + tempZipFile.toURI().toURL() + "!/" 
          + URLEncoder.encode(objFile, "UTF-8").replace("+", "%20")));
    } finally {
      setDefaultState();
    }
  }

  /**
   * Reads 3D model from <code>modelContent</code>.
   * Caution : this method must be thread safe because it's called from model loader executor. 
   */
  private BranchGroup readModel(Content modelContent) throws IOException {
    try {
      setReadingState();
      EventQueue.invokeLater(new Runnable() {
          public void run() {
            updatePreviewComponentsModel(null);
          }
        });
            
      // Load piece model 
      final BranchGroup modelNode = ModelManager.getInstance().loadModel(modelContent);
      
      // Change live object in Event Dispatch Thread
      EventQueue.invokeLater(new Runnable() {
          public void run() {
            updatePreviewComponentsModel(modelNode);
          }
        });
      return modelNode;
    } finally {
      setDefaultState();
    } 
  }

  /**
   * Sets the cursor to wait cursor and disables model choice button.
   */
  private void setReadingState() {
    EventQueue.invokeLater(new Runnable() {
        public void run() {
          modelChoiceOrChangeButton.setEnabled(false);
          Component rootPane = SwingUtilities.getRoot(ImportedFurnitureWizardStepsPanel.this);
          if (defaultCursor == null) {
            defaultCursor = rootPane.getCursor();
          }
          rootPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }
      });
  }

  /**
   * Sets the default cursor and enables model choice button.
   */
  private void setDefaultState() {
    EventQueue.invokeLater(new Runnable() {
        public void run() {
          modelChoiceOrChangeButton.setEnabled(true);
          SwingUtilities.getRoot(ImportedFurnitureWizardStepsPanel.this).setCursor(defaultCursor);
        }
      });
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
  private void setModelChangeTexts(UserPreferences preferences) {
    this.modelChoiceOrChangeLabel.setText(preferences.getLocalizedString(
        ImportedFurnitureWizardStepsPanel.class, "modelChangeLabel.text")); 
    this.modelChoiceOrChangeButton.setText(SwingTools.getLocalizedLabelText(preferences, 
        ImportedFurnitureWizardStepsPanel.class, "modelChangeButton.text"));
    if (!OperatingSystem.isMacOSX()) {
      this.modelChoiceOrChangeButton.setMnemonic(
          KeyStroke.getKeyStroke(preferences.getLocalizedString(
              ImportedFurnitureWizardStepsPanel.class, "modelChangeButton.mnemonic")).getKeyCode());
    }
  }

  /**
   * Sets the texts of label and button of model panel with choice texts. 
   */
  private void setModelChoiceTexts(UserPreferences preferences) {
    this.modelChoiceOrChangeLabel.setText(preferences.getLocalizedString(
        ImportedFurnitureWizardStepsPanel.class, "modelChoiceLabel.text")); 
    this.modelChoiceOrChangeButton.setText(SwingTools.getLocalizedLabelText(preferences, 
        ImportedFurnitureWizardStepsPanel.class, "modelChoiceButton.text"));
    if (!OperatingSystem.isMacOSX()) {
      this.modelChoiceOrChangeButton.setMnemonic(
          KeyStroke.getKeyStroke(preferences.getLocalizedString(
              ImportedFurnitureWizardStepsPanel.class, "modelChoiceButton.mnemonic")).getKeyCode());
    }
  }
  
  /**
   * Returns the model name chosen for a file chooser dialog.
   */
  private String showModelChoiceDialog(UserPreferences preferences,
                                       ContentManager contentManager) {
    return contentManager.showOpenDialog(this, 
        preferences.getLocalizedString(
            ImportedFurnitureWizardStepsPanel.class, "modelChoiceDialog.title"), 
        ContentManager.ContentType.MODEL);
  }
  
  /**
   * Returns the icon content of the chosen piece.
   * Icon is created once on demand of view's controller, because it demands either  
   * icon panel being displayed, or an offscreen 3D buffer that costs too much to create at each yaw change.
   */
  public Content getIcon() {
    try {
      return this.iconPreviewComponent.getIcon(400);
    } catch (IOException ex) {
      try {
        return new URLContent(new URL("file:/dummySweetHome3DContent"));
      } catch (MalformedURLException ex1) {
        return null;
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
      controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.BACK_FACE_SHOWN,  
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              setBackFaceShown(controller.isBackFaceShown());
            }
          });
      PropertyChangeListener rotationChangeListener = new PropertyChangeListener () {
          public void propertyChange(PropertyChangeEvent ev) {
            setModelRotation(controller.getModelRotation());
            
            // Update size when a new rotation is provided
            BranchGroup model = getModel();
            if (ev.getOldValue() != null
                && model != null) {
              try {
                float [][] oldModelRotation = (float [][])ev.getOldValue();
                Transform3D normalization = ModelManager.getInstance().getNormalizedTransform(model, oldModelRotation, 1f);
                Transform3D scaleTransform = new Transform3D();
                scaleTransform.setScale(new Vector3d(controller.getWidth(), controller.getHeight(), controller.getDepth()));
                scaleTransform.mul(normalization);
                
                // Compute rotation before old model rotation
                Matrix3f oldModelRotationMatrix = new Matrix3f(oldModelRotation [0][0], oldModelRotation [0][1], oldModelRotation [0][2],
                    oldModelRotation [1][0], oldModelRotation [1][1], oldModelRotation [1][2],
                    oldModelRotation [2][0], oldModelRotation [2][1], oldModelRotation [2][2]);
                oldModelRotationMatrix.invert();
                Transform3D backRotationTransform = new Transform3D();
                backRotationTransform.setRotation(oldModelRotationMatrix);
                backRotationTransform.mul(scaleTransform);
                
                // Compute size after new model rotation
                float [][] newModelRotation = (float [][])ev.getNewValue();
                Matrix3f newModelRotationMatrix = new Matrix3f(newModelRotation [0][0], newModelRotation [0][1], newModelRotation [0][2],
                    newModelRotation [1][0], newModelRotation [1][1], newModelRotation [1][2],
                    newModelRotation [2][0], newModelRotation [2][1], newModelRotation [2][2]);
                Transform3D transform = new Transform3D();
                transform.setRotation(newModelRotationMatrix);
                transform.mul(backRotationTransform);
                
                Vector3f newSize = ModelManager.getInstance().getSize(model, transform);
                controller.setWidth(newSize.x);
                controller.setHeight(newSize.y);
                controller.setDepth(newSize.z);
              } catch (IllegalArgumentException ex) {
                // Model is empty
              }
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
      controller.addPropertyChangeListener(ImportedFurnitureWizardController.Property.BACK_FACE_SHOWN, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              setBackFaceShown(controller.isBackFaceShown());
            }
          });
      PropertyChangeListener sizeChangeListener = new PropertyChangeListener() {
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

    public RotationPreviewComponent(UserPreferences preferences, 
                                    final ImportedFurnitureWizardController controller) {
      addRotationListener(controller);
      createComponents(preferences);
      layoutComponents();
    }

    /**
     * Creates components displayed by this panel.
     */
    private void createComponents(UserPreferences preferences) {
      this.frontViewLabel = new JLabel(preferences.getLocalizedString(
          ImportedFurnitureWizardStepsPanel.class, "frontViewLabel.text"));
      Component3DManager canvas3DManager = Component3DManager.getInstance();
      this.frontViewCanvas = canvas3DManager.getOnscreenCanvas3D();
      this.sideViewLabel = new JLabel(preferences.getLocalizedString(
          ImportedFurnitureWizardStepsPanel.class, "sideViewLabel.text"));
      this.sideViewCanvas = canvas3DManager.getOnscreenCanvas3D();
      this.topViewLabel = new JLabel(preferences.getLocalizedString(
          ImportedFurnitureWizardStepsPanel.class, "topViewLabel.text"));
      this.topViewCanvas = canvas3DManager.getOnscreenCanvas3D();
      this.perspectiveViewLabel = new JLabel(preferences.getLocalizedString(
          ImportedFurnitureWizardStepsPanel.class, "perspectiveViewLabel.text"));
      
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
            createView(0, 0, 1, View.PARALLEL_PROJECTION).addCanvas3D(frontViewCanvas);
            createView(Locale.getDefault().equals(Locale.US) 
                          ? -(float)Math.PI / 2 
                          : (float)Math.PI / 2, 
                0, 1, View.PARALLEL_PROJECTION).addCanvas3D(sideViewCanvas);
            createView(0, -(float)Math.PI / 2, 1, View.PARALLEL_PROJECTION).addCanvas3D(topViewCanvas);
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

      setBackground(UIManager.getColor("window"));
    }

    @Override
    public Dimension getPreferredSize() {
      Insets insets = getInsets();
      return new Dimension(128 + insets.left + insets.right, 128  + insets.top + insets.bottom);
    }

    /**
     * Sets the <code>yaw</code> angle used by view platform transform.
     */
    protected void setViewYaw(float viewYaw) {
      super.setViewYaw(viewYaw);
      this.controller.setIconYaw(viewYaw);
    }
  }
}
