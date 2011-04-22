/*
 * ImportedFurnitureWizardStepsController.java 4 juil. 07
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
package com.eteks.sweethome3d.viewcontroller;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.CatalogDoorOrWindow;
import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.DoorOrWindow;
import com.eteks.sweethome3d.model.FurnitureCatalog;
import com.eteks.sweethome3d.model.FurnitureCategory;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeDoorOrWindow;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.Sash;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * Wizard controller to manage furniture importation.
 * @author Emmanuel Puybaret
 */
public class ImportedFurnitureWizardController extends WizardController 
                                               implements Controller {
  public enum Property {STEP, NAME, MODEL, WIDTH, DEPTH, HEIGHT, ELEVATION, MOVABLE, 
      DOOR_OR_WINDOW, COLOR, CATEGORY, BACK_FACE_SHOWN, MODEL_ROTATION,  
      ICON_YAW, PROPORTIONAL}

  public enum Step {MODEL, ROTATION, ATTRIBUTES, ICON};
  
  private final Home                             home;
  private final CatalogPieceOfFurniture          piece;
  private final String                           modelName;
  private final UserPreferences                  preferences;
  private final ContentManager                   contentManager;
  private final UndoableEditSupport              undoSupport;
  private final PropertyChangeSupport            propertyChangeSupport;

  private final ImportedFurnitureWizardStepState furnitureModelStepState;
  private final ImportedFurnitureWizardStepState furnitureOrientationStepState;
  private final ImportedFurnitureWizardStepState furnitureAttributesStepState;
  private final ImportedFurnitureWizardStepState furnitureIconStepState;
  private ImportedFurnitureWizardStepsView       stepsView;
  
  private Step                             step;
  private String                           name;
  private Content                          model;
  private float                            width;
  private float                            depth;
  private float                            height;
  private float                            elevation;
  private boolean                          movable;
  private boolean                          doorOrWindow;
  private Integer                          color;
  private FurnitureCategory                category;
  private boolean                          backFaceShown;
  private float [][]                       modelRotation;
  private float                            iconYaw;
  private boolean                          proportional;
  private final ViewFactory viewFactory;
  
  /**
   * Creates a controller that edits a new catalog piece of furniture.
   */
  public ImportedFurnitureWizardController(UserPreferences preferences,
                                           ViewFactory    viewFactory,
                                           ContentManager contentManager) {
    this(null, null, null, preferences, viewFactory, contentManager, null);
  }
  
  /**
   * Creates a controller that edits a new catalog piece of furniture with a given 
   * <code>modelName</code>.
   */
  public ImportedFurnitureWizardController(String modelName,
                                           UserPreferences preferences,
                                           ViewFactory    viewFactory,
                                           ContentManager contentManager) {
    this(null, null, modelName, preferences, viewFactory, contentManager, null);
  }
  
  /**
   * Creates a controller that edits <code>piece</code> values.
   */
  public ImportedFurnitureWizardController(CatalogPieceOfFurniture piece, 
                                           UserPreferences preferences,
                                           ViewFactory    viewFactory,
                                           ContentManager contentManager) {
    this(null, piece, null, preferences, viewFactory, contentManager, null);
  }
  
  /**
   * Creates a controller that edits a new imported home piece of furniture.
   */
  public ImportedFurnitureWizardController(Home home, 
                                           UserPreferences preferences,
                                           ViewFactory    viewFactory,
                                           ContentManager contentManager,
                                           UndoableEditSupport undoSupport) {
    this(home, null, null, preferences, viewFactory, contentManager, undoSupport);
  }
  
  /**
   * Creates a controller that edits a new imported home piece of furniture
   * with a given <code>modelName</code>.
   */
  public ImportedFurnitureWizardController(Home home,
                                           String modelName,
                                           UserPreferences preferences,
                                           ViewFactory    viewFactory,
                                           ContentManager contentManager,
                                           UndoableEditSupport undoSupport) {
    this(home, null, modelName, preferences, viewFactory, contentManager, undoSupport);
  }
  
  /**
   * Creates a controller that edits <code>piece</code> values.
   */
  private ImportedFurnitureWizardController(Home home, 
                                            CatalogPieceOfFurniture piece,
                                            String modelName,
                                            UserPreferences preferences,
                                            ViewFactory    viewFactory,
                                            ContentManager contentManager,
                                            UndoableEditSupport undoSupport) {
    super(preferences, viewFactory);
    this.home = home;
    this.piece = piece;
    this.modelName = modelName;
    this.preferences = preferences;
    this.viewFactory = viewFactory;
    this.undoSupport = undoSupport;
    this.contentManager = contentManager;
    this.propertyChangeSupport = new PropertyChangeSupport(this);
    setTitle(this.preferences.getLocalizedString(ImportedFurnitureWizardController.class,
        piece == null 
            ? "importFurnitureWizard.title" 
            : "modifyFurnitureWizard.title"));    
    // Initialize states
    this.furnitureModelStepState = new FurnitureModelStepState();
    this.furnitureOrientationStepState = new FurnitureOrientationStepState();
    this.furnitureAttributesStepState = new FurnitureAttributesStepState();
    this.furnitureIconStepState = new FurnitureIconStepState();
    setStepState(this.furnitureModelStepState);
  }

  /**
   * Imports piece in catalog and/or home and posts an undoable operation.
   */
  @Override
  public void finish() {
    CatalogPieceOfFurniture newPiece;
    if (isDoorOrWindow()) {
      newPiece = new CatalogDoorOrWindow(getName(), getIcon(), getModel(), 
          getWidth(), getDepth(), getHeight(), getElevation(), 
          isMovable(), 1, 0, new Sash [0], getColor(), 
          getModelRotation(), isBackFaceShown(), 
          getIconYaw(), isProportional());
    } else {
      newPiece = new CatalogPieceOfFurniture(getName(), getIcon(), getModel(), 
          getWidth(), getDepth(), getHeight(), getElevation(), 
          isMovable(), getColor(), 
          getModelRotation(), isBackFaceShown(), 
          getIconYaw(), isProportional());
    }
    
    if (this.home != null) {
      // Add new piece to home
      addPieceOfFurniture(isDoorOrWindow() 
          ? new HomeDoorOrWindow((DoorOrWindow)newPiece)
          : new HomePieceOfFurniture(newPiece));
    }
    // Remove the edited piece from catalog
    FurnitureCatalog catalog = this.preferences.getFurnitureCatalog();
    if (this.piece != null) {
      catalog.delete(this.piece);
    }
    // If a category exists, add new piece to catalog
    if (this.category != null) {
      catalog.add(this.category, newPiece);
    }
  }
  
  /**
   * Controls new piece added to home. 
   * Once added the furniture will be selected in view 
   * and undo support will receive a new undoable edit.
   * @param piece the piece of furniture to add.
   */
  public void addPieceOfFurniture(HomePieceOfFurniture piece) {
    List<Selectable> oldSelection = this.home.getSelectedItems(); 
    // Get index of the piece added to home
    int pieceIndex = this.home.getFurniture().size();
    
    this.home.addPieceOfFurniture(piece, pieceIndex);
    this.home.setSelectedItems(Arrays.asList(piece)); 
    if (this.undoSupport != null) {
      UndoableEdit undoableEdit = new PieceOfFurnitureImportationUndoableEdit(
          this.home, this.preferences, oldSelection, piece, pieceIndex);
      this.undoSupport.postEdit(undoableEdit);
    }
  }
  
  /**
   * Undoable edit for piece importation. This class isn't anonymous to avoid
   * being bound to controller and its view.
   */
  private static class PieceOfFurnitureImportationUndoableEdit extends AbstractUndoableEdit {
    private final Home                 home;
    private final UserPreferences      preferences;
    private final List<Selectable>     oldSelection;
    private final HomePieceOfFurniture piece;
    private final int                  pieceIndex;

    private PieceOfFurnitureImportationUndoableEdit(Home home,
                                                    UserPreferences preferences, 
                                                    List<Selectable> oldSelection,
                                                    HomePieceOfFurniture piece,
                                                    int pieceIndex) {
      this.home = home;
      this.preferences = preferences;
      this.oldSelection = oldSelection;
      this.piece = piece;
      this.pieceIndex = pieceIndex;
    }

    @Override
    public void undo() throws CannotUndoException {
      super.undo();
      this.home.deletePieceOfFurniture(this.piece);
      this.home.setSelectedItems(this.oldSelection); 
    }

    @Override
    public void redo() throws CannotRedoException {
      super.redo();
      this.home.addPieceOfFurniture(this.piece, this.pieceIndex);
      this.home.setSelectedItems(Arrays.asList(this.piece)); 
    }

    @Override
    public String getPresentationName() {
      return this.preferences.getLocalizedString(ImportedFurnitureWizardController.class, 
          "undoImportFurnitureName");
    }
  }

  /**
   * Returns the content manager of this controller.
   */
  public ContentManager getContentManager() {
    return this.contentManager;
  }

  /**
   * Returns the current step state.
   */
  @Override
  protected ImportedFurnitureWizardStepState getStepState() {
    return (ImportedFurnitureWizardStepState)super.getStepState();
  }
  
  /**
   * Returns the furniture choice step state.
   */
  protected ImportedFurnitureWizardStepState getFurnitureModelStepStatee() {
    return this.furnitureModelStepState;
  }

  /**
   * Returns the furniture orientation step state.
   */
  protected ImportedFurnitureWizardStepState getFurnitureOrientationStepState() {
    return this.furnitureOrientationStepState;
  }

  /**
   * Returns the furniture attributes step state.
   */
  protected ImportedFurnitureWizardStepState getFurnitureAttributesStepState() {
    return this.furnitureAttributesStepState;
  }
 
  /**
   * Returns the furniture icon step state.
   */
  protected ImportedFurnitureWizardStepState getFurnitureIconStepState() {
    return this.furnitureIconStepState;
  }
 
  /**
   * Returns the unique wizard view used for all steps.
   */
  protected ImportedFurnitureWizardStepsView getStepsView() {
    // Create view lazily only once it's needed
    if (this.stepsView == null) {
      this.stepsView = this.viewFactory.createImportedFurnitureWizardStepsView(
          this.piece, this.modelName, this.home != null, this.preferences, this);
    }
    return this.stepsView;
  }

  /**
   * Switch in the wizard view to the given <code>step</code>.
   */
  protected void setStep(Step step) {
    if (step != this.step) {
      Step oldStep = this.step;
      this.step = step;
      this.propertyChangeSupport.firePropertyChange(Property.STEP.name(), oldStep, step);
    }
  }
  
  /**
   * Returns the current step in wizard view.
   */
  public Step getStep() {
    return this.step;
  }

  /**
   * Adds the property change <code>listener</code> in parameter to this home.
   */
  public void addPropertyChangeListener(Property property, PropertyChangeListener listener) {
    this.propertyChangeSupport.addPropertyChangeListener(property.name(), listener);
  }

  /**
   * Removes the property change <code>listener</code> in parameter from this home.
   */
  public void removePropertyChangeListener(Property property, PropertyChangeListener listener) {
    this.propertyChangeSupport.removePropertyChangeListener(property.name(), listener);
  }

  /**
   * Returns the model content of the imported piece.
   */
  public Content getModel() {
    return this.model;
  }
  
  /**
   * Sets the model content of the imported piece.
   */
  public void setModel(Content model) {
    if (model != this.model) {
      Content oldModel = this.model;
      this.model = model;
      this.propertyChangeSupport.firePropertyChange(Property.MODEL.name(), oldModel, model);
    }
  }
  
  /**
   * Returns <code>true</code> if imported piece back face should be shown.
   */
  public boolean isBackFaceShown() {
    return this.backFaceShown;
  }

  /**
   * Sets whether imported piece back face should be shown.
   */
  public void setBackFaceShown(boolean backFaceShown) {
    if (backFaceShown != this.backFaceShown) {
      this.backFaceShown = backFaceShown;
      this.propertyChangeSupport.firePropertyChange(Property.BACK_FACE_SHOWN.name(), !backFaceShown, backFaceShown);
    }
  }

  /**
   * Returns the pitch angle of the imported piece model.
   */
  public float [][] getModelRotation() {
    return this.modelRotation;
  }

  /**
   * Sets the orientation pitch angle of the imported piece model.
   */
  public void setModelRotation(float [][] modelRotation) {
    if (modelRotation != this.modelRotation) {
      float [][] oldModelRotation = this.modelRotation;
      this.modelRotation = modelRotation;
      this.propertyChangeSupport.firePropertyChange(Property.MODEL_ROTATION.name(), oldModelRotation, modelRotation);
    }
  }

  /**
   * Returns the name of the imported piece.
   */
  public String getName() {
    return this.name;
  }
  
  /**
   * Sets the name of the imported piece.
   */
  public void setName(String name) {
    if (name != this.name
        || (name != null && !name.equals(this.name))) {
      String oldName = this.name;
      this.name = name;
      if (this.propertyChangeSupport != null) {
        this.propertyChangeSupport.firePropertyChange(Property.NAME.name(), oldName, name);
      }
    }
  }
  
  /**
   * Returns the width.
   */
  public float getWidth() {
    return this.width;
  }
  
  /**
   * Sets the width of the imported piece.
   */
  public void setWidth(float width) {
    if (width != this.width) {
      float oldWidth = this.width;
      this.width = width;
      this.propertyChangeSupport.firePropertyChange(Property.WIDTH.name(), oldWidth, width);
    }
  }

  /**
   * Returns the depth of the imported piece.
   */
  public float getDepth() {
    return this.depth;
  }

  /**
   * Sets the depth of the imported piece.
   */
  public void setDepth(float depth) {
    if (depth != this.depth) {
      float oldDepth = this.depth;
      this.depth = depth;
      this.propertyChangeSupport.firePropertyChange(Property.DEPTH.name(), oldDepth, depth);
    }
  }

  /**
   * Returns the height.
   */
  public float getHeight() {
    return this.height;
  }

  /**
   * Sets the size of the imported piece.
   */
  public void setHeight(float height) {
    if (height != this.height) {
      float oldHeight = this.height;
      this.height = height;
      this.propertyChangeSupport.firePropertyChange(Property.HEIGHT.name(), oldHeight, height);
    }
  }

  /**
   * Returns the elevation of the imported piece.
   */
  public float getElevation() {
    return this.elevation;
  }
  
  /**
   * Sets the elevation of the imported piece.
   */
  public void setElevation(float elevation) {
    if (elevation != this.elevation) {
      float oldElevation = this.elevation;
      this.elevation = elevation;
      this.propertyChangeSupport.firePropertyChange(Property.ELEVATION.name(), oldElevation, elevation);
    }
  }
  
  /**
   * Returns <code>true</code> if imported piece is movable.
   */
  public boolean isMovable() {
    return this.movable;
  }

  /**
   * Sets whether imported piece is movable.
   */
  public void setMovable(boolean movable) {
    if (movable != this.movable) {
      this.movable = movable;
      this.propertyChangeSupport.firePropertyChange(Property.MOVABLE.name(), !movable, movable);
    }
  }

  /**
   * Returns <code>true</code> if imported piece is a door or a window.
   */
  public boolean isDoorOrWindow() {
    return this.doorOrWindow;
  }

  /**
   * Sets whether imported piece is a door or a window.
   */
  public void setDoorOrWindow(boolean doorOrWindow) {
    if (doorOrWindow != this.doorOrWindow) {
      this.doorOrWindow = doorOrWindow;
      this.propertyChangeSupport.firePropertyChange(Property.DOOR_OR_WINDOW.name(), !doorOrWindow, doorOrWindow);
      if (doorOrWindow) {
        setMovable(false);
      }
    }
  }

  /**
   * Returns the color of the imported piece.
   */
  public Integer getColor() {
    return this.color;
  }
  
  /**
   * Sets the color of the imported piece.
   */
  public void setColor(Integer color) {
    if (color != this.color) {
      Integer oldColor = this.color;
      this.color = color;
      this.propertyChangeSupport.firePropertyChange(Property.COLOR.name(), oldColor, color);
    }
  }
  
  /**
   * Returns the category of the imported piece.
   */
  public FurnitureCategory getCategory() {
    return this.category;
  }
  
  /**
   * Sets the category of the imported piece.
   */
  public void setCategory(FurnitureCategory category) {
    if (category != this.category) {
      FurnitureCategory oldCategory = this.category;
      this.category = category;
      this.propertyChangeSupport.firePropertyChange(Property.CATEGORY.name(), oldCategory, category);
    }
  }
  
  /**
   * Returns the icon of the imported piece.
   */
  private Content getIcon() {
    return getStepsView().getIcon();
  }
  
  /**
   * Returns the yaw of the piece icon.
   */
  public float getIconYaw() {
    return this.iconYaw;
  }
  
  /**
   * Sets the yaw angle of the piece icon.
   */
  public void setIconYaw(float iconYaw) {
    if (iconYaw != this.iconYaw) {
      float oldIconYaw = this.iconYaw;
      this.iconYaw = iconYaw;
      this.propertyChangeSupport.firePropertyChange(Property.ICON_YAW.name(), oldIconYaw, iconYaw);
    }
  }
  
  /**
   * Returns <code>true</code> if piece proportions should be kept.
   */
  public boolean isProportional() {
    return this.proportional;
  }
  
  /**
   * Sets whether piece proportions should be kept or not.
   */
  public void setProportional(boolean proportional) {
    if (proportional != this.proportional) {
      this.proportional = proportional;
      this.propertyChangeSupport.firePropertyChange(Property.PROPORTIONAL.name(), !proportional, proportional);
    }
  }

  /**
   * Returns <code>true</code> if piece name is valid.
   */
  public boolean isPieceOfFurnitureNameValid() {
    if (this.category == null) {
      return true;
    }
    CatalogPieceOfFurniture temporaryPiece = 
        new CatalogPieceOfFurniture(this.name, null, null, 0, 0, 0, 0, false, null, null, false, 0, false);
    if (this.piece != null
        && this.category == this.piece.getCategory()
        // Check piece names are equal with binary search to keep locale dependence
        && Collections.binarySearch(this.category.getFurniture(), this.piece)
              == Collections.binarySearch(this.category.getFurniture(), temporaryPiece)) {
      // Accept piece name if it didn't change 
      return true;
    }
    return this.name != null
            && this.name.length() > 0
            && Collections.binarySearch(this.category.getFurniture(), temporaryPiece) < 0;
  }

  /**
   * Step state superclass. All step state share the same step view,
   * that will display a different component depending on their class name. 
   */
  protected abstract class ImportedFurnitureWizardStepState extends WizardControllerStepState {
    private URL icon = ImportedFurnitureWizardController.class.getResource("resources/importedFurnitureWizard.png");
    
    public abstract Step getStep();

    @Override
    public void enter() {
      setStep(getStep());
    }
    
    @Override
    public View getView() {
      return getStepsView();
    }    
    
    @Override
    public URL getIcon() {
      return this.icon;
    }
  }
    
  /**
   * Furniture model step state (first step).
   */
  private class FurnitureModelStepState extends ImportedFurnitureWizardStepState {
    public FurnitureModelStepState() {
      ImportedFurnitureWizardController.this.addPropertyChangeListener(Property.MODEL, 
          new PropertyChangeListener() {
              public void propertyChange(PropertyChangeEvent evt) {
                setNextStepEnabled(getModel() != null);
              }
            });
    }

    @Override
    public void enter() {
      super.enter();
      setFirstStep(true);
      // First step is valid once a model is available
      setNextStepEnabled(getModel() != null);
    }

    @Override
    public Step getStep() {
      return Step.MODEL;
    }
    
    @Override
    public void goToNextStep() {
      setStepState(getFurnitureOrientationStepState());
    }
  }

  /**
   * Furniture orientation step state (second step).
   */
  private class FurnitureOrientationStepState extends ImportedFurnitureWizardStepState {
    @Override
    public void enter() {
      super.enter();
      // Step always valid by default
      setNextStepEnabled(true);
    }

    @Override
    public Step getStep() {
      return Step.ROTATION;
    }
    
    @Override
    public void goBackToPreviousStep() {
      setStepState(getFurnitureModelStepStatee());
    }
    
    @Override
    public void goToNextStep() {
      setStepState(getFurnitureAttributesStepState());
    }
  }

  /**
   * Furniture attributes step state (third step).
   */
  private class FurnitureAttributesStepState extends ImportedFurnitureWizardStepState {
    PropertyChangeListener widthChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          if (isProportional()) {
            ImportedFurnitureWizardController.this.removePropertyChangeListener(Property.DEPTH, depthChangeListener);
            ImportedFurnitureWizardController.this.removePropertyChangeListener(Property.HEIGHT, heightChangeListener);
            
            // If proportions should be kept, update depth and height
            float ratio = (Float)ev.getNewValue() / (Float)ev.getOldValue();
            setDepth(getDepth() * ratio); 
            setHeight(getHeight() * ratio);
            
            ImportedFurnitureWizardController.this.addPropertyChangeListener(Property.DEPTH, depthChangeListener);
            ImportedFurnitureWizardController.this.addPropertyChangeListener(Property.HEIGHT, heightChangeListener);
          }
        }
      };
    PropertyChangeListener depthChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          if (isProportional()) {
            ImportedFurnitureWizardController.this.removePropertyChangeListener(Property.WIDTH, widthChangeListener);
            ImportedFurnitureWizardController.this.removePropertyChangeListener(Property.HEIGHT, heightChangeListener);
            
            // If proportions should be kept, update width and height
            float ratio = (Float)ev.getNewValue() / (Float)ev.getOldValue();
            setWidth(getWidth() * ratio); 
            setHeight(getHeight() * ratio);
            
            ImportedFurnitureWizardController.this.addPropertyChangeListener(Property.WIDTH, widthChangeListener);
            ImportedFurnitureWizardController.this.addPropertyChangeListener(Property.HEIGHT, heightChangeListener);
          }
        }
      };
    PropertyChangeListener heightChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          if (isProportional()) {
            ImportedFurnitureWizardController.this.removePropertyChangeListener(Property.WIDTH, widthChangeListener);
            ImportedFurnitureWizardController.this.removePropertyChangeListener(Property.DEPTH, depthChangeListener);
            
            // If proportions should be kept, update width and depth
            float ratio = (Float)ev.getNewValue() / (Float)ev.getOldValue();
            setWidth(getWidth() * ratio); 
            setDepth(getDepth() * ratio);
            
            ImportedFurnitureWizardController.this.addPropertyChangeListener(Property.WIDTH, widthChangeListener);
            ImportedFurnitureWizardController.this.addPropertyChangeListener(Property.DEPTH, depthChangeListener);
          }
        }
      };
    PropertyChangeListener nameAndCategoryChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          checkPieceOfFurnitureNameInCategory();
        }
      };
    
    @Override
    public void enter() {
      super.enter();
      ImportedFurnitureWizardController.this.addPropertyChangeListener(Property.WIDTH, this.widthChangeListener);
      ImportedFurnitureWizardController.this.addPropertyChangeListener(Property.DEPTH, this.depthChangeListener);
      ImportedFurnitureWizardController.this.addPropertyChangeListener(Property.HEIGHT, this.heightChangeListener);
      ImportedFurnitureWizardController.this.addPropertyChangeListener(Property.NAME, this.nameAndCategoryChangeListener);
      ImportedFurnitureWizardController.this.addPropertyChangeListener(Property.CATEGORY, this.nameAndCategoryChangeListener);
      checkPieceOfFurnitureNameInCategory();
    }

    private void checkPieceOfFurnitureNameInCategory() {      
      setNextStepEnabled(isPieceOfFurnitureNameValid());
    }

    @Override
    public Step getStep() {
      return Step.ATTRIBUTES;
    }
    
    @Override
    public void goBackToPreviousStep() {
      setStepState(getFurnitureOrientationStepState());
    }

    @Override
    public void goToNextStep() {
      setStepState(getFurnitureIconStepState());
    }
    
    @Override
    public void exit() {
      ImportedFurnitureWizardController.this.removePropertyChangeListener(Property.WIDTH, this.widthChangeListener);
      ImportedFurnitureWizardController.this.removePropertyChangeListener(Property.DEPTH, this.depthChangeListener);
      ImportedFurnitureWizardController.this.removePropertyChangeListener(Property.HEIGHT, this.heightChangeListener);
      ImportedFurnitureWizardController.this.removePropertyChangeListener(Property.NAME, this.nameAndCategoryChangeListener);
      ImportedFurnitureWizardController.this.removePropertyChangeListener(Property.CATEGORY, this.nameAndCategoryChangeListener);
    }
  }

  /**
   * Furniture icon step state (last step).
   */
  private class FurnitureIconStepState extends ImportedFurnitureWizardStepState {
    @Override
    public void enter() {
      super.enter();
      setLastStep(true);
      // Step always valid by default
      setNextStepEnabled(true);
    }
    
    @Override
    public Step getStep() {
      return Step.ICON;
    }
    
    @Override
    public void goBackToPreviousStep() {
      setStepState(getFurnitureAttributesStepState());
    }
  }
}
