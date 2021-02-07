package com.marginallyclever.artPipeline.nodes;


import com.marginallyclever.artPipeline.nodeConnector.NodeConnectorTurtle;
import com.marginallyclever.artPipeline.nodes.panels.Generator_Package_Panel;
import com.marginallyclever.convenience.nodes.Node;
import com.marginallyclever.convenience.nodes.NodeConnectorInt;
import com.marginallyclever.convenience.nodes.NodePanel;
import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.makelangelo.Translator;

/**
 * Draws a border around the paper.  Uses current paper settings.
 * @author Dan Royer
 *
 */
public class Generator_Package extends Node {
	// shape of box
	private NodeConnectorInt width = new NodeConnectorInt(200);
	// shape of box
	private NodeConnectorInt length = new NodeConnectorInt(100);
	// shape of box
	private NodeConnectorInt height = new NodeConnectorInt(50);
	// results
	private NodeConnectorTurtle outputTurtle = new NodeConnectorTurtle();
	
	public Generator_Package() {
		super();
		inputs.add(width);
		inputs.add(length);
		inputs.add(height);
		outputs.add(outputTurtle);
	}
	
	@Override
	public String getName() {
		return Translator.get("Package");
	}

	@Override
	public NodePanel getPanel() {
		return new Generator_Package_Panel(this);
	}

	
	void drawRect(Turtle turtle,int x1,int y1,int x2,int y2) {
		turtle.moveTo(x1,y1);
		turtle.penDown();
		turtle.moveTo(x2,y1);
		turtle.moveTo(x2,y2);
		turtle.moveTo(x1,y2);
		turtle.moveTo(x1,y1);
		turtle.penUp();
	}
	
	void drawLine(Turtle turtle,int x1,int y1,int x2,int y2) {
		turtle.moveTo(x1,y1);
		turtle.penDown();
		turtle.moveTo(x2,y2);
		turtle.penUp();
	}

	@Override
	public boolean iterate() {
		Turtle turtle = new Turtle();
		int w = width.getValue();
		int h = height.getValue();
		int len = length.getValue();
		
		int ytot=2*w+3*h+20;
		int xtot=len+2*h;
		
		int x1=-xtot/2;
		int x2=x1+h;
		int x3=x2+len;
		int x4=x3+h;
		
		int y1=-ytot/2;
		int y2=y1+20;
		int y3=y2+w;
		int y4=y3+h;
		int y5=y4+w;
		int y6=y5+h;
		int y7=y6+h+5;
		

		turtle.penUp();
		// show extent
		turtle.moveTo(x1,y1);
		turtle.moveTo(x4,y1);
		turtle.moveTo(x4,y7);
		turtle.moveTo(x1,y7);
		turtle.moveTo(x1,y1);
		// start drawing

		drawRect(turtle,x2,y2,x3,y7);
		drawRect(turtle,x1,y3,x4,y6);

		drawLine(turtle,x1,y4,x4,y4);
		drawLine(turtle,x1,y5,x4,y5);
		
		drawLine(turtle,x1,y3,x2,y4); // 4 diags
		drawLine(turtle,x3,y5,x4,y6);
		drawLine(turtle,x1,y6,x2,y5);
		drawLine(turtle,x3,y4,x4,y3);

		drawRect(turtle,x2+20,y6-1,x3-20,y6+1); // lasche
		drawRect(turtle,x2,y5-1,x3,y5+1); // lasche
		drawRect(turtle,x2+20,y1,x3-20,y2); // lasche

		outputTurtle.setValue(turtle);
		
	    return false;
	}
}
