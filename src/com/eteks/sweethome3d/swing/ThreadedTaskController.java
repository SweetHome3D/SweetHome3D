/*
 * ThreadedTaskController.java 29 sept. 2008
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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import javax.swing.JComponent;

/**
 * A MVC controller used to execute a particular task in a separated thread.
 * @author Emmanuel Puybaret
 */
public class ThreadedTaskController {
  private Callable<Void> threadedTask;
  private ExceptionHandler exceptionHandler;
  private ExecutorService threadExecutor;
  private JComponent view;
  private Future<?> task;

  /**
   * Creates a controller that will execute in a separated thread the given task. 
   * This task shouldn't modify any model objects and should be able to handle
   * interruptions. 
   */
  public ThreadedTaskController(Callable<Void> threadedTask,
                                String taskMessage, 
                                ExceptionHandler exceptionHandler) {
    this.threadedTask = threadedTask;
    this.exceptionHandler = exceptionHandler;
    this.threadExecutor = Executors.newSingleThreadExecutor();
    this.view = new ThreadedTaskPanel(taskMessage, this);
    executeTask();
  }
  
  /**
   * Returns the view controlled by this controller.
   */
  public JComponent getView() {
    return this.view;
  }

  /**
   * Executes in a separated thread the task given in constructor. This task shouldn't
   * modify any model objects. 
   */
  private void executeTask() {
    this.task = this.threadExecutor.submit(new FutureTask<Void>(threadedTask) {
        @Override
        public void run() {
          // Update running status in view
          ((ThreadedTaskPanel)getView()).invokeLater(new Runnable() {
              public void run() {
                ((ThreadedTaskPanel)getView()).setTaskRunning(true);
              }
            });
          super.run();
        }
      
        @Override
        protected void done() {
          // Update running status in view
          ((ThreadedTaskPanel)getView()).invokeLater(new Runnable() {
              public void run() {
                ((ThreadedTaskPanel)getView()).setTaskRunning(false);
                task = null;
              }
            });
          
          try {
            get();
          } catch (ExecutionException ex) {
            // Handle exceptions with handler            
            final Throwable throwable = ex.getCause();
            if (throwable instanceof Exception) {
              ((ThreadedTaskPanel)getView()).invokeLater(new Runnable() {
                  public void run() {
                    exceptionHandler.handleException((Exception)throwable);
                  }
                });
            } else {
              throwable.printStackTrace();
            }
          } catch (final InterruptedException ex) {
            // Handle exception with handler            
            ((ThreadedTaskPanel)getView()).invokeLater(new Runnable() {
                public void run() {
                  exceptionHandler.handleException(ex);
                }
              });
          }
        }
      });
  }
  
  /**
   * Cancels the threaded task if it's running.
   */
  public void cancelTask() {
    if (this.task != null) {
      this.task.cancel(true);
    }
  }
  
  /**
   * Returns <code>true</code> if the threaded task is running.
   */
  public boolean isTaskRunning() {
    return this.task != null && !this.task.isDone();
  }

  /**
   * Handles exception that may happen during the execution of a threaded task.
   */
  public static interface ExceptionHandler {
    public void handleException(Exception ex);
  }
}
