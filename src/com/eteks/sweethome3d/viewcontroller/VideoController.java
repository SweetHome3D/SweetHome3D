/*
 * VideoController.java 15 fev 2010
 *
 * Copyright (c) 2010 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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
import java.util.List;

import com.eteks.sweethome3d.model.AspectRatio;
import com.eteks.sweethome3d.model.Camera;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeEnvironment;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * The controller of the video creation view.
 * @author Emmanuel Puybaret
 */
public class VideoController implements Controller {
  /**
   * The properties that may be edited by the view associated to this controller. 
   */
  public enum Property {ASPECT_RATIO, FRAME_RATE, WIDTH, HEIGHT, QUALITY, CAMERA_PATH, TIME, CEILING_LIGHT_COLOR}
  
  private final Home                  home;
  private final UserPreferences       preferences;
  private final ViewFactory           viewFactory;
  private final ContentManager        contentManager;
  private final PropertyChangeSupport propertyChangeSupport;
  private DialogView                  videoView;
  
  private AspectRatio                 aspectRatio;
  private int                         frameRate;
  private int                         width;
  private int                         height;
  private int                         quality;
  private List<Camera>                cameraPath;
  private long                        time;
  private int                         ceilingLightColor;

  public VideoController(Home home,
                         UserPreferences preferences, 
                         ViewFactory viewFactory,
                         ContentManager contentManager) {
    this.home = home;
    this.preferences = preferences;
    this.viewFactory = viewFactory;
    this.contentManager = contentManager;
    this.propertyChangeSupport = new PropertyChangeSupport(this);
    
    updateProperties();
    home.getEnvironment().addPropertyChangeListener(HomeEnvironment.Property.CEILING_LIGHT_COLOR, new HomeEnvironmentChangeListener(this));
  }

  /**
   * Home environment listener that updates properties. This listener is bound to this controller 
   * with a weak reference to avoid strong link between home and this controller.  
   */
  private static class HomeEnvironmentChangeListener implements PropertyChangeListener {
    private WeakReference<VideoController> videoController;
    
    public HomeEnvironmentChangeListener(VideoController videoController) {
      this.videoController = new WeakReference<VideoController>(videoController);
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
      // If controller was garbage collected, remove this listener from home
      final VideoController controller = this.videoController.get();
      if (controller == null) {
        ((HomeEnvironment)ev.getSource()).removePropertyChangeListener(HomeEnvironment.Property.CEILING_LIGHT_COLOR, this);
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
    if (this.videoView == null) {
      this.videoView = this.viewFactory.createVideoView(this.home, this.preferences, this);
    }
    return this.videoView;
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
   * Updates edited properties from the video creation preferences.
   */
  protected void updateProperties() {
    HomeEnvironment homeEnvironment = this.home.getEnvironment();
    setAspectRatio(homeEnvironment.getVideoAspectRatio());
    setFrameRate(homeEnvironment.getVideoFrameRate());
    setWidth(homeEnvironment.getVideoWidth(), false);
    setHeight(homeEnvironment.getVideoHeight(), false);
    setQuality(homeEnvironment.getVideoQuality());
    List<Camera> videoCameraPath = homeEnvironment.getVideoCameraPath();
    setCameraPath(videoCameraPath);
    setTime(videoCameraPath.isEmpty() 
        ? this.home.getCamera().getTime()
        : videoCameraPath.get(0).getTime());
    setCeilingLightColor(homeEnvironment.getCeillingLightColor());
  }
  
  /**
   * Sets the aspect ratio of the video.
   */
  public void setAspectRatio(AspectRatio aspectRatio) {
    if (this.aspectRatio != aspectRatio) {
      AspectRatio oldAspectRatio = this.aspectRatio;
      this.aspectRatio = aspectRatio;
      this.propertyChangeSupport.firePropertyChange(Property.ASPECT_RATIO.name(), oldAspectRatio, aspectRatio);
      this.home.getEnvironment().setVideoAspectRatio(this.aspectRatio);
      setHeight(Math.round(width / this.aspectRatio.getValue()), false);
    }
  }
  
  /**
   * Returns the aspect ratio of the video.
   */
  public AspectRatio getAspectRatio() {
    return this.aspectRatio;
  }

  /**
   * Sets the frame rate of the video.
   */
  public void setFrameRate(int frameRate) {
    if (this.frameRate != frameRate) {
      int oldFrameRate = this.frameRate;
      this.frameRate = frameRate;
      this.propertyChangeSupport.firePropertyChange(Property.QUALITY.name(), oldFrameRate, frameRate);
      this.home.getEnvironment().setVideoFrameRate(this.frameRate);
    }
  }
  
  /**
   * Returns the frame rate of the video.
   */
  public int getFrameRate() {
    return this.frameRate;
  }

  /**
   * Sets the width of the video.
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
        setHeight(Math.round(width / this.aspectRatio.getValue()), false);
      }
      this.home.getEnvironment().setVideoWidth(this.width);
    }
  }
  
  /**
   * Returns the width of the video.
   */
  public int getWidth() {
    return this.width;
  }

  /**
   * Sets the height of the video.
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
        setWidth(Math.round(height * this.aspectRatio.getValue()), false);
      }
    }
  }
  
  /**
   * Returns the height of the video.
   */
  public int getHeight() {
    return this.height;
  }

  /**
   * Sets the rendering quality of the video.
   */
  public void setQuality(int quality) {
    if (this.quality != quality) {
      int oldQuality = this.quality;
      this.quality = Math.min(quality, getQualityLevelCount() - 1);
      this.propertyChangeSupport.firePropertyChange(Property.QUALITY.name(), oldQuality, quality);
      this.home.getEnvironment().setVideoQuality(this.quality);
    }
  }
  
  /**
   * Returns the rendering quality of the video.
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
   * Returns the camera path of the video.
   */
  public List<Camera> getCameraPath() {
    return this.cameraPath;
  }
  
  /**
   * Sets the camera locations of the video.
   */
  public void setCameraPath(List<Camera> cameraPath) {
    if (this.cameraPath != cameraPath) {
      List<Camera> oldCameraPath = this.cameraPath;
      this.cameraPath = cameraPath;
      this.propertyChangeSupport.firePropertyChange(Property.CAMERA_PATH.name(), oldCameraPath, cameraPath);
      this.home.getEnvironment().setVideoCameraPath(this.cameraPath);
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
      this.home.getCamera().setTime(time);
    }
  }
  
  /**
   * Returns the edited time in UTC time zone.
   */
  public long getTime() {
    return this.time;
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
}
