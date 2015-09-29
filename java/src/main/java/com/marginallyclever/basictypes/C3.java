package com.marginallyclever.basictypes;


import java.awt.Color;


/**
 * RGB color class
 * @author danroyer
 *
 */
public class C3 {
  int red = 0;
  int green = 0;
  int blue = 0;

  public C3(int r, int g, int b) {
    red = r;
    green = g;
    blue = b;
  }

  public C3(C3 x) {
    set(x);
  }

  public C3(int pixel) {
    int r = ((pixel >> 16) & 0xff);
    int g = ((pixel >> 8) & 0xff);
    int b = ((pixel) & 0xff);
    set(r, g, b);
  }

  public C3(Color c) {
    red = c.getRed();
    green = c.getGreen();
    blue = c.getBlue();
  }

  public int toInt() {
    return ((red & 0xff) << 16) | ((green & 0xff) << 8) | (blue & 0xff);
  }

  public C3 set(C3 x) {
    red = x.red;
    green = x.green;
    blue = x.blue;
    return this;
  }

  public void set(int r, int g, int b) {
    red = r;
    green = g;
    blue = b;
  }

  public C3 sub(C3 x) {
    red -= x.red;
    green -= x.green;
    blue -= x.blue;
    return this;
  }

  public C3 add(C3 x) {
    red += x.red;
    green += x.green;
    blue += x.blue;
    return this;
  }

  public C3 mul(double f) {
    red *= f;
    green *= f;
    blue *= f;
    return this;
  }

  public float diff(C3 o) {
    int rDiff = o.red - this.red;
    int gDiff = o.green - this.green;
    int bDiff = o.blue - this.blue;
    int distanceSquared = rDiff * rDiff + gDiff * gDiff + bDiff * bDiff;
    return (float) Math.sqrt(distanceSquared);
  }

  public String toString() {
    return "(" + red + "," + green + "," + blue + ")";
  }
}
