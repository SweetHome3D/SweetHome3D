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

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

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
   * Supported JPEG filter.
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
  private static final String CSV_EXTENSION = ".csv";
  /**
   * Supported CSV filter.
   */
  private static final FileFilter [] CSV_FILTER = {
      new FileFilter() {
        @Override
        public boolean accept(File file) {
          // Accept directories and .csv files
          return file.isDirectory()
              || file.getName().toLowerCase().endsWith(CSV_EXTENSION);
        }
        
        @Override
        public String getDescription() {
          return "CSV - Tab Separated Values";
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
          // Accept directories and .svg files
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
    this.fileFilters.put(ContentType.CSV, CSV_FILTER);
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
    this.defaultFileExtensions.put(ContentType.CSV, CSV_EXTENSION);
    this.defaultFileExtensions.put(ContentType.SVG, SVG_EXTENSION);
    this.defaultFileExtensions.put(ContentType.OBJ, OBJ_EXTENSION);
  }
  
  /**
   * Returns a {@link URLContent URL content} object that references 
   * the given file path.
   */
  public Content getContent(String contentPath) throws RecorderException {
    try {
      return new URLContent(new File(contentPath).toURI().toURL());
    } catch (IOException ex) {
      throw new RecorderException("Couldn't access to content " + contentPath);
    }
  }
  
  /**
   * Returns the file name of the file path in parameter.
   */
  public String getPresentationName(String contentPath, 
                                    ContentType contentType) {
    switch (contentType) {
      case SWEET_HOME_3D :
        return new File(contentPath).getName();
      default :
        String fileName = new File(contentPath).getName();
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
  protected FileFilter [] getFileFilter(ContentType contentPath) {
    if (contentPath == ContentType.USER_DEFINED) {
      throw new IllegalArgumentException("Unknown user defined content type");
    } else {
      return this.fileFilters.get(contentPath);
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
  public boolean isAcceptable(String contentPath, 
                              ContentType contentType) {
    for (FileFilter filter : getFileFilter(contentType)) {
      if (filter.accept(new File(contentPath))) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Returns <code>true</code> if the given content type is for directories.
   */
  protected boolean isDirectory(ContentType contentType) {
    return contentType == ContentType.PHOTOS_DIRECTORY;
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
        && !isDirectory(contentType)) {
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
                               String      path) {
    String defaultExtension = getDefaultFileExtension(contentType);
    if (path != null) {
      // If path has an extension, remove it and build a path that matches contentType
      int extensionIndex = path.lastIndexOf('.');
      if (extensionIndex != -1) {
        path = path.substring(0, extensionIndex);
        if (defaultExtension != null) {
          path += defaultExtension;
        }
      }
    }
    
    String savedPath;
    // Use native file dialog under Mac OS X    
    if (OperatingSystem.isMacOSX()
        && !isDirectory(contentType)) {
      savedPath = showFileDialog(parentView, dialogTitle, contentType, path, true);
    } else {
      savedPath = showFileChooser(parentView, dialogTitle, contentType, path, true);
    }
    
    boolean addedExtension = false;
    if (savedPath != null) {
      if (defaultExtension != null) {
        if (!savedPath.toLowerCase().endsWith(defaultExtension)) {
          savedPath += defaultExtension;
          addedExtension = true;
        }
      }

      // If no extension was added to file under Mac OS X, 
      // FileDialog already asks to user if he wants to overwrite savedName
      if (OperatingSystem.isMacOSX()
          && !addedExtension) {
        return savedPath;
      }
      if (!isDirectory(contentType)) {
        // If the file exists, prompt user if he wants to overwrite it
        File savedFile = new File(savedPath);
        if (savedFile.exists()
            && !confirmOverwrite(parentView, savedFile.getName())) {
          return showSaveDialog(parentView, dialogTitle, contentType, savedPath);
        }
      }
    }
    return savedPath;
  }
  
  /**
   * Displays an AWT open file dialog.
   */
  private String showFileDialog(View               parentView,
                                String             dialogTitle,
                                final ContentType  contentType,
                                String             path, 
                                boolean            save) {
    FileDialog fileDialog = new FileDialog(
        JOptionPane.getFrameForComponent((JComponent)parentView));

    // Set selected file
    if (save && path != null) {
      fileDialog.setFile(new File(path).getName());
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
                                 String        path,
                                 boolean       save) {
    final JFileChooser fileChooser;
    if (isDirectory(contentType)) {
      fileChooser = new DirectoryChooser(this.preferences);
    } else {
      fileChooser = new JFileChooser();
    }
    if (dialogTitle == null) {
      dialogTitle = getFileDialogTitle(save);
    }
    fileChooser.setDialogTitle(dialogTitle);

    // Update directory
    File directory = getLastDirectory(contentType);
    if (directory != null && directory.exists()) {
      fileChooser.setCurrentDirectory(directory);
      if (isDirectory(contentType)) {
        fileChooser.setSelectedFile(directory);
      }
    }    
    // Set selected file
    if (save 
        && path != null
        && (directory == null || !isDirectory(contentType))) {
      fileChooser.setSelectedFile(new File(path));
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
    int option;
    if (isDirectory(contentType)) {
      option = fileChooser.showDialog((JComponent)parentView,
          this.preferences.getLocalizedString(FileContentManager.class, "selectFolderButton.text"));
    } else if (save) {
      option = fileChooser.showSaveDialog((JComponent)parentView);
    } else {
      option = fileChooser.showOpenDialog((JComponent)parentView);
    }    
    if (option == JFileChooser.APPROVE_OPTION) {
      // Retrieve last directory for future calls
      if (isDirectory(contentType)) {
        directory = fileChooser.getSelectedFile();
      } else {
        directory = fileChooser.getCurrentDirectory();
      }
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
   * file <code>path</code> or not.
   * @return <code>true</code> if user confirmed to overwrite.
   */
  protected boolean confirmOverwrite(View parentView, String path) {
    // Retrieve displayed text in buttons and message
    String message = this.preferences.getLocalizedString(FileContentManager.class, "confirmOverwrite.message", path);
    String title = this.preferences.getLocalizedString(FileContentManager.class, "confirmOverwrite.title");
    String replace = this.preferences.getLocalizedString(FileContentManager.class, "confirmOverwrite.overwrite");
    String cancel = this.preferences.getLocalizedString(FileContentManager.class, "confirmOverwrite.cancel");
    
    return JOptionPane.showOptionDialog(SwingUtilities.getRootPane((JComponent)parentView), 
        message, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
        null, new Object [] {replace, cancel}, cancel) == JOptionPane.OK_OPTION;
  }
  
  /**
   * A file chooser dedicated to directory choice.
   */
  private static class DirectoryChooser extends JFileChooser {
    private Executor               fileSystemViewExecutor;
    private JTree                  directoriesTree;
    private TreeSelectionListener  treeSelectionListener;
    private PropertyChangeListener selectedFileListener;
    private Action                 createDirectoryAction;

    public DirectoryChooser(final UserPreferences preferences) {
      setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      this.fileSystemViewExecutor = Executors.newSingleThreadExecutor();
      this.directoriesTree = new JTree(new DefaultTreeModel(new DirectoryNode()));
      this.directoriesTree.setRootVisible(false);
      this.directoriesTree.setEditable(false);
      this.directoriesTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
      this.directoriesTree.setCellRenderer(new DefaultTreeCellRenderer() {
          @Override
          public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                                                        boolean leaf, int row, boolean hasFocus) {
            DirectoryNode node = (DirectoryNode)value;
            File file = (File)node.getUserObject();
            super.getTreeCellRendererComponent(tree, DirectoryChooser.this.getName(file), 
                selected, expanded, leaf, row, hasFocus);
            setIcon(DirectoryChooser.this.getIcon(file));
            if (!node.isWritable()) {
              setForeground(Color.GRAY);
            } 
            return this;
          }
        });
      this.treeSelectionListener = new TreeSelectionListener() {
          public void valueChanged(TreeSelectionEvent ev) {
            TreePath selectionPath = directoriesTree.getSelectionPath();
            if (selectionPath != null) {
              DirectoryNode selectedNode = (DirectoryNode)selectionPath.getLastPathComponent();
              setSelectedFile((File)selectedNode.getUserObject());
            }
          }
        };
      this.directoriesTree.addTreeSelectionListener(this.treeSelectionListener);
      
      this.selectedFileListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            showSelectedFile();
          }
        };
      addPropertyChangeListener(SELECTED_FILE_CHANGED_PROPERTY, this.selectedFileListener);
      
      this.directoriesTree.addTreeExpansionListener(new TreeExpansionListener() {
          public void treeCollapsed(TreeExpansionEvent ev) {
            if (ev.getPath().isDescendant(directoriesTree.getSelectionPath())) {
              // If selected node becomes hidden select not hidden parent
              removePropertyChangeListener(SELECTED_FILE_CHANGED_PROPERTY, selectedFileListener);
              directoriesTree.setSelectionPath(ev.getPath());
              addPropertyChangeListener(SELECTED_FILE_CHANGED_PROPERTY, selectedFileListener);
            }
          }
          
          public void treeExpanded(TreeExpansionEvent ev) {
          }
        });
      
      // Create an action used to create additional directories
      final String newDirectoryText = UIManager.getString("FileChooser.win32.newFolder");
      this.createDirectoryAction = new AbstractAction(newDirectoryText) {
          public void actionPerformed(ActionEvent ev) {
            String newDirectoryNameBase = OperatingSystem.isWindows() || OperatingSystem.isMacOSX()
                ? newDirectoryText
                : UIManager.getString("FileChooser.other.newFolder");
            String newDirectoryName = newDirectoryNameBase;
            // Search a new directory name that doesn't exist
            DirectoryNode parentNode = (DirectoryNode)directoriesTree.getLastSelectedPathComponent();
            File parentDirectory = (File)parentNode.getUserObject();
            for (int i = 2; new File(parentDirectory, newDirectoryName).exists(); i++) {
              newDirectoryName = newDirectoryNameBase;
              if (OperatingSystem.isWindows() || OperatingSystem.isMacOSX()) {
                newDirectoryName += " ";
              }
              newDirectoryName += i;
            }
            newDirectoryName = (String)JOptionPane.showInputDialog(DirectoryChooser.this, 
                preferences.getLocalizedString(FileContentManager.class, "createFolder.message"),                
                newDirectoryText, JOptionPane.QUESTION_MESSAGE, null, null, newDirectoryName);
            if (newDirectoryName != null) {
              File newDirectory = new File(parentDirectory, newDirectoryName);
              if (!newDirectory.mkdir()) {
                String newDirectoryErrorText = UIManager.getString("FileChooser.newFolderErrorText");
                JOptionPane.showMessageDialog(DirectoryChooser.this, 
                    newDirectoryErrorText, newDirectoryErrorText, JOptionPane.ERROR_MESSAGE);
              } else {
                parentNode.updateChildren(parentNode.getChildDirectories());
                ((DefaultTreeModel)directoriesTree.getModel()).nodeStructureChanged(parentNode);
                setSelectedFile(newDirectory);
              }
            }
          }
        };
      
      setSelectedFile(getFileSystemView().getHomeDirectory());
    }
    
    /**
     * Selects the given directory or its parent if it's a file.
     */
    @Override
    public void setSelectedFile(File file) {
      if (file.isFile()) {
        file = file.getParentFile();
      }
      super.setSelectedFile(file);
    }
    
    /**
     * Shows asynchronously the selected file in the directories tree, 
     * filling the parents siblings hierarchy if necessary.
     */
    private void showSelectedFile() {
      final File selectedFile = getSelectedFile();
      if (selectedFile != null) {
        final DirectoryNode rootNode = (DirectoryNode)this.directoriesTree.getModel().getRoot();
        this.fileSystemViewExecutor.execute(new Runnable() {
            public void run() {
              try {
                EventQueue.invokeAndWait(new Runnable() {
                    public void run() {
                      createDirectoryAction.setEnabled(false);
                      if (directoriesTree.isShowing()) {
                        directoriesTree.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                      }
                    }
                  });
                File cononicalFile = selectedFile.getCanonicalFile();
                // Search parents of the selected file
                List<File> parentsAndFile = new ArrayList<File>();
                for (File file = cononicalFile; 
                    file != null;
                    file = getFileSystemView().getParentDirectory(file)) {
                  parentsAndFile.add(0, file);
                }
                // Build path of tree nodes
                final List<DirectoryNode> pathToFileNode = new ArrayList<DirectoryNode>();
                DirectoryNode node = rootNode;
                pathToFileNode.add(node);
                for (final File file : parentsAndFile) {
                  final File [] childDirectories = node.isLoaded() 
                      ? null 
                      : node.getChildDirectories();
                  // Search in a child of the node has a user object equal to file
                  final DirectoryNode currentNode = node;
                  EventQueue.invokeAndWait(new Runnable() {
                      public void run() {
                        if (!currentNode.isLoaded()) {
                          currentNode.updateChildren(childDirectories);
                          ((DefaultTreeModel)directoriesTree.getModel()).nodeStructureChanged(currentNode);
                        }
                        for (int i = 0, n = currentNode.getChildCount(); i < n; i++) {
                          DirectoryNode child = (DirectoryNode)currentNode.getChildAt(i);
                          if (file.equals(child.getUserObject())) {
                            pathToFileNode.add(child);
                            break;
                          }
                        }
                      }
                    });
                  node = pathToFileNode.get(pathToFileNode.size() - 1);
                  if (currentNode == node) {
                    // Give up since file wasn't found
                    break;
                  }
                }
                  
                if (pathToFileNode.size() > 1) {
                  final TreePath path = new TreePath(pathToFileNode.toArray(new TreeNode [pathToFileNode.size()]));
                  EventQueue.invokeAndWait(new Runnable() {
                      public void run() {
                        directoriesTree.removeTreeSelectionListener(treeSelectionListener);
                        directoriesTree.expandPath(path);
                        directoriesTree.setSelectionPath(path);
                        directoriesTree.scrollRowToVisible(directoriesTree.getRowForPath(path));
                        directoriesTree.addTreeSelectionListener(treeSelectionListener);
                      }
                    });                    
                }
                
              } catch (IOException ex) {
                // Ignore directories that can't be found 
              } catch (InterruptedException ex) {
                // Give up if interrupted
              } catch (InvocationTargetException ex) {
                ex.printStackTrace();
              } finally {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                      createDirectoryAction.setEnabled(directoriesTree.getSelectionCount() > 0 
                          && ((DirectoryNode)directoriesTree.getSelectionPath().getLastPathComponent()).isWritable());
                      directoriesTree.setCursor(Cursor.getDefaultCursor());
                    }
                  });
              }
            }
          });
      }
    }

    @Override
    public int showDialog(Component parent, final String approveButtonText) {
      final JButton createDirectoryButton = new JButton(this.createDirectoryAction);
      final JButton approveButton = new JButton(approveButtonText);
      Object cancelOption = UIManager.get("FileChooser.cancelButtonText");
      Object [] options;
      if (OperatingSystem.isMacOSX()) {
        options = new Object [] {approveButton, cancelOption, createDirectoryButton};
      } else {
        options = new Object [] {createDirectoryButton, approveButton, cancelOption};
      }
      // Display chooser in a resizable dialog
      final JOptionPane optionPane = new JOptionPane(SwingTools.createScrollPane(this.directoriesTree), 
          JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, options, approveButton); 
      final JDialog dialog = optionPane.createDialog(SwingUtilities.getRootPane(parent), getDialogTitle());
      dialog.setResizable(true);
      dialog.pack();
      if (this.directoriesTree.getSelectionCount() > 0) {
        this.directoriesTree.scrollPathToVisible(this.directoriesTree.getSelectionPath());
        boolean validDirectory = ((DirectoryNode)this.directoriesTree.getSelectionPath().getLastPathComponent()).isWritable();
        approveButton.setEnabled(validDirectory);
        createDirectoryAction.setEnabled(validDirectory);
      }
      this.directoriesTree.addTreeSelectionListener(new TreeSelectionListener() {
          public void valueChanged(TreeSelectionEvent ev) {
            TreePath selectedPath = ev.getPath();
            boolean validDirectory = selectedPath != null 
                && ((DirectoryNode)ev.getPath().getLastPathComponent()).isWritable();
            approveButton.setEnabled(validDirectory);
            createDirectoryAction.setEnabled(validDirectory); 
          }
        });
      approveButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ev) {
            optionPane.setValue(approveButtonText);
            dialog.setVisible(false);
          }
        });
      dialog.setMinimumSize(dialog.getPreferredSize());
      dialog.setVisible(true);
      dialog.dispose();
      if (approveButtonText.equals(optionPane.getValue())) {
        return JFileChooser.APPROVE_OPTION;
      } else {
        return JFileChooser.CANCEL_OPTION;
      }
    }

    /**
     * Directory nodes which children are loaded when needed. 
     */
    private class DirectoryNode extends DefaultMutableTreeNode {
      private boolean loaded;
      private boolean writable;

      private DirectoryNode() {
        super(null);
      }

      private DirectoryNode(File file) {
        super(file);
        this.writable = file.canWrite();
      }

      public boolean isWritable() {
        return this.writable;
      }
      
      @Override
      public int getChildCount() {
        if (!this.loaded) {
          this.loaded = true;
          return updateChildren(getChildDirectories());
        } else {
          return super.getChildCount();
        }
      }

      public File [] getChildDirectories() {
        File [] childFiles = getUserObject() == null
            ? getFileSystemView().getRoots()
            : getFileSystemView().getFiles((File)getUserObject(), true);
        if (childFiles != null) {
          List<File> childDirectories = new ArrayList<File>(childFiles.length);
          for (File childFile : childFiles) {
            if (isTraversable(childFile)) {
              childDirectories.add(childFile);
            }
          }
          return childDirectories.toArray(new File [childDirectories.size()]);
        } else {
          return new File [0];
        }
      }
      
      public boolean isLoaded() {
        return this.loaded;
      }

      public int updateChildren(File [] childDirectories) {
        if (this.children == null) {
          this.children = new Vector<File>(childDirectories.length); 
        }          
        synchronized (this.children) {
          removeAllChildren();
          for (File childFile : childDirectories) {
            add(new DirectoryNode(childFile));
          }
          return childDirectories.length;
        }
      }
    }
  }
}
