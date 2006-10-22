/*
 * HomeApplicationWindow.java 10 aout 2006
 *
 * Copyright (c) 2006 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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
package com.eteks.sweethome3d.jface;

import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import com.eteks.sweethome3d.model.Catalog;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeApplication;
import com.eteks.sweethome3d.model.HomeEvent;
import com.eteks.sweethome3d.model.HomeListener;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.swing.HomeComponent3D;
import com.eteks.sweethome3d.viewcontroller.CatalogController;
import com.eteks.sweethome3d.viewcontroller.CatalogView;
import com.eteks.sweethome3d.viewcontroller.FurnitureController;
import com.eteks.sweethome3d.viewcontroller.FurnitureView;
import com.eteks.sweethome3d.viewcontroller.HomeController;
import com.eteks.sweethome3d.viewcontroller.HomeView;
import com.eteks.sweethome3d.viewcontroller.PlanController;
import com.eteks.sweethome3d.viewcontroller.PlanView;
import com.eteks.sweethome3d.viewcontroller.ViewFactory;

/**
 * The MVC application view of Sweet Home 3D. This class implements <code>ViewFactory</code>
 * interface to keep control on the creation order of components and their parent. 
 * @author Emmanuel Puybaret
 */
public class HomeApplicationWindow extends ApplicationWindow implements ViewFactory, HomeView {
  private static int      newHomeCount;
  private int             newHomeNumber;
  private HomeController  controller;
  private Home            home;
  private UserPreferences preferences;
  private HomeApplication application;
  
  private ResourceBundle  resource;
  private Image           shellImage16x16;
  private Image           shellImage128x128;

  private SashForm        mainSashForm;
  private SashForm        catalogFurnitureSashForm;
  private SashForm        planView3DSashForm;
  private Map<ActionType, ResourceAction> actions;

  private static final String SWEET_HOME_3D_EXTENSION = ".sh3d";
  private static final String SWEET_HOME_3D_FILTER_NAME = "Sweet Home 3D"; 
  private static String currentPath;
  
  public HomeApplicationWindow(Home home, HomeApplication application) {
    this(home, application.getUserPreferences(), application);
  }

  public HomeApplicationWindow(Home home, UserPreferences preferences) {
    this(home, preferences, null);
  }
  
  private HomeApplicationWindow(Home home, UserPreferences preferences, 
                                HomeApplication application) {
    super(null);
    this.home = home;
    this.preferences = preferences;
    this.application = application;
    this.resource = ResourceBundle.getBundle(
        HomeApplicationWindow.class.getName());
    // If home is unnamed, give it a number
    if (home.getName() == null) {
      newHomeNumber = ++newHomeCount;
    }
    
    // Create actions first because createToolBarManager and createMenuManager needs them 
    createActions();
    addMenuBar();
    addToolBar(SWT.FLAT);
  }

  @Override
  protected void configureShell(Shell shell) {
    super.configureShell(shell);
    //  Update frame image ans title 
    try {
      // Load shell images in two different resolutions 
      // to let the system choose the one that fits best
      InputStream iconStream = HomeApplicationWindow.class.
          getResource("resources/frameIcon16x16.png").openStream();
      this.shellImage16x16 = new Image(Display.getCurrent(), iconStream);
      iconStream.close();
      iconStream = HomeApplicationWindow.class.
          getResource("resources/frameIcon128x128.png").openStream();
      this.shellImage128x128 = new Image(Display.getCurrent(), iconStream);
      shell.setImages(new Image [] {this.shellImage16x16, this.shellImage128x128});
      iconStream.close();
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
    updateFrameTitle(shell, this.home);
  }

  @Override
  protected Control createContents(Composite parent) {
    this.mainSashForm = new SashForm(parent, SWT.HORIZONTAL);
    this.catalogFurnitureSashForm = new SashForm(mainSashForm, SWT.VERTICAL);
    this.planView3DSashForm = new SashForm(mainSashForm, SWT.VERTICAL);
    // Create controller and the other view components
    this.controller = new HomeController(this, this.home, this.application);

    // Compute shell size and location
    computeShellBounds(getShell());
    // Add listeners to model and frame    
    addListeners(this.home, this.application, this.controller, getShell());
    return parent;
  }

  /**
   * Add listeners to <code>shell</code> and model objects.
   */
  private void addListeners(final Home home,
                            final HomeApplication application,
                            final HomeController controller,
                            final Shell shell) {
    // Control shell closing 
    shell.addShellListener(new ShellAdapter () {
        @Override
        public void shellClosed(ShellEvent e) {
          controller.close();
          shellImage16x16.dispose();
          shellImage128x128.dispose();
        }
      });
    // Dispose shell when a home is deleted 
    if (application != null) {
      application.addHomeListener(new HomeListener() {
          public void homeChanged(HomeEvent ev) {
            if (ev.getHome() == home
                && ev.getType() == HomeEvent.Type.DELETE) {
              application.removeHomeListener(this);
              shell.dispose();
            }
          };
        });
    }
    // Update title when the name or the modified state of home changes
    home.addPropertyChangeListener("name", new PropertyChangeListener () {
        public void propertyChange(PropertyChangeEvent ev) {
          updateFrameTitle(shell, home);
        }
      });
    home.addPropertyChangeListener("modified", new PropertyChangeListener () {
        public void propertyChange(PropertyChangeEvent ev) {
          updateFrameTitle(shell, home);
        }
      });

    // Can't bind controller to Mac OS X application menu : 
    // com.apple.eawt.Application works only for AWT/Swing
  }

  @Override
  protected ToolBarManager createToolBarManager(int style) {    
    ToolBarManager toolBarManager = new ToolBarManager(style);
    toolBarManager.add(this.actions.get(ActionType.NEW_HOME));
    toolBarManager.add(this.actions.get(ActionType.OPEN));
    toolBarManager.add(this.actions.get(ActionType.SAVE));
    toolBarManager.add(new Separator());

    toolBarManager.add(this.actions.get(ActionType.ADD_HOME_FURNITURE));
    toolBarManager.add(this.actions.get(ActionType.DELETE_HOME_FURNITURE));
    toolBarManager.add(new Separator());
    
    toolBarManager.add(this.actions.get(ActionType.WALL_CREATION));
    toolBarManager.add(this.actions.get(ActionType.DELETE_SELECTION));
    toolBarManager.add(new Separator());
    
    toolBarManager.add(this.actions.get(ActionType.UNDO));
    toolBarManager.add(this.actions.get(ActionType.REDO));
    return toolBarManager;
  }
  
  @Override
  protected MenuManager createMenuManager() {
    // Create main menu manager
    MenuManager menuManager = new MenuManager();

    // Create File menu
    MenuManager fileMenuManager = 
      new MenuManager(new ResourceAction(this.resource, "FILE_MENU").getText());
    menuManager.add(fileMenuManager);
    fileMenuManager.add(actions.get(ActionType.NEW_HOME));
    fileMenuManager.add(actions.get(ActionType.OPEN));
    fileMenuManager.add(new Separator());
    fileMenuManager.add(actions.get(ActionType.CLOSE));
    fileMenuManager.add(actions.get(ActionType.SAVE));
    fileMenuManager.add(actions.get(ActionType.SAVE_AS));
    fileMenuManager.add(new Separator());
    fileMenuManager.add(actions.get(ActionType.EXIT));

    // Create Edit menu manager
    MenuManager editMenuManager = 
      new MenuManager(new ResourceAction(this.resource, "EDIT_MENU").getText());
    menuManager.add(editMenuManager);
    editMenuManager.add(this.actions.get(ActionType.UNDO));
    editMenuManager.add(this.actions.get(ActionType.REDO));

    // Create Furniture menu manager
    MenuManager furnitureMenuManager = 
      new MenuManager(new ResourceAction(this.resource, "FURNITURE_MENU").getText());
    menuManager.add(furnitureMenuManager);
    furnitureMenuManager.add(this.actions.get(ActionType.ADD_HOME_FURNITURE));
    furnitureMenuManager.add(this.actions.get(ActionType.DELETE_HOME_FURNITURE));

    
    // Create Plan menu
    MenuManager planMenuManager = 
      new MenuManager(new ResourceAction(this.resource, "PLAN_MENU").getText());
    menuManager.add(planMenuManager);
    planMenuManager.add(this.actions.get(ActionType.WALL_CREATION));
    planMenuManager.add(this.actions.get(ActionType.DELETE_SELECTION));
    
    return menuManager;
  }

  /**
   * Create menu and tool bar actions. 
   */
  private void createActions() {
    this.actions = new HashMap<ActionType, ResourceAction>();
    this.actions.put(ActionType.NEW_HOME,
        new ResourceAction(this.resource, ActionType.NEW_HOME.toString()) {
          @Override
          public void run() {
            controller.newHome();
          }
        });
    this.actions.put(ActionType.OPEN,
        new ResourceAction(this.resource, ActionType.OPEN.toString()) {
          @Override
          public void run() {
            controller.open();
          }
        });
    this.actions.put(ActionType.CLOSE,
        new ResourceAction(this.resource, ActionType.CLOSE.toString()) {
          @Override
          public void run() {
            controller.close();
          }
        });
    this.actions.put(ActionType.SAVE,
        new ResourceAction(this.resource, ActionType.SAVE.toString()) {
          @Override
          public void run() {
            controller.save();
          }
        });
    this.actions.put(ActionType.SAVE_AS,
        new ResourceAction(this.resource, ActionType.SAVE_AS.toString()) {
          @Override
          public void run() {
            controller.saveAs();
          }
        });
    this.actions.put(ActionType.EXIT,
        new ResourceAction(this.resource, ActionType.EXIT.toString()) {
          @Override
          public void run() {
            controller.exit();
          }
        });
    this.actions.put(ActionType.ADD_HOME_FURNITURE,
      new ResourceAction(this.resource, ActionType.ADD_HOME_FURNITURE.toString()) {
        @Override
        public void run() {
          controller.addHomeFurniture();
        }
      });
    this.actions.put(ActionType.DELETE_HOME_FURNITURE,
      new ResourceAction(this.resource, ActionType.DELETE_HOME_FURNITURE.toString()) {
        @Override
        public void run() {
          controller.getFurnitureController().deleteSelection();
        }
      });
    this.actions.put(ActionType.UNDO,
      new ResourceAction(this.resource, ActionType.UNDO.toString()){
        @Override
        public void run() {
          controller.undo();
        }
      });
    this.actions.put(ActionType.REDO,
      new ResourceAction(this.resource, ActionType.REDO.toString()){
        @Override
        public void run() {
          controller.redo();
        }
      });
    this.actions.put(ActionType.WALL_CREATION,
        new ResourceAction (this.resource, ActionType.WALL_CREATION.toString()) {
          @Override
          public int getStyle() {
            return AS_CHECK_BOX;
          }

          @Override
          public void run() {
            if (isChecked()) {
              controller.setWallCreationMode();
            } else {
              controller.setSelectionMode();
            }
          }
        });
    this.actions.put(ActionType.DELETE_SELECTION,
      new ResourceAction(this.resource, ActionType.DELETE_SELECTION.toString()) {
        @Override
        public void run() {
          controller.getPlanController().deleteSelection();
        }
      });
  }

  /**
   * Enables or disables the action matching <code>actionType</code>.
   */
  public void setEnabled(ActionType actionType, 
                         boolean enabled) {
    this.actions.get(actionType).setEnabled(enabled);
  }

  /**
   * Sets the <code>NAME</code> and <code>SHORT_DESCRIPTION</code> properties value 
   * of undo and redo actions. If a parameter is null,
   * the properties will be reset to their initial values.
   */
  public void setUndoRedoName(String undoText, String redoText) {
    setNameAndShortDescription(ActionType.UNDO, undoText);
    setNameAndShortDescription(ActionType.REDO, redoText);
  }

  /**
   * Sets the <code>NAME</code> and <code>SHORT_DESCRIPTION</code> properties value 
   * matching <code>actionType</code>. If <code>name</code> is null,
   * the properties will be reset to their initial values.
   */
  private void setNameAndShortDescription(ActionType actionType, String name) {
    ResourceAction action = this.actions.get(actionType);
    if (name == null) {
      action.setText(action.getDefaultText());
      action.setToolTipText(action.getDefaultToolTipText());
    } else {
      action.setText("&" + name);
      action.setToolTipText(name);
    }
  }

  /**
   * Returns this application object. 
   */
  public HomeView createHomeView(Home home, UserPreferences preferences, HomeController controller) {
    return this;
  }

  public CatalogView createCatalogView(Catalog catalog, CatalogController controller) {
    return new CatalogTree(this.catalogFurnitureSashForm, catalog, controller);
  }

  public FurnitureView createFurnitureView(Home home, UserPreferences preferences, FurnitureController controller) {
    return new FurnitureTable(this.catalogFurnitureSashForm, home, preferences, controller);
  }

  public PlanView createPlanView(Home home, UserPreferences preferences, PlanController controller) {
    ScrolledComposite scrolledComposite = new ScrolledComposite(
        this.planView3DSashForm, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
    if (System.getProperty("os.name").startsWith("Mac OS X")) {
      scrolledComposite.setAlwaysShowScrollBars(true);
    }
    PlanViewer planViewer = new PlanViewer(scrolledComposite, home, preferences, controller);
    // Configure scrolledComposite content with planViewer control 
    scrolledComposite.setContent(planViewer.getControl());
    scrolledComposite.setMinSize(planViewer.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT));
    scrolledComposite.setExpandHorizontal(true);
    scrolledComposite.setExpandVertical(true);

    // Add a 3D view at bottom of planViewer
    createView3D(home);
    
    return planViewer;
  }

  /**
   * Creates a home 3D view.
   */
  private void createView3D(Home home) {
    Composite homeComponent3DParent = new Composite(this.planView3DSashForm, SWT.EMBEDDED);
    try {
      // Add a Swing HomeComponent3D component with SWT/AWT bridge
      Frame frame = SWT_AWT.new_Frame(homeComponent3DParent);
      frame.add(new HomeComponent3D(home));
    } catch (SWTError ex) {
      // If SWT/AWT bridge isn't supported, dispose 3D component
      homeComponent3DParent.dispose();
    }
  }

  /**
   * Displays a file dialog to open a .sh3d file.
   */
  public String showOpenDialog() {
    return showFileDialog(false, null);
  }

  /**
   * Displays a file dialog to save a home in a .sh3d file.
   */
  public String showSaveDialog(String name) {
    String file = showFileDialog(true, name);
    if (file != null && !file.toLowerCase().endsWith(SWEET_HOME_3D_EXTENSION)) {
      file += SWEET_HOME_3D_EXTENSION;
    }
    return file;
  }
  
  /**
   * Displays an SWT file dialog.
   */
  private String showFileDialog(boolean save, String name) {
    FileDialog fileDialog;
    if (save) {
      fileDialog = new FileDialog(getShell(), SWT.SAVE);
      fileDialog.setText(this.resource.getString("fileDialog.saveTitle"));
    } else {
      fileDialog = new FileDialog(getShell(), SWT.OPEN);
      fileDialog.setText(this.resource.getString("fileDialog.openTitle"));
    }
    // Choose default file 
    if (save && name != null) {
      fileDialog.setFileName(new File(name).getName());
    }
    // Set .sh3d files filter 
    fileDialog.setFilterExtensions(new String [] {"*" + SWEET_HOME_3D_EXTENSION});
    fileDialog.setFilterNames(new String [] {SWEET_HOME_3D_FILTER_NAME});
    // Update current directory
    if (currentPath != null) {
      fileDialog.setFilterPath(currentPath);
    }
    // Display file dialog
    String file = fileDialog.open();
    if (file != null) {
      // Retrieve current path for future calls
      currentPath = new File(file).getParent();
    } 
    return file;
  }

  /**
   * Displays <code>message</code> in an error message box.
   */
  public void showError(String message) {
    String title = this.resource.getString("error.title");
    MessageDialog.openError(getShell(), title, message);
  }

  /**
   * Displays a dialog that let user choose whether he wants to overwrite 
   * file <code>name</code> or not.
   * @return <code>true</code> if user confirmed to overwrite.
   */
  public boolean confirmOverwrite(String name) {
    // Retrieve displayed text in buttons and message
    String messageFormat = this.resource.getString("confirmOverwrite.message");
    String message = String.format(messageFormat, new File(name).getName());
    String title = this.resource.getString("confirmOverwrite.title");
    String replace = this.resource.getString("confirmOverwrite.overwrite");
    String cancel = this.resource.getString("confirmOverwrite.cancel");
    
    MessageDialog messageDialog = new MessageDialog(getShell(), title, null, message, 
        MessageDialog.QUESTION, new String [] {replace, cancel}, 1);
    return messageDialog.open() == 0; // 0 is the index of replace button
  }

  /**
   * Displays a dialog that let user choose whether he wants to save
   * the current home or not.
   * @return {@link SaveAnswer#SAVE} if user choosed to save home,
   * {@link SaveAnswer#DO_NOT_SAVE} if user don't want to save home,
   * or {@link SaveAnswer#CANCEL} if doesn't want to continue current operation.
   */
  public SaveAnswer confirmSave(String name) {
    // Retrieve displayed text in buttons and message
    String messageFormat = this.resource.getString("confirmSave.message");
    String message;
    if (name != null) {
      message = String.format(messageFormat, 
          "\"" + new File(name).getName() + "\"");
    } else {
      message = String.format(messageFormat, "");
    }
    String title = this.resource.getString("confirmSave.title");
    String save = this.resource.getString("confirmSave.save");
    String doNotSave = this.resource.getString("confirmSave.doNotSave");
    String cancel = this.resource.getString("confirmSave.cancel");

    MessageDialog messageDialog = new MessageDialog(getShell(), title, null, message, 
        MessageDialog.QUESTION, new String [] {save, doNotSave, cancel}, 0);
    
    switch (messageDialog.open()) {
      // Convert button index to SaveAnswer enum constants
      case 0 :
        return SaveAnswer.SAVE;
      case 1 :
        return SaveAnswer.DO_NOT_SAVE;
      default : return SaveAnswer.CANCEL;
    }
  }

  /**
   * Displays a dialog that let user choose whether he wants to exit 
   * application or not.
   * @return <code>true</code> if user confirmed to exit.
   */
  public boolean confirmExit() {
    String message = this.resource.getString("confirmExit.message");
    String title = this.resource.getString("confirmExit.title");
    return MessageDialog.openConfirm(getShell(), title, message);
  }
  
  /**
   * Computes <code>shell</code> size and location to fit into screen.
   */
  private void computeShellBounds(Shell frame) {
    frame.pack();
    Rectangle screenSize = frame.getDisplay().getClientArea();
    frame.setSize(Math.min(screenSize.width * 4 / 5, frame.getBounds().width), 
            Math.min(screenSize.height * 4 / 5, frame.getBounds().height));
  }

  /**
   * Updates <code>shell</code> title from <code>home</code> name.
   */
  private void updateFrameTitle(Shell shell, Home home) {
    String name = home.getName();
    if (name == null) {
      name = this.resource.getString("untitled"); 
      if (newHomeNumber > 1) {
        name += " " + newHomeNumber;
      }
    } else {
      name = new File(name).getName();
    }
    
    String title = name;
    if (!System.getProperty("os.name").startsWith("Mac OS X")) {
      title += " - Sweet Home 3D";
    }
    if (home.isModified()) {
      title = "* " + title;
    }
    shell.setText(title);
  }
}
