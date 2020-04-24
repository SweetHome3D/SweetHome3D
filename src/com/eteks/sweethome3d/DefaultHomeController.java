/*
 * DefaultHomeController.java 21 april 2020
 *
 * Sweet Home 3D, Copyright (c) 2020 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
package com.eteks.sweethome3d;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Callable;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeApplication;
import com.eteks.sweethome3d.model.Library;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.plugin.HomePluginController;
import com.eteks.sweethome3d.plugin.PluginManager;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.ContentManager;
import com.eteks.sweethome3d.viewcontroller.HomeController;
import com.eteks.sweethome3d.viewcontroller.ThreadedTaskController;
import com.eteks.sweethome3d.viewcontroller.ThreadedTaskView;
import com.eteks.sweethome3d.viewcontroller.View;
import com.eteks.sweethome3d.viewcontroller.ViewFactory;
import com.eteks.sweethome3d.viewcontroller.ViewFactoryAdapter;

/**
 * The home controller used in Sweet Home 3D application.
 * @author Emmanuel Puybaret
 */
public class DefaultHomeController extends HomePluginController {
  private HomeApplication application;
  private ViewFactory     viewFactory;

  public DefaultHomeController(Home home, HomeApplication application, ViewFactory viewFactory,
                               ContentManager contentManager, PluginManager pluginManager) {
    super(home, application, viewFactory, contentManager, pluginManager);
    this.application = application;
    this.viewFactory = viewFactory;
  }

  /**
   * Checks if some application or libraries updates are available.
   */
  @Override
  public void checkUpdates(final boolean displayOnlyIfNewUpdates) {
    String updatesUrl = getPropertyValue("com.eteks.sweethome3d.updatesUrl", "updatesUrl");
    if (updatesUrl != null && updatesUrl.length() > 0) {
      final URL url;
      try {
        url = new URL(updatesUrl);
      } catch (MalformedURLException ex) {
        ex.printStackTrace();
        return;
      }

      final UserPreferences preferences = application.getUserPreferences();
      final List<Library> libraries = preferences.getLibraries();
      final Long updatesMinimumDate = displayOnlyIfNewUpdates
          ? preferences.getUpdatesMinimumDate()
          : null;

      // Read updates from XML content in updatesUrl in a threaded task
      Callable<Void> checkUpdatesTask = new Callable<Void>() {
          public Void call() throws IOException, SAXException {
            final Map<Library, List<Update>> availableUpdates = readAvailableUpdates(url, libraries, updatesMinimumDate,
                displayOnlyIfNewUpdates ? 3000 : -1);
            getView().invokeLater(new Runnable () {
                public void run() {
                  if (availableUpdates.isEmpty()) {
                    if (!displayOnlyIfNewUpdates) {
                      getView().showMessage(preferences.getLocalizedString(HomeController.class, "noUpdateMessage"));
                    }
                  } else if (!getView().showUpdatesMessage(getUpdatesMessage(availableUpdates), !displayOnlyIfNewUpdates)) {
                    // Search the latest date among updates
                    long latestUpdateDate = Long.MIN_VALUE;
                    for (List<Update> libraryAvailableUpdates : availableUpdates.values()) {
                      for (Update update : libraryAvailableUpdates) {
                        latestUpdateDate = Math.max(latestUpdateDate, update.getDate().getTime());
                      }
                    }
                    preferences.setUpdatesMinimumDate(latestUpdateDate + 1);
                  }
                }
              });
            return null;
          }
        };
      ThreadedTaskController.ExceptionHandler exceptionHandler =
          new ThreadedTaskController.ExceptionHandler() {
            public void handleException(Exception ex) {
              if (!displayOnlyIfNewUpdates && !(ex instanceof InterruptedIOException)) {
                if (ex instanceof IOException) {
                  getView().showError(preferences.getLocalizedString(HomeController.class, "checkUpdatesIOError", ex));
                } else if (ex instanceof SAXException) {
                  getView().showError(preferences.getLocalizedString(HomeController.class, "checkUpdatesXMLError", ex.getMessage()));
                } else {
                  ex.printStackTrace();
                }
              }
            }
          };

      ViewFactory dummyThreadedTaskViewFactory = new ViewFactoryAdapter() {
          @Override
          public ThreadedTaskView createThreadedTaskView(String taskMessage, UserPreferences preferences,
                                                         ThreadedTaskController controller) {
            // Return a dummy view that doesn't do anything
            return new ThreadedTaskView() {
              public void setTaskRunning(boolean taskRunning, View executingView) {
              }

              public void invokeLater(Runnable runnable) {
                getView().invokeLater(runnable);
              }
            };
          }
        };
      new ThreadedTaskController(checkUpdatesTask,
          preferences.getLocalizedString(HomeController.class, "checkUpdatesMessage"), exceptionHandler,
          preferences, displayOnlyIfNewUpdates
            ? dummyThreadedTaskViewFactory
            : this.viewFactory).executeTask(getView());
    }
  }

  /**
   * Returns the System property value of the given <code>propertyKey</code>, or the
   * the resource property value matching <code>resourceKey</code> or <code>null</code>
   * if none are defined.
   */
  private String getPropertyValue(String propertyKey, String resourceKey) {
    String propertyValue = System.getProperty(propertyKey);
    if (propertyValue != null) {
      return propertyValue;
    } else {
      try {
        return this.application.getUserPreferences().getLocalizedString(HomeController.class, resourceKey);
      } catch (IllegalArgumentException ex) {
        return null;
      }
    }
  }

  /**
   * Reads the available updates from the XML stream contained in the given <code>url</code>.
   * Caution : this method is called from a separate thread.
   */
  private Map<Library, List<Update>> readAvailableUpdates(URL url, List<Library> libraries, Long minDate, int timeout) throws IOException, SAXException {
    try {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setValidating(false);
      SAXParser saxParser = factory.newSAXParser();
      UpdatesHandler updatesHandler = new UpdatesHandler(url);
      URLConnection connection = url.openConnection();
      if (timeout > 0) {
        connection.setConnectTimeout(timeout);
        connection.setReadTimeout(timeout);
      }
      saxParser.parse(connection.getInputStream(), updatesHandler);

      // Filter updates according to application version and libraries version
      Map<Library, List<Update>> availableUpdates = new LinkedHashMap<Library, List<Update>>();
      long now = System.currentTimeMillis();
      if (this.application != null) {
        String applicationId = this.application.getId();
        List<Update> applicationUpdates = getAvailableUpdates(updatesHandler.getUpdates(applicationId),
            this.application.getVersion(), minDate, now);
        if (!applicationUpdates.isEmpty()) {
          availableUpdates.put(null, applicationUpdates);
        }
      }
      Set<String> updatedLibraryIds = new HashSet<String>();
      for (Library library : libraries) {
        if (Thread.interrupted()) {
          throw new InterruptedIOException();
        }
        String libraryId = library.getId();
        if (libraryId != null
            && !updatedLibraryIds.contains(libraryId)) {
          List<Update> libraryUpdates = getAvailableUpdates(updatesHandler.getUpdates(libraryId),
              library.getVersion(), minDate, now);
          if (!libraryUpdates.isEmpty()) {
            availableUpdates.put(library, libraryUpdates);
          }
          // Ignore older libraries with same ID
          updatedLibraryIds.add(libraryId);
        }
      }
      return availableUpdates;
    } catch (ParserConfigurationException ex) {
      throw new SAXException(ex);
    } catch (SAXException ex) {
      // If task was interrupted (see UpdatesHandler implementation), report the interruption
      if (ex.getCause() instanceof InterruptedIOException) {
        throw (InterruptedIOException)ex.getCause();
      } else {
        throw ex;
      }
    }
  }

  /**
   * Returns the updates sublist which match the given <code>version</code>.
   * If no update has a date greater that <code>minDate</code>, an empty list is returned.
   * Caution : this method is called from a separate thread.
   */
  private List<Update> getAvailableUpdates(List<Update> updates, String version, Long minDate, long maxDate) {
    if (updates != null) {
      boolean recentUpdates = false;
      List<Update> availableUpdates = new ArrayList<Update>();
      for (Update update : updates) {
        String minVersion = update.getMinVersion();
        String maxVersion = update.getMaxVersion();
        String operatingSystem = update.getOperatingSystem();
        if (OperatingSystem.compareVersions(version, update.getVersion()) < 0
            && (minVersion == null || OperatingSystem.compareVersions(minVersion, version) <= 0)
            && (maxVersion == null || OperatingSystem.compareVersions(version, maxVersion) < 0)
            && (operatingSystem == null || System.getProperty("os.name").matches(operatingSystem))) {
          Date date = update.getDate();
          if (date == null
              || ((minDate == null || date.getTime() >= minDate)
                  && date.getTime() < maxDate)) {
            availableUpdates.add(update);
            recentUpdates = true;
          }
        }
      }
      if (recentUpdates) {
        Collections.sort(availableUpdates, new Comparator<Update>() {
            public int compare(Update update1, Update update2) {
              return -OperatingSystem.compareVersions(update1.getVersion(), update2.getVersion());
            }
          });
        return availableUpdates;
      }
    }
    return Collections.emptyList();
  }

  /**
   * Returns the message for the given updates.
   */
  private String getUpdatesMessage(Map<Library, List<Update>> updates) {
    UserPreferences preferences = application.getUserPreferences();
    if (updates.isEmpty()) {
      return preferences.getLocalizedString(HomeController.class, "noUpdateMessage");
    } else {
      String message = "<html><head><style>"
          + preferences.getLocalizedString(HomeController.class, "updatesMessageStyleSheet")
          + " .separator { margin: 0px;}</style></head><body>"
          + preferences.getLocalizedString(HomeController.class, "updatesMessageTitle");
      String applicationUpdateMessage = preferences.getLocalizedString(HomeController.class, "applicationUpdateMessage");
      String libraryUpdateMessage = preferences.getLocalizedString(HomeController.class, "libraryUpdateMessage");
      String sizeUpdateMessage = preferences.getLocalizedString(HomeController.class, "sizeUpdateMessage");
      String downloadUpdateMessage = preferences.getLocalizedString(HomeController.class, "downloadUpdateMessage");
      String updatesMessageSeparator = preferences.getLocalizedString(HomeController.class, "updatesMessageSeparator");
      boolean firstUpdate = true;
      for (Map.Entry<Library, List<Update>> updateEntry : updates.entrySet()) {
        if (firstUpdate) {
          firstUpdate = false;
        } else {
          message += updatesMessageSeparator;
        }
        Library library = updateEntry.getKey();
        if (library == null) {
          // Application itself
          if (this.application != null) {
            message += getApplicationOrLibraryUpdateMessage(updateEntry.getValue(), this.application.getName(),
                applicationUpdateMessage, sizeUpdateMessage, downloadUpdateMessage);
          }
        } else {
          String name = library.getName();
          if (name == null) {
            name = library.getDescription();
            if (name == null) {
              name = library.getLocation();
            }
          }
          message += getApplicationOrLibraryUpdateMessage(updateEntry.getValue(), name,
              libraryUpdateMessage, sizeUpdateMessage, downloadUpdateMessage);
        }
      }

      message += "</body></html>";
      return message;
    }
  }

  /**
   * Returns the message for the update of the application or a library.
   */
  private String getApplicationOrLibraryUpdateMessage(List<Update> updates,
                                                      String applicationOrLibraryName,
                                                      String applicationOrLibraryUpdateMessage,
                                                      String sizeUpdateMessage,
                                                      String downloadUpdateMessage) {
    String message = "";
    boolean first = true;
    DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG);
    DecimalFormat megabyteSizeFormat = new DecimalFormat("#,##0.#");
    for (Update update : updates) {
      String size;
      if (update.getSize() != null) {
        // Format at MB format
        size = String.format(sizeUpdateMessage,
            megabyteSizeFormat.format(update.getSize() / (1024. * 1024.)));
      } else {
        size = "";
      }
      message += String.format(applicationOrLibraryUpdateMessage,
          applicationOrLibraryName, update.getVersion(), dateFormat.format(update.getDate()), size);
      if (first) {
        first = false;
        URL downloadPage = update.getDownloadPage();
        if (downloadPage == null) {
          downloadPage = update.getDefaultDownloadPage();
        }
        if (downloadPage != null) {
          message += String.format(downloadUpdateMessage, downloadPage);
        }
      }
      String comment = update.getComment();
      if (comment == null) {
        comment = update.getDefaultComment();
      }
      if (comment != null) {
        message += "<p class='separator'/>";
        message += comment;
        message += "<p class='separator'/>";
      }
    }
    return message;
  }

  /**
   * SAX handler used to parse updates XML files.
   * DTD used in updated files:<pre>
   * &lt;!ELEMENT updates (update*)>
   *
   * &lt;!ELEMENT update (downloadPage*, comment*)>
   * &lt;!ATTLIST update id CDATA #REQUIRED>
   * &lt;!ATTLIST update version CDATA #REQUIRED>
   * &lt;!ATTLIST update operatingSystem CDATA #IMPLIED>
   * &lt;!ATTLIST update date CDATA #REQUIRED>
   * &lt;!ATTLIST update minVersion CDATA #IMPLIED>
   * &lt;!ATTLIST update maxVersion CDATA #IMPLIED>
   * &lt;!ATTLIST update size CDATA #IMPLIED>
   * &lt;!ATTLIST update inherits CDATA #IMPLIED>
   *
   * &lt;!ELEMENT downloadPage EMPTY>
   * &lt;!ATTLIST downloadPage url CDATA #REQUIRED>
   * &lt;!ATTLIST downloadPage lang CDATA #IMPLIED>
   *
   * &lt;!ELEMENT comment (#PCDATA)>
   * &lt;!ATTLIST comment lang CDATA #IMPLIED>
   * </pre>
   * with <code>updates</code> as root element,
   * <code>operatingSystem</code> an optional regular expression for the target OS,
   * <code>inherits</code> the id of an other <code>update</code> element with the same version,
   * <code>date</code> using <code>yyyy-MM-ddThh:mm:ss<code> or <code>yyyy-MM-dd</code> format
   * at GMT and <code>comment</code> element possibly containing XHTML.
   */
  private class UpdatesHandler extends DefaultHandler {
    private final URL                       baseUrl;
    private final StringBuilder             comment = new StringBuilder();
    private final SimpleDateFormat          dateTimeFormat;
    private final SimpleDateFormat          dateFormat;
    private final Map<String, List<Update>> updates = new HashMap<String, List<Update>>();
    private Update                          update;
    private boolean                         inComment;
    private boolean                         inUpdate;
    private String                          language;

    public UpdatesHandler(URL baseUrl) {
      this.baseUrl = baseUrl;
      TimeZone gmtTimeZone = TimeZone.getTimeZone("GMT");
      this.dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
      this.dateTimeFormat.setTimeZone(gmtTimeZone);
      this.dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      this.dateFormat.setTimeZone(gmtTimeZone);
    }

    /**
     * Returns the update matching the given <code>id</code>.
     */
    private List<Update> getUpdates(String id) {
      return this.updates.get(id);
    }

    /**
     * Throws a <code>SAXException</code> exception initialized with a <code>InterruptedRecorderException</code>
     * cause if current thread is interrupted. The interrupted status of the current thread
     * is cleared when an exception is thrown.
     */
    private void checkCurrentThreadIsntInterrupted() throws SAXException {
      if (Thread.interrupted()) {
        throw new SAXException(new InterruptedIOException());
      }
    }

    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
      checkCurrentThreadIsntInterrupted();
      if (this.inComment) {
        // Reproduce comment content
        this.comment.append("<" + name);
        for (int i = 0; i < attributes.getLength(); i++) {
          this.comment.append(" " + attributes.getQName(i) + "=\"" + attributes.getValue(i) + "\"");
        }
        this.comment.append(">");
      } else if (this.inUpdate && "comment".equals(name)) {
        this.comment.setLength(0);
        this.language = attributes.getValue("lang");
        if (this.language == null || application.getUserPreferences().getLanguage().equals(this.language)) {
          this.inComment = true;
        }
      } else if (this.inUpdate && "downloadPage".equals(name)) {
        String url = attributes.getValue("url");
        if (url != null) {
          try {
            String language = attributes.getValue("lang");
            if (language == null) {
              this.update.setDefaultDownloadPage(new URL(this.baseUrl, url));
            } else if (application.getUserPreferences().getLanguage().equals(language)) {
              this.update.setDownloadPage(new URL(this.baseUrl, url));
            }
          } catch (MalformedURLException ex) {
            // Ignore bad URLs
          }
        }
      } else if (!this.inUpdate && "update".equals(name)) {
        String id = attributes.getValue("id");
        String version = attributes.getValue("version");
        if (id != null
            && version != null) {
          this.update = new Update(id, version);

          String inheritedUpdate = attributes.getValue("inherits");
          // If update inherits from an other update, search the update with the same id and version
          if (inheritedUpdate != null) {
            List<Update> updates = this.updates.get(inheritedUpdate);
            if (updates != null) {
              for (Update update : updates) {
                if (version.equals(update.getVersion())) {
                  this.update = update.clone();
                  this.update.setId(id);
                  break;
                }
              }
            }
          }

          String dateAttibute = attributes.getValue("date");
          if (dateAttibute != null) {
            try {
              this.update.setDate(this.dateTimeFormat.parse(dateAttibute));
            } catch (ParseException ex) {
              try {
                this.update.setDate(this.dateFormat.parse(dateAttibute));
              } catch (ParseException ex1) {
              }
            }
          }

          String minVersion = attributes.getValue("minVersion");
          if (minVersion != null) {
            this.update.setMinVersion(minVersion);
          }

          String maxVersion = attributes.getValue("maxVersion");
          if (maxVersion != null) {
            this.update.setMaxVersion(maxVersion);
          }

          String size = attributes.getValue("size");
          if (size != null) {
            try {
              this.update.setSize(new Long (size));
            } catch (NumberFormatException ex) {
              // Ignore malformed number
            }
          }

          String operatingSystem = attributes.getValue("operatingSystem");
          if (operatingSystem != null) {
            this.update.setOperatingSystem(operatingSystem);
          }

          List<Update> updates = this.updates.get(id);
          if (updates == null) {
            updates = new ArrayList<Update>();
            this.updates.put(id, updates);
          }
          updates.add(this.update);
          this.inUpdate = true;
        }
      }
    }

    @Override
    public void characters(char [] ch, int start, int length) throws SAXException {
      checkCurrentThreadIsntInterrupted();
      if (this.inComment) {
        // Reproduce comment content
        this.comment.append(ch, start, length);
      }
    }

    @Override
    public void endElement(String uri, String localName, String name) throws SAXException {
      if (this.inComment) {
        if ("comment".equals(name)) {
          String comment = this.comment.toString().trim().replace('\n', ' ');
          if (comment.length() == 0) {
            comment = null;
          }
          if (this.language == null) {
            this.update.setDefaultComment(comment);
          } else {
            this.update.setComment(comment);
          }
          this.inComment = false;
        } else {
          // Reproduce comment content
          this.comment.append("</" + name + ">");
        }
      } else if (this.inUpdate && "update".equals(name)) {
        this.inUpdate = false;
      }
    }
  }

  /**
   * Update info.
   */
  private static class Update implements Cloneable {
    private String id;
    private final String version;
    private Date   date;
    private String minVersion;
    private String maxVersion;
    private Long   size;
    private String operatingSystem;
    private URL    defaultDownloadPage;
    private URL    downloadPage;
    private String defaultComment;
    private String comment;

    public Update(String id, String version) {
      this.id = id;
      this.version = version;
    }

    public String getId() {
      return this.id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getVersion() {
      return this.version;
    }

    public Date getDate() {
      return this.date;
    }

    public void setDate(Date date) {
      this.date = date;
    }

    public String getMinVersion() {
      return this.minVersion;
    }

    public void setMinVersion(String minVersion) {
      this.minVersion = minVersion;
    }

    public String getMaxVersion() {
      return this.maxVersion;
    }

    public void setMaxVersion(String maxVersion) {
      this.maxVersion = maxVersion;
    }

    public Long getSize() {
      return this.size;
    }

    public void setSize(Long size) {
      this.size = size;
    }

    public String getOperatingSystem() {
      return this.operatingSystem;
    }

    public void setOperatingSystem(String system) {
      this.operatingSystem = system;
    }

    public URL getDefaultDownloadPage() {
      return this.defaultDownloadPage;
    }

    public void setDefaultDownloadPage(URL defaultDownloadPage) {
      this.defaultDownloadPage = defaultDownloadPage;
    }

    public URL getDownloadPage() {
      return this.downloadPage;
    }

    public void setDownloadPage(URL downloadPage) {
      this.downloadPage = downloadPage;
    }

    public String getDefaultComment() {
      return this.defaultComment;
    }

    public void setDefaultComment(String defaultComment) {
      this.defaultComment = defaultComment;
    }

    public String getComment() {
      return this.comment;
    }

    public void setComment(String comment) {
      this.comment = comment;
    }

    @Override
    protected Update clone() {
      try {
        return (Update)super.clone();
      } catch (CloneNotSupportedException ex) {
        throw new InternalError();
      }
    }
  }
}