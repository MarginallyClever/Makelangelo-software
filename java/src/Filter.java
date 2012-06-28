
/**
 * A base class for image filtering
 * @author Dan
 */
public class Filter {
	protected int decode(int pixel) {
		//pixel=(int)( Math.min(Math.max(pixel, 0),255) );
		float r = ((pixel>>16)&0xff);
		float g = ((pixel>> 8)&0xff);
		float b = ((pixel    )&0xff);
		return (int)( (r+g+b)/3 );
	}
	
	
	protected int encode(int i) {
		return (0xff<<24) | (i<<16) | (i<< 8) | i;
	}		
}