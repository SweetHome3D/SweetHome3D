/*
 * NullableCheckBox.java 7 nov. 2008
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
package com.eteks.sweethome3d.swing;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A check box that accepts <code>null</code> values. Thus this check box is able to
 * display 3 states : <code>null</code>, <code>false</code> and <code>true</code>.
 * @author Emmanuel Puybaret
 */
public class NullableCheckBox extends JComponent {    
  /** 
   * Identifies a change in the check box text. 
   */
  public static final String TEXT_CHANGED_PROPERTY = "text";
  /** 
   * Identifies a change in the check box mnemonic. 
   */
  public static final String MNEMONIC_CHANGED_PROPERTY = "mnemonic";
  
  private JCheckBox    checkBox;
  private Boolean      value = Boolean.FALSE;
  private boolean      nullable;
  private ItemListener checkBoxListener;
  private List<ChangeListener> changeListeners = new ArrayList<ChangeListener>(1);
  
  /**
   * Creates a nullable check box.
   */
  public NullableCheckBox(String text) {
    // Measure check box size alone without its text
    final Dimension checkBoxSize = new JCheckBox().getPreferredSize();
    // Create a check box that displays a dash upon default check box for a null value
    this.checkBox = new JCheckBox(text) {
      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (value == null) {
          g.drawRect(checkBoxSize.width / 2 - 3, checkBoxSize.height / 2, 6, 1);
        }
      }
    };
    // Add an item listener to change default checking logic 
    this.checkBoxListener = new ItemListener() {
      public void itemStateChanged(ItemEvent ev) {
        // If this check box is nullable
        if (nullable) {
          // Checking sequence will be null, true, false
          if (getValue() == Boolean.FALSE) {
            setValue(null);
          } else if (getValue() == null) {
            setValue(Boolean.TRUE);
          } else {
            setValue(Boolean.FALSE);
          }
        } else {
          setValue(checkBox.isSelected());
        }
      }
    };
    this.checkBox.addItemListener(this.checkBoxListener);
    
    // Add the check box and its label to this component
    setLayout(new GridLayout());
    add(this.checkBox);
  }
  
  /**
   * Returns <code>null</code>, <code>Boolean.TRUE</code> or <code>Boolean.FALSE</code>.
   */
  public Boolean getValue() {
    return this.value;
  }

  /**
   * Sets displayed value in check box. 
   * @param value <code>null</code>, <code>Boolean.TRUE</code> or <code>Boolean.FALSE</code>
   */
  public void setValue(Boolean value) {
    this.value = value;
    this.checkBox.removeItemListener(this.checkBoxListener);
    try {
      if (value != null) {
        this.checkBox.setSelected(value);
      } else if (isNullable()) {
        // Unselect check box to display a dash in its middle
        this.checkBox.setSelected(false);
        this.checkBox.repaint();
      } else {
        throw new IllegalArgumentException("Check box isn't nullable");
      }
      fireStateChanged();
    } finally {
      this.checkBox.addItemListener(this.checkBoxListener);
    }      
  }
  
  /**
   * Returns <code>true</code> if this check box is nullable.
   */
  public boolean isNullable() {
    return this.nullable;
  }

  /**
   * Sets whether this check box is nullable.
   */
  public void setNullable(boolean nullable) {
    this.nullable = nullable;
    if (!nullable && getValue() == null) {
      setValue(Boolean.FALSE);
    }
  }
  
  /**
   * Sets the mnemonic of this component.
   * @param mnemonic a <code>VK_...</code> code defined in <code>java.awt.event.KeyEvent</code>. 
   */
  public void setMnemonic(int mnemonic) {
    int oldMnemonic = this.checkBox.getMnemonic();
    if (oldMnemonic != mnemonic) {
      this.checkBox.setMnemonic(mnemonic);
      firePropertyChange(MNEMONIC_CHANGED_PROPERTY, oldMnemonic, mnemonic);
    }
  }
  
  /**
   * Returns the mnemonic of this component.
   */
  public int getMnemonic() {
    return this.checkBox.getMnemonic();
  }

  /**
   * Sets the text of this component.
   * @param text a <code>VK_...</code> code defined in <code>java.awt.event.KeyEvent</code>. 
   */
  public void setText(String text) {
    String oldText = this.checkBox.getText();
    if (oldText != text) {
      this.checkBox.setText(text);
      firePropertyChange(TEXT_CHANGED_PROPERTY, oldText, text);
    }
  }
  
  /**
   * Returns the text of this component.
   */
  public String getText() {
    return this.checkBox.getText();
  }
  
  /**
   * Sets the tool tip text displayed by this check box.
   */
  public void setToolTipText(String text) {
    this.checkBox.setToolTipText(text);
  }

  @Override
  public void setEnabled(boolean enabled) {
    if (this.checkBox.isEnabled() != enabled) {
      this.checkBox.setEnabled(enabled);
      firePropertyChange("enabled", !enabled, enabled);
    }
  }

  @Override
  public boolean isEnabled() {
    return this.checkBox.isEnabled();
  }
  
  /**
   * Adds a listener to this component.
   */
  public void addChangeListener(final ChangeListener l) {
    this.changeListeners.add(l);
  }

  /**
   * Removes a listener from this component.
   */
  public void removeChangeListener(final ChangeListener l) {
    this.changeListeners.remove(l);
  }

  /**
   * Fires a state changed event to listeners.
   */
  private void fireStateChanged() {
    if (!this.changeListeners.isEmpty()) {
      ChangeEvent changeEvent = new ChangeEvent(this);
      // Work on a copy of changeListeners to ensure a listener 
      // can modify safely listeners list
      ChangeListener [] listeners = this.changeListeners.
        toArray(new ChangeListener [this.changeListeners.size()]);
      for (ChangeListener listener : listeners) {
        listener.stateChanged(changeEvent);
      }
    }
  }
}