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

import java.lang.reflect.Field;

/**
 * Gathers tools used by tests.
 * @author Emmanuel Puybaret
 */
public final class TestUtilities {  
  private TestUtilities() {    
    // This class isn't instatiable and contains only static methods
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
}
