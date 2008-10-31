/*
 * ViewFactory.java 28 oct. 2008
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

import java.util.List;

import com.eteks.sweethome3d.model.BackgroundImage;
import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.CatalogTexture;
import com.eteks.sweethome3d.model.FurnitureCatalog;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.plugin.Plugin;

/**
 * A factory that specifies how to create the views displayed in Sweet Home 3D. 
 * @author Emmanuel Puybaret
 */
public interface ViewFactory {
  /**
   * Returns a new view that displays furniture <code>catalog</code>.
   */
  public abstract FurnitureCatalogView createFurnitureCatalogView(FurnitureCatalog catalog,
                                           UserPreferences preferences,
                                           FurnitureCatalogController furnitureCatalogController);
  
  /**
   * Returns a new view that displays <code>home</code> furniture list.
   */
  public abstract FurnitureView createFurnitureView(Home home, UserPreferences preferences,
                                           FurnitureController furnitureController);

  /**
   * Returns a new view that displays <code>home</code> on a plan.
   */
  public abstract PlanView createPlanView(Home home, UserPreferences preferences,
                                          PlanController planController);

  /**
   * Returns a new view that displays <code>home</code> in 3D.
   */
  public abstract HomeView3D createView3D(Home home, UserPreferences preferences,
                                          HomeController3D homeController3D);

  /**
   * Returns a new view that displays <code>home</code> and its sub views.
   */
  public abstract HomeView createHomeView(Home home, UserPreferences preferences,
                                          ContentManager contentManager, List<Plugin> plugins,
                                          HomeController homeController);

  /**
   * Returns a new view that displays a wizard. 
   */
  public abstract WizardView createWizardView(WizardController wizardController);

  /**
   * Returns a new view that displays the different steps that helps the user to choose a background image. 
   */
  public abstract BackgroundImageWizardStepsView createBackgroundImageWizardStepsView(
                                 BackgroundImage backgroundImage,
                                 UserPreferences preferences, ContentManager contentManager,
                                 BackgroundImageWizardController backgroundImageWizardController);

  /**
   * Returns a new view that displays the different steps that helps the user to import furniture. 
   */
  public abstract ImportedFurnitureWizardStepsView createImportedFurnitureWizardStepsView(
                                 CatalogPieceOfFurniture piece,
                                 String modelName, boolean importHomePiece,
                                 UserPreferences preferences, ContentManager contentManager,
                                 ImportedFurnitureWizardController importedFurnitureWizardController);

  /**
   * Returns a new view that displays the different steps that helps the user to import a texture. 
   */
  public abstract ImportedTextureWizardStepsView createImportedTextureWizardStepsView(
                                 CatalogTexture texture, String textureName,
                                 UserPreferences preferences,
                                 ContentManager contentManager,
                                 ImportedTextureWizardController importedTextureWizardController);

  /**
   * Returns a new view that displays message for a threaded task.
   */
  public abstract ThreadedTaskView createThreadedTaskView(String taskMessage,
                                                          ThreadedTaskController threadedTaskController);

  /**
   * Returns a new view that edits user preferences.
   */
  public abstract UserPreferencesView createUserPreferencesView(
                                          UserPreferences preferences,
                                          UserPreferencesController userPreferencesController);
  /**
   * Returns a new view that edits furniture values.
   */
  public abstract HomeFurnitureView createHomeFurnitureView(UserPreferences preferences,
                                         HomeFurnitureController homeFurnitureController);

  /**
   * Returns a new view that edits wall values.
   */
  public abstract WallView createWallView(UserPreferences preferences,
                                          WallController wallController);

  /**
   * Returns a new view that edits 3D attributes.
   */
  public abstract Home3DAttributesView createHome3DAttributesView(UserPreferences preferences,
                                           Home3DAttributesController home3DAttributesController);

  /**
   * Returns a new view that edits the texture of its controller.  
   */
  public abstract TextureChoiceView createTextureChoiceView(String textureDialogTitle,
                                                     UserPreferences preferences,
                                                     TextureChoiceController textureChoiceController);

  /**
   * Creates a new view that edits page setup.
   */
  public abstract PageSetupView createPageSetupView(UserPreferences preferences,
                                                    PageSetupController pageSetupController);

  /**
   * Returns a new view that displays home print preview. 
   */
  public abstract PrintPreviewView createPrintPreviewView(Home home,
                                                 HomeController homeController,
                                                 PrintPreviewController printPreviewController);

  /**
   * Returns a new view that displays Sweet Home 3D help.
   */
  public abstract HelpView createHelpView(UserPreferences preferences,
                                          HelpController helpController);
}
