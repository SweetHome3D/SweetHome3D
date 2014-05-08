/*
 * DamagedHomeIOException.java 6 mai 2014
 *
 * Sweet Home 3D, Copyright (c) 2014 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
package com.eteks.sweethome3d.io;

import java.io.IOException;
import java.util.List;

import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.Home;

/**
 * Exception thrown when a home data is damaged containing invalid content.
 * @author Emmanuel Puybaret
 */
public class DamagedHomeIOException extends IOException {
  private static final long serialVersionUID = 1L;
  
  private Home damagedHome;
  private List<Content> invalidContent;

  /**
   * Creates an exception for the given damaged home with the invalid content it may contains.
   */
  public DamagedHomeIOException(Home damagedHome,
                                List<Content> invalidContent) {
    super();
    this.damagedHome = damagedHome;
    this.invalidContent = invalidContent;
  }
  
  /**
   * Returns the damaged home containing some possible invalid content.
   */
  public Home getDamagedHome() {
    return this.damagedHome;
  }
  
  /**
   * Returns the invalid content in the damaged home.
   */
  public List<Content> getInvalidContent() {
    return this.invalidContent;
  }
}
