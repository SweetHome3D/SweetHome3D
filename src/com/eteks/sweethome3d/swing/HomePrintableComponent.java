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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.text.MessageFormat;
import java.util.Date;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.JLabel;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePrint;
import com.eteks.sweethome3d.model.LengthUnit;
import com.eteks.sweethome3d.viewcontroller.ContentManager;
import com.eteks.sweethome3d.viewcontroller.HomeController;
import com.eteks.sweethome3d.viewcontroller.PlanView;

/**
 * A printable component used to print or preview the furniture, the plan 
 * and the 3D view of a home.
 */
public class HomePrintableComponent extends JComponent implements Printable {
  /**
   * List of the variables that the user may insert in header and footer.
   */
  public enum Variable {
    PAGE_NUMBER("$pageNumber", "{0, number, integer}"),
    PAGE_COUNT("$pageCount", "{1, number, integer}"),
    PLAN_SCALE("$planScale", "{2}"),
    DATE("$date", "{3, date}"),
    TIME("$time", "{3, time}"),
    HOME_PRESENTATION_NAME("$name", "{4}"),
    HOME_NAME("$file", "{5}");    
    
    private final String userCode;
    private final String formatCode;

    private Variable(String userCode, String formatCode) {
      this.userCode = userCode;
      this.formatCode = formatCode;      
    }
    
    /**
     * Returns a user readable code matching this field.
     */
    public String getUserCode() {
      return this.userCode;
    }
    
    /**
     * Returns a format usable code matching this field.
     */
    public String getFormatCode()  {
      return this.formatCode;
    }
    
    /**
     * Returns the message format built from a format that uses variables.
     */
    public static MessageFormat getMessageFormat(String format) {
      // Replace $$ escape sequence ($$ is the escape sequence for $ character)
      final String temp = "|#&%<>/!";
      format = format.replace("$$", temp);
      // Replace MessageFormat escape sequences
      format = format.replace("'", "''");
      format = format.replace("{", "'{'");
      // Replace variable by their MessageFormat code
      for (Variable variable : Variable.values()) {
        format = format.replace(variable.getUserCode(), variable.getFormatCode());
      }
      format = format.replace(temp, "$");
      return new MessageFormat(format);
    }
  };

  private static final float   HEADER_FOOTER_MARGIN = LengthUnit.centimeterToInch(0.2f) * 72;
  
  private final Home           home;
  private final HomeController controller;
  private final Font           defaultFont;
  private final Font           headerFooterFont;
  private int                  page;
  private int                  pageCount = -1;
  private int                  planViewIndex;
  private Date                 printDate;
  private JLabel               fixedHeaderLabel;
  private JLabel               fixedFooterLabel;
  
  /**
   * Creates a printable component that will print or display the
   * furniture view, the plan view and 3D view of the <code>home</code> 
   * managed by <code>controller</code>.
   */
  public HomePrintableComponent(Home home, HomeController controller, Font defaultFont) {
    this.home = home;
    this.controller = controller;
    this.defaultFont = defaultFont;
    this.headerFooterFont = defaultFont.deriveFont(11f);
    
    try {
      ResourceBundle resource = ResourceBundle.getBundle(HomePrintableComponent.class.getName());
      this.fixedHeaderLabel = getFixedLabel(resource, "fixedHeader");
      this.fixedFooterLabel = getFixedLabel(resource, "fixedFooter");
    } catch (MissingResourceException ex) {
      // No resource bundle 
    }
  }

  private JLabel getFixedLabel(ResourceBundle resource, String resourceKey) {
    try {        
      String fixedHeader = String.format(resource.getString(resourceKey), 
          HomePrintableComponent.class.getResource("."));
      JLabel fixedLabel = new JLabel(fixedHeader, JLabel.CENTER);
      fixedLabel.setFont(this.headerFooterFont);
      fixedLabel.setSize(fixedLabel.getPreferredSize());
      return fixedLabel;
    } catch (MissingResourceException ex) {
      // No fixed label
      return null;
    }
  }
  
  /**
   * Prints a given <code>page</code>.
   */
  public int print(Graphics g, PageFormat pageFormat, int page) throws PrinterException {
    // Check current thread isn't interrupted
    if (Thread.interrupted()) {
      throw new InterruptedPrinterException();
    }
    
    Graphics2D g2D = (Graphics2D)g;
    g2D.setFont(this.defaultFont);
    g2D.setColor(Color.WHITE);
    g2D.fill(new Rectangle2D.Double(0, 0, pageFormat.getWidth(), pageFormat.getHeight()));
    int pageExists = NO_SUCH_PAGE;
    HomePrint homePrint = this.home.getPrint();
    
    // Prepare header and footer
    float imageableY = (float)pageFormat.getImageableY();
    float imageableHeight = (float)pageFormat.getImageableHeight();
    String header = null;
    float  xHeader = 0;
    float  yHeader = 0;
    float  xFixedHeader = 0;
    float  yFixedHeader = 0;
    String footer = null;
    float  xFooter = 0;
    float  yFooter = 0;
    float  xFixedFooter = 0;
    float  yFixedFooter = 0;
    
    if (this.fixedHeaderLabel != null) {
      this.fixedHeaderLabel.setSize((int)pageFormat.getImageableWidth(), this.fixedHeaderLabel.getPreferredSize().height);
      imageableHeight -= this.fixedHeaderLabel.getHeight() + HEADER_FOOTER_MARGIN;
      imageableY += this.fixedHeaderLabel.getHeight() + HEADER_FOOTER_MARGIN;
      xFixedHeader = (float)pageFormat.getImageableX();
      yFixedHeader = (float)pageFormat.getImageableY();
    }
    
    if (this.fixedFooterLabel != null) {
      this.fixedFooterLabel.setSize((int)pageFormat.getImageableWidth(), this.fixedFooterLabel.getPreferredSize().height);
      imageableHeight -= this.fixedFooterLabel.getHeight() + HEADER_FOOTER_MARGIN;
      xFixedFooter = (float)pageFormat.getImageableX();
      yFixedFooter = (float)(pageFormat.getImageableY() + pageFormat.getImageableHeight()) - this.fixedFooterLabel.getHeight();
    }
    
    Rectangle clipBounds = g2D.getClipBounds();
    AffineTransform oldTransform = g2D.getTransform();
    Paper oldPaper = pageFormat.getPaper();
    final PlanView planView = this.controller.getPlanController().getView();
    if (homePrint != null
        || this.fixedHeaderLabel != null
        || this.fixedFooterLabel != null) {
      if (homePrint != null) {
        FontMetrics fontMetrics = g2D.getFontMetrics(this.headerFooterFont);
        float headerFooterHeight = fontMetrics.getAscent() + fontMetrics.getDescent() 
        + HEADER_FOOTER_MARGIN;
        
        // Retrieve variable values
        int pageNumber = page + 1; 
        int pageCount = getPageCount(); 
        String planScale = "?";
        if (homePrint.getPlanScale() != null) {
          planScale = "1/" + Math.round(1 / homePrint.getPlanScale());
        } else {
          if (planView instanceof PlanComponent) {
            planScale = "1/" + Math.round(1 / ((PlanComponent)planView).getPrintPreferredScale(g, pageFormat)); 
          }        
        }          
        if (page == 0) {
          this.printDate = new Date();
        }
        String homeName = this.home.getName();
        if (homeName == null) {
          homeName = "";
        }
        String homePresentationName = this.controller.getContentManager().getPresentationName(
             homeName, ContentManager.ContentType.SWEET_HOME_3D);
        Object [] variableValues = new Object [] {
            pageNumber, pageCount, planScale, this.printDate, homePresentationName, homeName};
        
        // Create header text
        String headerFormat = homePrint.getHeaderFormat();      
        if (headerFormat != null) {
          header = Variable.getMessageFormat(headerFormat).format(variableValues).trim();
          if (header.length() > 0) {
            xHeader = ((float)pageFormat.getWidth() - fontMetrics.stringWidth(header)) / 2;
            yHeader = imageableY + fontMetrics.getAscent();
            imageableY += headerFooterHeight;
            imageableHeight -= headerFooterHeight;
          } else {
            header = null;
          }
        }
        
        // Create footer text
        String footerFormat = homePrint.getFooterFormat();
        if (footerFormat != null) {
          footer = Variable.getMessageFormat(footerFormat).format(variableValues).trim();
          if (footer.length() > 0) {
            xFooter = ((float)pageFormat.getWidth() - fontMetrics.stringWidth(footer)) / 2;
            yFooter = imageableY + imageableHeight - fontMetrics.getDescent();
            imageableHeight -= headerFooterHeight;
          } else {
            footer = null;
          }
        }
      }
      
      // Update page format paper margins depending on paper orientation
      Paper paper = pageFormat.getPaper();
      switch (pageFormat.getOrientation()) {
        case PageFormat.PORTRAIT:
          paper.setImageableArea(paper.getImageableX(), imageableY, 
              paper.getImageableWidth(), imageableHeight);
          break;
        case PageFormat.LANDSCAPE :
          paper.setImageableArea(paper.getWidth() - (imageableHeight + imageableY), 
              paper.getImageableY(), 
              imageableHeight, paper.getImageableHeight());
        case PageFormat.REVERSE_LANDSCAPE:
          paper.setImageableArea(imageableY, paper.getImageableY(), 
              imageableHeight, paper.getImageableHeight());
          break;
      }
      pageFormat.setPaper(paper);
      if (clipBounds == null) {
        g2D.clipRect((int)pageFormat.getImageableX(), (int)pageFormat.getImageableY(), 
            (int)pageFormat.getImageableWidth(), (int)pageFormat.getImageableHeight());
      } else {  
        g2D.clipRect(clipBounds.x, (int)pageFormat.getImageableY(), 
            clipBounds.width, (int)pageFormat.getImageableHeight());
      }
    }
    
    // Print current page 
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
      pageExists = ((Printable)planView).print(g2D, pageFormat, 0);
    } else if ((homePrint == null && page == this.planViewIndex + 1)
               || (homePrint != null
                   && homePrint.isView3DPrinted()
                   && ((homePrint.isPlanPrinted()
                         && page == this.planViewIndex + 1)
                       || (!homePrint.isPlanPrinted()
                           && page == this.planViewIndex)))) {
      pageExists = ((Printable)this.controller.getHomeController3D().getView()).print(g2D, pageFormat, 0);
    }
    
    // Print header and footer
    if (pageExists == PAGE_EXISTS) {
      g2D.setTransform(oldTransform);
      g2D.setClip(clipBounds);
      g2D.setFont(this.headerFooterFont);
      g2D.setColor(Color.BLACK);
      if (this.fixedHeaderLabel != null) {
        g2D.translate(xFixedHeader, yFixedHeader);
        this.fixedHeaderLabel.print(g2D);
        g2D.translate(-xFixedHeader, -yFixedHeader);
      }
      if (header != null) {
        g2D.drawString(header, xHeader, yHeader);
      }
      if (footer != null) {
        g2D.drawString(footer, xFooter, yFooter);
      }
      if (this.fixedFooterLabel != null) {
        g2D.translate(xFixedFooter, yFixedFooter);
        this.fixedFooterLabel.print(g2D);
        g2D.translate(-xFixedFooter, -yFixedFooter);
      }
    }  
    pageFormat.setPaper(oldPaper);    
    return pageExists;
  }

  /**
   * Returns the preferred size of this component according to paper orientation and size
   * of home print attributes.
   */
  @Override
  public Dimension getPreferredSize() {
    PageFormat pageFormat = getPageFormat(this.home.getPrint());
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
      PageFormat pageFormat = getPageFormat(this.home.getPrint());
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
      PageFormat pageFormat = getPageFormat(this.home.getPrint());
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

  /**
   * Returns a <code>PageFormat</code> object created from <code>homePrint</code>.
   */
  public static PageFormat getPageFormat(HomePrint homePrint) {
    final PrinterJob printerJob = PrinterJob.getPrinterJob();
    if (homePrint == null) {
      return printerJob.defaultPage();
    } else {
      PageFormat pageFormat = new PageFormat();
      switch (homePrint.getPaperOrientation()) {
        case PORTRAIT :
          pageFormat.setOrientation(PageFormat.PORTRAIT);
          break;
        case LANDSCAPE :
          pageFormat.setOrientation(PageFormat.LANDSCAPE);
          break;
        case REVERSE_LANDSCAPE :
          pageFormat.setOrientation(PageFormat.REVERSE_LANDSCAPE);
          break;
      }
      Paper paper = new Paper();
      paper.setSize(homePrint.getPaperWidth(), homePrint.getPaperHeight());
      paper.setImageableArea(homePrint.getPaperLeftMargin(), homePrint.getPaperTopMargin(), 
          homePrint.getPaperWidth() - homePrint.getPaperLeftMargin() - homePrint.getPaperRightMargin(), 
          homePrint.getPaperHeight() - homePrint.getPaperTopMargin() - homePrint.getPaperBottomMargin());
      pageFormat.setPaper(paper);
      pageFormat = printerJob.validatePage(pageFormat);
      return pageFormat;
    }
  }
}