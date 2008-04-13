/*
 * URLContent.java 25 avr. 2006
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
package com.eteks.sweethome3d.tools;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import com.eteks.sweethome3d.model.Content;

/**
 * URL content for files, images...
 * @author Emmanuel Puybaret
 */
public class URLContent implements Content {
  private static final long serialVersionUID = 1L;

  private URL url;
  
  public URLContent(URL url) {
    this.url = url;
  }

  /**
   * Returns the URL of this content.
   */
  public URL getURL() {
    return this.url;
  }

  /**
   * Returns an InputStream on the URL content. 
   * @throws IOException if URL stream can't be opened. 
   */
  public InputStream openStream() throws IOException {
    return this.url.openStream();
  }
  
  /**
   * Returns <code>true</code> if the URL stored by this content 
   * references an entry in a JAR.
   */
  public boolean isJAREntry() {
    return "jar".equals(this.url.getProtocol());
  }
  
  /**
   * Returns the URL base of a JAR entry.
   * @throws IllegalStateException if the URL of this content 
   *                    doesn't reference an entry in a JAR.
   */
  public URL getJAREntryURL() {
    if (!isJAREntry()) {
      throw new IllegalStateException("Content isn't a JAR entry");
    }
    try {
      String file = this.url.getFile();
      return new URL(file.substring(0, file.indexOf('!')));
    } catch (MalformedURLException ex) {
      throw new IllegalStateException("Invalid URL base for JAR entry", ex);
    }
  }

  /**
   * Returns the name of a JAR entry.
   * @throws IllegalStateException if the URL of this content 
   *                    doesn't reference an entry in a JAR URL.
   */
  public String getJAREntryName() {
    if (!isJAREntry()) {
      throw new IllegalStateException("Content isn't a JAR entry");
    }
    String file = this.url.getFile();
    return file.substring(file.indexOf('!') + 2);
  }
}
