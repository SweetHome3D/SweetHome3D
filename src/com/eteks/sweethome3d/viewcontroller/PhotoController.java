/*
 * PhotoController.java 5 mai 2009
 *
 * Copyright (c) 2009 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.eteks.sweethome3d.model.AspectRatio;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeEnvironment;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * The controller of the photo creation view.
 * @author Emmanuel Puybaret
 */
public class PhotoController implements Controller {
  /**
   * The properties that may be edited by the view associated to this controller. 
   */
  public enum Property {ASPECT_RATIO, WIDTH, HEIGHT, QUALITY, VIEW_3D_ASPECT_RATIO}
  
  private final Home                  home;
  private final UserPreferences       preferences;
  private final View                  view3D;
  private final ViewFactory           viewFactory;
  private final ContentManager        contentManager;
  private final PropertyChangeSupport propertyChangeSupport;
  private DialogView                  photoView;
  
  private AspectRatio                 aspectRatio;
  private int                         width;
  private int                         height;
  private int                         quality;
  private float                       view3DAspectRatio;

  public PhotoController(Home home,
                         UserPreferences preferences, 
                         View view3D, ViewFactory viewFactory,
                         ContentManager contentManager) {
    this.home = home;
    this.preferences = preferences;
    this.view3D = view3D;
    this.viewFactory = viewFactory;
    this.contentManager = contentManager;
    this.propertyChangeSupport = new PropertyChangeSupport(this);
    
    updateProperties();
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
   * Returns the content manager of this controller.
   */
  public ContentManager getContentManager() {
    return this.contentManager;
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
    HomeEnvironment homeEnvironment = this.home.getEnvironment();
    setAspectRatio(homeEnvironment.getPhotoAspectRatio());
    setWidth(homeEnvironment.getPhotoWidth(), false);
    setHeight(homeEnvironment.getPhotoHeight(), false);
    setQuality(homeEnvironment.getPhotoQuality());
    this.view3DAspectRatio = 1;
  }
  
  /**
   * Sets the aspect ratio of the photo.
   */
  public void setAspectRatio(AspectRatio aspectRatio) {
    if (this.aspectRatio != aspectRatio) {
      AspectRatio oldAspectRatio = this.aspectRatio;
      this.aspectRatio = aspectRatio;
      this.propertyChangeSupport.firePropertyChange(Property.ASPECT_RATIO.name(), oldAspectRatio, aspectRatio);
      this.home.getEnvironment().setPhotoAspectRatio(this.aspectRatio);
      if (this.aspectRatio == AspectRatio.VIEW_3D_RATIO) {
        setHeight(Math.round(width / this.view3DAspectRatio), false);
      } else if (this.aspectRatio.getValue() != null) {
        setHeight(Math.round(width / this.aspectRatio.getValue()), false);
      }
    }
  }
  
  /**
   * Returns the aspect ratio of the photo.
   */
  public AspectRatio getAspectRatio() {
    return this.aspectRatio;
  }

  /**
   * Sets the width of the photo.
   */
  public void setWidth(int width) {
    setWidth(width, true);
  }
  
  private void setWidth(int width, boolean updateHeight) {
    if (this.width != width) {
      int oldWidth = this.width;
      this.width = width;
      this.propertyChangeSupport.firePropertyChange(Property.WIDTH.name(), oldWidth, width);
      if (updateHeight) {
        if (this.aspectRatio == AspectRatio.VIEW_3D_RATIO) {
          setHeight(Math.round(width / this.view3DAspectRatio), false);
        } else if (this.aspectRatio.getValue() != null) {
          setHeight(Math.round(width / this.aspectRatio.getValue()), false);
        }
      }
      this.home.getEnvironment().setPhotoWidth(this.width);
    }
  }
  
  /**
   * Returns the width of the photo.
   */
  public int getWidth() {
    return this.width;
  }

  /**
   * Sets the height of the photo.
   */
  public void setHeight(int height) {
    setHeight(height, true);
  }
  
  private void setHeight(int height, boolean updateWidth) {
    if (this.height != height) {
      int oldHeight = this.height;
      this.height = height;
      this.propertyChangeSupport.firePropertyChange(Property.HEIGHT.name(), oldHeight, height);
      if (updateWidth) {
        if (this.aspectRatio == AspectRatio.VIEW_3D_RATIO) {
          setWidth(Math.round(height * this.view3DAspectRatio), false);
        } else if (this.aspectRatio.getValue() != null) {
          setWidth(Math.round(height * this.aspectRatio.getValue()), false);
        }
      }
      this.home.getEnvironment().setPhotoHeight(this.height);
    }
  }
  
  /**
   * Returns the height of the photo.
   */
  public int getHeight() {
    return this.height;
  }

  /**
   * Sets the rendering quality of the photo.
   */
  public void setQuality(int quality) {
    if (this.quality != quality) {
      int oldQuality = this.quality;
      this.quality = Math.min(quality, getQualityLevelCount() - 1);
      this.propertyChangeSupport.firePropertyChange(Property.QUALITY.name(), oldQuality, quality);
      this.home.getEnvironment().setPhotoQuality(this.quality);
    }
  }
  
  /**
   * Returns the rendering quality of the photo.
   */
  public int getQuality() {
    return this.quality;
  }

  /**
   * Returns the maximum value for quality.
   */
  public int getQualityLevelCount() {
    return 4;
  }
  
  /**
   * Sets the aspect ratio of the 3D view.
   */
  public void set3DViewAspectRatio(float view3DAspectRatio) {
    if (this.view3DAspectRatio != view3DAspectRatio) {
      float oldAspectRatio = this.view3DAspectRatio;
      this.view3DAspectRatio = view3DAspectRatio;
      this.propertyChangeSupport.firePropertyChange(Property.ASPECT_RATIO.name(), oldAspectRatio, view3DAspectRatio);
      if (this.aspectRatio == AspectRatio.VIEW_3D_RATIO) {
        setHeight(Math.round(this.width / this.view3DAspectRatio), false);
      }
    }
  }
  
  /**
   * Returns the aspect ratio of the 3D view.
   */
  public float get3DViewAspectRatio() {
    return this.view3DAspectRatio;
  }


  /**
   * Returns the 3D view used to compute aspect ratio bound to it.
   */
  public View get3DView() {
    return this.view3D;
  }
}
