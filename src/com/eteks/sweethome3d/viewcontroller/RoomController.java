/*
 * RoomController.java 20 nov. 2008
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
package com.eteks.sweethome3d.viewcontroller;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.ResourceBundle;

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

/**
 * A MVC controller for room view.
 * @author Emmanuel Puybaret
 */
public class RoomController implements Controller {
  /**
   * The properties that may be edited by the view associated to this controller. 
   */
  public enum Property {NAME, AREA_VISIBLE, FLOOR_VISIBLE, FLOOR_COLOR, FLOOR_PAINT, 
      CEILING_VISIBLE, CEILING_COLOR, CEILING_PAINT}
  
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
  private final PropertyChangeSupport propertyChangeSupport;
  private DialogView                  roomView;

  private String    name;
  private Boolean   areaVisible;
  private Boolean   floorVisible;
  private Integer   floorColor;
  private RoomPaint floorPaint;
  private Boolean   ceilingVisible;
  private Integer   ceilingColor;
  private RoomPaint ceilingPaint;

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
      ResourceBundle resource = ResourceBundle.getBundle(RoomController.class.getName());
      this.floorTextureController = new TextureChoiceController(
          resource.getString("floorTextureTitle"), this.preferences, this.viewFactory, this.contentManager);
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
      ResourceBundle resource = ResourceBundle.getBundle(RoomController.class.getName());
      this.ceilingTextureController = new TextureChoiceController(
          resource.getString("ceilingTextureTitle"), this.preferences, this.viewFactory, this.contentManager);
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
   * Updates edited properties from selected rooms in the home edited by this controller.
   */
  protected void updateProperties() {
    List<Room> selectedRooms = Home.getRoomsSubList(this.home.getSelectedItems());
    if (selectedRooms.isEmpty()) {
      setAreaVisible(null); // Nothing to edit
      setFloorColor(null);
      getFloorTextureController().setTexture(null);
      setFloorPaint(null);
      setCeilingColor(null);
      getCeilingTextureController().setTexture(null);
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
    }
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
  public void setAreaVisible(Boolean areaAreaVisible) {
    if (areaAreaVisible != this.areaVisible) {
      Boolean oldAreaVisible = this.areaVisible;
      this.areaVisible = areaAreaVisible;
      this.propertyChangeSupport.firePropertyChange(Property.AREA_VISIBLE.name(), oldAreaVisible, areaAreaVisible);
    }
  }

  /**
   * Returns whether room area is areaAreaVisible or not.
   */
  public Boolean getAreaVisible() {
    return this.areaVisible;
  }

  /**
   * Sets whether room floor is visible or not.
   */
  public void setFloorVisible(Boolean floorFloorVisible) {
    if (floorFloorVisible != this.floorVisible) {
      Boolean oldFloorVisible = this.floorVisible;
      this.floorVisible = floorFloorVisible;
      this.propertyChangeSupport.firePropertyChange(Property.FLOOR_VISIBLE.name(), oldFloorVisible, floorFloorVisible);
    }
  }

  /**
   * Returns whether room floor is floorFloorVisible or not.
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
      Boolean ceilingVisible = getCeilingVisible();
      Integer ceilingColor = getCeilingPaint() == RoomPaint.COLORED
          ? getCeilingColor() : null;
      HomeTexture ceilingTexture = getCeilingPaint() == RoomPaint.TEXTURED
          ? getCeilingTextureController().getTexture() : null;
      
      // Create an array of modified rooms with their current properties values
      ModifiedRoom [] modifiedRooms = new ModifiedRoom [selectedRooms.size()]; 
      for (int i = 0; i < modifiedRooms.length; i++) {
        modifiedRooms [i] = new ModifiedRoom(selectedRooms.get(i));
      }
      // Apply modification
      doModifyRooms(modifiedRooms, name, areaVisible, 
          floorVisible, floorColor, floorTexture, 
          ceilingVisible, ceilingColor, ceilingTexture);       
      if (this.undoSupport != null) {
        UndoableEdit undoableEdit = new RoomsModificationUndoableEdit(this.home, oldSelection,
            modifiedRooms, name, areaVisible, floorColor,
            floorTexture, floorVisible, ceilingColor, ceilingTexture,
            ceilingVisible);
        this.undoSupport.postEdit(undoableEdit);
      }
    }
  }

  /**
   * Undoable edit for rooms modification. This class isn't anonymous to avoid
   * being bound to controller and its view.
   */
  private static class RoomsModificationUndoableEdit extends AbstractUndoableEdit {
    private final Home             home;
    private final List<Selectable> oldSelection;
    private final ModifiedRoom []  modifiedRooms;
    private final String           name;
    private final Boolean          areaVisible;
    private final Integer          floorColor;
    private final HomeTexture      floorTexture;
    private final Boolean          floorVisible;
    private final Integer          ceilingColor;
    private final HomeTexture      ceilingTexture;
    private final Boolean          ceilingVisible;

    private RoomsModificationUndoableEdit(Home home,
                                          List<Selectable> oldSelection,
                                          ModifiedRoom [] modifiedRooms,
                                          String name,
                                          Boolean areaVisible,
                                          Integer floorColor,
                                          HomeTexture floorTexture,
                                          Boolean floorVisible,
                                          Integer ceilingColor,
                                          HomeTexture ceilingTexture,
                                          Boolean ceilingVisible) {
      this.home = home;
      this.oldSelection = oldSelection;
      this.modifiedRooms = modifiedRooms;
      this.name = name;
      this.areaVisible = areaVisible;
      this.floorColor = floorColor;
      this.floorTexture = floorTexture;
      this.floorVisible = floorVisible;
      this.ceilingColor = ceilingColor;
      this.ceilingTexture = ceilingTexture;
      this.ceilingVisible = ceilingVisible;
    }

    @Override
    public void undo() throws CannotUndoException {
      super.undo();
      undoModifyRooms(this.modifiedRooms); 
      this.home.setSelectedItems(this.oldSelection); 
    }

    @Override
    public void redo() throws CannotRedoException {
      super.redo();
      doModifyRooms(this.modifiedRooms, this.name, this.areaVisible, 
          this.floorVisible, this.floorColor, this.floorTexture, 
          this.ceilingVisible, this.ceilingColor, this.ceilingTexture); 
      this.home.setSelectedItems(this.oldSelection); 
    }

    @Override
    public String getPresentationName() {
      return ResourceBundle.getBundle(RoomController.class.getName()).
          getString("undoModifyRoomsName");
    }
  }

  /**
   * Modifies rooms properties with the values in parameter.
   */
  private static void doModifyRooms(ModifiedRoom [] modifiedRooms, 
                             String name, Boolean areaVisible, 
                             Boolean floorVisible, Integer floorColor, HomeTexture floorTexture, 
                             Boolean ceilingVisible, Integer ceilingColor, HomeTexture ceilingTexture) {
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
    }
  }

  /**
   * Restores room properties from the values stored in <code>modifiedRooms</code>.
   */
  private static void undoModifyRooms(ModifiedRoom [] modifiedRooms) {
    for (ModifiedRoom modifiedRoom : modifiedRooms) {
      Room room = modifiedRoom.getRoom();
      room.setName(modifiedRoom.getName());
      room.setAreaVisible(modifiedRoom.isAreaVisible());
      room.setFloorVisible(modifiedRoom.isFloorVisible());
      room.setFloorColor(modifiedRoom.getFloorColor());
      room.setFloorTexture(modifiedRoom.getFloorTexture());
      room.setCeilingVisible(modifiedRoom.isCeilingVisible());
      room.setCeilingColor(modifiedRoom.getCeilingColor());
      room.setCeilingTexture(modifiedRoom.getCeilingTexture());
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
    private final boolean     ceilingVisible;
    private final Integer     ceilingColor;
    private final HomeTexture ceilingTexture;

    public ModifiedRoom(Room room) {
      this.room = room;
      this.name = room.getName();
      this.areaVisible = room.isAreaVisible();
      this.floorVisible = room.isFloorVisible();
      this.floorColor = room.getFloorColor();
      this.floorTexture = room.getFloorTexture();
      this.ceilingVisible = room.isCeilingVisible();
      this.ceilingColor = room.getCeilingColor();
      this.ceilingTexture = room.getCeilingTexture();
    }

    public Room getRoom() {
      return this.room;
    }

    public String getName() {
      return this.name;
    }
    
    public boolean isAreaVisible() {
      return this.areaVisible;
    }
    
    public boolean isFloorVisible() {
      return this.floorVisible;
    }
    
    public Integer getFloorColor() {
      return this.floorColor;
    }
    
    public HomeTexture getFloorTexture() {
      return this.floorTexture;
    }

    public boolean isCeilingVisible() {
      return this.ceilingVisible;
    }
    
    public Integer getCeilingColor() {
      return this.ceilingColor;
    }
    
    public HomeTexture getCeilingTexture() {
      return this.ceilingTexture;
    }
  }
}
