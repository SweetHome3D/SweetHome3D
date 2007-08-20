/*
 * BrowserManager.java 20 août 07
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
package com.eteks.sweethome3d.swing;

import java.net.URL;

import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;


/**
 * Singleton managing browser display.
 * @author Emmanuel Puybaret
 */
public class BrowserManager {
  private static BrowserManager instance;
  private BasicService          basicService; 
  
  private BrowserManager() {    
    // This class is a singleton
    try { 
      // Lookup the javax.jnlp.BasicService object
      this.basicService =
          (BasicService)ServiceManager.lookup("javax.jnlp.BasicService");
    } catch (UnavailableServiceException ex) {
      // Too bad : service is unavailable
    } 
  }
  
  /**
   * Returns an instance of this singleton. 
   */
  public static BrowserManager getInstance() {
    if (instance == null) {
      instance = new BrowserManager();
    }
    return instance;
  }

  /**
   * Displays the given <code>url</code> in default browser. 
   */
  public boolean viewURL(URL url) {
    if (this.basicService == null) {
      return false;
    }
    if ("http".equals(url.getProtocol())
        && this.basicService.isOffline()) {
        
    }
    return this.basicService.showDocument(url); 
  }

  /**
   * Returns <code>true</code> if a default online browser is available.
   */
  public boolean isOnlineBrowserAvailable() {
    return this.basicService != null
        && this.basicService.isWebBrowserSupported()
        && !this.basicService.isOffline();
  }
}
