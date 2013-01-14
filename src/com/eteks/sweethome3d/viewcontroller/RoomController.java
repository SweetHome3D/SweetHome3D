/*
 * RoomController.java 20 nov. 2008
 *
 * Sweet Home 3D, Copyright (c) 2008 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;

/**
 * A MVC controller for room view.
 * @author Emmanuel Puybaret
 */
public class RoomController implements Controller {
  /**
   * The properties that may be edited by the view associated to this controller. 
   */
  public enum Property {NAME, AREA_VISIBLE, FLOOR_VISIBLE, FLOOR_COLOR, FLOOR_PAINT, FLOOR_SHININESS,
      CEILING_VISIBLE, CEILING_COLOR, CEILING_PAINT, CEILING_SHININESS,
      SPLIT_SURROUNDING_WALLS, WALL_SIDES_COLOR, WALL_SIDES_PAINT, WALL_SIDES_SHININESS}
  
  /**
   * The possible values for {@linkplain #getFloorPaint() room paint type}.
   */
  public enum RoomPaint {COLORED, TEXTURED} 

  private final Home                  home;
  private final UserPreferences       preferences;
  private final ViewFactory           viewFactory;
  private final ContentManager        contentManager;
  private final UndoableEditSupport   undoSupport;
  private TextureChoiceController     floorTextureController;
  private TextureChoiceController     ceilingTextureController;
  private TextureChoiceController     wallSidesTextureController;
  private final PropertyChangeSupport propertyChangeSupport;
  private DialogView                  roomView;

  private String    name;
  private Boolean   areaVisible;
  private Boolean   floorVisible;
  private Integer   floorColor;
  private RoomPaint floorPaint;
  private Float     floorShininess;
  private Boolean   ceilingVisible;
  private Integer   ceilingColor;
  private RoomPaint ceilingPaint;
  private Float     ceilingShininess;
  private boolean   wallSidesEditable;
  private boolean   splitSurroundingWalls;
  private boolean   splitSurroundingWallsNeeded;
  private Integer   wallSidesColor;
  private RoomPaint wallSidesPaint;
  private Float     wallSidesShininess;

  /**
   * Creates the controller of room view with undo support.  
   */
  public RoomController(final Home home, 
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
    
    updateProperties();
  }

  /**
   * Returns the texture controller of the room floor.
   */
  public TextureChoiceController getFloorTextureController() {
    // Create sub controller lazily only once it's needed
    if (this.floorTextureController == null) {
      this.floorTextureController = new TextureChoiceController(
          this.preferences.getLocalizedString(RoomController.class, "floorTextureTitle"), 
          this.preferences, this.viewFactory, this.contentManager);
      this.floorTextureController.addPropertyChangeListener(TextureChoiceController.Property.TEXTURE,
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              setFloorPaint(RoomPaint.TEXTURED);
            }
          });
    }
    return this.floorTextureController;
  }

  /**
   * Returns the texture controller of the room ceiling.
   */
  public TextureChoiceController getCeilingTextureController() {
    // Create sub controller lazily only once it's needed
    if (this.ceilingTextureController == null) {
      this.ceilingTextureController = new TextureChoiceController(
          this.preferences.getLocalizedString(RoomController.class, "ceilingTextureTitle"), 
          this.preferences, this.viewFactory, this.contentManager);
      this.ceilingTextureController.addPropertyChangeListener(TextureChoiceController.Property.TEXTURE,
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              setCeilingPaint(RoomPaint.TEXTURED);
            }
          });
    }
    return this.ceilingTextureController;
  }

  /**
   * Returns the texture controller of the room wall sides.
   */
  public TextureChoiceController getWallSidesTextureController() {
    // Create sub controller lazily only once it's needed
    if (this.wallSidesTextureController == null) {
      this.wallSidesTextureController = new TextureChoiceController(
          this.preferences.getLocalizedString(RoomController.class, "wallSidesTextureTitle"), 
          this.preferences, this.viewFactory, this.contentManager);
      this.wallSidesTextureController.addPropertyChangeListener(TextureChoiceController.Property.TEXTURE,
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              setWallSidesPaint(RoomPaint.TEXTURED);
            }
          });
    }
    return this.wallSidesTextureController;
  }

  /**
   * Returns the view associated with this controller.
   */
  public DialogView getView() {
    // Create view lazily only once it's needed
    if (this.roomView == null) {
      this.roomView = this.viewFactory.createRoomView(this.preferences, this); 
    }
    return this.roomView;
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
   * Returns <code>true</code> if the given <code>property</code> is editable.
   * Depending on whether a property is editable or not, the view associated to this controller
   * may render it differently.
   * The implementation of this method always returns <code>true</code> except for <code>WALLS</code> properties. 
   */
  public boolean isPropertyEditable(Property property) {
    switch (property) {
      case SPLIT_SURROUNDING_WALLS :
      case WALL_SIDES_COLOR :
      case WALL_SIDES_PAINT :
      case WALL_SIDES_SHININESS :
        return this.wallSidesEditable;
      default :
        return true;
    }
  }
  
  /**
   * Updates edited properties from selected rooms in the home edited by this controller.
   */
  protected void updateProperties() {
    List<Room> selectedRooms = Home.getRoomsSubList(this.home.getSelectedItems());
    if (selectedRooms.isEmpty()) {
      setAreaVisible(null); // Nothing to edit
      setFloorColor(null);
      getFloorTextureController().setTexture(null);
      setFloorPaint(null);
      setFloorShininess(null);
      setCeilingColor(null);
      getCeilingTextureController().setTexture(null);
      setCeilingPaint(null);
      setCeilingShininess(null);
    } else {
      // Search the common properties among selected rooms
      Room firstRoom = selectedRooms.get(0);

      String name = firstRoom.getName();
      if (name != null) {
        for (int i = 1; i < selectedRooms.size(); i++) {
          if (!name.equals(selectedRooms.get(i).getName())) {
            name = null;
            break;
          }
        }
      }
      setName(name);
      
      // Search the common areaVisible value among rooms
      Boolean areaVisible = firstRoom.isAreaVisible();
      for (int i = 1; i < selectedRooms.size(); i++) {
        if (areaVisible != selectedRooms.get(i).isAreaVisible()) {
          areaVisible = null;
          break;
        }
      }
      setAreaVisible(areaVisible);      
      
      // Search the common floorVisible value among rooms
      Boolean floorVisible = firstRoom.isFloorVisible();
      for (int i = 1; i < selectedRooms.size(); i++) {
        if (floorVisible != selectedRooms.get(i).isFloorVisible()) {
          floorVisible = null;
          break;
        }
      }
      setFloorVisible(floorVisible);      
      
      // Search the common floor color among rooms
      Integer floorColor = firstRoom.getFloorColor();
      if (floorColor != null) {
        for (int i = 1; i < selectedRooms.size(); i++) {
          if (!floorColor.equals(selectedRooms.get(i).getFloorColor())) {
            floorColor = null;
            break;
          }
        }
      }
      setFloorColor(floorColor);
      
      // Search the common floor texture among rooms
      HomeTexture floorTexture = firstRoom.getFloorTexture();
      if (floorTexture != null) {
        for (int i = 1; i < selectedRooms.size(); i++) {
          if (!floorTexture.equals(selectedRooms.get(i).getFloorTexture())) {
            floorTexture = null;
            break;
          }
        }
      }
      getFloorTextureController().setTexture(floorTexture);
      
      if (floorColor != null) {
        setFloorPaint(RoomPaint.COLORED);
      } else if (floorTexture != null) {
        setFloorPaint(RoomPaint.TEXTURED);
      } else {
        setFloorPaint(null);
      }

      // Search the common floor shininess among rooms
      Float floorShininess = firstRoom.getFloorShininess();
      for (int i = 1; i < selectedRooms.size(); i++) {
        if (!floorShininess.equals(selectedRooms.get(i).getFloorShininess())) {
          floorShininess = null;
          break;
        }
      }
      setFloorShininess(floorShininess);
      
      // Search the common ceilingVisible value among rooms
      Boolean ceilingVisible = firstRoom.isCeilingVisible();
      for (int i = 1; i < selectedRooms.size(); i++) {
        if (ceilingVisible != selectedRooms.get(i).isCeilingVisible()) {
          ceilingVisible = null;
          break;
        }
      }
      setCeilingVisible(ceilingVisible);      
      
      // Search the common ceiling color among rooms
      Integer ceilingColor = firstRoom.getCeilingColor();
      if (ceilingColor != null) {
        for (int i = 1; i < selectedRooms.size(); i++) {
          if (!ceilingColor.equals(selectedRooms.get(i).getCeilingColor())) {
            ceilingColor = null;
            break;
          }
        }
      }
      setCeilingColor(ceilingColor);
      
      // Search the common ceiling texture among rooms
      HomeTexture ceilingTexture = firstRoom.getCeilingTexture();
      if (ceilingTexture != null) {
        for (int i = 1; i < selectedRooms.size(); i++) {
          if (!ceilingTexture.equals(selectedRooms.get(i).getCeilingTexture())) {
            ceilingTexture = null;
            break;
          }
        }
      }
      getCeilingTextureController().setTexture(ceilingTexture);
      
      if (ceilingColor != null) {
        setCeilingPaint(RoomPaint.COLORED);
      } else if (ceilingTexture != null) {
        setCeilingPaint(RoomPaint.TEXTURED);
      } else {
        setCeilingPaint(null);
      }

      // Search the common ceiling shininess among rooms
      Float ceilingShininess = firstRoom.getCeilingShininess();
      for (int i = 1; i < selectedRooms.size(); i++) {
        if (!ceilingShininess.equals(selectedRooms.get(i).getCeilingShininess())) {
          ceilingShininess = null;
          break;
        }
      }
      setCeilingShininess(ceilingShininess);
    }

    List<WallSide> wallSides = getRoomsWallSides(selectedRooms, null);
    if (wallSides.isEmpty()) {
      this.wallSidesEditable =
      this.splitSurroundingWallsNeeded  =
      this.splitSurroundingWalls = false;
      setWallSidesColor(null);
      setWallSidesPaint(null);
      setWallSidesShininess(null);
    } else {
      this.wallSidesEditable = true;
      this.splitSurroundingWallsNeeded = splitWalls(wallSides, null, null, null);
      this.splitSurroundingWalls = false;
      WallSide firstWallSide = wallSides.get(0);
      
      // Search the common wall color among wall sides        
      Integer wallSidesColor = firstWallSide.getSide() == WallSide.LEFT_SIDE
          ? firstWallSide.getWall().getLeftSideColor()
          : firstWallSide.getWall().getRightSideColor();
      if (wallSidesColor != null) {
        for (int i = 1; i < wallSides.size(); i++) {
          WallSide wallSide = wallSides.get(i);
          if (!wallSidesColor.equals(wallSide.getSide() == WallSide.LEFT_SIDE
                  ? wallSide.getWall().getLeftSideColor()
                  : wallSide.getWall().getRightSideColor())) {
            wallSidesColor = null;
            break;
          }
        }
      }
      setWallSidesColor(wallSidesColor);
      
      // Search the common wall texture among wall sides
      HomeTexture wallSidesTexture = firstWallSide.getSide() == WallSide.LEFT_SIDE
          ? firstWallSide.getWall().getLeftSideTexture()
          : firstWallSide.getWall().getRightSideTexture();
      if (wallSidesTexture != null) {
        for (int i = 1; i < wallSides.size(); i++) {
          WallSide wallSide = wallSides.get(i);
          if (!wallSidesTexture.equals(wallSide.getSide() == WallSide.LEFT_SIDE
                  ? wallSide.getWall().getLeftSideTexture()
                  : wallSide.getWall().getRightSideTexture())) {
            wallSidesTexture = null;
            break;
          }
        }
      }
      getWallSidesTextureController().setTexture(wallSidesTexture);
      
      // Search the common floor shininess among rooms
      Float wallSidesShininess = firstWallSide.getSide() == WallSide.LEFT_SIDE
          ? firstWallSide.getWall().getLeftSideShininess()
          : firstWallSide.getWall().getRightSideShininess();
      if (wallSidesShininess != null) {
        for (int i = 1; i < wallSides.size(); i++) {
          WallSide wallSide = wallSides.get(i);
          if (!wallSidesShininess.equals(wallSide.getSide() == WallSide.LEFT_SIDE
                  ? wallSide.getWall().getLeftSideShininess()
                  : wallSide.getWall().getRightSideShininess())) {
            wallSidesShininess = null;
            break;
          }
        }
      }
      setWallSidesShininess(wallSidesShininess);
    }      
  }
  
  /**
   * Returns the wall sides close to each room of <code>rooms</code>.
   */
  private List<WallSide> getRoomsWallSides(List<Room> rooms, List<WallSide> defaultWallSides) {
    List<WallSide> wallSides = new ArrayList<WallSide>();
    for (Room room : rooms) {
      float [][] points = room.getPoints();
      GeneralPath roomShape = new GeneralPath();
      roomShape.moveTo(points [0][0], points [0][1]);
      for (int i = 1; i < points.length; i++) {
        roomShape.lineTo(points [i][0], points [i][1]);
      }
      roomShape.closePath();
      Area roomArea = new Area(roomShape);

      if (defaultWallSides != null) {
        for (WallSide wallSide : defaultWallSides) {
          if (isRoomItersectingWallSide(wallSide.getWall().getPoints(), wallSide.getSide(), roomArea)) {
            wallSides.add(wallSide);
          }
        }
      } else {
        for (Wall wall : this.home.getWalls()) {
          if (wall.isAtLevel(this.home.getSelectedLevel())) {
            float [][] wallPoints = wall.getPoints();
            if (isRoomItersectingWallSide(wallPoints, WallSide.LEFT_SIDE, roomArea)) {
              wallSides.add(new WallSide(wall, WallSide.LEFT_SIDE));
            }
            if (isRoomItersectingWallSide(wallPoints, WallSide.RIGHT_SIDE, roomArea)) {
              wallSides.add(new WallSide(wall, WallSide.RIGHT_SIDE));
            }
          }
        }
      }
    }
    return wallSides;
  }

  /**
   * Returns <code>true</code> if the wall points on the given <code>wallSide</code>
   * intersects room area.
   */
  private boolean isRoomItersectingWallSide(float [][] wallPoints, int wallSide, Area roomArea) {
    BasicStroke lineStroke = new BasicStroke(2);
    Shape wallSideShape = getWallSideShape(wallPoints, wallSide);
    Area wallSideTestArea = new Area(lineStroke.createStrokedShape(wallSideShape));
    float wallSideTestAreaSurface = getSurface(wallSideTestArea);
    wallSideTestArea.intersect(roomArea);
    if (!wallSideTestArea.isEmpty()) {
      float wallSideIntersectionSurface = getSurface(wallSideTestArea);
      // Take into account only walls that shares a minimum surface with the room
      if (wallSideIntersectionSurface > wallSideTestAreaSurface * 0.02f) { 
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the shape of the side of the given <code>wall</code>. 
   */
  private Shape getWallSideShape(float [][] wallPoints, int wallSide) {
    if (wallPoints.length == 4) {
      if (wallSide == WallSide.LEFT_SIDE) {
        return new Line2D.Float(wallPoints [0][0], wallPoints [0][1], wallPoints [1][0], wallPoints [1][1]);
      } else {
        return new Line2D.Float(wallPoints [2][0], wallPoints [2][1], wallPoints [3][0], wallPoints [3][1]);
      }
    } else {
      float [][] wallSidePoints = new float [wallPoints.length / 2][];
      System.arraycopy(wallPoints, wallSide == WallSide.LEFT_SIDE ? 0 : wallSidePoints.length, 
          wallSidePoints, 0, wallSidePoints.length);
      return getPath(wallSidePoints, false);
    }
  }
  
  /**
   * Returns the shape matching the coordinates in <code>points</code> array.
   */
  private GeneralPath getPath(float [][] points, boolean closedPath) {
    GeneralPath path = new GeneralPath();
    path.moveTo(points [0][0], points [0][1]);
    for (int i = 1; i < points.length; i++) {
      path.lineTo(points [i][0], points [i][1]);
    }
    if (closedPath) {
      path.closePath();
    }
    return path;
  }

  /**
   * Returns the surface of the given <code>area</code>.
   */
  private float getSurface(Area area) {
    // Add the surface of the different polygons of this room
    float surface = 0;
    List<float []> currentPathPoints = new ArrayList<float[]>();
    for (PathIterator it = area.getPathIterator(null); !it.isDone(); ) {
      float [] roomPoint = new float[2];
      switch (it.currentSegment(roomPoint)) {
        case PathIterator.SEG_MOVETO : 
          currentPathPoints.add(roomPoint);
          break;
        case PathIterator.SEG_LINETO : 
          currentPathPoints.add(roomPoint);
          break;
        case PathIterator.SEG_CLOSE :
          float [][] pathPoints = 
              currentPathPoints.toArray(new float [currentPathPoints.size()][]);
          surface += Math.abs(getSignedSurface(pathPoints));
          currentPathPoints.clear();
          break;
      }
      it.next();        
    }
    return surface;
  }
  
  private float getSignedSurface(float areaPoints [][]) {
    // From "Area of a General Polygon" algorithm described in  
    // http://www.davidchandler.com/AreaOfAGeneralPolygon.pdf
    float area = 0;
    for (int i = 1; i < areaPoints.length; i++) {
      area += areaPoints [i][0] * areaPoints [i - 1][1];
      area -= areaPoints [i][1] * areaPoints [i - 1][0];
    }
    area += areaPoints [0][0] * areaPoints [areaPoints.length - 1][1];
    area -= areaPoints [0][1] * areaPoints [areaPoints.length - 1][0];
    return area / 2;
  }

  /**
   * Sets the edited name.
   */
  public void setName(String name) {
    if (name != this.name) {
      String oldName = this.name;
      this.name = name;
      this.propertyChangeSupport.firePropertyChange(Property.NAME.name(), oldName, name);
    }
  }

  /**
   * Returns the edited name.
   */
  public String getName() {
    return this.name;
  }
  
  /**
   * Sets whether room area is visible or not.
   */
  public void setAreaVisible(Boolean areaVisible) {
    if (areaVisible != this.areaVisible) {
      Boolean oldAreaVisible = this.areaVisible;
      this.areaVisible = areaVisible;
      this.propertyChangeSupport.firePropertyChange(Property.AREA_VISIBLE.name(), oldAreaVisible, areaVisible);
    }
  }

  /**
   * Returns whether room area is visible or not.
   */
  public Boolean getAreaVisible() {
    return this.areaVisible;
  }

  /**
   * Sets whether room floor is visible or not.
   */
  public void setFloorVisible(Boolean floorVisible) {
    if (floorVisible != this.floorVisible) {
      Boolean oldFloorVisible = this.floorVisible;
      this.floorVisible = floorVisible;
      this.propertyChangeSupport.firePropertyChange(Property.FLOOR_VISIBLE.name(), oldFloorVisible, floorVisible);
    }
  }

  /**
   * Returns whether room floor is visible or not.
   */
  public Boolean getFloorVisible() {
    return this.floorVisible;
  }

  /**
   * Sets the edited color of the floor.
   */
  public void setFloorColor(Integer floorColor) {
    if (floorColor != this.floorColor) {
      Integer oldFloorColor = this.floorColor;
      this.floorColor = floorColor;
      this.propertyChangeSupport.firePropertyChange(Property.FLOOR_COLOR.name(), oldFloorColor, floorColor);
      
      setFloorPaint(RoomPaint.COLORED);
    }
  }
  
  /**
   * Returns the edited color of the floor.
   */
  public Integer getFloorColor() {
    return this.floorColor;
  }

  /**
   * Sets whether the floor is colored, textured or unknown painted.
   */
  public void setFloorPaint(RoomPaint floorPaint) {
    if (floorPaint != this.floorPaint) {
      RoomPaint oldFloorPaint = this.floorPaint;
      this.floorPaint = floorPaint;
      this.propertyChangeSupport.firePropertyChange(Property.FLOOR_PAINT.name(), oldFloorPaint, floorPaint);
    }
  }
  
  /**
   * Returns whether the floor is colored, textured or unknown painted.
   */
  public RoomPaint getFloorPaint() {
    return this.floorPaint;
  }

  /**
   * Sets the edited shininess of the floor.
   */
  public void setFloorShininess(Float floorShininess) {
    if (floorShininess != this.floorShininess) {
      Float oldFloorShininess = this.floorShininess;
      this.floorShininess = floorShininess;
      this.propertyChangeSupport.firePropertyChange(Property.FLOOR_SHININESS.name(), oldFloorShininess, floorShininess);
    }
  }
  
  /**
   * Returns the edited shininess of the floor.
   */
  public Float getFloorShininess() {
    return this.floorShininess;
  }

  /**
   * Sets whether room ceiling is visible or not.
   */
  public void setCeilingVisible(Boolean ceilingCeilingVisible) {
    if (ceilingCeilingVisible != this.ceilingVisible) {
      Boolean oldCeilingVisible = this.ceilingVisible;
      this.ceilingVisible = ceilingCeilingVisible;
      this.propertyChangeSupport.firePropertyChange(Property.CEILING_VISIBLE.name(), oldCeilingVisible, ceilingCeilingVisible);
    }
  }

  /**
   * Returns whether room ceiling is ceilingCeilingVisible or not.
   */
  public Boolean getCeilingVisible() {
    return this.ceilingVisible;
  }

  /**
   * Sets the edited color of the ceiling.
   */
  public void setCeilingColor(Integer ceilingColor) {
    if (ceilingColor != this.ceilingColor) {
      Integer oldCeilingColor = this.ceilingColor;
      this.ceilingColor = ceilingColor;
      this.propertyChangeSupport.firePropertyChange(Property.CEILING_COLOR.name(), oldCeilingColor, ceilingColor);
      
      setCeilingPaint(RoomPaint.COLORED);
    }
  }
  
  /**
   * Returns the edited color of the ceiling.
   */
  public Integer getCeilingColor() {
    return this.ceilingColor;
  }

  /**
   * Sets whether the ceiling is colored, textured or unknown painted.
   */
  public void setCeilingPaint(RoomPaint ceilingPaint) {
    if (ceilingPaint != this.ceilingPaint) {
      RoomPaint oldCeilingPaint = this.ceilingPaint;
      this.ceilingPaint = ceilingPaint;
      this.propertyChangeSupport.firePropertyChange(Property.CEILING_PAINT.name(), oldCeilingPaint, ceilingPaint);
    }
  }
  
  /**
   * Returns whether the ceiling is colored, textured or unknown painted.
   */
  public RoomPaint getCeilingPaint() {
    return this.ceilingPaint;
  }

  /**
   * Sets the edited shininess of the ceiling.
   */
  public void setCeilingShininess(Float ceilingShininess) {
    if (ceilingShininess != this.ceilingShininess) {
      Float oldCeilingShininess = this.ceilingShininess;
      this.ceilingShininess = ceilingShininess;
      this.propertyChangeSupport.firePropertyChange(Property.CEILING_SHININESS.name(), oldCeilingShininess, ceilingShininess);
    }
  }
  
  /**
   * Returns the edited shininess of the ceiling.
   */
  public Float getCeilingShininess() {
    return this.ceilingShininess;
  }

  /**
   * Returns <code>true</code> if walls around the edited rooms should be split.
   * @since 4.0
   */
  public boolean isSplitSurroundingWalls() {
    return this.splitSurroundingWalls;
  }
  
  /**
   * Sets whether walls around the edited rooms should be split or not.
   * @since 4.0
   */
  public void setSplitSurroundingWalls(boolean splitSurroundingWalls) {
    if (splitSurroundingWalls != this.splitSurroundingWalls) {
      this.splitSurroundingWalls = splitSurroundingWalls;
      this.propertyChangeSupport.firePropertyChange(Property.SPLIT_SURROUNDING_WALLS.name(), !splitSurroundingWalls, splitSurroundingWalls);
    }
  }
  
  /**
   * Returns <code>true</code> if walls around the edited rooms need to be split 
   * to avoid changing the color of wall sides that belong to neighborhood rooms.
   * @since 4.0
   */
  public boolean isSplitSurroundingWallsNeeded() {
    return this.splitSurroundingWallsNeeded;
  }
  
  /**
   * Sets the edited color of the wall sides.
   * @since 4.0
   */
  public void setWallSidesColor(Integer wallSidesColor) {
    if (wallSidesColor != this.wallSidesColor) {
      Integer oldWallSidesColor = this.wallSidesColor;
      this.wallSidesColor = wallSidesColor;
      this.propertyChangeSupport.firePropertyChange(Property.WALL_SIDES_COLOR.name(), oldWallSidesColor, wallSidesColor);
      
      setWallSidesPaint(RoomPaint.COLORED);
    }
  }
  
  /**
   * Returns the edited color of the wall sides.
   * @since 4.0
   */
  public Integer getWallSidesColor() {
    return this.wallSidesColor;
  }

  /**
   * Sets whether the wall sides are colored, textured or unknown painted.
   * @since 4.0
   */
  public void setWallSidesPaint(RoomPaint wallSidesPaint) {
    if (wallSidesPaint != this.wallSidesPaint) {
      RoomPaint oldWallSidesPaint = this.wallSidesPaint;
      this.wallSidesPaint = wallSidesPaint;
      this.propertyChangeSupport.firePropertyChange(Property.WALL_SIDES_PAINT.name(), oldWallSidesPaint, wallSidesPaint);
    }
  }
  
  /**
   * Returns whether the wall sides are colored, textured or unknown painted.
   * @since 4.0
   */
  public RoomPaint getWallSidesPaint() {
    return this.wallSidesPaint;
  }

  /**
   * Sets the edited shininess of the wall sides.
   * @since 4.0
   */
  public void setWallSidesShininess(Float wallSidesShininess) {
    if (wallSidesShininess != this.wallSidesShininess) {
      Float oldWallSidesShininess = this.wallSidesShininess;
      this.wallSidesShininess = wallSidesShininess;
      this.propertyChangeSupport.firePropertyChange(Property.WALL_SIDES_SHININESS.name(), oldWallSidesShininess, wallSidesShininess);
    }
  }
  
  /**
   * Returns the edited shininess of the wall sides.
   * @since 4.0
   */
  public Float getWallSidesShininess() {
    return this.wallSidesShininess;
  }

  /**
   * Controls the modification of selected rooms in edited home.
   */
  public void modifyRooms() {
    List<Selectable> oldSelection = this.home.getSelectedItems(); 
    List<Room> selectedRooms = Home.getRoomsSubList(oldSelection);
    if (!selectedRooms.isEmpty()) {
      String name = getName();
      Boolean areaVisible = getAreaVisible();
      Boolean floorVisible = getFloorVisible();
      Integer floorColor = getFloorPaint() == RoomPaint.COLORED 
          ? getFloorColor() : null;
      HomeTexture floorTexture = getFloorPaint() == RoomPaint.TEXTURED
          ? getFloorTextureController().getTexture() : null;
      Float floorShininess = getFloorShininess();
      Boolean ceilingVisible = getCeilingVisible();
      Integer ceilingColor = getCeilingPaint() == RoomPaint.COLORED
          ? getCeilingColor() : null;
      HomeTexture ceilingTexture = getCeilingPaint() == RoomPaint.TEXTURED
          ? getCeilingTextureController().getTexture() : null;
      Float ceilingShininess = getCeilingShininess();
      Integer wallSidesColor = getWallSidesPaint() == RoomPaint.COLORED 
          ? getWallSidesColor() : null;
      HomeTexture wallSidesTexture = getWallSidesPaint() == RoomPaint.TEXTURED
          ? getWallSidesTextureController().getTexture() : null;
      Float wallSidesShininess = getWallSidesShininess();
      List<WallSide> selectedRoomsWallSides = getRoomsWallSides(selectedRooms, null);
      
      // Create an array of modified rooms with their current properties values
      ModifiedRoom [] modifiedRooms = new ModifiedRoom [selectedRooms.size()]; 
      for (int i = 0; i < modifiedRooms.length; i++) {
        modifiedRooms [i] = new ModifiedRoom(selectedRooms.get(i));
      }
      
      // Apply modification
      List<ModifiedWall> deletedWalls = new ArrayList<ModifiedWall>();
      List<ModifiedWall> addedWalls = new ArrayList<ModifiedWall>();
      List<Selectable> newSelection = new ArrayList<Selectable>(oldSelection);
      if (this.splitSurroundingWalls) {
        if (splitWalls(selectedRoomsWallSides, deletedWalls, addedWalls, newSelection)) {
          this.home.setSelectedItems(newSelection);
          // Update wall sides
          selectedRoomsWallSides = getRoomsWallSides(selectedRooms, selectedRoomsWallSides);
        }
      }
      
      // Create an array of modified wall sides with their current properties values
      ModifiedWallSide [] modifiedWallSides = new ModifiedWallSide [selectedRoomsWallSides.size()]; 
      for (int i = 0; i < modifiedWallSides.length; i++) {
        modifiedWallSides [i] = new ModifiedWallSide(selectedRoomsWallSides.get(i));
      }
      doModifyRoomsAndWallSides(home, modifiedRooms, name, areaVisible, 
          floorVisible, floorColor, floorTexture, floorShininess, 
          ceilingVisible, ceilingColor, ceilingTexture, ceilingShininess,
          modifiedWallSides, wallSidesColor, wallSidesTexture, wallSidesShininess, null, null);
      if (this.undoSupport != null) {
        UndoableEdit undoableEdit = new RoomsAndWallSidesModificationUndoableEdit(
            this.home, this.preferences, oldSelection, newSelection, modifiedRooms, name, areaVisible, 
            floorColor, floorTexture, floorVisible, floorShininess,
            ceilingColor, ceilingTexture, ceilingVisible, ceilingShininess,
            modifiedWallSides, wallSidesColor, wallSidesTexture, wallSidesShininess,
            deletedWalls.toArray(new ModifiedWall [deletedWalls.size()]), 
            addedWalls.toArray(new ModifiedWall [addedWalls.size()]));
        this.undoSupport.postEdit(undoableEdit);
      }
      if (name != null) {
        this.preferences.addAutoCompletionString("RoomName", name);
      }
    }
  }

  /**
   * Splits walls that overfill on other rooms if needed and returns <code>false</code> if the operation wasn't needed. 
   */
  private boolean splitWalls(List<WallSide> wallSides, 
                             List<ModifiedWall> deletedWalls,
                             List<ModifiedWall> addedWalls, 
                             List<Selectable> selectedItems) {
    Map<Wall, ModifiedWall> existingWalls = null;
    List<Wall> newWalls = new ArrayList<Wall>();
    WallSide splitWallSide;
    do {
      splitWallSide = null;
      Wall firstWall = null;
      Wall secondWall = null;
      ModifiedWall deletedWall = null;
      for (Iterator<WallSide> it = wallSides.iterator(); 
          it.hasNext() && splitWallSide == null; ) {
        WallSide wallSide = (WallSide)it.next();
        Wall wall = wallSide.getWall();
        if (wall.getArcExtent() == null) { // Ignore round walls
          Area wallArea = new Area(getPath(wall.getPoints(), true));
          for (WallSide intersectedWallSide : wallSides) {
            Wall intersectedWall = intersectedWallSide.getWall();
            if (wall != intersectedWall) {
              Area intersectedWallArea = new Area(getPath(intersectedWall.getPoints(), true));
              intersectedWallArea.intersect(wallArea);
              if (!intersectedWallArea.isEmpty()
                  && intersectedWallArea.isSingular()) {
                float [] intersection = computeIntersection(
                    wall.getXStart(), wall.getYStart(), wall.getXEnd(), wall.getYEnd(), 
                    intersectedWall.getXStart(), intersectedWall.getYStart(), intersectedWall.getXEnd(), intersectedWall.getYEnd());
                if (intersection != null) {
                  // Clone new walls to copy their characteristics 
                  firstWall = wall.clone();
                  secondWall = wall.clone();
                  
                  // Change split walls end and start point
                  firstWall.setXEnd(intersection [0]);
                  firstWall.setYEnd(intersection [1]);
                  secondWall.setXStart(intersection [0]);
                  secondWall.setYStart(intersection [1]);
                  
                  if (firstWall.getLength() > intersectedWall.getThickness() / 2
                      && secondWall.getLength() > intersectedWall.getThickness() / 2) {
                    // If method is called for test purpose
                    if (deletedWalls == null) { 
                      return true; 
                    }
                    
                    if (existingWalls == null) {
                      // Store all walls at start and end in case there would be more than one change on a wall   
                      existingWalls = new HashMap<Wall, ModifiedWall>(wallSides.size());
                      for (WallSide side : wallSides) {
                        if (!existingWalls.containsKey(side.getWall())) {
                          existingWalls.put(side.getWall(), new ModifiedWall(side.getWall()));
                        }
                      }
                    }
                    
                    deletedWall = existingWalls.get(wall);
                    Wall wallAtStart = wall.getWallAtStart();            
                    if (wallAtStart != null) {
                      firstWall.setWallAtStart(wallAtStart);
                      if (wallAtStart.getWallAtEnd() == wall) {
                        wallAtStart.setWallAtEnd(firstWall);
                      } else {
                        wallAtStart.setWallAtStart(firstWall);
                      }
                    }
                    
                    Wall wallAtEnd = wall.getWallAtEnd();      
                    if (wallAtEnd != null) {
                      secondWall.setWallAtEnd(wallAtEnd);
                      if (wallAtEnd.getWallAtEnd() == wall) {
                        wallAtEnd.setWallAtEnd(secondWall);
                      } else {
                        wallAtEnd.setWallAtStart(secondWall);
                      }
                    }
                    
                    firstWall.setWallAtEnd(secondWall);
                    secondWall.setWallAtStart(firstWall);

                    if (wall.getHeightAtEnd() != null) {
                      Float heightAtIntersecion = wall.getHeight() 
                          + (wall.getHeightAtEnd() - wall.getHeight()) 
                            * (float)Point2D.distance(wall.getXStart(), wall.getYStart(), intersection [0], intersection [1])
                            / wall.getLength();
                      firstWall.setHeightAtEnd(heightAtIntersecion);
                      secondWall.setHeight(heightAtIntersecion);
                    }
                    
                    splitWallSide = wallSide;
                    break;
                  }
                }
              }
            }
          }
        }
      }
      
      if (splitWallSide != null) {    
        newWalls.add(firstWall);
        newWalls.add(secondWall);        
        Wall splitWall = splitWallSide.getWall();
        if (this.home.getWalls().contains(splitWall)) {
          deletedWalls.add(deletedWall);
        } else {
          // Remove from newWalls in case it was a wall split twice
          for (Iterator<Wall> it = newWalls.iterator(); it.hasNext(); ) {
            if (it.next() == splitWall) {
              it.remove();
              break;
            }
          }
        }
        // Update selected items
        if (selectedItems.remove(splitWall)) {
          selectedItems.add(firstWall);
          selectedItems.add(secondWall);
        }
        
        wallSides.remove(splitWallSide);
        wallSides.add(new WallSide(firstWall, splitWallSide.getSide()));
        wallSides.add(new WallSide(secondWall, splitWallSide.getSide()));
        // Update any wall side that reference the same wall
        List<WallSide> sameWallSides = new ArrayList<WallSide>(); 
        for (Iterator<WallSide> it = wallSides.iterator(); it.hasNext(); ) {
          WallSide wallSide = it.next();
          if (wallSide.getWall() == splitWall) {
            it.remove();
            sameWallSides.add(new WallSide(firstWall, wallSide.getSide()));
            sameWallSides.add(new WallSide(secondWall, wallSide.getSide()));
          }
        }
        wallSides.addAll(sameWallSides);
      }
    } while (splitWallSide != null);

    // If method is called for test purpose
    if (deletedWalls == null) { 
      return false; 
    } else {
      for (Wall newWall : newWalls) {      
        this.home.addWall(newWall);
        addedWalls.add(new ModifiedWall(newWall));
      }
      for (ModifiedWall deletedWall : deletedWalls) {
        this.home.deleteWall(deletedWall.getWall());
      }
      return !deletedWalls.isEmpty();
    }
  }

  /** 
   * Returns the intersection between a line segment and a second line.
   */
  private float [] computeIntersection(float xPoint1, float yPoint1, float xPoint2, float yPoint2, 
                                       float xPoint3, float yPoint3, float xPoint4, float yPoint4) {    
    float x = xPoint1;
    float y = yPoint1;
    float alpha1 = (yPoint2 - yPoint1) / (xPoint2 - xPoint1);
    float alpha2 = (yPoint4 - yPoint3) / (xPoint4 - xPoint3);
    // If the two lines are not parallel
    if (alpha1 != alpha2) {
      // If first line is vertical
      if (Math.abs(alpha1) > 4000)  {
        if (Math.abs(alpha2) < 4000) {
          x = xPoint1;
          float beta2  = yPoint4 - alpha2 * xPoint4;
          y = alpha2 * x + beta2;
        }
      // If second line is vertical
      } else if (Math.abs(alpha2) > 4000) {
        if (Math.abs(alpha1) < 4000) {
          x = xPoint3;
          float beta1  = yPoint2 - alpha1 * xPoint2;
          y = alpha1 * x + beta1;
        }
      } else {
        boolean sameSignum = Math.signum(alpha1) == Math.signum(alpha2);
        if ((sameSignum && (Math.abs(alpha1) > Math.abs(alpha2)   ? alpha1 / alpha2   : alpha2 / alpha1) > 1.0001)
            || (!sameSignum && Math.abs(alpha1 - alpha2) > 1E-5)) {
          float beta1  = yPoint2 - alpha1 * xPoint2;
          float beta2  = yPoint4 - alpha2 * xPoint4;
          x = (beta2 - beta1) / (alpha1 - alpha2);
          y = alpha1 * x + beta1;
        }
      } 
    }
    if (Line2D.ptSegDistSq(xPoint1, yPoint1, xPoint2, yPoint2, x, y) < 1E-7
        && (Math.abs(xPoint1 - x) > 1E-4
            || Math.abs(yPoint1 - y) > 1E-4)
        && (Math.abs(xPoint2 - x) > 1E-4
            || Math.abs(yPoint2 - y) > 1E-4)) {
      return new float [] {x, y};
    } else {
      return null;
    }
  }

  /**
   * Undoable edit for rooms modification. This class isn't anonymous to avoid
   * being bound to controller and its view.
   */
  private static class RoomsAndWallSidesModificationUndoableEdit extends AbstractUndoableEdit {
    private final Home                home;
    private final UserPreferences     preferences;
    private final List<Selectable>    oldSelection;
    private final List<Selectable>    newSelection;
    private final ModifiedRoom []     modifiedRooms;
    private final String              name;
    private final Boolean             areaVisible;
    private final Integer             floorColor;
    private final HomeTexture         floorTexture;
    private final Boolean             floorVisible;
    private final Float               floorShininess;
    private final Integer             ceilingColor;
    private final HomeTexture         ceilingTexture;
    private final Boolean             ceilingVisible;
    private final Float               ceilingShininess;
    private final ModifiedWallSide [] modifiedWallSides;
    private final Integer             wallSidesColor;
    private final HomeTexture         wallSidesTexture;
    private final Float               wallSidesShininess;
    private final ModifiedWall []     deletedWalls;
    private final ModifiedWall []     addedWalls;

    private RoomsAndWallSidesModificationUndoableEdit(Home home,
                                          UserPreferences preferences,
                                          List<Selectable> oldSelection,
                                          List<Selectable> newSelection, 
                                          ModifiedRoom [] modifiedRooms,
                                          String name,
                                          Boolean areaVisible,
                                          Integer floorColor,
                                          HomeTexture floorTexture,
                                          Boolean floorVisible,
                                          Float floorShininess,
                                          Integer ceilingColor,
                                          HomeTexture ceilingTexture,
                                          Boolean ceilingVisible,
                                          Float ceilingShininess,
                                          ModifiedWallSide [] modifiedWallSides,
                                          Integer wallSidesColor,
                                          HomeTexture wallSidesTexture,
                                          Float wallSidesShininess, 
                                          ModifiedWall [] deletedWalls, 
                                          ModifiedWall [] addedWalls) {
      this.home = home;
      this.preferences = preferences;
      this.oldSelection = oldSelection;
      this.newSelection = newSelection;
      this.modifiedRooms = modifiedRooms;
      this.name = name;
      this.areaVisible = areaVisible;
      this.floorColor = floorColor;
      this.floorTexture = floorTexture;
      this.floorVisible = floorVisible;
      this.floorShininess = floorShininess;
      this.ceilingColor = ceilingColor;
      this.ceilingTexture = ceilingTexture;
      this.ceilingVisible = ceilingVisible;
      this.ceilingShininess = ceilingShininess;
      this.modifiedWallSides = modifiedWallSides;
      this.wallSidesColor = wallSidesColor;
      this.wallSidesTexture = wallSidesTexture;
      this.wallSidesShininess = wallSidesShininess;
      this.deletedWalls = deletedWalls;
      this.addedWalls = addedWalls;
    }

    @Override
    public void undo() throws CannotUndoException {
      super.undo();
      undoModifyRoomsAndWallSides(this.home, this.modifiedRooms, this.modifiedWallSides, this.deletedWalls, this.addedWalls); 
      this.home.setSelectedItems(this.oldSelection); 
    }

    @Override
    public void redo() throws CannotRedoException {
      super.redo();
      doModifyRoomsAndWallSides(this.home,
          this.modifiedRooms, this.name, this.areaVisible, 
          this.floorVisible, this.floorColor, this.floorTexture, this.floorShininess, 
          this.ceilingVisible, this.ceilingColor, this.ceilingTexture, this.ceilingShininess,
          this.modifiedWallSides, this.wallSidesColor, this.wallSidesTexture, this.wallSidesShininess,
          this.deletedWalls, this.addedWalls); 
      this.home.setSelectedItems(this.newSelection); 
    }

    @Override
    public String getPresentationName() {
      return this.preferences.getLocalizedString(RoomController.class, "undoModifyRoomsName");
    }
  }

  /**
   * Modifies rooms and walls properties with the values in parameter.
   */
  private static void doModifyRoomsAndWallSides(Home home, ModifiedRoom [] modifiedRooms, 
                             String name, Boolean areaVisible, 
                             Boolean floorVisible, Integer floorColor, HomeTexture floorTexture, Float floorShininess,
                             Boolean ceilingVisible, Integer ceilingColor, HomeTexture ceilingTexture, Float ceilingShininess,
                             ModifiedWallSide [] modifiedWallSides, 
                             Integer wallSidesColor, HomeTexture wallSidesTexture, Float wallSidesShininess, 
                             ModifiedWall [] deletedWalls, 
                             ModifiedWall [] addedWalls) {
    if (deletedWalls != null) {
      for (ModifiedWall newWall : addedWalls) {
        newWall.reset();
        home.addWall(newWall.getWall());
      }
      for (ModifiedWall deletedWall : deletedWalls) {
        home.deleteWall(deletedWall.getWall());
      }
    }
    for (ModifiedRoom modifiedRoom : modifiedRooms) {
      Room room = modifiedRoom.getRoom();
      if (name != null) {
        room.setName(name);
      }
      if (areaVisible != null) {
        room.setAreaVisible(areaVisible);
      }
      if (floorVisible != null) {
        room.setFloorVisible(floorVisible);
      }
      if (floorTexture != null) {
        room.setFloorTexture(floorTexture);
        room.setFloorColor(null);
      } else if (floorColor != null) {
        room.setFloorColor(floorColor);
        room.setFloorTexture(null);
      }
      if (floorShininess != null) {
        room.setFloorShininess(floorShininess);
      }
      if (ceilingVisible != null) {
        room.setCeilingVisible(ceilingVisible);
      }
      if (ceilingTexture != null) {
        room.setCeilingTexture(ceilingTexture);
        room.setCeilingColor(null);
      } else if (ceilingColor != null) {
        room.setCeilingColor(ceilingColor);
        room.setCeilingTexture(null);
      }
      if (ceilingShininess != null) {
        room.setCeilingShininess(ceilingShininess);
      }
    }
    for (ModifiedWallSide modifiedWallSide : modifiedWallSides) {
      WallSide wallSide = modifiedWallSide.getWallSide();
      if (wallSidesColor != null) {
        if (wallSide.getSide() == WallSide.LEFT_SIDE) {
          wallSide.getWall().setLeftSideColor(wallSidesColor);
        } else {
          wallSide.getWall().setRightSideColor(wallSidesColor);
        }
      }
      
      if (wallSidesTexture != null || wallSidesColor != null) {
        if (wallSide.getSide() == WallSide.LEFT_SIDE) {
          wallSide.getWall().setLeftSideTexture(wallSidesTexture);
        } else {
          wallSide.getWall().setRightSideTexture(wallSidesTexture);
        }
      }

      if (wallSidesShininess != null) {
        if (wallSide.getSide() == WallSide.LEFT_SIDE) {
          wallSide.getWall().setLeftSideShininess(wallSidesShininess);
        } else {
          wallSide.getWall().setRightSideShininess(wallSidesShininess);
        }
      }
    }
  }

  /**
   * Restores room properties from the values stored in <code>modifiedRooms</code> and <code>modifiedWallSides</code>.
   */
  private static void undoModifyRoomsAndWallSides(Home home, 
                                                  ModifiedRoom [] modifiedRooms,
                                                  ModifiedWallSide [] modifiedWallSides, 
                                                  ModifiedWall [] deletedWalls, 
                                                  ModifiedWall [] addedWalls) {
    for (ModifiedRoom modifiedRoom : modifiedRooms) {
      modifiedRoom.reset();
    }
    for (ModifiedWallSide modifiedWallSide : modifiedWallSides) {
      modifiedWallSide.reset();
    }
    for (ModifiedWall newWall : addedWalls) {
      home.deleteWall(newWall.getWall());
    }
    for (ModifiedWall deletedWall : deletedWalls) {
      deletedWall.reset();
      home.addWall(deletedWall.getWall());
    }
  }
  
  /**
   * Stores the current properties values of a modified room.
   */
  private static final class ModifiedRoom {
    private final Room        room;
    private final String      name;
    private final boolean     areaVisible;
    private final boolean     floorVisible;
    private final Integer     floorColor;
    private final HomeTexture floorTexture;
    private final float       floorShininess;
    private final boolean     ceilingVisible;
    private final Integer     ceilingColor;
    private final HomeTexture ceilingTexture;
    private final float       ceilingShininess;

    public ModifiedRoom(Room room) {
      this.room = room;
      this.name = room.getName();
      this.areaVisible = room.isAreaVisible();
      this.floorVisible = room.isFloorVisible();
      this.floorColor = room.getFloorColor();
      this.floorTexture = room.getFloorTexture();
      this.floorShininess = room.getFloorShininess();
      this.ceilingVisible = room.isCeilingVisible();
      this.ceilingColor = room.getCeilingColor();
      this.ceilingTexture = room.getCeilingTexture();
      this.ceilingShininess = room.getCeilingShininess();
    }

    public Room getRoom() {
      return this.room;
    }
    
    public void reset() {
      this.room.setName(this.name);
      this.room.setAreaVisible(this.areaVisible);
      this.room.setFloorVisible(this.floorVisible);
      this.room.setFloorColor(this.floorColor);
      this.room.setFloorTexture(this.floorTexture);
      this.room.setFloorShininess(this.floorShininess);
      this.room.setCeilingVisible(this.ceilingVisible);
      this.room.setCeilingColor(this.ceilingColor);
      this.room.setCeilingTexture(this.ceilingTexture);
      this.room.setCeilingShininess(this.ceilingShininess);
    }    
  }

  /**
   * A wall side.  
   */
  private class WallSide {
    public static final int LEFT_SIDE = 0;
    public static final int RIGHT_SIDE = 1;
    
    private Wall          wall;
    private int           side;
    private final Wall    wallAtStart;
    private final Wall    wallAtEnd;
    private final boolean joinedAtEndOfWallAtStart;
    private final boolean joinedAtStartOfWallAtEnd;
    
    public WallSide(Wall wall, int side) {
      this.wall = wall;
      this.side = side;
      this.wallAtStart = wall.getWallAtStart();
      this.joinedAtEndOfWallAtStart =
          this.wallAtStart != null
          && this.wallAtStart.getWallAtEnd() == wall;
      this.wallAtEnd = wall.getWallAtEnd();
      this.joinedAtStartOfWallAtEnd =
          this.wallAtEnd != null
          && wallAtEnd.getWallAtStart() == wall;
    }
    
    public Wall getWall() {
      return this.wall;
    }
    
    public int getSide() {
      return this.side;
    }
    
    public Wall getWallAtStart() {
      return this.wallAtStart;
    }
    
    public Wall getWallAtEnd() {
      return this.wallAtEnd;
    }

    public boolean isJoinedAtEndOfWallAtStart() {
      return this.joinedAtEndOfWallAtStart;
    }

    public boolean isJoinedAtStartOfWallAtEnd() {
      return this.joinedAtStartOfWallAtEnd;
    }
  }

  /**
   * A modified wall.  
   */
  private class ModifiedWall {
    private Wall          wall;
    private final Wall    wallAtStart;
    private final Wall    wallAtEnd;
    private final boolean joinedAtEndOfWallAtStart;
    private final boolean joinedAtStartOfWallAtEnd;
    
    public ModifiedWall(Wall wall) {
      this.wall = wall;
      this.wallAtStart = wall.getWallAtStart();
      this.joinedAtEndOfWallAtStart =
          this.wallAtStart != null
          && this.wallAtStart.getWallAtEnd() == wall;
      this.wallAtEnd = wall.getWallAtEnd();
      this.joinedAtStartOfWallAtEnd =
          this.wallAtEnd != null
          && wallAtEnd.getWallAtStart() == wall;
    }
    
    public Wall getWall() {
      return this.wall;
    }
    
    public void reset() {
      if (this.wallAtStart != null) {
        this.wall.setWallAtStart(this.wallAtStart);
        if (this.joinedAtEndOfWallAtStart) {
          this.wallAtStart.setWallAtEnd(this.wall);
        } else {
          this.wallAtStart.setWallAtStart(this.wall);
        }
      }
      if (this.wallAtEnd != null) {
        this.wall.setWallAtEnd(wallAtEnd);
        if (this.joinedAtStartOfWallAtEnd) {
          this.wallAtEnd.setWallAtStart(this.wall);
        } else {
          this.wallAtEnd.setWallAtEnd(this.wall);
        }
      }
    }    
  }

  /**
   * Stores the current properties values of a modified wall side.
   */
  private static final class ModifiedWallSide {
    private final WallSide    wallSide;
    private final Integer     wallColor;
    private final HomeTexture wallTexture;
    private final Float       wallShininess;

    public ModifiedWallSide(WallSide wallSide) {
      this.wallSide = wallSide;
      if (wallSide.getSide() == WallSide.LEFT_SIDE) {
        this.wallColor = wallSide.getWall().getLeftSideColor();
        this.wallTexture = wallSide.getWall().getLeftSideTexture();
        this.wallShininess = wallSide.getWall().getLeftSideShininess();
      } else {
        this.wallColor = wallSide.getWall().getRightSideColor();
        this.wallTexture = wallSide.getWall().getRightSideTexture();
        this.wallShininess = wallSide.getWall().getRightSideShininess();
      }
    }

    public WallSide getWallSide() {
      return this.wallSide;
    }
    
    public void reset() {
      if (this.wallSide.getSide() == WallSide.LEFT_SIDE) {
        this.wallSide.getWall().setLeftSideColor(this.wallColor);
        this.wallSide.getWall().setLeftSideTexture(this.wallTexture);
        this.wallSide.getWall().setLeftSideShininess(this.wallShininess);
      } else {
        this.wallSide.getWall().setRightSideColor(this.wallColor);
        this.wallSide.getWall().setRightSideTexture(this.wallTexture);
        this.wallSide.getWall().setRightSideShininess(this.wallShininess);
      }
      Wall wall = wallSide.getWall();
      Wall wallAtStart = wallSide.getWallAtStart();
      if (wallAtStart != null) {
        wall.setWallAtStart(wallAtStart);
        if (wallSide.isJoinedAtEndOfWallAtStart()) {
          wallAtStart.setWallAtEnd(wall);
        } else {
          wallAtStart.setWallAtStart(wall);
        }
      }
      Wall wallAtEnd = wallSide.getWallAtEnd();
      if (wallAtEnd != null) {
        wall.setWallAtEnd(wallAtEnd);
        if (wallSide.isJoinedAtStartOfWallAtEnd()) {
          wallAtEnd.setWallAtStart(wall);
        } else {
          wallAtEnd.setWallAtEnd(wall);
        }
      }
    }    
  }
}
