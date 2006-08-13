/*
 * PlanComponent.java 2 juin 2006
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
package com.eteks.sweethome3d.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.event.MouseInputAdapter;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.SelectionEvent;
import com.eteks.sweethome3d.model.SelectionListener;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.model.WallEvent;
import com.eteks.sweethome3d.model.WallListener;

/**
 * A component displaying the plan of a home.
 * @author Emmanuel Puybaret
 */
public class PlanComponent extends JComponent {
  private static final float MARGIN = 40;
  private Home               home;
  private UserPreferences    preferences;
  private float              scale = 0.5f;

  private Rectangle2D        rectangleFeedback;
  private Rectangle2D        planBoundsCache;

  public PlanComponent(Home home, UserPreferences preferences,
                       PlanController controller) {
    this.home = home;
    this.preferences = preferences;
    // Set JComponent default properties
    setOpaque(true);
    // Add listeners
    addModelListeners(home);
    if (controller != null) {
      addMouseListeners(controller);
      addKeyListener(controller);
      addFocusListener(controller);
      setFocusable(true);
      setAutoscrolls(true);
    }
  }

  /**
   * Adds wall and selection listeners on this component to receive wall 
   * changes notifications from home. 
   */
  private void addModelListeners(Home home) {
    home.addWallListener(new WallListener () {
      public void wallChanged(WallEvent ev) {
        planBoundsCache = null;
        // Revalidate and repaint
        revalidate();
        repaint();
      }
    });
    home.addSelectionListener(new SelectionListener () {
      public void selectionChanged(SelectionEvent selectionEvent) {
        repaint();
      }
    });
  }

  /**
   * Adds AWT mouse listeners to this component that calls back <code>controller</code> methods.  
   */
  private void addMouseListeners(final PlanController controller) {
    MouseInputAdapter mouseListener = new MouseInputAdapter() {
      @Override
      public void mousePressed(MouseEvent ev) {
        if (isEnabled() && !ev.isPopupTrigger()) {
          requestFocusInWindow();
          controller.pressMouse(convertXPixelToModel(ev.getX()), convertYPixelToModel(ev.getY()), 
              ev.getClickCount(), ev.isShiftDown());
        }
      }

      @Override
      public void mouseReleased(MouseEvent ev) {
        if (isEnabled() && !ev.isPopupTrigger()) {
          controller.releaseMouse(convertXPixelToModel(ev.getX()), convertYPixelToModel(ev.getY()));
        }
      }

      @Override
      public void mouseMoved(MouseEvent ev) {
        if (isEnabled()) {
          controller.moveMouse(convertXPixelToModel(ev.getX()), convertYPixelToModel(ev.getY()));
        }
      }

      @Override
      public void mouseDragged(MouseEvent ev) {
        if (isEnabled()) {
          controller.moveMouse(convertXPixelToModel(ev.getX()), convertYPixelToModel(ev.getY()));
        }
      }
    };
    addMouseListener(mouseListener);
    addMouseMotionListener(mouseListener);
  }

  /**
   * Adds AWT key listener to this component that calls back <code>controller</code> methods.  
   */
  private void addKeyListener(final PlanController controller) {
    addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent ev) {
        if (isEnabled()) {
          switch (ev.getKeyCode()) {
            case KeyEvent.VK_BACK_SPACE :
            case KeyEvent.VK_DELETE :
              controller.deleteSelection();
              break;
            case KeyEvent.VK_ESCAPE :
              controller.escape();
              break;
            case KeyEvent.VK_SHIFT :
              controller.toggleMagnetism(true);
              break;
            case KeyEvent.VK_LEFT :
              controller.moveSelection(-1 / scale, 0);
              break;
            case KeyEvent.VK_UP :
              controller.moveSelection(0, -1 / scale);
              break;
            case KeyEvent.VK_DOWN :
              controller.moveSelection(0, 1 / scale);
              break;
            case KeyEvent.VK_RIGHT :
              controller.moveSelection(1 / scale, 0);
              break;
          }
        }
      }

      @Override
      public void keyReleased(KeyEvent ev) {
        if (isEnabled() && ev.getKeyCode() == KeyEvent.VK_SHIFT) {
          controller.toggleMagnetism(false);
        }
      }
    });
  }
  
  /**
   * Adds AWT focus listener to this component that calls back <code>controller</code> 
   * escape method on focus lost event.  
   */
  private void addFocusListener(final PlanController controller) {
    addFocusListener(new FocusAdapter() {
      @Override
      public void focusLost(FocusEvent ev) {
        controller.escape();
      }
    });
  }

  /**
   * Returns the preferred size of this component.
   */
  @Override
  public Dimension getPreferredSize() {
    if (isPreferredSizeSet()) {
      return super.getPreferredSize();
    } else {
      Insets insets = getInsets();
      Rectangle2D planBounds = getPlanBounds();
      return new Dimension(
          Math.round(((float)planBounds.getWidth() + MARGIN * 2)
                     * this.scale) + insets.left + insets.right,
          Math.round(((float)planBounds.getHeight() + MARGIN * 2)
                     * this.scale) + insets.top + insets.bottom);
    }
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
   * Paints this component.
   */
  @Override
  protected void paintComponent(Graphics g) {
    Graphics2D g2D = (Graphics2D)g.create();
    paintBackground(g2D);
    Insets insets = getInsets();
    // Clip componant to avoid drawing in empty borders
    g2D.clipRect(insets.left, insets.top, 
        getWidth() - insets.left - insets.right, 
        getHeight() - insets.top - insets.bottom);
    // Change component coordinates system to plan system
    Rectangle2D planBounds = getPlanBounds();    
    g2D.translate(insets.left + (MARGIN - planBounds.getMinX()) * this.scale,
        insets.top + (MARGIN - planBounds.getMinY()) * this.scale);
    g2D.scale(scale, scale);
    g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    // Paint component contents
    paintGrid(g2D);
    paintContent(g2D);   
    paintRectangleFeedback(g2D);
    g2D.dispose();
  }

  /**
   * Fills the background with UI window background color. 
   */
  private void paintBackground(Graphics2D g2D) {
    if (isOpaque()) {
      Color backgroundColor = UIManager.getColor("window");
      g2D.setColor(backgroundColor);
      g2D.fillRect(0, 0, getWidth(), getHeight());
    }
  }

  /**
   * Paints background grid lines.
   */
  private void paintGrid(Graphics2D g2D) {
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
    
    Rectangle2D planBounds = getPlanBounds();    
    float xMin = (float)planBounds.getMinX() - MARGIN;
    float yMin = (float)planBounds.getMinY() - MARGIN;
    float xMax = convertXPixelToModel(getWidth());
    float yMax = convertYPixelToModel(getHeight());

    g2D.setColor(Color.LIGHT_GRAY);
    g2D.setStroke(new BasicStroke(1 / this.scale));
    // Draw vertical lines
    for (float x = (int) (xMin / gridSize) * gridSize; x < xMax; x += gridSize) {
      g2D.draw(new Line2D.Float(x, yMin, x, yMax));
    }
    // Draw horizontal lines
    for (float y = (int) (yMin / gridSize) * gridSize; y < yMax; y += gridSize) {
      g2D.draw(new Line2D.Float(xMin, y, xMax, y));
    }

    if (mainGridSize != gridSize) {
      g2D.setStroke(new BasicStroke(2 / this.scale,
          BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
      // Draw main vertical lines
      for (float x = (int) (xMin / mainGridSize) * mainGridSize; x < xMax; x += mainGridSize) {
        g2D.draw(new Line2D.Float(x, yMin, x, yMax));
      }
      // Draw positive main horizontal lines
      for (float y = (int) (yMin / mainGridSize) * mainGridSize; y < yMax; y += mainGridSize) {
        g2D.draw(new Line2D.Float(xMin, y, xMax, y));
      }
    }
  }

  /**
   * Paints rectangle feedback.
   */
  private void paintRectangleFeedback(Graphics2D g2D) {
    if (this.rectangleFeedback != null) {
      Color selectionColor = UIManager.getColor("textHighlight");
      g2D.setPaint(new Color(selectionColor.getRed(), selectionColor.getGreen(), selectionColor.getBlue(), 32));
      g2D.fill(this.rectangleFeedback);
      g2D.setPaint(selectionColor);
      g2D.setStroke(new BasicStroke(1 / this.scale));
      g2D.draw(this.rectangleFeedback);
    }
  }

  /**
   * Paints plan items.
   */
  private void paintContent(Graphics2D g2D) {
    Shape wallsArea = getWallsArea(this.home.getWalls());
    // Fill walls area
    g2D.setPaint(getWallPaint());
    g2D.fill(wallsArea);
    // Draw selected walls with a surrounding shape
    Color selectionColor = UIManager.getColor("textHighlight");
    g2D.setPaint(new Color(selectionColor.getRed(), selectionColor.getGreen(), selectionColor.getBlue(), 128));
    g2D.setStroke(new BasicStroke(6 / this.scale, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    for (Object item : this.home.getSelectedItems()) {
      if (item instanceof Wall) {
        g2D.draw(getShape(((Wall)item).getPoints()));
      }
    }
    // Draw walls area
    g2D.setPaint(getForeground());
    g2D.setStroke(new BasicStroke(2f / this.scale));
    g2D.draw(wallsArea);
  }

  /**
   * Returns the <code>Paint</code> object used to fill walls.
   */
  private Paint getWallPaint() {
    BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
    Graphics2D imageGraphics = (Graphics2D)image.getGraphics();
    // Create an image displaying a line in its diagonal
    imageGraphics.setColor(UIManager.getColor("window"));
    imageGraphics.fillRect(0, 0, 10, 10);
    imageGraphics.setColor(getForeground());
    imageGraphics.drawLine(0, 9, 9, 0);
    imageGraphics.dispose();
    return new TexturePaint(image, 
        new Rectangle2D.Float(0, 0, 10 / this.scale, 10 / this.scale));
  }

  /**
   * Returns an area matching the union of all wall shapes. 
   */
  private Area getWallsArea(Collection<Wall> walls) {
    Area area = new Area();
    for (Wall wall : walls) {
      area.add(new Area(getShape(wall.getPoints())));
    }    
    return area;
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
   * Sets rectangle selection feedback coordinates. 
   */
  public void setRectangleFeedback(float x0, float y0, float x1, float y1) {
    this.rectangleFeedback = new Rectangle2D.Float(x0, y0, 0, 0);
    this.rectangleFeedback.add(x1, y1);
    repaint();
  }
  
  /**
   * Deletes rectangle feed back.
   */
  public void deleteRectangleFeedback() {
    this.rectangleFeedback = null;
    repaint();
  }

  /**
   * Ensures selected walls are visible at screen and moves
   * scroll bars if needed.
   */
  public void makeSelectionVisible() {
    List<Wall> selectedWalls = new ArrayList<Wall>();
    for (Object item : this.home.getSelectedItems()) {
      if (item instanceof Wall) {
        selectedWalls.add((Wall)item); 
      }
    }      
    Shape wallsArea = getWallsArea(selectedWalls);
    scrollRectToVisible(getShapePixelBounds(wallsArea));
  }
 
  /**
   * Ensures the point at (<code>xPixel</code>, <code>yPixel</code>) is visible,
   * moving scroll bars if needed.
   */
  public void makePointVisible(float x, float y) {
    scrollRectToVisible(getShapePixelBounds(new Rectangle2D.Float(x, y, 1 / this.scale, 1 / this.scale)));
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
      revalidate();
      repaint();
    }
  }

  /**
   * Sets mouse cursor, depending on mode.
   */
  public void setCursor(PlanController.Mode mode) {
    if (mode == PlanController.Mode.WALL_CREATION) {
      setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    } else {
      setCursor(Cursor.getDefaultCursor());
    }
  }

  /**
   * Returns <code>x</code> converted in model coordinates space.
   */
  private float convertXPixelToModel(int x) {
    Insets insets = getInsets();
    Rectangle2D planBounds = getPlanBounds();    
    return (x - insets.left) / this.scale - MARGIN + (float)planBounds.getMinX();
  }

  /**
   * Returns <code>y</code> converted in model coordinates space.
   */
  private float convertYPixelToModel(int y) {
    Insets insets = getInsets();
    Rectangle2D planBounds = getPlanBounds();    
    return (y - insets.top) / this.scale - MARGIN + (float)planBounds.getMinY();
  }

  /**
   * Returns the bounds of <code>shape</code> in pixels coordinates space.
   */
  private Rectangle getShapePixelBounds(Shape shape) {
    Insets insets = getInsets();
    Rectangle2D planBounds = getPlanBounds();
    Rectangle2D shapeBounds = shape.getBounds2D();
    return new Rectangle(
        (int)Math.round((shapeBounds.getMinX() - planBounds.getMinX() + MARGIN) * this.scale) + insets.left,
        (int)Math.round((shapeBounds.getMinY() - planBounds.getMinY() + MARGIN) * this.scale) + insets.top,
        (int)Math.round(shapeBounds.getWidth() * this.scale),
        (int)Math.round(shapeBounds.getHeight() * this.scale));
  }
}