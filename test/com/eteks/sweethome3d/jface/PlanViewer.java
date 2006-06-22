/*
 * PlanViewer.java 20 juin 2006
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
package com.eteks.sweethome3d.jface;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import com.eteks.sweethome3d.model.Plan;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.model.WallEvent;
import com.eteks.sweethome3d.model.WallListener;
import com.eteks.sweethome3d.viewcontroller.PlanController;
import com.eteks.sweethome3d.viewcontroller.PlanView;

/**
 * A component displaying the plan of a home.
 * @author Emmanuel Puybaret
 */
public class PlanViewer extends Viewer implements PlanView {
  private static final float MARGIN = 40;
  private Plan               plan;
  private UserPreferences    preferences;
  private float              scale = 0.5f;

  private List<Wall>         selectedWalls;
  
  // SWT resources used by this viewer
  private Canvas             control;
  private Path               rectangleFeedback;
  private Cursor             arrowCursor;
  private Cursor             crossCursor;

  public PlanViewer(Composite parent, 
                    Plan plan, UserPreferences preferences,
                    PlanController controller) {
    this.plan = plan;
    this.preferences = preferences;
    this.selectedWalls = new ArrayList<Wall>();
    // Create control associated with this viewer
    this.control = new Canvas(parent, SWT.NONE) {
      // Returns the preferred size of control
      @Override
      public Point computeSize(int wHint, int hHint, boolean changed) {
        Rectangle2D wallsBounds = getPlanBounds();
        return new Point(
            Math.round(((float) wallsBounds.getWidth() + MARGIN * 2)
                       * scale) + 2 * getBorderWidth(),
            Math.round(((float) wallsBounds.getHeight() + MARGIN * 2)
                       * scale) + 2 * getBorderWidth());
      }
    }; 
    arrowCursor = new Cursor(this.control.getDisplay(), SWT.CURSOR_ARROW);
    crossCursor = new Cursor(this.control.getDisplay(), SWT.CURSOR_CROSS);
    // Add listeners
    addControlListeners();
    addModelListener(plan);
    if (controller != null) {
      addMouseListeners(controller);
      addKeyListener(controller);
      addFocusListener(controller);
      // SWT controls are focusable by default 
      // No SWT equivalent to Swing autoscroll
    }
  }

  private void addControlListeners() {
    // Add paint listener on this component. 
    this.control.addPaintListener(new PaintListener () {
      public void paintControl(PaintEvent ev) {
        PlanViewer.this.paintControl(ev.gc);
      }
    });
    // Add dispose listener
    this.control.addDisposeListener(new DisposeListener () {
      public void widgetDisposed(DisposeEvent e) {
        if (rectangleFeedback != null) {
          rectangleFeedback.dispose();
        }
        arrowCursor.dispose();
        crossCursor.dispose();
      }
    });
  }

  /**
   * Adds wall listener on this component to receive wall changes notifications from plan. 
   */
  private void addModelListener(Plan plan) {
    plan.addWallListener(new WallListener () {
      public void wallChanged(WallEvent ev) {
        // No direct SWT equivalent to Swing revalidate
        if (control.getParent() instanceof ScrolledComposite) {
          ((ScrolledComposite)control.getParent()).setMinSize(
              control.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        }
        refresh();
      }
    });
  }

  /**
   * Adds SWT mouse listeners to this component that calls back <code>controller</code> methods.  
   */
  private void addMouseListeners(final PlanController controller) {
    this.control.addMouseListener(new MouseListener() {
      public void mouseDoubleClick(MouseEvent ev) {
        controller.pressMouse(convertXPixelToModel(ev.x), convertYPixelToModel(ev.y), 
            2, (ev.stateMask & SWT.SHIFT) != 0);
      }

      public void mouseDown(MouseEvent ev) {
        controller.pressMouse(convertXPixelToModel(ev.x), convertYPixelToModel(ev.y), 
            1, (ev.stateMask & SWT.SHIFT) != 0);
      }

      public void mouseUp(MouseEvent ev) {
        controller.releaseMouse(convertXPixelToModel(ev.x), convertYPixelToModel(ev.y));
      }
    });
    this.control.addMouseMoveListener(new MouseMoveListener() {
      public void mouseMove(MouseEvent ev) {
        controller.moveMouse(convertXPixelToModel(ev.x), convertYPixelToModel(ev.y));
      }
    });
  }

  /**
   * Adds SWT key listener to this component that calls back <code>controller</code> methods.  
   */
  private void addKeyListener(final PlanController controller) {
    this.control.addKeyListener(new KeyListener() {
      public void keyPressed(KeyEvent ev) {
        switch (ev.keyCode) {
          case SWT.BS :
          case SWT.DEL :
            controller.deleteSelection();
            break;
          case SWT.ESC :
            controller.escape();
            break;
          case SWT.SHIFT :
            controller.toggleMagnetism(true);
            break;
          case SWT.ARROW_LEFT :
            controller.moveSelection(-1 / scale, 0);
            break;
          case SWT.ARROW_UP :
            controller.moveSelection(0, -1 / scale);
            break;
          case SWT.ARROW_DOWN :
            controller.moveSelection(0, 1 / scale);
            break;
          case SWT.ARROW_RIGHT :
            controller.moveSelection(1 / scale, 0);
            break;
        }
      }

      public void keyReleased(KeyEvent ev) {
        if (ev.keyCode == SWT.SHIFT) {
          controller.toggleMagnetism(false);
        }
      }
    });
  }
  
  /**
   * Adds SWT focus listener to this component that calls back <code>controller</code> 
   * escape method on focus lost event.  
   */
  private void addFocusListener(final PlanController controller) {
    this.control.addFocusListener(new FocusAdapter() {
      @Override
      public void focusLost(FocusEvent ev) {
        controller.escape();
      }
    });
  }

  /**
   * Returns the bounds of the plan displayed by this component.
   */
  private Rectangle2D getPlanBounds() {
    Rectangle2D wallsBounds = new Rectangle2D.Float(0, 0, 1000, 1000);
    for (Wall wall : plan.getWalls()) {
      wallsBounds.add(wall.getXStart(), wall.getYStart());
      wallsBounds.add(wall.getXEnd(), wall.getYEnd());
    }
    return wallsBounds;
  }

  /**
   * Paints this control.
   */
  private void paintControl(GC gc) {
    paintBackground(gc);
    // Change component coordinates system to plan system
    Rectangle2D wallsBounds = getPlanBounds();    
    Transform transform = new Transform(Display.getCurrent());
    transform.translate(this.control.getBorderWidth() + (float)(MARGIN - wallsBounds.getMinX()) * this.scale,
        this.control.getBorderWidth() + (float)(MARGIN - wallsBounds.getMinY()) * this.scale);
    transform.scale(scale, scale);
    gc.setTransform(transform);
    gc.setAntialias(SWT.ON);
    // Paint component contents
    paintGrid(gc);
    paintWalls(gc);   
    paintRectangleFeedback(gc);
    transform.dispose();
  }

  /**
   * Fills the background with UI window background color. 
   */
  private void paintBackground(GC gc) {
    gc.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
    gc.fillRectangle(0, 0, this.control.getSize().x, this.control.getSize().y);
  }

  private void paintGrid(GC gc) {
    float mainGridSize;
    float [] gridSizes;
    if (this.preferences.getUnit() == UserPreferences.Unit.INCH) {
      // Use a grid in inch and foot with a minimun grid increment of 1 inch
      mainGridSize = 2.54f * 12; // 1 foot
      gridSizes = new float [] {2.54f, 5.08f, 7.62f, 15.24f, 30.48f};
    } else {
      // Use a grid in cm and meters with a minimun grid increment of 1 cm
      mainGridSize = 100;
      gridSizes = new float [] {1, 2, 5, 10, 20, 50, 100};
    }
    // Compute grid size to get a grid where the space between each line is around 10 pixels
    float gridSize = gridSizes [0];
    for (int i = 1; i < gridSizes.length && gridSize * this.scale < 10; i++) {
      gridSize = gridSizes [i];
    }
    
    Rectangle2D wallsBounds = getPlanBounds();    
    float xMin = (float)wallsBounds.getMinX() - MARGIN;
    float yMin = (float)wallsBounds.getMinY() - MARGIN;
    float xMax = convertXPixelToModel(this.control.getSize().x);
    float yMax = convertYPixelToModel(this.control.getSize().y);

    gc.setForeground(this.control.getDisplay().getSystemColor(SWT.COLOR_GRAY));
    // No line thickness in float with SWT !
    gc.setLineWidth((int)Math.round(1 / this.scale));
    // Draw vertical lines
    for (float x = (int) (xMin / gridSize) * gridSize; x < xMax; x += gridSize) {
      Path linesPath = new Path(this.control.getDisplay());
      linesPath.moveTo(x, yMin);
      linesPath.lineTo(x, yMax);
      gc.drawPath(linesPath);
      linesPath.dispose();
    }
    // Draw horizontal lines
    for (float y = (int) (yMin / gridSize) * gridSize; y < yMax; y += gridSize) {
      Path linesPath = new Path(this.control.getDisplay());
      linesPath.moveTo(xMin, y);
      linesPath.lineTo(xMax, y);
      gc.drawPath(linesPath);
      linesPath.dispose();
    }
    
    if (mainGridSize != gridSize) {
      gc.setLineWidth((int)Math.round(2 / this.scale));
      // Draw main vertical lines
      for (float x = (int) (xMin / mainGridSize) * mainGridSize; x < xMax; x += mainGridSize) {
        Path linesPath = new Path(this.control.getDisplay());
        linesPath.moveTo(x, yMin);
        linesPath.lineTo(x, yMax);
        gc.drawPath(linesPath);
        linesPath.dispose();
      }
      // Draw positive main horizontal lines
      for (float y = (int) (yMin / mainGridSize) * mainGridSize; y < yMax; y += mainGridSize) {
        Path linesPath = new Path(this.control.getDisplay());
        linesPath.moveTo(xMin, y);
        linesPath.lineTo(xMax, y);
        gc.drawPath(linesPath);
        linesPath.dispose();
      }
    }
  }

  /**
   * Paints rectangle feedback.
   */
  private void paintRectangleFeedback(GC gc) {
    if (this.rectangleFeedback != null) {
      Color selectionColor = this.control.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION);
      gc.setBackground(selectionColor);
      gc.setAlpha(32);
      gc.fillPath(this.rectangleFeedback);
      gc.setForeground(selectionColor);
      gc.setAlpha(255);
      gc.setLineWidth((int)Math.round(1 / this.scale));
      gc.drawPath(this.rectangleFeedback);
    }
  }

  /**
   * Paints plan walls.
   */
  private void paintWalls(GC gc) {
    Path wallsPath = getWallsPath(this.plan.getWalls());
    // Fill walls area
    Pattern wallPattern = getWallPattern();
    gc.setBackgroundPattern(wallPattern);
    gc.fillPath(wallsPath);
    gc.setBackgroundPattern(null);
    wallPattern.dispose();
    // Draw selected walls with a surrounding shape
    if (!this.selectedWalls.isEmpty()) {
      gc.setForeground(this.control.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
      gc.setAlpha(128);
      gc.setLineWidth((int)Math.round(6 / this.scale));
      gc.setLineJoin(SWT.JOIN_ROUND);
      Path selectedPaths = new Path(this.control.getDisplay());
      for (Wall wall : this.selectedWalls) {
        float [][] wallPoints = getWallPoints(wall);
        selectedPaths.moveTo(wallPoints [0][0], wallPoints [0][1]);
        for (int i = 1; i < wallPoints.length; i++) {
          selectedPaths.lineTo(wallPoints [i][0], wallPoints [i][1]);        
        }
        selectedPaths.close();
      }
      gc.drawPath(selectedPaths);
      selectedPaths.dispose();
    }
    // Draw walls area
    gc.setForeground(this.control.getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND));
    gc.setAlpha(255);
    gc.setLineWidth((int)Math.round(2 / this.scale));
    gc.setLineJoin(SWT.JOIN_MITER);
    gc.drawPath(wallsPath);
    wallsPath.dispose();
  }

  /**
   * Returns the <code>Pattern</code> object used to fill walls.
   */
  private Pattern getWallPattern() {
    Image image = new Image(this.control.getDisplay(), 10, 10);
    GC imageGC = new GC(image);
    // Create an image displaying a line in its diagonal
    imageGC.setBackground(this.control.getDisplay().getSystemColor(SWT.COLOR_WHITE));
    imageGC.fillRectangle(0, 0, 10, 10);
    imageGC.setForeground(this.control.getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND));
    imageGC.drawLine(0, 9, 9, 0);
    imageGC.dispose();
    return new Pattern(this.control.getDisplay(), image);
  }

  /**
   * Returns an area matching the union of all wall shapes. 
   */
  private Path getWallsPath(Collection<Wall> walls) {
    Path wallsPath = new Path(this.control.getDisplay());
    for (Wall wall : walls) {
      float [][] wallPoints = getWallPoints(wall);
      wallsPath.moveTo(wallPoints [0][0], wallPoints [0][1]);
      for (int i = 1; i < wallPoints.length; i++) {
        wallsPath.lineTo(wallPoints [i][0], wallPoints [i][1]);        
      }
      wallsPath.close();
    }    
    return wallsPath;
  }
  
  /**
   * Returns an area matching the union of all wall shapes. 
   */
  private Shape getWallsArea(Collection<Wall> walls) {
    Area area = new Area();
    for (Wall wall : walls) {
      area.add(new Area(getWallShape(wall)));
    }    
    return area;
  }
  
  /**
   * Returns the shape of a <code>wall</code>.
   */
  private Shape getWallShape(Wall wall) {
    float [][] wallPoints = getWallRectangle(wall);
    float limit = 2 * wall.getThickness();
    Wall wallAtStart = wall.getWallAtStart();
    // If wall is joined to a wall at its start, 
    // compute the intersection between their outlines 
    if (wallAtStart != null) {
      float [][] wallAtStartPoints = getWallRectangle(wallAtStart);
      if (wallAtStart.getWallAtEnd() == wall) {
        computeIntersection(wallPoints [0], wallPoints [1], 
            wallAtStartPoints [1], wallAtStartPoints [0], limit);
        computeIntersection(wallPoints [3], wallPoints [2],  
            wallAtStartPoints [2], wallAtStartPoints [3], limit);
      } else if (wallAtStart.getWallAtStart() == wall) {
        computeIntersection(wallPoints [0], wallPoints [1], 
            wallAtStartPoints [2], wallAtStartPoints [3], limit);
        computeIntersection(wallPoints [3], wallPoints [2],  
            wallAtStartPoints [0], wallAtStartPoints [1], limit);
      }
    }
  
    Wall wallAtEnd = wall.getWallAtEnd();
    // If wall is joined to a wall at its end, 
    // compute the intersection between their outlines 
    if (wallAtEnd != null) {
      float [][] wallAtEndPoints = getWallRectangle(wallAtEnd);
      if (wallAtEnd.getWallAtStart() == wall) {
        computeIntersection(wallPoints [1], wallPoints [0], 
            wallAtEndPoints [0], wallAtEndPoints [1], limit);
        computeIntersection(wallPoints [2], wallPoints [3], 
            wallAtEndPoints [3], wallAtEndPoints [2], limit);
      
      } else if (wallAtEnd.getWallAtEnd() == wall) {
        computeIntersection(wallPoints [1], wallPoints [0],  
            wallAtEndPoints [3], wallAtEndPoints [2], limit);
        computeIntersection(wallPoints [2], wallPoints [3], 
            wallAtEndPoints [0], wallAtEndPoints [1], limit);
      }
    }

    return getShape(wallPoints);
  }

  /**
   * Compute the rectangle of a wall with its thickness.
   */  
  private float [][] getWallRectangle(Wall wall) {
    double angle = Math.atan2(wall.getYEnd() - wall.getYStart(), 
                              wall.getXEnd() - wall.getXStart());
    float dx = (float)Math.sin(angle) * wall.getThickness() / 2;
    float dy = (float)Math.cos(angle) * wall.getThickness() / 2;
    return new float [][] {
       {wall.getXStart() + dx, wall.getYStart() - dy},
       {wall.getXEnd()   + dx, wall.getYEnd()   - dy},
       {wall.getXEnd()   - dx, wall.getYEnd()   + dy},
       {wall.getXStart() - dx, wall.getYStart() + dy}};
  }
  
  /**
   * Compute the intersection between the line that joins <code>point1</code> to <code>point2</code>
   * and the line that joins <code>point3</code> and <code>point4</code>, and stores the result 
   * in <code>point1</code>.
   */
  private void computeIntersection(float [] point1, float [] point2, 
                                   float [] point3, float [] point4, float limit) {
    float x = point1 [0];
    float y = point1 [1];
    float alpha1 = (point2 [1] - point1 [1]) / (point2 [0] - point1 [0]);
    float beta1  = point2 [1] - alpha1 * point2 [0];
    float alpha2 = (point4 [1] - point3 [1]) / (point4 [0] - point3 [0]);
    float beta2  = point4 [1] - alpha2 * point4 [0];
    // If the two lines are not parallel
    if (alpha1 != alpha2) {
      // If first line is vertical
      if (point1 [0] == point2 [0]) {
        x = point1 [0];
        y = alpha2 * x + beta2;
      // If second line is vertical
      } else if (point3 [0] == point4 [0]) {
        x = point3 [0];
        y = alpha1 * x + beta1;
      } else  {
        x = (beta2 - beta1) / (alpha1 - alpha2);
        y = alpha1 * x + beta1;
      } 
    }
    if (Point2D.distanceSq(x, y, point1 [0], point1 [1]) < limit * limit) {
      point1 [0] = x;
      point1 [1] = y;
    }
  }
  
  /**
   * Returns the shape matching the coordinates in <code>points</code> array.
   */
  private Shape getShape(float [][] points) {
    GeneralPath wallPath = new GeneralPath();
    wallPath.moveTo(points [0][0], points [0][1]);
    for (int i = 1; i < points.length; i++) {
      wallPath.lineTo(points [i][0], points [i][1]);
    }
    wallPath.closePath();
    return wallPath;
  }

  /**
   * Returns an unmodifiable list of the selected walls in plan.
   */
  public List<Wall> getSelectedWalls() {
    return Collections.unmodifiableList(this.selectedWalls);
  }
  
  /**
   * Sets the selected walls in plan.
   * @param selectedWalls the list of walls to selected.
   */
  public void setSelectedWalls(List<Wall> selectedWalls) {
    this.selectedWalls.clear();
    if (!selectedWalls.isEmpty()) {
      this.selectedWalls.addAll(selectedWalls);
    }
    refresh();
  }
  
  /**
   * Sets rectangle selection feedback coordinates. 
   */
  public void setRectangleFeedback(float x0, float y0, float x1, float y1) {
    if (this.rectangleFeedback != null) {
      this.rectangleFeedback.dispose();
    }
    this.rectangleFeedback = new Path(this.control.getDisplay());
    this.rectangleFeedback.addRectangle(x0, y0, x1 - x0, y1 - y0);
    refresh();
  }
  
  /**
   * Deletes rectangle feed back.
   */
  public void deleteRectangleFeedback() {
    this.rectangleFeedback = null;
    refresh();
  }

  /**
   * Returns <code>true</code> if <code>wall</code> intersects
   * with the horizontal rectangle which opposite corners are at points
   * (<code>x0</code>, <code>y0</code>) and (<code>x1</code>, <code>y1</code>).
   */
  public boolean doesWallIntersectRectangle(Wall wall, float x0, float y0, float x1, float y1) {
    Rectangle2D rectangle = new Rectangle2D.Float(x0, y0, 0, 0);
    rectangle.add(x1, y1);
    return getWallShape(wall).intersects(rectangle);
  }
  
  /**
   * Returns <code>true</code> if <code>wall</code> contains 
   * the point at (<code>x</code>, <code>y</code>)
   * with a margin of 2 pixels.
   */
  public boolean containsWallAt(Wall wall, float x, float y) {
    return containsShapeAtWithMargin(getWallShape(wall), x, y);
  }
  
  /**
   * Returns <code>true</code> if <code>wall</code> start line contains 
   * the point at (<code>x</code>, <code>y</code>)
   * with a margin of 2 pixels around the wall start line.
   */
  public boolean containsWallStartAt(Wall wall, float x, float y) {
    float [][] wallPoints = getWallPoints(wall);
    Line2D startLine = new Line2D.Float(wallPoints [0][0], wallPoints [0][1], wallPoints [3][0], wallPoints [3][1]);
    return containsShapeAtWithMargin(startLine, x, y);
  }
  
  /**
   * Returns <code>true</code> if <code>wall</code> end line contains 
   * the point at (<code>x</code>, <code>y</code>)
   * with a margin of 2 pixels around the wall end line.
   */
  public boolean containsWallEndAt(Wall wall, float x, float y) {
    float [][] wallPoints = getWallPoints(wall);
    Line2D endLine = new Line2D.Float(wallPoints [1][0], wallPoints [1][1], wallPoints [2][0], wallPoints [2][1]); 
    return containsShapeAtWithMargin(endLine, x, y);
  }
  
  /**
   * Returns <code>true</code> if <code>shape</code> contains 
   * the point at (<code>x</code>, <code>y</code>)
   * with a margin of 2 pixels around the wall end line.
   */
  private boolean containsShapeAtWithMargin(Shape shape, float x, float y) {
    float margin = 2 / getScale();
    return shape.intersects(x - margin, y - margin, 2 * margin, 2 * margin);
  }
  
  /**
   * Returns the points of a wall shape.
   */
  private float [][] getWallPoints(Wall wall) {
    float [][] wallPoints = new float[4][2];
    PathIterator it = getWallShape(wall).getPathIterator(null);
    for (int i = 0; i < wallPoints.length; i++) {
      it.currentSegment(wallPoints [i]);
      it.next();
    }
    return wallPoints;
  }
  
  /**
   * Ensures <code>walls</code> are visible at screen and moves
   * scroll bars if needed.
   */
  public void ensureWallsAreVisible(Collection<Wall> walls) {
    Shape wallsArea = getWallsArea(walls);
    Rectangle2D wallsBounds = wallsArea.getBounds2D();
    ensurePointIsVisible((float)wallsBounds.getMinX(), (float)wallsBounds.getMinY());
  }
 
  /**
   * Ensures the point at (<code>xPixel</code>, <code>yPixel</code>) is visible,
   * moving scroll bars if needed.
   */
  public void ensurePointIsVisible(float x, float y) {
    // No direct SWT equivalent to Swing scrollRectToVisible
    if (this.control.getParent() instanceof ScrolledComposite) {
      Rectangle pixelBounds = getShapePixelBounds(new Rectangle2D.Float(x, y, 1 / this.scale, 1 / this.scale));
      ScrolledComposite parent = (ScrolledComposite)this.control.getParent();
      parent.setOrigin(pixelBounds.x, pixelBounds.y);
    }
  }

  /**
   * Returns the scale used to display the plan.
   */
  public float getScale() {
    return this.scale;
  }

  /**
   * Sets the scale used to display the plan.
   */
  public void setScale(float scale) {
    if (this.scale != scale) {
      this.scale = scale;
      // No direct SWT equivalent to Swing revalidate
      if (this.control.getParent() instanceof ScrolledComposite) {
        ((ScrolledComposite)control.getParent()).setMinSize(
            control.computeSize(SWT.DEFAULT, SWT.DEFAULT));
      }
      refresh();
    }
  }

  /**
   * Sets mouse cursor, depending on mode.
   */
  public void setCursor(PlanController.Mode mode) {
    if (mode == PlanController.Mode.WALL_CREATION) {
      this.control.setCursor(this.crossCursor);
    } else {
      this.control.setCursor(this.arrowCursor);
    }
  }

  /**
   * Returns <code>x</code> converted in model coordinates space.
   */
  private float convertXPixelToModel(int x) {
    Rectangle2D wallsBounds = getPlanBounds();    
    return x / this.scale - MARGIN + (float)wallsBounds.getMinX();
  }

  /**
   * Returns <code>y</code> converted in model coordinates space.
   */
  private float convertYPixelToModel(int y) {
    Rectangle2D wallsBounds = getPlanBounds();    
    return y / this.scale - MARGIN + (float)wallsBounds.getMinY();
  }

  /**
   * Returns the bounds of <code>shape</code> in pixels coordinates space.
   */
  private Rectangle getShapePixelBounds(Shape shape) {
    Rectangle2D wallsBounds = getPlanBounds();
    Rectangle2D shapeBounds = shape.getBounds2D();
    return new Rectangle(
        (int)Math.round((shapeBounds.getMinX() - wallsBounds.getMinX() + MARGIN) * this.scale),
        (int)Math.round((shapeBounds.getMinY() - wallsBounds.getMinY() + MARGIN) * this.scale),
        (int)Math.round(shapeBounds.getWidth() * this.scale),
        (int)Math.round(shapeBounds.getHeight() * this.scale));
  }

  // Viewer methods
  @Override
  public Control getControl() {
    return this.control;
  }

  @Override
  public void refresh() {
    this.control.redraw();
  }

  @Override
  public Object getInput() {
    return this.plan;
  }

  @Override
  public void setInput(Object input) {
    this.plan = (Plan)input;
    refresh();
  }

  @Override
  public ISelection getSelection() {
    return new StructuredSelection(this.selectedWalls);
  }

  @Override
  public void setSelection(ISelection selection, boolean reveal) {
    List<Wall> selectedWalls;
    if (selection.isEmpty()) {
      selectedWalls = Collections.emptyList();
    } else if (selection instanceof IStructuredSelection) {
      // Check selection contains only walls
      selectedWalls = Collections.checkedList(((IStructuredSelection)selection).toList(), Wall.class);
    } else {
      throw new IllegalArgumentException("Wrong selection class");
    }
    
    setSelectedWalls(selectedWalls);
    if (reveal) {
      ensureWallsAreVisible(selectedWalls);
    }
  }
}