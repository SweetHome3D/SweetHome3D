/*
 * HomeTransferableList.java 12 sept. 2006
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
package com.eteks.sweethome3d.swing;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.ArrayList;
import java.util.List;

import com.eteks.sweethome3d.model.Camera;
import com.eteks.sweethome3d.model.DimensionLine;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.PieceOfFurniture;
import com.eteks.sweethome3d.model.Wall;

/**
 * A transferable class that manages the transfer of a list of objects in a home.
 * @author Emmanuel Puybaret
 */
public class HomeTransferableList implements Transferable {
  public final static DataFlavor HOME_FLAVOR;
  
  static {
    try {
      // Create HomeTransferableList data flavor
      String homeFlavorMimeType = 
        DataFlavor.javaJVMLocalObjectMimeType
        + ";class=" + HomeTransferableList.class.getName();
      HOME_FLAVOR = new DataFlavor(homeFlavorMimeType);
    } catch (ClassNotFoundException ex) {
      throw new RuntimeException(ex);
    }
  }
  
  // Stores a copy of the tranfered items
  private List<Object> transferedItems;

  /**
   * Creates a transferable list of a copy of <code>items</code>.
   */
  public HomeTransferableList(List<? extends Object> items) {
    this.transferedItems = deepCopy(items);
  }

  /**
   * Performs a deep copy of home <code>objects</code>.
   */
  public static List<Object> deepCopy(List<? extends Object> objects) {
    List<Object> list = new ArrayList<Object>();
    for (Object obj : objects) {
      if (obj instanceof PieceOfFurniture) {
        list.add(new HomePieceOfFurniture((PieceOfFurniture)obj));
      } else if (obj instanceof DimensionLine) {
        list.add(new DimensionLine((DimensionLine)obj));
      } else if (!(obj instanceof Wall)
                 && !(obj instanceof Camera)) { // Camera isn't copiable
        throw new RuntimeException(
            "HomeTransferableList can't contain " + obj.getClass().getName());
      }
    }
    // Add to list a deep copy of walls with their walls at start and end point set
    list.addAll(Wall.deepCopy(Home.getWallsSubList(objects)));
    return list;
  }

  /**
   * Returns a copy of the transfered items list.
   */
  public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
    if (flavor.equals(HOME_FLAVOR)) {
      return deepCopy(this.transferedItems);
    } else {
      throw new UnsupportedFlavorException(flavor);
    }
  }

  /**
   * Returns the {@link #HOME_FLAVOR data flavor} of the transfered data 
   * of this transferable object.
   */
  public DataFlavor [] getTransferDataFlavors() {
    return new DataFlavor [] {HOME_FLAVOR};
  }

  /**
   * Returns <code>true</code> if <code>flavor</code> is 
   * {@link #HOME_FLAVOR HOME_FLAVOR}.
   */
  public boolean isDataFlavorSupported(DataFlavor flavor) {
    return HOME_FLAVOR.equals(flavor);
  }
}