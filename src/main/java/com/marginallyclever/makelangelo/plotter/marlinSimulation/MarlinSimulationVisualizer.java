package com.marginallyclever.makelangelo.plotter.marlinSimulation;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.makelangelo.plotter.settings.PlotterSettings;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleMove;
import com.marginallyclever.makelangelo.turtle.turtleRenderer.TurtleRenderer;

import javax.vecmath.Vector3d;
import java.util.ArrayList;

/**
 * {@link MarlinSimulationVisualizer} uses OpenGL to render the behavior of a Marlin-based 3D printer as it processes gcode instructions.
 * It can render in three modes:
 * 0, Acceleration and Deceleration
 * 1, minimum segment length highlighting
 * 2, alternating block colors (aka 'candy cane')
 * @author Dan Royer
 * @since 7.24.0
 *
 */
public class MarlinSimulationVisualizer implements TurtleRenderer {
	//private Turtle previousTurtle=null;
	private GL2 gl2;
	private Turtle myTurtle = new Turtle();
	private PlotterSettings mySettings;
	
	private int renderMode = 0;
	private boolean useDistance=true;
	private boolean showNominal=false;
	private boolean showEntry=false;
	private boolean showExit=true;

	private class ColorPoint {
		public Vector3d c;
		public Vector3d p;
		
		public ColorPoint(Vector3d cc, Vector3d pp) {
			c=cc;
			p=pp;
		}
	};
	
	private ArrayList<ColorPoint> buffer = new ArrayList<ColorPoint>();
	
	public MarlinSimulationVisualizer() {}
	
	private void drawBufferedTurtle(GL2 gl2) {
		gl2.glPushMatrix();
		gl2.glLineWidth(2);
		gl2.glBegin(GL2.GL_LINE_STRIP);

		for( ColorPoint a : buffer ) {
			gl2.glColor3d(a.c.x, a.c.y, a.c.z);
			gl2.glVertex2d(a.p.x, a.p.y);
		}
		
		gl2.glEnd();
		gl2.glPopMatrix();
	}

	private void recalculateBuffer(Turtle turtleToRender, final PlotterSettings settings) {
		buffer.clear();

		showNominal=false;
		showEntry=false;
		showExit=false;
		
		MarlinSimulation m = new MarlinSimulation(settings);
		m.historyAction(turtleToRender, (block)->{
			switch(renderMode) {
			case 0: renderAccelDecel(block,settings); break;
			case 1: renderMinLength(block); break;
			case 2: renderAlternatingBlocks(block);  break;
			}
		});
	}

	private void renderAlternatingBlocks(MarlinSimulationBlock block) {
		Vector3d c;
		switch(block.id % 3) {
		case 0: c=new Vector3d(1,0,0); break;
		case 1: c=new Vector3d(0,1,0); break;
		default: c=new Vector3d(0,0,1); break;
		}
		buffer.add(new ColorPoint(c,block.start));
		buffer.add(new ColorPoint(c,block.end));
	}

	private void renderMinLength(MarlinSimulationBlock block) {
		double d = block.distance / (MarlinSimulation.MIN_SEGMENT_LENGTH_MM*2.0);
		d = Math.max(Math.min(d, 1), 0);
		double g = d;
		double r = 1-d;
		buffer.add(new ColorPoint(new Vector3d(r,g,0),block.start));
		buffer.add(new ColorPoint(new Vector3d(r,g,0),block.end));
	}
	
	private void renderAccelDecel(MarlinSimulationBlock block,PlotterSettings settings) {
		double t,a,d;
		if(useDistance) {
			t = block.distance;
			a = block.accelerateUntilD;
			d = block.decelerateAfterD;
		} else {
			// use time
			t = block.end_s;
			a = block.accelerateUntilT;
			d = block.decelerateAfterT;
		}
		//if(d>t) d=t;

		//if(--limit<=0) return;
		//if(limit<20) block.report();
		// nominal vs entry speed

		Vector3d ortho = new Vector3d();
		if(showNominal || showEntry || showExit) {
			ortho = new Vector3d(-block.normal.y,block.normal.x,0);
			ortho.scale(150);
		}
		
		if(showNominal) {
			Vector3d o = new Vector3d(ortho);
			double f = block.nominalSpeed / settings.getDrawFeedRate();
			o.scale(f);
			o.add(block.start);
			Vector3d black = new Vector3d(1-f,f,0);
			buffer.add(new ColorPoint(black,block.start));
			buffer.add(new ColorPoint(black,o));
			buffer.add(new ColorPoint(black,block.start));
		}
		if(showEntry) {
			Vector3d o = new Vector3d(ortho);
			double f = block.entrySpeed / settings.getDrawFeedRate();
			o.scale(f);
			o.add(block.start);
			Vector3d red = new Vector3d(1-f,0,f);
			buffer.add(new ColorPoint(red,block.start));
			buffer.add(new ColorPoint(red,o));
			buffer.add(new ColorPoint(red,block.start));
		}
		if(showExit) {
			Vector3d o = new Vector3d(ortho);
			double f = block.exitSpeed / settings.getDrawFeedRate();
			o.scale(f);
			o.add(block.start);
			Vector3d black = new Vector3d(0,1-f,f);
			buffer.add(new ColorPoint(black,block.start));
			buffer.add(new ColorPoint(black,o));
			buffer.add(new ColorPoint(black,block.start));
		}

		double v = 1;
		if(a>0) {
			v = block.entrySpeed / block.nominalSpeed;
			// accel part of block
			Vector3d p0 = new Vector3d(block.delta);
			p0.scale(a/t);
			p0.add(block.start);
			Vector3d green = new Vector3d(0,1.0-v,v);
			buffer.add(new ColorPoint(green,block.start));
			buffer.add(new ColorPoint(green,p0));
		}
		if(a<d) {
			v=1;
			// nominal part of block
			Vector3d p1 = new Vector3d(block.delta);
			p1.scale(d/t);
			p1.add(block.start);
			Vector3d blue = new Vector3d(0,0,v);
			//buffer.add(new ColorPoint(blue,pLast));
			buffer.add(new ColorPoint(blue,p1));
		}
		if(d<t) {
			// decel part of block
			v = block.exitSpeed / block.nominalSpeed;
			Vector3d red = new Vector3d(1.0-v,0,v);
			//buffer.add(new ColorPoint(red,pLast));
			buffer.add(new ColorPoint(red,block.end));
		}
	}

	
	@Override
	public void start(GL2 gl2) {
		this.gl2 = gl2;
		myTurtle.history.clear();
	}

	@Override
	public void draw(TurtleMove p0, TurtleMove p1) {
		myTurtle.history.add(p1);
		
	}

	@Override
	public void travel(TurtleMove p0, TurtleMove p1) {
		myTurtle.history.add(p1);
	}

	@Override
	public void end() {
		//if(previousTurtle!=myTurtle || previousTurtle.history.size() != myTurtle.history.size()) {
			recalculateBuffer(myTurtle,mySettings);
			//previousTurtle = myTurtle;
		//}
		
		drawBufferedTurtle(gl2);
	}

	@Override
	public void setPenDownColor(ColorRGB color) {
		myTurtle.history.add(new TurtleMove(color.toInt(),0,TurtleMove.TOOL_CHANGE));
	}

	@Override
	public void setPenDiameter(double d) {
		
	}
	
	public void setSettings(PlotterSettings e) {
		mySettings = e;
	}
}
