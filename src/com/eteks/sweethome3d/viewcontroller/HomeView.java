/*
 * HomeView.java 28 oct 2008
 *
 * Copyright (c) 2008 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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

import java.util.concurrent.Callable;

import com.eteks.sweethome3d.model.InterruptedRecorderException;
import com.eteks.sweethome3d.model.RecorderException;

/**
 * The main view that displays a home.
 * @author Emmanuel Puybaret
 */
public interface HomeView extends View {
  /**
   * The actions proposed by the view to user.
   */
  public enum ActionType {
      NEW_HOME, CLOSE, OPEN, DELETE_RECENT_HOMES, SAVE, SAVE_AS, SAVE_AND_COMPRESS,
      PAGE_SETUP, PRINT_PREVIEW, PRINT, PRINT_TO_PDF, PREFERENCES, EXIT, 
      UNDO, REDO, CUT, COPY, PASTE, DELETE, SELECT_ALL,
      ADD_HOME_FURNITURE, DELETE_HOME_FURNITURE, MODIFY_FURNITURE, IMPORT_FURNITURE, IMPORT_FURNITURE_LIBRARY,
      SORT_HOME_FURNITURE_BY_CATALOG_ID, SORT_HOME_FURNITURE_BY_NAME, 
      SORT_HOME_FURNITURE_BY_WIDTH, SORT_HOME_FURNITURE_BY_DEPTH, SORT_HOME_FURNITURE_BY_HEIGHT, 
      SORT_HOME_FURNITURE_BY_X, SORT_HOME_FURNITURE_BY_Y, SORT_HOME_FURNITURE_BY_ELEVATION, 
      SORT_HOME_FURNITURE_BY_ANGLE, SORT_HOME_FURNITURE_BY_COLOR, 
      SORT_HOME_FURNITURE_BY_MOVABILITY, SORT_HOME_FURNITURE_BY_TYPE, SORT_HOME_FURNITURE_BY_VISIBILITY, 
      SORT_HOME_FURNITURE_BY_PRICE, SORT_HOME_FURNITURE_BY_VALUE_ADDED_TAX_PERCENTAGE, 
      SORT_HOME_FURNITURE_BY_VALUE_ADDED_TAX, SORT_HOME_FURNITURE_BY_PRICE_VALUE_ADDED_TAX_INCLUDED,
      SORT_HOME_FURNITURE_BY_DESCENDING_ORDER,
      DISPLAY_HOME_FURNITURE_CATALOG_ID, DISPLAY_HOME_FURNITURE_NAME, 
      DISPLAY_HOME_FURNITURE_WIDTH, DISPLAY_HOME_FURNITURE_DEPTH, DISPLAY_HOME_FURNITURE_HEIGHT, 
      DISPLAY_HOME_FURNITURE_X, DISPLAY_HOME_FURNITURE_Y, DISPLAY_HOME_FURNITURE_ELEVATION, 
      DISPLAY_HOME_FURNITURE_ANGLE, DISPLAY_HOME_FURNITURE_COLOR, 
      DISPLAY_HOME_FURNITURE_MOVABLE, DISPLAY_HOME_FURNITURE_DOOR_OR_WINDOW, DISPLAY_HOME_FURNITURE_VISIBLE,
      DISPLAY_HOME_FURNITURE_PRICE, DISPLAY_HOME_FURNITURE_VALUE_ADDED_TAX_PERCENTAGE,
      DISPLAY_HOME_FURNITURE_VALUE_ADDED_TAX, DISPLAY_HOME_FURNITURE_PRICE_VALUE_ADDED_TAX_INCLUDED,
      ALIGN_FURNITURE_ON_TOP, ALIGN_FURNITURE_ON_BOTTOM, ALIGN_FURNITURE_ON_LEFT, ALIGN_FURNITURE_ON_RIGHT,
      SELECT, CREATE_WALLS, CREATE_ROOMS, CREATE_DIMENSION_LINES, CREATE_LABELS, DELETE_SELECTION, 
      LOCK_BASE_PLAN, UNLOCK_BASE_PLAN, MODIFY_WALL, REVERSE_WALL_DIRECTION, SPLIT_WALL, MODIFY_ROOM, MODIFY_LABEL,
      INCREASE_TEXT_SIZE, DECREASE_TEXT_SIZE, TOGGLE_BOLD_STYLE, TOGGLE_ITALIC_STYLE,
      IMPORT_BACKGROUND_IMAGE, MODIFY_BACKGROUND_IMAGE, HIDE_BACKGROUND_IMAGE, SHOW_BACKGROUND_IMAGE, DELETE_BACKGROUND_IMAGE, 
      ZOOM_OUT, ZOOM_IN, EXPORT_TO_SVG,
      VIEW_FROM_TOP, VIEW_FROM_OBSERVER, MODIFY_3D_ATTRIBUTES, CREATE_PHOTO, EXPORT_TO_OBJ,
      HELP, ABOUT}
  public enum SaveAnswer {SAVE, CANCEL, DO_NOT_SAVE}

  /**
   * Enables or disables the action matching <code>actionType</code>.
   */
  public abstract void setEnabled(ActionType actionType,
                                  boolean enabled);

  /**
   * Sets the name and tool tip of undo and redo actions. If a parameter is <code>null</code>,
   * the properties will be reset to their initial values.
   */
  public abstract void setUndoRedoName(String undoText,
                                       String redoText);

  /**
   * Enables or disables transfer between components.  
   */
  public abstract void setTransferEnabled(boolean enabled);

  /**
   * Displays a content chooser open dialog to choose the name of a home.
   */
  public abstract String showOpenDialog();

  /**
   * Displays a content chooser open dialog to choose a furniture library.
   */
  public abstract String showImportFurnitureLibraryDialog();

  /**
   * Displays a dialog that lets user choose whether he wants to overwrite
   * an existing furniture library or not. 
   */
  public abstract boolean confirmReplaceFurnitureLibrary(String furnitureLibraryName);

  /**
   * Displays a dialog that lets user choose whether he wants to overwrite
   * an existing plug-in or not. 
   */
  public abstract boolean confirmReplacePlugin(String pluginName);

  /**
   * Displays a content chooser save dialog to choose the name of a home.
   */
  public abstract String showSaveDialog(String homeName);

  /**
   * Displays a dialog that lets user choose whether he wants to save
   * the current home or not.
   * @return {@link SaveAnswer#SAVE} if user chose to save home,
   * {@link SaveAnswer#DO_NOT_SAVE} if user don't want to save home,
   * or {@link SaveAnswer#CANCEL} if doesn't want to continue current operation.
   */
  public abstract SaveAnswer confirmSave(String homeName);

  /**
   * Displays a dialog that let user choose whether he wants to save
   * a home that was created with a newer version of Sweet Home 3D.
   * @return <code>true</code> if user confirmed to save.
   */
  public abstract boolean confirmSaveNewerHome(String homeName);

  /**
   * Displays a dialog that let user choose whether he wants to delete 
   * the selected furniture from catalog or not.
   * @return <code>true</code> if user confirmed to delete.
   */
  public abstract boolean confirmDeleteCatalogSelection();
  
  /**
   * Displays a dialog that let user choose whether he wants to exit 
   * application or not.
   * @return <code>true</code> if user confirmed to exit.
   */
  public abstract boolean confirmExit();
  
  /**
   * Displays <code>message</code> in an error message box.
   */
  public abstract void showError(String message);

  /**
   * Displays <code>message</code> in a message box.
   */
  public abstract void showMessage(String message);

  /**
   * Displays the tip matching <code>actionTipKey</code> and 
   * returns <code>true</code> if the user chose not to display again the tip.
   */
  public abstract boolean showActionTipMessage(String actionTipKey);
  
  /**
   * Displays an about dialog.
   */
  public abstract void showAboutDialog();

  /**
   * Shows a print dialog to print the home displayed by this pane.  
   * @return a print task to execute or <code>null</code> if the user canceled print.
   *    The <code>call<code> method of the returned task may throw a 
   *    {@link RecorderException RecorderException} exception if print failed 
   *    or an {@link InterruptedRecorderException InterruptedRecorderException}
   *    exception if it was interrupted.
   */
  public abstract Callable<Void> showPrintDialog();

  /**
   * Shows a content chooser save dialog to print a home in a PDF file.
   */
  public abstract String showPrintToPDFDialog(String homeName);

  /**
   * Prints a home to a given PDF file. This method may be overridden
   * to write to another kind of output stream.
   */
  public abstract void printToPDF(String pdfFile) throws RecorderException;

  /**
   * Shows a content chooser save dialog to export a home plan in a SVG file.
   */
  public abstract String showExportToSVGDialog(String name);

  /**
   * Exports the plan objects to a given SVG file.
   */
  public abstract void exportToSVG(String svgName) throws RecorderException;

  /**
   * Shows a content chooser save dialog to export a 3D home in a OBJ file.
   */
  public abstract String showExportToOBJDialog(String homeName);
  
  /**
   * Exports the 3D home objects to a given OBJ file.
   */
  public abstract void exportToOBJ(String objFile) throws RecorderException;

  /**
   * Returns <code>true</code> if clipboard contains data that
   * components are able to handle.
   */
  public abstract boolean isClipboardEmpty();

  /**
   * Execute <code>runnable</code> asynchronously in the thread 
   * that manages toolkit events.  
   */
  public abstract void invokeLater(Runnable runnable);
}