/*
 * AutoSelectSpinner.java 10 sept. 2008
 *
 * Sweet Home 3D, Copyright (c) 2008 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.ParseException;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

/**
 * A spinner which commits its value during edition and selects 
 * the value displayed in its editor when it gains focus.
 * @author Emmanuel Puybaret
 */
public class AutoCommitSpinner extends JSpinner {
  /**
   * Creates a spinner with a given <code>model</code>.
   */
  public AutoCommitSpinner(SpinnerModel model) {
    this(model, null);
  }
  
  /**
   * Creates a spinner with a given <code>model</code> and <code>format</code>.
   */
  public AutoCommitSpinner(SpinnerModel model, 
                           Format format) {
    super(model);
    JComponent editor = getEditor();
    if (editor instanceof JSpinner.DefaultEditor) {
      final JFormattedTextField textField = ((JSpinner.DefaultEditor)editor).getTextField();      
      SwingTools.addAutoSelectionOnFocusGain(textField);
      // Commit text during edition
      if (textField.getFormatterFactory() instanceof DefaultFormatterFactory) {
        DefaultFormatterFactory formatterFactory = (DefaultFormatterFactory)textField.getFormatterFactory();
        JFormattedTextField.AbstractFormatter defaultFormatter = formatterFactory.getDefaultFormatter();
        if (defaultFormatter instanceof DefaultFormatter) {
          ((DefaultFormatter)defaultFormatter).setCommitsOnValidEdit(true);
        }
        if (defaultFormatter instanceof NumberFormatter) {
          final NumberFormatter numberFormatter = (NumberFormatter)defaultFormatter;
          // Create a delegate of default formatter to change value returned by getFormat
          NumberFormatter editFormatter = new NumberFormatter() {
              private boolean keepFocusedTextUnchanged;

              {
                textField.addFocusListener(new FocusAdapter() {
                    public void focusGained(FocusEvent ev) {
                      keepFocusedTextUnchanged = false;
                    }
                  });
              }
              
              @Override
              public Format getFormat() {
                Format format = super.getFormat();
                // Use a different format depending on whether the text field has focus or not
                if (textField.hasFocus() && format instanceof DecimalFormat) {
                  // No grouping when text field has focus 
                  DecimalFormat noGroupingFormat = (DecimalFormat)format.clone();
                  noGroupingFormat.setGroupingUsed(false);
                  return noGroupingFormat;
                } else {
                  return format;
                }
              }
            
              @SuppressWarnings({"rawtypes"})
              @Override
              public Comparable getMaximum() {
                return numberFormatter.getMaximum();
              }
              
              @SuppressWarnings({"rawtypes"})
              @Override
              public Comparable getMinimum() {
                return numberFormatter.getMinimum();
              }
              
              @SuppressWarnings({"rawtypes"})
              @Override
              public void setMaximum(Comparable maximum) {
                numberFormatter.setMaximum(maximum);
              }
              
              @SuppressWarnings({"rawtypes"})
              @Override
              public void setMinimum(Comparable minimum) {
                numberFormatter.setMinimum(minimum);
              }
              
              @Override
              public Class<?> getValueClass() {
                return numberFormatter.getValueClass();
              }
              
              @Override
              public String valueToString(Object value) throws ParseException {
                if (textField.hasFocus()
                    && textField.getCaretPosition() > 0
                    && this.keepFocusedTextUnchanged) {
                  return textField.getText();
                } else {
                  this.keepFocusedTextUnchanged = true;
                  return super.valueToString(value);
                }
              }
            };
          editFormatter.setCommitsOnValidEdit(true);
          textField.setFormatterFactory(new DefaultFormatterFactory(editFormatter));
        }
      }
    }
    setFormat(format);
  }
  
  /**
   * Sets the format used to display the value of this spinner.
   */
  public void setFormat(Format format) {
    JComponent editor = getEditor();
    if (editor instanceof JSpinner.DefaultEditor) {
      JFormattedTextField textField = ((JSpinner.DefaultEditor)editor).getTextField();
      AbstractFormatter formatter = textField.getFormatter();
      if (formatter instanceof NumberFormatter) {
        ((NumberFormatter)formatter).setFormat(format);
        fireStateChanged();
      }
    }
  }

  /**
   * A spinner number model that will reset to minimum when maximum is reached. 
   */
  public static class SpinnerModuloNumberModel extends SpinnerNumberModel {
    public SpinnerModuloNumberModel(int value, int minimum, int maximum, int stepSize) {
      super(value, minimum, maximum, stepSize);
    }
    
    @Override
    public Object getNextValue() {
      if (getNumber().intValue() + getStepSize().intValue() < ((Number)getMaximum()).intValue()) {
        return ((Number)super.getNextValue()).intValue();
      } else {
        return getNumber().intValue() + getStepSize().intValue() - ((Number)getMaximum()).intValue() + ((Number)getMinimum()).intValue();
      }
    }
    
    @Override
    public Object getPreviousValue() {
      if (getNumber().intValue() - getStepSize().intValue() >= ((Number)getMinimum()).intValue()) {
        return ((Number)super.getPreviousValue()).intValue();
      } else {
        return getNumber().intValue() - getStepSize().intValue() - ((Number)getMinimum()).intValue() + ((Number)getMaximum()).intValue();
      }
    }
  }
}
