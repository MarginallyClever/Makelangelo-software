package com.marginallyclever.makelangeloRobot.generators;

import java.awt.GridLayout;
import java.io.IOException;
import java.io.Writer;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.marginallyclever.makelangelo.Translator;

public class Generator_LSystemTree extends ImageGenerator {
	private float turtleStep = 10.0f;
	private float xMax = 7;
	private float xMin = -7;
	private float yMax = 7;
	private float yMin = -7;
	private int order = 4; // controls complexity of curve

	float maxSize;

	private float angleSpan = 120;
	private int numBranches = 3;
	private float orderScale = 0.76f;
	
	private Turtle turtle;


	@Override
	public String getName() {
		return Translator.get("LSystemTreeName");
	}

	@Override
	public String getPreviewImage() {
		return "/images/generators/l-system.JPG";
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
		liftPen(out);
		machine.writeChangeTo(out);

		float v = Math.min((float)(machine.getPaperWidth() * machine.getPaperMargin()),
				(float)(machine.getPaperHeight() * machine.getPaperMargin())) * 10.0f / 2.0f;
		xMax = v;
		yMax = v;
		xMin = -v;
		yMin = -v;

		turtle = new Turtle();
		
		turtleStep = (float) ((xMax - xMin) / (Math.pow(2, order)));

		float xx = xMax - xMin;
		float yy = yMax - yMin;
		maxSize = xx > yy ? xx : yy;

		boolean drawBoundingBox=false;
		if(drawBoundingBox) {
			liftPen(out);
			moveTo(out, xMax, yMax, false);
			moveTo(out, xMax, yMin, false);
			moveTo(out, xMin, yMin, false);
			moveTo(out, xMin, yMax, false);
			moveTo(out, xMax, yMax, false);
			liftPen(out);
		}
		
		// move to starting position
		turtle.setX(0);
		turtle.setY(yMax - turtleStep / 2);
		moveTo(out, turtle.getX(), turtle.getY(), true);
		lowerPen(out);
		// do the curve
		lSystemTree(out, order, maxSize/4);
		liftPen(out);
	    moveTo(out, (float)machine.getHomeX(), (float)machine.getHomeY(),true);
	}


	// recursive L System tree fractal
	private void lSystemTree(Writer output, int n, float distance) throws IOException {
		if (n == 0) return;
		// 
		turtleMove(output,distance);
		if(n>1) {
			float angleStep = angleSpan / (float)(numBranches-1);

			turtle.turn(-(angleSpan/2.0f));
			for(int i=0;i<numBranches;++i) {
				lSystemTree(output,n-1,distance*orderScale);
				turtle.turn(angleStep);
			}
			turtle.turn(-(angleSpan/2.0f)-angleStep);

		}
		turtleMove(output,-distance);
	}


	public void turtleMove(Writer output,float distance) throws IOException {
		turtle.move(distance);
		moveTo(output, turtle.getX(), turtle.getY(), false);
	}
}
