/*
 * FileUtilities.java 4 juil. 07
 *
 * Copyright (c) 2007 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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
import java.awt.FileDialog;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.tools.URLContent;

/**
 * Tools for files.
 * @author Emmanuel Puybaret
 */
public class FileUtilities {
  private static File currentDirectory;

  private FileUtilities() {
    //  This class isn't instatiable and contains only static methods
  }
  
  /**
   * Returns a {@link URLContent URL content} object that references a temporary copy of 
   * a given <code>file</code>.
   */
  public static URLContent copyToTempFile(String file) throws IOException {
    return copyToTempFile(new URLContent(new File(file).toURL()));
  }
  
  /**
   * Returns a {@link URLContent URL content} object that references a temporary copy of 
   * a given <code>content</code>.
   */
  public static URLContent copyToTempFile(Content content) throws IOException {
    File tempFile = File.createTempFile("urlContent", "tmp");
    tempFile.deleteOnExit();
    InputStream tempIn = null;
    OutputStream tempOut = null;
    try {
      tempIn = content.openStream();
      tempOut = new FileOutputStream(tempFile);
      byte [] buffer = new byte [8096];
      int size; 
      while ((size = tempIn.read(buffer)) != -1) {
        tempOut.write(buffer, 0, size);
      }
    } finally {
      if (tempIn != null) {
        tempIn.close();
      }
      if (tempOut != null) {
        tempOut.close();
      }
    }
    return new URLContent(tempFile.toURL());
  }
  
  /**
   * Returns a file chosen by user with an open file dialog.
   * @return the chosen file or <code>null</code> if user cancelled its choice.
   */
  public static String showOpenFileDialog(Component parent,
                                          String    dialogTitle,
                                          FileFilter [] fileFilters) {
    // Use native file dialog under Mac OS X
    if (System.getProperty("os.name").startsWith("Mac OS X")) {
      return showFileDialog(parent, dialogTitle, fileFilters);
    } else {
      return showFileChooser(parent, dialogTitle, fileFilters);
    }
  }
  
  /**
   * Displays an AWT open file dialog.
   */
  private static String showFileDialog(Component  parent,
                                       String     dialogTitle,
                                       final FileFilter [] fileFilters) {
    FileDialog fileDialog = 
      new FileDialog(JOptionPane.getFrameForComponent(parent));

    // Set supported image files filter 
    fileDialog.setFilenameFilter(new FilenameFilter() {
        public boolean accept(File dir, String name) {
          for (FileFilter filter : fileFilters) {
            if (filter.accept(new File(name))) {
              return true;
            }
          }
          return false;
        }
      });
    // Update current directory
    if (currentDirectory != null) {
      fileDialog.setDirectory(currentDirectory.toString());
    }
    fileDialog.setMode(FileDialog.LOAD);
    fileDialog.setTitle(dialogTitle);
    fileDialog.setVisible(true);
    String selectedFile = fileDialog.getFile();
    // If user choosed a file
    if (selectedFile != null) {
      // Retrieve current directory for future calls
      currentDirectory = new File(fileDialog.getDirectory());
      // Return selected file
      return currentDirectory + File.separator + selectedFile;
    } else {
      return null;
    }
  }

  /**
   * Displays a Swing open file chooser.
   */
  private static String showFileChooser(Component     parent,
                                        String        dialogTitle,
                                        FileFilter [] fileFilters) {
    JFileChooser fileChooser = new JFileChooser();
    // Set supported image  files filter 
    for (FileFilter filter : fileFilters) {
      fileChooser.addChoosableFileFilter(filter);
    }
    fileChooser.setFileFilter(fileChooser.getAcceptAllFileFilter());
    // Update current directory
    if (currentDirectory != null) {
      fileChooser.setCurrentDirectory(currentDirectory);
    }
    fileChooser.setDialogTitle(dialogTitle);
    int option = fileChooser.showOpenDialog(parent);
    if (option == JFileChooser.APPROVE_OPTION) {
      // Retrieve current directory for future calls
      currentDirectory = fileChooser.getCurrentDirectory();
      // Return selected file
      return fileChooser.getSelectedFile().toString();
    } else {
      return null;
    }
  }
}
