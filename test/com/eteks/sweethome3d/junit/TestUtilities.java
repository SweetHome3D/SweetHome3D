/*
 * TestUtilities.java 16 mai 07
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
package com.eteks.sweethome3d.junit;

import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Field;

import junit.framework.TestCase;
import abbot.finder.BasicFinder;
import abbot.finder.ComponentSearchException;
import abbot.finder.Matcher;

/**
 * Gathers tools used by tests.
 * @author Emmanuel Puybaret
 */
public final class TestUtilities {  
  private TestUtilities() {    
    // This class isn't instantiable and contains only static methods
  }

  /**
   * Returns a reference to <code>fieldName</code> 
   * in a given <code>instance</code> by reflection.
   */
  public static Object getField(Object instance, String fieldName)
      throws NoSuchFieldException, IllegalAccessException {
    Field field = instance.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    return field.get(instance);
  }

  /**
   * Returns the component of a given class in <code>container</code> hierarchy.
   */
  public static Component findComponent(Container container, 
                                        final Class componentClass) 
      throws ComponentSearchException {
    return new BasicFinder().find(container, new Matcher () {
        public boolean matches(Component component) {
          return componentClass.isInstance(component);
        }
      });
  }

  /**
   * Asserts <code>value1</code> equals <code>value2</code> within <code>epsilon</code>.
   */
  public static void assertEqualsWithinEpsilon(String message, 
                                               float value1, float value2, float epsilon) {
    TestCase.assertTrue(message + ", expected:" + value1 + " but was:" + value2, 
        Math.abs(value1 - value2) < epsilon);
  }
}
