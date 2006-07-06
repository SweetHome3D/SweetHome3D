/*
 * ViewFactory.java 29 mai 2006
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
package com.eteks.sweethome3d.viewcontroller;

import com.eteks.sweethome3d.model.Catalog;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.Plan;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * A factory for view objects.
 * @author Emmanuel Puybaret
 */
public interface ViewFactory {
  /**
   * Creates a home view.
   */
  HomeView createHomeView(Home home, UserPreferences preferences, HomeController controller);
}
