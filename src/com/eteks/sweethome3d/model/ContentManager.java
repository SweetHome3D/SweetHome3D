/*
 * ContentManager.java 11 juil. 07
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
package com.eteks.sweethome3d.model;

/**
 * Content manager.
 * @author Emmanuel Puybaret
 */
public interface ContentManager {
  public enum ContentType {SWEET_HOME_3D, MODEL, IMAGE, PDF};

  /**
   * Returns a {@link Content content} object that references a given content name.
   */
  public abstract Content getContent(String contentName) throws RecorderException;

  /**
   * Returns a human readable string for a given content name.
   */
  public abstract String getPresentationName(String contentName,
                                             ContentType contentType);

  /**
   * Returns <code>true</code> if the content name in parameter is accepted
   * for <code>contentType</code>.
   */
  public abstract boolean isAcceptable(String contentName,
                                       ContentType contentType);

  /**
   * Returns the content name chosen by user with an open content dialog.
   * @return the chosen content name or <code>null</code> if user cancelled its choice.
   */
  public abstract String showOpenDialog(String dialogTitle,
                                        ContentType contentType);

  /**
   * Returns the content name chosen by user with a save content dialog.
   * If the returned name already exists, this method should have confirmed 
   * if the user wants to overwrite it before return. 
   * @return the chosen content name or <code>null</code> if user cancelled its choice.
   */
  public abstract String showSaveDialog(String dialogTitle,
                                        ContentType contentType,
                                        String name);
}