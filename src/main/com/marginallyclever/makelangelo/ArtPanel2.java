package com.marginallyclever.makelangelo;

import java.awt.Color;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.ServiceLoader;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.marginallyclever.artPipeline.nodes.ImageConverter;
import com.marginallyclever.core.log.Log;
import com.marginallyclever.core.node.Node;
import com.marginallyclever.core.node.NodePanel;
import com.marginallyclever.core.select.SelectButton;
import com.marginallyclever.core.select.SelectPanel;

public class ArtPanel2 {
	protected JFrame parentFrame;
	
	public ArtPanel2() {
		super();
	}
	
	public JPanel buildPanel(JFrame frame) {
		parentFrame = frame;
		
		JPanel rootPanel = new JPanel();
		rootPanel.setBorder(BorderFactory.createLoweredBevelBorder());
		rootPanel.setLayout(new BoxLayout(rootPanel,BoxLayout.LINE_AXIS));
		
		SelectPanel firstLayer = new SelectPanel();
		ServiceLoader<ImageConverter> imageConverters = ServiceLoader.load(ImageConverter.class);
		HashMap<SelectButton,JPanel> mani = new HashMap<SelectButton,JPanel>(); 
		
		for( Node generator : imageConverters ) {
			SelectButton b = new SelectButton(generator.getName()); 
			mani.put(b,new NodePanel(generator));
			firstLayer.add(b);
			b.addPropertyChangeListener(new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					JPanel p = mani.get(b);
					p.setMaximumSize(new Dimension(100,10000));
					for(SelectButton n : mani.keySet() ) {
						n.setForeground(Color.BLACK);
					}
					b.setForeground(Color.BLUE);
					while(rootPanel.getComponentCount()>1) {
						rootPanel.remove(1);
					}
					rootPanel.add(new JScrollPane(p,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
					//rootPanel.add(Box.createHorizontalGlue());
					rootPanel.invalidate();
					rootPanel.revalidate();
				}
			});
		}
		
		JScrollPane pane = new JScrollPane(firstLayer,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		pane.setMaximumSize(new Dimension(800,10000));
		
		rootPanel.add(pane);
		rootPanel.add(Box.createHorizontalGlue());
		
		// add image manipulators
		
		return rootPanel;
	}
	
	/**
	 * Run this to visually examine every panel element and how they look in next to each other.
	 * @param args ignored
	 */
	public static void main(String[] args) {
		Log.start();
		Translator.start();
		JFrame frame = new JFrame("Art Pipeline Panel 2");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		ArtPanel2 ap = new ArtPanel2();
		
		JPanel p = ap.buildPanel(frame);
		
		frame.setPreferredSize(new Dimension(800,600));
		frame.add(p);
		
		frame.pack();
		frame.setVisible(true);
	} 
}
