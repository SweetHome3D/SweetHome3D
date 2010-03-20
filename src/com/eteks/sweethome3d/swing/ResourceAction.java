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
import java.lang.ref.WeakReference;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import javax.swing.event.SwingPropertyChangeSupport;

import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;

/**
 * An action with properties read from a resource bundle file.
 * @author Emmanuel Puybaret
 */
public class ResourceAction extends AbstractAction {
  public static final String POPUP = "Popup";
    
  /**
   * Creates a disabled action with properties retrieved from a resource bundle 
   * in which key starts with <code>actionPrefix</code>.
   * @param preferences   user preferences used to retrieve localized properties of the action 
   * @param resourceClass the class used as a context to retrieve localized properties of the action
   * @param actionPrefix  prefix used in resource bundle to search action properties
   */
  public ResourceAction(UserPreferences preferences, 
                        Class<?> resourceClass, 
                        String actionPrefix) {
    this(preferences, resourceClass, actionPrefix, false);
  }
  
  /**
   * Creates an action with properties retrieved from a resource bundle 
   * in which key starts with <code>actionPrefix</code>.
   * @param preferences   user preferences used to retrieve localized description of the action
   * @param resourceClass the class used as a context to retrieve localized properties of the action
   * @param actionPrefix  prefix used in resource bundle to search action properties
   * @param enabled <code>true</code> if the action should be enabled at creation.
   */
  public ResourceAction(UserPreferences preferences, 
                        Class<?> resourceClass, 
                        String actionPrefix, 
                        boolean enabled) {
    readActionProperties(preferences, resourceClass, actionPrefix);    
    setEnabled(enabled);
    
    preferences.addPropertyChangeListener(UserPreferences.Property.LANGUAGE, 
        new LanguageChangeListener(this, resourceClass, actionPrefix));
  }
    
  /**
   * Preferences property listener bound to this action with a weak reference to avoid
   * strong link between preferences and this action.  
   */
  private static class LanguageChangeListener implements PropertyChangeListener {
    private final WeakReference<ResourceAction> resourceAction;
    private final Class<?>                      resourceClass;
    private final String                        actionPrefix;

    public LanguageChangeListener(ResourceAction resourceAction,
                                  Class<?> resourceClass,
                                  String actionPrefix) {
      this.resourceAction = new WeakReference<ResourceAction>(resourceAction);
      this.resourceClass = resourceClass;
      this.actionPrefix = actionPrefix;
    }

    public void propertyChange(PropertyChangeEvent ev) {
      // If action was garbage collected, remove this listener from preferences
      ResourceAction resourceAction = this.resourceAction.get();
      if (resourceAction == null) {
        ((UserPreferences)ev.getSource()).removePropertyChangeListener(
            UserPreferences.Property.LANGUAGE, this);
      } else {
        resourceAction.readActionProperties((UserPreferences)ev.getSource(), 
            this.resourceClass, this.actionPrefix);
      }
    }
  }
  
  /**
   * Reads from the properties of this action.
   */
  private void readActionProperties(UserPreferences preferences, 
                                    Class<?> resourceClass, 
                                    String actionPrefix) {
    String propertyPrefix = actionPrefix + ".";
    try {
      putValue(NAME, SwingTools.getLocalizedLabelText(preferences, resourceClass, propertyPrefix + NAME));
    } catch (IllegalArgumentException ex) {
      // Ignore unknown resource
    }
    putValue(DEFAULT, getValue(NAME));
    putValue(POPUP, getOptionalString(preferences, resourceClass, propertyPrefix + POPUP));
    
    putValue(SHORT_DESCRIPTION, 
        getOptionalString(preferences, resourceClass, propertyPrefix + SHORT_DESCRIPTION));
    putValue(LONG_DESCRIPTION, 
        getOptionalString(preferences, resourceClass, propertyPrefix + LONG_DESCRIPTION));
    
    String smallIcon = getOptionalString(preferences, resourceClass, propertyPrefix + SMALL_ICON);
    if (smallIcon != null) {
      putValue(SMALL_ICON, new ImageIcon(resourceClass.getResource(smallIcon)));
    }

    String propertyKey = propertyPrefix + ACCELERATOR_KEY;
    // Search first if there's a key for this OS
    String acceleratorKey = getOptionalString(preferences, 
        resourceClass, propertyKey + "." + System.getProperty("os.name"));
    if (acceleratorKey == null) {
      // Then search default value
      acceleratorKey = getOptionalString(preferences, resourceClass, propertyKey);
    }
    if (acceleratorKey !=  null) {
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(acceleratorKey));
    }
    
    String mnemonicKey = getOptionalString(preferences, resourceClass, propertyPrefix + MNEMONIC_KEY);
    if (mnemonicKey != null) {
      putValue(MNEMONIC_KEY, Integer.valueOf(KeyStroke.getKeyStroke(mnemonicKey).getKeyCode()));
    }
  }

  /**
   * Returns the value of <code>propertyKey</code> in <code>preferences</code>, 
   * or <code>null</code> if the property doesn't exist.
   */
  private String getOptionalString(UserPreferences preferences, 
                                   Class<?> resourceClass, 
                                   String propertyKey) {
    try {
      String localizedText = preferences.getLocalizedString(resourceClass, propertyKey);
      if (localizedText != null && localizedText.length() > 0) {
        return localizedText;
      } else {
        return null;
      }
    } catch (IllegalArgumentException ex) {
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
    
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
      this.propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
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
      // Add a listener on POPUP value changes because the value of the 
      // POPUP key replaces the one matching NAME if it exists       
      addPropertyChangeListener(new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            if (POPUP.equals(ev.getPropertyName())
                && (ev.getOldValue() != null || ev.getNewValue() != null)) {
              firePropertyChange(NAME, ev.getOldValue(), ev.getNewValue());
            }
          }
        });
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

  /**
   * An action decorator for  buttons.  
   */
  public static class ButtonAction extends AbstractDecoratedAction {
    public ButtonAction(Action action) {
      super(action);
    }

    public Object getValue(String key) {
      // Avoid mnemonics in Mac OS X menus
      if (OperatingSystem.isMacOSX()
          && key.equals(MNEMONIC_KEY)) {
        return null;
      }
      return super.getValue(key);
    }
  }
}
