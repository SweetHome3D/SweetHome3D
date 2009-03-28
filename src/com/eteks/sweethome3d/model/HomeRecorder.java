/*
 * HomeRecorder.java 30 août 2006
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
 * Homes recorder.
 * @author Emmanuel Puybaret
 */
public interface HomeRecorder {
  /**
   * Recorder type used as a hint to select a home recorder.
   * @see HomeApplication#getHomeRecorder(Type)
   * @since 1.8
   */
  public enum Type {
    /**
     * The default recorder type.
     */
    DEFAULT, 
    /**
     * A recorder type able to compress home data.
     */
    COMPRESSED}
  
  /**
   * Writes <code>home</code> data.
   * @param home  the home to write.
   * @param name  the name of the resource in which the home will be written. 
   */
  public void writeHome(Home home, String name) throws RecorderException;
  
  /**
   * Returns a home instance read from its <code>name</code>.
   * @param name  the name of the resource from which the home will be read. 
   */
  public Home readHome(String name) throws RecorderException;

  /**
   * Returns <code>true</code> if the home with a given <code>name</code>
   * exists.
   * @param name the name of the resource to check
   */
  public boolean exists(String name) throws RecorderException;
}
