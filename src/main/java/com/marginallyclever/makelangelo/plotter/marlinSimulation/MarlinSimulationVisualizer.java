package com.marginallyclever.makelangelo.plotter.marlinSimulation;

import com.jogamp.opengl.GL2;
import com.marginallyclever.makelangelo.plotter.settings.PlotterSettings;
import com.marginallyclever.makelangelo.turtle.Turtle;

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
public class MarlinSimulationVisualizer {
	//private static int limit;
	
	private Turtle previousTurtle=null;
	private int renderMode = 2;
	
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
	
	public void render(GL2 gl2,Turtle turtleToRender,PlotterSettings settings) {
		if(previousTurtle!=turtleToRender) {
			recalculateBuffer(gl2,turtleToRender,settings);
			previousTurtle = turtleToRender;
		}
		
		drawBufferedTurtle(gl2);
	}

	private void drawBufferedTurtle(GL2 gl2) {
		gl2.glPushMatrix();
		gl2.glLineWidth(1);
		gl2.glBegin(GL2.GL_LINE_STRIP);

		for( ColorPoint a : buffer ) {
			gl2.glColor3d(a.c.x, a.c.y, a.c.z);
			gl2.glVertex2d(a.p.x, a.p.y);
		}
		
		gl2.glEnd();
		gl2.glPopMatrix();
	}

	private void recalculateBuffer(GL2 gl2, Turtle turtleToRender, final PlotterSettings settings) {
		buffer.clear();
		
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
		boolean useDistance=true;
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
		
		boolean showNominal=false;
		if(showNominal) {
			Vector3d o = new Vector3d(-block.normal.y,block.normal.x,0);
			double f = block.nominalSpeed / settings.getDrawFeedRate();
			o.scale(f*5);
			o.add(block.start);
			Vector3d black = new Vector3d(1-f,f,0);
			buffer.add(new ColorPoint(black,block.start));
			buffer.add(new ColorPoint(black,o));
			buffer.add(new ColorPoint(black,block.start));
		}
		boolean showEntry=false;
		if(showEntry) {
			Vector3d o = new Vector3d(-block.normal.y,block.normal.x,0);
			double f = block.entrySpeed / settings.getDrawFeedRate();
			o.scale(f*5);
			o.add(block.start);
			Vector3d red = new Vector3d(1-f,0,f);
			buffer.add(new ColorPoint(red,block.start));
			buffer.add(new ColorPoint(red,o));
			buffer.add(new ColorPoint(red,block.start));
		}
		boolean showExit=false;
		if(showExit) {
			Vector3d o = new Vector3d(-block.normal.y,block.normal.x,0);
			double f = block.exitSpeed / settings.getDrawFeedRate();
			o.scale(f*-5);
			o.add(block.start);
			Vector3d black = new Vector3d(0,1-f,f);
			buffer.add(new ColorPoint(black,block.start));
			buffer.add(new ColorPoint(black,o));
			buffer.add(new ColorPoint(black,block.start));
		}

		double v = 1;
		Vector3d pLast = block.start;
		if(a>0) {
			// accel part of block
			Vector3d p0 = new Vector3d(block.delta);
			p0.scale(a/t);
			p0.add(block.start);
			Vector3d green = new Vector3d(0,v,0);
			buffer.add(new ColorPoint(green,block.start));
			buffer.add(new ColorPoint(green,p0));
			pLast=p0;
		}
		if(a<d) {
			// nominal part of block
			Vector3d p1 = new Vector3d(block.delta);
			p1.scale(d/t);
			p1.add(block.start);
			Vector3d blue = new Vector3d(0,0,v);
			buffer.add(new ColorPoint(blue,pLast));
			buffer.add(new ColorPoint(blue,p1));
			pLast=p1;
		}
		// decel part of block
		Vector3d red = new Vector3d(v,0,0);
		buffer.add(new ColorPoint(red,pLast));
		buffer.add(new ColorPoint(red,block.end));
	}
}
