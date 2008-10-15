/*
 * HomeAppletController.java 11 Oct. 2008
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
package com.eteks.sweethome3d.applet;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;

import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeApplication;
import com.eteks.sweethome3d.swing.HomeController;

/**
 * Home applet pane controller.
 * @author Emmanuel Puybaret
 */
public class HomeAppletController extends HomeController {
  public HomeAppletController(Home home, 
                              HomeApplication application) {
    super(home, application);
  }
  
  /**
   * Creates a new home after saving and deleting the current home.
   */
  @Override
  public void newHome() {
    close(new Runnable() {
        public void run() {
          HomeAppletController.super.newHome();
        }
      });
  }

  /**
   * Opens a home after saving and deleting the current home.
   */
  @Override
  public void open() {
    close(new Runnable() {
      public void run() {
        HomeAppletController.super.open();
      }
    });
  }
  
  /**
   * Displays Sweet Home user guide in a navigator window.
   */
  @Override
  public void help() {
    try { 
      // Lookup the javax.jnlp.BasicService object 
      final BasicService service = (BasicService)ServiceManager.lookup("javax.jnlp.BasicService");
      String helpIndex = ResourceBundle.getBundle(HomeAppletController.class.getName()).getString("helpIndex");
      service.showDocument(new URL(helpIndex)); 
    } catch (UnavailableServiceException ex) {
      // Too bad : service is unavailable             
    } catch (MalformedURLException ex) {
      ex.printStackTrace();
    } 
  }
}

