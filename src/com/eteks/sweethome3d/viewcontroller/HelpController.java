/*
 * HelpController.java 20 juil. 07
 *
 * Copyright (c) 2007 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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
package com.eteks.sweethome3d.viewcontroller;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTML.Tag;

import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.ResourceURLContent;

/**
 * A MVC controller for Sweet Home 3D help view.
 * @author Emmanuel Puybaret
 */
public class HelpController implements Controller {
  /**
   * The properties that may be edited by the view associated to this controller. 
   */
  public enum Property {HELP_PAGE, BROWSER_PAGE, 
      PREVIOUS_PAGE_ENABLED, NEXT_PAGE_ENABLED}
  
  private final UserPreferences       preferences;
  private final ViewFactory           viewFactory;
  private final PropertyChangeSupport propertyChangeSupport;
  private final List<URL>             history;
  private int                         historyIndex;
  private HelpView                    helpView;
  
  private URL helpPage;
  private URL browserPage;
  private boolean previousPageEnabled;
  private boolean nextPageEnabled;

  
  public HelpController(UserPreferences preferences, 
                        ViewFactory viewFactory) {
    this.preferences = preferences;
    this.viewFactory = viewFactory;    
    this.propertyChangeSupport = new PropertyChangeSupport(this);
    this.history = new ArrayList<URL>();
    this.historyIndex = -1;
    showPage(getHelpIndexPageURL());
  }

  /**
   * Returns the view associated with this controller.
   */
  public HelpView getView() {
    if (this.helpView == null) {
      this.helpView = this.viewFactory.createHelpView(this.preferences, this);
      addLanguageListener(this.preferences);
    }
    return this.helpView;
  }

  /**
   * Displays the help view controlled by this controller. 
   */
  public void displayView() {
    getView().displayView();
  }

  /**
   * Adds the property change <code>listener</code> in parameter to this controller.
   */
  public void addPropertyChangeListener(Property property, PropertyChangeListener listener) {
    this.propertyChangeSupport.addPropertyChangeListener(property.name(), listener);
  }

  /**
   * Removes the property change <code>listener</code> in parameter from this controller.
   */
  public void removePropertyChangeListener(Property property, PropertyChangeListener listener) {
    this.propertyChangeSupport.removePropertyChangeListener(property.name(), listener);
  }

  /**
   * Sets the current page.
   */
  private void setHelpPage(URL helpPage) {
    if (helpPage != this.helpPage) {
      URL oldHelpPage = this.helpPage;
      this.helpPage = helpPage;
      this.propertyChangeSupport.firePropertyChange(Property.HELP_PAGE.name(), oldHelpPage, helpPage);
    }
  }
  
  /**
   * Returns the current page.
   */
  public URL getHelpPage() {
    return this.helpPage;
  }

  /**
   * Sets the browser page.
   */
  private void setBrowserPage(URL browserPage) {
    if (browserPage != this.browserPage) {
      URL oldBrowserPage = this.browserPage;
      this.browserPage = browserPage;
      this.propertyChangeSupport.firePropertyChange(Property.BROWSER_PAGE.name(), oldBrowserPage, browserPage);
    }
  }
  
  /**
   * Returns the browser page.
   */
  public URL getBrowserPage() {
    return this.browserPage;
  }

  /**
   * Sets whether a previous page is available or not.
   */
  private void setPreviousPageEnabled(boolean previousPageEnabled) {
    if (previousPageEnabled != this.previousPageEnabled) {
      this.previousPageEnabled = previousPageEnabled;
      this.propertyChangeSupport.firePropertyChange(Property.PREVIOUS_PAGE_ENABLED.name(), 
          !previousPageEnabled, previousPageEnabled);
    }
  }
  
  /**
   * Returns whether a previous page is available or not.
   */
  public boolean isPreviousPageEnabled() {
    return this.previousPageEnabled;
  }

  /**
   * Sets whether a next page is available or not.
   */
  private void setNextPageEnabled(boolean nextPageEnabled) {
    if (nextPageEnabled != this.nextPageEnabled) {
      this.nextPageEnabled = nextPageEnabled;
      this.propertyChangeSupport.firePropertyChange(Property.NEXT_PAGE_ENABLED.name(), 
          !nextPageEnabled, nextPageEnabled);
    }
  }
  
  /**
   * Returns whether a next page is available or not.
   */
  public boolean isNextPageEnabled() {
    return this.nextPageEnabled;
  }

  /**
   * Adds a property change listener to <code>preferences</code> to update
   * displayed page when language changes.
   */
  private void addLanguageListener(UserPreferences preferences) {
    preferences.addPropertyChangeListener(UserPreferences.Property.LANGUAGE, 
        new LanguageChangeListener(this));
  }

  /**
   * Preferences property listener bound to this component with a weak reference to avoid
   * strong link between preferences and this component.  
   */
  private static class LanguageChangeListener implements PropertyChangeListener {
    private WeakReference<HelpController> helpController;

    public LanguageChangeListener(HelpController helpController) {
      this.helpController = new WeakReference<HelpController>(helpController);
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
      // If help controller was garbage collected, remove this listener from preferences
      HelpController helpController = this.helpController.get();
      if (helpController == null) {
        ((UserPreferences)ev.getSource()).removePropertyChangeListener(
            UserPreferences.Property.LANGUAGE, this);
      } else {
        // Updates home page from current default locale
        helpController.history.clear();
        helpController.historyIndex = -1;
        helpController.showPage(helpController.getHelpIndexPageURL());
      }
    }
  }
  
  /**
   * Controls the display of previous page.
   */
  public void showPrevious() {
    setHelpPage(this.history.get(--this.historyIndex));
    setPreviousPageEnabled(this.historyIndex > 0);
    setNextPageEnabled(true);
  }

  /**
   * Controls the display of next page.
   */
  public void showNext() {
    setHelpPage(this.history.get(++this.historyIndex));
    setPreviousPageEnabled(true);
    setNextPageEnabled(this.historyIndex < this.history.size() - 1);
  }

  /**
   * Controls the display of the given <code>page</code>.
   */
  public void showPage(URL page) {
    if (page.getProtocol().equals("http")) {
      setBrowserPage(page);
    } else if (this.historyIndex == -1
            || !this.history.get(this.historyIndex).equals(page)) {
      setHelpPage(page);
      for (int i = this.history.size() - 1; i > this.historyIndex; i--) {
        this.history.remove(i);
      }
      this.history.add(page);
      setPreviousPageEnabled(++this.historyIndex > 0);
      setNextPageEnabled(false);
    }
  }
  
  /**
   * Returns the URL of the help index page.
   */
  private URL getHelpIndexPageURL() {
    String helpIndex = this.preferences.getLocalizedString(HelpController.class, "helpIndex");
    // Build URL of index page with ResourceURLContent because of bug #6746185 
    return new ResourceURLContent(HelpController.class, helpIndex).getURL();
  }
  
  /**
   * Searches <code>searchedText</code> in help documents and displays 
   * the result.
   */
  public void search(String searchedText) {
    URL helpIndex = getHelpIndexPageURL();
    String [] searchedWords = searchedText.split("\\s");
    for (int i = 0; i < searchedWords.length; i++) {
      searchedWords [i] = searchedWords [i].toLowerCase();
    }
    List<HelpDocument> helpDocuments = searchInHelpDocuments(helpIndex, searchedWords);
    // Build dynamically the search result page
    final StringBuilder htmlText = new StringBuilder(
        "<html><head><meta http-equiv='content-type' content='text/html;charset=UTF-8'><link href='" 
        + new ResourceURLContent(HelpController.class, "resources/help/help.css").getURL()
        + "' rel='stylesheet'></head><body bgcolor='#ffffff'>\n"
        + "<div id='banner'><div id='helpheader'>"
        + "  <a class='bread' href='" + helpIndex + "'> " 
        +        this.preferences.getLocalizedString(HelpController.class, "helpTitle") + "</a>"
        + "</div></div>"
        + "<div id='mainbox' align='left'>"
        + "  <table width='100%' border='0' cellspacing='0' cellpadding='0'>"
        + "    <tr valign='bottom' height='32'>"
        + "      <td width='3' height='32'>&nbsp;</td>"
        + "      <td width='32' height='32'><img src='"  
        + new ResourceURLContent(HelpController.class, "resources/help/images/sweethome3dIcon32.png").getURL() 
        + "' height='32' width='32'></td>"
        + "      <td width='8' height='32'>&nbsp;&nbsp;</td>"
        + "      <td valign='bottom' height='32'><font id='topic'>" 
        +            this.preferences.getLocalizedString(HelpController.class, "searchResult") + "</font></td>"
        + "    </tr>"
        + "    <tr height='10'><td colspan='4' height='10'>&nbsp;</td></tr>"
        + "  </table>"
        + "  <table width='100%' border='0' cellspacing='0' cellpadding='3'>");
    
    if (helpDocuments.size() == 0) {
      String searchNotFound = this.preferences.getLocalizedString(HelpController.class, "searchNotFound", searchedText); 
      htmlText.append("<tr><td><p>" + searchNotFound + "</td></tr>");
    } else {
      String searchFound = this.preferences.getLocalizedString(HelpController.class, "searchFound", searchedText); 
      htmlText.append("<tr><td colspan='2'><p>" + searchFound + "</td></tr>");
      
      URL searchRelevanceImage = new ResourceURLContent(HelpController.class, "resources/searchRelevance.gif").getURL();
      for (HelpDocument helpDocument : helpDocuments) {
        // Add hyperlink to help document found
        htmlText.append("<tr><td valign='middle' nowrap><a href='" + helpDocument.getBase() + "'>" 
            + helpDocument.getTitle() + "</a></td><td valign='middle'>");
        // Add relevance image
        for (int i = 0; i < helpDocument.getRelevance() && i < 50; i++) {
          htmlText.append("<img src='" + searchRelevanceImage + "' width='4' height='12'>");
        }
        htmlText.append("</td></tr>");
      }
    }
    htmlText.append("</table></div></body></html>");

    try {
      // Show built HTML text as a page read from an URL
      showPage(new URL(null, "string://" + htmlText.hashCode(), new URLStreamHandler() {
          @Override
          protected URLConnection openConnection(URL url) throws IOException {
            return new URLConnection(url) {
                @Override
                public void connect() throws IOException {
                  // Don't need to connect
                }
                
                @Override
                public InputStream getInputStream() throws IOException {
                  return new ByteArrayInputStream(
                      htmlText.toString().getBytes("UTF-8"));
                }
              };
          }
        }));
    } catch (MalformedURLException ex) {
      // Can't happen
    }
  }

  /**
   * Searches <code>searchedWords</code> in help documents and returns 
   * the list of matching documents sorted from the most relevant to the least relevant.
   * This method uses some Swing classes for their HTML parsing capabilities 
   * and not to create components.
   */
  public List<HelpDocument> searchInHelpDocuments(URL helpIndex, String [] searchedWords) {
    List<URL> parsedDocuments = new ArrayList<URL>(); 
    parsedDocuments.add(helpIndex);
    
    List<HelpDocument> helpDocuments = new ArrayList<HelpDocument>();
    // Parse all the URLs added to parsedDocuments at each loop
    HTMLEditorKit html = new HTMLEditorKit();
    for (int i = 0; i < parsedDocuments.size(); i++) {
      URL helpDocumentUrl = (URL)parsedDocuments.get(i);
      Reader urlReader = null;
      try {
        urlReader = new InputStreamReader(helpDocumentUrl.openStream(), "ISO-8859-1");

        // Create an HTML document
        HelpDocument helpDocument = new HelpDocument(helpDocumentUrl, searchedWords);
        // Parse HTML file
        html.read(urlReader, helpDocument, 0);
        // If searched text was found add it to returned documents list
        if (helpDocument.getRelevance() > 0) {
          helpDocuments.add(helpDocument);
        }

        // Check if the HTML file contains new URLs to parse
        for (URL url : helpDocument.getReferencedDocuments()) {
          String lowerCaseFile = url.getFile().toLowerCase();
          if (lowerCaseFile.endsWith(".html")
              && !parsedDocuments.contains(url)) {
            parsedDocuments.add(url);
          } 
        } 
      } catch (IOException ex) {
        // Ignore unknown documents (their URLs should be checked outside of Sweet Home 3D)
      } catch (BadLocationException ex) {
      } finally {
        if (urlReader != null) {
          try {
            urlReader.close();
          } catch (IOException ex) {
          }
        }
      }
    }
    // Sort by relevance
    Collections.sort(helpDocuments, new Comparator<HelpDocument>() {
        public int compare(HelpDocument document1, HelpDocument document2) {
          return document2.getRelevance() - document1.getRelevance();
        }
      });
    return helpDocuments;
  }

  /**
   * A help HTML document parsed with <code>HTMLEditorKit</code>. 
   */
  private static class HelpDocument extends HTMLDocument {
    // Documents set referenced in this file 
    private Set<URL>  referencedDocuments = new HashSet<URL>();
    private String [] searchedWords;
    private int       relevance;
    private String    title = "";

    public HelpDocument(URL helpDocument, String [] searchedWords) {
      this.searchedWords = searchedWords;
      // Store HTML file base
      setBase(helpDocument);
      putProperty("IgnoreCharsetDirective", Boolean.TRUE);
    }

    public Set<URL> getReferencedDocuments() {
      return this.referencedDocuments;
    }
    
    public int getRelevance() {
      return this.relevance;
    }

    public String getTitle() {
      return this.title;
    }
    
    private void addReferencedDocument(String referencedDocument) {
      try {        
        if (!referencedDocument.startsWith("http:")) {
          URL url = new URL(getBase(), referencedDocument);
          URL urlWithNoAnchor = new URL(
              url.getProtocol(), url.getHost(), url.getPort(), url.getFile());
          this.referencedDocuments.add(urlWithNoAnchor);
        }
      } catch (MalformedURLException e) {
        // Ignore malformed URLs (they should be checked outside of Sweet Home 3D)
      }
    }

    @Override
    public HTMLEditorKit.ParserCallback getReader(int pos) {
      // Change default callback reader
      return new HelpReader();
    }

    // Reader that tracks all <a href=...> tags in current HTML document 
    private class HelpReader extends HTMLEditorKit.ParserCallback {
      private boolean inTitle;

      @Override
      public void handleStartTag(HTML.Tag tag,
                                 MutableAttributeSet att, int pos) {
        String attribute;
        if (tag.equals(HTML.Tag.A)) { // <a href=...> tag
          attribute = (String)att.getAttribute(HTML.Attribute.HREF);
          if (attribute != null) {
            addReferencedDocument(attribute);
          }
        } else if (tag.equals(HTML.Tag.TITLE)) {
          this.inTitle = true;
        }
      }
      
      @Override
      public void handleEndTag(Tag tag, int pos) {
        if (tag.equals(HTML.Tag.TITLE)) {
          this.inTitle = false;
        }
      }
      
      @Override
      public void handleText(char [] data, int pos) {
        String text = new String(data);
        if (this.inTitle) {
          title += text;
        }
        
        String lowerCaseText = text.toLowerCase();
        for (String searchedWord : searchedWords) {
          for (int index = 0; index < lowerCaseText.length(); index += searchedWord.length() + 1) {
            index = lowerCaseText.indexOf(searchedWord, index);
            if (index == -1) {
              break;
            } else {
              relevance++;
              // Give more relevance to searchedWord when it's found in title
              if (this.inTitle) {
                relevance++;
              }
            }
          }
        }
      }
    }
  }
}
