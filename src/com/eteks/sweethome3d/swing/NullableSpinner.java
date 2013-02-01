/*
 * NullableSpinner.java 29 mai 07
 *
 * Sweet Home 3D, Copyright (c) 2007 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
import javax.swing.SpinnerNumberModel;
import javax.swing.text.NumberFormatter;

import com.eteks.sweethome3d.model.LengthUnit;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * Spinner that accepts empty string values. In this case the returned value is <code>null</code>. 
 */
public class NullableSpinner extends AutoCommitSpinner {
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
    final JFormattedTextField.AbstractFormatter defaultFormatter = textField.getFormatter();
    // Change formatted text field formatter to enable the edition of empty values
    textField.setFormatterFactory(new JFormattedTextField.AbstractFormatterFactory() {
        @Override
        public JFormattedTextField.AbstractFormatter getFormatter(JFormattedTextField tf) {
          return new NumberFormatter () {
              @Override
              public boolean getCommitsOnValidEdit() {
                if (defaultFormatter instanceof NumberFormatter) {
                  return ((NumberFormatter)defaultFormatter).getCommitsOnValidEdit();
                } else {
                  return super.getCommitsOnValidEdit();
                }
              }
              
              @SuppressWarnings("unchecked")
              @Override
              public Comparable getMaximum() {
                if (defaultFormatter instanceof NumberFormatter) {
                  return ((NumberFormatter)defaultFormatter).getMaximum();
                } else {
                  return super.getMaximum();
                }
              }
              
              @SuppressWarnings("unchecked")
              @Override
              public Comparable getMinimum() {
                if (defaultFormatter instanceof NumberFormatter) {
                  return ((NumberFormatter)defaultFormatter).getMinimum();
                } else {
                  return super.getMinimum();
                }
              }
              
              @SuppressWarnings("unchecked")
              @Override
              public void setMaximum(Comparable maximum) {
                if (defaultFormatter instanceof NumberFormatter) {
                  ((NumberFormatter)defaultFormatter).setMaximum(maximum);
                } else {
                  super.setMaximum(maximum);
                }
              }
              
              @SuppressWarnings("unchecked")
              @Override
              public void setMinimum(Comparable minimum) {
                if (defaultFormatter instanceof NumberFormatter) {
                  ((NumberFormatter)defaultFormatter).setMinimum(minimum);
                } else {
                  super.setMinimum(minimum);
                }
              }
              
              @Override
              public Object stringToValue(String text) throws ParseException {
                if (text.length() == 0 && ((NullableSpinnerNumberModel)getModel()).isNullable()) {
                  // Return null for empty text 
                  return null;
                } else {
                  return defaultFormatter.stringToValue(text);
                }
              }

              @Override
              public String valueToString(Object value) throws ParseException {
                if (value == null && ((NullableSpinnerNumberModel)getModel()).isNullable()) {
                  // Return empty text for null values
                  return "";
                } else {
                  return defaultFormatter.valueToString(value);
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
      Object nextValue = super.getNextValue();
      if (nextValue == null) {
        // Force to maximum value
        return getMaximum();
      } else {
        return nextValue;
      }
    }

    @Override
    public Object getPreviousValue() {
      if (this.isNull) {
        return super.getValue();
      } 
      Object previousValue = super.getPreviousValue();
      if (previousValue == null) {
        // Force to minimum value
        return getMinimum();
      } else {
        return previousValue;
      }
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
     * Sets model value. This method is overridden to store whether current value is <code>null</code> 
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
        if (this.isNull 
            && value != null 
            && value.equals(super.getValue())) {
          // Fire a state change if the value set is the same one as the one stored by number model
          // and this model exposed a null value before
          this.isNull = false;
          fireStateChanged();
        } else {
          this.isNull = false;
          super.setValue(value);
        }
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

    /**
     * Creates a model managing lengths between the given <code>minimum</code> and <code>maximum</code> values in centimeter. 
     */
    public NullableSpinnerLengthModel(UserPreferences preferences, float minimum, float maximum) {
      super(preferences.getLengthUnit().centimeterToUnit(minimum), 
            preferences.getLengthUnit().centimeterToUnit(minimum), 
            preferences.getLengthUnit().centimeterToUnit(maximum), 
            preferences.getLengthUnit() == LengthUnit.INCH
            || preferences.getLengthUnit() == LengthUnit.INCH_DECIMALS
              ? 0.125f : preferences.getLengthUnit().centimeterToUnit(0.5f));
      this.preferences = preferences;
    }

    /**
     * Returns the displayed value in centimeter.
     */
    public Float getLength() {
      if (getValue() == null) {
        return null;
      } else {
        return this.preferences.getLengthUnit().unitToCentimeter(((Number)getValue()).floatValue());
      }
    }

    /**
     * Sets the length in centimeter displayed in this model.
     */
    public void setLength(Float length) {
      if (length != null) {
        length = this.preferences.getLengthUnit().centimeterToUnit(length);
      } 
      setValue(length);
    }
  }
}