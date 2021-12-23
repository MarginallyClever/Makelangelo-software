package com.marginallyclever.convenience;


public class LineInterpolatorSinCurve extends LineInterpolator {
	double amplitude=1;
	double scale=1;
	
	public LineInterpolatorSinCurve() {
		super();
	}
	
	public LineInterpolatorSinCurve(Point2D a,Point2D b) {
		super(a,b);
	}
	
	@Override
	public void getPoint(double t, Point2D c) {
		// line b-a (bitTan) is the tangent of the overall curve, and bigNorm is orthogonal to bigTan.
		Point2D bigTan = new Point2D();
		Point2D bigNorm = new Point2D();
		
		bigTan.x = b.x-a.x;
		bigTan.y = b.y-a.y;
		bigNorm.y = -bigTan.x/2;
		bigNorm.x = bigTan.y/2;
		
		// now we have overall tangent and normal, we can calculate the position on the sin curve.
		double s = Math.sin(t*Math.PI*2.0*scale) * amplitude;
		
		c.x = a.x + bigTan.x * t + bigNorm.x * s;
		c.y = a.y + bigTan.y * t + bigNorm.y * s;
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
