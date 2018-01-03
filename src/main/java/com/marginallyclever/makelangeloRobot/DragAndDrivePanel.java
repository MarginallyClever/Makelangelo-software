package com.marginallyclever.makelangeloRobot;

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

import com.marginallyclever.makelangelo.Translator;
/**
 * Drag cursor to drive plotter control. 
 * @author droyer
 * @since 7.4.5
 * @deprecated
 */
public class DragAndDrivePanel extends JPanel implements MouseListener, MouseMotionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JLabel coordinates;
	private JPanel dragAndDrive;
	private boolean mouseInside, mouseOn;
	private double mouseLastX, mouseLastY;
	private MakelangeloRobot robot;
	
	DragAndDrivePanel(MakelangeloRobot robot) {
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
		dragAndDrive.addMouseListener(this);
		dragAndDrive.addMouseMotionListener(this);

		coordinates = new JLabel(Translator.get("ClickAndDrag"));
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.CENTER;

		// TODO dimensioning doesn't work right.  The better way would be a pen tool to drag on the 3d view.  That's a lot of work.
		Dimension dims = new Dimension();
		dims.setSize( 150, 150 * (double)robot.getSettings().getPaperWidth()/(double)robot.getSettings().getPaperHeight());
		dragAndDrive.setPreferredSize(dims);
		dragAndDrive.add(coordinates,c);
	}


	public void mouseClicked(MouseEvent e) {}
	public void mouseDragged(MouseEvent e) {
		mouseAction(e);
	}
	public void mouseEntered(MouseEvent e) {
		mouseInside=true;
	}
	public void mouseExited(MouseEvent e) {
		mouseInside=false;
		mouseOn=false;
	}
	public void mouseMoved(MouseEvent e) {
		mouseAction(e);
	}
	public void mousePressed(MouseEvent e) {
		mouseOn=true;
		mouseAction(e);
	}
	public void mouseReleased(MouseEvent e) {
		mouseOn=false;
	}
	public void mouseWheelMoved(MouseEvent e) {}

	public void mouseAction(MouseEvent e) {
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
			x *= 10 * robot.getSettings().getPaperWidth()  / w;
			y *= 10 * robot.getSettings().getPaperHeight() / h;
			double dx = x-mouseLastX;
			double dy = y-mouseLastY;
			if(Math.sqrt(dx*dx+dy*dy)>=1) {
				mouseLastX=x;
				mouseLastY=y;
				String text = "X"+(Math.round(x*100)/100.0)+" Y"+(Math.round(y*100)/100.0);
				robot.sendLineToRobot("G00 "+text);
				coordinates.setText(text);
			} else {
				coordinates.setText("");
			}
		}
	}
}
