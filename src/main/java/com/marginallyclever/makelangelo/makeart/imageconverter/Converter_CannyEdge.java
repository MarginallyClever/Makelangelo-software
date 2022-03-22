package com.marginallyclever.makelangelo.makeart.imageConverter;


import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.io.Writer;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imageFilter.Filter_BlackAndWhite;
import com.marginallyclever.makelangelo.makeart.imageFilter.Filter_Invert;

/**
 * 
 * @author Dan Royer
 */
public class Converter_CannyEdge extends ImageConverter {
	@Override
	public String getName() {
		return Translator.get("ConverterCannyEdgeName");
	}

	/**
	 * turn the image into a grid of boxes.  box size is affected by source image darkness.
	 * @param img the image to convert.
	 */
	public boolean convert(TransformedImage img, Writer out) throws IOException {
		// The picture might be in color.  Smash it to 255 shades of grey.
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		img = bw.filter(img);

		//create the detector
		CannyEdgeDetector detector = new CannyEdgeDetector();

		//adjust its parameters as desired
		detector.setLowThreshold(0.5f);
		detector.setHighThreshold(1f);

		//apply it to an image
		detector.setSourceImage(img.getSourceImage());
		detector.process();
		TransformedImage edges = new TransformedImage(detector.getEdgesImage());
		edges.copySettingsFrom(img);
		
		Filter_Invert inv = new Filter_Invert();
		edges = inv.filter(edges);
		
		// Now we have a bitmap of the edges.  How to turn that into vectors?
		
/*
		// Set up the conversion from image space to paper space, select the current tool, etc.
		imageStart(edges, out);
		// Make sure the pen is up for the first move
		liftPen(out);

		double pw = machine.getPaperWidth();
		//double ph = machine.GetPaperHeight();

		// figure out how many lines we're going to have on this image.
		float steps = (float) (pw / tool.getDiameter());
		if (steps < 1) steps = 1;

		float blockSize = (int) (imageWidth / steps);
		float halfstep = (float) blockSize / 2.0f;

		// from top to bottom of the image...
		float x, y, z;
		int i = 0;
		for (y = 0; y < imageHeight; y += blockSize) {
			++i;
			if ((i % 2) == 0) {
				// every even line move left to right
				//lineTo(file,x,y,pen up?)]
				for (x = 0; x < imageWidth - blockSize; x += blockSize) {
					// read a block of the image and find the average intensity in this block
					z = takeImageSampleBlock(img, (int) x, (int) (y - halfstep), (int) (x + blockSize), (int) (y + halfstep));
					// scale the intensity value
					float scale_z = (255.0f - (float) z) / 255.0f;
					float pulse_size = (halfstep - 1.0f) * scale_z;
					if (pulse_size > 0.1f) {
						// draw a square.  the diameter is relative to the intensity.
						lineTo(out, x + halfstep - pulse_size, y + halfstep - pulse_size, true);
						lineTo(out, x + halfstep + pulse_size, y + halfstep - pulse_size, false);
						lineTo(out, x + halfstep + pulse_size, y + halfstep + pulse_size, false);
						lineTo(out, x + halfstep - pulse_size, y + halfstep + pulse_size, false);
						lineTo(out, x + halfstep - pulse_size, y + halfstep - pulse_size, false);
						lineTo(out, x + halfstep - pulse_size, y + halfstep - pulse_size, true);
					}
				}
			} else {
				// every odd line move right to left
				//lineTo(file,x,y,pen up?)]
				for (x = imageWidth - blockSize; x >= 0; x -= blockSize) {
					// read a block of the image and find the average intensity in this block
					z = takeImageSampleBlock(img, (int) (x - blockSize), (int) (y - halfstep), (int) x, (int) (y + halfstep));
					// scale the intensity value
					float scale_z = (255.0f - (float) z) / 255.0f;
					float pulse_size = (halfstep - 1.0f) * scale_z;
					if (pulse_size > 0.1f) {
						// draw a square.  the diameter is relative to the intensity.
						lineTo(out, x - halfstep - pulse_size, y + halfstep - pulse_size, true);
						lineTo(out, x - halfstep + pulse_size, y + halfstep - pulse_size, false);
						lineTo(out, x - halfstep + pulse_size, y + halfstep + pulse_size, false);
						lineTo(out, x - halfstep - pulse_size, y + halfstep + pulse_size, false);
						lineTo(out, x - halfstep - pulse_size, y + halfstep - pulse_size, false);
						lineTo(out, x - halfstep - pulse_size, y + halfstep - pulse_size, true);
					}
				}
			}

			liftPen(out);
		    moveTo(out, (float)machine.getHomeX(), (float)machine.getHomeY(),true);
	    }*/
		return true;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		
	}
}
