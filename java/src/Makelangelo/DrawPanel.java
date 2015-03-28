package Makelangelo;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import javax.swing.event.MouseInputListener;

import DrawingTools.DrawingTool;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLPipelineFactory;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLJPanel;

	// Custom drawing panel written as an inner class to access the instance variables.
public class DrawPanel extends GLJPanel implements MouseListener, MouseInputListener, GLEventListener  {
	static final long serialVersionUID=2;

	private Preferences prefs = Preferences.userRoot().node("DrawBot").node("Graphics");
	
	// arc smoothness - increase to make more smooth and run slower.
	public static final double STEPS_PER_DEGREE=1;
	public static final double RAD2DEG = 180.0/Math.PI;
	public static final double DEG2RAD = Math.PI/180.0;

	// progress
	long linesProcessed=0;
	boolean connected=false;
	boolean running=false;

	// config
	boolean show_pen_up=false;
	
	// motion control
	boolean mouseIn=false;
	int buttonPressed=MouseEvent.NOBUTTON;
	int oldx, oldy;

	// scale + position
	double cameraOffsetX=0,cameraOffsetY=0;
	double cameraZoom=20;
	float drawScale=0.1f;
	final float extraScale=1;
	int window_width=0;
	int window_height=0;
	float window_aspect_ratio = 1f;

	ArrayList<String> instructions;

	public enum NodeType { COLOR, POS, TOOL };
	
	// optimization attempt
	class DrawPanelNode {
		double x1,y1,x2,y2;
		Color c;
		int tool_id;
		int line_number;
		NodeType type;
	}
	ArrayList<DrawPanelNode> fast_nodes = new ArrayList<DrawPanelNode>();
	BasicStroke fast_stroke;
	
	
	public DrawPanel() {
		super();
        addMouseMotionListener(this);
        addMouseListener(this);
        addGLEventListener(this);
	}
	

    @Override
    public void reshape( GLAutoDrawable glautodrawable, int x, int y, int width, int height ) {
    	GL2 gl2 = glautodrawable.getGL().getGL2();
        gl2.setSwapInterval(1);

        window_width=width;
        window_height=height;
        window_aspect_ratio = window_width / window_height;
    }
    
    @Override
    public void init( GLAutoDrawable drawable ) {
    	// Use debug pipeline
    	boolean glDebug=true;
    	boolean glTrace=false;
    	
        GL gl = drawable.getGL();
        
        if(glDebug) {
            try {
                // Debug ..
                gl = gl.getContext().setGL( GLPipelineFactory.create("javax.media.opengl.Debug", null, gl, null) );
            } catch (Exception e) {e.printStackTrace();}
        }

        if(glTrace) {
            try {
                // Trace ..
                gl = gl.getContext().setGL( GLPipelineFactory.create("javax.media.opengl.Trace", null, gl, new Object[] { System.err } ) );
            } catch (Exception e) {e.printStackTrace();}
        }
    }
    
    
    @Override
    public void dispose( GLAutoDrawable glautodrawable ) {
    }
    
    
    @Override
    public void display( GLAutoDrawable glautodrawable ) {
        //long now_time = System.currentTimeMillis();
        //float dt = (now_time - last_time)*0.001f;
    	//last_time = now_time;
    	//System.out.println(dt);
    	
		// Clear The Screen And The Depth Buffer
    	GL2 gl2 = glautodrawable.getGL().getGL2();

        // draw the world
        render( gl2 );
    }
	
    
    
	public void setGCode(ArrayList<String> gcode) {
		instructions = gcode;
		emptyNodeBuffer();
		// process the image into a buffer once rather than re-reading the gcode over and over again?
	    repaint();
	}
	
	public void emptyNodeBuffer() {
		fast_nodes.clear();
		OptimizeNodes();
	}
	
	
	public void updateMachineConfig() {
		repaint();
	}
	
	
	public void setShowPenUp(boolean state) {
		show_pen_up=state;
		emptyNodeBuffer();
		repaint();
	}
	
	public boolean getShowPenUp() {
		return show_pen_up;
	}
	
	
	public void setLinesProcessed(long c) {
		linesProcessed=c;
		if((linesProcessed%10)==0) repaint();
	}
	
	public void setConnected(boolean state) {
		connected=state;
		repaint();
	}
	
	public void setRunning(boolean state) {
		running=state;
		if(running==false) {
			linesProcessed=0;
		}
	}
	
	// returns angle of dy/dx as a value from 0...2PI
	private double atan3(double dy,double dx) {
	  double a=Math.atan2(dy,dx);
	  if(a<0) a=(Math.PI*2.0)+a;
	  return a;
	}


	private void MoveCamera(int x,int y) {
    	cameraOffsetX+=oldx-x;
    	cameraOffsetY+=oldy-y;
	}
	
	private void ZoomCamera(int x,int y) {
		double amnt = (double)(y-oldy)*0.01;
		cameraZoom += amnt;
		if(cameraZoom<0.1) cameraZoom=0.1f;
	}
	
	public void ZoomIn() {
		cameraZoom*=4.0/3.0;
		cameraOffsetX*=4.0/3.0;
		cameraOffsetY*=4.0/3.0;
    	repaint();
	}
	public void ZoomOut() {
		cameraZoom*=3.0/4.0;
		cameraOffsetX*=3.0/4.0;
		cameraOffsetY*=3.0/4.0;
    	repaint();
	}
	
	public void ZoomToFitPaper() {
		MachineConfiguration mc=MachineConfiguration.getSingleton();
		
		float w=(float)this.getWidth();
		float h=(float)this.getHeight();
		// which one do we have to zoom more to fit the picture in the component?
		float wzoom=w/(float)(mc.paper_right-mc.paper_left);
		float hzoom=h/(float)(mc.paper_top-mc.paper_bottom);
		cameraZoom = (wzoom < hzoom ? wzoom : hzoom);
		cameraOffsetX=0;
		cameraOffsetY=0;
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
    		MoveCamera(x,y);
    	} else if(buttonPressed==MouseEvent.BUTTON3) {
    		ZoomCamera(x,y);
    	}
    	oldx=x;
    	oldy=y;
    	repaint();
    }
    public void mouseMoved(MouseEvent e) {}

    
    // scale and translate the output
    private void paintCamera(GL2 gl2) {
		gl2.glMatrixMode(GL2.GL_PROJECTION);
		gl2.glLoadIdentity();
		gl2.glOrtho(0, window_width, 0, window_height, 1, -1);
		//GLU glu = new GLU();
        //glu.gluPerspective(60, window_aspect_ratio, 1.0f, 1000.0f);
        gl2.glMatrixMode(GL2.GL_MODELVIEW);
		gl2.glLoadIdentity();
		
		
		//if(prefs.getBoolean("antialias", true)) g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		//if(prefs.getBoolean("speed over quality", true)) g2d.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_SPEED);
		gl2.glTranslated(-cameraOffsetX+window_width/2.0f, cameraOffsetY+window_height/2.0f,0);
		gl2.glScaled(cameraZoom,cameraZoom,1);
    }
  
    // clear the panel
    private void paintBackground( GL2 gl2 ) {
    	gl2.glClearColor(0.5f,0.5f,0.5f, 0);
    	
        // Special handling for the case where the GLJPanel is translucent
        // and wants to be composited with other Java 2D content
        if (GLProfile.isAWTAvailable() &&
            (this instanceof javax.media.opengl.awt.GLJPanel) &&
            !((javax.media.opengl.awt.GLJPanel) this).isOpaque() &&
            ((javax.media.opengl.awt.GLJPanel) this).shouldPreserveColorBufferIfTranslucent()) {
          gl2.glClear(GL2.GL_DEPTH_BUFFER_BIT);
        } else {
          gl2.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        }
    }
  
    // draw the machine edges and paper edges
    private void paintLimits(GL2 gl2,MachineConfiguration mc) {
		if(!connected) {
			gl2.glColor3f(194.0f/255.0f,133.0f/255.0f,71.0f/255.0f);
			gl2.glBegin(GL2.GL_LINE_LOOP);
			gl2.glVertex2d(mc.limit_left, mc.limit_top);
			gl2.glVertex2d(mc.limit_right, mc.limit_top);
			gl2.glVertex2d(mc.limit_right, mc.limit_bottom);
			gl2.glVertex2d(mc.limit_left, mc.limit_bottom);
			gl2.glEnd();
			gl2.glColor3f(1,1,1);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
			gl2.glVertex2d(mc.paper_left, mc.paper_top);
			gl2.glVertex2d(mc.paper_right, mc.paper_top);
			gl2.glVertex2d(mc.paper_right, mc.paper_bottom);
			gl2.glVertex2d(mc.paper_left, mc.paper_bottom);
			gl2.glEnd();
		} else {
			gl2.glColor3f(194.0f/255.0f,133.0f/255.0f,71.0f/255.0f);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
			gl2.glVertex2d(mc.limit_left, mc.limit_top);
			gl2.glVertex2d(mc.limit_right, mc.limit_top);
			gl2.glVertex2d(mc.limit_right, mc.limit_bottom);
			gl2.glVertex2d(mc.limit_left, mc.limit_bottom);
			gl2.glEnd();
			gl2.glColor3f(1,1,1);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
			gl2.glVertex2d(mc.paper_left, mc.paper_top);
			gl2.glVertex2d(mc.paper_right, mc.paper_top);
			gl2.glVertex2d(mc.paper_right, mc.paper_bottom);
			gl2.glVertex2d(mc.paper_left, mc.paper_bottom);
			gl2.glEnd();
		}
    }
    
	// draw calibration point
    private void paintCenter(GL2 gl2) {
    	gl2.glColor3f(1,0,0);
    	gl2.glBegin(GL2.GL_LINES);
    	gl2.glVertex2f(-0.25f,0.0f);
    	gl2.glVertex2f( 0.25f,0.0f);
    	gl2.glVertex2f(0.0f,-0.25f);
    	gl2.glVertex2f(0.0f, 0.25f);
    	gl2.glEnd();
    }
    
	
	public void render( GL2 gl2 ) {
		MachineConfiguration mc = MachineConfiguration.getSingleton();
		DrawingTool tool = mc.GetTool(0);
		
		paintBackground(gl2);
		paintCamera(gl2);
		paintLimits(gl2,mc);
		paintCenter(gl2);
		
		// TODO draw left motor
		// TODO draw right motor
		// TODO draw control box

		final int look_ahead=500;

		gl2.glColor3f(0, 0, 0);

		// draw image		
		if(fast_nodes.size()>0) {
			// draw the nodes
			for(int i=0;i<fast_nodes.size();++i) {
				DrawPanelNode n=fast_nodes.get(i);

				if(running) {
					if(n.line_number<=linesProcessed) {
						gl2.glColor3f(1, 0, 0);
						//g2d.setColor(Color.RED);
					} else if(n.line_number<=linesProcessed+look_ahead) {
						gl2.glColor3f(0, 1, 0);
						//g2d.setColor(Color.GREEN);
					} else if(prefs.getBoolean("Draw all while running", true) == false) {
						break;
					}
				}
				
				switch(n.type) {
				case TOOL:
					tool = MachineConfiguration.getSingleton().GetTool(n.tool_id);
					//g2d.setStroke(tool.getStroke());
					gl2.glLineWidth(tool.GetDiameter());
					break;
				case COLOR:
					if(!running || n.line_number>linesProcessed+look_ahead) {
						//g2d.setColor(n.c);
						gl2.glColor3f(n.c.getRed()/255.0f,n.c.getGreen()/255.0f,n.c.getBlue()/255.0f);
					}
					break;
				default:
					tool.DrawLine(gl2, n.x1, n.y1, n.x2, n.y2);
					break;
				}
			}
		}
	}
	
	private void addNodePos(int i,double x1,double y1,double x2,double y2) {
		DrawPanelNode n = new DrawPanelNode();
		n.line_number=i;
		n.x1=x1;
		n.x2=x2;
		n.y1=y1;
		n.y2=y2;
		n.type=NodeType.POS;
		fast_nodes.add(n);
	}
	
	private void addNodeColor(int i,Color c) {
		DrawPanelNode n = new DrawPanelNode();
		n.line_number=i;
		n.c=c;
		n.type=NodeType.COLOR;
		fast_nodes.add(n);
	}
	
	private void addNodeTool(int i,int tool_id) {
		DrawPanelNode n = new DrawPanelNode();
		n.line_number=i;
		n.tool_id=tool_id;
		n.type=NodeType.TOOL;
		fast_nodes.add(n);
		
	}

	private void OptimizeNodes() {
		if(instructions == null) return;
		
		MachineConfiguration mc = MachineConfiguration.getSingleton();
		DrawingTool tool = mc.GetTool(0);
		
		drawScale=0.1f;
		
		float px=0,py=0,pz=90;
		float x,y,z,ai,aj;
		int i,j;
		boolean absMode=true;
		String tool_change="M06 T";
		Color tool_color=Color.BLACK;
		
		pz=0.5f;
		
		for(i=0;i<instructions.size();++i) {
			String line=instructions.get(i);
			String[] pieces=line.split(";");
			if(pieces.length==0) continue;

			if(line.startsWith(tool_change)) {
				String numberOnly= line.substring(tool_change.length()).replaceAll("[^0-9]", "");
				int id = (int)Integer.valueOf(numberOnly, 10);
				addNodeTool(i,id);
				switch(id) {
				case 1: tool_color = Color.RED; break;
				case 2: tool_color = Color.GREEN; break;
				case 3: tool_color = Color.BLUE; break;
				default: tool_color = Color.BLACK; break;
				}
				continue;
			}
			
			String[] tokens = pieces[0].split("\\s");
			if(tokens.length==0) continue;
			
			// have we changed scale?
			// what are our coordinates?
			x=px;
			y=py;
			z=pz;
			ai=px;
			aj=py;
			for(j=0;j<tokens.length;++j) {
				if(tokens[j].equals("G20")) drawScale=2.54f; // in->cm
				else if(tokens[j].equals("G21")) drawScale=0.10f; // mm->cm
				else if(tokens[j].equals("G90")) {
					absMode=true;
					break;
				} else if(tokens[j].equals("G91")) {
					absMode=false;
					break;
				}
				else if(tokens[j].equals("G54")) break;
				else if(tokens[j].startsWith("X")) {
					float tx = Float.valueOf(tokens[j].substring(1)) * drawScale;
					x = absMode ? tx : x + tx; 
				}
				else if(tokens[j].startsWith("Y")) {
					float ty = Float.valueOf(tokens[j].substring(1)) * drawScale;
					y = absMode ? ty : y + ty; 
				}
				else if(tokens[j].startsWith("Z")) {
					float tz = z = Float.valueOf(tokens[j].substring(1));// * drawScale;
					z =  absMode ? tz : z + tz; 
				}
				if(tokens[j].startsWith("I")) ai = Float.valueOf(tokens[j].substring(1)) * drawScale;
				if(tokens[j].startsWith("J")) aj = Float.valueOf(tokens[j].substring(1)) * drawScale;
			}
			if(j<tokens.length) continue;
			//*
			// is pen up or down?
			tool.DrawZ(z);
			if(tool.DrawIsOff()) {
				if(show_pen_up==false) {
					px=x;
					py=y;
					pz=z;
					continue;
				}
				addNodeColor(i, Color.BLUE );
			} else if(tool.DrawIsOn()) {
				addNodeColor(i, tool_color );  // TODO use actual pen color
			} else {
				addNodeColor(i, Color.ORANGE );
			}
			
			// what kind of motion are we going to make?
			if(tokens[0].equals("G00") || tokens[0].equals("G0") ||
			   tokens[0].equals("G01") || tokens[0].equals("G1")) {
				//if(z==pz)
				{
					addNodePos(i,px,py,x,y);	
				}
			} else if(tokens[0].equals("G02") || tokens[0].equals("G2") ||
					  tokens[0].equals("G03") || tokens[0].equals("G3")) {
				// draw an arc
				
				// clockwise or counter-clockwise?
				int dir = (tokens[0].equals("G02") || tokens[0].equals("G2")) ? -1 : 1;

				double dx=px - ai;
				double dy=py - aj;
				double radius=Math.sqrt(dx*dx+dy*dy);

				// find angle of arc (sweep)
				double angle1=atan3(dy,dx);
				double angle2=atan3(y-aj,x-ai);
				double theta=angle2-angle1;

				if(dir>0 && theta<0) angle2+=Math.PI*2.0;
				else if(dir<0 && theta>0) angle1+=Math.PI*2.0;

				theta=angle2-angle1;

				double len = Math.abs(theta) * radius;
				double segments = len * STEPS_PER_DEGREE*2;
				double nx,ny,angle3,scale;
				
				// Draw the arc from a lot of little line segments.
				for(int k=0;k<segments;++k) {
					scale = (double)k / segments;
					angle3 = theta * scale + angle1;
					nx = ai + Math.cos(angle3) * radius;
				    ny = aj + Math.sin(angle3) * radius;

					addNodePos(i,px,py,nx,ny);
					px=(float)nx;
					py=(float)ny;
				}
				addNodePos(i,px,py,x,y);	
			}
			
			px=x;
			py=y;
			pz=z;
		}  // for ( each instruction )
	}
}



/**
 * This file is part of DrawbotGUI.
 *
 * DrawbotGUI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * DrawbotGUI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */