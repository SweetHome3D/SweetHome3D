/*
 * LevelController.java 27 oct 2011
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
package com.eteks.sweethome3d.viewcontroller;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.Level;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * A MVC controller for home levels view.
 * @author Emmanuel Puybaret
 */
public class LevelController implements Controller {
  /**
   * The properties that may be edited by the view associated to this controller. 
   */
  public enum Property {NAME, ELEVATION, FLOOR_THICKNESS, HEIGHT}
  
  private final Home                  home;
  private final UserPreferences       preferences;
  private final ViewFactory           viewFactory;
  private final UndoableEditSupport   undoSupport;
  private final PropertyChangeSupport propertyChangeSupport;
  private DialogView                  homeLevelView;

  private String  name;
  private Float   elevation;
  private Float   floorThickness;
  private Float   height;
  
  /**
   * Creates the controller of home levels view with undo support.
   */
  public LevelController(Home home, 
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
    if (this.homeLevelView == null) {
      this.homeLevelView = this.viewFactory.createLevelView(this.preferences, this); 
    }
    return this.homeLevelView;
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
   * Updates edited properties from selected level in the home edited by this controller.
   */
  protected void updateProperties() {
    Level selectedLevel = this.home.getSelectedLevel();
    if (selectedLevel == null) {
      setName(null); // Nothing to edit
      setElevation(null);
      setFloorThickness(null);
      setHeight(null);
    } else {
      setName(selectedLevel.getName());
      setElevation(selectedLevel.getElevation());
      setFloorThickness(selectedLevel.getFloorThickness());
      setHeight(selectedLevel.getHeight());
    }
  }  
  
  /**
   * Returns <code>true</code> if the given <code>property</code> is editable.
   * Depending on whether a property is editable or not, the view associated to this controller
   * may render it differently.
   * The implementation of this method always returns <code>true</code> except 
   * for <code>FLOOR_THICKNESS</code> if the selected level is the first level
   * and <code>HEIGHT</code> if the selected level is the last level. 
   */
  public boolean isPropertyEditable(Property property) {
    List<Level> levels = this.home.getLevels();
    switch (property) {
      case FLOOR_THICKNESS :
        return levels.indexOf(this.home.getSelectedLevel()) != 0 
            || this.home.getSelectedLevel().getElevation() > 0;
      case HEIGHT :
        return levels.indexOf(this.home.getSelectedLevel()) != levels.size() - 1;
      default :
        return true;
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
   * Sets the edited elevation.
   */
  public void setElevation(Float elevation) {
    if (elevation != this.elevation) {
      Float oldElevation = this.elevation;
      this.elevation = elevation;
      this.propertyChangeSupport.firePropertyChange(Property.ELEVATION.name(), oldElevation, elevation);
    }
  }

  /**
   * Returns the edited elevation.
   */
  public Float getElevation() {
    return this.elevation;
  }
  
  /**
   * Sets the edited floor thickness.
   */
  public void setFloorThickness(Float floorThickness) {
    if (floorThickness != this.floorThickness) {
      Float oldFloorThickness = this.floorThickness;
      this.floorThickness = floorThickness;
      this.propertyChangeSupport.firePropertyChange(Property.FLOOR_THICKNESS.name(), oldFloorThickness, floorThickness);
    }
  }

  /**
   * Returns the edited floor thickness.
   */
  public Float getFloorThickness() {
    return this.floorThickness;
  }
  
  /**
   * Sets the edited height.
   */
  public void setHeight(Float height) {
    if (height != this.height) {
      Float oldHeight = this.height;
      this.height = height;
      this.propertyChangeSupport.firePropertyChange(Property.HEIGHT.name(), oldHeight, height);
    }
  }

  /**
   * Returns the edited height.
   */
  public Float getHeight() {
    return this.height;
  }
  
  /**
   * Controls the modification of selected level in the edited home.
   */
  public void modifyLevels() {
    Level selectedLevel = this.home.getSelectedLevel();
    if (selectedLevel != null) {
      String name = getName();
      Float elevation = getElevation();
      Float floorThickness = getFloorThickness();
      Float height = getHeight();
      
      ModifiedLevel modifiedLevel = new ModifiedLevel(selectedLevel);
      // Apply modification
      doModifyLevel(modifiedLevel, name, elevation, floorThickness, height);
      if (this.undoSupport != null) {
        UndoableEdit undoableEdit = new LevelModificationUndoableEdit(
            this.preferences, modifiedLevel, name, elevation, floorThickness, height);
        this.undoSupport.postEdit(undoableEdit);
      }
      if (name != null) {
        this.preferences.addAutoCompletionString("LevelName", name);
      }
    }
  }
  
  /**
   * Undoable edit for level modification. This class isn't anonymous to avoid
   * being bound to controller and its view.
   */
  private static class LevelModificationUndoableEdit extends AbstractUndoableEdit {
    private final UserPreferences  preferences;
    private final ModifiedLevel    modifiedLevel;
    private final String           name;
    private final Float            elevation;
    private final Float            floorThickness;
    private final Float            height;

    private LevelModificationUndoableEdit(UserPreferences preferences,
                                          ModifiedLevel modifiedLevel, 
                                          String name,
                                          Float elevation,
                                          Float floorThickness,
                                          Float height) {
      this.preferences = preferences;
      this.modifiedLevel = modifiedLevel;
      this.name = name;
      this.elevation = elevation;
      this.floorThickness = floorThickness;
      this.height = height;
    }

    @Override
    public void undo() throws CannotUndoException {
      super.undo();
      undoModifyLevel(this.modifiedLevel);
    }

    @Override
    public void redo() throws CannotRedoException {
      super.redo();
      doModifyLevel(this.modifiedLevel, this.name, this.elevation, this.floorThickness, this.height); 
    }

    @Override
    public String getPresentationName() {
      return this.preferences.getLocalizedString(LevelController.class, "undoModifyLevelName");
    }
  }

  /**
   * Modifies level properties with the values in parameter.
   */
  private static void doModifyLevel(ModifiedLevel modifiedLevel, 
                                    String name, Float elevation, 
                                    Float floorThickness, Float height) {
    Level level = modifiedLevel.getLevel();
    if (name != null) {
      level.setName(name);
    }
    if (elevation != null) {
      level.setElevation(elevation); 
    }
    if (floorThickness != null) {
      level.setFloorThickness(floorThickness);
    }
    if (height != null) {
      level.setHeight(height);
    }
  }

  /**
   * Restores level properties from the values stored in <code>modifiedLevel</code>.
   */
  private static void undoModifyLevel(ModifiedLevel modifiedLevel) {
    modifiedLevel.reset();
  }

  /**
   * Stores the current properties values of a modified level.
   */
  private static class ModifiedLevel {
    private final Level   level;
    private final String  name;
    private final float   elevation;
    private final float   floorThickness;
    private final float   height;

    public ModifiedLevel(Level level) {
      this.level = level;
      this.name = level.getName();
      this.elevation = level.getElevation();
      this.floorThickness = level.getFloorThickness();
      this.height = level.getHeight();
    }

    public Level getLevel() {
      return this.level;
    }
        
    public void reset() {
      this.level.setName(this.name);
      this.level.setElevation(this.elevation);
      this.level.setFloorThickness(this.floorThickness);
      this.level.setHeight(this.height);
    }
  }
}
