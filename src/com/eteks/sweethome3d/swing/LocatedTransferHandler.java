/*
 * LocatedTransferHandler.java 12 sept. 2006
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

import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TooManyListenersException;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import com.eteks.sweethome3d.viewcontroller.ContentManager;

/**
 * Transfer handler that stores the dropped location of mouse pointer.
 * @author Emmanuel Puybaret
 */
public abstract class LocatedTransferHandler extends TransferHandler {
  private JComponent        currentDestination;
  private DropTargetAdapter destinationDropTargetListener;
  private Point             dropLocation;

  /**
   * Adds a listener to the drop target of <code>destination</code> that
   * will keep track of the current drop location and returns the value returned
   * by {@link #canImportFlavor(DataFlavor[]) canImportFlavor} method.
   * Once drop is finished the listener will be removed.
   */
  @Override
  public final boolean canImport(JComponent destination, DataFlavor [] flavors) {
    boolean canImportFlavor = canImportFlavor(flavors);
    
    // Add a drop target listener that will store drop location
    if (canImportFlavor && this.currentDestination != destination) {
      if (this.currentDestination != null) {
        this.currentDestination.getDropTarget().removeDropTargetListener(this.destinationDropTargetListener);
      }
      try {
        this.destinationDropTargetListener = new DropTargetAdapter() {
            private boolean acceptedDragAction;
           
            public void drop(DropTargetDropEvent ev) {
              removeDropTargetListener();
            }
            
            @Override
            public void dragEnter(DropTargetDragEvent ev) {
              dropLocation = ev.getLocation();
              SwingUtilities.convertPointToScreen(dropLocation, ev.getDropTargetContext().getComponent());
              Component component = ev.getDropTargetContext().getComponent();
              if (component instanceof JComponent
                  && acceptDropAction(ev.getSourceActions(), ev.getDropAction())) {
                this.acceptedDragAction = true;
                dragEntered((JComponent)component, ev.getTransferable(), ev.getDropAction());
              }
            }
            
            @Override
            public void dragOver(DropTargetDragEvent ev) {
              dropLocation = ev.getLocation();
              SwingUtilities.convertPointToScreen(dropLocation, ev.getDropTargetContext().getComponent());
              Component component = ev.getDropTargetContext().getComponent();
              if (component instanceof JComponent) {
                if (acceptDropAction(ev.getSourceActions(), ev.getDropAction()) ^ this.acceptedDragAction) {
                  // Simulate a drag enter or exit when accept status changes 
                  this.acceptedDragAction = !this.acceptedDragAction;
                  if (this.acceptedDragAction) {
                    dragEntered((JComponent)component, ev.getTransferable(), ev.getDropAction());
                  } else {
                    dragExited((JComponent)component);
                  }
                }
                if (this.acceptedDragAction) {
                  dragMoved((JComponent)component, ev.getTransferable(), ev.getDropAction());
                }
              }
            }
            
            @Override
            public void dragExit(DropTargetEvent ev) {
              removeDropTargetListener();
              Component component = ev.getDropTargetContext().getComponent();
              if (component instanceof JComponent) {
                dragExited((JComponent)component);
              }
            }
            
            private boolean acceptDropAction(int sourceActions, int dropAction) {
              return dropAction != DnDConstants.ACTION_NONE && (sourceActions & dropAction) == dropAction;                  
            }
            
            private void removeDropTargetListener() {
              currentDestination.getDropTarget().removeDropTargetListener(destinationDropTargetListener);
              destinationDropTargetListener = null;
              currentDestination = null;
              acceptedDragAction = false;
              // The drop method of this listener will be invoked after the drop method of 
              // TransferHandler$SwingDropTarget that calls importData method.
              // As drop location is useful only in importData, reseting dropLocation 
              // helps us to track if the future importation will be a drop or a paste
              dropLocation = null;
            }
          };
        destination.getDropTarget().addDropTargetListener(this.destinationDropTargetListener);
        this.currentDestination = destination;
      } catch (TooManyListenersException ex) {
        // Won't happen with default TransferHandler$SwingDropTarget
        throw new RuntimeException("Swing doesn't support multicast on DropTarget anymore!");
      }
    }
    
    return canImportFlavor;
  }

  /**
   * Called once <code>transferable</code> data entered in <code>destination</code> component
   * during a drag and drop operation. Subclasses should override this method if they are
   * interested by this event.  
   * @param dragAction the current drag action (<code>TransferHandler.COPY</code>, <code>TransferHandler.MOVE</code>
   *    or <code>TransferHandler.LINK</code>) 
   */
  protected void dragEntered(JComponent destination, Transferable transferable, int dragAction) {
  }
  
  /**
   * Called when <code>transferable</code> data moved in <code>destination</code> component
   * during a drag and drop operation. Subclasses should override this method if they are
   * interested by this event.  
   * @param dragAction the current drag action (<code>TransferHandler.COPY</code>, <code>TransferHandler.MOVE</code>
   *    or <code>TransferHandler.LINK</code>) 
   */
  protected void dragMoved(JComponent destination, Transferable transferable, int dragAction) {
  }
  
  /**
   * Called once the cursor left <code>destination</code> component during a drag and drop operation. 
   * Subclasses should override this method if they are interested by this event.  
   */
  protected void dragExited(JComponent destination) {
  }
  
  /**
   * Returns <code>true</code> if <code>flavors</code> contains a flavor that 
   * can be imported by this transfer handler. Subclasses should override this
   * method to return which flavors it supports.
   */
  protected abstract boolean canImportFlavor(DataFlavor [] flavors);
  
  /**
   * Returns the location where mouse pointer was dropped in screen coordinates. 
   * @throws IllegalStateException if current operation isn't a drag and drop.
   */
  protected Point getDropLocation() {
    if (this.dropLocation != null) {
      // Return a copy of dropLocation
      return new Point(this.dropLocation);
    } else {
      throw new IllegalStateException("Operation isn't a drag and drop");
    }
  }
  
  /**
   * Returns <code>true</code> if current operation is a drag and drop. 
   */
  protected boolean isDrop() {
    // dropLocation exists only during a drag and drop operation
    return this.dropLocation != null;
  }  

  /**
   * Returns the model contents among files.
   */
  protected List<String> getModelContents(List<File> files, 
                                          ContentManager contentManager) {
    final List<String> importableModels = new ArrayList<String>();        
    for (File file : files) {
      final String absolutePath = file.getAbsolutePath();
      if (contentManager.isAcceptable(absolutePath, ContentManager.ContentType.MODEL)) {
        importableModels.add(absolutePath);
      }        
    }
    return importableModels;
  }
}
