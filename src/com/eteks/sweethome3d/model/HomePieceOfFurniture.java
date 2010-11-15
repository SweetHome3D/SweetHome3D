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
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Collator;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * A piece of furniture in {@linkplain Home home}.
 * @author Emmanuel Puybaret
 */
public class HomePieceOfFurniture implements PieceOfFurniture, Serializable, Selectable {
  private static final long serialVersionUID = 1L;
  
  private static final double TWICE_PI = 2 * Math.PI;
  
  /**
   * The properties of a piece of furniture that may change. <code>PropertyChangeListener</code>s added 
   * to a piece of furniture will be notified under a property name equal to the string value of one these properties.
   */
  public enum Property {NAME, NAME_VISIBLE, NAME_X_OFFSET, NAME_Y_OFFSET, NAME_STYLE,
      DESCRIPTION, WIDTH, DEPTH, HEIGHT, COLOR, TEXTURE, VISIBLE, X, Y, ELEVATION, ANGLE, MODEL_MIRRORED, MOVABLE};
  
  /** 
   * The properties on which home furniture may be sorted.  
   */
  public enum SortableProperty {CATALOG_ID, NAME, WIDTH, DEPTH, HEIGHT, MOVABLE, 
                                DOOR_OR_WINDOW, COLOR, TEXTURE, VISIBLE, X, Y, ELEVATION, ANGLE,
                                PRICE, VALUE_ADDED_TAX, VALUE_ADDED_TAX_PERCENTAGE, PRICE_VALUE_ADDED_TAX_INCLUDED};
  private static final Map<SortableProperty, Comparator<HomePieceOfFurniture>> SORTABLE_PROPERTY_COMPARATORS;
  private static final float [][] IDENTITY = new float [][] {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
  
  static {
    final Collator collator = Collator.getInstance();
    // Init piece property comparators
    SORTABLE_PROPERTY_COMPARATORS = new HashMap<SortableProperty, Comparator<HomePieceOfFurniture>>();
    SORTABLE_PROPERTY_COMPARATORS.put(SortableProperty.CATALOG_ID, new Comparator<HomePieceOfFurniture>() {
        public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
          if (piece1.catalogId == null) {
            return -1;
          } else if (piece2.catalogId == null) {
            return 1; 
          } else {
            return collator.compare(piece1.catalogId, piece2.catalogId);
          }
        }
      });
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
    SORTABLE_PROPERTY_COMPARATORS.put(SortableProperty.TEXTURE, new Comparator<HomePieceOfFurniture>() {
        public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
          if (piece1.texture == null) {
            return -1;
          } else if (piece2.texture == null) {
            return 1; 
          } else {
            return collator.compare(piece1.texture.getName(), piece2.texture.getName());
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
    SORTABLE_PROPERTY_COMPARATORS.put(SortableProperty.ELEVATION, new Comparator<HomePieceOfFurniture>() {
        public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
          return HomePieceOfFurniture.compare(piece1.elevation, piece2.elevation);
        }
      });
    SORTABLE_PROPERTY_COMPARATORS.put(SortableProperty.ANGLE, new Comparator<HomePieceOfFurniture>() {
        public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
          return HomePieceOfFurniture.compare(piece1.angle, piece2.angle);
        }
      });
    SORTABLE_PROPERTY_COMPARATORS.put(SortableProperty.PRICE, new Comparator<HomePieceOfFurniture>() {
        public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
          return HomePieceOfFurniture.compare(piece1.price, piece2.price);
        }
      });
    SORTABLE_PROPERTY_COMPARATORS.put(SortableProperty.VALUE_ADDED_TAX_PERCENTAGE, new Comparator<HomePieceOfFurniture>() {
        public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
          return HomePieceOfFurniture.compare(piece1.valueAddedTaxPercentage, piece2.valueAddedTaxPercentage);
        }
      });
    SORTABLE_PROPERTY_COMPARATORS.put(SortableProperty.VALUE_ADDED_TAX, new Comparator<HomePieceOfFurniture>() {
        public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
          return HomePieceOfFurniture.compare(piece1.getValueAddedTax(), piece2.getValueAddedTax());
        }
      });
    SORTABLE_PROPERTY_COMPARATORS.put(SortableProperty.PRICE_VALUE_ADDED_TAX_INCLUDED, new Comparator<HomePieceOfFurniture>() {
        public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
          return HomePieceOfFurniture.compare(piece1.getPriceValueAddedTaxIncluded(), piece2.getPriceValueAddedTaxIncluded());
        }
      });
  }
  
  private static int compare(float value1, float value2) {
    return value1 < value2 
               ? -1
               : (value1 == value2
                   ? 0 : 1);
  }
  
  private static int compare(boolean value1, boolean value2) {
    return value1 == value2 
               ? 0
               : (value1 ? -1 : 1);
  }
  
  private static int compare(BigDecimal value1, BigDecimal value2) {
    if (value1 == null) {
      return -1;
    } else if (value2 == null) {
      return 1; 
    } else {
      return value1.compareTo(value2);
    }
  }
  
  private String                 catalogId;
  private String                 name;
  private boolean                nameVisible;
  private float                  nameXOffset;
  private float                  nameYOffset;
  private TextStyle              nameStyle;
  private String                 description;
  private Content                icon;
  private Content                planIcon;
  private Content                model;
  private float                  width;
  private float                  depth;
  private float                  height;
  private float                  elevation;
  private boolean                movable;
  private boolean                doorOrWindow;
  private Integer                color;
  private HomeTexture            texture;
  private float [][]             modelRotation;
  private boolean                backFaceShown;
  private boolean                resizable;
  private boolean                deformable;
  private BigDecimal             price;
  private BigDecimal             valueAddedTaxPercentage;
  private boolean                visible;
  private float                  x;
  private float                  y;
  private float                  angle;
  private boolean                modelMirrored;

  private transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
  private transient Shape shapeCache;


  /**
   * Creates a home piece of furniture from an existing piece.
   * @param piece the piece from which data are copied
   */
  public HomePieceOfFurniture(PieceOfFurniture piece) {
    this.name = piece.getName();
    this.description = piece.getDescription();
    this.icon = piece.getIcon();
    this.planIcon = piece.getPlanIcon();
    this.model = piece.getModel();
    this.width = piece.getWidth();
    this.depth = piece.getDepth();
    this.height = piece.getHeight();
    this.elevation = piece.getElevation();
    this.movable = piece.isMovable();
    this.doorOrWindow = piece.isDoorOrWindow();
    this.color = piece.getColor();
    this.modelRotation = piece.getModelRotation();
    this.backFaceShown = piece.isBackFaceShown();
    this.resizable = piece.isResizable();
    this.deformable = piece.isDeformable();
    this.price = piece.getPrice();
    this.valueAddedTaxPercentage = piece.getValueAddedTaxPercentage();
    if (piece instanceof HomePieceOfFurniture) {
      HomePieceOfFurniture homePiece = 
          (HomePieceOfFurniture)piece;
      this.catalogId = homePiece.getCatalogId();
      this.nameVisible = homePiece.isNameVisible();
      this.nameXOffset = homePiece.getNameXOffset();
      this.nameYOffset = homePiece.getNameYOffset();
      this.nameStyle = homePiece.getNameStyle();
      this.visible = homePiece.isVisible();
      this.angle = homePiece.getAngle();
      this.x = homePiece.getX();
      this.y = homePiece.getY();
      this.modelMirrored = homePiece.isModelMirrored();
      this.texture = homePiece.getTexture();
    } else {
      if (piece instanceof CatalogPieceOfFurniture) {
        this.catalogId = ((CatalogPieceOfFurniture)piece).getId();
      }      
      this.visible = true;
      this.x = this.width / 2;
      this.y = this.depth / 2;
    }
  }

  /**
   * Initializes new piece fields to their default values 
   * and reads piece from <code>in</code> stream with default reading method.
   */
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    this.modelRotation = IDENTITY;
    this.resizable = true;
    this.deformable = true;
    this.propertyChangeSupport = new PropertyChangeSupport(this);
    in.defaultReadObject();
  }

  /**
   * Adds the property change <code>listener</code> in parameter to this piece.
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    this.propertyChangeSupport.addPropertyChangeListener(listener);
  }

  /**
   * Removes the property change <code>listener</code> in parameter from this piece.
   */
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    this.propertyChangeSupport.removePropertyChangeListener(listener);
  }

  /**
   * Returns the catalog ID of this piece of furniture or <code>null</code> if it doesn't exist.
   */
  public String getCatalogId() {
    return this.catalogId;
  }
  
  /**
   * Returns the name of this piece of furniture.
   */
  public String getName() {
    return this.name;
  }

  /**
   * Sets the name of this piece of furniture. Once this piece is updated, 
   * listeners added to this piece will receive a change notification.
   */
  public void setName(String name) {
    if (name != this.name
        || (name != null && !name.equals(this.name))) {
      String oldName = this.name;
      this.name = name;
      this.propertyChangeSupport.firePropertyChange(Property.NAME.name(), oldName, name);
    }
  }
   
  /**
   * Returns whether the name of this piece should be drawn or not. 
   */
  public boolean isNameVisible() {
    return this.nameVisible;  
  }
  
  /**
   * Sets whether the name of this piece is visible or not. Once this piece of furniture 
   * is updated, listeners added to this piece will receive a change notification.
   */
  public void setNameVisible(boolean nameVisible) {
    if (nameVisible != this.nameVisible) {
      this.nameVisible = nameVisible;
      this.propertyChangeSupport.firePropertyChange(Property.NAME_VISIBLE.name(), !nameVisible, nameVisible);
    }
  }
  
  /**
   * Returns the distance along x axis applied to piece abscissa to display piece name. 
   */
  public float getNameXOffset() {
    return this.nameXOffset;  
  }
  
  /**
   * Sets the distance along x axis applied to piece abscissa to display piece name. 
   * Once this piece is updated, listeners added to this piece will receive a change notification.
   */
  public void setNameXOffset(float nameXOffset) {
    if (nameXOffset != this.nameXOffset) {
      float oldNameXOffset = this.nameXOffset;
      this.nameXOffset = nameXOffset;
      this.propertyChangeSupport.firePropertyChange(Property.NAME_X_OFFSET.name(), oldNameXOffset, nameXOffset);
    }
  }
  
  /**
   * Returns the distance along y axis applied to piece ordinate 
   * to display piece name.
   */
  public float getNameYOffset() {
    return this.nameYOffset;  
  }

  /**
   * Sets the distance along y axis applied to piece ordinate to display piece name. 
   * Once this piece is updated, listeners added to this piece will receive a change notification.
   */
  public void setNameYOffset(float nameYOffset) {
    if (nameYOffset != this.nameYOffset) {
      float oldNameYOffset = this.nameYOffset;
      this.nameYOffset = nameYOffset;
      this.propertyChangeSupport.firePropertyChange(Property.NAME_Y_OFFSET.name(), oldNameYOffset, nameYOffset);
    }
  }

  /**
   * Returns the text style used to display piece name.
   */
  public TextStyle getNameStyle() {
    return this.nameStyle;  
  }

  /**
   * Sets the text style used to display piece name.
   * Once this piece is updated, listeners added to this piece will receive a change notification.
   */
  public void setNameStyle(TextStyle nameStyle) {
    if (nameStyle != this.nameStyle) {
      TextStyle oldNameStyle = this.nameStyle;
      this.nameStyle = nameStyle;
      this.propertyChangeSupport.firePropertyChange(Property.NAME_STYLE.name(), oldNameStyle, nameStyle);
    }
  }
  
  /**
   * Returns the description of this piece of furniture.
   * The returned value may be <code>null</code>.
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * Sets the description of this piece of furniture. Once this piece is updated, 
   * listeners added to this piece will receive a change notification.
   */
  public void setDescription(String description) {
    if (description != this.description
        || (description != null && !description.equals(this.description))) {
      String oldDescription = this.description;
      this.description = description;
      this.propertyChangeSupport.firePropertyChange(Property.DESCRIPTION.name(), oldDescription, description);
    }
  }
   
  /**
   * Returns the depth of this piece of furniture.
   */
  public float getDepth() {
    return this.depth;
  }

  /**
   * Sets the depth of this piece of furniture. Once this piece is updated, 
   * listeners added to this piece will receive a change notification.
   * @throws IllegalStateException if this piece of furniture isn't resizable
   */
  public void setDepth(float depth) {
    if (isResizable()) {
      if (depth != this.depth) {
        float oldDepth = this.depth;
        this.depth = depth;
        this.shapeCache = null;
        this.propertyChangeSupport.firePropertyChange(Property.DEPTH.name(), oldDepth, depth);
      }
    } else {
      throw new IllegalStateException("Piece isn't resizable");
    }
  }

  /**
   * Returns the height of this piece of furniture.
   */
  public float getHeight() {
    return this.height;
  }

  /**
   * Sets the height of this piece of furniture. Once this piece is updated, 
   * listeners added to this piece will receive a change notification.
   * @throws IllegalStateException if this piece of furniture isn't resizable
   */
  public void setHeight(float height) {
    if (isResizable()) {
      if (height != this.height) {
        float oldHeight = this.height;
        this.height = height;
        this.shapeCache = null;
        this.propertyChangeSupport.firePropertyChange(Property.HEIGHT.name(), oldHeight, height);
      }
    } else {
      throw new IllegalStateException("Piece isn't resizable");
    }
  }

  /**
   * Returns the width of this piece of furniture.
   */
  public float getWidth() {
    return this.width;
  }

  /**
   * Sets the width of this piece of furniture. Once this piece is updated, 
   * listeners added to this piece will receive a change notification.
   * @throws IllegalStateException if this piece of furniture isn't resizable
   */
  public void setWidth(float width) {
    if (isResizable()) {
      if (width != this.width) {
        float oldWidth = this.width;
        this.width = width;
        this.shapeCache = null;
        this.propertyChangeSupport.firePropertyChange(Property.WIDTH.name(), oldWidth, width);
      }
    } else {
      throw new IllegalStateException("Piece isn't resizable");
    }
  }

  /**
   * Returns the elevation of the bottom of this piece of furniture. 
   */
  public float getElevation() {
    return this.elevation;
  }

  /**
   * Sets the elevation of this piece of furniture. Once this piece is updated, 
   * listeners added to this piece will receive a change notification.
   */
  public void setElevation(float elevation) {
    if (elevation != this.elevation) {
      float oldElevation = this.elevation;
      this.elevation = elevation;
      this.propertyChangeSupport.firePropertyChange(Property.ELEVATION.name(), oldElevation, elevation);
    }
  }

  /**
   * Returns <code>true</code> if this piece of furniture is movable.
   */
  public boolean isMovable() {
    return this.movable;
  }

  public void setMovable(boolean movable) {
    if (movable != this.movable) {
      this.movable = movable;
      this.propertyChangeSupport.firePropertyChange(Property.MOVABLE.name(), !movable, movable);
    }
  }
  
  /**
   * Returns <code>true</code> if this piece of furniture is a door or a window.
   * As this method existed before {@linkplain HomeDoorOrWindow HomeDoorOrWindow} class,
   * you shouldn't rely on the value returned by this method to guess if a piece
   * is an instance of <code>DoorOrWindow</code> class.
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
   * Returns the icon of this piece of furniture displayed in plan or <code>null</code>.
   * @since 2.2
   */
  public Content getPlanIcon() {
    return this.planIcon;
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
   * Sets the color of this piece of furniture or <code>null</code> if piece color is unchanged. 
   * Once this piece is updated, listeners added to this piece will receive a change notification.
   */
  public void setColor(Integer color) {
    if (color != this.color
        || (color != null && !color.equals(this.color))) {
      Integer oldColor = this.color;
      this.color = color;
      this.propertyChangeSupport.firePropertyChange(Property.COLOR.name(), oldColor, color);
    }
  }

  /**
   * Returns the texture of this piece of furniture.
   * @return the texture of the piece or <code>null</code> if piece texture is unchanged.
   * @since 2.3
   */
  public HomeTexture getTexture() {
    return this.texture;
  }
  
  /**
   * Sets the texture of this piece of furniture or <code>null</code> if piece texture is unchanged. 
   * Once this piece is updated, listeners added to this piece will receive a change notification.
   * @since 2.3
   */
  public void setTexture(HomeTexture texture) {
    if (texture != this.texture
        || (texture != null && !texture.equals(this.texture))) {
      HomeTexture oldTexture = this.texture;
      this.texture = texture;
      this.propertyChangeSupport.firePropertyChange(Property.TEXTURE.name(), oldTexture, texture);
    }
  }

  /**
   * Returns <code>true</code> if this piece is resizable.
   */
  public boolean isResizable() {
    return this.resizable;    
  }
  
  /**
   * Returns <code>true</code> if this piece is deformable.
   * @since 3.0
   */
  public boolean isDeformable() {
    return this.deformable;    
  }

  /**
   * Returns the price of this piece of furniture or <code>null</code>. 
   */
  public BigDecimal getPrice() {
    return this.price;
  }
  
  /**
   * Returns the Value Added Tax percentage applied to the price of this piece of furniture. 
   */
  public BigDecimal getValueAddedTaxPercentage() {
    return this.valueAddedTaxPercentage;
  }

  /**
   * Returns the Value Added Tax applied to the price of this piece of furniture. 
   */
  public BigDecimal getValueAddedTax() {
    if (this.price != null && this.valueAddedTaxPercentage != null) {
      return this.price.multiply(this.valueAddedTaxPercentage).
          setScale(this.price.scale(), RoundingMode.HALF_UP);
    } else {
      return null;
    }
  }

  /**
   * Returns the price of this piece of furniture, Value Added Tax included. 
   */
  public BigDecimal getPriceValueAddedTaxIncluded() {
    if (this.price != null && this.valueAddedTaxPercentage != null) {
      return this.price.add(getValueAddedTax());
    } else {
      return this.price;
    }
  }

  /**
   * Returns <code>true</code> if this piece of furniture is visible.
   */
  public boolean isVisible() {
    return this.visible;
  }
  
  /**
   * Sets whether this piece of furniture is visible or not. Once this piece is updated, 
   * listeners added to this piece will receive a change notification.
   */
  public void setVisible(boolean visible) {
    if (visible != this.visible) {
      this.visible = visible;
      this.propertyChangeSupport.firePropertyChange(Property.VISIBLE.name(), !visible, visible);
    }
  }

  /**
   * Returns the abscissa of the center of this piece of furniture.
   */
  public float getX() {
    return this.x;
  }

  /**
   * Sets the abscissa of the center of this piece. Once this piece is updated, 
   * listeners added to this piece will receive a change notification.
   */
  public void setX(float x) {
    if (x != this.x) {
      float oldX = this.x;
      this.x = x;
      this.shapeCache = null;
      this.propertyChangeSupport.firePropertyChange(Property.X.name(), oldX, x);
    }
  }
  
  /**
   * Returns the ordinate of the center of this piece of furniture.
   */
  public float getY() {
    return this.y;
  }

  /**
   * Sets the ordinate of the center of this piece. Once this piece is updated, 
   * listeners added to this piece will receive a change notification.
   */
  public void setY(float y) {
    if (y != this.y) {
      float oldY = this.y;
      this.y = y;
      this.shapeCache = null;
      this.propertyChangeSupport.firePropertyChange(Property.Y.name(), oldY, y);
    }
  }

  /**
   * Returns the angle in radians of this piece of furniture. 
   */
  public float getAngle() {
    return this.angle;
  }

  /**
   * Sets the angle of this piece. Once this piece is updated, 
   * listeners added to this piece will receive a change notification.
   */
  public void setAngle(float angle) {
    // Ensure angle is always positive and between 0 and 2 PI
    angle = (float)((angle % TWICE_PI + TWICE_PI) % TWICE_PI);
    if (angle != this.angle) {
      float oldAngle = this.angle;
      this.angle = angle;
      this.shapeCache = null;
      this.propertyChangeSupport.firePropertyChange(Property.ANGLE.name(), oldAngle, angle);
    }
  }

  /**
   * Returns <code>true</code> if the model of this piece should be mirrored.
   */
  public boolean isModelMirrored() {
    return this.modelMirrored;
  }

  /**
   * Sets whether the model of this piece of furniture is mirrored or not. Once this piece is updated, 
   * listeners added to this piece will receive a change notification.
   * @throws IllegalStateException if this piece of furniture isn't resizable
   */
  public void setModelMirrored(boolean modelMirrored) {
    if (isResizable()) {
      if (modelMirrored != this.modelMirrored) {
        this.modelMirrored = modelMirrored;
        this.propertyChangeSupport.firePropertyChange(Property.MODEL_MIRRORED.name(), 
            !modelMirrored, modelMirrored);
      }
    } else {
      throw new IllegalStateException("Piece isn't resizable");
    }
  }

  /**
   * Returns the rotation 3 by 3 matrix of this piece of furniture that ensures 
   * its model is correctly oriented.
   */
  public float [][] getModelRotation() {
    // Return a deep copy to avoid any misuse of piece data
    return new float [][] {{this.modelRotation[0][0], this.modelRotation[0][1], this.modelRotation[0][2]},
                           {this.modelRotation[1][0], this.modelRotation[1][1], this.modelRotation[1][2]},
                           {this.modelRotation[2][0], this.modelRotation[2][1], this.modelRotation[2][2]}};
  }

  /**
   * Returns <code>true</code> if the back face of the piece of furniture
   * model should be displayed.
   */
  public boolean isBackFaceShown() {
    return this.backFaceShown;
  }
  
  /**
   * Returns the points of each corner of a piece.
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
    if (margin == 0) {
      return getShape().contains(x, y);
    } else {
      return getShape().intersects(x - margin, y - margin, 2 * margin, 2 * margin);
    }
  }
  
  /**
   * Returns <code>true</code> if one of the corner of this piece is 
   * the point at (<code>x</code>, <code>y</code>) with a given <code>margin</code>.
   */
  public boolean isPointAt(float x, float y, float margin) {
    for (float [] point : getPoints()) {
      if (Math.abs(x - point[0]) <= margin && Math.abs(y - point[1]) <= margin) {
        return true;
      }
    } 
    return false;
  }

  /**
   * Returns <code>true</code> if the top left point of this piece is 
   * the point at (<code>x</code>, <code>y</code>) with a given <code>margin</code>.
   */
  public boolean isTopLeftPointAt(float x, float y, float margin) {
    float [][] points = getPoints();
    return Math.abs(x - points[0][0]) <= margin && Math.abs(y - points[0][1]) <= margin;
  }

  /**
   * Returns <code>true</code> if the top right point of this piece is 
   * the point at (<code>x</code>, <code>y</code>) with a given <code>margin</code>.
   */
  public boolean isTopRightPointAt(float x, float y, float margin) {
    float [][] points = getPoints();
    return Math.abs(x - points[1][0]) <= margin && Math.abs(y - points[1][1]) <= margin;
  }

  /**
   * Returns <code>true</code> if the bottom left point of this piece is 
   * the point at (<code>x</code>, <code>y</code>) with a given <code>margin</code>.
   */
  public boolean isBottomLeftPointAt(float x, float y, float margin) {
    float [][] points = getPoints();
    return Math.abs(x - points[3][0]) <= margin && Math.abs(y - points[3][1]) <= margin;
  }

  /**
   * Returns <code>true</code> if the bottom right point of this piece is 
   * the point at (<code>x</code>, <code>y</code>) with a given <code>margin</code>.
   */
  public boolean isBottomRightPointAt(float x, float y, float margin) {
    float [][] points = getPoints();
    return Math.abs(x - points[2][0]) <= margin && Math.abs(y - points[2][1]) <= margin;
  }

  /**
   * Returns <code>true</code> if the center point at which is displayed the name 
   * of this piece is equal to the point at (<code>x</code>, <code>y</code>) 
   * with a given <code>margin</code>. 
   */
  public boolean isNameCenterPointAt(float x, float y, float margin) {
    return Math.abs(x - getX() - getNameXOffset()) <= margin 
        && Math.abs(y - getY() - getNameYOffset()) <= margin;
  }

  /**
   * Returns the shape matching this piece.
   */
  private Shape getShape() {
    if (this.shapeCache == null) {
      // Create the rectangle that matches piece bounds
      Rectangle2D pieceRectangle = new Rectangle2D.Float(
          getX() - getWidth() / 2,
          getY() - getDepth() / 2,
          getWidth(), getDepth());
      // Apply rotation to the rectangle
      AffineTransform rotation = new AffineTransform();
      rotation.setToRotation(getAngle(), getX(), getY());
      PathIterator it = pieceRectangle.getPathIterator(rotation);
      GeneralPath pieceShape = new GeneralPath();
      pieceShape.append(it, false);
      // Cache shape
      this.shapeCache = pieceShape;
    }
    return this.shapeCache;
  }

  /**
   * Moves this piece of (<code>dx</code>, <code>dy</code>) units.
   */
  public void move(float dx, float dy) {
    setX(getX() + dx);
    setY(getY() + dy);
  }
  
  /**
   * Returns a clone of this piece.
   */
  @Override
  public HomePieceOfFurniture clone() {
    try {
      HomePieceOfFurniture clone = (HomePieceOfFurniture)super.clone();
      clone.propertyChangeSupport = new PropertyChangeSupport(clone);
      return clone;
    } catch (CloneNotSupportedException ex) {
      throw new IllegalStateException("Super class isn't cloneable"); 
    }
  }

  /**
   * Returns a comparator that compares furniture on a given <code>property</code> in ascending order.
   */
  public static Comparator<HomePieceOfFurniture> getFurnitureComparator(SortableProperty property) {
    return SORTABLE_PROPERTY_COMPARATORS.get(property);    
  }
}
