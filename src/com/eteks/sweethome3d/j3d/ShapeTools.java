/*
 * ShapeTools.java 13 sept. 2018
 *
 * Sweet Home 3D, Copyright (c) 2018 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
package com.eteks.sweethome3d.j3d;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.batik.parser.AWTPathProducer;
import org.apache.batik.parser.ParseException;
import org.apache.batik.parser.PathParser;

import com.eteks.sweethome3d.model.Polyline;

/**
 * Gathers some useful tools for shapes.
 * @author Emmanuel Puybaret
 */
public class ShapeTools {
  // SVG path Shapes
  private static final Map<String, Shape> parsedShapes = new WeakHashMap<String, Shape>();

  private ShapeTools() {
    // This class contains only tools
  }

  /**
   * Returns the line stroke matching the given line styles.
   */
  public static Stroke getStroke(float thickness,
                                 Polyline.CapStyle capStyle,
                                 Polyline.JoinStyle joinStyle,
                                 float [] dashPattern,
                                 float dashOffset) {
    int strokeCapStyle;
    switch (capStyle) {
      case ROUND :
        strokeCapStyle = BasicStroke.CAP_ROUND;
        break;
      case SQUARE :
        strokeCapStyle = BasicStroke.CAP_SQUARE;
        break;
      default:
        strokeCapStyle = BasicStroke.CAP_BUTT;
        break;
    }

    int strokeJoinStyle;
    switch (joinStyle) {
      case ROUND :
      case CURVED :
        strokeJoinStyle = BasicStroke.JOIN_ROUND;
        break;
      case BEVEL :
        strokeJoinStyle = BasicStroke.JOIN_BEVEL;
        break;
      default:
        strokeJoinStyle = BasicStroke.JOIN_MITER;
        break;
    }

    float dashPhase = 0;
    if (dashPattern != null) {
      dashPattern = dashPattern.clone();
      for (int i = 0; i < dashPattern.length; i++) {
        dashPattern [i] *= thickness;
        dashPhase += dashPattern [i];
      }
      dashPhase *= dashOffset;
    }

    return new BasicStroke(thickness, strokeCapStyle, strokeJoinStyle, 10, dashPattern, dashPhase);
  }

  /**
   * Returns the shape of a polyline.
   */
  public static Shape getPolylineShape(float [][] points, boolean curved, boolean closedPath) {
    if (curved) {
      GeneralPath polylineShape = new GeneralPath();
      for (int i = 0, n = closedPath ? points.length : points.length - 1; i < n; i++) {
        CubicCurve2D.Float curve2D = new CubicCurve2D.Float();
        float [] previousPoint = points [i == 0 ?  points.length - 1  : i - 1];
        float [] point         = points [i];
        float [] nextPoint     = points [i == points.length - 1 ?  0  : i + 1];
        float [] vectorToBisectorPoint = new float [] {nextPoint [0] - previousPoint [0], nextPoint [1] - previousPoint [1]};
        float [] nextNextPoint     = points [(i + 2) % points.length];
        float [] vectorToBisectorNextPoint = new float [] {point[0] - nextNextPoint [0], point[1] - nextNextPoint [1]};
        curve2D.setCurve(point[0], point[1],
            point [0] + (i != 0 || closedPath  ? vectorToBisectorPoint [0] / 3.625f  : 0),
            point [1] + (i != 0 || closedPath  ? vectorToBisectorPoint [1] / 3.625f  : 0),
            nextPoint [0] + (i != points.length - 2 || closedPath  ? vectorToBisectorNextPoint [0] / 3.625f  : 0),
            nextPoint [1] + (i != points.length - 2 || closedPath  ? vectorToBisectorNextPoint [1] / 3.625f  : 0),
            nextPoint [0], nextPoint [1]);
        polylineShape.append(curve2D, true);
      }
      return polylineShape;
    } else {
      return getShape(points, closedPath, null);
    }
  }

  /**
   * Returns the shape matching the coordinates in <code>points</code> array.
   */
  /**
   * Returns the shape matching the coordinates in <code>points</code> array.
   */
  public static Shape getShape(float [][] points, boolean closedPath, AffineTransform transform) {
    GeneralPath path = new GeneralPath();
    path.moveTo(points [0][0], points [0][1]);
    for (int i = 1; i < points.length; i++) {
      path.lineTo(points [i][0], points [i][1]);
    }
    if (closedPath) {
      path.closePath();
    }
    if (transform != null) {
      path.transform(transform);
    }
    return path;
  }

  /**
   * Returns the AWT shape matching the given <a href="http://www.w3.org/TR/SVG/paths.html">SVG path shape</a>.
   */
  public static Shape getShape(String svgPathShape) {
    Shape shape = parsedShapes.get(svgPathShape);
    if (shape == null) {
      try {
        shape = SVGPathSupport.parsePathShape(svgPathShape);
      } catch (LinkageError ex) {
        // Fallback to default square shape if batik classes aren't in classpath
        shape = new Rectangle2D.Float(0, 0, 1, 1);
      }
      parsedShapes.put(svgPathShape, shape);
    }
    return shape;
  }

  /**
   * Separated static class to be able to exclude Batik library from classpath.
   */
  private static class SVGPathSupport {
    public static Shape parsePathShape(String svgPathShape) {
      try {
        AWTPathProducer pathProducer = new AWTPathProducer();
        PathParser pathParser = new PathParser();
        pathParser.setPathHandler(pathProducer);
        pathParser.parse(svgPathShape);
        return pathProducer.getShape();
      } catch (ParseException ex) {
        // Fallback to default square shape if shape is incorrect
        return new Rectangle2D.Float(0, 0, 1, 1);
      }
    }
  }
}
