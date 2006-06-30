/*
 * IconManagerTest.java 4 mai 2006
 * 
 * Copyright (c) 2006 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights
 * Reserved.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.eteks.sweethome3d.junit;

import java.awt.Component;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import junit.framework.TestCase;

import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.swing.IconManager;
import com.eteks.sweethome3d.tools.URLContent;

/**
 * Tests IconManager class.
 * @author Emmanuel Puybaret
 */
public class IconManagerTest extends TestCase {
  private final int HEIGHT = 32;
  
  public void testIconManager() 
      throws NoSuchFieldException, IllegalAccessException, InterruptedException, BrokenBarrierException, ClassNotFoundException {
    // Stop iconsLoader of iconManager 
    IconManager iconManager = IconManager.getInstance();
    ThreadPoolExecutor iconsLoader = (ThreadPoolExecutor)getFieldValue(iconManager, "iconsLoader");
    iconsLoader.shutdownNow();
    // Replace it by an excecutor that controls the start of a task with a barrier
    final CyclicBarrier iconLoadingStartBarrier = new CyclicBarrier(2);
    final ThreadPoolExecutor replacingIconsLoader = 
      new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>()) {
        @Override
        protected void beforeExecute(Thread t, Runnable r) {
          super.beforeExecute(t, r);
          awaitBarrier(iconLoadingStartBarrier);
        }
    };
    // Redirect rejected tasks on iconsLoader to the replacing executor
    iconsLoader.setRejectedExecutionHandler(new RejectedExecutionHandler () {
      public void rejectedExecution(final Runnable r, ThreadPoolExecutor executor) {
        replacingIconsLoader.execute(r);
      }
    });
    
    // Empty existing icons to prove IconManager work
    ((Map)getFieldValue(iconManager, "icons")).clear();
    
    // Test icon loading on a good image
    testIconLoading(getClass().getResource("resources/test.png"), true, iconLoadingStartBarrier);
    // Test icon loading on a content that doesn't an image
    testIconLoading(getClass().getResource("IconManagerTest.class"), false, iconLoadingStartBarrier);

    Class iconProxyClass = Class.forName(iconManager.getClass().getName() + "$IconProxy");
    URLContent waitIconContent = (URLContent)getFieldValue(iconManager, "waitIconContent");
    URLContent errorIconContent = (URLContent)getFieldValue(iconManager, "errorIconContent");

    // Check waitIcon is loaded directly without proxy
    Icon waitIcon = iconManager.getIcon(waitIconContent, HEIGHT, null);
    assertNotSame("Wait icon loaded with IconProxy", waitIcon.getClass(), iconProxyClass);

    // Check errorIcon is loaded directly without proxy
    Icon errorIcon = iconManager.getIcon(errorIconContent, HEIGHT, null);
    assertNotSame("Error icon loaded with IconProxy", errorIcon.getClass(), iconProxyClass);

    // For other tests, replace again iconLoader by an excecutor that let icon loading complete normaly
    final Executor nextTestsIconsLoader = Executors.newFixedThreadPool(5); 
    iconsLoader.setRejectedExecutionHandler(new RejectedExecutionHandler () {
      public void rejectedExecution(final Runnable r, ThreadPoolExecutor executor) {
        nextTestsIconsLoader.execute(r);
      }
    });
  }

  /**
   * Test how an icon is loaded by IconManager. 
   */
  private void testIconLoading(URL iconURL, boolean goodIcon, CyclicBarrier iconLoadingStartBarrier) 
      throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, InterruptedException, BrokenBarrierException {
    IconManager iconManager = IconManager.getInstance();
    Class iconProxyClass = Class.forName(iconManager.getClass().getName() + "$IconProxy");
    
    URLContent waitIconContent = (URLContent)getFieldValue(iconManager, "waitIconContent");
    URLContent errorIconContent = (URLContent)getFieldValue(iconManager, "errorIconContent");
    
    final CyclicBarrier waitingComponentBarrier = new CyclicBarrier(2);
    // A dummy waiting component that waits on a barrier in its repaint method 
    Component waitingComponent = new Component() { 
      public void repaint() {
        awaitBarrier(waitingComponentBarrier); 
      }
    };

    Content iconContent = new URLContent(iconURL);
    Icon icon = iconManager.getIcon(iconContent, HEIGHT, waitingComponent);
    assertEquals("Icon not equal to wait icon while loading", waitIconContent.getURL(), icon);

    // Let iconManager load the iconContent
    iconLoadingStartBarrier.await();
    // Wait iconContent loading completion
    waitingComponentBarrier.await();
    if (goodIcon) {
      assertEquals("Icon not equal to icon read from resource", iconURL, icon);
    } else {
      assertEquals("Wrong icon not equal to errorIcon", errorIconContent.getURL(), icon);
    }
    
    // Check icon is loaded with proxy
    assertSame("Icon not loaded with IconProxy", icon.getClass(), iconProxyClass);

    // Check that icon is stored in cache
    Icon iconFromCache = iconManager.getIcon(iconContent, HEIGHT, waitingComponent);
    assertSame("Test icon reloaded", icon, iconFromCache);
  }
  
  private void awaitBarrier(CyclicBarrier barrier) {
    try {
      barrier.await();
    } catch (Exception ex) {
      fail();
    }
  }

  /**
   * Returns the value of <code>fieldName</code> in a given <code>instance</code> by reflection.
   */
  private Object getFieldValue(Object instance, String fieldName)
      throws NoSuchFieldException, IllegalAccessException {
    Field field = instance.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    return field.get(instance);
  }

  /**
   * Asserts icons in parameter at same size contains the same image data. 
   */
  private void assertEquals(String message, URL expectedIconURL, Icon actualIcon) {
    ImageIcon expectedIcon = new ImageIcon(expectedIconURL);
    Image scaledExpectedImage = expectedIcon.getImage()
        .getScaledInstance(actualIcon.getIconWidth(),
            actualIcon.getIconHeight(), Image.SCALE_SMOOTH);
    assertTrue(message, Arrays.equals(getIconData(new ImageIcon(scaledExpectedImage)), 
                                      getIconData(actualIcon)));
  }

  private int [] getIconData(Icon icon) {
    BufferedImage image = new BufferedImage(icon.getIconWidth(),
        icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
    icon.paintIcon(null, image.getGraphics(), 0, 0);
    int [] imageData = new int [icon.getIconWidth() * icon.getIconHeight()];
    return image.getRGB(0, 0, icon.getIconWidth(), icon.getIconHeight(), 
        imageData, 0, icon.getIconWidth());
  }
}
