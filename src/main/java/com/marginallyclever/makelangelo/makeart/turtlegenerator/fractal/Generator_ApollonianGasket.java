package com.marginallyclever.makelangelo.makeart.turtlegenerator.fractal;

import com.marginallyclever.convenience.ComplexNumber;
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

	public static class ComplexCircle {
		public final ComplexNumber center;
		public double radius;
		public double curvature;
		public ComplexCircle(ComplexNumber center, double radius) {
			this.center = center;
			this.radius = Math.abs(radius);
			this.curvature = 1.0 / radius;
		}

		public double distance(ComplexCircle other) {
			double dx = center.real - other.center.real;
			double dy = center.imag - other.center.imag;
			return Math.sqrt(dx*dx + dy*dy);
		}
	}

	private static int minDiameter = 1;
	private static int ratio = 6;
	private final List<ComplexCircle> list = new ArrayList<>();
	private final List<ComplexCircle> queue = new ArrayList<>();

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
		while(!queue.isEmpty()) {
			ComplexCircle a = queue.remove(0);
			ComplexCircle b = queue.remove(0);
			ComplexCircle c = queue.remove(0);
			createCircles(a,b,c);
		}
		drawCircles();
	}

	private void drawCircles() {
		Turtle turtle = new Turtle();

		for (ComplexCircle circle : list) {
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
		ComplexCircle a = new ComplexCircle(new ComplexNumber(0, 0), -maxRadius);
		list.add(a);

		ComplexCircle b = new ComplexCircle(new ComplexNumber(-r2, 0), r1);
		list.add(b);

		ComplexCircle c = new ComplexCircle(new ComplexNumber(r1, 0), r2);
		list.add(c);

		queue.add(a);
		queue.add(b);
		queue.add(c);
	}

	/**
	 * calculate the fourth circle between three touching circles.  The circles do not overlap.
	 * @param a the first circle
	 * @param b the second circle
	 * @param c the third circle
	 */
	private void createCircles(ComplexCircle a, ComplexCircle b, ComplexCircle c) {
		// Decartes' theorem
		double sum = a.curvature + b.curvature + c.curvature;
		double root = 2.0 * Math.sqrt(a.curvature*b.curvature + a.curvature*c.curvature + b.curvature*c.curvature);
		double [] k4 = {sum + root, sum-root};

		ComplexCircle[] found = calculateCenters(a, b, c, k4);
		for( ComplexCircle d : found ) {
			if(validateCircle(a,b,c,d)) {
				list.add(d);
				queue.add(a);
				queue.add(b);
				queue.add(d);

				queue.add(a);
				queue.add(c);
				queue.add(d);

				queue.add(b);
				queue.add(c);
				queue.add(d);
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
	private boolean validateCircle(ComplexCircle a, ComplexCircle b, ComplexCircle c, ComplexCircle d) {
		if(d.radius *2 < minDiameter) return false;
		
		for(ComplexCircle other : list) {
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

	private boolean isTangent(ComplexCircle c1, ComplexCircle c2) {
		double d = c1.distance(c2);
		double r1 = c1.radius;
		double r2 = c2.radius;
		var a = Math.abs(d - (r1+r2)) < EPSILON;
		var b = Math.abs(d - Math.abs(r1-r2)) < EPSILON;
		return a || b;
	}

	// return two centers
	private ComplexCircle[] calculateCenters(ComplexCircle a, ComplexCircle b, ComplexCircle c, double [] k4) {
		double k1 = a.curvature;
		double k2 = b.curvature;
		double k3 = c.curvature;
		ComplexNumber z1 = a.center;
		ComplexNumber z2 = b.center;
		ComplexNumber z3 = c.center;

		ComplexNumber a1 = z1.scale(k1);
		ComplexNumber b2 = z2.scale(k2);
		ComplexNumber c2 = z3.scale(k3);
		var sum = a1.add(b2).add(c2);
		var root = a1.multiply(b2).add(b2.multiply(c2)).add(a1.multiply(c2));
		root = root.sqrt().scale(2.0);

		ComplexCircle d0 = new ComplexCircle(sum.add(root).scale(1.0/k4[0]), 1.0/k4[0]);
		ComplexCircle d1 = new ComplexCircle(sum.sub(root).scale(1.0/k4[0]), 1.0/k4[0]);
		ComplexCircle d2 = new ComplexCircle(sum.add(root).scale(1.0/k4[1]), 1.0/k4[1]);
		ComplexCircle d3 = new ComplexCircle(sum.sub(root).scale(1.0/k4[1]), 1.0/k4[1]);
		return new ComplexCircle[] { d0,d1,d2,d3 };
	}
}
