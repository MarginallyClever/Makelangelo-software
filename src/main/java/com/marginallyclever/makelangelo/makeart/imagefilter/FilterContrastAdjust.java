package com.marginallyclever.makelangelo.makeart.imagefilter;

import com.marginallyclever.convenience.ResizableImagePanel;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.rangeslider.RangeSlider;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.stream.IntStream;

/**
 * Adjusts the top and bottom of the constrast curve.
 * @author Dan Royer
 * @since 7.39.9
 */
public class FilterContrastAdjust extends ImageFilter {
	private final TransformedImage img;
	private final int bottom;
	private final float range;

	/**
	 * Scale the colors so that bottom...top becomes 0...255
	 * @param bottom the new bottom range
	 * @param top the new top range.
	 */
	public FilterContrastAdjust(TransformedImage img, int bottom, int top) {
		super();
		this.img = img;
		this.bottom = bottom;
		range = 255f / (top-bottom);
	}

	@Override
	public TransformedImage filter() {
		int h = img.getSourceImage().getHeight();
		int w = img.getSourceImage().getWidth();
		BufferedImage bi = img.getSourceImage();
		TransformedImage after = new TransformedImage(img);
		BufferedImage afterBI = after.getSourceImage();

		var raster = bi.getRaster();
		var afterRaster = afterBI.getRaster();
		var count = bi.getColorModel().getNumComponents();
		// Temporary array to hold pixel components

		IntStream.range(0, h).parallel().forEach(y -> {
			int[] pixel = new int[count];
			for (int x = 0; x < w; ++x) {
				raster.getPixel(x, y, pixel);
				for(int i = 0; i < count; ++i) {
					pixel[i] = adjust(pixel[i]);
				}
				afterRaster.setPixel(x, y, pixel);
			}
		});

		return after;
	}

	private int adjust(int color) {
		color = Math.max(color-bottom,0);
		return Math.min((int)(color*range),255);
	}

	public static void main(String[] args) throws IOException {
		TransformedImage src = new TransformedImage( ImageIO.read(new FileInputStream("src/test/resources/mandrill.png")) );
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		final RangeSlider slider = new RangeSlider(0,255);
		slider.setValue(0);
		slider.setUpperValue(255);
		panel.add(slider,BorderLayout.NORTH);

		slider.addChangeListener(e->{
			FilterContrastAdjust f = new FilterContrastAdjust(src,slider.getValue(),slider.getUpperValue());
			ResizableImagePanel rip = new ResizableImagePanel(f.filter().getSourceImage());
			BorderLayout layout = (BorderLayout)panel.getLayout();
			Component c = layout.getLayoutComponent(BorderLayout.CENTER);
			if(c!=null) panel.remove(c);
			panel.add(rip,BorderLayout.CENTER);
			rip.revalidate();
			rip.repaint();
		});

		JFrame frame = new JFrame("Filter_ContrastAdjust");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 500);
		frame.add(panel);
		frame.setVisible(true);
	}
}