/*
 * RoomTest.java 25 nov. 2008
 *
 * Copyright (c) 2008 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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

import java.awt.event.InputEvent;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.JTextField;

import junit.extensions.abbot.ComponentTestFixture;
import abbot.finder.AWTHierarchy;
import abbot.finder.BasicFinder;
import abbot.finder.ComponentSearchException;
import abbot.finder.matchers.ClassMatcher;
import abbot.tester.JComponentTester;

import com.eteks.sweethome3d.io.DefaultUserPreferences;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.swing.FileContentManager;
import com.eteks.sweethome3d.swing.HomePane;
import com.eteks.sweethome3d.swing.NullableCheckBox;
import com.eteks.sweethome3d.swing.PlanComponent;
import com.eteks.sweethome3d.swing.RoomPanel;
import com.eteks.sweethome3d.swing.SwingViewFactory;
import com.eteks.sweethome3d.viewcontroller.HomeController;
import com.eteks.sweethome3d.viewcontroller.HomeView;
import com.eteks.sweethome3d.viewcontroller.PlanController;
import com.eteks.sweethome3d.viewcontroller.ViewFactory;

/**
 * Tests rooms in {@link com.eteks.sweethome3d.swing.PlanComponent plan} component and 
 * their management in {@link com.eteks.sweethome3d.viewcontroller.PlanController controller}.
 * @author Emmanuel Puybaret
 */
public class RoomTest extends ComponentTestFixture {
  public void testRoomCreation() throws ComponentSearchException, 
      NoSuchFieldException, IllegalAccessException {
    // 1. Create a frame that displays a home view 
    RoomTestFrame frame = new RoomTestFrame();    
    // Show home plan frame
    showWindow(frame);
    
    // 2. Change default wall thickness and height
    frame.preferences.setNewWallThickness(10);
    frame.preferences.setNewWallHeight(100);
    // Create a home with 5 walls 
    PlanController planController = frame.homeController.getPlanController();
    PlanComponent planComponent = (PlanComponent)planController.getView();
    planController.setMode(PlanController.Mode.WALL_CREATION);
    // Click at (50, 50), (200, 50), (250, 100), (250, 150), (50, 150) then double click at (50, 50) 
    JComponentTester tester = new JComponentTester();
    tester.actionClick(planComponent, 50, 50);
    tester.actionClick(planComponent, 200, 50);
    tester.actionClick(planComponent, 250, 100);
    tester.actionClick(planComponent, 250, 150);
    tester.actionClick(planComponent, 50, 150);
    tester.actionClick(planComponent, 50, 50, InputEvent.BUTTON1_MASK, 2);
    assertEquals("Wrong wall count in home", 5, frame.home.getWalls().size());
    
    // 3. Use ROOM_CREATION mode
    planController.setMode(PlanController.Mode.ROOM_CREATION);
    // Double click outside of walls 
    tester.actionClick(planComponent, 40, 40, InputEvent.BUTTON1_MASK, 2);
    // Check no room was created
    assertEquals("Wrong room count in home", 0, frame.home.getRooms().size());
    // Double click inside walls 
    tester.actionClick(planComponent, 100, 100, InputEvent.BUTTON1_MASK, 2);
    // Check a room was created
    assertEquals("Wrong room count in home", 1, frame.home.getRooms().size());
    // Check room point count and area
    Room room = frame.home.getRooms().get(0);
    assertEquals("Wrong point count", 5, room.getPoints().length);
    assertEquals("Wrong room area", 69273.02f, room.getArea());
    
    // 4. Edit created room
    JDialog attributesDialog = showRoomPanel(frame.preferences, frame.homeController, frame, tester);
    // Retrieve RoomPanel components
    RoomPanel wallPanel = (RoomPanel)TestUtilities.findComponent(
        attributesDialog, RoomPanel.class);
    JTextField nameTextField = 
        (JTextField)TestUtilities.getField(wallPanel, "nameTextField");
    NullableCheckBox areaVisibleCheckBox = 
        (NullableCheckBox)TestUtilities.getField(wallPanel, "areaVisibleCheckBox");
    NullableCheckBox floorVisibleCheckBox = 
        (NullableCheckBox)TestUtilities.getField(wallPanel, "floorVisibleCheckBox");
    NullableCheckBox ceilingVisibleCheckBox = 
        (NullableCheckBox)TestUtilities.getField(wallPanel, "ceilingVisibleCheckBox");
    // Check name is empty and check boxes are selected
    assertTrue("Name not empty", nameTextField.getText().length() == 0);
    assertTrue("Area check box isn't checked", areaVisibleCheckBox.getValue());
    assertTrue("Floor check box isn't checked", floorVisibleCheckBox.getValue());
    assertTrue("Ceiling check box isn't checked", ceilingVisibleCheckBox.getValue());
    
    // Enter a name and unchecked boxes
    nameTextField.setText("Test");
    tester.click(areaVisibleCheckBox);
    tester.click(floorVisibleCheckBox);
    tester.click(ceilingVisibleCheckBox);
    
    final JOptionPane attributesOptionPane = (JOptionPane)TestUtilities.findComponent(
        attributesDialog, JOptionPane.class);
    tester.invokeAndWait(new Runnable() {
        public void run() {
          // Select Ok option to hide dialog box in Event Dispatch Thread
          attributesOptionPane.setValue(JOptionPane.OK_OPTION);
        }
      });
    assertFalse("Dialog still showing", attributesDialog.isShowing());
    
    // Assert room was modified accordingly
    assertEquals("Name is incorrect", "Test", room.getName());
    assertFalse("Area is visible", room.isAreaVisible());
    assertFalse("Floor is visible", room.isFloorVisible());
    assertFalse("Ceiling is visible", room.isCeilingVisible());
    
    // 5. Increase font size of room name text
    assertNull("Text style exists", room.getNameStyle());
    runAction(frame.homeController, HomeView.ActionType.INCREASE_TEXT_SIZE);
    // Check text style
    assertEquals("Wrong text size", 26.f, room.getNameStyle().getFontSize());
    // Decrease font size of room name text
    runAction(frame.homeController, HomeView.ActionType.DECREASE_TEXT_SIZE);
    runAction(frame.homeController, HomeView.ActionType.DECREASE_TEXT_SIZE);    
    assertEquals("Wrong text size", 22.f, room.getNameStyle().getFontSize());
    // Change style to italic
    runAction(frame.homeController, HomeView.ActionType.TOGGLE_ITALIC_STYLE);
    assertTrue("Text isn't italic", room.getNameStyle().isItalic());
    assertFalse("Text is bold", room.getNameStyle().isBold());
    // Change style to bold
    runAction(frame.homeController, HomeView.ActionType.TOGGLE_BOLD_STYLE);
    assertTrue("Text isn't italic", room.getNameStyle().isItalic());
    assertTrue("Text isn't bold", room.getNameStyle().isBold());
    
    // 6. Undo style change
    runAction(frame.homeController, HomeView.ActionType.UNDO);
    runAction(frame.homeController, HomeView.ActionType.UNDO);
    // Check style
    assertFalse("Text is italic", room.getNameStyle().isItalic());
    assertFalse("Text is bold", room.getNameStyle().isBold());
    // Undo text size change
    runAction(frame.homeController, HomeView.ActionType.UNDO);
    runAction(frame.homeController, HomeView.ActionType.UNDO);
    runAction(frame.homeController, HomeView.ActionType.UNDO);
    assertEquals("Wrong text size", 24.f, room.getNameStyle().getFontSize());
    // Undo room modification
    runAction(frame.homeController, HomeView.ActionType.UNDO);
    assertNull("Name isn't empty", room.getName());
    assertTrue("Area isn't visible", room.isAreaVisible());
    assertTrue("Floor isn't visible", room.isFloorVisible());
    assertTrue("Ceiling isn't visible", room.isCeilingVisible());
    // Undo room creation
    runAction(frame.homeController, HomeView.ActionType.UNDO);
    assertEquals("Wrong room count in home", 0, frame.home.getRooms().size());
   
    // 7. Redo everything
    for (int i = 0; i < 7; i++) {
      runAction(frame.homeController, HomeView.ActionType.REDO);
    }
    // Check room is back
    assertEquals("Wrong room count in home", 1, frame.home.getRooms().size());
    room = frame.home.getRooms().get(0);    
    assertEquals("Name is incorrect", "Test", room.getName());
    assertFalse("Area is visible", room.isAreaVisible());
    assertFalse("Floor is visible", room.isFloorVisible());
    assertFalse("Ceiling is visible", room.isCeilingVisible());
    assertEquals("Wrong text size", 22.f, room.getNameStyle().getFontSize());
    assertTrue("Text isn't italic", room.getNameStyle().isItalic());
    assertTrue("Text isn't bold", room.getNameStyle().isBold());
  }

  /**
   * Runs <code>actionPerformed</code> method matching <code>actionType</code> 
   * in <code>HomePane</code>. 
   */
  private void runAction(HomeController controller,
                         HomePane.ActionType actionType) {
    ((JComponent)controller.getView()).getActionMap().get(actionType).actionPerformed(null);
  }
  
  /**
   * Returns the dialog that displays room attributes. 
   */
  private JDialog showRoomPanel(UserPreferences preferences,
                                final HomeController controller, 
                                JFrame parent, JComponentTester tester) 
            throws ComponentSearchException {
    tester.invokeLater(new Runnable() { 
        public void run() {
          // Display dialog box later in Event Dispatch Thread to avoid blocking test thread
          runAction(controller, HomeView.ActionType.MODIFY_ROOM);
        }
      });
    // Wait for wall view to be shown
    tester.waitForFrameShowing(new AWTHierarchy(), preferences.getLocalizedString(
        RoomPanel.class, "room.title"));
    // Check dialog box is displayed
    JDialog attributesDialog = (JDialog)new BasicFinder().find(parent, 
        new ClassMatcher (JDialog.class, true));
    assertTrue("Room dialog not showing", attributesDialog.isShowing());
    return attributesDialog;
  }

  public static void main(String [] args) {
    JFrame frame = new RoomTestFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);
  }
  
  private static class RoomTestFrame extends JFrame {
    private final UserPreferences preferences;
    private final Home            home;
    private final HomeController  homeController;

    public RoomTestFrame() {
      super("Room Test");
      // Create model objects
      this.home = new Home();
      Locale.setDefault(Locale.FRANCE);
      this.preferences = new DefaultUserPreferences() {
          @Override
          public void write() throws RecorderException {
          }
        };
      ViewFactory viewFactory = new SwingViewFactory();
      FileContentManager contentManager = new FileContentManager(this.preferences);
      this.homeController = new HomeController(this.home, this.preferences, viewFactory, contentManager);
      setRootPane((JRootPane)this.homeController.getView());
      pack();
    }
  }
}
