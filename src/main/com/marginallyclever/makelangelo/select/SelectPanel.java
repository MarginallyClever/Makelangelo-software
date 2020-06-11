package com.marginallyclever.makelangelo.select;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.marginallyclever.convenience.ColorRGB;

/**
 * A container for all Select elements, to facilitate formating as a group.
 * @author Dan Royer
 * @since 7.24.0
 */
public class SelectPanel implements Observer {
	protected JPanel panel;
	private GridBagConstraints gbc;
	
	public SelectPanel() {
		super();
		
		panel = new JPanel();
		//p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
		panel.setLayout(new GridBagLayout());
		//p.setBorder(new LineBorder(Color.RED));
		panel.setBorder(new EmptyBorder(5,5,5,5));

		gbc = new GridBagConstraints();
		gbc.weightx=1;
		gbc.gridx  =0;
		gbc.gridy  =0;
		gbc.fill      = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.insets.set(5, 5, 5, 5); 
	}
	
	public void add(Select c) {
		gbc.gridy++;
		panel.add(c.getPanel(),gbc);
		c.addObserver(this);
	}
	
	public void finish() {
		gbc.weighty=1;
		gbc.gridy++;
		panel.add(new JLabel(""),gbc);
	}
	
	public JPanel getPanel() {
		return panel;
	}

	@Override
	public void update(Observable o, Object arg) {}
	
	/**
	 * Run this to visually examine every panel element and how they look in next to each other.
	 * @param args ignored
	 */
	public static void main(String[] args) {
		JFrame frame = new JFrame("Select Look and feel");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		SelectPanel panel = new SelectPanel();
		frame.getContentPane().add(panel.getPanel());
		SelectBoolean a = new SelectBoolean("AAAAAAAAAAA",false);
		SelectButton b = new SelectButton("B");
		SelectColor c = new SelectColor(frame,"CCCCCC",new ColorRGB(0,0,0));
		SelectFile d = new SelectFile("D",null);
		SelectFloat e = new SelectFloat("E",0.0f);
		SelectInteger f = new SelectInteger("FFF",0);
		String [] list = {"cars","trains","planes","boats","rockets"};
		SelectOneOfMany g = new SelectOneOfMany("G",list,0);
		String ipsum = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
		SelectReadOnlyText h = new SelectReadOnlyText("H "+ipsum);
		SelectSlider i = new SelectSlider("I",200,0,100);
		SelectTextArea j = new SelectTextArea("J",ipsum);
		
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
		panel.finish();
		panel.getPanel().setPreferredSize(new Dimension(400,600));
		
		frame.pack();
		frame.setVisible(true);
	} 
}
