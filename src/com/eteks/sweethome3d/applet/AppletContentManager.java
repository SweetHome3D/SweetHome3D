/*
 * AppletContentManager.java 13 Oct. 2008
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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.Callable;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.eteks.sweethome3d.model.HomeRecorder;
import com.eteks.sweethome3d.model.InterruptedRecorderException;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.swing.FileContentManager;
import com.eteks.sweethome3d.swing.SwingViewFactory;
import com.eteks.sweethome3d.viewcontroller.ThreadedTaskController;
import com.eteks.sweethome3d.viewcontroller.View;
import com.eteks.sweethome3d.viewcontroller.ViewFactory;

/**
 * Content manager for Sweet Home 3D files stored on server.
 * @author Emmanuel Puybaret
 */
public class AppletContentManager extends FileContentManager {
  private final HomeRecorder    recorder;
  private final UserPreferences preferences;
  private final ViewFactory     viewFactory;

  public AppletContentManager(HomeRecorder recorder, 
                              UserPreferences preferences,
                              ViewFactory viewFactory) {
    super(preferences);
    this.recorder = recorder;
    this.preferences = preferences;
    this.viewFactory = viewFactory;  
  }
  
  public AppletContentManager(HomeRecorder recorder, 
                              UserPreferences preferences) {
    this(recorder, preferences, new SwingViewFactory());  
  }
  
  /**
   * Returns the name of the content in parameter.
   */
  @Override
  public String getPresentationName(String contentName, 
                                    ContentType contentType) {
    if (contentType == ContentType.SWEET_HOME_3D) {
      return contentName;
    } else {
      return super.getPresentationName(contentName, contentType);
    }    
  }
  
  /**
   * Returns <code>true</code> if the content name in parameter is accepted
   * for <code>contentType</code>.
   */
  @Override
  public boolean isAcceptable(String contentName, 
                              ContentType contentType) {
    if (contentType == ContentType.SWEET_HOME_3D) {
      return true;
    } else {
      return contentType != ContentType.PLUGIN 
          && super.isAcceptable(contentName, contentType);
    }    
  }
  
  /**
   * Returns the name chosen by user with an open dialog.
   * @return the name or <code>null</code> if user canceled its choice.
   */
  @Override
  public String showOpenDialog(final View  parentView,
                               String      dialogTitle,
                               ContentType contentType) {
    if (contentType == ContentType.SWEET_HOME_3D) {
      String [] availableHomes = null;
      if (this.recorder instanceof HomeAppletRecorder) {
        try {
          availableHomes = ((HomeAppletRecorder)this.recorder).getAvailableHomes();
        } catch (RecorderException ex) {
          String errorMessage = this.preferences.getLocalizedString(
              AppletContentManager.class, "showOpenDialog.availableHomesError");
          showError(parentView, errorMessage);
          return null;
        }
      }    
      
      String fileDialogTitle = getFileDialogTitle(false);
      JRootPane parent = SwingUtilities.getRootPane((JComponent)parentView);
      if (availableHomes != null && availableHomes.length == 0) {
        String message = this.preferences.getLocalizedString(
            AppletContentManager.class, "showOpenDialog.noAvailableHomes");
        JOptionPane.showMessageDialog(parent, 
            message, fileDialogTitle, JOptionPane.INFORMATION_MESSAGE);
        return null;
      } else {
        String message = this.preferences.getLocalizedString(
            AppletContentManager.class, "showOpenDialog.message");
        DefaultListModel listModel = new DefaultListModel();
        for (String home : availableHomes) {
          listModel.addElement(home);
        }
        final JList availableHomesList = new JList(listModel);
        availableHomesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        availableHomesList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent ev) {
              // Close the option pane when the user double clicks in the list
              if (ev.getClickCount() == 2 && availableHomesList.getSelectedValue() != null) {                
                ((JOptionPane)SwingUtilities.getAncestorOfClass(JOptionPane.class, availableHomesList)).
                    setValue(JOptionPane.OK_OPTION);
              }
            }
          });
        JPanel panel = new JPanel(new BorderLayout(3, 3));
        panel.add(new JLabel(message), BorderLayout.NORTH);
        panel.add(new JScrollPane(availableHomesList), BorderLayout.CENTER);
        
        Object answer;
        if (this.recorder instanceof HomeAppletRecorder
            && ((HomeAppletRecorder)this.recorder).isHomeDeletionAvailable()) {
          // Show a dialog that lets the user open and delete listed homes 
          String delete = this.preferences.getLocalizedString(AppletContentManager.class, "showOpenDialog.delete");
          String open = this.preferences.getLocalizedString(AppletContentManager.class, "showOpenDialog.open");
          String cancel = this.preferences.getLocalizedString(AppletContentManager.class, "showOpenDialog.cancel");
          final JButton deleteButton = new JButton(new AbstractAction(delete) {
              public void actionPerformed(ActionEvent ev) {
                deleteSelectedHome(parentView, availableHomesList);
              }
            });
          deleteButton.setEnabled(false);
          final JButton openButton = new JButton(new AbstractAction(open) {
              public void actionPerformed(ActionEvent ev) {
                JComponent button = (JComponent)ev.getSource();
                ((JOptionPane)SwingUtilities.getAncestorOfClass(JOptionPane.class, button)).setValue(JOptionPane.OK_OPTION);
              }
            });
          openButton.setEnabled(false);
          availableHomesList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
              public void valueChanged(ListSelectionEvent ev) {
                boolean selectionEmpty = availableHomesList.getSelectedValue() == null;
                deleteButton.setEnabled(!selectionEmpty);
                openButton.setEnabled(!selectionEmpty);
              }
            });
          JOptionPane optionPane = new JOptionPane(panel, 
              JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, 
              null, new Object [] {openButton, deleteButton, cancel}, cancel);
          optionPane.createDialog(parent, fileDialogTitle).setVisible(true);
          answer = optionPane.getValue();
        } else {    
          answer = JOptionPane.showConfirmDialog(parent, panel, fileDialogTitle, 
              JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        }
        if (answer.equals(JOptionPane.OK_OPTION)) {
          Object selectedValue = availableHomesList.getSelectedValue();
          if (selectedValue != null) {
            return (String)selectedValue;
          } 
        }
        return null;
      }
    } else {
      return super.showOpenDialog(parentView, dialogTitle, contentType);
    }
  }
  
  /**
   * Attempts to delete the home selected in the given list on the server.
   */
  private void deleteSelectedHome(final View parentView, final JList homesList) {
    final String homeName = (String)homesList.getSelectedValue();
    if (homeName != null) {
      String message = this.preferences.getLocalizedString(AppletContentManager.class, "confirmDeleteHome.message", homeName);
      String title = this.preferences.getLocalizedString(AppletContentManager.class, "confirmDeleteHome.title");
      String delete = this.preferences.getLocalizedString(AppletContentManager.class, "confirmDeleteHome.delete");
      String cancel = this.preferences.getLocalizedString(AppletContentManager.class, "confirmDeleteHome.cancel");
      
      if (JOptionPane.showOptionDialog((JComponent)parentView, message, title, 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
            null, new Object [] {delete, cancel}, cancel) == JOptionPane.OK_OPTION) {
        // Delete home in a threaded task
        Callable<Void> exportToObjTask = new Callable<Void>() {
            public Void call() throws RecorderException {
              ((HomeAppletRecorder)recorder).deleteHome(homeName);
              ((DefaultListModel)homesList.getModel()).removeElement(homeName);
              return null;
            }
          };
        ThreadedTaskController.ExceptionHandler exceptionHandler = 
            new ThreadedTaskController.ExceptionHandler() {
              public void handleException(Exception ex) {
                if (!(ex instanceof InterruptedRecorderException)) {
                  if (ex instanceof RecorderException) {
                    String errorMessage = preferences.getLocalizedString(AppletContentManager.class, "confirmDeleteHome.errorMessage", homeName);
                    String errorTitle = preferences.getLocalizedString(AppletContentManager.class, "showError.title");
                    JOptionPane.showMessageDialog((JComponent)parentView, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);
                  } else {
                    ex.printStackTrace();
                  }
                }
              }
            };
        new ThreadedTaskController(exportToObjTask, 
            this.preferences.getLocalizedString(AppletContentManager.class, "deleteHomeMessage"), exceptionHandler, 
            this.preferences, this.viewFactory).executeTask(parentView);
      }
    }
  }

  /**
   * Returns the name chosen by user with a save dialog.
   * If this name already exists, the user will be prompted whether 
   * he wants to overwrite this existing name. 
   * @return the chosen name or <code>null</code> if user canceled its choice.
   */
  @Override
  public String showSaveDialog(View        parentView,
                               String      dialogTitle,
                               ContentType contentType,
                               String      name) {
    if (contentType == ContentType.SWEET_HOME_3D) {
      String message = this.preferences.getLocalizedString(
          AppletContentManager.class, "showSaveDialog.message");
      String savedName = (String)JOptionPane.showInputDialog(SwingUtilities.getRootPane((JComponent)parentView), 
          message, getFileDialogTitle(true), JOptionPane.QUESTION_MESSAGE, null, null, name);
      if (savedName == null) {
        return null;
      }
      savedName = savedName.trim();
  
      try {
        // If the name exists, prompt user if he wants to overwrite it
        if (this.recorder.exists(savedName)
            && !confirmOverwrite(parentView, savedName)) {
          return showSaveDialog(parentView, dialogTitle, contentType, savedName);
        // If name is empty, prompt user again
        } else if (savedName.length() == 0) {
          return showSaveDialog(parentView, dialogTitle, contentType, savedName);
        }
        return savedName;
      } catch (RecorderException ex) {
        String errorMessage = this.preferences.getLocalizedString(
            AppletContentManager.class, "showSaveDialog.checkHomeError");
        showError(parentView, errorMessage);
        return null;
      }
    } else {
      return super.showSaveDialog(parentView, dialogTitle, contentType, name);
    }
  }
  
  /**
   * Shows the given <code>message</code> in an error message dialog. 
   */
  private void showError(View parentView, String message) {
    String title = this.preferences.getLocalizedString(
        AppletContentManager.class, "showError.title");
    JOptionPane.showMessageDialog(SwingUtilities.getRootPane((JComponent)parentView), 
        message, title, JOptionPane.ERROR_MESSAGE);    
  }
}
