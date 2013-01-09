/*
 * AbstractPhotoController.java 5 nov 2012
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

import com.eteks.sweethome3d.model.AspectRatio;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeEnvironment;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * The base class for controllers of photo creation views.
 * @author Emmanuel Puybaret
 * @since 4.0
 */
public abstract class AbstractPhotoController implements Controller {
  /**
   * The properties that may be edited by the view associated to this controller. 
   */
  public enum Property {ASPECT_RATIO, WIDTH, HEIGHT, QUALITY, VIEW_3D_ASPECT_RATIO, CEILING_LIGHT_COLOR}
  
  private final Home                  home;
  private final View                  view3D;
  private final ContentManager        contentManager;
  private final PropertyChangeSupport propertyChangeSupport;
  
  private AspectRatio                 aspectRatio;
  private int                         width;
  private int                         height;
  private int                         quality;
  private float                       view3DAspectRatio;
  private int                         ceilingLightColor;

  public AbstractPhotoController(Home home,
                                 UserPreferences preferences,
                                 View view3D,
                                 ContentManager contentManager) {
    this.home = home;
    this.view3D = view3D;
    this.contentManager = contentManager;
    this.propertyChangeSupport = new PropertyChangeSupport(this);
    this.view3DAspectRatio = 1;
    
    EnvironmentChangeListener listener = new EnvironmentChangeListener(this);
    home.getEnvironment().addPropertyChangeListener(HomeEnvironment.Property.PHOTO_WIDTH, listener);
    home.getEnvironment().addPropertyChangeListener(HomeEnvironment.Property.PHOTO_HEIGHT, listener);
    home.getEnvironment().addPropertyChangeListener(HomeEnvironment.Property.PHOTO_ASPECT_RATIO, listener);
    home.getEnvironment().addPropertyChangeListener(HomeEnvironment.Property.PHOTO_QUALITY, listener);
    home.getEnvironment().addPropertyChangeListener(HomeEnvironment.Property.CEILING_LIGHT_COLOR, listener);
    updateProperties();
  }

  /**
   * Home environment listener that updates ceiling light color. This listener is bound to this controller 
   * with a weak reference to avoid strong link between home and this controller.  
   */
  private static class EnvironmentChangeListener implements PropertyChangeListener {
    private WeakReference<AbstractPhotoController> photoController;
    
    public EnvironmentChangeListener(AbstractPhotoController photoController) {
      this.photoController = new WeakReference<AbstractPhotoController>(photoController);
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
      // If controller was garbage collected, remove this listener from home
      final AbstractPhotoController controller = this.photoController.get();
      if (controller == null) {
        ((HomeEnvironment)ev.getSource()).removePropertyChangeListener(HomeEnvironment.Property.PHOTO_WIDTH, this);
        ((HomeEnvironment)ev.getSource()).removePropertyChangeListener(HomeEnvironment.Property.PHOTO_HEIGHT, this);
        ((HomeEnvironment)ev.getSource()).removePropertyChangeListener(HomeEnvironment.Property.PHOTO_ASPECT_RATIO, this);
        ((HomeEnvironment)ev.getSource()).removePropertyChangeListener(HomeEnvironment.Property.PHOTO_QUALITY, this);
        ((HomeEnvironment)ev.getSource()).removePropertyChangeListener(HomeEnvironment.Property.CEILING_LIGHT_COLOR, this);
      } else if (HomeEnvironment.Property.PHOTO_WIDTH.name().equals(ev.getPropertyName())) {
        controller.setWidth((Integer)ev.getNewValue(), false);
      } else if (HomeEnvironment.Property.PHOTO_HEIGHT.name().equals(ev.getPropertyName())) {
        controller.setHeight((Integer)ev.getNewValue(), false);
      } else if (HomeEnvironment.Property.PHOTO_ASPECT_RATIO.name().equals(ev.getPropertyName())) {
        controller.setAspectRatio((AspectRatio)ev.getNewValue());
      } else if (HomeEnvironment.Property.PHOTO_QUALITY.name().equals(ev.getPropertyName())) {
        controller.setQuality((Integer)ev.getNewValue());
      } else if (HomeEnvironment.Property.CEILING_LIGHT_COLOR.name().equals(ev.getPropertyName())) {
        controller.setCeilingLightColor((Integer)ev.getNewValue());
      }
    }
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
    setCeilingLightColor(homeEnvironment.getCeillingLightColor());
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
        if (this.view3DAspectRatio != Float.POSITIVE_INFINITY) {
          setHeight(Math.round(width / this.view3DAspectRatio), false);
        }
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
          if (this.view3DAspectRatio != Float.POSITIVE_INFINITY) {
            setHeight(Math.round(width / this.view3DAspectRatio), false);
          }
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
          if (this.view3DAspectRatio != Float.POSITIVE_INFINITY) {
            setWidth(Math.round(height * this.view3DAspectRatio), false);
          }
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
   * Sets the edited ceiling light color.
   */
  public void setCeilingLightColor(int ceilingLightColor) {
    if (this.ceilingLightColor != ceilingLightColor) {
      int oldCeilingLightColor = this.ceilingLightColor;
      this.ceilingLightColor = ceilingLightColor;
      this.propertyChangeSupport.firePropertyChange(Property.CEILING_LIGHT_COLOR.name(), oldCeilingLightColor, ceilingLightColor);
      this.home.getEnvironment().setCeillingLightColor(ceilingLightColor);
    }
  }
  
  /**
   * Returns the edited ceiling light color.
   */
  public int getCeilingLightColor() {
    return this.ceilingLightColor;
  }

  /**
   * Sets the aspect ratio of the 3D view.
   */
  public void set3DViewAspectRatio(float view3DAspectRatio) {
    if (this.view3DAspectRatio != view3DAspectRatio) {
      float oldAspectRatio = this.view3DAspectRatio;
      this.view3DAspectRatio = view3DAspectRatio;
      this.propertyChangeSupport.firePropertyChange(Property.ASPECT_RATIO.name(), oldAspectRatio, view3DAspectRatio);
      if (this.aspectRatio == AspectRatio.VIEW_3D_RATIO
          && this.view3DAspectRatio != Float.POSITIVE_INFINITY) {
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

  /**
   * Controls the change of value of a visual property in home.
   */
  public void setVisualProperty(String propertyName,
                                Object propertyValue) {
    this.home.setVisualProperty(propertyName, propertyValue);
  }
}
