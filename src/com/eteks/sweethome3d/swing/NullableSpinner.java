/*
 * NullableSpinner.java 29 mai 07
 *
 * Copyright (c) 2007 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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

import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.eteks.sweethome3d.model.UserPreferences;

/**
 * Spinner that accepts empty string values. In this case the returned value is <code>null</code>. 
 */
public class NullableSpinner extends JSpinner {
  /**
   * Creates a default nullable spinner able to edit an integer. 
   */
  public NullableSpinner() {
    this(new NullableSpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
  }
  
  /**
   * Creates a nullable spinner from <code>model</code>. 
   */
 public NullableSpinner(NullableSpinnerNumberModel model) {
    super(model);
    final JFormattedTextField textField = ((DefaultEditor)getEditor()).getTextField();
    final JFormattedTextField.AbstractFormatter formatter = textField.getFormatter();
    // Change formatted text field formatter to enable the edition of empty values
    textField.setFormatterFactory(new JFormattedTextField.AbstractFormatterFactory() {
        @Override
        public JFormattedTextField.AbstractFormatter getFormatter(JFormattedTextField tf) {
          return new JFormattedTextField.AbstractFormatter () {
              @Override
              public Object stringToValue(String text) throws ParseException {
                if (text.length() == 0 && ((NullableSpinnerNumberModel)getModel()).isNullable()) {
                  // Return null for empty text 
                  return null;
                } else {
                  return formatter.stringToValue(text);
                }
              }

              @Override
              public String valueToString(Object value) throws ParseException {
                if (value == null && ((NullableSpinnerNumberModel)getModel()).isNullable()) {
                  // Return empty text forn null values
                  return "";
                } else {
                  return formatter.valueToString(value);
                }
              }
            };
        }
      });
  }
  
  /**
   * Spinner number model that accepts <code>null</code> values. 
   */
  public static class NullableSpinnerNumberModel extends SpinnerNumberModel {
    private boolean isNull;
    private boolean nullable;

    public NullableSpinnerNumberModel(int value, int minimum, int maximum, int stepSize) {
      super(value, minimum, maximum, stepSize);
    }

    public NullableSpinnerNumberModel(float value, float minimum, float maximum, float stepSize) {
      // Invoke constructor that take objects in parameter to avoid any ambiguity
      super(new Float(value), new Float(minimum), new Float(maximum), new Float(stepSize));
    }

    @Override
    public Object getNextValue() {
      if (this.isNull) {
        return super.getValue();
      } 
      return super.getNextValue();
    }

    @Override
    public Object getPreviousValue() {
      if (this.isNull) {
        return super.getValue();
      } 
      return super.getPreviousValue();
    }

    @Override
    public Object getValue() {
      if (this.isNull) {
        return null;
      } else {
        return super.getValue();
      }
    }

    /**
     * Sets model value. This method is overriden to store whether current value is <code>null</code> 
     * or not (super class <code>setValue</code> doesn't accept <code>null</code> value).
     */
    @Override
    public void setValue(Object value) {
      if (value == null && isNullable()) {
        if (!this.isNull) {
          this.isNull = true;
          fireStateChanged();
        }
      } else { 
        this.isNull = false;
        super.setValue(value);
      }
    }

    @Override
    public Number getNumber() {
      return (Number)getValue();
    }

    /**
     * Returns <code>true</code> if this spinner model is nullable.
     */
    public boolean isNullable() {
      return this.nullable;
    }

    /**
     * Sets whether this spinner model is nullable.
     */
    public void setNullable(boolean nullable) {
      this.nullable = nullable;
      if (!nullable && getValue() == null) {
        setValue(getMinimum());
      }
    }
  }
  
  /**
   * Nullable spinner model displaying length values matching preferences unit. 
   */
  public static class NullableSpinnerLengthModel extends NullableSpinnerNumberModel {
    private final UserPreferences preferences;

    public NullableSpinnerLengthModel(UserPreferences preferences, float minimum, float maximum) {
      super(minimum, minimum, maximum, 
            preferences.getUnit() == UserPreferences.Unit.INCH
              ? 0.125f : 0.5f);
      this.preferences = preferences;
    }

    /**
     * Returns the diplayed value in centimeter.
     */
    public Float getLength() {
      if (getValue() == null) {
        return null;
      } else if (this.preferences.getUnit() == UserPreferences.Unit.INCH) {
        return UserPreferences.Unit.inchToCentimeter(((Number)getValue()).floatValue());
      } else {
        return ((Number)getValue()).floatValue();
      }
    }

    /**
     * Sets the length in centimeter displayed in this model.
     */
    public void setLength(Float length) {
      if (length != null 
          && this.preferences.getUnit() == UserPreferences.Unit.INCH) {
        length = UserPreferences.Unit.centimeterToInch(length);
      } 
      setValue(length);
    }
  }
}