/*
 * FurnitureCatalogTransferHandler.java 12 sept. 2006
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import com.eteks.sweethome3d.model.ContentManager;
import com.eteks.sweethome3d.model.FurnitureCatalog;

/**
 * Catalog transfer handler.
 * @author Emmanuel Puybaret
 */
public class FurnitureCatalogTransferHandler extends TransferHandler {
  private FurnitureCatalog           catalog;
  private ContentManager             contentManager;
  private FurnitureCatalogController catalogController;

  /**
   * Creates a handler able to transfer catalog selected furniture.
   */
  public FurnitureCatalogTransferHandler(FurnitureCatalog catalog) {
    this(catalog, null, null);
  }

  /**
   * Creates a handler able to transfer catalog selected furniture.
   */
  public FurnitureCatalogTransferHandler(FurnitureCatalog catalog,
                                ContentManager contentManager,
                                FurnitureCatalogController catalogController) {
    this.catalog = catalog;
    this.contentManager = contentManager;
    this.catalogController = catalogController;
  }

  /**
   * Returns <code>COPY</code>.
   */
  @Override
  public int getSourceActions(JComponent source) {
    return COPY;
  }

  /**
   * Returns a {@link HomeTransferableList transferable object}
   * that contains a copy of the selected furniture in catalog. 
   */
  @Override
  protected Transferable createTransferable(JComponent source) {
    return new HomeTransferableList(catalog.getSelectedFurniture());
  }

  /**
   * Returns <code>true</code> if flavors contains 
   * <code>DataFlavor.javaFileListFlavor</code> flavor.
   */
  @Override
  public boolean canImport(JComponent destination, DataFlavor [] flavors) {
    return this.catalogController != null
        && Arrays.asList(flavors).contains(DataFlavor.javaFileListFlavor);
  }

  /**
   * Add to catalog the furniture contained in <code>transferable</code>.
   */
  @Override
  public boolean importData(JComponent destination, Transferable transferable) {
    if (canImport(destination, transferable.getTransferDataFlavors())) {
      try {
        List<File> files = (List<File>)transferable.getTransferData(DataFlavor.javaFileListFlavor);
        final List<String> importableModels = new ArrayList<String>();        
        for (File file : files) {
          final String absolutePath = file.getAbsolutePath();
          if (this.contentManager.isAcceptable(absolutePath, ContentManager.ContentType.MODEL)) {
            importableModels.add(absolutePath);
          }        
        }
        EventQueue.invokeLater(new Runnable() {
            public void run() {
              catalogController.dropFiles(importableModels);
            }
          });
        return !importableModels.isEmpty();
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
