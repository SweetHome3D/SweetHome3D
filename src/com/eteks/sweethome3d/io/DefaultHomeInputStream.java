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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.tools.URLContent;

/**
 * An <code>InputStream</code> filter that reads a home from a stream 
 * at .sh3d file format. 
 * @see DefaultHomeOutputStream
 * @author Emmanuel Puybaret
 */
public class DefaultHomeInputStream extends FilterInputStream {
  private final ContentRecording   contentRecording;
  private final HomeXMLHandler     xmlHandler;
  private final UserPreferences    preferences;
  private final boolean            preferPreferencesContent;
  
  private File file;

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
    this(in, contentRecording, null, false);
  }

  /**
   * Creates a home input stream filter able to read a home and its content
   * from <code>in</code>. If <code>preferences</code> isn't <code>null</code>
   * and <code>preferPreferencesContent</code> is <code>true</code>, 
   * the furniture and textures contents it references will replace the one of 
   * the read home when they are equal. If <code>preferPreferencesContent</code> 
   * is <code>false</code>, preferences content will be used only 
   * to replace damaged equal content that might be found in read home files.
   */
  public DefaultHomeInputStream(InputStream in, 
                                ContentRecording contentRecording, 
                                UserPreferences preferences,
                                boolean preferPreferencesContent) {
    this(in, contentRecording, null, preferences, preferPreferencesContent);
  }

  /**
   * Creates a home input stream filter able to read a home and its content
   * from <code>in</code>. 
   * @param in  the zipped stream from which the home will be read
   * @param contentRecording  specifies whether content referenced by the read home is included 
   *            or not in the stream.
   * @param xmlHandler  SAX handler used to parse <code>Home.xml</code> entry when present, or 
   *            <code>null</code> if only <code>Home</code> entry should taken into account.
   * @param preferences  if not <code>null</code> and <code>preferPreferencesContent</code> 
   *            is <code>true</code>, the furniture and textures contents it references will 
   *            replace the one of the read home when they are equal. 
   *            If <code>preferPreferencesContent</code> is <code>false</code>, preferences 
   *            content will be used only to replace damaged equal content that might be found 
   *            in read home files.
   * @param preferPreferencesContent if <code>true</code>, the returned home will reference 
   *            contents in preferences when equal. 
   */
  public DefaultHomeInputStream(InputStream in, 
                                ContentRecording contentRecording,
                                HomeXMLHandler xmlHandler,
                                UserPreferences preferences,
                                boolean preferPreferencesContent) {
    super(in);
    this.contentRecording = contentRecording;
    this.xmlHandler = xmlHandler;
    this.preferences = preferences;
    this.preferPreferencesContent = preferPreferencesContent;
  }

  /**
   * Creates a home input stream filter able to read a home and its content the given <code>file</code>. 
   * The file will be read directly without using a temporary copy except if it contains some invalid entries. 
   * @param file  the zipped file from which the home will be read
   * @param contentRecording  specifies whether content referenced by the read home is included 
   *            or not in the stream.
   * @param xmlHandler  SAX handler used to parse <code>Home.xml</code> entry when present, or 
   *            <code>null</code> if only <code>Home</code> entry should taken into account.
   * @param preferences  if not <code>null</code> and <code>preferPreferencesContent</code> 
   *            is <code>true</code>, the furniture and textures contents it references will 
   *            replace the one of the read home when they are equal. 
   *            If <code>preferPreferencesContent</code> is <code>false</code>, preferences 
   *            content will be used only to replace damaged equal content that might be found 
   *            in read home files.
   * @param preferPreferencesContent if <code>true</code>, the returned home will reference 
   *            contents in preferences when equal. 
   * @throws FileNotFoundException if the given file can't be opened
   */
  public DefaultHomeInputStream(File file, 
                                ContentRecording contentRecording,
                                HomeXMLHandler xmlHandler,
                                UserPreferences preferences,
                                boolean preferPreferencesContent) throws FileNotFoundException {
    super(new FileInputStream(file));
    this.file = file;
    this.contentRecording = contentRecording;
    this.xmlHandler = xmlHandler;
    this.preferences = preferences;
    this.preferPreferencesContent = preferPreferencesContent;
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
    boolean validZipFile = true;
    HomeContentContext contentContext = null;
    if (this.contentRecording != ContentRecording.INCLUDE_NO_CONTENT) {
      InputStream homeIn;
      if (this.file == null) {
        // Copy home stream in a temporary file  
        this.file = OperatingSystem.createTemporaryFile("open", ".sweethome3d");
        OutputStream fileCopyOut = new BufferedOutputStream(new FileOutputStream(this.file));
        homeIn = new CopiedInputStream(new BufferedInputStream(this.in), fileCopyOut);
      } else {
        homeIn = this.in;
      }
      // Check if all entries in the home file can be fully read using a zipped input stream
      List<ZipEntry> validEntries = new ArrayList<ZipEntry>();
      validZipFile = isZipFileValidUsingInputStream(homeIn, validEntries) && validEntries.size() > 0;
      if (!validZipFile) {
        int validEntriesCount = validEntries.size();
        validEntries.clear();
        // Check how many entries can be read using zip dictionary
        // (some times, this gives a different result from the previous way)
        // and create a temporary copy with only valid entries
        isZipFileValidUsingDictionnary(this.file, validEntries);
        if (validEntries.size() > validEntriesCount) {
          this.file = createTemporaryFileFromValidEntries(this.file, validEntries);
        } else {
          this.file = createTemporaryFileFromValidEntriesCount(this.file, validEntriesCount);
        }
      } 
      contentContext = new HomeContentContext(this.file.toURI().toURL(),
          this.preferences, this.preferPreferencesContent);
    }
    
    boolean homeEntry = false;
    boolean homeXmlEntry = false;
    // Open a zip input from file
    ZipInputStream zipIn = new ZipInputStream(this.contentRecording == ContentRecording.INCLUDE_NO_CONTENT
        ? this.in : new FileInputStream(this.file));
    ZipEntry entry = null;
    try {
      // Find whether Home and Home.xml entries exist
      while ((entry = zipIn.getNextEntry()) != null) {
        if ("Home".equals(entry.getName())) {
          homeEntry = true;
        } else if (this.xmlHandler != null 
                  && "Home.xml".equals(entry.getName())) {
          homeXmlEntry = true;
        }
        
        if (this.contentRecording == ContentRecording.INCLUDE_NO_CONTENT) {
          // Stop at the first entry from which home can be read
          if (homeEntry || homeXmlEntry) {
            break;
          }
        } else {
          // Stop once all entries from which home can be read are found 
          if (homeEntry && homeXmlEntry) {
            break;
          }
        }
      }
      
      checkCurrentThreadIsntInterrupted();
      if (!homeEntry && !homeXmlEntry) {
        throw new IOException("Missing entry \"Home\" or \"Home.xml\"");
      }

      if (this.contentRecording != ContentRecording.INCLUDE_NO_CONTENT) {
        // Reset stream on the home entry
        zipIn.close();
        zipIn = new ZipInputStream(new FileInputStream(this.file));
        do {
          entry = zipIn.getNextEntry();
        } while (homeXmlEntry && !"Home.xml".equals(entry.getName()) 
            || !homeXmlEntry && !"Home".equals(entry.getName()));
      }
      
      // Read Home entry
      checkCurrentThreadIsntInterrupted();
      Home home;
      if ("Home".equals(entry.getName())) {
        // Use an ObjectInputStream that replaces temporary URLs of Content objects 
        // by URLs relative to file 
        HomeObjectInputStream objectStream = new HomeObjectInputStream(zipIn, contentContext);
        home = (Home)objectStream.readObject();
      } else {
        try {
          SAXParserFactory factory = SAXParserFactory.newInstance();
          SAXParser saxParser = factory.newSAXParser();
          this.xmlHandler.setContentContext(contentContext);
          saxParser.parse(zipIn, this.xmlHandler);
          home = this.xmlHandler.getHome();
        } catch (ParserConfigurationException ex) {
          IOException ex2 = new IOException("Can't parse home XML stream");
          ex2.initCause(ex);
          throw ex2;
        } catch (SAXException ex) {
          IOException ex2 = new IOException("Can't parse home XML stream");
          ex2.initCause(ex);
          throw ex2;
        }
      }
      // Check all content is valid
      if (contentContext != null && (!validZipFile || contentContext.containsInvalidContents())) {
        if (contentContext.containsCheckedContents()) { 
          home.setRepaired(true);
        } else {
          throw new DamagedHomeIOException(home, contentContext.getInvalidContents());
        }
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
      zipIn = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)));
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
   * An input stream filter that copies to a given output stream all read data.
   */
  private class CopiedInputStream extends FilterInputStream {
    private OutputStream out;

    protected CopiedInputStream(InputStream in, OutputStream out) {
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
      try {
        // Copy remaining bytes
        byte [] buffer = new byte [8192];
        int size; 
        while ((size = this.in.read(buffer)) != -1) {
          this.out.write(buffer, 0, size);
        }
        this.out.flush();
      } finally {
        this.out.close();
        super.close();
      }
    }
  }
  
  /**
   * <code>ObjectInputStream</code> that replaces temporary <code>URLContent</code> 
   * objects by <code>URLContent</code> objects that points to file.
   */
  private class HomeObjectInputStream extends ObjectInputStream {
    private HomeContentContext contentContext;

    public HomeObjectInputStream(InputStream in, 
                                 HomeContentContext contentContext) throws IOException {
      super(in);
      if (contentRecording != ContentRecording.INCLUDE_NO_CONTENT) {
        enableResolveObject(true);
        this.contentContext = contentContext;
      }
    }
    
    @Override
    protected Object resolveObject(Object obj) throws IOException {
      if (obj instanceof URLContent) {
        String url = ((URLContent)obj).getURL().toString();
        if (url.startsWith("jar:file:temp!/")) {
          // Replace "temp" in URL and lookup content in read file or preferences resources
          return this.contentContext.lookupContent(url.substring(url.indexOf('!') + 2));
        }
      } 
      return obj;
    }
  }
}