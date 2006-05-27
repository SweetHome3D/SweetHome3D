/*
 * ResourceAction.java 27 mai 2006
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

import java.awt.event.ActionEvent;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

/**
 * An action configured from a resource file.
 * @author Emmanuel Puybaret
 */
public class ResourceAction extends AbstractAction {
  /**
   * Creates an action with properties retrieved from resource bundle 
   * of prefix <code>view.getClass().getName()</code> in which key starts with 
   * <code>action</code>.
   * @param action prefix used in resource bundle to search action properties
   * @param view   view used as file base for resource bundle
   */
  public ResourceAction(String action, JComponent view) {
    ResourceBundle resource = ResourceBundle.getBundle(view.getClass().getName());
    putValue(NAME, resource.getString(action + "." + NAME));
    String shortDescription = getOptionalString(resource, action + "." + SHORT_DESCRIPTION);
    if (shortDescription != null) {
      putValue(SHORT_DESCRIPTION, shortDescription);
    }
    String longDescription = getOptionalString(resource, action + "." + LONG_DESCRIPTION);
    if (longDescription != null) {
      putValue(LONG_DESCRIPTION, longDescription);
    }
    Icon smallIcon = getOptionalIcon(resource, action + "." + SMALL_ICON);
    if (smallIcon != null) {
      putValue(SMALL_ICON, smallIcon);
    }
    KeyStroke acceleratorKey = getOptionalKeyStroke(resource, action + "." + ACCELERATOR_KEY);
    if (acceleratorKey !=  null) {
      putValue(ACCELERATOR_KEY, acceleratorKey);
    }
    Integer mnemonicKey = getOptionalKeyEvent(resource, action + "." + MNEMONIC_KEY);
    if (mnemonicKey != null) {
      putValue(MNEMONIC_KEY, mnemonicKey);
    }
    
    // Find a way to use different KeyStroke on Mac OS and Windows (for example Command + Q or Alt + F4 to quit)
    // Take care of Command/Ctrl key on various systems with Toolkit#getMenuShortcutKeyMask
  }

  /**
   * Returns the value of <code>key</code> in <code>resource</code>, 
   * or <code>null</code> if the property doesn't exist.
   */
  private String getOptionalString(ResourceBundle resource, String key) {
    try {
      return resource.getString(key);
    } catch (MissingResourceException ex) {
      return null;
    }
  }

  /**
   * Returns an <code>Icon</code> object matching the value of <code>key</code>
   * in <code>resource</code>, or <code>null</code> if the property doesn't exist.
   */
  private Icon getOptionalIcon(ResourceBundle resource, String key) {
    String iconUrl = getOptionalString(resource, key);
    if (iconUrl == null) {
      return null;
    } else {
      return new ImageIcon(getClass().getResource(iconUrl));
    }
  }

  /**
   * Returns a <code>KeyStroke</code> object matching the value of <code>key</code>
   * in <code>resource</code>, or <code>null</code> if the property doesn't exist.
   */
  private KeyStroke getOptionalKeyStroke(ResourceBundle resource, String key) {
    // Search first if there's a key for this OS
    String propertyValue = getOptionalString(resource, 
        key + "." + System.getProperty("os.name"));
    if (propertyValue == null) {
      // Then search default value
      propertyValue = getOptionalString(resource, key);
    }
    if (propertyValue == null) {
      return null;
    } else {
      return KeyStroke.getKeyStroke(propertyValue);
    } 
  }

  /**
   * Returns an <code>Integer</code> object matching the value of <code>key</code>
   * in <code>resource</code>, or <code>null</code> if the property doesn't exist.
   */
  private Integer getOptionalKeyEvent(ResourceBundle resource, String key) {
    String propertyValue = getOptionalString(resource, key);
    if (propertyValue != null) {
      return null;
    } else {
      return Integer.valueOf(propertyValue.charAt(0));
    }
  }

  /**
   * Unsupported operation. Subclasses should override this method if they want
   * to assiciate a real action to this class.
   */
  public void actionPerformed(ActionEvent e) {
    throw new UnsupportedOperationException();
  }
}
