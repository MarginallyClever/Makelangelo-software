package com.marginallyclever.convenience.nodes;

import java.awt.Dimension;

import javax.swing.JFrame;

import org.junit.Test;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.convenience.select.SelectBoolean;
import com.marginallyclever.convenience.select.SelectDouble;
import com.marginallyclever.convenience.select.SelectInteger;
import com.marginallyclever.convenience.select.SelectPanel;
import com.marginallyclever.convenience.select.SelectReadOnlyText;
import com.marginallyclever.convenience.select.SelectString;
import com.marginallyclever.makelangelo.Translator;

/**
 * All generators have a panel with options.  This is their shared root.
 * @author Dan Royer
 *
 */
public class NodePanel extends SelectPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Node myNode;
	
	protected NodePanel() {
		super();
	}

	protected NodePanel(Node node) {
		super();
		setMyNode(node);
	}

	public Node getMyNode() {
		return myNode;
	}

	private void setMyNode(Node myNode) {
		this.myNode = myNode;
	}
	
	public void buildInputPanel(JFrame parent) {
		System.out.println("buildInputPanel "+myNode.getName());
		
		getInteriorPanel().removeAll();

		for(NodeConnector<?> nc : myNode.inputs ) {
			System.out.println("Node input "+nc.getType());
			String typeName = nc.getType();
			String inputName = nc.getName();
			if(typeName.equals("Boolean")) {
				this.add(new SelectBoolean(inputName, ((NodeConnector<Boolean>)nc).getValue()));
			}
			if(typeName.equals("Integer")) {
				this.add(new SelectInteger(inputName, ((NodeConnector<Integer>)nc).getValue()));
			}
			if(typeName.equals("Double")) {
				this.add(new SelectDouble(inputName, ((NodeConnector<Double>)nc).getValue()));
				
			}
			if(typeName.equals("String")) {
				this.add(new SelectString(inputName, ((NodeConnector<String>)nc).getValue()));
			}
			
			if(typeName.equals("TransformedImage")) {
				this.add(new SelectReadOnlyText("TransformedImage "+inputName));
			};
			if(typeName.equals("Turtle")) {
				this.add(new SelectReadOnlyText("Turtle "+inputName));
			};
		}
		
		this.finish();
		this.getInteriorPanel().setPreferredSize(new Dimension(400,600));
	}
	
	static public void main(String [] argv) {
		System.out.println("Hello, World!");
		Log.start();
		Translator.start();
		JFrame frame = new JFrame("Node dialog test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Node n = new com.marginallyclever.artPipeline.nodes.Generator_Lissajous();
		NodePanel p = n.getPanel();
		p.buildInputPanel(frame);

		frame.getContentPane().add(p.getInteriorPanel());
		frame.pack();
		frame.setVisible(true);
		System.out.println("Done!");
	}
}
