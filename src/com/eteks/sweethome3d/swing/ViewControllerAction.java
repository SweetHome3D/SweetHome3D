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
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.JComponent;

/**
 * An action configured to call a controller method.
 * @author Emmanuel Puybaret
 */
public class ViewControllerAction extends AbstractAction {
  private Object     controller;  //  TODOO change controller type to Controller
  private Object []  parameters;
  private Method     controllerMethod;

  /**
   * Creates an action whose {@link #actionPerfomed} will call <code>controllerMethod</code> on <code>controller</code> 
   * with the given list of <code>parameters</code>. The action is configured with values starting with 
   * <code>actionPrefix</code> retrieved from properties bundle of prefix <code>view.getClass().getName()</code>.
   * @param actionPrefix
   * @param view
   * @param controller
   * @param controllerMethod
   * @param parameters
   * @throws NoSuchMethodException
   */
  public ViewControllerAction(String actionPrefix, JComponent view, Object controller, String controllerMethod, Object ... parameters) throws NoSuchMethodException {
    //  TODOO change controller parameter type to Controller
    this.controller = controller;
    this.parameters = parameters;
    configureAction (view, actionPrefix);
    // Get parameters class
    Class [] parametersClass = new Class [parameters.length];
    for(int i = 0; i < parameters.length; i++)
      parametersClass [i] = parameters [i].getClass();
    this.controllerMethod = controller.getClass().getMethod(controllerMethod, parametersClass);
  }

  private void configureAction(JComponent view, String actionPrefix) {
    // ResourceBundle resource = ResourceBundle.getBundle(view.getClass().getName());
    // TODOO Read localized values in bundle using actionPrefix
    // Find a way to represent a KeyStroke or a KeyEvent as a string (toString() ?) 
    // Find a way to use different KeyStroke on Mac OS and Windows (for example Command + Q or Alt + F4 to quit)
    // Take care of Command/Ctrl key on various systems with Toolkit#getMenuShortcutKeyMask
    // No mnemonic under Mac OS X : is there a system property that disables them ?
    // Read icon as a resource URL (do not use IconManager)
    // Use actionPrefix as ACTION-COMMAND_KEY ?
    
  }

  /**
   * Calls the registered controller method of this action. 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent ev) {
    try {
      this.controllerMethod.invoke(controller, parameters);
    } catch (IllegalAccessException ex) {
      throw new RuntimeException (ex);
    } catch (InvocationTargetException ex) {
      throw new RuntimeException (ex);
    }
  }
  
  // TODOO Move to a JUnit class
  public static void main(String [] args) throws NoSuchMethodException {
    class Test {
      // Test with no parameter
      public void test1() { 
        System.out.println("It works !");
      }
      
      // Test with an object parameter
      public void test2(String s) {
        System.out.println("It works with " + s);
      }

      // Test with a wrapping class object parameter
      public void test3(Boolean b) {
        System.out.println("It works with "+ b);
      }

      // Test with an primary type parameter
      public void test4(boolean b) {
        System.out.println("It doesn't work with "+ b);
      }
    }
    
    new ViewControllerAction ("test", new JComponent() {}, new Test(), "test1").actionPerformed(null);
    new ViewControllerAction ("test", new JComponent() {}, new Test(), "test2", "value").actionPerformed(null);
    new ViewControllerAction ("test", new JComponent() {}, new Test(), "test3", true).actionPerformed(null);
    new ViewControllerAction ("test", new JComponent() {}, new Test(), "test4", true).actionPerformed(null);
  }
}
