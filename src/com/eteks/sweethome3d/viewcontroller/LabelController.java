/*
 * LabelController.java 29 nov. 2008
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
import java.util.ResourceBundle;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.Label;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * A MVC controller for label view.
 * @author Emmanuel Puybaret
 */
public class LabelController implements Controller {
  /**
   * The property that may be edited by the view associated to this controller. 
   */
  public enum Property {TEXT}
  
  private final Home                  home;
  private final Float                 x;
  private final Float                 y;
  private final UserPreferences       preferences;
  private final ViewFactory           viewFactory;
  private final UndoableEditSupport   undoSupport;
  private final PropertyChangeSupport propertyChangeSupport;
  private DialogView                  pageSetupView;

  private String text;
  
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
  }

  /**
   * Updates edited properties from selected labels in the home edited by this controller.
   */
  protected void updateProperties() {
    List<Label> selectedLabels = Home.getLabelsSubList(this.home.getSelectedItems());
    if (selectedLabels.isEmpty()) {
      setText(null); // Nothing to edit
    } else {
      // Search the common properties among selected furniture
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
    }
  }
  
  /**
   * Returns the view associated with this controller.
   */
  public DialogView getView() {
    // Create view lazily only once it's needed
    if (this.pageSetupView == null) {
      this.pageSetupView = this.viewFactory.createLabelView(this.x == null, this.preferences, this);
    }
    return this.pageSetupView;
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
   * Controls the creation of a label.
   */
  public void createLabel() {
    List<Selectable> oldSelection = this.home.getSelectedItems();
    String text = getText();
    
    // Apply modification
    Label label = new Label(text, x, y);
    doAddLabel(this.home, label); 
    if (this.undoSupport != null) {
      UndoableEdit undoableEdit = new LabelCreationUndoableEdit(this.home, oldSelection, label);
      this.undoSupport.postEdit(undoableEdit);
    }
  }

  /**
   * Undoable edit for label creation. This class isn't anonymous to avoid
   * being bound to controller and its view.
   */
  private static class LabelCreationUndoableEdit extends AbstractUndoableEdit {
    private final Home             home;
    private final List<Selectable> oldSelection;
    private final Label            label;

    private LabelCreationUndoableEdit(Home home,
                                      List<Selectable> oldSelection, Label label) {
      this.home = home;
      this.oldSelection = oldSelection;
      this.label = label;
    }

    @Override
    public void undo() throws CannotUndoException {
      super.undo();
      doDeleteLabel(this.home, this.label);
      this.home.setSelectedItems(this.oldSelection); 
    }

    @Override
    public void redo() throws CannotRedoException {
      super.redo();
      doAddLabel(this.home, this.label); 
    }

    @Override
    public String getPresentationName() {
      return ResourceBundle.getBundle(LabelController.class.getName()).
          getString("undoCreateLabelName");
    }
  }

  /**
   * Adds label to home and selects it.
   */
  private static void doAddLabel(Home home, Label label) {
    home.addLabel(label);
    home.setSelectedItems(Arrays.asList(new Selectable [] {label}));
  }

  /**
   * Deletes label from home.
   */
  private static void doDeleteLabel(Home home, Label label) {
    home.deleteLabel(label);
  }

  /**
   * Controls the modification of selected labels.
   */
  public void modifyLabels() {
    List<Selectable> oldSelection = this.home.getSelectedItems();
    List<Label> selectedLabels = Home.getLabelsSubList(oldSelection);
    if (!selectedLabels.isEmpty()) {
      String text = getText();
      
      // Create an array of modified labels with their current properties values
      ModifiedLabel [] modifiedLabels = new ModifiedLabel [selectedLabels.size()]; 
      for (int i = 0; i < modifiedLabels.length; i++) {
        modifiedLabels [i] = new ModifiedLabel(selectedLabels.get(i));
      }
      // Apply modification
      doModifyLabels(modifiedLabels, text); 
      if (this.undoSupport != null) {
        UndoableEdit undoableEdit = new LabelModificationUndoableEdit(home, 
            oldSelection, modifiedLabels, text);
        this.undoSupport.postEdit(undoableEdit);
      }
    }
  }
  
  /**
   * Undoable edit for label modification. This class isn't anonymous to avoid
   * being bound to controller and its view.
   */
  private static class LabelModificationUndoableEdit extends AbstractUndoableEdit {
    private final Home             home;
    private final List<Selectable> oldSelection;
    private final ModifiedLabel [] modifiedLabels;
    private final String           text;

    private LabelModificationUndoableEdit(Home home,
                                          List<Selectable> oldSelection,
                                          ModifiedLabel [] modifiedLabels,
                                          String text) {
      this.home = home;
      this.oldSelection = oldSelection;
      this.modifiedLabels = modifiedLabels;
      this.text = text;
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
      doModifyLabels(this.modifiedLabels, this.text); 
      this.home.setSelectedItems(this.oldSelection); 
    }

    @Override
    public String getPresentationName() {
      return ResourceBundle.getBundle(LabelController.class.getName()).
          getString("undoModifyLabelsName");
    }
  }

  /**
   * Modifies labels properties with the values in parameter.
   */
  private static void doModifyLabels(ModifiedLabel [] modifiedLabels, 
                                     String text) {
    for (ModifiedLabel modifiedPiece : modifiedLabels) {
      Label label = modifiedPiece.getLabel();
      label.setText(text != null 
          ? text : label.getText());
    }
  }

  /**
   * Restores furniture properties from the values stored in <code>modifiedLabels</code>.
   */
  private static void undoModifyLabels(ModifiedLabel [] modifiedLabels) {
    for (ModifiedLabel modifiedPiece : modifiedLabels) {
      Label label = modifiedPiece.getLabel();
      label.setText(modifiedPiece.getText());      
    }
  }

  /**
   * Stores the current properties values of a modified label of furniture.
   */
  private static final class ModifiedLabel {
    private final Label  label;
    private final String text;

    public ModifiedLabel(Label label) {
      this.label = label;
      this.text = label.getText();
    }

    public Label getLabel() {
      return this.label;
    }
    
    public String getText() {
      return this.text;
    }
  }
}
