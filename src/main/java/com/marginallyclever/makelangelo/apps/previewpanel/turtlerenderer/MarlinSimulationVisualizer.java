package com.marginallyclever.makelangelo.apps.previewpanel.turtlerenderer;

import com.jogamp.opengl.GL3;
import com.marginallyclever.makelangelo.Mesh;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.plotter.marlinsimulation.MarlinSimulation;
import com.marginallyclever.makelangelo.plotter.marlinsimulation.MarlinSimulationBlock;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleMove;

import javax.vecmath.Vector3d;
import java.awt.*;

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
	static private class ColorPoint {
		public Vector3d c;
		public Vector3d p;

		public ColorPoint(Vector3d cc, Vector3d pp) {
			c=cc;
			p=pp;
		}
	};

	//private Turtle previousTurtle=null;
	private GL3 gl;
	private final Turtle myTurtle = new Turtle();
	private Turtle previousTurtle=null;
	private PlotterSettings mySettings;
	private int renderMode = 0;
	private boolean useDistance=true;
	private boolean showNominal=false;
	private boolean showEntry=false;
	private boolean showExit=true;
	private float penDiameter = 1;

	private final Mesh mesh = new Mesh();
	
	public MarlinSimulationVisualizer() {
		mesh.setRenderStyle(GL3.GL_TRIANGLES);
	}

	private void recalculateBuffer(Turtle turtleToRender, final PlotterSettings settings) {
		mesh.clear();

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
		Color c =
		switch(block.id % 3) {
		case 0  -> new Color(255,  0,  0);
		case 1  -> new Color(  0,255,  0);
		default -> new Color(  0,  0,255);
		};
		Line2QuadHelper.thicken(mesh,block.start,block.end,c,penDiameter);
	}

	private void renderMinLength(MarlinSimulationBlock block) {
		double d = block.distance / (mySettings.getDouble(PlotterSettings.MIN_SEGMENT_LENGTH)*2.0);
		d = Math.max(Math.min(d, 1), 0);
		double g = d;
		double r = 1-d;
		Line2QuadHelper.thicken(mesh,block.start,block.end,new Color((int)(255*r),(int)(255*g),0),penDiameter);
	}

	private void renderAccelDecel(MarlinSimulationBlock block,PlotterSettings settings) {
		double distance,accelerateUntil,decelerateAfter;
		if(useDistance) {
			distance = block.distance;
			accelerateUntil = block.accelerateUntilD;
			decelerateAfter = block.decelerateAfterD;
		} else {
			// use time
			distance = block.end_s;
			accelerateUntil = block.accelerateUntilT;
			decelerateAfter = block.decelerateAfterT;
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
			double f = block.nominalSpeed / settings.getDouble(PlotterSettings.FEED_RATE_DRAW);
			o.scale(f);
			o.add(block.start);
			var c = new Color((int)(255*(1-f)),(int)(255*f),0);
			Line2QuadHelper.thicken(mesh,block.start,o,c,penDiameter);
		}
		if(showEntry) {
			Vector3d o = new Vector3d(ortho);
			double f = block.entrySpeed / settings.getDouble(PlotterSettings.FEED_RATE_DRAW);
			o.scale(f);
			o.add(block.start);
			var c = new Color((int)(255*(1-f)),0,(int)(255*f));
			Line2QuadHelper.thicken(mesh,block.start,o,c,penDiameter);
		}
		if(showExit) {
			Vector3d o = new Vector3d(ortho);
			double f = block.exitSpeed / settings.getDouble(PlotterSettings.FEED_RATE_DRAW);
			o.scale(f);
			o.add(block.start);
			var c = new Color(0,(int)(255*(1-f)),(int)(255*f));
			Line2QuadHelper.thicken(mesh,block.start,o,c,penDiameter);
		}

		Color c0,c1;
		Vector3d p0,p1;

		// accel part of block
		p0 = block.start;
		c0 = rainbow(block.entrySpeed / block.nominalSpeed);

		if(accelerateUntil<decelerateAfter) {
			// There is some nominal part of the block.  Add point at start.
			p1 = new Vector3d(block.delta);
			p1.scale(accelerateUntil/distance);
			p1.add(block.start);
			c1 = rainbow(1);
			Line2QuadHelper.thicken(mesh,p0,p1,c0,c1,penDiameter);
			p0 = p1;
			c0 = c1;

			p1 = new Vector3d(block.delta);
			p1.scale(decelerateAfter/distance);
			p1.add(block.start);
			c1 = rainbow(1);
			Line2QuadHelper.thicken(mesh,p0,p1,c0,c1,penDiameter);
			p0 = p1;
			c0 = c1;
		} else {
			// not nominal, add a point anyhow for correct color
			p1 = new Vector3d(block.delta);
			p1.scale(accelerateUntil/distance);
			p1.add(block.start);
			double peakSpeed = block.entrySpeed + block.acceleration * block.accelerateUntilT;
			c1 = rainbow(peakSpeed / block.nominalSpeed);
			Line2QuadHelper.thicken(mesh,p0,p1,c0,c1,penDiameter);
			p0 = p1;
			c0 = c1;
		}

		// decel part of block
		p1=block.end;
		c1 = rainbow(block.exitSpeed / block.nominalSpeed);
		Line2QuadHelper.thicken(mesh,p0,p1,c0,c1,penDiameter);
	}

	// return a color from red to blue to green
	private Color rainbow(double v) {
		v= Math.max(0,Math.min(1,v));
		double r=0,g=0,b;
		if(v<0.5) {
			r = 1.0 - v*2;
			b = v*2;
		} else {
			g = (v-0.5)*2;
			b = 1.0 - (v-0.5)*2;
		}
		return new Color((int)(255*r),(int)(255*g),(int)(255*b));
	}


	@Override
	public void start(GL3 gl) {
		this.gl = gl;
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
		if(previousTurtle!=myTurtle) {
			recalculateBuffer(myTurtle,mySettings);
			previousTurtle = myTurtle;
		}
		mesh.render(gl);
	}

	@Override
	public void setPenDownColor(Color color) {}

	@Override
	public void setPenUpColor(Color color) {}

	@Override
	public void setPenDiameter(double d) {}

    @Override
    public String getTranslatedName() {
        return Translator.get("MarlinSimulationVisualizer.name");
    }

    public void setSettings(PlotterSettings e) {
		mySettings = e;
	}

	/**
	 * Reset any internal state to defaults.  This makes sure rendering optimizations cleaned
	 * up when the turtle is changed.
	 */
	@Override
	public void reset() {
		previousTurtle=null;
		mesh.clear();
	}
}
