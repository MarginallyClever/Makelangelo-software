package com.marginallyclever.artPipeline.nodes;

import com.marginallyclever.artPipeline.nodeConnector.NodeConnectorTurtle;
import com.marginallyclever.artPipeline.nodes.panels.Generator_Empty_Panel;
import com.marginallyclever.convenience.nodes.Node;
import com.marginallyclever.convenience.nodes.NodeConnectorDouble;
import com.marginallyclever.convenience.nodes.NodePanel;
import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.makelangelo.Translator;

/**
 * Draws a rectangle.
 * @author Dan Royer
 * @since 7.25.0
 */
public class Generator_Rectangle extends Node {
	// width of box
	NodeConnectorDouble width = new NodeConnectorDouble(100.0);
	// height of box
	NodeConnectorDouble height = new NodeConnectorDouble(100.0);
	
	NodeConnectorTurtle outputTurtle = new NodeConnectorTurtle();
	
	public Generator_Rectangle() {
		super();
		inputs.add(width);
		inputs.add(height);
		outputs.add(outputTurtle);
	}
	
	@Override
	public String getName() {
		return Translator.get("BorderName");
	}

	@Override
	public NodePanel getPanel() {
		return new Generator_Empty_Panel(this);
	}

	@Override
	public boolean iterate() {		
		Turtle turtle = new Turtle();
		
		double yMin = -height.getValue()/2;
		double yMax =  height.getValue()/2;
		double xMin = -width.getValue()/2;
		double xMax =  width.getValue()/2;

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
