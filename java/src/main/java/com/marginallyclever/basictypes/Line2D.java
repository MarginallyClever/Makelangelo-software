package com.marginallyclever.basictypes;

/**
 * A straight line between two points in 3 dimensional space.
 * @author droyer
 */
public class Line2D {
  protected Point2D start = new Point2D();
  protected Point2D end = new Point2D();

  public Line2D() {
  }

  public void setStart(Point2D p) {
    start.set(p.x, p.y);
  }

  public Point2D getStart() {
    return start;
  }

  public void setEnd(Point2D p) {
    end.set(p.x, p.y);
  }

  public Point2D getEnd() {
    return end;
  }

  public float length() {
    float x = end.x - start.x;
    float y = end.y - start.y;

    return (float) Math.sqrt( x * x + y * y );
  }

  /**
   * Get a point on the line between start and end, where v=0 is start and v=1 is end.  **Note**: Does NOT check if 0 <= v <= 1.
   * @param v a value from 0 to 1, inclusive.
   * @return the Point2D .
   */
  public Point2D interpolate(float v) {
    Point2D n = new Point2D();
    n.set((end.x - start.x) * v + start.x,
        (end.y - start.y) * v + start.y);
    return n;
  }
}
