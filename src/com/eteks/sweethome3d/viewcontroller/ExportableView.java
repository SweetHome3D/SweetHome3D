/*
 * ExportableView.java 13 juil. 2017
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
package com.eteks.sweethome3d.viewcontroller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

/**
 * A view able to export data in an output stream.
 * @author Emmanuel Puybaret
 * @since 5.5
 */
public interface ExportableView extends View {
  /**
   * Data types.
   */
  public static class FormatType {
    // Don't qualify FormatType as an enumeration to be able to extend FormatType class
    public static final FormatType SVG  = new FormatType("SVG");
    public static final FormatType CSV  = new FormatType("CSV");
    
    private final String name;
    
    protected FormatType(String name) {
      this.name = name;      
    }
    
    public final String name() {
      return this.name;
    }
    
    @Override
    public String toString() {
      return this.name;
    }
  };
  
  /**
   * Returns <code>true</code> if this view is able to export at the given format.
   */
  public abstract boolean isFormatTypeSupported(FormatType formatType);

  /**
   * Exports data of the view at the given format.
   * Caution : this method can be called from a separate thread.
   */
  public abstract void exportData(OutputStream out, 
                                  FormatType formatType, 
                                  Properties settings) throws IOException;
}
