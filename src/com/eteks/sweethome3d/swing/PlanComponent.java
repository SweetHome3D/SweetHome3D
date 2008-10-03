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

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
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
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;

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
import javax.swing.event.MouseInputListener;

import com.eteks.sweethome3d.model.BackgroundImage;
import com.eteks.sweethome3d.model.CameraEvent;
import com.eteks.sweethome3d.model.CameraListener;
import com.eteks.sweethome3d.model.DimensionLine;
import com.eteks.sweethome3d.model.DimensionLineEvent;
import com.eteks.sweethome3d.model.DimensionLineListener;
import com.eteks.sweethome3d.model.FurnitureEvent;
import com.eteks.sweethome3d.model.FurnitureListener;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.ObserverCamera;
import com.eteks.sweethome3d.model.SelectionEvent;
import com.eteks.sweethome3d.model.SelectionListener;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.model.WallEvent;
import com.eteks.sweethome3d.model.WallListener;
import com.eteks.sweethome3d.tools.OperatingSystem;

/**
 * A component displaying the plan of a home.
 * @author Emmanuel Puybaret
 */
public class PlanComponent extends JComponent implements Scrollable, Printable {
  private enum ActionType {DELETE_SELECTION, ESCAPE, 
    MOVE_SELECTION_LEFT, MOVE_SELECTION_UP, MOVE_SELECTION_DOWN, MOVE_SELECTION_RIGHT,
    TOGGLE_MAGNETISM_ON, TOGGLE_MAGNETISM_OFF, 
    DUPLICATION_ON, DUPLICATION_OFF}
  private enum PaintMode {PAINT, PRINT, CLIPBOARD}
  
  private static final float MARGIN = 40;
  private Home               home;
  private UserPreferences    preferences;
  private float              scale  = 0.5f;

  private JComponent         horizontalRuler;
  private JComponent         verticalRuler;
  
  private Rectangle2D        rectangleFeedback;
  private Wall               wallAlignmentFeedback;
  private Point2D            wallLocationFeeback;
  private DimensionLine      dimensionLineAlignmentFeedback;
  private Point2D            dimensionLineLocationFeeback;
  private boolean            selectionScrollUpdated;
  private Cursor             rotationCursor;
  private Cursor             elevationCursor;
  private Cursor             heightCursor;
  private Cursor             resizeCursor;
  private Cursor             duplicationCursor;
  private JToolTip           toolTip;
  private JWindow            toolTipWindow;
  private boolean            resizeIndicatorVisible;
  private List<HomePieceOfFurniture> sortedHomeFurniture;

  private Rectangle2D        planBoundsCache;  
  private boolean            planBoundsCacheValid;  
  private BufferedImage      backgroundImageCache;
  private Area               wallsAreaCache;

  private static final GeneralPath FURNITURE_ROTATION_INDICATOR;
  private static final GeneralPath FURNITURE_RESIZE_INDICATOR;
  private static final GeneralPath FURNITURE_ELEVATION_INDICATOR;
  private static final Shape       FURNITURE_ELEVATION_POINT_INDICATOR;
  private static final GeneralPath FURNITURE_HEIGHT_INDICATOR;
  private static final Shape       FURNITURE_HEIGHT_POINT_INDICATOR;
  private static final GeneralPath WALL_ORIENTATION_INDICATOR;
  private static final Shape       WALL_POINT;
  private static final GeneralPath WALL_AND_LINE_RESIZE_INDICATOR;
  private static final Shape       CAMERA_YAW_ROTATION_INDICATOR;
  private static final GeneralPath CAMERA_PITCH_ROTATION_INDICATOR;
  private static final Shape       CAMERA_BODY;
  private static final Shape       CAMERA_HEAD;  
  private static final GeneralPath DIMENSION_LINE_END;  
  
  private static final float WALL_STROKE_WIDTH = 1.5f;

  static {
    // Create a path that draws an round arrow used as a rotation indicator 
    // at top left vertex of a piece of furniture
    FURNITURE_ROTATION_INDICATOR = new GeneralPath();
    FURNITURE_ROTATION_INDICATOR.append(new Ellipse2D.Float(-1.5f, -1.5f, 3, 3), false);
    FURNITURE_ROTATION_INDICATOR.append(new Arc2D.Float(-8, -8, 16, 16, 45, 180, Arc2D.OPEN), false);
    FURNITURE_ROTATION_INDICATOR.moveTo(2.66f, -5.66f);
    FURNITURE_ROTATION_INDICATOR.lineTo(5.66f, -5.66f);
    FURNITURE_ROTATION_INDICATOR.lineTo(4f, -8.3f);
    
    FURNITURE_ELEVATION_POINT_INDICATOR = new Rectangle2D.Float(-1.5f, -1.5f, 3f, 3f);
    
    // Create a path that draws a line with one arrow as an elevation indicator
    // at top right of a piece of furniture
    FURNITURE_ELEVATION_INDICATOR = new GeneralPath();
    FURNITURE_ELEVATION_INDICATOR.moveTo(0, -5); // Vertical line
    FURNITURE_ELEVATION_INDICATOR.lineTo(0, 5);
    FURNITURE_ELEVATION_INDICATOR.moveTo(-2.5f, 5);    // Bottom line
    FURNITURE_ELEVATION_INDICATOR.lineTo(2.5f, 5);
    FURNITURE_ELEVATION_INDICATOR.moveTo(-1.2f, 1.5f); // Bottom arrow
    FURNITURE_ELEVATION_INDICATOR.lineTo(0, 4.5f);
    FURNITURE_ELEVATION_INDICATOR.lineTo(1.2f, 1.5f);
    
    FURNITURE_HEIGHT_POINT_INDICATOR = new Rectangle2D.Float(-1.5f, -1.5f, 3f, 3f);
    
    // Create a path that draws a line with two arrows as a height indicator
    // at bottom left of a piece of furniture
    FURNITURE_HEIGHT_INDICATOR = new GeneralPath();
    FURNITURE_HEIGHT_INDICATOR.moveTo(0, -6); // Vertical line
    FURNITURE_HEIGHT_INDICATOR.lineTo(0, 6);
    FURNITURE_HEIGHT_INDICATOR.moveTo(-2.5f, -6);    // Top line
    FURNITURE_HEIGHT_INDICATOR.lineTo(2.5f, -6);
    FURNITURE_HEIGHT_INDICATOR.moveTo(-2.5f, 6);     // Bottom line
    FURNITURE_HEIGHT_INDICATOR.lineTo(2.5f, 6);
    FURNITURE_HEIGHT_INDICATOR.moveTo(-1.2f, -2.5f); // Top arrow
    FURNITURE_HEIGHT_INDICATOR.lineTo(0f, -5.5f);
    FURNITURE_HEIGHT_INDICATOR.lineTo(1.2f, -2.5f);
    FURNITURE_HEIGHT_INDICATOR.moveTo(-1.2f, 2.5f);  // Bottom arrow
    FURNITURE_HEIGHT_INDICATOR.lineTo(0f, 5.5f);
    FURNITURE_HEIGHT_INDICATOR.lineTo(1.2f, 2.5f);
    
    // Create a path used as a size indicator 
    // at bottom right vertex of a piece of furniture
    FURNITURE_RESIZE_INDICATOR = new GeneralPath();
    FURNITURE_RESIZE_INDICATOR.append(new Rectangle2D.Float(-1.5f, -1.5f, 3f, 3f), false);
    FURNITURE_RESIZE_INDICATOR.moveTo(5, -4);
    FURNITURE_RESIZE_INDICATOR.lineTo(7, -4);
    FURNITURE_RESIZE_INDICATOR.lineTo(7, 7);
    FURNITURE_RESIZE_INDICATOR.lineTo(-4, 7);
    FURNITURE_RESIZE_INDICATOR.lineTo(-4, 5);
    FURNITURE_RESIZE_INDICATOR.moveTo(3.5f, 3.5f);
    FURNITURE_RESIZE_INDICATOR.lineTo(9, 9);
    FURNITURE_RESIZE_INDICATOR.moveTo(7, 9.5f);
    FURNITURE_RESIZE_INDICATOR.lineTo(10, 10);
    FURNITURE_RESIZE_INDICATOR.lineTo(9.5f, 7);
    
    // Create a path used an orientation indicator
    // at start and end points of a selected wall
    WALL_ORIENTATION_INDICATOR = new GeneralPath();
    WALL_ORIENTATION_INDICATOR.moveTo(-4, -4);
    WALL_ORIENTATION_INDICATOR.lineTo(4, 0);
    WALL_ORIENTATION_INDICATOR.lineTo(-4, 4);

    WALL_POINT = new Ellipse2D.Float(-3, -3, 6, 6);

    // Create a path used as a size indicator 
    // at start and end points of a selected wall
    WALL_AND_LINE_RESIZE_INDICATOR = new GeneralPath();
    WALL_AND_LINE_RESIZE_INDICATOR.moveTo(5, -2);
    WALL_AND_LINE_RESIZE_INDICATOR.lineTo(5, 2);
    WALL_AND_LINE_RESIZE_INDICATOR.moveTo(6, 0);
    WALL_AND_LINE_RESIZE_INDICATOR.lineTo(11, 0);
    WALL_AND_LINE_RESIZE_INDICATOR.moveTo(8.7f, -1.8f);
    WALL_AND_LINE_RESIZE_INDICATOR.lineTo(12, 0);
    WALL_AND_LINE_RESIZE_INDICATOR.lineTo(8.7f, 1.8f);
    
    // Create a path used as yaw rotation indicator for the camera
    AffineTransform transform = new AffineTransform();
    transform.rotate(-Math.PI / 4);
    CAMERA_YAW_ROTATION_INDICATOR = FURNITURE_ROTATION_INDICATOR.createTransformedShape(transform);
    
    // Create a path used as pitch rotation indicator for the camera
    CAMERA_PITCH_ROTATION_INDICATOR = new GeneralPath();
    CAMERA_PITCH_ROTATION_INDICATOR.append(new Ellipse2D.Float(-1.5f, -1.5f, 3, 3), false);
    CAMERA_PITCH_ROTATION_INDICATOR.moveTo(4.5f, 0);
    CAMERA_PITCH_ROTATION_INDICATOR.lineTo(5.2f, 0);
    CAMERA_PITCH_ROTATION_INDICATOR.moveTo(9f, 0);
    CAMERA_PITCH_ROTATION_INDICATOR.lineTo(10, 0);
    CAMERA_PITCH_ROTATION_INDICATOR.append(new Arc2D.Float(7, -8, 5, 16, 20, 320, Arc2D.OPEN), false);
    CAMERA_PITCH_ROTATION_INDICATOR.moveTo(10f, 4.5f);
    CAMERA_PITCH_ROTATION_INDICATOR.lineTo(12.3f, 2f);
    CAMERA_PITCH_ROTATION_INDICATOR.lineTo(12.8f, 5.8f);
    
    // Create a path used to draw the camera 
    // This path looks like a human being seen from top that fits in one cm wide square 
    GeneralPath cameraBodyAreaPath = new GeneralPath();
    cameraBodyAreaPath.append(new Ellipse2D.Float(-0.5f, -0.425f, 1f, 0.85f), false); // Body
    cameraBodyAreaPath.append(new Ellipse2D.Float(-0.5f, -0.3f, 0.24f, 0.6f), false); // Shoulder
    cameraBodyAreaPath.append(new Ellipse2D.Float(0.26f, -0.3f, 0.24f, 0.6f), false); // Shoulder
    CAMERA_BODY = new Area(cameraBodyAreaPath);
    
    GeneralPath cameraHeadAreaPath = new GeneralPath();
    cameraHeadAreaPath.append(new Ellipse2D.Float(-0.18f, -0.45f, 0.36f, 1f), false); // Head
    cameraHeadAreaPath.moveTo(-0.04f, 0.55f); // Noise
    cameraHeadAreaPath.lineTo(0, 0.65f);
    cameraHeadAreaPath.lineTo(0.04f, 0.55f);
    cameraHeadAreaPath.closePath();
    CAMERA_HEAD = new Area(cameraHeadAreaPath);
    
    DIMENSION_LINE_END = new GeneralPath();
    DIMENSION_LINE_END.moveTo(-5, 5);
    DIMENSION_LINE_END.lineTo(5, -5);
    DIMENSION_LINE_END.moveTo(0, 5);
    DIMENSION_LINE_END.lineTo(0, -5);
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
    this.rotationCursor = createCustomCursor("resources/cursors/rotation16x16.png",
        "resources/cursors/rotation32x32.png", "Rotation cursor");
    this.elevationCursor = createCustomCursor("resources/cursors/elevation16x16.png",
        "resources/cursors/elevation32x32.png", "Elevation cursor");
    this.heightCursor = createCustomCursor("resources/cursors/height16x16.png",
        "resources/cursors/height32x32.png", "Height cursor");
    this.resizeCursor = createCustomCursor("resources/cursors/resize16x16.png",
        "resources/cursors/resize32x32.png", "Resize cursor");
    this.duplicationCursor = DragSource.DefaultCopyDrop;
    // Install default colors
    super.setForeground(UIManager.getColor("textText"));
    super.setBackground(UIManager.getColor("window"));
  }

  /**
   * Adds wall and selection listeners on this component to receive wall 
   * changes notifications from home. 
   */
  private void addModelListeners(Home home, UserPreferences preferences) {
    home.addFurnitureListener(new FurnitureListener() {
        public void pieceOfFurnitureChanged(FurnitureEvent ev) {
          sortedHomeFurniture = null;
          invalidatePlanBoundsAndRevalidate();
        }
      });
    home.addWallListener(new WallListener () {
        public void wallChanged(WallEvent ev) {
          wallsAreaCache = null;
          invalidatePlanBoundsAndRevalidate();
        }
      });
    home.addDimensionLineListener(new DimensionLineListener () {
        public void dimensionLineChanged(DimensionLineEvent ev) {
          invalidatePlanBoundsAndRevalidate();
        }
      });
    home.addSelectionListener(new SelectionListener () {
        public void selectionChanged(SelectionEvent ev) {
          repaint();
        }
      });
    home.addCameraListener(new CameraListener() {
        public void cameraChanged(CameraEvent ev) {
          invalidatePlanBoundsAndRevalidate();
        }
      });
    home.addPropertyChangeListener(Home.Property.BACKGROUND_IMAGE, 
      new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          backgroundImageCache = null;
          repaint();
        }
      });
    preferences.addPropertyChangeListener(UserPreferences.Property.UNIT, 
        new UnitChangeListener(this));
    preferences.addPropertyChangeListener(UserPreferences.Property.GRID_VISIBLE, 
        new GridVisibleChangeListener(this));
  }

  /**
   * Preferences property listener bound to this component with a weak reference to avoid
   * strong link between preferences and this component.  
   */
  private static class UnitChangeListener implements PropertyChangeListener {
    private WeakReference<PlanComponent>  planComponent;

    public UnitChangeListener(PlanComponent planComponent) {
      this.planComponent = new WeakReference<PlanComponent>(planComponent);
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
      // If plan component was garbage collected, remove this listener from preferences
      PlanComponent planComponent = this.planComponent.get();
      if (planComponent == null) {
        ((UserPreferences)ev.getSource()).removePropertyChangeListener(
            UserPreferences.Property.UNIT, this);
      } else {
        planComponent.repaint();
        if (planComponent.horizontalRuler != null) {
          planComponent.horizontalRuler.repaint();
        }
        if (planComponent.verticalRuler != null) {
          planComponent.verticalRuler.repaint();
        }
      }
    }
  }

  /**
   * Preferences property listener bound to this component with a weak reference to avoid
   * strong link between preferences and this component.  
   */
  private static class GridVisibleChangeListener implements PropertyChangeListener {
    private WeakReference<PlanComponent>  planComponent;

    public GridVisibleChangeListener(PlanComponent planComponent) {
      this.planComponent = new WeakReference<PlanComponent>(planComponent);
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
      // If plan component was garbage collected, remove this listener from preferences
      PlanComponent planComponent = this.planComponent.get();
      if (planComponent == null) {
        ((UserPreferences)ev.getSource()).removePropertyChangeListener(
            UserPreferences.Property.UNIT, this);
      } else {
        planComponent.repaint();
      }
    }
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
   * Invalidates plan bounds cache, revalidates this component and 
   * updates viewport position if this component is displayed in a scrolled pane.
   */
  private void invalidatePlanBoundsAndRevalidate() {
    Point viewPosition = null;
    float planBoundsMinX = (float)getPlanBounds().getMinX();
    float planBoundsMinY = (float)getPlanBounds().getMinY();
    JViewport parent = (JViewport)getParent();
    if (parent instanceof JViewport) {
      viewPosition = parent.getViewPosition();
    }
    
    planBoundsCacheValid = false;
    // Revalidate and repaint
    revalidate();
    
    float planBoundsNewMinX = (float)getPlanBounds().getMinX();
    float planBoundsNewMinY = (float)getPlanBounds().getMinY();
    // If plan bounds upper left corner diminished
    if (parent instanceof JViewport
        && (planBoundsNewMinX < planBoundsMinX
            || planBoundsNewMinY < planBoundsMinY)) {
      Dimension extentSize = parent.getExtentSize();
      Dimension viewSize = parent.getViewSize();
      // Update view position when scroll bars are visible
      if (extentSize.width < viewSize.width
          || extentSize.height < viewSize.height) {
        int deltaX = Math.round((planBoundsMinX - planBoundsNewMinX) * getScale());
        int deltaY = Math.round((planBoundsMinY - planBoundsNewMinY) * getScale());
        parent.setViewPosition(new Point(viewPosition.x + deltaX, viewPosition.y + deltaY));
      }
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
              ev.getClickCount(), ev.isShiftDown(), ev.isAltDown());
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
    inputMap.put(KeyStroke.getKeyStroke("shift ESCAPE"), ActionType.ESCAPE);
    inputMap.put(KeyStroke.getKeyStroke("alt ESCAPE"), ActionType.ESCAPE);
    inputMap.put(KeyStroke.getKeyStroke("LEFT"), ActionType.MOVE_SELECTION_LEFT);
    inputMap.put(KeyStroke.getKeyStroke("UP"), ActionType.MOVE_SELECTION_UP);
    inputMap.put(KeyStroke.getKeyStroke("DOWN"), ActionType.MOVE_SELECTION_DOWN);
    inputMap.put(KeyStroke.getKeyStroke("RIGHT"), ActionType.MOVE_SELECTION_RIGHT);
    
    // Use a key listener for SHIFT and ALT keys, 
    // to avoid component to consume the events for these keys 
    addKeyListener(new KeyAdapter() {
        public void keyPressed(KeyEvent ev) {
          switch (ev.getKeyCode()) {
            case KeyEvent.VK_ALT :
              getActionMap().get(ActionType.DUPLICATION_ON).actionPerformed(null);
              break;
            case KeyEvent.VK_SHIFT :
              getActionMap().get(ActionType.TOGGLE_MAGNETISM_ON).actionPerformed(null);
              break;
          }
        }
  
        public void keyReleased(KeyEvent ev) {
          switch (ev.getKeyCode()) {
            case KeyEvent.VK_ALT :
              getActionMap().get(ActionType.DUPLICATION_OFF).actionPerformed(null);
              break;
            case KeyEvent.VK_SHIFT :
              getActionMap().get(ActionType.TOGGLE_MAGNETISM_OFF).actionPerformed(null);
              break;
          }
        }
      });
  }
 
  /**
   * Creates actions that calls back <code>controller</code> methods.  
   */
  private void createActions(final PlanController controller) {
    // Delete selection action mapped to back space and delete keys
    Action deleteSelectionAction = new AbstractAction() {
      public void actionPerformed(ActionEvent ev) {
        controller.deleteSelection();
      }
    };
    // Escape action mapped to Esc key
    Action escapeAction = new AbstractAction() {
      public void actionPerformed(ActionEvent ev) {
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

      public void actionPerformed(ActionEvent ev) {
        controller.moveSelection(this.dx / getScale(), this.dy / getScale());
      }
    }
    // Temporary magnetism mapped to Shift key
    class ToggleMagnetismAction extends AbstractAction {
      private final boolean toggle;
      
      public ToggleMagnetismAction(boolean toggle) {
        this.toggle = toggle;
      }

      public void actionPerformed(ActionEvent ev) {
        controller.toggleMagnetism(this.toggle);
      }
    }
    // Duplication mapped to Alt key
    class DuplicationAction extends AbstractAction {
      private final boolean duplicationActivated;
      
      public DuplicationAction(boolean duplicationActivated) {
        this.duplicationActivated = duplicationActivated;
      }

      public void actionPerformed(ActionEvent ev) {
        controller.activateDuplication(this.duplicationActivated);
      }
    }
    ActionMap actionMap = getActionMap();
    actionMap.put(ActionType.DELETE_SELECTION, deleteSelectionAction);
    actionMap.put(ActionType.ESCAPE, escapeAction);
    actionMap.put(ActionType.MOVE_SELECTION_LEFT, new MoveSelectionAction(-1, 0));
    actionMap.put(ActionType.MOVE_SELECTION_UP, new MoveSelectionAction(0, -1));
    actionMap.put(ActionType.MOVE_SELECTION_DOWN, new MoveSelectionAction(0, 1));
    actionMap.put(ActionType.MOVE_SELECTION_RIGHT, new MoveSelectionAction(1, 0));
    actionMap.put(ActionType.TOGGLE_MAGNETISM_ON, new ToggleMagnetismAction(true));
    actionMap.put(ActionType.TOGGLE_MAGNETISM_OFF, new ToggleMagnetismAction(false));
    actionMap.put(ActionType.DUPLICATION_ON, new DuplicationAction(true));
    actionMap.put(ActionType.DUPLICATION_OFF, new DuplicationAction(false));
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
                     * getScale()) + insets.left + insets.right,
          Math.round(((float)planBounds.getHeight() + MARGIN * 2)
                     * getScale()) + insets.top + insets.bottom);
    }
  }
  
  /**
   * Returns the bounds of the plan displayed by this component.
   */
  private Rectangle2D getPlanBounds() {
    if (this.planBoundsCache == null) {      
      // Ensure plan bounds are 10 x 10 meters wide at minimum
      this.planBoundsCache = new Rectangle2D.Float(0, 0, 1000, 1000);
    }
    if (!this.planBoundsCacheValid) {
      // Enlarge plan bounds to include background image, home bounds and observer camera
      if (this.backgroundImageCache != null) {
        BackgroundImage backgroundImage = this.home.getBackgroundImage();
        this.planBoundsCache.add(this.backgroundImageCache.getWidth() * backgroundImage.getScale() - backgroundImage.getXOrigin(),
            this.backgroundImageCache.getHeight() * backgroundImage.getScale() - backgroundImage.getYOrigin());
      }
      Rectangle2D wallsAndFurnitureBounds = getWallFurnitureDimensionLinesBounds();
      if (wallsAndFurnitureBounds != null) {
        this.planBoundsCache.add(wallsAndFurnitureBounds);
      }
      for (float [] point : this.home.getObserverCamera().getPoints()) {
        this.planBoundsCache.add(point [0], point [1]);
      }
      this.planBoundsCacheValid = true;
    }
    return this.planBoundsCache;
  }
  
  /**
   * Returns the walls, furniture and dimension lines bounds of the home 
   * displayed by this component.
   */
  private Rectangle2D getWallFurnitureDimensionLinesBounds() {
    // Compute bounds that include walls and furniture
    Rectangle2D wallsFurnitureDimensionLinesBounds = null;
    for (Wall wall : this.home.getWalls()) {
      for (float [] point : wall.getPoints()) {
        if (wallsFurnitureDimensionLinesBounds == null) {
          wallsFurnitureDimensionLinesBounds = new Rectangle2D.Float(point [0], point [1], 0, 0);
        } else {
          wallsFurnitureDimensionLinesBounds.add(point [0], point [1]);
        }
      }
    }
    for (HomePieceOfFurniture piece : this.home.getFurniture()) {
      if (piece.isVisible()) {
        for (float [] point : piece.getPoints()) {
          if (wallsFurnitureDimensionLinesBounds == null) {
            wallsFurnitureDimensionLinesBounds = new Rectangle2D.Float(point [0], point [1], 0, 0);
          } else {
            wallsFurnitureDimensionLinesBounds.add(point [0], point [1]);
          }
        }
      }
    }
    for (DimensionLine dimensionLine : this.home.getDimensionLines()) {
      for (float [] point : dimensionLine.getPoints()) {
        if (wallsFurnitureDimensionLinesBounds == null) {
          wallsFurnitureDimensionLinesBounds = new Rectangle2D.Float(point [0], point [1], 0, 0);
        } else {
          wallsFurnitureDimensionLinesBounds.add(point [0], point [1]);
        }
      }
    }
    return wallsFurnitureDimensionLinesBounds;
  }

  /**
   * Paints this component.
   */
  @Override
  protected void paintComponent(Graphics g) {
    Graphics2D g2D = (Graphics2D)g.create();
    Color backgroundColor = getBackground();
    Color foregroundColor = getForeground();
    paintBackground(g2D, backgroundColor);
    Insets insets = getInsets();
    // Clip component to avoid drawing in empty borders
    g2D.clipRect(insets.left, insets.top, 
        getWidth() - insets.left - insets.right, 
        getHeight() - insets.top - insets.bottom);
    // Change component coordinates system to plan system
    Rectangle2D planBounds = getPlanBounds();    
    float paintScale = getScale();
    g2D.translate(insets.left + (MARGIN - planBounds.getMinX()) * paintScale,
        insets.top + (MARGIN - planBounds.getMinY()) * paintScale);
    g2D.scale(paintScale, paintScale);
    setRenderingHints(g2D);
    // Paint component contents
    paintBackgroundImage(g2D);
    if (this.preferences.isGridVisible()) {
      paintGrid(g2D, paintScale);
    }
    paintContent(g2D, paintScale, backgroundColor, foregroundColor, PaintMode.PAINT);   
    g2D.dispose();
  }
  
  
  /**
   * Prints this component to make it fill <code>pageFormat</code> imageable size.
   */
  public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
    Rectangle2D printedObjectBounds = getWallFurnitureDimensionLinesBounds();
    if (printedObjectBounds != null && pageIndex == 0) {
      Graphics2D g2D = (Graphics2D)g.create();
      double imageableX = pageFormat.getImageableX();
      double imageableY = pageFormat.getImageableY();
      double imageableWidth = pageFormat.getImageableWidth();
      double imageableHeight = pageFormat.getImageableHeight();
      g2D.clip(new Rectangle2D.Double(imageableX, imageableY, imageableWidth, imageableHeight));
      // Change coordinates system to paper imageable origin
      g2D.translate(imageableX, imageableY);
      // Compute a scale that ensures the plan will fill the component
      float extraMargin = 0;
      if (this.home.getWalls().size() > 0) {
        extraMargin = WALL_STROKE_WIDTH / 2;
      }
      if (this.home.getDimensionLines().size() > 0) {
        float dimensionLinesTextHeight = g2D.getFontMetrics().getHeight() * 1.5f;
        extraMargin = Math.max(extraMargin, dimensionLinesTextHeight);
      }
      float printScale = (float)Math.min(imageableWidth / (printedObjectBounds.getWidth() + 2 * extraMargin),
          imageableHeight / (printedObjectBounds.getHeight() + 2 * extraMargin));
      g2D.scale(printScale, printScale);
      g2D.translate(-printedObjectBounds.getMinX() + extraMargin,
          -printedObjectBounds.getMinY() + extraMargin);
      // Center plan in component
      g2D.translate((imageableWidth / printScale - printedObjectBounds.getWidth() - 2 * extraMargin) / 2, 
          (imageableHeight / printScale - printedObjectBounds.getHeight() - 2 * extraMargin) / 2);
      setRenderingHints(g2D);
      // Print component contents
      paintContent(g2D, printScale, Color.WHITE, Color.BLACK, PaintMode.PRINT);   
      g2D.dispose();
      return PAGE_EXISTS;
    } else {
      return NO_SUCH_PAGE;
    }
  }
  
  /**
   * Returns an image of the selected items displayed by this component 
   * (camera excepted) with no outline at scale 1/1 (1 pixel = 1cm).
   */
  public BufferedImage getClipboardImage() {
    // Create an image that contains only selected items
    Rectangle2D selectionBounds = getSelectionBounds(false);
    if (selectionBounds == null) {
      return null;
    } else {
      // Use a scale of 1
      float clipboardScale = 1f;
      float extraMargin = 0;
      List<Object> selectedItems = this.home.getSelectedItems();
      if (Home.getWallsSubList(selectedItems).size() > 0) {
        extraMargin = WALL_STROKE_WIDTH / 2;
      }
      if (Home.getDimensionLinesSubList(selectedItems).size() > 0) {
        float dimensionLinesTextHeight = getFontMetrics(getFont()).getHeight() * 1.5f * clipboardScale;
        extraMargin = Math.max(extraMargin, dimensionLinesTextHeight);
      }
      BufferedImage image = new BufferedImage((int)Math.ceil(selectionBounds.getWidth() * clipboardScale + 2 * extraMargin), 
              (int)Math.ceil(selectionBounds.getHeight() * clipboardScale + 2 * extraMargin), BufferedImage.TYPE_INT_RGB);      
      Graphics2D g2D = (Graphics2D)image.getGraphics();
      // Paint background in white
      g2D.setColor(Color.WHITE);
      g2D.fillRect(0, 0, image.getWidth(), image.getHeight());
      // Change component coordinates system to plan system
      g2D.scale(clipboardScale, clipboardScale);
      g2D.translate(-selectionBounds.getMinX() + extraMargin / clipboardScale,
          -selectionBounds.getMinY() + extraMargin / clipboardScale);
      setRenderingHints(g2D);
      // Paint component contents
      paintContent(g2D, clipboardScale, Color.WHITE, Color.BLACK, PaintMode.CLIPBOARD);   
      g2D.dispose();
      return image;
    }
  }
  
  /**
   * Sets rendering hints used to paint plan.
   */
  private void setRenderingHints(Graphics2D g2D) {
    g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g2D.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
  }

  /**
   * Fills the background. 
   */
  private void paintBackground(Graphics2D g2D, Color backgroundColor) {
    if (isOpaque()) {
      g2D.setColor(backgroundColor);
      g2D.fillRect(0, 0, getWidth(), getHeight());
    }
  }

  /**
   * Paints background image.
   */
  private void paintBackgroundImage(Graphics2D g2D) {
    final BackgroundImage backgroundImage = this.home.getBackgroundImage();
    if (backgroundImage == null) {
      this.backgroundImageCache = null;
    } else {
      if (this.backgroundImageCache == null) {
        // Load background image in an executor
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
              InputStream contentStream = null;
              try {
                contentStream = backgroundImage.getImage().openStream();
                backgroundImageCache = ImageIO.read(contentStream);
                contentStream.close();
              } catch (IOException ex) {
                backgroundImageCache = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
                // Ignore exceptions, the user may know its background image is incorrect 
                // if he tries to modify the background image
              } 
              invalidatePlanBoundsAndRevalidate();
              repaint();
            } 
          });
      } 
      // Paint image at specified scale with 0.7 alpha
      AffineTransform previousTransform = g2D.getTransform();
      g2D.translate(-backgroundImage.getXOrigin(), -backgroundImage.getYOrigin());
      g2D.scale(backgroundImage.getScale(), backgroundImage.getScale());
      Composite oldComposite = g2D.getComposite();
      g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
      g2D.drawImage(this.backgroundImageCache, 0, 0, this);
      g2D.setComposite(oldComposite);
      g2D.setTransform(previousTransform);
    }
  }

  /**
   * Paints background grid lines.
   */
  private void paintGrid(Graphics2D g2D, float gridScale) {
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
    for (int i = 1; i < gridSizes.length && gridSize * gridScale < 10; i++) {
      gridSize = gridSizes [i];
    }
    
    Rectangle2D planBounds = getPlanBounds();    
    float xMin = (float)planBounds.getMinX() - MARGIN;
    float yMin = (float)planBounds.getMinY() - MARGIN;
    float xMax = convertXPixelToModel(getWidth());
    float yMax = convertYPixelToModel(getHeight());

    g2D.setColor(UIManager.getColor("controlShadow"));
    g2D.setStroke(new BasicStroke(0.5f / gridScale));
    // Draw vertical lines
    for (float x = (int) (xMin / gridSize) * gridSize; x < xMax; x += gridSize) {
      g2D.draw(new Line2D.Float(x, yMin, x, yMax));
    }
    // Draw horizontal lines
    for (float y = (int) (yMin / gridSize) * gridSize; y < yMax; y += gridSize) {
      g2D.draw(new Line2D.Float(xMin, y, xMax, y));
    }

    if (mainGridSize != gridSize) {
      g2D.setStroke(new BasicStroke(1.5f / gridScale,
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
  private void paintContent(Graphics2D g2D, float planScale, 
                            Color backgroundColor, Color foregroundColor, PaintMode paintMode) {
    List<Object> selectedItems = this.home.getSelectedItems();
    Color selectionColor = getSelectioncolor(); 
    Paint selectionOutlinePaint = new Color(selectionColor.getRed(), selectionColor.getGreen(), 
        selectionColor.getBlue(), 128);
    Stroke selectionOutlineStroke = new BasicStroke(6 / planScale, 
        BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND); 
    Stroke dimensionLinesSelectionOutlineStroke = new BasicStroke(4 / planScale, 
        BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND); 
    Stroke locationFeedbackStroke = new BasicStroke(
        1 / planScale, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 0, 
        new float [] {20 / planScale, 5 / planScale, 5 / planScale, 5 / planScale}, 4 / planScale);
    
    paintWalls(g2D, selectedItems, selectionOutlinePaint, selectionOutlineStroke, selectionColor, 
        planScale, backgroundColor, foregroundColor, paintMode);
    paintFurniture(g2D, selectedItems, selectionOutlinePaint, selectionOutlineStroke, selectionColor, 
        planScale, backgroundColor, foregroundColor, paintMode);
    paintDimensionLines(g2D, selectedItems, selectionOutlinePaint, dimensionLinesSelectionOutlineStroke, selectionColor, 
        locationFeedbackStroke, planScale, foregroundColor, paintMode);
    if (paintMode == PaintMode.PAINT) {
      paintCamera(g2D, selectedItems, selectionOutlinePaint, selectionOutlineStroke, selectionColor, 
          planScale, backgroundColor, foregroundColor);
      paintWallAlignmentFeedback(g2D, selectionColor, locationFeedbackStroke, planScale);
      paintDimensionLineAlignmentFeedback(g2D, selectionColor, locationFeedbackStroke, planScale);
      paintRectangleFeedback(g2D, selectionColor, planScale);
    }
  }

  /**
   * Returns the color used to draw selection outlines. 
   */
  private Color getSelectioncolor() {
    if (OperatingSystem.isMacOSX()) {
      if (OperatingSystem.isMacOSXLeopardOrSuperior()) {
        Color selectionColor = UIManager.getColor("Focus.color").darker();
        if (selectionColor != null) {
          return selectionColor;
        } else {
          return UIManager.getColor("List.selectionBackground").darker();
        }
      } else { 
        return UIManager.getColor("textHighlight");
      }
    } else {
      // On systems different from Mac OS X, take a darker color
      return UIManager.getColor("textHighlight").darker();
    }
  }

  /**
   * Paints walls. 
   */
  private void paintWalls(Graphics2D g2D, List<Object> selectedItems,   
                          Paint selectionOutlinePaint, Stroke selectionOutlineStroke, 
                          Paint indicatorPaint, float planScale, 
                          Color backgroundColor, Color foregroundColor, PaintMode paintMode) {
    float scaleInverse = 1 / planScale;
    Collection<Wall> paintedWalls;
    Shape wallsArea;
    if (paintMode != PaintMode.CLIPBOARD) {
      wallsArea = getWallsArea();
    } else {
      paintedWalls = new ArrayList<Wall>();
      for (Object item : selectedItems) {
        // In clipboard paint mode, paint wall only if it is selected
        if (item instanceof Wall) {
          paintedWalls.add((Wall)item);
        }
      }
      wallsArea = getWallsArea(paintedWalls);
    }
    // Fill walls area
    float wallPaintScale = paintMode == PaintMode.PRINT
        ? planScale / 72 * 150 // Adjust scale to 150 dpi for print
        : planScale;
    g2D.setPaint(getWallPaint(wallPaintScale, backgroundColor, foregroundColor));
    g2D.fill(wallsArea);
    
    if (paintMode == PaintMode.PAINT) {
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
          g2D.scale(scaleInverse, scaleInverse);
          g2D.setPaint(indicatorPaint);         
          g2D.setStroke(indicatorStroke);
          g2D.fill(WALL_POINT);
          
          double wallAngle = Math.atan2(wall.getYEnd() - wall.getYStart(), 
              wall.getXEnd() - wall.getXStart());
          double distanceAtScale = Point2D.distance(wall.getXStart(), wall.getYStart(), 
              wall.getXEnd(), wall.getYEnd()) * planScale;
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
          g2D.scale(scaleInverse, scaleInverse);
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
    }
    // Draw walls area
    g2D.setPaint(foregroundColor);
    g2D.setStroke(new BasicStroke(WALL_STROKE_WIDTH / planScale));
    g2D.draw(wallsArea);
    
    // Paint resize indicator of selected wall
    if (selectedItems.size() == 1 
        && selectedItems.get(0) instanceof Wall
        && paintMode == PaintMode.PAINT) {
      paintWallResizeIndicator(g2D, (Wall)selectedItems.get(0), indicatorPaint, planScale);
    }
  }

  /**
   * Paints resize indicator on <code>wall</code>.
   */
  private void paintWallResizeIndicator(Graphics2D g2D, Wall wall,
                                        Paint indicatorPaint, 
                                        float planScale) {
    if (this.resizeIndicatorVisible) {
      g2D.setPaint(indicatorPaint);
      g2D.setStroke(new BasicStroke(1.5f));

      double wallAngle = Math.atan2(wall.getYEnd() - wall.getYStart(), 
          wall.getXEnd() - wall.getXStart());
      
      AffineTransform previousTransform = g2D.getTransform();
      float scaleInverse = 1 / planScale;
      // Draw resize indicator at wall start point
      g2D.translate(wall.getXStart(), wall.getYStart());
      g2D.scale(scaleInverse, scaleInverse);
      g2D.rotate(wallAngle + Math.PI);
      g2D.draw(WALL_AND_LINE_RESIZE_INDICATOR);
      g2D.setTransform(previousTransform);
      
      // Draw resize indicator at wall end point
      g2D.translate(wall.getXEnd(), wall.getYEnd());
      g2D.scale(scaleInverse, scaleInverse);
      g2D.rotate(wallAngle);
      g2D.draw(WALL_AND_LINE_RESIZE_INDICATOR);
      g2D.setTransform(previousTransform);
    }
  }
  
  /**
   * Returns an area matching the union of all home wall shapes. 
   */
  private Area getWallsArea() {
    if (this.wallsAreaCache == null) {
      this.wallsAreaCache = getWallsArea(this.home.getWalls());
    }
    return this.wallsAreaCache;
  }
  
  /**
   * Returns an area matching the union of all <code>walls</code> shapes. 
   */
  private Area getWallsArea(Collection<Wall> walls) {
    Area wallsArea = new Area();
    for (Wall wall : walls) {
      wallsArea.add(new Area(getShape(wall.getPoints())));
    }
    return wallsArea;
  }
  
  /**
   * Returns the <code>Paint</code> object used to fill walls.
   */
  private Paint getWallPaint(float planScale, Color backgroundColor, Color foregroundColor) {
    BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
    Graphics2D imageGraphics = (Graphics2D)image.getGraphics();
    // Create an image displaying a line in its diagonal
    imageGraphics.setPaint(backgroundColor);
    imageGraphics.fillRect(0, 0, 10, 10);
    imageGraphics.setColor(foregroundColor);
    imageGraphics.drawLine(0, 9, 9, 0);
    imageGraphics.dispose();
    return new TexturePaint(image, 
        new Rectangle2D.Float(0, 0, 10 / planScale, 10 / planScale));
  }
  
  /**
   * Paints home furniture.
   */
  private void paintFurniture(Graphics2D g2D, List<Object> selectedItems,  
                              Paint selectionOutlinePaint, Stroke selectionOutlineStroke, 
                              Paint indicatorPaint, float planScale, 
                              Color backgroundColor, Color foregroundColor, PaintMode paintMode) {
    BasicStroke pieceBorderStroke = new BasicStroke(1f / planScale);
    if (this.sortedHomeFurniture == null) {
      // Sort home furniture in elevation order
      this.sortedHomeFurniture = 
          new ArrayList<HomePieceOfFurniture>(this.home.getFurniture());
      Collections.sort(this.sortedHomeFurniture,
          new Comparator<HomePieceOfFurniture>() {
            public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
              float elevationDelta = piece1.getElevation() - piece2.getElevation();
              if (elevationDelta < 0) {
                return -1;
              } else if (elevationDelta > 0) {
                return 1;
              } else {
                return 0;
              }
            }
          });
    }
    // Draw furniture
    for (HomePieceOfFurniture piece : this.sortedHomeFurniture) {
      if (piece.isVisible()) {
        boolean selectedPiece = selectedItems.contains(piece);
        // In clipboard paint mode, paint piece only if it is selected
        if (paintMode != PaintMode.CLIPBOARD
            || selectedPiece) {
          Shape pieceShape = getShape(piece.getPoints());
          // Fill piece area
          g2D.setPaint(backgroundColor);
          g2D.fill(pieceShape);
          // Draw its icon
          paintPieceOfFurnitureIcon(g2D, piece, pieceShape, planScale);
          
          if (selectedPiece
              && paintMode == PaintMode.PAINT) {
            // Draw selection border
            g2D.setPaint(selectionOutlinePaint);
            g2D.setStroke(selectionOutlineStroke);
            g2D.draw(pieceShape);
          }        
          // Draw its border
          g2D.setPaint(foregroundColor);
          g2D.setStroke(pieceBorderStroke);
          g2D.draw(pieceShape);
          
          if (selectedItems.size() == 1 
              && selectedItems.get(0) == piece
              && paintMode == PaintMode.PAINT) {
            paintPieceOFFurnitureIndicators(g2D, 
                (HomePieceOfFurniture)selectedItems.get(0), indicatorPaint, planScale);
          }
        }
      }
    }
  }

  /**
   * Paints <code>piece</code> icon with <code>g2D</code>.
   */
  private void paintPieceOfFurnitureIcon(Graphics2D g2D, HomePieceOfFurniture piece, 
                                         Shape pieceShape, float planScale) {
    Shape previousClip = g2D.getClip();
    // Clip icon drawing into piece shape
    g2D.clip(pieceShape);
    AffineTransform previousTransform = g2D.getTransform();
    // Get piece icon
    Icon icon = IconManager.getInstance().getIcon(piece.getIcon(), 128, this);
    // Translate to piece center
    g2D.translate(piece.getX(), piece.getY());
    // Scale icon to fit in its area
    float minDimension = Math.min(piece.getWidth(), piece.getDepth());
    float iconScale = Math.min(1 / planScale, minDimension / icon.getIconHeight());
    // If piece model is mirrored, inverse x scale
    if (piece.isModelMirrored()) {
      g2D.scale(-iconScale, iconScale);
    } else {
      g2D.scale(iconScale, iconScale);
    }
    // Paint piece icon
    icon.paintIcon(this, g2D, -icon.getIconWidth() / 2, -icon.getIconHeight() / 2);
    // Revert g2D transformation to previous value
    g2D.setTransform(previousTransform);
    g2D.setClip(previousClip);
  }

  /**
   * Paints rotation, elevation, height and resize indicators on <code>piece</code>.
   */
  private void paintPieceOFFurnitureIndicators(Graphics2D g2D, 
                                               HomePieceOfFurniture piece,
                                               Paint indicatorPaint,
                                               float planScale) {
    if (this.resizeIndicatorVisible) {
      g2D.setPaint(indicatorPaint);
      g2D.setStroke(new BasicStroke(1.5f));
      
      AffineTransform previousTransform = g2D.getTransform();
      // Draw rotation indicator at top left vertex of the piece
      float [][] piecePoints = piece.getPoints();
      float scaleInverse = 1 / planScale;
      float pieceAngle = piece.getAngle();
      g2D.translate(piecePoints [0][0], piecePoints [0][1]);
      g2D.scale(scaleInverse, scaleInverse);
      g2D.rotate(pieceAngle);
      g2D.draw(FURNITURE_ROTATION_INDICATOR);
      g2D.setTransform(previousTransform);

      // Draw elevation indicator at top right vertex of the piece
      g2D.translate(piecePoints [1][0], piecePoints [1][1]);
      g2D.scale(scaleInverse, scaleInverse);
      g2D.rotate(pieceAngle);
      g2D.draw(FURNITURE_ELEVATION_POINT_INDICATOR);
      // Place elevation indicator farther but don't rotate it
      g2D.translate(6.5f, -6.5f);
      g2D.rotate(-pieceAngle); 
      g2D.draw(FURNITURE_ELEVATION_INDICATOR);
      g2D.setTransform(previousTransform);
      
      // Draw height indicator at bottom left vertex of the piece
      g2D.translate(piecePoints [3][0], piecePoints [3][1]);
      g2D.scale(scaleInverse, scaleInverse);
      g2D.rotate(pieceAngle);
      g2D.draw(FURNITURE_HEIGHT_POINT_INDICATOR);
      // Place height indicator farther but don't rotate it
      g2D.translate(-7.5f, 7.5f);
      g2D.rotate(-pieceAngle);
      g2D.draw(FURNITURE_HEIGHT_INDICATOR);
      g2D.setTransform(previousTransform);
      
      // Draw resize indicator at top left vertex of the piece
      g2D.translate(piecePoints [2][0], piecePoints [2][1]);
      g2D.scale(scaleInverse, scaleInverse);
      g2D.rotate(pieceAngle);
      g2D.draw(FURNITURE_RESIZE_INDICATOR);
      g2D.setTransform(previousTransform);
    }
  }
  
  /**
   * Paints dimension lines. 
   */
  private void paintDimensionLines(Graphics2D g2D, List<Object> selectedItems,   
                          Paint selectionOutlinePaint, Stroke selectionOutlineStroke, 
                          Paint indicatorPaint, Stroke extensionLineStroke, float planScale, 
                          Color foregroundColor, PaintMode paintMode) {
    Collection<DimensionLine> paintedDimensionLines;
    if (paintMode != PaintMode.CLIPBOARD) {
      paintedDimensionLines = this.home.getDimensionLines();
    } else {
      paintedDimensionLines = new ArrayList<DimensionLine>();
      for (Object item : selectedItems) {
        // In clipboard paint mode, paint dimension line only if it is selected
        if (item instanceof DimensionLine) {
          paintedDimensionLines.add((DimensionLine)item);
        }
      }
    }

    // Draw dimension lines
    g2D.setPaint(foregroundColor);
    BasicStroke dimensionLineStroke = new BasicStroke(1 / planScale);
    // Change font size
    Font previousFont = g2D.getFont();
    g2D.setFont(previousFont.deriveFont(previousFont.getSize2D() * 1.5f));
    FontMetrics fontMetrics = g2D.getFontMetrics();
    for (DimensionLine dimensionLine : paintedDimensionLines) {
      AffineTransform previousTransform = g2D.getTransform();
      double angle = Math.atan2(dimensionLine.getYEnd() - dimensionLine.getYStart(), 
          dimensionLine.getXEnd() - dimensionLine.getXStart());
      float dimensionLineLength = (float)Point2D.distance(dimensionLine.getXStart(), dimensionLine.getYStart(), 
          dimensionLine.getXEnd(), dimensionLine.getYEnd());
      g2D.translate(dimensionLine.getXStart(), dimensionLine.getYStart());
      g2D.rotate(angle);
      g2D.translate(0, dimensionLine.getOffset());
      
      if (paintMode == PaintMode.PAINT
          && selectedItems.contains(dimensionLine)) {
        // Draw selection border
        g2D.setPaint(selectionOutlinePaint);
        g2D.setStroke(selectionOutlineStroke);
        // Draw dimension line
        g2D.draw(new Line2D.Float(0, 0, dimensionLineLength, 0));
        // Draw dimension line ends
        g2D.draw(DIMENSION_LINE_END);
        g2D.translate(dimensionLineLength, 0);
        g2D.draw(DIMENSION_LINE_END);
        g2D.translate(-dimensionLineLength, 0);
        // Draw extension lines
        g2D.draw(new Line2D.Float(0, -dimensionLine.getOffset(), 0, -5));
        g2D.draw(new Line2D.Float(dimensionLineLength, -dimensionLine.getOffset(), dimensionLineLength, -5));
        
        g2D.setPaint(foregroundColor);
      }
      
      g2D.setStroke(dimensionLineStroke);
      // Draw dimension line
      g2D.draw(new Line2D.Float(0, 0, dimensionLineLength, 0));
      // Draw dimension line ends
      g2D.draw(DIMENSION_LINE_END);
      g2D.translate(dimensionLineLength, 0);
      g2D.draw(DIMENSION_LINE_END);
      g2D.translate(-dimensionLineLength, 0);
      // Draw extension lines
      g2D.setStroke(extensionLineStroke);
      g2D.draw(new Line2D.Float(0, -dimensionLine.getOffset(), 0, -5));
      g2D.draw(new Line2D.Float(dimensionLineLength, -dimensionLine.getOffset(), dimensionLineLength, -5));
      
      // Draw dimension length in middle
      String lengthText = this.preferences.getUnit().getLengthFormat().format(dimensionLineLength);
      Rectangle2D lengthTextBounds = fontMetrics.getStringBounds(lengthText, g2D);
      
      g2D.drawString(lengthText, 
          (dimensionLineLength - (float)lengthTextBounds.getWidth()) / 2, 
          dimensionLine.getOffset() <= 0 
              ? -fontMetrics.getDescent() - 1 / planScale
              : fontMetrics.getAscent() + 1 / planScale);
      
      g2D.setTransform(previousTransform);
    }
    g2D.setFont(previousFont);
    
    // Paint resize indicator of selected dimension line
    if (selectedItems.size() == 1 
        && selectedItems.get(0) instanceof DimensionLine
        && paintMode == PaintMode.PAINT) {
      paintDimensionLineResizeIndicator(g2D, (DimensionLine)selectedItems.get(0), indicatorPaint, planScale);
    }
  }

  /**
   * Paints resize indicator on a given dimension line.
   */
  private void paintDimensionLineResizeIndicator(Graphics2D g2D, DimensionLine dimensionLine,
                                                 Paint indicatorPaint, 
                                                 float planScale) {
    if (this.resizeIndicatorVisible) {
      g2D.setPaint(indicatorPaint);
      g2D.setStroke(new BasicStroke(1.5f));

      double wallAngle = Math.atan2(dimensionLine.getYEnd() - dimensionLine.getYStart(), 
          dimensionLine.getXEnd() - dimensionLine.getXStart());
      
      AffineTransform previousTransform = g2D.getTransform();
      float scaleInverse = 1 / planScale;
      // Draw resize indicator at the start of dimension line 
      g2D.translate(dimensionLine.getXStart(), dimensionLine.getYStart());
      g2D.rotate(wallAngle);
      g2D.translate(0, dimensionLine.getOffset());
      g2D.rotate(Math.PI);
      g2D.scale(scaleInverse, scaleInverse);
      g2D.draw(WALL_AND_LINE_RESIZE_INDICATOR);
      g2D.setTransform(previousTransform);
      
      // Draw resize indicator at the end of dimension line 
      g2D.translate(dimensionLine.getXEnd(), dimensionLine.getYEnd());
      g2D.rotate(wallAngle);
      g2D.translate(0, dimensionLine.getOffset());
      g2D.scale(scaleInverse, scaleInverse);
      g2D.draw(WALL_AND_LINE_RESIZE_INDICATOR);
      g2D.setTransform(previousTransform);

      // Draw resize indicator at the middle of dimension line
      g2D.translate((dimensionLine.getXStart() + dimensionLine.getXEnd()) / 2, 
          (dimensionLine.getYStart() + dimensionLine.getYEnd()) / 2);
      g2D.rotate(wallAngle);
      g2D.translate(0, dimensionLine.getOffset());
      g2D.rotate(dimensionLine.getOffset() <= 0 
          ? Math.PI / 2 
          : -Math.PI / 2);
      g2D.scale(scaleInverse, scaleInverse);
      g2D.draw(WALL_AND_LINE_RESIZE_INDICATOR);
      g2D.setTransform(previousTransform);
    }
  }
  
  /**
   * Paints wall location feedback.
   */
  private void paintWallAlignmentFeedback(Graphics2D g2D, 
                                          Paint feedbackPaint, Stroke feedbackStroke,
                                          float planScale) {
    // Paint wall location feedback
    if (this.wallLocationFeeback != null) {
      float margin = 1f / planScale;
      // Seach which wall start or end point is at wallLocationFeeback abcissa or ordinate
      // ignoring the start and end point of wallAlignmentFeedback
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

          float [][] alignedWallPoints = alignedWall.getPoints();
          for (int i = 0; i < alignedWallPoints.length; i++) {
            if (Math.abs(x - alignedWallPoints [i][0]) < margin
                && (this.wallAlignmentFeedback == null
                    || !equalsWallPoint(alignedWallPoints [i][0], alignedWallPoints [i][1], this.wallAlignmentFeedback))) {
              if (Math.abs(deltaYToClosestWall) > Math.abs(y - alignedWallPoints [i][1])) {
                deltaYToClosestWall = y - alignedWallPoints [i][1];
              }
            }
            if (Math.abs(y - alignedWallPoints [i][1]) < margin
                && (this.wallAlignmentFeedback == null
                    || !equalsWallPoint(alignedWallPoints [i][0], alignedWallPoints [i][1], this.wallAlignmentFeedback))) {
              if (Math.abs(deltaXToClosestWall) > Math.abs(x - alignedWallPoints [i][0])) {
                deltaXToClosestWall = x - alignedWallPoints [i][0];
              }                
            }
          }
        }
      }
      
      // Draw alignment horizontal and vertical lines
      g2D.setPaint(feedbackPaint);         
      g2D.setStroke(feedbackStroke);
      if (deltaXToClosestWall != Float.POSITIVE_INFINITY) {
        if (deltaXToClosestWall > 0) {
          g2D.draw(new Line2D.Float(x + 25 / planScale, y, 
              x - deltaXToClosestWall - 25 / planScale, y));
        } else {
          g2D.draw(new Line2D.Float(x - 25 / planScale, y, 
              x - deltaXToClosestWall + 25 / planScale, y));
        }
      }

      if (deltaYToClosestWall != Float.POSITIVE_INFINITY) {
        if (deltaYToClosestWall > 0) {
          g2D.draw(new Line2D.Float(x, y + 25 / planScale, 
              x, y - deltaYToClosestWall - 25 / planScale));
        } else {
          g2D.draw(new Line2D.Float(x, y - 25 / planScale, 
              x, y - deltaYToClosestWall + 25 / planScale));
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
   * Paints dimension line location feedback.
   */
  private void paintDimensionLineAlignmentFeedback(Graphics2D g2D, 
                                                   Paint feedbackPaint, Stroke feedbackStroke,
                                                   float planScale) {
    // Paint dimension line location feedback
    if (this.dimensionLineLocationFeeback != null) {
      float margin = 1f / planScale;
      // Seach which dimension line start or end point is at dimensionLineLocationFeeback abcissa or ordinate
      // ignoring the start and end point of dimensionLineFeeback
      float x = (float)this.dimensionLineLocationFeeback.getX(); 
      float y = (float)this.dimensionLineLocationFeeback.getY();
      float deltaXToClosestObject = Float.POSITIVE_INFINITY;
      float deltaYToClosestObject = Float.POSITIVE_INFINITY;
      for (DimensionLine alignedDimensionLine : this.home.getDimensionLines()) {
        if (alignedDimensionLine != this.dimensionLineAlignmentFeedback) {
          if (Math.abs(x - alignedDimensionLine.getXStart()) < margin
              && (this.dimensionLineAlignmentFeedback == null
                  || !equalsDimensionLinePoint(alignedDimensionLine.getXStart(), alignedDimensionLine.getYStart(), 
                          this.dimensionLineAlignmentFeedback))) {
            if (Math.abs(deltaYToClosestObject) > Math.abs(y - alignedDimensionLine.getYStart())) {
              deltaYToClosestObject = y - alignedDimensionLine.getYStart();
            }
          } else if (Math.abs(x - alignedDimensionLine.getXEnd()) < margin
                    && (this.dimensionLineAlignmentFeedback == null
                        || !equalsDimensionLinePoint(alignedDimensionLine.getXEnd(), alignedDimensionLine.getYEnd(), 
                                this.dimensionLineAlignmentFeedback))) {
            if (Math.abs(deltaYToClosestObject) > Math.abs(y - alignedDimensionLine.getYEnd())) {
              deltaYToClosestObject = y - alignedDimensionLine.getYEnd();
            }                
          }
          if (Math.abs(y - alignedDimensionLine.getYStart()) < margin
              && (this.dimensionLineAlignmentFeedback == null
                  || !equalsDimensionLinePoint(alignedDimensionLine.getXStart(), alignedDimensionLine.getYStart(), 
                          this.dimensionLineAlignmentFeedback))) {
            if (Math.abs(deltaXToClosestObject) > Math.abs(x - alignedDimensionLine.getXStart())) {
              deltaXToClosestObject = x - alignedDimensionLine.getXStart();
            }
          } else if (Math.abs(y - alignedDimensionLine.getYEnd()) < margin
                    && (this.dimensionLineAlignmentFeedback == null
                        || !equalsDimensionLinePoint(alignedDimensionLine.getXEnd(), alignedDimensionLine.getYEnd(), 
                                this.dimensionLineAlignmentFeedback))) {
            if (Math.abs(deltaXToClosestObject) > Math.abs(x - alignedDimensionLine.getXEnd())) {
              deltaXToClosestObject = x - alignedDimensionLine.getXEnd();
            }                
          }
        }
      }
      // Seach which wall points is at dimensionLineLocationFeeback abcissa or ordinate
      for (Wall alignedWall : this.home.getWalls()) {
        float [][] alignedWallPoints = alignedWall.getPoints();
        for (int i = 0; i < alignedWallPoints.length; i++) {
          if (Math.abs(x - alignedWallPoints [i][0]) < margin
              && Math.abs(deltaYToClosestObject) > Math.abs(y - alignedWallPoints [i][1])) {
            deltaYToClosestObject = y - alignedWallPoints [i][1];
          }
          if (Math.abs(y - alignedWallPoints [i][1]) < margin
              && Math.abs(deltaXToClosestObject) > Math.abs(x - alignedWallPoints [i][0])) {
            deltaXToClosestObject = x - alignedWallPoints [i][0];
          }
        }
      }
      // Seach which piece of furniture points is at dimensionLineLocationFeeback abcissa or ordinate
      for (HomePieceOfFurniture alignedFurniture : this.home.getFurniture()) {
        float [][] alignedPiecePoints = alignedFurniture.getPoints();
        for (int i = 0; i < alignedPiecePoints.length; i++) {
          if (Math.abs(x - alignedPiecePoints [i][0]) < margin
              && Math.abs(deltaYToClosestObject) > Math.abs(y - alignedPiecePoints [i][1])) {
            deltaYToClosestObject = y - alignedPiecePoints [i][1];
          }
          if (Math.abs(y - alignedPiecePoints [i][1]) < margin
              && Math.abs(deltaXToClosestObject) > Math.abs(x - alignedPiecePoints [i][0])) {
            deltaXToClosestObject = x - alignedPiecePoints [i][0];
          }
        }
      }
      
      // Draw alignment horizontal and vertical lines
      g2D.setPaint(feedbackPaint);         
      g2D.setStroke(feedbackStroke);
      if (deltaXToClosestObject != Float.POSITIVE_INFINITY) {
        if (deltaXToClosestObject > 0) {
          g2D.draw(new Line2D.Float(x + 25 / planScale, y, 
              x - deltaXToClosestObject - 25 / planScale, y));
        } else {
          g2D.draw(new Line2D.Float(x - 25 / planScale, y, 
              x - deltaXToClosestObject + 25 / planScale, y));
        }
      }

      if (deltaYToClosestObject != Float.POSITIVE_INFINITY) {
        if (deltaYToClosestObject > 0) {
          g2D.draw(new Line2D.Float(x, y + 25 / planScale, 
              x, y - deltaYToClosestObject - 25 / planScale));
        } else {
          g2D.draw(new Line2D.Float(x, y - 25 / planScale, 
              x, y - deltaYToClosestObject + 25 / planScale));
        }
      }
    }
  }
  
  /**
   * Returns <code>true</code> if <code>dimensionLine</code> start or end point 
   * equals the point (<code>x</code>, <code>y</code>).
   */
  private boolean equalsDimensionLinePoint(float x, float y, DimensionLine dimensionLine) {
    return x == dimensionLine.getXStart() && y == dimensionLine.getYStart()
           || x == dimensionLine.getXEnd() && y == dimensionLine.getYEnd();
  }

  /**
   * Paints the observer camera at its current location, if home camera is the observer camera.
   */
  private void paintCamera(Graphics2D g2D, List<Object> selectedItems,
                           Paint selectionOutlinePaint, Stroke selectionOutlineStroke, 
                           Paint indicatorPaint, float planScale, 
                           Color backgroundColor, Color foregroundColor) {
    ObserverCamera camera = this.home.getObserverCamera();
    if (camera == this.home.getCamera()) {
      AffineTransform oldTransform = g2D.getTransform();
      g2D.translate(camera.getX(), camera.getY());
      g2D.rotate(camera.getYaw());
  
      // Compute camera drawing at scale
      AffineTransform cameraTransform = new AffineTransform();
      float [][] points = camera.getPoints();
      double yScale = Point2D.distance(points [0][0], points [0][1], points [3][0], points [3][1]);
      double xScale = Point2D.distance(points [0][0], points [0][1], points [1][0], points [1][1]);
      cameraTransform.scale(xScale, yScale);    
      Shape scaledCameraBody = 
          new Area(CAMERA_BODY).createTransformedArea(cameraTransform);
      Shape scaledCameraHead = 
          new Area(CAMERA_HEAD).createTransformedArea(cameraTransform);
      
      // Paint body
      g2D.setPaint(backgroundColor);
      g2D.fill(scaledCameraBody);
      g2D.setPaint(foregroundColor);
      BasicStroke stroke = new BasicStroke(1 / planScale);
      g2D.setStroke(stroke);
      g2D.draw(scaledCameraBody);
  
      if (selectedItems.contains(camera)) {
        g2D.setPaint(selectionOutlinePaint);
        g2D.setStroke(selectionOutlineStroke);
        Area cameraOutline = new Area(scaledCameraBody);
        cameraOutline.add(new Area(scaledCameraHead));
        g2D.draw(cameraOutline);      
      }
      
      // Paint head
      g2D.setPaint(backgroundColor);
      g2D.fill(scaledCameraHead);
      g2D.setPaint(foregroundColor);
      g2D.setStroke(stroke);
      g2D.draw(scaledCameraHead);
      // Paint field of sight angle
      double sin = (float)Math.sin(camera.getFieldOfView() / 2);
      double cos = (float)Math.cos(camera.getFieldOfView() / 2);
      float xStartAngle = (float)(0.9f * yScale * sin);
      float yStartAngle = (float)(0.9f * yScale * cos);
      float xEndAngle = (float)(2.2f * yScale * sin);
      float yEndAngle = (float)(2.2f * yScale * cos);
      GeneralPath cameraFieldOfViewAngle = new GeneralPath();
      cameraFieldOfViewAngle.moveTo(xStartAngle, yStartAngle);
      cameraFieldOfViewAngle.lineTo(xEndAngle, yEndAngle);
      cameraFieldOfViewAngle.moveTo(-xStartAngle, yStartAngle);
      cameraFieldOfViewAngle.lineTo(-xEndAngle, yEndAngle);
      g2D.draw(cameraFieldOfViewAngle);
      g2D.setTransform(oldTransform);
  
      // Paint resize indicator of selected camera
      if (selectedItems.size() == 1 
          && selectedItems.get(0) == camera) {
        paintCameraRotationIndicators(g2D, camera, indicatorPaint, planScale);
      }
    }
  }

  private void paintCameraRotationIndicators(Graphics2D g2D, 
                                             ObserverCamera camera, Paint indicatorPaint,
                                             float planScale) {
    if (this.resizeIndicatorVisible) {
      g2D.setPaint(indicatorPaint);
      g2D.setStroke(new BasicStroke(1.5f));
      
      AffineTransform previousTransform = g2D.getTransform();
      // Draw yaw rotation indicator at middle of first and last point of camera 
      float [][] cameraPoints = camera.getPoints();
      float scaleInverse = 1 / planScale;
      g2D.translate((cameraPoints [0][0] + cameraPoints [3][0]) / 2, 
          (cameraPoints [0][1] + cameraPoints [3][1]) / 2);
      g2D.scale(scaleInverse, scaleInverse);
      g2D.rotate(camera.getYaw());
      g2D.draw(CAMERA_YAW_ROTATION_INDICATOR);
      g2D.setTransform(previousTransform);

      // Draw pitch rotation indicator at middle of second and third point of camera 
      g2D.translate((cameraPoints [1][0] + cameraPoints [2][0]) / 2, 
          (cameraPoints [1][1] + cameraPoints [2][1]) / 2);
      g2D.scale(scaleInverse, scaleInverse);
      g2D.rotate(camera.getYaw());
      g2D.draw(CAMERA_PITCH_ROTATION_INDICATOR);
      g2D.setTransform(previousTransform);
    }
  }

  /**
   * Paints rectangle feedback.
   */
  private void paintRectangleFeedback(Graphics2D g2D, Color selectionColor, float planScale) {
    if (this.rectangleFeedback != null) {
      g2D.setPaint(new Color(selectionColor.getRed(), selectionColor.getGreen(), selectionColor.getBlue(), 32));
      g2D.fill(this.rectangleFeedback);
      g2D.setPaint(selectionColor);
      g2D.setStroke(new BasicStroke(1 / planScale));
      g2D.draw(this.rectangleFeedback);
    }
  }

  /**
   * Returns the shape matching the coordinates in <code>points</code> array.
   */
  private Shape getShape(float [][] points) {
    GeneralPath path = new GeneralPath();
    path.moveTo(points [0][0], points [0][1]);
    for (int i = 1; i < points.length; i++) {
      path.lineTo(points [i][0], points [i][1]);
    }
    path.closePath();
    return path;
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
            Rectangle2D selectionBounds = getSelectionBounds(true);
            if (selectionBounds != null) {
              Rectangle pixelBounds = getShapePixelBounds(selectionBounds);
              pixelBounds.grow(5, 5);
              scrollRectToVisible(pixelBounds);
            }
          }
        });
    }
  }

  /**
   * Returns the bounds of the selected items.
   */
  private Rectangle2D getSelectionBounds(boolean includeCamera) {
    // Compute bounds that include selected walls and furniture
    Rectangle2D selectionBounds = null;
    for (Object item : this.home.getSelectedItems()) {
      if (item instanceof Wall) {
        for (float [] point : ((Wall)item).getPoints()) {
          if (selectionBounds == null) {
            selectionBounds = new Rectangle2D.Float(point [0], point [1], 0, 0);
          } else {
            selectionBounds.add(point [0], point [1]);
          }
        }
      } else if (item instanceof HomePieceOfFurniture) {
        HomePieceOfFurniture piece = (HomePieceOfFurniture)item;
        if (piece.isVisible()) {
          for (float [] point : piece.getPoints()) {
            if (selectionBounds == null) {
              selectionBounds = new Rectangle2D.Float(point [0], point [1], 0, 0);
            } else {
              selectionBounds.add(point [0], point [1]);
            }
          }
        }
      } else if (item instanceof DimensionLine) {
        for (float [] point : ((DimensionLine)item).getPoints()) {
          if (selectionBounds == null) {
            selectionBounds = new Rectangle2D.Float(point [0], point [1], 0, 0);
          } else {
            selectionBounds.add(point [0], point [1]);
          }
        }
      } else if (item instanceof ObserverCamera) {
        for (float [] point : ((ObserverCamera)item).getPoints()) {
          if (selectionBounds == null) {
            selectionBounds = new Rectangle2D.Float(point [0], point [1], 0, 0);
          } else {
            selectionBounds.add(point [0], point [1]);
          }
        }
      } 
    }
    return selectionBounds;
  }

  /**
   * Ensures the point at (<code>xPixel</code>, <code>yPixel</code>) is visible,
   * moving scroll bars if needed.
   */
  public void makePointVisible(float x, float y) {
    scrollRectToVisible(getShapePixelBounds(new Rectangle2D.Float(x, y, 1 / getScale(), 1 / getScale())));
  }

  /**
   * Returns the scale used to display the plan.
   */
  public float getScale() {
    return this.scale;
  }

  /**
   * Sets the scale used to display the plan.
   * If this component is displayed in a viewport the view position is updated
   * to ensure it remains unchanged in model coordinates system.
   */
  public void setScale(float scale) {
    if (this.scale != scale) {
      JViewport parent = (JViewport)getParent();
      float xViewPosition = 0;
      float yViewPosition = 0;
      if (parent instanceof JViewport) {
        Point viewPosition = parent.getViewPosition();
        xViewPosition = convertXPixelToModel(viewPosition.x);
        yViewPosition = convertYPixelToModel(viewPosition.y);
      }
      
      this.scale = scale;
      revalidate();

      if (parent instanceof JViewport) {
        parent.setViewPosition(new Point(convertXModelToPixel(xViewPosition), 
            convertYModelToPixel(yViewPosition)));
      }
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
    return (x - insets.left) / getScale() - MARGIN + (float)planBounds.getMinX();
  }

  /**
   * Returns <code>y</code> converted in model coordinates space.
   */
  public float convertYPixelToModel(int y) {
    Insets insets = getInsets();
    Rectangle2D planBounds = getPlanBounds();    
    return (y - insets.top) / getScale() - MARGIN + (float)planBounds.getMinY();
  }

  /**
   * Returns <code>x</code> converted in view coordinates space.
   */
  private int convertXModelToPixel(float x) {
    Insets insets = getInsets();
    Rectangle2D planBounds = getPlanBounds();    
    return (int)Math.round((x - planBounds.getMinX() + MARGIN) * getScale()) + insets.left;
  }

  /**
   * Returns <code>y</code> converted in view coordinates space.
   */
  private int convertYModelToPixel(float y) {
    Insets insets = getInsets();
    Rectangle2D planBounds = getPlanBounds();    
    return (int)Math.round((y - planBounds.getMinY() + MARGIN) * getScale()) + insets.top;
  }

  /**
   * Returns the length in centimeters of a pixel with the current scale.
   */
  public float getPixelLength() {
    return 1 / getScale();
  }
  
  /**
   * Returns the bounds of <code>shape</code> in pixels coordinates space.
   */
  private Rectangle getShapePixelBounds(Shape shape) {
    Rectangle2D shapeBounds = shape.getBounds2D();
    return new Rectangle(
        convertXModelToPixel((float)shapeBounds.getMinX()), 
        convertYModelToPixel((float)shapeBounds.getMinY()),
        (int)Math.round(shapeBounds.getWidth() * getScale()),
        (int)Math.round(shapeBounds.getHeight() * getScale()));
  }
  
  /**
   * Sets the cursor of this component as rotation cursor. 
   */
  public void setRotationCursor() {
    setCursor(this.rotationCursor);
  }

  /**
   * Sets the cursor of this component as height cursor. 
   */
  public void setHeightCursor() {
    setCursor(this.heightCursor);
  }

  /**
   * Sets the cursor of this component as elevation cursor. 
   */
  public void setElevationCursor() {
    setCursor(this.elevationCursor);
  }

  /**
   * Sets the cursor of this component as resize cursor. 
   */
  public void setResizeCursor() {
    setCursor(this.resizeCursor);
  }
  
  /**
   * Sets the cursor of this component as duplication cursor. 
   */
  public void setDuplicationCursor() {
    setCursor(this.duplicationCursor);
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
      this.toolTipWindow.setFocusableWindowState(false);
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

  /**
   * Sets the location point for <code>dimensionLine</code> alignment feedback. 
   */
  public void setDimensionLineAlignmentFeeback(DimensionLine dimensionLine, float x, float y) {
    this.dimensionLineAlignmentFeedback = dimensionLine;
    this.dimensionLineLocationFeeback = new Point2D.Float(x, y);
    repaint();
  }
  
  /**
   * Deletes the dimension line alignment feedback. 
   */
  public void deleteDimensionLineAlignmentFeeback() {
    this.dimensionLineAlignmentFeedback = null;
    this.dimensionLineLocationFeeback = null;
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
    private int   orientation;
    private Point mouseLocation;

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
      addMouseListeners();
    }

    /**
     * Adds a mouse listener to this ruler that stores current mouse location. 
     */
    private void addMouseListeners() {
      MouseInputListener mouseInputListener = new MouseInputAdapter() {
          @Override
          public void mouseDragged(MouseEvent ev) {
            mouseLocation = ev.getPoint();
            repaint();
          }
  
          @Override
          public void mouseMoved(MouseEvent ev) {
            mouseLocation = ev.getPoint();
            repaint();
          }

          @Override
          public void mouseEntered(MouseEvent ev) {
            mouseLocation = ev.getPoint();
            repaint();
          }
          
          @Override
          public void mouseExited(MouseEvent ev) {
            mouseLocation = null;
            repaint();
          }
        };
      PlanComponent.this.addMouseListener(mouseInputListener);
      PlanComponent.this.addMouseMotionListener(mouseInputListener);
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
      // Clip component to avoid drawing in empty borders
      g2D.clipRect(insets.left, insets.top, 
          getWidth() - insets.left - insets.right, 
          getHeight() - insets.top - insets.bottom);
      // Change component coordinates system to plan system
      Rectangle2D planBounds = getPlanBounds();    
      float paintScale = getScale();
      g2D.translate(insets.left + (MARGIN - planBounds.getMinX()) * paintScale,
          insets.top + (MARGIN - planBounds.getMinY()) * paintScale);
      g2D.scale(paintScale, paintScale);
      g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      // Paint component contents
      paintRuler(g2D, paintScale);
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
    private void paintRuler(Graphics2D g2D, float rulerScale) {
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
      for (int i = 1; i < gridSizes.length && gridSize * rulerScale < 10; i++) {
        gridSize = gridSizes [i];
      }
      
      Rectangle2D planBounds = getPlanBounds();    
      float xMin = (float)planBounds.getMinX() - MARGIN;
      float yMin = (float)planBounds.getMinY() - MARGIN;
      float xMax = convertXPixelToModel(getWidth());
      float yMax = convertYPixelToModel(getHeight());
      boolean leftToRightOriented = getComponentOrientation() == ComponentOrientation.LEFT_TO_RIGHT;

      FontMetrics metrics = getFontMetrics(getFont());
      int fontAscent = metrics.getAscent();
      float tickSize = 5 / rulerScale;
      float mainTickSize = (fontAscent + 6) / rulerScale;
      NumberFormat format = NumberFormat.getNumberInstance();
      String maxText = getFormattedTickText(format, 100);
      int maxTextWidth = metrics.stringWidth(maxText) + 10;
      float textInterval =
        mainGridSize != gridSize
          ? mainGridSize 
          : (float)Math.ceil(maxTextWidth / (gridSize * rulerScale)) * gridSize;
      
      g2D.setColor(getForeground());
      float lineWidth = 0.5f / rulerScale;
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
            g2D.scale(1 / rulerScale, 1 / rulerScale);
            g2D.drawString(getFormattedTickText(format, x), 3, fontAscent - 1);
            g2D.setTransform(previousTransform);
          } else {
            // Draw small tick
            g2D.draw(new Line2D.Float(x, yMax - tickSize, x, yMax));
          }
        }
      } else {
        // Draw vertical rule base
        if (leftToRightOriented) {
          g2D.draw(new Line2D.Float(xMax - lineWidth, yMin, xMax - lineWidth, yMax));
        } else {
          g2D.draw(new Line2D.Float(xMin + lineWidth, yMin, xMin + lineWidth, yMax));
        }
        // Draw horizontal lines
        for (float y = (int) (yMin / gridSize) * gridSize; y < yMax; y += gridSize) {
          if (Math.abs(Math.abs(y) % textInterval - textInterval) < 1E-2 
              || Math.abs(Math.abs(y) % textInterval) < 1E-2) {
            AffineTransform previousTransform = g2D.getTransform();
            String yText = getFormattedTickText(format, y);
            if (leftToRightOriented) {
              // Draw big tick
              g2D.draw(new Line2D.Float(xMax - mainTickSize, y, xMax, y));
              // Draw unit text with a vertical orientation
              g2D.translate(xMax - mainTickSize, y);
              g2D.scale(1 / rulerScale, 1 / rulerScale);
              g2D.rotate(-Math.PI / 2);
              g2D.drawString(yText, -metrics.stringWidth(yText) - 3, fontAscent - 1);
            } else {
              // Draw big tick
              g2D.draw(new Line2D.Float(xMin, y, xMin +  mainTickSize, y));
              // Draw unit text with a vertical orientation
              g2D.translate(xMin + mainTickSize, y);
              g2D.scale(1 / rulerScale, 1 / rulerScale);
              g2D.rotate(Math.PI / 2);
              g2D.drawString(yText, 3, fontAscent - 1);
            }
            g2D.setTransform(previousTransform);
          } else {
            // Draw small tick
            if (leftToRightOriented) {
              g2D.draw(new Line2D.Float(xMax - tickSize, y, xMax, y));
            } else {
              g2D.draw(new Line2D.Float(xMin, y, xMin + tickSize, y));
            }
          }
        }
      }

      if (mainGridSize != gridSize) {
        g2D.setStroke(new BasicStroke(1.5f / rulerScale,
            BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        if (this.orientation == SwingConstants.HORIZONTAL) {
          // Draw main vertical lines
          for (float x = (int) (xMin / mainGridSize) * mainGridSize; x < xMax; x += mainGridSize) {
            g2D.draw(new Line2D.Float(x, yMax - mainTickSize, x, yMax));
          }
        } else {
          // Draw positive main horizontal lines
          for (float y = (int) (yMin / mainGridSize) * mainGridSize; y < yMax; y += mainGridSize) {
            if (leftToRightOriented) {
              g2D.draw(new Line2D.Float(xMax - mainTickSize, y, xMax, y));
            } else {
              g2D.draw(new Line2D.Float(xMin, y, xMax + mainTickSize, y));
            }
          }
        }
      }

      if (this.mouseLocation != null) {
        g2D.setColor(getSelectioncolor());
        g2D.setStroke(new BasicStroke(1 / rulerScale));
        if (this.orientation == SwingConstants.HORIZONTAL) {
          // Draw mouse feeback vertical line
          float x = convertXPixelToModel(this.mouseLocation.x);
          g2D.draw(new Line2D.Float(x, yMax - mainTickSize, x, yMax));
        } else {
          // Draw mouse feeback horizontal line
          float y = convertYPixelToModel(this.mouseLocation.y);
          if (leftToRightOriented) {
            g2D.draw(new Line2D.Float(xMax - mainTickSize, y, xMax, y));
          } else {
            g2D.draw(new Line2D.Float(xMin, y, xMin + mainTickSize, y));
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
        text = format.format(UserPreferences.Unit.centimeterToFoot(value)) + "'"; 
      }
      return text;
    }
  }
}
