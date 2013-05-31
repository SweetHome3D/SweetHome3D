/*
 * DefaultHomeOutputStream.java 13 Oct 2008
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
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.tools.ResourceURLContent;
import com.eteks.sweethome3d.tools.TemporaryURLContent;
import com.eteks.sweethome3d.tools.URLContent;

/**
 * An <code>OutputStream</code> filter that writes a home in a stream 
 * at .sh3d file format. 
 * @see DefaultHomeInputStream
 */
public class DefaultHomeOutputStream extends FilterOutputStream {
  private int                    compressionLevel;
  private ContentRecording       contentRecording;
  private List<Content>          contents           = new ArrayList<Content>();
  private Map<URL, List<String>> zipUrlEntriesCache = new HashMap<URL, List<String>>();
  
  /**
   * Creates a stream that will serialize a home and all the contents it references
   * in an uncompressed zip stream.
   */
  public DefaultHomeOutputStream(OutputStream out) throws IOException {
    this(out, 0, false);
  }

  /**
   * Creates a stream that will serialize a home in a zip stream.
   * @param compressionLevel 0-9
   * @param includeTemporaryContent if <code>true</code>, content instances of 
   *            <code>TemporaryURLContent</code> class referenced by the saved home 
   *            as well as the content previously saved with it will be written. 
   *            If <code>false</code>, all the content instances 
   *            referenced by the saved home will be written in the zip stream.  
   */
  public DefaultHomeOutputStream(OutputStream out,
                                 int          compressionLevel, 
                                 boolean      includeTemporaryContent) throws IOException {
    this(out, compressionLevel, 
        includeTemporaryContent 
            ? ContentRecording.INCLUDE_TEMPORARY_CONTENT
            : ContentRecording.INCLUDE_ALL_CONTENT);
  }

  /**
   * Creates a stream that will serialize a home in a zip stream.
   * @param compressionLevel 0-9
   * @param contentRecording how content should be recorded with home.  
   */
  public DefaultHomeOutputStream(OutputStream out,
                                 int          compressionLevel, 
                                 ContentRecording contentRecording) throws IOException {
    super(out);
    this.compressionLevel = compressionLevel;
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
   * Writes home in a zipped stream followed by <code>Content</code> objects 
   * it points to.
   */
  public void writeHome(Home home) throws IOException {
    // Create a zip output on out stream 
    ZipOutputStream zipOut = new ZipOutputStream(this.out);
    zipOut.setLevel(this.compressionLevel);
    checkCurrentThreadIsntInterrupted();
    // Write home in first entry in a file "Home"
    zipOut.putNextEntry(new ZipEntry("Home"));
    // Use an ObjectOutputStream that keeps track of Content objects
    ObjectOutputStream objectOut = new HomeObjectOutputStream(zipOut);
    objectOut.writeObject(home);
    objectOut.flush();
    zipOut.closeEntry();
    // Write Content objects in files "0" to "n"
    for (int i = 0, n = contents.size(); i < n; i++) {
      Content content = contents.get(i);
      String entryNameOrDirectory = String.valueOf(i);
      if (content instanceof ResourceURLContent) {
        writeResourceZipEntries(zipOut, entryNameOrDirectory, (ResourceURLContent)content);
      } else if (content instanceof URLContent
                 && ((URLContent)content).isJAREntry()) {
        URLContent urlContent = (URLContent)content;
        // If content comes from a home stream
        if (urlContent instanceof HomeURLContent) {
          writeHomeZipEntries(zipOut, entryNameOrDirectory, (HomeURLContent)urlContent);            
        } else {
          writeZipEntries(zipOut, entryNameOrDirectory, urlContent);
        }
      } else {
        writeZipEntry(zipOut, entryNameOrDirectory, content);
      }
    }  
    // Finish zip writing
    zipOut.finish();
  }

  /**
   * Writes in <code>zipOut</code> stream one or more entries matching the content
   * <code>urlContent</code> coming from a resource file.
   */
  private void writeResourceZipEntries(ZipOutputStream zipOut,
                                       String entryNameOrDirectory,
                                       ResourceURLContent urlContent) throws IOException {
    if (urlContent.isMultiPartResource()) {
      if (urlContent.isJAREntry()) {
        URL zipUrl = urlContent.getJAREntryURL();
        String entryName = urlContent.getJAREntryName();
        int lastSlashIndex = entryName.lastIndexOf('/');
        String entryDirectory = entryName.substring(0, lastSlashIndex + 1);
        // Write in home stream each zipped stream entry that is stored in the same directory  
        for (String zipEntryName : getZipUrlEntries(zipUrl)) {
          if (zipEntryName.startsWith(entryDirectory)) {
            Content siblingContent = new URLContent(new URL("jar:" + zipUrl + "!/" 
                + URLEncoder.encode(zipEntryName, "UTF-8").replace("+", "%20")));
            writeZipEntry(zipOut, entryNameOrDirectory + zipEntryName.substring(lastSlashIndex), siblingContent);
          }
        }
      } else {
        // This should be the case only when resource isn't in a JAR file during development
        try {
          File contentFile = new File(urlContent.getURL().toURI());
          File parentFile = new File(contentFile.getParent());
          File [] siblingFiles = parentFile.listFiles();
          // Write in home stream each file that is stored in the same directory  
          for (File siblingFile : siblingFiles) {
            if (!siblingFile.isDirectory()) {
              writeZipEntry(zipOut, entryNameOrDirectory + "/" + siblingFile.getName(), 
                  new URLContent(siblingFile.toURI().toURL()));
            }
          }
        } catch (URISyntaxException ex) {
          IOException ex2 = new IOException();
          ex2.initCause(ex);
          throw ex2;
        }
      }
    } else {
      writeZipEntry(zipOut, entryNameOrDirectory, urlContent);
    }
  }

  /**
   * Returns the list of entries contained in <code>zipUrl</code>.
   */
  private List<String> getZipUrlEntries(URL zipUrl) throws IOException {
    List<String> zipUrlEntries = this.zipUrlEntriesCache.get(zipUrl);
    if (zipUrlEntries == null) {
      zipUrlEntries = new ArrayList<String>();
      this.zipUrlEntriesCache.put(zipUrl, zipUrlEntries);
      ZipInputStream zipIn = null;
      try {
        // Search all entries of zip url
        zipIn = new ZipInputStream(zipUrl.openStream());
        for (ZipEntry entry; (entry = zipIn.getNextEntry()) != null; ) {
          zipUrlEntries.add(entry.getName());
        }
      } finally {
        if (zipIn != null) {
          zipIn.close();
        }
      }
    }
    return zipUrlEntries;
  }
  
  /**
   * Writes in <code>zipOut</code> stream one or more entries matching the content
   * <code>urlContent</code> coming from a home file.
   */
  private void writeHomeZipEntries(ZipOutputStream zipOut,
                                   String entryNameOrDirectory,
                                   HomeURLContent urlContent) throws IOException {
    String entryName = urlContent.getJAREntryName();
    int slashIndex = entryName.indexOf('/');
    // If content comes from a directory of a home file
    if (slashIndex > 0) {
      URL zipUrl = urlContent.getJAREntryURL();
      String entryDirectory = entryName.substring(0, slashIndex + 1);
      // Write in home stream each zipped stream entry that is stored in the same directory  
      for (String zipEntryName : getZipUrlEntries(zipUrl)) {
        if (zipEntryName.startsWith(entryDirectory)) {
          Content siblingContent = new URLContent(new URL("jar:" + zipUrl + "!/" 
              + URLEncoder.encode(zipEntryName, "UTF-8").replace("+", "%20")));
          writeZipEntry(zipOut, entryNameOrDirectory + zipEntryName.substring(slashIndex), siblingContent);
        }
      }
    } else {
      writeZipEntry(zipOut, entryNameOrDirectory, urlContent);
    }
  }

  /**
   * Writes in <code>zipOut</code> stream all the sibling files of the zipped 
   * <code>urlContent</code>.
   */
  private void writeZipEntries(ZipOutputStream zipOut, 
                               String directory,
                               URLContent urlContent) throws IOException {
    ZipInputStream zipIn = null;
    try {
      // Open zipped stream that contains urlContent
      zipIn = new ZipInputStream(urlContent.getJAREntryURL().openStream());
      // Write each zipped stream entry in home stream 
      for (ZipEntry entry; (entry = zipIn.getNextEntry()) != null; ) {
        String zipEntryName = entry.getName();
        Content siblingContent = new URLContent(new URL("jar:" + urlContent.getJAREntryURL() + "!/" 
            + URLEncoder.encode(zipEntryName, "UTF-8").replace("+", "%20")));
        writeZipEntry(zipOut, directory + "/" + zipEntryName, siblingContent);
      }
    } finally {
      if (zipIn != null) {
        zipIn.close();
      }
    }
  }

  /**
   * Writes in <code>zipOut</code> stream a new entry named <code>entryName</code> that 
   * contains a given <code>content</code>.
   */
  private void writeZipEntry(ZipOutputStream zipOut, String entryName, Content content) throws IOException {
    checkCurrentThreadIsntInterrupted();
    byte [] buffer = new byte [8192];
    InputStream contentIn = null;
    try {
      zipOut.putNextEntry(new ZipEntry(entryName));
      contentIn = content.openStream();          
      int size; 
      while ((size = contentIn.read(buffer)) != -1) {
        zipOut.write(buffer, 0, size);
      }
      zipOut.closeEntry();  
    } finally {
      if (contentIn != null) {          
        contentIn.close();
      }
    }
  }

  /**
   * <code>ObjectOutputStream</code> that replaces <code>Content</code> objects
   * by temporary <code>URLContent</code> objects and stores them in a list.
   */
  private class HomeObjectOutputStream extends ObjectOutputStream {
    public HomeObjectOutputStream(OutputStream out) throws IOException {
      super(out);
      if (contentRecording != ContentRecording.INCLUDE_NO_CONTENT) {
        enableReplaceObject(true);
      }
    }

    @Override
    protected Object replaceObject(Object obj) throws IOException {
      if (obj instanceof TemporaryURLContent 
          || obj instanceof HomeURLContent
          || (contentRecording == ContentRecording.INCLUDE_ALL_CONTENT && obj instanceof Content)) {
        // Add obj to Content objects list
        contents.add((Content)obj);

        String subEntryName = "";
        if (obj instanceof URLContent) {
          URLContent urlContent = (URLContent)obj;
          // If content comes from a zipped content  
          if (urlContent.isJAREntry()) {
            String entryName = urlContent.getJAREntryName();
            if (urlContent instanceof HomeURLContent) {
              int slashIndex = entryName.indexOf('/');
              // If content comes from a directory of a home file
              if (slashIndex > 0) {
                // Retrieve entry name in zipped stream without the directory
                subEntryName = entryName.substring(slashIndex);
              }
            } else if (urlContent instanceof ResourceURLContent) {
              ResourceURLContent resourceUrlContent = (ResourceURLContent)urlContent;
              if (resourceUrlContent.isMultiPartResource()) {
                // If content is a resource coming from a JAR file, retrieve its file name
                subEntryName = entryName.substring(entryName.lastIndexOf('/'));
              }
            } else {
              // Retrieve entry name in zipped stream
              subEntryName = "/" + entryName;
            }            
          } else if (urlContent instanceof ResourceURLContent) {
            ResourceURLContent resourceUrlContent = (ResourceURLContent)urlContent;
            // If content is a resource coming from a directory (this should be the case 
            // only when resource isn't in a JAR file during development), retrieve its file name
            if (resourceUrlContent.isMultiPartResource()) {
              try {
                subEntryName = "/" + new File(resourceUrlContent.getURL().toURI()).getName();
              } catch (URISyntaxException ex) {
                IOException ex2 = new IOException();
                ex2.initCause(ex);
                throw ex2;
              }
            }
          }
        } 

        // Return a temporary URL that points to content object 
        return new URLContent(new URL("jar:file:temp!/" + (contents.size() - 1) + subEntryName));
      } else {
        return obj;
      }
    }
  }
}