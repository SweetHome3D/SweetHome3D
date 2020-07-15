/*
 * Level.java 22 oct. 2011
 *
 * Sweet Home 3D, Copyright (c) 2011 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
package com.eteks.sweethome3d.model;

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * A level in a home.
 * @author Emmanuel Puybaret
 * @since 3.4
 */
public class Level extends HomeObject {
  private static final long serialVersionUID = 1L;

  /**
   * The properties of a level that may change. <code>PropertyChangeListener</code>s added
   * to a level will be notified under a property name equal to the string value of one these properties.
   */
  public enum Property {NAME, ELEVATION, HEIGHT, FLOOR_THICKNESS, BACKGROUND_IMAGE, VISIBLE, VIEWABLE, ELEVATION_INDEX};

  private String              name;
  private float               elevation;
  private float               floorThickness;
  private float               height;
  private BackgroundImage     backgroundImage;
  private boolean             visible;
  private boolean             viewable;
  private int                 elevationIndex;

  /**
   * Creates a home level.
   * @param name  the name of the level
   * @param elevation the elevation of the bottom of the level
   * @param floorThickness the floor thickness of the level
   * @param height the height of the level
   */
  public Level(String name, float elevation, float floorThickness, float height) {
    this(createId("level"), name, elevation, floorThickness, height);
  }

  /**
   * Creates a home level.
   * @param id    the ID of the level
   * @param name  the name of the level
   * @param elevation the elevation of the bottom of the level
   * @param floorThickness the floor thickness of the level
   * @param height the height of the level
   * @since 6.4
   */
  public Level(String id, String name, float elevation, float floorThickness, float height) {
    super(id);
    this.name = name;
    this.elevation = elevation;
    this.floorThickness = floorThickness;
    this.height = height;
    this.visible = true;
    this.viewable = true;
    this.elevationIndex = -1;
  }

  /**
   * Initializes new level fields to their default values
   * and reads level from <code>in</code> stream with default reading method.
   */
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    this.visible = true;
    this.viewable = true;
    this.elevationIndex = -1;
    in.defaultReadObject();
  }

  /**
   * Returns the name of this level.
   */
  public String getName() {
    return this.name;
  }

  /**
   * Sets the name of this level. Once this level
   * is updated, listeners added to this level will receive a change notification.
   */
  public void setName(String name) {
    if (name != this.name
        && (name == null || !name.equals(this.name))) {
      String oldName = this.name;
      this.name = name;
      firePropertyChange(Property.NAME.name(), oldName, name);
    }
  }

  /**
   * Returns the elevation of the bottom of this level.
   */
  public float getElevation() {
    return this.elevation;
  }

  /**
   * Sets the elevation of this level. Once this level is updated,
   * listeners added to this level will receive a change notification.
   */
  public void setElevation(float elevation) {
    if (elevation != this.elevation) {
      float oldElevation = this.elevation;
      this.elevation = elevation;
      firePropertyChange(Property.ELEVATION.name(), oldElevation, elevation);
    }
  }

  /**
   * Returns the floor thickness of this level.
   */
  public float getFloorThickness() {
    return this.floorThickness;
  }

  /**
   * Sets the floor thickness of this level. Once this level is updated,
   * listeners added to this level will receive a change notification.
   */
  public void setFloorThickness(float floorThickness) {
    if (floorThickness != this.floorThickness) {
      float oldFloorThickness = this.floorThickness;
      this.floorThickness = floorThickness;
      firePropertyChange(Property.FLOOR_THICKNESS.name(), oldFloorThickness, floorThickness);
    }
  }

  /**
   * Returns the height of this level.
   */
  public float getHeight() {
    return this.height;
  }

  /**
   * Sets the height of this level. Once this level is updated,
   * listeners added to this level will receive a change notification.
   */
  public void setHeight(float height) {
    if (height != this.height) {
      float oldHeight = this.height;
      this.height = height;
      firePropertyChange(Property.HEIGHT.name(), oldHeight, height);
    }
  }

  /**
   * Returns the plan background image of this level.
   */
  public BackgroundImage getBackgroundImage() {
    return this.backgroundImage;
  }

  /**
   * Sets the plan background image of this level and fires a <code>PropertyChangeEvent</code>.
   */
  public void setBackgroundImage(BackgroundImage backgroundImage) {
    if (backgroundImage != this.backgroundImage) {
      BackgroundImage oldBackgroundImage = this.backgroundImage;
      this.backgroundImage = backgroundImage;
      firePropertyChange(Property.BACKGROUND_IMAGE.name(), oldBackgroundImage, backgroundImage);
    }
  }

  /**
   * Returns <code>true</code> if this level is visible.
   */
  public boolean isVisible() {
    return this.visible;
  }

  /**
   * Sets whether this level is visible or not. Once this level is updated,
   * listeners added to this level will receive a change notification.
   */
  public void setVisible(boolean visible) {
    if (visible != this.visible) {
      this.visible = visible;
      firePropertyChange(Property.VISIBLE.name(), !visible, visible);
    }
  }

  /**
   * Returns <code>true</code> if this level is viewable.
   * @since 5.0
   */
  public boolean isViewable() {
    return this.viewable;
  }

  /**
   * Sets whether this level is viewable or not. Once this level is updated,
   * listeners added to this level will receive a change notification.
   * @since 5.0
   */
  public void setViewable(boolean viewable) {
    if (viewable != this.viewable) {
      this.viewable = viewable;
      firePropertyChange(Property.VIEWABLE.name(), !viewable, viewable);
    }
  }

  /**
   * Returns <code>true</code> if this level is viewable and visible.
   * @since 5.0
   */
  public boolean isViewableAndVisible() {
    return this.viewable && this.visible;
  }

  /**
   * Returns the index of this level used to order levels at the same elevation.
   * @since 5.0
   */
  public int getElevationIndex() {
    return this.elevationIndex;
  }

  /**
   * Sets the index of this level used to order levels at the same elevation.
   * @since 5.0
   */
  public void setElevationIndex(int elevationIndex) {
    if (elevationIndex != this.elevationIndex) {
      int oldElevationIndex = this.elevationIndex;
      this.elevationIndex = elevationIndex;
      firePropertyChange(Property.ELEVATION_INDEX.name(), oldElevationIndex, elevationIndex);
    }
  }

  /**
   * Returns a clone of this level.
   */
  @Override
  public Level clone() {
    return (Level)super.clone();
  }
}
