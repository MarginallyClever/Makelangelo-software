package com.marginallyclever.makelangelo.select;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.marginallyclever.convenience.ColorRGB;

/**
 * A container for all Select elements, to facilitate formatting as a group.
 * @author Dan Royer
 * @since 7.24.0
 */
public class SelectPanel extends JPanel implements PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2141566275423257798L;

	private JPanel interiorPanel = new JPanel();
	
	public SelectPanel() {
		super(new BorderLayout());
		add(interiorPanel,BorderLayout.PAGE_START);

		//interiorPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
		
		interiorPanel.setBorder(new EmptyBorder(5,5,5,5));
		interiorPanel.setLayout(new BoxLayout(interiorPanel, BoxLayout.Y_AXIS));
	}
	
	public void add(Select c) {
		//gbc.gridy++;
		//interiorPanel.add(c,gbc);
		interiorPanel.add(c);
		c.addPropertyChangeListener(this);
	}
	
	// OBSERVER PATTERN	
	private ArrayList<SelectPanelChangeListener> listeners = new ArrayList<SelectPanelChangeListener>();

	public void removeSelectPanelChangeListener(SelectPanelChangeListener ear) {
		listeners.remove(ear);
	}

	public void addSelectPanelChangeListener(SelectPanelChangeListener ear) {
		listeners.add(ear);
	}
	
	protected void fireSelectPanelChangeEvent(PropertyChangeEvent evt) {
		for(SelectPanelChangeListener ear : listeners) {
			ear.selectPanelPropertyChange(evt);
		}
		firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		fireSelectPanelChangeEvent(evt);
	}
	
	/**
	 * Run this to visually examine every panel element and how they look in next to each other.
	 * @param args ignored
	 */
	public static void main(String[] args) throws Exception {
		JFrame frame = new JFrame("Select Look and feel");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		SelectPanel panel = new SelectPanel();
		SelectBoolean a = new SelectBoolean("A","AAAAAAAAAAA",false);
		SelectButton b = new SelectButton("B","B");
		SelectColor c = new SelectColor("C","CCCCCC",new ColorRGB(0,0,0),frame);
		SelectFile d = new SelectFile("D","D",null);
		SelectDouble e = new SelectDouble("E","E",0.0f);
		SelectInteger f = new SelectInteger("F","FFF",0);
		String [] list = {"cars","trains","planes","boats","rockets"};
		SelectOneOfMany g = new SelectOneOfMany("G","G",list,0);
		String ipsum = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. <a href=\"http://google.com\">Google</a> Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
		SelectReadOnlyText h = new SelectReadOnlyText("H","H "+ipsum);
		SelectSlider i = new SelectSlider("I","I",200,0,100);
		SelectTextArea j = new SelectTextArea("J","J",ipsum);
		
		panel.add(a);
		panel.add(b);
		panel.add(c);
		panel.add(d);
		panel.add(e);
		panel.add(f);
		panel.add(g);
		panel.add(h);
		panel.add(i);
		panel.add(j);
		
		// test finish
		panel.setPreferredSize(new Dimension(400,600));
		
		panel.addPropertyChangeListener((evt)-> {
			System.out.println("Event: "+evt.toString());
		});

		frame.getContentPane().add(panel);
		frame.pack();
		frame.setVisible(true);
	}
}
