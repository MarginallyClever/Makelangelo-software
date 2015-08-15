package com.marginallyclever.basictypes;

public class Polygon2D {
  public Point2D[] list;

  public void add(Point2D p) {
    if (list == null) {
      list = new Point2D[1];
    } else {
      Point2D[] new_list = new Point2D[list.length + 1];

      System.arraycopy(list, 0, new_list, 0, list.length);

      list = new_list;
    }
    list[list.length - 1] = new Point2D(p);
  }

  public void removeAll() {
    list = null;
  }

  public void getClip(Point2D smallest, Point2D largest) {
    if (list == null) {
      smallest.set(0, 0);
      largest.set(0, 0);
    } else {
      smallest.x = list[0].x;
      smallest.y = list[0].y;
      largest.x = list[0].x;
      largest.y = list[0].y;
      for (int i = 1; i < list.length; ++i) {
        if (smallest.x > list[i].x) smallest.x = list[i].x;
        if (smallest.y > list[i].y) smallest.y = list[i].y;
        if (largest.x < list[i].x) largest.x = list[i].x;
        if (largest.y < list[i].y) largest.y = list[i].y;
      }
    }
  }


  // sort the points in clockwise order
  public void makeClockwise() {

  }


  // assumes all points are ascending clockwise around a convex area
  public boolean insideBorder(float x, float y) {
    if (list == null || list.length < 3) {
      return false;
    }

    int i;
    float nx, ny, ox, oy, px, py;

    for (i = 0; i < list.length; ++i) {
      Point2D a = list[i];
      Point2D b = list[(i + 1) % list.length];

      nx = b.x - a.x;
      ny = b.y - a.y;
      ox = -ny;
      oy = nx;
      px = x - a.x;
      py = y - a.y;

      if (ox * px + oy * py < 0) return false;  // wrong side of the line
    }

    return true;
  }
}
