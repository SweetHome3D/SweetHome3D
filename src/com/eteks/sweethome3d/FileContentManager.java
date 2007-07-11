/*
 * FileContentManager.java 4 juil. 07
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
package com.eteks.sweethome3d;

import java.awt.Component;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Window;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.ContentManager;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.tools.TemporaryURLContent;
import com.eteks.sweethome3d.tools.URLContent;

/**
 * Content manager for files with Swing file choosers.
 * @author Emmanuel Puybaret
 */
public class FileContentManager implements ContentManager {
  private static final String SWEET_HOME_3D_EXTENSION = ".sh3d";
  /**
   * Supported Sweet Home 3D file filters.
   */
  private static final FileFilter [] SWEET_HOME_3D_FILTER = {
      new FileFilter() {
        @Override
        public boolean accept(File file) {
          // Accept directories and .sh3d files
          return file.isDirectory()
              || file.getName().toLowerCase().endsWith(SWEET_HOME_3D_EXTENSION);
        }
        
        @Override
        public String getDescription() {
          return "Sweet Home 3D";
        }
      }};
  /**
   * Supported 3D model file filters.
   */
  private static final FileFilter [] MODEL_FILTERS = {
     new FileFilter() {
       @Override
       public boolean accept(File file) {
         // Accept directories and OBJ files
         return file.isDirectory()
                || file.getName().toLowerCase().endsWith(".obj");
       }
   
       @Override
       public String getDescription() {
         return "OBJ";
       }
     },
     new FileFilter() {
       @Override
       public boolean accept(File file) {
         // Accept directories and LWS files
         return file.isDirectory()
                || file.getName().toLowerCase().endsWith(".lws");
       }
   
       @Override
       public String getDescription() {
         return "LWS";
       }
     },
     new FileFilter() {
       @Override
       public boolean accept(File file) {
         // Accept directories and 3DS files
         return file.isDirectory()
                || file.getName().toLowerCase().endsWith(".3ds");
       }
   
       @Override
       public String getDescription() {
         return "3DS";
       }
     }};
  /**
   * Supported image file filters.
   */
  private static final FileFilter [] IMAGE_FILTERS = {
      new FileFilter() {
        @Override
        public boolean accept(File file) {
          // Accept directories and .sh3d files
          return file.isDirectory()
                 || file.getName().toLowerCase().endsWith(".bmp")
                 || file.getName().toLowerCase().endsWith(".wbmp");
        }
    
        @Override
        public String getDescription() {
          return "BMP";
        }
      },
      new FileFilter() {
        @Override
        public boolean accept(File file) {
          // Accept directories and GIF files
          return file.isDirectory()
                 || file.getName().toLowerCase().endsWith(".gif");
        }
    
        @Override
        public String getDescription() {
          return "GIF";
        }
      },
      new FileFilter() {
        @Override
        public boolean accept(File file) {
          // Accept directories and JPEG files
          return file.isDirectory()
                 || file.getName().toLowerCase().endsWith(".jpg")
                 || file.getName().toLowerCase().endsWith(".jpeg");
        }
    
        @Override
        public String getDescription() {
          return "JPEG";
        }
      }, 
      new FileFilter() {
        @Override
        public boolean accept(File file) {
          // Accept directories and PNG files
          return file.isDirectory()
                 || file.getName().toLowerCase().endsWith(".png");
        }
    
        @Override
        public String getDescription() {
          return "PNG";
        }
      }};

  private File                            currentDirectory;
  private Map<ContentType, FileFilter []> fileFilters;

  public FileContentManager() {    
    // Fill file filters map
    this.fileFilters = new HashMap<ContentType, FileFilter[]>();
    this.fileFilters.put(ContentType.SWEET_HOME_3D, SWEET_HOME_3D_FILTER);
    this.fileFilters.put(ContentType.MODEL, MODEL_FILTERS);
    this.fileFilters.put(ContentType.IMAGE, IMAGE_FILTERS);
  }
  
  /**
   * Returns a {@link URLContent URL content} object that references a temporary copy of 
   * the given file path.
   */
  public Content getContent(String contentName) throws RecorderException {
    try {
      return TemporaryURLContent.copyToTemporaryURLContent(
          new URLContent(new File(contentName).toURL()));
    } catch (IOException ex) {
      throw new RecorderException("Couldn't access to content " + contentName);
    }
  }
  
  /**
   * Returns the file name of the file path in parameter.
   */
  public String getPresentationName(String contentName, 
                                    ContentType contentType) {
    switch (contentType) {
      case SWEET_HOME_3D :
        return new File(contentName).getName();
      default :
        String fileName = new File(contentName).getName();
        int pointIndex = fileName.lastIndexOf('.');
        if (pointIndex != -1) {
          fileName = fileName.substring(0, pointIndex);
        }
        return fileName;
    }    
  }
  
  /**
   * Returns <code>true</code> if the file path in parameter is accepted
   * for <code>contentType</code>.
   */
  public boolean isAcceptable(String contentName, 
                              ContentType contentType) {
    for (FileFilter filter : fileFilters.get(contentType)) {
      if (filter.accept(new File(contentName))) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Returns the file path chosen by user with an open file dialog.
   * @return the file path or <code>null</code> if user cancelled its choice.
   */
  public String showOpenDialog(String      dialogTitle,
                               ContentType contentType) {
    // Use native file dialog under Mac OS X
    if (System.getProperty("os.name").startsWith("Mac OS X")) {
      return showFileDialog(dialogTitle, contentType, null, false);
    } else {
      return showFileChooser(dialogTitle, contentType, null, false);
    }
  }
  
  /**
   * Returns the file path chosen by user with a save file dialog.
   * @return the chosen file path or <code>null</code> if user cancelled its choice.
   */
  public String showSaveDialog(String      dialogTitle,
                               ContentType contentType,
                               String      name) {
    String savedName;
    // Use native file dialog under Mac OS X    
    if (System.getProperty("os.name").startsWith("Mac OS X")) {
      savedName = showFileDialog(dialogTitle, contentType, null, true);
    } else {
      savedName = showFileChooser(dialogTitle, contentType, null, true);
    }
    if (contentType == ContentType.SWEET_HOME_3D 
        && savedName != null 
        && !savedName.toLowerCase().endsWith(SWEET_HOME_3D_EXTENSION)) {
      savedName += SWEET_HOME_3D_EXTENSION;
    }
    return savedName;
  }
  
  /**
   * Displays an AWT open file dialog.
   */
  private String showFileDialog(String              dialogTitle,
                                final ContentType   contentType,
                                String              name, 
                                boolean             save) {
    FileDialog fileDialog = 
        new FileDialog(JOptionPane.getFrameForComponent(getActiveParent()));

    // Set selected file
    if (save && name != null) {
      fileDialog.setFile(new File(name).getName());
    }
    
    // Set supported files filter 
    fileDialog.setFilenameFilter(new FilenameFilter() {
        public boolean accept(File dir, String name) {          
          return isAcceptable(new File(dir, name).toString(), contentType);
        }
      });
    // Update current directory
    if (this.currentDirectory != null) {
      fileDialog.setDirectory(this.currentDirectory.toString());
    }
    if (save) {
      fileDialog.setMode(FileDialog.SAVE);
    } else {
      fileDialog.setMode(FileDialog.LOAD);
    }
    fileDialog.setTitle(dialogTitle);
    fileDialog.setVisible(true);
    String selectedFile = fileDialog.getFile();
    // If user choosed a file
    if (selectedFile != null) {
      // Retrieve current directory for future calls
      this.currentDirectory = new File(fileDialog.getDirectory());
      // Return selected file
      return this.currentDirectory + File.separator + selectedFile;
    } else {
      return null;
    }
  }

  /**
   * Displays a Swing open file chooser.
   */
  private String showFileChooser(String        dialogTitle,
                                 ContentType   contentType,
                                 String        name,
                                 boolean       save) {
    JFileChooser fileChooser = new JFileChooser();
    // Set selected file
    if (save && name != null) {
      fileChooser.setSelectedFile(new File(name));
    }    
    // Set supported image  files filter 
    for (FileFilter filter : fileFilters.get(contentType)) {
      fileChooser.addChoosableFileFilter(filter);
    }
    fileChooser.setFileFilter(fileChooser.getAcceptAllFileFilter());
    // Update current directory
    if (this.currentDirectory != null) {
      fileChooser.setCurrentDirectory(this.currentDirectory);
    }
    fileChooser.setDialogTitle(dialogTitle);
    
    int option;
    if (save) {
      option = fileChooser.showSaveDialog(getActiveParent());
    } else {
      option = fileChooser.showOpenDialog(getActiveParent());
    }    
    if (option == JFileChooser.APPROVE_OPTION) {
      // Retrieve current directory for future calls
      this.currentDirectory = fileChooser.getCurrentDirectory();
      // Return selected file
      return fileChooser.getSelectedFile().toString();
    } else {
      return null;
    }
  }
  
  /**
   * Returns the currently active window.
   */
  private Component getActiveParent() {
    for (Frame frame : Frame.getFrames()) {
      if (frame.isActive()) {
        return frame;
      } else for (Component child : frame.getComponents()) {
        if (child instanceof Window
            && ((Window)child).isActive()) {
          return child;
        }
      }
    }
    return null;
  }
}
