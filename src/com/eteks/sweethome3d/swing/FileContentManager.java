/*
 * FileContentManager.java 4 juil. 07
 *
 * Sweet Home 3D, Copyright (c) 2007 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.tools.URLContent;
import com.eteks.sweethome3d.viewcontroller.ContentManager;
import com.eteks.sweethome3d.viewcontroller.View;

/**
 * Content manager for files with Swing file choosers.
 * @author Emmanuel Puybaret
 */
public class FileContentManager implements ContentManager {
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
         return "3DS - 3D Studio";
       }
     },
     new FileFilter() {
       @Override
       public boolean accept(File file) {
         // Accept directories and 3DS files
         return file.isDirectory()
                || file.getName().toLowerCase().endsWith(".dae");
       }
   
       @Override
       public String getDescription() {
         return "DAE - Collada";
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
   * Supported PNG filter.
   */
  private static final FileFilter [] PNG_FILTER = {
      new FileFilter() {
        @Override
        public boolean accept(File file) {
          // Accept directories and .png files
          return file.isDirectory()
              || file.getName().toLowerCase().endsWith(PNG_EXTENSION);
        }
        
        @Override
        public String getDescription() {
          return "PNG";
        }
      }};
  private static final String JPEG_EXTENSION = ".jpg";
  /**
   * Supported OBJ filter.
   */
  private static final FileFilter [] JPEG_FILTER = {
      new FileFilter() {
        @Override
        public boolean accept(File file) {
          // Accept directories and .png files
          return file.isDirectory()
              || file.getName().toLowerCase().endsWith(PNG_EXTENSION)
              || file.getName().toLowerCase().endsWith("jpeg");
        }
        
        @Override
        public String getDescription() {
          return "PNG";
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
      JPEG_FILTER [0], 
      PNG_FILTER [0]};
  private static final String MOV_EXTENSION = ".mov";
  /**
   * Supported MOV filter.
   */
  private static final FileFilter [] MOV_FILTER = {
      new FileFilter() {
        @Override
        public boolean accept(File file) {
          // Accept directories and .mov files
          return file.isDirectory()
              || file.getName().toLowerCase().endsWith(MOV_EXTENSION);
        }
        
        @Override
        public String getDescription() {
          return "MOV";
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
  private static final String SVG_EXTENSION = ".svg";
  /**
   * Supported SVG filter.
   */
  private static final FileFilter [] SVG_FILTER = {
      new FileFilter() {
        @Override
        public boolean accept(File file) {
          // Accept directories and .obj files
          return file.isDirectory()
              || file.getName().toLowerCase().endsWith(SVG_EXTENSION);
        }
        
        @Override
        public String getDescription() {
          return "SVG - Scalable Vector Graphics";
        }
      }};
  
  private final UserPreferences           preferences;
  private final String                    sweetHome3DFileExtension;
  private final String                    languageLibraryFileExtension;
  private final String                    furnitureLibraryFileExtension;
  private final String                    texturesLibraryFileExtension;
  private final String                    pluginFileExtension;
  private Map<ContentType, File>          lastDirectories;
  private Map<ContentType, FileFilter []> fileFilters;
  private Map<ContentType, String>        defaultFileExtensions;

  public FileContentManager(final UserPreferences preferences) {  
    this.preferences = preferences;
    this.sweetHome3DFileExtension = preferences.getLocalizedString(FileContentManager.class, "homeExtension");
    this.languageLibraryFileExtension = preferences.getLocalizedString(FileContentManager.class, "languageLibraryExtension");
    this.furnitureLibraryFileExtension = preferences.getLocalizedString(FileContentManager.class, "furnitureLibraryExtension");
    this.texturesLibraryFileExtension = preferences.getLocalizedString(FileContentManager.class, "texturesLibraryExtension");
    this.pluginFileExtension = preferences.getLocalizedString(FileContentManager.class, "pluginExtension");
    this.lastDirectories = new HashMap<ContentManager.ContentType, File>();
    
    // Fill file filters map
    this.fileFilters = new HashMap<ContentType, FileFilter[]>();
    this.fileFilters.put(ContentType.MODEL, MODEL_FILTERS);
    this.fileFilters.put(ContentType.IMAGE, IMAGE_FILTERS);
    this.fileFilters.put(ContentType.MOV, MOV_FILTER);
    this.fileFilters.put(ContentType.PNG, PNG_FILTER);
    this.fileFilters.put(ContentType.JPEG, JPEG_FILTER);
    this.fileFilters.put(ContentType.PDF, PDF_FILTER);
    this.fileFilters.put(ContentType.SVG, SVG_FILTER);
    this.fileFilters.put(ContentType.OBJ, OBJ_FILTER);
    this.fileFilters.put(ContentType.SWEET_HOME_3D, new FileFilter [] {
        new FileFilter() {
          @Override
          public boolean accept(File file) {
            // Accept directories and .sh3d files
            return file.isDirectory()
                || file.getName().toLowerCase().endsWith(sweetHome3DFileExtension);
          }
          
          @Override
          public String getDescription() {
            return preferences.getLocalizedString(FileContentManager.class, "homeDescription");
          }
        }
      });
    this.fileFilters.put(ContentType.LANGUAGE_LIBRARY, new FileFilter [] {
        new FileFilter() {
          @Override
          public boolean accept(File file) {
            // Accept directories and .sh3f files
            return file.isDirectory()
                || file.getName().toLowerCase().endsWith(languageLibraryFileExtension);
          }
         
          @Override
          public String getDescription() {
            return preferences.getLocalizedString(FileContentManager.class, "languageLibraryDescription");
          }
        }
      });
    this.fileFilters.put(ContentType.FURNITURE_LIBRARY, new FileFilter [] {
        new FileFilter() {
          @Override
          public boolean accept(File file) {
            // Accept directories and .sh3f files
            return file.isDirectory()
                || file.getName().toLowerCase().endsWith(furnitureLibraryFileExtension);
          }
          
          @Override
          public String getDescription() {
            return preferences.getLocalizedString(FileContentManager.class, "furnitureLibraryDescription");
          }
        }
      });
    this.fileFilters.put(ContentType.TEXTURES_LIBRARY, new FileFilter [] {
        new FileFilter() {
          @Override
          public boolean accept(File file) {
            // Accept directories and .sh3f files
            return file.isDirectory()
                || file.getName().toLowerCase().endsWith(texturesLibraryFileExtension);
          }
         
          @Override
          public String getDescription() {
            return preferences.getLocalizedString(FileContentManager.class, "texturesLibraryDescription");
          }
        }
      });
    this.fileFilters.put(ContentType.PLUGIN, new FileFilter [] {
        new FileFilter() {
          @Override
          public boolean accept(File file) {
            // Accept directories and .sh3f files
            return file.isDirectory()
                || file.getName().toLowerCase().endsWith(pluginFileExtension);
          }
         
          @Override
          public String getDescription() {
            return preferences.getLocalizedString(FileContentManager.class, "pluginDescription");
          }
        }
      });
    this.fileFilters.put(ContentType.PHOTOS_DIRECTORY, new FileFilter [] {
        new FileFilter() {
          @Override
          public boolean accept(File file) {
            // Accept directories only
            return file.isDirectory();
          }
         
          @Override
          public String getDescription() {
            return "Photos";
          }
        }
      });

    // Fill file default extension map
    this.defaultFileExtensions = new HashMap<ContentType, String>();
    this.defaultFileExtensions.put(ContentType.SWEET_HOME_3D, sweetHome3DFileExtension);
    this.defaultFileExtensions.put(ContentType.LANGUAGE_LIBRARY, languageLibraryFileExtension);
    this.defaultFileExtensions.put(ContentType.FURNITURE_LIBRARY, furnitureLibraryFileExtension);
    this.defaultFileExtensions.put(ContentType.TEXTURES_LIBRARY, texturesLibraryFileExtension);
    this.defaultFileExtensions.put(ContentType.PLUGIN, pluginFileExtension);
    this.defaultFileExtensions.put(ContentType.PNG, PNG_EXTENSION);
    this.defaultFileExtensions.put(ContentType.JPEG, JPEG_EXTENSION);
    this.defaultFileExtensions.put(ContentType.MOV, MOV_EXTENSION);
    this.defaultFileExtensions.put(ContentType.PDF, PDF_EXTENSION);
    this.defaultFileExtensions.put(ContentType.SVG, SVG_EXTENSION);
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
  public String getDefaultFileExtension(ContentType contentType) {
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
    if (OperatingSystem.isMacOSX()
        && contentType != ContentType.PHOTOS_DIRECTORY) {
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
    if (OperatingSystem.isMacOSX()
        && contentType != ContentType.PHOTOS_DIRECTORY) {
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
      if (contentType != ContentType.PHOTOS_DIRECTORY) {
        // If the file exists, prompt user if he wants to overwrite it
        File savedFile = new File(savedName);
        if (savedFile.exists()
            && !confirmOverwrite(parentView, savedFile.getName())) {
          return showSaveDialog(parentView, dialogTitle, contentType, savedName);
        }
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

      // Update directory
    File directory = getLastDirectory(contentType);
    if (directory != null) {
      fileDialog.setDirectory(directory.toString());
    }
    if (save) {
      fileDialog.setMode(FileDialog.SAVE);
    } else {
      fileDialog.setMode(FileDialog.LOAD);
    }

    if (dialogTitle == null) {
      dialogTitle = getFileDialogTitle(save);
    }
    fileDialog.setTitle(dialogTitle);
    
    fileDialog.setVisible(true);
    
    String selectedFile = fileDialog.getFile();
    // If user chose a file
    if (selectedFile != null) {
      // Retrieve directory for future calls
      directory = new File(fileDialog.getDirectory());
      // Store current directory
      setLastDirectory(contentType, directory);
      // Return selected file
      return directory + File.separator + selectedFile;
    } else {
      return null;
    }
  }

  /**
   * Returns the last directory used for the given content type.
   * @return the last directory for <code>contentType</code> or the default last directory 
   *         if it's not set. If <code>contentType</code> is <code>null</code>, the
   *         returned directory will be the default last one or <code>null</code> if it's not set yet.
   */
  protected File getLastDirectory(ContentType contentType) {
    File directory = this.lastDirectories.get(contentType);
    if (directory == null) {
      directory = this.lastDirectories.get(null);
    }
    return directory;
  }

  /**
   * Stores the last directory for the given content type.
   */
  protected void setLastDirectory(ContentType contentType, File directory) {
    this.lastDirectories.put(contentType, directory);
    // Store default last directory in null content 
    this.lastDirectories.put(null, directory);
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
    if (save 
        && name != null
        && contentType != ContentType.PHOTOS_DIRECTORY) {
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
    if (contentType == ContentType.PHOTOS_DIRECTORY) {
      fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      fileChooser.setAcceptAllFileFilterUsed(false);
    }
    
    // Update directory
    File directory = getLastDirectory(contentType);
    if (directory != null) {
      fileChooser.setCurrentDirectory(directory);
    }
    
    if (dialogTitle == null) {
      dialogTitle = getFileDialogTitle(save);
    }
    fileChooser.setDialogTitle(dialogTitle);
    
    int option;
    if (save) {
      option = fileChooser.showSaveDialog((JComponent)parentView);
    } else {
      option = fileChooser.showOpenDialog((JComponent)parentView);
    }    
    if (option == JFileChooser.APPROVE_OPTION) {
      // Retrieve last directory for future calls
      directory = fileChooser.getCurrentDirectory();
      // Store last directory
      setLastDirectory(contentType, directory);
      // Return selected file
      return fileChooser.getSelectedFile().toString();
    } else {
      return null;
    }
  }

  /**
   * Returns default file dialog title.
   */
  protected String getFileDialogTitle(boolean save) {
    if (save) {
      return this.preferences.getLocalizedString(FileContentManager.class, "saveDialog.title");
    } else {
      return this.preferences.getLocalizedString(FileContentManager.class, "openDialog.title");
    }
  }
    
  /**
   * Displays a dialog that let user choose whether he wants to overwrite 
   * file <code>fileName</code> or not.
   * @return <code>true</code> if user confirmed to overwrite.
   */
  protected boolean confirmOverwrite(View parentView, String fileName) {
    // Retrieve displayed text in buttons and message
    String message = this.preferences.getLocalizedString(FileContentManager.class, "confirmOverwrite.message", fileName);
    String title = this.preferences.getLocalizedString(FileContentManager.class, "confirmOverwrite.title");
    String replace = this.preferences.getLocalizedString(FileContentManager.class, "confirmOverwrite.overwrite");
    String cancel = this.preferences.getLocalizedString(FileContentManager.class, "confirmOverwrite.cancel");
    
    return JOptionPane.showOptionDialog(SwingUtilities.getRootPane((JComponent)parentView), 
        message, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
        null, new Object [] {replace, cancel}, cancel) == JOptionPane.OK_OPTION;
  }
}
