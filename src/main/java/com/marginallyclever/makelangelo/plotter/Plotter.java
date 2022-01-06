package com.marginallyclever.makelangelo.plotter;

import java.util.ArrayList;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangelo.plotter.settings.PlotterSettings;
import com.marginallyclever.makelangelo.preview.PreviewListener;
import com.marginallyclever.makelangelo.turtle.TurtleMove;

/**
 * {@link Plotter} is a virtual plotter.  It is directly responsible for the live state of the plotter.
 * It also contains {@link PlotterSettings}, which constrain the total possible valid live states.
 * @author Dan
 * @since 7.2.10
 */
public class Plotter implements PreviewListener, Cloneable {	
	private PlotterSettings settings = new PlotterSettings();

	// are motors actively engaged?  when disengaged pen can drift and re-homing is required.
	private boolean areMotorsEngaged = true;
	// did the robot find home?  if it has not then the pen position is undefined.
	private boolean didFindHome = false;
	// if pen is "up" then it is not drawing.
	private boolean penIsUp = false;
	// current pen position
	private Point2D pos = new Point2D(0,0);
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		Plotter b = (Plotter)super.clone();
		b.listeners = new ArrayList<PlotterEventListener>();
		b.pos = new Point2D();
		b.pos.set(this.pos);
		return b;
	}
	
	// OBSERVER PATTERN
	
	private ArrayList<PlotterEventListener> listeners = new ArrayList<PlotterEventListener>();

	public void addPlotterEventListener(PlotterEventListener listener) {
		listeners.add(listener);
	}

	public void removePlotterEventListener(PlotterEventListener listener) {
		listeners.remove(listener);
	}
	
	private void firePlotterEvent(PlotterEvent e) {
		for (PlotterEventListener listener : listeners) listener.makelangeloRobotEvent(e);
	}

	// OBSERVER PATTERN ENDS
	
	public void raisePen() {
		penIsUp=true;
		firePlotterEvent(new PlotterEvent(PlotterEvent.PEN_UPDOWN,this,true));
	}
	
	public void lowerPen() {
		penIsUp = false;
		firePlotterEvent(new PlotterEvent(PlotterEvent.PEN_UPDOWN,this,false));
	}
	
	private void requestUserChangeTool(int toolNumber) {
		firePlotterEvent(new PlotterEvent(PlotterEvent.TOOL_CHANGE, this, toolNumber));
	}
	
	public void findHome() {
		raisePen();
		pos.set(settings.getHome());
		didFindHome = true;
		firePlotterEvent(new PlotterEvent(PlotterEvent.HOME_FOUND,this));
	}
	
	public boolean getDidFindHome() {
		return didFindHome;
	}

	// in mm
	public void moveTo(double x, double y) {
		pos.set(x,y);
		firePlotterEvent(new PlotterEvent(PlotterEvent.POSITION,this));
	}

	// in mm
	public Point2D getPos() {
		return pos;
	}

	/**
	 * @return true for engaged, false for disengaged.
	 */
	public boolean getMotorsEngaged() {
		return areMotorsEngaged;
	}
	
	/**
	 * @param state true for engaged, false for disengaged.
	 */
	public void setMotorsEngaged(boolean state) {
		areMotorsEngaged = state;
		firePlotterEvent(new PlotterEvent(PlotterEvent.MOTORS_ENGAGED,this,state));
	}
	
	public PlotterSettings getSettings() {
		return settings;
	}
	
	public void setSettings(PlotterSettings s) {
		settings=s;
	}
	
	@Override
	public void render(GL2 gl2) {
		float[] lineWidthBuf = new float[1];
		gl2.glGetFloatv(GL2.GL_LINE_WIDTH, lineWidthBuf, 0);
		
		drawPhysicalLimits(gl2);
		
		// hardware features
		settings.getHardwareProperties().render(gl2, this);

		gl2.glLineWidth(lineWidthBuf[0]);
	}	
	
	private void drawPhysicalLimits(GL2 gl2) {
		gl2.glLineWidth(1);
		gl2.glColor3f(0.9f, 0.9f, 0.9f); // #color Machine Hardware Drawing Limits
		
		gl2.glBegin(GL2.GL_LINE_LOOP);
		gl2.glVertex2d(settings.getLimitLeft(), settings.getLimitTop());
		gl2.glVertex2d(settings.getLimitRight(), settings.getLimitTop());
		gl2.glVertex2d(settings.getLimitRight(), settings.getLimitBottom());
		gl2.glVertex2d(settings.getLimitLeft(), settings.getLimitBottom());
		gl2.glEnd();
	}

	public boolean getPenIsUp() {
		return penIsUp;
	}

	public void turtleMove(TurtleMove move) {
		switch(move.type) {
		case TurtleMove.TRAVEL:
			if(!penIsUp) raisePen();
			moveTo(move.x,move.y);
			break;
		case TurtleMove.DRAW:
			if(penIsUp) lowerPen();
			moveTo(move.x,move.y);
			break;
		case TurtleMove.TOOL_CHANGE:
			requestUserChangeTool((int)move.x);
			break;
		}
	}

	public double getPenUpAngle() {
		return settings.getPenUpAngle();
	}

	public double getPenDownAngle() {
		return settings.getPenDownAngle();
	}

	public double getPenLiftTime() {
		return settings.getPenLiftTime();
	}

	public double getLimitBottom() {
		return settings.getLimitBottom();
	}

	public double getLimitLeft() {
		return settings.getLimitLeft();
	}

	public double getLimitRight() {
		return settings.getLimitRight();
	}

	public double getLimitTop() {
		return settings.getLimitTop();
	}
}
