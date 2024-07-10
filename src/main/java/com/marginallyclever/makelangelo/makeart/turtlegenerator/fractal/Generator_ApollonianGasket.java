package com.marginallyclever.makelangelo.makeart.turtlegenerator.fractal;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.TurtleGenerator;
import com.marginallyclever.makelangelo.select.SelectReadOnlyText;
import com.marginallyclever.makelangelo.select.SelectSlider;
import com.marginallyclever.makelangelo.turtle.Turtle;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * See <a href="https://en.wikipedia.org/wiki/Apollonian_circles">Wikipedia</a>
 * and <a href="https://www.youtube.com/watch?v=6UlGLB_jiCs">The Coding Train</a>
 * @author Dan Royer
 * @since 2024-07-10
 */
public class Generator_ApollonianGasket extends TurtleGenerator {
	private static final double EPSILON = 1e-1;

	public static class Complex {
		public double real;
		public double imag;

		public Complex(double real, double imag) {
			this.real = real;
			this.imag = imag;
		}

		public Complex add(Complex b) {
			return new Complex(this.real + b.real, this.imag + b.imag);
		}

		public Complex sub(Complex b) {
			return new Complex(this.real - b.real, this.imag - b.imag);
		}

		public Complex multiply(Complex b) {
			return new Complex(this.real * b.real - this.imag * b.imag, this.real * b.imag + this.imag * b.real);
		}

		public Complex divide(Complex b) {
			Complex conjugate = new Complex(b.real, -b.imag);
			Complex numerator = this.multiply(conjugate);
			double denominator = b.multiply(conjugate).real;
			return new Complex(numerator.real / denominator, numerator.imag / denominator);
		}

		public double magnitude() {
			return Math.sqrt(real * real + imag * imag);
		}

		@Override
		public String toString() {
			return "(" + real + ", " + imag + ")";
		}

		public Complex scale(double k1) {
			return new Complex(real * k1, imag * k1);
		}

		public Complex sqrt() {
			double r = Math.sqrt(real * real + imag * imag);
			double x = Math.sqrt((r + real) / 2.0);
			double y = Math.sqrt((r - real) / 2.0);
			if (imag < 0) y = -y;
			return new Complex(x, y);
		}
	}

	public static class Circle {
		public final Complex center;
		public double radius;
		public double curvature;
		public Circle(Complex center, double radius) {
			this.center = center;
			this.radius = Math.abs(radius);
			this.curvature = 1.0 / radius;
		}

		public double distance(Circle other) {
			double dx = center.real - other.center.real;
			double dy = center.imag - other.center.imag;
			return Math.sqrt(dx*dx + dy*dy);
		}
	}

	private static int minDiameter = 1;
	private static int ratio = 1;
	private final List<Circle> list = new ArrayList<>();

	public Generator_ApollonianGasket() {
		super();

		SelectSlider field_minDiameter;
		add(field_minDiameter = new SelectSlider("minDiameter",Translator.get("ApollonianGasket.minDiameter"),10,1, Generator_ApollonianGasket.getMinDiameter()));
		field_minDiameter.addSelectListener(evt-> {
			Generator_ApollonianGasket.setMinDiameter(Math.max(1, field_minDiameter.getValue()));
			generate();
		});

		SelectSlider field_ratio;
		add(field_ratio = new SelectSlider("ratio",Translator.get("ApollonianGasket.ratio"),10,1, Generator_ApollonianGasket.getMinDiameter()));
		field_ratio.addSelectListener(evt-> {
			Generator_ApollonianGasket.setRatio(Math.max(1, field_ratio.getValue()));
			generate();
		});

		add(new SelectReadOnlyText("url","<a href='https://en.wikipedia.org/wiki/Apollonian_circles'>"+Translator.get("TurtleGenerators.LearnMore.Link.Text")+"</a>"));
	}

	@Override
	public String getName() {
		return Translator.get("ApollonianGasket.name");
	}

	static public int getMinDiameter() {
		return minDiameter;
	}
	static public void setMinDiameter(int arg0) {
		if(arg0<1) arg0=1;
		if(arg0>10) arg0=10;
		Generator_ApollonianGasket.minDiameter = arg0;
	}
	static public int getRatio() {
		return ratio;
	}
	static public void setRatio(int ratio) {
		if(ratio<1) ratio=1;
		if(ratio>10) ratio=10;
		Generator_ApollonianGasket.ratio = ratio;
	}

	@Override
	public void generate() {
		list.clear();
		Rectangle2D.Double rect = myPaper.getMarginRectangle();
		double width = rect.getWidth();
		double height = rect.getHeight();
		double maxRadius = Math.min(width, height)/2.0;

		// add initial circles - one of maxDiameter and three that fit inside the first.
		initializeBaseCircles(maxRadius);
		createCircles(list.get(0),list.get(1),list.get(2));
		drawCircles();
	}

	private void drawCircles() {
		Turtle turtle = new Turtle();

		for (Circle circle : list) {
			double r = circle.radius;
			turtle.jumpTo(circle.center.real + r, circle.center.imag);

			double circumference = Math.max(5,Math.min(180,Math.ceil(Math.PI*r*2.0)));
			for(int i=1;i<circumference;++i) {
				double v = 2.0*Math.PI * (double)i/circumference;
				turtle.moveTo(
						circle.center.real + r * Math.cos(v),
						circle.center.imag + r * Math.sin(v));
			}
			turtle.moveTo(circle.center.real + r, circle.center.imag);
		}

		notifyListeners(turtle);
	}

	private void initializeBaseCircles(double maxRadius) {
		double r1 = maxRadius * ratio / 20.0;
		double r2 = maxRadius - r1;
		Circle a = new Circle(new Complex(0, 0), -maxRadius);
		Circle b = new Circle(new Complex(-r2, 0), r1);
		Circle c = new Circle(new Complex(r1, 0), r2);

		list.add(a);
		list.add(b);
		list.add(c);
	}

	/**
	 * calculate the fourth circle between three touching circles.  The circles do not overlap.
	 * @param a the first circle
	 * @param b the second circle
	 * @param c the third circle
	 */
	private void createCircles(Circle a, Circle b, Circle c) {
		// decartes theorem
		double sum = a.curvature + b.curvature + c.curvature;
		double root = 2.0 * Math.sqrt(a.curvature*b.curvature + a.curvature*c.curvature + b.curvature*c.curvature);
		double [] k4 = {sum + root, sum-root};

		Circle [] found = calculateCenters(a, b, c, k4);
		for( Circle d : found ) {
			if(validateCircle(a,b,c,d)) {
				list.add(d);
				createCircles(a, b, d);
				createCircles(a, c, d);
				createCircles(b, c, d);
			}
		}
	}

	/**
	 * @param a the first circle
	 * @param b the second circle
	 * @param c the third circle
	 * @param d the fourth circle
	 * @return true if d is not within any other circle and is tangent to a/b/c.
	 */
	private boolean validateCircle(Circle a,Circle b,Circle c,Circle d) {
		if(d.radius *2 < minDiameter) return false;
		
		for(Circle other : list) {
			var dist = d.distance(other);
			var r2 = Math.abs( d.radius - other.radius);
			if(dist<EPSILON && r2 < EPSILON) return false;
		}
		// Check if all 4 circles are mutually tangential
		if (!isTangent(d, a)) return false;
		if (!isTangent(d, b)) return false;
		if (!isTangent(d, c)) return false;
		return true;
	}

	private boolean isTangent(Circle c1, Circle c2) {
		double d = c1.distance(c2);
		double r1 = c1.radius;
		double r2 = c2.radius;
		var a = Math.abs(d - (r1+r2)) < EPSILON;
		var b = Math.abs(d - Math.abs(r1-r2)) < EPSILON;
		return a || b;
	}

	// return two centers
	private Circle[] calculateCenters(Circle a, Circle b, Circle c, double [] k4) {
		double k1 = a.curvature;
		double k2 = b.curvature;
		double k3 = c.curvature;
		Complex z1 = a.center;
		Complex z2 = b.center;
		Complex z3 = c.center;

		Complex a1 = z1.scale(k1);
		Complex b2 = z2.scale(k2);
		Complex c2 = z3.scale(k3);
		var sum = a1.add(b2).add(c2);
		var root = a1.multiply(b2).add(b2.multiply(c2)).add(a1.multiply(c2));
		root = root.sqrt().scale(2.0);

		Circle d0 = new Circle(sum.add(root).scale(1.0/k4[0]), 1.0/k4[0]);
		Circle d1 = new Circle(sum.sub(root).scale(1.0/k4[0]), 1.0/k4[0]);
		Circle d2 = new Circle(sum.add(root).scale(1.0/k4[1]), 1.0/k4[1]);
		Circle d3 = new Circle(sum.sub(root).scale(1.0/k4[1]), 1.0/k4[1]);
		return new Circle[] { d0,d1,d2,d3 };
	}
}
