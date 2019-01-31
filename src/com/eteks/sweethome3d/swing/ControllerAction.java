/*
 * ControllerAction.java 8 août 2006
 *
 * Sweet Home 3D, Copyright (c) 2006 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
import java.awt.event.ActionListener;
import java.awt.im.InputContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Locale;

import javax.swing.KeyStroke;
import javax.swing.Timer;

import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;

/**
 * An action which <code>actionPerformed</code> method
 * will call a parametrizable method.
 * @author Emmanuel Puybaret
 */
public class ControllerAction extends ResourceAction {
  private final Object    controller;
  private final Method    controllerMethod;
  private final Object [] parameters;

  /**
   * Creates a disabled action with properties retrieved from a resource bundle
   * in which key starts with <code>actionPrefix</code>.
   * @param preferences   user preferences used to retrieve localized description of the action
   * @param resourceClass the class used as a context to retrieve localized properties of the action
   * @param actionPrefix prefix used in resource bundle to search action properties
   * @param controller   the controller on which the method will be called
   * @param method       the name of the controller method that will be invoked
   *          in {@link #actionPerformed(ActionEvent) actionPerfomed}
   * @param parameters list of parameters to be used with <code>method</code>
   * @throws NoSuchMethodException if <code>method</code> with a
   *           matching <code>parameters</code> list doesn't exist
   */
  public ControllerAction(UserPreferences preferences,
                          Class<?> resourceClass,
                          String actionPrefix,
                          Object controller,
                          String method,
                          Object ... parameters) throws NoSuchMethodException {
    this(preferences, resourceClass, actionPrefix, false, controller, method, parameters);
  }

  /**
   * Creates an action with properties retrieved from a resource bundle
   * in which key starts with <code>actionPrefix</code>.
   * @param preferences   user preferences used to retrieve localized description of the action
   * @param resourceClass the class used as a context to retrieve localized properties of the action
   * @param actionPrefix prefix used in resource bundle to search action properties
   * @param enabled <code>true</code> if the action should be enabled at creation.
   * @param controller   the controller on which the method will be called
   * @param method       the name of the controller method that will be invoked
   *          in {@link #actionPerformed(ActionEvent) actionPerfomed}
   * @param parameters list of parameters to be used with <code>method</code>
   * @throws NoSuchMethodException if <code>method</code> with a
   *           matching <code>parameters</code> list doesn't exist
   */
  public ControllerAction(UserPreferences preferences,
                          Class<?> resourceClass,
                          String actionPrefix,
                          boolean enabled,
                          Object controller,
                          String method,
                          Object ... parameters) throws NoSuchMethodException {
    super(preferences, resourceClass, actionPrefix, enabled);
    this.controller = controller;
    this.parameters = parameters;
    // Get parameters class
    Class<?> [] parametersClass = new Class [parameters.length];
    for(int i = 0; i < parameters.length; i++) {
      parametersClass [i] = parameters [i].getClass();
    }

    Method controllerMethod = null;
    try {
      controllerMethod = controller.getClass().getMethod(method, parametersClass);
    } catch (NoSuchMethodException ex) {
      // Try to find if a method with types assignable from the given parameters exists
      for (Method classMethod : controller.getClass().getMethods()) {
        if (classMethod.getName().equals(method)) {
          Class<?> [] parameterTypes = classMethod.getParameterTypes();
          if (parameterTypes.length == parametersClass.length) {
            int i = 0;
            for ( ; i < parameterTypes.length; i++) {
              if (!parameterTypes [i].isAssignableFrom(parametersClass [i])) {
                break;
              }
            }
            if (i == parameterTypes.length) {
              controllerMethod = classMethod;
              break;
            }
          }
        }
      }
      if (controllerMethod == null) {
        throw ex;
      }
    }
    this.controllerMethod = controllerMethod;
  }

  private static final String [] LATIN_AND_SUPPORTED_LOCALES = new String [] {"cs", "da", "de", "en", "es", "et", "fi", "fr", "hr", "hu", "it", "ja", "lt", "lv", "nl", "no", "pl", "pt", "ro", "sk", "sl", "sv", "tr", "vi"};
  private static KeyStroke previousActionAccelerator;
  private static Timer     doubleEventsTimer;

  /**
   * Calls the method on controller given in constructor.
   */
  @Override
  public void actionPerformed(ActionEvent ev) {
    try {
      if (OperatingSystem.isMacOSX()
          && OperatingSystem.isJavaVersionBetween("1.7", "9")) {
        Locale inputLocale = InputContext.getInstance().getLocale();
        if (inputLocale != null
            && Arrays.binarySearch(LATIN_AND_SUPPORTED_LOCALES, inputLocale.getLanguage()) < 0) {
          // Accelerators used with non latin keyboards provokes two events,
          // the second event being emitted by the menu item management
          if (!isInvokedFromMenuItem()) {
            previousActionAccelerator = (KeyStroke)getValue(ACCELERATOR_KEY);
            // Cancel double event tracker in 1s in case the user provokes events
            // by selecting the toolbar button then the menu item matching the accelerator
            if (doubleEventsTimer == null) {
              doubleEventsTimer = new Timer(1000, new ActionListener() {
                  public void actionPerformed(ActionEvent ev) {
                    previousActionAccelerator = null;
                    doubleEventsTimer.stop();
                  }
                });
            }
            doubleEventsTimer.restart();
          } else if (previousActionAccelerator != null
                     && previousActionAccelerator.equals(getValue(ACCELERATOR_KEY))) {
            previousActionAccelerator = null;
            return; // Cancel the second event
          }
        }
      }
      this.controllerMethod.invoke(controller, parameters);
    } catch (IllegalAccessException ex) {
      throw new RuntimeException (ex);
    } catch (InvocationTargetException ex) {
      throw new RuntimeException (ex);
    }
  }

  /**
   * Returns <code>true</code> if current call is done from a menu item.
   */
  private boolean isInvokedFromMenuItem() {
    for (StackTraceElement stackElement : Thread.currentThread().getStackTrace()) {
      if ("com.apple.laf.ScreenMenuItem".equals(stackElement.getClassName())
          && "actionPerformed".equals(stackElement.getMethodName())) {
        return true;
      }
    }
    return false;
  }
}
