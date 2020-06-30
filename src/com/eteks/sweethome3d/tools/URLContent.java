/*
 * URLContent.java 25 avr. 2006
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
package com.eteks.sweethome3d.tools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

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
    URLConnection connection = getURL().openConnection();
    if (isJAREntry()) {
      URL jarEntryURL = getJAREntryURL();
      if (jarEntryURL.getProtocol().equalsIgnoreCase("file")) {
        try {
          File file;
          try {
            file = new File(jarEntryURL.toURI());
          } catch (IllegalArgumentException ex) {
            // Try a second way to be able to access to files on Windows servers
            file = new File(jarEntryURL.getPath());
          }
          if (file.canWrite()) {
            // Even if cache is actually not used for JAR entries of files, refuse explicitly to use
            // caches to be able to delete the writable files accessed with jar protocol under Windows,
            // as suggested in http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6962459
            // Under other systems this is also required, otherwise the opened file is not closed on a call
            // to close() on the returned input stream, leading to resource leak when too many files are opened
            connection.setUseCaches(false);
          }
        } catch (URISyntaxException ex) {
          IOException ex2 = new IOException();
          ex2.initCause(ex);
          throw ex2;
        }
      }
    }
    return connection.getInputStream();
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
      return new URL(file.substring(0, file.indexOf("!/")));
    } catch (MalformedURLException ex) {
      throw new IllegalStateException("Invalid URL base for JAR entry", ex);
    }
  }

  /**
   * Returns the name of a JAR entry.
   * If the JAR entry in the URL given at creation time was encoded in application/x-www-form-urlencoded format,
   * this method will return it unchanged and not decoded.
   * @throws IllegalStateException if the URL of this content
   *                    doesn't reference an entry in a JAR URL.
   */
  public String getJAREntryName() {
    if (!isJAREntry()) {
      throw new IllegalStateException("Content isn't a JAR entry");
    }
    String file = this.url.getFile();
    return file.substring(file.indexOf("!/") + 2);
  }

  /**
   * Returns the size of this content.
   * @return the size of the uncompressed zip file from which this content comes if it's a JAR entry
   *      or the size of the content itself otherwise
   *      or -1 if the content couldn't be read
   */
  public long getSize() {
    InputStream in = null;
    ZipFile zipFile = null;
    try {
      if (isJAREntry()) {
        long size = 0;
        URL zipUrl = getJAREntryURL();
        if (zipUrl.getProtocol().equals("file")) {
          // Prefer to parse entries in zip files with ZipFile class because it runs much faster
          try {
            zipFile = new ZipFile(new File(zipUrl.toURI()));
            for (Enumeration<? extends ZipEntry> enumEntries = zipFile.entries(); enumEntries.hasMoreElements(); ) {
              size += enumEntries.nextElement().getSize();
            }
            return size;
          } catch (URISyntaxException ex) {
            // Try other method
          }
        }

        // Parse entries of the zipped stream
        ZipInputStream zipIn = new ZipInputStream(zipUrl.openStream());
        in = zipIn;
        for (ZipEntry entry; (entry = zipIn.getNextEntry()) != null; ) {
          long entrySize = entry.getSize();
          if (entrySize != -1) {
            size += entrySize;
          } else {
            size += getSize(zipIn);
          }
        }
        return size;
      } else {
        in = openStream();
        return getSize(in);
      }
    } catch (IOException ex) {
      return -1;
    } finally {
      try {
        if (in != null) {
          in.close();
        }
        if (zipFile != null) {
          zipFile.close();
        }
      } catch (IOException ex) {
        // Ignore close exception
      }
    }
  }

  /**
   * Returns the size of the data in the given input stream.
   */
  private long getSize(InputStream in) throws IOException {
    long size = 0;
    byte [] bytes = new byte [8192];
    for (int length; (length = in.read(bytes)) != -1; ) {
      size += length;
    }
    return size;
  }

  /**
   * Returns <code>true</code> if the object in parameter is an URL content
   * that references the same URL as this object.
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (obj instanceof URLContent) {
      URLContent urlContent = (URLContent)obj;
      return urlContent.url == this.url
          || urlContent.url.equals(this.url);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return this.url.hashCode();
  }
}
