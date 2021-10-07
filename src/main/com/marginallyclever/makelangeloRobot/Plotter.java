package com.marginallyclever.makelangeloRobot;

import java.util.ArrayList;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangelo.preview.PreviewListener;
import com.marginallyclever.makelangelo.turtle.TurtleMove;
import com.marginallyclever.makelangeloRobot.settings.PlotterSettings;

/**
 * {@link Plotter} is the Controller for a physical robot, following a
 * Model-View-Controller design pattern.  It also contains non-persistent model data. 
 * {@link PlotterPanel} is one of the Views. 
 * {@link PlotterSettings} is the persistent Model data (machine configuration).
 * 
 * @author Dan
 * @since 7.2.10
 */
public class Plotter implements PreviewListener {	
	private PlotterSettings settings = new PlotterSettings();

	private PlotterDecorator decorator = null;
	// are motors actively engaged?  when disengaged pen can drift and re-homing is required.
	private boolean areMotorsEngaged;
	// did the robot find home?  if it has not then the pen position is undefined.
	private boolean didFindHome;
	// if pen is "up" then it is not drawing.
	private boolean penIsUp;
	// current pen position
	private Point2D pos = new Point2D();

	public Plotter() {
		super();
		
		areMotorsEngaged = true;
		penIsUp = false;
		didFindHome = false;
		pos.set(0,0);
	}
	
	// OBSERVER PATTERN
	
	private ArrayList<PlotterEventListener> listeners = new ArrayList<PlotterEventListener>();

	public void addListener(PlotterEventListener listener) {
		listeners.add(listener);
	}

	public void removeListener(PlotterEventListener listener) {
		listeners.remove(listener);
	}
	
	private void notifyListeners(PlotterEvent e) {
		for (PlotterEventListener listener : listeners) listener.makelangeloRobotEvent(e);
	}

	// OBSERVER PATTERN ENDS
	
	public void raisePen() {
		penIsUp=true;
		notifyListeners(new PlotterEvent(PlotterEvent.PEN_UPDOWN,this,true));
	}
	
	public void lowerPen() {
		penIsUp = false;
		notifyListeners(new PlotterEvent(PlotterEvent.PEN_UPDOWN,this,false));
	}
	
	private void requestUserChangeTool(int toolNumber) {
		notifyListeners(new PlotterEvent(PlotterEvent.TOOL_CHANGE, this, toolNumber));
	}

	/**
	 * @return travel or draw feed rate, depending on pen state.
	 */
	@Deprecated
	public double getCurrentFeedRate() {
		return penIsUp
				? settings.getTravelFeedRate()
				: settings.getDrawFeedRate();
	}

	public void goHome() {
		moveTo(settings.getHomeX(),settings.getHomeY());
		pos.set(settings.getHomeX(),settings.getHomeY());
		didFindHome = true;
		notifyListeners(new PlotterEvent(PlotterEvent.HOME_FOUND,this));
	}
	
	public void findHome() {
		raisePen();
		pos.set(settings.getHomeX(),settings.getHomeY());
		didFindHome = true;
		notifyListeners(new PlotterEvent(PlotterEvent.HOME_FOUND,this));
	}
	
	public boolean getDidFindHome() {
		return didFindHome;
	}

	// in mm
	public void setPos(double x,double y) {
		pos.set(x,y);
		notifyListeners(new PlotterEvent(PlotterEvent.POSITION,this));
	}
	
	// in mm
	public void setPos(Point2D p) {
		pos.set(p);
		notifyListeners(new PlotterEvent(PlotterEvent.POSITION,this));
	}

	// in mm
	public void moveTo(double x, double y) {
		setPos(x,y);
	}

	// in mm
	public void moveTo(Point2D p) {
		setPos(p);
	}

	// in mm
	public Point2D getPos() {
		return pos;
	}
	
	public boolean getAreMotorsEngaged() {
		return areMotorsEngaged;
	}
		
	public void disengageMotors() {
		areMotorsEngaged = false;
		notifyListeners(new PlotterEvent(PlotterEvent.MOTORS_ENGAGED,this,false));
	}

	public void engageMotors() {
		areMotorsEngaged = true;
		notifyListeners(new PlotterEvent(PlotterEvent.MOTORS_ENGAGED,this,true));
	}
	
	public PlotterSettings getSettings() {
		return settings;
	}

	public void setDecorator(PlotterDecorator arg0) {
		decorator = arg0;
	}

	@Override
	public void render(GL2 gl2) {
		float[] lineWidthBuf = new float[1];
		gl2.glGetFloatv(GL2.GL_LINE_WIDTH, lineWidthBuf, 0);
		
		drawPhysicalLimits(gl2);
		
		// hardware features
		settings.getHardwareProperties().render(gl2, this);

		gl2.glLineWidth(lineWidthBuf[0]);
		
		if (decorator != null) {
			// filters can also draw WYSIWYG previews while converting.
			decorator.render(gl2);
		}
	}	
	
	private void drawPhysicalLimits(GL2 gl2) {
		gl2.glLineWidth(1);

		gl2.glColor3f(0.7f, 0.7f, 0.7f);
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
}
