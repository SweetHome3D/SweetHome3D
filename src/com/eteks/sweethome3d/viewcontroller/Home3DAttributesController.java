/*
 * Home3DAttributesController.java 25 juin 07
 *
 * Sweet Home 3D, Copyright (c) 2007 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeEnvironment;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * A MVC controller for home 3D attributes view.
 * @author Emmanuel Puybaret
 */
public class Home3DAttributesController implements Controller {
  /**
   * The properties that may be edited by the view associated to this controller.
   */
  public enum Property {GROUND_COLOR, GROUND_PAINT, BACKGROUND_IMAGE_VISIBLE_ON_GROUND_3D, SKY_COLOR, SKY_PAINT, LIGHT_COLOR, WALLS_ALPHA, WALLS_TOP_COLOR}
  /**
   * The possible values for {@linkplain #getGroundPaint() ground paint type}.
   */
  public enum EnvironmentPaint {COLORED, TEXTURED}

  private final Home                  home;
  private final UserPreferences       preferences;
  private final ViewFactory           viewFactory;
  private final ContentManager        contentManager;
  private final UndoableEditSupport   undoSupport;
  private TextureChoiceController     groundTextureController;
  private TextureChoiceController     skyTextureController;
  private final PropertyChangeSupport propertyChangeSupport;
  private DialogView                  home3DAttributesView;

  private int               groundColor;
  private EnvironmentPaint  groundPaint;
  private boolean           backgroundImageVisibleOnGround3D;
  private int               skyColor;
  private EnvironmentPaint  skyPaint;
  private int               lightColor;
  private float             wallsAlpha;

  /**
   * Creates the controller of 3D view with undo support.
   */
  public Home3DAttributesController(Home home,
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
   * Returns the texture controller of the ground.
   */
  public TextureChoiceController getGroundTextureController() {
    // Create sub controller lazily only once it's needed
    if (this.groundTextureController == null) {
      this.groundTextureController = new TextureChoiceController(
          this.preferences.getLocalizedString(Home3DAttributesController.class, "groundTextureTitle"),
          this.preferences, this.viewFactory, this.contentManager);
      this.groundTextureController.addPropertyChangeListener(TextureChoiceController.Property.TEXTURE,
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              setGroundPaint(EnvironmentPaint.TEXTURED);
            }
          });
    }
    return this.groundTextureController;
  }

  /**
   * Returns the texture controller of the sky.
   */
  public TextureChoiceController getSkyTextureController() {
    // Create sub controller lazily only once it's needed
    if (this.skyTextureController == null) {
      this.skyTextureController = new TextureChoiceController(
          this.preferences.getLocalizedString(Home3DAttributesController.class, "skyTextureTitle"),
          false,
          this.preferences, this.viewFactory, this.contentManager);
      this.skyTextureController.addPropertyChangeListener(TextureChoiceController.Property.TEXTURE,
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              setSkyPaint(EnvironmentPaint.TEXTURED);
            }
          });
    }
    return this.skyTextureController;
  }

  /**
   * Returns the view associated with this controller.
   */
  public DialogView getView() {
    // Create view lazily only once it's needed
    if (this.home3DAttributesView == null) {
      this.home3DAttributesView = this.viewFactory.createHome3DAttributesView(
          this.preferences, this);
    }
    return this.home3DAttributesView;
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
   * Updates edited properties from the 3D attributes of the home edited by this controller.
   */
  protected void updateProperties() {
    HomeEnvironment homeEnvironment = this.home.getEnvironment();
    setGroundColor(homeEnvironment.getGroundColor());
    HomeTexture groundTexture = homeEnvironment.getGroundTexture();
    getGroundTextureController().setTexture(groundTexture);
    if (groundTexture != null) {
      setGroundPaint(EnvironmentPaint.TEXTURED);
    } else {
      setGroundPaint(EnvironmentPaint.COLORED);
    }
    setBackgroundImageVisibleOnGround3D(homeEnvironment.isBackgroundImageVisibleOnGround3D());
    setSkyColor(homeEnvironment.getSkyColor());
    HomeTexture skyTexture = homeEnvironment.getSkyTexture();
    getSkyTextureController().setTexture(skyTexture);
    if (skyTexture != null) {
      setSkyPaint(EnvironmentPaint.TEXTURED);
    } else {
      setSkyPaint(EnvironmentPaint.COLORED);
    }
    setLightColor(homeEnvironment.getLightColor());
    setWallsAlpha(homeEnvironment.getWallsAlpha());
  }

  /**
   * Sets the edited ground color.
   */
  public void setGroundColor(int groundColor) {
    if (groundColor != this.groundColor) {
      int oldGroundColor = this.groundColor;
      this.groundColor = groundColor;
      this.propertyChangeSupport.firePropertyChange(Property.GROUND_COLOR.name(), oldGroundColor, groundColor);

      setGroundPaint(EnvironmentPaint.COLORED);
    }
  }

  /**
   * Returns the edited ground color.
   */
  public int getGroundColor() {
    return this.groundColor;
  }

  /**
   * Sets whether the ground is colored or textured.
   */
  public void setGroundPaint(EnvironmentPaint groundPaint) {
    if (groundPaint != this.groundPaint) {
      EnvironmentPaint oldGroundPaint = this.groundPaint;
      this.groundPaint = groundPaint;
      this.propertyChangeSupport.firePropertyChange(Property.GROUND_PAINT.name(), oldGroundPaint, groundPaint);
    }
  }

  /**
   * Returns whether the ground is colored or textured.
   */
  public EnvironmentPaint getGroundPaint() {
    return this.groundPaint;
  }

  /**
   * Returns <code>true</code> if the background image should be displayed on the ground in 3D.
   * @since 6.0
   */
  public boolean isBackgroundImageVisibleOnGround3D() {
    return this.backgroundImageVisibleOnGround3D;
  }

  /**
   * Sets whether the background image should be displayed on the ground in 3D.
   * @since 6.0
   */
  public void setBackgroundImageVisibleOnGround3D(boolean backgroundImageVisibleOnGround3D) {
    if (this.backgroundImageVisibleOnGround3D != backgroundImageVisibleOnGround3D) {
      this.backgroundImageVisibleOnGround3D = backgroundImageVisibleOnGround3D;
      this.propertyChangeSupport.firePropertyChange(Property.BACKGROUND_IMAGE_VISIBLE_ON_GROUND_3D.name(),
          !backgroundImageVisibleOnGround3D, backgroundImageVisibleOnGround3D);
    }
  }

  /**
   * Sets the edited sky color.
   */
  public void setSkyColor(int skyColor) {
    if (skyColor != this.skyColor) {
      int oldSkyColor = this.skyColor;
      this.skyColor = skyColor;
      this.propertyChangeSupport.firePropertyChange(Property.SKY_COLOR.name(), oldSkyColor, skyColor);
    }
  }

  /**
   * Returns the edited sky color.
   */
  public int getSkyColor() {
    return this.skyColor;
  }

  /**
   * Sets whether the sky is colored or textured.
   */
  public void setSkyPaint(EnvironmentPaint skyPaint) {
    if (skyPaint != this.skyPaint) {
      EnvironmentPaint oldSkyPaint = this.skyPaint;
      this.skyPaint = skyPaint;
      this.propertyChangeSupport.firePropertyChange(Property.SKY_PAINT.name(), oldSkyPaint, skyPaint);
    }
  }

  /**
   * Returns whether the sky is colored or textured.
   */
  public EnvironmentPaint getSkyPaint() {
    return this.skyPaint;
  }

  /**
   * Sets the edited light color.
   */
  public void setLightColor(int lightColor) {
    if (lightColor != this.lightColor) {
      int oldLightColor = this.lightColor;
      this.lightColor = lightColor;
      this.propertyChangeSupport.firePropertyChange(Property.LIGHT_COLOR.name(), oldLightColor, lightColor);
    }
  }

  /**
   * Returns the edited light color.
   */
  public int getLightColor() {
    return this.lightColor;
  }

  /**
   * Sets the edited walls transparency alpha.
   */
  public void setWallsAlpha(float wallsAlpha) {
    if (wallsAlpha != this.wallsAlpha) {
      float oldWallsAlpha = this.wallsAlpha;
      this.wallsAlpha = wallsAlpha;
      this.propertyChangeSupport.firePropertyChange(Property.WALLS_ALPHA.name(), oldWallsAlpha, wallsAlpha);
    }
  }

  /**
   * Returns the edited walls transparency alpha.
   */
  public float getWallsAlpha() {
    return this.wallsAlpha;
  }

  /**
   * Controls the modification of the 3D attributes of the edited home.
   */
  public void modify3DAttributes() {
    int   groundColor = getGroundColor();
    HomeTexture groundTexture = getGroundPaint() == EnvironmentPaint.TEXTURED
        ? getGroundTextureController().getTexture()
        : null;
    boolean backgroundImageVisibleOnGround3D = isBackgroundImageVisibleOnGround3D();
    int   skyColor = getSkyColor();
    HomeTexture skyTexture = getSkyPaint() == EnvironmentPaint.TEXTURED
        ? getSkyTextureController().getTexture()
        : null;
    int   lightColor  = getLightColor();
    float wallsAlpha = getWallsAlpha();

    HomeEnvironment homeEnvironment = this.home.getEnvironment();
    int   oldGroundColor = homeEnvironment.getGroundColor();
    boolean oldBackgroundImageVisibleOnGround3D = homeEnvironment.isBackgroundImageVisibleOnGround3D();
    HomeTexture oldGroundTexture = homeEnvironment.getGroundTexture();
    int   oldSkyColor = homeEnvironment.getSkyColor();
    HomeTexture oldSkyTexture = homeEnvironment.getSkyTexture();
    int   oldLightColor = homeEnvironment.getLightColor();
    float oldWallsAlpha = homeEnvironment.getWallsAlpha();

    // Apply modification
    doModify3DAttributes(home, groundColor, groundTexture, backgroundImageVisibleOnGround3D, skyColor,
        skyTexture, lightColor, wallsAlpha);
    if (this.undoSupport != null) {
      this.undoSupport.postEdit(new Home3DAttributesModificationUndoableEdit(
          this.home, this.preferences,
          oldGroundColor, oldGroundTexture,
          oldBackgroundImageVisibleOnGround3D, oldSkyColor,
          oldSkyTexture, oldLightColor, oldWallsAlpha,
          groundColor, groundTexture,
          backgroundImageVisibleOnGround3D, skyColor,
          skyTexture, lightColor, wallsAlpha));
    }
  }

  /**
   * Undoable edit for 3D attributes modification. This class isn't anonymous to avoid
   * being bound to controller and its view.
   */
  private static class Home3DAttributesModificationUndoableEdit extends LocalizedUndoableEdit {
    private final Home            home;
    private final int             oldGroundColor;
    private final HomeTexture     oldGroundTexture;
    private final boolean         oldBackgroundImageVisibleOnGround3D;
    private final int             oldSkyColor;
    private final HomeTexture     oldSkyTexture;
    private final int             oldLightColor;
    private final float           oldWallsAlpha;
    private final int             groundColor;
    private final HomeTexture     groundTexture;
    private final boolean         backgroundImageVisibleOnGround3D;
    private final int             skyColor;
    private final HomeTexture     skyTexture;
    private final int             lightColor;
    private final float           wallsAlpha;

    private Home3DAttributesModificationUndoableEdit(Home home,
                                                     UserPreferences preferences,
                                                     int oldGroundColor,
                                                     HomeTexture oldGroundTexture,
                                                     boolean oldBackgroundImageVisibleOnGround3D,
                                                     int oldSkyColor,
                                                     HomeTexture oldSkyTexture,
                                                     int oldLightColor,
                                                     float oldWallsAlpha,
                                                     int groundColor,
                                                     HomeTexture groundTexture,
                                                     boolean backgroundImageVisibleOnGround3D,
                                                     int skyColor,
                                                     HomeTexture skyTexture,
                                                     int lightColor,
                                                     float wallsAlpha) {
      super(preferences, Home3DAttributesController.class, "undoModify3DAttributesName");
      this.home = home;
      this.oldGroundColor = oldGroundColor;
      this.oldGroundTexture = oldGroundTexture;
      this.oldBackgroundImageVisibleOnGround3D = oldBackgroundImageVisibleOnGround3D;
      this.oldSkyColor = oldSkyColor;
      this.oldSkyTexture = oldSkyTexture;
      this.oldLightColor = oldLightColor;
      this.oldWallsAlpha = oldWallsAlpha;
      this.groundColor = groundColor;
      this.groundTexture = groundTexture;
      this.backgroundImageVisibleOnGround3D = backgroundImageVisibleOnGround3D;
      this.skyColor = skyColor;
      this.skyTexture = skyTexture;
      this.lightColor = lightColor;
      this.wallsAlpha = wallsAlpha;
    }

    @Override
    public void undo() throws CannotUndoException {
      super.undo();
      doModify3DAttributes(this.home, this.oldGroundColor, this.oldGroundTexture, this.oldBackgroundImageVisibleOnGround3D,
          this.oldSkyColor, this.oldSkyTexture, this.oldLightColor, this.oldWallsAlpha);
    }

    @Override
    public void redo() throws CannotRedoException {
      super.redo();
      doModify3DAttributes(this.home, this.groundColor, this.groundTexture, this.backgroundImageVisibleOnGround3D,
          this.skyColor, this.skyTexture, this.lightColor, this.wallsAlpha);
    }
  }

  /**
   * Modifies the 3D attributes of the given <code>home</code>.
   */
  private static void doModify3DAttributes(Home home,
                                           int groundColor, HomeTexture groundTexture,
                                           boolean backgroundImageVisibleOnGround3D,
                                           int skyColor, HomeTexture skyTexture,
                                           int lightColor,
                                           float wallsAlpha) {
    HomeEnvironment homeEnvironment = home.getEnvironment();
    homeEnvironment.setGroundColor(groundColor);
    homeEnvironment.setGroundTexture(groundTexture);
    homeEnvironment.setBackgroundImageVisibleOnGround3D(backgroundImageVisibleOnGround3D);
    homeEnvironment.setSkyColor(skyColor);
    homeEnvironment.setSkyTexture(skyTexture);
    homeEnvironment.setLightColor(lightColor);
    homeEnvironment.setWallsAlpha(wallsAlpha);
  }
}
