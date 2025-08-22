package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.DrawingHelper;
import com.marginallyclever.convenience.voronoi.VoronoiCell;
import com.marginallyclever.donatello.select.SelectSlider;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.turtletool.InfillTurtle;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.preview.ShaderProgram;
import com.marginallyclever.makelangelo.turtle.Turtle;

import java.awt.*;

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

	public Converter_VoronoiStippling() {
		super();

		SelectSlider selectMax = new SelectSlider("max", Translator.get("Converter_VoronoiStippling.DotMax"), 50,1, (int)(getMaxDotSize()*10));
		add(selectMax);
		selectMax.addSelectListener(evt -> setMaxDotSize((int)evt.getNewValue()*0.1));

		SelectSlider selectMin = new SelectSlider("min", Translator.get("Converter_VoronoiStippling.DotMin"), 50,1, (int)(getMinDotSize()*10));
		add(selectMin);
		selectMin.addSelectListener(evt -> setMinDotSize((int)evt.getNewValue()*0.1));
	}
	
	@Override
	public String getName() {
		return Translator.get("Converter_VoronoiStippling.Name");
	}

	@Override
	public void resume() {
		turtle = new Turtle();
		turtle.setStroke(Color.BLACK,settings.getDouble(PlotterSettings.DIAMETER));
		fireConversionFinished();
	}

	@Override
	public void start(Paper paper, TransformedImage image) {
		turtle = new Turtle();
		turtle.setStroke(Color.BLACK,settings.getDouble(PlotterSettings.DIAMETER));
		super.start(paper, image);
	}

	@Override
	public void render(ShaderProgram shader, GL3 gl) {
		super.render(shader, gl);

		ImageConverterThread thread = getThread();
		if(thread==null || thread.getPaused()) return;

		float cx = (float)myPaper.getCenterX();
		float cy = (float)myPaper.getCenterY();

		lock.lock();
		try {
			renderDots(shader,cx,cy);
		}
		finally {
			lock.unlock();
		}
	}

	private void renderDots(ShaderProgram shader,float sx,float sy) {
		int lpc = getLowpassCutoff();
		float scale = (float)(maxDotSize-minDotSize)/255.0f;
		float cx = (float)myPaper.getCenterX()+sx;
		float cy = (float)myPaper.getCenterY()+sy;

		for( VoronoiCell c : cells ) {
			if(c.weight<lpc) continue;
			float r = (float)(c.weight-lpc) * scale;
			float x = (float)c.center.x;
			float y = (float)c.center.y;
			DrawingHelper.drawCircle(shader.getContext(), cx+x, cy+y, r, new Color((float)c.change,0,0));
		}
	}

	/**
	 * write cell centers to gcode.
	 */
	@Override
	public void writeOutCells() {
		int lpc = getLowpassCutoff();
		double scale = (maxDotSize-minDotSize)/255.0;
		double cx = myPaper.getCenterX();
		double cy = myPaper.getCenterY();

		for( VoronoiCell c : cells ) {
			if(c.weight<lpc) continue;
			double r = (c.weight-lpc) * scale;
			double x = cx + c.center.x;
			double y = cy + c.center.y;
			turtleCircle(x, y, r);
		}
	}

	// filled circles
	private void turtleCircle(double x, double y, double r) {
		if(r<=minDotSize) return;

		int detail = (int)Math.max(4, Math.min(20,Math.ceil((r) * Math.PI * 2.0)));

		double r2 = r-0.5;

		Turtle circle = new Turtle();
		circle.setStroke(turtle.getColor(),settings.getDouble(PlotterSettings.DIAMETER));
		for(int j = 0; j <= detail; ++j) {
			double v = (double)j * 2.0 * Math.PI / (double)detail;
			double newX = x + r2 * Math.cos(v);
			double newY = y + r2 * Math.sin(v);
			if(j==0) circle.jumpTo(newX,newY);
			else circle.moveTo(newX,newY);
		}

		InfillTurtle filler = new InfillTurtle();
		filler.setPenDiameter(turtle.getDiameter());
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