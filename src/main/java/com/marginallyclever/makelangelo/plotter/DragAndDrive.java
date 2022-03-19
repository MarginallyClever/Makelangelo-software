package com.marginallyclever.makelangelo.plotter;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.makelangelo.Translator;

/**
 * Drag cursor to drive plotter control. 
 * @author droyer
 * @since 7.4.5
 * @deprecated
 */
@Deprecated
public class DragAndDrive extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JLabel coordinates;
	private JPanel dragAndDrive;
	private boolean mouseInside, mouseOn;
	private double mouseLastX, mouseLastY;
	private Plotter robot;
	
	DragAndDrive(Plotter robot) {
		super(new GridBagLayout());

		this.robot = robot;
		mouseInside=false;
		mouseOn=false;
		mouseLastX=mouseLastY=0;

		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx = 0;
		con1.gridy = 0;
		con1.weightx = 1;
		con1.weighty = 0;
		con1.fill = GridBagConstraints.HORIZONTAL;
		con1.anchor = GridBagConstraints.NORTHWEST;
		

		dragAndDrive = new JPanel(new GridBagLayout());
		dragAndDrive.setBorder(BorderFactory.createLineBorder(Color.BLACK));

		coordinates = new JLabel(Translator.get("ClickAndDrag"));
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.CENTER;

		// TODO dimensioning doesn't work right.  The better way would be a pen tool to drag on the 3d view.  That's a lot of work.
		Dimension dims = new Dimension();
		double w = robot.getLimitRight()-robot.getLimitLeft();
		double h = robot.getLimitTop()-robot.getLimitBottom();
		dims.setSize( 150, 150 * w / h);
		dragAndDrive.setPreferredSize(dims);
		dragAndDrive.add(coordinates,c);
		

		dragAndDrive.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {}
			@Override
			public void mousePressed(MouseEvent e) {
				mouseOn=true;
				mouseAction(e);
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				mouseOn=false;
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				mouseInside=true;
			}
			@Override
			public void mouseExited(MouseEvent e) {
				mouseInside=false;
				mouseOn=false;
			}			
		});
		dragAndDrive.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseDragged(MouseEvent e) {
				mouseAction(e);
			}
			@Override
			public void mouseMoved(MouseEvent e) {
				mouseAction(e);
			}
		});
	}
	
	private void mouseAction(MouseEvent e) {
		if(mouseInside && mouseOn) {
			double x = (double)e.getX();
			double y = (double)e.getY();
			Dimension d = dragAndDrive.getSize();
			double w = d.getWidth();
			double h = d.getHeight();
			double cx = w/2.0;
			double cy = h/2.0;
			x = x - cx;
			y = cy - y;
			double w2 = robot.getLimitRight()-robot.getLimitLeft();
			double h2 = robot.getLimitTop()-robot.getLimitBottom();
			x *= 10 * w2 / w;
			y *= 10 * h2 / h;
			double dx = x-mouseLastX;
			double dy = y-mouseLastY;
			if(Math.sqrt(dx*dx+dy*dy)>=1) {
				mouseLastX=x;
				mouseLastY=y;
				
				robot.setPos(x,y);
				coordinates.setText("X"+StringHelper.formatDouble(x)+" Y"+StringHelper.formatDouble(y));
			} else {
				coordinates.setText("");
			}
		}
	}
}
