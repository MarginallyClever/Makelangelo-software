package com.marginallyclever.generators;

import java.awt.GridLayout;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.marginallyclever.makelangelo.MakelangeloRobotSettings;
import com.marginallyclever.makelangelo.Makelangelo;
import com.marginallyclever.makelangelo.Translator;

public class Generator_Dragon extends ImageGenerator {
	private float turtleDx, turtleDy;
	private float xmax = 7;
	private float xmin = -7;
	private float ymax = 7;
	private float ymin = -7;
	private int order = 12; // controls complexity of curve
	private float x, y;

	private List<Integer> sequence;

	
	public Generator_Dragon(Makelangelo gui, MakelangeloRobotSettings mc,
			Translator ms) {
		super(gui, mc, ms);
	}

	@Override
	public String getName() {
		return translator.get("DragonName");
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

			JPanel panel = new JPanel(new GridLayout(0, 1));
			panel.add(new JLabel(translator.get("HilbertCurveOrder")));
			panel.add(field_order);

			int result = JOptionPane.showConfirmDialog(null, panel, getName(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			if (result == JOptionPane.OK_OPTION) {
				order = Integer.parseInt(field_order.getText());

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

			turtleDx = 0;
			turtleDy = -1;

			boolean drawBoundingBox=false;
			if(drawBoundingBox) {
		      // Draw bounding box
		      //SetAbsoluteMode(output);
		      liftPen(output);
		      moveTo(output, xmax, ymax, false);
		      moveTo(output, xmax, ymin, false);
		      moveTo(output, xmin, ymin, false);
		      moveTo(output, xmin, ymax, false);
		      moveTo(output, xmax, ymax, false);
		      liftPen(output);
			}
			
			// create the sequence
	        sequence = new ArrayList<Integer>();
	        for (int i = 0; i < order; i++) {
	            List<Integer> copy = new ArrayList<Integer>(sequence);
	            Collections.reverse(copy);
	            sequence.add(1);
	            for (Integer turn : copy) {
	                sequence.add(-turn);
	            }
	        }
			
			// move to starting position
			x = 0;
			y = 0;
			// scale the fractal
	        float stepSize = findStepSize(output);
			// move to starting position

			liftPen(output);
	        moveTo(output,x,y,true);
			// draw the fractal
			turtleDx = 0;
			turtleDy = -1;
			drawDragon(output, stepSize);
			liftPen(output);

			output.flush();
			output.close();
		} catch (IOException ex) {
		}
	}

	
	private float findStepSize(Writer output) throws IOException {
		float maxX=0;
		float maxY=0;
		float minX=0;
		float minY=0;
		
        for (Integer turn : sequence) {
            turtle_turn(turn * 90);
            turtle_goForward(output,1,false);
            
            if(maxX<x) maxX = x;
            if(minX>x) minX = x;
            
            if(maxY<y) maxY = y;
            if(minY>y) minY = y;
        }

        float dx = maxX - minX;
        float dy = maxY - minY;
		float xx = xmax - xmin;
		float yy = ymax - ymin;
		
		float largestX = xx/dx;
		float largestY = yy/dy;
		float largest = largestX < largestY ? largestX : largestY;

        x = -((minX+maxX)/2.0f);
        y = -((minY+maxY)/2.0f);
        
        x*=largest;
        y*=largest;
        
        return largest;
	}

	// L System tree
	private void drawDragon(Writer output, float distance) throws IOException {
        for (Integer turn : sequence) {
            turtle_turn(turn * 90);
            turtle_goForward(output,distance,true);
        }
	}


	public void turtle_turn(float degrees) {
		double n = Math.toRadians(degrees);
		double newx = Math.cos(n) * turtleDx + Math.sin(n) * turtleDy;
		double newy = -Math.sin(n) * turtleDx + Math.cos(n) * turtleDy;
		double len = Math.sqrt(newx * newx + newy * newy);
		assert (len > 0);
		turtleDx = (float) (newx / len);
		turtleDy = (float) (newy / len);
	}

	
	public void turtle_goForward(Writer output,float stepSize,boolean write) throws IOException {
		//turtle_x += turtle_dx * distance;
		//turtle_y += turtle_dy * distance;
		//output.write(new String("G0 X"+(turtle_x)+" Y"+(turtle_y)+"\n").getBytes());
		x += (turtleDx * (float)stepSize );
		y += (turtleDy * (float)stepSize );
		if(write) moveTo(output, x, y, false);
	}
}
