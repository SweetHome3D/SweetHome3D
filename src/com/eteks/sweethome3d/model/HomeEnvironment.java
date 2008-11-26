/*
 * HomeEnvironment.java 6 nov. 2008
 *
 * Copyright (c) 2008 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * The environment attributes of a home.
 * @author Emmanuel Puybaret
 */
public class HomeEnvironment implements Serializable {
  private static final long serialVersionUID = 1L;

  /**
   * The environment properties that may change.
   */
  public enum Property {SKY_COLOR, SKY_TEXTURE, GROUND_COLOR, GROUND_TEXTURE, LIGHT_COLOR, WALLS_ALPHA, DRAWING_MODE};
  /**
   * The various modes used to draw home in 3D. 
   */
  public enum DrawingMode {
    FILL, OUTLINE, FILL_AND_OUTLINE
  }
  
  private int                             groundColor;
  private HomeTexture                     groundTexture;
  private int                             skyColor;
  private HomeTexture                     skyTexture;
  private int                             lightColor;
  private float                           wallsAlpha;
  private DrawingMode                     drawingMode;
  private transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

  /**
   * Creates default environment.
   */
  public HomeEnvironment() {
    this(0xE0E0E0, // Ground color
        null,      // Ground texture
        (204 << 16) + (228 << 8) + 252, // Sky color
        0xF0F0F0,  // Light color
        0);        // Walls alpha
  }

  /**
   * Creates home environment from parameters.
   */
  public HomeEnvironment(int groundColor,
                         HomeTexture groundTexture, int skyColor,
                         int lightColor, float wallsAlpha) {
    this.groundColor = groundColor;
    this.groundTexture = groundTexture;
    this.skyColor = skyColor;
    this.lightColor = lightColor;
    this.wallsAlpha = wallsAlpha;
    
    this.drawingMode = DrawingMode.FILL;
  }

  /**
   * Initializes environment transient fields  
   * and reads attributes from <code>in</code> stream with default reading method.
   */
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    this.propertyChangeSupport = new PropertyChangeSupport(this);
    in.defaultReadObject();
  }

  /**
   * Adds the property change <code>listener</code> in parameter to this environment.
   */
  public void addPropertyChangeListener(Property property, PropertyChangeListener listener) {
    this.propertyChangeSupport.addPropertyChangeListener(property.name(), listener);
  }

  /**
   * Removes the property change <code>listener</code> in parameter from this environment.
   */
  public void removePropertyChangeListener(Property property, PropertyChangeListener listener) {
    this.propertyChangeSupport.removePropertyChangeListener(property.name(), listener);
  }

  /**
   * Returns the ground color of this environment.
   */
  public int getGroundColor() {
    return this.groundColor;
  }

  /**
   * Sets the ground color of this environment and fires a <code>PropertyChangeEvent</code>.
   */
  public void setGroundColor(int groundColor) {
    if (groundColor != this.groundColor) {
      int oldGroundColor = this.groundColor;
      this.groundColor = groundColor;
      this.propertyChangeSupport.firePropertyChange(
          Property.GROUND_COLOR.name(), oldGroundColor, groundColor);
    }
  }

  /**
   * Returns the ground texture of this environment.
   */
  public HomeTexture getGroundTexture() {
    return this.groundTexture;
  }

  /**
   * Sets the ground texture of this environment and fires a <code>PropertyChangeEvent</code>.
   */
  public void setGroundTexture(HomeTexture groundTexture) {
    if (groundTexture != this.groundTexture) {
      HomeTexture oldGroundTexture = this.groundTexture;
      this.groundTexture = groundTexture;
      this.propertyChangeSupport.firePropertyChange(
          Property.GROUND_TEXTURE.name(), oldGroundTexture, groundTexture);
    }
  }

  /**
   * Returns the sky color of this environment.
   */
  public int getSkyColor() {
    return this.skyColor;
  }
  
  /**
   * Sets the sky color of this environment and fires a <code>PropertyChangeEvent</code>.
   */
  public void setSkyColor(int skyColor) {
    if (skyColor != this.skyColor) {
      int oldSkyColor = this.skyColor;
      this.skyColor = skyColor;
      this.propertyChangeSupport.firePropertyChange(
          Property.SKY_COLOR.name(), oldSkyColor, skyColor);
    }
  }
  
  /**
   * Returns the sky texture of this environment.
   */
  public HomeTexture getSkyTexture() {
    return this.skyTexture;
  }

  /**
   * Sets the sky texture of this environment and fires a <code>PropertyChangeEvent</code>.
   */
  public void setSkyTexture(HomeTexture skyTexture) {
    if (skyTexture != this.skyTexture) {
      HomeTexture oldSkyTexture = this.skyTexture;
      this.skyTexture = skyTexture;
      this.propertyChangeSupport.firePropertyChange(
          Property.SKY_TEXTURE.name(), oldSkyTexture, skyTexture);
    }
  }

  /**
   * Returns the light color of this environment.
   */
  public int getLightColor() {
    return this.lightColor;
  }

  /**
   * Sets the color that lights this environment and fires a <code>PropertyChangeEvent</code>.
   */
  public void setLightColor(int lightColor) {
    if (lightColor != this.lightColor) {
      int oldLightColor = this.lightColor;
      this.lightColor = lightColor;
      this.propertyChangeSupport.firePropertyChange(
          Property.LIGHT_COLOR.name(), oldLightColor, lightColor);
    }
  }

  /**
   * Returns the walls transparency alpha factor of this environment.
   */
  public float getWallsAlpha() {
    return this.wallsAlpha;
  }

  /**
   * Sets the walls transparency alpha of this environment and fires a <code>PropertyChangeEvent</code>.
   * @param wallsAlpha a value between 0 and 1, 0 meaning opaque and 1 invisible.
   */
  public void setWallsAlpha(float wallsAlpha) {
    if (wallsAlpha != this.wallsAlpha) {
      float oldWallsAlpha = this.wallsAlpha;
      this.wallsAlpha = wallsAlpha;
      this.propertyChangeSupport.firePropertyChange(
          Property.WALLS_ALPHA.name(), oldWallsAlpha, wallsAlpha);
    }
  }

  /**
   * Returns the drawing mode of this environment.
   */
  public DrawingMode getDrawingMode() {
    return this.drawingMode;
  }

  /**
   * Sets the drawing mode of this environment and fires a <code>PropertyChangeEvent</code>.
   */
  public void setDrawingMode(DrawingMode drawingMode) {
    if (drawingMode != this.drawingMode) {
      DrawingMode oldDrawingMode = this.drawingMode;
      this.drawingMode = drawingMode;
      this.propertyChangeSupport.firePropertyChange(
          Property.DRAWING_MODE.name(), oldDrawingMode, drawingMode);
    }
  }
}