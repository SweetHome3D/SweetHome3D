/*
 * HomePieceOfFurniture3D.java 23 jan. 09
 *
 * Copyright (c) 2007-2009 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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
package com.eteks.sweethome3d.j3d;

import java.awt.Color;
import java.util.Enumeration;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TexCoordGeneration;
import javax.media.j3d.Texture;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Color3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeEnvironment;
import com.eteks.sweethome3d.model.HomeFurnitureGroup;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.HomeTexture;
import com.sun.j3d.utils.geometry.Box;

/**
 * Root of piece of furniture branch.
 */
public class HomePieceOfFurniture3D extends Object3DBranch {
  private static final Material               DEFAULT_TEXTURED_SHAPE_MATERIAL = new Material();
  private static final TransparencyAttributes DEFAULT_TEXTURED_SHAPE_TRANSPARENCY_ATTRIBUTES = 
      new TransparencyAttributes(TransparencyAttributes.NICEST, 0);
  private static final TextureAttributes      MODULATE_TEXTURE_ATTRIBUTES = new TextureAttributes();
  
  private final Home home;
  
  static {
    DEFAULT_TEXTURED_SHAPE_MATERIAL.setShininess(0);
    DEFAULT_TEXTURED_SHAPE_MATERIAL.setSpecularColor(0, 0, 0);
    MODULATE_TEXTURE_ATTRIBUTES.setTextureMode(TextureAttributes.MODULATE);
  }
  
  /**
   * Creates the 3D piece matching the given home <code>piece</code>.
   */
  public HomePieceOfFurniture3D(HomePieceOfFurniture piece, Home home) {
    this(piece, home, false, false);
  }

  /**
   * Creates the 3D piece matching the given home <code>piece</code>.
   */
  public HomePieceOfFurniture3D(HomePieceOfFurniture piece, 
                                Home home, 
                                boolean ignoreDrawingMode, 
                                boolean waitModelAndTextureLoadingEnd) {
    setUserData(piece);      
    this.home = home;

    // Allow piece branch to be removed from its parent
    setCapability(BranchGroup.ALLOW_DETACH);
    // Allow to read branch transform child
    setCapability(BranchGroup.ALLOW_CHILDREN_READ);
    
    if (piece instanceof HomeFurnitureGroup) {
      for (HomePieceOfFurniture groupPiece : ((HomeFurnitureGroup)piece).getFurniture()) {
        addChild(new HomePieceOfFurniture3D(groupPiece, home, ignoreDrawingMode, waitModelAndTextureLoadingEnd));
      }
    } else {
      createPieceOfFurnitureNode(piece, ignoreDrawingMode, waitModelAndTextureLoadingEnd);
    }
  }

  /**
   * Creates the piece node with its transform group and add it to the piece branch. 
   */
  private void createPieceOfFurnitureNode(final HomePieceOfFurniture piece, 
                                          final boolean ignoreDrawingMode, 
                                          final boolean waitModelAndTextureLoadingEnd) {
    final TransformGroup pieceTransformGroup = new TransformGroup();
    // Allow the change of the transformation that sets piece size and position
    pieceTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
    pieceTransformGroup.setCapability(Group.ALLOW_CHILDREN_READ);
    pieceTransformGroup.setCapability(Group.ALLOW_CHILDREN_WRITE);
    pieceTransformGroup.setCapability(Group.ALLOW_CHILDREN_EXTEND);
    addChild(pieceTransformGroup);
    
    // While loading model use a temporary node that displays a white box  
    final BranchGroup waitBranch = new BranchGroup();
    waitBranch.setCapability(BranchGroup.ALLOW_DETACH);
    waitBranch.addChild(getModelBox(Color.WHITE));      
    // Allow appearance change on all children
    setAppearanceChangeCapability(waitBranch);
    
    pieceTransformGroup.addChild(waitBranch);
    
    // Set piece model initial location, orientation and size      
    updatePieceOfFurnitureTransform();
    
    // Load piece real 3D model
    Content model = piece.getModel();
    ModelManager.getInstance().loadModel(model, waitModelAndTextureLoadingEnd,
        new ModelManager.ModelObserver() {
          public void modelUpdated(BranchGroup modelRoot) {
            float [][] modelRotation = piece.getModelRotation();
            // Add piece model scene to a normalized transform group
            TransformGroup modelTransformGroup = 
                ModelManager.getInstance().getNormalizedTransformGroup(modelRoot, modelRotation, 1);
            modelTransformGroup.addChild(modelRoot);
            updatePieceOfFurnitureModelNode(modelTransformGroup, ignoreDrawingMode, waitModelAndTextureLoadingEnd);            
          }
          
          public void modelError(Exception ex) {
            // In case of problem use a default red box
            updatePieceOfFurnitureModelNode(getModelBox(Color.RED), ignoreDrawingMode, waitModelAndTextureLoadingEnd);            
          }
        });
  }

  @Override
  public void update() {
    HomePieceOfFurniture piece = (HomePieceOfFurniture)getUserData();
    if (piece instanceof HomeFurnitureGroup) {
      Enumeration<?> enumeration = getAllChildren(); 
      while (enumeration.hasMoreElements()) {
        ((HomePieceOfFurniture3D)enumeration.nextElement()).update();
      }
    } else {
      updatePieceOfFurnitureTransform();
      updatePieceOfFurnitureColorAndTexture(false);      
      updatePieceOfFurnitureVisibility();      
      updatePieceOfFurnitureModelMirrored();
    }
  }

  /**
   * Sets the transformation applied to piece model to match
   * its location, its angle and its size.
   */
  private void updatePieceOfFurnitureTransform() {
    HomePieceOfFurniture piece = (HomePieceOfFurniture)getUserData();
    // Set piece size
    Transform3D scale = new Transform3D();
    float pieceWidth = piece.getWidth();
    // If piece model is mirrored, inverse its width
    if (piece.isModelMirrored()) {
      pieceWidth *= -1;
    }
    scale.setScale(new Vector3d(pieceWidth, piece.getHeight(), piece.getDepth()));
    // Change its angle around y axis
    Transform3D orientation = new Transform3D();
    orientation.rotY(-piece.getAngle());
    orientation.mul(scale);
    // Translate it to its location
    Transform3D pieceTransform = new Transform3D();
    pieceTransform.setTranslation(new Vector3f(
        piece.getX(), piece.getElevation() + piece.getHeight() / 2, piece.getY()));      
    pieceTransform.mul(orientation);
    
    // Change model transformation      
    ((TransformGroup)getChild(0)).setTransform(pieceTransform);
  }
  
  /**
   * Sets the color and the texture applied to piece model.
   */
  private void updatePieceOfFurnitureColorAndTexture(boolean waitTextureLoadingEnd) {
    HomePieceOfFurniture piece = (HomePieceOfFurniture)getUserData();
    Node filledModelNode = getFilledModelNode();
    if (piece.getColor() != null) {
      Integer color = piece.getColor();
      Color3f materialColor = new Color3f(((color >>> 16) & 0xFF) / 256f,
                                           ((color >>> 8) & 0xFF) / 256f,
                                                   (color & 0xFF) / 256f);
      Material material = new Material(materialColor, new Color3f(), materialColor, materialColor, 64);
      setMaterialAndTexture(filledModelNode, material, null, false, 
          piece.getWidth(), piece.getDepth(), piece.getHeight(), null);
    } else if (piece.getTexture() != null) {
      Vector3f modelSize = ModelManager.getInstance().getSize(filledModelNode);
      setMaterialAndTexture(filledModelNode, null, piece.getTexture(), waitTextureLoadingEnd, 
          piece.getWidth(), piece.getDepth(), piece.getHeight(), modelSize);
    } else {
      // Set default material and texture of model
      setMaterialAndTexture(filledModelNode, null, null, false, 
          piece.getWidth(), piece.getDepth(), piece.getHeight(), null);
    }
  }

  /**
   * Returns the node of the filled model.
   */
  private Node getFilledModelNode() {
    TransformGroup transformGroup = (TransformGroup)getChild(0);
    BranchGroup branchGroup = (BranchGroup)transformGroup.getChild(0);
    return branchGroup.getChild(0);
  }

  /**
   * Returns the node of the outline model.
   */
  private Node getOutlineModelNode() {
    TransformGroup transformGroup = (TransformGroup)getChild(0);
    BranchGroup branchGroup = (BranchGroup)transformGroup.getChild(0);
    if (branchGroup.numChildren() > 1) {
      return branchGroup.getChild(1);
    } else {
      return null;
    }
  }

  /**
   * Sets whether this piece model is visible or not.
   */
  private void updatePieceOfFurnitureVisibility() {
    HomePieceOfFurniture piece = (HomePieceOfFurniture)getUserData();
    HomeEnvironment.DrawingMode drawingMode = this.home.getEnvironment().getDrawingMode();
    // Update visibility of filled model shapes
    setVisible(getFilledModelNode(), piece.isVisible()
        && (drawingMode == null
            || drawingMode == HomeEnvironment.DrawingMode.FILL 
            || drawingMode == HomeEnvironment.DrawingMode.FILL_AND_OUTLINE));
    Node outlineModelNode = getOutlineModelNode();
    if (outlineModelNode != null) {
      // Update visibility of outline model shapes
      setVisible(outlineModelNode, piece.isVisible()
          && (drawingMode == HomeEnvironment.DrawingMode.OUTLINE
              || drawingMode == HomeEnvironment.DrawingMode.FILL_AND_OUTLINE));
    }
  }

  /**
   * Sets whether this piece model is mirrored or not.
   */
  private void updatePieceOfFurnitureModelMirrored() {
    HomePieceOfFurniture piece = (HomePieceOfFurniture)getUserData();
    // Cull front or back model faces whether its model is mirrored or not
    setCullFace(getFilledModelNode(), 
        piece.isModelMirrored() ^ piece.isBackFaceShown() 
            ? PolygonAttributes.CULL_FRONT 
            : PolygonAttributes.CULL_BACK);
    // Flip normals if back faces of model are shown
    if (piece.isBackFaceShown()) {
      setBackFaceNormalFlip(getFilledModelNode(), true);
    }
  }

  /**
   * Updates transform group children with <code>modelMode</code>.
   */
  private void updatePieceOfFurnitureModelNode(Node modelNode, boolean ignoreDrawingMode,
                                               boolean waitTextureLoadingEnd) {    
    BranchGroup modelBranch = new BranchGroup();
    // Add model node to branch group
    modelBranch.addChild(modelNode);
    if (!ignoreDrawingMode) {
      // Add outline model node 
      modelBranch.addChild(createOutlineModelNode(modelNode));
    }
    // Allow appearance change on all children
    setAppearanceChangeCapability(modelBranch);

    TransformGroup transformGroup = (TransformGroup)getChild(0);
    // Remove previous nodes    
    transformGroup.removeAllChildren();
    // Add model branch to live scene
    transformGroup.addChild(modelBranch);
    
    // Update piece color, visibility and model mirror in dispatch thread as
    // these attributes may be changed in that thread
    updatePieceOfFurnitureColorAndTexture(waitTextureLoadingEnd);      
    updatePieceOfFurnitureVisibility();
    updatePieceOfFurnitureModelMirrored();
  }

  /**
   * Returns a box that may replace model. 
   */
  private Node getModelBox(Color color) {
    Material material = new Material();
    material.setDiffuseColor(new Color3f(color));
    material.setAmbientColor(new Color3f(color.darker()));
    
    Appearance boxAppearance = new Appearance();
    boxAppearance.setMaterial(material);
    return new Box(0.5f, 0.5f, 0.5f, boxAppearance);
  }

  /**
   * Returns a clone of the given node with an outline appearance on its shapes.
   */
  private Node createOutlineModelNode(Node modelNode) {
    Node node = modelNode.cloneTree();
    setOutlineAppearance(node);
    return node;
  }
  
  /**
   * Sets the outline appearance on all the children of <code>node</code>.
   */
  private void setOutlineAppearance(Node node) {
    if (node instanceof Group) {
      Enumeration<?> enumeration = ((Group)node).getAllChildren(); 
      while (enumeration.hasMoreElements()) {
        setOutlineAppearance((Node)enumeration.nextElement());
      }
    } else if (node instanceof Shape3D) {        
      Appearance outlineAppearance = new Appearance();
      ((Shape3D)node).setAppearance(outlineAppearance);
      outlineAppearance.setCapability(Appearance.ALLOW_RENDERING_ATTRIBUTES_READ);
      RenderingAttributes renderingAttributes = new RenderingAttributes();
      renderingAttributes.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
      outlineAppearance.setRenderingAttributes(renderingAttributes);
      outlineAppearance.setColoringAttributes(Object3DBranch.OUTLINE_COLORING_ATTRIBUTES);
      outlineAppearance.setPolygonAttributes(Object3DBranch.OUTLINE_POLYGON_ATTRIBUTES);
      outlineAppearance.setLineAttributes(Object3DBranch.OUTLINE_LINE_ATTRIBUTES);
    }
  }

  /**
   * Sets the capability to change material and rendering attributes
   * for all children of <code>node</code>.
   */
  private void setAppearanceChangeCapability(Node node) {
    if (node instanceof Group) {
      node.setCapability(Group.ALLOW_CHILDREN_READ);
      Enumeration<?> enumeration = ((Group)node).getAllChildren(); 
      while (enumeration.hasMoreElements()) {
        setAppearanceChangeCapability((Node)enumeration.nextElement());
      }
    } else if (node instanceof Shape3D) {        
      Appearance appearance = ((Shape3D)node).getAppearance();
      if (appearance != null) {
        setAppearanceCapabilities(appearance);
      }
      node.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
      node.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
    }
  }

  /**
   * Sets the material and texture attribute of all <code>Shape3D</code> children nodes of <code>node</code> 
   * with a given <code>material</code>. 
   */
  private void setMaterialAndTexture(Node node, Material material, HomeTexture texture, boolean waitTextureLoadingEnd,
                                     float pieceWidth, float pieceDepth, float pieceHeight, Vector3f modelSize) {
    if (node instanceof Group) {
      // Set material and texture of all children
      Enumeration<?> enumeration = ((Group)node).getAllChildren(); 
      while (enumeration.hasMoreElements()) {
        setMaterialAndTexture((Node)enumeration.nextElement(), material, texture, waitTextureLoadingEnd,
            pieceWidth, pieceDepth, pieceHeight, modelSize);
      }
    } else if (node instanceof Shape3D) {
      final Shape3D shape = (Shape3D)node;
      String shapeName = (String)shape.getUserData();
      // Change material and texture of all shape that are not window panes 
      if (shapeName == null
          || !shapeName.startsWith(ModelManager.WINDOW_PANE_SHAPE_PREFIX)) {
        Appearance appearance = shape.getAppearance();
        if (appearance == null) {
          appearance = createAppearanceWithChangeCapabilities();
          ((Shape3D)node).setAppearance(appearance);
        }
        // Use appearance user data to store shape default material
        DefaultMaterialAndTexture defaultMaterialAndTexture = (DefaultMaterialAndTexture)appearance.getUserData();
        if (defaultMaterialAndTexture == null) {
          defaultMaterialAndTexture = new DefaultMaterialAndTexture(appearance.getMaterial(), 
              appearance.getTransparencyAttributes(), appearance.getTexCoordGeneration(), 
              appearance.getTexture(), appearance.getTextureAttributes());
          appearance.setUserData(defaultMaterialAndTexture);
        }
        if (material != null && defaultMaterialAndTexture.getTexture() == null) {
          // Change material if no default texture is displayed on the shape
          // (textures always keep the colors of their image file)
          appearance.setMaterial(material);
          appearance.setTexCoordGeneration(defaultMaterialAndTexture.getTexCoordGeneration());
          appearance.setTextureAttributes(defaultMaterialAndTexture.getTextureAttributes());
          appearance.setTexture(null);
        } else if (material == null && texture != null) {
          // Change material to white then texture
          appearance.setMaterial((Material)DEFAULT_TEXTURED_SHAPE_MATERIAL);
          TexCoordGeneration texCoordGeneration = new TexCoordGeneration(TexCoordGeneration.OBJECT_LINEAR,
              TexCoordGeneration.TEXTURE_COORDINATE_2,
              new Vector4f(-pieceWidth / modelSize.x / texture.getWidth(), 0, 0, 0), 
              new Vector4f(0, pieceHeight / modelSize.y / texture.getHeight(), pieceDepth / modelSize.z / texture.getHeight(), 0));
          appearance.setTexCoordGeneration(texCoordGeneration);
          appearance.setTextureAttributes(MODULATE_TEXTURE_ATTRIBUTES);
          TextureManager.getInstance().loadTexture(texture.getImage(), waitTextureLoadingEnd,
              new TextureManager.TextureObserver() {
                  public void textureUpdated(Texture texture) {
                    shape.getAppearance().setTexture(texture);
                    if (texture.getFormat() == Texture.RGBA) {
                      shape.getAppearance().setTransparencyAttributes(DEFAULT_TEXTURED_SHAPE_TRANSPARENCY_ATTRIBUTES);
                    }
                  }
                });
        } else {
          // Restore default material and texture
          appearance.setMaterial(defaultMaterialAndTexture.getMaterial());
          appearance.setTransparencyAttributes(defaultMaterialAndTexture.getTransparencyAttributes());
          appearance.setTexCoordGeneration(defaultMaterialAndTexture.getTexCoordGeneration());
          appearance.setTexture(defaultMaterialAndTexture.getTexture());
          appearance.setTextureAttributes(defaultMaterialAndTexture.getTextureAttributes());
        }
      }
    }
  }

  /**
   * Sets the visible attribute of all <code>Shape3D</code> children nodes of <code>node</code>. 
   */
  private void setVisible(Node node, boolean visible) {
    if (node instanceof Group) {
      // Set visibility of all children
      Enumeration<?> enumeration = ((Group)node).getAllChildren(); 
      while (enumeration.hasMoreElements()) {
        setVisible((Node)enumeration.nextElement(), visible);
      }
    } else if (node instanceof Shape3D) {
      Appearance appearance = ((Shape3D)node).getAppearance();
      if (appearance == null) {
        appearance = createAppearanceWithChangeCapabilities();
        ((Shape3D)node).setAppearance(appearance);
      }
      RenderingAttributes renderingAttributes = appearance.getRenderingAttributes();
      if (renderingAttributes == null) {
        renderingAttributes = new RenderingAttributes();
        renderingAttributes.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
        appearance.setRenderingAttributes(renderingAttributes);
      }
      
      // Change visibility
      renderingAttributes.setVisible(visible);
    }
  }

  /**
   * Sets the cull face of all <code>Shape3D</code> children nodes of <code>node</code>.
   * @param cullFace <code>PolygonAttributes.CULL_FRONT</code> or <code>PolygonAttributes.CULL_BACK</code>
   */
  private void setCullFace(Node node, int cullFace) {
    if (node instanceof Group) {
      // Set cull face of all children
      Enumeration<?> enumeration = ((Group)node).getAllChildren(); 
      while (enumeration.hasMoreElements()) {
        setCullFace((Node)enumeration.nextElement(), cullFace);
      }
    } else if (node instanceof Shape3D) {
      Appearance appearance = ((Shape3D)node).getAppearance();
      if (appearance == null) {
        appearance = createAppearanceWithChangeCapabilities();
        ((Shape3D)node).setAppearance(appearance);
      }
      PolygonAttributes polygonAttributes = appearance.getPolygonAttributes();
      if (polygonAttributes == null) {
        polygonAttributes = createPolygonAttributesWithChangeCapabilities();
        appearance.setPolygonAttributes(polygonAttributes);
      }
      
      // Change cull face 
      if (polygonAttributes.getCullFace() != PolygonAttributes.CULL_NONE) {
        polygonAttributes.setCullFace(cullFace);
      }
    }
  }
  
  /**
   * Sets whether all <code>Shape3D</code> children nodes of <code>node</code> should have 
   * their normal flipped or not.
   * @param backFaceNormalFlip <code>true</code> if normals should be flipped.
   */
  private void setBackFaceNormalFlip(Node node, boolean backFaceNormalFlip) {
    if (node instanceof Group) {
      // Set back face normal flip of all children
      Enumeration<?> enumeration = ((Group)node).getAllChildren(); 
      while (enumeration.hasMoreElements()) {
        setBackFaceNormalFlip((Node)enumeration.nextElement(), backFaceNormalFlip);
      }
    } else if (node instanceof Shape3D) {
      Appearance appearance = ((Shape3D)node).getAppearance();
      if (appearance == null) {
        appearance = createAppearanceWithChangeCapabilities();
        ((Shape3D)node).setAppearance(appearance);
      }
      PolygonAttributes polygonAttributes = appearance.getPolygonAttributes();
      if (polygonAttributes == null) {
        polygonAttributes = createPolygonAttributesWithChangeCapabilities();
        appearance.setPolygonAttributes(polygonAttributes);
      }
      
      // Change back face normal flip
      polygonAttributes.setBackFaceNormalFlip(backFaceNormalFlip);
    }
  }

  private PolygonAttributes createPolygonAttributesWithChangeCapabilities() {
    PolygonAttributes polygonAttributes = new PolygonAttributes();
    polygonAttributes.setCapability(PolygonAttributes.ALLOW_CULL_FACE_READ);
    polygonAttributes.setCapability(PolygonAttributes.ALLOW_CULL_FACE_WRITE);
    polygonAttributes.setCapability(PolygonAttributes.ALLOW_NORMAL_FLIP_WRITE);
    return polygonAttributes;
  }

  private Appearance createAppearanceWithChangeCapabilities() {
    Appearance appearance = new Appearance();
    setAppearanceCapabilities(appearance);
    return appearance;
  }

  private void setAppearanceCapabilities(Appearance appearance) {
    // Allow future material and rendering attributes changes
    appearance.setCapability(Appearance.ALLOW_MATERIAL_READ);
    appearance.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
    appearance.setCapability(Appearance.ALLOW_RENDERING_ATTRIBUTES_READ);
    appearance.setCapability(Appearance.ALLOW_RENDERING_ATTRIBUTES_WRITE);
    appearance.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_READ);
    appearance.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_WRITE);
    appearance.setCapability(Appearance.ALLOW_TEXGEN_READ);
    appearance.setCapability(Appearance.ALLOW_TEXGEN_WRITE);
    appearance.setCapability(Appearance.ALLOW_TEXTURE_READ);
    appearance.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
    appearance.setCapability(Appearance.ALLOW_TEXTURE_ATTRIBUTES_READ);
    appearance.setCapability(Appearance.ALLOW_TEXTURE_ATTRIBUTES_WRITE);
    appearance.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_READ);
    appearance.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE);
    PolygonAttributes polygonAttributes = appearance.getPolygonAttributes();
    if (polygonAttributes != null) {
      polygonAttributes.setCapability(PolygonAttributes.ALLOW_CULL_FACE_READ);
      polygonAttributes.setCapability(PolygonAttributes.ALLOW_CULL_FACE_WRITE);
      polygonAttributes.setCapability(PolygonAttributes.ALLOW_NORMAL_FLIP_WRITE);
    }
  }
  
  /**
   * A class used to store the default material and texture of a shape.
   */
  private static class DefaultMaterialAndTexture {
    private final Material               material;
    private final TransparencyAttributes transparencyAttributes;
    private final TexCoordGeneration     texCoordGeneration;
    private final Texture                texture;
    private final TextureAttributes      textureAttributes;

    public DefaultMaterialAndTexture(Material material, 
                                     TransparencyAttributes transparencyAttributes, 
                                     TexCoordGeneration texCoordGeneration, 
                                     Texture texture, 
                                     TextureAttributes textureAttributes) {
      this.material = material;
      this.transparencyAttributes = transparencyAttributes;
      this.texCoordGeneration = texCoordGeneration;
      this.texture = texture;
      this.textureAttributes = textureAttributes;      
    }
    
    public Material getMaterial() {
      return this.material;
    }

    public TransparencyAttributes getTransparencyAttributes() {
      return this.transparencyAttributes;
    }
    
    public TexCoordGeneration getTexCoordGeneration() {
      return this.texCoordGeneration;
    }
    
    public Texture getTexture() {
      return this.texture;
    }
    
    public TextureAttributes getTextureAttributes() {
      return this.textureAttributes;
    }
  }
}