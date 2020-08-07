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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.Elevatable;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.Level;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * A MVC controller for home levels view.
 * @author Emmanuel Puybaret
 */
public class LevelController implements Controller {
  /**
   * The properties that may be edited by the view associated to this controller.
   */
  public enum Property {VIEWABLE, NAME, ELEVATION, ELEVATION_INDEX, FLOOR_THICKNESS, HEIGHT, LEVELS, SELECT_LEVEL_INDEX}

  private final Home                  home;
  private final UserPreferences       preferences;
  private final ViewFactory           viewFactory;
  private final UndoableEditSupport   undoSupport;
  private final PropertyChangeSupport propertyChangeSupport;
  private DialogView                  homeLevelView;

  private String   name;
  private Boolean  viewable;
  private Float    elevation;
  private Integer  elevationIndex;
  private Float    floorThickness;
  private Float    height;
  private Level [] levels;
  private Integer  selectedLevelIndex;

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
    setLevels(clone(this.home.getLevels().toArray(new Level [0])));
    if (selectedLevel == null) {
      setSelectedLevelIndex(null);
      setName(null); // Nothing to edit
      setViewable(Boolean.TRUE);
      setElevation(null, false);
      setFloorThickness(null);
      setHeight(null);
      setElevationIndex(null, false);
    } else {
      setSelectedLevelIndex(this.home.getLevels().indexOf(selectedLevel));
      setName(selectedLevel.getName());
      setViewable(selectedLevel.isViewable());
      setElevation(selectedLevel.getElevation(), false);
      setFloorThickness(selectedLevel.getFloorThickness());
      setHeight(selectedLevel.getHeight());
      setElevationIndex(selectedLevel.getElevationIndex(), false);
    }
  }

  private Level [] clone(Level[] levels) {
    for (int i = 0; i < levels.length; i++) {
      levels [i] = levels [i].clone();
    }
    return levels;
  }

  /**
   * Returns <code>true</code> if the given <code>property</code> is editable.
   * Depending on whether a property is editable or not, the view associated to this controller
   * may render it differently.
   * The implementation of this method always returns <code>true</code>.
   */
  public boolean isPropertyEditable(Property property) {
    return true;
  }

  /**
   * Sets the edited name.
   */
  public void setName(String name) {
    if (name != this.name) {
      String oldName = this.name;
      this.name = name;
      this.propertyChangeSupport.firePropertyChange(Property.NAME.name(), oldName, name);
      if (this.selectedLevelIndex != null) {
        this.levels [this.selectedLevelIndex].setName(name);
        this.propertyChangeSupport.firePropertyChange(Property.LEVELS.name(), null, this.levels);
      }
    }
  }

  /**
   * Returns the edited name.
   */
  public String getName() {
    return this.name;
  }

  /**
   * Sets the edited viewable attribute.
   * @since 5.0
   */
  public void setViewable(Boolean viewable) {
    if (viewable != this.viewable) {
      Boolean oldViewable = viewable;
      this.viewable = viewable;
      this.propertyChangeSupport.firePropertyChange(Property.VIEWABLE.name(), oldViewable, viewable);
      if (viewable != null && this.selectedLevelIndex != null) {
        this.levels [this.selectedLevelIndex].setViewable(viewable);
        this.propertyChangeSupport.firePropertyChange(Property.LEVELS.name(), null, this.levels);
      }
    }
  }

  /**
   * Returns the edited viewable attribute.
   * @since 5.0
   */
  public Boolean getViewable() {
    return this.viewable;
  }

  /**
   * Sets the edited elevation.
   */
  public void setElevation(Float elevation) {
    setElevation(elevation, true);
  }

  private void setElevation(Float elevation, boolean updateLevels) {
    if (elevation != this.elevation) {
      Float oldElevation = this.elevation;
      this.elevation = elevation;
      this.propertyChangeSupport.firePropertyChange(Property.ELEVATION.name(), oldElevation, elevation);

      if (updateLevels
          && elevation != null
          && this.selectedLevelIndex != null) {
        int elevationIndex = updateLevelElevation(this.levels [this.selectedLevelIndex],
            elevation, Arrays.asList(this.levels));
        setElevationIndex(elevationIndex, false);
        updateLevels();
      }
    }
  }

  /**
   * Updates the elevation of the given <code>level</code> and modifies the
   * elevation index of other levels if necessary.
   */
  private static int updateLevelElevation(Level level, float elevation, List<Level> levels) {
    // Search biggest elevation index at the given elevation
    // and update elevation index of other levels at the current elevation of the modified level
    int levelIndex = levels.size();
    int elevationIndex = 0;
    for (int i = 0; i < levels.size(); i++) {
      Level homeLevel = levels.get(i);
      if (homeLevel == level) {
        levelIndex = i;
      } else {
        if (homeLevel.getElevation() == elevation) {
          elevationIndex = homeLevel.getElevationIndex() + 1;
        } else if (i > levelIndex
            && homeLevel.getElevation() == level.getElevation()) {
          homeLevel.setElevationIndex(homeLevel.getElevationIndex() - 1);
        }
      }
    }
    level.setElevation(elevation);
    level.setElevationIndex(elevationIndex);
    return elevationIndex;
  }

  /**
   * Returns the edited elevation.
   */
  public Float getElevation() {
    return this.elevation;
  }

  /**
   * Sets the edited elevation index.
   * @since 5.0
   */
  public void setElevationIndex(Integer elevationIndex) {
    setElevationIndex(elevationIndex, true);
  }

  private void setElevationIndex(Integer elevationIndex, boolean updateLevels) {
    if (elevationIndex != this.elevationIndex) {
      Integer oldElevationIndex = this.elevationIndex;
      this.elevationIndex = elevationIndex;
      this.propertyChangeSupport.firePropertyChange(Property.ELEVATION_INDEX.name(), oldElevationIndex, elevationIndex);

      if (updateLevels
          && elevationIndex != null
          && this.selectedLevelIndex != null) {
        updateLevelElevationIndex(this.levels [this.selectedLevelIndex], elevationIndex, Arrays.asList(this.levels));
        updateLevels();
      }
    }
  }

  /**
   * Updates the elevation index of the given <code>level</code> and modifies the
   * elevation index of other levels at same elevation if necessary.
   */
  private static void updateLevelElevationIndex(Level level, int elevationIndex, List<Level> levels) {
    // Update elevation index of levels with a value between selected level index and new index
    float elevationIndexSignum = Math.signum(elevationIndex - level.getElevationIndex());
    for (Level homeLevel : levels) {
      if (homeLevel != level
          && homeLevel.getElevation() == level.getElevation()
          && Math.signum(homeLevel.getElevationIndex() - level.getElevationIndex()) == elevationIndexSignum
          && Math.signum(homeLevel.getElevationIndex() - elevationIndex) != elevationIndexSignum) {
        homeLevel.setElevationIndex(homeLevel.getElevationIndex() - (int)elevationIndexSignum);
      } else if (homeLevel.getElevation() > level.getElevation()) {
        break;
      }
    }
    level.setElevationIndex(elevationIndex);
  }

  private void updateLevels() {
    // Create a temporary home with levels to update their order
    Home tempHome = new Home();
    Level selectedLevel = this.levels [this.selectedLevelIndex];
    for (Level homeLevel : this.levels) {
      tempHome.addLevel(homeLevel);
    }
    List<Level> updatedLevels = tempHome.getLevels();
    setLevels(updatedLevels.toArray(new Level [updatedLevels.size()]));
    setSelectedLevelIndex(updatedLevels.indexOf(selectedLevel));
  }

  /**
   * Returns the edited elevation index.
   * @since 5.0
   */
  public Integer getElevationIndex() {
    return this.elevationIndex;
  }

  /**
   * Sets the edited floor thickness.
   */
  public void setFloorThickness(Float floorThickness) {
    if (floorThickness != this.floorThickness) {
      Float oldFloorThickness = this.floorThickness;
      this.floorThickness = floorThickness;
      this.propertyChangeSupport.firePropertyChange(Property.FLOOR_THICKNESS.name(), oldFloorThickness, floorThickness);
      if (floorThickness != null && this.selectedLevelIndex != null) {
        this.levels [this.selectedLevelIndex].setFloorThickness(floorThickness);
        this.propertyChangeSupport.firePropertyChange(Property.LEVELS.name(), null, this.levels);
      }
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
      if (height != null && this.selectedLevelIndex != null) {
        this.levels [this.selectedLevelIndex].setHeight(height);
        this.propertyChangeSupport.firePropertyChange(Property.LEVELS.name(), null, this.levels);
      }
    }
  }

  /**
   * Returns the edited height.
   */
  public Float getHeight() {
    return this.height;
  }

  /**
   * Sets home levels.
   */
  private void setLevels(Level [] levels) {
    if (levels != this.levels) {
      Level [] oldLevels = this.levels;
      this.levels = levels;
      this.propertyChangeSupport.firePropertyChange(Property.LEVELS.name(), oldLevels, levels);
    }
  }

  /**
   * Returns a copy of home levels.
   */
  public Level [] getLevels() {
    return this.levels.clone();
  }

  /**
   * Sets the selected level index.
   */
  private void setSelectedLevelIndex(Integer selectedLevelIndex) {
    if (selectedLevelIndex != this.selectedLevelIndex) {
      Integer oldSelectedLevelIndex = this.selectedLevelIndex;
      this.selectedLevelIndex = selectedLevelIndex;
      this.propertyChangeSupport.firePropertyChange(Property.SELECT_LEVEL_INDEX.name(), oldSelectedLevelIndex, selectedLevelIndex);
    }
  }

  /**
   * Returns the selected level index.
   */
  public Integer getSelectedLevelIndex() {
    return this.selectedLevelIndex;
  }

  /**
   * Controls the modification of selected level in the edited home.
   */
  public void modifyLevels() {
    Level selectedLevel = this.home.getSelectedLevel();
    if (selectedLevel != null) {
      List<Selectable> oldSelection = this.home.getSelectedItems();
      String name = getName();
      Boolean viewable = getViewable();
      Float elevation = getElevation();
      Float floorThickness = getFloorThickness();
      Float height = getHeight();
      Integer elevationIndex = getElevationIndex();

      ModifiedLevel modifiedLevel = new ModifiedLevel(selectedLevel);
      // Apply modification
      doModifyLevel(this.home, modifiedLevel, name, viewable, elevation, floorThickness, height, elevationIndex);
      if (this.undoSupport != null) {
        UndoableEdit undoableEdit = new LevelModificationUndoableEdit(
            this.home, this.preferences, oldSelection.toArray(new Selectable [oldSelection.size()]),
            modifiedLevel, name, viewable,  elevation, floorThickness, height, elevationIndex);
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
  private static class LevelModificationUndoableEdit extends LocalizedUndoableEdit {
    private final Home          home;
    private final Selectable [] oldSelection;
    private final ModifiedLevel modifiedLevel;
    private final String        name;
    private final Boolean       viewable;
    private final Float         elevation;
    private final Float         floorThickness;
    private final Float         height;
    private final Integer       elevationIndex;

    private LevelModificationUndoableEdit(Home home,
                                          UserPreferences preferences,
                                          Selectable [] oldSelection,
                                          ModifiedLevel modifiedLevel,
                                          String name,
                                          Boolean viewable,
                                          Float elevation,
                                          Float floorThickness,
                                          Float height,
                                          Integer elevationIndex) {
      super(preferences, LevelController.class, "undoModifyLevelName");
      this.home = home;
      this.oldSelection = oldSelection;
      this.modifiedLevel = modifiedLevel;
      this.name = name;
      this.viewable = viewable;
      this.elevation = elevation;
      this.floorThickness = floorThickness;
      this.height = height;
      this.elevationIndex = elevationIndex;
    }

    @Override
    public void undo() throws CannotUndoException {
      super.undo();
      undoModifyLevel(this.home, this.modifiedLevel);
      this.home.setSelectedLevel(this.modifiedLevel.getLevel());
      this.home.setSelectedItems(Arrays.asList(this.oldSelection));
    }

    @Override
    public void redo() throws CannotRedoException {
      super.redo();
      this.home.setSelectedLevel(this.modifiedLevel.getLevel());
      doModifyLevel(this.home, this.modifiedLevel, this.name, this.viewable,
          this.elevation, this.floorThickness, this.height, this.elevationIndex);
    }
  }

  /**
   * Modifies level properties with the values in parameter.
   */
  private static void doModifyLevel(Home home, ModifiedLevel modifiedLevel,
                                    String name, Boolean viewable, Float elevation,
                                    Float floorThickness, Float height,
                                    Integer elevationIndex) {
    Level level = modifiedLevel.getLevel();
    if (name != null) {
      level.setName(name);
    }
    if (viewable != null) {
      List<Selectable> selectedItems = home.getSelectedItems();
      level.setViewable(viewable);
      home.setSelectedItems(getViewableSublist(selectedItems));
    }
    if (elevation != null
        && elevation != level.getElevation()) {
      updateLevelElevation(level, elevation, home.getLevels());
    }
    if (elevationIndex != null) {
      updateLevelElevationIndex(level, elevationIndex, home.getLevels());
    }
    if (!home.getEnvironment().isAllLevelsVisible()) {
      // Update visibility of levels
      Level selectedLevel = home.getSelectedLevel();
      boolean visible = true;
      for (Level homeLevel : home.getLevels()) {
        homeLevel.setVisible(visible);
        if (homeLevel == selectedLevel) {
          visible = false;
        }
      }
    }
    if (floorThickness != null) {
      level.setFloorThickness(floorThickness);
    }
    if (height != null) {
      level.setHeight(height);
    }
  }

  /**
   * Returns a sub list of <code>items</code> that are at a viewable level.
   */
  private static List<Selectable> getViewableSublist(List<? extends Selectable> items) {
    List<Selectable> viewableItems = new ArrayList<Selectable>(items.size());
    for (Selectable item : items) {
      if (!(item instanceof Elevatable)
          || ((Elevatable)item).getLevel().isViewable()) {
        viewableItems.add(item);
      }
    }
    return viewableItems;
  }

  /**
   * Restores level properties from the values stored in <code>modifiedLevel</code>.
   */
  private static void undoModifyLevel(Home home, ModifiedLevel modifiedLevel) {
    modifiedLevel.reset();
    Level level = modifiedLevel.getLevel();
    if (modifiedLevel.getElevation() != level.getElevation()) {
      updateLevelElevation(level, modifiedLevel.getElevation(), home.getLevels());
    }
    if (modifiedLevel.getElevationIndex() != level.getElevationIndex()) {
      updateLevelElevationIndex(level, modifiedLevel.getElevationIndex(), home.getLevels());
    }
  }

  /**
   * Stores the current properties values of a modified level.
   */
  private static class ModifiedLevel {
    private final Level   level;
    private final String  name;
    private final boolean viewable;
    private final float   elevation;
    private final float   floorThickness;
    private final float   height;
    private final int     elevationIndex;

    public ModifiedLevel(Level level) {
      this.level = level;
      this.name = level.getName();
      this.viewable = level.isViewable();
      this.elevation = level.getElevation();
      this.floorThickness = level.getFloorThickness();
      this.height = level.getHeight();
      this.elevationIndex = level.getElevationIndex();
    }

    public Level getLevel() {
      return this.level;
    }

    public float getElevation() {
      return this.elevation;
    }

    public int getElevationIndex() {
      return this.elevationIndex;
    }

    public void reset() {
      this.level.setName(this.name);
      this.level.setViewable(this.viewable);
      this.level.setFloorThickness(this.floorThickness);
      this.level.setHeight(this.height);
    }
  }
}
