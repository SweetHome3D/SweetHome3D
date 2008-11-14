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
package com.eteks.sweethome3d.swing;

import java.awt.FileDialog;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.tools.URLContent;
import com.eteks.sweethome3d.viewcontroller.ContentManager;
import com.eteks.sweethome3d.viewcontroller.View;

/**
 * Content manager for files with Swing file choosers.
 * @author Emmanuel Puybaret
 */
public class FileContentManager implements ContentManager {
  private static final String SWEET_HOME_3D_EXTENSION;
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
          return ResourceBundle.getBundle(FileContentManager.class.getName()).
              getString("homeDescription");
        }
      }};
  private static final String FURNITURE_LIBRARY_EXTENSION;
  /**
   * Supported Sweet Home 3D file filters.
   */
  private static final FileFilter [] FURNITURE_CATALOG_FILTER = {
      new FileFilter() {
        @Override
        public boolean accept(File file) {
          // Accept directories and .sh3f files
          return file.isDirectory()
              || file.getName().toLowerCase().endsWith(FURNITURE_LIBRARY_EXTENSION);
        }
        
        @Override
        public String getDescription() {
          return ResourceBundle.getBundle(FileContentManager.class.getName()).
              getString("furnitureLibraryDescription");
        }
      }};
  private static final String OBJ_EXTENSION = ".obj";
  /**
   * Supported OBJ filter.
   */
  private static final FileFilter [] OBJ_FILTER = {
      new FileFilter() {
        @Override
        public boolean accept(File file) {
          // Accept directories and .obj files
          return file.isDirectory()
              || file.getName().toLowerCase().endsWith(OBJ_EXTENSION);
        }
        
        @Override
        public String getDescription() {
          return "OBJ - Wavefront";
        }
      }};
  /**
   * Supported 3D model file filters.
   */
  private static final FileFilter [] MODEL_FILTERS = {
     OBJ_FILTER [0],
     new FileFilter() {
       @Override
       public boolean accept(File file) {
         // Accept directories and LWS files
         return file.isDirectory()
                || file.getName().toLowerCase().endsWith(".lws");
       }
   
       @Override
       public String getDescription() {
         return "LWS - LightWave Scene";
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
     },
     new FileFilter() {
       @Override
       public boolean accept(File file) {
         // Accept directories and ZIP files
         return file.isDirectory()
                || file.getName().toLowerCase().endsWith(".zip");
       }
   
       @Override
       public String getDescription() {
         return "ZIP";
       }
     }};
  private static final String PNG_EXTENSION = ".png";
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
                 || file.getName().toLowerCase().endsWith(PNG_EXTENSION);
        }
    
        @Override
        public String getDescription() {
          return "PNG";
        }
      }};
  private static final String PDF_EXTENSION = ".pdf";
  /**
   * Supported PDF filter.
   */
  private static final FileFilter [] PDF_FILTER = {
      new FileFilter() {
        @Override
        public boolean accept(File file) {
          // Accept directories and .pdf files
          return file.isDirectory()
              || file.getName().toLowerCase().endsWith(PDF_EXTENSION);
        }
        
        @Override
        public String getDescription() {
          return "PDF";
        }
      }};

  
  static {
    // Retrieve application document file extensions
    ResourceBundle resource = ResourceBundle.getBundle(FileContentManager.class.getName());
    SWEET_HOME_3D_EXTENSION = resource.getString("homeExtension");
    FURNITURE_LIBRARY_EXTENSION = resource.getString("furnitureLibraryExtension");
  }
  
  private File                            currentDirectory;
  private Map<ContentType, FileFilter []> fileFilters;
  private Map<ContentType, String>        defaultFileExtensions;

  public FileContentManager() {  
    // Fill file filters map
    this.fileFilters = new HashMap<ContentType, FileFilter[]>();
    this.fileFilters.put(ContentType.SWEET_HOME_3D, SWEET_HOME_3D_FILTER);
    this.fileFilters.put(ContentType.FURNITURE_LIBRARY, FURNITURE_CATALOG_FILTER);
    this.fileFilters.put(ContentType.MODEL, MODEL_FILTERS);
    this.fileFilters.put(ContentType.IMAGE, IMAGE_FILTERS);
    this.fileFilters.put(ContentType.PDF, PDF_FILTER);
    this.fileFilters.put(ContentType.OBJ, OBJ_FILTER);
    // Fill file default extension map
    this.defaultFileExtensions = new HashMap<ContentType, String>();
    this.defaultFileExtensions.put(ContentType.SWEET_HOME_3D, SWEET_HOME_3D_EXTENSION);
    this.defaultFileExtensions.put(ContentType.FURNITURE_LIBRARY, FURNITURE_LIBRARY_EXTENSION);
    this.defaultFileExtensions.put(ContentType.PDF, PDF_EXTENSION);
    this.defaultFileExtensions.put(ContentType.OBJ, OBJ_EXTENSION);
  }
  
  /**
   * Returns a {@link URLContent URL content} object that references 
   * the given file path.
   */
  public Content getContent(String contentName) throws RecorderException {
    try {
      return new URLContent(new File(contentName).toURI().toURL());
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
   * Returns the file filters available for a given content type.
   * This method may be overridden to add some file filters to existing content types
   * or to define the filters of a user defined content type.
   */
  protected FileFilter [] getFileFilter(ContentType contentType) {
    if (contentType == ContentType.USER_DEFINED) {
      throw new IllegalArgumentException("Unknown user defined content type");
    } else {
      return this.fileFilters.get(contentType);
    }
  }
  
  /**
   * Returns the default file extension of a given content type. 
   * If not <code>null</code> this extension will be appended automatically 
   * to the file name chosen by user in save dialog.
   * This method may be overridden to change the default file extension of an existing content type
   * or to define the default file extension of a user defined content type.
   */
  protected String getDefaultFileExtension(ContentType contentType) {
    if (contentType == ContentType.USER_DEFINED) {
      return null;
    } else {
      return this.defaultFileExtensions.get(contentType);
    }
  }
  
  /**
   * Returns <code>true</code> if the file path in parameter is accepted
   * for <code>contentType</code>.
   */
  public boolean isAcceptable(String contentName, 
                              ContentType contentType) {
    for (FileFilter filter : getFileFilter(contentType)) {
      if (filter.accept(new File(contentName))) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Returns the file path chosen by user with an open file dialog.
   * @return the file path or <code>null</code> if user canceled its choice.
   */
  public String showOpenDialog(View        parentView,
                               String      dialogTitle,
                               ContentType contentType) {
    // Use native file dialog under Mac OS X
    if (OperatingSystem.isMacOSX()) {
      return showFileDialog(parentView, dialogTitle, contentType, null, false);
    } else {
      return showFileChooser(parentView, dialogTitle, contentType, null, false);
    }
  }
  
  /**
   * Returns the file path chosen by user with a save file dialog.
   * If this file already exists, the user will be prompted whether 
   * he wants to overwrite this existing file. 
   * @return the chosen file path or <code>null</code> if user canceled its choice.
   */
  public String showSaveDialog(View        parentView,
                               String      dialogTitle,
                               ContentType contentType,
                               String      name) {
    String defaultExtension = getDefaultFileExtension(contentType);
    if (name != null) {
      // If name has an extension, remove it and build a name that matches contentType
      int extensionIndex = name.lastIndexOf('.');
      if (extensionIndex != -1) {
        name = name.substring(0, extensionIndex);
        if (defaultExtension != null) {
          name += defaultExtension;
        }
      }
    }
    
    String savedName;
    // Use native file dialog under Mac OS X    
    if (OperatingSystem.isMacOSX()) {
      savedName = showFileDialog(parentView, dialogTitle, contentType, name, true);
    } else {
      savedName = showFileChooser(parentView, dialogTitle, contentType, name, true);
    }
    
    boolean addedExtension = false;
    if (savedName != null) {
      if (defaultExtension != null) {
        if (!savedName.toLowerCase().endsWith(defaultExtension)) {
          savedName += defaultExtension;
          addedExtension = true;
        }
      }

      // If no extension was added to file under Mac OS X, 
      // FileDialog already asks to user if he wants to overwrite savedName
      if (OperatingSystem.isMacOSX()
          && !addedExtension) {
        return savedName;
      }
      // If the file exists, prompt user if he wants to overwrite it
      File savedFile = new File(savedName);
      if (savedFile.exists()
          && !confirmOverwrite(parentView, savedFile.getName())) {
        return showSaveDialog(parentView, dialogTitle, contentType, savedName);
      }
    }
    return savedName;
  }
  
  /**
   * Displays an AWT open file dialog.
   */
  private String showFileDialog(View               parentView,
                                String             dialogTitle,
                                final ContentType  contentType,
                                String             name, 
                                boolean            save) {
    FileDialog fileDialog = new FileDialog(
        JOptionPane.getFrameForComponent((JComponent)parentView));

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

    if (dialogTitle == null) {
      dialogTitle = getDefaultFileDialogTitle(save);
    }
    fileDialog.setTitle(dialogTitle);
    
    fileDialog.setVisible(true);
    String selectedFile = fileDialog.getFile();
    // If user chose a file
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
  private String showFileChooser(View          parentView,
                                 String        dialogTitle,
                                 ContentType   contentType,
                                 String        name,
                                 boolean       save) {
    JFileChooser fileChooser = new JFileChooser();
    // Set selected file
    if (save && name != null) {
      fileChooser.setSelectedFile(new File(name));
    }    
    // Set supported files filter 
    FileFilter acceptAllFileFilter = fileChooser.getAcceptAllFileFilter();
    fileChooser.addChoosableFileFilter(acceptAllFileFilter);
    FileFilter [] contentFileFilters = getFileFilter(contentType);
    for (FileFilter filter : contentFileFilters) {
      fileChooser.addChoosableFileFilter(filter);
    }
    // If there's only one file filter, select it 
    if (contentFileFilters.length == 1) {
      fileChooser.setFileFilter(contentFileFilters [0]);
    } else {
      fileChooser.setFileFilter(acceptAllFileFilter);
    }
    
    // Update current directory
    if (this.currentDirectory != null) {
      fileChooser.setCurrentDirectory(this.currentDirectory);
    }
    
    if (dialogTitle == null) {
      dialogTitle = getDefaultFileDialogTitle(save);
    }
    fileChooser.setDialogTitle(dialogTitle);
    
    int option;
    if (save) {
      option = fileChooser.showSaveDialog((JComponent)parentView);
    } else {
      option = fileChooser.showOpenDialog((JComponent)parentView);
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
   * Returns default file dialog title.
   */
  private String getDefaultFileDialogTitle(boolean save) {
    ResourceBundle resource = ResourceBundle.getBundle(FileContentManager.class.getName());
    if (save) {
      return resource.getString("saveDialog.title");
    } else {
      return resource.getString("openDialog.title");
    }
  }
  
  /**
   * Displays a dialog that let user choose whether he wants to overwrite 
   * file <code>fileName</code> or not.
   * @return <code>true</code> if user confirmed to overwrite.
   */
  protected boolean confirmOverwrite(View parentView, String fileName) {
    // Retrieve displayed text in buttons and message
    ResourceBundle resource = ResourceBundle.getBundle(FileContentManager.class.getName());
    String messageFormat = resource.getString("confirmOverwrite.message");
    String message = String.format(messageFormat, fileName);
    String title = resource.getString("confirmOverwrite.title");
    String replace = resource.getString("confirmOverwrite.overwrite");
    String cancel = resource.getString("confirmOverwrite.cancel");
    
    return JOptionPane.showOptionDialog((JComponent)parentView, 
        message, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
        null, new Object [] {replace, cancel}, cancel) == JOptionPane.OK_OPTION;
  }
}
