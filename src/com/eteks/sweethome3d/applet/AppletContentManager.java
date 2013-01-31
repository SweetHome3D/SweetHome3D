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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import com.eteks.sweethome3d.model.HomeRecorder;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.swing.FileContentManager;
import com.eteks.sweethome3d.viewcontroller.View;

/**
 * Content manager for Sweet Home 3D files stored on server.
 * @author Emmanuel Puybaret
 */
public class AppletContentManager extends FileContentManager {
  private final HomeRecorder recorder;
  private final UserPreferences preferences;

  public AppletContentManager(HomeRecorder recorder, UserPreferences preferences) {
    super(preferences);
    this.recorder = recorder;
    this.preferences = preferences;  
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
  public String showOpenDialog(View        parentView,
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
      
      if (availableHomes != null && availableHomes.length == 0) {
        String message = this.preferences.getLocalizedString(
            AppletContentManager.class, "showOpenDialog.noAvailableHomes");
        JOptionPane.showMessageDialog(SwingUtilities.getRootPane((JComponent)parentView), 
            message, getFileDialogTitle(false), JOptionPane.INFORMATION_MESSAGE);
        return null;
      } else {
        String message = this.preferences.getLocalizedString(
            AppletContentManager.class, "showOpenDialog.message");
        final JList availableHomesList = new JList(availableHomes);
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
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(message), BorderLayout.NORTH);
        panel.add(new JScrollPane(availableHomesList), BorderLayout.CENTER);
        if (JOptionPane.showConfirmDialog(SwingUtilities.getRootPane((JComponent)parentView), panel, 
              getFileDialogTitle(false), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION) {
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
