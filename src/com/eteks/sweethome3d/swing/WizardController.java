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
package com.eteks.sweethome3d.swing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;

import javax.swing.JComponent;

/**
 * An abstract MVC for a wizard view. Subclasses should create a set of wizard steps
 * with subclasses of <code>WizardControllerStepState</code> and
 * and choose the first step with a call to <code>setStepState</code>.
 * The {@link #finish() finish} method will be called if user completes the wizard
 * steps correctly.
 * @author Emmanuel Puybaret
 */
public abstract class WizardController {
  private JComponent                wizardView;
  // Current step state
  private WizardControllerStepState stepState;
  private PropertyChangeListener    stepStatePropertyChangeListener;

  public WizardController() {
    // Create a listener used to track changes in current step state
    this.stepStatePropertyChangeListener = new PropertyChangeListener () {
        public void propertyChange(PropertyChangeEvent ev) {
          WizardPane wizardView = (WizardPane)getView();
          switch (WizardControllerStepState.Property.valueOf(ev.getPropertyName())) {
            case FIRST_STEP :
              wizardView.setBackStepEnabled(!stepState.isFirstStep());
              break;
            case LAST_STEP :
              wizardView.setLastStep(stepState.isLastStep());
              break;
            case NEXT_STEP_ENABLED :
              wizardView.setNextStepEnabled(stepState.isNextStepEnabled());
              break;
          }
        }
      };
  }
  
  /**
   * Returns the view associated with this controller.
   */
  public JComponent getView() {
    // Create view lazily only once it's needed
    if (this.wizardView == null) {
      this.wizardView = new WizardPane(this);
    }
    return this.wizardView;
  }
  
  /**
   * Displays the view controlled by this controller. 
   */
  public void displayView(JComponent parentView) {
    ((WizardPane)getView()).displayView(parentView);
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
    
    WizardPane wizardView = (WizardPane)getView();
    wizardView.setBackStepEnabled(!stepState.isFirstStep());
    wizardView.setNextStepEnabled(stepState.isNextStepEnabled());
    wizardView.setStepMessage(stepState.getView());
    wizardView.setStepIcon(stepState.getIcon());
    wizardView.setLastStep(stepState.isLastStep());
    
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
   * Set the title of the view. 
   */
  protected void setTitle(String title) {
    ((WizardPane)getView()).setTitle(title);
  }  
  
  /**
   * Sets whether the view is resizable.
   */
  protected void setResizable(boolean resizable) {
    ((WizardPane)getView()).setResizable(resizable);
  }
  
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

    public abstract JComponent getView();
    
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
            Property.FIRST_STEP.toString(), !firstStep, firstStep);
      }
    }  
    
    public boolean isLastStep() {
      return this.lastStep;
    }   
    
    public void setLastStep(boolean lastStep) {
      if (lastStep != this.lastStep) {
        this.lastStep = lastStep;
        this.propertyChangeSupport.firePropertyChange(
            Property.LAST_STEP.toString(), !lastStep, lastStep);
      }
    }  
    
    public boolean isNextStepEnabled() {
      return this.nextStepEnabled;
    }
    
    public void setNextStepEnabled(boolean nextStepEnabled) {
      if (nextStepEnabled != this.nextStepEnabled) {
        this.nextStepEnabled = nextStepEnabled;
        this.propertyChangeSupport.firePropertyChange(
            Property.NEXT_STEP_ENABLED.toString(), !nextStepEnabled, nextStepEnabled);
      }
    }  
  }
}
