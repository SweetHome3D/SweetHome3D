/*
 * PluginAction.java 26 oct. 2008
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
package com.eteks.sweethome3d.plugin;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.eteks.sweethome3d.tools.ResourceURLContent;

/**
 * An action made available to application users through a plugin.
 * @author Emmanuel Puybaret
 */
public abstract class PluginAction {
  /**
   * Enumeration of the various properties this action may define.
   */
  public enum Property {
    /** 
     * The key of the property of <code>String</code> type that specifies
     * the name of an action, used for a menu or button.
     */
    NAME,
    /**
     * The key of the property of <code>String</code> type that specifies
     * a short description of an action, used for tool tip text.
     */
    SHORT_DESCRIPTION,
    /**
     * The key of the property of <code>Content</code> type that specifies
     * an image content of an action, used for tool bar buttons.  
     */
    SMALL_ICON,
    /**
     * The key of the property of <code>Character</code> type that specifies 
     * the ASCII character used as the mnemonic of an action.
     */
    MNEMONIC,
    /**
     * The key of the property of <code>Boolean</code> type that specifies
     * if an action will appear in the main tool bar.
     */
    TOOL_BAR,
    /**
     * The key of the property of <code>String</code> type that specifies
     * in which menu of the main menu bar an action should appear. 
     */
    MENU,
    /**
     * The key of the property of <code>Boolean</code> type that specifies
     * if an action is enabled or not.
     */
    ENABLED,
  }
  
  private final Map<Property, Object> propertyValues        = new HashMap<Property, Object>();
  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
  
  /**
   * Creates a disabled plug-in action.
   */
  public PluginAction() {
  }

  /**
   * Creates a disabled plug-in action with properties retrieved from a resource bundle 
   * in which key starts with <code>actionPrefix</code>.
   * <br>For example, a plug-in action created by the call
   * <code>new PluginAction("com.mycompany.mypackage.MyResources", "EXPORT", plugin.getPluginClassLoader())</code> 
   * will retrieve its property values from the <code>/com/mycompany/mypackage/MyResources.properties</code> file
   * bundled with the plug-in class, and this file may describe the action <code>EXPORT</code> 
   * with the following keys: 
   * <pre> EXPORT.NAME=Export
   * EXPORT.SHORT_DESCRIPTION=Export home data
   * EXPORT.SMALL_ICON=/com/mycompany/mypackage/resources/export.png
   * EXPORT.MNEMONIC=X
   * EXPORT.IN_TOOL_BAR=true
   * EXPORT.MENU=File</pre>
   * @param resourceBaseName the base name of a resource bundle  
   * @param actionPrefix  prefix used in resource bundle to search action properties
   * @param pluginClassLoader the class loader that will be used to search the resource bundle   
   * @throws MissingResourceException if no resource bundle could be found from 
   *    <code>resourceBaseName</code>.
   */
  public PluginAction(String resourceBaseName,
                      String actionPrefix,
                      ClassLoader pluginClassLoader) {
    this(resourceBaseName, actionPrefix, pluginClassLoader, false);
  }
  
  /**
   * Creates an action with properties retrieved from a resource bundle 
   * in which key starts with <code>actionPrefix</code>.
   * @param resourceBaseName the base name of a resource bundle
   * @param actionPrefix  prefix used in resource bundle to search action properties
   * @param pluginClassLoader the class loader that will be used to search the resource bundle   
   * @param enabled <code>true</code> if the action should be enabled at creation.
   * @throws MissingResourceException if no resource bundle could be found from 
   *    <code>resourceBaseName</code>.
   */
  public PluginAction(String resourceBaseName, 
                      String actionPrefix,
                      ClassLoader pluginClassLoader, 
                      boolean enabled) {
    readActionPropertyValues(resourceBaseName, actionPrefix, pluginClassLoader);    
    setEnabled(enabled);
  }
    
  /**
   * Reads the properties of this action from a resource bundle of given base name.
   * @throws MissingResourceException if no resource bundle could be found from 
   *    <code>resourceBaseName</code>.
   */
  private void readActionPropertyValues(String resourceBaseName, 
                                        String actionPrefix, 
                                        ClassLoader pluginClassLoader) {
    ResourceBundle resource;
    if (pluginClassLoader != null) {
      resource = ResourceBundle.getBundle(resourceBaseName, Locale.getDefault(), 
        pluginClassLoader);
    } else {
      resource = ResourceBundle.getBundle(resourceBaseName, Locale.getDefault());
    }
    String propertyPrefix = actionPrefix + ".";
    putPropertyValue(Property.NAME, 
        getOptionalString(resource, propertyPrefix + Property.NAME));
    putPropertyValue(Property.SHORT_DESCRIPTION, 
        getOptionalString(resource, propertyPrefix + Property.SHORT_DESCRIPTION));
    String smallIcon = getOptionalString(resource, propertyPrefix + Property.SMALL_ICON);
    if (smallIcon != null) {
      if (smallIcon.startsWith("/")) {
        smallIcon = smallIcon.substring(1);
      }
      putPropertyValue(Property.SMALL_ICON, 
          new ResourceURLContent(pluginClassLoader, smallIcon));
    }
    String mnemonicKey = getOptionalString(resource, propertyPrefix + Property.MNEMONIC);
    if (mnemonicKey != null) {
      putPropertyValue(Property.MNEMONIC, Character.valueOf(mnemonicKey.charAt(0)));
    }
    String toolBar = getOptionalString(resource, propertyPrefix + Property.TOOL_BAR);
    if (toolBar != null) {
      putPropertyValue(Property.TOOL_BAR, Boolean.valueOf(toolBar));
    }
    putPropertyValue(Property.MENU, 
        getOptionalString(resource, propertyPrefix + Property.MENU));
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
  
  /**
   * Adds the property change <code>listener</code> in parameter to this plugin action.
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    this.propertyChangeSupport.addPropertyChangeListener(listener);
  }

  /**
   * Removes the property change <code>listener</code> in parameter from this plugin action.
   */
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    this.propertyChangeSupport.removePropertyChangeListener(listener);
  }
  
  /**
   * Returns a property value of this action.
   */
  public Object getPropertyValue(Property property) {
    return this.propertyValues.get(property);
  }
  
  /**
   * Sets a property value of this action, and fires a <code>PropertyChangeEvent</code>
   * if the value changed. 
   */
  public void putPropertyValue(Property property, Object value) {
    Object oldValue = this.propertyValues.get(property);
    if (value != oldValue
        || (value != null && !value.equals(oldValue))) {
      this.propertyValues.put(property, value);
      this.propertyChangeSupport.firePropertyChange(property.name(), oldValue, value);
    }
    
  }

  /**
   * Sets the enabled state of this action. When enabled, any menu item or tool bar button 
   * associated with this object is enabled and able to call the <code>execute</code> method.
   * If the value has changed, a <code>PropertyChangeEvent</code> is sent
   * to listeners. By default, an action is disabled.
   */
  public void setEnabled(boolean enabled) {
    putPropertyValue(Property.ENABLED, enabled);
  }
  
  /**
   * Returns the enabled state of this action. 
   * @return <code>true</code> if this action is enabled.
   */
  public boolean isEnabled() {
    Boolean enabled = (Boolean)getPropertyValue(Property.ENABLED);
    return enabled != null && enabled.booleanValue();
  }
  
  /**
   * Executes this action. This method will be called by application when the user
   * wants to execute this action.
   */
  public abstract void execute();
}
