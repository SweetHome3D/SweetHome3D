/*
 * PrintPreviewController.java 27 aout 07
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

import javax.swing.JComponent;

import com.eteks.sweethome3d.model.Home;

/**
 * A MVC controller for home print preview view.
 * @author Emmanuel Puybaret
 */
public class PrintPreviewController {
  private Home           home;
  private HomeController homeController;
  private JComponent     printPreviewView;

  /**
   * Creates the controller of print preview with undo support.
   */
  public PrintPreviewController(Home home,
                                HomeController homeController) {
    this.home = home;
    this.homeController = homeController;
  }

  /**
   * Returns the view associated with this controller.
   */
  public JComponent getView() {
    // Create view lazily only once it's needed
    if (this.printPreviewView == null) {
      this.printPreviewView = new PrintPreviewPanel(this.home, this.homeController, this);
    }
    return this.printPreviewView;
  }
  
  /**
   * Displays the view controlled by this controller.
   */
  public void displayView(JComponent parentView) {
    ((PrintPreviewPanel)getView()).displayView(parentView);
  }
}
