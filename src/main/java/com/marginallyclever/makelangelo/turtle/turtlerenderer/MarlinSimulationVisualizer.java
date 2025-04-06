package com.marginallyclever.makelangelo.turtle.turtlerenderer;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.plotter.marlinsimulation.MarlinSimulation;
import com.marginallyclever.makelangelo.plotter.marlinsimulation.MarlinSimulationBlock;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.turtle.Line2d;
import com.marginallyclever.makelangelo.turtle.Turtle;

import javax.vecmath.Point2d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.geom.Line2D;
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
	static private class ColorPoint {
		public Vector3d c;
		public Vector3d p;

		public ColorPoint(Vector3d cc, Vector3d pp) {
			c=cc;
			p=pp;
		}
	};

	//private Turtle previousTurtle=null;
	private Graphics2D gl2;
	private final Turtle myTurtle = new Turtle();
	private Turtle previousTurtle = null;
	private PlotterSettings mySettings;
	private int renderMode = 0;
	private boolean useDistance = true;
	private boolean showNominal = false;
	private boolean showEntry = false;
	private boolean showExit = true;
	private final ArrayList<ColorPoint> buffer = new ArrayList<>();
	private final Line2D line = new Line2D.Double();
	
	public MarlinSimulationVisualizer() {}

	private void drawBufferedTurtle() {
		for (int i = 0; i < buffer.size() - 1; i++) {
			ColorPoint a = buffer.get(i);
			ColorPoint b = buffer.get(i + 1);

			GradientPaint gradient = new GradientPaint(
					0,0, new Color((float)a.c.x, (float)a.c.y, (float)a.c.z),
					0,0, new Color((float)b.c.x, (float)b.c.y, (float)b.c.z)
			);

			gl2.setPaint(gradient);
			line.setLine(a.p.x, a.p.y, b.p.x, b.p.y);
			gl2.draw(line);
		}

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
		case 0 : c=new Vector3d(1,0,0); break;
		case 1 : c=new Vector3d(0,1,0); break;
		default: c=new Vector3d(0,0,1); break;
		}
		buffer.add(new ColorPoint(c,block.start));
		buffer.add(new ColorPoint(c,block.end));
	}

	private void renderMinLength(MarlinSimulationBlock block) {
		double d = block.distance / (mySettings.getDouble(PlotterSettings.MIN_SEGMENT_LENGTH)*2.0);
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
			double f = block.nominalSpeed / settings.getDouble(PlotterSettings.FEED_RATE_DRAW);
			o.scale(f);
			o.add(block.start);
			Vector3d black = new Vector3d(1-f,f,0);
			buffer.add(new ColorPoint(black,block.start));
			buffer.add(new ColorPoint(black,o));
			buffer.add(new ColorPoint(black,block.start));
		}
		if(showEntry) {
			Vector3d o = new Vector3d(ortho);
			double f = block.entrySpeed / settings.getDouble(PlotterSettings.FEED_RATE_DRAW);
			o.scale(f);
			o.add(block.start);
			Vector3d red = new Vector3d(1-f,0,f);
			buffer.add(new ColorPoint(red,block.start));
			buffer.add(new ColorPoint(red,o));
			buffer.add(new ColorPoint(red,block.start));
		}
		if(showExit) {
			Vector3d o = new Vector3d(ortho);
			double f = block.exitSpeed / settings.getDouble(PlotterSettings.FEED_RATE_DRAW);
			o.scale(f);
			o.add(block.start);
			Vector3d black = new Vector3d(0,1-f,f);
			buffer.add(new ColorPoint(black,block.start));
			buffer.add(new ColorPoint(black,o));
			buffer.add(new ColorPoint(black,block.start));
		}

		// accel part of block
		buffer.add(new ColorPoint(rainbow(block.entrySpeed / block.nominalSpeed),block.start));

		if(a<d) {
			// nominal part of block.  add point at start.
			Vector3d p0 = new Vector3d(block.delta);
			p0.scale(a/t);
			p0.add(block.start);
			buffer.add(new ColorPoint(rainbow(1),p0));

			Vector3d p1 = new Vector3d(block.delta);
			p1.scale(d/t);
			p1.add(block.start);
			buffer.add(new ColorPoint(rainbow(1),p1));
		} else if(a<t) {
			// not nominal, add a point anyhow for correct color
			Vector3d p0 = new Vector3d(block.delta);
			p0.scale(a/t);
			p0.add(block.start);
			double peakSpeed = block.entrySpeed + block.acceleration * block.accelerateUntilT;
			buffer.add(new ColorPoint(rainbow(peakSpeed / block.nominalSpeed),p0));
		}

		// decel part of block
		buffer.add(new ColorPoint(rainbow(block.exitSpeed / block.nominalSpeed),block.end));
	}

	// return a color from red to blue to green
	private Vector3d rainbow(double v) {
		v= Math.max(0,Math.min(1,v));
		double r=0,g=0,b;
		if(v<0.5) {
			r = 1.0 - v*2;
			b = v*2;
		} else {
			g = (v-0.5)*2;
			b = 1.0 - (v-0.5)*2;
		}
		return new Vector3d(r,g,b);
	}

	@Override
	public void start(Graphics2D gl2) {
		this.gl2 = gl2;
		myTurtle.getLayers().clear();
	}

	@Override
	public void draw(Point2d p0, Point2d p1) {
		myTurtle.getLayers().getLast().getAllPoints().add(p0);
	}

	@Override
	public void travel(Point2d p0, Point2d p1) {
		myTurtle.getLayers().getLast().add(new Line2d());
		myTurtle.getLayers().getLast().getAllPoints().add(p0);
	}

	@Override
	public void end() {
		if(previousTurtle!=myTurtle) {
			recalculateBuffer(myTurtle,mySettings);
			previousTurtle = myTurtle;
		}
		
		drawBufferedTurtle();
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
	}

	@Override
	public void setShowTravel(boolean showTravel) {}
}
