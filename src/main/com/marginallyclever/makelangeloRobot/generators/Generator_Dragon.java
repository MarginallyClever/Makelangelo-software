package com.marginallyclever.makelangeloRobot.generators;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.marginallyclever.makelangelo.Translator;

public class Generator_Dragon extends ImageGenerator {
	private Turtle turtle;
	private float xMax = 7;
	private float xMin = -7;
	private float yMax = 7;
	private float yMin = -7;
	private static int order = 12; // controls complexity of curve

	private List<Integer> sequence;


	@Override
	public String getName() {
		return Translator.get("DragonName");
	}

	static public int getOrder() {
		return order;
	}
	static public void setOrder(int value) {
		if(value<1) value=1;
		order = value;
	}
	
	@Override
	public ImageGeneratorPanel getPanel() {
		return new Generator_Dragon_Panel(this);
	}
		
	@Override
	public boolean generate(Writer out) throws IOException {
		imageStart(out);

		xMax = (float)(machine.getPaperWidth()  * machine.getPaperMargin())/2.0f;
		yMax = (float)(machine.getPaperHeight() * machine.getPaperMargin())/2.0f;
		xMin = -xMax;
		yMin = -yMax;

		turtle = new Turtle();

		// create the sequence of moves
        sequence = new ArrayList<Integer>();
        for (int i = 0; i < order; i++) {
            List<Integer> copy = new ArrayList<Integer>(sequence);
            Collections.reverse(copy);
            sequence.add(1);
            for (Integer turn : copy) {
                sequence.add(-turn);
            }
        }
		
		// scale the step size so the fractal fits on the paper
        float stepSize = findStepSize(out);
        
		// move to starting position
		liftPen(out);
		moveTo(out,turtle.getX(),turtle.getY(),true);
		// draw the fractal
		drawDragon(out, stepSize);
		imageEnd(out);
	    
	    return true;
	}

	/**
	 * Walk through the sequence and find the max/min bounds of the dragon fractal.
	 * Moves the turtle to the new starting position.
	 * @param output where to write the gcode.
	 * @return largest dimension of the fractal
	 * @throws IOException
	 */
	private float findStepSize(Writer output) throws IOException {
		float maxX=0;
		float maxY=0;
		float minX=0;
		float minY=0;

        turtle.setX(0);
        turtle.setY(0);
        
        for (Integer turn : sequence) {
            turtle.turn(turn * 90);
            turtleMove(output,1,false);
            
            if(maxX<turtle.getX()) maxX = turtle.getX();
            if(minX>turtle.getX()) minX = turtle.getX();
            
            if(maxY<turtle.getY()) maxY = turtle.getY();
            if(minY>turtle.getY()) minY = turtle.getY();
        }

        float dragonWidth = maxX - minX;
        float dragonHeight = maxY - minY;
		float paperWidth = xMax - xMin;
		float paperHeight = yMax - yMin;
		
		float largestX = paperWidth/dragonWidth;
		float largestY = paperHeight/dragonHeight;
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
