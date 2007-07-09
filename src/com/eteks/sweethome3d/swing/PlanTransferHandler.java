/*
 * PlanTransferHandler.java 12 sept. 2006
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

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import com.eteks.sweethome3d.model.Home;

/**
 * Plan transfer handler.
 * @author Emmanuel Puybaret
 */
public class PlanTransferHandler extends LocatedTransferHandler {
  private Home             home;
  private HomeController   homeController;
  private List<Object>     copiedItems;

  /**
   * Creates a handler able to transfer furniture and walls in plan.
   */
  public PlanTransferHandler(Home home, HomeController homeController) {
    this.home = home;  
    this.homeController = homeController;  
  }
  
  /**
   * Returns <code>COPY_OR_MOVE</code>.
   */
  @Override
  public int getSourceActions(JComponent source) {
    return COPY_OR_MOVE;
  }
  
  /**
   * Returns a {@link HomeTransferableList transferable object}
   * that contains a copy of the selected furniture in home. 
   */
  @Override
  protected Transferable createTransferable(JComponent source) {
    this.copiedItems = this.home.getSelectedItems();
    return new HomeTransferableList(this.copiedItems);
  }
  
  /**
   * Removes the copied element once moved.
   */
  @Override
  protected void exportDone(JComponent source, Transferable data, int action) {
    if (action == MOVE) {
      this.homeController.cut(this.copiedItems);      
    }
    this.copiedItems = null;
    this.homeController.enablePasteAction();    
  }

  /**
   * Returns <code>true</code> if flavors contains 
   * {@link HomeTransferableList#HOME_FLAVOR LIST_FLAVOR} flavor
   * or <code>DataFlavor.javaFileListFlavor</code> flavor.
   */
  @Override
  public boolean canImport(JComponent destination, DataFlavor [] flavors) {
    List<DataFlavor> flavorList = Arrays.asList(flavors);
    return flavorList.contains(HomeTransferableList.HOME_FLAVOR)
        || flavorList.contains(DataFlavor.javaFileListFlavor);
  }

  /**
   * Add to home items contained in <code>transferable</code>.
   */
  @Override
  public boolean importData(JComponent destination, Transferable transferable) {
    if (canImport(destination, transferable.getTransferDataFlavors())) {
      try {
        List<DataFlavor> flavorList = Arrays.asList(transferable.getTransferDataFlavors());
        if (flavorList.contains(HomeTransferableList.HOME_FLAVOR)) {
          return importHomeTransferableList(destination, 
              (List<Object>)transferable.getTransferData(HomeTransferableList.HOME_FLAVOR));
        } else {
          return importFileList(destination, 
              (List<File>)transferable.getTransferData(DataFlavor.javaFileListFlavor));
        }
      } catch (UnsupportedFlavorException ex) {
        throw new RuntimeException("Can't import", ex);
      } catch (IOException ex) {
        throw new RuntimeException("Can't access to data", ex);
      }
    } else {
      return false;
    }
  }

  private boolean importHomeTransferableList(JComponent destination, 
                                             List<Object> transferedItems) {
    if (isDrop()) {
      Point2D dropLocation = getDropModelLocation(destination);
      this.homeController.drop(transferedItems, 
          (float)dropLocation.getX(), (float)dropLocation.getY());
    } else {
      this.homeController.paste(transferedItems);
    }
    return true;
  }
  
  private boolean importFileList(JComponent destination, List<File> files) {
    // isDrop current implementation doesn't work under Java 5 
    // for a drag operation coming from outside JVM
    Point2D dropLocation = isDrop() 
      ? getDropModelLocation(destination)
      : new Point2D.Float();
    return this.homeController.dropFiles(files, 
        (float)dropLocation.getX(), (float)dropLocation.getY());
  }
  
  /**
   * Returns the drop location converted in model coordinates space.
   */
  private Point2D getDropModelLocation(JComponent destination) {
    float x = 0;
    float y = 0;
    if (destination instanceof PlanComponent) {
      PlanComponent planView = (PlanComponent)destination;
      Point dropLocation = getDropLocation();
      SwingUtilities.convertPointFromScreen(dropLocation, planView);
      x = planView.convertXPixelToModel(dropLocation.x);
      y = planView.convertYPixelToModel(dropLocation.y);
    }
    return new Point2D.Float(x, y);
  }
}
