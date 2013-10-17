/*
 * TextureButton.java 01 oct. 2008
 *
 * Sweet Home 3D, Copyright (c) 2008 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.border.EtchedBorder;

/**
 * Component displaying an image scaled to fit within its bound.
 * @author Emmanuel Puybaret 
 */
public class ScaledImageComponent extends JComponent {
  private BufferedImage image;
  private boolean       imageEnlargementEnabled;
  private float         scaleMultiplier = 1f;
  
  /**
   * Creates a component that will display no image.
   */
  public ScaledImageComponent() {
    this(null);
  }

  /**
   * Creates a component that will display the given <code>image</code> 
   * at a maximum scale equal to 1.
   */
  public ScaledImageComponent(BufferedImage image) {
    this(image, false);
  }

  /**
   * Creates a component that will display the given <code>image</code> 
   * with no maximum scale if <code>imageEnlargementEnabled</code> is <code>true</code>.
   */
  public ScaledImageComponent(BufferedImage image, 
                              boolean imageEnlargementEnabled) {
    this.image = image;
    this.imageEnlargementEnabled = imageEnlargementEnabled;
    setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
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
      final int defaultPreferredWidth  = 300; 
      final int defaultPreferredHeight = 300; 
      int insetsWidth = insets.left + insets.right;
      int insetsHeight = insets.top + insets.bottom;
      if (this.image == null) {
        return new Dimension(defaultPreferredWidth + insetsWidth, defaultPreferredHeight + insetsHeight);
      } else if (getParent() instanceof JViewport){
        Dimension extentSize = ((JViewport)getParent()).getExtentSize();
        extentSize.width -= insetsWidth;
        extentSize.height -= insetsHeight;
        float widthScale = (float)this.image.getWidth() / extentSize.width;
        float heightScale = (float)this.image.getHeight() / extentSize.height;
        if (widthScale > heightScale) {
          return new Dimension((int)(extentSize.width * this.scaleMultiplier) + insetsWidth, 
              (int)(image.getHeight() / widthScale * this.scaleMultiplier) + insetsHeight);
        } else {
          return new Dimension((int)(this.image.getWidth() / heightScale * this.scaleMultiplier) + insetsWidth, 
              (int)(extentSize.height * this.scaleMultiplier) + insetsHeight);
        }
      } else {
        // Compute the component preferred size in such a way 
        // its bigger dimension (width or height) is 300 * scaleMultiplier pixels
        int maxImagePreferredWith   = defaultPreferredWidth - insetsWidth;
        int maxImagePreferredHeight = defaultPreferredHeight - insetsHeight;
        float widthScale = (float)this.image.getWidth() / maxImagePreferredWith;
        float heightScale = (float)this.image.getHeight() / maxImagePreferredHeight;
        if (widthScale > heightScale) {
          return new Dimension((int)(maxImagePreferredWith * this.scaleMultiplier) + insetsWidth, 
              (int)(image.getHeight() / widthScale * this.scaleMultiplier) + insetsHeight);
        } else {
          return new Dimension((int)(this.image.getWidth() / heightScale * this.scaleMultiplier) + insetsWidth, 
              (int)(maxImagePreferredHeight * this.scaleMultiplier) + insetsHeight);
        }
      }
    }
  }
  
  @Override
  protected void paintComponent(Graphics g) {
    if (isOpaque()) {
      g.setColor(getBackground());
      g.fillRect(0, 0, getWidth(), getHeight());
    }
    paintImage(g, null);
  }

  /**
   * Paints the image with a given <code>composite</code>. 
   * Image is scaled to fill width of the component. 
   */
  protected void paintImage(Graphics g, AlphaComposite composite) {
    if (image != null) {
      Graphics2D g2D = (Graphics2D)g;
      g2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      AffineTransform oldTransform = g2D.getTransform();
      Composite oldComposite = g2D.getComposite();
      Point translation = getImageTranslation();      
      g2D.translate(translation.x, translation.y);
      float scale = getImageScale();
      g2D.scale(scale, scale);    
      
      if (composite != null) {
        g2D.setComposite(composite);
      }
      // Draw image with composite
      g2D.drawImage(this.image, 0, 0, this);
      g2D.setComposite(oldComposite);
      g2D.setTransform(oldTransform);
    }
  }
  
  /**
   * Sets the image drawn by this component.
   */
  public void setImage(BufferedImage image) {
    this.image = image;
    revalidate();
    repaint();
  }

  /**
   * Returns the image drawn by this component.
   */
  public BufferedImage getImage() {
    return this.image;
  }
  
  /**
   * Returns the scale used to draw the image of this component.
   */
  protected float getImageScale() {
    float imageScale;
    if (this.image != null) {
      Dimension dimension;
      if (getParent() instanceof JViewport) {
        dimension = ((JViewport)getParent()).getExtentSize();
      } else {
        dimension = getSize();
      }
      Insets insets = getInsets();
      imageScale = Math.min((float)(dimension.width - insets.left - insets.right) / image.getWidth(), 
          (float)(dimension.height - insets.top - insets.bottom) / image.getHeight());
      if (!this.imageEnlargementEnabled) {
        imageScale = Math.min(1, imageScale);
      }
    } else {
      imageScale = 1;
    }
    return imageScale * this.scaleMultiplier;
  }
  
  /**
   * Returns the multiplier of the default scale.
   */
  public float getScaleMultiplier() {
    return this.scaleMultiplier;
  }
  
  /**
   * Sets the multiplier of the default scale.
   */
  public void setScaleMultiplier(float scaleMultiplier) {
    this.scaleMultiplier = scaleMultiplier;
    revalidate();
    repaint();
  }

  /**
   * Returns the origin point where the image of this component is drawn.
   */
  protected Point getImageTranslation() {
    float scale = getImageScale();
    Insets insets = getInsets();
    return new Point(insets.left + (getWidth() - insets.left - insets.right - Math.round(image.getWidth() * scale)) / 2,
        insets.top + (getHeight() - insets.top - insets.bottom - Math.round(image.getHeight() * scale)) / 2);
  }

  /**
   * Returns <code>true</code> if point at (<code>x</code>, <code>y</code>)
   * is in the image displayed by this component.
   */
  protected boolean isPointInImage(int x, int y) {
    Point translation = getImageTranslation();
    float scale = getImageScale();
    return x >= translation.x && x < translation.x + Math.round(getImage().getWidth() * scale)
        && y >= translation.y && y < translation.y + Math.round(getImage().getHeight() * scale);
  }
  
  
  /**
   * Returns a point with (<code>x</code>, <code>y</code>) coordinates constrained 
   * in the image displayed by this component.
   */
  protected Point getPointConstrainedInImage(int x, int y) {
    Point translation = getImageTranslation();
    float scale = getImageScale();
    x = Math.min(Math.max(x, translation.x), translation.x + Math.round(getImage().getWidth() * scale));
    y = Math.min(Math.max(y, translation.y), translation.y + Math.round(getImage().getHeight() * scale));
    return new Point(x, y);
  }
}