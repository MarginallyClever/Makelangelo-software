package com.marginallyclever.generators;

import java.awt.GridLayout;
import java.io.IOException;
import java.io.Writer;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.marginallyclever.makelangelo.Translator;

public class Generator_LSystemTree extends ImageGenerator {
	float turtleX, turtleY;
	float turtleDx, turtleDy;
	float turtleStep = 10.0f;
	float xmax = 7;
	float xmin = -7;
	float ymax = 7;
	float ymin = -7;
	float toolOffsetZ = 1.25f;
	float zDown = 40;
	float zUp = 90;
	int order = 4; // controls complexity of curve
	float x, y;

	float maxSize;

	float angleSpan = 120;
	int numBranches = 3;
	float orderScale = 0.76f;


	@Override
	public String getName() {
		return Translator.get("LSystemTreeName");
	}


	@Override
	public boolean generate(Writer out) throws IOException {
		boolean tryAgain=false;
		do {
			final JTextField field_order = new JTextField(Integer.toString(order));
			final JTextField field_orderScale = new JTextField(Float.toString(orderScale));
			final JTextField field_angle = new JTextField(Float.toString(angleSpan));
			final JTextField field_branches = new JTextField(Integer.toString(numBranches));

			JPanel panel = new JPanel(new GridLayout(0, 1));
			panel.add(new JLabel(Translator.get("HilbertCurveOrder")));
			panel.add(field_order);

			panel.add(new JLabel(Translator.get("HilbertCurveOrderScale")));
			panel.add(field_orderScale);
			
			panel.add(new JLabel(Translator.get("HilbertCurveAngle")));
			panel.add(field_angle);

			panel.add(new JLabel(Translator.get("HilbertCurveBranches")));
			panel.add(field_branches);

			int result = JOptionPane.showConfirmDialog(null, panel, getName(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			if (result == JOptionPane.OK_OPTION) {
				order = Integer.parseInt(field_order.getText());
				orderScale = Float.parseFloat(field_orderScale.getText());
				numBranches = Integer.parseInt(field_branches.getText());
				angleSpan = Float.parseFloat(field_angle.getText());

				// TODO: check angleSpan>0, angleSpan<360, numBranches>0, Order>0

				createCurveNow(out);
				return true;
			}
		}
		while(tryAgain == true);

		return false;
	}


	private void createCurveNow(Writer out) throws IOException {
		imageStart(out);

		float v = Math.min((float)(machine.getPaperWidth() * machine.getPaperMargin()),
				(float)(machine.getPaperHeight() * machine.getPaperMargin())) * 10.0f / 2.0f;
		xmax = v;
		ymax = v;
		xmin = -v;
		ymin = -v;

		turtleStep = (float) ((xmax - xmin) / (Math.pow(2, order)));
		turtleX = 0;
		turtleY = 0;
		turtleDx = 0;
		turtleDy = -1;

		float xx = xmax - xmin;
		float yy = ymax - ymin;
		maxSize = xx > yy ? xx : yy;
		
		// Draw bounding box
		//SetAbsoluteMode(output);
		liftPen(out);
		moveTo(out, xmax, ymax, false);
		moveTo(out, xmax, ymin, false);
		moveTo(out, xmin, ymin, false);
		moveTo(out, xmin, ymax, false);
		moveTo(out, xmax, ymax, false);
		 
	      liftPen(out);
		// move to starting position
		x = 0;//(xmax - turtleStep / 2);
		y = (ymax - turtleStep / 2);
		moveTo(out, x, y, true);
		lowerPen(out);
		// do the curve
		lSystemTree(out, order, maxSize/4);
		liftPen(out);
	}


	// L System tree
	private void lSystemTree(Writer output, int n, float distance) throws IOException {
		if (n == 0) return;
		turtle_goForward(output,distance);
		if(n>1) {
			float angleStep = angleSpan / (float)(numBranches-1);

			turtle_turn(-(angleSpan/2.0f));
			for(int i=0;i<numBranches;++i) {
				lSystemTree(output,n-1,distance*orderScale);
				turtle_turn(angleStep);
			}
			turtle_turn(-(angleSpan/2.0f)-angleStep);

		}
		turtle_goForward(output,-distance);
	}


	public void turtle_turn(float degrees) {
		double n = degrees * Math.PI / 180.0;
		double newx = Math.cos(n) * turtleDx + Math.sin(n) * turtleDy;
		double newy = -Math.sin(n) * turtleDx + Math.cos(n) * turtleDy;
		double len = Math.sqrt(newx * newx + newy * newy);
		assert (len > 0);
		turtleDx = (float) (newx / len);
		turtleDy = (float) (newy / len);
	}


	public void turtle_goForward(Writer output,float stepSize) throws IOException {
		//turtle_x += turtle_dx * distance;
		//turtle_y += turtle_dy * distance;
		//output.write(new String("G0 X"+(turtle_x)+" Y"+(turtle_y)+"\n").getBytes());
		x += (turtleDx * (float)stepSize );
		y += (turtleDy * (float)stepSize );
		moveTo(output, x, y, false);
	}
}
