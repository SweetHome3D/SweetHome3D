/*
 * LabelController.java 29 nov. 2008
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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.Label;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.TextStyle;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * A MVC controller for label view.
 * @author Emmanuel Puybaret
 */
public class LabelController implements Controller {
  /**
   * The property that may be edited by the view associated to this controller. 
   */
  public enum Property {TEXT, FONT_NAME, FONT_SIZE, COLOR, PITCH, ELEVATION}
  
  private final Home                  home;
  private final Float                 x;
  private final Float                 y;
  private final UserPreferences       preferences;
  private final ViewFactory           viewFactory;
  private final UndoableEditSupport   undoSupport;
  private final PropertyChangeSupport propertyChangeSupport;
  private DialogView                  labelView;

  private String  text;
  private String  fontName;
  private boolean fontNameSet;
  private Float   fontSize;
  private Integer color;
  private Float   pitch;
  private Boolean pitchEnabled;
  private Float   elevation;
  
  /**
   * Creates the controller of label modifications with undo support.
   */
  public LabelController(Home home,
                         UserPreferences preferences,
                         ViewFactory viewFactory, 
                         UndoableEditSupport undoSupport) {
    this.home = home;
    this.x = null;
    this.y = null;
    this.preferences = preferences;
    this.viewFactory = viewFactory;
    this.undoSupport = undoSupport;
    this.propertyChangeSupport = new PropertyChangeSupport(this);
    
    updateProperties();
  }

  /**
   * Creates the controller of label creation with undo support.
   */
  public LabelController(Home home, float x, float y,
                         UserPreferences preferences,
                         ViewFactory viewFactory, 
                         UndoableEditSupport undoSupport) {
    this.home = home;
    this.x = x;
    this.y = y;
    this.preferences = preferences;
    this.viewFactory = viewFactory;
    this.undoSupport = undoSupport;
    this.propertyChangeSupport = new PropertyChangeSupport(this);
    this.fontName = preferences.getDefaultFontName();
    this.fontNameSet = true;
    this.fontSize = preferences.getDefaultTextStyle(Label.class).getFontSize();
    this.pitchEnabled = Boolean.FALSE;
    this.elevation = 0f;
  }

  /**
   * Updates edited properties from selected labels in the home edited by this controller.
   */
  protected void updateProperties() {
    List<Label> selectedLabels = Home.getLabelsSubList(this.home.getSelectedItems());
    if (selectedLabels.isEmpty()) {
      setText(null); // Nothing to edit
      setFontName(null);
      this.fontNameSet = false;
      setFontSize(null);
      setColor(null);
      setPitch(null);
      this.pitchEnabled = Boolean.FALSE;
      setElevation(null);
    } else {
      // Search the common properties among selected labels
      Label firstLabel = selectedLabels.get(0);
      String text = firstLabel.getText();
      if (text != null) {
        for (int i = 1; i < selectedLabels.size(); i++) {
          if (!text.equals(selectedLabels.get(i).getText())) {
            text = null;
            break;
          }
        }
      }
      setText(text);

      String fontName = firstLabel.getStyle() != null 
          ? firstLabel.getStyle().getFontName()
          : null;
      boolean fontNameSet = true;
      for (int i = 1; i < selectedLabels.size(); i++) {
        Label label = selectedLabels.get(i);
        if (!(fontName == null && (label.getStyle() == null || label.getStyle().getFontName() == null)
              || fontName != null && label.getStyle() != null && fontName.equals(label.getStyle().getFontName()))) {
          fontNameSet = false;
          break;
        }
      }
      setFontName(fontName);
      this.fontNameSet = fontNameSet;
      
      float labelDefaultFontSize = this.preferences.getDefaultTextStyle(Label.class).getFontSize();
      Float fontSize = firstLabel.getStyle() != null 
          ? firstLabel.getStyle().getFontSize()
          : labelDefaultFontSize;
      for (int i = 1; i < selectedLabels.size(); i++) {
        Label label = selectedLabels.get(i);
        if (!fontSize.equals(label.getStyle() != null 
                ? label.getStyle().getFontSize()
                : labelDefaultFontSize)) {
          fontSize = null;
          break;
        }
      }
      setFontSize(fontSize);

      Integer color = firstLabel.getColor();
      if (color != null) {
        for (int i = 1; i < selectedLabels.size(); i++) {
          if (!color.equals(selectedLabels.get(i).getColor())) {
            color = null;
            break;
          }
        }
      }
      setColor(color);

      Float pitch = firstLabel.getPitch();
      for (int i = 1; i < selectedLabels.size(); i++) {
        Label label = selectedLabels.get(i);
        if (!(pitch == null && label.getPitch() == null
              || pitch != null && pitch.equals(label.getPitch()))) {
          pitch = null;
          break;
        }
      }
      setPitch(pitch);
      
      Boolean pitchEnabled = firstLabel.getPitch() != null;
      for (int i = 1; i < selectedLabels.size(); i++) {
        if (!pitchEnabled.equals(selectedLabels.get(i).getPitch() != null)) {
          pitchEnabled = null;
          break;
        }
      }
      this.pitchEnabled = pitchEnabled;

      Float elevation = firstLabel.getElevation();
      for (int i = 1; i < selectedLabels.size(); i++) {
        if (elevation.floatValue() != selectedLabels.get(i).getElevation()) {
          elevation = null;
          break;
        }
      }
      setElevation(elevation);
    }
  }
  
  /**
   * Returns the view associated with this controller.
   */
  public DialogView getView() {
    // Create view lazily only once it's needed
    if (this.labelView == null) {
      this.labelView = this.viewFactory.createLabelView(this.x == null, this.preferences, this);
    }
    return this.labelView;
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
   * Sets the edited text.
   */
  public void setText(String text) {
    if (text != this.text) {
      String oldText = this.text;
      this.text = text;
      this.propertyChangeSupport.firePropertyChange(Property.TEXT.name(), oldText, text);
    }
  }

  /**
   * Returns the edited text.
   */
  public String getText() {
    return this.text;
  }
  
  /**
   * Sets the edited font name.
   */
  public void setFontName(String fontName) {
    if (fontName != this.fontName) {
      String oldFontName = this.fontName;
      this.fontName = fontName;
      this.propertyChangeSupport.firePropertyChange(Property.FONT_NAME.name(), oldFontName, fontName);
      this.fontNameSet = true;
    }
  }

  /**
   * Returns the edited font name or <code>null</code> for default system font.
   */
  public String getFontName() {
    return this.fontName;
  }

  /**
   * Sets the edited font size.
   */
  public void setFontSize(Float fontSize) {
    if (fontSize != this.fontSize) {
      Float oldFontSize = this.fontSize;
      this.fontSize = fontSize;
      this.propertyChangeSupport.firePropertyChange(Property.FONT_SIZE.name(), oldFontSize, fontSize);
    }
  }

  /**
   * Returns the edited font size.
   */
  public Float getFontSize() {
    return this.fontSize;
  }

  /**
   * Returns <code>true</code> if all edited labels use the same font name.
   */
  public boolean isFontNameSet() {
    return this.fontNameSet;
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
   * Sets the edited pitch.
   */
  public void setPitch(Float pitch) {
    if (pitch != this.pitch) {
      Float oldPitch = this.pitch;
      this.pitch = pitch;
      this.propertyChangeSupport.firePropertyChange(Property.PITCH.name(), oldPitch, pitch);
    }
    this.pitchEnabled = pitch != null;
  }
  
  /**
   * Returns the edited pitch.
   */
  public Float getPitch() {
    return this.pitch;
  }
  
  /**
   * Returns <code>Boolean.TRUE</code> if all edited labels are viewed in 3D, 
   * or <code>Boolean.FALSE</code> if no label is viewed in 3D.
   */
  public Boolean isPitchEnabled() {
    return this.pitchEnabled;
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
   * Controls the creation of a label.
   */
  public void createLabel() {
    String text = getText();
    
    if (text != null && text.trim().length() > 0) {
      // Apply modification
      List<Selectable> oldSelection = this.home.getSelectedItems();
      boolean basePlanLocked = this.home.isBasePlanLocked();    
      boolean allLevelsSelection = this.home.isAllLevelsSelection();
      Label label = new Label(text, x, y);
      String fontName = getFontName();
      Float fontSize = getFontSize();
      if (fontName != null 
          || fontSize != null
          || getPitch() != null) {
        TextStyle style = this.preferences.getDefaultTextStyle(Label.class);
        if (fontName != null) {
          style = style.deriveStyle(fontName);
        } 
        if (fontSize != null) {
          style = style.deriveStyle(fontSize);
        }
        label.setStyle(style);
      }
      if (color != null) {
        label.setColor(color);
      }

      label.setColor(getColor());
      label.setPitch(getPitch());
      label.setElevation(getElevation());
      // Unlock base plan if label is a part of it
      boolean newBasePlanLocked = basePlanLocked && !isLabelPartOfBasePlan(label);
      doAddLabel(this.home, label, newBasePlanLocked); 
      if (this.undoSupport != null) {
        UndoableEdit undoableEdit = new LabelCreationUndoableEdit(
            this.home, this.preferences, oldSelection, basePlanLocked, allLevelsSelection, label, newBasePlanLocked);
        this.undoSupport.postEdit(undoableEdit);
      }
      this.preferences.addAutoCompletionString("LabelText", text);
    }
  }

  /**
   * Undoable edit for label creation. This class isn't anonymous to avoid
   * being bound to controller and its view.
   */
  private static class LabelCreationUndoableEdit extends AbstractUndoableEdit {
    private final Home             home;
    private final UserPreferences  preferences;
    private final List<Selectable> oldSelection;
    private final boolean          oldBasePlanLocked;
    private final boolean          oldAllLevelsSelection;
    private final Label            label;
    private final boolean          newBasePlanLocked;

    private LabelCreationUndoableEdit(Home home,
                                      UserPreferences preferences, 
                                      List<Selectable> oldSelection, 
                                      boolean oldBasePlanLocked, 
                                      boolean oldAllLevelsSelection,
                                      Label label, 
                                      boolean newBasePlanLocked) {
      this.home = home;
      this.preferences = preferences;
      this.oldSelection = oldSelection;
      this.oldBasePlanLocked = oldBasePlanLocked;
      this.oldAllLevelsSelection = oldAllLevelsSelection;
      this.label = label;
      this.newBasePlanLocked = newBasePlanLocked;
    }

    @Override
    public void undo() throws CannotUndoException {
      super.undo();
      doDeleteLabel(this.home, this.label, this.oldBasePlanLocked);
      this.home.setSelectedItems(this.oldSelection);
      this.home.setAllLevelsSelection(this.oldAllLevelsSelection);
    }

    @Override
    public void redo() throws CannotRedoException {
      super.redo();
      doAddLabel(this.home, this.label, this.newBasePlanLocked); 
    }

    @Override
    public String getPresentationName() {
      return this.preferences.getLocalizedString(LabelController.class, "undoCreateLabelName");
    }
  }

  /**
   * Adds label to home and selects it.
   */
  private static void doAddLabel(Home home, 
                                 Label label, 
                                 boolean basePlanLocked) {
    home.addLabel(label);
    home.setBasePlanLocked(basePlanLocked);
    home.setSelectedItems(Arrays.asList(new Selectable [] {label}));
    home.setAllLevelsSelection(false);
  }

  /**
   * Deletes label from home.
   */
  private static void doDeleteLabel(Home home, Label label, boolean basePlanLocked) {
    home.deleteLabel(label);
    home.setBasePlanLocked(basePlanLocked);
  }

  /**
   * Returns <code>true</code>.
   */
  protected boolean isLabelPartOfBasePlan(Label label) {
    return true;
  }

  /**
   * Controls the modification of selected labels.
   */
  public void modifyLabels() {
    List<Selectable> oldSelection = this.home.getSelectedItems();
    List<Label> selectedLabels = Home.getLabelsSubList(oldSelection);
    if (!selectedLabels.isEmpty()) {
      String text = getText();
      String fontName = getFontName();
      boolean fontNameSet = isFontNameSet();
      Float fontSize = getFontSize();
      Integer color = getColor();
      Float pitch = getPitch();
      Boolean pitchEnabled = isPitchEnabled();
      Float elevation = getElevation();
      
      // Create an array of modified labels with their current properties values
      ModifiedLabel [] modifiedLabels = new ModifiedLabel [selectedLabels.size()]; 
      for (int i = 0; i < modifiedLabels.length; i++) {
        modifiedLabels [i] = new ModifiedLabel(selectedLabels.get(i));
      }
      // Apply modification
      TextStyle defaultStyle = this.preferences.getDefaultTextStyle(Label.class);
      doModifyLabels(modifiedLabels, text, fontName, fontNameSet, fontSize, defaultStyle, color, pitch, pitchEnabled, elevation); 
      if (this.undoSupport != null) {
        UndoableEdit undoableEdit = new LabelModificationUndoableEdit(this.home, 
            this.preferences, oldSelection, modifiedLabels, text, fontName, fontNameSet, fontSize, defaultStyle, color, pitch, pitchEnabled, elevation);
        this.undoSupport.postEdit(undoableEdit);
      }      
      this.preferences.addAutoCompletionString("LabelText", text);
    }
  }
  
  /**
   * Undoable edit for label modification. This class isn't anonymous to avoid
   * being bound to controller and its view.
   */
  private static class LabelModificationUndoableEdit extends AbstractUndoableEdit {
    private final Home             home;
    private final UserPreferences  preferences;
    private final List<Selectable> oldSelection;
    private final ModifiedLabel [] modifiedLabels;
    private final String           text;
    private final String           fontName;
    private final boolean          fontNameSet;
    private final Float            fontSize;
    private final TextStyle        defaultStyle;
    private final Integer          color;
    private final Float            pitch;
    private final Boolean          pitchEnabled;
    private final Float            elevation;

    private LabelModificationUndoableEdit(Home home,
                                          UserPreferences preferences, 
                                          List<Selectable> oldSelection,
                                          ModifiedLabel [] modifiedLabels,
                                          String text, 
                                          String fontName, boolean fontNameSet, Float fontSize, TextStyle defaultStyle,
                                          Integer color, Float pitch, Boolean pitchEnabled,
                                          Float elevation) {
      this.home = home;
      this.preferences = preferences;
      this.oldSelection = oldSelection;
      this.modifiedLabels = modifiedLabels;
      this.text = text;
      this.fontName = fontName;
      this.fontNameSet = fontNameSet;
      this.fontSize = fontSize;
      this.defaultStyle = defaultStyle;
      this.color = color;
      this.pitch = pitch;
      this.pitchEnabled = pitchEnabled;
      this.elevation = elevation;
    }

    @Override
    public void undo() throws CannotUndoException {
      super.undo();
      undoModifyLabels(this.modifiedLabels); 
      this.home.setSelectedItems(this.oldSelection); 
    }

    @Override
    public void redo() throws CannotRedoException {
      super.redo();
      doModifyLabels(this.modifiedLabels, this.text, this.fontName, this.fontNameSet, this.fontSize, this.defaultStyle,
          this.color, this.pitch, this.pitchEnabled, this.elevation); 
      this.home.setSelectedItems(this.oldSelection); 
    }

    @Override
    public String getPresentationName() {
      return this.preferences.getLocalizedString(LabelController.class, 
          "undoModifyLabelsName");
    }
  }

  /**
   * Modifies labels properties with the values in parameter.
   */
  private static void doModifyLabels(ModifiedLabel [] modifiedLabels, 
                                     String text, String fontName, boolean fontNameSet, 
                                     Float fontSize, TextStyle defaultStyle,
                                     Integer color, Float pitch, Boolean pitchEnabled,
                                     Float elevation) {
    for (ModifiedLabel modifiedLabel : modifiedLabels) {
      Label label = modifiedLabel.getLabel();
      if (text != null) {
        label.setText(text);
      }
      if (fontNameSet) {
        label.setStyle(label.getStyle() != null
            ? label.getStyle().deriveStyle(fontName)
            : defaultStyle.deriveStyle(fontName));
      }
      if (fontSize != null) {
        label.setStyle(label.getStyle() != null
            ? label.getStyle().deriveStyle(fontSize)
            : defaultStyle.deriveStyle(fontSize));
      }
      if (color != null) {
        label.setColor(color);
      }
      if (pitchEnabled != null) {
        if (Boolean.FALSE.equals(pitchEnabled)) {
          label.setPitch(null);
        } else if (pitch != null) {
          label.setPitch(pitch);
          if (label.getStyle() == null) {
            label.setStyle(defaultStyle);
          }
        }
      }
      if (elevation != null) {
        label.setElevation(elevation);
      }
    }
  }

  /**
   * Restores label properties from the values stored in <code>modifiedLabels</code>.
   */
  private static void undoModifyLabels(ModifiedLabel [] modifiedLabels) {
    for (ModifiedLabel modifiedPiece : modifiedLabels) {
      modifiedPiece.reset();
    }
  }

  /**
   * Stores the current properties values of a modified label.
   */
  private static final class ModifiedLabel {
    private final Label     label;
    private final String    text;
    private final TextStyle style;
    private final Integer   color;
    private final Float     pitch;
    private final float     elevation;

    public ModifiedLabel(Label label) {
      this.label = label;
      this.text = label.getText();
      this.style = label.getStyle();
      this.color = label.getColor();
      this.pitch = label.getPitch();
      this.elevation = label.getElevation();
    }

    public Label getLabel() {
      return this.label;
    }

    public void reset() {
      this.label.setText(this.text);         
      this.label.setStyle(this.style);         
      this.label.setColor(this.color);         
      this.label.setPitch(this.pitch);         
      this.label.setElevation(this.elevation);         
    }
  }
}
