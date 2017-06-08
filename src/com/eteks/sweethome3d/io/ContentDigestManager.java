/*
 * ContentDigestManager.java 23 mai 2014
 *
 * Sweet Home 3D, Copyright (c) 2014 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.tools.ResourceURLContent;
import com.eteks.sweethome3d.tools.SimpleURLContent;
import com.eteks.sweethome3d.tools.URLContent;

/**
 * Manager able to store and compute content digest to compare content data faster.  
 * @author Emmanuel Puybaret
 */
public class ContentDigestManager {
  private static final String  DIGEST_ALGORITHM = "SHA-1";
  private static final byte [] INVALID_CONTENT_DIGEST = {};

  private static ContentDigestManager instance;
  
  private Map<Content, byte []>  contentDigestsCache;
  
  private Map<URLContent, URL>   zipUrlsCache;
  private Map<URL, List<ZipEntryData>> zipUrlEntriesCache;

  private ContentDigestManager() {
    this.contentDigestsCache = new WeakHashMap<Content, byte[]>();
    this.zipUrlsCache = new WeakHashMap<URLContent, URL>();
    this.zipUrlEntriesCache = new WeakHashMap<URL, List<ZipEntryData>>();
  }
  
  /**
   * Returns an instance of this singleton. 
   */
  public static ContentDigestManager getInstance() {
    if (instance == null) {
      synchronized (ContentDigestManager.class) {
        if (instance == null) {
          instance = new ContentDigestManager();
        }
      }
    }
    return instance;
  }

  /**
   * Returns <code>true</code> if the contents in parameter contains the same data,
   * comparing their digest. If the digest of the contents was not 
   * {@linkplain #setContentDigest(Content, byte[]) set} directly, it will be 
   * computed on the fly.
   */
  public boolean equals(Content content1, Content content2) {
    byte [] content1Digest = getContentDigest(content1);
    if (content1Digest == INVALID_CONTENT_DIGEST) {
      return false;
    } else {
      return Arrays.equals(content1Digest, getContentDigest(content2));
    }
  }

  /**
   * Returns <code>true</code> if the digest of the given <code>content</code>
   * is equal to <code>digest</code>.
   */
  public boolean isContentDigestEqual(Content content, byte [] digest) {
    byte [] contentDigest = getContentDigest(content);
    if (contentDigest == INVALID_CONTENT_DIGEST) {
      return false;
    } else {
      return Arrays.equals(contentDigest, digest);
    }
  }

  /**
   * Sets the SHA-1 digest of the given <code>content</code>.
   */
  public synchronized void setContentDigest(Content content, byte [] digest) {
    this.contentDigestsCache.put(content, digest);
  }
  
  /**
   * Returns the SHA-1 digest of the given <code>content</code>, computing it 
   * if it wasn't set.
   */
  public synchronized byte [] getContentDigest(Content content) {
    byte [] digest = this.contentDigestsCache.get(content);
    if (digest == null) {
      try {
        if (content instanceof ResourceURLContent) {
          digest = getResourceContentDigest((ResourceURLContent)content);
        } else if (content instanceof URLContent
                   && !(content instanceof SimpleURLContent)
                   && ((URLContent)content).isJAREntry()) {
          URLContent urlContent = (URLContent)content;
          // If content comes from a home stream
          if (urlContent instanceof HomeURLContent) {
            digest = getHomeContentDigest((HomeURLContent)urlContent);            
          } else {
            digest = getZipContentDigest(urlContent);
          }
        } else {
          digest = computeContentDigest(content);
        }
      } catch (NoSuchAlgorithmException ex) {
        throw new InternalError("No SHA-1 message digest is available");
      } catch (IOException ex) {
        digest = INVALID_CONTENT_DIGEST;
      }
      this.contentDigestsCache.put(content, digest);
    }
    return digest;
  }

  /**
   * Returns the digest of a content coming from a resource file.
   */
  private byte [] getResourceContentDigest(ResourceURLContent urlContent) throws IOException, NoSuchAlgorithmException {
    if (urlContent.isMultiPartResource()) {
      if (urlContent.isJAREntry()) {
        URL zipUrl = urlContent.getJAREntryURL();
        String entryName = urlContent.getJAREntryName();
        int lastSlashIndex = entryName.lastIndexOf('/');
        if (lastSlashIndex != -1) {
          // Consider content is a multi part resource only if it's in a subdirectory
          MessageDigest messageDigest = MessageDigest.getInstance(DIGEST_ALGORITHM);
          String entryDirectory = entryName.substring(0, lastSlashIndex + 1);
          for (ZipEntryData zipEntry : getZipURLEntries(urlContent)) {
            String zipEntryName = zipEntry.getName();
            if (zipEntryName.startsWith(entryDirectory) 
                && !zipEntryName.equals(entryDirectory)
                && isSignificant(zipEntryName)) {
              Content siblingContent = new URLContent(new URL("jar:" + zipUrl + "!/" 
                  + URLEncoder.encode(zipEntryName, "UTF-8").replace("+", "%20")));
              updateMessageDigest(messageDigest, siblingContent);
            }
          }
          return messageDigest.digest();
        } else {
          // Consider the content as not a multipart resource
          return computeContentDigest(urlContent);
        }
      } else {
        // This should be the case only when resource isn't in a JAR file during development
        try {
          MessageDigest messageDigest = MessageDigest.getInstance(DIGEST_ALGORITHM);
          File contentFile = new File(urlContent.getURL().toURI());
          File parentFile = new File(contentFile.getParent());
          File [] siblingFiles = parentFile.listFiles();
          // Sort files to ensure content files are always listed in the same order
          Arrays.sort(siblingFiles);
          for (File siblingFile : siblingFiles) {
            if (!siblingFile.isDirectory()) {
              updateMessageDigest(messageDigest, new URLContent(siblingFile.toURI().toURL()));
            }
          }
          return messageDigest.digest();
        } catch (URISyntaxException ex) {
          IOException ex2 = new IOException();
          ex2.initCause(ex);
          throw ex2;
        }
      }
    } else {
      return computeContentDigest(urlContent);
    }
  }

  /**
   * Returns the digest of a content coming from a home file.
   */
  private byte [] getHomeContentDigest(HomeURLContent urlContent) throws IOException, NoSuchAlgorithmException {
    String entryName = urlContent.getJAREntryName();
    int slashIndex = entryName.indexOf('/');
    // If content comes from a directory of a home file
    if (slashIndex > 0) {
      URL zipUrl = urlContent.getJAREntryURL();
      String entryDirectory = entryName.substring(0, slashIndex + 1);
      MessageDigest messageDigest = MessageDigest.getInstance(DIGEST_ALGORITHM);
      for (ZipEntryData zipEntry : getZipURLEntries(urlContent)) {
        String zipEntryName = zipEntry.getName();
        if (zipEntryName.startsWith(entryDirectory) 
            && !zipEntryName.equals(entryDirectory)
            && isSignificant(zipEntryName)) {
          Content siblingContent = new URLContent(new URL("jar:" + zipUrl + "!/" 
              + URLEncoder.encode(zipEntryName, "UTF-8").replace("+", "%20")));
          updateMessageDigest(messageDigest, siblingContent);    
        }
      }
      return messageDigest.digest();
    } else {
      return computeContentDigest(urlContent);
    }
  }

  /**
   * Returns the digest of the given zip content.
   */
  private byte [] getZipContentDigest(URLContent urlContent) throws IOException, NoSuchAlgorithmException {
    MessageDigest messageDigest = MessageDigest.getInstance(DIGEST_ALGORITHM);
    // Open zipped stream that contains urlContent
    for (ZipEntryData zipEntry : ContentDigestManager.getInstance().getZipURLEntries(urlContent)) {
      if (isSignificant(zipEntry.getName())) {
        Content siblingContent = new URLContent(new URL("jar:" + urlContent.getJAREntryURL() + "!/" 
            + URLEncoder.encode(zipEntry.getName(), "UTF-8").replace("+", "%20")));
        updateMessageDigest(messageDigest, siblingContent);
      }
    }
    return messageDigest.digest();
  }
  
  /**
   * Returns <code>true</code> if entry name is significant to distinguish 
   * the data of a content from an other one.
   */
  private boolean isSignificant(String entryName) {
    // Ignore LICENSE.TXT files
    String entryNameUpperCase = entryName.toUpperCase();
    return !entryNameUpperCase.equals("LICENSE.TXT") 
          && !entryNameUpperCase.endsWith("/LICENSE.TXT");
  }

  /**
   * Returns the list of entries contained in <code>zipUrl</code>.
   */
  synchronized List<ZipEntryData> getZipURLEntries(URLContent urlContent) throws IOException {
    URL zipUrl = this.zipUrlsCache.get(urlContent);
    if (zipUrl != null) {
      return this.zipUrlEntriesCache.get(zipUrl); 
    } else {
      zipUrl = urlContent.getJAREntryURL(); 
      for (Map.Entry<URL, List<ZipEntryData>> entry : this.zipUrlEntriesCache.entrySet()) {
        if (zipUrl.equals(entry.getKey())) {
          this.zipUrlsCache.put(urlContent, entry.getKey());
          return entry.getValue();
        }
      }
      List<ZipEntryData> zipUrlEntries = new ArrayList<ZipEntryData>();
      if (zipUrl.getProtocol().equals("file")) {
        // Prefer to retrieve entries in zip files with ZipFile class because it runs much faster
        ZipFile zipFile = null;
        try {
          zipFile = new ZipFile(new File(zipUrl.toURI()));
          for (Enumeration<? extends ZipEntry> enumEntries = zipFile.entries(); enumEntries.hasMoreElements(); ) {
            ZipEntry entry = enumEntries.nextElement();
            zipUrlEntries.add(new ZipEntryData(entry.getName(), entry.getSize()));
          }
        } catch (URISyntaxException ex) {
          IOException ex2 = new IOException("Can't retrieve zip file");
          ex2.initCause(ex);
          throw ex2;
        } finally {
          if (zipFile != null) {
            zipFile.close();
          }
        }
      } else {
        ZipInputStream zipIn = null;
        try {
          // Search all entries of zip url
          zipIn = new ZipInputStream(zipUrl.openStream());
          for (ZipEntry entry; (entry = zipIn.getNextEntry()) != null; ) {
            long size = entry.getSize(); 
            if (size == -1) {
              size = 0;
              byte [] bytes = new byte [8192];
              for (int length; (length = zipIn.read(bytes)) != -1; ) {
                size += length;
              }
            }
            zipUrlEntries.add(new ZipEntryData(entry.getName(), size));
          }
        } finally {
          if (zipIn != null) {
            zipIn.close();
          }
        }
      }
      
      // Sort entries to ensure the files of multi part content are always listed 
      // in the same order whatever its source
      Collections.sort(zipUrlEntries);
      // Store retrieved entries in the map with a URL key  
      this.zipUrlEntriesCache.put(zipUrl, zipUrlEntries);
      // Store URL in a map with keys that will be referenced as long as they are needed in the program
      // This second map allows to use a weak hash map for zipUrlEntriesCache that will be cleaned
      // only once all the URLContent objects sharing a same URL are not used anymore 
      this.zipUrlsCache.put(urlContent, zipUrl);
      return zipUrlEntries;
    }
  }

  /**
   * Returns the digest of the given <code>content</code>.
   */
  private byte [] computeContentDigest(Content content) throws IOException, NoSuchAlgorithmException {
    MessageDigest messageDigest = MessageDigest.getInstance(DIGEST_ALGORITHM);
    updateMessageDigest(messageDigest, content);
    return messageDigest.digest();
  }

  /**
   * Updates message digest with the data of the given <code>content</code>.
   */
  private void updateMessageDigest(MessageDigest messageDigest, Content content) throws IOException {
    InputStream in = null;
    try {
      in = content.openStream();
      byte [] buffer = new byte [8192];
      int size; 
      while ((size = in.read(buffer)) != -1) {
        messageDigest.update(buffer, 0, size);
      }
    } finally {
      if (in != null) {
        in.close();
      }
    }
  }
  
  /**
   * Returns the size of the given <code>content</code>.
   */
  public synchronized Long getContentSize(Content content) {
    try {
      if (content instanceof ResourceURLContent) {
        return getResourceContentSize((ResourceURLContent)content);
      } else if (content instanceof URLContent) {
        URLContent urlContent = (URLContent)content;
        // If content comes from a home stream and isn't a SimpleURLContent instance
        if (urlContent.isJAREntry()
            && urlContent instanceof HomeURLContent) {
          return getHomeContentSize((HomeURLContent)urlContent);            
        } else {
          return urlContent.getSize();
        }
      } else {
        return null;
      }
    } catch (IOException ex) {
      return null;
    }
  }

  /**
   * Returns the size of a content coming from a resource file.
   */
  private long getResourceContentSize(ResourceURLContent urlContent) throws IOException {
    if (urlContent.isMultiPartResource()) {
      if (urlContent.isJAREntry()) {
        String entryName = urlContent.getJAREntryName();
        int lastSlashIndex = entryName.lastIndexOf('/');
        List<ZipEntryData> zipEntries = getZipURLEntries(urlContent);
        if (lastSlashIndex != -1) {
          // Consider content is a multi part resource only if it's in a subdirectory
          String entryDirectory = entryName.substring(0, lastSlashIndex + 1);
          long size = 0;
          for (ZipEntryData zipEntry : zipEntries) {
            if (zipEntry.getName().startsWith(entryDirectory)) {
              size += zipEntry.getSize();    
            }
          }
          return size;
        } else {
          // Consider the content as not a multipart resource
          int index = Collections.binarySearch(zipEntries, new ZipEntryData(urlContent.getJAREntryName()));
          return zipEntries.get(index).getSize();
        }
      } else {
        // This should be the case only when resource isn't in a JAR file during development
        try {
          File contentFile = new File(urlContent.getURL().toURI());
          File parentFile = new File(contentFile.getParent());
          File [] siblingFiles = parentFile.listFiles();
          long size = 0;
          for (File siblingFile : siblingFiles) {
            if (!siblingFile.isDirectory()) {
              size += siblingFile.length();
            }
          }
          return size;
        } catch (URISyntaxException ex) {
          IOException ex2 = new IOException();
          ex2.initCause(ex);
          throw ex2;
        }
      }
    } else {
      if (urlContent.isJAREntry()) {
        List<ZipEntryData> zipEntries = getZipURLEntries(urlContent);
        int index = Collections.binarySearch(zipEntries, new ZipEntryData(urlContent.getJAREntryName()));
        return zipEntries.get(index).getSize();
      } else {
        return new SimpleURLContent(urlContent.getURL()).getSize();
      }
    }
  }

  /**
   * Returns the size of a content coming from a home file.
   */
  private long getHomeContentSize(HomeURLContent urlContent) throws IOException {
    String entryName = urlContent.getJAREntryName();
    List<ZipEntryData> zipEntries = getZipURLEntries(urlContent);
    int slashIndex = entryName.indexOf('/');
    // If content comes from a directory of a home file
    if (slashIndex > 0) {
      String entryDirectory = entryName.substring(0, slashIndex + 1);
      long size = 0;
      for (ZipEntryData zipEntry : getZipURLEntries(urlContent)) {
        if (zipEntry.getName().startsWith(entryDirectory)) {
          size += zipEntry.getSize();    
        }
      }
      return size;
    } else {
      int index = Collections.binarySearch(zipEntries, new ZipEntryData(urlContent.getJAREntryName()));
      return zipEntries.get(index).getSize();
    }
  }
  
  /**
   * A simplified zip entry.
   */
  static class ZipEntryData implements Comparable<ZipEntryData> {
    private String name;
    private long   size;
    
    private ZipEntryData(String name) {
      this(name, -1);
    }
    
    private ZipEntryData(String name, long size) {
      this.name = name;
      this.size = size;
    }
    
    public String getName() {
      return this.name;
    }
    
    public long getSize() {
      return this.size;
    }

    public int compareTo(ZipEntryData entry) {
      return this.name.compareTo(entry.name);
    }
  }
}
