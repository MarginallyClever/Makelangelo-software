package com.marginallyclever.convenience;


import javax.vecmath.Point2d;

/**
 * Given a line segment as the X axis and an amplitude, this class will generate a sin curve along the line.
 */
public class LineInterpolatorSinCurve extends LineInterpolator {
	double amplitude=1;
	double scale=1;
	
	public LineInterpolatorSinCurve() {
		super();
	}
	
	public LineInterpolatorSinCurve(Point2d a, Point2d b) {
		super(a,b);
	}
	
	@Override
	public void getPoint(double t, Point2d c) {
		// line b-a (bitTan) is the tangent of the overall curve, and bigNorm is orthogonal to bigTan.
		Point2d bigTan = new Point2d();
		Point2d bigNorm = new Point2d();
		
		bigTan.x = end.x- start.x;
		bigTan.y = end.y- start.y;
		bigNorm.y = -bigTan.x/2;
		bigNorm.x = bigTan.y/2;
		
		// now we have overall tangent and normal, we can calculate the position on the sin curve.
		double s = Math.sin(t*Math.PI*2.0*scale) * amplitude;
		
		c.x = start.x + bigTan.x * t + bigNorm.x * s;
		c.y = start.y + bigTan.y * t + bigNorm.y * s;
	}

	public double getAmplitude() {
		return amplitude;
	}

	public void setAmplitude(double amplitude) {
		this.amplitude = amplitude;
	}

	public double getScale() {
		return scale;
	}

	public void setScale(double scale) {
		this.scale = scale;
	}
}
