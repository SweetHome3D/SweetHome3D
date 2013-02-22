/*
 * PhotoController.java 5 mai 2009
 *
 * Sweet Home 3D, Copyright (c) 2009 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import com.eteks.sweethome3d.model.AspectRatio;
import com.eteks.sweethome3d.model.Camera;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * The controller of the photo creation view.
 * @author Emmanuel Puybaret
 */
public class PhotoController extends AbstractPhotoController {
  /**
   * The properties that may be edited by the view associated to this controller. 
   */
  public enum Property {TIME, LENS}
  
  private final Home                  home;
  private final UserPreferences       preferences;
  private final ViewFactory           viewFactory;
  private final PropertyChangeSupport propertyChangeSupport;
  private final CameraChangeListener  cameraChangeListener;
  private DialogView                  photoView;
  
  private long                        time;
  private Camera.Lens                 lens;  

  public PhotoController(Home home,
                         UserPreferences preferences, 
                         View view3D, ViewFactory viewFactory,
                         ContentManager contentManager) {
    super(home, preferences, view3D, contentManager);
    this.home = home;
    this.preferences = preferences;
    this.viewFactory = viewFactory;
    this.propertyChangeSupport = new PropertyChangeSupport(this);
    
    this.cameraChangeListener = new CameraChangeListener(this);
    home.getCamera().addPropertyChangeListener(this.cameraChangeListener);
    home.addPropertyChangeListener(Home.Property.CAMERA, new HomeCameraChangeListener(this));
    updateProperties();
  }

  /**
   * Home camera listener that updates properties when home camera changes. This listener is bound to this controller 
   * with a weak reference to avoid strong link between home and this controller.  
   */
  private static class HomeCameraChangeListener implements PropertyChangeListener {
    private WeakReference<PhotoController> photoController;
    
    public HomeCameraChangeListener(PhotoController photoController) {
      this.photoController = new WeakReference<PhotoController>(photoController);
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
      // If controller was garbage collected, remove this listener from home
      final PhotoController controller = this.photoController.get();
      if (controller == null) {
        ((Home)ev.getSource()).removePropertyChangeListener(Home.Property.CAMERA, this);
      } else {
        ((Camera)ev.getOldValue()).removePropertyChangeListener(controller.cameraChangeListener);
        controller.updateProperties();
        ((Camera)ev.getNewValue()).addPropertyChangeListener(controller.cameraChangeListener);
      }
    }
  }

  /**
   * Camera listener that updates properties when camera changes. This listener is bound to this controller 
   * with a weak reference to avoid strong link between home and this controller.  
   */
  private static class CameraChangeListener implements PropertyChangeListener {
    private WeakReference<AbstractPhotoController> photoController;
    
    public CameraChangeListener(AbstractPhotoController photoController) {
      this.photoController = new WeakReference<AbstractPhotoController>(photoController);
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
      // If controller was garbage collected, remove this listener from camera
      final AbstractPhotoController controller = this.photoController.get();
      if (controller == null) {
        ((Camera)ev.getSource()).removePropertyChangeListener(this);
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
      this.photoView = this.viewFactory.createPhotoView(this.home, this.preferences, this);
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
      setTime(this.home.getCamera().getTime());
      setLens(this.home.getCamera().getLens());
    }
  }
  
  /**
   * Sets the edited time in UTC time zone.
   */
  public void setTime(long time) {
    if (this.time != time) {
      long oldTime = this.time;
      this.time = time;
      this.propertyChangeSupport.firePropertyChange(Property.TIME.name(), oldTime, time);
      Camera homeCamera = this.home.getCamera();
      homeCamera.removePropertyChangeListener(this.cameraChangeListener);
      homeCamera.setTime(time);
      homeCamera.addPropertyChangeListener(this.cameraChangeListener);
    }
  }
  
  /**
   * Returns the edited time in UTC time zone.
   */
  public long getTime() {
    return this.time;
  }

  /**
   * Sets the edited camera lens.
   */
  public void setLens(Camera.Lens lens) {
    if (this.lens != lens) {
      Camera.Lens oldLens = this.lens;
      this.lens = lens;
      this.propertyChangeSupport.firePropertyChange(Property.LENS.name(), oldLens, lens);
      if (lens == Camera.Lens.SPHERICAL) {
        setAspectRatio(AspectRatio.RATIO_2_1);
      } else if (lens == Camera.Lens.FISHEYE) {
        setAspectRatio(AspectRatio.SQUARE_RATIO);
      }  
      this.home.getCamera().setLens(this.lens);
    }
  }
  
  /**
   * Returns the edited camera lens.
   */
  public Camera.Lens getLens() {    
    return this.lens;
  }
}
