package com.marginallyclever.makelangeloRobot;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import com.jogamp.opengl.GL2;
import com.marginallyclever.communications.NetworkSession;
import com.marginallyclever.communications.NetworkSessionEvent;
import com.marginallyclever.communications.NetworkSessionListener;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.convenience.turtle.DefaultTurtleRenderer;
import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.convenience.turtle.TurtleRenderer;
import com.marginallyclever.makelangelo.CommandLineOptions;
import com.marginallyclever.makelangelo.SoundSystem;
import com.marginallyclever.makelangelo.makeArt.io.vector.SaveGCode;
import com.marginallyclever.makelangelo.preview.PreviewListener;
import com.marginallyclever.makelangeloRobot.marlin.MarlinEvent;
import com.marginallyclever.makelangeloRobot.marlin.MarlinFirmware2;
import com.marginallyclever.makelangeloRobot.marlin.RobotIdentityEvent;
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
	
	private Turtle myTurtle = new Turtle();
	
	private TurtleRenderer turtleRenderer;
	
	private PlotterDecorator decorator = null;

	private boolean areMotorsEngaged;
		
	private boolean penIsUp;
		
	private boolean didFindHome;
	
	private Point2D pos = new Point2D();

	private ArrayList<PlotterEventListener> listeners = new ArrayList<PlotterEventListener>();

	
	public Plotter() {
		super();
		
		areMotorsEngaged = true;
		penIsUp = false;
		didFindHome = false;
		pos.set(0,0);
	}
	
	// OBSERVER PATTERN

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
		notifyListeners(new PlotterEvent(PlotterEvent.PEN_UP,this,true));
	}
	
	public void lowerPen() {
		penIsUp = false;
		notifyListeners(new PlotterEvent(PlotterEvent.PEN_UP,this,false));
	}
	
	/**
	 * Display a dialog asking the user to change the pen
	 * @param toolNumber a 24 bit RGB color of the new pen.
	 */
	@Deprecated
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
	
	public void movePenToEdgeLeft() {
		moveTo(settings.getPaperLeft(),pos.y);
	}

	public void movePenToEdgeRight() {
		moveTo(settings.getPaperRight(),pos.y);
	}
	
	public void movePenToEdgeTop() {
		moveTo(pos.x,settings.getPaperTop());
	}
	
	public void movePenToEdgeBottom() {
		moveTo(pos.x,settings.getPaperBottom());
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
	
	public void setTurtle(Turtle turtle) {
		myTurtle = turtle;
	}

	public Turtle getTurtle() {
		return myTurtle;
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
		} else if (myTurtle != null) {
			if(turtleRenderer==null) {
				turtleRenderer = new DefaultTurtleRenderer(gl2);
				//turtleRenderer = new BarberPoleTurtleRenderer(gl2);
			}
			if(turtleRenderer!=null) {
				myTurtle.render(turtleRenderer);
			}
			
			//TODO
			//MakelangeloFirmwareVisualizer viz = new MakelangeloFirmwareVisualizer(); 
			//viz.render(gl2, turtleToRender, settings);
		}
	}	
	
	private void drawPhysicalLimits(GL2 gl2) {
		paintLimits(gl2);
		paintPaper(gl2);
		paintMargins(gl2);		
	}

	public void setTurtleRenderer(TurtleRenderer r) {
		turtleRenderer = r;
	}
		
	public TurtleRenderer getTurtleRenderer() {
		return turtleRenderer;
	}
		
	private void paintLimits(GL2 gl2) {
		gl2.glLineWidth(1);

		gl2.glColor3f(0.7f, 0.7f, 0.7f);
		gl2.glBegin(GL2.GL_TRIANGLE_FAN);
		gl2.glVertex2d(settings.getLimitLeft(), settings.getLimitTop());
		gl2.glVertex2d(settings.getLimitRight(), settings.getLimitTop());
		gl2.glVertex2d(settings.getLimitRight(), settings.getLimitBottom());
		gl2.glVertex2d(settings.getLimitLeft(), settings.getLimitBottom());
		gl2.glEnd();
	}

	private void paintPaper(GL2 gl2) {
		ColorRGB c = settings.getPaperColor();
		gl2.glColor3d(
				(double)c.getRed() / 255.0, 
				(double)c.getGreen() / 255.0, 
				(double)c.getBlue() / 255.0);
		gl2.glBegin(GL2.GL_TRIANGLE_FAN);
		gl2.glVertex2d(settings.getPaperLeft(), settings.getPaperTop());
		gl2.glVertex2d(settings.getPaperRight(), settings.getPaperTop());
		gl2.glVertex2d(settings.getPaperRight(), settings.getPaperBottom());
		gl2.glVertex2d(settings.getPaperLeft(), settings.getPaperBottom());
		gl2.glEnd();
	}
	
	private void paintMargins(GL2 gl2) {
		gl2.glPushMatrix();
		gl2.glColor3f(0.9f, 0.9f, 0.9f);
		gl2.glBegin(GL2.GL_LINE_LOOP);
		gl2.glVertex2d(settings.getMarginLeft(), settings.getMarginTop());
		gl2.glVertex2d(settings.getMarginRight(), settings.getMarginTop());
		gl2.glVertex2d(settings.getMarginRight(), settings.getMarginBottom());
		gl2.glVertex2d(settings.getMarginLeft(), settings.getMarginBottom());
		gl2.glEnd();
		gl2.glPopMatrix();
	}

	public boolean getPenIsUp() {
		return penIsUp;
	}
}
