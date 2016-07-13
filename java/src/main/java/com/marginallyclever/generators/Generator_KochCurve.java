package com.marginallyclever.generators;

import java.awt.GridLayout;
import java.io.IOException;
import java.io.Writer;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.marginallyclever.makelangelo.Translator;

public class Generator_KochCurve extends ImageGenerator {
	private Turtle turtle;
	private float xmax = 7;
	private float xmin = -7;
	private float ymax = 7;
	private float ymin = -7;
	private int order = 4; // controls complexity of curve

	private float maxSize;


	@Override
	public String getName() {
		return Translator.get("KochTreeName");
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

				// TODO: check order>0

				createCurveNow(out);
				return true;
			}
		}
		while(tryAgain == true);

		return false;
	}


	private void createCurveNow(Writer out) throws IOException {
		imageStart(out);
		tool = machine.getCurrentTool();
		liftPen(out);
		tool.writeChangeTo(out);

		float v = Math.min((float)(machine.getPaperWidth() * machine.getPaperMargin()),
				(float)(machine.getPaperHeight() * machine.getPaperMargin())) * 10.0f/2.0f;
		xmax = v;
		ymax = v;
		xmin = -v;
		ymin = -v;

		turtle = new Turtle();
		
		float xx = xmax - xmin;
		float yy = ymax - ymin;
		maxSize = xx > yy ? xx : yy;

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
		turtle.setX(xmax);
		turtle.setY(0);
		moveTo(out, turtle.getX(), turtle.getY(), true);
		lowerPen(out);
		// do the curve
		turtle.turn(90);
		kochCurve(out, order, maxSize);
		liftPen(out);
	    moveTo(out, (float)machine.getHomeX(), (float)machine.getHomeY(),true);
	}


	// L System tree
	private void kochCurve(Writer output, int n, float distance) throws IOException {
		if (n == 0) {
			turtleMove(output,distance);
			return;
		}
		kochCurve(output,n-1,distance/3.0f);
		if(n>1) {
			turtle.turn(-60);
			kochCurve(output,n-1,distance/3.0f);
			turtle.turn(120);
			kochCurve(output,n-1,distance/3.0f);
			turtle.turn(-60);
		} else {
			turtleMove(output,distance/3.0f);
		}
		kochCurve(output,n-1,distance/3.0f);
	}


	public void turtleMove(Writer output,float distance) throws IOException {
		//turtle_x += turtle_dx * distance;
		//turtle_y += turtle_dy * distance;
		//output.write(new String("G0 X"+(turtle_x)+" Y"+(turtle_y)+"\n").getBytes());
		turtle.move(distance);
		moveTo(output, turtle.getX(), turtle.getY(), false);
	}
}
