import java.awt.image.BufferedImage;


	
/**
 * Floyd/Steinberg dithering
 * @author Dan
 * @see {@link http://en.literateprograms.org/Floyd-Steinberg_dithering_%28C%29}<br>
 * {@link http://www.home.unix-ag.org/simon/gimp/fsdither.c}
 */
public class Filter_DitherFloydSteinberg extends Filter {
	private long tone;
	
	private int QuantizeColor(int original) {
		int i=(int)Math.min(Math.max(original, 0),255);
		return ( i > tone ) ? 255 : 0;
	}
	
	
	private void DitherDirection(BufferedImage img,int y,int[] error,int[] nexterror,int direction) {
		int w = img.getWidth();
		int oldPixel, newPixel, quant_error;
		int start, end, x;

		for(x=0;x<w;++x) nexterror[x]=0;
		
		if(direction>0) {
			start=0;
			end=w;
		} else {
			start=w-1;
			end=-1;
		}
		
		// for each x from left to right
		for(x=start;x!=end;x+=direction) {
			// oldpixel := pixel[x][y]
			oldPixel = decode(img.getRGB(x, y)) + error[x];
			// newpixel := find_closest_palette_color(oldpixel)
			newPixel = QuantizeColor(oldPixel);
			// pixel[x][y] := newpixel
			img.setRGB(x, y, encode(newPixel));
			// quant_error := oldpixel - newpixel
			quant_error = oldPixel - newPixel;
			// pixel[x+1][y  ] := pixel[x+1][y  ] + 7/16 * quant_error
			// pixel[x-1][y+1] := pixel[x-1][y+1] + 3/16 * quant_error
			// pixel[x  ][y+1] := pixel[x  ][y+1] + 5/16 * quant_error
			// pixel[x+1][y+1] := pixel[x+1][y+1] + 1/16 * quant_error
				nexterror[x          ] += 5.0/16.0 * quant_error;
			if(x+direction>=0 && x+direction < w) {
				    error[x+direction] += 7.0/16.0 * quant_error;
				nexterror[x+direction] += 1.0/16.0 * quant_error;
			}
			if(x-direction>=0 && x-direction < w) {
				nexterror[x-direction] += 3.0/16.0 * quant_error;
			}
		}
	}
	
	
	public BufferedImage Process(BufferedImage img) {
		int y,x;
		int h = img.getHeight();
		int w = img.getWidth();
		int direction=1;
		int[] error=new int[w];
		int[] nexterror=new int[w];
		
		for(y=0;y<w;++y) {
			error[y]=nexterror[y]=0;
		}
		
		// find the average color of the system
		for(y=0;y<h;++y) {
			for(x=0;x<w;++x) {
				tone+=decode(img.getRGB(x,y));
			}
		}
		
		tone /= (w*h);
		
		
		// for each y from top to bottom
		for(y=0;y<h;++y) {
			DitherDirection(img,y,error,nexterror,direction);
			
			direction = direction> 0 ? -1 : 1;
			int [] tmp = error;
			error=nexterror;
			nexterror=tmp;
		}
		
		return img;
	}
}
