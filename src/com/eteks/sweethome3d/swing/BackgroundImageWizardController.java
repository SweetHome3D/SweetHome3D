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


/**
 * Wizard controller for background image in plan.
 * @author Emmanuel Puybaret
 */
public class BackgroundImageWizardController extends WizardController {
  public enum Step {CHOICE, SCALE, ORIGIN};
  
  private Home                           home;
  private UndoableEditSupport            undoSupport;
  private ResourceBundle                 resource;
  private BackgroundImageWizardStepState imageChoiceStepState;
  private BackgroundImageWizardStepState imageScaleStepState;
  private BackgroundImageWizardStepState imageOriginStepState;
  private JComponent                     stepsView;
  
  private Content                        imageContent;
  private Float                          scaleDistance;
  private float                          scaleDistanceXStart;
  private float                          scaleDistanceYStart;
  private float                          scaleDistanceXEnd;
  private float                          scaleDistanceYEnd;
  private Float                          xOrigin;
  private Float                          yOrigin;
  
  public BackgroundImageWizardController(Home home, UserPreferences preferences, 
                                         UndoableEditSupport undoSupport) {
    this.home = home;
    this.undoSupport = undoSupport;
    this.resource = ResourceBundle.getBundle(BackgroundImageWizardController.class.getName());
    this.imageChoiceStepState = new ImageChoiceStepState();
    this.imageScaleStepState = new ImageScaleStepState();
    this.imageOriginStepState = new ImageOriginStepState();
    this.stepsView = new BackgroundImageWizardStepsView(home.getBackgroundImage(), preferences, this);
    setTitle(this.resource.getString("wizard.title"));    
    setStepState(this.imageChoiceStepState);
    displayView();
  }

  /**
   * Changes background image in model and posts an undoable operation.
   */
  @Override
  public void finish() {
    final BackgroundImage oldImage = this.home.getBackgroundImage();
    final BackgroundImage image = new BackgroundImage(this.imageContent,
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
    ((BackgroundImageWizardStepsView)getStepsView()).setStep(step);
  }

  /**
   * Sets the image content of the background image.
   */
  public void setImageContent(Content imageContent) {
    this.imageContent = imageContent;
    if (getStepState() != null) {
      getStepState().updateStep();
    }
  }
  
  /**
   * Returns the image content of the background image.
   */
  public Content getImageContent() {
    return this.imageContent;
  }

  /**
   * Sets the scale distance of the background image.
   */
  public void setScaleDistance(Float scaleDistance) {
    this.scaleDistance = scaleDistance;
    if (getStepState() != null) {
      getStepState().updateStep();
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
    this.scaleDistanceXStart = scaleDistanceXStart;
    this.scaleDistanceYStart = scaleDistanceYStart;
    this.scaleDistanceXEnd = scaleDistanceXEnd;
    this.scaleDistanceYEnd = scaleDistanceYEnd;
    if (getStepState() != null) {
      getStepState().updateStep();
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
  public void setOrigin(Float xOrigin, Float yOrigin) {
    this.xOrigin = xOrigin;
    this.yOrigin = yOrigin;
    if (getStepState() != null) {
      getStepState().updateStep();
    }
  }

  /**
   * Returns the abcissa of the origin of the background image.
   */
  public Float getXOrigin() {
    return this.xOrigin;
  }

  /**
   * Returns the ordinate of the origin of the background image.
   */
  public Float getYOrigin() {
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
      updateStep();
    }
    
    @Override
    public JComponent getView() {
      return getStepsView();
    }    
    
    @Override
    public URL getIcon() {
      return this.icon;
    }

    public abstract void updateStep();
  }
    
  /**
   * Image choice step state (first step).
   */
  private class ImageChoiceStepState extends BackgroundImageWizardStepState {
    @Override
    public Step getStep() {
      return Step.CHOICE;
    }
    
    @Override
    public boolean isFirstStep() {
      return true;
    }
    
    @Override
    public void goToNextStep() {
      setStepState(getImageScaleStepState());
    }
    
    @Override
    public void updateStep() {
      setNextStepEnabled(getImageContent() != null);
    }
  }

  /**
   * Image scale step state (second step).
   */
  private class ImageScaleStepState extends BackgroundImageWizardStepState {
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
    
    @Override
    public void updateStep() {
      setNextStepEnabled(getScaleDistance() != null);
    }
  }

  /**
   * Image origin step state (last step).
   */
  private class ImageOriginStepState extends BackgroundImageWizardStepState {
    @Override
    public Step getStep() {
      return Step.ORIGIN;
    }
    
    @Override
    public boolean isLastStep() {
      return true;
    }
    
    @Override
    public void goBackToPreviousStep() {
      setStepState(getImageScaleStepState());
    }

    @Override
    public void updateStep() {
      setNextStepEnabled(getXOrigin() != null && getYOrigin() != null);
    }
  }
}
