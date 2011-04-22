/*
 * TextureChoiceController.java 26 sept. 2008
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

import com.eteks.sweethome3d.model.CatalogTexture;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * A MVC controller for texture choice.
 * @author Emmanuel Puybaret
 */
public class TextureChoiceController implements Controller {
  public enum Property {TEXTURE}

  private final String                title;
  private final UserPreferences       preferences;
  private final ViewFactory           viewFactory;
  private final ContentManager        contentManager;  
  private final PropertyChangeSupport propertyChangeSupport;
  private TextureChoiceView           textureChoiceView;
  
  private HomeTexture           texture;


  public TextureChoiceController(String title, 
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
  public TextureChoiceView getView() {
    // Create view lazily only once it's needed
    if (this.textureChoiceView == null) {
      this.textureChoiceView = this.viewFactory.createTextureChoiceView(this.preferences, this);
    }
    return this.textureChoiceView;
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
   * Sets the texture displayed by view and fires a <code>PropertyChangeEvent</code>.
   */
  public void setTexture(HomeTexture texture) {
    if (this.texture != texture) {
      HomeTexture oldTexture = this.texture;
      this.texture = texture;
      this.propertyChangeSupport.firePropertyChange(Property.TEXTURE.name(), oldTexture, texture);
    }
  }
  
  /**
   * Returns the texture displayed by view.
   */
  public HomeTexture getTexture() {
    return this.texture;
  }

  /**
   * Returns the text that should be displayed as texture choice dialog title.
   */
  public String getDialogTitle() {
    return this.title;
  }

  /**
   * Controls texture import.
   */
  public void importTexture() {
    new ImportedTextureWizardController(this.preferences, 
        this.viewFactory, this.contentManager).displayView(getView());
  }

  /**
   * Controls the import of a texture with a given name.
   */
  public void importTexture(String textureName) {
    new ImportedTextureWizardController(textureName, this.preferences, 
        this.viewFactory, this.contentManager).displayView(getView());
  }
  
  /**
   * Controls the modification of a texture.
   */
  public void modifyTexture(CatalogTexture texture) {
    new ImportedTextureWizardController(texture, this.preferences, 
        this.viewFactory, this.contentManager).displayView(getView());
  }

  /**
   * Controls the deletion of a texture.
   */
  public void deleteTexture(CatalogTexture texture) {
    if (getView().confirmDeleteSelectedCatalogTexture()) {
      this.preferences.getTexturesCatalog().delete(texture);
    }
  }
}
