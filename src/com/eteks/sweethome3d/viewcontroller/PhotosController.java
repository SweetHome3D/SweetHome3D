/*
 * PhotosController.java 5 nov 2012
 *
 * Sweet Home 3D, Copyright (c) 2012 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
package com.eteks.sweethome3d.viewcontroller;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.eteks.sweethome3d.model.Camera;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * The controller of multiple photos creation view.
 * @author Emmanuel Puybaret
 * @since 4.0
 */
public class PhotosController extends AbstractPhotoController {
  /**
   * The properties that may be edited by the view associated to this controller. 
   */
  public enum Property {CAMERAS, SELECTED_CAMERAS, FILE_FORMAT, FILE_COMPRESSION_QUALITY}
  
  private final Home                  home;
  private final UserPreferences       preferences;
  private final ViewFactory           viewFactory;
  private final PropertyChangeSupport propertyChangeSupport;
  private DialogView                  photoView;
  
  private List<Camera> cameras;
  private List<Camera> selectedCameras;
  private String       fileFormat;
  private Float        fileCompressionQuality;

  public PhotosController(Home home, UserPreferences preferences, View view3D, 
                          ViewFactory viewFactory, ContentManager contentManager) {
    super(home, preferences, view3D, contentManager);
    this.home = home;
    this.preferences = preferences;
    this.viewFactory = viewFactory;
    this.propertyChangeSupport = new PropertyChangeSupport(this);
    
    this.cameras = Collections.emptyList();
    this.selectedCameras = Collections.emptyList();
    
    home.addPropertyChangeListener(Home.Property.STORED_CAMERAS, new HomeStoredCamerasChangeListener(this));
    updateProperties();
  }

  /**
   * Home cameras listener that updates properties when home cameras change. This listener is bound to this controller 
   * with a weak reference to avoid strong link between home and this controller.  
   */
  private static class HomeStoredCamerasChangeListener implements PropertyChangeListener {
    private WeakReference<PhotosController> photosController;
    
    public HomeStoredCamerasChangeListener(PhotosController photoController) {
      this.photosController = new WeakReference<PhotosController>(photoController);
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
      // If controller was garbage collected, remove this listener from home
      final AbstractPhotoController controller = this.photosController.get();
      if (controller == null) {
        ((Home)ev.getSource()).removePropertyChangeListener(Home.Property.STORED_CAMERAS, this);
      } else {
        controller.updateProperties();
      }
    }
  }

  /**
   * Returns the view associated with this controller.
   */
  public DialogView getView() {
    // Create view lazily only once it's needed
    if (this.photoView == null) {
      this.photoView = this.viewFactory.createPhotosView(this.home, this.preferences, this);
    }
    return this.photoView;
  }

  /**
   * Displays the view controlled by this controller.
   */
  public void displayView(View parentView) {
    getView().displayView(parentView);
  }

  /**
   * Adds the property change <code>listener</code> in parameter to this controller.
   */
  public void addPropertyChangeListener(Property property, PropertyChangeListener listener) {
    this.propertyChangeSupport.addPropertyChangeListener(property.name(), listener);
  }

  /**
   * Removes the property change <code>listener</code> in parameter from this controller.
   */
  public void removePropertyChangeListener(Property property, PropertyChangeListener listener) {
    this.propertyChangeSupport.removePropertyChangeListener(property.name(), listener);
  }

  /**
   * Updates edited properties from the photo creation preferences.
   */
  protected void updateProperties() {
    // Update properties only once this object is initialized
    if (this.home != null) {
      super.updateProperties();
      setCameras(this.home.getStoredCameras());
      setSelectedCameras(this.home.getStoredCameras());
    }    
  }

  /**
   * Returns the cameras available to create photos.
   */
  public List<Camera> getCameras() {
    return this.cameras;
  }

  /**
   * Sets the selected cameras to create photos.
   */
  private void setCameras(List<Camera> cameras) {
    if (!cameras.equals(this.cameras)) {
      List<Camera> oldCameras = this.cameras;
      this.cameras = new ArrayList<Camera>(cameras);
      this.propertyChangeSupport.firePropertyChange(
          Property.CAMERAS.name(), Collections.unmodifiableList(oldCameras), Collections.unmodifiableList(cameras));
    }
  }

  /**
   * Returns the selected cameras to create photos.
   */
  public List<Camera> getSelectedCameras() {
    return this.selectedCameras;
  }

  /**
   * Sets the selected cameras to create photos.
   */
  public void setSelectedCameras(List<Camera> selectedCameras) {
    if (!selectedCameras.equals(this.selectedCameras)) {
      List<Camera> oldSelectedCameras = this.selectedCameras;
      this.selectedCameras = new ArrayList<Camera>(selectedCameras);
      this.propertyChangeSupport.firePropertyChange(
          Property.SELECTED_CAMERAS.name(), Collections.unmodifiableList(oldSelectedCameras), Collections.unmodifiableList(selectedCameras));
    }
  }

  /**
   * Returns the format used to save image files.
   */
  public String getFileFormat() {
    return this.fileFormat;
  }

  /**
   * Sets the format used to save image files.
   */
  public void setFileFormat(String fileFormat) {
    if (fileFormat != this.fileFormat
        || (fileFormat != null && !fileFormat.equals(this.fileFormat))) {
      String oldFileFormat = this.fileFormat;
      this.fileFormat = fileFormat;
      this.propertyChangeSupport.firePropertyChange(Property.FILE_FORMAT.name(), oldFileFormat, fileFormat);
    }
  }
  
  
  /**
   * Returns the compression quality used to save image files.
   */
  public Float getFileCompressionQuality() {
    return this.fileCompressionQuality;
  }

  /**
   * Sets the compression quality used to save image files.
   */
  public void setFileCompressionQuality(Float fileCompressionQuality) {
    if (fileCompressionQuality != this.fileCompressionQuality
        || (fileCompressionQuality != null && !fileCompressionQuality.equals(this.fileCompressionQuality))) {
      Float oldFileCompressionQuality = this.fileCompressionQuality;
      this.fileCompressionQuality = fileCompressionQuality;
      this.propertyChangeSupport.firePropertyChange(Property.FILE_COMPRESSION_QUALITY.name(), oldFileCompressionQuality, fileCompressionQuality);
    }
  }
}
