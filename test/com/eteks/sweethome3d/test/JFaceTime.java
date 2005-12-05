/*
 * JFaceTime.java 12 nov. 2005
 *
 * Copyright (c) 2005 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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
package com.eteks.sweethome3d.test;

import java.util.Date;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Simple JFace application that allows the user to see the current time after
 * clicking on a button.
 * @author Emmanuel Puybaret
 */
public class JFaceTime extends ApplicationWindow {
  /**
   * Creates a window.
   * @param parentShell the parent shell of thiw window. May be null. 
   */
  public JFaceTime(Shell parentShell) {
    super(parentShell);
  }
  
  /**
   * Set the title and the layout of this window's <code>shell</code>.
   */
  protected void configureShell(Shell shell) {
    shell.setText("Time");
    shell.setLayout(new FillLayout(SWT.VERTICAL));
  }

  /**
   * Creates the label and the button displayed by the window.
   */
  protected Control createContents(Composite parent) {
    // Create a label on top of parent
    final Label timeLabel = new Label(parent, SWT.CENTER);
    // Create bellow a button that updates timeLabel 
    Button timeButton = new Button(parent, SWT.PUSH);
    timeButton.setText("Display time");
    timeButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent ev) {
        // Modify the label with the current time
        timeLabel.setText(String.format("%tT", new Date()));
      }
    });
    return parent;
  }

  /**
   * Displays a JFace application window with a button and a label.
   * @param args no argument required.
   */
  public static void main(String [] args) {
    // Create an application window 
    JFaceTime window = new JFaceTime(null);
    // Run the event loop until window is disposed once window is opened
    window.setBlockOnOpen(true);
    // Pack and display the shell
    window.open();
    Display.getCurrent().dispose();
  }
}
