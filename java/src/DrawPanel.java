import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;


	// Custom drawing panel written as an inner class to access the instance variables.
public class DrawPanel extends JPanel implements MouseListener, MouseInputListener  {
	static final long serialVersionUID=2;

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
	int cx,cy;
	double cameraOffsetX=0,cameraOffsetY=0;
	double cameraZoom=20;
	float drawScale=0.1f;
	final float extraScale=100;

	ArrayList<String> instructions;
	
	
	public DrawPanel() {
		super();
        addMouseMotionListener(this);
        addMouseListener(this);
	}
	
	
	public void setGCode(ArrayList<String> gcode) {
		instructions = gcode;
		// process the image into a buffer once rather than re-reading the gcode over and over again?
	    repaint();
	}
	
	
	public void updateMachineConfig() {
		repaint();
	}
	
	
	public void setShowPenUp(boolean state) {
		show_pen_up=state;
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
	}
	
	// returns angle of dy/dx as a value from 0...2PI
	private double atan3(double dy,double dx) {
	  double a=Math.atan2(dy,dx);
	  if(a<0) a=(Math.PI*2.0)+a;
	  return a;
	}


	private void MoveCamera(int x,int y) {
		// scroll the gcode preview
		double dx=(x-oldx);
		double dy=(y-oldy);
    	cameraOffsetX-=dx;
    	cameraOffsetY-=dy;
	}
	private void ZoomCamera(int x,int y) {
		double amnt = (double)(y-oldy)*0.01;
		cameraZoom += amnt;
		if(cameraZoom<0.1) cameraZoom=0.1f;
	}
	public void ZoomIn() {
		cameraZoom*=4.0/3.0;
    	repaint();
	}
	public void ZoomOut() {
		cameraZoom*=3.0/4.0;
    	repaint();
	}
	public void ZoomToFitPaper() {
		MachineConfiguration mc=MachineConfiguration.getSingleton();
		
		float w=(float)this.getWidth();
		float h=(float)this.getHeight();
		// which one do we have to zoom more to fit the picture in the component?
		float wzoom=w/(float)(mc.paper_right-mc.paper_left);
		float hzoom=h/(float)(mc.paper_top-mc.paper_bottom);
		cameraZoom = (wzoom < hzoom ? wzoom : hzoom) / extraScale;
		cameraOffsetX=-getWidth()/2;
		cameraOffsetY=-getHeight()/2;
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

    
    private double TX(double a) {
    	return cx+(a*extraScale);
    }
    private double TY(double a) {
    	return cy-(a*extraScale);
    }
    private double ITX(double a) {
    	return TX(a); // TX(a*imageScale-imageOffsetX);
    }
    private double ITY(double a) {
    	return TY(a); // TY(a*imageScale-imageOffsetY);
    }
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);    // paint background
		Graphics2D g2d = (Graphics2D)g;

		MachineConfiguration mc = MachineConfiguration.getSingleton();
		
		DrawingTool tool = mc.GetTool(0);

		cx = this.getWidth()/2;
		cy = this.getHeight()/2;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.translate(-cameraOffsetX, -cameraOffsetY);
		g2d.scale(cameraZoom, cameraZoom);
		
		// draw background
		setBackground(Color.GRAY);

		// draw limits
		if(!connected) {
			g2d.setColor(new Color(194.0f/255.0f,133.0f/255.0f,71.0f/255.0f));
			g2d.drawRect((int)TX(mc.limit_left),(int)TY(mc.limit_top),
					(int)((mc.limit_right-mc.limit_left)*extraScale),
					(int)((mc.limit_top-mc.limit_bottom)*extraScale));
			g2d.setColor(Color.WHITE);
			g2d.fillRect((int)TX(mc.paper_left),(int)TY(mc.paper_top),
					(int)((mc.paper_right-mc.paper_left)*extraScale),
					(int)((mc.paper_top-mc.paper_bottom)*extraScale));
		} else {
			g2d.setColor(new Color(194.0f/255.0f,133.0f/255.0f,71.0f/255.0f));
			g2d.fillRect((int)TX(mc.limit_left),(int)TY(mc.limit_top),
					(int)((mc.limit_right-mc.limit_left)*extraScale),
					(int)((mc.limit_top-mc.limit_bottom)*extraScale));
			g2d.setColor(Color.WHITE);
			g2d.fillRect((int)TX(mc.paper_left),(int)TY(mc.paper_top),
					(int)((mc.paper_right-mc.paper_left)*extraScale),
					(int)((mc.paper_top-mc.paper_bottom)*extraScale));

		}

		// draw calibration point
		g2d.setColor(Color.RED);
		g2d.drawLine((int)TX(-0.25),(int)TY( 0.00), (int)TX(0.25),(int)TY(0.00));
		g2d.drawLine((int)TX( 0.00),(int)TY(-0.25), (int)TX(0.00),(int)TY(0.25));
		
		// TODO draw left motor
		// TODO draw right motor

		// draw image
		if(instructions==null) return;
		
		drawScale=0.1f;
		
		float px=0,py=0,pz=90;
		float x,y,z,ai,aj;
		int i,j;
		boolean absMode=true;
		String tool_change="M06 T";
		
		pz=0.5f;
		
		for(i=0;i<instructions.size();++i) {
			String line=instructions.get(i);
			String[] pieces=line.split(";");
			if(pieces.length==0) continue;

			if(line.startsWith(tool_change)) {
				String numberOnly= line.substring(tool_change.length()).replaceAll("[^0-9]", "");
				int tool_id = (int)Integer.valueOf(numberOnly, 10);

				tool = MachineConfiguration.getSingleton().GetTool(tool_id);
				g2d.setStroke(tool.getStroke());
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
				else if(tokens[j].equals("G90")) absMode=true;
				else if(tokens[j].equals("G91")) absMode=false;
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
				g2d.setColor( Color.BLUE );
			} else if(tool.DrawIsOn()) {
				g2d.setColor( Color.BLACK );
			} else {
				g2d.setColor( Color.ORANGE );
			}
			
			if(tool.DrawIsOn()) {
				if(running && i<=linesProcessed) {
					g2d.setColor( Color.RED );
				} else if(running && i>linesProcessed && i<=linesProcessed+500) {
					g2d.setColor( Color.GREEN );
				}
			}
			/*/
			// for testing gcodesender generated pictures
			if(z<0.1) {
				g2d.setColor(Color.RED);
			} else {
				px=x;
				py=y;
				pz=z;
				continue;
			}*/
			
			// what kind of motion are we going to make?
			if(tokens[0].equals("G00") || tokens[0].equals("G0") ||
			   tokens[0].equals("G01") || tokens[0].equals("G1")) {
				tool.DrawLine(g2d,ITX(px),ITY(py),ITX(x),ITY(y));	
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

				theta=Math.abs(angle2-angle1)*RAD2DEG*STEPS_PER_DEGREE;

				// Draw the arc from a lot of little line segments.
				for(int k=0;k<=theta;++k) {
					double angle3 = (angle2-angle1) * ((double)k/theta) + angle1;
					float nx = (float)(ai + Math.cos(angle3) * radius);
				    float ny = (float)(aj + Math.sin(angle3) * radius);

					tool.DrawLine(g2d,ITX(px),ITY(py),ITX(nx),ITY(ny));
					px=nx;
					py=ny;
				}
				tool.DrawLine(g2d,ITX(px),ITY(py),ITX(x),ITY(y));	
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