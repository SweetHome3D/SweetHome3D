/*
 * OBJLoader.java 10 févr. 2009
 *
 * Copyright (c) 2009 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.IndexedGeometryArray;
import javax.media.j3d.LineStripArray;
import javax.media.j3d.Material;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TexCoordGeneration;
import javax.media.j3d.Texture;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import javax.vecmath.TexCoord2f;
import javax.vecmath.Vector3f;

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
 * An OBJ + MTL loader. 
 * It supports the same features as {@link com.sun.j3d.loaders.objectfile.ObjectFile ObjectFile}
 * Java 3D class, expected for texture images format (supports only BMP, WBMP, GIF, JPEG and PNG format).
 * Compared to <code>ObjectFile</code>, this class supports transparency as defined in
 * <a href="http://local.wasp.uwa.edu.au/~pbourke/dataformats/mtl/">MTL file format</a> 
 * specifications, and doesn't oblige to define texture coordinate on all vertices 
 * when only one face needs such coordinates. Material description is stored in 
 * {@link OBJMaterial OBJMaterial} instances to be able to use additional OBJ information
 * in other circumstances.<br>
 * Note: this class is compatible with Java 3D 1.3.
 * @author Emmanuel Puybaret
 */
public class OBJLoader extends LoaderBase implements Loader {
  /**
   * Description of the default Java 3D materials at MTL format 
   * (copied from com.sun.j3d.loaders.objectfile.DefaultMaterials class with inverse d transparency factor)
   */
  private static final String JAVA_3D_MATERIALS = 
    "newmtl amber\n" +
    "Ka 0.0531 0.0531 0.0531\n" +
    "Kd 0.5755 0.2678 0.0000\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 60.0000\n" +
    "\n" +
    "newmtl amber_trans\n" +
    "Ka 0.0531 0.0531 0.0531\n" +
    "Kd 0.5755 0.2678 0.0000\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "d 0.1600\n" +
    "Ns 60.0000\n" +
    "\n" +
    "newmtl charcoal\n" +
    "Ka 0.0082 0.0082 0.0082\n" +
    "Kd 0.0041 0.0041 0.0041\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 60.0000\n" +
    "\n" +
    "newmtl lavendar\n" +
    "Ka 0.1281 0.0857 0.2122\n" +
    "Kd 0.2187 0.0906 0.3469\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 60.0000\n" +
    "\n" +
    "newmtl navy_blue\n" +
    "Ka 0.0000 0.0000 0.0490\n" +
    "Kd 0.0000 0.0000 0.0531\n" +
    "Ks 0.1878 0.1878 0.1878\n" +
    "illum 2\n" +
    "Ns 91.4700\n" +
    "\n" +
    "newmtl pale_green\n" +
    "Ka 0.0444 0.0898 0.0447\n" +
    "Kd 0.0712 0.3796 0.0490\n" +
    "Ks 0.1878 0.1878 0.1878\n" +
    "illum 2\n" +
    "Ns 91.4700\n" +
    "\n" +
    "newmtl pale_pink\n" +
    "Ka 0.0898 0.0444 0.0444\n" +
    "Kd 0.6531 0.2053 0.4160\n" +
    "Ks 0.1878 0.1878 0.1878\n" +
    "illum 2\n" +
    "Ns 91.4700\n" +
    "\n" +
    "newmtl pale_yellow\n" +
    "Ka 0.3606 0.3755 0.0935\n" +
    "Kd 0.6898 0.6211 0.1999\n" +
    "Ks 0.1878 0.1878 0.1878\n" +
    "illum 2\n" +
    "Ns 91.4700\n" +
    "\n" +
    "newmtl peach\n" +
    "Ka 0.3143 0.1187 0.0167\n" +
    "Kd 0.6367 0.1829 0.0156\n" +
    "Ks 0.1878 0.1878 0.1878\n" +
    "illum 2\n" +
    "Ns 91.4700\n" +
    "\n" +
    "newmtl periwinkle\n" +
    "Ka 0.0000 0.0000 0.1184\n" +
    "Kd 0.0000 0.0396 0.8286\n" +
    "Ks 0.1878 0.1878 0.1878\n" +
    "illum 2\n" +
    "Ns 91.4700\n" +
    "\n" +
    "newmtl redwood\n" +
    "Ka 0.0204 0.0027 0.0000\n" +
    "Kd 0.2571 0.0330 0.0000\n" +
    "Ks 0.1878 0.1878 0.1878\n" +
    "illum 2\n" +
    "Ns 91.4700\n" +
    "\n" +
    "newmtl smoked_glass\n" +
    "Ka 0.0000 0.0000 0.0000\n" +
    "Kd 0.0041 0.0041 0.0041\n" +
    "Ks 0.1878 0.1878 0.1878\n" +
    "illum 2\n" +
    "d 0.0200\n" +
    "Ns 91.4700\n" +
    "\n" +
    "newmtl aqua_filter\n" +
    "Ka 0.0000 0.0000 0.0000\n" +
    "Kd 0.3743 0.6694 0.5791\n" +
    "Ks 0.1878 0.1878 0.1878\n" +
    "illum 2\n" +
    "d 0.0200\n" +
    "Ns 91.4700\n" +
    "\n" +
    "newmtl yellow_green\n" +
    "Ka 0.0000 0.0000 0.0000\n" +
    "Kd 0.1875 0.4082 0.0017\n" +
    "Ks 0.1878 0.1878 0.1878\n" +
    "illum 2\n" +
    "Ns 91.4700\n" +
    "\n" +
    "newmtl bluetint\n" +
    "Ka 0.1100 0.4238 0.5388\n" +
    "Kd 0.0468 0.7115 0.9551\n" +
    "Ks 0.3184 0.3184 0.3184\n" +
    "illum 9\n" +
    "d 0.4300\n" +
    "Ns 60.0000\n" +
    "sharpness 60.0000\n" +
    "\n" +
    "newmtl plasma\n" +
    "Ka 0.4082 0.0816 0.2129\n" +
    "Kd 1.0000 0.0776 0.4478\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 9\n" +
    "d 0.2500\n" +
    "Ns 60.0000\n" +
    "sharpness 60.0000\n" +
    "\n" +
    "newmtl emerald\n" +
    "Ka 0.0470 1.0000 0.0000\n" +
    "Kd 0.0470 1.0000 0.0000\n" +
    "Ks 0.2000 0.2000 0.2000\n" +
    "illum 9\n" +
    "d 0.2500\n" +
    "Ns 60.0000\n" +
    "sharpness 60.0000\n" +
    "\n" +
    "newmtl ruby\n" +
    "Ka 1.0000 0.0000 0.0000\n" +
    "Kd 1.0000 0.0000 0.0000\n" +
    "Ks 0.2000 0.2000 0.2000\n" +
    "illum 9\n" +
    "d 0.2500\n" +
    "Ns 60.0000\n" +
    "sharpness 60.0000\n" +
    "\n" +
    "newmtl sapphire\n" +
    "Ka 0.0235 0.0000 1.0000\n" +
    "Kd 0.0235 0.0000 1.0000\n" +
    "Ks 0.2000 0.2000 0.2000\n" +
    "illum 9\n" +
    "d 0.2500\n" +
    "Ns 60.0000\n" +
    "sharpness 60.0000\n" +
    "\n" +
    "newmtl white\n" +
    "Ka 0.4000 0.4000 0.4000\n" +
    "Kd 1.0000 1.0000 1.0000\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 60.0000\n" +
    "\n" +
    "newmtl red\n" +
    "Ka 0.4449 0.0000 0.0000\n" +
    "Kd 0.7714 0.0000 0.0000\n" +
    "Ks 0.8857 0.0000 0.0000\n" +
    "illum 2\n" +
    "Ns 136.4300\n" +
    "\n" +
    "newmtl blue_pure\n" +
    "Ka 0.0000 0.0000 0.5000\n" +
    "Kd 0.0000 0.0000 1.0000\n" +
    "Ks 0.0000 0.0000 0.5000\n" +
    "illum 2\n" +
    "Ns 65.8900\n" +
    "\n" +
    "newmtl lime\n" +
    "Ka 0.0000 0.5000 0.0000\n" +
    "Kd 0.0000 1.0000 0.0000\n" +
    "Ks 0.0000 0.5000 0.0000\n" +
    "illum 2\n" +
    "Ns 65.8900\n" +
    "\n" +
    "newmtl green\n" +
    "Ka 0.0000 0.2500 0.0000\n" +
    "Kd 0.0000 0.2500 0.0000\n" +
    "Ks 0.0000 0.2500 0.0000\n" +
    "illum 2\n" +
    "Ns 65.8900\n" +
    "\n" +
    "newmtl yellow\n" +
    "Ka 1.0000 0.6667 0.0000\n" +
    "Kd 1.0000 0.6667 0.0000\n" +
    "Ks 1.0000 0.6667 0.0000\n" +
    "illum 2\n" +
    "Ns 65.8900\n" +
    "\n" +
    "newmtl purple\n" +
    "Ka 0.5000 0.0000 1.0000\n" +
    "Kd 0.5000 0.0000 1.0000\n" +
    "Ks 0.5000 0.0000 1.0000\n" +
    "illum 2\n" +
    "Ns 65.8900\n" +
    "\n" +
    "newmtl orange\n" +
    "Ka 1.0000 0.1667 0.0000\n" +
    "Kd 1.0000 0.1667 0.0000\n" +
    "Ks 1.0000 0.1667 0.0000\n" +
    "illum 2\n" +
    "Ns 65.8900\n" +
    "\n" +
    "newmtl grey\n" +
    "Ka 0.5000 0.5000 0.5000\n" +
    "Kd 0.1837 0.1837 0.1837\n" +
    "Ks 0.5000 0.5000 0.5000\n" +
    "illum 2\n" +
    "Ns 65.8900\n" +
    "\n" +
    "newmtl rubber\n" +
    "Ka 0.0000 0.0000 0.0000\n" +
    "Kd 0.0100 0.0100 0.0100\n" +
    "Ks 0.1000 0.1000 0.1000\n" +
    "illum 2\n" +
    "Ns 65.8900\n" +
    "\n" +
    "newmtl flaqua\n" +
    "Ka 0.0000 0.4000 0.4000\n" +
    "Kd 0.0000 0.5000 0.5000\n" +
    "illum 1\n" +
    "\n" +
    "newmtl flblack\n" +
    "Ka 0.0000 0.0000 0.0000\n" +
    "Kd 0.0041 0.0041 0.0041\n" +
    "illum 1\n" +
    "\n" +
    "newmtl flblue_pure\n" +
    "Ka 0.0000 0.0000 0.5592\n" +
    "Kd 0.0000 0.0000 0.7102\n" +
    "illum 1\n" +
    "\n" +
    "newmtl flgrey\n" +
    "Ka 0.2163 0.2163 0.2163\n" +
    "Kd 0.5000 0.5000 0.5000\n" +
    "illum 1\n" +
    "\n" +
    "newmtl fllime\n" +
    "Ka 0.0000 0.3673 0.0000\n" +
    "Kd 0.0000 1.0000 0.0000\n" +
    "illum 1\n" +
    "\n" +
    "newmtl florange\n" +
    "Ka 0.6857 0.1143 0.0000\n" +
    "Kd 1.0000 0.1667 0.0000\n" +
    "illum 1\n" +
    "\n" +
    "newmtl flpurple\n" +
    "Ka 0.2368 0.0000 0.4735\n" +
    "Kd 0.3755 0.0000 0.7510\n" +
    "illum 1\n" +
    "\n" +
    "newmtl flred\n" +
    "Ka 0.4000 0.0000 0.0000\n" +
    "Kd 1.0000 0.0000 0.0000\n" +
    "illum 1\n" +
    "\n" +
    "newmtl flyellow\n" +
    "Ka 0.7388 0.4925 0.0000\n" +
    "Kd 1.0000 0.6667 0.0000\n" +
    "illum 1\n" +
    "\n" +
    "newmtl pink\n" +
    "Ka 0.9469 0.0078 0.2845\n" +
    "Kd 0.9878 0.1695 0.6702\n" +
    "Ks 0.7429 0.2972 0.2972\n" +
    "illum 2\n" +
    "Ns 106.2000\n" +
    "\n" +
    "newmtl flbrown\n" +
    "Ka 0.0571 0.0066 0.0011\n" +
    "Kd 0.1102 0.0120 0.0013\n" +
    "illum 1\n" +
    "\n" +
    "newmtl brown\n" +
    "Ka 0.1020 0.0185 0.0013\n" +
    "Kd 0.0857 0.0147 0.0000\n" +
    "Ks 0.1633 0.0240 0.0000\n" +
    "illum 2\n" +
    "Ns 65.8900\n" +
    "\n" +
    "newmtl glass\n" +
    "Ka 1.0000 1.0000 1.0000\n" +
    "Kd 0.4873 0.4919 0.5306\n" +
    "Ks 0.6406 0.6939 0.9020\n" +
    "illum 2\n" +
    "Ns 200.0000\n" +
    "\n" +
    "newmtl flesh\n" +
    "Ka 0.4612 0.3638 0.2993\n" +
    "Kd 0.5265 0.4127 0.3374\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 60.0000\n" +
    "\n" +
    "newmtl aqua\n" +
    "Ka 0.0000 0.4000 0.4000\n" +
    "Kd 0.0000 0.5000 0.5000\n" +
    "Ks 0.5673 0.5673 0.5673\n" +
    "illum 2\n" +
    "Ns 60.0000\n" +
    "\n" +
    "newmtl black\n" +
    "Ka 0.0000 0.0000 0.0000\n" +
    "Kd 0.0020 0.0020 0.0020\n" +
    "Ks 0.5184 0.5184 0.5184\n" +
    "illum 2\n" +
    "Ns 157.3600\n" +
    "\n" +
    "newmtl silver\n" +
    "Ka 0.9551 0.9551 0.9551\n" +
    "Kd 0.6163 0.6163 0.6163\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 60.0000\n" +
    "\n" +
    "newmtl dkblue_pure\n" +
    "Ka 0.0000 0.0000 0.0449\n" +
    "Kd 0.0000 0.0000 0.1347\n" +
    "Ks 0.0000 0.0000 0.5673\n" +
    "illum 2\n" +
    "Ns 65.8900\n" +
    "\n" +
    "newmtl fldkblue_pure\n" +
    "Ka 0.0000 0.0000 0.0449\n" +
    "Kd 0.0000 0.0000 0.1347\n" +
    "illum 1\n" +
    "\n" +
    "newmtl dkgreen\n" +
    "Ka 0.0000 0.0122 0.0000\n" +
    "Kd 0.0058 0.0245 0.0000\n" +
    "Ks 0.0000 0.0490 0.0000\n" +
    "illum 2\n" +
    "Ns 60.0000\n" +
    "\n" +
    "newmtl dkgrey\n" +
    "Ka 0.0490 0.0490 0.0490\n" +
    "Kd 0.0490 0.0490 0.0490\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 60.0000\n" +
    "\n" +
    "newmtl ltbrown\n" +
    "Ka 0.1306 0.0538 0.0250\n" +
    "Kd 0.2776 0.1143 0.0531\n" +
    "Ks 0.3000 0.1235 0.0574\n" +
    "illum 2\n" +
    "Ns 60.0000\n" +
    "\n" +
    "newmtl fldkgreen\n" +
    "Ka 0.0000 0.0122 0.0000\n" +
    "Kd 0.0058 0.0245 0.0000\n" +
    "illum 1\n" +
    "\n" +
    "newmtl flltbrown\n" +
    "Ka 0.1306 0.0538 0.0250\n" +
    "Kd 0.2776 0.1143 0.0531\n" +
    "illum 1\n" +
    "\n" +
    "newmtl tan\n" +
    "Ka 0.4000 0.3121 0.1202\n" +
    "Kd 0.6612 0.5221 0.2186\n" +
    "Ks 0.5020 0.4118 0.2152\n" +
    "illum 2\n" +
    "Ns 60.0000\n" +
    "\n" +
    "newmtl fltan\n" +
    "Ka 0.4000 0.3121 0.1202\n" +
    "Kd 0.6612 0.4567 0.1295\n" +
    "illum 1\n" +
    "\n" +
    "newmtl brzskin\n" +
    "Ka 0.4408 0.2694 0.1592\n" +
    "Kd 0.3796 0.2898 0.2122\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 25.0000\n" +
    "\n" +
    "newmtl lips\n" +
    "Ka 0.4408 0.2694 0.1592\n" +
    "Kd 0.9265 0.2612 0.2898\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 25.0000\n" +
    "\n" +
    "newmtl redorange\n" +
    "Ka 0.3918 0.0576 0.0000\n" +
    "Kd 0.7551 0.0185 0.0000\n" +
    "Ks 0.4694 0.3224 0.1667\n" +
    "illum 2\n" +
    "Ns 132.5600\n" +
    "\n" +
    "newmtl blutan\n" +
    "Ka 0.4408 0.2694 0.1592\n" +
    "Kd 0.0776 0.2571 0.2041\n" +
    "Ks 0.1467 0.1469 0.0965\n" +
    "illum 2\n" +
    "Ns 25.0000\n" +
    "\n" +
    "newmtl bluteal\n" +
    "Ka 0.0041 0.1123 0.1224\n" +
    "Kd 0.0776 0.2571 0.2041\n" +
    "Ks 0.1467 0.1469 0.0965\n" +
    "illum 2\n" +
    "Ns 25.0000\n" +
    "\n" +
    "newmtl pinktan\n" +
    "Ka 0.4408 0.2694 0.1592\n" +
    "Kd 0.6857 0.2571 0.2163\n" +
    "Ks 0.1467 0.1469 0.0965\n" +
    "illum 2\n" +
    "Ns 25.0000\n" +
    "\n" +
    "newmtl brnhair\n" +
    "Ka 0.0612 0.0174 0.0066\n" +
    "Kd 0.0898 0.0302 0.0110\n" +
    "Ks 0.1306 0.0819 0.0352\n" +
    "illum 2\n" +
    "Ns 60.4700\n" +
    "\n" +
    "newmtl blondhair\n" +
    "Ka 0.4449 0.2632 0.0509\n" +
    "Kd 0.5714 0.3283 0.0443\n" +
    "Ks 0.7755 0.4602 0.0918\n" +
    "illum 2\n" +
    "Ns 4.6500\n" +
    "\n" +
    "newmtl flblonde\n" +
    "Ka 0.4449 0.2632 0.0509\n" +
    "Kd 0.5714 0.3283 0.0443\n" +
    "illum 1\n" +
    "\n" +
    "newmtl yelloworng\n" +
    "Ka 0.5837 0.1715 0.0000\n" +
    "Kd 0.8857 0.2490 0.0000\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 60.0000\n" +
    "\n" +
    "newmtl bone\n" +
    "Ka 0.3061 0.1654 0.0650\n" +
    "Kd 0.9000 0.7626 0.4261\n" +
    "Ks 0.8939 0.7609 0.5509\n" +
    "illum 2\n" +
    "Ns 200.0000\n" +
    "\n" +
    "newmtl teeth\n" +
    "Ka 0.6408 0.5554 0.3845\n" +
    "Kd 0.9837 0.7959 0.4694\n" +
    "illum 1\n" +
    "\n" +
    "newmtl brass\n" +
    "Ka 0.2490 0.1102 0.0000\n" +
    "Kd 0.4776 0.1959 0.0000\n" +
    "Ks 0.5796 0.5796 0.5796\n" +
    "illum 2\n" +
    "Ns 134.8800\n" +
    "\n" +
    "newmtl dkred\n" +
    "Ka 0.0939 0.0000 0.0000\n" +
    "Kd 0.2286 0.0000 0.0000\n" +
    "Ks 0.2490 0.0000 0.0000\n" +
    "illum 2\n" +
    "Ns 60.0000\n" +
    "\n" +
    "newmtl taupe\n" +
    "Ka 0.1061 0.0709 0.0637\n" +
    "Kd 0.2041 0.1227 0.1058\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 84.5000\n" +
    "\n" +
    "newmtl dkteal\n" +
    "Ka 0.0000 0.0245 0.0163\n" +
    "Kd 0.0000 0.0653 0.0449\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 55.0400\n" +
    "\n" +
    "newmtl dkdkgrey\n" +
    "Ka 0.0000 0.0000 0.0000\n" +
    "Kd 0.0122 0.0122 0.0122\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 60.0000\n" +
    "\n" +
    "newmtl dkblue\n" +
    "Ka 0.0000 0.0029 0.0408\n" +
    "Kd 0.0000 0.0041 0.0571\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 60.0000\n" +
    "\n" +
    "newmtl gold\n" +
    "Ka 0.7224 0.1416 0.0000\n" +
    "Kd 1.0000 0.4898 0.0000\n" +
    "Ks 0.7184 0.3695 0.3695\n" +
    "illum 2\n" +
    "Ns 123.2600\n" +
    "\n" +
    "newmtl redbrick\n" +
    "Ka 0.1102 0.0067 0.0067\n" +
    "Kd 0.3306 0.0398 0.0081\n" +
    "illum 1\n" +
    "\n" +
    "newmtl flmustard\n" +
    "Ka 0.4245 0.2508 0.0000\n" +
    "Kd 0.8898 0.3531 0.0073\n" +
    "illum 1\n" +
    "\n" +
    "newmtl flpinegreen\n" +
    "Ka 0.0367 0.0612 0.0204\n" +
    "Kd 0.1061 0.2163 0.0857\n" +
    "illum 1\n" +
    "\n" +
    "newmtl fldkred\n" +
    "Ka 0.0939 0.0000 0.0000\n" +
    "Kd 0.2286 0.0082 0.0082\n" +
    "illum 1\n" +
    "\n" +
    "newmtl fldkgreen2\n" +
    "Ka 0.0025 0.0122 0.0014\n" +
    "Kd 0.0245 0.0694 0.0041\n" +
    "illum 1\n" +
    "\n" +
    "newmtl flmintgreen\n" +
    "Ka 0.0408 0.1429 0.0571\n" +
    "Kd 0.1306 0.2898 0.1673\n" +
    "illum 1\n" +
    "\n" +
    "newmtl olivegreen\n" +
    "Ka 0.0167 0.0245 0.0000\n" +
    "Kd 0.0250 0.0367 0.0000\n" +
    "Ks 0.2257 0.2776 0.1167\n" +
    "illum 2\n" +
    "Ns 97.6700\n" +
    "\n" +
    "newmtl skin\n" +
    "Ka 0.2286 0.0187 0.0187\n" +
    "Kd 0.1102 0.0328 0.0139\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 17.8300\n" +
    "\n" +
    "newmtl redbrown\n" +
    "Ka 0.1469 0.0031 0.0000\n" +
    "Kd 0.2816 0.0060 0.0000\n" +
    "Ks 0.3714 0.3714 0.3714\n" +
    "illum 2\n" +
    "Ns 141.0900\n" +
    "\n" +
    "newmtl deepgreen\n" +
    "Ka 0.0000 0.0050 0.0000\n" +
    "Kd 0.0000 0.0204 0.0050\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 113.1800\n" +
    "\n" +
    "newmtl flltolivegreen\n" +
    "Ka 0.0167 0.0245 0.0000\n" +
    "Kd 0.0393 0.0531 0.0100\n" +
    "illum 1\n" +
    "\n" +
    "newmtl jetflame\n" +
    "Ka 0.7714 0.0000 0.0000\n" +
    "Kd 0.9510 0.4939 0.0980\n" +
    "Ks 0.8531 0.5222 0.0000\n" +
    "illum 2\n" +
    "Ns 132.5600\n" +
    "\n" +
    "newmtl brownskn\n" +
    "Ka 0.0122 0.0041 0.0000\n" +
    "Kd 0.0204 0.0082 0.0000\n" +
    "Ks 0.0735 0.0508 0.0321\n" +
    "illum 2\n" +
    "Ns 20.1600\n" +
    "\n" +
    "newmtl greenskn\n" +
    "Ka 0.0816 0.0449 0.0000\n" +
    "Kd 0.0000 0.0735 0.0000\n" +
    "Ks 0.0490 0.1224 0.0898\n" +
    "illum 3\n" +
    "Ns 46.5100\n" +
    "sharpness 146.5100\n" +
    "\n" +
    "newmtl ltgrey\n" +
    "Ka 0.5000 0.5000 0.5000\n" +
    "Kd 0.3837 0.3837 0.3837\n" +
    "Ks 0.5000 0.5000 0.5000\n" +
    "illum 2\n" +
    "Ns 65.8900\n" +
    "\n" +
    "newmtl bronze\n" +
    "Ka 0.0449 0.0204 0.0000\n" +
    "Kd 0.0653 0.0367 0.0122\n" +
    "Ks 0.0776 0.0408 0.0000\n" +
    "illum 3\n" +
    "Ns 137.2100\n" +
    "sharpness 125.5800\n" +
    "\n" +
    "newmtl bone1\n" +
    "Ka 0.6408 0.5554 0.3845\n" +
    "Kd 0.9837 0.7959 0.4694\n" +
    "illum 1\n" +
    "\n" +
    "newmtl flwhite1\n" +
    "Ka 0.9306 0.9306 0.9306\n" +
    "Kd 1.0000 1.0000 1.0000\n" +
    "illum 1\n" +
    "\n" +
    "newmtl flwhite\n" +
    "Ka 0.6449 0.6116 0.5447\n" +
    "Kd 0.9837 0.9309 0.8392\n" +
    "Ks 0.8082 0.7290 0.5708\n" +
    "illum 2\n" +
    "Ns 200.0000\n" +
    "\n" +
    "newmtl shadow\n" +
    "Kd 0.0350 0.0248 0.0194\n" +
    "illum 0\n" +
    "d 0.2500\n" +
    "\n" +
    "newmtl fldkolivegreen\n" +
    "Ka 0.0056 0.0082 0.0000\n" +
    "Kd 0.0151 0.0204 0.0038\n" +
    "illum 1\n" +
    "\n" +
    "newmtl fldkdkgrey\n" +
    "Ka 0.0000 0.0000 0.0000\n" +
    "Kd 0.0122 0.0122 0.0122\n" +
    "illum 1\n" +
    "\n" +
    "newmtl lcdgreen\n" +
    "Ka 0.4000 0.4000 0.4000\n" +
    "Kd 0.5878 1.0000 0.5061\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 60.0000\n" +
    "\n" +
    "newmtl brownlips\n" +
    "Ka 0.1143 0.0694 0.0245\n" +
    "Kd 0.1429 0.0653 0.0408\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 25.0000\n" +
    "\n" +
    "newmtl muscle\n" +
    "Ka 0.2122 0.0077 0.0154\n" +
    "Kd 0.4204 0.0721 0.0856\n" +
    "Ks 0.1184 0.1184 0.1184\n" +
    "illum 2\n" +
    "Ns 25.5800\n" +
    "\n" +
    "newmtl flltgrey\n" +
    "Ka 0.5224 0.5224 0.5224\n" +
    "Kd 0.8245 0.8245 0.8245\n" +
    "illum 1\n" +
    "\n" +
    "newmtl offwhite.warm\n" +
    "Ka 0.5184 0.4501 0.3703\n" +
    "Kd 0.8367 0.6898 0.4490\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 60.0000\n" +
    "\n" +
    "newmtl offwhite.cool\n" +
    "Ka 0.5184 0.4501 0.3703\n" +
    "Kd 0.8367 0.6812 0.5703\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 60.0000\n" +
    "\n" +
    "newmtl yellowbrt\n" +
    "Ka 0.4000 0.4000 0.4000\n" +
    "Kd 1.0000 0.7837 0.0000\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 60.0000\n" +
    "\n" +
    "newmtl chappie\n" +
    "Ka 0.4000 0.4000 0.4000\n" +
    "Kd 0.5837 0.1796 0.0367\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 60.0000\n" +
    "\n" +
    "newmtl archwhite\n" +
    "Ka 0.2816 0.2816 0.2816\n" +
    "Kd 0.9959 0.9959 0.9959\n" +
    "illum 1\n" +
    "\n" +
    "newmtl archwhite2\n" +
    "Ka 0.2816 0.2816 0.2816\n" +
    "Kd 0.8408 0.8408 0.8408\n" +
    "illum 1\n" +
    "\n" +
    "newmtl lighttan\n" +
    "Ka 0.0980 0.0536 0.0220\n" +
    "Kd 0.7020 0.4210 0.2206\n" +
    "Ks 0.8286 0.8057 0.5851\n" +
    "illum 2\n" +
    "Ns 177.5200\n" +
    "\n" +
    "newmtl lighttan2\n" +
    "Ka 0.0980 0.0492 0.0144\n" +
    "Kd 0.3143 0.1870 0.0962\n" +
    "Ks 0.8286 0.8057 0.5851\n" +
    "illum 2\n" +
    "Ns 177.5200\n" +
    "\n" +
    "newmtl lighttan3\n" +
    "Ka 0.0980 0.0492 0.0144\n" +
    "Kd 0.1796 0.0829 0.0139\n" +
    "Ks 0.8286 0.8057 0.5851\n" +
    "illum 2\n" +
    "Ns 177.5200\n" +
    "\n" +
    "newmtl lightyellow\n" +
    "Ka 0.5061 0.1983 0.0000\n" +
    "Kd 1.0000 0.9542 0.3388\n" +
    "Ks 1.0000 0.9060 0.0000\n" +
    "illum 2\n" +
    "Ns 177.5200\n" +
    "\n" +
    "newmtl lighttannew\n" +
    "Ka 0.0980 0.0492 0.0144\n" +
    "Kd 0.7878 0.6070 0.3216\n" +
    "Ks 0.8286 0.8057 0.5851\n" +
    "illum 2\n" +
    "Ns 177.5200\n" +
    "\n" +
    "newmtl default\n" +
    "Ka 0.4000 0.4000 0.4000\n" +
    "Kd 0.7102 0.7020 0.6531\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 128.0000\n" +
    "\n" +
    "newmtl ship2\n" +
    "Ka 0.0000 0.0000 0.0000\n" +
    "Kd 1.0000 1.0000 1.0000\n" +
    "Ks 0.1143 0.1143 0.1143\n" +
    "illum 2\n" +
    "Ns 60.0000\n" +
    "\n" +
    "newmtl dkpurple\n" +
    "Ka 0.0082 0.0000 0.0163\n" +
    "Kd 0.0245 0.0000 0.0490\n" +
    "Ks 0.1266 0.0000 0.2531\n" +
    "illum 2\n" +
    "Ns 65.8900\n" +
    "\n" +
    "newmtl dkorange\n" +
    "Ka 0.4041 0.0123 0.0000\n" +
    "Kd 0.7143 0.0350 0.0000\n" +
    "Ks 0.7102 0.0870 0.0000\n" +
    "illum 2\n" +
    "Ns 65.8900\n" +
    "\n" +
    "newmtl mintgrn\n" +
    "Ka 0.0101 0.1959 0.0335\n" +
    "Kd 0.0245 0.4776 0.0816\n" +
    "Ks 0.0245 0.4776 0.0816\n" +
    "illum 2\n" +
    "Ns 65.8900\n" +
    "\n" +
    "newmtl fgreen\n" +
    "Ka 0.0000 0.0449 0.0000\n" +
    "Kd 0.0000 0.0449 0.0004\n" +
    "Ks 0.0062 0.0694 0.0000\n" +
    "illum 2\n" +
    "Ns 106.2000\n" +
    "\n" +
    "newmtl glassblutint\n" +
    "Ka 0.4000 0.4000 0.4000\n" +
    "Kd 0.5551 0.8000 0.7730\n" +
    "Ks 0.7969 0.9714 0.9223\n" +
    "illum 4\n" +
    "d 0.6700\n" +
    "Ns 60.0000\n" +
    "sharpness 60.0000\n" +
    "\n" +
    "newmtl bflesh\n" +
    "Ka 0.0122 0.0122 0.0122\n" +
    "Kd 0.0245 0.0081 0.0021\n" +
    "Ks 0.0531 0.0460 0.0153\n" +
    "illum 2\n" +
    "Ns 20.1600\n" +
    "\n" +
    "newmtl meh\n" +
    "Ka 0.4000 0.4000 0.4000\n" +
    "Kd 0.5551 0.8000 0.7730\n" +
    "Ks 0.7969 0.9714 0.9223\n" +
    "illum 4\n" +
    "d 0.2500\n" +
    "Ns 183.7200\n" +
    "sharpness 60.0000\n" +
    "\n" +
    "newmtl violet\n" +
    "Ka 0.0083 0.0000 0.1265\n" +
    "Kd 0.0287 0.0269 0.1347\n" +
    "Ks 0.2267 0.4537 0.6612\n" +
    "illum 2\n" +
    "Ns 96.9000\n" +
    "\n" +
    "newmtl iris\n" +
    "Ka 0.3061 0.0556 0.0037\n" +
    "Kd 0.0000 0.0572 0.3184\n" +
    "Ks 0.8041 0.6782 0.1477\n" +
    "illum 2\n" +
    "Ns 188.3700\n" +
    "\n" +
    "newmtl blugrn\n" +
    "Ka 0.4408 0.4144 0.1592\n" +
    "Kd 0.0811 0.6408 0.2775\n" +
    "Ks 0.1467 0.1469 0.0965\n" +
    "illum 2\n" +
    "Ns 25.0000\n" +
    "\n" +
    "newmtl glasstransparent\n" +
    "Ka 0.2163 0.2163 0.2163\n" +
    "Kd 0.4694 0.4694 0.4694\n" +
    "Ks 0.6082 0.6082 0.6082\n" +
    "illum 4\n" +
    "d 0.2500\n" +
    "Ns 200.0000\n" +
    "sharpness 60.0000\n" +
    "\n" +
    "newmtl fleshtransparent\n" +
    "Ka 0.4000 0.2253 0.2253\n" +
    "Kd 0.6898 0.2942 0.1295\n" +
    "Ks 0.7388 0.4614 0.4614\n" +
    "illum 4\n" +
    "d 0.2500\n" +
    "Ns 6.2000\n" +
    "sharpness 60.0000\n" +
    "\n" +
    "newmtl fldkgrey\n" +
    "Ka 0.0449 0.0449 0.0449\n" +
    "Kd 0.0939 0.0939 0.0939\n" +
    "illum 1\n" +
    "\n" +
    "newmtl sky_blue\n" +
    "Ka 0.1363 0.2264 0.4122\n" +
    "Kd 0.1241 0.5931 0.8000\n" +
    "Ks 0.0490 0.0490 0.0490\n" +
    "illum 2\n" +
    "Ns 13.9500\n" +
    "\n" +
    "newmtl fldkpurple\n" +
    "Ka 0.0443 0.0257 0.0776\n" +
    "Kd 0.1612 0.0000 0.3347\n" +
    "Ks 0.0000 0.0000 0.0000\n" +
    "illum 2\n" +
    "Ns 13.9500\n" +
    "\n" +
    "newmtl dkbrown\n" +
    "Ka 0.0143 0.0062 0.0027\n" +
    "Kd 0.0087 0.0038 0.0016\n" +
    "Ks 0.2370 0.2147 0.1821\n" +
    "illum 3\n" +
    "Ns 60.0000\n" +
    "sharpness 60.0000\n" +
    "\n" +
    "newmtl bone2\n" +
    "Ka 0.6408 0.5388 0.3348\n" +
    "Kd 0.9837 0.8620 0.6504\n" +
    "illum 1\n" +
    "\n" +
    "newmtl bluegrey\n" +
    "Ka 0.4000 0.4000 0.4000\n" +
    "Kd 0.1881 0.2786 0.2898\n" +
    "Ks 0.3000 0.3000 0.3000\n" +
    "illum 2\n" +
    "Ns 14.7300\n" +
    "\n" +
    "newmtl metal\n" +
    "Ka 0.9102 0.8956 0.1932\n" +
    "Kd 0.9000 0.7626 0.4261\n" +
    "Ks 0.8939 0.8840 0.8683\n" +
    "illum 2\n" +
    "Ns 200.0000\n" +
    "\n" +
    "newmtl sand_stone\n" +
    "Ka 0.1299 0.1177 0.0998\n" +
    "Kd 0.1256 0.1138 0.0965\n" +
    "Ks 0.2370 0.2147 0.1821\n" +
    "illum 3\n" +
    "Ns 60.0000\n" +
    "sharpness 60.0000\n" +
    "\n" +
    "newmtl hair\n" +
    "Ka 0.0013 0.0012 0.0010\n" +
    "Kd 0.0008 0.0007 0.0006\n" +
    "Ks 0.0000 0.0000 0.0000\n" +
    "illum 3\n" +
    "Ns 60.0000\n" +
    "sharpness 60.0000\n";
  private final static Map<String, Appearance> DEFAULT_APPEARANCES;
  
  static {
    try {
      DEFAULT_APPEARANCES = parseMaterialStream(new StringReader(JAVA_3D_MATERIALS), null);
    } catch (IOException ex) {
      // Can't happen because materials are read from a string
      throw new InternalError("Can't access to default materials");
    }
  }
  
  private List<Point3f>           vertices;
  private List<TexCoord2f>        textureCoodinates;
  private List<Vector3f>          normals;
  private Map<String, Group>      groups;
  private Group                   currentGroup;
  private String                  currentMaterial;
  private Map<String, Appearance> appearances;
  
  /**
   * Returns the scene described in the given OBJ file.
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
    try {
      return load(new InputStreamReader(new FileInputStream(file), "ISO-8859-1"), baseUrl);
    } catch (UnsupportedEncodingException ex) {
      // Shouldn't happen 
      return load(new InputStreamReader(new FileInputStream(file)));
    }
  }

  /**
   * Returns the scene described in the given OBJ file url.
   */
  public Scene load(URL url) throws FileNotFoundException, IncorrectFormatException, ParsingErrorException {
    URL baseUrl = this.baseUrl;
    if (this.baseUrl == null) {
      baseUrl = url;
    } 
    InputStream in;
    try {
      in = url.openStream();
    } catch (IOException ex) {
      throw new FileNotFoundException("Can't read " + url);
    }
    try {
      return load(new InputStreamReader(in, "ISO-8859-1"), baseUrl);
    } catch (UnsupportedEncodingException ex) {
      // Shouldn't happen 
      return load(new InputStreamReader(in));
    }
  }

  /**
   * Returns the scene described in the given OBJ file stream.
   */
  public Scene load(Reader reader) throws FileNotFoundException, IncorrectFormatException, ParsingErrorException {
    return load(reader, null);
  }

  /**
   * Returns the scene described in the given OBJ file.
   */
  private Scene load(Reader reader, URL baseUrl) throws FileNotFoundException {
    if (!(reader instanceof BufferedReader)) {
      reader = new BufferedReader(reader);
    }
    try {
      return parseObjectStream(reader, baseUrl);
    } catch (IOException ex) {
      throw new ParsingErrorException(ex.getMessage());
    } finally {
      try {
        reader.close();
      } catch (IOException ex) {
        throw new ParsingErrorException(ex.getMessage());
      }
    }
  }

  /**
   * Returns the scene parsed from a stream. 
   */
  private Scene parseObjectStream(Reader reader, 
                                  URL baseUrl) throws IOException {
    this.vertices = new ArrayList<Point3f>();
    this.textureCoodinates = new ArrayList<TexCoord2f>();
    this.normals = new ArrayList<Vector3f>();
    this.groups = new LinkedHashMap<String, Group>();
    this.currentGroup = new Group("default");
    this.groups.put("default", this.currentGroup);
    this.currentMaterial = "default";
    this.appearances = new HashMap<String, Appearance>(DEFAULT_APPEARANCES);

    StreamTokenizer tokenizer = createTokenizer(reader);
    while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
      switch (tokenizer.ttype) {
        case StreamTokenizer.TT_WORD :
          parseObjectLine(tokenizer, baseUrl);
          break;
        case StreamTokenizer.TT_EOL:
          break;
        default :
          throw new IncorrectFormatException("Unexpected token " + tokenizer.sval 
              + " at row " + tokenizer.lineno());
      }
    }
    
    try {
      return createScene();
    } finally {
      this.vertices = null;
      this.textureCoodinates = null;
      this.normals = null;
      this.groups = null;
      this.appearances = null;
    }
  }

  /**
   * Returns a new scene created from the parsed objects. 
   */
  private SceneBase createScene() {
    Point3f [] vertices = this.vertices.toArray(new Point3f [this.vertices.size()]);
    TexCoord2f [] textureCoodinates = 
        this.textureCoodinates.toArray(new TexCoord2f [this.textureCoodinates.size()]);
    Vector3f [] normals = this.normals.toArray(new Vector3f [this.normals.size()]);
  
    SceneBase scene = new SceneBase();
    BranchGroup sceneRoot = new BranchGroup();
    scene.setSceneGroup(sceneRoot);
    for (Group group : this.groups.values()) {
      List<Geometry> geometries = group.getGeometry();
      if (geometries != null
          && !geometries.isEmpty()) {
        int i = 0;
        while (i < geometries.size()) {
          Geometry firstGeometry = geometries.get(i);          
          boolean firstGeometryHasTextureCoordinateIndices = firstGeometry.hasTextureCoordinateIndices();
          boolean firstFaceHasNormalIndices = (firstGeometry instanceof Face) && ((Face)firstGeometry).hasNormalIndices();
          
          String firstGeometryMaterial = firstGeometry.getMaterial();
          Appearance appearance = getAppearance(firstGeometryMaterial);
          
          // Search how many geometries share the same characteristics 
          int max = i;
          while (++max < geometries.size()) {
            Geometry geometry = geometries.get(max);
            String material = geometry.getMaterial();
            if (geometry.getClass() != firstGeometry.getClass()
                || material == null && firstGeometryMaterial != null
                || material != null && getAppearance(material) != appearance
                || (firstGeometryHasTextureCoordinateIndices ^ geometry.hasTextureCoordinateIndices())
                || (firstFaceHasNormalIndices ^ ((firstGeometry instanceof Face) && ((Face)geometry).hasNormalIndices()))) {
              break;
            }
          }
          
          // Create indices arrays for the faces with an index between i and max
          int faceCount = max - i;
          int indexCount = 0;
          for (int j = 0; j < faceCount; j++) {
            indexCount += geometries.get(i + j).getVertexIndices().length;
          }
          int [] coordinatesIndices = new int [indexCount];
          int [] stripCounts = new int [faceCount]; 
          for (int j = 0, destIndex = 0; j < faceCount; j++) {
            int [] geometryVertexIndices = geometries.get(i + j).getVertexIndices();
            System.arraycopy(geometryVertexIndices, 0, coordinatesIndices, destIndex, geometryVertexIndices.length);
            stripCounts [j] = geometryVertexIndices.length;
            destIndex += geometryVertexIndices.length;
          }

          int [] textureCoordinateIndices = null;
          if (firstGeometryHasTextureCoordinateIndices) {
            textureCoordinateIndices = new int [indexCount];
            for (int j = 0, destIndex = 0; j < faceCount; j++) {
              int [] geometryTextureCoordinateIndices = geometries.get(i + j).getTextureCoordinateIndices();
              System.arraycopy(geometryTextureCoordinateIndices, 0, textureCoordinateIndices, destIndex, geometryTextureCoordinateIndices.length);
              destIndex += geometryTextureCoordinateIndices.length;
            }
          } 

          GeometryArray geometryArray;
          if (firstGeometry instanceof Face) {
            GeometryInfo geometryInfo = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
            geometryInfo.setCoordinates(vertices);
            geometryInfo.setCoordinateIndices(coordinatesIndices);
            geometryInfo.setStripCounts(stripCounts);
            
            if (firstGeometryHasTextureCoordinateIndices) {
              geometryInfo.setTextureCoordinateParams(1, 2);
              geometryInfo.setTextureCoordinates(0, textureCoodinates);
              geometryInfo.setTextureCoordinateIndices(0, textureCoordinateIndices);
            } 
            
            if (firstFaceHasNormalIndices) {
              int [] normalIndices = new int [indexCount];
              for (int j = 0, destIndex = 0; j < faceCount; j++) {
                int [] faceNormalIndices = ((Face)geometries.get(i + j)).getNormalIndices();
                System.arraycopy(faceNormalIndices, 0, normalIndices, destIndex, faceNormalIndices.length);
                destIndex += faceNormalIndices.length;
              }
              geometryInfo.setNormals(normals);
              geometryInfo.setNormalIndices(normalIndices);
            } else {
              NormalGenerator normalGenerator = new NormalGenerator(Math.PI / 2);
              if (!group.isSmooth()) {
                normalGenerator.setCreaseAngle(0);
              }
              normalGenerator.generateNormals(geometryInfo);
            }
            geometryArray = geometryInfo.getGeometryArray(true, true, false);
          } else { // Line
            int format = IndexedGeometryArray.COORDINATES;
            if (firstGeometryHasTextureCoordinateIndices) {
              format |= IndexedGeometryArray.TEXTURE_COORDINATE_2;
            }
            
            // Use non indexed line array to avoid referencing the whole vertices
            geometryArray = new LineStripArray(coordinatesIndices.length, format, stripCounts);            
            for (int j = 0; j < coordinatesIndices.length; j++) {
              geometryArray.setCoordinate(j, vertices [coordinatesIndices [j]]);
            }
            if (firstGeometryHasTextureCoordinateIndices) {
              for (int j = 0; j < coordinatesIndices.length; j++) {
                geometryArray.setTextureCoordinate(0, j, textureCoodinates [textureCoordinateIndices [j]]);
              }
            }
          }
          
          // Clone appearance to avoid sharing it
          if (appearance != null) {
            appearance = (Appearance)appearance.cloneNodeComponent(false);
            // Create texture coordinates if geometry doesn't define its own coordinates 
            // and appearance contains a texture 
            if (!firstGeometryHasTextureCoordinateIndices
                && appearance.getTexture() != null) {
              appearance.setTexCoordGeneration(new TexCoordGeneration());
            }
          }
          Shape3D shape = new Shape3D(geometryArray, appearance);   
          sceneRoot.addChild(shape);
          scene.addNamedObject(group.getName() + (i == 0 ? "" : String.valueOf(i)), shape);
          
          i = max;
        }
      }
    }
    return scene;
  }

  /**
   * Returns the appearance matching a given <code>material</code>. 
   */
  private Appearance getAppearance(String material) {
    Appearance appearance = null;
    if (material != null) {
      appearance = this.appearances.get(material);
    }
    if (appearance == null) {
      appearance = DEFAULT_APPEARANCES.get("default");
    }
    return appearance;
  }

  /**
   * Parses the line starting with a word.
   */
  private void parseObjectLine(StreamTokenizer tokenizer, 
                               URL baseUrl) throws IOException {
    if ("v".equals(tokenizer.sval)) {
      // Read vertex v x y z
      this.vertices.add(new Point3f(
          parseNumber(tokenizer), parseNumber(tokenizer), parseNumber(tokenizer)));
      // Skip next number if it exists
      if (tokenizer.nextToken() == StreamTokenizer.TT_EOL) {
        tokenizer.pushBack();
      }
    } else if ("vn".equals(tokenizer.sval)) {
      // Read normal vn x y z
      this.normals.add(new Vector3f(
          parseNumber(tokenizer), parseNumber(tokenizer), parseNumber(tokenizer)));      
    } else if ("vt".equals(tokenizer.sval)) {
      // Read texture coordinate vt x y 
      //                       or vt x y z
      this.textureCoodinates.add(new TexCoord2f(
          parseNumber(tokenizer), parseNumber(tokenizer)));    
      // Skip next number if it exists
      if (tokenizer.nextToken() == StreamTokenizer.TT_EOL) {
        tokenizer.pushBack();
      }
    } else if ("l".equals(tokenizer.sval)) {
      tokenizer.ordinaryChar('/');
      // Read line l v       v       v       ...
      //        or l v/vt    v/vt    v/vt    ...
      List<Integer> vertexIndices = new ArrayList<Integer>(2);
      List<Integer> textureCoordinateIndices = new ArrayList<Integer>(2); 
      while (tokenizer.nextToken() != StreamTokenizer.TT_EOL) {      
        tokenizer.pushBack();        
        // Read vertex index
        int vertexIndex = parseInteger(tokenizer) - 1;
        if (vertexIndex < 0) {
          vertexIndex += this.vertices.size() + 1;
        }
        vertexIndices.add(vertexIndex);
        
        if (tokenizer.nextToken() != '/') {
          // l v  
          tokenizer.pushBack();        
        } else {
          // l v/vt : read texture coordinate index 
          int textureCoordinateIndex = parseInteger(tokenizer) - 1;
          if (textureCoordinateIndex < 0) {
            textureCoordinateIndex += this.textureCoodinates.size() + 1;
          }          
          textureCoordinateIndices.add(textureCoordinateIndex);
        }
      }
      tokenizer.pushBack();     
      tokenizer.wordChars('/', '/');
      if (textureCoordinateIndices.size() != 0
          && textureCoordinateIndices.size() != vertexIndices.size()) {
        // Ignore unconsistent texture coordinate 
        textureCoordinateIndices.clear();
      } 
      if (vertexIndices.size() > 1) {
        this.currentGroup.addGeometry(new Line(vertexIndices, textureCoordinateIndices, 
            this.currentMaterial));
      }
    } else if ("f".equals(tokenizer.sval)) {
      tokenizer.ordinaryChar('/');
      // Read face f v       v       v       ...
      //        or f v//vn   v//vn   v//vn   ...
      //        or f v/vt    v/vt    v/vt    ...
      //        or f v/vt/vn v/vt/vn v/vt/vn ...
      List<Integer> vertexIndices = new ArrayList<Integer>(4);
      List<Integer> textureCoordinateIndices = new ArrayList<Integer>(4); 
      List<Integer> normalIndices = new ArrayList<Integer>(4);
      while (tokenizer.nextToken() != StreamTokenizer.TT_EOL) {      
        tokenizer.pushBack();        
        // Read vertex index
        int vertexIndex = parseInteger(tokenizer) - 1;
        if (vertexIndex < 0) {
          vertexIndex += this.vertices.size() + 1;
        }
        vertexIndices.add(vertexIndex);
        
        if (tokenizer.nextToken() != '/') {
          // f v  
          tokenizer.pushBack();        
        } else {
          if (tokenizer.nextToken() != '/') {
            // f v/vt : read texture coordinate index 
            tokenizer.pushBack();        
            int textureCoordinateIndex = parseInteger(tokenizer) - 1;
            if (textureCoordinateIndex < 0) {
              textureCoordinateIndex += this.textureCoodinates.size() + 1;
            }          
            textureCoordinateIndices.add(textureCoordinateIndex);
            tokenizer.nextToken();
          }
          if (tokenizer.ttype == '/') {
            //    f v//vn 
            // or f v/vt/vn : read normal index
            int normalIndex = parseInteger(tokenizer) - 1;
            if (normalIndex < 0) {
              normalIndex += this.normals.size() + 1;
            }
            normalIndices.add(normalIndex);
          } else {
            tokenizer.pushBack();
          }
        }
      }
      tokenizer.pushBack();     
      tokenizer.wordChars('/', '/');
      if (textureCoordinateIndices.size() != 0
          && textureCoordinateIndices.size() != vertexIndices.size()) {
        // Ignore unconsistent texture coordinate 
        textureCoordinateIndices.clear();
      } 
      if (normalIndices.size() != 0
          && normalIndices.size() != vertexIndices.size()) {
        // Ignore unconsistent normals 
        normalIndices.clear();
      }
      if (vertexIndices.size() > 2) {
        this.currentGroup.addGeometry(new Face(vertexIndices, textureCoordinateIndices, normalIndices, 
            this.currentMaterial));
      }
    } else if ("g".equals(tokenizer.sval)
               || "o".equals(tokenizer.sval)) {
      // Read group name g name 
      //  or object name o name
      boolean smoothingGroup = this.currentGroup.isSmooth();
      if (tokenizer.nextToken() == StreamTokenizer.TT_WORD) {
        this.currentGroup = this.groups.get(tokenizer.sval);
        if (this.currentGroup == null) {
          this.currentGroup = new Group(tokenizer.sval);
          this.groups.put(this.currentGroup.getName(), this.currentGroup);
        }
      } else if (tokenizer.ttype == StreamTokenizer.TT_EOL) {
        // Use default group
        this.currentGroup = this.groups.get("default");
        tokenizer.pushBack();
      } else {  
        throw new IncorrectFormatException("Expected group or object name at line " + tokenizer.lineno());
      }
      this.currentGroup.setSmooth(smoothingGroup);
      // Skip other names
      while (tokenizer.nextToken() == StreamTokenizer.TT_WORD) {        
      }
      tokenizer.pushBack();     
    } else if ("s".equals(tokenizer.sval)) {
      // Read smoothing group s n 
      //                   or s off
      if (tokenizer.nextToken() == StreamTokenizer.TT_WORD) {
        this.currentGroup.setSmooth(!"off".equals(tokenizer.sval));
      } else {
        throw new IncorrectFormatException("Expected smoothing group or off at line " + tokenizer.lineno());
      }
    } else if ("usemtl".equals(tokenizer.sval)) {
      // Read the material name usemtl name
      if (tokenizer.nextToken() == StreamTokenizer.TT_WORD) {
        this.currentMaterial = tokenizer.sval;
      } else {
        throw new IncorrectFormatException("Expected material name at line " + tokenizer.lineno());
      }
    } else if ("mtllib".equals(tokenizer.sval)) {
      int libCount = 0;
      do {
        if (tokenizer.nextToken() == StreamTokenizer.TT_WORD) {
          parseMaterial(tokenizer.sval, baseUrl);
          libCount++;
        }
      }
      while (tokenizer.nextToken() != StreamTokenizer.TT_EOL);        
      if (libCount == 0) {
        throw new IncorrectFormatException("Expected material library at line " + tokenizer.lineno());
      }
      tokenizer.pushBack();
    } else {
      // Skip other lines (including comment lines starting by #)
      int token;
      do {
        token = tokenizer.nextToken();
      } while (token != StreamTokenizer.TT_EOL && token != StreamTokenizer.TT_EOF);
      tokenizer.pushBack();     
    }
    
    int token = tokenizer.nextToken();
    if (token != StreamTokenizer.TT_EOL && token != StreamTokenizer.TT_EOF) {
      throw new IncorrectFormatException("Expected end of line at line " + tokenizer.lineno());
    }
  }
  
  /**
   * Returns a new tokenizer for an OBJ or MTL stream.
   */
  private static StreamTokenizer createTokenizer(Reader reader) {
    StreamTokenizer tokenizer = new StreamTokenizer(reader);
    tokenizer.resetSyntax();
    tokenizer.eolIsSignificant(true);

    // All printable ASCII characters
    tokenizer.wordChars('!', '~');
    // Let's tolerate other ISO-8859-1 characters
    tokenizer.wordChars(0x80, 0xFF);

    tokenizer.whitespaceChars(' ', ' ');
    tokenizer.whitespaceChars('\n', '\n');
    tokenizer.whitespaceChars('\r', '\r');
    tokenizer.whitespaceChars('\t', '\t');
    return tokenizer;
  }

  /**
   * Returns the integer contained in the next token. 
   */
  private static int parseInteger(StreamTokenizer tokenizer) throws IOException {
    if (tokenizer.nextToken() != StreamTokenizer.TT_WORD) {
      throw new IncorrectFormatException("Expected an integer at line " + tokenizer.lineno());
    } else {
      try {
        return Integer.parseInt(tokenizer.sval);
      } catch (NumberFormatException ex) {
        throw new IncorrectFormatException("Found " + tokenizer.sval + 
            " instead of an integer at line " + tokenizer.lineno()); 
      }
    }
  }

  /**
   * Returns the number contained in the next token. 
   */
  private static float parseNumber(StreamTokenizer tokenizer) throws IOException {
    if (tokenizer.nextToken() != StreamTokenizer.TT_WORD) {
      throw new IncorrectFormatException("Expected a number at line " + tokenizer.lineno());
    } else {
      try {
        return Float.parseFloat(tokenizer.sval);
      } catch (NumberFormatException ex) {
        throw new IncorrectFormatException("Found " + tokenizer.sval + 
            " instead of a number at line " + tokenizer.lineno()); 
      }
    }
  }

  /**
   * Parses appearances from the given material file.
   */
  private void parseMaterial(String file, URL baseUrl) {
    InputStream in = null;
    try {
      if (baseUrl != null) {
        in = new URL(baseUrl, file).openStream();
      } else {
        in = new FileInputStream(file);
      }
    } catch (IOException ex) {
      // Ignore material files not found
    }

    if (in != null) {
      try {        
        this.appearances.putAll(parseMaterialStream(
            new BufferedReader(new InputStreamReader(in, "ISO-8859-1")), baseUrl));
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
  }

  /**
   * Parses a map of appearances parsed from the given stream. 
   */
  private static Map<String, Appearance> parseMaterialStream(Reader reader, 
                                                             URL baseUrl) throws IOException {
    Map<String, Appearance> appearances = new HashMap<String, Appearance>();
    Appearance              currentAppearance = null;    
    StreamTokenizer tokenizer = createTokenizer(reader);
    while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
      switch (tokenizer.ttype) {
        case StreamTokenizer.TT_WORD :
          currentAppearance = parseMaterialLine(tokenizer, 
              appearances, currentAppearance, baseUrl);
          break;
        case StreamTokenizer.TT_EOL:
          break;
        default :
          throw new IncorrectFormatException("Unexpected token " + tokenizer.sval 
              + " at row " + tokenizer.lineno());
      }
    }
    return appearances;
  }
  
  /**
   * Parses the line starting with a word describing a material.
   */
  private static Appearance parseMaterialLine(StreamTokenizer tokenizer, 
                                              Map<String, Appearance> appearances,
                                              Appearance currentAppearance,
                                              URL baseUrl) throws IOException {
    if ("newmtl".equals(tokenizer.sval)) {
      // Read material name newmtl name
      if (tokenizer.nextToken() == StreamTokenizer.TT_WORD) {
        currentAppearance = new Appearance();
        appearances.put(tokenizer.sval, currentAppearance);
      } else {
        throw new IncorrectFormatException("Expected material name at line " + tokenizer.lineno());
      }
    } else if ("Ka".equals(tokenizer.sval)) {
      // Read ambient color Ka r g b
      Color3f ambiantColor = new Color3f(parseNumber(tokenizer), 
          parseNumber(tokenizer), parseNumber(tokenizer));
      if (currentAppearance != null) {
        Material material = getMaterial(currentAppearance);
        material.setAmbientColor(ambiantColor);
      }
    } else if ("Kd".equals(tokenizer.sval)) {
      // Read diffuse or emissive color Kd r g b
      Color3f diffuseColor = new Color3f(parseNumber(tokenizer), 
          parseNumber(tokenizer), parseNumber(tokenizer));
      if (currentAppearance != null) {
        OBJMaterial material = getMaterial(currentAppearance);
        material.setDiffuseColor(diffuseColor);
        currentAppearance.setColoringAttributes(
            new ColoringAttributes(diffuseColor, ColoringAttributes.SHADE_GOURAUD));
      }
    } else if ("Ks".equals(tokenizer.sval)) {
      // Read specular color Ks r g b
      Color3f specularColor = new Color3f(parseNumber(tokenizer), 
          parseNumber(tokenizer), parseNumber(tokenizer));
      if (currentAppearance != null) {
        OBJMaterial material = getMaterial(currentAppearance);
        if (!material.isIlluminationModelSet() 
            || material.getIlluminationModel() >= 2) {
          material.setSpecularColor(specularColor);
        } else {
          material.setSpecularColor(0, 0, 0);
        }
      }
    } else if ("Ns".equals(tokenizer.sval)) {
      // Read shininess Ns val  with 0 <= val <= 1000
      float shininess = parseNumber(tokenizer);
      if (currentAppearance != null) {
        OBJMaterial material = getMaterial(currentAppearance);
        if (!material.isIlluminationModelSet() 
            || material.getIlluminationModel() >= 2) {
          // Use shininess at a max value equal to 128
          material.setShininess(Math.max(1f, Math.min(shininess, 128f)));
        } else {
          material.setShininess(1f);
        }
      }
    } else if ("Ni".equals(tokenizer.sval)) {
      // Read optical density Ni val  
      float opticalDensity = parseNumber(tokenizer);
      if (currentAppearance != null) {
        OBJMaterial material = getMaterial(currentAppearance);
        material.setOpticalDensity(opticalDensity);
      }
    } else if ("sharpness".equals(tokenizer.sval)) {
      // Read sharpness sharpness val  
      float sharpness = parseNumber(tokenizer);
      if (currentAppearance != null) {
        OBJMaterial material = getMaterial(currentAppearance);
        material.setSharpness(sharpness);
      }
    } else if ("d".equals(tokenizer.sval)) {
      // Read transparency d val  with 0 <= val <= 1
      if (tokenizer.nextToken() == StreamTokenizer.TT_WORD) {
        if ("-halo".equals(tokenizer.sval)) {
          // Ignore halo transparency
          parseNumber(tokenizer);
        } else {
          tokenizer.pushBack();     
          float transparency = parseNumber(tokenizer);
          if (currentAppearance != null) {
            if (transparency >= 1) {
              currentAppearance.setTransparencyAttributes(null);
            } else {
              currentAppearance.setTransparencyAttributes(new TransparencyAttributes(
                  TransparencyAttributes.NICEST, 1f - Math.max(0f, transparency)));
            }
          }
        }
      } else {
        throw new IncorrectFormatException("Expected transparency factor at line " + tokenizer.lineno());
      }
    } else if ("illum".equals(tokenizer.sval)) {
      // Read illumination setting illum n
      int illumination = parseInteger(tokenizer);
      if (currentAppearance != null) {
        OBJMaterial material = getMaterial(currentAppearance);
        material.setIlluminationModel(illumination);
        material.setLightingEnable(illumination >= 1);
        if (illumination <= 1) {
          material.setSpecularColor(0, 0, 0);
          material.setShininess(1f);
        }
      }
    } else if ("map_Kd".equals(tokenizer.sval)) {
      // Read material texture map_Kd name
      // Search last parameter that matches image file name
      String imageFileName = null;
      while (tokenizer.nextToken() != StreamTokenizer.TT_EOL) {
        if (tokenizer.ttype == StreamTokenizer.TT_WORD) {
          imageFileName = tokenizer.sval;
        }
      }
      
      if (imageFileName != null) {
        URL textureImageUrl = baseUrl != null
            ? new URL(baseUrl, imageFileName)
            : new File(imageFileName).toURI().toURL();
        BufferedImage textureImage = null;
        try {
          textureImage = ImageIO.read(textureImageUrl);
        } catch (IOException ex) {
          // Ignore images at other format
        }
        if (textureImage != null) {
          TextureLoader textureLoader = new TextureLoader(textureImage);
          Texture texture = textureLoader.getTexture();
          // Keep in user data the URL of the texture image
          texture.setUserData(textureImageUrl);
          currentAppearance.setTexture(texture);
        }
      } else {
        throw new IncorrectFormatException("Expected image file name at line " + tokenizer.lineno());
      }
      tokenizer.pushBack();     
    } else {
      int token;
      do {
        token = tokenizer.nextToken();
      } while (token != StreamTokenizer.TT_EOL && token != StreamTokenizer.TT_EOF);
      tokenizer.pushBack();     
    }
    
    int token = tokenizer.nextToken();
    if (token != StreamTokenizer.TT_EOL && token != StreamTokenizer.TT_EOF) {
      throw new IncorrectFormatException("Expected end of line at line " + tokenizer.lineno());
    }
    
    return currentAppearance;
  }

  /**
   * Returns the material stored in the given <code>appearance</code>.
   */
  private static OBJMaterial getMaterial(Appearance appearance) {
    OBJMaterial material = (OBJMaterial)appearance.getMaterial();
    if (material == null) {
      material = new OBJMaterial();
      appearance.setMaterial(material);
    }
    return material;
  }

  /**
   * The coordinates indices of a geometry. 
   */
  private static abstract class Geometry {
    private int [] vertexIndices;
    private int [] textureCoordinateIndices;
    private String material;
    
    public Geometry(List<Integer> vertexIndices, 
                    List<Integer> textureCoordinateIndices, 
                    String        material) {
      this.vertexIndices = new int [vertexIndices.size()];
      for (int i = 0; i < this.vertexIndices.length; i++) {
        this.vertexIndices [i] = vertexIndices.get(i);
      }
      if (textureCoordinateIndices.size() != 0) {
        this.textureCoordinateIndices = new int [textureCoordinateIndices.size()];
        for (int i = 0; i < this.textureCoordinateIndices.length; i++) {
          this.textureCoordinateIndices [i] = textureCoordinateIndices.get(i);
        }
      }
      this.material = material;
    }
    
    public int [] getVertexIndices() {
      return this.vertexIndices;
    }
    
    public int [] getTextureCoordinateIndices() {
      return this.textureCoordinateIndices;
    }
    
    public boolean hasTextureCoordinateIndices() {
      return this.textureCoordinateIndices != null 
          && this.textureCoordinateIndices.length > 0;
    }
    
    public String getMaterial() {
      return this.material;
    }
  }
  
  /**
   * The coordinates indices of a line. 
   */
  private static class Line extends Geometry {
    public Line(List<Integer> vertexIndices, 
                List<Integer> textureCoordinateIndices, 
                String        material) {
      super(vertexIndices, textureCoordinateIndices, material);
    }
  }
  
  /**
   * The coordinates indices of a face. 
   */
  private static class Face extends Geometry {
    private int [] normalIndices;
    
    public Face(List<Integer> vertexIndices, 
                List<Integer> textureCoordinateIndices, 
                List<Integer> normalIndices,
                String        material) {
      super(vertexIndices, textureCoordinateIndices, material);
      if (normalIndices.size() != 0) {
        this.normalIndices = new int [normalIndices.size()];
        for (int i = 0; i < this.normalIndices.length; i++) {
          this.normalIndices [i] = normalIndices.get(i);
        }
      }
    }
    
    public int [] getNormalIndices() {
      return this.normalIndices;
    }
    
    public boolean hasNormalIndices() {
      return this.normalIndices != null
          && this.normalIndices.length > 0;
    }
  }
  
  /**
   * A named group of faces. 
   */
  private static class Group {
    private final String   name;
    private boolean        smooth;
    private List<Geometry> faces;
 
    public Group(String name) {
      this.name = name;
      this.faces = new ArrayList<Geometry>();
    }
    
    public String getName() {
      return this.name;
    }
    
    public void setSmooth(boolean smooth) {
      this.smooth = smooth;
    }
    
    public boolean isSmooth() {
      return this.smooth;
    }
    
    public void addGeometry(Geometry face) {
      this.faces.add(face);
    }
    
    public List<Geometry> getGeometry() {
      return this.faces;
    }
  }
}
