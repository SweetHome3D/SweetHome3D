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
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.MouseInputAdapter;

import com.eteks.sweethome3d.model.Home;
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

  private List<Wall>         selectedWalls;
  private Rectangle2D        rectangleFeedback;

  public PlanComponent(PlanController controller, Home home,
                       UserPreferences preferences) {
    this.home = home;
    this.preferences = preferences;
    this.selectedWalls = new ArrayList<Wall>();

    // TODO
    home.addWallListener(new WallListener () {
      public void wallChanged(WallEvent ev) {
        repaint();
      }
    });
    if (controller != null) {
      addAWTListeners(controller);
      configureKeyMap(controller);
    }

    setFocusable(true);
    setOpaque(true);
  }

  // TODO
  /**
   * Adds AWT listeners to this component that calls back <code>controller</code> methods.  
   */
  private void addAWTListeners(final PlanController controller) {
    MouseInputAdapter mouseListener = new MouseInputAdapter() {
      @Override
      public void mousePressed(MouseEvent ev) {
        if (!ev.isPopupTrigger()) {
          requestFocusInWindow();
          controller.pressMouse(ev.getX(), ev.getY(), ev.getClickCount(), ev.isShiftDown());
          controller.setMagnetismDisabled(ev.isAltDown());
        }
      }

      @Override
      public void mouseReleased(MouseEvent ev) {
        if (!ev.isPopupTrigger()) {
          controller.releaseMouse(ev.getX(), ev.getY());
        }
      }

      @Override
      public void mouseMoved(MouseEvent ev) {
        controller.moveMouse(ev.getX(), ev.getY());
      }

      @Override
      public void mouseDragged(MouseEvent ev) {
        controller.moveMouse(ev.getX(), ev.getY());
      }
    };
    addMouseListener(mouseListener);
    addMouseMotionListener(mouseListener);
    addFocusListener(new FocusAdapter() {
      @Override
      public void focusLost(FocusEvent ev) {
        controller.escape();
      }
    });
  }

  // TODO
  private void configureKeyMap(final PlanController controller) {
    // Delete selection action mapped to back space and delete keys
    Action deleteSelectionAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        controller.deleteSelection();
      }
    };
    // Escape action mapped to Esc key
    Action escapeAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        controller.escape();
      }
    };
    // Move selection action mapped to arrow keys 
    class MoveSelectionAction extends AbstractAction {
      private final int dx;
      private final int dy;
      
      public MoveSelectionAction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
      }

      public void actionPerformed(ActionEvent e) {
        controller.moveSelection(this.dx, this.dy);
      }
    }
    // Temporary magnestism mapped to alt key
    class DisalbleMagnetismAction extends AbstractAction {
      private final boolean enabled;
      
      public DisalbleMagnetismAction(boolean enabled) {
        this.enabled = enabled;
      }

      public void actionPerformed(ActionEvent e) {
        controller.setMagnetismDisabled(this.enabled);
      }
    }
    ActionMap actionMap = getActionMap();
    actionMap.put("deleteSelectionAction", deleteSelectionAction);
    actionMap.put("escapeAction", escapeAction);
    actionMap.put("moveSelectionLeftAction", new MoveSelectionAction(-1, 0));
    actionMap.put("moveSelectionUpAction", new MoveSelectionAction(0, -1));
    actionMap.put("moveSelectionDownAction", new MoveSelectionAction(0, 1));
    actionMap.put("moveSelectionRightAction", new MoveSelectionAction(1, 0));
    actionMap.put("disableMagnetismAction", new DisalbleMagnetismAction(true));
    actionMap.put("enableMagnetismAction", new DisalbleMagnetismAction(false));
    InputMap inputMap = getInputMap(WHEN_FOCUSED);
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteSelectionAction");
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "deleteSelectionAction");
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escapeAction");
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "moveSelectionLeftAction");
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "moveSelectionUpAction");
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "moveSelectionDownAction");
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "moveSelectionRightAction");
    inputMap.put(KeyStroke.getKeyStroke("alt pressed ALT"), "disableMagnetismAction");
    inputMap.put(KeyStroke.getKeyStroke("released ALT"), "enableMagnetismAction");
  }

  /**
   * Returns the preferred size of this component.
   */
  @Override
  public Dimension getPreferredSize() {
    float xMax = Float.NEGATIVE_INFINITY;
    float yMax = Float.NEGATIVE_INFINITY;
    Collection<Wall> walls = home.getWalls(); 
    if (walls.isEmpty()) {
      xMax = yMax = 1000;
    } else {
      for (Wall wall : walls) {
        xMax = Math.max(xMax, wall.getXStart());
        xMax = Math.max(xMax, wall.getXEnd());
        yMax = Math.max(yMax, wall.getYStart());
        yMax = Math.max(yMax, wall.getYEnd());
      }
    }
    Insets insets = getInsets();
    return new Dimension(
        Math.round((xMax + MARGIN * 2) * this.scale) + insets.left + insets.right, 
        Math.round((yMax + MARGIN * 2) * this.scale) + insets.top  + insets.bottom);
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
    // Change coordinates system to home
    g2D.translate(insets.left + MARGIN * this.scale, insets.top + MARGIN * this.scale);
    g2D.scale(scale, scale);
    g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    // Paint component contents
    paintGrid(g2D);
    paintWalls(g2D);   
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
    
    Insets insets = getInsets();
    float xMin = convertXPixelToModel(insets.left);
    float xMax = convertXPixelToModel(getWidth() - insets.right);
    float yMin = convertYPixelToModel(insets.top);
    float yMax = convertYPixelToModel(getHeight() - insets.bottom);
    g2D.setColor(Color.LIGHT_GRAY);
    g2D.setStroke(new BasicStroke(1 / this.scale));
    // Draw vertical lines
    for (float x = (int)(xMin / gridSize) * gridSize; x < xMax; x += gridSize) {
      g2D.draw(new Line2D.Float(x, yMin, x, yMax)); 
    }
    // Draw horizontal lines
    for (float y = (int)(yMin / gridSize) * gridSize; y < yMax; y += gridSize) {
      g2D.draw(new Line2D.Float(xMin, y, xMax, y)); 
    }
    if (mainGridSize != gridSize) {
      g2D.setStroke(new BasicStroke(2 / this.scale, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
      // Draw main vertical lines
      for (float x = (int)(xMin / mainGridSize) * mainGridSize; x < xMax; x += mainGridSize) {
        g2D.draw(new Line2D.Float(x, yMin, x, yMax));
      }
      // Draw positive main horizontal lines
      for (float y = (int)(xMin / mainGridSize) * mainGridSize; y < yMax; y += mainGridSize) {
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
   * Paints home walls.
   */
  private void paintWalls(Graphics2D g2D) {
    Shape wallsArea = getWallsArea();
    // Fill walls area
    g2D.setPaint(getWallPaint());
    g2D.fill(wallsArea);
    // Draw selected walls with a surrounding shape
    Color selectionColor = UIManager.getColor("textHighlight");
    g2D.setPaint(new Color(selectionColor.getRed(), selectionColor.getGreen(), selectionColor.getBlue(), 128));
    g2D.setStroke(new BasicStroke(6 / this.scale, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    for (Wall wall : this.selectedWalls) {
      g2D.draw(getWallShape(wall));
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
  private Shape getWallsArea() {
    Area area = new Area();
    for (Wall wall : this.home.getWalls()) {
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
    this.selectedWalls.addAll(selectedWalls);
    repaint();
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
   * Returns <code>x</code> converted in model coordinates sytem.
   */
  public float convertXPixelToModel(int x) {
    Insets insets = getInsets();
    return (x - insets.left) / this.scale - MARGIN;
  }

  /**
   * Returns <code>y</code> converted in model coordinates sytem.
   */
  public float convertYPixelToModel(int y) {
    Insets insets = getInsets();
    return (y - insets.top) / this.scale - MARGIN;
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
   * with a given <code>margin</code>.
   */
  public boolean containsWallAt(Wall wall, float x, float y, float margin) {
    return getWallShape(wall).intersects(x - margin, y - margin, 2 * margin, 2 * margin);
  }
  
  /**
   * Returns <code>true</code> if <code>wall</code> start line contains 
   * the point at (<code>x</code>, <code>y</code>)
   * with a given <code>margin</code>.
   */
  public boolean containsWallStartLineAt(Wall wall, float x, float y, float margin) {
    float [][] wallPoints = getWallPoints(wall);
    return new Rectangle2D.Float(x - margin, y - margin, 2 * margin, 2 * margin).
      intersectsLine(wallPoints [0][0], wallPoints [0][1], wallPoints [3][0], wallPoints [3][1]);
  }
  
  /**
   * Returns <code>true</code> if <code>wall</code> end line contains 
   * the point at (<code>x</code>, <code>y</code>)
   * with a given <code>margin</code>.
   */
  public boolean containsWallEndLineAt(Wall wall, float x, float y, float margin) {
    float [][] wallPoints = getWallPoints(wall);
    return new Rectangle2D.Float(x - margin, y - margin, 2 * margin, 2 * margin).
      intersectsLine(wallPoints [1][0], wallPoints [1][1], wallPoints [2][0], wallPoints [2][1]);
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
  
  // TODO
  
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
    this.scale = scale;
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
}

