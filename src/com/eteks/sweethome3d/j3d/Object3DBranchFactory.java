/*
 * Object3DBranchFactory.java 8 févr. 2011
 *
 * Copyright (c) 2011 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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
package com.eteks.sweethome3d.j3d;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.viewcontroller.Object3DFactory;

/**
 * A factory able to create instances of {@link Object3DBranch Object3DBranch} class.
 * @author Emmanuel Puybaret
 */
public class Object3DBranchFactory implements Object3DFactory {
  /**
   * Returns the 3D object matching a given <code>item</code>.
   */
  public Object createObject3D(Home home, Selectable item, boolean waitForLoading) {
    if (item instanceof HomePieceOfFurniture) {
      return new HomePieceOfFurniture3D((HomePieceOfFurniture)item, home, true, waitForLoading);
    } else if (item instanceof Wall) {
      return new Wall3D((Wall)item, home, true, waitForLoading);
    } else if (item instanceof Room) {
      return new Room3D((Room)item, home, false, false, waitForLoading);
    } else {
      throw new IllegalArgumentException("Can't create 3D object for an item of class " + item.getClass());
    }  
  }
}
