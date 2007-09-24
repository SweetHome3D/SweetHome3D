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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JComponent;

import com.eteks.sweethome3d.model.UserPreferences;

/**
 * A MVC controller for Sweet Home 3D help view.
 * @author Emmanuel Puybaret
 */
public class HelpController {
  private List<URL>      history;
  private int            historyIndex;
  private JComponent     helpView;
  
  public HelpController(UserPreferences preferences) {
    this.history = new ArrayList<URL>();
    historyIndex = -1;
    this.helpView = new HelpPane(preferences, this);
    addLanguageListener(preferences);
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
    showPage(HelpController.class.getResource(
        ResourceBundle.getBundle(HelpController.class.getName()).getString("helpIndex")));
    ((HelpPane)getView()).displayView();
  }
  
  /**
   * Adds a property change listener to <code>preferences</code> to update
   * displayed page when language changes.
   */
  private void addLanguageListener(UserPreferences preferences) {
    preferences.addPropertyChangeListener(UserPreferences.Property.LANGUAGE, 
        new LanguageChangeListener(this));
  }

  /**
   * Preferences property listener bound to this component with a weak reference to avoid
   * strong link between preferences and this component.  
   */
  private static class LanguageChangeListener implements PropertyChangeListener {
    private WeakReference<HelpController> helpController;

    public LanguageChangeListener(HelpController helpController) {
      this.helpController = new WeakReference<HelpController>(helpController);
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
      // If help controller was garbage collected, remove this listener from preferences
      HelpController helpController = this.helpController.get();
      if (helpController == null) {
        ((UserPreferences)ev.getSource()).removePropertyChangeListener(
            UserPreferences.Property.LANGUAGE, this);
      } else {
        // Updates home page from current default locale
        helpController.history.clear();
        helpController.historyIndex = -1;
        helpController.showPage(HelpController.class.getResource(
            ResourceBundle.getBundle(HelpController.class.getName()).getString("helpIndex")));
      }
    }
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
