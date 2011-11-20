/*
 * PhotoCreationTest.java 18 sept. 2010
 *
 * Copyright (c) 2010 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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
package com.eteks.sweethome3d.junit;

import java.awt.Component;
import java.awt.Dialog;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.JSpinner;

import junit.extensions.abbot.ComponentTestFixture;
import abbot.finder.AWTHierarchy;
import abbot.finder.BasicFinder;
import abbot.finder.ComponentNotFoundException;
import abbot.finder.ComponentSearchException;
import abbot.finder.MultipleComponentsFoundException;
import abbot.finder.matchers.ClassMatcher;
import abbot.tester.JComponentTester;
import abbot.tester.JFileChooserTester;
import abbot.tester.JSliderTester;

import com.eteks.sweethome3d.io.DefaultUserPreferences;
import com.eteks.sweethome3d.io.HomeFileRecorder;
import com.eteks.sweethome3d.model.AspectRatio;
import com.eteks.sweethome3d.model.Compass;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeApplication;
import com.eteks.sweethome3d.model.HomeRecorder;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.swing.FileContentManager;
import com.eteks.sweethome3d.swing.HomePane;
import com.eteks.sweethome3d.swing.PhotoPanel;
import com.eteks.sweethome3d.swing.SwingViewFactory;
import com.eteks.sweethome3d.swing.VideoPanel;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.ContentManager;
import com.eteks.sweethome3d.viewcontroller.HomeController;
import com.eteks.sweethome3d.viewcontroller.View;

/**
 * Tests photo creation dialog.
 * @author Emmanuel Puybaret
 */
public class PhotoCreationTest extends ComponentTestFixture {
  private UserPreferences preferences;
  private HomeController homeController;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    this.preferences = new DefaultUserPreferences() {
        @Override
        public void write() throws RecorderException {        
        }
      };
    SwingViewFactory viewFactory = new SwingViewFactory();
    HomeApplication application = new HomeApplication() {
        @Override
        public HomeRecorder getHomeRecorder() {
          return new HomeFileRecorder();
        }
  
        @Override
        public UserPreferences getUserPreferences() {
          return preferences;
        }
      };
    // Create a dummy controller to load a home test file
    HomeController controller = new HomeController(new Home(), application, viewFactory);
    
    String testFile = PhotoCreationTest.class.getResource("resources/home1.sh3d").getFile();
    if (OperatingSystem.isWindows()) {
      testFile = testFile.substring(1).replace("%20", " ");
    }
    controller.open(testFile);
    // Wait home is opened
    for (int i = 0; i < 100 && application.getHomes().size() == 0; i++) {
      Thread.sleep(100);
    }
    
    List<Home> homes = application.getHomes();
    assertEquals("Application doesn't contain one home", 1, homes.size());
    assertEquals("Test home wasn't opened", testFile, homes.get(0).getName());
    ContentManager contentManager = new FileContentManager(preferences) {
        @Override
        public String showSaveDialog(View parentView, String dialogTitle, ContentType contentType, String name) {
          String os = System.getProperty("os.name");
          if (OperatingSystem.isMacOSX()) {
            // Let's pretend the OS isn't Mac OS X to get a JFileChooser instance that works better in test
            System.setProperty("os.name", "dummy");
          }
          try {
            return super.showSaveDialog(parentView, dialogTitle, contentType, name);
          } finally {
            System.setProperty("os.name", os);
          }
        }
      };
    this.homeController = new HomeController(homes.get(0), application, viewFactory, contentManager, null);
  }
  
  public void testSunLocation() {
    // Tests azimuth and elevation values computed in Compass to avoid any regression
    Compass compass = new Compass(0, 0, 2);
    compass.setLatitude((float)Math.toRadians(45));
    compass.setLongitude((float)Math.toRadians(0));
    compass.setTimeZone("Europe/Paris");
    GregorianCalendar calendar = new GregorianCalendar(2010, GregorianCalendar.SEPTEMBER, 1, 14, 30, 10);
    calendar.setTimeZone(TimeZone.getTimeZone(compass.getTimeZone()));
    TestUtilities.assertEqualsWithinEpsilon("Incorrect azimuth", 3.383972f, compass.getSunAzimuth(calendar.getTimeInMillis()), 1E-5f);
    TestUtilities.assertEqualsWithinEpsilon("Incorrect elevation", 0.915943f, compass.getSunElevation(calendar.getTimeInMillis()), 1E-5f);
    
    compass = new Compass(0, 0, 2);
    compass.setLatitude((float)Math.toRadians(40));
    compass.setLongitude((float)Math.toRadians(160));
    compass.setTimeZone("Asia/Tokyo");
    calendar = new GregorianCalendar(2011, GregorianCalendar.JANUARY, 31, 8, 0, 0);
    calendar.setTimeZone(TimeZone.getTimeZone(compass.getTimeZone()));
    TestUtilities.assertEqualsWithinEpsilon("Incorrect azimuth", 2.44565f, compass.getSunAzimuth(calendar.getTimeInMillis()), 1E-5f);
    TestUtilities.assertEqualsWithinEpsilon("Incorrect elevation", 0.38735f, compass.getSunElevation(calendar.getTimeInMillis()), 1E-5f);
  }
  
  public void testPhotoCreation() throws InterruptedException, ComponentSearchException, NoSuchFieldException, IllegalAccessException, InvocationTargetException, IOException {
    final JComponent view = (JComponent)this.homeController.getView();
    
    // Create a frame that displays a home view 
    JFrame frame = new JFrame("Photo Creation Test");    
    frame.add(view);
    frame.pack();

    showWindow(frame);
    JComponentTester tester = new JComponentTester();
    tester.waitForIdle();

    JDialog photoCreationDialog = showPhotoCreationPanel(this.preferences, this.homeController, frame, tester);
    // Retrieve PhotoPanel components
    PhotoPanel panel = (PhotoPanel)TestUtilities.findComponent(photoCreationDialog, PhotoPanel.class);    
    final JSpinner widthSpinner = (JSpinner)TestUtilities.getField(panel, "widthSpinner");
    JSpinner heightSpinner = (JSpinner)TestUtilities.getField(panel, "heightSpinner");
    final JComboBox aspectRatioComboBox = (JComboBox)TestUtilities.getField(panel, "aspectRatioComboBox");
    JSlider qualitySlider = (JSlider)TestUtilities.getField(panel, "qualitySlider");
    JButton createButton = (JButton)TestUtilities.getField(panel, "createButton");
    JButton saveButton = (JButton)TestUtilities.getField(panel, "saveButton");
    JButton closeButton = (JButton)TestUtilities.getField(panel, "closeButton");
    
    tester.invokeAndWait(new Runnable() {
        public void run() {
          widthSpinner.setValue(200L);
          aspectRatioComboBox.setSelectedItem(AspectRatio.SQUARE_RATIO);
        }
      });
    assertEquals("Height spinner has wrong value", 200, ((Number)heightSpinner.getValue()).intValue());
    JSliderTester sliderTester = new JSliderTester();
    for (int i = qualitySlider.getMinimum(); i <= qualitySlider.getMaximum(); i++) {
      sliderTester.actionSlide(qualitySlider, i);
      // Test image creation at each quality 
      tester.click(createButton);
      assertFalse("Rendering didn't start", saveButton.isEnabled());
      // Wait image is generated
      for (int j = 0; j < 1000 && !saveButton.isEnabled(); j++) {
        Thread.sleep(100);
      }
      assertTrue("Rendering didn't end", saveButton.isEnabled());
      saveAndAssertMediaIsSaved(saveButton, ContentManager.ContentType.PNG, 
          preferences.getLocalizedString(PhotoPanel.class, "savePhotoDialog.title"),
          photoCreationDialog, tester);
    }
    tester.click(closeButton);
    tester.close(frame);
  }

  public void testVideoCreation() throws InterruptedException, ComponentSearchException, NoSuchFieldException, IllegalAccessException, IOException {
    final JComponent view = (JComponent)this.homeController.getView();
    
    // Create a frame that displays a home view 
    JFrame frame = new JFrame("Video Creation Test");    
    frame.add(view);
    frame.pack();

    showWindow(frame);
    JComponentTester tester = new JComponentTester();
    tester.waitForIdle();

    JDialog videoCreationDialog = showVideoCreationPanel(this.preferences, this.homeController, frame, tester);
    // Retrieve VideoPanel components
    VideoPanel panel = (VideoPanel)TestUtilities.findComponent(videoCreationDialog, VideoPanel.class);
    JProgressBar progressBar = (JProgressBar)TestUtilities.getField(panel, "progressBar");
    JSlider qualitySlider = (JSlider)TestUtilities.getField(panel, "qualitySlider");
    JButton createButton = (JButton)TestUtilities.getField(panel, "createButton");
    JButton saveButton = (JButton)TestUtilities.getField(panel, "saveButton");
    JButton closeButton = (JButton)TestUtilities.getField(panel, "closeButton");
    JSliderTester sliderTester = new JSliderTester();
    for (int i = qualitySlider.getMinimum(); i < qualitySlider.getMaximum(); i += 2) {
      sliderTester.actionSlide(qualitySlider, i);
      // Test image creation at quality 1 and 3 
      assertFalse("Progress bar is showing", progressBar.isShowing());
      tester.click(createButton);
      assertTrue("Progress bar isn't showing", progressBar.isShowing());
      // Wait image is generated
      for (int j = 0; j < 1000 && !saveButton.isEnabled(); j++) {
        Thread.sleep(100);
      }
      assertTrue("Rendering didn't end", saveButton.isEnabled());
      saveAndAssertMediaIsSaved(saveButton, ContentManager.ContentType.MOV, 
          preferences.getLocalizedString(VideoPanel.class, "saveVideoDialog.title"),
          videoCreationDialog, tester);
    }
    tester.click(closeButton);
    tester.close(frame);
  }
  
  /**
   * Returns the dialog that displays photo panel. 
   */
  private JDialog showPhotoCreationPanel(UserPreferences preferences,
                                        final HomeController controller, 
                                        JFrame parent, JComponentTester tester) throws ComponentSearchException {
    tester.invokeLater(new Runnable() { 
        public void run() {
          // Display dialog box later in Event Dispatch Thread to avoid blocking test thread
          ((JComponent)controller.getView()).getActionMap().get(HomePane.ActionType.CREATE_PHOTO).actionPerformed(null);
        }
      });
    // Wait for 3D view to be shown
    tester.waitForFrameShowing(new AWTHierarchy(), preferences.getLocalizedString(
        PhotoPanel.class, "createPhoto.title"));
    // Check dialog box is displayed
    JDialog photoCreationDialog = (JDialog)new BasicFinder().find(parent, new ClassMatcher (JDialog.class, true));
    assertTrue("Photo creation dialog not showing", photoCreationDialog.isShowing());
    return photoCreationDialog;
  }

  /**
   * Returns the dialog that displays video panel. 
   */
  private JDialog showVideoCreationPanel(UserPreferences preferences,
                                        final HomeController controller, 
                                        JFrame parent, JComponentTester tester) throws ComponentSearchException {
    tester.invokeLater(new Runnable() { 
        public void run() {
          // Display dialog box later in Event Dispatch Thread to avoid blocking test thread
          ((JComponent)controller.getView()).getActionMap().get(HomePane.ActionType.CREATE_VIDEO).actionPerformed(null);
        }
      });
    // Wait for 3D view to be shown
    tester.waitForFrameShowing(new AWTHierarchy(), preferences.getLocalizedString(
        VideoPanel.class, "createVideo.title"));
    // Check dialog box is displayed
    JDialog photoCreationDialog = (JDialog)new BasicFinder().find(parent, new ClassMatcher (JDialog.class, true));
    assertTrue("Video creation dialog not showing", photoCreationDialog.isShowing());
    return photoCreationDialog;
  }
  
  /**
   * Assert the generated media can be saved. 
   */
  private void saveAndAssertMediaIsSaved(JButton saveButton, 
                                  ContentManager.ContentType contentType,
                                  final String fileDialogTitle, 
                                  JDialog parent, 
                                  JComponentTester tester) throws ComponentNotFoundException, MultipleComponentsFoundException, InterruptedException, IOException {
    File tmpDirectory = File.createTempFile("media", "dir");
    tmpDirectory.delete();    
    assertTrue("Couldn't create tmp directory", tmpDirectory.mkdir());
    String mediaBaseName = "test";
    // Show save dialog box
    tester.click(saveButton);
    // Wait for file chooser to be shown
    tester.waitForFrameShowing(new AWTHierarchy(), fileDialogTitle);
    // Check dialog box is displayed
    final Dialog saveDialog = (Dialog)new BasicFinder().find(parent, 
        new ClassMatcher (Dialog.class, true) {
          @Override
          public boolean matches(Component c) {
            if (super.matches(c)) {
              return ((Dialog)c).getTitle().equals(fileDialogTitle);
            } else {
              return false;
            }
          }
        });
    assertTrue("Save dialog not showing", saveDialog.isShowing());
    // Change file in print to PDF file chooser 
    final JFileChooserTester fileChooserTester = new JFileChooserTester();
    final JFileChooser fileChooser = (JFileChooser)new BasicFinder().find(saveDialog, 
        new ClassMatcher(JFileChooser.class));
    fileChooserTester.actionSetDirectory(fileChooser, tmpDirectory.getAbsolutePath());
    fileChooserTester.actionSetFilename(fileChooser, mediaBaseName);
    // Select Ok option to hide dialog box
    fileChooserTester.actionApprove(fileChooser);
    assertFalse("Save dialog still showing", saveDialog.isShowing());
    // Wait saving
    File mediaFile = new File(tmpDirectory, mediaBaseName + "." + contentType.toString().toLowerCase());
    for (int i = 0; i < 100 && !mediaFile.exists(); i++) {
      Thread.sleep(100);
    }
    assertTrue("Media file " + mediaFile + " doesn't exist", mediaFile.exists());
    assertTrue("Media file is empty", mediaFile.length() > 0);
    mediaFile.delete();
    tmpDirectory.delete();
  }
}
