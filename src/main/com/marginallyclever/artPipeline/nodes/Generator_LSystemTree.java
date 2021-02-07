package com.marginallyclever.artPipeline.nodes;

import java.security.SecureRandom;

import com.marginallyclever.artPipeline.nodeConnector.NodeConnectorTurtle;
import com.marginallyclever.artPipeline.nodes.panels.Generator_LSystemTree_Panel;
import com.marginallyclever.convenience.nodes.Node;
import com.marginallyclever.convenience.nodes.NodeConnectorDouble;
import com.marginallyclever.convenience.nodes.NodeConnectorInt;
import com.marginallyclever.convenience.nodes.NodePanel;
import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.makelangelo.Translator;

/**
 * L System fractal
 * @author Dan Royer
 */
public class Generator_LSystemTree extends Node {
	// random seed
	private NodeConnectorInt inputSeed = new NodeConnectorInt(0xDEADBEEF);
	// resursion depth
	private NodeConnectorInt inputOrder = new NodeConnectorInt(4);
	// resursion width
	private NodeConnectorInt inputBranches = new NodeConnectorInt(3);
	// variation
	private NodeConnectorInt inputNoise = new NodeConnectorInt(0);
	// how far branches can spread
	private NodeConnectorDouble inputAngleSpan = new NodeConnectorDouble(120.0);
	// how far branches can spread
	private NodeConnectorDouble inputOrderScale = new NodeConnectorDouble(0.76);
	// results
	private NodeConnectorTurtle outputTurtle = new NodeConnectorTurtle();
	
	private int order;
	private int numBranches;
	private int noise;
	private double angleSpan;
	private double orderScale;
	private SecureRandom random;
	
	public Generator_LSystemTree() {
		super();
		inputs.add(inputSeed);
		inputs.add(inputOrder);
		inputs.add(inputBranches);
		inputs.add(inputNoise);
		inputs.add(inputAngleSpan);
		inputs.add(inputOrderScale);
		outputs.add(outputTurtle);
	}

	@Override
	public String getName() {
		return Translator.get("LSystemTreeName");
	}
	
	@Override
	public NodePanel getPanel() {
		return new Generator_LSystemTree_Panel(this);
	}
	
	@Override
	public boolean iterate() {
		Turtle turtle = new Turtle();
		
		random = new SecureRandom();
		random.setSeed(inputSeed.getValue());
		
		order = inputOrder.getValue();
		numBranches = inputBranches.getValue();
		noise = inputNoise.getValue();
		angleSpan = inputAngleSpan.getValue();
		orderScale = inputOrderScale.getValue();
				
		// move to starting position
		turtle.moveTo(0,-100);
		turtle.turn(90);
		turtle.penDown();
		// do the curve
		lSystemTree(turtle,order, 10);

		outputTurtle.setValue(turtle);
	    return false;
	}


	// recursive L System tree fractal
	private void lSystemTree(Turtle turtle,int n, double distance) {
		if (n == 0) return;

		turtle.forward(distance);
		if(n>1) {
			double angleStep = angleSpan / (float)(numBranches-1);

			double oldAngle = turtle.getAngle();
			double len = distance*orderScale;
			turtle.turn(-(angleSpan/2.0f));
			for(int i=0;i<numBranches;++i) {
				lSystemTree(turtle,n-1,len - len*random.nextDouble()*(noise/100.0f) );
				if(noise>0) {
					turtle.turn(angleStep + (random.nextDouble()-0.5)*(noise/100.0f)*angleStep);
				} else {
					turtle.turn(angleStep);
				}
			}
			turtle.setAngle(oldAngle);
		}
		turtle.forward(-distance);
	}


	public void setOrder(int value) {
		order=value;	
	}
	public int getOrder() {
		return order;
	}

	public void setScale(double value) {
		orderScale = value;
	}
	public double getScale() {
		return orderScale;
	}

	public void setAngle(double value) {
		angleSpan = value;
	}
	public double getAngle() {
		return angleSpan;
	}

	public void setBranches(int value) {
		numBranches = value;
	}
	public int getBranches() {
		return numBranches;
	}

	public void setNoise(int value) {
		noise = value;		
	}

	public int getNoise() {
		return noise;		
	}
}