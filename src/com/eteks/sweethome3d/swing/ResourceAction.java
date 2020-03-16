/*
 * ResourceAction.java 8 juil. 2006
 *
 * Sweet Home 3D, Copyright (c) 2006 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
import java.awt.event.ActionListener;
import java.awt.im.InputContext;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.event.SwingPropertyChangeSupport;

import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;

/**
 * An action with properties read from a resource bundle file.
 * @author Emmanuel Puybaret
 */
public class ResourceAction extends AbstractAction {
  public static final String RESOURCE_CLASS = "ResourceClass";
  public static final String RESOURCE_PREFIX = "ResourcePrefix";

  public static final String VISIBLE = "Visible";
  public static final String POPUP   = "Popup";
  public static final String TOGGLE_BUTTON_MODEL = "ToggleButtonModel";
  public static final String TOOL_BAR_ICON = "ToolBarIcon";

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
    putValue(RESOURCE_CLASS, resourceClass);
    putValue(RESOURCE_PREFIX, actionPrefix);
    putValue(VISIBLE, Boolean.TRUE);

    readActionProperties(preferences, resourceClass, actionPrefix);
    setEnabled(enabled);

    preferences.addPropertyChangeListener(UserPreferences.Property.LANGUAGE,
        new LanguageChangeListener(this));
  }

  /**
   * Preferences property listener bound to this action with a weak reference to avoid
   * strong link between preferences and this action.
   */
  private static class LanguageChangeListener implements PropertyChangeListener {
    private final WeakReference<ResourceAction> resourceAction;

    public LanguageChangeListener(ResourceAction resourceAction) {
      this.resourceAction = new WeakReference<ResourceAction>(resourceAction);
    }

    public void propertyChange(PropertyChangeEvent ev) {
      // If action was garbage collected, remove this listener from preferences
      ResourceAction resourceAction = this.resourceAction.get();
      if (resourceAction == null) {
        ((UserPreferences)ev.getSource()).removePropertyChangeListener(
            UserPreferences.Property.LANGUAGE, this);
      } else {
        resourceAction.readActionProperties((UserPreferences)ev.getSource(),
            (Class<?>)resourceAction.getValue(RESOURCE_CLASS), (String)resourceAction.getValue(RESOURCE_PREFIX));
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
    putValue(NAME, getOptionalString(preferences, resourceClass, propertyPrefix + NAME, true));
    putValue(DEFAULT, getValue(NAME));
    putValue(POPUP, getOptionalString(preferences, resourceClass, propertyPrefix + POPUP, true));

    putValue(SHORT_DESCRIPTION,
        getOptionalString(preferences, resourceClass, propertyPrefix + SHORT_DESCRIPTION, false));
    putValue(LONG_DESCRIPTION,
        getOptionalString(preferences, resourceClass, propertyPrefix + LONG_DESCRIPTION, false));

    String smallIcon = getOptionalString(preferences, resourceClass, propertyPrefix + SMALL_ICON, false);
    if (smallIcon != null) {
      putValue(SMALL_ICON, SwingTools.getScaledImageIcon(resourceClass.getResource(smallIcon)));
    }

    String toolBarIcon = getOptionalString(preferences, resourceClass, propertyPrefix + TOOL_BAR_ICON, false);
    if (toolBarIcon != null) {
      putValue(TOOL_BAR_ICON, SwingTools.getScaledImageIcon(resourceClass.getResource(toolBarIcon)));
    }

    String propertyKey = propertyPrefix + ACCELERATOR_KEY;
    // Search first if there's a key for this OS
    String acceleratorKey = getOptionalString(preferences,
        resourceClass, propertyKey + "." + System.getProperty("os.name"), false);
    if (acceleratorKey == null) {
      // Then search default value
      acceleratorKey = getOptionalString(preferences, resourceClass, propertyKey, false);
    }
    if (acceleratorKey !=  null) {
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(acceleratorKey));
    }

    String mnemonicKey = getOptionalString(preferences, resourceClass, propertyPrefix + MNEMONIC_KEY, false);
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
                                   String propertyKey,
                                   boolean label) {
    try {
      String localizedText = label
          ? SwingTools.getLocalizedLabelText(preferences, resourceClass, propertyKey)
          : preferences.getLocalizedString(resourceClass, propertyKey);
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

  private static final String [] LATIN_AND_SUPPORTED_LOCALES = new String [] {"cs", "da", "de", "en", "es", "et", "fi", "fr", "hr", "hu", "it", "ja", "lt", "lv", "nl", "no", "pl", "pt", "ro", "sk", "sl", "sv", "tr", "vi"};
  private static KeyStroke previousActionAccelerator;
  private static Timer     doubleEventsTimer;

  /**
   * Returns <code>true</code> if the given <code>action</action> is valid to avoid it to be
   * fired twice for the same user command.
   */
  static boolean isActionValid(Action action) {
    if (OperatingSystem.isMacOSX()
        && OperatingSystem.isJavaVersionBetween("1.7", "9")) {
      Locale inputLocale = InputContext.getInstance().getLocale();
      if (inputLocale != null
          && Arrays.binarySearch(LATIN_AND_SUPPORTED_LOCALES, inputLocale.getLanguage()) < 0) {
        // Accelerators used with non latin keyboards provokes two events,
        // the second event being emitted by the menu item management
        if (!isInvokedFromMenuItem()) {
          previousActionAccelerator = (KeyStroke)action.getValue(ACCELERATOR_KEY);
          // Cancel double event tracker in 1s in case the user provokes events
          // by selecting the toolbar button then the menu item matching the accelerator
          if (doubleEventsTimer == null) {
            doubleEventsTimer = new Timer(1000, new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                  previousActionAccelerator = null;
                  doubleEventsTimer.stop();
                }
              });
          }
          doubleEventsTimer.restart();
        } else if (previousActionAccelerator != null
                   && previousActionAccelerator.equals(action.getValue(ACCELERATOR_KEY))) {
          previousActionAccelerator = null;
          return false; // The second event is doubled
        }
      }
    }
    return true;
  }

  /**
   * Returns <code>true</code> if current call is done from a menu item.
   */
  private static boolean isInvokedFromMenuItem() {
    for (StackTraceElement stackElement : Thread.currentThread().getStackTrace()) {
      if ("com.apple.laf.ScreenMenuItem".equals(stackElement.getClassName())
          && "actionPerformed".equals(stackElement.getMethodName())) {
        return true;
      }
    }
    return false;
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
      if (isActionValid(this)) {
        this.action.actionPerformed(ev);
      }
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
      if (key.equals(SMALL_ICON)) {
        Object toolBarIcon = super.getValue(TOOL_BAR_ICON);
        if (toolBarIcon != null) {
          return toolBarIcon;
        }
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
