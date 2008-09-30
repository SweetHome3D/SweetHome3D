/*
 * ThreadedTaskControllerTest.java 30 sept 2008
 * 
 * Copyright (c) 2008 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.eteks.sweethome3d.junit;

import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.swing.JButton;

import junit.framework.TestCase;
import abbot.finder.ComponentSearchException;

import com.eteks.sweethome3d.swing.ThreadedTaskController;

/**
 * Tests threaded task controller features.
 * @author Emmanuel Puybaret
 */
public class ThreadedTaskControllerTest extends TestCase {
  public void testThreadedTaskController() throws TimeoutException, InterruptedException {
    // 1. Create a very simple short task that simply counts down latch
    final CountDownLatch shortTaskLatch = new CountDownLatch(1);
    Callable<Void> shortTask = new Callable<Void>() {
        public Void call() throws Exception {
          shortTaskLatch.countDown();
          return null;
        }
      };
    // Create an exception handler that fails test if it was called
    ThreadedTaskController.ExceptionHandler noExceptionHandler = 
        new ThreadedTaskController.ExceptionHandler() {
          public void handleException(Exception ex) {
            fail("Exception handler shouldn't be called");
          }
        };
    // Add a listener that fails test if a window is displayed
    PropertyChangeListener activeWindowListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            fail("No window should be displayed for short task");
          }
        };
    KeyboardFocusManager.getCurrentKeyboardFocusManager().
        addPropertyChangeListener("activeWindow", activeWindowListener);
    // Check that a simple short task is correctly executed with no exception 
    // and doesn't create any visible dialog at screen
    new ThreadedTaskController(shortTask, "Message", noExceptionHandler);
    shortTaskLatch.await(1000, TimeUnit.MILLISECONDS);
    assertEquals("Simple task wasn't executed", 0, shortTaskLatch.getCount());
    KeyboardFocusManager.getCurrentKeyboardFocusManager().
        removePropertyChangeListener("activeWindow", activeWindowListener);
    
    // 2. Create a longer task 
    final CountDownLatch longTaskLatch = new CountDownLatch(2);
    Callable<Void> longTask = new Callable<Void>() {
        public Void call() throws Exception {
          Thread.sleep(1000);
          longTaskLatch.countDown();
          return null;
        }
      };
    // Add a listener that counts down latch once a waiting dialog is displayed
    activeWindowListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            longTaskLatch.countDown();
          }
        };
    KeyboardFocusManager.getCurrentKeyboardFocusManager().
        addPropertyChangeListener("activeWindow", activeWindowListener);
    // Check that a long task creates a visible dialog at screen
    new ThreadedTaskController(longTask, "Message", noExceptionHandler);
    longTaskLatch.await(1500, TimeUnit.MILLISECONDS);
    assertEquals("Long task wasn't executed with a waiting dialog", 0, longTaskLatch.getCount());
    KeyboardFocusManager.getCurrentKeyboardFocusManager().
        removePropertyChangeListener("activeWindow", activeWindowListener);
    
    // 3. Create a long task that we will cancel  
    final CountDownLatch cancelledTaskLatch = new CountDownLatch(1);
    Callable<Void> cancelledTask = new Callable<Void>() {
        public Void call() throws Exception {
          try {
            Thread.sleep(1000);
          } catch (InterruptedException ex) {
            cancelledTaskLatch.countDown();
          }
          return null;
        }
      };
    // Add a listener that closes the waiting dialog
    activeWindowListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            final Window activeWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
            if (activeWindow != null) {
              try {
                ((JButton)TestUtilities.findComponent(activeWindow, JButton.class)).doClick();
              } catch (ComponentSearchException ex) {
                fail("No button in waiting dialog");
              }
            }
          }
        };
    KeyboardFocusManager.getCurrentKeyboardFocusManager().
        addPropertyChangeListener("activeWindow", activeWindowListener);
    // Check that a long task creates a visible dialog at screen
    new ThreadedTaskController(cancelledTask, "Message", noExceptionHandler);
    cancelledTaskLatch.await(1500, TimeUnit.MILLISECONDS);
    assertEquals("Task wasn't cancelled", 0, cancelledTaskLatch.getCount());
    KeyboardFocusManager.getCurrentKeyboardFocusManager().
        removePropertyChangeListener("activeWindow", activeWindowListener);

    // 4. Create a task that fails  
    final CountDownLatch failingTaskLatch = new CountDownLatch(2);
    Callable<Void> failingTask = new Callable<Void>() {
        public Void call() throws Exception {
          failingTaskLatch.countDown();
          throw new Exception();
        }
      };
    // Create an exception handler that counts down latch when it's called
    ThreadedTaskController.ExceptionHandler exceptionHandler = 
        new ThreadedTaskController.ExceptionHandler() {
          public void handleException(Exception ex) {
            failingTaskLatch.countDown();
          }
        };
    // Check that a long task creates a visible dialog at screen
    new ThreadedTaskController(failingTask, "Message", exceptionHandler);
    failingTaskLatch.await(1000, TimeUnit.MILLISECONDS);
    assertEquals("Exception in task wasn't handled", 0, failingTaskLatch.getCount());
  }
}
