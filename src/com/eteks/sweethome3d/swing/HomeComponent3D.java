/*
 * HomeComponent3D.java 24 ao?t 2006
 *
 * Copyright (c) 2006 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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

import java.awt.GridLayout;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Group;
import javax.media.j3d.Light;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.JComponent;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import com.eteks.sweethome3d.model.FurnitureEvent;
import com.eteks.sweethome3d.model.FurnitureListener;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.PieceOfFurniture;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.model.WallEvent;
import com.eteks.sweethome3d.model.WallListener;
import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.objectfile.ObjectFile;
import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import com.sun.j3d.utils.universe.SimpleUniverse;

/**
 * A component that displays home walls and furniture with Java 3D. 
 * @author Emmanuel Puybaret
 */
public class HomeComponent3D extends JComponent {
  private Map<Object, ObjectBranch> homeObjects = 
      new HashMap<Object, ObjectBranch>();

  /**
   * Creates a 3D component that displays <code>home</code> walls and furniture.
   */
  public HomeComponent3D(Home home) {
    // Create the Java 3D canvas that will display home 
    Canvas3D canvas3D = new Canvas3D(
        SimpleUniverse.getPreferredConfiguration());
    // Link it to a default univers
    SimpleUniverse universe = new SimpleUniverse(canvas3D);
    universe.getViewingPlatform().setNominalViewingTransform();
    // Link scene matching home to universe
    universe.addBranchGraph(getSceneTree(home));
    
    // Layout canvas3D
    setLayout(new GridLayout(1, 1));
    add(canvas3D);
  }

  /**
   * Returns scene tree root.
   */
  private BranchGroup getSceneTree(Home home) {
    BranchGroup root = new BranchGroup();
    Group mainGroup = getMainGroup();
    Group behaviorGroup = getBehaviorGroup();

    // Build scene tree
    behaviorGroup.addChild(getHomeTree(home)); 
    mainGroup.addChild(behaviorGroup); 
    root.addChild(mainGroup);
    root.addChild(getBackgroundNode());
    for (Light light : getLights()) {
      root.addChild(light);
    }
    return root;
  }

  /**
   * Returns the group initialized to view a home 
   * with a angle of view of 45?.
   */
  private Group getMainGroup() {
    Transform3D rotationX = new Transform3D();
    rotationX.rotX(Math.PI / 4);
    return new TransformGroup(rotationX); 
  }

  /**
   * Returns the group which behavior controls the orientation of the scene. 
   */
  private Group getBehaviorGroup() {
    TransformGroup transformGroup = new TransformGroup();
    transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
    transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
    MouseRotate mouseBehavior = new MouseRotate(transformGroup);
    mouseBehavior.setFactor(mouseBehavior.getXFactor(), 0);
    mouseBehavior.setSchedulingBounds(new BoundingBox());
    // Add behavior to transform group to activate it
    transformGroup.addChild(mouseBehavior);    
    return transformGroup;
  }

  /**
   * Returns the background node.  
   */
  private Node getBackgroundNode() {
    Background background = new Background(0.9f, 0.9f, 0.9f);
    background.setApplicationBounds(new BoundingBox());
    return background;
  }
  
  /**
   * Returns the lights of the scene.
   */
  private Light [] getLights() {
    Light [] lights = {
      new DirectionalLight(new Color3f(1, 1, 1), 
                           new Vector3f(1, -1, -1)), 
      new DirectionalLight(new Color3f(1, 1, 1), 
                           new Vector3f(-1, -1, -1)), 
      new AmbientLight(new Color3f(0.8f, 0.8f, 0.8f))}; 

    for (Light light : lights) {
      light.setInfluencingBounds(new BoundingBox());
    }
    return lights;
  }
  
  /**
   * Returns <code>home</code> tree node, with branches for each wall 
   * and piece of furniture of <code>home</code>. 
   */
  private Node getHomeTree(Home home) {
    Group homeRoot = getHomeRoot();
    // Add walls and pieces already available 
    for (Wall wall : home.getWalls())
      addWall(homeRoot, wall, home);
    for (HomePieceOfFurniture piece : home.getFurniture())
      addPieceOfFurniture(homeRoot, piece);
    // Add wall and furniture listeners to home for further update
    addHomeListeners(home, homeRoot);
    return homeRoot;
  }

  /**
   * Returns the group at home subtree root.
   */
  private Group getHomeRoot() {
    // Create a transform group initialized to view 
    // a home of 1000 centimeters wide in a box of 1 unit centered at origin
    Transform3D translation = new Transform3D();
    translation.setTranslation(new Vector3f(-500, 0, -500));
    Transform3D homeTransform = new Transform3D();
    homeTransform.setScale(0.001);
    homeTransform.mul(translation);
    TransformGroup homeTransformGroup = 
        new TransformGroup(homeTransform);
    
    //  Allow transform group to have new children
    homeTransformGroup.setCapability(Group.ALLOW_CHILDREN_WRITE);
    homeTransformGroup.setCapability(Group.ALLOW_CHILDREN_EXTEND);
    return homeTransformGroup;
  }

  /**
   * Adds listeners to <code>home</code> that updates the scene <code>homeRoot</code>, 
   * each time a piece of furniture or a wall is added, updated or deleted. 
   */
  private void addHomeListeners(final Home home, final Group homeRoot) {
    home.addWallListener(new WallListener() {
      public void wallChanged(WallEvent ev) {
        Wall wall = ev.getWall();
        switch (ev.getType()) {
          case ADD :
            addWall(homeRoot, wall, home);
            break;
          case UPDATE :
            updateWall(wall);
            break;
          case DELETE :
            deleteObject(wall);
            break;
        }
      }
    });      
    home.addFurnitureListener(new FurnitureListener() {
      public void pieceOfFurnitureChanged(FurnitureEvent ev) {
        HomePieceOfFurniture piece = (HomePieceOfFurniture)ev.getPieceOfFurniture();
        switch (ev.getType()) {
          case ADD :
            addPieceOfFurniture(homeRoot, piece);
            break;
          case UPDATE :
            updatePieceOfFurniture(piece);
            break;
          case DELETE :
            deleteObject(piece);
            break;
        }
      }
    });
  }

  /**
   * Adds to <code>homeRoot</code> a wall branch matching <code>wall</code>.
   */
  private void addWall(Group homeRoot, Wall wall, Home home) {
    Wall3D wall3D = new Wall3D(wall, home);
    this.homeObjects.put(wall, wall3D);
    homeRoot.addChild(wall3D);
  }

  /**
   * Updates <code>wall</code> geometry, 
   * and the walls at its end or start.
   */
  private void updateWall(Wall wall) {
    this.homeObjects.get(wall).update();
    if (wall.getWallAtStart() != null) {
      this.homeObjects.get(wall.getWallAtStart()).update();                
    }
    if (wall.getWallAtEnd() != null) {
      this.homeObjects.get(wall.getWallAtEnd()).update();                
    }
  }
  
  /**
   * Detaches from the scene the branch matching <code>homeObject</code>.
   */
  private void deleteObject(Object homeObject) {
    this.homeObjects.get(homeObject).detach();
    this.homeObjects.remove(homeObject);
  }

  /**
   * Adds to <code>homeRoot</code> a piece branch matching <code>piece</code>.
   */
  private void addPieceOfFurniture(Group homeRoot, HomePieceOfFurniture piece) {
    HomePieceOfFurniture3D piece3D = new HomePieceOfFurniture3D(piece);
    this.homeObjects.put(piece, piece3D);
    homeRoot.addChild(piece3D);
  }

  /**
   * Updates <code>piece</code> scale, angle and location.
   */
  private void updatePieceOfFurniture(HomePieceOfFurniture piece) {
    this.homeObjects.get(piece).update();
  }
  
  /**
   * Root of a branch that matches a home object. 
   */
  private static abstract class ObjectBranch extends BranchGroup {
    public abstract void update();
  }

  /**
   * Root of wall branch.
   */
  private static class Wall3D extends ObjectBranch {
    private Home home;

    public Wall3D(Wall wall, Home home) {
      setUserData(wall);
      this.home = home;

      // Allow wall branch to be removed from its parent
      setCapability(BranchGroup.ALLOW_DETACH);
      // Allow to read branch shape child
      setCapability(BranchGroup.ALLOW_CHILDREN_READ);
      
      // Add wall shape to branch
      addChild(getWallShape());
      // Set wall shape geometry and appearance
      setWallGeometry();
      setWallAppearance();
    }

    /**
     * Returns an empty wall shape.
     */
    private Node getWallShape() {
      Shape3D wallShape = new Shape3D();
      // Allow wall shape to change its geometry
      wallShape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
      return wallShape;
    }

    @Override
    public void update() {
      setWallGeometry();
    }

    /**
     * Sets the 3D geometry of this wall that matches its 2D geometry.  
     */
    private void setWallGeometry() {
      float [][] wallPoints = ((Wall)getUserData()).getPoints();
      // Create points for the bottom and the top of the wall
      Point3f [] bottom = new Point3f [4];
      Point3f [] top    = new Point3f [4];
      for (int i = 0; i < bottom.length; i++) {
        bottom [i] = new Point3f(
            wallPoints[i][0], 0, wallPoints[i][1]);
        top [i] = new Point3f(
            wallPoints[i][0], home.getWallHeight(), wallPoints[i][1]);
      }
      // List of the 6 quadrilaterals of the wall
      Point3f [] wallCoordinates = {
          bottom [0], bottom [1], bottom [2], bottom [3],
          bottom [1], bottom [0], top [0], top [1],
          bottom [2], bottom [1], top [1], top [2],
          bottom [3], bottom [2], top [2], top [3],
          bottom [0], bottom [3], top [3], top [0],  
          top [3],    top [2],    top [1], top [0]};
      
      // Build wall geomtry
      GeometryInfo geometryInfo = 
        new GeometryInfo(GeometryInfo.QUAD_ARRAY);
      geometryInfo.setCoordinates(wallCoordinates);
      // Generates normals
      new NormalGenerator(0).generateNormals(geometryInfo);
      // Change wall geometry 
      ((Shape3D)getChild(0)).setGeometry(
          geometryInfo.getIndexedGeometryArray());
    }
    
    /**
     * Sets wall appearance with a white color.
     */
    private void setWallAppearance() {
      Appearance wallAppearance = new Appearance();
      Material material = new Material();
      wallAppearance.setMaterial(material);
      ((Shape3D)getChild(0)).setAppearance(wallAppearance);
    }    
  }

  /**
   * Root of piece of furniture branch.
   */
  private static class HomePieceOfFurniture3D extends ObjectBranch {
    public HomePieceOfFurniture3D(HomePieceOfFurniture piece) {
      setUserData(piece);      

      // Allow piece branch to be removed from its parent
      setCapability(BranchGroup.ALLOW_DETACH);
      // Allow to read branch transform child
      setCapability(BranchGroup.ALLOW_CHILDREN_READ);
      
      addChild(getPieceOfFurnitureNode());

      // Set piece model initial location, orientation and size 
      setPieceOfFurnitureTransform();
    }

    /**
     * Returns piece node with its transform group. 
     */
    private Node getPieceOfFurnitureNode() {
      TransformGroup pieceTransformGroup = new TransformGroup();
      // Allow the change of the transformation that sets piece size and position
      pieceTransformGroup.setCapability(
          TransformGroup.ALLOW_TRANSFORM_WRITE);
      // Add piece model to transform group
      pieceTransformGroup.addChild(getModelNode());
      return pieceTransformGroup;
    }

    @Override
    public void update() {
      setPieceOfFurnitureTransform();
    }

    /**
     * Sets the transformation applied to piece model to match
     * its location, its angle and its size.
     */
    public void setPieceOfFurnitureTransform() {
      HomePieceOfFurniture piece = (HomePieceOfFurniture)getUserData();
      // Set piece size
      Transform3D scale = new Transform3D();
      scale.setScale(new Vector3d(piece.getWidth(), piece.getHeight(), piece.getDepth()));
      // Change its angle around y axis
      Transform3D orientation = new Transform3D();
      orientation.rotY(-piece.getAngle());
      // Translate it to its location
      Transform3D pieceTransform = new Transform3D();
      pieceTransform.setTranslation(new Vector3f(piece.getX(), piece.getHeight() / 2, piece.getY()));      

      pieceTransform.mul(orientation);
      pieceTransform.mul(scale);
      // Change model transformation      
      ((TransformGroup)getChild(0)).setTransform(pieceTransform);
    }

    /**
     * Returns the 3D model of this piece that fits 
     * in a 1 unit wide box centered at the origin. 
     */
    private Node getModelNode() {
      PieceOfFurniture piece = (PieceOfFurniture)getUserData();
      Reader modelReader = null;
      try {
        // Read piece model from a object file 
        modelReader = new InputStreamReader(piece.getModel().openStream());
        ObjectFile loader = new ObjectFile();
        Scene scene = loader.load(modelReader);
        
        // Get model bounding box size
        BranchGroup modelScene = scene.getSceneGroup();
        BoundingBox modelBounds = getBounds(modelScene);
        Point3d lower = new Point3d();
        modelBounds.getLower(lower);
        Point3d upper = new Point3d();
        modelBounds.getUpper(upper);
        
        // Translate model to its center
        Transform3D translation = new Transform3D();
        translation.setTranslation(
            new Vector3d(-lower.x - (upper.x - lower.x) / 2, 
                -lower.y - (upper.y - lower.y) / 2, 
                -lower.z - (upper.z - lower.z) / 2));      
        // Scale model to make it fit in a 1 unit wide box
        Transform3D modelTransform = new Transform3D();
        modelTransform.setScale (
            new Vector3d(1 / (upper.x -lower.x), 
                1 / (upper.y - lower.y), 
                1 / (upper.z - lower.z)));
        modelTransform.mul(translation);
        // Add model scene to transform group
        TransformGroup modelTransformGroup = 
          new TransformGroup(modelTransform);
        modelTransformGroup.addChild(modelScene);
        return modelTransformGroup;
      } catch (IOException ex) {
        // In case of problem return a default box
        return getModelBox();
      } catch (IncorrectFormatException ex) {
        return getModelBox();
      } catch (ParsingErrorException ex) {
        return getModelBox();
      } finally {
        try {
          if (modelReader != null) {
            modelReader.close();
          }
        } catch (IOException ex) {
          throw new RuntimeException(ex);
        }
      }
    }

    /**
     * Returns a box that may replace model. 
     */
    private Node getModelBox() {
      Appearance boxAppearance = new Appearance();
      boxAppearance.setMaterial(new Material());
      return new Box(0.5f, 0.5f, 0.5f, boxAppearance);
    }

    /**
     * Returns the bounds of 3D shapes under <code>node</code>.
     * This method computes the exact box that contains all the shapes,
     * contrary to <code>node.getBounds()</code> that returns a bounding 
     * sphere for a scene.
     */
    private BoundingBox getBounds(Node node) {
      BoundingBox objectBounds = new BoundingBox(
          new Point3d(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY),
          new Point3d(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY));
      computeBounds(node, objectBounds);
      return objectBounds;
    }
    
    private void computeBounds(Node node, BoundingBox bounds) {
      if (node instanceof Group) {
        // Compute the bounds of all the node children
        Enumeration enumeration = ((Group)node).getAllChildren();
        while (enumeration.hasMoreElements ()) {
          computeBounds((Node)enumeration.nextElement (), bounds);
        }
      } else if (node instanceof Shape3D) {
        Bounds shapeBounds = ((Shape3D)node).getBounds();
        bounds.combine(shapeBounds);
      }
    }
  }
}
