package com.marginallyclever.makelangelo.plotter;

import java.security.InvalidParameterException;
import java.util.ArrayList;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.plotter.settings.PlotterSettings;
import com.marginallyclever.makelangelo.preview.PreviewListener;
import com.marginallyclever.makelangelo.preview.PreviewPanel;
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

	/**
	 * Subscribe to listen to {@link PlotterEvent}s.
	 * @param listener
	 */
	public void addPlotterEventListener(PlotterEventListener listener) {
		listeners.add(listener);
	}

	/**
	 * unsubscribe from {@link PlotterEvent}s.
	 * @param listener
	 */
	public void removePlotterEventListener(PlotterEventListener listener) {
		listeners.remove(listener);
	}
	
	private void firePlotterEvent(PlotterEvent e) {
		for (PlotterEventListener listener : listeners) listener.makelangeloRobotEvent(e);
	}

	// OBSERVER PATTERN ENDS

	/**
	 * Lift the pen up.  When the pen is up {@code setPos} will move the pen
	 * across the {@link Paper} without leaving a mark.
	 */
	public void raisePen() {
		penIsUp=true;
		firePlotterEvent(new PlotterEvent(PlotterEvent.PEN_UPDOWN,this,true));
	}
	
	/**
	 * Put the pen down.  When the pen is down {@code setPos} will drag the pen
	 * across the {@link Paper}, producing a visible line in the chosen pen color.
	 */
	public void lowerPen() {
		penIsUp = false;
		firePlotterEvent(new PlotterEvent(PlotterEvent.PEN_UPDOWN,this,false));
	}

	/**
	 * @return true if pen is up, false if pen is down.
	 */
	public boolean getPenIsUp() {
		return penIsUp;
	}
	
	private void requestUserChangeTool(int toolNumber) {
		firePlotterEvent(new PlotterEvent(PlotterEvent.TOOL_CHANGE, this, toolNumber));
	}
	
	/**
	 * When the {@link Plotter} first turns on it has no idea where it is.
	 * This method will instruct it to touch off the limit switches, after which 
	 * it will be at the home position obtained from {@code getHome()}.
	 */
	public void findHome() {
		raisePen();
		pos.set(settings.getHome());
		didFindHome = true;
		firePlotterEvent(new PlotterEvent(PlotterEvent.HOME_FOUND,this));
	}
	
	/**
	 * @return true if the machine has found home at least once.
	 */
	public boolean getDidFindHome() {
		return didFindHome;
	}

	/**
	 * move the {@link Plotter} pen holder to the desired position
	 * @param x in mm
	 * @param y in mm
	 */
	public void setPos(double x, double y) {
		pos.set(x,y);
		firePlotterEvent(new PlotterEvent(PlotterEvent.POSITION,this));
	}

	/**
	 * @return the current pen holder position.
	 */
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

	/**
	 * When a new connection is established, Marlin released the motors and reset the home position
	 */
	public void reInit() {
		areMotorsEngaged = false;
		didFindHome = false;
	}

	/**
	 * @return a reference to the active {@link PlotterSettings} in this {@link Plotter}.
	 * Modifications will immediately affect the {@link Plotter}.
	 */
	public PlotterSettings getSettings() {
		return settings;
	}
	
	/**
	 * Replace the existing {@link PlotterSettings} inside this {@link Plotter}.
	 * Does not fire any event notification.
	 * @param s the new settings.
	 */
	public void setSettings(PlotterSettings s) throws InvalidParameterException {
		if(s==null) throw new InvalidParameterException(PlotterSettings.class.getSimpleName()+" cannot be null.");
		settings=s;
	}
	
	/**
	 * Callback from {@link PreviewPanel} that it is time to render to the WYSIWYG display.
	 * @param gl2 the render context
	 */
	@Override
	public void render(GL2 gl2) {		
		float[] lineWidthBuf = new float[1];
		gl2.glGetFloatv(GL2.GL_LINE_WIDTH, lineWidthBuf, 0);
		
		drawPhysicalLimits(gl2);
		
		gl2.glLineWidth(lineWidthBuf[0]);
	}	
	
	/**
	 * Outline the drawing limits
	 * @param gl2
	 */
	private void drawPhysicalLimits(GL2 gl2) {
		gl2.glLineWidth(1);
		gl2.glColor3f(0.9f, 0.9f, 0.9f); // #color 
		
		gl2.glBegin(GL2.GL_LINE_LOOP);
		gl2.glVertex2d(settings.getLimitLeft(), settings.getLimitTop());
		gl2.glVertex2d(settings.getLimitRight(), settings.getLimitTop());
		gl2.glVertex2d(settings.getLimitRight(), settings.getLimitBottom());
		gl2.glVertex2d(settings.getLimitLeft(), settings.getLimitBottom());
		gl2.glEnd();
	}

	/**
	 * Instruct the {@link Plotter} to move.
	 * @param move a {@link TurtleMove} with instructions.
	 */
	public void turtleMove(TurtleMove move) {
		switch(move.type) {
		case TurtleMove.TRAVEL:
			if(!penIsUp) raisePen();
			setPos(move.x,move.y);
			break;
		case TurtleMove.DRAW:
			if(penIsUp) lowerPen();
			setPos(move.x,move.y);
			break;
		case TurtleMove.TOOL_CHANGE:
			requestUserChangeTool((int)move.x);
			break;
		}
	}

	/**
	 * @return the angle the pen holder servo should be at when the pen is up.
	 */
	public double getPenUpAngle() {
		return settings.getPenUpAngle();
	}

	/**
	 * @return the angle the pen holder servo should be at when the pen is down.
	 */
	public double getPenDownAngle() {
		return settings.getPenDownAngle();
	}

	/**
	 * @return the time it should take to move the pen lift servo from down position to up position.
	 */
	public double getPenLiftTime() {
		return settings.getPenLiftTime();
	}

	/**
	 * @return the top physical limit of the drawing area.
	 */
	public double getLimitTop() {
		return settings.getLimitTop();
	}

	/**
	 * @return the bottom physical limit of the drawing area.
	 */
	public double getLimitBottom() {
		return settings.getLimitBottom();
	}

	/**
	 * @return the left physical limit of the drawing area.
	 */
	public double getLimitLeft() {
		return settings.getLimitLeft();
	}

	/**
	 * @return the right physical limit of the drawing area.
	 */
	public double getLimitRight() {
		return settings.getLimitRight();
	}
}
