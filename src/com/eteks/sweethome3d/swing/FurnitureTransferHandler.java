/*
 * FurnitureTransferHandler.java 12 sept. 2006
 *
 * Sweet Home 3D, Copyright (c) 2006 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeFurnitureGroup;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.Level;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.ContentManager;
import com.eteks.sweethome3d.viewcontroller.FurnitureView;
import com.eteks.sweethome3d.viewcontroller.HomeController;
import com.eteks.sweethome3d.viewcontroller.TransferableView;
import com.eteks.sweethome3d.viewcontroller.View;

/**
 * Home furniture transfer handler.
 * @author Emmanuel Puybaret
 */
public class FurnitureTransferHandler extends LocatedTransferHandler {
  private final Home                 home;
  private final ContentManager       contentManager;
  private final HomeController       homeController;
  private JComponent                 transferableSource;
  private List<HomePieceOfFurniture> copiedFurniture;
  private Object                     copiedCSV;

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
    this.transferableSource = source;
    this.copiedFurniture = Home.getFurnitureSubList(this.home.getSelectedItems());
    final Transferable transferable = new HomeTransferableList(this.copiedFurniture);
    if (source instanceof TransferableView) {
      this.copiedCSV = null;
      // Retrieve the text that describes selected furniture in CSV format
      this.homeController.createTransferData(new TransferableView.TransferObserver() {
            public void dataReady(Object [] data) {
              for (Object transferedData : data) {
                if (transferedData instanceof String) {
                  copiedCSV = transferedData;
                  break;
                }
              }
            }
          }, TransferableView.DataType.FURNITURE_LIST);
      // Create a transferable that contains copied furniture and its CSV description
      return new Transferable () {
          public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (DataFlavor.stringFlavor.equals(flavor)) {
              return copiedCSV;
            } else {
              return transferable.getTransferData(flavor);
            }
          }

          public DataFlavor [] getTransferDataFlavors() {
            ArrayList<DataFlavor> dataFlavors =
                new ArrayList<DataFlavor>(Arrays.asList(transferable.getTransferDataFlavors()));
            dataFlavors.add(DataFlavor.stringFlavor);
            return dataFlavors.toArray(new DataFlavor [dataFlavors.size()]);
          }

          public boolean isDataFlavorSupported(DataFlavor flavor) {
            return transferable.isDataFlavorSupported(flavor)
              || DataFlavor.stringFlavor.equals(flavor);
          }
        };
    } else {
      return transferable;
    }
  }

  /**
   * Removes the copied element once moved.
   */
  @Override
  protected void exportDone(JComponent source, Transferable data, int action) {
    if (action == MOVE && this.copiedFurniture != null) {
      this.homeController.cut(this.copiedFurniture);
    }
    this.transferableSource =  null;
    this.copiedFurniture = null;
    this.copiedCSV = null;
    this.homeController.enablePasteAction();
  }

  /**
   * Returns <code>true</code> if flavors contains
   * {@link HomeTransferableList#HOME_FLAVOR HOME_FLAVOR} flavor
   * or <code>DataFlavor.javaFileListFlavor</code> flavor.
   */
  @Override
  public boolean canImportFlavor(DataFlavor [] flavors) {
    Level selectedLevel = this.home.getSelectedLevel();
    List<DataFlavor> flavorList = Arrays.asList(flavors);
    return (selectedLevel == null || selectedLevel.isViewable())
        && (flavorList.contains(HomeTransferableList.HOME_FLAVOR)
            || flavorList.contains(DataFlavor.javaFileListFlavor));
  }

  /**
   * Add to home the furniture contained in <code>transferable</code>.
   */
  @Override
  public boolean importData(JComponent destination, Transferable transferable) {
    if (canImportFlavor(transferable.getTransferDataFlavors())) {
      try {
        List<DataFlavor> flavorList = Arrays.asList(transferable.getTransferDataFlavors());
        if (flavorList.contains(HomeTransferableList.HOME_FLAVOR)) {
          List<Selectable> items = (List<Selectable>)transferable.getTransferData(HomeTransferableList.HOME_FLAVOR);
          List<HomePieceOfFurniture> furniture = Home.getFurnitureSubList(items);
          if (isDrop()) {
            HomePieceOfFurniture beforePiece = getDropPieceOfFurniture(destination);
            View furnitureView = this.homeController.getFurnitureController().getView();
            if ((destination == this.transferableSource
                   || destination instanceof JViewport
                       && ((JViewport)destination).getView() == this.transferableSource)
                && furnitureView != null) {
              this.homeController.getFurnitureController().moveSelectedFurnitureBefore(beforePiece);
            } else {
              this.homeController.drop(furniture,
                  furnitureView != null && SwingUtilities.isDescendingFrom(destination, (JComponent)furnitureView) ? furnitureView : null,
                  beforePiece);
            }
            this.copiedFurniture = null;
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

  /**
   * Returns the piece of furniture upon which the cursor was dropped.
   */
  private HomePieceOfFurniture getDropPieceOfFurniture(JComponent destination) {
    if (OperatingSystem.isJavaVersionGreaterOrEqual("1.6")
        && destination instanceof FurnitureView) {
      try {
        // Call Java 6 getDropLocation().getRow() by reflection
        Object dropLocation = destination.getClass().getMethod("getDropLocation").invoke(destination);
        if ((Boolean)dropLocation.getClass().getMethod("isInsertRow").invoke(dropLocation)) {
          int row = (Integer)dropLocation.getClass().getMethod("getRow").invoke(dropLocation);
          TableModel tableModel = ((JTable)destination).getModel();
          if (row != -1
              && row < tableModel.getRowCount()) {
            HomePieceOfFurniture piece = (HomePieceOfFurniture)tableModel.getValueAt(row, 0);
            if (this.copiedFurniture != null) {
              // Search first piece which is not copied
              while (this.copiedFurniture.contains(piece)
                     || isPieceOfFurnitureChild(piece, this.copiedFurniture)) {
                if (++row < tableModel.getRowCount()) {
                  piece = (HomePieceOfFurniture)tableModel.getValueAt(row, 0);
                } else {
                  piece = null;
                  break;
                }
              }
              if (piece instanceof HomeFurnitureGroup) {
                // Don't handle complicated cases where the group would be emptied by a move
                HomeFurnitureGroup group = (HomeFurnitureGroup)piece;
                if (this.copiedFurniture.containsAll(group.getFurniture())) {
                  piece = null;
                }
                List<HomePieceOfFurniture> groupFurniture = new ArrayList<HomePieceOfFurniture>(group.getAllFurniture());
                for (Iterator<HomePieceOfFurniture> it = groupFurniture.iterator(); it.hasNext();) {
                  if (it.next() instanceof HomeFurnitureGroup) {
                    it.remove();
                  }
                }
                if (this.copiedFurniture.containsAll(groupFurniture)) {
                  piece = null;
                }
              }
            }
            return piece;
          }
        }
      } catch (Exception ex) {
        // Shouldn't happen
        ex.printStackTrace();
      }
    }
    return null;
  }

  /**
   * Returns <code>true</code> if the given <code>piece</code> is a child of furniture groups among <code>furniture</code>.
   */
  private boolean isPieceOfFurnitureChild(HomePieceOfFurniture piece, List<HomePieceOfFurniture> furniture) {
    for (HomePieceOfFurniture item : furniture) {
      if (item instanceof HomeFurnitureGroup
          && ((HomeFurnitureGroup)item).getAllFurniture().contains(piece)) {
        return true;
      }
    }
    return false;
  }
}
