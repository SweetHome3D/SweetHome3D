/*
 * Compass.java 18 mars 2010
 *
 * Copyright (c) 2010 Frédéric Mantegazza. All Rights Reserved.
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
package com.eteks.sweethome3d.model;

import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * A compass used to locate where a home is located and how it's oriented towards North.
 * @since 3.0 
 * @author Emmanuel Puybaret
 * @author Frédéric Mantegazza (Sun location algorithm)
 */
public class Compass implements Serializable, Selectable {
  /**
   * The properties of a compass that may change. <code>PropertyChangeListener</code>s added 
   * to a wall will be notified under a property name equal to the string value of one these properties.
   */
  public enum Property {X, Y, DIAMETER, VISIBLE, NORTH_DIRECTION, LATITUDE, LONGITUDE, TIME_ZONE}
  
  private static final long serialVersionUID = 1L;

  private float    x;
  private float    y;
  private float    diameter;
  private boolean  visible;
  private float    northDirection;
  private float    latitude;
  private float    longitude;
  private TimeZone timeZone;
  
  private transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
  private transient float [][] pointsCache;
  private transient Calendar   dateCache;
  private transient float      sunElevationCache;
  private transient float      sunAzimuthCache;


  private static WeakReference<Map<String, GeographicPoint>> timeZoneGeographicPointsReference;

  /**
   * Creates a compass drawn at the given point.
   * North direction is set to zero, time zone to default 
   * and the latitudeInDegrees and the longitudeInDegrees of this new compass is equal
   * to the geographic point matching the default time zone.
   */
  public Compass(float x, float y, float diameter) {
    this.x = x;
    this.y = y;
    this.diameter = diameter;
    this.visible = true;
    this.timeZone = TimeZone.getDefault();
    initGeographicPoint();
  }  

  /**
   * Initializes compass transient fields  
   * and reads compass from <code>in</code> stream with default reading method.
   */
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    this.propertyChangeSupport = new PropertyChangeSupport(this);
    in.defaultReadObject();
  }

  /**
   * Adds the property change <code>listener</code> in parameter to this compass.
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    this.propertyChangeSupport.addPropertyChangeListener(listener);
  }

  /**
   * Removes the property change <code>listener</code> in parameter from this compass.
   */
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    this.propertyChangeSupport.removePropertyChangeListener(listener);
  }

  /**
   * Returns the abscissa of the center of this compass.
   */
  public float getX() {
    return this.x;
  }
  
  /**
   * Sets the abscissa of the center of this compass. Once this compass is updated, 
   * listeners added to this compass will receive a change notification.
   */
  public void setX(float x) {
    if (x != this.x) {
      float oldX = this.x;
      this.x = x;
      this.pointsCache = null;
      this.propertyChangeSupport.firePropertyChange(Property.X.name(), oldX, x);
    }
  }
  
  /**
   * Returns the ordinate of the center of this compass.
   */
  public float getY() {
    return this.y;
  }
  
  /**
   * Sets the ordinate of the center of this compass. Once this compass is updated, 
   * listeners added to this compass will receive a change notification.
   */
  public void setY(float y) {
    if (y != this.y) {
      float oldY = this.y;
      this.y = y;
      this.pointsCache = null;
      this.propertyChangeSupport.firePropertyChange(Property.Y.name(), oldY, y);
    }
  }

  /**
   * Returns the diameter of this compass.
   */
  public float getDiameter() {
    return this.diameter;
  }
  
  /**
   * Sets the diameter of this compass. Once this compass is updated, 
   * listeners added to this compass will receive a change notification.
   */
  public void setDiameter(float diameter) {
    if (diameter != this.diameter) {
      float oldDiameter = this.diameter;
      this.diameter = diameter;
      this.pointsCache = null;
      this.propertyChangeSupport.firePropertyChange(Property.DIAMETER.name(), oldDiameter, diameter);
    }
  }

  /**
   * Returns <code>true</code> if this compass is visible.
   */
  public boolean isVisible() {
    return this.visible;
  }
  
  /**
   * Sets whether this compass is visible or not. Once this compass is updated, 
   * listeners added to this piece will receive a change notification.
   */
  public void setVisible(boolean visible) {
    if (visible != this.visible) {
      this.visible = visible;
      this.propertyChangeSupport.firePropertyChange(Property.VISIBLE.name(), !visible, visible);
    }
  }

  /**
   * Returns the North direction angle of this compass in radians.
   */
  public float getNorthDirection() {
    return this.northDirection;
  }
  
  /**
   * Sets the North direction angle of this compass. Once this compass is updated, 
   * listeners added to this compass will receive a change notification.
   */
  public void setNorthDirection(float northDirection) {
    if (northDirection != this.northDirection) {
      float oldNorthDirection = this.northDirection;
      this.northDirection = northDirection;
      this.pointsCache = null;
      this.propertyChangeSupport.firePropertyChange(Property.NORTH_DIRECTION.name(), oldNorthDirection, northDirection);
    }
  }
  
  /**
   * Returns the latitudeInDegrees of this compass in radians.
   */
  public final float getLatitude() {
    return this.latitude;
  }
  
  /**
   * Sets the latitudeInDegrees of this compass. Once this compass is updated, 
   * listeners added to this compass will receive a change notification.
   */
  public void setLatitude(float latitude) {
    if (latitude != this.latitude) {
      float oldLatitude = this.latitude;
      this.latitude = latitude;
      this.dateCache = null;
      this.propertyChangeSupport.firePropertyChange(Property.LATITUDE.name(), oldLatitude, latitude);
    }
  }
  
  /**
   * Returns the longitudeInDegrees of this compass in radians.
   */
  public final float getLongitude() {
    return this.longitude;
  }
  
  /**
   * Sets the longitudeInDegrees of the center of this compass. Once this compass is updated, 
   * listeners added to this compass will receive a change notification.
   */
  public void setLongitude(float longitude) {
    if (longitude != this.longitude) {
      float oldLongitude = this.longitude;
      this.longitude = longitude;
      this.dateCache = null;
      this.propertyChangeSupport.firePropertyChange(Property.LONGITUDE.name(), oldLongitude, longitude);
    }
  }
  
  /**
   * Returns the time zone identifier of this compass.
   * @see java.util.TimeZone
   */
  public String getTimeZone() {
    return this.timeZone.getID();
  }
  
  /**
   * Sets the time zone identifier of this compass. Once this compass is updated, 
   * listeners added to this compass will receive a change notification.
   * @throws IllegalArgumentException if <code>timeZone</code> is <code>null</code> or contains an unknown identifier.
   * @see java.util.TimeZone
   */
  public void setTimeZone(String timeZone) {
    if (!this.timeZone.getID().equals(timeZone)) {
      if (timeZone == null) {
        throw new IllegalArgumentException("Time zone ID can't be null");
      }
      String oldTimeZone = this.timeZone.getID();
      this.timeZone = TimeZone.getTimeZone(timeZone);
      this.dateCache = null;
      this.propertyChangeSupport.firePropertyChange(Property.TIME_ZONE.name(), oldTimeZone, timeZone);
    }
  }
  
  /**
   * Returns the corner points of the square that contains compass disc.  
   */
  public float [][] getPoints() {
    if (this.pointsCache == null) {
      // Create the rectangle that matches piece bounds
      Rectangle2D pieceRectangle = new Rectangle2D.Float(
          getX() - getDiameter() / 2,
          getY() - getDiameter() / 2,
          getDiameter(), getDiameter());
      // Apply rotation to the rectangle
      AffineTransform rotation = new AffineTransform();
      rotation.setToRotation(getNorthDirection(), getX(), getY());
      this.pointsCache = new float[4][2];
      PathIterator it = pieceRectangle.getPathIterator(rotation);
      for (int i = 0; i < this.pointsCache.length; i++) {
        it.currentSegment(this.pointsCache [i]);
        it.next();
      }
    }
    return new float [][] {this.pointsCache [0].clone(), this.pointsCache [1].clone(), 
                           this.pointsCache [2].clone(), this.pointsCache [3].clone()};
  }

  /**
   * Returns <code>true</code> if the disc of this compass intersects
   * with the horizontal rectangle which opposite corners are at points
   * (<code>x0</code>, <code>y0</code>) and (<code>x1</code>, <code>y1</code>).
   */
  public boolean intersectsRectangle(float x0, float y0, 
                                     float x1, float y1) {
    Rectangle2D rectangle = new Rectangle2D.Float(x0, y0, 0, 0);
    rectangle.add(x1, y1);
    return new Ellipse2D.Float(getX() - getDiameter() / 2, getY() - getDiameter() / 2, getDiameter(), getDiameter()).intersects(rectangle);
  }
  
  /**
   * Returns <code>true</code> if the disc of this compass contains 
   * the point at (<code>x</code>, <code>y</code>)
   * with a given <code>margin</code>.
   */
  public boolean containsPoint(float x, float y, float margin) {
    Ellipse2D shape = new Ellipse2D.Float(getX() - getDiameter() / 2, getY() - getDiameter() / 2, getDiameter(), getDiameter());
    if (margin == 0) {
      return shape.contains(x, y);
    } else {
      return shape.intersects(x - margin, y - margin, 2 * margin, 2 * margin);
    }
  }

  /**
   * Moves this compass of (<code>dx</code>, <code>dy</code>) units.
   */
  public void move(float dx, float dy) {
    setX(getX() + dx);
    setY(getY() + dy);
  }

  /**
   * Returns a clone of this compass.
   */
  @Override
  public Compass clone() {
    try {
      Compass clone = (Compass)super.clone();
      clone.propertyChangeSupport = new PropertyChangeSupport(clone);
      return clone;
    } catch (CloneNotSupportedException ex) {
      throw new IllegalStateException("Super class isn't cloneable"); 
    }
  }

  /**
   * Returns the elevation angle of the Sun in the sky in radians at a given 
   * <code>date</code> in milliseconds since the Epoch.
   * See <a href="http://en.wikipedia.org/wiki/Horizontal_coordinate_system">Sun 
   * azimuth and elevation angles</a> for more information.
   */
  public float getSunElevation(long date) {
    updateSunLocation(date);
    return this.sunElevationCache;
  }
  
  /**
   * Returns the azimuth angle of the Sun in the sky in radians at a given 
   * <code>date</code> in milliseconds since the Epoch.
   * See <a href="http://en.wikipedia.org/wiki/Horizontal_coordinate_system">Sun 
   * azimuth and elevation angles</a> for more information.
   */
  public float getSunAzimuth(long date) {
    updateSunLocation(date);
    return this.sunAzimuthCache;
  }

  /**
   * Computes the Sun's location in the sky at a given <code>date</code>.
   */
  private void updateSunLocation(long date) {
    if (this.dateCache == null
        || this.dateCache.getTimeInMillis() != date) {
      this.dateCache = new GregorianCalendar(this.timeZone);
      this.dateCache.setTimeInMillis(date);
  
      int year = this.dateCache.get(Calendar.YEAR);
      int month = this.dateCache.get(Calendar.MONTH) + 1; // Based on 1 for January
      int day = this.dateCache.get(Calendar.DAY_OF_MONTH);
      int hour = this.dateCache.get(Calendar.HOUR_OF_DAY);
      int minute = this.dateCache.get(Calendar.MINUTE);
      int second = this.dateCache.get(Calendar.SECOND);
      int timeZone = this.dateCache.getTimeZone().getRawOffset() / 3600000;
      int savingTime = this.dateCache.get(Calendar.DST_OFFSET) / 3600000;
      
      double julianDay = computeJulianDay(year, month, day, hour, minute, second, timeZone, savingTime);
      double siderealTime = toSiderealTime(julianDay);
      double angleH = 360. * siderealTime / 23.9344;
      double angleT = (hour - (timeZone + savingTime) - 12. + minute / 60. + second / 3600.) * 360. / 23.9344;
      double angle = angleH + angleT;
  
      // Compute equatorial coordinates
      double g = 357.529 + 0.98560028 * julianDay;
      double q = 280.459 + 0.98564736 * julianDay;
      double l = q + 1.915 * Math.sin(Math.toRadians(g)) + 0.020 * Math.sin(Math.toRadians(2 * g));
      double e = 23.439 - 0.00000036 * julianDay;
      double rightAscension = Math.toDegrees(Math.atan(Math.cos(Math.toRadians(e)) * Math.sin(Math.toRadians(l)) / Math.cos(Math.toRadians(l)))) / 15.;
      if (Math.cos(Math.toRadians(l)) < 0.) {
        rightAscension += 12.;
      }
      if ((Math.cos(Math.toRadians(l)) > 0.) && (Math.sin(Math.toRadians(l)) < 0.)) {
        rightAscension += 24.;
      }
      double declination = Math.asin(Math.sin(Math.toRadians(e)) * Math.sin(Math.toRadians(l)));
  
      double hourAngle = Math.toRadians(angle - rightAscension * 15. + Math.toDegrees(this.longitude));
      double elevation = Math.asin(Math.sin(declination) * Math.sin(this.latitude) - Math.cos(declination) * Math.cos(this.latitude) * Math.cos(hourAngle));
      double azimuth = Math.acos((Math.sin(declination) - Math.sin(this.latitude) * Math.sin(elevation)) / (Math.cos(this.latitude) * Math.cos(elevation)));
      double sinAzimuth = (Math.cos(declination) * Math.sin(hourAngle)) / Math.cos(elevation);
      if (sinAzimuth < 0.) {
        azimuth = Math.PI * 2 - azimuth;
      }
  
      this.sunElevationCache = (float)elevation;
      this.sunAzimuthCache = (float)azimuth;
    }
  }  
  
  private double computeJulianDay(int year, int month, int day, int hour, int minute, int second, int timeZone, int savingTime) {
    double dayPart = day + hour / 24. + minute / 1440. + second / 86400.;
    if (month == 1 || month == 2) {
        year -= 1;
        month += 12;
    }
    int a = year / 100;
    int b = 2 - a + a / 4;

    double julianDay = (int)(365.25 * (year + 4716.)) + (int)((30.6001 * (month + 1))) + dayPart + b - 1524.5;
    julianDay -= (timeZone + savingTime) / 24.;
    julianDay -= 2451545.;
    return julianDay;
  }

  private double toSiderealTime(double julianDay) {
    double centuries = julianDay / 36525.;
    double siderealTime = (24110.54841 + (8640184.812866 * centuries) + (0.093104 * Math.pow(centuries, 2.)) - (0.0000062 * Math.pow(centuries, 3.))) / 3600.;
    return ((siderealTime / 24.) - (int)(siderealTime / 24.)) * 24.;
  }
  
  /**
   * Inits the latitudeInDegrees and longitudeInDegrees where this compass is located from the id of the default time zone.
   */
  private void initGeographicPoint() {
    Map<String, GeographicPoint> timeZoneGeographicPoints;
    if (timeZoneGeographicPointsReference == null
        || timeZoneGeographicPointsReference.get() == null) {
      timeZoneGeographicPoints = new HashMap<String, GeographicPoint>();
      // Default geographic points for Java 6 time zone ids
      // from free data provided by MaxMind WorldCities and Postal Code Databases
      // at http://www.maxmind.com/app/worldcities and from Wikipedia
      GeographicPoint apia = new GeographicPoint(-13.8333333f, -171.7333333f);
      timeZoneGeographicPoints.put("Etc/GMT+11", apia); // Apia
      timeZoneGeographicPoints.put("Pacific/Apia", apia);
      timeZoneGeographicPoints.put("Pacific/Midway", new GeographicPoint(28.2f, -177.35f));
      timeZoneGeographicPoints.put("Pacific/Niue", new GeographicPoint(-19.055f, -169.92f)); // Alofi
      timeZoneGeographicPoints.put("Pacific/Pago_Pago", new GeographicPoint(-14.2780556f, -170.7025000f));
      timeZoneGeographicPoints.put("Pacific/Samoa", apia);
      timeZoneGeographicPoints.put("US/Samoa", apia);
      timeZoneGeographicPoints.put("America/Adak", new GeographicPoint(51.8800000f, -176.6580556f));
      timeZoneGeographicPoints.put("America/Atka", new GeographicPoint(52.1961111f, -174.2005556f));
      GeographicPoint honolulu = new GeographicPoint(21.3069444f, -157.8583333f);
      timeZoneGeographicPoints.put("Etc/GMT+10", honolulu); // Honolulu
      timeZoneGeographicPoints.put("Pacific/Fakaofo", new GeographicPoint(-9.3653f, -171.215f));
      timeZoneGeographicPoints.put("Pacific/Honolulu", honolulu);
      timeZoneGeographicPoints.put("Pacific/Johnston", new GeographicPoint(16.75f, -169.517f));
      timeZoneGeographicPoints.put("Pacific/Rarotonga", new GeographicPoint(-21.233f, -159.783f));
      timeZoneGeographicPoints.put("Pacific/Tahiti", new GeographicPoint(-17.5333333f, -149.5666667f)); // Papeete
      timeZoneGeographicPoints.put("SystemV/HST10", honolulu); // Honolulu
      timeZoneGeographicPoints.put("US/Aleutian", new GeographicPoint(54.817f, 164.033f));
      timeZoneGeographicPoints.put("US/Hawaii", honolulu); // Honolulu
      timeZoneGeographicPoints.put("Pacific/Marquesas", new GeographicPoint(-9.45f, -139.38f));
      GeographicPoint anchorage = new GeographicPoint(61.2180556f, -149.9002778f);
      timeZoneGeographicPoints.put("America/Anchorage", anchorage);
      timeZoneGeographicPoints.put("America/Juneau", new GeographicPoint(58.3019444f, -134.4197222f));
      timeZoneGeographicPoints.put("America/Nome", new GeographicPoint(64.5011111f, -165.4063889f));
      timeZoneGeographicPoints.put("America/Yakutat", new GeographicPoint(59.5469444f, -139.7272222f)); 
      timeZoneGeographicPoints.put("Etc/GMT+9", anchorage); // Anchorage
      timeZoneGeographicPoints.put("Pacific/Gambier", new GeographicPoint(-23.1178f, -134.97f));
      timeZoneGeographicPoints.put("SystemV/YST9", anchorage); // Anchorage
      timeZoneGeographicPoints.put("SystemV/YST9YDT", anchorage); // Anchorage
      timeZoneGeographicPoints.put("US/Alaska", anchorage); // Anchorage
      timeZoneGeographicPoints.put("America/Dawson", new GeographicPoint(64.066667f, -139.416667f));
      timeZoneGeographicPoints.put("America/Ensenada", new GeographicPoint(31.866667f, -116.616667f));
      GeographicPoint losAngeles = new GeographicPoint(34.0522222f, -118.2427778f);
      timeZoneGeographicPoints.put("America/Los_Angeles", losAngeles);
      timeZoneGeographicPoints.put("America/Santa_Isabel", new GeographicPoint(28.383333f, -113.35f));
      timeZoneGeographicPoints.put("America/Tijuana", new GeographicPoint(32.533333f, -117.016667f));
      timeZoneGeographicPoints.put("America/Vancouver", new GeographicPoint(49.25f, -123.133333f));
      timeZoneGeographicPoints.put("America/Whitehorse", new GeographicPoint(60.716667f, -135.05f));
      timeZoneGeographicPoints.put("Canada/Pacific", new GeographicPoint(49.25f, -123.133333f)); // Vancouver
      timeZoneGeographicPoints.put("Canada/Yukon", new GeographicPoint(60.716667f, -135.05f)); // Whitehorse
      timeZoneGeographicPoints.put("Etc/GMT+8", losAngeles); // Los Angeles
      timeZoneGeographicPoints.put("Mexico/BajaNorte", new GeographicPoint(32.533333f, -117.016667f)); // Tijuana 
      timeZoneGeographicPoints.put("Pacific/Pitcairn", new GeographicPoint(-25.0667f, -130.1f)); // Adamstown
      timeZoneGeographicPoints.put("SystemV/PST8", losAngeles); // Los Angeles
      timeZoneGeographicPoints.put("SystemV/PST8PDT", losAngeles); // Los Angeles
      timeZoneGeographicPoints.put("US/Pacific", losAngeles); // Los Angeles
      timeZoneGeographicPoints.put("US/Pacific-New", losAngeles); // Los Angeles
      timeZoneGeographicPoints.put("America/Boise", new GeographicPoint(43.6136111f, -116.2025000f));
      timeZoneGeographicPoints.put("America/Cambridge_Bay", new GeographicPoint(69.116667f, -105.033333f));
      timeZoneGeographicPoints.put("America/Chihuahua", new GeographicPoint(28.633333f, -106.083333f));
      timeZoneGeographicPoints.put("America/Dawson_Creek", new GeographicPoint(55.766667f, -120.233333f));
      GeographicPoint denver = new GeographicPoint(39.7391667f, -104.9841667f);
      timeZoneGeographicPoints.put("America/Denver", denver);
      timeZoneGeographicPoints.put("America/Edmonton", new GeographicPoint(53.55f, -113.5f));
      timeZoneGeographicPoints.put("America/Hermosillo", new GeographicPoint(29.066667f, -110.966667f));
      timeZoneGeographicPoints.put("America/Inuvik", new GeographicPoint(68.35f, -133.7f));
      timeZoneGeographicPoints.put("America/Mazatlan", new GeographicPoint(23.216667f, -106.416667f));
      timeZoneGeographicPoints.put("America/Ojinaga", new GeographicPoint(29.566667f, -104.416667f));
      timeZoneGeographicPoints.put("America/Phoenix", new GeographicPoint(33.4483333f, -112.0733333f));
      timeZoneGeographicPoints.put("America/Shiprock", new GeographicPoint(36.7855556f, -108.6863889f));
      timeZoneGeographicPoints.put("America/Yellowknife", new GeographicPoint(62.45f, -114.35f));
      timeZoneGeographicPoints.put("Canada/Mountain", new GeographicPoint(53.55f, -113.5f)); // Edmonton
      timeZoneGeographicPoints.put("Etc/GMT+7", denver); // Denver 
      timeZoneGeographicPoints.put("Mexico/BajaSur", new GeographicPoint(32.567f, -116.633f)); // Tecate
      timeZoneGeographicPoints.put("SystemV/MST7", denver); // Denver
      timeZoneGeographicPoints.put("SystemV/MST7MDT", denver); // Denver
      timeZoneGeographicPoints.put("US/Arizona", new GeographicPoint(33.4483333f, -112.0733333f)); // Phoenix
      timeZoneGeographicPoints.put("US/Mountain", denver); // Denver
      timeZoneGeographicPoints.put("America/Belize", new GeographicPoint(17.4833333f, -88.1833333f));
      timeZoneGeographicPoints.put("America/Cancun", new GeographicPoint(21.166667f, -86.833333f));
      GeographicPoint chicago = new GeographicPoint(41.8500000f, -87.6500000f);
      timeZoneGeographicPoints.put("America/Chicago", chicago);
      timeZoneGeographicPoints.put("America/Costa_Rica", new GeographicPoint(9.9333333f, -84.0833333f)); // San Jose
      timeZoneGeographicPoints.put("America/El_Salvador", new GeographicPoint(13.7086111f, -89.2030556f)); // San Salvador
      timeZoneGeographicPoints.put("America/Guatemala", new GeographicPoint(14.6211111f, -90.5269444f));
      timeZoneGeographicPoints.put("America/Knox_IN", new GeographicPoint(41.2958333f, -86.6250000f)); // Knox
      timeZoneGeographicPoints.put("America/Managua", new GeographicPoint(12.1508333f, -86.2683333f));
      timeZoneGeographicPoints.put("America/Matamoros", new GeographicPoint(25.883333f, -97.5f));
      timeZoneGeographicPoints.put("America/Menominee", new GeographicPoint(45.1077778f, -87.6141667f));
      timeZoneGeographicPoints.put("America/Merida", new GeographicPoint(20.966667f, -89.616667f));
      timeZoneGeographicPoints.put("America/Mexico_City", new GeographicPoint(19.434167f, -99.138611f));
      timeZoneGeographicPoints.put("America/Monterrey", new GeographicPoint(25.666667f, -100.316667f));
      timeZoneGeographicPoints.put("America/Rainy_River", new GeographicPoint(48.716667f, -94.566667f));
      timeZoneGeographicPoints.put("America/Rankin_Inlet", new GeographicPoint(62.816667f, -92.083333f));
      timeZoneGeographicPoints.put("America/Regina", new GeographicPoint(50.45f, -104.616667f));
      timeZoneGeographicPoints.put("America/Swift_Current", new GeographicPoint(50.283333f, -107.766667f));
      timeZoneGeographicPoints.put("America/Tegucigalpa", new GeographicPoint(14.1f, -87.2166667f));
      timeZoneGeographicPoints.put("America/Winnipeg", new GeographicPoint(49.883333f, -97.166667f));
      timeZoneGeographicPoints.put("Canada/Central", new GeographicPoint(50.45f, -104.616667f)); // Regina
      timeZoneGeographicPoints.put("Canada/East-Saskatchewan", new GeographicPoint(51.216667f, -102.466667f)); // Yorkton
      timeZoneGeographicPoints.put("Canada/Saskatchewan", new GeographicPoint(50.45f, -104.616667f)); // Regina
      timeZoneGeographicPoints.put("Chile/EasterIsland", new GeographicPoint(-27.15f, -109.425f));
      timeZoneGeographicPoints.put("Etc/GMT+6", chicago); // Chicago
      timeZoneGeographicPoints.put("Mexico/General", new GeographicPoint(19.434167f, -99.138611f)); // Mexico City
      timeZoneGeographicPoints.put("Pacific/Easter", new GeographicPoint(-27.15f, -109.425f)); // Easter Island
      timeZoneGeographicPoints.put("Pacific/Galapagos", new GeographicPoint(-0.667f, -90.55f));
      timeZoneGeographicPoints.put("SystemV/CST6", chicago); // Chicago
      timeZoneGeographicPoints.put("SystemV/CST6CDT", chicago); // Chicago
      timeZoneGeographicPoints.put("US/Central", chicago); // Chicago
      timeZoneGeographicPoints.put("US/Indiana-Starke", new GeographicPoint(41.2958333f, -86.6250000f)); // Knox
      timeZoneGeographicPoints.put("America/Atikokan", new GeographicPoint(48.75f, -91.616667f));
      timeZoneGeographicPoints.put("America/Bogota", new GeographicPoint(4.6f, -74.0833333f));
      timeZoneGeographicPoints.put("America/Cayman", new GeographicPoint(19.3f, -81.3833333f)); // George Town
      timeZoneGeographicPoints.put("America/Coral_Harbour", new GeographicPoint(64.133333f, -83.166667f));
      timeZoneGeographicPoints.put("America/Detroit", new GeographicPoint(42.3313889f, -83.0458333f));
      timeZoneGeographicPoints.put("America/Fort_Wayne", new GeographicPoint(41.1305556f, -85.1288889f));
      timeZoneGeographicPoints.put("America/Grand_Turk", new GeographicPoint(21.4666667f, -71.1333333f));
      timeZoneGeographicPoints.put("America/Guayaquil", new GeographicPoint(-2.1666667f, -79.9f));
      timeZoneGeographicPoints.put("America/Havana", new GeographicPoint(23.1319444f, -82.3641667f));
      timeZoneGeographicPoints.put("America/Indianapolis", new GeographicPoint(39.7683333f, -86.1580556f));
      timeZoneGeographicPoints.put("America/Iqaluit", new GeographicPoint(63.733333f, -68.5f));
      timeZoneGeographicPoints.put("America/Jamaica", new GeographicPoint(18.0f, -76.8f)); // Kingston
      timeZoneGeographicPoints.put("America/Lima", new GeographicPoint(-12.05f, -77.05f));
      timeZoneGeographicPoints.put("America/Louisville", new GeographicPoint(38.2541667f, -85.7594444f));
      timeZoneGeographicPoints.put("America/Montreal", new GeographicPoint(45.5f, -73.583333f));
      timeZoneGeographicPoints.put("America/Nassau", new GeographicPoint(25.0833333f, -77.35f));
      GeographicPoint newYork = new GeographicPoint(40.7141667f, -74.0063889f);
      timeZoneGeographicPoints.put("America/New_York", newYork);
      timeZoneGeographicPoints.put("America/Nipigon", new GeographicPoint(49.016667f, -88.25f));
      timeZoneGeographicPoints.put("America/Panama", new GeographicPoint(8.9666667f, -79.5333333f));
      timeZoneGeographicPoints.put("America/Pangnirtung", new GeographicPoint(66.133333f, -65.75f));
      timeZoneGeographicPoints.put("America/Port-au-Prince", new GeographicPoint(18.5391667f, -72.335f));
      timeZoneGeographicPoints.put("America/Resolute", new GeographicPoint(74.683333f, -94.9f));
      timeZoneGeographicPoints.put("America/Thunder_Bay", new GeographicPoint(48.4f, -89.233333f));
      timeZoneGeographicPoints.put("America/Toronto", new GeographicPoint(43.666667f, -79.416667f));
      timeZoneGeographicPoints.put("Canada/Eastern", new GeographicPoint(43.666667f, -79.416667f)); // Toronto
      timeZoneGeographicPoints.put("Etc/GMT+5", newYork); // New York
      timeZoneGeographicPoints.put("SystemV/EST5", newYork); // New York
      timeZoneGeographicPoints.put("SystemV/EST5EDT", newYork); // New York
      timeZoneGeographicPoints.put("US/East-Indiana", new GeographicPoint(36.8381f, -84.85f)); // Monticello
      timeZoneGeographicPoints.put("US/Eastern", newYork); // New York
      timeZoneGeographicPoints.put("US/Michigan", new GeographicPoint(42.3313889f, -83.0458333f)); // Detroit
      timeZoneGeographicPoints.put("America/Caracas", new GeographicPoint(10.5f, -66.9166667f));
      timeZoneGeographicPoints.put("America/Anguilla", new GeographicPoint(18.2166667f, -63.05f)); // The Valley
      timeZoneGeographicPoints.put("America/Antigua", new GeographicPoint(17.1166667f, -61.85f)); // Saint John's
      timeZoneGeographicPoints.put("America/Aruba", new GeographicPoint(10.5411111f, -72.9175f));
      timeZoneGeographicPoints.put("America/Asuncion", new GeographicPoint(-25.2666667f, -57.6666667f));
      timeZoneGeographicPoints.put("America/Barbados", new GeographicPoint(13.1f, -59.6166667f)); // Bridgetown
      timeZoneGeographicPoints.put("America/Blanc-Sablon", new GeographicPoint(51.433333f, -57.116667f));
      timeZoneGeographicPoints.put("America/Boa_Vista", new GeographicPoint(2.8166667f, -60.6666667f));
      timeZoneGeographicPoints.put("America/Campo_Grande", new GeographicPoint(-20.45f, -54.6166667f));
      timeZoneGeographicPoints.put("America/Cuiaba", new GeographicPoint(-15.5833333f, -56.0833333f));
      timeZoneGeographicPoints.put("America/Curacao", new GeographicPoint(12.1167f, -68.933f)); // Willemstad
      timeZoneGeographicPoints.put("America/Dominica", new GeographicPoint(15.3f, -61.4f)); // Roseau
      timeZoneGeographicPoints.put("America/Eirunepe", new GeographicPoint(-6.6666667f, -69.8666667f));
      timeZoneGeographicPoints.put("America/Glace_Bay", new GeographicPoint(46.2f, -59.966667f));
      timeZoneGeographicPoints.put("America/Goose_Bay", new GeographicPoint(53.333333f, -60.416667f));
      timeZoneGeographicPoints.put("America/Grenada", new GeographicPoint(12.05f, -61.75f)); // Saint George
      timeZoneGeographicPoints.put("America/Guadeloupe", new GeographicPoint(16.2333333f, -61.5166667f)); // Pointe-a-Pitre
      timeZoneGeographicPoints.put("America/Guyana", new GeographicPoint(6.8f, -58.1666667f)); // Georgetown
      timeZoneGeographicPoints.put("America/Halifax", new GeographicPoint(44.65f, -63.6f));
      timeZoneGeographicPoints.put("America/La_Paz", new GeographicPoint(-16.5f, -68.15f));
      timeZoneGeographicPoints.put("America/Manaus", new GeographicPoint(-3.1133333f, -60.0252778f));
      timeZoneGeographicPoints.put("America/Marigot", new GeographicPoint(18.073f, 63.0844f)); // St Martin Island
      timeZoneGeographicPoints.put("America/Martinique", new GeographicPoint(14.6f, -61.0833333f)); // Fort-de-France
      timeZoneGeographicPoints.put("America/Moncton", new GeographicPoint(46.083333f, -64.766667f));
      timeZoneGeographicPoints.put("America/Montserrat", new GeographicPoint(16.7f, -62.2166667f)); // Plymouth
      timeZoneGeographicPoints.put("America/Port_of_Spain", new GeographicPoint(10.65f, -61.5166667f));
      timeZoneGeographicPoints.put("America/Porto_Acre", new GeographicPoint(-9.5877778f, -67.5355556f));
      timeZoneGeographicPoints.put("America/Porto_Velho", new GeographicPoint(-8.7666667f, -63.9f));
      timeZoneGeographicPoints.put("America/Puerto_Rico", new GeographicPoint(18.467f, 66.117f)); // San Juan
      timeZoneGeographicPoints.put("America/Rio_Branco", new GeographicPoint(-9.9666667f, -67.8f));
      GeographicPoint santiago = new GeographicPoint(-33.45f, -70.6666667f);
      timeZoneGeographicPoints.put("America/Santiago", santiago);
      timeZoneGeographicPoints.put("America/Santo_Domingo", new GeographicPoint(18.4666667f, -69.9f));
      timeZoneGeographicPoints.put("America/St_Barthelemy", new GeographicPoint(17.8978f, -62.851f)); // Gustavia
      timeZoneGeographicPoints.put("America/St_Kitts", new GeographicPoint(17.3f, -62.733f)); // Basseterre
      timeZoneGeographicPoints.put("America/St_Lucia", new GeographicPoint(14.0167f, -60.9833f)); // Castries
      timeZoneGeographicPoints.put("America/St_Thomas", new GeographicPoint(18.3333f, -64.9167f));
      timeZoneGeographicPoints.put("America/St_Vincent", new GeographicPoint(13.1667f, -61.2333f)); // Kingstown
      timeZoneGeographicPoints.put("America/Thule", new GeographicPoint(-54.2766667f, -36.5116667f)); // Grytviken
      timeZoneGeographicPoints.put("America/Tortola", new GeographicPoint(18.4166667f, -64.6166667f)); // Road Town
      timeZoneGeographicPoints.put("America/Virgin", new GeographicPoint(18.3438889f, -64.9311111f)); // Charlotte Amalie
      timeZoneGeographicPoints.put("Antarctica/Palmer", new GeographicPoint(-64.25f, -62.833f));
      timeZoneGeographicPoints.put("Atlantic/Bermuda", new GeographicPoint(32.2941667f, -64.7838889f)); // Hamilton
      timeZoneGeographicPoints.put("Atlantic/Stanley", new GeographicPoint(-51.7f, -57.85f));
      timeZoneGeographicPoints.put("Brazil/Acre", new GeographicPoint(-10.8833333f, -45.0833333f));
      timeZoneGeographicPoints.put("Brazil/West", new GeographicPoint(-10.8833333f, -45.0833333f)); // Acre
      timeZoneGeographicPoints.put("Canada/Atlantic", new GeographicPoint(44.65f, -63.6f)); // Halifax
      timeZoneGeographicPoints.put("Chile/Continental", santiago); // Santiago
      timeZoneGeographicPoints.put("Etc/GMT+4", santiago); // Santiago
      timeZoneGeographicPoints.put("SystemV/AST4", new GeographicPoint(44.65f, -63.6f)); // Halifax
      timeZoneGeographicPoints.put("SystemV/AST4ADT", new GeographicPoint(44.65f, -63.6f)); // Halifax
      timeZoneGeographicPoints.put("America/St_Johns", new GeographicPoint(47.5675f, -52.7072f));
      timeZoneGeographicPoints.put("Canada/Newfoundland", new GeographicPoint(47.5675f, -52.7072f)); // St John's
      timeZoneGeographicPoints.put("America/Araguaina", new GeographicPoint(-7.16f, -48.0575f));
      timeZoneGeographicPoints.put("America/Bahia", new GeographicPoint(-12.9833333f, -38.5166667f)); // Salvador
      timeZoneGeographicPoints.put("America/Belem", new GeographicPoint(-1.45f, -48.4833333f));
      timeZoneGeographicPoints.put("America/Buenos_Aires", new GeographicPoint(-34.5875f, -58.6725f));
      timeZoneGeographicPoints.put("America/Catamarca", new GeographicPoint(-28.4666667f, -65.7833333f));
      timeZoneGeographicPoints.put("America/Cayenne", new GeographicPoint(4.9333333f, -52.3333333f));
      timeZoneGeographicPoints.put("America/Cordoba", new GeographicPoint(-31.4f, -64.1833333f));
      timeZoneGeographicPoints.put("America/Fortaleza", new GeographicPoint(-3.7166667f, -38.5f));
      timeZoneGeographicPoints.put("America/Godthab", new GeographicPoint(64.1833333f, -51.75f));
      timeZoneGeographicPoints.put("America/Jujuy", new GeographicPoint(-24.1833333f, -65.3f));
      timeZoneGeographicPoints.put("America/Maceio", new GeographicPoint(-9.6666667f, -35.7166667f));
      timeZoneGeographicPoints.put("America/Mendoza", new GeographicPoint(-32.8833333f, -68.8166667f));
      timeZoneGeographicPoints.put("America/Miquelon", new GeographicPoint(47.0975f, -56.3813889f));
      timeZoneGeographicPoints.put("America/Montevideo", new GeographicPoint(-34.8580556f, -56.1708333f));
      timeZoneGeographicPoints.put("America/Paramaribo", new GeographicPoint(5.8333333f, -55.1666667f));
      timeZoneGeographicPoints.put("America/Recife", new GeographicPoint(-8.05f, -34.9f));
      timeZoneGeographicPoints.put("America/Rosario", new GeographicPoint(-32.9511111f, -60.6663889f));
      timeZoneGeographicPoints.put("America/Santarem", new GeographicPoint(-2.4333333f, -54.7f));
      GeographicPoint saoPaulo = new GeographicPoint(-23.5333333f, -46.6166667f);
      timeZoneGeographicPoints.put("America/Sao_Paulo", saoPaulo);
      timeZoneGeographicPoints.put("Antarctica/Rothera", new GeographicPoint(67.567f, 68.133f));
      timeZoneGeographicPoints.put("Brazil/East", saoPaulo); // Sao Paulo
      timeZoneGeographicPoints.put("Etc/GMT+3", saoPaulo); // Sao Paulo
      timeZoneGeographicPoints.put("America/Noronha", new GeographicPoint(3.85f, 25.417f));
      GeographicPoint southGeorgia = new GeographicPoint(54.25f, 36.75f);
      timeZoneGeographicPoints.put("Atlantic/South_Georgia", southGeorgia);
      timeZoneGeographicPoints.put("Brazil/DeNoronha", new GeographicPoint(3.85f, 25.417f));
      timeZoneGeographicPoints.put("Etc/GMT+2", southGeorgia); // South Georgia
      timeZoneGeographicPoints.put("America/Scoresbysund", new GeographicPoint(70.4833333f, -21.9666667f));
      GeographicPoint azores = new GeographicPoint(37.4833333f, -2.5666667f);
      timeZoneGeographicPoints.put("Atlantic/Azores", azores);
      timeZoneGeographicPoints.put("Atlantic/Cape_Verde", new GeographicPoint(14.9166667f, -23.5166667f)); // Praia
      timeZoneGeographicPoints.put("Etc/GMT+1", azores); // Azores 
      timeZoneGeographicPoints.put("Africa/Abidjan", new GeographicPoint(5.341111f, -4.028056f));
      timeZoneGeographicPoints.put("Africa/Accra", new GeographicPoint(5.55f, -0.2166667f));
      timeZoneGeographicPoints.put("Africa/Bamako", new GeographicPoint(12.65f, -8.0f));
      timeZoneGeographicPoints.put("Africa/Banjul", new GeographicPoint(13.4530556f, -16.5775f));
      timeZoneGeographicPoints.put("Africa/Bissau", new GeographicPoint(11.85f, -15.5833333f));
      timeZoneGeographicPoints.put("Africa/Casablanca", new GeographicPoint(33.5930556f, -7.6163889f));
      timeZoneGeographicPoints.put("Africa/Conakry", new GeographicPoint(9.5091667f, -13.7122222f));
      timeZoneGeographicPoints.put("Africa/Dakar", new GeographicPoint(14.6708333f, -17.4380556f));
      timeZoneGeographicPoints.put("Africa/El_Aaiun", new GeographicPoint(27.1536111f, -13.2033333f));
      timeZoneGeographicPoints.put("Africa/Freetown", new GeographicPoint(8.49f, -13.2341667f));
      timeZoneGeographicPoints.put("Africa/Lome", new GeographicPoint(6.1319444f, 1.2227778f));
      timeZoneGeographicPoints.put("Africa/Monrovia", new GeographicPoint(6.3105556f, -10.8047222f));
      timeZoneGeographicPoints.put("Africa/Nouakchott", new GeographicPoint(18.0863889f, -15.9752778f));
      timeZoneGeographicPoints.put("Africa/Ouagadougou", new GeographicPoint(12.3702778f, -1.5247222f));
      timeZoneGeographicPoints.put("Africa/Sao_Tome", new GeographicPoint(0.3333333f, 6.7333333f));
      timeZoneGeographicPoints.put("Africa/Timbuktu", new GeographicPoint(16.7666667f, -3.0166667f));
      timeZoneGeographicPoints.put("America/Danmarkshavn", new GeographicPoint(76.767f, 18.667f));
      timeZoneGeographicPoints.put("Atlantic/Canary", new GeographicPoint(28.45f, -16.2333333f)); // Santa Cruz de Tenerife
      timeZoneGeographicPoints.put("Atlantic/Faeroe", new GeographicPoint(62.0166667f, -6.7666667f)); // Thorshavn
      timeZoneGeographicPoints.put("Atlantic/Faroe", new GeographicPoint(62.0166667f, -6.7666667f)); // Thorshavn
      timeZoneGeographicPoints.put("Atlantic/Madeira", new GeographicPoint(32.6333333f, -16.9f)); // Funchal
      timeZoneGeographicPoints.put("Atlantic/Reykjavik", new GeographicPoint(64.15f, -21.95f));
      timeZoneGeographicPoints.put("Atlantic/St_Helena", new GeographicPoint(-15.9333333f, -5.7166667f)); // Jamestown
      GeographicPoint greenwich = new GeographicPoint(51.466667f, 0f);
      timeZoneGeographicPoints.put("Etc/GMT", greenwich); // Greenwich
      timeZoneGeographicPoints.put("Etc/GMT+0", greenwich); // Greenwich
      timeZoneGeographicPoints.put("Etc/GMT-0", greenwich); // Greenwich
      timeZoneGeographicPoints.put("Etc/GMT0", greenwich); // Greenwich
      timeZoneGeographicPoints.put("Etc/Greenwich", greenwich);
      timeZoneGeographicPoints.put("Etc/UCT", greenwich); // Greenwich
      timeZoneGeographicPoints.put("Etc/UTC", greenwich); // Greenwich
      timeZoneGeographicPoints.put("Etc/Universal", greenwich); // Greenwich
      timeZoneGeographicPoints.put("Etc/Zulu", greenwich); // Greenwich
      timeZoneGeographicPoints.put("Europe/Belfast", new GeographicPoint(54.583333f, -5.933333f));
      timeZoneGeographicPoints.put("Europe/Dublin", new GeographicPoint(53.3330556f, -6.2488889f));
      timeZoneGeographicPoints.put("Europe/Guernsey", new GeographicPoint(49.45f, -2.533f)); // Saint-Pierre-Port
      timeZoneGeographicPoints.put("Europe/Isle_of_Man", new GeographicPoint(54.14521f, -4.48172f)); // Douglas
      timeZoneGeographicPoints.put("Europe/Jersey", new GeographicPoint(49.2f, -2.117f)); // Saint-Helier
      timeZoneGeographicPoints.put("Europe/Lisbon", new GeographicPoint(38.7166667f, -9.1333333f));
      timeZoneGeographicPoints.put("Europe/London", new GeographicPoint(51.5f, -.116667f));
      timeZoneGeographicPoints.put("Africa/Algiers", new GeographicPoint(36.7630556f, 3.0505556f));
      timeZoneGeographicPoints.put("Africa/Bangui", new GeographicPoint(4.3666667f, 18.5833333f));
      timeZoneGeographicPoints.put("Africa/Brazzaville", new GeographicPoint(-4.2591667f, 15.2847222f));
      timeZoneGeographicPoints.put("Africa/Ceuta", new GeographicPoint(35.8902778f, -5.3075f));
      timeZoneGeographicPoints.put("Africa/Douala", new GeographicPoint(4.0502778f, 9.7f));
      timeZoneGeographicPoints.put("Africa/Kinshasa", new GeographicPoint(-4.3f, 15.3f));
      timeZoneGeographicPoints.put("Africa/Lagos", new GeographicPoint(6.4530556f, 3.3958333f));
      timeZoneGeographicPoints.put("Africa/Libreville", new GeographicPoint(0.3833333f, 9.45f));
      timeZoneGeographicPoints.put("Africa/Luanda", new GeographicPoint(-8.8383333f, 13.2344444f));
      timeZoneGeographicPoints.put("Africa/Malabo", new GeographicPoint(3.75f, 8.7833333f));
      timeZoneGeographicPoints.put("Africa/Ndjamena", new GeographicPoint(12.1130556f, 15.0491667f));
      timeZoneGeographicPoints.put("Africa/Niamey", new GeographicPoint(13.5166667f, 2.1166667f));
      timeZoneGeographicPoints.put("Africa/Porto-Novo", new GeographicPoint(6.4833333f, 2.6166667f));
      timeZoneGeographicPoints.put("Africa/Tunis", new GeographicPoint(36.8027778f, 10.1797222f));
      timeZoneGeographicPoints.put("Africa/Windhoek", new GeographicPoint(-22.57f, 17.0836111f));
      timeZoneGeographicPoints.put("Arctic/Longyearbyen", new GeographicPoint(78.2166667f, 15.6333333f));
      timeZoneGeographicPoints.put("Atlantic/Jan_Mayen", new GeographicPoint(71f, -8.333f));
      GeographicPoint paris = new GeographicPoint(48.866667f, 2.333333f);
      timeZoneGeographicPoints.put("Etc/GMT-1", paris); // Paris 
      timeZoneGeographicPoints.put("Europe/Amsterdam", new GeographicPoint(52.35f, 4.9166667f));
      timeZoneGeographicPoints.put("Europe/Andorra", new GeographicPoint(42.5f, 1.5166667f));
      timeZoneGeographicPoints.put("Europe/Belgrade", new GeographicPoint(44.818611f, 20.468056f));
      timeZoneGeographicPoints.put("Europe/Berlin", new GeographicPoint(52.5166667f, 13.4f));
      timeZoneGeographicPoints.put("Europe/Bratislava", new GeographicPoint(48.15f, 17.1166667f));
      timeZoneGeographicPoints.put("Europe/Brussels", new GeographicPoint(50.833333f, 4.333333f));
      timeZoneGeographicPoints.put("Europe/Budapest", new GeographicPoint(47.5f, 19.0833333f));
      timeZoneGeographicPoints.put("Europe/Copenhagen", new GeographicPoint(55.6666667f, 12.5833333f));
      timeZoneGeographicPoints.put("Europe/Gibraltar", new GeographicPoint(36.1333333f, -5.35f));
      timeZoneGeographicPoints.put("Europe/Ljubljana", new GeographicPoint(46.0552778f, 14.5144444f));
      timeZoneGeographicPoints.put("Europe/Luxembourg", new GeographicPoint(49.6116667f, 6.13f));
      timeZoneGeographicPoints.put("Europe/Madrid", new GeographicPoint(40.4f, -3.6833333f));
      timeZoneGeographicPoints.put("Europe/Malta", new GeographicPoint(35.8997222f, 14.5147222f)); // La Valette
      timeZoneGeographicPoints.put("Europe/Monaco", new GeographicPoint(43.7333333f, 7.4166667f));
      timeZoneGeographicPoints.put("Europe/Oslo", new GeographicPoint(59.916667f, 10.75f));
      timeZoneGeographicPoints.put("Europe/Paris", paris);
      timeZoneGeographicPoints.put("Europe/Podgorica", new GeographicPoint(42.441111f, 19.263611f));
      timeZoneGeographicPoints.put("Europe/Prague", new GeographicPoint(50.0833333f, 14.4666667f));
      timeZoneGeographicPoints.put("Europe/Rome", new GeographicPoint(41.9f, 12.4833333f));
      timeZoneGeographicPoints.put("Europe/San_Marino", new GeographicPoint(43.9333333f, 12.45f));
      timeZoneGeographicPoints.put("Europe/Sarajevo", new GeographicPoint(43.85f, 18.3833333f));
      timeZoneGeographicPoints.put("Europe/Skopje", new GeographicPoint(42.0f, 21.4333333f));
      timeZoneGeographicPoints.put("Europe/Stockholm", new GeographicPoint(59.3333333f, 18.05f));
      timeZoneGeographicPoints.put("Europe/Tirane", new GeographicPoint(41.3275f, 19.8188889f));
      timeZoneGeographicPoints.put("Europe/Vaduz", new GeographicPoint(47.1333333f, 9.5166667f));
      timeZoneGeographicPoints.put("Europe/Vatican", new GeographicPoint(41.9f, 12.45f));
      timeZoneGeographicPoints.put("Europe/Vienna", new GeographicPoint(48.2f, 16.3666667f));
      timeZoneGeographicPoints.put("Europe/Warsaw", new GeographicPoint(52.25f, 21.0f));
      timeZoneGeographicPoints.put("Europe/Zagreb", new GeographicPoint(45.8f, 16.0f));
      timeZoneGeographicPoints.put("Europe/Zurich", new GeographicPoint(47.3666667f, 8.55f));
      timeZoneGeographicPoints.put("Africa/Blantyre", new GeographicPoint(-15.7833333f, 35.0f));
      timeZoneGeographicPoints.put("Africa/Bujumbura", new GeographicPoint(-3.3761111f, 29.36f));
      timeZoneGeographicPoints.put("Africa/Cairo", new GeographicPoint(30.05f, 31.25f));
      timeZoneGeographicPoints.put("Africa/Gaborone", new GeographicPoint(-24.6463889f, 25.9119444f));
      timeZoneGeographicPoints.put("Africa/Harare", new GeographicPoint(-17.8177778f, 31.0447222f));
      timeZoneGeographicPoints.put("Africa/Johannesburg", new GeographicPoint(-26.2f, 28.0833333f));
      timeZoneGeographicPoints.put("Africa/Kigali", new GeographicPoint(-1.9536111f, 30.0605556f));
      timeZoneGeographicPoints.put("Africa/Lubumbashi", new GeographicPoint(-11.666667f, 27.466667f));
      timeZoneGeographicPoints.put("Africa/Lusaka", new GeographicPoint(-15.4166667f, 28.2833333f));
      timeZoneGeographicPoints.put("Africa/Maputo", new GeographicPoint(-25.9652778f, 32.5891667f));
      timeZoneGeographicPoints.put("Africa/Maseru", new GeographicPoint(-29.3166667f, 27.4833333f));
      timeZoneGeographicPoints.put("Africa/Mbabane", new GeographicPoint(-26.3166667f, 31.1333333f));
      timeZoneGeographicPoints.put("Africa/Tripoli", new GeographicPoint(32.8925f, 13.18f));
      timeZoneGeographicPoints.put("Asia/Amman", new GeographicPoint(31.95f, 35.9333333f));
      timeZoneGeographicPoints.put("Asia/Beirut", new GeographicPoint(33.8719444f, 35.5097222f));
      timeZoneGeographicPoints.put("Asia/Damascus", new GeographicPoint(33.5f, 36.3f));
      timeZoneGeographicPoints.put("Asia/Gaza", new GeographicPoint(31.5f, 34.466667f));
      timeZoneGeographicPoints.put("Asia/Istanbul", new GeographicPoint(41.0186111f, 28.9647222f));
      timeZoneGeographicPoints.put("Asia/Jerusalem", new GeographicPoint(31.78f, 35.23f));
      timeZoneGeographicPoints.put("Asia/Nicosia", new GeographicPoint(35.1666667f, 33.3666667f));
      timeZoneGeographicPoints.put("Asia/Tel_Aviv", new GeographicPoint(32.0666667f, 34.7666667f));
      GeographicPoint athens = new GeographicPoint(37.9833333f, 23.7333333f);
      timeZoneGeographicPoints.put("Etc/GMT-2", athens); // Athens
      timeZoneGeographicPoints.put("Europe/Athens", new GeographicPoint(37.9833333f, 23.7333333f));
      timeZoneGeographicPoints.put("Europe/Bucharest", new GeographicPoint(44.4333333f, 26.1f));
      timeZoneGeographicPoints.put("Europe/Chisinau", new GeographicPoint(47.0055556f, 28.8575f));
      timeZoneGeographicPoints.put("Europe/Helsinki", new GeographicPoint(60.1755556f, 24.9341667f));
      timeZoneGeographicPoints.put("Europe/Istanbul", new GeographicPoint(41.0186111f, 28.9647222f));
      timeZoneGeographicPoints.put("Europe/Kaliningrad", new GeographicPoint(54.71f, 20.5f));
      timeZoneGeographicPoints.put("Europe/Kiev", new GeographicPoint(50.4333333f, 30.5166667f));
      timeZoneGeographicPoints.put("Europe/Mariehamn", new GeographicPoint(60.1f, 19.95f));
      timeZoneGeographicPoints.put("Europe/Minsk", new GeographicPoint(53.9f, 27.5666667f));
      timeZoneGeographicPoints.put("Europe/Nicosia", new GeographicPoint(35.1666667f, 33.3666667f));
      timeZoneGeographicPoints.put("Europe/Riga", new GeographicPoint(56.95f, 24.1f));
      timeZoneGeographicPoints.put("Europe/Simferopol", new GeographicPoint(44.95f, 34.1f));
      timeZoneGeographicPoints.put("Europe/Sofia", new GeographicPoint(42.6833333f, 23.3166667f));
      timeZoneGeographicPoints.put("Europe/Tallinn", new GeographicPoint(59.4338889f, 24.7280556f));
      timeZoneGeographicPoints.put("Europe/Tiraspol", new GeographicPoint(46.8402778f, 29.6433333f));
      timeZoneGeographicPoints.put("Europe/Uzhgorod", new GeographicPoint(48.6166667f, 22.3f));
      timeZoneGeographicPoints.put("Europe/Vilnius", new GeographicPoint(54.6833333f, 25.3166667f));
      timeZoneGeographicPoints.put("Europe/Zaporozhye", new GeographicPoint(47.833f, 35.1667f));
      timeZoneGeographicPoints.put("Africa/Addis_Ababa", new GeographicPoint(9.0333333f, 38.7f));
      timeZoneGeographicPoints.put("Africa/Asmara", new GeographicPoint(15.3333333f, 38.9333333f));
      timeZoneGeographicPoints.put("Africa/Asmera", new GeographicPoint(15.3333333f, 38.9333333f));
      timeZoneGeographicPoints.put("Africa/Dar_es_Salaam", new GeographicPoint(-6.8f, 39.2833333f));
      timeZoneGeographicPoints.put("Africa/Djibouti", new GeographicPoint(11.595f, 43.1480556f));
      timeZoneGeographicPoints.put("Africa/Kampala", new GeographicPoint(0.3155556f, 32.5655556f));
      timeZoneGeographicPoints.put("Africa/Khartoum", new GeographicPoint(15.5880556f, 32.5341667f));
      timeZoneGeographicPoints.put("Africa/Mogadishu", new GeographicPoint(2.0666667f, 45.3666667f));
      timeZoneGeographicPoints.put("Africa/Nairobi", new GeographicPoint(-1.2833333f, 36.8166667f));
      timeZoneGeographicPoints.put("Antarctica/Syowa", new GeographicPoint(-69f, 39.5833f));
      timeZoneGeographicPoints.put("Asia/Aden", new GeographicPoint(12.7794444f, 45.0366667f));
      timeZoneGeographicPoints.put("Asia/Baghdad", new GeographicPoint(33.3386111f, 44.3938889f));
      timeZoneGeographicPoints.put("Asia/Bahrain", new GeographicPoint(26.2361111f, 50.5830556f)); // Manama
      timeZoneGeographicPoints.put("Asia/Kuwait", new GeographicPoint(29.3697222f, 47.9783333f));
      timeZoneGeographicPoints.put("Asia/Qatar", new GeographicPoint(25.2866667f, 51.5333333f)); // Doha
      timeZoneGeographicPoints.put("Asia/Riyadh", new GeographicPoint(24.6408333f, 46.7727778f));
      GeographicPoint moscow = new GeographicPoint(55.7522222f, 37.6155556f);
      timeZoneGeographicPoints.put("Etc/GMT-3", moscow); // Moscow
      timeZoneGeographicPoints.put("Europe/Moscow", moscow);
      timeZoneGeographicPoints.put("Europe/Volgograd", new GeographicPoint(48.8047222f, 44.5858333f));
      timeZoneGeographicPoints.put("Indian/Antananarivo", new GeographicPoint(-18.9166667f, 47.5166667f));
      timeZoneGeographicPoints.put("Indian/Comoro", new GeographicPoint(-11.7041667f, 43.2402778f)); // Moroni
      timeZoneGeographicPoints.put("Indian/Mayotte", new GeographicPoint(-12.7794444f, 45.2272222f)); // Mamoudzou
      timeZoneGeographicPoints.put("Asia/Riyadh87", new GeographicPoint(24.6408333f, 46.7727778f));
      timeZoneGeographicPoints.put("Asia/Riyadh88", new GeographicPoint(24.6408333f, 46.7727778f));
      timeZoneGeographicPoints.put("Asia/Riyadh89", new GeographicPoint(24.6408333f, 46.7727778f));
      timeZoneGeographicPoints.put("Mideast/Riyadh87", new GeographicPoint(24.6408333f, 46.7727778f));
      timeZoneGeographicPoints.put("Mideast/Riyadh88", new GeographicPoint(24.6408333f, 46.7727778f));
      timeZoneGeographicPoints.put("Mideast/Riyadh89", new GeographicPoint(24.6408333f, 46.7727778f));
      timeZoneGeographicPoints.put("Asia/Tehran", new GeographicPoint(35.6719444f, 51.4244444f));
      timeZoneGeographicPoints.put("Asia/Baku", new GeographicPoint(40.3952778f, 49.8822222f));
      GeographicPoint dubai = new GeographicPoint(25.2522222f, 55.28f);
      timeZoneGeographicPoints.put("Asia/Dubai", dubai);
      timeZoneGeographicPoints.put("Asia/Muscat", new GeographicPoint(23.6133333f, 58.5933333f));
      timeZoneGeographicPoints.put("Asia/Tbilisi", new GeographicPoint(41.725f, 44.7908333f));
      timeZoneGeographicPoints.put("Asia/Yerevan", new GeographicPoint(40.1811111f, 44.5136111f));
      timeZoneGeographicPoints.put("Etc/GMT-4", dubai); // Dubai
      timeZoneGeographicPoints.put("Europe/Samara", new GeographicPoint(53.2f, 50.15f));
      timeZoneGeographicPoints.put("Indian/Mahe", new GeographicPoint(-4.6166667f, 55.45f));
      timeZoneGeographicPoints.put("Indian/Mauritius", new GeographicPoint(-20.1619444f, 57.4988889f)); // Port Louis
      timeZoneGeographicPoints.put("Indian/Reunion", new GeographicPoint(-20.8666667f, 55.4666667f)); // Saint Denis
      timeZoneGeographicPoints.put("Asia/Kabul", new GeographicPoint(34.5166667f, 69.1833333f));
      timeZoneGeographicPoints.put("Antarctica/Davis", new GeographicPoint(-68.5764f, 77.9689f));
      timeZoneGeographicPoints.put("Antarctica/Mawson", new GeographicPoint(-53.104f, 73.514f));
      timeZoneGeographicPoints.put("Asia/Aqtau", new GeographicPoint(43.65f, 51.2f));
      timeZoneGeographicPoints.put("Asia/Aqtobe", new GeographicPoint(50.2980556f, 57.1813889f));
      timeZoneGeographicPoints.put("Asia/Ashgabat", new GeographicPoint(37.95f, 58.3833333f));
      timeZoneGeographicPoints.put("Asia/Ashkhabad", new GeographicPoint(37.95f, 58.3833333f));
      timeZoneGeographicPoints.put("Asia/Dushanbe", new GeographicPoint(38.56f, 68.7738889f));
      timeZoneGeographicPoints.put("Asia/Karachi", new GeographicPoint(24.8666667f, 67.05f));
      timeZoneGeographicPoints.put("Asia/Oral", new GeographicPoint(51.2333333f, 51.3666667f));
      timeZoneGeographicPoints.put("Asia/Samarkand", new GeographicPoint(39.6541667f, 66.9597222f));
      timeZoneGeographicPoints.put("Asia/Tashkent", new GeographicPoint(41.3166667f, 69.25f));
      timeZoneGeographicPoints.put("Asia/Yekaterinburg", new GeographicPoint(56.85f, 60.6f));
      GeographicPoint calcutta = new GeographicPoint(22.569722f, 88.369722f);
      timeZoneGeographicPoints.put("Etc/GMT-5", calcutta); // Calcutta
      timeZoneGeographicPoints.put("Indian/Kerguelen", new GeographicPoint(-49.25f, 69.583f)); // Port-aux-Francais
      timeZoneGeographicPoints.put("Indian/Maldives", new GeographicPoint(4.1666667f, 73.5f)); // Male
      timeZoneGeographicPoints.put("Asia/Calcutta", calcutta);
      timeZoneGeographicPoints.put("Asia/Colombo", new GeographicPoint(6.9319444f, 79.8477778f));
      timeZoneGeographicPoints.put("Asia/Kolkata", calcutta);
      timeZoneGeographicPoints.put("Asia/Kathmandu", new GeographicPoint(27.7166667f, 85.3166667f));
      timeZoneGeographicPoints.put("Asia/Katmandu", new GeographicPoint(27.7166667f, 85.3166667f));
      timeZoneGeographicPoints.put("Antarctica/Vostok", new GeographicPoint(-78.4644f, 106.8372f));
      timeZoneGeographicPoints.put("Asia/Almaty", new GeographicPoint(43.25f, 76.95f));
      timeZoneGeographicPoints.put("Asia/Bishkek", new GeographicPoint(42.8730556f, 74.6002778f));
      GeographicPoint dacca = new GeographicPoint(23.7230556f, 90.4086111f);
      timeZoneGeographicPoints.put("Asia/Dacca", dacca);
      timeZoneGeographicPoints.put("Asia/Dhaka", dacca);
      timeZoneGeographicPoints.put("Asia/Novokuznetsk", new GeographicPoint(53.75f, 87.1f));
      timeZoneGeographicPoints.put("Asia/Novosibirsk", new GeographicPoint(55.0411111f, 82.9344444f));
      timeZoneGeographicPoints.put("Asia/Omsk", new GeographicPoint(55.0f, 73.4f));
      timeZoneGeographicPoints.put("Asia/Qyzylorda", new GeographicPoint(44.8527778f, 65.5091667f));
      timeZoneGeographicPoints.put("Asia/Thimbu", new GeographicPoint(27.4833333f, 89.6f));
      timeZoneGeographicPoints.put("Asia/Thimphu", new GeographicPoint(27.4833333f, 89.6f));
      timeZoneGeographicPoints.put("Etc/GMT-6", dacca); // Dacca
      timeZoneGeographicPoints.put("Indian/Chagos", new GeographicPoint(-6f, 71.5f));
      timeZoneGeographicPoints.put("Asia/Rangoon", new GeographicPoint(16.7833333f, 96.1666667f));
      timeZoneGeographicPoints.put("Indian/Cocos", new GeographicPoint(-12.1167f, 96.9f));
      GeographicPoint bangkok = new GeographicPoint(13.75f, 100.516667f);
      timeZoneGeographicPoints.put("Asia/Bangkok", bangkok);
      timeZoneGeographicPoints.put("Asia/Ho_Chi_Minh", new GeographicPoint(10.75f, 106.6666667f));
      timeZoneGeographicPoints.put("Asia/Hovd", new GeographicPoint(48.0166667f, 91.6333333f));
      timeZoneGeographicPoints.put("Asia/Jakarta", new GeographicPoint(-6.1744444f, 106.8294444f));
      timeZoneGeographicPoints.put("Asia/Krasnoyarsk", new GeographicPoint(56.0097222f, 92.7916667f));
      timeZoneGeographicPoints.put("Asia/Phnom_Penh", new GeographicPoint(11.55f, 104.9166667f));
      timeZoneGeographicPoints.put("Asia/Pontianak", new GeographicPoint(-0.0333333f, 109.3333333f));
      timeZoneGeographicPoints.put("Asia/Saigon", new GeographicPoint(10.75f, 106.6666667f));
      timeZoneGeographicPoints.put("Asia/Vientiane", new GeographicPoint(17.966667f, 102.6f));
      timeZoneGeographicPoints.put("Etc/GMT-7", bangkok); // Bangkok 
      timeZoneGeographicPoints.put("Indian/Christmas", new GeographicPoint(-10.4166667f, 105.7166667f)); // Flying Fish Cove
      timeZoneGeographicPoints.put("Asia/Brunei", new GeographicPoint(4.8833333f, 114.9333333f));
      timeZoneGeographicPoints.put("Asia/Choibalsan", new GeographicPoint(48.0666667f, 114.5f));
      timeZoneGeographicPoints.put("Asia/Chongqing", new GeographicPoint(29.5627778f, 106.5527778f));
      timeZoneGeographicPoints.put("Asia/Chungking", new GeographicPoint(29.5627778f, 106.5527778f));
      timeZoneGeographicPoints.put("Asia/Harbin", new GeographicPoint(45.75f, 126.65f));
      timeZoneGeographicPoints.put("Asia/Hong_Kong", new GeographicPoint(22.2833333f, 114.15f));
      timeZoneGeographicPoints.put("Asia/Irkutsk", new GeographicPoint(52.2666667f, 104.3333333f));
      timeZoneGeographicPoints.put("Asia/Kashgar", new GeographicPoint(39.3913889f, 76.04f));
      timeZoneGeographicPoints.put("Asia/Kuala_Lumpur", new GeographicPoint(3.1666667f, 101.7f));
      timeZoneGeographicPoints.put("Asia/Kuching", new GeographicPoint(1.55f, 110.3333333f));
      timeZoneGeographicPoints.put("Asia/Macao", new GeographicPoint(22.2f, 113.55f));
      timeZoneGeographicPoints.put("Asia/Macau", new GeographicPoint(22.2f, 113.55f));
      timeZoneGeographicPoints.put("Asia/Makassar", new GeographicPoint(2.45f, 99.7833333f));
      timeZoneGeographicPoints.put("Asia/Manila", new GeographicPoint(14.6041667f, 120.9822222f));
      GeographicPoint shanghai = new GeographicPoint(31.005f, 121.4086111f);
      timeZoneGeographicPoints.put("Asia/Shanghai", shanghai);
      timeZoneGeographicPoints.put("Asia/Singapore", new GeographicPoint(1.2930556f, 103.8558333f));
      timeZoneGeographicPoints.put("Asia/Taipei", new GeographicPoint(25.0391667f, 121.525f));
      timeZoneGeographicPoints.put("Asia/Ujung_Pandang", new GeographicPoint(-5.1305556f, 119.4069444f));
      timeZoneGeographicPoints.put("Asia/Ulaanbaatar", new GeographicPoint(47.9166667f, 106.9166667f));
      timeZoneGeographicPoints.put("Asia/Ulan_Bator", new GeographicPoint(47.9166667f, 106.9166667f));
      timeZoneGeographicPoints.put("Asia/Urumqi", new GeographicPoint(43.8f, 87.5833333f));
      timeZoneGeographicPoints.put("Australia/Perth", new GeographicPoint(-31.933333f, 115.833333f));
      timeZoneGeographicPoints.put("Australia/West", new GeographicPoint(-31.933333f, 115.833333f)); // Perth
      timeZoneGeographicPoints.put("Etc/GMT-8", shanghai); // Shanghai
      timeZoneGeographicPoints.put("Australia/Eucla", new GeographicPoint(-31.716667f, 128.866667f));
      timeZoneGeographicPoints.put("Asia/Dili", new GeographicPoint(-8.55f, 125.5833f));
      timeZoneGeographicPoints.put("Asia/Jayapura", new GeographicPoint(-2.5333333f, 140.7f));
      timeZoneGeographicPoints.put("Asia/Pyongyang", new GeographicPoint(39.0194444f, 125.7547222f));
      timeZoneGeographicPoints.put("Asia/Seoul", new GeographicPoint(37.5663889f, 126.9997222f));
      GeographicPoint tokyo = new GeographicPoint(35.685f, 139.7513889f);
      timeZoneGeographicPoints.put("Asia/Tokyo", tokyo);
      timeZoneGeographicPoints.put("Asia/Yakutsk", new GeographicPoint(62.0338889f, 129.7330556f));
      timeZoneGeographicPoints.put("Etc/GMT-9", tokyo); // Tokyo
      timeZoneGeographicPoints.put("Pacific/Palau", new GeographicPoint(7.5f, 134.6241f)); // Ngerulmud
      timeZoneGeographicPoints.put("Australia/Adelaide", new GeographicPoint(-34.933333f, 138.6f));
      timeZoneGeographicPoints.put("Australia/Broken_Hill", new GeographicPoint(-31.95f, 141.433333f));
      timeZoneGeographicPoints.put("Australia/Darwin", new GeographicPoint(-12.466667f, 130.833333f));
      timeZoneGeographicPoints.put("Australia/North", new GeographicPoint(-12.466667f, 130.833333f)); // Darwin
      timeZoneGeographicPoints.put("Australia/South", new GeographicPoint(-34.933333f, 138.6f)); // Adelaide
      timeZoneGeographicPoints.put("Australia/Yancowinna", new GeographicPoint(-31.7581f, 141.7178f));
      timeZoneGeographicPoints.put("Antarctica/DumontDUrville", new GeographicPoint(-66.66277f, 140.0014f));
      timeZoneGeographicPoints.put("Asia/Sakhalin", new GeographicPoint(51f, 143f));
      timeZoneGeographicPoints.put("Asia/Vladivostok", new GeographicPoint(43.1333333f, 131.9f));
      timeZoneGeographicPoints.put("Australia/ACT", new GeographicPoint(-35.283333f, 149.216667f)); // Canberra
      timeZoneGeographicPoints.put("Australia/Brisbane", new GeographicPoint(-27.5f, 153.016667f));
      timeZoneGeographicPoints.put("Australia/Canberra", new GeographicPoint(-35.283333f, 149.216667f));
      timeZoneGeographicPoints.put("Australia/Currie", new GeographicPoint(-39.933333f, 143.866667f));
      timeZoneGeographicPoints.put("Australia/Hobart", new GeographicPoint(-42.916667f, 147.333333f));
      timeZoneGeographicPoints.put("Australia/Lindeman", new GeographicPoint(-20.45f, 149.0333f));
      timeZoneGeographicPoints.put("Australia/Melbourne", new GeographicPoint(-37.816667f, 144.966667f));
      GeographicPoint sydney = new GeographicPoint(-33.883333f, 151.216667f);
      timeZoneGeographicPoints.put("Australia/NSW", sydney); // Sydney
      timeZoneGeographicPoints.put("Australia/Queensland", new GeographicPoint(-27.5f, 153.016667f)); // Brisbane
      timeZoneGeographicPoints.put("Australia/Sydney", sydney);
      timeZoneGeographicPoints.put("Australia/Tasmania", new GeographicPoint(-42.916667f, 147.333333f)); // Hobart
      timeZoneGeographicPoints.put("Australia/Victoria", new GeographicPoint(-37.816667f, 144.966667f)); // Melbourne
      timeZoneGeographicPoints.put("Etc/GMT-10", sydney); // Sydney
      timeZoneGeographicPoints.put("Pacific/Guam", new GeographicPoint(13.467f, 144.75f)); // Hagatna
      timeZoneGeographicPoints.put("Pacific/Port_Moresby", new GeographicPoint(-9.4647222f, 147.1925f));
      timeZoneGeographicPoints.put("Pacific/Saipan", new GeographicPoint(15.1833f, 145.75f));
      timeZoneGeographicPoints.put("Pacific/Truk", new GeographicPoint(7.4167f, 151.7833f));
      timeZoneGeographicPoints.put("Pacific/Yap", new GeographicPoint(9.5144444f, 138.1291667f));
      timeZoneGeographicPoints.put("Australia/LHI", new GeographicPoint(-31.55f, 159.083f));
      timeZoneGeographicPoints.put("Australia/Lord_Howe", new GeographicPoint(-31.55f, 159.083f));
      timeZoneGeographicPoints.put("Antarctica/Casey", new GeographicPoint(-66.2833f, 110.5333f));
      timeZoneGeographicPoints.put("Asia/Magadan", new GeographicPoint(59.5666667f, 150.8f));
      GeographicPoint noumea = new GeographicPoint(-22.2666667f, 166.45f);
      timeZoneGeographicPoints.put("Etc/GMT-11", noumea); // Noumea
      timeZoneGeographicPoints.put("Pacific/Efate", new GeographicPoint(-17.667f, 168.417f));
      timeZoneGeographicPoints.put("Pacific/Guadalcanal", new GeographicPoint(-9.617f, 160.183f));
      timeZoneGeographicPoints.put("Pacific/Kosrae", new GeographicPoint(5.317f, 162.983f));
      timeZoneGeographicPoints.put("Pacific/Noumea", noumea);
      timeZoneGeographicPoints.put("Pacific/Ponape", new GeographicPoint(6.9638889f, 158.2083333f));
      timeZoneGeographicPoints.put("Pacific/Norfolk", new GeographicPoint(-29.05f, 167.95f)); // Kingston
      timeZoneGeographicPoints.put("Antarctica/McMurdo", new GeographicPoint(-77.85f, 166.667f));
      timeZoneGeographicPoints.put("Antarctica/South_Pole", new GeographicPoint(-90f, 0f));
      timeZoneGeographicPoints.put("Asia/Anadyr", new GeographicPoint(64.75f, 177.4833333f));
      timeZoneGeographicPoints.put("Asia/Kamchatka", new GeographicPoint(57f, 160f));
      GeographicPoint auckland = new GeographicPoint(-36.866667f, 174.766667f);
      timeZoneGeographicPoints.put("Etc/GMT-12", auckland); // Auckland
      timeZoneGeographicPoints.put("Pacific/Auckland", auckland);
      timeZoneGeographicPoints.put("Pacific/Fiji", new GeographicPoint(-18.1333333f, 178.4166667f)); // Suva
      timeZoneGeographicPoints.put("Pacific/Funafuti", new GeographicPoint(-8.5166667f, 179.2166667f));
      timeZoneGeographicPoints.put("Pacific/Kwajalein", new GeographicPoint(9.1939f, 167.4597f));
      timeZoneGeographicPoints.put("Pacific/Majuro", new GeographicPoint(7.1f, 171.3833333f));
      timeZoneGeographicPoints.put("Pacific/Nauru", new GeographicPoint(-0.5322f, 166.9328f));
      timeZoneGeographicPoints.put("Pacific/Tarawa", new GeographicPoint(1.4167f, 173.0333f));
      timeZoneGeographicPoints.put("Pacific/Wake", new GeographicPoint(19.2833f, 166.6f));
      timeZoneGeographicPoints.put("Pacific/Wallis", new GeographicPoint(-13.273f, -176.205f));
      timeZoneGeographicPoints.put("Pacific/Chatham", new GeographicPoint(-43.883f, -176.517f));
      GeographicPoint enderbury = new GeographicPoint(-3.133f, -171.0833f);
      timeZoneGeographicPoints.put("Etc/GMT-13", enderbury); // Enderbury
      timeZoneGeographicPoints.put("Pacific/Enderbury", enderbury);
      timeZoneGeographicPoints.put("Pacific/Tongatapu", new GeographicPoint(-21.2114f, -175.153f));
      GeographicPoint kiritimati = new GeographicPoint(1.883f, -157.4f);
      timeZoneGeographicPoints.put("Etc/GMT-14", kiritimati); // Kiritimati 
      timeZoneGeographicPoints.put("Pacific/Kiritimati", kiritimati);
      
      timeZoneGeographicPoints.put("MIT", apia); // Apia
      timeZoneGeographicPoints.put("HST", honolulu); // Honolulu
      timeZoneGeographicPoints.put("PST", losAngeles); // Los Angeles
      timeZoneGeographicPoints.put("PST8PDT", losAngeles); // Los Angeles
      timeZoneGeographicPoints.put("MST", denver); // Denver
      timeZoneGeographicPoints.put("MST7MDT", denver); // Denver
      timeZoneGeographicPoints.put("Navajo", new GeographicPoint(35.6728f, -109.0622f)); // Window Rock
      timeZoneGeographicPoints.put("PNT", new GeographicPoint(33.4483333f, -112.0733333f)); // Phoenix
      timeZoneGeographicPoints.put("America/Indiana/Knox", new GeographicPoint(41.2958333f, -86.6250000f)); 
      timeZoneGeographicPoints.put("America/Indiana/Tell_City", new GeographicPoint(37.953f, -86.7614f)); 
      timeZoneGeographicPoints.put("America/North_Dakota/Center", new GeographicPoint(47.115f, -101.3003f)); 
      timeZoneGeographicPoints.put("America/North_Dakota/New_Salem", new GeographicPoint(46.843f, 101.4119f)); 
      timeZoneGeographicPoints.put("CST", chicago); // Chicago
      timeZoneGeographicPoints.put("CST6CDT", chicago); // Chicago
      timeZoneGeographicPoints.put("America/Indiana/Indianapolis", new GeographicPoint(39.7683333f, -86.1580556f)); 
      timeZoneGeographicPoints.put("America/Indiana/Marengo", new GeographicPoint(36.3706f, -86.3433f));
      timeZoneGeographicPoints.put("America/Indiana/Petersburg", new GeographicPoint(38.4917f, -87.2803f));
      timeZoneGeographicPoints.put("America/Indiana/Vevay", new GeographicPoint(38.7458f, -85.0711f));
      timeZoneGeographicPoints.put("America/Indiana/Vincennes", new GeographicPoint(38.6783f, -87.5164f));
      timeZoneGeographicPoints.put("America/Indiana/Winamac", new GeographicPoint(41.0525f, -86.6044f));
      timeZoneGeographicPoints.put("America/Kentucky/Louisville", new GeographicPoint(38.2542f, -85.7603f));
      timeZoneGeographicPoints.put("America/Kentucky/Monticello", new GeographicPoint(36.8381f, -84.85f));
      timeZoneGeographicPoints.put("Cuba", new GeographicPoint(23.1319444f, -82.3641667f)); // Havana
      timeZoneGeographicPoints.put("EST", newYork); // New York
      timeZoneGeographicPoints.put("EST5EDT", newYork); // New York
      timeZoneGeographicPoints.put("IET", newYork); // New York
      timeZoneGeographicPoints.put("AST", new GeographicPoint(44.65f, -63.6f)); // Halifax
      timeZoneGeographicPoints.put("Jamaica", new GeographicPoint(18.0f, -76.8f)); // Kingston
      timeZoneGeographicPoints.put("America/Argentina/San_Luis", new GeographicPoint(-33.3f, -66.333f));
      timeZoneGeographicPoints.put("PRT", new GeographicPoint(18.467f, 66.117f)); // San Juan
      timeZoneGeographicPoints.put("CNT", new GeographicPoint(47.5675f, -52.7072f)); // St John's
      timeZoneGeographicPoints.put("AGT", new GeographicPoint(-34.5875f, -58.6725f)); // Buenos Aires
      timeZoneGeographicPoints.put("America/Argentina/Buenos_Aires", new GeographicPoint(-34.5875f, -58.6725f));
      timeZoneGeographicPoints.put("America/Argentina/Catamarca", new GeographicPoint(-28.4666667f, -65.7833333f));
      timeZoneGeographicPoints.put("America/Argentina/ComodRivadavia", new GeographicPoint(-42.7578f, -65.0297f));
      timeZoneGeographicPoints.put("America/Argentina/Cordoba", new GeographicPoint(-31.4f, -64.1833333f));
      timeZoneGeographicPoints.put("America/Argentina/Jujuy", new GeographicPoint(-24.1833333f, -65.3f));
      timeZoneGeographicPoints.put("America/Argentina/La_Rioja", new GeographicPoint(-29.4144f, -66.8552f)); 
      timeZoneGeographicPoints.put("America/Argentina/Mendoza", new GeographicPoint(-32.8833333f, -68.8166667f));
      timeZoneGeographicPoints.put("America/Argentina/Rio_Gallegos", new GeographicPoint(-51.625f, -69.2286f));
      timeZoneGeographicPoints.put("America/Argentina/Salta", new GeographicPoint(-24.7833333f, -65.4166667f));
      timeZoneGeographicPoints.put("America/Argentina/San_Juan", new GeographicPoint(-31.5333f, -68.5167f));
      timeZoneGeographicPoints.put("America/Argentina/Tucuman", new GeographicPoint(-26.8167f, 65.2167f));
      timeZoneGeographicPoints.put("America/Argentina/Ushuaia", new GeographicPoint(-54.6f, -68.3f));
      timeZoneGeographicPoints.put("BET", saoPaulo); // Sao Paulo
      timeZoneGeographicPoints.put("Eire", new GeographicPoint(53.3330556f, -6.2488889f)); // Dublin
      timeZoneGeographicPoints.put("GB", greenwich); // Greenwich
      timeZoneGeographicPoints.put("GB-Eire", new GeographicPoint(53.3330556f, -6.2488889f)); // Dublin
      timeZoneGeographicPoints.put("GMT", greenwich); // Greenwich
      timeZoneGeographicPoints.put("GMT0", greenwich); // Greenwich
      timeZoneGeographicPoints.put("Greenwich", greenwich); // Greenwich
      timeZoneGeographicPoints.put("Iceland", new GeographicPoint(64.1333f, -21.9333f)); // Reykjavík
      timeZoneGeographicPoints.put("Portugal", new GeographicPoint(38.7166667f, -9.1333333f)); // Lisbon
      timeZoneGeographicPoints.put("UCT", greenwich); // Greenwich
      timeZoneGeographicPoints.put("UTC", greenwich); // Greenwich
      timeZoneGeographicPoints.put("Universal", greenwich); // Greenwich
      timeZoneGeographicPoints.put("WET", greenwich); // Greenwich
      timeZoneGeographicPoints.put("Zulu", greenwich); // Greenwich
      timeZoneGeographicPoints.put("CET", paris); // Paris
      timeZoneGeographicPoints.put("ECT", paris); // Paris
      timeZoneGeographicPoints.put("MET", new GeographicPoint(35.6719444f, 51.4244444f)); // Tehran
      timeZoneGeographicPoints.put("Poland", new GeographicPoint(52.25f, 21.0f)); // Warsaw
      timeZoneGeographicPoints.put("ART", new GeographicPoint(-34.5875f, -58.6725f)); // Buenos Aires
      timeZoneGeographicPoints.put("CAT", new GeographicPoint(-1.9536111f, 30.0605556f)); // Kigali
      timeZoneGeographicPoints.put("EET", new GeographicPoint(37.9833333f, 23.7333333f)); // Athens
      timeZoneGeographicPoints.put("Egypt", new GeographicPoint(30.05f, 31.25f)); // Cairo
      timeZoneGeographicPoints.put("Israel", new GeographicPoint(32.0666667f, 34.7666667f)); // Tel Aviv
      timeZoneGeographicPoints.put("Libya", new GeographicPoint(32.8925f, 13.18f)); // Tripoli
      timeZoneGeographicPoints.put("Turkey", new GeographicPoint(41.0186111f, 28.9647222f)); // Istanbul 
      timeZoneGeographicPoints.put("EAT", new GeographicPoint(-1.2833333f, 36.8166667f)); // Nairobi
      timeZoneGeographicPoints.put("W-SU", moscow); // Moscow
      timeZoneGeographicPoints.put("Iran", new GeographicPoint(35.6719444f, 51.4244444f)); // Tehran
      timeZoneGeographicPoints.put("NET", new GeographicPoint(40.1811111f, 44.5136111f)); // Yerevan
      timeZoneGeographicPoints.put("PLT", new GeographicPoint(24.8666667f, 67.05f)); // Karachi
      timeZoneGeographicPoints.put("IST", calcutta); // Calcutta
      timeZoneGeographicPoints.put("BST", dacca); // Dacca
      timeZoneGeographicPoints.put("VST", bangkok); // Bangkok 
      timeZoneGeographicPoints.put("CTT", shanghai); // Shanghai
      timeZoneGeographicPoints.put("Hongkong", new GeographicPoint(22.2833333f, 114.15f));
      timeZoneGeographicPoints.put("PRC", shanghai); // Shanghai
      timeZoneGeographicPoints.put("Singapore", new GeographicPoint(1.2930556f, 103.8558333f));
      timeZoneGeographicPoints.put("JST", tokyo); // Tokyo
      timeZoneGeographicPoints.put("Japan", tokyo); // Tokyo
      timeZoneGeographicPoints.put("ROK", new GeographicPoint(37.5663889f, 126.9997222f)); // Seoul
      timeZoneGeographicPoints.put("ACT", new GeographicPoint(-35.283333f, 149.216667f)); // Canberra
      timeZoneGeographicPoints.put("AET", sydney); // Sydney
      timeZoneGeographicPoints.put("SST", new GeographicPoint(-28.4667f, 159.8167f)); // Honiara
      timeZoneGeographicPoints.put("Kwajalein", new GeographicPoint(9.1939f, 167.4597f));
      timeZoneGeographicPoints.put("NST", auckland); // Auckland
      timeZoneGeographicPoints.put("NZ", auckland); // Auckland
      timeZoneGeographicPoints.put("NZ-CHAT", new GeographicPoint(-43.883f, -176.517f)); // Chatham
      
      // Store geographic points in a weak reference because it shouldn't be used only to init new compass  
      timeZoneGeographicPointsReference = new WeakReference<Map<String,GeographicPoint>>(timeZoneGeographicPoints);
    } else {
      timeZoneGeographicPoints = timeZoneGeographicPointsReference.get();
    }

    GeographicPoint point = timeZoneGeographicPoints.get(TimeZone.getDefault().getID());
    if (point == null) {
      point = timeZoneGeographicPoints.get("Etc/GMT");
    }
    this.latitude = (float)Math.toRadians(point.getLatitudeInDegrees());
    this.longitude = (float)Math.toRadians(point.getLongitudeInDegrees());
  }
  
  /**
   * A geographic point used to store known points.
   */
  private static class GeographicPoint {
    private final float latitudeInDegrees;
    private final float longitudeInDegrees;

    public GeographicPoint(float latitudeInDegrees, float longitudeInDegrees) {
      this.latitudeInDegrees = latitudeInDegrees;
      this.longitudeInDegrees = longitudeInDegrees;
    }
    
    public float getLatitudeInDegrees() {
      return this.latitudeInDegrees;
    }
    
    public float getLongitudeInDegrees() {
      return this.longitudeInDegrees;
    }
  }
}
