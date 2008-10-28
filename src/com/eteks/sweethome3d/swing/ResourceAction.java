/*
 * ResourceAction.java 8 juil. 2006
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import javax.swing.event.SwingPropertyChangeSupport;

import com.eteks.sweethome3d.tools.OperatingSystem;

/**
 * An action with properties read from a resource bundle file.
 * @author Emmanuel Puybaret
 */
public class ResourceAction extends AbstractAction {
  public static final String POPUP = "Popup";
  
  private final String actionPrefix;
  
  /**
   * Creates a disabled action with properties retrieved from a resource bundle 
   * in which key starts with <code>actionPrefix</code>.
   * @param resource a resource bundle
   * @param actionPrefix  prefix used in resource bundle to search action properties
   */
  public ResourceAction(ResourceBundle resource, String actionPrefix) {
    this(resource, actionPrefix, false);
  }
  
  /**
   * Creates an action with properties retrieved from a resource bundle 
   * in which key starts with <code>actionPrefix</code>.
   * @param resource a resource bundle
   * @param actionPrefix  prefix used in resource bundle to search action properties
   * @param enabled <code>true</code> if the action should be enabled at creation.
   */
  public ResourceAction(ResourceBundle resource, String actionPrefix, boolean enabled) {
    this.actionPrefix = actionPrefix;
    readActionProperties(resource, actionPrefix);    
    setEnabled(enabled);
  }
    
  /**
   * Changes the resource from which properties action are read.
   */
  public void setResource(ResourceBundle resource) {
    readActionProperties(resource, this.actionPrefix);
  }

  /**
   * Reads from <code>resource</code> bundle the properties of this action.
   */
  private void readActionProperties(ResourceBundle resource, String actionPrefix) {
    String propertyPrefix = actionPrefix + ".";
    putValue(NAME, getOptionalString(resource, propertyPrefix + NAME));
    putValue(DEFAULT, getValue(NAME));
    putValue(POPUP, getOptionalString(resource, propertyPrefix + POPUP));
    
    putValue(SHORT_DESCRIPTION, 
        getOptionalString(resource, propertyPrefix + SHORT_DESCRIPTION));
    putValue(LONG_DESCRIPTION, 
        getOptionalString(resource, propertyPrefix + LONG_DESCRIPTION));
    
    String smallIcon = getOptionalString(resource, propertyPrefix + SMALL_ICON);
    if (smallIcon != null) {
      putValue(SMALL_ICON, new ImageIcon(getClass().getResource(smallIcon)));
    }

    String propertyKey = propertyPrefix + ACCELERATOR_KEY;
    // Search first if there's a key for this OS
    String acceleratorKey = getOptionalString(resource, 
        propertyKey + "." + System.getProperty("os.name"));
    if (acceleratorKey == null) {
      // Then search default value
      acceleratorKey = getOptionalString(resource, propertyKey);
    }
    if (acceleratorKey !=  null) {
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(acceleratorKey));
    }
    
    String mnemonicKey = getOptionalString(resource, propertyPrefix + MNEMONIC_KEY);
    if (mnemonicKey != null) {
      putValue(MNEMONIC_KEY, Integer.valueOf(KeyStroke.getKeyStroke(mnemonicKey).getKeyCode()));
    }
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
   * Unsupported operation. Subclasses should override this method if they want
   * to associate a real action to this class.
   */
  public void actionPerformed(ActionEvent ev) {
    throw new UnsupportedOperationException();
  }
  
  /**
   * An action decorator.  
   */
  private static class AbstractDecoratedAction implements Action {
    private Action action;
    private SwingPropertyChangeSupport propertyChangeSupport;

    public AbstractDecoratedAction(Action action) {
      this.action = action;
      this.propertyChangeSupport = new SwingPropertyChangeSupport(this);
      action.addPropertyChangeListener(new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            String propertyName = ev.getPropertyName();
            if ("enabled".equals(propertyName)) {
              propertyChangeSupport.firePropertyChange(ev);
            } else {
              Object newValue = getValue(propertyName);
              // In case a property value changes, fire the new value decorated in subclasses
              // unless new value is null (most Swing listeners don't check new value is null !)
              if (newValue != null) {
                propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(ev.getSource(), 
                    propertyName, ev.getOldValue(), newValue));
              }
            }
          }
        });
    }

    public final void actionPerformed(ActionEvent ev) {
      this.action.actionPerformed(ev);
    }

    public final void addPropertyChangeListener(PropertyChangeListener listener) {
      this.propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public Object getValue(String key) {
      return this.action.getValue(key);
    }

    public final boolean isEnabled() {
      return this.action.isEnabled();
    }

    public final void putValue(String key, Object value) {
      this.action.putValue(key, value);
    }

    public final void removePropertyChangeListener(PropertyChangeListener listener) {
      this.propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public final void setEnabled(boolean enabled) {
      this.action.setEnabled(enabled);
    }
  }
  
  /**
   * An action decorator for menu items.  
   */
  public static class MenuItemAction extends AbstractDecoratedAction {
    public MenuItemAction(Action action) {
      super(action);
    }

    public Object getValue(String key) {
      // Avoid mnemonics, tooltips and icons in Mac OS X menus
      if (OperatingSystem.isMacOSX()
          && (key.equals(MNEMONIC_KEY)
              || key.equals(SMALL_ICON)
              || key.equals(SHORT_DESCRIPTION))) {
        return null;
      }
      return super.getValue(key);
    }
  }
  
  /**
   * An action decorator for popup menu items.  
   */
  public static class PopupMenuItemAction extends MenuItemAction {
    public PopupMenuItemAction(Action action) {
      super(action);
    }

    public Object getValue(String key) {
      // If it exists, return POPUP key value if NAME key is required 
      if (key.equals(NAME)) {
        Object value = super.getValue(POPUP);
        if (value != null) {
          return value;
        }
      } else if (key.equals(SMALL_ICON)) {
        // Avoid icons in popus
        return null;
      } else if (OperatingSystem.isMacOSX()
                 && key.equals(ACCELERATOR_KEY)) {
        // Avoid accelerators in Mac OS X popups
        return null;
      }
      return super.getValue(key);
    }
  }

  /**
   * An action decorator for tool bar buttons.  
   */
  public static class ToolBarAction extends AbstractDecoratedAction {
    public ToolBarAction(Action action) {
      super(action);
    }

    public Object getValue(String key) {
      // Ignore NAME in tool bar 
      if (key.equals(NAME)) {        
        return null;
      } 
      return super.getValue(key);
    }
  }
}
