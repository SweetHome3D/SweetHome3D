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
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.tools.ResourceURLContent;
import com.eteks.sweethome3d.tools.SimpleURLContent;
import com.eteks.sweethome3d.tools.TemporaryURLContent;
import com.eteks.sweethome3d.tools.URLContent;

/**
 * An <code>OutputStream</code> filter that writes a home in a stream
 * at .sh3d file format.
 * @see DefaultHomeInputStream
 * @author Emmanuel Puybaret
 */
public class DefaultHomeOutputStream extends FilterOutputStream {
  private int              compressionLevel;
  private ContentRecording contentRecording;
  private boolean          serializedHome;
  private HomeXMLExporter  homeXmlExporter;

  /**
   * Creates a stream that will save a home and all the contents it references
   * in an uncompressed zip stream. Home data will be serialized in an entry named
   * <code>Home</code>.
   */
  public DefaultHomeOutputStream(OutputStream out) throws IOException {
    this(out, 0, false);
  }

  /**
   * Creates a stream that will save a home in a zip stream. Home data will be serialized
   * in an entry named <code>Home</code>.
   * @param compressionLevel 0-9
   * @param includeTemporaryContent if <code>true</code>, content instances of
   *            <code>TemporaryURLContent</code> and <code>SimpleURLContent</code> classes
   *            referenced by the saved home as well as the content previously saved with it
   *            will be written. If <code>false</code>, all the content instances
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
   * Creates a stream that will save a home in a zip stream. Home data will be serialized
   * in an entry named <code>Home</code>.
   * @param compressionLevel 0-9
   * @param contentRecording how content should be recorded with home.
   */
  public DefaultHomeOutputStream(OutputStream out,
                                 int          compressionLevel,
                                 ContentRecording contentRecording) throws IOException {
    this(out, compressionLevel, contentRecording, true, null);
  }

  /**
   * Creates a stream that will serialize a home in a zip stream. Home data will be serialized
   * in an entry named <code>Home</code> if <code>serializedHome</code> is <code>true</code>,
   * and saved in <code>Home.xml</code> entry at XML format if <code>homeXmlExporter</code> is not <code>null</code>.
   * @param compressionLevel 0-9
   * @param contentRecording specifies how content should be recorded with home
   * @param serializedHome if <code>true</code>, zip stream will include a <code>Home</code>
   *            entry containing the serialized home
   * @param homeXmlExporter  if not <code>null</code>, sets how a home will be saved
   *            in an additional <code>Home.xml</code> entry
   */
  public DefaultHomeOutputStream(OutputStream out,
                                 int          compressionLevel,
                                 ContentRecording contentRecording,
                                 boolean          serializedHome,
                                 HomeXMLExporter  homeXmlExporter) throws IOException {
    super(out);
    if (!serializedHome && homeXmlExporter == null) {
      throw new IllegalArgumentException("No entry specified for home data");
    }
    this.compressionLevel = compressionLevel;
    this.contentRecording = contentRecording;
    this.serializedHome = serializedHome;
    this.homeXmlExporter = homeXmlExporter;
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
    // Track content that must be saved in the zip stream with a dummy output stream
    HomeContentObjectsTracker contentTracker = new HomeContentObjectsTracker(new OutputStream() {
        @Override
        public void write(int b) throws IOException {
          // Don't write anything
        }
      });
    contentTracker.writeObject(home);
    Map<Content, String> savedContentNames = contentTracker.getSavedContentNames();

    if (this.serializedHome) {
      // Write home in the first entry named "Home"
      zipOut.putNextEntry(new ZipEntry("Home"));
      // Save home replacing Content objects if needed
      HomeObjectOutputStream objectOut = new HomeObjectOutputStream(zipOut, savedContentNames);
      objectOut.writeObject(home);
      objectOut.flush();
      zipOut.closeEntry();
    }

    if (this.homeXmlExporter != null) {
      // Write home at XML format in the second entry named "Home.xml"
      zipOut.putNextEntry(new ZipEntry("Home.xml"));
      // Save home replacing Content objects if needed
      XMLWriter xmlWriter = new XMLWriter(zipOut);
      this.homeXmlExporter.setSavedContentNames(savedContentNames);
      this.homeXmlExporter.writeElement(xmlWriter, home);
      xmlWriter.flush();
      zipOut.closeEntry();
    }

    if (savedContentNames.size() > 0) {
      Set<String> contentEntryNames = new HashSet<String>();
      // In the next entry named "ContentDigests", write content digests to help repair damaged files
      zipOut.putNextEntry(new ZipEntry("ContentDigests"));
      OutputStreamWriter writer = new OutputStreamWriter(zipOut, "UTF-8");
      ContentDigestManager digestManager = ContentDigestManager.getInstance();
      writer.write("ContentDigests-Version: 1.0\n\n");
      for (Map.Entry<Content, String> savedContent : savedContentNames.entrySet()) {
        String contentEntryName = savedContent.getValue();
        if (!contentEntryNames.contains(contentEntryName)) {
          contentEntryNames.add(contentEntryName);
          writer.write("Name: " + contentEntryName + "\n");
          writer.write("SHA-1-Digest: " + Base64.encodeBytes(digestManager.getContentDigest(savedContent.getKey())) + "\n\n");
        }
      }
      writer.flush();
      zipOut.closeEntry();

      // Write Content objects in additional zip entries
      contentEntryNames.clear();
      for (Map.Entry<Content, String> savedContent : savedContentNames.entrySet()) {
        String contentEntryName = savedContent.getValue();
        if (!contentEntryNames.contains(contentEntryName)) {
          contentEntryNames.add(contentEntryName);
          Content content = savedContent.getKey();
          int slashIndex = contentEntryName.indexOf('/');
          if (slashIndex > 0) {
            contentEntryName = contentEntryName.substring(0, slashIndex);
          }
          if (content instanceof ResourceURLContent) {
            writeResourceZipEntries(zipOut, contentEntryName, (ResourceURLContent)content);
          } else if (content instanceof URLContent
                     && !(content instanceof SimpleURLContent)
                     && ((URLContent)content).isJAREntry()) {
            URLContent urlContent = (URLContent)content;
            // If content comes from a home stream
            if (urlContent instanceof HomeURLContent) {
              writeHomeZipEntries(zipOut, contentEntryName, (HomeURLContent)urlContent);
            } else {
              writeZipEntries(zipOut, contentEntryName, urlContent);
            }
          } else {
            writeZipEntry(zipOut, contentEntryName, content);
          }
        }
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
        if (lastSlashIndex != -1) {
          // Consider content is a multi part resource only if it's in a subdirectory
          String entryDirectory = entryName.substring(0, lastSlashIndex + 1);
          // Write in home stream each zipped stream entry that is stored in the same directory
          for (ContentDigestManager.ZipEntryData zipEntry : ContentDigestManager.getInstance().getZipURLEntries(urlContent)) {
            String zipEntryName = zipEntry.getName();
            if (zipEntryName.startsWith(entryDirectory)) {
              Content siblingContent = new URLContent(new URL("jar:" + zipUrl + "!/"
                  + URLEncoder.encode(zipEntryName, "UTF-8").replace("+", "%20")));
              writeZipEntry(zipOut, entryNameOrDirectory + zipEntryName.substring(lastSlashIndex), siblingContent);
            }
          }
        } else {
          // Consider the content as not a multipart resource
          writeZipEntry(zipOut, entryNameOrDirectory, urlContent);
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
      for (ContentDigestManager.ZipEntryData zipEntry : ContentDigestManager.getInstance().getZipURLEntries(urlContent)) {
        String zipEntryName = zipEntry.getName();
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
    // Write in alphabetic order each zipped stream entry in home stream
    for (ContentDigestManager.ZipEntryData zipEntry : ContentDigestManager.getInstance().getZipURLEntries(urlContent)) {
      String zipEntryName = zipEntry.getName();
      Content siblingContent = new URLContent(new URL("jar:" + urlContent.getJAREntryURL() + "!/"
          + URLEncoder.encode(zipEntryName, "UTF-8").replace("+", "%20")));
      writeZipEntry(zipOut, directory + "/" + zipEntryName, siblingContent);
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
   * A dummy <code>ObjectOutputStream</code> that keeps track of the <code>Content</code>
   * objects of a home that should be saved.
   */
  private class HomeContentObjectsTracker extends ObjectOutputStream {
    private Map<Content, String> savedContentNames = new LinkedHashMap<Content, String>();
    private int savedContentIndex = 0;

    public HomeContentObjectsTracker(OutputStream out) throws IOException {
      super(out);
      if (contentRecording != ContentRecording.INCLUDE_NO_CONTENT) {
        enableReplaceObject(true);
      }
    }

    @Override
    protected Object replaceObject(Object obj) throws IOException {
      if (obj instanceof TemporaryURLContent
          || obj instanceof HomeURLContent
          || obj instanceof SimpleURLContent
          || (contentRecording == ContentRecording.INCLUDE_ALL_CONTENT && obj instanceof Content)) {
        String subEntryName = "";
        if (obj instanceof URLContent) {
          URLContent urlContent = (URLContent)obj;
          // Check if duplicated content can be avoided
          ContentDigestManager contentDigestManager = ContentDigestManager.getInstance();
          for (Map.Entry<Content, String> contentEntry : this.savedContentNames.entrySet()) {
            if (contentDigestManager.equals(urlContent, contentEntry.getKey())) {
              this.savedContentNames.put((Content)obj, contentEntry.getValue());
              return obj;
            }
          }
          checkCurrentThreadIsntInterrupted();
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
                int lastSlashIndex = entryName.lastIndexOf('/');
                if (lastSlashIndex != -1) {
                  // Consider content is a multi part resource only if it's in a subdirectory
                  subEntryName = entryName.substring(lastSlashIndex);
                }
              }
            } else if (!(urlContent instanceof SimpleURLContent)) {
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

        // Build a relative URL that points to content object
        String homeContentPath = this.savedContentIndex++ + subEntryName;
        this.savedContentNames.put((Content)obj, homeContentPath);
      }
      return obj;
    }

    /**
     * Returns the names of the home contents to be saved.
     */
    public Map<Content, String> getSavedContentNames() {
      return this.savedContentNames;
    }
  }

  /**
   * <code>ObjectOutputStream</code> that replaces <code>Content</code> objects
   * by temporary <code>URLContent</code> objects and stores them in a list.
   */
  private class HomeObjectOutputStream extends ObjectOutputStream {
    private Map<Content, String>    savedContentNames;
    private Map<String, URLContent> replacedContents = new HashMap<String, URLContent>();

    public HomeObjectOutputStream(OutputStream out,
                                  Map<Content, String> savedContentNames) throws IOException {
      super(out);
      this.savedContentNames = savedContentNames;
      if (contentRecording != ContentRecording.INCLUDE_NO_CONTENT) {
        enableReplaceObject(true);
      }
    }

    @Override
    protected Object replaceObject(Object obj) throws IOException {
      if (obj instanceof Content) {
        String savedContentName = this.savedContentNames.get((Content)obj);
        if (savedContentName != null) {
          checkCurrentThreadIsntInterrupted();
          // Ensure that the duplicated content share the same replaced URLContent instance
          URLContent replacedContent = this.replacedContents.get(savedContentName);
          if (replacedContent == null) {
            replacedContent = new URLContent(new URL("jar:file:temp!/" + savedContentName));
            this.replacedContents.put(savedContentName, replacedContent);
          }
          return replacedContent;
        }
      }
      return obj;
    }
  }
}