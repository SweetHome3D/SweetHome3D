/*
 * ThreadedTaskPanel.java 29 sept. 2008
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
package com.eteks.sweethome3d.swing;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.viewcontroller.ThreadedTaskController;
import com.eteks.sweethome3d.viewcontroller.ThreadedTaskView;
import com.eteks.sweethome3d.viewcontroller.View;

/**
 * A MVC view of a threaded task.
 * @author Emmanuel Puybaret
 */
public class ThreadedTaskPanel extends JPanel implements ThreadedTaskView {
  private final UserPreferences        preferences;
  private final ThreadedTaskController controller;
  private JLabel                       taskLabel;
  private JProgressBar                 taskProgressBar;
  private JDialog                      dialog;
  private boolean                      taskRunning;

  public ThreadedTaskPanel(String taskMessage, 
                           UserPreferences preferences, 
                           ThreadedTaskController controller) {
    super(new BorderLayout(5, 5));
    this.preferences = preferences;
    this.controller = controller;
    createComponents(taskMessage);
    layoutComponents();
  }

  /**
   * Creates and initializes components.
   */
  private void createComponents(String taskMessage) {
    this.taskLabel = new JLabel(taskMessage);
    this.taskProgressBar = new JProgressBar();
    this.taskProgressBar.setIndeterminate(true);
  }
    
  /**
   * Layouts panel components in panel with their labels. 
   */
  private void layoutComponents() {
    add(this.taskLabel, BorderLayout.NORTH);
    add(this.taskProgressBar, BorderLayout.SOUTH);
  }
  
  /**
   * Sets the status of the progress bar shown by this panel as indeterminate.
   * This method may be called from an other thread than EDT.  
   */
  public void setIndeterminateProgress() {
    if (EventQueue.isDispatchThread()) {
      this.taskProgressBar.setIndeterminate(true);
    } else {
      // Ensure modifications are done in EDT
      invokeLater(new Runnable() {
          public void run() {
            setIndeterminateProgress();
          }
        });
    }
  }
  
  /**
   * Sets the current value of the progress bar shown by this panel.  
   * This method may be called from an other thread than EDT.  
   */
  public void setProgress(final int value, 
                          final int minimum, 
                          final int maximum) {
    if (EventQueue.isDispatchThread()) {
      this.taskProgressBar.setIndeterminate(false);
      this.taskProgressBar.setValue(value);
      this.taskProgressBar.setMinimum(minimum);
      this.taskProgressBar.setMaximum(maximum);
    } else {
      // Ensure modifications are done in EDT
      invokeLater(new Runnable() {
          public void run() {
            setProgress(value, minimum, maximum);
          }
        });
    }
  }
  
  /**
   * Executes <code>runnable</code> asynchronously in the Event Dispatch Thread.
   * Caution !!! This method may be called from an other thread than EDT.  
   */
  public void invokeLater(Runnable runnable) {
    EventQueue.invokeLater(runnable);
  }

  /**
   * Sets the running status of the threaded task. 
   * If <code>taskRunning</code> is <code>true</code>, a waiting dialog will be shown.
   */
  public void setTaskRunning(boolean taskRunning, View executingView) {
    this.taskRunning = taskRunning;
    if (taskRunning && this.dialog == null) {
      String dialogTitle = this.preferences.getLocalizedString(
          ThreadedTaskPanel.class, "threadedTask.title");
      final JButton cancelButton = new JButton(this.preferences.getLocalizedString(
          ThreadedTaskPanel.class, "cancelButton.text"));
      
      final JOptionPane optionPane = new JOptionPane(this, JOptionPane.PLAIN_MESSAGE, 
          JOptionPane.DEFAULT_OPTION, null, new Object [] {cancelButton});
      cancelButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ev) {
            optionPane.setValue(cancelButton);
          }
        });
      this.dialog = optionPane.createDialog(SwingUtilities.getRootPane((JComponent)executingView), dialogTitle);
      
      try {
        // Sleep 200 ms before showing dialog to avoid displaying it 
        // when the task doesn't last so long
        Thread.sleep(200);
      } catch (InterruptedException ex) {
      }
      
      if (this.controller.isTaskRunning()) {
        this.dialog.setVisible(true);
        
        this.dialog.dispose();
        if (this.taskRunning 
            && (cancelButton == optionPane.getValue() 
                || new Integer(JOptionPane.CLOSED_OPTION).equals(optionPane.getValue()))) {
          this.dialog = null;
          this.controller.cancelTask();
        }
      }
    } else if (!taskRunning && this.dialog != null) {
      this.dialog.setVisible(false);
    }
  }
}
