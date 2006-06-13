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
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.event.MouseInputAdapter;

import quicktime.app.spaces.Collection;

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
  // Useles after second step
  private Home            home;
  
  private UserPreferences preferences;
  private List<Wall>      selectedWalls;
  private Rectangle2D     rectangleFeedback;
  private HashMap<Wall,Shape> shapes;

  private Area wallsArea;

  public PlanComponent(PlanController controller, Home home,
                       UserPreferences preferences) {
    this.home = home;
    this.preferences = preferences;
    // TODO
    if (controller != null) {
      addAWTListeners(controller);
      configureKeyMap(controller);
    }
    this.selectedWalls = new ArrayList<Wall>();
    this.shapes = new HashMap<Wall,Shape>();
    
    for (Wall wall : home.getWalls()) {
      updateWallShape(wall);
    }
    home.addWallListener(new WallListener () {
      public void wallChanged(WallEvent ev) {
        Wall wall = ev.getWall();
        // Fifth, update joined walls to an updated wall
        switch (ev.getType()) {
          case ADD :
          case UPDATE :
            updateWallShape(wall);
            break;
          case DELETE :
            shapes.remove(wall);
            break;
        }
        
        wallsArea = null;        
        // First to third step, generate a simple repaint
        repaint();
      }
    });

    setFocusable(true);
    setOpaque(true);
  }

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
    addComponentListener(new ComponentAdapter () {
      @Override
      public void componentResized(ComponentEvent ev) {
        controller.resizeComponent(getWidth(), getHeight());
      }
    });
  }

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
   * Returns the scale used to display the plan in component width.
   */
  public float getScale() {
    // TODO
    return 0.5f;
  }

  /**
   * Sets the scale used to display the plan in component width.
   */
  public void setScale(float scale) {
    // TODO Auto-generated method stub
  }

  public void setCursor(PlanController.Mode mode) {
    if (mode == PlanController.Mode.WALL_CREATION) {
      setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    } else {
      setCursor(Cursor.getDefaultCursor());
    }
  }
  
  /**
   * Returns the selected walls in plan.
   */
  public List<Wall> getSelectedWalls() {
    // TODO Auto-generated method stub
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
  public void setRectangleFeedback(int x, int y, int width, int height) {
    // Caution : Rectangle2D doesn't support negative width and height
    this.rectangleFeedback = new Rectangle2D.Float(convertPixelXToModel(x), convertPixelYToModel(y), 0, 0);
    this.rectangleFeedback.add(convertPixelXToModel(x) + width / getScale(), convertPixelYToModel(y) + height / getScale());
    repaint();
  }
  
  /**
   * Deletes feed back.
   */
  public void deleteFeedback() {
    this.rectangleFeedback = null;
    repaint();
  }

  // TODO make them private ?
  public float convertPixelDistanceToModel(int pixelDelta) {
    return pixelDelta / getScale();
  }
  
  public float convertPixelXToModel(int pixelX) {
    return (pixelX - 20) / getScale();
  }

  public float convertPixelYToModel(int pixelY) {
    return (pixelY - 20) / getScale();
  }

  public int convertModelXToPixel(float x) {
    return Math.round(x * getScale()) + 20;
  }

  public int convertModelYToPixel(float y) {
    return Math.round(y * getScale()) + 20;
  }
  
  /**
   * Returns <code>true</code> if <code>wall</code> contains 
   * the point at (<code>x</code>, <code>y</code>)
   * with a given <code>margin</code>.
   */
  public boolean containsWallAt(Wall wall, int x, int y, float margin) {
    // First and second, ignore intersection
//    return false;

    float modelX = convertPixelXToModel(x);
    float modelY = convertPixelYToModel(y);
    float modelMargin = margin / getScale();
    return shapes.get(wall).intersects(
        new Rectangle2D.Float(modelX - modelMargin, modelY - modelMargin, 2 * modelMargin, 2 * modelMargin));
  }
  
  /**
   * Returns <code>true</code> if <code>wall</code> end line contains 
   * the point at (<code>x</code>, <code>y</code>)
   * with a given <code>margin</code>.
   */
  public boolean wallEndLineContains(Wall wall, int x, int y, float margin) {
    // First and second, ignore intersection
//    return false;

    float modelX = convertPixelXToModel(x);
    float modelY = convertPixelYToModel(y);
    float modelMargin = margin / getScale();
    // Compute a line with end points of wall shape 
    float [][] wallPoints = getWallPoints(wall);
    Line2D endLine = new Line2D.Float(wallPoints [1][0], wallPoints [1][1], 
        wallPoints [2][0], wallPoints [2][1]);
    return endLine.intersects(
       new Rectangle2D.Float(modelX - modelMargin, modelY - modelMargin, 2 * modelMargin, 2 * modelMargin));
  }
  
  /**
   * Returns <code>true</code> if <code>wall</code> start line contains 
   * the point at (<code>x</code>, <code>y</code>)
   * with a given <code>margin</code>.
   */
  public boolean wallStartLineContains(Wall wall, int x, int y, float margin) {
    // First and second, ignore intersection
//    return false;

    float modelX = convertPixelXToModel(x);
    float modelY = convertPixelYToModel(y);
    float modelMargin = margin / getScale();
    // Compute a line with end points of wall shape 
    float [][] wallPoints = getWallPoints(wall);
    Line2D endLine = new Line2D.Float(wallPoints [0][0], wallPoints [0][1], 
        wallPoints [3][0], wallPoints [3][1]);
    return endLine.intersects(
       new Rectangle2D.Float(modelX - modelMargin, modelY - modelMargin, 2 * modelMargin, 2 * modelMargin));
  }
  
  /**
   * Returns <code>true</code> if <code>wall</code> intersects
   * with the horizontal rectangle which opposite corners are at points
   * (<code>x0</code>, <code>y0</code>) and (<code>x1</code>, <code>y1</code>).
   */
  public boolean wallIntersects(Wall wall, int x0, int y0, int x1, int y1) {
    // First and second, ignore intersection
//    return false;
    
    Rectangle2D rectangle = new Rectangle2D.Float(convertPixelXToModel(x0), convertPixelYToModel(y0), 0, 0);
    rectangle.add(convertPixelXToModel(x1), convertPixelYToModel(y1));
    return shapes.get(wall).intersects(rectangle);
  }

  @Override
  protected void paintComponent(Graphics g) {
    Color backgroundColor = UIManager.getDefaults().getColor("window");
    if (isOpaque()) {
      g.setColor(backgroundColor);
      g.fillRect(0, 0, getWidth(), getHeight());
      g.setColor(getForeground());
    }
    // TODO remove
    {
    Graphics2D g2D = (Graphics2D)g.create();
    g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2D.setPaint(new GradientPaint(0, 0, Color.WHITE, 100, 100, Color.RED));
    g2D.draw(new Line2D.Float(0, 0, 100, 100));
    }
    
    Graphics2D g2D = (Graphics2D)g.create();
    g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2D.translate(20, 20);
    g2D.scale(getScale(), getScale());
    
    // First, try with line thickness
//    for (Wall wall : this.home.getWalls()) {
//      Line2D line = new Line2D.Float(wall.getStartX(), wall.getStartY(), wall.getEndX(), wall.getEndY());
//      g2D.setStroke(new BasicStroke(wall.getThickness()));
//      g2D.draw(line);
//    }

      // Second, try with a rectangular shape built on the fly 
//    for (Wall wall : this.home.getWalls()) {
//      g2D.draw(getShape(wall));
//    }
    
    // Third, try with a collection of shapes
//    for (Shape path : this.shapes.values()) {
//      g2D.draw(path);
//    }
        
    // Forth, manage selected shapes and rectangle feedback
    Stroke surroundingSelectionStroke = new BasicStroke(6 / getScale(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    Stroke onePixelThickStroke = new BasicStroke(1 / getScale());
    Stroke threePixelThickStroke = new BasicStroke(2 / getScale());
    Color selectionColor = UIManager.getDefaults().getColor("textHighlight");
    
    float gridIncrement = 10;
    float mainGridIncrement = 100;
    if (this.preferences.getUnit() == UserPreferences.Unit.INCH) {
      gridIncrement = 2.54f;
      mainGridIncrement = gridIncrement * 12;
    }
    Insets insets = getInsets();
    float xMin = convertPixelXToModel(insets.left);
    float xMax = convertPixelXToModel(getWidth() - insets.left - insets.right);
    float yMin = convertPixelYToModel(insets.top);
    float yMax = convertPixelYToModel(getHeight() - insets.bottom - insets.top);
    g2D.setColor(Color.LIGHT_GRAY);
    for (float x = 0; x < xMax; x += gridIncrement) {
      if (x % mainGridIncrement == 0f) {
        g2D.setStroke(threePixelThickStroke);
      } else {
        g2D.setStroke(onePixelThickStroke);        
      }
      g2D.draw(new Line2D.Float(x, yMin, x, yMax)); 
    }
    for (float y = 0; y < yMax; y += gridIncrement) {
      if (y % mainGridIncrement == 0f) {
        g2D.setStroke(threePixelThickStroke);
      } else {
        g2D.setStroke(onePixelThickStroke);        
      }
      g2D.draw(new Line2D.Float(xMin, y, xMax, y)); 
    }

//    for (Wall wall : this.shapes.keySet()) {
//      Shape shape = this.shapes.get(wall);
//      if (selectedWalls.contains(wall)) {
//        g2D.setColor(new Color(selectionColor.getRed(), selectionColor.getGreen(), selectionColor.getBlue(), 64));
//        // Always use a 6 pixel wide border for selection
//        g2D.setStroke(surroundingSelectionStroke);
//        g2D.draw(shape);
//      } 
//      g2D.setColor(getForeground());
//      // Always draw shape with a 1 pixel wide border
//      g2D.setStroke(onePixelThickStroke);
//      g2D.draw(shape);
//    }

    if (this.wallsArea == null) {
      this.wallsArea = new Area();
      for (Wall wall : this.shapes.keySet()) {
        this.wallsArea.add(new Area (this.shapes.get(wall)));
      }
    }
    BufferedImage textureImage = new BufferedImage(10, 10, BufferedImage.OPAQUE);
    Graphics2D textureGraphics = (Graphics2D)textureImage.getGraphics();
    textureGraphics.setColor(backgroundColor);
    textureGraphics.fillRect(0, 0, textureImage.getWidth(), textureImage.getHeight());
    textureGraphics.setColor(getForeground());
    textureGraphics.drawLine(0, 0, textureImage.getWidth(), textureImage.getHeight());
    g2D.setPaint(new TexturePaint(textureImage, new Rectangle2D.Float(0, 0, textureImage.getWidth(), textureImage.getHeight())));
    g2D.fill(this.wallsArea);
    // Draw selection
    for (Wall wall : this.shapes.keySet()) {
      Shape shape = this.shapes.get(wall);
      if (selectedWalls.contains(wall)) {
        g2D.setColor(new Color(selectionColor.getRed(), selectionColor.getGreen(), selectionColor.getBlue(), 64));
        // Always use a 6 pixel wide border for selection
        g2D.setStroke(surroundingSelectionStroke);
        g2D.draw(shape);
      } 
    }
    // Draw wallsArea surround line
    g2D.setColor(getForeground());
    // Always draw shape with a 1 pixel wide border
    g2D.setStroke(onePixelThickStroke);
    g2D.draw(wallsArea);
    
    // Paint rectangle feeback if any
    if (this.rectangleFeedback != null) {
      g2D.setColor(new Color(selectionColor.getRed(), selectionColor.getGreen(), selectionColor.getBlue(), 16));
      g2D.fill(this.rectangleFeedback);
      g2D.setColor(selectionColor);
      g2D.setStroke(onePixelThickStroke);
      g2D.draw(this.rectangleFeedback);
    }
    
    g2D.dispose();
  }

  /**
   * Updates the shape of a <code>wall</code> as it's drawn at screen and used to compute intersections. 
   */
  private void updateWallShape(Wall wall) {
    // Second to forth, generate a rectangular shape for a wall from lines at +- half thickness
//    double angle = Math.atan2(wall.getEndY() - wall.getStartY(), wall.getEndX() - wall.getStartX());
//    float delta = wall.getThickness() / 2;
//    float sin = (float)Math.sin(angle) * delta;
//    float cos = (float)Math.cos(angle) * delta;
//    GeneralPath wallPath = new GeneralPath();
//    wallPath.moveTo(wall.getStartX() + sin, wall.getStartY() - cos);
//    wallPath.lineTo(wall.getEndX() + sin, wall.getEndY() - cos);
//    wallPath.lineTo(wall.getEndX() - sin, wall.getEndY() + cos);
//    wallPath.lineTo(wall.getStartX() - sin, wall.getStartY() + cos);
//    wallPath.closePath();
//    return wallPath;

    // Fifth, compute the exact shape of a wall if it's joined to another one at its end point or at its start point
//    double angle = Math.atan2(wall.getEndY() - wall.getStartY(), wall.getEndX() - wall.getStartX());
//    float delta = wall.getThickness() / 2;
//    float sin = (float)Math.sin(angle) * delta;
//    float cos = (float)Math.cos(angle) * delta;
//    float upperLineX1 = wall.getStartX() + sin;
//    float upperLineY1 = wall.getStartY() - cos;
//    float upperLineX2 = wall.getEndX() + sin;
//    float upperLineY2 = wall.getEndY() - cos;
//    float lowerLineX1 = wall.getStartX() - sin;
//    float lowerLineY1 = wall.getStartY() + cos;
//    float lowerLineX2 = wall.getEndX() - sin;
//    float lowerLineY2 = wall.getEndY() + cos;
//    Wall wallAtEnd = wall.getWallAtEnd();
//    if (wallAtEnd != null) {
//      Shape wallShape = shapes.get(wallAtEnd);
//      PathIterator it = wallShape.getPathIterator(null);
//      float [][] wallAtEndPoints = new float[4][2];
//      for (int i = 0; i < wallAtEndPoints.length; i++) {
//        it.currentSegment(wallAtEndPoints [i]);
//        it.next();
//      }
//      if (wallAtEnd.getWallAtStart() == wall) {
//        Point2D intersectionPoint = getIntersection(upperLineX1, upperLineY1, upperLineX2, upperLineY2,
//            wallAtEndPoints [0][0], wallAtEndPoints [0][1], wallAtEndPoints [1][0], wallAtEndPoints [1][1]);
//        upperLineX2 = (float)intersectionPoint.getX();
//        upperLineY2 = (float)intersectionPoint.getY();
//        intersectionPoint = getIntersection(lowerLineX1, lowerLineY1, lowerLineX2, lowerLineY2,
//            wallAtEndPoints [2][0], wallAtEndPoints [2][1], wallAtEndPoints [3][0], wallAtEndPoints [3][1]);
//        lowerLineX2 = (float)intersectionPoint.getX();
//        lowerLineY2 = (float)intersectionPoint.getY();
//      } else if (wallAtEnd.getWallAtEnd() == wall) {
//        Point2D intersectionPoint = getIntersection(upperLineX1, upperLineY1, upperLineX2, upperLineY2,
//            wallAtEndPoints [2][0], wallAtEndPoints [2][1], wallAtEndPoints [3][0], wallAtEndPoints [3][1]);
//        upperLineX2 = (float)intersectionPoint.getX();
//        upperLineY2 = (float)intersectionPoint.getY();
//        intersectionPoint = getIntersection(lowerLineX1, lowerLineY1, lowerLineX2, lowerLineY2,
//            wallAtEndPoints [0][0], wallAtEndPoints [0][1], wallAtEndPoints [1][0], wallAtEndPoints [1][1]);
//        lowerLineX2 = (float)intersectionPoint.getX();
//        lowerLineY2 = (float)intersectionPoint.getY();
//      }
//    }
//
//    Wall wallAtStart = wall.getWallAtStart();
//    if (wallAtStart != null) {
//      Shape wallShape = shapes.get(wallAtStart);
//      PathIterator it = wallShape.getPathIterator(null);
//      float [][] wallAtStartPoints = new float[4][2];
//      for (int i = 0; i < wallAtStartPoints.length; i++) {
//        it.currentSegment(wallAtStartPoints [i]);
//        it.next();
//      }
//      if (wallAtStart.getWallAtEnd() == wall) {
//        Point2D intersectionPoint = getIntersection(upperLineX1, upperLineY1, upperLineX2, upperLineY2,
//            wallAtStartPoints [0][0], wallAtStartPoints [0][1], wallAtStartPoints [1][0], wallAtStartPoints [1][1]);
//        upperLineX1 = (float)intersectionPoint.getX();
//        upperLineY1 = (float)intersectionPoint.getY();
//        intersectionPoint = getIntersection(lowerLineX1, lowerLineY1, lowerLineX2, lowerLineY2,
//            wallAtStartPoints [2][0], wallAtStartPoints [2][1], wallAtStartPoints [3][0], wallAtStartPoints [3][1]);
//        lowerLineX1 = (float)intersectionPoint.getX();
//        lowerLineY1 = (float)intersectionPoint.getY();
//      } else if (wallAtStart.getWallAtStart() == wall) {
//        Point2D intersectionPoint = getIntersection(upperLineX1, upperLineY1, upperLineX2, upperLineY2,
//            wallAtStartPoints [2][0], wallAtStartPoints [2][1], wallAtStartPoints [3][0], wallAtStartPoints [3][1]);
//        upperLineX1 = (float)intersectionPoint.getX();
//        upperLineY1 = (float)intersectionPoint.getY();
//        intersectionPoint = getIntersection(lowerLineX1, lowerLineY1, lowerLineX2, lowerLineY2,
//            wallAtStartPoints [0][0], wallAtStartPoints [0][1], wallAtStartPoints [1][0], wallAtStartPoints [1][1]);
//        lowerLineX1 = (float)intersectionPoint.getX();
//        lowerLineY1 = (float)intersectionPoint.getY();
//      }
//    }

    // Sixth, optimize
    double angle = Math.atan2(wall.getYEnd() - wall.getYStart(), wall.getXEnd() - wall.getXStart());
    float delta = wall.getThickness() / 2;
    float sin = (float)Math.sin(angle) * delta;
    float cos = (float)Math.cos(angle) * delta;
    float [][] wallPoints = {{wall.getXStart() + sin, wall.getYStart() - cos},
                             {wall.getXEnd() + sin,   wall.getYEnd() - cos},
                             {wall.getXEnd() - sin,   wall.getYEnd() + cos},
                             {wall.getXStart() - sin, wall.getYStart() + cos}};
    Wall wallAtEnd = wall.getWallAtEnd();
    if (wallAtEnd != null) {
      float [][] wallAtEndPoints = getWallPoints(wallAtEnd);
      if (wallAtEnd.getWallAtStart() == wall) {
        computeIntersection(wallPoints [1], wallAtEndPoints [0],
            wallPoints [0], wallPoints [1],
            wallAtEndPoints [0], wallAtEndPoints [1]);
        computeIntersection(wallPoints [2], wallAtEndPoints [3], 
            wallPoints [2], wallPoints [3],
            wallAtEndPoints [2], wallAtEndPoints [3]);
        
      } else if (wallAtEnd.getWallAtEnd() == wall) {
        computeIntersection(wallPoints [1], wallAtEndPoints [2],
            wallPoints [0], wallPoints [1],
            wallAtEndPoints [2], wallAtEndPoints [3]);
        computeIntersection(wallPoints [2], wallAtEndPoints [1], 
            wallPoints [2], wallPoints [3],
            wallAtEndPoints [0], wallAtEndPoints [1]);
      }

      this.shapes.put(wallAtEnd, getRectangleShape(wallAtEndPoints));
    }

    Wall wallAtStart = wall.getWallAtStart();
    if (wallAtStart != null) {
      float [][] wallAtStartPoints = getWallPoints(wallAtStart);
      if (wallAtStart.getWallAtEnd() == wall) {
        computeIntersection(wallPoints [0], wallAtStartPoints [1],
            wallPoints [0], wallPoints [1],
            wallAtStartPoints [0], wallAtStartPoints [1]);
        computeIntersection(wallPoints [3], wallAtStartPoints [2],
            wallPoints [2], wallPoints [3],
            wallAtStartPoints [2], wallAtStartPoints [3]);
      } else if (wallAtStart.getWallAtStart() == wall) {
        computeIntersection(wallPoints [0], wallAtStartPoints [3], 
            wallPoints [0], wallPoints [1],
            wallAtStartPoints [2], wallAtStartPoints [3]);
        computeIntersection(wallPoints [3], wallAtStartPoints [0],
            wallPoints [2], wallPoints [3],
            wallAtStartPoints [0], wallAtStartPoints [1]);
      }

      this.shapes.put(wallAtStart, getRectangleShape(wallAtStartPoints));
    }

    this.shapes.put(wall, getRectangleShape(wallPoints));
  }


  /**
   * Returns the shape matching the 4 points in <code>wallPoints</code>.
   */
  private GeneralPath getRectangleShape(float [][] wallPoints) {
    GeneralPath wallPath = new GeneralPath();
    wallPath.moveTo(wallPoints [0][0], wallPoints [0][1]);
    wallPath.lineTo(wallPoints [1][0], wallPoints [1][1]);
    wallPath.lineTo(wallPoints [2][0], wallPoints [2][1]);
    wallPath.lineTo(wallPoints [3][0], wallPoints [3][1]);
    wallPath.closePath();
    return wallPath;
  }

  /**
   * Returns the points of a wall shape.
   */
  private float [][] getWallPoints(Wall wall) {
    Shape wallShape = this.shapes.get(wall);
    float [][] wallPoints = new float[4][2];
    PathIterator it = wallShape.getPathIterator(null);
    for (int i = 0; i < wallPoints.length; i++) {
      it.currentSegment(wallPoints [i]);
      it.next();
    }
    return wallPoints;
  }
  
  /**
   * Compute the intersection between the line that joins <code>point1</code> to <code>point2</code>
   * and the line that joins <code>point3</code> and <code>point4</code>, and stores the result 
   * <code>pointLine1</code> and <code>pointLine2</code>.
   */
  private void computeIntersection(float [] pointLine1, float [] pointLine2,
                                   float [] point1, float [] point2, 
                                   float [] point3, float [] point4) {
    float x = pointLine1 [0];
    float y = pointLine1 [1];
    float alpha1 = (point2 [1] - point1 [1]) / (point2 [0] - point1 [0]);
    float beta1  = point2 [1] - alpha1 * point2 [0];
    float alpha2 = (point4 [1] - point3 [1]) / (point4 [0] - point3 [0]);
    float beta2  = point4 [1] - alpha2 * point4 [0];
    if (alpha1 != alpha2) {
      if (point1 [0] != point2 [0] 
          && point3 [0] != point4 [0]) {
        x = (beta2 - beta1) / (alpha1 - alpha2);
        y = alpha1 * x + beta1;
      } else if (point1 [0] == point2 [0]) {
        x = point1 [0];
        y = alpha2 * x + beta2;
      } else if (point3 [0] == point4 [0]) {
        x = point3 [0];
        y = alpha1 * x + beta1;
      }
    }
    pointLine1 [0] = x;
    pointLine1 [1] = y;
    pointLine2 [0] = x;
    pointLine2 [1] = y;
  }
}
