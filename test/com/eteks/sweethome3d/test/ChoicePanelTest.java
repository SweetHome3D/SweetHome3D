/*
 * ChoicePanelTest.java 17 sept. 2006
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

import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 * Displays a {@link com.eteks.sweethome3d.test.ChoicePanel choice panel}
 * in a resizable dialog.
 * @author Emmanuel Puybaret
 */
public class ChoicePanelTest {
  public static void main(String [] args) {
    ChoicePanel choicePanel = new ChoicePanel(
        new String [] {"Paris", "London", "Berlin", "Rome", 
            "Madrid", "Brussels", "New York", "Ottawa", "Tokyo"});
    JOptionPane choiceOptionPane =  new JOptionPane(choicePanel, 
        JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
    JDialog choiceDialog = choiceOptionPane.createDialog(
        JOptionPane.getRootFrame(), "Choose your cities");
    choiceDialog.setResizable(true);
    // No effect under Java 5 but works with Java 6
    choiceDialog.setMinimumSize(choiceDialog.getSize());
    choiceDialog.setVisible(true);
    // Dispose dialog otherwise it's only hidden 
    // and application won't stop at the end of main
    choiceDialog.dispose();
    if (new Integer(JOptionPane.OK_OPTION).equals(
            choiceOptionPane.getValue())) {
      JOptionPane.showMessageDialog(null, 
          new Object [] {"You chose :", choicePanel.getChoice()});
    }
  }
}
