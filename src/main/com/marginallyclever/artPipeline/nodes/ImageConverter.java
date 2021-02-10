package com.marginallyclever.artPipeline.nodes;

import com.marginallyclever.artPipeline.nodeConnector.NodeConnectorTransformedImage;
import com.marginallyclever.artPipeline.nodeConnector.NodeConnectorTurtle;
import com.marginallyclever.core.Clipper2D;
import com.marginallyclever.core.Point2D;
import com.marginallyclever.core.TransformedImage;
import com.marginallyclever.core.node.Node;
import com.marginallyclever.core.turtle.Turtle;

/**
 * Converts a BufferedImage to Turtle
 * 
 * in order to be found by the ServiceLoader.  This is so that you could write an independent plugin and 
 * drop it in the same folder as makelangelo software to be "found" by the software.
 * 
 * Don't forget http://www.reverb-marketing.com/wiki/index.php/When_a_new_style_has_been_added_to_the_Makelangelo_software
 * @author Dan Royer
 *
 */
public abstract class ImageConverter extends Node {
	public NodeConnectorTransformedImage inputImage = new NodeConnectorTransformedImage("ImageConverter.inputImage");
	public NodeConnectorTurtle outputTurtle = new NodeConnectorTurtle("ImageConverter.outputTurtle");
	
	
	protected ImageConverter() {
		super();
		inputs.add(inputImage);
		outputs.add(outputTurtle);
	}
	
	/**
	 * Drag the pen across the paper from p0 to p1, sampling (p1-p0)/stepSize times.  If the intensity of img
	 * at a sample location is greater than the channelCutff, raise the pen.  Print the gcode results to out.
	 * This method is used by several converters.
	 * 
	 * @param turtle the turtle being affected.
	 * @param x0 starting position on the paper.
	 * @param y0 starting position on the paper.
	 * @param x1 ending position on the paper.
	 * @param y1 ending position on the paper.
	 * @param stepSize mm level of detail for this line.
	 * @param channelCutoff only put pen down when color below this amount.
	 * @param img the image to sample while converting along the line.
	 */
	protected void convertAlongLine(Turtle turtle,double x0,double y0,double x1,double y1,double stepSize,double channelCutoff,TransformedImage img) {
		Point2D P0 = new Point2D(x0,y0);
		Point2D P1 = new Point2D(x1,y1);

		double [] bounds = img.getBounds();
		Point2D rMax = new Point2D(bounds[TransformedImage.RIGHT],bounds[TransformedImage.TOP]);
		Point2D rMin = new Point2D(bounds[TransformedImage.LEFT],bounds[TransformedImage.BOTTOM]);
		if(!Clipper2D.clipLineToRectangle(P0, P1, rMax, rMin)) {
			// entire line clipped
			return;
		}
		
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
			if(v<channelCutoff) turtle.penDown();
			else turtle.penUp();
			turtle.moveTo(x,y);
		}
	}
	

	/**
	 * Drag the pen across the paper from p0 to p1, sampling (p1-p0)/stepSize times.  If the intensity of img
	 * at a sample location is greater than the channelCutff, raise the pen.  Print the gcode results to out.
	 * This method is used by several converters.
	 * 
	 * @param turtle the turtle being affected.
	 * @param x0 starting position on the paper.
	 * @param y0 starting position on the paper.
	 * @param x1 ending position on the paper.
	 * @param y1 ending position on the paper.
	 * @param stepSize mm level of detail for this line.
	 * @param channelCutoff only put pen down when color below this amount.
	 * @param img the image to sample while converting along the line.
	 */
	protected void convertAlongLineErrorTerms(Turtle turtle,double x0,double y0,double x1,double y1,double stepSize,double channelCutoff,double [] error0,double [] error1,TransformedImage img) {
		double b;
		double dx=x1-x0;
		double dy=y1-y0;
		double halfStep = stepSize/2.0;
		double distance = Math.sqrt(dx*dx+dy*dy);

		double [] bounds = img.getBounds();
		double yBottom = bounds[TransformedImage.BOTTOM];
		double yTop    = bounds[TransformedImage.TOP];
		double xLeft   = bounds[TransformedImage.LEFT];
		double xRight  = bounds[TransformedImage.RIGHT];
		
		double n,fx,fy,oldPixel,newPixel;

		boolean wasInside = false;
		boolean isInside;
		boolean penUp;
		int steps=0;

		for (b = 0; b <= distance; b+=stepSize) {
			n = b / distance;
			fx = dx * n + x0;
			fy = dy * n + y0;
			isInside = (fx>=xLeft && fx<xRight && fy>=yBottom && fy<yTop);
			if(isInside) {
				oldPixel = img.sample( fx - halfStep, fy - halfStep, fx + halfStep, fy + halfStep);
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
			turtle.moveTo(fx,fy);
			steps++;
			
			if( wasInside && !isInside ) break;  // done
			wasInside=isInside;
		}
		turtle.penUp();
	}
}
