/*
 * ObserverCameraController.java 09 mars 2012
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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeEnvironment;
import com.eteks.sweethome3d.model.Level;
import com.eteks.sweethome3d.model.ObserverCamera;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * A MVC controller for observer camera attributes view.
 * @author Emmanuel Puybaret
 */
public class ObserverCameraController implements Controller {
  /**
   * The properties that may be edited by the view associated to this controller. 
   */
  public enum Property {X, Y, ELEVATION, MINIMUM_ELEVATION,
      YAW_IN_DEGREES, YAW, PITCH_IN_DEGREES, PITCH, FIELD_OF_VIEW_IN_DEGREES, FIELD_OF_VIEW, 
      OBSERVER_CAMERA_ELEVATION_ADJUSTED}
  
  private final Home                  home;
  private final UserPreferences       preferences;
  private final ViewFactory           viewFactory;
  private final PropertyChangeSupport propertyChangeSupport;
  private DialogView                  observerCameraView;

  private float             x;
  private float             y;
  private float             elevation;
  private float             minimumElevation;
  private int               yawInDegrees;
  private float             yaw;
  private int               pitchInDegrees;
  private float             pitch;
  private int               fieldOfViewInDegrees;
  private float             fieldOfView;
  private boolean           elevationAdjusted;

  /**
   * Creates the controller of 3D view with undo support.
   */
  public ObserverCameraController(Home home,
                                  UserPreferences preferences,
                                  ViewFactory viewFactory) {
    this.home = home;
    this.preferences = preferences;
    this.viewFactory = viewFactory;
    this.propertyChangeSupport = new PropertyChangeSupport(this);
    
    updateProperties();
  }

  /**
   * Returns the view associated with this controller.
   */
  public DialogView getView() {
    // Create view lazily only once it's needed
    if (this.observerCameraView == null) {
      this.observerCameraView = this.viewFactory.createObserverCameraView(this.preferences, this); 
    }
    return this.observerCameraView;
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
   * Updates edited properties from the 3D attributes of the home edited by this controller.
   */
  protected void updateProperties() {
    ObserverCamera observerCamera = this.home.getObserverCamera();
    setX(observerCamera.getX());
    setY(observerCamera.getY());
    List<Level> levels = this.home.getLevels();
    setMinimumElevation(levels.size() == 0 
        ? 10  
        : 10 + levels.get(0).getElevation());
    setElevation(observerCamera.getZ());
    setYaw(observerCamera.getYaw());
    setPitch(observerCamera.getPitch());
    setFieldOfView(observerCamera.getFieldOfView());
    HomeEnvironment homeEnvironment = this.home.getEnvironment();
    setElevationAdjusted(homeEnvironment.isObserverCameraElevationAdjusted());
  }

  /**
   * Sets the edited abscissa.
   */
  public void setX(float x) {
    if (x != this.x) {
      float oldX = this.x;
      this.x = x;
      this.propertyChangeSupport.firePropertyChange(Property.X.name(), oldX, x);
    }
  }

  /**
   * Returns the edited abscissa.
   */
  public float getX() {
    return this.x;
  }

  /**
   * Sets the edited ordinate.
   */
  public void setY(float y) {
    if (y != this.y) {
      float oldY = this.y;
      this.y = y;
      this.propertyChangeSupport.firePropertyChange(Property.Y.name(), oldY, y);
    }
  }

  /**
   * Returns the edited ordinate.
   */
  public float getY() {
    return this.y;
  }

  /**
   * Sets the edited camera elevation.
   */
  public void setElevation(float elevation) {
    if (elevation != this.elevation) {
      float oldObserverCameraElevation = this.elevation;
      this.elevation = elevation;
      this.propertyChangeSupport.firePropertyChange(Property.ELEVATION.name(), oldObserverCameraElevation, elevation);
    }
  }

  /**
   * Returns the edited camera elevation.
   */
  public float getElevation() {
    return this.elevation;
  }

  /**
   * Sets the minimum elevation.
   */
  private void setMinimumElevation(float minimumElevation) {
    if (minimumElevation != this.minimumElevation) {
      float oldMinimumElevation = this.minimumElevation;
      this.minimumElevation = minimumElevation;
      this.propertyChangeSupport.firePropertyChange(Property.MINIMUM_ELEVATION.name(), oldMinimumElevation, minimumElevation);
    }
  }

  /**
   * Returns the minimum elevation.
   */
  public float getMinimumElevation() {
    return this.minimumElevation;
  }

  /**
   * Returns <code>true</code> if the observer elevation should be adjusted according 
   * to the elevation of the selected level.
   */
  public boolean isElevationAdjusted() {
    return this.elevationAdjusted;
  }
  
  /**
   * Sets whether the observer elevation should be adjusted according 
   * to the elevation of the selected level.
   */
  public void setElevationAdjusted(boolean observerCameraElevationAdjusted) {
    if (this.elevationAdjusted != observerCameraElevationAdjusted) {
      this.elevationAdjusted = observerCameraElevationAdjusted;
      this.propertyChangeSupport.firePropertyChange(Property.OBSERVER_CAMERA_ELEVATION_ADJUSTED.name(), 
          !observerCameraElevationAdjusted, observerCameraElevationAdjusted);
      Level selectedLevel = this.home.getSelectedLevel();
      if (selectedLevel != null) {
        if (observerCameraElevationAdjusted) {
          setElevation(getElevation() - selectedLevel.getElevation());
        } else {
          setElevation(getElevation() + selectedLevel.getElevation());
        }
      }
    }
  }
  
  /**
   * Returns <code>true</code> if the adjustment of the observer camera according to the current level is modifiable.
   */
  public boolean isObserverCameraElevationAdjustedEditable() {
    return this.home.getLevels().size() > 1;
  }
  
  /**
   * Sets the edited yaw in degrees.
   */
  public void setYawInDegrees(int yawInDegrees) {
    setYawInDegrees(yawInDegrees, true);
  }

  private void setYawInDegrees(int yawInDegrees, boolean updateYaw) {
    if (yawInDegrees != this.yawInDegrees) {
      int oldYawInDegrees = this.yawInDegrees;
      this.yawInDegrees = yawInDegrees;
      this.propertyChangeSupport.firePropertyChange(Property.YAW_IN_DEGREES.name(), oldYawInDegrees, yawInDegrees);
      if (updateYaw) {
        setYaw((float)Math.toRadians(yawInDegrees), false);
      }
    }
  }

  /**
   * Returns the edited yaw in degrees.
   */
  public int getYawInDegrees() {
    return this.yawInDegrees;
  }

  /**
   * Sets the edited yaw in radians.
   * @since 5.5
   */
  public void setYaw(float yaw) {
    setYaw(yaw, true);
  }

  private void setYaw(float yaw, boolean updateYawInDegrees) {
    if (yaw != this.yaw) {
      float oldYaw = this.yaw;
      this.yaw = yaw;
      this.propertyChangeSupport.firePropertyChange(Property.YAW.name(), oldYaw, yaw);
      if (updateYawInDegrees) {
        setYawInDegrees((int)Math.round(Math.toDegrees(yaw)), false);
      }
    }
  }

  /**
   * Returns the edited yaw in radians.
   * @since 5.5
   */
  public float getYaw() {
    return this.yaw;
  }

  /**
   * Sets the edited pitch in degrees.
   */
  public void setPitchInDegrees(int pitchInDegrees) {
    setPitchInDegrees(pitchInDegrees, true);
  }

  private void setPitchInDegrees(int pitchInDegrees, boolean updatePitch) {
    if (pitchInDegrees != this.pitchInDegrees) {
      int oldPitchInDegrees = this.pitchInDegrees;
      this.pitchInDegrees = pitchInDegrees;
      this.propertyChangeSupport.firePropertyChange(Property.PITCH_IN_DEGREES.name(), oldPitchInDegrees, pitchInDegrees);
      if (updatePitch) {
        setPitch((float)Math.toRadians(pitchInDegrees), false);
      }
    }
  }
  
  /**
   * Returns the edited pitch in degrees.
   */
  public int getPitchInDegrees() {
    return this.pitchInDegrees;
  }

  /**
   * Sets the edited pitch in radians.
   * @since 5.5
   */
  public void setPitch(float pitch) {
    setPitch(pitch, true);
  }

  private void setPitch(float pitch, boolean updatePitchInDegrees) {
    if (pitch != this.pitch) {
      float oldPitch = this.pitch;
      this.pitch = pitch;
      this.propertyChangeSupport.firePropertyChange(Property.PITCH.name(), oldPitch, pitch);
      if (updatePitchInDegrees) {
        setPitchInDegrees((int)(Math.round(Math.toDegrees(pitch))), false);
      }
    }
  }

  /**
   * Returns the edited pitch in radians.
   * @since 5.5
   */
  public float getPitch() {
    return this.pitch;
  }

  /**
   * Sets the edited observer field of view in degrees.
   */
  public void setFieldOfViewInDegrees(int fieldOfViewInDegrees) {
    setFieldOfViewInDegrees(fieldOfViewInDegrees, true);
  }
  
  public void setFieldOfViewInDegrees(int fieldOfViewInDegrees, boolean updateFieldOfView) {
    if (fieldOfViewInDegrees != this.fieldOfViewInDegrees) {
      int oldFieldOfViewInDegrees = this.fieldOfViewInDegrees;
      this.fieldOfViewInDegrees = fieldOfViewInDegrees;
      this.propertyChangeSupport.firePropertyChange(Property.FIELD_OF_VIEW_IN_DEGREES.name(), 
          oldFieldOfViewInDegrees, fieldOfViewInDegrees);
      if (updateFieldOfView) {
        setFieldOfView((float)Math.toRadians(fieldOfViewInDegrees), false);
      }
    }
  }

  /**
   * Returns the edited observer field of view in degrees.
   */
  public int getFieldOfViewInDegrees() {
    return this.fieldOfViewInDegrees;
  }

  /**
   * Sets the edited observer field of view in radians.
   * @since 5.5
   */
  public void setFieldOfView(float fieldOfView) {
    setFieldOfView(fieldOfView, true);
  }

  private void setFieldOfView(float fieldOfView, boolean updateFieldOfViewInDegrees) {
    if (fieldOfView != this.fieldOfView) {
      float oldFieldOfView = this.fieldOfView;
      this.fieldOfView = fieldOfView;
      this.propertyChangeSupport.firePropertyChange(Property.FIELD_OF_VIEW.name(), oldFieldOfView, fieldOfView);
      if (updateFieldOfViewInDegrees) {
        setFieldOfViewInDegrees((int)(Math.round(Math.toDegrees(fieldOfView))), false);
      }
    }
  }

  /**
   * Returns the edited observer field of view in radians.
   * @since 5.5
   */
  public float getFieldOfView() {
    return this.fieldOfView;
  }

  /**
   * Controls the modification of the observer camera of the edited home.
   */
  public void modifyObserverCamera() {
    float x = getX();
    float y = getY();
    float z = getElevation();
    boolean observerCameraElevationAdjusted = isElevationAdjusted();
    Level selectedLevel = this.home.getSelectedLevel();
    if (observerCameraElevationAdjusted && selectedLevel != null) {
      z += selectedLevel.getElevation();
      List<Level> levels = this.home.getLevels();
      z = Math.max(z, levels.size() == 0  ? 10  : 10 + levels.get(0).getElevation());
    }
    float yaw = getYaw();
    float pitch = getPitch();
    float fieldOfView = getFieldOfView();

    // Apply modification with no undo
    ObserverCamera observerCamera = this.home.getObserverCamera();
    observerCamera.setX(x);
    observerCamera.setY(y);
    observerCamera.setZ(z);
    observerCamera.setYaw(yaw);
    observerCamera.setPitch(pitch);
    observerCamera.setFieldOfView(fieldOfView);
    HomeEnvironment homeEnvironment = this.home.getEnvironment();
    homeEnvironment.setObserverCameraElevationAdjusted(observerCameraElevationAdjusted);
  }
}
