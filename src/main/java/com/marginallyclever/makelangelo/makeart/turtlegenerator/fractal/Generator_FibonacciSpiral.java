package com.marginallyclever.makelangelo.makeart.turtlegenerator.fractal;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.TurtleGenerator;
import com.marginallyclever.donatello.select.SelectReadOnlyText;
import com.marginallyclever.donatello.select.SelectSlider;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.geom.Rectangle2D;
import java.util.Stack;

/**
 * generates a fibonacci spiral
 * @author dan royer
 *
 */
@Deprecated(since = "7.68.0")
public class Generator_FibonacciSpiral extends TurtleGenerator {
	private static final Logger logger = LoggerFactory.getLogger(Generator_FibonacciSpiral.class);

	private static int order = 7; // controls complexity of curve

	private final Stack<Integer> fibonacciSequence = new Stack<>();

	public Generator_FibonacciSpiral() {
		super();

		SelectSlider fieldOrder = new SelectSlider("order",Translator.get("HilbertCurveOrder"),16,0, Generator_Dragon.getOrder());
		add(fieldOrder);
		fieldOrder.addSelectListener(evt->{
			order = Math.max(1,fieldOrder.getValue());
			generate();
		});
		add(new SelectReadOnlyText("url","<a href='https://en.wikipedia.org/wiki/Fibonacci_number'>"+Translator.get("TurtleGenerators.LearnMore.Link.Text")+"</a>"));
	}
	
	@Override
	public String getName() {
		return Translator.get("FibonacciSpiralName");
	}

	private void buildFibonacciSequence(int order) {
		fibonacciSequence.clear();
		fibonacciSequence.add(1);
		fibonacciSequence.add(1);
		int a = 1;
		int b = 1;
		int c;
		
		while(order>2) {
			c = a+b;
			fibonacciSequence.add(c);
			a=b;
			b=c;
			order--;
		}
	}

	@Override
	public void generate() {
		Rectangle2D.Double rect = myPaper.getMarginRectangle();
		double xMax = rect.getWidth() / 2.0;
		double yMax = rect.getHeight() / 2.0;
		logger.debug("xMax={}", xMax);
		logger.debug("yMax={}", yMax);
		
		// build the Fibonacci sequence.
		buildFibonacciSequence(order);
		
		// scale the fractal to fit on the page
		// short side
		double s1 = fibonacciSequence.peek();
		logger.debug("s1={}", s1);
		double scale1 = Math.min(xMax, yMax) * 2.0f / s1;
		// long side
		double s2 = fibonacciSequence.get(fibonacciSequence.size()-2) + s1;
		logger.debug("s2={}", s2);
		double scale2 = Math.max(xMax, yMax) * 2.0f / s2;

		if(scale1>scale2) scale1=scale2;
		
		Turtle turtle = new Turtle();
		
		// move to starting position
		double shortSide = fibonacciSequence.peek() * scale1 /2.0f;
		logger.debug("shortSide={}", shortSide);
		if( xMax < yMax) {
			logger.debug("tall thin");
			// tall thin paper, top left corner
			turtle.moveTo(shortSide, yMax);
			turtle.turn(180);
		} else {
			logger.debug("short wide");
			// short wide paper, bottom left corner
			turtle.moveTo(-xMax,shortSide);
			turtle.turn(-90);
		}
		
		turtle.penDown();
		
		// do the curve, one square at a time.
		while(!fibonacciSequence.isEmpty()) {
			int o = fibonacciSequence.pop();
			fibonacciCell(turtle,o*scale1);
		}

		turtle.translate(myPaper.getCenterX(),myPaper.getCenterY());

		notifyListeners(turtle);
	}


	// L System tree
	private void fibonacciCell(Turtle turtle,double size) {
		// make the square around the cell
		turtle.forward(size);
		turtle.turn(90);
		turtle.forward(size);
		turtle.turn(90);
		double x2 = turtle.getX();
		double y2 = turtle.getY();
		turtle.forward(size);
		turtle.turn(90);
		double x0 = turtle.getX();
		double y0 = turtle.getY();
		turtle.forward(size);
		turtle.turn(90);

		// make the curve
		double x1 = turtle.getX();
		double y1 = turtle.getY();
		
		double dx, dy, px, py, len;
		final int steps = 20;
		int i;
		for(i=0;i<steps;++i) {
			px = (x2-x1) * ((double)i/steps) + x1;
			py = (y2-y1) * ((double)i/steps) + y1;
			dx = px - x0;
			dy = py - y0;
			len = Math.sqrt(dx*dx+dy*dy);
			px = dx*size/len + x0;
			py = dy*size/len + y0;
			turtle.moveTo(px, py);
		}
		turtle.moveTo(x2, y2);
		turtle.turn(90);
	}
}
