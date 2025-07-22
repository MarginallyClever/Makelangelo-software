package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import com.marginallyclever.convenience.Clipper2D;
import com.marginallyclever.convenience.helpers.MathHelper;

import com.marginallyclever.makelangelo.Mesh;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.donatello.select.Select;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point2d;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts a {@link TransformedImage} to {@link Turtle}
 * @author Dan Royer
 */
public abstract class ImageConverter {
	private static final Logger logger = LoggerFactory.getLogger(ImageConverter.class);
	protected TransformedImage myImage;
	protected Paper myPaper;
	protected PlotterSettings settings;
	protected Turtle turtle = new Turtle();

	// for previewing the image
	private Texture texture = null;

	private final List<Select> panelElements = new ArrayList<>();

	public void setPlotterSettings(PlotterSettings arg0) {
		settings = arg0;
	}

	/**
	 * @return the translated name.
	 */
	abstract public String getName();

	/**
	 * Start the conversion process.
	 * @param paper the bounds of the final output.
	 * @param img the {@code TransformedImage} this filter is using as source material.
	 */
	public void start(Paper paper,TransformedImage img) {
		myPaper = paper;
		myImage = img;
		texture = null;
	}

	/**
	 * Stop the conversion process.  Called by the GUI when the user cancels the conversion, either by changing the
	 * style or halting altogether.  In both cases the conversion should be aborted.
	 */
	public void stop() {}

	/**
	 * Live preview as the system is converting pictures.
	 * draw the results as the calculation is being performed.
	 */
	protected void render(GL3 gl) {
		if( texture==null && myImage!=null) {
			texture = AWTTextureIO.newTexture(gl.getGLProfile(), myImage.getSourceImage(), false);
		}
		if(texture!=null) {
			double w = myImage.getSourceImage().getWidth() * myImage.getScaleX();
			double h = myImage.getSourceImage().getHeight() * myImage.getScaleY();
			gl.glEnable(GL3.GL_TEXTURE_2D);
			gl.glEnable(GL3.GL_BLEND);
			gl.glBlendFunc(GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA);
			gl.glDisable(GL3.GL_COLOR);
			texture.bind(gl);
			Mesh mesh = new Mesh();
			mesh.setRenderStyle(GL3.GL_TRIANGLE_FAN);
			mesh.addColor(1, 1, 1,0.5f);
			mesh.addTexCoord(0, 0);	mesh.addVertex((float)-w/2, (float)-h/2,0);
			mesh.addTexCoord(1, 0);	mesh.addVertex((float) w/2, (float)-h/2,0);
			mesh.addTexCoord(1, 1);	mesh.addVertex((float) w/2, (float) h/2,0);
			mesh.addTexCoord(0, 1);	mesh.addVertex((float)-w/2, (float) h/2,0);
			mesh.render(gl);
			gl.glDisable(GL3.GL_TEXTURE_2D);
			gl.glDisable(GL3.GL_BLEND);
			gl.glEnable(GL3.GL_COLOR);
		}	
	}
	
	/**
	 * Drag the pen across the paper from p0 to p1, sampling (p1-p0)/stepSize times.  If the intensity of img
	 * at a sample location is greater than the channelCutff, raise the pen.  Print the gcode results to out.
	 * This method is used by several converters.
	 * 
	 * @param x0 starting position on the paper.
	 * @param y0 starting position on the paper.
	 * @param x1 ending position on the paper.
	 * @param y1 ending position on the paper.
	 * @param stepSize mm level of detail for this line.
	 * @param channelCutoff only put pen down when color below this amount.
	 * @param img the image to sample while converting along the line.
	 */
	protected void convertAlongLine(double x0,double y0,double x1,double y1,double stepSize,double channelCutoff,TransformedImage img) {
		Point2d P0 = new Point2d(x0,y0);
		Point2d P1 = new Point2d(x1,y1);
		Rectangle2D.Double rect = myPaper.getMarginRectangle();
		Point2d rMax = new Point2d(rect.getMaxX(),rect.getMaxY());
		Point2d rMin = new Point2d(rect.getMinX(),rect.getMinY());
		if(!Clipper2D.clipLineToRectangle(P0, P1, rMax, rMin)) {
			// entire line clipped
			return;
		}

		double cx = myPaper.getCenterX();
		double cy = myPaper.getCenterY();
		var o = turtle.getPosition();
		o.sub(P0);
		boolean firstJump = MathHelper.lengthSquared(o.x, o.y)>2;
		if(firstJump) turtle.jumpTo(cx+P0.x,cy+P0.y);

		double b;
		double dx=P1.x-P0.x;
		double dy=P1.y-P0.y;
		double halfStep = stepSize/2.0;
		double distance = Math.sqrt(dx*dx+dy*dy);
		double n,x,y,v;
		
		for( b = 0; b <= distance; b+=stepSize ) {
			n = b / distance;
			x = dx * n + P0.x;
			y = dy * n + P0.y;
			
			v = img.sample( x, y , halfStep);

			x += cx;
			y += cy;
			if(v<channelCutoff) turtle.moveTo(x,y);
			else turtle.jumpTo(x,y);
		}
	}
	
	/**
	 * Drag the pen across the paper from p0 to p1, sampling (p1-p0)/stepSize times.  If the intensity of img
	 * at a sample location is greater than the channelCutff, raise the pen.  Print the gcode results to out.
	 * This method is used by several converters.
	 * 
	 * @param x0 starting position on the paper.
	 * @param y0 starting position on the paper.
	 * @param x1 ending position on the paper.
	 * @param y1 ending position on the paper.
	 * @param stepSize mm level of detail for this line.
	 * @param channelCutoff only put pen down when color below this amount.
	 * @param img the image to sample while converting along the line.
	 */
	protected void convertAlongLineErrorTerms(double x0,double y0,double x1,double y1,double stepSize,double channelCutoff,double [] error0,double [] error1,TransformedImage img) {
		double b;
		double dx=x1-x0;
		double dy=y1-y0;
		double halfStep = stepSize/2.0;
		double distance = Math.sqrt(dx*dx+dy*dy);

		double n,x,y,oldPixel,newPixel;

		boolean wasInside = false;
		boolean isInside;
		boolean penUp;
		int steps=0;

		Rectangle2D.Double rect = myPaper.getMarginRectangle();

		for (b = 0; b <= distance; b+=stepSize) {
			n = b / distance;
			x = dx * n + x0;
			y = dy * n + y0;
			isInside = rect.contains(x,y);
			if(isInside) {
				oldPixel = img.sample( x, y, halfStep);
				int b2 = (int)Math.min(b, error0.length-2);
				oldPixel += error0[b2];
				newPixel = oldPixel>=channelCutoff? 255:0;
				double quantError = oldPixel - newPixel;
				if(b2+1< steps) error0[b2+1] += quantError * 7.0/16.0;
				if(b2-1>=0    ) error1[b2-1] += quantError * 3.0/16.0;
				                error1[b2  ] += quantError * 5.0/16.0;
				if(b2+1< steps) error1[b2+1] += quantError * 1.0/16.0;
				
				penUp = (newPixel==255);
			} else {
				penUp=true;
			}
			if(penUp) turtle.penUp();
			else turtle.penDown();
			turtle.moveTo(x,y);
			steps++;
			
			if( wasInside && !isInside ) break;  // done
			wasInside=isInside;
		}
		turtle.penUp();
	}

	// Observer pattern notified when a converter has finished a job.
	private final List<ImageConverterListener> listeners = new ArrayList<>();

	public void addImageConverterListener(ImageConverterListener listener) {
		listeners.add(listener);
	}

	public void removeImageConverterListener(ImageConverterListener listener) {
		listeners.remove(listener);
	}

	protected void fireRestart() {
		for(ImageConverterListener listener:listeners) listener.onRestart(this);
	}

	/**
	 * Called when the converter has successfully finished a job.
	 */
	protected void fireConversionFinished() {
		logger.debug("fire conversion finished");
		for(ImageConverterListener listener : listeners) {
			listener.onConvertFinished(turtle);
		}
	}

	public void add(Select element) {
		panelElements.add(element);
	}
	public List<Select> getPanelElements() {
		return panelElements;
	}

	/**
	 * @return the plotter settings for this converter.
	 */
	public PlotterSettings getPlotterSettings() {
		if(settings == null) {
			throw new RuntimeException("PlotterSettings is null.  Did you forget to call setPlotterSettings()?");
		}
		return settings;
	}
}
