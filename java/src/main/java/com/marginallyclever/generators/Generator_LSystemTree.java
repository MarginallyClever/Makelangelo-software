package com.marginallyclever.generators;

import java.awt.GridLayout;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.marginallyclever.makelangelo.MakelangeloRobotSettings;
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


	public Generator_LSystemTree(MakelangeloRobotSettings mc) {
		super(mc);
	}

	@Override
	public String getName() {
		return Translator.get("LSystemTreeName");
	}

	/**
	 * Overrides the basic MoveTo() because optimizing for spirals is different logic than straight lines.
	 */
	@Override
	protected void moveTo(Writer out, float x, float y, boolean up) throws IOException {
		tool.writeMoveTo(out, TX(x), TY(y));
		if (lastUp != up) {
			if (up) liftPen(out);
			else lowerPen(out);
			lastUp = up;
		}
	}


	@Override
	public boolean generate(final String dest) {
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

				createCurveNow(dest);
				return true;
			}
		}
		while(tryAgain == true);

		return false;
	}


	private void createCurveNow(String dest) {
		try (
				final OutputStream fileOutputStream = new FileOutputStream(dest);
				final Writer output = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)
				) {
			tool = machine.getCurrentTool();
			output.write(machine.getConfigLine() + ";\n");
			output.write(machine.getBobbinLine() + ";\n");
			tool.writeChangeTo(output);

			w2=0;
			h2=0;
			scale=10.0f;

			float v = Math.min((float)(machine.getPaperWidth() * machine.getPaperMargin())/2.0f,
					(float)(machine.getPaperHeight() * machine.getPaperMargin())/2.0f);
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
			/*
      // Draw bounding box
      //SetAbsoluteMode(output);
      liftPen(output);
      moveTo(output, xmax, ymax, false);
      moveTo(output, xmax, ymin, false);
      moveTo(output, xmin, ymin, false);
      moveTo(output, xmin, ymax, false);
      moveTo(output, xmax, ymax, false);
			 */
		      liftPen(output);
			// move to starting position
			x = 0;//(xmax - turtleStep / 2);
			y = (ymax - turtleStep / 2);
			moveTo(output, x, y, true);
			lowerPen(output);
			// do the curve
			lSystemTree(output, order, maxSize/4);
			liftPen(output);

			output.flush();
			output.close();
		} catch (IOException ex) {
		}
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
