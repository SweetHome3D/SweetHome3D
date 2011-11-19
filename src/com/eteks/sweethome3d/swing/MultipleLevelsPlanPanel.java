/*
 * MultipleLevelsPlanPanel.java 23 oct. 2011
 *
 * Sweet Home 3D, Copyright (c) 2011 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.eteks.sweethome3d.model.CollectionEvent;
import com.eteks.sweethome3d.model.CollectionListener;
import com.eteks.sweethome3d.model.DimensionLine;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.Level;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.TextStyle;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.PlanController;
import com.eteks.sweethome3d.viewcontroller.PlanController.EditableProperty;
import com.eteks.sweethome3d.viewcontroller.PlanView;
import com.eteks.sweethome3d.viewcontroller.View;

/**
 * A panel for multiple levels plans where users can select the displayed level.
 * @author Emmanuel Puybaret
 */
public class MultipleLevelsPlanPanel extends JPanel implements PlanView, Printable {
  private static final String ONE_LEVEL_PANEL_NAME = "oneLevelPanel";
  private static final String MULTIPLE_LEVELS_PANEL_NAME = "multipleLevelsPanel";
  
  private PlanComponent planComponent;
  private JScrollPane   planScrollPane;
  private JTabbedPane   multipleLevelsTabbedPane;
  private JPanel        oneLevelPanel;

  public MultipleLevelsPlanPanel(Home home, 
                                 UserPreferences preferences, 
                                 PlanController controller) {
    super(new CardLayout());
    createComponents(home, preferences, controller);
    layoutComponents();
    updateSelectedTab(home);
  }

  /**
   * Creates components displayed by this panel.
   */
  private void createComponents(final Home home, 
                                final UserPreferences preferences, final PlanController controller) {
    this.planComponent = new PlanComponent(home, preferences, controller);
    
    UIManager.getDefaults().put("TabbedPane.contentBorderInsets", OperatingSystem.isMacOSX()
        ? new Insets(2, 2, 2, 2)
        : new Insets(-1, 0, 2, 2));
    this.multipleLevelsTabbedPane = new JTabbedPane();
    if (OperatingSystem.isMacOSX()) {
      this.multipleLevelsTabbedPane.setBorder(new EmptyBorder(-2, -6, -7, -6));
    }
    List<Level> levels = home.getLevels();
    this.planScrollPane = new JScrollPane(this.planComponent);
    if (OperatingSystem.isMacOSX()) {
      this.planScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
      this.planScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    }
    
    createTabs(home, preferences);
    final ChangeListener changeListener = new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          int selectedIndex = multipleLevelsTabbedPane.getSelectedIndex();
          if (selectedIndex == multipleLevelsTabbedPane.getTabCount() - 1) {
            controller.addLevel();
          } else {
            controller.setSelectedLevel(home.getLevels().get(selectedIndex));
          }
        }
      };
    this.multipleLevelsTabbedPane.addChangeListener(changeListener);
    // Add a mouse listener that will give focus to plan component only if a change in tabbed pane comes from the mouse
    this.multipleLevelsTabbedPane.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent ev) {
          if (ev.getClickCount() == 1) {
            final Level oldSelectedLevel = home.getSelectedLevel();
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                  if (oldSelectedLevel == home.getSelectedLevel()) {
                    planComponent.requestFocusInWindow();
                  }
                }
              });
          } else {
            controller.modifySelectedLevel();
          }
        }
      });
    
    // Add listeners to levels to maintain tabs name and order
    final PropertyChangeListener levelChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          if (Level.Property.NAME.name().equals(ev.getPropertyName())) {
            multipleLevelsTabbedPane.setTitleAt(home.getLevels().indexOf(ev.getSource()), (String)ev.getNewValue());
          } else if (Level.Property.ELEVATION.name().equals(ev.getPropertyName())
              || Level.Property.HEIGHT.name().equals(ev.getPropertyName())) {
            multipleLevelsTabbedPane.removeChangeListener(changeListener);
            multipleLevelsTabbedPane.removeAll();
            createTabs(home, preferences);
            updateSelectedTab(home);
            multipleLevelsTabbedPane.addChangeListener(changeListener);
          }
        }
      };
    for (Level level : levels) {
      level.addPropertyChangeListener(levelChangeListener);
    }
    home.addLevelsListener(new CollectionListener<Level>() {
        public void collectionChanged(CollectionEvent<Level> ev) {
          multipleLevelsTabbedPane.removeChangeListener(changeListener);
          switch (ev.getType()) {
            case ADD:
              multipleLevelsTabbedPane.insertTab(ev.getItem().getName(), null, new JLabel(), null, ev.getIndex());
              ev.getItem().addPropertyChangeListener(levelChangeListener);
              break;
            case DELETE:
              ev.getItem().removePropertyChangeListener(levelChangeListener);
              multipleLevelsTabbedPane.remove(ev.getIndex());
              break;
          }
          updateLayout(home);
          multipleLevelsTabbedPane.addChangeListener(changeListener);
        }
      });
    
    home.addPropertyChangeListener(Home.Property.SELECTED_LEVEL, new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent ev) {
        if (ev.getOldValue() != null) {
          multipleLevelsTabbedPane.setComponentAt(home.getLevels().indexOf(ev.getOldValue()), new JLabel());
        }
        multipleLevelsTabbedPane.removeChangeListener(changeListener);
        updateSelectedTab(home);
        multipleLevelsTabbedPane.addChangeListener(changeListener);
      }
    });
    
    this.oneLevelPanel = new JPanel(new BorderLayout());
    
    preferences.addPropertyChangeListener(UserPreferences.Property.LANGUAGE, 
        new LanguageChangeListener(this));
  }

  /**
   * Preferences property listener bound to this component with a weak reference to avoid
   * strong link between preferences and this component.  
   */
  private static class LanguageChangeListener implements PropertyChangeListener {
    private WeakReference<MultipleLevelsPlanPanel> planPanel;

    public LanguageChangeListener(MultipleLevelsPlanPanel planPanel) {
      this.planPanel = new WeakReference<MultipleLevelsPlanPanel>(planPanel);
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
      // If help pane was garbage collected, remove this listener from preferences
      MultipleLevelsPlanPanel planPanel = this.planPanel.get();
      UserPreferences preferences = (UserPreferences)ev.getSource();
      if (planPanel == null) {
        preferences.removePropertyChangeListener(UserPreferences.Property.LANGUAGE, this);
      } else {
        // Update create level tooltip in new locale
        String createNewLevelTooltip = preferences.getLocalizedString(MultipleLevelsPlanPanel.class, "ADD_LEVEL.ShortDescription");
        planPanel.multipleLevelsTabbedPane.setToolTipTextAt(planPanel.multipleLevelsTabbedPane.getTabCount() - 1, createNewLevelTooltip);
      }
    }
  }

  /**
   * Creates the tabs from <code>home</code> levels.
   */
  private void createTabs(Home home, UserPreferences preferences) {
    for (Level level : home.getLevels()) {
      this.multipleLevelsTabbedPane.add(level.getName(), new JLabel());
    }
    String createNewLevelIcon = preferences.getLocalizedString(MultipleLevelsPlanPanel.class, "ADD_LEVEL.SmallIcon");
    String createNewLevelTooltip = preferences.getLocalizedString(MultipleLevelsPlanPanel.class, "ADD_LEVEL.ShortDescription");
    this.multipleLevelsTabbedPane.addTab("", new ImageIcon(MultipleLevelsPlanPanel.class.getResource(createNewLevelIcon)), 
        new JLabel(), createNewLevelTooltip);
  }
  
  /**
   * Selects the tab matching the selected level in <code>home</code>.
   */
  private void updateSelectedTab(Home home) {
    List<Level> levels = home.getLevels();
    Level selectedLevel = home.getSelectedLevel();
    if (levels.size() >= 2 && selectedLevel != null) {
      int levelIndex = levels.indexOf(selectedLevel);
      this.multipleLevelsTabbedPane.setComponentAt(levelIndex, this.planScrollPane);
      this.multipleLevelsTabbedPane.setSelectedIndex(levelIndex);
    }
    updateLayout(home);
  }

  /**
   * Switches between a simple plan component view and a tabbed pane for multiple levels.
   */
  private void updateLayout(Home home) {
    CardLayout layout = (CardLayout)getLayout();
    List<Level> levels = home.getLevels();
    boolean focus = this.planComponent.hasFocus();
    if (levels.size() < 2 || home.getSelectedLevel() == null) {
      int planIndex = this.multipleLevelsTabbedPane.indexOfComponent(this.planScrollPane);
      if (planIndex != -1) {
        // Replace plan component by a dummy label to avoid losing tab
        this.multipleLevelsTabbedPane.setComponentAt(planIndex, new JLabel());
      }
      this.oneLevelPanel.add(this.planScrollPane);      
      layout.show(this, ONE_LEVEL_PANEL_NAME);
    } else {
      layout.show(this, MULTIPLE_LEVELS_PANEL_NAME);
    }
    if (focus) {
      this.planComponent.requestFocusInWindow();
    }
  }

  /**
   * Layouts the components displayed by this panel.
   */
  private void layoutComponents() {
    add(this.multipleLevelsTabbedPane, MULTIPLE_LEVELS_PANEL_NAME);
    add(this.oneLevelPanel, ONE_LEVEL_PANEL_NAME);
    
    SwingTools.installFocusBorder(this.planComponent);
    setFocusTraversalPolicyProvider(false);
  }

  @Override
  public void setTransferHandler(TransferHandler newHandler) {
    this.planComponent.setTransferHandler(newHandler);
  }
  
  @Override
  public void setComponentPopupMenu(JPopupMenu popup) {
    this.planComponent.setComponentPopupMenu(popup);
  }
  
  @Override
  public void addMouseMotionListener(final MouseMotionListener l) {
    this.planComponent.addMouseMotionListener(new MouseMotionListener() {
        public void mouseMoved(MouseEvent ev) {
          Point point = SwingUtilities.convertPoint(planComponent, ev.getPoint(), MultipleLevelsPlanPanel.this);
          l.mouseMoved(new MouseEvent(MultipleLevelsPlanPanel.this, MouseEvent.MOUSE_MOVED, ev.getWhen(), 
              ev.getModifiers(), point.x, point.y, ev.getClickCount(), ev.isPopupTrigger(), ev.getButton()));
        }
        
        public void mouseDragged(MouseEvent ev) {
          Point point = SwingUtilities.convertPoint(planComponent, ev.getPoint(), MultipleLevelsPlanPanel.this);
          l.mouseDragged(new MouseEvent(MultipleLevelsPlanPanel.this, MouseEvent.MOUSE_DRAGGED, ev.getWhen(), 
              ev.getModifiers(), point.x, point.y, ev.getClickCount(), ev.isPopupTrigger(), ev.getButton()));
        }
      });
  }
  
  @Override
  public void addMouseListener(final MouseListener l) {
    this.planComponent.addMouseListener(new MouseListener() {
        public void mouseReleased(MouseEvent ev) {
          Point point = SwingUtilities.convertPoint(planComponent, ev.getPoint(), MultipleLevelsPlanPanel.this);
          l.mouseReleased(new MouseEvent(MultipleLevelsPlanPanel.this, MouseEvent.MOUSE_RELEASED, ev.getWhen(), 
              ev.getModifiers(), point.x, point.y, ev.getClickCount(), ev.isPopupTrigger(), ev.getButton()));
        }
        
        public void mousePressed(MouseEvent ev) {
          Point point = SwingUtilities.convertPoint(planComponent, ev.getPoint(), MultipleLevelsPlanPanel.this);
          l.mousePressed(new MouseEvent(MultipleLevelsPlanPanel.this, MouseEvent.MOUSE_PRESSED, ev.getWhen(), 
              ev.getModifiers(), point.x, point.y, ev.getClickCount(), ev.isPopupTrigger(), ev.getButton()));
        }
        
        public void mouseExited(MouseEvent ev) {
          Point point = SwingUtilities.convertPoint(planComponent, ev.getPoint(), MultipleLevelsPlanPanel.this);
          l.mouseExited(new MouseEvent(MultipleLevelsPlanPanel.this, MouseEvent.MOUSE_EXITED, ev.getWhen(), 
              ev.getModifiers(), point.x, point.y, ev.getClickCount(), ev.isPopupTrigger(), ev.getButton()));
        }
        
        public void mouseEntered(MouseEvent ev) {
          Point point = SwingUtilities.convertPoint(planComponent, ev.getPoint(), MultipleLevelsPlanPanel.this);
          l.mouseEntered(new MouseEvent(MultipleLevelsPlanPanel.this, MouseEvent.MOUSE_ENTERED, ev.getWhen(), 
              ev.getModifiers(), point.x, point.y, ev.getClickCount(), ev.isPopupTrigger(), ev.getButton()));
        }
        
        public void mouseClicked(MouseEvent ev) {
          Point point = SwingUtilities.convertPoint(planComponent, ev.getPoint(), MultipleLevelsPlanPanel.this);
          l.mouseClicked(new MouseEvent(MultipleLevelsPlanPanel.this, MouseEvent.MOUSE_CLICKED, ev.getWhen(), 
              ev.getModifiers(), point.x, point.y, ev.getClickCount(), ev.isPopupTrigger(), ev.getButton()));
        }
      });
  }
  
  @Override
  public void addFocusListener(final FocusListener l) {
    FocusListener componentFocusListener = new FocusListener() {
        public void focusGained(FocusEvent ev) {
          l.focusGained(new FocusEvent(MultipleLevelsPlanPanel.this, FocusEvent.FOCUS_GAINED, ev.isTemporary(), ev.getOppositeComponent()));
        }
        
        public void focusLost(FocusEvent ev) {
          l.focusLost(new FocusEvent(MultipleLevelsPlanPanel.this, FocusEvent.FOCUS_LOST, ev.isTemporary(), ev.getOppositeComponent()));
        }
      };
    this.planComponent.addFocusListener(componentFocusListener);
    this.multipleLevelsTabbedPane.addFocusListener(componentFocusListener);
  }
  
  /**
   * Sets rectangle selection feedback coordinates. 
   */
  public void setRectangleFeedback(float x0, float y0, float x1, float y1) {
    this.planComponent.setRectangleFeedback(x0, y0, x1, y1);
  }

  /**
   * Ensures selected items are visible in the plan displayed by this component and moves
   * its scroll bars if needed.
   */
  public void makeSelectionVisible() {
    this.planComponent.makeSelectionVisible();
  }

  /**
   * Ensures the point at (<code>x</code>, <code>y</code>) is visible in the plan displayed by this component,
   * moving its scroll bars if needed.
   */
  public void makePointVisible(float x, float y) {
    this.planComponent.makePointVisible(x, y);
  }

  /**
   * Returns the scale used to display the plan displayed by this component.
   */
  public float getScale() {
    return this.planComponent.getScale();
  }

  /**
   * Sets the scale used to display the plan displayed by this component.
   */
  public void setScale(float scale) {
    this.planComponent.setScale(scale);
  }

  /**
   * Moves the plan displayed by this component from (dx, dy) unit in the scrolling zone it belongs to.
   */
  public void moveView(float dx, float dy) {
    this.planComponent.moveView(dx, dy);
  }

  /**
   * Returns <code>x</code> converted in model coordinates space.
   */
  public float convertXPixelToModel(int x) {
    return this.planComponent.convertXPixelToModel(SwingUtilities.convertPoint(this, x, 0, this.planComponent).x);
  }

  /**
   * Returns <code>y</code> converted in model coordinates space.
   */
  public float convertYPixelToModel(int y) {
    return this.planComponent.convertYPixelToModel(SwingUtilities.convertPoint(this, 0, y, this.planComponent).y);
  }

  /**
   * Returns <code>x</code> converted in screen coordinates space.
   */
  public int convertXModelToScreen(float x) {
    return this.planComponent.convertXModelToScreen(x);
  }

  /**
   * Returns <code>y</code> converted in screen coordinates space.
   */
  public int convertYModelToScreen(float y) {
    return this.planComponent.convertYModelToScreen(y);
  }

  /**
   * Returns the length in centimeters of a pixel with the current scale.
   */
  public float getPixelLength() {
    return this.planComponent.getPixelLength();
  }

  /**
   * Returns the coordinates of the bounding rectangle of the <code>text</code> displayed at
   * the point (<code>x</code>,<code>y</code>).  
   */
  public float [][] getTextBounds(String text, TextStyle style, float x, float y, float angle) {
    return this.planComponent.getTextBounds(text, style, x, y, angle);
  }

  /**
   * Sets the cursor of this component as rotation cursor. 
   */
  public void setCursor(CursorType cursorType) {
    this.planComponent.setCursor(cursorType);
  }

  /**
   * Sets tool tip text displayed as feedback. 
   */
  public void setToolTipFeedback(String toolTipFeedback, float x, float y) {
    this.planComponent.setToolTipFeedback(toolTipFeedback, x, y);
  }

  /**
   * Set properties edited in tool tip.
   */
  public void setToolTipEditedProperties(EditableProperty [] toolTipEditedProperties, Object [] toolTipPropertyValues,
                                         float x, float y) {
    this.planComponent.setToolTipEditedProperties(toolTipEditedProperties, toolTipPropertyValues, x, y);
  }

  /**
   * Deletes tool tip text from screen. 
   */
  public void deleteToolTipFeedback() {
    this.planComponent.deleteToolTipFeedback();
  }

  /**
   * Sets whether the resize indicator of selected wall or piece of furniture 
   * should be visible or not. 
   */
  public void setResizeIndicatorVisible(boolean visible) {
    this.planComponent.setResizeIndicatorVisible(visible);
  }

  /**
   * Sets the location point for alignment feedback.
   */
  public void setAlignmentFeedback(Class<? extends Selectable> alignedObjectClass, Selectable alignedObject, float x,
                                   float y, boolean showPoint) {
    this.planComponent.setAlignmentFeedback(alignedObjectClass, alignedObject, x, y, showPoint);
  }

  /**
   * Sets the points used to draw an angle in the plan displayed by this component.
   */
  public void setAngleFeedback(float xCenter, float yCenter, float x1, float y1, float x2, float y2) {
    this.planComponent.setAngleFeedback(xCenter, yCenter, x1, y1, x2, y2);
  }

  /**
   * Sets the feedback of dragged items drawn during a drag and drop operation, 
   * initiated from outside of the plan displayed by this component.
   */
  public void setDraggedItemsFeedback(List<Selectable> draggedItems) {
    this.planComponent.setDraggedItemsFeedback(draggedItems);
  }

  /**
   * Sets the given dimension lines to be drawn as feedback.
   */
  public void setDimensionLinesFeedback(List<DimensionLine> dimensionLines) {
    this.planComponent.setDimensionLinesFeedback(dimensionLines);
  }

  /**
   * Deletes all elements shown as feedback.
   */
  public void deleteFeedback() {
    this.planComponent.deleteFeedback();
  }
  
  /**
   * Returns <code>true</code> if the given coordinates belong to the plan displayed by this component.
   */
  public boolean canImportDraggedItems(List<Selectable> items, int x, int y) {
    JViewport viewport = this.planScrollPane.getViewport();
    Point point = SwingUtilities.convertPoint(this, x, y, viewport);
    return viewport.contains(point);
  }

  /**
   * Returns the component used as an horizontal ruler for the plan displayed by this component.
   */
  public View getHorizontalRuler() {
    return this.planComponent.getHorizontalRuler();
  }

  /**
   * Returns the component used as a vertical ruler for the plan displayed by this component.
   */
  public View getVerticalRuler() {
    return this.planComponent.getVerticalRuler();
  }

  /**
   * Prints the plan component.
   */
  public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
    return this.planComponent.print(graphics, pageFormat, pageIndex);
  }
}
