/*
 * XMLWriter.java
 *
 * Copyright (c) 2015 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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

import java.io.FilterWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.util.Stack;

/**
 * A simple XML writer able to write XML elements, their attributes and texts, indenting child elements.
 * @author Emmanuel Puybaret
 */
public class XMLWriter extends FilterWriter {
  private Stack<String> elements = new Stack<String>();
  private boolean emptyElement;
  private boolean elementWithText;

  /**
   * Creates a writer in the given output stream encoded in UTF-8.
   */
  public XMLWriter(OutputStream out) throws IOException {
    super(new OutputStreamWriter(out, "UTF-8"));
    this.out.write("<?xml version='1.0'?>\n");
  }

  /**
   * Writes a start tag for the given element.
   */
  public void writeStartElement(String element) throws IOException {
    if (this.elements.size() > 0) {
      if (this.emptyElement) {
        this.out.write(">");
      }
      writeIndentation();
    }
    this.out.write("<" + element);
    this.elements.push(element);
    this.emptyElement = true;
    this.elementWithText = false;
  }

  /**
   * Writes an end tag for the given element.
   */
  public void writeEndElement() throws IOException {
    String element = this.elements.pop();
    if (this.emptyElement) {
      this.out.write("/>");
    } else {
      if (!this.elementWithText) {
        writeIndentation();
      }
      this.out.write("</" + element + ">");
    }
    this.emptyElement = false;
    this.elementWithText = false;
  }

  /**
   * Adds spaces according to the current depth of XML tree.
   */
  private void writeIndentation() throws IOException {
    this.out.write("\n");
    for (int i = 0; i < this.elements.size(); i++) {
      this.out.write("  ");
    }
  }

  /**
   * Writes the attribute of the given <code>name</code> with its <code>value</code>
   * in the tag of the last started element.
   */
  public void writeAttribute(String name, String value) throws IOException {
    this.out.write(" " + name + "='" + replaceByEntities(value) + "'");
  }

  /**
   * Writes the name and the value of an attribute in the tag of the last started element,
   * except if <code>value</code> equals <code>defaultValue</code>.
   */
  public void writeAttribute(String name, String value, String defaultValue) throws IOException {
    if ((value != null || value != defaultValue)
        && !value.equals(defaultValue)) {
      writeAttribute(name, value);
    }
  }

  /**
   * Writes the attribute of the given <code>name</code> with its integer <code>value</code>
   * in the tag of the last started element.
   */
  public void writeIntegerAttribute(String name, int value) throws IOException {
    writeAttribute(name, String.valueOf(value));
  }

  /**
   * Writes the name and the integer value of an attribute in the tag of the last started element,
   * except if <code>value</code> equals <code>defaultValue</code>.
   */
  public void writeIntegerAttribute(String name, int value, int defaultValue) throws IOException {
    if (value != defaultValue) {
      writeAttribute(name, String.valueOf(value));
    }
  }

  /**
   * Writes the attribute of the given <code>name</code> with its long <code>value</code>
   * in the tag of the last started element.
   */
  public void writeLongAttribute(String name, long value) throws IOException {
    writeAttribute(name, String.valueOf(value));
  }

  /**
   * Writes the name and the long value of an attribute in the tag of the last started element,
   * except if <code>value</code> equals <code>null</code>.
   */
  public void writeLongAttribute(String name, Long value) throws IOException {
    if (value != null) {
      writeAttribute(name, value.toString());
    }
  }

  /**
   * Writes the attribute of the given <code>name</code> with its float <code>value</code>
   * in the tag of the last started element.
   */
  public void writeFloatAttribute(String name, float value) throws IOException {
    writeAttribute(name, String.valueOf(value));
  }

  /**
   * Writes the name and the float value of an attribute in the tag of the last started element,
   * except if <code>value</code> equals <code>defaultValue</code>.
   */
  public void writeFloatAttribute(String name, float value, float defaultValue) throws IOException {
    if (value != defaultValue) {
      writeFloatAttribute(name, value);
    }
  }

  /**
   * Writes the name and the float value of an attribute in the tag of the last started element,
   * except if <code>value</code> equals <code>null</code>.
   */
  public void writeFloatAttribute(String name, Float value) throws IOException {
    if (value != null) {
      writeAttribute(name, value.toString());
    }
  }

  /**
   * Writes the name and the value of an attribute in the tag of the last started element,
   * except if <code>value</code> equals <code>null</code>.
   */
  public void writeBigDecimalAttribute(String name, BigDecimal value) throws IOException {
    if (value != null) {
      writeAttribute(name, String.valueOf(value));
    }
  }

  /**
   * Writes the name and the boolean value of an attribute in the tag of the last started element,
   * except if <code>value</code> equals <code>defaultValue</code>.
   */
  public void writeBooleanAttribute(String name, boolean value, boolean defaultValue) throws IOException {
    if (value != defaultValue) {
      writeAttribute(name, String.valueOf(value));
    }
  }

  /**
   * Writes the name and the color value of an attribute in the tag of the last started element,
   * except if <code>value</code> equals <code>null</code>. The color is written in hexadecimal.
   */
  public void writeColorAttribute(String name, Integer color) throws IOException {
    if (color != null) {
      writeAttribute(name, String.format("%08X", color));
    }
  }

  /**
   * Writes the given <code>text</code> as the content of the current element.
   */
  public void writeText(String text) throws IOException {
    if (this.emptyElement) {
      this.out.write(">");
      this.emptyElement = false;
      this.elementWithText = true;
    }
    super.out.write(replaceByEntities(text));
  }

  /**
   * Returns the string in parameter with &amp;, &lt;, &apos;, &quot; and feed line characters replaced by their matching entities.
   */
  private static String replaceByEntities(String s) {
    return s.replace("&", "&amp;").replace("<", "&lt;").replace("'", "&apos;").replace("\"", "&quot;").replace("\n", "&#10;");
  }

  /**
   * Writes the given character as the content of the current element.
   */
  public void write(int c) throws IOException {
    writeText(String.valueOf((char)c));
  }

  /**
   * Writes the given characters array as the content of the current element.
   */
  public void write(char buffer[], int offset, int length) throws IOException {
    writeText(new String(buffer, offset, length));
  }

  /**
   * Writes the given string as the content of the current element.
   */
  public void write(String str, int offset, int length) throws IOException {
    writeText(str.substring(offset, offset + length));
  }
}