import java.awt.image.BufferedImage;

	
/**
 * Converts an image to black & white, reduces contrast (washes it out)
 * @author Dan
 *
 */
public class Filter_BlackAndWhite extends Filter {
	float max_intensity, min_intensity;
	float max_threshold, min_threshold;

	public BufferedImage Process(BufferedImage img) {
		int h = img.getHeight();
		int w = img.getWidth();
		int x,y,i;

		max_intensity=-1000;
		min_intensity=1000;
		for(y=0;y<h;++y) {
			for(x=0;x<w;++x) {
				i=decode(img.getRGB(x, y));
				if(max_intensity<i) max_intensity=i;
				if(min_intensity>i) min_intensity=i;
				img.setRGB(x, y, encode(i));
			}
		}
		System.out.println("min_intensity="+min_intensity);
		System.out.println("max_intensity="+max_intensity);
		
		for(y=0;y<h;++y) {
			for(x=0;x<w;++x) {
				i=decode(img.getRGB(x, y));
				
				float a = (float)(i - min_intensity) / (float)(max_intensity - min_intensity);
				int b = (int)( a * 95.0f + 200.0f );
				if(b>255) b=255;
				//if(b==255) System.out.println(x+"\t"+y+"\t"+i+"\t"+b);
				img.setRGB(x, y, encode(b));
			}
		}
		
		return img;
	}
}
