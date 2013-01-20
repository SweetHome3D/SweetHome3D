/*
 * ContentManager.java 11 juil. 07
 *
 * Sweet Home 3D, Copyright (c) 2007 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.RecorderException;

/**
 * Content manager.
 * @author Emmanuel Puybaret
 */
public interface ContentManager {
  public enum ContentType {SWEET_HOME_3D, MODEL, IMAGE, CSV, SVG, OBJ, PNG, JPEG, MOV, PDF, LANGUAGE_LIBRARY, TEXTURES_LIBRARY, FURNITURE_LIBRARY, PLUGIN, PHOTOS_DIRECTORY, USER_DEFINED};

  /**
   * Returns a {@link Content content} object that references a given content location.
   */
  public abstract Content getContent(String contentLocation) throws RecorderException;

  /**
   * Returns a human readable string for a given content location.
   */
  public abstract String getPresentationName(String contentLocation,
                                             ContentType contentType);

  /**
   * Returns <code>true</code> if the content location in parameter is accepted
   * for <code>contentType</code>.
   */
  public abstract boolean isAcceptable(String contentLocation,
                                       ContentType contentType);

  /**
   * Returns the content location chosen by user with an open content dialog.
   * @return the chosen content location or <code>null</code> if user canceled its choice.
   */
  public abstract String showOpenDialog(View parentView,
                                        String dialogTitle,
                                        ContentType contentType);

  /**
   * Returns the content location chosen by user with a save content dialog.
   * If the returned location already exists, this method should have confirmed 
   * if the user wants to overwrite it before return. 
   * @return the chosen content location or <code>null</code> if user canceled its choice.
   */
  public abstract String showSaveDialog(View parentView,
                                        String dialogTitle,
                                        ContentType contentType,
                                        String location);
}