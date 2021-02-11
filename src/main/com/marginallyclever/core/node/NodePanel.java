package com.marginallyclever.core.node;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.ServiceLoader;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import com.marginallyclever.core.log.Log;
import com.marginallyclever.core.select.Select;
import com.marginallyclever.core.select.SelectPanel;
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

	public NodePanel(Node node) {
		super();
		setMyNode(node);
	}

	public Node getMyNode() {
		return myNode;
	}

	private void setMyNode(Node myNode) {
		this.myNode = myNode;
	}
	
	public void buildPanel() {
		//System.out.println("buildPanel "+myNode.getName());
		
		getInteriorPanel().removeAll();

		// inputs
		for(NodeConnector<?> nc : myNode.inputs ) {
			//System.out.println("  input "+nc.getName());
			Select s = nc.getSelect();
			s.getPanel().setToolTipText(nc.getDescription());
			add(s);
		}
		
		// outputs
		boolean showOutputs = false;
		if(showOutputs) {
			// divider
			addSeparator();
			
			for(NodeConnector<?> nc : myNode.outputs ) {
				//System.out.println("  output "+nc.getName());
				Select s = nc.getSelect();
				s.getPanel().setToolTipText(nc.getDescription());
				add(s);
			}
		}
		
		finish();
	}
	
	static public void main(String [] argv) {
		System.out.println("testOnePanel() start");
		Log.start();
		Translator.start();
		JFrame frame = new JFrame("Node panel test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.setLayout(new BorderLayout());
		JPanel comboBoxPane = new JPanel();
		JPanel cards = new JPanel(new CardLayout());
		frame.add(cards, BorderLayout.CENTER);
		frame.add(comboBoxPane, BorderLayout.PAGE_END);

		// build combo box
		ArrayList<String> names = new ArrayList<String>();
		ServiceLoader<Node> nodes = ServiceLoader.load(Node.class);
		for( Node n : nodes ) names.add(n.getName());
		String [] comboBoxItems = names.toArray(new String[] {});
		JComboBox<String> cb = new JComboBox<String>(comboBoxItems);
		cb.setEditable(false);
		// when combo box changes, visible card changes. 
		cb.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent evt) {
			    CardLayout cl = (CardLayout)(cards.getLayout());
			    cl.show(cards, (String)evt.getItem());
			}
		});
		comboBoxPane.add(cb);

		// build cards
		for( Node n : nodes ) {
			NodePanel p = new NodePanel(n);
			p.buildPanel();

			cards.add(n.getName(),p);
		}

		frame.setPreferredSize(new Dimension(400,600));
		frame.pack();
		frame.setVisible(true);
		System.out.println("testOnePanel() end");
	}
}
