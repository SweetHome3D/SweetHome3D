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

import java.util.GregorianCalendar;
import java.util.TimeZone;

import junit.framework.TestCase;

import com.eteks.sweethome3d.model.Compass;

/**
 * Tests photo creation dialog.
 * @author Emmanuel Puybaret
 */
public class PhotoCreationTest extends TestCase {
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
}
