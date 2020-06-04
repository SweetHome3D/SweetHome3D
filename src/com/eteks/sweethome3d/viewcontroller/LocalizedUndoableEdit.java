/*
 * LocalizedUndoableEdit.java 3 juin 2020
 *
 * Sweet Home 3D, Copyright (c) 2020 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import javax.swing.undo.AbstractUndoableEdit;

import com.eteks.sweethome3d.model.UserPreferences;

/**
 * An undoable edit able with a localized presentation name.
 * @author Emmanuel Puybaret
 * @since 6.4
 */
class LocalizedUndoableEdit extends AbstractUndoableEdit {
  private final UserPreferences             preferences;
  private final Class<? extends Controller> controllerClass;
  private final String                      presentationNameKey;

  public LocalizedUndoableEdit(UserPreferences preferences,
                               Class<? extends Controller>  controllerClass,
                               String presentationNameKey) {
    this.preferences = preferences;
    this.controllerClass = controllerClass;
    this.presentationNameKey = presentationNameKey;
  }

  @Override
  public String getPresentationName() {
    return preferences.getLocalizedString(controllerClass, this.presentationNameKey);
  }
}

