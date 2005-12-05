/*
 * SwingTime.java 10 nov. 2005
 * 
 * Copyright (c) 2005 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights
 * Reserved.
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
package com.eteks.sweethome3d.test;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.UIManager;

/**
 * Simple Swing application that allows the user to see the current time after
 * clicking on a button.
 * @author Emmanuel Puybaret
 */
public class SwingTime {
  /**
   * Displays a Swing Frame with a button and a label.
   * @param args no argument required.
   */
  public static void main(String [] args) {    
    try {
      // Apply current system look and feel
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) { 
    }
    // Create a frame that displays the two components with a GridLayout
    JFrame frame = new JFrame("Time");
    frame.setLayout(new GridLayout(2, 1));
    // Create a label on top of the frame
    final JLabel timeLabel = new JLabel("", JLabel.CENTER);
    frame.add(timeLabel);
    // Create bellow a button that updates timeLabel 
    JButton timeButton = new JButton("Display time");
    frame.add(timeButton);
    timeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        // Modify the label with the current time
        timeLabel.setText(String.format("%tT", new Date()));
      }
    });
    frame.pack();
    frame.setVisible(true);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }
}
