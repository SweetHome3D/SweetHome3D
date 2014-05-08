/*
 * DefaultHomeInputStream.java 13 Oct 2008
 *
 * Sweet Home 3D, Copyright (c) 2008 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.tools.URLContent;

/**
 * An <code>InputStream</code> filter that reads a home from a stream 
 * at .sh3d file format. 
 * @see DefaultHomeOutputStream
 */
public class DefaultHomeInputStream extends FilterInputStream {
  private final ContentRecording contentRecording;

  /**
   * Creates a home input stream filter able to read a home and its content
   * from <code>in</code>. The dependencies of the read home included in the stream 
   * will be checked.
   */
  public DefaultHomeInputStream(InputStream in) throws IOException {
    this(in, ContentRecording.INCLUDE_ALL_CONTENT);
  }

  /**
   * Creates a home input stream filter able to read a home and its content
   * from <code>in</code>.
   */
  public DefaultHomeInputStream(InputStream in, 
                                ContentRecording contentRecording) throws IOException {
    super(in);
    this.contentRecording = contentRecording;
  }

  /**
   * Throws an <code>InterruptedRecorderException</code> exception 
   * if current thread is interrupted. The interrupted status of the current thread 
   * is cleared when an exception is thrown.
   */
  private static void checkCurrentThreadIsntInterrupted() throws InterruptedIOException {
    if (Thread.interrupted()) {
      throw new InterruptedIOException();
    }
  }
  
  /**
   * Reads home from a zipped stream. 
   */
  public Home readHome() throws IOException, ClassNotFoundException {
    File fileCopy = null;
    boolean validZipFile = true;
    if (this.contentRecording != ContentRecording.INCLUDE_NO_CONTENT) {
      // Copy home stream in a temporary file  
      fileCopy = copyInputStreamToTemporaryFile(this.in);
      // Check if all entries in the temporary file can be fully read using a zipped input stream
      List<ZipEntry> validEntries = new ArrayList<ZipEntry>();
      validZipFile = isZipFileValidUsingInputStream(fileCopy, validEntries) && validEntries.size() > 0;
      if (!validZipFile) {
        int validEntriesCount = validEntries.size();
        validEntries.clear();
        // Check how many entries can be read using zip dictionnary
        // (some times, this gives a different result from the previous way)
        // and create a new copy with only valid entries
        isZipFileValidUsingDictionnary(fileCopy, validEntries);
        if (validEntries.size() > validEntriesCount) {
          fileCopy = createTemporaryFileFromValidEntries(fileCopy, validEntries);
        } else {
          fileCopy = createTemporaryFileFromValidEntriesCount(fileCopy, validEntriesCount);
        }
      }      
    }
    
    ZipInputStream zipIn = null;
    try {
      // Open a zip input from temp file
      zipIn = new ZipInputStream(this.contentRecording == ContentRecording.INCLUDE_NO_CONTENT
          ? this.in : new FileInputStream(fileCopy));
      // Read Home entry
      ZipEntry entry;
      while ((entry = zipIn.getNextEntry()) != null
          && !"Home".equals(entry.getName())) {
      }
      if (entry == null) {
        throw new IOException("Missing entry \"Home\"");
      }
      checkCurrentThreadIsntInterrupted();
      // Use an ObjectInputStream that replaces temporary URLs of Content objects 
      // by URLs relative to file 
      HomeObjectInputStream objectStream = new HomeObjectInputStream(zipIn, fileCopy);
      Home home = (Home)objectStream.readObject();
      if (!validZipFile) {
        throw new DamagedHomeIOException(home, objectStream.getInvalidContent());
      }
      return home;
    } finally {
      if (zipIn != null) {
        zipIn.close();
      }
    }
  }
  
  /**
   * Returns a copy of <code>in</code> content in a temporary file.
   */
  private File copyInputStreamToTemporaryFile(InputStream in) throws IOException {
    File tempfile = OperatingSystem.createTemporaryFile("open", ".sweethome3d");
    checkCurrentThreadIsntInterrupted();
    OutputStream tempOut = null;
    try {
      tempOut = new FileOutputStream(tempfile);
      byte [] buffer = new byte [8192];
      int size; 
      while ((size = in.read(buffer)) != -1) {
        tempOut.write(buffer, 0, size);
      }
    } finally {
      if (tempOut != null) {
        tempOut.close();
      }
    }
    return tempfile;
  }
  
  /**
   * Returns <code>true</code> if all the entries of the given zipped <code>file</code> are valid.  
   * <code>validEntries</code> will contain the valid entries.
   */
  private boolean isZipFileValidUsingDictionnary(File file, List<ZipEntry> validEntries) throws IOException {
    ZipFile zipFile = null;
    boolean validZipFile = true;
    try {
      zipFile = new ZipFile(file);
      for (Enumeration<? extends ZipEntry> enumEntries = zipFile.entries(); enumEntries.hasMoreElements(); ) {
        try {
          ZipEntry zipEntry = enumEntries.nextElement();
          InputStream zipIn = zipFile.getInputStream(zipEntry);
          // Read the entry to check it's ok
          byte [] buffer = new byte [8192];
          while (zipIn.read(buffer) != -1) {
          }
          zipIn.close();
          validEntries.add(zipEntry);
        } catch (IOException ex) {
          validZipFile = false;
        } 
      }
    } catch (Exception ex) {
      validZipFile = false;
    } finally {
      if (zipFile != null) {
        zipFile.close();
      }
    }
    return validZipFile;
  }

  /**
   * Returns <code>true</code> if all the entries of the given zipped <code>file</code> are valid.  
   * <code>validEntries</code> will contain the valid entries.
   */
  private boolean isZipFileValidUsingInputStream(File file, List<ZipEntry> validEntries) throws IOException {
    ZipInputStream zipIn = null;
    try {
      zipIn = new ZipInputStream(new FileInputStream(file));
      byte [] buffer = new byte [8192];
      for (ZipEntry zipEntry = null; (zipEntry = zipIn.getNextEntry()) != null; ) {
        // Read the entry to check it's ok
        while (zipIn.read(buffer) != -1) {
        }
        validEntries.add(zipEntry);
      }
      return true;
    } catch (IOException ex) {
      return false;
    } finally {
      if (zipIn != null) {
        zipIn.close();
      }
    }
  }
  
  /**
   * Returns a temporary file containing the valid entries of the given <code>file</code>.
   */
  private File createTemporaryFileFromValidEntries(File file, List<ZipEntry> validEntries) throws IOException {
    if (validEntries.size() <= 0) {      
      throw new IOException("No valid entries");
    }
    File tempfile = OperatingSystem.createTemporaryFile("part", ".sh3d");
    ZipOutputStream zipOut = null;
    ZipFile zipFile = null;
    try {
      zipFile = new ZipFile(file);
      zipOut = new ZipOutputStream(new FileOutputStream(tempfile));
      zipOut.setLevel(0);
      for (ZipEntry zipEntry : validEntries) {
        InputStream zipIn = zipFile.getInputStream(zipEntry);
        copyEntry(zipIn, zipEntry, zipOut);
        zipIn.close();
      }
      return tempfile;
    } finally {
      if (zipOut != null) {
        zipOut.close();
      }
      if (zipFile != null) {
        zipFile.close();
      }
    }
  }
  
  /**
   * Returns a temporary file containing the first valid entries count of the given <code>file</code>.
   */
  private File createTemporaryFileFromValidEntriesCount(File file, int entriesCount) throws IOException {
    if (entriesCount <= 0) {
      throw new IOException("No valid entries");
    }
    File tempfile = OperatingSystem.createTemporaryFile("part", ".sh3d");
    ZipOutputStream zipOut = null;
    ZipInputStream zipIn = null;
    try {
      zipIn = new ZipInputStream(new FileInputStream(file));
      zipOut = new ZipOutputStream(new FileOutputStream(tempfile));
      zipOut.setLevel(0);
      while (entriesCount-- > 0) {
        copyEntry(zipIn, zipIn.getNextEntry(), zipOut);
      }
      return tempfile;
    } finally {
      if (zipOut != null) {
        zipOut.close();
      }
      if (zipIn != null) {
        zipIn.close();
      }
    }
  }

  /**
   * Copies the a zipped entry.
   */
  private void copyEntry(InputStream zipIn, ZipEntry entry, ZipOutputStream zipOut) throws IOException {
    ZipEntry entryCopy = new ZipEntry(entry.getName());
    entryCopy.setComment(entry.getComment());
    entryCopy.setTime(entry.getTime());
    entryCopy.setExtra(entry.getExtra());
    zipOut.putNextEntry(entryCopy);
    byte [] buffer = new byte [8192];
    int size; 
    while ((size = zipIn.read(buffer)) != -1) {
      zipOut.write(buffer, 0, size);
    }
    zipOut.closeEntry();
  }

  /**
   * <code>ObjectInputStream</code> that replaces temporary <code>URLContent</code> 
   * objects by <code>URLContent</code> objects that points to file.
   */
  private class HomeObjectInputStream extends ObjectInputStream {
    private File zipFile;
    private List<Content> invalidContent;

    public HomeObjectInputStream(InputStream in, File zipFile) throws IOException {
      super(in);
      if (contentRecording != ContentRecording.INCLUDE_NO_CONTENT) {
        enableResolveObject(true);
        this.zipFile = zipFile;
        this.invalidContent = new ArrayList<Content>();
      }
    }
    
    @Override
    protected Object resolveObject(Object obj) throws IOException {
      if (obj instanceof URLContent) {
        URL tmpURL = ((URLContent)obj).getURL();
        String url = tmpURL.toString();
        if (url.startsWith("jar:file:temp!/")) {
          // Replace "temp" in URL by current temporary file
          String entryName = url.substring(url.indexOf('!') + 2);
          URL fileURL = new URL("jar:" + this.zipFile.toURI() + "!/" + entryName);
          HomeURLContent urlContent = new HomeURLContent(fileURL);
          if (!isValid(urlContent)) {
            this.invalidContent.add(urlContent);
          }
          return urlContent;
        } else {
          return obj;
        }
      } else {
        return obj;
      }
    }
 
    /**
     * Returns <code>true</code> if the given <code>content</code> exists.
     */
    private boolean isValid(Content content) {
      try {
        content.openStream().close();
        return true;
      } catch (IOException e) {
        return false;
      }
    }
    
    /**
     * Returns the list of invalid content found during deserialization.
     */
    public List<Content> getInvalidContent() {
      return this.invalidContent;
    }
  }
}