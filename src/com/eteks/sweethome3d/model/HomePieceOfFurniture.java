/*
 * HomePieceOfFurniture.java 15 mai 2006
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
package com.eteks.sweethome3d.model;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.text.Collator;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * A piece of furniture in {@link Home home}.
 * @author Emmanuel Puybaret
 */
public class HomePieceOfFurniture implements PieceOfFurniture {
  private static final long serialVersionUID = 1L;
  /** 
   * Properties on which home furniture may be sorted.  
   */
  public enum SortableProperty {NAME, WIDTH, DEPTH, HEIGHT, MOVABLE, 
                                DOOR_OR_WINDOW, COLOR, VISIBLE, X, Y, ANGLE};
  private static final Map<SortableProperty, Comparator<HomePieceOfFurniture>> SORTABLE_PROPERTY_COMPARATORS;
  
  static {
    final Collator collator = Collator.getInstance();
    // Init piece property comparators
    SORTABLE_PROPERTY_COMPARATORS = new HashMap<SortableProperty, Comparator<HomePieceOfFurniture>>();
    SORTABLE_PROPERTY_COMPARATORS.put(SortableProperty.NAME, new Comparator<HomePieceOfFurniture>() {
        public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
          return collator.compare(piece1.name, piece2.name);
        }
      });
    SORTABLE_PROPERTY_COMPARATORS.put(SortableProperty.WIDTH, new Comparator<HomePieceOfFurniture>() {
        public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
          return HomePieceOfFurniture.compare(piece1.width, piece2.width);
        }
      });
    SORTABLE_PROPERTY_COMPARATORS.put(SortableProperty.HEIGHT, new Comparator<HomePieceOfFurniture>() {
        public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
          return HomePieceOfFurniture.compare(piece1.height, piece2.height);
        }
      });
    SORTABLE_PROPERTY_COMPARATORS.put(SortableProperty.DEPTH, new Comparator<HomePieceOfFurniture>() {
        public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
          return HomePieceOfFurniture.compare(piece1.depth, piece2.depth);
        }
      });
    SORTABLE_PROPERTY_COMPARATORS.put(SortableProperty.MOVABLE, new Comparator<HomePieceOfFurniture>() {
        public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
          return HomePieceOfFurniture.compare(piece1.movable, piece2.movable);
        }
      });
    SORTABLE_PROPERTY_COMPARATORS.put(SortableProperty.DOOR_OR_WINDOW, new Comparator<HomePieceOfFurniture>() {
        public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
          return HomePieceOfFurniture.compare(piece1.doorOrWindow, piece2.doorOrWindow);
        }
      });
    SORTABLE_PROPERTY_COMPARATORS.put(SortableProperty.DOOR_OR_WINDOW, new Comparator<HomePieceOfFurniture>() {
        public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
          return HomePieceOfFurniture.compare(piece1.doorOrWindow, piece2.doorOrWindow);
        }
      });
    SORTABLE_PROPERTY_COMPARATORS.put(SortableProperty.COLOR, new Comparator<HomePieceOfFurniture>() {
        public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
          if (piece1.color == null) {
            return -1;
          } else if (piece2.color == null) {
            return 1; 
          } else {
            return piece1.color - piece2.color;
          }
        }
      });
    SORTABLE_PROPERTY_COMPARATORS.put(SortableProperty.VISIBLE, new Comparator<HomePieceOfFurniture>() {
        public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
          return HomePieceOfFurniture.compare(piece1.visible, piece2.visible);
        }
      });
    SORTABLE_PROPERTY_COMPARATORS.put(SortableProperty.X, new Comparator<HomePieceOfFurniture>() {
        public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
          return HomePieceOfFurniture.compare(piece1.x, piece2.x);
        }
      });
    SORTABLE_PROPERTY_COMPARATORS.put(SortableProperty.Y, new Comparator<HomePieceOfFurniture>() {
        public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
          return HomePieceOfFurniture.compare(piece1.y, piece2.y);
        }
      });
    SORTABLE_PROPERTY_COMPARATORS.put(SortableProperty.ANGLE, new Comparator<HomePieceOfFurniture>() {
        public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
          return HomePieceOfFurniture.compare(piece1.angle, piece2.angle);
        }
      });
  }
  
  private static int compare(float value1, float value2) {
    return value1 < value2 
               ? -1
               : (value1 == value2 ? 0 : 1);
  }
  
  private static int compare(boolean value1, boolean value2) {
    return value1 == value2 
               ? 0
               : (value1 ? -1 : 1);
  }
  
  private String  name;
  private Content icon;
  private Content model;
  private float   width;
  private float   depth;
  private float   height;
  private boolean movable;
  private boolean doorOrWindow;
  private Integer color;
  private boolean visible;
  private float   x;
  private float   y;
  private float   angle;

  /**
   * Creates a home piece of furniture from an existing piece.
   * @param piece the piece from which data are copied
   */
  public HomePieceOfFurniture(PieceOfFurniture piece) {
    this.name = piece.getName();
    this.icon = piece.getIcon();
    this.model = piece.getModel();
    this.width = piece.getWidth();
    this.depth = piece.getDepth();
    this.height = piece.getHeight();
    this.movable = piece.isMovable();
    this.doorOrWindow = piece.isDoorOrWindow();
    if (piece instanceof HomePieceOfFurniture) {
      HomePieceOfFurniture homePiece = 
          (HomePieceOfFurniture)piece;
      this.color = homePiece.getColor();
      this.visible = homePiece.isVisible();
      this.angle = homePiece.getAngle();
      this.x = homePiece.getX();
      this.y = homePiece.getY();
    } else {
      this.visible = true;
      this.x = this.width / 2;
      this.y = this.depth / 2;
    }
  }

  /**
   * Returns the name of this piece of furniture.
   */
   public String getName() {
    return this.name;
  }

  /**
   * Returns the depth of this piece of furniture.
   */
  public float getDepth() {
    return this.depth;
  }

  /**
   * Returns the height of this piece of furniture.
   */
  public float getHeight() {
    return this.height;
  }

  /**
   * Returns the width of this piece of furniture.
   */
  public float getWidth() {
    return this.width;
  }

  /**
   * Returns <code>true</code> if this piece of furniture is movable.
   */
  public boolean isMovable() {
    return this.movable;
  }

  /**
   * Returns <code>true</code> if this piece of furniture is a door or a window.
   */
  public boolean isDoorOrWindow() {
    return this.doorOrWindow;
  }

  /**
   * Returns the icon of this piece of furniture.
   */
  public Content getIcon() {
    return this.icon;
  }

  /**
   * Returns the 3D model of this piece of furniture.
   */
  public Content getModel() {
    return this.model;
  }
  
  /**
   * Returns the color of this piece of furniture.
   * @return the color of the piece as RGB code or <code>null</code> if piece color is unchanged.
   */
  public Integer getColor() {
    return this.color;
  }
  
  /**
   * Returns <code>true</code> if this piece of furniture is visible.
   */
  public boolean isVisible() {
    return this.visible;
  }
  
  /**
   * Returns the abscissa of this piece of furniture.
   */
  public float getX() {
    return this.x;
  }

  /**
   * Sets the abscissa of this piece.
   */
  void setX(float x) {
    this.x = x;
  }
  /**
   * Returns the ordinate of this piece of furniture.
   */
  public float getY() {
    return this.y;
  }

  /**
   * Sets the ordinate of this piece.
   */
  void setY(float y) {
    this.y = y;
  }

  /**
   * Returns the angle in radians of this piece of furniture. 
   */
  public float getAngle() {
    return this.angle;
  }

  /**
   * Sets the angle of this piece.
   */
  void setAngle(float angle) {
    this.angle = angle;
  }

  /**
   * Returns the points of a each corner of a piece.
   * @return an array of the 4 (x,y) coordinates of the piece corners.
   */
  public float [][] getPoints() {
    float [][] piecePoints = new float[4][2];
    PathIterator it = getShape().getPathIterator(null);
    for (int i = 0; i < piecePoints.length; i++) {
      it.currentSegment(piecePoints [i]);
      it.next();
    }
    return piecePoints;
  }
  
  /**
   * Returns <code>true</code> if this piece intersects
   * with the horizontal rectangle which opposite corners are at points
   * (<code>x0</code>, <code>y0</code>) and (<code>x1</code>, <code>y1</code>).
   */
  public boolean intersectsRectangle(float x0, float y0, 
                                     float x1, float y1) {
    Rectangle2D rectangle = new Rectangle2D.Float(x0, y0, 0, 0);
    rectangle.add(x1, y1);
    return getShape().intersects(rectangle);
  }
  
  /**
   * Returns <code>true</code> if this piece contains 
   * the point at (<code>x</code>, <code>y</code>)
   * with a given <code>margin</code>.
   */
  public boolean containsPoint(float x, float y, float margin) {
    return getShape().intersects(x - margin, y - margin, 2 * margin, 2 * margin);
  }
  
  /**
   * Returns <code>true</code> if one of the vertex of this piece vertex is 
   * the point at (<code>x</code>, <code>y</code>)
   * with a given <code>margin</code>.
   */
  public boolean isVertexAt(float x, float y, float margin) {
    for (float [] point : getPoints()) {
      if (Math.abs(x - point[0]) <= margin && Math.abs(y - point[1]) <= margin) {
        return true;
      }
    } 
    return false;
  }

  /**
   * Returns the shape matching this piece.
   */
  private Shape getShape() {
    // Create the rectangle that matches piece bounds
    Rectangle2D pieceRectangle = new Rectangle2D.Float(
        this.x - this.width / 2,
        this.y - this.depth / 2,
        this.width, this.depth);
    // Apply rotation to the rectangle
    AffineTransform rotation = new AffineTransform();
    rotation.setToRotation(this.angle, this.x, this.y);
    PathIterator it = pieceRectangle.getPathIterator(rotation);
    GeneralPath pieceShape = new GeneralPath();
    pieceShape.append(it, false);
    return pieceShape;
  }
  
  /**
   * Returns a comparator that compares furniture on a given <code>property</code> in ascending order.
   */
  public static Comparator<HomePieceOfFurniture> getFurnitureComparator(SortableProperty property) {
    return SORTABLE_PROPERTY_COMPARATORS.get(property);    
  }
}
