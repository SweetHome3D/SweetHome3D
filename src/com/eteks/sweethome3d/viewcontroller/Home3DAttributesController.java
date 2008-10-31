/*
 * Home3DAttributesController.java 25 juin 07
 *
 * Copyright (c) 2007 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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
import java.util.ResourceBundle;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.ObserverCamera;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * A MVC controller for home 3D attributes view.
 * @author Emmanuel Puybaret
 */
public class Home3DAttributesController implements Controller {
  /**
   * The properties that may be edited by the view associated to this controller. 
   */
  public enum Property {OBSERVER_FIELD_OF_VIEW_IN_DEGREES, OBSERVER_HEIGHT, 
      GROUND_COLOR, GROUND_PAINT, GROUND_TEXTURE, SKY_COLOR,
      LIGHT_COLOR, WALLS_ALPHA}
  /**
   * The possible values for {@linkplain #getGroundPaint() ground paint type}.
   */
  public enum EnvironmentPaint {COLORED, TEXTURED} 
  
  private final Home                  home;
  private final UserPreferences       preferences;
  private final ViewFactory           viewFactory;
  private final ContentManager        contentManager;
  private final UndoableEditSupport   undoSupport;
  private TextureChoiceController     groundTextureController;
  private final PropertyChangeSupport propertyChangeSupport;
  private DialogView                  home3DAttributesView;

  private int   observerFieldOfViewInDegrees;
  private float observerHeight;
  private int   groundColor;
  private EnvironmentPaint groundPaint;
  private int   skyColor;
  private int   lightColor;
  private float wallsAlpha;

  /**
   * Creates the controller of 3D view with undo support.
   */
  public Home3DAttributesController(Home home,
                                    UserPreferences preferences,
                                    ViewFactory viewFactory, 
                                    ContentManager contentManager,
                                    UndoableEditSupport undoSupport) {
    this.home = home;
    this.preferences = preferences;
    this.viewFactory = viewFactory;
    this.contentManager = contentManager;
    this.undoSupport = undoSupport;    
    this.propertyChangeSupport = new PropertyChangeSupport(this);
    
    updateProperties(home);
  }

  /**
   * Returns the texture controller of the ground.
   */
  public TextureChoiceController getGroundTextureController() {
    // Create sub controller lazily only once it's needed
    if (this.groundTextureController == null) {
      ResourceBundle resource = ResourceBundle.getBundle(Home3DAttributesController.class.getName());
      this.groundTextureController = new TextureChoiceController(
          resource.getString("groundTextureTitle"), this.preferences, this.viewFactory, this.contentManager);
      this.groundTextureController.addPropertyChangeListener(TextureChoiceController.Property.TEXTURE,
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              setGroundPaint(EnvironmentPaint.TEXTURED);
            }
          });
    }
    return this.groundTextureController;
  }

  /**
   * Returns the view associated with this controller.
   */
  public DialogView getView() {
    // Create view lazily only once it's needed
    if (this.home3DAttributesView == null) {
      this.home3DAttributesView = this.viewFactory.createHome3DAttributesView(
          this.preferences, this); 
    }
    return this.home3DAttributesView;
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
    this.propertyChangeSupport.addPropertyChangeListener(property.toString(), listener);
  }

  /**
   * Removes the property change <code>listener</code> in parameter from this controller.
   */
  public void removePropertyChangeListener(Property property, PropertyChangeListener listener) {
    this.propertyChangeSupport.removePropertyChangeListener(property.toString(), listener);
  }

  /**
   * Updates controller edited properties from <code>home</code> attributes.
   */
  private void updateProperties(Home home) {
    setObserverFieldOfViewInDegrees((int)(Math.round(Math.toDegrees(
        home.getObserverCamera().getFieldOfView())) + 360) % 360);
    setObserverHeight(home.getObserverCamera().getZ() * 15 / 14);
    setGroundColor(home.getGroundColor());
    HomeTexture groundTexture = home.getGroundTexture();
    getGroundTextureController().setTexture(groundTexture);
    if (groundTexture != null) {
      setGroundPaint(EnvironmentPaint.TEXTURED);
    } else {
      setGroundPaint(EnvironmentPaint.COLORED);
    }
    setSkyColor(home.getSkyColor());
    setLightColor(home.getLightColor());
    setWallsAlpha(home.getWallsAlpha());
  }
  
  /**
   * Sets the edited observer field of view in degrees.
   */
  public void setObserverFieldOfViewInDegrees(int observerFieldOfViewInDegrees) {
    if (observerFieldOfViewInDegrees != this.observerFieldOfViewInDegrees) {
      int oldObserverFieldOfViewInDegrees = this.observerFieldOfViewInDegrees;
      this.observerFieldOfViewInDegrees = observerFieldOfViewInDegrees;
      this.propertyChangeSupport.firePropertyChange(Property.OBSERVER_FIELD_OF_VIEW_IN_DEGREES.toString(), 
          oldObserverFieldOfViewInDegrees, observerFieldOfViewInDegrees);
    }
  }

  /**
   * Returns the edited observer field of view in degrees.
   */
  public int getObserverFieldOfViewInDegrees() {
    return this.observerFieldOfViewInDegrees;
  }

  /**
   * Sets the edited observer height.
   */
  public void setObserverHeight(float observerHeight) {
    if (observerHeight != this.observerHeight) {
      float oldObserverHeight = this.observerHeight;
      this.observerHeight = observerHeight;
      this.propertyChangeSupport.firePropertyChange(Property.OBSERVER_HEIGHT.toString(), oldObserverHeight, observerHeight);
    }
  }

  /**
   * Returns the edited observer height.
   */
  public float getObserverHeight() {
    return this.observerHeight;
  }

  /**
   * Sets the edited ground color.
   */
  public void setGroundColor(int groundColor) {
    if (groundColor != this.groundColor) {
      int oldGroundColor = this.groundColor;
      this.groundColor = groundColor;
      this.propertyChangeSupport.firePropertyChange(Property.GROUND_COLOR.toString(), oldGroundColor, groundColor);
      
      setGroundPaint(EnvironmentPaint.COLORED);
    }
  }

  /**
   * Returns the edited ground color.
   */
  public int getGroundColor() {
    return this.groundColor;
  }

  /**
   * Sets whether the ground is colored or textured.
   */
  public void setGroundPaint(EnvironmentPaint groundPaint) {
    if (groundPaint != this.groundPaint) {
      EnvironmentPaint oldGroundPaint = this.groundPaint;
      this.groundPaint = groundPaint;
      this.propertyChangeSupport.firePropertyChange(Property.GROUND_PAINT.toString(), oldGroundPaint, groundPaint);
    }
  }

  /**
   * Returns whether the ground is colored or textured.
   */
  public EnvironmentPaint getGroundPaint() {
    return this.groundPaint;
  }

  /**
   * Sets the edited sky color.
   */
  public void setSkyColor(int skyColor) {
    if (skyColor != this.skyColor) {
      int oldSkyColor = this.skyColor;
      this.skyColor = skyColor;
      this.propertyChangeSupport.firePropertyChange(Property.SKY_COLOR.toString(), oldSkyColor, skyColor);
    }
  }

  /**
   * Returns the edited sky color.
   */
  public int getSkyColor() {
    return this.skyColor;
  }

  /**
   * Sets the edited light color.
   */
  public void setLightColor(int lightColor) {
    if (lightColor != this.lightColor) {
      int oldLightColor = this.lightColor;
      this.lightColor = lightColor;
      this.propertyChangeSupport.firePropertyChange(Property.LIGHT_COLOR.toString(), oldLightColor, lightColor);
    }
  }

  /**
   * Returns the edited light color.
   */
  public int getLightColor() {
    return this.lightColor;
  }

  /**
   * Sets the edited walls transparency alpha.
   */
  public void setWallsAlpha(float wallsAlpha) {
    if (wallsAlpha != this.wallsAlpha) {
      float oldWallsAlpha = this.wallsAlpha;
      this.wallsAlpha = wallsAlpha;
      this.propertyChangeSupport.firePropertyChange(Property.WALLS_ALPHA.toString(), oldWallsAlpha, wallsAlpha);
    }
  }

  /**
   * Returns the edited walls transparency alpha.
   */
  public float getWallsAlpha() {
    return this.wallsAlpha;
  }

  /**
   * Controls the modification of of home.
   */
  public void modify() {
    final float observerCameraFieldOfView = (float)Math.toRadians(getObserverFieldOfViewInDegrees());
    final float observerCameraZ = getObserverHeight() * 14 / 15;
    final int   groundColor = getGroundColor();
    final HomeTexture groundTexture = getGroundPaint() == EnvironmentPaint.TEXTURED
        ? getGroundTextureController().getTexture()
        : null;
    final int   skyColor = getSkyColor();
    final int   lightColor  = getLightColor();
    final float wallsAlpha = getWallsAlpha();

    final float oldObserverCameraFieldOfView = this.home.getObserverCamera().getFieldOfView();
    final float oldObserverCameraZ = this.home.getObserverCamera().getZ();
    final int   oldGroundColor = this.home.getGroundColor();
    final HomeTexture oldGroundTexture = this.home.getGroundTexture();
    final int   oldSkyColor = this.home.getSkyColor();
    final int   oldLightColor  = this.home.getLightColor();
    final float oldWallsAlpha = this.home.getWallsAlpha();
    
    // Apply modification
    doModifyHome(observerCameraFieldOfView, observerCameraZ, 
        groundColor, groundTexture, skyColor, lightColor, wallsAlpha); 
    if (this.undoSupport != null) {
      UndoableEdit undoableEdit = new AbstractUndoableEdit() {
        @Override
        public void undo() throws CannotUndoException {
          super.undo();
          doModifyHome(oldObserverCameraFieldOfView, oldObserverCameraZ, 
              oldGroundColor, oldGroundTexture, oldSkyColor, oldLightColor, oldWallsAlpha); 
        }
        
        @Override
        public void redo() throws CannotRedoException {
          super.redo();
          doModifyHome(observerCameraFieldOfView, observerCameraZ, 
              groundColor, groundTexture, skyColor, lightColor, wallsAlpha); 
        }
        
        @Override
        public String getPresentationName() {
          return ResourceBundle.getBundle(Home3DAttributesController.class.getName()).
              getString("undoModify3DAttributes");
        }
      };
      this.undoSupport.postEdit(undoableEdit);
    }
  }

  /**
   * Modifies home 3D attributes.
   */
  private void doModifyHome(float observerCameraFieldOfView, 
                            float observerCameraZ, 
                            int groundColor, HomeTexture groundTexture, int skyColor, 
                            int lightColor, float wallsAlpha) {
    ObserverCamera observerCamera = this.home.getObserverCamera();
    observerCamera.setFieldOfView(observerCameraFieldOfView);
    observerCamera.setZ(observerCameraZ);
    this.home.setGroundColor(groundColor);
    this.home.setGroundTexture(groundTexture);
    this.home.setSkyColor(skyColor);
    this.home.setLightColor(lightColor);
    this.home.setWallsAlpha(wallsAlpha);
  }
}
