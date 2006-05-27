/*
 * ControllerAction.java 16 mai 2006
 *
 * Copyright (c) 2006 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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

import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.JComponent;

/**
 * A resource action configured to call a controller method.
 * @author Emmanuel Puybaret
 */
public class ViewControllerAction extends ResourceAction {
  private Controller controller; 
  private Method     controllerMethod;

  /**
   * Creates a {@link ResourceAction} whose {@link #actionPerfomed} will call
   * <code>controllerMethod</code> on <code>controller</code> with the given
   * list of <code>parameters</code>.
   * @param action prefix used in resource bundle to search action properties
   * @param view view used as file base for resource bundle
   * @param controller the controller object on which will be invoked
   *          controllerMethod
   * @param controllerMethod name of the controller method that will be invoked
   *          in {@link #actionPerformed(ActionEvent) actionPerfomed}
   * @throws NoSuchMethodException if <code>controllerMethod</code> with a
   *           matching <code>parameters</code> list doesn't exist
   */
  public ViewControllerAction(String action, JComponent view, 
                              Controller controller, String method) throws NoSuchMethodException {
    super(action, view);
    putValue(ACTION_COMMAND_KEY, action);
    this.controller = controller;
    this.controllerMethod = controller.getClass().getMethod(method);
  }

  /**
   * Calls the registered controller method of this action. 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent ev) {
    try {
      this.controllerMethod.invoke(controller);
    } catch (IllegalAccessException ex) {
      throw new RuntimeException (ex);
    } catch (InvocationTargetException ex) {
      throw new RuntimeException (ex);
    }
  }
}

