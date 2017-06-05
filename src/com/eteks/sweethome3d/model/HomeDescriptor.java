/*
 * HomeDescriptor.java 31 mai 2017
 *
 * Sweet Home 3D, Copyright (c) 2017 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
 * A descriptor that gives access to some data of a home not yet created.
 * @author Emmanuel Puybaret
 */
public class HomeDescriptor {
  private final String  name;
  private final Content content; 
  private final Content icon;
  
  /**
   * Creates a home descriptor.
   * @param name name of the home
   * @param content content that allows to read home data
   * @param icon icon of the home
   */
  public HomeDescriptor(String name, Content content, Content icon) {
    this.name = name;
    this.content = content;
    this.icon = icon;
  }

  /**
   * Returns the name of this home.
   */
  public String getName() {
    return this.name;
  }

  /**
   * Returns the content to read this home.
   */
  public Content getContent() {
    return this.content;
  }

  /**
   * Returns the icon of this home.
   */
  public Content getIcon() {
    return this.icon;
  }
}
