/*
 * ModelMaterialsController.java 26 oct. 2012
 *
 * Sweet Home 3D, Copyright (c) 2012 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.HomeMaterial;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * A MVC controller for model materials choice.
 * @author Emmanuel Puybaret
 * @since 4.0
 */
public class ModelMaterialsController implements Controller {
  public enum Property {MODEL, MATERIALS}

  private final String                title;
  private final UserPreferences       preferences;
  private final ViewFactory           viewFactory;
  private final ContentManager        contentManager;  
  private final PropertyChangeSupport propertyChangeSupport;
  private View                        materialsChoiceView;

  private TextureChoiceController     textureController;

  private Content                     model;
  private HomeMaterial []             materials;

  public ModelMaterialsController(String title, 
                                  UserPreferences preferences,
                                  ViewFactory    viewFactory,
                                  ContentManager contentManager) {
    this.title = title;
    this.preferences = preferences;
    this.viewFactory = viewFactory;
    this.contentManager = contentManager;
    this.propertyChangeSupport = new PropertyChangeSupport(this);
  }

  /**
   * Returns the view associated with this controller.
   */
  public View getView() {
    // Create view lazily only once it's needed
    if (this.materialsChoiceView == null) {
      this.materialsChoiceView = this.viewFactory.createModelMaterialsView(this.preferences, this);
    }
    return this.materialsChoiceView;
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
   * Sets the model displayed by view and fires a <code>PropertyChangeEvent</code>.
   */
  public void setModel(Content model) {
    if (this.model !=  model) {
      Content oldModel = this.model;
      this.model = model;
      this.propertyChangeSupport.firePropertyChange(Property.MODEL.name(), oldModel, model);
    }
  }
  
  /**
   * Returns the model displayed by view.
   */
  public Content getModel() {
    return this.model;
  }

  /**
   * Sets the materials displayed by view and fires a <code>PropertyChangeEvent</code>.
   */
  public void setMaterials(HomeMaterial [] texture) {
    if (!Arrays.equals(this.materials, texture)) {
      HomeMaterial [] oldMaterials = this.materials;
      this.materials = texture;
      this.propertyChangeSupport.firePropertyChange(Property.MATERIALS.name(), oldMaterials, texture);
    }
  }
  
  /**
   * Returns the materials displayed by view.
   */
  public HomeMaterial [] getMaterials() {
    return this.materials;
  }

  /**
   * Returns the text that should be displayed as materials choice dialog title.
   */
  public String getDialogTitle() {
    return this.title;
  }
  
  /**
   * Returns the texture controller of the model materials.
   */
  public TextureChoiceController getTextureController() {
    // Create sub controller lazily only once it's needed
    if (this.textureController == null
        && this.contentManager != null) {
      this.textureController = new TextureChoiceController(
          this.preferences.getLocalizedString(ModelMaterialsController.class, "textureTitle"), 
          this.preferences, this.viewFactory, this.contentManager);
    }
    return this.textureController;
  }
}
