/*
 * SweetHome3DBootstrap.java 2 sept. 07
 *
 * Copyright (c) 2007 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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
package com.eteks.sweethome3d;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;

/**
 * This bootstrap class loads Sweet Home 3D Jar executable application classes from 
 * extension jars stored as resources.
 * @author Emmanuel Puybaret
 */
public class SweetHome3DJarBootstrap {
  public static void main(String [] args) throws MalformedURLException, IllegalAccessException, 
        InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
    String [] extensionFiles = {
        "j3dcore.jar", // Main Java 3D jars
        "vecmath.jar",
        "j3dutils.jar",
        "j3dcore-d3d.dll", // Windows DLLs
        "j3dcore-ogl.dll",
        "j3dcore-ogl-cg.dll",
        "j3dcore-ogl-chk.dll",
        "libj3dcore-ogl.so", // Linux DLLs
        "libj3dcore-ogl-cg.so",
        "gluegen-rt.jar", // Mac OS X jars and DLLs
        "jogl.jar",
        "libgluegen-rt.jnilib",
        "libjogl.jnilib",
        "libjogl_awt.jnilib",
        "libjogl_cg.jnilib",
        "iText-2.1.2u.jar", // Other jars 
        "Loader3DS1_2.jar",
        "jnlp.jar"};
    SweetHome3DBootstrap.run(args, extensionFiles);
  }
}