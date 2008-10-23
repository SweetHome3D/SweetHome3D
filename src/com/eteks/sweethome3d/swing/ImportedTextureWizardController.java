/*
 * ImportedTextureWizardController.java 01 oct 2008
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
package com.eteks.sweethome3d.swing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import java.util.Collections;
import java.util.ResourceBundle;

import javax.swing.JComponent;

import com.eteks.sweethome3d.model.CatalogTexture;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.TexturesCatalog;
import com.eteks.sweethome3d.model.TexturesCategory;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * Wizard controller for background image in plan.
 * @author Emmanuel Puybaret
 */
public class ImportedTextureWizardController extends WizardController {
  public enum Property {IMAGE, NAME, CATEGORY, WIDTH, HEIGHT}

  public enum Step {IMAGE, ATTRIBUTES};
  
  private CatalogTexture                 texture;
  private UserPreferences                preferences;
  private PropertyChangeSupport          propertyChangeSupport;

  private ImportedTextureWizardStepState textureImageStepState;
  private ImportedTextureWizardStepState textureAttributesStepState;
  private JComponent                     stepsView;
  
  private Content                        image;
  private String                         name;
  private TexturesCategory               category;
  private float                          width;
  private float                          height;

  /**
   * Creates a controller that edits a new catalog texture.
   */
  public ImportedTextureWizardController(UserPreferences preferences,
                                         ContentManager contentManager) {
    this(null, null, preferences, contentManager);    
  }
  
  /**
   * Creates a controller that edits a new catalog texture with a given 
   * <code>textureName</code>.
   */
  public ImportedTextureWizardController(String textureName,
                                         UserPreferences preferences,
                                         ContentManager contentManager) {
    this(null, textureName, preferences, contentManager);    
  }
  
  /**
   * Creates a controller that edits <code>texture</code> values.
   */
  public ImportedTextureWizardController(CatalogTexture texture,
                                         UserPreferences preferences,
                                         ContentManager contentManager) {
    this(texture, null, preferences, contentManager);    
  }
  
  private ImportedTextureWizardController(CatalogTexture texture,
                                          String textureName,
                                          UserPreferences preferences,
                                          ContentManager contentManager) {
    this.texture = texture;
    this.preferences = preferences;
    this.propertyChangeSupport = new PropertyChangeSupport(this);
    // Create view
    this.stepsView = new ImportedTextureWizardStepsPanel(texture, textureName, 
        preferences, contentManager, this);
    ResourceBundle resource = ResourceBundle.getBundle(ImportedTextureWizardController.class.getName());
    setTitle(resource.getString(texture == null 
        ? "importTextureWizard.title" 
        : "modifyTextureWizard.title"));    
    // Initialize states
    this.textureImageStepState = new TextureImageStepState();
    this.textureAttributesStepState = new TextureAttributesStepState();
    setStepState(this.textureImageStepState);
    
    displayView();
  }

  /**
   * Changes background image in model and posts an undoable operation.
   */
  @Override
  public void finish() {
    CatalogTexture newTexture = new CatalogTexture(this.name, this.image, 
        this.width, this.height, true);
    // Remove the edited texture from catalog
    TexturesCatalog catalog = this.preferences.getTexturesCatalog();
    if (this.texture != null) {
      catalog.delete(this.texture);
    }
    catalog.add(this.category, newTexture);
  }
  
  /**
   * Returns the current step state.
   */
  @Override
  protected ImportedTextureWizardStepState getStepState() {
    return (ImportedTextureWizardStepState)super.getStepState();
  }
  
  /**
   * Returns the texture image step state.
   */
  protected ImportedTextureWizardStepState getTextureImageStepState() {
    return this.textureImageStepState;
  }

  /**
   * Returns the texture attributes step state.
   */
  protected ImportedTextureWizardStepState getTextureAttributesStepState() {
    return this.textureAttributesStepState;
  }
 
  /**
   * Returns the unique wizard view used for all steps.
   */
  protected JComponent getStepsView() {
    return this.stepsView;
  }

  /**
   * Switch in the wizard view to the given <code>step</code>.
   */
  protected void setStepView(Step step) {
    ((ImportedTextureWizardStepsPanel)getStepsView()).setStep(step);
  }

  /**
   * Adds the property change <code>listener</code> in parameter to this home.
   */
  public void addPropertyChangeListener(Property property, PropertyChangeListener listener) {
    this.propertyChangeSupport.addPropertyChangeListener(property.toString(), listener);
  }

  /**
   * Removes the property change <code>listener</code> in parameter from this home.
   */
  public void removePropertyChangeListener(Property property, PropertyChangeListener listener) {
    this.propertyChangeSupport.removePropertyChangeListener(property.toString(), listener);
  }

  /**
   * Sets the image content of the imported texture.
   */
  public void setImage(Content image) {
    if (image != this.image) {
      Content oldImage = this.image;
      this.image = image;
      this.propertyChangeSupport.firePropertyChange(Property.IMAGE.toString(), oldImage, image);
    }
  }
  
  /**
   * Returns the image content of the imported texture.
   */
  public Content getImage() {
    return this.image;
  }

  /**
   * Returns the name of the imported texture.
   */
  public String getName() {
    return this.name;
  }
  
  /**
   * Sets the name of the imported texture.
   */
  public void setName(String name) {
    if (name != this.name
        || (name != null && !name.equals(this.name))) {
      String oldName = this.name;
      this.name = name;
      if (this.propertyChangeSupport != null) {
        this.propertyChangeSupport.firePropertyChange(Property.NAME.toString(), oldName, name);
      }
    }
  }
  
  /**
   * Returns the category of the imported texture.
   */
  public TexturesCategory getCategory() {
    return this.category;
  }
  
  /**
   * Sets the category of the imported texture.
   */
  public void setCategory(TexturesCategory category) {
    if (category != this.category) {
      TexturesCategory oldCategory = this.category;
      this.category = category;
      this.propertyChangeSupport.firePropertyChange(Property.CATEGORY.toString(), oldCategory, category);
    }
  }
  
  /**
   * Returns the width.
   */
  public float getWidth() {
    return this.width;
  }
  
  /**
   * Sets the width of the imported texture.
   */
  public void setWidth(float width) {
    if (width != this.width) {
      float oldWidth = this.width;
      this.width = width;
      this.propertyChangeSupport.firePropertyChange(Property.WIDTH.toString(), oldWidth, width);
    }
  }

  /**
   * Returns the height.
   */
  public float getHeight() {
    return this.height;
  }

  /**
   * Sets the size of the imported texture.
   */
  public void setHeight(float height) {
    if (height != this.height) {
      float oldHeight = this.height;
      this.height = height;
      this.propertyChangeSupport.firePropertyChange(Property.HEIGHT.toString(), oldHeight, height);
    }
  }

  /**
   * Returns <code>true</code> if texture name is valid.
   */
  public boolean isTextureNameValid() {
    CatalogTexture temporaryTexture = new CatalogTexture(this.name, null, 0, 0);
    if (this.texture != null
        && this.category == this.texture.getCategory()
        // Check texture names are equal with binary search to keep locale dependence
        && Collections.binarySearch(this.category.getTextures(), this.texture)
              == Collections.binarySearch(this.category.getTextures(), temporaryTexture)) {
      // Accept piece name if it didn't change 
      return true;
    }
    return this.name != null
        && this.name.length() > 0
        && this.category != null
        && Collections.binarySearch(this.category.getTextures(), temporaryTexture) < 0;
  }

  /**
   * Step state superclass. All step state share the same step view,
   * that will display a different component depending on their class name. 
   */
  protected abstract class ImportedTextureWizardStepState extends WizardControllerStepState {
    private URL icon = ImportedTextureWizardController.class.getResource("resources/importedTextureWizard.png");
    
    public abstract Step getStep();

    @Override
    public void enter() {
      setStepView(getStep());
    }
    
    @Override
    public JComponent getView() {
      return getStepsView();
    }    
    
    @Override
    public URL getIcon() {
      return this.icon;
    }
  }
    
  /**
   * Texture image choice step state (first step).
   */
  private class TextureImageStepState extends ImportedTextureWizardStepState {
    public TextureImageStepState() {
      ImportedTextureWizardController.this.addPropertyChangeListener(Property.IMAGE, 
          new PropertyChangeListener() {
              public void propertyChange(PropertyChangeEvent evt) {
                setNextStepEnabled(getImage() != null);
              }
            });
    }
    
    @Override
    public void enter() {
      super.enter();
      setFirstStep(true);
      setNextStepEnabled(getImage() != null);
    }
    
    @Override
    public Step getStep() {
      return Step.IMAGE;
    }
    
    @Override
    public void goToNextStep() {
      setStepState(getTextureAttributesStepState());
    }
  }

  /**
   * Texture image attributes step state (last step).
   */
  private class TextureAttributesStepState extends ImportedTextureWizardStepState {
    private PropertyChangeListener widthChangeListener;
    private PropertyChangeListener heightChangeListener;
    private PropertyChangeListener nameAndCategoryChangeListener;
    
    public TextureAttributesStepState() {
      this.widthChangeListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            ImportedTextureWizardController.this.removePropertyChangeListener(Property.HEIGHT, heightChangeListener);
            float ratio = (Float)ev.getNewValue() / (Float)ev.getOldValue();
            setHeight(getHeight() * ratio);
            ImportedTextureWizardController.this.addPropertyChangeListener(Property.HEIGHT, heightChangeListener);
          }
        };
      this.heightChangeListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            ImportedTextureWizardController.this.removePropertyChangeListener(Property.WIDTH, widthChangeListener);
            float ratio = (Float)ev.getNewValue() / (Float)ev.getOldValue();
            setWidth(getWidth() * ratio); 
            ImportedTextureWizardController.this.addPropertyChangeListener(Property.WIDTH, widthChangeListener);
          }
        };
      this.nameAndCategoryChangeListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            setNextStepEnabled(isTextureNameValid());
          }
        };
    }
    
    @Override
    public void enter() {
      super.enter();
      setLastStep(true);
      
      ImportedTextureWizardController.this.addPropertyChangeListener(Property.WIDTH, this.widthChangeListener);
      ImportedTextureWizardController.this.addPropertyChangeListener(Property.HEIGHT, this.heightChangeListener);
      ImportedTextureWizardController.this.addPropertyChangeListener(Property.NAME, this.nameAndCategoryChangeListener);
      ImportedTextureWizardController.this.addPropertyChangeListener(Property.CATEGORY, this.nameAndCategoryChangeListener);
      
      // Last step is always valid by default
      setNextStepEnabled(isTextureNameValid());
    }
    
    @Override
    public Step getStep() {
      return Step.ATTRIBUTES;
    }
    
    @Override
    public void goBackToPreviousStep() {
      setStepState(getTextureImageStepState());
    }
    
    @Override
    public void exit() {
      ImportedTextureWizardController.this.removePropertyChangeListener(Property.WIDTH, this.widthChangeListener);
      ImportedTextureWizardController.this.removePropertyChangeListener(Property.HEIGHT, this.heightChangeListener);
      ImportedTextureWizardController.this.removePropertyChangeListener(Property.NAME, this.nameAndCategoryChangeListener);
      ImportedTextureWizardController.this.removePropertyChangeListener(Property.CATEGORY, this.nameAndCategoryChangeListener);
    }
  }
}
