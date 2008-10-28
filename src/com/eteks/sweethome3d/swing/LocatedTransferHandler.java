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
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.TransferHandler;

import com.eteks.sweethome3d.viewcontroller.ContentManager;


/**
 * Transfer handler that stores the dropped location of mouse pointer.
 * @author Emmanuel Puybaret
 */
public abstract class LocatedTransferHandler extends TransferHandler {
  private static Point dropLocation;
  
  static {
    // Install a drag source listener that tracks the location of mouse moves
    // and erase that location once drop is terminated
    // 
    DragSourceAdapter listener = new DragSourceAdapter() {
      @Override
      public void dragMouseMoved(DragSourceDragEvent ev) {
        dropLocation = ev.getLocation();
      }
      
      @Override
      public void dragDropEnd(DragSourceDropEvent ev) {
        // Erase dropLocation once drop is terminated 
        // to detect further paste operation
        // As this listener will called after TransferHandler own listener
        // dropLocation will be available in importData and exportDone method
        dropLocation = null;
      }
    };
    DragSource dragSource = DragSource.getDefaultDragSource();
    dragSource.addDragSourceMotionListener(listener);
    dragSource.addDragSourceListener(listener);
  }

  /**
   * Returns the location where mouse pointer was dropped in screen coordinates. 
   * @throws IllegalStateException if current operation isn't a drag and drop.
   */
  protected Point getDropLocation() {
    if (dropLocation != null) {
      // Return a copy of dropLocation
      return new Point(dropLocation);
    } else {
      throw new IllegalStateException("Operation isn't a drag and drop");
    }
  }
  
  /**
   * Returns <code>true</code> if current operation is a drag and drop. 
   */
  protected boolean isDrop() {
    return dropLocation != null;
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
