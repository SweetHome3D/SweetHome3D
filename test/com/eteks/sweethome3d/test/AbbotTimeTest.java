/*
 * AbbotTimeTest.java 3 juin 2006
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
package com.eteks.sweethome3d.test;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import junit.extensions.abbot.ComponentTestFixture;
import abbot.tester.AbstractButtonTester;

/**
 * Simple test on Abbot framework.
 * @author Emmanuel Puybaret
 */
public class AbbotTimeTest extends ComponentTestFixture {
  public void testTimeChange()  {
    // Create a label that displays time
    final JLabel timeLabel = new JLabel("", JLabel.CENTER);
    // Create a button that updates timeLabel 
    JButton timeButton = new JButton("Display time");
    timeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        // Modify the label with the current time
        timeLabel.setText(String.format("%tT", new Date()));
      }
    });
    JFrame frame = new JFrame("Time");
    // Layout label and button
    frame.setLayout(new GridLayout(2, 1));
    frame.add(timeLabel);
    frame.add(timeButton);
    
    // Show window with Abbot
    showWindow(frame, null, true);
    assertTrue(timeLabel.getText().equals(""));
    // Click on button
    new AbstractButtonTester().actionClick(timeButton);
    // Check label changed
    assertFalse(timeLabel.getText().equals(""));
  }
}
