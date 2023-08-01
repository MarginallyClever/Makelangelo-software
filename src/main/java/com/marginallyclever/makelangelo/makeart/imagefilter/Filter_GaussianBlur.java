package com.marginallyclever.makelangelo.makeart.imagefilter;

import com.marginallyclever.convenience.ResizableImagePanel;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.FileInputStream;
import java.io.IOException;


/**
 * Converts an image to N shades of grey.
 *
 * @author Dan
 */
public class Filter_GaussianBlur extends ImageFilter {
	private static final Logger logger = LoggerFactory.getLogger(Filter_GaussianBlur.class);
	int radius = 1;

	public Filter_GaussianBlur(int radius) {
		assert (radius >= 1);
		this.radius = radius;
	}

	@Override
	public TransformedImage filter(TransformedImage img) {
		BufferedImage src = img.getSourceImage();
	    TransformedImage after = new TransformedImage(img);
		BufferedImage dest = after.getSourceImage();
		BufferedImage inter = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
		getGaussianBlurFilter(true).filter(src,inter);
		getGaussianBlurFilter(false).filter(inter,dest);

		return after;
	}

	public ConvolveOp getGaussianBlurFilter(boolean horizontal) {
		int size = radius * 2 + 1;
		float[] data = new float[size];

		float sigma = radius / 3.0f;
		float twoSigmaSquare = 2.0f * sigma * sigma;
		float sigmaRoot = (float) Math.sqrt(twoSigmaSquare * Math.PI);
		float total = 0.0f;

		for (int i = -radius; i <= radius; i++) {
			float distance = i * i;
			int index = i + radius;
			data[index] = (float) Math.exp(-distance / twoSigmaSquare) / sigmaRoot;
			total += data[index];
		}

		for (int i = 0; i < data.length; i++) {
			data[i] /= total;
		}

		Kernel kernel = null;
		if (horizontal) {
			kernel = new Kernel(size, 1, data);
		} else {
			kernel = new Kernel(1, size, data);
		}
		return new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
	}

	public static void main(String[] args) throws IOException {
		TransformedImage src = new TransformedImage( ImageIO.read(new FileInputStream("src/test/resources/Lenna.png")) );
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		final JSlider slider = new JSlider(1,20);
		slider.setValue(1);
		panel.add(slider,BorderLayout.NORTH);

		slider.addChangeListener(e->{
			Filter_GaussianBlur f = new Filter_GaussianBlur(slider.getValue());
			TransformedImage dest = f.filter(src);
			ResizableImagePanel rip = new ResizableImagePanel(dest.getSourceImage());
			BorderLayout layout = (BorderLayout)panel.getLayout();
			Component c = layout.getLayoutComponent(BorderLayout.CENTER);
			if(c!=null) panel.remove(c);
			panel.add(rip,BorderLayout.CENTER);
			rip.revalidate();
			rip.repaint();
		});

		JFrame frame = new JFrame("Filter_GaussianBlur");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 500);
		frame.add(panel);
		frame.setVisible(true);
	}
}
