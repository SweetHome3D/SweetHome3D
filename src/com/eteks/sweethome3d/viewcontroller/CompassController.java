/*
 * CompassController.java 22 sept. 2010
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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.Compass;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * A MVC controller for the compass view.
 * @author Emmanuel Puybaret
 */
public class CompassController implements Controller {
  /**
   * The properties that may be edited by the view associated to this controller. 
   */
  public enum Property {X, Y, DIAMETER, VISIBLE, NORTH_DIRECTION_IN_DEGREES, 
      LATITUDE_IN_DEGREES, LONGITUDE_IN_DEGREES, TIME_ZONE}
  
  private final Home                  home;
  private final UserPreferences       preferences;
  private final ViewFactory           viewFactory;
  private final UndoableEditSupport   undoSupport;
  private final PropertyChangeSupport propertyChangeSupport;
  private DialogView                  compassView;
  
  private float   x;
  private float   y;
  private float   diameter;
  private boolean visible;
  private float   northDirectionInDegrees;
  private float   latitudeInDegrees;
  private float   longitudeInDegrees;
  private String  timeZone;

  public CompassController(Home home, 
                           UserPreferences preferences, 
                           ViewFactory viewFactory,
                           UndoableEditSupport undoSupport) {
    this.home = home;
    this.preferences = preferences;
    this.viewFactory = viewFactory;
    this.undoSupport = undoSupport;
    this.propertyChangeSupport = new PropertyChangeSupport(this);
    
    updateProperties();
  }

  /**
   * Returns the view associated with this controller.
   */
  public DialogView getView() {
    // Create view lazily only once it's needed
    if (this.compassView == null) {
      this.compassView = this.viewFactory.createCompassView(
          this.preferences, this); 
    }
    return this.compassView;
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
   * Updates compass properties edited by this controller.
   */
  protected void updateProperties() {
    Compass compass = this.home.getCompass();
    setX(compass.getX());
    setY(compass.getY());
    setDiameter(compass.getDiameter());
    setVisible(compass.isVisible());
    setNorthDirectionInDegrees((float)Math.toDegrees(compass.getNorthDirection()));
    setLatitudeInDegrees((float)Math.toDegrees(compass.getLatitude()));
    setLongitudeInDegrees((float)Math.toDegrees(compass.getLongitude()));
    setTimeZone(compass.getTimeZone());
  }  

  /**
   * Returns the edited abscissa of the center.
   */
  public float getX() {
    return this.x;
  }
  
  /**
   * Sets the edited abscissa of the center.
   */
  public void setX(float x) {
    if (x != this.x) {
      float oldX = this.x;
      this.x = x;
      this.propertyChangeSupport.firePropertyChange(Property.X.name(), oldX, x);
    }
  }
  
  /**
   * Returns the edited ordinate of the center.
   */
  public float getY() {
    return this.y;
  }
  
  /**
   * Sets the edited ordinate of the center. 
   */
  public void setY(float y) {
    if (y != this.y) {
      float oldY = this.y;
      this.y = y;
      this.propertyChangeSupport.firePropertyChange(Property.Y.name(), oldY, y);
    }
  }

  /**
   * Returns the edited diameter.
   */
  public float getDiameter() {
    return this.diameter;
  }
  
  /**
   * Sets the edited diameter.
   */
  public void setDiameter(float diameter) {
    if (diameter != this.diameter) {
      float oldDiameter = this.diameter;
      this.diameter = diameter;
      this.propertyChangeSupport.firePropertyChange(Property.DIAMETER.name(), oldDiameter, diameter);
    }
  }

  /**
   * Returns whether compass is visible or not.
   */
  public boolean isVisible() {
    return this.visible;
  }
  
  /**
   * Sets whether this compass is visible or not.
   */
  public void setVisible(boolean visible) {
    if (visible != this.visible) {
      this.visible = visible;
      this.propertyChangeSupport.firePropertyChange(Property.VISIBLE.name(), !visible, visible);
    }
  }

  /**
   * Returns the edited North direction angle in degrees.
   */
  public float getNorthDirectionInDegrees() {
    return this.northDirectionInDegrees;
  }
  
  /**
   * Sets the edited North direction angle.
   */
  public void setNorthDirectionInDegrees(float northDirectionInDegrees) {
    if (northDirectionInDegrees != this.northDirectionInDegrees) {
      float oldNorthDirectionInDegrees = this.northDirectionInDegrees;
      this.northDirectionInDegrees = northDirectionInDegrees;
      this.propertyChangeSupport.firePropertyChange(Property.NORTH_DIRECTION_IN_DEGREES.name(), 
          oldNorthDirectionInDegrees, northDirectionInDegrees);
    }
  }
  
  /**
   * Returns the edited latitude in degrees.
   */
  public final float getLatitudeInDegrees() {
    return this.latitudeInDegrees;
  }
  
  /**
   * Sets the edited latitude in degrees.
   */
  public void setLatitudeInDegrees(float latitudeInDegrees) {
    if (latitudeInDegrees != this.latitudeInDegrees) {
      float oldLatitudeInDegrees = this.latitudeInDegrees;
      this.latitudeInDegrees = latitudeInDegrees;
      this.propertyChangeSupport.firePropertyChange(Property.LATITUDE_IN_DEGREES.name(), oldLatitudeInDegrees, latitudeInDegrees);
    }
  }
  
  /**
   * Returns the edited longitude in degrees.
   */
  public final float getLongitudeInDegrees() {
    return this.longitudeInDegrees;
  }
  
  /**
   * Sets the edited longitude of the center.
   */
  public void setLongitudeInDegrees(float longitudeInDegrees) {
    if (longitudeInDegrees != this.longitudeInDegrees) {
      float oldLongitudeInDegrees = this.longitudeInDegrees;
      this.longitudeInDegrees = longitudeInDegrees;
      this.propertyChangeSupport.firePropertyChange(Property.LONGITUDE_IN_DEGREES.name(), oldLongitudeInDegrees, longitudeInDegrees);
    }
  }
  
  /**
   * Returns the edited time zone identifier.
   */
  public String getTimeZone() {
    return this.timeZone;
  }
  
  /**
   * Sets the edited time zone identifier. 
   */
  public void setTimeZone(String timeZone) {
    if (!timeZone.equals(this.timeZone)) {
      String oldTimeZone = this.timeZone;
      this.timeZone = timeZone;
      this.propertyChangeSupport.firePropertyChange(Property.TIME_ZONE.name(), oldTimeZone, timeZone);
    }
  }

  /**
   * Modifies home compass from the values stored in this controller. 
   */
  public void modifyCompass() {
    float x = getX();
    float y = getY();
    float diameter = getDiameter();
    boolean visible = isVisible();
    float northDirection = (float)Math.toRadians(getNorthDirectionInDegrees());
    float latitude = (float)Math.toRadians(getLatitudeInDegrees());
    float longitude = (float)Math.toRadians(getLongitudeInDegrees());
    String timeZone = getTimeZone();
    UndoableEdit undoableEdit = 
        new CompassUndoableEdit(this.home.getCompass(), this.preferences, 
            x, y, diameter, visible, northDirection, latitude, longitude, timeZone);
    doModifyCompass(this.home.getCompass(), x, y, diameter, visible, northDirection, latitude, longitude, timeZone);
    this.undoSupport.postEdit(undoableEdit);
  }
  
  /**
   * Undoable edit for compass. This class isn't anonymous to avoid
   * being bound to controller and its view.
   */
  private static class CompassUndoableEdit extends AbstractUndoableEdit {
    private final Compass compass;
    private final UserPreferences preferences;
    private final float oldX;
    private final float oldY;
    private final float oldDiameter;
    private final float oldNorthDirection;
    private final float oldLatitude;
    private final float oldLongitude;
    private final String oldTimeZone;
    private final boolean oldVisible;
    private final float newX;
    private final float newY;
    private final float newDiameter;
    private final float newNorthDirection;
    private final float newLatitude;
    private final float newLongitude;
    private final String newTimeZone;
    private final boolean newVisible;
  
    public CompassUndoableEdit(Compass compass, UserPreferences preferences, float newX, float newY,
                               float newDiameter, boolean newVisible, float newNorthDirection, 
                               float newLatitude, float newLongitude, String newTimeZone) {
      this.compass = compass;
      this.preferences = preferences;
      this.oldX = compass.getX();
      this.oldY = compass.getY();
      this.oldDiameter = compass.getDiameter();
      this.oldVisible = compass.isVisible();
      this.oldNorthDirection = compass.getNorthDirection();
      this.oldLatitude = compass.getLatitude();
      this.oldLongitude = compass.getLongitude();
      this.oldTimeZone = compass.getTimeZone();
      this.newX = newX;
      this.newY = newY;
      this.newDiameter = newDiameter;
      this.newVisible = newVisible;
      this.newNorthDirection = newNorthDirection;
      this.newLatitude = newLatitude;
      this.newLongitude = newLongitude;
      this.newTimeZone = newTimeZone;
    }

    @Override
    public void undo() throws CannotUndoException {
      super.undo();
      doModifyCompass(this.compass, this.oldX, this.oldY, this.oldDiameter, this.oldVisible, 
          this.oldNorthDirection, this.oldLatitude, this.oldLongitude, this.oldTimeZone);
    }
  
    @Override
    public void redo() throws CannotRedoException {
      super.redo();
      doModifyCompass(this.compass, this.newX, this.newY, this.newDiameter, this.newVisible, 
          this.newNorthDirection, this.newLatitude, this.newLongitude, this.newTimeZone);
    }
  
    @Override
    public String getPresentationName() {
      return this.preferences.getLocalizedString(CompassController.class, "undoModifyCompassName");
    }
  }
  
  private static void doModifyCompass(Compass compass, float x, float y, float diameter, boolean visible, 
                                      float northDirection, float latitude, float longitude, String timeZone) {
    compass.setX(x);
    compass.setY(y);
    compass.setDiameter(diameter);
    compass.setVisible(visible);
    compass.setNorthDirection(northDirection);
    compass.setLatitude(latitude);
    compass.setLongitude(longitude);
    compass.setTimeZone(timeZone);
  }
}
