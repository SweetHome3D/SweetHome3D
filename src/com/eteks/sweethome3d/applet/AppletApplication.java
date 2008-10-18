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
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.jnlp.ServiceManagerStub;
import javax.jnlp.UnavailableServiceException;
import javax.media.j3d.IllegalRenderingStateException;
import javax.media.j3d.RenderingError;
import javax.media.j3d.RenderingErrorListener;
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

import com.eteks.sweethome3d.model.ContentManager;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeApplication;
import com.eteks.sweethome3d.model.HomeEvent;
import com.eteks.sweethome3d.model.HomeListener;
import com.eteks.sweethome3d.model.HomeRecorder;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.swing.Component3DManager;
import com.eteks.sweethome3d.swing.HomeController;
import com.eteks.sweethome3d.swing.HomePane;
import com.eteks.sweethome3d.swing.PlanController;
import com.eteks.sweethome3d.swing.ResourceAction;
import com.eteks.sweethome3d.swing.HomePane.ActionType;
import com.eteks.sweethome3d.swing.PlanController.Mode;
import com.eteks.sweethome3d.tools.OperatingSystem;

/**
 * An application wrapper working in applet. 
 * @author Emmanuel Puybaret
 */
public class AppletApplication extends HomeApplication {
  private HomeRecorder       homeRecorder;
  private UserPreferences    userPreferences;
  private AppletContentManager contentManager;

  public AppletApplication(final JApplet applet) {
    String writeHomeURL = getAppletParameter(applet, "writeHomeURL", "writeHome.php");    
    String readHomeURL = getAppletParameter(applet, "readHomeURL", "readHome.php?home=%s");
    String checkHomeURL = getAppletParameter(applet, "checkHomeURL", "checkHome.php?home=%s");
    String listHomesURL = getAppletParameter(applet, "listHomesURL", "listHomes.php");

    this.homeRecorder = new HomeAppletRecorder(writeHomeURL, readHomeURL, checkHomeURL, listHomesURL);
    this.userPreferences = new AppletUserPreferences();
    this.contentManager = new AppletContentManager(this.homeRecorder);

    // If Sweet Home 3D applet is launched from outside of Java Web Start
    if (ServiceManager.getServiceNames() == null) {
      // Create JNLP services required by Sweet Home 3D 
      ServiceManager.setServiceManagerStub(
          new StandaloneServiceManager(applet.getAppletContext(), applet.getCodeBase()));
    }      

    initLookAndFeel();
   
    // Add a listener that changes the content pane of the current active applet 
    // when a home is added to application
    addHomeListener(new HomeListener() {
        public void homeChanged(HomeEvent ev) {
          switch (ev.getType()) {
            case ADD :
              Home home = ev.getHome();
              try {
                // Create a home controller for new home
                HomeAppletController controller = new HomeAppletController(home, AppletApplication.this);
                // Display its view in applet
                updateAppletView(applet, controller);
              } catch (IllegalRenderingStateException ex) {
                ex.printStackTrace();
                show3DError();
              }
              break;
          }
        }
      });

    addComponent3DRenderingErrorListener();
    
    // Create a home 
    addHome(new Home());      
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
  private void updateAppletView(final JApplet applet, final HomeController controller) {
    HomePane homeView = (HomePane)controller.getView();
    // Remove menu bar
    homeView.setJMenuBar(null);
    
    // As the applet has no menu, activate accelerators directly on home view
    for (ActionType actionType : HomePane.ActionType.values()) {
      ResourceAction.MenuAction menuAction = new ResourceAction.MenuAction(homeView.getActionMap().get(actionType));
      KeyStroke accelerator = (KeyStroke)menuAction.getValue(Action.ACCELERATOR_KEY);
      if (accelerator != null) {
        homeView.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(accelerator, actionType);
      }
    }
    
    // Change default buttons in toolbar
    JToolBar toolBar = (JToolBar)homeView.getContentPane().getComponent(0);
    toolBar.setFloatable(false);
    toolBar.removeAll();
    toolBar.add(getToolBarAction(homeView, ActionType.NEW_HOME));
    toolBar.add(getToolBarAction(homeView, ActionType.OPEN));
    toolBar.add(getToolBarAction(homeView, ActionType.SAVE));
    toolBar.add(getToolBarAction(homeView, ActionType.SAVE_AS));
    toolBar.add(Box.createRigidArea(new Dimension(2, 2)));
    toolBar.add(getToolBarAction(homeView, ActionType.PAGE_SETUP));
    toolBar.add(getToolBarAction(homeView, ActionType.PRINT));
    toolBar.add(Box.createRigidArea(new Dimension(2, 2)));
    toolBar.add(getToolBarAction(homeView, ActionType.PREFERENCES));
    toolBar.addSeparator();

    toolBar.add(getToolBarAction(homeView, ActionType.UNDO));
    toolBar.add(getToolBarAction(homeView, ActionType.REDO));
    toolBar.add(Box.createRigidArea(new Dimension(2, 2)));
    toolBar.add(getToolBarAction(homeView, ActionType.CUT));
    toolBar.add(getToolBarAction(homeView, ActionType.COPY));
    toolBar.add(getToolBarAction(homeView, ActionType.PASTE));
    toolBar.add(Box.createRigidArea(new Dimension(2, 2)));
    toolBar.add(getToolBarAction(homeView, ActionType.DELETE));
    toolBar.addSeparator();

    final JToggleButton selectToggleButton = 
        new JToggleButton(getToolBarAction(homeView, ActionType.SELECT));
    selectToggleButton.setSelected(true);
    toolBar.add(selectToggleButton);
    final JToggleButton createWallsToggleButton = 
        new JToggleButton(getToolBarAction(homeView, ActionType.CREATE_WALLS));
    toolBar.add(createWallsToggleButton);
    final JToggleButton createDimensionLinesToggleButton = 
        new JToggleButton(getToolBarAction(homeView, ActionType.CREATE_DIMENSION_LINES));
    toolBar.add(createDimensionLinesToggleButton);
    // Add Select, Create Walls and Create dimensions buttons to radio group 
    ButtonGroup group = new ButtonGroup();
    group.add(selectToggleButton);
    group.add(createWallsToggleButton);
    group.add(createDimensionLinesToggleButton);
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
    
    toolBar.add(getToolBarAction(homeView, ActionType.ZOOM_OUT));
    toolBar.add(getToolBarAction(homeView, ActionType.ZOOM_IN));
     
    // Add a border
    homeView.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
    // Change applet content 
    applet.setContentPane(homeView);
    applet.getRootPane().revalidate();
    if (OperatingSystem.isMacOSXLeopardOrSuperior()) {
      // Force focus traversal policy to ensure dividers and components of this kind won't get focus 
      final List<JComponent> focusableComponents = Arrays.asList(new JComponent [] {
          controller.getCatalogController().getView(),
          controller.getFurnitureController().getView(),
          controller.getPlanController().getView(),
          controller.getHomeController3D().getView()});      
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
  private Action getToolBarAction(JComponent homeView, ActionType actionType) {
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
   * Returns a content manager able to manage content local and server files. 
   */
  @Override
  public ContentManager getContentManager() {
    return this.contentManager;
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
    } catch (Exception ex) {
      // Too bad keep current look and feel
    }
  }
  
  /**
   * Sets the rendering error listener bound to Java 3D 
   * to avoid default System exit in case of error during 3D rendering. 
   */
  private void addComponent3DRenderingErrorListener() {
    // Instead of adding a RenderingErrorListener directly to VirtualUniverse, 
    // we add it through Canvas3DManager, because offscreen rendering needs to check 
    // rendering errors with its own RenderingErrorListener
    Component3DManager.getInstance().setRenderingErrorListener(new RenderingErrorListener() {
        public void errorOccurred(RenderingError error) {
          error.printVerbose();
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
    ResourceBundle resource = ResourceBundle.getBundle(AppletApplication.class.getName());
    String message = resource.getString("3DError.message");
    String title = resource.getString("3DError.title");
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