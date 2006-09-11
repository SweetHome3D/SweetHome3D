/*
 * DropImageFileTest.java 10 sept 2006
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
package com.eteks.sweethome3d.test;

import java.awt.BorderLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.TransferHandler;

import com.eteks.sweethome3d.swing.IconManager;
import com.eteks.sweethome3d.tools.URLContent;

/**
 * Tests the drop of an image on a label displayed in a frame.
 * @author Emmanuel Puybaret
 */
public class DropImageFileTest {
  public static void main(String [] args) {
    // Create a label able to receive an image file 
    // from a drop operation 
    JLabel label = new JLabel("Drop a file here", JLabel.CENTER);
    label.setTransferHandler(new LabelIconTransferHandler());
    // View this label in a frame
    JFrame frame = new JFrame("Image preview");
    frame.add(label);
    frame.setSize(200, 200);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);
  }
  
  /**
   * A transfer handler that changes the image of a label 
   * with the file received from a drop operation.
   */
  private static class LabelIconTransferHandler extends TransferHandler {
    public boolean canImport(JComponent destinationComponent, 
                             DataFlavor [] flavors) {
      // This handler accepts only a list of files dropped on a label
      return destinationComponent instanceof JLabel
             && Arrays.asList(flavors).contains(DataFlavor.javaFileListFlavor); 
    }

    public boolean importData(JComponent destinationComponent, 
                              Transferable transferedFiles) {
      try {
        // Get the transfered files
        List<File> files = (List<File>)transferedFiles.
            getTransferData(DataFlavor.javaFileListFlavor);
        // Get an icon from the content of the first file
        URLContent imageContent = new URLContent(files.get(0).toURL());
        Icon icon = IconManager.getInstance().getIcon(imageContent, 128, destinationComponent);
        // Update label icon
        JLabel label = (JLabel)destinationComponent;
        label.setIcon(icon);
        label.setText("");
        return true;
      } catch (UnsupportedFlavorException ex) {
        return false;
      } catch (IOException ex) {
        return false;
      }
    }
  }  
}
