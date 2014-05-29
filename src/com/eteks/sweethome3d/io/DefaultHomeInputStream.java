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

import java.io.BufferedOutputStream;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.CatalogTexture;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.FurnitureCategory;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.TexturesCategory;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.tools.URLContent;

/**
 * An <code>InputStream</code> filter that reads a home from a stream 
 * at .sh3d file format. 
 * @see DefaultHomeOutputStream
 */
public class DefaultHomeInputStream extends FilterInputStream {
  private final ContentRecording   contentRecording;
  private final UserPreferences    preferences;
  private Set<URLContent>          preferencesContentsCache;

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
    this(in, contentRecording, null);
  }

  /**
   * Creates a home input stream filter able to read a home and its content
   * from <code>in</code>. If <code>preferences</code> isn't <code>null</code>, 
   * the furniture and textures contents it references will replace the one of 
   * the read home when they are equal.
   */
  public DefaultHomeInputStream(InputStream in, 
                                ContentRecording contentRecording, 
                                UserPreferences preferences) {
    super(in);
    this.contentRecording = contentRecording;
    this.preferences = preferences;
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
      // and check if all entries in the temporary file can be fully read using a zipped input stream
      fileCopy = OperatingSystem.createTemporaryFile("open", ".sweethome3d");
      OutputStream fileCopyOut = new BufferedOutputStream(new FileOutputStream(fileCopy));
      List<ZipEntry> validEntries = new ArrayList<ZipEntry>();
      validZipFile = isZipFileValidUsingInputStream(new CopyInputStream(this.in, fileCopyOut), validEntries) 
          && validEntries.size() > 0;
      if (!validZipFile) {
        int validEntriesCount = validEntries.size();
        validEntries.clear();
        // Check how many entries can be read using zip dictionary
        // (some times, this gives a different result from the previous way)
        // and create a new copy with only valid entries
        isZipFileValidUsingDictionnary(fileCopy, validEntries);
        if (validEntries.size() > validEntriesCount) {
          fileCopy = createTemporaryFileFromValidEntries(fileCopy, validEntries);
        } else {
          fileCopy = createTemporaryFileFromValidEntriesCount(fileCopy, validEntriesCount);
        }
      }
      
      if (this.preferences != null 
          && this.preferencesContentsCache == null) {
        this.preferencesContentsCache = getUserPreferencesContent(this.preferences);
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
      List<Content> invalidContent = objectStream.getInvalidContents();
      if (!validZipFile || invalidContent.size() > 0) {
        throw new DamagedHomeIOException(home, invalidContent);
      }
      return home;
    } finally {
      if (zipIn != null) {
        zipIn.close();
      }
    }
  }

  /**
   * Returns <code>true</code> if all the entries of the given zipped <code>file</code> are valid.  
   * <code>validEntries</code> will contain the valid entries.
   */
  private boolean isZipFileValidUsingInputStream(InputStream in, List<ZipEntry> validEntries) throws IOException {
    ZipInputStream zipIn = null;
    try {
      zipIn = new ZipInputStream(in);
      byte [] buffer = new byte [8192];
      for (ZipEntry zipEntry = null; (zipEntry = zipIn.getNextEntry()) != null; ) {
        // Read the entry to check it's ok
        while (zipIn.read(buffer) != -1) {
        }
        validEntries.add(zipEntry);
        checkCurrentThreadIsntInterrupted();
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
          checkCurrentThreadIsntInterrupted();
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
   * Copies the a zipped entry.
   */
  private void copyEntry(InputStream zipIn, ZipEntry entry, ZipOutputStream zipOut) throws IOException {
    checkCurrentThreadIsntInterrupted();
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
   * Returns the content in preferences that could be shared with read homes.
   */
  private Set<URLContent> getUserPreferencesContent(UserPreferences preferences) {
    Set<URLContent> preferencesContent = new HashSet<URLContent>();
    for (FurnitureCategory category : preferences.getFurnitureCatalog().getCategories()) {
      for (CatalogPieceOfFurniture piece : category.getFurniture()) {
        addURLContent(piece.getIcon(), preferencesContent);
        addURLContent(piece.getModel(), preferencesContent);
        addURLContent(piece.getPlanIcon(), preferencesContent);
      }
    }
    for (TexturesCategory category : preferences.getTexturesCatalog().getCategories()) {
      for (CatalogTexture texture : category.getTextures()) {
        addURLContent(texture.getImage(), preferencesContent);
      }
    }
    return preferencesContent;
  }
  
  private void addURLContent(Content content, Set<URLContent> preferencesContent) {
    if (content instanceof URLContent) {
      preferencesContent.add((URLContent)content);
    }
  }

  /**
   * An input stream filter that copies to a given output stream everything it reads.
   */
  private class CopyInputStream extends FilterInputStream {
    private OutputStream out;

    protected CopyInputStream(InputStream in, OutputStream out) {
      super(in);
      this.out = out;
    }
    
    @Override
    public int read() throws IOException {
      int b = super.read();
      if (b != -1) {
        this.out.write(b);
      }
      return b;
    }

    @Override
    public int read(byte [] b, int off, int len) throws IOException {
      int size = super.read(b, off, len);
      if (size != -1) {
        this.out.write(b, off, size);
      }
      return size;
    }
    
    @Override
    public void close() throws IOException {
      // Copy remaining bytes
      byte [] buffer = new byte [8192];
      int size; 
      while ((size = this.in.read(buffer)) != -1) {
        this.out.write(buffer, 0, size);
      }
      this.out.flush();
      this.out.close();
      super.close();
    }
  }
  
  /**
   * <code>ObjectInputStream</code> that replaces temporary <code>URLContent</code> 
   * objects by <code>URLContent</code> objects that points to file.
   */
  private class HomeObjectInputStream extends ObjectInputStream {
    private File zipFile;
    private List<Content> invalidContents;
    private List<URLContent> validContentsNotInPreferences;

    public HomeObjectInputStream(InputStream in, File zipFile) throws IOException {
      super(in);
      if (contentRecording != ContentRecording.INCLUDE_NO_CONTENT) {
        enableResolveObject(true);
        this.zipFile = zipFile;
        this.invalidContents = new ArrayList<Content>();
        this.validContentsNotInPreferences = new ArrayList<URLContent>();
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
            this.invalidContents.add(urlContent);
          } else {
            ContentDigestManager contentDigestManager = ContentDigestManager.getInstance();
            // Check if duplicated content can be avoided 
            // (coming from files older than version 4.4)
            for (URLContent content : this.validContentsNotInPreferences) {
              if (contentDigestManager.equals(urlContent, content)) {
                return content;
              }
            }
            checkCurrentThreadIsntInterrupted();
            if (preferencesContentsCache != null) {
              // Check if user preferences contains the same content to share it
              for (URLContent content : preferencesContentsCache) {
                if (contentDigestManager.equals(urlContent, content)) {
                  return content;
                }
              }
            }
            this.validContentsNotInPreferences.add(urlContent);
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
    public List<Content> getInvalidContents() {
      return this.invalidContents;
    }
  
  }
}