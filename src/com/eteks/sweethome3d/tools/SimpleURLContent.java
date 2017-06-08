/*
 * SimpleURLContent.java 4 Fev. 2016
 *
 * Sweet Home 3D, Copyright (c) 2016 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
import java.net.URL;

/**
 * Content read from a URL with no dependency on other content when this URL is a JAR entry.
 * @author Emmanuel Puybaret
 * @since 5.2
 */
public class SimpleURLContent extends URLContent {
  private static final long serialVersionUID = 1L;
  
  public SimpleURLContent(URL url) {
    super(url);
  }
  
  @Override
  public long getSize() {
    long size = 0; 
    InputStream in = null;
    try {
      in = openStream();
      byte [] bytes = new byte [8192];
      for (int length; (length = in.read(bytes)) != -1; ) {
        size += length;
      }
    } catch (IOException ex) {
      return -1;
    } finally {
      try {
        if (in != null) {
          in.close();
        }
      } catch (IOException ex) {
        // Ignore close exception
      }
    }
    return size;
  }
}
