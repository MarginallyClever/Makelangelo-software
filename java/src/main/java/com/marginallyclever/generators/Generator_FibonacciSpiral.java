package com.marginallyclever.generators;

import java.awt.GridLayout;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Stack;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.marginallyclever.makelangelo.Translator;

public class Generator_FibonacciSpiral extends ImageGenerator {
	private Turtle turtle;
	private float xmax = 7;
	private float xmin = -7;
	private float ymax = 7;
	private float ymin = -7;
	private int order = 7; // controls complexity of curve

	private float maxSize;
	private Stack<Integer> sequence;


	@Override
	public String getName() {
		return Translator.get("FibonacciSpiralName");
	}

	
	@Override
	public boolean generate(Writer out) throws IOException {
		boolean tryAgain=false;
		do {
			JPanel panel = new JPanel(new GridLayout(0, 1));
			panel.add(new JLabel(Translator.get("HilbertCurveOrder")));

			JTextField field_order = new JTextField(Integer.toString(order));
			panel.add(field_order);

			int result = JOptionPane.showConfirmDialog(null, panel, getName(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			if (result == JOptionPane.OK_OPTION) {
				order = Integer.parseInt(field_order.getText());

				// TODO: check order>3

				createCurveNow(out);
				return true;
			}
		}
		while(tryAgain == true);

		return false;
	}


	private void buildFibonacciSequence(int order) {
		sequence = new Stack<Integer>();
		sequence.add(1);
		//System.out.println("add 1");
		sequence.add(1);
		//System.out.println("add 1");
		int a = 1;
		int b = 1;
		int c;
		
		while(order>2) {
			c = a+b;
			sequence.add(c);
			//System.out.println("add "+c);
			a=b;
			b=c;
			order--;
		}
	}
	
	private void createCurveNow(Writer out) throws IOException {
		imageStart(out);
		tool = machine.getCurrentTool();
		liftPen(out);
		tool.writeChangeTo(out);

		xmax = (float)(machine.getPaperWidth () * machine.getPaperMargin()) * 10.0f/2.0f;
		ymax = (float)(machine.getPaperHeight() * machine.getPaperMargin()) * 10.0f/2.0f;
		xmin = -xmax;
		ymin = -ymax;

		// build the Fibonacci sequence.
		buildFibonacciSequence(order);
		
		// scale the fractal to fit on the page
		// short side
		float s1 = sequence.peek();
		float scale1 = Math.min(xmax, ymax) * 2.0f / s1;
		// long side
		float s2 = sequence.get(sequence.size()-2) + s1;
		float scale2 = Math.max(xmax, ymax) * 2.0f / s2;

		System.out.println("size="+sequence.size());
		if(scale1>scale2) scale1=scale2;
		
		turtle = new Turtle();

		boolean drawBoundingBox=false;
		if(drawBoundingBox) {
			liftPen(out);
			moveTo(out, xmax, ymax, false);
			moveTo(out, xmax, ymin, false);
			moveTo(out, xmin, ymin, false);
			moveTo(out, xmin, ymax, false);
			moveTo(out, xmax, ymax, false);
			liftPen(out);
		}
		
		liftPen(out);
		// move to starting position
		float shortSide = sequence.peek() * scale1 /2.0f; 
		if( xmax < ymax ) {
			// tall thin paper, top left corner
			turtle.setX(-shortSide);
			turtle.setY(ymax);
			turtle.turn(-90);
		} else {
			// short wide paper, bottom left corner
			turtle.setX(-xmax);
			turtle.setY(-shortSide);
			turtle.turn(180);
		}
		moveTo(out, turtle.getX(), turtle.getY(), true);
		lowerPen(out);
		// do the curve
		while(!sequence.isEmpty()) {
			int o = sequence.pop();
			float size = o*scale1;
			fibonacciCell(out, size);
		}
		
		liftPen(out);
	    moveTo(out, (float)machine.getHomeX(), (float)machine.getHomeY(),true);
	}


	// L System tree
	private void fibonacciCell(Writer output, float size) throws IOException {
		// make the square around the cell
		turtle.move(size);
		moveTo(output, turtle.getX(), turtle.getY(), false);
		turtle.turn(90);
		turtle.move(size);
		moveTo(output, turtle.getX(), turtle.getY(), false);
		turtle.turn(90);
		float x2 = turtle.getX();
		float y2 = turtle.getY();
		turtle.move(size);
		moveTo(output, turtle.getX(), turtle.getY(), false);
		turtle.turn(90);
		float x0 = turtle.getX();
		float y0 = turtle.getY();
		turtle.move(size);
		moveTo(output, turtle.getX(), turtle.getY(), false);
		turtle.turn(90);

		// make the curve
		float x1 = turtle.getX();
		float y1 = turtle.getY();
		
		float dx, dy, px, py, len;
		int i;
		for(i=0;i<10;++i) {
			px = (x2-x1) * ((float)i/10.0f) + x1;
			py = (y2-y1) * ((float)i/10.0f) + y1;
			dx = px - x0;
			dy = py - y0;
			len = (float)Math.sqrt(dx*dx+dy*dy);
			px = dx*size/len + x0;
			py = dy*size/len + y0;
			moveTo(output, px, py, false);
		}
		turtle.setX(x2);
		turtle.setY(y2);
		moveTo(output, turtle.getX(), turtle.getY(), false);
		turtle.turn(90);
	}


	public void turtleMove(Writer output,float distance) throws IOException {
		//turtle_x += turtle_dx * distance;
		//turtle_y += turtle_dy * distance;
		//output.write(new String("G0 X"+(turtle_x)+" Y"+(turtle_y)+"\n").getBytes());
		turtle.move(distance);
		moveTo(output, turtle.getX(), turtle.getY(), false);
	}
}
