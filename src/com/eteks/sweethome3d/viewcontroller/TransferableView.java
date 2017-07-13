/*
 * TransferableView.java 11 juil. 2017
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

/**
 * A view able to transfer data.
 * @author Emmanuel Puybaret
 * @since 5.5
 */
public interface TransferableView extends View {
  /**
   * Data types.
   */
  public static class DataType {
    // Don't qualify DataType as an enumeration to be able to extend DataType class
    public static final DataType PLAN_IMAGE      = new DataType("PLAN_IMAGE");
    public static final DataType FURNITURE_LIST  = new DataType("FURNITURE_LIST");
    
    private final String name;
    
    protected DataType(String name) {
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
   * An observer to follow the data created for transfer.
   */
  public static interface TransferObserver {
    public void dataReady(Object [] data);
  }

  /**
   * Returns data at given types for transfer purpose.
   * Caution : this method can be called from a separate thread.
   */
  public abstract Object createTransferData(DataType dataType);
}
