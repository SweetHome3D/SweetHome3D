/*
 * Max3DSLoader.java 24 Nov. 2013
 *
 * Sweet Home 3D, Copyright (c) 2013 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;
import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Material;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Texture;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import javax.vecmath.TexCoord2f;

import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.Loader;
import com.sun.j3d.loaders.LoaderBase;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.SceneBase;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import com.sun.j3d.utils.image.TextureLoader;

/**
 * A loader for 3DS streams.<br> 
 * Mainly an adaptation in Java of the GNU LGPL C library available at www.lib3ds.org.
 * Note: this class is compatible with Java 3D 1.3.
 * @author Emmanuel Puybaret
 */
public class Max3DSLoader extends LoaderBase implements Loader {
  private enum ChunkID {
    NULL_CHUNK(0x0000),
    M3DMAGIC(0x4D4D),   // 3DS file
    SMAGIC(0x2D2D),    
    LMAGIC(0x2D3D),    
    MLIBMAGIC(0x3DAA),  // MLI file
    MATMAGIC(0x3DFF),    
    CMAGIC(0xC23D),     // PRJ file
    M3D_VERSION(0x0002),
    M3D_KFVERSION(0x0005),

    COLOR_FLOAT(0x0010),
    COLOR_24(0x0011),
    LINEAR_COLOR_24(0x0012),
    LINEAR_COLOR_FLOAT(0x0013),
    PERCENTAGE_INT(0x0030),
    FLOAT_PERCENTAGE(0x0031),

    EDITOR_DATA(0x3D3D),
    MESH_VERSION(0x3D3E),
    MASTER_SCALE(0x0100),
    LOW_SHADOW_BIAS(0x1400),
    HIGH_SHADOW_BIAS(0x1410),
    SHADOW_MAP_SIZE(0x1420),
    SHADOW_SAMPLES(0x1430),
    SHADOW_RANGE(0x1440),
    SHADOW_FILTER(0x1450),
    RAY_BIAS(0x1460),
    O_CONSTS(0x1500),
    AMBIENT_LIGHT(0x2100),
    BIT_MAP(0x1100),
    SOLID_BGND(0x1200),
    V_GRADIENT(0x1300),
    USE_BIT_MAP(0x1101),
    USE_SOLID_BGND(0x1201),
    USE_V_GRADIENT(0x1301),
    FOG(0x2200),
    FOG_BGND(0x2210),
    LAYER_FOG(0x2302),
    DISTANCE_CUE(0x2300),
    DCUE_BGND(0x2310),
    USE_FOG(0x2201),
    USE_LAYER_FOG(0x2303),
    USE_DISTANCE_CUE(0x2301),

    MATERIAL_ENTRY(0xAFFF),
    MATERIAL_NAME(0xA000),
    MATERIAL_AMBIENT(0xA010),
    MATERIAL_DIFFUSE(0xA020),
    MATERIAL_SPECULAR(0xA030),
    MATERIAL_SHININESS(0xA040),
    MATERIAL_SHIN2PCT(0xA041),
    MATERIAL_TRANSPARENCY(0xA050),
    MATERIAL_XPFALL(0xA052),
    MATERIAL_USE_XPFALL(0xA240),
    MATERIAL_REFBLUR(0xA053),
    MATERIAL_SHADING(0xA100),
    MATERIAL_USE_REFBLUR(0xA250),
    MATERIAL_SELF_ILLUM(0xA080),
    MATERIAL_TWO_SIDED(0xA081),
    MATERIAL_DECAL(0xA082),
    MATERIAL_ADDITIVE(0xA083),
    MATERIAL_SELF_ILPCT(0xA084),
    MATERIAL_WIRE(0xA085),
    MATERIAL_FACEMAP(0xA088),
    MATERIAL_PHONGSOFT(0xA08C),
    MATERIAL_WIREABS(0xA08E),
    MATERIAL_WIRE_SIZE(0xA087),
    MATERIAL_TEXMAP(0xA200),
    MATERIAL_SXP_TEXT_DATA(0xA320),
    MATERIAL_TEXMASK(0xA33E),
    MATERIAL_SXP_TEXTMASK_DATA(0xA32A),
    MATERIAL_TEX2MAP(0xA33A),
    MATERIAL_SXP_TEXT2_DATA(0xA321),
    MATERIAL_TEX2MASK(0xA340),
    MATERIAL_SXP_TEXT2MASK_DATA(0xA32C),
    MATERIAL_OPACMAP(0xA210),
    MATERIAL_SXP_OPAC_DATA(0xA322),
    MATERIAL_OPACMASK(0xA342),
    MATERIAL_SXP_OPACMASK_DATA(0xA32E),
    MATERIAL_BUMPMAP(0xA230),
    MATERIAL_SXP_BUMP_DATA(0xA324),
    MATERIAL_BUMPMASK(0xA344),
    MATERIAL_SXP_BUMPMASK_DATA(0xA330),
    MATERIAL_SPECMAP(0xA204),
    MATERIAL_SXP_SPEC_DATA(0xA325),
    MATERIAL_SPECMASK(0xA348),
    MATERIAL_SXP_SPECMASK_DATA(0xA332),
    MATERIAL_SHINMAP(0xA33C),
    MATERIAL_SXP_SHIN_DATA(0xA326),
    MATERIAL_SHINMASK(0xA346),
    MATERIAL_SXP_SHINMASK_DATA(0xA334),
    MATERIAL_SELFIMAP(0xA33D),
    MATERIAL_SXP_SELFI_DATA(0xA328),
    MATERIAL_SELFIMASK(0xA34A),
    MATERIAL_SXP_SELFIMASK_DATA(0xA336),
    MATERIAL_REFLMAP(0xA220),
    MATERIAL_REFLMASK(0xA34C),
    MATERIAL_SXP_REFLMASK_DATA(0xA338),
    MATERIAL_ACUBIC(0xA310),
    MATERIAL_MAPNAME(0xA300),
    MATERIAL_MAP_TILING(0xA351),
    MATERIAL_MAP_TEXBLUR(0xA353),
    MATERIAL_MAP_USCALE(0xA354),
    MATERIAL_MAP_VSCALE(0xA356),
    MATERIAL_MAP_UOFFSET(0xA358),
    MATERIAL_MAP_VOFFSET(0xA35A),
    MATERIAL_MAP_ANG(0xA35C),
    MATERIAL_MAP_COL1(0xA360),
    MATERIAL_MAP_COL2(0xA362),
    MATERIAL_MAP_RCOL(0xA364),
    MATERIAL_MAP_GCOL(0xA366),
    MATERIAL_MAP_BCOL(0xA368),

    NAMED_OBJECT(0x4000),
    LIGHT_OBJECT(0x4600),
    DL_OFF(0x4620),
    DL_OUTER_RANGE(0x465A),
    DL_INNER_RANGE(0x4659),
    DL_MULTIPLIER(0x465B),
    DL_EXCLUDE(0x4654),
    DL_ATTENUATE(0x4625),
    DL_SPOTLIGHT(0x4610),
    DL_SPOT_ROLL(0x4656),
    DL_SHADOWED(0x4630),
    DL_LOCAL_SHADOW2(0x4641),
    DL_SEE_CONE(0x4650),
    DL_SPOT_RECTANGULAR(0x4651),
    DL_SPOT_ASPECT(0x4657),
    DL_SPOT_PROJECTOR(0x4653),
    DL_SPOT_OVERSHOOT(0x4652),
    DL_RAY_BIAS(0x4658),
    DL_RAYSHAD(0x4627),
    CAMERA_OBJECT(0x4700),
    CAM_SEE_CONE(0x4710),
    CAM_RANGES(0x4720),
    OBJECT_HIDDEN(0x4010),
    OBJECT_VIS_LOFTER(0x4011),
    OBJECT_DOESNT_CAST(0x4012),
    OBJECT_DOESNT_RCVSHADOW(0x4017),
    OBJECT_MATTE(0x4013),
    OBJECT_FAST(0x4014),
    OBJ_PROCEDURAL(0x4015),
    OBJECT_FROZEN(0x4016),
    TRIANGLE_MESH_OBJECT(0x4100),
    POINT_ARRAY(0x4110),
    POINT_FLAG_ARRAY(0x4111),
    FACE_ARRAY(0x4120),
    MESH_MATERIAL_GROUP(0x4130),
    SMOOTHING_GROUP(0x4150),
    MESH_BOXMAP(0x4190),
    TEXTURE_COORDINATES(0x4140),
    MESH_MATRIX(0x4160),
    MESH_COLOR(0x4165),
    MESH_TEXTURE_INFO(0x4170),

    KEY_FRAMER_DATA(0xB000),
    KFHDR(0xB00A),
    KFSEG(0xB008),
    KFCURTIME(0xB009),
    AMBIENT_NODE_TAG(0xB001),
    OBJECT_NODE_TAG(0xB002),
    CAMERA_NODE_TAG(0xB003),
    TARGET_NODE_TAG(0xB004),
    LIGHT_NODE_TAG(0xB005),
    L_TARGET_NODE_TAG(0xB006),
    SPOTLIGHT_NODE_TAG(0xB007),
    NODE_ID(0xB030),
    NODE_HDR(0xB010),
    PIVOT(0xB013),
    INSTANCE_NAME(0xB011),
    MORPH_SMOOTH(0xB015),
    BOUNDBOX(0xB014),
    POS_TRACK_TAG(0xB020),
    COL_TRACK_TAG(0xB025),
    ROT_TRACK_TAG(0xB021),
    SCL_TRACK_TAG(0xB022),
    MORPH_TRACK_TAG(0xB026),
    FOV_TRACK_TAG(0xB023),
    ROLL_TRACK_TAG(0xB024),
    HOT_TRACK_TAG(0xB027),
    FALL_TRACK_TAG(0xB028),
    HIDE_TRACK_TAG(0xB029),

    POLY_2D(0x5000),
    SHAPE_OK(0x5010),
    SHAPE_NOT_OK(0x5011),
    SHAPE_HOOK(0x5020),
    PATH_3D(0x6000),
    PATH_MATRIX(0x6005),
    SHAPE_2D(0x6010),
    M_SCALE(0x6020),
    M_TWIST(0x6030),
    M_TEETER(0x6040),
    M_FIT(0x6050),
    M_BEVEL(0x6060),
    XZ_CURVE(0x6070),
    YZ_CURVE(0x6080),
    INTERPCT(0x6090),
    DEFORM_LIMIT(0x60A0),

    USE_CONTOUR(0x6100),
    USE_TWEEN(0x6110),
    USE_SCALE(0x6120),
    USE_TWIST(0x6130),
    USE_TEETER(0x6140),
    USE_FIT(0x6150),
    USE_BEVEL(0x6160),

    DEFAULT_VIEW(0x3000),
    VIEW_TOP(0x3010),
    VIEW_BOTTOM(0x3020),
    VIEW_LEFT(0x3030),
    VIEW_RIGHT(0x3040),
    VIEW_FRONT(0x3050),
    VIEW_BACK(0x3060),
    VIEW_USER(0x3070),
    VIEW_CAMERA(0x3080),
    VIEW_WINDOW(0x3090),

    VIEWPORT_LAYOUT_OLD(0x7000),
    VIEWPORT_DATA_OLD(0x7010),
    VIEWPORT_LAYOUT(0x7001),
    VIEWPORT_DATA(0x7011),
    VIEWPORT_DATA_3(0x7012),
    VIEWPORT_SIZE(0x7020),
    NETWORK_VIEW(0x7030);
    
    private short id;

    private ChunkID(int id) {
      this.id = (short)id;      
    }
    
    public static ChunkID valueOf(short id) {
      for (ChunkID chunck : values()) {
        if (id == chunck.id) {
          return chunck;
        }
      }
      return NULL_CHUNK;
    }
  };

  private final static Appearance DEFAULT_APPEARANCE;
  
  static {
    DEFAULT_APPEARANCE = new Appearance();
    DEFAULT_APPEARANCE.setMaterial(new Material(
        new Color3f(0.4000f, 0.4000f, 0.4000f), new Color3f(),
        new Color3f(0.7102f, 0.7020f, 0.6531f),
        new Color3f(0.3000f, 0.3000f, 0.3000f),
        128.0f));
  }
  
  private Boolean                  useCaches;
  private float                    masterScale;
  private List<Mesh3DS>            meshes;
  private Map<String, Material3DS> materials;

  /**
   * Sets whether this loader should try or avoid accessing to URLs with cache.
   * @param useCaches <id>Boolean.TRUE</id>, <id>Boolean.FALSE</id>, or 
   *    <id>null</id> then caches will be used according to the value 
   *    returned by {@link URLConnection#getDefaultUseCaches()}.
   */
  public void setUseCaches(Boolean useCaches) {
    this.useCaches = Boolean.valueOf(useCaches);
  }
  
  /**
   * Returns the scene described in the given 3DS file.
   */
  public Scene load(String file) throws FileNotFoundException, IncorrectFormatException, ParsingErrorException {
    URL baseUrl;
    try {
      if (this.basePath != null) {
        baseUrl = new File(this.basePath).toURI().toURL();
      } else {
        baseUrl = new File(file).toURI().toURL();
      } 
    } catch (MalformedURLException ex) {
      throw new FileNotFoundException(file);
    }
    return load(new FileInputStream(file), baseUrl);
  }

  /**
   * Returns the scene described in the given 3DS file URL.
   */
  public Scene load(URL url) throws FileNotFoundException, IncorrectFormatException, ParsingErrorException {
    URL baseUrl = this.baseUrl;
    if (this.baseUrl == null) {
      baseUrl = url;
    } 
    InputStream in;
    try {
      in = openStream(url, this.useCaches);
    } catch (IOException ex) {
      throw new FileNotFoundException("Can't read " + url);
    }
    return load(in, baseUrl);
  }
  
  /**
   * Returns an input stream ready to read data from the given URL.
   */
  private static InputStream openStream(URL url, Boolean useCaches) throws IOException {
    URLConnection connection = url.openConnection();
    if (useCaches != null) {
      connection.setUseCaches(useCaches.booleanValue());
    }
    return connection.getInputStream();
  }

  /**
   * Returns the scene described in the given 3DS file stream.
   */
  public Scene load(Reader reader) {
    throw new UnsupportedOperationException("Binary file format, can't read from a text");
  }

  /**
   * Returns the scene described in the given 3DS file.
   */
  private Scene load(InputStream in, URL baseUrl) throws FileNotFoundException {
    if (!(in instanceof BufferedInputStream)) {
      in = new BufferedInputStream(in);
    }
    try {
      return parseStream(new ChunksInputStream(in, baseUrl));
    } catch (IOException ex) {
      throw new ParsingErrorException(ex.getMessage());
    } finally {
      try {
        in.close();
      } catch (IOException ex) {
        throw new ParsingErrorException(ex.getMessage());
      }
    }
  }

  /**
   * Returns the scene with data read from the given 3DS stream.
   */
  private Scene parseStream(ChunksInputStream in) throws IOException {
    this.masterScale = 1;
    this.meshes = new ArrayList<Mesh3DS>(); 
    this.materials = new HashMap<String, Material3DS>();
    
    boolean magicNumberRead = false; 
    switch (in.readChunkHeader().getID()) {
      case M3DMAGIC :
      case MLIBMAGIC :
      case CMAGIC :
        magicNumberRead = true; 
        do {
          switch (in.readChunkHeader().getID()) {
            case M3D_VERSION :
              in.readLittleEndianUnsignedInt();
              break;
            case EDITOR_DATA : 
              parseEditorData(in);
              break;
            case KEY_FRAMER_DATA :
            default :
              in.readUntilChunkEnd();
              break;
          }
          in.releaseChunk();
        } while (in.isChunckEndReached());
        break;
      case EDITOR_DATA :
        parseEditorData(in);
        break;
      default :
        if (magicNumberRead) {
          in.readUntilChunkEnd();
        } else {
          throw new IncorrectFormatException("Bad magic number");
        }
    }
    in.releaseChunk();
    
    try {
      return createScene();
    } finally {
      this.meshes = null;
      this.materials = null;
    }
  }
  
  /**
   * Returns a new scene created from the parsed objects. 
   */
  private SceneBase createScene() {
    SceneBase scene = new SceneBase();
    BranchGroup sceneRoot = new BranchGroup();
    scene.setSceneGroup(sceneRoot);
    
    Transform3D rotation = new Transform3D();
    rotation.rotX(-Math.PI / 2);
    Transform3D scale = new Transform3D();
    scale.setScale(this.masterScale);
    rotation.mul(scale);
    TransformGroup mainTransformGroup = new TransformGroup(rotation);
    sceneRoot.addChild(mainTransformGroup);
    
    // Create appearances from 3DS materials
    Map<Material3DS, Appearance> appearances = new HashMap<Max3DSLoader.Material3DS, Appearance>();
    for (Material3DS material : this.materials.values()) {
      Appearance appearance = new Appearance();
      try {
        appearance.setName(material.getName());
      } catch (NoSuchMethodError ex) {
        // Don't set name with Java 3D < 1.4
      }
      Material appearanceMaterial = new Material();
      appearance.setMaterial(appearanceMaterial);
      Color3f ambientColor = material.getAmbientColor();
      if (ambientColor != null) {
        appearanceMaterial.setAmbientColor(ambientColor);
      }
      Color3f diffuseColor = material.getDiffuseColor();
      if (diffuseColor != null) {
        appearanceMaterial.setDiffuseColor(diffuseColor);
      }
      Color3f specularColor = material.getSpecularColor();
      if (specularColor != null) {
        appearanceMaterial.setSpecularColor(specularColor);
      }
      Float shininess = material.getShininess();
      if (shininess != null) {
        appearanceMaterial.setShininess(shininess * 128);
      }
      
      Float transparency = material.getTransparency();
      if (transparency != null && transparency > 0) {
        appearance.setTransparencyAttributes(new TransparencyAttributes(
            TransparencyAttributes.NICEST, Math.min(1f, transparency)));
      }
       
      appearance.setTexture(material.getTexture());
      appearances.put(material, appearance);
    }

    // Create shapes and their geometries
    for (Mesh3DS mesh : this.meshes) {
      Face3DS [] faces = mesh.getFaces();
      if (faces != null && faces.length > 0) {
        // Sort faces to ensure they are cited smoothing group by smoothing group
        Arrays.sort(faces, new Comparator<Face3DS>() {
            public int compare(Face3DS face1, Face3DS face2) {
              Material3DS material1 = face1.getMaterial();
              Material3DS material2 = face2.getMaterial();
              if (material1 == null) {
                return -1;
              } else if (material2 == null) {
                return 1;
              } else if (material1 != material2) {
                return material1.hashCode() - material2.hashCode();
              } else {
                Long smoothingGroup1 = face1.getSmoothingGroup();
                if (smoothingGroup1 == null) {
                  return -1;
                } else {
                  Long smoothingGroup2 = face2.getSmoothingGroup();
                  if (smoothingGroup2 == null) {
                    return 1;
                  } else {
                    return smoothingGroup1.compareTo(smoothingGroup2);
                  }
                }
              }
            }
          });
        
        Point3f [] vertices = mesh.getVertices();
        TexCoord2f [] textureCoordinates = mesh.getTextureCoordinates();
        TransformGroup transformGroup = mainTransformGroup;
        /* 
        // Do not use transformations: Coordinates are always already transformed in 3DS streams
        // and in the few cases they are incorrect, the provided transformation isn't correct
        Transform3D transform = mesh.getTransform();
        if (transform != null) {
          transformGroup = new TransformGroup();
          transformGroup.setTransform(transform);
          mainTransformGroup.addChild(transformGroup);
        }
        */
        
        int i = 0;
        Shape3D shape = null;
        Material3DS material = null;
        while (i < faces.length) {
          Face3DS firstFace = faces [i];
          Material3DS firstMaterial = firstFace.getMaterial();
          Long firstSmoothingGroup = firstFace.getSmoothingGroup();
          
          // Search how many faces share the same characteristics
          int max = i;
          while (++max < faces.length) {
            Face3DS face = faces [max];
            if (firstMaterial != face.getMaterial()
                || (firstSmoothingGroup != face.getSmoothingGroup()
                    && (firstSmoothingGroup == null 
                        || !firstSmoothingGroup.equals(face.getSmoothingGroup())))) {
              break;
            }
          }
          
          // Create indices arrays for the faces with an index between i and max
          int faceCount = max - i;
          int [] coordinatesIndices = new int [faceCount * 3];
          for (int j = 0, k = 0; j < faceCount; j++) {
            Face3DS face = faces [i + j];
            coordinatesIndices [k++] = face.getVertexAIndex();
            coordinatesIndices [k++] = face.getVertexBIndex();
            coordinatesIndices [k++] = face.getVertexCIndex();
          }
          
          GeometryInfo geometryInfo = new GeometryInfo(GeometryInfo.TRIANGLE_ARRAY);
          geometryInfo.setCoordinates(vertices);
          geometryInfo.setCoordinateIndices(coordinatesIndices);
          if (textureCoordinates != null) {
            geometryInfo.setTextureCoordinateParams(1, 2);
            geometryInfo.setTextureCoordinates(0, textureCoordinates);
            geometryInfo.setTextureCoordinateIndices(0, coordinatesIndices);
          }
          geometryInfo.recomputeIndices();
          
          NormalGenerator normalGenerator = new NormalGenerator(Math.PI / 2);
          if (firstSmoothingGroup == null) {
            normalGenerator.setCreaseAngle(Math.PI);
          }
          normalGenerator.generateNormals(geometryInfo);
          GeometryArray geometryArray = geometryInfo.getGeometryArray(true, true, false);
          
          if (shape == null || material != firstMaterial) {
            material = firstMaterial;
            Appearance appearance = appearances.get(firstMaterial);
            if (appearance == null) {
              appearance = DEFAULT_APPEARANCE;
            }
            appearance = (Appearance)appearance.cloneNodeComponent(false);
            if (firstMaterial != null && firstMaterial.isTwoSided()) {
              appearance.setPolygonAttributes(new PolygonAttributes(
                  PolygonAttributes.POLYGON_FILL, PolygonAttributes.CULL_NONE, 0));
            }
            shape = new Shape3D(geometryArray, appearance);   
            transformGroup.addChild(shape);
            scene.addNamedObject(mesh.getName() + (i == 0 ? "" : String.valueOf(i)), shape);
          } else {
            shape.addGeometry(geometryArray);
          }
          i = max;
        }
      }
    }
    return scene;
  }

  /**
   * Parses 3DS data in the current chunk.
   */
  private void parseEditorData(ChunksInputStream in) throws IOException {
    do {
      switch (in.readChunkHeader().getID()) {
        case MESH_VERSION : 
          in.readLittleEndianInt();
          break;
        case MASTER_SCALE: 
          this.masterScale = in.readLittleEndianFloat();
          break;
        case NAMED_OBJECT : 
          parseNamedObject(in);
          break;
        case MATERIAL_ENTRY : 
          Material3DS material = parseMaterial(in);
          this.materials.put(material.getName(), material);
          break;
        case O_CONSTS :
        case SHADOW_MAP_SIZE :
        case LOW_SHADOW_BIAS :
        case HIGH_SHADOW_BIAS :
        case SHADOW_SAMPLES :
        case SHADOW_RANGE :
        case SHADOW_FILTER :
        case RAY_BIAS :
        case VIEWPORT_LAYOUT :
        case DEFAULT_VIEW :
        case AMBIENT_LIGHT : 
        case BIT_MAP :
        case SOLID_BGND :
        case V_GRADIENT :
        case USE_BIT_MAP :
        case USE_SOLID_BGND :
        case USE_V_GRADIENT:
        case FOG :
        case LAYER_FOG :
        case DISTANCE_CUE :
        case USE_FOG :
        case USE_LAYER_FOG :
        case USE_DISTANCE_CUE : 
        default:
          in.readUntilChunkEnd();
          break;
      }
      in.releaseChunk();
    } while (in.isChunckEndReached());
  }

  /**
   * Parses named objects like mesh in the current chunk.
   */
  private void parseNamedObject(ChunksInputStream in) throws IOException {
    String name = in.readString(64);
    do {
      switch (in.readChunkHeader().getID()) {
        case TRIANGLE_MESH_OBJECT : 
          this.meshes.add(parseMeshData(in, name));
          break;
        case CAMERA_OBJECT : 
        case LIGHT_OBJECT : 
        case OBJECT_HIDDEN :
        case OBJECT_DOESNT_CAST :
        case OBJECT_VIS_LOFTER :
        case OBJECT_MATTE :
        case OBJECT_DOESNT_RCVSHADOW :
        case OBJECT_FAST :
        case OBJECT_FROZEN :
        default:
          in.readUntilChunkEnd();
          break;
      }
      in.releaseChunk();
    } while (in.isChunckEndReached());
  }

  /**
   * Returns the mesh read from the current chunk.  
   */
  private Mesh3DS parseMeshData(ChunksInputStream in, String name) throws IOException {
    Point3f [] vertices = null;
    TexCoord2f [] textureCoordinates = null;
    Transform3D transform = null;
    Short  color = null;
    Face3DS [] faces = null; 
    do {
      switch (in.readChunkHeader().getID()) {
        case MESH_MATRIX :
          transform = new Transform3D(parseMatrix(in));
          break;
        case MESH_COLOR : 
          color = in.readUnsignedByte();
          break;
        case POINT_ARRAY : 
          vertices = new Point3f [in.readLittleEndianUnsignedShort()];
          for (int i = 0; i < vertices.length; i++) {
            vertices [i] = new Point3f(in.readLittleEndianFloat(), 
                in.readLittleEndianFloat(), in.readLittleEndianFloat());
          }
          break;
        case FACE_ARRAY : 
          faces = parseFacesData(in);
          do {
            switch (in.readChunkHeader().getID()) {
              case MESH_MATERIAL_GROUP : 
                String materialName = in.readString(64);
                Material3DS material = null;
                if (this.materials != null) {
                  material = this.materials.get(materialName);
                }
                for (int i = 0, n = in.readLittleEndianUnsignedShort(); i < n; i++) {
                  int index = in.readLittleEndianUnsignedShort();
                  if (index < faces.length) {
                    faces [index].setMaterial(material);
                  }
                }
                break;
              case SMOOTHING_GROUP :
                for (int i = 0; i < faces.length; i++) {
                  faces [i].setSmoothingGroup(in.readLittleEndianUnsignedInt());
                }
                break;
              case MESH_BOXMAP :
              default:
                in.readUntilChunkEnd();
                break;
            }
            in.releaseChunk();
          } while (in.isChunckEndReached());
          break;
        case TEXTURE_COORDINATES : 
          textureCoordinates = new TexCoord2f [in.readLittleEndianUnsignedShort()];
          for (int i = 0; i < textureCoordinates.length; i++) {
            textureCoordinates [i] = 
                new TexCoord2f(in.readLittleEndianFloat(), in.readLittleEndianFloat());
          }
          break;
        case POINT_FLAG_ARRAY : 
        case MESH_TEXTURE_INFO : 
        default :
          in.readUntilChunkEnd();
          break;
      }
      in.releaseChunk();
    } while (in.isChunckEndReached());
    return new Mesh3DS(name, vertices, textureCoordinates, faces, color, transform);
  }

  /**
   * Returns the matrix read from the current chunk.  
   */
  private Transform3D parseMatrix(ChunksInputStream in) throws IOException {
    float [] matrix = {
        1, 0, 0, 0,
        0, 1, 0, 0,
        0, 0, 1, 0,
        0, 0, 0, 1};
    matrix [0] = in.readLittleEndianFloat();
    matrix [4] = in.readLittleEndianFloat();
    matrix [8] = in.readLittleEndianFloat();
    matrix [1] = in.readLittleEndianFloat();
    matrix [5] = in.readLittleEndianFloat();
    matrix [9] = in.readLittleEndianFloat();
    matrix [2] = in.readLittleEndianFloat();
    matrix [6] = in.readLittleEndianFloat();
    matrix [10] = in.readLittleEndianFloat();
  
    matrix [3] = in.readLittleEndianFloat();
    matrix [7] = in.readLittleEndianFloat();
    matrix [11] = in.readLittleEndianFloat();
    return new Transform3D(matrix);
  }

  /**
   * Returns the mesh faces read from the current chunk.  
   */
  private Face3DS [] parseFacesData(ChunksInputStream in) throws IOException {
    Face3DS [] faces = new Face3DS [in.readLittleEndianUnsignedShort()];
    for (int i = 0; i < faces.length; i++) {
      faces [i] = new Face3DS(
        in.readLittleEndianUnsignedShort(), 
        in.readLittleEndianUnsignedShort(), 
        in.readLittleEndianUnsignedShort(),
        in.readLittleEndianUnsignedShort());
    }
    return faces;
  }

  /**
   * Returns the 3DS material read from the current chunk.  
   */
  private Material3DS parseMaterial(ChunksInputStream in) throws IOException {
    String name = null;
    Color3f ambientColor = null;
    Color3f diffuseColor = null;
    Color3f specularColor = null;
    Float shininess = null;
    Float transparency = null;
    boolean twoSided = false;
    Texture texture = null;
    do {
      switch (in.readChunkHeader().getID()) {
        case MATERIAL_NAME : 
          name = in.readString(64);
          break;
        case MATERIAL_AMBIENT : 
          ambientColor = parseColor(in); 
          break;
        case MATERIAL_DIFFUSE : 
          diffuseColor = parseColor(in);
          break;
        case MATERIAL_SPECULAR : 
          specularColor = parseColor(in);
          break;
        case MATERIAL_SHININESS :
          shininess = parsePercentage(in);
          break;
        case MATERIAL_TRANSPARENCY :
          // 0 = fully opaque to 1 = fully transparent
          transparency = parsePercentage(in);
          break;
        case MATERIAL_TWO_SIDED :
          twoSided = true;
          break;         
        case MATERIAL_TEXMAP :
          texture = parseTextureMap(in);
          break;
        case MATERIAL_XPFALL :
        case MATERIAL_SELF_ILPCT : 
        case MATERIAL_SHIN2PCT : 
        case MATERIAL_USE_XPFALL : 
        case MATERIAL_SELF_ILLUM :  
        case MATERIAL_REFBLUR : 
        case MATERIAL_USE_REFBLUR :
        case MATERIAL_SHADING : 
        case MATERIAL_DECAL:
        case MATERIAL_ADDITIVE :
        case MATERIAL_FACEMAP :
        case MATERIAL_PHONGSOFT :
        case MATERIAL_WIRE :
        case MATERIAL_WIREABS :
        case MATERIAL_WIRE_SIZE :
        case MATERIAL_TEXMASK :
        case MATERIAL_TEX2MAP :
        case MATERIAL_TEX2MASK :
        case MATERIAL_OPACMAP :
        case MATERIAL_OPACMASK :
        case MATERIAL_BUMPMAP :
        case MATERIAL_BUMPMASK :
        case MATERIAL_SPECMAP :
        case MATERIAL_SPECMASK :
        case MATERIAL_SHINMAP :
        case MATERIAL_SHINMASK :
        case MATERIAL_SELFIMAP :
        case MATERIAL_SELFIMASK :
        case MATERIAL_REFLMAP :
        case MATERIAL_REFLMASK :
        case MATERIAL_ACUBIC :
        default:
          in.readUntilChunkEnd();
          break;
      }
      in.releaseChunk();
    } while (in.isChunckEndReached());
    return new Material3DS(name, ambientColor, diffuseColor, specularColor, 
        shininess, transparency, texture, twoSided);
  }

  /**
   * Returns the color read from the current chunk.  
   */
  private Color3f parseColor(ChunksInputStream in) throws IOException {
    boolean linearColor = false;
    Color3f color = null;
    do {
      switch (in.readChunkHeader().getID()) {
        case LINEAR_COLOR_24 :
          linearColor = true;
          color = new Color3f(in.readUnsignedByte() / 255.f, 
              in.readUnsignedByte() / 255.f, in.readUnsignedByte() / 255.f);
          break;
        case COLOR_24 :
          Color3f readColor = new Color3f(in.readUnsignedByte() / 255.f, 
              in.readUnsignedByte() / 255.f, in.readUnsignedByte() / 255.f);
          if (!linearColor) {
            color = readColor;
          }
          break;
        case LINEAR_COLOR_FLOAT : 
          linearColor = true;
          color = new Color3f(in.readLittleEndianFloat(), 
              in.readLittleEndianFloat(), in.readLittleEndianFloat());
          break;
        case COLOR_FLOAT :
          readColor = new Color3f(in.readLittleEndianFloat(), 
              in.readLittleEndianFloat(), in.readLittleEndianFloat());
          if (!linearColor) {
            color = readColor;
          }
          break;
        default:
          in.readUntilChunkEnd();
          break;
      }
      in.releaseChunk();
    } while (in.isChunckEndReached());
    return color;
  }

  /**
   * Returns the percentage read from the current chunk.  
   */
  private Float parsePercentage(ChunksInputStream in) throws IOException {
    Float percentage = null;
    do {
      switch (in.readChunkHeader().getID()) {
        case PERCENTAGE_INT :
          percentage = in.readLittleEndianShort() / 100.f;
          break;
        default:
          in.readUntilChunkEnd();
          break;
      }
      in.releaseChunk();
    } while (in.isChunckEndReached());
    return percentage;
  }

  /**
   * Returns the texture read from the current chunk.  
   */
  private Texture parseTextureMap(ChunksInputStream in) throws IOException {
    String mapName = null;
    do {
      switch (in.readChunkHeader().getID()) {
        case MATERIAL_MAPNAME :
          mapName = in.readString(64);
          break;
        case PERCENTAGE_INT :
        case MATERIAL_MAP_TILING :
        case MATERIAL_MAP_TEXBLUR: 
        case MATERIAL_MAP_USCALE:
        case MATERIAL_MAP_VSCALE :
        case MATERIAL_MAP_UOFFSET :
        case MATERIAL_MAP_VOFFSET :
        case MATERIAL_MAP_ANG :
        case MATERIAL_MAP_COL1 :
        case MATERIAL_MAP_COL2 :
        case MATERIAL_MAP_RCOL :
        case MATERIAL_MAP_GCOL :
        case MATERIAL_MAP_BCOL :
        default:
          in.readUntilChunkEnd();
          break;
      }
      in.releaseChunk();
    } while (in.isChunckEndReached());
    
    if (mapName != null) {
      Texture texture = readTexture(in, mapName);
      if (texture != null) {
        return texture;
      } else {
        // Test also if the texture file doesn't exist ignoring case
        URL baseUrl = in.getBaseURL();
        if (baseUrl != null) {
          if ("file".equals(baseUrl.getProtocol())) {
            String [] list = new File(baseUrl.getFile()).getParentFile().list();
            if (list != null) {
              for (String file : list) {
                if (file.equalsIgnoreCase(mapName)) {
                  return readTexture(in, file);
                }
              }
            }
          } else if ("jar".equals(baseUrl.getProtocol())) {
            String file = baseUrl.getFile();
            URL zipUrl = new URL(file.substring(0, file.indexOf('!'))); 
            ZipInputStream zipIn = null;
            try {
              // Search an entry of zip url equal to mapName ignoring case
              zipIn = new ZipInputStream(zipUrl.openStream());
              for (ZipEntry entry; (entry = zipIn.getNextEntry()) != null; ) {
                if (entry.getName().equalsIgnoreCase(mapName)) {
                  return readTexture (in, entry.getName());
                }
              }
              return null;
            } finally {
              if (zipIn != null) {
                zipIn.close();
              }
            } 
          }
        }
      }
    }
    return null;
  }

  /**
   * Returns the texture read from the given file.  
   */
  private Texture readTexture(ChunksInputStream in, String fileName) throws IOException {
    InputStream imageStream = null;
    try {
      URL baseUrl = in.getBaseURL();
      URL textureImageUrl = baseUrl != null
          ? new URL(baseUrl, fileName.replace("%", "%25").replace("#", "%23"))
          : new File(fileName).toURI().toURL();
      imageStream = openStream(textureImageUrl, useCaches);
      BufferedImage textureImage = ImageIO.read(imageStream);          
      if (textureImage != null) {
        TextureLoader textureLoader = new TextureLoader(textureImage);
        Texture texture = textureLoader.getTexture();
        // Keep in user data the URL of the texture image
        texture.setUserData(textureImageUrl);
        return texture;
      }
    } catch (IOException ex) {
      // Ignore images at other format
    } catch (RuntimeException ex) {
      // Take into account exceptions of Java 3D 1.5 ImageException class
      // in such a way program can run in Java 3D 1.3.1
      if (ex.getClass().getName().equals("com.sun.j3d.utils.image.ImageException")) {
        // Ignore images not supported by TextureLoader
      } else {
        throw ex;
      }
    } finally {
      if (imageStream != null) {
        imageStream.close();
      }
    }
    return null;
  }

  /**
   * A chunk with its ID and length.
   */
  private static class Chunk3DS {
    private final ChunkID id;
    private final int     length;
    private int           readLength;

    public Chunk3DS(short id, int length) throws IOException {
      if (length < 6) {
        throw new IncorrectFormatException("Invalid chunk " + id + " length " + length);
      }
      this.id = ChunkID.valueOf(id);
      this.length = length;
      this.readLength = 6;
    }
    
    public ChunkID getID() {
      return this.id;
    }
    
    public int getLength() {
      return this.length;
    }
    
    public void incrementReadLength(int readBytes) {
      this.readLength += readBytes;
    }
    
    public int getReadLength() {
      return this.readLength;
    }
    
    @Override
    public String toString() {
      return this.id + " " + this.length;
    }
  }
  
  /**
   * An input stream storing chunks hierarchy and other data required during parsing. 
   */
  private static class ChunksInputStream extends FilterInputStream {
    private Stack<Chunk3DS> stack;
    private URL             baseUrl;

    public ChunksInputStream(InputStream in, URL baseUrl) {
      super(in);
      this.stack = new Stack<Chunk3DS>();
      this.baseUrl = baseUrl;
    }

    public URL getBaseURL() {
      return this.baseUrl;
    }

    /**
     * Reads the next chunk id and length, pushes it in the stack and returns it.
     * <code>null</code> will be returned if the end of the stream is reached.
     */
    public Chunk3DS readChunkHeader() throws IOException {
      short chunkId;
      try {
        chunkId = readLittleEndianShort(false);
      } catch (EOFException ex) {
        return null;
      }
      Chunk3DS chunk = new Chunk3DS(chunkId, readLittleEndianInt(false));
      this.stack.push(chunk);
      return chunk;
    }
    
    /**
     * Checks if the chunk at the top of stack was entirely read. 
     */
    public void releaseChunk() {      
      Chunk3DS chunk = this.stack.pop();
      if (chunk.getLength() != chunk.getReadLength()) {
        throw new IncorrectFormatException("Chunk " + chunk.getID() + " invalid length. " 
            + "Expected to read " + chunk.getLength() + " bytes, but actually read " + chunk.getReadLength() + " bytes");
      }
      if (!this.stack.isEmpty()) {
        this.stack.peek().incrementReadLength(chunk.getLength());
      }      
    }

    /**
     * Returns <code>true</code> if the current chunk end was reached.
     */
    public boolean isChunckEndReached() {
      Chunk3DS chunk = this.stack.peek();
      return chunk.getLength() != chunk.getReadLength();
    }
    
    /**
     * Reads the stream until the end of the current chunk.
     */
    public void readUntilChunkEnd() throws IOException {
      Chunk3DS chunk = this.stack.peek();
      int remainingLength = chunk.getLength() - chunk.getReadLength();
      for (int length = remainingLength; length > 0; length--) {
        if (this.in.read() < 0) {
          throw new IncorrectFormatException("Chunk " + chunk.getID() + " too short");
        }
      }
      chunk.incrementReadLength(remainingLength);
    }
    
    /**
     * Returns the unsigned byte read from this stream.
     */
    public short readUnsignedByte() throws IOException {
      int b = this.in.read();
      if (b == -1) {
        throw new EOFException();
      } else {
        this.stack.peek().incrementReadLength(1);
        return (short)(b & 0xFF);
      }
    }
    
    /**
     * Returns the unsigned short read from this stream.
     */
    public int readLittleEndianUnsignedShort() throws IOException {
      return (int)readLittleEndianShort(true) & 0xFFFF;
    }
    
    /**
     * Returns the short read from this stream.
     */
    public short readLittleEndianShort() throws IOException {
      return readLittleEndianShort(true);
    }
    
    private short readLittleEndianShort(boolean incrementReadLength) throws IOException {
      int b1 = this.in.read();
      if (b1 == -1) {
        throw new EOFException();
      }
      int b2 = this.in.read();
      if (b2 == -1) {
        throw new IncorrectFormatException("Can't read short");
      }
      if (incrementReadLength) {
        this.stack.peek().incrementReadLength(2);
      }
      return (short)((b2 << 8) | b1);
    }

    /**
     * Returns the float read from this stream.
     */
    public float readLittleEndianFloat() throws IOException {
      return Float.intBitsToFloat(readLittleEndianInt(true));
    }
    
    /**
     * Returns the unsigned integer read from this stream.
     */
    public long readLittleEndianUnsignedInt() throws IOException {
      return (long)readLittleEndianInt(true) & 0xFFFFFFFFL;
    }
    
    /**
     * Returns the integer read from this stream.
     */
    public int readLittleEndianInt() throws IOException {
      return readLittleEndianInt(true);
    }
    
    private int readLittleEndianInt(boolean incrementReadLength) throws IOException {
      int b1 = this.in.read();
      if (b1 == -1) {
        throw new EOFException();
      }
      int b2 = this.in.read();
      int b3 = this.in.read();
      int b4 = this.in.read();
      if (b2 == -1 || b3 == -1 || b4 == -1)
        throw new IncorrectFormatException("Can't read int");
      if (incrementReadLength) {
        this.stack.peek().incrementReadLength(4);
      }
      return (b4 << 24) | (b3 << 16) | (b2 << 8) | b1;
    }

    /**
     * Returns the string read from this stream.
     */
    public String readString(int max) throws IOException {
      byte [] stringBytes = new byte [max];
      int b;
      int i = 0; 
      // Read max characters until terminal 0
      while ((b = this.in.read()) != -1 && b != 0 && i < max) {
        stringBytes [i++] = (byte)b;
      }
      if (b == -1) {
        throw new IncorrectFormatException("Unexpected end of file");
      }
      if (i >= max) {
        throw new IncorrectFormatException("Invalid string");
      }
      this.stack.peek().incrementReadLength(i + 1);
      return new String(stringBytes, 0, i, "ISO-8859-1");
    }
  }
  
  /**
   * 3DS mesh.
   */
  private static class Mesh3DS {
    private String        name;
    private Point3f []    vertices;
    private TexCoord2f [] textureCoordinates;
    private Face3DS []    faces = null;
    private Short         color;
    private Transform3D   transform;

    public Mesh3DS(String name, 
                   Point3f [] vertices, TexCoord2f [] textureCoordinates, Face3DS [] faces,
                   Short color, Transform3D transform) {
      this.name = name;
      this.vertices = vertices;
      this.textureCoordinates = textureCoordinates;
      this.faces = faces;
      this.color = color;
      this.transform = transform;
    }
    
    public String getName() {
      return this.name;
    }
    
    public Point3f [] getVertices() {
      return this.vertices;
    }
    
    public TexCoord2f [] getTextureCoordinates() {
      return this.textureCoordinates;
    }
    
    public Face3DS [] getFaces() {
      return this.faces;
    }
    
    public Transform3D getTransform() {
      return this.transform;
    }
  }

  /**
   * 3DS face.
   */
  private static class Face3DS {
    private int         vertexAIndex;
    private int         vertexBIndex;
    private int         vertexCIndex;
    private Material3DS material;
    private Long        smoothingGroup;

    public Face3DS(int vertexAIndex,
                   int vertexBIndex,
                   int vertexCIndex,
                   int flags) {
      this.vertexAIndex = vertexAIndex;
      this.vertexBIndex = vertexBIndex;
      this.vertexCIndex = vertexCIndex;
    }
    
    public int getVertexAIndex() {
      return this.vertexAIndex;
    }

    public int getVertexBIndex() {
      return this.vertexBIndex;
    }

    public int getVertexCIndex() {
      return this.vertexCIndex;
    }

    public void setMaterial(Material3DS material) {
      this.material = material;
    }

    public Material3DS getMaterial() {
      return this.material;
    }
    
    public void setSmoothingGroup(Long smoothingGroup) {
      this.smoothingGroup = smoothingGroup;
    }
    
    public Long getSmoothingGroup() {
      return this.smoothingGroup;
    }
  }

  /**
   * 3DS material.
   */
  private static class Material3DS {
    private String  name;
    private Color3f ambientColor;
    private Color3f diffuseColor;
    private Color3f specularColor;
    private Float   shininess;
    private Float   transparency;
    private Texture texture;
    private boolean twoSided;
    
    public Material3DS(String name, Color3f ambientColor, Color3f diffuseColor, Color3f specularColor,
                       Float shininess, Float transparency, Texture texture, boolean twoSided) {
      this.name = name;
      this.ambientColor = ambientColor;
      this.diffuseColor = diffuseColor;
      this.specularColor = specularColor;
      this.shininess = shininess;
      this.transparency = transparency;
      this.texture = texture;
      this.twoSided = twoSided;
    }

    public String getName() {
      return this.name;
    }
    
    public boolean isTwoSided() {
      return this.twoSided;
    }
    
    public Color3f getAmbientColor() {
      return this.ambientColor;
    }
    
    public Color3f getDiffuseColor() {
      return this.diffuseColor;
    }
    
    public Color3f getSpecularColor() {
      return this.specularColor;
    }
    
    public Float getShininess() {
      return this.shininess;
    }
    
    public Float getTransparency() {
      return this.transparency;
    }
    
    public Texture getTexture() {
      return this.texture;
    }
  }
}
