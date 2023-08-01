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

/**
 * Adjusts the top and bottom of the constrast curve.
 * @author Dan Royer
 * @since 7.39.9
 */
public class Filter_ContrastAdjust extends ImageFilter {
	private final int bottom;
	private final float range;

	/**
	 * Scale the colors so that bottom...top becomes 0...255
	 * @param bottom the new bottom range
	 * @param top the new top range.
	 */
	public Filter_ContrastAdjust(int bottom,int top) {
		super();
		this.bottom = bottom;
		range = 255f / (top-bottom);
	}

	public TransformedImage filter(TransformedImage img) {
		int h = img.getSourceImage().getHeight();
		int w = img.getSourceImage().getWidth();
		BufferedImage bi = img.getSourceImage();
		TransformedImage after = new TransformedImage(img);
		BufferedImage afterBI = after.getSourceImage();

		for (int y = 0; y < h; ++y) {
			for (int x = 0; x < w; ++x) {
				int color = bi.getRGB(x, y);
				int red = adjust(red32(color));
				int green = adjust(green32(color));
				int blue = adjust(blue32(color));
				int alpha = alpha32(color);

				afterBI.setRGB(x, y, ImageFilter.encode32bit(red,green,blue,alpha));
			}
		}

		return after;
	}

	private int adjust(int color) {
		color = Math.max(color-bottom,0);
		return Math.min((int)(color*range),255);
	}

	public static void main(String[] args) throws IOException {
		TransformedImage src = new TransformedImage( ImageIO.read(new FileInputStream("src/test/resources/Lenna.png")) );
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		final RangeSlider slider = new RangeSlider(0,255);
		slider.setValue(0);
		slider.setUpperValue(255);
		panel.add(slider,BorderLayout.NORTH);

		slider.addChangeListener(e->{
			Filter_ContrastAdjust f = new Filter_ContrastAdjust(slider.getValue(),slider.getUpperValue());
			TransformedImage dest = f.filter(src);
			ResizableImagePanel rip = new ResizableImagePanel(dest.getSourceImage());
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