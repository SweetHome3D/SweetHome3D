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
import java.awt.EventQueue;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JToolTip;
import javax.swing.JViewport;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.MouseInputAdapter;

import com.eteks.sweethome3d.model.BackgroundImage;
import com.eteks.sweethome3d.model.FurnitureEvent;
import com.eteks.sweethome3d.model.FurnitureListener;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
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
public class PlanComponent extends JComponent implements Scrollable {
  private enum ActionType {DELETE_SELECTION, ESCAPE, 
    MOVE_SELECTION_LEFT, MOVE_SELECTION_UP, MOVE_SELECTION_DOWN, MOVE_SELECTION_RIGHT,
    TOGGLE_MAGNTISM_ON, TOGGLE_MAGNTISM_OFF}
  
  private static final float MARGIN = 40;  
  private Home               home;
  private UserPreferences    preferences;
  private float              scale  = 0.5f;

  private JComponent         horizontalRuler;
  private JComponent         verticalRuler;
  
  private Rectangle2D        rectangleFeedback;
  private Wall               wallAlignmentFeedback;
  private Point2D            wallLocationFeeback;
  private Rectangle2D        planBoundsCache;
  private BufferedImage      backgroundImageCache;
  private boolean            selectionScrollUpdated;
  private Cursor             rotationCursor;
  private Cursor             resizeCursor;
  private JToolTip           toolTip;
  private JWindow            toolTipWindow;
  private boolean            resizeIndicatorVisible;
  
  private static final GeneralPath FURNITURE_ROTATION_INDICATOR;
  private static final GeneralPath FURNITURE_RESIZE_INDICATOR;
  private static final GeneralPath WALL_ORIENTATION_INDICATOR;
  private static final Shape       WALL_POINT;
  private static final GeneralPath WALL_RESIZE_INDICATOR;
  
  static {
    // Create a path that draws an round arrow used as a rotation indicator 
    // at top left vertex of a piece of furniture
    FURNITURE_ROTATION_INDICATOR = new GeneralPath();
    FURNITURE_ROTATION_INDICATOR.append(new Ellipse2D.Float(-1.5f, -1.5f, 3, 3), false);
    FURNITURE_ROTATION_INDICATOR.append(new Arc2D.Float(-8, -8, 16, 16, 45, 180, Arc2D.OPEN), false);
    FURNITURE_ROTATION_INDICATOR.moveTo(2.66f, -5.66f);
    FURNITURE_ROTATION_INDICATOR.lineTo(5.66f, -5.66f);
    FURNITURE_ROTATION_INDICATOR.lineTo(4f, -8.3f);
    
    // Create a path used as a size indicator 
    // at bottom right vertex of a piece of furniture
    FURNITURE_RESIZE_INDICATOR = new GeneralPath();
    FURNITURE_RESIZE_INDICATOR.append(new Rectangle2D.Float(-1.5f, -1.5f, 3f, 3f), false);
    FURNITURE_RESIZE_INDICATOR.moveTo(5, -3);
    FURNITURE_RESIZE_INDICATOR.lineTo(6, -3);
    FURNITURE_RESIZE_INDICATOR.lineTo(6, 6);
    FURNITURE_RESIZE_INDICATOR.lineTo(-3, 6);
    FURNITURE_RESIZE_INDICATOR.lineTo(-3, 5);
    FURNITURE_RESIZE_INDICATOR.moveTo(3.5f, 3.5f);
    FURNITURE_RESIZE_INDICATOR.lineTo(8, 8);
    FURNITURE_RESIZE_INDICATOR.moveTo(6, 8.5f);
    FURNITURE_RESIZE_INDICATOR.lineTo(9, 9);
    FURNITURE_RESIZE_INDICATOR.lineTo(8.5f, 6);
    
    // Create a path used an orientation indicator
    // at start and end points of a selected wall
    WALL_ORIENTATION_INDICATOR = new GeneralPath();
    WALL_ORIENTATION_INDICATOR.moveTo(-4, -4);
    WALL_ORIENTATION_INDICATOR.lineTo(4, 0);
    WALL_ORIENTATION_INDICATOR.lineTo(-4, 4);

    WALL_POINT = new Ellipse2D.Float(-3, -3, 6, 6);

    // Create a path used as a size indicator 
    // at start and end points of a selected wall
    WALL_RESIZE_INDICATOR = new GeneralPath();
    WALL_RESIZE_INDICATOR.moveTo(5, -2);
    WALL_RESIZE_INDICATOR.lineTo(5, 2);
    WALL_RESIZE_INDICATOR.moveTo(6, 0);
    WALL_RESIZE_INDICATOR.lineTo(11, 0);
    WALL_RESIZE_INDICATOR.moveTo(8.7f, -1.8f);
    WALL_RESIZE_INDICATOR.lineTo(12, 0);
    WALL_RESIZE_INDICATOR.lineTo(8.7f, 1.8f);
  }

  public PlanComponent(Home home, UserPreferences preferences,
                       PlanController controller) {
    this.home = home;
    this.preferences = preferences;
    // Set JComponent default properties
    setOpaque(true);
    // Add listeners
    addModelListeners(home, preferences);
    if (controller != null) {
      addMouseListeners(controller);
      addFocusListener(controller);
      createActions(controller);
      installKeyboardActions();
      setFocusable(true);
      setAutoscrolls(true);
    }
    this.rotationCursor = createCustomCursor("resources/rotationCursor16x16.png",
        "resources/rotationCursor32x32.png", "Rotation cursor");
    this.resizeCursor = createCustomCursor("resources/resizeCursor16x16.png",
        "resources/resizeCursor32x32.png", "Resize cursor");
    // Install default colors
    super.setForeground(UIManager.getColor("textText"));
    super.setBackground(UIManager.getColor("window"));
  }

  /**
   * Adds wall and selection listeners on this component to receive wall 
   * changes notifications from home. 
   */
  private void addModelListeners(Home home, UserPreferences preferences) {
    home.addWallListener(new WallListener () {
      public void wallChanged(WallEvent ev) {
        planBoundsCache = null;
        // Revalidate and repaint
        revalidate();
      }
    });
    home.addSelectionListener(new SelectionListener () {
      public void selectionChanged(SelectionEvent ev) {
        repaint();
      }
    });
    home.addFurnitureListener(new FurnitureListener() {
      public void pieceOfFurnitureChanged(FurnitureEvent ev) {
        planBoundsCache = null;
        // Revalidate and repaint
        revalidate();
      }
    });
    home.addPropertyChangeListener("backgroundImage", 
      new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          backgroundImageCache = null;
          repaint();
        }
      });
    preferences.addPropertyChangeListener("unit", 
      new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
          repaint();
          if (horizontalRuler != null) {
            horizontalRuler.repaint();
          }
          if (verticalRuler != null) {
            verticalRuler.repaint();
          }
        }
      });
  }

  /**
   * Revalidates and repaints this component and its rulers.
   */
  @Override
  public void revalidate() {
    super.revalidate();
    repaint();
    if (this.horizontalRuler != null) {
      this.horizontalRuler.revalidate();
      this.horizontalRuler.repaint();
    }
    if (this.verticalRuler != null) {
      this.verticalRuler.revalidate();
      this.verticalRuler.repaint();
    }
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
        mouseMoved(ev);
      }
    };
    addMouseListener(mouseListener);
    addMouseMotionListener(mouseListener);
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
   * Installs keys bound to actions. 
   */
  private void installKeyboardActions() {
    InputMap inputMap = getInputMap(WHEN_FOCUSED);
    inputMap.put(KeyStroke.getKeyStroke("DELETE"), ActionType.DELETE_SELECTION);
    inputMap.put(KeyStroke.getKeyStroke("BACK_SPACE"), ActionType.DELETE_SELECTION);
    inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), ActionType.ESCAPE);
    inputMap.put(KeyStroke.getKeyStroke("LEFT"), ActionType.MOVE_SELECTION_LEFT);
    inputMap.put(KeyStroke.getKeyStroke("UP"), ActionType.MOVE_SELECTION_UP);
    inputMap.put(KeyStroke.getKeyStroke("DOWN"), ActionType.MOVE_SELECTION_DOWN);
    inputMap.put(KeyStroke.getKeyStroke("RIGHT"), ActionType.MOVE_SELECTION_RIGHT);
    inputMap.put(KeyStroke.getKeyStroke("shift pressed SHIFT"), ActionType.TOGGLE_MAGNTISM_ON);
    inputMap.put(KeyStroke.getKeyStroke("released SHIFT"), ActionType.TOGGLE_MAGNTISM_OFF);
  }
 
  /**
   * Creates actions that calls back <code>controller</code> methods.  
   */
  private void createActions(final PlanController controller) {
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
        controller.moveSelection(this.dx / scale, this.dy / scale);
      }
    }
    // Temporary magnestism mapped to alt key
    class ToggleMagnetismAction extends AbstractAction {
      private final boolean toggle;
      
      public ToggleMagnetismAction(boolean toggle) {
        this.toggle = toggle;
      }

      public void actionPerformed(ActionEvent e) {
        controller.toggleMagnetism(this.toggle);
      }
    }
    ActionMap actionMap = getActionMap();
    actionMap.put(ActionType.DELETE_SELECTION, deleteSelectionAction);
    actionMap.put(ActionType.ESCAPE, escapeAction);
    actionMap.put(ActionType.MOVE_SELECTION_LEFT, new MoveSelectionAction(-1, 0));
    actionMap.put(ActionType.MOVE_SELECTION_UP, new MoveSelectionAction(0, -1));
    actionMap.put(ActionType.MOVE_SELECTION_DOWN, new MoveSelectionAction(0, 1));
    actionMap.put(ActionType.MOVE_SELECTION_RIGHT, new MoveSelectionAction(1, 0));
    actionMap.put(ActionType.TOGGLE_MAGNTISM_ON, new ToggleMagnetismAction(true));
    actionMap.put(ActionType.TOGGLE_MAGNTISM_OFF, new ToggleMagnetismAction(false));
  }

  /**
   * Create custom rotation cursor with a hot spot point at center of cursor.
   */ 
  private Cursor createCustomCursor(String smallCursorImageResource, 
                                    String largeCursorImageResource,
                                    String cursorName) {
    // Retrieve system cursor size
    Dimension cursorSize = getToolkit().getBestCursorSize(16, 16);
    String cursorImageResource;
    // If returned cursor size is 0, system doesn't support custom cursor  
    if (cursorSize.width == 0) {      
      return Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);      
    } else {
      // Use a different cursor image depending on system cursor size 
      if (cursorSize.width > 16) {
        cursorImageResource = largeCursorImageResource;
      } else {
        cursorImageResource = smallCursorImageResource;
      }
      try {
        // Read cursor image
        BufferedImage cursorImage = 
          ImageIO.read(getClass().getResource(cursorImageResource));
        // Create custom cursor from image
        return getToolkit().createCustomCursor(cursorImage, 
            new Point(cursorSize.width / 2, cursorSize.height / 2),
            cursorName);
      } catch (IOException ex) {
        throw new IllegalArgumentException("Unknown resource " + cursorImageResource);
      }
    }
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
      for (HomePieceOfFurniture piece : home.getFurniture()) {
        if (piece.isVisible()) {
          for (float [] point : piece.getPoints()) {
            this.planBoundsCache.add(point [0], point [1]);
          }
        }
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
    g2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    // Paint component contents
    paintBackgroundImage(g2D);
    paintGrid(g2D);
    paintContent(g2D);   
    g2D.dispose();
  }

  /**
   * Fills the background. 
   */
  private void paintBackground(Graphics2D g2D) {
    if (isOpaque()) {
      g2D.setColor(getBackground());
      g2D.fillRect(0, 0, getWidth(), getHeight());
    }
  }

  /**
   * Paints background image.
   */
  private void paintBackgroundImage(Graphics2D g2D) {
    BackgroundImage backgroundImage = this.home.getBackgroundImage();
    if (backgroundImage != null) {
      if (this.backgroundImageCache == null) {
        InputStream contentStream = null;
        try {
          contentStream = backgroundImage.getImage().openStream();
          this.backgroundImageCache = ImageIO.read(contentStream);
          contentStream.close();
        } catch (IOException ex) {
          this.backgroundImageCache = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
          // Ignore exceptions, the user may know its background image is incorrect 
          // if he tries to modify the background image
        } 
      }
      // Paint image at specified scale
      AffineTransform previousTransform = g2D.getTransform();
      g2D.translate(-backgroundImage.getXOrigin(), -backgroundImage.getYOrigin());
      g2D.scale(backgroundImage.getScale(), backgroundImage.getScale());
      g2D.drawImage(this.backgroundImageCache, 0, 0, this);
      g2D.setTransform(previousTransform);
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

    g2D.setColor(UIManager.getColor("controlShadow"));
    g2D.setStroke(new BasicStroke(0.5f / this.scale));
    // Draw vertical lines
    for (float x = (int) (xMin / gridSize) * gridSize; x < xMax; x += gridSize) {
      g2D.draw(new Line2D.Float(x, yMin, x, yMax));
    }
    // Draw horizontal lines
    for (float y = (int) (yMin / gridSize) * gridSize; y < yMax; y += gridSize) {
      g2D.draw(new Line2D.Float(xMin, y, xMax, y));
    }

    if (mainGridSize != gridSize) {
      g2D.setStroke(new BasicStroke(1.5f / this.scale,
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
   * Paints plan items.
   */
  private void paintContent(Graphics2D g2D) {
    List<Object> selectedItems = this.home.getSelectedItems();
    Color selectionColor = UIManager.getColor("textHighlight");
    if (!System.getProperty("os.name").startsWith("Mac OS X")) {
      // On systems different from Mac OS X, take a darker color
      // (Note : on Mac OS X, using SystemColor.textHighlight and 
      // a color built from its RGB gives a different color !)
      selectionColor = UIManager.getColor("textHighlight").darker();
    } 
    Paint selectionOutlinePaint = new Color(selectionColor.getRed(), selectionColor.getGreen(), 
        selectionColor.getBlue(), 128);
    Stroke selectionOutlineStroke = new BasicStroke(6 / this.scale, 
        BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND); 
    Stroke locationFeedbackStroke = new BasicStroke(
        1 / this.scale, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 0, 
        new float [] {20 / this.scale, 5 / this.scale, 5 / this.scale, 5 / this.scale}, 4 / this.scale);
    
    paintWalls(g2D, selectedItems, selectionOutlinePaint, selectionOutlineStroke, selectionColor);
    paintFurniture(g2D, selectedItems, selectionOutlinePaint, selectionOutlineStroke, selectionColor);
    paintWallAlignmentFeedback(g2D, selectionColor, locationFeedbackStroke);
    paintRectangleFeedback(g2D, selectionColor);
  }

  /**
   * Paints walls. 
   */
  private void paintWalls(Graphics2D g2D, List<Object> selectedItems,   
                          Paint selectionOutlinePaint, Stroke selectionOutlineStroke, 
                          Paint indicatorPaint) {
    Shape wallsArea = getWallsArea(this.home.getWalls());
    // Fill walls area
    g2D.setPaint(getWallPaint());
    g2D.fill(wallsArea);
    
    Stroke indicatorStroke = new BasicStroke(2f);  
    for (Object item : selectedItems) {
      if (item instanceof Wall) {
        Wall wall = (Wall)item;
        // Draw selection border
        g2D.setPaint(selectionOutlinePaint);
        g2D.setStroke(selectionOutlineStroke);
        g2D.draw(getShape(wall.getPoints()));
        
        AffineTransform previousTransform = g2D.getTransform();
        // Draw start point of the wall
        g2D.translate(wall.getXStart(), wall.getYStart());
        g2D.scale(1 / this.scale, 1 / this.scale);
        g2D.setPaint(indicatorPaint);         
        g2D.setStroke(indicatorStroke);
        g2D.fill(WALL_POINT);
        
        double wallAngle = Math.atan2(wall.getYEnd() - wall.getYStart(), 
            wall.getXEnd() - wall.getXStart());
        double distanceAtScale = Point2D.distance(wall.getXStart(), wall.getYStart(), 
            wall.getXEnd(), wall.getYEnd()) * this.scale;
        g2D.rotate(wallAngle);
        // If the distance between start and end points is < 30
        if (distanceAtScale < 30) { 
          // Draw only one orientation indicator between the two points
          g2D.translate(distanceAtScale / 2, 0);
        } else {
          // Draw orientation indicator at start of the wall
          g2D.translate(8, 0);
        }
        g2D.draw(WALL_ORIENTATION_INDICATOR);
        g2D.setTransform(previousTransform);
        
        // Draw end point of the wall
        g2D.translate(wall.getXEnd(), wall.getYEnd());
        g2D.scale(1 / this.scale, 1 / this.scale);
        g2D.fill(WALL_POINT);
        if (distanceAtScale >= 30) { 
          // Draw orientation indicator at end of the wall
          g2D.rotate(wallAngle);
          g2D.translate(-10, 0);
          g2D.draw(WALL_ORIENTATION_INDICATOR);
        }        
        g2D.setTransform(previousTransform);
      }  
    }
    // Draw walls area
    g2D.setPaint(getForeground());
    g2D.setStroke(new BasicStroke(1.5f / this.scale));
    g2D.draw(wallsArea);
    
    // Paint resize indicator of selected wall
    if (selectedItems.size() == 1 
        && selectedItems.get(0) instanceof Wall) {
      paintWallResizeIndicator(g2D, (Wall)selectedItems.get(0), indicatorPaint);
    }
  }

  /**
   * Paints resize indicator on <code>wall</code>.
   */
  public void paintWallResizeIndicator(Graphics2D g2D, Wall wall,
                                       Paint indicatorPaint) {
    if (this.resizeIndicatorVisible) {
      g2D.setPaint(indicatorPaint);
      g2D.setStroke(new BasicStroke(1.5f));

      double wallAngle = Math.atan2(wall.getYEnd() - wall.getYStart(), 
          wall.getXEnd() - wall.getXStart());
      
      AffineTransform previousTransform = g2D.getTransform();
      // Draw resize indicator at wall start point
      g2D.translate(wall.getXStart(), wall.getYStart());
      g2D.scale(1 / this.scale, 1 / this.scale);
      g2D.rotate(wallAngle + Math.PI);
      g2D.draw(WALL_RESIZE_INDICATOR);
      g2D.setTransform(previousTransform);
      
      // Draw resize indicator at wall end point
      g2D.translate(wall.getXEnd(), wall.getYEnd());
      g2D.scale(1 / this.scale, 1 / this.scale);
      g2D.rotate(wallAngle);
      g2D.draw(WALL_RESIZE_INDICATOR);
      g2D.setTransform(previousTransform);
    }
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
   * Returns the <code>Paint</code> object used to fill walls.
   */
  private Paint getWallPaint() {
    BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
    Graphics2D imageGraphics = (Graphics2D)image.getGraphics();
    // Create an image displaying a line in its diagonal
    imageGraphics.setPaint(getBackground());
    imageGraphics.fillRect(0, 0, 10, 10);
    imageGraphics.setColor(getForeground());
    imageGraphics.drawLine(0, 9, 9, 0);
    imageGraphics.dispose();
    return new TexturePaint(image, 
        new Rectangle2D.Float(0, 0, 10 / this.scale, 10 / this.scale));
  }
  
  /**
   * Paints home furniture.
   */
  public void paintFurniture(Graphics2D g2D, List<Object> selectedItems,  
                             Paint selectionOutlinePaint, Stroke selectionOutlineStroke, 
                             Paint indicatorPaint) {
    BasicStroke pieceBorderStroke = new BasicStroke(1f / this.scale);
    // Draw furniture
    for (HomePieceOfFurniture piece : this.home.getFurniture()) {
      if (piece.isVisible()) {
        Shape pieceShape = getShape(piece.getPoints());
        // Fill piece area
        g2D.setPaint(getBackground());
        g2D.fill(pieceShape);
        // Draw its icon
        paintPieceOfFurnitureIcon(g2D, piece);
        
        if (selectedItems.contains(piece)) {
          // Draw selection border
          g2D.setPaint(selectionOutlinePaint);
          g2D.setStroke(selectionOutlineStroke);
          g2D.draw(pieceShape);
        }        
        // Draw its border
        g2D.setPaint(getForeground());
        g2D.setStroke(pieceBorderStroke);
        g2D.draw(pieceShape);
        
        if (selectedItems.size() == 1 
            && selectedItems.get(0) == piece) {
          paintPieceOFFurnitureResizeIndicator(g2D, 
              (HomePieceOfFurniture)selectedItems.get(0), indicatorPaint);
        }
      }
    }
  }

  /**
   * Paints <code>piece</code> icon with <code>g2D</code>.
   */
  private void paintPieceOfFurnitureIcon(Graphics2D g2D, HomePieceOfFurniture piece) {
    AffineTransform previousTransform = g2D.getTransform();
    // Get piece icon
    Icon icon = IconManager.getInstance().getIcon(piece.getIcon(), 128, this);
    // Translate to piece center
    g2D.translate(piece.getX(), piece.getY());
    // Scale icon to fit in its area
    float minDimension = Math.min(piece.getWidth(), piece.getDepth());
    float scale = Math.min(1 / this.scale, minDimension / icon.getIconHeight());
    g2D.scale(scale, scale);
    // Paint piece icon
    icon.paintIcon(this, g2D, -icon.getIconWidth() / 2, -icon.getIconHeight() / 2);
    // Revert g2D transformation to previous value
    g2D.setTransform(previousTransform);
  }

  /**
   * Paints resize and rotation indicators on <code>piece</code>.
   */
  public void paintPieceOFFurnitureResizeIndicator(Graphics2D g2D, 
                                                   HomePieceOfFurniture piece,
                                                   Paint indicatorPaint) {
    if (this.resizeIndicatorVisible) {
      g2D.setPaint(indicatorPaint);
      g2D.setStroke(new BasicStroke(1.5f));
      
      AffineTransform previousTransform = g2D.getTransform();
      // Draw rotation indicator at top left vertex of the piece
      float [][] piecePoints = piece.getPoints();
      g2D.translate(piecePoints [0][0], piecePoints [0][1]);
      g2D.scale(1 / this.scale, 1 / this.scale);
      g2D.rotate(piece.getAngle());
      g2D.draw(FURNITURE_ROTATION_INDICATOR);
      g2D.setTransform(previousTransform);

      // Draw resize indicator at top left vertex of the piece
      g2D.translate(piecePoints [2][0], piecePoints [2][1]);
      g2D.scale(1 / this.scale, 1 / this.scale);
      g2D.rotate(piece.getAngle());
      g2D.draw(FURNITURE_RESIZE_INDICATOR);
      g2D.setTransform(previousTransform);
    }
  }
  
  /**
   * Paints wall location feedback.
   */
  public void paintWallAlignmentFeedback(Graphics2D g2D, 
                                        Paint feedbackPaint, Stroke feedbackStroke) {
    // Paint wall location feedback
    if (this.wallLocationFeeback != null) {
      float margin = 1f / this.scale;
      // Seach which wall start or end point is at wallLocationFeeback abcissa or ordinate
      // ignoring the start and end point of wallFeedback
      float x = (float)this.wallLocationFeeback.getX(); 
      float y = (float)this.wallLocationFeeback.getY();
      float deltaXToClosestWall = Float.POSITIVE_INFINITY;
      float deltaYToClosestWall = Float.POSITIVE_INFINITY;
      for (Wall alignedWall : this.home.getWalls()) {
        if (alignedWall != this.wallAlignmentFeedback) {
          if (Math.abs(x - alignedWall.getXStart()) < margin
              && (this.wallAlignmentFeedback == null
                  || !equalsWallPoint(alignedWall.getXStart(), alignedWall.getYStart(), this.wallAlignmentFeedback))) {
            if (Math.abs(deltaYToClosestWall) > Math.abs(y - alignedWall.getYStart())) {
              deltaYToClosestWall = y - alignedWall.getYStart();
            }
          } else if (Math.abs(x - alignedWall.getXEnd()) < margin
                    && (this.wallAlignmentFeedback == null
                        || !equalsWallPoint(alignedWall.getXEnd(), alignedWall.getYEnd(), this.wallAlignmentFeedback))) {
            if (Math.abs(deltaYToClosestWall) > Math.abs(y - alignedWall.getYEnd())) {
              deltaYToClosestWall = y - alignedWall.getYEnd();
            }                
          }
          if (Math.abs(y - alignedWall.getYStart()) < margin
              && (this.wallAlignmentFeedback == null
                  || !equalsWallPoint(alignedWall.getXStart(), alignedWall.getYStart(), this.wallAlignmentFeedback))) {
            if (Math.abs(deltaXToClosestWall) > Math.abs(x - alignedWall.getXStart())) {
              deltaXToClosestWall = x - alignedWall.getXStart();
            }
          } else if (Math.abs(y - alignedWall.getYEnd()) < margin
                    && (this.wallAlignmentFeedback == null
                        || !equalsWallPoint(alignedWall.getXEnd(), alignedWall.getYEnd(), this.wallAlignmentFeedback))) {
            if (Math.abs(deltaXToClosestWall) > Math.abs(x - alignedWall.getXEnd())) {
              deltaXToClosestWall = x - alignedWall.getXEnd();
            }                
          }
        }
      }
      
      // Draw alignment horizontal and vertical lines
      g2D.setPaint(feedbackPaint);         
      g2D.setStroke(feedbackStroke);
      if (deltaXToClosestWall != Float.POSITIVE_INFINITY) {
        if (deltaXToClosestWall > 0) {
          g2D.draw(new Line2D.Float(x + 25 / this.scale, y, 
              x - deltaXToClosestWall - 25 / this.scale, y));
        } else {
          g2D.draw(new Line2D.Float(x - 25 / this.scale, y, 
              x - deltaXToClosestWall + 25 / this.scale, y));
        }
      }

      if (deltaYToClosestWall != Float.POSITIVE_INFINITY) {
        if (deltaYToClosestWall > 0) {
          g2D.draw(new Line2D.Float(x, y + 25 / this.scale, 
              x, y - deltaYToClosestWall - 25 / this.scale));
        } else {
          g2D.draw(new Line2D.Float(x, y - 25 / this.scale, 
              x, y - deltaYToClosestWall + 25 / this.scale));
        }
      }
    }
  }
  
  /**
   * Returns <code>true</code> if <code>wall</code> start or end point 
   * equals the point (<code>x</code>, <code>y</code>).
   */
  private boolean equalsWallPoint(float x, float y, Wall wall) {
    return x == wall.getXStart() && y == wall.getYStart()
           || x == wall.getXEnd() && y == wall.getYEnd();
  }
  
  /**
   * Paints rectangle feedback.
   */
  private void paintRectangleFeedback(Graphics2D g2D, Color selectionColor) {
    if (this.rectangleFeedback != null) {
      g2D.setPaint(new Color(selectionColor.getRed(), selectionColor.getGreen(), selectionColor.getBlue(), 32));
      g2D.fill(this.rectangleFeedback);
      g2D.setPaint(selectionColor);
      g2D.setStroke(new BasicStroke(1 / this.scale));
      g2D.draw(this.rectangleFeedback);
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
   * Ensures selected items are visible at screen and moves
   * scroll bars if needed.
   */
  public void makeSelectionVisible() {
    // As multiple selections may happen during an action, 
    // make the selection visible the latest possible to avoid multiple changes
    if (!this.selectionScrollUpdated) {
      this.selectionScrollUpdated = true;
      EventQueue.invokeLater(new Runnable() {
          public void run() {
            selectionScrollUpdated = false;
            if (!home.getSelectedItems().isEmpty()) {
              Area area = new Area();
              for (Object item : home.getSelectedItems()) {
                if (item instanceof Wall) {
                  area.add(new Area(getShape(((Wall)item).getPoints())));
                } else if (item instanceof HomePieceOfFurniture) {
                  area.add(new Area(getShape(((HomePieceOfFurniture)item).getPoints())));        
                }
              }      
              Rectangle pixelBounds = getShapePixelBounds(area);
              pixelBounds.grow(5, 5);
              scrollRectToVisible(pixelBounds);
            }
          }
        });
    }
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
  public float convertXPixelToModel(int x) {
    Insets insets = getInsets();
    Rectangle2D planBounds = getPlanBounds();    
    return (x - insets.left) / this.scale - MARGIN + (float)planBounds.getMinX();
  }

  /**
   * Returns <code>y</code> converted in model coordinates space.
   */
  public float convertYPixelToModel(int y) {
    Insets insets = getInsets();
    Rectangle2D planBounds = getPlanBounds();    
    return (y - insets.top) / this.scale - MARGIN + (float)planBounds.getMinY();
  }

  /**
   * Returns <code>x</code> converted in view coordinates space.
   */
  private int convertXModelToPixel(float x) {
    Insets insets = getInsets();
    Rectangle2D planBounds = getPlanBounds();    
    return (int)Math.round((x - planBounds.getMinX() + MARGIN) * this.scale) + insets.left;
  }

  /**
   * Returns <code>y</code> converted in view coordinates space.
   */
  private int convertYModelToPixel(float y) {
    Insets insets = getInsets();
    Rectangle2D planBounds = getPlanBounds();    
    return (int)Math.round((y - planBounds.getMinY() + MARGIN) * this.scale) + insets.top;
  }

  /**
   * Returns the bounds of <code>shape</code> in pixels coordinates space.
   */
  private Rectangle getShapePixelBounds(Shape shape) {
    Rectangle2D shapeBounds = shape.getBounds2D();
    return new Rectangle(
        convertXModelToPixel((float)shapeBounds.getMinX()), 
        convertYModelToPixel((float)shapeBounds.getMinY()),
        (int)Math.round(shapeBounds.getWidth() * this.scale),
        (int)Math.round(shapeBounds.getHeight() * this.scale));
  }
  
  /**
   * Sets the cursor of this component as rotation cursor. 
   */
  public void setRotationCursor() {
    setCursor(this.rotationCursor);
  }

  /**
   * Sets the cursor of this component as resize cursor. 
   */
  public void setResizeCursor() {
    setCursor(this.resizeCursor);
  }
  
  /**
   * Sets tool tip text displayed as feeback. 
   * @param toolTipFeedback the text displayed in the tool tip 
   *                    or <code>null</code> to make tool tip disapear.
   */
  public void setToolTipFeedback(String toolTipFeedback, float x, float y) {
    // Create tool tip for this component
    if (this.toolTip == null) {
      this.toolTip = new JToolTip();
      this.toolTip.setComponent(this);
    }
    // Change its text    
    this.toolTip.setTipText(toolTipFeedback);

    if (this.toolTipWindow == null) {
      // Show tool tip in a window (we don't use a Swing Popup because 
      // we require the tool tip window to move along with mouse pointer 
      // and a Swing popup can't move without hiding then showing it again)
      this.toolTipWindow = new JWindow(JOptionPane.getFrameForComponent(this));
      this.toolTipWindow.add(this.toolTip);
      // Add to window a mouse listener that redispatch mouse events to
      // plan component (if the user moves fastly enough the mouse pointer in a way 
      // it's in toolTipWindow, the matching event is dispatched to toolTipWindow)
      MouseInputAdapter mouseAdapter = new MouseInputAdapter() {
        @Override
        public void mousePressed(MouseEvent ev) {
          mouseMoved(ev);
        }
        
        @Override
        public void mouseReleased(MouseEvent ev) {
          mouseMoved(ev);
        }
        
        @Override
        public void mouseMoved(MouseEvent ev) {
          Point mouseLocationInPlan = SwingUtilities.convertPoint(toolTipWindow, 
              ev.getX(), ev.getY(), PlanComponent.this);
          dispatchEvent(new MouseEvent(PlanComponent.this, ev.getID(), ev.getWhen(),
              ev.getModifiers(), mouseLocationInPlan.x, mouseLocationInPlan.y, 
              ev.getClickCount(), ev.isPopupTrigger(), ev.getButton()));
        }
        
        @Override
        public void mouseDragged(MouseEvent ev) {
          mouseMoved(ev);
        }
      };
      this.toolTipWindow.addMouseListener(mouseAdapter);
      this.toolTipWindow.addMouseMotionListener(mouseAdapter);
    } else {
      this.toolTip.revalidate();
    }
    // Convert (x, y) to screen coordinates 
    Point point = new Point(convertXModelToPixel(x), convertYModelToPixel(y));
    SwingUtilities.convertPointToScreen(point, this);
    // Add to point the half of cursor size
    Dimension cursorSize = getToolkit().getBestCursorSize(16, 16);
    if (cursorSize.width != 0) {
      point.x += cursorSize.width / 2 + 3;
      point.y += cursorSize.height / 2 + 3;
    } else {
      // If custom cursor isn't supported let's consider 
      // default cursor size is 16 pixels wide
      point.x += 11;
      point.y += 11;
    }
    this.toolTipWindow.setLocation(point);      
    this.toolTipWindow.pack();
    this.toolTipWindow.setVisible(true);
    this.toolTip.paintImmediately(this.toolTip.getBounds());
  }
  
  /**
   * Deletes tool tip text from screen. 
   */
  public void deleteToolTipFeedback() {
    if (this.toolTip != null) {
      this.toolTip.setTipText(null);
    }
    if (this.toolTipWindow != null) {
      this.toolTipWindow.setVisible(false);
    }
  }

  /**
   * Sets whether the resize indicator of selected wall or piece of furniture 
   * should be visible or not. 
   */
  public void setResizeIndicatorVisible(boolean resizeIndicatorVisible) {
    this.resizeIndicatorVisible = resizeIndicatorVisible;    
    repaint();
  }
  
  /**
   * Sets the location point for <code>wall</code> alignment feedback. 
   */
  public void setWallAlignmentFeeback(Wall wall, float x, float y) {
    this.wallAlignmentFeedback = wall;
    this.wallLocationFeeback = new Point2D.Float(x, y);
    repaint();
  }
  
  /**
   * Deletes the wall alignment feedback. 
   */
  public void deleteWallAlignmentFeeback() {
    this.wallAlignmentFeedback = null;
    this.wallLocationFeeback = null;
    repaint();
  }
  
  // Scrollable implementation
  public Dimension getPreferredScrollableViewportSize() {
    return getPreferredSize();
  }

  public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
    if (orientation == SwingConstants.HORIZONTAL) {
      return visibleRect.width / 2;
    } else { // SwingConstants.VERTICAL
      return visibleRect.height / 2;
    }
  }

  public boolean getScrollableTracksViewportHeight() {
    // Return true if the plan's preferred height is smaller than the viewport height
    return getParent() instanceof JViewport
        && getPreferredSize().height < ((JViewport)getParent()).getHeight();
  }

  public boolean getScrollableTracksViewportWidth() {
    // Return true if the plan's preferred width is smaller than the viewport width
    return getParent() instanceof JViewport
        && getPreferredSize().width < ((JViewport)getParent()).getWidth();
  }

  public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
    if (orientation == SwingConstants.HORIZONTAL) {
      return visibleRect.width / 10;
    } else { // SwingConstants.VERTICAL
      return visibleRect.height / 10;
    }
  }
  
  public JComponent getHorizontalRuler() {
    if (this.horizontalRuler == null) {
      this.horizontalRuler = new PlanRulerComponent(SwingConstants.HORIZONTAL);
    } 
    return this.horizontalRuler;
  }
  
  public JComponent getVerticalRuler() {
    if (this.verticalRuler == null) {
      this.verticalRuler = new PlanRulerComponent(SwingConstants.VERTICAL);
    } 
    return this.verticalRuler;
  }
  
  /**
   * A component displaying the plan horizontal or vertical ruler associated to this plan.
   */
  private class PlanRulerComponent extends JComponent {
    private int orientation;    

    /**
     * Creates a plan ruler.
     * @param orientation <code>SwingConstants.HORIZONTAL</code> or 
     *                    <code>SwingConstants.VERTICAL</code>. 
     */
    public PlanRulerComponent(int orientation) {
      this.orientation = orientation;
      setOpaque(true);
      // Use same font as tooltips
      setFont(UIManager.getFont("ToolTip.font"));
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
        FontMetrics metrics = getFontMetrics(getFont());
        int ruleHeight = metrics.getAscent() + 6;
        if (this.orientation == SwingConstants.HORIZONTAL) {
          return new Dimension(
              Math.round(((float)planBounds.getWidth() + MARGIN * 2)
                         * getScale()) + insets.left + insets.right,
              ruleHeight);
        } else {
          return new Dimension(ruleHeight,
              Math.round(((float)planBounds.getHeight() + MARGIN * 2)
                         * getScale()) + insets.top + insets.bottom);
        }
      }
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
      g2D.translate(insets.left + (MARGIN - planBounds.getMinX()) * getScale(),
          insets.top + (MARGIN - planBounds.getMinY()) * getScale());
      g2D.scale(getScale(), getScale());
      g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      // Paint component contents
      paintRuler(g2D);
      g2D.dispose();
    }

    /**
     * Fills the background with UI window background color. 
     */
    private void paintBackground(Graphics2D g2D) {
      if (isOpaque()) {
        g2D.setColor(getBackground());
        g2D.fillRect(0, 0, getWidth(), getHeight());
      }
    }

    /**
     * Paints background grid lines.
     */
    private void paintRuler(Graphics2D g2D) {
      float mainGridSize;
      float [] gridSizes;
      if (preferences.getUnit() == UserPreferences.Unit.INCH) {
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
      for (int i = 1; i < gridSizes.length && gridSize * getScale() < 10; i++) {
        gridSize = gridSizes [i];
      }
      
      Rectangle2D planBounds = getPlanBounds();    
      float xMin = (float)planBounds.getMinX() - MARGIN;
      float yMin = (float)planBounds.getMinY() - MARGIN;
      float xMax = convertXPixelToModel(getWidth());
      float yMax = convertYPixelToModel(getHeight());

      FontMetrics metrics = getFontMetrics(getFont());
      int fontAscent = metrics.getAscent();
      float tickSize = 5 / getScale();
      float mainTickSize = (fontAscent + 6) / getScale();
      NumberFormat format = NumberFormat.getNumberInstance();
      String maxText = getFormattedTickText(format, 100);
      int maxTextWidth = metrics.stringWidth(maxText) + 10;
      float textInterval =
        mainGridSize != gridSize
          ? mainGridSize 
          : (float)Math.ceil(maxTextWidth / (gridSize * getScale())) * gridSize;
      
      g2D.setColor(getForeground());
      float lineWidth = 0.5f / getScale();
      g2D.setStroke(new BasicStroke(lineWidth));
      if (this.orientation == SwingConstants.HORIZONTAL) {
        // Draw horizontal rule base
        g2D.draw(new Line2D.Float(xMin, yMax - lineWidth, xMax, yMax - lineWidth));
        // Draw vertical lines
        for (float x = (int) (xMin / gridSize) * gridSize; x < xMax; x += gridSize) {
          if (Math.abs(Math.abs(x) % textInterval - textInterval) < 1E-2 
              || Math.abs(Math.abs(x) % textInterval) < 1E-2) {
            // Draw big tick
            g2D.draw(new Line2D.Float(x, yMax - mainTickSize, x, yMax));
            // Draw unit text
            AffineTransform previousTransform = g2D.getTransform();
            g2D.translate(x, yMax - mainTickSize);
            g2D.scale(1 / getScale(), 1 / getScale());
            g2D.drawString(getFormattedTickText(format, x), 3, fontAscent - 1);
            g2D.setTransform(previousTransform);
          } else {
            // Draw small tick
            g2D.draw(new Line2D.Float(x, yMax - tickSize, x, yMax));
          }
        }
      } else {
        // Draw vertical rule base
        g2D.draw(new Line2D.Float(xMax - lineWidth, yMin, xMax - lineWidth, yMax));
        // Draw horizontal lines
        for (float y = (int) (yMin / gridSize) * gridSize; y < yMax; y += gridSize) {
          if (Math.abs(Math.abs(y) % textInterval - textInterval) < 1E-2 
              || Math.abs(Math.abs(y) % textInterval) < 1E-2) {
            // Draw big tick
            g2D.draw(new Line2D.Float(xMax - mainTickSize, y, xMax, y));
            // Draw unit text with a vertical orientation
            AffineTransform previousTransform = g2D.getTransform();
            g2D.translate(xMax - mainTickSize, y);
            g2D.scale(1 / getScale(), 1 / getScale());
            g2D.rotate(-Math.PI / 2);
            String yText = getFormattedTickText(format, y);
            g2D.drawString(yText, -metrics.stringWidth(yText) - 3, fontAscent - 1);
            g2D.setTransform(previousTransform);
          } else {
            // Draw small tick
            g2D.draw(new Line2D.Float(xMax - tickSize, y, xMax, y));
          }
        }
      }

      if (mainGridSize != gridSize) {
        g2D.setStroke(new BasicStroke(1.5f / getScale(),
            BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        if (this.orientation == SwingConstants.HORIZONTAL) {
          // Draw main vertical lines
          for (float x = (int) (xMin / mainGridSize) * mainGridSize; x < xMax; x += mainGridSize) {
            g2D.draw(new Line2D.Float(x, yMax - mainTickSize, x, yMax));
          }
        } else {
          // Draw positive main horizontal lines
          for (float y = (int) (yMin / mainGridSize) * mainGridSize; y < yMax; y += mainGridSize) {
            g2D.draw(new Line2D.Float(xMax - mainTickSize, y, xMax, y));
          }
        }
      }
    }

    private String getFormattedTickText(NumberFormat format, float value) {
      String text;
      if (Math.abs(value) < 1E-5) {
        value = 0; // Avoid "-0" text
      }
      if (preferences.getUnit() == UserPreferences.Unit.CENTIMETER) {
        text = format.format(value / 100);
        if (value == 0) {
          text += "m";
        }
      } else {
        text = format.format(UserPreferences.Unit.centimerToFoot(value)) + "'"; 
      }
      return text;
    }
  }
}
