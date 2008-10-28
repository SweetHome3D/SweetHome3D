/*
 * TextureChoiceView.java 28 oct 2008
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
package com.eteks.sweethome3d.viewcontroller;

/**
 * A view that edits the texture of its controller.
 * @author Emmanuel Puybaret
 */
public interface TextureChoiceView extends View {
  /**
   * Displays a dialog that let user choose whether he wants to delete 
   * the selected texture from catalog or not.
   * @return <code>true</code> if user confirmed to delete.
   */
  public abstract boolean confirmDeleteSelectedCatalogTexture();

}