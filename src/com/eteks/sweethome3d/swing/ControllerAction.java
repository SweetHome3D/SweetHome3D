/*
 * ControllerAction.java 8 août 2006
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
import java.util.ResourceBundle;

/**
 * An action which <code>actionPerformed</code> method 
 * will call a parametrizable method. 
 * @author Emmanuel Puybaret
 */
public class ControllerAction extends ResourceAction {
  private Object    controller;
  private Method    controllerMethod;
  private Object [] parameters;

  /**
   * Creates an action with properties retrieved from a resource bundle 
   * in which key starts with <code>actionPrefix</code>.
   * @param resource     a resource bundle
   * @param actionPrefix prefix used in resource bundle to search action properties
   * @param controller   the controller on which the method will be called
   * @param method       the name of the controller method that will be invoked
   *          in {@link #actionPerformed(ActionEvent) actionPerfomed}
   * @param parameters list of parameters to be used with <code>method</code>
   * @throws NoSuchMethodException if <code>method</code> with a
   *           matching <code>parameters</code> list doesn't exist
   */
  public ControllerAction(ResourceBundle resource, String actionPrefix, 
               Object controller, String method, Object ... parameters)
                     throws NoSuchMethodException {
    super(resource, actionPrefix);
    this.controller = controller;
    this.parameters = parameters;
    // Get parameters class
    Class [] parametersClass = new Class [parameters.length];
    for(int i = 0; i < parameters.length; i++)
      parametersClass [i] = parameters [i].getClass();
    
    this.controllerMethod = controller.getClass().getMethod(method, parametersClass);
  }

  /**
   * Calls the method on controller given in constructor.
   */
  @Override
  public void actionPerformed(ActionEvent ev) {
    try {
      this.controllerMethod.invoke(controller, parameters);
    } catch (IllegalAccessException ex) {
      throw new RuntimeException (ex);
    } catch (InvocationTargetException ex) {
      throw new RuntimeException (ex);
    }
  }
}
