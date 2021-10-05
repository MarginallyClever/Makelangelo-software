package com.marginallyclever.makelangelo.makeArt.imageConverter;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import com.marginallyclever.convenience.Clipper2D;
import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangelo.makeArt.ImageManipulator;
import com.marginallyclever.makelangelo.makeArt.TransformedImage;
import com.marginallyclever.makelangelo.makeArt.io.image.LoadImage;
import com.marginallyclever.makelangeloRobot.PlotterDecorator;

/**
 * Converts a {@link BufferedImage} to {@link Turtle}
 * @author Dan Royer
 */
public abstract class ImageConverter extends ImageManipulator implements PlotterDecorator {
	protected TransformedImage sourceImage;
	protected boolean keepIterating=false;
	protected Texture texture = null;

	public static LoadImage loadImage;

	public void restart() {
		if(!keepIterating) {
			if(loadImage!=null) {
				loadImage.reconvert();
			}
			return;
		}
	}
	
	/**
	 * set the image to be transformed.
	 * @param img the <code>java.awt.image.BufferedImage</code> this filter is using as source material.
	 */
	public void setImage(TransformedImage img) {
		sourceImage=img;
		texture = null;
	}
	
	/**
	 * run one "step" of an iterative image conversion process.
	 * @return true if conversion should iterate again.
	 */
	public boolean iterate() {
		return false;
	}
	
	public void stopIterating() {
		keepIterating=false;
	}
	
	/**
	 * for "run once" converters, return do the entire conversion and write to disk.
	 * for iterative solvers, the iteration is now done, write to disk.
	 */
	public void finish() {}
	
	/**
	 * @return the gui panel with options for this manipulator
	 */
	public ImageConverterPanel getPanel() {
		return null;
	}

	/**
	 * Live preview as the system is converting pictures.
	 * draw the results as the calculation is being performed.
	 */
	public void render(GL2 gl2) {
		if( texture==null ) {
			if( sourceImage!=null) {
				texture = AWTTextureIO.newTexture(gl2.getGLProfile(), sourceImage.getSourceImage(), false);
			}
		}
		if(texture!=null) {
			double w = sourceImage.getSourceImage().getWidth() * sourceImage.getScaleX();
			double h = sourceImage.getSourceImage().getHeight() * sourceImage.getScaleY();
			gl2.glEnable(GL2.GL_TEXTURE_2D);
			gl2.glEnable(GL2.GL_BLEND);
			gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
			gl2.glDisable(GL2.GL_COLOR);
			gl2.glColor4f(1, 1, 1,0.5f);
			gl2.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);
			texture.bind(gl2);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
			gl2.glTexCoord2d(0, 0);	gl2.glVertex2d(-w/2, -h/2 );
			gl2.glTexCoord2d(1, 0);	gl2.glVertex2d( w/2, -h/2 );
			gl2.glTexCoord2d(1, 1);	gl2.glVertex2d( w/2, h/2);
			gl2.glTexCoord2d(0, 1);	gl2.glVertex2d(-w/2, h/2);
			gl2.glEnd();
			gl2.glDisable(GL2.GL_TEXTURE_2D);
			gl2.glDisable(GL2.GL_BLEND);
			gl2.glEnable(GL2.GL_COLOR);
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
		Point2D P0 = new Point2D(x0,y0);
		Point2D P1 = new Point2D(x1,y1);

		Point2D rMax = new Point2D(settings.getMarginRight(),settings.getMarginTop());
		Point2D rMin = new Point2D(settings.getMarginLeft(),settings.getMarginBottom());
		if(!Clipper2D.clipLineToRectangle(P0, P1, rMax, rMin)) {
			// entire line clipped
			return;
		}
		
		double ox=turtle.getX()-P0.x;
		double oy=turtle.getY()-P0.y;
		boolean firstJump = MathHelper.lengthSquared(ox, oy)>2;
		if(firstJump) turtle.jumpTo(P0.x,P0.y);
			
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
			
			v = img.sample( x - halfStep, y - halfStep, x + halfStep, y + halfStep);

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

		for (b = 0; b <= distance; b+=stepSize) {
			n = b / distance;
			x = dx * n + x0;
			y = dy * n + y0;
			isInside=isInsidePaperMargins(x, y);
			if(isInside) {
				oldPixel = img.sample( x - halfStep, y - halfStep, x + halfStep, y + halfStep);
				int b2 = (int)b;
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
}
