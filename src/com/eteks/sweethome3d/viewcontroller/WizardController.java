/*
 * WizardController.java 7 juin 07
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
package com.eteks.sweethome3d.viewcontroller;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;

/**
 * An abstract MVC for a wizard view. Subclasses should create a set of wizard steps
 * with subclasses of <code>WizardControllerStepState</code> and
 * and choose the first step with a call to <code>setStepState</code>.
 * The {@link #finish() finish} method will be called if user completes the wizard
 * steps correctly.
 * @author Emmanuel Puybaret
 */
public abstract class WizardController implements Controller {
  /**
   * The properties that the view associated to this controller needs. 
   */
  public enum Property {BACK_STEP_ENABLED, NEXT_STEP_ENABLED, LAST_STEP, 
      STEP_VIEW, STEP_ICON, TITLE, RESIZABLE}
  
  private final ViewFactory            viewFactory;
  private final PropertyChangeSupport  propertyChangeSupport;
  private final PropertyChangeListener stepStatePropertyChangeListener;
  
  private DialogView                   wizardView;
  // Current step state
  private WizardControllerStepState    stepState;

  private boolean backStepEnabled;
  private boolean nextStepEnabled;
  private boolean lastStep;
  private View    stepView;
  private URL     stepIcon;
  private String  title;
  private boolean resizable;

  
  public WizardController(ViewFactory viewFactory) {
    this.viewFactory = viewFactory;
    // Create a listener used to track changes in current step state
    this.stepStatePropertyChangeListener = new PropertyChangeListener () {
        public void propertyChange(PropertyChangeEvent ev) {
          switch (WizardControllerStepState.Property.valueOf(ev.getPropertyName())) {
            case FIRST_STEP :
              setBackStepEnabled(!stepState.isFirstStep());
              break;
            case LAST_STEP :
              setLastStep(stepState.isLastStep());
              break;
            case NEXT_STEP_ENABLED :
              setNextStepEnabled(stepState.isNextStepEnabled());
              break;
          }
        }
      };
      
    this.propertyChangeSupport = new PropertyChangeSupport(this);
  }
  
  /**
   * Returns the view associated with this controller.
   */
  public DialogView getView() {
    // Create view lazily only once it's needed
    if (this.wizardView == null) {
      this.wizardView = this.viewFactory.createWizardView(this);
    }
    return this.wizardView;
  }
  
  /**
   * Displays the view controlled by this controller. 
   */
  public void displayView(View parentView) {
    getView().displayView(parentView);
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
   * Sets whether back step is enabled or not.
   */
  private void setBackStepEnabled(boolean backStepEnabled) {
    if (backStepEnabled != this.backStepEnabled) {
      this.backStepEnabled = backStepEnabled;
      this.propertyChangeSupport.firePropertyChange(Property.BACK_STEP_ENABLED.name(), 
          !backStepEnabled, backStepEnabled);
    }
  }
  
  /**
   * Returns whether back step is enabled or not.
   */
  public boolean isBackStepEnabled() {
    return this.backStepEnabled;
  }
  
  /**
   * Sets whether next step is enabled or not.
   */
  private void setNextStepEnabled(boolean nextStepEnabled) {
    if (nextStepEnabled != this.nextStepEnabled) {
      this.nextStepEnabled = nextStepEnabled;
      this.propertyChangeSupport.firePropertyChange(Property.NEXT_STEP_ENABLED.name(), 
          !nextStepEnabled, nextStepEnabled);
    }
  }
  
  /**
   * Returns whether next step is enabled or not.
   */
  public boolean isNextStepEnabled() {
    return this.nextStepEnabled;
  }
  
  /**
   * Sets whether this is the last step or not.
   */
  private void setLastStep(boolean lastStep) {
    if (lastStep != this.lastStep) {
      this.lastStep = lastStep;
      this.propertyChangeSupport.firePropertyChange(Property.LAST_STEP.name(), !lastStep, lastStep);
    }
  }
  
  /**
   * Returns whether this is the last step or not.
   */
  public boolean isLastStep() {
    return this.lastStep;
  }
  
  /**
   * Sets the step view.
   */
  private void setStepView(View stepView) {
    if (stepView != this.stepView) {
      View oldStepView = this.stepView;
      this.stepView = stepView;
      this.propertyChangeSupport.firePropertyChange(Property.STEP_VIEW.name(), oldStepView, stepView);
    }
  }
  
  /**
   * Returns the current step view.
   */
  public View getStepView() {
    return this.stepView;
  }
  
  /**
   * Sets the step icon.
   */
  private void setStepIcon(URL stepIcon) {
    if (stepIcon != this.stepIcon) {
      URL oldStepIcon = this.stepIcon;
      this.stepIcon = stepIcon;
      this.propertyChangeSupport.firePropertyChange(Property.STEP_ICON.name(), oldStepIcon, stepIcon);
    }
  }
  
  /**
   * Returns the current step icon.
   */
  public URL getStepIcon() {
    return this.stepIcon;
  }
  
  /**
   * Sets the wizard title.
   */
  public void setTitle(String title) {
    if (title != this.title) {
      String oldTitle = this.title;
      this.title = title;
      this.propertyChangeSupport.firePropertyChange(Property.TITLE.name(), oldTitle, title);
    }
  }
  
  /**
   * Returns the wizard title.
   */
  public String getTitle() {
    return this.title;
  }
  
  /**
   * Sets whether the wizard is resizable or not.
   */
  public void setResizable(boolean resizable) {
    if (resizable != this.resizable) {
      this.resizable = resizable;
      this.propertyChangeSupport.firePropertyChange(Property.RESIZABLE.name(), !resizable, resizable);
    }
  }
  
  /**
   * Returns whether the wizard is resizable or not.
   */
  public boolean isResizable() {
    return this.resizable;
  }
  
  /**
   * Changes current state of controller.
   */
  protected void setStepState(WizardControllerStepState stepState) {
    if (this.stepState != null) {
      this.stepState.exit();
      this.stepState.removePropertyChangeListener(this.stepStatePropertyChangeListener);
    } 
    this.stepState = stepState;
    
    setBackStepEnabled(!stepState.isFirstStep());
    setNextStepEnabled(stepState.isNextStepEnabled());
    setStepView(stepState.getView());
    setStepIcon(stepState.getIcon());
    setLastStep(stepState.isLastStep());
    
    this.stepState.addPropertyChangeListener(this.stepStatePropertyChangeListener);
    this.stepState.enter();
  }
  
  protected WizardControllerStepState getStepState() {
    return this.stepState;
  }
  
  /**
   * Requires to the current step to jump to next step. 
   */
  public void goToNextStep() {
    this.stepState.goToNextStep();
  }
  
  /**
   * Requires to the current step to go back to previous step. 
   */
  public void goBackToPreviousStep() {
    this.stepState.goBackToPreviousStep();
  }

  /**
   * Requires the wizard to finish. 
   */
  public abstract void finish();
  
  /**
   * State of a step in wizard. 
   */
  protected static abstract class WizardControllerStepState {
    private enum Property {NEXT_STEP_ENABLED, FIRST_STEP, LAST_STEP}
    
    private PropertyChangeSupport propertyChangeSupport;
    private boolean               firstStep;
    private boolean               lastStep;
    private boolean               nextStepEnabled;

    public WizardControllerStepState() {
      this.propertyChangeSupport = new PropertyChangeSupport(this);
    }
    
    /**
     * Adds the property change <code>listener</code> in parameter to this home.
     */
    private void addPropertyChangeListener(PropertyChangeListener listener) {
      this.propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Removes the property change <code>listener</code> in parameter from this home.
     */
    private void removePropertyChangeListener(PropertyChangeListener listener) {
      this.propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public void enter() {
    }

    public void exit() {
    }

    public abstract View getView();
    
    public URL getIcon() {
      return null;
    }
    
    public void goBackToPreviousStep() {
    }

    public void goToNextStep() {
    }
    
    public boolean isFirstStep() {
      return this.firstStep;
    }
    
    public void setFirstStep(boolean firstStep) {
      if (firstStep != this.firstStep) {
        this.firstStep = firstStep;
        this.propertyChangeSupport.firePropertyChange(
            Property.FIRST_STEP.name(), !firstStep, firstStep);
      }
    }  
    
    public boolean isLastStep() {
      return this.lastStep;
    }   
    
    public void setLastStep(boolean lastStep) {
      if (lastStep != this.lastStep) {
        this.lastStep = lastStep;
        this.propertyChangeSupport.firePropertyChange(
            Property.LAST_STEP.name(), !lastStep, lastStep);
      }
    }  
    
    public boolean isNextStepEnabled() {
      return this.nextStepEnabled;
    }
    
    public void setNextStepEnabled(boolean nextStepEnabled) {
      if (nextStepEnabled != this.nextStepEnabled) {
        this.nextStepEnabled = nextStepEnabled;
        this.propertyChangeSupport.firePropertyChange(
            Property.NEXT_STEP_ENABLED.name(), !nextStepEnabled, nextStepEnabled);
      }
    }  
  }
}
