/*
 * CalculatorFormat.java 23 mars 2020
 *
 * Sweet Home 3D, Copyright (c) 2020 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;

import com.eteks.parser.CalculatorParser;
import com.eteks.parser.CompilationException;
import com.eteks.parser.Function;
import com.eteks.parser.Interpreter;
import com.eteks.parser.Syntax;
import com.eteks.sweethome3d.model.LengthUnit;

/**
 * A decimal format able to calculate formulas.
 * @author Emmanuel Puybaret
 */
class CalculatorFormat extends DecimalFormat {
  private DecimalFormat numberFormat;
  private LengthUnit    lengthUnit;

  public CalculatorFormat(DecimalFormat numberFormat,
                          LengthUnit    lengthUnit) {
    super(numberFormat.toPattern());
    this.numberFormat = numberFormat;
    this.lengthUnit = lengthUnit;
  }

  @Override
  public Number parse(String text, ParsePosition pos) {
    final String parsedText = text.substring(pos.getIndex());
    Number number = this.numberFormat.parse(text, pos);
    if (number == null || pos.getIndex() != text.length()) {
      CalculatorParser parser = new CalculatorParser(new CalculatorSyntax());
      try {
        // Try to parse with Jeks Parser
        number = (Number)parser.computeExpression(parsedText, new CalculatorInterpreter());
        if (number != null && this.lengthUnit != null) {
          number = this.lengthUnit.unitToCentimeter(number.floatValue());
        }
        pos.setIndex(text.length());
      } catch (CompilationException ex) {
        // Keep default value
      }
    }
    return number;
  }

  public StringBuffer format(double number, StringBuffer result, FieldPosition position) {
    return this.numberFormat.format(number, result, position);
  }

  /**
   * The syntax used in computed formulas.
   */
  private class CalculatorSyntax implements Syntax {
    public Object getLiteral(String expression, StringBuffer extractedString) {
      ParsePosition position = new ParsePosition(0);
      Number literal = numberFormat.parse(expression, position);
      if (literal != null && lengthUnit != null) {
        literal = lengthUnit.centimeterToUnit(literal.floatValue());
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
   * The interpreter used to compute formulas.
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
