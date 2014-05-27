/*
 * HomeAppletController.java 11 Oct. 2008
 *
 * Sweet Home 3D, Copyright (c) 2008 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;

import com.eteks.sweethome3d.io.HomeFileRecorder;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeApplication;
import com.eteks.sweethome3d.model.HomeRecorder;
import com.eteks.sweethome3d.model.InterruptedRecorderException;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.plugin.HomePluginController;
import com.eteks.sweethome3d.plugin.PluginManager;
import com.eteks.sweethome3d.swing.FileContentManager;
import com.eteks.sweethome3d.swing.SwingTools;
import com.eteks.sweethome3d.viewcontroller.ContentManager;
import com.eteks.sweethome3d.viewcontroller.HomeController;
import com.eteks.sweethome3d.viewcontroller.HomeView;
import com.eteks.sweethome3d.viewcontroller.ThreadedTaskController;
import com.eteks.sweethome3d.viewcontroller.UserPreferencesController;
import com.eteks.sweethome3d.viewcontroller.ViewFactory;

/**
 * Home applet pane controller.
 * @author Emmanuel Puybaret
 */
public class HomeAppletController extends HomePluginController {
  private final Home               home;
  private final HomeApplication    application;
  private final ViewFactory        viewFactory;
  private final ContentManager     contentManager;
  private final long               homeMaximumLength;
  
  private static Map<Home, String> importedHomeNames = new WeakHashMap<Home, String>();

  public HomeAppletController(Home home, 
                              HomeApplication application, 
                              ViewFactory     viewFactory,
                              ContentManager  contentManager,
                              PluginManager   pluginManager,
                              boolean newHomeEnabled, 
                              boolean openEnabled, 
                              boolean saveEnabled, 
                              boolean saveAsEnabled) {
    this(home, application, viewFactory, contentManager, pluginManager, newHomeEnabled, openEnabled, saveEnabled, saveAsEnabled, -1);
  }
  
  public HomeAppletController(Home home, 
                              HomeApplication application, 
                              ViewFactory     viewFactory,
                              ContentManager  contentManager,
                              PluginManager   pluginManager,
                              boolean newHomeEnabled, 
                              boolean openEnabled, 
                              boolean saveEnabled, 
                              boolean saveAsEnabled,
                              long    homeMaximumLength) {
    super(home, application, viewFactory, contentManager, pluginManager);
    this.home = home;
    this.application = application;
    this.viewFactory = viewFactory;
    this.contentManager = contentManager;
    this.homeMaximumLength = homeMaximumLength;

    
    HomeView view = (HomeView)getView();
    view.setEnabled(HomeView.ActionType.EXIT, false);
    view.setEnabled(HomeView.ActionType.NEW_HOME, newHomeEnabled);
    view.setEnabled(HomeView.ActionType.OPEN, openEnabled);
    view.setEnabled(HomeView.ActionType.SAVE, saveEnabled);
    view.setEnabled(HomeView.ActionType.SAVE_AS, saveAsEnabled);
    
    // By default disabled Print to PDF, Export to SVG, Export to OBJ and Create photo actions 
    view.setEnabled(HomeView.ActionType.PRINT_TO_PDF, false);
    view.setEnabled(HomeView.ActionType.EXPORT_TO_SVG, false);
    view.setEnabled(HomeView.ActionType.EXPORT_TO_OBJ, false);
    view.setEnabled(HomeView.ActionType.CREATE_PHOTO, false);
    
    view.setEnabled(HomeView.ActionType.DETACH_3D_VIEW, false);
  }
  
  /**
   * Creates a new home after saving and deleting the current home.
   */
  @Override
  public void newHome() {
    close(new Runnable() {
        public void run() {
          HomeAppletController.super.newHome();
        }
      });
  }

  /**
   * Opens a home after saving and deleting the current home.
   */
  @Override
  public void open() {
    close(new Runnable() {
      public void run() {
        HomeAppletController.super.open();
      }
    });
  }
  
  /**
   * Forces a save as operation for imported homes.
   */
  @Override
  public void save() {
    if (this.home.getName() != null) {
      chekHomeLengthAndSave(new Runnable() {
          public void run() {
            HomeAppletController.super.save();
          }
        });
    } else {
      super.saveAs();
    }
  }
  
  /**
   * Prompts the user to choose a name for the edited home, 
   * suggesting the imported file name after an import.
   */
  @Override
  protected void saveAs(final HomeRecorder.Type recorderType, 
                        final Runnable postSaveTask) {
    chekHomeLengthAndSave(new Runnable() {
        public void run() {
          String homeName = importedHomeNames.get(home);
          if (homeName != null) {
            // Suggest imported home name
            home.setName(homeName);
            HomeAppletController.super.saveAs(recorderType, new Runnable () {
                public void run() {
                  if (postSaveTask != null) {
                    postSaveTask.run();
                  }
                  importedHomeNames.remove(home);
                }
              });
            // Reset name to null in case the user gave up
            home.setName(null);
          } else {
            HomeAppletController.super.saveAs(recorderType, postSaveTask);
          }
        }
      });
  }
  
  /**
   * Checks the length of data and executes <code>saveTask</code> if length is ok. 
   */
  public void chekHomeLengthAndSave(final Runnable saveTask) {
    if (this.homeMaximumLength > 0) {
      // Check home length in a threaded task
      Callable<Void> exportToObjTask = new Callable<Void>() {
          public Void call() throws RecorderException {
            final long homeLength = ((HomeAppletRecorder)application.getHomeRecorder()).getHomeLength(home);
            getView().invokeLater(new Runnable() {
                public void run() {
                  if (homeLength > homeMaximumLength) {
                    String message = getHomeLengthMessage(homeLength);
                    getView().showError(message);
                  } else {
                    saveTask.run();
                  }
                }
              });
  
            return null;
          }
        };
      ThreadedTaskController.ExceptionHandler exceptionHandler = 
          new ThreadedTaskController.ExceptionHandler() {
            public void handleException(Exception ex) {
              if (!(ex instanceof InterruptedRecorderException)) {
                if (ex instanceof RecorderException) {
                  String message = application.getUserPreferences().getLocalizedString(
                      HomeController.class, "saveError", home.getName());
                  getView().showError(message);
                } else {
                  ex.printStackTrace();
                }
              }
            }
          };
      new ThreadedTaskController(exportToObjTask, 
          this.application.getUserPreferences().getLocalizedString(HomeAppletController.class, "chekHomeLengthMessage"), exceptionHandler, 
          this.application.getUserPreferences(), viewFactory).executeTask(getView());
    } else {
      saveTask.run();
    }
  }

  /**
   * Displays Sweet Home user guide in a navigator window.
   */
  @Override
  public void help() {
    try { 
      String helpIndex = this.application.getUserPreferences().getLocalizedString(HomeAppletController.class, "helpIndex");
      SwingTools.showDocumentInBrowser(new URL(helpIndex)); 
    } catch (MalformedURLException ex) {
      ex.printStackTrace();
    } 
  }

  /**
   * Controls the export of home to a SH3D file.
   */
  public void exportToSH3D() {
    final String sh3dName = new FileContentManager(this.application.getUserPreferences()).showSaveDialog(getView(),
        this.application.getUserPreferences().getLocalizedString(HomeAppletController.class, "exportToSH3DDialog.title"), 
        ContentManager.ContentType.SWEET_HOME_3D, home.getName());    
    if (sh3dName != null) {
      // Export home in a threaded task
      Callable<Void> exportToObjTask = new Callable<Void>() {
          public Void call() throws RecorderException {
            new HomeFileRecorder(9).writeHome(home, sh3dName);
            return null;
          }
        };
      ThreadedTaskController.ExceptionHandler exceptionHandler = 
          new ThreadedTaskController.ExceptionHandler() {
            public void handleException(Exception ex) {
              if (!(ex instanceof InterruptedRecorderException)) {
                if (ex instanceof RecorderException) {
                  String message = application.getUserPreferences().getLocalizedString(
                      HomeAppletController.class, "exportToSH3DError", sh3dName);
                  getView().showError(message);
                } else {
                  ex.printStackTrace();
                }
              }
            }
          };
      new ThreadedTaskController(exportToObjTask, 
          this.application.getUserPreferences().getLocalizedString(HomeAppletController.class, "exportToSH3DMessage"), exceptionHandler, 
          this.application.getUserPreferences(), viewFactory).executeTask(getView());
    }
  }
  
  /**
   * Controls the import of home from a SH3D file.
   */
  public void importFromSH3D() {
    close(new Runnable() {
      public void run() {
        final String sh3dName = new FileContentManager(application.getUserPreferences()).showOpenDialog(getView(),
            application.getUserPreferences().getLocalizedString(HomeAppletController.class, "importFromSH3DDialog.title"), 
            ContentManager.ContentType.SWEET_HOME_3D);    
        if (sh3dName != null) {
          // Export home in a threaded task
          Callable<Void> exportToObjTask = new Callable<Void>() {
              public Void call() throws RecorderException {
                final Home openedHome = new HomeFileRecorder(9, true, application.getUserPreferences()).readHome(sh3dName);
                String name = new File(sh3dName).getName();
                name = name.substring(0, name.lastIndexOf("."));
                importedHomeNames.put(openedHome, name);
                openedHome.setName(null);
                openedHome.setModified(true);
                final long homeLength = homeMaximumLength > 0
                    ? ((HomeAppletRecorder)application.getHomeRecorder()).getHomeLength(openedHome)
                    : -1;
                getView().invokeLater(new Runnable() {
                    public void run() {
                      application.addHome(openedHome);
                      if (homeLength > homeMaximumLength) {
                        String message = getHomeLengthMessage(homeLength);
                        getView().showMessage(message);
                      } 
                    }
                  });
                return null;
              }
            };
          ThreadedTaskController.ExceptionHandler exceptionHandler = 
              new ThreadedTaskController.ExceptionHandler() {
                public void handleException(Exception ex) {
                  if (!(ex instanceof InterruptedRecorderException)) {
                    if (ex instanceof RecorderException) {
                      String message = application.getUserPreferences().getLocalizedString(
                          HomeAppletController.class, "importFromSH3DError", sh3dName);
                      getView().showError(message);
                    } else {
                      ex.printStackTrace();
                    }
                  }
                }
              };
          new ThreadedTaskController(exportToObjTask, 
              application.getUserPreferences().getLocalizedString(HomeAppletController.class, "importFromSH3DMessage"), exceptionHandler, 
              application.getUserPreferences(), viewFactory).executeTask(getView());
        }
      }
    });
  }
  
  /**
   * Returns a message stating that the home length is too large.
   */
  private String getHomeLengthMessage(long homeLength) {
    DecimalFormat decimalFormat = new DecimalFormat("#.#");
    String homeLengthText = decimalFormat.format(Math.max(0.1f, homeLength / 1048576f));
    String homeMaximumLengthText = decimalFormat.format(Math.max(0.1f, this.homeMaximumLength / 1048576f));
    return application.getUserPreferences().getLocalizedString(
        HomeAppletController.class, "homeLengthError", homeLengthText, homeMaximumLengthText);
  }
  
  /**
   * Edits preferences relevant to applet version.
   */
  @Override
  public void editPreferences() {
    new UserPreferencesController(this.application.getUserPreferences(), 
        this.viewFactory, this.contentManager) {
      public boolean isPropertyEditable(UserPreferencesController.Property property) {
        switch (property) {
          case CHECK_UPDATES_ENABLED :
          case AUTO_SAVE_DELAY_FOR_RECOVERY :
          case AUTO_SAVE_FOR_RECOVERY_ENABLED :
            // No check updates and auto recovery with applet
            return false;
          default :
            return super.isPropertyEditable(property);
        }
      }
    }.displayView(getView());
  }
}

