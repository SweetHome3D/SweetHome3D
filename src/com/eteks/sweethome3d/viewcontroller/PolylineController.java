/*
 * PolylineController.java 20 nov. 2008
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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.Polyline;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * A MVC controller for polyline view.
 * @author Emmanuel Puybaret
 * @since 5.0
 */
public class PolylineController implements Controller {
  /**
   * The properties that may be edited by the view associated to this controller.
   */
  public enum Property {THICKNESS, CAP_STYLE, JOIN_STYLE, DASH_STYLE, DASH_PATTERN, DASH_OFFSET, START_ARROW_STYLE, END_ARROW_STYLE, COLOR, ELEVATION}

  private final Home                  home;
  private final UserPreferences       preferences;
  private final ViewFactory           viewFactory;
  private final UndoableEditSupport   undoSupport;
  private final PropertyChangeSupport propertyChangeSupport;
  private DialogView                  polylineView;

  private Float               thickness;
  private boolean             capStyleEditable;
  private Polyline.CapStyle   capStyle;
  private Polyline.JoinStyle  joinStyle;
  private boolean             joinStyleEditable;
  private Polyline.DashStyle  dashStyle;
  private float []            dashPattern;
  private Float               dashOffset;
  private boolean             arrowsStyleEditable;
  private Polyline.ArrowStyle startArrowStyle;
  private Polyline.ArrowStyle endArrowStyle;
  private Integer             color;
  private Float               elevation;
  private Boolean             elevationEnabled;

  /**
   * Creates the controller of polyline view with undo support.
   */
  public PolylineController(final Home home,
                            UserPreferences preferences,
                            ViewFactory viewFactory,
                            ContentManager contentManager,
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
    if (this.polylineView == null) {
      this.polylineView = this.viewFactory.createPolylineView(this.preferences, this);
    }
    return this.polylineView;
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
   * Updates edited properties from selected polylines in the home edited by this controller.
   */
  protected void updateProperties() {
    List<Polyline> selectedPolylines = Home.getPolylinesSubList(this.home.getSelectedItems());
    if (selectedPolylines.isEmpty()) {
      setThickness(null); // Nothing to edit
      this.capStyleEditable = false;
      setCapStyle(null);
      this.joinStyleEditable = false;
      setJoinStyle(null);
      setDashStyle(null);
      this.dashPattern = null;
      setDashOffset(null);
      this.arrowsStyleEditable = false;
      setStartArrowStyle(null);
      setEndArrowStyle(null);
      setColor(null);
      this.elevationEnabled = Boolean.FALSE;
    } else {
      // Search the common properties among selected polylines
      Polyline firstPolyline = selectedPolylines.get(0);

      // Search the common thickness among polylines
      Float thickness = firstPolyline.getThickness();
      for (int i = 1; i < selectedPolylines.size(); i++) {
        if (thickness != selectedPolylines.get(i).getThickness()) {
          thickness = null;
          break;
        }
      }
      setThickness(thickness);

      this.capStyleEditable = false;
      for (int i = 0; i < selectedPolylines.size(); i++) {
        if (!selectedPolylines.get(i).isClosedPath()) {
          this.capStyleEditable = true;
          break;
        }
      }

      if (this.capStyleEditable) {
        Polyline.CapStyle capStyle = firstPolyline.getCapStyle();
        if (capStyle != null) {
          for (int i = 1; i < selectedPolylines.size(); i++) {
            if (capStyle != selectedPolylines.get(i).getCapStyle()) {
              capStyle = null;
              break;
            }
          }
        }
        setCapStyle(capStyle);
      } else {
        setCapStyle(null);
      }

      this.joinStyleEditable = false;
      for (int i = 0; i < selectedPolylines.size(); i++) {
        if (selectedPolylines.get(i).getPointCount() > 2) {
          this.joinStyleEditable = true;
          break;
        }
      }

      if (this.joinStyleEditable) {
        Polyline.JoinStyle joinStyle = firstPolyline.getJoinStyle();
        if (joinStyle != null) {
          for (int i = 1; i < selectedPolylines.size(); i++) {
            if (joinStyle != selectedPolylines.get(i).getJoinStyle()) {
              joinStyle = null;
              break;
            }
          }
        }
        setJoinStyle(joinStyle);
      } else {
        setJoinStyle(null);
      }

      float [] dashPattern = firstPolyline.getDashPattern();
      if (dashPattern != null) {
        for (int i = 1; i < selectedPolylines.size(); i++) {
          if (!Arrays.equals(dashPattern, selectedPolylines.get(i).getDashPattern())) {
            dashPattern = null;
            break;
          }
        }
      }
      setDashPattern(dashPattern);

      if (dashPattern != null) {
        Polyline.DashStyle dashStyle = firstPolyline.getDashStyle();
        if (dashStyle != null) {
          for (int i = 1; i < selectedPolylines.size(); i++) {
            if (dashStyle != selectedPolylines.get(i).getDashStyle()) {
              dashStyle = null;
              break;
            }
          }
        }
        if (dashStyle == Polyline.DashStyle.CUSTOMIZED) {
        }
        setDashStyle(dashStyle);
      } else {
        setDashStyle(null);
      }

      // Search the common dash offset among polylines
      Float dashOffset = firstPolyline.getDashOffset();
      for (int i = 1; i < selectedPolylines.size(); i++) {
        if (dashOffset != selectedPolylines.get(i).getDashOffset()) {
          dashOffset = null;
          break;
        }
      }
      setDashOffset(dashOffset);

      this.arrowsStyleEditable = this.capStyleEditable;
      if (this.arrowsStyleEditable) {
        Polyline.ArrowStyle startArrowStyle = firstPolyline.getStartArrowStyle();
        if (startArrowStyle != null) {
          for (int i = 1; i < selectedPolylines.size(); i++) {
            if (startArrowStyle != selectedPolylines.get(i).getStartArrowStyle()) {
              startArrowStyle = null;
              break;
            }
          }
        }
        setStartArrowStyle(startArrowStyle);

        Polyline.ArrowStyle endArrowStyle = firstPolyline.getEndArrowStyle();
        if (endArrowStyle != null) {
          for (int i = 1; i < selectedPolylines.size(); i++) {
            if (endArrowStyle != selectedPolylines.get(i).getEndArrowStyle()) {
              endArrowStyle = null;
              break;
            }
          }
        }
        setEndArrowStyle(endArrowStyle);
      } else {
        setStartArrowStyle(null);
        setEndArrowStyle(null);
      }

      // Search the common color among polylines
      Integer color = firstPolyline.getColor();
      if (color != null) {
        for (int i = 1; i < selectedPolylines.size(); i++) {
          if (color != selectedPolylines.get(i).getColor()) {
            color = null;
            break;
          }
        }
      }
      setColor(color);

      Float elevation = firstPolyline.getElevation();
      for (int i = 1; i < selectedPolylines.size(); i++) {
        if (elevation != selectedPolylines.get(i).getElevation()) {
          elevation = null;
          break;
        }
      }
      setElevation(elevation);

      Boolean elevationEnabled = firstPolyline.isVisibleIn3D();
      for (int i = 1; i < selectedPolylines.size(); i++) {
        if (!elevationEnabled.equals(selectedPolylines.get(i).isVisibleIn3D())) {
          elevationEnabled = null;
          break;
        }
      }
      this.elevationEnabled = elevationEnabled;
    }
  }

  /**
   * Sets the edited thickness.
   */
  public void setThickness(Float thickness) {
    if (thickness != this.thickness) {
      Float oldThickness = this.thickness;
      this.thickness = thickness;
      this.propertyChangeSupport.firePropertyChange(Property.THICKNESS.name(), oldThickness, thickness);
    }
  }

  /**
   * Returns the edited thickness.
   */
  public Float getThickness() {
    return this.thickness;
  }

  /**
   * Sets the edited capStyle.
   */
  public void setCapStyle(Polyline.CapStyle capStyle) {
    if (capStyle != this.capStyle) {
      Polyline.CapStyle oldCapStyle = this.capStyle;
      this.capStyle = capStyle;
      this.propertyChangeSupport.firePropertyChange(Property.CAP_STYLE.name(), oldCapStyle, capStyle);
    }
  }

  /**
   * Returns the edited capStyle.
   */
  public Polyline.CapStyle getCapStyle() {
    return this.capStyle;
  }

  /**
   * Returns <code>true</code> if cap style is editable.
   */
  public boolean isCapStyleEditable() {
    return this.capStyleEditable;
  }

  /**
   * Sets the edited joinStyle.
   */
  public void setJoinStyle(Polyline.JoinStyle joinStyle) {
    if (joinStyle != this.joinStyle) {
      Polyline.JoinStyle oldJoinStyle = this.joinStyle;
      this.joinStyle = joinStyle;
      this.propertyChangeSupport.firePropertyChange(Property.JOIN_STYLE.name(), oldJoinStyle, joinStyle);
    }
  }

  /**
   * Returns the edited joinStyle.
   */
  public Polyline.JoinStyle getJoinStyle() {
    return this.joinStyle;
  }

  /**
   * Returns <code>true</code> if join style is editable.
   */
  public boolean isJoinStyleEditable() {
    return this.joinStyleEditable;
  }

  /**
   * Sets the edited dash style.
   */
  public void setDashStyle(Polyline.DashStyle dashStyle) {
    if (dashStyle != this.dashStyle) {
      Polyline.DashStyle oldDashStyle = this.dashStyle;
      this.dashStyle = dashStyle;
      this.propertyChangeSupport.firePropertyChange(Property.DASH_STYLE.name(), oldDashStyle, dashStyle);
    }
  }

  /**
   * Returns the edited dash style.
   */
  public Polyline.DashStyle getDashStyle() {
    return this.dashStyle;
  }

  /**
   * Sets the edited dash pattern.
   * @since 6.1
   */
  public void setDashPattern(float [] dashPattern) {
    if (!Arrays.equals(dashPattern, this.dashPattern)) {
      float [] oldDashPattern = this.dashPattern;
      this.dashPattern = dashPattern;
      this.propertyChangeSupport.firePropertyChange(Property.DASH_PATTERN.name(), oldDashPattern, dashPattern);
    }
  }

  /**
   * Returns the edited dash pattern.
   */
  public float [] getDashPattern() {
    return this.dashPattern;
  }

  /**
   * Sets the edited dash offset.
   */
  public void setDashOffset(Float dashOffset) {
    if (dashOffset != this.dashOffset) {
      Float oldDashOffset = this.dashOffset;
      this.dashOffset = dashOffset;
      this.propertyChangeSupport.firePropertyChange(Property.DASH_OFFSET.name(), oldDashOffset, dashOffset);
    }
  }

  /**
   * Returns the edited dash offset.
   */
  public Float getDashOffset() {
    return this.dashOffset;
  }

  /**
   * Sets the edited start arrow style.
   */
  public void setStartArrowStyle(Polyline.ArrowStyle startArrowStyle) {
    if (startArrowStyle != this.startArrowStyle) {
      Polyline.ArrowStyle oldStartArrowStyle = this.startArrowStyle;
      this.startArrowStyle = startArrowStyle;
      this.propertyChangeSupport.firePropertyChange(Property.START_ARROW_STYLE.name(), oldStartArrowStyle, startArrowStyle);
    }
  }

  /**
   * Returns the edited start arrow style.
   */
  public Polyline.ArrowStyle getStartArrowStyle() {
    return this.startArrowStyle;
  }

  /**
   * Sets the edited end arrow style.
   */
  public void setEndArrowStyle(Polyline.ArrowStyle endArrowStyle) {
    if (endArrowStyle != this.endArrowStyle) {
      Polyline.ArrowStyle oldEndArrowStyle = this.endArrowStyle;
      this.endArrowStyle = endArrowStyle;
      this.propertyChangeSupport.firePropertyChange(Property.END_ARROW_STYLE.name(), oldEndArrowStyle, endArrowStyle);
    }
  }

  /**
   * Returns the edited end arrow style.
   */
  public Polyline.ArrowStyle getEndArrowStyle() {
    return this.endArrowStyle;
  }

  /**
   * Returns <code>true</code> if arrows style is editable.
   */
  public boolean isArrowsStyleEditable() {
    return this.arrowsStyleEditable;
  }

  /**
   * Sets the edited color.
   */
  public void setColor(Integer color) {
    if (color != this.color) {
      Integer oldColor = this.color;
      this.color = color;
      this.propertyChangeSupport.firePropertyChange(Property.COLOR.name(), oldColor, color);
    }
  }

  /**
   * Returns the edited color.
   */
  public Integer getColor() {
    return this.color;
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
    this.elevationEnabled = elevation != null;
  }

  /**
   * Returns the edited elevation.
   */
  public Float getElevation() {
    return this.elevation;
  }

  /**
   * Returns <code>Boolean.TRUE</code> if all edited polylines are viewed in 3D,
   * or <code>Boolean.FALSE</code> if no polyline is viewed in 3D.
   */
  public Boolean isElevationEnabled() {
    return this.elevationEnabled;
  }

  /**
   * Controls the modification of selected polylines in edited home.
   */
  public void modifyPolylines() {
    List<Selectable> oldSelection = this.home.getSelectedItems();
    List<Polyline> selectedPolylines = Home.getPolylinesSubList(oldSelection);
    if (!selectedPolylines.isEmpty()) {
      Float thickness = getThickness();
      Polyline.CapStyle capStyle = getCapStyle();
      Polyline.JoinStyle joinStyle = getJoinStyle();
      Polyline.DashStyle dashStyle = getDashStyle();
      float [] dashPattern = getDashPattern();
      Float dashOffset = getDashOffset();
      Polyline.ArrowStyle startArrowStyle = getStartArrowStyle();
      Polyline.ArrowStyle endArrowStyle = getEndArrowStyle();
      Integer color = getColor();
      Float elevation = getElevation();
      Boolean elevationEnabled = isElevationEnabled();

      // Create an array of modified polylines with their current properties values
      ModifiedPolyline [] modifiedPolylines = new ModifiedPolyline [selectedPolylines.size()];
      for (int i = 0; i < modifiedPolylines.length; i++) {
        modifiedPolylines [i] = new ModifiedPolyline(selectedPolylines.get(i));
      }
      // Apply modification
      doModifyPolylines(modifiedPolylines, thickness,
          capStyle, joinStyle, dashStyle, dashPattern, dashOffset, startArrowStyle, endArrowStyle, color, elevation, elevationEnabled);
      if (this.undoSupport != null) {
        UndoableEdit undoableEdit = new PolylinesModificationUndoableEdit(
            this.home, this.preferences, oldSelection.toArray(new Selectable [oldSelection.size()]),
            modifiedPolylines, thickness, capStyle, joinStyle, dashStyle, dashPattern, dashOffset,
            startArrowStyle, endArrowStyle, color, elevation, elevationEnabled);
        this.undoSupport.postEdit(undoableEdit);
      }
    }
  }

  /**
   * Undoable edit for polylines modification. This class isn't anonymous to avoid
   * being bound to controller and its view.
   */
  private static class PolylinesModificationUndoableEdit extends LocalizedUndoableEdit {
    private final Home                home;
    private final Selectable []       oldSelection;
    private final ModifiedPolyline [] modifiedPolylines;
    private final Float               thickness;
    private final Polyline.CapStyle   capStyle;
    private final Polyline.JoinStyle  joinStyle;
    private final Polyline.DashStyle  dashStyle;
    private final float []            dashPattern;
    private final Float               dashOffset;
    private final Polyline.ArrowStyle startArrowStyle;
    private final Polyline.ArrowStyle endArrowStyle;
    private final Integer             color;
    private final Float               elevation;
    private final Boolean             elevationEnabled;

    private PolylinesModificationUndoableEdit(Home home,
                                              UserPreferences preferences,
                                              Selectable []  oldSelection,
                                              ModifiedPolyline [] modifiedPolylines,
                                              Float thickness,
                                              Polyline.CapStyle capStyle,
                                              Polyline.JoinStyle joinStyle,
                                              Polyline.DashStyle dashStyle,
                                              float [] dashPattern,
                                              Float dashOffset,
                                              Polyline.ArrowStyle startArrowStyle,
                                              Polyline.ArrowStyle endArrowStyle,
                                              Integer color,
                                              Float elevation, Boolean elevationEnabled) {
      super(preferences, PolylineController.class, "undoModifyPolylinesName");
      this.home = home;
      this.oldSelection = oldSelection;
      this.modifiedPolylines = modifiedPolylines;
      this.thickness = thickness;
      this.capStyle = capStyle;
      this.joinStyle = joinStyle;
      this.dashStyle = dashStyle;
      this.dashPattern = dashPattern;
      this.dashOffset = dashOffset;
      this.startArrowStyle = startArrowStyle;
      this.endArrowStyle = endArrowStyle;
      this.color = color;
      this.elevation = elevation;
      this.elevationEnabled = elevationEnabled;
    }

    @Override
    public void undo() throws CannotUndoException {
      super.undo();
      undoModifyPolylines(this.modifiedPolylines);
      this.home.setSelectedItems(Arrays.asList(this.oldSelection));
    }

    @Override
    public void redo() throws CannotRedoException {
      super.redo();
      doModifyPolylines(this.modifiedPolylines, this.thickness,
          this.capStyle, this.joinStyle, this.dashStyle, this.dashPattern, this.dashOffset,
          this.startArrowStyle, this.endArrowStyle, this.color,
          this.elevation, this.elevationEnabled);
      this.home.setSelectedItems(Arrays.asList(this.oldSelection));
    }
  }

  /**
   * Modifies polylines properties with the values in parameter.
   */
  private static void doModifyPolylines(ModifiedPolyline [] modifiedPolylines,
                                        Float thickness, Polyline.CapStyle capStyle, Polyline.JoinStyle joinStyle,
                                        Polyline.DashStyle dashStyle, float [] dashPattern, Float dashOffset,
                                        Polyline.ArrowStyle startArrowStyle, Polyline.ArrowStyle endArrowStyle,
                                        Integer color, Float elevation, Boolean elevationEnabled) {
    for (ModifiedPolyline modifiedPolyline : modifiedPolylines) {
      Polyline polyline = modifiedPolyline.getPolyline();
      if (thickness != null) {
        polyline.setThickness(thickness);
      }
      if (capStyle != null) {
        polyline.setCapStyle(capStyle);
      }
      if (joinStyle != null) {
        polyline.setJoinStyle(joinStyle);
      }
      if (dashStyle != null) {
        polyline.setDashStyle(dashStyle);
      }
      if (dashStyle == Polyline.DashStyle.CUSTOMIZED && dashPattern != null) {
        polyline.setDashPattern(dashPattern);
      }
      if (dashOffset != null) {
        polyline.setDashOffset(polyline.getDashStyle() != Polyline.DashStyle.SOLID ? dashOffset : 0);
      }
      if (startArrowStyle != null) {
        polyline.setStartArrowStyle(startArrowStyle);
      }
      if (endArrowStyle != null) {
        polyline.setEndArrowStyle(endArrowStyle);
      }
      if (color != null) {
        polyline.setColor(color);
      }
      if (elevationEnabled != null) {
        if (Boolean.FALSE.equals(elevationEnabled)) {
          polyline.setVisibleIn3D(false);
        } else if (elevation != null) {
          polyline.setVisibleIn3D(true);
          polyline.setElevation(elevation);
        }
      }
    }
  }

  /**
   * Restores polyline properties from the values stored in <code>modifiedPolylines</code>.
   */
  private static void undoModifyPolylines(ModifiedPolyline [] modifiedPolylines) {
    for (ModifiedPolyline modifiedPolyline : modifiedPolylines) {
      modifiedPolyline.reset();
    }
  }

  /**
   * Stores the current properties values of a modified polyline.
   */
  private static final class ModifiedPolyline {
    private final Polyline            polyline;
    private final float               thickness;
    private final Polyline.CapStyle   capStyle;
    private final Polyline.JoinStyle  joinStyle;
    private final float []            dashPattern;
    private final Float               dashOffset;
    private final Polyline.ArrowStyle startArrowStyle;
    private final Polyline.ArrowStyle endArrowStyle;
    private final int                 color;
    private final boolean             visibleIn3D;
    private final float               elevation;

    public ModifiedPolyline(Polyline polyline) {
      this.polyline = polyline;
      this.thickness = polyline.getThickness();
      this.capStyle = polyline.getCapStyle();
      this.joinStyle = polyline.getJoinStyle();
      this.dashPattern = polyline.getDashPattern();
      this.dashOffset = polyline.getDashOffset();
      this.startArrowStyle = polyline.getStartArrowStyle();
      this.endArrowStyle = polyline.getEndArrowStyle();
      this.color = polyline.getColor();
      this.visibleIn3D = polyline.isVisibleIn3D();
      this.elevation = polyline.getElevation();
    }

    public Polyline getPolyline() {
      return this.polyline;
    }

    public void reset() {
      this.polyline.setThickness(this.thickness);
      this.polyline.setCapStyle(this.capStyle);
      this.polyline.setJoinStyle(this.joinStyle);
      this.polyline.setDashPattern(this.dashPattern);
      this.polyline.setDashOffset(this.dashOffset);
      this.polyline.setStartArrowStyle(this.startArrowStyle);
      this.polyline.setEndArrowStyle(this.endArrowStyle);
      this.polyline.setColor(this.color);
      this.polyline.setVisibleIn3D(this.visibleIn3D);
      this.polyline.setElevation(this.elevation);
    }
  }
}
