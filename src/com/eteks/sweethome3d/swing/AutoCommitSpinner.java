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

import java.awt.Color;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

import com.eteks.parser.CalculatorParser;
import com.eteks.parser.CompilationException;
import com.eteks.parser.Function;
import com.eteks.parser.Interpreter;
import com.eteks.parser.Syntax;
import com.eteks.sweethome3d.model.LengthUnit;

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
   * Creates a spinner with a given <code>model</code> and <code>format</code>, which will allow math expressions.
   */
  public AutoCommitSpinner(SpinnerModel model,
                           Format format) {
    this(model, format, true);
  }

  /**
   * Creates a spinner with a given <code>model</code> and <code>format</code>.
   */
  public AutoCommitSpinner(SpinnerModel model,
                           Format format,
                           final boolean allowMathExpressions) {
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
                // Add a listener to spinner text field that keeps track when the user typed a character
                final KeyAdapter keyListener = new KeyAdapter() {
                    public void keyTyped(KeyEvent ev) {
                      // keyTyped isn't called for UP and DOWN keys of text field input map
                      keepFocusedTextUnchanged = true;
                    };
                  };
                textField.addFocusListener(new FocusAdapter() {
                    public void focusGained(FocusEvent ev) {
                      textField.addKeyListener(keyListener);
                    }

                    public void focusLost(FocusEvent ev) {
                      textField.removeKeyListener(keyListener);
                    };
                  });
              }

              @Override
              public Format getFormat() {
                Format format = super.getFormat();
                // Use a different format depending on whether the text field has focus or not
                if (textField.hasFocus() && format instanceof DecimalFormat) {
                  // No grouping when text field has focus
                  final DecimalFormat noGroupingFormat = (DecimalFormat)format.clone();
                  noGroupingFormat.setGroupingUsed(false);
                  try {
                    if (allowMathExpressions) {
                      return new CalculatorFormat(noGroupingFormat);
                    }
                  } catch (LinkageError ex) {
                    // Don't allow math expressions if Jeks Parser library isn't available
                  }
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
                    && this.keepFocusedTextUnchanged) {
                  this.keepFocusedTextUnchanged = false;
                  return textField.getText();
                } else {
                  return super.valueToString(value);
                }
              }
            };
          editFormatter.setCommitsOnValidEdit(true);
          textField.setFormatterFactory(new DefaultFormatterFactory(editFormatter));
          textField.addFocusListener(new FocusAdapter() {
              public void focusLost(FocusEvent ev) {
                textField.setForeground(UIManager.getColor("FormattedTextField.foreground"));
              }
            });
        }
      }
    }

    if (format != null) {
      setFormat(format);
    }
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

  /**
   * A decimal format able to calculate formulas.
   */
  private class CalculatorFormat extends DecimalFormat {
    private DecimalFormat    numberFormat;

    private CalculatorFormat(DecimalFormat numberFormat) {
      super(numberFormat.toPattern());
      this.numberFormat = numberFormat;
    }

    @Override
    public Number parse(String text, ParsePosition pos) {
      final String parsedText = text.substring(pos.getIndex());
      Number number = this.numberFormat.parse(text, pos);
      if (number == null || pos.getIndex() != text.length()) {
        LengthUnit lengthUnit = getModel() instanceof NullableSpinner.NullableSpinnerLengthModel
            ? ((NullableSpinner.NullableSpinnerLengthModel)getModel()).getLengthUnit()
            : null;
        CalculatorParser parser = new CalculatorParser(new CalculatorSyntax(this.numberFormat, lengthUnit));
        try {
          // Try to parse with Jeks Parser
          number = (Number)parser.computeExpression(parsedText, new CalculatorInterpreter());
          if (number != null && lengthUnit != null) {
            number = lengthUnit.unitToCentimeter(number.floatValue());
          }
          pos.setIndex(text.length());
        } catch (CompilationException ex) {
          // Keep default value
        }
      }
      Color defaultColor = UIManager.getColor("FormattedTextField.foreground");
      if (Color.BLACK.equals(defaultColor) || Color.WHITE.equals(defaultColor)) {
        JFormattedTextField textField = ((DefaultEditor)getEditor()).getTextField();
        NumberFormatter formatter = (NumberFormatter)textField.getFormatter();
        if (pos.getIndex() != text.length()
               && text.substring(pos.getIndex()).trim().length() > 0
            || number != null
               && (number.doubleValue() < ((Number)formatter.getMinimum()).doubleValue()
                   || number.doubleValue() > ((Number)formatter.getMaximum()).doubleValue())) {
          // Change text color if parsing couldn't be completed
          textField.setForeground(Color.RED.darker());
        } else {
          textField.setForeground(defaultColor);
        }
      }

      return number;
    }

    public StringBuffer format(double number, StringBuffer result, FieldPosition position) {
      return this.numberFormat.format(number, result, position);
    }
  }

  /**
   * The syntax used in computed spinners.
   */
  private static class CalculatorSyntax implements Syntax {
    private final DecimalFormat format;
    private final LengthUnit    lengthUnit;

    private CalculatorSyntax(DecimalFormat format,
                             LengthUnit    lengthUnit) {
      this.format = format;
      this.lengthUnit = lengthUnit;
    }

    public Object getLiteral(String expression, StringBuffer extractedString) {
      ParsePosition position = new ParsePosition(0);
      Number literal = this.format.parse(expression, position);
      if (literal != null && this.lengthUnit != null) {
        literal = this.lengthUnit.centimeterToUnit(literal.floatValue());
      }
      extractedString.append(expression, 0, position.getIndex());
      return literal;
    }

    public Object getConstantKey(String constant) {
      return null; // No constant only numbers
    }

    public Object getUnaryOperatorKey(String unaryOperator) {
      if ("-".equals(unaryOperator)) {
        return unaryOperator;
      } else {
        return null;
      }
    }

    public Object getBinaryOperatorKey(String binaryOperator) {
      if ("+".equals(binaryOperator)
          || "-".equals(binaryOperator)
          || "/".equals(binaryOperator)
          || "*".equals(binaryOperator)
          || "^".equals(binaryOperator)) {
        return binaryOperator;
      } else {
        return null;
      }
    }

    public Object getConditionPartKey(String ternaryOperator) {
      return null; // No condition
    }

    public int getConditionPartCount() {
      return 0;
    }

    public Object getCommonFunctionKey(String predefinedFunction) {
      predefinedFunction = predefinedFunction.toUpperCase();
      if ("LN".equals(predefinedFunction)
          || "LOG".equals(predefinedFunction)
          || "EXP".equals(predefinedFunction)
          || "SQR".equals(predefinedFunction)
          || "SQRT".equals(predefinedFunction)
          || "COS".equals(predefinedFunction)
          || "SIN".equals(predefinedFunction)
          || "TAN".equals(predefinedFunction)
          || "ARCCOS".equals(predefinedFunction)
          || "ARCSIN".equals(predefinedFunction)
          || "ARCTAN".equals(predefinedFunction)) {
        return predefinedFunction;
      } else {
        return null;
      }
    }

    public Function getFunction(String userFunction) {
      return null; // No function
    }

    public int getBinaryOperatorPriority(Object binaryOperatorKey) {
      if ("+".equals(binaryOperatorKey)
          || "-".equals(binaryOperatorKey)) {
        return 1;
      } else if ("/".equals(binaryOperatorKey)
                || "*".equals(binaryOperatorKey)) {
        return 2;
      } else if ("^".equals(binaryOperatorKey)) {
        return 3;
      } else {
        throw new IllegalArgumentException();
      }
    }

    public String getAssignmentOperator() {
      return null;
    }

    public String getWhiteSpaceCharacters() {
      return " \t\n\r";
    }

    public char getOpeningBracket() {
      return '(';
    }

    public char getClosingBracket() {
      return ')';
    }

    public char getParameterSeparator() {
      return 0;
    }

    public String getDelimiters() {
      return " \t\n\r-+*/^().";
    }

    public boolean isCaseSensitive() {
      return false;
    }

    public boolean isShortSyntax() {
      // Supports common function calls without brackets
      return true;
    }

    public boolean isValidIdentifier(String identifier) {
      return false; // No identifier
    }
  }

  /**
   * The interpreter used to compute spinners.
   */
  private static class CalculatorInterpreter implements Interpreter {
    public Object getLiteralValue(Object literal) {
      return literal;
    }

    public Object getParameterValue(Object parameter) {
      return null; // No parameter
    }

    public Object getConstantValue(Object key) {
      return null; // No constant
    }

    public Double getUnaryOperatorValue(Object unaryOperator, Object param) {
      if (unaryOperator.equals("-")) {
        return -((Number)param).doubleValue();
      } else {
        throw new IllegalArgumentException("Not implemented");
      }
    }

    public Double getBinaryOperatorValue(Object binaryOperator, Object param1, Object param2) {
      if (binaryOperator.equals("+")) {
        return ((Number)param1).doubleValue() + ((Number)param2).doubleValue();
      } else if (binaryOperator.equals("-")) {
        return ((Number)param1).doubleValue() - ((Number)param2).doubleValue();
      } else if (binaryOperator.equals("/")) {
        return ((Number)param1).doubleValue() / ((Number)param2).doubleValue();
      } else if (binaryOperator.equals("*")) {
        return ((Number)param1).doubleValue() * ((Number)param2).doubleValue();
      } else if (binaryOperator.equals("^")) {
        return Math.pow(((Number)param1).doubleValue(), ((Number)param2).doubleValue());
      } else {
        throw new IllegalArgumentException("Not implemented");
      }
    }

    public Double getCommonFunctionValue(Object predefinedFunction, Object param) {
      if (predefinedFunction.equals("LN")) {
        return Math.log(((Number)param).doubleValue());
      } else if (predefinedFunction.equals("LOG")) {
        return Math.log(((Number)param).doubleValue()) / Math.log(10.);
      } else if (predefinedFunction.equals("EXP")) {
        return Math.exp(((Number)param).doubleValue());
      } else if (predefinedFunction.equals("SQR")) {
        return ((Number)param).doubleValue() * ((Number)param).doubleValue();
      } else if (predefinedFunction.equals("SQRT")) {
        return Math.sqrt(((Number)param).doubleValue());
      } else if (predefinedFunction.equals("COS")) {
        return Math.cos(Math.toRadians(((Number)param).doubleValue()));
      } else if (predefinedFunction.equals("SIN")) {
        return Math.sin(Math.toRadians(((Number)param).doubleValue()));
      } else if (predefinedFunction.equals("TAN")) {
        return Math.tan(Math.toRadians(((Number)param).doubleValue()));
      } else if (predefinedFunction.equals("ARCCOS")) {
        return Math.toDegrees(Math.acos(((Number)param).doubleValue()));
      } else if (predefinedFunction.equals("ARCSIN")) {
        return Math.toDegrees(Math.asin(((Number)param).doubleValue()));
      } else if (predefinedFunction.equals("ARCTAN")) {
        return Math.toDegrees(Math.atan(((Number)param).doubleValue()));
      } else {
        throw new IllegalArgumentException("Not implemented");
      }
    }

    public Object getConditionValue(Object paramIf, Object paramThen, Object paramElse) {
      return null;
    }

    public boolean isTrue(Object param) {
      return false; // No condition
    }

    public boolean supportsRecursiveCall() {
      return false;
    }

    public Object getFunctionValue(Function function, Object [] parameters, boolean recursiveCall) {
      return null; // No function
    }
  }
}
