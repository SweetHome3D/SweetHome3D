/*
 * AppletApplication.java 11 Oct 2008
 *
 * Copyright (c) 2008 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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
package com.eteks.sweethome3d.applet;

import java.applet.AppletContext;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.jnlp.ServiceManagerStub;
import javax.jnlp.UnavailableServiceException;
import javax.media.j3d.IllegalRenderingStateException;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JApplet;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;

import com.eteks.sweethome3d.j3d.Component3DManager;
import com.eteks.sweethome3d.j3d.ModelManager;
import com.eteks.sweethome3d.j3d.TextureManager;
import com.eteks.sweethome3d.model.CollectionEvent;
import com.eteks.sweethome3d.model.CollectionListener;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeApplication;
import com.eteks.sweethome3d.model.HomeRecorder;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.plugin.PluginAction;
import com.eteks.sweethome3d.plugin.PluginManager;
import com.eteks.sweethome3d.swing.ControllerAction;
import com.eteks.sweethome3d.swing.IconManager;
import com.eteks.sweethome3d.swing.ResourceAction;
import com.eteks.sweethome3d.swing.SwingTools;
import com.eteks.sweethome3d.swing.SwingViewFactory;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.ContentManager;
import com.eteks.sweethome3d.viewcontroller.HomeController;
import com.eteks.sweethome3d.viewcontroller.HomeView;
import com.eteks.sweethome3d.viewcontroller.PlanController;
import com.eteks.sweethome3d.viewcontroller.ViewFactory;

/**
 * An application wrapper working in applet. 
 * @author Emmanuel Puybaret
 */
public class AppletApplication extends HomeApplication {
  private static final String   FURNITURE_CATALOG_URLS_PARAMETER = "furnitureCatalogURLs";
  private static final String   TEXTURES_CATALOG_URLS_PARAMETER  = "texturesCatalogURLs";
  private static final String   PLUGIN_URLS_PARAMETER            = "pluginURLs";
  private static final String   WRITE_HOME_URL_PARAMETER         = "writeHomeURL";
  private static final String   READ_HOME_URL_PARAMETER          = "readHomeURL";
  private static final String   LIST_HOMES_URL_PARAMETER         = "listHomesURL";
  private static final String   READ_PREFERENCES_URL_PARAMETER   = "readPreferencesURL";
  private static final String   WRITE_PREFERENCES_URL_PARAMETER  = "writePreferencesURL";
  private static final String   DEFAULT_HOME_PARAMETER           = "defaultHome";
  private static final String   ENABLE_EXPORT_TO_SH3D            = "enableExportToSH3D";
  private static final String   ENABLE_EXPORT_TO_SVG             = "enableExportToSVG";
  private static final String   ENABLE_EXPORT_TO_OBJ             = "enableExportToOBJ";
  private static final String   ENABLE_PRINT_TO_PDF              = "enablePrintToPDF";
  private static final String   ENABLE_CREATE_PHOTO              = "enableCreatePhoto";
  private static final String   SHOW_MEMORY_STATUS_PARAMETER     = "showMemoryStatus";
  private static final String   USER_LANGUAGE                    = "userLanguage";
  
  private JApplet         applet;
  private final String    name;
  private HomeRecorder    homeRecorder;
  private UserPreferences userPreferences;
  private ContentManager  contentManager;
  private ViewFactory     viewFactory;
  private PluginManager   pluginManager;
  private Timer           memoryStatusTimer;

  public AppletApplication(final JApplet applet) {
    this.applet = applet;
    if (applet.getName() == null) {
      this.name = super.getName();
    } else {
      this.name = applet.getName();
    }
    
    final String readHomeURL = getAppletParameter(applet, READ_HOME_URL_PARAMETER, "readHome.php?home=%s");
    final String defaultHome = getAppletParameter(applet, DEFAULT_HOME_PARAMETER, "");    
    final boolean showMemoryStatus = getAppletBooleanParameter(applet, SHOW_MEMORY_STATUS_PARAMETER);

    URL codeBase = applet.getCodeBase();

    // If Sweet Home 3D applet is launched from outside of Java Web Start or basic service is unavailable
    boolean serviceManagerAvailable = ServiceManager.getServiceNames() != null; 
    if (serviceManagerAvailable) {
      try { 
        ServiceManager.lookup("javax.jnlp.BasicService");
      } catch (UnavailableServiceException ex) {
        serviceManagerAvailable = false;
      }
    }

    if (!serviceManagerAvailable) {
      // Create JNLP services required by Sweet Home 3D 
      ServiceManager.setServiceManagerStub(
          new StandaloneServiceManager(applet.getAppletContext(), codeBase, this.name));
      // Caution: setting a new service manager stub won't replace the existing one,
    }          
 
    initLookAndFeel();
   
    // Add a listener that changes the content pane of the current active applet 
    // when a home is added to application
    addHomesListener(new CollectionListener<Home>() {
        private boolean firstHome = true;
        
        public void collectionChanged(CollectionEvent<Home> ev) {
          Home home = ev.getItem();
          switch (ev.getType()) {
            case ADD :
              try {
                final HomeController controller = createHomeController(home);
                // Change applet content 
                applet.setContentPane((JComponent)controller.getView());
                applet.getRootPane().revalidate();

                // Open specified home at launch time if it exits
                if (this.firstHome) {
                  this.firstHome = false;
                  if (defaultHome.length() > 0 && readHomeURL.length() != 0) {
                    controller.open(defaultHome);
                  }
                }
              } catch (IllegalRenderingStateException ex) {
                ex.printStackTrace();
                show3DError();
              }
              break;
          }
        }
      });

    addComponent3DRenderingErrorObserver();
    
    EventQueue.invokeLater(new Runnable() {
        public void run() {
          // Create a home in Event Dispatch Thread 
          addHome(createHome());
          
          if (showMemoryStatus) {
            final String memoryStatus = getUserPreferences().getLocalizedString(AppletApplication.class, "memoryStatus");
            // Launch a timer that displays memory used by the applet 
            memoryStatusTimer = new Timer(1000, new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                  Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                  if (focusOwner != null && SwingUtilities.isDescendingFrom(focusOwner, applet)) {
                    Runtime runtime = Runtime.getRuntime();
                    applet.showStatus(String.format(memoryStatus, 
                        Math.round(100f * (runtime.totalMemory() - runtime.freeMemory()) / runtime.maxMemory()),
                        runtime.maxMemory() / 1024 / 1024));
                  }
                }
              });
            memoryStatusTimer.start();
          }
        }
      });
  }
  
  /**
   * Deletes open homes and clears all the resources used by this application.
   * This method is called when an applet is destroyed.  
   */
  public void destroy() {
    if (this.memoryStatusTimer != null) {
      this.memoryStatusTimer.stop();
      this.memoryStatusTimer = null;
    }
    for (Home home : getHomes()) {
      // Delete directly home without closing it because when an applet is destroyed 
      // we can't control how long a warning dialog about unsaved home will be displayed 
      deleteHome(home);
    }
    // Collect deleted objects (seems to be required under Mac OS X when the applet is being reloaded)
    System.gc();
    // Stop managers threads
    IconManager.getInstance().clear();
    TextureManager.getInstance().clear();
    ModelManager.getInstance().clear();
  }

  /**
   * Returns <code>true</code> if one of the homes of this application is modified.
   */
  public boolean isModified() {
    for (Home home : getHomes()) {
      if (home.isModified()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the array of URL objects matching the URL list.
   */
  private URL [] getURLs(URL codeBase, String urlList) {
    String [] urlStrings = urlList.split("\\s|,");
    List<URL> urls = new ArrayList<URL>(urlStrings.length);
    for (String urlString : urlStrings) {
      URL url = getURLWithCodeBase(codeBase, urlString);
      if (url != null) {
        urls.add(url);
      }
    }
    return urls.toArray(new URL [urls.size()]);
  }
  
  /**
   * Returns the URL object matching the given <code>url</code> eventually relative to <code>codeBase</code>.
   */
  private URL getURLWithCodeBase(URL codeBase, String url) {
    if (url.length() > 0) {
      try {
        return new URL(codeBase, url);
      } catch (MalformedURLException ex) {
        // Ignore malformed URLs
      }
    }
    return null;
  }
  
  /**
   * Returns the URL matching the given <code>url</code> eventually relative to <code>codeBase</code>.
   */
  private String getURLStringWithCodeBase(URL codeBase, String url) {
    if (url.length() > 0) {
      try {
        return new URL(codeBase, url).toString();
      } catch (MalformedURLException ex) {
        // Ignore malformed URLs
      }
    }
    return null;
  }
  
  /**
   * Returns the parameter value of the given <code>parameter</code> or 
   * <code>defaultValue</code> if it doesn't exist.
   */
  private String getAppletParameter(JApplet applet, String parameter, String defaultValue) {
    String parameterValue = applet.getParameter(parameter);
    if (parameterValue == null) {
      return defaultValue;
    } else {
      return parameterValue;
    }
  }
  
  /**
   * Returns the parameter value of the given <code>parameter</code> or 
   * <code>false</code> if it doesn't exist.
   */
  private boolean getAppletBooleanParameter(JApplet applet, String parameter) {
    return "true".equalsIgnoreCase(getAppletParameter(applet, parameter, "false"));
  }
  
  /**
   * Returns a new instance of a home controller after <code>home</code> was created.
   */
  protected HomeController createHomeController(Home home) {
    final String writeHomeURL = getAppletParameter(applet, WRITE_HOME_URL_PARAMETER, "writeHome.php");    
    final String readHomeURL = getAppletParameter(applet, READ_HOME_URL_PARAMETER, "readHome.php?home=%s");
    final String listHomesURL = getAppletParameter(applet, LIST_HOMES_URL_PARAMETER, "listHomes.php");
    final String defaultHome = getAppletParameter(applet, DEFAULT_HOME_PARAMETER, "");    
    
    // Create a home controller for new home
    boolean newHomeEnabled = 
        writeHomeURL.length() != 0 && listHomesURL.length() != 0;
    boolean openEnabled = 
        readHomeURL.length() != 0 && listHomesURL.length() != 0;
    boolean saveEnabled = writeHomeURL.length() != 0 
        && (defaultHome.length() != 0 || listHomesURL.length() != 0);
    boolean saveAsEnabled = 
        writeHomeURL.length() != 0 && listHomesURL.length() != 0;
    
    final HomeController controller = new HomeAppletController(
        home, AppletApplication.this, getViewFactory(), getContentManager(), getPluginManager(),
        newHomeEnabled, openEnabled, saveEnabled, saveAsEnabled);
    
    JRootPane homeView = (JRootPane)controller.getView();
    // Remove menu bar
    homeView.setJMenuBar(null);
    
    // As the applet has no menu, activate accelerators directly on home view
    for (HomeView.ActionType actionType : HomeView.ActionType.values()) {
      ResourceAction.MenuItemAction menuAction = new ResourceAction.MenuItemAction(homeView.getActionMap().get(actionType));
      KeyStroke accelerator = (KeyStroke)menuAction.getValue(Action.ACCELERATOR_KEY);
      if (accelerator != null) {
        homeView.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(accelerator, actionType);
      }
    }
    
    // Change default buttons in toolbar
    JToolBar toolBar = (JToolBar)homeView.getContentPane().getComponent(0);
    toolBar.setFloatable(false);    
    // Retrieve all buttons that are plug-in actions
    List<JComponent> pluginButtons = new ArrayList<JComponent>();
    for (int i = 0; i < toolBar.getComponentCount(); i++) {
      JComponent component = (JComponent)toolBar.getComponent(i);
      if (component instanceof AbstractButton
          && ((AbstractButton)component).getAction().
                getValue(PluginAction.Property.TOOL_BAR.name()) == Boolean.TRUE) {
        pluginButtons.add(component);
      }
    }
    toolBar.removeAll();
    // Add New, Open, Save, Save as buttons if they are enabled
    Action newHomeAction = getToolBarAction(homeView, HomeView.ActionType.NEW_HOME);
    if (newHomeAction.isEnabled()) {
      toolBar.add(newHomeAction);
    }
    Action openAction = getToolBarAction(homeView, HomeView.ActionType.OPEN);
    if (openAction.isEnabled()) {
      toolBar.add(openAction);
    }
    Action saveAction = getToolBarAction(homeView, HomeView.ActionType.SAVE);
    if (saveAction.isEnabled()) {
      toolBar.add(saveAction);
    }
    Action saveAsAction = getToolBarAction(homeView, HomeView.ActionType.SAVE_AS);
    if (saveAsAction.isEnabled()) {
      toolBar.add(saveAsAction);
    }
    
    if (getAppletBooleanParameter(this.applet, ENABLE_EXPORT_TO_SH3D)) {
      try {
        // Add export to SH3D action
        Action exportToSH3DAction = new ControllerAction(getUserPreferences(), 
            AppletApplication.class, "EXPORT_TO_SH3D", controller, "exportToSH3D");
        exportToSH3DAction.setEnabled(true);
        toolBar.add(new ResourceAction.ToolBarAction(exportToSH3DAction));
      } catch (NoSuchMethodException ex) {
        ex.printStackTrace();
      }
    }
    
    if (toolBar.getComponentCount() > 0) {
      toolBar.add(Box.createRigidArea(new Dimension(2, 2)));
    }
    toolBar.add(getToolBarAction(homeView, HomeView.ActionType.PAGE_SETUP));
    toolBar.add(getToolBarAction(homeView, HomeView.ActionType.PRINT));
    if (getAppletBooleanParameter(this.applet, ENABLE_PRINT_TO_PDF) && !OperatingSystem.isMacOSX()) {
      controller.getView().setEnabled(HomeView.ActionType.PRINT_TO_PDF, true);
      toolBar.add(getToolBarAction(homeView, HomeView.ActionType.PRINT_TO_PDF));
    }
    toolBar.add(Box.createRigidArea(new Dimension(2, 2)));
    toolBar.add(getToolBarAction(homeView, HomeView.ActionType.PREFERENCES));
    toolBar.addSeparator();

    toolBar.add(getToolBarAction(homeView, HomeView.ActionType.UNDO));
    toolBar.add(getToolBarAction(homeView, HomeView.ActionType.REDO));
    toolBar.add(Box.createRigidArea(new Dimension(2, 2)));
    toolBar.add(getToolBarAction(homeView, HomeView.ActionType.CUT));
    toolBar.add(getToolBarAction(homeView, HomeView.ActionType.COPY));
    toolBar.add(getToolBarAction(homeView, HomeView.ActionType.PASTE));
    toolBar.add(Box.createRigidArea(new Dimension(2, 2)));
    toolBar.add(getToolBarAction(homeView, HomeView.ActionType.DELETE));
    toolBar.addSeparator();

    final JToggleButton selectToggleButton = 
        new JToggleButton(getToolBarAction(homeView, HomeView.ActionType.SELECT));
    selectToggleButton.setSelected(true);
    toolBar.add(selectToggleButton);
    final JToggleButton createWallsToggleButton = 
        new JToggleButton(getToolBarAction(homeView, HomeView.ActionType.CREATE_WALLS));
    toolBar.add(createWallsToggleButton);
    final JToggleButton createRoomsToggleButton = 
        new JToggleButton(getToolBarAction(homeView, HomeView.ActionType.CREATE_ROOMS));
    toolBar.add(createRoomsToggleButton);
    final JToggleButton createDimensionLinesToggleButton = 
        new JToggleButton(getToolBarAction(homeView, HomeView.ActionType.CREATE_DIMENSION_LINES));
    toolBar.add(createDimensionLinesToggleButton);
    final JToggleButton createLabelsToggleButton = 
        new JToggleButton(getToolBarAction(homeView, HomeView.ActionType.CREATE_LABELS));
    toolBar.add(createLabelsToggleButton);
    // Add Select, Create Walls and Create dimensions buttons to radio group 
    ButtonGroup group = new ButtonGroup();
    group.add(selectToggleButton);
    group.add(createWallsToggleButton);
    group.add(createRoomsToggleButton);
    group.add(createDimensionLinesToggleButton);
    group.add(createLabelsToggleButton);
    controller.getPlanController().addPropertyChangeListener(PlanController.Property.MODE, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            PlanController.Mode mode = controller.getPlanController().getMode();
            selectToggleButton.setSelected(mode == PlanController.Mode.SELECTION);
            createWallsToggleButton.setSelected(mode == PlanController.Mode.WALL_CREATION);
            createRoomsToggleButton.setSelected(mode == PlanController.Mode.ROOM_CREATION);
            createDimensionLinesToggleButton.setSelected(mode == PlanController.Mode.DIMENSION_LINE_CREATION);
            createLabelsToggleButton.setSelected(mode == PlanController.Mode.LABEL_CREATION);
          }
        });
    toolBar.add(Box.createRigidArea(new Dimension(2, 2)));
    
    toolBar.add(getToolBarAction(homeView, HomeView.ActionType.ZOOM_OUT));
    toolBar.add(getToolBarAction(homeView, HomeView.ActionType.ZOOM_IN));

    boolean enableCreatePhoto = getAppletBooleanParameter(this.applet, ENABLE_CREATE_PHOTO);
    controller.getView().setEnabled(HomeView.ActionType.CREATE_PHOTO, enableCreatePhoto);
    if (enableCreatePhoto) {
      toolBar.addSeparator();
      toolBar.add(getToolBarAction(homeView, HomeView.ActionType.CREATE_PHOTO));
    }

    // Add plug-in buttons
    if (pluginButtons.size() > 0) {
      toolBar.addSeparator();
      for (JComponent pluginButton : pluginButtons) {
        toolBar.add(pluginButton);
      }
    }
    
    toolBar.addSeparator();
    toolBar.add(getToolBarAction(homeView, HomeView.ActionType.ABOUT));
    
    controller.getView().setEnabled(HomeView.ActionType.EXPORT_TO_SVG, 
        getAppletBooleanParameter(this.applet, ENABLE_EXPORT_TO_SVG));
    controller.getView().setEnabled(HomeView.ActionType.EXPORT_TO_OBJ, 
        getAppletBooleanParameter(this.applet, ENABLE_EXPORT_TO_OBJ));
    
    // Add a border
    homeView.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
    
    return controller;
  }

  /**
   * Returns an action decorated for tool bar buttons.
   */
  private Action getToolBarAction(JComponent homeView, HomeView.ActionType actionType) {
    return new ResourceAction.ToolBarAction(homeView.getActionMap().get(actionType));
  }
  
  /**
   * Returns a recorder able to write and read homes on server.
   */
  @Override
  public HomeRecorder getHomeRecorder() {
    if (this.homeRecorder == null) {
      URL codeBase = this.applet.getCodeBase();
      final String writeHomeURL = getAppletParameter(this.applet, WRITE_HOME_URL_PARAMETER, "writeHome.php");    
      final String readHomeURL = getAppletParameter(this.applet, READ_HOME_URL_PARAMETER, "readHome.php?home=%s");
      final String listHomesURL = getAppletParameter(this.applet, LIST_HOMES_URL_PARAMETER, "listHomes.php");
      this.homeRecorder = new HomeAppletRecorder(getURLStringWithCodeBase(codeBase, writeHomeURL), 
          getURLStringWithCodeBase(codeBase, readHomeURL), 
          getURLStringWithCodeBase(codeBase, listHomesURL));
    }
    return this.homeRecorder;
  }
  
  /**
   * Returns user preferences.
   */
  @Override
  public UserPreferences getUserPreferences() {
    // Initialize userPreferences lazily
    if (this.userPreferences == null) {
      URL codeBase = this.applet.getCodeBase();
      final String furnitureCatalogURLs = getAppletParameter(this.applet, FURNITURE_CATALOG_URLS_PARAMETER, "catalog.zip");
      final String texturesCatalogURLs = getAppletParameter(this.applet, TEXTURES_CATALOG_URLS_PARAMETER, "catalog.zip");
      final String readPreferencesURL = getAppletParameter(this.applet, READ_PREFERENCES_URL_PARAMETER, "");    
      final String writePreferencesURL = getAppletParameter(this.applet, WRITE_PREFERENCES_URL_PARAMETER, "");    
      final String userLanguage = getAppletParameter(this.applet, USER_LANGUAGE, null);    
      this.userPreferences = new AppletUserPreferences(
          getURLs(codeBase, furnitureCatalogURLs), 
          getURLs(codeBase, texturesCatalogURLs),
          getURLWithCodeBase(codeBase, writePreferencesURL), 
          getURLWithCodeBase(codeBase, readPreferencesURL),
          userLanguage);
    }
    return this.userPreferences;
  }

  /**
   * Returns a content manager able to handle files.
   */
  protected ContentManager getContentManager() {
    if (this.contentManager == null) {
      this.contentManager = new AppletContentManager(getHomeRecorder(), getUserPreferences());
    }
    return this.contentManager;
  }
  
  /**
   * Returns a Swing view factory. 
   */
  protected ViewFactory getViewFactory() {
    if (this.viewFactory == null) {
     this.viewFactory = new SwingViewFactory();
    }
    return this.viewFactory;
  }

  /**
   * Returns the plugin manager of this application. 
   */
  protected PluginManager getPluginManager() {
    if (this.pluginManager == null) {
      URL codeBase = this.applet.getCodeBase();
      String pluginURLs = getAppletParameter(this.applet, PLUGIN_URLS_PARAMETER, "");
      this.pluginManager = new PluginManager(getURLs(codeBase, pluginURLs));
    }
    return this.pluginManager;
  }

  /**
   * Returns applet name.
   */
  @Override
  public String getName() {
    return this.name;
  }
  
  /**
   * Returns information about the version of this applet application.
   */
  @Override
  public String getVersion() {
    String applicationVersion = getUserPreferences().getLocalizedString(
        AppletApplication.class, "applicationVersion");
    String versionInformation = System.getProperty("com.eteks.sweethome3d.deploymentInformation");
    if (versionInformation != null) {
      applicationVersion += " " + versionInformation;
    }
    return applicationVersion;
  }
  
  /**
   * Sets application look and feel.
   */
  private void initLookAndFeel() {
    try {
      // Apply current system look and feel
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      // Change default titled borders under Mac OS X 10.5
      if (OperatingSystem.isMacOSXLeopardOrSuperior()) {
        UIManager.put("TitledBorder.border", 
            UIManager.getBorder("TitledBorder.aquaVariant"));
      }
      // Enable applets to update their content while window resizing
      Toolkit.getDefaultToolkit().setDynamicLayout(true);
      SwingTools.updateSwingResourceLanguage();
    } catch (Exception ex) {
      // Too bad keep current look and feel
    }
  }
  
  /**
   * Sets the rendering error listener bound to Java 3D 
   * to avoid default System exit in case of error during 3D rendering. 
   */
  private void addComponent3DRenderingErrorObserver() {
    // Instead of adding a RenderingErrorListener directly to VirtualUniverse, 
    // we add it through Canvas3DManager, because offscreen rendering needs to check 
    // rendering errors with its own RenderingErrorListener
    Component3DManager.getInstance().setRenderingErrorObserver(
        new Component3DManager.RenderingErrorObserver() {
          public void errorOccured(int errorCode, String errorMessage) {
            System.err.print("Error in Java 3D : " + errorCode + " " + errorMessage);
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                  show3DError();
                }
              });
          }
        });
  }

  /**
   * Displays a message to user about a 3D error. 
   */
  private void show3DError() {
    String message = getUserPreferences().getLocalizedString(AppletApplication.class, "3DError.message");
    String title = getUserPreferences().getLocalizedString(AppletApplication.class, "3DError.title");
    JOptionPane.showMessageDialog(KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow(), 
        message, title, JOptionPane.ERROR_MESSAGE);
  }

  /**
   * JNLP <code>ServiceManagerStub</code> implementation for applets 
   * run out of Java Web Start. This service manager supports <code>BasicService</code> only.
   */
  private static class StandaloneServiceManager implements ServiceManagerStub {
    private BasicService basicService;

    public StandaloneServiceManager(AppletContext appletContext,
                                    URL codeBase,
                                    String appletName) {
      this.basicService = new AppletBasicService(appletContext, codeBase, appletName);
    }

    public Object lookup(final String name) throws UnavailableServiceException {
      if (name.equals("javax.jnlp.BasicService")) {
        return this.basicService;
      } else {
        throw new UnavailableServiceException(name);
      }
    }
    
    public String[] getServiceNames() {
      return new String[]  {"javax.jnlp.BasicService"};
    }
  }    

  /**
   * <code>BasicService</code> that displays a web page in the current browser.
   */
  private static class AppletBasicService implements BasicService {
    private final AppletContext appletContext;
    private final URL    codeBase;
    private final String appletName;

    public AppletBasicService(AppletContext appletContext,
                              URL codeBase, 
                              String appletName) {
      this.appletContext = appletContext;
      this.codeBase = codeBase;
      this.appletName = appletName;
    }

    public boolean showDocument(URL url) {
      this.appletContext.showDocument(url, this.appletName);
      return true;
    }

    public URL getCodeBase() {
      return this.codeBase;
    }

    public boolean isOffline() {
      return false;
    }

    public boolean isWebBrowserSupported() {
      return true;
    }
  }
}