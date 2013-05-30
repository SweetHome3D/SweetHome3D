/*
 * URLContentClassLoader.java 30 mai 2013
 * 
 * Sweet Home 3D, Copyright (c) 2013 Emmanuel PUYBARET / eTeks <info@eteks.com>
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.eteks.sweethome3d.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import com.eteks.sweethome3d.tools.URLContent;

/**
 * A class loader that gives access to its resources with {@link URLContent URLContent} instances.
 * @author Emmanuel Puybaret
 */
class URLContentClassLoader extends ClassLoader {
  private final URL url;

  public URLContentClassLoader(URL url) {
    this.url = url;
  }

  @Override
  public InputStream getResourceAsStream(String name) {
    try {
      // Return a stream managed by URLContent to be able to delete the writable files accessed with jar protocol
      return new URLContent(new URL("jar:" + this.url.toURI() + "!/" + name)).openStream();
    } catch (IOException ex) {
      return null;
    } catch (URISyntaxException ex) {
      return null;
    }
  }
}