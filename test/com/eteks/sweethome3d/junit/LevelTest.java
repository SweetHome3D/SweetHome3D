/*
 * UserPreferences.java 20 nov. 2010
 *
 * Copyright (c) 2011 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import junit.extensions.abbot.ComponentTestFixture;
import abbot.finder.AWTHierarchy;
import abbot.finder.BasicFinder;
import abbot.finder.ComponentNotFoundException;
import abbot.finder.ComponentSearchException;
import abbot.finder.MultipleComponentsFoundException;
import abbot.finder.matchers.ClassMatcher;
import abbot.tester.ComponentLocation;
import abbot.tester.JComponentTester;
import abbot.tester.JSpinnerTester;
import abbot.tester.JSplitPaneTester;
import abbot.tester.JTabbedPaneLocation;
import abbot.tester.JTabbedPaneTester;

import com.eteks.sweethome3d.io.DefaultUserPreferences;
import com.eteks.sweethome3d.io.HomeFileRecorder;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.Level;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.swing.HomePane;
import com.eteks.sweethome3d.swing.LevelPanel;
import com.eteks.sweethome3d.swing.SwingViewFactory;
import com.eteks.sweethome3d.swing.WallPanel;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.HomeController;
import com.eteks.sweethome3d.viewcontroller.HomeView;
import com.eteks.sweethome3d.viewcontroller.PlanView;

/**
 * Tests levels.
 * @author Emmanuel Puybaret
 */
public class LevelTest extends ComponentTestFixture {
  public void testLevels() throws RecorderException, NoSuchFieldException, IllegalAccessException, InterruptedException, ComponentSearchException {
    UserPreferences preferences = new DefaultUserPreferences() {
        @Override
        public void write() throws RecorderException {
        }
      };
    SwingViewFactory viewFactory = new SwingViewFactory();
    String testFile = LevelTest.class.getResource("resources/home1.sh3d").getFile();
    if (OperatingSystem.isWindows()) {
      testFile = testFile.substring(1).replace("%20", " ");
    }
    Home home = new HomeFileRecorder().readHome(testFile);
    HomeController homeController = new HomeController(home, preferences, viewFactory);
    
    final JComponent view = (JComponent)homeController.getView();
    
    // Create a frame that displays a home view 
    JFrame frame = new JFrame("Levels Test");    
    frame.add(view);

    showWindow(frame);
    JComponentTester tester = new JComponentTester();
    tester.waitForIdle();
    // Enlarge plan view size
    PlanView planView = homeController.getPlanController().getView();
    JComponent planViewComponent = (JComponent)planView;
    new JSplitPaneTester().actionMoveDivider(planViewComponent.getParent(), 0.75f);

    // Give focus to plan view
    tester.actionClick(planViewComponent, 50, 50);
    assertTrue("Plan view doesn't have focus", TestUtilities.getField(homeController, "focusedView") == planViewComponent);
    Collection<Wall> walls = home.getWalls();
    List<HomePieceOfFurniture> furniture = home.getFurniture();
    List<Room> rooms = home.getRooms();
    // Select all in plan to ensure walls at top left are visible
    runAction(homeController, HomeView.ActionType.SELECT_ALL, tester);
    assertEquals("All walls are not selected", walls.size(), Home.getWallsSubList(home.getSelectedItems()).size());
    assertEquals("All pieces are not selected", furniture.size(), Home.getFurnitureSubList(home.getSelectedItems()).size());
    assertEquals("All rooms are not selected", rooms.size(), Home.getRoomsSubList(home.getSelectedItems()).size());
    // Select wall at left
    Point p = new Point(planView.convertXModelToScreen(0), planView.convertYModelToScreen(50));
    SwingUtilities.convertPointFromScreen(p, planViewComponent);
    tester.actionClick(planViewComponent, new ComponentLocation(p));
    List<Selectable> selectedItems = home.getSelectedItems();
    assertEquals("No wall selected", 1, selectedItems.size());
    assertTrue("No wall selected", selectedItems.get(0) instanceof Wall);
    Wall firstWall = (Wall)selectedItems.get(0);
    float oldHeight = firstWall.getHeight();
    // Modify wall
    runAction(homeController, HomeView.ActionType.MODIFY_WALL, tester);
    // Wait for wall view to be shown
    tester.waitForFrameShowing(new AWTHierarchy(), preferences.getLocalizedString(
        WallPanel.class, "wall.title"));
    // Check dialog box is displayed
    JDialog wallModificationDialog = (JDialog)new BasicFinder().find(frame, new ClassMatcher (JDialog.class, true));
    assertTrue("Wall modification dialog not showing", wallModificationDialog.isShowing());
    WallPanel wallPanel = (WallPanel)TestUtilities.findComponent(wallModificationDialog, WallPanel.class);    
    JSpinner heightSpinner = (JSpinner)TestUtilities.getField(wallPanel, "rectangularWallHeightSpinner");
    // Increase its height
    JSpinnerTester spinnerTester = new JSpinnerTester();
    for (int i = 0; i < 25; i++) {
      spinnerTester.actionIncrement(heightSpinner);
    }
    float newHeight = ((Number)heightSpinner.getValue()).floatValue();
    // Click on Ok in dialog box    
    final JOptionPane optionPane = (JOptionPane)TestUtilities.findComponent(
        wallModificationDialog, JOptionPane.class);
    tester.invokeAndWait(new Runnable() {
        public void run() {
          // Select Ok option to hide dialog box in Event Dispatch Thread
          optionPane.setValue(JOptionPane.OK_OPTION); 
        }
      });
    assertFalse("Wall modification dialog still showing", wallModificationDialog.isShowing());
    assertEquals("Wall height unchanged", newHeight, firstWall.getHeight());
    
    // Create a new level
    runAction(homeController, HomeView.ActionType.ADD_LEVEL, tester);
    List<Level> levels = home.getLevels();
    assertEquals("No new level", 2, levels.size());
    assertSame("New level isn't selected", levels.get(levels.size() - 1), home.getSelectedLevel());
    
    // Check visibility of modified wall and other walls
    for (Wall wall : walls) {
      if (wall == firstWall) {
        assertTrue("High wall not visible", wall.isAtLevel(home.getSelectedLevel()));
      } else {
        assertFalse("High wall visible", wall.isAtLevel(home.getSelectedLevel()));
      }
    }

    // Test undo
    runAction(homeController, HomeView.ActionType.UNDO, tester);
    runAction(homeController, HomeView.ActionType.UNDO, tester);
    assertEquals("Wall height not restored", oldHeight, firstWall.getHeight());
    assertEquals("Incorrect level count", 0, home.getLevels().size());
    
    runAction(homeController, HomeView.ActionType.ADD_LEVEL, tester);
    runAction(homeController, HomeView.ActionType.DELETE_LEVEL, tester);
    assertEquals("Incorrect level count", 1, home.getLevels().size());
    runAction(homeController, HomeView.ActionType.UNDO, tester);
    // Check visibility of walls
    for (Wall wall : walls) {
      assertFalse("High wall visible", wall.isAtLevel(home.getSelectedLevel()));
    }

    // Change elevation of second level
    decreaseLevelElevation(home, homeController, preferences, frame, tester);
    // Check visibility of walls
    for (Wall wall : walls) {
      assertTrue("Wall invisible", wall.isAtLevel(home.getSelectedLevel()));
    }
    // Select first level
    JTabbedPane tabbedPane = (JTabbedPane)new BasicFinder().find(planViewComponent, new ClassMatcher (JTabbedPane.class, true));
    JTabbedPaneTester tabbedPaneTester = new JTabbedPaneTester();
    tabbedPaneTester.actionSelectTab(tabbedPane, new JTabbedPaneLocation(0));
    assertEquals("First level not selected", home.getSelectedLevel(), home.getLevels().get(0));
    // Change elevation of first level
    decreaseLevelElevation(home, homeController, preferences, frame, tester);
    // Select second level
    tabbedPaneTester.actionSelectTab(tabbedPane, new JTabbedPaneLocation(1));
    assertEquals("Second level not selected", home.getSelectedLevel(), home.getLevels().get(1));

    // Create a wall checking magnetism with 1st level works
    runAction(homeController, HomeView.ActionType.CREATE_WALLS, tester);
    p = new Point(planView.convertXModelToScreen(firstWall.getXStart()) + 2, planView.convertYModelToScreen(firstWall.getYStart()) - 1);
    SwingUtilities.convertPointFromScreen(p, planViewComponent);
    tester.actionClick(planViewComponent, new ComponentLocation(p));
    p = new Point(planView.convertXModelToScreen(firstWall.getXEnd()) - 1, planView.convertYModelToScreen(firstWall.getYEnd()) + 2);
    SwingUtilities.convertPointFromScreen(p, planViewComponent);
    tester.actionClick(planViewComponent, new ComponentLocation(p), MouseEvent.BUTTON1, 2);
    assertEquals("No new wall", walls.size() + 1, home.getWalls().size());
    Wall newWall = (Wall)home.getWalls().toArray() [walls.size()];
    assertTrue("Incorrect X start " + firstWall.getXStart() + " " + newWall.getXStart(), 
        Math.abs(firstWall.getXStart() - newWall.getXStart()) < 1E-4);
    assertTrue("Incorrect Y start " + firstWall.getYStart() + " " + newWall.getYStart(), 
        Math.abs(firstWall.getYStart() - newWall.getYStart()) < 1E-4);
    assertTrue("Incorrect X end " + firstWall.getXEnd() + " " + newWall.getXEnd(), 
        Math.abs(firstWall.getXEnd() - newWall.getXEnd()) < 1E-4);
    assertTrue("Incorrect Y end " + firstWall.getYEnd() + " " + newWall.getYEnd(), 
        Math.abs(firstWall.getYEnd() - newWall.getYEnd()) < 1E-4);
    
    // Create a room checking magnetism works
    runAction(homeController, HomeView.ActionType.CREATE_ROOMS, tester);
    Room firstRoom = rooms.get(0);
    float [][] firstRoomPoints = firstRoom.getPoints();
    firstRoomPoints = new float [][] {firstRoomPoints [0], firstRoomPoints [1], firstRoomPoints [2], firstRoomPoints [firstRoomPoints.length - 1]};
    for (float [] point : firstRoomPoints) {
      p = new Point(planView.convertXModelToScreen(point [0]) + 1, planView.convertYModelToScreen(point [1]) + 1);
      SwingUtilities.convertPointFromScreen(p, planViewComponent);
      tester.actionClick(planViewComponent, new ComponentLocation(p));      
    }
    tester.actionMouseMove(planViewComponent, new ComponentLocation(new Point(0, 0)));
    tester.actionKeyStroke(KeyEvent.VK_ESCAPE);
    assertEquals("No new room", rooms.size() + 1, home.getRooms().size());
    Room newRoom = home.getRooms().get(rooms.size());
    assertEquals("Wrong point count", firstRoomPoints.length, newRoom.getPointCount());
    float [][] points = newRoom.getPoints();
    for (int i = 0; i < firstRoomPoints.length; i++) {
      assertTrue("Incorrect X [" + i + "] "  + firstRoomPoints [i][0] + " " + points [i][0], 
          Math.abs(firstRoomPoints [i][0] - points [i][0]) < 1E-4);
      assertTrue("Incorrect Y [" + i + "] " + firstRoomPoints [i][1] + " " + points [i][1], 
          Math.abs(firstRoomPoints [i][1] - points [i][1]) < 1E-4);
    }
  }

  private void decreaseLevelElevation(Home home, HomeController homeController, UserPreferences preferences,
                                      JFrame frame, JComponentTester tester) throws ComponentNotFoundException,
            MultipleComponentsFoundException, ComponentSearchException, NoSuchFieldException, IllegalAccessException {
    JSpinnerTester spinnerTester;
    runAction(homeController, HomeView.ActionType.MODIFY_LEVEL, tester);
    // Wait for level view to be shown
    tester.waitForFrameShowing(new AWTHierarchy(), preferences.getLocalizedString(
        LevelPanel.class, "level.title"));
    // Check dialog box is displayed
    final JDialog levelModificationDialog = (JDialog)new BasicFinder().find(frame, new ClassMatcher (JDialog.class, true));
    assertTrue(" Level modification dialog not showing", levelModificationDialog.isShowing());
    LevelPanel levelPanel = ( LevelPanel)TestUtilities.findComponent(levelModificationDialog, LevelPanel.class);    
    JSpinner elevationSpinner = (JSpinner)TestUtilities.getField(levelPanel, "elevationSpinner");
    // Reduce its elevation at a level where walls of 1st level will be visible
    spinnerTester = new JSpinnerTester();
    for (int i = 0; i < 25; i++) {
      spinnerTester.actionDecrement(elevationSpinner);
    }
    float newElevation = ((Number)elevationSpinner.getValue()).floatValue();
    // Click on Ok in dialog box    
    final JOptionPane levelOptionPane = (JOptionPane)TestUtilities.findComponent(
        levelModificationDialog, JOptionPane.class);
    tester.invokeAndWait(new Runnable() {
        public void run() {
          // Select Ok option to hide dialog box in Event Dispatch Thread
          levelOptionPane.setValue(JOptionPane.OK_OPTION); 
        }
      });
    assertFalse("Level modification dialog still showing", levelModificationDialog.isShowing());
    assertEquals("Level elevation unchanged", newElevation, home.getSelectedLevel().getElevation());
  }

  /**
   * Runs <code>actionPerformed</code> method matching <code>actionType</code> 
   * in <code>controller</code> view. 
   */
  private void runAction(final HomeController controller,
                         final HomePane.ActionType actionType, JComponentTester tester) {
    tester.invokeAndWait(new Runnable() { 
        public void run() {
          ((JComponent)controller.getView()).getActionMap().get(actionType).actionPerformed(null);
        }
      });
  }
}
