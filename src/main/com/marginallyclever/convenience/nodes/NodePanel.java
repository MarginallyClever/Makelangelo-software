package com.marginallyclever.convenience.nodes;

import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JFrame;

import com.marginallyclever.artPipeline.nodeConnector.NodeConnectorTransformedImage;
import com.marginallyclever.convenience.select.SelectBoolean;
import com.marginallyclever.convenience.select.SelectDouble;
import com.marginallyclever.convenience.select.SelectFile;
import com.marginallyclever.convenience.select.SelectInteger;
import com.marginallyclever.convenience.select.SelectPanel;
import com.marginallyclever.convenience.select.SelectReadOnlyText;
import com.marginallyclever.convenience.select.SelectString;
import com.marginallyclever.convenience.select.SelectTextArea;

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
	
	public void showInputPanel(JFrame parent) {
		System.out.println("Node name "+myNode.getName());
		
		JDialog dialog = new JDialog(parent, myNode.getName());
		
		SelectPanel panel = new SelectPanel();

		for(NodeConnector<?> nc : myNode.inputs ) {
			System.out.println("Node input "+nc.getType());
			String typeName = nc.getType();
			String inputName = nc.getName();
			if(typeName.equals("Boolean")) {
				panel.add(new SelectBoolean(inputName, ((NodeConnector<Boolean>)nc).getValue()));
			}
			if(typeName.equals("Integer")) {
				panel.add(new SelectInteger(inputName, ((NodeConnector<Integer>)nc).getValue()));
			}
			if(typeName.equals("Double")) {
				panel.add(new SelectDouble(inputName, ((NodeConnector<Double>)nc).getValue()));
				
			}
			if(typeName.equals("String")) {
				panel.add(new SelectString(inputName, ((NodeConnector<String>)nc).getValue()));
			}
			
			if(typeName.equals("TransformedImage")) {
				panel.add(new SelectReadOnlyText("TransformedImage "+inputName));
			};
			if(typeName.equals("Turtle")) {
				panel.add(new SelectReadOnlyText("Turtle "+inputName));
			};
		}
		
		
		panel.finish();
		panel.getInteriorPanel().setPreferredSize(new Dimension(400,600));
		
		dialog.add(panel);
		dialog.pack();
		dialog.setVisible(true);
	}
}
