/*
 * HelpController.java 20 juil. 07
 *
 * Copyright (c) 2007 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JComponent;

/**
 * A MVC controller for Sweet Home 3D help view.
 * @author Emmanuel Puybaret
 */
public class HelpController {
  private ResourceBundle resource;
  private List<URL>      history;
  private int            historyIndex;
  private JComponent     helpView;
  
  public HelpController() {
    this.resource = ResourceBundle.getBundle(HelpController.class.getName());
    this.history = new ArrayList<URL>();
    historyIndex = -1;
    this.helpView = new HelpPane(this);
  }

  /**
   * Returns the view associated with this controller.
   */
  public JComponent getView() {
    return this.helpView;
  }

  /**
   * Displays the help view controlled by this controller. 
   */
  public void displayView() {
    showPage(HelpController.class.getResource(this.resource.getString("helpIndex")));
    ((HelpPane)getView()).displayView();
  }
  
  /**
   * Controls the display of previous page.
   */
  public void showPrevious() {
    HelpPane helpView = (HelpPane)getView();
    helpView.setPage(this.history.get(--this.historyIndex));
    helpView.setPreviousEnabled(this.historyIndex > 0);
    helpView.setNextEnabled(true);
  }

  /**
   * Controls the display of next page.
   */
  public void showNext() {
    HelpPane helpView = (HelpPane)getView();
    helpView.setPage(this.history.get(++this.historyIndex));
    helpView.setPreviousEnabled(true);
    helpView.setNextEnabled(this.historyIndex < this.history.size() - 1);
  }

  /**
   * Controls the display of the given <code>page</code>.
   */
  public void showPage(URL page) {
    HelpPane helpView = (HelpPane)getView();
    if (page.getProtocol().equals("http")) {
      helpView.setBrowserPage(page);
    } else if (this.historyIndex == -1
            || !this.history.get(this.historyIndex).equals(page)) {
      helpView.setPage(page);
      for (int i = this.history.size() - 1; i > this.historyIndex; i--) {
        this.history.remove(i);
      }
      this.history.add(page);
      helpView.setPreviousEnabled(++this.historyIndex > 0);
      helpView.setNextEnabled(false);
    }
  }
}
