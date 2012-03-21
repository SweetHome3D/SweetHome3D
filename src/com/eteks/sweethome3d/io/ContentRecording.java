/*
 * ContentRecording.java 21 mars 2012
 *
 * Sweet Home 3D, Copyright (c) 2012 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

/**
 * Describes how {@linkplain com.eteks.sweethome3d.model.Content content} associated to a home should be managed during recording.
 * @author Emmanuel Puybaret
 */
public enum ContentRecording {
  /**
   * Include all content objects referenced by a {@link com.eteks.sweethome3d.model.Home Home} instance.
   */
  INCLUDE_ALL_CONTENT,
  /**
   * Include content instances of <code>TemporaryURLContent</code> class referenced by a 
   * {@link com.eteks.sweethome3d.model.Home Home} instance as well as the content already saved with it. 
   */
  INCLUDE_TEMPORARY_CONTENT,
  /**
   * Include no content instance referenced by a {@link com.eteks.sweethome3d.model.Home Home} instance.
   * This should be used only if all the content objects reference external data which is always available 
   * (with an URL for example). 
   */
  INCLUDE_NO_CONTENT
}
