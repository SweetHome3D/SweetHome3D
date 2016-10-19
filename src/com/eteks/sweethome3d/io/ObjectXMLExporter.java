/*
 * ObjectXMLExporter.java 
 *
 * Copyright (c) 2016 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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
package com.eteks.sweethome3d.io;

import java.io.IOException;

import com.eteks.sweethome3d.io.XMLWriter;

/**
 * Base class used to write objects to XML.
 * @author Emmanuel Puybaret
 */
public abstract class ObjectXMLExporter<T> {
  /**
   * Writes in XML the given <code>object</code> in the element returned by the 
   * {@link #getTag(Object) getTag}, then writes its attributes and children 
   * calling {@link #writeAttributes(XMLWriter, Object) writeAttributes}
   * and {@link #writeChildren(XMLWriter, Object) writeChildren} methods.
   */
  public void writeElement(XMLWriter writer, T object) throws IOException {
    writer.writeStartElement(getTag(object));
    writeAttributes(writer, object);
    writeChildren(writer, object);
    writer.writeEndElement();
  }

  /**
   * Returns the element tag matching the object in parameter.
   * @return the simple class name of the exported object with its first letter at lower case,
   *    without <code>Home</code> prefix if it's the case.
   */
  protected String getTag(T object) {
    String tagName = object.getClass().getSimpleName();
    if (tagName.startsWith("Home") && !tagName.equals("Home")) {
      // Remove "Home" prefix
      tagName = tagName.substring(4);
    }
    return Character.toLowerCase(tagName.charAt(0)) + tagName.substring(1);
  }

  /**
   * Writes the attributes of the object in parameter.
   */
  protected void writeAttributes(XMLWriter writer, T object) throws IOException {
  }
  
  /**
   * Writes the children of the object in parameter.
   */
  protected void writeChildren(XMLWriter writer, T object) throws IOException {
  }
}