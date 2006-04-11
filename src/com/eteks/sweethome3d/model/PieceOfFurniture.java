/*
 * PieceOfFurniture.java 7 avr. 2006
 * 
 * Copyright (c) 2006 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights
 * Reserved.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.eteks.sweethome3d.model;

import java.net.URL;

/**
 * A piece of furniture.
 * @author Emmanuel Puybaret
 */
public class PieceOfFurniture {
  private String  name;
  private String  category;
  private URL     iconURL;
  private URL     modelURL;
  private float   width;
  private float   depth;
  private float   height;
  private boolean doorOrWindow;

  /**
   * Creates a piece of furniture with all its values.
   * @param name  the name of the new piece
   * @param category the category of the new piece
   * @param iconURL an URL to the icon file of the new piece
   * @param modelURL an URL to the 3D model file of the new piece
   * @param width  the width in meters of the new piece
   * @param depth  the depth in meters of the new piece
   * @param height  the height in meters of the new piece
   * @param doorOrWindow if true, the new piece is a door or a window
   */
  public PieceOfFurniture(String name, String category,
                          URL iconURL, URL modelURL, float width,
                          float depth, float height,
                          boolean doorOrWindow) {
    this.name = name;
    this.category = category;
    this.iconURL = iconURL;
    this.modelURL = modelURL;
    this.width = width;
    this.depth = depth;
    this.height = height;
    this.doorOrWindow = doorOrWindow;
  }

  /**
   * @return Returns the category.
   */
  public String getCategory() {
    return this.category;
  }

  /**
   * @param category The category to set.
   */
  public void setCategory(String category) {
    this.category = category;
  }

  /**
   * @return Returns the depth.
   */
  public float getDepth() {
    return this.depth;
  }

  /**
   * @param depth The depth to set.
   */
  public void setDepth(float depth) {
    this.depth = depth;
  }

  /**
   * @return Returns the doorOrWindow.
   */
  public boolean isDoorOrWindow() {
    return this.doorOrWindow;
  }

  /**
   * @param doorOrWindow The doorOrWindow to set.
   */
  public void setDoorOrWindow(boolean doorOrWindow) {
    this.doorOrWindow = doorOrWindow;
  }

  /**
   * @return Returns the height.
   */
  public float getHeight() {
    return this.height;
  }

  /**
   * @param height The height to set.
   */
  public void setHeight(float height) {
    this.height = height;
  }

  /**
   * @return Returns the iconURL.
   */
  public URL getIconURL() {
    return this.iconURL;
  }

  /**
   * @param iconURL The iconURL to set.
   */
  public void setIconURL(URL iconURL) {
    this.iconURL = iconURL;
  }

  /**
   * @return Returns the modelURL.
   */
  public URL getModelURL() {
    return this.modelURL;
  }

  /**
   * @param modelURL The modelURL to set.
   */
  public void setModelURL(URL modelURL) {
    this.modelURL = modelURL;
  }

  /**
   * @return Returns the width.
   */
  public float getWidth() {
    return this.width;
  }

  /**
   * @param width The width to set.
   */
  public void setWidth(float width) {
    this.width = width;
  }

  /**
   * @param name The name to set.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return Returns the name.
   */
  public String getName() {
    return this.name;
  }
}
