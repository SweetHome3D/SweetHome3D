/*
 * ViewFactoryAdapter.java 22 nov. 2012
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

import com.eteks.sweethome3d.model.BackgroundImage;
import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.CatalogTexture;
import com.eteks.sweethome3d.model.FurnitureCatalog;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * A view factory with all its methods throwing
 * <code>UnsupportedOperationException</code> exception.   
 * @author Emmanuel Puybaret
 * @since 4.0
 */
public class ViewFactoryAdapter implements ViewFactory {
  /**
   * @throws UnsupportedOperationException
   */
  public View createBackgroundImageWizardStepsView(BackgroundImage backgroundImage, UserPreferences preferences,
                                                   BackgroundImageWizardController backgroundImageWizardController) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public View createFurnitureCatalogView(FurnitureCatalog catalog, UserPreferences preferences,
                                         FurnitureCatalogController furnitureCatalogController) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public View createFurnitureView(Home home, UserPreferences preferences, FurnitureController furnitureController) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public HelpView createHelpView(UserPreferences preferences, HelpController helpController) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public DialogView createHome3DAttributesView(UserPreferences preferences,
                                               Home3DAttributesController home3DAttributesController) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public DialogView createLevelView(UserPreferences preferences, LevelController levelController) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public DialogView createHomeFurnitureView(UserPreferences preferences,
                                            HomeFurnitureController homeFurnitureController) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public HomeView createHomeView(Home home, UserPreferences preferences, HomeController homeController) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public ImportedFurnitureWizardStepsView createImportedFurnitureWizardStepsView(CatalogPieceOfFurniture piece,
              String modelName, boolean importHomePiece, UserPreferences preferences,
              ImportedFurnitureWizardController importedFurnitureWizardController) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public View createImportedTextureWizardStepsView(CatalogTexture texture, String textureName,
                                                   UserPreferences preferences,
                                                   ImportedTextureWizardController importedTextureWizardController) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public DialogView createLabelView(boolean modification, UserPreferences preferences,
                                    LabelController labelController) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public DialogView createPageSetupView(UserPreferences preferences, PageSetupController pageSetupController) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public PlanView createPlanView(Home home, UserPreferences preferences, PlanController planController) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public DialogView createPrintPreviewView(Home home, UserPreferences preferences, HomeController homeController,
                                           PrintPreviewController printPreviewController) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public DialogView createRoomView(UserPreferences preferences, RoomController roomController) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public DialogView createCompassView(UserPreferences preferences, CompassController compassController) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public DialogView createObserverCameraView(UserPreferences preferences,
                                             ObserverCameraController home3dAttributesController) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public TextureChoiceView createTextureChoiceView(UserPreferences preferences,
                                                   TextureChoiceController textureChoiceController) {
    throw new UnsupportedOperationException();
  }
  
  /**
   * @throws UnsupportedOperationException
   */
  public View createModelMaterialsView(UserPreferences preferences, 
                                       ModelMaterialsController modelMaterialsController) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public ThreadedTaskView createThreadedTaskView(String taskMessage, UserPreferences preferences,
                                                 ThreadedTaskController controller) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public DialogView createUserPreferencesView(UserPreferences preferences,
                                              UserPreferencesController userPreferencesController) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public View createView3D(final Home home, UserPreferences preferences, final HomeController3D controller) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public DialogView createWallView(UserPreferences preferences, WallController wallController) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public DialogView createWizardView(UserPreferences preferences, WizardController wizardController) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public DialogView createPhotoView(Home home, UserPreferences preferences, PhotoController photoController) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public DialogView createPhotosView(Home home, UserPreferences preferences, PhotosController photosController) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public DialogView createVideoView(Home home, UserPreferences preferences, VideoController videoController) {
    throw new UnsupportedOperationException();
  }
}
