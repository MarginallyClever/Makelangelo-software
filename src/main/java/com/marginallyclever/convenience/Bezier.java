package com.marginallyclever.convenience;

import javax.vecmath.Point2d;
import java.util.ArrayList;
import java.util.List;

/**
 * 4 point bezier splines
 * Based on <a href="https://github.com/pelson/antigrain/blob/master/agg-2.4/src/agg_curves.cpp">1</a>
 * and <a href="https://github.com/mattdesl/adaptive-bezier-curve">2</a>
 * @author Dan Royer
 *
 */
public class Bezier {
	private int recursionLimit = 8;
	private double curveAngleToleranceEpsilon =0.01;
	private double angleTolerance = 0;
	private double cuspLimit = 0;
	private static final double FLOAT_EPSILON=1.19209290e-7;
    
	private double x0,x1,x2,x3;
	private double y0,y1,y2,y3;
    
	public Bezier(double x0,double y0,double x1,double y1,double x2,double y2,double x3,double y3) {
		this.x0=x0;
		this.y0=y0;
		this.x1=x1;
		this.y1=y1;
		this.x2=x2;
		this.y2=y2;
		this.x3=x3;
		this.y3=y3;
	}
	
	// Based on https://github.com/pelson/antigrain/blob/master/agg-2.4/src/agg_curves.cpp
	// and https://github.com/mattdesl/adaptive-bezier-curve
	public List<Point2d> generateCurvePoints(double distanceTolerance) {
		ArrayList<Point2d> points = new ArrayList<Point2d>();
		points.add(new Point2d(x0,y0));
		recursive(x0,y0,x1,y1,x2,y2,x3,y3,points,distanceTolerance*distanceTolerance,0);
		points.add(new Point2d(x3,y3));
		return points;
	}
	
	private void recursive(double x1,double y1,double x2,double y2,double x3,double y3,double x4,double y4,ArrayList<Point2d> points, double distanceTolerance,int level) {
        if(level > recursionLimit) 
            return;

        // Calculate all the mid-points of the line segments
        double x12   = (x1 + x2) / 2.0;
        double y12   = (y1 + y2) / 2.0;
        double x23   = (x2 + x3) / 2.0;
        double y23   = (y2 + y3) / 2.0;
        double x34   = (x3 + x4) / 2.0;
        double y34   = (y3 + y4) / 2.0;
        double x123  = (x12 + x23) / 2.0;
        double y123  = (y12 + y23) / 2.0;
        double x234  = (x23 + x34) / 2.0;
        double y234  = (y23 + y34) / 2.0;
        double x1234 = (x123 + x234) / 2.0;
        double y1234 = (y123 + y234) / 2.0;

        if(level > 0) { // Enforce subdivision first time
            // Try to approximate the full cubic curve by a single straight line
            double dx = x4-x1;
            double dy = y4-y1;

            double d2 = Math.abs((x2 - x4) * dy - (y2 - y4) * dx);
            double d3 = Math.abs((x3 - x4) * dy - (y3 - y4) * dx);

            double da1, da2;

            if(d2 > FLOAT_EPSILON && d3 > FLOAT_EPSILON) {
                // Regular care
                if((d2 + d3)*(d2 + d3) <= distanceTolerance * (dx*dx + dy*dy)) {
                    // If the curvature doesn't exceed the distanceTolerance value we tend to finish subdivisions.
                    if(angleTolerance < curveAngleToleranceEpsilon) {
                        points.add(new Point2d(x1234, y1234));
                        return;
                    }

                    // Angle & Cusp Condition
					double a23 = Math.atan2(y3 - y2, x3 - x2);
                    da1 = Math.abs(a23 - Math.atan2(y2 - y1, x2 - x1));
                    da2 = Math.abs(Math.atan2(y4 - y3, x4 - x3) - a23);
                    if(da1 >= Math.PI) da1 = 2.0*Math.PI - da1;
                    if(da2 >= Math.PI) da2 = 2.0*Math.PI - da2;

                    if(da1 + da2 < angleTolerance) {
                        // Finally we can stop the recursion
                        points.add(new Point2d(x1234, y1234));
                        return;
                    }

                    if(cuspLimit != 0.0) {
                        if(da1 > cuspLimit) {
                            points.add(new Point2d(x2, y2));
                            return;
                        }
                        if(da2 > cuspLimit) {
                            points.add(new Point2d(x3, y3));
                            return;
                        }
                    }
                }
            } else {
                if(d2 > FLOAT_EPSILON) {
                    // p1,p3,p4 are co-linear, p2 is considerable
                    if(d2 * d2 <= distanceTolerance * (dx*dx + dy*dy)) {
                        if(angleTolerance < curveAngleToleranceEpsilon) {
                            points.add(new Point2d(x1234, y1234));
                            return;
                        }

                        // Angle Condition
                        da1 = Math.abs(Math.atan2(y3 - y2, x3 - x2) - Math.atan2(y2 - y1, x2 - x1));
                        if(da1 >= Math.PI) da1 = 2.0*Math.PI - da1;

                        if(da1 < angleTolerance) {
                            points.add(new Point2d(x2, y2));
                            points.add(new Point2d(x3, y3));
                            return;
                        }

                        if(cuspLimit != 0.0) {
                            if(da1 > cuspLimit) {
                                points.add(new Point2d(x2, y2));
                                return;
                            }
                        }
                    }
                } else if(d3 > FLOAT_EPSILON) {
                    // p1,p2,p4 are co-linear, p3 is considerable
                    if(d3 * d3 <= distanceTolerance * (dx*dx + dy*dy)) {
                        if(angleTolerance < curveAngleToleranceEpsilon) {
                            points.add(new Point2d(x1234, y1234));
                            return;
                        }

                        // Angle Condition
                        da1 = Math.abs(Math.atan2(y4 - y3, x4 - x3) - Math.atan2(y3 - y2, x3 - x2));
                        if(da1 >= Math.PI) da1 = 2.0*Math.PI - da1;

                        if(da1 < angleTolerance) {
                            points.add(new Point2d(x2, y2));
                            points.add(new Point2d(x3, y3));
                            return;
                        }

                        if(cuspLimit != 0.0) {
                            if(da1 > cuspLimit) {
                                points.add(new Point2d(x3, y3));
                                return;
                            }
                        }
                    }
                } else {
                    // Co-linear case
                    dx = x1234 - (x1 + x4) / 2.0;
                    dy = y1234 - (y1 + y4) / 2.0;
                    if(dx*dx + dy*dy <= distanceTolerance) {
                        points.add(new Point2d(x1234, y1234));
                        return;
                    }
                }
            }
        }

        // Continue subdivision
        recursive(x1, y1, x12, y12, x123, y123, x1234, y1234, points, distanceTolerance, level + 1);
        recursive(x1234, y1234, x234, y234, x34, y34, x4, y4, points, distanceTolerance, level + 1);
	}
	
	protected ArrayList<Point2d> generateCurvePointsOld() {
		ArrayList<Point2d> list = new ArrayList<Point2d>();
		list.add(new Point2d(x0,y0));
		
		double steps=25;
		for(double k=1;k<steps;k++) {
			double j = k/steps;
			/*
			// first method
			double xa = lerp(x0,x1,j);
			double ya = lerp(y0,y1,j);
			double xb = lerp(x1,x2,j);
			double yb = lerp(y1,y2,j);
			double xc = lerp(x2,x3,j);
			double yc = lerp(y2,y3,j);
			
			double xab = lerp(xa,xb,j);
			double yab = lerp(ya,yb,j);
			double xbc = lerp(xb,xc,j);
			double ybc = lerp(yb,yc,j);
			
			double xabc = lerp(xab,xbc,j);
			double yabc = lerp(yab,ybc,j);
			/*/
			// second method
			double xabc = getXAt(j);
			double yabc = getYAt(j);
	        //*/
			
	        list.add(new Point2d(xabc,yabc));
		}
		list.add(new Point2d(x3,y3));
		
		return list;
	}
	
	// for some value t=[0...1]
	double getXAt(double t) {
        double a = Math.pow((1.0 - t), 3.0);
        double b = 3.0 * t * Math.pow((1.0 - t), 2.0);
        double c = 3.0 * Math.pow(t, 2.0) * (1.0 - t);
        double d = Math.pow(t, 3.0);
 
        return a * x0 + b * x1 + c * x2 + d * x3;
	}

	// for some value t=[0...1]
	double getYAt(double t) {
        double a = Math.pow((1.0 - t), 3.0);
        double b = 3.0 * t * Math.pow((1.0 - t), 2.0);
        double c = 3.0 * Math.pow(t, 2.0) * (1.0 - t);
        double d = Math.pow(t, 3.0);
        
        return a * y0 + b * y1 + c * y2 + d * y3;
	}

	protected double lerp(double a,double b,double fraction) {
		return ( b - a ) * fraction + a;
	}
}
