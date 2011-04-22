/*
 * InterruptedRecorderException.java 29 sept 2008
 *
 * Sweet Home 3D, Copyright (c) 2008 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
 * Exception thrown when an a thread is interrupted during an access to data in IO layer.
 * @author Emmanuel Puybaret
 */
public class InterruptedRecorderException extends RecorderException {
  private static final long serialVersionUID = 1L;

  /**
   * Creates a default <code>InterruptedRecorderException</code>.
   */
  public InterruptedRecorderException() {
    super();
  }

  /**
   * Creates a <code>InterruptedRecorderException</code> from its message.
   */
  public InterruptedRecorderException(String message) {
    super(message);
  }
}
