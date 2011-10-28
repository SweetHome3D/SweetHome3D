/*
 * BackgroundImageWizardController.java 8 juin 07
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

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.BackgroundImage;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.Level;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * Wizard controller for background image in plan.
 * @author Emmanuel Puybaret
 */
public class BackgroundImageWizardController extends WizardController 
                                             implements Controller {
  public enum Property {STEP, IMAGE, SCALE_DISTANCE, SCALE_DISTANCE_POINTS, X_ORIGIN, Y_ORIGIN}

  public enum Step {CHOICE, SCALE, ORIGIN};
  
  private final Home                           home;
  private final UserPreferences                preferences;
  private final ViewFactory                    viewFactory;
  private final ContentManager                 contentManager;
  private final UndoableEditSupport            undoSupport;
  private final PropertyChangeSupport          propertyChangeSupport;

  private final BackgroundImageWizardStepState imageChoiceStepState;
  private final BackgroundImageWizardStepState imageScaleStepState;
  private final BackgroundImageWizardStepState imageOriginStepState;
  private View                                 stepsView;
  
  private Step    step;
  private Content image;
  private Float   scaleDistance;
  private float   scaleDistanceXStart;
  private float   scaleDistanceYStart;
  private float   scaleDistanceXEnd;
  private float   scaleDistanceYEnd;
  private float   xOrigin;
  private float   yOrigin;
  
  public BackgroundImageWizardController(Home home, 
                                         UserPreferences preferences,
                                         ViewFactory viewFactory,
                                         ContentManager contentManager,
                                         UndoableEditSupport undoSupport) {
    super(preferences, viewFactory);
    this.home = home;
    this.preferences = preferences;
    this.viewFactory = viewFactory;
    this.contentManager = contentManager;
    this.undoSupport = undoSupport;
    this.propertyChangeSupport = new PropertyChangeSupport(this);
    setTitle(preferences.getLocalizedString(BackgroundImageWizardController.class, "wizard.title"));    
    setResizable(true);
    // Initialize states
    this.imageChoiceStepState = new ImageChoiceStepState();
    this.imageScaleStepState = new ImageScaleStepState();
    this.imageOriginStepState = new ImageOriginStepState();
    setStepState(this.imageChoiceStepState);
  }

  /**
   * Changes background image in model and posts an undoable operation.
   */
  @Override
  public void finish() {
    Level selectedLevel = this.home.getSelectedLevel();
    BackgroundImage oldImage = selectedLevel != null
        ? selectedLevel.getBackgroundImage()
        : this.home.getBackgroundImage();
    float [][] scaleDistancePoints = getScaleDistancePoints();
    BackgroundImage image = new BackgroundImage(getImage(),
        getScaleDistance(), scaleDistancePoints [0][0], scaleDistancePoints [0][1],
        scaleDistancePoints [1][0], scaleDistancePoints [1][1], 
        getXOrigin(), getYOrigin());
    if (selectedLevel != null) {
      selectedLevel.setBackgroundImage(image);
    } else {
      this.home.setBackgroundImage(image);
    }
    boolean modification = oldImage == null;
    UndoableEdit undoableEdit = 
        new BackgroundImageUndoableEdit(this.home, selectedLevel, 
            this.preferences, modification, oldImage, image);
    this.undoSupport.postEdit(undoableEdit);
  }

  /**
   * Undoable edit for background image. This class isn't anonymous to avoid
   * being bound to controller and its view.
   */
  private static class BackgroundImageUndoableEdit extends AbstractUndoableEdit {
    private final Home            home;
    private final Level           level;
    private final UserPreferences preferences;
    private final boolean         modification;
    private final BackgroundImage oldImage;
    private final BackgroundImage image;

    private BackgroundImageUndoableEdit(Home home,
                                        Level level, 
                                        UserPreferences preferences,
                                        boolean modification,
                                        BackgroundImage oldImage,
                                        BackgroundImage image) {
      this.home = home;
      this.level = level;
      this.preferences = preferences;
      this.modification = modification;
      this.oldImage = oldImage;
      this.image = image;
    }

    @Override
    public void undo() throws CannotUndoException {
      super.undo();
      this.home.setSelectedLevel(this.level);
      if (this.level != null) {
        this.level.setBackgroundImage(this.oldImage);
      } else {
        this.home.setBackgroundImage(this.oldImage);
      } 
    }

    @Override
    public void redo() throws CannotRedoException {
      super.redo();
      this.home.setSelectedLevel(this.level);
      if (this.level != null) {
        this.level.setBackgroundImage(this.image);
      } else {
        this.home.setBackgroundImage(this.image);
      } 
    }

    @Override
    public String getPresentationName() {
      return this.preferences.getLocalizedString(BackgroundImageWizardController.class,
          this.modification 
              ? "undoImportBackgroundImageName"
              : "undoModifyBackgroundImageName");
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
  protected BackgroundImageWizardStepState getStepState() {
    return (BackgroundImageWizardStepState)super.getStepState();
  }
  
  /**
   * Returns the image choice step state.
   */
  protected BackgroundImageWizardStepState getImageChoiceStepState() {
    return this.imageChoiceStepState;
  }

  /**
   * Returns the image origin step state.
   */
  protected BackgroundImageWizardStepState getImageOriginStepState() {
    return this.imageOriginStepState;
  }

  /**
   * Returns the image scale step state.
   */
  protected BackgroundImageWizardStepState getImageScaleStepState() {
    return this.imageScaleStepState;
  }
 
  /**
   * Returns the unique wizard view used for all steps.
   */
  protected View getStepsView() {
    // Create view lazily only once it's needed
    if (this.stepsView == null) {
      BackgroundImage image = this.home.getSelectedLevel() != null
          ? this.home.getSelectedLevel().getBackgroundImage()
          : this.home.getBackgroundImage();
      this.stepsView = this.viewFactory.createBackgroundImageWizardStepsView(
          image, this.preferences, this);
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
   * Adds the property change <code>listener</code> in parameter to this controller.
   */
  public void addPropertyChangeListener(Property property, PropertyChangeListener listener) {
    this.propertyChangeSupport.addPropertyChangeListener(property.name(), listener);
  }

  /**
   * Removes the property change <code>listener</code> in parameter from this controller.
   */
  public void removePropertyChangeListener(Property property, PropertyChangeListener listener) {
    this.propertyChangeSupport.removePropertyChangeListener(property.name(), listener);
  }

  /**
   * Sets the image content of the background image.
   */
  public void setImage(Content image) {
    if (image != this.image) {
      Content oldImage = this.image;
      this.image = image;
      this.propertyChangeSupport.firePropertyChange(Property.IMAGE.name(), oldImage, image);
    }
  }
  
  /**
   * Returns the image content of the background image.
   */
  public Content getImage() {
    return this.image;
  }

  /**
   * Sets the scale distance of the background image.
   */
  public void setScaleDistance(Float scaleDistance) {
    if (scaleDistance != this.scaleDistance) {
      Float oldScaleDistance = this.scaleDistance;
      this.scaleDistance = scaleDistance;
      this.propertyChangeSupport.firePropertyChange(
          Property.SCALE_DISTANCE.name(), oldScaleDistance, scaleDistance);
    }
  }
  
  /**
   * Returns the scale distance of the background image.
   */
  public Float getScaleDistance() {
    return this.scaleDistance;
  }

  /**
   * Sets the coordinates of the scale distance points of the background image.
   */
  public void setScaleDistancePoints(float scaleDistanceXStart, float scaleDistanceYStart, 
                                     float scaleDistanceXEnd, float scaleDistanceYEnd) {
    if (scaleDistanceXStart != this.scaleDistanceXStart
        || scaleDistanceYStart != this.scaleDistanceYStart
        || scaleDistanceXEnd != this.scaleDistanceXEnd
        || scaleDistanceYEnd != this.scaleDistanceYEnd) {
      float [][] oldDistancePoints = new float [][] {{this.scaleDistanceXStart, this.scaleDistanceYStart},
                                                     {this.scaleDistanceXEnd, this.scaleDistanceYEnd}};
      this.scaleDistanceXStart = scaleDistanceXStart;
      this.scaleDistanceYStart = scaleDistanceYStart;
      this.scaleDistanceXEnd = scaleDistanceXEnd;
      this.scaleDistanceYEnd = scaleDistanceYEnd;
      this.propertyChangeSupport.firePropertyChange(
          Property.SCALE_DISTANCE.name(), oldDistancePoints, 
          new float [][] {{scaleDistanceXStart, scaleDistanceYStart},
                          {scaleDistanceXEnd, scaleDistanceYEnd}});
    }
  }
  
  /**
   * Returns the coordinates of the scale distance points of the background image.
   */
  public float [][] getScaleDistancePoints() {
    return new float [][] {{this.scaleDistanceXStart, this.scaleDistanceYStart},
                           {this.scaleDistanceXEnd, this.scaleDistanceYEnd}};
  }
  
  /**
   * Sets the origin of the background image.
   */
  public void setOrigin(float xOrigin, float yOrigin) {
    if (xOrigin != this.xOrigin) {
      Float oldXOrigin = this.xOrigin;
      this.xOrigin = xOrigin;
      this.propertyChangeSupport.firePropertyChange(
          Property.X_ORIGIN.name(), oldXOrigin, xOrigin);
    }
    if (yOrigin != this.yOrigin) {
      Float oldYOrigin = this.yOrigin;
      this.yOrigin = yOrigin;
      this.propertyChangeSupport.firePropertyChange(
          Property.Y_ORIGIN.name(), oldYOrigin, yOrigin);
    }
  }

  /**
   * Returns the abcissa of the origin of the background image.
   */
  public float getXOrigin() {
    return this.xOrigin;
  }

  /**
   * Returns the ordinate of the origin of the background image.
   */
  public float getYOrigin() {
    return this.yOrigin;
  }
  
  /**
   * Step state superclass. All step state share the same step view,
   * that will display a different component depending on their class name. 
   */
  protected abstract class BackgroundImageWizardStepState extends WizardControllerStepState {
    private URL icon = BackgroundImageWizardController.class.getResource("resources/backgroundImageWizard.png");
    
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
   * Image choice step state (first step).
   */
  private class ImageChoiceStepState extends BackgroundImageWizardStepState {
    public ImageChoiceStepState() {
      BackgroundImageWizardController.this.addPropertyChangeListener(Property.IMAGE, 
          new PropertyChangeListener() {
              public void propertyChange(PropertyChangeEvent evt) {
                setNextStepEnabled(getImage() != null);
              }
            });
    }
    
    @Override
    public void enter() {
      super.enter();
      setFirstStep(true);
      setNextStepEnabled(getImage() != null);
    }
    
    @Override
    public Step getStep() {
      return Step.CHOICE;
    }
    
    @Override
    public void goToNextStep() {
      setStepState(getImageScaleStepState());
    }
  }

  /**
   * Image scale step state (second step).
   */
  private class ImageScaleStepState extends BackgroundImageWizardStepState {
    public ImageScaleStepState() {
      BackgroundImageWizardController.this.addPropertyChangeListener(Property.SCALE_DISTANCE, 
          new PropertyChangeListener() {
              public void propertyChange(PropertyChangeEvent evt) {
                setNextStepEnabled(getScaleDistance() != null);
              }
            });
    }
    
    @Override
    public void enter() {
      super.enter();
      setNextStepEnabled(getScaleDistance() != null);
    }
    
    @Override
    public Step getStep() {
      return Step.SCALE;
    }
    
    @Override
    public void goBackToPreviousStep() {
      setStepState(getImageChoiceStepState());
    }
    
    @Override
    public void goToNextStep() {
      setStepState(getImageOriginStepState());
    }
  }

  /**
   * Image origin step state (last step).
   */
  private class ImageOriginStepState extends BackgroundImageWizardStepState {
    @Override
    public void enter() {
      super.enter();
      setLastStep(true);
      // Last step is always valid by default
      setNextStepEnabled(true);
    }
    
    @Override
    public Step getStep() {
      return Step.ORIGIN;
    }
    
    @Override
    public void goBackToPreviousStep() {
      setStepState(getImageScaleStepState());
    }
  }
}
