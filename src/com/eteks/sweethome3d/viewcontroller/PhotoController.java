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

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * The controller of the photo creation view.
 * @author Emmanuel Puybaret
 */
public class PhotoController implements Controller {
  /**
   * The properties that may be edited by the view associated to this controller. 
   */
  public enum Property {ASPECT_RATIO, WIDTH, HEIGHT, PROPORTIONAL, QUALITY}
  
  private final Home                  home;
  private final UserPreferences       preferences;
  private final ViewFactory           viewFactory;
  private final ContentManager        contentManager;
  private final PropertyChangeSupport propertyChangeSupport;
  private DialogView                  photoView;
  
  private float                       aspectRatio;
  private int                         width;
  private int                         height;
  private boolean                     propotional;
  private int                         quality;

  public PhotoController(Home home,
                         UserPreferences preferences, 
                         ViewFactory viewFactory,
                         ContentManager contentManager) {
    this.home = home;
    this.preferences = preferences;
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
    setAspectRatio(4f / 3);
    setWidth(400);
    setHeight(300);
    setProportional(false);
    setQuality(0);
  }
  
  /**
   * Sets the aspect ratio of the photo.
   */
  public void setAspectRatio(float aspectRatio) {
    if (this.aspectRatio != aspectRatio) {
      float oldAspectRatio = this.aspectRatio;
      this.aspectRatio = aspectRatio;
      this.propertyChangeSupport.firePropertyChange(Property.ASPECT_RATIO.name(), oldAspectRatio, aspectRatio);
    }
  }
  
  /**
   * Returns the aspect ratio of the photo.
   */
  public float getAspectRatio() {
    return this.aspectRatio;
  }

  /**
   * Sets the width of the photo.
   */
  public void setWidth(int width) {
    setWidth(width, true);
  }
  
  private void setWidth(int width, boolean updateHeightIfProportional) {
    if (this.width != width) {
      int oldWidth = this.width;
      this.width = width;
      this.propertyChangeSupport.firePropertyChange(Property.WIDTH.name(), oldWidth, width);
      if (updateHeightIfProportional && isProportional()) {
        setHeight(Math.round(width / getAspectRatio()), false);
      }
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
  
  private void setHeight(int height, boolean updateWidthIfProportional) {
    if (this.height != height) {
      int oldHeight = this.height;
      this.height = height;
      this.propertyChangeSupport.firePropertyChange(Property.HEIGHT.name(), oldHeight, height);
      if (updateWidthIfProportional && isProportional()) {
        setWidth(Math.round(height * getAspectRatio()), false);
      }
    }
  }
  
  /**
   * Returns the height of the photo.
   */
  public int getHeight() {
    return this.height;
  }

  /**
   * Sets whether the height of the photo is proportional to aspect ratio.
   */
  public void setProportional(boolean propotional) {
    if (this.propotional != propotional) {
      this.propotional = propotional;
      this.propertyChangeSupport.firePropertyChange(Property.PROPORTIONAL.name(), !propotional, propotional);
      if (propotional) {
        setHeight(Math.round(getWidth() / getAspectRatio()), false);
      }
    }
  }
  
  /**
   * Returns <code>true</code> if the height of the photo is proportional to aspect ratio.
   */
  public boolean isProportional() {
    return this.propotional;
  }

  /**
   * Sets the rendering quality of the photo.
   */
  public void setQuality(int quality) {
    if (this.quality != quality) {
      int oldQuality = this.quality;
      this.quality = quality;
      this.propertyChangeSupport.firePropertyChange(Property.QUALITY.name(), oldQuality, quality);
    }
  }
  
  /**
   * Returns the rendering quality of the photo.
   */
  public int getQuality() {
    return this.quality;
  }
}
