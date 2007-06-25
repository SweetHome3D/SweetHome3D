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
package com.eteks.sweethome3d.swing;

import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.ObserverCamera;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * A MVC controller for home 3D attributes view.
 * @author Emmanuel Puybaret
 */
public class Home3DAttributesController {
  private Home                home;
  private UndoableEditSupport undoSupport;
  private ResourceBundle      resource;
  private JComponent          wallView;

  /**
   * Creates the controller of home furniture view with undo support.
   */
  public Home3DAttributesController(Home home, 
                        UserPreferences preferences, 
                        UndoableEditSupport undoSupport) {
    this.home = home;
    this.undoSupport = undoSupport;
    this.resource    = ResourceBundle.getBundle(
        Home3DAttributesController.class.getName());
    this.wallView = new Home3DAttributesPanel(home, preferences, this); 
    ((Home3DAttributesPanel)this.wallView).displayView();
  }

  /**
   * Returns the view associated with this controller.
   */
  public JComponent getView() {
    return this.wallView;
  }

  /**
   * Controls the modification of of home.
   */
  public void modifyHome() {
    Home3DAttributesPanel attributesPanel = (Home3DAttributesPanel)getView();
    final float observerCameraFieldOfView = attributesPanel.getObserverCameraFieldOfView();
    final float observerCameraZ = attributesPanel.getObserverCameraHeight() * 14 / 15;
    final int   groundColor = attributesPanel.getGroundColor();
    final int   skyColor = attributesPanel.getSkyColor();
    final int   lightColor  = attributesPanel.getLightColor();
    final float wallsAlpha = attributesPanel.getWallsAlpha();

    final float oldObserverCameraFieldOfView = this.home.getObserverCamera().getFieldOfView();
    final float oldObserverCameraZ = this.home.getObserverCamera().getZ();
    final int   oldGroundColor = this.home.getGroundColor();
    final int   oldSkyColor = this.home.getSkyColor();
    final int   oldLightColor  = this.home.getLightColor();
    final float oldWallsAlpha = this.home.getWallsAlpha();
    
    // Apply modification
    doModifyHome(observerCameraFieldOfView, observerCameraZ, 
        groundColor, skyColor, lightColor, wallsAlpha); 
    if (this.undoSupport != null) {
      UndoableEdit undoableEdit = new AbstractUndoableEdit() {
        @Override
        public void undo() throws CannotUndoException {
          super.undo();
          doModifyHome(oldObserverCameraFieldOfView, oldObserverCameraZ, 
              oldGroundColor, oldSkyColor, oldLightColor, oldWallsAlpha); 
        }
        
        @Override
        public void redo() throws CannotRedoException {
          super.redo();
          doModifyHome(observerCameraFieldOfView, observerCameraZ, 
              groundColor, skyColor, lightColor, wallsAlpha); 
        }
        
        @Override
        public String getPresentationName() {
          return resource.getString("undoModify3DAttributes");
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
                            int groundColor, int skyColor, 
                            int lightColor, float wallsAlpha) {
    ObserverCamera observerCamera = this.home.getObserverCamera();
    this.home.setCameraFieldOfView(observerCamera, observerCameraFieldOfView);
    this.home.setCameraLocation(observerCamera, observerCamera.getX(), observerCamera.getY(), observerCameraZ);
    this.home.setGroundColor(groundColor);
    this.home.setSkyColor(skyColor);
    this.home.setLightColor(lightColor);
    this.home.setWallsAlpha(wallsAlpha);
  }
}
