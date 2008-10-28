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
  private final Home                home;
  private final UserPreferences     preferences;
  private final ViewFactory         viewFactory;
  private final ContentManager      contentManager;
  private final UndoableEditSupport undoSupport;
  private TextureChoiceController   groundTextureController;
  private Home3DAttributesView      home3DAttributesView;

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
    }
    return this.groundTextureController;
  }

  /**
   * Returns the view associated with this controller.
   */
  public Home3DAttributesView getView() {
    // Create view lazily only once it's needed
    if (this.home3DAttributesView == null) {
      this.home3DAttributesView = this.viewFactory.createHome3DAttributesView(
          this.home, this.preferences, this); 
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
   * Controls the modification of of home.
   */
  public void modifyHome() {
    Home3DAttributesView attributesView = getView();
    final float observerCameraFieldOfView = attributesView.getObserverCameraFieldOfView();
    final float observerCameraZ = attributesView.getObserverCameraHeight() * 14 / 15;
    final int   groundColor = attributesView.getGroundColor();
    final HomeTexture groundTexture = attributesView.getGroundTexture();
    final int   skyColor = attributesView.getSkyColor();
    final int   lightColor  = attributesView.getLightColor();
    final float wallsAlpha = attributesView.getWallsAlpha();

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
