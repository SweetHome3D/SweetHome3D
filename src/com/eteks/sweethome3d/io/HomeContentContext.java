/*
 * HomeContentContext.java 30 sept 2016
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
package com.eteks.sweethome3d.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.CatalogTexture;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.FurnitureCategory;
import com.eteks.sweethome3d.model.TexturesCategory;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.URLContent;

/**
 * Context used to manage content in a home stream.
 * @author Emmanuel Puybaret
 */
class HomeContentContext {
  private URL                      homeUrl;
  private boolean                  containsInvalidContents;
  private List<Content>            invalidContents;
  private List<URLContent>         validContentsNotInPreferences;

  private Map<URLContent, byte []> contentDigests;
  private Set<URLContent>          preferencesContentsCache;
  private boolean                  preferPreferencesContent;
  
  public HomeContentContext(URL homeSource,
                            UserPreferences preferences,
                            boolean preferPreferencesContent) {
    this.homeUrl = homeSource;
    this.preferPreferencesContent = preferPreferencesContent;
    this.contentDigests = readContentDigests(homeSource);
    this.invalidContents = new ArrayList<Content>();
    this.validContentsNotInPreferences = new ArrayList<URLContent>();
    if (preferences != null 
        && this.preferencesContentsCache == null) {
      this.preferencesContentsCache = getUserPreferencesContent(preferences);
    }
  }
  
  /**
   * Returns the digest of content contained in the given home, or 
   * <code>null</code> if this information doesn't exist in the home file.
   */
  private Map<URLContent, byte []> readContentDigests(URL homeUrl) {
    ZipInputStream zipIn = null;
    try {
      zipIn = new ZipInputStream(homeUrl.openStream());
      // Read the content of the entry named "ContentDigests" if it exists
      ZipEntry entry = null;
      while ((entry = zipIn.getNextEntry()) != null) {
        if ("ContentDigests".equals(entry.getName())) {
          BufferedReader reader = new BufferedReader(new InputStreamReader(zipIn, "UTF-8"));
          String line = reader.readLine();
          if (line != null
              && line.trim().startsWith("ContentDigests-Version: 1")) {
            Map<URLContent, byte []> contentDigests = new HashMap<URLContent, byte[]>();
            // Read Name / SHA-1-Digest lines  
            String entryName = null;
            while ((line = reader.readLine()) != null) {
              if (line.startsWith("Name:")) {
                entryName = line.substring("Name:".length()).trim();
              } else if (line.startsWith("SHA-1-Digest:")) {
                byte [] digest = Base64.decode(line.substring("SHA-1-Digest:".length()).trim());
                if (entryName == null) {
                  throw new IOException("Missing entry name");
                } else {
                  URL url = new URL("jar:" + homeUrl + "!/" + entryName);
                  contentDigests.put(new HomeURLContent(url), digest);
                  entryName = null;
                }
              }
            }
            return contentDigests;
          }
        }
      }
    } catch (IOException ex) {
      // Ignore issues in ContentDigests (this entry exists only from version 4.4)
    } finally {
      if (zipIn != null) {
        try {
          zipIn.close();
        } catch (IOException ex) {
        }
      }
    }
    return null;
  }

  /**
   * Returns the {@link Content} instance matching the given entry name in home stream.
   */
  public Content lookupContent(String contentEntryName) throws IOException {
    URL fileURL = new URL("jar:" + this.homeUrl + "!/" + contentEntryName);
    HomeURLContent urlContent = new HomeURLContent(fileURL);
    ContentDigestManager contentDigestManager = ContentDigestManager.getInstance();
    if (!isValid(urlContent)) {
      this.containsInvalidContents = true;
      // Try to find in user preferences a content with the same digest 
      // and repair silently damaged entry 
      URLContent preferencesContent = findUserPreferencesContent(urlContent);
      if (preferencesContent != null) {
        return preferencesContent;
      } else {
        this.invalidContents.add(urlContent);
      }
    } else {
      // Check if duplicated content can be avoided 
      // (coming from files older than version 4.4)
      for (URLContent content : this.validContentsNotInPreferences) {
        if (contentDigestManager.equals(urlContent, content)) {
          return content;
        }
      }
      if (Thread.interrupted()) {
        throw new InterruptedIOException();
      }
      // If content digests information is available, check the digest against read content 
      byte [] contentDigest;
      if (this.contentDigests != null
          && (contentDigest = this.contentDigests.get(urlContent)) != null
          && !contentDigestManager.isContentDigestEqual(urlContent, contentDigest)) {
        this.containsInvalidContents = true;
        // Try to find in user preferences a content with the same digest  
        URLContent preferencesContent = findUserPreferencesContent(urlContent);
        if (preferencesContent != null) {
          return preferencesContent;
        } else {
          this.invalidContents.add(urlContent);
        }
      } else {
        if (this.preferencesContentsCache != null
            && this.preferPreferencesContent) {
          // Check if user preferences contains the same content to share it
          for (URLContent preferencesContent : this.preferencesContentsCache) {
            if (contentDigestManager.equals(urlContent, preferencesContent)) {
              return preferencesContent;
            }
          }
        }
        this.validContentsNotInPreferences.add(urlContent);
      }
    }
    return urlContent;
  }

  /**
   * Returns <code>true</code> if the given <code>content</code> exists.
   */
  private boolean isValid(Content content) {
    try {
      InputStream in = content.openStream();
      try {
        in.close();
        return true;
      } catch (NullPointerException e) {
      }
    } catch (IOException e) {
    }
    return false;
  }

  /**
   * Returns <code>true</code> if all contents is valid whether it was replaced or correct.
   */
  public boolean containsCheckedContents() {
    return this.contentDigests != null && this.invalidContents.size() == 0;
  }

  /**
   * Returns <code>true</code> if the stream contains some invalid content 
   * whether it could be replaced or not.
   */
  public boolean containsInvalidContents() {
    return this.containsInvalidContents;
  }
  
  /**
   * Returns the content in user preferences with the same digest as the
   * given <code>content</code>, or <code>null</code> if it doesn't exist.
   */
  private URLContent findUserPreferencesContent(URLContent content) {
    if (this.contentDigests != null
        && this.preferencesContentsCache != null) {
      byte [] contentDigest = this.contentDigests.get(content);
      if (contentDigest != null) {
        ContentDigestManager contentDigestManager = ContentDigestManager.getInstance();
        for (URLContent preferencesContent : this.preferencesContentsCache) {
          if (contentDigestManager.isContentDigestEqual(preferencesContent, contentDigest)) {
            return preferencesContent;
          }
        }
      }
    }
    return null;
  }
  
  /**
   * Returns the list of invalid content found during lookup.
   */
  public List<Content> getInvalidContents() {
    return Collections.unmodifiableList(this.invalidContents);
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
}