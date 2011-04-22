/*
 * PlanTransferHandler.java 12 sept. 2006
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
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.viewcontroller.ContentManager;
import com.eteks.sweethome3d.viewcontroller.HomeController;
import com.eteks.sweethome3d.viewcontroller.View;

/**
 * Plan transfer handler.
 * @author Emmanuel Puybaret
 */
public class PlanTransferHandler extends LocatedTransferHandler {
  private final Home           home;
  private final ContentManager contentManager;
  private final HomeController homeController;
  private List<Selectable>     copiedItems;
  private BufferedImage        copiedImage;
  private boolean              isDragging;
  
  /**
   * Creates a handler able to transfer furniture and walls in plan.
   */
  public PlanTransferHandler(Home home, ContentManager contentManager, 
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
   * Returns a transferable object that contains a copy of the selected items in home
   * and an image of the selected items. 
   */
  @Override
  protected Transferable createTransferable(final JComponent source) {
    this.copiedItems = this.home.getSelectedItems();
    final Transferable transferable = new HomeTransferableList(this.copiedItems);
    if (source instanceof PlanComponent) {
      // Create an image that contains only selected items
      this.copiedImage = ((PlanComponent)source).getClipboardImage();
      // Create a transferable that contains copied items and an image
      return new Transferable () {
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
          if (DataFlavor.imageFlavor.equals(flavor)) {
            return copiedImage;
          } else {
            return transferable.getTransferData(flavor);
          }
        }

        public DataFlavor [] getTransferDataFlavors() {
          ArrayList<DataFlavor> dataFlavors = 
              new ArrayList<DataFlavor>(Arrays.asList(transferable.getTransferDataFlavors()));
          dataFlavors.add(DataFlavor.imageFlavor);
          return dataFlavors.toArray(new DataFlavor [dataFlavors.size()]);
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
          return transferable.isDataFlavorSupported(flavor)
            || DataFlavor.imageFlavor.equals(flavor);
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
    if (action == MOVE) {
      this.homeController.cut(this.copiedItems);      
    }
    this.copiedItems = null;
    this.copiedImage = null;
    this.homeController.enablePasteAction();    
  }

  /**
   * Returns <code>true</code> if <code>flavors</code> contains 
   * {@link HomeTransferableList#HOME_FLAVOR HOME_FLAVOR} flavor
   * or <code>DataFlavor.javaFileListFlavor</code> flavor.
   */
  @Override
  protected boolean canImportFlavor(DataFlavor [] flavors) {
    List<DataFlavor> flavorList = Arrays.asList(flavors);
    return flavorList.contains(HomeTransferableList.HOME_FLAVOR)
        || flavorList.contains(DataFlavor.javaFileListFlavor);
  }
  
  /**
   * Notifies home controller that a drag operation started if 
   * <code>transferable</code> data contains {@link HomeTransferableList#HOME_FLAVOR HOME_FLAVOR} 
   * flavor and destination is a plan.  
   */
  @Override
  protected void dragEntered(JComponent destination, Transferable transferable, int dragAction) {
    if (transferable.isDataFlavorSupported(HomeTransferableList.HOME_FLAVOR)
        && destination instanceof PlanComponent
        && this.homeController.getPlanController() != null) {
      try {
        List<Selectable> transferedItems = 
            (List<Selectable>)transferable.getTransferData(HomeTransferableList.HOME_FLAVOR);
        Point2D dropLocation = getDropModelLocation(destination);
        this.homeController.getPlanController().startDraggedItems(transferedItems, 
            (float)dropLocation.getX(), (float)dropLocation.getY());
        this.isDragging = true;
      } catch (UnsupportedFlavorException ex) {
        throw new RuntimeException("Can't import", ex);
      } catch (IOException ex) {
        throw new RuntimeException("Can't access to data", ex);
      }
    }
  }
  
  /**
   * Called when <code>transferable</code> data moved in <code>destination</code> component
   * during a drag and drop operation. Subclasses should override this method if they are
   * interested by this event.  
   */
  @Override
  protected void dragMoved(JComponent destination, Transferable transferable, int dragAction) {
    if (transferable.isDataFlavorSupported(HomeTransferableList.HOME_FLAVOR)
        && destination instanceof PlanComponent
        && this.homeController.getPlanController() != null) {
        Point2D dropLocation = getDropModelLocation(destination);
      this.homeController.getPlanController().moveMouse( 
          (float)dropLocation.getX(), (float)dropLocation.getY());
    }
  }
  
  /**
   * Called once the cursor left <code>destination</code> component during a drag and drop operation. 
   * Subclasses should override this method if they are interested by this event.  
   */
  @Override
  protected void dragExited(JComponent destination) {
    if (this.isDragging) {
      this.homeController.getPlanController().stopDraggedItems();
      this.isDragging = false;
    }
  }
  
  /**
   * Adds items contained in <code>transferable</code> to home.
   */
  @Override
  public boolean importData(JComponent destination, Transferable transferable) {
    if (canImportFlavor(transferable.getTransferDataFlavors())) {
      try {
        if (this.isDragging) {
          dragExited(destination);
        }
        List<DataFlavor> flavorList = Arrays.asList(transferable.getTransferDataFlavors());
        if (flavorList.contains(HomeTransferableList.HOME_FLAVOR)) {
          return importHomeTransferableList(destination, 
              (List<Selectable>)transferable.getTransferData(HomeTransferableList.HOME_FLAVOR));
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

  private boolean importHomeTransferableList(final JComponent destination, 
                                             final List<Selectable> transferedItems) {
    if (isDrop()) {
      Point2D dropLocation = getDropModelLocation(destination);
      if (destination instanceof View) {
        this.homeController.drop(transferedItems, (View)destination, 
            (float)dropLocation.getX(), (float)dropLocation.getY());
      } else {
        this.homeController.drop(transferedItems,  
            (float)dropLocation.getX(), (float)dropLocation.getY());
      }
    } else {
      this.homeController.paste(transferedItems);
    }
    return true;
  }
  
  private boolean importFileList(final JComponent destination, List<File> files) {
    final Point2D dropLocation = isDrop() 
        ? getDropModelLocation(destination)
        : new Point2D.Float();
    final List<String> importableModels = getModelContents(files, contentManager);
    EventQueue.invokeLater(new Runnable() {
        public void run() {
          homeController.dropFiles(importableModels, 
              (float)dropLocation.getX(), (float)dropLocation.getY());        
        }
      });
    return !importableModels.isEmpty();
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
