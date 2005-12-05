/*
 * SwtTime.java 12 nov. 2005
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Simple SWT application that allows the user to see the current time after
 * clicking on a button.
 * @author Emmanuel Puybaret
 */
public class SwtTime {
  /**
   * Displays a SWT Shell with a button and a label.
   * @param args no argument required.
   */
  public static void main(String [] args) {
    Display display = new Display();
    // Create a shell that displays components verticaly
    Shell shell = new Shell(display);
    shell.setText("Time");
    shell.setLayout(new FillLayout(SWT.VERTICAL));
    // Create a label on top of the shell
    final Label timeLabel = new Label(shell, SWT.CENTER);
    // Create bellow a button that updates timeLabel 
    Button timeButton = new Button(shell, SWT.PUSH);
    timeButton.setText("Display time");
    timeButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent ev) {
        // Modify the label with the current time
        timeLabel.setText(String.format("%tT", new Date()));
      }
    });
    // Pack and display the shell
    shell.pack();
    shell.open();
    // Event loop
    while (!shell.isDisposed()) {
      if (display.readAndDispatch()) {
        display.sleep(); 
      }        
    }
    display.dispose();
  }
}
