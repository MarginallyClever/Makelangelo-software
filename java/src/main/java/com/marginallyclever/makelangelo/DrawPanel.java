package com.marginallyclever.makelangelo;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.prefs.Preferences;

import javax.swing.event.MouseInputListener;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLPipelineFactory;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;
import com.marginallyclever.drawingtools.DrawingTool;

// Custom drawing panel written as an inner class to access the instance variables.
public class DrawPanel extends GLJPanel implements MouseListener, MouseInputListener, GLEventListener  {
	static final long serialVersionUID=2;

	private Preferences prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.GRAPHICS);

	// Use debug pipeline?
	private static final boolean DEBUG_GL_ON=false;
	private static final boolean TRACE_GL_ON=false;

	// arc smoothness - increase to make more smooth and run slower.
	private static final double STEPS_PER_DEGREE=1;

	// progress
	private long linesProcessed=0;
	private boolean connected=false;
	private boolean running=false;

	// config
	private boolean showPenUpMoves=false;

	// motion control
	//private boolean mouseIn=false;
	private int buttonPressed=MouseEvent.NOBUTTON;
	private int oldx, oldy;
	private double gondolaX,gondolaY;

	// scale + position
	private double cameraOffsetX = 0.0d;
	private double cameraOffsetY = 0.0d;
	private double cameraZoom = 20.0d;
	private float drawScale = 0.1f;
	private int windowWidth=0;
	private int windowHeight=0;

	private final int lookAhead=500;

	private GCodeFile instructions;

	private DrawPanelDecorator drawDecorator=null;

	protected MakelangeloRobotSettings machine;

	// optimization - turn gcode into vectors once on load, draw vectors after that.
	private enum NodeType {
		COLOR, POS, TOOL
	}

	class DrawPanelNode {
		double x1, y1, x2, y2;
		Color c;
		int tool_id;
		int line_number;
		NodeType type;
	}

	ArrayList<DrawPanelNode> fast_nodes = new ArrayList<DrawPanelNode>();


	public DrawPanel(MakelangeloRobotSettings mc) {
		super();
		machine = mc;
		addMouseMotionListener(this);
		addMouseListener(this);
		addGLEventListener(this);
	}


	/**
	 * Set the current DrawDecorator.
	 *
	 * @param dd the new DrawDecorator
	 */
	public void setDecorator(DrawPanelDecorator dd) {
		drawDecorator = dd;
		emptyNodeBuffer();
	}


	/**
	 * set up the correct projection so the image appears in the right location and aspect ratio.
	 */
	@Override
	public void reshape(GLAutoDrawable glautodrawable, int x, int y, int width, int height) {
		GL2 gl2 = glautodrawable.getGL().getGL2();
		gl2.setSwapInterval(1);

		windowWidth = width;
		windowHeight = height;
		//window_aspect_ratio = window_width / window_height;

		gl2.glMatrixMode(GL2.GL_PROJECTION);
		gl2.glLoadIdentity();
		gl2.glOrtho(-windowWidth / 2.0d, windowWidth / 2.0d, -windowHeight / 2.0d, windowHeight / 2.0d, 1.0d, -1.0d);
	}

	/**
	 * turn on debug pipeline(s) if needed.
	 */
	@Override
	public void init(GLAutoDrawable drawable) {
		if (DEBUG_GL_ON) {
			try {
				// Debug ..
				GL gl = drawable.getGL();
				gl = gl.getContext().setGL(GLPipelineFactory.create("com.jogamp.opengl.Debug", null, gl, null));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (TRACE_GL_ON) {
			try {
				// Trace ..
				GL gl = drawable.getGL();
				gl = gl.getContext().setGL(GLPipelineFactory.create("com.jogamp.opengl.Trace", null, gl, new Object[]{System.err}));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	@Override
	public void dispose(GLAutoDrawable glautodrawable) {
	}


	/**
	 * refresh the image in the view
	 */
	@Override
	public void display(GLAutoDrawable glautodrawable) {
		//long now_time = System.currentTimeMillis();
		//float dt = (now_time - last_time)*0.001f;
		//last_time = now_time;
		//System.out.println(dt);

		GL2 gl2 = glautodrawable.getGL().getGL2();

		// draw the world
		render(gl2);
	}

	/**
	 * scale the picture of the robot to fake a zoom.
	 */
	public void zoomToFitPaper() {
		int drawPanelWidth = this.getWidth();
		int drawPanelHeight = this.getHeight();
		double widthOfPaper = machine.paperRight - machine.paperLeft;
		double heightOfPaper = machine.paperTop - machine.paperBottom;
		double drawPanelWidthZoom = drawPanelWidth / widthOfPaper;
		double drawPanelHeightZoom = drawPanelHeight / heightOfPaper;
		cameraZoom = (drawPanelWidthZoom < drawPanelHeightZoom ? drawPanelWidthZoom : drawPanelHeightZoom );
		cameraOffsetX = 0;
		cameraOffsetY = 0;
		repaint();
	}

	public void mousePressed(MouseEvent e) {
		buttonPressed=e.getButton();
		oldx=e.getX();
		oldy=e.getY();
	}
	public void mouseReleased(MouseEvent e) {
		buttonPressed=MouseEvent.NOBUTTON;
	}
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseDragged(MouseEvent e) {
		int x=e.getX();
		int y=e.getY();
		if(buttonPressed==MouseEvent.BUTTON1) {
			moveCamera(x,y);
		} else if(buttonPressed==MouseEvent.BUTTON3) {
			zoomCamera(y);
		}
		gondolaX=x-windowWidth/2;
		gondolaY=y-windowHeight/2;
		oldx=x;
		oldy=y;
		repaint();
	}
	public void mouseMoved(MouseEvent e) {
		int x=e.getX();
		int y=e.getY();
		gondolaX=x-windowWidth/2;
		gondolaY=y-windowHeight/2;
		repaint();
	}


	public void setGCode(GCodeFile gcode) {
		instructions = gcode;
		emptyNodeBuffer();
		// process the image into a buffer once rather than re-reading the gcode over and over again?
				repaint();
	}

	public void emptyNodeBuffer() {
		fast_nodes.clear();
		optimizeNodes();
	}


	public void updateMachineConfig() {
		repaint();
	}


	/**
	 * toggle pen up moves.
	 *
	 * @param state if <strong>true</strong> the pen up moves will be drawn.  if <strong>false</strong> they will be hidden.
	 */
	public void setShowPenUp(boolean state) {
		showPenUpMoves = state;
		instructions.changed = true;
		repaint();
	}

	/**
	 * @return the "show pen up" flag
	 */
	public boolean getShowPenUp() {
		return showPenUpMoves;
	}


	public void setLinesProcessed(long c) {
		linesProcessed = c;
		if ((linesProcessed % 10) == 0) repaint();
	}

	public void setConnected(boolean state) {
		connected = state;
		repaint();
	}

	public void setRunning(boolean state) {
		running = state;
		if (running == false) {
			linesProcessed = 0;
		}
	}

	/**
	 * returns angle of dy/dx as a value from 0...2PI
	 *
	 * @param dy a value from -1...1 inclusive
	 * @param dx a value from -1...1 inclusive
	 * @return angle of dy/dx as a value from 0...2PI
	 */
	private double atan3(double dy, double dx) {
		double a = Math.atan2(dy, dx);
		if (a < 0) a = (Math.PI * 2.0) + a;
		return a;
	}


	/**
	 * position the camera in from of the robot
	 *
	 * @param x position horizontally
	 * @param y position vertically
	 */
	private void moveCamera(int x, int y) {
		cameraOffsetX += (oldx - x) / cameraZoom;
		cameraOffsetY += (oldy - y) / cameraZoom;
	}

	/**
	 * scale the picture of the robot to fake a zoom.
	 *
	 * @param y
	 */
	private void zoomCamera(int y) {
		final double zoomAmount = (double) (y - oldy) * 0.01;
		cameraZoom += zoomAmount;
		if (Double.compare(cameraZoom, 0.1d) < 0) cameraZoom = 0.1d;
	}

	/**
	 * scale the picture of the robot to fake a zoom.
	 */
	public void zoomIn() {
		cameraZoom *= 4.0d / 3.0d;
		repaint();
	}

	/**
	 * scale the picture of the robot to fake a zoom.
	 */
	public void zoomOut() {
		cameraZoom *= 3.0d / 4.0d;
		repaint();
	}

	/**
	 * set up the correct modelview so the robot appears where it hsould.
	 *
	 * @param gl2
	 */
	private void paintCamera(GL2 gl2) {
		gl2.glMatrixMode(GL2.GL_MODELVIEW);
		gl2.glLoadIdentity();
		gl2.glScaled(cameraZoom, cameraZoom, 1.0d);
		gl2.glTranslated(-cameraOffsetX, cameraOffsetY, 0);
	}

	/**
	 * clear the panel
	 *
	 * @param gl2
	 */
	private void paintBackground(GL2 gl2) {
		// Clear The Screen And The Depth Buffer
    	gl2.glClearColor(212.0f/255.0f, 233.0f/255.0f, 255.0f/255.0f, 0.0f);

		// Special handling for the case where the GLJPanel is translucent
		// and wants to be composited with other Java 2D content
		if (GLProfile.isAWTAvailable() &&
				(this instanceof com.jogamp.opengl.awt.GLJPanel) &&
				!((com.jogamp.opengl.awt.GLJPanel) this).isOpaque() &&
				((com.jogamp.opengl.awt.GLJPanel) this).shouldPreserveColorBufferIfTranslucent()) {
			gl2.glClear(GL2.GL_DEPTH_BUFFER_BIT);
		} else {
			gl2.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		}
	}

	/**
	 * draw the machine edges and paper edges
	 *
	 * @param gl2
	 */
	private void paintLimits(GL2 gl2) {
		gl2.glColor3f(0.7f, 0.7f, 0.7f);
		gl2.glBegin(GL2.GL_TRIANGLE_FAN);
		gl2.glVertex2d(machine.limitLeft, machine.limitTop);
		gl2.glVertex2d(machine.limitRight, machine.limitTop);
		gl2.glVertex2d(machine.limitRight, machine.limitBottom);
		gl2.glVertex2d(machine.limitLeft, machine.limitBottom);
		gl2.glEnd();
		
		if (!connected) {
			gl2.glColor3f(194.0f / 255.0f, 133.0f / 255.0f, 71.0f / 255.0f);
			gl2.glColor3f(1, 1, 1);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
			gl2.glVertex2d(machine.paperLeft, machine.paperTop);
			gl2.glVertex2d(machine.paperRight, machine.paperTop);
			gl2.glVertex2d(machine.paperRight, machine.paperBottom);
			gl2.glVertex2d(machine.paperLeft, machine.paperBottom);
			gl2.glEnd();
		} else {
			gl2.glColor3f(194.0f / 255.0f, 133.0f / 255.0f, 71.0f / 255.0f);
			gl2.glColor3f(1, 1, 1);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
			gl2.glVertex2d(machine.paperLeft, machine.paperTop);
			gl2.glVertex2d(machine.paperRight, machine.paperTop);
			gl2.glVertex2d(machine.paperRight, machine.paperBottom);
			gl2.glVertex2d(machine.paperLeft, machine.paperBottom);
			gl2.glEnd();
		}
		// margin settings
		gl2.glPushMatrix();
		gl2.glColor3f(0.9f,0.9f,0.9f);
		gl2.glLineWidth(1);
		gl2.glScaled(machine.paperMargin,machine.paperMargin,1);
		gl2.glBegin(GL2.GL_LINE_LOOP);
		gl2.glVertex2d(machine.paperLeft, machine.paperTop);
		gl2.glVertex2d(machine.paperRight, machine.paperTop);
		gl2.glVertex2d(machine.paperRight, machine.paperBottom);
		gl2.glVertex2d(machine.paperLeft, machine.paperBottom);
		gl2.glEnd();
		gl2.glPopMatrix();
	}


	/**
	 * draw calibration point
	 * @param gl2
	 */
	private void paintCenter(GL2 gl2) {
		gl2.glColor3f(1,0,0);
		gl2.glBegin(GL2.GL_LINES);
		gl2.glVertex2f(-0.25f,0.0f);
		gl2.glVertex2f( 0.25f,0.0f);
		gl2.glVertex2f(0.0f,-0.25f);
		gl2.glVertex2f(0.0f, 0.25f);
		gl2.glEnd();
	}

	public void repaintNow() {
		validate();
		repaint();
	}

	// draw left motor, right motor
	private void paintMotors( GL2 gl2 ) {
		gl2.glColor3f(1,0.8f,0.5f);
		// left frame
		gl2.glPushMatrix();
		gl2.glTranslatef(-2.1f, 2.1f, 0);
		gl2.glBegin(GL2.GL_TRIANGLE_FAN);
		gl2.glVertex2d(machine.limitLeft-5f, machine.limitTop+5f);
		gl2.glVertex2d(machine.limitLeft+5f, machine.limitTop+5f);
		gl2.glVertex2d(machine.limitLeft+5f, machine.limitTop);
		gl2.glVertex2d(machine.limitLeft   , machine.limitTop-5f);
		gl2.glVertex2d(machine.limitLeft-5f, machine.limitTop-5f);
		gl2.glEnd();
		gl2.glPopMatrix();

		// right frame
		gl2.glPushMatrix();
		gl2.glTranslatef(2.1f, 2.1f, 0);
		gl2.glBegin(GL2.GL_TRIANGLE_FAN);
		gl2.glVertex2d(machine.limitRight+5f, machine.limitTop+5f);
		gl2.glVertex2d(machine.limitRight-5f, machine.limitTop+5f);
		gl2.glVertex2d(machine.limitRight-5f, machine.limitTop);
		gl2.glVertex2d(machine.limitRight   , machine.limitTop-5f);
		gl2.glVertex2d(machine.limitRight+5f, machine.limitTop-5f);
		gl2.glEnd();
		gl2.glPopMatrix();

		// left motor
		gl2.glColor3f(0,0,0);
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glVertex2d(machine.limitLeft-4.2f, machine.limitTop+4.2f);
		gl2.glVertex2d(machine.limitLeft     , machine.limitTop+4.2f);
		gl2.glVertex2d(machine.limitLeft     , machine.limitTop);
		gl2.glVertex2d(machine.limitLeft-4.2f, machine.limitTop);
		// right motor
		gl2.glVertex2d(machine.limitRight     , machine.limitTop+4.2f);
		gl2.glVertex2d(machine.limitRight+4.2f, machine.limitTop+4.2f);
		gl2.glVertex2d(machine.limitRight+4.2f, machine.limitTop);
		gl2.glVertex2d(machine.limitRight     , machine.limitTop);
		gl2.glEnd();
	}


	private void paintGondolaAndCounterweights( GL2 gl2 ) {
		double dx,dy;
		double gx,gy;
		if(running) {
			// TODO test me!
			gx=gondolaX;
			gy=gondolaY;
		} else {
			gx=( gondolaX/cameraZoom+cameraOffsetX);
			gy=(-gondolaY/cameraZoom-cameraOffsetY);
		}

		double mw = machine.limitRight-machine.limitLeft;
		double mh = machine.limitTop-machine.limitBottom;
		double suggested_length = Math.sqrt(mw*mw+mh*mh)+5;

		dx = gx - machine.limitLeft;
		dy = gy - machine.limitTop;
		double left_a = Math.sqrt(dx*dx+dy*dy);
		double left_b = suggested_length - left_a;

		dx = gx - machine.limitRight;
		double right_a = Math.sqrt(dx*dx+dy*dy);
		double right_b = suggested_length - right_a;

		if(gx<machine.limitLeft) return;
		if(gx>machine.limitRight) return;
		if(gy>machine.limitTop) return;
		if(gy<machine.limitBottom) return;
		gl2.glBegin(GL2.GL_LINES);
		gl2.glColor3d(0.2,0.2,0.2);
		// motor to gondola left
		gl2.glVertex2d(machine.limitLeft, machine.limitTop);
		gl2.glVertex2d(gx,gy);
		// motor to counterweight left
		gl2.glVertex2d(machine.limitLeft-2.1-0.75, machine.limitTop);
		gl2.glVertex2d(machine.limitLeft-2.1-0.75, machine.limitTop-left_b);
		// motor to gondola right
		gl2.glVertex2d(machine.limitRight, machine.limitTop);
		gl2.glVertex2d(gx,gy);
		// motor to counterweight right
		gl2.glVertex2d(machine.limitRight+2.1+0.75, machine.limitTop);
		gl2.glVertex2d(machine.limitRight+2.1+0.75, machine.limitTop-right_b);
		gl2.glEnd();
		// gondola
		gl2.glBegin(GL2.GL_LINE_LOOP);
		gl2.glColor3f(0, 0, 1);
		float f;
		float r=2; // circle radius
		for(f=0;f<2.0*Math.PI;f+=0.3f) {
			gl2.glVertex2d(gx+Math.cos(f)*r,gy+Math.sin(f)*r);
		}
		gl2.glEnd();
		// counterweight left
		gl2.glBegin(GL2.GL_LINE_LOOP);
		gl2.glColor3f(0, 0, 1);
		gl2.glVertex2d(machine.limitLeft-2.1-0.75-1.5,machine.limitTop-left_b);
		gl2.glVertex2d(machine.limitLeft-2.1-0.75+1.5,machine.limitTop-left_b);
		gl2.glVertex2d(machine.limitLeft-2.1-0.75+1.5,machine.limitTop-left_b-15);
		gl2.glVertex2d(machine.limitLeft-2.1-0.75-1.5,machine.limitTop-left_b-15);
		gl2.glEnd();
		// counterweight right
		gl2.glBegin(GL2.GL_LINE_LOOP);
		gl2.glColor3f(0, 0, 1);
		gl2.glVertex2d(machine.limitRight+2.1+0.75-1.5,machine.limitTop-right_b);
		gl2.glVertex2d(machine.limitRight+2.1+0.75+1.5,machine.limitTop-right_b);
		gl2.glVertex2d(machine.limitRight+2.1+0.75+1.5,machine.limitTop-right_b-15);
		gl2.glVertex2d(machine.limitRight+2.1+0.75-1.5,machine.limitTop-right_b-15);
		gl2.glEnd();
		
		/*
		// bottom clearance arcs
		// right
		gl2.glColor3d(0.6, 0.6, 0.6);
		gl2.glBegin(GL2.GL_LINE_STRIP);
		double w = machine.limitRight - machine.limitLeft+2.1;
		double h = machine.limitTop - machine.limitBottom + 2.1;
		r=(float)Math.sqrt(h*h + w*w); // circle radius
		gx = machine.limitLeft - 2.1;
		gy = machine.limitTop + 2.1;
		double start = (float)1.5*(float)Math.PI;
		double end = (2*Math.PI-Math.atan(h/w));
		double v;
		for(v=0;v<=1.0;v+=0.1) {
			double vi = (end-start)*v + start;
			gl2.glVertex2d(gx+Math.cos(vi)*r,gy+Math.sin(vi)*r);
		}
		gl2.glEnd();
		
		// left
		gl2.glBegin(GL2.GL_LINE_STRIP);
		gx = machine.limitRight + 2.1;
		start = (float)(1*Math.PI+Math.atan(h/w));
		end = (float)1.5*(float)Math.PI;
		for(v=0;v<=1.0;v+=0.1) {
			double vi = (end-start)*v + start;
			gl2.glVertex2d(gx+Math.cos(vi)*r,gy+Math.sin(vi)*r);
		}
		gl2.glEnd();
		*/
	}


	public void render(GL2 gl2) {
		paintBackground(gl2);
		paintCamera(gl2);

		paintLimits(gl2);
		paintCenter(gl2);
		paintMotors(gl2);
		paintGondolaAndCounterweights(gl2);
		// TODO draw control box?

		if(drawDecorator!=null) {
			// filters can also draw WYSIWYG previews while converting.
			drawDecorator.render(gl2,machine);
			return;
		}

		paintGcode(gl2);
	}



	private void paintGcode( GL2 gl2 ) {
		// TODO move all robot drawing to a class
		optimizeNodes();

		DrawingTool tool = machine.getTool(0);
		gl2.glColor3f(0, 0, 0);

		// draw image
		if (fast_nodes.size() > 0) {
			// draw the nodes
			Iterator<DrawPanelNode> nodes = fast_nodes.iterator();
			while (nodes.hasNext()) {
				DrawPanelNode n = nodes.next();

				if (running) {
					if (n.line_number < linesProcessed) {
						gl2.glColor3f(1, 0, 0);
						//g2d.setColor(Color.RED);
						if(n.type==NodeType.POS) {
							gondolaX=n.x1;
							gondolaY=n.y1;
						}
					} else if (n.line_number <= linesProcessed + lookAhead) {
						gl2.glColor3f(0, 1, 0);
						//g2d.setColor(Color.GREEN);
					} else if (prefs.getBoolean("Draw all while running", true) == false) {
						break;
					}
				}

				switch (n.type) {
				case TOOL:
					tool = machine.getTool(n.tool_id);
					gl2.glLineWidth(tool.getDiameter() * (float) this.cameraZoom / 10.0f);
					break;
				case COLOR:
					if (!running || n.line_number > linesProcessed + lookAhead) {
						//g2d.setColor(n.c);
						gl2.glColor3f(n.c.getRed() / 255.0f, n.c.getGreen() / 255.0f, n.c.getBlue() / 255.0f);
					}
					break;
				default:
					tool.drawLine(gl2, n.x1, n.y1, n.x2, n.y2);
					break;
				}
			}
		}
	}

	private void addNodePos(int i, double x1, double y1, double x2, double y2) {
		DrawPanelNode n = new DrawPanelNode();
		n.line_number = i;
		n.x1 = x1;
		n.x2 = x2;
		n.y1 = y1;
		n.y2 = y2;
		n.type = NodeType.POS;
		fast_nodes.add(n);
	}

	private void addNodeColor(int i, Color c) {
		DrawPanelNode n = new DrawPanelNode();
		n.line_number = i;
		n.c = c;
		n.type = NodeType.COLOR;
		fast_nodes.add(n);
	}

	private void addNodeTool(int i, int tool_id) {
		DrawPanelNode n = new DrawPanelNode();
		n.line_number = i;
		n.tool_id = tool_id;
		n.type = NodeType.TOOL;
		fast_nodes.add(n);

	}

	private void optimizeNodes() {
		if (instructions == null) return;
		if (instructions.changed == false) return;
		instructions.changed = false;

		emptyNodeBuffer();

		DrawingTool tool = machine.getTool(0);

		drawScale = 0.1f;

		float px = 0, py = 0, pz = 90;
		//float oldz=pz;
		float x, y, z, ai, aj;
		int i, j;
		boolean absMode = true;
		String tool_change = "M06 T";
		Color tool_color = Color.BLACK;

		Iterator<String> commands = instructions.getLines().iterator();
		i = 0;
		while (commands.hasNext()) {
			String line = commands.next();
			++i;
			String[] pieces = line.split(";");
			if (pieces.length == 0) continue;

			if (line.startsWith(tool_change)) {
				String numberOnly = line.substring(tool_change.length()).replaceAll("[^0-9]", "");
				int id = (int) Integer.valueOf(numberOnly, 10);
				addNodeTool(i, id);
				switch (id) {
				case 1:
					tool_color = Color.RED;
					break;
				case 2:
					tool_color = Color.GREEN;
					break;
				case 3:
					tool_color = Color.BLUE;
					break;
				default:
					tool_color = Color.BLACK;
					break;
				}
				continue;
			}

			String[] tokens = pieces[0].split("\\s");
			if (tokens.length == 0) continue;

			// have we changed scale?
			// what are our coordinates?
			x = px;
			y = py;
			z = pz;
			ai = px;
			aj = py;
			for (j = 0; j < tokens.length; ++j) {
				if (tokens[j].equals("G20")) drawScale = 2.54f; // in->cm
				else if (tokens[j].equals("G21")) drawScale = 0.10f; // mm->cm
				else if (tokens[j].equals("G90")) {
					absMode = true;
					break;
				} else if (tokens[j].equals("G91")) {
					absMode = false;
					break;
				} else if (tokens[j].equals("G54")) break;
				else if (tokens[j].startsWith("X")) {
					float tx = Float.valueOf(tokens[j].substring(1)) * drawScale;
					x = absMode ? tx : x + tx;
				} else if (tokens[j].startsWith("Y")) {
					float ty = Float.valueOf(tokens[j].substring(1)) * drawScale;
					y = absMode ? ty : y + ty;
				} else if (tokens[j].startsWith("Z")) {
					float tz = z = Float.valueOf(tokens[j].substring(1));// * drawScale;
					z = absMode ? tz : z + tz;
				}
				if (tokens[j].startsWith("I")) ai = Float.valueOf(tokens[j].substring(1)) * drawScale;
				if (tokens[j].startsWith("J")) aj = Float.valueOf(tokens[j].substring(1)) * drawScale;
			}
			if (j < tokens.length) continue;
			//*
			// is pen up or down?
			//if(oldz!=z)
			{
				//oldz=z;
				tool.drawZ(z);
				if (tool.isDrawOff()) {
					if (showPenUpMoves == false) {
						px = x;
						py = y;
						pz = z;
						continue;
					}
					addNodeColor(i, Color.BLUE);
				} else if (tool.isDrawOn()) {
					addNodeColor(i, tool_color);  // TODO use actual pen color
				} else {
					addNodeColor(i, Color.ORANGE);
				}
			}

			// what kind of motion are we going to make?
			if (tokens[0].equals("G00") || tokens[0].equals("G0") ||
					tokens[0].equals("G01") || tokens[0].equals("G1")) {
				//if(z==pz)
				{
					addNodePos(i, px, py, x, y);
				}
			} else if (tokens[0].equals("G02") || tokens[0].equals("G2") ||
					tokens[0].equals("G03") || tokens[0].equals("G3")) {
				// draw an arc

				// clockwise or counter-clockwise?
				int dir = (tokens[0].equals("G02") || tokens[0].equals("G2")) ? -1 : 1;

				double dx = px - ai;
				double dy = py - aj;
				double radius = Math.sqrt(dx * dx + dy * dy);

				// find angle of arc (sweep)
				double angle1 = atan3(dy, dx);
				double angle2 = atan3(y - aj, x - ai);
				double theta = angle2 - angle1;

				if (dir > 0 && theta < 0) angle2 += Math.PI * 2.0;
				else if (dir < 0 && theta > 0) angle1 += Math.PI * 2.0;

				theta = angle2 - angle1;

				double len = Math.abs(theta) * radius;
				double segments = len * STEPS_PER_DEGREE * 2.0;
				double nx, ny, angle3, scale;

				// Draw the arc from a lot of little line segments.
				for (int k = 0; k < segments; ++k) {
					scale = (double) k / segments;
					angle3 = theta * scale + angle1;
					nx = ai + Math.cos(angle3) * radius;
					ny = aj + Math.sin(angle3) * radius;

					addNodePos(i, px, py, nx, ny);
					px = (float) nx;
					py = (float) ny;
				}
				addNodePos(i, px, py, x, y);
			}

			px = x;
			py = y;
			pz = z;
		}  // for ( each instruction )
	}
}


/**
 * This file is part of DrawbotGUI.
 * <p>
 * DrawbotGUI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * DrawbotGUI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with DrawbotGUI.  If not, see <http://www.gnu.org/licenses/>.
 */
