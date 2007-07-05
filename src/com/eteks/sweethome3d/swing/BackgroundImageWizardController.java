/*
 * BackgroundImageWizardController.java 8 juin 07
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.BackgroundImage;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.swing.PlanController.Mode;
import com.eteks.sweethome3d.swing.PlanController.Property;


/**
 * Wizard controller for background image in plan.
 * @author Emmanuel Puybaret
 */
public class BackgroundImageWizardController extends WizardController {
  public enum Property {IMAGE, SCALE_DISTANCE, SCALE_DISTANCE_POINTS, X_ORIGIN, Y_ORIGIN}

  public enum Step {CHOICE, SCALE, ORIGIN};
  
  private Home                           home;
  private UndoableEditSupport            undoSupport;
  private ResourceBundle                 resource;
  private PropertyChangeSupport          propertyChangeSupport;

  private BackgroundImageWizardStepState imageChoiceStepState;
  private BackgroundImageWizardStepState imageScaleStepState;
  private BackgroundImageWizardStepState imageOriginStepState;
  private JComponent                     stepsView;
  
  private Content                        image;
  private Float                          scaleDistance;
  private float                          scaleDistanceXStart;
  private float                          scaleDistanceYStart;
  private float                          scaleDistanceXEnd;
  private float                          scaleDistanceYEnd;
  private float                          xOrigin;
  private float                          yOrigin;
  
  public BackgroundImageWizardController(Home home, UserPreferences preferences, 
                                         UndoableEditSupport undoSupport) {
    this.home = home;
    this.undoSupport = undoSupport;
    this.resource = ResourceBundle.getBundle(BackgroundImageWizardController.class.getName());
    this.propertyChangeSupport = new PropertyChangeSupport(this);
    // Create view
    this.stepsView = new BackgroundImageWizardStepsPanel(home.getBackgroundImage(), preferences, this);
    setTitle(this.resource.getString("wizard.title"));    
    // Initialize states
    this.imageChoiceStepState = new ImageChoiceStepState();
    this.imageScaleStepState = new ImageScaleStepState();
    this.imageOriginStepState = new ImageOriginStepState();
    setStepState(this.imageChoiceStepState);
    
    displayView();
  }

  /**
   * Changes background image in model and posts an undoable operation.
   */
  @Override
  public void finish() {
    final BackgroundImage oldImage = this.home.getBackgroundImage();
    final BackgroundImage image = new BackgroundImage(this.image,
        this.scaleDistance, this.scaleDistanceXStart, this.scaleDistanceYStart,
        this.scaleDistanceXEnd, this.scaleDistanceYEnd, 
        this.xOrigin, this.yOrigin);
    this.home.setBackgroundImage(image);
    UndoableEdit undoableEdit = new AbstractUndoableEdit() {
      @Override
      public void undo() throws CannotUndoException {
        super.undo();
        home.setBackgroundImage(oldImage); 
      }
      
      @Override
      public void redo() throws CannotRedoException {
        super.redo();
        home.setBackgroundImage(image);
      }
      
      @Override
      public String getPresentationName() {
        return resource.getString(oldImage == null 
            ? "undoImportBackgroundImageName"
            : "undoModifyBackgroundImageName");
      }
    };
    this.undoSupport.postEdit(undoableEdit);
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
  protected JComponent getStepsView() {
    return this.stepsView;
  }

  /**
   * Switch in the wizard view to the given <code>step</code>.
   */
  protected void setStepView(Step step) {
    ((BackgroundImageWizardStepsPanel)getStepsView()).setStep(step);
  }

  /**
   * Adds the property change <code>listener</code> in parameter to this home.
   */
  public void addPropertyChangeListener(Property property, PropertyChangeListener listener) {
    this.propertyChangeSupport.addPropertyChangeListener(property.toString(), listener);
  }

  /**
   * Removes the property change <code>listener</code> in parameter from this home.
   */
  public void removePropertyChangeListener(Property property, PropertyChangeListener listener) {
    this.propertyChangeSupport.removePropertyChangeListener(property.toString(), listener);
  }

  /**
   * Sets the image content of the background image.
   */
  public void setImage(Content image) {
    if (image != this.image) {
      Content oldImage = this.image;
      this.image = image;
      this.propertyChangeSupport.firePropertyChange(Property.IMAGE.toString(), oldImage, image);
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
          Property.SCALE_DISTANCE.toString(), oldScaleDistance, scaleDistance);
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
          Property.SCALE_DISTANCE.toString(), oldDistancePoints, 
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
          Property.X_ORIGIN.toString(), oldXOrigin, xOrigin);
    }
    if (yOrigin != this.yOrigin) {
      Float oldYOrigin = this.yOrigin;
      this.yOrigin = yOrigin;
      this.propertyChangeSupport.firePropertyChange(
          Property.Y_ORIGIN.toString(), oldYOrigin, yOrigin);
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
      setStepView(getStep());
    }
    
    @Override
    public JComponent getView() {
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
