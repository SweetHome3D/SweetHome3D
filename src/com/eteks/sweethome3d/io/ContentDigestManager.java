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
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.tools.ResourceURLContent;
import com.eteks.sweethome3d.tools.URLContent;

/**
 * Manager able to compute and store content digest to compare content data quickly.  
 * @author Emmanuel Puybaret
 */
class ContentDigestManager {
  private static final byte [] INVALID_CONTENT_DIGEST = {};

  private static ContentDigestManager instance;
  
  private Map<URLContent, byte []> contentDigestsCache;
  private Map<URLContent, byte []> contentFirstBytesDigestsCache;
  
  private Map<URLContent, URL>     zipUrlsCache;
  private Map<URL, List<String>>   zipUrlEntriesCache;

  private ContentDigestManager() {
    this.contentDigestsCache = new WeakHashMap<URLContent, byte[]>();
    this.contentFirstBytesDigestsCache = new WeakHashMap<URLContent, byte[]>();
    this.zipUrlsCache = new WeakHashMap<URLContent, URL>();
    this.zipUrlEntriesCache = new WeakHashMap<URL, List<String>>();
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
   * Returns <code>true</code> if the contents in parameter contains the same data.
   */
  public synchronized boolean equals(URLContent content1, URLContent content2) {
    byte [] content1FirstBytesDigest = getContentFirstBytesDigest(content1);
    if (content1FirstBytesDigest == INVALID_CONTENT_DIGEST) {
      return false;
    } else if (!Arrays.equals(content1FirstBytesDigest, getContentFirstBytesDigest(content2))) {
      return false;
    } else {
      byte [] content1Digest = getContentDigest(content1);
      if (content1Digest == INVALID_CONTENT_DIGEST) {
        return false;
      } else {
        return Arrays.equals(content1Digest, getContentDigest(content2));
      }
    }
  }

  /**
   * Returns the digest of the max 1024 first bytes of the given <code>content</code>.
   */
  private byte [] getContentFirstBytesDigest(URLContent content) {
    byte [] firstBytesDigest = this.contentFirstBytesDigestsCache.get(content);
    if (firstBytesDigest == null) {
      firstBytesDigest = getContentDigest(content, 512);
      this.contentFirstBytesDigestsCache.put(content, firstBytesDigest);
    }
    return firstBytesDigest;
  }
  
  /**
   * Returns the digest of the given <code>content</code>. 
   */
  private byte [] getContentDigest(URLContent content) {
    byte [] digest = this.contentDigestsCache.get(content);
    if (digest == null) {
      try {
        if (content instanceof ResourceURLContent) {
          digest = getResourceContentDigest((ResourceURLContent)content);
        } else if (content instanceof URLContent
                   && ((URLContent)content).isJAREntry()) {
          URLContent urlContent = (URLContent)content;
          // If content comes from a home stream
          if (urlContent instanceof HomeURLContent) {
            digest = getHomeContentDigest((HomeURLContent)urlContent);            
          } else {
            digest = getZipContentDigest(urlContent);
          }
        } else {
          digest = getContentDigest(content, -1);
        }
      } catch (NoSuchAlgorithmException ex) {
        throw new InternalError("No SHA-1 message digest is available");
      } catch (IOException ex) {
        return INVALID_CONTENT_DIGEST;
      }
      this.contentDigestsCache.put(content, digest);
    }
    return digest;
  }

  /**
   * Returns the digest of the max <code>bytesCount</code> first bytes of this content content.
   */
  private byte [] getContentDigest(URLContent content, int bytesCount) {
    try {
      MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
      InputStream in = null;
      try {
        in = content.openStream();
        byte [] buffer = new byte [bytesCount != -1 ? Math.min(bytesCount, 8192) : 8192];
        int size; 
        while ((size = in.read(buffer)) != -1) {
          messageDigest.update(buffer, 0, size);
          if (bytesCount >= size) {
            break;
          }
        }
        return messageDigest.digest();
      } finally {
        if (in != null) {
          in.close();
        }
      }    
    } catch (NoSuchAlgorithmException ex) {
      throw new InternalError("No SHA-1 message digest is available");
    } catch (IOException ex) {
      return INVALID_CONTENT_DIGEST;
    }
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
          MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
          String entryDirectory = entryName.substring(0, lastSlashIndex + 1);
          for (String zipEntryName : getZipURLEntries(urlContent)) {
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
          return getContentDigest(urlContent, -1);
        }
      } else {
        // This should be the case only when resource isn't in a JAR file during development
        try {
          MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
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
      return getContentDigest(urlContent, -1);
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
      ZipInputStream zipIn = null;
      try {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
        zipIn = new ZipInputStream(zipUrl.openStream());
        for (String zipEntryName : getZipURLEntries(urlContent)) {
          if (zipEntryName.startsWith(entryDirectory) 
              && !zipEntryName.equals(entryDirectory)
              && isSignificant(zipEntryName)) {
            Content siblingContent = new URLContent(new URL("jar:" + zipUrl + "!/" 
                + URLEncoder.encode(zipEntryName, "UTF-8").replace("+", "%20")));
            updateMessageDigest(messageDigest, siblingContent);    
          }
        }
        return messageDigest.digest();
      } finally {
        if (zipIn != null) {
          zipIn.close();
        }
      }
    } else {
      return getContentDigest(urlContent, -1);
    }
  }

  /**
   * Returns the digest of the given zip content.
   */
  private byte [] getZipContentDigest(URLContent urlContent) throws IOException, NoSuchAlgorithmException {
    ZipInputStream zipIn = null;
    try {
      MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
      // Open zipped stream that contains urlContent
      zipIn = new ZipInputStream(urlContent.getJAREntryURL().openStream());
      for (ZipEntry entry; (entry = zipIn.getNextEntry()) != null; ) {
        String zipEntryName = entry.getName();
        if (isSignificant(zipEntryName)) {
          Content siblingContent = new URLContent(new URL("jar:" + urlContent.getJAREntryURL() + "!/" 
              + URLEncoder.encode(zipEntryName, "UTF-8").replace("+", "%20")));
          updateMessageDigest(messageDigest, siblingContent);
        }
      }
      return messageDigest.digest();
    } finally {
      if (zipIn != null) {
        zipIn.close();
      }
    }
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
  synchronized List<String> getZipURLEntries(URLContent urlContent) throws IOException {
    URL zipUrl = this.zipUrlsCache.get(urlContent);
    if (zipUrl != null) {
      return this.zipUrlEntriesCache.get(zipUrl); 
    } else {
      zipUrl = urlContent.getJAREntryURL(); 
      for (Map.Entry<URL, List<String>> entry : this.zipUrlEntriesCache.entrySet()) {
        if (zipUrl.equals(entry.getKey())) {
          this.zipUrlsCache.put(urlContent, entry.getKey());
          return entry.getValue();
        }
      }
      List<String> zipUrlEntries = new ArrayList<String>();
      ZipInputStream zipIn = null;
      try {
        // Search all entries of zip url
        zipIn = new ZipInputStream(zipUrl.openStream());
        for (ZipEntry entry; (entry = zipIn.getNextEntry()) != null; ) {
          zipUrlEntries.add(entry.getName());
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
      } finally {
        if (zipIn != null) {
          zipIn.close();
        }
      }
    }
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
}
