/*
 * ResourceAction.java 10 aout 2006
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
package com.eteks.sweethome3d.jface;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * An action with properties read from a resource bundle file.
 * @author Emmanuel Puybaret
 */
public class ResourceAction extends Action {
  private String defaultText;
  private String defaultToolTipText;
  
  /**
   * Creates an action with properties retrieved from a resource bundle 
   * in which key starts with <code>actionPrefix</code>.
   * @param resource a resource bundle
   * @param actionPrefix  prefix used in resource bundle to search action properties
   */
  public ResourceAction(ResourceBundle resource, String actionPrefix) {
    String propertyPrefix = actionPrefix + ".";
    this.defaultText = resource.getString(propertyPrefix + "Name");
    setText(this.defaultText); // Mnemonic is included in defaultText
        
    this.defaultToolTipText = getOptionalString(resource, propertyPrefix + "ShortDescription");
    if (this.defaultToolTipText != null) {
      setToolTipText(this.defaultToolTipText);
    }
    
    String description = getOptionalString(resource, propertyPrefix + "LongDescription");
    if (description != null) {
      setDescription(description);
    }
    
    String imageDescriptor = getOptionalString(resource, propertyPrefix + "SmallIcon");
    if (imageDescriptor != null) {
      setImageDescriptor(ImageDescriptor.createFromURL(getClass().getResource(imageDescriptor)));
    }

    String propertyKey = propertyPrefix + "AcceleratorKey";
    // Search first if there's a key for this OS
    String acceleratorKey = getOptionalString(resource, 
        propertyKey + "." + System.getProperty("os.name"));
    if (acceleratorKey == null) {
      // Then search default value
      acceleratorKey = getOptionalString(resource, propertyKey);
    }
    if (acceleratorKey !=  null) {
      setAccelerator(convertAccelerator(acceleratorKey));
    }
    
    setEnabled(false);
  }

  /**
   * Returns the default text of this action read from properties. 
   */
  public String getDefaultText() {
    return this.defaultText;
  }

  /**
   * Returns the default tool tip text of this action read from properties. 
   */
  public String getDefaultToolTipText() {
    return this.defaultToolTipText;
  }

  /**
   * Returns the value of <code>propertyKey</code> in <code>resource</code>, 
   * or <code>null</code> if the property doesn't exist.
   */
  private String getOptionalString(ResourceBundle resource, String propertyKey) {
    try {
      return resource.getString(propertyKey);
    } catch (MissingResourceException ex) {
      return null;
    }
  }
}
