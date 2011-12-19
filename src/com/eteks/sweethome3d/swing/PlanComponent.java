/*
 * PlanComponent.java 2 juin 2006
 *
 * Sweet Home 3D, Copyright (c) 2006 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import java.awt.AWTKeyStroke;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.Toolkit;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.MemoryImageSource;
import java.awt.image.RGBImageFilter;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.security.AccessControlException;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Group;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.Light;
import javax.media.j3d.Link;
import javax.media.j3d.Node;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Texture;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolTip;
import javax.swing.JViewport;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.InternationalFormatter;
import javax.swing.text.JTextComponent;
import javax.swing.text.NumberFormatter;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import org.freehep.graphicsio.ImageConstants;
import org.freehep.graphicsio.svg.SVGGraphics2D;
import org.freehep.util.UserProperties;

import com.eteks.sweethome3d.j3d.Component3DManager;
import com.eteks.sweethome3d.j3d.HomePieceOfFurniture3D;
import com.eteks.sweethome3d.j3d.ModelManager;
import com.eteks.sweethome3d.j3d.TextureManager;
import com.eteks.sweethome3d.model.BackgroundImage;
import com.eteks.sweethome3d.model.Camera;
import com.eteks.sweethome3d.model.CollectionEvent;
import com.eteks.sweethome3d.model.CollectionListener;
import com.eteks.sweethome3d.model.Compass;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.DimensionLine;
import com.eteks.sweethome3d.model.Elevatable;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeDoorOrWindow;
import com.eteks.sweethome3d.model.HomeFurnitureGroup;
import com.eteks.sweethome3d.model.HomeLight;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.Label;
import com.eteks.sweethome3d.model.LengthUnit;
import com.eteks.sweethome3d.model.Level;
import com.eteks.sweethome3d.model.ObserverCamera;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.Sash;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.SelectionEvent;
import com.eteks.sweethome3d.model.SelectionListener;
import com.eteks.sweethome3d.model.TextStyle;
import com.eteks.sweethome3d.model.TextureImage;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.PlanController;
import com.eteks.sweethome3d.viewcontroller.PlanView;
import com.eteks.sweethome3d.viewcontroller.View;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.Viewer;
import com.sun.j3d.utils.universe.ViewingPlatform;

/**
 * A component displaying the plan of a home.
 * @author Emmanuel Puybaret
 */
public class PlanComponent extends JComponent implements PlanView, Scrollable, Printable {
  /**
   * The circumstances under which the home items displayed by this component will be painted. 
   */
  protected enum PaintMode {PAINT, PRINT, CLIPBOARD, EXPORT}
  
  private enum ActionType {DELETE_SELECTION, ESCAPE, 
      MOVE_SELECTION_LEFT, MOVE_SELECTION_UP, MOVE_SELECTION_DOWN, MOVE_SELECTION_RIGHT,
      MOVE_SELECTION_FAST_LEFT, MOVE_SELECTION_FAST_UP, MOVE_SELECTION_FAST_DOWN, MOVE_SELECTION_FAST_RIGHT,
      TOGGLE_MAGNETISM_ON, TOGGLE_MAGNETISM_OFF, 
      ACTIVATE_DUPLICATION, DEACTIVATE_DUPLICATION, 
      ACTIVATE_EDITIION, DEACTIVATE_EDITIION}
  
  private static final float    MARGIN = 40;

  private final Home            home;
  private final UserPreferences preferences;
  private float                 scale  = 0.5f;
  private boolean               selectedItemsOutlinePainted = true;
  private boolean               backgroundPainted = true;

  private PlanRulerComponent    horizontalRuler;
  private PlanRulerComponent    verticalRuler;
  
  private final Cursor          rotationCursor;
  private final Cursor          elevationCursor;
  private final Cursor          heightCursor;
  private final Cursor          powerCursor;
  private final Cursor          resizeCursor;
  private final Cursor          panningCursor;
  private final Cursor          duplicationCursor;
  
  private Rectangle2D           rectangleFeedback;
  private Class<? extends Selectable> alignedObjectClass;
  private Selectable            alignedObjectFeedback;
  private Point2D               locationFeeback;
  private boolean               showPointFeedback;
  private Point2D               centerAngleFeedback;
  private Point2D               point1AngleFeedback;
  private Point2D               point2AngleFeedback;
  private List<Selectable>      draggedItemsFeedback;
  private List<DimensionLine>   dimensionLinesFeedback;
  private boolean               selectionScrollUpdated;
  private JToolTip              toolTip;
  private JWindow               toolTipWindow;
  private boolean               resizeIndicatorVisible;
  
  private Map<PlanController.EditableProperty, JFormattedTextField> toolTipEditableTextFields;
  private KeyListener                       toolTipKeyListener;
  
  private List<HomePieceOfFurniture>        sortedLevelFurniture;
  private List<Room>                        sortedLevelRooms;
  private Map<TextStyle, Font>              fonts;
  private Map<TextStyle, FontMetrics>       fontsMetrics;
  
  private Rectangle2D                       planBoundsCache;  
  private boolean                           planBoundsCacheValid = false;  
  private BufferedImage                     backgroundImageCache;
  private Map<TextureImage, BufferedImage>  patternImagesCache;
  private List<Wall>                        otherLevelWallsCache;
  private Area                              otherLevelWallAreaCache;
  private List<Room>                        otherLevelRoomsCache;
  private Area                              otherLevelRoomAreaCache;
  private Color                             wallsPatternBackgroundCache;
  private Color                             wallsPatternForegroundCache;
  private Map<Collection<Wall>, Area>       wallAreasCache;
  private Map<Content, BufferedImage>       floorTextureImagesCache;
  private Map<HomePieceOfFurniture, PieceOfFurnitureTopViewIcon> furnitureTopViewIconsCache;

  private static final Shape       POINT_INDICATOR;
  private static final GeneralPath FURNITURE_ROTATION_INDICATOR;
  private static final GeneralPath FURNITURE_RESIZE_INDICATOR;
  private static final GeneralPath FURNITURE_ELEVATION_INDICATOR;
  private static final Shape       FURNITURE_ELEVATION_POINT_INDICATOR;
  private static final GeneralPath FURNITURE_HEIGHT_INDICATOR;
  private static final Shape       FURNITURE_HEIGHT_POINT_INDICATOR;
  private static final GeneralPath LIGHT_POWER_INDICATOR;
  private static final Shape       LIGHT_POWER_POINT_INDICATOR;
  private static final GeneralPath WALL_ORIENTATION_INDICATOR;
  private static final Shape       WALL_POINT;
  private static final GeneralPath WALL_AND_LINE_RESIZE_INDICATOR;
  private static final Shape       CAMERA_YAW_ROTATION_INDICATOR;
  private static final GeneralPath CAMERA_PITCH_ROTATION_INDICATOR;
  private static final GeneralPath CAMERA_ELEVATION_INDICATOR;
  private static final Shape       CAMERA_BODY;
  private static final Shape       CAMERA_HEAD;  
  private static final GeneralPath DIMENSION_LINE_END;  
  private static final GeneralPath TEXT_LOCATION_INDICATOR;
  private static final Shape       COMPASS_DISC;
  private static final GeneralPath COMPASS;
  private static final GeneralPath COMPASS_ROTATION_INDICATOR;
  private static final GeneralPath COMPASS_RESIZE_INDICATOR;
  
  private static final Stroke      INDICATOR_STROKE = new BasicStroke(1.5f);
  private static final Stroke      POINT_STROKE = new BasicStroke(2f);
  
  private static final float       WALL_STROKE_WIDTH = 1.5f;
  private static final float       BORDER_STROKE_WIDTH = 1f;
  
  private static final BufferedImage ERROR_TEXTURE_IMAGE;
  private static final BufferedImage WAIT_TEXTURE_IMAGE;

  static {
    POINT_INDICATOR = new Ellipse2D.Float(-1.5f, -1.5f, 3, 3);
    
    // Create a path that draws an round arrow used as a rotation indicator 
    // at top left point of a piece of furniture
    FURNITURE_ROTATION_INDICATOR = new GeneralPath();
    FURNITURE_ROTATION_INDICATOR.append(POINT_INDICATOR, false);
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
    
    LIGHT_POWER_POINT_INDICATOR = new Rectangle2D.Float(-1.5f, -1.5f, 3f, 3f);
    
    // Create a path that draws a stripped triangle as a power indicator
    // at bottom left of a not deformable lights
    LIGHT_POWER_INDICATOR = new GeneralPath();
    LIGHT_POWER_INDICATOR.moveTo(-8, 0); 
    LIGHT_POWER_INDICATOR.lineTo(-6f, 0);
    LIGHT_POWER_INDICATOR.lineTo(-6f, -1);    
    LIGHT_POWER_INDICATOR.closePath();    
    LIGHT_POWER_INDICATOR.moveTo(-3, 0); 
    LIGHT_POWER_INDICATOR.lineTo(-1f, 0);
    LIGHT_POWER_INDICATOR.lineTo(-1f, -2.5f);    
    LIGHT_POWER_INDICATOR.lineTo(-3f, -1.8f);    
    LIGHT_POWER_INDICATOR.closePath();    
    LIGHT_POWER_INDICATOR.moveTo(2, 0); 
    LIGHT_POWER_INDICATOR.lineTo(4, 0);
    LIGHT_POWER_INDICATOR.lineTo(4f, -3.5f);    
    LIGHT_POWER_INDICATOR.lineTo(2f, -2.8f);    
    LIGHT_POWER_INDICATOR.closePath();    

    // Create a path used as a resize indicator 
    // at bottom right point of a piece of furniture
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
    CAMERA_PITCH_ROTATION_INDICATOR.append(POINT_INDICATOR, false);
    CAMERA_PITCH_ROTATION_INDICATOR.moveTo(4.5f, 0);
    CAMERA_PITCH_ROTATION_INDICATOR.lineTo(5.2f, 0);
    CAMERA_PITCH_ROTATION_INDICATOR.moveTo(9f, 0);
    CAMERA_PITCH_ROTATION_INDICATOR.lineTo(10, 0);
    CAMERA_PITCH_ROTATION_INDICATOR.append(new Arc2D.Float(7, -8, 5, 16, 20, 320, Arc2D.OPEN), false);
    CAMERA_PITCH_ROTATION_INDICATOR.moveTo(10f, 4.5f);
    CAMERA_PITCH_ROTATION_INDICATOR.lineTo(12.3f, 2f);
    CAMERA_PITCH_ROTATION_INDICATOR.lineTo(12.8f, 5.8f);
    
    // Create a path that draws a line with one arrow as an elevation indicator
    // at the back of the camera
    CAMERA_ELEVATION_INDICATOR = new GeneralPath();
    CAMERA_ELEVATION_INDICATOR.moveTo(0, -4); // Vertical line
    CAMERA_ELEVATION_INDICATOR.lineTo(0, 4);
    CAMERA_ELEVATION_INDICATOR.moveTo(-2.5f, 4);    // Bottom line
    CAMERA_ELEVATION_INDICATOR.lineTo(2.5f, 4);
    CAMERA_ELEVATION_INDICATOR.moveTo(-1.2f, 0.5f); // Bottom arrow
    CAMERA_ELEVATION_INDICATOR.lineTo(0, 3.5f);
    CAMERA_ELEVATION_INDICATOR.lineTo(1.2f, 0.5f);

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
    
    // Create a path that draws three arrows going left, right and down
    TEXT_LOCATION_INDICATOR = new GeneralPath();
    TEXT_LOCATION_INDICATOR.append(new Arc2D.Float(-2, 0, 4, 4, 190, 160, Arc2D.CHORD), false);
    TEXT_LOCATION_INDICATOR.moveTo(0, 4);        // Down line
    TEXT_LOCATION_INDICATOR.lineTo(0, 12);
    TEXT_LOCATION_INDICATOR.moveTo(-1.2f, 8.5f); // Down arrow
    TEXT_LOCATION_INDICATOR.lineTo(0f, 11.5f);
    TEXT_LOCATION_INDICATOR.lineTo(1.2f, 8.5f);
    TEXT_LOCATION_INDICATOR.moveTo(2f, 3f);      // Right line
    TEXT_LOCATION_INDICATOR.lineTo(9, 6);
    TEXT_LOCATION_INDICATOR.moveTo(6, 6.5f);     // Right arrow
    TEXT_LOCATION_INDICATOR.lineTo(10, 7);
    TEXT_LOCATION_INDICATOR.lineTo(7.5f, 3.5f);
    TEXT_LOCATION_INDICATOR.moveTo(-2f, 3f);     // Left line
    TEXT_LOCATION_INDICATOR.lineTo(-9, 6);
    TEXT_LOCATION_INDICATOR.moveTo(-6, 6.5f);    // Left arrow
    TEXT_LOCATION_INDICATOR.lineTo(-10, 7);
    TEXT_LOCATION_INDICATOR.lineTo(-7.5f, 3.5f);
    
    // Create the path used to draw the compass
    COMPASS_DISC = new Ellipse2D.Float(-0.5f, -0.5f, 1, 1);
    BasicStroke stroke = new BasicStroke(0.01f);
    COMPASS = new GeneralPath(stroke.createStrokedShape(COMPASS_DISC));
    COMPASS.append(stroke.createStrokedShape(new Line2D.Float(-0.6f, 0, -0.5f, 0)), false);
    COMPASS.append(stroke.createStrokedShape(new Line2D.Float(0.6f, 0, 0.5f, 0)), false);
    COMPASS.append(stroke.createStrokedShape(new Line2D.Float(0, 0.6f, 0, 0.5f)), false);
    stroke = new BasicStroke(0.04f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    COMPASS.append(stroke.createStrokedShape(new Line2D.Float(0, 0, 0, 0)), false);
    GeneralPath compassNeedle = new GeneralPath();
    compassNeedle.moveTo(0, -0.47f);
    compassNeedle.lineTo(0.15f, 0.46f);
    compassNeedle.lineTo(0, 0.32f);
    compassNeedle.lineTo(-0.15f, 0.46f);
    compassNeedle.closePath();
    stroke = new BasicStroke(0.03f);
    COMPASS.append(stroke.createStrokedShape(compassNeedle), false);
    GeneralPath compassNorthDirection = new GeneralPath();
    compassNorthDirection.moveTo(-0.07f, -0.55f); // Draws the N letter
    compassNorthDirection.lineTo(-0.07f, -0.69f);
    compassNorthDirection.lineTo(0.07f, -0.56f);
    compassNorthDirection.lineTo(0.07f, -0.7f);
    COMPASS.append(stroke.createStrokedShape(compassNorthDirection), false);

    // Create a path used as rotation indicator for the compass
    COMPASS_ROTATION_INDICATOR = new GeneralPath();
    COMPASS_ROTATION_INDICATOR.append(POINT_INDICATOR, false);
    COMPASS_ROTATION_INDICATOR.append(new Arc2D.Float(-8, -7, 16, 16, 210, 120, Arc2D.OPEN), false);
    COMPASS_ROTATION_INDICATOR.moveTo(4f, 5.66f);
    COMPASS_ROTATION_INDICATOR.lineTo(7f, 5.66f);
    COMPASS_ROTATION_INDICATOR.lineTo(5.6f, 8.3f);
    
    // Create a path used as a resize indicator for the compass 
    COMPASS_RESIZE_INDICATOR = new GeneralPath();
    COMPASS_RESIZE_INDICATOR.append(new Rectangle2D.Float(-1.5f, -1.5f, 3f, 3f), false);
    COMPASS_RESIZE_INDICATOR.moveTo(4, -6);
    COMPASS_RESIZE_INDICATOR.lineTo(6, -6);
    COMPASS_RESIZE_INDICATOR.lineTo(6, 6);
    COMPASS_RESIZE_INDICATOR.lineTo(4, 6);
    COMPASS_RESIZE_INDICATOR.moveTo(5, 0);
    COMPASS_RESIZE_INDICATOR.lineTo(9, 0);
    COMPASS_RESIZE_INDICATOR.moveTo(9, -1.5f);
    COMPASS_RESIZE_INDICATOR.lineTo(12, 0);
    COMPASS_RESIZE_INDICATOR.lineTo(9, 1.5f);
    
    ERROR_TEXTURE_IMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
    Graphics g = ERROR_TEXTURE_IMAGE.getGraphics();
    g.setColor(Color.RED);
    g.drawLine(0, 0, 0, 0);
    g.dispose();

    WAIT_TEXTURE_IMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
    g = WAIT_TEXTURE_IMAGE.getGraphics();
    g.setColor(Color.WHITE);
    g.drawLine(0, 0, 0, 0);
    g.dispose();
  }

  /**
   * Creates a new plan that displays <code>home</code>.
   */
  public PlanComponent(Home home, 
                       UserPreferences preferences,
                       PlanController controller) {
    this.home = home;
    this.preferences = preferences;
    // Set JComponent default properties
    setOpaque(true);
    // Add listeners
    addModelListeners(home, preferences, controller);
    createToolTipTextFields(preferences, controller);
    if (controller != null) {
      addMouseListeners(controller);
      addFocusListener(controller);
      createActions(controller);      
      installDefaultKeyboardActions();
      setFocusable(true);
      setAutoscrolls(true);
    }
    this.rotationCursor = createCustomCursor("resources/cursors/rotation16x16.png",
        "resources/cursors/rotation32x32.png", "Rotation cursor", Cursor.MOVE_CURSOR);
    this.elevationCursor = createCustomCursor("resources/cursors/elevation16x16.png",
        "resources/cursors/elevation32x32.png", "Elevation cursor", Cursor.MOVE_CURSOR);
    this.heightCursor = createCustomCursor("resources/cursors/height16x16.png",
        "resources/cursors/height32x32.png", "Height cursor", Cursor.MOVE_CURSOR);
    this.powerCursor = createCustomCursor("resources/cursors/power16x16.png",
        "resources/cursors/power32x32.png", "Power cursor", Cursor.MOVE_CURSOR);
    this.resizeCursor = createCustomCursor("resources/cursors/resize16x16.png",
        "resources/cursors/resize32x32.png", "Resize cursor", Cursor.MOVE_CURSOR);
    this.panningCursor = createCustomCursor("resources/cursors/panning16x16.png",
        "resources/cursors/panning32x32.png", "Panning cursor", Cursor.HAND_CURSOR);
    this.duplicationCursor = DragSource.DefaultCopyDrop;
    this.patternImagesCache = new HashMap<TextureImage, BufferedImage>();
    // Install default colors
    super.setForeground(UIManager.getColor("textText"));
    super.setBackground(UIManager.getColor("window"));
  }

  /**
   * Adds home items and selection listeners on this component to receive  
   * changes notifications from home. 
   */
  private void addModelListeners(Home home, final UserPreferences preferences, 
                                 final PlanController controller) {
    // Add listener to update plan when furniture changes
    final PropertyChangeListener furnitureChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          if (furnitureTopViewIconsCache != null
              && (HomePieceOfFurniture.Property.COLOR.name().equals(ev.getPropertyName())
                  || HomePieceOfFurniture.Property.TEXTURE.name().equals(ev.getPropertyName())
                  || HomePieceOfFurniture.Property.SHININESS.name().equals(ev.getPropertyName())
                  || (HomePieceOfFurniture.Property.WIDTH.name().equals(ev.getPropertyName())
                      || HomePieceOfFurniture.Property.DEPTH.name().equals(ev.getPropertyName())
                      || HomePieceOfFurniture.Property.HEIGHT.name().equals(ev.getPropertyName()))
                     && ((HomePieceOfFurniture)ev.getSource()).getTexture() != null)) {
            for (HomePieceOfFurniture piece : getFurnitureWithoutGroups((HomePieceOfFurniture)ev.getSource())) {
              furnitureTopViewIconsCache.remove(piece);
            }
            repaint();
          } else if (HomePieceOfFurniture.Property.ELEVATION.name().equals(ev.getPropertyName())
                     || HomePieceOfFurniture.Property.LEVEL.name().equals(ev.getPropertyName())
                     || HomePieceOfFurniture.Property.HEIGHT.name().equals(ev.getPropertyName())) {
            sortedLevelFurniture = null;
            repaint();
          } else {
            revalidate();
          }
        }
      };
    for (HomePieceOfFurniture piece : home.getFurniture()) {
      piece.addPropertyChangeListener(furnitureChangeListener);
    }
    home.addFurnitureListener(new CollectionListener<HomePieceOfFurniture>() {
        public void collectionChanged(CollectionEvent<HomePieceOfFurniture> ev) {
          if (ev.getType() == CollectionEvent.Type.ADD) {
            ev.getItem().addPropertyChangeListener(furnitureChangeListener);
          } else if (ev.getType() == CollectionEvent.Type.DELETE) {
            ev.getItem().removePropertyChangeListener(furnitureChangeListener);
          }
          sortedLevelFurniture = null;
          revalidate();
        }
      });
    
    // Add listener to update plan when walls change
    final PropertyChangeListener wallChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          String propertyName = ev.getPropertyName();
          if (Wall.Property.X_START.name().equals(propertyName)
              || Wall.Property.X_END.name().equals(propertyName) 
              || Wall.Property.Y_START.name().equals(propertyName) 
              || Wall.Property.Y_END.name().equals(propertyName)
              || Wall.Property.WALL_AT_START.name().equals(propertyName)
              || Wall.Property.WALL_AT_END.name().equals(propertyName)
              || Wall.Property.THICKNESS.name().equals(propertyName)
              || Wall.Property.ARC_EXTENT.name().equals(propertyName)
              || Wall.Property.PATTERN.name().equals(propertyName)) {
            wallAreasCache = null;
            revalidate();
          } else if (Wall.Property.LEVEL.name().equals(propertyName)
              || Wall.Property.HEIGHT.name().equals(propertyName)
              || Wall.Property.HEIGHT_AT_END.name().equals(propertyName)) {
            otherLevelWallAreaCache = null;
            otherLevelWallsCache = null;
            wallAreasCache = null;
            repaint();
          }
        }
      };
    for (Wall wall : home.getWalls()) {
      wall.addPropertyChangeListener(wallChangeListener);
    }
    home.addWallsListener(new CollectionListener<Wall> () {
        public void collectionChanged(CollectionEvent<Wall> ev) {
          if (ev.getType() == CollectionEvent.Type.ADD) {
            ev.getItem().addPropertyChangeListener(wallChangeListener);
          } else if (ev.getType() == CollectionEvent.Type.DELETE) {
            ev.getItem().removePropertyChangeListener(wallChangeListener);
          }
          otherLevelWallAreaCache = null;
          otherLevelWallsCache = null;
          wallAreasCache = null;
          revalidate();
        }
      });
    
    // Add listener to update plan when rooms change
    final PropertyChangeListener roomChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          String propertyName = ev.getPropertyName();
          if (Room.Property.POINTS.name().equals(propertyName)
              || Room.Property.NAME.name().equals(propertyName)
              || Room.Property.NAME_X_OFFSET.name().equals(propertyName)
              || Room.Property.NAME_Y_OFFSET.name().equals(propertyName)
              || Room.Property.NAME_STYLE.name().equals(propertyName)
              || Room.Property.AREA_VISIBLE.name().equals(propertyName)
              || Room.Property.AREA_X_OFFSET.name().equals(propertyName)
              || Room.Property.AREA_Y_OFFSET.name().equals(propertyName)
              || Room.Property.AREA_STYLE.name().equals(propertyName)) {
            sortedLevelRooms = null;
            otherLevelRoomsCache = null;
            otherLevelRoomAreaCache = null;
            revalidate();
          } else if (preferences.isRoomFloorColoredOrTextured()
                     && (Room.Property.FLOOR_COLOR.name().equals(propertyName)
                         || Room.Property.FLOOR_TEXTURE.name().equals(propertyName)
                         || Room.Property.FLOOR_VISIBLE.name().equals(propertyName))) {
            repaint(); 
          }
        }
      };
    for (Room room : home.getRooms()) {
      room.addPropertyChangeListener(roomChangeListener);
    }
    home.addRoomsListener(new CollectionListener<Room> () {
        public void collectionChanged(CollectionEvent<Room> ev) {
          if (ev.getType() == CollectionEvent.Type.ADD) {
            ev.getItem().addPropertyChangeListener(roomChangeListener);
          } else if (ev.getType() == CollectionEvent.Type.DELETE) {
            ev.getItem().removePropertyChangeListener(roomChangeListener);
          }
          sortedLevelRooms = null;
          otherLevelRoomsCache = null;
          otherLevelRoomAreaCache = null;
          revalidate();
        }
      });

    // Add listener to update plan when dimension lines change
    final PropertyChangeListener dimensionLineChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          revalidate();
        }
      };
    for (DimensionLine dimensionLine : home.getDimensionLines()) {
      dimensionLine.addPropertyChangeListener(dimensionLineChangeListener);
    }
    home.addDimensionLinesListener(new CollectionListener<DimensionLine> () {
        public void collectionChanged(CollectionEvent<DimensionLine> ev) {
          if (ev.getType() == CollectionEvent.Type.ADD) {
            ev.getItem().addPropertyChangeListener(dimensionLineChangeListener);
          } else if (ev.getType() == CollectionEvent.Type.DELETE) {
            ev.getItem().removePropertyChangeListener(dimensionLineChangeListener);
          }
          revalidate();
        }
      });

    // Add listener to update plan when labels change
    final PropertyChangeListener labelChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          revalidate();
        }
      };
    for (Label label : home.getLabels()) {
      label.addPropertyChangeListener(labelChangeListener);
    }
    home.addLabelsListener(new CollectionListener<Label> () {
        public void collectionChanged(CollectionEvent<Label> ev) {
          if (ev.getType() == CollectionEvent.Type.ADD) {
            ev.getItem().addPropertyChangeListener(labelChangeListener);
          } else if (ev.getType() == CollectionEvent.Type.DELETE) {
            ev.getItem().removePropertyChangeListener(labelChangeListener);
          }
          revalidate();
        }
      });

    // Add listener to update plan when levels change
    final PropertyChangeListener levelChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          String propertyName = ev.getPropertyName();
          if (Level.Property.BACKGROUND_IMAGE.name().equals(propertyName)) {
            backgroundImageCache = null;
            revalidate();
          } else if (Level.Property.ELEVATION.name().equals(propertyName)) {
            otherLevelWallAreaCache = null;
            otherLevelWallsCache = null;
            otherLevelRoomsCache = null;
            otherLevelRoomAreaCache = null;
            wallAreasCache = null;
            sortedLevelFurniture = null;
            repaint();
          }
        }
      };
    for (Level level : home.getLevels()) {
      level.addPropertyChangeListener(levelChangeListener);
    }
    home.addLevelsListener(new CollectionListener<Level> () {
        public void collectionChanged(CollectionEvent<Level> ev) {
          Level level = ev.getItem();
          if (ev.getType() == CollectionEvent.Type.ADD) {
            level.addPropertyChangeListener(levelChangeListener);
          } else if (ev.getType() == CollectionEvent.Type.DELETE) {
            level.removePropertyChangeListener(levelChangeListener);
          }
          revalidate();
        }
      });

    home.addPropertyChangeListener(Home.Property.CAMERA, new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          revalidate();
        }
      });
    home.getObserverCamera().addPropertyChangeListener(new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          String propertyName = ev.getPropertyName();
          if (Camera.Property.X.name().equals(propertyName)
              || Camera.Property.Y.name().equals(propertyName) 
              || Camera.Property.FIELD_OF_VIEW.name().equals(propertyName)
              || Camera.Property.YAW.name().equals(propertyName)
              || ObserverCamera.Property.WIDTH.name().equals(propertyName)
              || ObserverCamera.Property.DEPTH.name().equals(propertyName) 
              || ObserverCamera.Property.HEIGHT.name().equals(propertyName)) {
            revalidate();
          }
        }
      });
    home.getCompass().addPropertyChangeListener(new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          String propertyName = ev.getPropertyName();
          if (Compass.Property.X.name().equals(propertyName)
              || Compass.Property.Y.name().equals(propertyName) 
              || Compass.Property.NORTH_DIRECTION.name().equals(propertyName)
              || Compass.Property.DIAMETER.name().equals(propertyName)
              || Compass.Property.VISIBLE.name().equals(propertyName)) {
            revalidate();
          }
        }
      });
    home.addSelectionListener(new SelectionListener () {
        public void selectionChanged(SelectionEvent ev) {
          repaint();
        }
      });
    home.addPropertyChangeListener(Home.Property.BACKGROUND_IMAGE, 
      new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          backgroundImageCache = null;
          repaint();
        }
      });
    home.addPropertyChangeListener(Home.Property.SELECTED_LEVEL, new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          backgroundImageCache = null;
          otherLevelWallAreaCache = null;
          otherLevelWallsCache = null;
          otherLevelRoomsCache = null;
          otherLevelRoomAreaCache = null;
          wallAreasCache = null;
          sortedLevelRooms = null;
          sortedLevelFurniture = null;
          repaint();
        }
      });
    preferences.addPropertyChangeListener(UserPreferences.Property.UNIT, 
        new UserPreferencesChangeListener(this));
    preferences.addPropertyChangeListener(UserPreferences.Property.LANGUAGE, 
        new UserPreferencesChangeListener(this));
    preferences.addPropertyChangeListener(UserPreferences.Property.GRID_VISIBLE, 
        new UserPreferencesChangeListener(this));
    preferences.addPropertyChangeListener(UserPreferences.Property.FURNITURE_VIEWED_FROM_TOP, 
        new UserPreferencesChangeListener(this));
    preferences.addPropertyChangeListener(UserPreferences.Property.ROOM_FLOOR_COLORED_OR_TEXTURED, 
        new UserPreferencesChangeListener(this));
    preferences.addPropertyChangeListener(UserPreferences.Property.WALL_PATTERN, 
        new UserPreferencesChangeListener(this));
  }

  /**
   * Returns all the pieces depending on the given <code>piece</code> that are not groups.  
   */
  private List<HomePieceOfFurniture> getFurnitureWithoutGroups(HomePieceOfFurniture piece) {
    if (piece instanceof HomeFurnitureGroup) {
      List<HomePieceOfFurniture> pieces = new ArrayList<HomePieceOfFurniture>();
      for (HomePieceOfFurniture groupPiece : ((HomeFurnitureGroup)piece).getFurniture()) {
        pieces.addAll(getFurnitureWithoutGroups(groupPiece));
      }
      return pieces;
    } else {
      return Arrays.asList(new HomePieceOfFurniture [] {piece});
    }
  }

  /**
   * Preferences property listener bound to this component with a weak reference to avoid
   * strong link between preferences and this component.  
   */
  private static class UserPreferencesChangeListener implements PropertyChangeListener {
    private WeakReference<PlanComponent>  planComponent;

    public UserPreferencesChangeListener(PlanComponent planComponent) {
      this.planComponent = new WeakReference<PlanComponent>(planComponent);
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
      // If plan component was garbage collected, remove this listener from preferences
      PlanComponent planComponent = this.planComponent.get();
      UserPreferences preferences = (UserPreferences)ev.getSource();
      UserPreferences.Property property = UserPreferences.Property.valueOf(ev.getPropertyName());
      if (planComponent == null) {
        preferences.removePropertyChangeListener(property, this);
      } else {
        switch (property) {
          case LANGUAGE :
          case UNIT :
            // Update format of tool tip text fields
            for (Map.Entry<PlanController.EditableProperty, JFormattedTextField> toolTipTextFieldEntry : 
              planComponent.toolTipEditableTextFields.entrySet()) {
              updateToolTipTextFieldFormatterFactory(toolTipTextFieldEntry.getValue(), 
                  toolTipTextFieldEntry.getKey(), preferences);
            }
            if (planComponent.horizontalRuler != null) {
              planComponent.horizontalRuler.repaint();
            }
            if (planComponent.verticalRuler != null) {
              planComponent.verticalRuler.repaint();
            }
            break;
          case WALL_PATTERN :
            planComponent.wallAreasCache = null;
            break;
          case FURNITURE_VIEWED_FROM_TOP :
            if (planComponent.furnitureTopViewIconsCache != null
                && !preferences.isFurnitureViewedFromTop()) {
              planComponent.furnitureTopViewIconsCache = null;
            }
            break;
        }
        planComponent.repaint();
      }
    }
  }

  /**
   * Revalidates and repaints this component and its rulers.
   */
  @Override
  public void revalidate() {
    revalidate(true);
  }
  
  /**
   * Revalidates this component after invalidating plan bounds cache if <code>invalidatePlanBoundsCache</code> is <code>true</code>
   * and updates viewport position if this component is displayed in a scrolled pane.
   */
  private void revalidate(boolean invalidatePlanBoundsCache) {
    boolean planBoundsCacheWereValid = this.planBoundsCacheValid;
    final float planBoundsMinX = (float)getPlanBounds().getMinX();
    final float planBoundsMinY = (float)getPlanBounds().getMinY();
    if (invalidatePlanBoundsCache
        && planBoundsCacheWereValid) {      
      this.planBoundsCacheValid = false;
    }
    
    // Revalidate and repaint
    super.revalidate();
    repaint();

    if (invalidatePlanBoundsCache
        && getParent() instanceof JViewport) {
      float planBoundsNewMinX = (float)getPlanBounds().getMinX();
      float planBoundsNewMinY = (float)getPlanBounds().getMinY();
      // If plan bounds upper left corner diminished
      if (planBoundsNewMinX < planBoundsMinX
          || planBoundsNewMinY < planBoundsMinY) {
        JViewport parent = (JViewport)getParent();
        final Point viewPosition = parent.getViewPosition();
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
      private Point lastMousePressedLocation;
      
      @Override
      public void mousePressed(MouseEvent ev) {
        this.lastMousePressedLocation = ev.getPoint();
        if (isEnabled() && !ev.isPopupTrigger()) {
          requestFocusInWindow();          
          if (ev.getButton() == MouseEvent.BUTTON1) {
            controller.pressMouse(convertXPixelToModel(ev.getX()), convertYPixelToModel(ev.getY()), 
                ev.getClickCount(), ev.isShiftDown(), 
                OperatingSystem.isMacOSX() ? ev.isAltDown() : ev.isControlDown());
          }
        }
      }

      @Override
      public void mouseReleased(MouseEvent ev) {
        if (isEnabled() && !ev.isPopupTrigger() && ev.getButton() == MouseEvent.BUTTON1) {
          controller.releaseMouse(convertXPixelToModel(ev.getX()), convertYPixelToModel(ev.getY()));
        }
      }

      @Override
      public void mouseMoved(MouseEvent ev) {
        // Ignore mouseMoved events that follows a mousePressed at the same location (Linux notifies this kind of events)
        if (this.lastMousePressedLocation != null
            && !this.lastMousePressedLocation.equals(ev.getPoint())) {
          this.lastMousePressedLocation = null;
        }
        if (this.lastMousePressedLocation == null) {
          if (isEnabled()) {
            controller.moveMouse(convertXPixelToModel(ev.getX()), convertYPixelToModel(ev.getY()));
          }
        }
      }

      @Override
      public void mouseDragged(MouseEvent ev) {
        if (isEnabled()) {
          mouseMoved(ev);
        }
      }
    };
    addMouseListener(mouseListener);
    addMouseMotionListener(mouseListener);
    addMouseWheelListener(new MouseWheelListener() {        
        public void mouseWheelMoved(MouseWheelEvent ev) {
          if (ev.getModifiers() == getToolkit().getMenuShortcutKeyMask()) {
            controller.zoom((float)(ev.getWheelRotation() < 0 
                ? Math.pow(1.05, -ev.getWheelRotation()) 
                : Math.pow(0.95, ev.getWheelRotation())));
          } else if (getMouseWheelListeners().length == 1) {
            // If this listener is the only one registered on this component
            // redispatch event to its parent (for default scroll bar management)
            getParent().dispatchEvent(
              new MouseWheelEvent(getParent(), ev.getID(), ev.getWhen(), 
                  ev.getModifiersEx() | ev.getModifiers(), 
                  ev.getX() - getX(), ev.getY() - getY(), 
                  ev.getClickCount(), ev.isPopupTrigger(), ev.getScrollType(), 
                  ev.getScrollAmount(), ev.getWheelRotation()));
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
   * Installs default keys bound to actions. 
   */
  private void installDefaultKeyboardActions() {
    InputMap inputMap = getInputMap(WHEN_FOCUSED);
    inputMap.clear();
    inputMap.put(KeyStroke.getKeyStroke("DELETE"), ActionType.DELETE_SELECTION);
    inputMap.put(KeyStroke.getKeyStroke("BACK_SPACE"), ActionType.DELETE_SELECTION);
    inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), ActionType.ESCAPE);
    inputMap.put(KeyStroke.getKeyStroke("LEFT"), ActionType.MOVE_SELECTION_LEFT);
    inputMap.put(KeyStroke.getKeyStroke("shift LEFT"), ActionType.MOVE_SELECTION_FAST_LEFT);
    inputMap.put(KeyStroke.getKeyStroke("UP"), ActionType.MOVE_SELECTION_UP);
    inputMap.put(KeyStroke.getKeyStroke("shift UP"), ActionType.MOVE_SELECTION_FAST_UP);
    inputMap.put(KeyStroke.getKeyStroke("DOWN"), ActionType.MOVE_SELECTION_DOWN);
    inputMap.put(KeyStroke.getKeyStroke("shift DOWN"), ActionType.MOVE_SELECTION_FAST_DOWN);
    inputMap.put(KeyStroke.getKeyStroke("RIGHT"), ActionType.MOVE_SELECTION_RIGHT);
    inputMap.put(KeyStroke.getKeyStroke("shift RIGHT"), ActionType.MOVE_SELECTION_FAST_RIGHT);
    inputMap.put(KeyStroke.getKeyStroke("shift pressed SHIFT"), ActionType.TOGGLE_MAGNETISM_ON);
    inputMap.put(KeyStroke.getKeyStroke("released SHIFT"), ActionType.TOGGLE_MAGNETISM_OFF);
    inputMap.put(KeyStroke.getKeyStroke("shift ESCAPE"), ActionType.ESCAPE);
    inputMap.put(KeyStroke.getKeyStroke("ENTER"), ActionType.ACTIVATE_EDITIION);
    inputMap.put(KeyStroke.getKeyStroke("shift ENTER"), ActionType.ACTIVATE_EDITIION);
    if (OperatingSystem.isMacOSX()) {
      // Under Mac OS X, duplication with Alt key 
      inputMap.put(KeyStroke.getKeyStroke("alt pressed ALT"), ActionType.ACTIVATE_DUPLICATION);
      inputMap.put(KeyStroke.getKeyStroke("released ALT"), ActionType.DEACTIVATE_DUPLICATION);
      inputMap.put(KeyStroke.getKeyStroke("shift alt pressed ALT"), ActionType.ACTIVATE_DUPLICATION);
      inputMap.put(KeyStroke.getKeyStroke("shift released ALT"), ActionType.DEACTIVATE_DUPLICATION);
      inputMap.put(KeyStroke.getKeyStroke("alt ESCAPE"), ActionType.ESCAPE);
      inputMap.put(KeyStroke.getKeyStroke("alt shift pressed SHIFT"), ActionType.TOGGLE_MAGNETISM_ON);
      inputMap.put(KeyStroke.getKeyStroke("alt released SHIFT"), ActionType.TOGGLE_MAGNETISM_OFF);
    } else {
      // Under other systems, duplication with Ctrl key 
      inputMap.put(KeyStroke.getKeyStroke("control pressed CONTROL"), ActionType.ACTIVATE_DUPLICATION);
      inputMap.put(KeyStroke.getKeyStroke("released CONTROL"), ActionType.DEACTIVATE_DUPLICATION);
      inputMap.put(KeyStroke.getKeyStroke("shift control pressed CONTROL"), ActionType.ACTIVATE_DUPLICATION);
      inputMap.put(KeyStroke.getKeyStroke("shift released CONTROL"), ActionType.DEACTIVATE_DUPLICATION);
      inputMap.put(KeyStroke.getKeyStroke("control ESCAPE"), ActionType.ESCAPE);
      inputMap.put(KeyStroke.getKeyStroke("control shift pressed SHIFT"), ActionType.TOGGLE_MAGNETISM_ON);
      inputMap.put(KeyStroke.getKeyStroke("control released SHIFT"), ActionType.TOGGLE_MAGNETISM_OFF);
    }
  }
  
  /**
   * Installs keys bound to actions during edition. 
   */
  private void installEditionKeyboardActions() {
    InputMap inputMap = getInputMap(WHEN_FOCUSED);
    inputMap.clear();
    inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), ActionType.ESCAPE);
    inputMap.put(KeyStroke.getKeyStroke("shift ESCAPE"), ActionType.ESCAPE);
    inputMap.put(KeyStroke.getKeyStroke("ENTER"), ActionType.DEACTIVATE_EDITIION);
    inputMap.put(KeyStroke.getKeyStroke("shift ENTER"), ActionType.DEACTIVATE_EDITIION);
    if (OperatingSystem.isMacOSX()) {
      // Under Mac OS X, duplication with Alt key 
      inputMap.put(KeyStroke.getKeyStroke("alt ESCAPE"), ActionType.ESCAPE);
      inputMap.put(KeyStroke.getKeyStroke("alt ENTER"), ActionType.DEACTIVATE_EDITIION);
      inputMap.put(KeyStroke.getKeyStroke("alt shift ENTER"), ActionType.DEACTIVATE_EDITIION);
      inputMap.put(KeyStroke.getKeyStroke("alt pressed ALT"), ActionType.ACTIVATE_DUPLICATION);
      inputMap.put(KeyStroke.getKeyStroke("released ALT"), ActionType.DEACTIVATE_DUPLICATION);
      inputMap.put(KeyStroke.getKeyStroke("shift alt pressed ALT"), ActionType.ACTIVATE_DUPLICATION);
      inputMap.put(KeyStroke.getKeyStroke("shift released ALT"), ActionType.DEACTIVATE_DUPLICATION);
    } else {
      // Under other systems, duplication with Ctrl key 
      inputMap.put(KeyStroke.getKeyStroke("control ESCAPE"), ActionType.ESCAPE);
      inputMap.put(KeyStroke.getKeyStroke("control ENTER"), ActionType.DEACTIVATE_EDITIION);
      inputMap.put(KeyStroke.getKeyStroke("control shift ENTER"), ActionType.DEACTIVATE_EDITIION);
      inputMap.put(KeyStroke.getKeyStroke("control pressed CONTROL"), ActionType.ACTIVATE_DUPLICATION);
      inputMap.put(KeyStroke.getKeyStroke("released CONTROL"), ActionType.DEACTIVATE_DUPLICATION);
      inputMap.put(KeyStroke.getKeyStroke("shift control pressed CONTROL"), ActionType.ACTIVATE_DUPLICATION);
      inputMap.put(KeyStroke.getKeyStroke("shift released CONTROL"), ActionType.DEACTIVATE_DUPLICATION);
    }
  }
 
  /**
   * Creates actions that calls back <code>controller</code> methods.  
   */
  private void createActions(final PlanController controller) {
    // Delete selection action 
    Action deleteSelectionAction = new AbstractAction() {
      public void actionPerformed(ActionEvent ev) {
        controller.deleteSelection();
      }
    };
    // Escape action 
    Action escapeAction = new AbstractAction() {
      public void actionPerformed(ActionEvent ev) {
        controller.escape();
      }
    };
    // Move selection action  
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
    // Toggle magnetism action
    class ToggleMagnetismAction extends AbstractAction {
      private final boolean toggle;
      
      public ToggleMagnetismAction(boolean toggle) {
        this.toggle = toggle;
      }

      public void actionPerformed(ActionEvent ev) {
        controller.toggleMagnetism(this.toggle);
      }
    }
    // Duplication action
    class SetDuplicationActivatedAction extends AbstractAction {
      private final boolean duplicationActivated;
      
      public SetDuplicationActivatedAction(boolean duplicationActivated) {
        this.duplicationActivated = duplicationActivated;
      }

      public void actionPerformed(ActionEvent ev) {
        controller.setDuplicationActivated(this.duplicationActivated);
      }
    }
    // Edition action
    class SetEditionActivatedAction extends AbstractAction {
      private final boolean editionActivated;
      
      public SetEditionActivatedAction(boolean editionActivated) {
        this.editionActivated = editionActivated;
      }

      public void actionPerformed(ActionEvent ev) {
        controller.setEditionActivated(this.editionActivated);
      }
    }
    ActionMap actionMap = getActionMap();
    actionMap.put(ActionType.DELETE_SELECTION, deleteSelectionAction);
    actionMap.put(ActionType.ESCAPE, escapeAction);
    actionMap.put(ActionType.MOVE_SELECTION_LEFT, new MoveSelectionAction(-1, 0));
    actionMap.put(ActionType.MOVE_SELECTION_FAST_LEFT, new MoveSelectionAction(-10, 0));
    actionMap.put(ActionType.MOVE_SELECTION_UP, new MoveSelectionAction(0, -1));
    actionMap.put(ActionType.MOVE_SELECTION_FAST_UP, new MoveSelectionAction(0, -10));
    actionMap.put(ActionType.MOVE_SELECTION_DOWN, new MoveSelectionAction(0, 1));
    actionMap.put(ActionType.MOVE_SELECTION_FAST_DOWN, new MoveSelectionAction(0, 10));
    actionMap.put(ActionType.MOVE_SELECTION_RIGHT, new MoveSelectionAction(1, 0));
    actionMap.put(ActionType.MOVE_SELECTION_FAST_RIGHT, new MoveSelectionAction(10, 0));
    actionMap.put(ActionType.TOGGLE_MAGNETISM_ON, new ToggleMagnetismAction(true));
    actionMap.put(ActionType.TOGGLE_MAGNETISM_OFF, new ToggleMagnetismAction(false));
    actionMap.put(ActionType.ACTIVATE_DUPLICATION, new SetDuplicationActivatedAction(true));
    actionMap.put(ActionType.DEACTIVATE_DUPLICATION, new SetDuplicationActivatedAction(false));
    actionMap.put(ActionType.ACTIVATE_EDITIION, new SetEditionActivatedAction(true));
    actionMap.put(ActionType.DEACTIVATE_EDITIION, new SetEditionActivatedAction(false));
  }

  /**
   * Creates the text fields used in tool tip and their label.
   */
  private void createToolTipTextFields(UserPreferences preferences, 
                                       final PlanController controller) {
    this.toolTipEditableTextFields = new HashMap<PlanController.EditableProperty, JFormattedTextField>();
    Font toolTipFont = UIManager.getFont("ToolTip.font");
    for (final PlanController.EditableProperty editableProperty : PlanController.EditableProperty.values()) {
      final JFormattedTextField textField = new JFormattedTextField() {
          @Override
          public Dimension getPreferredSize() {
            // Enlarge preferred size of one pixel
            Dimension preferredSize = super.getPreferredSize();
            return new Dimension(preferredSize.width + 1, preferredSize.height);
          }
        };
      updateToolTipTextFieldFormatterFactory(textField, editableProperty, preferences);
      textField.setFont(toolTipFont);
      textField.setOpaque(false);
      textField.setBorder(null);
      if (controller != null) {
        // Add a listener to notify changes to controller
        textField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent ev) {
              try {
                textField.commitEdit();
                controller.updateEditableProperty(editableProperty, textField.getValue());
              } catch (ParseException ex) {
                controller.updateEditableProperty(editableProperty, null);
              }
            }
    
            public void insertUpdate(DocumentEvent ev) {
              changedUpdate(ev);
            }
    
            public void removeUpdate(DocumentEvent ev) {
              changedUpdate(ev);
            }
          });
      }

      this.toolTipEditableTextFields.put(editableProperty, textField);
    }
  }

  private static void updateToolTipTextFieldFormatterFactory(JFormattedTextField textField,
                                                             PlanController.EditableProperty editableProperty,
                                                             UserPreferences preferences) {
    InternationalFormatter formatter;
    if (editableProperty == PlanController.EditableProperty.ANGLE) {      
      formatter = new NumberFormatter(NumberFormat.getNumberInstance());
    } else {
      Format lengthFormat = preferences.getLengthUnit().getFormat();
      if (lengthFormat instanceof NumberFormat) {
        formatter = new NumberFormatter((NumberFormat)lengthFormat);
      } else {
        formatter = new InternationalFormatter(lengthFormat);
      }
    }
    textField.setFormatterFactory(new DefaultFormatterFactory(formatter));
  }
  
  /**
   * Returns a custom cursor with a hot spot point at center of cursor.
   */ 
  private Cursor createCustomCursor(String smallCursorImageResource, 
                                    String largeCursorImageResource,
                                    String cursorName,
                                    int    defaultCursor) {
    return createCustomCursor(PlanComponent.class.getResource(smallCursorImageResource), 
        PlanComponent.class.getResource(largeCursorImageResource), 
        0.5f, 0.5f, cursorName, 
        Cursor.getPredefinedCursor(defaultCursor));
  }

  /**
   * Returns a custom cursor created from images in parameters.
   */ 
  protected Cursor createCustomCursor(URL smallCursorImageUrl, 
                                      URL largeCursorImageUrl,
                                      float yCursorHotSpot,
                                      float xCursorHotSpot,
                                      String cursorName,
                                      Cursor defaultCursor) {
    if (GraphicsEnvironment.isHeadless()) {
      return defaultCursor;
    }
    // Retrieve system cursor size
    Dimension cursorSize = getToolkit().getBestCursorSize(16, 16);
    URL cursorImageResource;
    // If returned cursor size is 0, system doesn't support custom cursor  
    if (cursorSize.width == 0) {      
      return defaultCursor;      
    } else {
      // Use a different cursor image depending on system cursor size 
      if (cursorSize.width > 16) {
        cursorImageResource = largeCursorImageUrl;
      } else {
        cursorImageResource = smallCursorImageUrl;
      }
      try {
        // Read cursor image
        BufferedImage cursorImage = ImageIO.read(cursorImageResource);
        // Create custom cursor from image
        return getToolkit().createCustomCursor(cursorImage, 
            new Point(Math.round(cursorSize.width * xCursorHotSpot), 
                      Math.round(cursorSize.height * yCursorHotSpot)),
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
    if (!this.planBoundsCacheValid) {
      // Always enlarge plan bounds only when plan component is a child of a scroll pane 
      if (this.planBoundsCache == null 
          || !(getParent() instanceof JViewport)) {
        // Ensure plan bounds are 10 x 10 meters wide at minimum
        this.planBoundsCache = new Rectangle2D.Float(0, 0, 1000, 1000);
      }
      // Enlarge plan bounds to include background images, home bounds and observer camera
      if (this.backgroundImageCache != null) {
        BackgroundImage backgroundImage = this.home.getBackgroundImage();
        if (backgroundImage != null) {
          this.planBoundsCache.add(this.backgroundImageCache.getWidth() * backgroundImage.getScale() - backgroundImage.getXOrigin(),
              this.backgroundImageCache.getHeight() * backgroundImage.getScale() - backgroundImage.getYOrigin());
        }
        for (Level level : this.home.getLevels()) {
          BackgroundImage levelBackgroundImage = level.getBackgroundImage();
          if (levelBackgroundImage != null) {
            this.planBoundsCache.add(this.backgroundImageCache.getWidth() * levelBackgroundImage.getScale() - levelBackgroundImage.getXOrigin(),
                this.backgroundImageCache.getHeight() * levelBackgroundImage.getScale() - levelBackgroundImage.getYOrigin());
          }
        }
      }
      Rectangle2D homeItemsBounds = getItemsBounds(getGraphics(), getPaintedItems());
      if (homeItemsBounds != null) {
        this.planBoundsCache.add(homeItemsBounds);
      }
      for (float [] point : this.home.getObserverCamera().getPoints()) {
        this.planBoundsCache.add(point [0], point [1]);
      }
      this.planBoundsCacheValid = true;
    }
    return this.planBoundsCache;
  }
  
  /**
   * Returns the collection of walls, furniture, rooms and dimension lines of the home 
   * painted by this component wherever the level they belong to is selected or not.
   */
  protected List<Selectable> getPaintedItems() {
    List<Selectable> homeItems = new ArrayList<Selectable>(this.home.getWalls());
    for (HomePieceOfFurniture piece : this.home.getFurniture()) {
      if (piece.isVisible()) {
        homeItems.add(piece);
      }
    }
    homeItems.addAll(this.home.getRooms());
    homeItems.addAll(this.home.getDimensionLines());
    homeItems.addAll(this.home.getLabels());
    Compass compass = this.home.getCompass();
    if (compass.isVisible()) {
      homeItems.add(compass);
    }
    return homeItems;
  }
  
  /**
   * Returns the bounds of the given collection of <code>items</code>.
   */
  private Rectangle2D getItemsBounds(Graphics g, Collection<? extends Selectable> items) {
    Rectangle2D itemsBounds = null;
    for (Selectable item : items) {
      if (itemsBounds == null) {
        itemsBounds = getItemBounds(g, item);
      } else {
        itemsBounds.add(getItemBounds(g, item));
      }
    } 
    return itemsBounds;
  }
  
  /**
   * Returns the bounds of the given <code>item</code>.
   */
  protected Rectangle2D getItemBounds(Graphics g, Selectable item) {
    // Add to bounds all the visible items
    float [][] points = item.getPoints();
    Rectangle2D itemBounds = new Rectangle2D.Float(points [0][0], points [0][1], 0, 0);
    for (int i = 1; i < points.length; i++) {
      itemBounds.add(points [i][0], points [i][1]);
    }
    
    // Retrieve used font
    Font componentFont;
    if (g != null) {
      componentFont = g.getFont();
    } else {
      componentFont = getFont();
    }
    
    if (item instanceof Room) {
      // Add to bounds the displayed name and area bounds of each room 
      Room room = (Room)item;
      float xRoomCenter = room.getXCenter();
      float yRoomCenter = room.getYCenter();
      String roomName = room.getName();
      if (roomName != null && roomName.length() > 0) {
        float xName = xRoomCenter + room.getNameXOffset(); 
        float yName = yRoomCenter + room.getNameYOffset();
        TextStyle nameStyle = room.getNameStyle();
        if (nameStyle == null) {
          nameStyle = this.preferences.getDefaultTextStyle(room.getClass());
        }          
        FontMetrics nameFontMetrics = getFontMetrics(componentFont, nameStyle);
        Rectangle2D nameBounds = nameFontMetrics.getStringBounds(roomName, g);
        itemBounds.add(xName - nameBounds.getWidth() / 2, 
            yName - nameFontMetrics.getAscent());
        itemBounds.add(xName + nameBounds.getWidth() / 2, 
            yName + nameFontMetrics.getDescent());
      }
      if (room.isAreaVisible()) {
        float area = room.getArea();
        if (area > 0.01f) {
          float xArea = xRoomCenter + room.getAreaXOffset(); 
          float yArea = yRoomCenter + room.getAreaYOffset();
          String areaText = this.preferences.getLengthUnit().getAreaFormatWithUnit().format(area);
          TextStyle areaStyle = room.getAreaStyle();
          if (areaStyle == null) {
            areaStyle = this.preferences.getDefaultTextStyle(room.getClass());
          }          
          FontMetrics areaFontMetrics = getFontMetrics(componentFont, areaStyle);
          Rectangle2D areaTextBounds = areaFontMetrics.getStringBounds(areaText, g);
          itemBounds.add(xArea - areaTextBounds.getWidth() / 2, 
              yArea - areaFontMetrics.getAscent());
          itemBounds.add(xArea + areaTextBounds.getWidth() / 2, 
              yArea + areaFontMetrics.getDescent());
        }
      }
    } else if (item instanceof HomePieceOfFurniture) {
      if (item instanceof HomeDoorOrWindow) {
        HomeDoorOrWindow doorOrWindow = (HomeDoorOrWindow)item;
        // Add to bounds door and window sashes 
        for (Sash sash : doorOrWindow.getSashes()) {
          itemBounds.add(getDoorOrWindowSashShape(doorOrWindow, sash).getBounds2D());
        }
      } else if (item instanceof HomeFurnitureGroup) {
        itemBounds.add(getItemsBounds(g, ((HomeFurnitureGroup)item).getFurniture()));
      }
      // Add to bounds the displayed name of the piece of furniture 
      HomePieceOfFurniture piece = (HomePieceOfFurniture)item;
      float xPiece = piece.getX();
      float yPiece = piece.getY();
      String pieceName = piece.getName();
      if (piece.isVisible()
          && piece.isNameVisible()
          && pieceName.length() > 0) {
        float xName = xPiece + piece.getNameXOffset(); 
        float yName = yPiece + piece.getNameYOffset();
        TextStyle nameStyle = piece.getNameStyle();
        if (nameStyle == null) {
          nameStyle = this.preferences.getDefaultTextStyle(piece.getClass());
        }          
        FontMetrics nameFontMetrics = getFontMetrics(componentFont, nameStyle);
        Rectangle2D nameBounds = nameFontMetrics.getStringBounds(pieceName, g);
        itemBounds.add(xName - nameBounds.getWidth() / 2, 
            yName - nameFontMetrics.getAscent());
        itemBounds.add(xName + nameBounds.getWidth() / 2, 
            yName + nameFontMetrics.getDescent());
      }
    } else if (item instanceof DimensionLine) {
      // Add to bounds the text bounds of the dimension line length 
      DimensionLine dimensionLine = (DimensionLine)item;
      float dimensionLineLength = dimensionLine.getLength();
      String lengthText = this.preferences.getLengthUnit().getFormat().format(dimensionLineLength);
      TextStyle lengthStyle = dimensionLine.getLengthStyle();
      if (lengthStyle == null) {
        lengthStyle = this.preferences.getDefaultTextStyle(dimensionLine.getClass());
      }          
      FontMetrics lengthFontMetrics = getFontMetrics(componentFont, lengthStyle);
      Rectangle2D lengthTextBounds = lengthFontMetrics.getStringBounds(lengthText, g);
      // Transform length text bounding rectangle corners to their real location
      double angle = Math.atan2(dimensionLine.getYEnd() - dimensionLine.getYStart(), 
          dimensionLine.getXEnd() - dimensionLine.getXStart());
      AffineTransform transform = new AffineTransform();
      transform.translate(dimensionLine.getXStart(), dimensionLine.getYStart());
      transform.rotate(angle);
      transform.translate(0, dimensionLine.getOffset());
      transform.translate((dimensionLineLength - lengthTextBounds.getWidth()) / 2, 
          dimensionLine.getOffset() <= 0 
              ? -lengthFontMetrics.getDescent() - 1
              : lengthFontMetrics.getAscent() + 1);
      GeneralPath lengthTextBoundsPath = new GeneralPath(lengthTextBounds);
      for (PathIterator it = lengthTextBoundsPath.getPathIterator(transform); !it.isDone(); ) {
        float [] pathPoint = new float[2];
        if (it.currentSegment(pathPoint) != PathIterator.SEG_CLOSE) {
          itemBounds.add(pathPoint [0], pathPoint [1]);
        }
        it.next();
      }
      // Add to bounds the end lines drawn at dimension line start and end  
      transform = new AffineTransform();
      transform.translate(dimensionLine.getXStart(), dimensionLine.getYStart());
      transform.rotate(angle);
      transform.translate(0, dimensionLine.getOffset());
      for (PathIterator it = DIMENSION_LINE_END.getPathIterator(transform); !it.isDone(); ) {
        float [] pathPoint = new float[2];
        if (it.currentSegment(pathPoint) != PathIterator.SEG_CLOSE) {
          itemBounds.add(pathPoint [0], pathPoint [1]);
        }
        it.next();
      }
      transform.translate(dimensionLineLength, 0);
      for (PathIterator it = DIMENSION_LINE_END.getPathIterator(transform); !it.isDone(); ) {
        float [] pathPoint = new float[2];
        if (it.currentSegment(pathPoint) != PathIterator.SEG_CLOSE) {
          itemBounds.add(pathPoint [0], pathPoint [1]);
        }
        it.next();
      }
    } else if (item instanceof Label) {
      // Add to bounds the displayed text of a label 
      Label label = (Label)item;
      float xLabel = label.getX();
      float yLabel = label.getY();
      String labelText = label.getText();
      TextStyle labelStyle = label.getStyle();
      if (labelStyle == null) {
        labelStyle = this.preferences.getDefaultTextStyle(label.getClass());
      }          
      FontMetrics labelFontMetrics = getFontMetrics(componentFont, labelStyle);
      Rectangle2D labelBounds = labelFontMetrics.getStringBounds(labelText, g);
      itemBounds.add(xLabel - labelBounds.getWidth() / 2, 
          yLabel - labelFontMetrics.getAscent());
      itemBounds.add(xLabel + labelBounds.getWidth() / 2, 
          yLabel + labelFontMetrics.getDescent());
    } else if (item instanceof Compass) {
      Compass compass = (Compass)item;
      AffineTransform transform = AffineTransform.getTranslateInstance(compass.getX(), compass.getY());
      transform.scale(compass.getDiameter(), compass.getDiameter());
      transform.rotate(compass.getNorthDirection());
      return COMPASS.createTransformedShape(transform).getBounds2D();
    } 
    return itemBounds;
  }
  
  /**
   * Returns the coordinates of the bounding rectangle of the <code>text</code> centered at
   * the point (<code>x</code>,<code>y</code>).  
   */
  public float [][] getTextBounds(String text, TextStyle style, 
                                  float x, float y, float angle) {
    FontMetrics fontMetrics = getFontMetrics(getFont(), style);
    Rectangle2D textBounds = fontMetrics.getStringBounds(text, null);
    float halfTextLength = (float)textBounds.getWidth() / 2;
    float textBaseLine = fontMetrics.getAscent();
    if (angle == 0) {
      return new float [][] {
          {x - halfTextLength, y - textBaseLine},
          {x + halfTextLength, y - textBaseLine},
          {x + halfTextLength, y + (float)textBounds.getHeight() - textBaseLine},
          {x - halfTextLength, y + (float)textBounds.getHeight() - textBaseLine}};
    } else {
      // Transform text bounding rectangle corners to their real location
      AffineTransform transform = new AffineTransform();
      transform.translate(x - halfTextLength, y + (float)textBounds.getHeight() - textBaseLine);
      transform.rotate(angle);
      GeneralPath textBoundsPath = new GeneralPath(textBounds);
      List<float []> textPoints = new ArrayList<float[]>(4);
      for (PathIterator it = textBoundsPath.getPathIterator(transform); !it.isDone(); ) {
        float [] pathPoint = new float[2];
        if (it.currentSegment(pathPoint) != PathIterator.SEG_CLOSE) {
          textPoints.add(pathPoint);
        }
        it.next();
      }
      return textPoints.toArray(new float [textPoints.size()][]);
    }
  }
  
  /**
   * Returns the AWT font matching a given text style.
   */
  protected Font getFont(Font defaultFont, TextStyle textStyle) {
    if (this.fonts == null) {
      this.fonts = new WeakHashMap<TextStyle, Font>();
    }
    Font font = this.fonts.get(textStyle);
    if (font == null) {
      int fontStyle = Font.PLAIN;
      if (textStyle.isBold()) {
        fontStyle = Font.BOLD;
      }
      if (textStyle.isItalic()) {
        fontStyle |= Font.ITALIC;
      }
      String fontName = null;
      if (defaultFont != null) {
        fontName = defaultFont.getName();
      } else {
        fontName = null;
      }
      font = new Font(fontName, fontStyle, 1);
      font = font.deriveFont(textStyle.getFontSize());
      this.fonts.put(textStyle, font);
    }
    return font;
  }

  /**
   * Returns the font metrics matching a given text style.
   */
  protected FontMetrics getFontMetrics(Font defaultFont, TextStyle textStyle) {
    if (this.fontsMetrics == null) {
      this.fontsMetrics = new WeakHashMap<TextStyle, FontMetrics>();
    }
    FontMetrics fontMetrics = this.fontsMetrics.get(textStyle);
    if (fontMetrics == null) {
      fontMetrics = getFontMetrics(getFont(defaultFont, textStyle));
      this.fontsMetrics.put(textStyle, fontMetrics);
    }
    return fontMetrics;
  }

  /**
   * Sets whether plan's background should be painted or not.
   * Background may include grid and an image.   
   */
  public void setBackgroundPainted(boolean backgroundPainted) {
    if (this.backgroundPainted != backgroundPainted) {
      this.backgroundPainted = backgroundPainted;
      repaint();
    }
  }
  
  /**
   * Returns <code>true</code> if plan's background should be painted.
   */
  public boolean isBackgroundPainted() {
    return this.backgroundPainted;
  }

  /**
   * Sets whether the outline of home selected items should be painted or not.
   */
  public void setSelectedItemsOutlinePainted(boolean selectedItemsOutlinePainted) {
    if (this.selectedItemsOutlinePainted != selectedItemsOutlinePainted) {
      this.selectedItemsOutlinePainted = selectedItemsOutlinePainted;
      repaint();
    }
  }
  
  /**
   * Returns <code>true</code> if the outline of home selected items should be painted.
   */
  public boolean isSelectedItemsOutlinePainted() {
    return this.selectedItemsOutlinePainted;
  }
  
  /**
   * Paints this component.
   */
  @Override
  protected void paintComponent(Graphics g) {
    Graphics2D g2D = (Graphics2D)g.create();
    Color backgroundColor = getBackground();
    Color foregroundColor = getForeground();
    if (this.backgroundPainted) {
      paintBackground(g2D, backgroundColor);
    }
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
    try {
      paintContent(g2D, paintScale, backgroundColor, foregroundColor, PaintMode.PAINT);
    } catch (InterruptedIOException ex) {
      // Ignore exception because it may happen only in EXPORT paint mode 
    }   
    g2D.dispose();
  }

  /**
   * Returns the print preferred scale of the plan drawn in this component
   * to make it fill <code>pageFormat</code> imageable size.
   */
  public float getPrintPreferredScale(Graphics g, PageFormat pageFormat) {
    List<Selectable> printedItems = getPaintedItems(); 
    Rectangle2D printedItemBounds = getItemsBounds(g, printedItems);
    if (printedItemBounds != null) {
      float imageableWidthCm = LengthUnit.inchToCentimeter((float)pageFormat.getImageableWidth() / 72);
      float imageableHeightCm = LengthUnit.inchToCentimeter((float)pageFormat.getImageableHeight() / 72);
      float extraMargin = getStrokeWidthExtraMargin(printedItems);
      // Compute the largest integer scale possible
      int scaleInverse = (int)Math.ceil(Math.max(
          (printedItemBounds.getWidth() + 2 * extraMargin) / imageableWidthCm,
          (printedItemBounds.getHeight() + 2 * extraMargin) / imageableHeightCm));
      return 1f / scaleInverse;
    } else {
      return 0;
    }
  }
  
  /**
   * Returns the margin that should be added around home items bounds to ensure their
   * line stroke width is always fully visible.
   */
  private float getStrokeWidthExtraMargin(List<Selectable> items) {
    float extraMargin = BORDER_STROKE_WIDTH / 2;
    if (Home.getWallsSubList(items).size() > 0
        || Home.getRoomsSubList(items).size() > 0) {
      extraMargin = WALL_STROKE_WIDTH / 2;
    }
    return extraMargin;
  }
  
  /**
   * Prints this component plan at the scale given in the home print attributes or at a scale 
   * that makes it fill <code>pageFormat</code> imageable size if this attribute is <code>null</code>.
   */
  public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
    List<Selectable> printedItems = getPaintedItems(); 
    Rectangle2D printedItemBounds = getItemsBounds(g, printedItems);
    if (printedItemBounds != null) {
      double imageableX = pageFormat.getImageableX();
      double imageableY = pageFormat.getImageableY();
      double imageableWidth = pageFormat.getImageableWidth();
      double imageableHeight = pageFormat.getImageableHeight();
      float printScale;
      float rowIndex;
      float columnIndex;
      int pagesPerRow;
      int pagesPerColumn;
      if (this.home.getPrint() == null || this.home.getPrint().getPlanScale() == null) {
        // Compute a scale that ensures the plan will fill the component if plan scale is null
        printScale = getPrintPreferredScale(g, pageFormat) * LengthUnit.centimeterToInch(72);
        if (pageIndex > 0) {
          return NO_SUCH_PAGE;
        }
        pagesPerRow = 1;
        pagesPerColumn = 1;
        rowIndex   = 0;
        columnIndex = 0;
      } else {
        // Apply print scale to paper size expressed in 1/72nds of an inch
        printScale = this.home.getPrint().getPlanScale().floatValue() * LengthUnit.centimeterToInch(72);
        pagesPerRow = (int)(printedItemBounds.getWidth() * printScale / imageableWidth);
        if (printedItemBounds.getWidth() * printScale != imageableWidth) {
          pagesPerRow++;
        }
        pagesPerColumn = (int)(printedItemBounds.getHeight() * printScale / imageableHeight);
        if (printedItemBounds.getHeight() * printScale != imageableHeight) {
          pagesPerColumn++;
        }
        if (pageIndex >= pagesPerRow * pagesPerColumn) {
          return NO_SUCH_PAGE;
        }
        rowIndex = pageIndex / pagesPerRow;
        columnIndex = pageIndex - rowIndex * pagesPerRow;
      }
          
      Graphics2D g2D = (Graphics2D)g.create();
      g2D.clip(new Rectangle2D.Double(imageableX, imageableY, imageableWidth, imageableHeight));
      // Change coordinates system to paper imageable origin
      g2D.translate(imageableX - columnIndex * imageableWidth, imageableY - rowIndex * imageableHeight);
      g2D.scale(printScale, printScale);
      float extraMargin = getStrokeWidthExtraMargin(printedItems);
      g2D.translate(-printedItemBounds.getMinX() + extraMargin,
          -printedItemBounds.getMinY() + extraMargin);
      // Center plan in component if possible
      g2D.translate(Math.max(0, 
              (imageableWidth * pagesPerRow / printScale - printedItemBounds.getWidth() - 2 * extraMargin) / 2), 
          Math.max(0, 
              (imageableHeight * pagesPerColumn / printScale - printedItemBounds.getHeight() - 2 * extraMargin) / 2));
      setRenderingHints(g2D);
      try {
        // Print component contents
        paintContent(g2D, printScale, Color.WHITE, Color.BLACK, PaintMode.PRINT);
      } catch (InterruptedIOException ex) {
        // Ignore exception because it may happen only in EXPORT paint mode 
      }   
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
      float extraMargin = getStrokeWidthExtraMargin(this.home.getSelectedItems());
      BufferedImage image = new BufferedImage((int)Math.ceil(selectionBounds.getWidth() * clipboardScale + 2 * extraMargin), 
              (int)Math.ceil(selectionBounds.getHeight() * clipboardScale + 2 * extraMargin), BufferedImage.TYPE_INT_RGB);      
      Graphics2D g2D = (Graphics2D)image.getGraphics();
      // Paint background in white
      g2D.setColor(Color.WHITE);
      g2D.fillRect(0, 0, image.getWidth(), image.getHeight());
      // Change component coordinates system to plan system
      g2D.scale(clipboardScale, clipboardScale);
      g2D.translate(-selectionBounds.getMinX() + extraMargin,
          -selectionBounds.getMinY() + extraMargin);
      setRenderingHints(g2D);
      try {
        // Paint component contents
        paintContent(g2D, clipboardScale, Color.WHITE, Color.BLACK, PaintMode.CLIPBOARD);
      } catch (InterruptedIOException ex) {
        // Ignore exception because it may happen only in EXPORT paint mode 
      }   
      g2D.dispose();
      return image;
    }
  }
  
  /**
   * Writes this plan in the given output stream at SVG (Scalable Vector Graphics) format.
   */
  public void exportToSVG(OutputStream outputStream) throws IOException {
    SVGSupport.exportToSVG(outputStream, this);   
  }
  
  /**
   * Separated static class to be able to exclude FreeHEP library from classpath
   * in case the application doesn't use export to SVG format. 
   */
  private static class SVGSupport {
    public static void exportToSVG(OutputStream outputStream, 
                                   PlanComponent planComponent) throws IOException {
      List<Selectable> homeItems = planComponent.getPaintedItems();
      Rectangle2D svgItemBounds = planComponent.getItemsBounds(null, homeItems);
      if (svgItemBounds == null) {
        svgItemBounds = new Rectangle2D.Float();
      }
      
      float svgScale = 1f;
      float extraMargin = planComponent.getStrokeWidthExtraMargin(homeItems);
      Dimension imageSize = new Dimension((int)Math.ceil(svgItemBounds.getWidth() * svgScale + 2 * extraMargin), 
          (int)Math.ceil(svgItemBounds.getHeight() * svgScale + 2 * extraMargin));
      
      SVGGraphics2D exportG2D = new SVGGraphics2D(outputStream, imageSize) {
          @Override
          public void writeHeader() throws IOException {
            // Use English locale to avoid wrong encoding when localized dates contain accentuated letters 
            Locale defaultLocale = Locale.getDefault();
            Locale.setDefault(Locale.ENGLISH);
            super.writeHeader();
            Locale.setDefault(defaultLocale);
          }
        };
      UserProperties properties = new UserProperties();
      properties.setProperty(SVGGraphics2D.STYLABLE, true);
      properties.setProperty(SVGGraphics2D.WRITE_IMAGES_AS, ImageConstants.PNG);
      properties.setProperty(SVGGraphics2D.TITLE, 
          planComponent.home.getName() != null 
              ? planComponent.home.getName() 
              : "" );
      properties.setProperty(SVGGraphics2D.FOR, System.getProperty("user.name", ""));
      exportG2D.setProperties(properties);
      exportG2D.startExport();
      exportG2D.translate(-svgItemBounds.getMinX() + extraMargin,
          -svgItemBounds.getMinY() + extraMargin);
      
      planComponent.checkCurrentThreadIsntInterrupted(PaintMode.EXPORT);
      planComponent.paintContent(exportG2D, svgScale, Color.WHITE, Color.BLACK, PaintMode.EXPORT);   
      exportG2D.endExport();
    }
  }
  
  /**
   * Throws an <code>InterruptedRecorderException</code> exception if current thread 
   * is interrupted and <code>paintMode</code> is equal to <code>PaintMode.EXPORT</code>.  
   */
  private void checkCurrentThreadIsntInterrupted(PaintMode paintMode) throws InterruptedIOException {
    if (paintMode == PaintMode.EXPORT
        && Thread.interrupted()) {
      throw new InterruptedIOException("Current thread interrupted");
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
   * Paints background image and returns <code>true</code> if an image is painted.
   */
  private boolean paintBackgroundImage(Graphics2D g2D, PaintMode paintMode) {
    final BackgroundImage backgroundImage = this.home.getSelectedLevel() == null 
        ? this.home.getBackgroundImage()
        : this.home.getSelectedLevel().getBackgroundImage();
    if (backgroundImage != null && backgroundImage.isVisible()) {
      if (this.backgroundImageCache == null && paintMode == PaintMode.PAINT) {
        // Load background image in an executor
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
              backgroundImageCache = readImage(backgroundImage.getImage()); 
              revalidate();
            }
          });
      } else {
        // Paint image at specified scale with 0.7 alpha
        AffineTransform previousTransform = g2D.getTransform();
        g2D.translate(-backgroundImage.getXOrigin(), -backgroundImage.getYOrigin());
        g2D.scale(backgroundImage.getScale(), backgroundImage.getScale());
        Composite oldComposite = setTransparency(g2D, 0.7f);
        g2D.drawImage(this.backgroundImageCache != null
            ? this.backgroundImageCache
            : readImage(backgroundImage.getImage()), 0, 0, this);
        g2D.setComposite(oldComposite);
        g2D.setTransform(previousTransform);
      }
      return true;
    } else {
      return false;
    }
  }

  /**
   * Returns the image contained in <code>imageContent</code> or an empty image if reading failed.
   */
  private BufferedImage readImage(Content imageContent) {
    InputStream contentStream = null;
    try {
      try {
        contentStream = imageContent.openStream();
        return ImageIO.read(contentStream);
      } finally {
        contentStream.close();
      }
    } catch (IOException ex) {
      return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
      // Ignore exceptions, the user may know its background image is incorrect 
      // if he tries to modify the background image
    }
  } 

  /**
   * Paints walls and rooms of lower levels or upper levels to help the user draw in the selected level. 
   */
  private void paintOtherLevels(Graphics2D g2D, float planScale, 
                                Color backgroundColor, Color foregroundColor) {
    List<Level> levels = this.home.getLevels();
    Level selectedLevel = this.home.getSelectedLevel();
    if (levels.size() > 1 
        && selectedLevel != null) {
      int selectedLevelIndex = levels.indexOf(selectedLevel);
      boolean level0 = levels.get(0) == selectedLevel 
          || levels.get(selectedLevelIndex - 1).getElevation() == selectedLevel.getElevation();
      if (this.otherLevelRoomAreaCache == null
          && (!level0 || levels.size() >= 2)) {
        // Search floors in level 1 or ceilings in previous level 
        Level otherLevel = levels.get(level0 && selectedLevelIndex < levels.size() - 1 
            ? selectedLevelIndex + 1 
            : selectedLevelIndex  - 1);
        List<Room> otherLevelRooms = new ArrayList<Room>();
        for (Room room : this.home.getRooms()) {
          if (room.isAtLevel(otherLevel)
              && (level0 && room.isFloorVisible()
                  || !level0 && room.isCeilingVisible())) {
            otherLevelRooms.add(room);
          }
        }
        if (otherLevelRooms.size() > 0) {
          this.otherLevelRoomAreaCache = getItemsArea(otherLevelRooms);
          this.otherLevelRoomsCache = otherLevelRooms;
        }
      }
      if (this.otherLevelRoomAreaCache != null) {
        Composite oldComposite = setTransparency(g2D, 
            this.preferences.isGridVisible() ? 0.2f : 0.1f);
        g2D.setPaint(Color.GRAY);
        g2D.fill(this.otherLevelRoomAreaCache);
        g2D.setComposite(oldComposite);
      }

      if (this.otherLevelWallAreaCache == null) {
        // Search visible walls in level 1 or in previous level 
        Level otherLevel = levels.get(level0 && selectedLevelIndex < levels.size() - 1 
            ? selectedLevelIndex + 1 
            : selectedLevelIndex  - 1);
        List<Wall> otherLevelwalls = new ArrayList<Wall>();
        for (Wall wall : this.home.getWalls()) {
          if (!isViewableAtSelectedLevel(wall)
              && wall.isAtLevel(otherLevel)) {
            otherLevelwalls.add(wall);
          }
        }
        if (otherLevelwalls.size() > 0) {
          this.otherLevelWallAreaCache = getItemsArea(otherLevelwalls);
          this.otherLevelWallsCache = otherLevelwalls;
        }
      }
      if (this.otherLevelWallAreaCache != null) {
        Composite oldComposite = setTransparency(g2D, 
            this.preferences.isGridVisible() ? 0.2f : 0.1f);
        fillAndDrawArea(g2D, this.otherLevelWallAreaCache, planScale, 
            getWallPaint(planScale, backgroundColor, foregroundColor, this.preferences.getWallPattern()), foregroundColor);
        g2D.setComposite(oldComposite);
      }
    }
  }

  /**
   * Sets the transparency composite to the given percentage and returns the old composite.
   */
  private Composite setTransparency(Graphics2D g2D, float alpha) {
    Composite oldComposite = g2D.getComposite();
    if (oldComposite instanceof AlphaComposite) {
      g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 
        ((AlphaComposite)oldComposite).getAlpha() * alpha));
    } else {
      g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    }
    return oldComposite;
  }

  /**
   * Paints background grid lines.
   */
  private void paintGrid(Graphics2D g2D, float gridScale) {
    float gridSize = getGridSize(gridScale);
    float mainGridSize = getMainGridSize(gridScale);
    
    Rectangle2D planBounds = getPlanBounds();
    float xMin = (float)planBounds.getMinX() - MARGIN;
    float yMin = (float)planBounds.getMinY() - MARGIN;
    float xMax = convertXPixelToModel(getWidth());
    float yMax = convertYPixelToModel(getHeight());
    if (OperatingSystem.isMacOSX()
        && System.getProperty("apple.awt.graphics.UseQuartz", "false").equals("false")) {
      // Draw grid with an image texture in under Mac OS X, because default 2D rendering engine 
      // is too slow and can't be replaced by Quartz engine in applet environment 
      int imageWidth = Math.round(mainGridSize * gridScale);
      BufferedImage gridImage = new BufferedImage(imageWidth, imageWidth, BufferedImage.TYPE_INT_ARGB);
      Graphics2D imageGraphics = (Graphics2D)gridImage.getGraphics();
      setRenderingHints(imageGraphics);
      imageGraphics.scale(gridScale, gridScale);
      
      paintGridLines(imageGraphics, gridScale, 0, mainGridSize, 0, mainGridSize, gridSize, mainGridSize);    
      imageGraphics.dispose();
      
      g2D.setPaint(new TexturePaint(gridImage, new Rectangle2D.Float(0, 0, mainGridSize, mainGridSize)));
      
      g2D.fill(new Rectangle2D.Float(xMin, yMin, xMax - xMin, yMax - yMin));
    } else {
      paintGridLines(g2D, gridScale, xMin, xMax, yMin, yMax, gridSize, mainGridSize);          
    }
  }

  /**
   * Paints background grid lines from <code>xMin</code> to <code>xMax</code> 
   * and <code>yMin</code> to <code>yMax</code>.
   */
  private void paintGridLines(Graphics2D g2D, float gridScale, 
                              float xMin, float xMax, float yMin, float yMax, 
                              float gridSize, float mainGridSize) {
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
   * Returns the space between main lines grid.
   */
  private float getMainGridSize(float gridScale) {
    float [] mainGridSizes;
    if (this.preferences.getLengthUnit() == LengthUnit.INCH) {
      // Use a grid in inch and foot with a minimum grid increment of 1 inch
      float oneFoot = 2.54f * 12;
      mainGridSizes = new float [] {oneFoot, 3 * oneFoot, 6 * oneFoot, 
                                    12 * oneFoot, 24 * oneFoot, 48 * oneFoot, 96 * oneFoot, 192 * oneFoot, 384 * oneFoot}; 
    } else {
      // Use a grid in cm and meters with a minimum grid increment of 1 cm
      mainGridSizes = new float [] {100, 200, 500, 1000, 2000, 5000, 10000};
    }
    // Compute grid size to get a grid where the space between each line is less than 50 pixels
    float mainGridSize = mainGridSizes [0];
    for (int i = 1; i < mainGridSizes.length && mainGridSize * gridScale < 50; i++) {
      mainGridSize = mainGridSizes [i];
    }
    return mainGridSize;
  }
  
  /**
   * Returns the space between lines grid.
   */
  private float getGridSize(float gridScale) {
    float [] gridSizes;
    if (this.preferences.getLengthUnit() == LengthUnit.INCH) {
      // Use a grid in inch and foot with a minimum grid increment of 1 inch
      float oneFoot = 2.54f * 12;
      gridSizes = new float [] {2.54f, 5.08f, 7.62f, 15.24f, oneFoot, 3 * oneFoot, 6 * oneFoot, 
                                12 * oneFoot, 24 * oneFoot, 48 * oneFoot, 96 * oneFoot, 192 * oneFoot, 384 * oneFoot};
    } else {
      // Use a grid in cm and meters with a minimum grid increment of 1 cm
      gridSizes = new float [] {1, 2, 5, 10, 20, 50, 100, 200, 500, 1000, 2000, 5000, 10000};
    }
    // Compute grid size to get a grid where the space between each line is less than 10 pixels
    float gridSize = gridSizes [0];
    for (int i = 1; i < gridSizes.length && gridSize * gridScale < 10; i++) {
      gridSize = gridSizes [i];
    }
    return gridSize;
  }
  
  /**
   * Paints plan items.
   * @throws InterruptedIOException if painting was interrupted (may happen only 
   *           if <code>paintMode</code> is equal to <code>PaintMode.EXPORT</code>).
   */
  private void paintContent(Graphics2D g2D, float planScale, 
                            Color backgroundColor, Color foregroundColor, PaintMode paintMode) throws InterruptedIOException {
    if (this.backgroundPainted) {
      paintBackgroundImage(g2D, paintMode);
      if (paintMode == PaintMode.PAINT) {
        paintOtherLevels(g2D, planScale, backgroundColor, foregroundColor);
        if (this.preferences.isGridVisible()) {
          paintGrid(g2D, planScale);
        }
      }
    }
    
    paintHomeItems(g2D, planScale, backgroundColor, foregroundColor, paintMode);
        
    if (paintMode == PaintMode.PAINT) {
      List<Selectable> selectedItems = this.home.getSelectedItems();
      
      Color selectionColor = getSelectionColor(); 
      Color furnitureOutlineColor = getFurnitureOutlineColor();
      Paint selectionOutlinePaint = new Color(selectionColor.getRed(), selectionColor.getGreen(), 
          selectionColor.getBlue(), 128);
      Stroke selectionOutlineStroke = new BasicStroke(6 / planScale, 
          BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND); 
      Stroke dimensionLinesSelectionOutlineStroke = new BasicStroke(4 / planScale, 
          BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND); 
      Stroke locationFeedbackStroke = new BasicStroke(
          1 / planScale, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 0, 
          new float [] {20 / planScale, 5 / planScale, 5 / planScale, 5 / planScale}, 4 / planScale);
      
      paintCamera(g2D, selectedItems, selectionOutlinePaint, selectionOutlineStroke, selectionColor, 
          planScale, backgroundColor, foregroundColor);
      
      // Paint alignment feedback depending on aligned object class
      if (this.alignedObjectClass != null) {
        if (Wall.class.isAssignableFrom(this.alignedObjectClass)) {
          paintWallAlignmentFeedback(g2D, (Wall)this.alignedObjectFeedback, this.locationFeeback, this.showPointFeedback, 
              selectionColor, locationFeedbackStroke, planScale,
              selectionOutlinePaint, selectionOutlineStroke);
        } else if (Room.class.isAssignableFrom(this.alignedObjectClass)) {
          paintRoomAlignmentFeedback(g2D, (Room)this.alignedObjectFeedback, this.locationFeeback, this.showPointFeedback,
              selectionColor, locationFeedbackStroke, planScale,
              selectionOutlinePaint, selectionOutlineStroke);
        } else if (DimensionLine.class.isAssignableFrom(this.alignedObjectClass)) {
          paintDimensionLineAlignmentFeedback(g2D, (DimensionLine)this.alignedObjectFeedback, this.locationFeeback, this.showPointFeedback,               
              selectionColor, locationFeedbackStroke, planScale,
              selectionOutlinePaint, selectionOutlineStroke);
        }
      }
      if (this.centerAngleFeedback != null) {
       paintAngleFeedback(g2D, this.centerAngleFeedback, this.point1AngleFeedback, this.point2AngleFeedback, 
           planScale, selectionColor); 
      }
      if (this.dimensionLinesFeedback != null) {
        List<Selectable> emptySelection = Collections.emptyList();        
        paintDimensionLines(g2D, this.dimensionLinesFeedback, emptySelection, 
            null, null, null, locationFeedbackStroke, planScale, 
            backgroundColor, selectionColor, paintMode, true);
      }
      
      if (this.draggedItemsFeedback != null) {
        paintDimensionLines(g2D, Home.getDimensionLinesSubList(this.draggedItemsFeedback), this.draggedItemsFeedback, 
            selectionOutlinePaint, dimensionLinesSelectionOutlineStroke, null, 
            locationFeedbackStroke, planScale, backgroundColor, foregroundColor, paintMode, false);
        paintLabels(g2D, Home.getLabelsSubList(this.draggedItemsFeedback), this.draggedItemsFeedback, 
            selectionOutlinePaint, dimensionLinesSelectionOutlineStroke, 
            planScale, foregroundColor, paintMode);
        paintRoomsOutline(g2D, this.draggedItemsFeedback, selectionOutlinePaint, selectionOutlineStroke, null, 
            planScale, foregroundColor);
        paintWallsOutline(g2D, this.draggedItemsFeedback, selectionOutlinePaint, selectionOutlineStroke, null, 
            planScale, foregroundColor);
        paintFurniture(g2D, Home.getFurnitureSubList(this.draggedItemsFeedback), selectedItems, planScale, null, 
            foregroundColor, furnitureOutlineColor, paintMode, false);
        paintFurnitureOutline(g2D, this.draggedItemsFeedback, selectionOutlinePaint, selectionOutlineStroke, null, 
            planScale, foregroundColor);
      }
      
      paintRectangleFeedback(g2D, selectionColor, planScale);
    }
  }
  
  /**
   * Paints home items at the given scale, and with background and foreground colors.
   * Outline around selected items will be painted only under <code>PAINT</code> mode. 
   */
  protected void paintHomeItems(Graphics g, float planScale, 
                                Color backgroundColor, Color foregroundColor, PaintMode paintMode) throws InterruptedIOException {
    Graphics2D g2D = (Graphics2D)g;
    List<Selectable> selectedItems = this.home.getSelectedItems();
    if (this.sortedLevelFurniture == null) {
      // Sort home furniture in elevation order
      this.sortedLevelFurniture = new ArrayList<HomePieceOfFurniture>();
      for (HomePieceOfFurniture piece : this.home.getFurniture()) {
        if (isViewableAtSelectedLevel(piece)) {
          this.sortedLevelFurniture.add(piece);
        }
      }
      Collections.sort(this.sortedLevelFurniture,
          new Comparator<HomePieceOfFurniture>() {
            public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
              float elevationDelta = piece1.getGroundElevation() - piece2.getGroundElevation();
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
    
    Color selectionColor = getSelectionColor(); 
    Paint selectionOutlinePaint = new Color(selectionColor.getRed(), selectionColor.getGreen(), 
        selectionColor.getBlue(), 128);
    Stroke selectionOutlineStroke = new BasicStroke(6 / planScale, 
        BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND); 
    Stroke dimensionLinesSelectionOutlineStroke = new BasicStroke(4 / planScale, 
        BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND); 
    Stroke locationFeedbackStroke = new BasicStroke(
        1 / planScale, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 0, 
        new float [] {20 / planScale, 5 / planScale, 5 / planScale, 5 / planScale}, 4 / planScale);

    paintCompass(g2D, selectedItems, planScale, foregroundColor, paintMode);

    checkCurrentThreadIsntInterrupted(paintMode);
    paintRooms(g2D, selectedItems, planScale, foregroundColor, paintMode);

    checkCurrentThreadIsntInterrupted(paintMode);
    paintWalls(g2D, selectedItems, planScale, backgroundColor, foregroundColor, paintMode);
    
    checkCurrentThreadIsntInterrupted(paintMode);
    paintFurniture(g2D, this.sortedLevelFurniture, selectedItems, 
        planScale, backgroundColor, foregroundColor, getFurnitureOutlineColor(), paintMode, true);
    
    checkCurrentThreadIsntInterrupted(paintMode);
    paintDimensionLines(g2D, this.home.getDimensionLines(), selectedItems, 
        selectionOutlinePaint, dimensionLinesSelectionOutlineStroke, selectionColor, 
        locationFeedbackStroke, planScale, backgroundColor, foregroundColor, paintMode, false);
    
    // Paint rooms text, furniture name and labels last to ensure they are not hidden
    checkCurrentThreadIsntInterrupted(paintMode);
    paintRoomsNameAndArea(g2D, selectedItems, planScale, foregroundColor, paintMode);

    checkCurrentThreadIsntInterrupted(paintMode);
    paintFurnitureName(g2D, this.sortedLevelFurniture, selectedItems, planScale, foregroundColor, paintMode);

    checkCurrentThreadIsntInterrupted(paintMode);
    paintLabels(g2D, this.home.getLabels(), selectedItems, selectionOutlinePaint, dimensionLinesSelectionOutlineStroke, 
        planScale, foregroundColor, paintMode);
    
    if (paintMode == PaintMode.PAINT
        && this.selectedItemsOutlinePainted) {
      paintCompassOutline(g2D, selectedItems, selectionOutlinePaint, selectionOutlineStroke, selectionColor, 
          planScale, foregroundColor);
      paintRoomsOutline(g2D, selectedItems, selectionOutlinePaint, selectionOutlineStroke, selectionColor, 
          planScale, foregroundColor);
      paintWallsOutline(g2D, selectedItems, selectionOutlinePaint, selectionOutlineStroke, selectionColor, 
          planScale, foregroundColor);
      paintFurnitureOutline(g2D, selectedItems, selectionOutlinePaint, selectionOutlineStroke, selectionColor, 
          planScale, foregroundColor);
    }
  }

  /**
   * Returns the color used to draw selection outlines. 
   */
  protected Color getSelectionColor() {
    if (OperatingSystem.isMacOSX()) {
      if (OperatingSystem.isMacOSXLeopardOrSuperior()) {
        Color selectionColor = UIManager.getColor("Focus.color");
        if (selectionColor != null) {
          return selectionColor.darker();
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
   * Returns the color used to draw furniture outline of 
   * the shape where a user can click to select a piece of furniture.
   */
  protected Color getFurnitureOutlineColor() {
    return new Color((getForeground().getRGB() & 0xFFFFFF) | 0x55000000, true);
  }
  
  /**
   * Paints rooms. 
   */
  private void paintRooms(Graphics2D g2D, List<Selectable> selectedItems, float planScale, 
                          Color foregroundColor, PaintMode paintMode) {
    if (this.sortedLevelRooms == null) {
      // Sort home rooms in floor / floor-ceiling / ceiling order
      this.sortedLevelRooms = new ArrayList<Room>();
      for (Room room : this.home.getRooms()) {
        if (isViewableAtSelectedLevel(room)) {
          this.sortedLevelRooms.add(room);
        }
      }
      Collections.sort(this.sortedLevelRooms,
          new Comparator<Room>() {
            public int compare(Room room1, Room room2) {
              if (room1.isFloorVisible() == room2.isFloorVisible()
                  && room1.isCeilingVisible() == room2.isCeilingVisible()) {
                return 0; // Keep default order if the rooms have the same visibility
              } else if (!room2.isFloorVisible()
                         || room2.isCeilingVisible()) {
                return 1;
              } else {
                return -1;
              }
            }
          });
    }
    
    Color defaultFillPaint = paintMode == PaintMode.PRINT 
        ? Color.WHITE
        : Color.GRAY;
    // Draw rooms area
    g2D.setStroke(new BasicStroke(WALL_STROKE_WIDTH / planScale));
    for (Room room : this.sortedLevelRooms) { 
      boolean selectedRoom = selectedItems.contains(room);
      // In clipboard paint mode, paint room only if it is selected
      if (paintMode != PaintMode.CLIPBOARD
          || selectedRoom) {
        g2D.setPaint(defaultFillPaint);
        if (this.preferences.isRoomFloorColoredOrTextured()
            && room.isFloorVisible()) {
          // Use room floor color or texture image
          if (room.getFloorColor() != null) {
            g2D.setPaint(new Color(room.getFloorColor()));
          } else {
            final HomeTexture floorTexture = room.getFloorTexture();
            if (floorTexture != null) {
              if (this.floorTextureImagesCache == null) {
                this.floorTextureImagesCache = new WeakHashMap<Content, BufferedImage>();
              }
              BufferedImage textureImage = this.floorTextureImagesCache.get(floorTexture.getImage());
              if (textureImage == null
                  || textureImage == WAIT_TEXTURE_IMAGE) {
                final boolean waitForTexture = paintMode != PaintMode.PAINT;
                if (!isTextureManagerAvailable()) {
                  // Prefer to share textures images with texture manager if it's available
                  TextureManager.getInstance().loadTexture(floorTexture.getImage(), waitForTexture,
                      new TextureManager.TextureObserver() {
                        public void textureUpdated(Texture texture) {
                          floorTextureImagesCache.put(floorTexture.getImage(), 
                              ((ImageComponent2D)texture.getImage(0)).getImage());
                          if (!waitForTexture) {
                            repaint();
                          }
                        }
                      });
                } else {
                  // Use icon manager if texture manager should be ignored
                  Icon textureIcon = IconManager.getInstance().getIcon(floorTexture.getImage(), 
                      waitForTexture ? null : this);
                  if (IconManager.getInstance().isWaitIcon(textureIcon)) {
                    floorTextureImagesCache.put(floorTexture.getImage(), WAIT_TEXTURE_IMAGE);                    
                  } else if (IconManager.getInstance().isErrorIcon(textureIcon)) {
                    floorTextureImagesCache.put(floorTexture.getImage(), ERROR_TEXTURE_IMAGE);                    
                  } else {
                    BufferedImage textureIconImage = new BufferedImage(
                        textureIcon.getIconWidth(), textureIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2DIcon = (Graphics2D)textureIconImage.getGraphics();
                    textureIcon.paintIcon(this, g2DIcon, 0, 0);
                    g2DIcon.dispose();
                    floorTextureImagesCache.put(floorTexture.getImage(), textureIconImage);
                  } 
                }
                textureImage = this.floorTextureImagesCache.get(floorTexture.getImage());
              }
              
              g2D.setPaint(new TexturePaint(textureImage, 
                  new Rectangle2D.Float(0, 0, floorTexture.getWidth(), floorTexture.getHeight())));
            }
          }          
        }
        
        Composite oldComposite = setTransparency(g2D, 0.75f);
        g2D.fill(getShape(room.getPoints()));
        g2D.setComposite(oldComposite);

        g2D.setPaint(foregroundColor);
        g2D.draw(getShape(room.getPoints()));
      }
    }
  }

  /**
   * Returns <code>true</code> if <code>TextureManager</code> can be used to manage textures.
   * @return
   */
  private static boolean isTextureManagerAvailable() {
    try {
      return !Boolean.getBoolean("com.eteks.sweethome3d.no3D");
    } catch (AccessControlException ex) {
      // If com.eteks.sweethome3d.no3D can't be read, 
      // security manager won't allow to access to Java 3D DLLs required by TextureManager class too 
    }
    return false;
  }
  
  /**
   * Paints rooms name and area. 
   */
  private void paintRoomsNameAndArea(Graphics2D g2D, List<Selectable> selectedItems, float planScale, 
                                     Color foregroundColor, PaintMode paintMode) {
    g2D.setPaint(foregroundColor);
    Font previousFont = g2D.getFont();
    for (Room room : this.sortedLevelRooms) { 
      boolean selectedRoom = selectedItems.contains(room);
      // In clipboard paint mode, paint room only if it is selected
      if (paintMode != PaintMode.CLIPBOARD
          || selectedRoom) {
        float xRoomCenter = room.getXCenter();
        float yRoomCenter = room.getYCenter();
        String name = room.getName();
        if (name != null) {
          name = name.trim();
          if (name.length() > 0) {
            float xName = xRoomCenter + room.getNameXOffset(); 
            float yName = yRoomCenter + room.getNameYOffset();
            TextStyle nameStyle = room.getNameStyle();
            if (nameStyle == null) {
              nameStyle = this.preferences.getDefaultTextStyle(room.getClass());
            }          
            FontMetrics nameFontMetrics = getFontMetrics(previousFont, nameStyle);
            Rectangle2D nameBounds = nameFontMetrics.getStringBounds(name, g2D);
            // Draw room name
            g2D.setFont(getFont(previousFont, nameStyle));
            g2D.drawString(name, xName - (float)nameBounds.getWidth() / 2, yName);
          }
        }
        if (room.isAreaVisible()) {
          float area = room.getArea();
          if (area > 0.01f) {
            float xArea = xRoomCenter + room.getAreaXOffset(); 
            float yArea = yRoomCenter + room.getAreaYOffset();
            TextStyle areaStyle = room.getAreaStyle();
            if (areaStyle == null) {
              areaStyle = this.preferences.getDefaultTextStyle(room.getClass());
            }          
            FontMetrics areaFontMetrics = getFontMetrics(previousFont, areaStyle);
            String areaText = this.preferences.getLengthUnit().getAreaFormatWithUnit().format(area);
            Rectangle2D areaTextBounds = areaFontMetrics.getStringBounds(areaText, g2D);
            // Draw room area 
            g2D.setFont(getFont(previousFont, areaStyle));
            g2D.drawString(areaText, xArea - (float)areaTextBounds.getWidth() / 2, yArea);
          }
        }
      }
    }
    g2D.setFont(previousFont);
  }

  /**
   * Paints the outline of rooms among <code>items</code> and indicators if 
   * <code>items</code> contains only one room and indicator paint isn't <code>null</code>. 
   */
  private void paintRoomsOutline(Graphics2D g2D, List<Selectable> items,   
                          Paint selectionOutlinePaint, Stroke selectionOutlineStroke, 
                          Paint indicatorPaint, float planScale, Color foregroundColor) {
    Collection<Room> rooms = Home.getRoomsSubList(items);
    AffineTransform previousTransform = g2D.getTransform();
    float scaleInverse = 1 / planScale;
    // Draw selection border
    for (Room room : rooms) {
      if (isViewableAtSelectedLevel(room)) {
        g2D.setPaint(selectionOutlinePaint);
        g2D.setStroke(selectionOutlineStroke);
        g2D.draw(getShape(room.getPoints()));
  
        if (indicatorPaint != null) {
          g2D.setPaint(indicatorPaint);         
          // Draw points of the room
          for (float [] point : room.getPoints()) {
            g2D.translate(point [0], point [1]);
            g2D.scale(scaleInverse, scaleInverse);
            g2D.setStroke(POINT_STROKE);
            g2D.fill(WALL_POINT);
            g2D.setTransform(previousTransform);
          }
        }
      }
    }
    
    // Draw rooms area
    g2D.setPaint(foregroundColor);
    g2D.setStroke(new BasicStroke(WALL_STROKE_WIDTH / planScale));
    for (Room room : rooms) { 
      if (isViewableAtSelectedLevel(room)) {
        g2D.draw(getShape(room.getPoints()));
      }
    }

    // Paint resize indicators of the room if indicator paint exists
    if (items.size() == 1 
        && rooms.size() == 1 
        && indicatorPaint != null) {
      Room selectedRoom = rooms.iterator().next();
      if (isViewableAtSelectedLevel(selectedRoom)) {
        g2D.setPaint(indicatorPaint);         
        paintRoomResizeIndicators(g2D, selectedRoom, indicatorPaint, planScale);
        paintRoomNameOffsetIndicator(g2D, selectedRoom, indicatorPaint, planScale);
        paintRoomAreaOffsetIndicator(g2D, selectedRoom, indicatorPaint, planScale);
      }
    }
  }

  /**
   * Paints resize indicators on <code>room</code>.
   */
  private void paintRoomResizeIndicators(Graphics2D g2D, Room room,
                                         Paint indicatorPaint, 
                                         float planScale) {
    if (this.resizeIndicatorVisible) {
      g2D.setPaint(indicatorPaint);
      g2D.setStroke(INDICATOR_STROKE);
      AffineTransform previousTransform = g2D.getTransform();
      float scaleInverse = 1 / planScale;
      float [][] points = room.getPoints();
      for (int i = 0; i < points.length; i++) {
        // Draw resize indicator at room point
        float [] point = points [i];
        g2D.translate(point[0], point[1]);
        g2D.scale(scaleInverse, scaleInverse);
        float [] previousPoint = i == 0
            ? points [points.length - 1]
            : points [i -1];
        float [] nextPoint = i == points.length - 1
            ? points [0]
            : points [i + 1];
        // Compute the angle of the mean normalized normal at point i
        float distance1 = (float)Point2D.distance(
            previousPoint [0], previousPoint [1], point [0], point [1]);
        float xNormal1 = (point [1] - previousPoint [1]) / distance1;
        float yNormal1 = (previousPoint [0] - point [0]) / distance1;
        float distance2 = (float)Point2D.distance(
            nextPoint [0], nextPoint [1], point [0], point [1]);
        float xNormal2 = (nextPoint [1] - point [1]) / distance2;
        float yNormal2 = (point [0] - nextPoint [0]) / distance2;
        double angle = Math.atan2(yNormal1 + yNormal2, xNormal1 + xNormal2);         
        // Ensure the indicator will be drawn outside of room 
        if (room.containsPoint(point [0] + (float)Math.cos(angle), 
              point [1] + (float)Math.sin(angle), 0.001f)) {
          angle += Math.PI;
        }        
        g2D.rotate(angle);
        g2D.draw(WALL_AND_LINE_RESIZE_INDICATOR);
        g2D.setTransform(previousTransform);
      }
    }
  }
  
  /**
   * Paints name indicator on <code>room</code>.
   */
  private void paintRoomNameOffsetIndicator(Graphics2D g2D, Room room,
                                            Paint indicatorPaint, 
                                            float planScale) {
    if (this.resizeIndicatorVisible
        && room.getName() != null
        && room.getName().trim().length() > 0) {
      float xName = room.getXCenter() + room.getNameXOffset(); 
      float yName = room.getYCenter() + room.getNameYOffset();
      paintTextLocationIndicator(g2D, xName, yName,
          indicatorPaint, planScale);
    }
  }

  /**
   * Paints resize indicator on <code>room</code>.
   */
  private void paintRoomAreaOffsetIndicator(Graphics2D g2D, Room room,
                                            Paint indicatorPaint, 
                                            float planScale) {
    if (this.resizeIndicatorVisible
        && room.isAreaVisible()
        && room.getArea() > 0.01f) {
      float xArea = room.getXCenter() + room.getAreaXOffset(); 
      float yArea = room.getYCenter() + room.getAreaYOffset();
      paintTextLocationIndicator(g2D, xArea, yArea, indicatorPaint, planScale);
    }
  }

  /**
   * Paints text location indicator at the given coordinates.
   */
  private void paintTextLocationIndicator(Graphics2D g2D, float x, float y,
                                          Paint indicatorPaint, float planScale) {
    g2D.setPaint(indicatorPaint);
    g2D.setStroke(INDICATOR_STROKE);
    AffineTransform previousTransform = g2D.getTransform();
    float scaleInverse = 1 / planScale;
    g2D.translate(x, y);
    g2D.scale(scaleInverse, scaleInverse);
    g2D.draw(TEXT_LOCATION_INDICATOR);
    g2D.setTransform(previousTransform);
  }

  /**
   * Paints walls. 
   */
  private void paintWalls(Graphics2D g2D, List<Selectable> selectedItems, float planScale, 
                          Color backgroundColor, Color foregroundColor, PaintMode paintMode) {
    Collection<Wall> paintedWalls;
    Map<Collection<Wall>, Area> wallAreas;
    if (paintMode != PaintMode.CLIPBOARD) {
      wallAreas = getWallAreas();
    } else {
      // In clipboard paint mode, paint only selected walls
      paintedWalls = Home.getWallsSubList(selectedItems);
      wallAreas = getWallAreas(getDrawableWallsInSelectedLevel(paintedWalls));
    }
    float wallPaintScale = paintMode == PaintMode.PRINT 
        ? planScale / 72 * 150 // Adjust scale to 150 dpi for print
        : planScale;
    for (Map.Entry<Collection<Wall>, Area> areaEntry : wallAreas.entrySet()) {
      TextureImage wallPattern = areaEntry.getKey().iterator().next().getPattern();
      fillAndDrawArea(g2D, areaEntry.getValue(), planScale, 
          getWallPaint(wallPaintScale, backgroundColor, foregroundColor, 
              wallPattern != null ? wallPattern : this.preferences.getWallPattern()), foregroundColor);
    }
  }

  /**
   * Fills and paints the given area.
   */
  private void fillAndDrawArea( Graphics2D g2D, Area area, float planScale, Paint fillPaint, Paint drawPaint) {
    // Fill walls area
    g2D.setPaint(fillPaint);
    g2D.fill(area);
    // Draw walls area
    g2D.setPaint(drawPaint);
    g2D.setStroke(new BasicStroke(WALL_STROKE_WIDTH / planScale));
    g2D.draw(area);
  }
  
  /**
   * Paints the outline of walls among <code>items</code> and a resize indicator if 
   * <code>items</code> contains only one wall and indicator paint isn't <code>null</code>. 
   */
  private void paintWallsOutline(Graphics2D g2D, List<Selectable> items,   
                                 Paint selectionOutlinePaint, Stroke selectionOutlineStroke, 
                                 Paint indicatorPaint, float planScale, Color foregroundColor) {
    float scaleInverse = 1 / planScale;
    Collection<Wall> walls = Home.getWallsSubList(items);
    AffineTransform previousTransform = g2D.getTransform();
    for (Wall wall : walls) {
      if (isViewableAtSelectedLevel(wall)) {
        // Draw selection border
        g2D.setPaint(selectionOutlinePaint);
        g2D.setStroke(selectionOutlineStroke);
        g2D.draw(getShape(wall.getPoints()));
        
        if (indicatorPaint != null) {
          // Draw start point of the wall
          g2D.translate(wall.getXStart(), wall.getYStart());
          g2D.scale(scaleInverse, scaleInverse);
          g2D.setPaint(indicatorPaint);         
          g2D.setStroke(POINT_STROKE);
          g2D.fill(WALL_POINT);
        
          Float arcExtent = wall.getArcExtent();
          double indicatorAngle;
          double distanceAtScale;
          float xArcCircleCenter = 0;
          float yArcCircleCenter = 0;
          double arcCircleRadius = 0;
          double startPointToEndPointDistance = wall.getStartPointToEndPointDistance();
          double wallAngle = Math.atan2(wall.getYEnd() - wall.getYStart(), 
              wall.getXEnd() - wall.getXStart());
          if (arcExtent != null) {
            xArcCircleCenter = wall.getXArcCircleCenter();
            yArcCircleCenter = wall.getYArcCircleCenter();
            arcCircleRadius = Point2D.distance(wall.getXStart(), wall.getYStart(), 
                xArcCircleCenter, yArcCircleCenter);
            distanceAtScale = arcCircleRadius * Math.abs(arcExtent) * planScale;
            indicatorAngle = Math.atan2(yArcCircleCenter - wall.getYStart(), 
                    xArcCircleCenter - wall.getXStart())  
                + (arcExtent > 0 ? -Math.PI / 2 : Math.PI /2);
          } else {
            distanceAtScale = startPointToEndPointDistance * planScale;
            indicatorAngle = wallAngle;
          }
          // If the distance between start and end points is < 30
          if (distanceAtScale < 30) { 
            // Draw only one orientation indicator between the two points
            g2D.rotate(wallAngle);
            if (arcExtent != null) {
              double wallToStartPointArcCircleCenterAngle = Math.abs(arcExtent) > Math.PI 
                  ? -(Math.PI + arcExtent) / 2
                  : (Math.PI - arcExtent) / 2;
              float arcCircleCenterToWallDistance = (float)(Math.tan(wallToStartPointArcCircleCenterAngle) 
                  * startPointToEndPointDistance / 2); 
              g2D.translate(startPointToEndPointDistance * planScale / 2, 
                  (arcCircleCenterToWallDistance - arcCircleRadius * (Math.abs(wallAngle) > Math.PI / 2 ? -1: 1)) * planScale);
            } else {
              g2D.translate(distanceAtScale / 2, 0);
            }
          } else {
            // Draw orientation indicator at start of the wall
            g2D.rotate(indicatorAngle);
            g2D.translate(8, 0);
          }
          g2D.draw(WALL_ORIENTATION_INDICATOR);
          g2D.setTransform(previousTransform);
          
          // Draw end point of the wall
          g2D.translate(wall.getXEnd(), wall.getYEnd());
          g2D.scale(scaleInverse, scaleInverse);
          g2D.fill(WALL_POINT);
          if (distanceAtScale >= 30) { 
            if (arcExtent != null) {
              indicatorAngle += arcExtent;
            } 
            // Draw orientation indicator at end of the wall
            g2D.rotate(indicatorAngle);
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
    for (Area area : getWallAreas(walls).values()) {
      g2D.draw(area);
    }
    
    // Paint resize indicator of the wall if indicator paint exists
    if (items.size() == 1 
        && walls.size() == 1 
        && indicatorPaint != null) {
      Wall wall = walls.iterator().next();
      if (isViewableAtSelectedLevel(wall)) {
        paintWallResizeIndicator(g2D, wall, indicatorPaint, planScale);
      }
    }
  }

  /**
   * Returns <code>true</code> if the given item can be viewed in the plan at the selected level. 
   */
  private boolean isViewableAtSelectedLevel(Elevatable item) {
    Level selectedLevel = this.home.getSelectedLevel();
    return item.getLevel() == null
        || item.getLevel() == selectedLevel
        || item.isAtLevel(selectedLevel);
  }

  /**
   * Paints resize indicator on <code>wall</code>.
   */
  private void paintWallResizeIndicator(Graphics2D g2D, Wall wall,
                                        Paint indicatorPaint, 
                                        float planScale) {
    if (this.resizeIndicatorVisible) {
      g2D.setPaint(indicatorPaint);
      g2D.setStroke(INDICATOR_STROKE);
      
      Float arcExtent = wall.getArcExtent();
      double indicatorAngle;
      if (arcExtent != null) {
        indicatorAngle = Math.atan2(wall.getYArcCircleCenter() - wall.getYEnd(), 
                wall.getXArcCircleCenter() - wall.getXEnd())  
            + (arcExtent > 0 ? -Math.PI / 2 : Math.PI /2);
      } else {
        indicatorAngle = Math.atan2(wall.getYEnd() - wall.getYStart(), 
            wall.getXEnd() - wall.getXStart());
      }
      
      AffineTransform previousTransform = g2D.getTransform();
      float scaleInverse = 1 / planScale;
      // Draw resize indicator at wall end point
      g2D.translate(wall.getXEnd(), wall.getYEnd());
      g2D.scale(scaleInverse, scaleInverse);
      g2D.rotate(indicatorAngle);
      g2D.draw(WALL_AND_LINE_RESIZE_INDICATOR);
      g2D.setTransform(previousTransform);

      if (arcExtent != null) {
        indicatorAngle += Math.PI - arcExtent;
      } else {
        indicatorAngle += Math.PI;
      }
      
      // Draw resize indicator at wall start point
      g2D.translate(wall.getXStart(), wall.getYStart());
      g2D.scale(scaleInverse, scaleInverse);
      g2D.rotate(indicatorAngle);
      g2D.draw(WALL_AND_LINE_RESIZE_INDICATOR);
      g2D.setTransform(previousTransform);
    }
  }
  
  /**
   * Returns areas matching the union of home wall shapes sorted by pattern. 
   */
  private Map<Collection<Wall>, Area> getWallAreas() {
    if (this.wallAreasCache == null) {
      this.wallAreasCache = getWallAreas(getDrawableWallsInSelectedLevel(this.home.getWalls()));
    }
    return this.wallAreasCache;
  }

  /**
   * Returns the walls that belong to the selected level in home.
   */
  private Collection<Wall> getDrawableWallsInSelectedLevel(Collection<Wall> walls) {
    List<Wall> wallsInSelectedLevel = new ArrayList<Wall>();
    for (Wall wall : walls) {
      if (isViewableAtSelectedLevel(wall)) {
        wallsInSelectedLevel.add(wall);
      }
    }
    return wallsInSelectedLevel;
  }
  
  /**
   * Returns areas matching the union of <code>walls</code> shapes sorted by pattern. 
   */
  private Map<Collection<Wall>, Area> getWallAreas(Collection<Wall> walls) {
    if (walls.size() == 0) {
      return Collections.emptyMap();
    }
    // Check if all walls use the same pattern
    TextureImage pattern = walls.iterator().next().getPattern();
    boolean samePattern = true;
    for (Wall wall : walls) {
      if (pattern != wall.getPattern()) {
        samePattern = false;
        break;
      }
    }
    Map<Collection<Wall>, Area> wallAreas = new LinkedHashMap<Collection<Wall>, Area>();
    if (samePattern) {
      wallAreas.put(walls, getItemsArea(walls));
    } else {
      // Create walls sublists by pattern
      Map<TextureImage, Collection<Wall>> sortedWalls = new LinkedHashMap<TextureImage, Collection<Wall>>();
      for (Wall wall : walls) {
        TextureImage wallPattern = wall.getPattern();
        if (wallPattern == null) {
          wallPattern = this.preferences.getWallPattern();
        }
        Collection<Wall> patternWalls = sortedWalls.get(wallPattern);
        if (patternWalls == null) {
          patternWalls = new ArrayList<Wall>();
          sortedWalls.put(wallPattern, patternWalls);
        }
        patternWalls.add(wall);
      }
      for (Collection<Wall> patternWalls : sortedWalls.values()) {
        wallAreas.put(patternWalls, getItemsArea(patternWalls));
      }
    }
    return wallAreas;
  }
  
  /**
   * Returns an area matching the union of all <code>items</code> shapes. 
   */
  private Area getItemsArea(Collection<? extends Selectable> items) {
    Area itemsArea = new Area();
    for (Selectable item : items) {
      itemsArea.add(new Area(getShape(item.getPoints())));
    }
    return itemsArea;
  }

  /**
   * Returns the <code>Paint</code> object used to fill walls.
   */
  private Paint getWallPaint(float planScale, Color backgroundColor, Color foregroundColor, TextureImage wallPattern) {
    BufferedImage patternImage = this.patternImagesCache.get(wallPattern);
    if (patternImage == null
        || !backgroundColor.equals(this.wallsPatternBackgroundCache)
        || !foregroundColor.equals(this.wallsPatternForegroundCache)) {
      patternImage = SwingTools.getPatternImage(wallPattern, backgroundColor, foregroundColor);
      this.patternImagesCache.put(wallPattern, patternImage);
      this.wallsPatternBackgroundCache = backgroundColor;
      this.wallsPatternForegroundCache = foregroundColor;
    }
    return new TexturePaint(patternImage, 
        new Rectangle2D.Float(0, 0, 10 / planScale, 10 / planScale));
  }
  
  /**
   * Paints home furniture.
   */
  private void paintFurniture(Graphics2D g2D, List<HomePieceOfFurniture> furniture, 
                              List<? extends Selectable> selectedItems, float planScale, 
                              Color backgroundColor, Color foregroundColor, 
                              Color furnitureOutlineColor,
                              PaintMode paintMode, boolean paintIcon) {    
    if (!furniture.isEmpty()) {
      BasicStroke pieceBorderStroke = new BasicStroke(BORDER_STROKE_WIDTH / planScale);
      Boolean allFurnitureViewedFromTop = null;
      // Draw furniture
      for (HomePieceOfFurniture piece : furniture) {
        if (piece.isVisible()) {
          boolean selectedPiece = selectedItems.contains(piece);
          if (piece instanceof HomeFurnitureGroup) {
            List<HomePieceOfFurniture> groupFurniture = ((HomeFurnitureGroup)piece).getFurniture();
            List<Selectable> emptyList = Collections.emptyList();
            paintFurniture(g2D, groupFurniture,  
                selectedPiece 
                    ? groupFurniture 
                    : emptyList, 
                planScale, backgroundColor, foregroundColor, 
                furnitureOutlineColor, paintMode, paintIcon);
          } else if (paintMode != PaintMode.CLIPBOARD
                    || selectedPiece) {
            // In clipboard paint mode, paint piece only if it is selected
            Shape pieceShape = getShape(piece.getPoints());
            Shape pieceShape2D;
            if (piece instanceof HomeDoorOrWindow) {
              HomeDoorOrWindow doorOrWindow = (HomeDoorOrWindow)piece;
              pieceShape2D = getDoorOrWindowShapeAtWallIntersection(doorOrWindow);
              paintDoorOrWindowSashes(g2D, doorOrWindow, planScale, foregroundColor);
            } else {
              pieceShape2D = pieceShape;
            }

            boolean viewedFromTop;
            if (this.preferences.isFurnitureViewedFromTop()) {
              if (piece.getPlanIcon() != null
                  || piece instanceof HomeDoorOrWindow) {
                viewedFromTop = true;
              } else {
                if (allFurnitureViewedFromTop == null) {
                  try {
                    // Evaluate allFurnitureViewedFromTop value as late as possible to avoid mandatory dependency towards Java 3D 
                    allFurnitureViewedFromTop = !Boolean.getBoolean("com.eteks.sweethome3d.no3D") 
                        && Component3DManager.getInstance().isOffScreenImageSupported();
                  } catch (AccessControlException ex) {
                    // If com.eteks.sweethome3d.no3D property can't be read, 
                    // security manager won't allow to access to Java 3D DLLs required by PieceOfFurnitureModelIcon class too
                    allFurnitureViewedFromTop = false;
                  }
                }
                viewedFromTop = allFurnitureViewedFromTop.booleanValue();
              }                
            } else {
              viewedFromTop = false;
            }
            if (paintIcon 
                && viewedFromTop) {
              if (piece instanceof HomeDoorOrWindow) {
                // Draw doors and windows border
                g2D.setPaint(backgroundColor);
                g2D.fill(pieceShape2D);
                g2D.setPaint(foregroundColor);
                g2D.setStroke(pieceBorderStroke);
                g2D.draw(pieceShape2D);
              } else {
                paintPieceOfFurnitureTop(g2D, piece, pieceShape2D, pieceBorderStroke, planScale, 
                    backgroundColor, foregroundColor, paintMode);
              }
              if (paintMode == PaintMode.PAINT) {
                // Draw selection outline rectangle  
                g2D.setStroke(pieceBorderStroke);
                g2D.setPaint(furnitureOutlineColor);
                g2D.draw(pieceShape);
              } 
            } else {
              if (paintIcon) {
                // Draw its icon
                paintPieceOfFurnitureIcon(g2D, piece, pieceShape2D, planScale, 
                    backgroundColor, paintMode);
              }
              // Draw its border
              g2D.setPaint(foregroundColor);
              g2D.setStroke(pieceBorderStroke);
              g2D.draw(pieceShape2D);
              if (piece instanceof HomeDoorOrWindow
                  && paintMode == PaintMode.PAINT) {
                // Draw outline rectangle
                g2D.setPaint(furnitureOutlineColor);
                g2D.draw(pieceShape);
              } 
            }
          }
        }
      }
    }
  }

  /**
   * Returns the shape of a door or a window at wall intersection.
   */
  private Shape getDoorOrWindowShapeAtWallIntersection(HomeDoorOrWindow doorOrWindow) {
    // For doors and windows, compute rectangle at wall intersection 
    float wallThickness = doorOrWindow.getDepth() * doorOrWindow.getWallThickness(); 
    float wallDistance  = doorOrWindow.getDepth() * doorOrWindow.getWallDistance(); 
    Rectangle2D doorOrWindowRectangle = new Rectangle2D.Float(
        doorOrWindow.getX() - doorOrWindow.getWidth() / 2,
        doorOrWindow.getY() - doorOrWindow.getDepth() / 2 + wallDistance,
        doorOrWindow.getWidth(), wallThickness);
    // Apply rotation to the rectangle
    AffineTransform rotation = new AffineTransform();
    rotation.setToRotation(doorOrWindow.getAngle(), doorOrWindow.getX(), doorOrWindow.getY());
    PathIterator it = doorOrWindowRectangle.getPathIterator(rotation);
    GeneralPath doorOrWindowShape = new GeneralPath();
    doorOrWindowShape.append(it, false);
    return doorOrWindowShape;
  }

  /**
   * Paints the sashes of a door or a window.
   */
  private void paintDoorOrWindowSashes(Graphics2D g2D, HomeDoorOrWindow doorOrWindow, float planScale, 
                                       Color foregroundColor) {
    BasicStroke sashBorderStroke = new BasicStroke(BORDER_STROKE_WIDTH / planScale);
    g2D.setPaint(foregroundColor);
    g2D.setStroke(sashBorderStroke);
    for (Sash sash : doorOrWindow.getSashes()) {
      g2D.draw(getDoorOrWindowSashShape(doorOrWindow, sash));
    }  
  }

  /**
   * Returns the shape of a sash of a door or a window. 
   */
  private GeneralPath getDoorOrWindowSashShape(HomeDoorOrWindow doorOrWindow, 
                                               Sash sash) {
    float modelMirroredSign = doorOrWindow.isModelMirrored() ? -1 : 1;
    float xAxis = modelMirroredSign * sash.getXAxis() * doorOrWindow.getWidth();
    float yAxis = sash.getYAxis() * doorOrWindow.getDepth();
    float sashWidth = sash.getWidth() * doorOrWindow.getWidth();
    float startAngle = (float)Math.toDegrees(sash.getStartAngle());
    if (doorOrWindow.isModelMirrored()) {
      startAngle = 180 - startAngle;
    }
    float extentAngle = modelMirroredSign * (float)Math.toDegrees(sash.getEndAngle() - sash.getStartAngle());
    
    Arc2D arc = new Arc2D.Float(xAxis - sashWidth, yAxis - sashWidth, 
        2 * sashWidth, 2 * sashWidth, 
        startAngle, extentAngle, Arc2D.PIE);
    AffineTransform transformation = new AffineTransform();
    transformation.translate(doorOrWindow.getX(), doorOrWindow.getY());
    transformation.rotate(doorOrWindow.getAngle());
    transformation.translate(modelMirroredSign * -doorOrWindow.getWidth() / 2, -doorOrWindow.getDepth() / 2);
    PathIterator it = arc.getPathIterator(transformation);
    GeneralPath sashShape = new GeneralPath();
    sashShape.append(it, false);
    return sashShape;
  }

  /**
   * Paints home furniture visible name.
   */
  private void paintFurnitureName(Graphics2D g2D, List<HomePieceOfFurniture> furniture,
                                  List<? extends Selectable> selectedItems, float planScale, 
                                  Color foregroundColor, PaintMode paintMode) {
    Font previousFont = g2D.getFont();
    g2D.setPaint(foregroundColor);
    // Draw furniture name
    for (HomePieceOfFurniture piece : furniture) {
      if (piece.isVisible()) {
        boolean selectedPiece = selectedItems.contains(piece);
        if (piece instanceof HomeFurnitureGroup) {
          List<HomePieceOfFurniture> groupFurniture = ((HomeFurnitureGroup)piece).getFurniture();
          List<Selectable> emptyList = Collections.emptyList();
          paintFurnitureName(g2D, groupFurniture, 
               selectedPiece 
                   ? groupFurniture 
                   : emptyList, 
               planScale, foregroundColor, paintMode);
        } 
        if (piece.isNameVisible()
            && (paintMode != PaintMode.CLIPBOARD
                || selectedPiece)) {
          // In clipboard paint mode, paint piece only if it is selected
          String name = piece.getName().trim();
          if (name.length() > 0) {
            float xName = piece.getX() + piece.getNameXOffset(); 
            float yName = piece.getY() + piece.getNameYOffset();
            TextStyle nameStyle = piece.getNameStyle();
            if (nameStyle == null) {
              nameStyle = this.preferences.getDefaultTextStyle(piece.getClass());
            }          
            FontMetrics nameFontMetrics = getFontMetrics(previousFont, nameStyle);
            Rectangle2D nameBounds = nameFontMetrics.getStringBounds(name, g2D);
            // Draw piece name
            g2D.setFont(getFont(previousFont, nameStyle));
            g2D.drawString(name, xName - (float)nameBounds.getWidth() / 2, yName);
          }
        }
      }
    }
    g2D.setFont(previousFont);
  }

  /**
   * Paints the outline of furniture among <code>items</code> and indicators if 
   * <code>items</code> contains only one piece and indicator paint isn't <code>null</code>. 
   */
  private void paintFurnitureOutline(Graphics2D g2D, List<Selectable> items,  
                                     Paint selectionOutlinePaint, Stroke selectionOutlineStroke, 
                                     Paint indicatorPaint, float planScale, 
                                     Color foregroundColor) {
    BasicStroke pieceBorderStroke = new BasicStroke(BORDER_STROKE_WIDTH / planScale);
    BasicStroke pieceFrontBorderStroke = new BasicStroke(4 * BORDER_STROKE_WIDTH / planScale, 
        BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
    for (HomePieceOfFurniture piece : Home.getFurnitureSubList(items)) {
      if (piece.isVisible() 
          && isViewableAtSelectedLevel(piece)) {
        float [][] points = piece.getPoints();
        Shape pieceShape = getShape(points);
        
        // Draw selection border
        g2D.setPaint(selectionOutlinePaint);
        g2D.setStroke(selectionOutlineStroke);
        g2D.draw(pieceShape);

        // Draw its border
        g2D.setPaint(foregroundColor);
        g2D.setStroke(pieceBorderStroke);
        g2D.draw(pieceShape);
        
        // Draw its front face with a thicker line
        g2D.setStroke(pieceFrontBorderStroke);
        g2D.draw(new Line2D.Float(points [2][0], points [2][1], points [3][0], points [3][1]));
        
        if (items.size() == 1 && indicatorPaint != null) {
          paintPieceOFFurnitureIndicators(g2D, piece, indicatorPaint, planScale);
        }
      }
    }
  }

  /**
   * Paints <code>piece</code> icon with <code>g2D</code>.
   */
  private void paintPieceOfFurnitureIcon(Graphics2D g2D, HomePieceOfFurniture piece, 
                                         Shape pieceShape2D, float planScale, 
                                         Color backgroundColor, PaintMode paintMode) {
    // Get piece icon
    Icon icon = IconManager.getInstance().getIcon(piece.getIcon(), 128, 
        paintMode == PaintMode.PAINT ? this : null);
    paintPieceOfFurnitureIcon(g2D, piece, icon, pieceShape2D, planScale, backgroundColor);
  }

  /**
   * Paints <code>icon</code> with <code>g2D</code>.
   */
  private void paintPieceOfFurnitureIcon(Graphics2D g2D, HomePieceOfFurniture piece, Icon icon,
                                         Shape pieceShape2D, float planScale, Color backgroundColor) {
    // Fill piece area
    g2D.setPaint(backgroundColor);
    g2D.fill(pieceShape2D);
    
    Shape previousClip = g2D.getClip();
    // Clip icon drawing into piece shape
    g2D.clip(pieceShape2D);
    AffineTransform previousTransform = g2D.getTransform();
    // Translate to piece center
    final Rectangle bounds = pieceShape2D.getBounds();
    g2D.translate(bounds.getCenterX(), bounds.getCenterY());
    float pieceDepth = piece.getDepth();
    if (piece instanceof HomeDoorOrWindow) {
      pieceDepth *= ((HomeDoorOrWindow)piece).getWallThickness(); 
    } 
    // Scale icon to fit in its area
    float minDimension = Math.min(piece.getWidth(), pieceDepth);
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
   * Paints <code>piece</code> top icon with <code>g2D</code>.
   */
  private void paintPieceOfFurnitureTop(Graphics2D g2D, HomePieceOfFurniture piece, 
                                        Shape pieceShape2D, BasicStroke pieceBorderStroke, 
                                        float planScale, 
                                        Color backgroundColor, Color foregroundColor, 
                                        PaintMode paintMode) {
    if (this.furnitureTopViewIconsCache == null) {
      this.furnitureTopViewIconsCache = new WeakHashMap<HomePieceOfFurniture, PieceOfFurnitureTopViewIcon>();
    }
    PieceOfFurnitureTopViewIcon icon = this.furnitureTopViewIconsCache.get(piece);
    if (icon == null
        || icon.isWaitIcon()
           && paintMode != PaintMode.PAINT) {
      PlanComponent waitingComponent = paintMode == PaintMode.PAINT ? this : null;
      // Prefer use plan icon if it exists
      if (piece.getPlanIcon() != null) {
        icon = new PieceOfFurniturePlanIcon(piece, waitingComponent);
      } else {
        icon = new PieceOfFurnitureModelIcon(piece, waitingComponent);
      }
      this.furnitureTopViewIconsCache.put(piece, icon);
    }
    
    if (icon.isWaitIcon() || icon.isErrorIcon()) {
      paintPieceOfFurnitureIcon(g2D, piece, icon, pieceShape2D, planScale, backgroundColor);
      g2D.setPaint(foregroundColor);
      g2D.setStroke(pieceBorderStroke);
      g2D.draw(pieceShape2D);
    } else {        
      AffineTransform previousTransform = g2D.getTransform();  
      // Translate to piece center
      final Rectangle bounds = pieceShape2D.getBounds();
      g2D.translate(bounds.getCenterX(), bounds.getCenterY());
      g2D.rotate(piece.getAngle());
      float pieceDepth = piece.getDepth();
      // Scale icon to fit in its area
      if (piece.isModelMirrored()) {
        // If piece model is mirrored, inverse x scale
        g2D.scale(-piece.getWidth() / icon.getIconWidth(), pieceDepth / icon.getIconHeight());
      } else {
        g2D.scale(piece.getWidth() / icon.getIconWidth(), pieceDepth / icon.getIconHeight());
      }
      // Paint piece icon
      icon.paintIcon(this, g2D, -icon.getIconWidth() / 2, -icon.getIconHeight() / 2);
      // Revert g2D transformation to previous value
      g2D.setTransform(previousTransform);
    }
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
      g2D.setStroke(INDICATOR_STROKE);
      
      AffineTransform previousTransform = g2D.getTransform();
      // Draw rotation indicator at top left point of the piece
      float [][] piecePoints = piece.getPoints();
      float scaleInverse = 1 / planScale;
      float pieceAngle = piece.getAngle();
      g2D.translate(piecePoints [0][0], piecePoints [0][1]);
      g2D.scale(scaleInverse, scaleInverse);
      g2D.rotate(pieceAngle);
      g2D.draw(FURNITURE_ROTATION_INDICATOR);
      g2D.setTransform(previousTransform);

      // Draw elevation indicator at top right point of the piece
      g2D.translate(piecePoints [1][0], piecePoints [1][1]);
      g2D.scale(scaleInverse, scaleInverse);
      g2D.rotate(pieceAngle);
      g2D.draw(FURNITURE_ELEVATION_POINT_INDICATOR);
      // Place elevation indicator farther but don't rotate it
      g2D.translate(6.5f, -6.5f);
      g2D.rotate(-pieceAngle); 
      g2D.draw(FURNITURE_ELEVATION_INDICATOR);
      g2D.setTransform(previousTransform);
      
      if (piece.isResizable()) {
        // Draw height indicator at bottom left point of the piece
        g2D.translate(piecePoints [3][0], piecePoints [3][1]);
        g2D.scale(scaleInverse, scaleInverse);
        g2D.rotate(pieceAngle);
        
        if (piece instanceof HomeLight) {
          g2D.draw(LIGHT_POWER_POINT_INDICATOR);
          // Place power indicator farther but don't rotate it
          g2D.translate(-7.5f, 7.5f);
          g2D.rotate(-pieceAngle);
          g2D.draw(LIGHT_POWER_INDICATOR);
        } else {
          g2D.draw(FURNITURE_HEIGHT_POINT_INDICATOR);
          // Place height indicator farther but don't rotate it
          g2D.translate(-7.5f, 7.5f);
          g2D.rotate(-pieceAngle);
          g2D.draw(FURNITURE_HEIGHT_INDICATOR);
        }
        g2D.setTransform(previousTransform);
        
        // Draw resize indicator at top left point of the piece
        g2D.translate(piecePoints [2][0], piecePoints [2][1]);
        g2D.scale(scaleInverse, scaleInverse);
        g2D.rotate(pieceAngle);
        g2D.draw(FURNITURE_RESIZE_INDICATOR);
        g2D.setTransform(previousTransform);
      }
      
      if (piece.isNameVisible()
          && piece.getName().trim().length() > 0) {
        float xName = piece.getX() + piece.getNameXOffset(); 
        float yName = piece.getY() + piece.getNameYOffset();
        paintTextLocationIndicator(g2D, xName, yName,
            indicatorPaint, planScale);
      }
    }
  }
  
  /**
   * Paints dimension lines. 
   */
  private void paintDimensionLines(Graphics2D g2D, 
                          Collection<DimensionLine> dimensionLines, List<Selectable> selectedItems,   
                          Paint selectionOutlinePaint, Stroke selectionOutlineStroke, 
                          Paint indicatorPaint, Stroke extensionLineStroke, float planScale, 
                          Color backgroundColor, Color foregroundColor, 
                          PaintMode paintMode, boolean feedback) {
    // In clipboard paint mode, paint only selected dimension lines
    if (paintMode == PaintMode.CLIPBOARD) {
      dimensionLines = Home.getDimensionLinesSubList(selectedItems);
    }

    // Draw dimension lines
    g2D.setPaint(foregroundColor);
    BasicStroke dimensionLineStroke = new BasicStroke(BORDER_STROKE_WIDTH / planScale);
    // Change font size
    Font previousFont = g2D.getFont();
    for (DimensionLine dimensionLine : dimensionLines) {
      if (isViewableAtSelectedLevel(dimensionLine)) {
        AffineTransform previousTransform = g2D.getTransform();
        double angle = Math.atan2(dimensionLine.getYEnd() - dimensionLine.getYStart(), 
            dimensionLine.getXEnd() - dimensionLine.getXStart());
        float dimensionLineLength = dimensionLine.getLength();
        g2D.translate(dimensionLine.getXStart(), dimensionLine.getYStart());
        g2D.rotate(angle);
        g2D.translate(0, dimensionLine.getOffset());
        
        if (paintMode == PaintMode.PAINT
            && this.selectedItemsOutlinePainted
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
        
        float displayedValue = feedback
            ? this.preferences.getLengthUnit().getMagnetizedLength(dimensionLineLength, getPixelLength())
            : dimensionLineLength;
        String lengthText = this.preferences.getLengthUnit().getFormat().format(displayedValue);
        TextStyle lengthStyle = dimensionLine.getLengthStyle();
        if (lengthStyle == null) {
          lengthStyle = this.preferences.getDefaultTextStyle(dimensionLine.getClass());
        }          
        if (feedback) {
          // Use default for feedback
          lengthStyle = lengthStyle.deriveStyle(getFont().getSize() / getScale());
        }
        Font font = getFont(previousFont, lengthStyle);
        FontMetrics lengthFontMetrics = getFontMetrics(font, lengthStyle);
        Rectangle2D lengthTextBounds = lengthFontMetrics.getStringBounds(lengthText, g2D);
        int fontAscent = lengthFontMetrics.getAscent();
        g2D.translate((dimensionLineLength - (float)lengthTextBounds.getWidth()) / 2, 
            dimensionLine.getOffset() <= 0 
                ? -lengthFontMetrics.getDescent() - 1
                : fontAscent + 1);
        if (feedback) {
          // Draw text outline with half transparent background color
          g2D.setPaint(backgroundColor);
          Composite oldComposite = setTransparency(g2D, 0.7f);
          g2D.setStroke(new BasicStroke(3 / planScale));
          FontRenderContext fontRenderContext = g2D.getFontRenderContext();
          TextLayout textLayout = new TextLayout(lengthText, font, fontRenderContext);
          g2D.draw(textLayout.getOutline(new AffineTransform()));
          g2D.setComposite(oldComposite);
          g2D.setPaint(foregroundColor);
        }
        // Draw dimension length in middle
        g2D.setFont(font);
        g2D.drawString(lengthText, 0, 0);
        
        g2D.setTransform(previousTransform);
      }
    }
    g2D.setFont(previousFont);
    // Paint resize indicator of selected dimension line
    if (selectedItems.size() == 1 
        && selectedItems.get(0) instanceof DimensionLine
        && paintMode == PaintMode.PAINT
        && indicatorPaint != null) {
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
      g2D.setStroke(INDICATOR_STROKE);

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
   * Paints home labels.
   */
  private void paintLabels(Graphics2D g2D, Collection<Label> labels, List<Selectable> selectedItems, 
                           Paint selectionOutlinePaint, Stroke selectionOutlineStroke, 
                           float planScale, Color foregroundColor, PaintMode paintMode) {
    Font previousFont = g2D.getFont();
    g2D.setPaint(foregroundColor);
    // Draw labels
    for (Label label : labels) {
      if (isViewableAtSelectedLevel(label)) {
        boolean selectedLabel = selectedItems.contains(label);
        // In clipboard paint mode, paint label only if it is selected
        if (paintMode != PaintMode.CLIPBOARD || selectedLabel) {
          String labelText = label.getText();
          float xLabel = label.getX();
          float yLabel = label.getY();
          TextStyle labelStyle = label.getStyle();
          if (labelStyle == null) {
            labelStyle = this.preferences.getDefaultTextStyle(label.getClass());
          }
          float [][] labelBounds = getTextBounds(labelText, labelStyle, xLabel, yLabel, 0);
          float labelWidth = labelBounds [2][0] - labelBounds [0][0];
          // Draw label text
          g2D.setFont(getFont(previousFont, labelStyle));
          g2D.drawString(labelText, xLabel - labelWidth / 2, yLabel);

          if (paintMode == PaintMode.PAINT && this.selectedItemsOutlinePainted && selectedLabel) {
            // Draw selection border
            g2D.setPaint(selectionOutlinePaint);
            g2D.setStroke(selectionOutlineStroke);
            g2D.draw(getShape(labelBounds));
            g2D.setPaint(foregroundColor);
          }
        }
      }
    }
    g2D.setFont(previousFont);
  }

  /**
   * Paints the compass. 
   */
  private void paintCompass(Graphics2D g2D, List<Selectable> selectedItems, float planScale, 
                            Color foregroundColor, PaintMode paintMode) {
    Compass compass = this.home.getCompass();
    if (compass.isVisible() 
        && (paintMode != PaintMode.CLIPBOARD
            || selectedItems.contains(compass))) {
      AffineTransform previousTransform = g2D.getTransform();
      g2D.translate(compass.getX(), compass.getY());
      g2D.rotate(compass.getNorthDirection());
      float diameter = compass.getDiameter();
      g2D.scale(diameter, diameter);
      g2D.setColor(foregroundColor);
      g2D.fill(COMPASS);
      g2D.setTransform(previousTransform);
    }
  }

  /**
   * Paints the outline of the compass when it's belongs to <code>items</code>. 
   */
  private void paintCompassOutline(Graphics2D g2D, List<Selectable> items,
                                   Paint selectionOutlinePaint, Stroke selectionOutlineStroke, 
                                   Paint indicatorPaint, float planScale, Color foregroundColor) {
    Compass compass = this.home.getCompass();
    if (items.contains(compass) 
        && compass.isVisible()) {
      AffineTransform previousTransform = g2D.getTransform();
      g2D.translate(compass.getX(), compass.getY());
      g2D.rotate(compass.getNorthDirection());
      float diameter = compass.getDiameter();
      g2D.scale(diameter, diameter);
  
      g2D.setPaint(selectionOutlinePaint);
      g2D.setStroke(new BasicStroke((5.5f + planScale) / diameter / planScale));
      g2D.draw(COMPASS_DISC);
      g2D.setColor(foregroundColor);
      g2D.setStroke(new BasicStroke(1f / diameter / planScale));
      g2D.draw(COMPASS_DISC);
      g2D.setTransform(previousTransform);

      // Paint indicators of the compass 
      if (items.size() == 1 
          && items.get(0) == compass) {
        g2D.setPaint(indicatorPaint);         
        paintCompassIndicators(g2D, compass, indicatorPaint, planScale);
      }
    }
  }

  private void paintCompassIndicators(Graphics2D g2D, 
                                      Compass compass, Paint indicatorPaint,
                                      float planScale) {
    if (this.resizeIndicatorVisible) {
      g2D.setPaint(indicatorPaint);
      g2D.setStroke(INDICATOR_STROKE);
      
      AffineTransform previousTransform = g2D.getTransform();
      // Draw rotation indicator at middle of second and third point of compass 
      float [][] compassPoints = compass.getPoints();
      float scaleInverse = 1 / planScale;
      g2D.translate((compassPoints [2][0] + compassPoints [3][0]) / 2, 
          (compassPoints [2][1] + compassPoints [3][1]) / 2);
      g2D.scale(scaleInverse, scaleInverse);
      g2D.rotate(compass.getNorthDirection());
      g2D.draw(COMPASS_ROTATION_INDICATOR);
      g2D.setTransform(previousTransform);

      // Draw resize indicator at middle of second and third point of compass
      g2D.translate((compassPoints [1][0] + compassPoints [2][0]) / 2, 
          (compassPoints [1][1] + compassPoints [2][1]) / 2);
      g2D.scale(scaleInverse, scaleInverse);
      g2D.rotate(compass.getNorthDirection());
      g2D.draw(COMPASS_RESIZE_INDICATOR);
      g2D.setTransform(previousTransform);
    }
  }

  /**
   * Paints wall location feedback.
   */
  private void paintWallAlignmentFeedback(Graphics2D g2D, 
                                          Wall alignedWall, Point2D locationFeedback, 
                                          boolean showPointFeedback, 
                                          Paint feedbackPaint, Stroke feedbackStroke,
                                          float planScale, Paint pointPaint, 
                                          Stroke pointStroke) {
    // Paint wall location feedback
    if (locationFeedback != null) {
      float margin = 0.5f / planScale;
      // Search which wall start or end point is at locationFeedback abscissa or ordinate
      // ignoring the start and end point of alignedWall
      float x = (float)locationFeedback.getX(); 
      float y = (float)locationFeedback.getY();
      float deltaXToClosestWall = Float.POSITIVE_INFINITY;
      float deltaYToClosestWall = Float.POSITIVE_INFINITY;
      for (Wall wall : getViewedItems(this.home.getWalls(), this.otherLevelWallsCache)) {
        if (wall != alignedWall) {
          if (Math.abs(x - wall.getXStart()) < margin
              && (alignedWall == null
                  || !equalsWallPoint(wall.getXStart(), wall.getYStart(), alignedWall))) {
            if (Math.abs(deltaYToClosestWall) > Math.abs(y - wall.getYStart())) {
              deltaYToClosestWall = y - wall.getYStart();
            }
          } else if (Math.abs(x - wall.getXEnd()) < margin
                    && (alignedWall == null
                        || !equalsWallPoint(wall.getXEnd(), wall.getYEnd(), alignedWall))) {
            if (Math.abs(deltaYToClosestWall) > Math.abs(y - wall.getYEnd())) {
              deltaYToClosestWall = y - wall.getYEnd();
            }                
          }
          
          if (Math.abs(y - wall.getYStart()) < margin
              && (alignedWall == null
                  || !equalsWallPoint(wall.getXStart(), wall.getYStart(), alignedWall))) {
            if (Math.abs(deltaXToClosestWall) > Math.abs(x - wall.getXStart())) {
              deltaXToClosestWall = x - wall.getXStart();
            }
          } else if (Math.abs(y - wall.getYEnd()) < margin
                    && (alignedWall == null
                        || !equalsWallPoint(wall.getXEnd(), wall.getYEnd(), alignedWall))) {
            if (Math.abs(deltaXToClosestWall) > Math.abs(x - wall.getXEnd())) {
              deltaXToClosestWall = x - wall.getXEnd();
            }                
          }

          float [][] wallPoints = wall.getPoints();
          // Take into account only points at start and end of the wall
          wallPoints = new float [][] {wallPoints [0], wallPoints [wallPoints.length / 2 - 1], 
                                       wallPoints [wallPoints.length / 2], wallPoints [wallPoints.length - 1]}; 
          for (int i = 0; i < wallPoints.length; i++) {
            if (Math.abs(x - wallPoints [i][0]) < margin
                && (alignedWall == null
                    || !equalsWallPoint(wallPoints [i][0], wallPoints [i][1], alignedWall))) {
              if (Math.abs(deltaYToClosestWall) > Math.abs(y - wallPoints [i][1])) {
                deltaYToClosestWall = y - wallPoints [i][1];
              }
            }
            if (Math.abs(y - wallPoints [i][1]) < margin
                && (alignedWall == null
                    || !equalsWallPoint(wallPoints [i][0], wallPoints [i][1], alignedWall))) {
              if (Math.abs(deltaXToClosestWall) > Math.abs(x - wallPoints [i][0])) {
                deltaXToClosestWall = x - wallPoints [i][0];
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

      // Draw point feedback
      if (showPointFeedback) {
        paintPointFeedback(g2D, locationFeedback, feedbackPaint, planScale, pointPaint, pointStroke);
      }
    }
  }

  /**
   * Returns the items viewed in the plan at the selected level.
   */
  private <T extends Elevatable> Collection<T> getViewedItems(Collection<T> homeItems, List<T> otherLevelItems) {
    List<T> viewedWalls = new ArrayList<T>();
    if (otherLevelItems != null) {
      viewedWalls.addAll(otherLevelItems);
    }
    for (T wall : homeItems) {
      if (isViewableAtSelectedLevel(wall)) {
        viewedWalls.add(wall);
      }
    }
    return viewedWalls;
  }

  /**
   * Paints point feedback.
   */
  private void paintPointFeedback(Graphics2D g2D, Point2D locationFeedback, 
                                  Paint feedbackPaint, float planScale,
                                  Paint pointPaint, Stroke pointStroke) {
    g2D.setPaint(pointPaint);         
    g2D.setStroke(pointStroke);
    g2D.draw(new Ellipse2D.Float((float)locationFeedback.getX() - 5f / planScale, 
        (float)locationFeedback.getY() - 5f / planScale, 10f / planScale, 10f / planScale));
    g2D.setPaint(feedbackPaint);         
    g2D.setStroke(new BasicStroke(1 / planScale));
    g2D.draw(new Line2D.Float((float)locationFeedback.getX(), 
        (float)locationFeedback.getY() - 5f / planScale, 
        (float)locationFeedback.getX(), 
        (float)locationFeedback.getY() + 5f / planScale));
    g2D.draw(new Line2D.Float((float)locationFeedback.getX() - 5f / planScale, 
        (float)locationFeedback.getY(), 
        (float)locationFeedback.getX() + 5f / planScale, 
        (float)locationFeedback.getY()));
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
   * Paints room location feedback.
   */
  private void paintRoomAlignmentFeedback(Graphics2D g2D, 
                                          Room alignedRoom, Point2D locationFeedback, 
                                          boolean showPointFeedback, 
                                          Paint feedbackPaint, Stroke feedbackStroke,
                                          float planScale, Paint pointPaint, 
                                          Stroke pointStroke) {
    // Paint room location feedback
    if (locationFeedback != null) {
      float margin = 0.5f / planScale;
      // Search which room points are at locationFeedback abscissa or ordinate
      float x = (float)locationFeedback.getX(); 
      float y = (float)locationFeedback.getY();
      float deltaXToClosestObject = Float.POSITIVE_INFINITY;
      float deltaYToClosestObject = Float.POSITIVE_INFINITY;
      for (Room room : getViewedItems(this.home.getRooms(), this.otherLevelRoomsCache)) {
        float [][] roomPoints = room.getPoints();
        int editedPointIndex = -1;
        if (room == alignedRoom) {
          // Search which room point could match location feedback 
          for (int i = 0; i < roomPoints.length; i++) {
            if (roomPoints [i][0] == x && roomPoints [i][1] == y) {
              editedPointIndex = i;
              break;
            }
          }
        }
        for (int i = 0; i < roomPoints.length; i++) {
          if (editedPointIndex == -1 || (i != editedPointIndex && roomPoints.length > 2)) {
            if (Math.abs(x - roomPoints [i][0]) < margin
                && Math.abs(deltaYToClosestObject) > Math.abs(y - roomPoints [i][1])) {
              deltaYToClosestObject = y - roomPoints [i][1];
            }
            if (Math.abs(y - roomPoints [i][1]) < margin
                && Math.abs(deltaXToClosestObject) > Math.abs(x - roomPoints [i][0])) {
              deltaXToClosestObject = x - roomPoints [i][0];
            }
          }
        }
      }
      // Search which wall points are at locationFeedback abscissa or ordinate
      for (Wall wall : getViewedItems(this.home.getWalls(), this.otherLevelWallsCache)) {
        float [][] wallPoints = wall.getPoints();
        // Take into account only points at start and end of the wall
        wallPoints = new float [][] {wallPoints [0], wallPoints [wallPoints.length / 2 - 1], 
                                     wallPoints [wallPoints.length / 2], wallPoints [wallPoints.length - 1]}; 
        for (int i = 0; i < wallPoints.length; i++) {
          if (Math.abs(x - wallPoints [i][0]) < margin
              && Math.abs(deltaYToClosestObject) > Math.abs(y - wallPoints [i][1])) {
            deltaYToClosestObject = y - wallPoints [i][1];
          }
          if (Math.abs(y - wallPoints [i][1]) < margin
              && Math.abs(deltaXToClosestObject) > Math.abs(x - wallPoints [i][0])) {
            deltaXToClosestObject = x - wallPoints [i][0];
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
      
      if (showPointFeedback) {
        paintPointFeedback(g2D, locationFeedback, feedbackPaint, planScale, pointPaint, pointStroke);
      }
    }
  }

  /**
   * Paints dimension line location feedback.
   */
  private void paintDimensionLineAlignmentFeedback(Graphics2D g2D, 
                                                   DimensionLine alignedDimensionLine, Point2D locationFeedback, 
                                                   boolean showPointFeedback, 
                                                   Paint feedbackPaint, Stroke feedbackStroke,
                                                   float planScale, Paint pointPaint, 
                                                   Stroke pointStroke) {
    // Paint dimension line location feedback
    if (locationFeedback != null) {      
      float margin = 0.5f / planScale;
      // Search which room points are at locationFeedback abscissa or ordinate
      float x = (float)locationFeedback.getX(); 
      float y = (float)locationFeedback.getY();
      float deltaXToClosestObject = Float.POSITIVE_INFINITY;
      float deltaYToClosestObject = Float.POSITIVE_INFINITY;
      for (Room room : getViewedItems(this.home.getRooms(), this.otherLevelRoomsCache)) {
        float [][] roomPoints = room.getPoints();
        for (int i = 0; i < roomPoints.length; i++) {
          if (Math.abs(x - roomPoints [i][0]) < margin
              && Math.abs(deltaYToClosestObject) > Math.abs(y - roomPoints [i][1])) {
            deltaYToClosestObject = y - roomPoints [i][1];
          }
          if (Math.abs(y - roomPoints [i][1]) < margin
              && Math.abs(deltaXToClosestObject) > Math.abs(x - roomPoints [i][0])) {
            deltaXToClosestObject = x - roomPoints [i][0];
          }
        }
      }
      // Search which dimension line start or end point is at locationFeedback abscissa or ordinate
      // ignoring the start and end point of alignedDimensionLine
      for (DimensionLine dimensionLine : this.home.getDimensionLines()) {
        if (isViewableAtSelectedLevel(dimensionLine) 
            && dimensionLine != alignedDimensionLine) {
          if (Math.abs(x - dimensionLine.getXStart()) < margin
              && (alignedDimensionLine == null
                  || !equalsDimensionLinePoint(dimensionLine.getXStart(), dimensionLine.getYStart(), 
                          alignedDimensionLine))) {
            if (Math.abs(deltaYToClosestObject) > Math.abs(y - dimensionLine.getYStart())) {
              deltaYToClosestObject = y - dimensionLine.getYStart();
            }
          } else if (Math.abs(x - dimensionLine.getXEnd()) < margin
                    && (alignedDimensionLine == null
                        || !equalsDimensionLinePoint(dimensionLine.getXEnd(), dimensionLine.getYEnd(), 
                                alignedDimensionLine))) {
            if (Math.abs(deltaYToClosestObject) > Math.abs(y - dimensionLine.getYEnd())) {
              deltaYToClosestObject = y - dimensionLine.getYEnd();
            }                
          }
          if (Math.abs(y - dimensionLine.getYStart()) < margin
              && (alignedDimensionLine == null
                  || !equalsDimensionLinePoint(dimensionLine.getXStart(), dimensionLine.getYStart(), 
                          alignedDimensionLine))) {
            if (Math.abs(deltaXToClosestObject) > Math.abs(x - dimensionLine.getXStart())) {
              deltaXToClosestObject = x - dimensionLine.getXStart();
            }
          } else if (Math.abs(y - dimensionLine.getYEnd()) < margin
                    && (alignedDimensionLine == null
                        || !equalsDimensionLinePoint(dimensionLine.getXEnd(), dimensionLine.getYEnd(), 
                                alignedDimensionLine))) {
            if (Math.abs(deltaXToClosestObject) > Math.abs(x - dimensionLine.getXEnd())) {
              deltaXToClosestObject = x - dimensionLine.getXEnd();
            }                
          }
        }
      }
      // Search which wall points are at locationFeedback abscissa or ordinate
      for (Wall wall : getViewedItems(this.home.getWalls(), this.otherLevelWallsCache)) {
        float [][] wallPoints = wall.getPoints();
        // Take into account only points at start and end of the wall
        wallPoints = new float [][] {wallPoints [0], wallPoints [wallPoints.length / 2 - 1], 
                                     wallPoints [wallPoints.length / 2], wallPoints [wallPoints.length - 1]}; 
        for (int i = 0; i < wallPoints.length; i++) {
          if (Math.abs(x - wallPoints [i][0]) < margin
              && Math.abs(deltaYToClosestObject) > Math.abs(y - wallPoints [i][1])) {
            deltaYToClosestObject = y - wallPoints [i][1];
          }
          if (Math.abs(y - wallPoints [i][1]) < margin
              && Math.abs(deltaXToClosestObject) > Math.abs(x - wallPoints [i][0])) {
            deltaXToClosestObject = x - wallPoints [i][0];
          }
        }
      }
      // Search which piece of furniture points are at locationFeedback abscissa or ordinate
      for (HomePieceOfFurniture piece : this.home.getFurniture()) {
        if (piece.isVisible()
            && isViewableAtSelectedLevel(piece)) {
          float [][] piecePoints = piece.getPoints();
          for (int i = 0; i < piecePoints.length; i++) {
            if (Math.abs(x - piecePoints [i][0]) < margin
                && Math.abs(deltaYToClosestObject) > Math.abs(y - piecePoints [i][1])) {
              deltaYToClosestObject = y - piecePoints [i][1];
            }
            if (Math.abs(y - piecePoints [i][1]) < margin
                && Math.abs(deltaXToClosestObject) > Math.abs(x - piecePoints [i][0])) {
              deltaXToClosestObject = x - piecePoints [i][0];
            }

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

      if (showPointFeedback) {
        paintPointFeedback(g2D, locationFeedback, feedbackPaint, planScale, pointPaint, pointStroke);
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
   * Paints an arc centered at <code>center</code> point that goes
   */
  private void paintAngleFeedback(Graphics2D g2D, Point2D center, 
                                  Point2D point1, Point2D point2,
                                  float planScale, Color selectionColor) {
    g2D.setColor(selectionColor);
    g2D.setStroke(new BasicStroke(1 / planScale));
    // Compute angles
    double angle1 = Math.atan2(center.getY() - point1.getY(), point1.getX() - center.getX());
    if (angle1 < 0) {
      angle1 = 2 * Math.PI + angle1; 
    }
    double angle2 = Math.atan2(center.getY() - point2.getY(), point2.getX() - center.getX());
    if (angle2 < 0) {
      angle2 = 2 * Math.PI + angle2; 
    }
    double extent = angle2 - angle1;
    if (angle1 > angle2) {
      extent = 2 * Math.PI + extent;
    }
    AffineTransform previousTransform = g2D.getTransform();
    // Draw an arc
    g2D.translate(center.getX(), center.getY());
    float radius = 20 / planScale;
    g2D.draw(new Arc2D.Double(-radius, -radius,
        radius * 2, radius * 2, Math.toDegrees(angle1), Math.toDegrees(extent), Arc2D.OPEN));
    // Draw two radius
    radius += 5 / planScale;
    g2D.draw(new Line2D.Double(0, 0, radius * Math.cos(angle1), -radius * Math.sin(angle1)));
    g2D.draw(new Line2D.Double(0, 0, radius * Math.cos(angle1 + extent), -radius * Math.sin(angle1 + extent)));
    g2D.setTransform(previousTransform);
  }

  /**
   * Paints the observer camera at its current location, if home camera is the observer camera.
   */
  private void paintCamera(Graphics2D g2D, List<Selectable> selectedItems,
                           Paint selectionOutlinePaint, Stroke selectionOutlineStroke, 
                           Paint indicatorPaint, float planScale, 
                           Color backgroundColor, Color foregroundColor) {
    ObserverCamera camera = this.home.getObserverCamera();
    if (camera == this.home.getCamera()) {
      AffineTransform previousTransform = g2D.getTransform();
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
      BasicStroke stroke = new BasicStroke(BORDER_STROKE_WIDTH / planScale);
      g2D.setStroke(stroke);
      g2D.draw(scaledCameraBody);
  
      if (selectedItems.contains(camera)
          && this.selectedItemsOutlinePainted) {
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
      g2D.setTransform(previousTransform);
  
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
      g2D.setStroke(INDICATOR_STROKE);
      
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

      // Draw elevation indicator at middle of first and second point of camera 
      g2D.translate((cameraPoints [0][0] + cameraPoints [1][0]) / 2, 
          (cameraPoints [0][1] + cameraPoints [1][1]) / 2);
      g2D.scale(scaleInverse, scaleInverse);
      g2D.draw(POINT_INDICATOR);
      g2D.translate(Math.sin(camera.getYaw()) * 8, -Math.cos(camera.getYaw()) * 8);
      g2D.draw(CAMERA_ELEVATION_INDICATOR);
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
    if (includeCamera) {
      return getItemsBounds(getGraphics(), this.home.getSelectedItems());
    } else {
      List<Selectable> selectedItems = new ArrayList<Selectable>(this.home.getSelectedItems());
      selectedItems.remove(this.home.getCamera());
      return getItemsBounds(getGraphics(), selectedItems);
    }
  }

  /**
   * Ensures the point at (<code>x</code>, <code>y</code>) is visible,
   * moving scroll bars if needed.
   */
  public void makePointVisible(float x, float y) {
    scrollRectToVisible(getShapePixelBounds(new Rectangle2D.Float(x, y, 1 / getScale(), 1 / getScale())));
  }

  /**
   * Moves the view from (dx, dy) unit in the scrolling zone it belongs to.
   */
  public void moveView(float dx, float dy) {
    if (getParent() instanceof JViewport) {
      JViewport viewport = (JViewport)getParent();
      Rectangle viewRectangle = viewport.getViewRect();
      viewRectangle.translate(Math.round(dx * getScale()), Math.round(dy * getScale()));
      viewRectangle.x = Math.min(Math.max(0, viewRectangle.x), getWidth() - viewRectangle.width);
      viewRectangle.y = Math.min(Math.max(0, viewRectangle.y), getHeight() - viewRectangle.height);
      viewport.setViewPosition(viewRectangle.getLocation());
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
   * If this component is displayed in a viewport the view position is updated
   * to ensure the center's view will remain the same after the scale change.
   */
  public void setScale(float scale) {
    if (this.scale != scale) {
      JViewport parent = null;
      Rectangle viewRectangle = null;
      float xViewCenterPosition = 0;
      float yViewCenterPosition = 0;
      if (getParent() instanceof JViewport) {
        parent = (JViewport)getParent();
        viewRectangle = parent.getViewRect();
        xViewCenterPosition = (convertXPixelToModel(viewRectangle.x) 
            + convertXPixelToModel(viewRectangle.x + viewRectangle.width)) / 2;
        yViewCenterPosition = (convertYPixelToModel(viewRectangle.y)
            + convertYPixelToModel(viewRectangle.y + viewRectangle.height)) / 2;
      }
      
      this.scale = scale;
      revalidate(false);

      if (parent instanceof JViewport) {
        Dimension viewSize = parent.getViewSize();
        float viewWidth = convertXPixelToModel(viewRectangle.x + viewRectangle.width) 
            - convertXPixelToModel(viewRectangle.x);          
        int xViewLocation = Math.max(0, Math.min(convertXModelToPixel(xViewCenterPosition - viewWidth / 2),
            viewSize.width - viewRectangle.x));
        float viewHeight = convertYPixelToModel(viewRectangle.y + viewRectangle.height) 
            - convertYPixelToModel(viewRectangle.y);          
        int yViewLocation = Math.max(0, Math.min(convertYModelToPixel(yViewCenterPosition - viewHeight / 2),
            viewSize.height - viewRectangle.y));        
        parent.setViewPosition(new Point(xViewLocation, yViewLocation));
      }
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
   * Returns <code>x</code> converted in screen coordinates space.
   */
  public int convertXModelToScreen(float x) {
    Point point = new Point(convertXModelToPixel(x), 0);
    SwingUtilities.convertPointToScreen(point, this);
    return point.x;
  }

  /**
   * Returns <code>y</code> converted in screen coordinates space.
   */
  public int convertYModelToScreen(float y) {
    Point point = new Point(0, convertYModelToPixel(y));
    SwingUtilities.convertPointToScreen(point, this);
    return point.y;
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
  public void setCursor(CursorType cursorType) {
    switch (cursorType) {
      case SELECTION :
        setCursor(Cursor.getDefaultCursor());
        break;
      case DRAW :
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        break;
      case ROTATION :
        setCursor(this.rotationCursor);
        break;
      case HEIGHT :
        setCursor(this.heightCursor);
        break;
      case POWER :
        setCursor(this.powerCursor);
        break;
      case ELEVATION :
        setCursor(this.elevationCursor);
        break;
      case RESIZE :
        setCursor(this.resizeCursor);
        break;
      case PANNING :
        setCursor(this.panningCursor);
        break;
      case DUPLICATION :
        setCursor(this.duplicationCursor);
        break;
    }
  }

  /**
   * Sets tool tip text displayed as feedback. 
   * @param toolTipFeedback the text displayed in the tool tip 
   *                    or <code>null</code> to make tool tip disappear.
   */
  public void setToolTipFeedback(String toolTipFeedback, float x, float y) {
    stopToolTipPropertiesEdition();
    JToolTip toolTip = getToolTip();
    // Change tool tip text    
    toolTip.setTipText(toolTipFeedback);    
    showToolTipComponentAt(toolTip, x , y);
  }

  /**
   * Returns the tool tip of the plan.
   */
  private JToolTip getToolTip() {
    // Create tool tip for this component
    if (this.toolTip == null) {
      this.toolTip = new JToolTip();
      this.toolTip.setComponent(this);
    }
    return this.toolTip;
  }

  /**
   * Shows the given component as a tool tip.
   */
  private void showToolTipComponentAt(JComponent toolTipComponent, float x, float y) {
    if (this.toolTipWindow == null) {
      // Show tool tip in a window (we don't use a Swing Popup because 
      // we require the tool tip window to move along with mouse pointer 
      // and a Swing popup can't move without hiding then showing it again)
      this.toolTipWindow = new JWindow(JOptionPane.getFrameForComponent(this));
      this.toolTipWindow.setFocusableWindowState(false);
      this.toolTipWindow.add(toolTipComponent);
      // Add to window a mouse listener that redispatch mouse events to
      // plan component (if the user moves fast enough the mouse pointer in a way 
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
          dispatchEvent(SwingUtilities.convertMouseEvent(toolTipWindow, ev, PlanComponent.this));
        }
        
        @Override
        public void mouseDragged(MouseEvent ev) {
          mouseMoved(ev);
        }
      };
      this.toolTipWindow.addMouseListener(mouseAdapter);
      this.toolTipWindow.addMouseMotionListener(mouseAdapter);
    } else {
      Container contentPane = this.toolTipWindow.getContentPane();
      if (contentPane.getComponent(0) != toolTipComponent) {
        contentPane.removeAll();
        contentPane.add(toolTipComponent);
      }
      toolTipComponent.revalidate();
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
    toolTipComponent.paintImmediately(toolTipComponent.getBounds());
  }
  
  /**
   * Set tool tip edition.
   */
  public void setToolTipEditedProperties(final PlanController.EditableProperty [] toolTipEditedProperties, 
                                         Object [] toolTipPropertyValues,
                                         float x, float y) {
    final JPanel toolTipPropertiesPanel = new JPanel(new GridBagLayout());
    // Reuse tool tip look
    Border border = UIManager.getBorder("ToolTip.border");
    if (!OperatingSystem.isMacOSX()) {
      border = BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(0, 3, 0, 2));
    }
    toolTipPropertiesPanel.setBorder(border);
    // Copy colors from tool tip instance (on Linux, colors aren't set in UIManager)
    JToolTip toolTip = getToolTip();
    toolTipPropertiesPanel.setBackground(toolTip.getBackground());
    toolTipPropertiesPanel.setForeground(toolTip.getForeground());

    // Add labels and text fields to tool tip panel
    for (int i = 0; i < toolTipEditedProperties.length; i++) {
      JFormattedTextField textField = this.toolTipEditableTextFields.get(toolTipEditedProperties [i]);
      textField.setValue(toolTipPropertyValues [i]); 
      JLabel label = new JLabel(this.preferences.getLocalizedString(PlanComponent.class, 
          toolTipEditedProperties [i].name() + ".editablePropertyLabel.text") + " ");
      label.setFont(textField.getFont());
      JLabel unitLabel = null;
      if (toolTipEditedProperties [i] == PlanController.EditableProperty.ANGLE
          || toolTipEditedProperties [i] == PlanController.EditableProperty.ARC_EXTENT) {
        unitLabel = new JLabel(this.preferences.getLocalizedString(PlanComponent.class, "degreeLabel.text"));
      } else if (this.preferences.getLengthUnit() != LengthUnit.INCH) {
        unitLabel = new JLabel(" " + this.preferences.getLengthUnit().getName());
      }
      
      JPanel labelTextFieldPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
      labelTextFieldPanel.setOpaque(false);
      
      labelTextFieldPanel.add(label);
      labelTextFieldPanel.add(textField);
      if (unitLabel != null) {
        unitLabel.setFont(textField.getFont());
        labelTextFieldPanel.add(unitLabel);
      }
      toolTipPropertiesPanel.add(labelTextFieldPanel, new GridBagConstraints(
          0, i, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    }
    
    showToolTipComponentAt(toolTipPropertiesPanel, x, y);
    // Add a key listener that redispatches events to tool tip text fields
    // (don't give focus to tool tip window otherwise plan component window will lose focus)
    this.toolTipKeyListener = new KeyListener() {
        private int focusedTextFieldIndex;
        private JTextComponent focusedTextField;
  
        {
          // Simulate focus on first text field
          setFocusedTextFieldIndex(0);
        }
        
        private void setFocusedTextFieldIndex(int textFieldIndex) {
          if (this.focusedTextField != null) {
            this.focusedTextField.getCaret().setVisible(false);
            this.focusedTextField.getCaret().setSelectionVisible(false);        
          }
          this.focusedTextFieldIndex = textFieldIndex;
          this.focusedTextField = toolTipEditableTextFields.get(toolTipEditedProperties [textFieldIndex]);
          if (this.focusedTextField.getText().length() == 0) {
            this.focusedTextField.getCaret().setVisible(false);
          } else {
            this.focusedTextField.selectAll();
          }
          this.focusedTextField.getCaret().setSelectionVisible(true);
        }
        
        public void keyPressed(KeyEvent ev) {
          keyTyped(ev);
        }
  
        public void keyReleased(KeyEvent ev) {
          if (ev.getKeyCode() != KeyEvent.VK_CONTROL
              && ev.getKeyCode() != KeyEvent.VK_ALT) { 
            // Forward other key events to focused text field (except for Ctrl and Alt key, otherwise InputMap won't receive it)
            KeyboardFocusManager.getCurrentKeyboardFocusManager().redispatchEvent(this.focusedTextField, ev);
          }
        }
  
        public void keyTyped(KeyEvent ev) {
          Set<AWTKeyStroke> forwardKeys = this.focusedTextField.getFocusTraversalKeys(
              KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
          if (forwardKeys.contains(AWTKeyStroke.getAWTKeyStrokeForEvent(ev))
              || ev.getKeyCode() == KeyEvent.VK_DOWN) {
            setFocusedTextFieldIndex((this.focusedTextFieldIndex + 1) % toolTipEditedProperties.length);
            ev.consume();
          } else {
            Set<AWTKeyStroke> backwardKeys = this.focusedTextField.getFocusTraversalKeys(
                KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
            if (backwardKeys.contains(AWTKeyStroke.getAWTKeyStrokeForEvent(ev))
                || ev.getKeyCode() == KeyEvent.VK_UP) {
              setFocusedTextFieldIndex((this.focusedTextFieldIndex - 1 + toolTipEditedProperties.length) % toolTipEditedProperties.length);
              ev.consume();
            } else if ((ev.getKeyCode() == KeyEvent.VK_HOME
                          || ev.getKeyCode() == KeyEvent.VK_END)
                       && OperatingSystem.isMacOSX()
                       && !OperatingSystem.isMacOSXLeopardOrSuperior()) {
              // Support Home and End keys under Mac OS X Tiger
              if (ev.getKeyCode() == KeyEvent.VK_HOME) {
                focusedTextField.setCaretPosition(0);
              } else if (ev.getKeyCode() == KeyEvent.VK_END) {
                focusedTextField.setCaretPosition(focusedTextField.getText().length());
              }
              ev.consume();
            } else if (ev.getKeyCode() != KeyEvent.VK_ESCAPE
                       && ev.getKeyCode() != KeyEvent.VK_CONTROL
                       && ev.getKeyCode() != KeyEvent.VK_ALT) { 
              // Forward other key events to focused text field (except for Esc key, otherwise InputMap won't receive it)
              KeyboardFocusManager.getCurrentKeyboardFocusManager().redispatchEvent(this.focusedTextField, ev);
              this.focusedTextField.getCaret().setVisible(true);
              toolTipWindow.pack();
            } 
          }
        }      
      };

    addKeyListener(this.toolTipKeyListener);
    setFocusTraversalKeysEnabled(false);
    installEditionKeyboardActions();
  }
  
  /**
   * Deletes tool tip text from screen. 
   */
  public void deleteToolTipFeedback() {
    stopToolTipPropertiesEdition();
    if (this.toolTip != null) {
      this.toolTip.setTipText(null);
    }
    if (this.toolTipWindow != null) {
      this.toolTipWindow.setVisible(false);
    }
  }

  /**
   * Stops editing in tool tip text fields.
   */
  private void stopToolTipPropertiesEdition() {
    if (this.toolTipKeyListener != null) {
      installDefaultKeyboardActions();
      setFocusTraversalKeysEnabled(true);
      removeKeyListener(toolTipKeyListener);
      this.toolTipKeyListener = null;
      
      for (JFormattedTextField textField : this.toolTipEditableTextFields.values()) {
        textField.getCaret().setVisible(false);
        textField.getCaret().setSelectionVisible(false);
      }      
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
   * Sets the location point for alignment feedback. 
   */
  public void setAlignmentFeedback(Class<? extends Selectable> alignedObjectClass,
                                   Selectable alignedObject,
                                   float x, 
                                   float y, 
                                   boolean showPointFeedback) {
    this.alignedObjectClass = alignedObjectClass;
    this.alignedObjectFeedback = alignedObject;
    this.locationFeeback = new Point2D.Float(x, y);
    this.showPointFeedback = showPointFeedback;
    repaint();
  }
  
  /**
   * Sets the points used to draw an angle in plan view.
   */
  public void setAngleFeedback(float xCenter, float yCenter, 
                               float x1, float y1, 
                               float x2, float y2) {
    this.centerAngleFeedback = new Point2D.Float(xCenter, yCenter);
    this.point1AngleFeedback = new Point2D.Float(x1, y1);
    this.point2AngleFeedback = new Point2D.Float(x2, y2);
  }

  /**
   * Sets the feedback of dragged items drawn during a drag and drop operation, 
   * initiated from outside of plan view.
   */
  public void setDraggedItemsFeedback(List<Selectable> draggedItems) {
    this.draggedItemsFeedback = draggedItems;
    repaint();
  }

  /**
   * Sets the given dimension lines to be drawn as feedback.
   */
  public void setDimensionLinesFeedback(List<DimensionLine> dimensionLines) {
    this.dimensionLinesFeedback = dimensionLines;
    repaint();
  }

  /**
   * Deletes all elements shown as feedback.
   */
  public void deleteFeedback() {
    deleteToolTipFeedback();
    this.rectangleFeedback = null;
    
    this.alignedObjectClass = null;
    this.alignedObjectFeedback = null;
    this.locationFeeback = null;

    this.centerAngleFeedback = null;
    this.point1AngleFeedback = null;
    this.point2AngleFeedback = null;

    this.draggedItemsFeedback = null;

    this.dimensionLinesFeedback = null;
    repaint();
  }

  /**
   * Returns <code>true</code>.
   */
  public boolean canImportDraggedItems(List<Selectable> items, int x, int y) {
    return true;
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
  
  /**
   * Returns the component used as an horizontal ruler for this plan.
   */
  public View getHorizontalRuler() {
    if (this.horizontalRuler == null) {
      this.horizontalRuler = new PlanRulerComponent(SwingConstants.HORIZONTAL);
    } 
    return this.horizontalRuler;
  }
  
  /**
   * Returns the component used as a vertical ruler for this plan.
   */
  public View getVerticalRuler() {
    if (this.verticalRuler == null) {
      this.verticalRuler = new PlanRulerComponent(SwingConstants.VERTICAL);
    } 
    return this.verticalRuler;
  }
  
  /**
   * A component displaying the plan horizontal or vertical ruler associated to this plan.
   */
  public class PlanRulerComponent extends JComponent implements View {
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
      // Use same font as tool tips
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
      float gridSize = getGridSize(rulerScale);
      float mainGridSize = getMainGridSize(rulerScale);
      
      Rectangle2D planBounds = getPlanBounds();    
      float xMin = (float)planBounds.getMinX() - MARGIN;
      float yMin = (float)planBounds.getMinY() - MARGIN;
      float xMax = convertXPixelToModel(getWidth());
      float yMax = convertYPixelToModel(getHeight());
      boolean leftToRightOriented = getComponentOrientation().isLeftToRight();

      FontMetrics metrics = getFontMetrics(getFont());
      int fontAscent = metrics.getAscent();
      float tickSize = 5 / rulerScale;
      float mainTickSize = (fontAscent + 6) / rulerScale;
      NumberFormat format = NumberFormat.getIntegerInstance();
      
      g2D.setColor(getForeground());
      float lineWidth = 0.5f / rulerScale;
      g2D.setStroke(new BasicStroke(lineWidth));
      if (this.orientation == SwingConstants.HORIZONTAL) {
        // Draw horizontal rule base
        g2D.draw(new Line2D.Float(xMin, yMax - lineWidth, xMax, yMax - lineWidth));
        // Draw small ticks
        for (float x = (int) (xMin / gridSize) * gridSize; x < xMax; x += gridSize) {
          g2D.draw(new Line2D.Float(x, yMax - tickSize, x, yMax));
        }
      } else {
        // Draw vertical rule base
        if (leftToRightOriented) {
          g2D.draw(new Line2D.Float(xMax - lineWidth, yMin, xMax - lineWidth, yMax));
        } else {
          g2D.draw(new Line2D.Float(xMin + lineWidth, yMin, xMin + lineWidth, yMax));
        }
        // Draw small ticks
        for (float y = (int) (yMin / gridSize) * gridSize; y < yMax; y += gridSize) {
          if (leftToRightOriented) {
            g2D.draw(new Line2D.Float(xMax - tickSize, y, xMax, y));
          } else {
            g2D.draw(new Line2D.Float(xMin, y, xMin + tickSize, y));
          }
        }
      }

      if (mainGridSize != gridSize) {
        g2D.setStroke(new BasicStroke(1.5f / rulerScale,
            BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        AffineTransform previousTransform = g2D.getTransform();
        // Draw big ticks
        if (this.orientation == SwingConstants.HORIZONTAL) {
          for (float x = (int) (xMin / mainGridSize) * mainGridSize; x < xMax; x += mainGridSize) {
            g2D.draw(new Line2D.Float(x, yMax - mainTickSize, x, yMax));
            // Draw unit text
            g2D.translate(x, yMax - mainTickSize);
            g2D.scale(1 / rulerScale, 1 / rulerScale);
            g2D.drawString(getFormattedTickText(format, x), 3, fontAscent - 1);
            g2D.setTransform(previousTransform);
          }
        } else {
          for (float y = (int) (yMin / mainGridSize) * mainGridSize; y < yMax; y += mainGridSize) {
            String yText = getFormattedTickText(format, y);
            if (leftToRightOriented) {
              g2D.draw(new Line2D.Float(xMax - mainTickSize, y, xMax, y));
              // Draw unit text with a vertical orientation
              g2D.translate(xMax - mainTickSize, y);
              g2D.scale(1 / rulerScale, 1 / rulerScale);
              g2D.rotate(-Math.PI / 2);
              g2D.drawString(yText, -metrics.stringWidth(yText) - 3, fontAscent - 1);
            } else {
              g2D.draw(new Line2D.Float(xMin, y, xMin +  mainTickSize, y));
              // Draw unit text with a vertical orientation
              g2D.translate(xMin + mainTickSize, y);
              g2D.scale(1 / rulerScale, 1 / rulerScale);
              g2D.rotate(Math.PI / 2);
              g2D.drawString(yText, 3, fontAscent - 1);
            }
            g2D.setTransform(previousTransform);
          }
        }
      }

      if (this.mouseLocation != null) {
        g2D.setColor(getSelectionColor());
        g2D.setStroke(new BasicStroke(1 / rulerScale));
        if (this.orientation == SwingConstants.HORIZONTAL) {
          // Draw mouse feedback vertical line
          float x = convertXPixelToModel(this.mouseLocation.x);
          g2D.draw(new Line2D.Float(x, yMax - mainTickSize, x, yMax));
        } else {
          // Draw mouse feedback horizontal line
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
      if (preferences.getLengthUnit() == LengthUnit.INCH) {
        text = format.format(LengthUnit.centimeterToFoot(value)) + "'"; 
      } else {
        text = format.format(value / 100);
        if (value == 0) {
          text += LengthUnit.METER.getName();
        }
      }
      return text;
    }
  }

  /**
   * A proxy for the furniture icon seen from top. 
   */
  private abstract static class PieceOfFurnitureTopViewIcon implements Icon {
    private Icon icon;
    
    public PieceOfFurnitureTopViewIcon(Icon icon) {
      this.icon = icon;
    }

    public int getIconWidth() {
      return this.icon.getIconWidth();
    }

    public int getIconHeight() {
      return this.icon.getIconHeight();
    }
    
    public void paintIcon(Component c, Graphics g, int x, int y) {
      this.icon.paintIcon(c, g, x, y);
    }

    public boolean isWaitIcon() {
      return IconManager.getInstance().isWaitIcon(this.icon);
    }
    
    public boolean isErrorIcon() {
      return IconManager.getInstance().isErrorIcon(this.icon);
    }
    
    protected void setIcon(Icon icon) {
      this.icon = icon;
    }
  }
  
  /**
   * A proxy for the furniture plan icon generated from its plan icon. 
   */
  private static class PieceOfFurniturePlanIcon extends PieceOfFurnitureTopViewIcon {
    private final float pieceWidth;
    private final float pieceDepth;
    private Integer     pieceColor;
    private HomeTexture pieceTexture;
    
    /**
     * Creates a plan icon proxy for a <code>piece</code> of furniture.
     * @param piece an object containing a plan icon content
     * @param waitingComponent a waiting component. If <code>null</code>, the returned icon will
     *            be read immediately in the current thread.
     */
    public PieceOfFurniturePlanIcon(final HomePieceOfFurniture piece, 
                                    final Component waitingComponent) {
      super(IconManager.getInstance().getIcon(piece.getPlanIcon(), waitingComponent));
      this.pieceWidth = piece.getWidth();
      this.pieceDepth = piece.getDepth();
      this.pieceColor = piece.getColor();
      this.pieceTexture = piece.getTexture();
    }
    
    @Override
    public void paintIcon(final Component c, Graphics g, int x, int y) {
      if (!isWaitIcon()
          && !isErrorIcon()) {
        if (this.pieceColor != null) {
          // Create a monochrome icon from plan icon  
          BufferedImage image = new BufferedImage(getIconWidth(), getIconHeight(), BufferedImage.TYPE_INT_ARGB);
          Graphics imageGraphics = image.getGraphics();
          super.paintIcon(c, imageGraphics, 0, 0);
          imageGraphics.dispose();
          
          final int colorRed   = this.pieceColor & 0xFF0000;
          final int colorGreen = this.pieceColor & 0xFF00;
          final int colorBlue  = this.pieceColor & 0xFF;
          setIcon(new ImageIcon(c.createImage(new FilteredImageSource(image.getSource (),
              new RGBImageFilter() {
                {
                  canFilterIndexColorModel = true;
                }
  
                public int filterRGB (int x, int y, int rgb) {
                  int alpha = rgb & 0xFF000000;
                  int red   = (rgb & 0x00FF0000) >> 16;
                  int green = (rgb & 0x0000FF00) >> 8;
                  int blue  = rgb & 0x000000FF;
                  
                  // Approximate brightness computation to 0.375 red + 0.5 green + 0.125 blue 
                  // for faster results
                  int brightness = (red + red + red + green + green + green + green + blue) >> 3;
                  
                  red   = (colorRed   * brightness / 0xFF) & 0xFF0000;
                  green = (colorGreen * brightness / 0xFF) & 0xFF00;
                  blue  = (colorBlue  * brightness / 0xFF) & 0xFF;
                  return alpha | red | green | blue;
                }
              }))));
          // Don't need color information anymore
          this.pieceColor = null;
        } else if (this.pieceTexture != null) {
          if (isTextureManagerAvailable()) {
            // Prefer to share textures images with texture manager if it's available
            TextureManager.getInstance().loadTexture(this.pieceTexture.getImage(), true,
                new TextureManager.TextureObserver() {
                  public void textureUpdated(Texture texture) {                  
                    setTexturedIcon(c, ((ImageComponent2D)texture.getImage(0)).getImage());
                  }
                });
          } else { 
            Icon textureIcon = IconManager.getInstance().getIcon(this.pieceTexture.getImage(), null);
            if (IconManager.getInstance().isErrorIcon(textureIcon)) {
              setTexturedIcon(c, ERROR_TEXTURE_IMAGE);                    
            } else {
              BufferedImage textureIconImage = new BufferedImage(
                  textureIcon.getIconWidth(), textureIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
              Graphics2D g2DIcon = (Graphics2D)textureIconImage.getGraphics();
              textureIcon.paintIcon(c, g2DIcon, 0, 0);
              g2DIcon.dispose();
              setTexturedIcon(c, textureIconImage);
            }
          }
  
          // Don't need texture information anymore
          this.pieceTexture = null;
        }
      }
      super.paintIcon(c, g, x, y);
    }
    
    private void setTexturedIcon(Component c, BufferedImage textureImage) {
      // Paint plan icon in an image
      BufferedImage image = new BufferedImage(getIconWidth(), getIconHeight(), BufferedImage.TYPE_INT_ARGB);
      final Graphics2D imageGraphics = (Graphics2D)image.getGraphics();
      PieceOfFurniturePlanIcon.super.paintIcon(c, imageGraphics, 0, 0);                  
      // Fill the pixels of plan icon with texture image
      imageGraphics.setPaint(new TexturePaint(textureImage, 
          new Rectangle2D.Float(0, 0, -getIconWidth() / pieceWidth * pieceTexture.getWidth(), 
              -getIconHeight() / pieceDepth * pieceTexture.getHeight())));
      imageGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN));
      imageGraphics.fillRect(0, 0, getIconWidth(), getIconHeight());                  
      imageGraphics.dispose();
      
      setIcon(new ImageIcon(image));
    }
  }
  
  /**
   * A proxy for the furniture top view icon generated from its 3D model. 
   */
  private static class PieceOfFurnitureModelIcon extends PieceOfFurnitureTopViewIcon {
    private static Canvas3D        canvas3D;
    private static BranchGroup     sceneRoot;
    private static ExecutorService iconsCreationExecutor;
    
    static {
      // Create the universe used to compute top view icons 
      canvas3D = Component3DManager.getInstance().getOffScreenCanvas3D(128, 128);
      SimpleUniverse universe = new SimpleUniverse(canvas3D);
      ViewingPlatform viewingPlatform = universe.getViewingPlatform();
      // View model from top
      TransformGroup viewPlatformTransform = viewingPlatform.getViewPlatformTransform();
      Transform3D rotation = new Transform3D();
      rotation.rotX(-Math.PI / 2);
      Transform3D transform = new Transform3D();
      transform.setTranslation(new Vector3f(0, 5, 0));
      transform.mul(rotation);
      viewPlatformTransform.setTransform(transform);
      // Use parallel projection
      Viewer viewer = viewingPlatform.getViewers() [0];      
      javax.media.j3d.View view = viewer.getView();
      view.setProjectionPolicy(javax.media.j3d.View.PARALLEL_PROJECTION);
      sceneRoot = new BranchGroup();
      // Prepare scene root
      sceneRoot.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
      sceneRoot.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
      sceneRoot.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
      Background background = new Background(1.1f, 1.1f, 1.1f);
      background.setCapability(Background.ALLOW_COLOR_WRITE);
      background.setApplicationBounds(new BoundingBox(new Point3d(-1.1, -1.1, -1.1), new Point3d(1.1, 1.1, 1.1)));
      sceneRoot.addChild(background);
      Light [] lights = {new DirectionalLight(new Color3f(0.6f, 0.6f, 0.6f), new Vector3f(1.5f, -0.8f, -1)),         
                         new DirectionalLight(new Color3f(0.6f, 0.6f, 0.6f), new Vector3f(-1.5f, -0.8f, -1)), 
                         new DirectionalLight(new Color3f(0.6f, 0.6f, 0.6f), new Vector3f(0, -0.8f, 1)), 
                         new AmbientLight(new Color3f(0.2f, 0.2f, 0.2f))};
      for (Light light : lights) {
        light.setInfluencingBounds(new BoundingBox(new Point3d(-1.1, -1.1, -1.1), new Point3d(1.1, 1.1, 1.1)));
        sceneRoot.addChild(light);
      }
      universe.addBranchGraph(sceneRoot);
      iconsCreationExecutor = Executors.newSingleThreadExecutor();
    }
    
    /**
     * Creates a top view icon proxy for a <code>piece</code> of furniture.
     * @param piece an object containing a 3D content
     * @param waitingComponent a waiting component. If <code>null</code>, the returned icon will
     *            be read immediately in the current thread.
     */
    public PieceOfFurnitureModelIcon(final HomePieceOfFurniture piece, 
                                     final Component waitingComponent) {
      super(IconManager.getInstance().getWaitIcon());
      ModelManager.getInstance().loadModel(piece.getModel(), waitingComponent == null,
          new ModelManager.ModelObserver() {
            public void modelUpdated(final BranchGroup modelNode) {
              // Now that it's sure that 3D model exists
              // work on a clone of the piece centered at the origin
              // with the same size to get a correct texture mapping
              final HomePieceOfFurniture normalizedPiece = piece.clone();
              if (normalizedPiece.isResizable()) {
                normalizedPiece.setModelMirrored(false);
              }
              normalizedPiece.setX(0);
              normalizedPiece.setY(0);
              normalizedPiece.setElevation(-normalizedPiece.getHeight() / 2);
              normalizedPiece.setAngle(0);
              final float pieceWidth = normalizedPiece.getWidth();
              final float pieceDepth = normalizedPiece.getDepth();
              final float pieceHeight = normalizedPiece.getHeight();
              if (waitingComponent != null) {
                // Generate icons in an other thread to avoid blocking EDT during offscreen rendering
                iconsCreationExecutor.execute(new Runnable() {
                    public void run() {
                      setIcon(createIcon(new HomePieceOfFurniture3D(normalizedPiece, null, true, true),
                          pieceWidth, pieceDepth, pieceHeight));
                      waitingComponent.repaint();
                    }
                  });
              } else {
                setIcon(createIcon(new HomePieceOfFurniture3D(normalizedPiece, null, true, true),
                    pieceWidth, pieceDepth, pieceHeight));
              }
            }
        
            public void modelError(Exception ex) {
              // Too bad, we'll use errorIcon
              setIcon(IconManager.getInstance().getErrorIcon());                
              if (waitingComponent != null) {
                waitingComponent.repaint();
              }
            }
          });
    }
    
    /**
     * Returns an icon created and scaled from piece model content.
     */
    private Icon createIcon(BranchGroup modelNode,  
                            float pieceWidth, float pieceDepth, float pieceHeight) {
      // Add piece model scene to a normalized transform group
      Transform3D scaleTransform = new Transform3D();
      scaleTransform.setScale(new Vector3d(2 / pieceWidth, 2 / pieceHeight, 2 / pieceDepth));
      TransformGroup modelTransformGroup = new TransformGroup();
      modelTransformGroup.setTransform(scaleTransform);
      modelTransformGroup.addChild(modelNode);
      // Replace model textures by clones because Java 3D doesn't accept all the time 
      // to share textures between offscreen and onscreen environments 
      cloneTexture(modelNode, new HashMap<Texture, Texture>());

      BranchGroup model = new BranchGroup();
      model.setCapability(BranchGroup.ALLOW_DETACH);
      model.addChild(modelTransformGroup);
      sceneRoot.addChild(model);
      
      // Render scene with a white background
      Background background = (Background)sceneRoot.getChild(0);        
      background.setColor(1, 1, 1);
      canvas3D.renderOffScreenBuffer();
      canvas3D.waitForOffScreenRendering();          
      BufferedImage imageWithWhiteBackgound = canvas3D.getOffScreenBuffer().getImage();
      int [] imageWithWhiteBackgoundPixels = getImagePixels(imageWithWhiteBackgound);
      
      // Render scene with a black background
      background.setColor(0, 0, 0);
      canvas3D.renderOffScreenBuffer();
      canvas3D.waitForOffScreenRendering();          
      BufferedImage imageWithBlackBackgound = canvas3D.getOffScreenBuffer().getImage();
      int [] imageWithBlackBackgoundPixels = getImagePixels(imageWithBlackBackgound);
      
      // Create an image with transparent pixels where model isn't drawn
      for (int i = 0; i < imageWithBlackBackgoundPixels.length; i++) {
        if (imageWithBlackBackgoundPixels [i] != imageWithWhiteBackgoundPixels [i]
            && imageWithBlackBackgoundPixels [i] == 0xFF000000
            && imageWithWhiteBackgoundPixels [i] == 0xFFFFFFFF) {
          imageWithWhiteBackgoundPixels [i] = 0;
        }           
      }
      
      sceneRoot.removeChild(model);
      return new ImageIcon(Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(
          imageWithWhiteBackgound.getWidth(), imageWithWhiteBackgound.getHeight(), 
          imageWithWhiteBackgoundPixels, 0, imageWithWhiteBackgound.getWidth())));
    }

    /**
     * Replace the textures set on node shapes by clones. 
     */
    private void cloneTexture(Node node, Map<Texture, Texture> replacedTextures) {
      if (node instanceof Group) {
        // Enumerate children
        Enumeration<?> enumeration = ((Group)node).getAllChildren(); 
        while (enumeration.hasMoreElements()) {
          cloneTexture((Node)enumeration.nextElement(), replacedTextures);
        }
      } else if (node instanceof Link) {
        cloneTexture(((Link)node).getSharedGroup(), replacedTextures);
      } else if (node instanceof Shape3D) {
        Appearance appearance = ((Shape3D)node).getAppearance();
        if (appearance != null) {
          Texture texture = appearance.getTexture();
          if (texture != null) {
            Texture replacedTexture = replacedTextures.get(texture);
            if (replacedTexture == null) {
              replacedTexture = (Texture)texture.cloneNodeComponent(false);
              replacedTextures.put(texture, replacedTexture);
            }
            appearance.setTexture(replacedTexture);
          }
        }
      } 
    }

    /**
     * Returns the pixels of the given <code>image</code>.
     */
    private int [] getImagePixels(BufferedImage image) {
      if (image.getType() == BufferedImage.TYPE_INT_RGB
          || image.getType() == BufferedImage.TYPE_INT_ARGB) {
        // Use a faster way to get pixels 
        return (int [])image.getRaster().getDataElements(0, 0, image.getWidth(), image.getHeight(), null);
      } else {
        return image.getRGB(0, 0, image.getWidth(), image.getHeight(), null,
            0, image.getWidth());
      }
    }
  }
}
