/*
 * ChoicePanel.java 17 sept. 2006
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * A panel that displays a list into which the user can pick
 * values that are displayed in second list.
 * @author Emmanuel Puybaret
 */
public class ChoicePanel extends JPanel {
  private DefaultListModel choiceModel;

  public ChoicePanel(Object [] values) {
    super(new GridBagLayout());
    // Create a list model with all the values given in parameter
    DefaultListModel valuesModel = new DefaultListModel();
    for (Object value : values) {
      valuesModel.addElement(value);
    }
    // Use this model in a list displayed in a scroll pane
    JList valuesList = new JList(valuesModel);
    JScrollPane valuesScrollPane = new JScrollPane(valuesList);
    
    // Create an empty list model that stores chosen values 
    this.choiceModel = new DefaultListModel();
    // User this second model in a list
    JList choiceList = new JList(this.choiceModel);
    JScrollPane choiceScrollPane = new JScrollPane(choiceList);
    
    // Creation two buttons which action takes selected values
    // in a list to add them to an other list 
    JButton addToChoiceButton = new JButton(
        new MoveAction(">", valuesList, choiceList));
    JButton removeFromChoiceButton = new JButton(
        new MoveAction("<", choiceList, valuesList));
    
    // Layout labels, lists and buttons in panel 
    Insets labelInsets = new Insets(0, 0, 2, 0);
    add(new JLabel("Values :"), new GridBagConstraints(
        0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    add(new JLabel("Choice :"), new GridBagConstraints(
        2, 0, 1, 1, 0, 0, GridBagConstraints.WEST, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    Insets emptyInsets = new Insets(0, 0, 0, 0);
    add(valuesScrollPane, new GridBagConstraints(
        0, 1, 1, 2, 1, 1, GridBagConstraints.CENTER, 
        GridBagConstraints.BOTH, emptyInsets, 0, 0));
    add(choiceScrollPane, new GridBagConstraints(
        2, 1, 1, 2, 1, 1, GridBagConstraints.CENTER, 
        GridBagConstraints.BOTH, emptyInsets, 0, 0));
    Insets buttonInsets = new Insets(5, 5, 5, 5);
    add(addToChoiceButton, new GridBagConstraints(
        1, 1, 1, 1, 0, 0.5, GridBagConstraints.SOUTH, 
        GridBagConstraints.HORIZONTAL, buttonInsets, 0, 0));
    add(removeFromChoiceButton, new GridBagConstraints(
        1, 2, 1, 1, 0, 0.5, GridBagConstraints.NORTH, 
        GridBagConstraints.HORIZONTAL, buttonInsets, 0, 0));
    
    // Change preferred size of the second list 
    // so they are laid out with the same size 
    choiceScrollPane.setPreferredSize(
        valuesScrollPane.getPreferredSize());
  }

  /**
   * Returns the chosen values.
   */
  public Object [] getChoice() {
    return this.choiceModel.toArray();
  }

  private static class MoveAction extends AbstractAction {
    private JList sourceList;
    private JList destinationList;
    
    MoveAction(String name, JList sourceList, 
        JList destinationList) {
      super(name);
      this.sourceList = sourceList;
      this.destinationList = destinationList;
    }
    
    public void actionPerformed(ActionEvent ev) {
      DefaultListModel sourceModel = 
        (DefaultListModel)sourceList.getModel();
      DefaultListModel destinationModel = 
        (DefaultListModel)destinationList.getModel();
      this.destinationList.clearSelection();
      for (Object value : sourceList.getSelectedValues()) {
        sourceModel.removeElement(value);
        destinationModel.addElement(value);
        int index = destinationModel.indexOf(value);
        this.destinationList.addSelectionInterval(index, index);
      }
    }
  }
}
