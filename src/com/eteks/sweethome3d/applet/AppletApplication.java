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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FocusTraversalPolicy;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import com.eteks.sweethome3d.j3d.Component3DManager;
import com.eteks.sweethome3d.j3d.TextureManager;
import com.eteks.sweethome3d.model.CollectionEvent;
import com.eteks.sweethome3d.model.CollectionListener;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeApplication;
import com.eteks.sweethome3d.model.HomeRecorder;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.plugin.PluginAction;
import com.eteks.sweethome3d.plugin.PluginManager;
import com.eteks.sweethome3d.swing.HomePane;
import com.eteks.sweethome3d.swing.IconManager;
import com.eteks.sweethome3d.swing.ResourceAction;
import com.eteks.sweethome3d.swing.SwingTools;
import com.eteks.sweethome3d.swing.SwingViewFactory;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.ContentManager;
import com.eteks.sweethome3d.viewcontroller.HomeController;
import com.eteks.sweethome3d.viewcontroller.PlanController;
import com.eteks.sweethome3d.viewcontroller.ViewFactory;
import com.eteks.sweethome3d.viewcontroller.PlanController.Mode;

/**
 * An application wrapper working in applet. 
 * @author Emmanuel Puybaret
 */
public class AppletApplication extends HomeApplication {
  private static final String FURNITURE_CATALOG_URLS_PARAMETER = "furnitureCatalogURLs";
  private static final String TEXTURES_CATALOG_URLS_PARAMETER  = "texturesCatalogURLs";
  private static final String PLUGIN_URLS_PARAMETER            = "pluginURLs";
  private static final String WRITE_HOME_URL_PARAMETER         = "writeHomeURL";
  private static final String READ_HOME_URL_PARAMETER          = "readHomeURL";
  private static final String LIST_HOMES_URL_PARAMETER         = "listHomesURL";
  private static final String DEFAULT_HOME_PARAMETER           = "defaultHome";
  
  private final HomeRecorder              homeRecorder;
  private final UserPreferences           userPreferences;
  private final Map<Home, HomeController> homeControllers;

  public AppletApplication(final JApplet applet) {
    final String furnitureCatalogURLs = getAppletParameter(applet, FURNITURE_CATALOG_URLS_PARAMETER, "catalog.zip");
    final String texturesCatalogURLs = getAppletParameter(applet, TEXTURES_CATALOG_URLS_PARAMETER, "catalog.zip");
    final String pluginURLs = getAppletParameter(applet, PLUGIN_URLS_PARAMETER, "");
    final String writeHomeURL = getAppletParameter(applet, WRITE_HOME_URL_PARAMETER, "writeHome.php");    
    final String readHomeURL = getAppletParameter(applet, READ_HOME_URL_PARAMETER, "readHome.php?home=%s");
    final String listHomesURL = getAppletParameter(applet, LIST_HOMES_URL_PARAMETER, "listHomes.php");
    final String defaultHome = getAppletParameter(applet, DEFAULT_HOME_PARAMETER, "");    

    this.homeRecorder = new HomeAppletRecorder(writeHomeURL, readHomeURL, listHomesURL);
    this.userPreferences = new AppletUserPreferences(
        getURLs(applet.getCodeBase(), furnitureCatalogURLs), 
        getURLs(applet.getCodeBase(), texturesCatalogURLs));
    this.homeControllers = new HashMap<Home, HomeController>();
    
    final ViewFactory viewFactory = new SwingViewFactory();
    final ContentManager contentManager = new AppletContentManager(this.homeRecorder, this.userPreferences);
    final PluginManager  pluginManager  = new PluginManager(
        getURLs(applet.getCodeBase(), pluginURLs));

    // If Sweet Home 3D applet is launched from outside of Java Web Start
    if (ServiceManager.getServiceNames() == null) {
      // Create JNLP services required by Sweet Home 3D 
      ServiceManager.setServiceManagerStub(
          new StandaloneServiceManager(applet.getAppletContext(), applet.getCodeBase()));
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
                // Create a home controller for new home
                boolean newHomeEnabled = 
                    writeHomeURL.length() != 0 && listHomesURL.length() != 0;
                boolean openEnabled = 
                    readHomeURL.length() != 0 && listHomesURL.length() != 0;
                boolean saveEnabled = writeHomeURL.length() != 0 
                    && (defaultHome.length() != 0 || listHomesURL.length() != 0);
                boolean saveAsEnabled = 
                    writeHomeURL.length() != 0 && listHomesURL.length() != 0;
                
                final HomeAppletController controller = new HomeAppletController(
                    home, AppletApplication.this, viewFactory, contentManager, pluginManager,
                    newHomeEnabled, openEnabled, saveEnabled, saveAsEnabled);
                homeControllers.put(home, controller);
                
                // Display its view in applet
                updateAppletView(applet, controller);
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
            case DELETE :
              homeControllers.remove(home);
              break;
          }
        }
      });

    addComponent3DRenderingErrorObserver();
    
    EventQueue.invokeLater(new Runnable() {
        public void run() {
          // Create a home in Event Dispatch Thread 
          addHome(new Home());
        }
      });
  }
  
  /**
   * Closes open homes and clears all the resources used by this application.
   * This method is called when an applet is destroyed.  
   */
  public void destroy() {
    for (Home home : getHomes()) {
      this.homeControllers.get(home).close();
    }
    IconManager.getInstance().clear();
    TextureManager.getInstance().clear();
  }
  
  /**
   * Returns the array of URL objects matching the URL list.
   */
  private URL [] getURLs(URL codeBase, String urlList) {
    String [] urlStrings = urlList.split("\\s|,");
    List<URL> urls = new ArrayList<URL>(urlStrings.length);
    for (String urlString : urlStrings) {
      try {
        urls.add(new URL(codeBase, urlString));
      } catch (MalformedURLException ex) {
        // Ignore malformed URLs
      }
    }
    return urls.toArray(new URL [urls.size()]);
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
   * Updates the applet content pane with <code>controller</code> view. 
   */
  private void updateAppletView(final JApplet applet, 
                                final HomeController controller) {
    HomePane homeView = (HomePane)controller.getView();
    // Remove menu bar
    homeView.setJMenuBar(null);
    
    // As the applet has no menu, activate accelerators directly on home view
    for (HomePane.ActionType actionType : HomePane.ActionType.values()) {
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
    Action newHomeAction = getToolBarAction(homeView, HomePane.ActionType.NEW_HOME);
    if (newHomeAction.isEnabled()) {
      toolBar.add(newHomeAction);
    }
    Action openAction = getToolBarAction(homeView, HomePane.ActionType.OPEN);
    if (openAction.isEnabled()) {
      toolBar.add(openAction);
    }
    Action saveAction = getToolBarAction(homeView, HomePane.ActionType.SAVE);
    if (saveAction.isEnabled()) {
      toolBar.add(saveAction);
    }
    Action saveAsAction = getToolBarAction(homeView, HomePane.ActionType.SAVE_AS);
    if (saveAsAction.isEnabled()) {
      toolBar.add(saveAsAction);
    }
    if (toolBar.getComponentCount() > 0) {
      toolBar.add(Box.createRigidArea(new Dimension(2, 2)));
    }
    toolBar.add(getToolBarAction(homeView, HomePane.ActionType.PAGE_SETUP));
    toolBar.add(getToolBarAction(homeView, HomePane.ActionType.PRINT));
    toolBar.add(Box.createRigidArea(new Dimension(2, 2)));
    toolBar.add(getToolBarAction(homeView, HomePane.ActionType.PREFERENCES));
    toolBar.addSeparator();

    toolBar.add(getToolBarAction(homeView, HomePane.ActionType.UNDO));
    toolBar.add(getToolBarAction(homeView, HomePane.ActionType.REDO));
    toolBar.add(Box.createRigidArea(new Dimension(2, 2)));
    toolBar.add(getToolBarAction(homeView, HomePane.ActionType.CUT));
    toolBar.add(getToolBarAction(homeView, HomePane.ActionType.COPY));
    toolBar.add(getToolBarAction(homeView, HomePane.ActionType.PASTE));
    toolBar.add(Box.createRigidArea(new Dimension(2, 2)));
    toolBar.add(getToolBarAction(homeView, HomePane.ActionType.DELETE));
    toolBar.addSeparator();

    final JToggleButton selectToggleButton = 
        new JToggleButton(getToolBarAction(homeView, HomePane.ActionType.SELECT));
    selectToggleButton.setSelected(true);
    toolBar.add(selectToggleButton);
    final JToggleButton createWallsToggleButton = 
        new JToggleButton(getToolBarAction(homeView, HomePane.ActionType.CREATE_WALLS));
    toolBar.add(createWallsToggleButton);
    final JToggleButton createRoomsToggleButton = 
        new JToggleButton(getToolBarAction(homeView, HomePane.ActionType.CREATE_ROOMS));
    toolBar.add(createRoomsToggleButton);
    final JToggleButton createDimensionLinesToggleButton = 
        new JToggleButton(getToolBarAction(homeView, HomePane.ActionType.CREATE_DIMENSION_LINES));
    toolBar.add(createDimensionLinesToggleButton);
    final JToggleButton createLabelsToggleButton = 
        new JToggleButton(getToolBarAction(homeView, HomePane.ActionType.CREATE_LABELS));
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
            Mode mode = controller.getPlanController().getMode();
            selectToggleButton.setSelected(mode == PlanController.Mode.SELECTION);
            createWallsToggleButton.setSelected(mode == PlanController.Mode.WALL_CREATION);
            createDimensionLinesToggleButton.setSelected(mode == PlanController.Mode.DIMENSION_LINE_CREATION);
          }
        });
    toolBar.add(Box.createRigidArea(new Dimension(2, 2)));
    
    toolBar.add(getToolBarAction(homeView, HomePane.ActionType.ZOOM_OUT));
    toolBar.add(getToolBarAction(homeView, HomePane.ActionType.ZOOM_IN));
    
    // Add plug-in buttons
    if (pluginButtons.size() > 0) {
      toolBar.addSeparator();
      for (JComponent pluginButton : pluginButtons) {
        toolBar.add(pluginButton);
      }
    }
    
    // Add a border
    homeView.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
    // Change applet content 
    applet.setContentPane(homeView);
    applet.getRootPane().revalidate();
    if (OperatingSystem.isMacOSXLeopardOrSuperior()) {
      // Force focus traversal policy to ensure dividers and components of this kind won't get focus 
      final List<JComponent> focusableComponents = Arrays.asList(new JComponent [] {
          (JComponent)controller.getFurnitureCatalogController().getView(),
          (JComponent)controller.getFurnitureController().getView(),
          (JComponent)controller.getPlanController().getView(),
          (JComponent)controller.getHomeController3D().getView()});      
      applet.setFocusTraversalPolicy(new FocusTraversalPolicy() {
          @Override
          public Component getComponentAfter(Container container, Component component) {
            return focusableComponents.get((focusableComponents.indexOf(component) + 1) % focusableComponents.size());
          }
    
          @Override
          public Component getComponentBefore(Container container, Component component) {
            return focusableComponents.get((focusableComponents.indexOf(component) - 1) % focusableComponents.size());
          }
    
          @Override
          public Component getDefaultComponent(Container container) {
            return focusableComponents.get(0);
          }
    
          @Override
          public Component getFirstComponent(Container container) {
            return focusableComponents.get(0);
          }
    
          @Override
          public Component getLastComponent(Container container) {
            return focusableComponents.get(focusableComponents.size() - 1);
          }
        });
    }
  }

  /**
   * Returns an action decorated for tool bar buttons.
   */
  private Action getToolBarAction(JComponent homeView, HomePane.ActionType actionType) {
    return new ResourceAction.ToolBarAction(homeView.getActionMap().get(actionType));
  }
  
  /**
   * Returns a recorder able to write and read homes on server.
   */
  @Override
  public HomeRecorder getHomeRecorder() {
    return this.homeRecorder;
  }
  
  /**
   * Returns user preferences stored in resources.
   */
  @Override
  public UserPreferences getUserPreferences() {
    return this.userPreferences;
  }
  
  /**
   * Returns information about the version of this applet application.
   */
  public String getVersion() {
    String applicationVersion = this.userPreferences.getLocalizedString(
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
                                    URL codeBase) {
      this.basicService = new AppletBasicService(appletContext, codeBase);
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
    private final URL codeBase;

    public AppletBasicService(AppletContext appletContext,
                              URL codeBase) {
      this.appletContext = appletContext;
      this.codeBase = codeBase;
    }

    public boolean showDocument(URL url) {
      this.appletContext.showDocument(url, "SweetHome3D");
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