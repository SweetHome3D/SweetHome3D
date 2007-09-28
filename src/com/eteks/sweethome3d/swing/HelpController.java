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
package com.eteks.sweethome3d.swing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTML.Tag;

import com.eteks.sweethome3d.model.UserPreferences;

/**
 * A MVC controller for Sweet Home 3D help view.
 * @author Emmanuel Puybaret
 */
public class HelpController {
  private List<URL>      history;
  private int            historyIndex;
  private JComponent     helpView;
  
  public HelpController(UserPreferences preferences) {
    this.history = new ArrayList<URL>();
    historyIndex = -1;
    this.helpView = new HelpPane(preferences, this);
    addLanguageListener(preferences);
  }

  /**
   * Returns the view associated with this controller.
   */
  public JComponent getView() {
    return this.helpView;
  }

  /**
   * Displays the help view controlled by this controller. 
   */
  public void displayView() {
    showPage(HelpController.class.getResource(
        ResourceBundle.getBundle(HelpController.class.getName()).getString("helpIndex")));
    ((HelpPane)getView()).displayView();
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
        helpController.showPage(HelpController.class.getResource(
            ResourceBundle.getBundle(HelpController.class.getName()).getString("helpIndex")));
      }
    }
  }
  
  /**
   * Controls the display of previous page.
   */
  public void showPrevious() {
    HelpPane helpView = (HelpPane)getView();
    helpView.setPage(this.history.get(--this.historyIndex));
    helpView.setPreviousEnabled(this.historyIndex > 0);
    helpView.setNextEnabled(true);
  }

  /**
   * Controls the display of next page.
   */
  public void showNext() {
    HelpPane helpView = (HelpPane)getView();
    helpView.setPage(this.history.get(++this.historyIndex));
    helpView.setPreviousEnabled(true);
    helpView.setNextEnabled(this.historyIndex < this.history.size() - 1);
  }

  /**
   * Controls the display of the given <code>page</code>.
   */
  public void showPage(URL page) {
    HelpPane helpView = (HelpPane)getView();
    if (page.getProtocol().equals("http")) {
      helpView.setBrowserPage(page);
    } else if (this.historyIndex == -1
            || !this.history.get(this.historyIndex).equals(page)) {
      helpView.setPage(page);
      for (int i = this.history.size() - 1; i > this.historyIndex; i--) {
        this.history.remove(i);
      }
      this.history.add(page);
      helpView.setPreviousEnabled(++this.historyIndex > 0);
      helpView.setNextEnabled(false);
    }
  }
  
  /**
   * Searches <code>searchedText</code> in help documents and displays 
   * the result.
   */
  public void search(String searchedText) {
    ResourceBundle resource = ResourceBundle.getBundle(HelpController.class.getName());
    URL helpIndex = HelpController.class.getResource(resource.getString("helpIndex"));
    String [] searchedWords = searchedText.split("\\s");

    List<HelpDocument> helpDocuments = searchInHelpDocuments(helpIndex, searchedWords);
    // Build dinamically the search result page
    final StringBuilder htmlText = new StringBuilder(
        "<html><head><link href='" 
        + HelpController.class.getResource("resources/help/help.css")
        + "' rel='stylesheet'></head><body bgcolor='#ffffff'>\n"
        + "<div id='banner'><div id='helpheader'>"
        + "  <a class='bread' href='" + helpIndex + "'> " + resource.getString("helpTitle") + "</a>"
        + "</div></div>"
        + "<div id='mainbox' align='left'>"
        + "  <table width='100%' border='0' cellspacing='0' cellpadding='0'>"
        + "    <tr valign='bottom' height='32'>"
        + "      <td width='3' height='32'>&nbsp;</td>"
        + "      <td width='32' height='32'><img src='"  
        + HelpController.class.getResource("resources/help/images/sweethome3dIcon32.png") 
        + "' height='32' width='32'></td>"
        + "      <td width='8' height='32'>&nbsp;&nbsp;</td>"
        + "      <td valign='bottom' height='32'><font id='topic'>" + resource.getString("searchResult") + "</font></td>"
        + "    </tr>"
        + "    <tr height='10'><td colspan='4' height='10'>&nbsp;</td></tr>"
        + "  </table>"
        + "  <table width='100%' border='0' cellspacing='0' cellpadding='3'>");
    
    if (helpDocuments.size() == 0) {
      String searchNotFound = String.format(resource.getString("searchNotFound"), searchedText); 
      htmlText.append("<tr><td><p>" + searchNotFound + "</td></tr>");
    } else {
      URL searchRelevanceImage = HelpController.class.getResource("resources/searchRelevance.gif");
      for (HelpDocument helpDocument : helpDocuments) {
        // Add hyperlink to help document found
        htmlText.append("<tr><td valign='middle' nowrap><a href='" + helpDocument.getBase() + "'>" 
            + helpDocument.getTitle() + "</a></td><td valign='middle'>");
        // Add relevance image
        for (int i = 0; i < helpDocument.getRelevance() && i < 50; i++) {
          htmlText.append("<img src='" + searchRelevanceImage + "'>");
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
                      htmlText.toString().getBytes("ISO-8859-1"));
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
   * This method uses some Swing classes for their HTML parsing capibilities 
   * and not to create components.
   */
  public List<HelpDocument> searchInHelpDocuments(URL helpIndex, String [] searchedWords) {
    List<URL> parsedDocuments = new ArrayList<URL>(); 
    parsedDocuments.add(helpIndex);
    
    List<HelpDocument> helpDocuments = new ArrayList<HelpDocument>();
    // Parcours de toutes les urls qui sont ajoutées à parsedHtmlFiles
    HTMLEditorKit html = new HTMLEditorKit();
    for (int i = 0; i < parsedDocuments.size(); i++) {
      URL helpDocumentUrl = (URL)parsedDocuments.get(i);
      Reader urlReader = null;
      try {
        urlReader = new InputStreamReader(helpDocumentUrl.openStream(), "ISO-8859-1");

        // Création d'un document HTML ajouté à l'ensemble htmlFiles
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
