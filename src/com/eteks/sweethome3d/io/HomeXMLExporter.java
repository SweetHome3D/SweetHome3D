/*
 * HomeXMLExporter.java 
 *
 * Copyright (c) 2015 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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
package com.eteks.sweethome3d.io;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eteks.sweethome3d.model.BackgroundImage;
import com.eteks.sweethome3d.model.Baseboard;
import com.eteks.sweethome3d.model.Camera;
import com.eteks.sweethome3d.model.Compass;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.DimensionLine;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeDoorOrWindow;
import com.eteks.sweethome3d.model.HomeEnvironment;
import com.eteks.sweethome3d.model.HomeFurnitureGroup;
import com.eteks.sweethome3d.model.HomeLight;
import com.eteks.sweethome3d.model.HomeMaterial;
import com.eteks.sweethome3d.model.HomeObject;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.HomePrint;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.Label;
import com.eteks.sweethome3d.model.Level;
import com.eteks.sweethome3d.model.LightSource;
import com.eteks.sweethome3d.model.ObserverCamera;
import com.eteks.sweethome3d.model.Polyline;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.Sash;
import com.eteks.sweethome3d.model.TextStyle;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.tools.URLContent;

/**
 * Exporter for home instances. Homes will be written using the DTD given in {@link HomeXMLHandler} class.
 * @author Emmanuel Puybaret
 */
public class HomeXMLExporter extends ObjectXMLExporter<Home> {
  private Map<Content, String> savedContentNames;
  private Map<Level, String>   levelIds = new HashMap<Level, String>();
  private Map<Wall, String>    wallIds = new HashMap<Wall, String>();

  /**
   * Sets the names that will be saved as XML attribute values for each content.
   */
  void setSavedContentNames(Map<Content, String> savedContentNames) {
    this.savedContentNames = savedContentNames;
  }
  
  /**
   * Returns the XML id of the given <code>object</code> that can be referenced by other elements.
   * @throws IllegalArgumentException if the <code>object</code> has no associated id. 
   */
  protected String getId(Object object) {
    if (object == null) {
      return null;
    } else if (object instanceof Level) {
      return this.levelIds.get(object);
    } else if (object instanceof Wall) {
      return this.wallIds.get(object);
    } else {
      throw new IllegalArgumentException("No Id provided for object of class " + object.getClass().getName());
    }
  }
  
  /**
   * Writes in XML the <code>home</code> object and the objects that depends on it with the given <code>writer</code>.
   */
  @Override
  public void writeElement(XMLWriter writer, Home home) throws IOException {
    // Create level IDs
    int levelIndex = this.levelIds.size();
    for (Level level : home.getLevels()) {
      String levelId = "level" + levelIndex++;
      this.levelIds.put(level, levelId);
    }
    // Create wall IDs
    int wallIndex = this.wallIds.size();
    for (Wall wall : home.getWalls()) {
      this.wallIds.put(wall, "wall" + wallIndex++);
    }
    super.writeElement(writer, home);
  }
  
  /**
   * Writes as XML attributes some data of <code>home</code> object with the given <code>writer</code>.
   */
  @Override
  protected void writeAttributes(XMLWriter writer, Home home) throws IOException {
    writer.writeAttribute("version", String.valueOf(home.getVersion()));
    writer.writeAttribute("name", home.getName(), null);
    writer.writeAttribute("camera", home.getCamera() == home.getObserverCamera() ? "observerCamera" : "topCamera");
    writer.writeAttribute("selectedLevel", getId(home.getSelectedLevel()), null);
    writer.writeFloatAttribute("wallHeight", home.getWallHeight());
    writer.writeBooleanAttribute("basePlanLocked", home.isBasePlanLocked(), false);
    if (home.getFurnitureSortedProperty() != null) {
      writer.writeAttribute("furnitureSortedProperty", home.getFurnitureSortedProperty().name());
    }
    writer.writeBooleanAttribute("furnitureDescendingSorted", home.isFurnitureDescendingSorted(), false);
  }
  
  /**
   * Writes as XML elements some objects that depends on of <code>home</code> with the given <code>writer</code>.
   */
  @Override
  protected void writeChildren(XMLWriter writer, Home home) throws IOException {
    // Write properties in the alphabetic order of their names
    List<String> propertiesNames = new ArrayList<String>(home.getPropertyNames());
    Collections.sort(propertiesNames);
    for (String propertyName : propertiesNames) {
      writeProperty(writer, propertyName, home.getProperty(propertyName));
    }
    // Write furniture visible properties
    for (HomePieceOfFurniture.SortableProperty property : home.getFurnitureVisibleProperties()) {
      writer.writeStartElement("furnitureVisibleProperty");
      writer.writeAttribute("name", property.name());
      writer.writeEndElement();
    }
    // Write environment, compass and cameras
    writeEnvironment(writer, home.getEnvironment());
    writeBackgroundImage(writer, home.getBackgroundImage());
    writePrint(writer, home.getPrint());
    writeCompass(writer, home.getCompass());
    writeCamera(writer, home.getObserverCamera(), "observerCamera");
    writeCamera(writer, home.getTopCamera(), "topCamera");
    for (Camera camera : home.getStoredCameras()) {
      writeCamera(writer, camera, "storedCamera");
    }
    // Write Level elements
    for (Level level : home.getLevels()) {
      writeLevel(writer, level);
    }
    // Write furniture and other home elements
    for (HomePieceOfFurniture piece : home.getFurniture()) {
      writePieceOfFurniture(writer, piece);
    }
    for (Wall wall : home.getWalls()) {
      writeWall(writer, wall);
    }
    for (Room room : home.getRooms()) {
      writeRoom(writer, room);
    }
    for (Polyline polyline : home.getPolylines()) {
      writePolyline(writer, polyline);
    }
    for (DimensionLine dimensionLine : home.getDimensionLines()) {
      writeDimensionLine(writer, dimensionLine);
    }
    for (Label label : home.getLabels()) {
      writeLabel(writer, label);
    }
  }

  /**
   * Writes in XML the <code>environment</code> object with the given <code>writer</code>.
   */
  protected void writeEnvironment(XMLWriter writer, HomeEnvironment environment) throws IOException {
    new ObjectXMLExporter<HomeEnvironment>() {
        @Override
        protected void writeAttributes(XMLWriter writer, HomeEnvironment environment) throws IOException {
          writer.writeColorAttribute("groundColor", environment.getGroundColor());
          writer.writeColorAttribute("skyColor", environment.getSkyColor());
          writer.writeColorAttribute("lightColor", environment.getLightColor());
          writer.writeFloatAttribute("wallsAlpha", environment.getWallsAlpha(), 0);
          writer.writeBooleanAttribute("allLevelsVisible", environment.isAllLevelsVisible(), false);
          writer.writeBooleanAttribute("observerCameraElevationAdjusted", environment.isObserverCameraElevationAdjusted(), true);
          writer.writeColorAttribute("ceillingLightColor", environment.getCeillingLightColor());
          writer.writeAttribute("drawingMode", environment.getDrawingMode().name(), HomeEnvironment.DrawingMode.FILL.name());
          writer.writeFloatAttribute("subpartSizeUnderLight", environment.getSubpartSizeUnderLight(), 0);
          writer.writeIntegerAttribute("photoWidth", environment.getPhotoWidth());
          writer.writeIntegerAttribute("photoHeight", environment.getPhotoHeight());
          writer.writeAttribute("photoAspectRatio", environment.getPhotoAspectRatio().name());
          writer.writeIntegerAttribute("photoQuality", environment.getPhotoQuality());
          writer.writeIntegerAttribute("videoWidth", environment.getVideoWidth());
          writer.writeAttribute("videoAspectRatio", environment.getVideoAspectRatio().name());
          writer.writeIntegerAttribute("videoQuality", environment.getVideoQuality());
          writer.writeIntegerAttribute("videoFrameRate", environment.getVideoFrameRate());
        }
        
        @Override
        protected void writeChildren(XMLWriter writer, HomeEnvironment environment) throws IOException {
          if (!environment.getVideoCameraPath().isEmpty()) {
            for (Camera camera : environment.getVideoCameraPath()) {
              writeCamera(writer, camera, "cameraPath");
            }
          }
          writeTexture(writer, environment.getGroundTexture(), "groundTexture");
          writeTexture(writer, environment.getSkyTexture(), "skyTexture");
        }
      }.writeElement(writer, environment);
  }

  /**
   * Writes in XML the <code>background</code> object with the given <code>writer</code>.
   */
  protected void writeBackgroundImage(XMLWriter writer, BackgroundImage backgroundImage) throws IOException {
    if (backgroundImage != null) {
      new ObjectXMLExporter<BackgroundImage>() {
          @Override
          protected void writeAttributes(XMLWriter writer, BackgroundImage backgroundImage) throws IOException {
            writer.writeAttribute("image", getExportedContentName(backgroundImage, backgroundImage.getImage()), null);
            writer.writeFloatAttribute("scaleDistance", backgroundImage.getScaleDistance());
            writer.writeFloatAttribute("scaleDistanceXStart", backgroundImage.getScaleDistanceXStart());
            writer.writeFloatAttribute("scaleDistanceYStart", backgroundImage.getScaleDistanceYStart());
            writer.writeFloatAttribute("scaleDistanceXEnd", backgroundImage.getScaleDistanceXEnd());
            writer.writeFloatAttribute("scaleDistanceYEnd", backgroundImage.getScaleDistanceYEnd());
            writer.writeFloatAttribute("xOrigin", backgroundImage.getXOrigin(), 0);
            writer.writeFloatAttribute("yOrigin", backgroundImage.getYOrigin(), 0);
            writer.writeBooleanAttribute("visible", backgroundImage.isVisible(), true);
          }
        }.writeElement(writer, backgroundImage);
    }
  }

  /**
   * Writes in XML the <code>print</code> object with the given <code>writer</code>.
   */
  protected void writePrint(XMLWriter writer, HomePrint print) throws IOException {
    if (print != null) {
      new ObjectXMLExporter<HomePrint>() {
          @Override
          protected void writeAttributes(XMLWriter writer, HomePrint print) throws IOException {
            writer.writeAttribute("headerFormat", print.getHeaderFormat(), null);
            writer.writeAttribute("footerFormat", print.getFooterFormat(), null);
            writer.writeBooleanAttribute("furniturePrinted", print.isFurniturePrinted(), true);
            writer.writeBooleanAttribute("planPrinted", print.isPlanPrinted(), true);
            writer.writeBooleanAttribute("view3DPrinted", print.isView3DPrinted(), true);
            writer.writeFloatAttribute("planScale", print.getPlanScale());
            writer.writeFloatAttribute("paperWidth", print.getPaperWidth());
            writer.writeFloatAttribute("paperHeight", print.getPaperHeight());
            writer.writeFloatAttribute("paperTopMargin", print.getPaperTopMargin());
            writer.writeFloatAttribute("paperLeftMargin", print.getPaperLeftMargin());
            writer.writeFloatAttribute("paperBottomMargin", print.getPaperBottomMargin());
            writer.writeFloatAttribute("paperRightMargin", print.getPaperRightMargin());
            writer.writeAttribute("paperOrientation", print.getPaperOrientation().name());
          }
        }.writeElement(writer, print);
    }
  }

  /**
   * Writes in XML the <code>compass</code> object with the given <code>writer</code>.
   */
  protected void writeCompass(XMLWriter writer, Compass compass) throws IOException {
    new ObjectXMLExporter<Compass>() {
        @Override
        protected void writeAttributes(XMLWriter writer, Compass compass) throws IOException {
          writer.writeFloatAttribute("x", compass.getX());
          writer.writeFloatAttribute("y", compass.getY());
          writer.writeFloatAttribute("diameter", compass.getDiameter());
          writer.writeFloatAttribute("northDirection", compass.getNorthDirection());
          writer.writeFloatAttribute("longitude", compass.getLongitude());
          writer.writeFloatAttribute("latitude", compass.getLatitude());
          writer.writeAttribute("timeZone", compass.getTimeZone());
          writer.writeBooleanAttribute("visible", compass.isVisible(), true);
        }
        
        @Override
        protected void writeChildren(XMLWriter writer, Compass compass) throws IOException {
          writeProperties(writer, compass);
        }
      }.writeElement(writer, compass);
  }

  /**
   * Writes in XML the <code>camera</code> object with the given <code>writer</code>.
   */
  protected void writeCamera(XMLWriter writer, Camera camera, final String attributeName) throws IOException {
    if (camera != null) {
      new ObjectXMLExporter<Camera>() {
          @Override
          protected void writeAttributes(XMLWriter writer, Camera camera) throws IOException {
            writer.writeAttribute("attribute", attributeName, null);
            writer.writeAttribute("name", camera.getName(), null);
            writer.writeAttribute("lens", camera.getLens().name());
            writer.writeFloatAttribute("x", camera.getX());
            writer.writeFloatAttribute("y", camera.getY());
            writer.writeFloatAttribute("z", camera.getZ());
            writer.writeFloatAttribute("yaw", camera.getYaw());
            writer.writeFloatAttribute("pitch", camera.getPitch());
            writer.writeFloatAttribute("fieldOfView", camera.getFieldOfView());
            writer.writeLongAttribute("time", camera.getTime());
            if (camera instanceof ObserverCamera) {
              writer.writeBooleanAttribute("fixedSize", ((ObserverCamera)camera).isFixedSize(), false);
            }
          }
          
          @Override
          protected void writeChildren(XMLWriter writer, Camera camera) throws IOException {
            writeProperties(writer, camera);
          }
        }.writeElement(writer, camera);
    }
  }

  /**
   * Writes in XML the <code>level</code> object with the given <code>writer</code>.
   */
  protected void writeLevel(XMLWriter writer, Level level) throws IOException {
    new ObjectXMLExporter<Level>() {
        @Override
        protected void writeAttributes(XMLWriter writer, Level level) throws IOException {
          writer.writeAttribute("id", getId(level));
          writer.writeAttribute("name", level.getName());
          writer.writeFloatAttribute("elevation", level.getElevation());
          writer.writeFloatAttribute("floorThickness", level.getFloorThickness());
          writer.writeFloatAttribute("height", level.getHeight());
          writer.writeIntegerAttribute("elevationIndex", level.getElevationIndex());
          writer.writeBooleanAttribute("visible", level.isVisible(), true);
          writer.writeBooleanAttribute("viewable", level.isViewable(), true);
        }
        
        @Override
        protected void writeChildren(XMLWriter writer, Level level) throws IOException {
          writeProperties(writer, level);
          writeBackgroundImage(writer, level.getBackgroundImage());
        }
      }.writeElement(writer, level);
  }

  /**
   * Writes in XML the <code>piece</code> object with the given <code>writer</code>.
   */
  protected void writePieceOfFurniture(XMLWriter writer, HomePieceOfFurniture piece) throws IOException {
    new PieceOfFurnitureExporter().writeElement(writer, piece);
  }
  
  /**
   * Default exporter class used to write a piece of furniture in XML.
   */
  protected class PieceOfFurnitureExporter extends ObjectXMLExporter<HomePieceOfFurniture> { 
    public PieceOfFurnitureExporter() {
    }
    
    @Override
    protected void writeAttributes(XMLWriter writer, HomePieceOfFurniture piece) throws IOException {
      if (piece.getLevel() != null) {
        writer.writeAttribute("level", getId(piece.getLevel()));        
      }
      writer.writeAttribute("catalogId", piece.getCatalogId(), null);
      writer.writeAttribute("name", piece.getName());        
      writer.writeAttribute("creator", piece.getCreator(), null);
      writer.writeAttribute("model", getExportedContentName(piece, piece.getModel()), null);
      writer.writeAttribute("icon", getExportedContentName(piece, piece.getIcon()), null);
      writer.writeAttribute("planIcon", getExportedContentName(piece, piece.getPlanIcon()), null);
      writer.writeFloatAttribute("x", piece.getX());
      writer.writeFloatAttribute("y", piece.getY());
      writer.writeFloatAttribute("elevation", piece.getElevation(), 0f);
      writer.writeFloatAttribute("angle", piece.getAngle(), 0f);
      writer.writeFloatAttribute("pitch", piece.getPitch(), 0f);
      writer.writeFloatAttribute("roll", piece.getRoll(), 0f);
      writer.writeFloatAttribute("width", piece.getWidth());
      writer.writeFloatAttribute("widthInPlan", piece.getWidthInPlan(), piece.getWidth());
      writer.writeFloatAttribute("depth", piece.getDepth());
      writer.writeFloatAttribute("depthInPlan", piece.getDepthInPlan(), piece.getDepth());
      writer.writeFloatAttribute("height", piece.getHeight());
      writer.writeFloatAttribute("heightInPlan", piece.getHeightInPlan(), piece.getHeight());
      writer.writeBooleanAttribute("backFaceShown", piece.isBackFaceShown(), false);
      writer.writeBooleanAttribute("modelMirrored", piece.isModelMirrored(), false);
      writer.writeBooleanAttribute("visible", piece.isVisible(), true);
      writer.writeColorAttribute("color", piece.getColor());
      if (piece.getShininess() != null) {
        writer.writeFloatAttribute("shininess", piece.getShininess());
      }
      float [][] modelRotation = piece.getModelRotation();
      String modelRotationString = 
          floatToString(modelRotation[0][0]) + " " + floatToString(modelRotation[0][1]) + " " + floatToString(modelRotation[0][2]) + " "
        + floatToString(modelRotation[1][0]) + " " + floatToString(modelRotation[1][1]) + " " + floatToString(modelRotation[1][2]) + " "
        + floatToString(modelRotation[2][0]) + " " + floatToString(modelRotation[2][1]) + " " + floatToString(modelRotation[2][2]);
      writer.writeAttribute("modelRotation", modelRotationString, "1 0 0 0 1 0 0 0 1");
      writer.writeBooleanAttribute("modelCenteredAtOrigin", piece.isModelCenteredAtOrigin(), true);
      writer.writeLongAttribute("modelSize", piece.getModelSize());
      writer.writeAttribute("description", piece.getDescription(), null);        
      writer.writeAttribute("information", piece.getInformation(), null);        
      writer.writeBooleanAttribute("movable", piece.isMovable(), true);
      if (!(piece instanceof HomeFurnitureGroup)) {
        if (!(piece instanceof HomeDoorOrWindow)) {
          writer.writeBooleanAttribute("doorOrWindow", piece.isDoorOrWindow(), false);
          writer.writeBooleanAttribute("horizontallyRotatable", piece.isHorizontallyRotatable(), true);
        }
        writer.writeBooleanAttribute("resizable", piece.isResizable(), true);
        writer.writeBooleanAttribute("deformable", piece.isDeformable(), true);
        writer.writeBooleanAttribute("texturable", piece.isTexturable(), true);
      }
      if (piece instanceof HomeFurnitureGroup) {
        BigDecimal price = piece.getPrice();
        // Ignore price of group if one of its children has a price
        for (HomePieceOfFurniture groupPiece : ((HomeFurnitureGroup)piece).getFurniture()) {
          if (groupPiece.getPrice() != null) {
            price = null;
            break;
          }
        }
        writer.writeBigDecimalAttribute("price", price);
      } else {
        writer.writeBigDecimalAttribute("price", piece.getPrice());
        writer.writeBigDecimalAttribute("valueAddedTaxPercentage", piece.getValueAddedTaxPercentage());
        writer.writeAttribute("currency", piece.getCurrency(), null);
      }
      writer.writeAttribute("staircaseCutOutShape", piece.getStaircaseCutOutShape(), null);
      writer.writeFloatAttribute("dropOnTopElevation", piece.getDropOnTopElevation(), 1f);
      writer.writeBooleanAttribute("nameVisible", piece.isNameVisible(), false);
      writer.writeFloatAttribute("nameAngle", piece.getNameAngle(), 0f);
      writer.writeFloatAttribute("nameXOffset", piece.getNameXOffset(), 0f);
      writer.writeFloatAttribute("nameYOffset", piece.getNameYOffset(), 0f);
      if (piece instanceof HomeDoorOrWindow) {
        HomeDoorOrWindow doorOrWindow = (HomeDoorOrWindow)piece;
        writer.writeFloatAttribute("wallThickness", doorOrWindow.getWallThickness(), 1f);
        writer.writeFloatAttribute("wallDistance", doorOrWindow.getWallDistance(), 0f);
        writer.writeAttribute("cutOutShape", doorOrWindow.getCutOutShape(), null);
        writer.writeBooleanAttribute("wallCutOutOnBothSides", doorOrWindow.isWallCutOutOnBothSides(), false);
        writer.writeBooleanAttribute("widthDepthDeformable", doorOrWindow.isWidthDepthDeformable(), true);
        writer.writeBooleanAttribute("boundToWall", doorOrWindow.isBoundToWall(), true);
      } else if (piece instanceof HomeLight) {
        writer.writeFloatAttribute("power", ((HomeLight)piece).getPower());
      } 
    }
    
    @Override
    protected void writeChildren(XMLWriter writer, HomePieceOfFurniture piece) throws IOException {
      // Write subclass child elements 
      if (piece instanceof HomeFurnitureGroup) {
        for (HomePieceOfFurniture groupPiece : ((HomeFurnitureGroup)piece).getFurniture()) {
          writePieceOfFurniture(writer, groupPiece);
        }
      } else if (piece instanceof HomeLight) {
        for (LightSource lightSource : ((HomeLight)piece).getLightSources()) {
          writer.writeStartElement("lightSource");
          writer.writeFloatAttribute("x", lightSource.getX());
          writer.writeFloatAttribute("y", lightSource.getY());
          writer.writeFloatAttribute("z", lightSource.getZ());
          writer.writeColorAttribute("color", lightSource.getColor());
          writer.writeFloatAttribute("diameter", lightSource.getDiameter());
          writer.writeEndElement();
        }
      } else if (piece instanceof HomeDoorOrWindow) {
        for (Sash sash : ((HomeDoorOrWindow)piece).getSashes()) {
          writer.writeStartElement("sash");
          writer.writeFloatAttribute("xAxis", sash.getXAxis());
          writer.writeFloatAttribute("yAxis", sash.getYAxis());
          writer.writeFloatAttribute("width", sash.getWidth());
          writer.writeFloatAttribute("startAngle", sash.getStartAngle());
          writer.writeFloatAttribute("endAngle", sash.getEndAngle());
          writer.writeEndElement();
        }
      }
      
      // Write child elements 
      writeProperties(writer, piece);
      writeTextStyle(writer, piece.getNameStyle(), "nameStyle");
      writeTexture(writer, piece.getTexture(), null);
      if (piece.getModelMaterials() != null) {
        for (HomeMaterial material : piece.getModelMaterials()) {
          writeMaterial(writer, material, piece.getModel());
        }
      }
    }
  }

  /**
   * Writes in XML the <code>material</code> object with the given <code>writer</code>.
   */
  protected void writeMaterial(XMLWriter writer, HomeMaterial material, final Content model) throws IOException {
    if (material != null) {
      new ObjectXMLExporter<HomeMaterial>() {
          @Override
          protected void writeAttributes(XMLWriter writer, HomeMaterial material) throws IOException {
            writer.writeAttribute("name", material.getName());
            writer.writeAttribute("key", material.getKey(), null);
            writer.writeColorAttribute("color", material.getColor());
            if (material.getShininess() != null) {
              writer.writeFloatAttribute("shininess", material.getShininess());
            }
          }
          
          @Override
          protected void writeChildren(XMLWriter writer, HomeMaterial material) throws IOException {
            writeTexture(writer, material.getTexture(), null);
          }
        }.writeElement(writer, material);
    }
  }
  
  /**
   * Writes in XML the <code>wall</code> object with the given <code>writer</code>.
   */
  protected void writeWall(XMLWriter writer, Wall wall) throws IOException {
    new ObjectXMLExporter<Wall>() {
        @Override
        protected void writeAttributes(XMLWriter writer, Wall wall) throws IOException {
          writer.writeAttribute("id", getId(wall));
          if (wall.getLevel() != null) {
            writer.writeAttribute("level", getId(wall.getLevel()));        
          }
          if (wall.getWallAtStart() != null) {
            String id = getId(wall.getWallAtStart());
            // Check id isn't null to ensure saved data consistency
            if (id != null) {
              writer.writeAttribute("wallAtStart", id);
            }
          }
          if (wall.getWallAtEnd() != null) {
            String id = getId(wall.getWallAtEnd());
            // Check id isn't null to ensure saved data consistency
            if (id != null) {
              writer.writeAttribute("wallAtEnd", id);
            }
          }
          writer.writeFloatAttribute("xStart", wall.getXStart());
          writer.writeFloatAttribute("yStart", wall.getYStart());
          writer.writeFloatAttribute("xEnd", wall.getXEnd());
          writer.writeFloatAttribute("yEnd", wall.getYEnd());
          writer.writeFloatAttribute("height", wall.getHeight());
          writer.writeFloatAttribute("heightAtEnd", wall.getHeightAtEnd());
          writer.writeFloatAttribute("thickness", wall.getThickness());
          writer.writeFloatAttribute("arcExtent", wall.getArcExtent());
          if (wall.getPattern() != null) {
            writer.writeAttribute("pattern", wall.getPattern().getName());
          }
          writer.writeColorAttribute("topColor", wall.getTopColor());
          writer.writeColorAttribute("leftSideColor", wall.getLeftSideColor());
          writer.writeFloatAttribute("leftSideShininess", wall.getLeftSideShininess(), 0);
          writer.writeColorAttribute("rightSideColor", wall.getRightSideColor());
          writer.writeFloatAttribute("rightSideShininess", wall.getRightSideShininess(), 0);
        }
        
        @Override
        protected void writeChildren(XMLWriter writer, Wall wall) throws IOException {
          writeProperties(writer, wall);
          writeTexture(writer, wall.getLeftSideTexture(), "leftSideTexture");
          writeTexture(writer, wall.getRightSideTexture(), "rightSideTexture");
          writeBaseboard(writer, wall.getLeftSideBaseboard(), "leftSideBaseboard");
          writeBaseboard(writer, wall.getRightSideBaseboard(), "rightSideBaseboard");
        }
      }.writeElement(writer, wall);
  }
  
  /**
   * Writes in XML the <code>room</code> object with the given <code>writer</code>.
   */
  protected void writeRoom(XMLWriter writer, Room room) throws IOException {
    new ObjectXMLExporter<Room>() {
        @Override
        protected void writeAttributes(XMLWriter writer, Room room) throws IOException {
          if (room.getLevel() != null) {
            writer.writeAttribute("level", getId(room.getLevel()));        
          }
          writer.writeAttribute("name", room.getName(), null);
          writer.writeFloatAttribute("nameAngle", room.getNameAngle(), 0f);
          writer.writeFloatAttribute("nameXOffset", room.getNameXOffset(), 0f);
          writer.writeFloatAttribute("nameYOffset", room.getNameYOffset(), -40f);
          writer.writeBooleanAttribute("areaVisible", room.isAreaVisible(), false);
          writer.writeFloatAttribute("areaAngle", room.getAreaAngle(), 0f);
          writer.writeFloatAttribute("areaXOffset", room.getAreaXOffset(), 0f);
          writer.writeFloatAttribute("areaYOffset", room.getAreaYOffset(), 0f);
          writer.writeBooleanAttribute("floorVisible", room.isFloorVisible(), true);
          writer.writeColorAttribute("floorColor", room.getFloorColor());
          writer.writeFloatAttribute("floorShininess", room.getFloorShininess(), 0);
          writer.writeBooleanAttribute("ceilingVisible", room.isCeilingVisible(), true);
          writer.writeColorAttribute("ceilingColor", room.getCeilingColor());
          writer.writeFloatAttribute("ceilingShininess", room.getCeilingShininess(), 0);
        }
        
        @Override
        protected void writeChildren(XMLWriter writer, Room room) throws IOException {
          writeProperties(writer, room);
          writeTextStyle(writer, room.getNameStyle(), "nameStyle");
          writeTextStyle(writer, room.getAreaStyle(), "areaStyle");
          writeTexture(writer, room.getFloorTexture(), "floorTexture");
          writeTexture(writer, room.getCeilingTexture(), "ceilingTexture");
          for (float [] point : room.getPoints()) {
            writer.writeStartElement("point");
            writer.writeFloatAttribute("x", point [0]);
            writer.writeFloatAttribute("y", point [1]);
            writer.writeEndElement();
          }
        }
      }.writeElement(writer, room);
  }
  
  /**
   * Writes in XML the <code>polyline</code> object with the given <code>writer</code>.
   */
  protected void writePolyline(XMLWriter writer, Polyline polyline) throws IOException {
    new ObjectXMLExporter<Polyline>() {
        @Override
        protected void writeAttributes(XMLWriter writer, Polyline polyline) throws IOException {
          if (polyline.getLevel() != null) {
            writer.writeAttribute("level", getId(polyline.getLevel()));        
          }
          writer.writeFloatAttribute("thickness", polyline.getThickness(), 1f);
          writer.writeAttribute("capStyle", polyline.getCapStyle().name());
          writer.writeAttribute("joinStyle", polyline.getJoinStyle().name());
          writer.writeAttribute("dashStyle", polyline.getDashStyle().name());
          writer.writeAttribute("startArrowStyle", polyline.getStartArrowStyle().name());
          writer.writeAttribute("endArrowStyle", polyline.getEndArrowStyle().name());
          writer.writeColorAttribute("color", polyline.getColor());
          writer.writeBooleanAttribute("closedPath", polyline.isClosedPath(), false);
        }
        
        @Override
        protected void writeChildren(XMLWriter writer, Polyline polyline) throws IOException {
          writeProperties(writer, polyline);
          for (float [] point : polyline.getPoints()) {
            writer.writeStartElement("point");
            writer.writeFloatAttribute("x", point [0]);
            writer.writeFloatAttribute("y", point [1]);
            writer.writeEndElement();
          }
        }
      }.writeElement(writer, polyline);
  }
  
  /**
   * Writes in XML the <code>dimensionLine</code> object with the given <code>writer</code>.
   */
  protected void writeDimensionLine(XMLWriter writer, DimensionLine dimensionLine) throws IOException {
    new ObjectXMLExporter<DimensionLine>() {
        @Override
        protected void writeAttributes(XMLWriter writer, DimensionLine dimensionLine) throws IOException {
          if (dimensionLine.getLevel() != null) {
            writer.writeAttribute("level", getId(dimensionLine.getLevel()));        
          }
          writer.writeFloatAttribute("xStart", dimensionLine.getXStart());
          writer.writeFloatAttribute("yStart", dimensionLine.getYStart());
          writer.writeFloatAttribute("xEnd", dimensionLine.getXEnd());
          writer.writeFloatAttribute("yEnd", dimensionLine.getYEnd());
          writer.writeFloatAttribute("offset", dimensionLine.getOffset());
        }
        
        @Override
        protected void writeChildren(XMLWriter writer, DimensionLine dimensionLine) throws IOException {
          writeProperties(writer, dimensionLine);
          writeTextStyle(writer, dimensionLine.getLengthStyle(), "lengthStyle");
        }
      }.writeElement(writer, dimensionLine);
  }

  /**
   * Writes in XML the <code>label</code> object with the given <code>writer</code>.
   */
  protected void writeLabel(XMLWriter writer, Label label) throws IOException {
    new ObjectXMLExporter<Label>() {
        @Override
        protected void writeAttributes(XMLWriter writer, Label label) throws IOException {
          if (label.getLevel() != null) {
            writer.writeAttribute("level", getId(label.getLevel()));        
          }
          writer.writeFloatAttribute("x", label.getX());
          writer.writeFloatAttribute("y", label.getY());
          writer.writeFloatAttribute("angle", label.getAngle(), 0);
          writer.writeFloatAttribute("elevation", label.getElevation(), 0);
          writer.writeFloatAttribute("pitch", label.getPitch());
          writer.writeColorAttribute("color", label.getColor());
          writer.writeColorAttribute("outlineColor", label.getOutlineColor());
        }
        
        @Override
        protected void writeChildren(XMLWriter writer, Label label) throws IOException {
          writeProperties(writer, label);
          writeTextStyle(writer, label.getStyle(), null);
          // Write text in a child element
          writer.writeStartElement("text");
          writer.writeText(label.getText());
          writer.writeEndElement();
        }
      }.writeElement(writer, label);
  }
  
  /**
   * Writes in XML the <code>textStyle</code> object with the given <code>writer</code>.
   */
  protected void writeTextStyle(XMLWriter writer, TextStyle textStyle, 
                                 final String attributeName) throws IOException {
    if (textStyle != null) {
      new ObjectXMLExporter<TextStyle>() {
          @Override
          protected void writeAttributes(XMLWriter writer, TextStyle textStyle) throws IOException {
            writer.writeAttribute("attribute", attributeName, null);
            writer.writeAttribute("fontName", textStyle.getFontName(), null);
            writer.writeFloatAttribute("fontSize", textStyle.getFontSize());
            writer.writeBooleanAttribute("bold", textStyle.isBold(), false);
            writer.writeBooleanAttribute("italic", textStyle.isItalic(), false);
          }
        }.writeElement(writer, textStyle);
    }
  }

  /**
   * Writes in XML the <code>baseboard</code> object with the given <code>writer</code>.
   */
  protected void writeBaseboard(XMLWriter writer, Baseboard baseboard, 
                                 final String attributeName) throws IOException {
    if (baseboard != null) {
      new ObjectXMLExporter<Baseboard>() {
          @Override
          protected void writeAttributes(XMLWriter writer, Baseboard baseboard) throws IOException {
            writer.writeAttribute("attribute", attributeName, null);
            writer.writeFloatAttribute("thickness", baseboard.getThickness());
            writer.writeFloatAttribute("height", baseboard.getHeight());
            writer.writeColorAttribute("color", baseboard.getColor());
          }
          
          @Override
          protected void writeChildren(XMLWriter writer, Baseboard baseboard) throws IOException {
            writeTexture(writer, baseboard.getTexture(), null);
          }
        }.writeElement(writer, baseboard);
    }
  }
  
  /**
   * Writes in XML the <code>texture</code> object with the given <code>writer</code>.
   */
  protected void writeTexture(XMLWriter writer, HomeTexture texture, 
                               final String attributeName) throws IOException {
    if (texture != null) {
      new ObjectXMLExporter<HomeTexture>() {
          @Override
          protected void writeAttributes(XMLWriter writer, HomeTexture texture) throws IOException {
            writer.writeAttribute("attribute", attributeName, null);
            writer.writeAttribute("name", texture.getName(), null);
            writer.writeAttribute("creator", texture.getCreator(), null);
            writer.writeAttribute("catalogId", texture.getCatalogId(), null);
            writer.writeFloatAttribute("width", texture.getWidth());
            writer.writeFloatAttribute("height", texture.getHeight());
            writer.writeFloatAttribute("angle", texture.getAngle(), 0f);
            writer.writeFloatAttribute("scale", texture.getScale(), 1f);
            writer.writeBooleanAttribute("leftToRightOriented", texture.isLeftToRightOriented(), true); 
            writer.writeAttribute("image", getExportedContentName(texture, texture.getImage()), null);
          }
        }.writeElement(writer, texture);
    }
  }
  
  /**
   * Writes in XML the properties of the <code>HomeObject</code> instance with the given <code>writer</code>.
   */
  private void writeProperties(XMLWriter writer, HomeObject object) throws IOException {
    List<String> propertiesNames = new ArrayList<String>(object.getPropertyNames());
    Collections.sort(propertiesNames);
    for (String propertyName : propertiesNames) {
      writeProperty(writer, propertyName, object.getProperty(propertyName));
    }
  }

  /**
   * Writes in XML the given property.
   */
  private void writeProperty(XMLWriter writer, String propertyName, String propertyValue) throws IOException {
    if (propertyValue != null) {
      writer.writeStartElement("property");
      writer.writeAttribute("name", propertyName);
      writer.writeAttribute("value", propertyValue);
      writer.writeEndElement();
    }
  }

  /**
   * Returns the string value of the given float, except for -1.0, 1.0 or 0.0
   * where -1, 1 and 0 is returned.
   */
  private static String floatToString(float f) {
    if (Math.abs(f) < 1E-6) {
      return "0";
    } else if (Math.abs(f - 1f) < 1E-6) {
      return "1";
    } else if (Math.abs(f + 1f) < 1E-6) {
      return "-1";
    } else {
      return String.valueOf(f);
    }
  }

  /**
   * Returns the saved name of the given <code>content</code> owned by an object.
   */
  protected String getExportedContentName(Object owner, Content content) {
    if (content == null || this.savedContentNames == null) {
      return null;
    } else {
      String contentName = this.savedContentNames.get(content);
      if (contentName != null) {
        return contentName;
      } else if (content instanceof URLContent) {
        return ((URLContent)content).getURL().toString();
      } else {
        return content.toString();
      }
    }
  }
}
