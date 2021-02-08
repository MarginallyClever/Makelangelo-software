package com.marginallyclever.artPipeline.nodes;

import com.marginallyclever.artPipeline.nodeConnector.NodeConnectorTurtle;
import com.marginallyclever.core.node.Node;
import com.marginallyclever.core.node.NodeConnectorDouble;
import com.marginallyclever.core.turtle.Turtle;
import com.marginallyclever.makelangelo.Translator;

/**
 * Draws a rectangle.
 * @author Dan Royer
 * @since 7.25.0
 */
public class Generator_Rectangle extends Node {
	// width of box
	NodeConnectorDouble inputWidth = new NodeConnectorDouble("Generator_Rectangle.inputWidth",100.0);
	// height of box
	NodeConnectorDouble inputHeight = new NodeConnectorDouble("Generator_Rectangle.inputHeight",100.0);
	// results
	NodeConnectorTurtle outputTurtle = new NodeConnectorTurtle("ImageConverter.outputTurtle");
	
	public Generator_Rectangle() {
		super();
		inputs.add(inputWidth);
		inputs.add(inputHeight);
		outputs.add(outputTurtle);
	}
	
	@Override
	public String getName() {
		return Translator.get("BorderName");
	}

	@Override
	public boolean iterate() {		
		Turtle turtle = new Turtle();
		
		double yMin = -inputHeight.getValue()/2;
		double yMax =  inputHeight.getValue()/2;
		double xMin = -inputWidth.getValue()/2;
		double xMax =  inputWidth.getValue()/2;

		turtle.reset();
		turtle.penUp();
		turtle.moveTo(xMin,yMax);
		turtle.penDown();
		turtle.moveTo(xMin,yMax);
		turtle.moveTo(xMax,yMax);
		turtle.moveTo(xMax,yMin);
		turtle.moveTo(xMin,yMin);
		turtle.moveTo(xMin,yMax);
		turtle.penUp();

		outputTurtle.setValue(turtle);
	    return false;
	}
}
