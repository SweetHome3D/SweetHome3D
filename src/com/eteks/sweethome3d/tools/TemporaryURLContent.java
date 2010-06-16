/*
 * TemporaryURLContent.java 9 juil 2007
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
package com.eteks.sweethome3d.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import com.eteks.sweethome3d.model.Content;

/**
 * URL content for files, images...
 * @author Emmanuel Puybaret
 */
public class TemporaryURLContent extends URLContent {
  private static final long serialVersionUID = 1L;

  public TemporaryURLContent(URL temporaryUrl) {
    super(temporaryUrl);
  }

  /**
   * Returns a {@link URLContent URL content} object that references a temporary copy of 
   * a given <code>content</code>.
   */
  public static TemporaryURLContent copyToTemporaryURLContent(Content content) throws IOException {
    String extension = ".tmp";
    if (content instanceof URLContent) {
      String file = ((URLContent)content).getURL().getFile();
      int lastIndex = file.lastIndexOf('.');
      if (lastIndex > 0) {
        extension = file.substring(lastIndex);
      }
    }
    File tempFile = File.createTempFile("temp", extension, OperatingSystem.getDefaultTemporaryFolder());
    tempFile.deleteOnExit();
    InputStream tempIn = null;
    OutputStream tempOut = null;
    try {
      tempIn = content.openStream();
      tempOut = new FileOutputStream(tempFile);
      byte [] buffer = new byte [8192];
      int size; 
      while ((size = tempIn.read(buffer)) != -1) {
        tempOut.write(buffer, 0, size);
      }
    } finally {
      if (tempIn != null) {
        tempIn.close();
      }
      if (tempOut != null) {
        tempOut.close();
      }
    }
    return new TemporaryURLContent(tempFile.toURI().toURL());
  }
}
