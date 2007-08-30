/*
 * PageSetupController.java 27 aout 07
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

import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePrint;

/**
 * A MVC controller for home page setup view.
 * @author Emmanuel Puybaret
 */
public class PageSetupController {
  private Home                home;
  private UndoableEditSupport undoSupport;
  private ResourceBundle      resource;
  private JComponent          pageSetupView;

  /**
   * Creates the controller of page setup with undo support.
   */
  public PageSetupController(Home home,
                             UndoableEditSupport undoSupport) {
    this.home = home;
    this.undoSupport = undoSupport;
    this.resource    = ResourceBundle.getBundle(
        PageSetupController.class.getName());
    this.pageSetupView = new PageSetupPanel(home, this); 
    ((PageSetupPanel)this.pageSetupView).displayView();
  }

  /**
   * Returns the view associated with this controller.
   */
  public JComponent getView() {
    return this.pageSetupView;
  }

  /**
   * Controls the modification of home print attributes.
   */
  public void modifyHomePrint() {
    final HomePrint oldHomePrint = this.home.getPrint();
    final HomePrint homePrint = ((PageSetupPanel)getView()).getHomePrint();
    this.home.setPrint(homePrint);
    UndoableEdit undoableEdit = new AbstractUndoableEdit() {
      @Override
      public void undo() throws CannotUndoException {
        super.undo();
        home.setPrint(oldHomePrint);
      }
      
      @Override
      public void redo() throws CannotRedoException {
        super.redo();
        home.setPrint(homePrint);
      }
      
      @Override
      public String getPresentationName() {
        return resource.getString("undoSetupPageName");
      }
    };
    this.undoSupport.postEdit(undoableEdit);
  }
}
