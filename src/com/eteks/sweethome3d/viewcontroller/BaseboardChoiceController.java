/*
 * BaseboardChoiceController.java 24 mai 2015
 *
 * Sweet Home 3D, Copyright (c) 2015 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import com.eteks.sweethome3d.model.Baseboard;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * A MVC controller for baseboard choice view.
 * @author Emmanuel Puybaret
 * @since 5.0
 */
public class BaseboardChoiceController implements Controller {
  /**
   * The properties that may be edited by the view associated to this controller. 
   */
  public enum Property {VISIBLE, COLOR, PAINT, HEIGHT, MAX_HEIGHT, THICKNESS}
  
  /**
   * The possible values for {@linkplain #getPaint() paint type}.
   */
  public enum BaseboardPaint {DEFAULT, COLORED, TEXTURED} 

  private final UserPreferences       preferences;
  private final ViewFactory           viewFactory;
  private final ContentManager        contentManager;
  private TextureChoiceController     textureController;
  private final PropertyChangeSupport propertyChangeSupport;
  private View                        view;

  private Boolean   visible;
  private Float     thickness;
  private Float     height;
  private Float     maxHeight;
  private Integer   color;
  private BaseboardPaint paint;

  /**
   * Creates the controller of room view with undo support.  
   */
  public BaseboardChoiceController(UserPreferences preferences, 
                                   ViewFactory viewFactory,
                                   ContentManager contentManager) {
    this.preferences = preferences;
    this.viewFactory = viewFactory;
    this.contentManager = contentManager;
    this.propertyChangeSupport = new PropertyChangeSupport(this);
  }

  /**
   * Returns the texture controller of the baseboard.
   */
  public TextureChoiceController getTextureController() {
    // Create sub controller lazily only once it's needed
    if (this.textureController == null) {
      this.textureController = new TextureChoiceController(
          this.preferences.getLocalizedString(BaseboardChoiceController.class, "baseboardTextureTitle"), 
          this.preferences, this.viewFactory, this.contentManager);
      this.textureController.addPropertyChangeListener(TextureChoiceController.Property.TEXTURE,
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              setPaint(BaseboardPaint.TEXTURED);
            }
          });
    }
    return this.textureController;
  }

  /**
   * Returns the view associated with this controller.
   */
  public View getView() {
    // Create view lazily only once it's needed
    if (this.view == null) {
      this.view = this.viewFactory.createBaseboardChoiceView(this.preferences, this); 
    }
    return this.view;
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
   * Returns <code>true</code> if the baseboard should be visible.
   */
  public Boolean getVisible() {
    return this.visible;
  }

  /**
   * Sets whether the baseboard should be visible.
   */
  public void setVisible(Boolean baseboardVisible) {
    if (baseboardVisible != this.visible) {
      Boolean oldVisible = this.visible;
      this.visible = baseboardVisible;
      this.propertyChangeSupport.firePropertyChange(Property.VISIBLE.name(), 
          oldVisible, baseboardVisible);
    }
  }
  
  /**
   * Sets the edited thickness of the baseboard.
   */
  public void setThickness(Float baseboardThickness) {
    if (baseboardThickness != this.thickness) {
      Float oldThickness = this.thickness;
      this.thickness = baseboardThickness;
      this.propertyChangeSupport.firePropertyChange(Property.THICKNESS.name(), 
          oldThickness, baseboardThickness);
    }
  }
  
  /**
   * Returns the edited thickness of the baseboard.
   */
  public Float getThickness() {
    return this.thickness;
  }
  
  /**
   * Sets the edited height of the baseboard.
   */
  public void setHeight(Float baseboardHeight) {
    if (baseboardHeight != this.height) {
      Float oldHeight = this.height;
      this.height = baseboardHeight;
      this.propertyChangeSupport.firePropertyChange(Property.HEIGHT.name(), 
          oldHeight, baseboardHeight);
    }
  }
  
  /**
   * Returns the edited height of the baseboard.
   */
  public Float getHeight() {
    return this.height;
  }
  
  /**
   * Sets the maximum height allowed for the edited baseboard.
   */
  public void setMaxHeight(Float maxHeight) {
    if (this.maxHeight == null
        || maxHeight != this.maxHeight) {
      Float oldMaxHeight = this.maxHeight;
      this.maxHeight = maxHeight;
      this.propertyChangeSupport.firePropertyChange(Property.MAX_HEIGHT.name(), 
          oldMaxHeight, maxHeight);
    }
  }
  
  /**
   * Returns the maximum height allowed for the edited baseboard.
   */
  public Float getMaxHeight() {
    return this.maxHeight;
  }
  
  /**
   * Sets the edited color of the baseboard.
   */
  public void setColor(Integer baseboardColor) {
    if (baseboardColor != this.color) {
      Integer oldColor = this.color;
      this.color = baseboardColor;
      this.propertyChangeSupport.firePropertyChange(Property.COLOR.name(), oldColor, baseboardColor);
      
      setPaint(BaseboardPaint.COLORED);
    }
  }
  
  /**
   * Returns the edited color of the baseboard.
   */
  public Integer getColor() {
    return this.color;
  }

  /**
   * Sets whether the baseboard is as its wall, colored, textured or unknown painted.
   */
  public void setPaint(BaseboardPaint baseboardPaint) {
    if (baseboardPaint != this.paint) {
      BaseboardPaint oldPaint = this.paint;
      this.paint = baseboardPaint;
      this.propertyChangeSupport.firePropertyChange(Property.PAINT.name(), oldPaint, baseboardPaint);
    }
  }
  
  /**
   * Returns whether the baseboard side is colored, textured or unknown painted.
   * @return {@link BaseboardPaint#DEFAULT}, {@link BaseboardPaint#COLORED}, {@link BaseboardPaint#TEXTURED} or <code>null</code>
   */
  public BaseboardPaint getPaint() {
    return this.paint;
  }

  /**
   * Set controller properties from the given <code>baseboard</code>.
   */
  public void setBaseboard(Baseboard baseboard) {
    if (baseboard == null) {
      setVisible(false);
      setThickness(null);
      setHeight(null);
      setColor(null);
      getTextureController().setTexture(null);
      setPaint(null);
    } else {
      setVisible(true);
      setThickness(baseboard.getThickness());
      setHeight(baseboard.getHeight());
      if (baseboard.getTexture() != null) {
        setColor(null);
        getTextureController().setTexture(baseboard.getTexture());
        setPaint(BaseboardPaint.TEXTURED);
      } else if (baseboard.getColor() != null) {
        getTextureController().setTexture(null);
        setColor(baseboard.getColor());
      } else {
        setColor(null);
        getTextureController().setTexture(null);
        setPaint(BaseboardPaint.DEFAULT);
      }
    }
  }
}
