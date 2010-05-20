/*
 * SweetHome3DJavaWebStartBootstrap 20 mai 2010
 *
 * Copyright (c) 2010 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.jnlp.DownloadService;
import javax.jnlp.DownloadServiceListener;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;

/**
 * This bootstrap class loads lazy resource parts specified in the JNLP file
 * with the System property <code>com.eteks.sweethome3d.lazyParts</code>, 
 * then launches Sweet Home 3D application class.
 * @author Emmanuel Puybaret
 */
public class SweetHome3DJavaWebStartBootstrap {
  public static void main(String [] args) throws ClassNotFoundException, 
        NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    try { 
      // Lookup the javax.jnlp.DownloadService object 
      DownloadService service = (DownloadService)ServiceManager.lookup("javax.jnlp.DownloadService");
      DownloadServiceListener progressWindow = service.getDefaultProgressWindow();
      // Search jars lazily downloaded 
      String lazyParts = System.getProperty("com.eteks.sweethome3d.lazyParts", "");
      List<String> lazyPartsToDownload = new ArrayList<String>(); 
      for (String lazyPart : lazyParts.split("\\s")) {
        if (!service.isPartCached(lazyPart)) {
          lazyPartsToDownload.add(lazyPart);
        }
      }      
      try {
        if (lazyPartsToDownload.size() > 0) {
          service.loadPart(lazyPartsToDownload.toArray(new String [lazyPartsToDownload.size()]), progressWindow);
        }
      } catch (IOException ex) {
        ex.printStackTrace();
        System.exit(1);
      }        
    } catch (UnavailableServiceException ex) {
      // Sweet Home 3D isn't launched from Java Web Start
    }
    
    // Call application class main method with reflection
    String applicationClassName = "com.eteks.sweethome3d.SweetHome3DBootstrap";
    Class<?> applicationClass = Class.forName(applicationClassName);
    Method applicationClassMain = 
        applicationClass.getMethod("main", Array.newInstance(String.class, 0).getClass());
    applicationClassMain.invoke(null, new Object [] {args});
  }
}