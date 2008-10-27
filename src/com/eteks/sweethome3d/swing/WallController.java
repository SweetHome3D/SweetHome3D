/*
 * WallController.java 30 mai 07
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

import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;

/**
 * A MVC controller for wall view.
 * @author Emmanuel Puybaret
 */
public class WallController {
  private Home                    home;
  private UserPreferences         preferences;
  private ContentManager          contentManager;
  private UndoableEditSupport     undoSupport;
  private TextureChoiceController leftSideTextureController;
  private TextureChoiceController rightSideTextureController;
  private JComponent              wallView;

  /**
   * Creates the controller of wall view with undo support.
   */
  public WallController(Home home, 
                        UserPreferences preferences,
                        ContentManager contentManager, 
                        UndoableEditSupport undoSupport) {
    this.home = home;
    this.preferences = preferences;
    this.contentManager = contentManager;
    this.undoSupport = undoSupport;
  }

  /**
   * Returns the texture controller of the wall left side.
   */
  public TextureChoiceController getLeftSideTextureController() {
    // Create sub controller lazily only once it's needed
    if (this.leftSideTextureController == null) {
      ResourceBundle resource = ResourceBundle.getBundle(WallController.class.getName());
      this.leftSideTextureController = new TextureChoiceController(
          resource.getString("leftSideTextureTitle"), this.preferences, this.contentManager);
    }
    return this.leftSideTextureController;
  }

  /**
   * Returns the texture controller of the wall right side.
   */
  public TextureChoiceController getRightSideTextureController() {
    // Create sub controller lazily only once it's needed
    if (this.rightSideTextureController == null) {
      ResourceBundle resource = ResourceBundle.getBundle(WallController.class.getName());
      this.rightSideTextureController = new TextureChoiceController(
          resource.getString("rightSideTextureTitle"), this.preferences, this.contentManager);
    }
    return this.rightSideTextureController;
  }

  /**
   * Returns the view associated with this controller.
   */
  public JComponent getView() {
    // Create view lazily only once it's needed
    if (this.wallView == null) {
      this.wallView = new WallPanel(this.home, this.preferences, this); 
    }
    return this.wallView;
  }

  /**
   * Displays the view controlled by this controller.
   */
  public void displayView(JComponent parentView) {
    ((WallPanel)getView()).displayView(parentView);
  }

  /**
   * Controls the modification of selected walls.
   */
  public void modifySelection() {
    final List<Selectable> oldSelection = this.home.getSelectedItems(); 
    List<Wall> selectedWalls = Home.getWallsSubList(oldSelection);
    if (!selectedWalls.isEmpty()) {
      WallPanel wallPanel = (WallPanel)getView();
      final Float xStart = wallPanel.getWallXStart();
      final Float yStart = wallPanel.getWallYStart();
      final Float xEnd = wallPanel.getWallXEnd();
      final Float yEnd = wallPanel.getWallYEnd();
      final Integer leftSideColor = wallPanel.getWallLeftSideColor();
      final HomeTexture leftSideTexture = wallPanel.getWallLeftSideTexture();
      final Integer rightSideColor = wallPanel.getWallRightSideColor();
      final HomeTexture rightSideTexture = wallPanel.getWallRightSideTexture();
      final Float thickness = wallPanel.getWallThickness();
      final Float height = wallPanel.getWallHeight();
      final Float heightAtEnd = wallPanel.getWallHeightAtEnd();
      
      // Create an array of modified walls with their current properties values
      final ModifiedWall [] modifiedWalls = 
          new ModifiedWall [selectedWalls.size()]; 
      for (int i = 0; i < modifiedWalls.length; i++) {
        modifiedWalls [i] = new ModifiedWall(selectedWalls.get(i));
      }
      // Apply modification
      doModifyWalls(modifiedWalls, xStart, yStart, xEnd, yEnd, 
          leftSideColor, leftSideTexture, rightSideColor, rightSideTexture, 
          height, heightAtEnd, thickness);       
      if (this.undoSupport != null) {
        UndoableEdit undoableEdit = new AbstractUndoableEdit() {
          @Override
          public void undo() throws CannotUndoException {
            super.undo();
            undoModifyWalls(modifiedWalls); 
            home.setSelectedItems(oldSelection); 
          }
          
          @Override
          public void redo() throws CannotRedoException {
            super.redo();
            doModifyWalls(modifiedWalls, xStart, yStart, xEnd, yEnd, 
                leftSideColor, leftSideTexture, rightSideColor, rightSideTexture, 
                height, heightAtEnd, thickness); 
            home.setSelectedItems(oldSelection); 
          }
          
          @Override
          public String getPresentationName() {
            return ResourceBundle.getBundle(WallController.class.getName()).
                getString("undoModifyWallsName");
          }
        };
        this.undoSupport.postEdit(undoableEdit);
      }
    }
  }

  /**
   * Modifies walls properties with the values in parameter.
   */
  private void doModifyWalls(ModifiedWall [] modifiedWalls, 
                             Float xStart, Float yStart, Float xEnd, Float yEnd,
                             Integer leftSideColor, HomeTexture leftSideTexture, 
                             Integer rightSideColor, HomeTexture rightSideTexture,
                             Float height, Float heightAtEnd, Float thickness) {
    for (ModifiedWall modifiedWall : modifiedWalls) {
      Wall wall = modifiedWall.getWall();
      // Modify wall coordinates if modifiedWalls contains only one wall
      if (modifiedWalls.length == 1) {
        moveWallPoints(wall, xStart, yStart, xEnd, yEnd);
      }
      if (leftSideTexture != null) {
        wall.setLeftSideTexture(leftSideTexture);
        wall.setLeftSideColor(null);
      } else if (leftSideColor != null) {
        wall.setLeftSideColor(leftSideColor);
        wall.setLeftSideTexture(null);
      }
      if (rightSideTexture != null) {
        wall.setRightSideTexture(rightSideTexture);
        wall.setRightSideColor(null);
      } else if (rightSideColor != null) {
        wall.setRightSideColor(rightSideColor);
        wall.setRightSideTexture(null);
      }
      if (height != null) {
        wall.setHeight(height);
        if (heightAtEnd != null) {
          if (heightAtEnd.equals(height)) {
            wall.setHeightAtEnd(null);
          } else {
            wall.setHeightAtEnd(heightAtEnd);
          }
        }
      }
      wall.setThickness(thickness != null 
          ? thickness.floatValue() 
          : wall.getThickness());
    }
  }

  /**
   * Restores wall properties from the values stored in <code>modifiedWalls</code>.
   */
  private void undoModifyWalls(ModifiedWall [] modifiedWalls) {
    for (ModifiedWall modifiedWall : modifiedWalls) {
      Wall wall = modifiedWall.getWall();
      // Modify wall coordinates if modifiedWalls contains only one wall
      if (modifiedWalls.length == 1) {
        moveWallPoints(wall, modifiedWall.getXStart(), modifiedWall.getYStart(),
            modifiedWall.getXEnd(), modifiedWall.getYEnd());
      }
      wall.setLeftSideColor(modifiedWall.getLeftSideColor());
      wall.setLeftSideTexture(modifiedWall.getLeftSideTexture());
      wall.setRightSideColor(modifiedWall.getRightSideColor());
      wall.setRightSideTexture(modifiedWall.getRightSideTexture());
      wall.setThickness(modifiedWall.getThickness());
      wall.setHeight(modifiedWall.getHeight());
      wall.setHeightAtEnd(modifiedWall.getHeightAtEnd());
    }
  }
  
  private void moveWallPoints(Wall wall, float xStart, float yStart, float xEnd, float yEnd) {
    wall.setXStart(xStart);
    wall.setYStart(yStart);
    Wall wallAtStart = wall.getWallAtStart();
    // If wall is joined to a wall at its start 
    if (wallAtStart != null) {
      // Move the wall start point or end point
      if (wallAtStart.getWallAtStart() == wall) {
        wallAtStart.setXStart(xStart);
        wallAtStart.setYStart(yStart);
      } else if (wallAtStart.getWallAtEnd() == wall) {
        wallAtStart.setXEnd(xStart);
        wallAtStart.setYEnd(yStart);
      }
    }
    wall.setXEnd(xEnd);
    wall.setYEnd(yEnd);
    Wall wallAtEnd = wall.getWallAtEnd();
    // If wall is joined to a wall at its end  
    if (wallAtEnd != null) {
      // Move the wall start point or end point
      if (wallAtEnd.getWallAtStart() == wall) {
        wallAtEnd.setXStart(xEnd);
        wallAtEnd.setYStart(yEnd);
      } else if (wallAtEnd.getWallAtEnd() == wall) {
        wallAtEnd.setXEnd(xEnd);
        wallAtEnd.setYEnd(yEnd);
      }
    }
  }

  /**
   * Stores the current properties values of a modified wall.
   */
  private static final class ModifiedWall {
    private final Wall        wall;
    private final float       xStart;
    private final float       yStart;
    private final float       xEnd;
    private final float       yEnd;
    private final Integer     leftSideColor;
    private final HomeTexture leftSideTexture;
    private final Integer     rightSideColor;
    private final HomeTexture rightSideTexture;
    private final float       thickness;
    private final Float       height;
    private final Float       heightAtEnd;

    public ModifiedWall(Wall wall) {
      this.wall = wall;
      this.xStart = wall.getXStart();
      this.yStart = wall.getYStart();
      this.xEnd = wall.getXEnd();
      this.yEnd = wall.getYEnd();
      this.leftSideColor = wall.getLeftSideColor();
      this.leftSideTexture = wall.getLeftSideTexture();
      this.rightSideColor = wall.getRightSideColor();
      this.rightSideTexture = wall.getRightSideTexture();
      this.thickness = wall.getThickness();
      this.height = wall.getHeight();
      this.heightAtEnd = wall.getHeightAtEnd();
    }

    public Wall getWall() {
      return this.wall;
    }

    public Integer getLeftSideColor() {
      return this.leftSideColor;
    }
    
    public HomeTexture getLeftSideTexture() {
      return this.leftSideTexture;
    }

    public Integer getRightSideColor() {
      return this.rightSideColor;
    }
    
    public HomeTexture getRightSideTexture() {
      return this.rightSideTexture;
    }

    public float getThickness() {
      return this.thickness;
    }

    public float getXEnd() {
      return this.xEnd;
    }

    public float getXStart() {
      return this.xStart;
    }

    public float getYEnd() {
      return this.yEnd;
    }

    public float getYStart() {
      return this.yStart;
    }    

    public Float getHeight() {
      return this.height;
    }
    
    public Float getHeightAtEnd() {
      return this.heightAtEnd;
    }
  }
}
