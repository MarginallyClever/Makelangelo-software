package com.marginallyclever.makelangelo;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import com.marginallyclever.core.node.Node;
import com.marginallyclever.core.node.NodePanel;

/**
 * Show NodePanel with a run/cancel button at the bottom.
 * @author Dan Royer
 */
public class NodeDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Node myNode;
	private NodePanel myPanel;
	private ArrayList<ActionListener> actionListeners;
	
	public NodeDialog(Frame owner,Node node) {
		super(owner,true);
		actionListeners = new ArrayList<ActionListener>();
		myNode = node;
		myPanel = new NodePanel(node);
	}
	
	public void run() {
		myPanel.buildPanel();
		
		setTitle(myNode.getName());
		
		JPanel runMePanel = new JPanel(new BorderLayout());
		
		final NodeDialog parent = this; 
		JButton buttonRun = new JButton(Translator.get("Makelangelo.action.run"));
		buttonRun.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				myNode.restart();
				boolean b = myNode.iterate();
				if(b) {
					// TODO change the run button to stay "stop"
					// TODO setup a thread and run it 
					// TODO watch for completion of the iteration or user click stop.
				}
				// Do something with the run results.
				ActionEvent evt = new ActionEvent(parent,0,"");
				notifyListeners(evt);
			}
		});
		
		runMePanel.add(myPanel,BorderLayout.CENTER);
		runMePanel.add(buttonRun,BorderLayout.SOUTH);
		
		add(runMePanel);
		pack();
		setVisible(true);
	}
	
	public void addActionListener(ActionListener listener) {
		actionListeners.add(listener);
	}
	
	public void removeActionListener(ActionListener listener) {
		actionListeners.remove(listener);
	}
	
	private void notifyListeners(ActionEvent evt) {
		for( ActionListener l : actionListeners ) {
			l.actionPerformed(evt);
		}
	}
}
