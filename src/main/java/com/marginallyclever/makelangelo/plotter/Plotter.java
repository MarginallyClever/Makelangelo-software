package com.marginallyclever.makelangelo.plotter;

import com.jogamp.opengl.GL2;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.preview.PreviewListener;
import com.marginallyclever.makelangelo.preview.OpenGLPanel;
import com.marginallyclever.makelangelo.turtle.TurtleMove;

import javax.vecmath.Point2d;
import java.security.InvalidParameterException;
import java.util.ArrayList;

/**
 * {@link Plotter} contains the live state of the drawing robot: the position of the pen, is it homed, and
 * are motors engaged.  It also contains the {@link PlotterSettings} which define the physical characteristics.
 * @author Dan
 * @since 7.2.10
 */
public class Plotter implements PreviewListener, Cloneable {	
	private PlotterSettings settings = new PlotterSettings("Makelangelo 5");

	// are motors actively engaged?  when disengaged pen can drift and re-homing is required.
	private boolean areMotorsEngaged = true;
	// did the robot find home?  if it has not then the pen position is undefined.
	private boolean didFindHome = false;
	// if pen is "up" then it is not drawing.
	private boolean penIsUp = false;
	// current pen position
	private Point2d pos = new Point2d(0,0);
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		Plotter b = (Plotter)super.clone();
		b.listeners = new ArrayList<PlotterEventListener>();
		b.pos = new Point2d();
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
		for (PlotterEventListener listener : listeners) listener.plotterEvent(e);
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
	public Point2d getPos() {
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
	 * @param s the new plottersettings.
	 */
	public void setSettings(PlotterSettings s) throws InvalidParameterException {
		if(s==null) throw new InvalidParameterException(PlotterSettings.class.getSimpleName()+" cannot be null.");
		settings=s;
	}
	
	/**
	 * Callback from {@link OpenGLPanel} that it is time to render to the WYSIWYG display.
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
		gl2.glVertex2d(settings.getDouble(PlotterSettings.LIMIT_LEFT), settings.getDouble(PlotterSettings.LIMIT_TOP));
		gl2.glVertex2d(settings.getDouble(PlotterSettings.LIMIT_RIGHT), settings.getDouble(PlotterSettings.LIMIT_TOP));
		gl2.glVertex2d(settings.getDouble(PlotterSettings.LIMIT_RIGHT), settings.getDouble(PlotterSettings.LIMIT_BOTTOM));
		gl2.glVertex2d(settings.getDouble(PlotterSettings.LIMIT_LEFT), settings.getDouble(PlotterSettings.LIMIT_BOTTOM));
		gl2.glEnd();
	}

	/**
	 * Instruct the {@link Plotter} to move.
	 * @param move a {@link TurtleMove} with instructions.
	 */
	public void turtleMove(TurtleMove move) {
		switch(move.type) {
		case TRAVEL:
			if(!penIsUp) raisePen();
			setPos(move.x,move.y);
			break;
		case DRAW_LINE:
			if(penIsUp) lowerPen();
			setPos(move.x,move.y);
			break;
		case TOOL_CHANGE:
			requestUserChangeTool((int)move.x);
			break;
		}
	}
}
