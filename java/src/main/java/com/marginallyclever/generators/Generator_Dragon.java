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
	private Turtle turtle;
	private float xmax = 7;
	private float xmin = -7;
	private float ymax = 7;
	private float ymin = -7;
	private static int order = 12; // controls complexity of curve

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

		xmax = (float)(machine.getPaperWidth()/2.0f  * machine.getPaperMargin()) * 10.0f;
		ymax = (float)(machine.getPaperHeight()/2.0f * machine.getPaperMargin()) * 10.0f;
		xmin = -xmax;
		ymin = -ymax;

		turtle = new Turtle();

		boolean drawBoundingBox=false;
		if(drawBoundingBox) {
	      // Draw bounding box
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
		// scale the fractal
        turtle.setX(0);
        turtle.setY(0);
        float stepSize = findStepSize(out);
		// move to starting position
		liftPen(out);
		moveTo(out,turtle.getX(),turtle.getY(),true);
		// draw the fractal
		drawDragon(out, stepSize);
		liftPen(out);
	    moveTo(out, (float)machine.getHomeX(), (float)machine.getHomeY(),true);
	}

	
	private float findStepSize(Writer output) throws IOException {
		float maxX=0;
		float maxY=0;
		float minX=0;
		float minY=0;
		
        for (Integer turn : sequence) {
            turtle.turn(turn * 90);
            turtleMove(output,1,false);
            
            if(maxX<turtle.getX()) maxX = turtle.getX();
            if(minX>turtle.getX()) minX = turtle.getX();
            
            if(maxY<turtle.getY()) maxY = turtle.getY();
            if(minY>turtle.getY()) minY = turtle.getY();
        }

        float dx = maxX - minX;
        float dy = maxY - minY;
		float xx = xmax - xmin;
		float yy = ymax - ymin;
		
		float largestX = xx/dx;
		float largestY = yy/dy;
		float largest = largestX < largestY ? largestX : largestY;

		float x = turtle.getX();
		float y = turtle.getY();
		
        x = -((minX+maxX)/2.0f);
        y = -((minY+maxY)/2.0f);
        
        x*=largest;
        y*=largest;

        turtle.reset();
        turtle.setX(x);
        turtle.setY(y);
        
        return largest;
	}

	
	private void drawDragon(Writer output, float distance) throws IOException {
        for (Integer turn : sequence) {
            turtle.turn(turn * 90);
            turtleMove(output,distance,true);
        }
	}

	
	public void turtleMove(Writer output,float stepSize,boolean write) throws IOException {
		turtle.move(stepSize);
		if(write) moveTo(output, turtle.getX(),turtle.getY(), false);
	}
}
