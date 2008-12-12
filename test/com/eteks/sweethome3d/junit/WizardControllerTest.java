/*
 * WizardControllerTest.java 7 juin 07
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
package com.eteks.sweethome3d.junit;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import junit.framework.TestCase;
import abbot.finder.ComponentSearchException;

import com.eteks.sweethome3d.io.DefaultUserPreferences;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.swing.SwingViewFactory;
import com.eteks.sweethome3d.swing.WizardPane;
import com.eteks.sweethome3d.viewcontroller.View;
import com.eteks.sweethome3d.viewcontroller.ViewFactory;
import com.eteks.sweethome3d.viewcontroller.WizardController;

/**
 * Tests {@link com.eteks.sweethome3d.viewcontroller.WizardController wizard controller}.
 * @author Emmanuel Puybaret
 */
public class WizardControllerTest extends TestCase {
  public void testWizardController() 
      throws NoSuchFieldException, IllegalAccessException, ComponentSearchException {
    // 1. Create a wizard controller test waiting for finish call
    final boolean [] finished = {false};
    WizardController controller = new ControllerTest(new DefaultUserPreferences(), new SwingViewFactory()) {
      @Override
      public void finish() {
        finished [0] = true;
      }
    }; 
    WizardPane view = (WizardPane)controller.getView();
    // Retrieve view back and next buttons
    JButton backOptionButton = (JButton)TestUtilities.getField(view, "backOptionButton"); 
    JButton nextFinishOptionButton = (JButton)TestUtilities.getField(view, "nextFinishOptionButton"); 
    String nextFinishOptionButtonText = nextFinishOptionButton.getText();
    // Check view displays first step view
    assertEquals("First step view class isn't correct", 
        ControllerTest.FirstStepView.class, 
        ((BorderLayout)((JPanel)view.getMessage()).getLayout()).getLayoutComponent(BorderLayout.CENTER).getClass());
    // Check back button is disabled and next button is enabled
    assertFalse("Back button isn't disabled", backOptionButton.isEnabled());
    assertTrue("Next button isn't enabled", nextFinishOptionButton.isEnabled());
    
    // 2. Click on nextFinishButton
    nextFinishOptionButton.doClick();
    // Check view displays second step view
    assertEquals("Second step view class isn't correct", 
        ControllerTest.SecondStepView.class, 
        ((BorderLayout)((JPanel)view.getMessage()).getLayout()).getLayoutComponent(BorderLayout.CENTER).getClass());
    // Check back button is enabled and next button is disabled
    assertTrue("Back button isn't enabled", backOptionButton.isEnabled());
    assertFalse("Next button isn't disabled", nextFinishOptionButton.isEnabled());
    // Check next button text changed
    assertFalse("Next button text didn't changed", 
        nextFinishOptionButton.getText().equals(nextFinishOptionButtonText));
    
    // 3. Click on backButton
    backOptionButton.doClick();
    // Check view displays first step view
    assertEquals("First step view class isn't correct", 
        ControllerTest.FirstStepView.class, 
        ((BorderLayout)((JPanel)view.getMessage()).getLayout()).getLayoutComponent(BorderLayout.CENTER).getClass());
    // Check back button is disabled and next button is enabled
    assertFalse("Back button isn't disabled", backOptionButton.isEnabled());
    assertTrue("Next button isn't enabled", nextFinishOptionButton.isEnabled());
    // Check next button text changed back to its first value
    assertEquals("Next button text didn't changed", 
        nextFinishOptionButtonText, nextFinishOptionButton.getText());
    

    // 4. Click on nextFinishButton
    nextFinishOptionButton.doClick();
    // Check view displays second step view
    assertEquals("Second step view class isn't correct", 
        ControllerTest.SecondStepView.class, 
        ((BorderLayout)((JPanel)view.getMessage()).getLayout()).getLayoutComponent(BorderLayout.CENTER).getClass());
    // Check the check box in second step view isn't selected
    JCheckBox yesCheckBox = (JCheckBox)TestUtilities.findComponent(view, JCheckBox.class);
    assertFalse("Check box is selected", yesCheckBox.isSelected());
    // Select the check box in second step view
    yesCheckBox.doClick();
    // Check the check box is selected and next button is enabled
    assertTrue("Check box isn't selected", yesCheckBox.isSelected());
    assertTrue("Next button isn't enabled", nextFinishOptionButton.isEnabled());
    
    // 5. Click on nextFinishButton
    nextFinishOptionButton.doClick();
    // Check finish was called
    assertTrue("Finish wasn't called", finished [0]);
  }
  
  public static void main(String [] args) {
    // Display the wizard controlled by ControllerTest
    new ControllerTest(new DefaultUserPreferences(), new SwingViewFactory()).displayView(null);
  }

  /**
   * A simple <code>WizardController</code> implementation that displays two steps.
   */
  private static class ControllerTest extends WizardController {
    private static URL stepIcon = WizardController.class.getResource("resources/backgroundImageWizard.png");
    
    public ControllerTest(UserPreferences preferences, ViewFactory viewFactory) {
      super(preferences, viewFactory);
      // Choose step to display
      setStepState(new FirstStep());            
    }
    
    @Override
    public void finish() {
      JOptionPane.showMessageDialog(null, "Wizard finished");
    }
    
    // First step of wizard
    private class FirstStep extends WizardControllerStepState {
      @Override
      public void enter() {
        setNextStepEnabled(true);
      }
      
      @Override
      public View getView() {
        return new FirstStepView();
      }
      
      @Override
      public URL getIcon() {
        return stepIcon;
      }
      
      @Override
      public boolean isFirstStep() {
        return true;
      }
      
      @Override
      public void goToNextStep() {
        setStepState(new SecondStep());
      }
    }
    
    // First step view is a simple label
    private static class FirstStepView extends JLabel implements View {
      public FirstStepView() {
        super("First step");
      }
    }

    // Second step of wizard
    private class SecondStep extends WizardControllerStepState {
      @Override
      public View getView() {        
        return new SecondStepView(this);
      }
      
      @Override
      public URL getIcon() {
        return stepIcon;
      }
      
      @Override
      public boolean isLastStep() {
        return true;
      }
      
      @Override
      public void goBackToPreviousStep() {
        setStepState(new FirstStep());
      }

      public void setFinishEnabled(boolean enabled) {
        // Activate next step when check box is selected
        setNextStepEnabled(enabled);
      }
    }
    
    // Second step view is a panel displaying a check box that enables next step
    private static class SecondStepView extends JPanel implements View {
      public SecondStepView(final SecondStep secondStepController) {
        add(new JLabel("Finish ?"));
        add(new JCheckBox(new AbstractAction("Yes") {
            public void actionPerformed(ActionEvent ev) {
              // Activate next step when check box is selected
              secondStepController.setFinishEnabled(
                  ((AbstractButton)ev.getSource()).isSelected());
            }
          }));
     }
    }
  }
}
