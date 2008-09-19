/*
 * HomePrintableComponent.java 27 aout 07
 *
 * Copyright (c) 2007 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import javax.swing.JComponent;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePrint;

/**
 * A printable component used to print or preview the furniture, the plan 
 * and the 3D view of a home.
 */
public class HomePrintableComponent extends JComponent implements Printable {
  private Home           home;
  private HomeController controller;
  private Font           defaultFont;
  private int            page;
  private int            pageCount = -1;
  private int            planViewIndex;
  
  /**
   * Creates a printable component that will print or display the
   * furniture view, the plan view and 3D view of the <code>home</code> 
   * managed by <code>controller</code>.
   */
  public HomePrintableComponent(Home home, HomeController controller, Font defaultFont) {
    this.home = home;
    this.controller = controller;
    this.defaultFont = defaultFont;
  }
  
  /**
   * Prints a given <code>page</code>.
   */
  public int print(Graphics g, PageFormat pageFormat, int page) throws PrinterException {
    Graphics2D g2D = (Graphics2D)g;
    g2D.setFont(this.defaultFont);
    g2D.setColor(Color.WHITE);
    g2D.fill(new Rectangle2D.Double(0, 0, pageFormat.getWidth(), 
                                    pageFormat.getHeight()));
    
    int pageExists = NO_SUCH_PAGE;
    HomePrint homePrint = this.home.getPrint();
    if ((homePrint == null || homePrint.isFurniturePrinted())
        && page <= this.planViewIndex) {
      // Try to print next furniture view page
      pageExists = ((Printable)this.controller.getFurnitureController().getView()).print(g2D, pageFormat, page);
      if (pageExists == PAGE_EXISTS) {
        this.planViewIndex = page + 1;
      }
    } 
    if ((homePrint == null || homePrint.isPlanPrinted())
        && page == this.planViewIndex) {
      return ((Printable)this.controller.getPlanController().getView()).print(g2D, pageFormat, 0);
    } else if ((homePrint == null && page == this.planViewIndex + 1)
               || (homePrint != null
                   && homePrint.isView3DPrinted()
                   && ((homePrint.isPlanPrinted()
                         && page == this.planViewIndex + 1)
                       || (!homePrint.isPlanPrinted()
                           && page == this.planViewIndex)))) {
      return ((Printable)this.controller.getHomeController3D().getView()).print(g2D, pageFormat, 0);
    }
    return pageExists;
  }

  /**
   * Returns the preferred size of this component according to paper orientation and size
   * of home print attributes.
   */
  @Override
  public Dimension getPreferredSize() {
    PageFormat pageFormat = PageSetupPanel.getPageFormat(this.home.getPrint());
    double maxSize = Math.max(pageFormat.getWidth(), pageFormat.getHeight());
    Insets insets = getInsets();
    return new Dimension((int)(pageFormat.getWidth() / maxSize * 400) + insets.left + insets.right, 
        (int)(pageFormat.getHeight() / maxSize * 400) + insets.top + insets.bottom);
  }
  
  /**
   * Paints the current page.
   */
  @Override
  protected void paintComponent(Graphics g) {
    try {
      Graphics2D g2D = (Graphics2D)g.create();
      // Print printable object at component's scale
      PageFormat pageFormat = PageSetupPanel.getPageFormat(this.home.getPrint());
      Insets insets = getInsets();
      double scale = (getWidth() - insets.left - insets.right) / pageFormat.getWidth();
      g2D.scale(scale, scale);
      print(g2D, pageFormat, this.page);
      g2D.dispose();
    } catch (PrinterException ex) {
      throw new RuntimeException(ex);
    }
  }
  
  /**
   * Sets the page currently painted by this component.
   */
  public void setPage(int page) {
    if (this.page != page) {
      this.page = page;
      repaint();
    }
  }
  
  /**
   * Returns the page currently painted by this component.
   */
  public int getPage() {
    return this.page;
  }

  /**
   * Returns the page count of the home printed by this component. 
   */
  public int getPageCount() {
    if (this.pageCount == -1) {
      PageFormat pageFormat = PageSetupPanel.getPageFormat(this.home.getPrint());
      BufferedImage dummyImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
      Graphics dummyGraphics = dummyImage.getGraphics();
      // Count pages by printing in a dummy image
      this.pageCount = 0; 
      try {
        while (print(dummyGraphics, pageFormat, this.pageCount) == Printable.PAGE_EXISTS) {
          this.pageCount++;
        }
      } catch (PrinterException ex) {
        // There should be no reason that print fails if print is done on a dummy image
        throw new RuntimeException(ex);
      }
      dummyGraphics.dispose();
    }
    return this.pageCount;
  }
}