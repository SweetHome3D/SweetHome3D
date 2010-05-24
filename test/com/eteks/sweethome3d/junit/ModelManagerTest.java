/*
 * ModelManagerTest.java 24 mai 2010
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

import java.io.IOException;
import java.util.Enumeration;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.Link;
import javax.media.j3d.Node;
import javax.media.j3d.Shape3D;

import junit.framework.TestCase;

import com.eteks.sweethome3d.j3d.ModelManager;
import com.eteks.sweethome3d.tools.URLContent;

/**
 * Test ModelManager class.
 * @author Emmanuel Puybaret
 */
public class ModelManagerTest extends TestCase {
  public void testDAELoader() throws IOException {
    BranchGroup model = ModelManager.getInstance().loadModel(
        new URLContent(ModelManagerTest.class.getResource("resources/test.dae")));
    assertTrue("Model shouldn't be empty", getShapesCount(model) > 0);
  }

  public void testOBJLoader() throws IOException {
    BranchGroup model = ModelManager.getInstance().loadModel(
        new URLContent(ModelManagerTest.class.getResource("resources/test.obj")));
    assertTrue("Model shouldn't be empty", getShapesCount(model) > 0);
  }
  
  private int getShapesCount(Node node) {
    if (node instanceof Group) {
      int shapesCount = 0;
      Enumeration<?> enumeration = ((Group)node).getAllChildren();
      while (enumeration.hasMoreElements ()) {
        shapesCount += getShapesCount((Node)enumeration.nextElement());
      }
      return shapesCount;
    } else if (node instanceof Link) {
      return getShapesCount(((Link)node).getSharedGroup());
    } else if (node instanceof Shape3D) {
      return 1;
    } else {
      return 0;
    }
  }
}
