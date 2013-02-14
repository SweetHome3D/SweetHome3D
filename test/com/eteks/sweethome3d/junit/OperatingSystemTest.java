/*
 * OperatingSystemTest.java 26 janv. 2013
 *
 * Sweet Home 3D, Copyright (c) 2013 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import com.eteks.sweethome3d.tools.OperatingSystem;

import junit.framework.TestCase;

/**
 * Tests operating system utilities.
 * @author Emmanuel Puybaret
 */
public class OperatingSystemTest extends TestCase {
  /**
   * Tests {@linkplain OperatingSystem#compareVersions(String, String) versions comparator}.
   */
  public void testVersions() {
    assertVersionIsSmaller("", "1");
    assertVersionIsSmaller("0", "1.0");
    assertVersionIsSmaller("1.2beta", "1.2");
    assertVersionIsSmaller("1.2beta", "1.2beta2");
    assertVersionIsSmaller("1.2beta", "1.2.0");
    assertVersionIsSmaller("1.2beta4", "1.2beta10");
    assertVersionIsSmaller("1.2beta4", "1.2");
    assertVersionIsSmaller("1.2beta4", "1.2rc");
    assertVersionIsSmaller("1.2alpha", "1.2beta");
    assertVersionIsSmaller("1.2beta", "1.2rc");
    assertVersionIsSmaller("1.2rc", "1.2");
    assertVersionIsSmaller("1.2rc", "1.2a");
    assertVersionIsSmaller("1.2", "1.2a");
    assertVersionIsSmaller("1.2a", "1.2b");
    assertVersionIsSmaller("1.7.0_11", "1.7.0_12");
    assertVersionIsSmaller("1.7.0_11rc1", "1.7.0_11rc2");
    assertVersionIsSmaller("1.7.0_11rc", "1.7.0_11");
    assertVersionIsSmaller("1.7.0_9", "1.7.0_11rc");
    assertVersionIsSmaller("1.2", "1.2.1");
    assertVersionIsSmaller("1.2", "1.2.0.1");
    // Missing information is considered as 0
    assertVersionIsEqual("1.2", "1.2.0.0");
    // Punctuation (or missing punctuation) doesn't influence result
    assertVersionIsEqual("1.2beta4", "1.2 beta-4");
    assertVersionIsEqual("1.2beta4", "1,2,beta,4");
    
    // Can be used to compare file names too 
    // (if their version number uses only alpha, beta and rc pre-release strings)
    assertVersionIsSmaller("plugin1.2.sh3p", "plugin1.3.sh3p");
    assertVersionIsSmaller("plugin1.2.1.sh3p", "plugin1.3.sh3p");
    assertVersionIsSmaller("plugin1.2beta.sh3p", "plugin1.2.sh3p");
    assertVersionIsSmaller("plugin1.2beta1.sh3p", "plugin1.2-beta2.sh3p");
  }
  
  private void assertVersionIsSmaller(String version1, String version2) {
    assertTrue(version1 + " should be smaller than " + version2, OperatingSystem.compareVersions(version1, version2) < 0);
    assertTrue(version2 + " should be greater than " + version1, OperatingSystem.compareVersions(version2, version1) > 0);
  }

  private void assertVersionIsEqual(String version1, String version2) {
    assertTrue(version1 + " should be equal to " + version2, OperatingSystem.compareVersions(version1, version2) == 0);
    assertTrue(version2 + " should be equal to " + version1, OperatingSystem.compareVersions(version2, version1) == 0);
  }
}
