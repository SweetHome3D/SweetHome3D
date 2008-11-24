/*
 * HomePDFPrinter.java 7 sept. 07
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

import java.awt.Font;
import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.ResourceBundle;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.viewcontroller.ContentManager;
import com.eteks.sweethome3d.viewcontroller.HomeController;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Home PDF printer. PDF creation is implemented with iText. 
 * @author Emmanuel Puybaret
 */
public class HomePDFPrinter {
  private final Home           home;
  private final HomeController controller;
  private final Font           defaultFont;

  public HomePDFPrinter(Home home, HomeController controller, Font defaultFont) {
    this.home = home;
    this.controller = controller;
    this.defaultFont = defaultFont;
  }

  /**
   * Writes to <code>outputStream</code> the print of a home in PDF format.
   */
  public void write(OutputStream outputStream) throws IOException {
    PageFormat pageFormat = PageSetupPanel.getPageFormat(this.home.getPrint());
    Document pdfDocument = new Document(new Rectangle((float)pageFormat.getWidth(), (float)pageFormat.getHeight()));
    try {
      // Get a PDF writer that will write to the given PDF output stream
      PdfWriter pdfWriter = PdfWriter.getInstance(pdfDocument, outputStream);
      pdfDocument.open();
      
      // Set PDF document description
      pdfDocument.addAuthor(System.getProperty("user.name", ""));
      String pdfDocumentCreator = ResourceBundle.getBundle(HomePDFPrinter.class.getName()).
          getString("pdfDocument.creator");    
      pdfDocument.addCreator(pdfDocumentCreator);
      pdfDocument.addCreationDate();
      String homeName = this.home.getName();
      if (homeName != null) {
        pdfDocument.addTitle(this.controller.getContentManager().getPresentationName(
            homeName, ContentManager.ContentType.PDF));
      }
      
      PdfContentByte pdfContent = pdfWriter.getDirectContent();
      HomePrintableComponent printableComponent = 
        new HomePrintableComponent(this.home, this.controller, this.defaultFont);
      // Print each page
      for (int page = 0, pageCount = printableComponent.getPageCount(); page < pageCount; page++) {
        // Check current thread isn't interrupted
        if (Thread.interrupted()) {
          throw new InterruptedIOException();
        }
        PdfTemplate pdfTemplate = pdfContent.createTemplate((float)pageFormat.getWidth(), 
            (float)pageFormat.getHeight());
        Graphics g = pdfTemplate.createGraphicsShapes((float)pageFormat.getWidth(), 
            (float)pageFormat.getHeight());        
        
        printableComponent.print(g, pageFormat, page);
        
        pdfContent.addTemplate(pdfTemplate, 0, 0);
        g.dispose();
        
        if (page != pageCount - 1) {
          pdfDocument.newPage();
        }
      }
      pdfDocument.close();
    } catch (DocumentException ex) {
      IOException exception = new IOException("Couldn't print to PDF");
      exception.initCause(ex);
      throw exception;
    } catch (InterruptedPrinterException ex) {
      throw new InterruptedIOException("Print to PDF interrupted");
    } catch (PrinterException ex) {
      IOException exception = new IOException("Couldn't print to PDF");
      exception.initCause(ex);
      throw exception;
    }
  }
}
