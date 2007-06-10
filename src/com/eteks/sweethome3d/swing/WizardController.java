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

import java.net.URL;

import javax.swing.JComponent;

/**
 * An abstract MVC for a wizard view. Subclasses should create a set of wizard steps
 * with subclasses of {@link WizardControllerStepState WizardControllerStepState} and
 * and choose the first step with a call to <code>setStepState</code>.
 * The {@link #finish() finish} method will be called if user completes the wizard
 * steps correctly.
 * @author Emmanuel Puybaret
 */
public abstract class WizardController {
  private JComponent  wizardView;
  // Current step state
  private WizardControllerStepState stepState;

  public WizardController() {
    this.wizardView = new WizardPane(this);
  }
  
  /**
   * Returns the view associated with this controller.
   */
  public JComponent getView() {
    return this.wizardView;
  }
  
  /**
   * Displays wizard view. 
   */
  protected void displayView() {
    ((WizardPane)getView()).displayView();
  }

  /**
   * Changes current state of controller.
   */
  protected void setStepState(WizardControllerStepState stepState) {
    if (this.stepState != null) {
      this.stepState.exit();
    } 
    this.stepState = stepState;
    
    WizardPane wizardView = (WizardPane)getView();
    wizardView.setBackStepEnabled(!stepState.isFirstStep());
    wizardView.setNextStepEnabled(false);
    wizardView.setStepMessage(stepState.getView());
    wizardView.setStepIcon(stepState.getIcon());
    wizardView.setLastStep(stepState.isLastStep());

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
   * Enables the next step button in view. 
   */
  protected void setNextStepEnabled(boolean enabled) {
    ((WizardPane)getView()).setNextStepEnabled(enabled);
  }  
  
  /**
   * Set the title of the view. 
   */
  protected void setTitle(String title) {
    ((WizardPane)getView()).setTitle(title);
  }  
  
  /**
   * State of a step in wizard. 
   */
  protected static abstract class WizardControllerStepState {
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
      return false;
    }
    
    public boolean isLastStep() {
      return false;
    }    
  }
}
