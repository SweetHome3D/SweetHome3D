/*
 * JPEGImagesToVideo.java
 * 
 * From http://java.sun.com/javase/technologies/desktop/media/jmf/2.1.1/solutions/JpegImagesToMovie.java  1.3 01/03/13
 * 
 * Copyright (c) 1999-2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGES.
 * 
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear facility.
 * Licensee represents and warrants that it will not use or redistribute the
 * Software for such purposes.
 */
package com.eteks.sweethome3d.swing;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;

import javax.media.ConfigureCompleteEvent;
import javax.media.Controller;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.DataSink;
import javax.media.EndOfMediaEvent;
import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoDataSinkException;
import javax.media.NoProcessorException;
import javax.media.PrefetchCompleteEvent;
import javax.media.Processor;
import javax.media.RealizeCompleteEvent;
import javax.media.ResourceUnavailableEvent;
import javax.media.control.TrackControl;
import javax.media.datasink.DataSinkErrorEvent;
import javax.media.datasink.DataSinkEvent;
import javax.media.datasink.DataSinkListener;
import javax.media.datasink.EndOfStreamEvent;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.FileTypeDescriptor;

/**
 * This program takes a list of images and convert them into a
 * QuickTime movie.
 */
public class JPEGImagesToVideo {
  private Object  waitSync;
  private boolean stateTransitionOK;
  private Object  waitFileSync;
  private boolean fileDone;
  private String  fileError;

  /**
   * Creates a video file at the given size and frame rate. 
   */
  public void createVideoFile(int width, int height, int frameRate, DataSource dataSource, File file) throws IOException {
    this.waitSync = new Object();
    this.stateTransitionOK = true;
    this.waitFileSync = new Object();
    this.fileDone = false;
    this.fileError = null;
    
    ControllerListener controllerListener = new ControllerListener() {
        public void controllerUpdate(ControllerEvent ev) {
          if (ev instanceof ConfigureCompleteEvent 
              || ev instanceof RealizeCompleteEvent
              || ev instanceof PrefetchCompleteEvent) {
            synchronized (waitSync) {
              stateTransitionOK = true;
              waitSync.notifyAll();
            }
          } else if (ev instanceof ResourceUnavailableEvent) {
            synchronized (waitSync) {
              stateTransitionOK = false;
              waitSync.notifyAll();
            }
          } else if (ev instanceof EndOfMediaEvent) {
            ev.getSourceController().stop();
            ev.getSourceController().close();
          }
        }
      };
    DataSinkListener dataSinkListener = new DataSinkListener() {
        public void dataSinkUpdate(DataSinkEvent ev) {
          if (ev instanceof EndOfStreamEvent) {
            synchronized (waitFileSync) {
              fileDone = true;
              waitFileSync.notifyAll();
            }
          } else if (ev instanceof DataSinkErrorEvent) {
            synchronized (waitFileSync) {
              fileDone = true;
              fileError = "Data sink error";
              waitFileSync.notifyAll();
            }
          }
        }
      };
    Processor processor = null;
    DataSink dataSink = null;
    try {
      processor = Manager.createProcessor(dataSource);
      processor.addControllerListener(controllerListener);
      processor.configure();
      // Put the Processor into configured state so we can set
      // some processing options on the processor.
      if (!waitForState(processor, Processor.Configured)) {
        throw new IOException("Failed to configure the processor.");
      }

      // Set the output content descriptor to QuickTime.
      processor.setContentDescriptor(new ContentDescriptor(FileTypeDescriptor.QUICKTIME));

      // Query for the processor for supported formats.
      // Then set it on the processor.
      TrackControl trackControls[] = processor.getTrackControls();
      Format format[] = trackControls [0].getSupportedFormats();
      if (format == null || format.length <= 0) {
        throw new IOException("The mux does not support the input format: " + trackControls [0].getFormat());
      }

      trackControls [0].setFormat(format [0]);

      // We are done with programming the processor. Let's just realize it.
      processor.realize();
      if (!waitForState(processor, Controller.Realized)) {
        throw new IOException("Failed to realize the processor.");
      }

      // Now, we'll need to create a DataSink.
      dataSink = Manager.createDataSink(processor.getDataOutput(), 
          new MediaLocator(file.toURI().toURL()));
      dataSink.open();
      dataSink.addDataSinkListener(dataSinkListener);
      this.fileDone = false;

      // Start the actual transcoding
      processor.start();
      dataSink.start();

      // Wait for EndOfStream event.
      synchronized (this.waitFileSync) {
        while (!this.fileDone) {
          this.waitFileSync.wait();
        }
      }
      
      if (this.fileError != null) {
        throw new IOException(this.fileError);
      }
    } catch (NoProcessorException ex) {
      IOException ex2 = new IOException(ex.getMessage());
      ex2.initCause(ex);
      throw ex2;
    } catch (NoDataSinkException ex) {
      IOException ex2 = new IOException("Failed to create a DataSink for the given output MediaLocator");
      ex2.initCause(ex);
      throw ex2;
    } catch (InterruptedException ex) {
      if (dataSink != null) {
        dataSink.stop();
      }
      throw new InterruptedIOException("Video creation interrupted");
    } finally {
      if (dataSink != null) {
        dataSink.close();
        dataSink.removeDataSinkListener(dataSinkListener);
      }
      if (processor != null) {
        processor.close();
        processor.removeControllerListener(controllerListener);
      }
    }
  }

  /**
   * Blocks until the processor has transitioned to the given state. Return false
   * if the transition failed.
   */
  private boolean waitForState(Processor p, int state) throws InterruptedException {
    synchronized (waitSync) {
      while (p.getState() < state && stateTransitionOK) {
        waitSync.wait();
      }
    }
    return stateTransitionOK;
  }
}
