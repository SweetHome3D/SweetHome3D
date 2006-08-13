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
import java.awt.geom.Rectangle2D;
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

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.SelectionEvent;
import com.eteks.sweethome3d.model.SelectionListener;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.model.WallEvent;
import com.eteks.sweethome3d.model.WallListener;
import com.eteks.sweethome3d.viewcontroller.PlanController;
import com.eteks.sweethome3d.viewcontroller.PlanView;

/**
 * A component displaying a plan.
 * @author Emmanuel Puybaret
 */
public class PlanViewer extends Viewer implements PlanView {
  private static final float MARGIN = 40;
  private Home               home;
  private UserPreferences    preferences;
  private float              scale = 0.5f;

  private Rectangle2D        planBoundsCache;
  
  // SWT resources used by this viewer
  private Canvas             control;
  private Path               rectangleFeedback;
  private Cursor             arrowCursor;
  private Cursor             crossCursor;

  public PlanViewer(Composite parent, 
                    Home home, UserPreferences preferences,
                    PlanController controller) {
    this.home = home;
    this.preferences = preferences;
    // Create control associated with this viewer
    this.control = new Canvas(parent, SWT.DOUBLE_BUFFERED) {
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
    addModelListeners(home);
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
   * Adds wall and selection listeners on this component to receive wall 
   * changes notifications from home. 
   */
  private void addModelListeners(Home home) {
    home.addWallListener(new WallListener () {
      public void wallChanged(WallEvent ev) {
        planBoundsCache = null;
        // No direct SWT equivalent to Swing revalidate
        if (control.getParent() instanceof ScrolledComposite) {
          ((ScrolledComposite)control.getParent()).setMinSize(
              control.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        }
        refresh();
      }
    });
    home.addSelectionListener(new SelectionListener () {
      public void selectionChanged(SelectionEvent selectionEvent) {
        refresh();
      }
    });
  }

  /**
   * Adds SWT mouse listeners to this component that calls back 
   * <code>controller</code> methods.  
   */
  private void addMouseListeners(final PlanController controller) {
    this.control.addMouseListener(new MouseListener() {
      public void mouseDoubleClick(MouseEvent ev) {
        if (control.isEnabled()) {
          controller.pressMouse(convertXPixelToModel(ev.x), convertYPixelToModel(ev.y), 
              2, (ev.stateMask & SWT.SHIFT) != 0);
        }
      }

      public void mouseDown(MouseEvent ev) {
        if (control.isEnabled()) {
          controller.pressMouse(convertXPixelToModel(ev.x), convertYPixelToModel(ev.y), 
              1, (ev.stateMask & SWT.SHIFT) != 0);
        }
      }

      public void mouseUp(MouseEvent ev) {
        if (control.isEnabled()) {
          controller.releaseMouse(convertXPixelToModel(ev.x), convertYPixelToModel(ev.y));
        }
      }
    });
    this.control.addMouseMoveListener(new MouseMoveListener() {
      public void mouseMove(MouseEvent ev) {
        if (control.isEnabled()) {
          controller.moveMouse(convertXPixelToModel(ev.x), convertYPixelToModel(ev.y));
        }
      }
    });
  }

  /**
   * Adds SWT key listener to this component that calls back <code>controller</code> methods.  
   */
  private void addKeyListener(final PlanController controller) {
    this.control.addKeyListener(new KeyListener() {
      public void keyPressed(KeyEvent ev) {
        if (control.isEnabled()) {
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
      }

      public void keyReleased(KeyEvent ev) {
        if (control.isEnabled() && ev.keyCode == SWT.SHIFT) {
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
    if (this.planBoundsCache == null) {
      this.planBoundsCache = new Rectangle2D.Float(0, 0, 1000, 1000);
      for (Wall wall : home.getWalls()) {
        this.planBoundsCache.add(wall.getXStart(), wall.getYStart());
        this.planBoundsCache.add(wall.getXEnd(), wall.getYEnd());
      }
    }
    return this.planBoundsCache;
  }

  /**
   * Paints this control.
   */
  private void paintControl(GC gc) {
    paintBackground(gc);
    // Change component coordinates system to plan system
    Rectangle2D wallsBounds = getPlanBounds();    
    Transform transform = new Transform(Display.getCurrent());
    transform.translate(
        this.control.getBorderWidth() + (float)(MARGIN - wallsBounds.getMinX()) * this.scale,
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

  /**
   * Paints background grid lines.
   */
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
      for (float x = (int) (xMin / mainGridSize) * mainGridSize; 
           x < xMax; x += mainGridSize) {
        Path linesPath = new Path(this.control.getDisplay());
        linesPath.moveTo(x, yMin);
        linesPath.lineTo(x, yMax);
        gc.drawPath(linesPath);
        linesPath.dispose();
      }
      // Draw positive main horizontal lines
      for (float y = (int) (yMin / mainGridSize) * mainGridSize; 
           y < yMax; y += mainGridSize) {
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
    Path wallsPath = getWallsPath(this.home.getWalls());
    // Fill walls area
    Pattern wallPattern = getWallPattern();
    gc.setBackgroundPattern(wallPattern);
    gc.fillPath(wallsPath);
    gc.setBackgroundPattern(null);
    wallPattern.dispose();
    // Draw selected walls with a surrounding shape
    if (!this.home.getSelectedItems().isEmpty()) {
      gc.setForeground(this.control.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
      gc.setAlpha(128);
      gc.setLineWidth((int)Math.round(6 / this.scale));
      gc.setLineJoin(SWT.JOIN_ROUND);
      Path selectedPaths = new Path(this.control.getDisplay());
      for (Object item : this.home.getSelectedItems()) {
        if (item instanceof Wall) {
          float [][] wallPoints = ((Wall)item).getPoints();
          selectedPaths.moveTo(wallPoints [0][0], wallPoints [0][1]);
          for (int i = 1; i < wallPoints.length; i++) {
            selectedPaths.lineTo(wallPoints [i][0], wallPoints [i][1]);        
          }
          selectedPaths.close();
        }
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
    imageGC.setForeground(
        this.control.getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND));
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
      float [][] wallPoints = wall.getPoints();
      wallsPath.moveTo(wallPoints [0][0], wallPoints [0][1]);
      for (int i = 1; i < wallPoints.length; i++) {
        wallsPath.lineTo(wallPoints [i][0], wallPoints [i][1]);        
      }
      wallsPath.close();
    }    
    return wallsPath;
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
   * Ensures selected walls are visible at screen and moves
   * scroll bars if needed.
   */
  public void makeSelectionVisible() {
    List<Object> selectedItems = this.home.getSelectedItems();
    if (!selectedItems.isEmpty()) {
      float minX = Float.MAX_VALUE;
      float minY = Float.MAX_VALUE;
      for (Object item : selectedItems) {
        if (item instanceof Wall) {
          float [][] wallPoints = ((Wall)item).getPoints();
          for (int i = 0; i < wallPoints.length; i++) {
            minX = Math.min(minX, wallPoints [i][0]);
            minY = Math.min(minY, wallPoints [i][1]);
          }
        }
      }      
      makePointVisible(minX, minY);
    }
  }
 
  /**
   * Ensures the point at (<code>xPixel</code>, <code>yPixel</code>) is visible,
   * moving scroll bars if needed.
   */
  public void makePointVisible(float x, float y) {
    // No direct SWT equivalent to Swing scrollRectToVisible
    if (this.control.getParent() instanceof ScrolledComposite) {
      Rectangle pixelBounds = getShapePixelBounds(
          new Rectangle2D.Float(x, y, 1 / this.scale, 1 / this.scale));
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

  // Viewer super class methods implementation
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
    return this.home;
  }

  @Override
  public void setInput(Object input) {
    this.home = (Home)input;
    refresh();
  }

  @Override
  public ISelection getSelection() {
    return new StructuredSelection(this.home.getSelectedItems());
  }

  @Override
  public void setSelection(ISelection selection, boolean reveal) {
    List<Object> selectedWalls;
    if (selection.isEmpty()) {
      selectedWalls = Collections.emptyList();
    } else if (selection instanceof IStructuredSelection) {
      // Check selection contains only walls
      selectedWalls = Collections.checkedList(
          ((IStructuredSelection)selection).toList(), Wall.class);
    } else {
      throw new IllegalArgumentException("Wrong selection class");
    }
    
    this.home.setSelectedItems(selectedWalls);
    if (reveal) {
      makeSelectionVisible();
    }
  }
}