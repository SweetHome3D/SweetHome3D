/*
 * FurnitureTransferHandler.java 12 sept. 2006
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

import java.awt.EventQueue;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;

import com.eteks.sweethome3d.model.ContentManager;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;

/**
 * Home furniture transfer handler.
 * @author Emmanuel Puybaret
 */
public class FurnitureTransferHandler extends LocatedTransferHandler {
  private Home                       home;
  private ContentManager             contentManager;
  private HomeController             homeController;
  private List<HomePieceOfFurniture> copiedFurniture;

  /**
   * Creates a handler able to transfer home furniture.
   */
  public FurnitureTransferHandler(Home home, 
                                  ContentManager contentManager,
                                  HomeController homeController) {
    this.home = home;  
    this.contentManager = contentManager;
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
    this.copiedFurniture = Home.getFurnitureSubList(this.home.getSelectedItems());
    return new HomeTransferableList(this.copiedFurniture);
  }
  
  /**
   * Removes the copied element once moved.
   */
  @Override
  protected void exportDone(JComponent source, Transferable data, int action) {
    if (action == MOVE) {
      this.homeController.cut(copiedFurniture);      
    }
    this.copiedFurniture = null;
    this.homeController.enablePasteAction();
  }

  /**
   * Returns <code>true</code> if flavors contains 
   * {@link HomeTransferableList#HOME_FLAVOR LIST_FLAVOR} flavor.
   */
  @Override
  public boolean canImport(JComponent destination, DataFlavor [] flavors) {
    List<DataFlavor> flavorList = Arrays.asList(flavors);
    return flavorList.contains(HomeTransferableList.HOME_FLAVOR)
        || flavorList.contains(DataFlavor.javaFileListFlavor);
  }

  /**
   * Add to home the furniture contained in <code>transferable</code>.
   */
  @Override
  public boolean importData(JComponent destination, Transferable transferable) {
    if (canImport(destination, transferable.getTransferDataFlavors())) {
      try {
        List<DataFlavor> flavorList = Arrays.asList(transferable.getTransferDataFlavors());
        if (flavorList.contains(HomeTransferableList.HOME_FLAVOR)) {
          List<Object> items = (List<Object>)transferable.
              getTransferData(HomeTransferableList.HOME_FLAVOR);
          List<HomePieceOfFurniture> furniture = Home.getFurnitureSubList(items);
          if (isDrop()) {
            this.homeController.drop(furniture, 0, 0);
          } else {
            this.homeController.paste(furniture);          
          }
          return true;
        } else {
          List<File> files = (List<File>)transferable.getTransferData(DataFlavor.javaFileListFlavor);
          final List<String> importableModels = getModelContents(files, this.contentManager);
          EventQueue.invokeLater(new Runnable() {
              public void run() {
                homeController.dropFiles(importableModels, 0, 0);          
              }
            });
          return !importableModels.isEmpty();
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
}
