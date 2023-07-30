package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.voronoi.VoronoiCell;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.tools.InfillTurtle;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.select.SelectBoolean;
import com.marginallyclever.makelangelo.select.SelectInteger;
import com.marginallyclever.makelangelo.select.SelectSlider;
import com.marginallyclever.makelangelo.turtle.Turtle;

/**
 * Voronoi graph based stippling.
 * See <a href="http://en.wikipedia.org/wiki/Fortune%27s_algorithm">...</a>
 * See <a href="http://skynet.ie/~sos/mapviewer/voronoi.php">...</a>
 * @author Dan
 * @since 7.0.0?
 */
public class Converter_VoronoiStippling extends Converter_Voronoi {
	private static double maxDotSize = 3.5;
	private static double minDotSize = 0.5;

	private final int TABLE_SIZE=10;
	private final double [] cosTable = new double[TABLE_SIZE+1];
	private final double [] sinTable = new double[TABLE_SIZE+1];

	public Converter_VoronoiStippling() {
		super();

		for(int i=0;i<=TABLE_SIZE;++i) {
			cosTable[i] = Math.cos(i*2.0*Math.PI/TABLE_SIZE);
			sinTable[i] = Math.sin(i*2.0*Math.PI/TABLE_SIZE);
		}

		SelectInteger selectCells = new SelectInteger("cells", Translator.get("Converter_VoronoiStippling.CellCount"), getNumCells());
		add(selectCells);
		selectCells.addPropertyChangeListener(evt -> {
			setNumCells((int) evt.getNewValue());
			fireRestart();
		});

		SelectSlider selectMax = new SelectSlider("max", Translator.get("Converter_VoronoiStippling.DotMax"), 50,1, (int)(getMaxDotSize()*10));
		add(selectMax);
		selectMax.addPropertyChangeListener(evt -> setMaxDotSize((int)evt.getNewValue()*0.1));

		SelectSlider selectMin = new SelectSlider("min", Translator.get("Converter_VoronoiStippling.DotMin"), 50,1, (int)(getMinDotSize()*10));
		add(selectMin);
		selectMin.addPropertyChangeListener(evt -> setMinDotSize((int)evt.getNewValue()*0.1));

		SelectBoolean selectDrawVoronoi = new SelectBoolean("drawVoronoi", Translator.get("Converter_VoronoiStippling.DrawBorders"), getDrawVoronoi());
		add(selectDrawVoronoi);
		selectDrawVoronoi.addPropertyChangeListener(evt -> setDrawVoronoi((boolean) evt.getNewValue()));
	}
	
	@Override
	public String getName() {
		return Translator.get("Converter_VoronoiStippling.Name");
	}

	@Override
	public void resume() {
		turtle = new Turtle();
		fireConversionFinished();
	}

	@Override
	public void start(Paper paper, TransformedImage image) {
		turtle = new Turtle();
		super.start(paper, image);
	}

	@Override
	public void render(GL2 gl2) {
		super.render(gl2);

		ImageConverterThread thread = getThread();
		if(thread==null || thread.getPaused()) return;

		lock.lock();
		try {
			renderDots(gl2);
		}
		finally {
			lock.unlock();
		}
	}

	private void renderDots(GL2 gl2) {
		double scale = maxDotSize/255.0;

		for( VoronoiCell c : cells ) {
			double x = c.center.x;
			double y = c.center.y;
			double r = c.weight * scale;
			if(r<minDotSize) continue;

			gl2.glColor3f((float)c.change, 0, 0);
			drawCircle(gl2,x,y,r);
		}
	}

	private void drawCircle(GL2 gl2,double x, double y, double r) {
		gl2.glBegin(GL2.GL_TRIANGLE_FAN);
		for (int j = 0; j <= TABLE_SIZE; ++j) {
			gl2.glVertex2d(
					x + r * cosTable[j],
					y + r * sinTable[j] );
		}
		gl2.glEnd();
	}

	/**
	 * write cell centers to gcode.
	 */
	@Override
	public void writeOutCells() {
		double scale = maxDotSize/255.0;

		for( VoronoiCell c : cells ) {
			double val = c.weight;

			double x = c.center.x;
			double y = c.center.y;
			double r = val * scale;
			if(r<minDotSize) continue;

			turtleCircle(x, y, r);
		}
	}

	// filled circles
	private void turtleCircle(double x, double y, double r) {
		if(r<1) return;

		int detail = (int)Math.max(4, Math.min(20,Math.ceil((r) * Math.PI * 2.0)));

		double r2 = r-0.5;

		Turtle circle = new Turtle();
		for(int j = 0; j <= detail; ++j) {
			double v = (double)j * 2.0 * Math.PI / (double)detail;
			double newX = x + r2 * Math.cos(v);
			double newY = y + r2 * Math.sin(v);
			if(j==0) circle.jumpTo(newX,newY);
			else circle.moveTo(newX,newY);
		}

		InfillTurtle filler = new InfillTurtle();
		try {
			turtle.add(circle);
			turtle.add(filler.run(circle));
		} catch(Exception ignored) {}
	}

	public void setMinDotSize(double value) {
		minDotSize = Math.max(0.001,value);
	}
	public double getMinDotSize() {
		return minDotSize;
	}

	public double getMaxDotSize() {
		return maxDotSize;
	}
	public void setMaxDotSize(double value) {
		value = Math.max(value,minDotSize+1);
		maxDotSize = value;
	}
}