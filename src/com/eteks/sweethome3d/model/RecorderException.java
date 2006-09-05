/*
 * RecorderException.java 30 août 2006
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
package com.eteks.sweethome3d.model;

/**
 * Exception thrown by methods that access to data in IO layer.
 * @author Emmanuel Puybaret
 */
public class RecorderException extends Exception {
  private static final long serialVersionUID = 1L;

  /**
   * Creates a default <code>RecorderException</code>.
   */
  public RecorderException() {
    super();
  }

  /**
   * Creates a <code>RecorderException</code> from its message.
   */
  public RecorderException(String message) {
    super(message);
  }

  /**
   * Creates a <code>RecorderException</code> with its message 
   * and the internal cause that initiated this exception.
   */
  public RecorderException(String message, Throwable cause) {
    super(message, cause);
  }
}
