/*
 * ThreadedTaskView.java 29 sept. 2008
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
import java.util.ResourceBundle;

import javax.swing.FocusManager;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 * A MVC view of a threaded task.
 * @author Emmanuel Puybaret
 */
public class ThreadedTaskPanel extends JPanel {
  private ThreadedTaskController controller;
  private JLabel                 taskLabel;
  private JProgressBar           taskProgressBar;
  private JDialog                dialog;
  private boolean                taskRunning;

  public ThreadedTaskPanel(String taskMessage, 
                          ThreadedTaskController controller) {
    super(new BorderLayout(5, 5));
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
   * Executes <code>runnable</code> asynchronously in the Event Dispatch Thread.
   * Caution !!! This method may be called from an other thread than EDT.  
   */
  public void invokeLater(Runnable runnable) {
    EventQueue.invokeLater(runnable);
  }

  /**
   * Sets the running status of the threaded task. 
   */
  public void setTaskRunning(boolean taskRunning) {
    this.taskRunning = taskRunning;
    if (taskRunning && this.dialog == null) {
      ResourceBundle resource = ResourceBundle.getBundle(ThreadedTaskPanel.class.getName());
      String dialogTitle = resource.getString("threadedTask.title");
      final JButton cancelButton = new JButton(resource.getString("cancelButton.text"));
      
      final JOptionPane optionPane = new JOptionPane(this, JOptionPane.PLAIN_MESSAGE, 
          JOptionPane.DEFAULT_OPTION, null, new Object [] {cancelButton});
      cancelButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ev) {
            optionPane.setValue(cancelButton);
          }
        });
      this.dialog = optionPane.createDialog(FocusManager.getCurrentManager().getActiveWindow(), dialogTitle);
      
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
