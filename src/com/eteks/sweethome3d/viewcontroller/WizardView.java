/*
 * WizardView.java 28 oct. 2008
 *
 * Copyright (c) 2008 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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

import java.net.URL;

/**
 * The view that displays a wizard.
 * @author Emmanuel Puybaret
 */
public interface WizardView extends View {
  /**
   * Sets whether the back step button is <code>enabled</code> or not.
   */
  public abstract void setBackStepEnabled(boolean enabled);

  /**
   * Sets whether the next step button is <code>enabled</code> or not.
   */
  public abstract void setNextStepEnabled(boolean enabled);

  /**
   * Sets whether this wizard view is displaying the last step or not.
   */
  public abstract void setLastStep(boolean lastStep);

  /**
   * Sets the step view displayed by this wizard view.
   */
  public abstract void setStepMessage(View stepView);

  /**
   * Sets the step icon displayed by this wizard view.
   */
  public abstract void setStepIcon(URL stepIcon);

  /**
   * Sets the title of this wizard view.
   */
  public abstract void setTitle(String title);

  /**
   * Sets whether this wizard view is <code>resizable</code> or not.
   */
  public abstract void setResizable(boolean resizable);

  /**
   * Displays this wizard view in a modal dialog.
   */
  public abstract void displayView(View parentView);
}