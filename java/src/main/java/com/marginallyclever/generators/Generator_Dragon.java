package com.marginallyclever.generators;

import java.awt.GridLayout;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

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


	@Override
	public String getName() {
		return Translator.get("DragonName");
	}


	@Override
	public boolean generate(Writer out) throws IOException {
		boolean tryAgain=false;
		do {
			final JTextField field_order = new JTextField(Integer.toString(order));

			JPanel panel = new JPanel(new GridLayout(0, 1));
			panel.add(new JLabel(Translator.get("HilbertCurveOrder")));
			panel.add(field_order);

			int result = JOptionPane.showConfirmDialog(null, panel, getName(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			if (result == JOptionPane.OK_OPTION) {
				order = Integer.parseInt(field_order.getText());

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
						   (float)(machine.getPaperHeight() * machine.getPaperMargin())) * 10.0f/2.0f;
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
	      liftPen(out);
	      moveTo(out, xmax, ymax, false);
	      moveTo(out, xmax, ymin, false);
	      moveTo(out, xmin, ymin, false);
	      moveTo(out, xmin, ymax, false);
	      moveTo(out, xmax, ymax, false);
	      liftPen(out);
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
        float stepSize = findStepSize(out);
		// move to starting position

		liftPen(out);
		moveTo(out,x,y,true);
		// draw the fractal
		turtleDx = 0;
		turtleDy = -1;
		drawDragon(out, stepSize);
		liftPen(out);
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
