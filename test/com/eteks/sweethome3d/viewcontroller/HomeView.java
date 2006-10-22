/*
 * HomeView.java 29 mai 2006
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
package com.eteks.sweethome3d.viewcontroller;

/**
 * A MVC view for home view.
 * @author Emmanuel Puybaret
 */
public interface HomeView extends View {
  public enum ActionType {
    NEW_HOME, CLOSE, OPEN, SAVE, SAVE_AS, EXIT, 
    UNDO, REDO, 
    ADD_HOME_FURNITURE, DELETE_HOME_FURNITURE,
    WALL_CREATION, DELETE_SELECTION}
  public enum SaveAnswer {SAVE, CANCEL, DO_NOT_SAVE}

  /**
   * Enables or disables the action matching <code>actionType</code>.
   */
  public void setEnabled(ActionType actionType, boolean enabled);

  /**
   * Sets the name of undo and redo actions. If a parameter is null,
   * the properties will be reset to their initial values.
   */
  public void setUndoRedoName(String undoText, String redoText);
  
  /**
   * Displays a file chooser dialog to open a .sh3d file.
   */
  public String showOpenDialog();

  /**
   * Displays a file chooser dialog to save a home in a .sh3d file.
   */
  public String showSaveDialog(String name);

  /**
   * Displays <code>message</code> in an error message box.
   */
  public void showError(String message);

  /**
   * Displays a dialog that let user choose whether he wants to overwrite 
   * file <code>name</code> or not.
   * @return <code>true</code> if user confirmed to overwrite.
   */
  public boolean confirmOverwrite(String name);

  /**
   * Displays a dialog that let user choose whether he wants to save
   * the current home or not.
   * @return {@link SaveAnswer#SAVE} if user choosed to save home,
   * {@link SaveAnswer#DO_NOT_SAVE} if user don't want to save home,
   * or {@link SaveAnswer#CANCEL} if doesn't want to continue current operation.
   */
  public SaveAnswer confirmSave(String name);
  
  /**
   * Displays a dialog that let user choose whether he wants to exit 
   * application or not.
   * @return <code>true</code> if user confirmed to exit.
   */
  public boolean confirmExit();
}
